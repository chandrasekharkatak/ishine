package com.apmosys.employeeportal.service.validator;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.ActivityTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.EmployeeTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.LocationSessionDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.ProjectTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.TimesheetDocumentDataDTO;
import com.apmosys.employeeportal.enums.DayTypeCode;
import com.apmosys.employeeportal.enums.DayTypeTransition;
import com.apmosys.employeeportal.exception.UnauthorizedAccessException;
import com.apmosys.employeeportal.model.DayTypeMasterNew;
import com.apmosys.employeeportal.model.EmployeeLeave;
import com.apmosys.employeeportal.model.EmployeeTimesheetLocationMapping;
import com.apmosys.employeeportal.model.EmployeeTimesheetsNew;
import com.apmosys.employeeportal.repository.ActivitiesRepository;
import com.apmosys.employeeportal.repository.DayTypeMasterNewRepository;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.EmployeeTimesheetLocationMappingRepository;
import com.apmosys.employeeportal.repository.EmployeeTimesheetsNewRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.service.ActivityTimesheetService;
import com.apmosys.employeeportal.service.ProjectTimesheetService;
import com.apmosys.employeeportal.service.TimesheetService;
import com.apmosys.employeeportal.service.helper.TimesheetAggregationHelper;
import com.apmosys.employeeportal.utility.DateConversionUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Validation helper for new timesheet structure.
 * Provides validation methods for all levels of timesheet hierarchy.
 * 
 * @author System
 * @version 1.0
 */
@Component
@Slf4j
public class TimesheetValidationHelper {
	
	private final String pattern="yyyy-MM-dd HH:mm:ss";


    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EmployeeTeamMapRepository employeeTeamMapRepository;

    @Autowired
    private ActivitiesRepository activitiesRepository;

    @Autowired
    private DayTypeMasterNewRepository dayTypeMasterNewRepository;
    
    @Autowired
    private EmployeeTimesheetsNewRepository employeeTimesheetsNewRepository;
    
    
    
    public void validateEmployeeAuthorization(EmployeeTimesheetDTO timesheetDTO) {
        if (!Objects.equals(timesheetDTO.getEmpId(), timesheetDTO.getCreatedBy())) {
            TimesheetService timesheetService = new TimesheetService();
            List<EmployeeDTO> teamList = timesheetService.getAllTeamMemberView(timesheetDTO.getCreatedBy());
            boolean isEmpPresent = teamList.stream()
                    .anyMatch(emp -> emp.getEmpId() != null && emp.getEmpId().equals(timesheetDTO.getEmpId()));
            
            if (!isEmpPresent) {
                log.warn("Unauthorized access attempt: empId={}, createdBy={}", 
                        timesheetDTO.getEmpId(), timesheetDTO.getCreatedBy());
                throw new UnauthorizedAccessException("Employee not authorized to perform this action");
            }
        }
       }

    public void validateNullAndUnexpectedData(EmployeeTimesheetDTO dto) {

		if (dto == null) {
			throw new IllegalArgumentException("EmployeeTimesheetDTO cannot be null");
		}

		if (dto.getEmpId() == null) {
			throw new IllegalArgumentException("Employee ID is required");
		}

		if (dto.getDate() == null) {
			throw new IllegalArgumentException("Timesheet date is required");
		}

		if (dto.getDate().isAfter(LocalDate.now())) {
			throw new IllegalArgumentException("Timesheet date cannot be in the future");
		}

		if (dto.getDayTypeId() == null) {
			throw new IllegalArgumentException("Day type is required");
		}

		
	 }

	 public void validateWorkInWorkOutTime(EmployeeTimesheetDTO dto) {
		        if (dto.getWorkCheckIn() == null || dto.getWorkCheckOut() == null ||
						dto.getWorkCheckIn().trim().isEmpty() || dto.getWorkCheckOut().trim().isEmpty())
				{
					throw new IllegalArgumentException("workCheckIn and workCheckOut are mandatory for working days");
				}

				if (!DateConversionUtil.stringToLocalDateTime(dto.getWorkCheckOut(),pattern).isAfter(DateConversionUtil.stringToLocalDateTime(dto.getWorkCheckIn(),pattern)))
				{
					throw new IllegalArgumentException("workCheckOut must be after workCheckIn");
				}
			
		}
    
        
        /**
         * Validate timesheet lock period for employee.
         * If lock is enabled, employee cannot create/update timesheet
         * older than configured lock days.
         *
         * @param empId Employee ID
         * @param timesheetDate Timesheet date
         * @param lockDays Number of days allowed for modification
         */
        public void validateTimesheetLockPeriod(Long empId,
                                                LocalDate timesheetDate,
                                                Integer lockDays) {

            if (empId == null || timesheetDate == null || lockDays == null) {
                return; // nothing to validate
            }

            String isLockEnabled = employeeRepository.getIsLockEnabled(empId);

            if ("true".equalsIgnoreCase(isLockEnabled)) {

                LocalDate lockCutoffDate = LocalDate.now().minusDays(lockDays);

                if (timesheetDate.isBefore(lockCutoffDate)) {
                    throw new IllegalArgumentException(
                            "Timesheet is locked. You cannot modify timesheets older than "
                                    + lockDays + " days.");
                }
            }
        }

