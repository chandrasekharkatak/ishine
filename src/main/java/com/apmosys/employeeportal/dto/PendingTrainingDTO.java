package com.apmosys.employeeportal.dto;

import java.sql.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PendingTrainingDTO {
	
	private Integer trainingId;
	private String trainingName;
	private String trainingType;
	private Integer minViewTimeMinutes;
	private String consentRequired;
	private String skipAllowed;
	private String deadlineEnabled;
	private String deadlinePattern;
	private String customDeadlineMonths;
	private Integer currentCycleNumber;
	private Date currentCycleDeadline;
	private Boolean isDeadlineCrossed;
	private TrainingContentDTO content;
}
