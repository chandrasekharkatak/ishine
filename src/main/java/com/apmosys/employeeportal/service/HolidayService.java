package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.HolidayDTO;
import com.apmosys.employeeportal.model.Holiday;
import com.apmosys.employeeportal.repository.HolidayRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class HolidayService {

	@Autowired
	HolidayRepository holidayRepository;

	@Autowired
	StringToDateTimeParser stringToDateTimeParser;

	public ServiceResponse addHoliday(HolidayDTO holidayDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			Holiday newHoliday = new Holiday();

			newHoliday.setOccasion(holidayDTO.getOccasion());
			newHoliday.setDateOfHoliday(stringToDateTimeParser.getDate(holidayDTO.getDateOfHoliday()));
			newHoliday.setDayOfTheWeek(holidayDTO.getDayOfTheWeek());

			Holiday dbResponse = holidayRepository.save(newHoliday);

			if (dbResponse != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("New holiday Added.");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Failed to add new holiday.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse updateHoliday(HolidayDTO holidayDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			Optional<Holiday> existingHoliday = holidayRepository.findById(holidayDTO.getHolidayId());
			
			if(existingHoliday.isPresent())
			{
				Holiday holiday = existingHoliday.get();
				
				holiday.setOccasion(holidayDTO.getOccasion());
				holiday.setDayOfTheWeek(holidayDTO.getDayOfTheWeek());
				holiday.setDateOfHoliday(stringToDateTimeParser.getDate(holidayDTO.getDateOfHoliday()));
				
				Holiday dbResponse = holidayRepository.save(holiday);
				
				if (dbResponse != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Holiday updated successfully.");
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Failed to update holiday.");
				}
			}
			else
			{
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

}
