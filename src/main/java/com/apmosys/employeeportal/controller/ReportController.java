package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.service.ReportService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class ReportController {
	
	@Autowired
	ReportService reportService;
	
	@RequestMapping(value="/leaveReport" , method = RequestMethod.GET)
	public ServiceResponse leaveReport() {		
		
		ServiceResponse response =	reportService.leaveReport();
		return response;
	}
	
	@RequestMapping(value="/timesheetReport" , method = RequestMethod.GET)
	public ServiceResponse timesheetReport() {		
		
		ServiceResponse response =	reportService.timesheetReport();
		return response;
	}
	
	@RequestMapping(value="/getAccessControlListData" , method = RequestMethod.POST)
	public ServiceResponse getAccessControlListData(@RequestBody EmployeeDTO employeeDto) {		
		
		ServiceResponse response =	reportService.getAccessControlListData(employeeDto);
		return response;
	}
	
	@RequestMapping(value="/getAccessControlListByPersona" , method = RequestMethod.POST)
	public ServiceResponse getAccessControlListByPersona(@RequestBody EmployeeDTO employeeDto) {		
		
		ServiceResponse response =	reportService.getAccessControlListByPersona(employeeDto);
		return response;
	}

}
