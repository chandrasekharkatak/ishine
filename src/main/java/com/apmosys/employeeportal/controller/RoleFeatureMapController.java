package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.FeatureMasterDTO;
import com.apmosys.employeeportal.dto.JobRoleDTO;
import com.apmosys.employeeportal.dto.SubFeatureMasterDTO;
import com.apmosys.employeeportal.service.RoleFeatureMapService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class RoleFeatureMapController {
	
	@Autowired
	RoleFeatureMapService roleFeatureMapService;
	
	
	@RequestMapping(value="/setDefaultSubFeaturesByRoleId" , method = RequestMethod.POST)
	public ServiceResponse setDefaultSubFeaturesByRoleId(@RequestBody JobRoleDTO jobRoleDTO) {		
		
		ServiceResponse response = roleFeatureMapService.setDefaultSubFeaturesByRoleId(jobRoleDTO);
		return response;
	}
	
	/*
	 * 06/06/2022
	 * By suraj: 
	 * roleFeatureMapService.updateRoleFeatureMapping() was also throwing exception
	 * when transaction was rolled back. Hence try-catch has been added in controller as well.
	 * DO NOT REMOVE THIS TRY-CATCH BLOCK.		
	*/
	@RequestMapping(value="/updateRoleFeatureMapping" , method = RequestMethod.POST)
	public ServiceResponse updateRoleFeatureMapping(@RequestBody FeatureMasterDTO featureMasterDTO) {		
		
		
		ServiceResponse response = new ServiceResponse();
		try
		{
			 response =	roleFeatureMapService.updateRoleFeatureMapping(featureMasterDTO);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		
		return response;
	}

}
