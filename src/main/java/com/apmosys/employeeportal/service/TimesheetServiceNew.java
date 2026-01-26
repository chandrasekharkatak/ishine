package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.ProjectWithClientAndLocationDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.ActivityTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.EmployeeTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.LocationSessionDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.ProjectTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.TimesheetDocumentDataDTO;
import com.apmosys.employeeportal.enums.DayTypeTransition;
import com.apmosys.employeeportal.model.EmployeeTimesheetLocationMapping;
import com.apmosys.employeeportal.model.EmployeeTimesheetsNew;
import com.apmosys.employeeportal.model.FinalDocumentNew;
import com.apmosys.employeeportal.model.ProjectTimesheetStatusNew;
import com.apmosys.employeeportal.model.TimesheetDocumentDetailsNew;
import com.apmosys.employeeportal.repository.DayTypeMasterNewRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.EmployeeTimesheetLocationMappingRepository;
import com.apmosys.employeeportal.repository.EmployeeTimesheetsNewRepository;
import com.apmosys.employeeportal.repository.FinalDocumentNewRepository;
import com.apmosys.employeeportal.repository.TimesheetDocumentDetailsNewRepository;
import com.apmosys.employeeportal.repository.WorkLocationTypeMasterRepository;
import com.apmosys.employeeportal.service.helper.TimesheetAggregationHelper;
import com.apmosys.employeeportal.service.helper.TimesheetStructureCleanupService;
import com.apmosys.employeeportal.service.mapper.TimesheetMapper;
import com.apmosys.employeeportal.service.validator.EmployeeAssignmentValidationService;
import com.apmosys.employeeportal.service.validator.TimesheetValidationHelper;
import com.apmosys.employeeportal.utility.DateConversionUtil;
import com.apmosys.employeeportal.utility.ServiceResponse;

