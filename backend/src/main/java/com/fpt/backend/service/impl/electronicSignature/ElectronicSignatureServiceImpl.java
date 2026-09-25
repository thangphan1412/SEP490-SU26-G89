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
import com.fpt.backend.enums.ElectronicSignatureType;
import com.fpt.backend.enums.ElectronicStatus;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.repository.FileStorageRepository;
import com.fpt.backend.repository.electronicSignature.ElectronicSignatureRepository;
import com.fpt.backend.service.impl.CloudinaryService;
import com.fpt.backend.service.impl.signature.UserKeyServiceImpl;
import com.fpt.backend.service.impl.user.UserServiceImpl;
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
    @Autowired
    private UserKeyServiceImpl userKeyService;
    @Autowired
    private SignatureUpdateVerificationService updateVerificationService;
    @Autowired
    private com.fpt.backend.repository.user.UserRepository userRepository;
    @Override
    @org.springframework.transaction.annotation.Transactional
    public ElectronicSignatures createElectronicSignature(CreateElectronicSignatureRequest createElectronicSignatureRequest) {
        Users users = currentUser.getCurrentUser();
        userRepository.lockSignatureOwner(users.getId()).orElseThrow();
        if (createElectronicSignatureRequest.isDefault()) clearOtherDefaults(users.getId(), null);
        userKeyService.saveUserKey(users, createElectronicSignatureRequest.getPublicKey(), createElectronicSignatureRequest.getKeyCode(), createElectronicSignatureRequest.getCertificate());
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
    public List<ListElectronicResponse> getAllElectronicSignatures(
            String search,
            String type,
            String status
    ) {
        Users users = currentUser.getCurrentUser();

        ElectronicSignatureType signatureType = null;
        ElectronicStatus signatureStatus = null;

        if (type != null
                && !type.isBlank()
                && !type.equalsIgnoreCase("All")) {

            signatureType = ElectronicSignatureType.valueOf(
                    type.toUpperCase()
            );
        }

        if (status != null
                && !status.isBlank()
                && !status.equalsIgnoreCase("All")) {

            signatureStatus = ElectronicStatus.valueOf(
                    status.toUpperCase()
            );
        }

        String keyword =
                search == null || search.isBlank()
                        ? null
                        : search.trim();

        return electronicSignatureRepository.getAllElectronicSignaturesById(
                users.getId(),
                keyword,
                signatureType,
                signatureStatus
        );
    }

    @Override
    public ElectronicSignatureDetailResponse getElectronicSignatureDetail(UUID electronicSignatureId) {
        Users user = currentUser.getCurrentUser();
        return electronicSignatureRepository.getElectronicSignaturesById(user.getId(),electronicSignatureId);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public ElectronicSignatures updateElectronicSignature(
            UUID electronicSignatureId,
            UpdateElectronicSignatureRequest request,
            MultipartFile multipartFile
    ) {

        Users user = currentUser.getCurrentUser();
        userRepository.lockSignatureOwner(user.getId()).orElseThrow();
        ElectronicSignatures signature = electronicSignatureRepository.findById(electronicSignatureId)
                        .orElseThrow(() -> new BadHttpException("Electronic signature not found"));

        if (!signature.getUser().getId().equals(user.getId())) {
            throw new BadHttpException("You do not have permission to update this signature");
        }
        updateVerificationService.verify(electronicSignatureId,
                request.getVerificationChallengeId(), request.getVerificationSignature());
        if (request.isDefault()) clearOtherDefaults(user.getId(), signature.getId());
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

    private void clearOtherDefaults(UUID userId, UUID selectedId) {
        for (ElectronicSignatures existing : electronicSignatureRepository.findDefaultsByUserId(userId)) {
            if (!existing.getId().equals(selectedId)) {
                existing.setDefault(false);
                electronicSignatureRepository.save(existing);
            }
        }
    }

}
