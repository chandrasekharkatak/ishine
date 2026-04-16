package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.FeatureMasterDTO;
import com.apmosys.employeeportal.model.FeatureMaster;
import com.apmosys.employeeportal.model.TabMaster;
import com.apmosys.employeeportal.repository.FeatureMasterRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class FeatureMasterService {
	
	@Autowired
	FeatureMasterRepository featureMasterRepository;

//	public ServiceResponse createFeature(FeatureMasterDTO featureMasterDTO) {
//		ServiceResponse response = new ServiceResponse();
//		try
//		{
//			FeatureMaster feature = new FeatureMaster();
//			TabMaster tab = new TabMaster();
//			tab.setTabId(featureMasterDTO.getTabId());			
//			feature.setFeatureName(featureMasterDTO.getFeatureName());
//			feature.setTabMaster(tab);
//			
//			FeatureMaster dbResponse = featureMasterRepository.save(feature);
//			
//			if (dbResponse != null) {
//				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				response.setServiceResponse("New Feature Created.");
//			} else {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("New Feature Creation Failed.");
//			}
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//		}
//		return response;
//	}

//	public ServiceResponse getAllFeature() {
//		ServiceResponse response = new ServiceResponse();
//		try
//		{
//			List<FeatureMasterDTO> dtoList = new ArrayList<FeatureMasterDTO>();
//			
//			List<FeatureMaster> list = featureMasterRepository.findAll();
//			
//			if (list != null) {
//				for(FeatureMaster featureMaster :list)
//				{
//					FeatureMasterDTO featureMasterDTO  = new FeatureMasterDTO();
//					TabMaster tab = new TabMaster();
//					
//					featureMasterDTO.setFeatureId(featureMaster.getFeatureId());
//					featureMasterDTO.setFeatureName(featureMaster.getFeatureName());
//					tab.setTabId(featureMaster.getTabMaster().getTabId());
//					tab.setTabName(featureMaster.getTabMaster().getTabName());
//					tab.setTabVisibility(featureMaster.getTabMaster().getTabVisibility());
//					tab.setIconName(featureMaster.getTabMaster().getIconName());
//					featureMasterDTO.setTabMaster(tab);
//					dtoList.add(featureMasterDTO);
//				}
//				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				response.setServiceResponse(dtoList);
//			} else {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("Feature List is null.");
//			}		
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//		}
//		return response;
//	}

	
}
