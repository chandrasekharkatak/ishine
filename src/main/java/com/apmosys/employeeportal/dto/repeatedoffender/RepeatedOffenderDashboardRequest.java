package com.apmosys.employeeportal.dto.repeatedoffender;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Request for Repeated Offender dashboard APIs (summary + employee grid).
 * Filter fields mirror the HR dashboard RO filter bar; integrate SQL using these parameters.
 */
@Getter
@Setter
@ToString
public class RepeatedOffenderDashboardRequest {

	/** Logged-in user performing the request (authorization scope). */
	private Long viewerEmpId;

	private Boolean clientDashboard;

	/** Inclusive range start (first month of RO period). */
	private Integer rangeStartMonth;
	private Integer rangeStartYear;

	/** Inclusive range end (last month of RO period). */
	private Integer rangeEndMonth;
	private Integer rangeEndYear;

	private List<String> billableTypes;

	private String employeeActive;

	private String clientSideFilter;

	private String multiPOs;

	/** "Defaulted in N months" threshold (RO filter). */
	private Integer defaultedThreshold;

	/**
	 * Preset for snapshot KPI SQL: {@code Last 3 Months} | {@code Last 6 Months} | {@code Custom}
	 * (mirrors RO period dropdown).
	 */
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private String repeatedOffenderPeriod;

	private Long deptId;

	/**
	 * When true, future queries may use project-oriented aggregates instead of employee aggregates.
	 */
	private Boolean projectView;

	/** Project active filter when {@code projectView} is true (mirrors legacy dashboard). */
	private String projectActive;

	// --- employee grid only (ignored by /summary) ---

	/**
	 * ALL | TOTAL_APPLICABLE | REPEATED_OFFENDERS | ALL_MONTHS_STREAK | THRESHOLD_EXACT
	 */
	private String tableSegment;

	private Integer page;
	private Integer size;
	private String sortBy;
	private String sortDirection;

	/** Optional LIKE filters for RO employee grid (server-side; empty / null = ignore). */
	@JsonAlias({ "filter_employment_id" })
	private String filterEmploymentId;
	@JsonAlias({ "filter_employee_name" })
	private String filterEmployeeName;
	@JsonAlias({ "filter_department" })
	private String filterDepartment;
	@JsonAlias({ "filter_employment_status" })
	private String filterEmploymentStatus;
	@JsonAlias({ "filter_project_name" })
	private String filterProjectName;
	@JsonProperty("filterBillableType")
	@JsonAlias({ "filter_billable_type", "billableType" })
	private String filterBillableType;
	@JsonAlias({ "filter_manager_name" })
	private String filterManagerName;
	@JsonAlias({ "filter_project_mapping" })
	private String filterProjectMapping;
	@JsonProperty("filterTeamName")
	@JsonAlias({ "filter_team_name", "teamName" })
	private String filterTeamName;

	public String getRepeatedOffenderPeriod() {
		return repeatedOffenderPeriod;
	}

	public void setRepeatedOffenderPeriod(String repeatedOffenderPeriod) {
		this.repeatedOffenderPeriod = repeatedOffenderPeriod;
	}
}
