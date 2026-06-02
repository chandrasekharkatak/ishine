package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.util.EmployeeEmploymentIdUtil;
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
	
	@Value("${snipit.api.token}")
	private String authToken;

	@Value("${snipit.api.base-url}")
	private String baseUrl;
	
	@Autowired
	StringToDateTimeParser stringToDateTimeParser;
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private HttpServletRequest httpRequest;

	public ServiceResponse getEmployeeOnBoardingDetailByEmployeementId(AssetDTO assetDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("On-Boarding");
		apiLogInfo.setApiUrl("/api/getEmployeeOnBoardingDetailByEmployeementId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : "+assetDTO.getEmpId());
	
		try {
			Employee empObj;
			if (EmployeeEmploymentIdUtil.EMPLOYEE_TYPE_APMOSYS_PRODUCT_CONSULTANT
					.equalsIgnoreCase(assetDTO.getEmployeeType())) {
				empObj = employeeRepository.findByEmployeementIdForApmosysProductConsultant(assetDTO.getEmployeementId());
			} else if (EmployeeEmploymentIdUtil.EMPLOYEE_TYPE_APMOSYS_PRODUCT.equalsIgnoreCase(assetDTO.getEmployeeType())) {
				empObj = employeeRepository.findByEmployeementIdForApmosysProduct(assetDTO.getEmployeementId());
			} else if (EmployeeEmploymentIdUtil.EMPLOYEE_TYPE_CONSULTANT.equalsIgnoreCase(assetDTO.getEmployeeType())) {
				empObj = employeeRepository.findByEmployeementIdForConsultant(assetDTO.getEmployeementId());
			} else {
				empObj = employeeRepository.findByEmployeementIdForOthers(assetDTO.getEmployeementId());
			}
			
//			Employee empObj = employeeRepository.findByEmployeementId(assetDTO.getEmployeementId());
			
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
						empDto.setIsConsultant(empObj.getIsConsultant());
						empDto.setIsApprenticeship(empObj.getIsApprenticeship());
						empDto.setIsApmosysProduct(empObj.getIsApmosysProduct()	);					
						empDto.setEmpId(object[8] != null ? Long.parseLong(object[8].toString()) : null);
						empDto.setManagerId(object[7] != null ? Long.parseLong(object[7].toString()) : null);
						
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
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

//	public ServiceResponse updateOnBoardingCheckList(AssetDTO assetDTO) {
//		ServiceResponse response = new ServiceResponse();
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setSubFeatureName("On-Boarding");
//		apiLogInfo.setApiUrl("/api/updateOnBoardingCheckList");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("employeementId : "+assetDTO.getEmpId());
//		
//		try{
//			List<EmployeeAssetMap> updatedAssets = new ArrayList<EmployeeAssetMap>();
//			Long empId = null;
//			
//				for(EmployeeAssetMapDTO asset: assetDTO.getDepartmentWiseAssetList()) {
//					EmployeeAssetMap assetObj = employeeOnboardingMapRepository.findByAssetIdAndEmpId(asset.getAssetId(),asset.getEmpId());
//					
//					if(assetObj != null) {
//						assetObj.setIsAssigned(asset.getIsAssigned());
//						assetObj.getCommonProperty().setUpdatedBy(assetDTO.getUpdatedBy());
//						empId = asset.getEmpId();
//						updatedAssets.add(assetObj) ;
//					}
//				}
//			
//			if(!updatedAssets.isEmpty()) {
//				employeeOnboardingMapRepository.saveAll(updatedAssets);
//			}
//			
//			// send mail to HOD & employee & HR on update asset List
//			
//			Employee updatedBy = employeeRepository.findByEmpId(assetDTO.getUpdatedBy());
//			Employee employee = employeeRepository.findByEmpId(empId);
//			String updates = "";
//			for(EmployeeAssetMap obj:updatedAssets) {
//				Asset asset = employeeOnboardingRepository.getById(obj.getAssetId());
//				
//				updates = updates.concat(asset.getAssetName().concat(":").concat(Boolean.parseBoolean(obj.getIsAssigned()) ? "Assigned" : "Un-Assigned")) + "<br>";
//			}
//				
//			if(updatedBy != null && employee != null) {
//				mailService.sendMailWithCC(employee.getEmail(),
//						updatedBy.getEmail() +","+ hrMailAddress,
//						"Asset has been updated by " + updatedBy.getName(),
//						"Dear " + employee.getName() + ","
//						+ "<br>" + updatedBy.getName() + " has updated your asset List"
//						+ "<br><br>Asset Updated : "
//						+ "<br><br>" +updates);
//			}
//			
//			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//			response.setServiceResponse("Employee On-Boarding check list updated.");
//			
//			apiLogInfo.setApiResponse("Employee On-Boarding check list updated.");
//			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//			
//		}catch(Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//			
//			apiLogInfo.setApiError(e.getMessage());			
//			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			apiLogInfo.setLogLevel("ERROR");
//		}
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//	}
	
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateOnBoardingCheckList(AssetDTO assetDTO) throws Exception {
	    ServiceResponse response = new ServiceResponse();

	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("On-Boarding");
	    apiLogInfo.setApiUrl("/api/updateOnBoardingCheckList");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("empId : ").append(assetDTO != null ? assetDTO.getEmpId() : "null");

	    try {
	        if (assetDTO == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Invalid request: Asset data is missing.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiResponse("Invalid request: Asset data is missing.");
	            return response;
	        }

	        if (assetDTO.getDepartmentWiseAssetList() == null || assetDTO.getDepartmentWiseAssetList().isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No assets found to update.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiResponse("No assets found to update.");
	            return response;
	        }

	        if (assetDTO.getUpdatedBy() == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("UpdatedBy field cannot be null.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiResponse("UpdatedBy field cannot be null.");
	            return response;
	        }

	        // =======================
	        // Process Asset Updates
	        // =======================
	        List<EmployeeAssetMap> updatedAssets = new ArrayList<>();
	        Long empId = null;

	        for (EmployeeAssetMapDTO asset : assetDTO.getDepartmentWiseAssetList()) {
	            if (asset == null) continue;

	            if (asset.getAssetId() == null || asset.getEmpId() == null) {
	                // Skip invalid record
	                continue;
	            }

	            EmployeeAssetMap assetObj = employeeOnboardingMapRepository
	                    .findByAssetIdAndEmpId(asset.getAssetId(), asset.getEmpId());

	            if (assetObj != null) {
	                assetObj.setIsAssigned(asset.getIsAssigned());
	                if (assetObj.getCommonProperty() != null) {
	                    assetObj.getCommonProperty().setUpdatedBy(assetDTO.getUpdatedBy());
	                }
	                empId = asset.getEmpId();
	                updatedAssets.add(assetObj);
	            }
	        }

	        if (updatedAssets.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No valid asset records found for update.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiResponse("No valid asset records found for update.");
	            return response;
	        }

	        employeeOnboardingMapRepository.saveAll(updatedAssets);

	        // =======================
	        // Send Mail Notification
	        // =======================
	        Employee updatedBy = employeeRepository.findByEmpId(assetDTO.getUpdatedBy());
	        Employee employee = empId != null ? employeeRepository.findByEmpId(empId) : null;

	        StringBuilder updates = new StringBuilder();
	        for (EmployeeAssetMap obj : updatedAssets) {
	            if (obj == null || obj.getAssetId() == null) continue;
	            Asset asset = null;
	                asset = employeeOnboardingRepository.getById(obj.getAssetId());

	            if (asset != null) {
	                updates.append(asset.getAssetName())
	                       .append(" : ")
	                       .append(Boolean.parseBoolean(obj.getIsAssigned()) ? "Assigned" : "Un-Assigned")
	                       .append("<br>");
	            }
	        }

	        if (updatedBy != null && employee != null) {
	            String mailBody = new StringBuilder()
	                    .append("Dear ").append(employee.getName()).append(",<br>")
	                    .append(updatedBy.getName()).append(" has updated your asset list.<br><br>")
	                    .append("Asset Updates:<br><br>")
	                    .append(updates.toString())
	                    .toString();

	            mailService.sendMailWithCC(
	                    employee.getEmail(),
	                    updatedBy.getEmail() + "," + hrMailAddress,
	                    "Asset has been updated by " + updatedBy.getName(),
	                    mailBody
	            );
	        }

	        // =======================
	        // Response + Logging
	        // =======================
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("Employee On-Boarding check list updated.");

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        apiLogInfo.setApiResponse("Employee On-Boarding check list updated.");

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong while updating On-Boarding checklist.");
	        response.setServiceError(e.getMessage());

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setApiError(e.getMessage());
	        apiLogInfo.setLogLevel("ERROR");
	        apiLogInfo.setApiResponse(e.getMessage());
	        throw e;
	    }

	    apiLogInfo.setApiRequest(logBuilder.toString());
	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}

	
	public JSONArray snipitUserAPICall(String user) {
		JSONArray snipitResponse = null;
		try {
			String[] userName = user.split("=");
			String findByUserName =  "username=".concat(userName[1]);
			
			OkHttpClient client = new OkHttpClient();
			
	        String url = baseUrl + "/users?limit=50000&offset=0&sort=created_at&" 
                    + user + "&order=desc&deleted=false&all=false";

	        Request request = new Request.Builder()
               .url(url)
               .get()
               .addHeader("accept", "application/json")
               .addHeader("Authorization", authToken)
               .build();
			
//			Request request = new Request.Builder()
//			  .url("http://192.168.12.54/api/v1/users?limit=50000&offset=0&sort=created_at&"+user+"&order=desc&deleted=false&all=false")
//			  .get()
//			  .addHeader("accept", "application/json")
//			  .addHeader("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxIiwianRpIjoiMTliNzZiZTg5YWQ0ZGViYTQ1ZDllMTZhYTc2OTEwMDE5YWZkYWIzYWJhYzAyODBhNmY4YmI1MzI1NjAyYzczM2U1MzVjM2Y3NWVhYTQ2NmQiLCJpYXQiOjE3NzYwNjg1NDYsIm5iZiI6MTc3NjA2ODU0NiwiZXhwIjoyMjQ5NDU0MTQ2LCJzdWIiOiIxIiwic2NvcGVzIjpbXX0.I6KfXJHWcmf8-8ajCI4neO3vMk6-mFY5ZZy04unrSAtMsFp1ATA4CX9Hu_wixctzxnYzwpYH6VwcxOLuIl4sVl8ZmBhHVaoee4x0evhvb8xAiajrjxf76B_zBjp3QqtWLAd8MRgACuYXYO7q3vuwzXIDZArBo0WZunNbtIq60GmxDS6PnjMOXh4_6RZPGZPXtfnGj96kaqBBLAkOzy5nf5aDbiqPK0vxqMj1xnlywFzplL7GTs-bkJWkVGUhNOkXNedEHIxX4HEUT-i_nQ2EDWwwmQaf07IOzdbykqGOVutMWHDhBZG8MRTg7ezN6R5OReYyFQpEDiVA-SHa5Uah_620ZbB2kZMuX9ooCAIN54gsZ92zyXC65VUNnSzrHNb5fK7TWQWthbaR8k-PkNMVbyyKUL_LMsId8by1qlNyZ-QBsr9mGF1feJHm3zdFZL_zFjl8sa0EldTGwQ-QAP3rPD6zCI6noyU0imjOmn1RaFRWZM6xWtPrIy0o904SfOCtSmkaMC8wICeQ9479hKgS4AGf67tb0V3U-IyZdpMNK_Lo-gOy7mscXuPSr8pypxYPfRkFIMiLX4niyNEDygeRdJKMJMeu68J1-umAOrjg1ugitjH5ohcif5ZaTSqfe8p-nBAtprOw7SXrarbiHFrlNiuhsAGBeOKDM-1bwwWfz3s")
//			  .build();
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
			
	        String url = baseUrl + "/users/" + id + "/assets";

	        Request request = new Request.Builder()
	                .url(url)
	                .get()
	                .addHeader("accept", "application/json")
	                .addHeader("Authorization", authToken)
	                .build();
			
//			Request request = new Request.Builder()
//			  .url("http://192.168.12.54/api/v1/users/"+id+"/assets")
//			  .get()
//			  .addHeader("accept", "application/json")
//			  .addHeader("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxIiwianRpIjoiMTliNzZiZTg5YWQ0ZGViYTQ1ZDllMTZhYTc2OTEwMDE5YWZkYWIzYWJhYzAyODBhNmY4YmI1MzI1NjAyYzczM2U1MzVjM2Y3NWVhYTQ2NmQiLCJpYXQiOjE3NzYwNjg1NDYsIm5iZiI6MTc3NjA2ODU0NiwiZXhwIjoyMjQ5NDU0MTQ2LCJzdWIiOiIxIiwic2NvcGVzIjpbXX0.I6KfXJHWcmf8-8ajCI4neO3vMk6-mFY5ZZy04unrSAtMsFp1ATA4CX9Hu_wixctzxnYzwpYH6VwcxOLuIl4sVl8ZmBhHVaoee4x0evhvb8xAiajrjxf76B_zBjp3QqtWLAd8MRgACuYXYO7q3vuwzXIDZArBo0WZunNbtIq60GmxDS6PnjMOXh4_6RZPGZPXtfnGj96kaqBBLAkOzy5nf5aDbiqPK0vxqMj1xnlywFzplL7GTs-bkJWkVGUhNOkXNedEHIxX4HEUT-i_nQ2EDWwwmQaf07IOzdbykqGOVutMWHDhBZG8MRTg7ezN6R5OReYyFQpEDiVA-SHa5Uah_620ZbB2kZMuX9ooCAIN54gsZ92zyXC65VUNnSzrHNb5fK7TWQWthbaR8k-PkNMVbyyKUL_LMsId8by1qlNyZ-QBsr9mGF1feJHm3zdFZL_zFjl8sa0EldTGwQ-QAP3rPD6zCI6noyU0imjOmn1RaFRWZM6xWtPrIy0o904SfOCtSmkaMC8wICeQ9479hKgS4AGf67tb0V3U-IyZdpMNK_Lo-gOy7mscXuPSr8pypxYPfRkFIMiLX4niyNEDygeRdJKMJMeu68J1-umAOrjg1ugitjH5ohcif5ZaTSqfe8p-nBAtprOw7SXrarbiHFrlNiuhsAGBeOKDM-1bwwWfz3s")
//			  .build();
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
    logBuilder.append("employeementId : ").append(assetDTO.getEmployeementId());

    try {
        // Fetch employee
        Employee empObj;
        if ("Apmosys Product".equalsIgnoreCase(assetDTO.getEmployeeType())) {
            empObj = employeeRepository.findByEmployeementIdForApmosysProduct(assetDTO.getEmployeementId());
        } else {
            empObj = employeeRepository.findByEmployeementIdForOthers(assetDTO.getEmployeementId());
        }

        if (empObj == null) {
            response.setServiceResponse("Employee not found.");
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            apiLogInfo.setApiResponse("Employee not found.");
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            logService.logMyInfo(httpRequest, apiLogInfo);
            return response;
        }

        // SNIPIT API call to get user ID
        String[] empDetail = empObj.getEmail().split("@");
        String mailAddress = "email=" + empDetail[0] + "%40" + empDetail[1];
        JSONArray snipitResponse = snipitUserAPICall(mailAddress);

        if (snipitResponse.length() == 0) {
            response.setServiceResponse("Employee IT Asset Details not found in Snipit Portal.");
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            apiLogInfo.setApiResponse("Employee IT Asset Details not found in Snipit Portal.");
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            logService.logMyInfo(httpRequest, apiLogInfo);
            return response;
        }

        int userId = snipitResponse.getJSONObject(0).getInt("id");
        JSONArray snipitAssetResponse = snipitAssetAPICall(userId);

        List<EmployeeAssetMap> currentEmployeeAssets = employeeOnboardingMapRepository.findByEmpId(empObj.getEmpId());

        Map<String, EmployeeAssetMap> existingAssetMap = new HashMap<>();
        for (EmployeeAssetMap empAsset : currentEmployeeAssets) {
            Asset asset = employeeOnboardingRepository.findById(empAsset.getAssetId()).orElse(null);
            if (asset != null && asset.getAssetName() != null) {
                existingAssetMap.put(asset.getAssetName().toLowerCase(), empAsset);
            }
        }

        boolean updateSuccess = true;
        Set<String> processedCategories = new HashSet<>();

        for (int i = 0; i < snipitAssetResponse.length(); i++) {
            JSONObject snipitAsset = snipitAssetResponse.getJSONObject(i);
            String category = snipitAsset.getJSONObject("category").getString("name");
            String categoryKey = category.toLowerCase(); 
            String modelName = snipitAsset.getJSONObject("model").getString("name");
            String assetTag = snipitAsset.getString("asset_tag");
            String assetDetail = modelName + "(" + assetTag + ")";

            processedCategories.add(categoryKey);

            Asset assetObj = employeeOnboardingRepository.findByAssetName(category);
            if (assetObj == null) {
                Asset newAsset = new Asset();
                newAsset.setAssetName(category);
                newAsset.setDeptId(10L);
                newAsset.setAssetType("both");
                assetObj = employeeOnboardingRepository.save(newAsset);
            }

            if (assetObj == null) {
                updateSuccess = false;
                continue;
            }

            // Update existing EmployeeAsset
            EmployeeAssetMap empAsset = existingAssetMap.get(categoryKey);
            if (empAsset != null) {
                List<String> assetDetailsList = empAsset.getAssetDetail() != null
                        ? Arrays.stream(empAsset.getAssetDetail().split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.toList())
                        : new ArrayList<>();

                if (!assetDetailsList.contains(assetDetail)) {
                    assetDetailsList.add(assetDetail);
                }

                List<String> snipitDetailsForCategory = new ArrayList<>();
                for (int j = 0; j < snipitAssetResponse.length(); j++) {
                    JSONObject obj = snipitAssetResponse.getJSONObject(j);
                    if (obj.getJSONObject("category").getString("name").equalsIgnoreCase(category)) {
                        String detail = obj.getJSONObject("model").getString("name") + "(" + obj.getString("asset_tag") + ")";
                        snipitDetailsForCategory.add(detail);
                    }
                }
                assetDetailsList.retainAll(snipitDetailsForCategory);

                empAsset.setAssetDetail(String.join(",", assetDetailsList));
                empAsset.setIsAssigned(assetDetailsList.isEmpty() ? "false" : "true");

                employeeOnboardingMapRepository.save(empAsset);
            } else {
                // Create new only if category doesn't exist
                EmployeeAssetMap newEmpAsset = new EmployeeAssetMap();
                newEmpAsset.setAssetId(assetObj.getAssetId());
                newEmpAsset.setEmpId(empObj.getEmpId());
                newEmpAsset.setIsAssigned("true");
                newEmpAsset.setAssetDetail(assetDetail);
                employeeOnboardingMapRepository.save(newEmpAsset);
            }
        }
		
        for (Map.Entry<String, EmployeeAssetMap> entry : existingAssetMap.entrySet()) {
            String key = entry.getKey();
            if (!processedCategories.contains(key)) {
                EmployeeAssetMap empAsset = entry.getValue();
                empAsset.setIsAssigned("false");
                empAsset.setAssetDetail(null);
                employeeOnboardingMapRepository.save(empAsset);
            }
        }

        if (updateSuccess) {
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse("Employee Asset details synchronized successfully.");
            apiLogInfo.setApiResponse("Employee Asset details synchronized successfully.");
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
        } else {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("Partial synchronization of Employee Assets. Some updates failed.");
            apiLogInfo.setApiResponse("Partial synchronization of Employee Assets. Some updates failed.");
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
        }

    } catch (Exception e) {
        e.printStackTrace();
        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
        response.setServiceResponse("Something Went Wrong.");
        response.setServiceError(e.getMessage());
        apiLogInfo.setApiError(e.getMessage());
        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
        apiLogInfo.setLogLevel("ERROR");
    }

    apiLogInfo.setApiRequest(logBuilder.toString());
    logService.logMyInfo(httpRequest, apiLogInfo);
    return response;
}





//	public ServiceResponse createEmployeeAssetMapping() {
//		ServiceResponse response = new ServiceResponse();
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setSubFeatureName("create_EmployeeAssetMapping");
//		apiLogInfo.setApiUrl("/api/createEmployeeAssetMapping");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		
//		try {
//			
//			List<Asset> asset = employeeOnboardingRepository.findAll();
//			logBuilder.append("employeeOnboardingMapRepository size: "+asset.size());
//
//			// Getting All Active Employees
//			List<Object[]> allEmployee = employeeRepository.getEmployeeDetailForCron();
//			
//			// Deleting existing Asset Mapping 
//			List<EmployeeAssetMap> existingAssetMap = employeeOnboardingMapRepository.findAll();
//			
//			if(!existingAssetMap.isEmpty()) {
//				existingAssetMap.forEach(assetMap -> {
//					employeeOnboardingMapRepository.deleteById(assetMap.getEmployeeAssetMapId());
//				});
//			}
//			
//			
//			// Generating New Blank Asset Mappings
//			for(Object[] employeeList: allEmployee) {
//				Long empId = employeeList[0] != null ? Long.parseLong(employeeList[0].toString()) : null;
//				Long employmentId = employeeList[3] != null ? Long.parseLong(employeeList[3].toString()) : null;
//				
//				if(empId != null && employmentId != null) {
//					List<EmployeeAssetMap> assetMappingObj = new ArrayList<>();
//					
//					for(Asset assetObj : asset) {
//						EmployeeAssetMap employeeAssetMap = new EmployeeAssetMap();
//						employeeAssetMap.setAssetId(assetObj.getAssetId());
//						employeeAssetMap.setEmpId(empId);
//						employeeAssetMap.setIsAssigned("false");
//						assetMappingObj.add(employeeAssetMap);
//					}
//					
//					employeeOnboardingMapRepository.saveAll(assetMappingObj);
//					
//					// After creating Blank Mapping Checking for Assets in OTRS & Updating them into Assets
//					AssetDTO currentEmployee = new AssetDTO();
//					currentEmployee.setEmployeementId(employmentId);
//					
//					ServiceResponse snipitAssetApiResponse =  getAssetDataFromSnipitPortal(currentEmployee);
//				}
//			}
//			
//			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//			response.setServiceResponse("Employee Asset mapping generated Successfully.");
//			apiLogInfo.setApiResponse("Employee Asset mapping generated Successfully.");
//			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//			
//		}catch(Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			apiLogInfo.setLogLevel("ERROR");
//			response.setServiceError(e.getMessage());
//		}
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//	}
	
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse createEmployeeAssetMapping() {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("create_EmployeeAssetMapping");
	    apiLogInfo.setApiUrl("/api/createEmployeeAssetMapping");
	    apiLogInfo.setLogLevel("INFO");
	    StringBuilder logBuilder = new StringBuilder();

	    try {
	        // Fetch all assets
	        List<Asset> assetList = employeeOnboardingRepository.findAll();
	        logBuilder.append("Total Assets found: ").append(assetList.size()).append(" | ");

	        if (assetList == null || assetList.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No active assets found for mapping.");
	            apiLogInfo.setApiResponse("No active assets found for mapping.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiRequest(logBuilder.toString());
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        // Fetch all employees
	        List<Object[]> allEmployee = employeeRepository.getEmployeeDetailForCron();
	        logBuilder.append("Employees found: ").append(allEmployee.size()).append(" | ");

	        if (allEmployee == null || allEmployee.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No active employees found for asset mapping.");
	            apiLogInfo.setApiResponse("No active employees found for asset mapping.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiRequest(logBuilder.toString());
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        // Delete existing asset mappings
	        List<EmployeeAssetMap> existingAssetMap = employeeOnboardingMapRepository.findAll();
	        if (existingAssetMap != null && !existingAssetMap.isEmpty()) {
	            for (EmployeeAssetMap assetMap : existingAssetMap) {
	                if (assetMap.getEmployeeAssetMapId() != null) {
	                    employeeOnboardingMapRepository.deleteById(assetMap.getEmployeeAssetMapId());
	                }
	            }
	            logBuilder.append("Deleted existing asset mappings. | ");
	        }

	        // Generate new blank asset mappings for each employee
	        for (Object[] employeeData : allEmployee) {
	            Long empId = employeeData[0] != null ? Long.parseLong(employeeData[0].toString()) : null;
	            Long employmentId = employeeData[3] != null ? Long.parseLong(employeeData[3].toString()) : null;

	            // Validation before mapping
	            if (empId == null || employmentId == null) {
	                logBuilder.append("Skipped employee with null empId/employmentId | ");
	                continue;
	            }

	            List<EmployeeAssetMap> newMappings = new ArrayList<>();
	            for (Asset assetObj : assetList) {
	                if (assetObj.getAssetId() == null) continue;

	                EmployeeAssetMap employeeAssetMap = new EmployeeAssetMap();
	                employeeAssetMap.setAssetId(assetObj.getAssetId());
	                employeeAssetMap.setEmpId(empId);
	                employeeAssetMap.setIsAssigned("false");
	                newMappings.add(employeeAssetMap);
	            }

	            if (!newMappings.isEmpty()) {
	                employeeOnboardingMapRepository.saveAll(newMappings);
	                logBuilder.append("Created blank mapping for empId: ").append(empId).append(" | ");
	            }

	            // Sync assets from Snipit portal
	            AssetDTO currentEmployee = new AssetDTO();
	            currentEmployee.setEmployeementId(employmentId);
	            ServiceResponse snipitAssetApiResponse = getAssetDataFromSnipitPortal(currentEmployee);

	            if (!ServiceResponse.STATUS_SUCCESS.equals(snipitAssetApiResponse.getServiceStatus())) {
	                logBuilder.append("Snipit sync failed for empId: ").append(empId).append(" | ");
	            }
	        }

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("Employee Asset mapping generated successfully.");
	        apiLogInfo.setApiResponse("Employee Asset mapping generated successfully.");
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong.");
	        response.setServiceError(e.getMessage());

	        apiLogInfo.setApiError(e.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("ERROR");

	    }

	    apiLogInfo.setApiRequest(logBuilder.toString());
	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}
	
	

	
	
}
