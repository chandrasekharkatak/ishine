package com.apmosys.employeeportal.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.GetTeamAndTimesheetDetailsDTO;
import com.apmosys.employeeportal.request.EmployeeTimesheetProjectRequest;
import com.apmosys.employeeportal.service.EmployeeService;
import com.apmosys.employeeportal.utility.PoPortalAPIAuthenticationJWTUtility;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class EmployeeController {

	@Autowired
	EmployeeService employeeService;

	@Autowired
	PoPortalAPIAuthenticationJWTUtility poPortalAPIAuthenticationJWTUtility;

	@GetMapping(value = "/getAllEmployeeInfo")
	public ServiceResponse employeeInfo(HttpServletRequest httpRequest) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		return employeeService.getAllEmployeeInfo();
	}

	 @PostMapping("/getEmployeeAndTimesheetDetails")
	 public ServiceResponse getEmployeeAndTimesheetDetails(HttpServletRequest request,@RequestBody EmployeeTimesheetProjectRequest employeeTimesheetRequest) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(request);
		ServiceResponse response = new ServiceResponse();
		response = employeeService.getEmployeeAndTimesheetDetails(employeeTimesheetRequest);
		return response;
	 }

	@PostMapping("/getTeamAndTimeSheetDetails")
	public ServiceResponse getTeamAndTimeSheetDetails(HttpServletRequest request,
			@RequestBody GetTeamAndTimesheetDetailsDTO teamAndTimesheetDetailsDTO) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(request);
		return employeeService.getTeamAndTimeSheetDetails(teamAndTimesheetDetailsDTO);
	}

	@PostMapping("/getProjectDetailsByEmpIdAndDateRange")
	public ServiceResponse getProjectDetailsByEmpIdAndDateRange(HttpServletRequest request,
			@RequestBody EmployeeTimesheetProjectRequest employeeTimesheetRequest) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(request);
		return employeeService.getProjectDetailsByEmpIdAndDateRange(employeeTimesheetRequest);
	}
}
