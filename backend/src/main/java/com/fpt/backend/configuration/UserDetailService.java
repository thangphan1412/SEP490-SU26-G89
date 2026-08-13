package com.fpt.backend.configuration;

import com.fpt.backend.entity.Users;
import com.fpt.backend.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserDetailService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

//    @Autowired
//    private PasswordEncoder passwordEncoder;
//    private MyUserDetail myUserDetail;
@Override
public UserDetails loadUserByUsername(String email)
        throws UsernameNotFoundException {

    System.out.println("chay toi day");
    System.out.println("EMAIL LOGIN = " + email);

    try {

        Optional<Users> optionalUser =
                userRepository.findByEmail(email);

        System.out.println("QUERY DA CHAY XONG");
        System.out.println("OPTIONAL = " + optionalUser.isPresent());

        if (optionalUser.isEmpty()) {
            System.out.println("KHONG TIM THAY USER");
            throw new UsernameNotFoundException(email);
        }

        Users users = optionalUser.get();

        System.out.println("========== LOGIN ==========");
        System.out.println("EMAIL: " + users.getEmail());
        System.out.println("PASSWORD HASH: " + users.getPassword());
        System.out.println("ROLES: " + users.getUserRoles().size());

        users.getUserRoles().forEach(userRole ->
                System.out.println(
                        "ROLE: " + userRole.getRole().getRoleName()
                )
        );

        return new MyUserDetail(users);

    } catch (Exception e) {

        System.out.println("========== FIND USER ERROR ==========");
        System.out.println("TYPE: " + e.getClass().getName());
        System.out.println("MESSAGE: " + e.getMessage());

        e.printStackTrace();

        throw e;
    }
}
}
