package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.JobRoleAccess;
import com.apmosys.employeeportal.dto.ActivityTemplateDTO;
import com.apmosys.employeeportal.service.ActivityTemplateService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api")
public class ActivityTemplateController {
	
	@Autowired
	ActivityTemplateService activityTemplateService;
	
	@JobRoleAccess(featureIds = {7})
	@RequestMapping(value = "/createActivityTemplate", method = RequestMethod.POST)
	public ServiceResponse createActivityTemplate(@RequestBody ActivityTemplateDTO activityTemplateDTO) {
		
		ServiceResponse response = activityTemplateService.createActivityTemplate(activityTemplateDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {7})
	@RequestMapping(value = "/getActivityTemplate", method = RequestMethod.POST)
	public ServiceResponse getActivityTemplate(@RequestBody ActivityTemplateDTO activityTemplateDTO) {
		
		ServiceResponse response = activityTemplateService.getActivityTemplate(activityTemplateDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {7})
	@RequestMapping(value = "/getActivityTemplateById", method = RequestMethod.POST)
	public ServiceResponse getActivityTemplateById(@RequestBody ActivityTemplateDTO activityTemplateDTO) {
		
		ServiceResponse response = activityTemplateService.getActivityTemplateById(activityTemplateDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {7})
	@RequestMapping(value = "/updateActivityTemplate", method = RequestMethod.POST)
	public ServiceResponse updateActivityTemplate(@RequestBody ActivityTemplateDTO activityTemplateDTO) {
		
		ServiceResponse response = activityTemplateService.updateActivityTemplate(activityTemplateDTO);
		return response;
	}
	
}
