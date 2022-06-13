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
	
	@RequestMapping(value = "/getAllMyLeaveApplicationsByEmpId" ,method = RequestMethod.POST)
	public ServiceResponse getAllMyLeaveApplicationsByEmpId(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = employeeLeaveService.getAllMyLeaveApplicationsByEmpId(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/getAllMyTeamsLeaveApplicationsByManagerId" ,method = RequestMethod.POST)
	public ServiceResponse getAllMyTeamsLeaveApplicationsByManagerId(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = employeeLeaveService.getAllMyTeamsLeaveApplicationsByManagerId(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/getMyLeaveBalancesByEmpId" ,method = RequestMethod.POST)
	public ServiceResponse getMyLeaveBalancesByEmpId(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = employeeLeaveService.getMyLeaveBalancesByEmpId(leaveDTO);
		return response;
	}
	
	/*
	 * To update leave bucket of an employee eg: Add Compoff , monthly leave updation ,etc.
	 */
	@RequestMapping(value = "/updateLeavesByEmpId" ,method = RequestMethod.POST)
	public ServiceResponse updateLeavesByEmpId(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = employeeLeaveService.updateLeavesByEmpId(leaveDTO);
		return response;
	}
	
	
	

}
