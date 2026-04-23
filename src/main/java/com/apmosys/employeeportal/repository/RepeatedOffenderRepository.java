package com.apmosys.employeeportal.repository;

import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.repeatedoffender.RepeatedOffenderDashboardRequest;
import com.apmosys.employeeportal.dto.repeatedoffender.RepeatedOffenderEmployeeRowPayload;
import com.apmosys.employeeportal.dto.repeatedoffender.RepeatedOffenderProjectMappingPayload;
import com.apmosys.employeeportal.dto.repeatedoffender.RepeatedOffenderSummaryPayload;

import lombok.extern.slf4j.Slf4j;

/**
 * Data access for the Repeated Offender HR dashboard.
 * <p>
 * KPI counts and metric-card employee lists use {@link RepeatedOffenderSnapshotKpiSql} (same filters on
 * {@code employee_monthly_snapshot}). Legacy applicable list SQL lives in {@link RepeatedOffenderLegacyApplicableSql}
 * (constants, no classpath {@code .sql} files).
 */
@Slf4j
@Repository
public class RepeatedOffenderRepository {

	@PersistenceContext
	private EntityManager entityManager;

	/** Native SQL listing employees counted in total applicable (non-client). */
	private final String applicableEmployeesDetailSql = RepeatedOffenderLegacyApplicableSql.TOTAL_APPLICABLE_DETAIL;

	/** Native SQL listing employees counted in total applicable (client-side projects). */
	private final String applicableEmployeesDetailClientSql = RepeatedOffenderLegacyApplicableSql.TOTAL_APPLICABLE_DETAIL_CLIENT;

	/**
	 * Aggregates for the RO KPI cards from {@code employee_monthly_snapshot}. On failure
	 * (missing table, etc.) returns zero KPIs with month labels from the request range.
	 */
	public RepeatedOffenderSummaryPayload fetchSummary(RepeatedOffenderDashboardRequest request) {
		RepeatedOffenderSummaryPayload payload = RepeatedOffenderSummaryPayload.emptyStub();
		payload.setMonthLabels(buildMonthLabels(request));
		tryPopulateSnapshotKpis(request, payload);
		return payload;
	}

	private void tryPopulateSnapshotKpis(RepeatedOffenderDashboardRequest request, RepeatedOffenderSummaryPayload payload) {
		if (request.getRangeStartYear() == null || request.getRangeStartMonth() == null
				|| request.getRangeEndYear() == null || request.getRangeEndMonth() == null) {
			return;
		}
		try {
			Session session = entityManager.unwrap(Session.class);
			NativeQuery<?> q = session.createNativeQuery(RepeatedOffenderSnapshotKpiSql.FOUR_CARD_COUNTS);
			bindSnapshotKpiParameters(q, request);
			List<?> rows = q.getResultList();
			if (rows.isEmpty()) {
				return;
			}
			Object raw = rows.get(0);
			Object[] row = unwrapSnapshotRow(raw);
			if (row == null || row.length < 4) {
				log.warn("RO snapshot KPI: unexpected result shape {}", raw == null ? "null" : raw.getClass().getName());
				return;
			}
			long totalDefaulters = toLong(row[0]);
			long repeated = toLong(row[1]);
			long streak = toLong(row[2]);
			long exactN = toLong(row[3]);
			payload.setTotalApplicable(totalDefaulters);
			payload.setRepeatedOffenders(repeated);
			payload.setAllMonthsStreak(streak);
			payload.setThresholdBucket(exactN);
			String pct = totalDefaulters > 0 ? String.format(Locale.US, "%.1f", (100.0 * repeated) / totalDefaulters)
					: repeated > 0 ? "100.0" : "0";
			payload.setRepeatedPct(pct);
		} catch (Exception ex) {
			log.warn("RO snapshot KPI query failed (check employee_monthly_snapshot and columns): {}", ex.getMessage());
		}
	}

	private static long toLong(Object v) {
		if (v == null) {
			return 0L;
		}
		if (v instanceof Number) {
			return ((Number) v).longValue();
		}
		return Long.parseLong(v.toString());
	}

