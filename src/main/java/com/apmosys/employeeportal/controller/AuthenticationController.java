package com.apmosys.employeeportal.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.service.AuthenticationService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class AuthenticationController {
	
	@Autowired
	AuthenticationService authenticationService;
	
	@RequestMapping(value="/authenticateUser" , method = RequestMethod.POST)
	public ServiceResponse authenticateUser(@RequestBody EmployeeDTO employeedto , HttpSession session) {		
		
		ServiceResponse response =	authenticationService.authenticateUser(employeedto , session);		
		return response;
	}
	
	@RequestMapping(value="/authenticateUserWithOTP" , method = RequestMethod.POST)
	public ServiceResponse authenticateUserWithOTP(@RequestBody EmployeeDTO employeedto, HttpSession session) {		
		
		ServiceResponse response =	authenticationService.authenticateUserWithOTP(employeedto,session);		
		return response;
	}
	
	

}
