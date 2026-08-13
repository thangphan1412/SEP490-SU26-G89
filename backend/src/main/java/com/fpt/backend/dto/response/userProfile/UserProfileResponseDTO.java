package com.fpt.backend.dto.response.userProfile;

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
public class UserProfileResponseDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String numberPhone;
    private String role;
    private String status;

    // --- CÁC TRƯỜNG THÊM MỚI ĐỂ ĐỒNG BỘ VỚI FRONTEND ---
    private String departmentName;
    private String userId; // Đổi từ Employee ID
    private String dateJoined; // Map từ startDate
    private String lastActive;
    private String lastUpdated;

    public static UserProfileResponseDTO fromEntity(Users user) {
        // Logic tạo User ID (Cắt 8 ký tự đầu của UUID)
        String generateUserId = "N/A";
        if (user.getId() != null) {
            generateUserId = "UID-" + user.getId().toString().substring(0, 8).toUpperCase();
        }

        // Logic ẩn Department nếu là CEO / Administrator
        String role = user.getUserRoles().stream()
                .findFirst()
                .map(userRole -> userRole.getRole().getRoleName())
                .orElse("N/A");
        String deptName = "N/A";
        if (role != null && (role.equalsIgnoreCase("CEO") || role.equalsIgnoreCase("Administrator"))) {
            deptName = "N/A"; // Không thuộc phòng ban nào
        } else if (user.getDepartment() != null) {
            deptName = user.getDepartment().getDepartmentName();
        }

        // Lấy ngày update cuối từ BaseEntity (Giả sử BaseEntity có getUpdatedAt())
        // Nếu BaseEntity của bạn chưa có, nó sẽ trả về null, lúc đó FE sẽ hiển thị "Chưa cập nhật"
        String updatedAt = (user.getUpdatedAt() != null) ? user.getUpdatedAt().toString() : "N/A";

        return UserProfileResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .numberPhone(user.getNumberPhone())
                .role(role)
                .status(String.valueOf(user.getStatus()))

                // Map thêm các trường mới
                .departmentName(deptName)
                .userId(generateUserId)
                .dateJoined(user.getStartDate())
                .lastActive(user.getLastActive() != null ? user.getLastActive().toString() : "Never logged in")
                .lastUpdated(updatedAt)
                .build();
    }
}