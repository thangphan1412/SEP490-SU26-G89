package com.fpt.backend.service.impl.contract;

import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.ElectronicSignatures;
import com.fpt.backend.entity.FileStorage;
import com.fpt.backend.entity.Signature;
import com.fpt.backend.entity.UserKeys;
import com.fpt.backend.entity.Users;
import com.fpt.backend.enums.ElectronicStatus;
import com.fpt.backend.enums.SignatureAlgorithm;
import com.fpt.backend.enums.SignatureHash;
import com.fpt.backend.enums.SignatureStatus;
import com.fpt.backend.enums.SignatureType;
import com.fpt.backend.repository.electronicSignature.ElectronicSignatureRepository;
import com.fpt.backend.repository.signature.SignatureRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractSignatureServiceTest {
    @Mock
    private SignatureRepository signatureRepository;
    @Mock
    private ElectronicSignatureRepository electronicSignatureRepository;
    @InjectMocks
    private ContractSignatureService service;

    @Test
    void storesOwnedActiveSignatureImageAndApprovedPdfHash() {
        UUID userId = UUID.randomUUID();
        UUID electronicSignatureId = UUID.randomUUID();
        Users signer = new Users();
        signer.setId(userId);
        signer.setFirstName("Contract");
        signer.setLastName("Signer");

        FileStorage file = FileStorage.builder()
                .filePath("https://res.cloudinary.com/example/signature.png")
                .isDeleted(false)
                .build();
        ElectronicSignatures electronicSignature = ElectronicSignatures.builder()
                .electronicSignatureName("Primary signature")
                .status(ElectronicStatus.ACTIVE)
                .fileStorage(file)
                .user(signer)
                .build();
        electronicSignature.setId(electronicSignatureId);

        Contracts contract = new Contracts();
        contract.setId(UUID.randomUUID());
        contract.setContractNumber("CON-2026-001");
        contract.setContractTitle("Service contract");
        contract.setEffectiveDate(LocalDate.of(2026, 9, 1));
        contract.setExpirationDate(LocalDate.of(2027, 8, 31));
        contract.setContractContent("Clause {{contract_value}}");
        contract.setContractLayoutJson("{\"pageCount\":1}");

        when(electronicSignatureRepository.findByIdAndUserIdAndStatus(
                electronicSignatureId,
                userId,
                ElectronicStatus.ACTIVE
        )).thenReturn(Optional.of(electronicSignature));
        when(signatureRepository.save(any(Signature.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        byte[] approvedPdf = "%PDF-1.7\nCEO approved PDF".getBytes();
        CryptoProof cryptoProof = createCryptoProof(approvedPdf);
        UserKeys userKey = createUserKey(cryptoProof, signer);
        electronicSignature.setUserKey(userKey);
        service.recordSignature(
                contract,
                null,
                signer,
                electronicSignatureId,
                "DIRECTOR",
                approvedPdf,
                cryptoProof.digitalSignature()
        );

        ArgumentCaptor<Signature> captor = ArgumentCaptor.forClass(Signature.class);
        verify(signatureRepository).save(captor.capture());
        Signature stored = captor.getValue();
        assertThat(stored.getContract()).isSameAs(contract);
        assertThat(stored.getSignedBy()).isSameAs(signer);
        assertThat(stored.getFileStorage()).isSameAs(file);
        assertThat(stored.getElectronicSignatures()).isSameAs(electronicSignature);
        assertThat(stored.getUserKey()).isSameAs(userKey);
        assertThat(stored.getSignerRole()).isEqualTo("DIRECTOR");
        assertThat(stored.getStatus()).isEqualTo(SignatureStatus.SIGNED);
        assertThat(stored.getSignatureType())
                .isEqualTo(SignatureType.INTERNAL_RSA);
        assertThat(stored.getSignatureAlgorithm())
                .isEqualTo(SignatureAlgorithm.RSA2048);
        assertThat(stored.getSignatureHash()).isEqualTo(SignatureHash.SHA256);
        assertThat(stored.getDocumentHash())
                .isEqualTo(service.calculatePdfHash(approvedPdf));
        assertThat(stored.getSigningPublicKey())
                .isEqualTo(cryptoProof.publicKeyPem());
        assertThat(stored.getDigitalSignature())
                .isEqualTo(cryptoProof.digitalSignature());
        assertThat(stored.getPublicKeyFingerprint()).matches("[0-9a-f]{64}");
        assertThat(stored.getSignedAt()).isNotNull();
    }

    @Test
    void rejectsSignatureThatIsNotActiveAndOwnedBySigner() {
        UUID electronicSignatureId = UUID.randomUUID();
        Users signer = new Users();
        signer.setId(UUID.randomUUID());
        Contracts contract = new Contracts();
        contract.setId(UUID.randomUUID());

        when(electronicSignatureRepository.findByIdAndUserIdAndStatus(
                electronicSignatureId,
                signer.getId(),
                ElectronicStatus.ACTIVE
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.recordSignature(
                contract,
                null,
                signer,
                electronicSignatureId,
                "PARTNER",
                "%PDF-1.7\nCEO approved PDF".getBytes(),
                null
        )).hasMessageContaining("does not belong to you");
        verify(signatureRepository, never()).save(any());
    }

    @Test
    void rejectsProofCreatedForDifferentPdfWithoutSavingSignature() {
        UUID electronicSignatureId = UUID.randomUUID();
        Users signer = new Users();
        signer.setId(UUID.randomUUID());
        Contracts contract = new Contracts();
        contract.setId(UUID.randomUUID());

        FileStorage file = FileStorage.builder()
                .filePath("https://res.cloudinary.com/example/signature.png")
                .isDeleted(false)
                .build();
        ElectronicSignatures electronicSignature = ElectronicSignatures.builder()
                .electronicSignatureName("Primary signature")
                .status(ElectronicStatus.ACTIVE)
                .fileStorage(file)
                .user(signer)
                .build();
        electronicSignature.setId(electronicSignatureId);
        when(electronicSignatureRepository.findByIdAndUserIdAndStatus(
                electronicSignatureId,
                signer.getId(),
                ElectronicStatus.ACTIVE
        )).thenReturn(Optional.of(electronicSignature));

        byte[] approvedPdf = "%PDF-1.7\nCEO approved PDF".getBytes();
        CryptoProof proofForDifferentPdf = createCryptoProof(
                "%PDF-1.7\nDifferent content".getBytes()
        );
        electronicSignature.setUserKey(createUserKey(
                proofForDifferentPdf,
                signer
        ));

        assertThatThrownBy(() -> service.recordSignature(
                contract,
                null,
                signer,
                electronicSignatureId,
                "DIRECTOR",
                approvedPdf,
                proofForDifferentPdf.digitalSignature()
        )).hasMessageContaining("does not match");
        verify(signatureRepository, never()).save(any());
    }

    private CryptoProof createCryptoProof(byte[] content) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            java.security.Signature signer = java.security.Signature
                    .getInstance("SHA256withRSA");
            signer.initSign(keyPair.getPrivate());
            signer.update(content);

            String publicKeyBody = Base64.getMimeEncoder(
                    64,
                    new byte[]{'\n'}
            ).encodeToString(keyPair.getPublic().getEncoded());
            return new CryptoProof(
                    "-----BEGIN PUBLIC KEY-----\n"
                            + publicKeyBody
                            + "\n-----END PUBLIC KEY-----",
                    Base64.getEncoder().encodeToString(signer.sign())
            );
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private UserKeys createUserKey(CryptoProof proof, Users signer) {
        ContractSignatureService.RegisteredPublicKey registered =
                service.validatePublicKey(proof.publicKeyPem());
        return UserKeys.builder()
                .keySize(registered.keySize())
                .publicKey(registered.publicKeyPem())
                .keyFingerprint(registered.publicKeyFingerprint())
                .user(signer)
                .build();
    }

    private record CryptoProof(
            String publicKeyPem,
            String digitalSignature
    ) {
    }
}
