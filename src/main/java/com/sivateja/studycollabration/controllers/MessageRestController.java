package com.sivateja.studycollabration.controllers;


import com.sivateja.studycollabration.dto.message.MessageResponseDTO;
import com.sivateja.studycollabration.services.MessageServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageRestController {

    private final MessageServices chatService;

    // ChatTab.jsx: GET /api/messages/room/{roomId}
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<MessageResponseDTO>> getMessages(@PathVariable Long roomId) {
        return ResponseEntity.ok(chatService.getMessages(roomId));
    }
}
