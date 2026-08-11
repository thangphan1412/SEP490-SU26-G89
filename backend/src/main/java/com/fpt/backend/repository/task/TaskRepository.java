package com.fpt.backend.repository.task;

import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.TimelineTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<TimelineTask, UUID> {
    @Query("""
            SELECT task
            FROM TimelineTask task
            LEFT JOIN FETCH task.assignedTo
            WHERE task.timeline.id = :phaseId
            ORDER BY task.startDate, task.id
            """)
    List<TimelineTask> findByPhaseId(@Param("phaseId") UUID phaseId);

    @Query("""
            SELECT task
            FROM TimelineTask task
            JOIN FETCH task.assignedTo assignedUser
            WHERE task.timeline.id = :phaseId
                AND assignedUser.id = :userId
            ORDER BY task.startDate, task.id
            """)
    List<TimelineTask> findByPhaseIdAndAssignedUserId(
            @Param("phaseId") UUID phaseId,
            @Param("userId") UUID userId
    );

    @Query("""
            SELECT task
            FROM TimelineTask task
            JOIN FETCH task.timeline phase
            JOIN FETCH phase.project
            LEFT JOIN FETCH task.assignedTo
            WHERE task.id = :taskId
            """)
    Optional<TimelineTask> findDetailById(@Param("taskId") UUID taskId);

    @Query("""
            SELECT contract
            FROM Contracts contract
            WHERE contract.timelineTask.id = :taskId
            ORDER BY contract.contractCreatedAt, contract.id
            """)
    List<Contracts> findContractsByTaskId(@Param("taskId") UUID taskId);
}
