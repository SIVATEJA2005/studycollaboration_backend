package com.sivateja.studycollabration.dto.TopicTracker;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TopicResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String status;          // NOT_STARTED, IN_PROGRESS, DONE
    private Long roomId;
    // who claimed it
    private Long claimedById;
    private String claimedByName;   // null if unclaimed
    // who created it
    private Long createdById;
    private String createdByName;
    // progress stats
    private int totalTopics;        // total in room
    private int completedTopics;    // done count
    private int progressPercent;    // (done/total) * 100
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
