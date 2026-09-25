//package com.fpt.backend.controller.signatureController;
//
//import com.fpt.backend.dto.request.electronicSignature.CreateElectronicSignatureRequest;
//import com.fpt.backend.dto.request.electronicSignature.UpdateElectronicSignatureRequest;
//import com.fpt.backend.dto.request.fileStorage.CreateFileStorageRequest;
//import com.fpt.backend.dto.response.electronicSignature.ElectronicSignatureDetailResponse;
//import com.fpt.backend.dto.response.electronicSignature.ListElectronicResponse;
//import com.fpt.backend.enums.ElectronicSignatureType;
//import com.fpt.backend.enums.ElectronicStatus;
//import com.fpt.backend.service.impl.electronicSignature.ElectronicSignatureServiceImpl;
//import com.fpt.backend.util.BaseResponse;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class ElectronicSignatureControllerTest {
//
//    @Mock
//    private ElectronicSignatureServiceImpl electronicSignatureService;
//
//    @InjectMocks
//    private ElectronicSignatureController electronicSignatureController;
//
//    @Test
//    void createElectronic_attachesMultipartFileAndReturnsCreated() {
//        CreateElectronicSignatureRequest request = createRequest();
//        MultipartFile multipartFile = org.mockito.Mockito.mock(MultipartFile.class);
//
//        ResponseEntity<BaseResponse<?>> response =
//                electronicSignatureController.createElectronic(request, multipartFile);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
//        ArgumentCaptor<CreateElectronicSignatureRequest> requestCaptor =
//                ArgumentCaptor.forClass(CreateElectronicSignatureRequest.class);
//        verify(electronicSignatureService).createElectronicSignature(requestCaptor.capture());
//        assertThat(requestCaptor.getValue()).isSameAs(request);
//        assertThat(requestCaptor.getValue().getCreateFileStorageRequests()).isNotNull();
//        assertThat(requestCaptor.getValue().getCreateFileStorageRequests().getMultipartFile()).isSameAs(multipartFile);
//    }
//
//    @Test
//    void createElectronic_propagatesServiceFailure() {
//        CreateElectronicSignatureRequest request = createRequest();
//        MultipartFile multipartFile = org.mockito.Mockito.mock(MultipartFile.class);
//        RuntimeException serviceFailure = new RuntimeException("File is required");
//        when(electronicSignatureService.createElectronicSignature(request)).thenThrow(serviceFailure);
//
//        assertThatThrownBy(() -> electronicSignatureController.createElectronic(request, multipartFile))
//                .isSameAs(serviceFailure);
//    }
//
//    @Test
//    void getAll_returnsOkAndListFromService() {
//        List<ListElectronicResponse> signatures = List.of(new ListElectronicResponse(
//                UUID.randomUUID(),
//                "My signature",
//                ElectronicSignatureType.UPLOADED,
//                ElectronicStatus.ACTIVE,
//                true,
//                LocalDate.of(2026, 9, 18),
//                "https://cdn.example.com/signature.png"
//        ));
//        when(electronicSignatureService.getAllElectronicSignatures()).thenReturn(signatures);
//
//        ResponseEntity<BaseResponse<List<ListElectronicResponse>>> response = electronicSignatureController.getAll();
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(response.getBody()).satisfies(body -> {
//            assertThat(body.getStatus()).isEqualTo(HttpStatus.OK.value());
//            assertThat(body.getData()).isSameAs(signatures);
//        });
//        verify(electronicSignatureService).getAllElectronicSignatures();
//    }
//
//    @Test
//    void getAll_returnsOkWithEmptyListWhenUserHasNoSignatures() {
//        when(electronicSignatureService.getAllElectronicSignatures()).thenReturn(List.of());
//
//        ResponseEntity<BaseResponse<List<ListElectronicResponse>>> response = electronicSignatureController.getAll();
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(response.getBody().getData()).isEmpty();
//        verify(electronicSignatureService).getAllElectronicSignatures();
//    }
//
//    @Test
//    void getElectronicById_returnsOkAndDetailFromService() {
//        UUID signatureId = UUID.randomUUID();
//        ElectronicSignatureDetailResponse detail = new ElectronicSignatureDetailResponse(
//                "My signature",
//                ElectronicSignatureType.DRAW,
//                ElectronicStatus.ACTIVE,
//                false,
//                LocalDate.of(2026, 9, 18),
//                "https://cdn.example.com/signature.png"
//        );
//        when(electronicSignatureService.getElectronicSignatureDetail(signatureId)).thenReturn(detail);
//
//        ResponseEntity<BaseResponse<ElectronicSignatureDetailResponse>> response =
//                electronicSignatureController.getElectronicById(signatureId);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(response.getBody()).satisfies(body -> {
//            assertThat(body.getStatus()).isEqualTo(HttpStatus.OK.value());
//            assertThat(body.getData()).isSameAs(detail);
//        });
//        verify(electronicSignatureService).getElectronicSignatureDetail(signatureId);
//    }
//
//    @Test
//    void updateElectronicSignature_delegatesOptionalFileAndReturnsSuccessPayload() {
//        UUID signatureId = UUID.randomUUID();
//        UpdateElectronicSignatureRequest request = new UpdateElectronicSignatureRequest();
//        request.setElectronicSignatureName("Updated signature");
//        request.setElectronicSignatureType(ElectronicSignatureType.UPLOADED);
//        request.setElectronicStatus(ElectronicStatus.ACTIVE);
//        request.setDefault(true);
//
//        ResponseEntity<BaseResponse<?>> response =
//                electronicSignatureController.updateElectronicSignature(signatureId, request, null);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(response.getBody()).satisfies(body -> {
//            assertThat(body.getStatus()).isEqualTo(HttpStatus.OK.value());
//            assertThat(body.getMessage()).isEqualTo(HttpStatus.OK.getReasonPhrase());
//            assertThat(body.getData()).isEqualTo("Signature updated successfully");
//        });
//        verify(electronicSignatureService).updateElectronicSignature(signatureId, request, null);
//    }
//
//    @Test
//    void updateElectronicSignature_propagatesServiceFailure() {
//        UUID signatureId = UUID.randomUUID();
//        UpdateElectronicSignatureRequest request = new UpdateElectronicSignatureRequest();
//        RuntimeException serviceFailure = new RuntimeException("Electronic signature not found");
//        when(electronicSignatureService.updateElectronicSignature(signatureId, request, null))
//                .thenThrow(serviceFailure);
//
//        assertThatThrownBy(() -> electronicSignatureController.updateElectronicSignature(signatureId, request, null))
//                .isSameAs(serviceFailure);
//    }
//
//    private static CreateElectronicSignatureRequest createRequest() {
//        CreateElectronicSignatureRequest request = new CreateElectronicSignatureRequest();
//        request.setElectronicSignatureName("My signature");
//        request.setElectronicSignatureType(ElectronicSignatureType.UPLOADED);
//        request.setElectronicStatus(ElectronicStatus.ACTIVE);
//        request.setDefault(true);
//        request.setCreateFileStorageRequests(new CreateFileStorageRequest());
//        return request;
//    }
//}
