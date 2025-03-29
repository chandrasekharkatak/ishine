package com.apmosys.employeeportal.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.ModuleDTO;
import com.apmosys.employeeportal.dto.ProjectInsightDTO;
import com.apmosys.employeeportal.dto.ProjectInsightQuestionDTO;
import com.apmosys.employeeportal.dto.ProjectQuestionDTO;
import com.apmosys.employeeportal.dto.SubModuleDTO;
import com.apmosys.employeeportal.dto.SurveyDTO;
import com.apmosys.employeeportal.dto.SurveyQuestionDTO;
import com.apmosys.employeeportal.model.ProjectInsightMilestone;
import com.apmosys.employeeportal.model.ProjectInsightModule;
import com.apmosys.employeeportal.model.ProjectInsightResponse;
import com.apmosys.employeeportal.model.ProjectInsightSubModule;
import com.apmosys.employeeportal.model.QuestionMaster;
import com.apmosys.employeeportal.model.Survey;
import com.apmosys.employeeportal.model.SurveyQuestion;
import com.apmosys.employeeportal.repository.ProjectInsightMilestoneRepository;
import com.apmosys.employeeportal.repository.ProjectInsightModuleRepository;
import com.apmosys.employeeportal.repository.ProjectInsightResponseRepository;
import com.apmosys.employeeportal.repository.ProjectInsightSubModuleRepository;
import com.apmosys.employeeportal.repository.QuestionMasterRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class ProjectInsightService {
	
	@Autowired
	ValidationService validationService;
	
	@Autowired
	ProjectInsightMilestoneRepository projectInsightMilestoneRepository;
	
	@Autowired
	QuestionMasterRepository questionMasterRepository;
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private HttpServletRequest httpRequest;
	
	@Autowired
	ProjectInsightResponseRepository projectInsightResponseRepository;
	
	@Autowired
	ProjectInsightModuleRepository projectInsightModuleRepository;
	
	@Autowired
	ProjectInsightSubModuleRepository projectInsightSubModuleRepository;
	
	public ServiceResponse addQuestion(List<ProjectQuestionDTO> questionAddList, Long entityId, String entity) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		 apiLogInfo.setSubFeatureName("addQuestion common method");
	     apiLogInfo.setApiUrl("/api/createProjectInsightQuestion --- addQuestion");
	     apiLogInfo.setLogLevel("INFO");
		
		try {
			List<QuestionMaster> questionList = new ArrayList<>();
			questionAddList.forEach((questionObj) -> {
				QuestionMaster newProjQuestion = new QuestionMaster();
				
				newProjQuestion.setEntityId(entityId);
				newProjQuestion.setEntityType(entity);
				newProjQuestion.setQuestion(questionObj.getQuestion());
				newProjQuestion.setDescription(questionObj.getDescription());
				newProjQuestion.setDocumentUpload(questionObj.getDocumentUpload());
				newProjQuestion.setOptions(questionObj.getOptions());
				newProjQuestion.setOptionType(questionObj.getOptionType());
				newProjQuestion.setRequired(questionObj.getRequired());

				questionList.add(newProjQuestion);
			});
			
			List<QuestionMaster> listSaved = questionMasterRepository.saveAll(questionList);
			
			if (listSaved.size() > 0) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Project Insight created successfully.");
				apiLogInfo.setApiResponse("Project Insight Successfully");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project Insight but no questions were added to milestone.");
				apiLogInfo.setApiResponse("Project Insight but no questions were added to milestone");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		
		return response;
	}

	@Transactional
	public ServiceResponse createProjectInsightQuestion(ProjectInsightDTO projectInsightDTO) {
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("createProjectInsightQuestion");
        apiLogInfo.setApiUrl("/api/createProjectInsightQuestion");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("CreatedBy : " + projectInsightDTO.getCreatedBy() + " ,ProjectId :"+ projectInsightDTO.getProjectId() 
         + " ,Created By EmpId :" + projectInsightDTO.getCreatedBy());
        
        AtomicBoolean isSuccess = new AtomicBoolean(false);

		try {

			if (!validationService.validateEmpId(projectInsightDTO.getCreatedBy())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Id does not exists.");
				apiLogInfo.setApiResponse("Employee Id does not exists");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				return response;
			}
			
			if(!projectInsightDTO.getProjectInsightQuestionList().isEmpty()) {
				projectInsightDTO.getProjectInsightQuestionList().forEach((project) -> {
					ProjectInsightMilestone newProjectInsight = new ProjectInsightMilestone();

					newProjectInsight.setCreatedBy(projectInsightDTO.getCreatedBy());
					newProjectInsight.setProjectId(projectInsightDTO.getProjectId());
					newProjectInsight.setAssignedTo(project.getAssignedTo());
					newProjectInsight.setMilestone(project.getMilestone());
					newProjectInsight.setDescription(project.getDescription());
					newProjectInsight.setDeptId(project.getDeptId());
					newProjectInsight.setRedmineId(project.getRedmineId());
					

					ProjectInsightMilestone newMilestoneCreated = projectInsightMilestoneRepository.save(newProjectInsight);
					
					if(newMilestoneCreated.getMilestoneId() != null) {
						isSuccess.set(true);
						
						Long milestoneId = newMilestoneCreated.getMilestoneId();
						
						//Add Milestone question
						if(!project.getProjectQuestion().isEmpty()) {
							ServiceResponse milestoneQuestionResponse = addQuestion(project.getProjectQuestion(),
									milestoneId, "Milestone");
							
							if ("Success".equals(milestoneQuestionResponse.getServiceStatus())) {
				                isSuccess.set(true);
				            } else {
				                isSuccess.set(false);
				            }
								
						}
						
						//Add Module question
						if(!project.getModuleList().isEmpty()) {
							project.getModuleList().forEach((module) -> {
								ProjectInsightModule newProjectInsightModule = new ProjectInsightModule();

								newProjectInsightModule.setCreatedBy(projectInsightDTO.getCreatedBy());
								newProjectInsightModule.setAssignedTo(module.getAssignedTo());
								newProjectInsightModule.setMilestoneId(milestoneId);
								newProjectInsightModule.setDescription(module.getDescription());
								newProjectInsightModule.setRedmineId(module.getRedmineId());
								newProjectInsightModule.setModule(module.getModule());
								
								ProjectInsightModule moduleResponse = projectInsightModuleRepository.save(newProjectInsightModule);
								
								if(moduleResponse != null) {
									Long moduleId = moduleResponse.getModuleId();
									 isSuccess.set(true);
									
									//Add Milestone question
									if(!module.getProjectQuestion().isEmpty()) {
										ServiceResponse moduleQuestionResponse = addQuestion(module.getProjectQuestion(),
												moduleId, "Module");
										
										if ("Success".equals(moduleQuestionResponse.getServiceStatus())) {
							                isSuccess.set(true);
							            } else {
							                isSuccess.set(false);
							            }
									}
									
									if(!module.getSubModuleList().isEmpty()) {
										module.getSubModuleList().forEach((submodule) -> {
											ProjectInsightSubModule newProjectInsightSubModule = new ProjectInsightSubModule();

											newProjectInsightSubModule.setCreatedBy(projectInsightDTO.getCreatedBy());
											newProjectInsightSubModule.setAssignedTo(submodule.getAssignedTo());
											newProjectInsightSubModule.setModuleId(moduleId);
											newProjectInsightSubModule.setDescription(submodule.getDescription());
											newProjectInsightSubModule.setRedmineId(submodule.getRedmineId());
											newProjectInsightSubModule.setSubmodule(submodule.getSubModule());
											
											ProjectInsightSubModule submoduleResponse = projectInsightSubModuleRepository.save(newProjectInsightSubModule);
											
											if(submoduleResponse != null) {
												Long submoduleId = submoduleResponse.getSubmoduleId();
												isSuccess.set(true);
												
												//Add Milestone question
												if(!submodule.getProjectQuestion().isEmpty()) {
													ServiceResponse submoduleQuestionResponse = addQuestion(submodule.getProjectQuestion(),
															submoduleId, "SubModule");
													
													if ("Success".equals(submoduleQuestionResponse.getServiceStatus())) {
										                isSuccess.set(true);
										            } else {
										                isSuccess.set(false);
										            }
												}
											}
										});
									}
								}
							});
						}
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Failed to create Project Insight Questions.");
						apiLogInfo.setApiResponse("Failed to create Project Insight Questions");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					}
				});
				
				if(isSuccess.get()) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Project Insight created successfully.");
					apiLogInfo.setApiResponse("Project Insight Successfully");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}
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

	public ServiceResponse getAllProjectInsightList() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("");
		apiLogInfo.setApiUrl("/api/getAllProjectInsightList");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("AllSurveyList size : " + projectInsightMilestoneRepository.getAllProjectInsight().size());

		try {

			List<Object[]> objectList = projectInsightMilestoneRepository.getAllProjectInsight();

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No Project Insight found. List is empty.");
				} else {
					List<ProjectInsightDTO> dtoList = new ArrayList<ProjectInsightDTO>();

					list.forEach((object) -> {

						ProjectInsightDTO dto = new ProjectInsightDTO();
						
						dto.setProjectId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						dto.setProjectName(object[1] != null ? object[1].toString() : null);
						dto.setProjectManagerId(object[2] != null ? Long.parseLong(object[2].toString()) : null);
						dto.setProjectManagerName(object[3] != null ? object[3].toString() : null);
						dto.setCreatedBy(object[4] != null ? Long.parseLong(object[4].toString()) : null);
						dto.setCreatedByName(object[5] != null ? object[5].toString() : null);
						dto.setCreatedOn(object[6] != null ? object[6].toString() : null);
						
						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
                    apiLogInfo.setApiResponse("All Project Insight List Fetched");			
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Project Insight found. List is null.");
				apiLogInfo.setApiResponse("No Project Insight found. List is null");			
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
	
	public List<ProjectQuestionDTO> getQuestion(Long entityId, String entity) {
		 List<ProjectQuestionDTO> response = new ArrayList<ProjectQuestionDTO>();
		LogDTO apiLogInfo = new LogDTO();
		 apiLogInfo.setSubFeatureName("getQuestion common method");
	     apiLogInfo.setApiUrl("/api/getAllQuestionsByProjectId --- getQuestion");
	     apiLogInfo.setLogLevel("INFO");
		
		try {
			List<QuestionMaster> questionList = questionMasterRepository.findByEntityIdAndEntityType(entityId,entity);
			
			if(!questionList.isEmpty()) {
				questionList.forEach((question) -> {
					ProjectQuestionDTO questionDto = new ProjectQuestionDTO();
					
					questionDto.setDescription(question.getDescription());
					questionDto.setDocumentUpload(question.getDocumentUpload());
					questionDto.setOptions(question.getOptions());
					questionDto.setOptionType(question.getOptionType());
					questionDto.setQuestion(question.getQuestion());
					questionDto.setQuestionId(question.getQuestionMasterId());
					questionDto.setRequired(question.getRequired());
					questionDto.setEntityId(question.getEntityId());
					questionDto.setEntityType(question.getEntityType());
					
					ProjectInsightResponse dbResponse = projectInsightResponseRepository
							.findByQuestionMasterId(question.getQuestionMasterId());
					
					if(dbResponse != null) {
						questionDto.setResponse(dbResponse.getResponse());
						questionDto.setResponseId(dbResponse.getProjectInsightResponseId());
						questionDto.setEmpId(dbResponse.getEmpId());
						questionDto.setDocumentPath(dbResponse.getDocumentPath());
					}
					
					response.add(questionDto);
				});
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		
		return response;
	}

	public ServiceResponse getAllQuestionsByProjectId(ProjectInsightDTO projectInsightDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("");
		apiLogInfo.setApiUrl("/api/getAllQuestionsByProjectId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("ProjectId : " + projectInsightDTO.getProjectId());
		try {

			if (!validationService.validateProjectId(projectInsightDTO.getProjectId())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project Id does not exists.");
				apiLogInfo.setApiResponse("Project Id does not exists");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

				return response;
			}

			List<ProjectInsightMilestone> mileStoneList = projectInsightMilestoneRepository.getByProjectId(projectInsightDTO.getProjectId());

			if (mileStoneList != null) {
				if (mileStoneList.size() == 0) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No Project Insight found. List is empty.");
					apiLogInfo.setApiResponse("No Project Insight found. list is empty");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

				} else {
					ProjectInsightDTO responseObject = new ProjectInsightDTO();
					//Project Info
					responseObject.setProjectId(projectInsightDTO.getProjectId());
					responseObject.setProjectName(projectInsightDTO.getProjectName());
					responseObject.setProjectManagerId(projectInsightDTO.getProjectManagerId());
					responseObject.setProjectManagerName(projectInsightDTO.getProjectManagerName());
					responseObject.setCreatedBy(projectInsightDTO.getCreatedBy());
					
					//MileStoneList
					List<ProjectInsightQuestionDTO> projectInsightQuestion = new ArrayList<>();
					mileStoneList.forEach((mileStone) -> {
						ProjectInsightQuestionDTO mileStoneProjObject = new ProjectInsightQuestionDTO();
						
						mileStoneProjObject.setAssignedTo(mileStone.getAssignedTo());
						mileStoneProjObject.setDeptId(mileStone.getDeptId());
						mileStoneProjObject.setDescription(mileStone.getDescription());
						mileStoneProjObject.setMilestone(mileStone.getMilestone());
						mileStoneProjObject.setMilestoneId(mileStone.getMilestoneId());
						mileStoneProjObject.setRedmineId(mileStone.getMilestoneId());
						
						// --> Milestone Question
						List<ProjectQuestionDTO> projectQuestionList = getQuestion(mileStone.getMilestoneId(), "Milestone");
						mileStoneProjObject.setProjectQuestion(projectQuestionList);
						
						// --> get Module
						List<ProjectInsightModule> moduleList = projectInsightModuleRepository.getByMilestoneId(mileStone.getMilestoneId());
						List<ModuleDTO> moduleResponseList = new ArrayList<>();
						if(!moduleList.isEmpty()) {
							moduleList.forEach((module) -> {
								ModuleDTO modDto = new ModuleDTO();
								
								modDto.setAssignedTo(module.getAssignedTo());
								modDto.setCreatedBy(module.getCreatedBy());
								modDto.setCreatedOn(module.getCreatedOn().toString());
								modDto.setDescription(module.getDescription());
								modDto.setMilestoneId(module.getMilestoneId());
								modDto.setModule(module.getModule());
								modDto.setModuleId(module.getModuleId());
								modDto.setRedmineId(module.getRedmineId());
								modDto.setUpdatedBy(module.getUpdatedBy());
								
								//--> get submodule
								List<ProjectInsightSubModule> subModuleList = projectInsightSubModuleRepository.getByModuleId(module.getModuleId());
								List<SubModuleDTO> subModuleResponseList = new ArrayList<>();
								if(!subModuleList.isEmpty()) {
									subModuleList.forEach((submodule) -> {
										SubModuleDTO submodDto = new SubModuleDTO();
										
										submodDto.setAssignedTo(submodule.getAssignedTo());
										submodDto.setCreatedBy(submodule.getCreatedBy());
										submodDto.setCreatedOn(submodule.getCreatedOn().toString());
										submodDto.setDescription(submodule.getDescription());
										submodDto.setModuleId(submodule.getModuleId());
										submodDto.setRedmineId(submodule.getRedmineId());
										submodDto.setSubModule(submodule.getSubmodule());
										submodDto.setSubmoduleId(submodule.getSubmoduleId());
										submodDto.setUpdatedBy(submodule.getUpdatedBy());
										
										// --> subModule question
										List<ProjectQuestionDTO> submoduleQuestionList = getQuestion(submodule.getSubmoduleId(), "SubModule");
										submodDto.setProjectQuestion(submoduleQuestionList);
										
										subModuleResponseList.add(submodDto);
									});
								}
								
								modDto.setSubModuleList(subModuleResponseList);
								
								//--> module question
								List<ProjectQuestionDTO> moduleQuestionList = getQuestion(module.getModuleId(), "Module");
								modDto.setProjectQuestion(moduleQuestionList);
								
								moduleResponseList.add(modDto);
							});
						}
						
						mileStoneProjObject.setModuleList(moduleResponseList);
						projectInsightQuestion.add(mileStoneProjObject);
					});
					responseObject.setProjectInsightQuestionList(projectInsightQuestion);

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(responseObject);
					apiLogInfo.setApiResponse("All Questions By MilestoneId Fetched");			
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

	public ServiceResponse updateProjectInsightQuestion(ProjectInsightDTO projectInsightDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("updateProjectInsightQuestion");
		apiLogInfo.setApiUrl("/api/updateProjectInsightQuestion");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("projectId : "+ projectInsightDTO.getProjectId());
		try {

//			if(!projectInsightDTO.getProjectInsightQuestionList().isEmpty()) {
//				projectInsightDTO.getProjectInsightQuestionList().forEach((milestone) -> {
//					
//					if(milestone.getMilestoneId() != null) {
//						ProjectInsightMilestone milestoneDbObject = projectInsightMilestoneRepository
//								.getByMilestoneId(milestone.getMilestoneId());
//						
//						if(milestoneDbObject != null) {
//							
//							milestoneDbObject.setDescription(milestone.getDescription());
//							milestoneDbObject.setMilestone(milestone.getMilestone());
//							milestoneDbObject.setUpdatedBy(projectInsightDTO.getUpdatedBy());
//							milestoneDbObject.setAssignedTo(milestone.getAssignedTo());
//							milestoneDbObject.setUpdatedOn(LocalDateTime.now());
//							
//							ProjectInsightMilestone milestoneUpdateDbResponse = projectInsightMilestoneRepository
//									.save(milestoneDbObject);
//							
//							if(milestoneUpdateDbResponse != null) {
//								//update question
//								if(!milestone.getProjectQuestion().isEmpty()) {
//									milestone.getProjectQuestion().forEach((question) -> {
//										QuestionMaster questionDbObject = questionMasterRepository.findByQuestionMasterId(question.getQuestionId());
//										
//										if(questionDbObject != null) {
//											
//											questionDbObject.setQuestion(question.getQuestion());
//											questionDbObject.setDescription(question.getDescription());
//											questionDbObject.setDocumentUpload(question.getDocumentUpload());
//											questionDbObject.setOptions(question.getOptions());
//											questionDbObject.setOptionType(question.getOptionType());
//											questionDbObject.setRequired(question.getRequired());
//											
//											QuestionMaster questionDbsave = questionMasterRepository.save(questionDbObject);
//											
//											if(questionDbsave != null) {
//												response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//												response.setServiceResponse("Project Insight Updated successfully.");
//												apiLogInfo.setApiResponse("Project Insight Updated successfully.");			
//												apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//											}else {
//												response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//												response.setServiceResponse("Unable to update Project Insight.");
//												apiLogInfo.setApiResponse("Unable to update Project Insight.");			
//												apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//											}
//										}else {
//											response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//											response.setServiceResponse("No Project Insight Question Found in DB.");
//											apiLogInfo.setApiResponse("No Project Insight Question Found in DB.");			
//											apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//										}
//									});
//								}else {
//									response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//									response.setServiceResponse("No Project Insight Question found to update.");
//									apiLogInfo.setApiResponse("No Project Insight Question found to update.");			
//									apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//								}
//							}else {
//								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//								response.setServiceResponse("Unable to update project insight milestone.");
//								apiLogInfo.setApiResponse("Unable to update project insight milestone.");			
//								apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//							}
//						}else {
//							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//							response.setServiceResponse("No Project Insight Milestone Found.");
//							apiLogInfo.setApiResponse("No Project Insight Milestone Found.");			
//							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//						}
//					}else {
//						//create New Milestone
//						ProjectInsightMilestone newProjectInsight = new ProjectInsightMilestone();
//
//						newProjectInsight.setCreatedBy(projectInsightDTO.getCreatedBy());
//						newProjectInsight.setProjectId(projectInsightDTO.getProjectId());
//						newProjectInsight.setAssignedTo(projectInsightDTO.getProjectManagerId());
//						newProjectInsight.setMilestone(milestone.getMilestone());
//						newProjectInsight.setDescription(milestone.getDescription());
//						newProjectInsight.setDeptId(milestone.getDeptId());
//						
//
//						ProjectInsightMilestone newMilestoneCreated = projectInsightMilestoneRepository.save(newProjectInsight);
//						
//						if(newMilestoneCreated.getMilestoneId() != null) {
//							List<QuestionMaster> questionList = new ArrayList<>();
//							Long milestoneId = newMilestoneCreated.getMilestoneId();
//							
//							milestone.getProjectQuestion().forEach((questionObj) -> {
//								QuestionMaster newProjQuestion = new QuestionMaster();
//
//								newProjQuestion.setMilestoneId(newMilestoneCreated.getMilestoneId());
//								newProjQuestion.setQuestion(questionObj.getQuestion());
//								newProjQuestion.setDescription(questionObj.getDescription());
//								newProjQuestion.setDocumentUpload(questionObj.getDocumentUpload());
//								newProjQuestion.setOptions(questionObj.getOptions());
//								newProjQuestion.setOptionType(questionObj.getOptionType());
//								newProjQuestion.setRequired(questionObj.getRequired());
//
//								questionList.add(newProjQuestion);
//							});
//							
//							List<QuestionMaster> listSaved = questionMasterRepository.saveAll(questionList);
//							
//							if (listSaved.size() > 0) {
//								response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//								response.setServiceResponse("Project Insight created successfully.");
//								apiLogInfo.setApiResponse("Project Insight Successfully");			
//								apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//							} else {
//								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//								response.setServiceResponse("Project Insight but no questions were added to milestone.");
//								apiLogInfo.setApiResponse("Project Insight but no questions were added to milestone");			
//								apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//							}
//							
//						}else {
//							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//							response.setServiceResponse("Failed to create New Project Insight Questions.");
//							apiLogInfo.setApiResponse("Failed to create New Project Insight Questions");			
//							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//						}
//					}
//				});
//			}else {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("No Project Insight Update Found.");
//				apiLogInfo.setApiResponse("No Project Insight Update Found.");			
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			}
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
