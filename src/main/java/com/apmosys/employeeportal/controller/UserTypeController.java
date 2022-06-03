package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;



import com.apmosys.employeeportal.dto.UserTypeDTO;
import com.apmosys.employeeportal.service.EmployeeService;
import com.apmosys.employeeportal.service.UserTypeService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class UserTypeController {
	
	@Autowired
	UserTypeService userTypeService;
	
//	@RequestMapping(value = "/createUserType" ,method = RequestMethod.POST)
//	public ServiceResponse createUserType(@RequestBody UserTypeDTO userTypeDTO) {
//		
//		ServiceResponse response = userTypeService.createUserType(userTypeDTO);
//		return response;
//	}
//	
//	@RequestMapping(value="/getAllUserTypes" , method = RequestMethod.GET)
//	public ServiceResponse getAllUserTypes() {		
//		
//		ServiceResponse response =	userTypeService.getAllUserTypes();		
//		return response;
//	}	
	
	

}
