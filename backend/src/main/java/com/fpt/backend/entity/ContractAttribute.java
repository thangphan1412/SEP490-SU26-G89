package com.fpt.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "contract_attribute")
public class ContractAttribute extends BaseEntity {
    @Column(name = "contract_content")
    private String contractContent;

    ///  Relation
    // contracttemplate
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_template_id")
    private ContractTemplates contractTemplates;

}
