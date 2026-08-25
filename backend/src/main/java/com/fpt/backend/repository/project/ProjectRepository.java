package com.fpt.backend.repository.project;

import com.fpt.backend.entity.Projects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Projects, UUID> {
    // Kiểm tra mã dự án đã tồn tại mà không phân biệt chữ hoa chữ thường.
    boolean existsByProjectCodeIgnoreCase(String projectCode);

    // Kiểm tra mã dự án trùng với bản ghi khác khi cập nhật.
    boolean existsByProjectCodeIgnoreCaseAndIdNot(String projectCode, UUID id);

    // Lấy trang dự án theo trạng thái không phân biệt chữ hoa chữ thường.
    Page<Projects> findByProjectStatusIgnoreCase(String projectStatus, Pageable pageable);

    // Lấy toàn bộ dự án theo trạng thái không phân biệt chữ hoa chữ thường.
    List<Projects> findAllByProjectStatusIgnoreCase(String projectStatus);

    // Tìm kiếm dự án trên toàn hệ thống theo từ khóa và trạng thái.
    @Query("""
            SELECT project
            FROM Projects project
            JOIN project.projectCreatedBy creator
            WHERE (
                LOWER(COALESCE(project.projectCode, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(project.projectName, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(project.projectDescription, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(creator.firstName, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(creator.lastName, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(creator.email, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(
                    TRIM(
                        CONCAT(
                            COALESCE(creator.firstName, ''),
                            CONCAT(' ', COALESCE(creator.lastName, ''))
                        )
                    )
                ) LIKE CONCAT('%', CONCAT(:search, '%'))
            )
            AND (
                :status = ''
                OR LOWER(COALESCE(project.projectStatus, '')) = :status
            )
            """)
    Page<Projects> searchProjects(
            @Param("search") String search,
            @Param("status") String status,
            Pageable pageable
    );

    // Tìm kiếm dự án mà người dùng hiện tại là thành viên.
    @Query("""
            SELECT project
            FROM Projects project
            JOIN project.projectCreatedBy creator
            WHERE (
                :search = ''
                OR LOWER(COALESCE(project.projectCode, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(project.projectName, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(project.projectDescription, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(creator.firstName, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(creator.lastName, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(creator.email, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(
                    TRIM(
                        CONCAT(
                            COALESCE(creator.firstName, ''),
                            CONCAT(' ', COALESCE(creator.lastName, ''))
                        )
                    )
                ) LIKE CONCAT('%', CONCAT(:search, '%'))
            )
            AND (
                :status = ''
                OR LOWER(COALESCE(project.projectStatus, '')) = :status
            )
            AND EXISTS (
                SELECT member.id
                FROM ProjectMember member
                WHERE member.project.id = project.id
                    AND member.user.id = :userId
            )
            """)
    Page<Projects> searchViewableProjects(
            @Param("search") String search,
            @Param("status") String status,
            @Param("userId") UUID userId,
            Pageable pageable
    );

}
