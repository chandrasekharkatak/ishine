package com.apmosys.employeeportal.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.JobRoleAccess;
import com.apmosys.employeeportal.dto.CreateTimesheetRequestDTONew;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.service.TimesheetServiceNew;
import com.apmosys.employeeportal.service.helper.TimesheetEncryptionHelper;
import com.apmosys.employeeportal.utility.ServiceResponse;

/**
 * New Controller for Hierarchical Timesheet APIs
 * 
 * This controller handles all new hierarchical timesheet operations:
 * - EmployeeTimesheet (one per day per employee)
 * - ProjectTimesheets (multiple per day)
 * - Activities (nested under projects)
 * 
 * @author System
 * @version 2.0
 */
@RestController
@RequestMapping(path = "/api/v2/timesheet")
public class EmployeeTimesheetControllerNew {
	
	@Autowired
	TimesheetServiceNew timesheetServiceNew;
	
	@Autowired
	TimesheetEncryptionHelper timesheetEncryptionHelper;
	
	/**
	 * API 1.1: Create Timesheet (New Hierarchical Structure)
	 * Endpoint: POST /api/v2/timesheet/create
	 * 
	 * Creates EmployeeTimesheet, ProjectTimesheets, and Activities in a single transaction.
	 */
	@JobRoleAccess(featureIds = {15})
	@PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ServiceResponse createTimesheet(@RequestPart("dto") String encryptedDto,
			@RequestPart(value = "doc1", required = false) MultipartFile doc1,
			@RequestPart(value = "doc2", required = false) MultipartFile doc2) throws Exception {
		// Decrypt and parse encrypted DTO using helper service
		TimesheetDTO dto = timesheetEncryptionHelper.decryptAndParseTimesheetDto(encryptedDto);
		ServiceResponse response = timesheetServiceNew.createTimesheet(dto, doc1, doc2);
		return response;
	}
	
	@JobRoleAccess(featureIds = {15, 16, 24})
    @GetMapping("/getByEmployee")
    public ServiceResponse getTimesheetsByEmployee(
            @RequestParam Long empId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return timesheetServiceNew.getTimesheetsByEmployee(empId, startDate, endDate);
    }
    
    /**
     * UPDATE - Update existing timesheet
     * PUT /api/timesheetNew/update/{id}
     */
    @JobRoleAccess(featureIds = {15})
    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ServiceResponse updateTimesheet(
            @RequestParam Long timesheetId,
            @RequestPart("dto") String encryptedDto,
            @RequestPart(value = "doc1", required = false) MultipartFile doc1,
            @RequestPart(value = "doc2", required = false) MultipartFile doc2) throws Exception {
        
        // Decrypt and parse encrypted DTO
    CreateTimesheetRequestDTONew dto = timesheetEncryptionHelper.decryptAndParseTimesheetDtoNew(encryptedDto);
        
        return timesheetServiceNew.updateTimesheet(timesheetId, dto, doc1, doc2);
    }
	
	/**
	 * API 1.3: Get Timesheet by ID
	 * Endpoint: GET /api/v2/timesheet/{timesheetId}
	 */
	@JobRoleAccess(featureIds = {15, 16})
	@GetMapping(value = "/{timesheetId}")
	public ServiceResponse getTimesheetById(@PathVariable Long timesheetId) {
		ServiceResponse response = timesheetServiceNew.getTimesheetById(timesheetId);
		return response;
	}
	
	/**
	 * API 1.4: Get Timesheet by Date
	 * Endpoint: POST /api/v2/timesheet/by-date
	 */
	@JobRoleAccess(featureIds = {15, 16})
	@PostMapping(value = "/by-date")
	public ServiceResponse getTimesheetByDate(@RequestBody TimesheetDTO timesheetDTO) {
		ServiceResponse response = timesheetServiceNew.getTimesheetByDate(timesheetDTO);
		return response;
	}
	
	/**
	 * API 1.5: Get Timesheets by Date Range
	 * Endpoint: POST /api/v2/timesheet/by-date-range
	 */
	@JobRoleAccess(featureIds = {15, 16})
	@PostMapping(value = "/by-date-range")
	public ServiceResponse getTimesheetsByDateRange(@RequestBody TimesheetDTO timesheetDTO) {
		ServiceResponse response = timesheetServiceNew.getTimesheetsByDateRange(timesheetDTO);
		return response;
	}
	
	/**
	 * API 1.7: Update Timesheet Status
	 * Endpoint: POST /api/v2/timesheet/update-status
	 */
	@JobRoleAccess(featureIds = {15, 16, 24})
	@PostMapping(value = "/update-status")
	public ServiceResponse updateTimesheetStatus(@RequestBody TimesheetDTO timesheetDTO) {
		ServiceResponse response = timesheetServiceNew.updateTimesheetStatus(timesheetDTO);
		return response;
	}
	
	/**
	 * API 1.8: Delete Timesheet
	 * Endpoint: DELETE /api/v2/timesheet/{timesheetId}
	 */
	@JobRoleAccess(featureIds = {15, 16})
	@DeleteMapping(value = "/{timesheetId}")
	public ServiceResponse deleteTimesheet(@PathVariable Long timesheetId) {
		ServiceResponse response = timesheetServiceNew.deleteTimesheet(timesheetId);
		return response;
	}
	
	/**
	 * API 1.9: Delete Project from Timesheet
	 * Endpoint: DELETE /api/v2/timesheet/project
	 */
	@JobRoleAccess(featureIds = {15, 16})
	@DeleteMapping(value = "/project")
	public ServiceResponse deleteProjectFromTimesheet(@RequestBody TimesheetDTO timesheetDTO) {
		ServiceResponse response = timesheetServiceNew.deleteProjectFromTimesheet(timesheetDTO);
		return response;
	}
	
	/**
	 * API 1.10: Delete Activity from Timesheet
	 * Endpoint: DELETE /api/v2/timesheet/activity
	 */
	@JobRoleAccess(featureIds = {15, 16})
	@DeleteMapping(value = "/activity")
	public ServiceResponse deleteActivityFromTimesheet(@RequestBody TimesheetDTO timesheetDTO) {
		ServiceResponse response = timesheetServiceNew.deleteActivityFromTimesheet(timesheetDTO);
		return response;
	}
}

