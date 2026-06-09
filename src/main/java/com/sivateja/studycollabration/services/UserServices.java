package com.sivateja.studycollabration.services;
import com.sivateja.studycollabration.dto.Room.RoomResponseDTO;
import com.sivateja.studycollabration.dto.user.UserRequestDTO;
import com.sivateja.studycollabration.dto.user.UserResponseDTO;

import java.util.List;
import java.util.Map;

public interface UserServices {

    UserResponseDTO registerUser(UserRequestDTO user);

    UserResponseDTO getUserById(Long id);

    UserResponseDTO getUserByEmail(String email);
    
    List<UserResponseDTO> getAllUsers(String search,String subject);


    void deleteUser(Long userId, Long id);

    List<RoomResponseDTO> getUserRooms(Long id);

    UserResponseDTO updateProfile(Long userId, UserRequestDTO request);

    public void forgotPassword(String email);

    public void resetPassword(String token, String newPassword);


    public Map<String, Object> login(String email, String password);


    String activateUser(String token);
}
