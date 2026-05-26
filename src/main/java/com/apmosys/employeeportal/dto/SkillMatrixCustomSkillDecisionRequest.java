package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class SkillMatrixCustomSkillDecisionRequest {

	private String decision;
	private String skillType;
	private String comment;
}
