package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TrainingFrequencyDTO {
	
	private Integer trainingId;
	private String trainingName;
	private Integer completionCount;
	private Integer requiredFrequency;
	private Boolean needsAssignment;
	private String lastCompletedOn;
}
