package com.fpt.backend.repository.project;

import com.fpt.backend.entity.Approvals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectApprovalRepository
        extends JpaRepository<Approvals, UUID> {

    // Mỗi proposal có một bản ghi cho mỗi cấp; lấy cả người duyệt để kiểm tra phòng ban.
    Optional<Approvals> findByProposalIdAndApprovalLevelIgnoreCase(
            UUID proposalId,
            String approvalLevel
    );
}
