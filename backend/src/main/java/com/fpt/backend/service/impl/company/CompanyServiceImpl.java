package com.fpt.backend.service.impl.company;

import com.fpt.backend.dto.request.companyProfile.CompanyProfileRequestDTO;
import com.fpt.backend.dto.response.companyProfile.CompanyProfileResponseDTO;
import com.fpt.backend.entity.Company;
import com.fpt.backend.entity.Users;
import com.fpt.backend.repository.company.CompanyRepository;
import com.fpt.backend.service.interfaces.company.ICompanyService;
import com.fpt.backend.mail.EmailService;
import com.fpt.backend.mail.MessageInfor;
import com.fpt.backend.util.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CompanyServiceImpl implements ICompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private EmailService emailService;

    // THÊM: Tiêm CurrentUser vào để biết ai đang gọi API
    @Autowired
    private CurrentUser currentUser;

    @Override
    public CompanyProfileResponseDTO getCompanyProfile(UUID id) {
        // 1. Lấy thông tin user đang đăng nhập từ Token
        Users loggedInUser = currentUser.getCurrentUser();

        // 2. Lấy đúng hồ sơ công ty được móc nối với User đó (Đối tác ra đối tác, nội bộ ra nội bộ)
        Company company = loggedInUser.getCompany();

        // 3. Fallback (Phòng hờ): Nếu user nội bộ cũ chưa kịp có liên kết, thì lấy mặc định công ty nội bộ
        if (company == null) {
            company = companyRepository.findByIsInternalTrue()
                    .orElseThrow(() -> new RuntimeException("Company profile not found. Please initialize it."));
        }

        return CompanyProfileResponseDTO.fromEntity(company);
    }

    @Override
    public CompanyProfileResponseDTO updateCompanyProfile(UUID id, CompanyProfileRequestDTO request) {
        // Màn Update Company Profile chỉ dành cho Accountant (Nội bộ), nên luôn luôn lấy công ty nội bộ để sửa
        Company existingCompany = companyRepository.findByIsInternalTrue()
                .orElse(new Company());

        existingCompany.setIsInternal(true);
        existingCompany.setCompanyName(request.getCompanyName());
        existingCompany.setEmail(request.getEmail());
        existingCompany.setRegisteredAddress(request.getRegisteredAddress());

        Company updatedCompany = companyRepository.save(existingCompany);

        // Gửi Mail
        try {
            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                MessageInfor messageInfor = new MessageInfor();
                messageInfor.setEmail(request.getEmail());
                messageInfor.setTitle("System Alert: Company Legal Profile Updated");
                messageInfor.setText("The Legal Profile for company '" + request.getCompanyName() + "' has been successfully updated.");
                emailService.sendEmail(messageInfor);
            }
        } catch (Exception e) {
            System.err.println("Mail Error: " + e.getMessage());
        }

        return CompanyProfileResponseDTO.fromEntity(updatedCompany);
    }
}