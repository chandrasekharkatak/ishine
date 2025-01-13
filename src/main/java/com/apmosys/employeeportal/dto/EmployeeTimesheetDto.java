package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EmployeeTimesheetDto {

	private String activity;
	private String projectName;
	private String date; 
	private String description; 
	private String teamName;
	private Float totalWorkingHours;
	private Float completionTime;
	private Long timesheetId;
	private Long teamId;
	private Long projectId;
	private Long activityId;
	
}
