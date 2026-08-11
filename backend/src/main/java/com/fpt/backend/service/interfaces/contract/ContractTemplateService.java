package com.fpt.backend.service.interfaces.contract;

import com.fpt.backend.dto.request.contract.ContractTemplateRequest;
import com.fpt.backend.dto.request.contract.ContractTemplateVersionRequest;
import com.fpt.backend.dto.response.contract.ContractTemplateResponse;
import com.fpt.backend.dto.response.contract.ContractTemplateVersionResponse;

import java.util.List;
import java.util.UUID;

public interface ContractTemplateService {
    List<ContractTemplateResponse> getContractTemplates(UUID contractTypeId);

    ContractTemplateResponse getContractTemplateById(UUID id);

    ContractTemplateResponse createContractTemplate(ContractTemplateRequest request);

    ContractTemplateResponse updateContractTemplate(UUID id, ContractTemplateRequest request);

    void deleteContractTemplate(UUID id);

    ContractTemplateVersionResponse createContractTemplateVersion(
            UUID contractTemplateId,
            ContractTemplateVersionRequest request
    );
}
