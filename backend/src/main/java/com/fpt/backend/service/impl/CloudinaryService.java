package com.fpt.backend.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fpt.backend.entity.FileStorage;
import com.fpt.backend.entity.Users;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.repository.FileStorageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private final FileStorageRepository fileStorageRepository;

    public FileStorage uploadAndSave(MultipartFile file, Users user) {
        if (file == null || file.isEmpty()) {
            throw new BadHttpException("File is required");
        }

        Map uploadResult = uploadToCloudinary(file);

        String secureUrl = (String) uploadResult.get("secure_url");
        String publicId = (String) uploadResult.get("public_id");

        FileStorage fileStorage = FileStorage.builder()
                .originalName(file.getOriginalFilename())
                .fileName(publicId + "-" + UUID.randomUUID())
                .filePath(secureUrl)
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .uploadAt(LocalDateTime.now())
                .user(user)
                .isDeleted(false)
                .build();

        return fileStorageRepository.save(fileStorage);
    }

    private Map uploadToCloudinary(MultipartFile file) {
        try {
            return cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "signatures",
                            "resource_type", "image"
                    )
            );
        } catch (IOException e) {
            throw new BadHttpException("Failed to upload file: " + e.getMessage());
        }
    }
}