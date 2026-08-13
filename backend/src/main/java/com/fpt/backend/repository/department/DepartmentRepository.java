package com.fpt.backend.repository.department;

import com.fpt.backend.entity.Departments;
import com.fpt.backend.enums.DepartmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Departments, UUID> {
    Boolean existsByDepartmentCodeIgnoreCase(String departmentCode);

    @Query("""
            SELECT department
            FROM Departments department
            WHERE (
                :search = ''
                OR LOWER(COALESCE(department.departmentName, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
                OR LOWER(COALESCE(department.departmentCode, '')) LIKE CONCAT('%', CONCAT(:search, '%'))
            )
            AND (
                :status IS NULL
                OR department.departmentStatus = :status
            )
            ORDER BY department.id DESC
            """)
    List<Departments> searchAndFilter(
            @Param("search") String search,
            @Param("status") DepartmentStatus status
    );
    Boolean existsByDepartmentCodeIgnoreCaseAndIdNot(String departmentCode, UUID id);

    // Thêm hàm này để tìm Department theo tên
    Optional<Departments> findByDepartmentName(String departmentName);

}
