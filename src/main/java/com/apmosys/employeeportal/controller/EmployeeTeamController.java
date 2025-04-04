package com.apmosys.employeeportal.controller;

import java.util.List;

import org.hibernate.service.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.service.EmployeeService;
import com.apmosys.employeeportal.service.employeeDeptService;
import com.apmosys.employeeportal.utility.ServiceResponse;


@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping(path = "/api")
public class EmployeeTeamController {
	
	@Autowired
	private employeeDeptService employeedeptservice;
	
//	@Autowired
//	private employeeDeptService employeeService;
	
    @GetMapping("/department-colleagues/{hodId}")
    public ServiceResponse getDepartmentColleagues(@PathVariable Long hodId) {
    	ServiceResponse response = employeedeptservice.findEmployeesInSameDepartmentAsCurrentUser(hodId);
    	
    	return response;
    }

}	
