package com.fpt.backend.service.impl;

import com.fpt.backend.dto.request.contract.ContractListRequest;
import com.fpt.backend.dto.request.contract.ContractRequest;
import com.fpt.backend.dto.request.contract.ContractTemplateLayout;
import com.fpt.backend.dto.request.contract.ContractTransitionRequest;
import com.fpt.backend.dto.response.contract.ContractListResponse;
import com.fpt.backend.dto.response.contract.ContractProjectOptionResponse;
import com.fpt.backend.dto.response.contract.ContractResponse;
import com.fpt.backend.dto.response.contract.ContractStatusHistoryResponse;
import com.fpt.backend.entity.ContractStatusHistory;
import com.fpt.backend.entity.ContractTemplateVersions;
import com.fpt.backend.entity.ContractTemplates;
import com.fpt.backend.entity.ContractTypes;
import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.enums.ContractAction;
import com.fpt.backend.enums.ContractStatus;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.exception.NotFoundException;
import com.fpt.backend.repository.contract.ContractRepository;
import com.fpt.backend.repository.contract.ContractStatusHistoryRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {
    private static final int PAGE_SIZE = 8;
    private static final int MINIMUM_SIGNER_AGE = 18;
    private static final String DATA_SOURCE = "DATABASE";
    private static final String DEFAULT_SORT_FIELD = "id";
    private static final String ADMIN_ROLE = "ADMIN";

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
    private final ContractStatusHistoryRepository contractStatusHistoryRepository;
    private final ContractTemplateLayoutMapper layoutMapper;

    @Override
    @Transactional(readOnly = true)
    public ContractListResponse getContracts(ContractListRequest request) {
        String search = normalize(request.search());
        String status = normalize(request.status());
        Pageable pageable = createPageable(
                request.page(),
                request.sortBy(),
                request.sortDirection()
        );

        Page<Contracts> contracts = findContracts(search, status, pageable);
        List<String> availableStatuses = Arrays.stream(ContractStatus.values())
                .map(Enum::name)
                .toList();

        return new ContractListResponse(
                DATA_SOURCE,
                contracts.map(this::toResponse).getContent(),
                contracts.getNumber(),
                contracts.getSize(),
                contracts.getTotalElements(),
                contracts.getTotalPages(),
                contracts.isFirst(),
                contracts.isLast(),
                availableStatuses
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
        validateCreateActor(request);
        Contracts contract = new Contracts();
        applyEditableFields(contract, request, true);

        LocalDateTime now = LocalDateTime.now();
        contract.setContractStatus(ContractStatus.NEW.name());
        contract.setContractCreatedAt(now);
        contract.setContractStatusUpdatedAt(now);
        contract.setContractEndedAt(null);
        contract.setContractCancellationReason(null);
        contract.setPreviousContract(resolvePreviousContract(
                request.previousContractId(),
                contract.getProject()
        ));

        Contracts savedContract = contractRepository.save(contract);
        recordHistory(
                savedContract,
                null,
                ContractStatus.NEW,
                "CREATE",
                savedContract.getContractCreateBy(),
                "CREATOR",
                savedContract.getPreviousContract() == null
                        ? "Contract created"
                        : "Replacement contract created",
                null
        );

        return toResponse(savedContract);
    }

    @Override
    @Transactional
    public ContractResponse updateContract(UUID id, ContractRequest request) {
        Contracts contract = findContract(id);
        requireStatus(contract, ContractStatus.NEW, "Only NEW contracts can be edited");
        validateNewContractOwner(
                contract,
                request.actorName(),
                request.actorRole(),
                "edit"
        );
        applyEditableFields(contract, request, false);

        return toResponse(contractRepository.save(contract));
    }

    @Override
    @Transactional
    public ContractResponse transitionContract(
            UUID id,
            ContractTransitionRequest request
    ) {
        if (request == null) {
            throw new BadHttpException("Contract transition information is required");
        }

        Contracts contract = findContract(id);
        ContractStatus currentStatus = readStatus(contract);
        ContractAction action = readAction(request.action());
        String actorName = requireText(request.actorName(), "Actor name is required");
        String actorRole = normalizeRole(request.actorRole());

        if (currentStatus.isTerminal()) {
            throw new BadHttpException(
                    "A terminal contract cannot transition from " + currentStatus.name()
            );
        }

        validateActionStatus(action, currentStatus);
        validateRole(action, currentStatus, actorRole);
        validateCreatorAction(contract, action, currentStatus, actorName, actorRole);

        if (action == ContractAction.CANCEL || action == ContractAction.REJECT) {
            requireText(request.comment(), "A cancellation or rejection reason is required");
        }

        Boolean signerAgeVerified = null;
        if (action == ContractAction.SIGN_DIRECTOR
                || action == ContractAction.SIGN_PARTNER) {
            signerAgeVerified = validateSignerAge(request.signerDateOfBirth());

            if (!signerAgeVerified) {
                String ageFailureReason =
                        "Signer must be at least " + MINIMUM_SIGNER_AGE + " years old";
                applyStatus(
                        contract,
                        currentStatus,
                        ContractStatus.CANCELLED,
                        "AGE_VALIDATION_FAILED",
                        actorName,
                        actorRole,
                        combineComments(ageFailureReason, request.comment()),
                        false
                );
                return toResponse(contractRepository.save(contract));
            }
        }

        ContractStatus targetStatus = resolveTargetStatus(action);
        applyStatus(
                contract,
                currentStatus,
                targetStatus,
                action.name(),
                actorName,
                actorRole,
                normalizeToNull(request.comment()),
                signerAgeVerified
        );

        return toResponse(contractRepository.save(contract));
    }

    @Override
    @Transactional
    public void deleteContract(UUID id, String actorName, String actorRole) {
        Contracts contract = findContract(id);
        requireStatus(contract, ContractStatus.NEW, "Only NEW contracts can be deleted");
        validateNewContractOwner(contract, actorName, actorRole, "delete");
        contractRepository.delete(contract);
    }

    private Page<Contracts> findContracts(
            String search,
            String status,
            Pageable pageable
    ) {
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

    private void applyEditableFields(
            Contracts contract,
            ContractRequest request,
            boolean creating
    ) {
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
            version = createTemplateVersion(template, version, request);
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
        contract.setEffectiveDate(request.effectiveDate());
        contract.setExpirationDate(request.expirationDate());
        if (creating) {
            contract.setContractCreateBy(normalizeToNull(request.contractCreatedBy()));
        }
        contract.setProject(project);
        contract.setContractType(contractType);
        contract.setContractTemplate(template);
        contract.setContractTemplateVersion(version);
        contract.setContractContent(content);
        contract.setContractLayoutJson(layoutJson);
    }

    private void applyStatus(
            Contracts contract,
            ContractStatus fromStatus,
            ContractStatus toStatus,
            String action,
            String actorName,
            String actorRole,
            String comment,
            Boolean signerAgeVerified
    ) {
        LocalDateTime now = LocalDateTime.now();
        contract.setContractStatus(toStatus.name());
        contract.setContractStatusUpdatedAt(now);

        if (toStatus.isTerminal()) {
            contract.setContractEndedAt(now);
        }

        if (toStatus == ContractStatus.CANCELLED) {
            contract.setContractCancellationReason(comment);
        } else {
            contract.setContractCancellationReason(null);
        }

        Contracts savedContract = contractRepository.save(contract);
        recordHistory(
                savedContract,
                fromStatus,
                toStatus,
                action,
                actorName,
                actorRole,
                comment,
                signerAgeVerified
        );
    }

    private void recordHistory(
            Contracts contract,
            ContractStatus fromStatus,
            ContractStatus toStatus,
            String action,
            String actorName,
            String actorRole,
            String comment,
            Boolean signerAgeVerified
    ) {
        ContractStatusHistory history = ContractStatusHistory.builder()
                .contract(contract)
                .fromStatus(fromStatus == null ? null : fromStatus.name())
                .toStatus(toStatus.name())
                .action(action)
                .actorName(normalizeToNull(actorName))
                .actorRole(normalizeToNull(actorRole))
                .comment(normalizeToNull(comment))
                .signerAgeVerified(signerAgeVerified)
                .changedAt(LocalDateTime.now())
                .build();
        contractStatusHistoryRepository.save(history);
    }

    private void validateActionStatus(
            ContractAction action,
            ContractStatus currentStatus
    ) {
        boolean valid = switch (action) {
            case SUBMIT -> currentStatus == ContractStatus.NEW;
            case APPROVE_INTERNAL ->
                    currentStatus == ContractStatus.PENDING_INTERNAL_APPROVAL;
            case SIGN_DIRECTOR ->
                    currentStatus == ContractStatus.PENDING_DIRECTOR_SIGNATURE;
            case SIGN_PARTNER ->
                    currentStatus == ContractStatus.PENDING_PARTNER_SIGNATURE;
            case END -> currentStatus == ContractStatus.ACTIVE;
            case CANCEL -> !currentStatus.isTerminal();
            case REJECT -> currentStatus == ContractStatus.PENDING_INTERNAL_APPROVAL
                    || currentStatus == ContractStatus.PENDING_DIRECTOR_SIGNATURE
                    || currentStatus == ContractStatus.PENDING_PARTNER_SIGNATURE;
        };

        if (!valid) {
            throw new BadHttpException(
                    "Action " + action.name()
                            + " is not allowed while contract status is "
                            + currentStatus.name()
            );
        }
    }

    private void validateRole(
            ContractAction action,
            ContractStatus currentStatus,
            String actorRole
    ) {
        if (ADMIN_ROLE.equals(actorRole)) {
            return;
        }

        Set<String> allowedRoles = switch (action) {
            case SUBMIT -> Set.of("EMPLOYEE", "MANAGER", "CEO", "DIRECTOR");
            case APPROVE_INTERNAL -> Set.of("MANAGER");
            case SIGN_DIRECTOR -> Set.of("CEO", "DIRECTOR");
            case SIGN_PARTNER -> Set.of("PARTNER", "EXTERNAL", "EXTERNAL_PARTNER");
            case END -> Set.of("CEO", "DIRECTOR", "PARTNER", "EXTERNAL_PARTNER");
            case REJECT -> rolesForCurrentStage(currentStatus);
            case CANCEL -> rolesForCancellation(currentStatus);
        };

        if (!allowedRoles.contains(actorRole)) {
            throw new BadHttpException(
                    "Role " + actorRole + " cannot perform " + action.name()
                            + " while contract status is " + currentStatus.name()
            );
        }
    }

    private Set<String> rolesForCurrentStage(ContractStatus status) {
        return switch (status) {
            case PENDING_INTERNAL_APPROVAL -> Set.of("MANAGER");
            case PENDING_DIRECTOR_SIGNATURE -> Set.of("CEO", "DIRECTOR");
            case PENDING_PARTNER_SIGNATURE ->
                    Set.of("PARTNER", "EXTERNAL", "EXTERNAL_PARTNER");
            default -> Set.of();
        };
    }

    private Set<String> rolesForCancellation(ContractStatus status) {
        return switch (status) {
            case NEW -> Set.of("EMPLOYEE", "MANAGER", "CEO", "DIRECTOR");
            case PENDING_INTERNAL_APPROVAL -> Set.of("MANAGER", "CEO", "DIRECTOR");
            case PENDING_DIRECTOR_SIGNATURE -> Set.of("CEO", "DIRECTOR");
            case PENDING_PARTNER_SIGNATURE ->
                    Set.of("CEO", "DIRECTOR", "PARTNER", "EXTERNAL", "EXTERNAL_PARTNER");
            case ACTIVE ->
                    Set.of("CEO", "DIRECTOR", "PARTNER", "EXTERNAL_PARTNER");
            case ENDED, CANCELLED -> Set.of();
        };
    }

    private void validateCreatorAction(
            Contracts contract,
            ContractAction action,
            ContractStatus currentStatus,
            String actorName,
            String actorRole
    ) {
        if (currentStatus != ContractStatus.NEW
                || (action != ContractAction.SUBMIT
                && action != ContractAction.CANCEL)
                || ADMIN_ROLE.equals(actorRole)
                || isBlank(contract.getContractCreateBy())) {
            return;
        }

        if (!contract.getContractCreateBy().trim().equalsIgnoreCase(actorName.trim())) {
            throw new BadHttpException(
                    "Only the contract creator can submit or cancel a NEW contract"
            );
        }
    }

    private Boolean validateSignerAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            throw new BadHttpException(
                    "Signer date of birth is required before signing"
            );
        }

        LocalDate today = LocalDate.now();
        if (dateOfBirth.isAfter(today)) {
            throw new BadHttpException("Signer date of birth cannot be in the future");
        }

        return Period.between(dateOfBirth, today).getYears() >= MINIMUM_SIGNER_AGE;
    }

    private ContractStatus resolveTargetStatus(ContractAction action) {
        return switch (action) {
            case SUBMIT -> ContractStatus.PENDING_INTERNAL_APPROVAL;
            case APPROVE_INTERNAL -> ContractStatus.PENDING_DIRECTOR_SIGNATURE;
            case SIGN_DIRECTOR -> ContractStatus.PENDING_PARTNER_SIGNATURE;
            case SIGN_PARTNER -> ContractStatus.ACTIVE;
            case END -> ContractStatus.ENDED;
            case CANCEL, REJECT -> ContractStatus.CANCELLED;
        };
    }

    private Projects resolveProject(UUID projectId) {
        if (projectId == null) {
            throw new BadHttpException("Project is required");
        }

        return projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException(
                        "Project not found with id: " + projectId
                ));
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

    private Contracts resolvePreviousContract(
            UUID previousContractId,
            Projects project
    ) {
        if (previousContractId == null) {
            return null;
        }

        Contracts previousContract = findContract(previousContractId);
        requireStatus(
                previousContract,
                ContractStatus.CANCELLED,
                "Only a CANCELLED contract can be replaced"
        );

        if (previousContract.getProject() == null
                || !previousContract.getProject().getId().equals(project.getId())) {
            throw new BadHttpException(
                    "A replacement contract must belong to the same project"
            );
        }

        return previousContract;
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
            ContractTemplateVersions sourceVersion,
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
        version.setChangeNote(normalizeToNull(request.templateVersionNote()));
        version.setCreatedBy(normalizeToNull(request.contractCreatedBy()));
        version.setCreatedAt(LocalDateTime.now());

        ContractTemplateLayout layout;
        if (!isBlank(request.contractLayoutJson())) {
            layout = layoutMapper.normalize(
                    null,
                    null,
                    request.contractLayoutJson()
            );
        } else if (sourceVersion != null) {
            layout = layoutMapper.fromVersion(sourceVersion);
        } else {
            layout = layoutMapper.normalize(null, null, null);
        }
        layoutMapper.applyToVersion(version, layout);

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

        if (isBlank(request.contractCreatedBy())) {
            throw new BadHttpException("Contract creator is required");
        }

        if (request.effectiveDate() == null) {
            throw new BadHttpException("Effective date is required");
        }

        if (request.expirationDate() == null) {
            throw new BadHttpException("Expiration date is required");
        }

        if (request.expirationDate().isBefore(request.effectiveDate())) {
            throw new BadHttpException(
                    "Expiration date must be on or after the effective date"
            );
        }
    }

    private Contracts findContract(UUID id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Contract not found with id: " + id
                ));
    }

    private void validateCreateActor(ContractRequest request) {
        if (request == null) {
            throw new BadHttpException("Contract information is required");
        }

        String actorName = requireText(request.actorName(), "Actor name is required");
        String actorRole = normalizeRole(request.actorRole());
        String creatorName = requireText(
                request.contractCreatedBy(),
                "Contract creator is required"
        );

        if (!actorName.equalsIgnoreCase(creatorName)) {
            throw new BadHttpException(
                    "The signed-in user must be the contract creator"
            );
        }

        if (!Set.of("EMPLOYEE", "MANAGER", "CEO", "DIRECTOR", ADMIN_ROLE)
                .contains(actorRole)) {
            throw new BadHttpException(
                    "Role " + actorRole + " cannot create contracts"
            );
        }
    }

    private void validateNewContractOwner(
            Contracts contract,
            String actorName,
            String actorRole,
            String operation
    ) {
        String normalizedActorName = requireText(actorName, "Actor name is required");
        String normalizedActorRole = normalizeRole(actorRole);

        if (ADMIN_ROLE.equals(normalizedActorRole)) {
            return;
        }

        if (isBlank(contract.getContractCreateBy())
                || !contract.getContractCreateBy().trim()
                .equalsIgnoreCase(normalizedActorName)) {
            throw new BadHttpException(
                    "Only the contract creator can " + operation + " a NEW contract"
            );
        }
    }

    private void requireStatus(
            Contracts contract,
            ContractStatus requiredStatus,
            String message
    ) {
        if (readStatus(contract) != requiredStatus) {
            throw new BadHttpException(message);
        }
    }

    private ContractStatus readStatus(Contracts contract) {
        try {
            return ContractStatus.fromValue(contract.getContractStatus());
        } catch (IllegalArgumentException exception) {
            throw new BadHttpException(
                    "Unsupported contract status: " + contract.getContractStatus()
            );
        }
    }

    private ContractAction readAction(String value) {
        try {
            return ContractAction.fromValue(value);
        } catch (IllegalArgumentException exception) {
            throw new BadHttpException(
                    exception.getMessage() == null
                            ? "Unsupported contract action"
                            : exception.getMessage()
            );
        }
    }

    private Pageable createPageable(
            int page,
            String sortBy,
            String sortDirection
    ) {
        int validPage = Math.max(page, 0);
        String sortField = sortBy != null && SORT_FIELDS.contains(sortBy)
                ? sortBy
                : DEFAULT_SORT_FIELD;
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(
                validPage,
                PAGE_SIZE,
                Sort.by(direction, sortField)
        );
    }

    private ContractResponse toResponse(Contracts contract) {
        Projects project = contract.getProject();
        ContractTypes contractType = contract.getContractType();
        ContractTemplates template = contract.getContractTemplate();
        ContractTemplateVersions version = contract.getContractTemplateVersion();
        Contracts previousContract = contract.getPreviousContract();
        ContractStatus status = readStatus(contract);
        List<ContractStatusHistoryResponse> history =
                contractStatusHistoryRepository
                        .findByContractIdOrderByChangedAtDesc(contract.getId())
                        .stream()
                        .map(this::toHistoryResponse)
                        .toList();

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
                status.name(),
                contract.getEffectiveDate(),
                contract.getExpirationDate(),
                contract.getContractCreateBy(),
                contract.getContractCreatedAt(),
                contract.getContractContent(),
                contract.getContractLayoutJson(),
                contract.getContractStatusUpdatedAt(),
                contract.getContractEndedAt(),
                contract.getContractCancellationReason(),
                previousContract != null ? previousContract.getId() : null,
                previousContract != null ? previousContract.getContractNumber() : null,
                history
        );
    }

    private ContractStatusHistoryResponse toHistoryResponse(
            ContractStatusHistory history
    ) {
        return new ContractStatusHistoryResponse(
                history.getId(),
                history.getFromStatus(),
                history.getToStatus(),
                history.getAction(),
                history.getActorName(),
                history.getActorRole(),
                history.getComment(),
                history.getSignerAgeVerified(),
                history.getChangedAt()
        );
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeRole(String value) {
        String role = requireText(value, "Actor role is required");
        return role.toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }

    private String normalizeContent(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private String requireText(String value, String message) {
        if (isBlank(value)) {
            throw new BadHttpException(message);
        }
        return value.trim();
    }

    private String combineComments(String first, String second) {
        String normalizedSecond = normalizeToNull(second);
        return normalizedSecond == null ? first : first + ". " + normalizedSecond;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
