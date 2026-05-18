-- Skill Matrix (Submit for Review) - UAT DDL
-- Target: MySQL 8.x
-- Creates all skillmatrix_* tables + relations and adds provenance columns to skills_master.

SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------------
-- skills_master provenance columns (safe add via information_schema)
-- ------------------------------------------------------------------
SET @db := DATABASE();

SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'skills_master' AND COLUMN_NAME = 'created_via_skillmatrix_submit'
    ),
    'SELECT 1',
    'ALTER TABLE skills_master ADD COLUMN created_via_skillmatrix_submit BOOLEAN NOT NULL DEFAULT FALSE'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'skills_master' AND COLUMN_NAME = 'created_by_emp_id'
    ),
    'SELECT 1',
    'ALTER TABLE skills_master ADD COLUMN created_by_emp_id BIGINT NULL'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'skills_master' AND COLUMN_NAME = 'created_for_dept_id'
    ),
    'SELECT 1',
    'ALTER TABLE skills_master ADD COLUMN created_for_dept_id BIGINT NULL'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'skills_master' AND INDEX_NAME = 'idx_sm_skill_created_via_submit'
    ),
    'SELECT 1',
    'CREATE INDEX idx_sm_skill_created_via_submit ON skills_master(created_via_skillmatrix_submit)'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'skills_master' AND INDEX_NAME = 'idx_sm_skill_created_by_emp'
    ),
    'SELECT 1',
    'CREATE INDEX idx_sm_skill_created_by_emp ON skills_master(created_by_emp_id)'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------------
-- Step 1: submission header
-- ------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS skillmatrix_assessment_submission (
  submission_id              VARCHAR(30)  PRIMARY KEY,
  employee_id                BIGINT       NOT NULL,
  employee_name              VARCHAR(100) NOT NULL,
  dept_id                    BIGINT       NOT NULL,
  dept_name                  VARCHAR(100) NOT NULL,
  designation                VARCHAR(100) NOT NULL,
  reporting_manager_id       BIGINT       NOT NULL,
  reporting_manager_name     VARCHAR(100) NOT NULL,
  experience_in_role         VARCHAR(20),
  assessment_cycle           VARCHAR(50)  NOT NULL,
  cycle_year                 SMALLINT     NOT NULL,
  submission_deadline        DATE,
  declaration_accepted       BOOLEAN      NOT NULL DEFAULT FALSE,
  declaration_accepted_at    DATETIME,
  status                     VARCHAR(30)  NOT NULL DEFAULT 'draft',
  submitted_at               DATETIME,
  approved_at                DATETIME,
  created_at                 DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at                 DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uq_sm_submission_employee_cycle UNIQUE (employee_id, assessment_cycle)
);

-- ------------------------------------------------------------------
-- Steps 2+3: per-skill rating/evidence
-- ------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS skillmatrix_assessment_skill_rating (
  id                         BIGINT PRIMARY KEY AUTO_INCREMENT,
  submission_id              VARCHAR(30) NOT NULL,
  skill_id                   INT         NOT NULL,
  skill_name                 VARCHAR(150) NOT NULL,
  dept_id                    BIGINT      NOT NULL,
  is_required                BOOLEAN     NOT NULL,
  self_rating                TINYINT     NOT NULL,
  years_experience           VARCHAR(20),
  last_used                  VARCHAR(30),
  usage_frequency            VARCHAR(20),
  what_can_you_do            TEXT        NOT NULL,
  used_in_project            BOOLEAN     NOT NULL DEFAULT FALSE,
  github_portfolio_url       VARCHAR(500),
  colleague_endorser         VARCHAR(200),
  knowledge_session_note     TEXT,
  manager_rating             TINYINT,
  manager_decision           VARCHAR(20),
  manager_comment            TEXT,
  manager_reviewed_at        DATETIME,
  final_rating               TINYINT,
  created_at                 DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at                 DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_sm_skill_rating_submission
    FOREIGN KEY (submission_id) REFERENCES skillmatrix_assessment_submission(submission_id) ON DELETE CASCADE,
  CONSTRAINT uq_sm_skill_per_submission UNIQUE (submission_id, skill_id)
);

