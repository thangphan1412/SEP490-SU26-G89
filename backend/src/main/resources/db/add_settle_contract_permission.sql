-- Thêm quyền SETTLE_CONTRACTS (thanh lý hợp đồng) vào catalog.
-- Chạy một lần trên SQL Server. Cột contract_* mới của bảng contracts sẽ được Hibernate
-- tự thêm (ddl-auto=update).
IF NOT EXISTS (SELECT 1 FROM permission_action_catalog WHERE action_code = 'SETTLE_CONTRACTS')
BEGIN
    INSERT INTO permission_action_catalog (id, action_code, action_name, resource_code, action_description, display_order)
    SELECT NEWID(),
           'SETTLE_CONTRACTS',
           N'Settle contracts',
           resource_code,
           N'Settle (liquidate) contracts that were signed by all parties',
           COALESCE(MAX(display_order), 0) + 1
    FROM permission_action_catalog
    WHERE action_code = 'CANCEL_CONTRACTS'
    GROUP BY resource_code;
END;

-- Hợp đồng cũ đã ký đủ đang ở SIGNED sẽ được scheduler tự chuyển sang
-- PENDING_EFFECTIVE/ACTIVE/OVERDUE ở lần chạy đầu tiên, không cần migrate tay.