	/** Native row is normally {@code Object[]} (one row, four columns). */
	private static Object[] unwrapSnapshotRow(Object raw) {
		if (raw instanceof Object[]) {
			return (Object[]) raw;
		}
		return null;
	}

	private void bindSnapshotKpiParameters(NativeQuery<?> nq, RepeatedOffenderDashboardRequest request) {
		Date customFrom = Date.valueOf(LocalDate.of(request.getRangeStartYear(), request.getRangeStartMonth(), 1));
		Date customTo = Date.valueOf(YearMonth.of(request.getRangeEndYear(), request.getRangeEndMonth()).atEndOfMonth());
		nq.setParameter("customFrom", customFrom);
		nq.setParameter("customTo", customTo);
		nq.setParameter("isClientSide", Boolean.TRUE.equals(request.getClientDashboard()) ? 1 : 0);
		nq.setParameterList("billableTypes", normalizeBillableTypes(request.getBillableTypes()));
		String empSt = request.getEmployeeActive() != null ? request.getEmployeeActive() : "All";
		nq.setParameter("employeeStatus", empSt);
		int th = request.getDefaultedThreshold() != null && request.getDefaultedThreshold() > 0 ? request.getDefaultedThreshold() : 1;
		nq.setParameter("threshold", th);
	}

	/** Date window + client / employment / billable chip filters for {@link RepeatedOffenderSnapshotKpiSql#SNAPSHOT_PROJECT_MAPPINGS} only. */
	private void bindSnapshotProjectMappingParameters(NativeQuery<?> nq, RepeatedOffenderDashboardRequest request) {
		Date customFrom = Date.valueOf(LocalDate.of(request.getRangeStartYear(), request.getRangeStartMonth(), 1));
		Date customTo = Date.valueOf(YearMonth.of(request.getRangeEndYear(), request.getRangeEndMonth()).atEndOfMonth());
		nq.setParameter("customFrom", customFrom);
		nq.setParameter("customTo", customTo);
		nq.setParameter("isClientSide", Boolean.TRUE.equals(request.getClientDashboard()) ? 1 : 0);
		String empSt = request.getEmployeeActive() != null ? request.getEmployeeActive() : "All";
		nq.setParameter("employeeStatus", empSt);
		nq.setParameterList("billableTypes", normalizeBillableTypes(request.getBillableTypes()));
	}

	/**
	 * Paginated employee rows for the selected metric segment (snapshot SQL aligned with KPI cards).
	 */
	public List<RepeatedOffenderEmployeeRowPayload> fetchEmployeeRows(RepeatedOffenderDashboardRequest request) {
		List<String> monthLabels = buildMonthLabels(request);
		if (isSnapshotListSegment(request)) {
			return fetchSnapshotEmployeeRows(request);
		}
		if (!isLegacyApplicableListSegment(request)) {
			return Collections.emptyList();
		}
		String detailSql = applicableDetailSqlFor(request);
		if (detailSql == null || detailSql.isBlank()) {
			return Collections.emptyList();
		}
		String sqlBase = stripTrailingOrderBy(detailSql);
		int offset = Math.max(0, (request.getPage() - 1) * request.getSize());
		String pagedSql = "SELECT * FROM (" + sqlBase + ") ro_inner ORDER BY ro_inner.employee_name LIMIT "
				+ Integer.toString(request.getSize()) + " OFFSET " + Integer.toString(offset);

		Session session = entityManager.unwrap(Session.class);
		NativeQuery<Object[]> query = session.createNativeQuery(pagedSql);
		bindApplicableDetailParameters(query, request);

		List<Object[]> rows = query.getResultList();
		List<RepeatedOffenderEmployeeRowPayload> out = new ArrayList<>();
		for (Object[] row : rows) {
			out.add(mapApplicableRow(row, monthLabels));
		}
		return out;
	}

