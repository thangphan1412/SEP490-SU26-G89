package com.fpt.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Builder
@Table(name = "contract_templates")
public class ContractTemplates extends BaseEntity{
    @Column(name = "contract_template_name", columnDefinition = "nvarchar(200)")
    private String contractTemplateName;
    @Column(name = "contract_template_description", columnDefinition = "nvarchar(1000)")
    private String contractTemplateDescription;
    @Column(name = "contract_template_status", columnDefinition = "nvarchar(30)")
    private String contractTemplateStatus;
    @Column(name = "contract_template_created_by", columnDefinition = "nvarchar(150)")
    private String contractTemplateCreatedBy;
    @Column(name = "contract_template_create_at")
    private LocalDateTime contractTemplateCreateAt;
    @Column(name = "contract_template_update_at")
    private LocalDateTime contractTemplateUpdateAt;

    /// Relation
    //contractType
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_type_id")
    private ContractTypes contractType;
    //contractAtribute
    @OneToMany(mappedBy = "contractTemplates")
    private List<ContractAttribute> contractAttributes;
    // immutable child versions
    @OneToMany(
            mappedBy = "contractTemplate",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("versionNumber DESC")
    private List<ContractTemplateVersions> contractTemplateVersions;
    // contracts created from this template
    @OneToMany(mappedBy = "contractTemplate")
    private List<Contracts> contracts;

}
