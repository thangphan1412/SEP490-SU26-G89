package com.fpt.backend.dto.request.electronicSignature;

import com.fpt.backend.enums.ElectronicSignatureType;
import com.fpt.backend.enums.ElectronicStatus;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateElectronicSignatureRequest {

    private String electronicSignatureName;

    private ElectronicSignatureType electronicSignatureType;

    private boolean isDefault;

    private ElectronicStatus electronicStatus;

    private String publicKey;
}
