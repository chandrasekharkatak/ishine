package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EmployeeTimesheetDto {

	private List<String> activities;
	private String projectName;
	private String teamName;
	private Float totalWorkingHours;
	private Long timesheetId;
	private Long projectId;
	private Long activityId;

}
