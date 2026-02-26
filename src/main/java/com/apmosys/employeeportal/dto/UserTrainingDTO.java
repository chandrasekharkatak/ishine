package com.apmosys.employeeportal.dto;

import java.time.LocalDate;

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
	private LocalDate deadline;
	private Boolean isDeadlineCrossed;
	private Boolean lockEnabled;
	private String mandatoryFlag;
	private TrainingContentDTO content;
	private Integer completionCount;
	private Integer requiredFrequency;
	private Integer minViewTimeMinutes;
	private String consentRequired;
	private String skipAllowed;
	private LocalDate lastCompletedOn;
	private Integer skipCount;
	private Boolean hasQuiz;
	private Boolean quizAttempted;
	private Boolean hasSeenContent;
}
