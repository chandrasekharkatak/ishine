package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.SurveyDTO;
import com.apmosys.employeeportal.dto.SurveyQuestionDTO;
import com.apmosys.employeeportal.model.Survey;
import com.apmosys.employeeportal.model.SurveyEmployeeResponse;
import com.apmosys.employeeportal.model.SurveyQuestion;
import com.apmosys.employeeportal.model.TrainingContent;
import com.apmosys.employeeportal.model.TrainingMaster;
import com.apmosys.employeeportal.model.TrainingQuizMapping;
import com.apmosys.employeeportal.repository.SurveyEmployeeResponseRepository;
import com.apmosys.employeeportal.repository.SurveyQuestionRepository;
import com.apmosys.employeeportal.repository.SurveyRepository;
import com.apmosys.employeeportal.repository.TrainingContentRepository;
import com.apmosys.employeeportal.repository.TrainingMasterRepository;
import com.apmosys.employeeportal.repository.TrainingQuizMappingRepository;
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
	
	@Autowired
	private HttpServletRequest httpRequest;

	@Autowired
	private LogService logService;
	
	@Autowired
	private TrainingQuizMappingRepository trainingQuizMappingRepository;
	
	@Autowired
	private TrainingMasterRepository trainingMasterRepository;
	
	@Autowired
	private TrainingContentRepository trainingContentRepository;

	@Override
	@Transactional
	public ServiceResponse createSurvey(SurveyDTO surveyDTO) {
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("CreateSurvey");
        apiLogInfo.setApiUrl("/api/createSurvey");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("CreatedBy : " + surveyDTO.getCreatedBy() + " ,SurveyName :"+ surveyDTO.getSurveyName() 
         + " ,EmpId :" + surveyDTO.getEmpId());

		
		try {

			if (!validationService.validateEmpId(surveyDTO.getCreatedBy())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Id does not exists.");
				apiLogInfo.setApiResponse("Employee Id does not exists");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				return response;
			}

			Survey newSurvey = new Survey();

			newSurvey.setCreatedBy(surveyDTO.getCreatedBy());
			newSurvey.setSurveyName(surveyDTO.getSurveyName());
			newSurvey.setDescription(surveyDTO.getDescription());
			newSurvey.setIsActive(surveyDTO.getIsActive());
			
			// Set type to "quiz" if created from training context
			if (surveyDTO.getTrainingId() != null) {
				newSurvey.setType("quiz");
			}
//			newSurvey.setImageUrl(surveyDTO.getImageUrl()); 
//	        newSurvey.setVideoUrl(surveyDTO.getVideoUrl());
			
//			 if (surveyDTO.getImageFile() != null) {
//		            ServiceResponse imageResponse = fileUploadService.storeFile(surveyDTO.getImageFile());
//		            if (imageResponse.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS)) {
//		                newSurvey.setImageUrl(imageResponse.getServiceResponse());
//		            } else {
//		            }
//		        }
//		        
//		        if (surveyDTO.getVideoFile() != null) {
//		            ServiceResponse videoResponse = fileUploadService.storeFile(surveyDTO.getVideoFile());
//		            if (videoResponse.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS)) {
//		                newSurvey.setVideoUrl(videoResponse.getServiceResponse());
//		            } else {
//		                
//		            }
//		        }

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
					// Create training-quiz mapping if quiz is created from training context
					if (surveyDTO.getTrainingId() != null && newSurveyCreated.getSurveyId() != null) {
						try {
							TrainingMaster trainingMaster = trainingMasterRepository.findByTrainingId(surveyDTO.getTrainingId())
									.orElseThrow(() -> new RuntimeException("Training not found with ID: " + surveyDTO.getTrainingId()));
							
							TrainingQuizMapping mapping = new TrainingQuizMapping();
							mapping.setTrainingMaster(trainingMaster);
							mapping.setSurvey(newSurveyCreated);
							mapping.setIsMandatory(surveyDTO.getIsMandatory() != null ? surveyDTO.getIsMandatory() : false);
							mapping.setMustPassToComplete(surveyDTO.getMustPassToComplete() != null ? surveyDTO.getMustPassToComplete() : false);
							mapping.setActiveStatus("true");
							mapping.setCreatedBy(surveyDTO.getCreatedBy());
							
							// Set content if provided
							if (surveyDTO.getContentId() != null) {
								TrainingContent trainingContent = trainingContentRepository.findByContentId(surveyDTO.getContentId())
										.orElse(null);
								if (trainingContent != null) {
									mapping.setTrainingContent(trainingContent);
								}
							}
							
							trainingQuizMappingRepository.save(mapping);
							logBuilder.append(" , Training-Quiz Mapping created for TrainingId: " + surveyDTO.getTrainingId());
						} catch (Exception mappingException) {
							// Log error but don't fail the survey creation
							System.err.println("Error creating training-quiz mapping: " + mappingException.getMessage());
							mappingException.printStackTrace();
							logBuilder.append(" , Warning: Training-Quiz Mapping creation failed: " + mappingException.getMessage());
						}
					}
					
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Survey created successfully.");
					apiLogInfo.setApiResponse("Survey Created Successfully");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Survey created but no questions were added to survey.");
					apiLogInfo.setApiResponse("Survey created but no questions were added to survey");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Failed to create survey.");
				apiLogInfo.setApiResponse("Failed to create survey");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Override
	public ServiceResponse getAllSurveys(Integer trainingId) {

		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("");
		apiLogInfo.setApiUrl("/api/getAllSurveys");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();

		try {

			List<Object[]> objectList;

			if (trainingId != null) {
			    objectList = surveyRepository.getSurveysByTrainingId(trainingId);
			    logBuilder.append("Survey fetched in TRAINING context. TrainingId = " + trainingId);
			} else {
			    objectList = surveyRepository.getAllSurveys();
			    logBuilder.append("Survey fetched in GLOBAL context");
			}

			logBuilder.append("AllSurveyList size : " + objectList.size());

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
						dto.setType(object[8] != null ? object[8].toString() : null);
						dto.setCreatedBy(object[9] != null ? Long.parseLong(object[9].toString()) : null);
						dto.setUpdatedBy(object[10] != null ? Long.parseLong(object[10].toString()) : null);
						
						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
                    apiLogInfo.setApiResponse("All Survey List Fetched");			
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No survey found. List is null.");
				apiLogInfo.setApiResponse("No survey found. List is null");			
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			});

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Override
	public ServiceResponse getAllQuestionsBySurveyId(SurveyDTO surveyDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("");
		apiLogInfo.setApiUrl("/api/getAllQuestionsBySurveyId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("SureyId : " + surveyDTO.getSurveyId());
		try {

			if (!validationService.validateSurveyId(surveyDTO.getSurveyId())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Survey Id does not exists.");
				apiLogInfo.setApiResponse("survey Id does not exists");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

				return response;
			}

			List<SurveyQuestion> questionList = surveyQuestionRepository.findAllBySurveyId(surveyDTO.getSurveyId());

			if (questionList != null) {
				if (questionList.size() == 0) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No survey questions found. List is empty.");
					apiLogInfo.setApiResponse("No survey questions found. list is empty");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

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
					apiLogInfo.setApiResponse("All Questions By SurveyId Fetched");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No survey questions found. List is null.");
				apiLogInfo.setApiResponse("NO survey questions found.List is null");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Override
	public ServiceResponse setSurveyResponseByEmpId(SurveyDTO surveyDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("setSurveyResponseByEmpId");
		apiLogInfo.setApiUrl("/api/setSurveyResponseByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("EmpId : " + surveyDTO.getEmpId() + " , SurveyQuestionList :" + surveyDTO.getSurveyQuestionList().size());
		try {

			if (!validationService.validateEmpId(surveyDTO.getEmpId())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Id does not exists.");
				apiLogInfo.setApiResponse("Employee Id does not Exists.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

				return response;
			}

			List<SurveyEmployeeResponse> dtoList = new ArrayList<SurveyEmployeeResponse>();

			surveyDTO.getSurveyQuestionList().forEach((question) -> {
				
				SurveyEmployeeResponse checkResponse = surveyEmployeeResponseRepository.findByEmpIdAndSurveyQuestionId(surveyDTO.getEmpId(),question.getSurveyQuestionId());
				
				SurveyEmployeeResponse surveyEmployeeResponse = new SurveyEmployeeResponse();	
				
				SurveyEmployeeResponse responseList = new SurveyEmployeeResponse();

				if (checkResponse != null) {
					checkResponse.setResponse(question.getResponse());
					dtoList.add(checkResponse);
//					responseList = surveyEmployeeResponseRepository.save(checkResponse);		
				}else {
					
					surveyEmployeeResponse.setEmpId(surveyDTO.getEmpId());
					surveyEmployeeResponse.setSurveyQuestionId(question.getSurveyQuestionId());
					surveyEmployeeResponse.setResponse(question.getResponse());
					dtoList.add(surveyEmployeeResponse);
				}
			});

			List<SurveyEmployeeResponse> responseList = surveyEmployeeResponseRepository.saveAll(dtoList);

			if (responseList.size() > 0)  {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Responses stored successfully.");
				apiLogInfo.setApiResponse("Responses stored successfully");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No responses were stored.");
				apiLogInfo.setApiResponse("No responses were stored");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");

		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Override
	public ServiceResponse getAnsweredSurveysByEmpId(SurveyDTO surveyDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("");
		apiLogInfo.setApiUrl("/api/getAnsweredSurveysByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Empid : " + surveyDTO.getEmpId());
		try {
			List<Object[]> objectList = surveyQuestionRepository.getAnsweredSurveysByEmpId(surveyDTO.getEmpId());

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No survey found. List is empty.");
					apiLogInfo.setApiResponse("No survey found. List is empty");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				} else {
					List<SurveyDTO> dtoList = new ArrayList<SurveyDTO>();

					list.forEach((object) -> {

						SurveyDTO dto = new SurveyDTO();
						dto.setSurveyId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("AnsweredSurvey List fetched"+dtoList.size());			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No survey found. List is null.");
				apiLogInfo.setApiResponse("no survey found. list is null");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			});

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");

		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Override
	public ServiceResponse changeSurveyStatus(SurveyDTO surveyDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Change Survey Status");
		apiLogInfo.setApiUrl("/api/changeSurveyStatus");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("SurveyId : " + surveyDTO.getSurveyId());

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
					apiLogInfo.setApiResponse("Survey status changed");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);


				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Failed to change status.");
					apiLogInfo.setApiResponse("Failed to change status");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Survey Not Found.");
				apiLogInfo.setApiResponse("Survey not found");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Override
	public ServiceResponse getSurveyResponseByEmpIdAndSurveyId(SurveyDTO surveyDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("");
		apiLogInfo.setApiUrl("/api/getSurveyResponseByEmpIdAndSurveyId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("SurveyId :" + surveyDTO.getSurveyId() + " , EmpId: " + surveyDTO.getEmpId());
		try {
			if (!validationService.validateEmpId(surveyDTO.getEmpId())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Id does not exists.");
				apiLogInfo.setApiResponse("Employee Id does not exists");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				return response;
			}
			if (!validationService.validateSurveyId(surveyDTO.getSurveyId())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Survey Id does not exists.");
				apiLogInfo.setApiResponse("Survey Id does not exists");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				return response;
			}

			List<Object[]> objectList = surveyEmployeeResponseRepository
					.getSurveyResponseByEmpIdAndSurveyId(surveyDTO.getEmpId(), surveyDTO.getSurveyId());

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No responses found for survey. List is empty.");
					apiLogInfo.setApiResponse("No responses found for survey. list is empty");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
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
					apiLogInfo.setApiResponse("SurveyResponse By EmpId and SurveyId fetched");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No responses found for survey. List is null.");
				apiLogInfo.setApiResponse("No responses found for survey.List is  null");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			});

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Override
	public ServiceResponse getSurveyAllResponsesBySurveyId(SurveyDTO surveyDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("");
		apiLogInfo.setApiUrl("/api/getSurveyAllResponsesBySurveyId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("SurveyId : "+ surveyDTO.getSurveyId());
		try {

			if (!validationService.validateSurveyId(surveyDTO.getSurveyId())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Survey Id does not exists.");
				apiLogInfo.setApiResponse("Survey Id does not exists");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				return response;
			}

			List<Object[]> objectList = surveyEmployeeResponseRepository
					.getSurveyAllResponsesBySurveyId(surveyDTO.getSurveyId());

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No responses found for survey. List is empty.");
					apiLogInfo.setApiResponse("No responses found for survey. List is empty");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
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
						dto.setIsConsultant(object[7] != null ? object[7].toString() : null);
						dto.setIsApprentice(object[8] != null ? object[8].toString() : null);
						dto.setIsApmosysProduct(object[9] != null ? object[9].toString() : null);
						
						
						 
					    String employmentId = dto.getEmployeementId() != null ? dto.getEmployeementId().toString() : null;
					    String isConsultant = dto.getIsConsultant();
					    String isApmosysProduct = dto.getIsApmosysProduct();

					    if (employmentId != null) {
					        if ("true".equalsIgnoreCase(isConsultant)) {
					            dto.setEmploymentIdAccToET("CS-" + employmentId);
					        } else if ("true".equalsIgnoreCase(isApmosysProduct)) {
					            dto.setEmploymentIdAccToET("AP-" + employmentId);
					        } else {
					            dto.setEmploymentIdAccToET("A-" + employmentId);
					        }
					    }
						
						dtoList.add(dto);

					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("All Survey Response list fetched");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No responses found for survey. List is null.");
				apiLogInfo.setApiResponse("No responses found for survey.list is null");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			});

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Override
	public ServiceResponse deleteSurvey(SurveyDTO surveyDTO) {
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("DeleteSurvey");
        apiLogInfo.setApiUrl("/api/deleteSurvey");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("SurveyId : "+ surveyDTO.getSurveyId());
		try {

			Optional<Survey> surveyObject = surveyRepository.findById(surveyDTO.getSurveyId());

			if (surveyObject.isPresent()) {
				Survey survey = surveyObject.get();

				// Survey with Active(true) and Completed(Completed) status cannot be deleted
				if (survey.getIsActive().equals("false")) {
					surveyRepository.deleteById(survey.getSurveyId());
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Survey Deleted.");
					apiLogInfo.setApiResponse("Survey Deleted");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Active/Completed survey cannot be deleted.");
					apiLogInfo.setApiResponse("Active/Completed survey cannot be deleted");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Survey Not Found.");
				apiLogInfo.setApiResponse("Survey not Found");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Override
	public ServiceResponse updateSurvey(SurveyDTO surveyDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("UpdateSurvey");
		apiLogInfo.setApiUrl("/api/updateSurvey");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("SurveyId : "+ surveyDTO.getSurveyId() + " ,SurveyName:" + surveyDTO.getSurveyName());
		try {

			Optional<Survey> surveyObject = surveyRepository.findById(surveyDTO.getSurveyId());

			if (surveyObject.isPresent()) {
				Survey survey = surveyObject.get();

				// Survey with Active(true) and Completed(Completed) status cannot be updated.
				// Survey with Active(true), but with Type(exit) can be updated.
				if (survey.getIsActive().equals("false") || survey.getType().equals("exit")) {

					survey.setUpdatedBy(surveyDTO.getUpdatedBy());
					survey.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
					survey.setSurveyName(surveyDTO.getSurveyName());
					survey.setDescription(surveyDTO.getDescription());

					Survey surveyUpdated = surveyRepository.save(survey);

					if (surveyUpdated.getSurveyId() != null) {
						
						List<SurveyQuestion> questionList = surveyQuestionRepository.findBySurveyId(surveyUpdated.getSurveyId());
						
						
						questionList.forEach((question) -> {
							surveyQuestionRepository.deleteById(question.getSurveyQuestionId());
						});
						
						List<SurveyQuestion> list = new ArrayList<>();
						Long surveyId = surveyUpdated.getSurveyId();

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
							response.setServiceResponse("Survey updated successfully.");
							apiLogInfo.setApiResponse("Survey updated successfully");			
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

						} else {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Survey updated but no questions were added to survey.");
							apiLogInfo.setApiResponse("Survey updated but no questions were added to survey");			
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

						}

					} else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Failed to update survey.");
						apiLogInfo.setApiResponse("Failed to update survey");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

					}
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Active/Completed survey cannot be updated.");
					apiLogInfo.setApiResponse("Active/Completed survey cannot be updated");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Survey Not Found.");
				apiLogInfo.setApiResponse("Survey Not Found");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");

			
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
}
