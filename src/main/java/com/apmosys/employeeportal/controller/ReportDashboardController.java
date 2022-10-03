package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.serviceInterface.ReportDashboardService;
import com.apmosys.employeeportal.utility.ServiceResponse;


@RestController
@RequestMapping(path = "/api")
public class ReportDashboardController {
	
	@Autowired
	ReportDashboardService reportDashboardService;
	
	
	@RequestMapping(value = "/getLast8DaysLeaveReport" ,method = RequestMethod.GET)
	public ServiceResponse getLast8DaysLeaveReport() {
		
		ServiceResponse response = reportDashboardService.getLast8DaysLeaveReport();
		return response;
	}
	
	@RequestMapping(value = "/getLast9DaysTimesheetReport" ,method = RequestMethod.GET)
	public ServiceResponse getLast9DaysTimesheetReport() {
		
		ServiceResponse response = reportDashboardService.getLast9DaysTimesheetReport();
		return response;
	}

}
