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

    @NotBlank(message = "Company name is required.")
    private String companyName;

    @NotBlank(message = "Company email is required.")
    @Email(message = "Enter a valid email address.")
    private String email;

    @NotBlank(message = "Address is required.")
    private String registeredAddress;
}