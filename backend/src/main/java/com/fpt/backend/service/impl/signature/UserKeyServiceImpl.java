package com.fpt.backend.service.impl.signature;

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

import java.time.LocalDateTime;
@Service
@RequiredArgsConstructor
public class UserKeyServiceImpl
        implements IUserKeyService {

    private final UserKeysRepository userKeysRepository;

    private final CalculateRSA calculateRSA;

    @Override
    @Transactional
    public UserKeys generateUserKey(Users user) {

        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException(
                    "User must be saved before generating key"
            );
        }

        if (userKeysRepository.existsByUserId(
                user.getId()
        )) {

            throw new IllegalStateException(
                    "User already has RSA key"
            );
        }

        // Generate RSA
        CalculateRSA.RSAKeyPair keyPair =
                calculateRSA.generateKeyPair();

        // Public Key = (n, e)
        String publicKey =
                RSAKeyConverter.encode(
                        keyPair.modulus(),
                        keyPair.publicExponent()
                );

        // Private Key = (n, d)
        String privateKey =
                RSAKeyConverter.encode(
                        keyPair.modulus(),
                        keyPair.privateExponent()
                );

        UserKeys userKeys =
                UserKeys.builder()
                        .user(user)
                        .keyAlgorithm(KeyAlgorithm.RSA)
                        .keySize(2048)
                        .publicKey(publicKey)
                        .privateKey(privateKey)
                        .createAt(LocalDateTime.now())
                        .build();

        return userKeysRepository.save(userKeys);
    }
}
