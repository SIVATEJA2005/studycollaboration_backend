package com.sivateja.studycollabration.controllers;

import com.sivateja.studycollabration.Security.JwtConfig;
import com.sivateja.studycollabration.dto.message.MessageResponseDTO;
import com.sivateja.studycollabration.entities.Users;
import com.sivateja.studycollabration.repository.UserRepository;
import com.sivateja.studycollabration.services.MessageServices;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageServices chatService;
    private final JwtConfig jwtUtils;
    private final UserRepository userRepository;

    // ChatTab.jsx publishes to: /app/chat/{roomId}
    // ChatTab.jsx subscribes to: /topic/room{roomId}   ← NOTE: no slash between "room" and roomId
    @MessageMapping("/chat/{roomId}")
    public void handleMessage(
            @DestinationVariable Long roomId,
            @Payload WsMessage payload) {

        // Resolve sender from senderId in payload
        Users sender = userRepository.findById(payload.getSenderId())
                .orElse(null);
        if (sender == null) return;

        MessageResponseDTO response = chatService.saveAndBroadcast(roomId, payload.getContent(), sender);

        // Frontend subscribes to /topic/room{roomId}  (no slash — matches ChatTab exactly)
        messagingTemplate.convertAndSend("/topic/room" + roomId, response);
    }

    @Data
    public static class WsMessage {
        private String content;
        private Long senderId;
    }
}
