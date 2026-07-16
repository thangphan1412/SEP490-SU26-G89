package com.fpt.backend.service.interfaces.company;

import com.fpt.backend.dto.request.companyProfile.CompanyProfileRequestDTO;
import com.fpt.backend.dto.response.companyProfile.CompanyProfileResponseDTO;

public interface ICompanyService {
    CompanyProfileResponseDTO getCompanyProfile(Integer id);
    CompanyProfileResponseDTO updateCompanyProfile(Integer id, CompanyProfileRequestDTO request);
}