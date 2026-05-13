-- Backfill HOD fields for existing approval rows (UAT)
-- Matches backend behavior: use safe defaults when HOD is missing.

-- 1) If hod_name is NULL/blank, set to a readable default.
UPDATE skillmatrix_assessment_approval
SET hod_name = 'HOD'
WHERE hod_name IS NULL OR TRIM(hod_name) = '';

-- 2) If hod_id is NULL (older data), set to 0 to match NOT NULL defaulted schema.
-- (This is safe even if column is NOT NULL; it only affects rows where NULL existed.)
UPDATE skillmatrix_assessment_approval
SET hod_id = 0
WHERE hod_id IS NULL;

