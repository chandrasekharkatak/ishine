-- PO number snapshot on reimbursement claim (from project at submit time).

SET NAMES utf8mb4;

SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'reimbursement_ticket_claim'
    AND COLUMN_NAME = 'po_no'
);

SET @ddl := IF(
  @col_exists = 0,
  'ALTER TABLE reimbursement_ticket_claim ADD COLUMN po_no VARCHAR(512) NULL COMMENT ''Denormalized PO number(s) at submit time'' AFTER client_name',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
