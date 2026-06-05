package com.sivateja.studycollabration.repository;



import com.sivateja.studycollabration.entities.PomodoroSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PomodoroSessionRepository extends JpaRepository<PomodoroSession, Long> {

    Optional<PomodoroSession> findTopByRoomIdAndStatusInOrderByCreatedAtDesc(
            Long roomId, List<String> statuses);

    int countByRoomIdAndPhaseAndStatusAndStartedAtAfter(
            Long roomId, String phase, String status, LocalDateTime after);
}