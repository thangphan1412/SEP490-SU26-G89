package com.fpt.backend.repository.permission;

import com.fpt.backend.entity.Permissions;
import com.fpt.backend.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permissions, UUID> {
    boolean existsByPermissionCodeIgnoreCase(String permissionCode);

    boolean existsByPermissionCodeIgnoreCaseAndIdNot(String permissionCode, UUID id);

    @Query("SELECT role FROM Role role ORDER BY role.roleName ASC")
    List<Role> findRolesForPermissionSelection();

    @Query("SELECT role FROM Role role WHERE role.id = :roleId")
    Optional<Role> findPermissionRoleById(@Param("roleId") UUID roleId);

    @Query("""
            SELECT permission
            FROM Permissions permission
            LEFT JOIN permission.project project
            LEFT JOIN permission.role role
            WHERE (
                LOWER(COALESCE(permission.permissionName, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(permission.permissionCode, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(permission.permissionModule, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(permission.permissionDescription, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(project.projectName, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(project.projectCode, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(role.roleName, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
            )
            AND (:projectId IS NULL OR project.id = :projectId)
            AND (:roleId IS NULL OR role.id = :roleId)
            AND (:status IS NULL OR permission.status = :status)
            """)
    Page<Permissions> searchPermissions(
            @Param("search") String search,
            @Param("projectId") UUID projectId,
            @Param("roleId") UUID roleId,
            @Param("status") Boolean status,
            Pageable pageable
    );
}
