package com.apmosys.employeeportal.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.GetEmployeeByNameAndEmpldDTO;
import com.apmosys.employeeportal.dto.GetEmployeeSummaryOnExportDTO;
import com.apmosys.employeeportal.dto.GetEmployeeTimesheetAsCalenderDTO;
import com.apmosys.employeeportal.dto.GetProjectByMonthRangeAndEmpIdDTO;
import com.apmosys.employeeportal.dto.LastTimesheetFieldDto;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDashboardCountDTO;
import com.apmosys.employeeportal.dto.TimesheetDocumentApprovalDTO;
import com.apmosys.employeeportal.model.TimesheetDataDTO;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.TimesheetDocumentApprovalRepository;
import com.apmosys.employeeportal.repository.TimesheetsRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing timesheet dashboard operations.
 * 
 * This service encapsulates all dashboard-related operations for timesheets:
 * - Dashboard counts and statistics
 * - Employee timesheet summaries
 * - Calendar views
 * - Export functionality
 * - Status-wise counts
 * 
 * @author Timesheet Refactoring - Phase 5
 */
@Slf4j
@Service
public class TimesheetDashboardService {

    @Autowired
    private TimesheetsRepository timesheetsRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private TimesheetDocumentApprovalRepository timesheetDocumentApprovalRepository;
    
    @Autowired
    private LogService logService;
    
    @Autowired
    private HttpServletRequest httpRequest;

    /**
     * Gets timesheet dashboard count for employee.
     * 
     * @param month Month
     * @param year Year
     * @param empId Employee ID
     * @param isClientDashboard Whether it's client dashboard
     * @param billableTypes List of billable types
     * @param employeeActive Employee active status
     * @param clientSideFilter Client side filter
     * @return ServiceResponse with dashboard count DTO
     */
    public ServiceResponse getTimesheetDashboardCountForEmployee(
            Integer month, Integer year, Long empId, Boolean isClientDashboard,
            List<String> billableTypes, String employeeActive, String clientSideFilter) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("getTimesheetDashboardCountForEmployee");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("getTimesheetDashboardCountForEmployee");
        
