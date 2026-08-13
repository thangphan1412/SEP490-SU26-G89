package com.fpt.backend.service.impl.signature;

import com.fpt.backend.entity.UserKeys;
import com.fpt.backend.repository.signature.UserKeysRepository;
import com.fpt.backend.util.RSAKeyConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DigitalSignatureService {

    private final UserKeysRepository userKeysRepository;

    public SignatureResult sign(
            byte[] document,
            UUID userId
    ) throws Exception {

        // 1. SHA-256 PDF
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(document);
        String documentHash = Base64.getEncoder().encodeToString(hash);
        // 2. Lấy RSA key của User
        UserKeys userKeys = userKeysRepository.findByUserId(userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User RSA key not found"
                                )
                        );

        // 3. Decode private key
        BigInteger[] privateKey = RSAKeyConverter.decode(
                        userKeys.getPrivateKey()
                );
        BigInteger n = privateKey[0];
        BigInteger d = privateKey[1];
        // 4. Hash -> BigInteger
        BigInteger hashNumber = new BigInteger(1, hash);
        // 5. RSA signing
        BigInteger signature = hashNumber.modPow(d, n);
        // 6. Signature -> Base64
        String signatureValue = Base64.getEncoder().encodeToString(
                                signature.toByteArray());

        return new SignatureResult(
                documentHash,
                signatureValue,
                userKeys
        );
    }

    public record SignatureResult(
            String documentHash,
            String signatureValue,
            UserKeys userKey
    ) {
    }
}