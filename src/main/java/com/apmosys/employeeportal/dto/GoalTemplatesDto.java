package com.apmosys.employeeportal.dto;

import lombok.*;

@Getter
@Setter
public class GoalTemplatesDto {

    private Long templateId;
    private String title; 
//    private Long quarterId;
    private String description;
    private Long createdById;
    private Long departmentId;
    private String department;
    private Long approvedById;
    private String approvedByName;
    private Boolean isApproved;
}