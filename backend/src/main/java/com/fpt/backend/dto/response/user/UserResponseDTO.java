package com.fpt.backend.dto.response.user;

import com.fpt.backend.entity.Company;
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

    // --- BỔ SUNG TRƯỜNG ĐỂ TRẢ VỀ VIEW USER ---
    private String companyName;
    private String companyEmail;
    private String registeredAddress;

    public static UserResponseDTO fromEntity(Users user) {
        String generateEmpId = "N/A";
        if (user.getId() != null) {
            generateEmpId = "UID-" + user.getId().toString().substring(0, 8).toUpperCase();
        }

        Company c = user.getCompany(); // Lấy công ty của User

        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .numberPhone(user.getNumberPhone())
                .role(user.getUserRoles() != null && !user.getUserRoles().isEmpty() ? user.getUserRoles().get(0).getRole().getRoleName(): "N/A")
                .status(user.getStatus() != null ? user.getStatus() : UserStatus.INACTIVE)
                .departmentName(user.getDepartment() != null ? user.getDepartment().getDepartmentName() : "N/A")
                .dob(user.getDob())
                .lastActive(user.getLastActive() != null ? user.getLastActive().toString() : null)
                .startDate(user.getStartDate())
                .employeeId(generateEmpId)
                // Map thông tin Company
                .companyName(c != null ? c.getCompanyName() : "N/A")
                .companyEmail(c != null ? c.getEmail() : "N/A")
                .registeredAddress(c != null ? c.getRegisteredAddress() : "N/A")
                .build();
    }
}
