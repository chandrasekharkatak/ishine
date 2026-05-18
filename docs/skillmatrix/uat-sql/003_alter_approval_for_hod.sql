-- Add Level-2 (HOD) workflow tracking into existing manager approval table
-- Single-table design: skillmatrix_assessment_approval contains both manager + HOD statuses

ALTER TABLE skillmatrix_assessment_approval
  ADD COLUMN hod_id BIGINT NOT NULL DEFAULT 0,
  ADD COLUMN hod_name VARCHAR(100) NULL,
  ADD COLUMN manager_approved VARCHAR(10) NOT NULL DEFAULT 'pending', -- pending | yes | no
  ADD COLUMN hod_approved VARCHAR(10) NOT NULL DEFAULT 'pending',     -- pending | yes | no
  ADD COLUMN final_status VARCHAR(10) NOT NULL DEFAULT 'pending',     -- pending | approved | rejected
  ADD COLUMN manager_comment TEXT NULL,
  ADD COLUMN hod_comment TEXT NULL;

