package com.fpt.backend.dto.request.companyProfile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CompanyProfileRequestDTO {

    @NotBlank(message = "Tên công ty không được để trống")
    private String companyName;

    @NotBlank(message = "Email công ty không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String registeredAddress;
}