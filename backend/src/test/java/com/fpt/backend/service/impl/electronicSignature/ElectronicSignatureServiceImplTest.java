//package com.fpt.backend.service.impl.electronicSignature;
//
//import com.cloudinary.Cloudinary;
//import com.fpt.backend.dto.request.electronicSignature.CreateElectronicSignatureRequest;
//import com.fpt.backend.dto.request.electronicSignature.UpdateElectronicSignatureRequest;
//import com.fpt.backend.dto.request.fileStorage.CreateFileStorageRequest;
//import com.fpt.backend.dto.response.electronicSignature.ElectronicSignatureDetailResponse;
//import com.fpt.backend.dto.response.electronicSignature.ListElectronicResponse;
//import com.fpt.backend.entity.ElectronicSignatures;
//import com.fpt.backend.entity.FileStorage;
//import com.fpt.backend.entity.Users;
//import com.fpt.backend.enums.ElectronicSignatureType;
//import com.fpt.backend.enums.ElectronicStatus;
//import com.fpt.backend.exception.BadHttpException;
//import com.fpt.backend.repository.FileStorageRepository;
//import com.fpt.backend.repository.electronicSignature.ElectronicSignatureRepository;
//import com.fpt.backend.service.impl.CloudinaryService;
//import com.fpt.backend.service.impl.signature.UserKeyServiceImpl;
//import com.fpt.backend.util.CurrentUser;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class ElectronicSignatureServiceImplTest {
//
//    @Mock
//    private ElectronicSignatureRepository electronicSignatureRepository;
//
//    @Mock
//    private CurrentUser currentUser;
//
//    @Mock
//    private Cloudinary cloudinary;
//
//    @Mock
//    private CloudinaryService cloudinaryService;
//
//    @Mock
//    private FileStorageRepository fileStorageRepository;
//
//    @Mock
//    private UserKeyServiceImpl userKeyService;
//
//    @InjectMocks
//    private ElectronicSignatureServiceImpl electronicSignatureService;
//
//    @Test
//    void createElectronicSignature_savesKeyFileAndSignatureForCurrentUser() {
//        Users user = user(UUID.randomUUID());
//        MultipartFile image = org.mockito.Mockito.mock(MultipartFile.class);
//        FileStorage storedFile = fileStorage("https://cdn.example.com/signature.png");
//        CreateElectronicSignatureRequest request = createRequest(image);
//
//        when(currentUser.getCurrentUser()).thenReturn(user);
//        when(cloudinaryService.uploadAndSave(image, user)).thenReturn(storedFile);
//        when(electronicSignatureRepository.save(any(ElectronicSignatures.class)))
//                .thenAnswer(invocation -> invocation.getArgument(0, ElectronicSignatures.class));
//
//        ElectronicSignatures result = electronicSignatureService.createElectronicSignature(request);
//
//        ArgumentCaptor<ElectronicSignatures> signatureCaptor =
//                ArgumentCaptor.forClass(ElectronicSignatures.class);
//        verify(electronicSignatureRepository).save(signatureCaptor.capture());
//        ElectronicSignatures saved = signatureCaptor.getValue();
//        assertThat(result).isSameAs(saved);
//        assertThat(saved.getElectronicSignatureName()).isEqualTo("Contract approval signature");
//        assertThat(saved.getElectronicSignatureType()).isEqualTo(ElectronicSignatureType.UPLOADED);
//        assertThat(saved.getStatus()).isEqualTo(ElectronicStatus.ACTIVE);
//        assertThat(saved.isDefault()).isTrue();
//        assertThat(saved.getCreatedAt()).isEqualTo(LocalDate.now());
//        assertThat(saved.getFileStorage()).isSameAs(storedFile);
//        assertThat(saved.getUser()).isSameAs(user);
//        verify(userKeyService).saveUserKey(user, "public-key", "123456", "certificate-data");
//        verify(cloudinaryService).uploadAndSave(image, user);
//    }
//
//    @Test
//    void getAllElectronicSignatures_returnsRepositoryResultForCurrentUser() {
//        UUID userId = UUID.randomUUID();
//        Users user = user(userId);
//        List<ListElectronicResponse> expected = List.of(new ListElectronicResponse(
//                UUID.randomUUID(),
//                "Contract approval signature",
//                ElectronicSignatureType.UPLOADED,
//                ElectronicStatus.ACTIVE,
//                true,
//                LocalDate.of(2026, 9, 18),
//                "https://cdn.example.com/signature.png"
//        ));
//        when(currentUser.getCurrentUser()).thenReturn(user);
//        when(electronicSignatureRepository.getAllElectronicSignaturesById(userId)).thenReturn(expected);
//
//        List<ListElectronicResponse> result = electronicSignatureService.getAllElectronicSignatures();
//
//        assertThat(result).isSameAs(expected);
//        verify(electronicSignatureRepository).getAllElectronicSignaturesById(userId);
//    }
//
//    @Test
//    void getElectronicSignatureDetail_returnsRepositoryResultForCurrentUserAndId() {
//        UUID userId = UUID.randomUUID();
//        UUID signatureId = UUID.randomUUID();
//        Users user = user(userId);
//        ElectronicSignatureDetailResponse expected = new ElectronicSignatureDetailResponse(
//                "Contract approval signature",
//                ElectronicSignatureType.DRAW,
//                ElectronicStatus.ACTIVE,
//                false,
//                LocalDate.of(2026, 9, 18),
//                "https://cdn.example.com/signature.png"
//        );
//        when(currentUser.getCurrentUser()).thenReturn(user);
//        when(electronicSignatureRepository.getElectronicSignaturesById(userId, signatureId))
//                .thenReturn(expected);
//
//        ElectronicSignatureDetailResponse result =
//                electronicSignatureService.getElectronicSignatureDetail(signatureId);
//
//        assertThat(result).isSameAs(expected);
//        verify(electronicSignatureRepository).getElectronicSignaturesById(userId, signatureId);
//    }
//
//    @Test
//    void updateElectronicSignature_updatesMetadataWithoutUploadingWhenFileIsAbsent() {
//        UUID userId = UUID.randomUUID();
//        UUID signatureId = UUID.randomUUID();
//        Users user = user(userId);
//        ElectronicSignatures signature = signature(signatureId, user, fileStorage("old-url"));
//        UpdateElectronicSignatureRequest request = updateRequest("Updated signature", ElectronicSignatureType.DRAW,
//                ElectronicStatus.INACTIVE, false);
//        when(currentUser.getCurrentUser()).thenReturn(user);
//        when(electronicSignatureRepository.findById(signatureId)).thenReturn(Optional.of(signature));
//        when(electronicSignatureRepository.save(signature)).thenReturn(signature);
//
//        ElectronicSignatures result =
//                electronicSignatureService.updateElectronicSignature(signatureId, request, null);
//
//        assertThat(result).isSameAs(signature);
//        assertThat(signature.getElectronicSignatureName()).isEqualTo("Updated signature");
//        assertThat(signature.getElectronicSignatureType()).isEqualTo(ElectronicSignatureType.DRAW);
//        assertThat(signature.getStatus()).isEqualTo(ElectronicStatus.INACTIVE);
//        assertThat(signature.isDefault()).isFalse();
//        assertThat(signature.getUpdatedAt()).isEqualTo(LocalDate.now());
//        verify(cloudinaryService, never()).uploadAndSave(any(MultipartFile.class), any(Users.class));
//        verify(electronicSignatureRepository).save(signature);
//    }
//
//    @Test
//    void updateElectronicSignature_replacesFileWhenNonEmptyFileIsProvided() {
//        UUID userId = UUID.randomUUID();
//        UUID signatureId = UUID.randomUUID();
//        Users user = user(userId);
//        FileStorage previousFile = fileStorage("old-url");
//        FileStorage replacementFile = fileStorage("new-url");
//        ElectronicSignatures signature = signature(signatureId, user, previousFile);
//        UpdateElectronicSignatureRequest request = updateRequest("Updated signature", ElectronicSignatureType.UPLOADED,
//                ElectronicStatus.ACTIVE, true);
//        MultipartFile replacementUpload = org.mockito.Mockito.mock(MultipartFile.class);
//        when(replacementUpload.isEmpty()).thenReturn(false);
//        when(currentUser.getCurrentUser()).thenReturn(user);
//        when(electronicSignatureRepository.findById(signatureId)).thenReturn(Optional.of(signature));
//        when(cloudinaryService.uploadAndSave(replacementUpload, user)).thenReturn(replacementFile);
//        when(electronicSignatureRepository.save(signature)).thenReturn(signature);
//
//        ElectronicSignatures result = electronicSignatureService.updateElectronicSignature(
//                signatureId,
//                request,
//                replacementUpload
//        );
//
//        assertThat(result).isSameAs(signature);
//        assertThat(signature.getFileStorage()).isSameAs(replacementFile);
//        verify(cloudinaryService).uploadAndSave(replacementUpload, user);
//        verify(electronicSignatureRepository).save(signature);
//    }
//
//    @Test
//    void updateElectronicSignature_throwsWhenSignatureDoesNotExist() {
//        UUID userId = UUID.randomUUID();
//        UUID signatureId = UUID.randomUUID();
//        Users user = user(userId);
//        when(currentUser.getCurrentUser()).thenReturn(user);
//        when(electronicSignatureRepository.findById(signatureId)).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> electronicSignatureService.updateElectronicSignature(
//                signatureId,
//                updateRequest("Updated signature", ElectronicSignatureType.DRAW, ElectronicStatus.ACTIVE, false),
//                null
//        ))
//                .isInstanceOf(BadHttpException.class)
//                .hasMessage("Electronic signature not found");
//
//        verify(electronicSignatureRepository, never()).save(any(ElectronicSignatures.class));
//    }
//
//    @Test
//    void updateElectronicSignature_throwsWhenCurrentUserDoesNotOwnSignature() {
//        UUID actorId = UUID.randomUUID();
//        UUID ownerId = UUID.randomUUID();
//        UUID signatureId = UUID.randomUUID();
//        Users actor = user(actorId);
//        Users owner = user(ownerId);
//        ElectronicSignatures signature = signature(signatureId, owner, fileStorage("old-url"));
//        when(currentUser.getCurrentUser()).thenReturn(actor);
//        when(electronicSignatureRepository.findById(signatureId)).thenReturn(Optional.of(signature));
//
//        assertThatThrownBy(() -> electronicSignatureService.updateElectronicSignature(
//                signatureId,
//                updateRequest("Updated signature", ElectronicSignatureType.DRAW, ElectronicStatus.ACTIVE, false),
//                null
//        ))
//                .isInstanceOf(BadHttpException.class)
//                .hasMessage("You do not have permission to update this signature");
//
//        verify(cloudinaryService, never()).uploadAndSave(any(MultipartFile.class), any(Users.class));
//        verify(electronicSignatureRepository, never()).save(any(ElectronicSignatures.class));
//    }
//
//    private static CreateElectronicSignatureRequest createRequest(MultipartFile image) {
//        CreateElectronicSignatureRequest request = new CreateElectronicSignatureRequest();
//        request.setElectronicSignatureName("Contract approval signature");
//        request.setElectronicSignatureType(ElectronicSignatureType.UPLOADED);
//        request.setDefault(true);
//        request.setElectronicStatus(ElectronicStatus.ACTIVE);
//        request.setCreateFileStorageRequests(CreateFileStorageRequest.builder().multipartFile(image).build());
//        request.setPublicKey("public-key");
//        request.setKeyCode("123456");
//        request.setCertificate("certificate-data");
//        return request;
//    }
//
//    private static UpdateElectronicSignatureRequest updateRequest(
//            String name,
//            ElectronicSignatureType type,
//            ElectronicStatus status,
//            boolean isDefault
//    ) {
//        UpdateElectronicSignatureRequest request = new UpdateElectronicSignatureRequest();
//        request.setElectronicSignatureName(name);
//        request.setElectronicSignatureType(type);
//        request.setElectronicStatus(status);
//        request.setDefault(isDefault);
//        return request;
//    }
//
//    private static ElectronicSignatures signature(UUID id, Users owner, FileStorage fileStorage) {
//        ElectronicSignatures signature = new ElectronicSignatures();
//        signature.setId(id);
//        signature.setUser(owner);
//        signature.setFileStorage(fileStorage);
//        signature.setElectronicSignatureName("Original signature");
//        signature.setElectronicSignatureType(ElectronicSignatureType.UPLOADED);
//        signature.setStatus(ElectronicStatus.ACTIVE);
//        signature.setDefault(true);
//        return signature;
//    }
//
//    private static Users user(UUID id) {
//        Users user = new Users();
//        user.setId(id);
//        user.setEmail("user-" + id + "@example.com");
//        return user;
//    }
//
//    private static FileStorage fileStorage(String url) {
//        FileStorage fileStorage = new FileStorage();
//        fileStorage.setFilePath(url);
//        return fileStorage;
//    }
//}
