package com.fpt.backend.dto.request.signature;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SignatureRequest {
    private String signatureName;
    private String signatureType;
    private String status;
    private String accessScope;

}
