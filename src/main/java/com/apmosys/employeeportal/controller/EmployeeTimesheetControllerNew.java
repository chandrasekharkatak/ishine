package com.apmosys.employeeportal.controller;

import java.time.LocalDate;
import java.util.List;

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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.JobRoleAccess;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.EmployeeTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.TimesheetDeleteRequestDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.TimesheetStatusUpdateRequestDTO;
import com.apmosys.employeeportal.service.TimesheetServiceNew;
import com.apmosys.employeeportal.service.helper.TimesheetEncryptionHelper;
import com.apmosys.employeeportal.utility.ServiceResponse;

import lombok.extern.slf4j.Slf4j;

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
@Slf4j
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
	 * 
	 * NEW CONTRACT: Accepts list of multipart files for document uploads
	 * Multiple documents can be uploaded for multiple projects in a single timesheet
	 * 
	 * @param encryptedDto Encrypted timesheet DTO (new contract structure)
	 * @param documents List of multipart files for document uploads (one per project)
	 *                  Documents are linked to projects via documentData in DTO
	 * @return ServiceResponse with created timesheet data
	 */
	@PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ServiceResponse createTimesheet(
	        @RequestPart("dto") String encryptedDto,
	        @RequestPart(value = "documents", required = false) List<MultipartFile> documents) {
	    
	    ServiceResponse response = new ServiceResponse();
	    
	    try {
	        if (encryptedDto == null || encryptedDto.trim().isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Encrypted DTO is required");
	            response.setServiceError("Missing or empty encryptedDto parameter");
	            return response;
	        }
	          
	        EmployeeTimesheetDTO dto;
            try {
                dto = timesheetEncryptionHelper.decryptAndParseTimesheetDtoNewMapping(encryptedDto);
                
            } catch (Exception e) {
                log.error("Decryption/parsing failed - traceId: {}, error: {}", e.getMessage(), e);
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Failed to decrypt or parse request data");
                response.setServiceError("Invalid encrypted data format");
	            return response;
            }
	        response = timesheetServiceNew.createTimesheet(dto, documents);
	        }catch (Exception e) {
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse("Failed to process request data");
	        response.setServiceError("Unexpected error: " + e.getMessage());
	        log.error("Error in createTimesheet - decryption/parsing failed: {}", e.getMessage(), e);
	    }
	    
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
     * PUT /api/v2/timesheet/update
     * 
     * NEW CONTRACT: Accepts list of multipart files for document uploads
     * Multiple documents can be uploaded/updated for multiple projects
     * 
     * UPDATE-SPECIFIC VALIDATIONS:
     * - timesheetId must be provided and valid
     * - Timesheet must exist (validated in service layer)
     * - Date cannot be locked (validated in service layer)
     * - Cannot update timesheets older than lock period
     * 
     * @param timesheetId Timesheet ID to update (required)
     * @param encryptedDto Encrypted timesheet DTO (new contract structure)
     * @param documents List of multipart files for document uploads (one per project)
     *                  Documents are linked to projects via documentData in DTO
     * @return ServiceResponse with updated timesheet data
     */
    @JobRoleAccess(featureIds = {15})
    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ServiceResponse updateTimesheet(
            @RequestParam Long timesheetId,
            @RequestPart("dto") String encryptedDto,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents) {
        
        ServiceResponse response = new ServiceResponse();
        
        try {
            // 1. Validate timesheetId parameter
            if (timesheetId == null) {
                log.warn("Update timesheet request with null timesheetId");
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Timesheet ID is required");
                response.setServiceError("Missing timesheetId parameter");
                return response;
            }
            
            // 2. Validate encryptedDto parameter
            if (encryptedDto == null || encryptedDto.trim().isEmpty()) {
                log.warn("Update timesheet request with empty encryptedDto - timesheetId: {}", timesheetId);
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Encrypted DTO is required");
                response.setServiceError("Missing or empty encryptedDto parameter");
                return response;
            }
            
            // 3. Decrypt and parse encrypted DTO to new structure
            EmployeeTimesheetDTO dto;
            try {
                dto = timesheetEncryptionHelper.decryptAndParseTimesheetDtoNewMapping(encryptedDto);
                log.debug("Decrypted DTO for update - timesheetId: {}, empId: {}, date: {}", 
                    timesheetId, dto.getEmpId(), dto.getDate());
                
            } catch (Exception e) {
                log.error("Decryption/parsing failed for update - timesheetId: {}, error: {}", 
                    timesheetId, e.getMessage(), e);
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Failed to decrypt or parse request data");
                response.setServiceError("Invalid encrypted data format");
                return response;
            }
            
            // 4.
            // Service layer handles:
            // - Timesheet existence validation
            // - Date lock validation (validateDateNotLocked)
            // - Authorization validation
            // - Business rule validations
            response = timesheetServiceNew.updateTimesheet(timesheetId, dto, documents);
             
        } catch (IllegalArgumentException e) {
            // Handle validation errors (e.g., date locked, timesheet not found)
            // Note: timesheetId should be non-null here as we validate it early
            log.error("Validation error in updateTimesheet - timesheetId: {}, error: {}", 
                timesheetId != null ? timesheetId : "null", e.getMessage(), e);
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("Validation failed: " + e.getMessage());
            response.setServiceError(e.getMessage());
            
        } catch (Exception e) {
            // Handle unexpected errors
            // Note: timesheetId should be non-null here as we validate it early
            log.error("Unexpected error in updateTimesheet - timesheetId: {}, error: {}", 
                timesheetId != null ? timesheetId : "null", e.getMessage(), e);
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("Failed to process update request");
            response.setServiceError("Unexpected error: " + e.getMessage());
        }
        
        return response;
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
	public ServiceResponse getTimesheetByDate(@RequestBody EmployeeTimesheetDTO requestDTO) {
		ServiceResponse response = timesheetServiceNew.getTimesheetByDate(requestDTO);
		return response;
	}
	
	/**
	 * API 1.5: Get Timesheets by Date Range
	 * Endpoint: POST /api/v2/timesheet/by-date-range
	 */
	@JobRoleAccess(featureIds = {15, 16})
	@PostMapping(value = "/by-date-range")
	public ServiceResponse getTimesheetsByDateRange(@RequestBody EmployeeTimesheetDTO requestDTO) {
		ServiceResponse response = timesheetServiceNew.getTimesheetsByDateRange(requestDTO);
		return response;
	}
	
	/**
	 * API 1.7: Update Timesheet Status
	 * Endpoint: POST /api/v2/timesheet/update-status
	 */
	@JobRoleAccess(featureIds = {15, 16, 24})
	@PostMapping(value = "/update-status")
	public ServiceResponse updateTimesheetStatus(@RequestBody TimesheetStatusUpdateRequestDTO requestDTO) {
		ServiceResponse response = timesheetServiceNew.updateTimesheetStatus(
				requestDTO.getTimesheetId(), 
				requestDTO.getProjectId(), 
				requestDTO.getStatus(),
				requestDTO.getUpdatedBy());
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
	public ServiceResponse deleteProjectFromTimesheet(@RequestBody TimesheetDeleteRequestDTO requestDTO) {
		ServiceResponse response = timesheetServiceNew.deleteProjectFromTimesheet(
				requestDTO.getTimesheetId(), 
				requestDTO.getProjectId());
		return response;
	}
	
	/**
	 * API 1.10: Delete Activity from Timesheet
	 * Endpoint: DELETE /api/v2/timesheet/activity
	 */
	@JobRoleAccess(featureIds = {15, 16})
	@DeleteMapping(value = "/activity")
	public ServiceResponse deleteActivityFromTimesheet(@RequestBody TimesheetDeleteRequestDTO requestDTO) {
		ServiceResponse response = timesheetServiceNew.deleteActivityFromTimesheet(
				requestDTO.getTimesheetId(), 
				requestDTO.getActivityId(),
				requestDTO.getProjectId());
		return response;
	}
	
	@JobRoleAccess(featureIds = {7,15,16})
	@RequestMapping(value = "/getAllProjectsByEmpId", method = RequestMethod.POST)
	public ServiceResponse getAllProjectsByEmpId(@RequestBody Long empId) {

		ServiceResponse response = timesheetServiceNew.getAllProjectsByEmpId(empId);
		return response;
	}
	
	@JobRoleAccess(featureIds = {15,16})
	@RequestMapping(value = "/getAllMyTimesheetsByEmpId", method = RequestMethod.POST)
	public ServiceResponse getAllMyTimesheetsByEmpId(@RequestBody TimesheetDTO timesheetDTO) {

		ServiceResponse response = timesheetServiceNew.getAllMyTimesheetsByEmpId(timesheetDTO);
		return response;
	}
	
	@JobRoleAccess(featureIds = {15})
	 @PostMapping("/getActiveProjectsAndClientSideIdByEmpId")
	 public ServiceResponse getActiveProjectsAndClientSideIdByEmpId(@RequestBody Long empId) {
	     return timesheetServiceNew.getActiveProjectsAndClientSideIdByEmpId(empId);
	 }
	
//	@JobRoleAccess(featureIds = {15,16})
//	@PostMapping("/getAlreadyFilledTimesheetDatesByEmpId")
//	 public ServiceResponse getAlreadyFilledTimesheetDatesByEmpId(@RequestBody Long empId,@RequestBody String daytype) {
//	     return timesheetServiceNew.getAlreadyFilledTimesheetDatesByEmpId(empId,dayType);
//	 }
	
}

