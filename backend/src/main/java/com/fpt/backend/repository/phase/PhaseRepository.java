package com.fpt.backend.repository.phase;

import com.fpt.backend.entity.Timeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PhaseRepository extends JpaRepository<Timeline, UUID> {
    // Lấy danh sách mã dự án hiện đang có phase.
    @Query("""
            SELECT DISTINCT phase.project.id
            FROM Timeline phase
            """)
    List<UUID> findProjectIds();

    // Lấy các phase của một dự án kèm thông tin dự án.
    @Query("""
            SELECT phase
            FROM Timeline phase
            JOIN FETCH phase.project project
            WHERE project.id = :projectId
            ORDER BY phase.startDate, phase.id
            """)
    List<Timeline> findByProjectId(@Param("projectId") UUID projectId);

    // Lấy chi tiết một phase kèm thông tin dự án.
    @Query("""
            SELECT phase
            FROM Timeline phase
            JOIN FETCH phase.project project
            WHERE phase.id = :phaseId
            """)
    Optional<Timeline> findDetailById(@Param("phaseId") UUID phaseId);

    // Xóa toàn bộ phase thuộc một dự án.
    @Modifying
    @Query("DELETE FROM Timeline phase WHERE phase.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") UUID projectId);
}
