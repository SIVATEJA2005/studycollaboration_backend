package com.sivateja.studycollabration.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRequestDTO
{
    private String email;
    private String userName;
    private String displayName;
    private String password;
}
