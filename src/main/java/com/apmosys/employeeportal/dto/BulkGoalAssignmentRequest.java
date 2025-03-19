package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class BulkGoalAssignmentRequest {
    private List<Long> empIds;
    private Long templateId;
    private String expectedCompletionDate;
}
