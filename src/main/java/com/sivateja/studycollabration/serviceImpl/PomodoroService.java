package com.sivateja.studycollabration.serviceImpl;
import com.sivateja.studycollabration.dto.PomodoroSession.PomodoroSessionDTO;
import com.sivateja.studycollabration.entities.*;
import com.sivateja.studycollabration.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class PomodoroService {

    private final PomodoroSessionRepository repo;
    private final RoomRepository roomRepo;
    private final SimpMessagingTemplate ws;

    // Get active session for a room (RUNNING or PAUSED)
    public PomodoroSessionDTO getActive(Long roomId) {
        return repo.findTopByRoomIdAndStatusInOrderByCreatedAtDesc(
                roomId, java.util.List.of("RUNNING", "PAUSED")
        ).map(this::toDTO).orElse(null);
    }

    // Start a new session
    public PomodoroSessionDTO start(Long roomId, String phase, Users user) {
        Room room = roomRepo.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // count today's completed pomodoros
        int count = repo.countByRoomIdAndPhaseAndStatusAndStartedAtAfter(
                roomId, "FOCUS", "FINISHED",
                LocalDateTime.now().toLocalDate().atStartOfDay()
        );

        PomodoroSession session = PomodoroSession.builder()
                .room(room)
                .startedBy(user)
                .phase(phase)
                .durationSeconds(phaseDuration(phase))
                .startedAt(LocalDateTime.now())
                .status("RUNNING")
                .pomodoroCount(count)
                .build();

        session = repo.save(session);
        PomodoroSessionDTO dto = toDTO(session);
        ws.convertAndSend("/topic/room/" + roomId + "/pomodoro", dto);
        return dto;
    }

    // Pause / Resume
    public PomodoroSessionDTO togglePause(Long sessionId, Users user) {
        PomodoroSession s = repo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        s.setStatus(s.getStatus().equals("RUNNING") ? "PAUSED" : "RUNNING");
        if (s.getStatus().equals("RUNNING")) s.setStartedAt(LocalDateTime.now());
        s = repo.save(s);
        PomodoroSessionDTO dto = toDTO(s);
        ws.convertAndSend("/topic/room/" + s.getRoom().getId() + "/pomodoro", dto);
        return dto;
    }

    // Finish / Stop
    public PomodoroSessionDTO finish(Long sessionId, Users user) {
        PomodoroSession s = repo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        s.setStatus("FINISHED");
        s = repo.save(s);
        PomodoroSessionDTO dto = toDTO(s);
        ws.convertAndSend("/topic/room/" + s.getRoom().getId() + "/pomodoro", dto);
        return dto;
    }

    private int phaseDuration(String phase) {
        return switch (phase) {
            case "SHORT_BREAK" -> 5 * 60;
            case "LONG_BREAK"  -> 15 * 60;
            default            -> 25 * 60; // FOCUS
        };
    }

    public PomodoroSessionDTO toDTO(PomodoroSession s) {
        return PomodoroSessionDTO.builder()
                .id(s.getId())
                .phase(s.getPhase())
                .durationSeconds(s.getDurationSeconds())
                .startedAt(s.getStartedAt())
                .status(s.getStatus())
                .pomodoroCount(s.getPomodoroCount())
                .startedById(s.getStartedBy() != null ? s.getStartedBy().getId() : null)
                .startedByName(s.getStartedBy() != null ? s.getStartedBy().getDisplayName() : null)
                .roomId(s.getRoom().getId())
                .build();
    }
}
