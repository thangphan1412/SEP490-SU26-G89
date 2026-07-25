package com.fpt.backend.repository.user;

import com.fpt.backend.entity.Departments;
import com.fpt.backend.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Integer> { // Sửa UUID thành Integer
    Optional<Users> findByEmail(String email);
    Boolean existsByEmail(String email);

    // THÊM 3 HÀM TÌM KIẾM THEO PHÂN QUYỀN
    List<Users> findByRole(String role);
    List<Users> findByRoleIn(List<String> roles);
    List<Users> findByRoleAndDepartment(String role, Departments department);

    // BẠN THÊM CÂU QUERY NÀY VÀO:
    @Query("SELECT u FROM Users u " +
            "LEFT JOIN FETCH u.department d " + // THÊM CHỮ 'FETCH' VÀO ĐÂY
            "WHERE (:keyword = '' OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:role = '' OR u.role = :role) " +
            "AND (:status = '' OR u.status = :status) " +
            "AND (:departmentName = '' OR d.departmentName = :departmentName) " +
            "AND u.role IN :allowedRoles")
    List<Users> searchAndFilterUsers(
            @Param("keyword") String keyword,
            @Param("role") String role,
            @Param("status") String status,
            @Param("departmentName") String departmentName,
            @Param("allowedRoles") List<String> allowedRoles
    );
}