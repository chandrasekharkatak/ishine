package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class GetEmployeeProjectReportPayloadDTO {
	
	private String report;
	private String poProjectType;
	private String category;
	private String flag;
	private List<String> billableType;
	private List<Long> deptId;
	private Boolean hideMaternityLeaveEmps;

}
