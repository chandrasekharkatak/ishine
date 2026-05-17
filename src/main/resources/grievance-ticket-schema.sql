CREATE TABLE IF NOT EXISTS grievance_ticket (
  ticket_id BIGINT NOT NULL AUTO_INCREMENT,
  ticket_number VARCHAR(30) UNIQUE,
  created_by_emp_id BIGINT NOT NULL,
  created_by_name VARCHAR(200),
  created_by_employee_id BIGINT,
  created_by_department VARCHAR(200),
  created_by_phone VARCHAR(30),
  created_by_project_name VARCHAR(500),
  created_by_client_name VARCHAR(300),
  created_by_reporting_manager VARCHAR(200),
  subject VARCHAR(300) NOT NULL,
  category VARCHAR(150),
  sub_category VARCHAR(200),
  ticket_feature VARCHAR(200),
  issue_scenario VARCHAR(500),
  description VARCHAR(4000) NOT NULL,
  priority VARCHAR(30),
  status VARCHAR(40),
  assigned_to_emp_id BIGINT,
  assigned_to_name VARCHAR(200),
  resolution_remarks VARCHAR(4000),
  resolution_category VARCHAR(150),
  action_taken VARCHAR(4000),
  resolved_by VARCHAR(200),
  resolution_date DATETIME,
  resolution_doc_name VARCHAR(255),
  resolution_doc_path VARCHAR(1000),
  feedback_rating INT,
  feedback_comments VARCHAR(2000),
  feedback_on DATETIME,
  proof_file_name VARCHAR(255),
  proof_file_path VARCHAR(1000),
  created_on DATETIME,
  updated_on DATETIME,
  updated_by BIGINT,
  is_active INT DEFAULT 1,
  closed_by_creator TINYINT(1) DEFAULT 0,
  PRIMARY KEY (ticket_id)
);

ALTER TABLE grievance_ticket
  ADD COLUMN IF NOT EXISTS ticket_number VARCHAR(30) UNIQUE;

ALTER TABLE grievance_ticket
  ADD COLUMN IF NOT EXISTS category VARCHAR(150);

ALTER TABLE grievance_ticket
  ADD COLUMN IF NOT EXISTS sub_category VARCHAR(200);

ALTER TABLE grievance_ticket
  ADD COLUMN IF NOT EXISTS ticket_feature VARCHAR(200);

ALTER TABLE grievance_ticket
  ADD COLUMN IF NOT EXISTS issue_scenario VARCHAR(500);

ALTER TABLE grievance_ticket
  ADD COLUMN IF NOT EXISTS created_by_employee_id BIGINT,
  ADD COLUMN IF NOT EXISTS created_by_department VARCHAR(200),
  ADD COLUMN IF NOT EXISTS resolution_category VARCHAR(150),
  ADD COLUMN IF NOT EXISTS action_taken VARCHAR(4000),
  ADD COLUMN IF NOT EXISTS resolved_by VARCHAR(200),
  ADD COLUMN IF NOT EXISTS resolution_date DATETIME,
  ADD COLUMN IF NOT EXISTS resolution_doc_name VARCHAR(255),
  ADD COLUMN IF NOT EXISTS resolution_doc_path VARCHAR(1000),
  ADD COLUMN IF NOT EXISTS feedback_rating INT,
  ADD COLUMN IF NOT EXISTS feedback_comments VARCHAR(2000),
  ADD COLUMN IF NOT EXISTS feedback_on DATETIME;

ALTER TABLE grievance_ticket
  ADD COLUMN IF NOT EXISTS closed_by_creator TINYINT(1) DEFAULT 0;

ALTER TABLE grievance_ticket
  ADD COLUMN IF NOT EXISTS created_by_phone VARCHAR(30),
  ADD COLUMN IF NOT EXISTS created_by_project_name VARCHAR(500),
  ADD COLUMN IF NOT EXISTS created_by_client_name VARCHAR(300),
  ADD COLUMN IF NOT EXISTS created_by_reporting_manager VARCHAR(200);
