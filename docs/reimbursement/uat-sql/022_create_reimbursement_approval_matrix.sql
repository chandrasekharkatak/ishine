-- Reimbursement approval matrix configuration (admin-defined approval chains).
-- Run on UAT/prod when tables do not exist yet.

CREATE TABLE IF NOT EXISTS reimbursement_approval_matrix (
  matrix_id BIGINT NOT NULL AUTO_INCREMENT,
  matrix_name VARCHAR(120) NOT NULL,
  is_active CHAR(1) NOT NULL DEFAULT 'Y',
  finance_dept_id BIGINT NULL,
  finance_assignee_emp_id BIGINT NULL,
  created_by BIGINT NULL,
  created_by_name VARCHAR(200) NULL,
  created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by BIGINT NULL,
  updated_by_name VARCHAR(200) NULL,
  updated_on TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (matrix_id),
  KEY idx_rmb_appr_matrix_active (is_active),
  KEY idx_rmb_appr_matrix_name (matrix_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reimbursement_approval_matrix_app_dept (
  matrix_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL,
  PRIMARY KEY (matrix_id, dept_id),
  CONSTRAINT fk_rmb_appr_app_dept_matrix FOREIGN KEY (matrix_id)
    REFERENCES reimbursement_approval_matrix (matrix_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reimbursement_approval_matrix_app_role (
  matrix_id BIGINT NOT NULL,
  job_role_id BIGINT NOT NULL,
  PRIMARY KEY (matrix_id, job_role_id),
  CONSTRAINT fk_rmb_appr_app_role_matrix FOREIGN KEY (matrix_id)
    REFERENCES reimbursement_approval_matrix (matrix_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reimbursement_approval_matrix_level (
  level_id BIGINT NOT NULL AUTO_INCREMENT,
  matrix_id BIGINT NOT NULL,
  level_order INT NOT NULL,
  routing_mode VARCHAR(64) NOT NULL,
  specific_employee_id BIGINT NULL,
  PRIMARY KEY (level_id),
  UNIQUE KEY uq_rmb_appr_matrix_level_order (matrix_id, level_order),
  CONSTRAINT fk_rmb_appr_level_matrix FOREIGN KEY (matrix_id)
    REFERENCES reimbursement_approval_matrix (matrix_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reimbursement_approval_matrix_level_dept (
  level_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL,
  PRIMARY KEY (level_id, dept_id),
  CONSTRAINT fk_rmb_appr_lvl_dept_level FOREIGN KEY (level_id)
    REFERENCES reimbursement_approval_matrix_level (level_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reimbursement_approval_matrix_level_role (
  level_id BIGINT NOT NULL,
  job_role_id BIGINT NOT NULL,
  PRIMARY KEY (level_id, job_role_id),
  CONSTRAINT fk_rmb_appr_lvl_role_level FOREIGN KEY (level_id)
    REFERENCES reimbursement_approval_matrix_level (level_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Multiple named approvers per level (optional).
CREATE TABLE IF NOT EXISTS reimbursement_approval_matrix_level_emp (
  level_id BIGINT NOT NULL,
  emp_id BIGINT NOT NULL,
  PRIMARY KEY (level_id, emp_id),
  CONSTRAINT fk_rmb_appr_lvl_emp_level FOREIGN KEY (level_id)
    REFERENCES reimbursement_approval_matrix_level (level_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
