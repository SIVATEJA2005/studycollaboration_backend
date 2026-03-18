package com.sivateja.studycollabration.services;

import com.sivateja.studycollabration.dto.message.MessageResponseDTO;
import com.sivateja.studycollabration.entities.Users;

import java.util.List;

public interface MessageServices {
    public List<MessageResponseDTO> getMessages(Long roomId);
    public MessageResponseDTO saveAndBroadcast(Long roomId, String content, Users sender);
}
