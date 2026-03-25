package com.sivateja.studycollabration.repository;

import com.sivateja.studycollabration.dto.Resource.ResourceRequestDTO;
import com.sivateja.studycollabration.dto.Resource.ResourceResponseDTO;
import com.sivateja.studycollabration.entities.Resource;
import com.sivateja.studycollabration.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.sivateja.studycollabration.entities.Resource;
import com.sivateja.studycollabration.model.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    List<Resource> findByRoomIdOrderByCreatedAtDesc(Long roomId);
    List<Resource> findByRoomIdAndType(Long roomId, ResourceType type);
    long countByRoomId(Long roomId);
}