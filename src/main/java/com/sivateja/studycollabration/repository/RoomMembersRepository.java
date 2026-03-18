package com.sivateja.studycollabration.repository;

import com.sivateja.studycollabration.entities.RoomMembers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomMembersRepository extends JpaRepository<RoomMembers,Long>
{

    Optional<RoomMembers> findByRoomIdAndUserId(Long RoomId, Long UserId);

    List<RoomMembers> findByRoomId(Long roomId);

    List<RoomMembers> findByUserId(Long userId);

    boolean existsByRoomIdAndUserId(Long roomId,Long userId);


    void deleteByRoomIdAndUserId(Long roomId,Long userId);



    

}
