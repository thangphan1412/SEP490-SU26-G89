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
    @Column(name = "fileStorage_original_name")
    private String originalName;
    @Column(name = "fileStorage_stored_name")
    private String storedName;
    @Column(name = "fileStorage_mime_type")
    private String mimeType;
    @Column(name = "fileStorage_extension")
    private String extension;
    @Column(name = "fileStorage_storage_provider")
    private String storageProvider;
    @Column(name = "fileStorage_storage_key")
    private String storageKey;
    @Column(name = "fileStorage_url")
    private String url;
    @Column(name = "fileStorage_upload_by")
    private String uploadBy;
    @Column(name = "fileStorage_upload_at")
    private LocalDateTime uploadAt;
    @Column(name = "fileStorage_is_deleted")
    private Boolean isDeleted;

    /// Relation
    //user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;
}
