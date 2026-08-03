package com.fpt.backend.dto.request.electronicSignature;

import com.fpt.backend.dto.request.fileStorage.CreateFileStorageRequest;
import com.fpt.backend.enums.ElectronicSignatureType;
import com.fpt.backend.enums.ElectronicStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateElectronicSignatureRequest {
    private String electronicSignatureName;
    @Enumerated(EnumType.STRING)
    private ElectronicSignatureType electronicSignatureType;
    private boolean isDefault;
    @Enumerated(EnumType.STRING)
    private ElectronicStatus  electronicStatus;
    private LocalDate createdAt;
    private CreateFileStorageRequest createFileStorageRequests;
}
