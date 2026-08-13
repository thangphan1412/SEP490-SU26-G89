package com.fpt.backend.controller.contractController;

import com.fpt.backend.dto.request.contract.ContractTypeRequest;
import com.fpt.backend.dto.response.contract.ContractTypeResponse;
import com.fpt.backend.dto.response.contract.ContractWorkflowOptionsResponse;
import com.fpt.backend.service.interfaces.contract.ContractTypeService;
import com.fpt.backend.util.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/contract-types", "/api/v1/contract-types"})
@CrossOrigin(originPatterns = "*")
@RequiredArgsConstructor
public class ContractTypeController {
    private final ContractTypeService contractTypeService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<ContractTypeResponse>>> getContractTypes() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new BaseResponse<>(contractTypeService.getContractTypes()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ContractTypeResponse>> getContractTypeById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new BaseResponse<>(contractTypeService.getContractTypeById(id)));
    }

    @GetMapping("/workflow-options")
    public ResponseEntity<BaseResponse<ContractWorkflowOptionsResponse>>
    getWorkflowOptions() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new BaseResponse<>(contractTypeService.getWorkflowOptions()));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<ContractTypeResponse>> createContractType(
            @RequestBody ContractTypeRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new BaseResponse<>(
                HttpStatus.CREATED.value(),
                "Created",
                contractTypeService.createContractType(request)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<ContractTypeResponse>> updateContractType(
            @PathVariable UUID id,
            @RequestBody ContractTypeRequest request
    ) {
        return ResponseEntity.ok(new BaseResponse<>(
                contractTypeService.updateContractType(id, request)
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteContractType(@PathVariable UUID id) {
        contractTypeService.deleteContractType(id);
        return ResponseEntity.ok(new BaseResponse<>(
                HttpStatus.OK.value(),
                "Deleted",
                null
        ));
    }
}
