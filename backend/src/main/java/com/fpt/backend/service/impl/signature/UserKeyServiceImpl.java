package com.fpt.backend.service.impl.signature;

import com.fpt.backend.dto.response.signature.UserKeyInfoResponse;
import com.fpt.backend.entity.UserKeys;
import com.fpt.backend.entity.Users;
import com.fpt.backend.enums.KeyAlgorithm;
import com.fpt.backend.repository.signature.UserKeysRepository;
import com.fpt.backend.service.interfaces.signature.IUserKeyService;
import com.fpt.backend.util.CalculateRSA;
import com.fpt.backend.util.RSAKeyConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
@Service
@RequiredArgsConstructor
public class UserKeyServiceImpl
        implements IUserKeyService {

    private final UserKeysRepository userKeysRepository;

    private final CalculateRSA calculateRSA;

    private final PrivateKeyProtectionService privateKeyProtectionService;

    @Override
    @Transactional
    public UserKeyInfoResponse generateUserKey(Users user) {

        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException(
                    "User must be saved before generating key"
            );
        }

//        if (userKeysRepository.existsByUserId(
//                user.getId()
//        )) {
//
//            throw new IllegalStateException(
//                    "User already has RSA key"
//            );
//        }

        CalculateRSA.RSAKeyPair keyPair = calculateRSA.generateKeyPair();
        String publicKey = RSAKeyConverter.encode(
                        keyPair.modulus(),
                        keyPair.publicExponent());

       String publicKeyCodeStr= changToPingPrivateKey(publicKey);
        System.out.println("Public Key Code: " + publicKeyCodeStr);
        String privateKey = RSAKeyConverter.encode(
                        keyPair.modulus(),
                        keyPair.privateExponent()
                );

        String privateKeyCodeStr = changToPingPrivateKey(privateKey);
//        byte[] privateKeyBytes = privateKeyProtectionService.encrypt(privateKey, privateKeyCodeStr).getBytes();
        System.out.println("Private Key Code: " + privateKeyCodeStr);
//        UserKeys userKeys = UserKeys.builder()
//                        .user(user)
//                        .keyAlgorithm(KeyAlgorithm.RSA)
//                        .keySize(2048)
//                        .publicKey(publicKey)
//                        .keyCode(publicKeyCodeStr)
////                        .privateKey(privateKeyProtectionService.encrypt(privateKey))
//
//                        .createAt(LocalDateTime.now())
//                        .build();
//
//        UserKeys savedUserKeys = userKeysRepository.save(userKeys);
        return new UserKeyInfoResponse(
//                true,
//                savedUserKeys.getPublicKey(),
//                savedUserKeys.getKeyAlgorithm().name(),
//                savedUserKeys.getKeyCode(),
//                savedUserKeys.getKeySize(),
//                savedUserKeys.getCreateAt(),
//                privateKey
                true,
                publicKey,
                KeyAlgorithm.RSA.name(),
                publicKeyCodeStr,
                2048,
                LocalDateTime.now(),
                privateKey
        );
    }
//    public String changToPing(String key){
//        BigInteger number = new BigInteger(key, 16);
//        int publicKeyCode = number.mod(BigInteger.valueOf(1000000)).intValue();
//        String publicKeyCodeStr = String.format("%06d", publicKeyCode);
//        return publicKeyCodeStr;
//    }
    public String changToPingPrivateKey(String key) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(key.getBytes(StandardCharsets.UTF_8));

            BigInteger number = new BigInteger(1, digest);

            int code = number
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
            String keyCode
    ) {
        UserKeys userKeys = UserKeys.builder()
                .user(user)
                .keyAlgorithm(KeyAlgorithm.RSA)
                .keySize(2048)
                .publicKey(publicKey)
                .keyCode(keyCode)
                .createAt(LocalDateTime.now())
                .build();

        userKeysRepository.save(userKeys);
    }
}
