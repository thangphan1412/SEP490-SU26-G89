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
        // Tìm người dùng theo email và nạp kèm danh sách vai trò để kiểm tra quyền.
        @EntityGraph(attributePaths = {
                        "userRoles",
                        "userRoles.role"
        })
        Optional<Users> findByEmail(String email);

        // Kiểm tra email đã được sử dụng bởi người dùng nào trong hệ thống hay chưa.
        Boolean existsByEmail(String email);

        // Tìm người dùng theo một vai trò cụ thể.
        List<Users> findByRole(String role);

        // Tìm người dùng thuộc một trong các vai trò được cung cấp.
        List<Users> findByRoleIn(List<String> roles);

        // Tìm người dùng theo đồng thời vai trò và phòng ban.
        List<Users> findByRoleAndDepartment(String role, Departments department);


        @Query("SELECT u FROM Users u " +
                "LEFT JOIN FETCH u.department d " +
                "WHERE (:#{#filter.keyword} = '' OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :#{#filter.keyword}, '%')) " +
                "   OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :#{#filter.keyword}, '%')) " +
                "   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :#{#filter.keyword}, '%'))) " +
                "AND (:#{#filter.role} = '' OR u.role = :#{#filter.role}) " +
                "AND (:#{#filter.statusEnum} IS NULL OR u.status = :#{#filter.statusEnum}) " +
                "AND (:#{#filter.departmentName} = '' OR d.departmentName = :#{#filter.departmentName}) " +
                "AND u.role IN :#{#filter.allowedRoles}")
        Page<Users> searchAndFilterUsers(@Param("filter") UserFilterRequestDTO filter, Pageable pageable);
}
