package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.ActivityDTO;
import com.apmosys.employeeportal.dto.DesignationDTO;
import com.apmosys.employeeportal.service.DesignationService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class DesignationController {
	
	@Autowired
	DesignationService designationService;

	@RequestMapping(value="/createDesignation" , method = RequestMethod.POST)
	public ServiceResponse createDesignation(@RequestBody DesignationDTO designationDTO) {		
		
		ServiceResponse response =	designationService.createDesignation(designationDTO);		
		return response;
	}
	
	@RequestMapping(value="/getAllDesignation" , method = RequestMethod.GET)
	public ServiceResponse getAllDesignation() {		
		
		ServiceResponse response =	designationService.getAllDesignation();		
		return response;
	}
	
	@RequestMapping(value="/checkDesignationName" , method = RequestMethod.POST)
	public ServiceResponse checkDesignationName(@RequestBody DesignationDTO designationDTO) {		
		
		ServiceResponse response =	designationService.checkDesignationName(designationDTO);		
		return response;
	}
	
	@RequestMapping(value="/getDesignationById" , method = RequestMethod.POST)
	public ServiceResponse getDesignationById(@RequestBody DesignationDTO designationDTO) {		
		
		ServiceResponse response =	designationService.getDesignationById(designationDTO);		
		return response;
	}
	
	@RequestMapping(value="/updateDesignation" , method = RequestMethod.POST)
	public ServiceResponse updateDesignation(@RequestBody DesignationDTO designationDTO) {		
		
		ServiceResponse response =	designationService.updateDesignation(designationDTO);		
		return response;
	}
	
	@RequestMapping(value="/getDesignationByDeptId" , method = RequestMethod.POST)
	public ServiceResponse getDesignationByDeptId(@RequestBody DesignationDTO designationDTO) {		
		
		ServiceResponse response =	designationService.getDesignationByDeptId(designationDTO);
		return response;
	}
	
	@RequestMapping(value="/deleteDesignation" , method = RequestMethod.POST)
	public ServiceResponse deleteDesignation(@RequestBody DesignationDTO designationDTO) {		
		
		ServiceResponse response =	designationService.deleteDesignation(designationDTO);
		return response;
	}
	
	@RequestMapping(value="/changeEmployeeDesignationMapping" , method = RequestMethod.POST)
	public ServiceResponse changeEmployeeDesignationMapping(@RequestBody DesignationDTO designationDTO) {		
		
		ServiceResponse response =	designationService.changeEmployeeDesignationMapping(designationDTO);
		return response;
	}
	
}
