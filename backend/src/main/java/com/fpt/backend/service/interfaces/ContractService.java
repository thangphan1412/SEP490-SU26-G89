package com.fpt.backend.service.interfaces;

import com.fpt.backend.dto.request.contract.ContractListRequest;
import com.fpt.backend.dto.request.contract.ContractRequest;
import com.fpt.backend.dto.response.contract.ContractListResponse;
import com.fpt.backend.dto.response.contract.ContractResponse;

public interface ContractService {
    ContractListResponse getContracts(ContractListRequest request);

    ContractResponse getContractById(int id);

    ContractResponse createContract(ContractRequest request);

    ContractResponse updateContract(int id, ContractRequest request);

    void deleteContract(int id);
}
