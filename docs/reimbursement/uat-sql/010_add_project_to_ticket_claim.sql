-- Add project columns to reimbursement_ticket_claim (upgrade path).
-- Skip if you used 001_create_reimbursement_ticket_tables.sql (project columns already included).
-- Requires reimbursement_ticket_claim to exist.

ALTER TABLE reimbursement_ticket_claim
  ADD COLUMN project_id BIGINT NULL COMMENT 'Logical FK to projects.project_id',
  ADD COLUMN project_name VARCHAR(500) NULL COMMENT 'Denormalized display name at submit time';
