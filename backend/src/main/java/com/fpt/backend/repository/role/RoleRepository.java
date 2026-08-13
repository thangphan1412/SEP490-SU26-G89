package com.fpt.backend.repository.role;

import com.fpt.backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

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
            SELECT account.role AS roleValue, COUNT(account) AS userCount
            FROM Users account
            WHERE account.role IS NOT NULL AND TRIM(account.role) <> ''
            GROUP BY account.role
            """)
    List<SystemRoleUsageProjection> findSystemRoleUsage();

    @Query("""
            SELECT COUNT(account)
            FROM Users account
            WHERE LOWER(TRIM(account.role)) = LOWER(TRIM(:roleCode))
               OR LOWER(TRIM(account.role)) = LOWER(TRIM(:roleName))
            """)
    long countUsersReferencingRole(
            @Param("roleCode") String roleCode,
            @Param("roleName") String roleName
    );

    @Query("""
            SELECT role
            FROM Role role
            WHERE role.roleName IS NOT NULL
                AND TRIM(role.roleName) <> ''
            ORDER BY role.roleName, role.id
            """)
    List<Role> findAllForSelection();
}
