package com.fpt.backend.service.impl.contract;

import com.fpt.backend.dto.request.contract.ContractTypeRequest;
import com.fpt.backend.dto.response.contract.ContractTypeResponse;
import com.fpt.backend.entity.ContractTypes;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.exception.NotFoundException;
import com.fpt.backend.repository.contract.ContractRepository;
import com.fpt.backend.repository.contract.ContractTemplateRepository;
import com.fpt.backend.repository.contract.ContractTypeRepository;
import com.fpt.backend.service.interfaces.contract.ContractTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractTypeServiceImpl implements ContractTypeService {
    private static final String DEFAULT_STATUS = "Active";

    private final ContractTypeRepository contractTypeRepository;
    private final ContractTemplateRepository contractTemplateRepository;
    private final ContractRepository contractRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ContractTypeResponse> getContractTypes() {
        return contractTypeRepository.findAllByOrderByContractTypeNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ContractTypeResponse getContractTypeById(UUID id) {
        return toResponse(findContractType(id));
    }

    @Override
    @Transactional
    public ContractTypeResponse createContractType(ContractTypeRequest request) {
        validateRequest(request);
        String code = requireText(request.contractTypeCode(), "Contract type code is required");

        if (contractTypeRepository.existsByContractTypeCodeIgnoreCase(code)) {
            throw new BadHttpException("Contract type code already exists");
        }

        LocalDateTime now = LocalDateTime.now();
        ContractTypes contractType = new ContractTypes();
        applyRequest(contractType, request);
        contractType.setCreatedAt(now);
        contractType.setUpdatedAt(now);

        return toResponse(contractTypeRepository.save(contractType));
    }

    @Override
    @Transactional
    public ContractTypeResponse updateContractType(UUID id, ContractTypeRequest request) {
        validateRequest(request);
        ContractTypes contractType = findContractType(id);
        String code = requireText(request.contractTypeCode(), "Contract type code is required");

        if (contractTypeRepository.existsByContractTypeCodeIgnoreCaseAndIdNot(code, id)) {
            throw new BadHttpException("Contract type code already exists");
        }

        String originalCreator = contractType.getCreatedBy();
        applyRequest(contractType, request);
        if (isBlank(request.createdBy())) {
            contractType.setCreatedBy(originalCreator);
        }
        contractType.setUpdatedAt(LocalDateTime.now());

        return toResponse(contractTypeRepository.save(contractType));
    }

    @Override
    @Transactional
    public void deleteContractType(UUID id) {
        ContractTypes contractType = findContractType(id);
        long templateCount = contractTemplateRepository.countByContractTypeId(id);
        long contractCount = contractRepository.countByContractTypeId(id);

        if (templateCount > 0 || contractCount > 0) {
            throw new BadHttpException(
                    "Cannot delete a contract type that is used by templates or contracts"
            );
        }

        contractTypeRepository.delete(contractType);
    }

    private void validateRequest(ContractTypeRequest request) {
        if (request == null) {
            throw new BadHttpException("Contract type information is required");
        }

        requireText(request.contractTypeCode(), "Contract type code is required");
        requireText(request.contractTypeName(), "Contract type name is required");

        if (request.validityDays() != null && request.validityDays() <= 0) {
            throw new BadHttpException("Default validity must be greater than zero");
        }
    }

    private void applyRequest(ContractTypes contractType, ContractTypeRequest request) {
        contractType.setContractTypeCode(
                requireText(request.contractTypeCode(), "Contract type code is required")
        );
        contractType.setContractTypeName(
                requireText(request.contractTypeName(), "Contract type name is required")
        );
        contractType.setDescription(normalizeToNull(request.description()));
        contractType.setValidityDays(request.validityDays());
        contractType.setCategory(normalizeToNull(request.category()));
        contractType.setStatus(
                isBlank(request.status()) ? DEFAULT_STATUS : request.status().trim()
        );
        contractType.setCreatedBy(normalizeToNull(request.createdBy()));
    }

    private ContractTypes findContractType(UUID id) {
        if (id == null) {
            throw new BadHttpException("Contract type id is required");
        }

        return contractTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Contract type not found with id: " + id
                ));
    }

    private ContractTypeResponse toResponse(ContractTypes contractType) {
        UUID id = contractType.getId();

        return new ContractTypeResponse(
                id,
                contractType.getContractTypeCode(),
                contractType.getContractTypeName(),
                contractType.getDescription(),
                contractType.getValidityDays(),
                contractType.getCategory(),
                contractType.getStatus(),
                contractType.getCreatedBy(),
                contractType.getCreatedAt(),
                contractType.getUpdatedAt(),
                contractTemplateRepository.countByContractTypeId(id),
                contractRepository.countByContractTypeId(id)
        );
    }

    private String requireText(String value, String message) {
        if (isBlank(value)) {
            throw new BadHttpException(message);
        }

        return value.trim();
    }

    private String normalizeToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
