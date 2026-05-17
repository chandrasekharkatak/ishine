-- Two-level approval tracking for Skill Matrix submissions
-- Manager (L1) -> HOD (L2)

CREATE TABLE IF NOT EXISTS skillmatrix_assessment_approval_flow (
  submission_id VARCHAR(80) NOT NULL,
  dept_id BIGINT NOT NULL DEFAULT 0,
  revision_round INT NOT NULL DEFAULT 1,

  manager_id BIGINT NOT NULL DEFAULT 0,
  hod_id BIGINT NOT NULL DEFAULT 0,

  manager_approved VARCHAR(10) NOT NULL DEFAULT 'pending', -- pending | yes | no
  hod_approved VARCHAR(10) NOT NULL DEFAULT 'pending',     -- pending | yes | no
  final_status VARCHAR(10) NOT NULL DEFAULT 'pending',     -- pending | approved | rejected

  manager_comment TEXT NULL,
  hod_comment TEXT NULL,

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (submission_id)
);

