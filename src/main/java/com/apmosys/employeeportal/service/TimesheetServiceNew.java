package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.ActivityRequestDTONew;
import com.apmosys.employeeportal.dto.ActivityTimesheetDTO;
import com.apmosys.employeeportal.dto.CreateTimesheetRequestDTONew;
import com.apmosys.employeeportal.dto.EmployeeTimesheetDTO;
import com.apmosys.employeeportal.dto.ProjectEntryRequestDTONew;
import com.apmosys.employeeportal.dto.ProjectTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.model.EmployeeTimesheetsNew;
import com.apmosys.employeeportal.model.ProjectTimesheetStatusNew;
import com.apmosys.employeeportal.service.helper.TimesheetAggregationHelper;
import com.apmosys.employeeportal.service.validator.TimesheetValidationHelper;
import com.apmosys.employeeportal.utility.ServiceResponse;

/**
 * New Service for Hierarchical Timesheet Operations
 * 
 * This service orchestrates all new hierarchical timesheet operations:
 * - EmployeeTimesheet (one per day per employee)
 * - ProjectTimesheets (multiple per day)
 * - Activities (nested under projects)
 * 
 * @author System
 * @version 2.0
 */
@Service
public class TimesheetServiceNew {

	@Autowired
	private EmployeeTimesheetService employeeTimesheetService;
	
	@Autowired
	private ProjectTimesheetService projectTimesheetService;
	
	@Autowired
	private ActivityTimesheetService activityTimesheetService;
	
	@Autowired
	private TimesheetValidationHelper timesheetValidationHelper;
	
	@Autowired
	private TimesheetAggregationHelper timesheetAggregationHelper;
	
	@Value("${timesheet.lock.days:30}")
	private Integer timesheetLockDays;
	
	/**
	 * API 1.1: Create Timesheet (New Hierarchical Structure)
	 * Creates EmployeeTimesheet, ProjectTimesheets, and Activities in a single transaction.
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse createTimesheet(TimesheetDTO timesheetDTO, MultipartFile doc1, MultipartFile doc2) {
		ServiceResponse response = new ServiceResponse();
		
		try {
			// Validate complete structure
			timesheetValidationHelper.validateTimesheetStructure(timesheetDTO);
			
			EmployeeTimesheetDTO empDTO = timesheetDTO.getEmployeeTimesheet();
			List<ProjectTimesheetDTO> projectDTOs = timesheetDTO.getProjectTimesheets();
			
			// Validate date not locked
			if (timesheetLockDays != null) {
				timesheetValidationHelper.validateDateNotLocked(empDTO.getEmpId(), empDTO.getDate(), timesheetLockDays);
			}
			
			// Check if timesheet already exists
			if (employeeTimesheetService.existsByEmpIdAndDate(empDTO.getEmpId(), empDTO.getDate())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet already exists for this date");
				return response;
			}
			
			// Set audit fields
			empDTO.setCreatedBy(empDTO.getEmpId()); // TODO: Get from security context
			empDTO.setCreatedOn(LocalDateTime.now());
			empDTO.setUpdatedBy(empDTO.getEmpId());
			empDTO.setUpdatedOn(LocalDateTime.now());
			
			// Create EmployeeTimesheet
			EmployeeTimesheetsNew empTS = employeeTimesheetService.create(empDTO);
			Long timesheetId = empTS.getTimesheetId();
			
			// Create ProjectTimesheets and Activities
			if (projectDTOs != null && !projectDTOs.isEmpty()) {
				for (ProjectTimesheetDTO projectDTO : projectDTOs) {
					projectDTO.setTimesheetId(timesheetId);
					
					// Set default status if not provided
					if (projectDTO.getStatus() == null) {
						projectDTO.setStatus(TimesheetAggregationHelper.STATUS_PENDING);
					}
					
					// Create ProjectTimesheet
					ProjectTimesheetStatusNew projectTS = projectTimesheetService.create(timesheetId, projectDTO);
					
					// Create Activities
					if (projectDTO.getActivities() != null && !projectDTO.getActivities().isEmpty()) {
						activityTimesheetService.createAll(timesheetId, projectDTO.getProjectId(), projectDTO.getActivities());
					}
				}
			}
			
			// Calculate and update totals
			employeeTimesheetService.calculateAndUpdateTotals(timesheetId, empDTO, projectDTOs);
			
			// Handle document uploads if provided
			if (doc1 != null || doc2 != null) {
				// TODO: Integrate with TimesheetDocumentService
				// timesheetDocumentService.handleDocumentUpload(timesheetDTO, empTS, doc1, doc2);
			}
			
			// Fetch complete timesheet for response
			TimesheetDTO responseDTO = getTimesheetByIdInternal(timesheetId);
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(responseDTO);
			response.setServiceMessage("Timesheet created successfully");
			
		} catch (IllegalArgumentException e) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Validation failed: " + e.getMessage());
			response.setServiceError(e.getMessage());
		} catch (Exception e) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceError(e.getMessage());
			e.printStackTrace();
		}
		
		return response;
	}
	
	/**
	 * API 1.3: Get Timesheet by ID
	 */
	public ServiceResponse getTimesheetById(Long timesheetId) {
		ServiceResponse response = new ServiceResponse();
		
		try {
			if (timesheetId == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet ID is required");
				return response;
			}
			
			TimesheetDTO timesheetDTO = getTimesheetByIdInternal(timesheetId);
			
			if (timesheetDTO == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet not found");
				return response;
			}
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(timesheetDTO);
			
		} catch (Exception e) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceError(e.getMessage());
			e.printStackTrace();
		}
		
