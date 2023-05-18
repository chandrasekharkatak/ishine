package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.JobRoleDTO;
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
	
	@RequestMapping(value="/getMappedSubFeatureList" , method = RequestMethod.POST)
	public ServiceResponse getMappedSubFeatureList(@RequestBody EmployeeDTO employeeDto) {		
		
		ServiceResponse response =	reportService.getMappedSubFeatureList(employeeDto);
		return response;
	}
	
	@RequestMapping(value="/getAllSubFeatureList" , method = RequestMethod.GET)
	public ServiceResponse getAllSubFeatureList() {		
		
		ServiceResponse response =	reportService.getAllSubFeatureList();
		return response;
	}
	
	@RequestMapping(value = "/getDefaultMapping", method = RequestMethod.GET)
	public ServiceResponse getDefaultMapping() {

		ServiceResponse response = reportService.getDefaultMapping();
		return response;
	}
	
	@RequestMapping(value = "/updateDefaultFeatureMapping", method = RequestMethod.POST)
	public ServiceResponse updateDefaultFeatureMapping(@RequestBody JobRoleDTO jobRoleDTO) {

		ServiceResponse response = reportService.updateDefaultFeatureMapping(jobRoleDTO);
		return response;
	}

}
