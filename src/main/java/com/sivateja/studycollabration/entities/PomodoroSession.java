package com.sivateja.studycollabration.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pomodoro_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PomodoroSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "started_by")
    private Users startedBy;

    private String phase;           // FOCUS | SHORT_BREAK | LONG_BREAK
    private int durationSeconds;    // total duration for this phase
    private String status;          // RUNNING | PAUSED | FINISHED

    private LocalDateTime startedAt;   // start of the CURRENT run segment
    private LocalDateTime pausedAt;    // when it was last paused

    @Builder.Default
    private int elapsedSeconds = 0;    // accumulated seconds from all previous run segments

    private LocalDateTime createdAt;

    @Builder.Default
    private int pomodoroCount = 0;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
