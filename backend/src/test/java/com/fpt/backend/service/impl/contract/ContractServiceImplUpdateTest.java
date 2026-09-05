package com.fpt.backend.service.impl.contract;

import com.fpt.backend.dto.request.contract.ContractRequest;
import com.fpt.backend.dto.request.contract.ContractWorkflowAssigneeRequest;
import com.fpt.backend.entity.ContractTypeWorkflow;
import com.fpt.backend.entity.ContractTypeWorkflowStep;
import com.fpt.backend.entity.ContractTypes;
import com.fpt.backend.entity.ContractTemplateVersions;
import com.fpt.backend.entity.ContractTemplates;
import com.fpt.backend.entity.ContractWorkflowStepInstance;
import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.FileStorage;
import com.fpt.backend.entity.PermissionAction;
import com.fpt.backend.entity.Permissions;
import com.fpt.backend.entity.ProjectMember;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.entity.Role;
import com.fpt.backend.entity.Timeline;
import com.fpt.backend.entity.TimelineTask;
import com.fpt.backend.entity.UserPermission;
import com.fpt.backend.entity.UserRole;
import com.fpt.backend.entity.Users;
import com.fpt.backend.dto.response.project.ProjectAccessResponse;
import com.fpt.backend.enums.ContractWorkflowActionType;
import com.fpt.backend.enums.UserStatus;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.repository.contract.ContractAttributeValueRepository;
import com.fpt.backend.repository.contract.ContractRepository;
import com.fpt.backend.repository.contract.ContractStatusHistoryRepository;
import com.fpt.backend.repository.contract.ContractTemplateRepository;
import com.fpt.backend.repository.contract.ContractTemplateVersionRepository;
import com.fpt.backend.repository.contract.ContractTypeRepository;
import com.fpt.backend.repository.contract.ContractTypeWorkflowRepository;
import com.fpt.backend.repository.contract.ContractWorkflowStepInstanceRepository;
import com.fpt.backend.repository.permission.UserPermissionRepository;
import com.fpt.backend.repository.phase.PhaseRepository;
import com.fpt.backend.repository.phase.PhaseTaskRepository;
import com.fpt.backend.repository.project.ProjectMemberRepository;
import com.fpt.backend.repository.project.ProjectRepository;
import com.fpt.backend.repository.user.UserRepository;
import com.fpt.backend.service.impl.CloudinaryService;
import com.fpt.backend.service.interfaces.permission.IPermissionAccessService;
import com.fpt.backend.util.CurrentUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractServiceImplUpdateTest {

    @Mock
    private ContractRepository contractRepository;
    @Mock
    private ContractTypeRepository contractTypeRepository;
    @Mock
    private ContractTemplateRepository contractTemplateRepository;
    @Mock
    private ContractTemplateVersionRepository contractTemplateVersionRepository;
    @Mock
    private ContractStatusHistoryRepository contractStatusHistoryRepository;
    @Mock
    private ContractTypeWorkflowRepository contractTypeWorkflowRepository;
    @Mock
    private ContractWorkflowStepInstanceRepository workflowStepRepository;
    @Mock
    private ContractAttributeValueRepository contractAttributeValueRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectMemberRepository projectMemberRepository;
    @Mock
    private PhaseRepository phaseRepository;
    @Mock
    private PhaseTaskRepository phaseTaskRepository;
    @Mock
    private UserPermissionRepository userPermissionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ContractDocumentRenderer documentRenderer;
    @Mock
    private ContractPdfGenerator pdfGenerator;
    @Mock
    private CloudinaryService cloudinaryService;
    @Mock
    private IPermissionAccessService permissionAccessService;
    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private ContractServiceImpl contractService;

    @Test
    void updateNewContractCanChangeTypeAndReplaceWorkflowAssignees() {
        Users creator = user("Employee", "Test", "EMPLOYEE");
        Users reviewer = user("Reviewer", "Test", "MANAGER");
        ContractTypes oldType = contractType("OLD", "Old type");
        ContractTypes newType = contractType("NEW", "New type");
        ContractTypeWorkflow workflow = workflow(newType);
        Contracts contract = draftContract(creator, oldType);
        Projects project = project();
        Timeline phase = phase(project);
        TimelineTask task = task(phase);
        ContractTemplates template = template(newType);
        ContractTemplateVersions version = version(template);
        AtomicReference<List<ContractWorkflowStepInstance>> savedSteps =
                new AtomicReference<>(List.of());

        when(currentUser.getCurrentUser()).thenReturn(creator);
        when(contractRepository.findById(contract.getId()))
                .thenReturn(Optional.of(contract));
        when(contractRepository.save(any(Contracts.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(contractTypeRepository.findById(newType.getId()))
                .thenReturn(Optional.of(newType));
        when(projectRepository.findById(project.getId()))
                .thenReturn(Optional.of(project));
        when(phaseRepository.findById(phase.getId()))
                .thenReturn(Optional.of(phase));
        when(phaseTaskRepository.findById(task.getId()))
                .thenReturn(Optional.of(task));
        when(contractTemplateRepository.findById(template.getId()))
                .thenReturn(Optional.of(template));
        when(contractTemplateVersionRepository.findById(version.getId()))
                .thenReturn(Optional.of(version));
        when(contractTypeWorkflowRepository
                .findFirstByContractTypeIdAndActiveTrueOrderByVersionNumberDesc(
                        newType.getId()
                )).thenReturn(Optional.of(workflow));
        when(projectMemberRepository.findByProjectId(project.getId()))
                .thenReturn(List.of(
                        ProjectMember.builder().project(project).user(creator).build(),
                        ProjectMember.builder().project(project).user(reviewer).build()
                ));
        when(userPermissionRepository.findActiveByUserIdAndProjectId(
                creator.getId(), project.getId()
        )).thenReturn(permission(
                creator,
                ContractProjectActions.VIEW,
                ContractProjectActions.CREATE,
                ContractProjectActions.SUBMIT
        ));
        when(userPermissionRepository.findActiveByUserIdAndProjectId(
                reviewer.getId(), project.getId()
        )).thenReturn(permission(
                reviewer,
                ContractProjectActions.VIEW,
                ContractProjectActions.APPROVE
        ));
        when(permissionAccessService.getCurrentUserAccess(project.getId()))
                .thenReturn(new ProjectAccessResponse(
                        project.getId(),
                        creator.getId(),
                        false,
                        true,
                        false,
                        List.of(
                                ContractProjectActions.VIEW,
                                ContractProjectActions.EDIT
                        ),
                        List.of(),
                        "OWN"
                ));
        when(workflowStepRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<ContractWorkflowStepInstance> steps = invocation.getArgument(0);
            savedSteps.set(List.copyOf(steps));
            return steps;
        });
        when(workflowStepRepository.findByContractIdOrderByStepOrderAsc(
                contract.getId()
        )).thenAnswer(invocation -> savedSteps.get());
        when(contractStatusHistoryRepository
                .findByContractIdOrderByChangedAtDesc(contract.getId()))
                .thenReturn(List.of());
        when(contractAttributeValueRepository
                .findByContractIdOrderByAttributeKeyAsc(contract.getId()))
                .thenReturn(List.of());
        ContractDocumentRenderer.RenderedDocument renderedDocument =
                new ContractDocumentRenderer.RenderedDocument(
                        "Updated terms", null, null, null, null
                );
        when(documentRenderer.render(
                any(Contracts.class), anyList(), anyMap()
        )).thenReturn(renderedDocument);
        when(pdfGenerator.generate(
                any(Contracts.class), eq(renderedDocument)
        )).thenReturn(new byte[]{1, 2, 3});
        FileStorage storedPdf = new FileStorage();
        storedPdf.setId(UUID.randomUUID());
        when(cloudinaryService.uploadPdfAndSave(
                any(byte[].class), anyString(), eq(creator)
        )).thenReturn(storedPdf);

        var response = contractService.updateContract(
                contract.getId(),
                projectRequest(
                        newType.getId(),
                        project.getId(),
                        phase.getId(),
                        task.getId(),
                        template.getId(),
                        version.getId(),
                        new ContractWorkflowAssigneeRequest(
                                null, 2, reviewer.getId()
                        )
                )
        );

        assertThat(contract.getContractType()).isSameAs(newType);
        assertThat(contract.getWorkflowVersion()).isSameAs(workflow);
        assertThat(contract.getProject()).isSameAs(project);
        assertThat(contract.getTimelineContract().getTimeline()).isSameAs(phase);
        assertThat(contract.getTimelineTask()).isSameAs(task);
        assertThat(contract.getContractTemplate()).isSameAs(template);
        assertThat(contract.getContractTemplateVersion()).isSameAs(version);
        assertThat(contract.getContractTitle()).isEqualTo("Updated contract");
        assertThat(response.workflowRuntime().workflowVersionId())
                .isEqualTo(workflow.getId());
        assertThat(savedSteps.get())
                .extracting(step -> step.getAssignedUser().getId())
                .containsExactly(creator.getId(), reviewer.getId());
        verify(workflowStepRepository).deleteAllByContractId(contract.getId());
        verify(permissionAccessService).requireAction(
                project.getId(), ContractProjectActions.CREATE
        );
    }

    @Test
    void updateSubmittedContractIsRejectedBeforeChangingFields() {
        Users creator = user("Employee", "Test", "EMPLOYEE");
        ContractTypes type = contractType("TYPE", "Contract type");
        Contracts contract = draftContract(creator, type);
        contract.setContractStatus("PENDING_APPROVAL");

        when(currentUser.getCurrentUser()).thenReturn(creator);
        when(contractRepository.findById(contract.getId()))
                .thenReturn(Optional.of(contract));

        assertThatThrownBy(() -> contractService.updateContract(
                contract.getId(), request(type.getId(), null)
        )).isInstanceOf(BadHttpException.class)
                .hasMessageContaining("Only NEW contracts can be edited");

        verify(contractTypeRepository, never()).findById(any());
        verify(workflowStepRepository, never()).deleteAllByContractId(any());
    }

    private ContractRequest request(
            UUID contractTypeId,
            ContractWorkflowAssigneeRequest assignee
    ) {
        return projectRequest(
                contractTypeId,
                null,
                null,
                null,
                null,
                null,
                assignee
        );
    }

    private ContractRequest projectRequest(
            UUID contractTypeId,
            UUID projectId,
            UUID phaseId,
            UUID taskId,
            UUID templateId,
            UUID templateVersionId,
            ContractWorkflowAssigneeRequest assignee
    ) {
        return new ContractRequest(
                projectId,
                phaseId,
                taskId,
                contractTypeId,
                templateId,
                templateVersionId,
                "CON-EDIT-001",
                "Updated contract",
                "NEW",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2027, 9, 1),
                "Employee Test",
                null,
                "Updated terms",
                null,
                false,
                null,
                null,
                null,
                "Employee Test",
                "EMPLOYEE",
                Map.of(),
                assignee == null ? List.of() : List.of(assignee)
        );
    }

    private Contracts draftContract(Users creator, ContractTypes type) {
        Contracts contract = new Contracts();
        contract.setId(UUID.randomUUID());
        contract.setContractNumber("CON-OLD-001");
        contract.setContractTitle("Old contract");
        contract.setContractStatus("NEW");
        contract.setContractCreateBy("Employee Test");
        contract.setContractCreatedByUser(creator);
        contract.setEffectiveDate(LocalDate.of(2026, 8, 1));
        contract.setExpirationDate(LocalDate.of(2027, 8, 1));
        contract.setContractType(type);
        contract.setContractContent("Old terms");
        contract.setWorkflowStepInstances(new ArrayList<>());
        return contract;
    }

    private ContractTypes contractType(String code, String name) {
        ContractTypes type = new ContractTypes();
        type.setId(UUID.randomUUID());
        type.setContractTypeCode(code);
        type.setContractTypeName(name);
        return type;
    }

    private Projects project() {
        Projects project = new Projects();
        project.setId(UUID.randomUUID());
        project.setProjectCode("PRJ-EDIT");
        project.setProjectName("Editable project");
        return project;
    }

    private Timeline phase(Projects project) {
        Timeline phase = new Timeline();
        phase.setId(UUID.randomUUID());
        phase.setTitle("Editable phase");
        phase.setProject(project);
        return phase;
    }

    private TimelineTask task(Timeline phase) {
        TimelineTask task = new TimelineTask();
        task.setId(UUID.randomUUID());
        task.setTitle("Editable task");
        task.setTimeline(phase);
        return task;
    }

    private ContractTemplates template(ContractTypes type) {
        ContractTemplates template = new ContractTemplates();
        template.setId(UUID.randomUUID());
        template.setContractTemplateName("Editable template");
        template.setContractType(type);
        return template;
    }

    private ContractTemplateVersions version(ContractTemplates template) {
        ContractTemplateVersions version = new ContractTemplateVersions();
        version.setId(UUID.randomUUID());
        version.setContractTemplate(template);
        version.setVersionNumber(1);
        version.setVersionName("V1");
        version.setTemplateContent("Updated terms");
        version.setPositions(List.of());
        return version;
    }

    private UserPermission permission(Users user, String... actionCodes) {
        LinkedHashSet<PermissionAction> actions = new LinkedHashSet<>();
        Arrays.stream(actionCodes).forEach(actionCode -> actions.add(
                PermissionAction.builder()
                        .actionCode(actionCode)
                        .actionName(actionCode)
                        .resourceCode("CONTRACT")
                        .displayOrder(1)
                        .build()
        ));
        Permissions permission = new Permissions();
        permission.setActions(actions);
        permission.setStatus(true);
        return UserPermission.builder()
                .user(user)
                .permission(permission)
                .build();
    }

    private ContractTypeWorkflow workflow(ContractTypes type) {
        ContractTypeWorkflow workflow = new ContractTypeWorkflow();
        workflow.setId(UUID.randomUUID());
        workflow.setContractType(type);
        workflow.setVersionNumber(2);
        workflow.setWorkflowName("Updated workflow");
        workflow.setActive(true);

        ContractTypeWorkflowStep create = step(
                workflow, 1, "Create contract",
                ContractWorkflowActionType.CREATE, "EMPLOYEE"
        );
        ContractTypeWorkflowStep approve = step(
                workflow, 2, "Review contract",
                ContractWorkflowActionType.APPROVE, "MANAGER"
        );
        workflow.setSteps(List.of(create, approve));
        return workflow;
    }

    private ContractTypeWorkflowStep step(
            ContractTypeWorkflow workflow,
            int order,
            String name,
            ContractWorkflowActionType action,
            String role
    ) {
        ContractTypeWorkflowStep step = new ContractTypeWorkflowStep();
        step.setId(UUID.randomUUID());
        step.setWorkflow(workflow);
        step.setStepOrder(order);
        step.setStepName(name);
        step.setActionType(action);
        step.setRequiredRoleCode(role);
        step.setRequired(true);
        step.setCanReject(action != ContractWorkflowActionType.CREATE);
        return step;
    }

    private Users user(String firstName, String lastName, String roleCode) {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setRoleCode(roleCode);
        role.setRoleName(roleCode);

        Users user = new Users();
        user.setId(UUID.randomUUID());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(firstName.toLowerCase() + "@example.com");
        user.setStatus(UserStatus.ACTIVE);
        user.setUserRoles(List.of(UserRole.builder()
                .user(user)
                .role(role)
                .build()));
        return user;
    }
}
