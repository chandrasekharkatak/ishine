package com.apmosys.employeeportal.serviceInterface;

import com.apmosys.employeeportal.dto.SurveyDTO;
import com.apmosys.employeeportal.utility.ServiceResponse;

public interface SurveyService {

	ServiceResponse createSurvey(SurveyDTO surveyDTO);

	ServiceResponse getAllSurveys(Integer trainingId);

	ServiceResponse getAllQuestionsBySurveyId(SurveyDTO surveyDTO);

	ServiceResponse setSurveyResponseByEmpId(SurveyDTO surveyDTO);

	ServiceResponse getAnsweredSurveysByEmpId(SurveyDTO surveyDTO);

	ServiceResponse changeSurveyStatus(SurveyDTO surveyDTO);

	ServiceResponse getSurveyResponseByEmpIdAndSurveyId(SurveyDTO surveyDTO, Boolean isQuizResponse, Boolean isTrainingResponse);

	ServiceResponse getSurveyAllResponsesBySurveyId(SurveyDTO surveyDTO);

	ServiceResponse deleteSurvey(SurveyDTO surveyDTO);

	ServiceResponse updateSurvey(SurveyDTO surveyDTO);

}
