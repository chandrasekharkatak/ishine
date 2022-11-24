package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.ActivityDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.service.EmployeeExitService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class EmployeeExitController {
	
	@Autowired
	EmployeeExitService employeeExitService;

	@RequestMapping(value = "/getEmployeeResignationDetails" ,method = RequestMethod.POST)
	public ServiceResponse getEmployeeResignationDetails(@RequestBody EmployeeDTO employeeDTO) {
		
		ServiceResponse response = employeeExitService.getEmployeeResignationDetails(employeeDTO);
		return response;
	}
	
	@RequestMapping(value = "/updateEmployeeResignationDetails" ,method = RequestMethod.POST)
	public ServiceResponse updateEmployeeResignationDetails(@RequestBody EmployeeDTO employeeDTO) {
		
		ServiceResponse response = employeeExitService.updateEmployeeResignationDetails(employeeDTO);
		return response;
	}
	
	@RequestMapping(value = "/getEmployeeExitAssetDetails" ,method = RequestMethod.POST)
	public ServiceResponse getEmployeeExitAssetDetails(@RequestBody EmployeeDTO employeeDTO) {
		
		ServiceResponse response = employeeExitService.getEmployeeExitAssetDetails(employeeDTO);
		return response;
	}
	
}
