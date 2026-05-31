-- Expense policy tables only — creates new objects, does not alter any existing table.
-- Safe to re-run (CREATE TABLE IF NOT EXISTS).

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS reimbursement_expense_policy (
  expense_policy_id BIGINT NOT NULL AUTO_INCREMENT,
  policy_no VARCHAR(32) NULL COMMENT 'Public id e.g. APM-RMPOL-20260531-0001',
  created_by BIGINT NULL,
  created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by BIGINT NULL,
  updated_on TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (expense_policy_id),
  UNIQUE KEY uq_reimbursement_expense_policy_no (policy_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reimbursement_expense_policy_job_role (
  expense_policy_id BIGINT NOT NULL,
  job_role_id BIGINT NOT NULL,
  PRIMARY KEY (job_role_id),
  KEY idx_repjr_expense_policy (expense_policy_id),
  CONSTRAINT fk_repjr_expense_policy FOREIGN KEY (expense_policy_id)
    REFERENCES reimbursement_expense_policy (expense_policy_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reimbursement_expense_policy_day_seq (
  day_key CHAR(8) NOT NULL,
  last_seq INT NOT NULL DEFAULT 0,
  PRIMARY KEY (day_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reimbursement_role_policy (
  policy_id BIGINT NOT NULL AUTO_INCREMENT,
  expense_policy_id BIGINT NULL,
  job_role_id BIGINT NULL,
  policy_category VARCHAR(32) NOT NULL COMMENT 'AMOUNT_LIMIT | TRAVEL_MODE | TRAVEL_MODE_LIMIT | VEHICLE_TYPE | VEHICLE_RATE | FOOD_ALLOWANCE',
  item_name VARCHAR(255) NULL COMMENT 'Expenditure/travel/vehicle/food type name; required for eligibility rows',
  max_amount DECIMAL(15, 2) NULL COMMENT 'Per-day cap when policy_category = AMOUNT_LIMIT (total = max_amount × inclusive days from claim From/To)',
  is_allowed CHAR(1) NOT NULL DEFAULT 'Y',
  created_by BIGINT NULL,
  created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by BIGINT NULL,
  updated_on TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (policy_id),
  UNIQUE KEY uk_rmb_expense_policy_rule (expense_policy_id, policy_category, item_name),
  KEY idx_rmb_role_policy_job_role (job_role_id),
  KEY idx_rmb_role_policy_expense (expense_policy_id),
  CONSTRAINT fk_rmb_role_policy_expense FOREIGN KEY (expense_policy_id)
    REFERENCES reimbursement_expense_policy (expense_policy_id) ON DELETE CASCADE,
  CONSTRAINT chk_rmb_role_policy_allowed CHECK (is_allowed IN ('Y', 'N')),
  CONSTRAINT chk_rmb_role_policy_cat CHECK (
    policy_category IN (
      'AMOUNT_LIMIT',
      'TRAVEL_MODE',
      'TRAVEL_MODE_LIMIT',
      'VEHICLE_TYPE',
      'VEHICLE_RATE',
      'FOOD_ALLOWANCE'
    )
  )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
