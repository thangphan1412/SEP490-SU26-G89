package com.fpt.backend.repository.contract;

import com.fpt.backend.entity.ContractAttributeValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContractAttributeValueRepository
        extends JpaRepository<ContractAttributeValues, UUID> {
    List<ContractAttributeValues> findByContractIdOrderByAttributeKeyAsc(UUID contractId);

    void deleteAllByContractId(UUID contractId);
}
