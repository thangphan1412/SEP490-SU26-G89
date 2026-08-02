package com.fpt.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Builder
@Table(name = "contracts")
public class Contracts extends BaseEntity {
    @Column(name = "contract_number")
    private String contractNumber;
    @Column(name = "contract_title")
    private String contractTitle;
    @Column(name = "contract_status")
    private String contractStatus;
    @Column(name = "contract_effective_date")
    private LocalDate effectiveDate;
    @Column(name = "contract_expiration_date")
    private LocalDate expirationDate;
    @Column(name = "contract_created_by")
    private String contractCreateBy;
    @Column(name = "contract_created_at")
    private LocalDateTime contractCreatedAt;
    @Column(name = "contract_status_updated_at")
    private LocalDateTime contractStatusUpdatedAt;
    @Column(name = "contract_ended_at")
    private LocalDateTime contractEndedAt;
    @Column(name = "contract_cancellation_reason", columnDefinition = "nvarchar(1000)")
    private String contractCancellationReason;
    @Column(name = "contract_content", columnDefinition = "nvarchar(max)")
    private String contractContent;
    @Column(name = "contract_layout_json", columnDefinition = "nvarchar(max)")
    private String contractLayoutJson;

    /// Relation
    // project
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Projects project;
    //Signature
    @OneToMany(mappedBy = "contract")
    private List<Signature>  signatures;
    // contract type
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_type_id")
    private ContractTypes contractType;
    // contract template
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_template_id")
    private ContractTemplates contractTemplate;
    // immutable template version selected for this contract
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_template_version_id")
    private ContractTemplateVersions contractTemplateVersion;
    // cancelled contract that this new contract replaces
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_contract_id")
    private Contracts previousContract;
    @OneToMany(mappedBy = "previousContract")
    private List<Contracts> replacementContracts;
    // immutable audit trail for lifecycle transitions
    @OneToMany(
            mappedBy = "contract",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("changedAt DESC")
    private List<ContractStatusHistory> statusHistory;
    // contrac approvals
    @OneToMany(mappedBy = "contract")
    private List<ContractApprovals> contractApprovals;
    //contract attribute value
    @OneToMany(mappedBy = "contract")
    private List<ContractAttributeValues> contractAttributeValues;
    // external
    @OneToMany(mappedBy = "contract")
    private List<ExternalAccess> externalAccess;
}
