package com.fpt.backend.service.impl.signature;

import com.fpt.backend.dto.response.signature.UserKeyInfoResponse;
import com.fpt.backend.entity.UserKeys;
import com.fpt.backend.entity.Users;
import com.fpt.backend.enums.KeyAlgorithm;
import com.fpt.backend.repository.signature.UserKeysRepository;
import com.fpt.backend.service.interfaces.signature.IUserKeyService;
import com.fpt.backend.util.CalculateRSA;
import com.fpt.backend.util.CertificateGenerator;
import com.fpt.backend.util.RSAKeyConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.KeyFactory;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class UserKeyServiceImpl implements IUserKeyService {

    private final UserKeysRepository userKeysRepository;

    private final CalculateRSA calculateRSA;

    @Override
    @Transactional
    public UserKeyInfoResponse generateUserKey(Users user) {

        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException(
                    "User must be saved before generating key"
            );
        }
        CalculateRSA.RSAKeyPair keyPair = calculateRSA.generateKeyPair();
        String publicKeyCode =
                changToPingPrivateKey(
                        keyPair.modulus().toString(16)
                );

        String publicKey = encodePublicKey(
                keyPair.modulus(),
                keyPair.publicExponent()
        );

        String privateKey;

        try {
            privateKey = RSAKeyConverter.encodePKCS8(
                    keyPair.modulus(),
                    keyPair.publicExponent(),
                    keyPair.privateExponent(),
                    keyPair.prime1(),
                    keyPair.prime2(),
                    keyPair.exponent1(),
                    keyPair.exponent2(),
                    keyPair.coefficient()
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Unable to encode RSA private key to PKCS#8",
                    e
            );
        }


        PublicKey publicKeyObject;
        PrivateKey privateKeyObject;

        try {

            KeyFactory keyFactory =
                    KeyFactory.getInstance("RSA");


            RSAPublicKeySpec publicKeySpec =
                    new RSAPublicKeySpec(
                            keyPair.modulus(),
                            keyPair.publicExponent()
                    );

            publicKeyObject =
                    keyFactory.generatePublic(publicKeySpec);

            RSAPrivateCrtKeySpec privateKeySpec =
                    new RSAPrivateCrtKeySpec(
                            keyPair.modulus(),
                            keyPair.publicExponent(),
                            keyPair.privateExponent(),
                            keyPair.prime1(),
                            keyPair.prime2(),
                            keyPair.exponent1(),
                            keyPair.exponent2(),
                            keyPair.coefficient()
                    );

            privateKeyObject =
                    keyFactory.generatePrivate(privateKeySpec);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Unable to create RSA key objects",
                    e
            );
        }
        String certificate;

        try {
            X509Certificate x509Certificate =
                    CertificateGenerator.generate(
                            publicKeyObject,
                            privateKeyObject,
                            publicKeyCode
                    );
            certificate = Base64.getEncoder().encodeToString(x509Certificate.getEncoded());
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to generate X.509 certificate",
                    e
            );
        }
        return new UserKeyInfoResponse(
                true,
                publicKey,
                KeyAlgorithm.RSA.name(),
                publicKeyCode,
                2048,
                LocalDateTime.now(),
                privateKey,
                certificate
        );
    }

    public String changToPingPrivateKey(String key) {

        try {

            byte[] digest =
                    MessageDigest.getInstance("SHA-256")
                            .digest(
                                    key.getBytes(
                                            StandardCharsets.UTF_8
                                    )
                            );

            BigInteger number =
                    new BigInteger(1, digest);

            int code =
                    number
                            .mod(BigInteger.valueOf(1_000_000))
                            .intValue();

            return String.format("%06d", code);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Unable to generate key code",
                    e
            );
        }
    }

    @Transactional
    public void saveUserKey(
            Users user,
            String publicKey,
            String keyCode,
            String certificate
    ) {

        UserKeys userKeys =
                UserKeys.builder()
                        .user(user)
                        .keyAlgorithm(KeyAlgorithm.RSA)
                        .keySize(2048)
                        .publicKey(publicKey)
                        .keyCode(keyCode)
                        .certificate(certificate)
                        .createAt(LocalDateTime.now())
                        .build();
        userKeysRepository.save(userKeys);
    }

    public String encodePublicKey(
            BigInteger modulus,
            BigInteger publicExponent
    ) {

        try {

            org.bouncycastle.asn1.pkcs.RSAPublicKey rsaPublicKey =
                    new org.bouncycastle.asn1.pkcs.RSAPublicKey(
                            modulus,
                            publicExponent
                    );

            org.bouncycastle.asn1.x509.AlgorithmIdentifier algorithmIdentifier =
                    new org.bouncycastle.asn1.x509.AlgorithmIdentifier(
                            org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers.rsaEncryption,
                            org.bouncycastle.asn1.DERNull.INSTANCE
                    );
            org.bouncycastle.asn1.x509.SubjectPublicKeyInfo publicKeyInfo =
                    new org.bouncycastle.asn1.x509.SubjectPublicKeyInfo(
                            algorithmIdentifier,
                            rsaPublicKey
                    );
            return Base64.getEncoder()
                    .encodeToString(
                            publicKeyInfo.getEncoded()
                    );
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to encode RSA public key", e
            );
        }
    }
}