package com.fpt.backend.service.impl.contract;

import com.fpt.backend.dto.request.contract.ContractTypeRequest;
import com.fpt.backend.dto.request.contract.ContractWorkflowStepRequest;
import com.fpt.backend.entity.ContractTypeWorkflow;
import com.fpt.backend.entity.ContractTypes;
import com.fpt.backend.entity.Departments;
import com.fpt.backend.entity.Role;
import com.fpt.backend.repository.contract.ContractRepository;
import com.fpt.backend.repository.contract.ContractTemplateRepository;
import com.fpt.backend.repository.contract.ContractTypeRepository;
import com.fpt.backend.repository.contract.ContractTypeWorkflowRepository;
import com.fpt.backend.repository.department.DepartmentRepository;
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

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private ContractTypeServiceImpl contractTypeService;

    @Test
    void workflowOptionsComeFromRoleAndDepartmentRepositories() {
        Role role = role("LEGAL_REVIEWER", "Legal reviewer");
        Departments department = department("LEGAL", "Legal Department");
        when(roleRepository.findAllForSelection()).thenReturn(List.of(role));
        when(departmentRepository.findAll()).thenReturn(List.of(department));

        var options = contractTypeService.getWorkflowOptions();

        assertThat(options.roles())
                .extracting(item -> item.roleCode())
                .containsExactly("LEGAL_REVIEWER");
        assertThat(options.departments())
                .extracting(item -> item.id())
                .containsExactly(department.getId());
    }

    @Test
    void createsWorkflowUsingRoleCodesLoadedFromRoleRepository() {
        UUID contractTypeId = UUID.randomUUID();
        Departments department = department(
                "LEGAL",
                "Legal Department"
        );
        when(roleRepository.findAllForSelection()).thenReturn(List.of(
                role("LEGAL_AUTHOR", "Legal author"),
                role("RISK_REVIEWER", "Risk reviewer"),
                role("AUTHORIZED_SIGNER", "Authorized signer")
        ));
        when(departmentRepository.findAll()).thenReturn(List.of(department));
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
                        step(1, "Draft", "CREATE", "legal-author", department.getId()),
                        step(2, "Risk review", "APPROVE", "risk reviewer", department.getId()),
                        step(3, "Final signature", "SIGN", "authorized_signer", department.getId())
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
        assertThat(workflowCaptor.getValue().getSteps())
                .extracting(step -> step.getRequiredDepartment().getId())
                .containsOnly(department.getId());
    }

    private ContractWorkflowStepRequest step(
            int order,
            String name,
            String action,
            String roleCode,
            UUID departmentId
    ) {
        return new ContractWorkflowStepRequest(
                order,
                name,
                action,
                roleCode,
                departmentId,
                true,
                !"CREATE".equals(action)
        );
    }

    private Role role(String code, String name) {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setRoleCode(code);
        role.setRoleName(name);
        return role;
    }

    private Departments department(String code, String name) {
        Departments department = new Departments();
        department.setId(UUID.randomUUID());
        department.setDepartmentCode(code);
        department.setDepartmentName(name);
        return department;
    }
}
