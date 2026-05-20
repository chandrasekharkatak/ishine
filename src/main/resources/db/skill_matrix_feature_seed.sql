-- Skill Matrix as its own sidebar module (tab_route_name = skill-matrix), separate from Performance.
-- Permissions: feature_master / sub_feature_master / role_subfeature_mapping / employee_role_master
-- (same model as other modules).

-- 0) Tab (own left-nav entry — must exist before feature_master row)
INSERT INTO tab_master (tab_name, tab_route_name, tab_icon, tab_sequence)
SELECT 'Skill Matrix', 'skill-matrix', 'fa fa-table', COALESCE((SELECT MAX(tm.tab_sequence) FROM tab_master tm), 0) + 1
WHERE NOT EXISTS (SELECT 1 FROM tab_master x WHERE x.tab_route_name = 'skill-matrix');

-- 1) Feature under Skill Matrix tab
INSERT INTO feature_master (feature_name, tab_id)
SELECT 'Skill Matrix', tm.tab_id
FROM tab_master tm
WHERE tm.tab_route_name = 'skill-matrix'
LIMIT 1;

-- If you already had Skill Matrix under Performance tab, move it instead of inserting duplicate:
-- UPDATE feature_master f
-- INNER JOIN tab_master t ON t.tab_route_name = 'skill-matrix'
-- SET f.tab_id = t.tab_id
-- WHERE f.feature_name = 'Skill Matrix';

-- 2) Sub-features (names fixed in Java SkillMatrixSubFeatureNames + Angular userMapping)
INSERT INTO sub_feature_master (sub_feature_name, feature_id, sub_feature_type)
SELECT 'Skill Matrix Submit For Review', f.feature_id, 0
FROM feature_master f WHERE f.feature_name = 'Skill Matrix' LIMIT 1;

INSERT INTO sub_feature_master (sub_feature_name, feature_id, sub_feature_type)
SELECT 'Skill Matrix My Submissions', f.feature_id, 0
FROM feature_master f WHERE f.feature_name = 'Skill Matrix' LIMIT 1;

INSERT INTO sub_feature_master (sub_feature_name, feature_id, sub_feature_type)
SELECT 'Skill Matrix Approve Skill Requests', f.feature_id, 0
FROM feature_master f WHERE f.feature_name = 'Skill Matrix' LIMIT 1;

INSERT INTO sub_feature_master (sub_feature_name, feature_id, sub_feature_type)
SELECT 'Skill Matrix Master Configuration', f.feature_id, 0
FROM feature_master f WHERE f.feature_name = 'Skill Matrix' LIMIT 1;

-- 3) employee_role_master — ACL "View Default Role Access"
INSERT INTO employee_role_master (employee_role, sub_feature_master_id, sub_feature_name, permission)
SELECT r.persona, s.sub_feature_master_id, s.sub_feature_name, 'N'
FROM sub_feature_master s
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

-- 4) role_subfeature_mapping — assign per job role via Reports → ACL or INSERT as needed.
