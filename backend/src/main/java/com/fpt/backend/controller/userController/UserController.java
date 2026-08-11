package com.fpt.backend.controller.userController;

import com.fpt.backend.dto.request.user.UserFilterRequestDTO;
import com.fpt.backend.dto.request.user.UserCreateRequestDTO;
import com.fpt.backend.dto.request.user.UserUpdateRequestDTO;
import com.fpt.backend.dto.response.user.UserResponseDTO;
import com.fpt.backend.service.interfaces.user.IUserService;
import com.fpt.backend.util.BaseResponse;
import jakarta.validation.Valid; // BẮT BUỘC IMPORT CÁI NÀY
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private IUserService userService;

    // 1. GET ALL USERS (ListUser) - ĐÃ HỖ TRỢ PHÂN TRANG THỰC TẾ
    @PreAuthorize("hasAnyAuthority('CEO', 'Administrator', 'Accountant', 'HeadOfDepartment')")
    @GetMapping
    public ResponseEntity<BaseResponse<Page<UserResponseDTO>>> getAllUsers(
            @ModelAttribute UserFilterRequestDTO filterDTO, // Ép Spring map URL Params vào DTO
            @RequestParam(defaultValue = "0") int page,     // Lấy số trang từ FE (Mặc định 0)
            @RequestParam(defaultValue = "10") int size,    // Lấy số lượng từ FE (Mặc định 10)
            Principal principal
    ) {
        try {
            Page<UserResponseDTO> usersPage = userService.getAllUsersFiltered(filterDTO, principal.getName(), page, size);

            return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.<Page<UserResponseDTO>>builder()
                    .status(HttpStatus.OK.value())
                    .message("Successfully fetched users")
                    .data(usersPage)
                    .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BaseResponse.<Page<UserResponseDTO>>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Lỗi xử lý: " + e.getMessage())
                    .build()
            );
        }
    }

    // 2. GET USER BY ID (ViewUser)
    @PreAuthorize("hasAnyAuthority('CEO', 'Administrator', 'Accountant', 'HeadOfDepartment')")
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<UserResponseDTO>> getUserById(@PathVariable UUID id) {
        try {
            UserResponseDTO user = userService.getUserById(id);
            return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.<UserResponseDTO>builder()
                    .status(HttpStatus.OK.value())
                    .message("User found")
                    .data(user)
                    .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(BaseResponse.<UserResponseDTO>builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message(e.getMessage())
                    .build()
            );
        }
    }

    // 3. CREATE USER (CreateUser) - ĐÃ BẬT @Valid
    @PreAuthorize("hasAnyAuthority('Accountant', 'HeadOfDepartment')")
    @PostMapping
    public ResponseEntity<BaseResponse<UserResponseDTO>> createUser(@Valid @RequestBody UserCreateRequestDTO request) {
        try {
            UserResponseDTO newUser = userService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.<UserResponseDTO>builder()
                    .status(HttpStatus.CREATED.value())
                    .message("User created successfully")
                    .data(newUser)
                    .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BaseResponse.<UserResponseDTO>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .build()
            );
        }
    }

    // 4. UPDATE USER (UpdateUser) - ĐÃ BẬT @Valid
    @PreAuthorize("hasAnyAuthority('Accountant', 'HeadOfDepartment')")
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<UserResponseDTO>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequestDTO request) {
        try {
            UserResponseDTO updatedUser = userService.updateUser(id, request);
            return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.<UserResponseDTO>builder()
                    .status(HttpStatus.OK.value())
                    .message("User updated successfully")
                    .data(updatedUser)
                    .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BaseResponse.<UserResponseDTO>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .build()
            );
        }
    }
}