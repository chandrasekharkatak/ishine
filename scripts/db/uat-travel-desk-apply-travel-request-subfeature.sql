-- =============================================================================
-- UAT ONLY — db_emp_backup_new @ 192.168.21.195
-- Travel Desk: add "Apply Travel Request" sub-feature (create form tab).
-- Tab order in UI: Apply Travel Request → View → Approve → Total Travel Request.
-- Maps via employee_role_master + role_subfeature_mapping (not hardcoded in Angular).
-- =============================================================================

USE db_emp_backup_new;

START TRANSACTION;

INSERT INTO sub_feature_master (sub_feature_name, feature_id, sub_feature_type)
SELECT 'Apply Travel Request', fm.feature_id, 0
FROM feature_master fm
WHERE fm.feature_name = 'Travel Desk'
  AND NOT EXISTS (
    SELECT 1 FROM sub_feature_master sf
    WHERE sf.sub_feature_name = 'Apply Travel Request' AND sf.feature_id = fm.feature_id
  );

-- Default ACL (Configuration → View Default Role Access), same personas as My Reimbursement
INSERT INTO employee_role_master (employee_role, sub_feature_master_id, sub_feature_name, permission)
SELECT r.persona, s.sub_feature_master_id, s.sub_feature_name, 'Y'
FROM sub_feature_master s
INNER JOIN feature_master f ON f.feature_id = s.feature_id AND f.feature_name = 'Travel Desk'
CROSS JOIN (
    SELECT 'Employee' AS persona
    UNION ALL SELECT 'TeamLead'
    UNION ALL SELECT 'Manager'
    UNION ALL SELECT 'RMG'
    UNION ALL SELECT 'HOD'
    UNION ALL SELECT 'SuperAdmin'
) r
WHERE s.sub_feature_name = 'Apply Travel Request'
  AND NOT EXISTS (
    SELECT 1 FROM employee_role_master e
    WHERE e.sub_feature_master_id = s.sub_feature_master_id
      AND e.employee_role = r.persona
  );

-- Copy job-role mappings from legacy "Apply Journey" where present
INSERT INTO role_subfeature_mapping (job_role_id, sub_feature_master_id)
SELECT rsm.job_role_id, s_new.sub_feature_master_id
FROM role_subfeature_mapping rsm
INNER JOIN sub_feature_master s_old
    ON s_old.sub_feature_master_id = rsm.sub_feature_master_id
    AND s_old.sub_feature_name = 'Apply Journey'
INNER JOIN feature_master fm ON fm.feature_id = s_old.feature_id AND fm.feature_name = 'Travel Desk'
INNER JOIN sub_feature_master s_new
    ON s_new.feature_id = fm.feature_id AND s_new.sub_feature_name = 'Apply Travel Request'
WHERE NOT EXISTS (
    SELECT 1 FROM role_subfeature_mapping x
    WHERE x.job_role_id = rsm.job_role_id AND x.sub_feature_master_id = s_new.sub_feature_master_id
);

-- Align role_subfeature_mapping with employee_role_master (permission Y)
INSERT INTO role_subfeature_mapping (job_role_id, sub_feature_master_id)
SELECT jr.job_role_id, erm.sub_feature_master_id
FROM employee_role_master erm
INNER JOIN job_role jr ON jr.employee_role = erm.employee_role
WHERE UPPER(TRIM(COALESCE(erm.permission, ''))) = 'Y'
  AND erm.sub_feature_name = 'Apply Travel Request'
  AND NOT EXISTS (
    SELECT 1 FROM role_subfeature_mapping r
    WHERE r.job_role_id = jr.job_role_id AND r.sub_feature_master_id = erm.sub_feature_master_id
  );

COMMIT;

SELECT sfm.sub_feature_master_id, sfm.sub_feature_name, COUNT(rsm.role_feature_map_id) AS mapped_job_roles
FROM sub_feature_master sfm
INNER JOIN feature_master fm ON fm.feature_id = sfm.feature_id AND fm.feature_name = 'Travel Desk'
LEFT JOIN role_subfeature_mapping rsm ON rsm.sub_feature_master_id = sfm.sub_feature_master_id
WHERE sfm.sub_feature_name IN ('Apply Travel Request', 'Apply Journey', 'Total Travelrequest')
GROUP BY sfm.sub_feature_master_id, sfm.sub_feature_name
ORDER BY sfm.sub_feature_name;
