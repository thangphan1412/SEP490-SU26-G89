package com.fpt.backend.repository.permission;

import com.fpt.backend.entity.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, UUID> {
    // Lấy toàn bộ liên kết người dùng và quyền của một dự án.
    @Query("""
            SELECT userPermission
            FROM UserPermission userPermission
            JOIN FETCH userPermission.user user
            JOIN FETCH userPermission.permission permission
            WHERE permission.project.id = :projectId
            ORDER BY user.id, permission.id
            """)
    List<UserPermission> findByProjectId(@Param("projectId") UUID projectId);

    // Lấy quyền đang hoạt động của người dùng trong một dự án cùng các action liên quan.
    @Query("""
            SELECT DISTINCT userPermission
            FROM UserPermission userPermission
            JOIN FETCH userPermission.permission permission
            LEFT JOIN FETCH permission.actions action
            WHERE userPermission.user.id = :userId
                AND permission.project.id = :projectId
                AND permission.status = true
            """)
    UserPermission findActiveByUserIdAndProjectId(
            @Param("userId") UUID userId,
            @Param("projectId") UUID projectId
    );

    // Lấy các dự án mà người dùng là thành viên và có action được yêu cầu.
    @Query("""
            SELECT DISTINCT permission.project.id
            FROM UserPermission userPermission
            JOIN userPermission.permission permission
            JOIN permission.actions action
            WHERE userPermission.user.id = :userId
                AND permission.status = true
                AND action.actionCode = :actionCode
                AND EXISTS (
                    SELECT member.id
                    FROM ProjectMember member
                    WHERE member.project.id = permission.project.id
                        AND member.user.id = :userId
                )
            ORDER BY permission.project.id
            """)
    List<UUID> findProjectIdsByUserAndAction(
            @Param("userId") UUID userId,
            @Param("actionCode") String actionCode
    );

    // Xóa mọi liên kết quyền người dùng thuộc một dự án.
    @Modifying
    @Query("""
            DELETE FROM UserPermission userPermission
            WHERE userPermission.permission.id IN (
                SELECT permission.id
                FROM Permissions permission
                WHERE permission.project.id = :projectId
            )
            """)
    void deleteByProjectId(@Param("projectId") UUID projectId);
}
