package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.ActivityDTO;
import com.apmosys.employeeportal.service.ActivitiesService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class ActivityController {

	@Autowired
	ActivitiesService activitiesService;
	
	
	@RequestMapping(value="/createActivity" , method = RequestMethod.POST)
	public ServiceResponse createActivity(@RequestBody ActivityDTO activityDTO) {		
		
		ServiceResponse response =	activitiesService.createActivity(activityDTO);		
		return response;
	}
	
	@RequestMapping(value="/updateActivity" , method = RequestMethod.POST)
	public ServiceResponse updateActivity(@RequestBody ActivityDTO activityDTO) {		
		
		ServiceResponse response =	activitiesService.updateActivity(activityDTO);		
		return response;
	}
	
	
	@RequestMapping(value="/getAllActivitiesByProjectIdAndTeamId" , method = RequestMethod.POST)
	public ServiceResponse getAllActivitiesByProjectIdAndTeamId(@RequestBody ActivityDTO activityDTO) {		
		
		ServiceResponse response =	activitiesService.getAllActivitiesByProjectIdAndTeamId(activityDTO);		
		return response;
	}
	
	@RequestMapping(value="/deleteActivity" , method = RequestMethod.POST)
	public ServiceResponse deleteActivity(@RequestBody ActivityDTO activityDTO) {		
		
		ServiceResponse response =	activitiesService.deleteActivity(activityDTO);		
		return response;
	}
}
