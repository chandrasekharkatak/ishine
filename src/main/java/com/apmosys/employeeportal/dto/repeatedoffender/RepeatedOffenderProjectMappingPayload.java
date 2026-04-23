package com.apmosys.employeeportal.dto.repeatedoffender;

import lombok.Getter;
import lombok.Setter;

/** One project / team mapping row for Repeated Offender list (expandable under an employee). */
@Getter
@Setter
public class RepeatedOffenderProjectMappingPayload {

	private String projectName;
	private String billableType;
	private String employmentStatus;
	private String managerName;
	private String teamName;
	/** Short label e.g. team / project. */
	private String projectMapping;
}
