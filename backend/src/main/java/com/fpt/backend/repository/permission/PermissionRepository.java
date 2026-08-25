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
    // Kiểm tra mã quyền đã tồn tại mà không phân biệt chữ hoa chữ thường.
    boolean existsByPermissionCodeIgnoreCase(String permissionCode);

    // Kiểm tra mã quyền trùng với bản ghi khác khi cập nhật.
    boolean existsByPermissionCodeIgnoreCaseAndIdNot(String permissionCode, UUID id);

    // Lấy các quyền thuộc một dự án theo thứ tự định danh.
    @Query("""
            SELECT permission
            FROM Permissions permission
            WHERE permission.project.id = :projectId
            ORDER BY permission.id
            """)
    List<Permissions> findByProjectId(@Param("projectId") UUID projectId);

    // Tìm một quyền theo đồng thời mã quyền và mã dự án.
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

    // Tìm kiếm và phân trang quyền trong phạm vi dự án mà người dùng được phép xem.
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
            AND (
                :canViewAllProjects = true
                OR (
                    EXISTS (
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
                            AND assignedAction.actionCode = :requiredActionCode
                    )
                )
            )
            """)
    Page<Permissions> searchPermissions(
            @Param("search") String search,
            @Param("projectId") UUID projectId,
            @Param("status") Boolean status,
            @Param("currentUserId") UUID currentUserId,
            @Param("requiredActionCode") String requiredActionCode,
            @Param("canViewAllProjects") boolean canViewAllProjects,
            Pageable pageable
    );
}
