package com.apmosys.employeeportal.repository;

/**
 * Native SQL for Repeated Offender KPI counts and employee list (same snapshot filters).
 * <p>
 * Parameters: {@code :customFrom}, {@code :customTo}, {@code :isClientSide}, {@code :billableTypes},
 * {@code :employeeStatus}, {@code :threshold} (counts + threshold segment only), {@code :segmentRule}
 * (list only: 1 total applicable, 2 repeated, 3 streak, 4 exact threshold).
 * <p>
 * Table {@code employee_monthly_snapshot} must expose {@code is_defaulter_client} and
 * {@code is_defaulter_overall}.
 */
public final class RepeatedOffenderSnapshotKpiSql {

	private RepeatedOffenderSnapshotKpiSql() {
	}

	/** Shared CTEs: date window, per-emp defaulted month sum, range width. */
	public static final String SNAPSHOT_CTES = ""
			+ "WITH Date_Range AS (\n"
			+ "    SELECT :customFrom AS start_dt, :customTo AS end_dt\n"
			+ "),\n"
			+ "Period_Stats AS (\n"
			+ "    SELECT \n"
			+ "        s.emp_id,\n"
			+ "        SUM(IF(:isClientSide = 1, s.is_defaulter_client, s.is_defaulter_overall)) AS defaulted_months_count\n"
			+ "    FROM employee_monthly_snapshot s\n"
			+ "    JOIN employee e ON s.emp_id = e.emp_id\n"
			+ "    CROSS JOIN Date_Range dr\n"
			+ "    WHERE s.month_start_date >= dr.start_dt \n"
			+ "      AND s.month_start_date <= dr.end_dt\n"
			+ "      AND (:isClientSide = 0 OR s.has_client_side_id = 1)\n"
			+ "      AND (s.billable_type IN (:billableTypes) OR 'All' IN (:billableTypes))\n"
			+ "      AND (:employeeStatus = 'All' OR e.employmentstatus = :employeeStatus)\n"
			+ "      AND s.emp_id NOT BETWEEN 1 AND 6\n"
			+ "    GROUP BY s.emp_id\n"
			+ "),\n"
			+ "Window_Meta AS (\n"
			+ "    SELECT TIMESTAMPDIFF(MONTH, dr.start_dt, dr.end_dt) + 1 AS months_in_range\n"
			+ "    FROM Date_Range dr\n"
			+ ")\n";

	/** One row: total_defaulters, more_than_1_month, streak_count, exactly_n_month. */
	public static final String FOUR_CARD_COUNTS = SNAPSHOT_CTES
			+ "SELECT \n"
			+ "    COUNT(DISTINCT CASE WHEN ps.defaulted_months_count >= 1 THEN ps.emp_id END) AS total_defaulters,\n"
			+ "    COUNT(DISTINCT CASE WHEN ps.defaulted_months_count >= 2 THEN ps.emp_id END) AS more_than_1_month,\n"
			+ "    COUNT(DISTINCT CASE WHEN ps.defaulted_months_count = (SELECT months_in_range FROM Window_Meta) THEN ps.emp_id END) AS streak_count,\n"
			+ "    COUNT(DISTINCT CASE WHEN ps.defaulted_months_count = :threshold THEN ps.emp_id END) AS exactly_n_month\n"
			+ "FROM Period_Stats ps";

	/**
	 * Inner list (no ORDER/LIMIT): Filtered emps + identity columns. Append ORDER BY / LIMIT for paging.
	 */
	public static final String SNAPSHOT_EMPLOYEE_LIST_INNER = SNAPSHOT_CTES
			+ ", Filtered AS (\n"
			+ "    SELECT ps.emp_id, ps.defaulted_months_count\n"
			+ "    FROM Period_Stats ps\n"
			+ "    CROSS JOIN Window_Meta wm\n"
			+ "    WHERE (:segmentRule = 1 AND ps.defaulted_months_count >= 1)\n"
			+ "       OR (:segmentRule = 2 AND ps.defaulted_months_count >= 2)\n"
			+ "       OR (:segmentRule = 3 AND ps.defaulted_months_count = wm.months_in_range)\n"
			+ "       OR (:segmentRule = 4 AND ps.defaulted_months_count = :threshold)\n"
			+ ")\n"
			+ "SELECT DISTINCT\n"
			+ "    e.emp_id,\n"
			+ "    CASE\n"
			+ "        WHEN e.is_apmosys_product = 'true' OR e.is_apmosys_product = '1' OR e.is_apmosys_product = 1\n"
			+ "            THEN CONCAT('AP-', CAST(e.employeement_id AS CHAR))\n"
			+ "        ELSE CONCAT('A-', CAST(e.employeement_id AS CHAR))\n"
			+ "    END AS employment_id,\n"
			+ "    e.name AS employee_name,\n"
			+ "    COALESCE(d.name, '') AS department,\n"
			+ "    e.employmentstatus AS employment_status,\n"
			+ "    fe.defaulted_months_count\n"
			+ "FROM Filtered fe\n"
			+ "JOIN employee e ON e.emp_id = fe.emp_id\n"
			+ "LEFT JOIN job_role jr ON e.job_role_id = jr.job_role_id\n"
			+ "LEFT JOIN department d ON jr.dept_id = d.dept_id\n";

