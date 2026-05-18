-- Skill Matrix submit-for-review: persist optional domain / subdomain / feature per project row.
-- Run on UAT emp_portal_db after skill_domain_master tables exist (see skill_matrix_domain_stack_all_tables.sql).
-- Safe to run once; if columns already exist, skip or comment out.

ALTER TABLE skillmatrix_assessment_project
  ADD COLUMN domain_specific TINYINT(1) NOT NULL DEFAULT 0
    COMMENT '1 when employee mapped this project to skill domain master'
    AFTER is_included,
  ADD COLUMN skill_domain_id INT NULL COMMENT 'skill_domain_master.domain_id' AFTER domain_specific,
  ADD COLUMN skill_subdomain_id INT NULL COMMENT 'skill_subdomain_master.subdomain_id' AFTER skill_domain_id,
  ADD COLUMN skill_domain_feature_id INT NULL COMMENT 'skill_domain_feature_master.feature_id' AFTER skill_subdomain_id;

ALTER TABLE skillmatrix_assessment_project
  ADD KEY idx_sm_project_skill_domain (skill_domain_id),
  ADD KEY idx_sm_project_skill_subdomain (skill_subdomain_id),
  ADD KEY idx_sm_project_skill_feature (skill_domain_feature_id);

ALTER TABLE skillmatrix_assessment_project
  ADD CONSTRAINT fk_sm_proj_skill_domain
    FOREIGN KEY (skill_domain_id) REFERENCES skill_domain_master (domain_id),
  ADD CONSTRAINT fk_sm_proj_skill_subdomain
    FOREIGN KEY (skill_subdomain_id) REFERENCES skill_subdomain_master (subdomain_id),
  ADD CONSTRAINT fk_sm_proj_skill_domain_feature
    FOREIGN KEY (skill_domain_feature_id) REFERENCES skill_domain_feature_master (feature_id);
