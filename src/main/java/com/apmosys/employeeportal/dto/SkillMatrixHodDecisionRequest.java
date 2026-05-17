package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class SkillMatrixHodDecisionRequest {
	/** approved | rejected */
	private String decision;
	private String comment;
}

