package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.UserTypeCrudMappingDTO;
import com.apmosys.employeeportal.dto.UserTypeDTO;
import com.apmosys.employeeportal.service.UserTypeCrudMappingService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class UserTypeCrudMappingController {
	
//	@Autowired
//	UserTypeCrudMappingService userTypeCrudMappingService;
//	
//	@RequestMapping(value="/getAllUserTypeCrudMappings" , method = RequestMethod.GET)
//	public ServiceResponse getAllUserTypeCrudMappings() {		
//		
//		ServiceResponse response =	userTypeCrudMappingService.getAllUserTypeCrudMappings();		
//		return response;
//	}
//	
//	@RequestMapping(value="/getUserTypeCrudMappingsByUserTypeId" , method = RequestMethod.POST)
//	public ServiceResponse getUserTypeCrudMappingsByUserTypeId(@RequestBody UserTypeDTO userTypeDTO) {		
//		
//		ServiceResponse response =	userTypeCrudMappingService.getUserTypeCrudMappingsByUserTypeId(userTypeDTO);		
//		return response;
//	}
//	
//	@RequestMapping(value="/updateCrudMappingsByMapId" , method = RequestMethod.POST)
//	public ServiceResponse updateCrudMappingsByMapId(@RequestBody UserTypeCrudMappingDTO userTypeCrudMappingDTO) {		
//		
//		ServiceResponse response =	userTypeCrudMappingService.updateCrudMappingsByMapId(userTypeCrudMappingDTO);		
//		return response;
//	}
	
	

}
