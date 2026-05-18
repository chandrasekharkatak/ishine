-- Monthly reimbursement submission window (apply allowed only through Nth day of each calendar month).
CREATE TABLE IF NOT EXISTS reimbursement_submission_settings (
  settings_id BIGINT NOT NULL PRIMARY KEY,
  monthly_deadline_day INT NOT NULL DEFAULT 10,
  enabled CHAR(1) NOT NULL DEFAULT 'Y',
  updated_by BIGINT NULL,
  updated_on TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT chk_rmb_sub_deadline_day CHECK (monthly_deadline_day >= 1 AND monthly_deadline_day <= 31),
  CONSTRAINT chk_rmb_sub_enabled CHECK (enabled IN ('Y', 'N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO reimbursement_submission_settings (settings_id, monthly_deadline_day, enabled)
SELECT 1, 10, 'Y' FROM (SELECT 1) AS t
WHERE NOT EXISTS (SELECT 1 FROM reimbursement_submission_settings WHERE settings_id = 1);
