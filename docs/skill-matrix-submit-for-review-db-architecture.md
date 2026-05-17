# Skill Matrix — Submit for Review (DB architecture)

This document captures the **proposed database schema** for the Skill Matrix “Submit for review” wizard (Steps 1–6) and the **adjustments needed to align with the current codebase** in this repo.

## What the current code already assumes

From the implementation under:

- `src/main/java/com/apmosys/employeeportal/service/SkillMatrixSubmitService.java`
- `src/main/java/com/apmosys/employeeportal/controller/SkillMatrixController.java`
- `src/main/java/com/apmosys/employeeportal/model/*`

The system uses these identifier types today:

- **Employee**: `employee.emp_id` → `Employee.empId` is `BIGINT`/`Long`
- **Department**: `department.dept_id` → `Department.deptId` is `BIGINT`/`Long`
- **Skill**: `skills_master.skill_id` → `SkillsMaster.skillId` is `INT`/`Integer`
- **Sub-skill**: `subskills_master.subskill_id` → `SubskillsMaster.subskillId` is `INT`/`Integer`
- **Project history** (Step 4 list) is derived from mapping tables:
  - `employee_team_mapping` (`EmployeeTeamMap`) → `teams` (`Team`) → `projects` (`Project`)
  - Exposed as `GET /api/skill-matrix/submit-for-review/projects`

Also important:

- “Skill not in the list?” currently **creates a real `skills_master` row** via `POST /submit-for-review/propose-skill` (Optional, Active=Yes, under employee’s department).  
  That means in the submit flow, a “custom skill” is **not free-text** anymore; it becomes a real skill with an `INT` `skill_id`.

## Changes recommended to your schema (to match this repo)

Your draft is solid, but these tweaks will prevent mismatches with existing types/flows:

1. **Use numeric FK types that match existing masters**
   - Replace `VARCHAR(10)` skill/subskill ids with `INT`
   - Replace department id with `BIGINT`
   - Replace employee ids with `BIGINT`

2. **Drop/relax “custom skill” columns unless you truly need free-text skills**
   - Because the current UI/BE design persists proposed skills into `skills_master`, `assessment_skill_rating.skill_id` can remain **NOT NULL** for all skills.
   - If you still want “free-text skills” that are never inserted into `skills_master`, then keep `is_custom_skill` + `custom_skill_category`, but allow `skill_id` to be nullable and change unique constraints accordingly.

3. **Project employee_role should be free text (per latest UI requirement)**
   - Use `VARCHAR(100)` or `VARCHAR(150)` instead of enum-like values.

4. **Submission primary key**
   - Keeping `submission_id` as a business id like `SKA-2025-EMP-1082` is fine.
   - Alternative (often easier): `BIGINT` surrogate key + separate `submission_code` unique column. Either works.

5. **MySQL vs Postgres**
   - This repo uses MySQL-style named native queries and `TINYINT`-like flags in multiple places.  
   - The schema below is written in a **MySQL 8 friendly** style (use `JSON` instead of `JSONB`, `BIGINT AUTO_INCREMENT` instead of `BIGSERIAL`).

## Final schema (aligned with this repo’s types)

> Note: reference/master tables (`employee`, `department`, `skills_master`, `subskills_master`, `projects`, `teams`, `employee_team_mapping`) already exist in the HRMS DB.  
> The tables below are the **new submit-for-review persistence layer**.

