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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
                .storageProvider("CLOUDINARY")
                .storageKey(publicId)
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .uploadAt(LocalDateTime.now())
                .user(user)
                .isDeleted(false)
                .build();

        return fileStorageRepository.save(fileStorage);
    }

    public FileStorage uploadPdfAndSave(byte[] pdf, String originalName, Users user) {
        if (pdf == null || pdf.length == 0) {
            throw new BadHttpException("Contract PDF is empty");
        }
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    pdf,
                    ObjectUtils.asMap(
                            "folder", "contracts",
                            "resource_type", "raw",
                            "public_id", UUID.randomUUID() + ".pdf"
                    )
            );
            String secureUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");
            System.out.println("=== CLOUDINARY UPLOAD ===");
            System.out.println("public_id = " + uploadResult.get("public_id"));
            System.out.println("secure_url = " + uploadResult.get("secure_url"));
            System.out.println("resource_type = " + uploadResult.get("resource_type"));
            System.out.println("type = " + uploadResult.get("type"));
            return fileStorageRepository.save(FileStorage.builder()
                    .originalName(originalName)
                    .fileName(publicId)
                    .filePath(secureUrl)
                    .storageProvider("CLOUDINARY")
                    .storageKey(publicId)
                    .mimeType("application/pdf")
                    .fileSize((long) pdf.length)
                    .uploadAt(LocalDateTime.now())
                    .user(user)
                    .isDeleted(false)
                    .build());
        } catch (IOException exception) {
            throw new BadHttpException("Failed to upload contract PDF: " + exception.getMessage());
        }
    }

    public byte[] download(FileStorage fileStorage) {
        if (fileStorage == null || fileStorage.getStorageKey() == null) {
            throw new BadHttpException("Contract PDF is unavailable");
        }

        try {
//            System.out.println("######## ENTER CLOUDINARY DOWNLOAD ########");
//            String publicId = fileStorage.getStorageKey();
//
//            System.out.println("=== CLOUDINARY DOWNLOAD ===");
//            System.out.println("Public ID: " + publicId);
//            System.out.println("Resource type: raw");
//
//            String url = cloudinary.url()
//                    .resourceType("raw")
//                    .secure(true)
//                    .generate(publicId);

            // System.out.println("Generated URL: " + url);
            String url = fileStorage.getFilePath();
            HttpRequest request = HttpRequest.newBuilder(
                    URI.create(url)
            ).GET().build();
            // System.out.println("Sending HTTP GET request to: " + fileStorage.getFilePath());
            HttpResponse<byte[]> response = HttpClient.newHttpClient().send(
                    request,
                    HttpResponse.BodyHandlers.ofByteArray()
            );

            // System.out.println("HTTP status: " + response.statusCode());
            // System.out.println("Bytes: " + response.body().length);

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BadHttpException(
                        "Unable to download contract PDF from Cloudinary. HTTP "
                                + response.statusCode()
                );
            }

            return response.body();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BadHttpException("Contract PDF download was interrupted");

        } catch (IOException e) {
            throw new BadHttpException(
                    "Unable to download contract PDF: " + e.getMessage()
            );
        }
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
