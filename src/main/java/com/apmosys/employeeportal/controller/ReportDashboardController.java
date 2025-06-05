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
	
	
	@RequestMapping(value = "/getLast8DaysLeaveReport" ,method = RequestMethod.POST)
	public ServiceResponse getLast8DaysLeaveReport(@RequestBody LeaveDTO leaveDto) {
		
		ServiceResponse response = reportDashboardService.getLast8DaysLeaveReport(leaveDto);
		return response;
	}
	
	@RequestMapping(value = "/getLast9DaysTimesheetReport" ,method = RequestMethod.GET)
	public ServiceResponse getLast9DaysTimesheetReport() {
		
		ServiceResponse response = reportDashboardService.getLast9DaysTimesheetReport();
		return response;
	}
	
	@RequestMapping(value = "/getLeaveTrendAnalysisReport" ,method = RequestMethod.POST)
	public ServiceResponse getLeaveTrendAnalysisReport(@RequestBody LeaveDTO leaveDto) {
		
		ServiceResponse response = reportDashboardService.getLeaveTrendAnalysisReport(leaveDto);
		return response;
	}
	
	@RequestMapping(value = "/getEmployeeWorkLocationForSummary" ,method = RequestMethod.GET)
	public ServiceResponse getEmployeeWorkLocationForSummary() {
		
		ServiceResponse response = reportDashboardService.getEmployeeWorkLocationForSummary();
		return response;
	}
	
	@RequestMapping(value = "/getDepartmentWiseBillableData" ,method = RequestMethod.POST)
	public ServiceResponse getDepartmentWiseBillableData(@RequestBody LeaveDTO leaveDto) {
		
		ServiceResponse response = reportDashboardService.getDepartmentWiseBillableData(leaveDto);
		return response;
	}
	
	//Fresher Lateral
	@RequestMapping(value = "/countforFresherLateral",method = RequestMethod.GET)
	public ServiceResponse countforFresherLateral()
	{
		ServiceResponse response = reportDashboardService.countforFresherLateral();
		return response;
	}
	
	@RequestMapping(value = "/tableforFresherLateral",method = RequestMethod.GET)
	public ServiceResponse tableforFresherLateral()
	{
		ServiceResponse response = reportDashboardService.tableforFresherLateral();
		return response;
	}
	
	//Billable Employee Summary
	@RequestMapping(value = "/countforBillabeEmployee",method = RequestMethod.GET)
	public ServiceResponse countforBillabeEmployee()
	{
		ServiceResponse response = reportDashboardService.countforBillabeEmployee();
		return response;
	}
	@RequestMapping(value = "/tableforBillabeEmployee",method = RequestMethod.GET)
	public ServiceResponse tableforBillabeEmployee()
	{
		ServiceResponse response = reportDashboardService.tableforBillabeEmployee();
		return response;
	}
	
	
	

}
