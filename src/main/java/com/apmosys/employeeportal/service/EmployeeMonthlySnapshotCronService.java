package com.apmosys.employeeportal.service;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.repository.EmployeeMonthlySnapshotCronSql;

import lombok.extern.slf4j.Slf4j;

/**
 * Runs native maintenance SQL on {@code employee_monthly_snapshot} (live MIS refresh + month close).
 */
@Slf4j
@Service
public class EmployeeMonthlySnapshotCronService {

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Job 1: upsert current + prior month snapshots, then recompute defaulter flags for open months.
	 *
	 * @return row counts per step (MySQL affected rows semantics)
	 */
	@Transactional
	public Map<String, Integer> runLiveRefreshJob() {
		Map<String, Integer> out = new LinkedHashMap<>();
		int insert = entityManager.createNativeQuery(EmployeeMonthlySnapshotCronSql.LIVE_REFRESH_INSERT).executeUpdate();
		out.put("upsertRows", Integer.valueOf(insert));
		int cur = entityManager.createNativeQuery(EmployeeMonthlySnapshotCronSql.LIVE_REFRESH_UPDATE_CURRENT_DEFAULTER)
				.executeUpdate();
		out.put("isCurrentDefaulterRows", Integer.valueOf(cur));
		int dash = entityManager.createNativeQuery(EmployeeMonthlySnapshotCronSql.LIVE_REFRESH_UPDATE_DASHBOARD_FLAGS)
				.executeUpdate();
		out.put("dashboardFlagRows", Integer.valueOf(dash));
		log.info("employee_monthly_snapshot live refresh completed: {}", out);
		return out;
	}

	/**
	 * Job 2: freeze prior calendar month (set {@code is_month_closed}).
	 */
	@Transactional
	public Map<String, Integer> runCloseMonthJob() {
		Map<String, Integer> out = new LinkedHashMap<>();
		int closed = entityManager.createNativeQuery(EmployeeMonthlySnapshotCronSql.CLOSE_PRIOR_MONTH).executeUpdate();
		out.put("closedRows", Integer.valueOf(closed));
		log.info("employee_monthly_snapshot close-month completed: {}", out);
		return out;
	}
}
