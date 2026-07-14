package com.fpt.backend.service.impl.user;

import com.fpt.backend.dto.request.authentication.RegisterRequest;
import com.fpt.backend.dto.response.authentication.RegisterResponse;
import com.fpt.backend.entity.Users;
import com.fpt.backend.repository.user.UserRepository;
import com.fpt.backend.service.interfaces.user.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void save(Users user) {
         userRepository.save(user);
    }

    @Override
    public RegisterResponse create(RegisterRequest registerRequest) {
        List<Users> users = userRepository.findAll();
        Users user = new Users();
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        userRepository.save(user);
        RegisterResponse registerResponse = new RegisterResponse();
        registerResponse.setId(user.getId());
        return registerResponse;
    }
}
