package com.apmosys.employeeportal.dto.polink;

import lombok.Builder;
import lombok.Getter;

/**
 * One row in the requirement vs mapping table (business-facing fields only).
 */
@Getter
@Builder
public class RequirementDto {

	private String poLabel;
	private String roleAndDepartment;
	private long requiredCount;
	private long mappedCount;
	/** Staffing vs requirement: Balanced, Underboarded, Overboarded, etc. */
	private String statusText;
	/** Inline style color for status (email-safe). */
	private String statusColor;
	/** PO lifecycle: Active, Expired, etc. */
	private String poLifecycleStatus;
	/** When true, row is styled as expired / removed context. */
	private boolean expiredRow;
}
