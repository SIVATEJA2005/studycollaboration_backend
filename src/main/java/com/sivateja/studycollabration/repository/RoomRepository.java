package com.sivateja.studycollabration.repository;

import com.sivateja.studycollabration.entities.Room;
import com.sivateja.studycollabration.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room,Long>
{
    Optional<Room> findByNameContainingIgnoreCase(String keyword);
    Optional<Room> findByInviteCode(String inviteCode);

    // FIX: Compare m.user with :user instead of just m
    @Query("SELECT DISTINCT r FROM Room r LEFT JOIN r.members m WHERE r.createdBy = :user OR m.user = :user")
    List<Room> findAllByUser(@Param("user") Users user);

    // Use the id-based approach for the ID query
    @Query("SELECT DISTINCT r FROM Room r LEFT JOIN r.members m WHERE r.createdBy.id = :userId OR m.user.id = :userId")
    List<Room> findRoomsByUserId(@Param("userId") Long userId);

    // Use the username-based approach for the Name query
    @Query("SELECT DISTINCT r FROM Room r LEFT JOIN r.members m WHERE r.createdBy.userName = :userName OR m.user.userName = :userName")
    List<Room> findRoomsByUserName(@Param("userName") String userName);

//    <T> ScopedValue<T> findByRoomIdAndUserName(Long roomId);
}
