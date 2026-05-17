-- Step 5 aspiration chips (Submit for review): configurable per department with global fallback.
-- dept_id = 0 means “default pool” when no rows exist for the employee’s HRMS department id.
-- Run on UAT emp_portal_db after deploy; safe to re-run with IF NOT EXISTS.

CREATE TABLE IF NOT EXISTS skillmatrix_aspiration_chip_master (
  chip_id     INT NOT NULL AUTO_INCREMENT,
  dept_id     BIGINT NOT NULL DEFAULT 0 COMMENT '0 = global default; else HRMS / job-role department id',
  chip_label  VARCHAR(200) NOT NULL,
  sort_order  INT NOT NULL DEFAULT 0,
  is_active   TINYINT(1) NOT NULL DEFAULT 1,
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (chip_id),
  UNIQUE KEY uk_sachm_dept_label (dept_id, chip_label),
  KEY idx_sachm_dept_active (dept_id, is_active, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed global defaults (matches Development prototype list; adjust per org). Safe if rows already exist.
INSERT IGNORE INTO skillmatrix_aspiration_chip_master (dept_id, chip_label, sort_order, is_active) VALUES
  (0, 'Solution architect', 10, 1),
  (0, 'Tech lead', 20, 1),
  (0, 'Full stack specialist', 30, 1),
  (0, 'Cloud-native engineer', 40, 1),
  (0, 'Engineering manager', 50, 1);
