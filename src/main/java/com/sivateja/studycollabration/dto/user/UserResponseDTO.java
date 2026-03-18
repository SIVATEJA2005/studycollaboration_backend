package com.sivateja.studycollabration.dto.user;

import com.sivateja.studycollabration.model.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponseDTO {

    private Long id;
    private String email;
    private String username;
    private String displayName;
    private UserRole role;
    private LocalDateTime createdAt;
}