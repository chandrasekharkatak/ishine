-- Group expense policy rules under expense_policy_id; map many job roles to one policy.
-- Run after 029–031. Safe to re-run when objects already exist.

SET NAMES utf8mb4;

START TRANSACTION;

CREATE TABLE IF NOT EXISTS reimbursement_expense_policy (
  expense_policy_id BIGINT NOT NULL AUTO_INCREMENT,
  created_by BIGINT NULL,
  created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by BIGINT NULL,
  updated_on TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (expense_policy_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reimbursement_expense_policy_job_role (
  expense_policy_id BIGINT NOT NULL,
  job_role_id BIGINT NOT NULL,
  PRIMARY KEY (job_role_id),
  KEY idx_repjr_expense_policy (expense_policy_id),
  CONSTRAINT fk_repjr_expense_policy FOREIGN KEY (expense_policy_id)
    REFERENCES reimbursement_expense_policy (expense_policy_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'reimbursement_role_policy'
    AND COLUMN_NAME = 'expense_policy_id'
);

SET @ddl := IF(
  @col_exists = 0,
  'ALTER TABLE reimbursement_role_policy ADD COLUMN expense_policy_id BIGINT NULL AFTER policy_id, ADD KEY idx_rmb_role_policy_expense (expense_policy_id)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

DROP TEMPORARY TABLE IF EXISTS tmp_rmb_legacy_sig;
CREATE TEMPORARY TABLE tmp_rmb_legacy_sig AS
SELECT
  p.job_role_id,
  GROUP_CONCAT(
    CONCAT(
      p.policy_category, '|',
      COALESCE(p.item_name, ''), '|',
      COALESCE(CAST(p.max_amount AS CHAR), ''), '|',
      p.is_allowed
    )
    ORDER BY p.policy_category, p.item_name
  ) AS rule_sig,
  MAX(p.created_by) AS created_by,
  MAX(p.updated_by) AS updated_by
FROM reimbursement_role_policy p
WHERE p.expense_policy_id IS NULL
GROUP BY p.job_role_id;

DROP TEMPORARY TABLE IF EXISTS tmp_rmb_sig_group;
CREATE TEMPORARY TABLE tmp_rmb_sig_group AS
SELECT
  rule_sig,
  MIN(job_role_id) AS sample_job_role_id,
  MAX(created_by) AS created_by,
  MAX(updated_by) AS updated_by
FROM tmp_rmb_legacy_sig
GROUP BY rule_sig;

INSERT INTO reimbursement_expense_policy (created_by, updated_by)
SELECT g.created_by, g.updated_by
FROM tmp_rmb_sig_group g;

DROP TEMPORARY TABLE IF EXISTS tmp_rmb_sig_ep;
CREATE TEMPORARY TABLE tmp_rmb_sig_ep AS
SELECT
  g.rule_sig,
  ep.expense_policy_id
FROM (
  SELECT rule_sig, ROW_NUMBER() OVER (ORDER BY sample_job_role_id) AS rn
  FROM tmp_rmb_sig_group
) g
INNER JOIN (
  SELECT expense_policy_id, ROW_NUMBER() OVER (ORDER BY expense_policy_id) AS rn
  FROM reimbursement_expense_policy
) ep ON ep.rn = g.rn;

INSERT INTO reimbursement_expense_policy_job_role (expense_policy_id, job_role_id)
SELECT m.expense_policy_id, s.job_role_id
FROM tmp_rmb_legacy_sig s
INNER JOIN tmp_rmb_sig_ep m ON m.rule_sig = s.rule_sig
ON DUPLICATE KEY UPDATE expense_policy_id = VALUES(expense_policy_id);

INSERT INTO reimbursement_role_policy (
  expense_policy_id, job_role_id, policy_category, item_name, max_amount, is_allowed, created_by, updated_by
)
SELECT
  m.expense_policy_id,
  NULL,
  p.policy_category,
  p.item_name,
  p.max_amount,
  p.is_allowed,
  p.created_by,
  p.updated_by
FROM reimbursement_role_policy p
INNER JOIN tmp_rmb_legacy_sig s ON s.job_role_id = p.job_role_id
INNER JOIN tmp_rmb_sig_ep m ON m.rule_sig = s.rule_sig
WHERE p.expense_policy_id IS NULL
GROUP BY
  m.expense_policy_id,
  p.policy_category,
  p.item_name,
  p.max_amount,
  p.is_allowed,
  p.created_by,
  p.updated_by;

DELETE FROM reimbursement_role_policy WHERE expense_policy_id IS NULL;

ALTER TABLE reimbursement_role_policy
  MODIFY COLUMN job_role_id BIGINT NULL;

SET @uk_old := (
  SELECT COUNT(*)
  FROM information_schema.TABLE_CONSTRAINTS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'reimbursement_role_policy'
    AND CONSTRAINT_NAME = 'uk_rmb_role_policy'
);

SET @ddl := IF(
  @uk_old > 0,
  'ALTER TABLE reimbursement_role_policy DROP INDEX uk_rmb_role_policy',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @uk_new := (
  SELECT COUNT(*)
  FROM information_schema.TABLE_CONSTRAINTS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'reimbursement_role_policy'
    AND CONSTRAINT_NAME = 'uk_rmb_expense_policy_rule'
);

SET @ddl := IF(
  @uk_new = 0,
  'ALTER TABLE reimbursement_role_policy ADD CONSTRAINT uk_rmb_expense_policy_rule UNIQUE (expense_policy_id, policy_category, item_name)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk := (
  SELECT COUNT(*)
  FROM information_schema.TABLE_CONSTRAINTS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'reimbursement_role_policy'
    AND CONSTRAINT_NAME = 'fk_rmb_role_policy_expense'
);

SET @ddl := IF(
  @fk = 0,
  'ALTER TABLE reimbursement_role_policy ADD CONSTRAINT fk_rmb_role_policy_expense FOREIGN KEY (expense_policy_id) REFERENCES reimbursement_expense_policy (expense_policy_id) ON DELETE CASCADE',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

COMMIT;