	/**
	 * Total rows for pagination (same segment + filters as {@link #fetchEmployeeRows}).
	 */
	public long countEmployeeRows(RepeatedOffenderDashboardRequest request) {
		if (isSnapshotListSegment(request)) {
			return countSnapshotEmployeeRows(request);
		}
		if (!isLegacyApplicableListSegment(request)) {
			return 0L;
		}
		String detailSql = applicableDetailSqlFor(request);
		if (detailSql == null || detailSql.isBlank()) {
			return 0L;
		}
		String sqlBase = stripTrailingOrderBy(detailSql);
		String countSql = "SELECT COUNT(*) FROM (" + sqlBase + ") ro_applicable_cnt";
		Session session = entityManager.unwrap(Session.class);
		NativeQuery<?> query = session.createNativeQuery(countSql);
		bindApplicableDetailParameters(query, request);
		Object single = query.getSingleResult();
		if (single == null) {
			return 0L;
		}
		if (single instanceof Number) {
			return ((Number) single).longValue();
		}
		return Long.parseLong(single.toString());
	}

	private static boolean isSnapshotListSegment(RepeatedOffenderDashboardRequest request) {
		String seg = request.getTableSegment();
		if (seg == null || seg.isBlank()) {
			return true;
		}
		String u = seg.trim().toUpperCase(Locale.ROOT);
		return "ALL".equals(u) || "TOTAL_APPLICABLE".equals(u) || "REPEATED_OFFENDERS".equals(u)
				|| "ALL_MONTHS_STREAK".equals(u) || "THRESHOLD_EXACT".equals(u);
	}

	private static boolean isLegacyApplicableListSegment(RepeatedOffenderDashboardRequest request) {
		String seg = request.getTableSegment();
		if (seg == null || seg.isBlank()) {
			return true;
		}
		String u = seg.trim().toUpperCase(Locale.ROOT);
		return "ALL".equals(u) || "TOTAL_APPLICABLE".equals(u);
	}

	private static int snapshotSegmentRule(RepeatedOffenderDashboardRequest request) {
		String seg = request.getTableSegment();
		if (seg == null || seg.isBlank()) {
			return 1;
		}
		switch (seg.trim().toUpperCase(Locale.ROOT)) {
		case "REPEATED_OFFENDERS":
			return 2;
		case "ALL_MONTHS_STREAK":
			return 3;
		case "THRESHOLD_EXACT":
			return 4;
		case "ALL":
		case "TOTAL_APPLICABLE":
		default:
			return 1;
		}
	}

	private List<RepeatedOffenderEmployeeRowPayload> fetchSnapshotEmployeeRows(RepeatedOffenderDashboardRequest request) {
		if (request.getRangeStartYear() == null || request.getRangeStartMonth() == null
				|| request.getRangeEndYear() == null || request.getRangeEndMonth() == null) {
			return Collections.emptyList();
		}
		try {
			int offset = Math.max(0, (request.getPage() - 1) * request.getSize());
			int limit = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 20;
			String pagedSql = buildRoSnapshotPagedSql(request, offset, limit);
			Session session = entityManager.unwrap(Session.class);
			NativeQuery<Object[]> query = session.createNativeQuery(pagedSql);
			bindSnapshotListParameters(query, request);
			bindRoListGridFilters(query, request);
			List<Object[]> rows = query.getResultList();
			List<RepeatedOffenderEmployeeRowPayload> out = new ArrayList<>();
			for (Object[] row : rows) {
				out.add(mapSnapshotListRow(row));
			}
			hydrateProjectMappings(out, request);
			return out;
		} catch (Exception ex) {
			log.error("RO snapshot list query failed", ex);
			return Collections.emptyList();
		}
	}

	private long countSnapshotEmployeeRows(RepeatedOffenderDashboardRequest request) {
		if (request.getRangeStartYear() == null || request.getRangeStartMonth() == null
				|| request.getRangeEndYear() == null || request.getRangeEndMonth() == null) {
			return 0L;
		}
		try {
			Session session = entityManager.unwrap(Session.class);
			String countSql = "SELECT COUNT(*) FROM (" + RepeatedOffenderSnapshotKpiSql.SNAPSHOT_EMPLOYEE_LIST_INNER
					+ ") ro_list WHERE 1=1 " + RepeatedOffenderSnapshotKpiSql.RO_LIST_GRID_FILTER;
			NativeQuery<?> query = session.createNativeQuery(countSql);
			bindSnapshotListParameters(query, request);
			bindRoListGridFilters(query, request);
			Object single = query.getSingleResult();
			if (single == null) {
				return 0L;
			}
			if (single instanceof Number) {
				return ((Number) single).longValue();
			}
			return Long.parseLong(single.toString());
		} catch (Exception ex) {
			log.error("RO snapshot list count failed", ex);
			return 0L;
		}
	}

