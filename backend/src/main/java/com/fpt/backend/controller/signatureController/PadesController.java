package com.fpt.backend.controller.signatureController;


import com.fpt.backend.dto.request.signature.PadesCompleteRequest;
import com.fpt.backend.dto.request.signature.PadesSigningSession;
import com.fpt.backend.dto.response.signature.PadesPrepareResponse;
import com.fpt.backend.service.impl.signature.PadesSigningService;
import com.fpt.backend.repository.contract.ContractRepository;
import com.fpt.backend.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
public class PadesController {

    private final PadesSigningService padesSigningService;
    private final ContractRepository contractRepository;
    private final CurrentUser currentUser;

    @PostMapping("/{contractId}/sign/prepare")
    public ResponseEntity<?> prepareSigning(
            @PathVariable UUID contractId,
            @RequestParam UUID electronicSignatureId,
            @RequestParam String keyCode,
            @RequestParam("file") MultipartFile file
    ) throws Exception {
        UUID userId = currentUser.getCurrentUser().getId();
        contractRepository.findById(contractId).orElseThrow(() ->
                        new IllegalArgumentException(
                                "Contract not found"
                        )
                );

        PadesPrepareResponse response = padesSigningService.prepare(
                        contractId,
                        userId,
                        electronicSignatureId,
                        keyCode,
                        file.getBytes()
                );
        return ResponseEntity.ok(
                response
        );
    }


    @PostMapping("/{contractId}/sign/complete")
    public ResponseEntity<?> completeSigning(
            @PathVariable UUID contractId,
            @RequestBody
            PadesCompleteRequest request
    ) throws Exception {
        System.err.println("########### PADES COMPLETE BACKEND ###########");
        System.err.println("contractId = " + contractId);
        System.err.println("sessionId = " + request.sessionId());

        PadesSigningSession session =
                padesSigningService.getSession(
                        request.sessionId()
                );
        if (!contractId.equals(
                session.getContractId()
        )) {

            throw new IllegalArgumentException(
                    "Signing session does not belong to this contract"
            );
        }
        byte[] signedPdf =
                padesSigningService.complete(
                        request.sessionId(),
                        request.signatureValue()
                );
        String pdfBase64 = Base64.getEncoder().encodeToString(signedPdf);
        return ResponseEntity.ok(
                pdfBase64
        );
    }
}