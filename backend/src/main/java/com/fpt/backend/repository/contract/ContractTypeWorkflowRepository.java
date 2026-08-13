package com.fpt.backend.repository.contract;

import com.fpt.backend.entity.ContractTypeWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractTypeWorkflowRepository
        extends JpaRepository<ContractTypeWorkflow, UUID> {
    Optional<ContractTypeWorkflow>
    findFirstByContractTypeIdAndActiveTrueOrderByVersionNumberDesc(UUID contractTypeId);

    List<ContractTypeWorkflow> findByContractTypeIdOrderByVersionNumberDesc(
            UUID contractTypeId
    );

    @Query("""
            SELECT COALESCE(MAX(workflow.versionNumber), 0)
            FROM ContractTypeWorkflow workflow
            WHERE workflow.contractType.id = :contractTypeId
            """)
    int findLatestVersionNumber(@Param("contractTypeId") UUID contractTypeId);
}
