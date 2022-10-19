package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.SurveyDTO;
import com.apmosys.employeeportal.dto.SurveyQuestionDTO;
import com.apmosys.employeeportal.model.Holiday;
import com.apmosys.employeeportal.model.Survey;
import com.apmosys.employeeportal.model.SurveyEmployeeResponse;
import com.apmosys.employeeportal.model.SurveyQuestion;
import com.apmosys.employeeportal.repository.SurveyEmployeeResponseRepository;
import com.apmosys.employeeportal.repository.SurveyQuestionRepository;
import com.apmosys.employeeportal.repository.SurveyRepository;
import com.apmosys.employeeportal.serviceInterface.SurveyService;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class SurveyServiceImpl implements SurveyService {

	@Autowired
	SurveyRepository surveyRepository;

	@Autowired
	SurveyQuestionRepository surveyQuestionRepository;

	@Autowired
	ValidationService validationService;

	@Autowired
	SurveyEmployeeResponseRepository surveyEmployeeResponseRepository;

	@Autowired
	StringToDateTimeParser stringToDateTimeParser;

	@Override
	@Transactional
	public ServiceResponse createSurvey(SurveyDTO surveyDTO) {
		ServiceResponse response = new ServiceResponse();
		try {

			if (!validationService.validateEmpId(surveyDTO.getCreatedBy())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Id does not exists.");
				return response;
			}

			Survey newSurvey = new Survey();

			newSurvey.setCreatedBy(surveyDTO.getCreatedBy());
			newSurvey.setSurveyName(surveyDTO.getSurveyName());
			newSurvey.setDescription(surveyDTO.getDescription());
			newSurvey.setIsActive(surveyDTO.getIsActive());

			Survey newSurveyCreated = surveyRepository.save(newSurvey);

			if (newSurveyCreated.getSurveyId() != null) {
				List<SurveyQuestion> list = new ArrayList<>();
				Long surveyId = newSurveyCreated.getSurveyId();

				surveyDTO.getSurveyQuestionList().forEach((question) -> {
					SurveyQuestion newSurveyQuestion = new SurveyQuestion();

					newSurveyQuestion.setSurveyId(surveyId);
					newSurveyQuestion.setQuestion(question.getQuestion());
					newSurveyQuestion.setOptionType(question.getOptionType());
					newSurveyQuestion.setOptions(question.getOptions());
					newSurveyQuestion.setRequired(question.getRequired());
					newSurveyQuestion.setDescription(question.getDescription());

					list.add(newSurveyQuestion);
				});

				List<SurveyQuestion> listSaved = surveyQuestionRepository.saveAll(list);

				if (listSaved.size() > 0) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Survey created successfully.");
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Survey created but no questions were added to survey.");
				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Failed to create survey.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	@Override
	public ServiceResponse getAllSurveys() {

		ServiceResponse response = new ServiceResponse();
		try {

			List<Object[]> objectList = surveyRepository.getAllSurveys();

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No survey found. List is empty.");
				} else {
					List<SurveyDTO> dtoList = new ArrayList<SurveyDTO>();

					list.forEach((object) -> {

						SurveyDTO dto = new SurveyDTO();

						dto.setSurveyId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						dto.setSurveyName(object[1] != null ? object[1].toString() : null);
						dto.setDescription(object[2] != null ? object[2].toString() : null);
						dto.setIsActive(object[3] != null ? object[3].toString() : null);
						dto.setCreatedByName(object[4] != null ? object[4].toString() : null);
						dto.setCreatedOn(object[5] != null ? object[5].toString() : null);
						dto.setUpdatedByName(object[6] != null ? object[6].toString() : null);
						dto.setUpdatedOn(object[7] != null ? object[7].toString() : null);
						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No survey found. List is null.");
			});

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	@Override
	public ServiceResponse getAllQuestionsBySurveyId(SurveyDTO surveyDTO) {
		ServiceResponse response = new ServiceResponse();
		try {

			if (!validationService.validateSurveyId(surveyDTO.getSurveyId())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Survey Id does not exists.");
				return response;
			}

			List<SurveyQuestion> questionList = surveyQuestionRepository.findAllBySurveyId(surveyDTO.getSurveyId());

			if (questionList != null) {
				if (questionList.size() == 0) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No survey questions found. List is empty.");
				} else {
					List<SurveyQuestionDTO> dtoList = new ArrayList<SurveyQuestionDTO>();

					questionList.forEach((object) -> {

						SurveyQuestionDTO dto = new SurveyQuestionDTO();

						dto.setSurveyQuestionId(object.getSurveyQuestionId());
						dto.setSurveyId(object.getSurveyId());
						dto.setQuestion(object.getQuestion());
						dto.setOptionType(object.getOptionType());
						dto.setOptions(object.getOptions());
						dto.setRequired(object.getRequired());
						dto.setDescription(object.getDescription());
						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No survey questions found. List is null.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	@Override
	public ServiceResponse setSurveyResponseByEmpId(SurveyDTO surveyDTO) {
		ServiceResponse response = new ServiceResponse();
		try {

			if (!validationService.validateEmpId(surveyDTO.getEmpId())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Id does not exists.");
				return response;
			}

			List<SurveyEmployeeResponse> dtoList = new ArrayList<SurveyEmployeeResponse>();

			surveyDTO.getSurveyQuestionList().forEach((question) -> {

				SurveyEmployeeResponse surveyEmployeeResponse = new SurveyEmployeeResponse();

				surveyEmployeeResponse.setEmpId(surveyDTO.getEmpId());
				surveyEmployeeResponse.setSurveyQuestionId(question.getSurveyQuestionId());
				surveyEmployeeResponse.setResponse(question.getResponse());
				dtoList.add(surveyEmployeeResponse);
			});

			List<SurveyEmployeeResponse> responseList = surveyEmployeeResponseRepository.saveAll(dtoList);

			if (responseList.size() > 0) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Responses stored successfully.");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No responses were stored.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	@Override
	public ServiceResponse getAnsweredSurveysByEmpId(SurveyDTO surveyDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> objectList = surveyQuestionRepository.getAnsweredSurveysByEmpId(surveyDTO.getEmpId());

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No survey found. List is empty.");
				} else {
					List<SurveyDTO> dtoList = new ArrayList<SurveyDTO>();

					list.forEach((object) -> {

						SurveyDTO dto = new SurveyDTO();
						dto.setSurveyId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No survey found. List is null.");
			});

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	@Override
	public ServiceResponse changeSurveyStatus(SurveyDTO surveyDTO) {
		ServiceResponse response = new ServiceResponse();
		try {

			Optional<Survey> surveyObject = surveyRepository.findById(surveyDTO.getSurveyId());
			if (surveyObject.isPresent()) {
				Survey surveyToBeDeleted = surveyObject.get();

				surveyToBeDeleted.setIsActive(surveyDTO.getIsActive());
				surveyToBeDeleted.setUpdatedBy(surveyDTO.getUpdatedBy());
				surveyToBeDeleted.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());

				Survey surveyupdated = surveyRepository.save(surveyToBeDeleted);

				if (surveyupdated.getSurveyId() != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Survey status changed.");

				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Failed to change status.");
				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Survey Not Found.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	@Override
	public ServiceResponse getSurveyResponseByEmpIdAndSurveyId(SurveyDTO surveyDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			if (!validationService.validateEmpId(surveyDTO.getEmpId())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Id does not exists.");
				return response;
			}
			if (!validationService.validateSurveyId(surveyDTO.getSurveyId())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Survey Id does not exists.");
				return response;
			}

			List<Object[]> objectList = surveyEmployeeResponseRepository
					.getSurveyResponseByEmpIdAndSurveyId(surveyDTO.getEmpId(), surveyDTO.getSurveyId());

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No responses found for survey. List is empty.");
				} else {
					List<SurveyQuestionDTO> dtoList = new ArrayList<SurveyQuestionDTO>();

					list.forEach((object) -> {

						SurveyQuestionDTO dto = new SurveyQuestionDTO();
						dto.setQuestion(object[0] != null ? object[0].toString() : null);
						dto.setOptions(object[1] != null ? object[1].toString() : null);
						dto.setResponse(object[2] != null ? object[2].toString() : null);
						dtoList.add(dto);

					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No responses found for survey. List is null.");
			});

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	@Override
	public ServiceResponse getSurveyAllResponsesBySurveyId(SurveyDTO surveyDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			if (!validationService.validateSurveyId(surveyDTO.getSurveyId())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Survey Id does not exists.");
				return response;
			}
			
			List<Object[]> objectList =	surveyEmployeeResponseRepository.getSurveyAllResponsesBySurveyId(surveyDTO.getSurveyId());
			
			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No responses found for survey. List is empty.");
				} else {
					List<SurveyQuestionDTO> dtoList = new ArrayList<SurveyQuestionDTO>();

					list.forEach((object) -> {

						SurveyQuestionDTO dto = new SurveyQuestionDTO();
						dto.setEmployeementId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						dto.setName(object[1] != null ? object[1].toString() : null);
						dto.setCreatedOn(object[2] != null ? object[2].toString() : null);
						dto.setSurveyQuestionId(object[3] != null ? Long.parseLong(object[3].toString()) : null);
						dto.setQuestion(object[4] != null ? object[4].toString() : null);
						dto.setOptions(object[5] != null ? object[5].toString() : null);
						dto.setResponse(object[6] != null ? object[6].toString() : null);
						dtoList.add(dto);

					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No responses found for survey. List is null.");
			});
			
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
}
