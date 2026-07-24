package com.fpt.backend.dto.response.user;

import com.fpt.backend.entity.Users;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDTO {
    private UUID id; // Sửa UUID thành Integer
    private String email;
    private String firstName;
    private String lastName;
    private String numberPhone;
    private String role;
    private String status;

    public static UserResponseDTO fromEntity(Users user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .numberPhone(user.getNumberPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }
}
