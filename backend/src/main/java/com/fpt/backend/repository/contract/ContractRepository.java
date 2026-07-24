package com.fpt.backend.repository.contract;

import com.fpt.backend.entity.Contracts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contracts, UUID> {
    Page<Contracts> findByContractStatusIgnoreCase(String contractStatus, Pageable pageable);

    @Query("""
            SELECT contract
            FROM Contracts contract
            LEFT JOIN contract.project project
            WHERE (
                LOWER(COALESCE(contract.contractNumber, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(contract.contractTitle, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(contract.contractCreateBy, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(project.projectName, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
            )
            AND (
                :status = ''
                OR LOWER(COALESCE(contract.contractStatus, '')) = :status
            )
            """)
    Page<Contracts> searchContracts(
            @Param("search") String search,
            @Param("status") String status,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT contract.contractStatus
            FROM Contracts contract
            WHERE contract.contractStatus IS NOT NULL
                AND TRIM(contract.contractStatus) <> ''
            ORDER BY contract.contractStatus
            """)
    List<String> findDistinctContractStatuses();
}
