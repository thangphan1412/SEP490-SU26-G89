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
import com.fpt.backend.service.impl.signature.UserKeyServiceImpl;
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
    @Autowired
    private UserKeyServiceImpl userKeyService;
    @Override
    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void save(Users user) {
         userRepository.save(user);
    }

//    @Override
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
        Users user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        Users loggedInUser = currentUser.getCurrentUser();
        String currentRole = loggedInUser.getRole();

        // CEO / Admin xem được Kế toán, Trưởng phòng, NV, Đối tác
        if ("CEO".equalsIgnoreCase(currentRole) || "Administrator".equalsIgnoreCase(currentRole)) {
            if (!List.of("Accountant", "HeadOfDepartment", "Employee", "External Parners").contains(user.getRole())) {
                throw new RuntimeException("Access Denied!");
            }
        }
        // Accountant xem được Trưởng phòng, NV, Đối tác
        else if ("Accountant".equalsIgnoreCase(currentRole)) {
            if (!List.of("HeadOfDepartment", "Employee", "External Parners").contains(user.getRole())) {
                throw new RuntimeException("Access Denied!");
            }
        }
        // HeadOfDepartment chỉ xem được NV cùng phòng
        else if ("HeadOfDepartment".equalsIgnoreCase(currentRole)) {
            String myDept = loggedInUser.getDepartment() != null ? loggedInUser.getDepartment().getDepartmentName() : "";
            String targetDept = user.getDepartment() != null ? user.getDepartment().getDepartmentName() : "";
            if (!myDept.equals(targetDept) || !"Employee".equalsIgnoreCase(user.getRole())) {
                throw new RuntimeException("Access Denied: Bạn chỉ được phép xem thông tin nhân viên cùng phòng ban!");
            }
        }
        return UserResponseDTO.fromEntity(user);
    }

    // 3. Create User
    @Override
    public UserResponseDTO createUser(UserCreateRequestDTO request) {
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new RuntimeException("Lỗi: Mật khẩu không được để trống!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        Users loggedInUser = currentUser.getCurrentUser();
        String currentRole = loggedInUser.getRole();

        // 1. Phân quyền CREATE
        if ("CEO".equalsIgnoreCase(currentRole) || "Administrator".equalsIgnoreCase(currentRole)) {
            throw new RuntimeException("Access Denied: Chức vụ của bạn chỉ có quyền xem, không có quyền tạo tài khoản!");
        } else if ("Accountant".equalsIgnoreCase(currentRole)) {
            if (!List.of("HeadOfDepartment", "Employee", "External Parners").contains(request.getRole())) {
                throw new RuntimeException("Access Denied: Bạn chỉ được tạo tài khoản HeadOfDepartment, Employee, External Parners!");
            }
        } else if ("HeadOfDepartment".equalsIgnoreCase(currentRole)) {
            if (!"Employee".equalsIgnoreCase(request.getRole())) {
                throw new RuntimeException("Access Denied: Bạn chỉ được tạo tài khoản Employee!");
            }
            String myDept = loggedInUser.getDepartment() != null ? loggedInUser.getDepartment().getDepartmentName() : "";
            if (!myDept.equals(request.getDepartmentName())) {
                throw new RuntimeException("Access Denied: Bạn chỉ được phép tạo nhân viên trong phòng " + myDept);
            }
        } else {
            throw new RuntimeException("Access Denied!");
        }

        // 2. Tấm khiên kiểm soát số lượng Role duy nhất (Create)
        if (UserStatus.ACTIVE.equals(request.getStatus())) {
            String roleToCheck = request.getRole();
            if (List.of("CEO", "Administrator", "Accountant").contains(roleToCheck)) {
                boolean isExist = userRepository.findByRole(roleToCheck).stream().anyMatch(u -> UserStatus.ACTIVE.equals(u.getStatus()));
                if (isExist) throw new RuntimeException("Lỗi: Hệ thống chỉ cho phép có 1 tài khoản " + roleToCheck + " đang hoạt động!");
            } else if ("HeadOfDepartment".equalsIgnoreCase(roleToCheck)) {
                Departments dept = departmentRepository.findByDepartmentName(request.getDepartmentName()).orElseThrow(() -> new RuntimeException("Phòng ban không tồn tại"));
                boolean isExist = userRepository.findByRoleAndDepartment(roleToCheck, dept).stream().anyMatch(u -> UserStatus.ACTIVE.equals(u.getStatus()));
                if (isExist) throw new RuntimeException("Lỗi: Phòng ban " + request.getDepartmentName() + " đã có Trưởng phòng đang hoạt động!");
            }
        }

        Users newUser = Users.builder()
                .email(request.getEmail()).password(passwordEncoder.encode(request.getPassword())).firstName(request.getFirstName())
                .lastName(request.getLastName()).numberPhone(request.getNumberPhone()).role(request.getRole())
                .dob(request.getDob()).startDate(request.getStartDate()).status(request.getStatus()).build();

        if (request.getDepartmentName() != null && !request.getDepartmentName().isEmpty()) {
            Departments dept = departmentRepository.findByDepartmentName(request.getDepartmentName()).orElseThrow(() -> new RuntimeException("Department not found"));
            newUser.setDepartment(dept);
        }



        Users savedUser = userRepository.save(newUser);
        userKeyService.generateUserKey(savedUser);
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            Role roleEntity = roleRepository.findByRoleName(request.getRole()).orElseThrow(() -> new RuntimeException("Role không tồn tại"));
            UserRole userRole = UserRole.builder().user(savedUser).role(roleEntity).build();
            userRoleRepository.save(userRole);
        }

        if (Boolean.TRUE.equals(request.getSendWelcomeEmail())) {
            MessageInfor messageInfor = new MessageInfor();
            messageInfor.setEmail(savedUser.getEmail());
            messageInfor.setTitle("Welcome to E-CONTRACT System");
            messageInfor.setText("Account created. Email: " + savedUser.getEmail() + " | Password: " + request.getPassword());
            emailService.sendEmail(messageInfor);
        }
        return UserResponseDTO.fromEntity(savedUser);
    }

    // 4. Update User
    @Override
    public UserResponseDTO updateUser(UUID id, UserUpdateRequestDTO request) {
        Users existingUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        Users loggedInUser = currentUser.getCurrentUser();
        String currentRole = loggedInUser.getRole();

        // 1. Phân quyền UPDATE
        if ("CEO".equalsIgnoreCase(currentRole) || "Administrator".equalsIgnoreCase(currentRole)) {
            throw new RuntimeException("Access Denied: Bạn không có quyền chỉnh sửa tài khoản!");
        } else if ("Accountant".equalsIgnoreCase(currentRole)) {
            if (!List.of("HeadOfDepartment", "Employee", "External Parners").contains(existingUser.getRole()) ||
                    !List.of("HeadOfDepartment", "Employee", "External Parners").contains(request.getRole())) {
                throw new RuntimeException("Access Denied: Bạn chỉ được chỉnh sửa quyền của HeadOfDepartment, Employee, External Parners!");
            }
        } else if ("HeadOfDepartment".equalsIgnoreCase(currentRole)) {
            String myDept = loggedInUser.getDepartment() != null ? loggedInUser.getDepartment().getDepartmentName() : "";
            String targetDept = existingUser.getDepartment() != null ? existingUser.getDepartment().getDepartmentName() : "";
            if (!myDept.equals(targetDept) || !"Employee".equalsIgnoreCase(existingUser.getRole())) {
                throw new RuntimeException("Access Denied: Bạn chỉ có quyền sửa nhân viên cùng phòng ban!");
            }
            if (!"Employee".equalsIgnoreCase(request.getRole()) || !myDept.equals(request.getDepartmentName())) {
                throw new RuntimeException("Access Denied: Lỗi vượt quyền hoặc chuyển phòng ban trái phép!");
            }
        }

        // 2. Tấm khiên kiểm soát số lượng Role duy nhất (Update)
        if (UserStatus.ACTIVE.equals(request.getStatus())) {
            String roleToCheck = request.getRole();
            if (List.of("CEO", "Administrator", "Accountant").contains(roleToCheck)) {
                boolean isExist = userRepository.findByRole(roleToCheck).stream()
                        .anyMatch(u -> UserStatus.ACTIVE.equals(u.getStatus()) && !u.getId().equals(existingUser.getId()));
                if (isExist) throw new RuntimeException("Lỗi: Hệ thống chỉ cho phép có 1 tài khoản " + roleToCheck + " đang hoạt động!");
            } else if ("HeadOfDepartment".equalsIgnoreCase(roleToCheck)) {
                Departments dept = departmentRepository.findByDepartmentName(request.getDepartmentName()).orElseThrow(() -> new RuntimeException("Phòng ban không tồn tại"));
                boolean isExist = userRepository.findByRoleAndDepartment(roleToCheck, dept).stream()
                        .anyMatch(u -> UserStatus.ACTIVE.equals(u.getStatus()) && !u.getId().equals(existingUser.getId()));
                if (isExist) throw new RuntimeException("Lỗi: Phòng ban " + request.getDepartmentName() + " đã có Trưởng phòng đang hoạt động!");
            }
        }

        existingUser.setFirstName(request.getFirstName());
        existingUser.setLastName(request.getLastName());
        existingUser.setNumberPhone(request.getNumberPhone());

        if (request.getRole() != null && !request.getRole().equals(existingUser.getRole())) {
            existingUser.setRole(request.getRole());
            Role newRoleEntity = roleRepository.findByRoleName(request.getRole()).orElseThrow(() -> new RuntimeException("Role không tồn tại"));
            Optional<UserRole> existingUserRole = userRoleRepository.findByUser(existingUser);
            if (existingUserRole.isPresent()) {
                UserRole ur = existingUserRole.get();
                ur.setRole(newRoleEntity);
                userRoleRepository.save(ur);
            } else {
                UserRole newUr = UserRole.builder().user(existingUser).role(newRoleEntity).build();
                userRoleRepository.save(newUr);
            }
        }

        existingUser.setStatus(request.getStatus());
        existingUser.setDob(request.getDob());
        existingUser.setStartDate(request.getStartDate());

        if (request.getDepartmentName() != null && !request.getDepartmentName().isEmpty()) {
            Departments dept = departmentRepository.findByDepartmentName(request.getDepartmentName()).orElseThrow(() -> new RuntimeException("Department not found"));
            existingUser.setDepartment(dept);
        }

        if (!existingUser.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) throw new RuntimeException("Email is already in use!");
            existingUser.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        Users updatedUser = userRepository.save(existingUser);
        return UserResponseDTO.fromEntity(updatedUser);
    }


    @Override
    public Page<UserResponseDTO> getAllUsersFiltered(UserFilterRequestDTO filter, String currentUsername, int page, int size) {
        Users currentUser = userRepository.findByEmail(currentUsername).orElseThrow(() -> new RuntimeException("Current user not found"));
        String currentUserRole = currentUser.getRole();
        List<String> allowedRoles;

        // Phân quyền LIST USER
        if ("CEO".equalsIgnoreCase(currentUserRole) || "Administrator".equalsIgnoreCase(currentUserRole)) {
            allowedRoles = List.of("Accountant", "HeadOfDepartment", "Employee", "External Parners");
        } else if ("Accountant".equalsIgnoreCase(currentUserRole)) {
            allowedRoles = List.of("HeadOfDepartment", "Employee", "External Parners");
        } else if ("HeadOfDepartment".equalsIgnoreCase(currentUserRole)) {
            allowedRoles = List.of("Employee");
            if (currentUser.getDepartment() != null) {
                filter.setDepartmentName(currentUser.getDepartment().getDepartmentName()); // Ép cứng phòng ban
            }
        } else {
            throw new RuntimeException("Access Denied!");
        }

        filter.setAllowedRoles(allowedRoles);
        if (filter.getRole() == null || "All".equalsIgnoreCase(filter.getRole())) filter.setRole("");
        if (filter.getDepartmentName() == null || "All".equalsIgnoreCase(filter.getDepartmentName())) filter.setDepartmentName("");
        filter.setKeyword(filter.getKeyword() == null ? "" : filter.getKeyword().trim());
        if (filter.getStatus() == null || "All".equalsIgnoreCase(filter.getStatus()) || filter.getStatus().isEmpty()) {
            filter.setStatusEnum(null);
        } else {
            filter.setStatusEnum(UserStatus.valueOf(filter.getStatus().toUpperCase()));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Users> resultPage = userRepository.searchAndFilterUsers(filter, pageable);
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
