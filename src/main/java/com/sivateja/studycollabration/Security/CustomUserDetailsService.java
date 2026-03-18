package com.sivateja.studycollabration.Security;

import com.sivateja.studycollabration.entities.Users;
import com.sivateja.studycollabration.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor // This now only creates a constructor for UserRepository
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // REMOVED: private final CustomUserDetails customUserDetails;
    // You must remove the line above entirely.

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fetch the user from the database
        Users user = userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with username: " + username));

        // Return a NEW instance of CustomUserDetails populated with the user data
        return CustomUserDetails.builder()
                .user(user)
                .build();
    }
}