package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.ResourceManagementDTO;
import com.apmosys.employeeportal.service.ResourceManagementService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api")
public class ResourceManagementController {
	
	@Autowired
	ResourceManagementService resourceManagementService;

	@RequestMapping(value = "/createDraftProjectInfo", method = RequestMethod.POST)
	public ServiceResponse createDraftProjectInfo(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		
		ServiceResponse response = resourceManagementService.createDraftProjectInfo(resourceManagementDTO);
		return response;
	}
	
	@RequestMapping(value = "/getTeamListByProjectName", method = RequestMethod.POST)
	public ServiceResponse getTeamListByProjectName(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		
		ServiceResponse response = resourceManagementService.getTeamListByProjectName(resourceManagementDTO);
		return response;
	}
	
	@RequestMapping(value = "/alreadyCreatedTeam", method = RequestMethod.GET)
	public ServiceResponse alreadyCreatedTeam() {
		
		ServiceResponse response = resourceManagementService.alreadyCreatedTeam();
		return response;
	}
	
}
