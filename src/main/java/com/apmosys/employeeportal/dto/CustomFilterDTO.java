package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CustomFilterDTO {

	String column;
	String operator;
	String value;
	String conjunction;
	String customQuery;
	Long empId;
	String field;
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
