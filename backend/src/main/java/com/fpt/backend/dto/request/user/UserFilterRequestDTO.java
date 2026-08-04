package com.fpt.backend.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserFilterRequestDTO {
    private String type; // employee hay customer
    private String keyword;
    private String role;
    private String departmentName;
    private String status;
    private List<String> allowedRoles; // Được Service gán vào sau khi check phân quyền
}