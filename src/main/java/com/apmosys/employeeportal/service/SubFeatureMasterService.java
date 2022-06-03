package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.JobRoleDTO;
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
	
	public ServiceResponse getSubfeaturesByJobRoleId(JobRoleDTO jobRoleDTO) {
		ServiceResponse response = new ServiceResponse();
		try
		{
			List<RoleFeatureMap> roleFeatureMapList = roleFeatureMapRepository.findByJobRoleId(jobRoleDTO.getId());

			if (roleFeatureMapList.isEmpty()) {
				
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Subfeatures Not Found For The Job Role.");			
				
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
			}	
		}
		catch(Exception e)
		{
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getAllSubFeatures() {
		ServiceResponse response = new ServiceResponse();
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
					dtoList.add(dto);
				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
			}
			else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("List is null");
			}
		}		
		catch(Exception e)
		{
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

}
