package com.fpt.backend.repository.project;

import com.fpt.backend.entity.Approvals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProjectApprovalRepository
        extends JpaRepository<Approvals, UUID> {

    // Đếm số lượt phê duyệt thành công tại một cấp duyệt của proposal.
    @Query("""
            SELECT COUNT(approval)
            FROM Approvals approval
            WHERE approval.proposal.id = :proposalId
                AND UPPER(COALESCE(approval.approvalLevel, '')) = UPPER(:approvalLevel)
                AND UPPER(COALESCE(approval.approvalStatus, '')) = 'APPROVED'
            """)
    long countApprovedLevel(
            @Param("proposalId") UUID proposalId,
            @Param("approvalLevel") String approvalLevel
    );
}
