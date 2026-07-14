package com.fpt.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.processing.Pattern;

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
    @Column(name = "user_status")
    private String status;
    @Column(name = "user_role")
    private String role;

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
    // Signature
    @OneToMany(mappedBy = "user")
    private List<Signature> signatures;
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
}
