//package com.fpt.backend.controller.authencationController;
//
//import com.fpt.backend.configuration.JWTService;
//import com.fpt.backend.configuration.MyUserDetail;
//import com.fpt.backend.dto.request.authentication.AuthenticateRequest;
//import com.fpt.backend.dto.response.authentication.AuthenticateResponse;
//import com.fpt.backend.entity.Departments;
//import com.fpt.backend.entity.Role;
//import com.fpt.backend.entity.UserRole;
//import com.fpt.backend.entity.Users;
//import com.fpt.backend.enums.KeyStatus;
//import com.fpt.backend.repository.signature.UserKeysRepository;
//import com.fpt.backend.service.impl.user.UserServiceImpl;
//import com.fpt.backend.util.BaseResponse;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//
//import java.util.List;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.verifyNoInteractions;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class AuthenticateControllerTest {
//
//    @Mock
//    private AuthenticationManager authenticationManager;
//
//    @Mock
//    private JWTService jwtService;
//
//    @Mock
//    private UserServiceImpl userServiceImpl;
//
//    @Mock
//    private UserKeysRepository userKeysRepository;
//
//    @InjectMocks
//    private AuthenticateController authenticateController;
//
//    @Test
//    void authenticateUser_returnsCreatedResponseForValidCredentials() {
//        UUID userId = UUID.randomUUID();
//        Users user = user(userId, "employee@example", "Lan", "Nguyen", "Engineering");
//        MyUserDetail userDetail = new MyUserDetail(user);
//        Authentication authenticated = org.mockito.Mockito.mock(Authentication.class);
//        AuthenticateRequest request = new AuthenticateRequest("employee@example.com", "ValidPass1!");
//
//        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
//                .thenReturn(authenticated);
//        when(authenticated.getPrincipal()).thenReturn(userDetail);
//        when(jwtService.generateToken(userDetail)).thenReturn("signed-jwt-token");
//        when(userKeysRepository.existsAllByKeyStatus(userId, KeyStatus.ACTIVE)).thenReturn(true);
//
//        ResponseEntity<BaseResponse<AuthenticateResponse>> response =
//                authenticateController.authenticateUser(request);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
//        assertThat(response.getBody()).satisfies(body -> {
//            assertThat(body.getStatus()).isEqualTo(HttpStatus.CREATED.value());
//            assertThat(body.getMessage()).isEqualTo("Login susscessed");
//            assertThat(body.getData().getToken()).isEqualTo("signed-jwt-token");
//            assertThat(body.getData().getRole()).isEqualTo("Employee");
//            assertThat(body.getData().getFullName()).isEqualTo("Lan Nguyen");
//            assertThat(body.getData().getDepartmentName()).isEqualTo("Engineering");
//            assertThat(body.getData().isHasSignatureKey()).isTrue();
//        });
//
//        ArgumentCaptor<Authentication> authenticationCaptor = ArgumentCaptor.forClass(Authentication.class);
//        verify(authenticationManager).authenticate(authenticationCaptor.capture());
//        assertThat(authenticationCaptor.getValue()).isInstanceOf(UsernamePasswordAuthenticationToken.class);
//        UsernamePasswordAuthenticationToken loginToken =
//                (UsernamePasswordAuthenticationToken) authenticationCaptor.getValue();
//        assertThat(loginToken.getPrincipal()).isEqualTo("employee@example.com");
//        assertThat(loginToken.getCredentials()).isEqualTo("ValidPass1!");
//        verify(jwtService).generateToken(userDetail);
//        verify(userKeysRepository).existsAllByKeyStatus(userId, KeyStatus.ACTIVE);
//    }
//
//    @Test
//    void authenticateUser_usesEmptyDepartmentNameWhenUserHasNoDepartment() {
//        UUID userId = UUID.randomUUID();
//        Users user = user(userId, "external@example.com", "Minh", "Tran", null);
//        MyUserDetail userDetail = new MyUserDetail(user);
//        Authentication authenticated = org.mockito.Mockito.mock(Authentication.class);
//        AuthenticateRequest request = new AuthenticateRequest("external@example.com", "ValidPass1!");
//
//        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
//                .thenReturn(authenticated);
//        when(authenticated.getPrincipal()).thenReturn(userDetail);
//        when(jwtService.generateToken(userDetail)).thenReturn("external-jwt-token");
//        when(userKeysRepository.existsAllByKeyStatus(userId, KeyStatus.ACTIVE)).thenReturn(false);
//
//        ResponseEntity<BaseResponse<AuthenticateResponse>> response =
//                authenticateController.authenticateUser(request);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
//        assertThat(response.getBody().getData().getDepartmentName()).isEmpty();
//        assertThat(response.getBody().getData().isHasSignatureKey()).isFalse();
//        verify(userKeysRepository).existsAllByKeyStatus(userId, KeyStatus.ACTIVE);
//    }
//
//    @Test
//    void authenticateUser_wrapsAuthenticationFailureAndSkipsTokenGeneration() {
//        AuthenticateRequest request = new AuthenticateRequest("employee@example.com", "wrong-password");
//        BadCredentialsException authenticationFailure = new BadCredentialsException("Bad credentials");
//        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
//                .thenThrow(authenticationFailure);
//
//        assertThatThrownBy(() -> authenticateController.authenticateUser(request))
//                .isInstanceOf(RuntimeException.class)
//                .hasCause(authenticationFailure);
//
//        verifyNoInteractions(jwtService, userKeysRepository);
//    }
//
//    private static Users user(
//            UUID id,
//            String email,
//            String firstName,
//            String lastName,
//            String departmentName
//    ) {
//        Users user = new Users();
//        user.setId(id);
//        user.setEmail(email);
//        user.setPassword("encoded-password");
//        user.setFirstName(firstName);
//        user.setLastName(lastName);
//
//        Role role = new Role();
//        role.setRoleName("Employee");
//        UserRole userRole = new UserRole();
//        userRole.setUser(user);
//        userRole.setRole(role);
//        user.setUserRoles(List.of(userRole));
//
//        if (departmentName != null) {
//            Departments department = new Departments();
//            department.setDepartmentName(departmentName);
//            user.setDepartment(department);
//        }
//        return user;
//    }
//}
