-- Grievance RBAC seed script
-- Run this once in the application database.
-- It creates:
-- 1) Grievance tab
-- 2) Grievance feature
-- 3) Sub-features: Raise Ticket, View Own Tickets, View All Tickets, Update Ticket, Assigned Tickets,
--    Grievance Issue Scenarios (admin list/API for curated issue labels)
-- 4) Role mappings:
--    - All roles: Raise Ticket + View Own Tickets
--    - Admin/VP/Project Manager roles: View All Tickets + Update Ticket
--    - Development/HR departments: Assigned Tickets
--    - Admins (job role or department name contains admin), or Development dept with VP or Project Manager in role text: Grievance Issue Scenarios

START TRANSACTION;

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
    SELECT 1
    FROM feature_master fm
    WHERE fm.feature_name = 'Grievance'
      AND fm.tab_id = tm.tab_id
);

INSERT INTO sub_feature_master (sub_feature_name, feature_id, sub_feature_type)
SELECT 'Raise Ticket', fm.feature_id, 1
FROM feature_master fm
WHERE fm.feature_name = 'Grievance'
AND NOT EXISTS (
    SELECT 1 FROM sub_feature_master sf
    WHERE sf.sub_feature_name = 'Raise Ticket' AND sf.feature_id = fm.feature_id
);

INSERT INTO sub_feature_master (sub_feature_name, feature_id, sub_feature_type)
SELECT 'View Own Tickets', fm.feature_id, 1
FROM feature_master fm
WHERE fm.feature_name = 'Grievance'
AND NOT EXISTS (
    SELECT 1 FROM sub_feature_master sf
    WHERE sf.sub_feature_name = 'View Own Tickets' AND sf.feature_id = fm.feature_id
);

INSERT INTO sub_feature_master (sub_feature_name, feature_id, sub_feature_type)
SELECT 'View All Tickets', fm.feature_id, 1
FROM feature_master fm
WHERE fm.feature_name = 'Grievance'
AND NOT EXISTS (
    SELECT 1 FROM sub_feature_master sf
    WHERE sf.sub_feature_name = 'View All Tickets' AND sf.feature_id = fm.feature_id
);

INSERT INTO sub_feature_master (sub_feature_name, feature_id, sub_feature_type)
SELECT 'Update Ticket', fm.feature_id, 1
FROM feature_master fm
WHERE fm.feature_name = 'Grievance'
AND NOT EXISTS (
    SELECT 1 FROM sub_feature_master sf
    WHERE sf.sub_feature_name = 'Update Ticket' AND sf.feature_id = fm.feature_id
);

INSERT INTO sub_feature_master (sub_feature_name, feature_id, sub_feature_type)
SELECT 'Assigned Tickets', fm.feature_id, 1
FROM feature_master fm
WHERE fm.feature_name = 'Grievance'
AND NOT EXISTS (
    SELECT 1 FROM sub_feature_master sf
    WHERE sf.sub_feature_name = 'Assigned Tickets' AND sf.feature_id = fm.feature_id
);

INSERT INTO sub_feature_master (sub_feature_name, feature_id, sub_feature_type)
SELECT 'Grievance Issue Scenarios', fm.feature_id, 1
FROM feature_master fm
WHERE fm.feature_name = 'Grievance'
AND NOT EXISTS (
    SELECT 1 FROM sub_feature_master sf
    WHERE sf.sub_feature_name = 'Grievance Issue Scenarios' AND sf.feature_id = fm.feature_id
);

-- Map Raise/View Own to all job roles
INSERT INTO role_subfeature_mapping (job_role_id, sub_feature_master_id)
SELECT jr.job_role_id, sf.sub_feature_master_id
FROM job_role jr
JOIN sub_feature_master sf ON sf.sub_feature_name IN ('Raise Ticket', 'View Own Tickets')
JOIN feature_master fm ON fm.feature_id = sf.feature_id AND fm.feature_name = 'Grievance'
LEFT JOIN role_subfeature_mapping rsm
       ON rsm.job_role_id = jr.job_role_id AND rsm.sub_feature_master_id = sf.sub_feature_master_id
