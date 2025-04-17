package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class GetProjectToEmployeeReportForEmployeeDTO {
	
	private Long empId;
    private String employeeName;
    private String effectiveStartDate;
    private String billable;
    private String billableType;
    private String lastFilledTimesheet;
    private String jobRole;
    private String deptName;
    private Long mobileNo;
    private String email;
    
}
