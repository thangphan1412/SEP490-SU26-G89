package com.fpt.backend.repository.department;

import com.fpt.backend.entity.Departments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Departments, UUID> {
    Boolean existsByDepartmentCodeIgnoreCase(String departmentCode);

    Boolean existsByDepartmentCodeIgnoreCaseAndIdNot(String departmentCode, UUID id);

    // Thêm hàm này để tìm Department theo tên
    Optional<Departments> findByDepartmentName(String departmentName);

}
