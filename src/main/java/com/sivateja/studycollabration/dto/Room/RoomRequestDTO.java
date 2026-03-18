package com.sivateja.studycollabration.dto.Room;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RoomRequestDTO {

    private String name;
    private String description;
    private String icon;
    private String tag;

}
