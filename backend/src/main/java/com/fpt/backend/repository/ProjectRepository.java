package com.fpt.backend.repository;

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
    @Query("""
            SELECT project
            FROM Projects project
            WHERE (
                :search = ''
                OR LOWER(COALESCE(project.projectCode, '')) LIKE LOWER(CONCAT('%', CONCAT(:search, '%')))
                OR LOWER(COALESCE(project.projectName, '')) LIKE LOWER(CONCAT('%', CONCAT(:search, '%')))
                OR LOWER(COALESCE(project.projectDescription, '')) LIKE LOWER(CONCAT('%', CONCAT(:search, '%')))
                OR LOWER(COALESCE(project.projectCreatedBy, '')) LIKE LOWER(CONCAT('%', CONCAT(:search, '%')))
            )
            AND (
                :status = ''
                OR LOWER(COALESCE(project.projectStatus, '')) = LOWER(:status)
            )
            """)
    Page<Projects> findProjectList(
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
    List<String> findDistinctStatuses();
}
