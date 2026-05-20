package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.TimesheetDashboardCountDTO;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTimesheetsNewRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class TimesheetDashboardServiceNew {

    @Autowired
    private EmployeeTimesheetsNewRepository employeeTimesheetsNewRepository;

   

    @Autowired
    private EmployeeRepository employeeRepository;

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
            Integer month, 
            Integer year, 
            Long empId, 
            Boolean isClientDashboard,
            List<String> billableTypes, String employeeActive, String clientSideFilter) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("getTimesheetDashboardCountForEmployee");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("getTimesheetDashboardCountForEmployee");
        
        try {
            List<Object[]> countForEmployee =  new ArrayList<>();

            List<Date> dateRange = getDateRange(month, year);

            Date fromDate = dateRange.get(0);
            Date toDate = dateRange.get(1);

            List<Long> authorizedEmpIds = employeeRepository.getAllAuthorizeEmployeeId(empId);
            // Long ishineNotFilledEmployees = null;

            if (isClientDashboard) {
                countForEmployee = employeeTimesheetsNewRepository.getTimesheetDashboardCountForEmployeeNew(
                        fromDate, toDate, empId, clientSideFilter, authorizedEmpIds, month, year);
                // ishineNotFilledEmployees = employeeTimesheetsNewRepository.countIshineNotFilled(fromDate, toDate);
                
            } else {
                countForEmployee = employeeTimesheetsNewRepository.getTimesheetDashboardCountForAllEmployee(
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
               
                dto.setClientSidePendingCount(row[2] != null ? ((Number) row[2]).intValue() : 0);
                dto.setDefaulterCount(row[3] != null ? ((Number) row[3]).intValue() : 0);
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
            apiLogInfo.setApiError(e.getMessage());
            apiLogInfo.setLogLevel("ERROR");
            apiLogInfo.setApiRequest(logBuilder.toString());
            logService.logMyInfo(httpRequest, apiLogInfo);
        }
        
        return response;
    }

    public static List<Date> getDateRange(Integer month, Integer year) {

        LocalDate today = LocalDate.now();

        // from_date
        LocalDate fromDate;
        if (month != null && year != null) {
            fromDate = YearMonth.of(year, month).atDay(1);
        } else {
            fromDate = today.withDayOfMonth(1);
        }

        // to_date
        LocalDate toDate;
        if (month != null && year != null) {
            if (year == today.getYear() && month == today.getMonthValue()) {
                toDate = today;
            } else {
                toDate = YearMonth.of(year, month).atEndOfMonth();
            }
        } else {
            toDate = today;
        }

        return List.of(
                Date.valueOf(fromDate),
                Date.valueOf(toDate) 
        );
    }
    
}
