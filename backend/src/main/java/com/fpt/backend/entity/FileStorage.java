package com.fpt.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Builder
@Table(name = "file_storage")
public class FileStorage extends BaseEntity{
    @Column(name = "original_name", nullable = false, columnDefinition = "nvarchar(max)")
    private String originalName;

    @Column(name = "file_name", nullable = false, unique = true, columnDefinition = "nvarchar(255)")
    private String fileName;

    @Column(name = "file_path", nullable = false, columnDefinition = "nvarchar(max)")
    private String filePath;

    @Column(name = "storage_provider", columnDefinition = "nvarchar(max)")
    private String storageProvider;

    @Column(name = "storage_key", columnDefinition = "nvarchar(max)")
    private String storageKey;

    @Column(name = "mime_type", nullable = false, columnDefinition = "nvarchar(max)")
    private String mimeType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "upload_at", nullable = false)
    private LocalDateTime uploadAt;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    /// Relation
    //user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;
    //electronic
    @OneToMany(mappedBy = "fileStorage")
    private List<ElectronicSignatures> electronicSignatures;
    //signature
    @OneToMany(mappedBy = "fileStorage")
    private List<Signature> signatures;

    @OneToMany(mappedBy = "documentFile")
    private List<Contracts> contracts;
}
