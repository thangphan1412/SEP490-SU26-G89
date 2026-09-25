//
//package com.fpt.backend.service.impl.contract;
//
//import com.fpt.backend.dto.request.contract.ContractRequest;
//import com.fpt.backend.dto.response.contract.ContractResponse;
//import com.fpt.backend.entity.*;
//import com.fpt.backend.enums.ContractWorkflowActionType;
//import com.fpt.backend.enums.UserStatus;
//import com.fpt.backend.exception.BadHttpException;
//import com.fpt.backend.exception.NotFoundException;
//import com.fpt.backend.repository.contract.ContractAttributeValueRepository;
//import com.fpt.backend.repository.contract.ContractRepository;
//import com.fpt.backend.repository.contract.ContractStatusHistoryRepository;
//import com.fpt.backend.repository.contract.ContractTemplateRepository;
//import com.fpt.backend.repository.contract.ContractTemplateVersionRepository;
//import com.fpt.backend.repository.contract.ContractTypeRepository;
//import com.fpt.backend.repository.contract.ContractTypeWorkflowRepository;
//import com.fpt.backend.repository.contract.ContractWorkflowStepInstanceRepository;
//
//import com.fpt.backend.repository.electronicSignature.ElectronicSignatureRepository;
//import com.fpt.backend.repository.permission.UserPermissionRepository;
//import com.fpt.backend.repository.phase.PhaseRepository;
//import com.fpt.backend.repository.phase.PhaseTaskRepository;
//import com.fpt.backend.repository.project.ProjectMemberRepository;
//import com.fpt.backend.repository.project.ProjectRepository;
//import com.fpt.backend.repository.signature.SignatureRepository;
//
//import com.fpt.backend.repository.user.UserRepository;
//
//import com.fpt.backend.service.impl.CloudinaryService;
//import com.fpt.backend.service.impl.signature.ContractSigningService;
//import com.fpt.backend.service.impl.signature.PadesVerificationService;
//import com.fpt.backend.service.interfaces.permission.IPermissionAccessService;
//import com.fpt.backend.util.CurrentUser;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.context.ApplicationEventPublisher;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.UUID;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class ContractServiceImplTest {
//
//    @Mock
//    private ContractRepository contractRepository;
//
//    @Mock
//    private ProjectRepository projectRepository;
//
//    @Mock
//    private ContractTypeRepository contractTypeRepository;
//
//    @Mock
//    private ContractTemplateRepository contractTemplateRepository;
//
//    @Mock
//    private ContractTemplateVersionRepository contractTemplateVersionRepository;
//
//    @Mock
//    private ContractStatusHistoryRepository contractStatusHistoryRepository;
//
//    @Mock
//    private ContractTypeWorkflowRepository contractTypeWorkflowRepository;
//
//    @Mock
//    private ContractWorkflowStepInstanceRepository workflowStepRepository;
//
//    @Mock
//    private ContractAttributeValueRepository contractAttributeValueRepository;
//
//    @Mock
//    private ProjectMemberRepository projectMemberRepository;
//
//    @Mock
//    private PhaseRepository phaseRepository;
//
//    @Mock
//    private PhaseTaskRepository phaseTaskRepository;
//
//    @Mock
//    private UserPermissionRepository userPermissionRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private ContractTemplateLayoutMapper layoutMapper;
//
//    @Mock
//    private ContractDocumentRenderer documentRenderer;
//
//    @Mock
//    private ContractPdfGenerator pdfGenerator;
//
//    @Mock
//    private ElectronicSignatureRepository electronicSignatureRepository;
//
//    @Mock
//    private SignatureRepository signatureRepository;
//
//    @Mock
//    private ContractSigningService contractSigningService;
//
//    @Mock
//    private CloudinaryService cloudinaryService;
//
//    @Mock
//    private IPermissionAccessService permissionAccessService;
//
//    @Mock
//    private CurrentUser currentUser;
//
//    @Mock
//    private ApplicationEventPublisher eventPublisher;
//
//    @Mock
//    private PadesVerificationService padesVerificationService;
//
//    @InjectMocks
//    private ContractServiceImpl contractService;
//
//    private Users actor;
//
//    @BeforeEach
//    void setUp() {
//        actor = mock(Users.class);
//
//        UUID actorId = UUID.randomUUID();
//
//        when(actor.getId()).thenReturn(actorId);
//        when(actor.getFirstName()).thenReturn("Test");
//        when(actor.getLastName()).thenReturn("User");
//        when(actor.getEmail()).thenReturn("test.user@example.com");
//
//        when(currentUser.getCurrentUser()).thenReturn(actor);
//    }
//
//    // ============================================================
//    // Helper tạo ContractRequest hợp lệ
//    // ============================================================
//
//    private ContractRequest validRequest() {
//        return new ContractRequest(
//                null,                           // projectId
//                null,                           // phaseId
//                null,                           // taskId
//                UUID.randomUUID(),              // contractTypeId
//                null,                           // contractTemplateId
//                null,                           // contractTemplateVersionId
//                "HD-001",                       // contractNumber
//                "Test Contract",                // contractTitle
//                null,                           // contractStatus
//                LocalDate.now(),                // effectiveDate
//                LocalDate.now().plusDays(30),   // expirationDate
//                null,                           // contractCreatedBy
//                null,                           // contractCreatedAt
//                "Contract content",             // contractContent
//                null,                           // contractLayoutJson
//                false,                          // saveAsTemplateVersion
//                null,                           // templateVersionName
//                null,                           // templateVersionNote
//                null,                           // previousContractId
//                null,                           // actorName
//                null,                           // actorRole
//                Map.of(),                       // attributeValues
//                List.of()                       // workflowAssignees
//        );
//    }
//
//    private ContractRequest requestWith(
//            String contractNumber,
//            String contractTitle,
//            LocalDate effectiveDate,
//            LocalDate expirationDate,
//            UUID contractTypeId
//    ) {
//        return new ContractRequest(
//                null,                   // projectId
//                null,                   // phaseId
//                null,                   // taskId
//                contractTypeId,         // contractTypeId
//                null,                   // contractTemplateId
//                null,                   // contractTemplateVersionId
//                contractNumber,         // contractNumber
//                contractTitle,          // contractTitle
//                null,                   // contractStatus
//                effectiveDate,          // effectiveDate
//                expirationDate,         // expirationDate
//                null,                   // contractCreatedBy
//                null,                   // contractCreatedAt
//                null,                   // contractContent
//                null,                   // contractLayoutJson
//                false,                  // saveAsTemplateVersion
//                null,                   // templateVersionName
//                null,                   // templateVersionNote
//                null,                   // previousContractId
//                null,                   // actorName
//                null,                   // actorRole
//                null,                   // attributeValues
//                null                    // workflowAssignees
//        );
//    }
//
//    // ============================================================
//    // 1. Request null
//    // ============================================================
//
//    @Test
//    void createContract_shouldThrowException_whenRequestIsNull() {
//
//        BadHttpException exception = assertThrows(
//                BadHttpException.class,
//                () -> contractService.createContract(null)
//        );
//
//        assertEquals(
//                "Contract information is required",
//                exception.getMessage()
//        );
//
//        verifyNoInteractions(
//                currentUser,
//                contractRepository,
//                permissionAccessService
//        );
//    }
//
//    // ============================================================
//    // 2. happy case: contract type not found
//    // ============================================================
//
//    @Test
//    void createContract_shouldCreateSuccessfully_whenRequestIsValid() {
//
//        // =========================================================
//        // 1. Arrange: actor hiện tại
//        // =========================================================
//
//        UUID actorId = UUID.randomUUID();
//
//        when(actor.getId()).thenReturn(actorId);
//        when(actor.getFirstName()).thenReturn("Test");
//        when(actor.getLastName()).thenReturn("User");
//        when(actor.getEmail()).thenReturn("test.user@example.com");
//
//        // Nếu service kiểm tra trạng thái user
//        when(actor.getStatus()).thenReturn(UserStatus.ACTIVE);
//
//
//        // =========================================================
//        // 2. Contract type
//        // =========================================================
//
//        UUID contractTypeId = UUID.randomUUID();
//
//        ContractTypes contractType = mock(ContractTypes.class);
//
//        when(contractType.getId()).thenReturn(contractTypeId);
//
//        when(contractTypeRepository.findById(contractTypeId))
//                .thenReturn(Optional.of(contractType));
//
//
//        // =========================================================
//        // 3. Workflow step definitions
//        // =========================================================
//
//        ContractTypeWorkflow workflow =
//                mock(ContractTypeWorkflow.class);
//
//        ContractTypeWorkflowStep createStep =
//                mock(ContractTypeWorkflowStep.class);
//
//        ContractTypeWorkflowStep approveStep =
//                mock(ContractTypeWorkflowStep.class);
//
//
//        when(workflow.getSteps())
//                .thenReturn(List.of(createStep, approveStep));
//
//        when(createStep.getStepOrder())
//                .thenReturn(1);
//
//        when(approveStep.getStepOrder())
//                .thenReturn(2);
//
//
//        when(createStep.getStepName())
//                .thenReturn("Create contract");
//
//        when(approveStep.getStepName())
//                .thenReturn("Approve contract");
//
//
//        when(createStep.getRequiredRoleCode())
//                .thenReturn("EMPLOYEE");
//
//        when(approveStep.getRequiredRoleCode())
//                .thenReturn("EMPLOYEE");
//
//
//        when(createStep.getRequired())
//                .thenReturn(true);
//
//        when(approveStep.getRequired())
//                .thenReturn(true);
//
//
//        when(createStep.getCanReject())
//                .thenReturn(false);
//
//        when(approveStep.getCanReject())
//                .thenReturn(false);
//
//
//        when(createStep.getActionType())
//                .thenReturn(ContractWorkflowActionType.CREATE);
//
//        when(approveStep.getActionType())
//                .thenReturn(ContractWorkflowActionType.APPROVE);
//
//
//        // Nếu workflow step có department bắt buộc,
//        // mock department tương ứng tại đây.
//        when(createStep.getRequiredDepartment())
//                .thenReturn(null);
//
//        when(approveStep.getRequiredDepartment())
//                .thenReturn(null);
//
//
//        when(contractTypeWorkflowRepository
//                .findFirstByContractTypeIdAndActiveTrueOrderByVersionNumberDesc(
//                        contractTypeId
//                ))
//                .thenReturn(Optional.of(workflow));
//
//
//        // =========================================================
//        // 4. Request hợp lệ
//        // =========================================================
//
//        ContractRequest request = requestWith(
//                "HD-001",
//                "Test Contract",
//                LocalDate.now(),
//                LocalDate.now().plusDays(30),
//                contractTypeId
//        );
//
//
//        // =========================================================
//        // 5. Save contract
//        // =========================================================
//
//        Contracts savedContract = new Contracts();
//
//        UUID contractId = UUID.randomUUID();
//
//        savedContract.setId(contractId);
//
//        when(contractRepository.save(any(Contracts.class)))
//                .thenAnswer(invocation -> {
//                    Contracts contract = invocation.getArgument(0);
//
//                    if (contract.getId() == null) {
//                        contract.setId(contractId);
//                    }
//
//                    return contract;
//                });
//
//
//        // =========================================================
//        // 6. Các repository/service phụ
//        // =========================================================
//
//        when(userRepository.findAll())
//                .thenReturn(List.of(actor));
//
//        when(workflowStepRepository.saveAll(anyList()))
//                .thenAnswer(invocation -> invocation.getArgument(0));
//
//        when(contractStatusHistoryRepository.save(any()))
//                .thenAnswer(invocation -> invocation.getArgument(0));
//
//
//        // Nếu canonical document cần PDF,
//        // mock các service tạo tài liệu theo method thực tế
//        // trong ContractDocumentRenderer và ContractPdfGenerator.
//
//
//        // =========================================================
//        // 7. Act
//        // =========================================================
//
//        ContractResponse response =
//                contractService.createContract(request);
//
//
//        // =========================================================
//        // 8. Assert
//        // =========================================================
//
//        assertNotNull(response);
//
//        verify(currentUser)
//                .getCurrentUser();
//
//        verify(contractTypeRepository)
//                .findById(contractTypeId);
//
//        verify(contractTypeWorkflowRepository)
//                .findFirstByContractTypeIdAndActiveTrueOrderByVersionNumberDesc(
//                        contractTypeId
//                );
//
//        verify(contractRepository)
//                .save(any(Contracts.class));
//
//        verify(workflowStepRepository)
//                .saveAll(anyList());
//
//        verify(contractStatusHistoryRepository)
//                .save(any());
//    }
//    // ============================================================
//    // 2. Contract number null
//    // ============================================================
//
//    @Test
//    void createContract_shouldThrowException_whenContractNumberIsNull() {
//
//        ContractRequest request = requestWith(
//                null,
//                "Test Contract",
//                LocalDate.now(),
//                LocalDate.now().plusDays(30),
//                UUID.randomUUID()
//        );
//
//        BadHttpException exception = assertThrows(
//                BadHttpException.class,
//                () -> contractService.createContract(request)
//        );
//
//        assertEquals(
//                "Contract number is required",
//                exception.getMessage()
//        );
//
//        verify(currentUser).getCurrentUser();
//        verifyNoInteractions(contractRepository);
//    }
//
//    // ============================================================
//    // 3. Contract number blank
//    // ============================================================
//
//    @Test
//    void createContract_shouldThrowException_whenContractNumberIsBlank() {
//
//        ContractRequest request = requestWith(
//                "   ",
//                "Test Contract",
//                LocalDate.now(),
//                LocalDate.now().plusDays(30),
//                UUID.randomUUID()
//        );
//
//        BadHttpException exception = assertThrows(
//                BadHttpException.class,
//                () -> contractService.createContract(request)
//        );
//
//        assertEquals(
//                "Contract number is required",
//                exception.getMessage()
//        );
//
//        verifyNoInteractions(contractRepository);
//    }
//
//    @Test
//    void createContract_shouldThrowValidationMessage_whenContractNumberIsEmpty() {
//
//        ContractRequest request = requestWith(
//                "",
//                "Test Contract",
//                LocalDate.now(),
//                LocalDate.now().plusDays(30),
//                UUID.randomUUID()
//        );
//
//        BadHttpException exception = assertThrows(
//                BadHttpException.class,
//                () -> contractService.createContract(request)
//        );
//
//        assertEquals("Contract number is required", exception.getMessage());
//        verifyNoInteractions(contractRepository, contractTypeRepository);
//    }
//
//    // ============================================================
//    // 4. Contract title null
//    // ============================================================
//
//    @Test
//    void createContract_shouldThrowException_whenContractTitleIsNull() {
//
//        ContractRequest request = requestWith(
//                "HD-001",
//                null,
//                LocalDate.now(),
//                LocalDate.now().plusDays(30),
//                UUID.randomUUID()
//        );
//
//        BadHttpException exception = assertThrows(
//                BadHttpException.class,
//                () -> contractService.createContract(request)
//        );
//
//        assertEquals(
//                "Contract title is required",
//                exception.getMessage()
//        );
//
//        verifyNoInteractions(contractRepository);
//    }
//
//    // ============================================================
//    // 5. Contract title blank
//    // ============================================================
//
//    @Test
//    void createContract_shouldThrowException_whenContractTitleIsBlank() {
//
//        ContractRequest request = requestWith(
//                "HD-001",
//                "   ",
//                LocalDate.now(),
//                LocalDate.now().plusDays(30),
//                UUID.randomUUID()
//
//        );
//
//        BadHttpException exception = assertThrows(
//                BadHttpException.class,
//                () -> contractService.createContract(request)
//        );
//
//        assertEquals(
//                "Contract title is required",
//                exception.getMessage()
//        );
//
//        verifyNoInteractions(contractRepository);
//    }
//
//    // ============================================================
//    // 6. Effective date null
//    // ============================================================
//
//    @Test
//    void createContract_shouldThrowValidationMessage_whenContractTitleIsEmpty() {
//
//        ContractRequest request = requestWith(
//                "HD-001",
//                "",
//                LocalDate.now(),
//                LocalDate.now().plusDays(30),
//                UUID.randomUUID()
//        );
//
//        BadHttpException exception = assertThrows(
//                BadHttpException.class,
//                () -> contractService.createContract(request)
//        );
//
//        assertEquals("Contract title is required", exception.getMessage());
//        verifyNoInteractions(contractRepository, contractTypeRepository);
//    }
//
//    @Test
//    void createContract_shouldThrowException_whenEffectiveDateIsNull() {
//
//        ContractRequest request = requestWith(
//                "HD-001",
//                "Test Contract",
//                null,
//                LocalDate.now().plusDays(30),
//                UUID.randomUUID()
//        );
//
//        BadHttpException exception = assertThrows(
//                BadHttpException.class,
//                () -> contractService.createContract(request)
//        );
//
//        assertEquals(
//                "Effective date is required",
//                exception.getMessage()
//        );
//
//        verifyNoInteractions(contractRepository);
//    }
//
//    // ============================================================
//    // 7. Expiration date null
//    // ============================================================
//
//    @Test
//    void createContract_shouldThrowException_whenExpirationDateIsNull() {
//
//        ContractRequest request = requestWith(
//                "HD-001",
//                "Test Contract",
//                LocalDate.now(),
//                null,
//                UUID.randomUUID()
//        );
//
//        BadHttpException exception = assertThrows(
//                BadHttpException.class,
//                () -> contractService.createContract(request)
//        );
//
//        assertEquals(
//                "Expiration date is required",
//                exception.getMessage()
//        );
//
//        verifyNoInteractions(contractRepository);
//    }
//
//    // ============================================================
//    // 8. Expiration date before effective date
//    // ============================================================
//
//    @Test
//    void createContract_shouldThrowException_whenExpirationDateIsBeforeEffectiveDate() {
//
//        LocalDate effectiveDate = LocalDate.of(2026, 10, 20);
//
//        ContractRequest request = requestWith(
//                "HD-001",
//                "Test Contract",
//                effectiveDate,
//                effectiveDate.minusDays(1),
//                UUID.randomUUID()
//        );
//
//        BadHttpException exception = assertThrows(
//                BadHttpException.class,
//                () -> contractService.createContract(request)
//        );
//
//        assertEquals(
//                "Expiration date must be on or after the effective date",
//                exception.getMessage()
//        );
//
//        verifyNoInteractions(contractRepository);
//    }
//
//    // ============================================================
//    // 9. Contract type null
//    // ============================================================
//
//    @Test
//    void createContract_shouldThrowException_whenContractTypeIdIsNull() {
//
//        ContractRequest request = requestWith(
//                "HD-001",
//                "Test Contract",
//                LocalDate.now(),
//                LocalDate.now().plusDays(30),
//                null
//        );
//
//        BadHttpException exception = assertThrows(
//                BadHttpException.class,
//                () -> contractService.createContract(request)
//        );
//
//        assertEquals(
//                "Contract type is required",
//                exception.getMessage()
//        );
//
//        verify(contractTypeRepository, never()).findById(any());
//        verify(contractRepository, never()).save(any());
//    }
//
//    // ============================================================
//    // 10. Contract type not found
//    // ============================================================
//
//    @Test
//    void createContract_shouldThrowException_whenContractTypeDoesNotExist() {
//
//        UUID contractTypeId = UUID.randomUUID();
//
//        ContractRequest request = requestWith(
//                "HD-001",
//                "Test Contract",
//                LocalDate.now(),
//                LocalDate.now().plusDays(30),
//                contractTypeId
//        );
//
//        when(contractTypeRepository.findById(contractTypeId))
//                .thenReturn(Optional.empty());
//
//        Exception exception = assertThrows(
//                Exception.class,
//                () -> contractService.createContract(request)
//        );
//
//        assertEquals(
//                "Contract type not found with id: " + contractTypeId,
//                exception.getMessage()
//        );
//
//        verify(contractTypeRepository).findById(contractTypeId);
//        verify(contractRepository, never()).save(any());
//    }
//
//    // ============================================================
//    // 11. Contract type has no active workflow
//    // ============================================================
//
//    @Test
//    void createContract_shouldThrowException_whenActiveWorkflowDoesNotExist() {
//
//        UUID contractTypeId = UUID.randomUUID();
//
//        ContractTypes contractType = mock(ContractTypes.class);
//
//        when(contractType.getId()).thenReturn(contractTypeId);
//
//        ContractRequest request = requestWith(
//                "HD-001",
//                "Test Contract",
//                LocalDate.now(),
//                LocalDate.now().plusDays(30),
//                contractTypeId
//        );
//
//        when(contractTypeRepository.findById(contractTypeId))
//                .thenReturn(Optional.of(contractType));
//
//        when(contractTypeWorkflowRepository
//                .findFirstByContractTypeIdAndActiveTrueOrderByVersionNumberDesc(
//                        contractTypeId
//                ))
//                .thenReturn(Optional.empty());
//
//        BadHttpException exception = assertThrows(
//                BadHttpException.class,
//                () -> contractService.createContract(request)
//        );
//
//        assertEquals(
//                "The selected contract type does not have an active workflow",
//                exception.getMessage()
//        );
//
//        verify(contractRepository, never()).save(any());
//    }
//
//    // ============================================================
//    // 12. Project permission is required when projectId exists
//    // ============================================================
//
//    @Test
//    void createContract_shouldCheckProjectPermission_whenProjectIdIsProvided() {
//
//        UUID projectId = UUID.randomUUID();
//
//        ContractRequest request = new ContractRequest(
//                projectId,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                false,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null
//        );
//
//        BadHttpException exception = assertThrows(
//                BadHttpException.class,
//                () -> contractService.createContract(request)
//        );
//
//        assertEquals(
//                "Contract number is required",
//                exception.getMessage()
//        );
//
//        // Project authorization is a precondition and happens before
//        // request-field validation.
//        verify(permissionAccessService)
//                .requireAction(projectId, ContractProjectActions.CREATE);
//        verifyNoInteractions(contractRepository);
//    }
//
//    @Test
//    void createContract_shouldAcceptDateBoundary_whenExpirationDateEqualsEffectiveDate() {
//
//        UUID contractTypeId = UUID.randomUUID();
//        LocalDate contractDate = LocalDate.of(2026, 10, 20);
//        ContractRequest request = requestWith(
//                "HD-001",
//                "Test Contract",
//                contractDate,
//                contractDate,
//                contractTypeId
//        );
//        when(contractTypeRepository.findById(contractTypeId))
//                .thenReturn(Optional.empty());
//
//        NotFoundException exception = assertThrows(
//                NotFoundException.class,
//                () -> contractService.createContract(request)
//        );
//
//        // The contract-type lookup confirms equal dates passed validation.
//        assertEquals(
//                "Contract type not found with id: " + contractTypeId,
//                exception.getMessage()
//        );
//        verify(contractTypeRepository).findById(contractTypeId);
//        verify(contractRepository, never()).save(any());
//    }
//
//    // ============================================================
//    // 13. User does not have permission to create a project contract
//    // ============================================================
//
//    @Test
//    void createContract_shouldStopImmediately_whenProjectCreatePermissionIsDenied() {
//
//        UUID projectId = UUID.randomUUID();
//        ContractRequest request = new ContractRequest(
//                projectId,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                false,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null
//        );
//        BadHttpException denied = new BadHttpException("You do not have permission to create contracts");
//        doThrow(denied).when(permissionAccessService)
//                .requireAction(projectId, ContractProjectActions.CREATE);
//
//        BadHttpException exception = assertThrows(
//                BadHttpException.class,
//                () -> contractService.createContract(request)
//        );
//
//        assertEquals(denied.getMessage(), exception.getMessage());
//        verify(permissionAccessService)
//                .requireAction(projectId, ContractProjectActions.CREATE);
//        verifyNoInteractions(
//                contractRepository,
//                projectRepository,
//                contractTypeRepository,
//                contractTypeWorkflowRepository
//        );
//    }
//}
