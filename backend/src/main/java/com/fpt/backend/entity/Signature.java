package com.fpt.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Builder
@Table(name = "signatures")
public class Signature extends BaseEntity{
    @Column(name = "signature_type")
    private String signatureType;
    @Column(name = "signature_hash")
    private String signatureHash;
    @Column(name = "certificate_serial")
    private String certificateSerial;
    @Column(name = "status")
    private String status;

    /// Relation
    //User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;
    //contract
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contracts contract;
}
