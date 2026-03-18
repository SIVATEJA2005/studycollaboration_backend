package com.sivateja.studycollabration.repository;
import com.sivateja.studycollabration.entities.Messages;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface MessagesRepository extends JpaRepository<Messages,Long>
{
    List<Messages> findByRoomIdOrderByCreatedAtAsc(Long roomId);
    Page<Messages> findByRoomId(Long roomId, Pageable pageable);
}
