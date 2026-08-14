package com.fpt.backend.dto.response.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectPermissionConfigurationResponse {
    private UUID permissionId;
    private String permissionName;
    private String permissionCode;
    private String permissionDescription;
    private Boolean status;
    private List<String> allowedActions;
    private String workScope;
}
