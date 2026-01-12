package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class EmployeeInfoDTO {

	private Long empId;
    private String employeeName;
    private String department;
    private String employmentId;
    
    private List<ProjectTimesheetInfoDTO> projectTimesheet;
}
