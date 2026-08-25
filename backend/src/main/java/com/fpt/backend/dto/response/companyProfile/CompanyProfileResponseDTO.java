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
    private String email;
    private String registeredAddress;

    public static CompanyProfileResponseDTO fromEntity(Company company) {
        if (company == null) return null;
        return CompanyProfileResponseDTO.builder()
                .id(company.getId())
                .companyName(company.getCompanyName())
                .email(company.getEmail())
                .registeredAddress(company.getRegisteredAddress())
                .build();
    }
}