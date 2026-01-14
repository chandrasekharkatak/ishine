package com.apmosys.employeeportal.dto.TimesheetDTO_new;

import java.time.LocalDateTime;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing project-level timesheet data.
 * Multiple ProjectTimesheets per day (one per project employee works on).
 * Maps to: project_timesheet_status_new table
 * 
 * @author System
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTimesheetDTO {

    /**
     * Timesheet ID (FK to employee_timesheets_new)
     * Required - links to parent EmployeeTimesheet
     */
    @NotNull("Timesheet ID is required")
    private Long timesheetId;

    /**
     * Project ID (FK to projects table)
     * Required for creation
     */
    @NotNull("Project ID is required")
    private Integer projectId;

    /**
     * PO Number
     * Optional - retrieved from project if available
     */
    private String poNo;

    /**
     * PO Project ID
     * Optional - retrieved from project if available
     */
    private Long poId;

    /**
     * Client in time
     * Optional - for client-side time tracking
     */
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    private LocalDateTime clientInTime;

    /**
     * Client out time
     * Optional - for client-side time tracking
     */
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    private LocalDateTime clientOutTime;

    /**
     * Is night shift
     * Optional - indicates if this is a night shift
     */
    private Boolean isNightShift;

    /**
     * Status ID (FK to status_master_new)
     * Values: 1=Pending, 2=Approved, 3=Rejected
     * Required - defaults to Pending
     */
    @NotNull( "Status is required")
    private Integer status;

    
    /**
     * Total client working minutes
     * Calculated from activities for this project
     * Or from clientInTime and clientOutTime if provided
     */
    private Integer totalClientWorkingMinutes;

    /**
     * List of activities for this project
     * Required for working days
     * Can be empty for non-working days
     */
    private List<ActivityTimesheetDTO> activities;
    
    // ========== NEW CONTRACT FIELDS ==========
    
    /**
     * Client ID (FK to clients table)
     * NEW CONTRACT: Required for project identification
     * Retrieved from project if not provided
     */
    private Long clientId;
    
    /**
     * Client approval status as string (e.g., "Pending", "Approved", "Rejected")
     * NEW CONTRACT: Maps to clientApprovalStatus (Integer)
     * Optional - defaults to "Pending"
     */
    private Integer clientApprovalStatus;
    
    /**
     * Project hours in minutes
     * NEW CONTRACT: Maps to totalClientWorkingMinutes
     * Calculated from activities or provided directly
     */
    private Integer projectHoursMinutes;
    
    /**
     * Team ID (FK to teams table)
     * NEW CONTRACT: Required for project identification
     * Retrieved from project if not provided
     */
    private Long teamId;
    
    
    // private Boolean isShadowForSelf;
    
    /**
     * Is shadow timesheet flag (alternative name)
     * NOT IN NEW CONTRACT but kept for backward compatibility
     * Maps to isShadow
     */
    private Boolean isShadowTimesheet;
    
    
    /**
     * Shadow employee ID
     * Optional - for shadow timesheet scenarios
     */
    private Long shadowEmpId;
    
    
    private Boolean isShadowForSelf;
    
    
    private Long locationMappingId;
    
    
    /**
     * Client location ID (FK to client_locations table)
     * Optional - for location-specific tracking
     */
    private Long clientLocationId;
    
    private String description;
    
    private String clientSideId;

    
  
}

