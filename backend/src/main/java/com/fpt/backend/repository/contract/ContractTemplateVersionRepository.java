package com.fpt.backend.repository.contract;

import com.fpt.backend.entity.ContractTemplateVersions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContractTemplateVersionRepository
        extends JpaRepository<ContractTemplateVersions, UUID> {

    List<ContractTemplateVersions> findByContractTemplateIdOrderByVersionNumberDesc(
            UUID contractTemplateId
    );

    @Query("""
            SELECT COALESCE(MAX(version.versionNumber), 0)
            FROM ContractTemplateVersions version
            WHERE version.contractTemplate.id = :templateId
            """)
    int findLatestVersionNumber(@Param("templateId") UUID templateId);
}
