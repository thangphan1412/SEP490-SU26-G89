package com.fpt.backend.repository.electronicSignature;

import com.fpt.backend.dto.response.electronicSignature.ElectronicSignatureDetailResponse;
import com.fpt.backend.dto.response.electronicSignature.ListElectronicResponse;
import com.fpt.backend.entity.ElectronicSignatures;
import com.fpt.backend.enums.ElectronicStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ElectronicSignatureRepository extends JpaRepository<ElectronicSignatures, UUID> {
@Query("""
    select new com.fpt.backend.dto.response.electronicSignature.ListElectronicResponse(
        es.id,
            es.electronicSignatureName,
        es.electronicSignatureType,
        es.status,
        es.isDefault,
        es.createdAt,
        es.fileStorage.filePath,
        uk.publicKey,
        uk.keyFingerprint,
        uk.keyAlgorithm,
        uk.keySize
    )
    from ElectronicSignatures es
    left join es.userKey uk
    where es.user.id = :userId
    """)
        List<ListElectronicResponse> getAllElectronicSignaturesById(@Param("userId") UUID userId);


    @Query("""
        select new com.fpt.backend.dto.response.electronicSignature.ElectronicSignatureDetailResponse(
                
                        es.electronicSignatureName ,
                        es.electronicSignatureType,
                                es.status,
                                        es.isDefault,
                                                es.createdAt,
                                                es.fileStorage.filePath,
                                                uk.publicKey,
                                                uk.keyFingerprint,
                                                uk.keyAlgorithm,
                                                uk.keySize
                )
                        from ElectronicSignatures es
                        left join es.userKey uk
                                where es.user.id =:userId and es.id =:signatureId
        """)
    ElectronicSignatureDetailResponse getElectronicSignaturesById(@Param("userId") UUID userId, @Param("signatureId") UUID signatureId);

    Optional<ElectronicSignatures> findByIdAndUserIdAndStatus(
            UUID id,
            UUID userId,
            ElectronicStatus status
    );
}


