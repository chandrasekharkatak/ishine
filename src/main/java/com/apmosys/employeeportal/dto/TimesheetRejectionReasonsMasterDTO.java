package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public class TimesheetRejectionReasonsMasterDTO {

	private Long rejectionReason;
	private Boolean active;
	private Long createdBy;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdOn;
	private Long updatedBy;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updatedOn;
	
}
