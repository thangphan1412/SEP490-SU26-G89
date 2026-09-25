//package com.fpt.backend.service.impl.user;
//
//import com.fpt.backend.dto.request.authentication.ChangePasswordRequest;
//import com.fpt.backend.dto.request.authentication.ResetPasswordRequest;
//import com.fpt.backend.entity.Users;
//import com.fpt.backend.mail.EmailService;
//import com.fpt.backend.mail.MessageInfor;
//import com.fpt.backend.repository.user.UserRepository;
//import com.fpt.backend.util.CurrentUser;
//import com.fpt.backend.util.OTPGenerator;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Captor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.verifyNoInteractions;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class PasswordOperationsServiceTest {
//
//    private static final String EMAIL = "employee@example.com";
//    private static final String OTP = "123456";
//    private static final String NEW_PASSWORD = "NewPassword1!";
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private PasswordEncoder passwordEncoder;
//
//    @Mock
//    private OTPGenerator otpGenerator;
//
//    @Mock
//    private RedisOtpService redisOtpService;
//
//    @Mock
//    private EmailService emailService;
//
//    @Mock
//    private CurrentUser currentUser;
//
//    @Captor
//    private ArgumentCaptor<MessageInfor> messageCaptor;
//
//    @InjectMocks
//    private UserServiceImpl userService;
//
//    @Test
//    void forgotPassword_generatesStoresAndEmailsOtpForExistingUser() {
//        Users user = user(EMAIL, "encoded-password");
//        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
//        when(otpGenerator.generateOTP()).thenReturn(OTP);
//
//        userService.forgotPassword(EMAIL);
//
//        verify(redisOtpService).saveOTP(EMAIL, OTP);
//        verify(emailService).sendEmail(messageCaptor.capture());
//        MessageInfor sentMessage = messageCaptor.getValue();
//        assertThat(sentMessage.getEmail()).isEqualTo(EMAIL);
//        assertThat(sentMessage.getTitle()).isEqualTo("OTP Reset Password");
//        assertThat(sentMessage.getText()).contains("OTP:" + OTP).contains("expire in 5 minutes");
//    }
//
//    @Test
//    void forgotPassword_throwsWhenEmailDoesNotBelongToAnyUser() {
//        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> userService.forgotPassword(EMAIL))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessage("User not found with email: " + EMAIL);
//
//        verifyNoInteractions(otpGenerator, redisOtpService, emailService);
//    }
//
//    @Test
//    void resetPassword_encodesPersistsAndClearsOtpWhenRequestIsValid() {
//        Users user = user(EMAIL, "old-encoded-password");
//        ResetPasswordRequest request = new ResetPasswordRequest(EMAIL, OTP, NEW_PASSWORD, NEW_PASSWORD);
//        when(redisOtpService.getOTP(EMAIL)).thenReturn(OTP);
//        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
//        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn("new-encoded-password");
//
//        userService.resetPassword(request);
//
//        assertThat(user.getPassword()).isEqualTo("new-encoded-password");
//        verify(passwordEncoder).encode(NEW_PASSWORD);
//        verify(userRepository).save(user);
//        verify(redisOtpService).deleteOTP(EMAIL);
//    }
//
//    @Test
//    void resetPassword_rejectsMissingOtpBeforeLookingUpUser() {
//        ResetPasswordRequest request = new ResetPasswordRequest(EMAIL, OTP, NEW_PASSWORD, NEW_PASSWORD);
//        when(redisOtpService.getOTP(EMAIL)).thenReturn(null);
//
//        assertThatThrownBy(() -> userService.resetPassword(request))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessage("OTP is empty");
//
//        verify(userRepository, never()).findByEmail(EMAIL);
//        verifyNoInteractions(passwordEncoder);
//        verify(redisOtpService, never()).deleteOTP(EMAIL);
//    }
//
//    @Test
//    void resetPassword_rejectsIncorrectOtpBeforeLookingUpUser() {
//        ResetPasswordRequest request = new ResetPasswordRequest(EMAIL, "654321", NEW_PASSWORD, NEW_PASSWORD);
//        when(redisOtpService.getOTP(EMAIL)).thenReturn(OTP);
//
//        assertThatThrownBy(() -> userService.resetPassword(request))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessage("Invalid OTP");
//
//        verify(userRepository, never()).findByEmail(EMAIL);
//        verifyNoInteractions(passwordEncoder);
//        verify(redisOtpService, never()).deleteOTP(EMAIL);
//    }
//
//    @Test
//    void resetPassword_throwsWhenOtpIsValidButUserDoesNotExist() {
//        ResetPasswordRequest request = new ResetPasswordRequest(EMAIL, OTP, NEW_PASSWORD, NEW_PASSWORD);
//        when(redisOtpService.getOTP(EMAIL)).thenReturn(OTP);
//        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> userService.resetPassword(request))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessage("User not found with email: " + EMAIL);
//
//        verifyNoInteractions(passwordEncoder);
//        verify(redisOtpService, never()).deleteOTP(EMAIL);
//    }
//
//    @Test
//    void resetPassword_rejectsEmptyNewPasswordWithoutPersistingOrClearingOtp() {
//        Users user = user(EMAIL, "old-encoded-password");
//        ResetPasswordRequest request = new ResetPasswordRequest(EMAIL, OTP, "", "");
//        when(redisOtpService.getOTP(EMAIL)).thenReturn(OTP);
//        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
//
//        assertThatThrownBy(() -> userService.resetPassword(request))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessage("New password is empty");
//
//        verifyNoInteractions(passwordEncoder);
//        verify(userRepository, never()).save(user);
//        verify(redisOtpService, never()).deleteOTP(EMAIL);
//    }
//
//    @Test
//    void resetPassword_rejectsMismatchedPasswordConfirmation() {
//        Users user = user(EMAIL, "old-encoded-password");
//        ResetPasswordRequest request = new ResetPasswordRequest(EMAIL, OTP, NEW_PASSWORD, "DifferentPassword1!");
//        when(redisOtpService.getOTP(EMAIL)).thenReturn(OTP);
//        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
//
//        assertThatThrownBy(() -> userService.resetPassword(request))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessage("New passwords do not match");
//
//        verifyNoInteractions(passwordEncoder);
//        verify(userRepository, never()).save(user);
//        verify(redisOtpService, never()).deleteOTP(EMAIL);
//    }
//
//    @Test
//    void changePassword_encodesAndPersistsWhenOldPasswordMatches() {
//        Users current = user(EMAIL, "old-encoded-password");
//        ChangePasswordRequest request = new ChangePasswordRequest("OldPassword1!", NEW_PASSWORD, NEW_PASSWORD);
//        when(currentUser.getCurrentUser()).thenReturn(current);
//        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(current));
//        when(passwordEncoder.matches("OldPassword1!", "old-encoded-password")).thenReturn(true);
//        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn("new-encoded-password");
//
//        userService.changePassword(request);
//
//        assertThat(current.getPassword()).isEqualTo("new-encoded-password");
//        verify(passwordEncoder).matches("OldPassword1!", "old-encoded-password");
//        verify(passwordEncoder).encode(NEW_PASSWORD);
//        verify(userRepository).save(current);
//    }
//
//    @Test
//    void changePassword_rejectsIncorrectOldPasswordWithoutSaving() {
//        Users current = user(EMAIL, "old-encoded-password");
//        ChangePasswordRequest request = new ChangePasswordRequest("WrongPassword1!", NEW_PASSWORD, NEW_PASSWORD);
//        when(currentUser.getCurrentUser()).thenReturn(current);
//        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(current));
//        when(passwordEncoder.matches("WrongPassword1!", "old-encoded-password")).thenReturn(false);
//
//        assertThatThrownBy(() -> userService.changePassword(request))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessage("Old password do not match");
//
//        verify(passwordEncoder, never()).encode(NEW_PASSWORD);
//        verify(userRepository, never()).save(current);
//    }
//
//    @Test
//    void changePassword_throwsWhenCurrentUserCannotBeFoundInRepository() {
//        Users current = user(EMAIL, "old-encoded-password");
//        ChangePasswordRequest request = new ChangePasswordRequest("OldPassword1!", NEW_PASSWORD, NEW_PASSWORD);
//        when(currentUser.getCurrentUser()).thenReturn(current);
//        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> userService.changePassword(request))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessage("User not found with email: " + EMAIL);
//
//        verifyNoInteractions(passwordEncoder);
//    }
//
//    private static Users user(String email, String encodedPassword) {
//        Users user = new Users();
//        user.setEmail(email);
//        user.setPassword(encodedPassword);
//        return user;
//    }
//}
