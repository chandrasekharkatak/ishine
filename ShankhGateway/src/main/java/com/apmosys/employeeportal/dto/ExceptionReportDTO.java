package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class ExceptionReportDTO {   
    private String employmentId;
    private String employeeName;
    private String department;
    private String billableType;
    private List<RMGProject> rmgProjects;
    private Long empId;
    
}

