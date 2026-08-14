//package com.fpt.backend.service.impl.user;
//
//import com.fpt.backend.dto.request.user.UserCreateRequestDTO;
//import com.fpt.backend.dto.request.user.UserFilterRequestDTO;
//import com.fpt.backend.dto.request.user.UserUpdateRequestDTO;
//import com.fpt.backend.dto.response.user.UserResponseDTO;
//import com.fpt.backend.entity.Departments;
//import com.fpt.backend.entity.Role;
//import com.fpt.backend.entity.UserRole;
//import com.fpt.backend.entity.Users;
//import com.fpt.backend.enums.UserStatus;
//import com.fpt.backend.mail.EmailService;
//import com.fpt.backend.mail.MessageInfor;
//import com.fpt.backend.repository.department.DepartmentRepository;
//import com.fpt.backend.repository.role.RoleRepository;
//import com.fpt.backend.repository.user.UserRepository;
//import com.fpt.backend.repository.userRole.UserRoleRepository;
//import com.fpt.backend.util.CurrentUser;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.verifyNoInteractions;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class UserManagementServiceTest {
//
//    private static final String DEFAULT_PASSWORD = "StrongPass1!";
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private PasswordEncoder passwordEncoder;
//
//    @Mock
//    private DepartmentRepository departmentRepository;
//
//    @Mock
//    private CurrentUser currentUser;
//
//    @Mock
//    private RoleRepository roleRepository;
//
//    @Mock
//    private UserRoleRepository userRoleRepository;
//
//    @Mock
//    private EmailService emailService;
//
//    @InjectMocks
//    private UserServiceImpl userService;
//
//    @Test
//    void getAllUsers_mapsEveryUserToResponseDto() {
//        Departments engineering = department("Engineering");
//        Users employee = user(
//                UUID.randomUUID(),
//                "employee@example.com",
//                "Employee",
//                engineering,
//                UserStatus.ACTIVE
//        );
//        Users partner = user(
//                UUID.randomUUID(),
//                "partner@example.com",
//                "External Parners",
//                null,
//                UserStatus.INACTIVE
//        );
//        when(userRepository.findAll()).thenReturn(List.of(employee, partner));
//
//        List<UserResponseDTO> result = userService.getAllUsers();
//
//        assertThat(result)
//                .hasSize(2)
//                .extracting(UserResponseDTO::getEmail)
//                .containsExactly("employee@example.com", "partner@example.com");
//        assertThat(result.getFirst().getDepartmentName()).isEqualTo("Engineering");
//        assertThat(result.get(1).getStatus()).isEqualTo(UserStatus.INACTIVE);
//    }
//
//    @Test
//    void getUserById_allowsAccountantToViewEmployee() {
//        Users actor = user(
//                UUID.randomUUID(), "accountant@example.com", "Accountant", null, UserStatus.ACTIVE
//        );
//        Departments engineering = department("Engineering");
//        UUID employeeId = UUID.randomUUID();
//        Users employee = user(
//                employeeId, "employee@example.com", "Employee", engineering, UserStatus.ACTIVE
//        );
//        when(userRepository.findById(employeeId)).thenReturn(Optional.of(employee));
//        when(currentUser.getCurrentUser()).thenReturn(actor);
//
//        UserResponseDTO result = userService.getUserById(employeeId);
//
//        assertThat(result.getId()).isEqualTo(employeeId);
//        assertThat(result.getRole()).isEqualTo("Employee");
//        assertThat(result.getDepartmentName()).isEqualTo("Engineering");
//    }
//
//    @Test
//    void getUserById_deniesCeoFromViewingAnotherCeo() {
//        UUID targetId = UUID.randomUUID();
//        Users actor = user(
//                UUID.randomUUID(), "ceo@example.com", "CEO", null, UserStatus.ACTIVE
//        );
//        Users target = user(
//                targetId, "other.ceo@example.com", "CEO", null, UserStatus.ACTIVE
//        );
//        when(userRepository.findById(targetId)).thenReturn(Optional.of(target));
//        when(currentUser.getCurrentUser()).thenReturn(actor);
//
//        assertThatThrownBy(() -> userService.getUserById(targetId))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Access Denied");
//    }
//
//    @Test
//    void getUserById_allowsHeadOfDepartmentToViewEmployeeInOwnDepartment() {
//        Departments engineering = department("Engineering");
//        Users actor = user(
//                UUID.randomUUID(), "hod@example.com", "HeadOfDepartment", engineering, UserStatus.ACTIVE
//        );
//        UUID employeeId = UUID.randomUUID();
//        Users employee = user(
//                employeeId, "employee@example.com", "Employee", engineering, UserStatus.ACTIVE
//        );
//        when(userRepository.findById(employeeId)).thenReturn(Optional.of(employee));
//        when(currentUser.getCurrentUser()).thenReturn(actor);
//
//        UserResponseDTO result = userService.getUserById(employeeId);
//
//        assertThat(result.getEmail()).isEqualTo("employee@example.com");
//    }
//
//    @Test
//    void getUserById_deniesHeadOfDepartmentFromViewingOtherDepartment() {
//        Departments engineering = department("Engineering");
//        Departments finance = department("Finance");
//        Users actor = user(
//                UUID.randomUUID(), "hod@example.com", "HeadOfDepartment", engineering, UserStatus.ACTIVE
//        );
//        UUID employeeId = UUID.randomUUID();
//        Users employee = user(
//                employeeId, "employee@example.com", "Employee", finance, UserStatus.ACTIVE
//        );
//        when(userRepository.findById(employeeId)).thenReturn(Optional.of(employee));
//        when(currentUser.getCurrentUser()).thenReturn(actor);
//
//        assertThatThrownBy(() -> userService.getUserById(employeeId))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Access Denied");
//    }
//
//    @Test
//    void getUserById_throwsWhenUserDoesNotExist() {
//        UUID missingId = UUID.randomUUID();
//        when(userRepository.findById(missingId)).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> userService.getUserById(missingId))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("User not found");
//        verifyNoInteractions(currentUser);
//    }
//
//    @Test
//    void createUser_rejectsBlankPasswordBeforeCallingRepository() {
//        UserCreateRequestDTO request = userCreateRequest("Employee", "Engineering");
//        request.setPassword("");
//
//        assertThatThrownBy(() -> userService.createUser(request))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Mật khẩu");
//        verifyNoInteractions(userRepository, currentUser);
//    }
//
//    @Test
//    void createUser_rejectsDuplicateEmail() {
//        UserCreateRequestDTO request = userCreateRequest("Employee", "Engineering");
//        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);
//
//        assertThatThrownBy(() -> userService.createUser(request))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Email is already in use");
//        verifyNoInteractions(currentUser, passwordEncoder, departmentRepository);
//    }
//
//    @Test
//    void createUser_deniesCeoEvenForAnOtherwiseValidRequest() {
//        UserCreateRequestDTO request = userCreateRequest("Employee", "Engineering");
//        Users ceo = user(
//                UUID.randomUUID(), "ceo@example.com", "CEO", null, UserStatus.ACTIVE
//        );
//        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
//        when(currentUser.getCurrentUser()).thenReturn(ceo);
//
//        assertThatThrownBy(() -> userService.createUser(request))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Access Denied");
//        verify(userRepository, never()).save(any(Users.class));
//    }
//
//    @Test
//    void createUser_deniesAccountantFromCreatingAdministrator() {
//        UserCreateRequestDTO request = userCreateRequest("Administrator", "Engineering");
//        Users accountant = user(
//                UUID.randomUUID(), "accountant@example.com", "Accountant", null, UserStatus.ACTIVE
//        );
//        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
//        when(currentUser.getCurrentUser()).thenReturn(accountant);
//
//        assertThatThrownBy(() -> userService.createUser(request))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Access Denied");
//        verify(userRepository, never()).save(any(Users.class));
//    }
//
//    @Test
//    void createUser_createsEmployeeWithDepartmentRoleAndWelcomeEmail() {
//        UUID savedId = UUID.randomUUID();
//        Departments engineering = department("Engineering");
//        Role employeeRole = role("Employee");
//        Users accountant = user(
//                UUID.randomUUID(), "accountant@example.com", "Accountant", null, UserStatus.ACTIVE
//        );
//        UserCreateRequestDTO request = userCreateRequest("Employee", "Engineering");
//        request.setSendWelcomeEmail(true);
//
//        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
//        when(currentUser.getCurrentUser()).thenReturn(accountant);
//        when(passwordEncoder.encode(DEFAULT_PASSWORD)).thenReturn("encoded-new-password");
//        when(departmentRepository.findByDepartmentName("Engineering")).thenReturn(Optional.of(engineering));
//        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> {
//            Users savedUser = invocation.getArgument(0);
//            savedUser.setId(savedId);
//            return savedUser;
//        });
//        when(roleRepository.findByRoleName("Employee")).thenReturn(Optional.of(employeeRole));
//
//        UserResponseDTO result = userService.createUser(request);
//
//        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);
//        verify(userRepository).save(userCaptor.capture());
//        Users persisted = userCaptor.getValue();
//        assertThat(persisted.getEmail()).isEqualTo("new.user@example.com");
//        assertThat(persisted.getPassword()).isEqualTo("encoded-new-password");
//        assertThat(persisted.getDepartment()).isSameAs(engineering);
//        assertThat(result.getId()).isEqualTo(savedId);
//
//        ArgumentCaptor<UserRole> userRoleCaptor = ArgumentCaptor.forClass(UserRole.class);
//        verify(userRoleRepository).save(userRoleCaptor.capture());
//        assertThat(userRoleCaptor.getValue().getUser()).isSameAs(persisted);
//        assertThat(userRoleCaptor.getValue().getRole()).isSameAs(employeeRole);
//
//        ArgumentCaptor<MessageInfor> messageCaptor = ArgumentCaptor.forClass(MessageInfor.class);
//        verify(emailService).sendEmail(messageCaptor.capture());
//        assertThat(messageCaptor.getValue().getEmail()).isEqualTo("new.user@example.com");
//        assertThat(messageCaptor.getValue().getTitle()).isEqualTo("Welcome to E-CONTRACT System");
//        assertThat(messageCaptor.getValue().getText()).contains(DEFAULT_PASSWORD);
//    }
//
//    @Test
//    void createUser_allowsHeadOfDepartmentToCreateEmployeeInOwnDepartment() {
//        Departments engineering = department("Engineering");
//        Role employeeRole = role("Employee");
//        Users headOfDepartment = user(
//                UUID.randomUUID(), "hod@example.com", "HeadOfDepartment", engineering, UserStatus.ACTIVE
//        );
//        UserCreateRequestDTO request = userCreateRequest("Employee", "Engineering");
//        request.setStatus(UserStatus.INACTIVE);
//
//        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
//        when(currentUser.getCurrentUser()).thenReturn(headOfDepartment);
//        when(passwordEncoder.encode(DEFAULT_PASSWORD)).thenReturn("encoded-new-password");
//        when(departmentRepository.findByDepartmentName("Engineering")).thenReturn(Optional.of(engineering));
//        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));
//        when(roleRepository.findByRoleName("Employee")).thenReturn(Optional.of(employeeRole));
//
//        UserResponseDTO result = userService.createUser(request);
//
//        assertThat(result.getRole()).isEqualTo("Employee");
//        verify(userRepository).save(any(Users.class));
//        verify(userRoleRepository).save(any(UserRole.class));
//    }
//
//    @Test
//    void createUser_deniesHeadOfDepartmentFromCreatingOutsideOwnDepartment() {
//        Departments engineering = department("Engineering");
//        Users headOfDepartment = user(
//                UUID.randomUUID(), "hod@example.com", "HeadOfDepartment", engineering, UserStatus.ACTIVE
//        );
//        UserCreateRequestDTO request = userCreateRequest("Employee", "Finance");
//        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
//        when(currentUser.getCurrentUser()).thenReturn(headOfDepartment);
//
//        assertThatThrownBy(() -> userService.createUser(request))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Access Denied");
//        verifyNoInteractions(departmentRepository, roleRepository, userRoleRepository);
//        verify(userRepository, never()).save(any(Users.class));
//    }
//
//    @Test
//    void createUser_rejectsSecondActiveHeadOfDepartmentInSameDepartment() {
//        Departments engineering = department("Engineering");
//        Users accountant = user(
//                UUID.randomUUID(), "accountant@example.com", "Accountant", null, UserStatus.ACTIVE
//        );
//        Users existingHead = user(
//                UUID.randomUUID(), "existing.hod@example.com", "HeadOfDepartment", engineering, UserStatus.ACTIVE
//        );
//        UserCreateRequestDTO request = userCreateRequest("HeadOfDepartment", "Engineering");
//
//        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
//        when(currentUser.getCurrentUser()).thenReturn(accountant);
//        when(departmentRepository.findByDepartmentName("Engineering")).thenReturn(Optional.of(engineering));
//        when(userRepository.findByRoleAndDepartment("HeadOfDepartment", engineering))
//                .thenReturn(List.of(existingHead));
//
//        assertThatThrownBy(() -> userService.createUser(request))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Engineering");
//        verify(userRepository, never()).save(any(Users.class));
//    }
//
//    @Test
//    void updateUser_updatesPermittedEmployeeAndEncodesNewPassword() {
//        UUID employeeId = UUID.randomUUID();
//        Departments engineering = department("Engineering");
//        Users accountant = user(
//                UUID.randomUUID(), "accountant@example.com", "Accountant", null, UserStatus.ACTIVE
//        );
//        Users employee = user(
//                employeeId, "employee@example.com", "Employee", engineering, UserStatus.ACTIVE
//        );
//        employee.setPassword("old-hash");
//        UserUpdateRequestDTO request = userUpdateRequest(
//                "employee@example.com", "Employee", "Engineering"
//        );
//        request.setPassword(DEFAULT_PASSWORD);
//
//        when(userRepository.findById(employeeId)).thenReturn(Optional.of(employee));
//        when(currentUser.getCurrentUser()).thenReturn(accountant);
//        when(departmentRepository.findByDepartmentName("Engineering")).thenReturn(Optional.of(engineering));
//        when(passwordEncoder.encode(DEFAULT_PASSWORD)).thenReturn("updated-hash");
//        when(userRepository.save(employee)).thenReturn(employee);
//
//        UserResponseDTO result = userService.updateUser(employeeId, request);
//
//        assertThat(employee.getFirstName()).isEqualTo("Updated");
//        assertThat(employee.getNumberPhone()).isEqualTo("0987654321");
//        assertThat(employee.getPassword()).isEqualTo("updated-hash");
//        assertThat(result.getEmail()).isEqualTo("employee@example.com");
//        verify(userRepository).save(employee);
//        verify(userRoleRepository, never()).save(any(UserRole.class));
//    }
//
//    @Test
//    void updateUser_throwsWhenTargetDoesNotExist() {
//        UUID missingId = UUID.randomUUID();
//        UserUpdateRequestDTO request = userUpdateRequest(
//                "missing@example.com", "Employee", "Engineering"
//        );
//        when(userRepository.findById(missingId)).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> userService.updateUser(missingId, request))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("User not found");
//        verifyNoInteractions(currentUser);
//    }
//
//    @Test
//    void updateUser_deniesAdministrator() {
//        UUID employeeId = UUID.randomUUID();
//        Users administrator = user(
//                UUID.randomUUID(), "admin@example.com", "Administrator", null, UserStatus.ACTIVE
//        );
//        Users employee = user(
//                employeeId, "employee@example.com", "Employee", null, UserStatus.ACTIVE
//        );
//        UserUpdateRequestDTO request = userUpdateRequest(
//                "employee@example.com", "Employee", "Engineering"
//        );
//        when(userRepository.findById(employeeId)).thenReturn(Optional.of(employee));
//        when(currentUser.getCurrentUser()).thenReturn(administrator);
//
//        assertThatThrownBy(() -> userService.updateUser(employeeId, request))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Access Denied");
//        verify(userRepository, never()).save(any(Users.class));
//    }
//
//    @Test
//    void updateUser_deniesAccountantFromChangingEmployeeToAdministrator() {
//        UUID employeeId = UUID.randomUUID();
//        Users accountant = user(
//                UUID.randomUUID(), "accountant@example.com", "Accountant", null, UserStatus.ACTIVE
//        );
//        Users employee = user(
//                employeeId, "employee@example.com", "Employee", null, UserStatus.ACTIVE
//        );
//        UserUpdateRequestDTO request = userUpdateRequest(
//                "employee@example.com", "Administrator", "Engineering"
//        );
//        when(userRepository.findById(employeeId)).thenReturn(Optional.of(employee));
//        when(currentUser.getCurrentUser()).thenReturn(accountant);
//
//        assertThatThrownBy(() -> userService.updateUser(employeeId, request))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Access Denied");
//        verify(userRepository, never()).save(any(Users.class));
//    }
//
//    @Test
//    void updateUser_allowsHeadOfDepartmentToUpdateEmployeeInOwnDepartment() {
//        UUID employeeId = UUID.randomUUID();
//        Departments engineering = department("Engineering");
//        Users headOfDepartment = user(
//                UUID.randomUUID(), "hod@example.com", "HeadOfDepartment", engineering, UserStatus.ACTIVE
//        );
//        Users employee = user(
//                employeeId, "employee@example.com", "Employee", engineering, UserStatus.ACTIVE
//        );
//        UserUpdateRequestDTO request = userUpdateRequest(
//                "employee@example.com", "Employee", "Engineering"
//        );
//        request.setStatus(UserStatus.INACTIVE);
//        when(userRepository.findById(employeeId)).thenReturn(Optional.of(employee));
//        when(currentUser.getCurrentUser()).thenReturn(headOfDepartment);
//        when(departmentRepository.findByDepartmentName("Engineering")).thenReturn(Optional.of(engineering));
//        when(userRepository.save(employee)).thenReturn(employee);
//
//        UserResponseDTO result = userService.updateUser(employeeId, request);
//
//        assertThat(result.getStatus()).isEqualTo(UserStatus.INACTIVE);
//        assertThat(employee.getDepartment()).isSameAs(engineering);
//        verify(userRepository).save(employee);
//    }
//
//    @Test
//    void updateUser_deniesHeadOfDepartmentFromUpdatingEmployeeInOtherDepartment() {
//        UUID employeeId = UUID.randomUUID();
//        Departments engineering = department("Engineering");
//        Departments finance = department("Finance");
//        Users headOfDepartment = user(
//                UUID.randomUUID(), "hod@example.com", "HeadOfDepartment", engineering, UserStatus.ACTIVE
//        );
//        Users employee = user(
//                employeeId, "employee@example.com", "Employee", finance, UserStatus.ACTIVE
//        );
//        UserUpdateRequestDTO request = userUpdateRequest(
//                "employee@example.com", "Employee", "Engineering"
//        );
//        when(userRepository.findById(employeeId)).thenReturn(Optional.of(employee));
//        when(currentUser.getCurrentUser()).thenReturn(headOfDepartment);
//
//        assertThatThrownBy(() -> userService.updateUser(employeeId, request))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Access Denied");
//        verify(userRepository, never()).save(any(Users.class));
//    }
//
//    @Test
//    void updateUser_rejectsChangingToAnEmailThatAlreadyExists() {
//        UUID employeeId = UUID.randomUUID();
//        Users accountant = user(
//                UUID.randomUUID(), "accountant@example.com", "Accountant", null, UserStatus.ACTIVE
//        );
//        Users employee = user(
//                employeeId, "employee@example.com", "Employee", null, UserStatus.ACTIVE
//        );
//        UserUpdateRequestDTO request = userUpdateRequest(
//                "taken@example.com", "Employee", null
//        );
//        request.setStatus(UserStatus.INACTIVE);
//        when(userRepository.findById(employeeId)).thenReturn(Optional.of(employee));
//        when(currentUser.getCurrentUser()).thenReturn(accountant);
//        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);
//
//        assertThatThrownBy(() -> userService.updateUser(employeeId, request))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Email is already in use");
//        verify(userRepository, never()).save(any(Users.class));
//    }
//
//    @Test
//    void updateUser_replacesExistingUserRoleWhenRoleChanges() {
//        UUID employeeId = UUID.randomUUID();
//        Departments engineering = department("Engineering");
//        Users accountant = user(
//                UUID.randomUUID(), "accountant@example.com", "Accountant", null, UserStatus.ACTIVE
//        );
//        Users employee = user(
//                employeeId, "employee@example.com", "Employee", engineering, UserStatus.ACTIVE
//        );
//        Role oldRole = role("Employee");
//        Role newRole = role("HeadOfDepartment");
//        UserRole assignment = new UserRole();
//        assignment.setUser(employee);
//        assignment.setRole(oldRole);
//        UserUpdateRequestDTO request = userUpdateRequest(
//                "employee@example.com", "HeadOfDepartment", "Engineering"
//        );
//        request.setStatus(UserStatus.INACTIVE);
//
//        when(userRepository.findById(employeeId)).thenReturn(Optional.of(employee));
//        when(currentUser.getCurrentUser()).thenReturn(accountant);
//        when(roleRepository.findByRoleName("HeadOfDepartment")).thenReturn(Optional.of(newRole));
//        when(userRoleRepository.findByUser(employee)).thenReturn(Optional.of(assignment));
//        when(departmentRepository.findByDepartmentName("Engineering")).thenReturn(Optional.of(engineering));
//        when(userRepository.save(employee)).thenReturn(employee);
//
//        UserResponseDTO result = userService.updateUser(employeeId, request);
//
//        assertThat(result.getRole()).isEqualTo("HeadOfDepartment");
//        assertThat(assignment.getRole()).isSameAs(newRole);
//        verify(userRoleRepository).save(assignment);
//    }
//
//    @Test
//    void updateUser_createsUserRoleWhenRoleChangesAndNoAssignmentExists() {
//        UUID employeeId = UUID.randomUUID();
//        Departments engineering = department("Engineering");
//        Users accountant = user(
//                UUID.randomUUID(), "accountant@example.com", "Accountant", null, UserStatus.ACTIVE
//        );
//        Users employee = user(
//                employeeId, "employee@example.com", "Employee", engineering, UserStatus.ACTIVE
//        );
//        Role newRole = role("HeadOfDepartment");
//        UserUpdateRequestDTO request = userUpdateRequest(
//                "employee@example.com", "HeadOfDepartment", "Engineering"
//        );
//        request.setStatus(UserStatus.INACTIVE);
//
//        when(userRepository.findById(employeeId)).thenReturn(Optional.of(employee));
//        when(currentUser.getCurrentUser()).thenReturn(accountant);
//        when(roleRepository.findByRoleName("HeadOfDepartment")).thenReturn(Optional.of(newRole));
//        when(userRoleRepository.findByUser(employee)).thenReturn(Optional.empty());
//        when(departmentRepository.findByDepartmentName("Engineering")).thenReturn(Optional.of(engineering));
//        when(userRepository.save(employee)).thenReturn(employee);
//
//        userService.updateUser(employeeId, request);
//
//        ArgumentCaptor<UserRole> assignmentCaptor = ArgumentCaptor.forClass(UserRole.class);
//        verify(userRoleRepository).save(assignmentCaptor.capture());
//        assertThat(assignmentCaptor.getValue().getUser()).isSameAs(employee);
//        assertThat(assignmentCaptor.getValue().getRole()).isSameAs(newRole);
//    }
//
//    @Test
//    void getAllUsersFiltered_normalizesAdministratorFiltersAndPaging() {
//        Users administrator = user(
//                UUID.randomUUID(), "admin@example.com", "Administrator", null, UserStatus.ACTIVE
//        );
//        Users employee = user(
//                UUID.randomUUID(), "employee@example.com", "Employee", null, UserStatus.ACTIVE
//        );
//        UserFilterRequestDTO filter = UserFilterRequestDTO.builder()
//                .role("All")
//                .departmentName("All")
//                .keyword("  lan  ")
//                .status("All")
//                .build();
//        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(administrator));
//        when(userRepository.searchAndFilterUsers(any(UserFilterRequestDTO.class), any(Pageable.class)))
//                .thenReturn(new PageImpl<>(List.of(employee)));
//
//        Page<UserResponseDTO> result = userService.getAllUsersFiltered(filter, "admin@example.com", 1, 20);
//
//        assertThat(result.getContent()).singleElement()
//                .extracting(UserResponseDTO::getEmail)
//                .isEqualTo("employee@example.com");
//        ArgumentCaptor<UserFilterRequestDTO> filterCaptor = ArgumentCaptor.forClass(UserFilterRequestDTO.class);
//        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
//        verify(userRepository).searchAndFilterUsers(filterCaptor.capture(), pageableCaptor.capture());
//        assertThat(filterCaptor.getValue().getAllowedRoles())
//                .containsExactly("Accountant", "HeadOfDepartment", "Employee", "External Parners");
//        assertThat(filterCaptor.getValue().getRole()).isEmpty();
//        assertThat(filterCaptor.getValue().getDepartmentName()).isEmpty();
//        assertThat(filterCaptor.getValue().getKeyword()).isEqualTo("lan");
//        assertThat(filterCaptor.getValue().getStatusEnum()).isNull();
//        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(1);
//        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(20);
//        assertThat(pageableCaptor.getValue().getSort().getOrderFor("id").getDirection())
//                .isEqualTo(Sort.Direction.DESC);
//    }
//
//    @Test
//    void getAllUsersFiltered_forcesHeadOfDepartmentToOwnDepartmentAndEmployees() {
//        Departments engineering = department("Engineering");
//        Users headOfDepartment = user(
//                UUID.randomUUID(), "hod@example.com", "HeadOfDepartment", engineering, UserStatus.ACTIVE
//        );
//        UserFilterRequestDTO filter = UserFilterRequestDTO.builder()
//                .role("All")
//                .departmentName("Finance")
//                .keyword(null)
//                .status("active")
//                .build();
//        when(userRepository.findByEmail("hod@example.com")).thenReturn(Optional.of(headOfDepartment));
//        when(userRepository.searchAndFilterUsers(any(UserFilterRequestDTO.class), any(Pageable.class)))
//                .thenReturn(Page.empty());
//
//        Page<UserResponseDTO> result = userService.getAllUsersFiltered(filter, "hod@example.com", 0, 10);
//
//        assertThat(result).isEmpty();
//        assertThat(filter.getAllowedRoles()).containsExactly("Employee");
//        assertThat(filter.getDepartmentName()).isEqualTo("Engineering");
//        assertThat(filter.getRole()).isEmpty();
//        assertThat(filter.getKeyword()).isEmpty();
//        assertThat(filter.getStatusEnum()).isEqualTo(UserStatus.ACTIVE);
//    }
//
//    @Test
//    void getAllUsersFiltered_deniesRoleWithoutListPermission() {
//        Users employee = user(
//                UUID.randomUUID(), "employee@example.com", "Employee", null, UserStatus.ACTIVE
//        );
//        UserFilterRequestDTO filter = new UserFilterRequestDTO();
//        when(userRepository.findByEmail("employee@example.com")).thenReturn(Optional.of(employee));
//
//        assertThatThrownBy(() -> userService.getAllUsersFiltered(filter, "employee@example.com", 0, 10))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Access Denied");
//        verify(userRepository, never()).searchAndFilterUsers(any(), any());
//    }
//
//    @Test
//    void getAllUsersFiltered_throwsWhenCurrentUserCannotBeFound() {
//        UserFilterRequestDTO filter = new UserFilterRequestDTO();
//        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> userService.getAllUsersFiltered(filter, "missing@example.com", 0, 10))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Current user not found");
//        verify(userRepository, never()).searchAndFilterUsers(any(), any());
//    }
//
//    private static Departments department(String name) {
//        Departments department = new Departments();
//        department.setId(UUID.randomUUID());
//        department.setDepartmentName(name);
//        department.setDepartmentCode(name.substring(0, Math.min(name.length(), 3)).toUpperCase());
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
//        user.setPassword("encoded-password");
//        user.setFirstName("Lan");
//        user.setLastName("Nguyen");
//        user.setNumberPhone("0901234567");
//        user.setRole(role);
//        user.setDepartment(department);
//        user.setStatus(status);
//        user.setDob("1995-01-15");
//        user.setStartDate("2024-01-02");
//        return user;
//    }
//
//    private static Role role(String roleName) {
//        Role role = new Role();
//        role.setId(UUID.randomUUID());
//        role.setRoleName(roleName);
//        role.setRoleCode(roleName.toUpperCase());
//        return role;
//    }
//
//    private static UserCreateRequestDTO userCreateRequest(
//            String role,
//            String departmentName
//    ) {
//        UserCreateRequestDTO request = new UserCreateRequestDTO();
//        request.setEmail("new.user@example.com");
//        request.setPassword(DEFAULT_PASSWORD);
//        request.setFirstName("Mai");
//        request.setLastName("Tran");
//        request.setNumberPhone("0907654321");
//        request.setRole(role);
//        request.setStatus(UserStatus.ACTIVE);
//        request.setDepartmentName(departmentName);
//        request.setDob("1998-02-03");
//        request.setStartDate("2024-04-01");
//        request.setSendWelcomeEmail(false);
//        return request;
//    }
//
//    private static UserUpdateRequestDTO userUpdateRequest(
//            String email,
//            String role,
//            String departmentName
//    ) {
//        UserUpdateRequestDTO request = new UserUpdateRequestDTO();
//        request.setEmail(email);
//        request.setPassword("");
//        request.setFirstName("Updated");
//        request.setLastName("User");
//        request.setNumberPhone("0987654321");
//        request.setRole(role);
//        request.setStatus(UserStatus.ACTIVE);
//        request.setDepartmentName(departmentName);
//        request.setDob("1997-03-04");
//        request.setStartDate("2023-03-01");
//        return request;
//    }
//}
