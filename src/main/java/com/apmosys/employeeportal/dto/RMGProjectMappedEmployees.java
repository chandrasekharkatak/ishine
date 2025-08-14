package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RMGProjectMappedEmployees {
	
	private Long empId;
	private Long employeementId;
	private String isConsultant;
	private String isApprenticeship;
	private String isApmosysProduct;
	private String employeementIdAccToET;
	private String name;
	private String employmentstatus;
	private String billable;
	private String billableType; 
	private String department;
	private List<RMGProject> rmgprojects;
	
}
