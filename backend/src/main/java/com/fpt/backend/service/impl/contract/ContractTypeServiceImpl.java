package com.fpt.backend.service.impl.contract;

import com.fpt.backend.dto.request.contract.ContractTypeRequest;
import com.fpt.backend.dto.request.contract.ContractWorkflowStepRequest;
import com.fpt.backend.dto.response.contract.ContractRoleOptionResponse;
import com.fpt.backend.dto.response.contract.ContractTypeResponse;
import com.fpt.backend.dto.response.contract.ContractWorkflowDefinitionResponse;
import com.fpt.backend.dto.response.contract.ContractWorkflowOptionsResponse;
import com.fpt.backend.dto.response.contract.ContractWorkflowStepDefinitionResponse;
import com.fpt.backend.entity.ContractTypeWorkflow;
import com.fpt.backend.entity.ContractTypeWorkflowStep;
import com.fpt.backend.entity.ContractTypes;
import com.fpt.backend.entity.Users;
import com.fpt.backend.enums.ContractWorkflowActionType;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.exception.NotFoundException;
import com.fpt.backend.repository.contract.ContractRepository;
import com.fpt.backend.repository.contract.ContractTemplateRepository;
import com.fpt.backend.repository.contract.ContractTypeRepository;
import com.fpt.backend.repository.contract.ContractTypeWorkflowRepository;
import com.fpt.backend.repository.role.RoleRepository;
import com.fpt.backend.service.interfaces.contract.ContractTypeService;
import com.fpt.backend.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractTypeServiceImpl implements ContractTypeService {
        private static final String DEFAULT_STATUS = "Active";

        private final ContractTypeRepository contractTypeRepository;
        private final ContractTemplateRepository contractTemplateRepository;
        private final ContractRepository contractRepository;
        private final ContractTypeWorkflowRepository workflowRepository;
        private final RoleRepository roleRepository;

        @Override
        @Transactional(readOnly = true)
        public List<ContractTypeResponse> getContractTypes() {
                return contractTypeRepository.findAllByOrderByContractTypeNameAsc()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public ContractTypeResponse getContractTypeById(UUID id) {
                return toResponse(findContractType(id));
        }

        @Override
        @Transactional(readOnly = true)
        public ContractWorkflowOptionsResponse getWorkflowOptions() {
                List<ContractRoleOptionResponse> roles = roleRepository.findAllForSelection()
                                .stream()
                                .map(role -> new ContractRoleOptionResponse(
                                                role.getId(),
                                                role.getRoleCode(),
                                                role.getRoleName()))
                                .toList();

                return new ContractWorkflowOptionsResponse(
                                List.of(
                                                ContractWorkflowActionType.CREATE.name(),
                                                ContractWorkflowActionType.APPROVE.name(),
                                                ContractWorkflowActionType.SIGN.name()),
                                roles);
        }

        @Override
        @Transactional
        public ContractTypeResponse createContractType(ContractTypeRequest request) {
                validateRequest(request);
                String code = requireText(request.contractTypeCode(), "Contract type code is required");

                if (contractTypeRepository.existsByContractTypeCodeIgnoreCase(code)) {
                        throw new BadHttpException("Contract type code already exists");
                }

                LocalDateTime now = LocalDateTime.now();
                ContractTypes contractType = new ContractTypes();
                applyRequest(contractType, request);
                contractType.setCreatedAt(now);
                contractType.setUpdatedAt(now);

                ContractTypes savedType = contractTypeRepository.save(contractType);
                List<ContractWorkflowStepRequest> requestedSteps = request.workflowSteps();
                if (requestedSteps == null || requestedSteps.isEmpty()) {
                        requestedSteps = defaultWorkflowSteps();
                }
                createWorkflowVersion(savedType, request.workflowName(), requestedSteps);
                return toResponse(savedType);
        }

        @Override
        @Transactional
        public ContractTypeResponse updateContractType(UUID id, ContractTypeRequest request) {
                validateRequest(request);
                ContractTypes contractType = findContractType(id);
                String code = requireText(request.contractTypeCode(), "Contract type code is required");

                if (contractTypeRepository.existsByContractTypeCodeIgnoreCaseAndIdNot(code, id)) {
                        throw new BadHttpException("Contract type code already exists");
                }

                String originalCreator = contractType.getCreatedBy();
                applyRequest(contractType, request);
                if (isBlank(request.createdBy())) {
                        contractType.setCreatedBy(originalCreator);
                }
                contractType.setUpdatedAt(LocalDateTime.now());

                ContractTypes savedType = contractTypeRepository.save(contractType);
                if (request.workflowSteps() != null
                                && !request.workflowSteps().isEmpty()
                                && !workflowMatches(
                                                findActiveWorkflow(savedType.getId()),
                                                request.workflowName(),
                                                request.workflowSteps())) {
                        createWorkflowVersion(
                                        savedType,
                                        request.workflowName(),
                                        request.workflowSteps());
                }
                return toResponse(savedType);
        }

        @Override
        @Transactional
        public void deleteContractType(UUID id) {
                ContractTypes contractType = findContractType(id);
                long templateCount = contractTemplateRepository.countByContractTypeId(id);
                long contractCount = contractRepository.countByContractTypeId(id);

                if (templateCount > 0 || contractCount > 0) {
                        throw new BadHttpException(
                                        "Cannot delete a contract type that is used by templates or contracts");
                }

                contractTypeRepository.delete(contractType);
        }

        private void validateRequest(ContractTypeRequest request) {
                if (request == null) {
                        throw new BadHttpException("Contract type information is required");
                }

                requireText(request.contractTypeCode(), "Contract type code is required");
                requireText(request.contractTypeName(), "Contract type name is required");

                if (request.validityDays() != null && request.validityDays() <= 0) {
                        throw new BadHttpException("Default validity must be greater than zero");
                }
        }

        private void applyRequest(ContractTypes contractType, ContractTypeRequest request) {
                contractType.setContractTypeCode(
                                requireText(request.contractTypeCode(), "Contract type code is required"));
                contractType.setContractTypeName(
                                requireText(request.contractTypeName(), "Contract type name is required"));
                contractType.setDescription(normalizeToNull(request.description()));
                contractType.setValidityDays(request.validityDays());
                contractType.setCategory(normalizeToNull(request.category()));
                contractType.setStatus(
                                isBlank(request.status()) ? DEFAULT_STATUS : request.status().trim());
                contractType.setCreatedBy(normalizeToNull(request.createdBy()));
        }

        private ContractTypes findContractType(UUID id) {
                if (id == null) {
                        throw new BadHttpException("Contract type id is required");
                }

                return contractTypeRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException(
                                                "Contract type not found with id: " + id));
        }

        private ContractTypeResponse toResponse(ContractTypes contractType) {
                UUID id = contractType.getId();
                ContractTypeWorkflow activeWorkflow = findActiveWorkflow(id);

                return new ContractTypeResponse(
                                id,
                                contractType.getContractTypeCode(),
                                contractType.getContractTypeName(),
                                contractType.getDescription(),
                                contractType.getValidityDays(),
                                contractType.getCategory(),
                                contractType.getStatus(),
                                contractType.getCreatedBy(),
                                contractType.getCreatedAt(),
                                contractType.getUpdatedAt(),
                                contractTemplateRepository.countByContractTypeId(id),
                                contractRepository.countByContractTypeId(id),
                                toWorkflowResponse(activeWorkflow));
        }

        private ContractTypeWorkflow findActiveWorkflow(UUID contractTypeId) {
                return workflowRepository
                                .findFirstByContractTypeIdAndActiveTrueOrderByVersionNumberDesc(
                                                contractTypeId)
                                .orElse(null);
        }

        // Đã chuyển kiểu trả về thành void vì giá trị trả về không được sử dụng
        private void createWorkflowVersion(
                        ContractTypes contractType,
                        String requestedWorkflowName,
                        List<ContractWorkflowStepRequest> requestedSteps) {
                List<NormalizedWorkflowStep> normalizedSteps = normalizeWorkflowSteps(
                                requestedSteps);
                List<ContractTypeWorkflow> existingVersions = workflowRepository
                                .findByContractTypeIdOrderByVersionNumberDesc(contractType.getId());
                existingVersions.forEach(workflow -> workflow.setActive(false));
                if (!existingVersions.isEmpty()) {
                        workflowRepository.saveAll(existingVersions);
                }

                int versionNumber = workflowRepository
                                .findLatestVersionNumber(contractType.getId()) + 1;
                ContractTypeWorkflow workflow = ContractTypeWorkflow.builder()
                                .contractType(contractType)
                                .versionNumber(versionNumber)
                                .workflowName(isBlank(requestedWorkflowName)
                                                ? contractType.getContractTypeName() + " workflow"
                                                : requestedWorkflowName.trim())
                                .active(true)
                                .createdBy(contractType.getCreatedBy())
                                .createdAt(LocalDateTime.now())
                                .steps(new ArrayList<>())
                                .build();

                normalizedSteps.forEach(step -> workflow.getSteps().add(
                                ContractTypeWorkflowStep.builder()
                                                .workflow(workflow)
                                                .stepOrder(step.stepOrder())
                                                .stepName(step.stepName())
                                                .actionType(step.actionType())
                                                .requiredRoleCode(step.requiredRoleCode())
                                                .required(step.required())
                                                .canReject(step.canReject())
                                                .build()));

                // Đã bỏ chữ 'return' ở đây
                workflowRepository.save(workflow);
        }

        private List<NormalizedWorkflowStep> normalizeWorkflowSteps(
                        List<ContractWorkflowStepRequest> requestedSteps) {
                if (requestedSteps == null || requestedSteps.size() < 2) {
                        throw new BadHttpException(
                                        "A contract type workflow requires at least two steps");
                }

                List<ContractWorkflowStepRequest> orderedSteps = new ArrayList<>(requestedSteps);
                orderedSteps.sort(Comparator.comparing(
                                step -> step.stepOrder() == null
                                                ? Integer.MAX_VALUE
                                                : step.stepOrder()));

                List<NormalizedWorkflowStep> normalized = new ArrayList<>();
                int createStepCount = 0;
                for (int index = 0; index < orderedSteps.size(); index++) {
                        ContractWorkflowStepRequest step = orderedSteps.get(index);
                        if (step == null) {
                                throw new BadHttpException("Workflow step information is required");
                        }

                        ContractWorkflowActionType actionType;
                        try {
                                actionType = ContractWorkflowActionType.fromValue(step.actionType());
                        } catch (IllegalArgumentException exception) {
                                throw new BadHttpException(exception.getMessage());
                        }
                        if (actionType == ContractWorkflowActionType.APPROVE_AND_SIGN) {
                                throw new BadHttpException(
                                                "APPROVE_AND_SIGN is no longer supported. Add one APPROVE step followed by one SIGN step");
                        }
                        if (actionType == ContractWorkflowActionType.CREATE) {
                                createStepCount++;
                        }

                        normalized.add(new NormalizedWorkflowStep(
                                        index + 1,
                                        requireText(step.stepName(), "Workflow step name is required"),
                                        actionType,
                                        normalizeRole(step.requiredRoleCode()),
                                        step.required() == null || step.required(),
                                        Boolean.TRUE.equals(step.canReject())
                                                        && actionType != ContractWorkflowActionType.CREATE));
                }

                // Đã đổi .get(0) thành .getFirst()
                if (normalized.getFirst().actionType() != ContractWorkflowActionType.CREATE
                                || createStepCount != 1) {
                        throw new BadHttpException(
                                        "A workflow must start with exactly one CREATE step");
                }

                int ceoSignIndex = -1;
                for (int index = 0; index < normalized.size(); index++) {
                        NormalizedWorkflowStep step = normalized.get(index);
                        if (step.actionType() != ContractWorkflowActionType.SIGN) {
                                continue;
                        }
                        if ("CEO".equals(step.requiredRoleCode())) {
                                ceoSignIndex = index;
                        }
                }
                for (int index = 0; index < normalized.size(); index++) {
                        NormalizedWorkflowStep step = normalized.get(index);
                        if (step.actionType() == ContractWorkflowActionType.SIGN
                                        && !"CEO".equals(step.requiredRoleCode())
                                        && (ceoSignIndex < 0 || ceoSignIndex > index)) {
                                throw new BadHttpException(
                                                "The CEO SIGN step must come before every other signer");
                        }
                }

                return List.copyOf(normalized);
        }

        private boolean workflowMatches(
                        ContractTypeWorkflow current,
                        String workflowName,
                        List<ContractWorkflowStepRequest> requestedSteps) {
                if (current == null) {
                        return false;
                }

                List<NormalizedWorkflowStep> normalized = normalizeWorkflowSteps(requestedSteps);
                String requestedName = isBlank(workflowName)
                                ? current.getContractType().getContractTypeName() + " workflow"
                                : workflowName.trim();
                if (!requestedName.equals(current.getWorkflowName())
                                || current.getSteps() == null
                                || current.getSteps().size() != normalized.size()) {
                        return false;
                }

                List<ContractTypeWorkflowStep> currentSteps = current.getSteps().stream()
                                .sorted(Comparator.comparing(ContractTypeWorkflowStep::getStepOrder))
                                .toList();
                for (int index = 0; index < normalized.size(); index++) {
                        NormalizedWorkflowStep expected = normalized.get(index);
                        ContractTypeWorkflowStep actual = currentSteps.get(index);
                        if (!expected.stepOrder().equals(actual.getStepOrder())
                                        || !expected.stepName().equals(actual.getStepName())
                                        || expected.actionType() != actual.getActionType()
                                        || !expected.requiredRoleCode().equals(
                                                        normalizeRole(actual.getRequiredRoleCode()))
                                        || expected.required() != Boolean.TRUE.equals(actual.getRequired())
                                        || expected.canReject() != Boolean.TRUE.equals(actual.getCanReject())) {
                                return false;
                        }
                }
                return true;
        }

        private ContractWorkflowDefinitionResponse toWorkflowResponse(
                        ContractTypeWorkflow workflow) {
                if (workflow == null) {
                        return null;
                }

                List<ContractWorkflowStepDefinitionResponse> steps = workflow.getSteps()
                                .stream()
                                .sorted(Comparator.comparing(ContractTypeWorkflowStep::getStepOrder))
                                .map(step -> new ContractWorkflowStepDefinitionResponse(
                                                step.getId(),
                                                step.getStepOrder(),
                                                step.getStepName(),
                                                step.getActionType().name(),
                                                step.getRequiredRoleCode(),
                                                ContractWorkflowRules.requiredPermissions(step.getActionType()),
                                                Boolean.TRUE.equals(step.getRequired()),
                                                Boolean.TRUE.equals(step.getCanReject())))
                                .toList();

                return new ContractWorkflowDefinitionResponse(
                                workflow.getId(),
                                workflow.getVersionNumber(),
                                workflow.getWorkflowName(),
                                Boolean.TRUE.equals(workflow.getActive()),
                                workflow.getCreatedBy(),
                                workflow.getCreatedAt(),
                                steps);
        }

        private List<ContractWorkflowStepRequest> defaultWorkflowSteps() {
                return List.of(
                                new ContractWorkflowStepRequest(
                                                1, "Prepare and submit", "CREATE", "EMPLOYEE", true, false),
                                new ContractWorkflowStepRequest(
                                                2, "Head of Department approval", "APPROVE", "HEADOFDEPARTMENT", true,
                                                true),
                                new ContractWorkflowStepRequest(
                                                3, "CEO final approval", "APPROVE", "CEO", true, true),
                                new ContractWorkflowStepRequest(
                                                4, "CEO electronic signature", "SIGN", "CEO", true, true),
                                new ContractWorkflowStepRequest(
                                                5, "Assigned representative signature", "SIGN", "EMPLOYEE", true,
                                                true));
        }

        private String normalizeRole(String value) {
                String role = requireText(value, "Workflow role is required");
                return role.toUpperCase(Locale.ROOT)
                                .replace('-', '_')
                                .replace(' ', '_');
        }

        private record NormalizedWorkflowStep(
                        Integer stepOrder,
                        String stepName,
                        ContractWorkflowActionType actionType,
                        String requiredRoleCode,
                        boolean required,
                        boolean canReject) {
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
