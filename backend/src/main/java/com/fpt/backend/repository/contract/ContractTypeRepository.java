package com.fpt.backend.repository.contract;

import com.fpt.backend.entity.ContractTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface ContractTypeRepository extends JpaRepository<ContractTypes, UUID> {
    List<ContractTypes> findAllByOrderByContractTypeNameAsc();

    boolean existsByContractTypeCodeIgnoreCase(String contractTypeCode);

    boolean existsByContractTypeCodeIgnoreCaseAndIdNot(
            String contractTypeCode,
            UUID id
    );

    Optional<ContractTypes> findByContractTypeCodeIgnoreCase(String contractTypeCode);
}
