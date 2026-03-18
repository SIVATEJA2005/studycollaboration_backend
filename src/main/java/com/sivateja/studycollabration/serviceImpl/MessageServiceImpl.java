package com.sivateja.studycollabration.serviceImpl;

import com.sivateja.studycollabration.dto.message.MessageResponseDTO;
import com.sivateja.studycollabration.dto.message.MessageSendRequestDTO;
import com.sivateja.studycollabration.entities.Messages;
import com.sivateja.studycollabration.entities.Room;
import com.sivateja.studycollabration.entities.Users;
import com.sivateja.studycollabration.repository.MessagesRepository;
import com.sivateja.studycollabration.repository.RoomRepository;
import com.sivateja.studycollabration.services.MessageServices;
//import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageServices {

    private final MessagesRepository messageRepository;
    private final RoomRepository roomRepository;

    // GET /api/messages/room/{roomId}
    public List<MessageResponseDTO> getMessages(Long roomId) {
        return messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Called by WebSocketController when message arrives on /app/chat/{roomId}
    @Transactional
    public MessageResponseDTO saveAndBroadcast(Long roomId, String content, Users sender) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        Messages msg = Messages.builder()
                .room(room)
                .sender(sender)
                .content(content)
                .build();

        return toResponse(messageRepository.save(msg));
    }

    private MessageResponseDTO toResponse(Messages msg) {
        return MessageResponseDTO.builder()
                .id(msg.getId())
                .content(msg.getContent())
                .senderId(msg.getSender().getId())
                .senderName(msg.getSender().getUserName())    // senderName  — ChatTab checks this
                 // senderUsername — fallback check
                .createdAt(msg.getCreatedAt())
                .build();
    }


}
