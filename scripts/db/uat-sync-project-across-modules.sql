-- Align project shown in Timesheet vs Grievance vs Reimbursement (emp_portal_db UAT)
--
-- Module sources:
--   Timesheet: employee_team_mapping (active != 2) + date range on start/end
--   Grievance: emp_primary_project_mapping WHERE is_mapped = 'Y'
--   Reimbursement: employee_team_mapping (active=1) + projects.active=true + teams.is_active=Y + client_locations
--
-- Common mismatch: inactive project still on team mapping (active=0) with end_date >= today
--   -> Timesheet still lists it; Reimbursement/Grievance do not.

USE emp_portal_db;

-- Example: emp 934 — end stale mapping to project 6940 before AXIS team (9846) started
UPDATE employee_team_mapping
SET active = 0, end_date = '2026-05-13 23:59:59', updated_on = NOW()
WHERE emp_id = 934 AND team_id = 920;

-- Ensure primary = current delivery project (9846 for emp 934)
UPDATE emp_primary_project_mapping SET is_mapped = 'N'
WHERE emp_id = 934 AND is_mapped = 'Y' AND primary_project_id != 9846;
UPDATE emp_primary_project_mapping
SET is_mapped = 'Y', primary_project_name = 'DEV-TNM-wcdbat-dev@test', updated_on = NOW()
WHERE emp_id = 934 AND primary_project_id = 9846;

-- Bulk: primary project not on any active=1 team -> set to latest active team mapping
UPDATE emp_primary_project_mapping eppm
INNER JOIN (
  SELECT etm.emp_id, t.project_id, p.project_name,
         ROW_NUMBER() OVER (PARTITION BY etm.emp_id ORDER BY etm.start_date DESC, t.project_id DESC) AS rn
  FROM employee_team_mapping etm
  INNER JOIN teams t ON t.team_id = etm.team_id AND t.is_active = 'Y'
  INNER JOIN projects p ON p.project_id = t.project_id AND p.active = 'true'
  WHERE etm.active = 1
) latest ON latest.emp_id = eppm.emp_id AND latest.rn = 1
SET eppm.primary_project_id = latest.project_id,
    eppm.primary_project_name = latest.project_name,
    eppm.updated_on = NOW()
WHERE eppm.is_mapped = 'Y'
  AND NOT EXISTS (
    SELECT 1 FROM employee_team_mapping etm2
    INNER JOIN teams t2 ON t2.team_id = etm2.team_id
    WHERE etm2.emp_id = eppm.emp_id AND etm2.active = 1
      AND t2.project_id = eppm.primary_project_id
  );
