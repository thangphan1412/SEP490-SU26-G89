package com.fpt.backend.repository.phase;

import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.TimelineContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PhaseContractRepository extends JpaRepository<TimelineContract, UUID> {
    // Lấy các hợp đồng liên kết trực tiếp với một phase.
    @Query("""
            SELECT phaseContract
            FROM TimelineContract phaseContract
            JOIN FETCH phaseContract.contract
            WHERE phaseContract.timeline.id = :phaseId
            ORDER BY phaseContract.linkedAt, phaseContract.id
            """)
    List<TimelineContract> findByPhaseId(@Param("phaseId") UUID phaseId);

    // Lấy các hợp đồng liên kết thông qua task thuộc một phase.
    @Query("""
            SELECT contract
            FROM Contracts contract
            JOIN contract.timelineTask task
            WHERE task.timeline.id = :phaseId
            ORDER BY contract.contractCreatedAt, contract.id
            """)
    List<Contracts> findByTaskPhaseId(@Param("phaseId") UUID phaseId);

    // Xóa các liên kết phase-hợp đồng thuộc một dự án.
    @Modifying
    @Query("""
            DELETE FROM TimelineContract phaseContract
            WHERE phaseContract.timeline.id IN (
                SELECT phase.id
                FROM Timeline phase
                WHERE phase.project.id = :projectId
            )
            """)
    void deleteByProjectId(@Param("projectId") UUID projectId);
}
