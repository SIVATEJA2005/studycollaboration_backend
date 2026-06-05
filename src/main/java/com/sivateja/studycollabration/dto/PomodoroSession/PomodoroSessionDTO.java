package com.sivateja.studycollabration.dto.PomodoroSession;



import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PomodoroSessionDTO {
    private Long id;
    private String phase;
    private int durationSeconds;
    private LocalDateTime startedAt;
    private String status;
    private int pomodoroCount;
    private Long startedById;
    private String startedByName;
    private Long roomId;
}
