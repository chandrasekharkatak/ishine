package com.apmosys.employeeportal.service;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.hibernate.internal.build.AllowSysOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;

import com.apmosys.employeeportal.dto.FeatureMasterDTO;
import com.apmosys.employeeportal.dto.JobRoleDTO;
import com.apmosys.employeeportal.dto.LogDTO;
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
	
	@Autowired
	private HttpServletRequest httpRequest;

	@Autowired
	private LogService logService;

	public ServiceResponse setDefaultSubFeaturesByRoleId(JobRoleDTO jobRoleDTO) {

		ServiceResponse serviceResponse = new ServiceResponse();
		
		try {
			List<SubFeatureMaster> defaultSubFeatureMasterList = subFeatureMasterRepository
					.findBySubFeatureType((short) 1);
			List<RoleFeatureMap> roleFeatureMapList = new ArrayList<RoleFeatureMap>();
			for (SubFeatureMaster subFeatureMaster : defaultSubFeatureMasterList) {
				RoleFeatureMap roleFeatureMap = new RoleFeatureMap();
				roleFeatureMap.setSubFeatureMasterId(subFeatureMaster.getSubFeatureMasterId());
				roleFeatureMap.setJobRoleId(jobRoleDTO.getJobRoleId());
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

//	@Transactional
//	public ServiceResponse updateRoleFeatureMapping(FeatureMasterDTO featureMasterDTO) {
//		ServiceResponse serviceResponse = new ServiceResponse();
//		try {
//			Long jobRoleId = featureMasterDTO.getJobRoleId();
//
//			featureMasterDTO.getSubFeatures().stream().filter(subfeatures -> subfeatures.getRoleFeatureMapId() == null)
//					.forEach(dto -> {
//						RoleFeatureMap roleFeatureMap = new RoleFeatureMap();
//						roleFeatureMap.setJobRoleId(jobRoleId);
//						roleFeatureMap.setSubFeatureMasterId(dto.getSubFeatureMasterId());
//						roleFeatureMapRepository.save(roleFeatureMap);
//
//					});
//
//			featureMasterDTO.getSubFeatures().stream().filter(subfeatures -> !subfeatures.getIsActive())
//					.forEach(dto -> {
//						roleFeatureMapRepository.deleteById(dto.getRoleFeatureMapId());
//
//					});
//
//			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//			serviceResponse.setServiceResponse("Subfeatures of role updated.");
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			serviceResponse.setServiceResponse("Something Went Wrong.");
//			serviceResponse.setServiceError(e.getMessage());
//		}
//		return serviceResponse;
//	}

	@Transactional
	public ServiceResponse updateRoleFeatureMapping(FeatureMasterDTO featureMasterDTO) {
		ServiceResponse serviceResponse = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("UpdateRoleFeatureMapping");
		apiLogInfo.setApiUrl("/api/updateRoleFeatureMapping");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("JobRole Id : " + featureMasterDTO.getJobRoleId());
		try {
			Long jobRoleId = featureMasterDTO.getJobRoleId();

			roleFeatureMapRepository.deleteByJobRoleId(jobRoleId);

			List<RoleFeatureMap> roleFeatureMapList = new ArrayList<>();

			featureMasterDTO.getSubFeatures().forEach(dto -> {

				RoleFeatureMap roleFeatureMap = new RoleFeatureMap();
				roleFeatureMap.setJobRoleId(jobRoleId);
				roleFeatureMap.setSubFeatureMasterId(dto.getSubFeatureMasterId());
				roleFeatureMapList.add(roleFeatureMap);

			});

			roleFeatureMapRepository.saveAll(roleFeatureMapList);

			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			serviceResponse.setServiceResponse("Subfeatures of role updated.");
			apiLogInfo.setApiResponse("Subfeatures of role updated.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			serviceResponse.setServiceResponse("Something Went Wrong.");
			serviceResponse.setServiceError(e.getMessage());
			apiLogInfo.setApiResponse("Something went Wrong");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return serviceResponse;
	}

}
