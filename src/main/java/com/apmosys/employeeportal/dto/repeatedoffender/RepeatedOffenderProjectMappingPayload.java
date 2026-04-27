package com.apmosys.employeeportal.dto.repeatedoffender;

import lombok.Getter;
import lombok.Setter;

/** One project / team mapping row for Repeated Offender list (expandable under an employee). */
@Getter
@Setter
public class RepeatedOffenderProjectMappingPayload {

	private String projectName;
	private String poName;
	private String billableType;
	private String employmentStatus;
	/** Reporting Manager name from {@code projects.apmosysrm}. */
	private String managerName;
	private String teamName;
	/** Short label e.g. team / project. */
	private String projectMapping;
	/** Team assignment start (from {@code employee_team_mapping.start_date}), display format dd-MM-yyyy. */
	private String teamStartDate;
	/** Team assignment end; empty when still active (no end date). */
	private String teamEndDate;
	/**
	 * Defaulted months in the selected range for this mapping (from {@code employee_monthly_snapshot}),
	 * e.g. {@code January 2026, February 2026}.
	 */
	private String defaultedPeriodDisplay;
}
