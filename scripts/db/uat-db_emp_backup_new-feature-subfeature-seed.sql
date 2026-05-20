-- =============================================================================
-- UAT ONLY — db_emp_backup_new @ 192.168.21.195
-- DO NOT run on production (db_emp_prod).
-- =============================================================================
-- Seeds tab_master + feature_master + sub_feature_master for:
--   • Reimbursement (may already exist — inserts are idempotent)
--   • Grievance
--   • Skill Matrix
--
-- Matches emp_portal_db UAT + application code (Angular userMapping keys).
-- After this script, map job roles in Configuration → ACL (role_subfeature_mapping)
-- or run the optional role-mapping block at the bottom.
-- =============================================================================

USE db_emp_backup_new;

START TRANSACTION;

-- -----------------------------------------------------------------------------
-- 1) REIMBURSEMENT
-- -----------------------------------------------------------------------------
INSERT INTO tab_master (tab_name, tab_route_name, tab_icon, tab_sequence)
SELECT 'Reimbursement', 'reimbursement', 'fa fa-credit-card',
       COALESCE((SELECT MAX(tm.tab_sequence) FROM tab_master tm), 0) + 1
WHERE NOT EXISTS (
    SELECT 1 FROM tab_master WHERE tab_route_name = 'reimbursement'
);

INSERT INTO feature_master (feature_name, tab_id)
SELECT 'Reimbursement', tm.tab_id
FROM tab_master tm
WHERE tm.tab_route_name = 'reimbursement'
  AND NOT EXISTS (
    SELECT 1 FROM feature_master fm
    WHERE fm.feature_name = 'Reimbursement' AND fm.tab_id = tm.tab_id
  );

INSERT INTO sub_feature_master (sub_feature_name, feature_id, sub_feature_type)
SELECT 'My Reimbursement', fm.feature_id, 0
FROM feature_master fm
WHERE fm.feature_name = 'Reimbursement'
  AND NOT EXISTS (
    SELECT 1 FROM sub_feature_master sf
    WHERE sf.sub_feature_name = 'My Reimbursement' AND sf.feature_id = fm.feature_id
  );

INSERT INTO sub_feature_master (sub_feature_name, feature_id, sub_feature_type)
SELECT 'View Reimbursement', fm.feature_id, 0
FROM feature_master fm
WHERE fm.feature_name = 'Reimbursement'
  AND NOT EXISTS (
    SELECT 1 FROM sub_feature_master sf
    WHERE sf.sub_feature_name = 'View Reimbursement' AND sf.feature_id = fm.feature_id
  );

INSERT INTO sub_feature_master (sub_feature_name, feature_id, sub_feature_type)
SELECT 'Approve Reimbursement', fm.feature_id, 0
FROM feature_master fm
WHERE fm.feature_name = 'Reimbursement'
  AND NOT EXISTS (
    SELECT 1 FROM sub_feature_master sf
    WHERE sf.sub_feature_name = 'Approve Reimbursement' AND sf.feature_id = fm.feature_id
  );

INSERT INTO sub_feature_master (sub_feature_name, feature_id, sub_feature_type)
SELECT 'Total Reimbursementrequest', fm.feature_id, 0
FROM feature_master fm
WHERE fm.feature_name = 'Reimbursement'
  AND NOT EXISTS (
    SELECT 1 FROM sub_feature_master sf
    WHERE sf.sub_feature_name = 'Total Reimbursementrequest' AND sf.feature_id = fm.feature_id
  );

-- -----------------------------------------------------------------------------
-- 2) GRIEVANCE
-- -----------------------------------------------------------------------------
INSERT INTO tab_master (tab_name, tab_route_name, tab_icon, tab_sequence)
SELECT 'Grievance', 'grievance', 'fa fa-ticket', 999
WHERE NOT EXISTS (
    SELECT 1 FROM tab_master WHERE tab_route_name = 'grievance'
);

INSERT INTO feature_master (feature_name, tab_id)
SELECT 'Grievance', tm.tab_id
FROM tab_master tm
WHERE tm.tab_route_name = 'grievance'
  AND NOT EXISTS (
    SELECT 1 FROM feature_master fm
    WHERE fm.feature_name = 'Grievance' AND fm.tab_id = tm.tab_id
  );

