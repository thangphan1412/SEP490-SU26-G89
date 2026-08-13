package com.fpt.backend.dto.response.user;

import com.fpt.backend.entity.Users;
import com.fpt.backend.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDTO {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String numberPhone;
    private String role;
    private UserStatus status;
    private String departmentName;
    private String dob;
    private String employeeId;
    private String lastActive;
    private String startDate;

    public static UserResponseDTO fromEntity(Users user) {
        // 2. XỬ LÝ LOGIC CẮT UUID THÀNH MÃ NHÂN VIÊN TẠI ĐÂY
        String generateEmpId = "N/A";
        if (user.getId() != null) {
            generateEmpId = "UID-" + user.getId().toString().substring(0, 8).toUpperCase();
        }
        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .numberPhone(user.getNumberPhone())
                .role(user.getUserRoles() != null && !user.getUserRoles().isEmpty() ? user.getUserRoles().get(0).getRole().getRoleName(): "N/A")
                // Truyền thẳng Enum vào đây. Nếu null thì set mặc định là Inactive
                .status(user.getStatus() != null ? user.getStatus() : UserStatus.INACTIVE)
                // Lấy tên department (nếu user có department)
                .departmentName(user.getDepartment() != null ? user.getDepartment().getDepartmentName() : "N/A")
                .dob(user.getDob()) // MAP TRƯỜNG DOB
                .lastActive(user.getLastActive() != null ? user.getLastActive().toString() : null)
                .startDate(user.getStartDate())
                .employeeId(generateEmpId)
                .build();
    }
}
