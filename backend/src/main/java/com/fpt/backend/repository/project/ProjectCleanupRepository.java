package com.fpt.backend.repository.project;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProjectCleanupRepository {
    private final EntityManager entityManager;

    // Xóa các bản ghi phụ thuộc còn lại trước khi xóa dự án.
    public void deleteProjectRecords(UUID projectId) {
        executeDelete(
                "DELETE FROM Approvals approval WHERE approval.proposal.id IN "
                        + "(SELECT proposal.id FROM Proposals proposal WHERE proposal.project.id = :projectId)",
                projectId
        );
        executeDelete(
                "DELETE FROM Proposals proposal WHERE proposal.project.id = :projectId",
                projectId
        );
        executeDelete(
                "DELETE FROM ActivityLog log WHERE log.project.id = :projectId",
                projectId
        );
        executeDelete(
                "DELETE FROM Workflow workflow WHERE workflow.project.id = :projectId",
                projectId
        );
    }

    // Thực thi một câu lệnh xóa JPQL với mã dự án được truyền vào.
    private void executeDelete(String query, UUID projectId) {
        entityManager.createQuery(query)
                .setParameter("projectId", projectId)
                .executeUpdate();
    }
}
