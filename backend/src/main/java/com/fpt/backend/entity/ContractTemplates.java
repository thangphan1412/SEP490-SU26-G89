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
    @Column(name = "contract_template_name")
    private String contractTemplateName;
    @Column(name = "contract_template_version")
    private Integer contractTemplateVersion;
    @Column(name = "contract_template_description")
    private String contractTemplateDescription;
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
    // contractposition
    @OneToMany(mappedBy = "contractTemplates")
    private List<ContractPositions> contractPositions;

}
