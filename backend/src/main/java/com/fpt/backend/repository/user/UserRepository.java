package com.fpt.backend.repository.user;

import com.fpt.backend.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<Users, UUID> { // Sửa UUID thành Integer
    Optional<Users> findByEmail(String email);
    Boolean existsByEmail(String email);
}