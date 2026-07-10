package com.fpt.backend.repository.permission;

import com.fpt.backend.entity.Permissions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permissions, Integer> {
    boolean existsByPermissionCodeIgnoreCase(String permissionCode);

    boolean existsByPermissionCodeIgnoreCaseAndIdNot(String permissionCode, int id);

    Page<Permissions> findByProject_Id(int projectId, Pageable pageable);

    @Query("""
            SELECT permission
            FROM Permissions permission
            LEFT JOIN permission.project project
            WHERE (
                LOWER(COALESCE(permission.permissionName, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(permission.permissionCode, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(permission.permissionModule, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(project.projectName, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(project.projectCode, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
            )
            AND (:projectId IS NULL OR project.id = :projectId)
            """)
    Page<Permissions> searchPermissions(
            @Param("search") String search,
            @Param("projectId") Integer projectId,
            Pageable pageable
    );
}
