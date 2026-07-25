package com.fpt.backend.service.impl.company;

import com.fpt.backend.dto.request.companyProfile.CompanyProfileRequestDTO;
import com.fpt.backend.dto.response.companyProfile.CompanyProfileResponseDTO;
import com.fpt.backend.entity.Company;
import com.fpt.backend.repository.company.CompanyRepository;
import com.fpt.backend.service.interfaces.company.ICompanyService;
// Nhớ import 2 thư viện mail của bạn vào nhé
import com.fpt.backend.mail.EmailService;
import com.fpt.backend.mail.MessageInfor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompanyServiceImpl implements ICompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    // THÊM: Inject EmailService vào đây
    @Autowired
    private EmailService emailService;

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

        // --- BẮT ĐẦU ĐOẠN CODE GỬI EMAIL TỰ ĐỘNG ---
        try {
            // Kiểm tra xem frontend có truyền email xuống không
            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                MessageInfor messageInfor = new MessageInfor();
                messageInfor.setEmail(request.getEmail()); // Lấy email từ request để gửi
                messageInfor.setTitle("System Alert: Company Legal Profile Updated");

                String emailBody = "Hello,\n\n" +
                        "This is an automated notification to inform you that the Legal Profile for company '" + request.getCompanyName() + "' has been successfully updated in the E-CONTRACT system.\n\n" +
                        "The updated information includes Company Name, Tax Code, Registered Address, and other legal identifiers used in contract templates.\n\n" +
                        "If your authorized administrator did not make these changes, please verify the system audit logs or contact technical support immediately.\n\n" +
                        "Best regards,\nE-CONTRACT Security System";

                messageInfor.setText(emailBody);
                emailService.sendEmail(messageInfor);
            }
        } catch (Exception e) {
            // Bọc try-catch để lỡ cấu hình mail lỗi thì hệ thống vẫn lưu thông tin công ty bình thường
            System.err.println("Lỗi khi gửi email thông báo update company profile: " + e.getMessage());
        }
        // --- KẾT THÚC ĐOẠN CODE GỬI EMAIL ---

        return CompanyProfileResponseDTO.fromEntity(updatedCompany);
    }
}