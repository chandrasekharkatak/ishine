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
public class CreateTimesheetRequestDTONew {
    
    private Long empId;
    private LocalDate date;
    private Integer dayTypeId;
    private String dayType;
    private Integer status;
    private String description;
    private Long leaveTypeMasterId;
    
    // Office times
    private LocalDateTime officeInTime;
    private LocalDateTime officeOutTime;
    private Integer totalWorkingMinutes;
    private Boolean isNightShift;
    private LocalDate toDate; // For night shift
    
    // Timesheet level client approval status
    private String clientApprovalStatus;
    
    // Project entries
    private List<ProjectEntryRequestDTONew> projectEntries;
}

