package com.sivateja.studycollabration.repository;
import com.sivateja.studycollabration.entities.ToDos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ToDosRepository extends JpaRepository<ToDos,Long>
{
        List<ToDos> findByRoomId(Long roomId);

        List<ToDos> findByAssignedToId(Long userId);

        List<ToDos> findByCreatedById(Long userId);

        List<ToDos> findByRoomIdAndStatus(Long roomId, boolean status);


}
