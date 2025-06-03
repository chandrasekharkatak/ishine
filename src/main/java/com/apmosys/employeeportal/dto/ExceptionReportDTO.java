package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ExceptionReportDTO {   
    private String employmentId;
    private String employeeName;
    private String department;
    private String billableType;
    private String projectName;
    private String clientName;
    private String apmosysRM;
    private String clientRM;
    private String poNumber;
    private String poProjectType;
    private String poStartDate;
    private String poEndDate;
}

