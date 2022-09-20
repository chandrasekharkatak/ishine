package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.ProtalConfigDTO;
import com.apmosys.employeeportal.model.PortalConfig;
import com.apmosys.employeeportal.repository.PortalConfigRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class PortalConfigService {
	
	@Autowired
	PortalConfigRepository portalConfigRepository;
	
	@Autowired
	StringToDateTimeParser stringToDateTimeParser;
	
	public ServiceResponse getPortalConfig() {
		ServiceResponse response = new ServiceResponse();
		try {
			List<PortalConfig> allPortalConfig = portalConfigRepository.findAll();
			List<ProtalConfigDTO> dtoList = new ArrayList<ProtalConfigDTO>();

			if (allPortalConfig.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("PortalConfig list is empty.");
			} else {
				allPortalConfig.forEach((portal) -> {
					
					ProtalConfigDTO portaldto = new ProtalConfigDTO();
					portaldto.setPortalConfigId(portal.getPortalConfigId());
					portaldto.setConfigName(portal.getConfigName());
					portaldto.setConfigPeriod(portal.getConfigPeriod());
					dtoList.add(portaldto);
					
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
			}
			
		}catch(Exception e){
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse updatePortalConfig(ProtalConfigDTO protalConfigDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			Optional<PortalConfig> portalCongifObj = portalConfigRepository.findById(protalConfigDTO.getPortalConfigId());
			
			if(!portalCongifObj.isEmpty()) {
				PortalConfig portalConfigToBeUpdate = portalCongifObj.get();
				
				portalConfigToBeUpdate.setUpdatedBy(protalConfigDTO.getUpdatedBy());
				portalConfigToBeUpdate.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
				portalConfigToBeUpdate.setConfigPeriod(protalConfigDTO.getConfigPeriod());
				PortalConfig dbResponse = portalConfigRepository.save(portalConfigToBeUpdate);
				
				if(dbResponse!=null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Portal Global Configuration Updated Successfully.");
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Portal Global Configuration Updation Failed.");
				}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Portal Global Configuration not Found.");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

}
