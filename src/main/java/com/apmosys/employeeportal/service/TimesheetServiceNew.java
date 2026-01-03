package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.ActivityTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.EmployeeTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.FinalDocumentDTO_new;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.ProjectTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.TimesheetDocumentDTO_new;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.employeeTimesheetMappingDTO_new;
import com.apmosys.employeeportal.model.EmployeeTimesheetsNew;
import com.apmosys.employeeportal.model.ProjectTimesheetStatusNew;
import com.apmosys.employeeportal.repository.EmployeeTimesheetsNewRepository;
import com.apmosys.employeeportal.service.helper.TimesheetAggregationHelper;
import com.apmosys.employeeportal.service.mapper.TimesheetMapper;
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
	    private EmployeeTimesheetsNewRepository employeeTimesheetsNewRepository;

	    @Autowired
	    private TimesheetMapper timesheetMapper;

    @Autowired
    private TimesheetAggregationHelper aggregationHelper;

    @Autowired
	private ProjectTimesheetService projectTimesheetService;
    
    @Autowired
	private ActivityTimesheetService activityTimesheetService;
    
    @Autowired
	private TimesheetValidationHelper timesheetValidationHelper;
	
	@Value("${timesheet.lock.days:30}")
	private Integer timesheetLockDays;
	
	/**
	 * Check if timesheet exists for employee and date
	 */
	private boolean existsByEmpIdAndDate(Long empId, LocalDate date) {
		return employeeTimesheetsNewRepository.findByEmpIdAndDateNew(empId, date).isPresent();
	}
	
	/**
	 * Get current user ID from security context.
	 * TODO: Implement proper security context retrieval
	 * For now, returns null and caller should use empId as fallback
	 */
	private Long getCurrentUserId() {
		// TODO: Get from SecurityContextHolder or similar
		// return SecurityContextHolder.getContext().getAuthentication().getPrincipal().getUserId();
		return null;
	}
	
	/**
	 * API 1.1: Create Timesheet (New Hierarchical Structure)
	 * Creates EmployeeTimesheet, ProjectTimesheets, and Activities in a single transaction.
	 * Uses new JSON contract: employeeTimesheetMappingDTO_new
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse createTimesheet(employeeTimesheetMappingDTO_new requestDTO, MultipartFile doc1, MultipartFile doc2) {
        ServiceResponse response = new ServiceResponse();
		
		try {
			if (requestDTO == null || requestDTO.getEmployeeTimesheet() == null) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee timesheet data is required");
                return response;
            }

			// Extract employee timesheet and projects from new structure
			EmployeeTimesheetDTO empDTO = requestDTO.getEmployeeTimesheet();
			List<ProjectTimesheetDTO> projectDTOs = empDTO.getProjectTimesheets();
			
			// Validate date not locked
			if (timesheetLockDays != null) {
				timesheetValidationHelper.validateDateNotLocked(empDTO.getEmpId(), empDTO.getDate(), timesheetLockDays);
			}
			
			// Check if timesheet already exists
			if (existsByEmpIdAndDate(empDTO.getEmpId(), empDTO.getDate())) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Timesheet already exists for this date");
                return response;
            }

			// Set audit fields
			Long currentUserId = getCurrentUserId();
			empDTO.setCreatedBy(currentUserId != null ? currentUserId : empDTO.getEmpId());
			empDTO.setCreatedOn(LocalDateTime.now());
			empDTO.setUpdatedBy(currentUserId != null ? currentUserId : empDTO.getEmpId());
			empDTO.setUpdatedOn(LocalDateTime.now());
			
			// Convert new DTO to old DTO for mapper (mapper expects old DTO)
			com.apmosys.employeeportal.dto.EmployeeTimesheetDTO oldEmpDTO = convertToOldEmployeeDTO(empDTO);
			
			// Create EmployeeTimesheet using repository and mapper
			EmployeeTimesheetsNew empTS = timesheetMapper.toEntity(oldEmpDTO);
			empTS = employeeTimesheetsNewRepository.save(empTS);
			Long timesheetId = empTS.getTimesheetId();
			
			// Create ProjectTimesheets and Activities
			if (projectDTOs != null && !projectDTOs.isEmpty()) {
				for (ProjectTimesheetDTO projectDTO : projectDTOs) {
					projectDTO.setTimesheetId(timesheetId);
					
					// Set default status if not provided
					if (projectDTO.getStatus() == null) {
						projectDTO.setStatus(TimesheetAggregationHelper.STATUS_PENDING);
					}
					
					// Convert to old DTO for service layer
					com.apmosys.employeeportal.dto.ProjectTimesheetDTO oldProjectDTO = convertToOldProjectDTO(projectDTO);
					
					// Create ProjectTimesheet
					ProjectTimesheetStatusNew projectTS = projectTimesheetService.create(timesheetId, oldProjectDTO);
					
					// Create Activities
					if (projectDTO.getActivities() != null && !projectDTO.getActivities().isEmpty()) {
						// Convert to old DTOs for service layer
						List<com.apmosys.employeeportal.dto.ActivityTimesheetDTO> oldActivities = convertToOldActivityDTOs(projectDTO.getActivities());
						activityTimesheetService.createAll(timesheetId, projectDTO.getProjectId(), oldActivities);
					}
				}
			}
			
			// Calculate and update totals using aggregation helper (helper expects old DTOs)
			List<com.apmosys.employeeportal.dto.ProjectTimesheetDTO> oldProjectDTOs = convertToOldProjectDTOs(projectDTOs);
			aggregationHelper.calculateAndSetEmployeeTimesheetTotals(oldEmpDTO, oldProjectDTOs);
			
			// Update employee timesheet with calculated totals from old DTO
			empTS.setTotalWorkingMinutes(oldEmpDTO.getTotalWorkingMinutes());
			empTS.setTotalActivitiesMinutes(oldEmpDTO.getTotalActivitiesMinutes());
			empTS.setStatus(oldEmpDTO.getStatus());
			employeeTimesheetsNewRepository.save(empTS);
			
			// Handle document uploads if provided
			if (requestDTO.getFilledDocument() != null || requestDTO.getFinalDocument() != null || doc1 != null || doc2 != null) {
				// TODO: Integrate with TimesheetDocumentService
				// Handle filledDocument and finalDocument from requestDTO
				// timesheetDocumentService.handleDocumentUpload(requestDTO, empTS, doc1, doc2);
			}
			
			// Fetch complete timesheet for response using new structure
			employeeTimesheetMappingDTO_new responseDTO = getTimesheetByIdInternalNew(timesheetId);

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
			
			employeeTimesheetMappingDTO_new timesheetDTO = getTimesheetByIdInternalNew(timesheetId);
			
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
	public ServiceResponse getTimesheetByDate(employeeTimesheetMappingDTO_new requestDTO) {
        ServiceResponse response = new ServiceResponse();
		
		try {
			if (requestDTO == null || requestDTO.getEmployeeTimesheet() == null || 
			    requestDTO.getEmployeeTimesheet().getEmpId() == null || 
			    requestDTO.getEmployeeTimesheet().getDate() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee ID and Date are required");
				return response;
			}
			
			LocalDate date = requestDTO.getEmployeeTimesheet().getDate();
			Long empId = requestDTO.getEmployeeTimesheet().getEmpId();
			
			// Find employee timesheet using repository
			EmployeeTimesheetDTO empDTO = findByEmpIdAndDate(empId, date);
			
			if (empDTO == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet not found for the given date");
				return response;
			}
			
			// Fetch complete timesheet using new structure
			employeeTimesheetMappingDTO_new completeDTO = getTimesheetByIdInternalNew(empDTO.getTimesheetId());
            
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
	 * Note: This method should accept startDate and endDate as separate parameters
	 * For now, keeping the old signature that accepts employeeTimesheetMappingDTO_new for backward compatibility
	 * TODO: Update to use new structure with explicit date parameters
	 */
	public ServiceResponse getTimesheetsByDateRange(employeeTimesheetMappingDTO_new requestDTO) {
        ServiceResponse response = new ServiceResponse();
		
		try {
			// For date range queries, we need empId, startDate, and endDate
			// Since the new structure doesn't have these fields directly, 
			// we'll use getTimesheetsByEmployee which accepts them as parameters
			// This method signature needs to be updated to accept dates explicitly
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Please use getTimesheetsByEmployee endpoint with explicit date parameters");
			return response;
			
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
			
			// Find employee timesheets using repository
			List<EmployeeTimesheetDTO> empDTOs = findByEmpIdAndDateRange(empId, startDate, endDate);
			
			List<employeeTimesheetMappingDTO_new> timesheetDTOs = new ArrayList<>();
			for (EmployeeTimesheetDTO empDTO : empDTOs) {
				employeeTimesheetMappingDTO_new completeDTO = getTimesheetByIdInternalNew(empDTO.getTimesheetId());
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
	public ServiceResponse updateTimesheetStatus(Long timesheetId, Long projectId, Integer status, Long updatedBy) {
		ServiceResponse response = new ServiceResponse();
		
		try {
			if (timesheetId == null || projectId == null || status == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet ID, Project ID, and Status are required");
				return response;
			}
			
			if (updatedBy == null) {
				updatedBy = getCurrentUserId();
			}
			
			// Update project status (using old DTO from service)
			com.apmosys.employeeportal.dto.ProjectTimesheetDTO projectDTO = projectTimesheetService.findByTimesheetIdAndProjectId(timesheetId, projectId);
			if (projectDTO == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("ProjectTimesheet not found");
				return response;
			}
			
			projectDTO.setStatus(status);
			projectTimesheetService.update(projectDTO);
			
			// Recalculate employee timesheet status
			List<com.apmosys.employeeportal.dto.ProjectTimesheetDTO> allProjects = projectTimesheetService.findByTimesheetId(timesheetId);
			
			// Aggregation helper expects old DTOs (which we already have)
			Integer calculatedStatus = aggregationHelper.calculateEmployeeTimesheetStatus(allProjects);
			
			// Update employee timesheet status
			Optional<EmployeeTimesheetsNew> empTSOpt = employeeTimesheetsNewRepository.findById(timesheetId);
			if (empTSOpt.isPresent()) {
				EmployeeTimesheetsNew empTS = empTSOpt.get();
				empTS.setStatus(calculatedStatus);
				employeeTimesheetsNewRepository.save(empTS);
			}
            
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
			
			// Delete employee timesheet using repository
			employeeTimesheetsNewRepository.deleteById(timesheetId);
            
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
	public ServiceResponse deleteProjectFromTimesheet(Long timesheetId, Long projectId) {
		ServiceResponse response = new ServiceResponse();
		
		try {
			if (timesheetId == null || projectId == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet ID and Project ID are required");
				return response;
			}
			
			// Delete activities for this project
			activityTimesheetService.deleteByTimesheetIdAndProjectId(timesheetId, projectId);
			
			// Delete project
			projectTimesheetService.delete(timesheetId, projectId);
			
			// Recalculate employee timesheet status
			List<com.apmosys.employeeportal.dto.ProjectTimesheetDTO> remainingProjects = projectTimesheetService.findByTimesheetId(timesheetId);
			
			// Aggregation helper expects old DTOs (which we already have)
			Integer calculatedStatus = aggregationHelper.calculateEmployeeTimesheetStatus(remainingProjects);
			
			// Update employee timesheet status
			Optional<EmployeeTimesheetsNew> empTSOpt = employeeTimesheetsNewRepository.findById(timesheetId);
			if (empTSOpt.isPresent()) {
				EmployeeTimesheetsNew empTS = empTSOpt.get();
				empTS.setStatus(calculatedStatus);
				employeeTimesheetsNewRepository.save(empTS);
			}
			
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
	public ServiceResponse deleteActivityFromTimesheet(Long timesheetId, Long activityId, Long projectId) {
		ServiceResponse response = new ServiceResponse();
		
		try {
			if (timesheetId == null || activityId == null || projectId == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet ID, Activity ID, and Project ID are required");
				return response;
			}
			
			// Delete activity
			activityTimesheetService.delete(timesheetId, activityId, projectId);
			
			// Recalculate project totals (using old DTOs from service)
			com.apmosys.employeeportal.dto.ProjectTimesheetDTO projectDTO = projectTimesheetService.findByTimesheetIdAndProjectId(timesheetId, projectId);
			if (projectDTO != null) {
				List<com.apmosys.employeeportal.dto.ActivityTimesheetDTO> remainingActivities = activityTimesheetService.findByTimesheetIdAndProjectId(timesheetId, projectId);
				projectDTO.setActivities(remainingActivities);
				projectTimesheetService.calculateProjectTotals(projectDTO);
				projectTimesheetService.update(projectDTO);
			}
			
			// Recalculate employee timesheet totals
			EmployeeTimesheetDTO empDTO = findById(timesheetId);
			List<com.apmosys.employeeportal.dto.ProjectTimesheetDTO> allProjects = projectTimesheetService.findByTimesheetId(timesheetId);
			
			// Convert to old DTOs for aggregation helper (helper expects old DTOs)
			com.apmosys.employeeportal.dto.EmployeeTimesheetDTO oldEmpDTO = convertToOldEmployeeDTO(empDTO);
			aggregationHelper.calculateAndSetEmployeeTimesheetTotals(oldEmpDTO, allProjects);
			
			// Update employee timesheet with calculated totals from old DTO
			Optional<EmployeeTimesheetsNew> empTSOpt = employeeTimesheetsNewRepository.findById(timesheetId);
			if (empTSOpt.isPresent()) {
				EmployeeTimesheetsNew empTS = empTSOpt.get();
				empTS.setTotalWorkingMinutes(oldEmpDTO.getTotalWorkingMinutes());
				empTS.setTotalActivitiesMinutes(oldEmpDTO.getTotalActivitiesMinutes());
				empTS.setStatus(oldEmpDTO.getStatus());
				employeeTimesheetsNewRepository.save(empTS);
			}
			
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
	 * Internal method to fetch complete timesheet by ID (old structure - for backward compatibility)
	 */
	private TimesheetDTO getTimesheetByIdInternal(Long timesheetId) {
		// Fetch EmployeeTimesheet using repository
		EmployeeTimesheetDTO empDTO = findById(timesheetId);
		if (empDTO == null) {
			return null;
		}
		
		// Convert to old DTO for compatibility
		com.apmosys.employeeportal.dto.EmployeeTimesheetDTO oldEmpDTO = convertToOldEmployeeDTO(empDTO);
		
		// Fetch ProjectTimesheets (old DTOs from service)
		List<com.apmosys.employeeportal.dto.ProjectTimesheetDTO> projectDTOs = projectTimesheetService.findByTimesheetId(timesheetId);
		
		// Fetch Activities for each project (old DTOs from service)
		for (com.apmosys.employeeportal.dto.ProjectTimesheetDTO projectDTO : projectDTOs) {
			List<com.apmosys.employeeportal.dto.ActivityTimesheetDTO> activities = activityTimesheetService.findByTimesheetIdAndProjectId(
					timesheetId, projectDTO.getProjectId());
			projectDTO.setActivities(activities);
		}
		
		return new TimesheetDTO(oldEmpDTO, projectDTOs);
	}
	
	/**
	 * Internal method to fetch complete timesheet by ID using new DTO structure
	 */
	private employeeTimesheetMappingDTO_new getTimesheetByIdInternalNew(Long timesheetId) {
		// Fetch using old structure first (service layer still uses old DTOs)
		TimesheetDTO oldDTO = getTimesheetByIdInternal(timesheetId);
		if (oldDTO == null) {
			return null;
		}
		
		// Convert to new structure
		return convertToNewStructure(oldDTO);
	}
	
	/**
	 * Update Timesheet (Full Update)
	 * Updates existing timesheet with new data from employeeTimesheetMappingDTO_new
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateTimesheet(Long timesheetId, employeeTimesheetMappingDTO_new requestDTO, 
			MultipartFile doc1, MultipartFile doc2) {
		ServiceResponse response = new ServiceResponse();
		
		try {
			if (timesheetId == null || requestDTO == null || requestDTO.getEmployeeTimesheet() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet ID and request data are required");
				return response;
			}
			
			// Extract employee timesheet and projects from new structure
			EmployeeTimesheetDTO newEmpDTO = requestDTO.getEmployeeTimesheet();
			List<ProjectTimesheetDTO> newProjectDTOs = newEmpDTO.getProjectTimesheets();
			
			// Validate date not locked
			if (timesheetLockDays != null) {
				timesheetValidationHelper.validateDateNotLocked(
						newEmpDTO.getEmpId(), newEmpDTO.getDate(), timesheetLockDays);
			}
			
			// Fetch employee timesheet entity once (will be updated and saved at the end)
			Optional<EmployeeTimesheetsNew> empTSOpt = employeeTimesheetsNewRepository.findById(timesheetId);
			if (empTSOpt.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet not found");
				return response;
			}
			EmployeeTimesheetsNew empTS = empTSOpt.get();
			
			// Set audit fields
			Long currentUserId = getCurrentUserId();
			newEmpDTO.setUpdatedBy(currentUserId != null ? currentUserId : newEmpDTO.getEmpId());
			newEmpDTO.setUpdatedOn(LocalDateTime.now());
			
			// Update basic fields from DTO
			empTS.setEmpId(newEmpDTO.getEmpId());
			empTS.setDate(newEmpDTO.getDate());
			empTS.setDayTypeId(newEmpDTO.getDayTypeId());
			empTS.setLeaveTypeMasterId(newEmpDTO.getLeaveTypeId());
			empTS.setOfficeInTime(newEmpDTO.getOfficeInTime());
			empTS.setOfficeOutTime(newEmpDTO.getOfficeOutTime());
			empTS.setUpdatedBy(newEmpDTO.getUpdatedBy());
			empTS.setUpdatedOn(newEmpDTO.getUpdatedOn());
			
			// Get existing projects (using old DTOs from service)
			List<com.apmosys.employeeportal.dto.ProjectTimesheetDTO> existingProjects = projectTimesheetService.findByTimesheetId(timesheetId);
			
			// Collect project IDs from request
			List<Long> requestedProjectIds = new ArrayList<>();
			if (newProjectDTOs != null) {
				for (ProjectTimesheetDTO projectDTO : newProjectDTOs) {
					if (projectDTO.getProjectId() != null) {
						requestedProjectIds.add(projectDTO.getProjectId());
					}
				}
			}
			
			// Delete projects that are not in the request
			for (com.apmosys.employeeportal.dto.ProjectTimesheetDTO existingProject : existingProjects) {
				if (!requestedProjectIds.contains(existingProject.getProjectId())) {
					// Delete activities first
					activityTimesheetService.deleteByTimesheetIdAndProjectId(
							timesheetId, existingProject.getProjectId());
					// Delete project
					projectTimesheetService.delete(timesheetId, existingProject.getProjectId());
				}
			}
			
			// Update or create projects
			if (newProjectDTOs != null && !newProjectDTOs.isEmpty()) {
				for (ProjectTimesheetDTO newProjectDTO : newProjectDTOs) {
					if (newProjectDTO.getProjectId() == null) {
						continue; // Skip invalid entries
					}
					
					newProjectDTO.setTimesheetId(timesheetId);
					
					// Find existing project (using old DTO)
					com.apmosys.employeeportal.dto.ProjectTimesheetDTO existingProject = projectTimesheetService.findByTimesheetIdAndProjectId(
							timesheetId, newProjectDTO.getProjectId());
					
					if (existingProject != null) {
						// Update existing project - convert new DTO to old DTO
						com.apmosys.employeeportal.dto.ProjectTimesheetDTO oldProjectDTO = convertToOldProjectDTO(newProjectDTO);
						projectTimesheetService.update(oldProjectDTO);
						
						// Update activities
						if (newProjectDTO.getActivities() != null) {
							// Convert to old DTOs
							List<com.apmosys.employeeportal.dto.ActivityTimesheetDTO> oldActivities = convertToOldActivityDTOs(newProjectDTO.getActivities());
							updateProjectActivitiesOld(timesheetId, newProjectDTO.getProjectId(), oldActivities);
						}
						
					} else {
						// Create new project - convert new DTO to old DTO
						com.apmosys.employeeportal.dto.ProjectTimesheetDTO oldProjectDTO = convertToOldProjectDTO(newProjectDTO);
						if (oldProjectDTO.getStatus() == null) {
							oldProjectDTO.setStatus(TimesheetAggregationHelper.STATUS_PENDING);
						}
						
						projectTimesheetService.create(timesheetId, oldProjectDTO);
						
						// Create activities
						if (newProjectDTO.getActivities() != null && !newProjectDTO.getActivities().isEmpty()) {
							// Convert to old DTOs
							List<com.apmosys.employeeportal.dto.ActivityTimesheetDTO> oldActivities = convertToOldActivityDTOs(newProjectDTO.getActivities());
							activityTimesheetService.createAll(timesheetId, newProjectDTO.getProjectId(), oldActivities);
						}
					}
				}
			}
			
			// Recalculate totals after all projects are updated
			List<com.apmosys.employeeportal.dto.ProjectTimesheetDTO> allProjects = projectTimesheetService.findByTimesheetId(timesheetId);
			
			// Convert to old DTOs for aggregation helper (helper expects old DTOs)
			com.apmosys.employeeportal.dto.EmployeeTimesheetDTO oldEmpDTO = convertToOldEmployeeDTO(newEmpDTO);
			aggregationHelper.calculateAndSetEmployeeTimesheetTotals(oldEmpDTO, allProjects);
			
			// Update employee timesheet with calculated totals (single save at the end)
			empTS.setTotalWorkingMinutes(oldEmpDTO.getTotalWorkingMinutes());
			empTS.setTotalActivitiesMinutes(oldEmpDTO.getTotalActivitiesMinutes());
			empTS.setStatus(oldEmpDTO.getStatus());
			employeeTimesheetsNewRepository.save(empTS);
			
			// Handle document uploads if provided
			if (requestDTO.getFilledDocument() != null || requestDTO.getFinalDocument() != null || doc1 != null || doc2 != null) {
				// TODO: Integrate with TimesheetDocumentService
				// Handle filledDocument and finalDocument from requestDTO
				// timesheetDocumentService.handleDocumentUpload(requestDTO, empTS, doc1, doc2);
			}
			
			// Fetch complete updated timesheet using new structure
			employeeTimesheetMappingDTO_new responseDTO = getTimesheetByIdInternalNew(timesheetId);
			
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
	 * Update activities for a project (using old DTOs)
	 */
	private void updateProjectActivitiesOld(Long timesheetId, Long projectId, 
			List<com.apmosys.employeeportal.dto.ActivityTimesheetDTO> activityDTOs) {
		if (activityDTOs == null || activityDTOs.isEmpty()) {
			// Delete all existing activities
			activityTimesheetService.deleteByTimesheetIdAndProjectId(timesheetId, projectId);
			return;
		}
		
		// For simplicity, delete all and recreate
		// TODO: Implement smarter diff logic (update existing, delete removed, add new)
		activityTimesheetService.deleteByTimesheetIdAndProjectId(timesheetId, projectId);
		
		if (!activityDTOs.isEmpty()) {
			activityTimesheetService.createAll(timesheetId, projectId, activityDTOs);
		}
	}
	
	// ========== CONVERTER METHODS (Old DTO <-> New DTO) ==========
	
	/**
	 * Convert new EmployeeTimesheetDTO to old EmployeeTimesheetDTO
	 */
	private com.apmosys.employeeportal.dto.EmployeeTimesheetDTO convertToOldEmployeeDTO(EmployeeTimesheetDTO newDTO) {
		if (newDTO == null) return null;
		
		com.apmosys.employeeportal.dto.EmployeeTimesheetDTO oldDTO = new com.apmosys.employeeportal.dto.EmployeeTimesheetDTO();
		oldDTO.setTimesheetId(newDTO.getTimesheetId());
		oldDTO.setEmpId(newDTO.getEmpId());
		oldDTO.setDate(newDTO.getDate());
		oldDTO.setDayTypeId(newDTO.getDayTypeId());
		oldDTO.setLeaveTypeId(newDTO.getLeaveTypeId());
		oldDTO.setStatus(newDTO.getStatus());
		oldDTO.setTotalWorkingMinutes(newDTO.getTotalWorkingMinutes());
		oldDTO.setTotalActivitiesMinutes(newDTO.getTotalActivitiesMinutes());
		oldDTO.setOfficeInTime(newDTO.getOfficeInTime());
		oldDTO.setOfficeOutTime(newDTO.getOfficeOutTime());
		oldDTO.setCreatedBy(newDTO.getCreatedBy());
		oldDTO.setCreatedOn(newDTO.getCreatedOn());
		oldDTO.setUpdatedBy(newDTO.getUpdatedBy());
		oldDTO.setUpdatedOn(newDTO.getUpdatedOn());
		
		return oldDTO;
	}
	
	/**
	 * Convert new ProjectTimesheetDTO to old ProjectTimesheetDTO
	 */
	private com.apmosys.employeeportal.dto.ProjectTimesheetDTO convertToOldProjectDTO(ProjectTimesheetDTO newDTO) {
		if (newDTO == null) return null;
		
		com.apmosys.employeeportal.dto.ProjectTimesheetDTO oldDTO = new com.apmosys.employeeportal.dto.ProjectTimesheetDTO();
		oldDTO.setTimesheetId(newDTO.getTimesheetId());
		oldDTO.setProjectId(newDTO.getProjectId());
		oldDTO.setPoNo(newDTO.getPoNo());
		oldDTO.setPoId(newDTO.getPoId());
		oldDTO.setClientInTime(newDTO.getClientInTime());
		oldDTO.setClientOutTime(newDTO.getClientOutTime());
		oldDTO.setIsNightShift(newDTO.getIsNightShift());
		oldDTO.setClientApprovalStatus(newDTO.getClientApprovalStatus());
		oldDTO.setStatus(newDTO.getStatus());
		oldDTO.setShadowEmpId(newDTO.getShadowEmpId());
		oldDTO.setTotalClientWorkingMinutes(newDTO.getTotalClientWorkingMinutes());
		
		return oldDTO;
	}
	
	/**
	 * Convert list of new ProjectTimesheetDTOs to old ProjectTimesheetDTOs
	 */
	private List<com.apmosys.employeeportal.dto.ProjectTimesheetDTO> convertToOldProjectDTOs(List<ProjectTimesheetDTO> newDTOs) {
		if (newDTOs == null) return new ArrayList<>();
		
		List<com.apmosys.employeeportal.dto.ProjectTimesheetDTO> oldDTOs = new ArrayList<>();
		for (ProjectTimesheetDTO newDTO : newDTOs) {
			oldDTOs.add(convertToOldProjectDTO(newDTO));
		}
		return oldDTOs;
	}
	
	/**
	 * Convert new ActivityTimesheetDTO to old ActivityTimesheetDTO
	 */
	private com.apmosys.employeeportal.dto.ActivityTimesheetDTO convertToOldActivityDTO(ActivityTimesheetDTO newDTO) {
		if (newDTO == null) return null;
		
		com.apmosys.employeeportal.dto.ActivityTimesheetDTO oldDTO = new com.apmosys.employeeportal.dto.ActivityTimesheetDTO();
		oldDTO.setTimesheetId(newDTO.getTimesheetId());
		oldDTO.setActivityId(newDTO.getActivityId());
		oldDTO.setProjectId(newDTO.getProjectId());
		oldDTO.setDescription(newDTO.getDescription());
		oldDTO.setDurationMinutes(newDTO.getDurationMinutes());
		oldDTO.setClientLocationId(newDTO.getClientLocationId());
		
		return oldDTO;
	}
	
	/**
	 * Convert list of new ActivityTimesheetDTOs to old ActivityTimesheetDTOs
	 */
	private List<com.apmosys.employeeportal.dto.ActivityTimesheetDTO> convertToOldActivityDTOs(List<ActivityTimesheetDTO> newDTOs) {
		if (newDTOs == null) return new ArrayList<>();
		
		List<com.apmosys.employeeportal.dto.ActivityTimesheetDTO> oldDTOs = new ArrayList<>();
		for (ActivityTimesheetDTO newDTO : newDTOs) {
			oldDTOs.add(convertToOldActivityDTO(newDTO));
		}
		return oldDTOs;
	}
	
	/**
	 * Convert old TimesheetDTO to new employeeTimesheetMappingDTO_new structure
	 */
	private employeeTimesheetMappingDTO_new convertToNewStructure(TimesheetDTO oldDTO) {
		if (oldDTO == null) return null;
		
		employeeTimesheetMappingDTO_new newDTO = new employeeTimesheetMappingDTO_new();
		
		// Convert employee timesheet
		EmployeeTimesheetDTO newEmpDTO = convertToNewEmployeeDTO(oldDTO.getEmployeeTimesheet());
		if (newEmpDTO != null) {
			// Set project timesheets in employee timesheet
			List<ProjectTimesheetDTO> newProjectDTOs = convertToNewProjectDTOs(oldDTO.getProjectTimesheets());
			newEmpDTO.setProjectTimesheets(newProjectDTOs);
		}
		newDTO.setEmployeeTimesheet(newEmpDTO);
		
		// TODO: Fetch and set documents (filledDocument and finalDocument)
		// This will require integration with document service
		// newDTO.setFilledDocument(...);
		// newDTO.setFinalDocument(...);
		
		return newDTO;
	}
	
	/**
	 * Convert old EmployeeTimesheetDTO to new EmployeeTimesheetDTO
	 */
	private EmployeeTimesheetDTO convertToNewEmployeeDTO(com.apmosys.employeeportal.dto.EmployeeTimesheetDTO oldDTO) {
		if (oldDTO == null) return null;
		
		EmployeeTimesheetDTO newDTO = new EmployeeTimesheetDTO();
		newDTO.setTimesheetId(oldDTO.getTimesheetId());
		newDTO.setEmpId(oldDTO.getEmpId());
		newDTO.setDate(oldDTO.getDate());
		newDTO.setDayTypeId(oldDTO.getDayTypeId());
		newDTO.setLeaveTypeId(oldDTO.getLeaveTypeId());
		newDTO.setStatus(oldDTO.getStatus());
		newDTO.setTotalWorkingMinutes(oldDTO.getTotalWorkingMinutes());
		newDTO.setTotalActivitiesMinutes(oldDTO.getTotalActivitiesMinutes());
		newDTO.setOfficeInTime(oldDTO.getOfficeInTime());
		newDTO.setOfficeOutTime(oldDTO.getOfficeOutTime());
		newDTO.setCreatedBy(oldDTO.getCreatedBy());
		newDTO.setCreatedOn(oldDTO.getCreatedOn());
		newDTO.setUpdatedBy(oldDTO.getUpdatedBy());
		newDTO.setUpdatedOn(oldDTO.getUpdatedOn());
		
		return newDTO;
	}
	
	/**
	 * Convert old ProjectTimesheetDTO to new ProjectTimesheetDTO
	 */
	private ProjectTimesheetDTO convertToNewProjectDTO(com.apmosys.employeeportal.dto.ProjectTimesheetDTO oldDTO) {
		if (oldDTO == null) return null;
		
		ProjectTimesheetDTO newDTO = new ProjectTimesheetDTO();
		newDTO.setTimesheetId(oldDTO.getTimesheetId());
		newDTO.setProjectId(oldDTO.getProjectId());
		newDTO.setPoNo(oldDTO.getPoNo());
		newDTO.setPoId(oldDTO.getPoId());
		newDTO.setClientInTime(oldDTO.getClientInTime());
		newDTO.setClientOutTime(oldDTO.getClientOutTime());
		newDTO.setIsNightShift(oldDTO.getIsNightShift());
		newDTO.setClientApprovalStatus(oldDTO.getClientApprovalStatus());
		newDTO.setStatus(oldDTO.getStatus());
		newDTO.setShadowEmpId(oldDTO.getShadowEmpId());
		newDTO.setTotalClientWorkingMinutes(oldDTO.getTotalClientWorkingMinutes());
		
		// Convert activities
		List<ActivityTimesheetDTO> newActivities = convertToNewActivityDTOs(oldDTO.getActivities());
		newDTO.setActivities(newActivities);
		
		return newDTO;
	}
	
	/**
	 * Convert list of old ProjectTimesheetDTOs to new ProjectTimesheetDTOs
	 */
	private List<ProjectTimesheetDTO> convertToNewProjectDTOs(List<com.apmosys.employeeportal.dto.ProjectTimesheetDTO> oldDTOs) {
		if (oldDTOs == null) return new ArrayList<>();
		
		List<ProjectTimesheetDTO> newDTOs = new ArrayList<>();
		for (com.apmosys.employeeportal.dto.ProjectTimesheetDTO oldDTO : oldDTOs) {
			newDTOs.add(convertToNewProjectDTO(oldDTO));
		}
		return newDTOs;
	}
	
	/**
	 * Convert old ActivityTimesheetDTO to new ActivityTimesheetDTO
	 */
	private ActivityTimesheetDTO convertToNewActivityDTO(com.apmosys.employeeportal.dto.ActivityTimesheetDTO oldDTO) {
		if (oldDTO == null) return null;
		
		ActivityTimesheetDTO newDTO = new ActivityTimesheetDTO();
		newDTO.setTimesheetId(oldDTO.getTimesheetId());
		newDTO.setActivityId(oldDTO.getActivityId());
		newDTO.setProjectId(oldDTO.getProjectId());
		newDTO.setDescription(oldDTO.getDescription());
		newDTO.setDurationMinutes(oldDTO.getDurationMinutes());
		newDTO.setClientLocationId(oldDTO.getClientLocationId());
		
		return newDTO;
	}
	
	/**
	 * Convert list of old ActivityTimesheetDTOs to new ActivityTimesheetDTOs
	 */
	private List<ActivityTimesheetDTO> convertToNewActivityDTOs(List<com.apmosys.employeeportal.dto.ActivityTimesheetDTO> oldDTOs) {
		if (oldDTOs == null) return new ArrayList<>();
		
		List<ActivityTimesheetDTO> newDTOs = new ArrayList<>();
		for (com.apmosys.employeeportal.dto.ActivityTimesheetDTO oldDTO : oldDTOs) {
			newDTOs.add(convertToNewActivityDTO(oldDTO));
		}
		return newDTOs;
	}
	
	
    /**
     * Find EmployeeTimesheet by ID.
     * 
     * @param timesheetId Timesheet ID
     * @return EmployeeTimesheetDTO (new) or null if not found
     */
    public EmployeeTimesheetDTO findById(Long timesheetId) {
        if (timesheetId == null) {
            return null;
        }

        Optional<EmployeeTimesheetsNew> entity = employeeTimesheetsNewRepository.findById(timesheetId);
        if (entity.isEmpty()) {
            return null;
        }
        
        // Mapper returns old DTO, convert to new DTO
        com.apmosys.employeeportal.dto.EmployeeTimesheetDTO oldDTO = timesheetMapper.toDTO(entity.get());
        return convertToNewEmployeeDTO(oldDTO);
    }

    /**
     * Find EmployeeTimesheet by employee ID and date.
     * 
     * @param empId Employee ID
     * @param date Date
     * @return EmployeeTimesheetDTO (new) or null if not found
     */
    public EmployeeTimesheetDTO findByEmpIdAndDate(Long empId, LocalDate date) {
        if (empId == null || date == null) {
            return null;
        }

        Optional<EmployeeTimesheetsNew> entity = employeeTimesheetsNewRepository
                .findByEmpIdAndDateNew(empId, date);
        if (entity.isEmpty()) {
            return null;
        }
        
        // Mapper returns old DTO, convert to new DTO
        com.apmosys.employeeportal.dto.EmployeeTimesheetDTO oldDTO = timesheetMapper.toDTO(entity.get());
        return convertToNewEmployeeDTO(oldDTO);
    }

    /**
     * Find EmployeeTimesheets by employee ID and date range.
     * 
     * @param empId Employee ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of EmployeeTimesheetDTO (new)
     */
    public List<EmployeeTimesheetDTO> findByEmpIdAndDateRange(Long empId, LocalDate startDate, LocalDate endDate) {
        if (empId == null || startDate == null || endDate == null) {
            return List.of();
        }

        List<EmployeeTimesheetsNew> entities = employeeTimesheetsNewRepository
                .findAllByEmpIdAndDateBetweenOrderByDateDescNew(empId, startDate, endDate);
        
        // Mapper returns old DTOs, convert to new DTOs
        return entities.stream()
                .map(timesheetMapper::toDTO)
                .map(this::convertToNewEmployeeDTO)
                .collect(Collectors.toList());
    }

}
