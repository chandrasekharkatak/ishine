package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class ReimbursementClaimDecisionDTO {
	private Long claimId;
	/** true = approve claim at current stage, false = reject */
	private Boolean approved;
	private String remarks;
}
