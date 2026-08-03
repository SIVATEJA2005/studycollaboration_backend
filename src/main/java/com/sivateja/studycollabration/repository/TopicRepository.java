package com.sivateja.studycollabration.repository;
import com.sivateja.studycollabration.entities.Topic;
import com.sivateja.studycollabration.model.TopicStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {

    // get all topics for a room
    List<Topic> findByRoomIdOrderByCreatedAtAsc(Long roomId);

    // get unclaimed topics — for alert system
    List<Topic> findByRoomIdAndClaimedByIsNull(Long roomId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Topic t WHERE t.id = :topicId")
    Topic findByIdWithLock(@Param("topicId") Long topicId);

    // get topics claimed by a specific user in a room
    List<Topic> findByRoomIdAndClaimedById(Long roomId, Long userId);

    // count done topics — for progress bar
    long countByRoomIdAndStatus(Long roomId, TopicStatus status);

    // count total topics in room
    long countByRoomId(Long roomId);
}