package com.fpt.backend.controller.userController;

import com.fpt.backend.dto.request.user.UserFilterRequestDTO;
import com.fpt.backend.dto.request.user.UserRequestDTO;
import com.fpt.backend.dto.response.user.UserResponseDTO;
import com.fpt.backend.service.interfaces.user.IUserService;
import com.fpt.backend.util.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users") // Đổi tên mapping để chuẩn RESTful hơn
public class UserController {

    @Autowired
    private IUserService userService; // Khuyên dùng interface thay vì implementation (UserServiceImpl)

    // 1. GET ALL USERS (ListUser)
    @PreAuthorize("hasAnyAuthority('CEO', 'Administrator', 'HeadOfDepartment')")
    @GetMapping
    public ResponseEntity<BaseResponse<List<UserResponseDTO>>> getAllUsers(
            UserFilterRequestDTO filterDTO, // Spring tự động map ?keyword=...&role=... vào object này
            Principal principal
    ) {
        try {
            // Chỉ cần truyền 2 tham số: Object lọc và Username hiện tại
            List<UserResponseDTO> users = userService.getAllUsersFiltered(filterDTO, principal.getName());

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(BaseResponse.<List<UserResponseDTO>>builder()
                            .status(HttpStatus.OK.value())
                            .message("Successfully fetched users")
                            .data(users)
                            .build()
                    );
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(BaseResponse.<List<UserResponseDTO>>builder()
                            .status(HttpStatus.FORBIDDEN.value())
                            .message(e.getMessage())
                            .build()
                    );
        }
    }

    // 2. GET USER BY ID (ViewUser)
    @PreAuthorize("hasAnyAuthority('CEO', 'Administrator', 'HeadOfDepartment')")
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<UserResponseDTO>> getUserById(@PathVariable UUID id) {
        try {
            UserResponseDTO user = userService.getUserById(id);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(BaseResponse.<UserResponseDTO>builder()
                            .status(HttpStatus.OK.value())
                            .message("User found")
                            .data(user)
                            .build()
                    );
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.<UserResponseDTO>builder()
                            .status(HttpStatus.NOT_FOUND.value())
                            .message(e.getMessage())
                            .build()
                    );
        }
    }

    // 3. CREATE USER (CreateUser)
    @PreAuthorize("hasAnyAuthority('CEO', 'Administrator', 'HeadOfDepartment')")
    @PostMapping
    public ResponseEntity<BaseResponse<UserResponseDTO>> createUser(@RequestBody UserRequestDTO request) {
        try {
            UserResponseDTO newUser = userService.createUser(request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(BaseResponse.<UserResponseDTO>builder()
                            .status(HttpStatus.CREATED.value())
                            .message("User created successfully")
                            .data(newUser)
                            .build()
                    );
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(BaseResponse.<UserResponseDTO>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .build()
                    );
        }
    }

    // 4. UPDATE USER (UpdateUser)
    @PreAuthorize("hasAnyAuthority('CEO', 'Administrator', 'HeadOfDepartment')")
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<UserResponseDTO>> updateUser(
            @PathVariable UUID id,
            @RequestBody UserRequestDTO request) {
        try {
            UserResponseDTO updatedUser = userService.updateUser(id, request);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(BaseResponse.<UserResponseDTO>builder()
                            .status(HttpStatus.OK.value())
                            .message("User updated successfully")
                            .data(updatedUser)
                            .build()
                    );
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(BaseResponse.<UserResponseDTO>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .build()
                    );
        }
    }
}