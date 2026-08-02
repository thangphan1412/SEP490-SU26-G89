package com.fpt.backend.controller.contractController;

import com.fpt.backend.dto.request.contract.ContractTemplateRequest;
import com.fpt.backend.dto.request.contract.ContractTemplateVersionRequest;
import com.fpt.backend.dto.response.contract.ContractTemplateResponse;
import com.fpt.backend.dto.response.contract.ContractTemplateVersionResponse;
import com.fpt.backend.service.interfaces.ContractTemplateService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/contract-templates", "/api/v1/contract-templates"})
@CrossOrigin(originPatterns = "*")
@RequiredArgsConstructor
public class ContractTemplateController {
    private final ContractTemplateService contractTemplateService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<ContractTemplateResponse>>> getContractTemplates(
            @RequestParam(required = false) UUID contractTypeId
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new BaseResponse<>(
                        contractTemplateService.getContractTemplates(contractTypeId)
                ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ContractTemplateResponse>> getContractTemplateById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new BaseResponse<>(
                        contractTemplateService.getContractTemplateById(id)
                ));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<ContractTemplateResponse>> createContractTemplate(
            @RequestBody ContractTemplateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new BaseResponse<>(
                HttpStatus.CREATED.value(),
                "Created",
                contractTemplateService.createContractTemplate(request)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<ContractTemplateResponse>> updateContractTemplate(
            @PathVariable UUID id,
            @RequestBody ContractTemplateRequest request
    ) {
        return ResponseEntity.ok(new BaseResponse<>(
                contractTemplateService.updateContractTemplate(id, request)
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteContractTemplate(
            @PathVariable UUID id
    ) {
        contractTemplateService.deleteContractTemplate(id);
        return ResponseEntity.ok(new BaseResponse<>(
                HttpStatus.OK.value(),
                "Deleted",
                null
        ));
    }

    @PostMapping("/{id}/versions")
    public ResponseEntity<BaseResponse<ContractTemplateVersionResponse>>
            createContractTemplateVersion(
                    @PathVariable UUID id,
                    @RequestBody ContractTemplateVersionRequest request
            ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new BaseResponse<>(
                HttpStatus.CREATED.value(),
                "Created",
                contractTemplateService.createContractTemplateVersion(id, request)
        ));
    }
}
