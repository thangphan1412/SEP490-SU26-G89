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
@Table(
        name = "signatures",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_contract_signature_workflow_step",
                columnNames = {"contract_id", "workflow_step_instance_id"}
        )
)
public class Signature extends BaseEntity{
    @Column(name = "signature_name")
    private String signatureName;
    @Column(name = "signature_type")
    @Enumerated(EnumType.STRING)
    private SignatureType signatureType;
    @Column(name = "document_hash")
    private String documentHash;
<<<<<<< HEAD
    @Column(name = "signing_public_key", columnDefinition = "nvarchar(max)")
    private String signingPublicKey;
    @Column(name = "digital_signature", columnDefinition = "nvarchar(max)")
    private String digitalSignature;
    @Column(name = "public_key_fingerprint", length = 64)
    private String publicKeyFingerprint;
=======
    @Lob
    @Column(name = "signature_value", columnDefinition = "nvarchar(max)")
    private String signatureValue;
>>>>>>> 7d6eb51fe9c660b46d1a1bc0200bcbbc73cf5f51
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
    @Column(name = "signed_at")
    private LocalDateTime signedAt;
    @Column(name = "signer_role", length = 60)
    private String signerRole;
    /// Relation
    //User_key
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_key_id")
    private UserKeys userKey;
    //contract
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contracts contract;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signed_by_user_id")
    private Users signedBy;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_step_instance_id")
    private ContractWorkflowStepInstance workflowStepInstance;
    // electric
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "electronic_signature_id")
    private ElectronicSignatures electronicSignatures;
    //file sto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private FileStorage fileStorage;
}
