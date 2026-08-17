package com.fpt.backend.service.impl.signature;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SignatureSchemaMigration {

    private final JdbcTemplate jdbcTemplate;

    @Order(10)
    @EventListener(ApplicationReadyEvent.class)
    public void allowLongRsaKeysAndSignatureValues() {
        jdbcTemplate.execute("""
            IF COL_LENGTH('dbo.user_keys', 'public_key') IS NOT NULL
                ALTER TABLE dbo.user_keys ALTER COLUMN public_key nvarchar(max) NULL;
            IF COL_LENGTH('dbo.user_keys', 'private_key') IS NOT NULL
                ALTER TABLE dbo.user_keys ALTER COLUMN private_key nvarchar(max) NULL;
            IF COL_LENGTH('dbo.signatures', 'signature_value') IS NULL
                ALTER TABLE dbo.signatures ADD signature_value nvarchar(max) NULL;
            """);
    }
}
