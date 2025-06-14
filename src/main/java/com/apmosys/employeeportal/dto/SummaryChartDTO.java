package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SummaryChartDTO {
	
	private String projectName;
	private Long totalTimesheetsFilled;

}
