package com.fpt.backend.controller.contractController;

import com.fpt.backend.dto.request.contract.ContractListRequest;
import com.fpt.backend.dto.request.contract.ContractRequest;
import com.fpt.backend.dto.response.contract.ContractListResponse;
import com.fpt.backend.dto.response.contract.ContractResponse;
import com.fpt.backend.service.interfaces.ContractService;
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

@RestController
@RequestMapping({"/api/contracts", "/api/v1/contracts"})
@CrossOrigin(originPatterns = "*")
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

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ContractResponse>> getContractById(@PathVariable int id) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new BaseResponse<>(contractService.getContractById(id)));
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
            @PathVariable int id,
            @RequestBody ContractRequest request) {
        return ResponseEntity.ok(new BaseResponse<>(contractService.updateContract(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteContract(@PathVariable int id) {
        contractService.deleteContract(id);
        return ResponseEntity.ok(new BaseResponse<>(
                HttpStatus.OK.value(),
                "Deleted",
                null
        ));
    }
}
