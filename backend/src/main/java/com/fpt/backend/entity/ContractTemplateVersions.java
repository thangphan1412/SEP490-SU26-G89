package com.fpt.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Builder
@Table(
        name = "contract_template_versions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_template_version_number",
                columnNames = {"contract_template_id", "version_number"}
        )
)
public class ContractTemplateVersions extends BaseEntity {
    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "version_name", nullable = false, columnDefinition = "nvarchar(100)")
    private String versionName;

    @Column(name = "template_content", columnDefinition = "nvarchar(max)")
    private String templateContent;

    @Column(name = "layout_json", columnDefinition = "nvarchar(max)")
    private String layoutJson;

    @Column(name = "page_count", nullable = false)
    private Integer pageCount;

    @Column(name = "change_note", columnDefinition = "nvarchar(1000)")
    private String changeNote;

    @Column(name = "created_by", columnDefinition = "nvarchar(150)")
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_template_id", nullable = false)
    private ContractTemplates contractTemplate;

    @OneToMany(mappedBy = "contractTemplateVersion")
    private List<Contracts> contracts;

    @OneToMany(
            mappedBy = "contractTemplateVersion",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("pageNumber ASC, createdAt ASC")
    private List<ContractPositions> positions;
}
