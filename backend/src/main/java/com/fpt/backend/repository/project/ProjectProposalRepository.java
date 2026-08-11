package com.fpt.backend.repository.project;

import com.fpt.backend.entity.Proposals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectProposalRepository
        extends JpaRepository<Proposals, UUID> {

    @Query("""
            SELECT proposal
            FROM Proposals proposal
            WHERE proposal.project.id = :projectId
                AND proposal.proposalCode = :proposalCode
            """)
    Optional<Proposals> findProjectApprovalProposal(
            @Param("projectId") UUID projectId,
            @Param("proposalCode") String proposalCode
    );
}
