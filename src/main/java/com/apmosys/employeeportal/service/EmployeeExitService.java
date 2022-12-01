package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.AssetDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.model.Asset;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.repository.EmployeeOnBoardingMapRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class EmployeeExitService {
	
	@Autowired
	EmployeeRepository employeeRepository;
	
	@Autowired
	EmployeeOnBoardingMapRepository employeeOnboardingMapRepository;
	
	@Autowired
	StringToDateTimeParser stringToDateTimeParser;

	public ServiceResponse updateEmployeeResignationDetails(EmployeeDTO employeeDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Exit");
		apiLogInfo.setApiUrl("/api/updateEmployeeResignationDetails");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : "+employeeDTO.getEmpId());
		try {
			Employee employeeObj = employeeRepository.findByEmpId(employeeDTO.getEmpId());
			
			if(employeeObj != null) {
				employeeObj.setDateOfResign(stringToDateTimeParser.getDate(employeeDTO.getDateOfResign(), "yyyy-MM-dd"));
				employeeObj.setEmploymentstatus("Resigned");
				Employee dbResponse = employeeRepository.save(employeeObj);
				
				if(dbResponse != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Employee Resignation Details Updated Successfully.");
					
					apiLogInfo.setApiResponse("Employee Resignation Details Updated Successfully.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Employee Resignation Details Updation Failed.");
					
					apiLogInfo.setApiResponse("Employee Resignation Details Updation Failed.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee not found.");
				
				apiLogInfo.setApiResponse("Employee not found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiError(e.getMessage());			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		return response;
	}

	public ServiceResponse getEmployeeResignationDetails(EmployeeDTO employeeDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Exit");
		apiLogInfo.setApiUrl("/api/getEmployeeResignationDetails");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : "+employeeDTO.getEmpId());
		try {

			Employee employeeObj = employeeRepository.findByEmpId(employeeDTO.getEmpId());

			if (employeeObj != null) {
				ModelMapper mapper = new ModelMapper();
				EmployeeDTO empDTO = mapper.map(employeeObj, EmployeeDTO.class);
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(empDTO);
				
				apiLogInfo.setApiResponse(empDTO + "Employee Resignation Details found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee not found.");
				
				apiLogInfo.setApiResponse("Employee not found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}

		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiError(e.getMessage());			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		return response;
	}

	public ServiceResponse getEmployeeExitAssetDetails(EmployeeDTO employeeDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Exit");
		apiLogInfo.setApiUrl("/api/getEmployeeExitAssetDetails");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : "+employeeDTO.getEmpId());
		try {
			
			List<Object[]> exitAssetList = employeeOnboardingMapRepository.getExitAssetDetailsByEmployeementId(employeeDTO.getEmployeementId());
			List<AssetDTO> dtoList = new ArrayList<>();
			
			if(!exitAssetList.isEmpty()) {
				
				for(Object[] object: exitAssetList) {
					AssetDTO dto = new AssetDTO();
					dto.setAssetId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setAssestName(object[1] != null ? object[1].toString() : null);
					dto.setDeptId(object[2] != null ? Long.parseLong(object[2].toString()) : null);
					dto.setDepartmentName(object[3] != null ? object[3].toString() : null);
					dto.setEmployeeName(object[4] != null ? object[4].toString() : null);
					dto.setEmployeementId(object[5] != null ? Long.parseLong(object[5].toString()) : null);
					dto.setEmpId(object[6] != null ? Long.parseLong(object[6].toString()) : null);
					dto.setIsAssigned(object[7] != null ? object[7].toString() : null);
					dto.setDeptConsent(object[8] != null ? object[8].toString() : null);
					
					dtoList.add(dto);
				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				
				apiLogInfo.setApiResponse(dtoList + "Employee Exit Asset Details found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Exit Asset Details is empty.");
				
				apiLogInfo.setApiResponse("Employee Exit Asset Details is empty.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiError(e.getMessage());			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		return response;
	}

}