WHERE rsm.role_feature_map_id IS NULL;

-- Map View All/Update only to admin/vp/project manager roles
INSERT INTO role_subfeature_mapping (job_role_id, sub_feature_master_id)
SELECT jr.job_role_id, sf.sub_feature_master_id
FROM job_role jr
JOIN sub_feature_master sf ON sf.sub_feature_name IN ('View All Tickets', 'Update Ticket')
JOIN feature_master fm ON fm.feature_id = sf.feature_id AND fm.feature_name = 'Grievance'
LEFT JOIN role_subfeature_mapping rsm
       ON rsm.job_role_id = jr.job_role_id AND rsm.sub_feature_master_id = sf.sub_feature_master_id
WHERE rsm.role_feature_map_id IS NULL
  AND (
      LOWER(COALESCE(jr.employee_role, '')) LIKE '%admin%'
      OR LOWER(COALESCE(jr.employee_role, '')) LIKE '%vp%'
      OR LOWER(COALESCE(jr.employee_role, '')) LIKE '%project manager%'
      OR LOWER(COALESCE(jr.name, '')) LIKE '%admin%'
      OR LOWER(COALESCE(jr.name, '')) LIKE '%vp%'
      OR LOWER(COALESCE(jr.name, '')) LIKE '%project manager%'
  );

-- Assigned Tickets queue: job roles whose department is Development or HR (matches app-side department check)
INSERT INTO role_subfeature_mapping (job_role_id, sub_feature_master_id)
SELECT jr.job_role_id, sf.sub_feature_master_id
FROM job_role jr
INNER JOIN department d ON d.dept_id = jr.dept_id
JOIN sub_feature_master sf ON sf.sub_feature_name = 'Assigned Tickets'
JOIN feature_master fm ON fm.feature_id = sf.feature_id AND fm.feature_name = 'Grievance'
LEFT JOIN role_subfeature_mapping rsm
       ON rsm.job_role_id = jr.job_role_id AND rsm.sub_feature_master_id = sf.sub_feature_master_id
WHERE rsm.role_feature_map_id IS NULL
  AND (
      LOWER(TRIM(d.name)) LIKE '%development%'
      OR LOWER(TRIM(d.name)) LIKE '%human resource%'
      OR LOWER(TRIM(d.name)) = 'hr'
  );

-- Grievance issue scenario admin UI/API:
-- - Any job role that looks like admin (name/employee_role), OR whose department name looks like admin (e.g. Super Admin),
-- - OR Development department with VP or Project Manager in role text (either is enough; not both required).
INSERT INTO role_subfeature_mapping (job_role_id, sub_feature_master_id)
SELECT jr.job_role_id, sf.sub_feature_master_id
FROM job_role jr
LEFT JOIN department d ON d.dept_id = jr.dept_id
JOIN sub_feature_master sf ON sf.sub_feature_name = 'Grievance Issue Scenarios'
JOIN feature_master fm ON fm.feature_id = sf.feature_id AND fm.feature_name = 'Grievance'
LEFT JOIN role_subfeature_mapping rsm
       ON rsm.job_role_id = jr.job_role_id AND rsm.sub_feature_master_id = sf.sub_feature_master_id
WHERE rsm.role_feature_map_id IS NULL
  AND (
      LOWER(COALESCE(jr.employee_role, '')) LIKE '%admin%'
      OR LOWER(COALESCE(jr.name, '')) LIKE '%admin%'
      OR LOWER(TRIM(COALESCE(d.name, ''))) LIKE '%admin%'
      OR (
          LOWER(TRIM(COALESCE(d.name, ''))) LIKE '%development%'
          AND (
              LOWER(CONCAT(COALESCE(jr.name, ''), ' ', COALESCE(jr.employee_role, ''))) LIKE '%vp%'
              OR LOWER(CONCAT(COALESCE(jr.name, ''), ' ', COALESCE(jr.employee_role, ''))) LIKE '%project manager%'
          )
      )
  );

COMMIT;
