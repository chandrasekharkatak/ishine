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

}
