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
    private Long projectId;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime clientInTime;

    /**
     * Client out time
     * Optional - for client-side time tracking
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime clientOutTime;

    /**
     * Is night shift
     * Optional - indicates if this is a night shift
     */
    private Boolean isNightShift;

    /**
     * Client approval status ID (FK to client_status_master_new)
     * Values: 1=Pending, 2=Approved, 3=Rejected
     * Optional - defaults to Pending
     */
    private Integer clientApprovalStatus;

    /**
     * Status ID (FK to status_master_new)
     * Values: 1=Pending, 2=Approved, 3=Rejected
     * Required - defaults to Pending
     */
    @NotNull( "Status is required")
    private Integer status;

    /**
     * Shadow employee ID
     * Optional - for shadow timesheet scenarios
     */
    private Long shadowEmpId;

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
}

