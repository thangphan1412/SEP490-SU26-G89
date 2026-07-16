package com.fpt.backend.controller.userController;

import com.fpt.backend.dto.request.userProfile.UserProfileRequestDTO;
import com.fpt.backend.dto.response.userProfile.UserProfileResponseDTO;
import com.fpt.backend.entity.Users;
import com.fpt.backend.service.interfaces.user.IUserService;
import com.fpt.backend.util.BaseResponse;
import com.fpt.backend.util.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile") // Tách ra route riêng biệt so với việc quản lý /api/users
public class UserProfileController {

    @Autowired
    private IUserService userService;

    @Autowired
    private CurrentUser currentUserUtil; // Gọi tiện ích lấy thông tin từ JWT

    // 1. Xem thông tin cá nhân
    @GetMapping
    public ResponseEntity<BaseResponse<UserProfileResponseDTO>> getMyProfile() {
        try {
            // Lấy user đang đăng nhập
            Users currentUser = currentUserUtil.getCurrentUser();

            UserProfileResponseDTO profile = userService.getMyProfile(currentUser.getId());

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(BaseResponse.<UserProfileResponseDTO>builder()
                            .status(HttpStatus.OK.value())
                            .message("Profile fetched successfully")
                            .data(profile)
                            .build()
                    );
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(BaseResponse.<UserProfileResponseDTO>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .build()
                    );
        }
    }

    // 2. Cập nhật thông tin cá nhân
    @PutMapping
    public ResponseEntity<BaseResponse<UserProfileResponseDTO>> updateMyProfile(@RequestBody UserProfileRequestDTO request) {
        try {
            // Lấy user đang đăng nhập để tránh việc user này truyền ID cập nhật cho user khác
            Users currentUser = currentUserUtil.getCurrentUser();

            UserProfileResponseDTO updatedProfile = userService.updateMyProfile(currentUser.getId(), request);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(BaseResponse.<UserProfileResponseDTO>builder()
                            .status(HttpStatus.OK.value())
                            .message("Profile updated successfully")
                            .data(updatedProfile)
                            .build()
                    );
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(BaseResponse.<UserProfileResponseDTO>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .build()
                    );
        }
    }
}