package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.JobRoleDTO;
import com.apmosys.employeeportal.dto.RoleFeatureMapDTO;
import com.apmosys.employeeportal.model.RoleFeatureMap;
import com.apmosys.employeeportal.model.SubFeatureMaster;
import com.apmosys.employeeportal.repository.RoleFeatureMapRepository;
import com.apmosys.employeeportal.repository.SubFeatureMasterRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class RoleFeatureMapService {

	@Autowired
	SubFeatureMasterRepository subFeatureMasterRepository;

	@Autowired
	RoleFeatureMapRepository roleFeatureMapRepository;

	public ServiceResponse setDefaultSubFeaturesByRoleId(JobRoleDTO jobRoleDTO) {

		ServiceResponse serviceResponse = new ServiceResponse();
		try {
			List<SubFeatureMaster> defaultSubFeatureMasterList = subFeatureMasterRepository.findBySubFeatureType((short) 1);
			List<RoleFeatureMap> roleFeatureMapList = new ArrayList<RoleFeatureMap>();
			for (SubFeatureMaster subFeatureMaster : defaultSubFeatureMasterList) {
				RoleFeatureMap roleFeatureMap = new RoleFeatureMap();
				roleFeatureMap.setSubFeatureMasterId(subFeatureMaster.getSubFeatureMasterId());
				roleFeatureMap.setJobRoleId(jobRoleDTO.getId());
				roleFeatureMapList.add(roleFeatureMap);
			}
			List<RoleFeatureMap> savedRoleFeatureMapList = roleFeatureMapRepository.saveAll(roleFeatureMapList);
			if (!savedRoleFeatureMapList.isEmpty()) {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				serviceResponse.setServiceResponse("Deafult subfeatures mapped to role.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			serviceResponse.setServiceResponse("Something Went Wrong.");
			serviceResponse.setServiceError(e.getMessage());
		}

		return serviceResponse;
	}

}
