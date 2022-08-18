package com.apmosys.employeeportal.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.service.AuthenticationService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class AuthenticationController {

	@Autowired
	AuthenticationService authenticationService;

	LeaveDTO dto = new LeaveDTO();

	@Autowired
	HttpServletRequest request;

	@RequestMapping(value = "/authenticateUser", method = RequestMethod.POST)
	public ServiceResponse authenticateUser(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = authenticationService.authenticateUser(employeedto);
		return response;
	}

	@RequestMapping(value = "/authenticateUserWithOTP", method = RequestMethod.POST)
	public ServiceResponse authenticateUserWithOTP(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = authenticationService.authenticateUserWithOTP(employeedto);
		return response;
	}

	@RequestMapping(value = "/checkUserSession", method = RequestMethod.POST)
	public ServiceResponse checkUserSession(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = authenticationService.checkUserSession(employeedto);
		return response;
	}

	@RequestMapping(value = "/logoutUser", method = RequestMethod.POST)
	public ServiceResponse logoutUser(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = authenticationService.logoutUser(employeedto);
		return response;
	}
	
	@RequestMapping(value = "/checkEmailWhenForgotPassword", method = RequestMethod.POST)
	public ServiceResponse checkEmailWhenForgotPassword(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = authenticationService.checkEmailWhenForgotPassword(employeedto);
		return response;
	}
	
	@RequestMapping(value = "/checkOTPWhenForgotPassword", method = RequestMethod.POST)
	public ServiceResponse checkOTPWhenForgotPassword(@RequestBody EmployeeDTO employeedto) {

		ServiceResponse response = authenticationService.checkOTPWhenForgotPassword(employeedto);
		return response;
	}

}
