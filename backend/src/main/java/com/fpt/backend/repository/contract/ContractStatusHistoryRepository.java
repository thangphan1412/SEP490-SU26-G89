package com.fpt.backend.repository.contract;

import com.fpt.backend.entity.ContractStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContractStatusHistoryRepository
        extends JpaRepository<ContractStatusHistory, UUID> {
    List<ContractStatusHistory> findByContractIdOrderByChangedAtDesc(UUID contractId);
}
