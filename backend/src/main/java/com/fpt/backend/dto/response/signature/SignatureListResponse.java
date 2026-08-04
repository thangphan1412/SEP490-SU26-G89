package com.fpt.backend.dto.response.signature;

import com.fpt.backend.dto.response.electronicSignature.ListElectronicResponse;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignatureListResponse {
    private String signatureName;
    private ListElectronicResponse electronic;
}
