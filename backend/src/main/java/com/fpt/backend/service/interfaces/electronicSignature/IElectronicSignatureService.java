package com.fpt.backend.service.interfaces.electronicSignature;

import com.fpt.backend.dto.request.electronicSignature.CreateElectronicSignatureRequest;
import com.fpt.backend.dto.response.electronicSignature.ListElectronicResponse;
import com.fpt.backend.entity.ElectronicSignatures;

import java.util.List;

public interface IElectronicSignatureService {
    ElectronicSignatures createElectronicSignature(CreateElectronicSignatureRequest createElectronicSignatureRequest);
    List<ListElectronicResponse> getAllElectronicSignatures();
}