-- ------------------------------------------------------------------
-- Step 3: selected subskills
-- ------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS skillmatrix_assessment_subskill_selection (
  id                         BIGINT PRIMARY KEY AUTO_INCREMENT,
  skill_rating_id            BIGINT      NOT NULL,
  submission_id              VARCHAR(30) NOT NULL,
  skill_id                   INT         NOT NULL,
  subskill_id                INT         NOT NULL,
  subskill_name              VARCHAR(200) NOT NULL,
  is_confident               BOOLEAN     NOT NULL DEFAULT TRUE,
  created_at                 DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_sm_subskill_rating
    FOREIGN KEY (skill_rating_id) REFERENCES skillmatrix_assessment_skill_rating(id) ON DELETE CASCADE,
  CONSTRAINT uq_sm_subskill_per_rating UNIQUE (skill_rating_id, subskill_id)
);

-- ------------------------------------------------------------------
-- Step 3: certification
-- ------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS skillmatrix_assessment_certification (
  id                         BIGINT PRIMARY KEY AUTO_INCREMENT,
  skill_rating_id            BIGINT      NOT NULL,
  submission_id              VARCHAR(30) NOT NULL,
  skill_id                   INT         NOT NULL,
  cert_name                  VARCHAR(200) NOT NULL,
  issuing_body               VARCHAR(200) NOT NULL,
  date_obtained              DATE         NOT NULL,
  expiry_type                VARCHAR(30),
  expiry_date                DATE,
  credential_id              VARCHAR(200),
  credential_url             VARCHAR(500),
  file_uploaded              BOOLEAN      NOT NULL DEFAULT FALSE,
  file_reference_key         VARCHAR(500),
  original_filename          VARCHAR(255),
  file_size_bytes            INT,
  file_mime_type             VARCHAR(50),
  uploaded_at                DATETIME,
  created_at                 DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at                 DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_sm_cert_skill_rating
    FOREIGN KEY (skill_rating_id) REFERENCES skillmatrix_assessment_skill_rating(id) ON DELETE CASCADE
);

-- ------------------------------------------------------------------
-- Step 3: training
-- ------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS skillmatrix_assessment_training (
  id                         BIGINT PRIMARY KEY AUTO_INCREMENT,
  skill_rating_id            BIGINT      NOT NULL,
  submission_id              VARCHAR(30) NOT NULL,
  skill_id                   INT         NOT NULL,
  course_name                VARCHAR(300) NOT NULL,
  platform_institute         VARCHAR(200),
  completion_year            SMALLINT,
  created_at                 DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_sm_training_skill_rating
    FOREIGN KEY (skill_rating_id) REFERENCES skillmatrix_assessment_skill_rating(id) ON DELETE CASCADE
);

