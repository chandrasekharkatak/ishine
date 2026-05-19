package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class ReimbursementSubmissionSettingsDTO {
	private Integer monthlyDeadlineDay;
	/** When true, submissions are only allowed on days 1..monthlyDeadlineDay each month. */
	private Boolean enabled;
	private Long updatedBy;
}
