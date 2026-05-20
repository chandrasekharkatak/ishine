-- =============================================================================
-- UAT / prod: add project & client columns to travel requests
-- Correct table name: travel_desk  (NOT travel_desks)
-- Database (UAT): db_emp_backup_new
-- =============================================================================

USE db_emp_backup_new;

-- 1) Confirm the table exists (should return one row)
SELECT table_name
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name = 'travel_desk';

-- If the query above returns nothing, list similar tables:
-- SHOW TABLES LIKE '%travel%';

-- 2) Add columns (MySQL 8.0.12+ supports IF NOT EXISTS on ADD COLUMN)
-- If you get "Duplicate column name", those columns are already present — safe to ignore.

ALTER TABLE travel_desk
  ADD COLUMN IF NOT EXISTS project_id BIGINT NULL,
  ADD COLUMN IF NOT EXISTS project_name VARCHAR(500) NULL,
  ADD COLUMN IF NOT EXISTS client_id INT NULL,
  ADD COLUMN IF NOT EXISTS client_name VARCHAR(512) NULL;

-- 3) Verify
SHOW COLUMNS FROM travel_desk LIKE 'project%';
SHOW COLUMNS FROM travel_desk LIKE 'client%';
