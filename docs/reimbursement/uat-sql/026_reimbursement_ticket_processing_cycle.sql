-- Payroll / processing cycle for reimbursement tickets (yyyy-MM).
-- Submissions after the monthly deadline day are stamped with the next calendar month.

ALTER TABLE reimbursement_ticket
  ADD COLUMN processing_cycle_year_month VARCHAR(7) NULL COMMENT 'yyyy-MM processing cycle' AFTER submitted_on;
