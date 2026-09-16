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
    public boolean verify( byte[] document, String signatureValue, UUID userId , String keyCode)
            throws Exception {

         UserKeys userKeys = userKeysRepository.findByUserIdAndKeyCode(userId, keyCode)
                 .orElseThrow(() -> new IllegalArgumentException( "User RSA key not found" ) );

         byte[] publicKeyBytes = Base64 .getDecoder() .decode(userKeys.getPublicKey());
         X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
         KeyFactory keyFactory = KeyFactory.getInstance("RSA");
         PublicKey publicKey = keyFactory.generatePublic(keySpec);
        byte[] signatureBytes = Base64 .getDecoder() .decode(signatureValue);
        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(publicKey);
        verifier.update(document);
        return verifier.verify(signatureBytes);
    }
}