	/**
	 * Paginated snapshot list: six columns only, {@code ORDER BY} on the same derived-table alias as the
	 * select list (avoids an extra outer wrapper that some drivers / Hibernate versions handle poorly).
	 */
	private static String buildRoSnapshotPagedSql(RepeatedOffenderDashboardRequest request, int offset, int limit) {
		String dir = "DESC".equalsIgnoreCase(trimToNull(request.getSortDirection())) ? "DESC" : "ASC";
		String orderKeyExpr = roOrderKeySelectExpression(request);
		return "SELECT ro_list.emp_id, ro_list.employment_id, ro_list.employee_name, ro_list.department, "
				+ "ro_list.employment_status, ro_list.defaulted_months_count "
				+ "FROM (" + RepeatedOffenderSnapshotKpiSql.SNAPSHOT_EMPLOYEE_LIST_INNER + ") ro_list WHERE 1=1 "
				+ RepeatedOffenderSnapshotKpiSql.RO_LIST_GRID_FILTER + " ORDER BY " + orderKeyExpr + " " + dir
				+ " LIMIT " + Integer.toString(limit) + " OFFSET " + Integer.toString(offset);
	}

	private static String roOrderKeySelectExpression(RepeatedOffenderDashboardRequest request) {
		String sortBy = trimToNull(request.getSortBy());
		if (sortBy == null) {
			sortBy = "employeeName";
		}
		switch (sortBy) {
		case "employmentId":
			return "ro_list.employment_id";
		case "department":
			return "ro_list.department";
		case "employmentStatus":
			return "ro_list.employment_status";
		case "streak":
		case "defaultedMonthsCount":
			return "ro_list.defaulted_months_count";
		case "projectName":
			return "COALESCE(" + roMinProjectNameSubquery() + ", '')";
		case "billableType":
			return "COALESCE(" + roMinBillableSubquery() + ", '')";
		case "managerName":
			return "COALESCE(" + roMinManagerSubquery() + ", '')";
		case "projectMapping":
			return "COALESCE(" + roMinMappingSubquery() + ", '')";
		case "teamName":
			return "COALESCE(" + roMinTeamNameSubquery() + ", '')";
		case "employeeName":
		default:
			return "ro_list.employee_name";
		}
	}

	private static String trimToNull(String s) {
		if (s == null) {
			return null;
		}
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}

	/** Strips invisible chars / NBSP so pasted grid values match DB-bound needles. */
	private static String normalizeRoFilterNeedle(String raw) {
		if (raw == null) {
			return null;
		}
		String t = raw.trim().replace('\u200B', ' ').replace('\uFEFF', ' ').replace('\u00A0', ' ');
		while (t.contains("  ")) {
			t = t.replace("  ", " ");
		}
		t = t.trim();
		return t.isEmpty() ? null : t;
	}

