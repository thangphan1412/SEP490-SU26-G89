package com.fpt.backend.repository.project;

import com.fpt.backend.entity.UserRole;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface ProjectUserRoleRepository extends Repository<UserRole, UUID> {
    @Query("""
            SELECT userRole
            FROM UserRole userRole
            JOIN FETCH userRole.user user
            JOIN FETCH userRole.role role
            ORDER BY user.id, role.roleName, role.id
            """)
    List<UserRole> findAllWithUserAndRole();
}
