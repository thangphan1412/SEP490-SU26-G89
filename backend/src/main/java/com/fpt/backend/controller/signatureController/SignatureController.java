package com.fpt.backend.controller.signatureController;

import com.fpt.backend.dto.response.signature.SignatureVerificationResponse;
import com.fpt.backend.dto.response.signature.UserKeyInfoResponse;
import com.fpt.backend.entity.Signature;
import com.fpt.backend.entity.UserKeys;
import com.fpt.backend.repository.signature.SignatureRepository;

import com.fpt.backend.repository.signature.UserKeysRepository;
import com.fpt.backend.service.impl.signature.DigitalSignatureVerificationService;
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

    @GetMapping("/keys/me")
    public ResponseEntity<BaseResponse<UserKeyInfoResponse>> getMyPublicKey() {
        var user = currentUser.getCurrentUser();
        UUID userId = user.getId();
        UserKeyInfoResponse response = userKeysRepository.findByUserId(userId)
                .map(this::toResponse)
                .orElseGet(() -> toResponse(userKeyService.generateUserKey(user)));
        return ResponseEntity.ok(new BaseResponse<>(response));
    }

    @PostMapping("/{signatureId}/verify")
    public ResponseEntity<BaseResponse<SignatureVerificationResponse>> verify(
            @PathVariable UUID signatureId,
            @RequestParam("file") MultipartFile file
    ) throws Exception {
        Signature signature = signatureRepository.findById(signatureId)
                .orElseThrow(() -> new IllegalArgumentException("Signature not found"));
        if (signature.getSignatureValue() == null || signature.getUserKey() == null) {
            throw new IllegalArgumentException("Signature value or public key is unavailable");
        }
        UUID signerId = signature.getUserKey().getUser().getId();
        boolean valid = verificationService.verify(file.getBytes(), signature.getSignatureValue(), signerId);
        SignatureVerificationResponse response = new SignatureVerificationResponse(
                signature.getId(), signature.getContract().getId(), signerId,
                signature.getDocumentHash(), valid
        );
        return ResponseEntity.ok(new BaseResponse<>(response));
    }

    @GetMapping("/{signatureId}/verify-stored")
    public ResponseEntity<BaseResponse<SignatureVerificationResponse>> verifyStoredPdf(
            @PathVariable UUID signatureId
    ) throws Exception {
        Signature signature = signatureRepository.findById(signatureId)
                .orElseThrow(() -> new IllegalArgumentException("Signature not found"));
        if (signature.getFileStorage() == null
                || signature.getSignatureValue() == null
                || signature.getUserKey() == null) {
            throw new IllegalArgumentException("Stored PDF, signature value or public key is unavailable");
        }
        byte[] pdf = cloudinaryService.download(signature.getFileStorage());
        UUID signerId = signature.getUserKey().getUser().getId();
        boolean valid = verificationService.verify(pdf, signature.getSignatureValue(), signerId);
        SignatureVerificationResponse response = new SignatureVerificationResponse(
                signature.getId(), signature.getContract().getId(), signerId,
                signature.getDocumentHash(), valid
        );
        return ResponseEntity.ok(new BaseResponse<>(response));
    }

    private UserKeyInfoResponse toResponse(UserKeys key) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(key.getPublicKey().getBytes(StandardCharsets.UTF_8));
            String fingerprint = HexFormat.ofDelimiter(":").withUpperCase().formatHex(digest);
            return new UserKeyInfoResponse(
                    true, key.getPublicKey(), fingerprint,
                    key.getKeyAlgorithm().name(), key.getKeySize(), key.getCreateAt()
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to create public key fingerprint", exception);
        }
    }
}
