-- UAT db_emp_backup_new: grant "Approve Reimbursement" tab for Sanjana Rane
-- Employee: emp_id 1570, job_role_id 206 (Assistant Manager - Accounts Finance)
-- Sub-feature: 201 (Approve Reimbursement) under feature Reimbursement (61)

INSERT INTO role_subfeature_mapping (job_role_id, sub_feature_master_id)
SELECT 206, 201
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM role_subfeature_mapping
  WHERE job_role_id = 206 AND sub_feature_master_id = 201
);
