package com.apmosys.employeeportal.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.apmosys.employeeportal.dto.*;
import com.apmosys.employeeportal.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.service.CustomFilterService;
import com.apmosys.employeeportal.utility.ServiceResponse;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class CustomFilterController {

	@Autowired
	CustomFilterService customFilterService;
	@Autowired
	private HttpServletRequest httpRequest;

	@Autowired
	private LogService logService;

	@RequestMapping(value = "/customQueryForLeaveReport", method = RequestMethod.POST)
	public ServiceResponse customQueryForLeaveReport(@RequestBody LeaveDTO leaveDTO) {
		ServiceResponse response = customFilterService.customQueryForLeaveReport(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/getCustomLAttendanceApplicationsList", method = RequestMethod.POST)
	public ServiceResponse getCustomLAttendanceApplicationsList(@RequestBody LeaveDTO leaveDTO) {
		ServiceResponse response = customFilterService.getCustomLAttendanceApplicationsList(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/customQueryForEmployeeReport", method = RequestMethod.POST)
	public ServiceResponse customQueryForEmployeeReport(@RequestBody EmployeeDTO employeeDTO) {
		System.out.println("employeeDTO  ::  "+employeeDTO);
		ServiceResponse response = customFilterService.customQueryForEmployeeReport(employeeDTO);
		return response;
	}
	
//	added by anurag for billable / non-billable dashboard report
	
	@RequestMapping(value = "/customQueryForDepartmentWiseBillableEmployeeReport", method = RequestMethod.POST)
	public ServiceResponse customQueryForDepartmentWiseBillableEmployeeReport(@RequestBody Long[] ids) {
		System.out.println("In controller "+ids);
		List<Long> deptIds = new ArrayList<Long>(Arrays.asList(ids));
		ServiceResponse response = customFilterService.customQueryForDepartmentWiseBillableEmployeeReport(deptIds);
		return response;
	}
	
	@RequestMapping(value = "/customQueryForLeaveTrendAnalysisReport", method = RequestMethod.POST)
	public ServiceResponse customQueryForLeaveTrendAnalysisReport(@RequestBody LeaveDTO leaveDTO) {
		ServiceResponse response = customFilterService.customQueryForLeaveTrendAnalysisReport(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/customTimesheetApplicationReport", method = RequestMethod.POST)
	public ServiceResponse customTimesheetApplicationReport(@RequestBody TimesheetDTO timesheetDTO) {
	    return customFilterService.customTimesheetApplicationReport(timesheetDTO);
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
	
//	added by anurag for document
	
	@RequestMapping(value = "/customQueryForDocument", method = RequestMethod.POST)
	public ServiceResponse customQueryForDocument(@RequestBody NewsletterDTO newsletterDto) {
		ServiceResponse response = customFilterService.customQueryForDocument(newsletterDto);
		return response;
	}


	//get Custom Query Filtered Data
	@PostMapping("/getFilteredQueryData")
	public ServiceResponse getFilteredQueryData(@RequestBody QueryRequestDTO requestDTO) {

		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();

		apiLogInfo.setApiUrl("/api/getFilteredQueryData");
		apiLogInfo.setLogLevel("INFO");

		StringBuilder logBuilder = new StringBuilder();

		try {

			// Validation
			if (requestDTO == null || requestDTO.getCustomQuery() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Request or query cannot be null");

				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiResponse("Invalid request payload");

				return response;
			}
			logBuilder.append("Request Query: ").append(requestDTO.getCustomQuery());
			// Call Service
			response = customFilterService.getFilteredQueryData(requestDTO);

			apiLogInfo.setApiStatus(response.getServiceStatus());
			apiLogInfo.setApiResponse("Execution completed");

		} catch (Exception e) {
			e.printStackTrace();

			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong while processing request.");
			response.setServiceError(e.getMessage());

			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			apiLogInfo.setApiResponse(e.getMessage());
		} finally {
			apiLogInfo.setApiRequest(logBuilder.toString());
			logService.logMyInfo(httpRequest, apiLogInfo);
		}

		return response;
	}
	
	
}
