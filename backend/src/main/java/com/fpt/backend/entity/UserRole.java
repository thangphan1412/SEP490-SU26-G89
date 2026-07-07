package com.fpt.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.apache.catalina.User;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Entity
@Table(name = "user_role")
public class UserRole extends BaseEntity{
    /// relation
    // role
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_Id")
    private Role role;
    // user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;
}