INSERT INTO sub_feature_master (sub_feature_name, feature_id, sub_feature_type)
SELECT v.sub_feature_name, fm.feature_id, 1
FROM feature_master fm
CROSS JOIN (
    SELECT 'Raise Ticket' AS sub_feature_name
    UNION ALL SELECT 'View Own Tickets'
    UNION ALL SELECT 'View All Tickets'
    UNION ALL SELECT 'Update Ticket'
    UNION ALL SELECT 'Assigned Tickets'
    UNION ALL SELECT 'Grievance Issue Scenarios'
) v
WHERE fm.feature_name = 'Grievance'
  AND NOT EXISTS (
    SELECT 1 FROM sub_feature_master sf
    WHERE sf.sub_feature_name = v.sub_feature_name AND sf.feature_id = fm.feature_id
  );

-- -----------------------------------------------------------------------------
-- 3) SKILL MATRIX
-- -----------------------------------------------------------------------------
INSERT INTO tab_master (tab_name, tab_route_name, tab_icon, tab_sequence)
SELECT 'Skill Matrix', 'skill-matrix', 'fa fa-table', 1000
WHERE NOT EXISTS (
    SELECT 1 FROM tab_master WHERE tab_route_name = 'skill-matrix'
);

INSERT INTO feature_master (feature_name, tab_id)
SELECT 'Skill Matrix', tm.tab_id
FROM tab_master tm
WHERE tm.tab_route_name = 'skill-matrix'
  AND NOT EXISTS (
    SELECT 1 FROM feature_master fm
    WHERE fm.feature_name = 'Skill Matrix' AND fm.tab_id = tm.tab_id
  );

INSERT INTO sub_feature_master (sub_feature_name, feature_id, sub_feature_type)
SELECT v.sub_feature_name, fm.feature_id, 0
FROM feature_master fm
CROSS JOIN (
    SELECT 'Skill Matrix Submit For Review' AS sub_feature_name
    UNION ALL SELECT 'Skill Matrix My Submissions'
    UNION ALL SELECT 'Skill Matrix Approve Skill Requests'
    UNION ALL SELECT 'Skill Matrix Master Configuration'
) v
WHERE fm.feature_name = 'Skill Matrix'
  AND NOT EXISTS (
    SELECT 1 FROM sub_feature_master sf
    WHERE sf.sub_feature_name = v.sub_feature_name AND sf.feature_id = fm.feature_id
  );

-- Skill Matrix — employee_role_master rows for Configuration → ACL "View Default Role Access"
INSERT INTO employee_role_master (employee_role, sub_feature_master_id, sub_feature_name, permission)
SELECT r.persona, s.sub_feature_master_id, s.sub_feature_name, 'N'
FROM sub_feature_master s
INNER JOIN feature_master f ON f.feature_id = s.feature_id AND f.feature_name = 'Skill Matrix'
CROSS JOIN (
    SELECT 'Employee' AS persona
    UNION ALL SELECT 'TeamLead'
    UNION ALL SELECT 'Manager'
    UNION ALL SELECT 'HR'
    UNION ALL SELECT 'RMG'
    UNION ALL SELECT 'HOD'
    UNION ALL SELECT 'SuperAdmin'
) r
WHERE s.sub_feature_name IN (
    'Skill Matrix Submit For Review',
    'Skill Matrix My Submissions',
    'Skill Matrix Approve Skill Requests',
    'Skill Matrix Master Configuration'
)
AND NOT EXISTS (
    SELECT 1 FROM employee_role_master e
    WHERE e.sub_feature_master_id = s.sub_feature_master_id
      AND e.employee_role = r.persona
);

COMMIT;

-- -----------------------------------------------------------------------------
-- Verify
-- -----------------------------------------------------------------------------
SELECT tm.tab_route_name, fm.feature_name, sfm.sub_feature_name, sfm.sub_feature_type
FROM tab_master tm
JOIN feature_master fm ON fm.tab_id = tm.tab_id
LEFT JOIN sub_feature_master sfm ON sfm.feature_id = fm.feature_id
WHERE tm.tab_route_name IN ('reimbursement', 'grievance', 'skill-matrix')
ORDER BY tm.tab_route_name, fm.feature_name, sfm.sub_feature_name;
