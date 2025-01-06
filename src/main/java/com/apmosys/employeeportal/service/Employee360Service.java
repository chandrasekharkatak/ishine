package com.apmosys.employeeportal.service;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import com.apmosys.employeeportal.dto.Employee360DTO;
import com.apmosys.employeeportal.dto.EmployeeTimesheetDto;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.model.Timesheet;
import com.apmosys.employeeportal.repository.Employee360Repository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.TimesheetsRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class Employee360Service {
	
	@Autowired
	Employee360Repository employee360Repository;
	
	@Autowired
	EmployeeRepository employeeRepository;
	
	@Autowired
	TimesheetsRepository timesheetsRepository;
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private HttpServletRequest httpRequest;

	private String LocalDate;
	
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
		
	public ServiceResponse get360TimesheetDetails(String status,long empId,long projectId,String teamName,long managerId, String startDate, String endDate) {
		ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("get360TimesheetDetails");
	    apiLogInfo.setApiUrl("/api/get360TimesheetDetails");
	    apiLogInfo.setLogLevel("INFO");
	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("status : " + status);
		try {
			
			List<Employee360DTO> employeeDtoList = new ArrayList<>();

	        // Parse and convert date strings to LocalDate
	        LocalDate localStartDate = null;
	        LocalDate localEndDate = null;
	        SimpleDateFormat formatedDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
	        
	        // Debugging parameter values before making the repository call
	        System.out.println("Parameters passed to employeeRepository.getDynamicTimesheetData:");
	        System.out.println("status: " + status + " (Type: " + ((status != null) ? status.getClass().getSimpleName() : "null") + ")");
	        System.out.println("empId: " + empId + " (Type: " + Long.TYPE.getSimpleName() + ")");
	        System.out.println("projectId: " + projectId + " (Type: " + Long.TYPE.getSimpleName() + ")");
	        System.out.println("teamName: " + teamName + " (Type: " + ((teamName != null) ? teamName.getClass().getSimpleName() : "null") + ")");
	        System.out.println("managerId: " + managerId + " (Type: " + Long.TYPE.getSimpleName() + ")");
	        System.out.println("localStartDate: " + localStartDate + " (Type: " + ((localStartDate != null) ? localStartDate.getClass().getSimpleName() : "null") + ")");
	        System.out.println("localEndDate: " + localEndDate + " (Type: " + ((localEndDate != null) ? localEndDate.getClass().getSimpleName() : "null") + ")");
	        System.out.println("startDate: " + startDate + " (Type: " + ((startDate != null) ? startDate.getClass().getSimpleName() : "null") + ")");
	        System.out.println("endDate: " + endDate + " (Type: " + ((endDate != null) ? endDate.getClass().getSimpleName() : "null") + ")");


	        if (startDate != null && !startDate.isEmpty() && !startDate.equalsIgnoreCase("null") &&
	        	    endDate != null && !endDate.isEmpty() && !endDate.equalsIgnoreCase("null")) {
	            Date startingDate = formatedDate.parse(startDate);
	            Date endingDate = formatedDate.parse(endDate);
	            localStartDate = startingDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	            localEndDate = endingDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	        } else {
	            System.out.println("No date range provided, sending null for dates.");
	        }
	        
	        System.out.println("localStartDate: " + localStartDate + " (Type: " + ((localStartDate != null) ? localStartDate.getClass().getSimpleName() : "null") + ")");
	        System.out.println("localEndDate: " + localEndDate + " (Type: " + ((localEndDate != null) ? localEndDate.getClass().getSimpleName() : "null") + ")");

	        // Example test call
	        List<Object[]> results = employeeRepository.getDynamicTimesheetData(
	                "Pending", 21899, 0, null, 0L, null, null);

	        // Fetching data from repository
	        List<Object[]> objectList = new ArrayList<>();
	        if (teamName == null || teamName.isEmpty() || teamName.equalsIgnoreCase("null")) {
	        	objectList = employeeRepository.getDynamicTimesheetData(status, empId, projectId, null, managerId, localStartDate, localEndDate);
	        }else {
		        objectList = employeeRepository.getDynamicTimesheetData(status, empId, projectId, teamName, managerId, localStartDate, localEndDate);
	        }
	        System.out.println(objectList);
//			System.out.println(results);
			Map<Long, Employee360DTO> employeeMap = new HashMap<>();

		if (!objectList.isEmpty()) {
			for (Object[] row : objectList) {
			    String projectName = (String) row[13];
			    String activity = (String) row[12];
			    String date = ((java.sql.Date) row[4]).toString();

			    // Find or create Employee DTO
			    Employee360DTO employee = employeeMap.computeIfAbsent(empId, id -> {
			        Employee360DTO dto = new Employee360DTO();
					dto.setEmpId(empId!=0?empId:Long.parseLong(row[1].toString()));
			        dto.setName((String) row[3]);
			        dto.setDate(date);
			        dto.setDayType(row[5] != null ? row[5].toString() : null);
			        if (row[6] != null) {
			           dto.setOfficeInTime(((java.sql.Timestamp) row[6]).toLocalDateTime());}
			        if (row[7] != null) {
				           dto.setOfficeOutTime(((java.sql.Timestamp) row[7]).toLocalDateTime());}
	                dto.setTotalWorkingHours(row[8] != null ? row[8].toString() : null);
			        dto.setStatus(row[10] != null ? row[10].toString() : null);
			        dto.setRemarks(row[15] != null ? row[15].toString() : null);
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
			                newProject.setTeamName(!teamName.equals("null") ? teamName :(String) row[14]);
			                newProject.setTimesheetId(row[16] != null ? Long.parseLong(row[16].toString()) : null);
			                newProject.setProjectId(projectId != 0L ?projectId: Long.parseLong(row[1].toString()));
			                newProject.setActivityId(row[2] != null ? Long.parseLong(row[2].toString()) : null);
			                newProject.setActivities(new ArrayList<>());
			                timeSheet.add(newProject);
			                return newProject;
			            });

			    // Add activity to the project
			    project.getActivities().add(activity);
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

	public ServiceResponse updateStatus(String status,List<Long>timesheetId,Long updatedBy) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("get360TimesheetDetails");
		apiLogInfo.setApiUrl("/api/updateStatus");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("timesheetId : " + timesheetId);
		try {
			
			List<Employee360DTO> employeeDtoList = new ArrayList<>();
			List<Timesheet> timesheet = timesheetsRepository.findByTimesheetIdIn(timesheetId);
			
			if (!timesheet.isEmpty()) {
				for(Timesheet timesheetobj:timesheet){
					timesheetobj.setStatus(status);
					timesheetobj.setTimesheetStatusUpdatedBy(updatedBy);
//					timesheetobj.setUpdatedOn(new Date());
//					timesheetobj.setRemarks(timesheetDTO.getRejectReason());
		            timesheetsRepository.save(timesheetobj);
				}
				}
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Updated Successfully!!");
			apiLogInfo.setApiResponse("Employee_Timesheet details updated.");
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
