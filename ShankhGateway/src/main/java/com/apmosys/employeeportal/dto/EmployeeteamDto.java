package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeteamDto {
	private Long empId;
	private Long employeementId;
	private String name;
	private Long totalGoals;
	private Long goalsCompleted;
	private Long quarterId;
}