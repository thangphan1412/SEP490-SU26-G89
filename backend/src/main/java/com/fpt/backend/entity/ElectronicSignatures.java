package com.fpt.backend.entity;

import com.fpt.backend.enums.ElectronicSignatureType;
import com.fpt.backend.enums.ElectronicStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "electronic_signature")
public class ElectronicSignatures extends BaseEntity{
//    user_id              FK -> users
//    file_storage_id      FK -> file_storage
    @Column(name = "electronic_signature_type")
    @Enumerated(EnumType.STRING)
    private ElectronicSignatureType electronicSignatureType;
    @Column(name = "is_default")
    private boolean is_default;
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
}
