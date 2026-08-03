package com.fpt.backend.service.interfaces.electronicSignature;

import com.fpt.backend.dto.request.electronicSignature.CreateElectronicSignatureRequest;
import com.fpt.backend.entity.ElectronicSignatures;

public interface IElectronicSignatureService {
    ElectronicSignatures createElectronicSignature(CreateElectronicSignatureRequest createElectronicSignatureRequest);
}
