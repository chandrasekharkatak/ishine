package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.DepartmentDTO;
import com.apmosys.employeeportal.dto.HolidayDTO;
import com.apmosys.employeeportal.model.DepartmentHolidayMap;
import com.apmosys.employeeportal.model.Holiday;
import com.apmosys.employeeportal.repository.DepartmentHolidayMapRepository;
import com.apmosys.employeeportal.repository.DepartmentRepository;
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
	DepartmentRepository departmentRepository;
	
	@Autowired
	DepartmentHolidayMapRepository departmentHolidayMapRepository;

	@Transactional
	public ServiceResponse addHoliday(HolidayDTO holidayDTO) {
		String message = "";
		ServiceResponse response = new ServiceResponse();
		try {
			Holiday newHoliday = new Holiday();

			newHoliday.setOccasion(holidayDTO.getOccasion());
			newHoliday.setDateOfHoliday(stringToDateTimeParser.getDate(holidayDTO.getDateOfHoliday()));
			newHoliday.setDayOfTheWeek(holidayDTO.getDayOfTheWeek());
			newHoliday.setOptionalHoliday(holidayDTO.getOptionalHoliday());
			newHoliday.setCustomHoliday(holidayDTO.getCustomHoliday());

			Holiday newHolidayCreated = holidayRepository.save(newHoliday);

			if (newHolidayCreated != null) {
				
				message = "New default holiday added.";
				
				List<DepartmentHolidayMap> departmentHolidayMapList = new ArrayList<DepartmentHolidayMap>();
				
				//To map holiday to All departments , whenever a new default holiday is created.
				if(newHolidayCreated.getCustomHoliday().equals("false"))
				{
					departmentRepository.findAll().forEach((department)-> {
						DepartmentHolidayMap departmentHolidayMap = new DepartmentHolidayMap();
						departmentHolidayMap.setDeptId(department.getDeptId());
						departmentHolidayMap.setHolidayId(newHolidayCreated.getHolidayId());					
						departmentHolidayMapList.add(departmentHolidayMap);
					});
					
					List<DepartmentHolidayMap> deptHolidayMapped =	departmentHolidayMapRepository.saveAll(departmentHolidayMapList);
					
					if(deptHolidayMapped.isEmpty())
					{
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse(message+"Holiday was not mapped to all department.");
					}
					else
					{
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse(message+"Holiday mapped to all department.");
					}	
				}
				else
				{
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("New custom holiday added.");
				}				
				
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
				holiday.setDateOfHoliday(stringToDateTimeParser.getDate(holidayDTO.getDateOfHoliday()));
				holiday.setOptionalHoliday(holidayDTO.getOptionalHoliday());

				if (holiday.getCustomHoliday().equals(holidayDTO.getCustomHoliday())) {
					/*
					 * Case I : When no change in custom holiday status
					 */
					holiday.setCustomHoliday(holidayDTO.getCustomHoliday());
					message = "No changes in holiday mapping.";

				} else {

					if (holidayDTO.getCustomHoliday().equalsIgnoreCase("true")) {
						/*
						 * Case II : Updating Default holiday to Custom holiday. This will remove all
						 * mappings of default holiday from department_holiday_map table
						 */
						holiday.setCustomHoliday(holidayDTO.getCustomHoliday());
						Integer deleteCount = departmentHolidayMapRepository
								.deleteByHolidayId(holidayDTO.getHolidayId());

						message = (deleteCount > 0) ? "Default holiday removed from all departments"
								: "Default holiday was not removed from all departments";

					} else if (holidayDTO.getCustomHoliday().equalsIgnoreCase("false")) {

						// Case II : Updating Custom holiday to Default holiday. This will map new
						// default holiday to all departments

						holiday.setCustomHoliday(holidayDTO.getCustomHoliday());
						List<DepartmentHolidayMap> departmentHolidayMapList = new ArrayList<DepartmentHolidayMap>();
						departmentRepository.findAll().forEach((department) -> {
							DepartmentHolidayMap departmentHolidayMap = new DepartmentHolidayMap();
							departmentHolidayMap.setDeptId(department.getDeptId());
							departmentHolidayMap.setHolidayId(holidayDTO.getHolidayId());
							departmentHolidayMapList.add(departmentHolidayMap);
						});
						message = departmentHolidayMapRepository.saveAll(departmentHolidayMapList) != null
								? "Default holiday added to all departments"
								: "Default holiday was not added all departments";
					}

				}

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
					dto.setCustomHoliday(holiday.getCustomHoliday());
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

	public ServiceResponse getHolidayListByDeptId(HolidayDTO holidayDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> allHolidayList =	departmentHolidayMapRepository.getHolidayListByDeptId(holidayDTO.getDeptId());
			
			if (allHolidayList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Department wise holiday list is empty.");
			} else {
				List<HolidayDTO> dtoList = new ArrayList<HolidayDTO>();
				for (Object[] object : allHolidayList) {					
					
					HolidayDTO dto = new HolidayDTO();
					
					dto.setDepartmentHolidayMapId(object[0] != null?Long.parseLong(object[0].toString()):null);
					dto.setDeptId(object[1] != null?Long.parseLong(object[1].toString()):null);
					dto.setHolidayId(object[2] != null?Short.parseShort(object[2].toString()):null);
					dto.setOccasion(object[3] != null?object[3].toString():null);
					dto.setDateOfHoliday(object[4] != null?object[4].toString():null);
					dto.setDayOfTheWeek(object[5] != null?object[5].toString():null);
					dto.setOptionalHoliday(object[6] != null?object[6].toString():null);
					
					dtoList.add(dto);
				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);

			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
}
