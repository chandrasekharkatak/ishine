package com.apmosys.employeeportal.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TimesheetDTO {

	private Integer projectId;
	private String clientName;
	private String clientLocation;
	private String state;
	private String projectName;
	private String description;
	private Long projectManagerId;
	private Long managerId;
	private Long empId;
	private String approvedOn;
	private String status;
	private Long timesheetId;
	private String date;
	private String dayType;
	private String employeeName;
	private String createdByName;
	private String createdOn;
	private String startDate;
	private String endDate;
	private Long createdBy;
	private List<ActivityDTO> allTimesheetActivities;
	private List<ActivityDTO> updatedTimesheetActivities;
	
	private Long employeementId;
	
	private Long applicationCount;
	
	private String weekDayName;
	private Float totalWorkingHours;
	
	private Float totalTime;
	
	private Long timesheetStatusUpdatedBy;
	private String timesheetStatusUpdatedByName;
	private String updatedOn;
}
