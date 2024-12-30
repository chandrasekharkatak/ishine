package com.apmosys.employeeportal.service;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.Employee360DTO;
import com.apmosys.employeeportal.dto.EmployeeTimesheetDto;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.repository.Employee360Repository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class Employee360Service {
	
	@Autowired
	Employee360Repository employee360Repository;
	
	@Autowired
	EmployeeRepository employeeRepository;
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private HttpServletRequest httpRequest;
	
	public ServiceResponse getLeaveDataPerMonthByEmpId(Long empId) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getLeaveDataPerMonthByEmpId");
		apiLogInfo.setApiUrl("/api/getLeaveDataPerMonthByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " + empId);
		try {
			
			List<Employee360DTO> employeeDtoList = new ArrayList<>();

			List<Object[]> employee360 = employee360Repository.getLeaveDataPerMonthByEmpId(empId);
			
			if(!employee360.isEmpty()) {
				
				for (Object[] employee360dto : employee360) { 
					Employee360DTO dto = new Employee360DTO();
					
		            dto.setLeaveYear(employee360dto[0] != null ? employee360dto[0].toString() : null);
		            dto.setLeaveMonth(employee360dto[1] != null ? employee360dto[1].toString() : null);
		            dto.setLeaveType(employee360dto[2] != null ? employee360dto[2].toString() : null);
		            dto.setLeaveStatus(employee360dto[3] != null ? employee360dto[3].toString() : null);
		            dto.setTotalDays(employee360dto[4] != null ? employee360dto[4].toString() : null);
		            
				    employeeDtoList.add(dto);
				}
			}
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(employeeDtoList);
			apiLogInfo.setApiResponse("Employee leave details fetched.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			
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
	
	public ServiceResponse getEmployeeDetails(Long empId) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getEmployeeDetails");
		apiLogInfo.setApiUrl("/api/getEmployeeDetails");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " + empId);
		try {
			
			List<Employee360DTO> employeeDtoList = new ArrayList<>();
			
			List<Object[]> objectList = employeeRepository.getEmployeeByEmpId(empId);
			
			if (!objectList.isEmpty()) {

				for (Object[] object : objectList) {
					
					Employee360DTO dto = new Employee360DTO();
					
					dto.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setName(object[24] != null ? object[24].toString() : null);
					dto.setManagerId(object[48] != null ? Long.parseLong(object[48].toString()) : null);
					dto.setManagerName(object[43] != null ? object[43].toString() : null);
					dto.setDepartmentName(object[45] != null ? object[45].toString() : null);
					dto.setDepartmentId(object[46] != null ? Long.parseLong(object[46].toString()) : null);
					
					employeeDtoList.add(dto);
				}
			}
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(employeeDtoList);
			apiLogInfo.setApiResponse("Employee leave details fetched.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			
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
	
	public ServiceResponse get360TimesheetDetails(String status) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("get360TimesheetDetails");
		apiLogInfo.setApiUrl("/api/get360TimesheetDetails");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("status : " + status);
		try {
			
			List<Employee360DTO> employeeDtoList = new ArrayList<>();
			
			List<Object[]> objectList = employeeRepository.getTimesheetData(status);
			Map<Long, Employee360DTO> employeeMap = new HashMap<>();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		if (!objectList.isEmpty()) {
			for (Object[] row : objectList) {
				Long empId = row[0] != null ? ((BigInteger) row[0]).longValue() : null;
			    String projectName = (String) row[13];
			    String activity = (String) row[12];
			    String date = ((java.sql.Date) row[4]).toString();

			    // Find or create Employee DTO
			    Employee360DTO employee = employeeMap.computeIfAbsent(empId, id -> {
			        Employee360DTO dto = new Employee360DTO();
					dto.setEmpId(row[0] != null ? Long.parseLong(row[0].toString()) : null);
			        dto.setName((String) row[3]);
			        dto.setDate(date);
			        dto.setDayType(row[5] != null ? row[5].toString() : null);
			        if (row[6] != null) {
			           dto.setOfficeInTime(((java.sql.Timestamp) row[6]).toLocalDateTime());}
			        if (row[7] != null) {
				           dto.setOfficeOutTime(((java.sql.Timestamp) row[7]).toLocalDateTime());}
	                dto.setTotalWorkingHours(row[8] != null ? row[8].toString() : null);
			        dto.setStatus(row[10] != null ? row[10].toString() : null);
			        if (row[11] != null) {
				           dto.setCreatedOn(((java.sql.Timestamp) row[11]).toLocalDateTime());}
			        dto.setTimeSheet(new ArrayList<>());
			        return dto;
			    });
			    

			    // Find or create Project DTO
			    List<EmployeeTimesheetDto> timeSheet = employee.getTimeSheet();
			    EmployeeTimesheetDto project = timeSheet.stream()
			            .filter(p -> p.getProjectName().equals(projectName))
			            .findFirst()
			            .orElseGet(() -> {
			            	EmployeeTimesheetDto newProject = new EmployeeTimesheetDto();
			                newProject.setProjectName(projectName);
			                newProject.setProjectId(row[1] != null ? Long.parseLong(row[1].toString()) : null);
			                newProject.setActivityId(row[2] != null ? Long.parseLong(row[2].toString()) : null);
			                newProject.setActivities(new ArrayList<>());
			                timeSheet.add(newProject);
			                return newProject;
			            });

			    // Add activity to the project
			    project.getActivities().add(activity);
			}
		}

			// Convert Map values to a list of Employee360DTO if needed
			List<Employee360DTO> employees = new ArrayList<>(employeeMap.values());

			
			if (!objectList.isEmpty()) {

				for (Object[] object : objectList) {
					
					Employee360DTO dto = new Employee360DTO();
					
				
				}
			}
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(employeeMap);
			apiLogInfo.setApiResponse("Timesheet details fetched.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			
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

	
}
