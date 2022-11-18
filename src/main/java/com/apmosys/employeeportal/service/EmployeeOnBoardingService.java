package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.AssetDTO;
import com.apmosys.employeeportal.dto.EmployeeAssetMapDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.model.Asset;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeAssetMap;
import com.apmosys.employeeportal.repository.EmployeeOnBoardingMapRepository;
import com.apmosys.employeeportal.repository.EmployeeOnBoardingRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class EmployeeOnBoardingService {
	
	@Autowired
	EmployeeRepository employeeRepository;
	
	@Autowired
	EmployeeOnBoardingRepository employeeOnboardingRepository;
	
	@Autowired
	EmployeeOnBoardingMapRepository employeeOnboardingMapRepository;

	@Autowired
	private MailService mailService;
	
	@Value("${hr.mail}")
	private String hrMailAddress;
	
	@Autowired
	StringToDateTimeParser stringToDateTimeParser;

	public ServiceResponse getEmployeeOnBoardingDetailByEmployeementId(AssetDTO assetDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("On-Boarding");
		apiLogInfo.setApiUrl("/api/getEmployeeOnBoardingDetailByEmployeementId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : "+assetDTO.getEmpId());
	
		try {
			
			Employee empObj = employeeRepository.findByEmployeementId(assetDTO.getEmployeementId());
			if(empObj != null) {
				
				List<Object[]> assetDetails = employeeOnboardingRepository.getAssetListByEmpId(empObj.getEmpId());
				List<Object[]> employeeData = employeeRepository.getEmployeeData(empObj.getEmpId());
				List<AssetDTO> dtoList = new ArrayList<>();
				List<EmployeeDTO> employeeDataList = new ArrayList<EmployeeDTO>();
				
				if(assetDetails.isEmpty()) {
					response.setServiceResponse("Employee OnBoarding details not found.");
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					
					apiLogInfo.setApiResponse("Employee OnBoarding details not found.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}else {

					assetDetails.forEach((object) -> {
						AssetDTO dto = new AssetDTO();
						
						dto.setAssetId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						dto.setAssestName(object[1] != null ? object[1].toString() : null);
						dto.setIsAssigned(object[2] != null ? object[2].toString() : null);
						dto.setDepartmentName(object[3] != null ? object[3].toString() : null);
						dto.setDeptId(object[4] != null ? Long.parseLong(object[4].toString()) : null);
						dto.setEmpId(object[5] != null ? Long.parseLong(object[5].toString()) : null);
						
						dtoList.add(dto);
					});
					
					employeeData.forEach((object) -> {
						EmployeeDTO empDto = new EmployeeDTO();
						empDto.setManagerName(object[0] != null ? object[0].toString() : null);
						empDto.setJobRoleName(object[1] != null ? object[1].toString() : null);
						empDto.setDepartmentName(object[2] != null ? object[2].toString() : null);
						empDto.setEmploymentstatus(object[3] != null ? object[3].toString() : null);
						empDto.setName(empObj.getName());
						empDto.setDateOfJoining(empObj.getDateOfJoining().toString());
						empDto.setEmail(empObj.getEmail());
						empDto.setEmployeementId(empObj.getEmployeementId());
						
						employeeDataList.add(empDto);
					});
					
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					response.setServiceResponse1(employeeDataList);
					
					apiLogInfo.setApiResponse("Employee OnBoarding Detail Found.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}
				
			}else {
				response.setServiceResponse("Employee not found.");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				
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

	public ServiceResponse updateOnBoardingCheckList(AssetDTO assetDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("On-Boarding");
		apiLogInfo.setApiUrl("/api/updateOnBoardingCheckList");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("employeementId : "+assetDTO.getEmpId());
		
		try{
			List<EmployeeAssetMap> updatedAssets = new ArrayList<EmployeeAssetMap>();
			Long empId = null;
			
				for(EmployeeAssetMapDTO asset: assetDTO.getDepartmentWiseAssetList()) {
					EmployeeAssetMap assetObj = employeeOnboardingMapRepository.findByAssetId(asset.getAssetId());
					
					if(assetObj != null) {
						assetObj.setIsAssigned(asset.getIsAssigned());
						assetObj.getCommonProperty().setUpdatedBy(assetDTO.getUpdatedBy());
						empId = asset.getEmpId();
						updatedAssets.add(assetObj) ;
					}
				}
			
			if(!updatedAssets.isEmpty()) {
				employeeOnboardingMapRepository.saveAll(updatedAssets);
			}
			
			// send mail to HOD & employee & HR on update asset List
			
			Employee updatedBy = employeeRepository.findByEmpId(assetDTO.getUpdatedBy());
			Employee employee = employeeRepository.findByEmpId(empId);
			String updates = "";
			for(EmployeeAssetMap obj:updatedAssets) {
				Asset asset = employeeOnboardingRepository.getById(obj.getAssetId());
				
				updates = updates.concat(asset.getAssestName().concat(":").concat(Boolean.parseBoolean(obj.getIsAssigned()) ? "Assigned" : "Un-Assigned")) + "<br>";
			}
				
			if(updatedBy != null && employee != null) {
				mailService.sendMailWithCC(employee.getEmail(),
						updatedBy.getEmail() +","+ hrMailAddress,
						"Asset has been updated by " + updatedBy.getName(),
						"Dear " + employee.getName() + ","
						+ "<br>" + updatedBy.getName() + " has updated your asset List"
						+ "<br><br>Asset Updated : "
						+ "<br><br>" +updates);
			}
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Employee OnBoarding checkList updated.");
			
			apiLogInfo.setApiResponse("Employee OnBoarding checkList updated.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			
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
