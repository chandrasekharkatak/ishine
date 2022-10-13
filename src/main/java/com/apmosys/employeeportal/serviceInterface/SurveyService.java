package com.apmosys.employeeportal.serviceInterface;

import com.apmosys.employeeportal.dto.SurveyDTO;
import com.apmosys.employeeportal.utility.ServiceResponse;

public interface SurveyService {

	ServiceResponse createSurvey(SurveyDTO surveyDTO);

	ServiceResponse getAllSurveys();

	ServiceResponse getAllQuestionsBySurveyId(SurveyDTO surveyDTO);

	ServiceResponse setSurveyResponseByEmpId(SurveyDTO surveyDTO);

	ServiceResponse getAnsweredSurveysByEmpId(SurveyDTO surveyDTO);

	ServiceResponse changeSurveyStatus(SurveyDTO surveyDTO);

}
