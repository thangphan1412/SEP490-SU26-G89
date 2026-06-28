package com.fpt.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Builder
@Table(name = "contract_types")
public class ContractTypes extends BaseEntity {
    @Column(name = "contract_type_name")
    private String contractTypeName;
    @Column(name = "contract_type_code")
    private String contractTypeCode;

    /// Relation
    //Contract
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contracts contract;
    //COntractTemplate
    @OneToMany(mappedBy = "contractType")
    private List<ContractTemplates> contractTemplates;
}
