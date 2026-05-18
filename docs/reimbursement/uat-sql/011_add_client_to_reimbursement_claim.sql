-- Client snapshot on reimbursement claim (from project or BD "Others" flow).
-- Run on UAT/prod when upgrading existing reimbursement_ticket_claim tables.

ALTER TABLE reimbursement_ticket_claim
  ADD COLUMN client_id INT NULL COMMENT 'FK to clients.client_id (resolved from project or chosen for Others)',
  ADD COLUMN client_name VARCHAR(512) NULL COMMENT 'Denormalized client name at submit time',
  ADD COLUMN expenditure_type_description VARCHAR(255) NULL COMMENT 'Snapshot from expenditure_type.description at submit time';
