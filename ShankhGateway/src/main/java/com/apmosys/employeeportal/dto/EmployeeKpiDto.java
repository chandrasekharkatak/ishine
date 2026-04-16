package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeKpiDto {

	private Long kpiId;
	private Long templateId;
	private Long empId;
	private Long assignedBy;
	private Long assignedOn;
	private String departmentId;
	private String employeeRole;
	private Long quarterId;
	
	
}
