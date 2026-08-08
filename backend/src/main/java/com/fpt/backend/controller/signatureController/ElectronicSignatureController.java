package com.fpt.backend.controller.signatureController;

import com.fpt.backend.constant.ApiConstant;
import com.fpt.backend.dto.request.electronicSignature.CreateElectronicSignatureRequest;
import com.fpt.backend.dto.request.fileStorage.CreateFileStorageRequest;
import com.fpt.backend.dto.response.electronicSignature.CreateElectronicSignature;
import com.fpt.backend.dto.response.electronicSignature.ElectronicSignatureDetailResponse;
import com.fpt.backend.dto.response.electronicSignature.ListElectronicResponse;
import com.fpt.backend.entity.BaseEntity;
import com.fpt.backend.service.impl.electronicSignature.ElectronicSignatureServiceImpl;
import com.fpt.backend.util.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstant.API)
public class ElectronicSignatureController {
    @Autowired
    private ElectronicSignatureServiceImpl electronicSignatureService;
    @PostMapping(ApiConstant.Signatures.ELECTRONICSIGNATURES)
    public ResponseEntity<BaseResponse<?>> createElectronic(
            @ModelAttribute CreateElectronicSignatureRequest createElectronicSignatureRequest,
            @RequestParam("multipartFile") MultipartFile multipartFile){
        createElectronicSignatureRequest.setCreateFileStorageRequests(
                CreateFileStorageRequest.builder()
                        .multipartFile(multipartFile)
                        .build()
        );

        electronicSignatureService.createElectronicSignature(createElectronicSignatureRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new BaseResponse<>());
    }
    @GetMapping(ApiConstant.Signatures.SIGNATURES)
    public ResponseEntity<BaseResponse<List<ListElectronicResponse>>> getAll() {
        return ResponseEntity.ok(new BaseResponse<>(electronicSignatureService.getAllElectronicSignatures()));
    }

    @GetMapping(ApiConstant.Signatures.ELECTRONICBYID)
    public ResponseEntity<BaseResponse<ElectronicSignatureDetailResponse>> getElectronicById(@PathVariable("id") UUID electronicSignatureId) {
        return ResponseEntity.ok(new BaseResponse<>(electronicSignatureService.getElectronicSignatureDetail(electronicSignatureId)));
    }
}
