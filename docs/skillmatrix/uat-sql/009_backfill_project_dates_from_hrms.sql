-- Backfill Skill Matrix project start/end dates from HRMS projects table (UAT)
-- UAT projects.start_date/end_date are VARCHAR(255) in format 'yyyy-MM-dd' (or NULL).
-- skillmatrix_assessment_project.start_date/end_date are DATE.

UPDATE skillmatrix_assessment_project smp
JOIN projects p ON p.project_id = smp.hrms_project_id
SET
  smp.start_date = CASE
    WHEN smp.start_date IS NULL AND p.start_date IS NOT NULL AND TRIM(p.start_date) <> '' THEN STR_TO_DATE(TRIM(p.start_date), '%Y-%m-%d')
    ELSE smp.start_date
  END,
  smp.end_date = CASE
    WHEN smp.end_date IS NULL AND p.end_date IS NOT NULL AND TRIM(p.end_date) <> '' THEN STR_TO_DATE(TRIM(p.end_date), '%Y-%m-%d')
    ELSE smp.end_date
  END
WHERE smp.project_source = 'hrms'
  AND smp.hrms_project_id IS NOT NULL
  AND (smp.start_date IS NULL OR smp.end_date IS NULL);

