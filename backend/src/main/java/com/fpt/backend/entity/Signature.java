package com.fpt.backend.entity;

import com.fpt.backend.enums.SignatureAlgorithm;
import com.fpt.backend.enums.SignatureHash;
import com.fpt.backend.enums.SignatureStatus;
import com.fpt.backend.enums.SignatureType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Builder
@Table(name = "signatures")
public class Signature extends BaseEntity{
    @Column(name = "signature_name")
    private String signatureName;
    @Column(name = "signature_type")
    @Enumerated(EnumType.STRING)
    private SignatureType signatureType;
    @Column(name = "document_hash")
    private String documentHash;
    @Lob
    @Column(name = "signature_value", columnDefinition = "nvarchar(max)")
    private String signatureValue;
    @Column(name = "signature_algorithm")
    @Enumerated(EnumType.STRING)
    private SignatureAlgorithm signatureAlgorithm;
    @Column(name = "signature_hash")
    @Enumerated(EnumType.STRING)
    private SignatureHash signatureHash;
    @Column(name = "certificate_serial")
    private String certificateSerial;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private SignatureStatus status;
    @Column(name = "signature_update_at")
    private LocalDate signatureUpdateAt;
    @Column(name = "signature_create_at")
    private LocalDateTime signatureCreateAt;
    /// Relation
    //User_key
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_key_id")
    private UserKeys userKey;
    //contract
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contracts contract;
    // electric
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "electronic_signature_id")
    private ElectronicSignatures electronicSignatures;
    //file sto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private FileStorage fileStorage;
}
