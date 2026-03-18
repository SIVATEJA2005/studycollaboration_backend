package com.sivateja.studycollabration.dto.todo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ToDoCreateRequestDTO {

    private String title;
    private String description;
    private Long assignedToId;
    private LocalDateTime dueDate;
}