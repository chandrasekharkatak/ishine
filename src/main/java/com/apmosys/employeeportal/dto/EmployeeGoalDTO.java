package com.apmosys.employeeportal.dto;

import lombok.*;

import java.time.LocalDate;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonFormat;

@Getter
@Setter

public class EmployeeGoalDTO {
     
	 private Long goalId;
	 private Long empId;
	 private String assignedBy;
	 private Long kpiId;
	 private Long quarterId;
	 private Long templateId;
	 private String goalTitle;
	 private String reviewStatus;
	 @JsonFormat(pattern="dd-MM-yyyy")
	 private LocalDate expectedCompletionDate;
	 @JsonFormat(pattern="dd-MM-yyyy")
	 private LocalDate actualCompletionDate;
	

}
