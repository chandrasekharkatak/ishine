package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SummaryDto {
	
	private Long quarter;
	
	private Long empId;
	
	private Long goalsCompleted;
	
	private Long goalsRemaining;
	
	private Long kraKpiScore;
	
	private Long questionnaireScore;

}
