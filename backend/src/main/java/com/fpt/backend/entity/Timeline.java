package com.fpt.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "time_line")
public class Timeline extends BaseEntity {

    @Column(name = "title", nullable = false, columnDefinition = "nvarchar(150)")
    private String title;

    @Column(name = "description", columnDefinition = "nvarchar(500)")
    private String description;

    @Column(name = "start_date", nullable = false)
    private Date startDate;

    @Column(name = "end_date", nullable = false)
    private Date endDate;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "progress")
    private Double progress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Projects project;

    @OneToMany(mappedBy = "timeline")
    private List<TimelineTask> timelineTasks = new ArrayList<>();

    @OneToMany(mappedBy = "timeline")
    private List<Deliverable> deliverables = new ArrayList<>();

    @OneToMany(mappedBy = "timeline", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimelineContract> timelineContracts = new ArrayList<>();
}