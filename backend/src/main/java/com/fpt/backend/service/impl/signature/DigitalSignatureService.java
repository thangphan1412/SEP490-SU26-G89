package com.fpt.backend.service.impl.signature;

import com.fpt.backend.entity.UserKeys;
import com.fpt.backend.repository.signature.UserKeysRepository;
import com.fpt.backend.util.RSAKeyConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Base64;

@Service
public class DigitalSignatureService {
    @Autowired
    private  UserKeysRepository userKeysRepository;

    public String sign(byte[] document, java.util.UUID userId) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(document);
        UserKeys userKeys = userKeysRepository.findByUserId(userId)
                        .orElseThrow(() -> new IllegalArgumentException("User RSA key not found"));
        BigInteger[] privateKey = RSAKeyConverter.decode(userKeys.getPrivateKey());
        BigInteger n = privateKey[0];
        BigInteger d = privateKey[1];
        BigInteger hashNumber = new BigInteger(1, hash);
        BigInteger signature = hashNumber.modPow(d, n);
        return Base64.getEncoder().encodeToString(signature.toByteArray());
    }
}
