package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.EmployeeTimesheetDTO;
import com.apmosys.employeeportal.dto.ProjectTimesheetDTO;
import com.apmosys.employeeportal.model.EmployeeTimesheetsNew;
import com.apmosys.employeeportal.repository.EmployeeTimesheetsNewRepository;
import com.apmosys.employeeportal.service.helper.TimesheetAggregationHelper;
import com.apmosys.employeeportal.service.mapper.TimesheetMapper;

/**
 * Service for EmployeeTimesheet CRUD operations.
 * Handles employee-level timesheet data (one per day per employee).
 * 
 * @author System
 * @version 1.0
 */
@Service
public class EmployeeTimesheetService {

    @Autowired
    private EmployeeTimesheetsNewRepository employeeTimesheetsNewRepository;

    @Autowired
    private TimesheetMapper timesheetMapper;

    @Autowired
    private TimesheetAggregationHelper aggregationHelper;

    /**
     * Create a new EmployeeTimesheet.
     * 
     * @param dto EmployeeTimesheetDTO
     * @return Created EmployeeTimesheetsNew entity
     */
    @Transactional
    public EmployeeTimesheetsNew create(EmployeeTimesheetDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("EmployeeTimesheetDTO cannot be null");
        }

        // Set timestamps if not provided
        if (dto.getCreatedOn() == null) {
            dto.setCreatedOn(LocalDateTime.now());
        }
        if (dto.getUpdatedOn() == null) {
            dto.setUpdatedOn(LocalDateTime.now());
        }

