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
    boolean existsByProjectCodeIgnoreCase(String projectCode);

    boolean existsByProjectCodeIgnoreCaseAndIdNot(String projectCode, UUID id);

    Page<Projects> findByProjectStatusIgnoreCase(String projectStatus, Pageable pageable);

    List<Projects> findAllByProjectStatusIgnoreCase(String projectStatus);

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
