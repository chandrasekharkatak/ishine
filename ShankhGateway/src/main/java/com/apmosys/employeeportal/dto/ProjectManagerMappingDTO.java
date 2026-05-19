package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProjectManagerMappingDTO {
	
	private Long projectId;
	private Long projectManagerId;
	private Integer active;
	private String createdOn;
	private String updatedOn;
	private Long updatedBy;
	private Long createdBy;
	
}
