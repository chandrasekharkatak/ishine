package com.apmosys.employeeportal.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.BioMaTO;
import com.apmosys.employeeportal.dto.BioMax360;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.repository.TimesheetActivityMapRepository;
import com.apmosys.employeeportal.service.BioMaxService;
import com.apmosys.employeeportal.service.DraftEmployeeService;
import com.apmosys.employeeportal.service.Employee360Service;
import com.apmosys.employeeportal.service.TimesheetService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class Employee360Controller {
	
	@Autowired
	Employee360Service employee360Service;
	@Autowired
	private BioMaxService bioMaxService;
	@Autowired
	private TimesheetService timesheetService;
	
	@RequestMapping(value = "/getLeaveDataPerMonthByEmpId", method = RequestMethod.GET)
	public ServiceResponse getLeaveDataPerMonthByEmpId(@RequestParam Long empId) {

		ServiceResponse response = employee360Service.getLeaveDataPerMonthByEmpId(empId);
		return response;
	}
	
	@RequestMapping(value = "/getEmployeeDetails", method = RequestMethod.GET)
	public ServiceResponse getEmployeeDetails(@RequestParam Long empId) {

		ServiceResponse response = employee360Service.getEmployeeDetails(empId);
		return response;
	}
	
	@RequestMapping(value = "/get360TimesheetDetails", method = RequestMethod.GET)
	public ServiceResponse get360TimesheetDetails(@RequestParam String status, @RequestParam long empId,
			@RequestParam long projectId,@RequestParam String teamName,@RequestParam(required = false) long managerId,
	        @RequestParam(required = false) String startDate,
	        @RequestParam(required = false) String endDate 
			) {
//			startDate = null;

		 if (startDate == null || startDate.isEmpty()) {
		        System.out.println("startDate is null or undefined");
		    } else {
		        System.out.println("Start Date: " + startDate);
		    }

		    if (endDate == null || endDate.isEmpty()) {
		        System.out.println("endDate is null or undefined");
		    } else {
		        System.out.println("End Date: " + endDate);
		    }
		    
		ServiceResponse response = employee360Service.get360TimesheetDetails(status,empId,projectId,teamName,managerId,startDate,endDate);
		return response;
	}
	
	@RequestMapping(value = "/updateStatus", method = RequestMethod.GET)
	public ServiceResponse updateStatus(@RequestParam String status,@RequestParam List<Long> timesheetIds,@RequestParam Long updatedBy) {

		ServiceResponse response = employee360Service.updateStatus(status,timesheetIds,updatedBy);
		return response;
	}
	
	@PostMapping(value = "/employee360state")
	public ServiceResponse getBioOverTimeandState(@RequestBody EmployeeDTO employeedto ) {
		ServiceResponse response = bioMaxService.getBioOverTimeandState(employeedto);
		return response;
	}
	@RequestMapping(value="/biomax",method=RequestMethod.GET)
public ServiceResponse getBioMax(@RequestParam String startDate,@RequestParam String endDate, @RequestParam String employeeId) {
	return bioMaxService.getEmpBioData360(startDate, endDate, employeeId);
}

	
}
