package com.sivateja.studycollabration.controllers;

import com.sivateja.studycollabration.dto.TopicTracker.TopicProgressDTO;
import com.sivateja.studycollabration.dto.TopicTracker.TopicRequestDTO;
import com.sivateja.studycollabration.dto.TopicTracker.TopicResponseDTO;
import com.sivateja.studycollabration.entities.Users;
import com.sivateja.studycollabration.model.TopicStatus;
import com.sivateja.studycollabration.repository.UserRepository;
import com.sivateja.studycollabration.services.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;
    private final UserRepository userRepository;

    private Users getUser(UserDetails u) {
        return userRepository.findByUserName(u.getUsername()).orElseThrow();
    }

    // Add topic to room
    @PostMapping("/room/{roomId}")
    public ResponseEntity<TopicResponseDTO> addTopic(
            @PathVariable Long roomId,
            @RequestBody TopicRequestDTO req,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                topicService.addTopic(roomId, req, getUser(userDetails)));
    }

    // Get all topics in room
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<TopicResponseDTO>> getTopics(
            @PathVariable Long roomId) {
        return ResponseEntity.ok(topicService.getTopics(roomId));
    }

    // Claim a topic
    @PutMapping("/{topicId}/claim")
    public ResponseEntity<TopicResponseDTO> claimTopic(
            @PathVariable Long topicId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                topicService.claimTopic(topicId, getUser(userDetails)));
    }

    // Unclaim a topic
    @PutMapping("/{topicId}/unclaim")
    public ResponseEntity<TopicResponseDTO> unclaimTopic(
            @PathVariable Long topicId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                topicService.unclaimTopic(topicId, getUser(userDetails)));
    }

    // Update status
    @PutMapping("/{topicId}/status")
    public ResponseEntity<TopicResponseDTO> updateStatus(
            @PathVariable Long topicId,
            @RequestParam TopicStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                topicService.updateStatus(topicId, status, getUser(userDetails)));
    }

    // Delete topic
    @DeleteMapping("/{topicId}")
    public ResponseEntity<Void> deleteTopic(
            @PathVariable Long topicId,
            @AuthenticationPrincipal UserDetails userDetails) {
        topicService.deleteTopic(topicId, getUser(userDetails));
        return ResponseEntity.ok().build();
    }

    // Get room progress
    @GetMapping("/room/{roomId}/progress")
    public ResponseEntity<TopicProgressDTO> getProgress(
            @PathVariable Long roomId) {
        return ResponseEntity.ok(topicService.getProgress(roomId));
    }
}