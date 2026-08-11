package com.fpt.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
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
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "contract_type_workflows",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_contract_type_workflow_version",
                columnNames = {"contract_type_id", "version_number"}
        )
)
@SuppressWarnings("JpaDataSourceORMInspection") // Bạn thêm dòng này vào đây nhé
public class ContractTypeWorkflow extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "contract_type_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_contract_workflow_type")
    )
    private ContractTypes contractType;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "workflow_name", nullable = false, columnDefinition = "nvarchar(150)")
    private String workflowName;

    @Column(name = "is_active", nullable = false)
    private Boolean active;

    @Column(name = "created_by", columnDefinition = "nvarchar(150)")
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(
            mappedBy = "workflow",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("stepOrder ASC")
    private List<ContractTypeWorkflowStep> steps = new ArrayList<>();
}