package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.service.LeaveTypeMasterService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class LeaveTypeMasterController {
	
	@Autowired
	LeaveTypeMasterService leaveTypeMasterService;
	
	@RequestMapping(value = "/getAllLeaveTypes" ,method = RequestMethod.GET)
	public ServiceResponse getAllLeaveTypes() {
		
		ServiceResponse response = leaveTypeMasterService.getAllLeaveTypes();
		return response;
	}
	
	@RequestMapping(value = "/updateLeaveType" ,method = RequestMethod.POST)
	public ServiceResponse updateLeaveType(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = leaveTypeMasterService.updateLeaveType(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/createLeaveType" ,method = RequestMethod.POST)
	public ServiceResponse createLeaveType(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = leaveTypeMasterService.createLeaveType(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/getAllLeaveTypesByLeavePolicies" ,method = RequestMethod.POST)
	public ServiceResponse getLeavePolicyByEmployentStatusAndLeaveTypeMasterId(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = leaveTypeMasterService.getAllLeaveTypesByLeavePolicies(leaveDTO);
		return response;
	}

	@RequestMapping(value = "/deleteLeaveType" ,method = RequestMethod.POST)
	public ServiceResponse deleteLeaveType(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = leaveTypeMasterService.deleteLeaveType(leaveDTO);
		return response;
	}
	
	@RequestMapping(value = "/changeLeaveTypeMapping" ,method = RequestMethod.POST)
	public ServiceResponse changeLeaveTypeMapping(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = leaveTypeMasterService.changeLeaveTypeMapping(leaveDTO);
		return response;
	}
	
}
