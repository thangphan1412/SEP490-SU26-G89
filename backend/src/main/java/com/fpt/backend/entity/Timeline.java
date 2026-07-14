package com.fpt.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "time_line")
public class Timeline extends BaseEntity{
    private String title;
    private String description;
    private Date startDate;
    private Date endDate;
    private String status;
    private Double Progress;

    /// Reltion
    // deliverble
    @OneToMany(mappedBy = "timeline")
    private List<Deliverable> deliverable;
    //project
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Projects project;
    //timelineTask
    @OneToMany(mappedBy = "timeline")
    private List<TimelineTask>  timelineTasks;
}
