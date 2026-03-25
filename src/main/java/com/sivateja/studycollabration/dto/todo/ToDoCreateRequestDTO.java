package com.sivateja.studycollabration.dto.todo;
import com.sivateja.studycollabration.model.ToDoPriority;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
public class ToDoCreateRequestDTO {
    private String text;
    private ToDoPriority priority;
    private boolean done;
    private LocalDateTime dueDate;
}