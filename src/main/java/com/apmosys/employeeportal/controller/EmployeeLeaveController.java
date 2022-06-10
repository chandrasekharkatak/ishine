package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.service.EmployeeLeaveService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class EmployeeLeaveController {
	
	@Autowired
	EmployeeLeaveService employeeLeaveService;
	
	@RequestMapping(value = "/getAllLeaveTypes" ,method = RequestMethod.GET)
	public ServiceResponse getAllLeaveTypes() {
		
		ServiceResponse response = employeeLeaveService.getAllLeaveTypes();
		return response;
	}
	
	@RequestMapping(value = "/updateLeaveType" ,method = RequestMethod.POST)
	public ServiceResponse updateLeaveType(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = employeeLeaveService.updateLeaveType(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/applyLeave" ,method = RequestMethod.POST)
	public ServiceResponse applyLeave(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = employeeLeaveService.applyLeave(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/updateLeaveStatus" ,method = RequestMethod.POST)
	public ServiceResponse updateLeaveStatus(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = employeeLeaveService.updateLeaveStatus(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/getAllMyLeavesByEmpId" ,method = RequestMethod.POST)
	public ServiceResponse getAllMyLeavesByEmpId(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = employeeLeaveService.getAllMyLeavesByEmpId(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/getAllMyTeamsLeavesByManagerId" ,method = RequestMethod.POST)
	public ServiceResponse getAllMyTeamsLeavesByManagerId(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = employeeLeaveService.getAllMyTeamsLeavesByManagerId(leaveDTO);
		return response;
	}
	
	

}
