package com.fpt.backend.service.impl.user;

import com.fpt.backend.dto.request.authentication.ChangePasswordRequest;
import com.fpt.backend.dto.request.authentication.RegisterRequest;
import com.fpt.backend.dto.request.authentication.ResetPasswordRequest;
import com.fpt.backend.dto.request.user.UserFilterRequestDTO;
import com.fpt.backend.dto.request.user.UserCreateRequestDTO;
import com.fpt.backend.dto.request.user.UserUpdateRequestDTO;
import com.fpt.backend.dto.request.userProfile.UserProfileRequestDTO;
import com.fpt.backend.dto.response.authentication.RegisterResponse;
import com.fpt.backend.dto.response.user.UserResponseDTO;
import com.fpt.backend.dto.response.userProfile.UserProfileResponseDTO;
import com.fpt.backend.entity.Departments;
import com.fpt.backend.entity.Role;
import com.fpt.backend.entity.UserRole;
import com.fpt.backend.entity.Users;
import com.fpt.backend.enums.UserStatus;
import com.fpt.backend.mail.EmailService;
import com.fpt.backend.mail.MessageInfor;
import com.fpt.backend.repository.department.DepartmentRepository;
import com.fpt.backend.repository.role.RoleRepository;
import com.fpt.backend.repository.user.UserRepository;
import com.fpt.backend.repository.userRole.UserRoleRepository;
import com.fpt.backend.service.interfaces.user.IUserService;
import com.fpt.backend.util.CurrentUser;
import com.fpt.backend.util.OTPGenerator;
import com.fpt.backend.util.ValidateEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;

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

        // --- BẮT ĐẦU: TẤM KHIÊN BẢO VỆ XEM CHI TIẾT ---
        Users loggedInUser = currentUser.getCurrentUser();
        if ("HeadOfDepartment".equalsIgnoreCase(loggedInUser.getRole())) {
            String myDept = loggedInUser.getDepartment() != null ? loggedInUser.getDepartment().getDepartmentName() : "";
            String targetDept = user.getDepartment() != null ? user.getDepartment().getDepartmentName() : "";

            if (!myDept.equals(targetDept)) {
                throw new RuntimeException("Access Denied: Bạn không được phép xem thông tin nhân viên phòng ban khác!");
            }
        }
        // --- KẾT THÚC TẤM KHIÊN ---

        return UserResponseDTO.fromEntity(user);
    }

    // 3. Create User
    @Override
    public UserResponseDTO createUser(UserCreateRequestDTO request) {

        // --- CHẶN PASSWORD RỖNG LÚC TẠO MỚI ---
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new RuntimeException("Lỗi: Mật khẩu không được để trống khi tạo tài khoản mới!");
        }
        // ---------------------------------------------------------

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        // --- BẮT ĐẦU: TẤM KHIÊN BẢO VỆ PHÂN QUYỀN TẠO MỚI ---
        Users loggedInUser = currentUser.getCurrentUser();
        String currentRole = loggedInUser.getRole();

        if ("HeadOfDepartment".equalsIgnoreCase(currentRole)) {
            // 1. Chỉ được tạo nhân viên cấp dưới
            if (!List.of("Employee", "Accountant").contains(request.getRole())) {
                throw new RuntimeException("Access Denied: Bạn không có quyền tạo tài khoản chức vụ " + request.getRole());
            }

            // 2. Chỉ được tạo nhân viên trong cùng phòng ban của mình
            String myDept = loggedInUser.getDepartment() != null ? loggedInUser.getDepartment().getDepartmentName() : "";
            if (!myDept.equals(request.getDepartmentName())) {
                throw new RuntimeException("Access Denied: Bạn chỉ được phép tạo nhân viên trong phòng " + myDept);
            }
        }
        // --- KẾT THÚC TẤM KHIÊN ---

        Users newUser = Users.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .numberPhone(request.getNumberPhone())
                .role(request.getRole())
                .dob(request.getDob())
                .startDate(request.getStartDate())
                .status(request.getStatus())
                .build();

        // LOGIC LƯU DEPARTMENT
        if (request.getDepartmentName() != null && !request.getDepartmentName().isEmpty()) {
            Departments dept = departmentRepository.findByDepartmentName(request.getDepartmentName())
                    .orElseThrow(() -> new RuntimeException("Department not found: " + request.getDepartmentName()));
            newUser.setDepartment(dept);
        }

        Users savedUser = userRepository.save(newUser);

        // --- BỔ SUNG LOGIC LƯU BẢNG USER_ROLE Ở ĐÂY ---
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            // 1. Tìm Role trong Database dựa vào tên Role truyền từ FE
            Role roleEntity = roleRepository.findByRoleName(request.getRole())
                    .orElseThrow(() -> new RuntimeException("Role không tồn tại trong hệ thống: " + request.getRole()));

            // 2. Tạo record UserRole mới để nối User và Role
            UserRole userRole = UserRole.builder()
                    .user(savedUser)
                    .role(roleEntity)
                    .build();

            // 3. Lưu xuống Database
            userRoleRepository.save(userRole);
        }
        // ----------------------------------------------

        // DEBUG:
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
    public UserResponseDTO updateUser(UUID id, UserUpdateRequestDTO request) {
        Users existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // --- BẮT ĐẦU: TẤM KHIÊN BẢO VỆ PHÂN QUYỀN CẬP NHẬT ---
        Users loggedInUser = currentUser.getCurrentUser();
        if ("HeadOfDepartment".equalsIgnoreCase(loggedInUser.getRole())) {

            // 1. Phải là người trong cùng phòng ban mới được sửa
            String myDept = loggedInUser.getDepartment() != null ? loggedInUser.getDepartment().getDepartmentName() : "";
            String targetDept = existingUser.getDepartment() != null ? existingUser.getDepartment().getDepartmentName() : "";
            if (!myDept.equals(targetDept)) {
                throw new RuntimeException("Access Denied: Bạn không có quyền sửa thông tin nhân viên của phòng ban khác!");
            }

            // 2. Không được phép "nâng quyền" nhân viên lên thành CEO hay Admin
            if (!List.of("Employee", "Accountant").contains(request.getRole())) {
                throw new RuntimeException("Access Denied: Bạn không được phép cấp quyền " + request.getRole() + " cho nhân viên!");
            }

            // 3. Không được phép "đá" nhân viên này sang phòng ban khác
            if (!myDept.equals(request.getDepartmentName())) {
                throw new RuntimeException("Access Denied: Bạn không được phép chuyển nhân viên này sang phòng ban khác!");
            }
        }
        // --- KẾT THÚC TẤM KHIÊN ---

        existingUser.setFirstName(request.getFirstName());
        existingUser.setLastName(request.getLastName());
        existingUser.setNumberPhone(request.getNumberPhone());
        // --- XỬ LÝ UPDATE ROLE TRONG BẢNG USER VÀ USER_ROLE ---
        if (request.getRole() != null && !request.getRole().equals(existingUser.getRole())) {
            existingUser.setRole(request.getRole()); // Lưu string vào bảng user

            // Tìm Role mới trong bảng Role
            Role newRoleEntity = roleRepository.findByRoleName(request.getRole())
                    .orElseThrow(() -> new RuntimeException("Role không tồn tại: " + request.getRole()));

            // Cập nhật bảng trung gian (Tìm xem user đã có record chưa, nếu có thì sửa, chưa thì tạo mới)
            Optional<UserRole> existingUserRole = userRoleRepository.findByUser(existingUser);
            if (existingUserRole.isPresent()) {
                UserRole ur = existingUserRole.get();
                ur.setRole(newRoleEntity);
                userRoleRepository.save(ur);
            } else {
                UserRole newUr = UserRole.builder()
                        .user(existingUser)
                        .role(newRoleEntity)
                        .build();
                userRoleRepository.save(newUr);
            }
        }
        // --------------------------------------------------------
        existingUser.setStatus(request.getStatus());
        existingUser.setDob(request.getDob());
        existingUser.setStartDate(request.getStartDate());

        // XỬ LÝ UPDATE PHÒNG BAN MỚI
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

        // CHECK XEM BACKEND NHẬN ĐƯỢC GÌ:
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


    @Override
    public Page<UserResponseDTO> getAllUsersFiltered(UserFilterRequestDTO filter, String currentUsername, int page, int size) {

        Users currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        String currentUserRole = currentUser.getRole();
        if (currentUserRole == null) {
            throw new RuntimeException("Access Denied: User role is missing.");
        }

        List<String> allowedRoles;

        if ("CEO".equalsIgnoreCase(currentUserRole) || "Administrator".equalsIgnoreCase(currentUserRole)) {
            if ("customer".equalsIgnoreCase(filter.getType())) {
                allowedRoles = List.of("External Parners");
            } else {
                allowedRoles = List.of("HeadOfDepartment", "Employee", "Accountant");
            }
        }
        else if ("HeadOfDepartment".equalsIgnoreCase(currentUserRole)) {
            if ("employee".equalsIgnoreCase(filter.getType())) {
                allowedRoles = List.of("Employee", "Accountant");

                if (currentUser.getDepartment() != null && currentUser.getDepartment().getDepartmentName() != null) {
                    filter.setDepartmentName(currentUser.getDepartment().getDepartmentName());
                } else {
                    throw new RuntimeException("Access Denied: Tài khoản HeadOfDepartment chưa được gán Phòng ban!");
                }
            } else {
                throw new RuntimeException("Access Denied: HeadOfDepartment cannot view External Partners.");
            }
        }
        else {
            throw new RuntimeException("Access Denied: You do not have permission.");
        }

        // Gán allowedRoles vào object filter
        filter.setAllowedRoles(allowedRoles);

        // 2. CHUẨN HÓA DỮ LIỆU LỌC
        if (filter.getRole() == null || "All".equalsIgnoreCase(filter.getRole())) filter.setRole("");
        if (filter.getDepartmentName() == null || "All".equalsIgnoreCase(filter.getDepartmentName())) filter.setDepartmentName("");
        filter.setKeyword(filter.getKeyword() == null ? "" : filter.getKeyword().trim());

        // XỬ LÝ RIÊNG CHO ENUM STATUS
        if (filter.getStatus() == null || "All".equalsIgnoreCase(filter.getStatus()) || filter.getStatus().isEmpty()) {
            filter.setStatusEnum(null); // Nếu là All thì truyền null để DB không lọc
        } else {
            // Ép an toàn chuỗi từ FE sang Enum
            filter.setStatusEnum(UserStatus.valueOf(filter.getStatus().toUpperCase()));
        }

        // Tạo đối tượng Pageable (Sắp xếp theo ngày tạo mới nhất hoặc ID)
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        // Gọi Repository trả về Page
        Page<Users> resultPage = userRepository.searchAndFilterUsers(filter, pageable);

        // Map từ Page<Users> sang Page<UserResponseDTO>
        return resultPage.map(UserResponseDTO::fromEntity);
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
