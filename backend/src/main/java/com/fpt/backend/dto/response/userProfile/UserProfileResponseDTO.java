package com.fpt.backend.dto.response.userProfile;

import com.fpt.backend.entity.Users;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileResponseDTO {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String numberPhone;
    private String role;
    private String status;

    public static UserProfileResponseDTO fromEntity(Users user) {
        return UserProfileResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .numberPhone(user.getNumberPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }
}
