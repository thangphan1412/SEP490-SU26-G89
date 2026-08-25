package com.fpt.backend.repository.contract;

import com.fpt.backend.entity.ContractWorkflowStepInstance;
import com.fpt.backend.enums.ContractWorkflowStepState;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
import org.springframework.data.jpa.repository.EntityGraph;
=======
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
>>>>>>> 7d6eb51fe9c660b46d1a1bc0200bcbbc73cf5f51
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

    @EntityGraph(attributePaths = {"assignedUser", "stepDefinition"})
    List<ContractWorkflowStepInstance>
    findByContractIdInOrderByStepOrderAsc(List<UUID> contractIds);

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

    @Query("""
        select distinct step.contract.project.id
        from ContractWorkflowStepInstance step
        where step.assignedUser.id = :userId
        """)
    List<UUID> findDistinctProjectIdsByAssignedUserId(
            @Param("userId") UUID userId
    );
}
