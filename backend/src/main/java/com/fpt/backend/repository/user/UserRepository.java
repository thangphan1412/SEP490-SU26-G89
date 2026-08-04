package com.fpt.backend.repository.user;

import com.fpt.backend.dto.request.user.UserFilterRequestDTO;
import com.fpt.backend.entity.Departments;
import com.fpt.backend.entity.Users;

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
                        "userRoles.role"
        })
        Optional<Users> findByEmail(String email);

        Boolean existsByEmail(String email);

        // THÊM 3 HÀM TÌM KIẾM THEO PHÂN QUYỀN
        List<Users> findByRole(String role);

        List<Users> findByRoleIn(List<String> roles);

        List<Users> findByRoleAndDepartment(String role, Departments department);


        @Query("SELECT u FROM Users u " +
                "LEFT JOIN FETCH u.department d " +
                "WHERE (:#{#filter.keyword} = '' OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :#{#filter.keyword}, '%')) " +
                "   OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :#{#filter.keyword}, '%')) " +
                "   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :#{#filter.keyword}, '%'))) " +
                "AND (:#{#filter.role} = '' OR u.role = :#{#filter.role}) " +
                "AND (:#{#filter.statusEnum} IS NULL OR u.status = :#{#filter.statusEnum}) " + // ĐÃ SỬA DÒNG NÀY
                "AND (:#{#filter.departmentName} = '' OR d.departmentName = :#{#filter.departmentName}) " +
                "AND u.role IN :#{#filter.allowedRoles}")
        List<Users> searchAndFilterUsers(@Param("filter") UserFilterRequestDTO filter);
}