package com.sivateja.studycollabration.services;

import com.sivateja.studycollabration.dto.TopicTracker.TopicProgressDTO;
import com.sivateja.studycollabration.dto.TopicTracker.TopicRequestDTO;
import com.sivateja.studycollabration.dto.TopicTracker.TopicResponseDTO;
import com.sivateja.studycollabration.entities.Users;
import com.sivateja.studycollabration.model.TopicStatus;

import java.util.List;

public interface TopicService {

    // Admin or any member adds topic to room
    // POST /api/topics/room/{roomId}
    TopicResponseDTO addTopic(Long roomId, TopicRequestDTO req, Users user);

    // Get all topics in a room
    // GET /api/topics/room/{roomId}
    List<TopicResponseDTO> getTopics(Long roomId);

    // Member claims a topic — "I will study this"
    // PUT /api/topics/{topicId}/claim
    TopicResponseDTO claimTopic(Long topicId, Users user);

    // Member unclaims — "I can't do this anymore"
    // PUT /api/topics/{topicId}/unclaim
    TopicResponseDTO unclaimTopic(Long topicId, Users user);

    // Update status — NOT_STARTED, IN_PROGRESS, DONE
    // PUT /api/topics/{topicId}/status
    TopicResponseDTO updateStatus(Long topicId, TopicStatus status, Users user);

    // Delete topic — only admin or creator
    // DELETE /api/topics/{topicId}
    void deleteTopic(Long topicId, Users user);

    // Get room progress stats
    // GET /api/topics/room/{roomId}/progress
    TopicProgressDTO getProgress(Long roomId);
}
