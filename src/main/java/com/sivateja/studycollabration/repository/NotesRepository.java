package com.sivateja.studycollabration.repository;
import com.sivateja.studycollabration.entities.Notes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotesRepository extends JpaRepository<Notes,Long> {

    List<Notes> findByRoomId(Long roomId);

    List<Notes> findByCreatedById(Long userId);

    Page<Notes> findByRoomId(Long roomId, Pageable pageable);

}
