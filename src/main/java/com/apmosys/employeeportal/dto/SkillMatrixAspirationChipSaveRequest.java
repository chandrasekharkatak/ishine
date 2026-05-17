package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class SkillMatrixAspirationChipSaveRequest {

	/** Use {@code 0} for the global default pool; otherwise HRMS / job-role department id. */
	private Long deptId;
	private String chipLabel;
	private Integer sortOrder;
	private Boolean isActive;
}
