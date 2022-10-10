package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.service.DraftEmployeeService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class DraftEmployeeController {
	
	@Autowired
	DraftEmployeeService draftEmployeeService;
	
	@RequestMapping(value="/createDraftEmployee" , method = RequestMethod.POST)
	public ServiceResponse createDraftEmployee(@RequestBody EmployeeDTO employeedto) {		
		
		ServiceResponse response =	draftEmployeeService.createDraftEmployee(employeedto);		
		return response;
	}
	
	@RequestMapping(value="/getDraftEmployeeById" , method = RequestMethod.POST)
	public ServiceResponse getDraftEmployeeById(@RequestBody EmployeeDTO employeedto) {		
		
		ServiceResponse response = draftEmployeeService.getDraftEmployeeById(employeedto);
		return response;
	}
	
	@RequestMapping(value="/getAllDraftEmployees" , method = RequestMethod.POST)
	public ServiceResponse getAllDraftEmployees(@RequestBody EmployeeDTO employeedto) {		
		
		ServiceResponse response =draftEmployeeService.getAllDraftEmployees(employeedto);
		return response;		
	}
	
	@RequestMapping(value="/updateDraftEmployeeById" , method = RequestMethod.POST)
	public ServiceResponse updateDraftEmployeeById(@RequestBody EmployeeDTO employeedto) {		
		
		ServiceResponse response =draftEmployeeService.updateDraftEmployeeById(employeedto);
		return response;		
	}
	
	@RequestMapping(value="/deleteDraftEmployeeById" , method = RequestMethod.POST)
	public ServiceResponse deleteDraftEmployeeById(@RequestBody EmployeeDTO employeedto) {		
		
		ServiceResponse response =draftEmployeeService.deleteDraftEmployeeById(employeedto);
		return response;
	}
	
	@RequestMapping(value="/getDraftEmployeeByEmploymentId" , method = RequestMethod.POST)
	public ServiceResponse getDraftEmployeeByEmploymentId(@RequestBody EmployeeDTO employeedto) {		
		
		ServiceResponse response = draftEmployeeService.getDraftEmployeeByEmploymentId(employeedto);
		return response;
	}
	
	@RequestMapping(value="/rejectDraftEmployeeApplication" , method = RequestMethod.POST)
	public ServiceResponse rejectDraftEmployeeApplication(@RequestBody EmployeeDTO employeedto) {		
		
		ServiceResponse response = draftEmployeeService.rejectDraftEmployeeApplication(employeedto);
		return response;
	}
	
	@RequestMapping(value="/approveDraftEmployeeApplication" , method = RequestMethod.POST)
	public ServiceResponse approveDraftEmployeeApplication(@RequestBody EmployeeDTO employeedto) {		
		
		ServiceResponse response = draftEmployeeService.approveDraftEmployeeApplication(employeedto);
		return response;
	}
	
	@RequestMapping(value="/updateDraftStatusById" , method = RequestMethod.POST)
	public ServiceResponse updateDraftStatusById(@RequestBody EmployeeDTO employeedto) {		
		
		ServiceResponse response =draftEmployeeService.updateDraftStatusById(employeedto);
		return response;
	}		

	@RequestMapping(value="/revokeDraftEmployeeApplication" , method = RequestMethod.POST)
	public ServiceResponse revokeDraftEmployeeApplication(@RequestBody EmployeeDTO employeedto) {		
		
		ServiceResponse response = draftEmployeeService.revokeDraftEmployeeApplication(employeedto);
		return response;
	}

}
