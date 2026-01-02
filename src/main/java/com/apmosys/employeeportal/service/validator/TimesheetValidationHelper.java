package com.apmosys.employeeportal.service.validator;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.apmosys.employeeportal.dto.ActivityTimesheetDTO;
import com.apmosys.employeeportal.dto.EmployeeTimesheetDTO;
import com.apmosys.employeeportal.dto.ProjectTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.ActivitiesRepository;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.Activity;

/**
 * Validation helper for new timesheet structure.
 * Provides validation methods for all levels of timesheet hierarchy.
 * 
 * @author System
 * @version 1.0
 */
@Component
public class TimesheetValidationHelper {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EmployeeTeamMapRepository employeeTeamMapRepository;

    @Autowired
    private ActivitiesRepository activitiesRepository;

    /**
     * Validate EmployeeTimesheet DTO.
     * 
     * @param dto EmployeeTimesheetDTO
     * @throws IllegalArgumentException if validation fails
     */
    public void validateEmployeeTimesheet(EmployeeTimesheetDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("EmployeeTimesheetDTO cannot be null");
        }

        if (dto.getEmpId() == null) {
            throw new IllegalArgumentException("Employee ID is required");
        }

        if (dto.getDate() == null) {
            throw new IllegalArgumentException("Date is required");
        }

        if (dto.getDayTypeId() == null) {
            throw new IllegalArgumentException("Day Type ID is required");
        }

