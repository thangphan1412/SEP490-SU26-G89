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
public class PermissionActionResponse {
    private UUID id;
    private String actionCode;
    private String actionName;
    private String resourceCode;
    private String description;
    private Integer displayOrder;
}
