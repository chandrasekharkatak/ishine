package com.apmosys.employeeportal.dto;

import java.sql.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TrainingMasterDTO {
	
	private Integer trainingId;
	private String trainingName;
	private String trainingType;
	private String mandatoryFlag;
	private Date effectiveFrom;
	private Date effectiveTo;
//	private Integer frequencyPerYear;
	private String lockEnabled;
	private Integer minViewTimeMinutes;
	private String consentRequired;
	private String skipAllowed;
	private String deadlineEnabled;
	private String deadlinePattern;
	private String customDeadlineMonths;
	private String activeStatus;
	private Long createdBy;
	private String createdByName;
	private String createdOn;
	private Long updatedBy;
	private String updatedByName;
	private String updatedOn;
}
