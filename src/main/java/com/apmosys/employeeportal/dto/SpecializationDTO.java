package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SpecializationDTO {
	
	private Long specializationId;
	private String specializationName;
	private Long domainId;
	private Long createdBy;
	private Long updatedBy;
	private LocalDateTime updatedOn;
	private LocalDateTime createdOn;
	
}
