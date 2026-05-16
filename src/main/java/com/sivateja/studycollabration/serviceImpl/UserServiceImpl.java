package com.sivateja.studycollabration.serviceImpl;
import com.sivateja.studycollabration.Security.JwtConfig;
import com.sivateja.studycollabration.Security.JwtFilter;
import com.sivateja.studycollabration.dto.Room.RoomResponseDTO;
import com.sivateja.studycollabration.dto.user.UserRequestDTO;
import com.sivateja.studycollabration.dto.user.UserResponseDTO;
import com.sivateja.studycollabration.entities.Room;
import com.sivateja.studycollabration.entities.Users;
import com.sivateja.studycollabration.model.UserRole;
import com.sivateja.studycollabration.repository.RoomRepository;
import com.sivateja.studycollabration.repository.UserRepository;
import com.sivateja.studycollabration.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

//import java.nio.file.AccessDeniedException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserServices {

    private final UserRepository userRepository;
    private final RoomServiceImpl roomService;
    private final RoomRepository roomRepository;
    private final JwtConfig jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final BCryptPasswordEncoder passwordEncoder;
    // Add to constructor injection
    private final EmailServices emailService;

    @Value("${app.backend.url}")
    private String backendUrl;

    @Override
    public UserResponseDTO registerUser(UserRequestDTO user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Generate activation token
        String token = UUID.randomUUID().toString();

        // Build user entity — inactive until email verified
        Users userEntity = toUserEntity(user);
        userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
        userEntity.setActive(false);
        userEntity.setActivationToken(token);

        Users savedUser = userRepository.save(userEntity);

        // Send activation email
        String activationLink = backendUrl + "/api/users/activate?token=" + token;
        emailService.sendActivationEmail(savedUser.getEmail(), savedUser.getDisplayName(), activationLink);

        return toUserDTO(savedUser);
    }

    @Override
    public UserResponseDTO getUserById(Long id) {
        try {
            Users user=userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User with id not found"));

            return toUserDTO(user);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public UserResponseDTO getUserByEmail(String email) {
        try {
            Users user=userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User with id not found"));

            return toUserDTO(user);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<UserResponseDTO> getAllUsers(String search, String subject) {
        List<Users> users=new ArrayList<>();
        return users.stream().map(this::toUserDTO).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> login(String email, String password) {
        try {
            // Fetch user by email
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User with email not found"));

            // Authenticate using Spring Security (username + password)
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUserName(), password)
            );

            // Get UserDetails
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Generate JWT
            String token = jwtUtil.generateToken(userDetails);

            // Build user data map
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("email", user.getEmail());
            userMap.put("userName", user.getUserName());
            userMap.put("displayName", user.getDisplayName());
            userMap.put("role", user.getRole());

            // Build final response map
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", userMap);

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Invalid email or password");
        }
    }
    @Override
    public void deleteUser(Long requesterId, Long targetId) {
        if (!requesterId.equals(targetId))
            throw new AccessDeniedException("You can only delete your own account");
        userRepository.findById(targetId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.deleteById(targetId);
    }

    @Override
    public List<RoomResponseDTO> getUserRooms(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return roomRepository.findRoomsByUserId(userId).stream()
                .map(roomService::toResponse)
                .collect(Collectors.toList());
    }

    public Users toUserEntity(UserRequestDTO userDto)
    {
        return Users.builder()
                .email(userDto.getEmail())
                .userName(userDto.getUserName())
                .displayName(userDto.getDisplayName())
                .password(userDto.getPassword())
                .role(UserRole.USER)
                .build();
    }

    public UserResponseDTO toUserDTO(Users user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUserName())
                .displayName(user.getDisplayName())
                .role(user.getRole())
                .build();
    }

    public UserResponseDTO updateProfile(Long userId, UserRequestDTO req) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (StringUtils.hasText(req.getUserName())) user.setUserName((req.getUserName()));
        if (req.getEmail() != null) user.setEmail(req.getEmail());
        if (req.getDisplayName() != null) user.setDisplayName(req.getDisplayName());
        if (req.getPassword() != null) user.setPassword(req.getPassword());

        return toUserDTO(userRepository.save(user));
    }

    public String activateUser(String token) {
        Users user = userRepository.findByActivationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or already used activation link"));

        if (user.isActive()) {
            return "Account is already activated. Please log in.";
        }

        user.setActive(true);
        user.setActivationToken(null); // clear token after use
        userRepository.save(user);

        return "Your account has been activated! You can now log in.";
    }


}
