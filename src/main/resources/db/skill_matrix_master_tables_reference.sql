-- Reference: skill master tables on UAT emp_portal_db (verified 2026-04).
-- Tables are application-owned; use this to align local/other envs — do not drop production data.

-- skill_category_master: category_id (PK AI), category_name (UNIQUE)
-- skills_master: skill_id (PK AI), skill_name, category_id (FK), skill_type ENUM('Required','Optional'),
--   is_active, created_at, updated_at, department_id
-- subskills_master: subskill_id (PK AI), skill_id (FK), subskill_name, created_at, updated_at, is_active
-- skill_domain_master: domain_id (PK AI), domain_name (UNIQUE)
-- skill_subdomain_master: subdomain_id (PK AI), domain_id (FK), subdomain_name, is_active
-- skill_domain_feature_master: feature_id (PK AI), domain_id (FK), subdomain_id (FK NULL), feature_name, is_active
