package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.ReportsQueryDTO;
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
		
	//for multiple graph queries 
	// employee status summary graph 
	@RequestMapping(value = "/getEmployeeStatusSummaryCount/{status}",method = RequestMethod.GET)
	public ServiceResponse getEmployeeStatusSummaryCount()
	{
		ServiceResponse response = reportDashboardService.getEmployeeStatusSummary();
		return response;
	}
	
	@RequestMapping(value = "/getEmployeeStatusSummary/{status}",method = RequestMethod.GET)
	public ServiceResponse getEmployeeStatusSummary()
	{
		ServiceResponse response = reportDashboardService.getEmployeeStatusSummary();
		return response;
	}
	
	
	// employee gender summary graph
	@RequestMapping(value = "/getEmployeeGenderSummaryCount/{gender}",method = RequestMethod.GET)
	public ServiceResponse getEmployeeGenderSummaryCount()
	{
		ServiceResponse response = reportDashboardService.getEmployeeGenderSummaryCount();
		return response;
	}
	
	@RequestMapping(value = "/getEmployeeGenderSummary/{gender}",method = RequestMethod.GET)
	public ServiceResponse getEmployeeGenderSummary()
	{
		ServiceResponse response = reportDashboardService.getEmployeeGenderSummary();
		return response;
	}
	
	@RequestMapping(value = "/getEmployeeAgeSummaryCount/{ageRange}",method = RequestMethod.GET)
	public ServiceResponse getEmployeeAgeSummaryCount()
	{
		ServiceResponse response = reportDashboardService.getEmployeeAgeSummaryCount();
		return response;
	}
	
	@RequestMapping(value = "/getEmployeeAgeSummary/{ageRange}",method = RequestMethod.GET)
	public ServiceResponse getEmployeeAgeSummary()
	{
		ServiceResponse response = reportDashboardService.getEmployeeAgeSummary();
		return response;
	}
	
	
	

	@RequestMapping(value = "/getDepartmentWiseKycCount" ,method = RequestMethod.GET)
	public ServiceResponse getDepartmentWiseEmployeeCount() {
		
		ServiceResponse response = reportDashboardService.getDepartmentWiseKycCount();
		return response;
	}
	
	@RequestMapping(value = "/getJoiningVsResignationCount" ,method = RequestMethod.POST)
	public ServiceResponse getEmployeeExperienceCount(@RequestBody ReportsQueryDTO request) {
		
		ServiceResponse response = reportDashboardService.getJoiningVsResignationCount(request);
		return response;
	}
	
	@RequestMapping(value = "/getAllGraphEmployeeSummary" , method = RequestMethod.GET)
	public ServiceResponse getAllGraphEmployeeSummary()
	{
		ServiceResponse response = reportDashboardService.getAllGraphEmployeeSummary();
		return response;
	}

}