```sql
-- ─────────────────────────────────────────────────────────────
-- SKILLS MASTER — provenance fields (recommended)
-- Because “Skill not in the list?” currently inserts into skills_master,
-- add columns so we can ALWAYS tell a skill was created via Skill Matrix.
-- ─────────────────────────────────────────────────────────────
ALTER TABLE skills_master
  ADD COLUMN created_via_skillmatrix_submit BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN created_by_emp_id BIGINT NULL,
  ADD COLUMN created_for_dept_id BIGINT NULL;

CREATE INDEX idx_sm_skill_created_via_submit ON skills_master(created_via_skillmatrix_submit);
CREATE INDEX idx_sm_skill_created_by_emp ON skills_master(created_by_emp_id);

-- ============================================================
--  iShine / HRMS SKILL MATRIX — ASSESSMENT SUBMISSION SCHEMA
--  (Aligned with current repo types: emp_id BIGINT, dept_id BIGINT,
--   skill_id INT, subskill_id INT, project_id INT)
-- ============================================================

-- ─────────────────────────────────────────────────────────────
-- STEP 1 — Submission header (one row per cycle per employee)
-- ─────────────────────────────────────────────────────────────
CREATE TABLE skillmatrix_assessment_submission (
  submission_id              VARCHAR(30)  PRIMARY KEY,             -- e.g. SKA-2025-EMP-1082
  employee_id                BIGINT       NOT NULL,                -- FK → employee.emp_id
  employee_name              VARCHAR(100) NOT NULL,
  dept_id                    BIGINT       NOT NULL,                -- FK → department.dept_id
  dept_name                  VARCHAR(100) NOT NULL,
  designation                VARCHAR(100) NOT NULL,
  reporting_manager_id       BIGINT       NOT NULL,                -- FK → employee.emp_id
  reporting_manager_name     VARCHAR(100) NOT NULL,
  experience_in_role         VARCHAR(20),
  assessment_cycle           VARCHAR(50)  NOT NULL,
  cycle_year                 SMALLINT     NOT NULL,
  submission_deadline        DATE,
  declaration_accepted       BOOLEAN      NOT NULL DEFAULT FALSE,
  declaration_accepted_at    DATETIME,

  status                     VARCHAR(30)  NOT NULL DEFAULT 'draft',
  -- draft | submitted | under_review | changes_requested | approved | rejected

  submitted_at               DATETIME,
  approved_at                DATETIME,
  created_at                 DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at                 DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT uq_sm_submission_employee_cycle UNIQUE (employee_id, assessment_cycle)
);

-- ─────────────────────────────────────────────────────────────
-- STEPS 2+3 — One row per selected skill
-- ─────────────────────────────────────────────────────────────
CREATE TABLE skillmatrix_assessment_skill_rating (
  id                         BIGINT PRIMARY KEY AUTO_INCREMENT,
  submission_id              VARCHAR(30) NOT NULL,

  skill_id                   INT         NOT NULL,                -- FK → skills_master.skill_id
  skill_name                 VARCHAR(150) NOT NULL,
  dept_id                    BIGINT      NOT NULL,
  is_required                BOOLEAN     NOT NULL,

  -- Step 3A
  self_rating                TINYINT     NOT NULL,                -- 1..5
  years_experience           VARCHAR(20),
  last_used                  VARCHAR(30),
  usage_frequency            VARCHAR(20),
  what_can_you_do            TEXT        NOT NULL,

  -- Step 3B
  used_in_project            BOOLEAN     NOT NULL DEFAULT FALSE,

  -- Step 3C
  github_portfolio_url       VARCHAR(500),
  colleague_endorser         VARCHAR(200),
  knowledge_session_note     TEXT,

  -- Manager fields (later)
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

-- ─────────────────────────────────────────────────────────────
-- STEP 3 — Selected sub-skills per skill
-- ─────────────────────────────────────────────────────────────
CREATE TABLE skillmatrix_assessment_subskill_selection (
  id                         BIGINT PRIMARY KEY AUTO_INCREMENT,
  skill_rating_id            BIGINT      NOT NULL,
  submission_id              VARCHAR(30) NOT NULL,
  skill_id                   INT         NOT NULL,
  subskill_id                INT         NOT NULL,                -- FK → subskills_master.subskill_id
  subskill_name              VARCHAR(200) NOT NULL,
  is_confident               BOOLEAN     NOT NULL DEFAULT TRUE,
  created_at                 DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_sm_subskill_rating
    FOREIGN KEY (skill_rating_id) REFERENCES skillmatrix_assessment_skill_rating(id) ON DELETE CASCADE,
  CONSTRAINT uq_sm_subskill_per_rating UNIQUE (skill_rating_id, subskill_id)
);

-- ─────────────────────────────────────────────────────────────
-- STEP 3 — Certification per skill
-- ─────────────────────────────────────────────────────────────
CREATE TABLE skillmatrix_assessment_certification (
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

-- ─────────────────────────────────────────────────────────────
-- STEP 3 — Training per skill
-- ─────────────────────────────────────────────────────────────
CREATE TABLE skillmatrix_assessment_training (
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

-- ─────────────────────────────────────────────────────────────
-- STEP 4 — Project history rows captured for a submission
--   project_source = 'hrms' (from mapping) or 'self_added' (manual)
-- ─────────────────────────────────────────────────────────────
CREATE TABLE skillmatrix_assessment_project (
  id                         BIGINT PRIMARY KEY AUTO_INCREMENT,
  submission_id              VARCHAR(30) NOT NULL,

  project_source             VARCHAR(20) NOT NULL DEFAULT 'hrms',   -- hrms | self_added
  hrms_project_id            INT,                                    -- FK → projects.project_id (nullable for manual)
  project_name               VARCHAR(200) NOT NULL,

  client_or_type             VARCHAR(100),
  project_status             VARCHAR(20),
  start_date                 DATE,
  end_date                   DATE,
  duration_text              VARCHAR(50),

  employee_role              VARCHAR(150),                           -- free text
  allocation_pct             SMALLINT,
  contribution_summary       TEXT        NOT NULL,
  is_included                BOOLEAN     NOT NULL DEFAULT TRUE,

  created_at                 DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at                 DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT fk_sm_project_submission
    FOREIGN KEY (submission_id) REFERENCES skillmatrix_assessment_submission(submission_id) ON DELETE CASCADE
);

-- ─────────────────────────────────────────────────────────────
-- STEP 4 — Skills applied per project
-- ─────────────────────────────────────────────────────────────
CREATE TABLE skillmatrix_assessment_project_skill (
  id                         BIGINT PRIMARY KEY AUTO_INCREMENT,
  assessment_project_id      BIGINT      NOT NULL,
  submission_id              VARCHAR(30) NOT NULL,

  skill_id                   INT         NOT NULL,
  skill_name                 VARCHAR(150) NOT NULL,
  level_used                 TINYINT     NOT NULL,                  -- 1..5
  specific_contribution      TEXT        NOT NULL,

  created_at                 DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_sm_project_skill_project
    FOREIGN KEY (assessment_project_id) REFERENCES skillmatrix_assessment_project(id) ON DELETE CASCADE
);

-- ─────────────────────────────────────────────────────────────
-- STEP 5 — Aspirations (header) + skills to learn
-- ─────────────────────────────────────────────────────────────
CREATE TABLE skillmatrix_assessment_aspiration (
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

CREATE TABLE skillmatrix_assessment_aspiration_skill (
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

-- ─────────────────────────────────────────────────────────────
-- STEP 6 — Approval workflow header + per-skill decisions
-- ─────────────────────────────────────────────────────────────
CREATE TABLE skillmatrix_assessment_approval (
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

CREATE TABLE skillmatrix_assessment_approval_skill (
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

-- ─────────────────────────────────────────────────────────────
-- Audit log
-- ─────────────────────────────────────────────────────────────
CREATE TABLE skillmatrix_assessment_audit_log (
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

-- ─────────────────────────────────────────────────────────────
-- Published profile (final)
-- ─────────────────────────────────────────────────────────────
CREATE TABLE skillmatrix_employee_skill_profile (
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
```

## Indexes (keep from your draft)

Your index plan is good; keep it. The only changes needed are column types (e.g., skill_id INT, employee_id BIGINT).

## Notes for implementing submit persistence in this repo

As of now, the repo only supports:

- Step 1 context: `GET /submit-for-review/context`
- Step 2 skill pool: `GET /submit-for-review/skill-pool`
- Skill categories: `GET /submit-for-review/skill-categories`
- Propose skill into `skills_master`: `POST /submit-for-review/propose-skill`
- Step 4 project list: `GET /submit-for-review/projects`

It does **not** yet have a “final submit” endpoint that writes a submission record. When you implement it, the backend will need:

- A **submission create/update** API (save draft each step or save everything at final submit)
- Insert/update logic into the new tables above (and later manager review flow)
- A publish job/process to upsert into `employee_skill_profile` once the submission is approved

