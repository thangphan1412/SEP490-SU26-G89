package com.fpt.backend.controller.signatureController;

import com.fpt.backend.entity.Contracts;
import com.fpt.backend.repository.contract.ContractRepository;
import com.fpt.backend.service.impl.signature.ContractSigningService;
import com.fpt.backend.service.impl.signature.DigitalSignatureService;
import com.fpt.backend.service.interfaces.contract.ContractService;
import com.fpt.backend.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
public class ContractSigningController {

    private final ContractSigningService contractSigningService;
    private final CurrentUser currentUser;
    private final ContractService contractService;
    private final ContractRepository contractRepository;
    private final DigitalSignatureService digitalSignatureService;
    @PostMapping("/{contractId}/sign")
    public ResponseEntity<?> signContract(
            @PathVariable UUID contractId,
            @RequestParam("file") MultipartFile file
    ) throws Exception {
        UUID currentUserId = currentUser.getCurrentUser().getId();
        Contracts contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));
        String signature = String.valueOf(digitalSignatureService.sign(
                file.getBytes(),
                currentUserId
        ));
        return ResponseEntity.ok(
                signature
        );
    }

}