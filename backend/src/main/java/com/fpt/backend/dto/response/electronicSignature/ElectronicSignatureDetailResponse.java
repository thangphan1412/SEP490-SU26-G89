package com.fpt.backend.dto.response.electronicSignature;

import com.fpt.backend.enums.ElectronicSignatureType;
import com.fpt.backend.enums.ElectronicStatus;
import com.fpt.backend.enums.SignatureStatus;
import com.fpt.backend.enums.SignatureType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ElectronicSignatureDetailResponse {
    private String electronicSignatureName;
    @Enumerated(EnumType.STRING)
    private ElectronicSignatureType electronicSignatureType;
    @Enumerated(EnumType.STRING)
    private ElectronicStatus electronicSignatureStatus;
    private boolean isDefault;
    private LocalDate createAt;
}
