
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

import java.time.LocalDateTime;
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
            String signatureValue
    ) throws Exception {

        // =========================================
        // 1. Validate contract
        // =========================================

        if (contract == null) {
            throw new IllegalArgumentException(
                    "Contract not found"
            );
        }

        // =========================================
        // 2. Validate document
        // =========================================

        if (document == null || document.length == 0) {
            throw new IllegalArgumentException(
                    "Contract document is empty"
            );
        }

        // =========================================
        // 3. Validate signature value
        // =========================================

        if (signatureValue == null
                || signatureValue.isBlank()) {

            throw new IllegalArgumentException(
                    "Digital signature is required"
            );
        }

        // =========================================
        // 4. Get User RSA Key
        // =========================================

        UserKeys userKeys = userKeysRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User RSA key not found"
                        )
                );

        // =========================================
        // 5. Calculate document hash
        // =========================================

        DigitalSignatureService.SignatureResult result =
                digitalSignatureService.sign(
                        document,
                        userId
                );

        // =========================================
        // 6. Verify digital signature
        //
        // Frontend created signature using
        // user's private key.
        //
        // Backend verifies using public key.
        // =========================================

        boolean valid =
                verificationService.verify(
                        document,
                        signatureValue,
                        userId
                );

        if (!valid) {
            throw new IllegalArgumentException(
                    "Invalid digital signature"
            );
        }

        // =========================================
        // 7. Create Signature entity
        // =========================================

        Signature signature =
                Signature.builder()

                        // ---------------------------------
                        // Signature information
                        // ---------------------------------

                        .signatureName(
                                "Digital Signature"
                        )

                        .signatureType(
                                SignatureType.INTERNAL_RSA
                        )

                        // ---------------------------------
                        // Document hash
                        // ---------------------------------

                        .documentHash(
                                result.documentHash()
                        )

                        // ---------------------------------
                        // Actual RSA signature
                        // ---------------------------------

                        .signatureValue(
                                signatureValue
                        )

                        // ---------------------------------
                        // RSA
                        // ---------------------------------

                        .signatureAlgorithm(
                                SignatureAlgorithm.RSA
                        )

                        // ---------------------------------
                        // SHA-256
                        // ---------------------------------

                        .signatureHash(
                                SignatureHash.SHA256
                        )

                        // ---------------------------------
                        // No CA certificate
                        // ---------------------------------

                        .certificateSerial(null)

                        // ---------------------------------
                        // Signature status
                        // ---------------------------------

                        .status(
                                SignatureStatus.SIGNED
                        )

                        // ---------------------------------
                        // Signature creation time
                        // ---------------------------------

                        .signatureCreateAt(
                                LocalDateTime.now()
                        )

                        // ---------------------------------
                        // User RSA Key
                        // ---------------------------------

                        .userKey(
                                userKeys
                        )

                        // ---------------------------------
                        // Contract
                        // ---------------------------------

                        .contract(
                                contract
                        )

                        // ---------------------------------
                        // Visual electronic signature
                        // ---------------------------------

                        .electronicSignatures(
                                electronicSignature
                        )

                        // ---------------------------------
                        // Contract PDF
                        // ---------------------------------

                        .fileStorage(
                                contract.getDocumentFile()
                        )

                        .build();

        // =========================================
        // 8. Save signature
        // =========================================

        return signatureRepository.save(signature);
    }
}

