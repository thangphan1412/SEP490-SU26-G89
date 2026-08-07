package com.fpt.backend.service.interfaces;

import com.fpt.backend.dto.request.contract.ContractListRequest;
import com.fpt.backend.dto.request.contract.ContractRequest;
import com.fpt.backend.dto.request.contract.ContractTransitionRequest;
import com.fpt.backend.dto.response.contract.ContractListResponse;
import com.fpt.backend.dto.response.contract.ContractPdfResponse;
import com.fpt.backend.dto.response.contract.ContractProjectOptionResponse;
import com.fpt.backend.dto.response.contract.ContractResponse;

import java.util.List;
import java.util.UUID;

public interface ContractService {
    ContractListResponse getContracts(ContractListRequest request);

    List<ContractProjectOptionResponse> getProjectOptions();

    ContractResponse getContractById(UUID id);

    ContractResponse createContract(ContractRequest request);

    ContractResponse updateContract(UUID id, ContractRequest request);

    ContractResponse transitionContract(UUID id, ContractTransitionRequest request);

    ContractPdfResponse exportContractPdf(UUID id);

    void deleteContract(UUID id);
}
