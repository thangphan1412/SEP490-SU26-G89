package com.fpt.backend.repository.role;

import com.fpt.backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByRoleName(String roleName);

    Boolean existsByRoleCodeIgnoreCase(String roleCode);

    Boolean existsByRoleCodeIgnoreCaseAndIdNot(
            String roleCode,
            UUID id
    );

    @Query("""
            SELECT role
            FROM Role role
            WHERE (
                :search = ''
                OR LOWER(COALESCE(role.roleCode, ''))
                    LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(role.roleName, ''))
                    LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(role.roleDescription, ''))
                    LIKE CONCAT('%', CONCAT(:search, '%'))
            )
            ORDER BY role.createdAt DESC, role.id DESC
            """)
    List<Role> searchRoles(@Param("search") String search);

    @Query("""
            SELECT COUNT(userRole)
            FROM UserRole userRole
            WHERE userRole.role.id = :roleId
            """)
    long countAssignedUsers(@Param("roleId") UUID roleId);

    @Query("""
            SELECT role
            FROM Role role
            WHERE role.roleName IS NOT NULL
                AND TRIM(role.roleName) <> ''
            ORDER BY role.roleName, role.id
            """)
    List<Role> findAllForSelection();
}
