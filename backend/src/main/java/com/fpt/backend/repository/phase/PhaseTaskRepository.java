package com.fpt.backend.repository.phase;

import com.fpt.backend.entity.TimelineTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PhaseTaskRepository extends JpaRepository<TimelineTask, UUID> {
    // Lấy toàn bộ task của một phase kèm người được giao.
    @Query("""
            SELECT task
            FROM TimelineTask task
            LEFT JOIN FETCH task.assignedTo
            WHERE task.timeline.id = :phaseId
            ORDER BY task.startDate, task.id
            """)
    List<TimelineTask> findByPhaseId(@Param("phaseId") UUID phaseId);

    // Lấy các task của một phase được giao cho người dùng chỉ định.
    @Query("""
            SELECT task
            FROM TimelineTask task
            LEFT JOIN FETCH task.assignedTo assignedUser
            WHERE task.timeline.id = :phaseId
                AND assignedUser.id = :userId
            ORDER BY task.startDate, task.id
            """)
    List<TimelineTask> findByPhaseIdAndAssignedUserId(
            @Param("phaseId") UUID phaseId,
            @Param("userId") UUID userId
    );

    // Đếm tổng số task thuộc một phase.
    @Query("SELECT COUNT(task) FROM TimelineTask task WHERE task.timeline.id = :phaseId")
    long countByPhaseId(@Param("phaseId") UUID phaseId);

    // Đếm số task đã hoàn thành thuộc một phase.
    @Query("""
            SELECT COUNT(task)
            FROM TimelineTask task
            WHERE task.timeline.id = :phaseId
              AND UPPER(TRIM(task.status)) = 'DONE'
            """)
    long countDoneByPhaseId(@Param("phaseId") UUID phaseId);

    // Xóa toàn bộ task thuộc các phase của một dự án.
    @Modifying
    @Query("""
            DELETE FROM TimelineTask task
            WHERE task.timeline.id IN (
                SELECT phase.id
                FROM Timeline phase
                WHERE phase.project.id = :projectId
            )
            """)
    void deleteByProjectId(@Param("projectId") UUID projectId);
}