	/** Count rows for list pagination (same as Filtered cardinality). */
	public static final String SNAPSHOT_EMPLOYEE_COUNT = SNAPSHOT_CTES
			+ ", Filtered AS (\n"
			+ "    SELECT ps.emp_id, ps.defaulted_months_count\n"
			+ "    FROM Period_Stats ps\n"
			+ "    CROSS JOIN Window_Meta wm\n"
			+ "    WHERE (:segmentRule = 1 AND ps.defaulted_months_count >= 1)\n"
			+ "       OR (:segmentRule = 2 AND ps.defaulted_months_count >= 2)\n"
			+ "       OR (:segmentRule = 3 AND ps.defaulted_months_count = wm.months_in_range)\n"
			+ "       OR (:segmentRule = 4 AND ps.defaulted_months_count = :threshold)\n"
			+ ")\n"
			+ "SELECT COUNT(*) FROM Filtered fe\n";

	/**
	 * Project / team rows overlapping the RO range for the given employees (bind {@code :ids}).
	 * Employees are already restricted by snapshot KPI filters; this list mirrors Employee Overview by
	 * returning all overlapping mappings (client dashboard still requires {@code has_client_side_id}).
	 */
	public static final String SNAPSHOT_PROJECT_MAPPINGS = ""
			+ "SELECT etm.emp_id,\n"
			+ "       p.project_name,\n"
			+ "       CASE\n"
			+ "         WHEN COALESCE(etm.is_shadow, 0) = 0 AND p.po_project_type IS NOT NULL THEN p.po_project_type\n"
			+ "         WHEN COALESCE(p.internal_project_type, '') <> '' THEN p.internal_project_type\n"
			+ "         WHEN p.po_project_type = 'TNM' AND COALESCE(etm.is_shadow, 0) = 1 THEN 'TNM(Shadow)'\n"
			+ "         WHEN p.po_project_type = 'Fixed Cost' AND COALESCE(etm.is_shadow, 0) = 1 THEN 'Fixed Cost(Shadow)'\n"
			+ "         ELSE COALESCE(p.po_project_type, '')\n"
			+ "       END AS billable_display,\n"
			+ "       e.employmentstatus AS employment_status,\n"
			+ "       COALESCE(pma.pm_names, 'NA') AS manager_names,\n"
			+ "       t.team_name,\n"
			+ "       CONCAT(COALESCE(t.team_name, ''), ' / ', COALESCE(p.project_name, '')) AS mapping_label\n"
			+ "FROM employee_team_mapping etm\n"
			+ "INNER JOIN teams t ON t.team_id = etm.team_id\n"
			+ "INNER JOIN projects p ON p.project_id = t.project_id\n"
			+ "INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
			+ "LEFT JOIN (\n"
			+ "    SELECT pm.project_id, GROUP_CONCAT(DISTINCT e2.name ORDER BY e2.name SEPARATOR ', ') AS pm_names\n"
			+ "    FROM project_manager_mapping pm\n"
			+ "    INNER JOIN employee e2 ON e2.emp_id = pm.project_manager_id\n"
			+ "    GROUP BY pm.project_id\n"
			+ ") pma ON pma.project_id = p.project_id\n"
			+ "CROSS JOIN (SELECT :customFrom AS range_start, :customTo AS range_end) dr\n"
			+ "WHERE etm.emp_id IN (:ids)\n"
			+ "  AND DATE(etm.start_date) <= dr.range_end\n"
			+ "  AND (etm.end_date IS NULL OR DATE(etm.end_date) >= dr.range_start)\n"
			+ "  AND (:isClientSide = 0 OR COALESCE(p.has_client_side_id, 0) = 1)\n"
			+ "  AND (:employeeStatus = 'All' OR e.employmentstatus = :employeeStatus)\n"
			+ "  AND e.emp_id NOT BETWEEN 1 AND 6\n"
			+ "ORDER BY e.name, p.project_name, t.team_name\n";

