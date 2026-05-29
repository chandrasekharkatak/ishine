-- Recurring expense, POC project, and reimbursement-only prospective clients.
-- Run on UAT/prod when upgrading reimbursement_ticket_claim.

CREATE TABLE IF NOT EXISTS reimbursement_client (
  reimbursement_client_id BIGINT NOT NULL AUTO_INCREMENT,
  client_name VARCHAR(512) NOT NULL,
  client_category VARCHAR(64) NOT NULL DEFAULT 'PROSPECTIVE_NEW_CLIENT',
  created_by_emp_id BIGINT NULL,
  created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_active TINYINT NOT NULL DEFAULT 1,
  PRIMARY KEY (reimbursement_client_id),
  UNIQUE KEY uk_reimbursement_client_name (client_name(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE reimbursement_ticket_claim
  ADD COLUMN recurring_expense TINYINT NOT NULL DEFAULT 0 COMMENT '1 = recurring expense',
  ADD COLUMN poc_project TINYINT NOT NULL DEFAULT 0 COMMENT '1 = POC-related manual project',
  ADD COLUMN reimbursement_client_id BIGINT NULL COMMENT 'FK reimbursement_client when not in clients master',
  ADD COLUMN client_category VARCHAR(64) NULL COMMENT 'MASTER or PROSPECTIVE_NEW_CLIENT';
