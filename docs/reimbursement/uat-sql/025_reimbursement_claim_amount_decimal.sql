-- Store reimbursement claim amounts with up to 2 decimal places (rupees).
-- Run on UAT/prod when upgrading existing reimbursement_ticket_claim tables.

ALTER TABLE reimbursement_ticket_claim
  MODIFY COLUMN amount DECIMAL(15, 2) NULL;
