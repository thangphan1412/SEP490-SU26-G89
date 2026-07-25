package com.fpt.backend.service.interfaces;

import com.fpt.backend.dto.request.contract.ContractListRequest;
import com.fpt.backend.dto.request.contract.ContractRequest;
import com.fpt.backend.dto.response.contract.ContractListResponse;
import com.fpt.backend.dto.response.contract.ContractResponse;

import java.util.UUID;

public interface ContractService {
    ContractListResponse getContracts(ContractListRequest request);

    ContractResponse getContractById(UUID id);

    ContractResponse createContract(ContractRequest request);

    ContractResponse updateContract(UUID id, ContractRequest request);

    void deleteContract(UUID id);
}
