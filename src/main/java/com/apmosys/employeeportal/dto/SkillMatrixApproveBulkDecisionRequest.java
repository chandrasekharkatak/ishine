package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class SkillMatrixApproveBulkDecisionRequest {
	/** Optional comment applied to all impacted skills (required for reject / send_back). */
	private String managerComment;
}

