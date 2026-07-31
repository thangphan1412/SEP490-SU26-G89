package com.fpt.backend.service.impl;

import com.fpt.backend.dto.request.contract.ContractListRequest;
import com.fpt.backend.dto.request.contract.ContractRequest;
import com.fpt.backend.dto.response.contract.ContractListResponse;
import com.fpt.backend.dto.response.contract.ContractProjectOptionResponse;
import com.fpt.backend.dto.response.contract.ContractResponse;
import com.fpt.backend.entity.ContractTemplateVersions;
import com.fpt.backend.entity.ContractTemplates;
import com.fpt.backend.entity.ContractTypes;
import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.exception.NotFoundException;
import com.fpt.backend.repository.contract.ContractRepository;
import com.fpt.backend.repository.contract.ContractTemplateRepository;
import com.fpt.backend.repository.contract.ContractTemplateVersionRepository;
import com.fpt.backend.repository.contract.ContractTypeRepository;
import com.fpt.backend.repository.project.ProjectRepository;
import com.fpt.backend.service.interfaces.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {
    private static final int PAGE_SIZE = 8;
    private static final String DATA_SOURCE = "DATABASE";
    private static final String DEFAULT_SORT_FIELD = "id";

    private static final Set<String> SORT_FIELDS = Set.of(
            "id",
            "contractNumber",
            "contractTitle",
            "contractStatus",
            "effectiveDate",
            "expirationDate",
            "contractCreateBy",
            "contractCreatedAt"
    );

    private final ContractRepository contractRepository;
    private final ProjectRepository projectRepository;
    private final ContractTypeRepository contractTypeRepository;
    private final ContractTemplateRepository contractTemplateRepository;
    private final ContractTemplateVersionRepository contractTemplateVersionRepository;

    @Override
    @Transactional(readOnly = true)
    public ContractListResponse getContracts(ContractListRequest request) {
        String search = normalize(request.search());
        String status = normalize(request.status());
        Pageable pageable = createPageable(request.page(), request.sortBy(), request.sortDirection());

        Page<Contracts> contracts = findContracts(search, status, pageable);

        return new ContractListResponse(
                DATA_SOURCE,
                contracts.map(this::toResponse).getContent(),
                contracts.getNumber(),
                contracts.getSize(),
                contracts.getTotalElements(),
                contracts.getTotalPages(),
                contracts.isFirst(),
                contracts.isLast(),
                contractRepository.findDistinctContractStatuses()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractProjectOptionResponse> getProjectOptions() {
        return projectRepository.findAll(Sort.by(
                        Sort.Direction.ASC,
                        "projectName"
                ))
                .stream()
                .map(project -> new ContractProjectOptionResponse(
                        project.getId(),
                        project.getProjectCode(),
                        project.getProjectName()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ContractResponse getContractById(UUID id) {
        return toResponse(findContract(id));
    }

    @Override
    @Transactional
    public ContractResponse createContract(ContractRequest request) {
        Contracts contract = new Contracts();
        applyRequest(contract, request);
        contract.setContractCreatedAt(
                request.contractCreatedAt() != null ? request.contractCreatedAt() : LocalDateTime.now()
        );

        return toResponse(contractRepository.save(contract));
    }

    @Override
    @Transactional
    public ContractResponse updateContract(UUID id, ContractRequest request) {
        Contracts contract = findContract(id);
        applyRequest(contract, request);
        if (request.contractCreatedAt() != null) {
            contract.setContractCreatedAt(request.contractCreatedAt());
        }

        return toResponse(contractRepository.save(contract));
    }

    @Override
    @Transactional
    public void deleteContract(UUID id) {
        Contracts contract = findContract(id);
        contractRepository.delete(contract);
    }

    private Page<Contracts> findContracts(String search, String status, Pageable pageable) {
        if (search.isBlank() && status.isBlank()) {
            return contractRepository.findAll(pageable);
        }

        if (search.isBlank()) {
            return contractRepository.findByContractStatusIgnoreCase(status, pageable);
        }

        return contractRepository.searchContracts(
                search.toLowerCase(Locale.ROOT),
                status.toLowerCase(Locale.ROOT),
                pageable
        );
    }

    private void applyRequest(Contracts contract, ContractRequest request) {
        validateRequest(request);

        Projects project = resolveProject(request.projectId());
        ContractTypes contractType = resolveContractType(request.contractTypeId());
        ContractTemplates template = resolveTemplate(request.contractTemplateId());
        validateTemplateBelongsToType(template, contractType);

        ContractTemplateVersions version = resolveVersion(
                request.contractTemplateVersionId()
        );
        validateVersionBelongsToTemplate(version, template);

        if (Boolean.TRUE.equals(request.saveAsTemplateVersion())) {
            version = createTemplateVersion(template, request);
        }

        String content = normalizeContent(request.contractContent());
        String layoutJson = normalizeContent(request.contractLayoutJson());

        if (version != null) {
            if (request.contractContent() == null) {
                content = version.getTemplateContent();
            }
            if (request.contractLayoutJson() == null) {
                layoutJson = version.getLayoutJson();
            }
        }

        contract.setContractNumber(request.contractNumber().trim());
        contract.setContractTitle(request.contractTitle().trim());
        contract.setContractStatus(
                isBlank(request.contractStatus())
                        ? "Draft"
                        : request.contractStatus().trim()
        );
        contract.setEffectiveDate(request.effectiveDate());
        contract.setExpirationDate(request.expirationDate());
        contract.setContractCreateBy(normalizeToNull(request.contractCreatedBy()));
        contract.setProject(project);
        contract.setContractType(contractType);
        contract.setContractTemplate(template);
        contract.setContractTemplateVersion(version);
        contract.setContractContent(content);
        contract.setContractLayoutJson(layoutJson);
    }

    private Projects resolveProject(UUID projectId) {
        if (projectId == null) {
            throw new BadHttpException("Project is required");
        }

        return projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found with id: " + projectId));
    }

    private ContractTypes resolveContractType(UUID contractTypeId) {
        if (contractTypeId == null) {
            throw new BadHttpException("Contract type is required");
        }

        return contractTypeRepository.findById(contractTypeId)
                .orElseThrow(() -> new NotFoundException(
                        "Contract type not found with id: " + contractTypeId
                ));
    }

    private ContractTemplates resolveTemplate(UUID contractTemplateId) {
        if (contractTemplateId == null) {
            return null;
        }

        return contractTemplateRepository.findById(contractTemplateId)
                .orElseThrow(() -> new NotFoundException(
                        "Contract template not found with id: " + contractTemplateId
                ));
    }

    private ContractTemplateVersions resolveVersion(UUID versionId) {
        if (versionId == null) {
            return null;
        }

        return contractTemplateVersionRepository.findById(versionId)
                .orElseThrow(() -> new NotFoundException(
                        "Contract template version not found with id: " + versionId
                ));
    }

    private void validateTemplateBelongsToType(
            ContractTemplates template,
            ContractTypes contractType
    ) {
        if (template == null) {
            return;
        }

        ContractTypes templateType = template.getContractType();
        if (templateType == null || !templateType.getId().equals(contractType.getId())) {
            throw new BadHttpException(
                    "The selected contract template does not belong to the selected contract type"
            );
        }
    }

    private void validateVersionBelongsToTemplate(
            ContractTemplateVersions version,
            ContractTemplates template
    ) {
        if (version == null) {
            return;
        }

        if (template == null
                || version.getContractTemplate() == null
                || !version.getContractTemplate().getId().equals(template.getId())) {
            throw new BadHttpException(
                    "The selected template version does not belong to the selected template"
            );
        }
    }

    private ContractTemplateVersions createTemplateVersion(
            ContractTemplates template,
            ContractRequest request
    ) {
        if (template == null) {
            throw new BadHttpException(
                    "Select a contract template before saving a reusable version"
            );
        }

        String content = normalizeContent(request.contractContent());
        if (isBlank(content)) {
            throw new BadHttpException(
                    "Contract content is required to save a reusable template version"
            );
        }

        int nextVersionNumber =
                contractTemplateVersionRepository.findLatestVersionNumber(template.getId()) + 1;
        ContractTemplateVersions version = new ContractTemplateVersions();
        version.setContractTemplate(template);
        version.setVersionNumber(nextVersionNumber);
        version.setVersionName(
                isBlank(request.templateVersionName())
                        ? "Version " + nextVersionNumber
                        : request.templateVersionName().trim()
        );
        version.setTemplateContent(content);
        version.setLayoutJson(normalizeContent(request.contractLayoutJson()));
        version.setChangeNote(normalizeToNull(request.templateVersionNote()));
        version.setCreatedBy(normalizeToNull(request.contractCreatedBy()));
        version.setCreatedAt(LocalDateTime.now());

        ContractTemplateVersions savedVersion =
                contractTemplateVersionRepository.save(version);
        template.setContractTemplateUpdateAt(LocalDateTime.now());
        contractTemplateRepository.save(template);
        return savedVersion;
    }

    private void validateRequest(ContractRequest request) {
        if (request == null) {
            throw new BadHttpException("Contract information is required");
        }

        if (isBlank(request.contractNumber())) {
            throw new BadHttpException("Contract number is required");
        }

        if (isBlank(request.contractTitle())) {
            throw new BadHttpException("Contract title is required");
        }

        if (request.effectiveDate() != null
                && request.expirationDate() != null
                && request.expirationDate().isBefore(request.effectiveDate())) {
            throw new BadHttpException(
                    "Expiration date must be on or after the effective date"
            );
        }
    }

    private Contracts findContract(UUID id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Contract not found with id: " + id));
    }

    private Pageable createPageable(int page, String sortBy, String sortDirection) {
        int validPage = Math.max(page, 0);
        String sortField = sortBy != null && SORT_FIELDS.contains(sortBy) ? sortBy : DEFAULT_SORT_FIELD;
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(validPage, PAGE_SIZE, Sort.by(direction, sortField));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private ContractResponse toResponse(Contracts contract) {
        Projects project = contract.getProject();
        ContractTypes contractType = contract.getContractType();
        ContractTemplates template = contract.getContractTemplate();
        ContractTemplateVersions version = contract.getContractTemplateVersion();

        return new ContractResponse(
                contract.getId(),
                project != null ? project.getId() : null,
                project != null ? project.getProjectName() : null,
                contractType != null ? contractType.getId() : null,
                contractType != null ? contractType.getContractTypeCode() : null,
                contractType != null ? contractType.getContractTypeName() : null,
                template != null ? template.getId() : null,
                template != null ? template.getContractTemplateName() : null,
                version != null ? version.getId() : null,
                version != null ? version.getVersionNumber() : null,
                version != null ? version.getVersionName() : null,
                contract.getContractNumber(),
                contract.getContractTitle(),
                contract.getContractStatus(),
                contract.getEffectiveDate(),
                contract.getExpirationDate(),
                contract.getContractCreateBy(),
                contract.getContractCreatedAt(),
                contract.getContractContent(),
                contract.getContractLayoutJson()
        );
    }

    private String normalizeContent(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
