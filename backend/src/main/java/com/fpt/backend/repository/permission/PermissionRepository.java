package com.fpt.backend.repository.permission;

import com.fpt.backend.entity.Permissions;
import com.fpt.backend.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Query("""
            SELECT role
            FROM Role role
            WHERE role.roleName IS NOT NULL
                AND TRIM(role.roleName) <> ''
            ORDER BY role.roleName, role.id
            """)
    List<Role> findRolesForPermissionSelection();

    @Query("SELECT role FROM Role role WHERE role.id = :roleId")
    Optional<Role> findPermissionRoleById(@Param("roleId") UUID roleId);

    @Query("""
            SELECT permission
            FROM Permissions permission
            WHERE permission.project.id = :projectId
            ORDER BY permission.id
            """)
    List<Permissions> findByProjectId(@Param("projectId") UUID projectId);

    @Query("""
            SELECT permission
            FROM Permissions permission
            WHERE permission.id = :permissionId
                AND permission.project.id = :projectId
            """)
    Optional<Permissions> findByIdAndProjectId(
            @Param("permissionId") UUID permissionId,
            @Param("projectId") UUID projectId
    );

    @Modifying
    @Query("DELETE FROM Permissions permission WHERE permission.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") UUID projectId);

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
