package com.apmosys.employeeportal.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.EncryptDecrypt;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.RewardConfigurationDTO;
import com.apmosys.employeeportal.model.Asset;
import com.apmosys.employeeportal.model.CommonProperties;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.LeaveBalanceLog;
import com.apmosys.employeeportal.model.LeaveTypeMaster;
import com.apmosys.employeeportal.model.Log;
import com.apmosys.employeeportal.model.RewardConfig;
import com.apmosys.employeeportal.repository.Employee360Repository;
import com.apmosys.employeeportal.utility.DbTable;
import com.apmosys.employeeportal.utility.LeaveLogMessage;
import com.apmosys.employeeportal.utility.LogEvents;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class Employee360Service {
	
	@Autowired
	Employee360Repository employee360Repository;
	
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
			
			List<Object[]> employee360 = employee360Repository.getLeaveDataPerMonthByEmpId(empId);
			List<EmployeeDTO> employeeDtoList = new ArrayList<>();

			for (Object[] employee360dto : employee360) { 
				EmployeeDTO dto = new EmployeeDTO();
				
				dto.setLeaveDate(employee360dto[0] != null ? employee360dto[0].toString() : null);
			    dto.setLeaveType(employee360dto[1] != null ? employee360dto[1].toString() : null);
			    dto.setTotal_leave_records(employee360dto[2] != null ? employee360dto[2].toString() : null);
			    dto.setTotal_leave_days(employee360dto[3] != null ? employee360dto[3].toString() : null);
			    dto.setStatus(employee360dto[4] != null ? employee360dto[4].toString() : null);

			    employeeDtoList.add(dto);
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
