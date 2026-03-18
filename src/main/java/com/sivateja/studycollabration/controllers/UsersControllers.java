package com.sivateja.studycollabration.controllers;
import com.sivateja.studycollabration.dto.Room.RoomResponseDTO;
import com.sivateja.studycollabration.dto.user.UserRequestDTO;
import com.sivateja.studycollabration.dto.user.UserResponseDTO;
import com.sivateja.studycollabration.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersControllers {

    private final UserServices userService;

    // GET /api/users?search=&subject=
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String subject) {
        return ResponseEntity.ok((userService.getAllUsers(search, subject)));
    }

    // GET /api/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok((userService.getUserById(id)));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody UserRequestDTO userRequestDTO)
    {
        System.out.println("in register");
        return ResponseEntity.ok(userService.registerUser(userRequestDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody UserRequestDTO userRequestDTO) {
        // Call service that returns token + user info
        Map<String, Object> response = userService.login(
                userRequestDTO.getEmail(),
                userRequestDTO.getPassword()
        );

        // Return as JSON
        return ResponseEntity.ok(response);
    }


    // PUT /api/users/profile
    @PutMapping("/profile")
    public ResponseEntity<UserResponseDTO> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserRequestDTO request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    // DELETE /api/users/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = Long.parseLong(userDetails.getUsername());
        userService.deleteUser(userId, id);
        return ResponseEntity.ok("Account deleted");
    }

    @GetMapping("/{id}/rooms")
    public ResponseEntity<List<RoomResponseDTO>> getUserRooms(@PathVariable Long id) {
        return ResponseEntity.ok((userService.getUserRooms(id)));
    }
}
