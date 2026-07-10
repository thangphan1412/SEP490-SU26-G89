package com.fpt.backend.repository.project;

import com.fpt.backend.dto.response.project.ProjectEmployeeResponse;
import com.fpt.backend.entity.Projects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Projects, Integer> {
    boolean existsByProjectCodeIgnoreCase(String projectCode);

    Page<Projects> findByProjectStatusIgnoreCase(String projectStatus, Pageable pageable);

    @Query("""
            SELECT project
            FROM Projects project
            WHERE (
                LOWER(COALESCE(project.projectCode, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(project.projectName, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(project.projectDescription, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(project.projectCreatedBy, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
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

    @Query("""
            SELECT DISTINCT project.projectStatus
            FROM Projects project
            WHERE project.projectStatus IS NOT NULL
                AND TRIM(project.projectStatus) <> ''
            ORDER BY project.projectStatus
            """)
    List<String> findDistinctProjectStatuses();

    @Query("""
            SELECT new com.fpt.backend.dto.response.project.ProjectEmployeeResponse(
                u.id,
                u.email,
                u.firstName,
                u.lastName,
                u.role,
                u.status
            )
            FROM Users u
            ORDER BY u.firstName, u.lastName, u.email
            """)
    List<ProjectEmployeeResponse> findEmployeesForProjectSelection();
}
