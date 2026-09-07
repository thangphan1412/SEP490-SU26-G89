package com.fpt.backend.dto.response.fileStorage;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileStorageResponse {
    private String signatureUrl;
}
