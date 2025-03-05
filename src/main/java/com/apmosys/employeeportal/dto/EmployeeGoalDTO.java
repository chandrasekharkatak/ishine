package com.apmosys.employeeportal.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.*;


import java.time.LocalDate;

@Getter
@Setter
public class EmployeeGoalDTO {

    private Long goalId;

    
    private Long empId;

   
    private String assignedBy;

   
    private String goalTitle;

    
    private String goalProgress;

   
    private String reviewStatus;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expectedCompletionDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualCompletionDate;

    private LocalDate createdDate;
    private LocalDate updatedDate;
}

















































