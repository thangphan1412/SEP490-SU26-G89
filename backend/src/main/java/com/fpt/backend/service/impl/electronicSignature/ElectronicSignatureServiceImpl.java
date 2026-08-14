package com.fpt.backend.service.impl.electronicSignature;

import com.fpt.backend.dto.request.electronicSignature.CreateElectronicSignatureRequest;
import com.fpt.backend.dto.request.electronicSignature.UpdateElectronicSignatureRequest;
import com.fpt.backend.dto.response.electronicSignature.ElectronicSignatureDetailResponse;
import com.fpt.backend.dto.response.electronicSignature.ListElectronicResponse;
import com.fpt.backend.entity.ElectronicSignatures;
import com.fpt.backend.entity.FileStorage;
import com.fpt.backend.entity.UserKeys;
import com.fpt.backend.entity.Users;
import com.fpt.backend.enums.KeyAlgorithm;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.repository.electronicSignature.ElectronicSignatureRepository;
import com.fpt.backend.repository.signature.UserKeyRepository;
import com.fpt.backend.service.impl.CloudinaryService;
import com.fpt.backend.service.impl.contract.ContractSignatureService;
import com.fpt.backend.service.interfaces.electronicSignature.IElectronicSignatureService;
import com.fpt.backend.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ElectronicSignatureServiceImpl implements IElectronicSignatureService {
    private final ElectronicSignatureRepository electronicSignatureRepository;
    private final UserKeyRepository userKeyRepository;
    private final CurrentUser currentUser;
    private final CloudinaryService cloudinaryService;
    private final ContractSignatureService contractSignatureService;

    @Override
    @Transactional
    public ElectronicSignatures createElectronicSignature(CreateElectronicSignatureRequest createElectronicSignatureRequest) {
        Users users = currentUser.getCurrentUser();
        ContractSignatureService.RegisteredPublicKey registeredPublicKey =
                contractSignatureService.validatePublicKey(
                        createElectronicSignatureRequest.getPublicKey()
                );
        UserKeys userKey = saveUserKey(registeredPublicKey, users);
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
                .userKey(userKey)
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
    @Transactional
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
        registerLegacyKeyIfNeeded(signature, request.getPublicKey(), user);
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

    private void registerLegacyKeyIfNeeded(
            ElectronicSignatures signature,
            String requestedPublicKey,
            Users user
    ) {
        if (requestedPublicKey == null || requestedPublicKey.isBlank()) {
            return;
        }
        ContractSignatureService.RegisteredPublicKey registeredPublicKey =
                contractSignatureService.validatePublicKey(requestedPublicKey);
        UserKeys existingKey = signature.getUserKey();
        if (existingKey != null) {
            if (!registeredPublicKey.publicKeyFingerprint().equalsIgnoreCase(
                    existingKey.getKeyFingerprint()
            )) {
                throw new BadHttpException(
                        "The RSA key of an existing signature cannot be replaced"
                );
            }
            return;
        }
        signature.setUserKey(saveUserKey(registeredPublicKey, user));
    }

    private UserKeys saveUserKey(
            ContractSignatureService.RegisteredPublicKey registeredPublicKey,
            Users user
    ) {
        return userKeyRepository.save(UserKeys.builder()
                .keySize(registeredPublicKey.keySize())
                .createAt(LocalDateTime.now())
                .publicKey(registeredPublicKey.publicKeyPem())
                .keyFingerprint(registeredPublicKey.publicKeyFingerprint())
                .keyAlgorithm(KeyAlgorithm.RSA)
                .user(user)
                .build());
    }

}
