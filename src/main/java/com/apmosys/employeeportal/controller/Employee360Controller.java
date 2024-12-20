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

}
