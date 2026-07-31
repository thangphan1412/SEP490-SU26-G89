package com.fpt.backend.repository.contract;

import com.fpt.backend.entity.ContractTemplates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContractTemplateRepository extends JpaRepository<ContractTemplates, UUID> {
    List<ContractTemplates> findAllByOrderByContractTemplateNameAsc();

    List<ContractTemplates> findByContractTypeIdOrderByContractTemplateNameAsc(
            UUID contractTypeId
    );

    boolean existsByContractTypeIdAndContractTemplateNameIgnoreCase(
            UUID contractTypeId,
            String contractTemplateName
    );

    boolean existsByContractTypeIdAndContractTemplateNameIgnoreCaseAndIdNot(
            UUID contractTypeId,
            String contractTemplateName,
            UUID id
    );

    long countByContractTypeId(UUID contractTypeId);
}
