package com.sivateja.studycollabration.dto.Notes;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class NoteResponseDTO {

    private Long id;
    private String title;
    private String content;
    private Long roomId;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
}
