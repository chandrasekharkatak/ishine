package com.apmosys.employeeportal.dto;

import lombok.Data;

import java.time.LocalDateTime; 

@Data
public class LastTimesheetFieldDto {
	
	private String employementId;
	private LocalDateTime officeInTime;
	private LocalDateTime officeOutTime;
	private Long ProjectId;
	private Long clientId;
	private Long clientLocationID;
	private String teamName;
	private String activity;
	private Long activityID;
	private String description;
	private Long teamId;
	private String clientApprovalStatus;
	

}
