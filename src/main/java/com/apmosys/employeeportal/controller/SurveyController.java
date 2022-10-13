package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.SurveyDTO;
import com.apmosys.employeeportal.serviceInterface.SurveyService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api")
public class SurveyController {

	@Autowired
	SurveyService surveyService;

	@RequestMapping(value = "/createSurvey", method = RequestMethod.POST)
	public ServiceResponse createSurvey(@RequestBody SurveyDTO surveyDTO) {

		ServiceResponse response = surveyService.createSurvey(surveyDTO);
		return response;
	}

	@RequestMapping(value = "/getAllSurveys", method = RequestMethod.GET)
	public ServiceResponse getAllSurveys() {

		ServiceResponse response = surveyService.getAllSurveys();
		return response;
	}

	@RequestMapping(value = "/getAllQuestionsBySurveyId", method = RequestMethod.POST)
	public ServiceResponse getAllQuestionsBySurveyId(@RequestBody SurveyDTO surveyDTO) {

		ServiceResponse response = surveyService.getAllQuestionsBySurveyId(surveyDTO);
		return response;
	}

	@RequestMapping(value = "/setSurveyResponseByEmpId", method = RequestMethod.POST)
	public ServiceResponse setSurveyResponseByEmpId(@RequestBody SurveyDTO surveyDTO) {

		ServiceResponse response = surveyService.setSurveyResponseByEmpId(surveyDTO);
		return response;
	}

	@RequestMapping(value = "/getAnsweredSurveysByEmpId", method = RequestMethod.POST)
	public ServiceResponse getAnsweredSurveysByEmpId(@RequestBody SurveyDTO surveyDTO) {

		ServiceResponse response = surveyService.getAnsweredSurveysByEmpId(surveyDTO);
		return response;
	}
	
	@RequestMapping(value = "/changeSurveyStatus", method = RequestMethod.POST)
	public ServiceResponse changeSurveyStatus(@RequestBody SurveyDTO surveyDTO) {

		ServiceResponse response = surveyService.changeSurveyStatus(surveyDTO);
		return response;
	}

}
