package com.apmosys.employeeportal.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.apmosys.employeeportal.model.Notification;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class Employee360DTO {
	
	private Long empId;
	private String name;
	private Long managerId;
	private String managerName;
	private String departmentName;
	private Long departmentId;
	private String leaveYear;
	private String leaveMonth;
	private String leaveType;
	private String totalDays;
	private String leaveStatus;
	private int createdBy;
	private String employeeName;
	private String date;
	private String dayType;
	private LocalDateTime officeInTime;
	private LocalDateTime officeOutTime;
	private boolean isNightShift;
	private String totalWorkingHours;
	private String status;
	private String remarks;
	private Long timesheetId;
	private LocalDateTime createdOn;
	private List timeSheet;
	
}
