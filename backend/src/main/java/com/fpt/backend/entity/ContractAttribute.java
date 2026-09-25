package com.fpt.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "contract_attribute")
public class ContractAttribute extends BaseEntity {
    @Column(name = "attribute_key", columnDefinition = "nvarchar(max)")
    private String attributeKey;
    @Column(name = "attribute_label", columnDefinition = "nvarchar(max)")
    private String attributeLabel;
    //enum
    @Column(name = "input_type", columnDefinition = "nvarchar(max)")
    private String inputType;
    @Column(name = "default_value", columnDefinition = "nvarchar(max)")
    private String defaultValue;
    @Column(name = "options_json", columnDefinition = "nvarchar(max)")
    private String optionsJson;
    @Column(name = "is_required")
    private Boolean isRequired;
    @Column(name = "is_editable")
    private Boolean isEditable;
    @Column(name = "is_system_field")
    private Boolean isSystemField;
    @Column(name = "display_order")
    private Integer displayOrder;
    @Column(name = "validation_regex", columnDefinition = "nvarchar(max)")
    private String validationRegex;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    ///  Relation
    // contracttemplate
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_template_id")
    private ContractTemplates contractTemplates;

}
