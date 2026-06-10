package com.sivateja.studycollabration.serviceImpl;

import com.sivateja.studycollabration.dto.PomodoroSession.PomodoroSessionDTO;
import com.sivateja.studycollabration.entities.*;
import com.sivateja.studycollabration.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PomodoroService {

    private final PomodoroSessionRepository repo;
    private final RoomRepository roomRepo;
    private final SimpMessagingTemplate ws;

    // ── Get active session for a room (RUNNING or PAUSED) ───────────────────
    public PomodoroSessionDTO getActive(Long roomId) {
        return repo.findTopByRoomIdAndStatusInOrderByCreatedAtDesc(
                roomId, List.of("RUNNING", "PAUSED")
        ).map(this::toDTO).orElse(null);
    }

    // ── Start a new session ──────────────────────────────────────────────────
    public PomodoroSessionDTO start(Long roomId, String phase, Users user) {
        Room room = roomRepo.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Guard: prevent duplicate active sessions
        boolean alreadyActive = repo.findTopByRoomIdAndStatusInOrderByCreatedAtDesc(
                roomId, List.of("RUNNING", "PAUSED")
        ).isPresent();
        if (alreadyActive) {
            throw new IllegalStateException("A session is already active for this room");
        }

        // Count today's completed focus pomodoros
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
                .elapsedSeconds(0)   // fresh session — no elapsed time yet
                .pomodoroCount(count)
                .build();

        session = repo.save(session);
        PomodoroSessionDTO dto = toDTO(session);
        ws.convertAndSend("/topic/room/" + roomId + "/pomodoro", dto);
        return dto;
    }

    // ── Pause / Resume ───────────────────────────────────────────────────────
    public PomodoroSessionDTO togglePause(Long sessionId, Users user) {
        PomodoroSession s = repo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (s.getStatus().equals("RUNNING")) {
            // ── Pausing: accumulate elapsed seconds from this run segment ──
            long segmentSeconds = Duration.between(s.getStartedAt(), LocalDateTime.now()).getSeconds();
            s.setElapsedSeconds((int) (s.getElapsedSeconds() + segmentSeconds));
            s.setPausedAt(LocalDateTime.now());
            s.setStatus("PAUSED");
        } else {
            // ── Resuming: reset startedAt for the new run segment ──
            s.setStartedAt(LocalDateTime.now());
            s.setStatus("RUNNING");
        }

        s = repo.save(s);
        PomodoroSessionDTO dto = toDTO(s);
        ws.convertAndSend("/topic/room/" + s.getRoom().getId() + "/pomodoro", dto);
        return dto;
    }

    // ── Finish / Stop ────────────────────────────────────────────────────────
    public PomodoroSessionDTO finish(Long sessionId, Users user) {
        PomodoroSession s = repo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        s.setStatus("FINISHED");
        s = repo.save(s);
        PomodoroSessionDTO dto = toDTO(s);
        ws.convertAndSend("/topic/room/" + s.getRoom().getId() + "/pomodoro", dto);
        return dto;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private int phaseDuration(String phase) {
        return switch (phase) {
            case "SHORT_BREAK" -> 5  * 60;
            case "LONG_BREAK"  -> 15 * 60;
            default            -> 25 * 60; // FOCUS
        };
    }

    public PomodoroSessionDTO toDTO(PomodoroSession s) {
        return PomodoroSessionDTO.builder()
                .id(s.getId())
                .phase(s.getPhase())
                .durationSeconds(s.getDurationSeconds())
                .elapsedSeconds(s.getElapsedSeconds())   // ← include accumulated time
                .startedAt(s.getStartedAt())
                .status(s.getStatus())
                .pomodoroCount(s.getPomodoroCount())
                .startedById(s.getStartedBy() != null ? s.getStartedBy().getId() : null)
                .startedByName(s.getStartedBy() != null ? s.getStartedBy().getDisplayName() : null)
                .roomId(s.getRoom().getId())
                .build();
    }
}
