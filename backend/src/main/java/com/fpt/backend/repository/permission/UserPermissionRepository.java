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
    @Query("""
            SELECT userPermission
            FROM UserPermission userPermission
            JOIN FETCH userPermission.user user
            JOIN FETCH userPermission.permission permission
            WHERE permission.project.id = :projectId
            ORDER BY user.id, permission.id
            """)
    List<UserPermission> findByProjectId(@Param("projectId") UUID projectId);

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
