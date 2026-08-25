package com.fpt.backend.service.impl.contract;

import com.fpt.backend.dto.request.contract.ContractListRequest;
import com.fpt.backend.dto.request.contract.ContractRequest;
import com.fpt.backend.dto.request.contract.ContractTemplateLayout;
import com.fpt.backend.dto.request.contract.ContractTransitionRequest;
import com.fpt.backend.dto.request.contract.ContractWorkflowAssigneeRequest;
import com.fpt.backend.dto.response.contract.ContractAccessResponse;
import com.fpt.backend.dto.response.contract.ContractListResponse;
import com.fpt.backend.dto.response.contract.ContractPdfResponse;
import com.fpt.backend.dto.response.contract.ContractPhaseOptionResponse;
import com.fpt.backend.dto.response.contract.ContractProjectContextResponse;
import com.fpt.backend.dto.response.contract.ContractProjectMemberOptionResponse;
import com.fpt.backend.dto.response.contract.ContractProjectOptionResponse;
import com.fpt.backend.dto.response.contract.ContractResponse;
import com.fpt.backend.dto.response.contract.ContractStatusHistoryResponse;
import com.fpt.backend.dto.response.contract.ContractTaskOptionResponse;
import com.fpt.backend.dto.response.contract.ContractWorkflowRuntimeResponse;
import com.fpt.backend.dto.response.contract.ContractWorkflowStepRuntimeResponse;
import com.fpt.backend.dto.response.project.ProjectAccessResponse;
import com.fpt.backend.entity.*;
import com.fpt.backend.enums.ContractAction;
import com.fpt.backend.enums.ContractStatus;
import com.fpt.backend.enums.ContractWorkflowActionType;
import com.fpt.backend.enums.ContractWorkflowStepState;
import com.fpt.backend.enums.ElectronicStatus;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.exception.NotFoundException;
import com.fpt.backend.repository.contract.ContractAttributeValueRepository;
import com.fpt.backend.repository.contract.ContractRepository;
import com.fpt.backend.repository.contract.ContractStatusHistoryRepository;
import com.fpt.backend.repository.contract.ContractTemplateRepository;
import com.fpt.backend.repository.contract.ContractTemplateVersionRepository;
import com.fpt.backend.repository.contract.ContractTypeRepository;
import com.fpt.backend.repository.contract.ContractTypeWorkflowRepository;
import com.fpt.backend.repository.contract.ContractWorkflowStepInstanceRepository;
import com.fpt.backend.repository.electronicSignature.ElectronicSignatureRepository;
import com.fpt.backend.repository.signature.SignatureRepository;
import com.fpt.backend.service.impl.signature.ContractSigningService;
import com.fpt.backend.service.impl.CloudinaryService;
import com.fpt.backend.repository.permission.UserPermissionRepository;
import com.fpt.backend.repository.phase.PhaseRepository;
import com.fpt.backend.repository.phase.PhaseTaskRepository;
import com.fpt.backend.repository.project.ProjectMemberRepository;
import com.fpt.backend.repository.project.ProjectRepository;
import com.fpt.backend.service.interfaces.contract.ContractService;
import com.fpt.backend.service.interfaces.permission.IPermissionAccessService;
import com.fpt.backend.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.security.MessageDigest;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {
    private static final int PAGE_SIZE = 8;
    private static final int MINIMUM_SIGNER_AGE = 18;
    private static final String DATA_SOURCE = "DATABASE";
    private static final String DEFAULT_SORT_FIELD = "id";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final UUID NO_MATCH_PROJECT_ID = new UUID(0L, 0L);

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
    private final ContractTypeWorkflowRepository contractTypeWorkflowRepository;
    private final ContractWorkflowStepInstanceRepository workflowStepRepository;
    private final ContractAttributeValueRepository contractAttributeValueRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final PhaseRepository phaseRepository;
    private final PhaseTaskRepository phaseTaskRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final ContractTemplateLayoutMapper layoutMapper;
    private final ContractDocumentRenderer documentRenderer;
    private final ContractPdfGenerator pdfGenerator;
    private final ElectronicSignatureRepository electronicSignatureRepository;
    private final SignatureRepository signatureRepository;
    private final ContractSigningService contractSigningService;
    private final CloudinaryService cloudinaryService;
    private final IPermissionAccessService permissionAccessService;
    private final CurrentUser currentUser;
    private final ApplicationEventPublisher eventPublisher;

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

        Users user = currentUser.getCurrentUser();
        List<UUID> permissionProjectIds = permissionAccessService
                .getCurrentUserProjectIdsWithAction(
                        ContractProjectActions.VIEW
                );
        List<UUID> viewableProjectIds = new ArrayList<>(permissionProjectIds);
        workflowStepRepository.findDistinctProjectIdsByAssignedUserId(user.getId())
                .stream()
                .filter(projectId -> !viewableProjectIds.contains(projectId))
                .forEach(viewableProjectIds::add);
        Page<Contracts> contracts = findContracts(
                search,
                status,
                pageable,
                user,
                viewableProjectIds
        );
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
        List<UUID> projectIds = permissionAccessService
                .getCurrentUserProjectIdsWithAction(
                        ContractProjectActions.CREATE
                );

        if (projectIds.isEmpty()) {
            return List.of();
        }

        return projectRepository.findAllById(projectIds)
                .stream()
                .sorted(Comparator.comparing(
                        project -> normalize(project.getProjectName()),
                        String.CASE_INSENSITIVE_ORDER
                ))
                .map(project -> new ContractProjectOptionResponse(
                        project.getId(),
                        project.getProjectCode(),
                        project.getProjectName()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ContractProjectContextResponse getProjectContext(UUID projectId) {
        Projects project = resolveProject(projectId);
        ProjectAccessResponse access = permissionAccessService
                .getCurrentUserAccess(project.getId());
        if (!permissionAccessService.hasAction(
                access,
                ContractProjectActions.CREATE
        ) && !permissionAccessService.hasAction(
                access,
                ContractProjectActions.EDIT
        )) {
            throw forbidden(
                    "CREATE_CONTRACTS or EDIT_CONTRACTS permission is required to load contract creation context"
            );
        }

        List<ContractPhaseOptionResponse> phases = phaseRepository
                .findByProjectId(project.getId())
                .stream()
                .map(phase -> new ContractPhaseOptionResponse(
                        phase.getId(),
                        phase.getTitle(),
                        phase.getStatus() == null
                                ? null
                                : phase.getStatus().name(),
                        phaseTaskRepository.findByPhaseId(phase.getId())
                                .stream()
                                .map(task -> new ContractTaskOptionResponse(
                                        task.getId(),
                                        task.getTitle(),
                                        task.getStatus(),
                                        task.getAssignedTo() == null
                                                ? null
                                                : task.getAssignedTo().getId(),
                                        task.getAssignedTo() == null
                                                ? null
                                                : getUserDisplayName(
                                                task.getAssignedTo()
                                        )
                                ))
                                .toList()
                ))
                .toList();

        Map<UUID, Users> eligibleUsers = new LinkedHashMap<>();
        projectMemberRepository.findByProjectId(project.getId()).stream()
                .map(ProjectMember::getUser).filter(Objects::nonNull)
                .forEach(user -> eligibleUsers.put(user.getId(), user));

        List<ContractProjectMemberOptionResponse> members = eligibleUsers.values()
                .stream()
                .map(user -> new ContractProjectMemberOptionResponse(
                        user.getId(),
                        getUserDisplayName(user),
                        user.getEmail(),
                        primaryRoleCode(user),
                        roleCodes(user),
                        List.copyOf(contractActionsForCandidate(user, project.getId()))
                ))
                .toList();

        return new ContractProjectContextResponse(
                project.getId(),
                phases,
                members
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ContractResponse getContractById(UUID id) {
        Contracts contract = findContract(id);
        requireContractAction(
                contract,
                ContractProjectActions.VIEW,
                currentUser.getCurrentUser()
        );
        return toResponse(contract);
    }

    @Override
    @Transactional
    public ContractResponse createContract(ContractRequest request) {
        if (request == null) {
            throw new BadHttpException("Contract information is required");
        }

        Users actor = currentUser.getCurrentUser();
        permissionAccessService.requireAction(
                request.projectId(),
                ContractProjectActions.CREATE
        );
        Contracts contract = new Contracts();
        applyEditableFields(contract, request, true);
        contract.setContractCreatedByUser(actor);
        contract.setContractCreateBy(getUserDisplayName(actor));

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
        createWorkflowInstances(savedContract, actor, request.workflowAssignees());
        syncAttributeValues(savedContract, request.attributeValues());
        recordHistory(
                savedContract,
                null,
                ContractStatus.NEW,
                "CREATE",
                savedContract.getContractCreateBy(),
                primaryRoleCode(actor),
                savedContract.getPreviousContract() == null
                        ? "Contract created"
                        : "Replacement contract created",
                null
        );

        storeCanonicalDocument(savedContract, actor);

        return toResponse(savedContract);
    }

    @Override
    @Transactional
    public ContractResponse updateContract(UUID id, ContractRequest request) {
        Contracts contract = findContract(id);
        Users actor = currentUser.getCurrentUser();
        requireContractAction(
                contract,
                ContractProjectActions.EDIT,
                actor
        );
        requireStatus(contract, ContractStatus.NEW, "Only NEW contracts can be edited");
        validateProjectUnchanged(contract, request);
        validateWorkflowSelectionUnchanged(contract, request);
        applyEditableFields(contract, request, false);
        Contracts savedContract = contractRepository.save(contract);
        if (request.attributeValues() != null) {
            syncAttributeValues(savedContract, request.attributeValues());
        }
        storeCanonicalDocument(savedContract, actor);
        return toResponse(savedContract);
    }

    // doan chu ky o day
    //CalculRSA
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
        Users actor = currentUser.getCurrentUser();
        ContractStatus currentStatus = readStatus(contract);
        ContractAction action = readAction(request.action());
        String actorName = getUserDisplayName(actor);
        String actorRole = primaryRoleCode(actor);

        if (currentStatus.isTerminal()) {
            throw new BadHttpException(
                    "A terminal contract cannot transition from " + currentStatus.name()
            );
        }

        if (contract.getWorkflowVersion() != null
                || workflowStepRepository.existsByContractId(contract.getId())) {
            return transitionWorkflowContract(
                    contract,
                    currentStatus,
                    action,
                    request,
                    actor
            );
        }

        validateActionStatus(action, currentStatus);
        requireContractAction(
                contract,
                permissionForWorkflowAction(action, currentStatus),
                actor
        );
        validateRole(action, currentStatus, actorRole);

        if (action == ContractAction.CANCEL || action == ContractAction.REJECT) {
            requireText(request.comment(), "A cancellation or rejection reason is required");
        }

        Boolean signerAgeVerified = null;
        if (action == ContractAction.SIGN_DIRECTOR
                || action == ContractAction.SIGN_PARTNER) {
            signerAgeVerified = validateSignerAge(actor.getDob());

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
            registerElectronicSignatureWhenRequired(contract, action, request, actor);
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

    private Signature registerElectronicSignatureWhenRequired(
            Contracts contract,
            ContractAction action,
            ContractTransitionRequest request,
            Users actor
    ) {
        boolean legacySign = action == ContractAction.SIGN_DIRECTOR
                || action == ContractAction.SIGN_PARTNER;
        boolean workflowSign = false;
        if (action == ContractAction.COMPLETE_STEP
                && workflowStepRepository.existsByContractId(contract.getId())) {
            workflowSign = workflowStepRepository
                    .findFirstByContractIdAndStatusOrderByStepOrderAsc(
                            contract.getId(), ContractWorkflowStepState.PENDING
                    )
                    .map(step -> step.getActionType().requiresSignature())
                    .orElse(false);
        }
        if (!legacySign && !workflowSign) {
            return null;
        }
        if (request.electronicSignatureId() == null) {
            throw new BadHttpException("Please select an electronic signature before signing");
        }
        ElectronicSignatures selected = electronicSignatureRepository.findOwnedById(
                        request.electronicSignatureId(), actor.getId()
                )
                .orElseThrow(() -> new BadHttpException(
                        "Electronic signature not found or does not belong to the current user"
                ));
        if (selected.getStatus() != ElectronicStatus.ACTIVE) {
            throw new BadHttpException("Only an active electronic signature can be used");
        }

        if (contract.getDocumentFile() == null) {
            storeCanonicalDocument(
                    contract,
                    contract.getContractCreatedByUser() != null
                            ? contract.getContractCreatedByUser()
                            : actor
            );
        }
        byte[] pdf = loadAndValidateCanonicalDocument(contract, actor);
        try {
            return contractSigningService.signContract(
                    contract, pdf, actor.getId(), selected
            );
        } catch (Exception exception) {
            throw new BadHttpException("Unable to sign the generated contract PDF");
        }
    }

    private void storeCanonicalDocument(Contracts contract, Users owner) {
        replaceCanonicalDocument(contract, owner);
    }

    private byte[] replaceCanonicalDocument(Contracts contract, Users owner) {
        List<ContractStatusHistory> history = loadHistory(contract.getId());
        Map<String, String> attributes = readAttributeValues(contract.getId());
        byte[] pdf = pdfGenerator.generate(
                contract,
                documentRenderer.render(contract, history, attributes)
        );
        FileStorage previousFile = contract.getDocumentFile();
        FileStorage uploaded = cloudinaryService.uploadPdfAndSave(
                pdf, createPdfFileName(contract), owner
        );
        contract.setDocumentFile(uploaded);
        contract.setDocumentHash(calculateDocumentHash(pdf));
        contractRepository.save(contract);
        if (previousFile != null) {
            previousFile.setIsDeleted(true);
        }
        return pdf;
    }

    private byte[] loadAndValidateCanonicalDocument(
            Contracts contract,
            Users recoveryOwner
    ) {
        if (contract.getDocumentFile() == null || contract.getDocumentHash() == null) {
            return recoverUnsignedCanonicalDocument(contract, recoveryOwner);
        }
        byte[] pdf;
        try {
            pdf = cloudinaryService.download(contract.getDocumentFile());
        } catch (BadHttpException exception) {
            return recoverUnsignedCanonicalDocument(contract, recoveryOwner);
        }
        String actualHash = calculateDocumentHash(pdf);
        if (!MessageDigest.isEqual(
                actualHash.getBytes(StandardCharsets.UTF_8),
                contract.getDocumentHash().getBytes(StandardCharsets.UTF_8)
        )) {
            throw new BadHttpException("The stored contract PDF has been changed");
        }
        return pdf;
    }

    private byte[] recoverUnsignedCanonicalDocument(
            Contracts contract,
            Users recoveryOwner
    ) {
        if (signatureRepository.existsByContractId(contract.getId())) {
            throw new BadHttpException(
                    "The signed contract PDF is unavailable and cannot be regenerated "
                            + "without invalidating its digital signatures"
            );
        }
        return replaceCanonicalDocument(contract, recoveryOwner);
    }

    private String calculateDocumentHash(byte[] document) {
        try {
            return Base64.getEncoder().encodeToString(
                    MessageDigest.getInstance("SHA-256").digest(document)
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash contract PDF", exception);
        }
    }

    @Override
    @Transactional
    public ContractPdfResponse exportContractPdf(UUID id) {
        Contracts contract = findContract(id);
        Users actor = currentUser.getCurrentUser();
        requireContractAction(
                contract,
                ContractProjectActions.VIEW,
                actor
        );
        requireContractAction(
                contract,
                ContractProjectActions.EXPORT,
                actor
        );

        List<ContractStatusHistory> history = loadHistory(contract.getId());
        Map<String, String> attributeValues = readAttributeValues(contract.getId());
        ContractDocumentRenderer.RenderedDocument renderedDocument =
                documentRenderer.render(contract, history, attributeValues);

        return new ContractPdfResponse(
                createPdfFileName(contract),
                contract.getDocumentFile() == null
                        ? pdfGenerator.generate(contract, renderedDocument)
                        : loadAndValidateCanonicalDocument(contract, actor)
        );
    }

    @Override
    @Transactional
    public void deleteContract(UUID id) {
        Contracts contract = findContract(id);
        requireContractAction(
                contract,
                ContractProjectActions.DELETE,
                currentUser.getCurrentUser()
        );
        requireStatus(contract, ContractStatus.NEW, "Only NEW contracts can be deleted");
        contractAttributeValueRepository.deleteAllByContractId(contract.getId());
        contractRepository.delete(contract);
    }

    private Page<Contracts> findContracts(
            String search,
            String status,
            Pageable pageable,
            Users user,
            List<UUID> projectIds
    ) {
        if (projectIds.isEmpty()) {
            return Page.empty(pageable);
        }

        List<UUID> fullScopeProjectIds = new ArrayList<>();
        for (UUID projectId : projectIds) {
            ProjectAccessResponse access = permissionAccessService
                    .getCurrentUserAccess(projectId);
            if (permissionAccessService.hasFullWorkScope(
                    access,
                    ContractProjectActions.VIEW
            )) {
                fullScopeProjectIds.add(projectId);
            }
        }

        if (fullScopeProjectIds.isEmpty()) {
            fullScopeProjectIds = List.of(NO_MATCH_PROJECT_ID);
        }

        return contractRepository.searchAccessibleContracts(
                projectIds,
                fullScopeProjectIds,
                user.getId(),
                getUserDisplayName(user).toLowerCase(Locale.ROOT),
                search.toLowerCase(Locale.ROOT),
                status.toLowerCase(Locale.ROOT),
                pageable
        );
    }

    private void requireContractAction(
            Contracts contract,
            String actionCode,
            Users user
    ) {
        if (contract.getProject() == null
                || contract.getProject().getId() == null) {
            throw new BadHttpException(
                    "The contract must belong to a project"
            );
        }

        ProjectAccessResponse access = permissionAccessService
                .getCurrentUserAccess(contract.getProject().getId());

        if (!permissionAccessService.hasAction(access, actionCode)) {
            throw forbidden(
                    "You do not have permission to perform " + actionCode
                            + " in this project"
            );
        }

        if (permissionAccessService.hasFullWorkScope(access, actionCode)
                || isContractOwner(contract, user)
                || (isWorkflowParticipant(contract, user)
                && (ContractProjectActions.VIEW.equals(actionCode)
                || ContractProjectActions.EXPORT.equals(actionCode)))) {
            return;
        }

        throw forbidden(
                "Permission " + actionCode
                        + " is limited to contracts you created"
        );
    }

    private String permissionForWorkflowAction(
            ContractAction action,
            ContractStatus currentStatus
    ) {
        return switch (action) {
            case COMPLETE_STEP -> throw new BadHttpException(
                    "COMPLETE_STEP is only available for configurable workflows"
            );
            case SUBMIT -> ContractProjectActions.SUBMIT;
            case APPROVE_INTERNAL -> ContractProjectActions.APPROVE;
            case SIGN_DIRECTOR, SIGN_PARTNER -> ContractProjectActions.SIGN;
            case CANCEL -> ContractProjectActions.CANCEL;
            case REJECT -> currentStatus
                    == ContractStatus.PENDING_INTERNAL_APPROVAL
                    ? ContractProjectActions.APPROVE
                    : ContractProjectActions.SIGN;
        };
    }

    private void validateProjectUnchanged(
            Contracts contract,
            ContractRequest request
    ) {
        if (request == null) {
            throw new BadHttpException("Contract information is required");
        }

        UUID currentProjectId = contract.getProject() == null
                ? null
                : contract.getProject().getId();

        if (currentProjectId == null
                || !currentProjectId.equals(request.projectId())) {
            throw new BadHttpException(
                    "A contract cannot be moved to another project"
            );
        }
    }

    private void validateWorkflowSelectionUnchanged(
            Contracts contract,
            ContractRequest request
    ) {
        UUID currentTypeId = contract.getContractType() == null
                ? null
                : contract.getContractType().getId();
        UUID currentTaskId = contract.getTimelineTask() == null
                ? null
                : contract.getTimelineTask().getId();

        if (currentTypeId == null
                || !currentTypeId.equals(request.contractTypeId())) {
            throw new BadHttpException(
                    "Contract type cannot be changed after the contract is created"
            );
        }
        if (currentTaskId == null || !currentTaskId.equals(request.taskId())) {
            throw new BadHttpException(
                    "Contract task cannot be changed after the contract is created"
            );
        }
    }

    private void createWorkflowInstances(
            Contracts contract,
            Users creator,
            List<ContractWorkflowAssigneeRequest> requestedAssignees
    ) {
        ContractTypeWorkflow workflow = contract.getWorkflowVersion();
        if (workflow == null) {
            throw new BadHttpException(
                    "The selected contract type does not have an active workflow"
            );
        }
        List<ContractTypeWorkflowStep> definitions = workflow.getSteps() == null
                ? List.of()
                : workflow.getSteps().stream()
                .sorted(Comparator.comparing(ContractTypeWorkflowStep::getStepOrder))
                .toList();
        if (definitions.size() < 2) {
            throw new BadHttpException(
                    "The selected contract type workflow requires at least two steps"
            );
        }

        Map<Integer, UUID> assigneeByStep = new LinkedHashMap<>();
        if (requestedAssignees != null) {
            for (ContractWorkflowAssigneeRequest assignment : requestedAssignees) {
                if (assignment == null || assignment.stepOrder() == null
                        || assignment.userId() == null) {
                    throw new BadHttpException(
                            "Every workflow assignment requires a step and a user"
                    );
                }
                if (assigneeByStep.putIfAbsent(
                        assignment.stepOrder(),
                        assignment.userId()
                ) != null) {
                    throw new BadHttpException(
                            "A workflow step can only have one assigned user"
                    );
                }
            }
        }

        Map<UUID, Users> projectMembers = new LinkedHashMap<>();
        projectMemberRepository.findByProjectId(contract.getProject().getId())
                .stream()
                .map(ProjectMember::getUser)
                .filter(Objects::nonNull)
                .forEach(user -> projectMembers.put(user.getId(), user));

        if (!projectMembers.containsKey(creator.getId())) {
            throw new BadHttpException(
                    "The contract creator must be a member of the selected project"
            );
        }

        LocalDateTime now = LocalDateTime.now();
        List<ContractWorkflowStepInstance> instances = new ArrayList<>();
        for (int index = 0; index < definitions.size(); index++) {
            ContractTypeWorkflowStep definition = definitions.get(index);
            Users assignedUser;
            if (index == 0) {
                assignedUser = creator;
                UUID requestedCreatorId = assigneeByStep.remove(definition.getStepOrder());
                if (requestedCreatorId != null
                        && !creator.getId().equals(requestedCreatorId)) {
                    throw new BadHttpException(
                            "The CREATE step must be assigned to the current user"
                    );
                }
            } else {
                UUID userId = assigneeByStep.remove(definition.getStepOrder());
                if (userId == null) {
                    throw new BadHttpException(
                            "Select a project member for workflow step: "
                                    + definition.getStepName()
                    );
                }
                assignedUser = projectMembers.get(userId);
                if (assignedUser == null) {
                    throw new BadHttpException(
                            "Every workflow assignee must be a member of the selected project"
                    );
                }
            }

            validateWorkflowAssignee(
                    assignedUser,
                    definition,
                    contract.getProject().getId()
            );
            instances.add(ContractWorkflowStepInstance.builder()
                    .contract(contract)
                    .stepDefinition(definition)
                    .assignedUser(assignedUser)
                    .stepOrder(definition.getStepOrder())
                    .stepName(definition.getStepName())
                    .actionType(definition.getActionType())
                    .requiredRoleCode(definition.getRequiredRoleCode())
                    .required(Boolean.TRUE.equals(definition.getRequired()))
                    .canReject(Boolean.TRUE.equals(definition.getCanReject()))
                    .status(index == 0
                            ? ContractWorkflowStepState.PENDING
                            : ContractWorkflowStepState.WAITING)
                    .activatedAt(index == 0 ? now : null)
                    .build());
        }

        if (!assigneeByStep.isEmpty()) {
            throw new BadHttpException(
                    "One or more workflow assignments do not belong to the selected contract type"
            );
        }

        workflowStepRepository.saveAll(instances);
        contract.setWorkflowStepInstances(instances);
    }

    private void validateWorkflowAssignee(
            Users user,
            ContractTypeWorkflowStep definition,
            UUID projectId
    ) {
        if (!userHasRole(user, definition.getRequiredRoleCode())) {
            throw new BadHttpException(
                    getUserDisplayName(user) + " does not have required role "
                            + definition.getRequiredRoleCode() + " for step "
                            + definition.getStepName()
            );
        }

        Set<String> allowedActions = activeActionsForUser(user.getId(), projectId);
        List<String> missingActions = ContractWorkflowRules
                .requiredPermissions(definition.getActionType())
                .stream()
                .filter(action -> !allowedActions.contains(action))
                .toList();
        if (!missingActions.isEmpty()) {
            throw new BadHttpException(
                    getUserDisplayName(user) + " is missing project permission(s) "
                            + String.join(", ", missingActions)
                            + " for step " + definition.getStepName()
            );
        }
    }

    private ContractResponse transitionWorkflowContract(
            Contracts contract,
            ContractStatus currentStatus,
            ContractAction action,
            ContractTransitionRequest request,
            Users actor
    ) {
        if (action == ContractAction.CANCEL) {
            requireText(request.comment(), "A cancellation reason is required");
            requireContractAction(
                    contract,
                    ContractProjectActions.CANCEL,
                    actor
            );
            cancelUnfinishedWorkflowSteps(contract, request.comment());
            applyStatus(
                    contract,
                    currentStatus,
                    ContractStatus.CANCELLED,
                    ContractAction.CANCEL.name(),
                    getUserDisplayName(actor),
                    primaryRoleCode(actor),
                    normalizeToNull(request.comment()),
                    null
            );
            return toResponse(contract);
        }

        if (action != ContractAction.COMPLETE_STEP
                && action != ContractAction.REJECT) {
            throw new BadHttpException(
                    "Use COMPLETE_STEP or REJECT for a configurable contract workflow"
            );
        }

        ContractWorkflowStepInstance currentStep = workflowStepRepository
                .findFirstByContractIdAndStatusOrderByStepOrderAsc(
                        contract.getId(),
                        ContractWorkflowStepState.PENDING
                )
                .orElseThrow(() -> new BadHttpException(
                        "The contract has no pending workflow step"
                ));
        requireCurrentAssignee(currentStep, actor);
        requireCurrentWorkflowPermissions(contract, currentStep, actor);

        if (action == ContractAction.REJECT) {
            if (!Boolean.TRUE.equals(currentStep.getCanReject())) {
                throw new BadHttpException(
                        "This workflow step cannot reject the contract"
                );
            }
            requireText(request.comment(), "A rejection reason is required");
            rejectWorkflowStep(currentStep, request.comment());
            cancelUnfinishedWorkflowSteps(contract, request.comment());
            applyStatus(
                    contract,
                    currentStatus,
                    ContractStatus.CANCELLED,
                    ContractAction.REJECT.name(),
                    getUserDisplayName(actor),
                    primaryRoleCode(actor),
                    normalizeToNull(request.comment()),
                    null
            );
            return toResponse(contract);
        }

        Boolean signerAgeVerified = null;
        Signature completedSignature = null;
        if (currentStep.getActionType().requiresSignature()) {
            signerAgeVerified = validateSignerAge(actor.getDob());
            if (!signerAgeVerified) {
                String reason = "Signer must be at least "
                        + MINIMUM_SIGNER_AGE + " years old";
                rejectWorkflowStep(currentStep, reason);
                cancelUnfinishedWorkflowSteps(contract, reason);
                applyStatus(
                        contract,
                        currentStatus,
                        ContractStatus.CANCELLED,
                        "AGE_VALIDATION_FAILED",
                        getUserDisplayName(actor),
                        primaryRoleCode(actor),
                        combineComments(reason, request.comment()),
                        false
                );
                return toResponse(contract);
            }
            completedSignature = registerElectronicSignatureWhenRequired(
                    contract, action, request, actor
            );
        }

        LocalDateTime now = LocalDateTime.now();
        currentStep.setStatus(ContractWorkflowStepState.COMPLETED);
        currentStep.setCompletedAt(now);
        currentStep.setComment(normalizeToNull(request.comment()));
        workflowStepRepository.save(currentStep);

        ContractWorkflowStepInstance nextStep = workflowStepRepository
                .findByContractIdOrderByStepOrderAsc(contract.getId())
                .stream()
                .filter(step -> step.getStatus()
                        == ContractWorkflowStepState.WAITING)
                .findFirst()
                .orElse(null);
        ContractStatus targetStatus = ContractStatus.SIGNED;
        if (nextStep != null) {
            nextStep.setStatus(ContractWorkflowStepState.PENDING);
            nextStep.setActivatedAt(now);
            workflowStepRepository.save(nextStep);
            targetStatus = ContractWorkflowRules.pendingStatus(
                    nextStep.getActionType()
            );
        }

        applyStatus(
                contract,
                currentStatus,
                targetStatus,
                ContractWorkflowRules.historyAction(
                        currentStep.getActionType()
                ),
                getUserDisplayName(actor),
                primaryRoleCode(actor),
                normalizeToNull(request.comment()),
                signerAgeVerified
        );
        publishSigningEmailEvent(
                contract, currentStep, nextStep, actor, completedSignature, now
        );
        return toResponse(contract);
    }

    private void publishSigningEmailEvent(
            Contracts contract,
            ContractWorkflowStepInstance completedStep,
            ContractWorkflowStepInstance nextStep,
            Users signer,
            Signature signature,
            LocalDateTime signedAt
    ) {
        if (signature == null || completedStep == null
                || !completedStep.getActionType().requiresSignature()) {
            return;
        }

        if (userHasRole(signer, "CEO")) {
            if (nextStep == null || nextStep.getAssignedUser() == null) {
                return;
            }
            eventPublisher.publishEvent(new ContractSigningEmailEvent(
                    ContractSigningEmailEvent.Type.CEO_SIGNED,
                    contract.getContractNumber(),
                    contract.getContractTitle(),
                    getUserDisplayName(signer),
                    signer.getEmail(),
                    getUserDisplayName(nextStep.getAssignedUser()),
                    nextStep.getAssignedUser().getEmail(),
                    signature.getUserKey().getPublicKey(),
                    signedAt
            ));
            return;
        }

        ContractWorkflowStepInstance ceoSignatureStep = workflowStepRepository
                .findByContractIdOrderByStepOrderAsc(contract.getId())
                .stream()
                .filter(step -> step.getActionType().requiresSignature())
                .filter(step -> step.getAssignedUser() != null)
                .filter(step -> userHasRole(step.getAssignedUser(), "CEO"))
                .filter(step -> step.getStatus() == ContractWorkflowStepState.COMPLETED)
                .findFirst()
                .orElse(null);
        if (ceoSignatureStep == null) {
            return;
        }
        Users ceo = ceoSignatureStep.getAssignedUser();
        eventPublisher.publishEvent(new ContractSigningEmailEvent(
                ContractSigningEmailEvent.Type.PARTNER_SIGNED,
                contract.getContractNumber(),
                contract.getContractTitle(),
                getUserDisplayName(signer),
                signer.getEmail(),
                getUserDisplayName(ceo),
                ceo.getEmail(),
                signature.getUserKey().getPublicKey(),
                signedAt
        ));
    }

    private void requireCurrentAssignee(
            ContractWorkflowStepInstance currentStep,
            Users actor
    ) {
        if (currentStep.getAssignedUser() == null
                || !currentStep.getAssignedUser().getId().equals(actor.getId())) {
            throw forbidden(
                    "Only the user assigned to the current workflow step may perform this action"
            );
        }
        if (!userHasRole(actor, currentStep.getRequiredRoleCode())) {
            throw forbidden(
                    "Your current role no longer matches the workflow step"
            );
        }
    }

    private void requireCurrentWorkflowPermissions(
            Contracts contract,
            ContractWorkflowStepInstance currentStep,
            Users actor
    ) {
        Set<String> allowedActions = contractActionsForCandidate(
                actor, contract.getProject().getId()
        );
        List<String> missing = ContractWorkflowRules
                .requiredPermissions(currentStep.getActionType())
                .stream()
                .filter(action -> !allowedActions.contains(action))
                .toList();
        if (!missing.isEmpty()) {
            throw forbidden(
                    "You are missing project permission(s): "
                            + String.join(", ", missing)
            );
        }
    }

    private void rejectWorkflowStep(
            ContractWorkflowStepInstance step,
            String comment
    ) {
        step.setStatus(ContractWorkflowStepState.REJECTED);
        step.setCompletedAt(LocalDateTime.now());
        step.setComment(normalizeToNull(comment));
        workflowStepRepository.save(step);
    }

    private void cancelUnfinishedWorkflowSteps(
            Contracts contract,
            String comment
    ) {
        List<ContractWorkflowStepInstance> changed = workflowStepRepository
                .findByContractIdOrderByStepOrderAsc(contract.getId())
                .stream()
                .filter(step -> step.getStatus()
                        == ContractWorkflowStepState.WAITING
                        || step.getStatus() == ContractWorkflowStepState.PENDING)
                .peek(step -> {
                    step.setStatus(ContractWorkflowStepState.CANCELLED);
                    step.setComment(normalizeToNull(comment));
                })
                .toList();
        if (!changed.isEmpty()) {
            workflowStepRepository.saveAll(changed);
        }
    }

    private void applyEditableFields(
            Contracts contract,
            ContractRequest request,
            boolean creating
    ) {
        validateRequest(request);

        Projects project = resolveProject(request.projectId());
        ContractTypes contractType = resolveContractType(request.contractTypeId());
        TimelineTask task = resolveTask(request.taskId(), project);
        ContractTemplates template = resolveTemplate(request.contractTemplateId());
        validateTemplateBelongsToType(template, contractType);

        ContractTemplateVersions version = resolveVersion(
                request.contractTemplateVersionId()
        );
        validateVersionBelongsToTemplate(version, template);

        if (Boolean.TRUE.equals(request.saveAsTemplateVersion())) {
            version = createTemplateVersion(template, version, request);
        }
        Map<String, String> requestedAttributeValues = request.attributeValues();
        if (!creating && requestedAttributeValues == null) {
            requestedAttributeValues = readAttributeValues(contract.getId());
        }
        validateManualAttributeValues(version, requestedAttributeValues);

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
        contract.setProject(project);
        contract.setTimelineTask(task);
        contract.setContractType(contractType);
        if (creating) {
            ContractTypeWorkflow workflow = contractTypeWorkflowRepository
                    .findFirstByContractTypeIdAndActiveTrueOrderByVersionNumberDesc(
                            contractType.getId()
                    )
                    .orElseThrow(() -> new BadHttpException(
                            "The selected contract type does not have an active workflow"
                    ));
            contract.setWorkflowVersion(workflow);
        }
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
            case COMPLETE_STEP -> false;
            case SUBMIT -> currentStatus == ContractStatus.NEW;
            case APPROVE_INTERNAL ->
                    currentStatus == ContractStatus.PENDING_INTERNAL_APPROVAL;
            case SIGN_DIRECTOR ->
                    currentStatus == ContractStatus.PENDING_DIRECTOR_SIGNATURE;
            case SIGN_PARTNER ->
                    currentStatus == ContractStatus.PENDING_PARTNER_SIGNATURE;
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
            case COMPLETE_STEP -> Set.of();
            case SUBMIT -> Set.of("EMPLOYEE", "MANAGER", "CEO", "DIRECTOR");
            case APPROVE_INTERNAL -> Set.of("MANAGER");
            case SIGN_DIRECTOR -> Set.of("CEO", "DIRECTOR");
            case SIGN_PARTNER -> Set.of("PARTNER", "EXTERNAL", "EXTERNAL_PARTNER");
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
            case PENDING_APPROVAL, PENDING_SIGNATURE -> Set.of();
            case SIGNED, ACTIVE ->
                    Set.of("CEO", "DIRECTOR", "PARTNER", "EXTERNAL_PARTNER");
            case PENDING_EFFECTIVE -> null;
            case ENDED, CANCELLED -> Set.of();
        };
    }

    private Boolean validateSignerAge(String storedDateOfBirth) {
        if (storedDateOfBirth == null || storedDateOfBirth.isBlank()) {
            throw new BadHttpException(
                    "Your account date of birth is required before signing"
            );
        }

        LocalDate dateOfBirth;
        try {
            dateOfBirth = LocalDate.parse(storedDateOfBirth.trim());
        } catch (DateTimeParseException exception) {
            throw new BadHttpException(
                    "Your account date of birth must use YYYY-MM-DD format before signing"
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
            case COMPLETE_STEP -> throw new BadHttpException(
                    "COMPLETE_STEP is only available for configurable workflows"
            );
            case SUBMIT -> ContractStatus.PENDING_INTERNAL_APPROVAL;
            case APPROVE_INTERNAL -> ContractStatus.PENDING_DIRECTOR_SIGNATURE;
            case SIGN_DIRECTOR -> ContractStatus.PENDING_PARTNER_SIGNATURE;
            case SIGN_PARTNER -> ContractStatus.SIGNED;
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

    private TimelineTask resolveTask(UUID taskId, Projects project) {
//        if (taskId == null) {
//            throw new BadHttpException("A project task is required");
//        }

        TimelineTask task = phaseTaskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException(
                        "Project task not found with id: " + taskId
                ));
        Timeline phase = task.getTimeline();
        if (phase == null || phase.getProject() == null
                || !phase.getProject().getId().equals(project.getId())) {
            throw new BadHttpException(
                    "The selected task does not belong to the selected project"
            );
        }
        return task;
    }

    private Set<String> activeActionsForUser(UUID userId, UUID projectId) {
        Set<String> actions = new LinkedHashSet<>();
        UserPermission userPermission = userPermissionRepository
                .findActiveByUserIdAndProjectId(userId, projectId);

        if (userPermission == null
                || userPermission.getPermission() == null
                || userPermission.getPermission().getActions() == null) {
            return actions;
        }

        for (PermissionAction action
                : userPermission.getPermission().getActions()) {
            if (action != null && !isBlank(action.getActionCode())) {
                actions.add(action.getActionCode().trim()
                        .toUpperCase(Locale.ROOT));
            }
        }

        return actions;
    }

    private boolean userHasRole(Users user, String requiredRoleCode) {
        String required = normalizeRoleOrEmpty(requiredRoleCode);
        if (required.isEmpty() || user == null || user.getUserRoles() == null) {
            return false;
        }

        if (required.equals(normalizeRoleOrEmpty(user.getUserRoles().stream()
                .map(UserRole::getRole)
                .filter(Objects::nonNull)
                .map(Role::getRoleCode)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null)))) {
            return true;
        }
        return user.getUserRoles().stream()
                .map(UserRole::getRole)
                .filter(Objects::nonNull)
                .anyMatch(role -> required.equals(
                        normalizeRoleOrEmpty(role.getRoleCode())
                ) || required.equals(
                        normalizeRoleOrEmpty(role.getRoleName())
                ));
    }

    private String primaryRoleCode(Users user) {
        String assignedRole = roleCodes(user).stream().findFirst().orElse("");
        if (!assignedRole.isEmpty()) {
            return assignedRole;
        }
        String legacyRole = normalizeRoleOrEmpty(user.getUserRoles().stream()
                .map(UserRole::getRole)
                .filter(Objects::nonNull)
                .map(Role::getRoleCode)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null));
        return legacyRole.isEmpty() ? "UNKNOWN" : legacyRole;
    }

    private List<String> roleCodes(Users user) {
        LinkedHashSet<String> codes = new LinkedHashSet<>();
        if (user != null && user.getUserRoles() != null) {
            user.getUserRoles().stream()
                    .map(UserRole::getRole)
                    .filter(Objects::nonNull)
                    .map(role -> normalizeRoleOrEmpty(role.getRoleCode()))
                    .filter(value -> !value.isEmpty())
                    .forEach(codes::add);
        }
        if (user != null) {
            String legacyRole = normalizeRoleOrEmpty(user.getUserRoles().stream()
                    .map(UserRole::getRole)
                    .filter(Objects::nonNull)
                    .map(Role::getRoleCode)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null));
            if (!legacyRole.isEmpty()) {
                codes.add(legacyRole);
            }
        }
        return List.copyOf(codes);
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
        version.setCreatedBy(getUserDisplayName(currentUser.getCurrentUser()));
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

    private void validateManualAttributeValues(
            ContractTemplateVersions version,
            Map<String, String> requestedValues
    ) {
        Map<String, String> normalizedValues = normalizeAttributeValues(
                requestedValues
        );

        for (ContractPositions position : manualPositions(version)) {
            String value = normalizedValues.get(
                    normalizeAttributeKey(position.getAttributeKey())
            );
            if (Boolean.TRUE.equals(position.getIsRequired()) && isBlank(value)) {
                throw new BadHttpException(
                        position.getFieldLabel() + " is required"
                );
            }
            if (value != null && value.length() > 255) {
                throw new BadHttpException(
                        position.getFieldLabel() + " must not exceed 255 characters"
                );
            }
        }
    }

    private void syncAttributeValues(
            Contracts contract,
            Map<String, String> requestedValues
    ) {
        contractAttributeValueRepository.deleteAllByContractId(contract.getId());
        Map<String, String> normalizedValues = normalizeAttributeValues(
                requestedValues
        );
        LocalDateTime now = LocalDateTime.now();
        List<ContractAttributeValues> values = manualPositions(
                contract.getContractTemplateVersion()
        ).stream()
                .map(position -> {
                    String attributeKey = normalizeAttributeKey(
                            position.getAttributeKey()
                    );
                    String value = normalizeToNull(normalizedValues.get(attributeKey));
                    if (value == null) {
                        return null;
                    }

                    ContractAttributeValues attributeValue =
                            new ContractAttributeValues();
                    attributeValue.setContract(contract);
                    attributeValue.setAttributeKey(attributeKey);
                    attributeValue.setAttributeValue(value);
                    attributeValue.setValueSource("MANUAL");
                    attributeValue.setCreatedAt(now);
                    attributeValue.setUpdatedAt(now);
                    return attributeValue;
                })
                .filter(Objects::nonNull)
                .toList();

        if (!values.isEmpty()) {
            contractAttributeValueRepository.saveAll(values);
        }
    }

    private List<ContractPositions> manualPositions(
            ContractTemplateVersions version
    ) {
        if (version == null || version.getPositions() == null) {
            return List.of();
        }

        Map<String, ContractPositions> uniquePositions = new LinkedHashMap<>();
        version.getPositions().stream()
                .filter(position -> "MANUAL".equalsIgnoreCase(
                        position.getValueSource()
                ))
                .forEach(position -> uniquePositions.putIfAbsent(
                        normalizeAttributeKey(position.getAttributeKey()),
                        position
                ));
        return List.copyOf(uniquePositions.values());
    }

    private Map<String, String> normalizeAttributeValues(
            Map<String, String> requestedValues
    ) {
        Map<String, String> normalized = new LinkedHashMap<>();
        if (requestedValues == null) {
            return normalized;
        }

        requestedValues.forEach((key, value) -> {
            String normalizedKey = normalizeAttributeKey(key);
            if (!normalizedKey.isBlank()) {
                normalized.put(normalizedKey, normalizeToNull(value));
            }
        });
        return normalized;
    }

    private String normalizeAttributeKey(String key) {
        return key == null ? "" : key.trim().toLowerCase(Locale.ROOT);
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
        TimelineTask task = contract.getTimelineTask();
        Timeline phase = task == null ? null : task.getTimeline();
        ContractTypes contractType = contract.getContractType();
        ContractTemplates template = contract.getContractTemplate();
        ContractTemplateVersions version = contract.getContractTemplateVersion();
        Contracts previousContract = contract.getPreviousContract();
        ContractStatus status = readStatus(contract);
        List<ContractStatusHistory> historyEntities = loadHistory(contract.getId());
        List<ContractStatusHistoryResponse> history = historyEntities.stream()
                .map(this::toHistoryResponse)
                .toList();
        Map<String, String> attributeValues = readAttributeValues(contract.getId());
        ContractDocumentRenderer.RenderedDocument renderedDocument =
                documentRenderer.render(contract, historyEntities, attributeValues);
        boolean pdfAvailable = contract.getDocumentFile() != null;
        Users user = currentUser.getCurrentUser();
        ProjectAccessResponse projectAccess = permissionAccessService
                .getCurrentUserAccess(project.getId());
        ContractAccessResponse contractAccess = new ContractAccessResponse(
                projectAccess.getProjectId(),
                projectAccess.getCurrentUserId(),
                projectAccess.isProjectCreator(),
                projectAccess.isProjectMember(),
                projectAccess.getAllowedActions(),
                projectAccess.getFullScopeActions(),
                projectAccess.getWorkScope(),
                isContractOwner(contract, user),
                isWorkflowParticipant(contract, user)
        );
        ContractWorkflowRuntimeResponse workflowRuntime =
                toWorkflowRuntimeResponse(
                        contract,
                        user,
                        projectAccess
                );

        return new ContractResponse(
                contract.getId(),
                project.getId(),
                project.getProjectName(),
                phase != null ? phase.getId() : null,
                phase != null ? phase.getTitle() : null,
                task != null ? task.getId() : null,
                task != null ? task.getTitle() : null,
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
                renderedDocument.content(),
                contract.getContractLayoutJson(),
                attributeValues,
                renderedDocument.directorSignerName(),
                renderedDocument.directorSignedAt(),
                renderedDocument.partnerSignerName(),
                renderedDocument.partnerSignedAt(),
                pdfAvailable,
                contract.getContractStatusUpdatedAt(),
                contract.getContractEndedAt(),
                contract.getContractCancellationReason(),
                previousContract != null ? previousContract.getId() : null,
                previousContract != null ? previousContract.getContractNumber() : null,
                history,
                workflowRuntime,
                contractAccess
        );
    }

    private ContractWorkflowRuntimeResponse toWorkflowRuntimeResponse(
            Contracts contract,
            Users currentUser,
            ProjectAccessResponse projectAccess
    ) {
        ContractTypeWorkflow workflow = contract.getWorkflowVersion();
        if (contract.getId() == null) {
            return null;
        }

        List<ContractWorkflowStepInstance> instances = workflowStepRepository
                .findByContractIdOrderByStepOrderAsc(contract.getId());
        ContractWorkflowStepInstance currentStep = instances.stream()
                .filter(step -> step.getStatus()
                        == ContractWorkflowStepState.PENDING)
                .findFirst()
                .orElse(null);
        List<String> allowedActions = new ArrayList<>();
        if (currentStep != null
                && currentStep.getAssignedUser() != null
                && currentStep.getAssignedUser().getId().equals(currentUser.getId())
                && userHasRole(currentUser, currentStep.getRequiredRoleCode())
                && contractActionsForCandidate(currentUser, contract.getProject().getId())
                    .containsAll(ContractWorkflowRules.requiredPermissions(
                            currentStep.getActionType()
                    ))) {
            allowedActions.add(ContractAction.COMPLETE_STEP.name());
            if (Boolean.TRUE.equals(currentStep.getCanReject())) {
                allowedActions.add(ContractAction.REJECT.name());
            }
        }
        if (!readStatus(contract).isTerminal()
                && permissionAccessService.hasAction(
                projectAccess,
                ContractProjectActions.CANCEL
        )
                && (permissionAccessService.hasFullWorkScope(
                projectAccess,
                ContractProjectActions.CANCEL
        ) || isContractOwner(contract, currentUser))) {
            allowedActions.add(ContractAction.CANCEL.name());
        }

        List<ContractWorkflowStepRuntimeResponse> steps = instances.stream()
                .map(step -> new ContractWorkflowStepRuntimeResponse(
                        step.getId(),
                        step.getStepDefinition() == null
                                ? null
                                : step.getStepDefinition().getId(),
                        step.getStepOrder(),
                        step.getStepName(),
                        step.getActionType().name(),
                        step.getRequiredRoleCode(),
                        ContractWorkflowRules.requiredPermissions(
                                step.getActionType()
                        ),
                        Boolean.TRUE.equals(step.getRequired()),
                        Boolean.TRUE.equals(step.getCanReject()),
                        step.getAssignedUser() == null
                                ? null
                                : step.getAssignedUser().getId(),
                        step.getAssignedUser() == null
                                ? null
                                : getUserDisplayName(step.getAssignedUser()),
                        step.getStatus().name(),
                        step.getActivatedAt(),
                        step.getCompletedAt(),
                        step.getComment(),
                        step.getAssignedUser() != null
                                && step.getAssignedUser().getId()
                                .equals(currentUser.getId())
                ))
                .toList();

        return new ContractWorkflowRuntimeResponse(
                workflow == null ? null : workflow.getId(),
                workflow == null ? 1 : workflow.getVersionNumber(),
                workflow == null ? "Contract approval and signing workflow" : workflow.getWorkflowName(),
                currentStep == null ? null : currentStep.getId(),
                currentStep == null ? null : currentStep.getStepName(),
                currentStep == null
                        ? null
                        : currentStep.getActionType().name(),
                currentStep == null || currentStep.getAssignedUser() == null
                        ? null
                        : currentStep.getAssignedUser().getId(),
                currentStep == null || currentStep.getAssignedUser() == null
                        ? null
                        : getUserDisplayName(currentStep.getAssignedUser()),
                List.copyOf(allowedActions),
                steps
        );
    }

    private Set<String> contractActionsForCandidate(Users user, UUID projectId) {
        return activeActionsForUser(user.getId(), projectId);
    }

    private boolean hasAllActions(
            ProjectAccessResponse projectAccess,
            List<String> requiredActions
    ) {
        return requiredActions.stream().allMatch(action ->
                permissionAccessService.hasAction(projectAccess, action)
        );
    }

    private List<ContractStatusHistory> loadHistory(UUID contractId) {
        return contractStatusHistoryRepository
                .findByContractIdOrderByChangedAtDesc(contractId);
    }

    private Map<String, String> readAttributeValues(UUID contractId) {
        Map<String, String> values = new LinkedHashMap<>();
        contractAttributeValueRepository
                .findByContractIdOrderByAttributeKeyAsc(contractId)
                .forEach(item -> values.put(
                        normalizeAttributeKey(item.getAttributeKey()),
                        item.getAttributeValue()
                ));
        return values;
    }

    private String createPdfFileName(Contracts contract) {
        String number = isBlank(contract.getContractNumber())
                ? contract.getId().toString()
                : contract.getContractNumber().trim();
        String safeNumber = number.replaceAll("[^a-zA-Z0-9._-]", "_");
        return "contract-" + safeNumber + ".pdf";
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

    private String normalizeRoleOrEmpty(String value) {
        String normalized = isBlank(value)
                ? ""
                : value.trim().toUpperCase(Locale.ROOT)
                .replace("-", "")
                .replace("_", "")
                .replace(" ", "");
        if (normalized.startsWith("ROLE") && normalized.length() > 4) {
            normalized = normalized.substring(4);
        }
        return Set.of("HOD", "HEADDEPARTMENT", "DEPARTMENTHEAD")
                .contains(normalized)
                ? "HEADOFDEPARTMENT"
                : normalized;
    }

    private boolean isContractOwner(Contracts contract, Users user) {
        if (contract == null || user == null || user.getId() == null) {
            return false;
        }

        Users creator = contract.getContractCreatedByUser();
        if (creator != null && creator.getId() != null) {
            return creator.getId().equals(user.getId());
        }

        return !isBlank(contract.getContractCreateBy())
                && contract.getContractCreateBy().trim().equalsIgnoreCase(
                getUserDisplayName(user)
        );
    }

    private boolean isWorkflowParticipant(Contracts contract, Users user) {
        return contract != null && contract.getId() != null
                && user != null && user.getId() != null
                && workflowStepRepository.existsByContractIdAndAssignedUserId(
                contract.getId(),
                user.getId()
        );
    }

    private String getUserDisplayName(Users user) {
        if (user == null) {
            throw new BadHttpException("User is not authenticated");
        }

        String fullName = (normalize(user.getFirstName()) + " "
                + normalize(user.getLastName())).trim();

        if (!fullName.isBlank()) {
            return fullName;
        }

        return requireText(user.getEmail(), "Authenticated user name is unavailable");
    }

    private ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
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
