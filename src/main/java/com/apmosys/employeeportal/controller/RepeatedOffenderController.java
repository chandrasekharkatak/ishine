package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.JobRoleAccess;
import com.apmosys.employeeportal.dto.repeatedoffender.RepeatedOffenderDashboardRequest;
import com.apmosys.employeeportal.service.RepeatedOffenderService;
import com.apmosys.employeeportal.utility.ServiceResponse;

/**
 * Dedicated REST APIs for the HR dashboard "Repeated Offender" section.
 * Legacy timesheet dashboard / employee-view endpoints are not used by these routes.
 */
@RestController
@RequestMapping(path = "/api/repeated-offender")
public class RepeatedOffenderController {

	@Autowired
	private RepeatedOffenderService repeatedOffenderService;

	@JobRoleAccess(featureIds = { 15, 16 })
	@PostMapping("/summary")
	public ServiceResponse getSummary(@RequestBody RepeatedOffenderDashboardRequest request) {
		return repeatedOffenderService.getSummary(request);
	}

	@JobRoleAccess(featureIds = { 15, 16 })
	@PostMapping("/employees")
	public ServiceResponse getEmployeeGrid(@RequestBody RepeatedOffenderDashboardRequest request) {
		return repeatedOffenderService.getEmployeeGrid(request);
	}
}
