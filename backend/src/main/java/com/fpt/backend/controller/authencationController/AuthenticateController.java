package com.fpt.backend.controller.authencationController;

import com.fpt.backend.configuration.JWTService;
import com.fpt.backend.configuration.MyUserDetail;
import com.fpt.backend.constant.ApiConstant;
import com.fpt.backend.dto.request.authentication.AuthenticateRequest;
import com.fpt.backend.dto.response.authentication.AuthenticateResponse;
import com.fpt.backend.entity.Users;
import com.fpt.backend.util.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstant.API)
public class AuthenticateController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JWTService jwtService;
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
            var token  = jwtService.generateToken(myUsersDetail);
            System.out.println(">>> Login controller called");
            System.out.println(token);
            AuthenticateResponse authenticateResponse = new AuthenticateResponse();
            authenticateResponse.setToken(token);
            authenticateResponse.setRole(users.getRole());
            authenticateResponse.setFullName(users.getFirstName()+" "+users.getLastName());
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

    @PostMapping("/forgotPassword")
    public ResponseEntity<BaseResponse<?>> forGotPassword(){


        return ResponseEntity.status(HttpStatus.CREATED).body(new BaseResponse<>());
    }
}
