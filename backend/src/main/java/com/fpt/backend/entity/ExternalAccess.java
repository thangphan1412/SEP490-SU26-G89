package com.fpt.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "external_access")
public class ExternalAccess extends BaseEntity{
    @Column(name = " external_email")
    private String externalEmail;
    @Column(name = "external_name")
    private String externalName;
    @Column(name = "token_hash")
    private String tokenHash;
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;
    @Column(name = "used_at")
    private LocalDateTime usedAt;
    @Column(name = "access_status")
    private String accessStatus;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    ///
    //- created_by

    //- contract_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contracts contract;
}
