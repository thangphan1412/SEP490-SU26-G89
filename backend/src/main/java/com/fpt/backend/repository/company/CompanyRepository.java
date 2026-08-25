package com.fpt.backend.repository.company;

import com.fpt.backend.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    // Tìm hồ sơ công ty nội bộ (công ty của mình)
    Optional<Company> findByIsInternalTrue();
}