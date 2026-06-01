-- Team meal claims: number of members sharing the food allowance.

SET NAMES utf8mb4;

SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'reimbursement_ticket_claim'
    AND COLUMN_NAME = 'team_member_count'
);

SET @ddl := IF(
  @col_exists = 0,
  'ALTER TABLE reimbursement_ticket_claim ADD COLUMN team_member_count INT NULL COMMENT ''Team meal: headcount for per-member daily food cap'' AFTER food_allowance_type',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
