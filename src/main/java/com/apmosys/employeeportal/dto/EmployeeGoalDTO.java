package com.apmosys.employeeportal.dto;

//import com.apmosys.employeeportal.model.EmployeeGoals.ReviewStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.*;


import java.time.LocalDate;
import java.util.List;

@Data
@Getter
@Setter
public class EmployeeGoalDTO {
    private Long goalId;
    private Long empId;
    private String goalTitle;
    private String description;
    private String goalStatus;
    private Integer goalProgress;
    private String managerRemark;
    private String employeeRemark;
    private Long templateId;
    private String assignedBy;
    private String quarter;
    private Long quarterId;
    private String expectedCompletionDate;
    private String createdDate;
    private Long assignById;
    private List<GoalRemarksDTO> remarks;
    
    // Getters and setters
}
   

















































