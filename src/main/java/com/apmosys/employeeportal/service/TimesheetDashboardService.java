package com.apmosys.employeeportal.service;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.apmosys.employeeportal.config.CacheConfig;

import com.apmosys.employeeportal.dto.DepartmentCountDTO;
import com.apmosys.employeeportal.dto.DepartmentStatusSummaryDTO;
import com.apmosys.employeeportal.dto.DepartmentWiseStatusDTO;
import com.apmosys.employeeportal.dto.GetEmployeeByNameAndEmpldDTO;
import com.apmosys.employeeportal.dto.GetEmployeeSummaryOnExportDTO;
import com.apmosys.employeeportal.dto.GetEmployeeTimesheetAsCalenderByProjectIdDTO;
import com.apmosys.employeeportal.dto.GetEmployeeTimesheetAsCalenderDTO;
import com.apmosys.employeeportal.dto.GetProjectByMonthRangeAndEmpIdDTO;
import com.apmosys.employeeportal.dto.GetProjectViewForClientAttendanceStatusDTO;
import com.apmosys.employeeportal.dto.ProjectIdAndNameDTO;
import com.apmosys.employeeportal.dto.LastTimesheetFieldDto;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDashboardCountDTO;
import com.apmosys.employeeportal.dto.TimesheetDashboardResponseDTO;
import com.apmosys.employeeportal.dto.TimesheetDocumentApprovalDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.ActivityTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.EmployeeTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.LocationSessionDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.ProjectTimesheetDTO;
import com.apmosys.employeeportal.model.TimesheetDataDTO;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTimesheetsNewRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.TimesheetDocumentApprovalRepository;
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
    private EmployeeTimesheetsNewRepository timesheetsNewRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectHierarchyResolverService projectHierarchyResolverService;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private TimesheetDocumentApprovalRepository timesheetDocumentApprovalRepository;
    
    @Autowired
    private LogService logService;
    
    @Autowired
    private HttpServletRequest httpRequest;
    

	@PersistenceContext
    private EntityManager entityManager;

    private String resolveTimesheetApiUrl(String legacyFallback) {
        if (httpRequest != null) {
            String path = httpRequest.getServletPath();
            if (path != null && !path.isEmpty()) {
                return path;
            }
        }
        return legacyFallback;
    }

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
    @Cacheable(cacheNames = CacheConfig.CACHE_TIMESHEET_DASHBOARD_COUNT, keyGenerator = "timesheetDashboardCacheKeyGenerator")
    public ServiceResponse getTimesheetDashboardCountForEmployee(Integer month, Integer year,Long empId,Boolean isClientDashboard,List<String> billableTypes,String employeeActive,String clientSideFilter, String multiPOs) {
        ServiceResponse response = new ServiceResponse();
        System.out.println("DB CALL EXECUTED--CACHE TEST-101");

        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("getTimesheetDashboardCountForEmployee");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("getTimesheetDashboardCountForEmployee");
        
        try {
        	List<Object[]> summaryRows;
            if (isClientDashboard) {
            	summaryRows = timesheetsNewRepository.getTimesheetDashboardCountForEmployee(
                		 month, year, empId, clientSideFilter,employeeActive, billableTypes,multiPOs);
            } else {
            	summaryRows = timesheetsNewRepository.getTimesheetDashboardCountForAllEmployee(month, year, empId, billableTypes, employeeActive);
            }
            
            if (summaryRows.isEmpty()) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Unable to fetch the dashboard count for employee!");
                apiLogInfo.setApiResponse("Failed to fetch the dashboard count for employee \n");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            } else {
            	Object[] row = summaryRows.get(0);

    	        TimesheetDashboardCountDTO summary = new TimesheetDashboardCountDTO();
    	        summary.setTotalApplicableCount(getInt(row[0]));
    	        summary.setApprovedCount(getInt(row[1]));
    	        summary.setClientSidePendingCount(getInt(row[2]));
    	        summary.setDefaulterCount(getInt(row[3]));
    	        summary.setTotaldefaulterCount(getInt(row[4]));
                
                /* ================= DEPARTMENT LIST ================= */
//    	        List<Object[]> deptList = timesheetsRepository.getDepartmentList( month, year, empId, "All", clientSideFilter);

    	        Map<String, DepartmentWiseStatusDTO> departmentWise =
    	                initDepartmentBuckets();

    	        /* ================= DEPT WISE COUNTS ================= */
//    	        for (Object[] deptRow : deptList) {
//
//    	            Long deptId = ((Number) deptRow[1]).longValue();
//    	            String deptName = (String) deptRow[0];
//    	            String deptCode = (String) deptRow[2];
//
//    	            List<Object[]> counts = timesheetsNewRepository.getDepartmentWiseTimesheetDashboard( month, year, empId, deptId, clientSideFilter);
//
//    	            Object[] c = counts.get(0);
//
//    	            int total = getInt(c[0]);
//    	            int approved = getInt(c[1]);
//    	            int pending = getInt(c[2]);
//    	            int defaulter = getInt(c[3]);
//
//    	            addDept(departmentWise.get("All"), deptId, deptCode, deptName, total);
//
//    	            if (approved > 0) {
//    	                addDept(departmentWise.get("Approved"), deptId, deptCode, deptName, approved);
//    	            }
//
//    	            if (pending > 0) {
//    	                addDept(departmentWise.get("ClientSidePending"), deptId, deptCode, deptName, pending);
//    	            }
//
//    	            if (defaulter > 0) {
//    	                addDept(departmentWise.get("Defaulter"), deptId, deptCode, deptName, defaulter);
//    	            }
//    	        }
                
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(new TimesheetDashboardResponseDTO(summary, departmentWise));
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

    private void addDept(DepartmentWiseStatusDTO bucket, Long deptId, String deptCode, String deptName, int count) 
	{
	    bucket.getDepartments().add(
	        new DepartmentCountDTO(deptId, deptCode, deptName, count)
	    );
	    bucket.setTotal(bucket.getTotal() + count);
	}
    
    private int getInt(Object o) {
	    return o == null ? 0 : ((Number) o).intValue();
	}
    
    private Map<String, DepartmentWiseStatusDTO> initDepartmentBuckets() {
	    Map<String, DepartmentWiseStatusDTO> map = new LinkedHashMap<>();
	    map.put("All", new DepartmentWiseStatusDTO(0, new ArrayList<>()));
	    map.put("Approved", new DepartmentWiseStatusDTO(0, new ArrayList<>()));
	    map.put("ClientSidePending", new DepartmentWiseStatusDTO(0, new ArrayList<>()));
	    map.put("Defaulter", new DepartmentWiseStatusDTO(0, new ArrayList<>()));
	    return map;
	}
    
    
    
    public ServiceResponse getDepartmentStatusSummary(GetEmployeeSummaryOnExportDTO requestDTO) {

	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("getDepartmentStatusSummary");
	    apiLogInfo.setApiUrl("/api/getDepartmentStatusSummary");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder("Received request for Department Status Summary");

	    if (requestDTO != null) {
	        logBuilder.append(" | ProjectId: ").append(requestDTO.getProjectId())
	                  .append(" | Month: ").append(requestDTO.getMonth())
	                  .append(" | Year: ").append(requestDTO.getYear());
	    }

	    apiLogInfo.setApiRequest(logBuilder.toString());

	    try {

	        if (requestDTO == null) {
	            throw new IllegalArgumentException("Request body cannot be null.");
	        }
	        
	        String clientSideFilter = "All";
	        
	        Integer month = null;

	        if (requestDTO.getMonth() != null ) {
	            month = requestDTO.getMonth();
	        }

	        List<Object[]> data = timesheetsNewRepository.getDepartmentStatusSummary(
	                requestDTO.getEmpId(),
	                month,
	                requestDTO.getYear(),
	                clientSideFilter, requestDTO.getBillableType(),requestDTO.getEmployeeActive(),requestDTO.getMultiPOs()             
	        );

	        if (data == null || data.isEmpty()) {

	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceMessage("No department summary data found.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiResponse("No data returned from repository.");

	            return response;
	        }

	        List<DepartmentStatusSummaryDTO> result = new ArrayList<>();

	        for (Object[] row : data) {

	            DepartmentStatusSummaryDTO dto = new DepartmentStatusSummaryDTO();
	            dto.setDept(row[0] != null ? row[0].toString() : null);
	            dto.setTotal(row[1] != null ? ((Number) row[1]).intValue() : 0);
	            dto.setReady(row[2] != null ? ((Number) row[2]).intValue() : 0);
	            dto.setPending(row[3] != null ? ((Number) row[3]).intValue() : 0);
	            dto.setDefaulter(row[5] != null ? ((Number) row[5]).intValue() : 0);
	            dto.setNotFilled(row[4] != null ? ((Number) row[4]).intValue() : 0);
	            dto.setDeptId(row[6] != null ? ((Number) row[6]).longValue() : 0L);
	            dto.setApprovedRepeat(row[7] != null ? ((Number) row[7]).intValue() : 0);
	            dto.setPendingRepeat(row[8] != null ? ((Number) row[8]).intValue() : 0);
	            dto.setDefaulterRepeat(row[9] != null ? ((Number) row[9]).intValue() : 0);	            
	            
	            
	            result.add(dto);
	        }

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(result);
	        response.setServiceMessage("Department status summary fetched successfully.");

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        apiLogInfo.setApiResponse("Fetched " + result.size() + " department record(s).");

	    } catch (IllegalArgumentException ex) {
	    	
	    	ex.printStackTrace();
	    	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceMessage(ex.getMessage());
	        response.setServiceError(ex.toString());

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setApiResponse("Validation Error: " + ex.getMessage());
	        apiLogInfo.setLogLevel("WARN");

	    } catch (Exception ex) {
	    	ex.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceMessage("Unexpected error occurred while fetching department summary.");
	        response.setServiceError(ex.toString());

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setApiResponse("Unexpected Exception: " + ex.getMessage());
	        apiLogInfo.setLogLevel("ERROR");

	    } finally {
	        logService.logMyInfo(httpRequest, apiLogInfo);
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
        apiLogInfo.setApiUrl(resolveTimesheetApiUrl("/api/getTimesheetDashboardCountForProject"));
        apiLogInfo.setLogLevel("INFO");
        apiLogInfo.setEmpId(empId);
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("getTimesheetDashboardCountForProject");
        
        try {
            List<Object[]> countForProject;
            if (isClientDashboard) {
                countForProject = timesheetsNewRepository.getTimesheetDashboardCountForProject(
                        month, year, empId);
            } else {
                countForProject = timesheetsNewRepository.getAllEmpTimesheetDashboardCountForProject(
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
            apiLogInfo.setApiRequest(logBuilder.toString());
            logService.logMyInfo(httpRequest, apiLogInfo);
        }
        
        return response;
    }

    /**
     * Gets total VMS filled count.
     * 
     * @param timesheetDTO Contains empId
     * @return ServiceResponse with timesheet list
     */
//    public ServiceResponse totalVmsFilledCount(TimesheetDTO timesheetDTO) {
//        ServiceResponse response = new ServiceResponse();
//        LogDTO apiLogInfo = new LogDTO();
//        apiLogInfo.setApiUrl("/api/getEmployeeListByProjectId");
//        apiLogInfo.setLogLevel("INFO");
//        
//        try {
//            Long empId = Long.valueOf(timesheetDTO.getEmpId());
//            List<Object[]> timesheetList = timesheetsRepository.getTotalVmsFilledCount(empId);
//
//            if (timesheetList == null || timesheetList.isEmpty()) {
//                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//                response.setServiceResponse("No VMS Filled!");
//                response.setServiceMessage("No VMS Filled!");
//                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//                logService.logMyInfo(httpRequest, apiLogInfo);
//                return response;
//            }
//            
//            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//            response.setServiceResponse(timesheetList);
//            response.setServiceMessage("TimesheetList List fetched successfully!");
//            apiLogInfo.setApiResponse("Timesheet List fetched successfully!");
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
     * Gets total Ishine filled count.
     * 
     * @param status Status filter
     * @return ServiceResponse with timesheet list
     */
//    public ServiceResponse totalIshineFilledCount(String status) {
//        ServiceResponse response = new ServiceResponse();
//        LogDTO apiLogInfo = new LogDTO();
//        apiLogInfo.setApiUrl("/api/getEmployeeListByProjectId");
//        apiLogInfo.setLogLevel("INFO");
//        
//        try {
//            List<Object[]> ishineTimesheetList = timesheetsRepository.totalIshineFilledCount();
//            
//            if (ishineTimesheetList == null || ishineTimesheetList.isEmpty()) {
//                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//                response.setServiceResponse("No Timesheet Filled!");
//                response.setServiceMessage("No Timesheet Filled!");
//                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//                logService.logMyInfo(httpRequest, apiLogInfo);
//                return response;
//            }
//            
//            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//            response.setServiceResponse(ishineTimesheetList);
//            response.setServiceMessage("TimesheetList List fetched successfully!");
//            apiLogInfo.setApiResponse("Timesheet List fetched successfully!");
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
     * Gets total VMS not filled.
     * 
     * @param timesheetDTO Contains empId
     * @return ServiceResponse with timesheet list
     */
//    public ServiceResponse totalvmsNotFilled(TimesheetDTO timesheetDTO) {
//        ServiceResponse response = new ServiceResponse();
//        LogDTO apiLogInfo = new LogDTO();
//        apiLogInfo.setApiUrl("/api/getEmployeeListByProjectId");
//        apiLogInfo.setLogLevel("INFO");
//        
//        try {
//            List<Object[]> timesheetList = timesheetsRepository.totalvmsNotFilled(timesheetDTO.getEmpId());
//            
//            if (timesheetList == null || timesheetList.isEmpty()) {
//                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//                response.setServiceResponse("No VMS Filled!");
//                response.setServiceMessage("No VMS Filled!");
//                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//                logService.logMyInfo(httpRequest, apiLogInfo);
//                return response;
//            }
//            
//            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//            response.setServiceResponse(timesheetList);
//            response.setServiceMessage("TimesheetList List fetched successfully!");
//            apiLogInfo.setApiResponse("Timesheet List fetched successfully!");
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
     * Gets total Ishine not filled count.
     * 
     * @param timesheetDTO Contains empId and isClientDashboard flag
     * @return ServiceResponse with timesheet list
     */
//    public ServiceResponse totalIshineNotFilledCount(TimesheetDTO timesheetDTO) {
//        ServiceResponse response = new ServiceResponse();
//        LogDTO apiLogInfo = new LogDTO();
//        apiLogInfo.setApiUrl("/api/getEmployeeListByProjectId");
//        apiLogInfo.setLogLevel("INFO");
//        
//        try {
//            List<Object[]> ishineTimesheetList;
//            if (timesheetDTO.getIsClientDashboard()) {
//                ishineTimesheetList = timesheetsRepository.totalIshineNotFilledCount(timesheetDTO.getEmpId());
//            } else {
//                ishineTimesheetList = timesheetsRepository.totalIshineNotFilledCountForAllEmpDash(timesheetDTO.getEmpId());
//            }
//            
//            if (ishineTimesheetList == null || ishineTimesheetList.isEmpty()) {
//                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//                response.setServiceResponse("No Timesheet Filled!");
//                response.setServiceMessage("No Timesheet Filled!");
//                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//                logService.logMyInfo(httpRequest, apiLogInfo);
//                return response;
//            }
//            
//            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//            response.setServiceResponse(ishineTimesheetList);
//            response.setServiceMessage("TimesheetList List fetched successfully!");
//            apiLogInfo.setApiResponse("Timesheet List fetched successfully!");
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
            Boolean flag = timesheetsNewRepository.checkProjectIsClientApplicable(projectId);
            
            if (flag == null || flag == false) {
                empTimesheet = timesheetsNewRepository.getEmployeeTimesheetAsCalenderForAllEmp(
                        empId, month, year, projectId);
            } else {
                empTimesheet = timesheetsNewRepository.getEmployeeTimesheetAsCalender(
                        empId, month, year, projectId);
            }
            
            List<GetEmployeeTimesheetAsCalenderDTO> dtoList = buildCalendarDTOList(empTimesheet);

            if (dtoList.isEmpty()) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("No timesheet Data available for Employee !!!");
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
                empTimesheet = timesheetsNewRepository.getEmployeeSummaryReportAll(
                        object.getMonth(), object.getYear(), object.getEmpId(), object.getBillableType());
            } else {
                empTimesheet = timesheetsNewRepository.getEmployeeSummaryReportClientSideApplicable(
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
//    public ServiceResponse getLastFilledTimesheetByEmp(Long empId) {
//        ServiceResponse response = new ServiceResponse();
//        LogDTO apiLogInfo = new LogDTO();
//        apiLogInfo.setApiUrl("/api/getLastFilledTimesheetByEmp");
//        apiLogInfo.setLogLevel("INFO");
//        
//        try {
////            List<Object[]> activeCheckList = timesheetsRepository.checkEmployeeActiveOrNot(empId);
//        	Optional<Integer> exsistsTimesheet = timesheetsRepository.existsTimesheetByEmpId(empId);
//        	
//        	
//
//            // Case 1: No records found in the timesheet - employee never filled any timesheet
//            if (!exsistsTimesheet.isPresent()) {
//                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//                response.setServiceResponse(Collections.emptyList());
//                response.setServiceMessage("Employee has never filled any timesheet.");
//                response.setServiceResponse1("No Timesheet");
//                apiLogInfo.setApiResponse("No timesheet record found for employee.");
//                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//                logService.logMyInfo(httpRequest, apiLogInfo);
//                return response;
//            }
//            Optional<Integer> activeCheckList = timesheetsRepository.isEmployeeActive(empId);
//            // Determine active status from the activeCheckList result
//            boolean isActive = activeCheckList.isPresent();
//            response.setServiceResponse1(isActive ? "Active" : "Not Active");
//
//            if (!isActive) {
//                // Case 2: Employee was in a project but is not currently active
//                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//                response.setServiceResponse(Collections.emptyList());
//                response.setServiceMessage("Employee is not active on previous project.");
//                apiLogInfo.setApiResponse("Employee is Not Active");
//                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//                logService.logMyInfo(httpRequest, apiLogInfo);
//                return response;
//            }
//            
////            List<Object[]> resultList = timesheetsRepository.getLastTimesheetFiledByEmpId(empId);
//            List<Object[]> resultList = timesheetsRepository.getLastFilledTimesheetByEmp(empId);
//            
//            if (!resultList.isEmpty()) {
//            	Map<Long, EmployeeTimesheetDTO> timesheetMap = new LinkedHashMap<>();
//            	resultList.forEach(row -> {
//
//            	    /* ================= TIMESHEET ================= */
//            	    Long timesheetId = ((Number) row[1]).longValue();
//
//            	    EmployeeTimesheetDTO timesheet =
//            	        timesheetMap.computeIfAbsent(timesheetId, id -> {
//            	            EmployeeTimesheetDTO dto = new EmployeeTimesheetDTO();
//            	            dto.setEmpId(((Number) row[0]).longValue());
//            	            dto.setTimesheetId(id);
//            	            dto.setDate(((java.sql.Date) row[2]).toLocalDate());
//
//            	            dto.setWorkCheckIn(
//            	                row[3] != null
//            	                    ? (row[3]).toString()
//            	                    : null
//            	            );
//            	            dto.setWorkCheckOut(
//            	                row[4] != null
//            	                    ? (row[4]).toString()
//            	                    : null
//            	            );
//
//            	            dto.setDayType(row[25] != null ? row[25].toString() : null);
//
////            	            dto.setEmployementId(row[15] != null ? row[15].toString() : null);
//
////            	            dto.setTimesheetLockUpdatedOn(
////            	                row[23] != null
////            	                    ? ((Timestamp) row[23]).toLocalDateTime()
////            	                    : null
////            	            );
////
////            	            dto.setIsTimesheetLockCheckEnable(
////            	                row[24] != null ? row[24].toString() : null
////            	            );
//
//            	            dto.setLocationSessions(new ArrayList<>());
//            	            return dto;
//            	        });
//
//            	    /* ================= LOCATION ================= */
//            	    Long locationMappingId = ((Number) row[5]).longValue();
//
//            	    LocationSessionDTO location =
//            	        timesheet.getLocationSessions()
//            	                 .stream()
//            	                 .filter(l -> locationMappingId.equals(l.getLocationMappingId()))
//            	                 .findFirst()
//            	                 .orElseGet(() -> {
//            	                     LocationSessionDTO l = new LocationSessionDTO();
//            	                     l.setLocationMappingId(locationMappingId);
//
//            	                     l.setLocationInTime(
//            	                         row[6] != null
//            	                             ? (row[6]).toString()
//            	                             : null
//            	                     );
//            	                     l.setLocationOutTime(
//            	                         row[7] != null
//            	                             ? (row[7]).toString()
//            	                             : null
//            	                     );
//
//            	                     l.setWorkLocationTypeId(((Number) row[8]).intValue());
//            	                     l.setWorkLocationType(row[9].toString());
//
//            	                     l.setProjects(new ArrayList<>());
//            	                     timesheet.getLocationSessions().add(l);
//            	                     return l;
//            	                 });
//
//            	    /* ================= PROJECT ================= */
//            	    Long projectId = ((Number) row[10]).longValue();
//            	    Long clientLocationId = row[30] != null ? ((Number) row[30]).longValue() : null;
//
//            	    ProjectTimesheetDTO project =
//            	        location.getProjects()
//            	                .stream()
//            	                .filter(p -> projectId.equals(p.getProjectId().longValue()) &&
//            	                		 Objects.equals(clientLocationId, p.getClientLocationId())
//            	                		)
//            	                .findFirst()
//            	                .orElseGet(() -> {
//            	                    ProjectTimesheetDTO p = new ProjectTimesheetDTO();
//            	                    p.setTimesheetId(timesheetId);
//            	                    p.setProjectId(projectId.intValue());
//
//            	                    p.setProjectName(row[26] != null ? row[26].toString() : null);
////            	                    p.setClientName(row[27] != null ? row[27].toString() : null);
//            	                    p.setClientSideId(row[28] != null ? row[28].toString() : null);
////            	                    p.setClientLocation(row[29] != null ? row[29].toString() : null);
//
//            	                    p.setPoNo(row[11] != null ? row[11].toString() : null);
//            	                    p.setPoId(row[12] != null ? ((Number) row[12]).longValue() : null);
//            	                    p.setStatus(((Number) row[13]).intValue());
//            	                    p.setClientApprovalStatus(
//            	                        row[14] != null ? ((Number) row[14]).intValue() : null
//            	                    );
//
//            	                    p.setTeamId(((Number) row[20]).longValue());
////            	                    p.setTeamName(row[21] != null ? row[21].toString() : null);
////            	                    p.setTeamActive(
////            	                        row[22] != null && ((Number) row[22]).intValue() == 1
////            	                    );
//
//            	                    p.setClientLocationId(row[30] != null ? ((Number) row[30]).longValue():null);
//            	                    p.setClientId(row[31]!=null?((Number)row[31]).longValue():null);
//            	                    p.setActivities(new ArrayList<>());
//            	                    location.getProjects().add(p);
//            	                    return p;
//            	                });
//
//            	    /* ================= ACTIVITY ================= */
//            	    ActivityTimesheetDTO activity = new ActivityTimesheetDTO();
//            	    activity.setActivityId(((Number) row[16]).longValue());
//            	    activity.setActivity(row[17].toString());
//            	    activity.setDurationMinutes(
//            	        row[18] != null ? ((Short) row[18]) : null
//            	    );
//            	    activity.setDescription(
//            	        row[19] != null ? row[19].toString() : null
//            	    );
//
//            	    project.getActivities().add(activity);
//            	});
//
//            	// FINAL RESPONSE
//            	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//            	response.setServiceResponse(
//            	    new ArrayList<>(timesheetMap.values())
//            	);
//            	response.setServiceMessage("Last timesheet found for employee.");
//            	
//            } else {
//                // Case 4: Active employee but no timesheet found
//                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//                response.setServiceResponse(Collections.emptyList());
//                response.setServiceMessage("No timesheet found for employee.");
//                apiLogInfo.setApiResponse("No timesheet found");
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
//        logService.logMyInfo(httpRequest, apiLogInfo);
//        return response;
//    }

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
            
            List<Object[]> projList = timesheetsNewRepository.getProjectByMonthRangeAndEmpId(
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
    
    public ServiceResponse getProjectViewForClientAttendanceStatus(@RequestBody TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl(resolveTimesheetApiUrl("/api/getProjectViewForClientAttendanceStatus"));
	    apiLogInfo.setSubFeatureName("getProjectViewForClientAttendanceStatus");
	    apiLogInfo.setLogLevel("INFO");
	    
	    String projectName = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getProjectName());
	    String poNo = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getPoNo());
	    String projectManagerName = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getProjectManagerName());
	    String projectType = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getProjectType());
	    String clientName = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getClientName());
	    String apmosysRM = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getApmosysRm());
	    String apmosysRMEmail = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getApmosysRmEmail());
	    String clientRM = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getClientRm());
	    Integer totalExpectedFillCount = getIntegerColumnFilterValue(timesheetDTO.getColumnFilter().getTotalExpectedFillCount());
	    Integer totalClientSideApprovedCount = getIntegerColumnFilterValue(timesheetDTO.getColumnFilter().getTotalClientSideApprovedCount());
	    Integer totalClientSidePendingCount = getIntegerColumnFilterValue(timesheetDTO.getColumnFilter().getTotalClientSidePendingCount());
	    Integer totalClientSideNotFilledCou = getIntegerColumnFilterValue(timesheetDTO.getColumnFilter().getTotalClientSideNotFilledCount());
	    Integer totalEmployees = getIntegerColumnFilterValue(timesheetDTO.getColumnFilter().getTotalEmployees());
	    String active = null;
	    String activeValue = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getActive());
	    String startDate = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getStartDate());
	    String endDate = getStringColumnFilterValue(timesheetDTO.getColumnFilter().getEndDate());
	    if (activeValue != null) {  
	        String lower = activeValue.trim().toLowerCase();

	        // check explicit full-word containment first for safety
	        if (lower.contains("inactive")) {
	            active = "false";
	        } else if (lower.contains("active")) {
	            // only reached if "inactive" wasn't present (avoids misclassifying "inactive")
	            active = "true";
	        } else {
	            // accept short partial prefixes commonly typed by users
	            if (lower.startsWith("in") || lower.startsWith("ina") || lower.startsWith("inac")) {
	                active = "false";
	            } else if (lower.startsWith("a") || lower.startsWith("ac") || lower.startsWith("act")) {
	                active = "true";
	            }
	        }
	    }

	    
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append( "getProjectViewForClientAttendanceStatus: \n");
		 try {
			 List<Object[]> resultList;
			 int page = timesheetDTO.getPage(); 
			 int pageSize = timesheetDTO.getSize();
			 int offset = (page-1) * pageSize;
	         String sortBy=timesheetDTO.getSortBy();
	         String sortDirection=timesheetDTO.getSortDirection();

	         // Link-aware project name search:
	         // If user searches by a linked/deleted project name, resolve it to primary project ids and
	         // filter by ids (so aggregation remains by primary project and counts/cards stay consistent).
	         List<Integer> primaryProjectIdsForSearch = null;
	         String originalProjectNameSearch = projectName;
	         if (projectName != null && !projectName.trim().isEmpty()) {
	        	 List<Integer> matchedByName = projectRepository.findProjectIdsByProjectNameLike(projectName.trim());
	        	 boolean linkedHit = matchedByName != null && matchedByName.stream()
	        			 .filter(Objects::nonNull)
	        			 .anyMatch(id -> !Objects.equals(id, projectHierarchyResolverService.resolveToPrimaryProjectId(id)));

	        	 primaryProjectIdsForSearch = resolvePrimaryProjectIdsByProjectNameContains(projectName);
	        	 if (primaryProjectIdsForSearch.isEmpty()) {
	        		 response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        		 response.setServiceResponse("No data found from database");
	        		 return response;
	        	 }
	        	 if (linkedHit) {
	        		 response.setServiceResponse1("Your search matched a linked project name. Rows marked with the link icon are the primary project(s) for those matches. Search text: "
	        				 + originalProjectNameSearch);
	        		 response.setServiceResponse2(buildLinkedProjectSearchMetadata(matchedByName));
					 projectName = null;
	        	 } else {
	        		 response.setServiceResponse2(null);
	        	 }
	        	 // Avoid filtering by primary project's name; ids are authoritative
	        	 
	         }

 				if (timesheetDTO.getDataForExcel()) {
 					if (timesheetDTO.getIsClientDashboard()) {
 						resultList = timesheetsNewRepository.getProjectViewForClientAttendanceStatus(timesheetDTO.getMonth1(),
 								timesheetDTO.getYear(), timesheetDTO.getStatus(), timesheetDTO.getEmpId(),
 								(primaryProjectIdsForSearch != null && !primaryProjectIdsForSearch.isEmpty()) ? 1 : 0,
 								(primaryProjectIdsForSearch != null && !primaryProjectIdsForSearch.isEmpty()) ? primaryProjectIdsForSearch
 										: Collections.singletonList(-1),
 								projectName,
 								poNo,startDate,endDate, projectManagerName, projectType, clientName, apmosysRM, apmosysRMEmail, clientRM,
 								totalExpectedFillCount, totalClientSideApprovedCount, totalClientSidePendingCount,
 								totalClientSideNotFilledCou, totalEmployees, active, sortBy, sortDirection, 0,
 								Integer.MAX_VALUE);
 					} else {
 						resultList = timesheetsNewRepository.getProjectViewForAllEmpAttendanceStatus(timesheetDTO.getMonth1(),
 								timesheetDTO.getYear(), timesheetDTO.getStatus(), timesheetDTO.getEmpId(),
 								timesheetDTO.getBillableTypes(), timesheetDTO.getProjectActive(), projectName, poNo,startDate,endDate,
 								projectManagerName, projectType, clientName, apmosysRM, apmosysRMEmail, clientRM,
 								totalExpectedFillCount, totalClientSideApprovedCount, totalClientSidePendingCount,
 								totalClientSideNotFilledCou, totalEmployees, sortBy, sortDirection, 0, Integer.MAX_VALUE);
 					}
 				} else {
 					if (timesheetDTO.getIsClientDashboard()) {
 						System.out.println("inside iffffffff");
 						resultList = timesheetsNewRepository.getProjectViewForClientAttendanceStatus(timesheetDTO.getMonth1(),
 								timesheetDTO.getYear(), timesheetDTO.getStatus(), timesheetDTO.getEmpId(),
 								(primaryProjectIdsForSearch != null && !primaryProjectIdsForSearch.isEmpty()) ? 1 : 0,
 								(primaryProjectIdsForSearch != null && !primaryProjectIdsForSearch.isEmpty()) ? primaryProjectIdsForSearch
 										: Collections.singletonList(-1),
 								projectName,
 								poNo,startDate,endDate, projectManagerName, projectType, clientName, apmosysRM, apmosysRMEmail, clientRM,
 								totalExpectedFillCount, totalClientSideApprovedCount, totalClientSidePendingCount,
 								totalClientSideNotFilledCou, totalEmployees, active, sortBy, sortDirection, offset,
 								pageSize);
 					} else {
 						resultList = timesheetsNewRepository.getProjectViewForAllEmpAttendanceStatus(timesheetDTO.getMonth1(),
 								timesheetDTO.getYear(), timesheetDTO.getStatus(), timesheetDTO.getEmpId(),
 								timesheetDTO.getBillableTypes(), timesheetDTO.getProjectActive(), projectName, poNo,startDate,endDate,
 								projectManagerName, projectType, clientName, apmosysRM, apmosysRMEmail, clientRM,
 								totalExpectedFillCount, totalClientSideApprovedCount, totalClientSidePendingCount,
 								totalClientSideNotFilledCou, totalEmployees, sortBy, sortDirection, offset, pageSize);
 					}
 				}



			
			 Integer totalItems = ((BigInteger) entityManager.createNativeQuery("SELECT FOUND_ROWS()").getSingleResult()).intValue();
			 
			 if(resultList.isEmpty()) {
			        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			        response.setServiceResponse("No data found from database");

			        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			        apiLogInfo.setApiResponse("Empty list received from repository."); 
			        apiLogInfo.setLogLevel("ERROR");
			        
			        logService.logMyInfo(httpRequest, apiLogInfo);
					return response;
			 }

	        List<GetProjectViewForClientAttendanceStatusDTO> dtoList = new ArrayList<>();
	        
	        for (Object[] obj : resultList){
	            GetProjectViewForClientAttendanceStatusDTO dto = new GetProjectViewForClientAttendanceStatusDTO();
	            dto.setProjectId(obj[0] != null ? Integer.parseInt(obj[0].toString()) : null);
	            dto.setProjectName(obj[1] != null ? obj[1].toString() : null);
	            dto.setProjectManagerName(obj[2] != null ? obj[2].toString() : null);
	            dto.setPoNo(obj[3] != null ? obj[3].toString() : null);
	            dto.setProjectType(obj[4] != null ? obj[4].toString() : null);
	            dto.setClientName(obj[5] != null ? obj[5].toString() : null);
	            dto.setApmosysRM(obj[6] != null ? obj[6].toString() : null);
	            dto.setApmosysRMEmail(obj[7] != null ? obj[7].toString() : null);
	            dto.setClientRM(obj[8] != null ? obj[8].toString() : null);
	            dto.setTotalExpectedFillCount(obj[9] != null ? Integer.parseInt(obj[9].toString()) : 0);
//	            dto.setTotalIshineFilledCount(obj[10] != null ? Integer.parseInt(obj[10].toString()) : 0);
	            dto.setTotalClientSideNotFilledCount(obj[10] != null ? Integer.parseInt(obj[10].toString()) : 0);
	            dto.setTotalClientSidePendingCount(obj[11] != null ? Integer.parseInt(obj[11].toString()) : 0);
	            dto.setTotalClientSideApprovedCount(obj[12] != null ? Integer.parseInt(obj[12].toString()) : 0);
	            dto.setClientSideApprovedPercent(obj[14] != null ? Double.parseDouble(obj[14].toString()) : 0.0);
	            dto.setClientSidePendingPercent(obj[15] != null ? Double.parseDouble(obj[15].toString()) : 0.0);
	            dto.setClientSideNotFilledPercent(obj[16] != null ? Double.parseDouble(obj[16].toString()) : 0.0);
	            dto.setTotalEmployees(obj[17] != null ? Integer.parseInt(obj[17].toString()) : 0);
	            dto.setActive(obj[18] != null ? obj[18].toString() : null);
	            dto.setStartDate(obj[19] != null ? obj[19].toString() : null);
	            dto.setEndDate(obj[20] != null ? obj[20].toString() : null);
	            dtoList.add(dto);
	        }
	        
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(dtoList);
	        response.setTotalElements(totalItems);

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        apiLogInfo.setApiResponse("Fetched " + dtoList.size() + " records successfully.");
	        
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
		 logService.logMyInfo(httpRequest, apiLogInfo);
		 return response;
	}

	private List<Integer> resolvePrimaryProjectIdsByProjectNameContains(String projectName) {
		if (projectName == null || projectName.trim().isEmpty()) {
			return Collections.emptyList();
		}
		List<Integer> matched = projectRepository.findProjectIdsByProjectNameLike(projectName.trim());
		if (matched == null || matched.isEmpty()) {
			return Collections.emptyList();
		}
		return matched.stream()
				.filter(Objects::nonNull)
				.map(projectHierarchyResolverService::resolveToPrimaryProjectId)
				.filter(Objects::nonNull)
				.distinct()
				.collect(Collectors.toList());
	}

	/**
	 * Metadata for linked-name search: primary ids for row markers, plus matched non-primary project names
	 * grouped by resolved primary (supports chains P1→…→Pn).
	 */
	private Map<String, Object> buildLinkedProjectSearchMetadata(List<Integer> matchedByName) {
		LinkedHashSet<Integer> linkedPrimaryIds = new LinkedHashSet<>();
		LinkedHashSet<Integer> contributorIds = new LinkedHashSet<>();
		if (matchedByName != null) {
			for (Integer mid : matchedByName) {
				if (mid == null) {
					continue;
				}
				Integer pid = projectHierarchyResolverService.resolveToPrimaryProjectId(mid);
				if (pid != null && !Objects.equals(mid, pid)) {
					linkedPrimaryIds.add(pid);
					contributorIds.add(mid);
				}
			}
		}
		Map<Integer, String> idToName = new HashMap<>();
		if (!contributorIds.isEmpty()) {
			List<ProjectIdAndNameDTO> rows = projectRepository
					.findProjectIdAndNameByProjectIdIn(new HashSet<>(contributorIds));
			if (rows != null) {
				for (ProjectIdAndNameDTO row : rows) {
					if (row != null && row.getProjectId() != null) {
						idToName.put(row.getProjectId(), row.getProjectName());
					}
				}
			}
		}
		LinkedHashMap<Integer, LinkedHashSet<String>> namesByPrimary = new LinkedHashMap<>();
		if (matchedByName != null) {
			for (Integer mid : matchedByName) {
				if (mid == null) {
					continue;
				}
				Integer pid = projectHierarchyResolverService.resolveToPrimaryProjectId(mid);
				if (pid == null || Objects.equals(mid, pid)) {
					continue;
				}
				String nm = idToName.get(mid);
				if (nm == null || nm.isBlank()) {
					nm = "Project #" + mid;
				}
				namesByPrimary.computeIfAbsent(pid, k -> new LinkedHashSet<>()).add(nm);
			}
		}
		Map<String, List<String>> matchedLinkedNamesByPrimaryId = new LinkedHashMap<>();
		for (Map.Entry<Integer, LinkedHashSet<String>> e : namesByPrimary.entrySet()) {
			matchedLinkedNamesByPrimaryId.put(String.valueOf(e.getKey()), new ArrayList<>(e.getValue()));
		}
		Map<String, Object> meta = new LinkedHashMap<>();
		meta.put("linkedPrimaryIds", new ArrayList<>(linkedPrimaryIds));
		meta.put("matchedLinkedNamesByPrimaryId", matchedLinkedNamesByPrimaryId);
		return meta;
	}
    private String getStringColumnFilterValue(String columnValue) {
		if(columnValue == null || columnValue.isBlank()) {
			return null;
		}
		return columnValue.toLowerCase();
	}
	
	private Integer getIntegerColumnFilterValue(Integer columnValue) {
		if(columnValue == null || columnValue.toString().isBlank()) {
			return null;
		}
		return columnValue;
	}
	public ServiceResponse getEmployeeSummaryOnExportAccordingToStatus(GetEmployeeSummaryOnExportDTO object) {
		
		ServiceResponse response = new ServiceResponse();
 
		 LogDTO apiLogInfo = new LogDTO();
		 apiLogInfo.setSubFeatureName("getEmployeeSummaryOnExport");
		 apiLogInfo.setApiUrl(resolveTimesheetApiUrl("/api/getEmployeeSummaryOnExport"));
		 apiLogInfo.setLogLevel("INFO");
		 StringBuilder logBuilder = new StringBuilder();
		 logBuilder.append("getEmployeeTimesheetAsCalenderByProjectId");
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
			String projectManagers = getStringColumnFilterValue(object.getFilters().getProjectManagers());
			String clientName = getStringColumnFilterValue(object.getFilters().getClientName());
			String teamName = getStringColumnFilterValue(object.getFilters().getTeamName());
			String department = getStringColumnFilterValue(object.getFilters().getDepartment());
			String employmentStatus = getStringColumnFilterValue(object.getFilters().getEmploymentStatus());
			String projectStatus = getStringColumnFilterValue(object.getFilters().getProjectStatus());
			String statusCode = null;
			if ("Mapped".equalsIgnoreCase(projectStatus)) {
			    statusCode = "1";
			} else if ("Removed".equalsIgnoreCase(projectStatus)) {
			    statusCode = "0";
			} else if ("Approval Pending".equalsIgnoreCase(projectStatus)) {
			    statusCode = "2";
			}
			
			List<Long> employeeIds = new ArrayList<Long>();
			
			if (object.getAllEmp()) {
	        	employeeIds = timesheetsNewRepository.getPaginatedEmployeeIds(
						object.getMonth(),
	                    object.getYear(),
	                    object.getEmpId(),   
	                    object.getBillableType(),
	                    object.getStatus(),object.getEmployeeActive(),employmentId,clientsideId,employeeName,billableType,projectName,poNo,
	                    projectManagers,clientName,teamName,department,statusCode,0,Integer.MAX_VALUE);
	        	
	            empTimesheet = timesheetsNewRepository.getEmployeeSummaryReportAllEMP(
	                    object.getMonth(),
	                    object.getYear(),
	                    object.getEmpId(),   
	                    object.getBillableType(),
	                    object.getStatus(),object.getEmployeeActive(),employmentId,clientsideId,employeeName,billableType,projectName,poNo,
	                    projectManagers,clientName,teamName,department,statusCode,
	                    object.getSortBy(),
	                    object.getSortDirection(),employeeIds);
	        } else {
	        	employeeIds = timesheetsNewRepository.getPaginatedEmployeeIdsForClientAttendance(
	        			object.getMonth(),
	                    object.getYear(),
	                    object.getEmpId(),
	                    object.getStatus(),
						object.getClientSideFilter(),employmentId,clientsideId,employeeName,billableType,projectName,poNo,
	                    projectManagers,clientName,teamName,department,employmentStatus,statusCode,0,Integer.MAX_VALUE,object.getDeptId(), object.getEmployeeActive(), object.getBillableType(),object.getMultiPOs());       	
	            empTimesheet = timesheetsNewRepository.getEmployeeViewForClientAttendanceStatusNew(
	                    object.getMonth(),
	                    object.getYear(),
	                    object.getEmpId(),
	                    object.getStatus(),
						object.getClientSideFilter(),employmentId,clientsideId,employeeName,billableType,projectName,poNo,
	                    projectManagers,clientName,teamName,department,employmentStatus,statusCode,
	                    object.getSortBy(),
	                    object.getSortDirection(),employeeIds,object.getDeptId(),
						object.getEmployeeActive(), object.getBillableType(),object.getMultiPOs()
						);
	        }


					 List<GetEmployeeTimesheetAsCalenderDTO> dtoList = new ArrayList<>();
 
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
				 DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
				 DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("hh:mm a");
 
				 for (int i = 0; i < 31; i++) {
					 int baseIndex = 21 + (i * 3);
					 String status = obj.length > baseIndex && obj[baseIndex] != null ? obj[baseIndex].toString() : null;
					 
					 String inTimeRaw = obj.length > (baseIndex + 1) && obj[baseIndex + 1] != null ? obj[baseIndex + 1].toString() : null;
					 String outTimeRaw = obj.length > (baseIndex + 2) && obj[baseIndex + 2] != null ? obj[baseIndex + 2].toString() : null;
 
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
					 } catch (Exception e) {
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
				 dto.setPresent(obj[115] != null ? obj[115].toString() : null);
				 dto.setWeekOff(obj[116] != null ? obj[116].toString() : null);
				 dto.setHoliday(obj[117] != null ? obj[117].toString() : null);
				 dto.setLeave(obj[118] != null ? obj[118].toString() : null);
				 dto.setCompOff(obj[119] != null ? obj[119].toString() : null);
				 dto.setNa(obj[120] != null ? obj[120].toString() : null);
				 dto.setHalfDay(obj[121] != null ? obj[121].toString() : null);
				 dto.setTotalNoOfDays(obj[122] != null ? obj[122].toString() : null);
				 dto.setApmosysTimesheetFilledCount(obj[123] != null ? Integer.parseInt(obj[123].toString()) : null);
				 dto.setEmploymentStatus(obj[124] != null ? obj[124].toString() : null);
				 dto.setEndDate(obj[125] != null ? obj[125].toString() : null);
				 dto.setReadyForInvoicing(obj[126] != null ? obj[126].toString() : null);
				 if (obj[127] != null) {
					 int active = Integer.parseInt(obj[127].toString());
					 switch (active) {
						 case 1:
							 dto.setProjectStatus("Mapped");
							 break;
						 case 0:
							 dto.setProjectStatus("Removed");
							 break;
						 case 2:
							 dto.setProjectStatus("Approval Pending");
							 break;
						 default:
							 dto.setProjectStatus("Undefined");
							 break;
					 }
				 } else {
					 dto.setProjectStatus("Undefined");
				 }
				 if (obj[128] != null) {
					 String active = obj[128].toString();
					 switch (active) {
						 case "true":
							 dto.setProjectActive("Active");
							 break;
						 case "false":
							 dto.setProjectActive("Inactive");
							 break;
						 default:
							 dto.setProjectActive("Undefined");
							 break;
					 }
				 } else {
					 dto.setProjectActive("Undefined");
				 }
				 
 
				 dtoList.add(dto);
			 }
 
			 if(dtoList.isEmpty()){
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
		  
		 } catch(Exception e) {
					 e.printStackTrace();
					 response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
					 response.setServiceResponse("Something went wrong.");
					 response.setServiceError(e.getMessage());
					 apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					 apiLogInfo.setApiResponse(e.getMessage()); 
					 apiLogInfo.setLogLevel("ERROR");
					 apiLogInfo.setApiRequest(logBuilder.toString());
					 logService.logMyInfo(httpRequest, apiLogInfo);
			 }
			 
		 return response;
	 }
	
	public ServiceResponse getEmployeeTimesheetAsCalenderByProjectId(GetEmployeeTimesheetAsCalenderByProjectIdDTO object) {
		
		   ServiceResponse response = new ServiceResponse();

		    LogDTO apiLogInfo = new LogDTO();
		    apiLogInfo.setSubFeatureName("getEmployeeTimesheetAsCalenderByProjectId");
		    apiLogInfo.setLogLevel("INFO");
		    StringBuilder logBuilder = new StringBuilder();
		    logBuilder.append("getEmployeeTimesheetAsCalenderByProjectId");
		    try {
		    	List<Object[]> empTimesheet;
		    	List<String> poNoFilters = normalizePoNoFilters(object.getPoNo());
		    	LocalDate resolvedFromDate = object.getFromDate();
		    	LocalDate resolvedToDate = object.getToDate();
		    	String poToken = object.getRotatedToken();
		    	if (resolvedFromDate == null || resolvedToDate == null) {
		    		if (object.getYear() != null && object.getMonth() != null) {
		    			YearMonth yearMonth = YearMonth.of(object.getYear(), object.getMonth());
		    			resolvedFromDate = yearMonth.atDay(1);
		    			LocalDate lastDay = yearMonth.atEndOfMonth();
		    			LocalDate today = LocalDate.now();
		    			resolvedToDate = lastDay.isAfter(today) ? today : lastDay;
		    		} else {
		    			LocalDate today = LocalDate.now();
		    			resolvedFromDate = today.withDayOfMonth(1);
		    			resolvedToDate = today;
		    		}
		    	}
		    	if (resolvedFromDate.isAfter(resolvedToDate)) {
		    		LocalDate temp = resolvedFromDate;
		    		resolvedFromDate = resolvedToDate;
		    		resolvedToDate = temp;
		    	}
		    	if(object.getAllEmp()) {
		    		empTimesheet= timesheetsNewRepository.getEmployeeTimesheetAsCalenderByProjectIdForAllEmp(object.getProjectId(),
		    				resolvedFromDate,resolvedToDate,object.getEmpId(),poNoFilters,object.getPoProjectId(), poToken);
		    	} else {
		    		empTimesheet= timesheetsNewRepository.getEmployeeTimesheetAsCalenderByProjectId(object.getProjectId(),
		    				resolvedFromDate,resolvedToDate,object.getEmpId(),poNoFilters,object.getPoProjectId(), poToken);
		    	}
 			
		    	List<GetEmployeeTimesheetAsCalenderDTO> dtoList = new ArrayList<>();

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
		    	    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
		    	    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("hh:mm a");

		    	    for (int i = 0; i < 31; i++) {
		    	        int baseIndex = 21 + (i * 3);
		    	        String status = obj.length > baseIndex && obj[baseIndex] != null ? obj[baseIndex].toString() : null;
		    	        
		    	        String inTimeRaw = obj.length > (baseIndex + 1) && obj[baseIndex + 1] != null ? obj[baseIndex + 1].toString() : null;
		    	        String outTimeRaw = obj.length > (baseIndex + 2) && obj[baseIndex + 2] != null ? obj[baseIndex + 2].toString() : null;

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
		    	        } catch (Exception e) {
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
		    	    dto.setPresent(obj[115] != null ? obj[115].toString() : null);
		    	    dto.setWeekOff(obj[116] != null ? obj[116].toString() : null);
		    	    dto.setHoliday(obj[117] != null ? obj[117].toString() : null);
		    	    dto.setLeave(obj[118] != null ? obj[118].toString() : null);
		    	    dto.setCompOff(obj[119] != null ? obj[119].toString() : null);
		    	    dto.setNa(obj[120] != null ? obj[120].toString() : null);
		    	    dto.setHalfDay(obj[121] != null ? obj[121].toString() : null);
		    	    dto.setTotalNoOfDays(obj[122] != null ? obj[122].toString() : null);
		    	    dto.setApmosysTimesheetFilledCount(obj[123] != null ? Integer.parseInt(obj[123].toString()) : null);
		    	    dto.setEmploymentStatus(obj[124] != null ? obj[124].toString() : null);
		    	    dto.setEndDate(obj[125] != null ? obj[125].toString() : null);
		    	    dto.setReadyForInvoicing(obj[126] != null ? obj[126].toString() : null);
		    	    if (obj[127] != null) {
		    	        int active = Integer.parseInt(obj[127].toString());
		    	        switch (active) {
		    	            case 1:
		    	                dto.setProjectStatus("Mapped");
		    	                break;
		    	            case 0:
		    	                dto.setProjectStatus("Removed");
		    	                break;
		    	            case 2:
		    	                dto.setProjectStatus("Approval Pending");
		    	                break;
		    	            default:
		    	                dto.setProjectStatus("Undefined");
		    	                break;
		    	        }
		    	    } else {
		    	        dto.setProjectStatus("Undefined");
		    	    }

		    	    dtoList.add(dto);
		    	}

		    	if(dtoList.isEmpty()){
		    		response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("No data Found for the selected date range/Month");
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
	    		
		    } catch(Exception e) {
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

	@SuppressWarnings("unchecked")
	private List<String> normalizePoNoFilters(Object poNoPayload) {
		if (poNoPayload == null) {
			return null;
		}
		if (poNoPayload instanceof String) {
			String value = ((String) poNoPayload).trim();
			return value.isEmpty() ? null : Collections.singletonList(value);
		}
		if (poNoPayload instanceof List<?>) {
			List<String> values = ((List<?>) poNoPayload).stream()
					.filter(Objects::nonNull)
					.map(String::valueOf)
					.map(String::trim)
					.filter(v -> !v.isEmpty())
					.collect(Collectors.toList());
			return values.isEmpty() ? null : values;
		}
		String fallback = String.valueOf(poNoPayload).trim();
		return fallback.isEmpty() ? null : Collections.singletonList(fallback);
	}
    
}

