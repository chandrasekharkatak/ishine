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
	private Integer totalEmployees;
	private String active;
	
	private String employmentId;
	private String employeeName;
	private String clientSideId;
	private String employmentStatus;
	private String projectStatus;
	private String billableType;
	private String department;
	private String projectManagers;
	private String teamName;
	private String startDate;
	private String endDate;
	private String name;
}
