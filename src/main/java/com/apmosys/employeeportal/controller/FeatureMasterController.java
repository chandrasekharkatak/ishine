package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.FeatureMasterDTO;
import com.apmosys.employeeportal.service.FeatureMasterService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class FeatureMasterController {
	
	@Autowired
	FeatureMasterService featureMasterService;
	
//	@RequestMapping(value="/createFeature" , method = RequestMethod.POST)
//	public ServiceResponse createFeature(@RequestBody FeatureMasterDTO featureMasterDTO) {		
//		
//		ServiceResponse response =	featureMasterService.createFeature(featureMasterDTO);		
//		return response;
//	}
	
//	@RequestMapping(value="/getAllFeature" , method = RequestMethod.GET)
//	public ServiceResponse getAllFeature() {		
//		
//		ServiceResponse response =	featureMasterService.getAllFeature();		
//		return response;
//	}

}
