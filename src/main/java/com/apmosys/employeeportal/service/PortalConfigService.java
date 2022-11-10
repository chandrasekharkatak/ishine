package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
	CronJobService cronJobService;
	
	@Autowired
	StringToDateTimeParser stringToDateTimeParser;
	
	@Value("${monthlyTimesheetExcelGenerator.expression}")
	private String excelGenerator;
	
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
					portaldto.setMailTrigger(portal.getMailTrigger());
					portaldto.setConfigValue(portal.getConfigValue());
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
			
			protalConfigDTO.getAllPortalConfigData().forEach((dto) -> {
				
				Optional<PortalConfig> portalCongifObj = portalConfigRepository.findById(dto.getPortalConfigId());
			
				if(!portalCongifObj.isEmpty()) {
					PortalConfig portalConfigToBeUpdate = portalCongifObj.get();
					
					portalConfigToBeUpdate.setUpdatedBy(dto.getUpdatedBy());
					portalConfigToBeUpdate.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
					portalConfigToBeUpdate.setConfigPeriod(dto.getConfigPeriod());
					portalConfigToBeUpdate.setMailTrigger(dto.getMailTrigger());
					portalConfigToBeUpdate.setConfigValue(dto.getConfigValue());
					PortalConfig dbResponse = portalConfigRepository.save(portalConfigToBeUpdate);
					
					if(dbResponse!=null) {
						Properties p = System.getProperties();
						p.setProperty("monthlyTimesheetExcelGenerator.expression", "0 0/7 * ? * *");
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
			});
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse generatePerviousMonthDSR() {
		ServiceResponse response = new ServiceResponse();
		try {
			
			ServiceResponse monthlyTimesheetExcelGeneratorResponse = cronJobService.monthlyTimesheetExcelGenerator();
			
			if(monthlyTimesheetExcelGeneratorResponse.getServiceStatus().equals("Success")) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(monthlyTimesheetExcelGeneratorResponse.getServiceResponse());
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse(monthlyTimesheetExcelGeneratorResponse.getServiceResponse());
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
	public void dynamicCronExpressionForDSRGenerator() {
		try {
			
//			List<PortalConfig> portalConfig = portalConfigRepository.findAll();
//			
//			String cronExpression = null;
//			
//			for(PortalConfig portalConfigObj : portalConfig) {
//				if(portalConfigObj.getConfigName().equals("DSR Day")) {
//					String generateDay = portalConfigObj.getConfigValue();
////					cronExpression = "0 0 4 "+ generateDay +" * ?";
//					cronExpression = "0 0/" + generateDay + " * ? * *";
//				}
//			}
//			
//			System.out.println(cronExpression + " : cronExpression");
			
			Properties p = System.getProperties();
			p.setProperty("monthlyTimesheetExcelGenerator.expression", "0 0/5 * ? * *");
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
