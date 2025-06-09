package com.apmosys.employeeportal.dto;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GetEmployeeInformationForDefaultProjectDTO {
	
	private Long empId;
	private String employmentId;
	private String name;
	private List<Map<String, Object>> otherActiveProjects;
	
}
