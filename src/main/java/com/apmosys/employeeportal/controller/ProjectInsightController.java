package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.ProjectInsightDTO;
import com.apmosys.employeeportal.dto.SurveyDTO;
import com.apmosys.employeeportal.service.ProjectInsightService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api")
public class ProjectInsightController {
	
	@Autowired
	ProjectInsightService projectInsightService;
	
	@RequestMapping(value = "/createProjectInsightQuestion", method = RequestMethod.POST)
	public ServiceResponse createProjectInsightQuestion(@RequestBody ProjectInsightDTO projectInsightDTO) {

		ServiceResponse response = projectInsightService.createProjectInsightQuestion(projectInsightDTO);
		return response;
	}
	
	@RequestMapping(value = "/getAllProjectInsightList", method = RequestMethod.GET)
	public ServiceResponse getAllSurveys() {

		ServiceResponse response = projectInsightService.getAllProjectInsightList();
		return response;
	}
	
	@RequestMapping(value = "/getAllQuestionsByProjectId", method = RequestMethod.POST)
	public ServiceResponse getAllQuestionsBySurveyId(@RequestBody ProjectInsightDTO projectInsightDTO) {

		ServiceResponse response = projectInsightService.getAllQuestionsByProjectId(projectInsightDTO);
		return response;
	}
	
	@RequestMapping(value = "/updateProjectInsightQuestion", method = RequestMethod.POST)
	public ServiceResponse updateProjectInsightQuestion(@RequestBody ProjectInsightDTO projectInsightDTO) {

		ServiceResponse response = projectInsightService.updateProjectInsightQuestion(projectInsightDTO);
		return response;
	}
	
	@RequestMapping(value = "/getAllProjectInsightResponsesByProjectId", method = RequestMethod.POST)
	public ServiceResponse getAllProjectInsightResponsesByProjectId(@RequestBody ProjectInsightDTO projectInsightDTO) {
		ServiceResponse response = projectInsightService.getAllProjectInsightResponsesByProjectId(projectInsightDTO);
		return response;
	}
	
	@RequestMapping(value = "/saveProjectInsightResponse", method = RequestMethod.POST)
	public ServiceResponse saveProjectInsightResponse(@RequestBody ProjectInsightDTO projectInsightDTO) {
		ServiceResponse response = projectInsightService.saveProjectInsightResponse(projectInsightDTO);
		return response;
	}
}
