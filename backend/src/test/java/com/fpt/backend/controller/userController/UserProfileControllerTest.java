//package com.fpt.backend.controller.userController;
//
//import com.fpt.backend.controller.userController.UserProfileController;
//import com.fpt.backend.dto.request.userProfile.UserProfileRequestDTO;
//import com.fpt.backend.dto.response.userProfile.UserProfileResponseDTO;
//import com.fpt.backend.entity.Users;
//import com.fpt.backend.service.interfaces.user.IUserService;
//import com.fpt.backend.util.BaseResponse;
//import com.fpt.backend.util.CurrentUser;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class UserProfileControllerTest {
//
//    @Mock
//    private IUserService userService;
//
//    @Mock
//    private CurrentUser currentUserUtil;
//
//    @InjectMocks
//    private UserProfileController userProfileController;
//
//    @Test
//    void getMyProfile_returnsCurrentUsersProfile() {
//        UUID userId = UUID.randomUUID();
//        Users currentUser = currentUser(userId);
//        UserProfileResponseDTO profile = profileResponse(userId);
//        when(currentUserUtil.getCurrentUser()).thenReturn(currentUser);
//        when(userService.getMyProfile(userId)).thenReturn(profile);
//
//        ResponseEntity<BaseResponse<UserProfileResponseDTO>> response =
//                userProfileController.getMyProfile();
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(response.getBody()).satisfies(body -> {
//            assertThat(body.getStatus()).isEqualTo(200);
//            assertThat(body.getMessage()).isEqualTo("Profile fetched successfully");
//            assertThat(body.getData()).isSameAs(profile);
//        });
//        verify(userService).getMyProfile(userId);
//    }
//
//    @Test
//    void getMyProfile_returnsBadRequestWhenCurrentUserCannotBeResolved() {
//        when(currentUserUtil.getCurrentUser()).thenThrow(new RuntimeException("User is not authenticated"));
//
//        ResponseEntity<BaseResponse<UserProfileResponseDTO>> response =
//                userProfileController.getMyProfile();
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
//        assertThat(response.getBody()).satisfies(body -> {
//            assertThat(body.getStatus()).isEqualTo(400);
//            assertThat(body.getMessage()).isEqualTo("User is not authenticated");
//            assertThat(body.getData()).isNull();
//        });
//    }
//
//    @Test
//    void getMyProfile_returnsBadRequestWhenProfileServiceFails() {
//        UUID userId = UUID.randomUUID();
//        when(currentUserUtil.getCurrentUser()).thenReturn(currentUser(userId));
//        when(userService.getMyProfile(userId)).thenThrow(new RuntimeException("User not found"));
//
//        ResponseEntity<BaseResponse<UserProfileResponseDTO>> response =
//                userProfileController.getMyProfile();
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
//        assertThat(response.getBody().getMessage()).isEqualTo("User not found");
//    }
//
//    @Test
//    void updateMyProfile_usesIdFromCurrentUserRatherThanRequestData() {
//        UUID userId = UUID.randomUUID();
//        Users currentUser = currentUser(userId);
//        UserProfileRequestDTO request = profileRequest();
//        UserProfileResponseDTO updatedProfile = profileResponse(userId);
//        updatedProfile.setFirstName("Updated");
//        when(currentUserUtil.getCurrentUser()).thenReturn(currentUser);
//        when(userService.updateMyProfile(userId, request)).thenReturn(updatedProfile);
//
//        ResponseEntity<BaseResponse<UserProfileResponseDTO>> response =
//                userProfileController.updateMyProfile(request);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(response.getBody()).satisfies(body -> {
//            assertThat(body.getStatus()).isEqualTo(200);
//            assertThat(body.getMessage()).isEqualTo("Profile updated successfully");
//            assertThat(body.getData()).isSameAs(updatedProfile);
//        });
//        verify(userService).updateMyProfile(userId, request);
//    }
//
//    @Test
//    void updateMyProfile_returnsBadRequestWhenEmailIsAlreadyUsed() {
//        UUID userId = UUID.randomUUID();
//        UserProfileRequestDTO request = profileRequest();
//        when(currentUserUtil.getCurrentUser()).thenReturn(currentUser(userId));
//        when(userService.updateMyProfile(userId, request))
//                .thenThrow(new RuntimeException("Email is already in use by another account!"));
//
//        ResponseEntity<BaseResponse<UserProfileResponseDTO>> response =
//                userProfileController.updateMyProfile(request);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
//        assertThat(response.getBody()).satisfies(body -> {
//            assertThat(body.getStatus()).isEqualTo(400);
//            assertThat(body.getMessage()).contains("Email is already in use");
//            assertThat(body.getData()).isNull();
//        });
//    }
//
//    @Test
//    void updateMyProfile_returnsBadRequestWhenCurrentUserIsUnavailable() {
//        UserProfileRequestDTO request = profileRequest();
//        when(currentUserUtil.getCurrentUser()).thenThrow(new RuntimeException("User is not authenticated"));
//
//        ResponseEntity<BaseResponse<UserProfileResponseDTO>> response =
//                userProfileController.updateMyProfile(request);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
//        assertThat(response.getBody().getMessage()).isEqualTo("User is not authenticated");
//    }
//
//    private static Users currentUser(UUID id) {
//        Users user = new Users();
//        user.setId(id);
//        user.setEmail("employee@example.com");
//        return user;
//    }
//
//    private static UserProfileResponseDTO profileResponse(UUID userId) {
//        return UserProfileResponseDTO.builder()
//                .id(userId)
//                .firstName("Lan")
//                .lastName("Nguyen")
//                .email("employee@example.com")
//                .numberPhone("0901234567")
//                .role("Employee")
//                .status("ACTIVE")
//                .build();
//    }
//
//    private static UserProfileRequestDTO profileRequest() {
//        UserProfileRequestDTO request = new UserProfileRequestDTO();
//        request.setFirstName("Updated");
//        request.setLastName("Profile");
//        request.setEmail("employee@example.com");
//        request.setNumberPhone("0987654321");
//        return request;
//    }
//}
