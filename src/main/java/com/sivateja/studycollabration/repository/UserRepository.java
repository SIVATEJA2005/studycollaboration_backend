package com.sivateja.studycollabration.repository;

import com.sivateja.studycollabration.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);
    Optional<Users> findByActivationToken(String activationToken);
    Optional<Users> findByUserName(String userName);
    boolean existsByEmail(String email);
    boolean existsByUserName(String userName);

//    List<Users> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String search, String search1);

//    List<Users> findBySubjectsContaining(String subject);
}

