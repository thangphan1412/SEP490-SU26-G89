//package com.fpt.backend.controller.signatureController;
//
//import com.fpt.backend.dto.request.signature.PadesCompleteRequest;
//import com.fpt.backend.dto.response.signature.PadesPrepareResponse;
//import com.fpt.backend.entity.Contracts;
//import com.fpt.backend.entity.FileStorage;
//import com.fpt.backend.repository.FileStorageRepository;
//import com.fpt.backend.repository.contract.ContractRepository;
//import com.fpt.backend.service.impl.CloudinaryService;
//import com.fpt.backend.service.impl.signature.ContractSigningService;
//import com.fpt.backend.service.impl.signature.DigitalSignatureService;
//import com.fpt.backend.service.impl.signature.PadesSigningService;
//import com.fpt.backend.service.interfaces.contract.ContractService;
//import com.fpt.backend.util.CurrentUser;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/v1/contracts")
//@RequiredArgsConstructor
//public class ContractSigningController {
//
//    private final ContractSigningService contractSigningService;
//    private final CurrentUser currentUser;
//    private final ContractService contractService;
//    private final ContractRepository contractRepository;
//    private final DigitalSignatureService digitalSignatureService;
//    private final CloudinaryService cloudinaryService;
//    private final FileStorageRepository fileStorageRepository;
//    private final PadesSigningService padesSigningService;
//    @PostMapping("/{contractId}/sign/prepares")
//    public ResponseEntity<?> prepareSigning(
//            @PathVariable UUID contractId,
//            @RequestParam UUID electronicSignatureId,
//            @RequestParam String keyCode,
//            @RequestParam("file") MultipartFile file
//    ) throws Exception {
//
//        UUID userId =
//                currentUser.getCurrentUser().getId();
//
//        contractRepository.findById(contractId)
//                .orElseThrow(() ->
//                        new IllegalArgumentException(
//                                "Contract not found"
//                        )
//                );
//
//        if (file == null || file.isEmpty()) {
//            throw new IllegalArgumentException(
//                    "PDF file is required"
//            );
//        }
//
//        PadesPrepareResponse result =
//                padesSigningService.prepare(
//                        contractId,
//                        userId,
//                        electronicSignatureId,
//                        keyCode,
//                        file.getBytes()
//                );
//
//        return ResponseEntity.ok(result);
//    }
//
//    @GetMapping("/test-download/{fileStorageId}")
//    public ResponseEntity<byte[]> testDownload(@PathVariable UUID fileStorageId) {
//        FileStorage fs = fileStorageRepository.findById(fileStorageId)
//                .orElseThrow(() -> new RuntimeException("Not found"));
//
//        byte[] data = cloudinaryService.download(fs);
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                .body(data);
//    }
//}