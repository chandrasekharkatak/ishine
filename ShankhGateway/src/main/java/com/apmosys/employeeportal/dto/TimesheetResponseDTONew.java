package com.apmosys.employeeportal.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetResponseDTONew {
    
    private Long timesheetId;
    private Long empId;
    private String employeeName;
    private LocalDate date;
    private Integer dayTypeId;
    private String dayType;
    private Integer status;
    private String statusName;
    private String description;
    private Long leaveTypeMasterId;
    
    // Office times
    private LocalDateTime officeInTime;
    private LocalDateTime officeOutTime;
    private Integer totalWorkingMinutes;
    private Double totalWorkingHours;
    private Boolean isNightShift;
    private LocalDate toDate;
    
    // Client approval status
    private String clientApprovalStatus;
    
    // Project entries
    private List<ProjectEntryResponseDTONew> projectEntries;
    
    // Documents
    private List<DocumentResponseDTONew> documents;
    
    // Audit fields
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdOn;
    private Long updatedBy;
    private String updatedByName;
    private LocalDateTime updatedOn;
}