		return response;
	}
	
	/**
	 * API 1.4: Get Timesheet by Date
	 */
	public ServiceResponse getTimesheetByDate(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		try {
			if (timesheetDTO == null || timesheetDTO.getEmpId() == null || timesheetDTO.getDate() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee ID and Date are required");
				return response;
			}
			
			LocalDate date = LocalDate.parse(timesheetDTO.getDate());
			EmployeeTimesheetDTO empDTO = employeeTimesheetService.findByEmpIdAndDate(timesheetDTO.getEmpId(), date);
			
			if (empDTO == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet not found for the given date");
				return response;
			}
			
			// Fetch complete timesheet
			TimesheetDTO completeDTO = getTimesheetByIdInternal(empDTO.getTimesheetId());
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(completeDTO);
			
		} catch (Exception e) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceError(e.getMessage());
			e.printStackTrace();
		}
		
		return response;
	}
	
	/**
	 * API 1.5: Get Timesheets by Date Range
	 */
	public ServiceResponse getTimesheetsByDateRange(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		try {
			if (timesheetDTO == null || timesheetDTO.getEmpId() == null || 
			    timesheetDTO.getStartDate() == null || timesheetDTO.getEndDate() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee ID, Start Date, and End Date are required");
				return response;
			}
			
			LocalDate startDate = LocalDate.parse(timesheetDTO.getStartDate());
			LocalDate endDate = LocalDate.parse(timesheetDTO.getEndDate());
			
			List<EmployeeTimesheetDTO> empDTOs = employeeTimesheetService.findByEmpIdAndDateRange(
					timesheetDTO.getEmpId(), startDate, endDate);
			
			List<TimesheetDTO> timesheetDTOs = new ArrayList<>();
			for (EmployeeTimesheetDTO empDTO : empDTOs) {
				TimesheetDTO completeDTO = getTimesheetByIdInternal(empDTO.getTimesheetId());
				if (completeDTO != null) {
					timesheetDTOs.add(completeDTO);
				}
			}
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(timesheetDTOs);
			
		} catch (Exception e) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceError(e.getMessage());
			e.printStackTrace();
		}
		
		return response;
	}
	
	/**
	 * Get Timesheets by Employee ID and Date Range
	 * Similar to getTimesheetsByDateRange but with direct parameters
	 */
	public ServiceResponse getTimesheetsByEmployee(Long empId, LocalDate startDate, LocalDate endDate) {
		ServiceResponse response = new ServiceResponse();
		
		try {
			if (empId == null || startDate == null || endDate == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee ID, Start Date, and End Date are required");
				return response;
			}
			
			if (startDate.isAfter(endDate)) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Start Date must be before or equal to End Date");
				return response;
			}
			
			List<EmployeeTimesheetDTO> empDTOs = employeeTimesheetService.findByEmpIdAndDateRange(
					empId, startDate, endDate);
			
			List<TimesheetDTO> timesheetDTOs = new ArrayList<>();
			for (EmployeeTimesheetDTO empDTO : empDTOs) {
				TimesheetDTO completeDTO = getTimesheetByIdInternal(empDTO.getTimesheetId());
				if (completeDTO != null) {
					timesheetDTOs.add(completeDTO);
				}
			}
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(timesheetDTOs);
			
		} catch (Exception e) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceError(e.getMessage());
			e.printStackTrace();
		}
		
		return response;
	}
	
	/**
	 * API 1.7: Update Timesheet Status
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateTimesheetStatus(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		try {
			if (timesheetDTO == null || timesheetDTO.getTimesheetId() == null || 
			    timesheetDTO.getProjectId() == null || timesheetDTO.getStatus() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet ID, Project ID, and Status are required");
				return response;
			}
			
			Long timesheetId = timesheetDTO.getTimesheetId();
			Long projectId = timesheetDTO.getProjectId().longValue();
			Integer status = Integer.parseInt(timesheetDTO.getStatus());
			Long updatedBy = timesheetDTO.getUpdatedBy() != null ? timesheetDTO.getUpdatedBy() : timesheetDTO.getEmpId();
			
			// Update project status
			ProjectTimesheetDTO projectDTO = projectTimesheetService.findByTimesheetIdAndProjectId(timesheetId, projectId);
			if (projectDTO == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("ProjectTimesheet not found");
				return response;
			}
			
			projectDTO.setStatus(status);
			projectTimesheetService.update(projectDTO);
			
			// Recalculate employee timesheet status
			List<ProjectTimesheetDTO> allProjects = projectTimesheetService.findByTimesheetId(timesheetId);
			employeeTimesheetService.calculateAndUpdateStatus(timesheetId, allProjects);
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Timesheet status updated successfully");
			
		} catch (Exception e) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceError(e.getMessage());
			e.printStackTrace();
		}
		
		return response;
	}
	
	/**
	 * API 1.8: Delete Timesheet
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse deleteTimesheet(Long timesheetId) {
		ServiceResponse response = new ServiceResponse();
		
		try {
			if (timesheetId == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet ID is required");
				return response;
			}
			
			// Delete activities first
			activityTimesheetService.deleteByTimesheetId(timesheetId);
			
			// Delete projects
			projectTimesheetService.deleteByTimesheetId(timesheetId);
			
			// Delete employee timesheet
			employeeTimesheetService.delete(timesheetId);
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Timesheet deleted successfully");
			
		} catch (Exception e) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceError(e.getMessage());
			e.printStackTrace();
		}
		
		return response;
	}
	
	/**
	 * API 1.9: Delete Project from Timesheet
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse deleteProjectFromTimesheet(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		try {
			if (timesheetDTO == null || timesheetDTO.getTimesheetId() == null || 
			    timesheetDTO.getProjectId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet ID and Project ID are required");
				return response;
			}
			
			Long timesheetId = timesheetDTO.getTimesheetId();
			Long projectId = timesheetDTO.getProjectId().longValue();
			
			// Delete activities for this project
			activityTimesheetService.deleteByTimesheetIdAndProjectId(timesheetId, projectId);
			
			// Delete project
			projectTimesheetService.delete(timesheetId, projectId);
			
			// Recalculate employee timesheet status
			List<ProjectTimesheetDTO> remainingProjects = projectTimesheetService.findByTimesheetId(timesheetId);
			employeeTimesheetService.calculateAndUpdateStatus(timesheetId, remainingProjects);
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Project deleted from timesheet successfully");
			
		} catch (Exception e) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceError(e.getMessage());
			e.printStackTrace();
		}
		
		return response;
	}
	
	/**
	 * API 1.10: Delete Activity from Timesheet
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse deleteActivityFromTimesheet(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		
		try {
			if (timesheetDTO == null || timesheetDTO.getTimesheetId() == null || 
			    timesheetDTO.getActivityId() == null || timesheetDTO.getProjectId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet ID, Activity ID, and Project ID are required");
				return response;
			}
			
			Long timesheetId = timesheetDTO.getTimesheetId();
			Long activityId = timesheetDTO.getActivityId();
			Long projectId = timesheetDTO.getProjectId().longValue();
			
			// Delete activity
			activityTimesheetService.delete(timesheetId, activityId, projectId);
			
			// Recalculate project totals
			ProjectTimesheetDTO projectDTO = projectTimesheetService.findByTimesheetIdAndProjectId(timesheetId, projectId);
			if (projectDTO != null) {
				List<ActivityTimesheetDTO> remainingActivities = activityTimesheetService.findByTimesheetIdAndProjectId(timesheetId, projectId);
				projectDTO.setActivities(remainingActivities);
				projectTimesheetService.calculateProjectTotals(projectDTO);
				projectTimesheetService.update(projectDTO);
			}
			
			// Recalculate employee timesheet totals
			EmployeeTimesheetDTO empDTO = employeeTimesheetService.findById(timesheetId);
			List<ProjectTimesheetDTO> allProjects = projectTimesheetService.findByTimesheetId(timesheetId);
			employeeTimesheetService.calculateAndUpdateTotals(timesheetId, empDTO, allProjects);
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Activity deleted from timesheet successfully");
			
		} catch (Exception e) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceError(e.getMessage());
			e.printStackTrace();
		}
		
		return response;
	}
	
	// ========== INTERNAL HELPER METHODS ==========
	
	/**
	 * Internal method to fetch complete timesheet by ID
	 */
	private TimesheetDTO getTimesheetByIdInternal(Long timesheetId) {
		// Fetch EmployeeTimesheet
		EmployeeTimesheetDTO empDTO = employeeTimesheetService.findById(timesheetId);
		if (empDTO == null) {
			return null;
		}
		
		// Fetch ProjectTimesheets
		List<ProjectTimesheetDTO> projectDTOs = projectTimesheetService.findByTimesheetId(timesheetId);
		
		// Fetch Activities for each project
		for (ProjectTimesheetDTO projectDTO : projectDTOs) {
			List<ActivityTimesheetDTO> activities = activityTimesheetService.findByTimesheetIdAndProjectId(
					timesheetId, projectDTO.getProjectId());
			projectDTO.setActivities(activities);
		}
		
		return new TimesheetDTO(empDTO, projectDTOs);
	}
	
	/**
	 * Update Timesheet (Full Update)
	 * Updates existing timesheet with new data from CreateTimesheetRequestDTONew
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateTimesheet(Long timesheetId, CreateTimesheetRequestDTONew requestDTO, 
			MultipartFile doc1, MultipartFile doc2) {
		ServiceResponse response = new ServiceResponse();
		
		try {
			if (timesheetId == null || requestDTO == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet ID and request data are required");
				return response;
			}
			
			// Find existing employee timesheet
			EmployeeTimesheetDTO existingEmpDTO = employeeTimesheetService.findById(timesheetId);
			if (existingEmpDTO == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet not found");
				return response;
			}
			
			// Validate date not locked
			if (timesheetLockDays != null) {
				timesheetValidationHelper.validateDateNotLocked(
						requestDTO.getEmpId(), requestDTO.getDate(), timesheetLockDays);
			}
			
			// Update employee timesheet fields
			existingEmpDTO.setDate(requestDTO.getDate());
			existingEmpDTO.setDayTypeId(requestDTO.getDayTypeId());
			existingEmpDTO.setOfficeInTime(requestDTO.getOfficeInTime());
			existingEmpDTO.setOfficeOutTime(requestDTO.getOfficeOutTime());
			existingEmpDTO.setTotalWorkingMinutes(requestDTO.getTotalWorkingMinutes());
			existingEmpDTO.setUpdatedBy(requestDTO.getEmpId()); // TODO: Get from security context
			existingEmpDTO.setUpdatedOn(LocalDateTime.now());
			
			// Update employee timesheet
			employeeTimesheetService.update(existingEmpDTO);
			
			// Get existing projects
			List<ProjectTimesheetDTO> existingProjects = projectTimesheetService.findByTimesheetId(timesheetId);
			
			// Collect project IDs from request
			List<Long> requestedProjectIds = new ArrayList<>();
			if (requestDTO.getProjectEntries() != null) {
				for (ProjectEntryRequestDTONew projectEntry : requestDTO.getProjectEntries()) {
					if (projectEntry.getProjectId() != null) {
						requestedProjectIds.add(projectEntry.getProjectId());
					}
				}
			}
			
			// Delete projects that are not in the request
			for (ProjectTimesheetDTO existingProject : existingProjects) {
				if (!requestedProjectIds.contains(existingProject.getProjectId())) {
					// Delete activities first
					activityTimesheetService.deleteByTimesheetIdAndProjectId(
							timesheetId, existingProject.getProjectId());
					// Delete project
					projectTimesheetService.delete(timesheetId, existingProject.getProjectId());
				}
			}
			
			// Update or create projects
			if (requestDTO.getProjectEntries() != null && !requestDTO.getProjectEntries().isEmpty()) {
				for (ProjectEntryRequestDTONew projectEntry : requestDTO.getProjectEntries()) {
					if (projectEntry.getProjectId() == null) {
						continue; // Skip invalid entries
					}
					
					// Find existing project
					ProjectTimesheetDTO existingProject = projectTimesheetService.findByTimesheetIdAndProjectId(
							timesheetId, projectEntry.getProjectId());
					
					if (existingProject != null) {
						// Update existing project
						existingProject.setClientInTime(projectEntry.getClientInTime());
						existingProject.setClientOutTime(projectEntry.getClientOutTime());
						existingProject.setTotalClientWorkingMinutes(projectEntry.getTotalClientWorkingMinutes());
						existingProject.setIsNightShift(requestDTO.getIsNightShift());
						existingProject.setShadowEmpId(projectEntry.getShadowEmpId());
						if (projectEntry.getClientApprovalStatus() != null) {
							// TODO: Map string status to integer status code
							// existingProject.setClientApprovalStatus(...);
						}
						
						projectTimesheetService.update(existingProject);
						
						// Update activities
						updateProjectActivities(timesheetId, projectEntry.getProjectId(), projectEntry.getActivities());
						
					} else {
						// Create new project
						ProjectTimesheetDTO newProjectDTO = new ProjectTimesheetDTO();
						newProjectDTO.setTimesheetId(timesheetId);
						newProjectDTO.setProjectId(projectEntry.getProjectId());
						newProjectDTO.setClientInTime(projectEntry.getClientInTime());
						newProjectDTO.setClientOutTime(projectEntry.getClientOutTime());
						newProjectDTO.setTotalClientWorkingMinutes(projectEntry.getTotalClientWorkingMinutes());
						newProjectDTO.setIsNightShift(requestDTO.getIsNightShift());
						newProjectDTO.setShadowEmpId(projectEntry.getShadowEmpId());
						newProjectDTO.setStatus(TimesheetAggregationHelper.STATUS_PENDING);
						
						projectTimesheetService.create(timesheetId, newProjectDTO);
						
						// Create activities
						if (projectEntry.getActivities() != null && !projectEntry.getActivities().isEmpty()) {
							// Convert ActivityRequestDTONew to ActivityTimesheetDTO
							List<ActivityTimesheetDTO> activities = convertActivities(
									timesheetId, projectEntry.getProjectId(), projectEntry.getActivities());
							activityTimesheetService.createAll(timesheetId, projectEntry.getProjectId(), activities);
						}
					}
				}
			}
			
			// Recalculate totals
			List<ProjectTimesheetDTO> allProjects = projectTimesheetService.findByTimesheetId(timesheetId);
			employeeTimesheetService.calculateAndUpdateTotals(timesheetId, existingEmpDTO, allProjects);
			
			// Handle document uploads if provided
			if (doc1 != null || doc2 != null) {
				// TODO: Integrate with TimesheetDocumentService
				// timesheetDocumentService.handleDocumentUpload(...);
			}
			
			// Fetch complete updated timesheet
			TimesheetDTO responseDTO = getTimesheetByIdInternal(timesheetId);
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(responseDTO);
			response.setServiceMessage("Timesheet updated successfully");
			
		} catch (IllegalArgumentException e) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Validation failed: " + e.getMessage());
			response.setServiceError(e.getMessage());
		} catch (Exception e) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceError(e.getMessage());
			e.printStackTrace();
		}
		
		return response;
	}
	
	/**
	 * Update activities for a project
	 */
	private void updateProjectActivities(Long timesheetId, Long projectId, 
			List<ActivityRequestDTONew> activityRequests) {
		if (activityRequests == null || activityRequests.isEmpty()) {
			// Delete all existing activities
			activityTimesheetService.deleteByTimesheetIdAndProjectId(timesheetId, projectId);
			return;
		}
		
		// For simplicity, delete all and recreate
		// TODO: Implement smarter diff logic (update existing, delete removed, add new)
		activityTimesheetService.deleteByTimesheetIdAndProjectId(timesheetId, projectId);
		
		// Convert and create new activities
		List<ActivityTimesheetDTO> newActivities = convertActivities(timesheetId, projectId, activityRequests);
		if (!newActivities.isEmpty()) {
			activityTimesheetService.createAll(timesheetId, projectId, newActivities);
		}
	}
	
	/**
	 * Convert activity request DTOs to ActivityTimesheetDTO
	 */
	private List<ActivityTimesheetDTO> convertActivities(Long timesheetId, Long projectId, 
			List<ActivityRequestDTONew> activityRequests) {
		List<ActivityTimesheetDTO> activities = new ArrayList<>();
		
		if (activityRequests == null || activityRequests.isEmpty()) {
			return activities;
		}
		
		for (ActivityRequestDTONew activityRequest : activityRequests) {
			ActivityTimesheetDTO activityDTO = new ActivityTimesheetDTO();
			activityDTO.setTimesheetId(timesheetId);
			activityDTO.setProjectId(projectId);
			activityDTO.setActivityId(activityRequest.getActivityId());
			activityDTO.setDescription(activityRequest.getDescription());
			activityDTO.setDurationMinutes(activityRequest.getDurationMinutes());
			activityDTO.setClientLocationId(activityRequest.getClientLocationId());
			
			activities.add(activityDTO);
		}
		
		return activities;
	}
}
