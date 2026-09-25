package com.fpt.backend.service.impl.contract;

import com.fpt.backend.dto.request.contract.ContractRequest;
import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.Users;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.repository.contract.ContractRepository;
import com.fpt.backend.util.CurrentUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractServiceImplCreationRulesTest {

    @Mock
    private ContractRepository contractRepository;
    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private ContractServiceImpl contractService;

    @Test
    void createContractRequiresTemplate() {
        assertThatThrownBy(() -> contractService.createContract(
                request(null, UUID.randomUUID())
        )).isInstanceOf(BadHttpException.class)
                .hasMessage("Contract template is required");
    }

    @Test
    void createContractRequiresTemplateVersion() {
        assertThatThrownBy(() -> contractService.createContract(
                request(UUID.randomUUID(), null)
        )).isInstanceOf(BadHttpException.class)
                .hasMessage("Contract template version is required");
    }

    @Test
    void newContractCannotBeOpenedByAnotherWorkflowUser() {
        Users creator = user();
        Users workflowUser = user();
        Contracts contract = new Contracts();
        contract.setId(UUID.randomUUID());
        contract.setContractStatus("NEW");
        contract.setContractCreatedByUser(creator);

        when(contractRepository.findById(contract.getId()))
                .thenReturn(Optional.of(contract));
        when(currentUser.getCurrentUser()).thenReturn(workflowUser);

        assertThatThrownBy(() -> contractService.getContractById(
                contract.getId()
        )).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining(
                        "Only the contract creator can access a NEW contract before it is submitted"
                );
    }

    private ContractRequest request(UUID templateId, UUID versionId) {
        return new ContractRequest(
                null,
                null,
                null,
                UUID.randomUUID(),
                templateId,
                versionId,
                "CON-NEW-001",
                "New contract",
                "NEW",
                LocalDate.of(2026, 9, 18),
                LocalDate.of(2027, 9, 18),
                "Contract Creator",
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                "Contract Creator",
                "EMPLOYEE",
                Map.of(),
                List.of()
        );
    }

    private Users user() {
        Users user = new Users();
        user.setId(UUID.randomUUID());
        return user;
    }
}
