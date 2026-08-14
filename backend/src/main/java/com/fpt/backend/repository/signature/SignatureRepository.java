package com.fpt.backend.repository.signature;

import com.fpt.backend.entity.Signature;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SignatureRepository extends JpaRepository<Signature, UUID> {
    @EntityGraph(attributePaths = {
            "signedBy",
            "electronicSignatures",
            "fileStorage",
            "workflowStepInstance"
    })
    List<Signature> findByContractIdOrderBySignedAtAsc(UUID contractId);

    boolean existsByContractIdAndWorkflowStepInstanceId(
            UUID contractId,
            UUID workflowStepInstanceId
    );
}
