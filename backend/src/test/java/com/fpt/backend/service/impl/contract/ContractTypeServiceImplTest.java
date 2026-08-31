package com.fpt.backend.service.impl.contract;

import com.fpt.backend.dto.request.contract.ContractTypeRequest;
import com.fpt.backend.dto.request.contract.ContractWorkflowStepRequest;
import com.fpt.backend.entity.ContractTypeWorkflow;
import com.fpt.backend.entity.ContractTypes;
import com.fpt.backend.entity.Role;
import com.fpt.backend.repository.contract.ContractRepository;
import com.fpt.backend.repository.contract.ContractTemplateRepository;
import com.fpt.backend.repository.contract.ContractTypeRepository;
import com.fpt.backend.repository.contract.ContractTypeWorkflowRepository;
import com.fpt.backend.repository.role.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractTypeServiceImplTest {
    @Mock
    private ContractTypeRepository contractTypeRepository;

    @Mock
    private ContractTemplateRepository contractTemplateRepository;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private ContractTypeWorkflowRepository workflowRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private ContractTypeServiceImpl contractTypeService;

    @Test
    void createsWorkflowUsingRoleCodesLoadedFromRoleRepository() {
        UUID contractTypeId = UUID.randomUUID();
        when(roleRepository.findAllForSelection()).thenReturn(List.of(
                role("LEGAL_AUTHOR", "Legal author"),
                role("RISK_REVIEWER", "Risk reviewer"),
                role("AUTHORIZED_SIGNER", "Authorized signer")
        ));
        when(contractTypeRepository.save(any(ContractTypes.class)))
                .thenAnswer(invocation -> {
                    ContractTypes contractType = invocation.getArgument(0);
                    contractType.setId(contractTypeId);
                    return contractType;
                });
        when(workflowRepository.findByContractTypeIdOrderByVersionNumberDesc(
                contractTypeId
        )).thenReturn(List.of());
        when(workflowRepository.findLatestVersionNumber(contractTypeId))
                .thenReturn(0);

        contractTypeService.createContractType(new ContractTypeRequest(
                "LEGAL_SERVICE",
                "Legal service contract",
                null,
                365,
                "Legal",
                "Active",
                "tester@example.com",
                "Legal approval workflow",
                List.of(
                        step(1, "Draft", "CREATE", "legal-author"),
                        step(2, "Risk review", "APPROVE", "risk reviewer"),
                        step(3, "Final signature", "SIGN", "authorized_signer")
                )
        ));

        ArgumentCaptor<ContractTypeWorkflow> workflowCaptor =
                ArgumentCaptor.forClass(ContractTypeWorkflow.class);
        verify(workflowRepository).save(workflowCaptor.capture());

        assertThat(workflowCaptor.getValue().getSteps())
                .extracting(step -> step.getRequiredRoleCode())
                .containsExactly(
                        "LEGAL_AUTHOR",
                        "RISK_REVIEWER",
                        "AUTHORIZED_SIGNER"
                );
    }

    private ContractWorkflowStepRequest step(
            int order,
            String name,
            String action,
            String roleCode
    ) {
        return new ContractWorkflowStepRequest(
                order,
                name,
                action,
                roleCode,
                true,
                action.equals("CREATE") ? false : true
        );
    }

    private Role role(String code, String name) {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setRoleCode(code);
        role.setRoleName(name);
        return role;
    }
}
