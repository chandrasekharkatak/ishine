package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.service.LeavePolicyMasterService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class LeavePolicyMasterController {

	
	@Autowired
	LeavePolicyMasterService leavePolicyMasterService;
	
	@RequestMapping(value = "/addLeavePolicy" ,method = RequestMethod.POST)
	public ServiceResponse addLeavePolicy(@RequestBody LeaveDTO leaveDTO) {
		
		ServiceResponse response = leavePolicyMasterService.addLeavePolicy(leaveDTO);
		return response;
	}
}
