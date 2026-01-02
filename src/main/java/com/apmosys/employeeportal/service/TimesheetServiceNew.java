package com.apmosys.employeeportal.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.ActivityRequestDTONew;
import com.apmosys.employeeportal.dto.ActivityResponseDTONew;
import com.apmosys.employeeportal.dto.CreateTimesheetRequestDTONew;
import com.apmosys.employeeportal.dto.DocumentResponseDTONew;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.ProjectEntryRequestDTONew;
import com.apmosys.employeeportal.dto.ProjectEntryResponseDTONew;
import com.apmosys.employeeportal.dto.TimesheetResponseDTONew;
import com.apmosys.employeeportal.model.Activity;
import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.ClientLocation;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeTimesheetActivitiesMappingNew;
import com.apmosys.employeeportal.model.EmployeeTimesheetsNew;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectTimesheetStatusId;
import com.apmosys.employeeportal.model.ProjectTimesheetStatusNew;
import com.apmosys.employeeportal.model.Team;
import com.apmosys.employeeportal.model.TimesheetActivityMapId;
import com.apmosys.employeeportal.model.TimesheetDocumentDetailsNew;
import com.apmosys.employeeportal.repository.ActivitiesRepository;
import com.apmosys.employeeportal.repository.ClientLocationRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTimesheetsNewRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.ProjectTimesheetStatusNewRepository;
import com.apmosys.employeeportal.repository.TeamRepository;
import com.apmosys.employeeportal.repository.TimesheetActivityMapNewRepository;
import com.apmosys.employeeportal.repository.TimesheetDocumentDetailsNewRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TimesheetServiceNew {

    @Autowired
    private EmployeeTimesheetsNewRepository timesheetsNewRepository;
    
    @Autowired
    private ProjectTimesheetStatusNewRepository projectTimesheetStatusNewRepository;
    
    @Autowired
    private TimesheetActivityMapNewRepository activityMapNewRepository;
    
    @Autowired
    private TimesheetDocumentDetailsNewRepository documentDetailsNewRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private ActivitiesRepository activitiesRepository;
    
    @Autowired
    private ClientLocationRepository clientLocationRepository;
    
    @Autowired
    private TeamRepository teamRepository;
    
    @Autowired
    private LogService logService;
    
    @Autowired
    private HttpServletRequest httpRequest;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * CREATE - Add new timesheet with multiple projects
     */
    @Transactional
    public ServiceResponse createTimesheet(CreateTimesheetRequestDTONew request, 
                                           MultipartFile doc1, 
                                           MultipartFile doc2) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("create_timesheet_new");
        apiLogInfo.setApiUrl("/api/timesheetNew/create");
        apiLogInfo.setLogLevel("INFO");
        
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("empId: ").append(request.getEmpId())
                  .append(", date: ").append(request.getDate())
                  .append(", dayTypeId: ").append(request.getDayTypeId());
        
        try {
            // Validate request
            if (request.getEmpId() == null || request.getDate() == null || request.getDayTypeId() == null) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Missing required fields: empId, date, or dayTypeId");
                return response;
            }

            // Check for duplicate timesheet
            Optional<EmployeeTimesheetsNew> existing = timesheetsNewRepository.findById(
                timesheetsNewRepository.findAll().stream()
                    .filter(t -> t.getEmpId().equals(request.getEmpId()) && 
                                t.getDate().equals(request.getDate()))
                    .findFirst()
                    .map(EmployeeTimesheetsNew::getTimesheetId)
                    .orElse(-1L)
            );
            
            if (existing.isPresent()) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Timesheet already exists for this date");
                return response;
            }

            // Create main timesheet entity
            EmployeeTimesheetsNew timesheet = new EmployeeTimesheetsNew();
            timesheet.setEmpId(request.getEmpId());
            timesheet.setDate(request.getDate());
            timesheet.setDayTypeId(request.getDayTypeId());
            timesheet.setStatus(request.getStatus() != null ? request.getStatus() : 1); // Default: Pending
            // Note: EmployeeTimesheetsNew doesn't have description field - stored in activities
            timesheet.setLeaveTypeMasterId(request.getLeaveTypeMasterId());
            timesheet.setOfficeInTime(request.getOfficeInTime());
            timesheet.setOfficeOutTime(request.getOfficeOutTime());
            timesheet.setTotalWorkingMinutes(request.getTotalWorkingMinutes());
            timesheet.setCreatedBy(request.getEmpId());
            timesheet.setCreatedOn(LocalDateTime.now());
            timesheet.setUpdatedBy(request.getEmpId());
            timesheet.setUpdatedOn(LocalDateTime.now());

            EmployeeTimesheetsNew savedTimesheet = timesheetsNewRepository.save(timesheet);

            // Save project entries and activities
            if (request.getProjectEntries() != null && !request.getProjectEntries().isEmpty()) {
                for (ProjectEntryRequestDTONew projectEntry : request.getProjectEntries()) {
                    // Save project timesheet status
                    ProjectTimesheetStatusId projectStatusId = new ProjectTimesheetStatusId();
                    projectStatusId.setTimesheetId(savedTimesheet.getTimesheetId());
                    projectStatusId.setProjectId(projectEntry.getProjectId().longValue());
                    
                    ProjectTimesheetStatusNew projectStatus = new ProjectTimesheetStatusNew();
                    projectStatus.setId(projectStatusId);
                    projectStatus.setClientInTime(projectEntry.getClientInTime());
                    projectStatus.setClientOutTime(projectEntry.getClientOutTime());
                    projectStatus.setTotalClientWorkingMinutes(projectEntry.getTotalClientWorkingMinutes());
                    projectStatus.setShadowEmpId(projectEntry.getShadowEmpId());
                    projectStatus.setIsNightShift(request.getIsNightShift());
                    
                    // Set client approval status (convert string to integer if needed)
                    if (projectEntry.getClientApprovalStatus() != null) {
                        int approvalStatusId = convertApprovalStatusToId(projectEntry.getClientApprovalStatus());
                        projectStatus.setClientApprovalStatus(approvalStatusId);
                    }
                    
                    projectTimesheetStatusNewRepository.save(projectStatus);

                    // Save activities for this project
                    if (projectEntry.getActivities() != null && !projectEntry.getActivities().isEmpty()) {
                        for (ActivityRequestDTONew activity : projectEntry.getActivities()) {
                            TimesheetActivityMapId activityMapId = new TimesheetActivityMapId();
                            activityMapId.setTimesheetId(savedTimesheet.getTimesheetId());
                            activityMapId.setProjectId(projectEntry.getProjectId().longValue());
                            activityMapId.setActivityId(activity.getActivityId());
                            
                            EmployeeTimesheetActivitiesMappingNew activityMapping = 
                                new EmployeeTimesheetActivitiesMappingNew();
                            activityMapping.setId(activityMapId);
                            activityMapping.setDescription(activity.getDescription());
                            activityMapping.setDurationMinutes(activity.getDurationMinutes() != null ? 
                                activity.getDurationMinutes().shortValue() : null);
                            
                            activityMapNewRepository.save(activityMapping);
                        }
                    }
                }
            }

            // Handle document uploads (per timesheet, max 2)
            if (request.getClientApprovalStatus() != null && 
                !request.getClientApprovalStatus().equalsIgnoreCase("no")) {
                handleDocumentUploads(savedTimesheet.getTimesheetId(), 
                                     request.getEmpId(), 
                                     request.getClientApprovalStatus(), 
                                     doc1, doc2);
            }

            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse("Timesheet created successfully");
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
            
        } catch (Exception e) {
            log.error("Error creating timesheet", e);
            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceResponse("Something went wrong: " + e.getMessage());
            response.setServiceError(e.getMessage());
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            apiLogInfo.setLogLevel("ERROR");
        }
        
        apiLogInfo.setApiRequest(logBuilder.toString());
        logService.logMyInfo(httpRequest, apiLogInfo);
        return response;
    }

    /**
     * READ - Get timesheet by ID
     */
    public ServiceResponse getTimesheetById(Long timesheetId) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setApiUrl("/api/timesheetNew/get/" + timesheetId);
        apiLogInfo.setLogLevel("INFO");
        
        try {
            Optional<EmployeeTimesheetsNew> timesheetOpt = timesheetsNewRepository.findById(timesheetId);
            
            if (!timesheetOpt.isPresent()) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Timesheet not found");
                return response;
            }
            
            EmployeeTimesheetsNew timesheet = timesheetOpt.get();
            TimesheetResponseDTONew dto = mapToResponseDTO(timesheet);
            
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(dto);
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
            
        } catch (Exception e) {
            log.error("Error fetching timesheet", e);
            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceResponse("Something went wrong: " + e.getMessage());
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            apiLogInfo.setLogLevel("ERROR");
        }
        
        logService.logMyInfo(httpRequest, apiLogInfo);
        return response;
    }

    /**
     * READ - Get all timesheets by employee ID and date range
     */
    public ServiceResponse getTimesheetsByEmployee(Long empId, LocalDate startDate, LocalDate endDate) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setApiUrl("/api/timesheetNew/getByEmployee");
        apiLogInfo.setLogLevel("INFO");
        
        try {
            List<EmployeeTimesheetsNew> timesheets = timesheetsNewRepository.findAll().stream()
                .filter(t -> t.getEmpId().equals(empId) && 
                            !t.getDate().isBefore(startDate) && 
                            !t.getDate().isAfter(endDate))
                .collect(Collectors.toList());
            
            List<TimesheetResponseDTONew> dtoList = timesheets.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
            
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(dtoList);
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
            
        } catch (Exception e) {
            log.error("Error fetching timesheets", e);
            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceResponse("Something went wrong: " + e.getMessage());
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            apiLogInfo.setLogLevel("ERROR");
        }
        
        logService.logMyInfo(httpRequest, apiLogInfo);
        return response;
    }

    /**
     * UPDATE - Update existing timesheet
     */
    @Transactional
    public ServiceResponse updateTimesheet(Long timesheetId, 
                                          CreateTimesheetRequestDTONew request,
                                          MultipartFile doc1, 
                                          MultipartFile doc2) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("update_timesheet_new");
        apiLogInfo.setApiUrl("/api/timesheetNew/update/" + timesheetId);
        apiLogInfo.setLogLevel("INFO");
        
        try {
            Optional<EmployeeTimesheetsNew> timesheetOpt = timesheetsNewRepository.findById(timesheetId);
            
            if (!timesheetOpt.isPresent()) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Timesheet not found");
                return response;
            }
            
            EmployeeTimesheetsNew timesheet = timesheetOpt.get();
            
            // Update timesheet fields
            if (request.getDayTypeId() != null) {
                timesheet.setDayTypeId(request.getDayTypeId());
            }
            // Note: Description is stored in activities, not in main timesheet
            if (request.getOfficeInTime() != null) {
                timesheet.setOfficeInTime(request.getOfficeInTime());
            }
            if (request.getOfficeOutTime() != null) {
                timesheet.setOfficeOutTime(request.getOfficeOutTime());
            }
            if (request.getTotalWorkingMinutes() != null) {
                timesheet.setTotalWorkingMinutes(request.getTotalWorkingMinutes());
            }
            timesheet.setUpdatedBy(request.getEmpId());
            timesheet.setUpdatedOn(LocalDateTime.now());
            
            timesheetsNewRepository.save(timesheet);
            
            // Delete existing project entries and activities
            projectTimesheetStatusNewRepository.deleteByTimesheetId(timesheetId);
            // Note: Activity deletion would need a custom query
            
            // Recreate project entries (same as create)
            if (request.getProjectEntries() != null && !request.getProjectEntries().isEmpty()) {
                for (ProjectEntryRequestDTONew projectEntry : request.getProjectEntries()) {
                    ProjectTimesheetStatusId projectStatusId = new ProjectTimesheetStatusId();
                    projectStatusId.setTimesheetId(timesheetId);
                    projectStatusId.setProjectId(projectEntry.getProjectId().longValue());
                    
                    ProjectTimesheetStatusNew projectStatus = new ProjectTimesheetStatusNew();
                    projectStatus.setId(projectStatusId);
                    projectStatus.setClientInTime(projectEntry.getClientInTime());
                    projectStatus.setClientOutTime(projectEntry.getClientOutTime());
                    projectStatus.setTotalClientWorkingMinutes(projectEntry.getTotalClientWorkingMinutes());
                    projectStatus.setShadowEmpId(projectEntry.getShadowEmpId());
                    
                    if (projectEntry.getClientApprovalStatus() != null) {
                        int approvalStatusId = convertApprovalStatusToId(projectEntry.getClientApprovalStatus());
                        projectStatus.setClientApprovalStatus(approvalStatusId);
                    }
                    
                    projectTimesheetStatusNewRepository.save(projectStatus);
                    
                    // Recreate activities
                    if (projectEntry.getActivities() != null) {
                        for (ActivityRequestDTONew activity : projectEntry.getActivities()) {
                            TimesheetActivityMapId activityMapId = new TimesheetActivityMapId();
                            activityMapId.setTimesheetId(timesheetId);
                            activityMapId.setProjectId(projectEntry.getProjectId().longValue());
                            activityMapId.setActivityId(activity.getActivityId());
                            
                            EmployeeTimesheetActivitiesMappingNew activityMapping = 
                                new EmployeeTimesheetActivitiesMappingNew();
                            activityMapping.setId(activityMapId);
                            activityMapping.setDescription(activity.getDescription());
                            activityMapping.setDurationMinutes(activity.getDurationMinutes() != null ? 
                                activity.getDurationMinutes().shortValue() : null);
                            
                            activityMapNewRepository.save(activityMapping);
                        }
                    }
                }
            }
            
            // Handle document updates
            if (request.getClientApprovalStatus() != null && 
                !request.getClientApprovalStatus().equalsIgnoreCase("no")) {
                // Delete existing documents and re-upload
                documentDetailsNewRepository.findAll().stream()
                    .filter(doc -> doc.getTimesheetId().equals(timesheetId))
                    .forEach(doc -> documentDetailsNewRepository.delete(doc));
                
                handleDocumentUploads(timesheetId, request.getEmpId(), 
                                     request.getClientApprovalStatus(), doc1, doc2);
            }
            
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse("Timesheet updated successfully");
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
            
        } catch (Exception e) {
            log.error("Error updating timesheet", e);
            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceResponse("Something went wrong: " + e.getMessage());
            response.setServiceError(e.getMessage());
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            apiLogInfo.setLogLevel("ERROR");
        }
        
        logService.logMyInfo(httpRequest, apiLogInfo);
        return response;
    }

    /**
     * DELETE - Delete timesheet by ID
     */
    @Transactional
    public ServiceResponse deleteTimesheet(Long timesheetId) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("delete_timesheet_new");
        apiLogInfo.setApiUrl("/api/timesheetNew/delete/" + timesheetId);
        apiLogInfo.setLogLevel("INFO");
        
        try {
            Optional<EmployeeTimesheetsNew> timesheetOpt = timesheetsNewRepository.findById(timesheetId);
            
            if (!timesheetOpt.isPresent()) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Timesheet not found");
                return response;
            }
            
            // Delete related records first (cascade delete)
            projectTimesheetStatusNewRepository.deleteByTimesheetId(timesheetId);
            
            // Delete activities (would need custom query)
            // For now, we'll rely on database cascade if configured
            
            // Delete documents
            documentDetailsNewRepository.findAll().stream()
                .filter(doc -> doc.getTimesheetId().equals(timesheetId))
                .forEach(doc -> documentDetailsNewRepository.delete(doc));
            
            // Delete main timesheet
            timesheetsNewRepository.deleteById(timesheetId);
            
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse("Timesheet deleted successfully");
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
            
        } catch (Exception e) {
            log.error("Error deleting timesheet", e);
            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceResponse("Something went wrong: " + e.getMessage());
            response.setServiceError(e.getMessage());
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            apiLogInfo.setLogLevel("ERROR");
        }
        
        logService.logMyInfo(httpRequest, apiLogInfo);
        return response;
    }

    // ========== HELPER METHODS ==========

    private TimesheetResponseDTONew mapToResponseDTO(EmployeeTimesheetsNew timesheet) {
        TimesheetResponseDTONew dto = new TimesheetResponseDTONew();
        dto.setTimesheetId(timesheet.getTimesheetId());
        dto.setEmpId(timesheet.getEmpId());
        dto.setDate(timesheet.getDate());
        dto.setDayTypeId(timesheet.getDayTypeId());
        dto.setStatus(timesheet.getStatus());
        // Description is derived from activities
        dto.setDescription(null); // Can be built from activities if needed
        dto.setLeaveTypeMasterId(timesheet.getLeaveTypeMasterId());
        dto.setOfficeInTime(timesheet.getOfficeInTime());
        dto.setOfficeOutTime(timesheet.getOfficeOutTime());
        dto.setTotalWorkingMinutes(timesheet.getTotalWorkingMinutes());
        if (timesheet.getTotalWorkingMinutes() != null) {
            dto.setTotalWorkingHours(timesheet.getTotalWorkingMinutes() / 60.0);
        }
        dto.setCreatedBy(timesheet.getCreatedBy());
        dto.setCreatedOn(timesheet.getCreatedOn());
        dto.setUpdatedBy(timesheet.getUpdatedBy());
        dto.setUpdatedOn(timesheet.getUpdatedOn());
        
        // Get employee name
        Optional<Employee> employeeOpt = employeeRepository.findById(timesheet.getEmpId());
        if (employeeOpt.isPresent()) {
            dto.setEmployeeName(employeeOpt.get().getName());
        }
        
        // Get project entries
        List<ProjectTimesheetStatusNew> projectStatuses = 
            projectTimesheetStatusNewRepository.findByTimesheetId(timesheet.getTimesheetId());
        
        List<ProjectEntryResponseDTONew> projectEntries = new ArrayList<>();
        for (ProjectTimesheetStatusNew projectStatus : projectStatuses) {
            ProjectEntryResponseDTONew projectEntry = new ProjectEntryResponseDTONew();
            projectEntry.setProjectId(projectStatus.getId().getProjectId());
            
            Optional<Project> projectOpt = projectRepository.findById(
                projectStatus.getId().getProjectId().intValue());
            if (projectOpt.isPresent()) {
                projectEntry.setProjectName(projectOpt.get().getProjectName());
            }
            
            projectEntry.setClientInTime(projectStatus.getClientInTime());
            projectEntry.setClientOutTime(projectStatus.getClientOutTime());
            projectEntry.setTotalClientWorkingMinutes(projectStatus.getTotalClientWorkingMinutes());
            if (projectStatus.getTotalClientWorkingMinutes() != null) {
                projectEntry.setTotalClientWorkingHours(projectStatus.getTotalClientWorkingMinutes() / 60.0);
            }
            projectEntry.setShadowEmpId(projectStatus.getShadowEmpId());
            projectEntry.setIsShadowTimesheet(projectStatus.getShadowEmpId() != null);
            
            if (projectStatus.getShadowEmpId() != null) {
                Optional<Employee> shadowEmpOpt = employeeRepository.findById(projectStatus.getShadowEmpId());
                if (shadowEmpOpt.isPresent()) {
                    projectEntry.setShadowEmployeeName(shadowEmpOpt.get().getName());
                }
            }
            
            // Get activities for this project
            List<EmployeeTimesheetActivitiesMappingNew> activities = 
                activityMapNewRepository.findAll().stream()
                    .filter(a -> a.getId().getTimesheetId().equals(timesheet.getTimesheetId()) &&
                                a.getId().getProjectId().equals(projectStatus.getId().getProjectId()))
                    .collect(Collectors.toList());
            
            List<ActivityResponseDTONew> activityDTOs = new ArrayList<>();
            for (EmployeeTimesheetActivitiesMappingNew activity : activities) {
                ActivityResponseDTONew activityDTO = new ActivityResponseDTONew();
                activityDTO.setActivityId(activity.getId().getActivityId());
                activityDTO.setDescription(activity.getDescription());
                activityDTO.setDurationMinutes(activity.getDurationMinutes() != null ? 
                    activity.getDurationMinutes().intValue() : null);
                if (activity.getDurationMinutes() != null) {
                    activityDTO.setDurationHours(activity.getDurationMinutes() / 60.0);
                }
                
                Optional<Activity> activityOpt = activitiesRepository.findById(activity.getId().getActivityId());
                if (activityOpt.isPresent()) {
                    activityDTO.setActivityName(activityOpt.get().getActivity());
                }
                
                activityDTOs.add(activityDTO);
            }
            projectEntry.setActivities(activityDTOs);
            projectEntries.add(projectEntry);
        }
        dto.setProjectEntries(projectEntries);
        
        // Get documents
        List<TimesheetDocumentDetailsNew> documents = documentDetailsNewRepository.findAll().stream()
            .filter(doc -> doc.getTimesheetId().equals(timesheet.getTimesheetId()))
            .collect(Collectors.toList());
        
        List<DocumentResponseDTONew> documentDTOs = documents.stream()
            .map(this::mapDocumentToDTO)
            .collect(Collectors.toList());
        dto.setDocuments(documentDTOs);
        
        return dto;
    }

    private DocumentResponseDTONew mapDocumentToDTO(TimesheetDocumentDetailsNew doc) {
        DocumentResponseDTONew dto = new DocumentResponseDTONew();
        dto.setDocId(doc.getDocId());
        dto.setTimesheetId(doc.getTimesheetId());
        dto.setFileUrl(doc.getFileUrl());
        dto.setDocName(doc.getDocName());
        dto.setMimeTypeId(doc.getMimeTypeId());
        dto.setClientApprovalStatusId(doc.getClientApprovalStatusId());
        dto.setActive(doc.getActive());
        dto.setFinalFlag(doc.getFinalFlag());
        dto.setCreatedBy(doc.getCreatedBy());
        dto.setCreatedOn(doc.getCreatedOn());
        dto.setUpdatedBy(doc.getUpdatedBy());
        dto.setUpdatedOn(doc.getUpdatedOn());
        return dto;
    }

    private void handleDocumentUploads(Long timesheetId, 
                                      Long empId, 
                                      String approvalStatus,
                                      MultipartFile doc1, 
                                      MultipartFile doc2) throws IOException {
        if (approvalStatus == null || approvalStatus.equalsIgnoreCase("no")) {
            return;
        }
        
        // Filled document (required for both pending and approved)
        if (doc1 != null && !doc1.isEmpty()) {
            saveDocument(timesheetId, empId, doc1, false, approvalStatus);
        }
        
        // Approved document (required only for approved status)
        if ("approved".equalsIgnoreCase(approvalStatus) && doc2 != null && !doc2.isEmpty()) {
            saveDocument(timesheetId, empId, doc2, true, approvalStatus);
        }
    }

    private void saveDocument(Long timesheetId, 
                             Long empId, 
                             MultipartFile file, 
                             boolean isFinal,
                             String approvalStatus) throws IOException {
        TimesheetDocumentDetailsNew doc = new TimesheetDocumentDetailsNew();
        doc.setTimesheetId(timesheetId);
        doc.setDocName(file.getOriginalFilename());
        doc.setMimeTypeId(getMimeTypeId(file.getContentType()));
        doc.setClientApprovalStatusId(convertApprovalStatusToId(approvalStatus));
        doc.setActive(true);
        doc.setFinalFlag(isFinal);
        doc.setCreatedBy(empId);
        doc.setCreatedOn(LocalDateTime.now());
        doc.setUpdatedBy(empId);
        doc.setUpdatedOn(LocalDateTime.now());
        
        // Save file (using Base64 encoding or file storage - adjust as per your system)
        byte[] fileBytes = file.getBytes();
        String base64File = Base64.getEncoder().encodeToString(fileBytes);
        doc.setFileUrl(base64File); // Or save to file system and store path
        
        documentDetailsNewRepository.save(doc);
    }

    private int convertApprovalStatusToId(String approvalStatus) {
        if (approvalStatus == null) return 0;
        switch (approvalStatus.toLowerCase()) {
            case "no":
            case "not filled":
                return 1;
            case "pending":
            case "filled":
                return 2;
            case "approved":
                return 3;
            default:
                return 0;
        }
    }

    private Integer getMimeTypeId(String contentType) {
        if (contentType == null) return null;
        if (contentType.contains("pdf")) return 1;
        if (contentType.contains("image/jpeg") || contentType.contains("image/jpg")) return 2;
        if (contentType.contains("image/png")) return 3;
        return null;
    }
}

