package com.sivateja.studycollabration.serviceImpl;

import com.sivateja.studycollabration.entities.Topic;
import com.sivateja.studycollabration.dto.TopicTracker.TopicProgressDTO;
import com.sivateja.studycollabration.dto.TopicTracker.TopicRequestDTO;
import com.sivateja.studycollabration.dto.TopicTracker.TopicResponseDTO;
import com.sivateja.studycollabration.entities.Room;
import com.sivateja.studycollabration.entities.Users;
import com.sivateja.studycollabration.model.TopicStatus;
import com.sivateja.studycollabration.repository.RoomRepository;
import com.sivateja.studycollabration.repository.TopicRepository;
import com.sivateja.studycollabration.services.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements TopicService {

    private final TopicRepository topicRepository;
    private final RoomRepository roomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // ── Add Topic ─────────────────────────────────────────────────────────────
    @Override
    public TopicResponseDTO addTopic(Long roomId, TopicRequestDTO req, Users user) {
        Room room = getRoom(roomId);
        checkMember(room, user);

        Topic topic = Topic.builder()
                .name(req.getName())
                .description(req.getDescription())
                .status(TopicStatus.NOT_STARTED)
                .room(room)
                .createdBy(user)
                .build();

        topic = topicRepository.save(topic);
        TopicResponseDTO response = toResponse(topic);

        // broadcast to all room members
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/topics", response);

        return response;
    }

    // ── Get All Topics ────────────────────────────────────────────────────────
    @Override
    public List<TopicResponseDTO> getTopics(Long roomId) {
        return topicRepository.findByRoomIdOrderByCreatedAtAsc(roomId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Claim Topic ───────────────────────────────────────────────────────────
    @Override
    @Transactional
    public TopicResponseDTO claimTopic(Long topicId, Users user) {
        Topic topic = getTopicWithLock(topicId);
        checkMember(topic.getRoom(), user);

        if (topic.getClaimedBy() != null)
            throw new RuntimeException(
                    topic.getClaimedBy().getUserName() + " already claimed this topic");

        topic.setClaimedBy(user);
        topic.setStatus(TopicStatus.NOT_STARTED);
        topic = topicRepository.save(topic);

        TopicResponseDTO response = toResponse(topic);

        // broadcast claim to all members
        messagingTemplate.convertAndSend(
                "/topic/room/" + topic.getRoom().getId() + "/topics/update",
                response);

        return response;
    }

    private Topic getTopicWithLock(Long topicId) {
        return topicRepository.findByIdWithLock(topicId);
    }
    // ── Unclaim Topic ─────────────────────────────────────────────────────────
    @Override
    @Transactional
    public TopicResponseDTO unclaimTopic(Long topicId, Users user) {
        Topic topic = getTopicWithLock(topicId);

        if (topic.getClaimedBy() == null ||
                !topic.getClaimedBy().getId().equals(user.getId()))
            throw new RuntimeException("You haven't claimed this topic");

        topic.setClaimedBy(null);
        topic.setStatus(TopicStatus.NOT_STARTED);
        topic = topicRepository.save(topic);

        TopicResponseDTO response = toResponse(topic);

        messagingTemplate.convertAndSend(
                "/topic/room/" + topic.getRoom().getId() + "/topics/update",
                response);

        return response;
    }

    // ── Update Status ─────────────────────────────────────────────────────────
    @Override
    @Transactional
    public TopicResponseDTO updateStatus(Long topicId,
                                         TopicStatus status, Users user) {
        Topic topic =getTopicWithLock(topicId);;

        // only the person who claimed it can update status
        if (topic.getClaimedBy() == null ||
                !topic.getClaimedBy().getId().equals(user.getId()))
            throw new RuntimeException("Only the person who claimed this topic can update it");

        topic.setStatus(status);
        topic = topicRepository.save(topic);

        TopicResponseDTO response = toResponse(topic);

        messagingTemplate.convertAndSend(
                "/topic/room/" + topic.getRoom().getId() + "/topics/update",
                response);

        return response;
    }

    // ── Delete Topic ──────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void deleteTopic(Long topicId, Users user) {
        Topic topic = getTopic(topicId);

        // only creator or admin can delete
        if (!topic.getCreatedBy().getId().equals(user.getId()))
            throw new RuntimeException("Only the creator can delete this topic");

        Long roomId = topic.getRoom().getId();
        topicRepository.delete(topic);

        // broadcast deletion
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/topics/delete", topicId);
    }

    // ── Get Progress ──────────────────────────────────────────────────────────
    @Override
    public TopicProgressDTO getProgress(Long roomId) {
        List<Topic> topics = topicRepository.findByRoomIdOrderByCreatedAtAsc(roomId);

        int total      = topics.size();
        int done       = (int) topics.stream().filter(t -> t.getStatus() == TopicStatus.DONE).count();
        int inProgress = (int) topics.stream().filter(t -> t.getStatus() == TopicStatus.IN_PROGRESS).count();
        int notStarted = (int) topics.stream().filter(t -> t.getStatus() == TopicStatus.NOT_STARTED).count();
        int unclaimed  = (int) topics.stream().filter(t -> t.getClaimedBy() == null).count();
        int percent    = total == 0 ? 0 : (done * 100) / total;

        return TopicProgressDTO.builder()
                .total(total)
                .done(done)
                .inProgress(inProgress)
                .notStarted(notStarted)
                .unclaimed(unclaimed)
                .progressPercent(percent)
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private Room getRoom(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
    }

    private Topic getTopic(Long topicId) {
        return topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));
    }
    private void checkMember(Room room, Users user) {
        boolean isMember = room.getMembers().stream()
                .filter(m -> m != null)
                .anyMatch(m -> m.getUser().getId().equals(user.getId())); // ← m.getUser()
        if (!isMember)
            throw new RuntimeException("Only room members can manage topics");
    }
    private TopicResponseDTO toResponse(Topic t) {
        long total = topicRepository.countByRoomId(t.getRoom().getId());
        long done  = topicRepository.countByRoomIdAndStatus(
                t.getRoom().getId(), TopicStatus.DONE);
        int percent = total == 0 ? 0 : (int) ((done * 100) / total);

        return TopicResponseDTO.builder()
                .id(t.getId())
                .name(t.getName())
                .description(t.getDescription())
                .status(t.getStatus().name())
                .roomId(t.getRoom().getId())
                .claimedById(t.getClaimedBy() != null ? t.getClaimedBy().getId() : null)
                .claimedByName(t.getClaimedBy() != null ? t.getClaimedBy().getUserName() : null)
                .createdById(t.getCreatedBy().getId())
                .createdByName(t.getCreatedBy().getUserName())
                .totalTopics((int) total)
                .completedTopics((int) done)
                .progressPercent(percent)
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}