        try {
            List<Object[]> countForEmployee;
            if (isClientDashboard) {
                countForEmployee = timesheetsRepository.getTimesheetDashboardCountForEmployee(
                        month, year, empId, clientSideFilter);
            } else {
                countForEmployee = timesheetsRepository.getTimesheetDashboardCountForAllEmployee(
                        month, year, empId, billableTypes, employeeActive);
            }
            
            if (countForEmployee.isEmpty()) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Unable to fetch the dashboard count for employee!");
                apiLogInfo.setApiResponse("Failed to fetch the dashboard count for employee \n");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            } else {
                Object[] row = countForEmployee.get(0);
                TimesheetDashboardCountDTO dto = new TimesheetDashboardCountDTO();
                dto.setTotalApplicableCount(row[0] != null ? ((Number) row[0]).intValue() : 0);
                dto.setApprovedCount(row[1] != null ? ((Number) row[1]).intValue() : 0);
                dto.setDefaulterCount(row[3] != null ? ((Number) row[3]).intValue() : 0);
                dto.setClientSidePendingCount(row[2] != null ? ((Number) row[2]).intValue() : 0);
                dto.setTotaldefaulterCount(row[4] != null ? ((Number) row[4]).intValue() : 0);
                
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(dto);
                apiLogInfo.setApiResponse("Dashboard count fetched successfully ");
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

    /**
     * Gets timesheet dashboard count for project.
     * 
     * @param month Month
     * @param year Year
     * @param empId Employee ID
     * @param isClientDashboard Whether it's client dashboard
     * @param billableType List of billable types
     * @param projectActive Project active status
     * @return ServiceResponse with dashboard count DTO
     */
    public ServiceResponse getTimesheetDashboardCountForProject(
            Integer month, Integer year, Long empId, Boolean isClientDashboard,
            List<String> billableType, String projectActive) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("getTimesheetDashboardCountForProject");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("getTimesheetDashboardCountForProject");
        
        try {
            List<Object[]> countForProject;
            if (isClientDashboard) {
                countForProject = timesheetsRepository.getTimesheetDashboardCountForProject(
                        month, year, empId);
            } else {
                countForProject = timesheetsRepository.getAllEmpTimesheetDashboardCountForProject(
                        month, year, empId, billableType, projectActive);
            }

            if (countForProject.isEmpty()) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Unable to fetch the dashboard count for project!");
                apiLogInfo.setApiResponse("Failed to fetch the dashboard count for project \n");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            } else {
                Object[] row = countForProject.get(0);
                TimesheetDashboardCountDTO dto = new TimesheetDashboardCountDTO();
                dto.setTotalApplicableCount(row[0] != null ? ((Number) row[0]).intValue() : 0);
                dto.setApprovedCount(row[1] != null ? ((Number) row[1]).intValue() : 0);
                dto.setDefaulterCount(row[3] != null ? ((Number) row[3]).intValue() : 0);
                dto.setClientSidePendingCount(row[2] != null ? ((Number) row[2]).intValue() : 0);
                
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(dto);
                apiLogInfo.setApiResponse("Dashboard count fetched successfully ");
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

    /**
     * Gets total VMS filled count.
     * 
     * @param timesheetDTO Contains empId
     * @return ServiceResponse with timesheet list
     */
    public ServiceResponse totalVmsFilledCount(TimesheetDTO timesheetDTO) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setApiUrl("/api/getEmployeeListByProjectId");
        apiLogInfo.setLogLevel("INFO");
        
        try {
            Long empId = Long.valueOf(timesheetDTO.getEmpId());
            List<Object[]> timesheetList = timesheetsRepository.getTotalVmsFilledCount(empId);

            if (timesheetList == null || timesheetList.isEmpty()) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("No VMS Filled!");
                response.setServiceMessage("No VMS Filled!");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                logService.logMyInfo(httpRequest, apiLogInfo);
                return response;
            }
            
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(timesheetList);
            response.setServiceMessage("TimesheetList List fetched successfully!");
            apiLogInfo.setApiResponse("Timesheet List fetched successfully!");
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
     * Gets total Ishine filled count.
     * 
     * @param status Status filter
     * @return ServiceResponse with timesheet list
     */
    public ServiceResponse totalIshineFilledCount(String status) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setApiUrl("/api/getEmployeeListByProjectId");
        apiLogInfo.setLogLevel("INFO");
        
        try {
            List<Object[]> ishineTimesheetList = timesheetsRepository.totalIshineFilledCount();
            
            if (ishineTimesheetList == null || ishineTimesheetList.isEmpty()) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("No Timesheet Filled!");
                response.setServiceMessage("No Timesheet Filled!");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                logService.logMyInfo(httpRequest, apiLogInfo);
                return response;
            }
            
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(ishineTimesheetList);
            response.setServiceMessage("TimesheetList List fetched successfully!");
            apiLogInfo.setApiResponse("Timesheet List fetched successfully!");
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
     * Gets total VMS not filled.
     * 
     * @param timesheetDTO Contains empId
     * @return ServiceResponse with timesheet list
     */
    public ServiceResponse totalvmsNotFilled(TimesheetDTO timesheetDTO) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setApiUrl("/api/getEmployeeListByProjectId");
        apiLogInfo.setLogLevel("INFO");
        
        try {
            List<Object[]> timesheetList = timesheetsRepository.totalvmsNotFilled(timesheetDTO.getEmpId());
            
            if (timesheetList == null || timesheetList.isEmpty()) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("No VMS Filled!");
                response.setServiceMessage("No VMS Filled!");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                logService.logMyInfo(httpRequest, apiLogInfo);
                return response;
            }
            
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(timesheetList);
            response.setServiceMessage("TimesheetList List fetched successfully!");
            apiLogInfo.setApiResponse("Timesheet List fetched successfully!");
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
     * Gets total Ishine not filled count.
     * 
     * @param timesheetDTO Contains empId and isClientDashboard flag
     * @return ServiceResponse with timesheet list
     */
    public ServiceResponse totalIshineNotFilledCount(TimesheetDTO timesheetDTO) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setApiUrl("/api/getEmployeeListByProjectId");
        apiLogInfo.setLogLevel("INFO");
        
        try {
            List<Object[]> ishineTimesheetList;
            if (timesheetDTO.getIsClientDashboard()) {
                ishineTimesheetList = timesheetsRepository.totalIshineNotFilledCount(timesheetDTO.getEmpId());
            } else {
                ishineTimesheetList = timesheetsRepository.totalIshineNotFilledCountForAllEmpDash(timesheetDTO.getEmpId());
            }
            
            if (ishineTimesheetList == null || ishineTimesheetList.isEmpty()) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("No Timesheet Filled!");
                response.setServiceMessage("No Timesheet Filled!");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                logService.logMyInfo(httpRequest, apiLogInfo);
                return response;
            }
            
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(ishineTimesheetList);
            response.setServiceMessage("TimesheetList List fetched successfully!");
            apiLogInfo.setApiResponse("Timesheet List fetched successfully!");
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
     * Gets VMS document approval status wise count.
     * 
     * @return ServiceResponse with rejection counts by level
     */
    public ServiceResponse getVmsDocumentApprovalStatusWiseCount() {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("getVmsDocumentApprovalStatusWiseCount");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("getVmsDocumentApprovalStatusWiseCount");
        
        try {
            List<TimesheetDocumentApprovalDTO> details = 
                    timesheetDocumentApprovalRepository.getRejectionCountsByLevel();
            
            if (details == null) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("VMS Rejection Count Not Present");
                response.setServiceMessage("VMS Rejection Count Not Present");
                apiLogInfo.setApiResponse("VMS Rejection Count Not Present");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                logService.logMyInfo(httpRequest, apiLogInfo);
                return response;
            } else {
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(details);
                response.setServiceMessage("VMS Rejection Count Fetched");
                apiLogInfo.setApiResponse("VMS Rejection Count Fetched");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
            }
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

    /**
     * Gets employee timesheet as calendar.
     * 
     * @param object Contains empId, month, year, projectId
     * @return ServiceResponse with calendar DTO list
     */
    public ServiceResponse getEmployeeTimesheetAsCalender(GetEmployeeSummaryOnExportDTO object) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("getEmployeeTimesheetAsCalender");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("getEmployeeTimesheetAsCalender");
        
        try {
            List<Object[]> empTimesheet;
            Long empId = Long.parseLong(object.getEmpId().toString());
            Integer month = Integer.parseInt(object.getMonth().toString());
            Integer year = Integer.parseInt(object.getYear().toString());
            Integer projectId = Integer.parseInt(object.getProjectId().toString());
            Boolean flag = timesheetsRepository.checkProjectIsClientApplicable(projectId);
            
            if (flag == null || flag == false) {
                empTimesheet = timesheetsRepository.getEmployeeTimesheetAsCalenderForAllEmp(
                        empId, month, year, projectId);
            } else {
                empTimesheet = timesheetsRepository.getEmployeeTimesheetAsCalender(
                        empId, month, year, projectId);
            }
            
            List<GetEmployeeTimesheetAsCalenderDTO> dtoList = buildCalendarDTOList(empTimesheet);

            if (dtoList.isEmpty()) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Unable to fetch the timesheet Data for Employee !!!");
                apiLogInfo.setApiResponse("Failed to set the data in dto \n");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            } else {
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(dtoList);
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

    /**
     * Gets employee summary on export.
     * 
     * @param object Contains month, year, empId, getAllEmp flag, billableType
     * @return ServiceResponse with summary DTO list
     */
    public ServiceResponse getEmployeeSummaryOnExport(GetEmployeeSummaryOnExportDTO object) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("getEmployeeSummaryOnExport");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("getEmployeeTimesheetAsCalenderByProjectId");
        
        try {
            List<Object[]> empTimesheet;
            if (object.getAllEmp()) {
                empTimesheet = timesheetsRepository.getEmployeeSummaryReportAll(
                        object.getMonth(), object.getYear(), object.getEmpId(), object.getBillableType());
            } else {
                empTimesheet = timesheetsRepository.getEmployeeSummaryReportClientSideApplicable(
                        object.getMonth(), object.getYear(), object.getEmpId());
            }
            
            List<GetEmployeeTimesheetAsCalenderDTO> dtoList = buildCalendarDTOList(empTimesheet);

            if (dtoList.isEmpty()) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Unable to fetch the timesheet Data !!!");
                apiLogInfo.setApiResponse("Failed to set the data in dto \n");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            } else {
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(dtoList);
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

    /**
     * Gets last filled timesheet by employee.
     * 
     * @param empId Employee ID
     * @return ServiceResponse with last timesheet details
     */
    public ServiceResponse getLastFilledTimesheetByEmp(Long empId) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setApiUrl("/api/getLastFilledTimesheetByEmp");
        apiLogInfo.setLogLevel("INFO");
        
        try {
            List<Object[]> activeCheckList = timesheetsRepository.checkEmployeeActiveOrNot(empId);

            // Case 1: No records found in the timesheet - employee never filled any timesheet
            if (activeCheckList.isEmpty()) {
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(Collections.emptyList());
                response.setServiceMessage("Employee has never filled any timesheet.");
                response.setServiceResponse1("No Timesheet");
                apiLogInfo.setApiResponse("No timesheet record found for employee.");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
                logService.logMyInfo(httpRequest, apiLogInfo);
                return response;
            }
            
            // Determine active status from the activeCheckList result
            boolean isActive = Integer.parseInt(String.valueOf(activeCheckList.get(0)[1])) == 1;
            response.setServiceResponse1(isActive ? "Active" : "Not Active");

            if (!isActive) {
                // Case 2: Employee was in a project but is not currently active
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(Collections.emptyList());
                response.setServiceMessage("Employee is not active on previous project.");
                apiLogInfo.setApiResponse("Employee is Not Active");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
                logService.logMyInfo(httpRequest, apiLogInfo);
                return response;
            }
            
            List<Object[]> resultList = timesheetsRepository.getLastTimesheetFiledByEmpId(empId);
            
            if (!resultList.isEmpty()) {
                List<LastTimesheetFieldDto> dtoList = resultList.stream()
                        .map(row -> {
                            LastTimesheetFieldDto dto = new LastTimesheetFieldDto();
                            dto.setEmployementId(row.length > 0 && row[0] != null ? row[0].toString() : null);
                            dto.setOfficeInTime(row.length > 1 && row[1] != null ? 
                                    ((java.sql.Timestamp) row[1]).toLocalDateTime() : null);
                            dto.setOfficeOutTime(row.length > 2 && row[2] != null ? 
                                    ((java.sql.Timestamp) row[2]).toLocalDateTime() : null);
                            dto.setProjectId(row.length > 3 && row[3] != null ? 
                                    Long.parseLong(row[3].toString()) : null);
                            dto.setClientId(row.length > 4 && row[4] != null ? 
                                    Long.parseLong(row[4].toString()) : null);
                            dto.setClientLocationID(row.length > 5 && row[5] != null ? 
                                    Long.parseLong(row[5].toString()) : null);
                            dto.setTeamName(row.length > 6 && row[6] != null ? row[6].toString() : null);
                            dto.setActivity(row.length > 7 && row[7] != null ? row[7].toString() : null);
                            dto.setActivityID(row.length > 8 && row[8] != null ? 
                                    Long.parseLong(row[8].toString()) : null);
                            dto.setDescription(row.length > 9 && row[9] != null ? row[9].toString() : null);
                            dto.setTeamId(row.length > 10 && row[10] != null ? 
                                    Long.parseLong(row[10].toString()) : null);
                            dto.setClientApprovalStatus(
                                    (row.length > 11) 
                                        ? (row[11] != null ? row[11].toString() : null) 
                                        : null
                            );
                            dto.setCompletionTime(row.length > 12 && row[12] != null ? 
                                    Float.parseFloat(row[12].toString()) : null);
                            dto.setTimesheetLockUpdatedOn(
                                    row.length > 13 && row[13] != null 
                                        ? ((java.sql.Date) row[13]).toLocalDate() 
                                        : null
                            );
                            dto.setIstimesheetLockCheckEnable(row.length > 14 && row[14] != null ? 
                                    row[14].toString() : null);
                            return dto;
                        })
                        .collect(Collectors.toList());

                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(dtoList);
                response.setServiceMessage("Last timesheet found for employee.");
                apiLogInfo.setApiResponse("Last timesheet found for active employee");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
            } else {
                // Case 4: Active employee but no timesheet found
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(Collections.emptyList());
                response.setServiceMessage("No timesheet found for employee.");
                apiLogInfo.setApiResponse("No timesheet found");
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

        logService.logMyInfo(httpRequest, apiLogInfo);
        return response;
    }

    /**
     * Gets employee by name and emp ID for timesheet.
     * 
     * @param timesheetDTO Contains empId and isClientDashboard flag
     * @return ServiceResponse with employee list
     */
    public ServiceResponse getEmployeeByNameAndEmpidForTimesheet(TimesheetDTO timesheetDTO) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("getEmployeeByNameAndEmpidForTimesheet");
        apiLogInfo.setApiUrl("/api/getEmployeeByNameAndEmpidForTimesheet");
        apiLogInfo.setLogLevel("INFO");

        try {
            List<Object[]> employees;
            if (timesheetDTO.getIsClientDashboard()) {
                employees = employeeRepository.getEmployeeByNameAndEmpidForTimesheetClientDashboard(
                        timesheetDTO.getEmpId());
            } else {
                employees = employeeRepository.getEmployeeByNameAndEmpidForTimesheet(timesheetDTO.getEmpId());
            }

            List<GetEmployeeByNameAndEmpldDTO> listDto = new ArrayList<GetEmployeeByNameAndEmpldDTO>();

            if (!employees.isEmpty()) {
                for (Object[] object : employees) {
                    GetEmployeeByNameAndEmpldDTO dto = new GetEmployeeByNameAndEmpldDTO();
                    dto.setEmpId(object[0] != null ? Long.valueOf(object[0].toString()) : null);
                    dto.setName(object[1] != null ? object[1].toString() : null);
                    dto.setEmploymentId(object[2] != null ? object[2].toString() : null);
                    listDto.add(dto);
                }
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(listDto);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("Error : " + e.getMessage());
        }

        return response;
    }

    /**
     * Gets project by month range and employee ID.
     * 
     * @param object Contains month, year, empId
     * @return ServiceResponse with project list
     */
    public ServiceResponse getProjectByMonthRangeAndEmpId(GetEmployeeSummaryOnExportDTO object) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("Timesheet/Calander View/Project Drop-Down");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("Timesheet/Calander View/Project Drop-Down/getProjectByMonthRangeAndEmpId");
        
        try {
            if (object == null) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Invalid request: request body is missing.");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                return response;
            }

            if (object.getMonth() == null || object.getYear() == null || object.getEmpId() == null) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Month, Year and Employee ID are mandatory.");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                return response;
            }

            if (object.getMonth() < 1 || object.getMonth() > 12) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Invalid month value. It must be between 1 and 12.");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                return response;
            }
            
            List<Object[]> projList = timesheetsRepository.getProjectByMonthRangeAndEmpId(
                    object.getMonth(), object.getYear(), object.getEmpId());
            
            if (projList == null || projList.isEmpty()) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("No projects found for the selected month.");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                return response;
            }
            
            List<GetProjectByMonthRangeAndEmpIdDTO> projectList = new ArrayList<>();

            for (Object[] row : projList) {
                GetProjectByMonthRangeAndEmpIdDTO dto = new GetProjectByMonthRangeAndEmpIdDTO();
                dto.setEmpId(row[0] != null ? ((Number) row[0]).longValue() : null);
                dto.setName(row[1] != null ? row[1].toString() : null);
                dto.setProjectId(row[2] != null ? ((Number) row[2]).intValue() : null);
                dto.setProjectName(row[3] != null ? row[3].toString() : null);
                dto.setPoNo(row[4] != null ? row[4].toString() : null);
                dto.setEmployementId(row[5] != null ? row[5].toString() : null);
                projectList.add(dto);
            }

            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(projectList);
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
        
        return response;
    }

    // ========== Helper Methods ==========

    /**
     * Builds calendar DTO list from object array.
     */
    private List<GetEmployeeTimesheetAsCalenderDTO> buildCalendarDTOList(List<Object[]> empTimesheet) {
        List<GetEmployeeTimesheetAsCalenderDTO> dtoList = new ArrayList<>();
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        for (Object[] obj : empTimesheet) {
            GetEmployeeTimesheetAsCalenderDTO dto = new GetEmployeeTimesheetAsCalenderDTO();

            dto.setEmpId(obj[0] != null ? Long.parseLong(obj[0].toString()) : null);
            dto.setClientSideId(obj[1] != null ? obj[1].toString() : null);
            dto.setStartDate(obj[2] != null ? obj[2].toString() : null);
            dto.setTeamName(obj[3] != null ? obj[3].toString() : null);
            dto.setTeamId(obj[4] != null ? Long.parseLong(obj[4].toString()) : null);
            dto.setEmployeeName(obj[5] != null ? obj[5].toString() : null);
            dto.setSpoc(obj[6] != null ? obj[6].toString() : null);
            dto.setBillableType(obj[7] != null ? obj[7].toString() : null);
            dto.setEmployeeRole(obj[8] != null ? obj[8].toString() : null);
            dto.setDepartment(obj[9] != null ? obj[9].toString() : null);
            dto.setProjectId(obj[10] != null ? Integer.parseInt(obj[10].toString()) : null);
            dto.setProjectName(obj[11] != null ? obj[11].toString() : null);
            dto.setProjectManagerName(obj[12] != null ? obj[12].toString() : null);
            dto.setPoNo(obj[13] != null ? obj[13].toString() : null);
            dto.setClientName(obj[14] != null ? obj[14].toString() : null);
            dto.setReportingManagerId(obj[15] != null ? Long.parseLong(obj[15].toString()) : null);
            dto.setMonthName(obj[16] != null ? obj[16].toString() : null);
            dto.setExpectedTimesheetFillCount(obj[17] != null ? Integer.parseInt(obj[17].toString()) : null);
            dto.setClientSideNotFilledCount(obj[18] != null ? Integer.parseInt(obj[18].toString()) : null);
            dto.setClientSidePendingCount(obj[19] != null ? Integer.parseInt(obj[19].toString()) : null);
            dto.setClientSideApprovedCount(obj[20] != null ? Integer.parseInt(obj[20].toString()) : null);
            
            Map<String, TimesheetDataDTO> timesheetData = new HashMap<>();

            for (int i = 0; i < 31; i++) {
                int baseIndex = 21 + (i * 3);
                String status = obj.length > baseIndex && obj[baseIndex] != null ? obj[baseIndex].toString() : null;
                
                String inTimeRaw = obj.length > (baseIndex + 1) && obj[baseIndex + 1] != null ? 
                        obj[baseIndex + 1].toString() : null;
                String outTimeRaw = obj.length > (baseIndex + 2) && obj[baseIndex + 2] != null ? 
                        obj[baseIndex + 2].toString() : null;

                String inTime = null;
                String outTime = null;

                try {
                    if (inTimeRaw != null && !inTimeRaw.isEmpty() && !"NA".equalsIgnoreCase(inTimeRaw)) {
                        LocalDateTime inDateTime = LocalDateTime.parse(inTimeRaw, inputFormatter);
                        inTime = inDateTime.format(outputFormatter);
                    }
                    if (outTimeRaw != null && !outTimeRaw.isEmpty() && !"NA".equalsIgnoreCase(outTimeRaw)) {
                        LocalDateTime outDateTime = LocalDateTime.parse(outTimeRaw, inputFormatter);
                        outTime = outDateTime.format(outputFormatter);
                    }
                } catch (Exception e) {
                    // Handle invalid format if needed
                    e.printStackTrace();
                }

                TimesheetDataDTO dayData = new TimesheetDataDTO();
                dayData.setStatus(status);
                dayData.setInTime(inTime);
                dayData.setOutTime(outTime);

                timesheetData.put("d" + (i + 1), dayData);
            }

            dto.setTimesheetData(timesheetData);
            dto.setEmploymentId(obj[114] != null ? obj[114].toString() : null);

            dtoList.add(dto);
        }

        return dtoList;
    }
}

