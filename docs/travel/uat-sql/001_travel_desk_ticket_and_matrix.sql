-- Travel Desk: multi-line tickets + configurable approval matrix + admin fulfillment.
-- Legacy table travel_desk is NOT dropped; new flows use travel_desk_ticket*.
-- Run on UAT/prod when these tables do not exist yet.

-- ---------------------------------------------------------------------------
-- Ticket header (one submit = one ticket id, many lines)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS travel_desk_ticket (
  ticket_id BIGINT NOT NULL AUTO_INCREMENT,
  ticket_no VARCHAR(32) NULL COMMENT 'Public ref e.g. APM-TRV-20260521-0001',
  emp_id BIGINT NULL,
  full_name VARCHAR(512) NULL,
  email VARCHAR(512) NULL,
  department VARCHAR(512) NULL,
  designation VARCHAR(512) NULL,
  mobile_no VARCHAR(64) NULL,
  manager_emp_id BIGINT NULL,
  manager_name VARCHAR(512) NULL,
  manager_email VARCHAR(512) NULL,
  workflow_stage VARCHAR(64) NOT NULL COMMENT 'PENDING_LEVEL|PENDING_ADMIN|COMPLETED|REJECTED',
  approval_matrix_id BIGINT NULL,
  current_level_order INT NULL DEFAULT 1,
  current_assignee_emp_id BIGINT NULL,
  submitted_on DATETIME(3) NULL,
  is_active INT NOT NULL DEFAULT 1,
  admin_remarks VARCHAR(4000) NULL,
  completed_on DATETIME(3) NULL,
  admin_actor_emp_id BIGINT NULL,
  PRIMARY KEY (ticket_id),
  UNIQUE KEY uq_travel_ticket_no (ticket_no),
  KEY idx_travel_ticket_emp (emp_id),
  KEY idx_travel_ticket_stage (workflow_stage),
  KEY idx_travel_ticket_matrix (approval_matrix_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Ticket lines (multiple travel/hotel requests per ticket)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS travel_desk_ticket_line (
  line_id BIGINT NOT NULL AUTO_INCREMENT,
  ticket_id BIGINT NOT NULL,
  line_no INT NOT NULL,
  request_type VARCHAR(128) NULL COMMENT 'Travel|Hotel|...',
  travel_mode VARCHAR(128) NULL,
  travel_class VARCHAR(128) NULL,
  trip_type VARCHAR(32) NULL COMMENT 'ONE_WAY|ROUND|MULTI_CITY|HOTEL',
  travel_reason VARCHAR(256) NULL,
  purpose VARCHAR(4000) NULL,
  from_location VARCHAR(512) NULL,
  to_location VARCHAR(512) NULL,
  from_date DATETIME(3) NULL,
  to_date DATETIME(3) NULL,
  hotel_category VARCHAR(128) NULL,
  hotel_sub_category VARCHAR(256) NULL,
  city VARCHAR(256) NULL COMMENT 'Hotel stay city',
  project_id BIGINT NULL,
  project_name VARCHAR(500) NULL,
  client_id INT NULL,
  client_name VARCHAR(512) NULL,
  supporting_doc_ids VARCHAR(2000) NULL COMMENT 'Employee uploads at apply',
  line_status VARCHAR(64) NOT NULL COMMENT 'PENDING_APPROVAL|LEVEL_REJECTED|PENDING_ADMIN|FULFILLED|ADMIN_REJECTED',
  approver_remarks VARCHAR(2000) NULL,
  booking_reference VARCHAR(512) NULL COMMENT 'Admin: PNR / confirmation no',
  admin_proof_doc_ids VARCHAR(2000) NULL COMMENT 'Admin: ticket/voucher file ids',
  fulfilled_on DATETIME(3) NULL,
  PRIMARY KEY (line_id),
  KEY idx_travel_line_ticket (ticket_id),
  CONSTRAINT fk_travel_line_ticket FOREIGN KEY (ticket_id)
    REFERENCES travel_desk_ticket (ticket_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Audit
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS travel_desk_ticket_audit (
  audit_id BIGINT NOT NULL AUTO_INCREMENT,
  ticket_id BIGINT NULL,
  line_id BIGINT NULL,
  actor_emp_id BIGINT NULL,
  actor_email VARCHAR(512) NULL,
  action VARCHAR(120) NULL,
  remarks VARCHAR(4000) NULL,
  created_on DATETIME(3) NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (audit_id),
  KEY idx_travel_audit_ticket (ticket_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Public ticket number sequence (APM-TRV-YYYYMMDD-####)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS travel_desk_ticket_day_seq (
  day_key CHAR(8) NOT NULL,
  last_seq INT NOT NULL DEFAULT 0,
  PRIMARY KEY (day_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Approval matrix (mirror reimbursement_approval_matrix*)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS travel_approval_matrix (
  matrix_id BIGINT NOT NULL AUTO_INCREMENT,
  matrix_name VARCHAR(120) NOT NULL,
  is_active CHAR(1) NOT NULL DEFAULT 'Y',
  admin_dept_id BIGINT NULL COMMENT 'Travel desk / admin owning dept',
  admin_assignee_emp_id BIGINT NULL,
  created_by BIGINT NULL,
  created_by_name VARCHAR(200) NULL,
  created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by BIGINT NULL,
  updated_by_name VARCHAR(200) NULL,
  updated_on TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (matrix_id),
  KEY idx_travel_appr_matrix_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS travel_approval_matrix_app_dept (
  matrix_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL,
  PRIMARY KEY (matrix_id, dept_id),
  CONSTRAINT fk_travel_appr_app_dept_matrix FOREIGN KEY (matrix_id)
    REFERENCES travel_approval_matrix (matrix_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS travel_approval_matrix_app_role (
  matrix_id BIGINT NOT NULL,
  job_role_id BIGINT NOT NULL,
  PRIMARY KEY (matrix_id, job_role_id),
  CONSTRAINT fk_travel_appr_app_role_matrix FOREIGN KEY (matrix_id)
    REFERENCES travel_approval_matrix (matrix_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS travel_approval_matrix_level (
  level_id BIGINT NOT NULL AUTO_INCREMENT,
  matrix_id BIGINT NOT NULL,
  level_order INT NOT NULL,
  routing_mode VARCHAR(64) NOT NULL,
  specific_employee_id BIGINT NULL,
  PRIMARY KEY (level_id),
  UNIQUE KEY uq_travel_appr_matrix_level_order (matrix_id, level_order),
  CONSTRAINT fk_travel_appr_level_matrix FOREIGN KEY (matrix_id)
    REFERENCES travel_approval_matrix (matrix_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS travel_approval_matrix_level_dept (
  level_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL,
  PRIMARY KEY (level_id, dept_id),
  CONSTRAINT fk_travel_appr_lvl_dept_level FOREIGN KEY (level_id)
    REFERENCES travel_approval_matrix_level (level_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS travel_approval_matrix_level_role (
  level_id BIGINT NOT NULL,
  job_role_id BIGINT NOT NULL,
  PRIMARY KEY (level_id, job_role_id),
  CONSTRAINT fk_travel_appr_lvl_role_level FOREIGN KEY (level_id)
    REFERENCES travel_approval_matrix_level (level_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE travel_desk_ticket
  ADD CONSTRAINT fk_travel_ticket_matrix FOREIGN KEY (approval_matrix_id)
    REFERENCES travel_approval_matrix (matrix_id) ON DELETE SET NULL;
