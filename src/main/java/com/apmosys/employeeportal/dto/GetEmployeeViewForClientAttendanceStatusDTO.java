package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GetEmployeeViewForClientAttendanceStatusDTO {
	
	private Long empId;
	private String name;
	private Long projectId;
	private String projectName;
	private String poNo;
	private String projectType;
	private String clientName;
	private String teamLeadName;
	private String team;
	private String billable;
	private String billableType;
	private Long mobileNo;
	private String email;
	private Long expectedFillCount;
	private Long timesheetFilledCount;
	private Long clientSideAttendanceApprovedCount;
	private Long clientSideAttendancePendingCount;
	private Long clientSideAttendanceNotFilledCount;
	private String departmentName;
	private String employmentId;
	private String projectManagers;
	private String apmosysRm;
	private String apmosysRmEmail;
	private String clientRm;
	private Long expectedIshineFillCount ;
	private Long ishineNotFilledTimesheetCount ;
	private Long ishinePendingTimesheetCount ;
	private Long ishineApprovedTimesheetCount;
	private String employeeStatus ;

	
	
	
}
