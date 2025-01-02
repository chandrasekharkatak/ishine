package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.service.DraftEmployeeService;
import com.apmosys.employeeportal.service.Employee360Service;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class Employee360Controller {
	
	@Autowired
	Employee360Service employee360Service;
	
	@RequestMapping(value = "/getLeaveDataPerMonthByEmpId", method = RequestMethod.GET)
	public ServiceResponse getLeaveDataPerMonthByEmpId(@RequestParam Long empId) {

		ServiceResponse response = employee360Service.getLeaveDataPerMonthByEmpId(empId);
		return response;
	}
	
	@RequestMapping(value = "/getEmployeeDetails", method = RequestMethod.GET)
	public ServiceResponse getEmployeeDetails(@RequestParam Long empId) {

		ServiceResponse response = employee360Service.getEmployeeDetails(empId);
		return response;
	}
	
	@RequestMapping(value = "/get360TimesheetDetails", method = RequestMethod.GET)
	public ServiceResponse get360TimesheetDetails(@RequestParam String status, @RequestParam long empId,
			@RequestParam long projectId,@RequestParam String teamName,@RequestParam long managerId) {

		ServiceResponse response = employee360Service.get360TimesheetDetails(status,empId,projectId,teamName,managerId);
		return response;
	}
	
	@RequestMapping(value = "/updateStatus", method = RequestMethod.GET)
	public ServiceResponse updateStatus(@RequestParam String status, @RequestParam long empId,@RequestParam String date) {

		ServiceResponse response = employee360Service.updateStatus(status,empId,date);
		return response;
	}

	
}
