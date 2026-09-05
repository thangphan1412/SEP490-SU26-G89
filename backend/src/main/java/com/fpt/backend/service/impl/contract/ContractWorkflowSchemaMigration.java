package com.fpt.backend.service.impl.contract;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Applies idempotent SQL Server schema fixes required by the Contract flow.
 * Hibernate ddl-auto=update does not reliably alter existing column definitions,
 * so these migrations also keep older developer databases compatible.
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

    @EventListener(ApplicationReadyEvent.class)
    public void useUnicodeForTimelineTaskTitle() {
        jdbcTemplate.execute("""
            IF EXISTS (
                SELECT 1
                FROM sys.columns
                WHERE object_id = OBJECT_ID('dbo.timeline_task')
                  AND name = 'title'
                  AND system_type_id = TYPE_ID('varchar')
            )
            ALTER TABLE dbo.timeline_task
                ALTER COLUMN title nvarchar(255) NULL
            """);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void useUnicodeForContractTitle() {
        jdbcTemplate.execute("""
            IF EXISTS (
                SELECT 1
                FROM sys.columns
                WHERE object_id = OBJECT_ID('dbo.contracts')
                  AND name = 'contract_title'
                  AND system_type_id = TYPE_ID('varchar')
            )
            ALTER TABLE dbo.contracts
                ALTER COLUMN contract_title nvarchar(255) NULL
            """);
    }
}
