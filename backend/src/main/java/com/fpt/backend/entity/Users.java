package com.fpt.backend.entity;

import com.fpt.backend.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.processing.Pattern;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users")
public class Users extends BaseEntity {

    @Column(name = "user_email")
    private String email;
    @Column(name = "user_password")
    private String password;
    @Column(name = "user_firstName")
    private String firstName;
    @Column(name = "user_lastName")
    private String lastName;
    @Column(name = "user_number_phone")
    private String numberPhone;
    @Enumerated(EnumType.STRING)
    @Column(name = "user_status")
    private UserStatus status;
    @Column(name = "date_of_birth")
    private String dob;
    // THÊM TRƯỜNG NÀY ĐỂ LƯU THỜI GIAN HOẠT ĐỘNG CUỐI
    @Column(name = "last_active")
    private LocalDateTime lastActive;
    @Column(name = "start_date")
    private String startDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
        this.updatedAt = LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
    }

    /// Relation
    // permissions
    @OneToMany(mappedBy = "user")
    private List<UserPermission>  userPermissions;
    //Logs
    @OneToMany(mappedBy = "user")
    private  List<ActivityLog> activityLogs;
    //Department
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Departments department;
    //UserKey
    @OneToMany(mappedBy = "user")
    private List<UserKeys> userKeys;
    //Notification
    @OneToMany(mappedBy = "user")
    private List<Notifications> notifications;
    //FileStorage
    @OneToMany(mappedBy = "user")
    private List<FileStorage> fileStorages;
    //Proposal
    @OneToMany(mappedBy = "user")
    private List<Proposals> proposals;
    // userrole
    @OneToMany(mappedBy = "user")
    private List<UserRole> userRoles;
    // contracappoval
    @OneToMany(mappedBy = "user")
    private List<ContractApprovals>  contractApprovals;
    @OneToMany(mappedBy = "user")
    private List<ProjectMember>  projectMembers;
    @OneToMany(mappedBy = "assignedTo")
    private List<TimelineTask>  timelineTasks;
    @OneToMany(mappedBy = "user")
    private List<ElectronicSignatures>  electronicSignatures;

}