	private void bindRoListGridFilters(NativeQuery<?> nq, RepeatedOffenderDashboardRequest request) {
		String fei = normalizeRoFilterNeedle(request.getFilterEmploymentId());
		nq.setParameter("rofUseEmpId", Integer.valueOf(fei != null ? 1 : 0));
		nq.setParameter("rofEmpId", fei != null ? fei : "");

		String fen = normalizeRoFilterNeedle(request.getFilterEmployeeName());
		nq.setParameter("rofUseEmpName", Integer.valueOf(fen != null ? 1 : 0));
		nq.setParameter("rofEmpName", fen != null ? fen : "");

		String fd = normalizeRoFilterNeedle(request.getFilterDepartment());
		nq.setParameter("rofUseDept", Integer.valueOf(fd != null ? 1 : 0));
		nq.setParameter("rofDept", fd != null ? fd : "");

		String fst = normalizeRoFilterNeedle(request.getFilterEmploymentStatus());
		nq.setParameter("rofUseEmpSt", Integer.valueOf(fst != null ? 1 : 0));
		nq.setParameter("rofEmpSt", fst != null ? fst : "");

		String pn = normalizeRoFilterNeedle(request.getFilterProjectName());
		nq.setParameter("rofUsePn", Integer.valueOf(pn != null ? 1 : 0));
		nq.setParameter("rofPn", pn != null ? pn : "");

		String bt = normalizeRoFilterNeedle(request.getFilterBillableType());
		nq.setParameter("rofUseBt", Integer.valueOf(bt != null ? 1 : 0));
		nq.setParameter("rofBt", bt != null ? bt : "");

		String mgr = normalizeRoFilterNeedle(request.getFilterManagerName());
		nq.setParameter("rofUseMgr", Integer.valueOf(mgr != null ? 1 : 0));
		nq.setParameter("rofMgr", mgr != null ? mgr : "");

		String map = normalizeRoFilterNeedle(request.getFilterProjectMapping());
		nq.setParameter("rofUseMap", Integer.valueOf(map != null ? 1 : 0));
		nq.setParameter("rofMap", map != null ? map : "");

		String team = normalizeRoFilterNeedle(request.getFilterTeamName());
		nq.setParameter("rofUseTeam", Integer.valueOf(team != null ? 1 : 0));
		nq.setParameter("rofTeam", team != null ? team : "");
	}

	private static String roMappingOverlapSql() {
		return " FROM employee_team_mapping etm "
				+ " INNER JOIN teams t ON t.team_id = etm.team_id "
				+ " INNER JOIN projects p ON p.project_id = t.project_id "
				+ " INNER JOIN employee ex ON ex.emp_id = etm.emp_id "
				+ " LEFT JOIN ( "
				+ "   SELECT pm.project_id, GROUP_CONCAT(DISTINCT e2.name ORDER BY e2.name SEPARATOR ', ') AS pm_names "
				+ "   FROM project_manager_mapping pm "
				+ "   INNER JOIN employee e2 ON e2.emp_id = pm.project_manager_id "
				+ "   GROUP BY pm.project_id "
				+ " ) pma ON pma.project_id = p.project_id "
				+ " CROSS JOIN (SELECT :customFrom AS drf_s, :customTo AS drf_e) drf "
				+ " WHERE etm.emp_id = ro_list.emp_id "
				+ " AND DATE(etm.start_date) <= drf.drf_e "
				+ " AND (etm.end_date IS NULL OR DATE(etm.end_date) >= drf.drf_s) "
				+ " AND (:isClientSide = 0 OR COALESCE(p.has_client_side_id, 0) = 1) "
				+ " AND (:employeeStatus = 'All' OR ex.employmentstatus = :employeeStatus) "
				+ " AND ex.emp_id NOT BETWEEN 1 AND 6 "
				+ " AND ('All' IN (:billableTypes) OR (" + RepeatedOffenderSnapshotKpiSql.PROJECT_BILLABLE_CASE + ") IN (:billableTypes)) ";
	}

	private static String roMinProjectNameSubquery() {
		return "(SELECT MIN(p.project_name) " + roMappingOverlapSql() + ")";
	}

	private static String roMinBillableSubquery() {
		return "(SELECT MIN(CASE "
				+ " WHEN COALESCE(etm.is_shadow, 0) = 0 AND p.po_project_type IS NOT NULL THEN p.po_project_type "
				+ " WHEN COALESCE(p.internal_project_type, '') <> '' THEN p.internal_project_type "
				+ " WHEN p.po_project_type = 'TNM' AND COALESCE(etm.is_shadow, 0) = 1 THEN 'TNM(Shadow)' "
				+ " WHEN p.po_project_type = 'Fixed Cost' AND COALESCE(etm.is_shadow, 0) = 1 THEN 'Fixed Cost(Shadow)' "
				+ " ELSE COALESCE(p.po_project_type, '') END) " + roMappingOverlapSql() + ")";
	}

	private static String roMinManagerSubquery() {
		return "(SELECT MIN(pma.pm_names) " + roMappingOverlapSql() + ")";
	}

