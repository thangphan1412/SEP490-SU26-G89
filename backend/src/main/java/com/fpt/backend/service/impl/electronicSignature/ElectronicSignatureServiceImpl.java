package com.fpt.backend.service.impl.electronicSignature;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fpt.backend.dto.request.electronicSignature.CreateElectronicSignatureRequest;
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

}
