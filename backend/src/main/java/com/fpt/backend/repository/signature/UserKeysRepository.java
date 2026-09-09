package com.fpt.backend.repository.signature;

import com.fpt.backend.entity.UserKeys;
import com.fpt.backend.entity.Users;
import com.fpt.backend.enums.KeyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserKeysRepository extends JpaRepository<UserKeys, UUID> {
    Optional<UserKeys> findByUser(Users user);
    Optional<UserKeys> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
    @Query("""
      select count(uk) >0
      from UserKeys uk
          where uk.user.id = :userId 
              and uk.keyStatus = :keysStatus
    """)
    boolean existsAllByKeyStatus(@Param("userId") UUID id,@Param("keysStatus") KeyStatus keysStatus);
}
