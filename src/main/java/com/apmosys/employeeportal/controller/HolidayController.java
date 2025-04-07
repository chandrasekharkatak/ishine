package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.DepartmentDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.HolidayDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.service.HolidayService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class HolidayController {

	@Autowired
	HolidayService holidayService;

	@RequestMapping(value = "/addHoliday", method = RequestMethod.POST)
	public ServiceResponse addHoliday(@RequestBody HolidayDTO holidayDTO) {

		ServiceResponse response = holidayService.addHoliday(holidayDTO);
		return response;
	}

	@RequestMapping(value = "/updateHoliday", method = RequestMethod.POST)
	public ServiceResponse updateHoliday(@RequestBody HolidayDTO holidayDTO) {

		ServiceResponse response = holidayService.updateHoliday(holidayDTO);
		return response;
	}
	
	@RequestMapping(value = "/deleteHoliday", method = RequestMethod.POST)
	public ServiceResponse deleteHoliday(@RequestBody HolidayDTO holidayDTO) {

		ServiceResponse response = holidayService.deleteHoliday(holidayDTO);
		return response;
	}

	@RequestMapping(value = "/getAllHolidays", method = RequestMethod.POST)
	public ServiceResponse getAllHolidays(@RequestBody HolidayDTO holidayDTO) {

		ServiceResponse response = holidayService.getAllHolidays(holidayDTO);
		return response;
	}
	
	@RequestMapping(value = "/getAllHoliday", method = RequestMethod.GET)
	public ServiceResponse getAllHolidays() {

		ServiceResponse response = holidayService.getAllHoliday();
		return response;
	}
	
	@RequestMapping(value = "/getAllHolidayByEmpWorkLocation", method = RequestMethod.POST)
	public ServiceResponse getAllHolidayByWorkLocation(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = holidayService.getAllHolidayByEmpWorkLocation(employeedto);
		return response;
	}
	
	@RequestMapping(value = "/checkOccasionIfAlreadyExist", method = RequestMethod.POST)
	public ServiceResponse checkOccasionIfAlreadyExist(@RequestBody HolidayDTO holidayDTO) {

		ServiceResponse response = holidayService.checkOccasionIfAlreadyExist(holidayDTO);
		return response;
	}

	
	@RequestMapping(value = "/getHolidayWeekOffSize", method = RequestMethod.POST)
	public ServiceResponse getHolidayWeekOffSize(@RequestBody HolidayDTO holidayDTO) {

		ServiceResponse response = holidayService.getHolidayWeekOffSize(holidayDTO);
		return response;
	}
	
	@RequestMapping(value = "/reconsileHolidayTimesheet" ,method = RequestMethod.POST)
	public ServiceResponse addTimesheetForHolidays(@RequestBody HolidayDTO holidayDTO) {
		
		ServiceResponse response = holidayService.reconsileHolidayTimesheet(holidayDTO);
		return response;
	}
	
}
