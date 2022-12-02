package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.HolidayDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.Holiday;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.HolidayRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class HolidayService {

	@Autowired
	HolidayRepository holidayRepository;

	@Autowired
	StringToDateTimeParser stringToDateTimeParser;
	
	@Autowired
	EmployeeRepository employeeRepository;

	@Autowired
	private LogService logService;
	
	@Autowired
	private HttpServletRequest httpRequest;

	@Transactional
	public ServiceResponse addHoliday(HolidayDTO holidayDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("add_holidays");
		apiLogInfo.setApiUrl("/api/addHoliday");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("occasion : " + holidayDTO.getOccasion()+ "optionalHoliday : " +holidayDTO.getOptionalHoliday()+  "holidayType : " +holidayDTO.getHolidayType());
		try {
			     Holiday newHoliday = new Holiday();

			     newHoliday.setOccasion(holidayDTO.getOccasion());
			     newHoliday.setDateOfHoliday(stringToDateTimeParser.getDate(holidayDTO.getDateOfHoliday(),"yyyy-MM-dd"));
			     newHoliday.setDayOfTheWeek(holidayDTO.getDayOfTheWeek());
			     newHoliday.setOptionalHoliday(holidayDTO.getOptionalHoliday());
			     newHoliday.setState(holidayDTO.getState());
			     newHoliday.setHolidayType(holidayDTO.getHolidayType());

			     Holiday newHolidayCreated = holidayRepository.save(newHoliday);

			     if (newHolidayCreated != null) {
			     	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				    response.setServiceResponse("New holiday added.");
				    
				    apiLogInfo.setApiResponse("New holiday added.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			     } else {
			    	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				    response.setServiceResponse("Failed to add new holiday.");
				   
				    apiLogInfo.setApiResponse("Failed to add new holiday.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			     }

		} catch (Exception e) {
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

	@Transactional
	public ServiceResponse updateHoliday(HolidayDTO holidayDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("update_holidays");
		apiLogInfo.setApiUrl("/api/updateHoliday");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("holidayId : " + holidayDTO.getHolidayId());
		String message = "";
		try {
			Optional<Holiday> existingHoliday = holidayRepository.findById(holidayDTO.getHolidayId());

			if (existingHoliday.isPresent()) {
				Holiday holiday = existingHoliday.get();

				holiday.setOccasion(holidayDTO.getOccasion());
				holiday.setDayOfTheWeek(holidayDTO.getDayOfTheWeek());
				holiday.setDateOfHoliday(stringToDateTimeParser.getDate(holidayDTO.getDateOfHoliday(),"yyyy-MM-dd"));
				holiday.setOptionalHoliday(holidayDTO.getOptionalHoliday());
				holiday.setState(holidayDTO.getState());
				holiday.setHolidayType(holidayDTO.getHolidayType());
				Holiday dbResponse = holidayRepository.save(holiday);

				if (dbResponse != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Holiday updated successfully." + message);
					
					apiLogInfo.setApiResponse("Holiday updated successfully." + message);			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);	
					
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Failed to update holiday.");
					
					apiLogInfo.setApiResponse("Failed to update holiday.");	
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);	
					
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No such holiday available.");
				
				apiLogInfo.setApiResponse("No such holiday available.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);	
				
			}

		} catch (Exception e) {
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
	
	public ServiceResponse deleteHoliday(HolidayDTO holidayDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("delete_holiday");
		apiLogInfo.setApiUrl("/api/deleteHoliday");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("holidayId : " + holidayDTO.getHolidayId());
		try {
			Optional<Holiday> holidayObject = holidayRepository.findById(holidayDTO.getHolidayId());
			if (holidayObject.isPresent()) {
				Holiday holidayToBeDeleted = holidayObject.get();
				holidayRepository.deleteById(holidayToBeDeleted.getHolidayId());
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Holiday Deleted.");
				
				apiLogInfo.setApiResponse("Holiday Deleted.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Holiday Not Found.");
				
				apiLogInfo.setApiResponse("Holiday Not Found.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
		} catch (Exception e) {
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

	public ServiceResponse getAllHolidays() {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Holiday> list = holidayRepository.findAll();

			List<HolidayDTO> dtoList = new ArrayList<HolidayDTO>();

			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Holiday list is empty.");
			} else {
				list.forEach((holiday) -> {

					HolidayDTO dto = new HolidayDTO();
					dto.setHolidayId(holiday.getHolidayId());
					dto.setDateOfHoliday(holiday.getDateOfHoliday().toString());
					dto.setOccasion(holiday.getOccasion());
					dto.setDayOfTheWeek(holiday.getDayOfTheWeek());
					dto.setOptionalHoliday(holiday.getOptionalHoliday());
					dto.setState(holiday.getState());
					dto.setHolidayType(holiday.getHolidayType());
					dtoList.add(dto);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getAllHolidayByEmpWorkLocation(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("delete_holiday");
		apiLogInfo.setApiUrl("/api/getAllHolidayByEmpWorkLocation");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " + employeedto.getEmpId());
		
		try {

			List<Object[]> list = employeeRepository.getAllHolidayByEmpWorkLocation(employeedto.getEmpId());
			List<HolidayDTO> dtoList = new ArrayList<HolidayDTO>();
			
			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Holidays found");
				
				apiLogInfo.setApiResponse("No Holidays found.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			} else {

				list.forEach((object) -> {
					HolidayDTO dto = new HolidayDTO();
					dto.setOccasion(object[0] != null ? object[0].toString() : null);
					dto.setDayOfTheWeek(object[1] != null ? object[1].toString() : null);
					dto.setDateOfHoliday(object[2] != null ? object[2].toString() : null);
					dto.setState(object[3] != null ? object[3].toString() : null);
					dto.setOptionalHoliday(object[4] != null ? object[4].toString() : null);
					dtoList.add(dto);		
					});

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				
				apiLogInfo.setApiResponse("dtoList : " +dtoList);			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}

		} catch (Exception e) {
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

	public ServiceResponse checkOccasionIfAlreadyExist(HolidayDTO holidayDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("delete_holiday");
		apiLogInfo.setApiUrl("/api/checkOccasionIfAlreadyExist");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("occasion : " + holidayDTO.getOccasion());
		
		
		  try {
			  Holiday checkOccasion = holidayRepository.findByOccasion(holidayDTO.getOccasion());
			  
			  if(checkOccasion!=null) {
				  response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				  response.setServiceResponse("Occasion already exist!");
				  
				  apiLogInfo.setApiResponse("Occasion already exist!");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			  }
			
		  } catch (Exception e) {
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
