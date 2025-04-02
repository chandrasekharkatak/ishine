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
	
	private Long kraKpiScore;//change to float
	
	private Long questionnaireScore;//change to float

}
