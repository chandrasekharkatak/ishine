-- Timesheet "Failed to load activities" for project 9846 / team 1456 (emp 934)
-- API: getAllActivitiesByProjectIdandEmpId → activities table by team_id
-- Team 1456 had zero rows; copied from team 920 (same dept 7 roles).

USE emp_portal_db;

INSERT INTO activities (activity, created_by, dept_ids, employee_role, team_id, created_on)
SELECT activity, 934, dept_ids, employee_role, 1456, NOW()
FROM activities
WHERE team_id = 920
  AND NOT EXISTS (SELECT 1 FROM activities a2 WHERE a2.team_id = 1456 LIMIT 1);
