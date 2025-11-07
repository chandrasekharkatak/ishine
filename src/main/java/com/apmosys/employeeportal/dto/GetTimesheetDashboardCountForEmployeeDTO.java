package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GetTimesheetDashboardCountForEmployeeDTO {
	
	private Integer month;
	private Integer year;
	private Long empId;
	private Boolean isClientDashboard;
	private String selectedBillableType;
	
}
