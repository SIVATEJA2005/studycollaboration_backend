package com.sivateja.studycollabration.dto.Room;

import com.sivateja.studycollabration.dto.user.UserResponseDTO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

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
    private int memberCount;
    private String inviteCode;
    private List<UserResponseDTO> members;
}