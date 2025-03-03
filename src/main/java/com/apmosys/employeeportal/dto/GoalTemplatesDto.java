package com.apmosys.employeeportal.dto;

import lombok.*;


@Getter 
@Setter
public class GoalTemplatesDto {
	private Long templateId;
	private String description;
	private Long createdById;
	private String createdByName;
	private String department;
	private Long approvedById;
	private String approvedByName;
	private Boolean isApproved;
}
