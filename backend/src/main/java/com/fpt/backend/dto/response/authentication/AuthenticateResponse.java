package com.fpt.backend.dto.response.authentication;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticateResponse {
    private String token;
    private String role;
    private String fullName;
    private String departmentName;
   
}
