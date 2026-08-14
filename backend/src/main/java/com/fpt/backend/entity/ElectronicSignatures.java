package com.fpt.backend.entity;

import com.fpt.backend.enums.ElectronicSignatureType;
import com.fpt.backend.enums.ElectronicStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "electronic_signature")
public class ElectronicSignatures extends BaseEntity {
   @Column(name = "electronic_signature_name")
   private String electronicSignatureName;
    @Column(name = "electronic_signature_type")
    @Enumerated(EnumType.STRING)
    private ElectronicSignatureType electronicSignatureType;
    @Column(name = "is_default")
    private boolean isDefault;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ElectronicStatus status;
    @Column(name = "create_at")
    private LocalDate createdAt;
    @Column(name = "update_at")
    private LocalDate updatedAt;
    //file sto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private FileStorage fileStorage;
    // signature
    @OneToMany(mappedBy = "electronicSignatures")
    private List<Signature> signatures;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_key_id",
            foreignKey = @ForeignKey(name = "FK_electronic_signature_user_key")
    )
    private UserKeys userKey;
}
