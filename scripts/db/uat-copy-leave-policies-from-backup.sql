-- Alternative: copy all policies from db_emp_backup_new when leave types share the same IDs (1-5).
-- Prefer this if backup has your full production policy set (17 rows).

USE emp_portal_db;

SET @policy_count = (SELECT COUNT(*) FROM leave_policies_master);
SET @type_match = (
  SELECT COUNT(*) FROM leave_type_master l
  INNER JOIN db_emp_backup_new.leave_type_master b
    ON l.leave_type_master_id = b.leave_type_master_id
   AND l.leave_type_code = b.leave_type_code
);

SELECT @policy_count AS existing_policies, @type_match AS matching_leave_types;

INSERT INTO leave_policies_master (
  carry_forward, carry_forward_value, created_by, description, employment_status,
  expiration_period, expiration_period_value, increment, increment_value,
  leave_application, leave_policy_name, leave_type_master_id,
  locking_period, locking_period_value, locking_value,
  marital_status, maternity_leave_days, maternity_type,
  one_time_leave, one_time_leave_count, one_time_leave_min_count,
  probation, probation_period, updated_by
)
SELECT
  b.carry_forward, b.carry_forward_value, b.created_by, b.description, b.employment_status,
  b.expiration_period, b.expiration_period_value, b.increment, b.increment_value,
  b.leave_application, b.leave_policy_name, b.leave_type_master_id,
  b.locking_period, b.locking_period_value, b.locking_value,
  b.marital_status, b.maternity_leave_days, b.maternity_type,
  b.one_time_leave, b.one_time_leave_count, b.one_time_leave_min_count,
  b.probation, b.probation_period, b.updated_by
FROM db_emp_backup_new.leave_policies_master b
WHERE @policy_count = 0
  AND @type_match >= (SELECT COUNT(*) FROM leave_type_master);

SELECT COUNT(*) AS policies_after_copy FROM leave_policies_master;
