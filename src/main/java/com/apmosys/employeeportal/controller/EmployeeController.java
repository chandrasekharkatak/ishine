package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.service.EmployeeService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
public class EmployeeController {
	
	
	@Autowired
	EmployeeService employeeService;
	
	@RequestMapping(value="/createEmployee" , method = RequestMethod.POST)
	public ServiceResponse createEmployee(@RequestBody EmployeeDTO employeedto) {		
		
		ServiceResponse response =	employeeService.createEmployee(employeedto);		
		return response;
	}
	
	@RequestMapping(value="/getEmployeeByEmpId" , method = RequestMethod.POST)
	public ServiceResponse getEmployeeByEmpId(@RequestBody EmployeeDTO employeedto) {		
		
		ServiceResponse response = employeeService.getEmployeeByEmpId(employeedto);
		return response;
	}
	
	@RequestMapping(value="/getAllEmployees" , method = RequestMethod.GET)
	public ServiceResponse getAllEmployees() {		
		
		ServiceResponse response =employeeService.getAllEmployees();
		return response;
		
	}
	
	@RequestMapping(value="/updateEmployeeByEmpId" , method = RequestMethod.POST)
	public ServiceResponse updateEmployeeByEmpId(@RequestBody EmployeeDTO employeedto) {		
		
		ServiceResponse response =employeeService.updateEmployeeByEmpId(employeedto);
		return response;		
	}
	
	@RequestMapping(value="/deleteEmployeeByEmpId" , method = RequestMethod.POST)
	public ServiceResponse deleteEmployeeByEmpId(@RequestBody EmployeeDTO employeedto) {		
		
		ServiceResponse response =employeeService.deleteEmployeeByEmpId(employeedto);
		return response;
	}

}
