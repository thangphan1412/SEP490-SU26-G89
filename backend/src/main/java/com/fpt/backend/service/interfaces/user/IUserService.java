package com.fpt.backend.service.interfaces.user;

import com.fpt.backend.dto.request.user.UserRequestDTO;
import com.fpt.backend.dto.response.user.UserResponseDTO;
import com.fpt.backend.entity.Users;

import java.util.List;

public interface IUserService {
    Boolean existsByEmail(String email);
    void save(Users user);

    List<UserResponseDTO> getAllUsers();
    UserResponseDTO getUserById(Integer id); // Sửa thành Integer
    UserResponseDTO createUser(UserRequestDTO request);
    UserResponseDTO updateUser(Integer id, UserRequestDTO request); // Sửa thành Integer
}