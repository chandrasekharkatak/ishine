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
	private Double previousExperience;
	private Double currentExperience;
	private Double totalExperience;
	private String billableType;
	private String jobRole;
	private String deptName;

}
