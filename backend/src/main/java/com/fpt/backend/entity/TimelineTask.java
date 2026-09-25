package com.fpt.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Builder
@Table(name = "timeline_task")
public class TimelineTask extends BaseEntity {

    @Nationalized
    @Column(name = "title", length = 255, columnDefinition = "nvarchar(255)")
    private String title;
    @Column(columnDefinition = "nvarchar(max)")
    private String status;
    private Date startDate;
    private Date endDate;

    // TimelineId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timeline_id")
    private Timeline timeline;

    // AssignedTo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private Users assignedTo;
}
