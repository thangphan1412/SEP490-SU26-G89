package com.fpt.backend.dto.response.project;

import com.fpt.backend.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectUserResponse {
    private UUID userId;
    private String email;
    private String userName;
    private UserStatus userStatus;
    private LocalDate joinDate;
    private UUID permissionId;
    private String permissionName;
    private String permissionCode;
}
