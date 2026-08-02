package com.fpt.backend.controller.contractController;

import com.fpt.backend.dto.request.contract.ContractListRequest;
import com.fpt.backend.dto.request.contract.ContractRequest;
import com.fpt.backend.dto.request.contract.ContractTransitionRequest;
import com.fpt.backend.dto.response.contract.ContractListResponse;
import com.fpt.backend.dto.response.contract.ContractPdfResponse;
import com.fpt.backend.dto.response.contract.ContractProjectOptionResponse;
import com.fpt.backend.dto.response.contract.ContractResponse;
import com.fpt.backend.service.interfaces.ContractService;
import com.fpt.backend.util.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import java.util.UUID;
import java.util.List;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping({"/api/contracts", "/api/v1/contracts"})
@CrossOrigin(
        originPatterns = "*",
        exposedHeaders = HttpHeaders.CONTENT_DISPOSITION
)
@RequiredArgsConstructor
public class ContractController {
    private final ContractService contractService;

    @GetMapping("/list")
    public ResponseEntity<BaseResponse<ContractListResponse>> getContracts(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        ContractListResponse contracts = contractService.getContracts(
                new ContractListRequest(search, status, page, sortBy, sortDirection)
        );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new BaseResponse<>(contracts));
    }

    @GetMapping("/project-options")
    public ResponseEntity<BaseResponse<List<ContractProjectOptionResponse>>>
            getProjectOptions() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new BaseResponse<>(contractService.getProjectOptions()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ContractResponse>> getContractById(@PathVariable UUID id) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new BaseResponse<>(contractService.getContractById(id)));
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportContractPdf(@PathVariable UUID id) {
        ContractPdfResponse pdf = contractService.exportContractPdf(id);
        ContentDisposition disposition = ContentDisposition.builder("inline")
                .filename(pdf.fileName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.content().length)
                .body(pdf.content());
    }

    @PostMapping
    public ResponseEntity<BaseResponse<ContractResponse>> createContract(@RequestBody ContractRequest request) {
        ContractResponse contract = contractService.createContract(request);
        BaseResponse<ContractResponse> response = new BaseResponse<>(
                HttpStatus.CREATED.value(),
                "Created",
                contract
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<ContractResponse>> updateContract(
            @PathVariable UUID id,
            @RequestBody ContractRequest request) {
        return ResponseEntity.ok(new BaseResponse<>(contractService.updateContract(id, request)));
    }

    @PostMapping("/{id}/transitions")
    public ResponseEntity<BaseResponse<ContractResponse>> transitionContract(
            @PathVariable UUID id,
            @RequestBody ContractTransitionRequest request) {
        return ResponseEntity.ok(new BaseResponse<>(
                contractService.transitionContract(id, request)
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteContract(
            @PathVariable UUID id,
            @RequestParam String actorName,
            @RequestParam String actorRole) {
        contractService.deleteContract(id, actorName, actorRole);
        return ResponseEntity.ok(new BaseResponse<>(
                HttpStatus.OK.value(),
                "Deleted",
                null
        ));
    }
}
