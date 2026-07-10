package com.fpt.backend.service.impl.user;

import com.fpt.backend.dto.request.user.UserRequestDTO;
import com.fpt.backend.dto.response.user.UserResponseDTO;
import com.fpt.backend.entity.Users;
import com.fpt.backend.repository.user.UserRepository;
import com.fpt.backend.service.interfaces.user.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void save(Users user) {
        userRepository.save(user);
    }

    // 1. List User
    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 2. View User
    @Override
    public UserResponseDTO getUserById(Integer id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id)); // Cân nhắc dùng custom exception của bạn
        return UserResponseDTO.fromEntity(user);
    }

    // 3. Create User
    @Override
    public UserResponseDTO createUser(UserRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        Users newUser = Users.builder()
                .email(request.getEmail())
                // Lưu ý: Password nên được mã hóa (VD: passwordEncoder.encode(request.getPassword())) trong thực tế
                .password(request.getPassword())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .numberPhone(request.getNumberPhone())
                .role(request.getRole())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .build();

        Users savedUser = userRepository.save(newUser);
        return UserResponseDTO.fromEntity(savedUser);
    }

    // 4. Update User
    @Override
    public UserResponseDTO updateUser(Integer id, UserRequestDTO request) {
        Users existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        existingUser.setFirstName(request.getFirstName());
        existingUser.setLastName(request.getLastName());
        existingUser.setNumberPhone(request.getNumberPhone());
        existingUser.setRole(request.getRole());
        existingUser.setStatus(request.getStatus());

        // Cân nhắc logic nếu cho phép đổi email, phải check trùng email
        if (!existingUser.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email is already in use by another account!");
            }
            existingUser.setEmail(request.getEmail());
        }

        // Cập nhật password nếu có truyền lên
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            existingUser.setPassword(request.getPassword()); // Nhớ mã hóa nếu có dùng Spring Security
        }

        Users updatedUser = userRepository.save(existingUser);
        return UserResponseDTO.fromEntity(updatedUser);
    }
}