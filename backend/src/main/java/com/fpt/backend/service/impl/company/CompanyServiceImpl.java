package com.fpt.backend.service.impl.company;

import com.fpt.backend.dto.request.companyProfile.CompanyProfileRequestDTO;
import com.fpt.backend.dto.response.companyProfile.CompanyProfileResponseDTO;
import com.fpt.backend.entity.Company;
import com.fpt.backend.repository.company.CompanyRepository;
import com.fpt.backend.service.interfaces.company.ICompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompanyServiceImpl implements ICompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Override
    public CompanyProfileResponseDTO getCompanyProfile(Integer id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));
        return CompanyProfileResponseDTO.fromEntity(company);
    }

    @Override
    public CompanyProfileResponseDTO updateCompanyProfile(Integer id, CompanyProfileRequestDTO request) {
        Company existingCompany = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));

        // Chỉ cập nhật những trường thực sự có trong Database
        existingCompany.setCompanyName(request.getCompanyName());
        existingCompany.setCompanyCode(request.getBusinessRegistrationNumber());

        Company updatedCompany = companyRepository.save(existingCompany);
        return CompanyProfileResponseDTO.fromEntity(updatedCompany);
    }
}