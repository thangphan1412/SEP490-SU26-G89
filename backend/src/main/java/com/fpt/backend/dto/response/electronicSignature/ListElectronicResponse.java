package com.fpt.backend.dto.response.electronicSignature;

import com.fpt.backend.enums.ElectronicSignatureType;
import com.fpt.backend.enums.ElectronicStatus;
import com.fpt.backend.enums.KeyAlgorithm;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.hibernate.loader.ast.spi.Loadable;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ListElectronicResponse {
    private UUID id;
    private String signatureName;
    @Enumerated(EnumType.STRING)
    private ElectronicSignatureType type;
    @Enumerated(EnumType.STRING)
    private ElectronicStatus status;
    private boolean isDefault;
    private LocalDate uploadAt;
    private String imageUrl;
    private String publicKey;
    private String publicKeyFingerprint;
    private KeyAlgorithm keyAlgorithm;
    private Long keySize;
}
