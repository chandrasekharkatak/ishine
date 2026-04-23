package com.apmosys.employeeportal.repository;

/**
 * Legacy native SQL for the non-snapshot "total applicable" employee list (rarely used).
 * <p>
 * Kept as compile-time constants (same pattern as {@link RepeatedOffenderSnapshotKpiSql}) so the repository
 * does not read classpath {@code .sql} files at runtime.
 */
public final class RepeatedOffenderLegacyApplicableSql {

	private RepeatedOffenderLegacyApplicableSql() {
	}

	/** Non-client dashboard; binds {@code RepeatedOffenderRepository#bindApplicableDetailParameters} (non-client). */
	public static final String TOTAL_APPLICABLE_DETAIL = ""
			+ "SELECT "
			+ "    e.emp_id, "
			+ "    CASE "
			+ "        WHEN e.is_apmosys_product = 'true' OR e.is_apmosys_product = '1' OR e.is_apmosys_product = 1 "
			+ "            THEN CONCAT('AP-', CAST(e.employeement_id AS CHAR)) "
			+ "        ELSE CONCAT('A-', CAST(e.employeement_id AS CHAR)) "
			+ "    END AS employment_id, "
			+ "    e.name AS employee_name, "
			+ "    COALESCE(d.name, '') AS department "
			+ "FROM employee e "
			+ "LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id "
			+ "LEFT JOIN department d ON jr.dept_id = d.dept_id "
			+ "WHERE e.emp_id = -1 "
			+ "  AND :year = :year "
			+ "  AND :month = :month "
			+ "  AND :emp_id = :emp_id "
			+ "  AND (:employeeActive = 'All' OR e.employmentstatus = :employeeActive) "
			+ "  AND ('All' IN (:billableType) OR e.emp_id = e.emp_id) "
			+ "ORDER BY e.name";

	/** Client dashboard; same shape plus {@code clientSideFilter} and {@code multiPOs}. */
	public static final String TOTAL_APPLICABLE_DETAIL_CLIENT = ""
			+ "SELECT "
			+ "    e.emp_id, "
			+ "    CASE "
			+ "        WHEN e.is_apmosys_product = 'true' OR e.is_apmosys_product = '1' OR e.is_apmosys_product = 1 "
			+ "            THEN CONCAT('AP-', CAST(e.employeement_id AS CHAR)) "
			+ "        ELSE CONCAT('A-', CAST(e.employeement_id AS CHAR)) "
			+ "    END AS employment_id, "
			+ "    e.name AS employee_name, "
			+ "    COALESCE(d.name, '') AS department "
			+ "FROM employee e "
			+ "LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id "
			+ "LEFT JOIN department d ON jr.dept_id = d.dept_id "
			+ "WHERE e.emp_id = -1 "
			+ "  AND :year = :year "
			+ "  AND :month = :month "
			+ "  AND :emp_id = :emp_id "
			+ "  AND (:employeeActive = 'All' OR e.employmentstatus = :employeeActive) "
			+ "  AND ('All' IN (:billableType) OR e.emp_id = e.emp_id) "
			+ "  AND (:clientSideFilter = :clientSideFilter OR 1 = 1) "
			+ "  AND (:multiPOs = :multiPOs OR 1 = 1) "
			+ "ORDER BY e.name";
}
