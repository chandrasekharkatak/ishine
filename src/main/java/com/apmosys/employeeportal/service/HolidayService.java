package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.HolidayDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
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


	@Transactional
	public ServiceResponse addHoliday(HolidayDTO holidayDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			LocalDate holidayDtoDateOfHoliday = stringToDateTimeParser.getDate(holidayDTO.getDateOfHoliday(),"yyyy-MM-dd");
			LocalDate dbDateOfHoliday = null;
			String dbState = null;
			String dbOccasion = null;

			List<Holiday> holidayData = holidayRepository.findAll();
			for(Holiday dbHolidayData : holidayData) {
				dbDateOfHoliday = dbHolidayData.getDateOfHoliday();
				dbState = dbHolidayData.getState();
				dbOccasion = dbHolidayData.getOccasion();
			}
			
			if(holidayDtoDateOfHoliday.equals(dbDateOfHoliday) && holidayDTO.getState().equals(dbState) && holidayDTO.getOccasion().equals(dbOccasion)) {
				
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Holiday already present on this Date");
				
			}else {
			
			     Holiday newHoliday = new Holiday();

			     newHoliday.setOccasion(holidayDTO.getOccasion());
			     newHoliday.setDateOfHoliday(stringToDateTimeParser.getDate(holidayDTO.getDateOfHoliday(),"yyyy-MM-dd"));
			     newHoliday.setDayOfTheWeek(holidayDTO.getDayOfTheWeek());
			     newHoliday.setOptionalHoliday(holidayDTO.getOptionalHoliday());
			     newHoliday.setState(holidayDTO.getState());

			     Holiday newHolidayCreated = holidayRepository.save(newHoliday);

			     if (newHolidayCreated != null) {
			     	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				    response.setServiceResponse("New holiday added.");

			     } else {
			    	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				    response.setServiceResponse("Failed to add new holiday.");
			     }
		  }	

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	@Transactional
	public ServiceResponse updateHoliday(HolidayDTO holidayDTO) {
		ServiceResponse response = new ServiceResponse();
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
				Holiday dbResponse = holidayRepository.save(holiday);

				if (dbResponse != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Holiday updated successfully." + message);
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Failed to update holiday.");
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No such holiday available.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
	public ServiceResponse deleteHoliday(HolidayDTO holidayDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			Optional<Holiday> holidayObject = holidayRepository.findById(holidayDTO.getHolidayId());
			if (holidayObject.isPresent()) {
				Holiday holidayToBeDeleted = holidayObject.get();
				holidayRepository.deleteById(holidayToBeDeleted.getHolidayId());
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Holiday Deleted.");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Holiday Not Found.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
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
		
		try {

			List<Object[]> list = employeeRepository.getAllHolidayByEmpWorkLocation(employeedto.getEmpId());
			List<HolidayDTO> dtoList = new ArrayList<HolidayDTO>();
			
			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Holidays found");
			} else {

				list.forEach((object) -> {
					HolidayDTO dto = new HolidayDTO();
					dto.setOccasion(object[0] != null ? object[0].toString() : null);
					dto.setDayOfTheWeek(object[1] != null ? object[1].toString() : null);
					dto.setDateOfHoliday(object[2] != null ? object[2].toString() : null);
					dto.setState(object[3] != null ? object[3].toString() : null);
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

	public ServiceResponse checkOccasionIfAlreadyExist(HolidayDTO holidayDTO) {
		ServiceResponse response = new ServiceResponse();
		
		  try {
			  Holiday checkOccasion = holidayRepository.findByOccasion(holidayDTO.getOccasion());
			  
			  if(checkOccasion!=null) {
				  response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				  response.setServiceResponse("Occasion already exist!");
			  }
			
		  } catch (Exception e) {
			   e.printStackTrace();
			   response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			   response.setServiceResponse("Something Went Wrong.");
			   response.setServiceError(e.getMessage());
		  }
		  return response;
	}
	
}
