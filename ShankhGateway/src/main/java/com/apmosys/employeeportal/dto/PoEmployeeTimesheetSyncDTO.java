package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PoEmployeeTimesheetSyncDTO {

	private Long employeeMentId;
    private String employeeName;
    private String currentStatus;
    private Timestamp startDate;
    private Timestamp endDate; 
    private Long lastTimesheetFilledPoProjectId; 
    private String lastTimesheetFilledProjectName; 
    private Boolean isInternal;
    private String employmentId;
    
}
