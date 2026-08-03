package com.fpt.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Entity
@Table(name = "contract_status_history")
public class ContractStatusHistory extends BaseEntity {
    @Column(name = "from_status", length = 60)
    private String fromStatus;

    @Column(name = "to_status", nullable = false, length = 60)
    private String toStatus;

    @Column(name = "transition_action", nullable = false, length = 60)
    private String action;

    @Column(name = "actor_name", columnDefinition = "nvarchar(150)")
    private String actorName;

    @Column(name = "actor_role", length = 60)
    private String actorRole;

    @Column(name = "transition_comment", columnDefinition = "nvarchar(1000)")
    private String comment;

    @Column(name = "signer_age_verified")
    private Boolean signerAgeVerified;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contracts contract;
}