	private static String roMinMappingSubquery() {
		return "(SELECT MIN(CONCAT(COALESCE(t.team_name, ''), ' / ', COALESCE(p.project_name, ''))) "
				+ roMappingOverlapSql() + ")";
	}

	private static String roMinTeamNameSubquery() {
		return "(SELECT MIN(COALESCE(t.team_name, '')) " + roMappingOverlapSql() + ")";
	}

	private void bindSnapshotListParameters(NativeQuery<?> nq, RepeatedOffenderDashboardRequest request) {
		bindSnapshotKpiParameters(nq, request);
		nq.setParameter("segmentRule", snapshotSegmentRule(request));
	}

	private static RepeatedOffenderEmployeeRowPayload mapSnapshotListRow(Object[] row) {
		RepeatedOffenderEmployeeRowPayload p = new RepeatedOffenderEmployeeRowPayload();
		if (row.length > 0 && row[0] != null) {
			p.setEmpId(((Number) row[0]).longValue());
		}
		if (row.length > 1) {
			p.setEmploymentId(row[1] != null ? row[1].toString() : "");
		}
		if (row.length > 2) {
			p.setEmployeeName(row[2] != null ? row[2].toString() : "");
		}
		if (row.length > 3) {
			p.setDepartment(row[3] != null ? row[3].toString() : "");
		}
		if (row.length > 4) {
			p.setEmploymentStatus(row[4] != null ? row[4].toString() : "");
		}
		long defMonths = row.length > 5 && row[5] != null ? toLong(row[5]) : 0L;
		p.setStreakLabel(Long.toString(defMonths));
		p.setMonthStatuses(new LinkedHashMap<>());
		p.setCurrentStatusCode("not_filled");
		p.setCurrentStatusLabel("N/A");
		return p;
	}

	private void hydrateProjectMappings(List<RepeatedOffenderEmployeeRowPayload> rows,
			RepeatedOffenderDashboardRequest request) {
		if (rows.isEmpty()) {
			return;
		}
		List<Long> ids = new ArrayList<>();
		for (RepeatedOffenderEmployeeRowPayload p : rows) {
			if (p.getEmpId() != null) {
				ids.add(p.getEmpId());
			}
		}
		if (ids.isEmpty()) {
			return;
		}
		try {
			Session session = entityManager.unwrap(Session.class);
			NativeQuery<Object[]> q = session.createNativeQuery(RepeatedOffenderSnapshotKpiSql.SNAPSHOT_PROJECT_MAPPINGS);
			bindSnapshotProjectMappingParameters(q, request);
			q.setParameterList("ids", ids);
			List<Object[]> detailRows = q.getResultList();
			Map<Long, List<RepeatedOffenderProjectMappingPayload>> byEmp = new LinkedHashMap<>();
			for (RepeatedOffenderEmployeeRowPayload p : rows) {
				if (p.getEmpId() != null) {
					byEmp.put(p.getEmpId(), new ArrayList<>());
				}
			}
			for (Object[] r : detailRows) {
				if (r.length < 7 || r[0] == null) {
					continue;
				}
				long empId = ((Number) r[0]).longValue();
				List<RepeatedOffenderProjectMappingPayload> list = byEmp.get(empId);
				if (list == null) {
					continue;
				}
				RepeatedOffenderProjectMappingPayload m = new RepeatedOffenderProjectMappingPayload();
				m.setProjectName(r[1] != null ? r[1].toString() : "");
				m.setBillableType(r[2] != null ? r[2].toString() : "");
				m.setEmploymentStatus(r[3] != null ? r[3].toString() : "");
				m.setManagerName(r[4] != null ? r[4].toString() : "");
				m.setTeamName(r[5] != null ? r[5].toString() : "");
				m.setProjectMapping(r[6] != null ? r[6].toString() : "");
				list.add(m);
			}
			for (RepeatedOffenderEmployeeRowPayload p : rows) {
				if (p.getEmpId() == null) {
					continue;
				}
				List<RepeatedOffenderProjectMappingPayload> list = byEmp.get(p.getEmpId());
				if (list != null) {
					p.setProjectMappings(list);
				}
				if (p.getProjectMappings() == null || p.getProjectMappings().isEmpty()) {
					p.setCurrentStatusLabel("No mapping in range");
				} else {
					p.setCurrentStatusLabel(p.getProjectMappings().size() + " project(s)");
				}
			}
		} catch (Exception ex) {
			log.warn("RO snapshot project mappings failed: {}", ex.getMessage());
		}
	}