/**
 * New Service for Hierarchical Timesheet Operations
 * 
 * This service orchestrates all new hierarchical timesheet operations: -
 * EmployeeTimesheet (one per day per employee) - ProjectTimesheets (multiple
 * per day) - Activities (nested under projects)
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

	@Autowired
	private TimesheetDocumentServiceNew timesheetDocumentService;

	@Autowired
	private TimesheetDocumentDetailsNewRepository timesheetDocumentDetailsNewRepository;

	@Autowired
	private FinalDocumentNewRepository finalDocumentNewRepository;

	@Autowired
	TimesheetStructureCleanupService timesheetStructureCleanupService;

	@Value("${timesheet.lock.days:30}")
	private Integer timesheetLockDays;

	@Autowired
	private EmployeeTeamMapRepository employeeTeamMapRepository;

	@Autowired
	private TimesheetQueryService timesheetQueryService;

	@Autowired
	EmployeeAssignmentValidationService employeeAssignmentValidationService;

	// @Autowired
	// private TimesheetDashboardService timesheetDashboardService;

	@Autowired
	private TimesheetDashboardServiceNew timesheetDashboardServiceNew;

	private final String pattern = "yyyy-MM-dd HH:mm:ss";

	/**
	 * Get current user ID from security context. TODO: Implement proper security
	 * context retrieval For now, returns null and caller should use empId as
	 * fallback
	 */
	private Long getCurrentUserId() {
		// TODO: Get from SecurityContextHolder or similar
		// return
		// SecurityContextHolder.getContext().getAuthentication().getPrincipal().getUserId();
		return null;
	}

	// ========== NEW CONTRACT HELPER METHODS ==========

	/**
	 * Convert time string (HH:mm or HH:mm:ss) to LocalDateTime for the given date
	 * NEW CONTRACT: workCheckIn/workCheckOut are strings like "09:00" or "09:00:00"
	 * Maps to officeInTime/officeOutTime (LocalDateTime)
	 * 
	 * @param timeStr Time string in format "HH:mm" or "HH:mm:ss"
	 * @param date    Date to combine with time
	 * @return LocalDateTime or null if timeStr is null/empty
	 */

	private LocalDateTime convertTimeStringToLocalDateTime(String timeStr, LocalDate date) {
		if (timeStr == null || timeStr.trim().isEmpty() || date == null) {
			return null;
		}

		try {

			if (timeStr.contains(" ")) {
				String[] parts = timeStr.split(" ");
				timeStr = parts[parts.length - 1];
			}

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
	 * Normalize EmployeeTimesheetDTO from new contract format NEW CONTRACT: Maps
	 * workCheckIn/workCheckOut to officeInTime/officeOutTime Maps dayType string to
	 * dayTypeId Extracts projects from locationSessions
	 * 
	 * @param empDTO Employee timesheet DTO from new contract
	 */
	private void normalizeEmployeeTimesheetFromNewContract(EmployeeTimesheetDTO empDTO, LocalDate date) {

	}

	/**
	 * API 1.1: Create Timesheet (New Hierarchical Structure) Creates
	 * EmployeeTimesheet, Work Location Mappings, ProjectTimesheets, and Activities
	 * in a single transaction.
	 * 
	 * NEW CONTRACT FLOW: Employee Timesheet (Day Header) ↓ Work Location Mapping
	 * (Where & When) - NEW LEVEL ↓ Project Timesheet (Which Project) ↓ Project
	 * Activities (What Work Done)
	 * 
	 * RELATIONSHIPS: - One Timesheet can have multiple Work Location Mappings - One
	 * Location Mapping can have multiple Projects - One Project can have multiple
	 * Activities
	 * 
	 * NEW CONTRACT SUPPORT: - Employee Timesheet (Day Header) with
	 * workCheckIn/workCheckOut, dayType string - Location Sessions containing
	 * Projects (creates EmployeeTimesheetLocationMapping entries) - Projects
	 * containing Activities - Document Data at employee level
	 * 
	 * CONTRACT MAPPING: - workCheckIn/workCheckOut (String "HH:mm") ->
	 * officeInTime/officeOutTime (LocalDateTime) - dayType (String "Working") ->
	 * dayTypeId (Integer FK) - locationSessions[] ->
	 * EmployeeTimesheetLocationMapping entries - locationSessions[].projects[] ->
	 * projectTimesheets[] - documentData[] -> handled separately
	 * 
	 * VALIDATION CHANGES: - workCheckIn/workCheckOut are required for working days
	 * (NEW) - dayType string must exist in day_type_master_new (NEW) -
	 * locationSessions must have at least one project for working days (NEW) -
	 * Existing validations (date lock, duplicate check) remain unchanged
	 */

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse createTimesheet(EmployeeTimesheetDTO empDTO, List<MultipartFile> documents) {

		ServiceResponse response = new ServiceResponse();
		EmployeeTimesheetsNew newTimesheet = null;

		try {

			// Normalize new contract
			normalizeEmployeeTimesheetFromNewContract(empDTO, empDTO.getDate());

			timesheetValidationHelper.validateEmployeeAuthorization(empDTO);

			timesheetValidationHelper.validateNullAndUnexpectedData(empDTO);

			if (timesheetLockDays != null) {
				timesheetValidationHelper.validateTimesheetLockPeriod(empDTO.getEmpId(), empDTO.getDate(),
						timesheetLockDays);
			}

			employeeAssignmentValidationService.validateEmployeeAssignments(empDTO.getEmpId(), empDTO.getDate(),
					empDTO.getLocationSessions());

			timesheetValidationHelper.validateDayTypeAgainstLeave(empDTO.getEmpId(), empDTO.getDate(),
					empDTO.getDayTypeId());

			EmployeeTimesheetsNew existing = timesheetValidationHelper.validateTimesheetAlreadyExists(empDTO,
					empDTO.getEmpId(), empDTO.getDate());

			if (existing != null) {
				newTimesheet = existing;
				newTimesheet.setUpdatedBy(empDTO.getCreatedBy());
				newTimesheet.setUpdatedOn(LocalDateTime.now());
			} else {
				newTimesheet = new EmployeeTimesheetsNew();
				newTimesheet.setCreatedBy(empDTO.getCreatedBy());
				newTimesheet.setCreatedOn(empDTO.getCreatedOn() != null ? empDTO.getCreatedOn() : LocalDateTime.now());
			}

			if (timesheetValidationHelper.isWorkingDay(empDTO)) {

				timesheetValidationHelper.validateWorkInWorkOutTime(empDTO);

				timesheetValidationHelper.validateLocationTimeOverlap(empDTO.getLocationSessions());

				timesheetValidationHelper.validateLocationWiseProjectAndActivities(empDTO);

				timesheetValidationHelper.validateDocumentsDTO(empDTO);

				timesheetValidationHelper.validateUploadedDocuments(empDTO, documents);

			} else {
				timesheetValidationHelper.validateNonWorkingDayTimesheet(empDTO);
			}

			newTimesheet.setEmpId(empDTO.getEmpId());
			newTimesheet.setDate(empDTO.getDate());
			newTimesheet.setDayTypeId(empDTO.getDayTypeId());
			newTimesheet.setIsNightShift(empDTO.getIsNightShift());
			newTimesheet.setStatus(TimesheetAggregationHelper.STATUS_PENDING);
			newTimesheet.setCurrentManagerId(empDTO.getCurrentManagerId());

			if (!timesheetValidationHelper.isWorkingDay(empDTO)) {
				// newTimesheet.setDescription(empDTO.getDescription());
				newTimesheet.setTotalWorkingMinutes(0);
			} else {
				newTimesheet.setTotalWorkingMinutes(empDTO.getTotalWorkingMinutes());
				newTimesheet.setWorkCheckIn(DateConversionUtil.stringToLocalDateTime(empDTO.getWorkCheckIn(), pattern));
				newTimesheet
						.setWorkCheckOut(DateConversionUtil.stringToLocalDateTime(empDTO.getWorkCheckOut(), pattern));
				newTimesheet.setCreatedBy(empDTO.getCreatedBy());
			}

			EmployeeTimesheetsNew empTS = employeeTimesheetsNewRepository.save(newTimesheet);
			empDTO.setTimesheetId(empTS.getTimesheetId());

			if (timesheetValidationHelper.isWorkingDay(empDTO)) {
				response = createTimesheetForWorkingDays(empTS, empDTO, documents);
			} else {
				// handle non working days.
				response = createTimesheetForNonWorkingDays(empTS, empDTO);
			}
			return response;

		} catch (IllegalArgumentException e) {
			// delete the file uploaded if any
//			if(documents != null && documents.size() > 0) {
//				for(MultipartFile document : documents) {
//					if(document != null) {
//						timesheetDocumentService.deleteFile(document.getOriginalFilename());
//					}
//				}
//			}
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Validation failed: " + e.getMessage());
			response.setServiceError(e.getMessage());

		} catch (Exception e) {

			// delete the file uploaded if any
			if (documents != null && documents.size() > 0) {
				for (MultipartFile document : documents) {
					if (document != null) {
						timesheetDocumentService.deleteFile(document.getOriginalFilename());
					}
				}
			}

			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceError(e.getMessage());
			e.printStackTrace();
		}

		return response;
	}

	public ServiceResponse createTimesheetForWorkingDays(EmployeeTimesheetsNew empTS, EmployeeTimesheetDTO empDTO,
			List<MultipartFile> documents) {

		/*
		 * ====================================================== Timesheet → Location →
		 * Project → Activity ======================================================
		 */
		ServiceResponse response = new ServiceResponse();

		Long timesheetId = empTS.getTimesheetId();
		if (empDTO.getLocationSessions() != null) {

			for (LocationSessionDTO locationSession : empDTO.getLocationSessions()) {

				// 1️ Create Location Mapping

				EmployeeTimesheetLocationMapping locationMapping = EmployeeTimesheetLocationMapping.builder()
						.timesheetId(timesheetId)
						.locationTypeId(locationSession.getWorkLocationTypeId() != null
								? locationSession.getWorkLocationTypeId()
								: null)
						.locationInTime(
								DateConversionUtil.stringToLocalDateTime(locationSession.getLocationInTime(), pattern))
						.locationOutTime(
								DateConversionUtil.stringToLocalDateTime(locationSession.getLocationOutTime(), pattern))
						.build();

				// Persist location mapping
				locationMapping = employeeTimesheetLocationMappingRepository.save(locationMapping);

				// 2️ Projects under this location
				if (locationSession.getProjects() != null) {

					for (ProjectTimesheetDTO projectDTO : locationSession.getProjects()) {

						projectDTO.setTimesheetId(timesheetId);

						if (projectDTO.getStatus() == null) {
							projectDTO.setStatus(TimesheetAggregationHelper.STATUS_PENDING);
						}
						projectDTO.setLocationMappingId(locationMapping.getLocationMappingId());

						// Create Project
						@SuppressWarnings("unused")
						ProjectTimesheetStatusNew projectTS = projectTimesheetService.create(timesheetId, projectDTO,
								empTS.getCreatedBy());

						// 3️ Activities under this project
						if (projectDTO.getActivities() != null && !projectDTO.getActivities().isEmpty()) {

							activityTimesheetService.createAll(timesheetId, projectDTO.getProjectId(),
									projectDTO.getActivities(), locationMapping.getLocationMappingId());
						}
					}
				}
			}
		}

		/*
		 * ====================================================== Aggregation
		 * (UNCHANGED) ======================================================
		 */

		List<ProjectTimesheetDTO> projectDTOs = projectTimesheetService.findByTimesheetId(timesheetId);

		aggregationHelper.calculateAndSetEmployeeTimesheetTotals(empDTO, projectDTOs);

		empTS.setTotalWorkingMinutes(empDTO.getTotalWorkingMinutes());
		empTS.setStatus(empDTO.getStatus());

		employeeTimesheetsNewRepository.save(empTS);

		/*
		 * ====================================================== Document handling
		 * (UNCHANGED) ======================================================
		 */

		if (empDTO.getDocumentData() != null && !empDTO.getDocumentData().isEmpty()) {

			handleDocumentUploadsFromNewContract(empDTO.getDocumentData(), documents, timesheetId, empTS);
		}

		EmployeeTimesheetDTO responseDTO = getTimesheetByIdInternalNew(timesheetId);

		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(responseDTO);
		response.setServiceMessage("Timesheet created successfully");
		return response;

	}

	public ServiceResponse createTimesheetForNonWorkingDays(EmployeeTimesheetsNew empTS, EmployeeTimesheetDTO empDTO) {

		ServiceResponse response = new ServiceResponse();

		Long timesheetId = empTS.getTimesheetId();

		LocationSessionDTO location = empDTO.getLocationSessions().get(0);

		// 1 Create single LOCATION mapping (NA)
		EmployeeTimesheetLocationMapping locationMapping = EmployeeTimesheetLocationMapping.builder()
				.timesheetId(timesheetId).locationTypeId(4).locationInTime(null).locationOutTime(null).build();

		locationMapping = employeeTimesheetLocationMappingRepository.save(locationMapping);

		// 2 Validate project selection
		if (location.getProjects() == null || location.getProjects().isEmpty()) {
			throw new IllegalArgumentException("At least one project must be selected for Non-Working day");
		}

		// 3️ Create project timesheets (NO activities)
		for (ProjectTimesheetDTO projectDTO : location.getProjects()) {

			projectDTO.setTimesheetId(timesheetId);
			projectDTO.setLocationMappingId(locationMapping.getLocationMappingId());

			if (projectDTO.getStatus() == null) {
				projectDTO.setStatus(TimesheetAggregationHelper.STATUS_PENDING);
			}

			// Activities NOT allowed
			projectDTO.setActivities(null);

			projectTimesheetService.create(timesheetId, projectDTO, empTS.getCreatedBy());
		}

		// 4️ Employee Timesheet totals
		empTS.setTotalWorkingMinutes(0);
		empTS.setStatus(TimesheetAggregationHelper.STATUS_PENDING);
		employeeTimesheetsNewRepository.save(empTS);

		// 5️ Build response
		EmployeeTimesheetDTO responseDTO = getTimesheetByIdInternalNew(timesheetId);

		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(responseDTO);
		response.setServiceMessage("Non-working day timesheet created successfully");

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
	 * API 1.5: Get Timesheets by Date Range Note: This method should accept
	 * startDate and endDate as separate parameters For now, keeping the old
	 * signature that accepts employeeTimesheetMappingDTO_new for backward
	 * compatibility TODO: Update to use new structure with explicit date parameters
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
	 * Get Timesheets by Employee ID and Date Range Similar to
	 * getTimesheetsByDateRange but with direct parameters
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
			ProjectTimesheetDTO projectDTO = projectTimesheetService.findByTimesheetIdAndProjectId(timesheetId,
					projectId);
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

			timesheetDocumentService.deleteByTimesheetId(timesheetId);

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

			timesheetDocumentService.deleteByProjectId(projectId.longValue());

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
	public ServiceResponse deleteActivityFromTimesheet(Long id, Long timesheetId, Long activityId, Integer projectId) {
		ServiceResponse response = new ServiceResponse();

		try {
			if (timesheetId == null || activityId == null || projectId == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet ID, Activity ID, and Project ID are required");
				return response;
			}

			// Delete activity
			activityTimesheetService.delete(id, timesheetId, activityId, projectId);

			// Recalculate project totals (using old DTOs from service)
			ProjectTimesheetDTO projectDTO = projectTimesheetService.findByTimesheetIdAndProjectId(timesheetId,
					projectId);
			if (projectDTO != null) {
				List<ActivityTimesheetDTO> remainingActivities = activityTimesheetService
						.findByTimesheetIdAndProjectId(timesheetId, projectId);
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
	 * Update Timesheet (Full Update) Updates existing timesheet with new data from
	 * employeeTimesheetMappingDTO_new
	 */
	/**
	 * API 1.2: Update Timesheet (New Hierarchical Structure) Updates
	 * EmployeeTimesheet, ProjectTimesheets, and Activities in a single transaction.
	 * 
	 * NEW CONTRACT SUPPORT: - Same as createTimesheet - supports new contract
	 * structure - Updates existing timesheet instead of creating new - Handles
	 * deletion of projects/activities not in request
	 * 
	 * CONTRACT MAPPING: - Same as createTimesheet
	 * 
	 * VALIDATION CHANGES: - Same as createTimesheet - Additional: Cannot update
	 * locked timesheets
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateTimesheet(Long timesheetId, EmployeeTimesheetDTO newEmpDTO,
			List<MultipartFile> documents) {
		ServiceResponse response = new ServiceResponse();

		try {
			normalizeEmployeeTimesheetFromNewContract(newEmpDTO, newEmpDTO.getDate());

			timesheetValidationHelper.validateEmployeeAuthorization(newEmpDTO);

			timesheetValidationHelper.validateNullAndUnexpectedData(newEmpDTO);

			EmployeeTimesheetsNew empTS = timesheetValidationHelper.validateTimesheetUpdatable(timesheetId, newEmpDTO);

			timesheetValidationHelper.validateTimesheetDateImmutable(empTS, newEmpDTO);
			timesheetValidationHelper.validateEmployeeImmutableIfProjectApproved(empTS, newEmpDTO);

			/* Day type transition not allowed if any of project is approved */
			timesheetValidationHelper.validateDayTypeTransition(empTS, newEmpDTO);

			employeeAssignmentValidationService.validateEmployeeAssignments(newEmpDTO.getEmpId(), newEmpDTO.getDate(),
					newEmpDTO.getLocationSessions());

			// Based on day type transition we have to take validation action

			DayTypeTransition transition = timesheetValidationHelper.resolveDayTypeTransition(empTS, newEmpDTO);

			if (transition == DayTypeTransition.WORKING_TO_WORKING) {

				timesheetValidationHelper.validateLocationDeletionRules(timesheetId, newEmpDTO.getLocationSessions());
				timesheetValidationHelper.validateApprovedProjectImmutableByLocationMapping(timesheetId,
						newEmpDTO.getLocationSessions());

				timesheetValidationHelper.validateWorkInWorkOutTime(newEmpDTO);
				timesheetValidationHelper.validateLocationTimeOverlap(newEmpDTO.getLocationSessions());
				timesheetValidationHelper.validateLocationWiseProjectAndActivities(newEmpDTO);
				timesheetValidationHelper.validateDocumentsDTO(newEmpDTO);
				timesheetValidationHelper.validateUploadedDocuments(newEmpDTO, documents);
				timesheetValidationHelper.validateActivityDurationWithinLocation(newEmpDTO.getLocationSessions());

				timesheetValidationHelper.validateDocumentsDTO(newEmpDTO);
				timesheetValidationHelper.validateUploadedDocuments(newEmpDTO, documents);

			} else if (transition == DayTypeTransition.NON_WORKING_TO_WORKING) {

				timesheetValidationHelper.validateWorkInWorkOutTime(newEmpDTO);
				timesheetValidationHelper.validateLocationTimeOverlap(newEmpDTO.getLocationSessions());
				timesheetValidationHelper.validateLocationWiseProjectAndActivities(newEmpDTO);
				timesheetValidationHelper.validateDocumentsDTO(newEmpDTO);
				timesheetValidationHelper.validateUploadedDocuments(newEmpDTO, documents);
				timesheetValidationHelper.validateActivityDurationWithinLocation(newEmpDTO.getLocationSessions());

				timesheetValidationHelper.validateDocumentsDTO(newEmpDTO);
				timesheetValidationHelper.validateUploadedDocuments(newEmpDTO, documents);

			} else if (transition == DayTypeTransition.WORKING_TO_NON_WORKING) {
				timesheetValidationHelper.validateNonWorkingDayTimesheet(newEmpDTO);

			} else if (transition == DayTypeTransition.NON_WORKING_TO_NON_WORKING) {

				timesheetValidationHelper.validateLocationDeletionRules(timesheetId, newEmpDTO.getLocationSessions());
				timesheetValidationHelper.validateApprovedProjectImmutableByLocationMapping(timesheetId,
						newEmpDTO.getLocationSessions());

			} else {
				throw new IllegalStateException("Invalid day type transition");
			}

			timesheetStructureCleanupService.cleanTimesheetStructure(timesheetId, newEmpDTO.getLocationSessions());

			handleUpdateTimesheet(timesheetId, newEmpDTO);

			Long currentUserId = getCurrentUserId();
			newEmpDTO.setUpdatedBy(currentUserId != null ? currentUserId : newEmpDTO.getEmpId());
			newEmpDTO.setUpdatedOn(LocalDateTime.now());

			// Update basic fields (Note: empId and date should not change, but keeping for
			// safety)
			empTS.setEmpId(newEmpDTO.getEmpId());
			empTS.setDate(newEmpDTO.getDate());
			empTS.setDayTypeId(newEmpDTO.getDayTypeId());
			empTS.setLeaveTypeMasterId(newEmpDTO.getLeaveTypeId());
			empTS.setWorkCheckIn(DateConversionUtil.stringToLocalDateTime(newEmpDTO.getWorkCheckIn(), pattern));
			empTS.setWorkCheckOut(DateConversionUtil.stringToLocalDateTime(newEmpDTO.getWorkCheckOut(), pattern));
			empTS.setUpdatedBy(newEmpDTO.getUpdatedBy());
			empTS.setUpdatedOn(newEmpDTO.getUpdatedOn());

			List<ProjectTimesheetDTO> allProjects = projectTimesheetService.findByTimesheetId(timesheetId);
			aggregationHelper.calculateAndSetEmployeeTimesheetTotals(newEmpDTO, allProjects);

			// Update employee timesheet with calculated totals
			empTS.setTotalWorkingMinutes(newEmpDTO.getTotalWorkingMinutes());
			empTS.setStatus(newEmpDTO.getStatus());
			employeeTimesheetsNewRepository.save(empTS);
			if (newEmpDTO.getDocumentData() != null && !newEmpDTO.getDocumentData().isEmpty()) {
				// NEW CONTRACT: Handle document uploads/updates for multiple projects

			}

			// Handle document uploads if provided (EXISTING LOGIC - for backward
			// compatibility)
			// Old contract: filledDocument and finalDocument
			if (newEmpDTO.getDocumentData() != null) {

				handleDocumentUploadsFromNewContract(newEmpDTO.getDocumentData(), documents, timesheetId, empTS);
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

	private void handleUpdateTimesheet(Long timesheetId, EmployeeTimesheetDTO newEmpDTO) {
		// Existing locations from DB
		Map<Long, EmployeeTimesheetLocationMapping> existingLocationMap = employeeTimesheetLocationMappingRepository
				.findByTimesheetId(timesheetId).stream()
				.collect(Collectors.toMap(EmployeeTimesheetLocationMapping::getLocationMappingId, l -> l));

		for (LocationSessionDTO locationDTO : newEmpDTO.getLocationSessions()) {

			EmployeeTimesheetLocationMapping locationMapping;

			// CREATE new location
			if (locationDTO.getLocationMappingId() == null) {

				locationMapping = EmployeeTimesheetLocationMapping.builder().timesheetId(timesheetId)
						.locationTypeId(locationDTO.getWorkLocationTypeId())
						.locationInTime(
								convertTimeStringToLocalDateTime(locationDTO.getLocationInTime(), newEmpDTO.getDate()))
						.locationOutTime(
								convertTimeStringToLocalDateTime(locationDTO.getLocationOutTime(), newEmpDTO.getDate()))
						.build();

				locationMapping = employeeTimesheetLocationMappingRepository.save(locationMapping);

			}
			// UPDATE existing location (only times)
			else {

				locationMapping = existingLocationMap.get(locationDTO.getLocationMappingId());

				if (locationMapping == null) {
					throw new IllegalStateException("Invalid locationMappingId");
				}

				locationMapping.setLocationInTime(
						convertTimeStringToLocalDateTime(locationDTO.getLocationInTime(), newEmpDTO.getDate()));

				locationMapping.setLocationOutTime(
						convertTimeStringToLocalDateTime(locationDTO.getLocationOutTime(), newEmpDTO.getDate()));

				employeeTimesheetLocationMappingRepository.save(locationMapping);
			}

			// 3️ HANDLE PROJECTS UNDER LOCATION
			handleProjectsUnderLocationMapping(timesheetId, locationMapping.getLocationMappingId(),
					locationDTO.getProjects(), newEmpDTO.getCreatedBy());
		}
	}

	private void handleProjectsUnderLocationMapping(Long timesheetId, Long locationMappingId,
			List<ProjectTimesheetDTO> incomingProjects, Long createdBy) {

		if (incomingProjects == null) {
			return;
		}

		// Existing projects for this location
		// TO DO :: While cleaning we need to flush so that we don't get it it DB under
		// same transaction
		Map<Integer, ProjectTimesheetDTO> existingProjectMap = projectTimesheetService
				.findByTimesheetIdAndLocationMappingId(timesheetId, locationMappingId).stream()
				.collect(Collectors.toMap(ProjectTimesheetDTO::getProjectId, p -> p));

		for (ProjectTimesheetDTO projectDTO : incomingProjects) {

			projectDTO.setTimesheetId(timesheetId);
			projectDTO.setLocationMappingId(locationMappingId);

			ProjectTimesheetDTO existingProject = existingProjectMap.get(projectDTO.getProjectId());

			// 1️ CREATE project
			if (existingProject == null) {

				if (projectDTO.getStatus() == null) {
					projectDTO.setStatus(TimesheetAggregationHelper.STATUS_PENDING);
				}

				projectTimesheetService.create(timesheetId, projectDTO, createdBy);
			}
			// 2️ UPDATE project (only PENDING ones)
			else {

				if (TimesheetAggregationHelper.STATUS_APPROVED.equals(existingProject.getStatus())) {
					continue; // approved projects already validated as immutable
				}

				projectTimesheetService.update(projectDTO);

				// Replace activities
				activityTimesheetService.deleteByTimesheetIdAndLocationMappingIdAndProjectId(timesheetId,
						locationMappingId, projectDTO.getProjectId());

				if (projectDTO.getActivities() != null) {
					activityTimesheetService.createAll(timesheetId, locationMappingId, projectDTO.getProjectId(),
							projectDTO.getActivities());
				}
			}
		}
	}

	// ========== DOCUMENT HANDLING METHODS ==========

	/**
	 * Handle document uploads from new contract NEW CONTRACT: Multiple documents
	 * can be uploaded for multiple projects Documents are linked to projects via
	 * documentData array
	 * 
	 * @param documentDataList List of document data from new contract (linked to
	 *                         projects)
	 * @param documents        List of multipart files (indexed to match
	 *                         documentData)
	 * @param timesheetId      Timesheet ID to link documents
	 * @param empTS            Employee timesheet entity
	 * 
	 *                         NOTE: This method is ready for implementation Logic
	 *                         will be implemented later as per requirements For
	 *                         now, it validates the structure and prepares for
	 *                         document service integration
	 */
	@Transactional
	private void handleDocumentUploadsFromNewContract(List<TimesheetDocumentDataDTO> documentDataList,
			List<MultipartFile> documents, Long timesheetId, EmployeeTimesheetsNew empTS) {

		if (documentDataList == null || documentDataList.isEmpty()) {
			return; // No documents to handle
		}

		// Validate documents list matches documentData
		if (documents != null && documents.size() == documentDataList.size()) {
			/*
			 * Log warning: document count mismatch For now, proceed with available
			 * documents TODO: Decide on validation strategy - strict match or allow partial
			 */
			timesheetDocumentService.handleDocumentUpload(empTS, timesheetId, documents, documentDataList);
		}

		// Process each document data entry
		// for (int i = 0; i < documentDataList.size(); i++) {
		// TimesheetDocumentDataDTO docData = documentDataList.get(i);

		// // Validate document data
		// if (docData.getProjectId() == null) {
		// // Skip invalid entries - projectId is required
		// continue;
		// }

		// // Get corresponding file (if available)
		// MultipartFile file = null;
		// if (documents != null && i < documents.size()) {
		// file = documents.get(i);
		// }

		// // TODO: Integrate with TimesheetDocumentService
		// // Implementation will:
		// // 1. If docId is null: Create new document
		// // - Save file to storage (S3/local)
		// // - Create TimesheetDocumentDetails record
		// // - Link to timesheetId and projectId
		// // - Set finalFlag, docName, etc.
		// // 2. If docId is not null: Update existing document
		// // - Update file if new file provided
		// // - Update TimesheetDocumentDetails record
		// // - Handle bulkApprovedDocId if applicable
		// // 3. Handle uniqueIdentifier for tracking
		// // 4. Validate file type, size, etc.

		// // Example structure (to be implemented):
		// // if (file != null && !file.isEmpty()) {
		// // TimesheetDocumentDetails doc = new TimesheetDocumentDetails();
		// // doc.setTimesheetId(timesheetId);
		// // doc.setProjectId(docData.getProjectId());
		// // doc.setDocName(docData.getDocName());
		// // doc.setFinalFlag(docData.getFinalFlag());
		// // doc.setBulkApprovedDocId(docData.getBulkApprovedDocId());
		// // doc.setUniqueIdentifier(docData.getUniqueIdentifier());
		// //
		// // if (docData.getDocId() == null) {
		// // // Create new
		// // timesheetDocumentService.createDocument(doc, file);
		// // } else {
		// // // Update existing
		// // doc.setDocId(docData.getDocId());
		// // timesheetDocumentService.updateDocument(doc, file);
		// // }
		// // }
		// }

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
	 * @param date  Date
	 * @return EmployeeTimesheetDTO (new) or null if not found
	 */
	public EmployeeTimesheetDTO findByEmpIdAndDate(Long empId, LocalDate date) {
		if (empId == null || date == null) {
			return null;
		}

		Optional<EmployeeTimesheetsNew> entity = employeeTimesheetsNewRepository.findByEmpIdAndDateNew(empId, date);
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
	 * @param empId     Employee ID
	 * @param startDate Start date
	 * @param endDate   End date
	 * @return List of EmployeeTimesheetDTO (new)
	 */
	public List<EmployeeTimesheetDTO> findByEmpIdAndDateRange(Long empId, LocalDate startDate, LocalDate endDate) {
		if (empId == null || startDate == null || endDate == null) {
			return List.of();
		}

		List<EmployeeTimesheetsNew> entities = employeeTimesheetsNewRepository
				.findAllByEmpIdAndDateBetweenOrderByDateDescNew(empId, startDate, endDate);

		// Mapper returns old DTOs, convert to new DTOs
		return entities.stream().map(timesheetMapper::toDTO).collect(Collectors.toList());
	}

	private EmployeeTimesheetDTO getTimesheetByIdInternalNew(Long timesheetId) {

		// 1️ Fetch timesheet header
		EmployeeTimesheetDTO empDTO = findById(timesheetId);
		if (empDTO == null) {
			return null;
		}

		// 2️ Fetch all location mappings
		List<EmployeeTimesheetLocationMapping> locationMappings = employeeTimesheetLocationMappingRepository
				.findByTimesheetId(timesheetId);

		if (locationMappings == null || locationMappings.isEmpty()) {
			// Backward compatibility: populate old structure
			List<ProjectTimesheetDTO> projects = projectTimesheetService.findByTimesheetId(timesheetId);

			for (ProjectTimesheetDTO project : projects) {
				List<ActivityTimesheetDTO> activities = activityTimesheetService
						.findByTimesheetIdAndProjectId(timesheetId, project.getProjectId());
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
			locationSession.setLocationInTime(locationMapping.getLocationInTime() != null
					? locationMapping.getLocationInTime().toLocalTime().toString()
					: null);

			locationSession.setLocationOutTime(locationMapping.getLocationOutTime() != null
					? locationMapping.getLocationOutTime().toLocalTime().toString()
					: null);

			// 4️⃣ Fetch projects (TEMP: until locationMappingId is added to project table)
			List<ProjectTimesheetDTO> projects = projectTimesheetService.findByTimesheetId(timesheetId);

			List<ProjectTimesheetDTO> mappedProjects = new ArrayList<>();

			for (ProjectTimesheetDTO projectDTO : projects) {

				// 5️⃣ Fetch activities
				List<ActivityTimesheetDTO> activities = activityTimesheetService
						.findByTimesheetIdAndProjectId(timesheetId, projectDTO.getProjectId());

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

	public ServiceResponse getAllProjectsByEmpId(Long empId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("add_timesheet");
		apiLogInfo.setApiUrl("/api/getAllProjectsByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " + empId);

		try {

			List<Object[]> projectList = employeeTeamMapRepository.findProjectsByTeamId(empId);
//	        List<TimesheetDTO> listDto = new ArrayList<TimesheetDTO>();
			List<ProjectWithClientAndLocationDTO> listDto = new ArrayList<>();

			if (!projectList.isEmpty()) {

				for (Object[] object : projectList) {

					String projectType = object[8] != null ? object[8].toString() : "";
					String poEndDateStr = object[9] != null ? object[9].toString() : null;

					if ("TNM".equalsIgnoreCase(projectType) || "Fixed Cost".equalsIgnoreCase(projectType)) {
						if (poEndDateStr != null) {
							String onlyDateStr = poEndDateStr.contains("T") ? poEndDateStr.split("T")[0] : poEndDateStr;
							LocalDate poEndDate = LocalDate.parse(onlyDateStr);
							LocalDate currentDate = LocalDate.now();
//	                        if (poEndDate.isBefore(currentDate)) {
//	                            continue; // skip if poEndDate is in the past
//	                        }
						}
					}

					ProjectWithClientAndLocationDTO projectClientDTO = new ProjectWithClientAndLocationDTO();
					projectClientDTO.setClientId(object[0] != null ? Integer.parseInt(object[0].toString()) : null);
					projectClientDTO.setClientName(object[1] != null ? object[1].toString() : null);
					projectClientDTO
							.setClientLocationId(object[2] != null ? Integer.parseInt(object[2].toString()) : null);
					projectClientDTO.setClientLocation(object[3] != null ? object[3].toString() : null);
					projectClientDTO.setProjectId(object[4] != null ? Integer.parseInt(object[4].toString()) : null);
					projectClientDTO.setProjectName(object[5] != null ? object[5].toString() : null);
					projectClientDTO.setTeamName(object[6] != null ? object[6].toString() : null);
					projectClientDTO.setTeamId(object[7] != null ? Long.parseLong(object[7].toString()) : null);
					projectClientDTO.setHasClientSideId(Boolean.TRUE.equals(object[10]));
					projectClientDTO.setHasClientFlag(Boolean.TRUE.equals(object[11]));
					listDto.add(projectClientDTO);
				}

				if (!listDto.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(listDto);
					apiLogInfo.setApiResponse("listDto : " + listDto);
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					System.out.println("Project List: " + listDto);
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No valid projects found.");
					apiLogInfo.setApiResponse("No valid projects found.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project List is empty !!");
				apiLogInfo.setApiResponse("Project List is empty !!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());

			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}

		apiLogInfo.setApiRequest(logBuilder.toString());
//	    logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse getAllMyTimesheetsByEmpId(TimesheetDTO timesheetDTO) {
		return timesheetQueryService.getAllMyTimesheetsByEmpId(timesheetDTO);

	}

	public ServiceResponse getActiveProjectsAndClientSideIdByEmpId(Long empId) {

		return timesheetQueryService.getActiveProjectsAndClientSideIdByEmpId(empId);

	}

	public ServiceResponse getAlreadyFilledTimesheetDatesByEmpId(Long empId, String dayType) {
		return timesheetQueryService.getAlreadyFilledTimesheetDatesByEmpId(empId, dayType);
	}
	
	
	

	public Resource getDocumentDataByDocId(Long docId, Boolean approvedDocType) {
		try {
			TimesheetDocumentDetailsNew docDetails = new TimesheetDocumentDetailsNew();
			docDetails = timesheetDocumentDetailsNewRepository.findByDocIdAndActive(docId, true);
			if (approvedDocType == null || !approvedDocType) {
				return timesheetDocumentService.viewFile(docDetails.getFileUrl());
			}
			FinalDocumentNew finalDocument = finalDocumentNewRepository.findById(docId)
					.orElseThrow(() -> new RuntimeException("Document not found"));
			return timesheetDocumentService.viewFile(finalDocument.getFileUrl());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// @Transactional(rollbackFor = Exception.class)
	// public ServiceResponse approveOrRejectDocument(Long docId, Long
	// approvedOrRejectedBy, String approvalStatus) {
	// return timesheetDocumentService.approveOrRejectDocument(docId,
	// approvedOrRejectedBy, approvalStatus);
	// }

	public ServiceResponse getTimesheetDashboardCountForEmployee(Integer month, Integer year, Long empId,
			Boolean isClientDashboard, List<String> billableTypes, String employeeActive, String clientSideFilter) {
		return timesheetDashboardServiceNew.getTimesheetDashboardCountForEmployee(month, year, empId, isClientDashboard,
				billableTypes, employeeActive, clientSideFilter);
	}
	

}
