-- Baseline: multi-claim reimbursement tickets (employee → HOD → HR → finance).
-- Run on UAT/prod when reimbursement_ticket* tables do not exist yet.
-- Aligns with JPA entities: ReimbursementTicket, ReimbursementTicketClaim, ReimbursementTicketAuditLog.

CREATE TABLE IF NOT EXISTS reimbursement_ticket (
  ticket_id BIGINT NOT NULL AUTO_INCREMENT,
  ticket_no VARCHAR(32) NULL,
  emp_id BIGINT NULL,
  full_name VARCHAR(512) NULL,
  email VARCHAR(512) NULL,
  department VARCHAR(512) NULL,
  designation VARCHAR(512) NULL,
  mobile_no VARCHAR(64) NULL,
  hod_emp_id BIGINT NULL,
  hod_name VARCHAR(512) NULL,
  hod_email VARCHAR(512) NULL,
  workflow_stage VARCHAR(64) NOT NULL,
  submitted_on DATETIME(3) NULL,
  is_active INT NULL,
  finance_reject_reason VARCHAR(4000) NULL,
  paid_on DATETIME(3) NULL,
  PRIMARY KEY (ticket_id),
  UNIQUE KEY uq_reimbursement_ticket_no (ticket_no),
  KEY idx_reimb_ticket_emp (emp_id),
  KEY idx_reimb_ticket_stage (workflow_stage)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS reimbursement_ticket_claim (
  claim_id BIGINT NOT NULL AUTO_INCREMENT,
  ticket_id BIGINT NOT NULL,
  line_no INT NULL,
  expenditure_type VARCHAR(128) NULL,
  expenditure_type_description VARCHAR(255) NULL,
  amount DECIMAL(15, 2) NULL,
  travel_mode VARCHAR(128) NULL,
  distance BIGINT NULL,
  vehicle_type VARCHAR(128) NULL,
  food_allowance_type VARCHAR(256) NULL,
  date_of_food DATETIME(3) NULL,
  from_date DATETIME(3) NULL,
  to_date DATETIME(3) NULL,
  purpose VARCHAR(4000) NULL,
  project_id BIGINT NULL,
  project_name VARCHAR(500) NULL,
  client_id INT NULL,
  client_name VARCHAR(512) NULL,
  doc_ids VARCHAR(2000) NULL,
  claim_status VARCHAR(64) NOT NULL,
  hod_remarks VARCHAR(2000) NULL,
  hr_remarks VARCHAR(2000) NULL,
  PRIMARY KEY (claim_id),
  KEY idx_reimb_claim_ticket (ticket_id),
  CONSTRAINT fk_reimb_claim_ticket FOREIGN KEY (ticket_id) REFERENCES reimbursement_ticket (ticket_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS reimbursement_ticket_audit (
  audit_id BIGINT NOT NULL AUTO_INCREMENT,
  ticket_id BIGINT NULL,
  claim_id BIGINT NULL,
  actor_emp_id BIGINT NULL,
  actor_email VARCHAR(512) NULL,
  action VARCHAR(120) NULL,
  remarks VARCHAR(4000) NULL,
  created_on DATETIME(3) NULL,
  PRIMARY KEY (audit_id),
  KEY idx_reimb_audit_ticket (ticket_id),
  KEY idx_reimb_audit_claim (claim_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS reimbursement_ticket_day_seq (
  day_key CHAR(8) NOT NULL,
  last_seq INT NOT NULL DEFAULT 0,
  PRIMARY KEY (day_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
