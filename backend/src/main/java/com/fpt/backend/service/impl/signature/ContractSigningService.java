package com.fpt.backend.service.impl.signature;

import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.ElectronicSignatures;
import com.fpt.backend.entity.Signature;
import com.fpt.backend.enums.SignatureAlgorithm;
import com.fpt.backend.enums.SignatureHash;
import com.fpt.backend.enums.SignatureStatus;
import com.fpt.backend.enums.SignatureType;
import com.fpt.backend.repository.signature.SignatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractSigningService {

    private final DigitalSignatureService digitalSignatureService;
    private final SignatureRepository signatureRepository;

    @Transactional
    public Signature signContract(
            Contracts contract,
            byte[] document,
            UUID userId,
            ElectronicSignatures electronicSignature
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
        // 3. Digital Signature
        // =========================================

        DigitalSignatureService.SignatureResult result =
                digitalSignatureService.sign(
                        document,
                        userId
                );

        // =========================================
        // 4. Create Signature entity
        // =========================================

        Signature signature =
                Signature.builder()

                        // Tên chữ ký
                        .signatureName(
                                "Digital Signature"
                        )

                        // Loại chữ ký
                        .signatureType(
                                SignatureType.IMAGE_SIGNATURE
                        )

                        // SHA-256(PDF)
                        .documentHash(
                                result.documentHash()
                        )

                        // RSA
                        .signatureAlgorithm(
                                SignatureAlgorithm.RSA
                        )

                        // Hash algorithm
                        .signatureHash(
                                SignatureHash.SHA256
                        )

                        // Chưa dùng CA
                        .certificateSerial(null)

                        // Signature status
                        .status(
                                SignatureStatus.SIGNED
                        )

                        // Time
                        .signatureCreateAt(
                                LocalDateTime.now()
                        )

                        // User's RSA key
                        .userKey(
                                result.userKey()
                        )

                        // Contract
                        .contract(contract)

                        // The visual electronic signature selected by the signer.
                        .electronicSignatures(electronicSignature)
                        .fileStorage(electronicSignature.getFileStorage())

                        .build();

        // =========================================
        // 5. Save
        // =========================================

        return signatureRepository.save(signature);
    }
}
