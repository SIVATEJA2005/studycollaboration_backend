package com.sivateja.studycollabration.dto.Room;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RoomResponseDTO {

    private Long id;
    private String name;
    private String description;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private String icon;
    private String tag;
    private int memberSize;
}