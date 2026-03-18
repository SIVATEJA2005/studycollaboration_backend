package com.sivateja.studycollabration.dto.RoomMember;


import com.sivateja.studycollabration.model.RoomRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomMemberResponseDTO {

    private Long id;
    private Long userId;
    private String username;
    private RoomRole role;
}