-- Add HOD id/name to submission header for reporting & UI needs (UAT)

ALTER TABLE skillmatrix_assessment_submission
  ADD COLUMN hod_id BIGINT NOT NULL DEFAULT 0 AFTER reporting_manager_name,
  ADD COLUMN hod_name VARCHAR(100) NULL AFTER hod_id;

-- Backfill existing rows from department master mapping (if available)
UPDATE skillmatrix_assessment_submission s
LEFT JOIN department d ON d.dept_id = s.dept_id
LEFT JOIN employee e ON e.emp_id = d.hod_id
SET
  s.hod_id = COALESCE(d.hod_id, 0),
  s.hod_name = COALESCE(NULLIF(TRIM(e.name),''), 'HOD')
WHERE (s.hod_id = 0 OR s.hod_name IS NULL OR TRIM(s.hod_name)='');

