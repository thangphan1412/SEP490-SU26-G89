package com.fpt.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Builder
@Table(name = "file_storage")
public class FileStorage extends BaseEntity{
    @Column(name = "original_name")
    private String originalName;
    @Column(name = "stored_name")
    private String storedName;
    @Column(name = "mime_type")
    private String mimeType;
    @Column(name = "extension")
    private String extension;
    @Column(name = "storage_provider")
    private String storageProvider;
    @Column(name = "storage_key")
    private String storageKey;
    @Column(name = "url")
    private String url;
    @Column(name = "upload_by")
    private String uploadBy;
    @Column(name = "upload_at")
    private LocalDateTime uploadAt;
    @Column(name = "is_deleted")
    private Boolean isDeleted;

    /// Relation
    //user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;
}
