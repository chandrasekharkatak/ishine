package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TrainingSkipDTO {
	
	private Long skipId;
	private Integer trainingId;
	private String trainingName;
	private Long empId;
	private String empName;
	private Integer cycleNumber;
	private Integer skipCount;
	private String firstSkippedOn;
	private String lastSkippedOn;
}
