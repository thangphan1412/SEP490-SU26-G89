-- Xoá 2 cột không còn dùng của contract_types (Default Validity, Category).
-- ddl-auto=update không tự xoá cột, nên chạy tay một lần trên SQL Server sau khi deploy code mới.
IF COL_LENGTH('contract_types', 'validity_days') IS NOT NULL
    ALTER TABLE contract_types DROP COLUMN validity_days;

IF COL_LENGTH('contract_types', 'category') IS NOT NULL
    ALTER TABLE contract_types DROP COLUMN category;
