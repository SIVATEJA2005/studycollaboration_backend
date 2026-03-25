package com.sivateja.studycollabration.dto.todo;

import com.sivateja.studycollabration.model.ToDoPriority;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Builder
public class ToDoResponseDTO {
    private Long id;
    private String text;
    private ToDoPriority priority;
    private boolean done;
    private LocalDateTime dueDate;
    private Long roomId;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
}