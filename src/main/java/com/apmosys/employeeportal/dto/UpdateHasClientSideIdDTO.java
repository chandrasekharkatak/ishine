package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UpdateHasClientSideIdDTO {
	
	private Integer projectId;
	private Boolean hasClientSideId;
	private Long currentUserEmpId;
	private LocalDateTime updatedOn;
}
