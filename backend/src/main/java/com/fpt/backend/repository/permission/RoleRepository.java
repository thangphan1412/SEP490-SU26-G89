package com.fpt.backend.repository.permission;

import com.fpt.backend.entity.Role;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends Repository<Role, UUID> {
    @Query("""
            SELECT role
            FROM Role role
            WHERE role.roleName IS NOT NULL
                AND TRIM(role.roleName) <> ''
            ORDER BY role.roleName, role.id
            """)
    List<Role> findAllForSelection();

    Optional<Role> findById(UUID id);
}