      public void validateLocationWiseProjectAndActivities(EmployeeTimesheetDTO empDTO) {

            boolean isWorkingDay = isWorkingDay(empDTO);

            if (isWorkingDay &&
                (empDTO.getLocationSessions() == null || empDTO.getLocationSessions().isEmpty())) {
                throw new IllegalArgumentException(
                        "At least one location session is required for working days");
            }

            if (empDTO.getLocationSessions() == null) {
                return;
            }

            for (LocationSessionDTO location : empDTO.getLocationSessions()) {
                validateLocationSession(location, empDTO);
            }
        }

   
      // TODO :: Need to Test date comparison properly 
      private void validateLocationSession(LocationSessionDTO location, EmployeeTimesheetDTO empDTO) {
    	  
    	  
    	  
    	  if (location.getWorkLocationTypeId() == null) 
    	  { 
    		  throw new IllegalArgumentException("Work location type is required"); 
    	  } 
    	  
    	  if (location.getWorkLocationTypeId()!=3 &&  (location.getLocationInTime() == null || location.getLocationOutTime() == null))
    	  {
    		  throw new IllegalArgumentException("Location inTime and outTime are required for non-remote locations");
    	  }
    	  if(location.getWorkLocationTypeId()!=3) {
    		 LocalDateTime locInTime = DateConversionUtil.stringToLocalDateTime(location.getLocationInTime(),pattern);
    		 LocalDateTime locOutTime = DateConversionUtil.stringToLocalDateTime(location.getLocationOutTime(),pattern);

      	    if (!locOutTime.isAfter(locInTime)) {
      	        throw new IllegalArgumentException("Location outTime must be greater than inTime");
      	    }

      	    LocalDate date = empDTO.getDate();

      	    LocalDateTime officeIn = DateConversionUtil.stringToLocalDateTime(empDTO.getWorkCheckIn(),pattern);
      	    LocalDateTime officeOut = DateConversionUtil.stringToLocalDateTime(empDTO.getWorkCheckOut(),pattern);

      	    if (officeIn != null && officeOut != null) {

      	        if (locInTime.isBefore(officeIn) || locInTime.isAfter(officeOut)) {
      	            throw new IllegalArgumentException("Location inTime must be within work range");
      	        }

      	        if (locOutTime.isBefore(officeIn) || locOutTime.isAfter(officeOut)) {
      	            throw new IllegalArgumentException("Location outTime must be within work range");
      	        }
      	    }

    	  }

    	    
    	    for (ProjectTimesheetDTO project : location.getProjects()) {
    	        validateProject(project, empDTO);
    	    }
    	}

      
      
         private void validateProject(ProjectTimesheetDTO project,
                                     EmployeeTimesheetDTO empDTO) {

            if (project.getProjectId() == null) {
            throw new IllegalArgumentException("Project ID is required");
        }


            boolean isWorkingDay = isWorkingDay(empDTO);
            
            
            if (isWorkingDay) {

                if (Boolean.TRUE.equals(project.getIsShadowTimesheet()) 
                    && Boolean.FALSE.equals(project.getIsShadowForSelf()) 
                    && project.getShadowEmpId() == null) {

                    throw new IllegalArgumentException(
                        String.format("Shadow employee id is mandatory for shadow timesheet on project.")
                    );
                }

            }

           if (isWorkingDay &&
                (project.getActivities() == null || project.getActivities().isEmpty())) {
                throw new IllegalArgumentException(
                        "At least one activity is required for project "
                                + project.getProjectId());
            }

            if (project.getActivities() != null) {
                for (ActivityTimesheetDTO activity : project.getActivities()) {
                    validateActivity(activity, project);
                }
            }
        }

        /* =====================================================
           ACTIVITY VALIDATION
           ===================================================== */

        private void validateActivity(ActivityTimesheetDTO activity,
                                      ProjectTimesheetDTO project) {

            if (activity.getActivityId() == null) {
                throw new IllegalArgumentException("Activity ID is required");
            }

            if (activity.getDurationMinutes() == null ||
                activity.getDurationMinutes() <= 0) {
                throw new IllegalArgumentException(
                        "Activity duration must be greater than 0 minutes");
            }

           
        }
        /**
         * Validate document requirements for new contract.
         * Applies only for:
         *  - Working days
         *  - Shadow timesheets
         *  - Projects where client-side document is mandatory
         */
        public void validateDocumentsDTO(EmployeeTimesheetDTO empDTO) {

            if (empDTO == null) {
                return;
            }

            // 1️⃣ Skip validation for non-working day types
            if (isNonWorkingDay(empDTO.getDayType())) {
                return;
            }

            List<TimesheetDocumentDataDTO> docs=empDTO.getDocumentData();
            // Normalize docs list
            List<TimesheetDocumentDataDTO> documentList =
                    docs != null ? docs : List.of();

            // 3️⃣ Validate per project
            if (empDTO.getLocationSessions() == null) {
                return;
            }

            for (LocationSessionDTO location : empDTO.getLocationSessions()) {
                if (location.getProjects() == null) continue;

                for (ProjectTimesheetDTO project : location.getProjects()) {

                    Integer projectId = project.getProjectId();
                    
                    if(project.getIsShadowForSelf()) continue;

                    // Check if client-side document is mandatory for this project
                    Boolean isClientSideMandatory =
                            projectRepository.getClientSideIdMandatory(projectId.intValue());

                    if (!Boolean.TRUE.equals(isClientSideMandatory)) {
                        continue;
                    }

                    // 4️⃣ Check if document exists for this project
                    boolean documentPresentForProject =
                            documentList.stream()
                                    .anyMatch(doc ->
                                            projectId.longValue() == doc.getProjectId() &&
                                            doc.getDocName() != null &&
                                            doc.getFinalFlag() != null
                                    );

                    if (!documentPresentForProject) {
                        throw new IllegalArgumentException(
                                "Client-side ID is mandatory. Please upload required documents for projectId: "
                                        + projectId);
                    }
                }
        }
    }

