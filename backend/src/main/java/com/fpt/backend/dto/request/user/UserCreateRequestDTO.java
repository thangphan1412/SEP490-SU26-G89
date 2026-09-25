package com.fpt.backend.dto.request.user;

import com.fpt.backend.enums.UserStatus;
import com.fpt.backend.util.startDate.ValidStartDate;
import com.fpt.backend.util.userDate.ValidUserDate;
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
public class UserCreateRequestDTO {

    @NotBlank(message = "Email is required.")
    @Email(message = "Enter a valid email address (for example, name@domain.com).")
    private String email;

    // --- TÁCH RÕ RÀNG 2 LỚP KHIÊN BẢO VỆ MẬT KHẨU ---
    // 1. Khiên kiểm tra độ dài
    @Size(min = 9, message = "Password must contain at least 9 characters.")
    // 2. Khiên kiểm tra độ phức tạp (Đã đổi {9,} thành .+ để không đếm ký tự nữa)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character.")
    private String password;

    @NotBlank(message = "First name is required.")
    @Pattern(regexp = "^[a-zA-ZÀ-ỹ\\s]+$", message = "First name must contain letters only, without numbers or special characters.")
    private String firstName;

    @NotBlank(message = "Last name is required.")
    @Pattern(regexp = "^[a-zA-ZÀ-ỹ\\s]+$", message = "Last name must contain letters only, without numbers or special characters.")
    private String lastName;

    @NotBlank(message = "Phone number is required.")
    @Pattern(regexp = "^(0[35789])[0-9]{8}$", message = "Phone number must contain 10 digits and start with a valid Vietnamese mobile prefix.")
    private String numberPhone;

    @NotBlank(message = "Role is required.")
    private String role;

    @NotNull(message = "Status is required.")
    private UserStatus status;

    private Boolean sendWelcomeEmail;

    @NotBlank(message = "Department is required.")
    private String departmentName;

    @NotBlank(message = "Date of birth is required.")
    @ValidUserDate(message = "Enter a valid date of birth in dd/MM/yyyy format.")
    private String dob;

    @NotBlank(message = "Start date is required.")
    @ValidUserDate(message = "Enter a valid start date in dd/MM/yyyy format.")
    @ValidStartDate
    private String startDate;

    // --- CÁC TRƯỜNG THÔNG TIN COMPANY KHI TẠO/SỬA USER ---
    private String companyName;
    private String companyEmail;
    private String registeredAddress;

}
