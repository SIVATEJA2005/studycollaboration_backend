package com.sivateja.studycollabration.services;

import com.sivateja.studycollabration.dto.Room.RoomRequestDTO;
import com.sivateja.studycollabration.dto.Room.RoomResponseDTO;
import com.sivateja.studycollabration.entities.Users;

import java.util.List;

public interface RoomService {

    public RoomResponseDTO createRoom(RoomRequestDTO req);

    public List<RoomResponseDTO> getMyRooms();

    public RoomResponseDTO getRoomById(Long id);

    public RoomResponseDTO joinByCode(String code);

    public RoomResponseDTO joinById(Long roomId);

    public void leaveRoom(Long roomId);
}
