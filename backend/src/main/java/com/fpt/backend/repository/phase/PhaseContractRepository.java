package com.fpt.backend.repository.phase;

import com.fpt.backend.entity.TimelineContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PhaseContractRepository extends JpaRepository<TimelineContract, UUID> {
    @Query("""
            SELECT phaseContract
            FROM TimelineContract phaseContract
            JOIN FETCH phaseContract.contract
            WHERE phaseContract.timeline.id = :phaseId
            ORDER BY phaseContract.linkedAt, phaseContract.id
            """)
    List<TimelineContract> findByPhaseId(@Param("phaseId") UUID phaseId);
}
