package com.fpt.backend.dto.response.companyProfile;

import com.fpt.backend.entity.Company;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyProfileResponseDTO {
    private UUID id;
    private String companyName;
    private String businessRegistrationNumber; // Lấy từ companyCode

    // Các trường hardcode vì chưa có trong DB
    private String email;
    private String taxCode;
    private String phone;
    private String registeredAddress;
    private String legalRepresentative;
    private String registrationDate;
    private String lastVerifiedDate;
    private String verifiedBy;

    public static CompanyProfileResponseDTO fromEntity(Company company) {
        return CompanyProfileResponseDTO.builder()
                .id(company.getId())
                .companyName(company.getCompanyName())
                .businessRegistrationNumber(company.getCompanyCode()) // Dùng companyCode làm số ĐKKD
                // Cắm cứng dữ liệu trả về cho các trường thiếu
                .email("legal@abcholdings.vn")
                .taxCode("0312345678")
                .phone("+84 28 3822 5678")
                .registeredAddress("125 Nguyen Hue Boulevard, Ben Nghe Ward, District 1, Ho Chi Minh City, Vietnam")
                .legalRepresentative("Nguyen Minh An")
                .registrationDate("2020-05-12")
                .lastVerifiedDate("May 10, 2025")
                .verifiedBy("Alex Morgan")
                .build();
    }
}
