package com.apmosys.employeeportal.service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.apmosys.employeeportal.dto.EmployeeDocumentDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.ModuleDTO;
import com.apmosys.employeeportal.dto.ProjectInsightDTO;
import com.apmosys.employeeportal.dto.ProjectInsightQuestionDTO;
import com.apmosys.employeeportal.dto.ProjectQuestionDTO;
import com.apmosys.employeeportal.dto.ProjectResponseDTO;
import com.apmosys.employeeportal.dto.SubModuleDTO;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeTeamMap;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectInsightAssignees;
import com.apmosys.employeeportal.model.ProjectInsightMilestone;
import com.apmosys.employeeportal.model.ProjectInsightModule;
import com.apmosys.employeeportal.model.ProjectInsightResponse;
import com.apmosys.employeeportal.model.ProjectInsightSubModule;
import com.apmosys.employeeportal.model.QuestionMaster;
import com.apmosys.employeeportal.model.TagMaster;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.ProjectInsightAssigneesRepository;
import com.apmosys.employeeportal.repository.ProjectInsightMilestoneRepository;
import com.apmosys.employeeportal.repository.ProjectInsightModuleRepository;
import com.apmosys.employeeportal.repository.ProjectInsightResponseRepository;
import com.apmosys.employeeportal.repository.ProjectInsightSubModuleRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.QuestionMasterRepository;
import com.apmosys.employeeportal.repository.TagMasterRepository;
import com.apmosys.employeeportal.utility.NLPUtils;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.TagSpecifications;

@Service
public class ProjectInsightService {
	
	private static final List<String> ROLE_HIERARCHY = Arrays.asList("EMPLOYEE", "TEAMLEAD", "MANAGER", "HOD", "SUPERADMIN", "RMG", "HR");
	
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

	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Value("${dmsPortalUrl}")
	private String dmsPortalUrl;
	
	@Value("${dmsPortalUploadUrlKey}")
	private String dmsPortalUploadUrlKey;
	
	@Value("${dmsPortalFetchUrlKey}")
	private String dmsPortalFetchUrlKey;
	
	@Autowired
	NLPUtils nlpUtils;
	
	@Autowired
	TagMasterRepository tagMasterRepository;
	
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
	
