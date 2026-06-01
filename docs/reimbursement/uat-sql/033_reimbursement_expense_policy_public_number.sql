-- Public expense policy numbers APM-RMPOL-YYYYMMDD-#### (same pattern as reimbursement ticket_no).

SET NAMES utf8mb4;

START TRANSACTION;

CREATE TABLE IF NOT EXISTS reimbursement_expense_policy_day_seq (
  day_key CHAR(8) NOT NULL,
  last_seq INT NOT NULL DEFAULT 0,
  PRIMARY KEY (day_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'reimbursement_expense_policy'
    AND COLUMN_NAME = 'policy_no'
);

SET @ddl := IF(
  @col_exists = 0,
  'ALTER TABLE reimbursement_expense_policy ADD COLUMN policy_no VARCHAR(32) NULL COMMENT ''Public id e.g. APM-RMPOL-20260531-0001'' AFTER expense_policy_id',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE reimbursement_expense_policy ep
JOIN (
  SELECT expense_policy_id,
         DATE_FORMAT(COALESCE(created_on, CURRENT_TIMESTAMP), '%Y%m%d') AS dk,
         ROW_NUMBER() OVER (
           PARTITION BY DATE_FORMAT(COALESCE(created_on, CURRENT_TIMESTAMP), '%Y%m%d')
           ORDER BY expense_policy_id) AS rn
  FROM reimbursement_expense_policy
) x ON ep.expense_policy_id = x.expense_policy_id
SET ep.policy_no = CONCAT('APM-RMPOL-', x.dk, '-', LPAD(x.rn, 4, '0'))
WHERE ep.policy_no IS NULL OR ep.policy_no = '';

INSERT INTO reimbursement_expense_policy_day_seq (day_key, last_seq)
SELECT SUBSTRING(policy_no, 11, 8) AS d,
       MAX(CAST(SUBSTRING_INDEX(policy_no, '-', -1) AS UNSIGNED)) AS mx
FROM reimbursement_expense_policy
WHERE policy_no REGEXP '^APM-RMPOL-[0-9]{8}-[0-9]{4}$'
GROUP BY SUBSTRING(policy_no, 11, 8)
ON DUPLICATE KEY UPDATE last_seq = GREATEST(reimbursement_expense_policy_day_seq.last_seq, VALUES(last_seq));

SET @idx_exists := (
  SELECT COUNT(*)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'reimbursement_expense_policy'
    AND INDEX_NAME = 'uq_reimbursement_expense_policy_no'
);

SET @ddl := IF(
  @idx_exists = 0,
  'CREATE UNIQUE INDEX uq_reimbursement_expense_policy_no ON reimbursement_expense_policy (policy_no)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

COMMIT;
