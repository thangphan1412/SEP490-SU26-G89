package com.fpt.backend.service.impl.user;

import com.fpt.backend.dto.request.authentication.ChangePasswordRequest;
import com.fpt.backend.dto.request.authentication.RegisterRequest;
import com.fpt.backend.dto.request.authentication.ResetPasswordRequest;
import com.fpt.backend.dto.request.user.UserRequestDTO;
import com.fpt.backend.dto.request.userProfile.UserProfileRequestDTO;
import com.fpt.backend.dto.response.authentication.RegisterResponse;
import com.fpt.backend.dto.response.user.UserResponseDTO;
import com.fpt.backend.dto.response.userProfile.UserProfileResponseDTO;
import com.fpt.backend.entity.Departments;
import com.fpt.backend.entity.Users;
import com.fpt.backend.enums.UserStatus;
import com.fpt.backend.mail.EmailService;
import com.fpt.backend.mail.MessageInfor;
import com.fpt.backend.repository.department.DepartmentRepository;
import com.fpt.backend.repository.user.UserRepository;
import com.fpt.backend.service.interfaces.user.IUserService;
import com.fpt.backend.util.CurrentUser;
import com.fpt.backend.util.OTPGenerator;
import com.fpt.backend.util.ValidateEmail;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    @Autowired
    private OTPGenerator otpGenerator;
    @Autowired
    private RedisOtpService redisOtpService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private CurrentUser currentUser;

    @Override
    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void save(Users user) {
         userRepository.save(user);
    }

    @Override
    public RegisterResponse create(RegisterRequest registerRequest) {
        ValidateEmail validateEmail = new ValidateEmail();
        String regexPattern = "^(.+)@(\\S+)$";
        List<Users> users = userRepository.findAll();

        if(userRepository.existsByEmail(registerRequest.getEmail())){
            throw new RuntimeException("Email already exists");
        }
        if(!ValidateEmail.validateEmail(registerRequest.getEmail(), regexPattern)){
            throw new RuntimeException("Invalid format email: abc@domain.com");
        }
        if(registerRequest.getPassword().length() < 8 || registerRequest.getPassword().length() > 12){
            throw new RuntimeException("Password too short, have to be at least 8 characters and less than 12 characters");
        }
        if(registerRequest.getPassword().isEmpty()){
            throw new RuntimeException("Password cannot be empty");
        }
        Users user = new Users();
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        userRepository.save(user);
        RegisterResponse registerResponse = new RegisterResponse();
        registerResponse.setId(user.getId());
        return registerResponse;
    }

    // 1. List User
    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 2. View User
    @Override
    public UserResponseDTO getUserById(UUID id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return UserResponseDTO.fromEntity(user);
    }

    // 3. Create User
    @Override
    public UserResponseDTO createUser(UserRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        Users newUser = Users.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .numberPhone(request.getNumberPhone())
                .role(request.getRole())
                .status(UserStatus.valueOf(request.getStatus()))
                .build();

        // THÊM ĐOẠN NÀY LÀM LOGIC LƯU DEPARTMENT
        if (request.getDepartmentName() != null && !request.getDepartmentName().isEmpty()) {
            Departments dept = departmentRepository.findByDepartmentName(request.getDepartmentName())
                    .orElseThrow(() -> new RuntimeException("Department not found: " + request.getDepartmentName()));
            newUser.setDepartment(dept);
        }

        Users savedUser = userRepository.save(newUser);

        // THÊM DÒNG NÀY ĐỂ DEBUG:
        System.out.println("====== Gửi mail không? " + request.getSendWelcomeEmail());

        // GỬI MAIL CHÀO MỪNG (Khi tạo mới)
        if (Boolean.TRUE.equals(request.getSendWelcomeEmail())) {
            MessageInfor messageInfor = new MessageInfor();
            messageInfor.setEmail(savedUser.getEmail());
            messageInfor.setTitle("Welcome to E-CONTRACT System");
            messageInfor.setText("Hello " + savedUser.getFirstName() + ",\n\n" +
                    "Your account has been successfully created.\n" +
                    "Email: " + savedUser.getEmail() + "\n" +
                    "Password: " + request.getPassword() + "\n\n" +
                    "Please log in and change your password as soon as possible.");
            emailService.sendEmail(messageInfor);
        }

        return UserResponseDTO.fromEntity(savedUser);
    }

    // 4. Update User
    @Override
    public UserResponseDTO updateUser(UUID id, UserRequestDTO request) {
        Users existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        existingUser.setFirstName(request.getFirstName());
        existingUser.setLastName(request.getLastName());
        existingUser.setNumberPhone(request.getNumberPhone());
        existingUser.setRole(request.getRole());
        existingUser.setStatus(UserStatus.valueOf(request.getStatus()));

        // THÊM ĐOẠN NÀY ĐỂ XỬ LÝ UPDATE PHÒNG BAN MỚI
        if (request.getDepartmentName() != null && !request.getDepartmentName().isEmpty()) {
            Departments dept = departmentRepository.findByDepartmentName(request.getDepartmentName())
                    .orElseThrow(() -> new RuntimeException("Department not found: " + request.getDepartmentName()));
            existingUser.setDepartment(dept);
        }

        boolean isEmailChanged = false;
        // Kiểm tra logic nếu user đổi email
        if (!existingUser.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email is already in use by another account!");
            }
            existingUser.setEmail(request.getEmail());
            isEmailChanged = true;
        }

        boolean isPasswordChanged = false;
        // Cập nhật và mã hóa password nếu có truyền lên
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
            isPasswordChanged = true;
        }

        Users updatedUser = userRepository.save(existingUser);

        // THÊM DÒNG NÀY ĐỂ CHECK XEM BACKEND NHẬN ĐƯỢC GÌ:
        System.out.println("====== UPDATE USER - Có gửi mail không? " + request.getSendWelcomeEmail());

        // GỬI MAIL THÔNG BÁO UPDATE
        if (Boolean.TRUE.equals(request.getSendWelcomeEmail())) {
            MessageInfor messageInfor = new MessageInfor();
            messageInfor.setEmail(updatedUser.getEmail()); // Sẽ gửi vào mail mới nhất
            messageInfor.setTitle("Your E-CONTRACT Account Has Been Updated");

            StringBuilder emailBody = new StringBuilder("Hello " + updatedUser.getFirstName() + ",\n\nYour account information has been updated by the Administrator.\n");
            if (isEmailChanged) {
                emailBody.append("- Your registered email has been changed to: ").append(updatedUser.getEmail()).append("\n");
            }
            if (isPasswordChanged) {
                // Lấy mật khẩu gốc từ request.getPassword() thay vì updatedUser.getPassword()
                emailBody.append("- Your password has been reset. Your new password is: ")
                        .append(request.getPassword())
                        .append("\n");
                emailBody.append("  (Please log in and change this password immediately for security reasons).\n");
            }
            emailBody.append("\nIf you did not request this change, please contact support immediately.");

            messageInfor.setText(emailBody.toString());
            emailService.sendEmail(messageInfor);
        }

        return UserResponseDTO.fromEntity(updatedUser);
    }


    // Thêm hàm này vào UserServiceImpl.java
    @Override
    public List<UserResponseDTO> getAllUsersFiltered(String type, String currentUsername, String keyword, String roleFilter, String departmentFilter, String statusFilter) {

        Users currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        String currentUserRole = currentUser.getRole();
        if (currentUserRole == null) {
            throw new RuntimeException("Access Denied: User role is missing.");
        }

        List<String> allowedRoles;

        // 1. BẢO MẬT: Dùng equalsIgnoreCase để không sợ lỗi viết hoa/thường ở Database
        if ("CEO".equalsIgnoreCase(currentUserRole) || "Admin".equalsIgnoreCase(currentUserRole)) {
            if ("customer".equalsIgnoreCase(type)) {
                allowedRoles = List.of("Customer");
            } else {
                // Sửa lại thành kiểm tra không phân biệt hoa thường (phòng khi DB viết hoa)
                allowedRoles = List.of("Manager", "Employee", "MANAGER", "EMPLOYEE", "manager", "employee");
            }
        }
        else if ("Manager".equalsIgnoreCase(currentUserRole)) {
            if ("employee".equalsIgnoreCase(type)) {
                allowedRoles = List.of("Employee", "EMPLOYEE", "employee");

                // BẢO MẬT & CHỐNG LỖI 500: Kiểm tra Manager đã có phòng ban chưa
                if (currentUser.getDepartment() != null && currentUser.getDepartment().getDepartmentName() != null) {
                    departmentFilter = currentUser.getDepartment().getDepartmentName();
                } else {
                    // Nếu Manager bị lỗi chưa có phòng ban trong DB -> Chặn luôn để không văng lỗi hệ thống
                    throw new RuntimeException("Access Denied: Tài khoản Manager này chưa được gán Phòng ban trong hệ thống!");
                }
            } else {
                throw new RuntimeException("Access Denied: Managers cannot view customers.");
            }
        }
        else {
            throw new RuntimeException("Access Denied: You do not have permission.");
        }

        // 2. CHUẨN HÓA DỮ LIỆU TỪ FE (Đổi null hoặc "All" thành "" để tránh lỗi SQL)
        if (roleFilter == null || "All".equalsIgnoreCase(roleFilter)) roleFilter = "";
        if (departmentFilter == null || "All".equalsIgnoreCase(departmentFilter)) departmentFilter = "";
        if (statusFilter == null || "All".equalsIgnoreCase(statusFilter)) statusFilter = "";
        if (keyword == null) keyword = ""; else keyword = keyword.trim();

        // 3. GỌI DATABASE THỰC THI QUERY
        List<Users> resultList = userRepository.searchAndFilterUsers(
                keyword,
                roleFilter,
                statusFilter,
                departmentFilter,
                allowedRoles
        );

        // 4. TRẢ VỀ DTO
        return resultList.stream()
                .map(UserResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }


    @Override
    public UserProfileResponseDTO getMyProfile(UUID userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return UserProfileResponseDTO.fromEntity(user);
    }

    @Override
    public UserProfileResponseDTO updateMyProfile(UUID userId, UserProfileRequestDTO request) {
        Users existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        existingUser.setFirstName(request.getFirstName());
        existingUser.setLastName(request.getLastName());
        existingUser.setNumberPhone(request.getNumberPhone());

        boolean isEmailChanged = false; // Biến cờ theo dõi xem user có đổi email không

        // Kiểm tra logic nếu user đổi email
        if (!existingUser.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email is already in use by another account!");
            }
            existingUser.setEmail(request.getEmail());
            isEmailChanged = true; // Ghi nhận là email đã bị thay đổi
        }

        // Lưu ý: Không cho phép tự ý đổi Role hoặc Status ở hàm Update Profile cá nhân

        Users updatedUser = userRepository.save(existingUser);

        // --- BẮT ĐẦU ĐOẠN CODE GỬI EMAIL TỰ ĐỘNG ---
        try {
            MessageInfor messageInfor = new MessageInfor();
            messageInfor.setEmail(updatedUser.getEmail()); // Sẽ gửi vào email mới nhất
            messageInfor.setTitle("Security Alert: Your Profile Has Been Updated");

            StringBuilder emailBody = new StringBuilder("Hello " + updatedUser.getFirstName() + ",\n\n");
            emailBody.append("We are writing to let you know that your personal profile information has been successfully updated in the E-CONTRACT system.\n");

            if (isEmailChanged) {
                emailBody.append("\n- Your registered email address has been changed to this email.\n");
            }

            emailBody.append("\nIf you did not make these changes, please contact the system administrator immediately to secure your account.");

            messageInfor.setText(emailBody.toString());
            emailService.sendEmail(messageInfor);
        } catch (Exception e) {
            // Đặt trong khối try-catch để lỡ cấu hình mail lỗi, hệ thống vẫn lưu profile thành công và không văng lỗi 500 ra FE
            System.err.println("Lỗi khi gửi email thông báo update profile: " + e.getMessage());
        }
        // --- KẾT THÚC ĐOẠN CODE GỬI EMAIL ---

        return UserProfileResponseDTO.fromEntity(updatedUser);
    }


    public void forgotPassword(String email) {

        Users users = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        String otp = otpGenerator.generateOTP();
        redisOtpService.saveOTP(email, otp);
        MessageInfor messageInfor = new MessageInfor();
        messageInfor.setEmail(email);
        messageInfor.setTitle("OTP Reset Password");
        messageInfor.setText("OTP:"+otp+"\n\n this code will expire in 5 minutes.");
        emailService.sendEmail(messageInfor);
    }

    public void  resetPassword(ResetPasswordRequest resetPasswordRequest) {


        String otp = redisOtpService.getOTP(resetPasswordRequest.getEmail());
        if(otp == null || otp.isEmpty()){
            throw new RuntimeException("OTP is empty");
        }
        if(!otp.equals(resetPasswordRequest.getOtp())){
            throw new RuntimeException("Invalid OTP");
        }
        Users users = userRepository.findByEmail(resetPasswordRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + resetPasswordRequest.getEmail()));
        if(resetPasswordRequest.getEmail() == null || resetPasswordRequest.getEmail().isEmpty()){
            throw new RuntimeException("Email is empty");
        }  if(resetPasswordRequest.getNewPassword() == null || resetPasswordRequest.getNewPassword().isEmpty()){
            throw new RuntimeException("New password is empty");
        }
        if(resetPasswordRequest.getNewPasswordConfirm() == null || resetPasswordRequest.getNewPasswordConfirm().isEmpty()){
            throw new RuntimeException("New password confirm is empty");
        }
        if(!resetPasswordRequest.getNewPassword().equals(resetPasswordRequest.getNewPasswordConfirm())){
            throw new RuntimeException("New passwords do not match");
        }
        users.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
        userRepository.save(users);
        redisOtpService.deleteOTP(resetPasswordRequest.getEmail());
    }

    public void changePassword(ChangePasswordRequest changePasswordRequest) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String email = authentication.getName();


        String email =currentUser.getCurrentUser().getEmail();
        System.out.println(email);
        Users users = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        if(!passwordEncoder.matches(changePasswordRequest.getOldPassword(), users.getPassword())){
            throw new RuntimeException("Old password do not match");
        }

        users.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(users);

    }
}