	public String saveProjectWiseTags(ProjectInsightDTO projectInsightDTO) {
	    String response = "Failed";

	    try {
	        Long projectId = projectInsightDTO.getProjectId();
	        String tagType = projectInsightDTO.getTagType();
	        String projectText = projectInsightDTO.getProjectText().toString();

	        if (projectId == null || projectText == null || projectText.isEmpty() || tagType == null) {
	            return "Invalid Project ID or Text or TagType";
	        }

	        List<String> extractedTags = nlpUtils.extractTags(projectText);
	        
	        List<TagMaster> existingTags = tagMasterRepository.findByProjectId(projectId);
	        Set<String> existingTagNames = existingTags.stream()
	        											.map(TagMaster::getTag)
	        											.collect(Collectors.toSet());

	        if ("response".equalsIgnoreCase(tagType)) {
	            extractedTags.removeIf(existingTagNames::contains);
	        } else {
	            tagMasterRepository.deleteAll(existingTags);
	        }

	        // Save new tags if any remain
	        if (!extractedTags.isEmpty()) {
	            List<TagMaster> newTags = extractedTags.stream()
	                .map(tag -> {
	                    TagMaster tm = new TagMaster();
	                    tm.setProjectId(projectId);
	                    tm.setTag(tag);
	                    return tm;
	                })
	                .collect(Collectors.toList());

	            List<TagMaster> savedTags = tagMasterRepository.saveAll(newTags);
	            if (!savedTags.isEmpty()) {
	                response = "Success";
	            }else {
	            	response = "Failed";
	            }
	        } else {
	            response = "No New Tags to Save";
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
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
        StringBuilder combinedText = new StringBuilder();

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
						
						//Add Application Question
						if(!projectInsightDTO.getApplicationQuestion().isEmpty()) {
							ServiceResponse applicationQuestionResponse = addUpdateQuestion(projectInsightDTO.getApplicationQuestion(),
									projectInsightDTO.getProjectId(), "Project");
							
							if ("Success".equals(applicationQuestionResponse.getServiceStatus())) {
				                isSuccess.set(true);
				            } else {
				                isSuccess.set(false);
				            }
								
						}
						
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
					
					//ApplciationQuestionList
					responseObject.setApplicationQuestion(getQuestion(projectInsightDTO.getProjectId(), "Project"));
					
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
			
			//add/update milestone question
			if(!projectInsightDTO.getApplicationQuestion().isEmpty()) {
				ServiceResponse applicationQuestionResponse = addUpdateQuestion(projectInsightDTO.getApplicationQuestion(),
						projectInsightDTO.getProjectId(), "Project");		
			}

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
														newAssignObj.setEntityId(submoduleObject.getSubmoduleId());
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
	
//	----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

	public ServiceResponse getAllProjectInsightResponsesByProjectId(ProjectInsightDTO projectInsightDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setSubFeatureName("getAllProjectInsightResponsesByProjectId");
		apiLogInfo.setApiUrl("/api/getAllProjectInsightResponsesByProjectId");
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
			
			List<ProjectInsightMilestone> projectInsightMilestoneList =  projectInsightMilestoneRepository.getByProjectId(projectInsightDTO.getProjectId());
			if (projectInsightMilestoneList != null && !projectInsightMilestoneList.isEmpty()) {
				ProjectInsightDTO projectInsightDTODbObject = createProjectInsightMileStoneObject(projectInsightMilestoneList,projectInsightDTO.getEmpId(),isEmployee,projectInsightDTO.getPerformanceTabName(),projectInsightDTO.getProjectId());
				projectInsightDTODbObject.setApplicationQuestion(getProjectQuestionDTOList(projectInsightDTO.getProjectId(), "Project",projectInsightDTO.getEmpId(), isEmployee, projectInsightDTO.getPerformanceTabName(), projectInsightDTO.getProjectId()));
				projectInsightDTODbObject.setTaggedToUserNames(getAllTaggedUserName(projectInsightDTO.getProjectId(), "Project",projectInsightDTO.getEmpId()));
				projectInsightDTODbObject.setTaggedForHelp(getAllTaggedUserId(projectInsightDTO.getProjectId(), "Project",projectInsightDTO.getEmpId()));
				projectInsightDTODbObject.setProjectId(projectInsightDTO.getProjectId());
				projectInsightDTODbObject.setProjectManagerId(project.getProjectManagerId());
				projectInsightDTODbObject.setProjectName(project.getProjectName());
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

	private ProjectInsightDTO createProjectInsightMileStoneObject(List<ProjectInsightMilestone> projectInsightMilestoneList, Long employeeId, boolean isEmployee, String performanceTabName, Long projectId) {
		ProjectInsightDTO projectInsightDTO = new ProjectInsightDTO();
		try {
			if (projectInsightMilestoneList != null && !projectInsightMilestoneList.isEmpty()) {
				List<ProjectInsightQuestionDTO> projectInsightQuestionDTOList = new ArrayList<>();
				for (ProjectInsightMilestone projectInsightMilestone : projectInsightMilestoneList) {
					ProjectInsightQuestionDTO projectInsightQuestionDTO = mapProjectMilestoneToDTO(projectInsightMilestone, employeeId, isEmployee, performanceTabName, projectId);
					if ((projectInsightQuestionDTO.getModuleList() != null && !projectInsightQuestionDTO.getModuleList().isEmpty()) 
							|| (projectInsightQuestionDTO.getProjectQuestion() != null && !projectInsightQuestionDTO.getProjectQuestion().isEmpty())) {
						projectInsightQuestionDTOList.add(projectInsightQuestionDTO);
					}
				}
				projectInsightDTO.setProjectInsightQuestionList(projectInsightQuestionDTOList);
			} 
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return projectInsightDTO;
	}
	
	private ProjectInsightQuestionDTO mapProjectMilestoneToDTO(ProjectInsightMilestone projectInsightMilestone, Long employeeId, boolean isEmployee, String performanceTabName, Long projectId) {
		try {
			ProjectInsightQuestionDTO projectInsightQuestionDTO = new ProjectInsightQuestionDTO();
			projectInsightQuestionDTO.setMilestoneId(projectInsightMilestone.getMilestoneId());
			projectInsightQuestionDTO.setMilestone(projectInsightMilestone.getMilestone());
			projectInsightQuestionDTO.setDescription(projectInsightMilestone.getDescription());
			projectInsightQuestionDTO.setDeptId(projectInsightMilestone.getDeptId());
			projectInsightQuestionDTO.setAssignedToUserNames(getAssignedToUserName(projectInsightMilestone.getMilestoneId(), "Milestone"));
			projectInsightQuestionDTO.setTaggedToUserNames(getAllTaggedUserName(projectInsightMilestone.getMilestoneId(), "Milestone",employeeId));
			projectInsightQuestionDTO.setTaggedForHelp(getAllTaggedUserId(projectInsightMilestone.getMilestoneId(), "Milestone",employeeId));
			projectInsightQuestionDTO.setProjectQuestion(getProjectQuestionDTOList(projectInsightMilestone.getMilestoneId(), "Milestone",employeeId, isEmployee, performanceTabName, projectId));
			projectInsightQuestionDTO.setModuleList(getProjectInsightModuleDTOList(projectInsightMilestone.getMilestoneId(), employeeId, isEmployee, performanceTabName, projectId));
			return projectInsightQuestionDTO;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private List<ModuleDTO> getProjectInsightModuleDTOList(Long milestoneId, Long employeeId, boolean isEmployee, String performanceTabName, Long projectId) {
		List<ModuleDTO> projectInsightModuleDTOList = new ArrayList<>();
		try {
			List<ProjectInsightModule> projectInsightModuleList = projectInsightModuleRepository.findByMilestoneId(milestoneId);
			if (!projectInsightModuleList.isEmpty()) {
				for (ProjectInsightModule projectInsightModule : projectInsightModuleList) {
					ModuleDTO moduleDTO = new ModuleDTO();
					moduleDTO.setModuleId(projectInsightModule.getModuleId());
					moduleDTO.setMilestoneId(projectInsightModule.getMilestoneId());
					moduleDTO.setModule(projectInsightModule.getModule());
					moduleDTO.setDescription(projectInsightModule.getDescription());
					moduleDTO.setRedmineId(projectInsightModule.getRedmineId());
					moduleDTO.setAssignedToUserNames(getAssignedToUserName(projectInsightModule.getModuleId(), "Module"));
					moduleDTO.setTaggedToUserNames(getAllTaggedUserName(projectInsightModule.getModuleId(), "Module",employeeId));
					moduleDTO.setTaggedForHelp(getAllTaggedUserId(projectInsightModule.getModuleId(), "Module",employeeId));
					moduleDTO.setProjectQuestion(getProjectQuestionDTOList(projectInsightModule.getModuleId(), "Module", employeeId, isEmployee, performanceTabName, projectId));
					moduleDTO.setSubModuleList(getProjectInsightSubModuleDTOList(projectInsightModule.getModuleId(), employeeId, isEmployee, performanceTabName, projectId));
					if ((moduleDTO.getSubModuleList() != null && !moduleDTO.getSubModuleList().isEmpty()) 
							|| (moduleDTO.getProjectQuestion() != null && !moduleDTO.getProjectQuestion().isEmpty())) {
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

	private List<SubModuleDTO> getProjectInsightSubModuleDTOList(Long moduleId, Long employeeId, boolean isEmployee, String performanceTabName, Long projectId) {
		List<SubModuleDTO> projectInsightSubModuleDTOList = new ArrayList<>();
		try {
			List<ProjectInsightSubModule> projectInsightSubModuleList = projectInsightSubModuleRepository.findByModuleId(moduleId);
			if (!projectInsightSubModuleList.isEmpty()) {
				for (ProjectInsightSubModule projectInsightSubModule : projectInsightSubModuleList) {
					SubModuleDTO subModuleDTO = new SubModuleDTO();
					subModuleDTO.setSubmoduleId(projectInsightSubModule.getSubmoduleId());
					subModuleDTO.setSubModule(projectInsightSubModule.getSubmodule());
					subModuleDTO.setModuleId(projectInsightSubModule.getModuleId());
					subModuleDTO.setDescription(projectInsightSubModule.getDescription());
					subModuleDTO.setRedmineId(projectInsightSubModule.getRedmineId());
					subModuleDTO.setAssignedToUserNames(getAssignedToUserName(projectInsightSubModule.getSubmoduleId(), "SubModule"));
					subModuleDTO.setTaggedToUserNames(getAllTaggedUserName(projectInsightSubModule.getSubmoduleId(), "SubModule",employeeId));
					subModuleDTO.setTaggedForHelp(getAllTaggedUserId(projectInsightSubModule.getSubmoduleId(), "SubModule",employeeId));
					subModuleDTO.setProjectQuestion(getProjectQuestionDTOList(projectInsightSubModule.getSubmoduleId(), "SubModule", employeeId, isEmployee, performanceTabName, projectId));
					if (subModuleDTO.getProjectQuestion() != null && !subModuleDTO.getProjectQuestion().isEmpty()) {
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

	public List<ProjectQuestionDTO> getProjectQuestionDTOList(Long entityId, String entityType, Long employeeId, boolean isEmployee, String performanceTabName, Long projectId) {
		List<ProjectQuestionDTO> projectInsightQuestionList = new ArrayList<>();
		try {
			List<QuestionMaster> projectInsightQuestionMasterList = new ArrayList<>();
			if (isEmployee) {
				projectInsightQuestionMasterList = questionMasterRepository.findByEntityIdAndEntityTypeAndAssignedTo(entityId, entityType, employeeId);
			} else {
				projectInsightQuestionMasterList = questionMasterRepository.findByEntityIdAndEntityType(entityId, entityType);
			}
			if (!projectInsightQuestionMasterList.isEmpty()) {
				for (QuestionMaster questionMaster : projectInsightQuestionMasterList) {
					ProjectQuestionDTO projectQuestionDTO = mapQuestionMasterToProjectQuestionDTO(questionMaster);
					List<ProjectResponseDTO> projectResponseDTOList = new ArrayList<>();
					List<ProjectInsightResponse> projectInsightResponseList = getProjectInsightResponseListAsPerEmployeeAndQuestion(employeeId, performanceTabName, projectId,questionMaster.getQuestionMasterId());
					List<ProjectInsightResponse> projectInsightResponseList2 = getAllTaggedQuestionsResponse(entityId, entityType,  employeeId,questionMaster.getQuestionMasterId());
					if(projectInsightResponseList2 != null && !projectInsightResponseList2.isEmpty()) {
						for (ProjectInsightResponse projectInsightResponse2 : projectInsightResponseList2) {
							if(questionMaster.getQuestionMasterId().equals(projectInsightResponse2.getQuestionMasterId())) {
								projectQuestionDTO.setTagged(true);
							}
						}
					}
					projectInsightResponseList.addAll(projectInsightResponseList2);
					
					if (projectInsightResponseList != null && !projectInsightResponseList.isEmpty()) {
						for (ProjectInsightResponse projectInsightResponse : projectInsightResponseList) {
							ProjectResponseDTO projectResponseDTO = mapPojectInsightResponseToProjectInsightResponseDTO(projectInsightResponse,questionMaster.getOptions());
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
	
	private List<ProjectInsightResponse> getAllTaggedQuestionsResponse(Long entityId, String entityType, Long employeeId,Long questionMasterId) {
		List<ProjectInsightResponse> projectInsightTaggedResponseList = new ArrayList<>();
		try {		
			List<ProjectInsightAssignees> projectInsightAssigneesList = projectInsightAssigneesRepository.getProjectInsightAssigneesByEntityIdAndEntityTypeAndHelpTaggedBy(entityId, entityType, employeeId);
			if (projectInsightAssigneesList != null && !projectInsightAssigneesList.isEmpty()) {
				for(ProjectInsightAssignees projectInsightAssignees : projectInsightAssigneesList) {
					ProjectInsightResponse projectInsightResponse = projectInsightResponseRepository.findByEmpIdAndQuestionMasterId(projectInsightAssignees.getAssigneeId(), questionMasterId);
					if(projectInsightResponse != null) {
						projectInsightResponse.setResponseType("Tagged");
						projectInsightTaggedResponseList.add(projectInsightResponse);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return projectInsightTaggedResponseList;
	}

	private ProjectQuestionDTO mapQuestionMasterToProjectQuestionDTO(QuestionMaster questionMaster) {
		try {
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
			return projectQuestionDTO;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private List<ProjectInsightResponse> getProjectInsightResponseListAsPerEmployeeAndQuestion(Long employeeId, String performanceTabName, Long projectId, Long questionMasterId) {
		List<ProjectInsightResponse> projectInsightResponseList = new ArrayList<>();
		try {
			if (!performanceTabName.equals("Team Dashboard")) {
				return projectInsightResponseRepository.findByQuestionMasterIdAndEmpId(questionMasterId, employeeId);
			}
			List<EmployeeTeamMap> employeeTeamList = employeeTeamMapRepository.findByProjectIdAndActive(Integer.parseInt(projectId.toString()), 1l);

			if (employeeTeamList == null || !employeeTeamList.isEmpty()) {
				return projectInsightResponseRepository.findByQuestionMasterIdAndEmpId(questionMasterId, employeeId);
			}
			Map<Long, String> employeeTeamMapObj = getEmpIdToHighestRoleMap(employeeTeamList);
			String employeeRole = employeeTeamMapObj.getOrDefault(employeeId, null);

			if (employeeRole != null && (employeeRole.equals("SUPERADMIN") || employeeRole.equals("RMG") || employeeRole.equals("HR"))) {
				projectInsightResponseList = projectInsightResponseRepository.findAllByQuestionMasterId(questionMasterId);
			} else if (employeeRole != null && !employeeRole.equals("SUPERADMIN") && !employeeRole.equals("RMG") && !employeeRole.equals("HR")) {
				List<Long> seniorEmployeeIdList = getSeniorEmployeeList(employeeTeamMapObj, employeeRole);
				if (seniorEmployeeIdList.isEmpty()) {
					seniorEmployeeIdList.add(1l);
				}
				projectInsightResponseList = projectInsightResponseRepository.findAllByQuestionMasterIdAndEmpIdListNotIn(questionMasterId, seniorEmployeeIdList);
			} else {
				projectInsightResponseList = projectInsightResponseRepository.findByQuestionMasterIdAndEmpId(questionMasterId, employeeId);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return projectInsightResponseList;
	}
	
	private List<Long> getSeniorEmployeeList(Map<Long, String> employeeTeamMapObj, String employeeRole) {
		List<Long> seniorEmployeeList = new ArrayList<>();
		try {
			if (employeeRole != null && ROLE_HIERARCHY.indexOf(employeeRole) + 1 < ROLE_HIERARCHY.size()) {
				int roleIndex = ROLE_HIERARCHY.indexOf(employeeRole) + 1;
				for (int i = roleIndex; i < ROLE_HIERARCHY.size(); i++) {
					String nextLevelRole = ROLE_HIERARCHY.get(i);
					for (Map.Entry<Long, String> entry : employeeTeamMapObj.entrySet()) {
						if (entry.getValue() != null && entry.getValue().equalsIgnoreCase(nextLevelRole)) {
							seniorEmployeeList.add(entry.getKey());
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return seniorEmployeeList;
	}

	private ProjectResponseDTO mapPojectInsightResponseToProjectInsightResponseDTO(ProjectInsightResponse projectInsightResponse, String options) {
		try {
			ProjectResponseDTO projectResponseDTO = new ProjectResponseDTO();
			projectResponseDTO.setResponseId(projectInsightResponse.getProjectInsightResponseId());
			projectResponseDTO.setResponse(projectInsightResponse.getResponse());
			projectResponseDTO.setDocumentPath(projectInsightResponse.getDocumentPath());
			projectResponseDTO.setOptions(options);
			projectResponseDTO.setUploadedFileName(projectInsightResponse.getDocumentFileName());
			projectResponseDTO.setResponseByEmpId(projectInsightResponse.getEmpId());
			Employee employee = employeeRepository.findByEmpId(projectInsightResponse.getEmpId());
			projectResponseDTO.setResponseByEmpName(employee != null ? employee.getName() : "");
			projectResponseDTO.setIsDraft(projectInsightResponse.getIsDraft());
			projectResponseDTO.setProcessTo(projectInsightResponse.getProcessTo());
			projectResponseDTO.setMarks(projectInsightResponse.getMarks());
			projectResponseDTO.setResponseType(projectInsightResponse.getResponseType());
			return projectResponseDTO;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public ServiceResponse saveProjectInsightResponse(ProjectInsightDTO projectInsightDTO, List<MultipartFile> files) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setSubFeatureName("saveProjectInsightResponse");
		apiLogInfo.setApiUrl("/api/saveProjectInsightResponse");
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

			Long reviewerId = getReviewerIdForQuestion(projectInsightDTO.getProjectId(), projectInsightDTO.getEmpId());
			if (projectInsightDTO.getProjectInsightQuestionList() != null && !projectInsightDTO.getProjectInsightQuestionList().isEmpty()) {
				saveProjectResponse(projectInsightDTO.getApplicationQuestion(), projectInsightDTO.getEmpId(), files, reviewerId,projectInsightDTO.getProjectId());
				saveProjectMileStoneResponse(projectInsightDTO.getProjectInsightQuestionList(), projectInsightDTO.getEmpId(), files, reviewerId);
				saveTaggedForHelp(projectInsightDTO.getTaggedForHelp(), projectInsightDTO.getProjectId(), "Project", projectInsightDTO.getEmpId());
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
	
	
	private void saveProjectResponse(List<ProjectQuestionDTO> applicationQuestionList, Long empId, List<MultipartFile> files, Long reviewerId, Long projectId) {
		try {
			if (applicationQuestionList != null && !applicationQuestionList.isEmpty()) {
			saveProjectInsightResponse(applicationQuestionList, files, reviewerId, empId);
				for (ProjectQuestionDTO projectQuestionDTO : applicationQuestionList) {
					saveTaggedForHelp(projectQuestionDTO.getTaggedForHelp(), projectQuestionDTO.getQuestionId(), "Question", empId);
				}
			} 
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private String saveProjectMileStoneResponse(List<ProjectInsightQuestionDTO> projectInsightQuestionDTOList, Long employeeId, List<MultipartFile> files, Long reviewerId) {
		String response = null;
		try {
			if (projectInsightQuestionDTOList != null && !projectInsightQuestionDTOList.isEmpty()) {
				for (ProjectInsightQuestionDTO projectInsightQuestionDTO : projectInsightQuestionDTOList) {
					saveProjectInsightResponse(projectInsightQuestionDTO.getProjectQuestion(), files, reviewerId,employeeId);
					saveProjectInsightModuleResponse(projectInsightQuestionDTO.getModuleList(), employeeId, files, reviewerId);
					saveTaggedForHelp(projectInsightQuestionDTO.getTaggedForHelp(),projectInsightQuestionDTO.getMilestoneId(),"Milestone",employeeId);
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

	private void saveProjectInsightModuleResponse(List<ModuleDTO> moduleList, Long employeeId,List<MultipartFile> files,Long reviewerId) {
		try {
			if (moduleList != null && !moduleList.isEmpty()) {
				for (ModuleDTO moduleDTO : moduleList) {
					saveProjectInsightResponse(moduleDTO.getProjectQuestion(),files,reviewerId,employeeId);
					saveProjectInsightSubModuleResponse(moduleDTO.getSubModuleList(), employeeId,files,reviewerId);
					saveTaggedForHelp(moduleDTO.getTaggedForHelp(),moduleDTO.getModuleId(),"Module",employeeId);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void saveProjectInsightSubModuleResponse(List<SubModuleDTO> subModuleList, Long employeeId,List<MultipartFile> files,Long reviewerId) {
		try {
			if (subModuleList != null && !subModuleList.isEmpty()) {
				for (SubModuleDTO subModuleDTO : subModuleList) {
					saveProjectInsightResponse(subModuleDTO.getProjectQuestion(),files,reviewerId,employeeId);
					saveTaggedForHelp(subModuleDTO.getTaggedForHelp(),subModuleDTO.getSubmoduleId(),"SubModule",employeeId);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void saveProjectInsightResponse(List<ProjectQuestionDTO> projectQuestionList, List<MultipartFile> files, Long reviewerId, Long employeeId) {
		try {
			if (projectQuestionList != null && !projectQuestionList.isEmpty()) {
				for (ProjectQuestionDTO projectQuestionDTO : projectQuestionList) {
					if (projectQuestionDTO.getQuestionId() != null
							&& projectQuestionDTO.getProjectResponseList() != null
							&& !projectQuestionDTO.getProjectResponseList().isEmpty()) {
						for (ProjectResponseDTO projectResponseDTO : projectQuestionDTO.getProjectResponseList()) {
							saveOrUpdateAllProjectResponseAndUploadDocument(projectQuestionDTO, projectResponseDTO, files, reviewerId);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private void saveTaggedForHelp(List<Long> taggedForHelp,Long entityId, String entityType, Long employeeId) {
		if (taggedForHelp != null && !taggedForHelp.isEmpty()) {
			for (Long taggedUserId : taggedForHelp) {
				ProjectInsightAssignees projectInsightAssignees = projectInsightAssigneesRepository.getProjectInsightAssigneesByEntityIdAndEntityTypeAndAssignedToHelpTaggedBy(entityId, entityType, taggedUserId, employeeId);
				if (projectInsightAssignees == null) {
					projectInsightAssignees = new ProjectInsightAssignees();
				}
				projectInsightAssignees.setAssignedTo(taggedUserId);
				projectInsightAssignees.setAssignType("Tagged");
				projectInsightAssignees.setEntityId(entityId);
				projectInsightAssignees.setEntityType(entityType);
				projectInsightAssignees.setTaggedBy(employeeId);
				projectInsightAssigneesRepository.save(projectInsightAssignees);
			}
		}
	}

	private void saveOrUpdateAllProjectResponseAndUploadDocument(ProjectQuestionDTO projectQuestionDTO, ProjectResponseDTO projectResponseDTO, List<MultipartFile> files, Long reviewerId) {
		try {
			ProjectInsightResponse projectInsightResponse = projectInsightResponseRepository.findByEmpIdAndQuestionMasterId(projectResponseDTO.getResponseByEmpId(), projectQuestionDTO.getQuestionId());
			if (projectInsightResponse != null) {
				projectInsightResponse = mapProjectResponseDTOToProjectResponseAndUploadFile(projectInsightResponse,projectResponseDTO ,files,reviewerId,projectQuestionDTO.getQuestionId());
				if (files != null) {}
			} else {
				projectInsightResponse = new ProjectInsightResponse();
				projectInsightResponse = mapProjectResponseDTOToProjectResponseAndUploadFile(projectInsightResponse,projectResponseDTO ,files,reviewerId,projectQuestionDTO.getQuestionId());
			}
			projectInsightResponseRepository.save(projectInsightResponse);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private ProjectInsightResponse mapProjectResponseDTOToProjectResponseAndUploadFile(ProjectInsightResponse projectInsightResponse, ProjectResponseDTO projectResponseDTO,List<MultipartFile> files, Long reviewerId, Long questionMasterId) {
		try {
			projectInsightResponse.setResponse(projectResponseDTO.getResponse());
			projectInsightResponse.setEmpId(projectResponseDTO.getResponseByEmpId());
			projectInsightResponse.setDocumentPath(projectResponseDTO.getDocumentPath());
			projectInsightResponse.setQuestionMasterId(questionMasterId);
			projectInsightResponse.setIsDraft(projectResponseDTO.getIsDraft());
			projectInsightResponse.setProcessTo(reviewerId);
			projectInsightResponse.setMarks(projectResponseDTO.getMarks());
			projectInsightResponse.setDocumentPath(projectResponseDTO.getDocumentPath());
			projectInsightResponse.setDocumentFileName(projectResponseDTO.getUploadedFileName());
			if (files != null) {
				for (MultipartFile document : files) {
					if (document != null && document.getOriginalFilename() != null && projectResponseDTO.getUploadedFileName() != null
							&& document.getOriginalFilename().equals(projectResponseDTO.getUploadedFileName())) {
						if(projectInsightResponse.getDocumentFileName() != null && !projectInsightResponse.getDocumentFileName().trim().equals("")) {
							deleteExistingDocumentByFileName(projectInsightResponse.getDocumentFileName());
						}
						String uploadResponse = uploadProjectResponseDocument(document);
						if (uploadResponse.equalsIgnoreCase("Document uploaded successfully")) {
							projectInsightResponse.setDocumentPath(projectResponseDTO.getDocumentPath());
							projectInsightResponse.setDocumentFileName(projectResponseDTO.getUploadedFileName());
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return projectInsightResponse;
	}

	private void deleteExistingDocumentByFileName(String documentFileName) {
		try {
			if (documentFileName != null) {
				 RestTemplate restTemplate = new RestTemplate();
		            HttpHeaders headers = new HttpHeaders();
		            headers.setAccept(List.of(MediaType.APPLICATION_OCTET_STREAM));
		            headers.set("X-API-KEY", dmsPortalFetchUrlKey);
		            String dmsPortalFetchUrl = dmsPortalUrl + "/delete.php";
		            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(dmsPortalFetchUrl).queryParam("document_name", documentFileName);
		            URI finalUri = builder.build().encode().toUri();
		            restTemplate.postForObject(finalUri, null, String.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public ServiceResponse getUserUploadedFileForQuestion(EmployeeDocumentDTO employeeDocumentDTO) {
		ServiceResponse serviceResponse = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setSubFeatureName("getProjectResponseDocument");
		apiLogInfo.setApiUrl("/api/getProjectResponseDocument");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("projectId : " + employeeDocumentDTO.getDocumentName());
		try {
			 RestTemplate restTemplate = new RestTemplate();
	            HttpHeaders headers = new HttpHeaders();
	            headers.setAccept(List.of(MediaType.APPLICATION_OCTET_STREAM));
	            headers.set("X-API-KEY", dmsPortalFetchUrlKey);
	            HttpEntity<String> entity = new HttpEntity<>(headers);
	            String dmsPortalFetchUrl = dmsPortalUrl + "/download.php";
	            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(dmsPortalFetchUrl).queryParam("document_name", employeeDocumentDTO.getDocumentName());
	            URI finalUri = builder.build().encode().toUri();
	            ResponseEntity<byte[]> response = restTemplate.exchange(
	            		finalUri,
	                    HttpMethod.GET,
	                    entity,
	                    byte[].class
	            );
	            
	            if(response.getBody()!= null) {
	                String contentType =   response.getHeaders().getContentType() != null ? response.getHeaders().getContentType().toString() : null;

		            serviceResponse.setServiceResponse( ResponseEntity.ok()
		                    .contentType(MediaType.parseMediaType(contentType))
		                    .body(response.getBody()));
		            
		        	apiLogInfo.setApiResponse("Document Fetched Successfully");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            } else {
	            	apiLogInfo.setApiResponse("Unable to Fetch Document Successfully");
		        	serviceResponse.setServiceResponse("Unable to Fetch Document Successfully");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            }
		} catch (Exception e) {
			e.printStackTrace();
			serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			serviceResponse.setServiceResponse("Something Went Wrong.");
			serviceResponse.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		return serviceResponse;
	}
	
	public String uploadProjectResponseDocument(MultipartFile document) {
		String response = null;
		try {
			if (document != null) {
				String dmsPortalUploadUrl = dmsPortalUrl + "/upload.php";
				RestTemplate restTemplate = new RestTemplate();

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.MULTIPART_FORM_DATA);
				headers.set("X-API-KEY", dmsPortalUploadUrlKey);
				MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
				body.add("document", new ByteArrayResource(document.getBytes()) {
					@Override
					public String getFilename() {
						return document.getOriginalFilename();
					}
				});
				HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
				String syncResponse = restTemplate.postForObject(dmsPortalUploadUrl, requestEntity, String.class);
				JSONObject json = new JSONObject(syncResponse);
				if (!json.isNull("message")) {
					String obj = json.getString("message");
					if (obj != null) {
						response = obj;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			response = "Failed to Upload Document.";
		}
		return response;
	}

	public ServiceResponse getAllProjectInsightContributionList(ProjectInsightDTO projectInsightDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setApiUrl("/api/getAllProjectInsightContributionList");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("All Project Contribution List By : " + projectInsightDTO.getEmpId());
		try {
			List<Object[]> objectList = null;
			if (projectInsightDTO.getPerformanceTabName().equals("Team Dashboard")) {
				String jobRole = employeeRepository.getJobRoleByEmployeeId(projectInsightDTO.getEmpId());
				if (jobRole != null) {
					projectInsightDTO.setEmployeeRole(jobRole);
				}
			}

			if (projectInsightDTO.getPerformanceTabName().equals("Team Dashboard")) {
				objectList = projectInsightResponseRepository.getAllProjectInsightForReviewByUser(projectInsightDTO.getEmpId());
			} else {
				if (projectInsightDTO.getEmployeeRole().equals("Employee")
						|| projectInsightDTO.getEmployeeRole().equals("TeamLead")
						|| projectInsightDTO.getEmployeeRole().equals("Manager")) {
					objectList = projectInsightResponseRepository.getAllProjectInsightByUser(projectInsightDTO.getEmpId());
				} else {
					objectList = projectInsightMilestoneRepository.getAllProjectInsight();
				}
			}
			
			if (objectList != null && !objectList.isEmpty()) {
				List<ProjectInsightDTO> dtoList = new ArrayList<>();
				for (Object[] object : objectList) {
					ProjectInsightDTO dto = new ProjectInsightDTO();
					dto.setProjectId(parseLong(object[0]));
					dto.setProjectName(parseString(object[1]));
					dto.setProjectManagerId(parseLong(object[2]));
					dto.setProjectManagerName(parseString(object[3]));
					dto.setCreatedBy(parseLong(object[4]));
					dto.setCreatedByName(parseString(object[5]));
					dto.setCreatedOn(parseString(object[6]));
					dtoList.add(dto);
				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("All Project Insight List Fetched");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Project Insight found. List is null.");
				apiLogInfo.setApiResponse("No Project Insight found. List is null");
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
	
	private Long parseLong(Object obj) {
		return obj != null ? Long.parseLong(obj.toString()) : null;
	}

	private String parseString(Object obj) {
		return obj != null ? obj.toString() : null;
	}
	
	private boolean checkIfIsEmployee(List<Object[]> employeePersonaListForTeam, String employeeRole) {
		boolean flag = true;
		try {
			if (employeeRole != null && !employeeRole.trim().equals("")
					&& (employeeRole.toLowerCase().contains("hod") || employeeRole.toLowerCase().contains("teamlead")
							|| employeeRole.toLowerCase().contains("manager")
							|| employeeRole.toLowerCase().contains("superadmin")
							|| employeeRole.toLowerCase().contains("rmg"))) {
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

	private String getAssignedToUserName(Long entityId, String entityType) {
		String assignedTo = null;
		try {
			List<String> usernameList = projectInsightAssigneesRepository.getUserNameByEntityIdAndEntityType(entityId,entityType);
			assignedTo = usernameList != null  ? usernameList.stream().map(String::valueOf).collect(Collectors.joining(", ")) : "";
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return assignedTo;
	}
	
	private String getAllTaggedUserName(Long entityId, String entityType,Long taggedBy) {
		String assignedTo = null;
		try {
			List<String> usernameList = projectInsightAssigneesRepository.getUserNameByEntityIdAndEntityTypeAndTaggedBy(entityId,entityType,taggedBy);
			assignedTo = usernameList != null  ? usernameList.stream().map(String::valueOf).collect(Collectors.joining(", ")) : "";
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return assignedTo;
	}
	
	private List<Long> getAllTaggedUserId(Long entityId, String entityType, Long taggedBy) {
		try {
			return projectInsightAssigneesRepository.getUserIdByEntityIdAndEntityTypeAndTaggedBy(entityId, entityType,taggedBy);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private Long getReviewerIdForQuestion(Long projectId, Long employeeId) {
		Long reviewerId = null;
		try {
			List<EmployeeTeamMap> employeeTeamList = employeeTeamMapRepository.findByProjectIdAndActive(Integer.parseInt(projectId.toString()), 1l);
			if (employeeTeamList == null || employeeTeamList.isEmpty()) {
				return getEmployeesRMorManagerId(employeeId);
			}

			Map<Long, String> employeeTeamMapObj = getEmpIdToHighestRoleMap(employeeTeamList);
			String employeeRole = employeeTeamMapObj.getOrDefault(employeeId, null);
			if (employeeRole == null || employeeRole.equals("SUPERADMIN") || employeeRole.equals("RMG") || employeeRole.equals("HR")) {
				return getEmployeesRMorManagerId(employeeId);
			}

			if (ROLE_HIERARCHY.indexOf(employeeRole) + 1 < ROLE_HIERARCHY.size()) {
				int roleIndex = ROLE_HIERARCHY.indexOf(employeeRole) + 1;
				String nextLevelRole = ROLE_HIERARCHY.get(roleIndex);
				for (Map.Entry<Long, String> entry : employeeTeamMapObj.entrySet()) {
					if (entry.getValue() != null && entry.getValue().equalsIgnoreCase(nextLevelRole)) {
						reviewerId = entry.getKey();
						break;
					}
				}
			}
			
			if (reviewerId == null) {
				reviewerId = getEmployeesRMorManagerId(employeeId);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return reviewerId;
	}
	
	public static Map<Long, String> getEmpIdToHighestRoleMap(List<EmployeeTeamMap> employeeTeamMapList) {
		Map<Long, String> result = new HashMap<>();

		for (EmployeeTeamMap etm : employeeTeamMapList) {
			String employeeRoleStr = etm.getEmployeeRole();
			if (employeeRoleStr == null || employeeRoleStr.trim().isEmpty())
				continue;

			List<String> roles = Arrays.stream(employeeRoleStr.split(",")).map(String::trim).map(String::toUpperCase)
					.filter(role -> !role.isEmpty()).collect(Collectors.toList());

			String highestRole = null;
			int maxIndex = -1;

			for (String role : roles) {
				int index = ROLE_HIERARCHY.indexOf(role);
				if (index > maxIndex) {
					maxIndex = index;
					highestRole = role;
				}
			}
			result.put(etm.getEmpId(), highestRole);
		}
		return result;
	}

	private Long getEmployeesRMorManagerId(Long employeeId) {
		try {
			Employee employee = employeeRepository.findByEmpId(employeeId);
			if (employee != null) {
				return employee.getReportingManagerId() != null ? employee.getReportingManagerId() : employee.getManagerId();
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public ResponseEntity<ProjectInsightDTO> onSearchTerm(String search) {
		ProjectInsightDTO response = new ProjectInsightDTO();
		try {
			List<String> tagList = nlpUtils.extractTags(search);
			
			if(!tagList.isEmpty()) {
				List<TagMaster> matchedTags = tagMasterRepository.findAll(TagSpecifications.tagNameLikeAny(tagList));

				Set<Long> projectIds = matchedTags.stream()
				        .map(TagMaster::getProjectId)
				        .collect(Collectors.toSet());
				
				System.out.println(projectIds);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok(response);
	}
	
}
