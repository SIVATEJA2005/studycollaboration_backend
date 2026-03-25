package com.sivateja.studycollabration.repository;
import com.sivateja.studycollabration.entities.ToDos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ToDosRepository extends JpaRepository<ToDos, Long> {

        // Get all todos for a room — TasksTab loads these
        List<ToDos> findByRoomId(Long roomId);

        // Get todos by who created them
        List<ToDos> findByCreatedById(Long userId);

        // Get pending or completed todos in a room
        // findByRoomIdAndStatus → findByRoomIdAndDone (matches entity field)
        List<ToDos> findByRoomIdAndDone(Long roomId, boolean done);

        // Get todos by priority in a room — "show all HIGH priority tasks"
        List<ToDos> findByRoomIdAndPriority(Long roomId, String priority);

        // Get overdue todos — dueDate is before now and not done
        List<ToDos> findByRoomIdAndDoneFalseAndDueDateBefore(Long roomId, LocalDateTime now);

        // Count pending todos in a room — useful for badge count
        long countByRoomIdAndDoneFalse(Long roomId);
}