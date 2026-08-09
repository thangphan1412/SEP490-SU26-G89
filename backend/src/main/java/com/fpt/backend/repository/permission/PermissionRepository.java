package com.fpt.backend.repository.permission;

import com.fpt.backend.entity.Permissions;
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

    @Query("""
            SELECT DISTINCT permission
            FROM Permissions permission
            LEFT JOIN permission.project project
            LEFT JOIN permission.actions action
            WHERE (
                LOWER(COALESCE(permission.permissionName, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(permission.permissionCode, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(permission.permissionDescription, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(project.projectName, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(project.projectCode, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(action.actionCode, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(action.actionName, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(action.resourceCode, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
            )
            AND (:projectId IS NULL OR project.id = :projectId)
            AND (:status IS NULL OR permission.status = :status)
            AND EXISTS (
                SELECT member.id
                FROM ProjectMember member
                WHERE member.project.id = project.id
                    AND member.user.id = :currentUserId
            )
            AND EXISTS (
                SELECT userPermission.id
                FROM UserPermission userPermission
                JOIN userPermission.permission assignedPermission
                JOIN assignedPermission.actions assignedAction
                WHERE userPermission.user.id = :currentUserId
                    AND assignedPermission.project.id = project.id
                    AND assignedPermission.status = true
                    AND assignedAction.status = true
                    AND UPPER(assignedAction.actionCode) = :requiredActionCode
            )
            """)
    Page<Permissions> searchPermissions(
            @Param("search") String search,
            @Param("projectId") UUID projectId,
            @Param("status") Boolean status,
            @Param("currentUserId") UUID currentUserId,
            @Param("requiredActionCode") String requiredActionCode,
            Pageable pageable
    );
}
