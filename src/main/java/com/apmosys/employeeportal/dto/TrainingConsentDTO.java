package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TrainingConsentDTO {
	
	private Long consentId;
	private Integer trainingId;
	private String trainingName;
	private Integer contentId;
	private String contentName;
	private Long empId;
	private String empName;
	private String consentTimestamp;
	private Integer completionCycleNumber;
	private Long createdBy;
}
