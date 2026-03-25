package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEntryResponseDTONew {
    
    private Long projectId;
    private String projectName;
    private String clientSideId;
    private Boolean hasClientSideId;
    
    // Office times
    private LocalDateTime officeInTime;
    private LocalDateTime officeOutTime;
    
    // Client times
    private LocalDateTime clientInTime;
    private LocalDateTime clientOutTime;
    private Integer totalClientWorkingMinutes;
    private Double totalClientWorkingHours;
    
    // Shadow timesheet
    private Long shadowEmpId;
    private String shadowEmployeeName;
    private Boolean isShadowTimesheet;
    
    // Client approval status
    private String clientApprovalStatus;
    
    // Activities
    private List<ActivityResponseDTONew> activities;
}

