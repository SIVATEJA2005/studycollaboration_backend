package com.sivateja.studycollabration.controllers;


import com.sivateja.studycollabration.Security.CustomUserDetails;
import com.sivateja.studycollabration.dto.Room.InviteCodeDTo;
import com.sivateja.studycollabration.dto.Room.RoomRequestDTO;
import com.sivateja.studycollabration.dto.Room.RoomResponseDTO;
import com.sivateja.studycollabration.entities.Users;
import com.sivateja.studycollabration.services.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomControllers {

    private final RoomService roomService;

    // Dashboard.jsx: POST /room/create  body: {name, description, tag, icon}
    @PostMapping("/create")
    public ResponseEntity<RoomResponseDTO> createRoom(
            @RequestBody RoomRequestDTO req) {
        return ResponseEntity.ok(roomService.createRoom(req));
    }

    // Dashboard.jsx: GET /room/myRooms
    @GetMapping("/myRooms")
    public ResponseEntity<List<RoomResponseDTO>> getMyRooms() {
        return ResponseEntity.ok(roomService.getMyRooms());
    }

    // RoomPage.jsx: GET /room/{id}
    @GetMapping("/{id}")
    public ResponseEntity<RoomResponseDTO> getRoom(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    // Dashboard.jsx JoinByCodeModal: POST /room/join-by-code  body: {code}
    @PostMapping("/join-by-code")
    public ResponseEntity<RoomResponseDTO> joinByCode(
            @RequestBody InviteCodeDTo req) {
        System.out.println("in join by code");
        return ResponseEntity.ok(roomService.joinByCode(req.getCode()));
    }

    // RoomPage.jsx MembersDrawer: GET /room/join/{id}
    @GetMapping("/join/{id}")
    public ResponseEntity<RoomResponseDTO> joinById(
            @PathVariable Long id) {
        return ResponseEntity.ok(roomService.joinById(id));
    }

    // RoomPage.jsx MembersDrawer: GET /room/leave/{id}
    @GetMapping("/leave/{id}")
    public ResponseEntity<Void> leaveRoom(
            @PathVariable Long id) {
        roomService.leaveRoom(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/getAllRooms")
    public ResponseEntity<List<RoomResponseDTO>> getAllRooms()
    {
        return ResponseEntity.ok(roomService.getMyRooms());
    }
}
