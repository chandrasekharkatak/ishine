package com.apmosys.employeeportal.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.PieChartListDTO;
import com.apmosys.employeeportal.dto.PieParamDTO;
import com.apmosys.employeeportal.dto.ReportCountDTO;
import com.apmosys.employeeportal.dto.ReportsQueryDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.service.CustomFilterService;
import com.apmosys.employeeportal.serviceInterface.ReportDashboardService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class ReportDashboardController {

	@Autowired
	ReportDashboardService reportDashboardService;
	
	@Autowired
	private CustomFilterService customFilterService;

	@RequestMapping(value = "/getLast8DaysLeaveReport", method = RequestMethod.POST)
	public ServiceResponse getLast8DaysLeaveReport(@RequestBody LeaveDTO leaveDto) {

		ServiceResponse response = reportDashboardService.getLast8DaysLeaveReport(leaveDto);
		return response;
	}

	@RequestMapping(value = "/getLast9DaysTimesheetReport", method = RequestMethod.GET)
	public ServiceResponse getLast9DaysTimesheetReport() {

		ServiceResponse response = reportDashboardService.getLast9DaysTimesheetReport();
		return response;
	}

	@RequestMapping(value = "/getLeaveTrendAnalysisReport", method = RequestMethod.POST)
	public ServiceResponse getLeaveTrendAnalysisReport(@RequestBody LeaveDTO leaveDto) {

		ServiceResponse response = reportDashboardService.getLeaveTrendAnalysisReport(leaveDto);
		return response;
	}

	@RequestMapping(value = "/getEmployeeWorkLocationForSummary", method = RequestMethod.GET)
	public ServiceResponse getEmployeeWorkLocationForSummary() {

		ServiceResponse response = reportDashboardService.getEmployeeWorkLocationForSummary();
		return response;
	}

	@RequestMapping(value = "/getDepartmentWiseBillableData", method = RequestMethod.POST)
	public ServiceResponse getDepartmentWiseBillableData(@RequestBody LeaveDTO leaveDto) {

		ServiceResponse response = reportDashboardService.getDepartmentWiseBillableData(leaveDto);
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
	
	@RequestMapping(value = "/getAllPieGraphListSummary" , method = RequestMethod.POST)
	public ServiceResponse getAllPieGraphListSummary(@RequestBody PieParamDTO pieParamDto)
	{
		ServiceResponse response = reportDashboardService.getAllPieGraphListSummary(pieParamDto);
		return response;
	}
	@RequestMapping(value = "/getEmployeeBillableAndNonBillable",method = RequestMethod.POST)
	public ServiceResponse getEmployeeBillableAndNonBillable(@RequestBody ReportsQueryDTO reportqueryDTO)
	{
		ServiceResponse response = new ServiceResponse();
		response = reportDashboardService.getEmployeeDetailsByDepartmentAndBillableType(reportqueryDTO);
		return response;
	}

	
	@RequestMapping(value = "/getAllEmployeeCountDepartmentWise",method = RequestMethod.GET)
	public  ServiceResponse getAllEmployeeCountDepartmentWise() {
		ServiceResponse	response=reportDashboardService.getAllEmployeeCountDepartmentWise();
		return response;
	}
	
	 @PostMapping("/getDepartmentWiseBillableNonBillableSummary")
	public ServiceResponse getDepartmentWiseBillableNonBillableSummary(
	        @RequestBody ReportsQueryDTO request) {
	    ServiceResponse response = reportDashboardService.getDepartmentWiseBillableNonBillableSummary(request);
        return response;
	}
	 
	 @PostMapping("/getEmployeeDetailsByEmploymentType")
	public ServiceResponse getEmployeeDetailsByEmploymentType(
	        @RequestBody ReportsQueryDTO request) {
	    ServiceResponse response = reportDashboardService.getEmployeeDetailsByEmploymentType(request);
        return response;
	}
	 
	 @RequestMapping(value = "/getEmployeesByExperience",method = RequestMethod.POST)
		public  ServiceResponse getEmployeesByExperience(@RequestBody ReportsQueryDTO reportqueryDTO) {
			ServiceResponse	response=reportDashboardService.getEmployeesByExperience(reportqueryDTO);
			return response;
		}
	 
	 @RequestMapping(value = "/getEmployeeDetailsByDepartmentAndKyc",method = RequestMethod.POST)
		public  ServiceResponse getEmployeeDetailsByDepartmentAndKyc(@RequestBody ReportsQueryDTO reportqueryDTO) {
			ServiceResponse	response=reportDashboardService.getEmployeeDetailsByDepartmentAndKyc(reportqueryDTO);
			return response;
		}
		
		@RequestMapping(value = "/getDepartmentwiseEmployeesByType",method = RequestMethod.POST)
		public  ServiceResponse getDepartmentwiseEmployeesByType(@RequestBody ReportsQueryDTO reportqueryDTO) {
			ServiceResponse	response=reportDashboardService.getDepartmentwiseEmployeesByType(reportqueryDTO);
			return response;
		}
		
		@RequestMapping(value = "/getJoinVsResignEmployeeDetails" ,method = RequestMethod.POST)
		public ServiceResponse getJoinVsResignEmployeeDetails(@RequestBody ReportsQueryDTO request) {
			
			ServiceResponse response = reportDashboardService.getJoinVsResignEmployeeDetails(request);
			return response;
		}
		
		   @PostMapping("/customgetLeaveTrendDetails") // Changed from GET to POST
		    public ServiceResponse getLeaveTrendDetails(@RequestBody ReportsQueryDTO request) {
		        return reportDashboardService.customgetLeaveTrendDetails(request);
		    }
		@RequestMapping(value = "/getLeaveTrendAnalysis" ,method = RequestMethod.POST)
		public ServiceResponse getLeaveTrendAnalysis(@RequestBody ReportsQueryDTO request) {
			
			ServiceResponse response = reportDashboardService.getLeaveTrendAnalysis(request);
			return response;
		}
		
		@RequestMapping(value = "/getWorkLocationDetails",method = RequestMethod.GET)
		public  ServiceResponse getWorkLocationDetails() {
			ServiceResponse	response=reportDashboardService.getWorkLocationDetails();
			return response;
		}
		
		@RequestMapping(value = "/getWorkLocationSummaryDetails" ,method = RequestMethod.POST)
		public ServiceResponse getWorkLocationSummaryDetails(@RequestBody ReportsQueryDTO request) {
			
			ServiceResponse response = reportDashboardService.getWorkLocationSummaryDetails(request);
			return response;
		}
		@RequestMapping(value = "/getLeaveTrendDetails",method = RequestMethod.GET)
		public  ServiceResponse getLeaveTrendDetails() {
			ServiceResponse	response=reportDashboardService.getLeaveTrendDetails();
			return response;
		}
		
		@PostMapping("/graph-employee-summary")
	    public ServiceResponse getAllGraphEmployeeSummary(@RequestBody ReportsQueryDTO request) {
	        ServiceResponse response = reportDashboardService.customGetAllGraphEmployeeSummary(request);
	        return response;
	    }
		
	    @PostMapping("/customgetJoiningVsResignationCount")
	    public ServiceResponse customGetJoiningVsResignationCount(@RequestBody ReportsQueryDTO request) {
	        return reportDashboardService.customGetJoiningVsResignationCount(request);
	    }
	    
	    @PostMapping("/work-location-details")
	    public ServiceResponse getWorkLocationDetails(@RequestBody ReportsQueryDTO request) {
	        ServiceResponse response = reportDashboardService.customGetWorkLocationDetails(request);
	        return response;
	    }
	    
		@RequestMapping(value = "/getAllResignedEmployees",method = RequestMethod.GET)
		public  ServiceResponse getAllResignedEmployees(@RequestParam(defaultValue = "1") int page,
		        @RequestParam(defaultValue = "10") int size,
		        @RequestParam(defaultValue = "name") String sortBy) {
      ServiceResponse	response=reportDashboardService.getAllResignedEmployees(page, size, sortBy);
			return response;
		}

}
