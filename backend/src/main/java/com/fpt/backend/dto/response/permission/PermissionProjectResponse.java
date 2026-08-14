package com.fpt.backend.dto.response.permission;

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
public class PermissionProjectResponse {
    private UUID id;
    private String projectCode;
    private String projectName;
    private boolean canManage;
}
