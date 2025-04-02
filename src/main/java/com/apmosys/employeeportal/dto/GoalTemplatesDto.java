package com.apmosys.employeeportal.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

import lombok.*;

@Getter
@Setter
public class GoalTemplatesDto {

	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long templateId;
	
    private String title; 
    private String description;
    private Long createdById;
    private Long departmentId;
    private String department;
    private Long approvedById;
    private String approvedByName;
    private Boolean isApproved;
}