package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.Employee360DTO;
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

}
