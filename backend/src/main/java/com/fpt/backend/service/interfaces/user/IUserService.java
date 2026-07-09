package com.fpt.backend.service.interfaces.user;

import com.fpt.backend.entity.Users;

import java.util.Optional;

public interface IUserService {

    Boolean existsByEmail(String email);
    void save(Users user);
}
