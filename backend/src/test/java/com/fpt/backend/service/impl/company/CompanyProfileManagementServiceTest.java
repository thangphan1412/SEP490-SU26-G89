//package com.fpt.backend.service.impl.company;
//
//import com.fpt.backend.dto.request.companyProfile.CompanyProfileRequestDTO;
//import com.fpt.backend.dto.response.companyProfile.CompanyProfileResponseDTO;
//import com.fpt.backend.entity.Company;
//import com.fpt.backend.mail.EmailService;
//import com.fpt.backend.mail.MessageInfor;
//import com.fpt.backend.repository.company.CompanyRepository;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.InOrder;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.doThrow;
//import static org.mockito.Mockito.inOrder;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.verifyNoInteractions;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class CompanyProfileManagementServiceTest {
//
//    @Mock
//    private CompanyRepository companyRepository;
//
//    @Mock
//    private EmailService emailService;
//
//    @InjectMocks
//    private CompanyServiceImpl companyService;
//
//    @Test
//    void getCompanyProfile_mapsCompanyIdentityNameAndBusinessRegistrationNumber() {
//        UUID companyId = UUID.randomUUID();
//        Company company = company(companyId, "E-Contract", "0101234567");
//        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
//
//        CompanyProfileResponseDTO result = companyService.getCompanyProfile(companyId);
//
//        assertThat(result.getId()).isEqualTo(companyId);
//        assertThat(result.getCompanyName()).isEqualTo("E-Contract");
//        assertThat(result.getBusinessRegistrationNumber()).isEqualTo("0101234567");
//        assertThat(result.getEmail()).isEqualTo("legal@abcholdings.vn");
//    }
//
//    @Test
//    void getCompanyProfile_throwsWhenCompanyDoesNotExist() {
//        UUID missingId = UUID.randomUUID();
//        when(companyRepository.findById(missingId)).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> companyService.getCompanyProfile(missingId))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Company not found");
//    }
//
//    @Test
//    void updateCompanyProfile_updatesNameAndMapsBusinessNumberToCompanyCode() {
//        UUID companyId = UUID.randomUUID();
//        Company company = company(companyId, "Old Company", "OLD-CODE");
//        CompanyProfileRequestDTO request = companyProfileRequest(null);
//        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
//        when(companyRepository.save(company)).thenReturn(company);
//
//        CompanyProfileResponseDTO result = companyService.updateCompanyProfile(companyId, request);
//
//        assertThat(company.getCompanyName()).isEqualTo("E-Contract Vietnam");
//        assertThat(company.getCompanyCode()).isEqualTo("0312345678");
//        assertThat(result.getCompanyName()).isEqualTo("E-Contract Vietnam");
//        assertThat(result.getBusinessRegistrationNumber()).isEqualTo("0312345678");
//        verify(companyRepository).save(company);
//    }
//
//    @Test
//    void updateCompanyProfile_sendsNotificationWhenEmailIsProvided() {
//        UUID companyId = UUID.randomUUID();
//        Company company = company(companyId, "Old Company", "OLD-CODE");
//        CompanyProfileRequestDTO request = companyProfileRequest("legal@example.com");
//        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
//        when(companyRepository.save(company)).thenReturn(company);
//
//        companyService.updateCompanyProfile(companyId, request);
//
//        ArgumentCaptor<MessageInfor> messageCaptor = ArgumentCaptor.forClass(MessageInfor.class);
//        verify(emailService).sendEmail(messageCaptor.capture());
//        assertThat(messageCaptor.getValue().getEmail()).isEqualTo("legal@example.com");
//        assertThat(messageCaptor.getValue().getTitle())
//                .isEqualTo("System Alert: Company Legal Profile Updated");
//        assertThat(messageCaptor.getValue().getText())
//                .contains("E-Contract Vietnam")
//                .contains("Legal Profile");
//    }
//
//    @Test
//    void updateCompanyProfile_skipsNotificationWhenEmailIsNull() {
//        UUID companyId = UUID.randomUUID();
//        Company company = company(companyId, "Old Company", "OLD-CODE");
//        CompanyProfileRequestDTO request = companyProfileRequest(null);
//        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
//        when(companyRepository.save(company)).thenReturn(company);
//
//        companyService.updateCompanyProfile(companyId, request);
//
//        verify(emailService, never()).sendEmail(any(MessageInfor.class));
//        verify(companyRepository).save(company);
//    }
//
//    @Test
//    void updateCompanyProfile_skipsNotificationWhenEmailIsEmpty() {
//        UUID companyId = UUID.randomUUID();
//        Company company = company(companyId, "Old Company", "OLD-CODE");
//        CompanyProfileRequestDTO request = companyProfileRequest("");
//        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
//        when(companyRepository.save(company)).thenReturn(company);
//
//        companyService.updateCompanyProfile(companyId, request);
//
//        verify(emailService, never()).sendEmail(any(MessageInfor.class));
//    }
//
//    @Test
//    void updateCompanyProfile_keepsPersistedChangesWhenNotificationFails() {
//        UUID companyId = UUID.randomUUID();
//        Company company = company(companyId, "Old Company", "OLD-CODE");
//        CompanyProfileRequestDTO request = companyProfileRequest("legal@example.com");
//        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
//        when(companyRepository.save(company)).thenReturn(company);
//        doThrow(new RuntimeException("mail server unavailable"))
//                .when(emailService).sendEmail(any(MessageInfor.class));
//
//        CompanyProfileResponseDTO result = companyService.updateCompanyProfile(companyId, request);
//
//        assertThat(result.getCompanyName()).isEqualTo("E-Contract Vietnam");
//        assertThat(company.getCompanyCode()).isEqualTo("0312345678");
//        verify(companyRepository).save(company);
//    }
//
//    @Test
//    void updateCompanyProfile_throwsWhenCompanyDoesNotExistBeforePersistingOrMailing() {
//        UUID missingId = UUID.randomUUID();
//        CompanyProfileRequestDTO request = companyProfileRequest("legal@example.com");
//        when(companyRepository.findById(missingId)).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> companyService.updateCompanyProfile(missingId, request))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Company not found");
//        verify(companyRepository, never()).save(any(Company.class));
//        verifyNoInteractions(emailService);
//    }
//
//    @Test
//    void updateCompanyProfile_returnsTheEntityReturnedByRepositorySave() {
//        UUID companyId = UUID.randomUUID();
//        Company existing = company(companyId, "Old Company", "OLD-CODE");
//        Company persisted = company(companyId, "Persisted Name", "PERSISTED-CODE");
//        CompanyProfileRequestDTO request = companyProfileRequest(null);
//        when(companyRepository.findById(companyId)).thenReturn(Optional.of(existing));
//        when(companyRepository.save(existing)).thenReturn(persisted);
//
//        CompanyProfileResponseDTO result = companyService.updateCompanyProfile(companyId, request);
//
//        assertThat(result.getCompanyName()).isEqualTo("Persisted Name");
//        assertThat(result.getBusinessRegistrationNumber()).isEqualTo("PERSISTED-CODE");
//    }
//
//    @Test
//    void updateCompanyProfile_persistsBeforeSendingTheNotification() {
//        UUID companyId = UUID.randomUUID();
//        Company company = company(companyId, "Old Company", "OLD-CODE");
//        CompanyProfileRequestDTO request = companyProfileRequest("legal@example.com");
//        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
//        when(companyRepository.save(company)).thenReturn(company);
//
//        companyService.updateCompanyProfile(companyId, request);
//
//        InOrder order = inOrder(companyRepository, emailService);
//        order.verify(companyRepository).save(company);
//        order.verify(emailService).sendEmail(any(MessageInfor.class));
//    }
//
//    private static Company company(UUID id, String name, String code) {
//        Company company = new Company();
//        company.setId(id);
//        company.setCompanyName(name);
//        company.setCompanyCode(code);
//        return company;
//    }
//
//    private static CompanyProfileRequestDTO companyProfileRequest(String email) {
//        CompanyProfileRequestDTO request = new CompanyProfileRequestDTO();
//        request.setCompanyName("E-Contract Vietnam");
//        request.setBusinessRegistrationNumber("0312345678");
//        request.setEmail(email);
//        request.setTaxCode("0312345678");
//        request.setPhone("02838225678");
//        request.setRegisteredAddress("Ho Chi Minh City");
//        request.setLegalRepresentative("Nguyen Van A");
//        request.setRegistrationDate("2020-05-12");
//        return request;
//    }
//}
