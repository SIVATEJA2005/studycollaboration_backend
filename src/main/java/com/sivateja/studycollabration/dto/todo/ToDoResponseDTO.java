package com.sivateja.studycollabration.dto.todo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ToDoResponseDTO {

    private Long id;
    private String title;
    private String description;
    private boolean status;
    private Long roomId;
    private Long assignedToId;
    private String assignedToName;
    private Long createdById;
    private String createdByName;
    private LocalDateTime dueDate;
}
