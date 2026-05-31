-- Job-role based reimbursement limits and eligibility (Travel Mode, Vehicle Type, Food Allowance, amount caps).
CREATE TABLE IF NOT EXISTS reimbursement_role_policy (
  policy_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  job_role_id BIGINT NOT NULL,
  policy_category VARCHAR(32) NOT NULL COMMENT 'AMOUNT_LIMIT | TRAVEL_MODE | VEHICLE_TYPE | FOOD_ALLOWANCE',
  item_name VARCHAR(255) NULL COMMENT 'Expenditure/travel/vehicle/food type name; required for eligibility rows',
  max_amount DECIMAL(15, 2) NULL COMMENT 'Per-day cap when policy_category = AMOUNT_LIMIT (total = max_amount × inclusive days from claim From/To)',
  is_allowed CHAR(1) NOT NULL DEFAULT 'Y',
  created_by BIGINT NULL,
  created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by BIGINT NULL,
  updated_on TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_rmb_role_policy UNIQUE (job_role_id, policy_category, item_name),
  CONSTRAINT chk_rmb_role_policy_cat CHECK (policy_category IN ('AMOUNT_LIMIT', 'TRAVEL_MODE', 'VEHICLE_TYPE', 'FOOD_ALLOWANCE')),
  CONSTRAINT chk_rmb_role_policy_allowed CHECK (is_allowed IN ('Y', 'N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_rmb_role_policy_job_role ON reimbursement_role_policy (job_role_id);