    /**
         * Validate uploaded document files based on new contract rules.
         *
         * Rules:
         * - Applies only if client-side document is mandatory for project
         * - At least ONE filled document is mandatory per project
         * - Approved document is optional
         * - Max 2 documents per project (filled + approved)
         * - File name format: projectId_filled_xxx OR projectId_approved_xxx
         */
        public void validateUploadedDocuments(EmployeeTimesheetDTO empDTO,
                                              List<MultipartFile> documents) {

            if (empDTO == null) {
                return;
            }

            // 1️⃣ Skip validation for non-working days
            if (isNonWorkingDay(empDTO.getDayType())) {
                return;
            }
            List<TimesheetDocumentDataDTO> docs=empDTO.getDocumentData();

            

            if (documents == null || documents.isEmpty()) {
                throw new IllegalArgumentException(
                        "Client-side document is mandatory. Please upload required documents.");
            }

            //Group uploaded files by projectId (parsed from filename)
            Map<Long, List<MultipartFile>> filesByProject =
                    groupFilesByProjectId(documents);

            //Validate project-wise
            for (LocationSessionDTO location : empDTO.getLocationSessions()) {
                if (location.getProjects() == null) continue;

                for (ProjectTimesheetDTO project : location.getProjects()) {

                    Integer projectId = project.getProjectId();
                    if(project.getIsShadowForSelf()) continue;
                    
                    
                    Boolean isClientIdMandatory =
                            projectRepository.getClientSideIdMandatory(projectId);

                    if (Boolean.FALSE.equals(isClientIdMandatory)) {
                        continue;
                    }

                    List<MultipartFile> projectFiles =
                            filesByProject.getOrDefault(projectId, List.of());

                    //Enforce min / max rule
                    if (projectFiles.isEmpty()) {
                        throw new IllegalArgumentException(
                                "Filled document is mandatory for projectId: " + projectId);
                    }

                    if (projectFiles.size() > 2) {
                        throw new IllegalArgumentException(
                                "Maximum 2 documents (filled + approved) allowed for projectId: "
                                        + projectId);
                    }
                    
                    if(project.getClientApprovalStatus()==2 && projectFiles.size()!=2) {
                    	throw new IllegalArgumentException(
                                "2 documents (filled + approved) are required for projectId: "
                                        + projectId);
                    }

                    boolean filledPresent = false;

                    //Validate file naming & types
                    for (MultipartFile file : projectFiles) {

                        String fileName = file.getOriginalFilename();
                        if (fileName == null) {
                            throw new IllegalArgumentException(
                                    "Invalid document name for projectId: " + projectId);
                        }

                        String lowerName = fileName.toLowerCase();

                        if (lowerName.contains("_filled_")) {
                            filledPresent = true;
                        } else if (lowerName.contains("_approved_")) {
                            // approved doc → optional
                        } else {
                            throw new IllegalArgumentException(
                                    "Invalid document name format for projectId: " + projectId +
                                    ". Expected: projectId_filled_xxx or projectId_approved_xxx");
                        }
                    }

                    // 7️ Filled doc is mandatory
                    if (!filledPresent) {
                        throw new IllegalArgumentException(
                                "Filled document is mandatory for projectId: " + projectId);
                    }
                }
            }
        }
        
        
        /**
         * Groups uploaded files by projectId extracted from filename.
         *
         * Expected filename format:
         * projectId_filled_xxx OR projectId_approved_xxx
         */
        private Map<Long, List<MultipartFile>> groupFilesByProjectId(
                List<MultipartFile> documents) {

            Map<Long, List<MultipartFile>> map = new HashMap<>();

            for (MultipartFile file : documents) {

                String fileName = file.getOriginalFilename();
                if (fileName == null || !fileName.contains("_")) {
                    throw new IllegalArgumentException(
                            "Invalid document name: " + fileName +
                            ". Expected format: projectId_filled_xxx");
                }

                String[] parts = fileName.split("_", 2);

                Long projectId;
                try {
                    projectId = Long.parseLong(parts[0]);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(
                            "Invalid projectId in document name: " + fileName);
                }

                map.computeIfAbsent(projectId, k -> new ArrayList<>()).add(file);
            }

            return map;
        }



        
        
        
        
        /**
         * Validate time consistency across timesheet:
         *
         * 1) (workCheckOut - workCheckIn) >= sum of all location session durations
         * 2) sum of all activity durations <= (workCheckOut - workCheckIn)
         *
         * This must be called AFTER basic validations and normalization.
         */
        public void validateTimeConsistency(EmployeeTimesheetDTO empDTO) {

            if (empDTO == null) {
                return;
            }

            // Apply only for working days
            if (!isWorkingDay(empDTO)) {
                return;
            }

            if (empDTO.getWorkCheckIn() == null || empDTO.getWorkCheckOut() == null) {
                return; // already handled by earlier validations
            }
            //Total work duration (in minutes)
            long totalWorkMinutes = java.time.Duration
                    .between(DateConversionUtil.stringToLocalDateTime(empDTO.getWorkCheckIn(),pattern),
                    		DateConversionUtil.stringToLocalDateTime(empDTO.getWorkCheckOut(),pattern))
                    .toMinutes();

            if (totalWorkMinutes <= 0) {
                throw new IllegalArgumentException(
                        "Invalid workCheckIn/workCheckOut duration");
            }

            //Sum of all location session durations
            long totalLocationMinutes = 0;

            if (empDTO.getLocationSessions() != null) {
                for (LocationSessionDTO location : empDTO.getLocationSessions()) {

                    if (location.getLocationInTime() == null ||
                        location.getLocationOutTime() == null) {
                        continue;
                    }

                    LocalDateTime locationIn =
                            parseLocationTime(location.getLocationInTime(), empDTO.getDate());

                    LocalDateTime locationOut =
                            parseLocationTime(location.getLocationOutTime(), empDTO.getDate());

                    if (!locationOut.isAfter(locationIn)) {
                        throw new IllegalArgumentException(
                                "Location out time must be after location in time");
                    }

                    totalLocationMinutes += java.time.Duration
                            .between(locationIn, locationOut)
                            .toMinutes();
                }
            }

            if (totalLocationMinutes > totalWorkMinutes) {
                throw new IllegalArgumentException(
                        "Sum of location session duration (" + totalLocationMinutes +
                        " mins) cannot exceed total working duration (" +
                        totalWorkMinutes + " mins)");
            }

            //Sum of all activity durations
            long totalActivityMinutes = 0;

            if (empDTO.getLocationSessions() != null) {
                for (LocationSessionDTO location : empDTO.getLocationSessions()) {
                    if (location.getProjects() == null) continue;

                    for (ProjectTimesheetDTO project : location.getProjects()) {
                        if (project.getActivities() == null) continue;

                        for (ActivityTimesheetDTO activity : project.getActivities()) {
                            if (activity.getDurationMinutes() != null) {
                                totalActivityMinutes += activity.getDurationMinutes();
                            }
                        }
                    }
                }
            }

            if (totalActivityMinutes > totalWorkMinutes) {
                throw new IllegalArgumentException(
                        "Total activity duration (" + totalActivityMinutes +
                        " mins) cannot exceed total working duration (" +
                        totalWorkMinutes + " mins)");
            }
        }
        
