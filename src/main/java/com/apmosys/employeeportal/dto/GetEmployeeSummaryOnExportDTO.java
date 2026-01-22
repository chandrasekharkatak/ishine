package com.apmosys.employeeportal.dto;

import java.util.List;

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
	private List<String> billableType;
	private String projectActive;
	private String employeeActive;
	private String status;
	private Integer page;
	private Integer size;
	private String sortBy;
	private String sortDirection; 
	private ColumnFilterDTO filters;
	private String clientSideFilter;
	private Long deptId;
	private Boolean isEmployeeRepeated;
	
}
