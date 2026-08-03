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
@Table(name = "contract_positions")
public class ContractPositions extends BaseEntity{
    @Column(name = "attribute_key")
    private String attributeKey;
    @Column(name = "field_label")
    private String fieldLabel;
    @Column(name = "page_number")
    private Integer pageNumber;
    @Column(name = "x_position")
    private Double xPosition;
    @Column(name = "y_position")
    private Double yPosition;
    @Column(name = "width")
    private Double width;
    @Column(name = "height")
    private Double height;
    @Column(name = "field_type")
    private String fieldType;
    @Column(name = "value_source")
    private String valueSource;
    @Column(name = "signer_role")
    private String signerRole;
    @Column(name = "is_system_field")
    private Boolean isSystemField;
    @Column(name = "is_required")
    private Boolean isRequired;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /// relation
    // Positions belong to an immutable template version, not the parent template.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_template_version_id", nullable = false)
    private ContractTemplateVersions contractTemplateVersion;
}
