package com.fpt.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "contract_attribute_values")
public class ContractAttributeValues extends BaseEntity{
    @Column(name = "attribute_key", columnDefinition = "nvarchar(max)")
    private String attributeKey;
    @Column(name = "attribute_value", columnDefinition = "nvarchar(max)")
    private String attributeValue;
    @Column(name = "value_source", columnDefinition = "nvarchar(max)")
    private String valueSource;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    ///
    //    - contract_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contracts contract;
}
