package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProjectOverheadMappingDTO {
	
	private Long projectId;
	private Long projectOverheadId;
	private Integer active;
	private String createdOn;
	private String updatedOn;
	private Long updatedBy;
	private Long createdBy;
	
}
