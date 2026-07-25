package com.fpt.backend.repository.project;

import com.fpt.backend.entity.Contracts;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProjectContractRepository extends Repository<Contracts, UUID> {
    @Query("""
            SELECT contract
            FROM Contracts contract
            WHERE contract.project.id = :projectId
            ORDER BY contract.id
            """)
    List<Contracts> findByProjectId(@Param("projectId") UUID projectId);
}
