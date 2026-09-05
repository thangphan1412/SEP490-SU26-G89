package com.fpt.backend.service.impl.contract;

import com.fpt.backend.dto.request.contract.ContractTransitionRequest;
import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.Users;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.repository.contract.ContractRepository;
import com.fpt.backend.repository.contract.ContractWorkflowStepInstanceRepository;
import com.fpt.backend.util.CurrentUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractServiceImplTransitionTest {

    @Mock
    private ContractRepository contractRepository;
    @Mock
    private ContractWorkflowStepInstanceRepository workflowStepRepository;
    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private ContractServiceImpl contractService;

    @Test
    void transitionWithoutConfiguredWorkflowIsRejected() {
        UUID contractId = UUID.randomUUID();
        Contracts contract = new Contracts();
        contract.setId(contractId);
        contract.setContractStatus("NEW");
        Users actor = new Users();
        actor.setId(UUID.randomUUID());

        when(contractRepository.findById(contractId))
                .thenReturn(Optional.of(contract));
        when(currentUser.getCurrentUser()).thenReturn(actor);
        when(workflowStepRepository.existsByContractId(contractId))
                .thenReturn(false);

        ContractTransitionRequest request = new ContractTransitionRequest(
                "COMPLETE_STEP", null, null, null, null
        );

        assertThatThrownBy(() -> contractService.transitionContract(
                contractId, request
        )).isInstanceOf(BadHttpException.class)
                .hasMessage("Contract does not have a configured workflow");

        verify(contractRepository, never()).save(contract);
    }
}
