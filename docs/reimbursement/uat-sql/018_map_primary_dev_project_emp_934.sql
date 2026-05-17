-- emp_portal_db: Map a development (DEV-*) primary project for emp_id = 934 (Asutosh Maharana).
-- Uses existing row mapping_id = 1568; adjust mapping_id if your data differs.
--
-- Project 9846 = DEV-TNM-wcdbat-dev@test (exists in `projects` on UAT as of 2026-05-14).
-- To pick another DEV project, change 9846 and re-run the name subquery or set primary_project_name manually.

UPDATE emp_primary_project_mapping
SET primary_project_id = 9846,
    primary_project_name = (SELECT p.project_name FROM projects p WHERE p.project_id = 9846 LIMIT 1),
    is_mapped = 'Y',
    updated_by = 934,
    updated_on = NOW()
WHERE emp_id = 934
  AND mapping_id = 1568;
