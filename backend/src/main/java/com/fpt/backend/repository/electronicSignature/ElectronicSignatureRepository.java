package com.fpt.backend.repository.electronicSignature;

import com.fpt.backend.entity.ElectronicSignatures;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ElectronicSignatureRepository extends JpaRepository<ElectronicSignatures, UUID> {
}
