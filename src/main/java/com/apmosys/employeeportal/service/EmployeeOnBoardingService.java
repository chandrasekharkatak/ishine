package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
			ServiceResponse snipitAssetApiResponse =  getAssetDataFromSnipitPortal(assetDTO);
			
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
						dto.setAssetType(object[6] != null ? object[6].toString() : null);
						dto.setAssetDetail(object[7] != null ? object[7].toString() : null);
						
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
					EmployeeAssetMap assetObj = employeeOnboardingMapRepository.findByAssetIdAndEmpId(asset.getAssetId(),asset.getEmpId());
					
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
				
				updates = updates.concat(asset.getAssetName().concat(":").concat(Boolean.parseBoolean(obj.getIsAssigned()) ? "Assigned" : "Un-Assigned")) + "<br>";
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
			response.setServiceResponse("Employee On-Boarding check list updated.");
			
			apiLogInfo.setApiResponse("Employee On-Boarding check list updated.");
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
	
	public JSONArray snipitUserAPICall(String user) {
		JSONArray snipitResponse = null;
		try {
			String[] userName = user.split("=");
			String findByUserName =  "username=".concat(userName[1]);
			
			OkHttpClient client = new OkHttpClient();
			Request request = new Request.Builder()
			  .url("http://192.168.12.54/api/v1/users?limit=50000&offset=0&sort=created_at&"+user+"&order=desc&deleted=false&all=false")
			  .get()
			  .addHeader("accept", "application/json")
			  .addHeader("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxIiwianRpIjoiYTk1M2RmMjRmOTBkNGQ3N2RkNTg3NjI3NjkzZWE0ZTc5NDBhMjYwMzcyYTU4NjZkZjIwOTM5NDg5YTAxNGIyODRjMjQzMzA4YTk1OTk1Y2YiLCJpYXQiOjE2Njg4NDY0MDQsIm5iZiI6MTY2ODg0NjQwNCwiZXhwIjoyMTQyMjMyMDAzLCJzdWIiOiIyMTYyIiwic2NvcGVzIjpbXX0.Tnj02njjqoHhHQqHCQmMcirxlj56kiQOz8WMc0RkaWRq8rQEkdItPErtAj5T8xen7cwgT94vYZdF3PDNU-oLAKScGCQ8YfB7L9qKtOfEPCgwEd8A1cd5shsN-XZ37RPCMI57ZdzBqeKbvPbOd0e9qTl5rjFMmo3JbygKlRvdFKEyNb7ZVn_WEi6NgSo0aDO8Ig5BjUW7WNFoss4CZQBc0MZvOJaACPJNERbWK91OdywlLBZ4oX2ygzP6PJ_HcbA5474VYFylNpushiFteoybZxAjklrlDY9q1IxpybYpY7RzQDyfTyyaSZexQgdJPit8KCPvWIQMIF2M-0OEC9XCEyUYpo-E4sDtqxtg8bASj4NWktgSD5jrobKZ-a-F0ijnOchIOxzvogXeP6WuugD7O2o-eOIUjhaHiFMI3mcHTAHYIMVU9VQpOOFRzP-Mg5YkaEYpJ7O5cPXLq86ilalNFNJNJvib-M3iDM4c8e6F_FkMneNcuwpSa2IiHLsWeWSw5WjBwRP0x78zmHML2TWdPS0c8pN3KtEBl39n1yWbgVxWNq5E4uXkNeNqD4fr76aV-PvhEwSSizIumLIYqPepx9qYRgn_zdoaibgY56U8RgHcVVps1RNlACFyffNFeUAjMyUxclqHY1U9Ya0AiRRZSDljOFloAKAIyD52nJzIk1Y")
			  .build();
			Response httpResponse = client.newCall(request).execute();
			String jsonData = httpResponse.body().string();
			JSONObject json = new JSONObject(jsonData);
			snipitResponse = json.getJSONArray("rows");
			
			if(snipitResponse == null) {
				JSONArray usernameResponse = snipitUserAPICall(findByUserName);
				if(usernameResponse == null) {
					return null;
				}else {
					return usernameResponse;
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return snipitResponse;
	}
	
	public JSONArray snipitAssetAPICall(Integer id) {
		
		JSONArray snipitAssetResponse = null;
		try {
			OkHttpClient client = new OkHttpClient();
			Request request = new Request.Builder()
			  .url("http://192.168.12.54/api/v1/users/"+id+"/assets")
			  .get()
			  .addHeader("accept", "application/json")
			  .addHeader("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxIiwianRpIjoiYTk1M2RmMjRmOTBkNGQ3N2RkNTg3NjI3NjkzZWE0ZTc5NDBhMjYwMzcyYTU4NjZkZjIwOTM5NDg5YTAxNGIyODRjMjQzMzA4YTk1OTk1Y2YiLCJpYXQiOjE2Njg4NDY0MDQsIm5iZiI6MTY2ODg0NjQwNCwiZXhwIjoyMTQyMjMyMDAzLCJzdWIiOiIyMTYyIiwic2NvcGVzIjpbXX0.Tnj02njjqoHhHQqHCQmMcirxlj56kiQOz8WMc0RkaWRq8rQEkdItPErtAj5T8xen7cwgT94vYZdF3PDNU-oLAKScGCQ8YfB7L9qKtOfEPCgwEd8A1cd5shsN-XZ37RPCMI57ZdzBqeKbvPbOd0e9qTl5rjFMmo3JbygKlRvdFKEyNb7ZVn_WEi6NgSo0aDO8Ig5BjUW7WNFoss4CZQBc0MZvOJaACPJNERbWK91OdywlLBZ4oX2ygzP6PJ_HcbA5474VYFylNpushiFteoybZxAjklrlDY9q1IxpybYpY7RzQDyfTyyaSZexQgdJPit8KCPvWIQMIF2M-0OEC9XCEyUYpo-E4sDtqxtg8bASj4NWktgSD5jrobKZ-a-F0ijnOchIOxzvogXeP6WuugD7O2o-eOIUjhaHiFMI3mcHTAHYIMVU9VQpOOFRzP-Mg5YkaEYpJ7O5cPXLq86ilalNFNJNJvib-M3iDM4c8e6F_FkMneNcuwpSa2IiHLsWeWSw5WjBwRP0x78zmHML2TWdPS0c8pN3KtEBl39n1yWbgVxWNq5E4uXkNeNqD4fr76aV-PvhEwSSizIumLIYqPepx9qYRgn_zdoaibgY56U8RgHcVVps1RNlACFyffNFeUAjMyUxclqHY1U9Ya0AiRRZSDljOFloAKAIyD52nJzIk1Y")
			  .build();
			Response httpResponse = client.newCall(request).execute();
			String jsonData = httpResponse.body().string();
			JSONObject json = new JSONObject(jsonData);
			snipitAssetResponse = json.getJSONArray("rows");
		}catch(Exception e) {
			e.printStackTrace();
		}
		return snipitAssetResponse;
	}

	public ServiceResponse getAssetDataFromSnipitPortal(AssetDTO assetDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("On-Boarding");
		apiLogInfo.setApiUrl("/api/getAssetDataFromSnipitPortal");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("employeementId : "+assetDTO.getEmployeementId());
		try {
			
			Employee empObj = employeeRepository.findByEmployeementId(assetDTO.getEmployeementId());
			if(empObj != null) {
				
				// SNIPIT API call to get user Id by email/username
				
				String[] empDetail = empObj.getEmail().split("@");
				String mailAddress = "email=".concat(empDetail[0]).concat("%40").concat(empDetail[1]);
				JSONArray snipitResponse = snipitUserAPICall(mailAddress);
				if(snipitResponse.length() != 0) {
					int id = 0;
						for (int i = 0; i< snipitResponse.length(); i++){
					        id = snipitResponse.getJSONObject(i).getInt("id");
					    }
						
						List<String> assetDetailList = new ArrayList<>();
						
						// SNIPIT API call to get asset details by user Id
						JSONArray snipitAssetResponse = snipitAssetAPICall(id);
						if(snipitAssetResponse.length() != 0) {
							for (int i = 0; i< snipitAssetResponse.length(); i++){
								String category = snipitAssetResponse.getJSONObject(i).getJSONObject("category").getString("name");
								
								// set true / false in asset table
								
								if(category != null) {
									
									List<Object[]> employeeAssetMapObj = employeeOnboardingMapRepository.findAssetByAssetNameAndEmpId(category,empObj.getEmpId());
									
									Long empAssetMapId = null;
									String modalName = snipitAssetResponse.getJSONObject(i).getJSONObject("model").getString("name");
									String assetTag = snipitAssetResponse.getJSONObject(i).getString("asset_tag");
									
									if(!employeeAssetMapObj.isEmpty()) {
										for(Object[] object : employeeAssetMapObj) {
											empAssetMapId = object[0] != null ? Long.parseLong(object[0].toString()) : null;
										}
										if(empAssetMapId != null) {
											EmployeeAssetMap empAsset = employeeOnboardingMapRepository.getById(empAssetMapId);
											
											empAsset.setIsAssigned("true");
											
											if(modalName != null && assetTag != null) {
												String updatedAssetDetail = modalName+"("+assetTag+")";
												
												if(empAsset.getAssetDetail() != null) {
													if(!assetDetailList.isEmpty() && !assetDetailList.contains(updatedAssetDetail)) {
														
														empAsset.setAssetDetail(String.join(",", assetDetailList));
														assetDetailList.add(updatedAssetDetail);
													}
												}else {
													assetDetailList.add(updatedAssetDetail);
													empAsset.setAssetDetail(updatedAssetDetail);
												}
											}
											
											EmployeeAssetMap dbResponse = employeeOnboardingMapRepository.save(empAsset);
											
											if(dbResponse != null) {
												response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
												response.setServiceResponse("Employee Asset found.");
												
												apiLogInfo.setApiResponse("Employee Asset found.");
												apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
											}else {
												response.setServiceResponse("Employee Asset are not upto date.");
												response.setServiceStatus(ServiceResponse.STATUS_FAIL);
												
												apiLogInfo.setApiResponse("Employee Asset not upto date.");
												apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
											}
										}
									}else {
										//    Add asset in asset table + create mapping for paticular employee
										
										Asset assetObj = employeeOnboardingRepository.findByAssetName(category);
										Asset dbResponse = null;
										if(assetObj != null) {
											dbResponse = assetObj;
										}else {
											Asset asset = new Asset();
											asset.setAssetName(category);
											// by-default dept will be IT 
											asset.setDeptId(10l);
											dbResponse = employeeOnboardingRepository.save(asset);
										}
										
										if(dbResponse != null) {
											
											EmployeeAssetMap employeeAssetMap = new EmployeeAssetMap();
											employeeAssetMap.setAssetId(dbResponse.getAssetId());
											employeeAssetMap.setEmpId(empObj.getEmpId());
											employeeAssetMap.setIsAssigned("true");
											
											if(modalName != null && assetTag != null) {
												String updatedAssetDetail = modalName+"("+assetTag+")";
												employeeAssetMap.setAssetDetail(updatedAssetDetail);
											}
											
											EmployeeAssetMap assetMappingResponse = employeeOnboardingMapRepository.save(employeeAssetMap);
											
											if(assetMappingResponse != null) {
												response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
												response.setServiceResponse("Employee Asset found.");
												
												apiLogInfo.setApiResponse("Employee Asset found.");
												apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
											}else {
												response.setServiceResponse("New Asset Mapping Failed.");
												response.setServiceStatus(ServiceResponse.STATUS_FAIL);
												
												apiLogInfo.setApiResponse("New Asset Mapping Failed.");
												apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
											}
										}
									}
								}
						    }
						}else {
							response.setServiceResponse("No IT Asset has been assigned to user.");
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							
							apiLogInfo.setApiResponse("No IT Asset has been assigned to user.");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						}
				}else {
					response.setServiceResponse("Employee IT Asset Details not found.");
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					
					apiLogInfo.setApiResponse("Employee IT Asset Details not found.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
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

	public ServiceResponse createEmployeeAssetMapping() {
		ServiceResponse response = new ServiceResponse();
		try {
			
			List<Employee> employee = employeeRepository.findAll();
			List<Asset> asset = employeeOnboardingRepository.findAll();
			
			for(Employee obj : employee) {
				List<EmployeeAssetMap> assetMappingObj = new ArrayList<>();
				
				for(Asset assetObj : asset) {
					EmployeeAssetMap employeeAssetMap = new EmployeeAssetMap();
					employeeAssetMap.setAssetId(assetObj.getAssetId());
					employeeAssetMap.setEmpId(obj.getEmpId());
					employeeAssetMap.setIsAssigned("false");
					assetMappingObj.add(employeeAssetMap);
				}
				
				employeeOnboardingMapRepository.saveAll(assetMappingObj);
				
				// After creating Blank Mapping Checking for Assets in OTRS
				AssetDTO currentEmployee = new AssetDTO();
				currentEmployee.setEmployeementId(obj.getEmployeementId());
				
				ServiceResponse snipitAssetApiResponse =  getAssetDataFromSnipitPortal(currentEmployee);
			}
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Employee Asset mapping generated Successfully.");
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

}
