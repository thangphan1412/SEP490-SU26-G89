package com.fpt.backend.controller.userController;

import com.fpt.backend.constant.ApiConstant;
import com.fpt.backend.service.impl.user.UserServiceImpl;
import com.fpt.backend.util.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/get")
public class UserController {
    @Autowired
    private UserServiceImpl userService;

    @GetMapping("/getAll")
    public ResponseEntity<BaseResponse<?>> Test() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.builder()
                        .status(1)
                        .message("Securing Spring Boot using Spring Security and JWT")
                        .build()
                );
    }


}
