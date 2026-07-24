package com.fpt.backend.repository.phase;

import com.fpt.backend.entity.Deliverable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PhaseDeliverableRepository extends JpaRepository<Deliverable, UUID> {
    @Query("""
            SELECT deliverable
            FROM Deliverable deliverable
            WHERE deliverable.timeline.id = :phaseId
            ORDER BY deliverable.dueDate, deliverable.id
            """)
    List<Deliverable> findByPhaseId(@Param("phaseId") UUID phaseId);
}
