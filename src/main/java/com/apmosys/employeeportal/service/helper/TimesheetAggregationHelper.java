package com.apmosys.employeeportal.service.helper;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Component;

import com.apmosys.employeeportal.dto.TimesheetDTO_new.EmployeeTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.ActivityTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.ProjectTimesheetDTO;
import com.apmosys.employeeportal.utility.DateConversionUtil;

/**
 * Helper class for timesheet aggregation and calculation logic.
 * Handles all calculations for totals, status, and minutes.
 * 
 * @author System
 * @version 1.0
 */
@Component
public class TimesheetAggregationHelper {

    // Status constants
    public static final Integer STATUS_PENDING = 1;
    public static final Integer STATUS_APPROVED = 2;
    public static final Integer STATUS_REJECTED = 3;
    
    
    private final String pattern="yyyy-MM-dd HH:mm:ss";

    /**
     * Calculate total activities minutes from all activities across all projects.
     * 
     * @param projectTimesheets List of project timesheets
     * @return Total minutes from all activities
     */
    public Integer calculateTotalActivitiesMinutes(List<ProjectTimesheetDTO> projectTimesheets) {
        if (projectTimesheets == null || projectTimesheets.isEmpty()) {
            return 0;
        }

        return projectTimesheets.stream()
                .flatMap(project -> project.getActivities() != null ? project.getActivities().stream() : java.util.stream.Stream.empty())
                .mapToInt(activity -> activity.getDurationMinutes() != null ? activity.getDurationMinutes() : 0)
                .sum();
    }

    /**
     * Calculate total working minutes from office in/out times.
     * If office times are not provided, returns null.
     * 
     * @param officeInTime Office in time
     * @param officeOutTime Office out time
     * @return Total working minutes, or null if times not provided
     */
    public Integer calculateTotalWorkingMinutes(LocalDateTime officeInTime, LocalDateTime officeOutTime) {
        if (officeInTime == null || officeOutTime == null) {
            return null;
        }

        if (officeOutTime.isBefore(officeInTime)) {
            throw new IllegalArgumentException("Office out time cannot be before office in time");
        }

        return (int) ChronoUnit.MINUTES.between(officeInTime, officeOutTime);
    }

    /**
     * Calculate total client working minutes for a specific project.
     * Sums all activity durations for the project.
     * 
     * @param activities List of activities for the project
     * @return Total minutes for the project
     */
    public Integer calculateTotalClientWorkingMinutes(List<ActivityTimesheetDTO> activities) {
        if (activities == null || activities.isEmpty()) {
            return 0;
        }

        return activities.stream()
                .mapToInt(activity -> activity.getDurationMinutes() != null ? activity.getDurationMinutes() : 0)
                .sum();
    }

    /**
     * Calculate total client working minutes from client in/out times.
     * If client times are not provided, returns null.
     * 
     * @param clientInTime Client in time
     * @param clientOutTime Client out time
     * @return Total client working minutes, or null if times not provided
     */
    public Integer calculateTotalClientWorkingMinutesFromTimes(LocalDateTime clientInTime, LocalDateTime clientOutTime) {
        if (clientInTime == null || clientOutTime == null) {
            return null;
        }

        if (clientOutTime.isBefore(clientInTime)) {
            throw new IllegalArgumentException("Client out time cannot be before client in time");
        }

        return (int) ChronoUnit.MINUTES.between(clientInTime, clientOutTime);
    }

    /**
     * Calculate EmployeeTimesheet status based on project statuses.
     * 
     * Rules:
     * - If ALL projects approved → APPROVED
     * - If ANY project rejected → REJECTED
     * - If ANY project pending → PENDING
     * - If MIXED (some approved, some pending) → PARTIAL
     * 
     * @param projectTimesheets List of project timesheets
     * @return Calculated status
     */
    public Integer calculateEmployeeTimesheetStatus(List<ProjectTimesheetDTO> projectTimesheets) {
        if (projectTimesheets == null || projectTimesheets.isEmpty()) {
            return STATUS_PENDING;
        }

        boolean allApproved = projectTimesheets.stream()
                .allMatch(p -> p.getStatus() != null && p.getStatus().equals(STATUS_APPROVED));

        boolean anyRejected = projectTimesheets.stream()
                .anyMatch(p -> p.getStatus() != null && p.getStatus().equals(STATUS_REJECTED));

        boolean anyPending = projectTimesheets.stream()
                .anyMatch(p -> p.getStatus() == null || p.getStatus().equals(STATUS_PENDING));

        if (allApproved) {
            return STATUS_APPROVED;
        }
        if (anyRejected) {
            return STATUS_REJECTED;
        }
        if (anyPending) {
            return STATUS_PENDING;
        }
        
        // Mixed state (some approved, some not)
        return STATUS_PARTIAL;
    }

    /**
     * Calculate and set totals for EmployeeTimesheet.
     * 
     * @param employeeTimesheet Employee timesheet DTO
     * @param projectTimesheets List of project timesheets
     */
    public void calculateAndSetEmployeeTimesheetTotals(EmployeeTimesheetDTO employeeTimesheet, 
                                                       List<ProjectTimesheetDTO> projectTimesheets) {
        // Calculate total activities minutes
        Integer totalActivitiesMinutes = calculateTotalActivitiesMinutes(projectTimesheets);
        employeeTimesheet.setTotalActivitiesMinutes(totalActivitiesMinutes);

        // Calculate total working minutes
        if (employeeTimesheet.getWorkCheckIn() != null && employeeTimesheet.getWorkCheckOut() != null) {
            Integer totalWorkingMinutes = calculateTotalWorkingMinutes(
            		DateConversionUtil.stringToLocalDateTime(employeeTimesheet.getWorkCheckIn(),pattern), 
            		DateConversionUtil.stringToLocalDateTime(employeeTimesheet.getWorkCheckOut(),pattern));
            employeeTimesheet.setTotalWorkingMinutes(totalWorkingMinutes);
        } else {
            // If office times not provided, use activities minutes
            employeeTimesheet.setTotalWorkingMinutes(totalActivitiesMinutes);
        }

        // Calculate status
        Integer status = calculateEmployeeTimesheetStatus(projectTimesheets);
        employeeTimesheet.setStatus(status);
    }

    /**
     * Calculate and set totals for each ProjectTimesheet.
     * 
     * @param projectTimesheet Project timesheet DTO
     */
    public void calculateAndSetProjectTimesheetTotals(ProjectTimesheetDTO projectTimesheet) {
        if (projectTimesheet.getActivities() != null && !projectTimesheet.getActivities().isEmpty()) {
            Integer totalMinutes = calculateTotalClientWorkingMinutes(projectTimesheet.getActivities());
            projectTimesheet.setTotalClientWorkingMinutes(totalMinutes);
        }else {
            // No activities or times provided
            projectTimesheet.setTotalClientWorkingMinutes(0);
        }
    }

    /**
     * Calculate and set totals for all project timesheets.
     * 
     * @param projectTimesheets List of project timesheets
     */
    public void calculateAndSetAllProjectTimesheetTotals(List<ProjectTimesheetDTO> projectTimesheets) {
        if (projectTimesheets != null) {
            projectTimesheets.forEach(this::calculateAndSetProjectTimesheetTotals);
        }
    }
}

