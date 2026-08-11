package com.fpt.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Builder
@Table(name = "timeline_task")
public class TimelineTask extends BaseEntity{

     private String Title;
    private String status;
    private Date startDate;
    private Date endDate;
    //            TimelineId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timeline_id")
    private Timeline timeline;
//    AssignedTo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private Users assignedTo;
}
