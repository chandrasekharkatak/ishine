package com.apmosys.employeeportal.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class EmployeeGoalsQuarterDto {
	
    private Long goalId;
    private Long empId;
    private Long assignedBy;
    private Long templateId;
    private String goalTitle;
    private String goalProgress;
    private String goalStatus;
    private LocalDate expectedCompletionDate;
    private LocalDate actualCompletionDate;
    private LocalDate createdDate;
    private String quarter;
}
