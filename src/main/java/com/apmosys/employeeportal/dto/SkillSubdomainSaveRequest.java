package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class SkillSubdomainSaveRequest {

	private Integer domainId;
	private String subdomainName;
	private Boolean isActive;
}
