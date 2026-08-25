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
public class DigitalSignatureVerificationService {

    private final UserKeysRepository userKeysRepository;

    public boolean verify(
            byte[] document,
            String signatureHash,
            UUID userId
    ) throws Exception {

        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        byte[] hash = digest.digest(document);

        BigInteger originalHash = new BigInteger(1, hash);
        UserKeys userKeys = userKeysRepository
                        .findByUserId(userId)
                        .orElseThrow(() -> new IllegalArgumentException("User RSA key not found"));
        BigInteger[] publicKey = RSAKeyConverter.decode(userKeys.getPublicKey());
        BigInteger n = publicKey[0];
        BigInteger e = publicKey[1];
        // 3. Decode signature
        byte[] signatureBytes =
                Base64.getDecoder()
                        .decode(signatureHash);

        BigInteger signature = new BigInteger(1, signatureBytes);
        // 4. RSA Verify
        // recoveredHash = signature^e mod n
        BigInteger recoveredHash = signature.modPow(e, n);
        // 5. Compare
        return originalHash.equals(
                recoveredHash
        );
    }
}