-- Business justification, HOD pre-approval flag, pre-approval date, and pre-approval document ids.
-- Run on UAT/prod when upgrading reimbursement_ticket_claim.

-- business_justification uses TEXT (not VARCHAR) to stay under InnoDB max row size (~65535)
-- with existing purpose VARCHAR(4000), doc_ids, remarks, client_name, etc.
ALTER TABLE reimbursement_ticket_claim
  ADD COLUMN business_justification TEXT NULL COMMENT 'Mandatory business justification for the claim',
  ADD COLUMN hod_approval TINYINT NOT NULL DEFAULT 0 COMMENT '1 = HOD pre-approval obtained',
  ADD COLUMN pre_approval_date DATE NULL COMMENT 'Date on HOD pre-approval email',
  ADD COLUMN pre_approval_doc_ids VARCHAR(2000) NULL COMMENT 'Comma-separated document ids for pre-approval emails';
