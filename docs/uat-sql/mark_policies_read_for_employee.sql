-- UAT: allow navigation past HR Policies gate (AuthGuard -> /user-policies).
-- Login sets policyReadConsent when any read_enabled policy lacks policy_read_employee_response.
-- isAllPolicyRead passes when count(read_enabled policies) <= count(all reads for emp_id).

-- Sibasisha Mishra (Travel Admin) — emp_id 475
SET @empId := 475;

INSERT INTO policy_read_employee_response (emp_id, policyid)
SELECT @empId, h.policyid
FROM hrpolicies h
WHERE LOWER(TRIM(COALESCE(h.read_enabled, ''))) IN ('true', 'y', 'yes', '1')
  AND NOT EXISTS (
    SELECT 1 FROM policy_read_employee_response r
    WHERE r.emp_id = @empId AND r.policyid = h.policyid
  );

-- Verify (required count should be <= emp_reads)
SELECT
  (SELECT COUNT(*) FROM hrpolicies
   WHERE LOWER(TRIM(COALESCE(read_enabled, ''))) IN ('true', 'y', 'yes', '1')) AS policies_required,
  (SELECT COUNT(*) FROM policy_read_employee_response WHERE emp_id = @empId) AS emp_policy_reads;
