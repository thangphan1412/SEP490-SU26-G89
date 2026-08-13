package com.fpt.backend.repository.signature;

import com.fpt.backend.entity.UserKeys;
import com.fpt.backend.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserKeysRepository extends JpaRepository<UserKeys, UUID> {
    Optional<UserKeys> findByUser(Users user);
    Optional<UserKeys> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
}
