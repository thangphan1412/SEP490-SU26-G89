package com.fpt.backend.repository.signature;

import com.fpt.backend.entity.UserKeys;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserKeyRepository extends JpaRepository<UserKeys, UUID> {
}