        EmployeeTimesheetsNew entity = timesheetMapper.toEntity(dto);
        return employeeTimesheetsNewRepository.save(entity);
    }

    /**
     * Find EmployeeTimesheet by ID.
     * 
     * @param timesheetId Timesheet ID
     * @return EmployeeTimesheetDTO or null if not found
     */
    public EmployeeTimesheetDTO findById(Long timesheetId) {
        if (timesheetId == null) {
            return null;
        }

        Optional<EmployeeTimesheetsNew> entity = employeeTimesheetsNewRepository.findById(timesheetId);
        return entity.map(timesheetMapper::toDTO).orElse(null);
    }

    /**
     * Find EmployeeTimesheet by employee ID and date.
     * 
     * @param empId Employee ID
     * @param date Date
     * @return EmployeeTimesheetDTO or null if not found
     */
    public EmployeeTimesheetDTO findByEmpIdAndDate(Long empId, LocalDate date) {
        if (empId == null || date == null) {
            return null;
        }

        // Note: Repository method returns Timesheet (old entity), need to add new method
        // For now, using query to find by empId and date
        Optional<EmployeeTimesheetsNew> entity = employeeTimesheetsNewRepository
                .findByEmpIdAndDateNew(empId, date);
        return entity.map(timesheetMapper::toDTO).orElse(null);
    }

    /**
     * Find EmployeeTimesheets by employee ID and date range.
     * 
     * @param empId Employee ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of EmployeeTimesheetDTO
     */
    public List<EmployeeTimesheetDTO> findByEmpIdAndDateRange(Long empId, LocalDate startDate, LocalDate endDate) {
        if (empId == null || startDate == null || endDate == null) {
            return List.of();
        }

        List<EmployeeTimesheetsNew> entities = employeeTimesheetsNewRepository
                .findAllByEmpIdAndDateBetweenOrderByDateDescNew(empId, startDate, endDate);
        return entities.stream()
                .map(timesheetMapper::toDTO)
                .toList();
    }

    /**
     * Update EmployeeTimesheet.
     * 
     * @param dto EmployeeTimesheetDTO with updated data
     * @return Updated EmployeeTimesheetsNew entity
     */
    @Transactional
    public EmployeeTimesheetsNew update(EmployeeTimesheetDTO dto) {
        if (dto == null || dto.getTimesheetId() == null) {
            throw new IllegalArgumentException("EmployeeTimesheetDTO and timesheetId are required");
        }

        EmployeeTimesheetsNew entity = employeeTimesheetsNewRepository.findById(dto.getTimesheetId())
                .orElseThrow(() -> new IllegalArgumentException("Timesheet not found: " + dto.getTimesheetId()));

        // Update fields
        entity.setDayTypeId(dto.getDayTypeId());
        entity.setLeaveTypeMasterId(dto.getLeaveTypeId());
        entity.setStatus(dto.getStatus());
        entity.setTotalWorkingMinutes(dto.getTotalWorkingMinutes());
        entity.setTotalActivitiesMinutes(dto.getTotalActivitiesMinutes());
        entity.setOfficeInTime(dto.getOfficeInTime());
        entity.setOfficeOutTime(dto.getOfficeOutTime());
        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedOn(LocalDateTime.now());

        return employeeTimesheetsNewRepository.save(entity);
    }

    /**
     * Delete EmployeeTimesheet by ID.
     * Note: This will cascade delete related ProjectTimesheets and Activities.
     * 
     * @param timesheetId Timesheet ID
     */
    @Transactional
    public void delete(Long timesheetId) {
        if (timesheetId == null) {
            throw new IllegalArgumentException("Timesheet ID is required");
        }

        if (!employeeTimesheetsNewRepository.existsById(timesheetId)) {
            throw new IllegalArgumentException("Timesheet not found: " + timesheetId);
        }

        employeeTimesheetsNewRepository.deleteById(timesheetId);
    }

    /**
     * Calculate and update status based on project statuses.
     * 
     * @param timesheetId Timesheet ID
     * @param projectTimesheets List of project timesheets
     * @return Updated status
     */
    @Transactional
    public Integer calculateAndUpdateStatus(Long timesheetId, List<ProjectTimesheetDTO> projectTimesheets) {
        if (timesheetId == null) {
            throw new IllegalArgumentException("Timesheet ID is required");
        }

        Integer calculatedStatus = aggregationHelper.calculateEmployeeTimesheetStatus(projectTimesheets);

        EmployeeTimesheetsNew entity = employeeTimesheetsNewRepository.findById(timesheetId)
                .orElseThrow(() -> new IllegalArgumentException("Timesheet not found: " + timesheetId));

        entity.setStatus(calculatedStatus);
        entity.setUpdatedOn(LocalDateTime.now());
        employeeTimesheetsNewRepository.save(entity);

        return calculatedStatus;
    }

    /**
     * Calculate and update totals.
     * 
     * @param timesheetId Timesheet ID
     * @param employeeTimesheet EmployeeTimesheetDTO
     * @param projectTimesheets List of project timesheets
     */
    @Transactional
    public void calculateAndUpdateTotals(Long timesheetId, EmployeeTimesheetDTO employeeTimesheet, 
                                       List<ProjectTimesheetDTO> projectTimesheets) {
        if (timesheetId == null) {
            throw new IllegalArgumentException("Timesheet ID is required");
        }

        // Calculate totals
        aggregationHelper.calculateAndSetEmployeeTimesheetTotals(employeeTimesheet, projectTimesheets);

        // Update entity
        EmployeeTimesheetsNew entity = employeeTimesheetsNewRepository.findById(timesheetId)
                .orElseThrow(() -> new IllegalArgumentException("Timesheet not found: " + timesheetId));

        entity.setTotalActivitiesMinutes(employeeTimesheet.getTotalActivitiesMinutes());
        entity.setTotalWorkingMinutes(employeeTimesheet.getTotalWorkingMinutes());
        entity.setStatus(employeeTimesheet.getStatus());
        entity.setUpdatedOn(LocalDateTime.now());

        employeeTimesheetsNewRepository.save(entity);
    }

    /**
     * Check if timesheet exists for employee and date.
     * 
     * @param empId Employee ID
     * @param date Date
     * @return true if exists, false otherwise
     */
    public boolean existsByEmpIdAndDate(Long empId, LocalDate date) {
        if (empId == null || date == null) {
            return false;
        }

        return employeeTimesheetsNewRepository.findByEmpIdAndDate(empId, date).isPresent();
    }
}

