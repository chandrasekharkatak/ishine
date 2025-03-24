package com.apmosys.employeeportal.dto;

//import com.apmosys.employeeportal.model.EmployeeGoals.ReviewStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.*;


import java.time.LocalDate;

@Data
public class EmployeeGoalDTO {

    private Long goalId;

    
    private Long empId;
    
    private Long templateId;

   
    private Long assignedBy;

   
    private String goalTitle;

    
    private Long goalProgress;
    
    private String goalStatus;
    
    private String description;

   
    private Long reviewStatus;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expectedCompletionDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualCompletionDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate;
    
    private String quarter;
    
    private Long quarterId;
    
    
    private String remarks;
    
    
}

















































