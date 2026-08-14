package com.fpt.backend.service.impl.contract;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Keeps the runtime contract workflow independent from Contract Type steps.
 * SQL Server does not always relax NOT NULL columns through ddl-auto=update,
 * so this small idempotent migration is required for existing databases.
 */
@Component
@RequiredArgsConstructor
public class ContractWorkflowSchemaMigration {

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void allowContractOwnedWorkflowSteps() {
        jdbcTemplate.execute("""
            IF EXISTS (
                SELECT 1
                FROM sys.columns
                WHERE object_id = OBJECT_ID('dbo.contract_workflow_step_instances')
                  AND name = 'step_definition_id'
                  AND is_nullable = 0
            )
            ALTER TABLE dbo.contract_workflow_step_instances
                ALTER COLUMN step_definition_id uniqueidentifier NULL
            """);
    }
}
