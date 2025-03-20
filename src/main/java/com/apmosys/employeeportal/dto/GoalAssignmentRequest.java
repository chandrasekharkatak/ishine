package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoalAssignmentRequest {
    private Long empId;
    private Long templateId;
    private String expectedCompletionDate;
    private Long quarterId;
}
