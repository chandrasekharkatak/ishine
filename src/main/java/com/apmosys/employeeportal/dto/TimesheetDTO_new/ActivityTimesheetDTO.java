package com.apmosys.employeeportal.dto.TimesheetDTO_new;


import org.jetbrains.annotations.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing activity-level timesheet data.
 * Multiple Activities per project (nested under ProjectTimesheetDTO).
 * Maps to: employee_timesheet_activities_mapping_new table
 * 
 * @author System
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityTimesheetDTO {
	
	
	private Long id;

    /**
     * Timesheet ID (FK to employee_timesheets_new)
     * Required - links to parent EmployeeTimesheet
     */
    @NotNull("Timesheet ID is required")
    private Long timesheetId;

    /**
     * Activity ID (FK to activities table)
     * Required for creation
     */
    @NotNull("Activity ID is required")
    private Long activityId;

    /**
     * Project ID (FK to projects table)
     * Required - part of composite key
     * Must match parent ProjectTimesheetDTO.projectId
     */
    @NotNull( "Project ID is required")
    private Integer projectId;

    /**
     * Activity description
     * Optional - can be auto-filled from activity master
     */
    private String description;

    /**
     * Duration in minutes
     * Required - must be greater than 0
     */
    @NotNull("Duration minutes is required")
    private Integer durationMinutes;

   
}

