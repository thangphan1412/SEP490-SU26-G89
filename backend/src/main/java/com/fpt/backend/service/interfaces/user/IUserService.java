package com.fpt.backend.service.interfaces.user;

import com.fpt.backend.dto.request.authentication.RegisterRequest;
import com.fpt.backend.dto.response.authentication.RegisterResponse;
import com.fpt.backend.entity.Users;

public interface IUserService {

    Boolean existsByEmail(String email);
    void save(Users user);

    RegisterResponse create(RegisterRequest registerRequest);
}
