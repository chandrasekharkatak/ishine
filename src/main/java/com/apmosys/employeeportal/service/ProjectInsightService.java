package com.apmosys.employeeportal.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.ModuleDTO;
import com.apmosys.employeeportal.dto.ProjectInsightDTO;
import com.apmosys.employeeportal.dto.ProjectInsightQuestionDTO;
import com.apmosys.employeeportal.dto.ProjectQuestionDTO;
import com.apmosys.employeeportal.dto.ProjectResponseDTO;
import com.apmosys.employeeportal.dto.SubModuleDTO;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectInsightAssignees;
import com.apmosys.employeeportal.model.ProjectInsightMilestone;
import com.apmosys.employeeportal.model.ProjectInsightModule;
import com.apmosys.employeeportal.model.ProjectInsightResponse;
import com.apmosys.employeeportal.model.ProjectInsightSubModule;
import com.apmosys.employeeportal.model.QuestionMaster;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.ProjectInsightAssigneesRepository;
import com.apmosys.employeeportal.repository.ProjectInsightMilestoneRepository;
import com.apmosys.employeeportal.repository.ProjectInsightModuleRepository;
import com.apmosys.employeeportal.repository.ProjectInsightResponseRepository;
import com.apmosys.employeeportal.repository.ProjectInsightSubModuleRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.QuestionMasterRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

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
	private ProjectRepository projectRepository;
	
	@Autowired
	private ProjectInsightSubModuleRepository projectInsightSubModuleRepository;
	
	@Autowired
	ProjectInsightModuleRepository projectInsightModuleRepository;
	
	@Autowired
	ProjectInsightAssigneesRepository projectInsightAssigneesRepository;
	
	@Autowired
	private EmployeeTeamMapRepository employeeTeamMapRepository;
	
	@Value("${dmsPortalUrl}")
	private String dmsPortalUrl;
	
	@Value("${dmsPortalUrlKey}")
	private String dmsPortalUrlKey;
	
	public ServiceResponse addUpdateQuestion(List<ProjectQuestionDTO> questionAddList, Long entityId, String entity) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		 apiLogInfo.setSubFeatureName("addQuestion common method");
	     apiLogInfo.setApiUrl("/api/createProjectInsightQuestion --- addQuestion");
	     apiLogInfo.setLogLevel("INFO");
		
		try {
			List<QuestionMaster> questionList = new ArrayList<>();
			questionAddList.forEach((questionObj) -> {
				
				//update
				if(questionObj.getQuestionId() != null) {
					QuestionMaster quesDbObject = questionMasterRepository.findByQuestionMasterId(questionObj.getQuestionId());
					
					if(quesDbObject != null) {
						
						quesDbObject.setQuestion(questionObj.getQuestion());
						quesDbObject.setDescription(questionObj.getDescription());
						quesDbObject.setDocumentUpload(questionObj.getDocumentUpload());
						quesDbObject.setOptions(questionObj.getOptions());
						quesDbObject.setOptionType(questionObj.getOptionType());
						quesDbObject.setRequired(questionObj.getRequired());
						
						questionList.add(quesDbObject);
					}
					
				}else {
					//Add
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
				}
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
					newProjectInsight.setMilestone(project.getMilestone());
					newProjectInsight.setDescription(project.getDescription());
					newProjectInsight.setDeptId(project.getDeptId());
					newProjectInsight.setRedmineId(project.getRedmineId());
					

					ProjectInsightMilestone newMilestoneCreated = projectInsightMilestoneRepository.save(newProjectInsight);
					
					if(newMilestoneCreated.getMilestoneId() != null) {
						isSuccess.set(true);
						Long milestoneId = newMilestoneCreated.getMilestoneId();
						
						//Assign to
						if(!project.getAssignedTo().isEmpty()) {
							for(Long assignToId: project.getAssignedTo()) {
								ProjectInsightAssignees newAssignObj = new ProjectInsightAssignees();
								
								newAssignObj.setAssignedTo(assignToId);
								newAssignObj.setEntityId(milestoneId);
								newAssignObj.setEntityType("Milestone");
								
								ProjectInsightAssignees assignToDbResponse = projectInsightAssigneesRepository.save(newAssignObj);
							}
						}
						
						//Add Milestone question
						if(!project.getProjectQuestion().isEmpty()) {
							ServiceResponse milestoneQuestionResponse = addUpdateQuestion(project.getProjectQuestion(),
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
								newProjectInsightModule.setMilestoneId(milestoneId);
								newProjectInsightModule.setDescription(module.getDescription());
								newProjectInsightModule.setRedmineId(module.getRedmineId());
								newProjectInsightModule.setModule(module.getModule());
								
								ProjectInsightModule moduleResponse = projectInsightModuleRepository.save(newProjectInsightModule);
								
								if(moduleResponse != null) {
									Long moduleId = moduleResponse.getModuleId();
									 isSuccess.set(true);
									 
									//Assign to
										if(!module.getAssignedTo().isEmpty()) {
											for(Long assignToId: module.getAssignedTo()) {
												ProjectInsightAssignees newAssignObj = new ProjectInsightAssignees();
												
												newAssignObj.setAssignedTo(assignToId);
												newAssignObj.setEntityId(moduleId);
												newAssignObj.setEntityType("Module");
												
												ProjectInsightAssignees assignToDbResponse = projectInsightAssigneesRepository.save(newAssignObj);
											}
										}
									
									//Add Milestone question
									if(!module.getProjectQuestion().isEmpty()) {
										ServiceResponse moduleQuestionResponse = addUpdateQuestion(module.getProjectQuestion(),
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
											newProjectInsightSubModule.setModuleId(moduleId);
											newProjectInsightSubModule.setDescription(submodule.getDescription());
											newProjectInsightSubModule.setRedmineId(submodule.getRedmineId());
											newProjectInsightSubModule.setSubmodule(submodule.getSubModule());
											
											ProjectInsightSubModule submoduleResponse = projectInsightSubModuleRepository.save(newProjectInsightSubModule);
											
											if(submoduleResponse != null) {
												Long submoduleId = submoduleResponse.getSubmoduleId();
												isSuccess.set(true);
												
												//Assign to
												if(!submodule.getAssignedTo().isEmpty()) {
													for(Long assignToId: submodule.getAssignedTo()) {
														ProjectInsightAssignees newAssignObj = new ProjectInsightAssignees();
														
														newAssignObj.setAssignedTo(assignToId);
														newAssignObj.setEntityId(submoduleId);
														newAssignObj.setEntityType("SubModule");
														
														ProjectInsightAssignees assignToDbResponse = projectInsightAssigneesRepository.save(newAssignObj);
													}
												}
												
												//Add Milestone question
												if(!submodule.getProjectQuestion().isEmpty()) {
													ServiceResponse submoduleQuestionResponse = addUpdateQuestion(submodule.getProjectQuestion(),
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

	public ServiceResponse getAllProjectInsightList(ProjectInsightDTO projectInsightDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("");
		apiLogInfo.setApiUrl("/api/getAllProjectInsightList");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("AllSurveyList size : " + projectInsightMilestoneRepository.getAllProjectInsight().size());

		try {

			List<Object[]> objectList = null; 
			
			if(projectInsightDTO.getEmployeeRole().equals("Employee") || 
					projectInsightDTO.getEmployeeRole().equals("TeamLead") ||
					projectInsightDTO.getEmployeeRole().equals("Manager")) {
				objectList= projectInsightMilestoneRepository.getAllProjectInsightByUser(projectInsightDTO.getEmpId());
			}else {
				objectList= projectInsightMilestoneRepository.getAllProjectInsight();
			}

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
						
						List<ProjectInsightAssignees> assignToDbList = projectInsightAssigneesRepository.getByEntityIdAndEntityType(mileStone.getMilestoneId(), "Milestone");

						if(!assignToDbList.isEmpty()) {
							mileStoneProjObject.setAssignedTo(assignToDbList.stream()
	                                .map(ProjectInsightAssignees::getAssignedTo)
	                                .collect(Collectors.toList()));
						}
						
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
								
								List<ProjectInsightAssignees> assignModuleToDbList = projectInsightAssigneesRepository
										.getByEntityIdAndEntityType(module.getModuleId(), "Module");

								if(!assignModuleToDbList.isEmpty()) {
									modDto.setAssignedTo(assignModuleToDbList.stream()
			                                .map(ProjectInsightAssignees::getAssignedTo)
			                                .collect(Collectors.toList()));
								}
								
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
										
										List<ProjectInsightAssignees> assignSubModuleToDbList = projectInsightAssigneesRepository
												.getByEntityIdAndEntityType(submodule.getSubmoduleId(), "SubModule");

										if(!assignSubModuleToDbList.isEmpty()) {
											submodDto.setAssignedTo(assignSubModuleToDbList.stream()
					                                .map(ProjectInsightAssignees::getAssignedTo)
					                                .collect(Collectors.toList()));
										}
										
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

			if(!projectInsightDTO.getProjectInsightQuestionList().isEmpty()) {
				projectInsightDTO.getProjectInsightQuestionList().forEach((milestone) -> {
					
					if(milestone.getMilestoneId() != null) {
						ProjectInsightMilestone milestoneDbObject = projectInsightMilestoneRepository
								.getByMilestoneId(milestone.getMilestoneId());
						
						if(milestoneDbObject != null) {
							
							milestoneDbObject.setUpdatedBy(projectInsightDTO.getUpdatedBy());
							milestoneDbObject.setMilestone(milestone.getMilestone());
							milestoneDbObject.setDescription(milestone.getDescription());
							milestoneDbObject.setDeptId(milestone.getDeptId());
							milestoneDbObject.setRedmineId(milestone.getRedmineId());
							
							ProjectInsightMilestone milestoneUpdateDbResponse = projectInsightMilestoneRepository
									.save(milestoneDbObject);
							
							if(milestoneUpdateDbResponse != null) {
								//add/update milestone question
								if(!milestone.getProjectQuestion().isEmpty()) {
									ServiceResponse milestoneQuestionResponse = addUpdateQuestion(milestone.getProjectQuestion(),
											null, "Milestone");		
								}
								
								//Assign to
								if(!milestone.getAssignedTo().isEmpty()) {
									List<ProjectInsightAssignees> milestoneAssigneeDbResponse = projectInsightAssigneesRepository
											.getByEntityIdAndEntityType(milestone.getMilestoneId(), "Milestone");
									
									if(!milestoneAssigneeDbResponse.isEmpty()) {
										projectInsightAssigneesRepository.deleteAll(milestoneAssigneeDbResponse);
									}
									
									for(Long assignToId: milestone.getAssignedTo()) {
										ProjectInsightAssignees newAssignObj = new ProjectInsightAssignees();
										
										newAssignObj.setAssignedTo(assignToId);
										newAssignObj.setEntityId(milestone.getMilestoneId());
										newAssignObj.setEntityType("Milestone");
										
										ProjectInsightAssignees assignToDbResponse = projectInsightAssigneesRepository.save(newAssignObj);
									}
								}
								
								// --> Module
								if(!milestone.getModuleList().isEmpty()) {
									milestone.getModuleList().forEach((module) -> {
										ProjectInsightModule moduleDbResponse = new ProjectInsightModule();
										if(module.getModuleId() != null) {
											//Update
											ProjectInsightModule moduleDbObject = projectInsightModuleRepository
													.getByModuleId(module.getModuleId());
											
											if(moduleDbObject != null) {
												moduleDbObject.setDescription(module.getDescription());
												moduleDbObject.setModule(module.getModule());
												moduleDbObject.setRedmineId(module.getRedmineId());
												moduleDbObject.setUpdatedBy(projectInsightDTO.getUpdatedBy());
												moduleDbObject.setUpdatedOn(LocalDateTime.now());
												
												moduleDbResponse = projectInsightModuleRepository.save(moduleDbObject);
											}
										}else {
											//create
											ProjectInsightModule newProjectInsightModule = new ProjectInsightModule();

											newProjectInsightModule.setCreatedBy(projectInsightDTO.getCreatedBy());
											newProjectInsightModule.setMilestoneId(milestoneUpdateDbResponse.getMilestoneId());
											newProjectInsightModule.setDescription(module.getDescription());
											newProjectInsightModule.setRedmineId(module.getRedmineId());
											newProjectInsightModule.setModule(module.getModule());
											
											moduleDbResponse = projectInsightModuleRepository.save(newProjectInsightModule);
										}
										
										Long moduleId = moduleDbResponse.getModuleId();
										 
										//Assign to
											if(!module.getAssignedTo().isEmpty()) {
												List<ProjectInsightAssignees> moduleAssigneeDbResponse = projectInsightAssigneesRepository
														.getByEntityIdAndEntityType(moduleId, "Module");
												
												if(!moduleAssigneeDbResponse.isEmpty()) {
													projectInsightAssigneesRepository.deleteAll(moduleAssigneeDbResponse);
												}
												
												for(Long assignToId: module.getAssignedTo()) {
													ProjectInsightAssignees newAssignObj = new ProjectInsightAssignees();
													
													newAssignObj.setAssignedTo(assignToId);
													newAssignObj.setEntityId(moduleId);
													newAssignObj.setEntityType("Module");
													
													ProjectInsightAssignees assignToDbResponse = projectInsightAssigneesRepository.save(newAssignObj);
												}
											}
										
										
										//add/update module question
										if(!module.getProjectQuestion().isEmpty()) {
											ServiceResponse moduleQuestionResponse = addUpdateQuestion(module.getProjectQuestion(),
													null, "Module");		
										}
										
										
										// --> SubModule
										if(!module.getSubModuleList().isEmpty()) {
											module.getSubModuleList().forEach((submodule) -> {
												ProjectInsightSubModule submoduleObject = new ProjectInsightSubModule();
												//update submodule
												if(submodule.getSubmoduleId() != null) {
													ProjectInsightSubModule submoduleDbResponse = projectInsightSubModuleRepository
															.getById(submodule.getSubmoduleId());
													
													if(submoduleDbResponse != null) {
														
														submoduleDbResponse.setUpdatedBy(projectInsightDTO.getUpdatedBy());
														submoduleDbResponse.setDescription(submodule.getDescription());
														submoduleDbResponse.setRedmineId(submodule.getRedmineId());
														submoduleDbResponse.setSubmodule(submodule.getSubModule());
														
														submoduleObject = projectInsightSubModuleRepository.save(submoduleDbResponse);
													}
												}else {
													//Create submodule
													ProjectInsightSubModule newProjectInsightSubModule = new ProjectInsightSubModule();

													newProjectInsightSubModule.setCreatedBy(projectInsightDTO.getCreatedBy());
													newProjectInsightSubModule.setModuleId(moduleId);
													newProjectInsightSubModule.setDescription(submodule.getDescription());
													newProjectInsightSubModule.setRedmineId(submodule.getRedmineId());
													newProjectInsightSubModule.setSubmodule(submodule.getSubModule());
													
													submoduleObject = projectInsightSubModuleRepository.save(newProjectInsightSubModule);
												}
												
												//Assign to
												if(submodule.getAssignedTo() != null &&  !submodule.getAssignedTo().isEmpty()) {
													List<ProjectInsightAssignees> submoduleAssigneeDbResponse = projectInsightAssigneesRepository
															.getByEntityIdAndEntityType(submoduleObject.getSubmoduleId(), "SubModule");
													
													if(!submoduleAssigneeDbResponse.isEmpty()) {
														projectInsightAssigneesRepository.deleteAll(submoduleAssigneeDbResponse);
													}
													
													for(Long assignToId: submodule.getAssignedTo()) {
														ProjectInsightAssignees newAssignObj = new ProjectInsightAssignees();
														
														newAssignObj.setAssignedTo(assignToId);
														newAssignObj.setEntityId(moduleId);
														newAssignObj.setEntityType("SubModule");
														
														ProjectInsightAssignees assignToDbResponse = projectInsightAssigneesRepository.save(newAssignObj);
													}
												}
											
											
											//add/update module question
											if(!submodule.getProjectQuestion().isEmpty()) {
												ServiceResponse submoduleQuestionResponse = addUpdateQuestion(submodule.getProjectQuestion(),
														null, "SubModule");		
											}
											});
										}
									});
								}
							}else {
								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
								response.setServiceResponse("Unable to update project insight milestone.");
								apiLogInfo.setApiResponse("Unable to update project insight milestone.");			
								apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
							}
						}else {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("No Project Insight Milestone Found.");
							apiLogInfo.setApiResponse("No Project Insight Milestone Found.");			
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						}
					}else {
						//Create New Milestone
						ProjectInsightMilestone newProjectInsight = new ProjectInsightMilestone();

						newProjectInsight.setCreatedBy(projectInsightDTO.getCreatedBy());
						newProjectInsight.setProjectId(projectInsightDTO.getProjectId());
						newProjectInsight.setMilestone(milestone.getMilestone());
						newProjectInsight.setDescription(milestone.getDescription());
						newProjectInsight.setDeptId(milestone.getDeptId());
						newProjectInsight.setRedmineId(milestone.getRedmineId());
						

						ProjectInsightMilestone newMilestoneCreated = projectInsightMilestoneRepository.save(newProjectInsight);
						
						if(newMilestoneCreated.getMilestoneId() != null) {
							Long milestoneId = newMilestoneCreated.getMilestoneId();
							
							//Assign to
							if(!milestone.getAssignedTo().isEmpty()) {
								for(Long assignToId: milestone.getAssignedTo()) {
									ProjectInsightAssignees newAssignObj = new ProjectInsightAssignees();
									
									newAssignObj.setAssignedTo(assignToId);
									newAssignObj.setEntityId(milestoneId);
									newAssignObj.setEntityType("Milestone");
									
									ProjectInsightAssignees assignToDbResponse = projectInsightAssigneesRepository.save(newAssignObj);
								}
							}
							
							//Add Milestone question
							if(!milestone.getProjectQuestion().isEmpty()) {
								ServiceResponse milestoneQuestionResponse = addUpdateQuestion(milestone.getProjectQuestion(),
										milestoneId, "Milestone");
							}
							
							//Add Module question
							if(!milestone.getModuleList().isEmpty()) {
								milestone.getModuleList().forEach((module) -> {
									ProjectInsightModule newProjectInsightModule = new ProjectInsightModule();

									newProjectInsightModule.setCreatedBy(projectInsightDTO.getCreatedBy());
									newProjectInsightModule.setMilestoneId(milestoneId);
									newProjectInsightModule.setDescription(module.getDescription());
									newProjectInsightModule.setRedmineId(module.getRedmineId());
									newProjectInsightModule.setModule(module.getModule());
									
									ProjectInsightModule moduleResponse = projectInsightModuleRepository.save(newProjectInsightModule);
									
									if(moduleResponse != null) {
										Long moduleId = moduleResponse.getModuleId();
										 
										//Assign to
											if(!module.getAssignedTo().isEmpty()) {
												for(Long assignToId: module.getAssignedTo()) {
													ProjectInsightAssignees newAssignObj = new ProjectInsightAssignees();
													
													newAssignObj.setAssignedTo(assignToId);
													newAssignObj.setEntityId(moduleId);
													newAssignObj.setEntityType("Module");
													
													ProjectInsightAssignees assignToDbResponse = projectInsightAssigneesRepository.save(newAssignObj);
												}
											}
										
										//Add Milestone question
										if(!module.getProjectQuestion().isEmpty()) {
											ServiceResponse moduleQuestionResponse = addUpdateQuestion(module.getProjectQuestion(),
													moduleId, "Module");
										}
										
										if(!module.getSubModuleList().isEmpty()) {
											module.getSubModuleList().forEach((submodule) -> {
												ProjectInsightSubModule newProjectInsightSubModule = new ProjectInsightSubModule();

												newProjectInsightSubModule.setCreatedBy(projectInsightDTO.getCreatedBy());
												newProjectInsightSubModule.setModuleId(moduleId);
												newProjectInsightSubModule.setDescription(submodule.getDescription());
												newProjectInsightSubModule.setRedmineId(submodule.getRedmineId());
												newProjectInsightSubModule.setSubmodule(submodule.getSubModule());
												
												ProjectInsightSubModule submoduleResponse = projectInsightSubModuleRepository.save(newProjectInsightSubModule);
												
												if(submoduleResponse != null) {
													Long submoduleId = submoduleResponse.getSubmoduleId();
													
													//Assign to
													if(!submodule.getAssignedTo().isEmpty()) {
														for(Long assignToId: submodule.getAssignedTo()) {
															ProjectInsightAssignees newAssignObj = new ProjectInsightAssignees();
															
															newAssignObj.setAssignedTo(assignToId);
															newAssignObj.setEntityId(submoduleId);
															newAssignObj.setEntityType("SubModule");
															
															ProjectInsightAssignees assignToDbResponse = projectInsightAssigneesRepository.save(newAssignObj);
														}
													}
													
													//Add Milestone question
													if(!submodule.getProjectQuestion().isEmpty()) {
														ServiceResponse submoduleQuestionResponse = addUpdateQuestion(submodule.getProjectQuestion(),
																submoduleId, "SubModule");
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
					}
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Project Insight updated successfully.");
				apiLogInfo.setApiResponse("Project Insight updated Successfully");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Project Insight Update Found.");
				apiLogInfo.setApiResponse("No Project Insight Update Found.");			
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

	public ServiceResponse getAllProjectInsightResponsesByProjectId(ProjectInsightDTO projectInsightDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getAllProjectInsightResponsesByProjectId");
		apiLogInfo.setApiUrl("/api/getAllProjectInsightResponsesByProjectId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("projectId : " + projectInsightDTO.getProjectId());
		try {

			if (projectInsightDTO.getProjectId() == null) {
				throw new IllegalArgumentException("Project Id cannot be null!!");
			}

			Project project = projectRepository.findByProjectId(Integer.parseInt(projectInsightDTO.getProjectId().toString()));
			if (project == null) {
				throw new RuntimeException("Project Not Found!!");
			}

			List<Object[]>  employeePersonaListForTeam = employeeTeamMapRepository.getEmployeePersonaForProject(projectInsightDTO.getEmpId(),projectInsightDTO.getProjectId());
			boolean isEmployee = checkIfIsEmployee(employeePersonaListForTeam,projectInsightDTO.getEmployeeRole());
			
			List<ProjectInsightMilestone> projectInsightMilestoneList = new ArrayList<>();
//			if (isEmployee) {
//				projectInsightMilestoneList = projectInsightMilestoneRepository.findByProjectIdAndAssignedMilestone(projectInsightDTO.getProjectId(),projectInsightDTO.getEmpId());
//			} else {
//				projectInsightMilestoneList = projectInsightMilestoneRepository.getByProjectId(projectInsightDTO.getProjectId());
//			}
			projectInsightMilestoneList = projectInsightMilestoneRepository.getByProjectId(projectInsightDTO.getProjectId());
			
			if (projectInsightMilestoneList != null && !projectInsightMilestoneList.isEmpty()) {
				ProjectInsightDTO projectInsightDTODbObject = createProjectInsightMileStoneObject(projectInsightMilestoneList,projectInsightDTO.getEmpId(),isEmployee);
				projectInsightDTODbObject.setProjectId(projectInsightDTO.getProjectId());
				projectInsightDTODbObject.setProjectManagerId(project.getProjectManagerId());
				response.setServiceResponse(projectInsightDTODbObject);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setApiResponse("All Project Insight Responses Fetched For Project!!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Milestone Found for Project.");
				apiLogInfo.setApiResponse("No Milestone Found for Project.");
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

	private boolean checkIfIsEmployee(List<Object[]> employeePersonaListForTeam,String employeeRole) {
		boolean flag = true;
		try {
			if(employeeRole != null && !employeeRole.trim().equals("")
				&& (employeeRole.toLowerCase().contains("hod")
								|| employeeRole.toLowerCase().contains("teamlead")
								|| employeeRole.toLowerCase().contains("manager")
								|| employeeRole.toLowerCase().contains("superadmin"))
						|| employeeRole.toLowerCase().contains("rmg")) {
					return false;
			}
			
			if (employeePersonaListForTeam != null && !employeePersonaListForTeam.isEmpty()) {
				for (Object[] objectArray : employeePersonaListForTeam) {
					if (objectArray[1] != null && !objectArray[1].toString().trim().equals("")
							&& (objectArray[1].toString().toLowerCase().contains("hod")
									|| objectArray[1].toString().toLowerCase().contains("teamlead")
									|| objectArray[1].toString().toLowerCase().contains("manager")
									|| objectArray[1].toString().toLowerCase().contains("superadmin"))
							|| objectArray[1].toString().toLowerCase().contains("rmg")) {
						return false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
		return flag;
	}

	private ProjectInsightDTO createProjectInsightMileStoneObject(List<ProjectInsightMilestone> projectInsightMilestoneList,Long employeeId,boolean isEmployee) {
		ProjectInsightDTO projectInsightDTO = new ProjectInsightDTO();
		try {
			if (projectInsightMilestoneList != null && !projectInsightMilestoneList.isEmpty()) {
				List<ProjectInsightQuestionDTO> projectInsightQuestionDTOList = new ArrayList<>();
				for (ProjectInsightMilestone projectInsightMilestone : projectInsightMilestoneList) {
					ProjectInsightQuestionDTO projectInsightQuestionDTO = mapProjectMilestoneToDTO(projectInsightMilestone, employeeId, isEmployee);

					if ((projectInsightQuestionDTO.getModuleList() != null && !projectInsightQuestionDTO.getModuleList().isEmpty())
							|| (projectInsightQuestionDTO.getProjectQuestion() != null && !projectInsightQuestionDTO.getProjectQuestion().isEmpty())) {
						projectInsightQuestionDTOList.add(projectInsightQuestionDTO);
					}

				}
				projectInsightDTO.setProjectInsightQuestionList(projectInsightQuestionDTOList);
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return projectInsightDTO;
	}
	
	private ProjectInsightQuestionDTO mapProjectMilestoneToDTO(ProjectInsightMilestone projectInsightMilestone,Long employeeId,boolean isEmployee) {
		try {
			ProjectInsightQuestionDTO projectInsightQuestionDTO = new ProjectInsightQuestionDTO();
			projectInsightQuestionDTO.setMilestoneId(projectInsightMilestone.getMilestoneId());
			projectInsightQuestionDTO.setMilestone(projectInsightMilestone.getMilestone());
			projectInsightQuestionDTO.setDescription(projectInsightMilestone.getDescription());
			projectInsightQuestionDTO.setDeptId(projectInsightMilestone.getDeptId());
			projectInsightQuestionDTO.setAssignedToUserNames(getAssignedToUserName(projectInsightMilestone.getMilestoneId(),"Milestone"));
			projectInsightQuestionDTO.setProjectQuestion(getProjectQuestionDTOList(projectInsightMilestone.getMilestoneId(), "Milestone",employeeId,isEmployee));
			projectInsightQuestionDTO.setModuleList(getProjectInsightModuleDTOList(projectInsightMilestone.getMilestoneId(),employeeId,isEmployee));
			return projectInsightQuestionDTO;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private String getAssignedToUserName(Long entityId, String entityType) {
		String assignedTo = null;
		try {
			List<String> usernameList = projectInsightAssigneesRepository.getUserNameByEntityIdAndEntityType(entityId,entityType);
			assignedTo = usernameList != null
					? usernameList.stream().map(String::valueOf).collect(Collectors.joining(", "))
					: null;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return assignedTo;
	}

	private List<ModuleDTO> getProjectInsightModuleDTOList(Long milestoneId,Long employeeId,boolean isEmployee) {
		List<ModuleDTO> projectInsightModuleDTOList = new ArrayList<>();
		try {
			List<ProjectInsightModule> projectInsightModuleList =  new ArrayList<>();
//			if (isEmployee) {
//				projectInsightModuleList = projectInsightModuleRepository.findByMilestoneIdAndAssignedModule(milestoneId, employeeId);
//			} else {
//				projectInsightModuleList =  projectInsightModuleRepository.findByMilestoneId(milestoneId);
//			}
			
			projectInsightModuleList =  projectInsightModuleRepository.findByMilestoneId(milestoneId);
			if (!projectInsightModuleList.isEmpty()) {
				for (ProjectInsightModule projectInsightModule : projectInsightModuleList) {
					ModuleDTO moduleDTO = new ModuleDTO();
					moduleDTO.setModuleId(projectInsightModule.getModuleId());
					moduleDTO.setMilestoneId(projectInsightModule.getMilestoneId());
					moduleDTO.setModule(projectInsightModule.getModule());
					moduleDTO.setDescription(projectInsightModule.getDescription());
					moduleDTO.setRedmineId(projectInsightModule.getRedmineId());
					moduleDTO.setAssignedToUserNames(getAssignedToUserName(projectInsightModule.getModuleId(),"Module"));
					moduleDTO.setProjectQuestion(getProjectQuestionDTOList(projectInsightModule.getModuleId(), "Module",employeeId,isEmployee));
					moduleDTO.setSubModuleList(getProjectInsightSubModuleDTOList(projectInsightModule.getModuleId(),employeeId,isEmployee));
					if((moduleDTO.getSubModuleList() != null && !moduleDTO.getSubModuleList().isEmpty()) || (moduleDTO.getProjectQuestion() != null && !moduleDTO.getProjectQuestion().isEmpty())) {
					projectInsightModuleDTOList.add(moduleDTO);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return projectInsightModuleDTOList;
	}

	private List<SubModuleDTO> getProjectInsightSubModuleDTOList(Long moduleId,Long employeeId,boolean isEmployee) {
		List<SubModuleDTO> projectInsightSubModuleDTOList = new ArrayList<>();
		try {
			List<ProjectInsightSubModule> projectInsightSubModuleList = new ArrayList<>();
//			if (isEmployee) {
//				projectInsightSubModuleList = projectInsightSubModuleRepository.findByModuleIdAndAssignedSubModule(moduleId, employeeId);
//			} else {
//				projectInsightSubModuleList = projectInsightSubModuleRepository.findByModuleId(moduleId);
//			}
			
			projectInsightSubModuleList = projectInsightSubModuleRepository.findByModuleId(moduleId);
			if (!projectInsightSubModuleList.isEmpty()) {
				for (ProjectInsightSubModule projectInsightSubModule : projectInsightSubModuleList) {
					SubModuleDTO subModuleDTO = new SubModuleDTO();
					subModuleDTO.setSubmoduleId(projectInsightSubModule.getSubmoduleId());
					subModuleDTO.setSubModule(projectInsightSubModule.getSubmodule());
					subModuleDTO.setModuleId(projectInsightSubModule.getModuleId());
					subModuleDTO.setDescription(projectInsightSubModule.getDescription());
					subModuleDTO.setRedmineId(projectInsightSubModule.getRedmineId());
					subModuleDTO.setAssignedToUserNames(getAssignedToUserName(projectInsightSubModule.getSubmoduleId(),"SubModule"));
					subModuleDTO.setProjectQuestion(getProjectQuestionDTOList(projectInsightSubModule.getSubmoduleId(), "SubModule",employeeId,isEmployee));
					if(subModuleDTO.getProjectQuestion() != null && !subModuleDTO.getProjectQuestion().isEmpty()) {
						projectInsightSubModuleDTOList.add(subModuleDTO);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return projectInsightSubModuleDTOList;
	}

	public List<ProjectQuestionDTO> getProjectQuestionDTOList(Long entityId, String entityType,Long employeeId,boolean isEmployee) {
		List<ProjectQuestionDTO> projectInsightQuestionList = new ArrayList<>();
		try {
			List<QuestionMaster> projectInsightQuestionMasterList = new ArrayList<>();
			if(isEmployee) {
				projectInsightQuestionMasterList = questionMasterRepository.findByEntityIdAndEntityTypeAndAssignedTo(entityId,entityType,employeeId);
			} else {
				projectInsightQuestionMasterList = questionMasterRepository.findByEntityIdAndEntityType(entityId,entityType);
			}
			if (!projectInsightQuestionMasterList.isEmpty()) {
				for (QuestionMaster questionMaster : projectInsightQuestionMasterList) {
					ProjectQuestionDTO projectQuestionDTO = new ProjectQuestionDTO();
					projectQuestionDTO.setQuestionId(questionMaster.getQuestionMasterId());
					projectQuestionDTO.setEntityId(questionMaster.getEntityId());
					projectQuestionDTO.setEntityType(questionMaster.getEntityType());
					projectQuestionDTO.setQuestion(questionMaster.getQuestion());
					projectQuestionDTO.setDescription(questionMaster.getDescription());
					projectQuestionDTO.setOptionType(questionMaster.getOptionType());
					projectQuestionDTO.setOptions(questionMaster.getOptions());
					projectQuestionDTO.setRequired(questionMaster.getRequired());
					projectQuestionDTO.setDocumentUpload(questionMaster.getDocumentUpload());
					
					boolean flag = true;
					List<ProjectInsightResponse> projectInsightResponseList = new ArrayList<>();
					List<ProjectResponseDTO> projectResponseDTOList = new ArrayList<>();
					if(flag) { // here will check for if preview is of all the team members
						projectInsightResponseList = projectInsightResponseRepository.findAllByQuestionMasterId(questionMaster.getQuestionMasterId());
					} else {
						projectInsightResponseList = projectInsightResponseRepository.findByQuestionMasterIdAndEmpId(questionMaster.getQuestionMasterId(), employeeId);
					}
					if(projectInsightResponseList != null && !projectInsightResponseList.isEmpty()) {
						for(ProjectInsightResponse projectInsightResponse :projectInsightResponseList) {
							ProjectResponseDTO projectResponseDTO = new ProjectResponseDTO();
							projectResponseDTO.setResponseId(projectInsightResponse.getProjectInsightResponseId());
							projectResponseDTO.setResponse(projectInsightResponse.getResponse());
							projectResponseDTO.setDocumentPath(projectInsightResponse.getDocumentPath());
							projectResponseDTO.setUploadedFileName(projectInsightResponse.getDocumentFileName());
							projectResponseDTOList.add(projectResponseDTO);
						}
					}
					projectQuestionDTO.setProjectResponseList(projectResponseDTOList);
					projectInsightQuestionList.add(projectQuestionDTO);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return projectInsightQuestionList;
	}

	public ServiceResponse saveProjectInsightResponse(ProjectInsightDTO projectInsightDTO,List<MultipartFile> files) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("saveProjectInsightResponse");
		apiLogInfo.setApiUrl("/api/saveProjectInsightResponse");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("projectId : " + projectInsightDTO.getProjectId());
		try {

			if (projectInsightDTO.getProjectId() == null) {
				throw new IllegalArgumentException("Project Id cannot be null!!");
			}

			Project project = projectRepository.findByProjectId(Integer.parseInt(projectInsightDTO.getProjectId().toString()));
			if (project == null) {
				throw new RuntimeException("Project Not Found!!");
			}
			
			if (projectInsightDTO.getProjectInsightQuestionList() != null && !projectInsightDTO.getProjectInsightQuestionList().isEmpty()) {
				saveProjectMileStoneResponse(projectInsightDTO.getProjectInsightQuestionList(),projectInsightDTO.getEmpId(),files);
				apiLogInfo.setApiResponse("Project Insight Response Saved Successfully");
				response.setServiceResponse("Project Insight Response Saved Successfully");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				apiLogInfo.setApiResponse("No Milestone Found for Project.");
				response.setServiceResponse("No Milestone Found for Project.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
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

	private String saveProjectMileStoneResponse(List<ProjectInsightQuestionDTO> projectInsightQuestionDTOList,
			Long employeeId,List<MultipartFile> files) {
		String response = null;
		try {
			if (projectInsightQuestionDTOList != null && !projectInsightQuestionDTOList.isEmpty()) {
				for (ProjectInsightQuestionDTO projectInsightQuestionDTO : projectInsightQuestionDTOList) {
					saveProjectInsightResponse(projectInsightQuestionDTO.getProjectQuestion(), employeeId,files);
					saveProjectInsightModuleResponse(projectInsightQuestionDTO.getModuleList(), employeeId,files);
				}
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return response;
	}

	private void saveProjectInsightModuleResponse(List<ModuleDTO> moduleList, Long employeeId,List<MultipartFile> files) {
		try {
			if (moduleList != null && !moduleList.isEmpty()) {
				for (ModuleDTO moduleDTO : moduleList) {
					saveProjectInsightResponse(moduleDTO.getProjectQuestion(), employeeId,files);
					saveProjectInsightSubModuleResponse(moduleDTO.getSubModuleList(), employeeId,files);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void saveProjectInsightSubModuleResponse(List<SubModuleDTO> subModuleList, Long employeeId,List<MultipartFile> files) {
		try {
			if (subModuleList != null && !subModuleList.isEmpty()) {
				for (SubModuleDTO subModuleDTO : subModuleList) {
					saveProjectInsightResponse(subModuleDTO.getProjectQuestion(), employeeId,files);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void saveProjectInsightResponse(List<ProjectQuestionDTO> projectQuestionList, Long employeeId,List<MultipartFile> files) {
		try {
			if (projectQuestionList != null && !projectQuestionList.isEmpty()) {
				for (ProjectQuestionDTO projectQuestionDTO : projectQuestionList) {
					if (projectQuestionDTO.getQuestionId() != null && projectQuestionDTO.getProjectResponseList() != null && !projectQuestionDTO.getProjectResponseList().isEmpty()) {
						
						for(ProjectResponseDTO projectResponseDTO : projectQuestionDTO.getProjectResponseList()) {
							List<ProjectInsightResponse> projectInsightResponseList = projectInsightResponseRepository.findByQuestionMasterIdAndEmpId(projectQuestionDTO.getQuestionId(), projectResponseDTO.getResponseByEmpId());
							for(ProjectInsightResponse projectInsightResponse: projectInsightResponseList){
								if (projectInsightResponse != null) {
									projectInsightResponse.setResponse(projectResponseDTO.getResponse());
									projectInsightResponse.setUpdatedBy(projectResponseDTO.getResponseByEmpId());
									
								} else {
									projectInsightResponse = new ProjectInsightResponse();
									projectInsightResponse.setResponse(projectResponseDTO.getResponse());
									projectInsightResponse.setEmpId(projectResponseDTO.getResponseByEmpId());
									projectInsightResponse.setDocumentPath(projectResponseDTO.getDocumentPath());
									projectInsightResponse.setQuestionMasterId(projectQuestionDTO.getQuestionId());
								}
								if (files != null) {
									for (MultipartFile document : files) {
										if (document != null &&  document.getOriginalFilename() != null && document.getOriginalFilename().equals(projectResponseDTO.getUploadedFileName())) {
											String uploadResponse = uploadProjectResponseDocument(projectQuestionDTO, document);
											if(uploadResponse.equalsIgnoreCase("Document uploaded successfully")) {
												projectInsightResponse.setDocumentPath(projectResponseDTO.getDocumentPath());
												projectInsightResponse.setDocumentFileName(projectResponseDTO.getUploadedFileName());
											}
										}
									}
								}
								projectInsightResponseRepository.save(projectInsightResponse);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public ServiceResponse getProjectResponseDocument(ProjectInsightDTO projectInsightDTO) {
		try {
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	
	
	public String uploadProjectResponseDocument2(ProjectQuestionDTO projectQuestionDTO,MultipartFile document) {
		try {
			if(document != null) {
				String dmsPortalUploadUrl = dmsPortalUrl +"/upload.php";
				RestTemplate restTemplate = new RestTemplate();
				String syncResponse = restTemplate.postForObject(dmsPortalUploadUrl, document, String.class);
				JSONObject json = new JSONObject(syncResponse);
				System.out.println(json.toString());
			}
		} catch (Exception e) {
			 e.printStackTrace();
			 throw e;
		}
		return null;
	}
	
	public String uploadProjectResponseDocument(ProjectQuestionDTO projectQuestionDTO, MultipartFile document) {
		String response = null;
	    try {
	        if (document != null) {
	            String dmsPortalUploadUrl = dmsPortalUrl + "/upload.php";
	            RestTemplate restTemplate = new RestTemplate();

	            // Create HttpHeaders for multipart request
	            HttpHeaders headers = new HttpHeaders();
	            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
	            headers.set("X-API-KEY", dmsPortalUrlKey);
	            // Create MultipartBody using LinkedMultiValueMap
	            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
	            body.add("document", new ByteArrayResource(document.getBytes()) {
	                @Override
	                public String getFilename() {
	                    return document.getOriginalFilename(); // Required to set file name
	                }
	            });

	            // Wrap in HttpEntity
	            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

	            // Send request
	            String syncResponse = restTemplate.postForObject(dmsPortalUploadUrl, requestEntity, String.class);

	            // Parse response
	            JSONObject json = new JSONObject(syncResponse);
	            if(json != null) {
	            	String obj = json.getString("message");
	            	if(obj != null) {
	            		response= obj;
	            	}
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        response="Failed to Upload Document.";
	    }
	    return response;
	}

}
