package com.apmosys.employeeportal.dto;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class EmployeeProjectSummaryDTO {
	
	private String poProjectType;
    private String billableType;
    private Long totalEmp;
    private Long totalEmpPerProjectType;
    
}
