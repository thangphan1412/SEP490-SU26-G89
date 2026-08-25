package com.fpt.backend.service.impl.signature;


import com.fpt.backend.repository.signature.UserKeysRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserKeySecurityMigration {

    private final UserKeysRepository userKeysRepository;
    private final PrivateKeyProtectionService protectionService;

    @Order(20)
    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void encryptLegacyPrivateKeys() {
        userKeysRepository.findAll().stream()
                .filter(key -> key.getPrivateKey() != null)
                .filter(key -> !protectionService.isProtected(key.getPrivateKey()))
                .forEach(key -> key.setPrivateKey(protectionService.encrypt(key.getPrivateKey())));
    }
}
