package com.fpt.backend.service.impl.signature;


import com.fpt.backend.entity.UserKeys;
import com.fpt.backend.repository.signature.UserKeysRepository;
import com.fpt.backend.util.RSAKeyConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class DigitalSignatureVerificationService {
    private final UserKeysRepository userKeysRepository;
    public boolean verify( byte[] document, String signatureValue, UUID userId ) throws Exception {
        // =========================================================
        // 1. Lấy Public Key của User từ Database
        // =========================================================
         UserKeys userKeys = userKeysRepository.findByUserId(userId)
                 .orElseThrow(() -> new IllegalArgumentException( "User RSA key not found" ) );
         // =========================================================
        // 2. Decode Public Key //
        // Public Key trong DB hiện tại:
        //Base64(X.509 SubjectPublicKeyInfo) //
        // Base64 // ↓ // byte[] // ↓ // X509EncodedKeySpec // ↓ // PublicKey // =========================================================
         byte[] publicKeyBytes = Base64 .getDecoder() .decode(userKeys.getPublicKey());
         X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
         KeyFactory keyFactory = KeyFactory.getInstance("RSA");
         PublicKey publicKey = keyFactory.generatePublic(keySpec);
         // =========================================================
        // 3. Decode Signature //
        // Frontend sẽ gửi: // Base64(signature) // // Base64 // ↓ // byte[]
        // =========================================================
        byte[] signatureBytes = Base64 .getDecoder() .decode(signatureValue);
        // =========================================================
        // 4. Tạo RSA Signature Verifier // // SHA256withRSA: // // SHA-256(document) // ↓ // RSA verify bằng Public Key
        // =========================================================

        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(publicKey);
        // =========================================================
        // 5. Đưa document gốc vào verifier //

        // Không cần tự SHA-256 ở đây. //
        // SHA256withRSA sẽ tự hash document bằng SHA-256.
        // =========================================================
        verifier.update(document);
        // ========================================================= //
        // 6. Verify
        // =========================================================
        return verifier.verify(signatureBytes);
    }
}