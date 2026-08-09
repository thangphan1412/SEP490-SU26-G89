package com.fpt.backend.service.interfaces.electronicSignature;

import com.fpt.backend.dto.request.electronicSignature.CreateElectronicSignatureRequest;
import com.fpt.backend.dto.request.electronicSignature.UpdateElectronicSignatureRequest;
import com.fpt.backend.dto.response.electronicSignature.ElectronicSignatureDetailResponse;
import com.fpt.backend.dto.response.electronicSignature.ListElectronicResponse;
import com.fpt.backend.entity.ElectronicSignatures;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface IElectronicSignatureService {
    ElectronicSignatures createElectronicSignature(CreateElectronicSignatureRequest createElectronicSignatureRequest);
    List<ListElectronicResponse> getAllElectronicSignatures();
    ElectronicSignatureDetailResponse getElectronicSignatureDetail(UUID electronicSignatureId);
    ElectronicSignatures updateElectronicSignature(UUID electronicSignatureId, UpdateElectronicSignatureRequest request, MultipartFile multipartFile);
}
