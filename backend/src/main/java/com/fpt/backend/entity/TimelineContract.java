package com.fpt.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "timeline_contract", indexes = {
        @Index(name = "idx_timeline_contract_timeline", columnList = "timeline_id")
})
public class TimelineContract extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "timeline_id", nullable = false, foreignKey = @ForeignKey(name = "fk_timeline_contract_timeline"))
    private Timeline timeline;

    /*
     * unique = true bảo đảm một contract
     * chỉ thuộc tối đa một timeline.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_timeline_contract_contract"))
    private Contracts contract;

    @Column(name = "linked_at", nullable = false)
    private LocalDateTime linkedAt;
}