	/** Client repository expects literal {@code true}/{@code false} for client-side filter. */
	private static String clientSideFilterParam(String raw) {
		if (raw == null || "ALL".equalsIgnoreCase(raw)) {
			return "ALL";
		}
		if ("true".equalsIgnoreCase(raw) || "false".equalsIgnoreCase(raw)) {
			return raw;
		}
		return raw;
	}

	private static List<String> normalizeBillableTypes(List<String> billableTypes) {
		if (billableTypes == null || billableTypes.isEmpty()) {
			return Collections.singletonList("All");
		}
		return billableTypes;
	}

	private String applicableDetailSqlFor(RepeatedOffenderDashboardRequest request) {
		if (Boolean.TRUE.equals(request.getClientDashboard())) {
			return applicableEmployeesDetailClientSql;
		}
		return applicableEmployeesDetailSql;
	}

	private void bindApplicableDetailParameters(NativeQuery<?> nq, RepeatedOffenderDashboardRequest request) {
		nq.setParameter("year", request.getRangeEndYear());
		nq.setParameter("month", request.getRangeEndMonth());
		nq.setParameter("emp_id", request.getViewerEmpId());
		nq.setParameter("employeeActive", request.getEmployeeActive() != null ? request.getEmployeeActive() : "All");
		nq.setParameterList("billableType", normalizeBillableTypes(request.getBillableTypes()));
		if (Boolean.TRUE.equals(request.getClientDashboard())) {
			String clientSide = request.getClientSideFilter() != null ? request.getClientSideFilter() : "ALL";
			nq.setParameter("clientSideFilter", clientSideFilterParam(clientSide));
			String multi = request.getMultiPOs() != null ? request.getMultiPOs() : "All";
			nq.setParameter("multiPOs", multi);
		}
	}

	private static String stripTrailingOrderBy(String sql) {
		return sql.replaceAll("(?is)\\s+ORDER\\s+BY\\s+\\w+\\.name\\s*$", "");
	}

	private static RepeatedOffenderEmployeeRowPayload mapApplicableRow(Object[] row, List<String> monthLabels) {
		RepeatedOffenderEmployeeRowPayload p = new RepeatedOffenderEmployeeRowPayload();
		if (row.length > 0 && row[0] != null) {
			p.setEmpId(((Number) row[0]).longValue());
		}
		if (row.length > 1) {
			p.setEmploymentId(row[1] != null ? row[1].toString() : "");
		}
		if (row.length > 2) {
			p.setEmployeeName(row[2] != null ? row[2].toString() : "");
		}
		if (row.length > 3) {
			p.setDepartment(row[3] != null ? row[3].toString() : "");
		}
		Map<String, String> months = new LinkedHashMap<>();
		for (String label : monthLabels) {
			months.put(label, "NA");
		}
		p.setMonthStatuses(months);
		p.setStreakLabel("");
		p.setCurrentStatusCode("not_filled");
		p.setCurrentStatusLabel("N/A");
		return p;
	}

	private static List<String> buildMonthLabels(RepeatedOffenderDashboardRequest r) {
		List<String> labels = new ArrayList<>();
		if (r.getRangeStartYear() == null || r.getRangeStartMonth() == null || r.getRangeEndYear() == null
				|| r.getRangeEndMonth() == null) {
			return labels;
		}
		YearMonth cur = YearMonth.of(r.getRangeStartYear(), r.getRangeStartMonth());
		YearMonth end = YearMonth.of(r.getRangeEndYear(), r.getRangeEndMonth());
		if (cur.isAfter(end)) {
			return labels;
		}
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yyyy", Locale.US);
		while (!cur.isAfter(end)) {
			labels.add(cur.format(fmt).toUpperCase(Locale.US));
			cur = cur.plusMonths(1);
		}
		return labels;
	}
}
