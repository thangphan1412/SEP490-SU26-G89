package com.fpt.backend.repository.user;

import com.fpt.backend.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Integer> { // Sửa UUID thành Integer
    Optional<Users> findByEmail(String email);
    Boolean existsByEmail(String email);
}