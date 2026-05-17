-- emp_portal_db (UAT / any env): Fix blank "Your details" on Apply Reimbursement when HOD still shows.
--
-- Root cause (EmployeeService.getEmployeeByEmpId):
--   After building EmployeeDTO, the service loads emp_primary_project_mapping with is_mapped = 'Y'
--   and calls projectRepository.findByProjectId(primary_project_id).
--   If that project_id is missing from `projects`, findByProjectId returns null → NPE on
--   project.getProjectName() → API fails → currentEmployeeInfo never loads. HOD on the page
--   comes from session (currentUser.hodName), so it can still appear.
--
-- Fix (all employees): unmap any "active" primary mapping that points to a non-existent project.
-- HR can re-assign a valid primary project later where needed.

-- 1) Preview how many rows will change
SELECT epm.emp_id, e.name, e.employeement_id, epm.mapping_id, epm.primary_project_id
FROM emp_primary_project_mapping epm
JOIN employee e ON e.emp_id = epm.emp_id
LEFT JOIN projects p ON p.project_id = epm.primary_project_id
WHERE epm.is_mapped = 'Y'
  AND epm.primary_project_id IS NOT NULL
  AND p.project_id IS NULL;

-- 2) Apply fix (run after reviewing the SELECT above)
UPDATE emp_primary_project_mapping epm
LEFT JOIN projects p ON p.project_id = epm.primary_project_id
SET epm.is_mapped = 'N', epm.updated_on = NOW()
WHERE epm.is_mapped = 'Y'
  AND epm.primary_project_id IS NOT NULL
  AND p.project_id IS NULL;

-- 3) Verify none remain
SELECT COUNT(*) AS remaining_orphans
FROM emp_primary_project_mapping epm
LEFT JOIN projects p ON p.project_id = epm.primary_project_id
WHERE epm.is_mapped = 'Y'
  AND epm.primary_project_id IS NOT NULL
  AND p.project_id IS NULL;
