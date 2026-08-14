package com.fpt.backend.dto.response.project;

import com.fpt.backend.enums.UserStatus;
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
public class ProjectEmployeeResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private UserStatus status;
}
