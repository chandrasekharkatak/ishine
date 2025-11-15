package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GetEmployeeSummaryOnExportDTO {
	
	private Integer projectId;
	private Integer month;
	private Integer year;
	private Long empId;
	private Boolean allEmp;
	private String billableType;
	private String status;
	private Integer page;
	private Integer size;
	private String sortBy;
	private String sortDirection; 
	
	
}
