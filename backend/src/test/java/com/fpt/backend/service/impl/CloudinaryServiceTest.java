package com.fpt.backend.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.fpt.backend.entity.FileStorage;
import com.fpt.backend.entity.Users;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.repository.FileStorageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {
    @Mock
    private Cloudinary cloudinary;
    @Mock
    private Uploader uploader;
    @Mock
    private FileStorageRepository fileStorageRepository;
    @InjectMocks
    private CloudinaryService service;

    @Test
    void uploadsApprovedPdfAsImmutableRawAssetAndStoresMetadata()
            throws IOException {
        byte[] pdf = "%PDF-1.7\napproved contract".getBytes();
        Users user = new Users();
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(Map.of(
                "secure_url", "https://res.cloudinary.com/demo/raw/upload/contracts/approved/contract.pdf",
                "public_id", "contracts/approved/contract.pdf"
        ));
        when(fileStorageRepository.save(any(FileStorage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FileStorage stored = service.uploadPdfAndSave(
                pdf,
                "CON-2026-001.pdf",
                user
        );

        ArgumentCaptor<Map> options = ArgumentCaptor.forClass(Map.class);
        verify(uploader).upload(any(byte[].class), options.capture());
        assertThat(options.getValue())
                .containsEntry("folder", "contracts/approved")
                .containsEntry("resource_type", "raw")
                .containsEntry("overwrite", false);
        assertThat(stored.getMimeType()).isEqualTo("application/pdf");
        assertThat(stored.getFileSize()).isEqualTo((long) pdf.length);
        assertThat(stored.getFilePath()).endsWith("contract.pdf");
        assertThat(stored.getUser()).isSameAs(user);
    }

    @Test
    void rejectsNonPdfBeforeCallingCloudinary() {
        assertThatThrownBy(() -> service.uploadPdfAndSave(
                "not a pdf".getBytes(),
                "contract.pdf",
                new Users()
        )).isInstanceOf(BadHttpException.class)
                .hasMessageContaining("valid PDF");

        verify(cloudinary, never()).uploader();
        verify(fileStorageRepository, never()).save(any());
    }
}