        public boolean isWorkingDay(EmployeeTimesheetDTO dto) {

            if (dto.getDayTypeId() == null) {
                return false;
            }

            DayTypeMasterNew dayType = dayTypeMasterNewRepository
                    .findById(dto.getDayTypeId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Invalid dayTypeId"));

            // Java authoritative meaning
            DayTypeCode dayTypeCode =
                    DayTypeCode.fromDbValue(dayType.getDayType());

            // DB configuration
            boolean dbSaysWorking =
                    Boolean.TRUE.equals(dayType.getIsWorkingDay());

            // Safety check — mismatch should NEVER happen silently
            if (dbSaysWorking != dayTypeCode.isWorkingDay()) {
                throw new IllegalStateException(
                        "DayType mismatch detected. dayType='"
                                + dayType.getDayType()
                                + "', DB=" + dbSaysWorking
                                + ", Java=" + dayTypeCode.isWorkingDay()
                );
            }

            return dayTypeCode.isWorkingDay();
        }


        
        
        /**
         * Parse location time string (HH:mm or HH:mm:ss) into LocalDateTime.
         */
        private LocalDateTime parseLocationTime(String timeStr, LocalDate date) {
            try {
                String[] parts = timeStr.split(":");
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);
                int second = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
                return LocalDateTime.of(date, java.time.LocalTime.of(hour, minute, second));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid location time format: " + timeStr);
            }
        }
        
        
        private boolean isNonWorkingDay(String dayType) {
            if (dayType == null) {
                return false;
            }
            return "Public Holiday".equalsIgnoreCase(dayType)
                || "Week Off".equalsIgnoreCase(dayType)
                || "Leave".equalsIgnoreCase(dayType)
                || "Client Holiday".equalsIgnoreCase(dayType);
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
    
    /**
     * Validate workCheckIn/workCheckOut are required for working days.
     * NEW CONTRACT: These map to officeInTime/officeOutTime
     * 
     * @param empDTO Employee timesheet DTO from new contract
     * @throws IllegalArgumentException if validation fails
     */
    public void validateWorkCheckInCheckOutForWorkingDays(
                   EmployeeTimesheetDTO empDTO) {
        if (empDTO == null) {
            return;
        }
        if (empDTO == null || empDTO.getDayTypeId() == null) {
            return; // Cannot validate without dayTypeId
        }
        
        // Check if it's a working day
        DayTypeMasterNew dayType = dayTypeMasterNewRepository.findById(empDTO.getDayTypeId())
                .orElse(null);
        
        if (dayType == null) {
            return; // Day type not found, skip validation
        }
        
        // Working day typically means dayType = "Working" (case-insensitive)
        boolean isWorkingDay = "Working".equalsIgnoreCase(dayType.getDayType()) ||
                              "Non-working".equalsIgnoreCase(dayType.getDayType());
        
        if (isWorkingDay) {
            // For working days, workCheckIn and workCheckOut are required
            if (empDTO.getWorkCheckIn() == null) {
                throw new IllegalArgumentException("workCheckIn is required for working days");
            }
            
            if (empDTO.getWorkCheckOut() == null) {
                throw new IllegalArgumentException("workCheckOut is required for working days");
            }
            
            // Also validate that officeInTime and officeOutTime are set (after normalization)
            if (empDTO.getWorkCheckIn() == null || empDTO.getWorkCheckOut() == null) {
                throw new IllegalArgumentException("Office in/out times are required for working days");
            }
        }
    }
     /**
     * Validate that timesheet does not already exist for employee and date.
     * 
     * @param empId Employee ID
     * @param date Date to check
     * @throws IllegalArgumentException if timesheet already exists
     */
    public EmployeeTimesheetsNew validateTimesheetAlreadyExists(
            EmployeeTimesheetDTO empDTO,
            Long empId,
            LocalDate date) {

        if (empDTO == null || empId == null || date == null) {
            throw new IllegalArgumentException("Employee, Date and DTO are required");
        }

        Optional<EmployeeTimesheetsNew> existingOpt =
                employeeTimesheetsNewRepository.findByEmpIdAndDateNew(empId, date);

        // No timesheet → allow creation
        if (existingOpt.isEmpty()) {
            return null;
        }

        DayTypeCode dayType =
                resolveDayType(empDTO.getDayTypeId());

        // ONLY Non-Working allows override
        if (dayType == DayTypeCode.NON_WORKING) {
            return existingOpt.get();
        }

        //Everything else is blocked
        throw new IllegalArgumentException(
                "Timesheet already exists for employee "
                        + empId + " on date " + date
                        + " (DayType=" + dayType + ")"
        );
    }

    
    private DayTypeCode resolveDayType(Integer dayTypeId) {

        DayTypeMasterNew dayType = dayTypeMasterNewRepository
                .findById(dayTypeId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid dayTypeId"));

        DayTypeCode code =
                DayTypeCode.fromDbValue(dayType.getDayType());

        // Optional safety check (recommended)
        if (!Objects.equals(
                Boolean.TRUE.equals(dayType.getIsWorkingDay()),
                code.isWorkingDay())) {

            throw new IllegalStateException(
                    "DayType mismatch for " + dayType.getDayType());
        }

        return code;
    }


    
    
    @Autowired
    EmployeeTimesheetLocationMappingRepository employeeTimesheetLocationMappingRepository;
    
    @Autowired
    ProjectTimesheetService  projectTimesheetService;
    
    @Autowired
    ActivityTimesheetService activityTimesheetService;
    
    
    
    public EmployeeTimesheetsNew validateTimesheetUpdatable(
            Long timesheetId,
            EmployeeTimesheetDTO newEmpDTO) {

        // Timesheet existence check
        EmployeeTimesheetsNew empTS = employeeTimesheetsNewRepository
                .findById(timesheetId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Timesheet with ID " + timesheetId + " does not exist"));

        // Status validation
        Integer currentStatus = empTS.getStatus();
        if (currentStatus != null
                && !currentStatus.equals(TimesheetAggregationHelper.STATUS_PENDING)
                && !currentStatus.equals(TimesheetAggregationHelper.STATUS_REJECTED)) {

            throw new IllegalStateException(
                    "Timesheet can only be updated when status is PENDING or REJECTED. Current status: "
                            + currentStatus);
        }

        // Duplicate timesheet date check
        if (!empTS.getDate().equals(newEmpDTO.getDate())) {

            employeeTimesheetsNewRepository
                    .findByEmpIdAndDateNew(newEmpDTO.getEmpId(), newEmpDTO.getDate())
                    .filter(existing ->
                            !existing.getTimesheetId().equals(timesheetId))
                    .ifPresent(existing -> {
                        throw new IllegalStateException(
                                "A timesheet already exists for employee "
                                        + newEmpDTO.getEmpId()
                                        + " on date "
                                        + newEmpDTO.getDate());
                    });
        }

        return empTS;
    }
    
    public void validateTimesheetDateImmutable(
            EmployeeTimesheetsNew existingEntity,
            EmployeeTimesheetDTO incomingDTO) {

        if (existingEntity == null || incomingDTO == null) {
            throw new IllegalArgumentException(
                    "Existing timesheet and incoming data are required"
            );
        }

        LocalDate existingDate = existingEntity.getDate();
        LocalDate incomingDate = incomingDTO.getDate();

        if (existingDate == null || incomingDate == null) {
            throw new IllegalArgumentException(
                    "Timesheet date cannot be null"
            );
        }

        if (!existingDate.equals(incomingDate)) {
            throw new IllegalArgumentException(
                    "Timesheet date cannot be changed. "
                    + "Existing date: " + existingDate
                    + ", Requested date: " + incomingDate
            );
        }
    }
    public void validateEmployeeImmutableIfProjectApproved(
            EmployeeTimesheetsNew existingEntity,
            EmployeeTimesheetDTO incomingDTO) {

        if (existingEntity == null || incomingDTO == null) {
            throw new IllegalArgumentException(
                    "Existing timesheet and incoming data are required"
            );
        }

        Long existingEmpId = existingEntity.getEmpId();
        Long incomingEmpId = incomingDTO.getEmpId();

        if (existingEmpId == null || incomingEmpId == null) {
            throw new IllegalArgumentException(
                    "Employee ID cannot be null"
            );
        }

        // If employee ID is not changing → nothing to validate
        if (existingEmpId.equals(incomingEmpId)) {
            return;
        }

        // Check if any project is approved
        boolean hasApprovedProject =
                projectTimesheetService
                        .existsApprovedProject(existingEntity.getTimesheetId());

        if (hasApprovedProject) {
            throw new IllegalArgumentException(
                    "Employee cannot be changed because one or more projects "
                  + "in the timesheet are already approved. "
                  + "Existing EmpId=" + existingEmpId
                  + ", Requested EmpId=" + incomingEmpId
            );
        }
    }



    
    public void validateLocationDeletionRules(
            Long timesheetId,
            List<LocationSessionDTO> incomingLocations) {

        //Existing location sessions from DB
        List<EmployeeTimesheetLocationMapping> existingLocations =
                employeeTimesheetLocationMappingRepository.findByTimesheetId(timesheetId);

        if (existingLocations == null || existingLocations.isEmpty()) {
            return;
        }

        // Incoming mapping IDs (ONLY non-null → existing sessions)
        Set<Long> incomingMappingIds =
                incomingLocations == null
                        ? Set.of()
                        : incomingLocations.stream()
                            .map(LocationSessionDTO::getLocationMappingId)
                            .filter(Objects::nonNull) 
                            .collect(Collectors.toSet());

        //Validate deletions session-wise
        for (EmployeeTimesheetLocationMapping dbLocation : existingLocations) {

            Long dbMappingId = dbLocation.getLocationMappingId();

            // Session removed by user
            if (!incomingMappingIds.contains(dbMappingId)) {

                boolean hasApprovedProject =
                        projectTimesheetService
                                .existsApprovedProjectByLocationMappingId(dbMappingId);

                if (hasApprovedProject) {
                    throw new IllegalStateException(
                            "Cannot delete location session because it contains approved project(s)");
                }
            }
        }
    }

    
    @Autowired
    EmployeeTimesheetLocationMappingRepository  locationRepo;
    
    public void  validateProjectDeletionRules( Long timesheetId,
            List<LocationSessionDTO> incomingLocations) {

    	  Set<Long> incomingLocationMappingIds =
    	      incomingLocations.stream()
    	        .map(LocationSessionDTO::getLocationMappingId)
    	        .filter(Objects::nonNull)
    	        .collect(Collectors.toSet());

    	  List<EmployeeTimesheetLocationMapping> dbLocations =
    	      locationRepo.findByTimesheetId(timesheetId);

    	  for (EmployeeTimesheetLocationMapping dbLoc : dbLocations) {

    	     if (!incomingLocationMappingIds.contains(dbLoc.getLocationMappingId())) {

    	        boolean hasApprovedProject =
    	        		projectTimesheetService.existsApprovedProjectByLocationMappingId(
    	                dbLoc.getLocationMappingId()
    	            );

    	        if (hasApprovedProject) {
    	           throw new IllegalStateException(
    	             "Cannot delete location session because approved project exists"
    	           );
    	        }
    	     }
    	  }
    	}

   	public void validateLocationTimeOverlap(List<LocationSessionDTO> locations) {

		if (locations == null || locations.size() <= 1) {
			return;
		}

		// Filter only time-based locations
		List<LocationSessionDTO> timeBasedLocations = locations.stream()
				.filter(l -> l.getLocationInTime() != null && l.getLocationOutTime() != null)
				.collect(Collectors.toList());

		if (timeBasedLocations.size() <= 1) {
			return;
		}

		// Sort by in-time
		timeBasedLocations.sort(Comparator.comparing(l -> LocalTime.parse(l.getLocationInTime())));

		for (int i = 0; i < timeBasedLocations.size() - 1; i++) {

			LocationSessionDTO current = timeBasedLocations.get(i);
			LocationSessionDTO next = timeBasedLocations.get(i + 1);

			LocalTime currentEnd = LocalTime.parse(current.getLocationOutTime());

			LocalTime nextStart = LocalTime.parse(next.getLocationInTime());

			if (currentEnd.isAfter(nextStart)) {
				throw new IllegalStateException("Location time overlap detected between " + current.getLocationInTime()
						+ " - " + current.getLocationOutTime() + " and " + next.getLocationInTime() + " - "
						+ next.getLocationOutTime());
			}
		}
	}
	
	public void validateActivityDurationWithinLocation(List<LocationSessionDTO> locations) {

		if (locations == null || locations.isEmpty()) {
			return;
		}

		for (LocationSessionDTO location : locations) {

			if (location.getLocationInTime() == null || location.getLocationOutTime() == null
					|| location.getProjects() == null) {
				continue;
			}

			LocalTime start = LocalTime.parse(location.getLocationInTime());
			LocalTime end = LocalTime.parse(location.getLocationOutTime());

			long locationMinutes = Duration.between(start, end).toMinutes();

			if (locationMinutes <= 0) {
				throw new IllegalArgumentException("Invalid location time range");
			}

			double totalActivityMinutes = 0;

			for (ProjectTimesheetDTO project : location.getProjects()) {

				if (project.getActivities() == null) {
					continue;
				}

				for (ActivityTimesheetDTO activity : project.getActivities()) {

					if (activity.getDurationMinutes() != null) {
						totalActivityMinutes += activity.getDurationMinutes() * 60;
					}
				}
			}

			if (totalActivityMinutes > locationMinutes) {
				throw new IllegalStateException("Total activity duration (" + totalActivityMinutes / 60
						+ " hrs) exceeds location duration (" + locationMinutes / 60 + " hrs)");
			}
		}
	}
	
	
	
	public void validateApprovedProjectImmutableByLocationMapping(
	        Long timesheetId,
	        List<LocationSessionDTO> incomingLocations) {

	    if (incomingLocations == null || incomingLocations.isEmpty()) {
	        return;
	    }

	    // Fetch APPROVED project mappings from DB
	    List<ProjectTimesheetDTO> approvedMappings =
	            projectTimesheetService
	                    .findApprovedProjectsByTimesheetId(timesheetId);

	    if (approvedMappings == null || approvedMappings.isEmpty()) {
	        return;
	    }

	    // Build incoming map:
	    // locationMappingId -> projectId -> projectDTO
	    Map<Long, Map<Integer, ProjectTimesheetDTO>> incomingMap =
	            incomingLocations.stream()
	                    .filter(l -> l.getLocationMappingId() != null
	                              && l.getProjects() != null)
	                    .collect(Collectors.toMap(
	                            LocationSessionDTO::getLocationMappingId,
	                            l -> l.getProjects().stream()
	                                    .collect(Collectors.toMap(
	                                            ProjectTimesheetDTO::getProjectId,
	                                            p -> p,
	                                            (a, b) -> a
	                                    )),
	                            (a, b) -> a
	                    ));

	    for (ProjectTimesheetDTO approved : approvedMappings) {

	        Long locationMappingId = approved.getLocationMappingId();
	        Integer projectId = approved.getProjectId();

	        Map<Integer, ProjectTimesheetDTO> incomingProjectsAtLocation =
	                incomingMap.get(locationMappingId);

	        // Project removed from this time-slot
	        if (incomingProjectsAtLocation == null
	                || !incomingProjectsAtLocation.containsKey(projectId)) {

	            throw new IllegalStateException(
	                    "Approved project cannot be removed from its time slot. "
	                    + "ProjectId=" + projectId
	                    + ", LocationMappingId=" + locationMappingId);
	        }

	        ProjectTimesheetDTO incomingProject =
	                incomingProjectsAtLocation.get(projectId);

	        // Activities immutability
	        validateApprovedProjectActivitiesImmutable(
	                approved.getActivities(),
	                incomingProject.getActivities(),
	                projectId,
	                locationMappingId);
	    }
	}
	
	
	
	private void validateApprovedProjectActivitiesImmutable(
	        List<ActivityTimesheetDTO> existingActivities,
	        List<ActivityTimesheetDTO> incomingActivities,
	        Integer projectId,
	        Long locationMappingId) {

	    if (existingActivities == null) existingActivities = List.of();
	    if (incomingActivities == null) incomingActivities = List.of();
	    
	    

	    Map<Long, ActivityTimesheetDTO> existingMap =
	            existingActivities.stream()
	                    .collect(Collectors.toMap(
	                            ActivityTimesheetDTO::getActivityId,
	                            a -> a
	                    ));

	    Map<Long, ActivityTimesheetDTO> incomingMap =
	            incomingActivities.stream()
	                    .collect(Collectors.toMap(
	                            ActivityTimesheetDTO::getActivityId,
	                            a -> a
	                    ));

	    // Add/remove activity
	    if (!existingMap.keySet().equals(incomingMap.keySet())) {
	        throw new IllegalStateException(
	                "Activities cannot be changed for approved project. "
	                + "ProjectId=" + projectId
	                + ", LocationMappingId=" + locationMappingId);
	    }

	    // Duration change
	    for (Long activityId : existingMap.keySet()) {

	        if (!Objects.equals(
	                existingMap.get(activityId).getDurationMinutes(),
	                incomingMap.get(activityId).getDurationMinutes())) {

	            throw new IllegalStateException(
	                    "Activity duration cannot be modified for approved project. "
	                    + "ProjectId=" + projectId
	                    + ", ActivityId=" + activityId
	                    + ", LocationMappingId=" + locationMappingId);
	        }
	    }
	}
	
	
	
	public void validateNonWorkingDayTimesheet(EmployeeTimesheetDTO empDTO) {

	    if (empDTO.getLocationSessions() == null ||
	        empDTO.getLocationSessions().isEmpty()) {
	        throw new IllegalArgumentException(
	                "Location session is required for Non-Working day"
	        );
	    }

	    //Multiple locations not allowed
	    if (empDTO.getLocationSessions().size() > 1) {
	        throw new IllegalArgumentException(
	                "Multiple locations are not allowed for Non-Working day"
	        );
	    }

	    LocationSessionDTO location = empDTO.getLocationSessions().get(0);

	    //Location in/out time not allowed
	    if (location.getLocationInTime() != null ||
	        location.getLocationOutTime() != null) {
	        throw new IllegalArgumentException(
	                "Location in/out time must be empty for Non-Working day"
	        );
	    }

	    // Activities not allowed
	    if (location.getProjects() != null) {
	        for (ProjectTimesheetDTO project : location.getProjects()) {
	            if (project.getActivities() != null &&
	                !project.getActivities().isEmpty()) {
	                throw new IllegalArgumentException(
	                        "Activities are not allowed for Non-Working day"
	                );
	            }
	        }
	    }

	    //  At least one project required
	    if (location.getProjects() == null ||
	        location.getProjects().isEmpty()) {
	        throw new IllegalArgumentException(
	                "At least one project must be selected for Non-Working day"
	        );
	    }
	}
	
	public void validateDayTypeTransition(EmployeeTimesheetsNew existingTS,
	        EmployeeTimesheetDTO newDTO) {
		
		if (existingTS == null || newDTO == null) {
	        throw new IllegalArgumentException(
	                "Existing timesheet and incoming data are required");
	    }

	    Integer oldDayTypeId = existingTS.getDayTypeId();
	    Integer newDayTypeId = newDTO.getDayTypeId();

	    // No change → nothing to validate
	    if (Objects.equals(oldDayTypeId, newDayTypeId)) {
	    	return;
	    }
	    if (projectTimesheetService.existsApprovedProject(
	                existingTS.getTimesheetId())) {

	            throw new IllegalArgumentException(
	                    "Cannot change day type "
	                  + "because approved work already exists.");
	        }
		
	}
	
	
	public boolean isWorkingToNonWorking(
	        EmployeeTimesheetsNew existingTS,
	        EmployeeTimesheetDTO newDTO) {

	    if (existingTS == null || newDTO == null) {
	        throw new IllegalArgumentException(
	                "Existing timesheet and incoming data are required");
	    }

	    Integer oldDayTypeId = existingTS.getDayTypeId();
	    Integer newDayTypeId = newDTO.getDayTypeId();

	    // No change → nothing to validate
	    if (Objects.equals(oldDayTypeId, newDayTypeId)) {
	        return false;
	    }

	    DayTypeCode oldType = resolveDayType(oldDayTypeId);
	    DayTypeCode newType = resolveDayType(newDayTypeId);

	    // WORKING → NON_WORKING
	    if (oldType.isWorkingDay() && !newType.isWorkingDay()) {
             return true;
	    }
	    return false;
   	}
	
	public boolean isWorkingToWorking( EmployeeTimesheetsNew existingTS,
	        EmployeeTimesheetDTO newDTO) {
		Integer oldDayTypeId = existingTS.getDayTypeId();
	    Integer newDayTypeId = newDTO.getDayTypeId();
	    
	    DayTypeCode oldType = resolveDayType(oldDayTypeId);
	    DayTypeCode newType = resolveDayType(newDayTypeId);

	 // WORKING →WORKING
	    if (oldType.isWorkingDay() && newType.isWorkingDay()) {
	    	return true;
	    }return false;
		
	}
	public boolean isNonWorkingToWorking(
	        EmployeeTimesheetsNew existingTS,
	        EmployeeTimesheetDTO newDTO) {

	    if (existingTS == null || newDTO == null) {
	        throw new IllegalArgumentException(
	                "Existing timesheet and incoming data are required");
	    }

	    Integer oldDayTypeId = existingTS.getDayTypeId();
	    Integer newDayTypeId = newDTO.getDayTypeId();

	    DayTypeCode oldType = resolveDayType(oldDayTypeId);
	    DayTypeCode newType = resolveDayType(newDayTypeId);

	    // NON-WORKING → WORKING
	    if (!oldType.isWorkingDay() && newType.isWorkingDay()) {

	        return true;
	    }

	    return false;
	}
	
	
	public boolean isNonWorkingToNonWorking(
	        EmployeeTimesheetsNew existingTS,
	        EmployeeTimesheetDTO newDTO) {

	    if (existingTS == null || newDTO == null) {
	        throw new IllegalArgumentException(
	                "Existing timesheet and incoming data are required");
	    }

	    Integer oldDayTypeId = existingTS.getDayTypeId();
	    Integer newDayTypeId = newDTO.getDayTypeId();

	    if (Objects.equals(oldDayTypeId, newDayTypeId)) {
	        return false;
	    }

	    DayTypeCode oldType = resolveDayType(oldDayTypeId);
	    DayTypeCode newType = resolveDayType(newDayTypeId);

	    // NON-WORKING → NON-WORKING
	    if (!oldType.isWorkingDay() && !newType.isWorkingDay()) {
	        return true;
	    }
        return false;
	}
	
	
	public DayTypeTransition resolveDayTypeTransition(
	        EmployeeTimesheetsNew existingTS,
	        EmployeeTimesheetDTO newDTO) {
		
	    if (isWorkingToNonWorking(existingTS, newDTO)) {
	        return DayTypeTransition.WORKING_TO_NON_WORKING;
	    }

	    if (isWorkingToWorking(existingTS, newDTO)) {
	        return DayTypeTransition.WORKING_TO_WORKING;
	    }

	    if (isNonWorkingToWorking(existingTS, newDTO)) {
	        return DayTypeTransition.NON_WORKING_TO_WORKING;
	    }

	    if (isNonWorkingToNonWorking(existingTS, newDTO)) {
	        return DayTypeTransition.NON_WORKING_TO_NON_WORKING;
	    }

	    throw new IllegalStateException("Unsupported day type transition");
	}	
	
	@Autowired
	private EmployeeLeaveRepository employeeLeaveRepository;
    
	private static final float HALF_DAY = 0.5f;

	public void validateDayTypeAgainstLeave(
	        Long empId,
	        LocalDate timesheetDate,
	        Integer dayTypeId) {

	    List<EmployeeLeave> leaves =
	            employeeLeaveRepository
	                    .findActiveLeavesByEmpIdAndDate(empId, timesheetDate);

	    // No leave → no restriction
	    if (leaves == null || leaves.isEmpty()) {
	        return;
	    }
	     
	    DayTypeMasterNew dayType = dayTypeMasterNewRepository
	            .findById(dayTypeId)
	            .orElseThrow(() ->
	                    new IllegalArgumentException("Invalid dayTypeId"));

	    DayTypeCode incomingDayType =
	            DayTypeCode.fromDbValue(dayType.getDayType());

	    // Evaluate all leaves affecting this date
	    for (EmployeeLeave leave : leaves) {

	        validateSingleLeaveForDate(
	                leave,
	                timesheetDate,
	                incomingDayType  
	        );
	    }

	}
	
	
	private void validateSingleLeaveForDate(
	        EmployeeLeave leave,
	        LocalDate timesheetDate,
	        DayTypeCode incomingDayType) {

	    LocalDate fromDate = leave.getFromDate();
	    LocalDate toDate   = leave.getToDate();

	    // Single-day leave
	    if (fromDate.equals(toDate)) {
	        validateLeaveDayType(
	                leave.getFromDateDayType(),
	                incomingDayType
	        );
	        return;
	    }

	    // From date
	    if (timesheetDate.equals(fromDate)) {
	        validateLeaveDayType(
	                leave.getFromDateDayType(),
	                incomingDayType
	        );
	        return;
	    }

	    // To date
	    if (timesheetDate.equals(toDate)) {
	        validateLeaveDayType(
	                leave.getToDateDayType(),
	                incomingDayType
	        );
	        return;
	    }

	    // Middle date → always full-day leave
	    if (timesheetDate.isAfter(fromDate)
	            && timesheetDate.isBefore(toDate)) {

	        throw new IllegalArgumentException(
	                "Timesheet cannot be created because employee "
	                        + "is on leave for the selected date."
	                  );

	    }
	}
	
	
	private void validateLeaveDayType(
	        Float leaveDayType,
	        DayTypeCode incomingDayType) {

	    if (Float.valueOf(HALF_DAY).equals(leaveDayType)) {

	        if (incomingDayType != DayTypeCode.HALF_DAY_WORKING) {
	            throw new IllegalArgumentException(
	                    "Only Half-Day timesheet is allowed because "
	                            + "a half-day leave exists on the selected date."
	                      );
	        }
	        return;
	    }

	    // Full-day leave
	    throw new IllegalArgumentException(
	            "Timesheet cannot be created because employee "
	                    + "is on leave for the selected date."
	              );

	}





	
	
	
	

	    
}

