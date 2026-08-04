package com.fpt.backend.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDTO {
    private String email;
    private String password; // Trong thực tế, lúc update có thể không gửi kèm password hoặc tách riêng
    private String firstName;
    private String lastName;
    private String numberPhone;
    private String role;
    private String status;
    private Boolean sendWelcomeEmail;
    private String departmentName;
    private String dob;
    private String startDate;
}
