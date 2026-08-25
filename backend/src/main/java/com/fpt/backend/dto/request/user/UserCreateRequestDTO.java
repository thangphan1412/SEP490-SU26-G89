package com.fpt.backend.dto.request.user;

import com.fpt.backend.enums.UserStatus;
import com.fpt.backend.util.startDate.ValidStartDate;
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

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng (Ví dụ: ten@domain.com)")
    private String email;

    // --- TÁCH RÕ RÀNG 2 LỚP KHIÊN BẢO VỆ MẬT KHẨU ---
    // 1. Khiên kiểm tra độ dài
    @Size(min = 9, message = "Mật khẩu phải có ít nhất 9 ký tự")
    // 2. Khiên kiểm tra độ phức tạp (Đã đổi {9,} thành .+ để không đếm ký tự nữa)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$",
            message = "Mật khẩu phải chứa ít nhất 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt")
    private String password;

    @NotBlank(message = "Họ không được để trống")
    @Pattern(regexp = "^[a-zA-ZÀ-ỹ\\s]+$", message = "Họ chỉ được chứa chữ cái, không chứa số hoặc ký tự đặc biệt")
    private String firstName;

    @NotBlank(message = "Tên không được để trống")
    @Pattern(regexp = "^[a-zA-ZÀ-ỹ\\s]+$", message = "Tên chỉ được chứa chữ cái, không chứa số hoặc ký tự đặc biệt")
    private String lastName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0[35789])[0-9]{8}$", message = "Số điện thoại không hợp lệ (Phải là 10 số và thuộc đầu số VN)")
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

    @NotBlank(message = "Ngày bắt đầu làm việc không được để trống")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Ngày bắt đầu làm việc phải theo định dạng YYYY-MM-DD")
    @ValidStartDate
    private String startDate;

    // --- CÁC TRƯỜNG THÔNG TIN COMPANY KHI TẠO/SỬA USER ---
    private String companyName;
    private String companyEmail;
    private String registeredAddress;

}