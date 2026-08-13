package com.fpt.backend.dto.response.role;

import com.fpt.backend.entity.Role;
import com.fpt.backend.enums.SystemRoleCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleResponseDTO {
    private UUID id;
    private String roleCode;
    private String roleName;
    private String roleDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean systemRole;
    private SystemRoleCode systemRoleCode;
    private long assignedUserCount;

    public static RoleResponseDTO fromEntity(Role role) {
        return RoleResponseDTO.builder()
                .id(role.getId())
                .roleCode(role.getRoleCode())
                .roleName(role.getRoleName())
                .roleDescription(role.getRoleDescription())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .systemRole(false)
                .build();
    }
}
