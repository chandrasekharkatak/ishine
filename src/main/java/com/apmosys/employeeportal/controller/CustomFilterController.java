package com.apmosys.employeeportal.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.CustomFilterDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.service.CustomFilterService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api")
public class CustomFilterController {

	@Autowired
	CustomFilterService customFilterService;
	
//	@RequestMapping(value = "/createCustomQueryForLeaveReport", method = RequestMethod.POST)
//	public ServiceResponse createCustomQueryForLeaveReport(@RequestBody CustomFilterDTO customFilterDTO) {
//		ServiceResponse response = customFilterService.createCustomQueryForLeaveReport(customFilterDTO);
//		return response;
//	}
	
	@RequestMapping(value = "/customQueryForLeaveReport", method = RequestMethod.POST)
	public ServiceResponse customQueryForLeaveReport(@RequestBody LeaveDTO leaveDTO) {
		ServiceResponse response = customFilterService.customQueryForLeaveReport(leaveDTO);
		return response;
	}
	
}
