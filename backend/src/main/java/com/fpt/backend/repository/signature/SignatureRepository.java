package com.fpt.backend.repository.signature;


import com.fpt.backend.entity.Signature;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SignatureRepository extends JpaRepository<Signature, UUID> {
<<<<<<< HEAD
<<<<<<< HEAD
    @EntityGraph(attributePaths = {
            "signedBy",
            "electronicSignatures",
            "fileStorage",
            "workflowStepInstance"
    })
    List<Signature> findByContractIdOrderBySignedAtAsc(UUID contractId);

    boolean existsByContractIdAndWorkflowStepInstanceId(
            UUID contractId,
            UUID workflowStepInstanceId
    );
=======
=======
>>>>>>> origin
    boolean existsByContractId(UUID contractId);
//    @Query("""
//            select new com.fpt.backend.dto.response.signature.SignatureListResponse(
//                                                                                         s.signatureName,
//                                                                                         new com.fpt.backend.dto.response.electronicSignature.ListElectronicResponse(
//                                                                                             es.electronicSignatureType,
//                                                                                             es.status,
//                                                                                             es.isDefault,
//                                                                                             es.createdAt
//                                                                                         )
//                                                                                     )
//                                                                                     from Signature s
//                                                                                     join s.electronicSignatures es
//                                                                                     where s.userKey.user.id = :userId
//        """)
//    List<SignatureListResponse> findAll(@Param("userId") UUID userId);
<<<<<<< HEAD
>>>>>>> 7d6eb51fe9c660b46d1a1bc0200bcbbc73cf5f51
=======
>>>>>>> origin
}
