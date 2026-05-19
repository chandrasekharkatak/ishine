package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEntryRequestDTONew {
    
    private Long projectId;
    private String clientSideId;
    private Boolean hasClientSideId;
    
    // Office times
    private LocalDateTime officeInTime;
    private LocalDateTime officeOutTime;
    
    // Client times
    private LocalDateTime clientInTime;
    private LocalDateTime clientOutTime;
    private Integer totalClientWorkingMinutes;
    
    // Shadow timesheet
    private Long shadowEmpId;
    private Boolean isShadowTimesheet;
    
    // Client approval status (per-project)
    private String clientApprovalStatus;
    
    // Activities
    private List<ActivityRequestDTONew> activities;
}

