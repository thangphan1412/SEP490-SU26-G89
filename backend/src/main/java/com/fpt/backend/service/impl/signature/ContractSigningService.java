
package com.fpt.backend.service.impl.signature;

import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.ElectronicSignatures;
import com.fpt.backend.entity.Signature;
import com.fpt.backend.entity.UserKeys;
import com.fpt.backend.enums.SignatureAlgorithm;
import com.fpt.backend.enums.SignatureHash;
import com.fpt.backend.enums.SignatureStatus;
import com.fpt.backend.enums.SignatureType;
import com.fpt.backend.repository.signature.SignatureRepository;
import com.fpt.backend.repository.signature.UserKeysRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractSigningService {

    private final DigitalSignatureService digitalSignatureService;

    private final DigitalSignatureVerificationService verificationService;

    private final SignatureRepository signatureRepository;

    private final UserKeysRepository userKeysRepository;

    @Transactional
    public Signature signContract(
            Contracts contract,
            byte[] document,
            UUID userId,
            ElectronicSignatures electronicSignature,
            String signatureValue,
            String  keyCode

    ) throws Exception {
        if (contract == null) { throw new IllegalArgumentException("Contract not found");}
        if (document == null || document.length == 0) {
            throw new IllegalArgumentException(
                    "Contract document is empty"
            );
        }
        if (signatureValue == null || signatureValue.isBlank()) {
            throw new IllegalArgumentException(
                    "Digital signature is required"
            );
        }
        UserKeys userKeys = userKeysRepository.findByUserIdAndKeyCode(
                        userId,
                      keyCode
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User RSA key not found"
                        )
                );
        String documentHash =
                Base64.getEncoder().encodeToString(
                        MessageDigest.getInstance("SHA-256")
                                .digest(document)
                );
        Signature signature = Signature.builder()
                        .signatureName("Digital Signature")
                        .signatureType(SignatureType.INTERNAL_RSA)
                        .documentHash(documentHash)
                        .signatureValue(signatureValue)
                        .signatureAlgorithm(SignatureAlgorithm.RSA)
                        .signatureHash(SignatureHash.SHA256)
                        .certificateSerial(null)
                        .status(SignatureStatus.SIGNED)
                        .signatureCreateAt(LocalDateTime.now())
                        .userKey(userKeys)
                        .contract(contract)
                        .electronicSignatures(electronicSignature)
                        .fileStorage(contract.getDocumentFile())
                        .build();
        return signatureRepository.save(signature);
    }
}

