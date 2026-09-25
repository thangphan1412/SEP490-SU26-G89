package com.fpt.backend.dto.request.user;

import com.fpt.backend.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequestDTO {

    @NotBlank(message = "Email is required.")
    @Email(message = "Enter a valid email address.")
    private String email;

    @Size(min = 9, message = "Password must contain at least 9 characters.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character.")
    private String password;

    @NotBlank(message = "First name is required.")
    @Pattern(regexp = "^[a-zA-ZÀ-ỹ\\s]+$", message = "First name must contain letters only.")
    private String firstName;

    @NotBlank(message = "Last name is required.")
    @Pattern(regexp = "^[a-zA-ZÀ-ỹ\\s]+$", message = "Last name must contain letters only.")
    private String lastName;

    @NotBlank(message = "Phone number is required.")
    @Pattern(regexp = "^(0[35789])[0-9]{8}$", message = "Enter a valid phone number.")
    private String numberPhone;

    @NotBlank(message = "Role is required.")
    private String role;

    @NotNull(message = "Status is required.")
    private UserStatus status;

    private Boolean sendWelcomeEmail;

    @NotBlank(message = "Department is required.")
    private String departmentName;

    @NotBlank(message = "Date of birth is required.")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date of birth must use YYYY-MM-DD format.")
    private String dob;

    // KHÔNG CÒN @ValidStartDate Ở ĐÂY NỮA -> THẢ HỒ CHỌN QUÁ KHỨ!
    @NotBlank(message = "Start date is required.")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Start date must use YYYY-MM-DD format.")
    private String startDate;

    // --- CÁC TRƯỜNG THÔNG TIN COMPANY KHI TẠO/SỬA USER ---
    private String companyName;
    private String companyEmail;
    private String registeredAddress;

}