package com.fpt.backend.repository.department;

import com.fpt.backend.entity.Departments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Departments, Integer> {
    Boolean existsByDepartmentCodeIgnoreCase(String departmentCode);

    Boolean existsByDepartmentCodeIgnoreCaseAndIdNot(String departmentCode, Integer id);

    // Thêm hàm này để tìm Department theo tên
    Optional<Departments> findByDepartmentName(String departmentName);
}
