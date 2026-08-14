package com.fpt.backend.controller.authencationController;

import com.fpt.backend.configuration.JWTService;
import com.fpt.backend.configuration.MyUserDetail;
import com.fpt.backend.constant.ApiConstant;
import com.fpt.backend.dto.request.authentication.AuthenticateRequest;
import com.fpt.backend.dto.request.authentication.ChangePasswordRequest;
import com.fpt.backend.dto.request.authentication.ForgotPasswordRequest;
import com.fpt.backend.dto.request.authentication.ResetPasswordRequest;
import com.fpt.backend.dto.response.authentication.AuthenticateResponse;
import com.fpt.backend.entity.Users;
import com.fpt.backend.service.impl.user.UserServiceImpl;
import com.fpt.backend.util.BaseResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping(ApiConstant.API)
public class AuthenticateController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JWTService jwtService;
    @Autowired
    private UserServiceImpl userServiceImpl;
    @PostMapping(ApiConstant.Authentication.LOGIN)
    public ResponseEntity<BaseResponse<AuthenticateResponse>> authenticateUser(@RequestBody AuthenticateRequest authenticateRequest)  {
        try{

            Authentication authenticate =  authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authenticateRequest.getEmail(),
                    authenticateRequest.getPassword()
            ));
            System.out.println("password:"+ authenticateRequest.getPassword());
            MyUserDetail myUsersDetail = (MyUserDetail) authenticate.getPrincipal();
            Users users =  myUsersDetail.getUsers();

//            // ==============================================================
            // --- 1. TẤM KHIÊN CHẶN TÀI KHOẢN BỊ KHÓA (INACTIVE) ---
//            if (!"ACTIVE".equalsIgnoreCase(users.getStatus().name())) {
//                throw new RuntimeException("Tài khoản của bạn đã bị khóa hoặc chưa được kích hoạt. Vui lòng liên hệ Admin.");
//            }
//
//            // --- 2. TẤM KHIÊN CHẶN ĐĂNG NHẬP SỚM (PRE-ONBOARDING) ---
//            if (users.getStartDate() != null && !users.getStartDate().isEmpty()) {
//                LocalDate startDate = LocalDate.parse(users.getStartDate());
//                LocalDate today = LocalDate.now();
//
//                // Nếu hôm nay (today) diễn ra TRƯỚC ngày bắt đầu (startDate)
//                if (today.isBefore(startDate)) {
//                    throw new RuntimeException("Truy cập bị từ chối: Tài khoản của bạn sẽ được kích hoạt vào ngày " + users.getStartDate());
//                }
//            }
            // ==============================================================

            // --- THÊM ĐÚNG 2 DÒNG NÀY VÀO ĐÂY NHÉ ---
            users.setLastActive(java.time.LocalDateTime.now());
            userServiceImpl.save(users); // Lưu thời gian đăng nhập xuống Database
//            // ----------------------------------------

            var token  = jwtService.generateToken(myUsersDetail);
            System.out.println(">>> Login controller called");
            System.out.println(token);
            AuthenticateResponse authenticateResponse = new AuthenticateResponse();
            authenticateResponse.setToken(token);
            authenticateResponse.setRole(users.getUserRoles().stream().findFirst().get().getRole().getRoleName());
            authenticateResponse.setFullName(users.getFirstName()+" "+users.getLastName());

//            // --- THÊM ĐOẠN CODE NÀY ---
//            if (users.getDepartment() != null) {
//                authenticateResponse.setDepartmentName(users.getDepartment().getDepartmentName());
//            } else {
//                authenticateResponse.setDepartmentName(""); // Đề phòng user chưa có phòng ban
//            }
//            // ---------------------------------------

            BaseResponse<AuthenticateResponse> response = new BaseResponse<>(
                    HttpStatus.CREATED.value(),
                    "Login susscessed",
                    authenticateResponse
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping(ApiConstant.Authentication.FORGOT)
    public ResponseEntity<BaseResponse<?>> forGotPassword(@RequestBody ForgotPasswordRequest  forgotPasswordRequest)  {
        userServiceImpl.forgotPassword(forgotPasswordRequest.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(new BaseResponse<>());
    }
    @PostMapping(ApiConstant.Authentication.RESET_PASSWORD)
    public ResponseEntity<BaseResponse<?>> resetPasswrod(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest)  {
        userServiceImpl.resetPassword(resetPasswordRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(new BaseResponse<>());
    }

    @PostMapping(ApiConstant.Authentication.CHANGE_PASSWORD)
    public ResponseEntity<BaseResponse<?>> resetPassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest)  {
        userServiceImpl.changePassword(changePasswordRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(new BaseResponse<>());
    }
}
