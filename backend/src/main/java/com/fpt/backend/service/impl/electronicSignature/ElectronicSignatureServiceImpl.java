package com.fpt.backend.service.impl.electronicSignature;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fpt.backend.dto.request.electronicSignature.CreateElectronicSignatureRequest;
import com.fpt.backend.dto.request.electronicSignature.UpdateElectronicSignatureRequest;
import com.fpt.backend.dto.request.fileStorage.CreateFileStorageRequest;
import com.fpt.backend.dto.response.electronicSignature.ElectronicSignatureDetailResponse;
import com.fpt.backend.dto.response.electronicSignature.ListElectronicResponse;
import com.fpt.backend.entity.ElectronicSignatures;
import com.fpt.backend.entity.FileStorage;
import com.fpt.backend.entity.UserKeys;
import com.fpt.backend.entity.Users;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.repository.FileStorageRepository;
import com.fpt.backend.repository.electronicSignature.ElectronicSignatureRepository;
import com.fpt.backend.service.impl.CloudinaryService;
import com.fpt.backend.service.interfaces.electronicSignature.IElectronicSignatureService;
import com.fpt.backend.util.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ElectronicSignatureServiceImpl implements IElectronicSignatureService {
    @Autowired
    private ElectronicSignatureRepository  electronicSignatureRepository;
    @Autowired
    private CurrentUser  currentUser;
    @Autowired
    private Cloudinary  cloudinary;
    @Autowired
    private CloudinaryService cloudinaryService;
    @Autowired
    private FileStorageRepository fileStorageRepository;
    @Override
    public ElectronicSignatures createElectronicSignature(CreateElectronicSignatureRequest createElectronicSignatureRequest) {
        Users users = currentUser.getCurrentUser();
        MultipartFile img = createElectronicSignatureRequest.getCreateFileStorageRequests().getMultipartFile();
        FileStorage fileStorage = cloudinaryService.uploadAndSave(img, users);
        ElectronicSignatures electronicSignatures = ElectronicSignatures.builder()
                .electronicSignatureName(createElectronicSignatureRequest.getElectronicSignatureName())
                .electronicSignatureType(createElectronicSignatureRequest.getElectronicSignatureType())
                .status(createElectronicSignatureRequest.getElectronicStatus())
                .isDefault(createElectronicSignatureRequest.isDefault())
                .createdAt(LocalDate.now())
                .fileStorage(fileStorage)
                .user(users)
                .build();
        return electronicSignatureRepository.save(electronicSignatures);
    }

    @Override
    public List<ListElectronicResponse> getAllElectronicSignatures() {
        Users users = currentUser.getCurrentUser();
        return electronicSignatureRepository.getAllElectronicSignaturesById(users.getId());
    }

    @Override
    public ElectronicSignatureDetailResponse getElectronicSignatureDetail(UUID electronicSignatureId) {
        Users user = currentUser.getCurrentUser();
        return electronicSignatureRepository.getElectronicSignaturesById(user.getId(),electronicSignatureId);
    }

    @Override
    public ElectronicSignatures updateElectronicSignature(
            UUID electronicSignatureId,
            UpdateElectronicSignatureRequest request,
            MultipartFile multipartFile
    ) {

        Users user = currentUser.getCurrentUser();
        ElectronicSignatures signature = electronicSignatureRepository.findById(electronicSignatureId)
                        .orElseThrow(() -> new BadHttpException("Electronic signature not found"));

        if (!signature.getUser().getId().equals(user.getId())) {
            throw new BadHttpException("You do not have permission to update this signature");
        }
        signature.setElectronicSignatureName(request.getElectronicSignatureName());
        signature.setElectronicSignatureType(request.getElectronicSignatureType());
        signature.setStatus(request.getElectronicStatus());
        signature.setDefault(request.isDefault());
        if (multipartFile != null && !multipartFile.isEmpty()) {
            FileStorage newFileStorage = cloudinaryService.uploadAndSave(multipartFile, user);
            signature.setFileStorage(newFileStorage);
        }
        signature.setUpdatedAt(LocalDate.now());
        return electronicSignatureRepository.save(
                signature
        );
    }

}
