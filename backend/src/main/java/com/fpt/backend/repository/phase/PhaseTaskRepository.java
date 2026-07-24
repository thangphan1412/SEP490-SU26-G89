package com.fpt.backend.repository.phase;

import com.fpt.backend.entity.TimelineTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhaseTaskRepository extends JpaRepository<TimelineTask, Integer> {
    @Query("""
            SELECT task
            FROM TimelineTask task
            LEFT JOIN FETCH task.assignedTo
            WHERE task.timeline.id = :phaseId
            ORDER BY task.startDate, task.id
            """)
    List<TimelineTask> findByPhaseId(@Param("phaseId") int phaseId);
}
