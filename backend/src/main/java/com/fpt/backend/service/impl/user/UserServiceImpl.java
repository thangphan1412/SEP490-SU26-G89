package com.fpt.backend.service.impl.user;

import com.fpt.backend.dto.request.authentication.RegisterRequest;
import com.fpt.backend.dto.request.authentication.ResetPasswordRequest;
import com.fpt.backend.dto.request.user.UserRequestDTO;
import com.fpt.backend.dto.request.userProfile.UserProfileRequestDTO;
import com.fpt.backend.dto.response.authentication.RegisterResponse;
import com.fpt.backend.dto.response.user.UserResponseDTO;
import com.fpt.backend.dto.response.userProfile.UserProfileResponseDTO;
import com.fpt.backend.entity.Users;
import com.fpt.backend.mail.EmailService;
import com.fpt.backend.mail.MessageInfor;
import com.fpt.backend.repository.user.UserRepository;
import com.fpt.backend.service.interfaces.user.IUserService;
import com.fpt.backend.util.OTPGenerator;
import com.fpt.backend.util.ValidateEmail;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
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
//        if(users.isEmpty() || users.get(0).getEmail() == null || users.get(0).getPassword() == null || users.get(0).getPassword().isEmpty()){
//            throw new RuntimeException("Email or password is empty");
//        }
        if(userRepository.existsByEmail(registerRequest.getEmail())){
            throw new RuntimeException("Email already exists");
        }
        if(!ValidateEmail.validateEmail(registerRequest.getEmail(), regexPattern)){
            throw new RuntimeException("Invalid format email: abc@domain.com");
        }
        if(registerRequest.getPassword().length() < 8){
            throw new RuntimeException("Password too short, have to be at least 8 characters");
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
    public UserResponseDTO getUserById(Integer id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id)); // Cân nhắc dùng custom exception của bạn
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
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .build();

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
    public UserResponseDTO updateUser(Integer id, UserRequestDTO request) {
        Users existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        existingUser.setFirstName(request.getFirstName());
        existingUser.setLastName(request.getLastName());
        existingUser.setNumberPhone(request.getNumberPhone());
        existingUser.setRole(request.getRole());
        existingUser.setStatus(request.getStatus());

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


    @Override
    public UserProfileResponseDTO getMyProfile(Integer userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return UserProfileResponseDTO.fromEntity(user);
    }

    @Override
    public UserProfileResponseDTO updateMyProfile(Integer userId, UserProfileRequestDTO request) {
        Users existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        existingUser.setFirstName(request.getFirstName());
        existingUser.setLastName(request.getLastName());
        existingUser.setNumberPhone(request.getNumberPhone());

        // Kiểm tra logic nếu user đổi email
        if (!existingUser.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email is already in use by another account!");
            }
            existingUser.setEmail(request.getEmail());
        }

        // Lưu ý: Không cho phép tự ý đổi Role hoặc Status ở hàm Update Profile cá nhân

        Users updatedUser = userRepository.save(existingUser);
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
}
