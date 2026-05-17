-- Fix Configuration > Leave > Leave Types showing "No details to display"
-- API GET /api/getAllLeaveTypes uses named query LeaveTypeMaster.findByLeaveTypeMasterId
-- which INNER JOINs employee ON created_by — NULL created_by returns zero rows.

USE emp_portal_db;

UPDATE leave_type_master
SET created_by = COALESCE(created_by, 1),
    created_on = COALESCE(created_on, NOW())
WHERE created_by IS NULL;

USE db_emp_backup_new;

UPDATE leave_type_master
SET created_by = COALESCE(created_by, 1),
    created_on = COALESCE(created_on, NOW())
WHERE created_by IS NULL;

-- Verify (run per schema):
-- SELECT COUNT(*) FROM leave_type_master ltm
-- INNER JOIN employee e1 ON e1.emp_id = ltm.created_by;
