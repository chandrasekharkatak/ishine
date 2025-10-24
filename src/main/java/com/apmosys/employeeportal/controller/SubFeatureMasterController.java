package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.JobRoleAccess;
import com.apmosys.employeeportal.dto.JobRoleDTO;
import com.apmosys.employeeportal.service.SubFeatureMasterService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class SubFeatureMasterController {
	
	@Autowired
	SubFeatureMasterService subFeatureMasterService;
	
	@JobRoleAccess(featureIds = {5})
	@RequestMapping(value = "/getSubfeaturesByJobRoleId" ,method = RequestMethod.POST)
	public ServiceResponse getSubfeaturesByJobRoleId(@RequestBody JobRoleDTO jobRoleDTO) {
		
		ServiceResponse response = subFeatureMasterService.getSubfeaturesByJobRoleId(jobRoleDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {5})
	@RequestMapping(value="/getAllSubFeatures" , method = RequestMethod.GET)
	public ServiceResponse getAllSubFeatures() {		
		
		ServiceResponse response =	subFeatureMasterService.getAllSubFeatures();		
		return response;
	}

}
