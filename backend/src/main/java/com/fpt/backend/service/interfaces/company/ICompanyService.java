package com.fpt.backend.service.interfaces.company;

import com.fpt.backend.dto.request.companyProfile.CompanyProfileRequestDTO;
import com.fpt.backend.dto.response.companyProfile.CompanyProfileResponseDTO;

import java.util.UUID;

public interface ICompanyService {
    CompanyProfileResponseDTO getCompanyProfile(UUID id);
    CompanyProfileResponseDTO updateCompanyProfile(UUID id, CompanyProfileRequestDTO request);
}