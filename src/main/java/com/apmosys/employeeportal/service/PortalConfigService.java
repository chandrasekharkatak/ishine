package com.apmosys.employeeportal.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.ProtalConfigDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.PortalConfig;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.PortalConfigRepository;
import com.apmosys.employeeportal.repository.TimesheetsRepository;
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
	
	@Autowired
	DepartmentRepository departmentRepository;
	
	@Autowired
	TimesheetsRepository timesheetsRepository;
	
	@Value("${hr.mail}")
	private String hrMailAddress;
	
	@Autowired
	MailService mailService;
	
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

	public ServiceResponse generateAllEmployeeDSR(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			ServiceResponse dsrResponse = cronJobService.allEmployeeDsrReport(timesheetDTO);
			
			if(dsrResponse.getServiceStatus().equals("Success")) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dsrResponse.getServiceResponse());
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse(dsrResponse.getServiceResponse());
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse segregatedDeptEodDefaulter() {
		ServiceResponse response = new ServiceResponse();
		try {
			
			int currentYear = LocalDate.now().getYear();
			int currentMonth = LocalDate.now().getMonthValue();
				
			LocalDate firstOfMonth = LocalDate.of(currentYear, currentMonth, 1);
			LocalDate currentDate = LocalDate.now().minusDays(1);
			
			List<TimesheetDTO> dtoList = new ArrayList<TimesheetDTO>();
			
			List<Object[]> timesheetDefaulterList = departmentRepository.getSegregatedDeptEodDefaulter(firstOfMonth,currentDate);
			if(!timesheetDefaulterList.isEmpty()) {
				
				for(Object[] object: timesheetDefaulterList) {
					TimesheetDTO timesheetDto = new TimesheetDTO();
					long days = ChronoUnit.DAYS.between(firstOfMonth, currentDate);
					Long resourceCount = object[2] != null ? Long.parseLong(object[2].toString()) : null;
					Long expectedEODCount = days * resourceCount;
					
					timesheetDto.setDepartmentName(object[0] != null ? object[0].toString() : null);
					timesheetDto.setActualEODCount(object[1] != null ? Long.parseLong(object[1].toString()) : null);
					timesheetDto.setResourceCount(object[2] != null ? Long.parseLong(object[2].toString()) : null);
					timesheetDto.setExpectedEODCount(expectedEODCount);
					
					dtoList.add(timesheetDto);
				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Defaulter List is empty");
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
