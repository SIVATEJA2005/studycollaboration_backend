package com.sivateja.studycollabration.controllers;




import com.sivateja.studycollabration.entities.Users;
import com.sivateja.studycollabration.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.sivateja.studycollabration.serviceImpl.PomodoroService;
import com.sivateja.studycollabration.dto.PomodoroSession.PomodoroSessionDTO;
@RestController
@RequestMapping("/api/pomodoro")
@RequiredArgsConstructor
public class PomodoroController {

    private final PomodoroService pomodoroService;
    private final UserRepository userRepository;

    private Users getUser(UserDetails ud) {
        return userRepository.findByUserName(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // GET /api/pomodoro/room/{roomId}/active
    @GetMapping("/room/{roomId}/active")
    public ResponseEntity<PomodoroSessionDTO> getActive(@PathVariable Long roomId) {
        return ResponseEntity.ok(pomodoroService.getActive(roomId));
    }

    // POST /api/pomodoro/room/{roomId}/start?phase=FOCUS
    @PostMapping("/room/{roomId}/start")
    public ResponseEntity<PomodoroSessionDTO> start(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "FOCUS") String phase,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(pomodoroService.start(roomId, phase, getUser(ud)));
    }

    // PUT /api/pomodoro/{sessionId}/toggle
    @PutMapping("/{sessionId}/toggle")
    public ResponseEntity<PomodoroSessionDTO> toggle(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(pomodoroService.togglePause(sessionId, getUser(ud)));
    }

    // PUT /api/pomodoro/{sessionId}/finish
    @PutMapping("/{sessionId}/finish")
    public ResponseEntity<PomodoroSessionDTO> finish(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(pomodoroService.finish(sessionId, getUser(ud)));
    }
}
