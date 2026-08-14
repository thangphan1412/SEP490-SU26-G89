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
import java.util.Locale;
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

        Map<?, ?> uploadResult = uploadToCloudinary(file);

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

    public FileStorage uploadPdfAndSave(
            byte[] pdfContent,
            String originalName,
            Users user
    ) {
        requirePdf(pdfContent);
        String normalizedName = normalizePdfName(originalName);
        String publicId = normalizedName.substring(
                0,
                normalizedName.length() - 4
        ) + "-" + UUID.randomUUID() + ".pdf";

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    pdfContent,
                    ObjectUtils.asMap(
                            "folder", "contracts/approved",
                            "public_id", publicId,
                            "resource_type", "raw",
                            "overwrite", false
                    )
            );
            String secureUrl = requireUploadValue(
                    uploadResult,
                    "secure_url"
            );
            String cloudinaryPublicId = requireUploadValue(
                    uploadResult,
                    "public_id"
            );

            return fileStorageRepository.save(FileStorage.builder()
                    .originalName(normalizedName)
                    .fileName(cloudinaryPublicId)
                    .filePath(secureUrl)
                    .mimeType("application/pdf")
                    .fileSize((long) pdfContent.length)
                    .uploadAt(LocalDateTime.now())
                    .user(user)
                    .isDeleted(false)
                    .build());
        } catch (IOException exception) {
            throw new BadHttpException(
                    "Failed to upload the approved contract PDF"
            );
        }
    }

    private Map<?, ?> uploadToCloudinary(MultipartFile file) {
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

    private void requirePdf(byte[] content) {
        if (content == null
                || content.length < 5
                || content[0] != '%'
                || content[1] != 'P'
                || content[2] != 'D'
                || content[3] != 'F'
                || content[4] != '-') {
            throw new BadHttpException("A valid PDF file is required");
        }
    }

    private String normalizePdfName(String originalName) {
        String value = originalName == null ? "contract.pdf" : originalName;
        value = value.trim().replaceAll("[^a-zA-Z0-9._-]", "-");
        value = value.replaceAll("-+", "-");
        if (value.isBlank() || value.equals(".pdf")) {
            value = "contract.pdf";
        }
        if (!value.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            value += ".pdf";
        }
        return value;
    }

    private String requireUploadValue(Map<?, ?> uploadResult, String key) {
        Object value = uploadResult.get(key);
        if (!(value instanceof String text) || text.isBlank()) {
            throw new BadHttpException(
                    "Cloudinary did not return " + key + " for the uploaded file"
            );
        }
        return text;
    }
}
