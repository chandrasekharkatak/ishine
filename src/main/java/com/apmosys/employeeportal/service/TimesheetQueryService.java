package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.ActivityDTO;
import com.apmosys.employeeportal.dto.FilteredTimesheetDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDocumentDetailsDTO;
import com.apmosys.employeeportal.model.TimesheetDocumentDetails;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.TimesheetDocumentDetailsRepository;
import com.apmosys.employeeportal.repository.TimesheetsRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing timesheet query operations.
 * 
 * This service encapsulates all query-related operations for timesheets:
 * - Employee timesheet queries
 * - Team and reportee queries
 * - Project and activity queries
 * - Complex filtering and pagination
 * - Report generation queries
 * 
 * @author Timesheet Refactoring - Phase 6
 */
@Slf4j
@Service
public class TimesheetQueryService {

    @Autowired
    private TimesheetsRepository timesheetsRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private EmployeeTeamMapRepository employeeTeamMapRepository;
    
    @Autowired
    private TimesheetDocumentDetailsRepository timesheetDocumentDetailsRepository;
    
    @Autowired
    private LogService logService;
    
    @Autowired
    private HttpServletRequest httpRequest;
    
    @Autowired
    private EntityManager entityManager;

    /**
     * Gets all timesheets by employee ID.
     * 
     * @param timesheetDTO Contains empId, startDate, endDate
     * @return ServiceResponse with timesheet list
     */
    public ServiceResponse getAllMyTimesheetsByEmpId(TimesheetDTO timesheetDTO) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("add_timesheet");
        apiLogInfo.setApiUrl("/api/getAllMyTimesheetsByEmpId");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("startDate : ").append(timesheetDTO.getStartDate())
                  .append(" ,endDate : ").append(timesheetDTO.getEndDate());
        
        try {
            LocalDate start = LocalDate.parse(timesheetDTO.getStartDate());
            LocalDate end = LocalDate.parse(timesheetDTO.getEndDate());
            
            List<Object[]> timesheetList = timesheetsRepository
                    .getAllMyTimesheetsOLD(timesheetDTO.getEmpId(), start, end);

            Optional.ofNullable(timesheetList).ifPresentOrElse((list) -> {
                if (list.isEmpty()) {
                    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                    response.setServiceResponse("Timesheet list is empty");
                    apiLogInfo.setApiResponse("Timesheet list is empty");
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                } else {
                    List<TimesheetDTO> dtoList = buildTimesheetDTOList(list);
                    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                    response.setServiceResponse(dtoList);
                    apiLogInfo.setApiResponse("dtoList : " + dtoList);
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
                }
            }, () -> {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Timesheet list is null");
                apiLogInfo.setApiResponse("Timesheet list is null");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            });
        } catch (Exception e) {
            e.printStackTrace();
            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceResponse("Something Went Wrong.");
            response.setServiceError(e.getMessage());
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            apiLogInfo.setLogLevel("ERROR");
        }
        
