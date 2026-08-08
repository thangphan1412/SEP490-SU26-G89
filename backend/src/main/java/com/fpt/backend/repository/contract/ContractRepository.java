package com.fpt.backend.repository.contract;

import com.fpt.backend.entity.Contracts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contracts, UUID> {
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
}
