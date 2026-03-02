package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ComplianceReportDTO {
	
	private Integer trainingId;
	private String trainingName;
	private Long totalAssigned;
	private Long completed;
	private Long pending;
	private Long skipped;
	private Double compliancePercentage;
}
