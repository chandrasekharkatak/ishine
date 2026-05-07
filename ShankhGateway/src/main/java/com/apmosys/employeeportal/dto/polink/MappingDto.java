package com.apmosys.employeeportal.dto.polink;

import lombok.Builder;
import lombok.Getter;

/**
 * Aggregated mapping insight for a PO / role window (headcount in overlap period).
 */
@Getter
@Builder
public class MappingDto {

	private long mappedHeadcount;
	/** True when based on {@code po_requirement_mapping} + {@code employee_team_mapping}. */
	private boolean derivedFromPrm;
}