-- ------------------------------------------------------------------
-- Step 4: project history
-- ------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS skillmatrix_assessment_project (
  id                         BIGINT PRIMARY KEY AUTO_INCREMENT,
  submission_id              VARCHAR(30) NOT NULL,
  project_source             VARCHAR(20) NOT NULL DEFAULT 'hrms',
  hrms_project_id            INT,
  project_name               VARCHAR(200) NOT NULL,
  client_or_type             VARCHAR(100),
  project_status             VARCHAR(20),
  start_date                 DATE,
  end_date                   DATE,
  duration_text              VARCHAR(50),
  employee_role              VARCHAR(150),
  allocation_pct             SMALLINT,
  contribution_summary       TEXT        NOT NULL,
  is_included                BOOLEAN     NOT NULL DEFAULT TRUE,
  domain_specific            TINYINT(1)  NOT NULL DEFAULT 0,
  skill_domain_id            INT         NULL,
  skill_subdomain_id         INT         NULL,
  skill_domain_feature_id    INT         NULL,
  created_at                 DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at                 DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_sm_project_submission
    FOREIGN KEY (submission_id) REFERENCES skillmatrix_assessment_submission(submission_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS skillmatrix_assessment_project_skill (
  id                         BIGINT PRIMARY KEY AUTO_INCREMENT,
  assessment_project_id      BIGINT      NOT NULL,
  submission_id              VARCHAR(30) NOT NULL,
  skill_id                   INT         NOT NULL,
  skill_name                 VARCHAR(150) NOT NULL,
  level_used                 TINYINT     NOT NULL,
  specific_contribution      TEXT        NOT NULL,
  created_at                 DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_sm_project_skill_project
    FOREIGN KEY (assessment_project_id) REFERENCES skillmatrix_assessment_project(id) ON DELETE CASCADE
);

-- ------------------------------------------------------------------
-- Step 5: aspirations
-- ------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS skillmatrix_assessment_aspiration (
  id                         BIGINT PRIMARY KEY AUTO_INCREMENT,
  submission_id              VARCHAR(30) NOT NULL,
  target_role_2yr            VARCHAR(200),
  target_departments         TEXT,
  preferred_project_type     TEXT,
  pref_online_self_paced     BOOLEAN DEFAULT FALSE,
  pref_classroom_workshop    BOOLEAN DEFAULT FALSE,
  pref_project_based         BOOLEAN DEFAULT FALSE,
  pref_pair_mentoring        BOOLEAN DEFAULT FALSE,
  pref_certifications        BOOLEAN DEFAULT FALSE,
  pref_conferences           BOOLEAN DEFAULT FALSE,
  other_skills_to_learn      TEXT,
  message_to_manager         TEXT,
  created_at                 DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at                 DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_sm_asp_submission
    FOREIGN KEY (submission_id) REFERENCES skillmatrix_assessment_submission(submission_id) ON DELETE CASCADE,
  CONSTRAINT uq_sm_aspiration_per_submission UNIQUE (submission_id)
);

CREATE TABLE IF NOT EXISTS skillmatrix_assessment_aspiration_skill (
  id                         BIGINT PRIMARY KEY AUTO_INCREMENT,
  submission_id              VARCHAR(30) NOT NULL,
  aspiration_id              BIGINT      NOT NULL,
  skill_id                   INT,
  skill_name                 VARCHAR(150) NOT NULL,
  is_custom                  BOOLEAN NOT NULL DEFAULT FALSE,
  created_at                 DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_sm_asp_skill_header
    FOREIGN KEY (aspiration_id) REFERENCES skillmatrix_assessment_aspiration(id) ON DELETE CASCADE,
  CONSTRAINT uq_sm_aspiration_skill UNIQUE (submission_id, skill_name)
);

-- Step 5 chip master (optional; seed via 003_create_aspiration_chip_master.sql on UAT).
CREATE TABLE IF NOT EXISTS skillmatrix_aspiration_chip_master (
  chip_id     INT NOT NULL AUTO_INCREMENT,
  dept_id     BIGINT NOT NULL DEFAULT 0,
  chip_label  VARCHAR(200) NOT NULL,
  sort_order  INT NOT NULL DEFAULT 0,
  is_active   TINYINT(1) NOT NULL DEFAULT 1,
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (chip_id),
  UNIQUE KEY uk_sachm_dept_label (dept_id, chip_label),
  KEY idx_sachm_dept_active (dept_id, is_active, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------------
-- Step 6: approval workflow
-- ------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS skillmatrix_assessment_approval (
  id                         BIGINT PRIMARY KEY AUTO_INCREMENT,
  submission_id              VARCHAR(30) NOT NULL,
  manager_id                 BIGINT      NOT NULL,
  manager_name               VARCHAR(100) NOT NULL,
  notified_at                DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  review_deadline            DATE,
  review_started_at          DATETIME,
  review_completed_at        DATETIME,
  overall_decision           VARCHAR(20),
  overall_comment            TEXT,
  revision_round             SMALLINT NOT NULL DEFAULT 1,
  employee_revised_at        DATETIME,
  created_at                 DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at                 DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_sm_approval_submission
    FOREIGN KEY (submission_id) REFERENCES skillmatrix_assessment_submission(submission_id) ON DELETE CASCADE,
  CONSTRAINT uq_sm_approval_round UNIQUE (submission_id, revision_round)
);

CREATE TABLE IF NOT EXISTS skillmatrix_assessment_approval_skill (
  id                         BIGINT PRIMARY KEY AUTO_INCREMENT,
  approval_id                BIGINT      NOT NULL,
  skill_rating_id            BIGINT      NOT NULL,
  submission_id              VARCHAR(30) NOT NULL,
  skill_id                   INT         NOT NULL,
  skill_name                 VARCHAR(150) NOT NULL,
  employee_rating            TINYINT     NOT NULL,
  decision                   VARCHAR(20) NOT NULL,
  manager_rating             TINYINT,
  manager_comment            TEXT,
  decided_at                 DATETIME,
  employee_response          TEXT,
  employee_responded_at      DATETIME,
  created_at                 DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at                 DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_sm_approval_skill_header
    FOREIGN KEY (approval_id) REFERENCES skillmatrix_assessment_approval(id) ON DELETE CASCADE,
  CONSTRAINT fk_sm_approval_skill_rating
    FOREIGN KEY (skill_rating_id) REFERENCES skillmatrix_assessment_skill_rating(id) ON DELETE CASCADE,
  CONSTRAINT uq_sm_approval_skill UNIQUE (approval_id, skill_rating_id)
);

-- ------------------------------------------------------------------
-- Audit log
-- ------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS skillmatrix_assessment_audit_log (
  id                         BIGINT PRIMARY KEY AUTO_INCREMENT,
  submission_id              VARCHAR(30) NOT NULL,
  actor_id                   BIGINT      NOT NULL,
  actor_name                 VARCHAR(100),
  actor_role                 VARCHAR(20),
  action                     VARCHAR(50) NOT NULL,
  previous_status            VARCHAR(30),
  new_status                 VARCHAR(30),
  context_json               JSON,
  ip_address                 VARCHAR(45),
  user_agent                 TEXT,
  created_at                 DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------------
-- Published profile
-- ------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS skillmatrix_employee_skill_profile (
  id                         BIGINT PRIMARY KEY AUTO_INCREMENT,
  employee_id                BIGINT      NOT NULL,
  skill_id                   INT         NOT NULL,
  skill_name                 VARCHAR(150) NOT NULL,
  dept_id                    BIGINT      NOT NULL,
  self_rating                TINYINT     NOT NULL,
  manager_rating             TINYINT,
  final_rating               TINYINT     NOT NULL,
  confidence_score           SMALLINT,
  has_certification          BOOLEAN DEFAULT FALSE,
  has_training               BOOLEAN DEFAULT FALSE,
  has_project_evidence       BOOLEAN DEFAULT FALSE,
  has_portfolio_link         BOOLEAN DEFAULT FALSE,
  source_submission_id       VARCHAR(30) NOT NULL,
  cycle_year                 SMALLINT    NOT NULL,
  last_validated_at          DATETIME    NOT NULL,
  is_stale                   BOOLEAN     NOT NULL DEFAULT FALSE,
  published_at               DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at                 DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uq_sm_employee_skill_per_cycle UNIQUE (employee_id, skill_id, cycle_year)
);

-- ------------------------------------------------------------------
-- Indexes (core)
-- ------------------------------------------------------------------
SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'skillmatrix_assessment_submission' AND INDEX_NAME = 'idx_sm_submission_employee'
    ),
    'SELECT 1',
    'CREATE INDEX idx_sm_submission_employee ON skillmatrix_assessment_submission(employee_id)'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'skillmatrix_assessment_submission' AND INDEX_NAME = 'idx_sm_submission_dept'
    ),
    'SELECT 1',
    'CREATE INDEX idx_sm_submission_dept ON skillmatrix_assessment_submission(dept_id)'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'skillmatrix_assessment_submission' AND INDEX_NAME = 'idx_sm_submission_manager'
    ),
    'SELECT 1',
    'CREATE INDEX idx_sm_submission_manager ON skillmatrix_assessment_submission(reporting_manager_id)'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'skillmatrix_assessment_submission' AND INDEX_NAME = 'idx_sm_submission_status'
    ),
    'SELECT 1',
    'CREATE INDEX idx_sm_submission_status ON skillmatrix_assessment_submission(status)'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'skillmatrix_assessment_skill_rating' AND INDEX_NAME = 'idx_sm_skill_rating_submission'
    ),
    'SELECT 1',
    'CREATE INDEX idx_sm_skill_rating_submission ON skillmatrix_assessment_skill_rating(submission_id)'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'skillmatrix_assessment_skill_rating' AND INDEX_NAME = 'idx_sm_skill_rating_skill'
    ),
    'SELECT 1',
    'CREATE INDEX idx_sm_skill_rating_skill ON skillmatrix_assessment_skill_rating(skill_id)'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'skillmatrix_assessment_project' AND INDEX_NAME = 'idx_sm_project_submission'
    ),
    'SELECT 1',
    'CREATE INDEX idx_sm_project_submission ON skillmatrix_assessment_project(submission_id)'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'skillmatrix_assessment_project' AND INDEX_NAME = 'idx_sm_project_hrms_id'
    ),
    'SELECT 1',
    'CREATE INDEX idx_sm_project_hrms_id ON skillmatrix_assessment_project(hrms_project_id)'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'skillmatrix_assessment_project' AND INDEX_NAME = 'idx_sm_project_skill_domain'
    ),
    'SELECT 1',
    'CREATE INDEX idx_sm_project_skill_domain ON skillmatrix_assessment_project(skill_domain_id)'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'skillmatrix_assessment_project' AND INDEX_NAME = 'idx_sm_project_skill_subdomain'
    ),
    'SELECT 1',
    'CREATE INDEX idx_sm_project_skill_subdomain ON skillmatrix_assessment_project(skill_subdomain_id)'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'skillmatrix_assessment_project' AND INDEX_NAME = 'idx_sm_project_skill_feature'
    ),
    'SELECT 1',
    'CREATE INDEX idx_sm_project_skill_feature ON skillmatrix_assessment_project(skill_domain_feature_id)'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'skillmatrix_assessment_project_skill' AND INDEX_NAME = 'idx_sm_project_skill_project'
    ),
    'SELECT 1',
    'CREATE INDEX idx_sm_project_skill_project ON skillmatrix_assessment_project_skill(assessment_project_id)'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'skillmatrix_assessment_project_skill' AND INDEX_NAME = 'idx_sm_project_skill_skill'
    ),
    'SELECT 1',
    'CREATE INDEX idx_sm_project_skill_skill ON skillmatrix_assessment_project_skill(skill_id)'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'skillmatrix_assessment_approval' AND INDEX_NAME = 'idx_sm_approval_submission'
    ),
    'SELECT 1',
    'CREATE INDEX idx_sm_approval_submission ON skillmatrix_assessment_approval(submission_id)'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'skillmatrix_assessment_approval' AND INDEX_NAME = 'idx_sm_approval_manager'
    ),
    'SELECT 1',
    'CREATE INDEX idx_sm_approval_manager ON skillmatrix_assessment_approval(manager_id)'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'skillmatrix_assessment_audit_log' AND INDEX_NAME = 'idx_sm_audit_submission'
    ),
    'SELECT 1',
    'CREATE INDEX idx_sm_audit_submission ON skillmatrix_assessment_audit_log(submission_id)'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'skillmatrix_employee_skill_profile' AND INDEX_NAME = 'idx_sm_profile_employee'
    ),
    'SELECT 1',
    'CREATE INDEX idx_sm_profile_employee ON skillmatrix_employee_skill_profile(employee_id)'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;