	/**
	 * Appended after {@code ) ro_list WHERE 1=1 } on the snapshot employee list + count.
	 * Bind via {@code RepeatedOffenderRepository#bindRoListGridFilters}.
	 * <p>
	 * Filters use {@code LOCATE(LOWER(:needle), LOWER(haystack))} (substring, case-insensitive) so pasted values with
	 * {@code _}, {@code %}, or {@code |} match without LIKE wildcards or ESCAPE quirks. Billable matches the same
	 * CASE-derived label as {@link #SNAPSHOT_PROJECT_MAPPINGS} / the grid (single {@code :rofBt} bind).
	 */
	public static final String RO_LIST_GRID_FILTER = ""
			+ " AND (:rofUseEmpId = 0 OR LOCATE(LOWER(:rofEmpId), LOWER(COALESCE(ro_list.employment_id, ''))) > 0) "
			+ " AND (:rofUseEmpName = 0 OR LOCATE(LOWER(:rofEmpName), LOWER(COALESCE(ro_list.employee_name, ''))) > 0) "
			+ " AND (:rofUseDept = 0 OR LOCATE(LOWER(:rofDept), LOWER(COALESCE(ro_list.department, ''))) > 0) "
			+ " AND (:rofUseEmpSt = 0 OR LOCATE(LOWER(:rofEmpSt), LOWER(COALESCE(ro_list.employment_status, ''))) > 0) "
			+ " AND ( "
			+ "   (:rofUsePn = 0 AND :rofUseBt = 0 AND :rofUseMgr = 0 AND :rofUseMap = 0 AND :rofUseTeam = 0) OR EXISTS ( "
			+ "     SELECT 1 FROM employee_team_mapping etm "
			+ "     INNER JOIN teams t ON t.team_id = etm.team_id "
			+ "     INNER JOIN projects p ON p.project_id = t.project_id "
			+ "     INNER JOIN employee ex ON ex.emp_id = etm.emp_id "
			+ "     LEFT JOIN ( "
			+ "       SELECT pm.project_id, GROUP_CONCAT(DISTINCT e2.name ORDER BY e2.name SEPARATOR ', ') AS pm_names "
			+ "       FROM project_manager_mapping pm "
			+ "       INNER JOIN employee e2 ON e2.emp_id = pm.project_manager_id "
			+ "       GROUP BY pm.project_id "
			+ "     ) pma ON pma.project_id = p.project_id "
			+ "     CROSS JOIN (SELECT :customFrom AS drf_s, :customTo AS drf_e) drf "
			+ "     WHERE etm.emp_id = ro_list.emp_id "
			+ "       AND DATE(etm.start_date) <= drf.drf_e "
			+ "       AND (etm.end_date IS NULL OR DATE(etm.end_date) >= drf.drf_s) "
			+ "       AND (:isClientSide = 0 OR COALESCE(p.has_client_side_id, 0) = 1) "
			+ "       AND (:employeeStatus = 'All' OR ex.employmentstatus = :employeeStatus) "
			+ "       AND ex.emp_id NOT BETWEEN 1 AND 6 "
			+ "       AND (:rofUsePn = 0 OR LOCATE(LOWER(:rofPn), LOWER(COALESCE(p.project_name, ''))) > 0) "
			+ "       AND (:rofUseBt = 0 OR LOCATE(LOWER(:rofBt), LOWER(COALESCE((CASE "
			+ "               WHEN COALESCE(etm.is_shadow, 0) = 0 AND p.po_project_type IS NOT NULL THEN p.po_project_type "
			+ "               WHEN COALESCE(p.internal_project_type, '') <> '' THEN p.internal_project_type "
			+ "               WHEN p.po_project_type = 'TNM' AND COALESCE(etm.is_shadow, 0) = 1 THEN 'TNM(Shadow)' "
			+ "               WHEN p.po_project_type = 'Fixed Cost' AND COALESCE(etm.is_shadow, 0) = 1 THEN 'Fixed Cost(Shadow)' "
			+ "               ELSE COALESCE(p.po_project_type, '') "
			+ "             END), ''))) > 0) "
			+ "       AND (:rofUseMgr = 0 OR LOCATE(LOWER(:rofMgr), LOWER(COALESCE(pma.pm_names, ''))) > 0) "
			+ "       AND (:rofUseMap = 0 OR LOCATE(LOWER(:rofMap), LOWER(CONCAT(COALESCE(t.team_name, ''), ' / ', COALESCE(p.project_name, '')))) > 0) "
			+ "       AND (:rofUseTeam = 0 OR LOCATE(LOWER(:rofTeam), LOWER(COALESCE(t.team_name, ''))) > 0) "
			+ "   ) "
			+ " ) ";
}
