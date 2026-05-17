-- Seed leave_policies_master for emp_portal_db (UAT)
-- leave_type_master_id: 1=PL, 2=CL, 3=LWP, 4=CO, 5=ML

USE emp_portal_db;

SET @policy_count = (SELECT COUNT(*) FROM leave_policies_master);
SELECT @policy_count AS existing_policy_count;

INSERT INTO leave_policies_master (
  leave_policy_name, leave_type_master_id, employment_status, description,
  leave_application, increment, increment_value,
  one_time_leave, one_time_leave_min_count, one_time_leave_count,
  carry_forward, carry_forward_value,
  expiration_period, expiration_period_value,
  locking_period, locking_period_value, locking_value,
  probation, probation_period,
  marital_status, maternity_type, maternity_leave_days,
  created_by
)
SELECT
  v.leave_policy_name, v.leave_type_master_id, v.employment_status, v.description,
  v.leave_application, v.increment, v.increment_value,
  v.one_time_leave, v.one_time_leave_min_count, v.one_time_leave_count,
  v.carry_forward, v.carry_forward_value,
  v.expiration_period, v.expiration_period_value,
  v.locking_period, v.locking_period_value, v.locking_value,
  v.probation, v.probation_period,
  v.marital_status, v.maternity_type, v.maternity_leave_days,
  v.created_by
FROM (
  SELECT 'CL-Confirmed' AS leave_policy_name, CAST(2 AS SIGNED) AS leave_type_master_id, 'Confirmed' AS employment_status,
    'It defines "confirmed employee" Casual Leave policy' AS description,
    'Yes' AS leave_application, 'Yes' AS increment, 1.0 AS increment_value,
    'NA' AS one_time_leave, 0.5 AS one_time_leave_min_count, 6.0 AS one_time_leave_count,
    'No' AS carry_forward, CAST(NULL AS SIGNED) AS carry_forward_value,
    'NA' AS expiration_period, CAST(NULL AS SIGNED) AS expiration_period_value,
    'NA' AS locking_period, 180 AS locking_period_value, 6 AS locking_value,
    'NA' AS probation, CAST(NULL AS SIGNED) AS probation_period,
    CAST(NULL AS CHAR) AS marital_status, CAST(NULL AS CHAR) AS maternity_type, CAST(NULL AS SIGNED) AS maternity_leave_days,
    CAST(1 AS SIGNED) AS created_by
  UNION ALL SELECT 'PL- Confirmed', 1, 'Confirmed', 'It defines "confirmed employee" Paid Leave policy.',
    'Yes', 'Yes', 1.5, 'Yes', 0.5, 15.0, 'Yes', 45, 'NA', NULL, 'NA', 365, 18, 'NA', NULL, NULL, NULL, NULL, 1
  UNION ALL SELECT 'CO-Confirmed', 4, 'Confirmed', 'It defines "confirmed employee" Compensatory Off policy',
    'Yes', 'NA', NULL, 'NA', 0.5, 1.0, 'NA', NULL, 'Yes', 15, 'NA', NULL, NULL, 'NA', NULL, NULL, NULL, NULL, 1
  UNION ALL SELECT 'LWP-Confirmed', 3, 'Confirmed', 'It defines "Confirmed employee" Leave Without Pay policy',
    'Yes', 'NA', NULL, 'NA', NULL, NULL, 'NA', NULL, 'NA', NULL, 'NA', NULL, NULL, 'NA', NULL, NULL, NULL, NULL, 1
  UNION ALL SELECT 'CL-Probation', 2, 'Probation', 'Casual Leave policy for probation employees',
    'Yes', 'Yes', 1.0, 'NA', 0.5, 6.0, 'No', NULL, 'NA', NULL, 'NA', 180, 6, 'NA', NULL, NULL, NULL, NULL, 1
  UNION ALL SELECT 'PL-Probation', 1, 'Probation', 'Paid Leave policy for probation employees',
    'Yes', 'Yes', 1.0, 'Yes', 0.5, 6.0, 'No', NULL, 'NA', NULL, 'NA', 180, 6, 'NA', NULL, NULL, NULL, NULL, 1
  UNION ALL SELECT 'CO-Probation', 4, 'Probation', 'Compensatory Off policy for probation employees',
    'Yes', 'NA', NULL, 'NA', 0.5, 1.0, 'NA', NULL, 'Yes', 15, 'NA', NULL, NULL, 'NA', NULL, NULL, NULL, NULL, 1
  UNION ALL SELECT 'LWP-Probation', 3, 'Probation', 'Leave Without Pay policy for probation employees',
    'Yes', 'NA', NULL, 'NA', NULL, NULL, 'NA', NULL, 'NA', NULL, 'NA', NULL, NULL, 'NA', NULL, NULL, NULL, NULL, 1
  UNION ALL SELECT 'ML-FullMaternity-Confirmed', 5, 'Confirmed', 'Full maternity leave for confirmed married employees',
    'Yes', 'NA', NULL, 'NA', NULL, NULL, 'NA', NULL, 'NA', NULL, 'NA', NULL, NULL, 'NA', NULL, 'married', 'fullMaternity', 182, 1
  UNION ALL SELECT 'ML-Adoption-Confirmed', 5, 'Confirmed', 'Adoption leave for confirmed married employees',
    'Yes', 'NA', NULL, 'NA', NULL, NULL, 'NA', NULL, 'NA', NULL, 'NA', NULL, NULL, 'NA', NULL, 'married', 'adoption', 84, 1
  UNION ALL SELECT 'ML-Miscarriage-Confirmed', 5, 'Confirmed', 'Miscarriage leave for confirmed married employees',
    'Yes', 'NA', NULL, 'NA', NULL, NULL, 'NA', NULL, 'NA', NULL, 'NA', NULL, NULL, 'NA', NULL, 'married', 'miscarriage', 42, 1
) AS v
WHERE @policy_count = 0;

SELECT leave_policy_master_id, leave_policy_name, leave_type_master_id, employment_status, leave_application
FROM leave_policies_master
ORDER BY employment_status, leave_type_master_id;

SELECT ltm.leave_type_code, ltm.leave_type, lpm.leave_policy_name
FROM leave_type_master ltm
INNER JOIN leave_policies_master lpm ON lpm.leave_type_master_id = ltm.leave_type_master_id
WHERE lpm.leave_application = 'Yes'
  AND lpm.employment_status = 'Confirmed'
  AND ltm.gender = 'All';
