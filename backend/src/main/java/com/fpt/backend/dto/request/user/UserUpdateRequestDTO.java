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

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @Size(min = 9, message = "Mật khẩu phải có ít nhất 9 ký tự")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$",
            message = "Mật khẩu phải chứa ít nhất 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt")
    private String password;

    @NotBlank(message = "Họ không được để trống")
    @Pattern(regexp = "^[a-zA-ZÀ-ỹ\\s]+$", message = "Họ chỉ được chứa chữ cái")
    private String firstName;

    @NotBlank(message = "Tên không được để trống")
    @Pattern(regexp = "^[a-zA-ZÀ-ỹ\\s]+$", message = "Tên chỉ được chứa chữ cái")
    private String lastName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0[35789])[0-9]{8}$", message = "Số điện thoại không hợp lệ")
    private String numberPhone;

    @NotBlank(message = "Quyền (Role) không được để trống")
    private String role;

    @NotNull(message = "Trạng thái không được để trống")
    private UserStatus status;

    private Boolean sendWelcomeEmail;

    @NotBlank(message = "Phòng ban không được để trống")
    private String departmentName;

    @NotBlank(message = "Ngày sinh không được để trống")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Ngày sinh phải theo định dạng YYYY-MM-DD")
    private String dob;

    // KHÔNG CÒN @ValidStartDate Ở ĐÂY NỮA -> THẢ HỒ CHỌN QUÁ KHỨ!
    @NotBlank(message = "Ngày bắt đầu làm việc không được để trống")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Ngày bắt đầu làm việc phải theo định dạng YYYY-MM-DD")
    private String startDate;
}