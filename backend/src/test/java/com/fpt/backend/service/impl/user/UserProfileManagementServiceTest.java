//package com.fpt.backend.service.impl.user;
//
//import com.fpt.backend.dto.request.userProfile.UserProfileRequestDTO;
//import com.fpt.backend.dto.response.userProfile.UserProfileResponseDTO;
//import com.fpt.backend.entity.Departments;
//import com.fpt.backend.entity.Users;
//import com.fpt.backend.enums.UserStatus;
//import com.fpt.backend.mail.EmailService;
//import com.fpt.backend.mail.MessageInfor;
//import com.fpt.backend.repository.user.UserRepository;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.doThrow;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.verifyNoInteractions;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class UserProfileManagementServiceTest {
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private EmailService emailService;
//
//    @InjectMocks
//    private UserServiceImpl userService;
//
//    @Test
//    void getMyProfile_mapsEmployeeProfileAndSupportingFields() {
//        UUID userId = UUID.randomUUID();
//        Departments engineering = department("Engineering");
//        Users user = user(
//                userId, "employee@example.com", "Employee", engineering, UserStatus.ACTIVE
//        );
//        user.setLastActive(LocalDateTime.of(2026, 8, 1, 9, 30));
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//
//        UserProfileResponseDTO result = userService.getMyProfile(userId);
//
//        assertThat(result.getId()).isEqualTo(userId);
//        assertThat(result.getEmail()).isEqualTo("employee@example.com");
//        assertThat(result.getRole()).isEqualTo("Employee");
//        assertThat(result.getStatus()).isEqualTo("ACTIVE");
//        assertThat(result.getDepartmentName()).isEqualTo("Engineering");
//        assertThat(result.getDateJoined()).isEqualTo("2024-01-02");
//        assertThat(result.getUserId()).isEqualTo("UID-" + userId.toString().substring(0, 8).toUpperCase());
//        assertThat(result.getLastActive()).contains("2026-08-01T09:30");
//    }
//
//    @Test
//    void getMyProfile_hidesDepartmentForCeo() {
//        UUID userId = UUID.randomUUID();
//        Users ceo = user(
//                userId,
//                "ceo@example.com",
//                "CEO",
//                department("Executive"),
//                UserStatus.ACTIVE
//        );
//        when(userRepository.findById(userId)).thenReturn(Optional.of(ceo));
//
//        UserProfileResponseDTO result = userService.getMyProfile(userId);
//
//        assertThat(result.getDepartmentName()).isEqualTo("N/A");
//    }
//
//    @Test
//    void getMyProfile_throwsWhenUserDoesNotExist() {
//        UUID missingId = UUID.randomUUID();
//        when(userRepository.findById(missingId)).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> userService.getMyProfile(missingId))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("User not found");
//    }
//
//    @Test
//    void updateMyProfile_updatesOnlyEditableFieldsAndKeepsRoleStatusAndDepartment() {
//        UUID userId = UUID.randomUUID();
//        Departments engineering = department("Engineering");
//        Users user = user(
//                userId, "employee@example.com", "Employee", engineering, UserStatus.ACTIVE
//        );
//        UserProfileRequestDTO request = userProfileRequest("employee@example.com");
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//        when(userRepository.save(user)).thenReturn(user);
//
//        UserProfileResponseDTO result = userService.updateMyProfile(userId, request);
//
//        assertThat(user.getFirstName()).isEqualTo("Updated");
//        assertThat(user.getLastName()).isEqualTo("Profile");
//        assertThat(user.getNumberPhone()).isEqualTo("0987654321");
//        assertThat(user.getRole()).isEqualTo("Employee");
//        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
//        assertThat(user.getDepartment()).isSameAs(engineering);
//        assertThat(result.getEmail()).isEqualTo("employee@example.com");
//        verify(userRepository).save(user);
//    }
//
//    @Test
//    void updateMyProfile_doesNotCheckDuplicateEmailWhenEmailIsUnchanged() {
//        UUID userId = UUID.randomUUID();
//        Users user = user(
//                userId, "employee@example.com", "Employee", null, UserStatus.ACTIVE
//        );
//        UserProfileRequestDTO request = userProfileRequest("employee@example.com");
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//        when(userRepository.save(user)).thenReturn(user);
//
//        userService.updateMyProfile(userId, request);
//
//        verify(userRepository, never()).existsByEmail(any());
//    }
//
//    @Test
//    void updateMyProfile_changesAvailableEmailAndIncludesItInNotification() {
//        UUID userId = UUID.randomUUID();
//        Users user = user(
//                userId, "old@example.com", "Employee", null, UserStatus.ACTIVE
//        );
//        UserProfileRequestDTO request = userProfileRequest("new@example.com");
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
//        when(userRepository.save(user)).thenReturn(user);
//
//        UserProfileResponseDTO result = userService.updateMyProfile(userId, request);
//
//        assertThat(result.getEmail()).isEqualTo("new@example.com");
//        ArgumentCaptor<MessageInfor> messageCaptor = ArgumentCaptor.forClass(MessageInfor.class);
//        verify(emailService).sendEmail(messageCaptor.capture());
//        assertThat(messageCaptor.getValue().getEmail()).isEqualTo("new@example.com");
//        assertThat(messageCaptor.getValue().getTitle())
//                .isEqualTo("Security Alert: Your Profile Has Been Updated");
//        assertThat(messageCaptor.getValue().getText())
//                .contains("registered email address has been changed");
//    }
//
//    @Test
//    void updateMyProfile_rejectsEmailAlreadyUsedByAnotherAccount() {
//        UUID userId = UUID.randomUUID();
//        Users user = user(
//                userId, "old@example.com", "Employee", null, UserStatus.ACTIVE
//        );
//        UserProfileRequestDTO request = userProfileRequest("taken@example.com");
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);
//
//        assertThatThrownBy(() -> userService.updateMyProfile(userId, request))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Email is already in use");
//        verify(userRepository, never()).save(any(Users.class));
//        verify(emailService, never()).sendEmail(any(MessageInfor.class));
//    }
//
//    @Test
//    void updateMyProfile_sendsNotificationWithoutEmailChangeNoticeWhenEmailIsUnchanged() {
//        UUID userId = UUID.randomUUID();
//        Users user = user(
//                userId, "employee@example.com", "Employee", null, UserStatus.ACTIVE
//        );
//        UserProfileRequestDTO request = userProfileRequest("employee@example.com");
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//        when(userRepository.save(user)).thenReturn(user);
//
//        userService.updateMyProfile(userId, request);
//
//        ArgumentCaptor<MessageInfor> messageCaptor = ArgumentCaptor.forClass(MessageInfor.class);
//        verify(emailService).sendEmail(messageCaptor.capture());
//        assertThat(messageCaptor.getValue().getText())
//                .doesNotContain("registered email address has been changed");
//    }
//
//    @Test
//    void updateMyProfile_returnsSavedProfileWhenNotificationFails() {
//        UUID userId = UUID.randomUUID();
//        Users user = user(
//                userId, "employee@example.com", "Employee", null, UserStatus.ACTIVE
//        );
//        UserProfileRequestDTO request = userProfileRequest("employee@example.com");
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//        when(userRepository.save(user)).thenReturn(user);
//        doThrow(new RuntimeException("mail server unavailable"))
//                .when(emailService).sendEmail(any(MessageInfor.class));
//
//        UserProfileResponseDTO result = userService.updateMyProfile(userId, request);
//
//        assertThat(result.getFirstName()).isEqualTo("Updated");
//        verify(userRepository).save(user);
//        verify(emailService).sendEmail(any(MessageInfor.class));
//    }
//
//    @Test
//    void updateMyProfile_throwsWhenUserDoesNotExistAndDoesNotSendMail() {
//        UUID missingId = UUID.randomUUID();
//        UserProfileRequestDTO request = userProfileRequest("employee@example.com");
//        when(userRepository.findById(missingId)).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> userService.updateMyProfile(missingId, request))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("User not found");
//        verify(emailService, never()).sendEmail(any(MessageInfor.class));
//    }
//
//    private static Departments department(String name) {
//        Departments department = new Departments();
//        department.setId(UUID.randomUUID());
//        department.setDepartmentName(name);
//        return department;
//    }
//
//    private static Users user(
//            UUID id,
//            String email,
//            String role,
//            Departments department,
//            UserStatus status
//    ) {
//        Users user = new Users();
//        user.setId(id);
//        user.setEmail(email);
//        user.setFirstName("Lan");
//        user.setLastName("Nguyen");
//        user.setNumberPhone("0901234567");
//        user.setRole(role);
//        user.setDepartment(department);
//        user.setStatus(status);
//        user.setStartDate("2024-01-02");
//        return user;
//    }
//
//    private static UserProfileRequestDTO userProfileRequest(String email) {
//        UserProfileRequestDTO request = new UserProfileRequestDTO();
//        request.setFirstName("Updated");
//        request.setLastName("Profile");
//        request.setEmail(email);
//        request.setNumberPhone("0987654321");
//        return request;
//    }
//}
