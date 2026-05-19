package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class SkillMasterSaveRequest {

	private String skillName;
	private Integer categoryId;
	/** Optional / Required (matches DB enum). */
	private String skillType;
	private Boolean isActive;
	private Long departmentId;
}
