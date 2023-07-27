package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.JobRoleDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.RoleFeatureMapDTO;
import com.apmosys.employeeportal.dto.SubFeatureMasterDTO;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.model.RoleFeatureMap;
import com.apmosys.employeeportal.repository.RoleFeatureMapRepository;
import com.apmosys.employeeportal.repository.SubFeatureMasterRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class SubFeatureMasterService {
	
	@Autowired
	RoleFeatureMapRepository roleFeatureMapRepository;
	
	@Autowired
	SubFeatureMasterRepository subFeatureMasterRepository;
	
	@Autowired
	private HttpServletRequest httpRequest;

	@Autowired
	private LogService logService;
	
	public ServiceResponse getSubfeaturesByJobRoleId(JobRoleDTO jobRoleDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("");
		apiLogInfo.setApiUrl("/api/getSubfeaturesByJobRoleId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
	logBuilder.append("JobRoleId : " + jobRoleDTO.getJobRoleId());
		try
		{
			List<RoleFeatureMap> roleFeatureMapList = roleFeatureMapRepository.findByJobRoleId(jobRoleDTO.getJobRoleId());

			if (roleFeatureMapList.isEmpty()) {
				
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Subfeatures Not Found For The Job Role.");
				apiLogInfo.setApiResponse("Subfeatures Not Found For the Job Role");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			} else {
				
				List<RoleFeatureMapDTO> dtolist = new ArrayList<RoleFeatureMapDTO>();
				for(RoleFeatureMap roleFeatureMap :roleFeatureMapList)
				{
					RoleFeatureMapDTO map = new RoleFeatureMapDTO();
					map.setRoleFeatureMapId(roleFeatureMap.getRoleFeatureMapId());
					map.setJobRoleId(roleFeatureMap.getJobRoleId());
					map.setSubFeatureMasterId(roleFeatureMap.getSubFeatureMasterId());
					dtolist.add(map);
				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtolist);
				apiLogInfo.setApiResponse(dtolist.size() + " subfeatures found !");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}	
		}
		catch(Exception e)
		{
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

	public ServiceResponse getAllSubFeatures() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getAllSubFeatures");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("AllSubFeaturesList size : " + subFeatureMasterRepository.getAllSubFeatures().size());

		try
		{
			List<Object[]> objectList = subFeatureMasterRepository.getAllSubFeatures();
			
			if (objectList != null) {
			
				List<SubFeatureMasterDTO> dtoList = new ArrayList<>();
				for(Object[] object: objectList)
				{
					SubFeatureMasterDTO dto = new SubFeatureMasterDTO();
					dto.setSubFeatureMasterId(Long.parseLong(object[0].toString()));
					dto.setFeatureId(Long.parseLong(object[1].toString()));
					dto.setSubFeatureName(object[2].toString());
					dto.setSubFeatureType(Short.parseShort(object[3].toString()));
					dto.setFeatureName(object[4].toString());
					dto.setTabName(object[5].toString());
					dtoList.add(dto);
				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse(" SubFeatureList Fetched!" + dtoList.size());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}
			else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("List is null");
				apiLogInfo.setApiResponse("List is null");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
		}		
		catch(Exception e)
		{
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
