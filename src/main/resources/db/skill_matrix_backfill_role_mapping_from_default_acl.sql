-- One-time backfill: copy Default ACL (employee_role_master permission=Y) into role_subfeature_mapping
-- so the side nav appears without re-saving each checkbox. Safe to re-run (NOT EXISTS).
-- Limit to Skill Matrix sub-features only.

INSERT INTO role_subfeature_mapping (job_role_id, sub_feature_master_id)
SELECT DISTINCT jr.job_role_id, erm.sub_feature_master_id
FROM employee_role_master erm
INNER JOIN job_role jr ON jr.employee_role = erm.employee_role
INNER JOIN sub_feature_master sfm ON sfm.sub_feature_master_id = erm.sub_feature_master_id
WHERE erm.permission = 'Y'
  AND sfm.sub_feature_name LIKE 'Skill Matrix%'
  AND NOT EXISTS (
    SELECT 1 FROM role_subfeature_mapping rsm
    WHERE rsm.job_role_id = jr.job_role_id
      AND rsm.sub_feature_master_id = erm.sub_feature_master_id
  );
