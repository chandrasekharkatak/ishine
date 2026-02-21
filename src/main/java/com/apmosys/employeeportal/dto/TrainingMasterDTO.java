package com.apmosys.employeeportal.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
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
    private LocalDateTime createdOn; 
    private Long updatedBy;
    private String updatedByName;
    private LocalDateTime updatedOn;  

    public TrainingMasterDTO(Integer trainingId, String trainingName, 
            String trainingType, String mandatoryFlag, LocalDate effectiveFrom, 
            LocalDate effectiveTo, String lockEnabled, Integer minViewTimeMinutes, 
            String consentRequired, String skipAllowed, String deadlineEnabled, 
            String deadlinePattern, String customDeadlineMonths, 
            String activeStatus, Long createdBy, String createdByName, 
            LocalDateTime createdOn, Long updatedBy, LocalDateTime updatedOn) {
        
        this.trainingId = trainingId;
        this.trainingName = trainingName;
        this.trainingType = trainingType;
        this.mandatoryFlag = mandatoryFlag;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.lockEnabled = lockEnabled;
        this.minViewTimeMinutes = minViewTimeMinutes;
        this.consentRequired = consentRequired;
        this.skipAllowed = skipAllowed;
        this.deadlineEnabled = deadlineEnabled;
        this.deadlinePattern = deadlinePattern;
        this.customDeadlineMonths = customDeadlineMonths;
        this.activeStatus = activeStatus;
        this.createdBy = createdBy;
        this.createdByName = createdByName;
        this.createdOn = createdOn;
        this.updatedBy = updatedBy;
        this.updatedOn = updatedOn;
    }

	
}