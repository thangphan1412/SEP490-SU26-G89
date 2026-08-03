package com.fpt.backend.dto.response.user;

import com.fpt.backend.entity.Users;
import com.fpt.backend.enums.UserStatus;
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
    private UserStatus status;
    private String departmentName;

    public static UserResponseDTO fromEntity(Users user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .numberPhone(user.getNumberPhone())
                .role(user.getRole())
                .status(user.getStatus())
                // Lấy tên department (nếu user có department)
                .departmentName(user.getDepartment() != null ? user.getDepartment().getDepartmentName() : "N/A")
                .build();
    }
}
