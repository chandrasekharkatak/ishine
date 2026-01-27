package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EmployeeInformationDTO {

	private Long empId;
	private String employmentId;
	private String name;
	private String previousExperience;
	private String currentExperience;
	private String totalExperience;
	private String billableType;
	private String jobRole;
	private String deptName;
	private String startDate;
	private Long deptId;

}
