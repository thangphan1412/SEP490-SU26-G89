package com.fpt.backend.service.impl.contract;

import com.fpt.backend.dto.request.contract.ContractTemplateRequest;
import com.fpt.backend.dto.request.contract.ContractTemplateLayout;
import com.fpt.backend.dto.request.contract.ContractTemplateVersionRequest;
import com.fpt.backend.dto.response.contract.ContractTemplateResponse;
import com.fpt.backend.dto.response.contract.ContractTemplateVersionResponse;
import com.fpt.backend.entity.ContractTemplateVersions;
import com.fpt.backend.entity.ContractTemplates;
import com.fpt.backend.entity.ContractTypes;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.exception.NotFoundException;
import com.fpt.backend.repository.contract.ContractRepository;
import com.fpt.backend.repository.contract.ContractTemplateRepository;
import com.fpt.backend.repository.contract.ContractTemplateVersionRepository;
import com.fpt.backend.repository.contract.ContractTypeRepository;
import com.fpt.backend.service.interfaces.contract.ContractTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractTemplateServiceImpl implements ContractTemplateService {
    private static final String DEFAULT_STATUS = "Active";

    private final ContractTemplateRepository contractTemplateRepository;
    private final ContractTemplateVersionRepository contractTemplateVersionRepository;
    private final ContractTypeRepository contractTypeRepository;
    private final ContractRepository contractRepository;
    private final ContractTemplateLayoutMapper layoutMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ContractTemplateResponse> getContractTemplates(UUID contractTypeId) {
        List<ContractTemplates> templates;

        if (contractTypeId == null) {
            templates = contractTemplateRepository.findAllByOrderByContractTemplateNameAsc();
        } else {
            findContractType(contractTypeId);
            templates = contractTemplateRepository
                    .findByContractTypeIdOrderByContractTemplateNameAsc(contractTypeId);
        }

        return templates.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ContractTemplateResponse getContractTemplateById(UUID id) {
        return toResponse(findContractTemplate(id));
    }

    @Override
    @Transactional
    public ContractTemplateResponse createContractTemplate(ContractTemplateRequest request) {
        validateTemplateRequest(request);
        ContractTypes contractType = findContractType(request.contractTypeId());
        String name = requireText(
                request.contractTemplateName(),
                "Contract template name is required"
        );

        if (contractTemplateRepository
                .existsByContractTypeIdAndContractTemplateNameIgnoreCase(
                        contractType.getId(),
                        name
                )) {
            throw new BadHttpException(
                    "A contract template with this name already exists in the selected type"
            );
        }

        LocalDateTime now = LocalDateTime.now();
        ContractTemplates template = new ContractTemplates();
        applyTemplateRequest(template, request, contractType);
        template.setContractTemplateCreateAt(now);
        template.setContractTemplateUpdateAt(now);

        return toResponse(contractTemplateRepository.save(template));
    }

    @Override
    @Transactional
    public ContractTemplateResponse updateContractTemplate(
            UUID id,
            ContractTemplateRequest request
    ) {
        validateTemplateRequest(request);
        ContractTemplates template = findContractTemplate(id);
        ContractTypes contractType = findContractType(request.contractTypeId());
        String name = requireText(
                request.contractTemplateName(),
                "Contract template name is required"
        );

        if (contractTemplateRepository
                .existsByContractTypeIdAndContractTemplateNameIgnoreCaseAndIdNot(
                        contractType.getId(),
                        name,
                        id
                )) {
            throw new BadHttpException(
                    "A contract template with this name already exists in the selected type"
            );
        }

        String originalCreator = template.getContractTemplateCreatedBy();
        applyTemplateRequest(template, request, contractType);
        if (isBlank(request.createdBy())) {
            template.setContractTemplateCreatedBy(originalCreator);
        }
        template.setContractTemplateUpdateAt(LocalDateTime.now());

        return toResponse(contractTemplateRepository.save(template));
    }

    @Override
    @Transactional
    public void deleteContractTemplate(UUID id) {
        ContractTemplates template = findContractTemplate(id);

        if (contractRepository.countByContractTemplateId(id) > 0) {
            throw new BadHttpException(
                    "Cannot delete a contract template that is used by contracts"
            );
        }

        contractTemplateRepository.delete(template);
    }

    @Override
    @Transactional
    public ContractTemplateVersionResponse createContractTemplateVersion(
            UUID contractTemplateId,
            ContractTemplateVersionRequest request
    ) {
        if (request == null) {
            throw new BadHttpException("Contract template version information is required");
        }

        ContractTemplates template = findContractTemplate(contractTemplateId);
        String content = requireText(
                request.templateContent(),
                "Template content is required"
        );
        ContractTemplateLayout layout = layoutMapper.normalize(
                request.pageCount(),
                request.positions(),
                request.layoutJson()
        );
        int nextVersionNumber =
                contractTemplateVersionRepository.findLatestVersionNumber(contractTemplateId) + 1;

        ContractTemplateVersions version = new ContractTemplateVersions();
        version.setContractTemplate(template);
        version.setVersionNumber(nextVersionNumber);
        version.setVersionName(
                isBlank(request.versionName())
                        ? "Version " + nextVersionNumber
                        : request.versionName().trim()
        );
        version.setTemplateContent(content);
        version.setChangeNote(normalizeToNull(request.changeNote()));
        version.setCreatedBy(normalizeToNull(request.createdBy()));
        version.setCreatedAt(LocalDateTime.now());
        layoutMapper.applyToVersion(version, layout);

        ContractTemplateVersions savedVersion =
                contractTemplateVersionRepository.save(version);
        template.setContractTemplateUpdateAt(LocalDateTime.now());
        contractTemplateRepository.save(template);

        return toVersionResponse(savedVersion);
    }

    private void validateTemplateRequest(ContractTemplateRequest request) {
        if (request == null) {
            throw new BadHttpException("Contract template information is required");
        }

        if (request.contractTypeId() == null) {
            throw new BadHttpException("Contract type is required");
        }

        requireText(request.contractTemplateName(), "Contract template name is required");
    }

    private void applyTemplateRequest(
            ContractTemplates template,
            ContractTemplateRequest request,
            ContractTypes contractType
    ) {
        template.setContractType(contractType);
        template.setContractTemplateName(
                requireText(
                        request.contractTemplateName(),
                        "Contract template name is required"
                )
        );
        template.setContractTemplateDescription(
                normalizeToNull(request.contractTemplateDescription())
        );
        template.setContractTemplateStatus(
                isBlank(request.status()) ? DEFAULT_STATUS : request.status().trim()
        );
        template.setContractTemplateCreatedBy(normalizeToNull(request.createdBy()));
    }

    private ContractTypes findContractType(UUID id) {
        if (id == null) {
            throw new BadHttpException("Contract type is required");
        }

        return contractTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Contract type not found with id: " + id
                ));
    }

    private ContractTemplates findContractTemplate(UUID id) {
        if (id == null) {
            throw new BadHttpException("Contract template id is required");
        }

        return contractTemplateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Contract template not found with id: " + id
                ));
    }

    private ContractTemplateResponse toResponse(ContractTemplates template) {
        ContractTypes contractType = template.getContractType();
        List<ContractTemplateVersionResponse> versions =
                contractTemplateVersionRepository
                        .findByContractTemplateIdOrderByVersionNumberDesc(template.getId())
                        .stream()
                        .map(this::toVersionResponse)
                        .toList();

        return new ContractTemplateResponse(
                template.getId(),
                contractType != null ? contractType.getId() : null,
                contractType != null ? contractType.getContractTypeCode() : null,
                contractType != null ? contractType.getContractTypeName() : null,
                template.getContractTemplateName(),
                template.getContractTemplateDescription(),
                template.getContractTemplateStatus(),
                template.getContractTemplateCreatedBy(),
                template.getContractTemplateCreateAt(),
                template.getContractTemplateUpdateAt(),
                versions.isEmpty() ? 0 : versions.getFirst().versionNumber(),
                contractRepository.countByContractTemplateId(template.getId()),
                versions
        );
    }

    private ContractTemplateVersionResponse toVersionResponse(
            ContractTemplateVersions version
    ) {
        return new ContractTemplateVersionResponse(
                version.getId(),
                version.getVersionNumber(),
                version.getVersionName(),
                version.getTemplateContent(),
                version.getLayoutJson(),
                version.getChangeNote(),
                version.getCreatedBy(),
                version.getCreatedAt(),
                version.getPageCount() == null ? 1 : version.getPageCount(),
                layoutMapper.toResponses(version)
        );
    }

    private String requireText(String value, String message) {
        if (isBlank(value)) {
            throw new BadHttpException(message);
        }

        return value.trim();
    }

    private String normalizeToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