        // Validate employee exists and is active
        Employee employee = employeeRepository.findById(dto.getEmpId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + dto.getEmpId()));

        if ("InActive".equalsIgnoreCase(employee.getEmploymentstatus())) {
            throw new IllegalArgumentException("Employee is inactive: " + dto.getEmpId());
        }

        // Validate date not in future
        validateDateNotInFuture(dto.getDate());

        // Validate office times if provided
        if (dto.getOfficeInTime() != null && dto.getOfficeOutTime() != null) {
            validateOfficeTimes(dto.getOfficeInTime(), dto.getOfficeOutTime());
        }
    }

    /**
     * Validate ProjectTimesheet DTO.
     * 
     * @param dto ProjectTimesheetDTO
     * @param empId Employee ID for validation
     * @throws IllegalArgumentException if validation fails
     */
    public void validateProjectTimesheet(ProjectTimesheetDTO dto, Long empId) {
        if (dto == null) {
            throw new IllegalArgumentException("ProjectTimesheetDTO cannot be null");
        }

        if (dto.getProjectId() == null) {
            throw new IllegalArgumentException("Project ID is required");
        }

        // Validate project exists and is active
        Project project = projectRepository.findByProjectId(dto.getProjectId().intValue());
        if (project == null) {
            throw new IllegalArgumentException("Project not found: " + dto.getProjectId());
        }

        if (!"true".equalsIgnoreCase(project.getActive())) {
            throw new IllegalArgumentException("Project is not active: " + dto.getProjectId());
        }

        // Validate employee is assigned to project
        validateProjectAssignment(empId, dto.getProjectId());

        // Validate client times if provided
        if (dto.getClientInTime() != null && dto.getClientOutTime() != null) {
            validateClientTimes(dto.getClientInTime(), dto.getClientOutTime());
        }

        // Validate activities if provided
        if (dto.getActivities() != null && !dto.getActivities().isEmpty()) {
            for (ActivityTimesheetDTO activity : dto.getActivities()) {
                validateActivityTimesheet(activity, dto.getProjectId());
            }
        }
    }

    /**
     * Validate ActivityTimesheet DTO.
     * 
     * @param dto ActivityTimesheetDTO
     * @param projectId Project ID for validation
     * @throws IllegalArgumentException if validation fails
     */
    public void validateActivityTimesheet(ActivityTimesheetDTO dto, Long projectId) {
        if (dto == null) {
            throw new IllegalArgumentException("ActivityTimesheetDTO cannot be null");
        }

        if (dto.getActivityId() == null) {
            throw new IllegalArgumentException("Activity ID is required");
        }

        if (dto.getDurationMinutes() == null) {
            throw new IllegalArgumentException("Duration minutes is required");
        }

        validateActivityDuration(dto.getDurationMinutes());

        // Validate activity exists
        Activity activity = activitiesRepository.findById(dto.getActivityId())
                .orElseThrow(() -> new IllegalArgumentException("Activity not found: " + dto.getActivityId()));

        // Validate project ID matches
        if (projectId != null && !projectId.equals(dto.getProjectId())) {
            throw new IllegalArgumentException("Activity project ID does not match project timesheet project ID");
        }
    }

    /**
     * Validate complete TimesheetDTO structure.
     * 
     * @param dto TimesheetDTO
     * @throws IllegalArgumentException if validation fails
     */
    public void validateTimesheetStructure(TimesheetDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("TimesheetDTO cannot be null");
        }

        if (dto.getEmployeeTimesheet() == null) {
            throw new IllegalArgumentException("EmployeeTimesheet is required");
        }

        // Validate employee timesheet
        validateEmployeeTimesheet(dto.getEmployeeTimesheet());

        Long empId = dto.getEmployeeTimesheet().getEmpId();

        // Validate project timesheets
        if (dto.getProjectTimesheets() != null && !dto.getProjectTimesheets().isEmpty()) {
            for (ProjectTimesheetDTO projectDTO : dto.getProjectTimesheets()) {
                validateProjectTimesheet(projectDTO, empId);
            }
        }
    }

    /**
     * Validate date is not in future.
     * 
     * @param date Date to validate
     * @throws IllegalArgumentException if date is in future
     */
    public void validateDateNotInFuture(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }

        LocalDate today = LocalDate.now();
        if (date.isAfter(today)) {
            throw new IllegalArgumentException("Timesheet date cannot be in future: " + date);
        }
    }

    /**
     * Validate date is not within lock period.
     * 
     * @param empId Employee ID
     * @param date Date to validate
     * @param lockDays Number of days to lock
     * @return true if date is locked, false otherwise
     */
    public boolean isDateLocked(Long empId, LocalDate date, Integer lockDays) {
        if (date == null || lockDays == null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        LocalDate lockDate = today.minusDays(lockDays);

        return date.isBefore(lockDate);
    }

    /**
     * Validate office in/out times.
     * 
     * @param officeInTime Office in time
     * @param officeOutTime Office out time
     * @throws IllegalArgumentException if validation fails
     */
    public void validateOfficeTimes(LocalDateTime officeInTime, LocalDateTime officeOutTime) {
        if (officeInTime == null || officeOutTime == null) {
            throw new IllegalArgumentException("Both office in time and office out time are required");
        }

        if (officeOutTime.isBefore(officeInTime)) {
            throw new IllegalArgumentException("Office out time cannot be before office in time");
        }

        if (officeOutTime.isEqual(officeInTime)) {
            throw new IllegalArgumentException("Office out time cannot be equal to office in time");
        }
    }

    /**
     * Validate client in/out times.
     * 
     * @param clientInTime Client in time
     * @param clientOutTime Client out time
     * @throws IllegalArgumentException if validation fails
     */
    public void validateClientTimes(LocalDateTime clientInTime, LocalDateTime clientOutTime) {
        if (clientInTime == null || clientOutTime == null) {
            throw new IllegalArgumentException("Both client in time and client out time are required");
        }

        if (clientOutTime.isBefore(clientInTime)) {
            throw new IllegalArgumentException("Client out time cannot be before client in time");
        }

        if (clientOutTime.isEqual(clientInTime)) {
            throw new IllegalArgumentException("Client out time cannot be equal to client in time");
        }
    }

    /**
     * Validate activity duration.
     * 
     * @param durationMinutes Duration in minutes
     * @throws IllegalArgumentException if validation fails
     */
    public void validateActivityDuration(Integer durationMinutes) {
        if (durationMinutes == null) {
            throw new IllegalArgumentException("Duration minutes cannot be null");
        }

        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Duration must be greater than 0 minutes");
        }

        // Maximum 24 hours (1440 minutes) per activity
        if (durationMinutes > 1440) {
            throw new IllegalArgumentException("Duration cannot exceed 24 hours (1440 minutes)");
        }
    }

    /**
     * Validate employee is assigned to project.
     * 
     * @param empId Employee ID
     * @param projectId Project ID
     * @throws IllegalArgumentException if employee is not assigned to project
     */
    public void validateProjectAssignment(Long empId, Long projectId) {
        if (empId == null || projectId == null) {
            throw new IllegalArgumentException("Employee ID and Project ID are required");
        }

        // Check if employee is assigned to any team in the project
        boolean isAssigned = employeeTeamMapRepository.findByProjectIdAndActive(projectId.intValue(), 1L)
                .stream()
                .anyMatch(etm -> etm.getEmpId().equals(empId));

        if (!isAssigned) {
            throw new IllegalArgumentException("Employee " + empId + " is not assigned to project " + projectId);
        }
    }

    /**
     * Validate timesheet date is not locked.
     * 
     * @param empId Employee ID
     * @param date Date to validate
     * @param lockDays Number of lock days
     * @throws IllegalArgumentException if date is locked
     */
    public void validateDateNotLocked(Long empId, LocalDate date, Integer lockDays) {
        if (isDateLocked(empId, date, lockDays)) {
            throw new IllegalArgumentException("Timesheet date is locked. Cannot modify timesheets older than " + lockDays + " days");
        }
    }
}

