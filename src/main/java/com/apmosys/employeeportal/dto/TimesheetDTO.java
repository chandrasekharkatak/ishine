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
	private Integer clientId;
	private String clientName;
	private Integer clientLocationId;
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

	private Long teamId;
	private String activity;

	private String departmentName;
	private String email;
	private Long mobileNo;
	private String managerName;
	private Long pendingEodCount;
	private String legend;
	private String rejectReason;
	private List<CustomFilterDTO> queryList;
	private String employmentstatus;
	private List<TimesheetDTO> bulkApprovedList;
	private List<TimesheetDTO> bulkRejectList;
	
}
