package com.sivateja.studycollabration.dto.TopicTracker;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopicProgressDTO {
    private int total;
    private int notStarted;
    private int inProgress;
    private int done;
    private int unclaimed;       // nobody took this yet
    private int progressPercent; // done/total * 100
}
