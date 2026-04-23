package com.apmosys.employeeportal.repository;

/**
 * Native SQL for {@code employee_monthly_snapshot} maintenance (MIS + dashboard defaulter flags).
 * Executed by {@link com.apmosys.employeeportal.service.EmployeeMonthlySnapshotCronService} and scheduled jobs.
 */
public final class EmployeeMonthlySnapshotCronSql {

	private EmployeeMonthlySnapshotCronSql() {
	}

	/**
	 * Upserts snapshot rows for the current and previous calendar month (live window), then refreshes defaulter flags.
	 */
	public static final String LIVE_REFRESH_INSERT = ""
			+ "INSERT INTO employee_monthly_snapshot ( "
			+ "    emp_id, employee_team_map_id, project_id, has_client_side_id, is_shadow, billable_type, "
			+ "    snapshot_year, snapshot_month, month_start_date, "
			+ "    expected_days, filled_days_overall, filled_days_client, is_month_closed "
			+ ") "
			+ "SELECT "
			+ "    etm.emp_id, etm.employee_team_map_id, t.project_id, COALESCE(p.has_client_side_id, 0), COALESCE(etm.is_shadow, 0), "
			+ "    CASE "
			+ "        WHEN p.po_project_type = 'TNM' AND etm.is_shadow = 1 THEN 'TNM(Shadow)' "
			+ "        WHEN p.po_project_type = 'Fixed Cost' AND etm.is_shadow = 1 THEN 'Fixed Cost(Shadow)' "
			+ "        WHEN p.po_project_type IS NOT NULL THEN p.po_project_type "
			+ "        ELSE p.internal_project_type "
			+ "    END AS b_type, "
			+ "    YEAR(m.m_start), MONTH(m.m_start), m.m_start, "
			+ "    (SELECT COUNT(DISTINCT DATE_ADD(m.m_start, INTERVAL h.s DAY)) "
			+ "     FROM helper_seq h "
			+ "     WHERE DATE_ADD(m.m_start, INTERVAL h.s DAY) <= LAST_DAY(m.m_start) "
			+ "       AND DATE_ADD(m.m_start, INTERVAL h.s DAY) BETWEEN etm.start_date AND COALESCE(etm.end_date, '2099-12-31') "
			+ "       AND NOT EXISTS ( "
			+ "           SELECT 1 FROM employee_timesheets_new et_ex "
			+ "           JOIN day_type_master_new dtm_ex ON et_ex.day_type_id = dtm_ex.day_type_id "
			+ "           WHERE et_ex.emp_id = etm.emp_id AND et_ex.date = DATE_ADD(m.m_start, INTERVAL h.s DAY) "
			+ "             AND UPPER(dtm_ex.day_type) IN ('LEAVE','CLIENT HOLIDAY','PUBLIC HOLIDAY','APMOSYS HOLIDAY','WEEK OFF','COMP OFF') "
			+ "       ) "
			+ "    ) AS expected, "
			+ "    COALESCE(( "
			+ "        SELECT COUNT(DISTINCT et_i.date) "
			+ "        FROM employee_timesheets_new et_i "
			+ "        JOIN employee_timesheet_activities_mapping_new etam_i ON et_i.timesheet_id = etam_i.timesheet_id "
			+ "        JOIN activities ac_i ON etam_i.activity_id = ac_i.activity_id "
			+ "        JOIN day_type_master_new dtm_i ON et_i.day_type_id = dtm_i.day_type_id "
			+ "        WHERE et_i.emp_id = etm.emp_id AND ac_i.team_id = etm.team_id "
			+ "          AND et_i.date BETWEEN m.m_start AND LAST_DAY(m.m_start) "
			+ "          AND UPPER(dtm_i.day_type) IN ('WORKING', 'NON-WORKING' ,'HALF-DAY WORKING') "
			+ "    ), 0) AS filled_ovr, "
			+ "    CASE WHEN COALESCE(p.has_client_side_id, 0) = 0 THEN 0 ELSE COALESCE(( "
			+ "        SELECT COUNT(DISTINCT tdd.timesheet_id) "
			+ "        FROM timesheet_document_details_new tdd "
			+ "        JOIN employee_timesheets_new et_c ON tdd.timesheet_id = et_c.timesheet_id "
			+ "        LEFT JOIN client_status_master_new csm ON tdd.client_approval_status_id = csm.status_id "
			+ "        WHERE et_c.emp_id = etm.emp_id AND tdd.project_id = t.project_id "
			+ "          AND et_c.date BETWEEN m.m_start AND LAST_DAY(m.m_start) AND tdd.active = TRUE "
			+ "          AND (UPPER(csm.status) = 'APPROVED' AND tdd.final_flag = 1 AND et_c.status != 3) "
			+ "    ), 0) END AS filled_clt, "
			+ "    0 "
			+ "FROM employee_team_mapping etm "
			+ "JOIN teams t ON etm.team_id = t.team_id "
			+ "JOIN projects p ON t.project_id = p.project_id "
			+ "JOIN ( "
			+ "    SELECT DATE_FORMAT(CURDATE(), '%Y-%m-01') AS m_start UNION "
			+ "    SELECT DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y-%m-01') "
			+ ") m ON 1 = 1 "
			+ "WHERE etm.emp_id NOT BETWEEN 1 AND 6 "
			+ "  AND etm.start_date <= LAST_DAY(m.m_start) AND (etm.end_date IS NULL OR etm.end_date >= m.m_start) "
			+ "ON DUPLICATE KEY UPDATE "
			+ "    filled_days_overall = VALUES(filled_days_overall), "
			+ "    filled_days_client = VALUES(filled_days_client), "
			+ "    expected_days = VALUES(expected_days)";

	public static final String LIVE_REFRESH_UPDATE_CURRENT_DEFAULTER = ""
			+ "UPDATE employee_monthly_snapshot "
			+ "SET is_current_defaulter = IF(expected_days = 0 OR expected_days - filled_days_overall >= 2, 1, 0) "
			+ "WHERE snapshot_id > 0";

	public static final String LIVE_REFRESH_UPDATE_DASHBOARD_FLAGS = ""
			+ "UPDATE employee_monthly_snapshot "
			+ "SET is_defaulter_overall = IF(expected_days = 0 OR expected_days - filled_days_overall >= 2, 1, 0), "
			+ "    is_defaulter_client = IF(has_client_side_id = 1 AND (expected_days - filled_days_client >= 2), 1, 0) "
			+ "WHERE is_month_closed = 0";

	public static final String CLOSE_PRIOR_MONTH = ""
			+ "UPDATE employee_monthly_snapshot "
			+ "SET is_month_closed = 1 "
			+ "WHERE month_start_date = DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '%Y-%m-01') "
			+ "  AND is_month_closed = 0";
}
