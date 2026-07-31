package com.fpt.backend.service.interfaces;

import com.fpt.backend.dto.request.contract.ContractTypeRequest;
import com.fpt.backend.dto.response.contract.ContractTypeResponse;

import java.util.List;
import java.util.UUID;

public interface ContractTypeService {
    List<ContractTypeResponse> getContractTypes();

    ContractTypeResponse getContractTypeById(UUID id);

    ContractTypeResponse createContractType(ContractTypeRequest request);

    ContractTypeResponse updateContractType(UUID id, ContractTypeRequest request);

    void deleteContractType(UUID id);
}
