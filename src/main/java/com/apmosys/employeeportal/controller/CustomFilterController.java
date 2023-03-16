package com.apmosys.employeeportal.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.CustomFilterDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.service.CustomFilterService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api")
public class CustomFilterController {

	@Autowired
	CustomFilterService customFilterService;

	@RequestMapping(value = "/customQueryForLeaveReport", method = RequestMethod.POST)
	public ServiceResponse customQueryForLeaveReport(@RequestBody LeaveDTO leaveDTO) {
		ServiceResponse response = customFilterService.customQueryForLeaveReport(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/customQueryForEmployeeReport", method = RequestMethod.POST)
	public ServiceResponse customQueryForEmployeeReport(@RequestBody EmployeeDTO employeeDTO) {
		ServiceResponse response = customFilterService.customQueryForEmployeeReport(employeeDTO);
		return response;
	}
	
	@RequestMapping(value = "/customQueryForLeaveTrendAnalysisReport", method = RequestMethod.POST)
	public ServiceResponse customQueryForLeaveTrendAnalysisReport(@RequestBody LeaveDTO leaveDTO) {
		ServiceResponse response = customFilterService.customQueryForLeaveTrendAnalysisReport(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/customTimesheetApplicationReport", method = RequestMethod.POST)
	public ServiceResponse customTimesheetApplicationReport(@RequestBody TimesheetDTO timesheetDTO) {
		ServiceResponse response = customFilterService.customTimesheetApplicationReport(timesheetDTO);
		return response;
	}
	
	@RequestMapping(value = "/customQueryForTimesheetSummaryChart", method = RequestMethod.POST)
	public ServiceResponse customQueryForTimesheetSummaryChart(@RequestBody TimesheetDTO timesheetDTO) {
		ServiceResponse response = customFilterService.customQueryForTimesheetSummaryChart(timesheetDTO);
		return response;
	}
	
	@RequestMapping(value = "/getCustomFilteredTimesheet", method = RequestMethod.POST)
	public ServiceResponse getCustomFilteredTimesheet(@RequestBody TimesheetDTO timesheetDTO) {
		ServiceResponse response = customFilterService.getCustomFilteredTimesheet(timesheetDTO);
		return response;
	}
	
	@RequestMapping(value = "/getValueOptionData", method = RequestMethod.POST)
	public ServiceResponse getValueOptionData(@RequestBody CustomFilterDTO customFilterDTO) {
		ServiceResponse response = customFilterService.getValueOptionData(customFilterDTO);
		return response;
	}
	
	@RequestMapping(value = "/getCustomQueryData", method = RequestMethod.POST)
	public ServiceResponse getCustomQueryData(@RequestBody CustomFilterDTO customFilterDTO) {
		ServiceResponse response = customFilterService.getCustomQueryData(customFilterDTO);
		return response;
	}
}
