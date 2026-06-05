package com.sivateja.studycollabration.entities;



import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PomodoroSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "started_by")
    private Users startedBy;

    // FOCUS | SHORT_BREAK | LONG_BREAK
    @Column(nullable = false)
    private String phase;

    // duration in seconds
    @Column(nullable = false)
    private int durationSeconds;

    // when the timer actually started (used by clients to sync)
    private LocalDateTime startedAt;

    // RUNNING | PAUSED | FINISHED
    @Column(nullable = false)
    private String status;

    // how many pomodoros completed today in this room
    private int pomodoroCount;

    @CreationTimestamp
    private LocalDateTime createdAt;
}