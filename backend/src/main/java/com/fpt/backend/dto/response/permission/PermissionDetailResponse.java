package com.fpt.backend.dto.response.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissionDetailResponse {
    private UUID id;
    private String permissionName;
    private String permissionCode;
    private List<String> allowedActions;
    private List<PermissionActionResponse> actionDetails;
    private String workScope;
    private String permissionDescription;
    private Boolean status;
    private UUID projectId;
    private String projectCode;
    private String projectName;
    private LocalDateTime createdAt;
    private boolean canManage;
}
