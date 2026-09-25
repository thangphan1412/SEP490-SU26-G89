package com.fpt.backend.controller.signatureController;

import com.fpt.backend.dto.response.signature.SignatureVerificationResponse;
import com.fpt.backend.dto.response.signature.UserKeyInfoResponse;
import com.fpt.backend.entity.Signature;
import com.fpt.backend.entity.UserKeys;
import com.fpt.backend.entity.Users;
import com.fpt.backend.repository.contract.ContractRepository;
import com.fpt.backend.repository.signature.SignatureRepository;

import com.fpt.backend.repository.signature.UserKeysRepository;
import com.fpt.backend.service.impl.signature.DigitalSignatureVerificationService;
import com.fpt.backend.service.impl.signature.ContractVerificationService;
import com.fpt.backend.service.impl.signature.PadesVerificationService;
import com.fpt.backend.service.impl.signature.UserKeyServiceImpl;
import com.fpt.backend.service.impl.CloudinaryService;
import com.fpt.backend.util.BaseResponse;
import com.fpt.backend.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/signature")
@RequiredArgsConstructor
public class SignatureController {

    private final CurrentUser currentUser;
    private final UserKeysRepository userKeysRepository;
    private final SignatureRepository signatureRepository;
    private final DigitalSignatureVerificationService verificationService;
    private final CloudinaryService cloudinaryService;
    private final UserKeyServiceImpl userKeyService;
    private final PadesVerificationService padesVerificationService;
    private final ContractRepository contractRepository;
    private final ContractVerificationService contractVerificationService;

    @GetMapping("/{contractId}/verify-stored-signatures")
    public ResponseEntity<?> verifyStoredSignatures(@PathVariable UUID contractId) throws Exception {
        return ResponseEntity.ok(new BaseResponse<>(contractVerificationService.verify(contractId)));
    }


    @GetMapping("/keys/me")
    public ResponseEntity<BaseResponse<UserKeyInfoResponse>> getMyPublicKey() {

        var user = currentUser.getCurrentUser();
        UUID userId = user.getId();

        UserKeyInfoResponse response =
                userKeysRepository.findByUserId(userId)
                        .map(this::toResponse)
                        .orElseGet(() ->
                                new UserKeyInfoResponse(
                                        false,
                                        null,
                                        null,
                                        null,
                                        0,
                                        null,
                                        null,
                                        null
                                )
                        );

        return ResponseEntity.ok(
                new BaseResponse<>(response)
        );
    }
    @PostMapping("/keys/generate")
    public ResponseEntity<BaseResponse<UserKeyInfoResponse>> generateKey() {

        Users user = currentUser.getCurrentUser();

        UserKeyInfoResponse response =
                userKeyService.generateUserKey(user);

        return ResponseEntity.ok(
                new BaseResponse<>(response)
        );
    }

    @PostMapping("/{contractId}/verify-signatures")
    public ResponseEntity<?> verifySignatures(
            @PathVariable UUID contractId,
            @RequestParam("file") MultipartFile file
    ) throws Exception {

        contractRepository.findById(contractId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Contract not found"
                        ));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "PDF file is empty"
            );
        }

        List<PadesVerificationService.PadesVerificationResult> results =
                padesVerificationService.verifyAll(
                        file.getBytes()
                );

        return ResponseEntity.ok(results);
    }

    private UserKeyInfoResponse toResponse(UserKeys key) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(key.getPublicKey().getBytes(StandardCharsets.UTF_8));
            String fingerprint = HexFormat.ofDelimiter(":").withUpperCase().formatHex(digest);
            return new UserKeyInfoResponse(
                    true, key.getPublicKey(),
                    key.getKeyAlgorithm().name(),key.getKeyCode(), key.getKeySize(), key.getCreateAt(), null, null
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to create public key fingerprint", exception);
        }
    }
}
