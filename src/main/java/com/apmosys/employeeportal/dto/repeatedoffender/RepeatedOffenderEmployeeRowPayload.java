package com.apmosys.employeeportal.dto.repeatedoffender;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * One employee row in the Repeated Offender table (snapshot metric list).
 * {@link #projectMappings} holds team/project lines; UI may show first row + expand for more.
 */
@Getter
@Setter
public class RepeatedOffenderEmployeeRowPayload {

	private Long empId;
	private String employmentId;
	private String employeeName;
	private String department;

	/** Employee-level status (same as employee.employmentstatus). */
	private String employmentStatus;

	/** Total defaulted snapshot months in range (matches card logic). */
	private String streakLabel;

	private List<RepeatedOffenderProjectMappingPayload> projectMappings = new ArrayList<>();

	/** Optional month grid (legacy); may be empty when list uses project columns only. */
	private Map<String, String> monthStatuses = new LinkedHashMap<>();

	private String currentStatusCode;
	private String currentStatusLabel;
}
