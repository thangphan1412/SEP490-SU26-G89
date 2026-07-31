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
@Table(name = "contract_types")
public class ContractTypes extends BaseEntity {
    @Column(name = "contract_type_name", columnDefinition = "nvarchar(150)")
    private String contractTypeName;
    @Column(name = "contract_type_code", length = 50)
    private String contractTypeCode;
    @Column(name = "description", columnDefinition = "nvarchar(1000)")
    private String description;
    @Column(name = "validity_days")
    private Integer validityDays;
    @Column(name = "category", columnDefinition = "nvarchar(100)")
    private String category;
    @Column(name = "status", columnDefinition = "nvarchar(30)")
    private String status;
    @Column(name = "created_by", columnDefinition = "nvarchar(150)")
    private String createdBy;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /// Relation
    // Contract
    @OneToMany(mappedBy = "contractType")
    private List<Contracts> contracts;
    // ContractTemplate
    @OneToMany(mappedBy = "contractType")
    private List<ContractTemplates> contractTemplates;
}
