package com.sivateja.studycollabration.Security;

import com.sivateja.studycollabration.entities.Users;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
// Removed @Component because we build this manually in the Service
public class CustomUserDetails implements UserDetails {

    private final Users user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Changed to "ROLE_" (All caps) to match Spring Security defaults
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUserName();
    }

    // Since you are implementing UserDetails, you must provide these 4 methods.
    // Usually, we return 'true' unless your DB has specific columns for these.
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}