        apiLogInfo.setApiRequest(logBuilder.toString());
        logService.logMyInfo(httpRequest, apiLogInfo);
        return response;
    }

    /**
     * Gets all team timesheets.
     * 
     * @param timesheetDTO Contains createdBy, startDate, endDate
     * @return ServiceResponse with timesheet list
     */
    public ServiceResponse getAllMyTeamTimesheets(TimesheetDTO timesheetDTO) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setApiUrl("/api/getAllMyTeamTimesheets");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("startDate : ").append(timesheetDTO.getStartDate())
                  .append(" ,endDate : ").append(timesheetDTO.getEndDate());
        
        try {
            LocalDate start = LocalDate.parse(timesheetDTO.getStartDate());
            LocalDate end = LocalDate.parse(timesheetDTO.getEndDate());
            
            List<Object[]> timesheetList = timesheetsRepository
                    .getAllMyTeamTimesheetsOLD(timesheetDTO.getCreatedBy(), start, end);

            Optional.ofNullable(timesheetList).ifPresentOrElse((list) -> {
                if (list.isEmpty()) {
                    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                    response.setServiceResponse("Timesheet list is empty");
                    apiLogInfo.setApiResponse("Timesheet list is empty");
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                } else {
                    List<TimesheetDTO> dtoList = buildTimesheetDTOList(list);
                    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                    response.setServiceResponse(dtoList);
                    apiLogInfo.setApiResponse("dtoList : " + dtoList);
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
                }
            }, () -> {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Timesheet list is null");
                apiLogInfo.setApiResponse("Timesheet list is null");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            });
        } catch (Exception e) {
            e.printStackTrace();
            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceResponse("Something Went Wrong.");
            response.setServiceError(e.getMessage());
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            apiLogInfo.setLogLevel("ERROR");
        }
        
        apiLogInfo.setApiRequest(logBuilder.toString());
        logService.logMyInfo(httpRequest, apiLogInfo);
        return response;
    }

    /**
     * Gets my reportees.
     * 
     * @param timesheetDTO Contains managerId
     * @return ServiceResponse with reportee list
     */
    public ServiceResponse getMyReportees(TimesheetDTO timesheetDTO) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("view_my_teams_timesheets");
        apiLogInfo.setApiUrl("/api/getMyReportees");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("managerId : ").append(timesheetDTO.getManagerId())
                  .append(" ,startDate : ").append(timesheetDTO.getStartDate())
                  .append(" ,endDate : ").append(timesheetDTO.getEndDate());
        
        try {
            List<TimesheetDTO> objectList = timesheetsRepository.getMyReportees(
                    timesheetDTO.getManagerId());

            Optional.ofNullable(objectList).ifPresentOrElse((list) -> {
                if (list.isEmpty()) {
                    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                    response.setServiceResponse("No reportees found. List is empty.");
                    apiLogInfo.setApiResponse("No reportees found");
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                } else {
                    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                    response.setServiceResponse(objectList);
                    apiLogInfo.setApiResponse("dtoList : " + objectList);
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
                }
            }, () -> {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("No reportees found. List is null.");
                apiLogInfo.setApiResponse("No reportees found. List is null.");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            });
        } catch (Exception e) {
            e.printStackTrace();
            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceResponse("Something Went Wrong.");
            response.setServiceError(e.getMessage());
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            apiLogInfo.setLogLevel("ERROR");
        }
        
        apiLogInfo.setApiRequest(logBuilder.toString());
        logService.logMyInfo(httpRequest, apiLogInfo);
        return response;
    }

    /**
     * Gets my reportees approved timesheets.
     * 
     * @param timesheetDTO Contains managerId, startDate, endDate
     * @return ServiceResponse with approved timesheet list
     */
    public ServiceResponse getMyReporteesApprovedTimesheets(TimesheetDTO timesheetDTO) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("view_my_teams_timesheets");
        apiLogInfo.setApiUrl("/api/getMyReporteesApprovedTimesheets");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("managerId : ").append(timesheetDTO.getManagerId())
                  .append(" ,startDate : ").append(timesheetDTO.getStartDate())
                  .append(" ,endDate : ").append(timesheetDTO.getEndDate());
        
        try {
            LocalDate start = LocalDate.parse(timesheetDTO.getStartDate());
            LocalDate end = LocalDate.parse(timesheetDTO.getEndDate());
            
            List<Object[]> objectList = timesheetsRepository.getMyReporteesApprovedTimesheetsOLD(
                    timesheetDTO.getManagerId(), start, end);

            Optional.ofNullable(objectList).ifPresentOrElse((list) -> {
                if (list.isEmpty()) {
                    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                    response.setServiceResponse("No timesheets found. List is empty.");
                    apiLogInfo.setApiResponse("No timesheets found");
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                } else {
                    List<TimesheetDTO> dtoList = buildApprovedTimesheetDTOList(list);
                    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                    response.setServiceResponse(dtoList);
                    apiLogInfo.setApiResponse("dtoList : " + dtoList);
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
                }
            }, () -> {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("No timesheets found. List is null.");
                apiLogInfo.setApiResponse("No timesheets found. List is null.");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            });
        } catch (Exception e) {
            e.printStackTrace();
            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceResponse("Something Went Wrong.");
            response.setServiceError(e.getMessage());
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            apiLogInfo.setLogLevel("ERROR");
        }
        
        apiLogInfo.setApiRequest(logBuilder.toString());
        logService.logMyInfo(httpRequest, apiLogInfo);
        return response;
    }

    /**
     * Gets all projects by employee ID.
     * 
     * @param timesheetDTO Contains empId
     * @return ServiceResponse with project list
     */
    public ServiceResponse getAllProjectsByEmpId(TimesheetDTO timesheetDTO) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("add_timesheet");
        apiLogInfo.setApiUrl("/api/getAllProjectsByEmpId");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("empId : ").append(timesheetDTO.getEmpId());

        try {
            List<Object[]> projectList = employeeTeamMapRepository.findProjectsByTeamId(timesheetDTO.getEmpId());
            List<TimesheetDTO> listDto = new ArrayList<TimesheetDTO>();

            if (!projectList.isEmpty()) {
                for (Object[] object : projectList) {
                    String projectType = object[8] != null ? object[8].toString() : "";
                    String poEndDateStr = object[9] != null ? object[9].toString() : null;

                    if ("TNM".equalsIgnoreCase(projectType) || "Fixed Cost".equalsIgnoreCase(projectType)) {
                        if (poEndDateStr != null) {
                            String onlyDateStr = poEndDateStr.contains("T") ? poEndDateStr.split("T")[0] : poEndDateStr;
                            LocalDate poEndDate = LocalDate.parse(onlyDateStr);
                            LocalDate currentDate = LocalDate.now();
                        }
                    }

                    TimesheetDTO timesheetDto = new TimesheetDTO();
                    timesheetDto.setClientId(object[0] != null ? Integer.parseInt(object[0].toString()) : null);
                    timesheetDto.setClientName(object[1] != null ? object[1].toString() : null);
                    timesheetDto.setClientLocationId(object[2] != null ? Integer.parseInt(object[2].toString()) : null);
                    timesheetDto.setClientLocation(object[3] != null ? object[3].toString() : null);
                    timesheetDto.setProjectId(object[4] != null ? Integer.parseInt(object[4].toString()) : null);
                    timesheetDto.setProjectName(object[5] != null ? object[5].toString() : null);
                    timesheetDto.setTeamName(object[6] != null ? object[6].toString() : null);
                    timesheetDto.setTeamId(object[7] != null ? Long.parseLong(object[7].toString()) : null);
                    listDto.add(timesheetDto);
                }

                if (!listDto.isEmpty()) {
                    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                    response.setServiceResponse(listDto);
                    apiLogInfo.setApiResponse("listDto : " + listDto);
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
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
        logService.logMyInfo(httpRequest, apiLogInfo);
        return response;
    }

    /**
     * Gets all activities by project ID and employee ID.
     * 
     * @param timesheetDTO Contains teamId, empId
     * @return ServiceResponse with activity list
     */
    public ServiceResponse getAllActivitiesByProjectIdandEmpId(TimesheetDTO timesheetDTO) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("add_timesheet");
        apiLogInfo.setApiUrl("/api/getAllActivitiesByProjectIdandEmpId");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("projectId : ").append(timesheetDTO.getProjectId())
                  .append("empId : ").append(timesheetDTO.getEmpId());

        try {
            List<Object[]> objectList = projectRepository
                    .getActivitiesByTeamIdAndEmployeeId(timesheetDTO.getTeamId(), timesheetDTO.getEmpId());

            Optional.ofNullable(objectList).ifPresentOrElse((list) -> {
                if (list.isEmpty()) {
                    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                    response.setServiceResponse("No activities found.Activity list is empty");
                    apiLogInfo.setApiResponse("No activities found.Activity list is empty");
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                } else {
                    List<ActivityDTO> dtoList = new ArrayList<ActivityDTO>();

                    list.forEach((object) -> {
                        String[] employeeRoleInTeam = (object[4] != null ? object[4].toString() : null).split(",");
                        boolean contains = java.util.Arrays.stream(employeeRoleInTeam)
                                .anyMatch((object[3] != null ? object[3].toString() : null)::equals);

                        if (contains) {
                            ActivityDTO dto = new ActivityDTO();
                            dto.setActivityId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
                            dto.setActivity(object[1] != null ? object[1].toString() : null);
                            dto.setTeamId(object[2] != null ? Long.parseLong(object[2].toString()) : null);
                            dto.setDepartmentList(object[5] != null ? object[5].toString().split(",") : null);
                            dtoList.add(dto);
                        }
                    });

                    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                    response.setServiceResponse(dtoList);
                    apiLogInfo.setApiResponse("dtoList : " + dtoList);
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
                }
            }, () -> {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("No activities found.Activity list is null");
                apiLogInfo.setApiResponse("No activities found.Activity list is null");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            });
        } catch (Exception e) {
            e.printStackTrace();
            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceResponse("Something Went Wrong.");
            response.setServiceError(e.getMessage());
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            apiLogInfo.setLogLevel("ERROR");
        }
        
        apiLogInfo.setApiRequest(logBuilder.toString());
        logService.logMyInfo(httpRequest, apiLogInfo);
        return response;
    }

    /**
     * Gets all or department-wise employee timesheet report.
     * 
     * @param filteredTimesheetDTO Contains deptId, startDate, endDate
     * @return ServiceResponse with timesheet report
     */
    public ServiceResponse getAllOrDeptWiseEmployeeTimesheetReport(FilteredTimesheetDTO filteredTimesheetDTO) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("fetch_timesheets");
        apiLogInfo.setApiUrl("/api/getAllOrDeptWiseEmployeeTimesheetReport");
        apiLogInfo.setLogLevel("INFO");

        String deptId = filteredTimesheetDTO.getDeptId();
        String startDate = filteredTimesheetDTO.getStartDate();
        String endDate = filteredTimesheetDTO.getEndDate();

        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("Requested Department ID: ").append(deptId);

        try {
            List<Object[]> timesheetList;
            if ("all".equalsIgnoreCase(deptId)) {
                timesheetList = timesheetsRepository.getAllEmployeeTimesheetsBetweenDates(startDate, endDate);
            } else {
                timesheetList = timesheetsRepository.getTimesheetsByDepartmentAndDateRange(
                        Long.parseLong(deptId), startDate, endDate);
            }

            if (timesheetList == null || timesheetList.isEmpty()) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("No timesheets found.");
                apiLogInfo.setApiResponse("No timesheets found.");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            } else {
                List<TimesheetDTO> dtoList = timesheetList.stream()
                        .map(this::mapToTimesheetDTO)
                        .collect(Collectors.toList());

                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(dtoList);
                apiLogInfo.setApiResponse("Timesheet data retrieved.");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceResponse("Something went wrong.");
            response.setServiceError(e.getMessage());
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            apiLogInfo.setLogLevel("ERROR");
        }

        apiLogInfo.setApiRequest(logBuilder.toString());
        logService.logMyInfo(httpRequest, apiLogInfo);
        return response;
    }

    /**
     * Gets active projects by employee ID.
     * 
     * @param empId Employee ID
     * @return ServiceResponse with active project list
     */
    public ServiceResponse getActiveProjectsByEmpId(Long empId) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setApiUrl("/api/getActiveProjectsByEmpId");
        apiLogInfo.setLogLevel("INFO");

        try {
            List<ProjectDTO> activeProjectList = timesheetsRepository.getActiveProjectsByEmpId(empId);

            if (activeProjectList.isEmpty()) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Please contact to the RMG team to provide you active project mapping!");
                response.setServiceMessage("Employee has no active project mapping ! For EmpId: " + empId);
                apiLogInfo.setApiResponse("Employee has no active project mapping ! For EmpId: " + empId);
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                logService.logMyInfo(httpRequest, apiLogInfo);
                return response;
            }

            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(activeProjectList);
            response.setServiceMessage("Project List fetched successfully!");
            apiLogInfo.setApiResponse("Project list where employee has active = 1 in Employee Team Mapping table fetched successfully!");
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
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

    /**
     * Fetches employment ID by employee ID.
     * 
     * @param empId Employee ID
     * @return ServiceResponse with employment ID
     */
    public ServiceResponse fetchEmploymentIdByEmpId(Long empId) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setApiUrl("/api/fetchEmploymentIdByEmpId");
        apiLogInfo.setLogLevel("INFO");

        try {
            String clientSideId = employeeRepository.fetchEmploymentIdByEmpId(empId);

            if (clientSideId == null || clientSideId.isEmpty()) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("No Emp Id fetched!");
                response.setServiceMessage("No Emp Id fetched for the employee with EmpId: " + empId);
                apiLogInfo.setApiResponse("No Emp Id fetched for the employee with EmpId: " + empId);
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                logService.logMyInfo(httpRequest, apiLogInfo);
                return response;
            }

            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(clientSideId);
            response.setServiceMessage("Emp Id fetched successfully!");
            apiLogInfo.setApiResponse("Emp Id fetched successfully!");
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
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

    /**
     * Gets documents by employee and date.
     * 
     * @param timesheetDTO Contains empId, date
     * @return ServiceResponse with document list
     */
    public ServiceResponse getDocumentsByEmpAndDate(TimesheetDTO timesheetDTO) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("getDocumentsByEmpAndDate");
        apiLogInfo.setApiUrl("/api/getDocumentsByEmpAndDate");
        apiLogInfo.setLogLevel("INFO");

        StringBuilder logBuilder = new StringBuilder("Received request to get timesheet documents")
                .append(" | EmpId: ").append(timesheetDTO.getEmpId())
                .append(" | Date: ").append(timesheetDTO.getDate());
        apiLogInfo.setApiRequest(logBuilder.toString());

        try {
            if (timesheetDTO == null) {
                throw new IllegalArgumentException("Request body cannot be null.");
            }
            if (timesheetDTO.getEmpId() == null || timesheetDTO.getEmpId() <= 0) {
                throw new IllegalArgumentException("Employee ID must be a valid positive number.");
            }
            if (timesheetDTO.getDate() == null || timesheetDTO.getDate().trim().isEmpty()) {
                throw new IllegalArgumentException("Date is required.");
            }

            LocalDate date;
            try {
                date = LocalDate.parse(timesheetDTO.getDate());
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format. Expected format: yyyy-MM-dd");
            }

            List<TimesheetDocumentDetails> docs = timesheetDocumentDetailsRepository
                    .findDocumentsByEmpIdAndDate(timesheetDTO.getEmpId(), date);

            if (docs == null || docs.isEmpty()) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceMessage("No documents found for the given employee and date.");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                apiLogInfo.setApiResponse("No documents found.");
            } else {
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(docs);
                response.setServiceMessage("Documents fetched successfully.");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
                apiLogInfo.setApiResponse("Fetched " + docs.size() + " document(s).");
            }
        } catch (IllegalArgumentException ex) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceMessage(ex.getMessage());
            response.setServiceError(ex.toString());
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            apiLogInfo.setApiResponse("Validation Error: " + ex.getMessage());
            apiLogInfo.setLogLevel("WARN");
        } catch (Exception ex) {
            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceMessage("Unexpected error occurred while fetching documents.");
            response.setServiceError(ex.toString());
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            apiLogInfo.setApiResponse("Unexpected Exception: " + ex.getMessage());
            apiLogInfo.setLogLevel("ERROR");
        } finally {
            logService.logMyInfo(httpRequest, apiLogInfo);
        }

        return response;
    }

    // ========== Helper Methods ==========

    /**
     * Builds timesheet DTO list from object array.
     */
    private List<TimesheetDTO> buildTimesheetDTOList(List<Object[]> timesheetList) {
        List<TimesheetDTO> dtoList = new ArrayList<TimesheetDTO>();

        timesheetList.forEach((object) -> {
            TimesheetDTO dto = new TimesheetDTO();
            Long timesheetId = object[0] != null ? Long.parseLong(object[0].toString()) : null;
            dto.setTimesheetId(timesheetId);
            dto.setDate(object[1] != null ? object[1].toString() : null);
            dto.setDayType(object[2] != null ? object[2].toString() : null);
            dto.setEmployeeName(object[3] != null ? object[3].toString() : null);
            dto.setDescription(object[4] != null ? object[4].toString() : null);
            dto.setTotalTime(object[5] != null ? Float.parseFloat(object[5].toString()) : null);
            dto.setStatus(object[6] != null ? object[6].toString() : null);
            dto.setCreatedByName(object[7] != null ? object[7].toString() : null);
            dto.setCreatedOn(object[8] != null ? object[8].toString() : null);
            dto.setEmpId(object[9] != null ? Long.parseLong(object[9].toString()) : null);
            dto.setRemarks(object[10] != null ? object[10].toString() : null);
            dto.setOfficeInTime(object[11] != null ? object[11].toString() : null);
            dto.setOfficeOutTime(object[12] != null ? object[12].toString() : null);
            dto.setTotalWorkingOfficeHours(object[13] != null ? object[13].toString() : null);
            dto.setIsNightShift(object[14] != null ? object[14].toString() : null);
            dto.setLeaveType(object[15] != null ? object[15].toString() : null);
            dto.setClientInTime(object[16] != null ? ((java.sql.Timestamp) object[16]).toLocalDateTime() : null);
            dto.setClientOutTime(object[17] != null ? ((java.sql.Timestamp) object[17]).toLocalDateTime() : null);
            dto.setClientSideId(object[18] != null ? object[18].toString() : null);
            dto.setTotalClientWorkingHours(object[19] != null ? object[19].toString() : null);
            dto.setProjectId(object[20] != null ? Integer.parseInt(object[20].toString()) : null);
            dto.setClientApprovalStatus(object[21] != null ? object[21].toString() : null);
            dto.setHasClientSideId(object[22] != null ? (Boolean) object[22] : null);
            dto.setEmploymentId(object[22] != null ? employeeRepository.fetchEmploymentIdByEmpId(Long.parseLong(object[9].toString())) : null);
            dto.setIsShadowTimesheet(object[23] != null ? (Boolean) object[23] : null);
            dto.setShadowEmpId(object[24] != null ? Long.parseLong(object[24].toString()) : null);
            dto.setRejectReason(object[25] != null ? object[25].toString() : null);
            
			if(timesheetId != null) {

				setDocument(timesheetId,dto);
			};
			// Get InActive Activities In Timesheet
			if(dto.getStatus() != null && dto.getStatus().equals("Pending")) {
				//For a timesheet if activites were inactive after u filled that timesheet this method get those activites.
				setInactiveActivityList(dto);
				
			}

			
            dtoList.add(dto);
        });

        return dtoList;
    }

    /**
     * Builds approved timesheet DTO list from object array.
     */
    private List<TimesheetDTO> buildApprovedTimesheetDTOList(List<Object[]> objectList) {
        List<TimesheetDTO> dtoList = new ArrayList<TimesheetDTO>();

        objectList.forEach((object) -> {
            TimesheetDTO dto = new TimesheetDTO();
            dto.setTimesheetId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
            dto.setDate(object[1] != null ? object[1].toString() : null);
            dto.setDayType(object[2] != null ? object[2].toString() : null);
            dto.setEmployeeName(object[3] != null ? object[3].toString() : null);
            dto.setDescription(object[4] != null ? object[4].toString() : null);
            dto.setStatus(object[5] != null ? object[5].toString() : null);
            dto.setCreatedByName(object[6] != null ? object[6].toString() : null);
            dto.setCreatedOn(object[7] != null ? object[7].toString() : null);
            dto.setTotalTime(object[8] != null ? Float.parseFloat(object[8].toString()) : null);
            dto.setRemarks(object[9] != null ? object[9].toString() : null);
            dto.setEmployeementId(object[10] != null ? Long.parseLong(object[10].toString()) : null);
            dto.setOfficeInTime(object[11] != null ? object[11].toString() : null);
            dto.setOfficeOutTime(object[12] != null ? object[12].toString() : null);
            dto.setTotalWorkingOfficeHours(object[13] != null ? object[13].toString() : null);
            dto.setIsNightShift(object[14] != null ? object[14].toString() : null);
            dto.setLeaveType(object[15] != null ? object[15].toString() : null);
            dto.setEmpId(object[16] != null ? Long.parseLong(object[16].toString()) : null);
            dto.setIsApmosysProduct(object[17] != null ? object[17].toString() : null);

            String employmentId = dto.getEmployeementId() != null ? dto.getEmployeementId().toString() : null;
            String isApmosysProduct = dto.getIsApmosysProduct();

            if (employmentId != null) {
                if ("true".equalsIgnoreCase(isApmosysProduct)) {
                    dto.setEmploymentIdAcToET("AP-" + employmentId);
                } else {
                    dto.setEmploymentIdAcToET("A-" + employmentId);
                }
            }

            dtoList.add(dto);
        });

        return dtoList;
    }

    /**
     * Maps object array to TimesheetDTO.
     */
    private TimesheetDTO mapToTimesheetDTO(Object[] object) {
        TimesheetDTO dto = new TimesheetDTO();
        dto.setTimesheetId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
        dto.setDate(object[1] != null ? object[1].toString() : null);
        dto.setDayType(object[2] != null ? object[2].toString() : null);
        dto.setEmployeementId(object[3] != null ? Long.parseLong(object[3].toString()) : null);
        dto.setEmployeeName(object[4] != null ? object[4].toString() : null);
        dto.setTotalTime(object[5] != null ? Float.parseFloat(object[5].toString()) : null);
        dto.setStatus(object[6] != null ? object[6].toString() : null);
        dto.setCreatedByName(object[7] != null ? object[7].toString() : null);
        dto.setCreatedOn(object[8] != null ? object[8].toString() : null);
        dto.setCreatedByEmpId(object[9] != null ? Long.parseLong(object[9].toString()) : null);
        dto.setRemarks(object[10] != null ? object[10].toString() : null);
        dto.setOfficeInTime(object[11] != null ? object[11].toString() : null);
        dto.setOfficeOutTime(object[12] != null ? object[12].toString() : null);
        dto.setIsNightShift(object[13] != null ? object[13].toString() : null);
        dto.setLeaveType(object[14] != null ? object[14].toString() : null);
        dto.setActivityTimesheetId(object[15] != null ? Long.parseLong(object[15].toString()) : null);
        dto.setActivity(object[16] != null ? object[16].toString() : null);
        dto.setDescription(object[18] != null ? object[18].toString() : null);
        dto.setProjectName(object[19] != null ? object[19].toString() : null);
        dto.setClientName(object[20] != null ? object[20].toString() : null);
        dto.setClientLocation(object[21] != null ? object[21].toString() : null);
        dto.setTeamName(object[22] != null ? object[22].toString() : null);
        dto.setManagerName(object[23] != null ? object[23].toString() : null);
        dto.setActivityId(object[24] != null ? Long.parseLong(object[24].toString()) : null);
        dto.setProjectId(object[25] != null ? Integer.parseInt(object[25].toString()) : null);
        dto.setIsApprenticeship(object[31] != null ? object[31].toString() : null);
        dto.setIsConsultant(object[30] != null ? object[30].toString() : null);
        dto.setDepartmentName(object[32] != null ? object[32].toString() : null);
        dto.setManagerId(object[33] != null ? Long.parseLong(object[33].toString()) : null);
        dto.setEmpId(object[34] != null ? Long.parseLong(object[34].toString()) : null);
        return dto;
    }
    
    public void setDocument(Long timesheetId,TimesheetDTO dto ){
    	 List<TimesheetDocumentDetailsDTO> details =timesheetDocumentDetailsRepository.findAllDocIdByTimesheetId(timesheetId);
		 if(details.size()>1) {
			 dto.setBulkApprovedDocId(null);					
			 for (TimesheetDocumentDetailsDTO doc : details) {
		     if (Boolean.TRUE.equals(doc.getFinalFlag())) {
		         dto.setApprovedDocument(doc.getDocId());
		     }
		     if(Boolean.FALSE.equals(doc.getFinalFlag())) {
		    	 dto.setFilledDocument(doc.getDocId());  	 
		     }
		 } 
		}
		 else if(details.size()==1) {
			dto.setBulkApprovedDocId(details.get(0).getBulkApprovedDocId());								
			if(Boolean.TRUE.equals(details.get(0).getFinalFlag()) && details.get(0).getBulkApprovedDocId()!=null) {
				dto.setFilledDocument(details.get(0).getDocId());
		        dto.setApprovedDocument(details.get(0).getBulkApprovedDocId());
		        }
		else {
			dto.setFilledDocument(details.get(0).getDocId());
		}
		}
    }
    
    public void setInactiveActivityList(TimesheetDTO dto) {
    	
    	List<Object[]> inactiveActivityList = timesheetsRepository
				.getInactiveActivitiesByTimesheetIdOLD(dto.getTimesheetId());
		
		List<ActivityDTO> inactiveDtoList = new ArrayList<ActivityDTO>();
		
		if(!inactiveActivityList.isEmpty()) {
			inactiveActivityList.forEach((actObject) -> {
				ActivityDTO actDto = new ActivityDTO();
				actDto.setTimesheetActivityMapId(actObject[0]!= null ? Long.parseLong(actObject[0].toString()) : null);
				
				inactiveDtoList.add(actDto);
			});
		
			dto.setInactiveTimesheetActivities(inactiveDtoList);	

    }
    }
}

