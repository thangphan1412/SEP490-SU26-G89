package com.fpt.backend.repository.user;

import com.fpt.backend.dto.request.user.UserFilterRequestDTO;
import com.fpt.backend.entity.Departments;
import com.fpt.backend.entity.Users;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<Users, UUID> {

    @EntityGraph(attributePaths = {
            "userRoles",
            "userRoles.role",
            "department"
    })
    Optional<Users> findByEmail(String email);

    Boolean existsByEmail(String email);

    List<Users> findByUserRoles_Role_RoleName(String roleName);

    List<Users> findByUserRoles_Role_RoleNameIn(List<String> roleNames);

    List<Users> findByUserRoles_Role_RoleNameAndDepartment(
            String roleName,
            Departments department
    );

    @Query("""
        SELECT DISTINCT u
        FROM Users u
        LEFT JOIN FETCH u.department d
        LEFT JOIN u.userRoles ur
        LEFT JOIN ur.role r
        WHERE (:#{#filter.keyword} = ''
            OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :#{#filter.keyword}, '%'))
            OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :#{#filter.keyword}, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :#{#filter.keyword}, '%')))
        AND (:#{#filter.role} = '' OR r.roleName = :#{#filter.role})
        AND (:#{#filter.statusEnum} IS NULL OR u.status = :#{#filter.statusEnum})
        AND (:#{#filter.departmentName} = '' OR d.departmentName = :#{#filter.departmentName})
        AND r.roleName IN :#{#filter.allowedRoles}
    """)
    Page<Users> searchAndFilterUsers(
            @Param("filter") UserFilterRequestDTO filter,
            Pageable pageable
    );
}
