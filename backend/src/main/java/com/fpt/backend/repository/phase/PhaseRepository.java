package com.fpt.backend.repository.phase;

import com.fpt.backend.entity.Timeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhaseRepository extends JpaRepository<Timeline, Integer> {
    @Query("""
            SELECT phase
            FROM Timeline phase
            JOIN FETCH phase.project project
            WHERE project.id = :projectId
            ORDER BY phase.startDate, phase.id
            """)
    List<Timeline> findByProjectId(@Param("projectId") int projectId);

    @Query("""
            SELECT phase
            FROM Timeline phase
            JOIN FETCH phase.project project
            WHERE phase.id = :phaseId
            """)
    Optional<Timeline> findDetailById(@Param("phaseId") int phaseId);
}
