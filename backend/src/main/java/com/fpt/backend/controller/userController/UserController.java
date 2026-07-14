//package com.fpt.backend.controller.userController;
//
//import com.fpt.backend.dto.request.user.UserRequestDTO;
//import com.fpt.backend.dto.response.user.UserResponseDTO;
//import com.fpt.backend.service.interfaces.user.IUserService;
//import com.fpt.backend.util.BaseResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/users") // Đổi tên mapping để chuẩn RESTful hơn
//public class UserController {
//
//    @Autowired
//    private IUserService userService; // Khuyên dùng interface thay vì implementation (UserServiceImpl)
//
//    // 1. GET ALL USERS (ListUser)
//    @GetMapping
//    public ResponseEntity<BaseResponse<List<UserResponseDTO>>> getAllUsers() {
//        List<UserResponseDTO> users = userService.getAllUsers();
//        return ResponseEntity
//                .status(HttpStatus.OK)
//                .body(BaseResponse.<List<UserResponseDTO>>builder()
//                        .status(HttpStatus.OK.value())
//                        .message("Successfully fetched all users")
//                        .data(users) // Giả sử BaseResponse của bạn có field "data" kiểu T
//                        .build()
//                );
//    }
//
//    // 2. GET USER BY ID (ViewUser)
//    @GetMapping("/{id}")
//    public ResponseEntity<BaseResponse<UserResponseDTO>> getUserById(@PathVariable Integer id) {
//        try {
//            UserResponseDTO user = userService.getUserById(id);
//            return ResponseEntity
//                    .status(HttpStatus.OK)
//                    .body(BaseResponse.<UserResponseDTO>builder()
//                            .status(HttpStatus.OK.value())
//                            .message("User found")
//                            .data(user)
//                            .build()
//                    );
//        } catch (Exception e) {
//            return ResponseEntity
//                    .status(HttpStatus.NOT_FOUND)
//                    .body(BaseResponse.<UserResponseDTO>builder()
//                            .status(HttpStatus.NOT_FOUND.value())
//                            .message(e.getMessage())
//                            .build()
//                    );
//        }
//    }
//
//    // 3. CREATE USER (CreateUser)
//    @PostMapping
//    public ResponseEntity<BaseResponse<UserResponseDTO>> createUser(@RequestBody UserRequestDTO request) {
//        try {
//            UserResponseDTO newUser = userService.createUser(request);
//            return ResponseEntity
//                    .status(HttpStatus.CREATED)
//                    .body(BaseResponse.<UserResponseDTO>builder()
//                            .status(HttpStatus.CREATED.value())
//                            .message("User created successfully")
//                            .data(newUser)
//                            .build()
//                    );
//        } catch (Exception e) {
//            return ResponseEntity
//                    .status(HttpStatus.BAD_REQUEST)
//                    .body(BaseResponse.<UserResponseDTO>builder()
//                            .status(HttpStatus.BAD_REQUEST.value())
//                            .message(e.getMessage())
//                            .build()
//                    );
//        }
//    }
//
//    // 4. UPDATE USER (UpdateUser)
//    @PutMapping("/{id}")
//    public ResponseEntity<BaseResponse<UserResponseDTO>> updateUser(
//            @PathVariable Integer id,
//            @RequestBody UserRequestDTO request) {
//        try {
//            UserResponseDTO updatedUser = userService.updateUser(id, request);
//            return ResponseEntity
//                    .status(HttpStatus.OK)
//                    .body(BaseResponse.<UserResponseDTO>builder()
//                            .status(HttpStatus.OK.value())
//                            .message("User updated successfully")
//                            .data(updatedUser)
//                            .build()
//                    );
//        } catch (Exception e) {
//            return ResponseEntity
//                    .status(HttpStatus.BAD_REQUEST)
//                    .body(BaseResponse.<UserResponseDTO>builder()
//                            .status(HttpStatus.BAD_REQUEST.value())
//                            .message(e.getMessage())
//                            .build()
//                    );
//        }
//    }
//}