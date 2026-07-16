package com.fpt.backend.dto.request.companyProfile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CompanyProfileRequestDTO {
    private String companyName;
    private String email;
    private String taxCode;
    private String phone;
    private String registeredAddress;
    private String businessRegistrationNumber; // Sẽ map vào companyCode
    private String legalRepresentative;
    private String registrationDate;
}
