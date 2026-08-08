package com.fpt.backend.service.interfaces.electronicSignature;

import com.fpt.backend.dto.request.electronicSignature.CreateElectronicSignatureRequest;
import com.fpt.backend.dto.response.electronicSignature.ElectronicSignatureDetailResponse;
import com.fpt.backend.dto.response.electronicSignature.ListElectronicResponse;
import com.fpt.backend.entity.ElectronicSignatures;

import java.util.List;
import java.util.UUID;

public interface IElectronicSignatureService {
    ElectronicSignatures createElectronicSignature(CreateElectronicSignatureRequest createElectronicSignatureRequest);
    List<ListElectronicResponse> getAllElectronicSignatures();
    ElectronicSignatureDetailResponse getElectronicSignatureDetail(UUID electronicSignatureId);
}
