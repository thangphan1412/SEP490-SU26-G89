package com.fpt.backend.repository.contract;

import com.fpt.backend.entity.ContractWorkflowStepInstance;
import com.fpt.backend.enums.ContractWorkflowStepState;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
