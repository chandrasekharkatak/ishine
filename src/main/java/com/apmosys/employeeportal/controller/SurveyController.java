package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.JobRoleAccess;
import com.apmosys.employeeportal.dto.SurveyDTO;
import com.apmosys.employeeportal.serviceInterface.SurveyService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api")
public class SurveyController {

	@Autowired
	SurveyService surveyService;

	@JobRoleAccess(featureIds = {31})
	@RequestMapping(value = "/createSurvey", method = RequestMethod.POST)
	public ServiceResponse createSurvey(@RequestBody SurveyDTO surveyDTO) {

		ServiceResponse response = surveyService.createSurvey(surveyDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {30,31,37})
	@RequestMapping(value = "/getAllSurveys", method = RequestMethod.GET)
	public ServiceResponse getAllSurveys(
	        @RequestParam(required = false) Integer trainingId) {

	    return surveyService.getAllSurveys(trainingId);
	}

	@JobRoleAccess(featureIds = {30,31,37})
	@RequestMapping(value = "/getAllQuestionsBySurveyId", method = RequestMethod.POST)
	public ServiceResponse getAllQuestionsBySurveyId(@RequestBody SurveyDTO surveyDTO, @RequestParam(required = false) Boolean isEditing) {

		ServiceResponse response = surveyService.getAllQuestionsBySurveyId(surveyDTO, isEditing);
		return response;
	}
	@JobRoleAccess(featureIds = {30})
	@RequestMapping(value = "/setSurveyResponseByEmpId", method = RequestMethod.POST)
	public ServiceResponse setSurveyResponseByEmpId(@RequestBody SurveyDTO surveyDTO) {

		ServiceResponse response = surveyService.setSurveyResponseByEmpId(surveyDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {30})
	@RequestMapping(value = "/getAnsweredSurveysByEmpId", method = RequestMethod.POST)
	public ServiceResponse getAnsweredSurveysByEmpId(@RequestBody SurveyDTO surveyDTO) {

		ServiceResponse response = surveyService.getAnsweredSurveysByEmpId(surveyDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {31})
	@RequestMapping(value = "/changeSurveyStatus", method = RequestMethod.POST)
	public ServiceResponse changeSurveyStatus(@RequestBody SurveyDTO surveyDTO) {

		ServiceResponse response = surveyService.changeSurveyStatus(surveyDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {30})
	@RequestMapping(value = "/getSurveyResponseByEmpIdAndSurveyId", method = RequestMethod.POST)
	public ServiceResponse getSurveyResponseByEmpIdAndSurveyId(@RequestBody SurveyDTO surveyDTO) {

		ServiceResponse response = surveyService.getSurveyResponseByEmpIdAndSurveyId(surveyDTO, surveyDTO.getIsQuizResponse(), surveyDTO.getIsAttendingQuiz());
		return response;
	}
	@JobRoleAccess(featureIds = {31})
	@RequestMapping(value = "/getSurveyAllResponsesBySurveyId", method = RequestMethod.POST)
	public ServiceResponse getSurveyAllResponsesBySurveyId(@RequestBody SurveyDTO surveyDTO) {

		ServiceResponse response = surveyService.getSurveyAllResponsesBySurveyId(surveyDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {31})
	@RequestMapping(value = "/deleteSurvey", method = RequestMethod.POST)
	public ServiceResponse deleteSurvey(@RequestBody SurveyDTO surveyDTO) {

		ServiceResponse response = surveyService.deleteSurvey(surveyDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {31})
	@RequestMapping(value = "/updateSurvey", method = RequestMethod.POST)
	public ServiceResponse updateSurvey(@RequestBody SurveyDTO surveyDTO) {

		ServiceResponse response = surveyService.updateSurvey(surveyDTO);
		return response;
	}

}
