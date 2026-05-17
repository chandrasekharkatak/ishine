package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class SubskillMasterSaveRequest {

	private Integer skillId;
	private String subskillName;
	private Boolean isActive;
}
