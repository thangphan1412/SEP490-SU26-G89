package com.fpt.backend.repository.contract;

import com.fpt.backend.entity.ContractWorkflowStepInstance;
import com.fpt.backend.enums.ContractWorkflowStepState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractWorkflowStepInstanceRepository
        extends JpaRepository<ContractWorkflowStepInstance, UUID> {
    List<ContractWorkflowStepInstance> findByContractIdOrderByStepOrderAsc(
            UUID contractId
    );

    Optional<ContractWorkflowStepInstance>
    findFirstByContractIdAndStatusOrderByStepOrderAsc(
            UUID contractId,
            ContractWorkflowStepState status
    );

    boolean existsByContractIdAndAssignedUserId(
            UUID contractId,
            UUID assignedUserId
    );

    boolean existsByContractId(UUID contractId);

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM ContractWorkflowStepInstance step WHERE step.contract.id = :contractId")
    void deleteAllByContractId(@Param("contractId") UUID contractId);

    @Query("""
        select distinct step.contract.project.id
        from ContractWorkflowStepInstance step
        where step.assignedUser.id = :userId
        """)
    List<UUID> findDistinctProjectIdsByAssignedUserId(
            @Param("userId") UUID userId
    );
}
