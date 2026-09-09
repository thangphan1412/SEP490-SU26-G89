package com.fpt.backend.repository.contract;

import com.fpt.backend.entity.Contracts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

import jakarta.persistence.LockModeType;

@Repository
public interface ContractRepository extends JpaRepository<Contracts, UUID> {
    boolean existsByContractNumberIgnoreCase(String contractNumber);

    boolean existsByContractNumberIgnoreCaseAndIdNot(
            String contractNumber,
            UUID id
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT contract FROM Contracts contract WHERE contract.id = :id")
    Optional<Contracts> findByIdForUpdate(@Param("id") UUID id);

    long countByContractTypeId(UUID contractTypeId);

    long countByContractTemplateId(UUID contractTemplateId);

    @Query("""
            SELECT contract
            FROM Contracts contract
            LEFT JOIN contract.project project
            LEFT JOIN contract.contractType contractType
            LEFT JOIN contract.contractTemplate contractTemplate
            LEFT JOIN contract.contractCreatedByUser creator
            WHERE project.id IN (:projectIds)
            AND (
                project.id IN (:fullScopeProjectIds)
                OR creator.id = :currentUserId
                OR EXISTS (
                    SELECT workflowStep.id
                    FROM ContractWorkflowStepInstance workflowStep
                    WHERE workflowStep.contract.id = contract.id
                        AND workflowStep.assignedUser.id = :currentUserId
                )
                OR (
                    contract.contractCreatedByUser IS NULL
                    AND LOWER(TRIM(COALESCE(contract.contractCreateBy, '')))
                        = :currentUserName
                )
            )
            AND (
                :search = ''
                OR LOWER(COALESCE(contract.contractNumber, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(contract.contractTitle, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(contract.contractCreateBy, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(project.projectName, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(contractType.contractTypeCode, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(contractType.contractTypeName, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(contractTemplate.contractTemplateName, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
            )
            AND (
                :status = ''
                OR LOWER(COALESCE(contract.contractStatus, '')) = :status
            )
            """)
    @EntityGraph(attributePaths = {
            "project",
            "contractType",
            "contractTemplate",
            "contractTemplateVersion",
            "timelineTask",
            "timelineTask.timeline",
            "workflowVersion",
            "previousContract",
            "contractCreatedByUser"
    })
    Page<Contracts> searchAccessibleContracts(
            @Param("projectIds") List<UUID> projectIds,
            @Param("fullScopeProjectIds") List<UUID> fullScopeProjectIds,
            @Param("currentUserId") UUID currentUserId,
            @Param("currentUserName") String currentUserName,
            @Param("search") String search,
            @Param("status") String status,
            Pageable pageable
    );

    @Query("""
            SELECT contract.id
            FROM Contracts contract
            WHERE UPPER(COALESCE(contract.contractStatus, '')) = UPPER(:activeStatus)
                AND contract.expirationDate IS NOT NULL
                AND contract.expirationDate < :today
            """)
    List<UUID> findExpiredActiveContractIds(
            @Param("activeStatus") String activeStatus,
            @Param("today") LocalDate today
    );

    @Modifying(flushAutomatically = true)
    @Query("""
            UPDATE Contracts contract
            SET contract.contractStatus = :endedStatus,
                contract.contractStatusUpdatedAt = :endedAt,
                contract.contractEndedAt = :endedAt,
                contract.contractCancellationReason = NULL
            WHERE contract.id = :contractId
                AND UPPER(COALESCE(contract.contractStatus, '')) = UPPER(:activeStatus)
                AND contract.expirationDate IS NOT NULL
                AND contract.expirationDate < :today
            """)
    int markExpiredContractEnded(
            @Param("contractId") UUID contractId,
            @Param("activeStatus") String activeStatus,
            @Param("endedStatus") String endedStatus,
            @Param("today") LocalDate today,
            @Param("endedAt") LocalDateTime endedAt
    );

    @Query("""
            SELECT contract.id
            FROM Contracts contract
            WHERE UPPER(COALESCE(contract.contractStatus, '')) = UPPER(:pendingStatus)
                AND contract.effectiveDate IS NOT NULL
                AND contract.effectiveDate <= :today
            """)
    List<UUID> findEffectivePendingContractIds(
            @Param("pendingStatus") String pendingStatus,
            @Param("today") LocalDate today
    );

    @Modifying(flushAutomatically = true)
    @Query("""
            UPDATE Contracts contract
            SET contract.contractStatus = :activeStatus,
                contract.contractStatusUpdatedAt = :activatedAt,
                contract.contractEndedAt = NULL,
                contract.contractCancellationReason = NULL
            WHERE contract.id = :contractId
                AND UPPER(COALESCE(contract.contractStatus, '')) = UPPER(:pendingStatus)
                AND contract.effectiveDate IS NOT NULL
                AND contract.effectiveDate <= :today
            """)
    int markEffectiveContractActive(
            @Param("contractId") UUID contractId,
            @Param("pendingStatus") String pendingStatus,
            @Param("activeStatus") String activeStatus,
            @Param("today") LocalDate today,
            @Param("activatedAt") LocalDateTime activatedAt
    );


    // Đếm theo trạng thái
    long countByContractStatus(String status);

    // Lấy thống kê Status cho biểu đồ
    @Query("SELECT c.contractStatus, COUNT(c) FROM Contracts c GROUP BY c.contractStatus")
    List<Object[]> countContractsByStatus();

    // Lấy các hợp đồng sắp hết hạn (Trong vòng 30 ngày tới)
    @Query("SELECT c FROM Contracts c WHERE c.contractStatus = 'ACTIVE' AND c.expirationDate BETWEEN :today AND :thirtyDaysLater ORDER BY c.expirationDate ASC")
    List<Contracts> findUpcomingExpirations(@Param("today") LocalDate today, @Param("thirtyDaysLater") LocalDate thirtyDaysLater, Pageable pageable);

    // Lấy số lượng hợp đồng nhóm theo Năm và Tháng
    @Query("SELECT YEAR(c.contractCreatedAt), MONTH(c.contractCreatedAt), COUNT(c) " +
            "FROM Contracts c " +
            "WHERE c.contractCreatedAt >= :startDate " +
            "GROUP BY YEAR(c.contractCreatedAt), MONTH(c.contractCreatedAt) " +
            "ORDER BY YEAR(c.contractCreatedAt) ASC, MONTH(c.contractCreatedAt) ASC")
    List<Object[]> countContractsByMonth(@Param("startDate") java.time.LocalDateTime startDate);

    // Lấy thống kê theo Loại hợp đồng (Dành cho màn Statistical)
    @Query("SELECT ct.contractTypeName, COUNT(c) FROM Contracts c LEFT JOIN c.contractType ct GROUP BY ct.contractTypeName")
    List<Object[]> countContractsByType();

    // Lấy danh sách hợp đồng Pending để tính toán thời gian chờ
    @Query("SELECT p.projectName, c.contractCreatedAt FROM Contracts c LEFT JOIN c.project p WHERE c.contractStatus IN ('PENDING_DIRECTOR_SIGNATURE', 'PENDING_PARTNER_SIGNATURE')")
    List<Object[]> getPendingSignatureDetails();

    // Đếm hợp đồng đã hết hạn dựa vào ngày
    @Query("SELECT COUNT(c) FROM Contracts c WHERE c.expirationDate < :today")
    long countExpiredContracts(@Param("today") java.time.LocalDate today);
}
