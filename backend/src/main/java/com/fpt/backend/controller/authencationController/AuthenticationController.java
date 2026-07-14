package com.fpt.backend.controller.authencationController;

import com.fpt.backend.dto.request.authentication.RegisterRequest;
import com.fpt.backend.dto.response.Authentication.RegisterResponse;
import com.fpt.backend.entity.Users;
import com.fpt.backend.repository.user.UserRepository;
import com.fpt.backend.service.impl.user.UserServiceImpl;
import com.fpt.backend.util.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthenticationController {
    @Autowired
    private UserServiceImpl userService;
    @PostMapping("/register")
    public ResponseEntity<BaseResponse<RegisterResponse>> registerUser(@RequestBody RegisterRequest registerRequest) {
        RegisterResponse registerResponse = userService.create(registerRequest);
        BaseResponse<RegisterResponse> response = new BaseResponse<>(
                HttpStatus.CREATED.value(),
                "Create successful news",
                registerResponse
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
