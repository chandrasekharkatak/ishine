package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColumnFilterDTO {
	
		private String projectName;
		private String poNo;
		private String projectManagerName;
		private String projectType;
		private String clientName;
		private String apmosysRm;
		private String apmosysRmEmail;
		private String clientRm;
		private Integer totalExpectedFillCount;
		private Integer totalClientSideApprovedCount;
		private Integer totalClientSidePendingCount;
		private Integer totalClientSideNotFilledCount;
		
		private String employmentId;
		private String name;
		private String billable;
		private String billableType;
		private String mobileNo;
		private String email;
		private String departmentName;
		private Integer expectedFillCount;
		private Integer clientSideAttendancePendingCount;
		private Integer clientSideAttendanceApprovedCount;
		private Integer clientSideAttendanceNotFilledCount;
		private String projectManagers;
		private String team;
		private String teamLeadName;
		private Integer totalEmployees;
		private String active;

}
