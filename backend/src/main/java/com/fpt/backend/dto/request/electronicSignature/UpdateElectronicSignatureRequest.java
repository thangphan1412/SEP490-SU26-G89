package com.fpt.backend.dto.request.electronicSignature;

import com.fpt.backend.enums.ElectronicSignatureType;
import com.fpt.backend.enums.ElectronicStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateElectronicSignatureRequest {

    private String electronicSignatureName;
    @Enumerated(EnumType.STRING)
    private ElectronicSignatureType electronicSignatureType;

    private boolean isDefault;
    @Enumerated(EnumType.STRING)
    private ElectronicStatus electronicStatus;
}
