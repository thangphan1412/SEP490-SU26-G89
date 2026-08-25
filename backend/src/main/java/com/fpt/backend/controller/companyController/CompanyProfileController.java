package com.fpt.backend.controller.companyController;

import com.fpt.backend.constant.ApiConstant;
import com.fpt.backend.dto.request.companyProfile.CompanyProfileRequestDTO;
import com.fpt.backend.dto.response.companyProfile.CompanyProfileResponseDTO;
import com.fpt.backend.service.interfaces.company.ICompanyService;
import com.fpt.backend.util.BaseResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstant.Company.COMPANY_PROFILE)
public class CompanyProfileController {

    @Autowired
    private ICompanyService companyService;

    // Bất kỳ ai cũng có thể XEM Company Profile nội bộ
    @GetMapping
    public ResponseEntity<BaseResponse<CompanyProfileResponseDTO>> getCompanyProfile() {
        try {
            CompanyProfileResponseDTO profile = companyService.getCompanyProfile(null);
            return ResponseEntity.ok(new BaseResponse<>(HttpStatus.OK.value(), "Success", profile));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new BaseResponse<>(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
        }
    }

    // CHỈ CÓ ACCOUNTANT MỚI CÓ QUYỀN SỬA Company Profile nội bộ
    @PreAuthorize("hasAuthority('Accountant')")
    @PutMapping
    public ResponseEntity<BaseResponse<CompanyProfileResponseDTO>> updateCompanyProfile(@Valid @RequestBody CompanyProfileRequestDTO request) {
        try {
            CompanyProfileResponseDTO updatedProfile = companyService.updateCompanyProfile(null, request);
            return ResponseEntity.ok(new BaseResponse<>(HttpStatus.OK.value(), "Success", updatedProfile));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new BaseResponse<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        }
    }
}