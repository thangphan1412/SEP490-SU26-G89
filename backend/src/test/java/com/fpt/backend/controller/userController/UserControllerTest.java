//package com.fpt.backend.controller.userController;
//
//import com.fpt.backend.controller.userController.UserController;
//import com.fpt.backend.dto.request.user.UserCreateRequestDTO;
//import com.fpt.backend.dto.request.user.UserFilterRequestDTO;
//import com.fpt.backend.dto.request.user.UserUpdateRequestDTO;
//import com.fpt.backend.dto.response.user.UserResponseDTO;
//import com.fpt.backend.enums.UserStatus;
//import com.fpt.backend.service.interfaces.user.IUserService;
//import com.fpt.backend.util.BaseResponse;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//
//import java.security.Principal;
//import java.util.List;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class UserControllerTest {
//
//    @Mock
//    private IUserService userService;
//
//    @InjectMocks
//    private UserController userController;
//
//    @Test
//    void getAllUsers_returnsOkPageForAuthorizedPrincipal() {
//        UserFilterRequestDTO filter = new UserFilterRequestDTO();
//        Principal principal = () -> "accountant@example.com";
//        Page<UserResponseDTO> page = new PageImpl<>(List.of(userResponse()));
//        when(userService.getAllUsersFiltered(filter, "accountant@example.com", 0, 10))
//                .thenReturn(page);
//
//        ResponseEntity<BaseResponse<Page<UserResponseDTO>>> response =
//                userController.getAllUsers(filter, 0, 10, principal);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(response.getBody()).satisfies(body -> {
//            assertThat(body.getStatus()).isEqualTo(200);
//            assertThat(body.getMessage()).isEqualTo("Successfully fetched users");
//            assertThat(body.getData()).isSameAs(page);
//        });
//    }
//
//    @Test
//    void getAllUsers_returnsBadRequestWhenServiceFails() {
//        UserFilterRequestDTO filter = new UserFilterRequestDTO();
//        Principal principal = () -> "accountant@example.com";
//        when(userService.getAllUsersFiltered(filter, "accountant@example.com", 0, 10))
//                .thenThrow(new RuntimeException("Invalid status"));
//
//        ResponseEntity<BaseResponse<Page<UserResponseDTO>>> response =
//                userController.getAllUsers(filter, 0, 10, principal);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
//        assertThat(response.getBody()).satisfies(body -> {
//            assertThat(body.getStatus()).isEqualTo(400);
//            assertThat(body.getMessage()).contains("Invalid status");
//            assertThat(body.getData()).isNull();
//        });
//    }
//
//    @Test
//    void getUserById_returnsOkWhenServiceFindsUser() {
//        UUID userId = UUID.randomUUID();
//        UserResponseDTO user = userResponse();
//        user.setId(userId);
//        when(userService.getUserById(userId)).thenReturn(user);
//
//        ResponseEntity<BaseResponse<UserResponseDTO>> response = userController.getUserById(userId);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(response.getBody()).satisfies(body -> {
//            assertThat(body.getStatus()).isEqualTo(200);
//            assertThat(body.getMessage()).isEqualTo("User found");
//            assertThat(body.getData()).isSameAs(user);
//        });
//    }
//
//    @Test
//    void getUserById_returnsNotFoundWhenServiceThrows() {
//        UUID userId = UUID.randomUUID();
//        when(userService.getUserById(userId)).thenThrow(new RuntimeException("User not found"));
//
//        ResponseEntity<BaseResponse<UserResponseDTO>> response = userController.getUserById(userId);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
//        assertThat(response.getBody()).satisfies(body -> {
//            assertThat(body.getStatus()).isEqualTo(404);
//            assertThat(body.getMessage()).isEqualTo("User not found");
//            assertThat(body.getData()).isNull();
//        });
//    }
//
//    @Test
//    void createUser_returnsCreatedUser() {
//        UserCreateRequestDTO request = userCreateRequest();
//        UserResponseDTO createdUser = userResponse();
//        when(userService.createUser(request)).thenReturn(createdUser);
//
//        ResponseEntity<BaseResponse<UserResponseDTO>> response = userController.createUser(request);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
//        assertThat(response.getBody()).satisfies(body -> {
//            assertThat(body.getStatus()).isEqualTo(201);
//            assertThat(body.getMessage()).isEqualTo("User created successfully");
//            assertThat(body.getData()).isSameAs(createdUser);
//        });
//        verify(userService).createUser(request);
//    }
//
//    @Test
//    void createUser_returnsBadRequestWhenServiceRejectsRequest() {
//        UserCreateRequestDTO request = userCreateRequest();
//        when(userService.createUser(request)).thenThrow(new RuntimeException("Email is already in use!"));
//
//        ResponseEntity<BaseResponse<UserResponseDTO>> response = userController.createUser(request);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
//        assertThat(response.getBody()).satisfies(body -> {
//            assertThat(body.getStatus()).isEqualTo(400);
//            assertThat(body.getMessage()).isEqualTo("Email is already in use!");
//            assertThat(body.getData()).isNull();
//        });
//    }
//
//    @Test
//    void updateUser_returnsUpdatedUser() {
//        UUID userId = UUID.randomUUID();
//        UserUpdateRequestDTO request = userUpdateRequest();
//        UserResponseDTO updatedUser = userResponse();
//        when(userService.updateUser(userId, request)).thenReturn(updatedUser);
//
//        ResponseEntity<BaseResponse<UserResponseDTO>> response = userController.updateUser(userId, request);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(response.getBody()).satisfies(body -> {
//            assertThat(body.getStatus()).isEqualTo(200);
//            assertThat(body.getMessage()).isEqualTo("User updated successfully");
//            assertThat(body.getData()).isSameAs(updatedUser);
//        });
//        verify(userService).updateUser(userId, request);
//    }
//
//    @Test
//    void updateUser_returnsBadRequestWhenServiceRejectsUpdate() {
//        UUID userId = UUID.randomUUID();
//        UserUpdateRequestDTO request = userUpdateRequest();
//        when(userService.updateUser(userId, request)).thenThrow(new RuntimeException("Access Denied"));
//
//        ResponseEntity<BaseResponse<UserResponseDTO>> response = userController.updateUser(userId, request);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
//        assertThat(response.getBody()).satisfies(body -> {
//            assertThat(body.getStatus()).isEqualTo(400);
//            assertThat(body.getMessage()).isEqualTo("Access Denied");
//            assertThat(body.getData()).isNull();
//        });
//    }
//
//    private static UserResponseDTO userResponse() {
//        return UserResponseDTO.builder()
//                .id(UUID.randomUUID())
//                .email("employee@example.com")
//                .firstName("Lan")
//                .lastName("Nguyen")
//                .role("Employee")
//                .status(UserStatus.ACTIVE)
//                .departmentName("Engineering")
//                .build();
//    }
//
//    private static UserCreateRequestDTO userCreateRequest() {
//        UserCreateRequestDTO request = new UserCreateRequestDTO();
//        request.setEmail("new.user@example.com");
//        request.setPassword("StrongPass1!");
//        request.setFirstName("Mai");
//        request.setLastName("Tran");
//        request.setNumberPhone("0907654321");
//        request.setRole("Employee");
//        request.setStatus(UserStatus.ACTIVE);
//        request.setDepartmentName("Engineering");
//        request.setDob("1998-02-03");
//        request.setStartDate("2024-04-01");
//        return request;
//    }
//
//    private static UserUpdateRequestDTO userUpdateRequest() {
//        UserUpdateRequestDTO request = new UserUpdateRequestDTO();
//        request.setEmail("employee@example.com");
//        request.setFirstName("Updated");
//        request.setLastName("User");
//        request.setNumberPhone("0987654321");
//        request.setRole("Employee");
//        request.setStatus(UserStatus.ACTIVE);
//        request.setDepartmentName("Engineering");
//        request.setDob("1997-03-04");
//        request.setStartDate("2024-04-01");
//        return request;
//    }
//}
