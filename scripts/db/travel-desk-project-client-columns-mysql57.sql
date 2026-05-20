-- =============================================================================
-- Same as travel-desk-project-client-columns.sql but for MySQL 5.7 / MariaDB
-- (no ADD COLUMN IF NOT EXISTS). Run each block once; skip if column already exists.
-- Table: travel_desk  (NOT travel_desks)
-- =============================================================================

USE db_emp_backup_new;

SELECT table_name
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name = 'travel_desk';

ALTER TABLE travel_desk ADD COLUMN project_id BIGINT NULL;
ALTER TABLE travel_desk ADD COLUMN project_name VARCHAR(500) NULL;
ALTER TABLE travel_desk ADD COLUMN client_id INT NULL;
ALTER TABLE travel_desk ADD COLUMN client_name VARCHAR(512) NULL;
