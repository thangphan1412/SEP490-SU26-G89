package com.fpt.backend.repository.userRole;

import com.fpt.backend.entity.UserRole;
import com.fpt.backend.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
    Optional<UserRole> findByUser(Users user);
}
