//package com.fpt.backend.controller.companyController;
//
//import com.fpt.backend.dto.request.companyProfile.CompanyProfileRequestDTO;
//import com.fpt.backend.dto.response.companyProfile.CompanyProfileResponseDTO;
//import com.fpt.backend.service.interfaces.company.ICompanyService;
//import com.fpt.backend.util.BaseResponse;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class CompanyProfileControllerTest {
//
//    @Mock
//    private ICompanyService companyService;
//
//    @InjectMocks
//    private CompanyProfileController companyProfileController;
//
//    @Test
//    void getCompanyProfile_returnsCompanyProfile() {
//        CompanyProfileResponseDTO profile = companyProfile();
//        when(companyService.getCompanyProfile(null)).thenReturn(profile);
//
//        ResponseEntity<BaseResponse<CompanyProfileResponseDTO>> response =
//                companyProfileController.getCompanyProfile();
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(response.getBody()).satisfies(body -> {
//            assertThat(body.getStatus()).isEqualTo(200);
//            assertThat(body.getMessage()).isEqualTo("Company profile fetched successfully");
//            assertThat(body.getData()).isSameAs(profile);
//        });
//        verify(companyService).getCompanyProfile(null);
//    }
//
//    @Test
//    void getCompanyProfile_returnsNotFoundWhenServiceFails() {
//        when(companyService.getCompanyProfile(null))
//                .thenThrow(new RuntimeException("Company not found with id: null"));
//
//        ResponseEntity<BaseResponse<CompanyProfileResponseDTO>> response =
//                companyProfileController.getCompanyProfile();
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
//        assertThat(response.getBody()).satisfies(body -> {
//            assertThat(body.getStatus()).isEqualTo(404);
//            assertThat(body.getMessage()).contains("Company not found");
//            assertThat(body.getData()).isNull();
//        });
//    }
//
//    @Test
//    void updateCompanyProfile_returnsUpdatedCompanyProfile() {
//        CompanyProfileRequestDTO request = companyProfileRequest();
//        CompanyProfileResponseDTO updated = companyProfile();
//        updated.setCompanyName("E-Contract Vietnam");
//        when(companyService.updateCompanyProfile(null, request)).thenReturn(updated);
//
//        ResponseEntity<BaseResponse<CompanyProfileResponseDTO>> response =
//                companyProfileController.updateCompanyProfile(request);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(response.getBody()).satisfies(body -> {
//            assertThat(body.getStatus()).isEqualTo(200);
//            assertThat(body.getMessage()).isEqualTo("Company profile updated successfully");
//            assertThat(body.getData()).isSameAs(updated);
//        });
//        verify(companyService).updateCompanyProfile(null, request);
//    }
//
//    @Test
//    void updateCompanyProfile_returnsBadRequestWhenServiceFails() {
//        CompanyProfileRequestDTO request = companyProfileRequest();
//        when(companyService.updateCompanyProfile(null, request))
//                .thenThrow(new RuntimeException("Company profile validation failed"));
//
//        ResponseEntity<BaseResponse<CompanyProfileResponseDTO>> response =
//                companyProfileController.updateCompanyProfile(request);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
//        assertThat(response.getBody()).satisfies(body -> {
//            assertThat(body.getStatus()).isEqualTo(400);
//            assertThat(body.getMessage()).isEqualTo("Company profile validation failed");
//            assertThat(body.getData()).isNull();
//        });
//    }
//
//    private static CompanyProfileResponseDTO companyProfile() {
//        return CompanyProfileResponseDTO.builder()
//                .id(UUID.randomUUID())
//                .companyName("E-Contract")
//                .businessRegistrationNumber("0101234567")
//                .email("legal@example.com")
//                .build();
//    }
//
//    private static CompanyProfileRequestDTO companyProfileRequest() {
//        CompanyProfileRequestDTO request = new CompanyProfileRequestDTO();
//        request.setCompanyName("E-Contract Vietnam");
//        request.setBusinessRegistrationNumber("0312345678");
//        request.setEmail("legal@example.com");
//        request.setTaxCode("0312345678");
//        request.setPhone("02838225678");
//        request.setRegisteredAddress("Ho Chi Minh City");
//        return request;
//    }
//}
