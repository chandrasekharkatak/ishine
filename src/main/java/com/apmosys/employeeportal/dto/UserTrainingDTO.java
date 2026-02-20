package com.apmosys.employeeportal.dto;

import java.sql.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserTrainingDTO {
	
	private Integer trainingId;
	private String trainingName;
	private String trainingType;
	private String status; // PENDING, IN_PROGRESS, COMPLETED, SKIPPED
	private Integer currentCycleNumber;
	private Date deadline;
	private Boolean isDeadlineCrossed;
	private Boolean lockEnabled;
	private String mandatoryFlag;
	private TrainingContentDTO content;
	private Integer completionCount;
	private Integer requiredFrequency;
	private Integer minViewTimeMinutes;
	private String consentRequired;
	private String skipAllowed;
	private Date lastCompletedOn;
	private Integer skipCount;
	private Boolean hasQuiz;
	private Boolean quizAttempted;
}
