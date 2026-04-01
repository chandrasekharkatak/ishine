package com.apmosys.employeeportal.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.ActivityDTO;
import com.apmosys.employeeportal.dto.FilteredTimesheetDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.ProjectClientSideIdDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDocumentDetailsDTO;
import com.apmosys.employeeportal.dto.TimesheetMetadataDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.ActivityTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.EmployeeTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.LocationSessionDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.ProjectTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.RejectionDataDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.TimesheetDocumentDataDTO;
import com.apmosys.employeeportal.model.TimesheetDocumentDetails;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.EmployeeTimesheetsNewRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.TimesheetDocumentDetailsRepository;
//import com.apmosys.employeeportal.repository.TimesheetsRepository;
import com.apmosys.employeeportal.utility.DateConversionUtil;
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
	
    private final String pattern="yyyy-MM-dd HH:mm:ss";


//    @Autowired
//    private TimesheetsRepository timesheetsRepository;
    
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
    
    @Autowired
    private EmployeeTimesheetsNewRepository employeeTimesheetsNewRepository;

    @Autowired
    private TimesheetDocumentServiceNew timesheetDocumentServiceNew;

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
            LocalDate start = (timesheetDTO.getStartDate() != null && !timesheetDTO.getStartDate().isEmpty())
                    ? LocalDate.parse(timesheetDTO.getStartDate())
                    : LocalDate.of(2000, 1, 1);
                    //for all time filter;

            LocalDate end = (timesheetDTO.getEndDate() != null && !timesheetDTO.getEndDate().isEmpty())
                    ? LocalDate.parse(timesheetDTO.getEndDate())
                    : LocalDate.now();
           Integer statusInt = null;

           String status = timesheetDTO.getStatus();

           if (status != null && !status.trim().isEmpty()) {
               statusInt = Integer.parseInt(status);
           }
        	//   LocalDate start = LocalDate.parse("2025-11-19");
            //   LocalDate end = LocalDate.parse("2025-12-31");
            List<Object[]> timesheetList = employeeTimesheetsNewRepository
                    .getAllMyTimesheets(timesheetDTO.getEmpId(), start, end, statusInt);

            Optional.ofNullable(timesheetList).ifPresentOrElse((list) -> {
                if (list.isEmpty()) {
                    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                    response.setServiceResponse("Timesheet list is empty");
                    apiLogInfo.setApiResponse("Timesheet list is empty");
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                } else {
                    // Return hierarchical structure - UI will handle display
                    List<EmployeeTimesheetDTO> dtoList = buildTimesheetDTOList(list, false);
                    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                    response.setServiceResponse(dtoList);
                    apiLogInfo.setApiResponse("dtoList : " + dtoList.size() + " timesheets");
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
     * Lightweight timesheet metadata by employee and date range.
     * Returns only header-level fields from employee_timesheets_new
     * (timesheetId, empId, date, dayTypeId, status) for use in UI date pickers.
     */
    public ServiceResponse getTimesheetMetadataByEmpId(TimesheetDTO timesheetDTO) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("add_timesheet");
        apiLogInfo.setApiUrl("/api/v2/timesheet/getTimesheetMetadataByEmpId");
        apiLogInfo.setLogLevel("INFO");

        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("empId : ").append(timesheetDTO.getEmpId())
                  .append(" ,startDate : ").append(timesheetDTO.getStartDate())
                  .append(" ,endDate : ").append(timesheetDTO.getEndDate());

        try {
            LocalDate start = LocalDate.parse(timesheetDTO.getStartDate());
            LocalDate end = LocalDate.parse(timesheetDTO.getEndDate());

            List<Object[]> rows = employeeTimesheetsNewRepository
                    .findTimesheetMetadataByEmpIdAndDateRange(timesheetDTO.getEmpId(), start, end);

            if (rows == null || rows.isEmpty()) {
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(new ArrayList<TimesheetMetadataDTO>());
                apiLogInfo.setApiResponse("Timesheet metadata list is empty");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
            } else {
                List<TimesheetMetadataDTO> metadataList = rows.stream()
                        .map(row -> {
                            TimesheetMetadataDTO dto = new TimesheetMetadataDTO();
                            dto.setTimesheetId(((Number) row[0]).longValue());
                            dto.setEmpId(((Number) row[1]).longValue());
                            dto.setDate(((java.sql.Date) row[2]).toLocalDate());
                            dto.setDayTypeId(row[3] != null ? ((Number) row[3]).intValue() : null);
                            dto.setStatus(row[4] != null ? ((Number) row[4]).intValue() : null);
                            return dto;
                        })
                        .collect(Collectors.toList());

                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(metadataList);
                apiLogInfo.setApiResponse("metadataList : " + metadataList.size() + " records");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
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
//            LocalDate start = LocalDate.parse("2025-11-19");
//            LocalDate end = LocalDate.parse("2025-12-31");
            LocalDate start = LocalDate.parse(timesheetDTO.getStartDate());
            LocalDate end = LocalDate.parse(timesheetDTO.getEndDate());
            
            List<Object[]> timesheetList = employeeTimesheetsNewRepository
                    .getAllMyTeamTimesheets(timesheetDTO.getCreatedBy(), start, end);

            Optional.ofNullable(timesheetList).ifPresentOrElse((list) -> {
                if (list.isEmpty()) {
                    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                    response.setServiceResponse("Timesheet list is empty");
                    apiLogInfo.setApiResponse("Timesheet list is empty");
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                } else {
                    List<EmployeeTimesheetDTO> dtoList = buildTeamTimesheetDTOList(list);
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
//    public ServiceResponse getMyReportees(TimesheetDTO timesheetDTO) {
//        ServiceResponse response = new ServiceResponse();
//        LogDTO apiLogInfo = new LogDTO();
//        apiLogInfo.setSubFeatureName("view_my_teams_timesheets");
//        apiLogInfo.setApiUrl("/api/getMyReportees");
//        apiLogInfo.setLogLevel("INFO");
//        StringBuilder logBuilder = new StringBuilder();
//        logBuilder.append("managerId : ").append(timesheetDTO.getManagerId())
//                  .append(" ,startDate : ").append(timesheetDTO.getStartDate())
//                  .append(" ,endDate : ").append(timesheetDTO.getEndDate());
//        
//        try {
//            List<TimesheetDTO> objectList = timesheetsRepository.getMyReportees(
//                    timesheetDTO.getManagerId());
//
//            Optional.ofNullable(objectList).ifPresentOrElse((list) -> {
//                if (list.isEmpty()) {
//                    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//                    response.setServiceResponse("No reportees found. List is empty.");
//                    apiLogInfo.setApiResponse("No reportees found");
//                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//                } else {
//                    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//                    response.setServiceResponse(objectList);
//                    apiLogInfo.setApiResponse("dtoList : " + objectList);
//                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//                }
//            }, () -> {
//                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//                response.setServiceResponse("No reportees found. List is null.");
//                apiLogInfo.setApiResponse("No reportees found. List is null.");
//                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//            response.setServiceResponse("Something Went Wrong.");
//            response.setServiceError(e.getMessage());
//            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//            apiLogInfo.setLogLevel("ERROR");
//        }
//        
//        apiLogInfo.setApiRequest(logBuilder.toString());
//        logService.logMyInfo(httpRequest, apiLogInfo);
//        return response;
//    }

    /**
     * Gets my reportees approved timesheets.
     * 
     * @param timesheetDTO Contains managerId, startDate, endDate
     * @return ServiceResponse with approved timesheet list
     */
//    public ServiceResponse getMyReporteesApprovedTimesheets(TimesheetDTO timesheetDTO) {
//        ServiceResponse response = new ServiceResponse();
//        LogDTO apiLogInfo = new LogDTO();
//        apiLogInfo.setSubFeatureName("view_my_teams_timesheets");
//        apiLogInfo.setApiUrl("/api/getMyReporteesApprovedTimesheets");
//        apiLogInfo.setLogLevel("INFO");
//        StringBuilder logBuilder = new StringBuilder();
//        logBuilder.append("managerId : ").append(timesheetDTO.getManagerId())
//                  .append(" ,startDate : ").append(timesheetDTO.getStartDate())
//                  .append(" ,endDate : ").append(timesheetDTO.getEndDate());
//        
//        try {
//            LocalDate start = LocalDate.parse(timesheetDTO.getStartDate());
//            LocalDate end = LocalDate.parse(timesheetDTO.getEndDate());
//            
//            List<Object[]> objectList = timesheetsRepository.getMyReporteesApprovedTimesheetsOLD(
//                    timesheetDTO.getManagerId(), start, end);
//
//            Optional.ofNullable(objectList).ifPresentOrElse((list) -> {
//                if (list.isEmpty()) {
//                    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//                    response.setServiceResponse("No timesheets found. List is empty.");
//                    apiLogInfo.setApiResponse("No timesheets found");
//                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//                } else {
//                    List<TimesheetDTO> dtoList = buildApprovedTimesheetDTOList(list);
//                    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//                    response.setServiceResponse(dtoList);
//                    apiLogInfo.setApiResponse("dtoList : " + dtoList);
//                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//                }
//            }, () -> {
//                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//                response.setServiceResponse("No timesheets found. List is null.");
//                apiLogInfo.setApiResponse("No timesheets found. List is null.");
//                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//            response.setServiceResponse("Something Went Wrong.");
//            response.setServiceError(e.getMessage());
//            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//            apiLogInfo.setLogLevel("ERROR");
//        }
//        
//        apiLogInfo.setApiRequest(logBuilder.toString());
//        logService.logMyInfo(httpRequest, apiLogInfo);
//        return response;
//    }

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
                            // dto.setEmployeeTeamDepartment(object[6] != null ? Long.parseLong(object[6].toString()) : null);
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
//    public ServiceResponse getAllOrDeptWiseEmployeeTimesheetReport(FilteredTimesheetDTO filteredTimesheetDTO) {
//        ServiceResponse response = new ServiceResponse();
//        LogDTO apiLogInfo = new LogDTO();
//        apiLogInfo.setSubFeatureName("fetch_timesheets");
//        apiLogInfo.setApiUrl("/api/getAllOrDeptWiseEmployeeTimesheetReport");
//        apiLogInfo.setLogLevel("INFO");
//
//        String deptId = filteredTimesheetDTO.getDeptId();
//        String startDate = filteredTimesheetDTO.getStartDate();
//        String endDate = filteredTimesheetDTO.getEndDate();
//
//        StringBuilder logBuilder = new StringBuilder();
//        logBuilder.append("Requested Department ID: ").append(deptId);
//
//        try {
//            List<Object[]> timesheetList;
//            if ("all".equalsIgnoreCase(deptId)) {
//                timesheetList = timesheetsRepository.getAllEmployeeTimesheetsBetweenDates(startDate, endDate);
//            } else {
//                timesheetList = timesheetsRepository.getTimesheetsByDepartmentAndDateRange(
//                        Long.parseLong(deptId), startDate, endDate);
//            }
//
//            if (timesheetList == null || timesheetList.isEmpty()) {
//                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//                response.setServiceResponse("No timesheets found.");
//                apiLogInfo.setApiResponse("No timesheets found.");
//                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//            } else {
//                List<TimesheetDTO> dtoList = timesheetList.stream()
//                        .map(this::mapToTimesheetDTO)
//                        .collect(Collectors.toList());
//
//                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//                response.setServiceResponse(dtoList);
//                apiLogInfo.setApiResponse("Timesheet data retrieved.");
//                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//            response.setServiceResponse("Something went wrong.");
//            response.setServiceError(e.getMessage());
//            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//            apiLogInfo.setLogLevel("ERROR");
//        }
//
//        apiLogInfo.setApiRequest(logBuilder.toString());
//        logService.logMyInfo(httpRequest, apiLogInfo);
//        return response;
//    }

    /**
     * Gets active projects by employee ID.
     * 
     * @param empId Employee ID
     * @return ServiceResponse with active project list
     */
//    public ServiceResponse getActiveProjectsByEmpId(Long empId) {
//        ServiceResponse response = new ServiceResponse();
//        LogDTO apiLogInfo = new LogDTO();
//        apiLogInfo.setApiUrl("/api/getActiveProjectsByEmpId");
//        apiLogInfo.setLogLevel("INFO");
//
//        try {
//            List<ProjectDTO> activeProjectList = timesheetsRepository.getActiveProjectsByEmpId(empId);
//
//            if (activeProjectList.isEmpty()) {
//                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//                response.setServiceResponse("Please contact to the RMG team to provide you active project mapping!");
//                response.setServiceMessage("Employee has no active project mapping ! For EmpId: " + empId);
//                apiLogInfo.setApiResponse("Employee has no active project mapping ! For EmpId: " + empId);
//                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//                logService.logMyInfo(httpRequest, apiLogInfo);
//                return response;
//            }
//
//            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//            response.setServiceResponse(activeProjectList);
//            response.setServiceMessage("Project List fetched successfully!");
//            apiLogInfo.setApiResponse("Project list where employee has active = 1 in Employee Team Mapping table fetched successfully!");
//            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//        } catch (Exception e) {
//            e.printStackTrace();
//            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//            response.setServiceResponse("Something went wrong.");
//            response.setServiceError(e.getMessage());
//            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//            apiLogInfo.setApiResponse(e.getMessage());
//            apiLogInfo.setLogLevel("ERROR");
//        }
//
//        logService.logMyInfo(httpRequest, apiLogInfo);
//        return response;
//    }

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
//    public ServiceResponse getDocumentsByEmpAndDate(TimesheetDTO timesheetDTO) {
//        ServiceResponse response = new ServiceResponse();
//        LogDTO apiLogInfo = new LogDTO();
//        apiLogInfo.setSubFeatureName("getDocumentsByEmpAndDate");
//        apiLogInfo.setApiUrl("/api/getDocumentsByEmpAndDate");
//        apiLogInfo.setLogLevel("INFO");
//
//        StringBuilder logBuilder = new StringBuilder("Received request to get timesheet documents")
//                .append(" | EmpId: ").append(timesheetDTO.getEmpId())
//                .append(" | Date: ").append(timesheetDTO.getDate());
//        apiLogInfo.setApiRequest(logBuilder.toString());
//
//        try {
//            if (timesheetDTO == null) {
//                throw new IllegalArgumentException("Request body cannot be null.");
//            }
//            if (timesheetDTO.getEmpId() == null || timesheetDTO.getEmpId() <= 0) {
//                throw new IllegalArgumentException("Employee ID must be a valid positive number.");
//            }
//            if (timesheetDTO.getDate() == null || timesheetDTO.getDate().trim().isEmpty()) {
//                throw new IllegalArgumentException("Date is required.");
//            }
//
//            LocalDate date;
//            try {
//                date = LocalDate.parse(timesheetDTO.getDate());
//            } catch (DateTimeParseException e) {
//                throw new IllegalArgumentException("Invalid date format. Expected format: yyyy-MM-dd");
//            }
//
//            List<TimesheetDocumentDetails> docs = timesheetDocumentDetailsRepository
//                    .findDocumentsByEmpIdAndDate(timesheetDTO.getEmpId(), date);
//
//            if (docs == null || docs.isEmpty()) {
//                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//                response.setServiceMessage("No documents found for the given employee and date.");
//                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//                apiLogInfo.setApiResponse("No documents found.");
//            } else {
//                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//                response.setServiceResponse(docs);
//                response.setServiceMessage("Documents fetched successfully.");
//                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//                apiLogInfo.setApiResponse("Fetched " + docs.size() + " document(s).");
//            }
//        } catch (IllegalArgumentException ex) {
//            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//            response.setServiceMessage(ex.getMessage());
//            response.setServiceError(ex.toString());
//            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//            apiLogInfo.setApiResponse("Validation Error: " + ex.getMessage());
//            apiLogInfo.setLogLevel("WARN");
//        } catch (Exception ex) {
//            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//            response.setServiceMessage("Unexpected error occurred while fetching documents.");
//            response.setServiceError(ex.toString());
//            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//            apiLogInfo.setApiResponse("Unexpected Exception: " + ex.getMessage());
//            apiLogInfo.setLogLevel("ERROR");
//        } finally {
//            logService.logMyInfo(httpRequest, apiLogInfo);
//        }
//
//        return response;
//    }

    // ========== Helper Methods ==========

    /**
     * Builds timesheet DTO list from object array.
     */
    private List<EmployeeTimesheetDTO> buildTimesheetDTOList(List<Object[]> rows , boolean isTeam) {

    	Map<Long, EmployeeTimesheetDTO> timesheetMap = new LinkedHashMap<>();

        // The timesheetIds of the timesheets document datas that are already fetched
        List<Long> doneTimesheetIds = new ArrayList<>();

rows.forEach(row -> {

    /* ================= TIMESHEET ================= */
    Long timesheetId = ((Number) row[0]).longValue();
    	System.out.println("timesheetId==> "+timesheetId);

    EmployeeTimesheetDTO timesheet =
        timesheetMap.computeIfAbsent(timesheetId, id -> {
            EmployeeTimesheetDTO dto = new EmployeeTimesheetDTO();
            dto.setTimesheetId(id);
            dto.setEmpId(((Number) row[1]).longValue());
            dto.setDate(((java.sql.Date) row[2]).toLocalDate());
            dto.setDayType(row[3] != null ? row[3].toString() : null);
            dto.setStatus(((Number) row[4]).intValue());
            dto.setTotalWorkingMinutes(
                row[5] != null ? ((Number) row[5]).intValue() : null
            );
            LocalDateTime ldt_checkIn = 
            		row[6] != null ? ((Timestamp) row[6]).toLocalDateTime()
                    : null;
            		
            dto.setWorkCheckIn(ldt_checkIn!= null  ? DateConversionUtil.localDateTimeToString(ldt_checkIn, pattern):null);
            
            LocalDateTime ltd_checkOut = 
            		row[7] != null
                    ? ((Timestamp) row[7]).toLocalDateTime()
                            : null;

            dto.setWorkCheckOut(ltd_checkOut!= null  ? DateConversionUtil.localDateTimeToString(ltd_checkOut, pattern):null);
            dto.setIsNightShift(row[8]!=null&& ((Boolean)row[8]));
            dto.setCreatedBy(row[37]!=null?((Number)row[37]).longValue():null);
            dto.setCreatedOn(row[9]!=null?((Timestamp)row[9]).toLocalDateTime():null);
            dto.setCreatedByName(row[42] != null ? row[42].toString() : null);
            dto.setDayTypeId(row[38]!=null?((Number)row[38]).intValue():null);
                dto.setEmployeeName(row[43] != null ? row[43].toString() : null);
                dto.setEmploymentId(row[44] != null ? ((Number) row[44]).longValue() : null);
                dto.setTeamName((String) row[45]);
            dto.setLocationSessions(new ArrayList<>());
            dto.setDocumentData(new ArrayList<>());
            if(isTeam)  {
                dto.setEmployeeName(row[39]!=null?((String)row[39]):null);
            }
            return dto;
        });

    /* ================= LOCATION ================= */
    Long locationMappingId =
        row[10] != null ? ((Number) row[10]).longValue() : null;
    if (locationMappingId == null) return;

    LocationSessionDTO location =
        timesheet.getLocationSessions()
            .stream()
            .filter(l -> locationMappingId.equals(l.getLocationMappingId()))
            .findFirst()
            .orElseGet(() -> {
                LocationSessionDTO l = new LocationSessionDTO();
                l.setLocationMappingId(locationMappingId);
                l.setWorkLocationTypeId(
                    row[11] != null ? ((Number) row[11]).intValue() : null
                );
                l.setWorkLocationType(row[12] != null ? row[12].toString() : null);
                l.setLocationInTime(row[13] != null ? row[13].toString() : null);
                l.setLocationOutTime(row[14] != null ? row[14].toString() : null);
                l.setClientLocation(row[24] != null ? row[24].toString() : null);
                l.setProjects(new ArrayList<>());
                timesheet.getLocationSessions().add(l);
                return l;
            });

    /* ================= PROJECT ================= */
    Long projectId =
        row[15] != null ? ((Number) row[15]).longValue() : null;
    Long clientLocationId =
        row[32] != null ? ((Number) row[32]).longValue() : null;

    if (projectId == null) return;

    ProjectTimesheetDTO project =
        location.getProjects()
            .stream()
            .filter(p ->
                projectId.equals(p.getProjectId().longValue()) &&
                Objects.equals(clientLocationId, p.getClientLocationId())
            )
            .findFirst()
            .orElseGet(() -> {
                ProjectTimesheetDTO p = new ProjectTimesheetDTO();
                p.setTimesheetId(timesheetId);
                p.setProjectId(projectId.intValue());
                p.setProjectName(row[16] != null ? row[16].toString() : null);
                p.setClientLocationId(clientLocationId);
                p.setClientSideId(row[33] != null ? row[33].toString() : null);
                p.setPoNo(row[17] != null ? row[17].toString() : null);
                p.setPoId(row[18] != null ? ((Number) row[18]).longValue() : null);
                p.setStatus(((Number) row[19]).intValue());
                p.setClientApprovalStatus(
                    row[20] != null ? ((Number) row[20]).intValue() : null
                );
                p.setTotalClientWorkingMinutes(
                    row[21] != null ? ((Number) row[21]).intValue() : null
                );
                p.setDescription(row[41] != null ? row[41].toString() : null);
                p.setActivities(new ArrayList<>());
                p.setRejectionDetails(new ArrayList<>());
                p.setClientId(row[36] != null ? ((Number) row[36]).longValue() : null);
                p.setClientLocation(row[24] != null ? row[24].toString() : null);
                p.setShadowEmpId(row[22]!=null?((Number)row[22]).longValue():null);
                location.getProjects().add(p);
                return p;
            });

    /* ================= ACTIVITY ================= */
    if (row[26] != null) {
        ActivityTimesheetDTO activity = new ActivityTimesheetDTO();
        activity.setId(row[27] != null ? ((Number) row[27]).longValue() : null);
        activity.setTimesheetId(timesheetId);
        activity.setProjectId(projectId != null ? projectId.intValue() : null);
        activity.setActivityId(((Number) row[26]).longValue());
        activity.setActivity(row[28] != null ? row[28].toString() : null);
        activity.setDurationMinutes(
            row[29] != null ? ((Short) row[29]) : null
        );
        activity.setDescription(
            row[30] != null ? row[30].toString() : null
        );
        activity.setTeamId(row[31] != null ? ((Number) row[31]).longValue() : null);
        project.getActivities().add(activity);
    }
    
    /* ================= REJECTION DETAILS ================= */
 // NOTE: Rejection is project-level, but query rows repeat per activity.
 // So add once per (timesheetId + locationMappingId + projectId + clientLocationId).

 String remark = row[35] != null ? row[35].toString() : null;     // trd.remarks
 String rejectionReason = row[39] != null ? row[39].toString() : null; // trrm.rejection_reason

 LocalDateTime rejectedOnLdt =
         row[40] != null ? ((Timestamp) row[40]).toLocalDateTime() : null; // trd.rejected_on

 String rejectedOn =
         rejectedOnLdt != null ? DateConversionUtil.localDateTimeToString(rejectedOnLdt, pattern) : null;

 // Add only if any rejection data exists for this project row
 if (remark != null || rejectionReason != null || rejectedOn != null) {

     boolean alreadyAdded = project.getRejectionDetails().stream().anyMatch(r ->
             Objects.equals(r.getTimesheetId(), timesheetId) &&
             Objects.equals(r.getProjectId(), projectId != null ? projectId.intValue() : null) &&
             Objects.equals(r.getLocationMappingId(), locationMappingId)
     );

     if (!alreadyAdded) {
         RejectionDataDTO rejectionData = new RejectionDataDTO();
         rejectionData.setTimesheetId(timesheetId);
         rejectionData.setProjectId(projectId != null ? projectId.intValue() : null);
         rejectionData.setLocationMappingId(locationMappingId); // if your DTO has this; helps uniqueness
         rejectionData.setRejectionReason(rejectionReason);
         rejectionData.setRejectedOn(rejectedOn);
         rejectionData.setRemark(remark);

         project.getRejectionDetails().add(rejectionData);
     }
 }

      //         // ======================Documents======================
                
                if(!doneTimesheetIds.contains(timesheetId)){
                    List<TimesheetDocumentDataDTO> timesheetDocs = timesheetDocumentServiceNew.getTimesheetDocumentDataByTimesheetId(timesheetId);
                    doneTimesheetIds.add(timesheetId);
                    timesheet.setDocumentData(timesheetDocs);
                }
});

	List<EmployeeTimesheetDTO> response =
	        new ArrayList<>(timesheetMap.values());

        	return response;
    }

    private List<EmployeeTimesheetDTO> buildTeamTimesheetDTOList(List<Object[]> rows) {

        Map<Long, EmployeeTimesheetDTO> timesheetMap = new LinkedHashMap<>();

        // Use Set for O(1) contains
        Set<Long> doneTimesheetIds = new HashSet<>();

        rows.forEach(row -> {

            /* ================= TIMESHEET ================= */
            Long timesheetId = ((Number) row[0]).longValue();

            EmployeeTimesheetDTO timesheet =
                    timesheetMap.computeIfAbsent(timesheetId, id -> {
                        EmployeeTimesheetDTO dto = new EmployeeTimesheetDTO();
                        dto.setTimesheetId(id);
                        dto.setEmpId(row[1] != null ? ((Number) row[1]).longValue() : null);
                        dto.setDate(row[2] != null ? ((java.sql.Date) row[2]).toLocalDate() : null);
                        dto.setDayType(row[3] != null ? row[3].toString() : null);
                        dto.setStatus(row[4] != null ? ((Number) row[4]).intValue() : null);
                        dto.setTotalWorkingMinutes(row[5] != null ? ((Number) row[5]).intValue() : null);

                        LocalDateTime checkIn =
                                row[6] != null ? ((Timestamp) row[6]).toLocalDateTime() : null;
                        dto.setWorkCheckIn(checkIn != null ? DateConversionUtil.localDateTimeToString(checkIn, pattern) : null);

                        LocalDateTime checkOut =
                                row[7] != null ? ((Timestamp) row[7]).toLocalDateTime() : null;
                        dto.setWorkCheckOut(checkOut != null ? DateConversionUtil.localDateTimeToString(checkOut, pattern) : null);

                        dto.setIsNightShift(row[8] != null && ((Boolean) row[8]));
                        dto.setCreatedOn(row[9] != null ? ((Timestamp) row[9]).toLocalDateTime() : null);

                        // Team-specific fields
                        dto.setCreatedBy(row[37] != null ? ((Number) row[37]).longValue() : null);
                        dto.setDayTypeId(row[38] != null ? ((Number) row[38]).intValue() : null);
                        dto.setEmployeeName(row[39] != null ? row[39].toString() : null); // e.name
                        dto.setCreatedByName(row[43] != null ? row[43].toString() : null); // creator_emp.name

                        dto.setLocationSessions(new ArrayList<>());
                        dto.setDocumentData(new ArrayList<>());
                        return dto;
                    });

            /* ================= LOCATION ================= */
            Long locationMappingId = row[10] != null ? ((Number) row[10]).longValue() : null;
            if (locationMappingId == null) return;

            LocationSessionDTO location =
                    timesheet.getLocationSessions()
                            .stream()
                            .filter(l -> locationMappingId.equals(l.getLocationMappingId()))
                            .findFirst()
                            .orElseGet(() -> {
                                LocationSessionDTO l = new LocationSessionDTO();
                                l.setLocationMappingId(locationMappingId);
                                l.setWorkLocationTypeId(row[11] != null ? ((Number) row[11]).intValue() : null);
                                l.setWorkLocationType(row[12] != null ? row[12].toString() : null);
                                l.setLocationInTime(row[13] != null ? row[13].toString() : null);
                                l.setLocationOutTime(row[14] != null ? row[14].toString() : null);
                                l.setProjects(new ArrayList<>());
                                timesheet.getLocationSessions().add(l);
                                return l;
                            });

            /* ================= PROJECT ================= */
            Long projectId = row[15] != null ? ((Number) row[15]).longValue() : null;
            Long clientLocationId = row[32] != null ? ((Number) row[32]).longValue() : null;
            if (projectId == null) return;

            ProjectTimesheetDTO project =
                    location.getProjects()
                            .stream()
                            .filter(p ->
                                    projectId.equals(p.getProjectId().longValue()) &&
                                    Objects.equals(clientLocationId, p.getClientLocationId())
                            )
                            .findFirst()
                            .orElseGet(() -> {
                                ProjectTimesheetDTO p = new ProjectTimesheetDTO();
                                p.setTimesheetId(timesheetId);
                                p.setProjectId(projectId.intValue());
                                p.setProjectName(row[16] != null ? row[16].toString() : null);
                                p.setClientLocationId(clientLocationId);
                                p.setClientSideId(row[33] != null ? row[33].toString() : null);
                                p.setPoNo(row[17] != null ? row[17].toString() : null);
                                p.setPoId(row[18] != null ? ((Number) row[18]).longValue() : null);
                                p.setStatus(row[19] != null ? ((Number) row[19]).intValue() : null);
                                p.setClientApprovalStatus(row[20] != null ? ((Number) row[20]).intValue() : null);
                                p.setTotalClientWorkingMinutes(row[21] != null ? ((Number) row[21]).intValue() : null);
                                p.setShadowEmpId(row[22] != null ? ((Number) row[22]).longValue() : null);
                                p.setClientId(row[36] != null ? ((Number) row[36]).longValue() : null);
                                p.setClientLocation(row[24] != null ? row[24].toString() : null);
                                p.setDescription(row[42] != null ? row[42].toString() : null);
                                p.setActivities(new ArrayList<>());
                                p.setRejectionDetails(new ArrayList<>());
                                location.getProjects().add(p);
                                return p;
                            });

            /* ================= ACTIVITY ================= */
            // NOTE: rows can repeat; to avoid duplicate activities, dedupe by etam_id (row[27]) if you want.
            if (row[26] != null) {
                Long etamId = row[27] != null ? ((Number) row[27]).longValue() : null;

                boolean activityAlreadyAdded = etamId != null && project.getActivities().stream()
                        .anyMatch(a -> Objects.equals(a.getId(), etamId));

                if (!activityAlreadyAdded) {
                    ActivityTimesheetDTO activity = new ActivityTimesheetDTO();
                    activity.setId(etamId);
                    activity.setTimesheetId(timesheetId);
                    activity.setProjectId(projectId.intValue());
                    activity.setActivityId(((Number) row[26]).longValue());
                    activity.setActivity(row[28] != null ? row[28].toString() : null);
                    activity.setDurationMinutes(row[29] != null ? ((Short) row[29]) : null);
                    activity.setDescription(row[30] != null ? row[30].toString() : null);
                    activity.setTeamId(row[31] != null ? ((Number) row[31]).longValue() : null);
                    project.getActivities().add(activity);
                }
            }

            /* ================= REJECTION DETAILS ================= */
            String remark = row[35] != null ? row[35].toString() : null;            // trd.remarks
            String rejectionReason = row[40] != null ? row[40].toString() : null;   // trrm.rejection_reason

            LocalDateTime rejectedOnLdt =
                    row[41] != null ? ((Timestamp) row[41]).toLocalDateTime() : null; // trd.rejected_on
            String rejectedOn =
                    rejectedOnLdt != null ? DateConversionUtil.localDateTimeToString(rejectedOnLdt, pattern) : null;

            if (remark != null || rejectionReason != null || rejectedOn != null) {

                boolean alreadyAdded = project.getRejectionDetails().stream().anyMatch(r ->
                        Objects.equals(r.getTimesheetId(), timesheetId) &&
                        Objects.equals(r.getProjectId(), projectId.intValue()) &&
                        Objects.equals(r.getLocationMappingId(), locationMappingId) &&
                        Objects.equals(r.getRejectionReason(), rejectionReason) &&
                        Objects.equals(r.getRemark(), remark) &&
                        Objects.equals(r.getRejectedOn(), rejectedOn)
                );

                if (!alreadyAdded) {
                    RejectionDataDTO rejectionData = new RejectionDataDTO();
                    rejectionData.setTimesheetId(timesheetId);
                    rejectionData.setProjectId(projectId.intValue());
                    rejectionData.setLocationMappingId(locationMappingId);
                    rejectionData.setRejectionReason(rejectionReason);
                    rejectionData.setRemark(remark);
                    rejectionData.setRejectedOn(rejectedOn);
                    project.getRejectionDetails().add(rejectionData);
                }
            }

            /* ================= DOCUMENTS ================= */
            if (doneTimesheetIds.add(timesheetId)) { // add() returns false if already present
                List<TimesheetDocumentDataDTO> timesheetDocs =
                        timesheetDocumentServiceNew.getTimesheetDocumentDataByTimesheetId(timesheetId);
                timesheet.setDocumentData(timesheetDocs);
            }
        });

        return new ArrayList<>(timesheetMap.values());
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
    
//    public void setInactiveActivityList(TimesheetDTO dto) {
//    	
//    	List<Object[]> inactiveActivityList = timesheetsRepository
//				.getInactiveActivitiesByTimesheetIdOLD(dto.getTimesheetId());
//		
//		List<ActivityDTO> inactiveDtoList = new ArrayList<ActivityDTO>();
//		
//		if(!inactiveActivityList.isEmpty()) {
//			inactiveActivityList.forEach((actObject) -> {
//				ActivityDTO actDto = new ActivityDTO();
//				actDto.setTimesheetActivityMapId(actObject[0]!= null ? Long.parseLong(actObject[0].toString()) : null);
//				
//				inactiveDtoList.add(actDto);
//			});
//		
//			dto.setInactiveTimesheetActivities(inactiveDtoList);	
//
//    }
//    }
    private Integer mapStatusToId(String status) {
        switch (status) {
            case "Pending": return 1;
            case "Approved": return 2;
            case "Rejected": return 3;
            default: return 0;
        }
    }
    
//    public ServiceResponse getActiveProjectsAndClientSideIdByEmpId(Long empId) {
//		
////		return timesheetQueryService.getActiveProjectsAndClientSideIdByEmpId(empId);
//	    ServiceResponse response = new ServiceResponse();
//	    LogDTO apiLogInfo = new LogDTO();
//	    apiLogInfo.setApiUrl("/api/getActiveProjectsByEmpId");
//	    apiLogInfo.setLogLevel("INFO");
//	    
//	    try {
//	        List<ProjectClientSideIdDTO> activeProjectList = employeeTimesheetsNewRepository.getActiveProjectsAndClientSideIdByEmpId(empId);
//	        
//	        if (activeProjectList.isEmpty()) {
//	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//	            response.setServiceResponse("Please contact to the RMG team to provide you active project mapping!");
//	            response.setServiceMessage("Employee has no active project mapping ! For EmpId: " + empId);
//	            
//	            apiLogInfo.setApiResponse("Employee has no active project mapping ! For EmpId: " + empId);
//	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//	            logService.logMyInfo(httpRequest, apiLogInfo);
//	            return response;
//	        }
//	        
//            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//            response.setServiceResponse(activeProjectList);
//            response.setServiceMessage("Project List fetched successfully!");
//
//            apiLogInfo.setApiResponse("Project list where employee has active = 1 in Employee Team Mapping table fetched successfully!");
//            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//	        
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//	        response.setServiceResponse("Something went wrong.");
//	        response.setServiceError(e.getMessage());
//
//	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//	        apiLogInfo.setApiResponse(e.getMessage()); 
//	        apiLogInfo.setLogLevel("ERROR");
//	    }
//
////	    logService.logMyInfo(httpRequest, apiLogInfo);
//	    return response;
//	}
	
public ServiceResponse getAlreadyFilledTimesheetDatesByEmpId(Long empId,String dayType){
    try 
    {
    	if(dayType.equalsIgnoreCase("Week-off")){
    		
    	}
    }
    catch(Exception ex) {
    	
    }
	return null;
}

}
