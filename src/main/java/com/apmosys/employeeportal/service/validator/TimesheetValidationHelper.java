package com.apmosys.employeeportal.service.validator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.TimesheetDTO_new.ActivityTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.EmployeeTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.LocationSessionDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.ProjectTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.TimesheetDocumentDataDTO;
import com.apmosys.employeeportal.model.Activity;
import com.apmosys.employeeportal.model.DayTypeMasterNew;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.repository.ActivitiesRepository;
import com.apmosys.employeeportal.repository.DayTypeMasterNewRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.EmployeeTimesheetsNewRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;

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
    
    @Autowired
    private DayTypeMasterNewRepository dayTypeMasterNewRepository;
    
    @Autowired
    private EmployeeTimesheetsNewRepository employeeTimesheetsNewRepository;

        public void validateForCreate(EmployeeTimesheetDTO empDTO) {
            validateEmployeeTimesheet(empDTO);
            validateLocationSessions(empDTO);
            validateDocuments(empDTO.getDocumentData(), empDTO);
        }

        /* =====================================================
           EMPLOYEE LEVEL VALIDATION
           ===================================================== */

        private void validateEmployeeTimesheet(EmployeeTimesheetDTO dto) {

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

            if (dto.getDayTypeId() == null && dto.getDayType() == null) {
                throw new IllegalArgumentException("Day type is required");
            }

            boolean isWorkingDay = isWorkingDay(dto);

            if (isWorkingDay) {
                if (dto.getWorkCheckIn() == null || dto.getWorkCheckOut() == null) {
                    throw new IllegalArgumentException(
                            "workCheckIn and workCheckOut are mandatory for working days");
                }

                if (!dto.getWorkCheckOut().isAfter(dto.getWorkCheckIn())) {
                    throw new IllegalArgumentException(
                            "workCheckOut must be after workCheckIn");
                }
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


        /* =====================================================
           LOCATION SESSION VALIDATION
           ===================================================== */

        private void validateLocationSessions(EmployeeTimesheetDTO empDTO) {

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

        private void validateLocationSession(LocationSessionDTO location,
                                             EmployeeTimesheetDTO empDTO) {

            if (location.getWorkLocationType() == null &&
                location.getWorkLocationTypeId() == null) {
                throw new IllegalArgumentException("Work location type is required");
            }

            if (location.getLocationInTime() == null ||
                location.getLocationOutTime() == null) {
                throw new IllegalArgumentException(
                        "Location inTime and outTime are required");
            }

            if (location.getProjects() == null || location.getProjects().isEmpty()) {
                throw new IllegalArgumentException(
                        "Each location session must contain at least one project");
            }

            for (ProjectTimesheetDTO project : location.getProjects()) {
                validateProject(project, empDTO);
            }
        }

        /* =====================================================
           PROJECT VALIDATION
           ===================================================== */

        private void validateProject(ProjectTimesheetDTO project,
                                     EmployeeTimesheetDTO empDTO) {

            if (project.getProjectId() == null) {
                throw new IllegalArgumentException("Project ID is required");
            }

            if (project.getStatus() == null) {
                throw new IllegalArgumentException(
                        "Project status is required (Pending/Approved/Rejected)");
            }

            boolean isWorkingDay = isWorkingDay(empDTO);

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

        /* =====================================================
           DOCUMENT VALIDATION
           ===================================================== */

        /**
         * Validate document requirements for new contract.
         * Applies only for:
         *  - Working days
         *  - Shadow timesheets
         *  - Projects where client-side document is mandatory
         */
        private void validateDocuments(List<TimesheetDocumentDataDTO> docs,
                                       EmployeeTimesheetDTO empDTO) {

            if (empDTO == null) {
                return;
            }

            // 1️⃣ Skip validation for non-working day types
            if (isNonWorkingDay(empDTO.getDayType())) {
                return;
            }

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
                    if (projectId == null) continue;

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
                                            projectId.equals(doc.getProjectId()) &&
                                            doc.getDocName() != null &&
                                            doc.getFinalFlag() != null
                                    );

                    if (!documentPresentForProject) {
                        throw new IllegalArgumentException(
                                "Client-side ID is mandatory. Please upload required documents for projectId="
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

            

            if (documents == null || documents.isEmpty()) {
                throw new IllegalArgumentException(
                        "Client-side document is mandatory. Please upload required documents.");
            }

            // 3️⃣ Group uploaded files by projectId (parsed from filename)
            Map<Long, List<MultipartFile>> filesByProject =
                    groupFilesByProjectId(documents);

            // 4️⃣ Validate project-wise
            for (LocationSessionDTO location : empDTO.getLocationSessions()) {
                if (location.getProjects() == null) continue;

                for (ProjectTimesheetDTO project : location.getProjects()) {

                    Integer projectId = project.getProjectId();
                    if (projectId == null) continue;

                    Boolean isClientIdMandatory =
                            projectRepository.getClientSideIdMandatory(projectId);

                    if (!Boolean.TRUE.equals(isClientIdMandatory)) {
                        continue;
                    }

                    List<MultipartFile> projectFiles =
                            filesByProject.getOrDefault(projectId, List.of());

                    // 5️⃣ Enforce min / max rule
                    if (projectFiles.isEmpty()) {
                        throw new IllegalArgumentException(
                                "Filled document is mandatory for projectId=" + projectId);
                    }

                    if (projectFiles.size() > 2) {
                        throw new IllegalArgumentException(
                                "Maximum 2 documents (filled + approved) allowed for projectId="
                                        + projectId);
                    }
                    
                    if(project.getClientApprovalStatus()==1 && projectFiles.size()==2) {
                    	throw new IllegalArgumentException(
                                "2 documents (filled + approved) are required for projectId="
                                        + projectId);
                    }

                    boolean filledPresent = false;

                    // 6️  Validate file naming & types
                    for (MultipartFile file : projectFiles) {

                        String fileName = file.getOriginalFilename();
                        if (fileName == null) {
                            throw new IllegalArgumentException(
                                    "Invalid document name for projectId=" + projectId);
                        }

                        String lowerName = fileName.toLowerCase();

                        if (lowerName.contains("_filled_")) {
                            filledPresent = true;
                        } else if (lowerName.contains("_approved_")) {
                            // approved doc → optional
                        } else {
                            throw new IllegalArgumentException(
                                    "Invalid document name format for projectId=" + projectId +
                                    ". Expected: projectId_filled_xxx or projectId_approved_xxx");
                        }
                    }

                    // 7️ Filled doc is mandatory
                    if (!filledPresent) {
                        throw new IllegalArgumentException(
                                "Filled document is mandatory for projectId=" + projectId);
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

            // 1️⃣ Total work duration (in minutes)
            long totalWorkMinutes = java.time.Duration
                    .between(empDTO.getWorkCheckIn(), empDTO.getWorkCheckOut())
                    .toMinutes();

            if (totalWorkMinutes <= 0) {
                throw new IllegalArgumentException(
                        "Invalid workCheckIn/workCheckOut duration");
            }

            // 2️⃣ Sum of all location session durations
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

            // 3️⃣ Sum of all activity durations
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


        /* =====================================================
           HELPERS
           ===================================================== */

        private boolean isWorkingDay(EmployeeTimesheetDTO dto) {
            if (dto.getDayType() != null) {
                return "Working".equalsIgnoreCase(dto.getDayType());
            }
            // fallback – based on dayTypeId (customize later)
            return true;
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
     * Validate that at least one project is required for working days.
     * NEW CONTRACT: Projects come from locationSessions
     * 
     * @param empDTO Employee timesheet DTO from new contract
     * @param projectDTOs List of project DTOs (flattened from locationSessions)
     * @throws IllegalArgumentException if validation fails
     */
    public void validateProjectsRequiredForWorkingDays(
             EmployeeTimesheetDTO empDTO) {
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
        
        // Non-working days: Week Off, Public Holiday, Leave, Client Holiday
        boolean isNonWorkingDay = "Week Off".equalsIgnoreCase(dayType.getDayType()) ||
                                 "Public Holiday".equalsIgnoreCase(dayType.getDayType()) ||
                                 "Leave".equalsIgnoreCase(dayType.getDayType()) ||
                                 "Client Holiday".equalsIgnoreCase(dayType.getDayType());
        
       
        // For non-working days, projects are optional
        // No validation needed for non-working days
    }
    
    /**
     * Validate that timesheet does not already exist for employee and date.
     * 
     * @param empId Employee ID
     * @param date Date to check
     * @throws IllegalArgumentException if timesheet already exists
     */
    public void validateTimesheetNotExists(Long empId, LocalDate date) {
        if (empId == null || date == null) {
            throw new IllegalArgumentException("Employee ID and Date are required");
        }
        
        boolean exists = employeeTimesheetsNewRepository.findByEmpIdAndDateNew(empId, date).isPresent();
        
        if (exists) {
            throw new IllegalArgumentException("Timesheet already exists for employee " + empId + " on date " + date);
        }
    }
    
    
}

