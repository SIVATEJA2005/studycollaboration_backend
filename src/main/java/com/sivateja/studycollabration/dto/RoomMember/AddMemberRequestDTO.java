package com.sivateja.studycollabration.dto.RoomMember;
import com.sivateja.studycollabration.model.RoomRole;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class AddMemberRequestDTO
{
    private Long userId;
    private RoomRole role;
}
