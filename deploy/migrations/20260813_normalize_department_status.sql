BEGIN TRANSACTION;

UPDATE departments
SET department_status = UPPER(LTRIM(RTRIM(department_status)))
WHERE department_status IS NOT NULL;

COMMIT TRANSACTION;
