package com.fpt.backend.service.interfaces.user;

import com.fpt.backend.dto.request.authentication.RegisterRequest;
import com.fpt.backend.dto.request.user.UserFilterRequestDTO;
import com.fpt.backend.dto.request.user.UserRequestDTO;
import com.fpt.backend.dto.request.userProfile.UserProfileRequestDTO;
import com.fpt.backend.dto.response.authentication.RegisterResponse;
import com.fpt.backend.dto.response.user.UserResponseDTO;
import com.fpt.backend.dto.response.userProfile.UserProfileResponseDTO;
import com.fpt.backend.entity.Users;

import java.util.List;
import java.util.UUID;

public interface IUserService {

    Boolean existsByEmail(String email);
    void save(Users user);

    RegisterResponse create(RegisterRequest registerRequest);

    List<UserResponseDTO> getAllUsers();
    UserResponseDTO getUserById(UUID id); // Sửa thành Integer
    UserResponseDTO createUser(UserRequestDTO request);
    UserResponseDTO updateUser(UUID id, UserRequestDTO request); // Sửa thành Integer

    List<UserResponseDTO> getAllUsersFiltered(UserFilterRequestDTO filterDTO, String currentUsername);

    // Thêm 2 hàm cho Profile
    UserProfileResponseDTO getMyProfile(UUID userId);
    UserProfileResponseDTO updateMyProfile(UUID userId, UserProfileRequestDTO request);


}
