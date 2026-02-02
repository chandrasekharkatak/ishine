package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TrainingHistoryDTO {
	
	private Integer trainingId;
	private String trainingName;
	private String trainingType;
	private String completedOn;
	private String status; // COMPLETED, SKIPPED, PENDING
	private Integer cycleNumber;
	private Integer skipCount;
	private Integer completionCount;
	private Integer contentId;
	private String contentName;
	private String contentType;
}
