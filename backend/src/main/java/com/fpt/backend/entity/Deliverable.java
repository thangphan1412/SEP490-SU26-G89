package com.fpt.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "deliverable")
public class Deliverable extends BaseEntity{
    @Column(columnDefinition = "nvarchar(max)")
    private String title;
    @Column(columnDefinition = "nvarchar(max)")
    private String description;
    private Date dueDate;
    @Column(columnDefinition = "nvarchar(max)")
    private String status;
//    AttachmentId

//            TimelineId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timeline_id")
    private Timeline timeline;
}
