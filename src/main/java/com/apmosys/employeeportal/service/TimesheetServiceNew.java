package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.EmployeeInfoDTO;
import com.apmosys.employeeportal.dto.GetEmployeeSummaryOnExportDTO;
import com.apmosys.employeeportal.dto.FinalBulkUploadDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.ProjectTimesheetInfoDTO;
import com.apmosys.employeeportal.dto.ProjectWithClientAndLocationDTO;
import com.apmosys.employeeportal.dto.TimesheetApprovalNewDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetIdAndEmpIdDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.ActivityTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.EmployeeTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.LocationSessionDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.ProjectTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.TimesheetDocumentDataDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.TimesheetStatusCountDTO;
import com.apmosys.employeeportal.enums.DayTypeTransition;
import com.apmosys.employeeportal.enums.DayTypeCode;
import com.apmosys.employeeportal.exception.UnauthorizedAccessException;
import com.apmosys.employeeportal.model.DocMimeTypeMasterNew;
import com.apmosys.employeeportal.model.EmployeeTimesheetLocationMapping;
import com.apmosys.employeeportal.model.EmployeeTimesheetsNew;
import com.apmosys.employeeportal.model.FinalDocument;
import com.apmosys.employeeportal.model.FinalDocumentNew;
import com.apmosys.employeeportal.model.ProjectTimesheetStatusNew;
import com.apmosys.employeeportal.model.TimesheetDataDTO;
import com.apmosys.employeeportal.model.Timesheet;
import com.apmosys.employeeportal.model.TimesheetDocumentDetails;
import com.apmosys.employeeportal.model.TimesheetDocumentDetailsNew;
import com.apmosys.employeeportal.repository.DayTypeMasterNewRepository;
import com.apmosys.employeeportal.repository.DocMimeTypeMasterNewRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.EmployeeTimesheetLocationMappingRepository;
import com.apmosys.employeeportal.repository.EmployeeTimesheetsNewRepository;
import com.apmosys.employeeportal.repository.FinalDocumentNewRepository;
import com.apmosys.employeeportal.repository.ProjectTimesheetStatusNewRepository;
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
	TimesheetApprovalServiceNew timesheetApprovalServiceNew;

	@Autowired
	private TimesheetMapper timesheetMapper;

	@Autowired
	private TimesheetAggregationHelper aggregationHelper;

	@Autowired
	private ProjectTimesheetService projectTimesheetService;

	@Autowired
	private ActivityTimesheetService activityTimesheetService;


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

	@Autowired
	private ProjectTimesheetStatusNewRepository projectTimesheetStatusNewRepository;

	@Autowired
	private DocMimeTypeMasterNewRepository docMimeTypeMasterNewRepository;

	 @Autowired
	 private TimesheetDashboardService timesheetDashboardService;

		@Autowired
	private TimesheetDashboardServiceNew timesheetDashboardServiceNew;

		@Autowired
	 TimesheetValidationHelper timesheetValidationHelper;
	 
	@Value("${timesheet.minus.days.for.bulk.upload}")
	private Integer minusDays;

	@Value("${check.minus.days.for.bulk.upload}")
	private Boolean checkMinusDaysForBulkUpload;

	@Value("${timesheet.max.file.size}")
	private DataSize maxFileSize;

	private final String pattern = "yyyy-MM-dd HH:mm:ss";

	

	@Autowired
	private LogService logService;
	
	@Autowired
	private HttpServletRequest httpRequest;
	
	
	private Map<String, Integer> mimeTypeMap = new HashMap<>();

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

			Integer effectiveLockDays = (timesheetLockDays != null) ? timesheetLockDays : 30;
			timesheetValidationHelper.validateTimesheetLockPeriod(empDTO.getEmpId(), empDTO.getDate(),
					effectiveLockDays);

			employeeAssignmentValidationService.validateEmployeeAssignments(empDTO.getEmpId(), empDTO.getDate(),
					empDTO.getLocationSessions());

			if (timesheetValidationHelper.isWorkingDay(empDTO)) {
				timesheetValidationHelper.validateDayTypeAgainstLeave(empDTO.getEmpId(), empDTO.getDate(),
						empDTO.getDayTypeId());
				// Prevent Working / Half-day Working on dates configured as Holiday / Week Off
				timesheetValidationHelper.validateDayTypeAgainstHoliday(empDTO.getDate(), empDTO.getDayTypeId());
			}

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
				// Ensure same project has consistent client approval status across locations
				timesheetValidationHelper.validateClientApprovalStatusConsistency(empDTO);

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

		} catch (UnauthorizedAccessException e) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Unauthorized");
			response.setServiceError(e.getMessage());

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Validation failed: " + e.getMessage());
			response.setServiceError(e.getMessage());

		} catch (IllegalStateException e) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Validation failed: " + e.getMessage());
			response.setServiceError(e.getMessage());

		} catch (Exception e) {
			// Clean up any partially uploaded documents on failure
			cleanupDocumentsOnFailure(documents);
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceError(e.getMessage());
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

		if (empDTO.getLocationSessions() == null || empDTO.getLocationSessions().isEmpty()) {
			throw new IllegalArgumentException("Location session is required for Non-Working day");
		}
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

			if (timesheetValidationHelper.isWorkingDay(newEmpDTO)) {
				timesheetValidationHelper.validateDayTypeAgainstLeave(newEmpDTO.getEmpId(), newEmpDTO.getDate(),
						newEmpDTO.getDayTypeId());
				// Prevent Working / Half-day Working on dates configured as Holiday / Week Off
				timesheetValidationHelper.validateDayTypeAgainstHoliday(newEmpDTO.getDate(), newEmpDTO.getDayTypeId());
			}

			// Based on day type transition we have to take validation action

			DayTypeTransition transition = timesheetValidationHelper.resolveDayTypeTransition(empTS, newEmpDTO);

			if (transition == DayTypeTransition.WORKING_TO_WORKING) {

				timesheetValidationHelper.validateLocationDeletionRules(timesheetId, newEmpDTO.getLocationSessions());
				timesheetValidationHelper.validateApprovedProjectImmutableByLocationMapping(timesheetId,
						newEmpDTO.getLocationSessions());

				timesheetValidationHelper.validateWorkInWorkOutTime(newEmpDTO);
				timesheetValidationHelper.validateLocationTimeOverlap(newEmpDTO.getLocationSessions());
				timesheetValidationHelper.validateLocationWiseProjectAndActivities(newEmpDTO);
				// Ensure same project has consistent client approval status across locations
				timesheetValidationHelper.validateClientApprovalStatusConsistency(newEmpDTO);
				timesheetValidationHelper.validateDocumentsDTO(newEmpDTO);
				timesheetValidationHelper.validateUploadedDocuments(newEmpDTO, documents, timesheetId);
				timesheetValidationHelper.validateActivityDurationWithinLocation(newEmpDTO.getLocationSessions());

			} else if (transition == DayTypeTransition.NON_WORKING_TO_WORKING) {

				timesheetValidationHelper.validateWorkInWorkOutTime(newEmpDTO);
				timesheetValidationHelper.validateLocationTimeOverlap(newEmpDTO.getLocationSessions());
				timesheetValidationHelper.validateLocationWiseProjectAndActivities(newEmpDTO);
				// Ensure same project has consistent client approval status across locations
				timesheetValidationHelper.validateClientApprovalStatusConsistency(newEmpDTO);
				timesheetValidationHelper.validateDocumentsDTO(newEmpDTO);
				timesheetValidationHelper.validateUploadedDocuments(newEmpDTO, documents, timesheetId);
				timesheetValidationHelper.validateActivityDurationWithinLocation(newEmpDTO.getLocationSessions());

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
			Long updatedBy = currentUserId != null ? currentUserId
					: (newEmpDTO.getUpdatedBy() != null ? newEmpDTO.getUpdatedBy() : newEmpDTO.getEmpId());
			newEmpDTO.setUpdatedBy(updatedBy);
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
			empTS = employeeTimesheetsNewRepository.save(empTS);
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

		} catch (UnauthorizedAccessException e) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Unauthorized");
			response.setServiceError(e.getMessage());

		} catch (IllegalArgumentException e) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Validation failed: " + e.getMessage());
			response.setServiceError(e.getMessage());

		} catch (IllegalStateException e) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Validation failed: " + e.getMessage());
			response.setServiceError(e.getMessage());

		} catch (Exception e) {
			cleanupDocumentsOnFailure(documents);
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceError(e.getMessage());
		}

		return response;
	}

	/**
	 * Clean up any partially uploaded documents on failure.
	 */
	private void cleanupDocumentsOnFailure(List<MultipartFile> documents) {
		if (documents != null && !documents.isEmpty()) {
			for (MultipartFile document : documents) {
				try {
					if (document != null && document.getOriginalFilename() != null) {
						timesheetDocumentService.deleteFile(document.getOriginalFilename());
					}
				} catch (Exception ex) {
					// Log but don't propagate - cleanup failure should not mask original error
				}
			}
		}
	}

	private void handleUpdateTimesheet(Long timesheetId, EmployeeTimesheetDTO newEmpDTO) {
		if (newEmpDTO.getLocationSessions() == null || newEmpDTO.getLocationSessions().isEmpty()) {
			throw new IllegalArgumentException("At least one location session is required");
		}
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
					throw new IllegalStateException("Invalid location. Please refresh the page and try again.");
				}

				locationMapping.setLocationInTime(
						convertTimeStringToLocalDateTime(locationDTO.getLocationInTime(), newEmpDTO.getDate()));

				locationMapping.setLocationOutTime(
						convertTimeStringToLocalDateTime(locationDTO.getLocationOutTime(), newEmpDTO.getDate()));

				employeeTimesheetLocationMappingRepository.save(locationMapping);
			}

			// 3️ HANDLE PROJECTS UNDER LOCATION
			Long createdBy = newEmpDTO.getCreatedBy() != null ? newEmpDTO.getCreatedBy()
					: (newEmpDTO.getUpdatedBy() != null ? newEmpDTO.getUpdatedBy() : newEmpDTO.getEmpId());
			handleProjectsUnderLocationMapping(timesheetId, locationMapping.getLocationMappingId(),
					locationDTO.getProjects(), createdBy);
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

				// Create activities for the new project
				if (projectDTO.getActivities() != null && !projectDTO.getActivities().isEmpty()) {
					activityTimesheetService.createAll(timesheetId, locationMappingId, projectDTO.getProjectId(),
							projectDTO.getActivities());
				}
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

		boolean isUpdate = (timesheetId != null);

		// Update: existing docs don't need files - only process entries that have new uploads
		if (isUpdate && (documents == null || documents.isEmpty())) {
			return; // No new files to upload; existing docs already in DB
		}

		if (isUpdate) {
			// Filter to only documentData entries that have a matching new file
			Set<String> fileNames = documents.stream()
					.map(MultipartFile::getOriginalFilename)
					.filter(Objects::nonNull)
					.collect(Collectors.toSet());

			List<TimesheetDocumentDataDTO> toUpload = new ArrayList<>();
			List<MultipartFile> filesToUpload = new ArrayList<>();
			for (TimesheetDocumentDataDTO docData : documentDataList) {
				String id = docData.getUniqueIdentifier();
				if (id != null && fileNames.contains(id)) {
					toUpload.add(docData);
					for (MultipartFile f : documents) {
						if (id.equals(f.getOriginalFilename())) {
							filesToUpload.add(f);
							break;
						}
					}
				}
			}

			if (toUpload.isEmpty()) {
				return; // No new files to upload
			}
			timesheetDocumentService.handleDocumentUpload(empTS, timesheetId, filesToUpload, toUpload);
		} else {
			// Create: strict match required - every documentData must have a file
			if (documents == null || documents.size() != documentDataList.size()) {
				throw new IllegalArgumentException(
						"Please ensure all required documents are attached.");
			}
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
			if (projects == null) {
				return empDTO;
			}
			for (ProjectTimesheetDTO project : projects) {
				List<ActivityTimesheetDTO> activities = activityTimesheetService
						.findByTimesheetIdAndProjectId(timesheetId, project.getProjectId());
				project.setActivities(activities);
			}

//            empDTO.setProjectTimesheets(projects);
			// Populate documentData for update form (backward compat path)
			List<TimesheetDocumentDataDTO> docsCompat = getDocumentDataForTimesheet(timesheetId);
			empDTO.setDocumentData(docsCompat);
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

		// 7️ Populate documentData for update form
		List<TimesheetDocumentDataDTO> docs = getDocumentDataForTimesheet(timesheetId);
		empDTO.setDocumentData(docs);

		return empDTO;
	}

	/**
	 * Find last working-day timesheet for an employee (before a target date) and
	 * return it as an autofill template.
	 *
	 * Rules:
	 * - Only considers days where day type is "working" (via DayTypeCode).
	 * - Only looks at dates STRICTLY before the targetDate.
	 * - Each project must be active for the target date (same logic as getProjectListForDateAndEmpId:
	 *   mapping startDate/endDate valid for targetDate).
	 * - Each project must pass validateProjectAssignment (employee assigned to project).
	 * - If any project fails either check, no autofill data is returned.
	 * - Documents are NOT included in the template (documentData is set to null).
	 *
	 * @param empId       Employee ID
	 * @param targetDate  Date user wants to fill (autofill looks before this date)
	 * @return ServiceResponse with EmployeeTimesheetDTO or null (no template)
	 */
	public ServiceResponse getAutofillTimesheetTemplate(Long empId, LocalDate targetDate) {
		ServiceResponse response = new ServiceResponse();

		if (empId == null || targetDate == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Employee and date are required for autofill.");
			response.setServiceError("Missing empId or targetDate in autofill request");
			return response;
		}

		// We look for any previous working-day timesheet in a reasonable window
		LocalDate endDate = targetDate.minusDays(1);
		if (!endDate.isBefore(targetDate)) {
			// If targetDate is the earliest possible, nothing to search
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(null);
			response.setServiceMessage("No previous working timesheet found for autofill.");
			return response;
		}

		// Fetch ONLY the latest working-day timesheet BEFORE targetDate within a 3‑month window.
		// Uses repository-level paging so we don't pull the entire history.
		LocalDate startDate = endDate.minusMonths(3);
		List<EmployeeTimesheetsNew> history = employeeTimesheetsNewRepository
				.findLatestWorkingTimesheetBeforeDate(empId, startDate, endDate, PageRequest.of(0, 1));

		if (history == null || history.isEmpty()) {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(null);
			response.setServiceMessage("No previous timesheet history found for autofill.");
			return response;
		}

		// PageRequest.of(0,1) ensures at most one entity
		EmployeeTimesheetsNew candidate = history.get(0);

		if (candidate == null) {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(null);
			response.setServiceMessage("No previous working timesheet found for autofill.");
			return response;
		}

		Long candidateTimesheetId = candidate.getTimesheetId();
		if (candidateTimesheetId == null) {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(null);
			response.setServiceMessage("No valid previous working timesheet found for autofill.");
			return response;
		}

		// Validate that all projects under this timesheet are still active for the employee
		// and active for the target date (same logic as getProjectListForDateAndEmpId)
		List<ProjectTimesheetDTO> projects = projectTimesheetService.findByTimesheetId(candidateTimesheetId);
		if (projects != null && !projects.isEmpty()) {
			// 1. Get projects active for empId + targetDate (mapping startDate/endDate valid)
			List<Integer> activeProjectIds = employeeTeamMapRepository
					.findActiveProjectIdsByEmpIdAndDate(empId, targetDate.atStartOfDay());
			Set<Integer> activeSet = new HashSet<>(activeProjectIds != null ? activeProjectIds : List.of());

			for (ProjectTimesheetDTO project : projects) {
				if (project.getProjectId() == null) {
					continue;
				}
				// 2. Project must be active for target date
				if (!activeSet.contains(project.getProjectId().intValue())) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(null);
					response.setServiceMessage("Previous timesheet projects are not active for the selected date.");
					return response;
				}
				try {
					// 3. Re-use existing assignment validation (checks active mapping)
					timesheetValidationHelper.validateProjectAssignment(empId, project.getProjectId().longValue());
				} catch (IllegalArgumentException ex) {
					// If any project is no longer active for this employee, do not autofill
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(null);
					response.setServiceMessage("Previous timesheet projects are no longer active for autofill.");
					return response;
				}
			}
		}

		// Fetch full hierarchical data for this timesheet
		EmployeeTimesheetDTO template = getTimesheetByIdInternalNew(candidateTimesheetId);
		if (template == null) {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(null);
			response.setServiceMessage("Unable to load previous timesheet for autofill.");
			return response;
		}

		// Do NOT send documents as part of autofill template
		template.setDocumentData(null);

		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(template);
		response.setServiceMessage("Autofill template fetched successfully.");
		return response;
	}

	/**
	 * Fetch document data for a timesheet. Returns empty list on error or when no docs exist.
	 */
	private List<TimesheetDocumentDataDTO> getDocumentDataForTimesheet(Long timesheetId) {
		if (timesheetId == null) {
			return new ArrayList<>();
		}
		try {
			List<TimesheetDocumentDataDTO> docs = timesheetDocumentService.getTimesheetDocumentDataByTimesheetId(timesheetId);
			return docs != null ? docs : new ArrayList<>();
		} catch (Exception e) {
			return new ArrayList<>();
		}
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

	public ServiceResponse getTimesheetMetadataByEmpId(TimesheetDTO timesheetDTO) {
		return timesheetQueryService.getTimesheetMetadataByEmpId(timesheetDTO);
	}

	public ServiceResponse getActiveProjectsAndClientSideIdByEmpId(Long empId) {

		return timesheetQueryService.getActiveProjectsAndClientSideIdByEmpId(empId);

	}

	public ServiceResponse getAlreadyFilledTimesheetDatesByEmpId(Long empId, String dayType) {
		return timesheetQueryService.getAlreadyFilledTimesheetDatesByEmpId(empId, dayType);
	}
	
	
	

	public Resource getDocumentDataByDocId(Long docId, Boolean approvedDocType) {
		try {
			if (Boolean.TRUE.equals(approvedDocType)) {
				FinalDocumentNew finalDocument = finalDocumentNewRepository.findById(docId).orElse(null);
				if (finalDocument != null && finalDocument.getFileUrl() != null) {
					return timesheetDocumentService.viewFile(finalDocument.getFileUrl());
				}
			} else {
				TimesheetDocumentDetailsNew docDetails = timesheetDocumentDetailsNewRepository.findByDocIdAndActive(docId, true);
				if (docDetails != null && docDetails.getFileUrl() != null) {
					return timesheetDocumentService.viewFile(docDetails.getFileUrl());
				}
				if (approvedDocType == null) {
					FinalDocumentNew finalDocument = finalDocumentNewRepository.findById(docId).orElse(null);
					if (finalDocument != null && finalDocument.getFileUrl() != null) {
						return timesheetDocumentService.viewFile(finalDocument.getFileUrl());
					}
				}
			}
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
	
	public ServiceResponse bulkApproveOrRejectTimesheet(TimesheetApprovalNewDTO aprOrRejData) {
		ServiceResponse serviceResponse =  new ServiceResponse();
		if(aprOrRejData.getStatusId() == 2)
			serviceResponse = timesheetApprovalServiceNew.bulkApproveTimesheets(aprOrRejData);
		else if(aprOrRejData.getStatusId() == 3)
			serviceResponse =  timesheetApprovalServiceNew.bulkRejectTimesheets(aprOrRejData);
		return serviceResponse;
	}
		public ServiceResponse getTimesheetStatusCountsByManager(Long managerId,  Boolean clientFilter) {
	    ServiceResponse response = new ServiceResponse();
	    try {

	        if (managerId == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//	            response.setServiceResponse("Manager Id is required");
	            return response;
	        }
	        List<Object[]> counts =
	                employeeTimesheetsNewRepository.getTimesheetStatusCountsByCurrentManagerId(managerId,clientFilter);
	        
	        Long total = 0L;
	        
	        if (!counts.isEmpty()) {
	            Object[] row = counts.get(0);
	            total = row[1] == null ? 0L : ((Number) row[1]).longValue();
	        }

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(counts);
	    } catch (Exception e) {
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceError(e.getMessage());
	        e.printStackTrace();
	    }

	    return response;
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse bulkFinalUploadProjectBased(FinalBulkUploadDTO finalBulkUploadDTO, MultipartFile file) {
	    ServiceResponse response = new ServiceResponse();

	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("bulkFinalUploadProjectBased");
	    apiLogInfo.setLogLevel("INFO");

		try {

			List<Long> empIds = finalBulkUploadDTO.getEmpIds();

			if(empIds == null || empIds.isEmpty()) {
				throw new IllegalArgumentException("Please select at least one employee.");
			}

			Integer projectId = finalBulkUploadDTO.getProjectId();

			LocalDate fromDate = finalBulkUploadDTO.getFromDate();
			LocalDate toDate = finalBulkUploadDTO.getToDate();

			if(projectId == null || projectId <= 0) {
				throw new IllegalArgumentException("Please select a project.");
			}

			if(fromDate == null || toDate == null) {
				throw new IllegalArgumentException("From date and to date are required.");
			}

			if(file == null || file.isEmpty()) {
				throw new IllegalArgumentException("File is required.");
			}

			if(file.getSize() > maxFileSize.toBytes()) {
				throw new IllegalArgumentException("File size is too large. Maximum file size is " + maxFileSize.toMegabytes() + "MB");
			}

			LocalDate today = LocalDate.now();
			int minusDays = (this.minusDays == null || this.minusDays <= 0)
					? 45
					: this.minusDays;

			LocalDate expectedDate = today.minusDays(minusDays);

			YearMonth expectedYearMonth = YearMonth.from(expectedDate);
			YearMonth currentYearMonth = YearMonth.from(today);
			YearMonth fromYearMonth = YearMonth.from(fromDate);

			// if (fromYearMonth.equals(currentYearMonth)) {
			// 	throw new IllegalArgumentException(
			// 			"From date cannot be in the current month"
			// 	);
			// }

			if(fromDate.isAfter(toDate)) {
				throw new IllegalArgumentException("Invalid date range. From date cannot be greater than to date.");
			}
			if(toDate.isBefore(fromDate)){
				throw new IllegalArgumentException("Invalid date range. To date cannot be less than from date.");
			}

			List<String> allowedFileExtensions = List.of("png", "jpg", "jpeg", "pdf","xlsx","xls","ods");
			String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);

			if(!allowedFileExtensions.contains(fileExtension.toLowerCase())) {
				throw new IllegalArgumentException(
						"Invalid file extension. Allowed file extensions are: " + String.join(", ", allowedFileExtensions)
				);
			}

			if (checkMinusDaysForBulkUpload) {

				LocalDate expectedToDate = fromYearMonth.atEndOfMonth();
				if (toDate.isAfter(expectedToDate)) {
					throw new IllegalArgumentException(
							"Invalid date range. To date cannot be greater than expected to date."
					);
				}

				LocalDate allowedStartDate;

				if (expectedYearMonth.equals(currentYearMonth.minusMonths(1))) {
					allowedStartDate = expectedYearMonth.atDay(1);
				} else {
					allowedStartDate = expectedYearMonth.atDay(15);
				}

				if (fromDate.isBefore(allowedStartDate)) {
					throw new IllegalArgumentException(
							String.format(
									"Invalid date range. From date cannot be before %s",
									allowedStartDate
							)
					);
				}
			}

			Long createdBy = finalBulkUploadDTO.getCreatedBy();

			List<EmployeeTimesheetsNew> notFilledTimesheetDocumentDetails = new ArrayList<>();

			notFilledTimesheetDocumentDetails = timesheetDocumentDetailsNewRepository.getDocsByEmpIdsAndDate(empIds, fromDate, toDate, projectId);

			if(notFilledTimesheetDocumentDetails == null || notFilledTimesheetDocumentDetails.isEmpty()) {
				throw new IllegalArgumentException("No Eligible timesheet(s) found for the given employee(s) and date range.");
			}

			Set<Long> timesheetIds = notFilledTimesheetDocumentDetails.stream().map(EmployeeTimesheetsNew::getTimesheetId).collect(Collectors.toSet());

			List<Long> rejectedTimesheetIds = new ArrayList<>();

			for(EmployeeTimesheetsNew timesheet : notFilledTimesheetDocumentDetails){
				if(timesheet.getStatus() == 3){
					rejectedTimesheetIds.add(timesheet.getTimesheetId());
				}
			}			

			List<ProjectTimesheetStatusNew> projectTimesheetStatusNewList = projectTimesheetStatusNewRepository.findByTimesheetIdsAndProjectIdAndStatusPending(timesheetIds, projectId);

			// both pending and rejected
			List<TimesheetDocumentDetailsNew> timesheetDocumentDetailsNewList = timesheetDocumentDetailsNewRepository.getDocsByTimesheetIdsAndProjectId(timesheetIds, projectId);

			List<FinalDocumentNew> finalDocumentNewListToSave = new ArrayList<>();
			List<EmployeeTimesheetsNew> timesheetsToSave = new ArrayList<>();
			List<ProjectTimesheetStatusNew> projectTimesheetStatusNewListToSave = new ArrayList<>();
			List<TimesheetDocumentDetailsNew> timesheetDocumentDetailsNewListToSave = new ArrayList<>();

	        String contentType = file.getContentType();
			String fileName = file.getOriginalFilename();

			FinalDocumentNew finalDocumentNew = new FinalDocumentNew();
			finalDocumentNew.setCreatedBy(createdBy);
			finalDocumentNew.setCreatedOn(LocalDateTime.now());
			finalDocumentNew.setProjectId(projectId);
			finalDocumentNew.setMimeTypeId(getDocMimeTypeId(contentType));
			finalDocumentNew.setFileUrl(fileName);
			finalDocumentNew.setDocName(fileName);
			FinalDocumentNew savedFinalDocumentNew = finalDocumentNewRepository.save(finalDocumentNew);

			for(EmployeeTimesheetsNew timesheet : notFilledTimesheetDocumentDetails){
				if(timesheet.getStatus() == 3){
					timesheet.setStatus(1);
					timesheetsToSave.add(timesheet);
				}

				for(ProjectTimesheetStatusNew projectTimesheetStatusNew : projectTimesheetStatusNewList){
					if(projectTimesheetStatusNew.getId().getTimesheetId().equals(timesheet.getTimesheetId())){
						projectTimesheetStatusNew.setClientApprovalStatus(2);
						projectTimesheetStatusNew.setStatus(1);
						projectTimesheetStatusNewListToSave.add(projectTimesheetStatusNew);
					}
				}

				for(TimesheetDocumentDetailsNew tdn : timesheetDocumentDetailsNewList){

					if(tdn.getTimesheetId().equals(timesheet.getTimesheetId())){
						tdn.setClientApprovalStatusId(2);
						tdn.setFinalFlag(true);
						tdn.setBulkApprovedDocId(savedFinalDocumentNew.getFinalDocId());
						timesheetDocumentDetailsNewListToSave.add(tdn);
					}
				}
			}			
			
			
			employeeTimesheetsNewRepository.saveAll(timesheetsToSave);
			finalDocumentNewRepository.saveAll(finalDocumentNewListToSave);
			projectTimesheetStatusNewRepository.saveAll(projectTimesheetStatusNewListToSave);
			timesheetDocumentDetailsNewRepository.saveAll(timesheetDocumentDetailsNewListToSave);
			
			timesheetDocumentService.uploadFile(file, fileName);
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Documents uploaded successfully.");
			response.setServiceError(null);
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			apiLogInfo.setApiResponse("Documents uploaded successfully.");
			apiLogInfo.setLogLevel("INFO");

		} catch (IllegalArgumentException | IllegalStateException e) {
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse(e.getMessage());
	        response.setServiceError(e.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setApiResponse(e.getMessage());
	        apiLogInfo.setLogLevel("ERROR");
	        return response;

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong.");
	        response.setServiceError(e.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setApiResponse(e.getMessage());
	        apiLogInfo.setLogLevel("ERROR");
	    }

		return response;
	}
	
	private Integer getDocMimeTypeId(String contentType){
		if(mimeTypeMap.isEmpty()){
			mimeTypeMap = docMimeTypeMasterNewRepository.findAll().stream().collect(Collectors.toMap(DocMimeTypeMasterNew::getMimeType, DocMimeTypeMasterNew::getMimeTypeId));
		}
		return mimeTypeMap.get(contentType);
	}



	public ServiceResponse getEmployeeViewForClientAttendanceStatus(GetEmployeeSummaryOnExportDTO object) {

	    ServiceResponse response = new ServiceResponse();

	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("getEmployeeViewForClientAttendanceStatus");
	    apiLogInfo.setLogLevel("INFO");
	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("getEmployeeViewForClientAttendanceStatus	");

	    try {

	        List<Object[]> empTimesheet;
	        int page = object.getPage(); 
			int pageSize = object.getSize();
			int offset = (page-1) * pageSize; 
			String employmentId = getStringColumnFilterValue(object.getFilters().getEmploymentId());
			String clientsideId = getStringColumnFilterValue(object.getFilters().getClientSideId());
			String employeeName = getStringColumnFilterValue(object.getFilters().getEmployeeName());
			String billableType = getStringColumnFilterValue(object.getFilters().getBillableType());
			String projectName = getStringColumnFilterValue(object.getFilters().getProjectName());
			String poNo = getStringColumnFilterValue(object.getFilters().getPoNo());
			String projectManagers = getStringColumnFilterValue(object.getFilters().getProjectManagerName());
			String clientName = getStringColumnFilterValue(object.getFilters().getClientName());
			String teamName = getStringColumnFilterValue(object.getFilters().getTeamName());
			String department = getStringColumnFilterValue(object.getFilters().getDepartment());
			String employmentStatus = getStringColumnFilterValue(object.getFilters().getEmploymentStatus());
			String projectStatus = getStringColumnFilterValue(object.getFilters().getProjectStatus());
			String statusCode = null;
			if( projectStatus !=null && !projectStatus.isEmpty()) {
			if ("Mapped".equalsIgnoreCase(projectStatus) || projectStatus.contains("map") || projectStatus.startsWith("m") ) {
			    statusCode = "1";
			} else if ("Removed".equalsIgnoreCase(projectStatus)|| projectStatus.contains("re") || projectStatus.startsWith("r") ) {
			    statusCode = "0";
			} else if ("Approval Pending".equalsIgnoreCase(projectStatus) || projectStatus.contains("Ap") || projectStatus.startsWith("a")) {
			    statusCode = "2";
			}
			}
			List<Long> employeeIds = new ArrayList<Long>();
			
			Integer totalDistinctEmployees;

			//			authorized_employee CTE in query is replaced with the list of authorizedEmployeeList
			
			List<Long> authorizedEmployees  = employeeTimesheetsNewRepository.getAllAuthorizedEmployees(object.getEmpId());

			//			authorizedProjectIds CTE in query is replaced with the list of authorizedProject 
        	
			List<Integer> authorizedProjectIds = employeeTimesheetsNewRepository.getAllAuthorizedProjectIds(authorizedEmployees);

//	        if (object.getAllEmp()) {
	        	
	        	LocalDate fromLocalDate = LocalDate.of(object.getYear(), object.getMonth(), 1);
	        	LocalDate toLocalDate = YearMonth.of(object.getYear(), object.getMonth()).atEndOfMonth();
	        	if (object.getYear() == LocalDate.now().getYear() 
	        			&& object.getMonth() == LocalDate.now().getMonthValue()) {
	        		toLocalDate = LocalDate.now();
	        	}   	
	        	String fromDate = fromLocalDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
	        	String toDate   = toLocalDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

	        	System.out.println(fromLocalDate+"-----"+toLocalDate);
//	        	employeeIds = employeeTimesheetsNewRepository.getPaginatedEmployeeIds(
//						object.getMonth(),
//	                    object.getYear(),
//	                    object.getEmpId(),   
//	                    object.getBillableType(),
//	                    object.getStatus(),object.getEmployeeActive(),employmentId,clientsideId,employeeName,billableType,projectName,poNo,
//	                    projectManagers,clientName,teamName,department,statusCode,offset,pageSize );

	        	employeeIds = employeeTimesheetsNewRepository.getPaginatedEmployeeIdsNew(
	                    object.getBillableType(),
	                    object.getStatus(),object.getEmployeeActive(),fromDate , toDate,employmentId,
	                    clientsideId,employeeName,billableType,projectName,poNo,projectManagers, 
	                    clientName,teamName,department,statusCode,offset,pageSize , 
	                    authorizedEmployees , authorizedProjectIds);

	        	
//	            empTimesheet = employeeTimesheetsNewRepository.getEmployeeSummaryReportAllEMP(
//	                    object.getMonth(),
//	                    object.getYear(),
//	                    object.getEmpId(),   
//	                    object.getBillableType(),
//	                    object.getStatus(),object.getEmployeeActive(),employmentId,clientsideId,employeeName,billableType,projectName,poNo,
//	                    projectManagers,clientName,teamName,department,statusCode,
//	                    object.getSortBy(),
//	                    object.getSortDirection(),employeeIds);
	            
	            empTimesheet = employeeTimesheetsNewRepository.getEmployeeSummaryReportAllEMPNew(  
	                    object.getBillableType(),
	                    object.getStatus(),object.getEmployeeActive(),fromDate , toDate , employmentId,clientsideId,employeeName,billableType,projectName,poNo,
	                    projectManagers,clientName,teamName,department,statusCode,
	                    object.getSortBy(),
	                    object.getSortDirection(),employeeIds , authorizedEmployees , authorizedProjectIds);
	            
//	            totalDistinctEmployees = employeeTimesheetsNewRepository.getTotalEmployeeCount(
//						object.getMonth(),
//	                    object.getYear(),
//	                    object.getEmpId(),   
//	                    object.getBillableType(),
//	                    object.getStatus(),object.getEmployeeActive(),employmentId,clientsideId,employeeName,billableType,projectName,poNo,
//	                    projectManagers,clientName,teamName,department,statusCode);
//	            
	            
	            totalDistinctEmployees = employeeTimesheetsNewRepository.getTotalEmployeeCountNew(  
	                    object.getBillableType(),
	                    object.getStatus(),object.getEmployeeActive(),employmentId,clientsideId,employeeName,billableType,projectName,poNo,
	                    projectManagers,clientName,teamName,department,statusCode , authorizedEmployees , authorizedProjectIds);
	            
	            
//	        } 
//	        else {
//	        	System.out.println("else"+authorizedEmployees);
//	        	System.out.println(authorizedProjectIds);
//	        	employeeIds = employeeTimesheetsNewRepository.getPaginatedEmployeeIdsForClientAttendance(
//	        			object.getMonth(),
//	                    object.getYear(),
//	                    object.getEmpId(),
//	                    object.getStatus(),
//						object.getClientSideFilter(),employmentId,clientsideId,employeeName,billableType,projectName,poNo,
//	                    projectManagers,clientName,teamName,department,employmentStatus,statusCode,offset,pageSize);
//	        	
//	            empTimesheet = employeeTimesheetsNewRepository.getEmployeeViewForClientAttendanceStatus(
//	                    object.getMonth(),
//	                    object.getYear(),
//	                    object.getEmpId(),
//	                    object.getStatus(),
//						object.getClientSideFilter(),employmentId,clientsideId,employeeName,billableType,projectName,poNo,
//	                    projectManagers,clientName,teamName,department,employmentStatus,statusCode,
//	                    object.getSortBy(),
//	                    object.getSortDirection(),employeeIds
//						);
//	            
//	            totalDistinctEmployees = employeeTimesheetsNewRepository.getTotalEmployeeCountForClientApplicable(
//	            		object.getMonth(),
//	                    object.getYear(),
//	                    object.getEmpId(),
//	                    object.getStatus(),
//						object.getClientSideFilter(),employmentId,clientsideId,employeeName,billableType,projectName,poNo,
//	                    projectManagers,clientName,teamName,department,employmentStatus,statusCode);
//	        }
	        
	        // Map to hold employees grouped by empId
	        Map<Long, EmployeeInfoDTO> employeeMap = new HashMap<>();

	        for (Object[] obj : empTimesheet) {

	            Long empId = obj[0] != null ? Long.parseLong(obj[0].toString()) : null;

	            // ---------- Create EmployeeInfoDTO only once per Employee ----------
	            EmployeeInfoDTO employeeInfo = employeeMap.get(empId);

	            if (employeeInfo == null) {

	                employeeInfo = new EmployeeInfoDTO();

	                employeeInfo.setEmpId(empId);
	                employeeInfo.setEmployeeName(obj[5] != null ? obj[5].toString() : null);
	                employeeInfo.setDepartment(obj[9] != null ? obj[9].toString() : null);
	                employeeInfo.setEmploymentId(obj[114] != null ? obj[114].toString() : null);

	                employeeInfo.setProjectTimesheet(new ArrayList<>());

	                employeeMap.put(empId, employeeInfo);
	            }

	            // ---------- Create project + timesheet DTO ----------
	            ProjectTimesheetInfoDTO projectInfo = new ProjectTimesheetInfoDTO();

	            projectInfo.setClientSideId(obj[1] != null ? obj[1].toString() : null);
	            projectInfo.setStartDate(obj[2] != null ? obj[2].toString() : null);
	            projectInfo.setTeamName(obj[3] != null ? obj[3].toString() : null);
	            projectInfo.setTeamId(obj[4] != null ? Long.parseLong(obj[4].toString()) : null);
	            projectInfo.setSpoc(obj[6] != null ? obj[6].toString() : null);
	            projectInfo.setBillableType(obj[7] != null ? obj[7].toString() : null);
	            projectInfo.setEmployeeRole(obj[8] != null ? obj[8].toString() : null);
	            projectInfo.setProjectId(obj[10] != null ? Integer.parseInt(obj[10].toString()) : null);
	            projectInfo.setProjectName(obj[11] != null ? obj[11].toString() : null);
	            projectInfo.setProjectManagerName(obj[12] != null ? obj[12].toString() : null);
	            projectInfo.setPoNo(obj[13] != null ? obj[13].toString() : null);
	            projectInfo.setClientName(obj[14] != null ? obj[14].toString() : null);
	            projectInfo.setReportingManagerId(obj[15] != null ? Long.parseLong(obj[15].toString()) : null);
	            projectInfo.setMonthName(obj[16] != null ? obj[16].toString() : null);

	            projectInfo.setExpectedTimesheetFillCount(obj[17] != null ? Integer.parseInt(obj[17].toString()) : null);
	            projectInfo.setClientSideNotFilledCount(obj[18] != null ? Integer.parseInt(obj[18].toString()) : null);
	            projectInfo.setClientSidePendingCount(obj[19] != null ? Integer.parseInt(obj[19].toString()) : null);
	            projectInfo.setClientSideApprovedCount(obj[20] != null ? Integer.parseInt(obj[20].toString()) : null);

	            // ---------- 31 days timesheet mapping ----------
	            Map<String, TimesheetDataDTO> timesheetData = new HashMap<>();
	            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
	            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("hh:mm a");

	            for (int i = 0; i < 31; i++) {

	                int baseIndex = 21 + (i * 3);

	                String status = obj.length > baseIndex && obj[baseIndex] != null
	                        ? obj[baseIndex].toString()
	                        : null;

	                String inTimeRaw = obj.length > (baseIndex + 1) && obj[baseIndex + 1] != null
	                        ? obj[baseIndex + 1].toString()
	                        : null;

	                String outTimeRaw = obj.length > (baseIndex + 2) && obj[baseIndex + 2] != null
	                        ? obj[baseIndex + 2].toString()
	                        : null;

	                String inTime = null;
	                String outTime = null;

	                try {
	                    if (inTimeRaw != null && !inTimeRaw.isEmpty()) {
	                        LocalDateTime inDateTime = LocalDateTime.parse(inTimeRaw, inputFormatter);
	                        inTime = inDateTime.format(outputFormatter);
	                    }
	                    if (outTimeRaw != null && !outTimeRaw.isEmpty()) {
	                        LocalDateTime outDateTime = LocalDateTime.parse(outTimeRaw, inputFormatter);
	                        outTime = outDateTime.format(outputFormatter);
	                    }
	                } catch (Exception e) {}

	                TimesheetDataDTO dayData = new TimesheetDataDTO();
	                dayData.setStatus(status);
	                dayData.setInTime(inTime);
	                dayData.setOutTime(outTime);

	                timesheetData.put("d" + (i + 1), dayData);
	            }

	            projectInfo.setTimesheetData(timesheetData);

	            // ---------- Summary fields ----------
	            projectInfo.setPresent(obj[115] != null ? obj[115].toString() : null);
	            projectInfo.setWeekOff(obj[116] != null ? obj[116].toString() : null);
	            projectInfo.setHoliday(obj[117] != null ? obj[117].toString() : null);
	            projectInfo.setLeave(obj[118] != null ? obj[118].toString() : null);
	            projectInfo.setCompOff(obj[119] != null ? obj[119].toString() : null);
	            projectInfo.setNa(obj[120] != null ? obj[120].toString() : null);
	            projectInfo.setHalfDay(obj[121] != null ? obj[121].toString() : null);
	            projectInfo.setTotalNoOfDays(obj[122] != null ? obj[122].toString() : null);
	            projectInfo.setApmosysTimesheetFilledCount(obj[123] != null ? Integer.parseInt(obj[123].toString()) : null);
	            projectInfo.setEmploymentStatus(obj[124] != null ? obj[124].toString() : null);
	            projectInfo.setEndDate(obj[125] != null ? obj[125].toString() : null);
	            projectInfo.setReadyForInvoicing(obj[126] != null ? obj[126].toString() : null);

	            // ---------- Project Status (Integer) ----------
	            if (obj[127] != null) {
	                int active = Integer.parseInt(obj[127].toString());
	                switch (active) {
	                    case 1:
	                        projectInfo.setProjectStatus("Mapped");
	                        break;
	                    case 0:
	                        projectInfo.setProjectStatus("Removed");
	                        break;
	                    case 2:
	                        projectInfo.setProjectStatus("Approval Pending");
	                        break;
	                    default:
	                        projectInfo.setProjectStatus("Undefined");
	                }
	            }

	            // ---------- Project Active (Boolean) ----------
	            if (obj[128] != null) {
	                String active = obj[128].toString();
	                if ("true".equals(active)) projectInfo.setProjectActive("Active");
	                else if ("false".equals(active)) projectInfo.setProjectActive("Inactive");
	                else projectInfo.setProjectActive("Undefined");
	            }

	            // Add project info to employee
	            employeeInfo.getProjectTimesheet().add(projectInfo);
	        }

	        // Convert map to list
	        List<EmployeeInfoDTO> finalList = new ArrayList<>(employeeMap.values());

			//code for sorting project-status mapped first then removed.
			for (EmployeeInfoDTO employee : finalList) {

				if (employee.getProjectTimesheet() != null && !employee.getProjectTimesheet().isEmpty()) {
					
					List<ProjectTimesheetInfoDTO> sortedProjects = employee.getProjectTimesheet().stream()
						.sorted((p1, p2) -> {

							boolean p1IsMapped = "Mapped".equalsIgnoreCase(p1.getProjectStatus());
							boolean p2IsMapped = "Mapped".equalsIgnoreCase(p2.getProjectStatus());
							
							if (p1IsMapped && !p2IsMapped) {
								return -1; // p1 comes first
							} else if (!p1IsMapped && p2IsMapped) {
								return 1; // p2 comes first
							} else {
								// If both are same status, you can add secondary sorting here
								// For example, by project name or start date
								return 0;
							}
						})
						.collect(Collectors.toList());
					
					employee.setProjectTimesheet(sortedProjects);
				}
			}

	        if (finalList.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Unable to fetch the timesheet Data !!!");
	            apiLogInfo.setApiResponse("Failed to set the data in dto \n");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse(finalList);
		        response.setTotalElements(totalDistinctEmployees);
	            apiLogInfo.setApiResponse("Timesheet Data fetched successfully ");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        }

	        apiLogInfo.setApiRequest(logBuilder.toString());
	        logService.logMyInfo(httpRequest, apiLogInfo);
	        return response;

	    } catch (Exception e) {

	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong.");
	        response.setServiceError(e.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setApiResponse(e.getMessage());
	        apiLogInfo.setLogLevel("ERROR");
	    }

	    return response;
	}

	private String getStringColumnFilterValue(String columnValue) {
		if(columnValue == null || columnValue.isBlank()) {
			return null;
		}
		return columnValue.toLowerCase();
	}
	
//	private Integer getIntegerColumnFilterValue(Integer columnValue) {
//		if(columnValue == null || columnValue.toString().isBlank()) {
//			return null;
//		}
//		return columnValue;
//	}

	public ServiceResponse getMyReporteesAndClientSideProjectsInMonthYear(TimesheetDTO timesheetDTO) {

	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getMyReporteesAndClientSideProjectsInMonthYear");
	    apiLogInfo.setLogLevel("INFO");

	    try {

	    
	       	String monthYear = timesheetDTO.getMonthYear(); 
	        Integer year = null;
	        Integer month = null;

	        if (monthYear != null && monthYear.contains("-")) {
	            String[] parts = monthYear.split("-");
	            year = Integer.parseInt(parts[0]);
	            month = Integer.parseInt(parts[1]);
	        }

			LocalDate toDate = YearMonth.of(year, month).atEndOfMonth();
			LocalDate fromDate = YearMonth.of(year, month).atDay(1);
	
	        List<Object[]> resultList = employeeTimesheetsNewRepository.getMyReporteesAndClientSideProjectsInMonthYearNew(year,month,timesheetDTO.getManagerId(),toDate,fromDate);
	                       

	        if (resultList == null || resultList.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No reportees found!");
	            response.setServiceMessage("No reportees found for given month & manager.");

	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	      
	        Map<Long, EmployeeDTO> employeeMap = new LinkedHashMap<>();

	        for (Object[] row : resultList) {

	            Long empId = row[0] != null ? Long.parseLong(row[0].toString()) : null;

	            EmployeeDTO employeeDTO = employeeMap.get(empId);

	            if (employeeDTO == null) {
	                employeeDTO = new EmployeeDTO();
	                employeeDTO.setEmpId(empId);
	                employeeDTO.setName(row[1] != null ? row[1].toString() : null);
	                employeeDTO.setProjectList(new ArrayList<>());

	                employeeMap.put(empId, employeeDTO);
	            }

	           
	            ProjectDTO projectDTO = new ProjectDTO();
	            projectDTO.setProjectId(row[2] != null ? Integer.parseInt(row[2].toString()) : null);
	            projectDTO.setProjectName(row[3] != null ? row[3].toString() : null);
	            employeeDTO.getProjectList().add(projectDTO);
	        }

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(new ArrayList<>(employeeMap.values()));
	        response.setServiceMessage("Reportees & project details fetched successfully!");

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        apiLogInfo.setApiResponse("Data fetched successfully");

	    } catch (Exception e) {
	        e.printStackTrace();

	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong.");
	        response.setServiceError(e.getMessage());

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setApiResponse(e.getMessage());
	        apiLogInfo.setLogLevel("ERROR");
	    }

	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}
	
	public ServiceResponse getTimesheetDashboardCountForProject(Integer month, Integer year,Long empId,Boolean isClientDashboard ,List<String> billableType,String projectActive) {
		return timesheetDashboardService.getTimesheetDashboardCountForProject(
				month, year, empId, isClientDashboard, billableType, projectActive);
	}
	
	 public ServiceResponse getProjectViewForClientAttendanceStatus(TimesheetDTO timesheetDTO)
	 {
		 return timesheetDashboardService.getProjectViewForClientAttendanceStatus(timesheetDTO);
	 }

}
