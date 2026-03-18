package com.sivateja.studycollabration.serviceImpl;
import com.sivateja.studycollabration.Security.CustomUserDetails;
import com.sivateja.studycollabration.dto.Room.RoomRequestDTO;
import com.sivateja.studycollabration.dto.Room.RoomResponseDTO;
import com.sivateja.studycollabration.dto.user.UserResponseDTO;
import com.sivateja.studycollabration.entities.Room;
import com.sivateja.studycollabration.entities.RoomMembers;
import com.sivateja.studycollabration.entities.Users;
import com.sivateja.studycollabration.model.RoomRole;
import com.sivateja.studycollabration.repository.RoomRepository;
import com.sivateja.studycollabration.repository.UserRepository;
import com.sivateja.studycollabration.services.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    // POST /room/create
    @Transactional
    public RoomResponseDTO createRoom(RoomRequestDTO req) {
        String inviteCode = UUID.randomUUID().toString()
                .replace("-", "").substring(0, 8).toUpperCase();

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        Users creator = userRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("error in fetching user"));

        Room room = toRoom(req, inviteCode, creator);

        // Create the membership record for the creator as ADMIN
        RoomMembers membership = RoomMembers.builder()
                .room(room)
                .user(creator)
                .role(RoomRole.ADMIN)
                .build();

        room.getMembers().add(membership);
        room = roomRepository.save(room);
        return toResponse(room);
    }

    public Room toRoom(RoomRequestDTO req, String inviteCode, Users user) {
        return Room.builder()
                .name(req.getName())
                .description(req.getDescription())
                .icon(req.getIcon() != null ? req.getIcon() : "book")
                .tag(req.getTag() != null ? req.getTag() : "General")
                .inviteCode(inviteCode)
                .createdBy(user)
                .members(new ArrayList<>()) // Initialize for the new intermediate entity
                .build();
    }

    // GET /room/myRooms
    public List<RoomResponseDTO> getMyRooms() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        return roomRepository.findRoomsByUserName(name)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // GET /room/{id}
    public RoomResponseDTO getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        return toResponse(room);
    }
    // POST /room/join-by-code  { code }
    @Transactional
    public RoomResponseDTO joinByCode(String code) {
        System.out.println(code);
        Users user = userRepository.findByUserName(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new RuntimeException("error user not found"));
        Room room = roomRepository.findByInviteCode(code.trim().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Invalid or expired invite code"));

        // Check membership against the User inside the RoomMembers object
        boolean alreadyMember = room.getMembers().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()));
        if (!alreadyMember) {
            RoomMembers newMember = RoomMembers.builder()
                    .room(room)
                    .user(user)
                    .role(RoomRole.MEMBER)
                    .build();
            room.getMembers().add(newMember);
            roomRepository.save(room);
        }
        return toResponse(room);
    }

    // GET /room/join/{id}  — join by room id directly
    @Transactional
    public RoomResponseDTO joinById(Long roomId) {
        Users user = userRepository.findByUserName(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new RuntimeException("user not found with these user name"));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        boolean alreadyMember = room.getMembers().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()));

        if (!alreadyMember) {
            RoomMembers newMember = RoomMembers.builder()
                    .room(room)
                    .user(user)
                    .role(RoomRole.MEMBER)
                    .build();
            room.getMembers().add(newMember);
            roomRepository.save(room);
        }
        return toResponse(room);
    }

    // GET /room/leave/{id}
    @Transactional
    public void leaveRoom(Long roomId) {
        Users user = userRepository.findByUserName(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new RuntimeException("user not found with this name"));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Remove the RoomMembers record where the associated User ID matches
        room.getMembers().removeIf(m -> m.getUser().getId().equals(user.getId()));
        roomRepository.save(room);
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    public RoomResponseDTO toResponse(Room room) {
        // Map List<RoomMembers> to List<UserResponseDTO> by extracting the User
        List<UserResponseDTO> members = room.getMembers().stream()
                .map(m -> {
                    Users u = m.getUser();
                    return UserResponseDTO.builder()
                            .role(u.getRole()) // Global role (USER/ADMIN)
                            .email(u.getEmail())
                            .createdAt(u.getCreatedAt())
                            .displayName(u.getDisplayName())
                            .username(u.getUserName())
                            .build();
                })
                .toList();

        return RoomResponseDTO.builder()
                .id(room.getId())
                .name(room.getName())
                .description(room.getDescription())
                .icon(room.getIcon())
                .tag(room.getTag())
                .memberSize(members.size())
                .createdByName(room.getCreatedBy().getUserName())
                .createdById(room.getCreatedBy().getId())
                .build();
    }
}