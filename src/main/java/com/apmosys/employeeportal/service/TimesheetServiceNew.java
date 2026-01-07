package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.TimesheetDTO_new.ActivityTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.EmployeeTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.LocationSessionDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.ProjectTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.TimesheetDocumentDataDTO;
import com.apmosys.employeeportal.model.DayTypeMasterNew;
import com.apmosys.employeeportal.model.EmployeeTimesheetLocationMapping;
import com.apmosys.employeeportal.model.EmployeeTimesheetsNew;
import com.apmosys.employeeportal.model.ProjectTimesheetStatusNew;
import com.apmosys.employeeportal.model.WorkLocationTypeMaster;
import com.apmosys.employeeportal.repository.DayTypeMasterNewRepository;
import com.apmosys.employeeportal.repository.EmployeeTimesheetLocationMappingRepository;
import com.apmosys.employeeportal.repository.EmployeeTimesheetsNewRepository;
import com.apmosys.employeeportal.repository.WorkLocationTypeMasterRepository;
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
	
	@Autowired
	private DayTypeMasterNewRepository dayTypeMasterNewRepository;
	
	@Autowired
	private WorkLocationTypeMasterRepository workLocationTypeMasterRepository;
	
	@Autowired
	private EmployeeTimesheetLocationMappingRepository employeeTimesheetLocationMappingRepository;
	
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
	
	// ========== NEW CONTRACT HELPER METHODS ==========
	
	/**
	 * Convert time string (HH:mm or HH:mm:ss) to LocalDateTime for the given date
	 * NEW CONTRACT: workCheckIn/workCheckOut are strings like "09:00" or "09:00:00"
	 * Maps to officeInTime/officeOutTime (LocalDateTime)
	 * 
	 * @param timeStr Time string in format "HH:mm" or "HH:mm:ss"
	 * @param date Date to combine with time
	 * @return LocalDateTime or null if timeStr is null/empty
	 */
	private LocalDateTime convertTimeStringToLocalDateTime(String timeStr, LocalDate date) {
		if (timeStr == null || timeStr.trim().isEmpty() || date == null) {
			return null;
		}
		
		try {
			// Handle both "HH:mm" and "HH:mm:ss" formats
			String[] parts = timeStr.trim().split(":");
			if (parts.length >= 2) {
				int hour = Integer.parseInt(parts[0]);
				int minute = Integer.parseInt(parts[1]);
				int second = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
				return LocalDateTime.of(date, java.time.LocalTime.of(hour, minute, second));
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid time format: " + timeStr + ". Expected HH:mm or HH:mm:ss");
		}
		
		return null;
	}
	
	/**
	 * Convert dayType string to dayTypeId
	 * NEW CONTRACT: dayType is string like "Working", "Week Off", "Leave"
	 * Maps to dayTypeId (Integer FK to day_type_master_new)
	 * 
	 * @param dayType Day type string
	 * @return Day type ID or null if not found
	 */
	private Integer convertDayTypeStringToId(String dayType) {
		if (dayType == null || dayType.trim().isEmpty()) {
			return null;
		}
		
		// Find day type by name
		Optional<DayTypeMasterNew> dayTypeOpt = dayTypeMasterNewRepository.findAll().stream()
				.filter(dt -> dt.getDayType() != null && dt.getDayType().equalsIgnoreCase(dayType.trim()))
				.filter(dt -> dt.getIsActive() != null && dt.getIsActive())
				.findFirst();
		
		if (dayTypeOpt.isPresent()) {
			return dayTypeOpt.get().getDayTypeId();
		}
		
		// If not found, throw exception
		throw new IllegalArgumentException("Day type not found: " + dayType);
	}
	
	/**
	 * Convert workLocationType code to locationTypeId
	 * NEW CONTRACT: workLocationType is code like "APMOSYS_OFFICE", "CLIENT_LOCATION", "WFH"
	 * Maps to locationTypeId (Integer FK to work_location_type_master)
	 * 
	 * @param workLocationTypeCode Work location type code
	 * @return Location type ID or null if not found
	 */
	private Integer convertWorkLocationTypeCodeToId(String workLocationTypeCode) {
		if (workLocationTypeCode == null || workLocationTypeCode.trim().isEmpty()) {
			return null;
		}
		
		// Find work location type by code
		Optional<WorkLocationTypeMaster> locationTypeOpt = workLocationTypeMasterRepository.findAll().stream()
				.filter(lt -> lt.getCode() != null && lt.getCode().equalsIgnoreCase(workLocationTypeCode.trim()))
				.findFirst();
		
		if (locationTypeOpt.isPresent()) {
			return locationTypeOpt.get().getWorkLocationTypeId();
		}
		
		// If not found, throw exception
		throw new IllegalArgumentException("Work location type not found: " + workLocationTypeCode);
	}
	
	/**
	 * Create location mappings from location sessions
	 * NEW CONTRACT: Creates EmployeeTimesheetLocationMapping entries for each location session
	 * 
	 * Flow: Employee Timesheet → Work Location Mapping → Project Timesheet → Activities
	 * 
	 * @param locationSessions List of location sessions from new contract
	 * @param timesheetId Timesheet ID to link location mappings
	 * @param date Date for converting time strings to LocalDateTime
	 * @return Map of location session index to locationMappingId for linking projects
	 */
	private java.util.Map<Integer, Long> createLocationMappings(
			List<LocationSessionDTO> locationSessions, 
			Long timesheetId, 
			LocalDate date) {
		
		java.util.Map<Integer, Long> locationMappingIdMap = new java.util.HashMap<>();
		
		if (locationSessions == null || locationSessions.isEmpty()) {
			return locationMappingIdMap;
		}
		
		for (int i = 0; i < locationSessions.size(); i++) {
			LocationSessionDTO locationSession = locationSessions.get(i);
			
			// Convert workLocationType code to locationTypeId
			Integer locationTypeId = convertWorkLocationTypeCodeToId(locationSession.getWorkLocationType());
			
			// Convert locationInTime/locationOutTime strings to LocalDateTime
			LocalDateTime locationInTime = convertTimeStringToLocalDateTime(
					locationSession.getLocationInTime(), date);
			LocalDateTime locationOutTime = convertTimeStringToLocalDateTime(
					locationSession.getLocationOutTime(), date);
			
			// Create location mapping entity
			EmployeeTimesheetLocationMapping locationMapping = EmployeeTimesheetLocationMapping.builder()
					.timesheetId(timesheetId)
					.locationTypeId(locationSessions.get(i).getWorkLocationTypeId()) // Convert Integer to Long
					.locationInTime(locationInTime)
					.locationOutTime(locationOutTime)
					.build();
			
			// Save location mapping
			EmployeeTimesheetLocationMapping savedLocationMapping = 
					employeeTimesheetLocationMappingRepository.save(locationMapping);
			
			// Store mapping for linking projects
			locationMappingIdMap.put(i, savedLocationMapping.getLocationMappingId());
		}
		
		return locationMappingIdMap;
	}
		
	/**
	 * Extract all projects from location sessions (backward compatibility - without location mapping)
	 * NEW CONTRACT: Projects are nested under locationSessions
	 * This method flattens the structure to get all projects
	 * 
	 * @param locationSessions List of location sessions
	 * @return List of all projects from all location sessions
	 */
	private List<ProjectTimesheetDTO> extractProjectsFromLocationSessions(List<LocationSessionDTO> locationSessions) {
		List<ProjectTimesheetDTO> allProjects = new ArrayList<>();
		
		if (locationSessions != null && !locationSessions.isEmpty()) {
			for (LocationSessionDTO locationSession : locationSessions) {
				if (locationSession.getProjects() != null && !locationSession.getProjects().isEmpty()) {
					allProjects.addAll(locationSession.getProjects());
				}
			}
		}
		
		return allProjects;
	}
	
	/**
	 * Convert client approval status string to Integer ID
	 * NEW CONTRACT: clientApprovalStatus is string like "Pending", "Approved", "Rejected"
	 * Maps to clientApprovalStatus (Integer FK to client_status_master_new)
	 * 
	 * @param statusStr Status string
	 * @return Status ID (1=Pending, 2=Approved, 3=Rejected) or null
	 */
	private Integer convertClientApprovalStatusStringToId(String statusStr) {
		if (statusStr == null || statusStr.trim().isEmpty()) {
			return null;
		}
		
		String status = statusStr.trim();
		if (status.equalsIgnoreCase("Pending")) {
			return 1;
		} else if (status.equalsIgnoreCase("Approved")) {
			return 2;
		} else if (status.equalsIgnoreCase("Rejected")) {
			return 3;
		}
		
		return null;
	}
	
	/**
	 * Normalize EmployeeTimesheetDTO from new contract format
	 * NEW CONTRACT: Maps workCheckIn/workCheckOut to officeInTime/officeOutTime
	 * Maps dayType string to dayTypeId
	 * Extracts projects from locationSessions
	 * 
	 * @param empDTO Employee timesheet DTO from new contract
	 */
	private void normalizeEmployeeTimesheetFromNewContract(EmployeeTimesheetDTO empDTO, LocalDate date) {
		
		
	}
	
 	/**
	 * API 1.1: Create Timesheet (New Hierarchical Structure)
	 * Creates EmployeeTimesheet, Work Location Mappings, ProjectTimesheets, and Activities in a single transaction.
	 * 
	 * NEW CONTRACT FLOW:
	 * Employee Timesheet (Day Header)
	 *   ↓
	 * Work Location Mapping (Where & When) - NEW LEVEL
	 *   ↓
	 * Project Timesheet (Which Project)
	 *   ↓
	 * Project Activities (What Work Done)
	 * 
	 * RELATIONSHIPS:
	 * - One Timesheet can have multiple Work Location Mappings
	 * - One Location Mapping can have multiple Projects
	 * - One Project can have multiple Activities
	 * 
	 * NEW CONTRACT SUPPORT:
	 * - Employee Timesheet (Day Header) with workCheckIn/workCheckOut, dayType string
	 * - Location Sessions containing Projects (creates EmployeeTimesheetLocationMapping entries)
	 * - Projects containing Activities
	 * - Document Data at employee level
	 * 
	 * CONTRACT MAPPING:
	 * - workCheckIn/workCheckOut (String "HH:mm") -> officeInTime/officeOutTime (LocalDateTime)
	 * - dayType (String "Working") -> dayTypeId (Integer FK)
	 * - locationSessions[] -> EmployeeTimesheetLocationMapping entries
	 * - locationSessions[].projects[] -> projectTimesheets[]
	 * - documentData[] -> handled separately
	 * 
	 * VALIDATION CHANGES:
	 * - workCheckIn/workCheckOut are required for working days (NEW)
	 * - dayType string must exist in day_type_master_new (NEW)
	 * - locationSessions must have at least one project for working days (NEW)
	 * - Existing validations (date lock, duplicate check) remain unchanged
	 */
	
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse createTimesheet(EmployeeTimesheetDTO empDTO,
	                                       List<MultipartFile> documents) {

	    ServiceResponse response = new ServiceResponse();

	    try {
	        
	        // Normalize new contract
	        normalizeEmployeeTimesheetFromNewContract(empDTO, empDTO.getDate());
	        
	        timesheetValidationHelper.validateEmployeeAuthorization(empDTO);
	        
	        timesheetValidationHelper.validateNullAndUnexpectedData(empDTO);
	        
	        timesheetValidationHelper.validateTimesheetAlreadyExists(
	                empDTO.getEmpId(), empDTO.getDate());
	        
	        if (timesheetLockDays != null) {
	            timesheetValidationHelper.validateTimesheetLockPeriod(
	                    empDTO.getEmpId(), empDTO.getDate(), timesheetLockDays);
	        }
            timesheetValidationHelper.validateLocationWiseProjectAndActivities(empDTO);
            
            timesheetValidationHelper.validateDocumentsDTO(empDTO);
            
            timesheetValidationHelper.validateUploadedDocuments(empDTO,documents);
	        

	        // Audit fields
	        Long currentUserId = getCurrentUserId();
	        empDTO.setCreatedBy(currentUserId != null ? currentUserId : empDTO.getEmpId());
	        empDTO.setCreatedOn(LocalDateTime.now());
	        empDTO.setUpdatedBy(empDTO.getCreatedBy());
	        empDTO.setUpdatedOn(LocalDateTime.now());


	        EmployeeTimesheetsNew empTS =
	                employeeTimesheetsNewRepository.save(
	                        timesheetMapper.toEntity(empDTO));

	        Long timesheetId = empTS.getTimesheetId();

	        /*======================================================
	           Timesheet → Location → Project → Activity
	           ====================================================== */

	        if (empDTO.getLocationSessions() != null) {

	            for (LocationSessionDTO locationSession : empDTO.getLocationSessions()) {

	                // 1️ Create Location Mapping
	                
	                EmployeeTimesheetLocationMapping locationMapping =
	                EmployeeTimesheetLocationMapping.builder()
	                        .timesheetId(timesheetId)
	                        .locationTypeId(
	                        		locationSession.getWorkLocationTypeId() != null
	                                        ? locationSession.getWorkLocationTypeId()
	                                        : null)
	                        .locationInTime(
	                                convertTimeStringToLocalDateTime(
	                                        locationSession.getLocationInTime(),
	                                        empDTO.getDate()))
	                        .locationOutTime(
	                                convertTimeStringToLocalDateTime(
	                                        locationSession.getLocationOutTime(),
	                                        empDTO.getDate()))
	                        .build();

	             // Persist location mapping
	              locationMapping = employeeTimesheetLocationMappingRepository.save(locationMapping);


	                // 2️ Projects under this location
	                if (locationSession.getProjects() != null) {

	                    for (ProjectTimesheetDTO projectDTO : locationSession.getProjects()) {

	                        projectDTO.setTimesheetId(timesheetId);

	                        if (projectDTO.getStatus() == null) {
	                            projectDTO.setStatus(
	                                    TimesheetAggregationHelper.STATUS_PENDING);
	                        }

	                        // Create Project
							@SuppressWarnings("unused")
							ProjectTimesheetStatusNew projectTS =
	                                projectTimesheetService.create(
	                                        timesheetId,
	                                        projectDTO
	                                );

	                        // 3️ Activities under this project
	                        if (projectDTO.getActivities() != null &&
	                                !projectDTO.getActivities().isEmpty()) {

	                            activityTimesheetService.createAll(
	                                    timesheetId,
	                                    projectDTO.getProjectId(),
	                                    projectDTO.getActivities()
	                            );
	                        }
	                    }
	                }
	            }
	        }

	        /* ======================================================
	           Aggregation (UNCHANGED)
	           ====================================================== */

	        List<ProjectTimesheetDTO> projectDTOs =
	                projectTimesheetService.findByTimesheetId(timesheetId);

	        aggregationHelper.calculateAndSetEmployeeTimesheetTotals(
	                empDTO,
	                projectDTOs);

	        empTS.setTotalWorkingMinutes(empDTO.getTotalWorkingMinutes());
	        empTS.setTotalActivitiesMinutes(empDTO.getTotalActivitiesMinutes());
	        empTS.setStatus(empDTO.getStatus());

	        employeeTimesheetsNewRepository.save(empTS);

	        /* ======================================================
	           Document handling (UNCHANGED)
	           ====================================================== */

	        if (empDTO.getDocumentData() != null &&
	                !empDTO.getDocumentData().isEmpty()) {

	            handleDocumentUploadsFromNewContract(
	                    empDTO.getDocumentData(),
	                    documents,
	                    timesheetId,
	                    empTS);
	        }

	        EmployeeTimesheetDTO responseDTO =
	                getTimesheetByIdInternalNew(timesheetId);

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

	@Transactional(rollbackFor = Exception.class)
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
			
			EmployeeTimesheetDTO timesheetDTO = getTimesheetByIdInternalNew(timesheetId);
			
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
	public ServiceResponse getTimesheetByDate(EmployeeTimesheetDTO requestDTO) {
        ServiceResponse response = new ServiceResponse();
		
		try {
			
			
			LocalDate date = requestDTO.getDate();
			Long empId = requestDTO.getEmpId();
			
			// Find employee timesheet using repository
			EmployeeTimesheetDTO empDTO = findByEmpIdAndDate(empId, date);
			
			if (empDTO == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet not found for the given date");
				return response;
			}
			
			// Fetch complete timesheet using new structure
			EmployeeTimesheetDTO completeDTO = getTimesheetByIdInternalNew(empDTO.getTimesheetId());
            
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
	public ServiceResponse getTimesheetsByDateRange(EmployeeTimesheetDTO requestDTO) {
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
			
			List<EmployeeTimesheetDTO> timesheetDTOs = new ArrayList<>();
			for (EmployeeTimesheetDTO empDTO : empDTOs) {
				EmployeeTimesheetDTO completeDTO = getTimesheetByIdInternalNew(empDTO.getTimesheetId());
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
	public ServiceResponse updateTimesheetStatus(Long timesheetId, Integer projectId, Integer status, Long updatedBy) {
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
	public ServiceResponse deleteProjectFromTimesheet(Long timesheetId, Integer projectId) {
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
			List<ProjectTimesheetDTO> remainingProjects = projectTimesheetService.findByTimesheetId(timesheetId);
			
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
	public ServiceResponse deleteActivityFromTimesheet(Long timesheetId, Long activityId, Integer projectId) {
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
			ProjectTimesheetDTO projectDTO = projectTimesheetService.findByTimesheetIdAndProjectId(timesheetId, projectId);
			if (projectDTO != null) {
				List<ActivityTimesheetDTO> remainingActivities = activityTimesheetService.findByTimesheetIdAndProjectId(timesheetId, projectId);
				projectDTO.setActivities(remainingActivities);
				projectTimesheetService.calculateProjectTotals(projectDTO);
				projectTimesheetService.update(projectDTO);
			}
			
			// Recalculate employee timesheet totals
			EmployeeTimesheetDTO empDTO = findById(timesheetId);
			List<ProjectTimesheetDTO> allProjects = projectTimesheetService.findByTimesheetId(timesheetId);
			
			// Convert to old DTOs for aggregation helper (helper expects old DTOs)
			aggregationHelper.calculateAndSetEmployeeTimesheetTotals(empDTO, allProjects);
			
			// Update employee timesheet with calculated totals from old DTO
			Optional<EmployeeTimesheetsNew> empTSOpt = employeeTimesheetsNewRepository.findById(timesheetId);
			if (empTSOpt.isPresent()) {
				EmployeeTimesheetsNew empTS = empTSOpt.get();
				empTS.setTotalWorkingMinutes(empDTO.getTotalWorkingMinutes());
				empTS.setTotalActivitiesMinutes(empDTO.getTotalActivitiesMinutes());
				empTS.setStatus(empDTO.getStatus());
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
		
	/**
	 * Update Timesheet (Full Update)
	 * Updates existing timesheet with new data from employeeTimesheetMappingDTO_new
	 */
	@Transactional(rollbackFor = Exception.class)
	/**
	 * API 1.2: Update Timesheet (New Hierarchical Structure)
	 * Updates EmployeeTimesheet, ProjectTimesheets, and Activities in a single transaction.
	 * 
	 * NEW CONTRACT SUPPORT:
	 * - Same as createTimesheet - supports new contract structure
	 * - Updates existing timesheet instead of creating new
	 * - Handles deletion of projects/activities not in request
	 * 
	 * CONTRACT MAPPING:
	 * - Same as createTimesheet
	 * 
	 * VALIDATION CHANGES:
	 * - Same as createTimesheet
	 * - Additional: Cannot update locked timesheets
	 */
	public ServiceResponse updateTimesheet(Long timesheetId, EmployeeTimesheetDTO newEmpDTO, 
			List<MultipartFile> documents) {
		ServiceResponse response = new ServiceResponse();
		
		try {
			// NEW CONTRACT: Normalize DTO from new contract format
			// This maps workCheckIn/workCheckOut to officeInTime/officeOutTime
			// Maps dayType string to dayTypeId
			// Extracts projects from locationSessions
			normalizeEmployeeTimesheetFromNewContract(newEmpDTO, newEmpDTO.getDate());
		
			// VALIDATION: Validate new contract structure
			// This includes: workCheckIn/workCheckOut for working days, projects required, etc.
			//timesheetValidationHelper.validateForCreate(newEmpDTO);
			
			// VALIDATION: Validate date not locked
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
			
			// NEW CONTRACT: Update Work Location Mappings
			// Flow: Employee Timesheet → Work Location Mapping → Project Timesheet → Activities
			// Delete existing location mappings and create new ones
			// This ensures location mappings match the new contract structure
			if (newEmpDTO.getLocationSessions() != null && !newEmpDTO.getLocationSessions().isEmpty()) {
				// Delete existing location mappings for this timesheet
				List<EmployeeTimesheetLocationMapping> existingLocationMappings = 
						employeeTimesheetLocationMappingRepository.findByTimesheetId(timesheetId);
				if (existingLocationMappings != null && !existingLocationMappings.isEmpty()) {
					employeeTimesheetLocationMappingRepository.deleteAll(existingLocationMappings);
				}
				
				// Create new location mappings
				@SuppressWarnings("unused")
				Map<Integer, Long> locationMappingIdMap = createLocationMappings(
						newEmpDTO.getLocationSessions(), 
						timesheetId, 
						newEmpDTO.getDate());
		     	}
			
			// Set audit fields (EXISTING LOGIC - unchanged)
			Long currentUserId = getCurrentUserId();
			newEmpDTO.setUpdatedBy(currentUserId != null ? currentUserId : newEmpDTO.getEmpId());
			newEmpDTO.setUpdatedOn(LocalDateTime.now());
			
			// Update basic fields from DTO
			// NEW CONTRACT: officeInTime/officeOutTime are set from workCheckIn/workCheckOut via normalizeEmployeeTimesheetFromNewContract
			empTS.setEmpId(newEmpDTO.getEmpId());
			empTS.setDate(newEmpDTO.getDate());
			empTS.setDayTypeId(newEmpDTO.getDayTypeId()); // NEW CONTRACT: Set from dayType string
			empTS.setLeaveTypeMasterId(newEmpDTO.getLeaveTypeId());
			empTS.setWorkCheckIn(newEmpDTO.getWorkCheckIn()); // NEW CONTRACT: Set from workCheckIn
			empTS.setWorkCheckOut(newEmpDTO.getWorkCheckOut()); // NEW CONTRACT: Set from workCheckOut
			empTS.setUpdatedBy(newEmpDTO.getUpdatedBy());
			empTS.setUpdatedOn(newEmpDTO.getUpdatedOn());
			
			// NOT IN NEW CONTRACT but kept for business logic:
			// This ensures location mappings match the new contract structure
			if (newEmpDTO.getLocationSessions() != null && !newEmpDTO.getLocationSessions().isEmpty()) {
				// Delete existing location mappings for this timesheet
				List<EmployeeTimesheetLocationMapping> existingLocationMappings = 
						employeeTimesheetLocationMappingRepository.findByTimesheetId(timesheetId);
				if (existingLocationMappings != null && !existingLocationMappings.isEmpty()) {
					employeeTimesheetLocationMappingRepository.deleteAll(existingLocationMappings);
				}
				
				// Create new location mappings
				@SuppressWarnings("unused")
				Map<Integer, Long> locationMappingIdMap = createLocationMappings(
						newEmpDTO.getLocationSessions(), 
						timesheetId, 
						newEmpDTO.getDate());
			}
			
			// Get existing projects (using old DTOs from service)
			// EXISTING LOGIC - unchanged
			List<ProjectTimesheetDTO> existingProjects = projectTimesheetService.findByTimesheetId(timesheetId);
			
			// Collect project IDs from request
			// NEW CONTRACT: Projects come from locationSessions (already flattened in normalizeEmployeeTimesheetFromNewContract)
			List<Long> requestedProjectIds = new ArrayList<>();
//			if (newProjectDTOs != null) {
//				for (ProjectTimesheetDTO projectDTO : newProjectDTOs) {
//					if (projectDTO.getProjectId() != null) {
//						requestedProjectIds.add(projectDTO.getProjectId());
//					}
//				}
//			}
			
			// Delete projects that are not in the request
			// EXISTING LOGIC - unchanged (handles project deletion)
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
			// NEW CONTRACT: Projects are normalized from locationSessions
			// NEW CONTRACT: clientApprovalStatus, projectHoursMinutes, teamId, isShadow are handled
		
			
			// Recalculate totals after all projects are updated
			List<ProjectTimesheetDTO> allProjects = projectTimesheetService.findByTimesheetId(timesheetId);
			
			aggregationHelper.calculateAndSetEmployeeTimesheetTotals(newEmpDTO, allProjects);
			
			// Update employee timesheet with calculated totals (single save at the end)
			// EXISTING LOGIC - unchanged
			empTS.setTotalWorkingMinutes(newEmpDTO.getTotalWorkingMinutes());
			empTS.setTotalActivitiesMinutes(newEmpDTO.getTotalActivitiesMinutes());
			empTS.setStatus(newEmpDTO.getStatus());
			employeeTimesheetsNewRepository.save(empTS);
			
			// NEW CONTRACT: Handle documentData with list of multipart files
			// Document data is at employee level, linked to projects
			// Each document has projectId, docName, finalFlag, etc.
			// Documents list contains files corresponding to documentData entries
			if (newEmpDTO.getDocumentData() != null && !newEmpDTO.getDocumentData().isEmpty()) {
				// NEW CONTRACT: Handle document uploads/updates for multiple projects
				// Documents list is indexed to match documentData array
				// Each document in documentData should have corresponding file in documents list
				handleDocumentUploadsFromNewContract(newEmpDTO.getDocumentData(), documents, timesheetId, empTS);
			}
			
			// Handle document uploads if provided (EXISTING LOGIC - for backward compatibility)
			// Old contract: filledDocument and finalDocument
			if (newEmpDTO.getDocumentData()!=null) {
				// TODO: Integrate with TimesheetDocumentService for old contract
				// Handle filledDocument and finalDocument from requestDTO (old contract)
				// For backward compatibility - old contract may still send these
				// timesheetDocumentService.handleDocumentUpload(requestDTO, empTS, null, null);
			}
			
			// Fetch complete updated timesheet using new structure
			EmployeeTimesheetDTO responseDTO = getTimesheetByIdInternalNew(timesheetId);
			
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
	private void updateProjectActivitiesOld(Long timesheetId, Integer projectId, 
			List<ActivityTimesheetDTO> activityDTOs) {
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
	
	// ========== DOCUMENT HANDLING METHODS ==========
	
	/**
	 * Handle document uploads from new contract
	 * NEW CONTRACT: Multiple documents can be uploaded for multiple projects
	 * Documents are linked to projects via documentData array
	 * 
	 * @param documentDataList List of document data from new contract (linked to projects)
	 * @param documents List of multipart files (indexed to match documentData)
	 * @param timesheetId Timesheet ID to link documents
	 * @param empTS Employee timesheet entity
	 * 
	 * NOTE: This method is ready for implementation
	 * Logic will be implemented later as per requirements
	 * For now, it validates the structure and prepares for document service integration
	 */
	private void handleDocumentUploadsFromNewContract(
			List<TimesheetDocumentDataDTO> documentDataList,
			List<MultipartFile> documents,
			Long timesheetId,
			EmployeeTimesheetsNew empTS) {
		
		if (documentDataList == null || documentDataList.isEmpty()) {
			return; // No documents to handle
		}
		
		// Validate documents list matches documentData
		if (documents != null && documents.size() != documentDataList.size()) {
			// Log warning: document count mismatch
			// For now, proceed with available documents
			// TODO: Decide on validation strategy - strict match or allow partial
		}
		
		// Process each document data entry
		for (int i = 0; i < documentDataList.size(); i++) {
			TimesheetDocumentDataDTO docData = documentDataList.get(i);
			
			// Validate document data
			if (docData.getProjectId() == null) {
				// Skip invalid entries - projectId is required
				continue;
			}
			
			// Get corresponding file (if available)
			MultipartFile file = null;
			if (documents != null && i < documents.size()) {
				file = documents.get(i);
			}
			
			// TODO: Integrate with TimesheetDocumentService
			// Implementation will:
			// 1. If docId is null: Create new document
			//    - Save file to storage (S3/local)
			//    - Create TimesheetDocumentDetails record
			//    - Link to timesheetId and projectId
			//    - Set finalFlag, docName, etc.
			// 2. If docId is not null: Update existing document
			//    - Update file if new file provided
			//    - Update TimesheetDocumentDetails record
			//    - Handle bulkApprovedDocId if applicable
			// 3. Handle uniqueIdentifier for tracking
			// 4. Validate file type, size, etc.
			
			// Example structure (to be implemented):
			// if (file != null && !file.isEmpty()) {
			//     TimesheetDocumentDetails doc = new TimesheetDocumentDetails();
			//     doc.setTimesheetId(timesheetId);
			//     doc.setProjectId(docData.getProjectId());
			//     doc.setDocName(docData.getDocName());
			//     doc.setFinalFlag(docData.getFinalFlag());
			//     doc.setBulkApprovedDocId(docData.getBulkApprovedDocId());
			//     doc.setUniqueIdentifier(docData.getUniqueIdentifier());
			//     
			//     if (docData.getDocId() == null) {
			//         // Create new
			//         timesheetDocumentService.createDocument(doc, file);
			//     } else {
			//         // Update existing
			//         doc.setDocId(docData.getDocId());
			//         timesheetDocumentService.updateDocument(doc, file);
			//     }
			// }
		}
	}
	
	// ========== CONVERTER METHODS (Old DTO <-> New DTO) ==========	
	/**
	 * Convert new ActivityTimesheetDTO to old ActivityTimesheetDTO
	 */
	private ActivityTimesheetDTO convertToOldActivityDTO(ActivityTimesheetDTO newDTO) {
		if (newDTO == null) return null;
		
		ActivityTimesheetDTO oldDTO = new ActivityTimesheetDTO();
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
	private List<ActivityTimesheetDTO> convertToOldActivityDTOs(List<ActivityTimesheetDTO> newDTOs) {
		if (newDTOs == null) return new ArrayList<>();
		
		List<ActivityTimesheetDTO> oldDTOs = new ArrayList<>();
		for (ActivityTimesheetDTO newDTO : newDTOs) {
			oldDTOs.add(convertToOldActivityDTO(newDTO));
		}
		return oldDTOs;
	}
		
	/**
	 * Convert old ActivityTimesheetDTO to new ActivityTimesheetDTO
	 */
	private ActivityTimesheetDTO convertToNewActivityDTO(ActivityTimesheetDTO oldDTO) {
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
	private List<ActivityTimesheetDTO> convertToNewActivityDTOs(List<ActivityTimesheetDTO> oldDTOs) {
		if (oldDTOs == null) return new ArrayList<>();
		
		List<ActivityTimesheetDTO> newDTOs = new ArrayList<>();
		for (ActivityTimesheetDTO oldDTO : oldDTOs) {
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
         EmployeeTimesheetDTO dTO = timesheetMapper.toDTO(entity.get());
        return dTO;
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
        EmployeeTimesheetDTO dTO = timesheetMapper.toDTO(entity.get());
        return dTO;
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
                .collect(Collectors.toList());
    }
    
 

    private EmployeeTimesheetDTO getTimesheetByIdInternalNew(Long timesheetId) {

        // 1️ Fetch timesheet header
        EmployeeTimesheetDTO empDTO = findById(timesheetId);
        if (empDTO == null) {
            return null;
        }

        // 2️ Fetch all location mappings
        List<EmployeeTimesheetLocationMapping> locationMappings =
                employeeTimesheetLocationMappingRepository.findByTimesheetId(timesheetId);

        if (locationMappings == null || locationMappings.isEmpty()) {
            // Backward compatibility: populate old structure
            List<ProjectTimesheetDTO> projects =
                    projectTimesheetService.findByTimesheetId(timesheetId);

            for (ProjectTimesheetDTO project : projects) {
                List<ActivityTimesheetDTO> activities =
                        activityTimesheetService.findByTimesheetIdAndProjectId(
                                timesheetId, project.getProjectId());
                project.setActivities(activities);
            }

//            empDTO.setProjectTimesheets(projects);
            return empDTO;
        }

        // 3️⃣ Build location sessions (NEW CONTRACT)
        List<LocationSessionDTO> locationSessions = new ArrayList<>();

        for (EmployeeTimesheetLocationMapping locationMapping : locationMappings) {

            LocationSessionDTO locationSession = new LocationSessionDTO();

            // Work location type (code)
            locationSession.setWorkLocationTypeId(locationMapping.getLocationTypeId());

            // Location in/out time (string format as per contract)
            locationSession.setLocationInTime(
                    locationMapping.getLocationInTime() != null
                            ? locationMapping.getLocationInTime()
                                  .toLocalTime()
                                  .toString()
                            : null
            );

            locationSession.setLocationOutTime(
                    locationMapping.getLocationOutTime() != null
                            ? locationMapping.getLocationOutTime()
                                  .toLocalTime()
                                  .toString()
                            : null
            );

            // 4️⃣ Fetch projects (TEMP: until locationMappingId is added to project table)
            List<ProjectTimesheetDTO> projects =
                    projectTimesheetService.findByTimesheetId(timesheetId);

            List<ProjectTimesheetDTO> mappedProjects = new ArrayList<>();

            for (ProjectTimesheetDTO projectDTO : projects) {

                // 5️⃣ Fetch activities
                List<ActivityTimesheetDTO> activities =
                        activityTimesheetService.findByTimesheetIdAndProjectId(
                                timesheetId,
                                projectDTO.getProjectId()
                        );

                projectDTO.setActivities(activities);
                mappedProjects.add(projectDTO);
            }

            locationSession.setProjects(mappedProjects);
            locationSessions.add(locationSession);
        }

        // 6️ Set NEW CONTRACT fields
        empDTO.setLocationSessions(locationSessions);

        // 7️ (Optional) keep backward compatibility
//        empDTO.setProjectTimesheets(
//                projectTimesheetService.findByTimesheetId(timesheetId)
//        );

        return empDTO;
    }


}
