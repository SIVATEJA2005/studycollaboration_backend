package com.sivateja.studycollabration.repository;

import com.sivateja.studycollabration.entities.PasswordResetToken;
import com.sivateja.studycollabration.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(Users user);
}