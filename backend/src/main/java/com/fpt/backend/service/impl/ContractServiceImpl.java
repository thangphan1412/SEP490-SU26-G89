package com.fpt.backend.service.impl;

import com.fpt.backend.dto.request.contract.ContractListRequest;
import com.fpt.backend.dto.request.contract.ContractRequest;
import com.fpt.backend.dto.response.contract.ContractListResponse;
import com.fpt.backend.dto.response.contract.ContractResponse;
import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.exception.NotFoundException;
import com.fpt.backend.repository.contract.ContractRepository;
import com.fpt.backend.repository.project.ProjectRepository;
import com.fpt.backend.service.interfaces.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {
    private static final int PAGE_SIZE = 8;
    private static final String DATA_SOURCE = "DATABASE";
    private static final String DEFAULT_SORT_FIELD = "id";

    private static final Set<String> SORT_FIELDS = Set.of(
            "id",
            "contractNumber",
            "contractTitle",
            "contractStatus",
            "effectiveDate",
            "experationDate",
            "contractCreateBy",
            "contractCreatedAt"
    );

    private final ContractRepository contractRepository;
    private final ProjectRepository projectRepository;

    @Override
    @Transactional(readOnly = true)
    public ContractListResponse getContracts(ContractListRequest request) {
        String search = normalize(request.search());
        String status = normalize(request.status());
        Pageable pageable = createPageable(request.page(), request.sortBy(), request.sortDirection());

        Page<Contracts> contracts = findContracts(search, status, pageable);

        return new ContractListResponse(
                DATA_SOURCE,
                contracts.map(this::toResponse).getContent(),
                contracts.getNumber(),
                contracts.getSize(),
                contracts.getTotalElements(),
                contracts.getTotalPages(),
                contracts.isFirst(),
                contracts.isLast(),
                contractRepository.findDistinctContractStatuses()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ContractResponse getContractById(int id) {
        return toResponse(findContract(id));
    }

    @Override
    @Transactional
    public ContractResponse createContract(ContractRequest request) {
        Contracts contract = new Contracts();
        applyRequest(contract, request);
        contract.setContractCreatedAt(
                request.contractCreatedAt() != null ? request.contractCreatedAt() : LocalDateTime.now()
        );

        return toResponse(contractRepository.save(contract));
    }

    @Override
    @Transactional
    public ContractResponse updateContract(int id, ContractRequest request) {
        Contracts contract = findContract(id);
        applyRequest(contract, request);
        if (request.contractCreatedAt() != null) {
            contract.setContractCreatedAt(request.contractCreatedAt());
        }

        return toResponse(contractRepository.save(contract));
    }

    @Override
    @Transactional
    public void deleteContract(int id) {
        Contracts contract = findContract(id);
        contractRepository.delete(contract);
    }

    private Page<Contracts> findContracts(String search, String status, Pageable pageable) {
        if (search.isBlank() && status.isBlank()) {
            return contractRepository.findAll(pageable);
        }

        if (search.isBlank()) {
            return contractRepository.findByContractStatusIgnoreCase(status, pageable);
        }

        return contractRepository.searchContracts(
                search.toLowerCase(Locale.ROOT),
                status.toLowerCase(Locale.ROOT),
                pageable
        );
    }

    private void applyRequest(Contracts contract, ContractRequest request) {
        contract.setContractNumber(request.contractNumber());
        contract.setContractTitle(request.contractTitle());
        contract.setContractStatus(request.contractStatus());
        contract.setEffectiveDate(request.effectiveDate());
        contract.setExperationDate(request.expirationDate());
        contract.setContractCreateBy(request.contractCreatedBy());
        contract.setProject(resolveProject(request.projectId()));
    }

    private Projects resolveProject(Integer projectId) {
        if (projectId == null) {
            return null;
        }

        return projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found with id: " + projectId));
    }

    private Contracts findContract(int id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Contract not found with id: " + id));
    }

    private Pageable createPageable(int page, String sortBy, String sortDirection) {
        int validPage = Math.max(page, 0);
        String sortField = sortBy != null && SORT_FIELDS.contains(sortBy) ? sortBy : DEFAULT_SORT_FIELD;
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(validPage, PAGE_SIZE, Sort.by(direction, sortField));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private ContractResponse toResponse(Contracts contract) {
        Projects project = contract.getProject();

        return new ContractResponse(
                contract.getId(),
                project != null ? project.getId() : null,
                project != null ? project.getProjectName() : null,
                contract.getContractNumber(),
                contract.getContractTitle(),
                contract.getContractStatus(),
                contract.getEffectiveDate(),
                contract.getExperationDate(),
                contract.getContractCreateBy(),
                contract.getContractCreatedAt()
        );
    }
}
