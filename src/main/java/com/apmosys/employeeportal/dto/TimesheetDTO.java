package com.apmosys.employeeportal.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
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
	private Long CreatedByEmpId;
	private String createdOn;
	private String startDate;
	private String endDate;
	private Long createdBy;
	private List<ActivityDTO> allTimesheetActivities;
	private List<ActivityDTO> updatedTimesheetActivities;
    private Long activityTimesheetId;
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
	private List<CustomFilterDTO> queryList1;

	private String employmentstatus;
	private List<TimesheetDTO> bulkApprovedList;
	private List<TimesheetDTO> bulkRejectList;
	
	private String remarks;
	private String teamName;
	private Long activityId;
	
	//project
	private String active;

	private String officeInTime;
	private String officeOutTime;
	private String totalWorkingOfficeHours; //<-- totalWorkingHours in Timesheet Model
	private String isNightShift;
	private String managerEmail;
	
	private String isCron; //<-- for allEmployee DSR report cronJob
	private Integer year;
	private String month;
	private Long actualEODCount;
	private Long resourceCount;
	private Long expectedEODCount;
	private Long currentManagerId;
	
	private String leaveType;
	private List<ActivityDTO> inactiveTimesheetActivities;
	private String isConsultant;
	private String isApprenticeship;
	
	private List<EmployeeTimesheetDto> timeSheet;
	
	public TimesheetDTO(
		    Long timesheetId,               // long
		    LocalDate date,                 // java.time.LocalDate
		    String dayType,                 // java.lang.String
		    String employeeName,            // java.lang.String (ec.name)
		    String description,             // java.lang.String
		    String status,                  // java.lang.String
		    String createdByName,           // java.lang.String (eh.name)
		    Long createdByEmpId,            // long (eh.empId)
		    String createdOn,               // java.lang.String
		    Long employeementId,            // long
		    Float totalTime,                // float
		    String email,                   // java.lang.String
		    LocalDateTime officeInTime,     // java.time.LocalDateTime
		    LocalDateTime officeOutTime,    // java.time.LocalDateTime
		    String totalWorkingHours,       // java.lang.String
		    String isNightShift,            // java.lang.String
		    Long currentManagerId,          // long
		    String isConsultant,            // java.lang.String
		    String isApprenticeship,        // java.lang.String
		    Long empId                      // long
		) {
		    this.timesheetId = timesheetId;
		    this.date = date.toString();    // Convert LocalDate to String
		    this.dayType = dayType;
		    this.employeeName = employeeName;
		    this.description = description;
		    this.status = status;
		    this.createdByName = createdByName;
		    this.CreatedByEmpId = createdByEmpId;
		    this.createdOn = createdOn;
		    this.employeementId = employeementId;
		    this.totalTime = totalTime;
		    this.email = email;
		    this.officeInTime = officeInTime.toString();    // Convert LocalDateTime to String
		    this.officeOutTime = officeOutTime.toString();  // Convert LocalDateTime to String
		    this.totalWorkingHours = Float.parseFloat(totalWorkingHours);  // Convert String to Float
		    this.isNightShift = isNightShift;
		    this.currentManagerId = currentManagerId;
		    this.isConsultant = isConsultant;
		    this.isApprenticeship = isApprenticeship;
		    this.empId = empId;
		}}