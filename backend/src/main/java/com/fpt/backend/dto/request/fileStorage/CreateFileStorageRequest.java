package com.fpt.backend.dto.request.fileStorage;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateFileStorageRequest {
    private String originalFileName;
    private MultipartFile multipartFile;
    private String fileName;
    private String mimeType;
    private Long fileSize;
    private LocalDateTime uploadedAt;
}
