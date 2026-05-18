package com.apmosys.employeeportal.dto.polink;

import lombok.Builder;
import lombok.Getter;

/** Row for the "previous (removed) PO" requirements table. */
@Getter
@Builder
public class PreviousRequirementDto {

	private String projectName;
	private String poLabel;
	private String roleLabel;
	private long requiredCount;
}
