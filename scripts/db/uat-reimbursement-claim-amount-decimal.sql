-- Reimbursement claim amounts: support decimals (run once on UAT/prod).
-- See also: docs/reimbursement/uat-sql/025_reimbursement_claim_amount_decimal.sql

ALTER TABLE reimbursement_ticket_claim
  MODIFY COLUMN amount DECIMAL(15, 2) NULL;
