package com.fpt.backend.controller.companyController;

import com.fpt.backend.dto.request.companyProfile.CompanyProfileRequestDTO;
import com.fpt.backend.dto.response.companyProfile.CompanyProfileResponseDTO;
import com.fpt.backend.service.interfaces.company.ICompanyService;
import com.fpt.backend.util.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/company-profile")
public class CompanyProfileController {

    @Autowired
    private ICompanyService companyService;

    // Giả định hệ thống chỉ có 1 công ty quản lý hợp đồng chính, ID mặc định là 1 trong DB
    private static final UUID DEFAULT_COMPANY_ID = null;

    // 1. Xem hồ sơ công ty
    @GetMapping
    public ResponseEntity<BaseResponse<CompanyProfileResponseDTO>> getCompanyProfile() {
        try {
            CompanyProfileResponseDTO profile = companyService.getCompanyProfile(DEFAULT_COMPANY_ID);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(BaseResponse.<CompanyProfileResponseDTO>builder()
                            .status(HttpStatus.OK.value())
                            .message("Company profile fetched successfully")
                            .data(profile)
                            .build()
                    );
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.<CompanyProfileResponseDTO>builder()
                            .status(HttpStatus.NOT_FOUND.value())
                            .message(e.getMessage())
                            .build()
                    );
        }
    }

    // 2. Cập nhật hồ sơ công ty
    @PutMapping
    public ResponseEntity<BaseResponse<CompanyProfileResponseDTO>> updateCompanyProfile(@RequestBody CompanyProfileRequestDTO request) {
        try {
            CompanyProfileResponseDTO updatedProfile = companyService.updateCompanyProfile(DEFAULT_COMPANY_ID, request);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(BaseResponse.<CompanyProfileResponseDTO>builder()
                            .status(HttpStatus.OK.value())
                            .message("Company profile updated successfully")
                            .data(updatedProfile)
                            .build()
                    );
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(BaseResponse.<CompanyProfileResponseDTO>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .build()
                    );
        }
    }
}