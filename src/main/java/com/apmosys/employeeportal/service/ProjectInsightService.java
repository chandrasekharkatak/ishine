package com.apmosys.employeeportal.service;

import java.net.URI;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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
import com.apmosys.employeeportal.dto.PoProjectSyncDTO;
import com.apmosys.employeeportal.dto.ProjectInsightDTO;
import com.apmosys.employeeportal.dto.ProjectInsightEntityDTO;
import com.apmosys.employeeportal.dto.ProjectInsightFilterDTO;
import com.apmosys.employeeportal.dto.ProjectInsightMilestoneDTO;
import com.apmosys.employeeportal.dto.ProjectInsightResponsePointsDTO;
import com.apmosys.employeeportal.dto.ProjectInsightUserContributionDTO;
import com.apmosys.employeeportal.dto.ProjectQuestionDTO;
import com.apmosys.employeeportal.dto.ProjectResponseDTO;
import com.apmosys.employeeportal.dto.SubModuleDTO;
import com.apmosys.employeeportal.dto.TagDTO;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeTeamMap;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectInsightAssignees;
import com.apmosys.employeeportal.model.ProjectInsightFilter;
import com.apmosys.employeeportal.model.ProjectInsightFilterOptions;
import com.apmosys.employeeportal.model.ProjectInsightMilestone;
import com.apmosys.employeeportal.model.ProjectInsightModule;
import com.apmosys.employeeportal.model.ProjectInsightResponse;
import com.apmosys.employeeportal.model.ProjectInsightResponseMetadata;
import com.apmosys.employeeportal.model.ProjectInsightResponsePoints;
import com.apmosys.employeeportal.model.ProjectInsightSubModule;
import com.apmosys.employeeportal.model.ProjectInsightUserContribution;
import com.apmosys.employeeportal.model.QuestionMaster;
import com.apmosys.employeeportal.model.TagMaster;
import com.apmosys.employeeportal.model.UserContributionDocument;
import com.apmosys.employeeportal.model.UserContributionResponseRemarks;
import com.apmosys.employeeportal.mongodb.modal.FileStorage;
import com.apmosys.employeeportal.mongodb.repository.FileMongoRepository;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.ProjectInsightAssigneesRepository;
import com.apmosys.employeeportal.repository.ProjectInsightFilterOptionsRepository;
import com.apmosys.employeeportal.repository.ProjectInsightFilterRepository;
import com.apmosys.employeeportal.repository.ProjectInsightMilestoneRepository;
import com.apmosys.employeeportal.repository.ProjectInsightModuleRepository;
import com.apmosys.employeeportal.repository.ProjectInsightResponseMetadataRepository;
import com.apmosys.employeeportal.repository.ProjectInsightResponsePointsRepository;
import com.apmosys.employeeportal.repository.ProjectInsightResponseRepository;
import com.apmosys.employeeportal.repository.ProjectInsightSubModuleRepository;
import com.apmosys.employeeportal.repository.ProjectInsightUserContributionRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.QuestionMasterRepository;
import com.apmosys.employeeportal.repository.TagMasterRepository;
import com.apmosys.employeeportal.repository.UserContributionDocumentRepository;
import com.apmosys.employeeportal.repository.UserContributionResponseRemarksRepository;
import com.apmosys.employeeportal.response.SearchResultResponse;
import com.apmosys.employeeportal.utility.NLPUtils;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.TagSpecifications;
import com.apmosys.employeeportal.utility.TagUtils;
import com.fasterxml.jackson.annotation.JsonFormat;

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
	
	@Autowired
	DepartmentRepository departmentRepository;
	
	@Autowired
	ProjectInsightFilterRepository projectInsightFilterRepository;
	
	@Autowired
	ProjectInsightFilterOptionsRepository projectInsightFilterOptionsRepository;
	
	@Autowired
	ProjectInsightUserContributionRepository projectInsightUserContributionRepository;
	
	@Autowired
	UserContributionResponseRemarksRepository userContributionResponseRemarksRepository;
	
	@Autowired
	UserContributionDocumentRepository userContributionDocumentRepository;
	
	@Autowired
	private ProjectInsightResponseMetadataRepository projectInsightResponseMetadataRepository;
	
	@Autowired
	private ProjectInsightResponsePointsRepository projectInsightResponsePointsRepository;
	
	@Autowired
	TagUtils tagUtils;
	
	@Autowired
	FileMongoRepository fileMongoRepository;
	
	@Transactional
	public ServiceResponse createProjectInsightQuestion(ProjectInsightDTO projectInsightDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setApiUrl("/api/createProjectInsightQuestion");
		apiLogInfo.setSubFeatureName("createProjectInsightQuestion");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("CreatedBy : " + projectInsightDTO.getCreatedBy() + " ,ProjectId :"  + projectInsightDTO.getProjectId() + " ,Created By EmpId :" + projectInsightDTO.getCreatedBy());

		AtomicBoolean isSuccess = new AtomicBoolean(false);
        StringBuilder combinedText = new StringBuilder();
		try {
			if (!validationService.validateEmpId(projectInsightDTO.getCreatedBy())) {
				apiLogInfo.setApiResponse("Employee Id does not exists");
				response.setServiceResponse("Employee Id does not exists.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				return response;
			}
			
			combinedText.append(projectInsightDTO.getProjectName()).append(" ").append(projectInsightDTO.getProjectManagerName()).append(" ");
			
			Project project = projectRepository.findByProjectId(Integer.parseInt(projectInsightDTO.getProjectId().toString()));
			if (project == null) {
				throw new RuntimeException("Project Not Found!!");
			}
			
			// Project Assign to ProjectManager
			List<Long> assignedToProjectManager = new ArrayList<>();
			assignedToProjectManager.add(project.getProjectManagerId());
			projectInsightDTO.setAssignedToUserId(assignedToProjectManager);
			saveAssignedTo(projectInsightDTO.getAssignedToUserId(),  projectInsightDTO.getProjectId(), "Project",false);
			
			// Add Application Question
			if (!projectInsightDTO.getQuestionList().isEmpty()) {
				ServiceResponse applicationQuestionResponse = addUpdateQuestion(projectInsightDTO.getQuestionList(), projectInsightDTO.getProjectId(), "Project", combinedText);
				isSuccess.set("Success".equals(applicationQuestionResponse.getServiceStatus()));
				response.setServiceResponse("Project Insight created successfully.");
				apiLogInfo.setApiResponse("Project Insight Successfully");
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}

			// Add Milestone
			if (!projectInsightDTO.getProjectInsightMilestoneList().isEmpty()) {
				projectInsightDTO.getProjectInsightMilestoneList().forEach((projectInsightMilestoneDTO) -> {
					ProjectInsightMilestone newMilestoneCreated = saveOrUpdateProjectMilestone(projectInsightMilestoneDTO, projectInsightDTO.getProjectId(), combinedText, projectInsightDTO.getCreatedBy(), projectInsightDTO.getUpdatedBy());

					if (newMilestoneCreated != null && newMilestoneCreated.getMilestoneId() != null) {
						isSuccess.set(true);
						Long milestoneId = newMilestoneCreated.getMilestoneId();

						// Milestone Assign to
						saveAssignedTo(projectInsightMilestoneDTO.getAssignedToUserId(), milestoneId, "Milestone",false);
						
						// Add Milestone Question
						if (!projectInsightMilestoneDTO.getQuestionList().isEmpty()) {
							ServiceResponse milestoneQuestionResponse = addUpdateQuestion(projectInsightMilestoneDTO.getQuestionList(), milestoneId, "Milestone", combinedText);
							isSuccess.set("Success".equals(milestoneQuestionResponse.getServiceStatus()));
						}

						// Add Module
						if (projectInsightMilestoneDTO.getModuleList() != null && !projectInsightMilestoneDTO.getModuleList().isEmpty()) {

							for (ModuleDTO moduleDTO : projectInsightMilestoneDTO.getModuleList()) { 
								ProjectInsightModule projectInsightModule = saveOrUpdateModule(moduleDTO, milestoneId, combinedText, projectInsightDTO.getCreatedBy(), projectInsightDTO.getUpdatedBy());
								if(projectInsightModule.getModuleId() != null) {
									Long moduleId = projectInsightModule.getModuleId();

									// Module Assign to
									saveAssignedTo(moduleDTO.getAssignedToUserId(), moduleId, "Module", true);

									// Add/Update Module question
									if (moduleDTO.getQuestionList() != null && !moduleDTO.getQuestionList().isEmpty()) {
										addUpdateQuestion(moduleDTO.getQuestionList(), moduleId, "Module", combinedText);
									}
									
									if (!moduleDTO.getSubModuleList().isEmpty()) {
										saveOrUpdateSubModuleList(moduleDTO.getSubModuleList(), moduleId, "SubModule", combinedText, projectInsightDTO.getCreatedBy(), projectInsightDTO.getUpdatedBy());
									}
								}
							}
						}
					} else {
						apiLogInfo.setApiResponse("Failed to create Project Insight Questions");
						response.setServiceResponse("Failed to create Project Insight Questions.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					}
				});

				if (isSuccess.get()) {
					response.setServiceResponse("Project Insight created successfully.");
					apiLogInfo.setApiResponse("Project Insight Successfully");
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
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

	private ProjectInsightModule saveProjectModule(ModuleDTO module, Long milestoneId, Long createdBy, StringBuilder combinedText) {
		try {
			combinedText.append(module.getModule()).append(" ").append(module.getDescription());
			ProjectInsightModule newProjectInsightModule = new ProjectInsightModule();
			newProjectInsightModule.setCreatedBy(createdBy);
			newProjectInsightModule.setMilestoneId(milestoneId);
			newProjectInsightModule.setDescription(module.getDescription());
			newProjectInsightModule.setRedmineId(module.getRedmineId());
			newProjectInsightModule.setModule(module.getModule());
			return projectInsightModuleRepository.save(newProjectInsightModule);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private ProjectInsightMilestone saveOrUpdateProjectMilestone(ProjectInsightMilestoneDTO projectInsightMilestoneDTO, Long projectId, StringBuilder combinedText, Long createdBy, Long updatedBy) {
		try {
			ProjectInsightMilestone projectInsightMilestone = null;
			if (projectInsightMilestoneDTO.getMilestoneId() == null || (projectInsightMilestoneDTO.getActionType() != null && projectInsightMilestoneDTO.getActionType().equalsIgnoreCase("Add"))) {
				projectInsightMilestone = saveProjectInsightMilestone(projectInsightMilestone, projectInsightMilestoneDTO, projectId, createdBy, updatedBy);
			} else if (projectInsightMilestoneDTO.getMilestoneId() != null) {
				ProjectInsightMilestone milestoneDbObject = projectInsightMilestoneRepository.getByMilestoneId(projectInsightMilestoneDTO.getMilestoneId());
				if (milestoneDbObject != null) {
					projectInsightMilestone = saveProjectInsightMilestone(milestoneDbObject, projectInsightMilestoneDTO, projectId, createdBy, updatedBy);
				} else {
					projectInsightMilestone = saveProjectInsightMilestone(projectInsightMilestone, projectInsightMilestoneDTO, projectId, createdBy, updatedBy);
				}
			}
			if (projectInsightMilestone != null) {
				projectInsightMilestone = projectInsightMilestoneRepository.save(projectInsightMilestone);
			}
			return projectInsightMilestone;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public ProjectInsightMilestone saveProjectInsightMilestone(ProjectInsightMilestone projectInsightMilestone, ProjectInsightMilestoneDTO projectInsightMilestoneDTO, Long projectId, Long createdBy,Long updatedBy) {
		try {
			if (projectInsightMilestone == null) {
				projectInsightMilestone = new ProjectInsightMilestone();
			}
			projectInsightMilestone.setProjectId(projectId);
			projectInsightMilestone.setMilestone(projectInsightMilestoneDTO.getMilestone());
			projectInsightMilestone.setDescription(projectInsightMilestoneDTO.getDescription());
			projectInsightMilestone.setDeptId(projectInsightMilestoneDTO.getDeptId());
			projectInsightMilestone.setUpdatedBy(updatedBy);
			projectInsightMilestone.setCreatedBy(createdBy);
			projectInsightMilestone.setRedmineId(projectInsightMilestoneDTO.getRedmineId());
			return projectInsightMilestone;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private void saveSubModuleList(List<SubModuleDTO> subModuleList, Long moduleId, String subModuleType, StringBuilder combinedText) {
		try {
			if (subModuleList != null && !subModuleList.isEmpty()) {
				for (SubModuleDTO subModuleDTO : subModuleList) {
					
					combinedText.append(subModuleDTO.getSubModule()).append(" ")
											.append(subModuleDTO.getDescription());
					
					ProjectInsightSubModule newProjectInsightSubModule = new ProjectInsightSubModule();
					newProjectInsightSubModule.setCreatedBy(subModuleDTO.getCreatedBy());
					newProjectInsightSubModule.setModuleId(moduleId);
					newProjectInsightSubModule.setDescription(subModuleDTO.getDescription());
					newProjectInsightSubModule.setRedmineId(subModuleDTO.getRedmineId());
					newProjectInsightSubModule.setSubmodule(subModuleDTO.getSubModule());
					newProjectInsightSubModule.setSubModuleType(subModuleType);
					ProjectInsightSubModule submoduleResponse = projectInsightSubModuleRepository.save(newProjectInsightSubModule);
					if (submoduleResponse != null) {
						Long submoduleId = submoduleResponse.getSubmoduleId();

						// Assign to
						saveAssignedTo(subModuleDTO.getAssignedToUserId(), submoduleId, subModuleType,false);

						// Add SubModule question
						if (subModuleDTO.getQuestionList() != null && !subModuleDTO.getQuestionList().isEmpty()) {
							addUpdateQuestion(subModuleDTO.getQuestionList(), submoduleId, subModuleType, combinedText);
						}

						if (subModuleDTO.getSubSubModuleList() != null && !subModuleDTO.getSubSubModuleList().isEmpty()) {
							saveSubModuleList(subModuleDTO.getSubSubModuleList(), submoduleId, "Sub-SubModule", combinedText);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public QuestionMaster saveQuestionMaster(QuestionMaster questionMaster, ProjectQuestionDTO projectQuestionDTO, Long entityId, String entity) {
		try {
			if (questionMaster == null) {
				questionMaster = new QuestionMaster();
			}
			questionMaster.setEntityId(entityId);
			questionMaster.setEntityType(entity);
			questionMaster.setQuestion(projectQuestionDTO.getQuestion());
			questionMaster.setDescription(projectQuestionDTO.getDescription());
			questionMaster.setDocumentUpload(projectQuestionDTO.getDocumentUpload());
			questionMaster.setOptions(projectQuestionDTO.getOptions());
			questionMaster.setOptionType(projectQuestionDTO.getOptionType());
			questionMaster.setRequired(projectQuestionDTO.getRequired());
			return questionMaster;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void saveAssignedTo(List<Long> assignedToUserId, Long entityId, String entityType, boolean deleteExistingRecords) {
		try {
			if (assignedToUserId != null && !assignedToUserId.isEmpty()) {
				if (deleteExistingRecords) {
					List<ProjectInsightAssignees> milestoneAssigneeDbResponse = projectInsightAssigneesRepository.getByEntityIdAndEntityType(entityId, entityType);
					if (milestoneAssigneeDbResponse != null && !milestoneAssigneeDbResponse.isEmpty()) {
						projectInsightAssigneesRepository.deleteAll(milestoneAssigneeDbResponse);
					}
				}
				for (Long assignToId : assignedToUserId) {
					ProjectInsightAssignees newAssignObj = new ProjectInsightAssignees();
					newAssignObj.setAssignedTo(assignToId);
					newAssignObj.setEntityId(entityId);
					newAssignObj.setEntityType(entityType);
					newAssignObj.setAssignType("Assigned");
					projectInsightAssigneesRepository.save(newAssignObj);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e; 
		}
	}
	
	public ServiceResponse addUpdateQuestion(List<ProjectQuestionDTO> questionAddList, Long entityId, String entity, StringBuilder combinedText) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("addQuestion common method");
		apiLogInfo.setApiUrl("/api/createProjectInsightQuestion --- addQuestion");
		apiLogInfo.setLogLevel("INFO");

		try {
			List<QuestionMaster> questionList = new ArrayList<>();
			questionAddList.forEach((questionObj) -> {
				combinedText.append(questionObj.getQuestion()).append(" ").append(questionObj.getDescription()).append(questionObj.getOptions());
 
				if (questionObj.getQuestionId() == null || (questionObj.getActionType() != null && questionObj.getActionType().equalsIgnoreCase("Add"))) {
					questionList.add(saveQuestionMaster(null, questionObj, entityId, entity));
				} else if (questionObj.getQuestionId() != null) {
					QuestionMaster quesDbObject = questionMasterRepository.findByQuestionMasterId(questionObj.getQuestionId());
					if (quesDbObject != null) {
						questionList.add(saveQuestionMaster(quesDbObject, questionObj, entityId, entity));
					} else {
						questionList.add(saveQuestionMaster(null, questionObj, entityId, entity));
					}
				}
			});

			List<QuestionMaster> listSaved = questionMasterRepository.saveAll(questionList);
			if (!listSaved .isEmpty()) {
				apiLogInfo.setApiResponse("Project Insight Successfully");
				response.setServiceResponse("Project Insight created successfully.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				apiLogInfo.setApiResponse("Project Insight but no questions were added to milestone");
				response.setServiceResponse("Project Insight but no questions were added to milestone.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			}
		} catch (Exception e) {
			e.printStackTrace();
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		}
		return response;
	}
	
	public ServiceResponse getAllProjectInsightList(ProjectInsightDTO projectInsightDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setApiUrl("/api/getAllProjectInsightList");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("AllSurveyList size : " + projectInsightMilestoneRepository.getAllProjectInsight().size());

		try {
			List<Object[]> objectList = null;

			if (projectInsightDTO.getEmployeeRole().equals("Employee")
					|| projectInsightDTO.getEmployeeRole().equals("TeamLead")
					|| projectInsightDTO.getEmployeeRole().equals("Manager")) {
				objectList = projectInsightMilestoneRepository.getAllProjectInsightByUser(projectInsightDTO.getEmpId());
			} else {
				List<Long> projectIds  = new ArrayList<Long>();
				objectList = projectInsightMilestoneRepository.getAllProjectInsight();
				if(objectList != null && !objectList.isEmpty()) {
					projectIds =  objectList.stream().map(object -> parseLong(object[0])).collect(Collectors.toList());
				} else {
					projectIds.add(-1l);
				}
				objectList.addAll(questionMasterRepository.getAllProjectInsight(projectIds));
			}

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No Project Insight found. List is empty.");
				} else {
					List<ProjectInsightDTO> dtoList = new ArrayList<>();

					list.forEach((object) -> {
						ProjectInsightDTO dto = new ProjectInsightDTO();
						dto.setProjectId(parseLong(object[0]));
						dto.setProjectName(parseString(object[1]));
						dto.setProjectManagerId(parseLong(object[2]));
						dto.setProjectManagerName(parseString(object[3]));
						dto.setCreatedBy(parseLong(object[4]));
						dto.setCreatedByName(parseString(object[5]));
						dto.setCreatedOn(parseString(object[6]));
						dtoList.add(dto);
					});
					apiLogInfo.setApiResponse("All Project Insight List Fetched");
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				}
			}, () -> {
				apiLogInfo.setApiResponse("No Project Insight found. List is null");
				response.setServiceResponse("No Project Insight found. List is null.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			});

		} catch (Exception e) {
			e.printStackTrace();
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	public List<ProjectQuestionDTO> getQuestion(Long entityId, String entity) {
		List<ProjectQuestionDTO> response = new ArrayList<>();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setSubFeatureName("getQuestion common method");
		apiLogInfo.setApiUrl("/api/getAllQuestionsByProjectId --- getQuestion");

		try {
			List<QuestionMaster> questionList = questionMasterRepository.findByEntityIdAndEntityType(entityId, entity);

			if (!questionList.isEmpty()) {
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

		} catch (Exception e) {
			e.printStackTrace();
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		return response;
	}

	public ServiceResponse getAllQuestionsByProjectId(ProjectInsightDTO projectInsightDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setApiUrl("/api/getAllQuestionsByProjectId");
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
			
			Project project = projectRepository.findByProjectId(Integer.parseInt(projectInsightDTO.getProjectId().toString()));
			if (project == null) {
				throw new RuntimeException("Project Not Found!!");
			}
			
			ProjectInsightDTO responseObject = new ProjectInsightDTO();
			responseObject.setProjectId(projectInsightDTO.getProjectId());
			responseObject.setProjectName(project.getProjectName());
			responseObject.setProjectManagerId(project.getProjectManagerId());
			responseObject.setProjectManagerName(getProjectManagerName(project.getProjectManagerId()));
			responseObject.setCreatedBy(project.getCreatedBy());
			responseObject.setQuestionList(getQuestion(projectInsightDTO.getProjectId(), "Project"));
			
			List<ProjectInsightMilestone> mileStoneList = projectInsightMilestoneRepository.getByProjectId(projectInsightDTO.getProjectId());
			if (mileStoneList != null && !mileStoneList.isEmpty()) {
					List<ProjectInsightMilestoneDTO> projectInsightQuestion = new ArrayList<>();
					mileStoneList.forEach((mileStone) -> {
						ProjectInsightMilestoneDTO mileStoneProjObject =  mapMilestone(mileStone,projectInsightDTO.getProjectId());   
						
						List<ProjectInsightModule> moduleList = projectInsightModuleRepository.getByMilestoneId(mileStone.getMilestoneId());
						List<ModuleDTO> moduleResponseList = new ArrayList<>();
						if (moduleList != null &&  !moduleList.isEmpty()) {
							moduleList.forEach((module) -> {
								ModuleDTO modDto = mapProjectModule(module);
								
								List<ProjectInsightSubModule> subModuleList = projectInsightSubModuleRepository.getByModuleIdAndSubModuleType(module.getModuleId(), "SubModule");
								modDto.setSubModuleList( getSubModuleQuestionList(subModuleList,"SubModule"));
								moduleResponseList.add(modDto);
							});
						}
						mileStoneProjObject.setModuleList(moduleResponseList);
						projectInsightQuestion.add(mileStoneProjObject);
					});
					responseObject.setProjectInsightMilestoneList(projectInsightQuestion);
					response.setServiceResponse(responseObject);
					apiLogInfo.setApiResponse("All Questions By MilestoneId Fetched");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			} 
		} catch (Exception e) {
			e.printStackTrace();
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse getAllProjectInsightQuestionsByProjectIdAndEmpId(ProjectInsightDTO projectInsightDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setSubFeatureName("getAllProjectInsightQuestionsByProjectIdAndEmpId");
		apiLogInfo.setApiUrl("/api/getAllProjectInsightQuestionsByProjectIdAndEmpId");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("projectId : " + projectInsightDTO.getProjectId());
		try {
			if (projectInsightDTO.getProjectId() == null) {
				throw new IllegalArgumentException("Project Id cannot be null!!");
			}

			Project project = projectRepository
					.findByProjectId(Integer.parseInt(projectInsightDTO.getProjectId().toString()));
			if (project == null) {
				throw new RuntimeException("Project Not Found!!");
			}

			List<Object[]> employeePersonaListForTeam = employeeTeamMapRepository.getEmployeePersonaForProject(projectInsightDTO.getEmpId(), projectInsightDTO.getProjectId());
			boolean isEmployee = checkIfIsEmployee(employeePersonaListForTeam, projectInsightDTO.getEmployeeRole());

			List<ProjectQuestionDTO> projectQuestionList = getProjectQuestionDTOList(projectInsightDTO.getProjectId(),"Project", projectInsightDTO.getEmpId(), isEmployee, projectInsightDTO.getPerformanceTabName(), projectInsightDTO.getProjectId(), null);
			List<ProjectInsightMilestone> projectInsightMilestoneList = projectInsightMilestoneRepository.getByProjectId(projectInsightDTO.getProjectId());
			if ((projectQuestionList == null || projectQuestionList.isEmpty())
					&& (projectInsightMilestoneList == null || projectInsightMilestoneList.isEmpty())) {
				response.setServiceResponse("No Questions or Milestone Found for Project.");
				apiLogInfo.setApiResponse("No Questions or Milestone Found for Project.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				return response;
			}

			ProjectInsightDTO projectInsightDTODbObject = createProjectInsightMileStoneObject(projectInsightMilestoneList, projectInsightDTO.getEmpId(), isEmployee, projectInsightDTO.getPerformanceTabName(), projectInsightDTO.getProjectId());
			projectInsightDTODbObject.setQuestionList(projectQuestionList);
			projectInsightDTODbObject.setProjectId(projectInsightDTO.getProjectId());
			projectInsightDTODbObject.setProjectManagerId(project.getProjectManagerId());
			projectInsightDTODbObject.setProjectManagerName(getProjectManagerName(project.getProjectManagerId()));
			projectInsightDTODbObject.setProjectName(project.getProjectName());
			projectInsightDTODbObject.setIsFinalSubmitted(getIsFinalResponseSubmittedByUserForProject(projectInsightDTO.getEmpId(), projectInsightDTO.getProjectId()));
			response.setServiceResponse(projectInsightDTODbObject);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			apiLogInfo.setApiResponse("All Project Insight Questions Fetched For Project!!");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

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
	
	private String getProjectManagerName(Long projectManagerId) {
		try {
			if(projectManagerId == null) {
				return "";
			}
			Optional<Employee> employee = employeeRepository.findById(projectManagerId);
			if (employee.isPresent())
				return employee.get().getName();
			else
				return "";
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private ProjectInsightMilestoneDTO mapMilestone(ProjectInsightMilestone mileStone,Long projectId) {
		try {
			ProjectInsightMilestoneDTO mileStoneProjObject = new ProjectInsightMilestoneDTO();
			List<ProjectInsightAssignees> assignToDbList = projectInsightAssigneesRepository.getByEntityIdAndEntityType(mileStone.getMilestoneId(), "Milestone");
			if (!assignToDbList.isEmpty()) {
				mileStoneProjObject.setAssignedToUserId(assignToDbList.stream().map(ProjectInsightAssignees::getAssignedTo).collect(Collectors.toList()));
				mileStoneProjObject.setAssignedToUserNames(getAssignedToUserName(mileStone.getMilestoneId(), "Milestone"));
			}

			mileStoneProjObject.setDeptId(mileStone.getDeptId());
			mileStoneProjObject.setDescription(mileStone.getDescription());
			mileStoneProjObject.setMilestone(mileStone.getMilestone());
			mileStoneProjObject.setMilestoneId(mileStone.getMilestoneId());
			mileStoneProjObject.setRedmineId(mileStone.getMilestoneId());
			mileStoneProjObject.setProjectId(projectId);
			List<ProjectQuestionDTO> projectQuestionList = getQuestion(mileStone.getMilestoneId(), "Milestone");
			mileStoneProjObject.setQuestionList(projectQuestionList);

			return mileStoneProjObject;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private ModuleDTO mapProjectModule(ProjectInsightModule module) {
		try {
			ModuleDTO modDto = new ModuleDTO();
			List<ProjectInsightAssignees> assignModuleToDbList = projectInsightAssigneesRepository.getByEntityIdAndEntityType(module.getModuleId(), "Module");
			if (!assignModuleToDbList.isEmpty()) {
				modDto.setAssignedToUserId(assignModuleToDbList.stream().map(ProjectInsightAssignees::getAssignedTo).collect(Collectors.toList()));
				modDto.setAssignedToUserNames(getAssignedToUserName(module.getModuleId(), "Module"));
			}

			modDto.setCreatedBy(module.getCreatedBy());
			modDto.setCreatedOn(module.getCreatedOn().toString());
			modDto.setDescription(module.getDescription());
			modDto.setMilestoneId(module.getMilestoneId());
			modDto.setModule(module.getModule());
			modDto.setModuleId(module.getModuleId());
			modDto.setRedmineId(module.getRedmineId());
			modDto.setUpdatedBy(module.getUpdatedBy());
			List<ProjectQuestionDTO> moduleQuestionList = getQuestion(module.getModuleId(),"Module");
			modDto.setQuestionList(moduleQuestionList);
			
			return modDto;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private List<SubModuleDTO> getSubModuleQuestionList(List<ProjectInsightSubModule> subModuleList, String subModuleType) {
		List<SubModuleDTO> subModuleResponseList = new ArrayList<>();
		try {
			if (!subModuleList.isEmpty()) {
				subModuleList.forEach((submodule) -> {
					SubModuleDTO submodDto = new SubModuleDTO();
					List<ProjectInsightAssignees> assignSubModuleToDbList = projectInsightAssigneesRepository.getByEntityIdAndEntityType(submodule.getSubmoduleId(), subModuleType);
					if (!assignSubModuleToDbList.isEmpty()) {
						submodDto.setAssignedToUserId(assignSubModuleToDbList.stream().map(ProjectInsightAssignees::getAssignedTo).collect(Collectors.toList()));
						submodDto.setAssignedToUserNames(getAssignedToUserName(submodule.getSubmoduleId(), subModuleType));
					}
					submodDto.setCreatedBy(submodule.getCreatedBy());
					submodDto.setCreatedOn(submodule.getCreatedOn().toString());
					submodDto.setDescription(submodule.getDescription());
					submodDto.setModuleId(submodule.getModuleId());
					submodDto.setRedmineId(submodule.getRedmineId());
					submodDto.setSubModule(submodule.getSubmodule());
					submodDto.setSubModuleId(submodule.getSubmoduleId());
					submodDto.setUpdatedBy(submodule.getUpdatedBy());
					submodDto.setQuestionList(getQuestion(submodule.getSubmoduleId(),subModuleType));
					List<ProjectInsightSubModule> subSubModuleList = projectInsightSubModuleRepository.getByModuleIdAndSubModuleType(submodule.getSubmoduleId(),"Sub-SubModule");
					if(subSubModuleList != null && !subSubModuleList.isEmpty()) {
						submodDto.setSubSubModuleList(getSubModuleQuestionList(subSubModuleList, "Sub-SubModule"));
					}
					subModuleResponseList.add(submodDto);
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return subModuleResponseList;
	}

	public ServiceResponse updateProjectInsightQuestion(ProjectInsightDTO projectInsightDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setSubFeatureName("updateProjectInsightQuestion");
		apiLogInfo.setApiUrl("/api/updateProjectInsightQuestion");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("projectId : " + projectInsightDTO.getProjectId());
		try {
			StringBuilder combinedText = new StringBuilder();

			Project project = projectRepository.findByProjectId(Integer.parseInt(projectInsightDTO.getProjectId().toString()));
			if (project == null) {
				throw new RuntimeException("Project Not Found!!");
			}
			
			combinedText.append(project.getProjectName()).append(" ").append(projectInsightDTO.getProjectManagerName()).append(" ");
			
			// Project Assign to ProjectManager
			List<Long> assignedToProjectManager = new ArrayList<>();
			assignedToProjectManager.add(project.getProjectManagerId());
			projectInsightDTO.setAssignedToUserId(assignedToProjectManager);
			saveAssignedTo(projectInsightDTO.getAssignedToUserId(),  projectInsightDTO.getProjectId(), "Project",false);
			
			if(projectInsightDTO.getDeletedProjectInsightEntityList() !=null && !projectInsightDTO.getDeletedProjectInsightEntityList().isEmpty()) {
				deleteProjectInsightEntities(projectInsightDTO.getDeletedProjectInsightEntityList());
			}
			
			// Add/Update Project question
			if (!projectInsightDTO.getQuestionList().isEmpty()) {
				addUpdateQuestion(projectInsightDTO.getQuestionList(), projectInsightDTO.getProjectId(), "Project", combinedText);
			}

			if (projectInsightDTO.getProjectInsightMilestoneList() != null && !projectInsightDTO.getProjectInsightMilestoneList().isEmpty()) {
				for(ProjectInsightMilestoneDTO projectInsightMilestoneDTO : projectInsightDTO.getProjectInsightMilestoneList()) {
					ProjectInsightMilestone projectInsightMilestone = saveOrUpdateProjectMilestone(projectInsightMilestoneDTO,projectInsightDTO.getProjectId(), combinedText,projectInsightDTO.getUpdatedBy(),projectInsightDTO.getCreatedBy());
					if (projectInsightMilestone == null || (projectInsightMilestone != null && projectInsightMilestone.getMilestoneId() == null)) {
						apiLogInfo.setApiResponse("No Project Insight Milestone Found.");
						response.setServiceResponse("No Project Insight Milestone Found.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						return response;
					}
					combinedText.append(projectInsightMilestoneDTO.getMilestone()).append(" ").append(projectInsightMilestoneDTO.getDescription());
					Long milestoneId = projectInsightMilestone.getMilestoneId();
					
					// Milestone Assign to
					saveAssignedTo(projectInsightMilestoneDTO.getAssignedToUserId(),milestoneId, "Milestone", true);
					
					// Add/Update Milestone question
					if (projectInsightMilestoneDTO.getQuestionList() != null && !projectInsightMilestoneDTO.getQuestionList().isEmpty()) {
						addUpdateQuestion(projectInsightMilestoneDTO.getQuestionList(), milestoneId , "Milestone", combinedText);
					}
					
					if (projectInsightMilestoneDTO.getModuleList() != null && !projectInsightMilestoneDTO.getModuleList().isEmpty()) {
						for (ModuleDTO moduleDTO : projectInsightMilestoneDTO.getModuleList()) { 
							ProjectInsightModule projectInsightModule = saveOrUpdateModule(moduleDTO, milestoneId, combinedText, projectInsightDTO.getCreatedBy(), projectInsightDTO.getUpdatedBy());
							if(projectInsightModule.getModuleId() != null) {
								Long moduleId = projectInsightModule.getModuleId();

								// Module Assign to
								saveAssignedTo(moduleDTO.getAssignedToUserId(), moduleId, "Module", true);

								// Add/Update Module question
								if (moduleDTO.getQuestionList() != null && !moduleDTO.getQuestionList().isEmpty()) {
									addUpdateQuestion(moduleDTO.getQuestionList(), moduleId, "Module", combinedText);
								}
								
								if (!moduleDTO.getSubModuleList().isEmpty()) {
									saveOrUpdateSubModuleList(moduleDTO.getSubModuleList(), moduleId, "SubModule", combinedText, projectInsightDTO.getCreatedBy(), projectInsightDTO.getUpdatedBy());
								}
							}
						}
					}
				}
				
				apiLogInfo.setApiResponse("Project Insight updated Successfully");
				response.setServiceResponse("Project Insight updated Successfully.");
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				apiLogInfo.setApiResponse("No Project Insight Found for Update.");
				response.setServiceResponse("No Project Insight Found for Update.");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
		} catch (Exception e) {
			e.printStackTrace();
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	private void deleteProjectInsightEntities(List<ProjectInsightEntityDTO> deletedProjectInsightEntityList) {
		try {
			for (ProjectInsightEntityDTO projectInsightEntityDTO : deletedProjectInsightEntityList) {
				if (projectInsightEntityDTO.getEntityType() != null
						&& projectInsightEntityDTO.getEntityType().equals("Milestone")) {
					deleteProjectInsightMilestoneAndItsChild(projectInsightEntityDTO.getEntityId());
				} else if (projectInsightEntityDTO.getEntityType() != null
						&& projectInsightEntityDTO.getEntityType().equals("Module")) {
					deleteProjectInsightModuleAndItsChild(projectInsightEntityDTO.getEntityId());
				} else if (projectInsightEntityDTO.getEntityType() != null
						&& (projectInsightEntityDTO.getEntityType().equals("SubModule")
								|| projectInsightEntityDTO.getEntityType().equals("Sub-SubModule"))) {
					deleteProjectInsightSubModuleAndItsChild(projectInsightEntityDTO.getEntityId(),projectInsightEntityDTO.getEntityType());
				} else if (projectInsightEntityDTO.getEntityType() != null
						&& projectInsightEntityDTO.getEntityType().equals("Question")) {
					deleteProjectInsightQuestion(projectInsightEntityDTO.getEntityId(),"Question");
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void deleteResponseById(Long questionId) {
		try {
			List<ProjectInsightResponse> projectInsightResponseList =  projectInsightResponseRepository.findAllByQuestionMasterId(questionId);
			if(projectInsightResponseList != null && !projectInsightResponseList.isEmpty()) {
				List<Long> projectInsightResponseIdList = projectInsightResponseList.stream().map(ProjectInsightResponse::getProjectInsightResponseId).collect(Collectors.toList());
				List<TagMaster> dbTagMasterResponse = tagMasterRepository.findByEntityIdInAndEntityTypeAndType(projectInsightResponseIdList, "Response", "user");
				tagMasterRepository.deleteAll(dbTagMasterResponse);
			}
			 projectInsightResponseRepository.deleteAllProjectInsightResponseByQuestionMasterId(questionId);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private void deleteProjectInsightQuestion(Long questionId,String entityType) {
		try {
			entityType = entityType.equals(null) ? "Question" : entityType;
			deleteResponseById(questionId);
			 projectInsightAssigneesRepository.deleteAllProjectInsightAssigneesByEntityIdAndEntityType(questionId,entityType);
			 questionMasterRepository.deleteAllQuestionsByEntityIdAndEntityType(questionId);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void deleteProjectInsightSubModuleAndItsChild(Long subModuleId, String subModuleType) {
		try {
			List<QuestionMaster> questionList = questionMasterRepository.findByEntityIdAndEntityType(subModuleId,subModuleType);
			if (questionList != null && !questionList.isEmpty()) {
				for (QuestionMaster questionMaster : questionList) {
					deleteProjectInsightQuestion(questionMaster.getQuestionMasterId(),subModuleType);
				}
			}
		
			List<ProjectInsightSubModule> projectInsightSubSubModuleList = projectInsightSubModuleRepository.getByModuleIdAndSubModuleType(subModuleId, subModuleType);
			if (projectInsightSubSubModuleList != null && !projectInsightSubSubModuleList.isEmpty()) {
				for (ProjectInsightSubModule projectInsightSubModule : projectInsightSubSubModuleList) {
					deleteProjectInsightSubModuleAndItsChild(projectInsightSubModule.getSubmoduleId(),subModuleType);
				}
			}
			projectInsightSubModuleRepository.deleteBySubModuleIdAndSubModuleType(subModuleId, subModuleType);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void deleteProjectInsightModuleAndItsChild(Long entityId) {
		try {
			List<QuestionMaster> questionList = questionMasterRepository.findByEntityIdAndEntityType(entityId,"Module");
			if (questionList != null && !questionList.isEmpty()) {
				for (QuestionMaster questionMaster : questionList) {
					deleteProjectInsightQuestion(questionMaster.getQuestionMasterId(),"Module");
				}
			}

			List<ProjectInsightSubModule> projectInsightSubModuleList = projectInsightSubModuleRepository.findByModuleId(entityId);
			if (projectInsightSubModuleList != null && !projectInsightSubModuleList.isEmpty()) {
				for (ProjectInsightSubModule projectInsightSubModule : projectInsightSubModuleList) {
					deleteProjectInsightSubModuleAndItsChild(projectInsightSubModule.getSubmoduleId(), "SubModule");
				}
			}
			projectInsightModuleRepository.deleteProjectInsightModuleByModuleId(entityId);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void deleteProjectInsightMilestoneAndItsChild(Long entityId) {
		try {
			List<QuestionMaster> questionList = questionMasterRepository.findByEntityIdAndEntityType(entityId,"Milestone");
			if (questionList != null && !questionList.isEmpty()) {
				for (QuestionMaster questionMaster : questionList) {
					 deleteProjectInsightQuestion(questionMaster.getQuestionMasterId(),"Milestone");
				}
			}
			List<ProjectInsightModule> projectInsightModuleList = projectInsightModuleRepository.findByMilestoneId(entityId);
			if (projectInsightModuleList != null && !projectInsightModuleList.isEmpty()) {
				for (ProjectInsightModule projectInsightModule : projectInsightModuleList) {
					deleteProjectInsightModuleAndItsChild(projectInsightModule.getModuleId());
				}
			}
			projectInsightMilestoneRepository.deleteProjectInsightMilestoneByMilestoneId(entityId);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void saveOrUpdateSubModuleList(List<SubModuleDTO> subModuleList, Long moduleId, Long updatedBy, String subModuleType, StringBuilder combinedText) {
		try {
			if (subModuleList != null && !subModuleList.isEmpty()) {
				for (SubModuleDTO subModuleDTO : subModuleList) {
					
					combinedText.append(subModuleDTO.getDescription()).append(" ")
					.append(subModuleDTO.getSubModule());
					
					
					if (subModuleDTO.getSubModuleId() != null) {
						ProjectInsightSubModule submoduleDbResponse = projectInsightSubModuleRepository.findById(subModuleDTO.getSubModuleId()).orElse(null);
						if (submoduleDbResponse != null) {
							submoduleDbResponse.setModuleId(moduleId);
							submoduleDbResponse.setDescription(subModuleDTO.getDescription());
							submoduleDbResponse.setRedmineId(subModuleDTO.getRedmineId());
							submoduleDbResponse.setSubmodule(subModuleDTO.getSubModule());
							submoduleDbResponse.setUpdatedBy(updatedBy);
							submoduleDbResponse.setSubModuleType(subModuleType);
							projectInsightSubModuleRepository.save(submoduleDbResponse);
							saveAssignedTo(subModuleDTO.getAssignedToUserId(), subModuleDTO.getSubModuleId(),subModuleType, true);

							// Add SubModule question
							if (subModuleDTO.getQuestionList() != null && !subModuleDTO.getQuestionList().isEmpty()) {
								addUpdateQuestion(subModuleDTO.getQuestionList(), subModuleDTO.getSubModuleId(), subModuleType, combinedText);
							}

							if (subModuleDTO.getSubSubModuleList() != null && !subModuleDTO.getSubSubModuleList().isEmpty()) {
								saveOrUpdateSubModuleList(subModuleDTO.getSubSubModuleList(), submoduleDbResponse.getSubmoduleId(), updatedBy, "Sub-SubModule", combinedText);
							}
						} else {
							ProjectInsightSubModule newProjectInsightSubModule = new ProjectInsightSubModule();
							newProjectInsightSubModule.setCreatedBy(subModuleDTO.getCreatedBy());
							newProjectInsightSubModule.setModuleId(moduleId);
							newProjectInsightSubModule.setDescription(subModuleDTO.getDescription());
							newProjectInsightSubModule.setRedmineId(subModuleDTO.getRedmineId());
							newProjectInsightSubModule.setSubmodule(subModuleDTO.getSubModule());
							newProjectInsightSubModule.setSubModuleType(subModuleType);
							ProjectInsightSubModule projectInsightSubModule = projectInsightSubModuleRepository.save(newProjectInsightSubModule);
							saveAssignedTo(subModuleDTO.getAssignedToUserId(), projectInsightSubModule.getSubmoduleId(), subModuleType, true);

							// Add SubModule question
							if (subModuleDTO.getQuestionList() != null && !subModuleDTO.getQuestionList().isEmpty()) {
								addUpdateQuestion(subModuleDTO.getQuestionList(), projectInsightSubModule.getSubmoduleId(), subModuleType, combinedText);
							}

							if (subModuleDTO.getSubSubModuleList() != null && !subModuleDTO.getSubSubModuleList().isEmpty()) {
								saveOrUpdateSubModuleList(subModuleDTO.getSubSubModuleList(),	projectInsightSubModule.getSubmoduleId(), updatedBy, "Sub-SubModule", combinedText);
							}
						}
					} else {
						ProjectInsightSubModule newProjectInsightSubModule = new ProjectInsightSubModule();
						newProjectInsightSubModule.setCreatedBy(subModuleDTO.getCreatedBy());
						newProjectInsightSubModule.setModuleId(moduleId);
						newProjectInsightSubModule.setDescription(subModuleDTO.getDescription());
						newProjectInsightSubModule.setRedmineId(subModuleDTO.getRedmineId());
						newProjectInsightSubModule.setSubmodule(subModuleDTO.getSubModule());
						newProjectInsightSubModule.setSubModuleType(subModuleType);
						ProjectInsightSubModule projectInsightSubModule = projectInsightSubModuleRepository.save(newProjectInsightSubModule);

						saveAssignedTo(subModuleDTO.getAssignedToUserId(), projectInsightSubModule.getSubmoduleId(), subModuleType, true);

						// Add SubModule question
						if (subModuleDTO.getQuestionList() != null && !subModuleDTO.getQuestionList().isEmpty()) {
							addUpdateQuestion(subModuleDTO.getQuestionList(), projectInsightSubModule.getSubmoduleId(), subModuleType, combinedText);
						}
						if (subModuleDTO.getSubSubModuleList() != null && !subModuleDTO.getSubSubModuleList().isEmpty()) {
							saveOrUpdateSubModuleList(subModuleDTO.getSubSubModuleList(), projectInsightSubModule.getSubmoduleId(), updatedBy, "Sub-SubModule", combinedText);
						}
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private ProjectInsightModule saveOrUpdateModule(ModuleDTO moduleDTO, Long milestoneId, StringBuilder combinedText, Long createdBy, Long updatedBy) {
		try {
			ProjectInsightModule projectInsightModule = null;
			if (moduleDTO.getModuleId() == null || (moduleDTO.getActionType() != null  && moduleDTO.getActionType().equalsIgnoreCase("Add"))) {
				projectInsightModule = saveProjectModule(projectInsightModule, moduleDTO, milestoneId, createdBy, updatedBy);
			} else if (moduleDTO.getModuleId() != null) {
				ProjectInsightModule moduleDbObject = projectInsightModuleRepository.getByModuleId(moduleDTO.getModuleId());
				if (moduleDbObject != null) {
					projectInsightModule = saveProjectModule(moduleDbObject, moduleDTO, milestoneId, createdBy,updatedBy);
				} else {
					projectInsightModule = saveProjectModule(projectInsightModule, moduleDTO, milestoneId, createdBy, updatedBy);
				}
			}
			if (projectInsightModule != null) {
				projectInsightModule = projectInsightModuleRepository.save(projectInsightModule);
			}
			return projectInsightModule;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private ProjectInsightModule saveProjectModule(ProjectInsightModule projectInsightModule, ModuleDTO moduleDTO, Long milestoneId, Long createdBy, Long updatedBy) {
		try {
			if(projectInsightModule == null) {
				projectInsightModule = new ProjectInsightModule();
			}
			projectInsightModule.setMilestoneId(milestoneId);
			projectInsightModule.setDescription(moduleDTO.getDescription());
			projectInsightModule.setRedmineId(moduleDTO.getRedmineId());
			projectInsightModule.setModule(moduleDTO.getModule());
			projectInsightModule.setCreatedBy(createdBy);
			projectInsightModule.setUpdatedBy(updatedBy);
			projectInsightModule.setUpdatedOn(LocalDateTime.now());
			return projectInsightModule;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void saveOrUpdateSubModuleList(List<SubModuleDTO> subModuleList, Long moduleId, String subModuleType,StringBuilder combinedText, Long createdBy, Long updatedBy) {
		try {
			if (subModuleList != null && !subModuleList.isEmpty()) {
				for (SubModuleDTO subModuleDTO : subModuleList) {
					combinedText.append(subModuleDTO.getDescription()).append(" ").append(subModuleDTO.getSubModule());
					ProjectInsightSubModule projectInsightSubModule = null;
					if (subModuleDTO.getSubModuleId() == null || (subModuleDTO.getActionType() != null && subModuleDTO.getActionType().equalsIgnoreCase("Add"))) {
						projectInsightSubModule = saveSubModule(projectInsightSubModule, subModuleDTO, moduleId, subModuleType, combinedText, createdBy, updatedBy);
					} else if (subModuleDTO.getSubModuleId() != null) {
						ProjectInsightSubModule submoduleDbResponse = projectInsightSubModuleRepository.findById(subModuleDTO.getSubModuleId()).orElse(null);
						if (submoduleDbResponse != null) {
							projectInsightSubModule = saveSubModule(submoduleDbResponse, subModuleDTO, moduleId, subModuleType, combinedText, createdBy, updatedBy);
						} else {
							projectInsightSubModule = saveSubModule(projectInsightSubModule, subModuleDTO, moduleId, subModuleType, combinedText, createdBy, updatedBy);
						}
					}
					if (projectInsightSubModule != null) {
						projectInsightSubModule = projectInsightSubModuleRepository.save(projectInsightSubModule);
						if (projectInsightSubModule.getSubmoduleId() != null) {
							Long subModuleId = projectInsightSubModule.getSubmoduleId();
							saveAssignedTo(subModuleDTO.getAssignedToUserId(), subModuleId, subModuleType, true);

							// Add SubModule question
							if (subModuleDTO.getQuestionList() != null && !subModuleDTO.getQuestionList().isEmpty()) {
								addUpdateQuestion(subModuleDTO.getQuestionList(), subModuleId, subModuleType, combinedText);
							}

							if (subModuleDTO.getSubSubModuleList() != null && !subModuleDTO.getSubSubModuleList().isEmpty()) {
								saveOrUpdateSubModuleList(subModuleDTO.getSubSubModuleList(), subModuleId, "Sub-SubModule", combinedText, createdBy, updatedBy);
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

	private ProjectInsightSubModule saveSubModule(ProjectInsightSubModule projectInsightSubModule, SubModuleDTO subModuleDTO, Long moduleId, String subModuleType, StringBuilder combinedText, Long createdBy,
			Long updatedBy) {
		try {
			if (projectInsightSubModule == null) {
				projectInsightSubModule = new ProjectInsightSubModule();
			}

			projectInsightSubModule.setSubmodule(subModuleDTO.getSubModule());
			projectInsightSubModule.setModuleId(moduleId);
			projectInsightSubModule.setDescription(subModuleDTO.getDescription());
			projectInsightSubModule.setRedmineId(subModuleDTO.getRedmineId());
			projectInsightSubModule.setSubModuleType(subModuleType);
			projectInsightSubModule.setCreatedBy(subModuleDTO.getCreatedBy());
			projectInsightSubModule.setUpdatedBy(updatedBy);
			return projectInsightSubModule;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
//	----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// Project Response Methods
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
				projectInsightDTODbObject.setQuestionList(getProjectQuestionDTOList(projectInsightDTO.getProjectId(), "Project",projectInsightDTO.getEmpId(), isEmployee, projectInsightDTO.getPerformanceTabName(), projectInsightDTO.getProjectId(),null));
				projectInsightDTODbObject.setTaggedToUserNames(getAllTaggedUserName(projectInsightDTO.getProjectId(), "Project",projectInsightDTO.getEmpId()));
				projectInsightDTODbObject.setTaggedToUserId(getAllTaggedUserId(projectInsightDTO.getProjectId(), "Project",projectInsightDTO.getEmpId()));
				projectInsightDTODbObject.setProjectId(projectInsightDTO.getProjectId());
				projectInsightDTODbObject.setProjectManagerId(project.getProjectManagerId());
				projectInsightDTODbObject.setProjectManagerName(getProjectManagerName(project.getProjectManagerId()));
				projectInsightDTODbObject.setProjectName(project.getProjectName());
				projectInsightDTODbObject.setIsFinalSubmitted(getIsFinalResponseSubmittedByUserForProject(projectInsightDTO.getEmpId(),projectInsightDTO.getProjectId()));
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

	private String getIsFinalResponseSubmittedByUserForProject(Long empId, Long projectId) {
		try {
			ProjectInsightResponseMetadata projectInsightResponseMetadata = projectInsightResponseMetadataRepository.findByResponseByAndProjectId(empId, projectId);
			return projectInsightResponseMetadata != null ? projectInsightResponseMetadata.getIsFinalSubmitted() != null  ? projectInsightResponseMetadata.getIsFinalSubmitted() : "N" : "N";
		} catch (Exception e) {
		  e.printStackTrace();
		  throw e;
	  }
	}

	private ProjectInsightDTO createProjectInsightMileStoneObject(List<ProjectInsightMilestone> projectInsightMilestoneList, Long employeeId, boolean isEmployee, String performanceTabName, Long projectId) {
		ProjectInsightDTO projectInsightDTO = new ProjectInsightDTO();
		try {
			if (projectInsightMilestoneList != null && !projectInsightMilestoneList.isEmpty()) {
				List<ProjectInsightMilestoneDTO> projectInsightMilestoneDTOList = new ArrayList<>();
				for (ProjectInsightMilestone projectInsightMilestone : projectInsightMilestoneList) {
					ProjectInsightMilestoneDTO projectInsightMilestoneDTO = mapProjectMilestoneToDTO(projectInsightMilestone, employeeId, isEmployee, performanceTabName, projectId);
					if ((projectInsightMilestoneDTO.getModuleList() != null && !projectInsightMilestoneDTO.getModuleList().isEmpty()) 
							|| (projectInsightMilestoneDTO.getQuestionList() != null && !projectInsightMilestoneDTO.getQuestionList().isEmpty())) {
						projectInsightMilestoneDTOList.add(projectInsightMilestoneDTO);
					}
				}
				projectInsightDTO.setProjectInsightMilestoneList(projectInsightMilestoneDTOList);
			} 
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return projectInsightDTO;
	}
	
	private ProjectInsightMilestoneDTO mapProjectMilestoneToDTO(ProjectInsightMilestone projectInsightMilestone, Long employeeId, boolean isEmployee, String performanceTabName, Long projectId) {
		try {
			ProjectInsightMilestoneDTO projectInsightMilestoneDTO = new ProjectInsightMilestoneDTO();
			projectInsightMilestoneDTO.setMilestoneId(projectInsightMilestone.getMilestoneId());
			projectInsightMilestoneDTO.setMilestone(projectInsightMilestone.getMilestone());
			projectInsightMilestoneDTO.setDescription(projectInsightMilestone.getDescription());
			projectInsightMilestoneDTO.setDeptId(projectInsightMilestone.getDeptId());
			projectInsightMilestoneDTO.setAssignedToUserId(getAssignedToUserId(projectInsightMilestone.getMilestoneId(), "Milestone"));
			projectInsightMilestoneDTO.setAssignedToUserNames(getAssignedToUserName(projectInsightMilestone.getMilestoneId(), "Milestone"));
			projectInsightMilestoneDTO.setTaggedToUserNames(getAllTaggedUserName(projectInsightMilestone.getMilestoneId(), "Milestone",employeeId));
			projectInsightMilestoneDTO.setTaggedToUserId(getAllTaggedUserId(projectInsightMilestone.getMilestoneId(), "Milestone",employeeId));
			projectInsightMilestoneDTO.setQuestionList(getProjectQuestionDTOList(projectInsightMilestone.getMilestoneId(), "Milestone",employeeId, isEmployee, performanceTabName, projectId,null));
			projectInsightMilestoneDTO.setModuleList(getProjectInsightModuleDTOList(projectInsightMilestone.getMilestoneId(), employeeId, isEmployee, performanceTabName, projectId));
			return projectInsightMilestoneDTO;
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
					moduleDTO.setAssignedToUserId(getAssignedToUserId(projectInsightModule.getModuleId(), "Module"));
					moduleDTO.setAssignedToUserNames(getAssignedToUserName(projectInsightModule.getModuleId(), "Module"));
					moduleDTO.setTaggedToUserNames(getAllTaggedUserName(projectInsightModule.getModuleId(), "Module",employeeId));
					moduleDTO.setTaggedToUserId(getAllTaggedUserId(projectInsightModule.getModuleId(), "Module",employeeId));
					moduleDTO.setQuestionList(getProjectQuestionDTOList(projectInsightModule.getModuleId(), "Module", employeeId, isEmployee, performanceTabName, projectId,null));
					moduleDTO.setSubModuleList(getProjectInsightSubModuleDTOList(projectInsightModule.getModuleId(), employeeId, isEmployee, performanceTabName, projectId,"SubModule"));
					if ((moduleDTO.getSubModuleList() != null && !moduleDTO.getSubModuleList().isEmpty()) 
							|| (moduleDTO.getQuestionList() != null && !moduleDTO.getQuestionList().isEmpty())) {
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

	private List<SubModuleDTO> getProjectInsightSubModuleDTOList(Long moduleId, Long employeeId, boolean isEmployee, String performanceTabName, Long projectId,String subModuleType) {
		List<SubModuleDTO> projectInsightSubModuleDTOList = new ArrayList<>();
		try {
			List<ProjectInsightSubModule> projectInsightSubModuleList = projectInsightSubModuleRepository.getByModuleIdAndSubModuleType(moduleId,subModuleType);
			if (!projectInsightSubModuleList.isEmpty()) {
				for (ProjectInsightSubModule projectInsightSubModule : projectInsightSubModuleList) {
					SubModuleDTO subModuleDTO = new SubModuleDTO();
					subModuleDTO.setSubModuleId(projectInsightSubModule.getSubmoduleId());
					subModuleDTO.setSubModule(projectInsightSubModule.getSubmodule());
					subModuleDTO.setModuleId(projectInsightSubModule.getModuleId());
					subModuleDTO.setDescription(projectInsightSubModule.getDescription());
					subModuleDTO.setRedmineId(projectInsightSubModule.getRedmineId());
					subModuleDTO.setAssignedToUserId(getAssignedToUserId(projectInsightSubModule.getSubmoduleId(),subModuleType));
					subModuleDTO.setAssignedToUserNames(getAssignedToUserName(projectInsightSubModule.getSubmoduleId(),subModuleType));
					subModuleDTO.setTaggedToUserNames(getAllTaggedUserName(projectInsightSubModule.getSubmoduleId(),subModuleType,employeeId));
					subModuleDTO.setTaggedToUserId(getAllTaggedUserId(projectInsightSubModule.getSubmoduleId(), subModuleType,employeeId));
					subModuleDTO.setQuestionList(getProjectQuestionDTOList(projectInsightSubModule.getSubmoduleId(), "SubModule", employeeId, isEmployee, performanceTabName, projectId,subModuleType));
					
					List<ProjectInsightSubModule> projectInsightSubSubModuleList = projectInsightSubModuleRepository.getByModuleIdAndSubModuleType(projectInsightSubModule.getSubmoduleId(),"Sub-SubModule");
					if(projectInsightSubSubModuleList != null && !projectInsightSubSubModuleList.isEmpty()) {
						subModuleDTO.setSubSubModuleList(getProjectInsightSubModuleDTOList(projectInsightSubModule.getSubmoduleId(), employeeId, isEmployee, performanceTabName, projectId,"Sub-SubModule"));
					}
					
					if ((subModuleDTO.getQuestionList() != null && !subModuleDTO.getQuestionList().isEmpty()) || (projectInsightSubSubModuleList != null && !projectInsightSubSubModuleList.isEmpty())) {
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

	public List<ProjectQuestionDTO> getProjectQuestionDTOList(Long entityId, String entityType, Long employeeId, boolean isEmployee, String performanceTabName, Long projectId,String subModuleType) {
		List<ProjectQuestionDTO> projectInsightQuestionList = new ArrayList<>();
		try {
			List<QuestionMaster> projectInsightQuestionMasterList = new ArrayList<>();
			if (isEmployee) {
				projectInsightQuestionMasterList = questionMasterRepository.findByEntityIdAndEntityTypeAndAssignedTo(entityId, entityType, employeeId);
			} else {
				entityType = subModuleType  != null ? subModuleType :entityType ;
				projectInsightQuestionMasterList = questionMasterRepository.findByEntityIdAndEntityType(entityId, entityType);
			}
			if (!projectInsightQuestionMasterList.isEmpty()) {
				for (QuestionMaster questionMaster : projectInsightQuestionMasterList) {
					ProjectQuestionDTO projectQuestionDTO = mapQuestionMasterToProjectQuestionDTO(questionMaster);
					List<ProjectResponseDTO> projectResponseDTOList = new ArrayList<>();
					
					ProjectInsightResponseMetadata  projectInsightResponseMetadata = projectInsightResponseMetadataRepository.findProjectInsightResponseMetadataByResponseByAndProjectId(employeeId,projectId);
					if(projectInsightResponseMetadata!= null) {
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
					}
					
					projectQuestionDTO.setProjectResponseList(projectResponseDTOList);
					projectQuestionDTO.setRecommendedResponseId(questionMaster.getRecommendedResponseId());
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
			if (!performanceTabName.equals("Teams Dashboard")) {
				return projectInsightResponseRepository.findByQuestionMasterIdAndEmpId(questionMasterId, employeeId);
			}
			List<EmployeeTeamMap> employeeTeamList = employeeTeamMapRepository.findByTeamIdAndIsActive(employeeId, 1l,Integer.parseInt(projectId.toString()));

			if (employeeTeamList == null || employeeTeamList.isEmpty()) {
				return  projectInsightResponseRepository.findAllByQuestionMasterId(questionMasterId);
			}
			Map<Long, String> employeeTeamMapObj = getEmpIdToHighestRoleMap(employeeTeamList);
			String employeeRole = employeeTeamMapObj.getOrDefault(employeeId, null);

			if (employeeRole != null && (employeeRole.equals("HOD") || employeeRole.equals("SUPERADMIN") || employeeRole.equals("RMG") || employeeRole.equals("HR"))) {
				projectInsightResponseList = projectInsightResponseRepository.findAllByQuestionMasterId(questionMasterId);
			} else if (employeeRole != null && !employeeRole.equals("HOD")  && !employeeRole.equals("SUPERADMIN") && !employeeRole.equals("RMG") && !employeeRole.equals("HR")) {
				List<Long> seniorEmployeeIdList = getSeniorEmployeeList(employeeTeamMapObj, employeeRole);
				if (seniorEmployeeIdList.isEmpty()) {
					seniorEmployeeIdList.add(-1l);
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
			projectResponseDTO.setProjectInsightResponseId(projectInsightResponse.getProjectInsightResponseId());
			projectResponseDTO.setOptions(options);
			projectResponseDTO.setResponse(projectInsightResponse.getResponse());
			projectResponseDTO.setResponseBy(projectInsightResponse.getResponseBy());
			projectResponseDTO.setResponseByEmpName(getProjectManagerName(projectInsightResponse.getResponseBy()));
			projectResponseDTO.setDocumentFileName(projectInsightResponse.getDocumentFileName());
			projectResponseDTO.setDocumentPath(projectInsightResponse.getDocumentPath());
			projectResponseDTO.setResponseType(projectInsightResponse.getResponseType());
			projectResponseDTO.setIsFinalSubmitted(projectInsightResponse.getIsFinalSubmitted());
			projectResponseDTO.setTags(getAllTagsForResponse(projectInsightResponse.getProjectInsightResponseId()));
			projectResponseDTO.setProjectInsightResponsePointList(getResponsePointDTOList(projectInsightResponse.getProjectInsightResponseId()));
			projectResponseDTO.setApprovedForKnowledgeHub( projectInsightResponse.getIsApprovedForKnowledgeHub()!= null&&  projectInsightResponse.getIsApprovedForKnowledgeHub().equals("Y") ? true :  false );
			return projectResponseDTO;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private List<String> getAllTagsForResponse(Long projectInsightResponseId) {
		List<String> tagsList = new ArrayList<String>();
		try {
			List<TagMaster> dbTagMasterResponse = tagMasterRepository.findByEntityIdAndEntityTypeAndType(projectInsightResponseId, "Response", "user");
			if (dbTagMasterResponse != null && !dbTagMasterResponse.isEmpty()) {
				tagsList = dbTagMasterResponse.stream().map(TagMaster::getTag).collect(Collectors.toList());
			}
			return tagsList;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private List<ProjectInsightResponsePointsDTO> getResponsePointDTOList(Long projectInsightResponseId) {
		try {
			List<ProjectInsightResponsePointsDTO> projectInsightResponsePointDTOList = new ArrayList<ProjectInsightResponsePointsDTO>();
			List<ProjectInsightResponsePoints> projectInsightResponsePointList = projectInsightResponsePointsRepository.findAllProjectInsightResponsePointsByResponseId(projectInsightResponseId);
			if (projectInsightResponsePointList != null && !projectInsightResponsePointList.isEmpty()) {
				for (ProjectInsightResponsePoints projectInsightResponsePoints : projectInsightResponsePointList) {
					ProjectInsightResponsePointsDTO projectInsightResponsePointsDTO = new ProjectInsightResponsePointsDTO();
					projectInsightResponsePointsDTO.setProjectInsightResponsePointId(projectInsightResponsePoints.getProjectInsightResponsePointId());
					projectInsightResponsePointsDTO.setResponseId(projectInsightResponseId);
					projectInsightResponsePointsDTO.setPoints(projectInsightResponsePoints.getPoints());
					projectInsightResponsePointsDTO.setPointsBy(projectInsightResponsePoints.getPointsBy());
					projectInsightResponsePointsDTO.setPointsByName(getProjectManagerName(projectInsightResponsePoints.getPointsBy()));
					projectInsightResponsePointsDTO.setIsPointsDrafted(projectInsightResponsePoints.getIsPointsDrafted());
					projectInsightResponsePointsDTO.setFinalSubmittedOn(projectInsightResponsePoints.getFinalSubmittedOn());
					projectInsightResponsePointDTOList.add(projectInsightResponsePointsDTO)	;
				}
			}
			return projectInsightResponsePointDTOList;
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
			StringBuilder combinedText = new StringBuilder();

			if (projectInsightDTO.getProjectId() == null) {
				throw new IllegalArgumentException("Project Id cannot be null!!");
			}

			Project project = projectRepository.findByProjectId(Integer.parseInt(projectInsightDTO.getProjectId().toString()));
			if (project == null) {
				throw new RuntimeException("Project Not Found!!");
			}
			
			combinedText.append(project.getProjectName()).append(" ");		
			
			Long reviewerId = getReviewerIdForQuestion(projectInsightDTO.getProjectId(), projectInsightDTO.getEmpId());
			
			ProjectInsightResponseMetadata projectInsightResponseMetadata = saveProjectInsightResponseMetadata(projectInsightDTO.getIsFinalSubmitted(),projectInsightDTO.getResponseBy() , projectInsightDTO.getProjectId() , reviewerId);
			
			if (projectInsightDTO.getProjectInsightMilestoneList() != null && !projectInsightDTO.getProjectInsightMilestoneList().isEmpty()) {
				saveProjectResponse(projectInsightDTO.getQuestionList(), projectInsightDTO.getResponseBy(), files, projectInsightResponseMetadata.getProjectInsightResponseMetadataId(), projectInsightDTO.getProjectId(), combinedText);
				saveProjectMileStoneResponse(projectInsightDTO.getProjectInsightMilestoneList(), projectInsightDTO.getResponseBy(), files, projectInsightResponseMetadata.getProjectInsightResponseMetadataId(), combinedText);
				saveTaggedForHelp(projectInsightDTO.getTaggedToUserId(), projectInsightDTO.getProjectId(), "Project", projectInsightDTO.getEmpId());
				
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
	
	
	private ProjectInsightResponseMetadata saveProjectInsightResponseMetadata(String isFinalSubmitted, Long responseBy, Long projectId, Long reviewerId) {
		ProjectInsightResponseMetadata projectInsightResponseMetadata = null;
		try {
			projectInsightResponseMetadata = projectInsightResponseMetadataRepository.findByResponseByAndProjectId(responseBy, projectId);
			if (projectInsightResponseMetadata != null) {
				projectInsightResponseMetadata.setUpdatedOn(new Timestamp(System.currentTimeMillis()));
				projectInsightResponseMetadata.setIsFinalSubmitted(isFinalSubmitted);
				if (isFinalSubmitted.equals("Y")) {
					projectInsightResponseMetadata.setFinalSubmittedOn(new Timestamp(System.currentTimeMillis()));
				}
				if (projectInsightResponseMetadata.getReviewerId() == null) {
					projectInsightResponseMetadata.setReviewerId(reviewerId);
				}
			} else {
				projectInsightResponseMetadata = new ProjectInsightResponseMetadata();
				projectInsightResponseMetadata.setResponseBy(responseBy);
				projectInsightResponseMetadata.setProjectId(projectId);
				projectInsightResponseMetadata.setReviewerId(reviewerId);
				projectInsightResponseMetadata.setIsFinalSubmitted(isFinalSubmitted);
				if (isFinalSubmitted.equals("Y")) {
					projectInsightResponseMetadata.setFinalSubmittedOn(new Timestamp(System.currentTimeMillis()));
				}
			}
			projectInsightResponseMetadataRepository.save(projectInsightResponseMetadata);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return projectInsightResponseMetadata;
	}

	private void saveProjectResponse(List<ProjectQuestionDTO> questionList, Long empId, List<MultipartFile> files, Long projectInsightResponseMetadataId, Long projectId, StringBuilder combinedText) {
		try {
			if (questionList != null && !questionList.isEmpty()) {
			saveProjectInsightResponse(questionList, files, projectInsightResponseMetadataId, empId, combinedText);
				for (ProjectQuestionDTO projectQuestionDTO : questionList) {
					saveTaggedForHelp(projectQuestionDTO.getTaggedForHelp(), projectQuestionDTO.getQuestionId(), "Question", empId);
				}
			} 
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private String saveProjectMileStoneResponse(List<ProjectInsightMilestoneDTO> projectInsightMilestoneDTOList, Long responseBy, List<MultipartFile> files, Long projectInsightResponseMetadata, StringBuilder combinedText) {
		String response = null;
		try {
			if (projectInsightMilestoneDTOList != null && !projectInsightMilestoneDTOList.isEmpty()) {
				for (ProjectInsightMilestoneDTO projectInsightQuestionDTO : projectInsightMilestoneDTOList) {
					saveProjectInsightResponse(projectInsightQuestionDTO.getQuestionList(), files, projectInsightResponseMetadata,responseBy, combinedText);
					saveProjectInsightModuleResponse(projectInsightQuestionDTO.getModuleList(), responseBy, files, projectInsightResponseMetadata, combinedText);
					saveTaggedForHelp(projectInsightQuestionDTO.getTaggedToUserId(),projectInsightQuestionDTO.getMilestoneId(),"Milestone",responseBy);
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

	private void saveProjectInsightModuleResponse(List<ModuleDTO> moduleList, Long responseBy,List<MultipartFile> files,Long projectInsightResponseMetadata, StringBuilder combinedText) {
		try {
			if (moduleList != null && !moduleList.isEmpty()) {
				for (ModuleDTO moduleDTO : moduleList) {
					saveProjectInsightResponse(moduleDTO.getQuestionList(),files,projectInsightResponseMetadata,responseBy,combinedText);
					saveProjectInsightSubModuleResponse(moduleDTO.getSubModuleList(), responseBy,files,projectInsightResponseMetadata,"SubModule",combinedText);
					saveTaggedForHelp(moduleDTO.getTaggedToUserId(),moduleDTO.getModuleId(),"Module",responseBy);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void saveProjectInsightSubModuleResponse(List<SubModuleDTO> subModuleList, Long responseBy,List<MultipartFile> files,Long projectInsightResponseMetadata,String subModuleType, StringBuilder combinedText) {
		try {
			if (subModuleList != null && !subModuleList.isEmpty()) {
				for (SubModuleDTO subModuleDTO : subModuleList) {
					saveProjectInsightResponse(subModuleDTO.getQuestionList(),files,projectInsightResponseMetadata,responseBy,combinedText);
					saveTaggedForHelp(subModuleDTO.getTaggedToUserId(),subModuleDTO.getSubModuleId(),subModuleType,responseBy);
					if(subModuleDTO.getSubSubModuleList() != null && !subModuleDTO.getSubSubModuleList().isEmpty()) {
						saveProjectInsightSubModuleResponse(subModuleDTO.getSubSubModuleList(), responseBy, files, projectInsightResponseMetadata,"Sub-SubModule",combinedText);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void saveProjectInsightResponse(List<ProjectQuestionDTO> projectQuestionList, List<MultipartFile> files, Long projectInsightResponseMetadataId, Long responseBy, StringBuilder combinedText) {
		try {
			if (projectQuestionList != null && !projectQuestionList.isEmpty()) {
				for (ProjectQuestionDTO projectQuestionDTO : projectQuestionList) {
					if (projectQuestionDTO.getQuestionId() != null && projectQuestionDTO.getProjectResponseList() != null && !projectQuestionDTO.getProjectResponseList().isEmpty()) {
						for (ProjectResponseDTO projectResponseDTO : projectQuestionDTO.getProjectResponseList()) {
							if(projectResponseDTO.getResponse() != null && !projectResponseDTO.getResponse().equals("[]") ) {
								saveOrUpdateAllProjectResponseAndUploadDocument(projectQuestionDTO, projectResponseDTO, files, projectInsightResponseMetadataId, combinedText, responseBy);
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

	private void saveOrUpdateAllProjectResponseAndUploadDocument(ProjectQuestionDTO projectQuestionDTO, ProjectResponseDTO projectResponseDTO, List<MultipartFile> files, Long projectInsightResponseMetadataId, StringBuilder combinedText,Long responseBy) {
		try {
			ProjectInsightResponse projectInsightResponse = projectInsightResponseRepository.findByEmpIdAndQuestionMasterId(responseBy, projectQuestionDTO.getQuestionId());
			if (projectInsightResponse != null) {
				List<TagMaster> dbTagMasterResponse = tagMasterRepository.findByEntityIdAndEntityTypeAndType(projectInsightResponse.getProjectInsightResponseId(), "Response", "user");
				tagMasterRepository.deleteAll(dbTagMasterResponse);
				projectInsightResponse = mapProjectResponseDTOToProjectResponseAndUploadFile(projectInsightResponse,projectResponseDTO ,files,projectInsightResponseMetadataId,projectQuestionDTO.getQuestionId(), combinedText);
				if (files != null) {}
			} else {
				projectInsightResponse = new ProjectInsightResponse();
				projectInsightResponse = mapProjectResponseDTOToProjectResponseAndUploadFile(projectInsightResponse,projectResponseDTO ,files,projectInsightResponseMetadataId,projectQuestionDTO.getQuestionId(), combinedText);
			}
			ProjectInsightResponse projectInsightResponse2 = projectInsightResponseRepository.save(projectInsightResponse);
			if (projectResponseDTO.getTags() != null && !projectResponseDTO.getTags().isEmpty()
					&& projectInsightResponse2 != null && projectInsightResponse2.getProjectInsightResponseId() != null) {
				List<TagMaster> newUserDefinedTags = projectResponseDTO.getTags().stream().map(tagObj -> {
					TagMaster newObj = new TagMaster();
					newObj.setEntityId(projectInsightResponse2.getProjectInsightResponseId());
					newObj.setEntityType("Response");
					newObj.setTag(tagObj);
					newObj.setType("user");
					return newObj;
				}).collect(Collectors.toList());
				tagMasterRepository.saveAll(newUserDefinedTags);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private ProjectInsightResponse mapProjectResponseDTOToProjectResponseAndUploadFile(ProjectInsightResponse projectInsightResponse, ProjectResponseDTO projectResponseDTO,List<MultipartFile> files, Long projectInsightResponseMetadataId, Long questionMasterId, StringBuilder combinedText) {
		try {
			combinedText.append(projectResponseDTO.getResponse()).append(" ").append(projectResponseDTO.getDocumentFileName());
			projectInsightResponse.setProjectInsightResponseMetadataId(projectInsightResponseMetadataId);
			projectInsightResponse.setResponse(projectResponseDTO.getResponse());
			projectInsightResponse.setDocumentPath(projectResponseDTO.getDocumentPath());
			projectInsightResponse.setQuestionMasterId(questionMasterId);
			projectInsightResponse.setDocumentFileName(projectResponseDTO.getDocumentFileName());
			projectInsightResponse.setIsApprovedForKnowledgeHub(projectResponseDTO.isApprovedForKnowledgeHub() ? "Y" : "N");
			if (files == null) {
				return projectInsightResponse;
			}
			for (MultipartFile document : files) {
				if (document != null && document.getOriginalFilename() != null && projectResponseDTO.getDocumentFileName() != null
						&& document.getOriginalFilename().equals(projectResponseDTO.getDocumentFileName())) {
					if (projectInsightResponse.getDocumentFileName() != null && !projectInsightResponse.getDocumentFileName().trim().equals("")) {
						deleteExistingDocumentByFileName(projectInsightResponse.getDocumentFileName());
					}
					String uploadResponse = uploadProjectResponseDocument(document, questionMasterId, null);
					if (uploadResponse.equalsIgnoreCase("Document uploaded successfully")) {
						projectInsightResponse.setDocumentPath(projectResponseDTO.getDocumentPath());
						projectInsightResponse.setDocumentFileName(projectResponseDTO.getDocumentFileName());
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
	
	public String uploadProjectResponseDocument(MultipartFile document, Long entityIdOrQuestionMasterId, String entityType) {
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
				
				//Send document for OCR
				if(response.equalsIgnoreCase("Document uploaded successfully")) {
					Long entityId = null;
					String type = null;
					Long questionMasterId = null;
					Long projectId = null;
					
					if(entityType != null && entityType.equals("UserContribution")) {
						entityId = entityIdOrQuestionMasterId;
						type = entityType;
					}else {
						QuestionMaster questionMasterObject = questionMasterRepository.findByQuestionMasterId(entityIdOrQuestionMasterId);
						List<Object[]> projectInfoByQuestionMasterId = questionMasterRepository.getProjectInfoByQuestionMasterId(entityIdOrQuestionMasterId);
						
						if(questionMasterObject != null) {
							entityId = questionMasterObject.getEntityId();
							type = questionMasterObject.getEntityType();
							questionMasterId = entityIdOrQuestionMasterId;
						}
						if(!projectInfoByQuestionMasterId.isEmpty()) {
							projectId = ((Number) projectInfoByQuestionMasterId.get(0)[2]).longValue();
						}
					}
					
					tagUtils.sendFileForOCR(document, entityId, type, questionMasterId, projectId);
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
			if (projectInsightDTO.getPerformanceTabName().equals("Teams Dashboard")) {
				String jobRole = employeeRepository.getJobRoleByEmployeeId(projectInsightDTO.getEmpId());
				if (jobRole != null) {
					projectInsightDTO.setEmployeeRole(jobRole);
				}
			}

			if (projectInsightDTO.getPerformanceTabName().equals("Teams Dashboard")) {
				objectList = projectInsightResponseRepository.getAllProjectInsightForReviewByUser(projectInsightDTO.getEmpId());
			} else {
				if (projectInsightDTO.getEmployeeRole().equals("Employee")) {
					objectList = projectInsightResponseRepository.getAllProjectInsightByUser(projectInsightDTO.getEmpId());
					List<Long> projectIds = objectList.stream().map(obj -> parseLong(obj[0])).collect(Collectors.toList()); 
					if(projectIds == null || projectIds.isEmpty()) {
						projectIds.add(-1l);
					}
					List<Object[]> objectList2 = projectInsightMilestoneRepository.getAllProjectInsightByUserIdForEmployee(projectInsightDTO.getEmpId(),projectIds);
					if (objectList2 != null && !objectList2.isEmpty()) {
						objectList.addAll(objectList2);
					}
				} else {
					List<Long> projectIds  = new ArrayList<Long>();
					objectList = projectInsightMilestoneRepository.getAllProjectInsight();
					if(objectList != null && !objectList.isEmpty()) {
						projectIds =  objectList.stream().map(object -> parseLong(object[0])).collect(Collectors.toList());
					} else {
						projectIds.add(-1l);
					}
					objectList.addAll(questionMasterRepository.getAllProjectInsight(projectIds));
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
		return obj != null ? !obj.toString().equalsIgnoreCase("null") ?  Long.parseLong(obj.toString()) :null : null;
	}

	private String parseString(Object obj) {
		return obj != null ? !obj.toString().equalsIgnoreCase("null") ?  obj.toString() : null : null;
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
	
	private List<Long> getAssignedToUserId(Long entityId, String entityType) {
		try {
			return projectInsightAssigneesRepository.getAssignedToByEntityIdAndEntityType(entityId,entityType);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
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
			List<EmployeeTeamMap> employeeTeamList = employeeTeamMapRepository.findByTeamIdAndIsActive(employeeId, 1l, Integer.parseInt(projectId.toString()));
			if (employeeTeamList == null || employeeTeamList.isEmpty()) {
				return getEmployeesRMorManagerId(employeeId);
			}

			Map<Long, String> employeeTeamMapObj = getEmpIdToHighestRoleMap(employeeTeamList);
			String employeeRole = employeeTeamMapObj.getOrDefault(employeeId, null);
			if (employeeRole == null) {
				return getEmployeesRMorManagerId(employeeId);
			}
			else if(employeeRole.equals("SUPERADMIN") || employeeRole.equals("RMG") || employeeRole.equals("HR") || employeeRole.equals("HOD")) {
				return null;
			}

			int roleIndex = ROLE_HIERARCHY.indexOf(employeeRole);
			reviewerId = getNextReviewerId(roleIndex, employeeTeamMapObj);

			if (reviewerId == null) {
				reviewerId = getEmployeesRMorManagerId(employeeId);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return reviewerId;
	}
	
	private Long getNextReviewerId(int roleIndex, Map<Long, String> employeeTeamMapObj) {
		try {
			Long reviewerId = null;
			if (roleIndex + 1 < ROLE_HIERARCHY.size()) {
				String nextLevelRole = ROLE_HIERARCHY.get(roleIndex + 1);
				for (Map.Entry<Long, String> entry : employeeTeamMapObj.entrySet()) {
					if (entry.getValue() != null && entry.getValue().equalsIgnoreCase(nextLevelRole)) {
						reviewerId = entry.getKey();
						break;
					}
				}
				if (reviewerId == null) {
					reviewerId = getNextReviewerId(roleIndex + 1, employeeTeamMapObj);
				}
			} 
			return reviewerId;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public Map<Long, String> getEmpIdToHighestRoleMap(List<EmployeeTeamMap> employeeTeamMapList) {
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
	
	
	/*
	 * ------------------------------ Search impl -----------------------------------------
	 * */

	public ResponseEntity<SearchResultResponse> onSearchTerm(String search) {
		SearchResultResponse response = new SearchResultResponse();
		try {

			/*
			 * Note getSearchResultByEntityIdAndEntityType is configured in such a way that
			 * if entityid, entitytype passed which are present in Tagmaster as well as
			 * Filestorage then the reult will be in section i.e different section for
			 * project, milestone, module, submodule, subsubmodule
			 */

			List<String> tagList = nlpUtils.extractTags(search);

			if (tagList.isEmpty()) {
				return ResponseEntity.ok(response);
			}

			List<TagDTO> responseTagList = new ArrayList<>();
			List<TagMaster> matchedTags = tagMasterRepository.findAll(TagSpecifications.tagNameLikeAny(tagList));
//			List<FileStorage> files = fileMongoRepository.findByTagsIn(tagList);
			List<FileStorage> files = new ArrayList<>();

			Set<Long> projectIdSet = new HashSet<>();

			for (TagMaster tag : matchedTags) {
				Long projectId = tag.getProjectId();
				if (projectId != null) {
					projectIdSet.add(projectId);

					boolean alreadyExists = responseTagList.stream()
							.anyMatch(obj -> obj.getEntityId().equals(tag.getEntityId())
									&& obj.getEntityType().equals(tag.getEntityType()));

					if (!alreadyExists) {
						TagDTO newdto = new TagDTO();
						newdto.setEntityId(tag.getEntityId());
						newdto.setEntityType(tag.getEntityType());
						newdto.setProjectId(projectId);
						responseTagList.add(newdto);
					}
				}
			}

			for (FileStorage file : files) {
				Long projectId = file.getProjectId();
				if (projectId != null) {
					projectIdSet.add(projectId);

					boolean alreadyExists = responseTagList.stream()
							.anyMatch(tag -> tag.getEntityId().equals(file.getEntityId())
									&& tag.getEntityType().equals(file.getEntityType()));

					if (!alreadyExists) {
						TagDTO newdto = new TagDTO();
						newdto.setEntityId(file.getEntityId());
						newdto.setEntityType(file.getEntityType());
						newdto.setProjectId(projectId);
						responseTagList.add(newdto);
					}
				}
			}

			List<ProjectInsightDTO> projectRespList = new ArrayList<>();
			for (Long projectId : projectIdSet) {
				SearchResultResponse result = getSearchResultByEntityIdAndEntityType(projectId);
				if (result != null && result.getProject() != null) {
					projectRespList.add(result.getProject());
				}
			}

			response.setProjectList(projectRespList);
			response.setUserContributionList(getUserContributionSearchResult(matchedTags, files));
			response.setTagList(responseTagList);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ResponseEntity.ok(response);
	}

	private List<ProjectInsightUserContributionDTO> getUserContributionSearchResult(List<TagMaster> matchedTags,
			List<FileStorage> files) {
		List<ProjectInsightUserContributionDTO> response = new ArrayList<>();
		try {
			Set<Long> uniqueContributionIds = new HashSet<>();

			matchedTags.stream().filter(obj -> "UserContribution".equals(obj.getEntityType()))
					.map(TagMaster::getEntityId).forEach(uniqueContributionIds::add);

			files.stream().filter(obj -> "UserContribution".equals(obj.getEntityType())).map(FileStorage::getEntityId)
					.forEach(uniqueContributionIds::add);

			List<ProjectInsightUserContribution> userContributionsResult = projectInsightUserContributionRepository
					.findAllById(uniqueContributionIds);

			if (userContributionsResult != null && !userContributionsResult.isEmpty()) {
				List<Object[]> employeeList = employeeRepository.getEmployees();

				response = userContributionsResult.stream().map(object -> {
					List<UserContributionResponseRemarks> responseRemarkDbResp = userContributionResponseRemarksRepository
							.findByUserContributionIdAndRemarkStatus(object.getUserContributionId(), "Approve");

					if (responseRemarkDbResp == null || responseRemarkDbResp.isEmpty()) {
						return null;
					}

					List<ProjectInsightAssignees> allTaggedUser = projectInsightAssigneesRepository
							.getByEntityIdAndEntityType(object.getUserContributionId(), "UserContribution");

					List<TagMaster> dbTagMasterResponse = tagMasterRepository.findByEntityIdAndEntityTypeAndType(
							object.getUserContributionId(), "UserContribution", "user");

					List<UserContributionDocument> dbDocumentResponse = userContributionDocumentRepository
							.findByUserContributionId(object.getUserContributionId());

					Project projectObject = projectRepository.findByProjectId(object.getProjectId().intValue());

					ProjectInsightUserContributionDTO dto = new ProjectInsightUserContributionDTO();
					dto.setAssignTo(object.getAssignTo());
					dto.setCreatedOn(object.getCreatedOn() != null ? object.getCreatedOn().toString() : null);
					dto.setEmpId(object.getEmpId());
					dto.setContributionBy(Optional.ofNullable(employeeList).orElse(Collections.emptyList()).stream()
							.filter(emp -> emp[0] != null && Long.valueOf(emp[0].toString()).equals(object.getEmpId()))
							.map(emp -> emp[3] != null ? emp[3].toString() : null).findFirst().orElse(null));
					dto.setProjectId(object.getProjectId());
					dto.setProjectName(projectObject != null ? projectObject.getProjectName() : null);
					dto.setResponse(object.getResponse());
					dto.setStatus(object.getStatus());
					dto.setUpdatedOn(object.getUpdatedOn() != null ? object.getUpdatedOn().toString() : null);
					dto.setUserContributionId(object.getUserContributionId());
					dto.setUserDefinedProjectName(object.getUserDefinedProjectName());
					dto.setParentContribution(object.getParentContribution());
					dto.setTitle(object.getTitle());
					dto.setOnlyText(object.getOnlyTextResponse());
					dto.setTeamMembers(allTaggedUser.stream().map(ProjectInsightAssignees::getAssignedTo)
							.collect(Collectors.toList()));
					dto.setTags(dbTagMasterResponse.stream().map(TagMaster::getTag).collect(Collectors.toList()));
					dto.setUserDocument(dbDocumentResponse);
					dto.setResponseRemarkList(responseRemarkDbResp);
					dto.setReviewType(object.getReviewType());

					return dto;
				}).filter(Objects::nonNull)
						.collect(Collectors.toList());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return response;
	}

	public List<ProjectQuestionDTO> getProjectQuestionDTOListForSearchResult(Long entityId, String entityType) {
		List<ProjectQuestionDTO> projectInsightQuestionList = new ArrayList<>();
		try {
			List<QuestionMaster> projectInsightQuestionMasterList = questionMasterRepository.findByEntityIdAndEntityType(entityId, entityType);
			if (!projectInsightQuestionMasterList.isEmpty()) {
				for (QuestionMaster questionMaster : projectInsightQuestionMasterList) {
					ProjectQuestionDTO projectQuestionDTO = mapQuestionMasterToProjectQuestionDTO(questionMaster);
					List<ProjectResponseDTO> projectResponseDTOList = new ArrayList<>();
					
					List<ProjectInsightResponse> projectInsightResponseList = projectInsightResponseRepository
							.findByQuestionMasterIdAndIsApprovedForKnowledgeHub(questionMaster.getQuestionMasterId(), "Y");
					if (projectInsightResponseList != null && !projectInsightResponseList.isEmpty()) {
						for (ProjectInsightResponse projectInsightResponse : projectInsightResponseList) {
							ProjectResponseDTO projectResponseDTO = mapPojectInsightResponseToProjectInsightResponseDTO(projectInsightResponse,questionMaster.getOptions());
							projectResponseDTOList.add(projectResponseDTO);
						}
					}
					projectQuestionDTO.setProjectResponseList(projectResponseDTOList);
					projectQuestionDTO.setRecommendedResponseId(questionMaster.getRecommendedResponseId());
					projectInsightQuestionList.add(projectQuestionDTO);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return projectInsightQuestionList;
	}
	
	public ProjectInsightDTO getAllMilestoneInfoByProjectId(Long projectId){
		ProjectInsightDTO response = new ProjectInsightDTO();
		try {
			//projectinfo + Milestone .....
			Project project = projectRepository.findByProjectId(projectId.intValue());
			
			if(project != null) {
				
				response.setQuestionList(getProjectQuestionDTOListForSearchResult(projectId, "Project"));
				response.setProjectId(projectId);
				response.setProjectManagerId(project.getProjectManagerId());
				response.setProjectManagerName(getProjectManagerName(project.getProjectManagerId()));
				response.setProjectName(project.getProjectName());
				
				List<ProjectInsightMilestone> projectMilestoneListByProjectId = projectInsightMilestoneRepository.getByProjectId(projectId);
				if(!projectMilestoneListByProjectId.isEmpty()) {
					List<ProjectInsightMilestoneDTO> milestoneList = new ArrayList<>();
					projectMilestoneListByProjectId.forEach((object) -> {
						ProjectInsightMilestoneDTO projectInsightMilestoneDTO = new ProjectInsightMilestoneDTO();
						
						projectInsightMilestoneDTO.setMilestoneId(object.getMilestoneId());
						projectInsightMilestoneDTO.setMilestone(object.getMilestone());
						projectInsightMilestoneDTO.setDescription(object.getDescription());
						projectInsightMilestoneDTO.setDeptId(object.getDeptId());
						
						projectInsightMilestoneDTO.setQuestionList(getProjectQuestionDTOListForSearchResult(object.getMilestoneId(), "Milestone"));
						projectInsightMilestoneDTO.setModuleList(getAllModuleInfoByMilestoneId(object.getMilestoneId()).getModuleList());
						
						milestoneList.add(projectInsightMilestoneDTO);
				});
					
					response.setProjectInsightMilestoneList(milestoneList);
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return response;
	}
	
    public ProjectInsightMilestoneDTO getAllModuleInfoByMilestoneId(Long milestoneId){
    	ProjectInsightMilestoneDTO projectInsightMilestoneDTOList = new ProjectInsightMilestoneDTO();
		try {
			//Milestone + List<Module> .....
			ProjectInsightMilestone milestoneObject = projectInsightMilestoneRepository.getByMilestoneId(milestoneId);

			if(milestoneObject != null) {
				projectInsightMilestoneDTOList.setMilestone(milestoneObject.getMilestone());
				projectInsightMilestoneDTOList.setMilestoneId(milestoneObject.getMilestoneId());
				projectInsightMilestoneDTOList.setDescription(milestoneObject.getDescription());
				projectInsightMilestoneDTOList.setDeptId(milestoneObject.getDeptId());
				projectInsightMilestoneDTOList.setCreatedOn(milestoneObject.getCreatedOn().toString());
				projectInsightMilestoneDTOList.setQuestionList(getProjectQuestionDTOListForSearchResult(milestoneObject.getProjectId(), "Project"));
				
				List<ProjectInsightModule> projectInsightModuleList = projectInsightModuleRepository.findByMilestoneId(milestoneId);
				if (!projectInsightModuleList.isEmpty()) {
					List<ModuleDTO> projectInsightModuleDTOList = new ArrayList<>();
					for (ProjectInsightModule projectInsightModule : projectInsightModuleList) {
						ModuleDTO moduleDTO = new ModuleDTO();
						moduleDTO.setModuleId(projectInsightModule.getModuleId());
						moduleDTO.setMilestoneId(projectInsightModule.getMilestoneId());
						moduleDTO.setModule(projectInsightModule.getModule());
						moduleDTO.setDescription(projectInsightModule.getDescription());
						moduleDTO.setRedmineId(projectInsightModule.getRedmineId());
						
						moduleDTO.setQuestionList(getProjectQuestionDTOListForSearchResult(projectInsightModule.getMilestoneId(), "Milestone"));
						moduleDTO.setSubModuleList(getAllSubModuleInfoByModuleId(projectInsightModule.getModuleId(), "SubModule").getSubModuleList());
						
						if ((moduleDTO.getSubModuleList() != null && !moduleDTO.getSubModuleList().isEmpty()) 
								|| (moduleDTO.getQuestionList() != null && !moduleDTO.getQuestionList().isEmpty())) {
							projectInsightModuleDTOList.add(moduleDTO);
						}
					}
					
					projectInsightMilestoneDTOList.setModuleList(projectInsightModuleDTOList);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return projectInsightMilestoneDTOList;
	}
    
    public ModuleDTO getAllSubModuleInfoByModuleId(Long moduleId, String subModuleType){
    	ModuleDTO moduleDTO = new ModuleDTO();
		try {
			//Module + List<Submodule> .....
			ProjectInsightModule moduleObject = projectInsightModuleRepository.getByModuleId(moduleId);
			if(moduleObject != null) {
				moduleDTO.setModule(moduleObject.getModule());
				moduleDTO.setModuleId(moduleObject.getModuleId());
				moduleDTO.setMilestoneId(moduleObject.getMilestoneId());
				moduleDTO.setDescription(moduleObject.getDescription());
				moduleDTO.setRedmineId(moduleObject.getRedmineId());
				moduleDTO.setQuestionList(getProjectQuestionDTOListForSearchResult(moduleObject.getModuleId(),"Module"));
				
				
				List<ProjectInsightSubModule> projectInsightSubModuleList = projectInsightSubModuleRepository.getByModuleIdAndSubModuleType(moduleId,subModuleType);
				if (!projectInsightSubModuleList.isEmpty()) {
					List<SubModuleDTO> subModuleList = new ArrayList<>();
					
					for (ProjectInsightSubModule projectInsightSubModule : projectInsightSubModuleList) {
						SubModuleDTO subModuleDTO = new SubModuleDTO();
						subModuleDTO.setSubModuleId(projectInsightSubModule.getSubmoduleId());
						subModuleDTO.setSubModule(projectInsightSubModule.getSubmodule());
						subModuleDTO.setModuleId(projectInsightSubModule.getModuleId());
						subModuleDTO.setDescription(projectInsightSubModule.getDescription());
						subModuleDTO.setRedmineId(projectInsightSubModule.getRedmineId());
						subModuleDTO.setQuestionList(getProjectQuestionDTOListForSearchResult(projectInsightSubModule.getSubmoduleId(),subModuleType));
						
						if ((subModuleDTO.getQuestionList() != null && !subModuleDTO.getQuestionList().isEmpty())) {
							subModuleList.add(subModuleDTO);
						}
						
						moduleDTO.setSubModuleList(subModuleList);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return moduleDTO;
	}
    
    public SubModuleDTO getAllSubSubModuleInfoBySubModuleId(Long subModuleId, String subModuleType){
    	SubModuleDTO subModuleDTO = new SubModuleDTO();
		try {
			//Module + List<Submodule> .....
			ProjectInsightSubModule subModuleObject = projectInsightSubModuleRepository.getBySubmoduleId(subModuleId);
			if(subModuleObject != null) {
				subModuleDTO.setSubModule(subModuleObject.getSubmodule());
				subModuleDTO.setSubModuleId(subModuleObject.getSubmoduleId());
				subModuleDTO.setModuleId(subModuleObject.getModuleId());
				subModuleDTO.setDescription(subModuleObject.getDescription());
				subModuleDTO.setRedmineId(subModuleObject.getRedmineId());
				subModuleDTO.setQuestionList(getProjectQuestionDTOListForSearchResult(subModuleObject.getSubmoduleId(),subModuleType));
				
				List<ProjectInsightSubModule> projectInsightSubModuleList = projectInsightSubModuleRepository.getByModuleIdAndSubModuleType(subModuleId,subModuleType);
				if (!projectInsightSubModuleList.isEmpty()) {
					List<SubModuleDTO> subsubModuleList = new ArrayList<>();
					
					for (ProjectInsightSubModule projectInsightSubModule : projectInsightSubModuleList) {
						SubModuleDTO subsubModuleDTO = new SubModuleDTO();
						subsubModuleDTO.setSubModuleId(projectInsightSubModule.getSubmoduleId());
						subsubModuleDTO.setSubModule(projectInsightSubModule.getSubmodule());
						subsubModuleDTO.setModuleId(projectInsightSubModule.getModuleId());
						subsubModuleDTO.setDescription(projectInsightSubModule.getDescription());
						subsubModuleDTO.setRedmineId(projectInsightSubModule.getRedmineId());
						subsubModuleDTO.setQuestionList(getProjectQuestionDTOListForSearchResult(projectInsightSubModule.getSubmoduleId(),subModuleType));
						
						if ((subModuleDTO.getQuestionList() != null && !subModuleDTO.getQuestionList().isEmpty())) {
							subsubModuleList.add(subsubModuleDTO);
						}
						
						subModuleDTO.setSubSubModuleList(subsubModuleList);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return subModuleDTO;
	}
	
    public SearchResultResponse getSearchResultByEntityIdAndEntityType(Long projectId) {
        SearchResultResponse response = new SearchResultResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setLogLevel("INFO");
        apiLogInfo.setSubFeatureName("getSearchResultByEntityIdAndEntityType");
        apiLogInfo.setApiUrl("getSearchResultByEntityIdAndEntityType");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("projectId : " + projectId);
        try {
            Project project = projectRepository.findByProjectId(projectId.intValue());
            if (project == null) {
                throw new RuntimeException("Project Not Found!!");
            }
            
            ProjectInsightDTO projectInsightDTO = getAllMilestoneInfoByProjectId(projectId);
            response.setProject(projectInsightDTO);

//            if ("Project".equals(entityType)) {
//            	ProjectInsightDTO projectInsightDTO = getAllMilestoneInfoByProjectId(projectId);
//                response.setProject(projectInsightDTO);
//                
//            } else if ("Milestone".equals(entityType)) {
//            	ProjectInsightMilestoneDTO milestone = getAllModuleInfoByMilestoneId(entityId);
//                response.setMilestone(milestone);
//                
//            } else if ("Module".equals(entityType)) {
//            	ModuleDTO module = getAllSubModuleInfoByModuleId(entityId, "SubModule");
//                response.setModule(module);
//                
//            } else if ("SubModule".equals(entityType) || "Sub-SubModule".equals(entityType)) {
//            	SubModuleDTO submodule = getAllSubSubModuleInfoBySubModuleId(entityId, "SubModule");
//            	
//            	if("SubModule".equals(entityType)) {
//            		response.setSubModule(submodule);
//            	}else {
//            		response.setSubSubModule(submodule);
//            	}
//            } else {
//                System.out.println("invalid");
//            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return response;
    }
	
	
	
	public ResponseEntity<Set<String>> suggestSearchOption(String search) {
		Set<String> response = new HashSet<>();
		try {
			List<TagMaster> matchedTags = tagMasterRepository.findAll(TagSpecifications.tagNameLikeAny(search));
			response = matchedTags.stream().map(TagMaster::getTag).collect(Collectors.toSet());
		}catch(Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok(response);
	}

	public ResponseEntity<List<ProjectInsightFilterDTO>> getFilterList() {
		List<ProjectInsightFilterDTO> response = new ArrayList<>();
		try {
			List<ProjectInsightFilter> projectFilterList = projectInsightFilterRepository.findAll();
			if(!projectFilterList.isEmpty()) {
				projectFilterList.forEach((object) -> {
					List<ProjectInsightFilterOptions> projectFilterOptionList = new ArrayList<>();
					
					if(object.getFilterName().equals("Department")) {
						//getAllDepartmentList
						List<Department> deptList = departmentRepository.findAll();
						if(!deptList.isEmpty()) {
							for(Department dept: deptList) {
								ProjectInsightFilterOptions dto = new ProjectInsightFilterOptions();
								
								dto.setOptionId(dept.getDeptId());
								dto.setOptionName(dept.getName());
								dto.setFilterId(object.getFilterId());
								
								projectFilterOptionList.add(dto);
							}
						}
					}else {
						projectFilterOptionList = projectInsightFilterOptionsRepository.findByFilterId(object.getFilterId());
					}

					ProjectInsightFilterDTO dto = new ProjectInsightFilterDTO();
					
					dto.setFilterId(object.getFilterId());
					dto.setFilterName(object.getFilterName());
					dto.setOptionList(projectFilterOptionList != null ? projectFilterOptionList : null);
					
					response.add(dto);
				});
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok(response);
	}
	
	
	
	/*
	 * ------------------------------------- ProjectInsight -------------------------------------------------- 
	 * 
	 * */

	public ResponseEntity<List<ProjectInsightUserContributionDTO>> getContributionByEmpId(
	        ProjectInsightUserContributionDTO projectInsightUserContributionDTO) {

	    List<ProjectInsightUserContributionDTO> response = new ArrayList<>();

	    try {
	        if (projectInsightUserContributionDTO == null || projectInsightUserContributionDTO.getEmpId() == null) {
	            return ResponseEntity.badRequest().body(Collections.emptyList());
	        }

	        List<ProjectInsightUserContribution> userContributions =
	                projectInsightUserContributionRepository.findByEmpId(projectInsightUserContributionDTO.getEmpId());

	        if (userContributions != null && !userContributions.isEmpty()) {
	            response = userContributions.stream().map(object -> {
	            	
	            	List<ProjectInsightAssignees> alltaggedUser = projectInsightAssigneesRepository.getByEntityIdAndEntityType(object.getUserContributionId(),
		        			"UserContribution");
	            	List<TagMaster> dbTagMasterResponse = tagMasterRepository.findByEntityIdAndEntityTypeAndType(object.getUserContributionId()
	            			,"UserContribution","user");
	            	List<UserContributionDocument> dbDocumentResponse = userContributionDocumentRepository.findByUserContributionId(object.getUserContributionId());
	            	List<UserContributionResponseRemarks> responseRemarkDbResp = userContributionResponseRemarksRepository.findByUserContributionId(object.getUserContributionId());
	            	
	                ProjectInsightUserContributionDTO dto = new ProjectInsightUserContributionDTO();
	                dto.setAssignTo(object.getAssignTo());
	                dto.setCreatedOn(object.getCreatedOn() != null ? object.getCreatedOn().toString() : null);
	                dto.setEmpId(object.getEmpId());
	                dto.setProjectId(object.getProjectId());
	                dto.setResponse(object.getResponse());
	                dto.setStatus(object.getStatus());
	                dto.setUpdatedOn(object.getUpdatedOn() != null ? object.getUpdatedOn().toString() : null);
	                dto.setUserContributionId(object.getUserContributionId());
	                dto.setUserDefinedProjectName(object.getUserDefinedProjectName());
	                dto.setParentContribution(object.getParentContribution());
	                dto.setTitle(object.getTitle());
	                dto.setOnlyText(object.getOnlyTextResponse());
	                dto.setTeamMembers(alltaggedUser.stream().map(assignObj -> assignObj.getAssignedTo()).collect(Collectors.toList()));  
	                dto.setTags(dbTagMasterResponse.stream().map(TagMaster::getTag).collect(Collectors.toList()));
	                dto.setUserDocument(dbDocumentResponse);
	                dto.setResponseRemarkList(responseRemarkDbResp);
	                dto.setReviewType(object.getReviewType());
	                
	                return dto;
	            }).collect(Collectors.toList());
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.internalServerError().body(Collections.emptyList());
	    }

	    return ResponseEntity.ok(response);
	}

	public ResponseEntity<ServiceResponse> createOrUpdateUserContribution(ProjectInsightUserContributionDTO dto) {
		ServiceResponse response = new ServiceResponse();
	    try {
	        if (dto.getEmpId() == null) {
	        	response.setServiceMessage("Missing required fields: empId");
	            return ResponseEntity.badRequest().body(response);
	        }

	        ProjectInsightUserContribution entity;
	        Employee empObj = employeeRepository.findByEmpId(dto.getEmpId());
	        if (dto.getUserContributionId() != null) {
	            Optional<ProjectInsightUserContribution> optionalEntity =
	                    projectInsightUserContributionRepository.findById(dto.getUserContributionId());

	            if (optionalEntity.isPresent()) {
	                entity = optionalEntity.get();
	                entity.setUpdatedOn(LocalDateTime.now());
	                entity.setStatus("Pending");
	                
	                if(entity.getAssignTo() == null) {
	                	entity.setAssignTo(empObj != null ?
	    	            		empObj.getReportingManagerId() != null ?
	    	            				empObj.getReportingManagerId():empObj.getManagerId() :null);
	                }
	                
	            } else {
	            	response.setServiceMessage("User contribution not found with ID: " + dto.getUserContributionId());
	                return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                        .body(response);
	            }
	        } else {
	        	
	            entity = new ProjectInsightUserContribution();
	            entity.setAssignTo(empObj != null ?
	            		empObj.getReportingManagerId() != null ?
	            				empObj.getReportingManagerId():empObj.getManagerId() :null);
	            entity.setEmpId(dto.getEmpId());
		        entity.setProjectId(dto.getProjectId());
	        }
	        
	        entity.setResponse(dto.getResponse());
	        entity.setStatus("Pending");
	        entity.setUserDefinedProjectName(dto.getUserDefinedProjectName());
	        entity.setTitle(dto.getTitle());
	        entity.setParentContribution(dto.getParentContribution());
	        entity.setOnlyTextResponse(dto.getOnlyText());
	        
	        ProjectInsightUserContribution dbResponse = projectInsightUserContributionRepository.save(entity);
	        
	        if(dbResponse != null) {
	        	//Add new team member tag
	        	List<ProjectInsightAssignees> alltaggedUser = projectInsightAssigneesRepository.getByEntityIdAndEntityType(dbResponse.getUserContributionId(),
	        			"UserContribution");
	        	if(!alltaggedUser.isEmpty()) {
	        		projectInsightAssigneesRepository.deleteAll(alltaggedUser);
	        	}

	        	if(!dto.getTeamMembers().isEmpty()) {
	        		List<ProjectInsightAssignees> newAssignees = dto.getTeamMembers().stream().map(object -> {
	        			ProjectInsightAssignees obj = new ProjectInsightAssignees();
	        			obj.setEntityId(dbResponse.getUserContributionId());
	        			obj.setEntityType("UserContribution");
	        			obj.setAssignedTo(object);
	        			obj.setTaggedBy(dto.getEmpId());
	        			
	        			return obj;
	        		}).collect(Collectors.toList());
	        		
	        		projectInsightAssigneesRepository.saveAll(newAssignees);
	        	}
	        	
	        	//Add tags
	        	List<TagMaster> dbTagMasterResponse = tagMasterRepository
	        			.findByEntityIdAndEntityTypeAndType(dbResponse.getUserContributionId(),"UserContribution","user");
	        	
	        	if(!dbTagMasterResponse.isEmpty()) {
	        		tagMasterRepository.deleteAll(dbTagMasterResponse);
	        	}
	        	if(!dto.getTags().isEmpty()) {
	        		List<TagMaster> newUserDefinedTags = dto.getTags().stream()
	        														.map(tagObj -> {
	        															TagMaster newObj = new TagMaster();
	        															
	        															newObj.setEntityId(dbResponse.getUserContributionId());
	        															newObj.setEntityType("UserContribution");
	        															newObj.setTag(tagObj);
	        															newObj.setType("user");
	        															
	        															return newObj;
	        														}).collect(Collectors.toList());
	        		tagMasterRepository.saveAll(newUserDefinedTags);
	        	}
	        	
	        	//Upload Files
	        	List<UserContributionDocument> existingDocs = userContributionDocumentRepository
	                    .findByUserContributionId(dbResponse.getUserContributionId());

	            final Set<Long> documentIdsToKeep = dto.getUserDocument() != null ? 
	                dto.getUserDocument().stream()
	                    .map(UserContributionDocument::getDocumentId)
	                    .collect(Collectors.toSet()) : 
	                new HashSet<>();

	            List<UserContributionDocument> docsToDelete = existingDocs.stream()
	                    .filter(doc -> !documentIdsToKeep.contains(doc.getDocumentId()))
	                    .collect(Collectors.toList());
	            
	            if (!docsToDelete.isEmpty()) {
	                userContributionDocumentRepository.deleteAll(docsToDelete);
	            }

	            // Upload new files
	            if (dto.getAttachments() != null) {
	                for (MultipartFile document : dto.getAttachments()) {
	                    if (document != null && document.getOriginalFilename() != null) {
	                        String uploadResponse = uploadProjectResponseDocument(document, dbResponse.getUserContributionId(), "UserContribution");
	                        if (uploadResponse.equalsIgnoreCase("Document uploaded successfully")) {
	                            UserContributionDocument newDoc = new UserContributionDocument();
	                            newDoc.setDocumentName(document.getOriginalFilename());
	                            newDoc.setUserContributionId(dbResponse.getUserContributionId());
	                            userContributionDocumentRepository.save(newDoc);
	                        }
	                    }
	                }
	            }
	        }

	        response.setServiceMessage(dto.getUserContributionId() != null ? "User contribution updated successfully." : "User contribution created successfully.");
	        return ResponseEntity.ok(response);
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceMessage("An error occurred while saving the contribution.");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}

	public ResponseEntity<List<ProjectInsightUserContributionDTO>> getUserContributionForReview(
			ProjectInsightUserContributionDTO projectInsightUserContributionDTO) {
		List<ProjectInsightUserContributionDTO> response = new ArrayList<>();
		try {
			List<Object[]> employeeList = employeeRepository.getEmployees();
			List<ProjectInsightUserContribution> userContributionsForReview = new ArrayList<>();
			if(projectInsightUserContributionDTO.getEmpId() != null
					&& projectInsightUserContributionDTO.getAssignTo() != null) {
				//Get data for manager i.e ---> Team dashboard --> user's contribution
				
				userContributionsForReview =projectInsightUserContributionRepository
						.findByEmpIdAndAssignTo(projectInsightUserContributionDTO.getEmpId(),projectInsightUserContributionDTO.getAssignTo());
				
			}else if(projectInsightUserContributionDTO.getAssignTo() != null) {
				//Get for pre reviewer i.e ---> My performance dashboard --> (view assigned contribution)
				
				userContributionsForReview = projectInsightUserContributionRepository
						.findByAssignTo(projectInsightUserContributionDTO.getAssignTo());
				
			}
			
			if (userContributionsForReview != null && !userContributionsForReview.isEmpty()) {
	            response = userContributionsForReview.stream().map(object -> {
	            	
	            	List<ProjectInsightAssignees> alltaggedUser = projectInsightAssigneesRepository.getByEntityIdAndEntityType(object.getUserContributionId(),
		        			"UserContribution");
	            	List<TagMaster> dbTagMasterResponse = tagMasterRepository.findByEntityIdAndEntityTypeAndType(object.getUserContributionId()
	            			,"UserContribution","user");
	            	List<UserContributionDocument> dbDocumentResponse = userContributionDocumentRepository.findByUserContributionId(object.getUserContributionId());
	            	List<UserContributionResponseRemarks> responseRemarkDbResp = userContributionResponseRemarksRepository.findByUserContributionId(object.getUserContributionId());
	            	
	                ProjectInsightUserContributionDTO dto = new ProjectInsightUserContributionDTO();
	                dto.setAssignTo(object.getAssignTo());
	                dto.setCreatedOn(object.getCreatedOn() != null ? object.getCreatedOn().toString() : null);
	                dto.setEmpId(object.getEmpId());
	                dto.setContributionBy(
	                	    Optional.ofNullable(employeeList)
	                	            .orElse(Collections.emptyList())
	                	            .stream()
	                	            .filter(obj -> obj[0] != null && Long.valueOf(obj[0].toString()).equals(object.getEmpId()))
	                	            .map(obj -> obj[3] != null ? obj[3].toString() : null)
	                	            .findFirst()
	                	            .orElse(null)
	                	);
	                dto.setProjectId(object.getProjectId());
	                dto.setResponse(object.getResponse());
	                dto.setStatus(object.getStatus());
	                dto.setUpdatedOn(object.getUpdatedOn() != null ? object.getUpdatedOn().toString() : null);
	                dto.setUserContributionId(object.getUserContributionId());
	                dto.setUserDefinedProjectName(object.getUserDefinedProjectName());
	                dto.setParentContribution(object.getParentContribution());
	                dto.setTitle(object.getTitle());
	                dto.setOnlyText(object.getOnlyTextResponse());
	                dto.setTeamMembers(alltaggedUser.stream().map(ProjectInsightAssignees::getAssignedTo).collect(Collectors.toList()));  
	                dto.setTags(dbTagMasterResponse.stream().map(TagMaster::getTag).collect(Collectors.toList()));
	                dto.setUserDocument(dbDocumentResponse);
	                dto.setResponseRemarkList(responseRemarkDbResp);
	                dto.setReviewType(object.getReviewType());
	                
	                return dto;
	            }).collect(Collectors.toList());
	        }
			
		}catch(Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body(Collections.emptyList());
		}
		return ResponseEntity.ok(response);
	}

	public ResponseEntity<ServiceResponse> processUserContribution(
			ProjectInsightUserContributionDTO projectInsightUserContributionDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			 Optional<ProjectInsightUserContribution> dbResponse = projectInsightUserContributionRepository
					 .findById(projectInsightUserContributionDTO.getUserContributionId());
			 
			 if(!dbResponse.isEmpty()) {
				 ProjectInsightUserContribution userContributionDbResp = dbResponse.get();
				 
				 if(projectInsightUserContributionDTO.getProcessType().equals("preReviewer")) {
					 userContributionDbResp.setAssignTo(projectInsightUserContributionDTO.getAssignTo());
					 userContributionDbResp.setReviewType("preReviewer");
				 }else {
					 userContributionDbResp.setStatus(projectInsightUserContributionDTO.getStatus());
					 userContributionDbResp.setProjectId(projectInsightUserContributionDTO.getProjectId());
					 
					 if(projectInsightUserContributionDTO.getProcessType().equals("Reject")) {
						 userContributionDbResp.setAssignTo(null);
					 }else {
						 if(projectInsightUserContributionDTO.getReviewType() == null || projectInsightUserContributionDTO.getReviewType().equals("preReviewer")) {
							 Employee empObj = employeeRepository.findByEmpId(userContributionDbResp.getEmpId());
							 userContributionDbResp.setAssignTo(empObj != null ?
					            		empObj.getReportingManagerId() != null ?
					            				empObj.getReportingManagerId():empObj.getManagerId() :null);
							 
							 userContributionDbResp.setReviewType("manager");
						 }
					 }
					 
					 // Add remarks
					 if(projectInsightUserContributionDTO.getRemark() != null) {
						 UserContributionResponseRemarks newObj = new UserContributionResponseRemarks();
						 newObj.setRemark(projectInsightUserContributionDTO.getRemark());
						 newObj.setRemarkBy(projectInsightUserContributionDTO.getAssignTo());
						 newObj.setUserContributionId(userContributionDbResp.getUserContributionId());
						 newObj.setRemarkStatus(projectInsightUserContributionDTO.getProcessType());
						 
						 userContributionResponseRemarksRepository.save(newObj);
					 }
				 }
				 
				 projectInsightUserContributionRepository.save(userContributionDbResp);
			 }else {
				 response.setServiceMessage("No contribution found !!");
		         return ResponseEntity.badRequest().body(response);
			 }
			
			 response.setServiceMessage("Review added successfully !!");
			return ResponseEntity.ok(response);
		}catch(Exception e) {
			 e.printStackTrace();
		     response.setServiceMessage("An error occurred while saving the contribution.");
		     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
	
	public ServiceResponse extractTagDataFromProjectInsight(ProjectInsightDTO projectInsightDTO) {
	    ServiceResponse response = new ServiceResponse();
	    try {
	        List<TagDTO> tagDTOList = new ArrayList<>();
	        processProjectNode(projectInsightDTO, tagDTOList);
	        
	        if(!tagDTOList.isEmpty()) {
	        	tagDTOList.forEach((tagobj) -> {
	        		tagUtils.saveTags(tagobj);
	        	});
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return response;
	}
	
	
	// Search tag ------------------------------------------------------------------------
	
	private void processProjectNode(ProjectInsightDTO project, List<TagDTO> tagDTOList) {
	    TagDTO projectTag = new TagDTO();
	    projectTag.setEntityType("Project");
	    projectTag.setEntityId(project.getProjectId());
	    projectTag.setProjectId(project.getProjectId());
	    
	    StringBuilder sb = new StringBuilder();
	    sb.append(project.getProjectName()).append(", ");
	    sb.append(project.getProjectManagerName()).append(". ");
	    if (project.getQuestionList() != null) {
	        for (ProjectQuestionDTO q : project.getQuestionList()) {
	            sb.append(formatQuestion(q));
	        }
	    }
	    projectTag.setProjectText(sb);
	    tagDTOList.add(projectTag);

	    if (project.getProjectInsightMilestoneList() != null) {
	        for (ProjectInsightMilestoneDTO milestone : project.getProjectInsightMilestoneList()) {
	            processMilestoneNode(milestone, tagDTOList, project.getProjectId());
	        }
	    }
	}
	
	private void processMilestoneNode(ProjectInsightMilestoneDTO milestone, List<TagDTO> tagDTOList, Long projectId) {
	    TagDTO milestoneTag = new TagDTO();
	    milestoneTag.setEntityType("Milestone");
	    milestoneTag.setEntityId(milestone.getMilestoneId());
	    milestoneTag.setProjectId(projectId);
	    
	    StringBuilder sb = new StringBuilder();
	    sb.append(milestone.getMilestone()).append(". ");
	    // Add milestone-level questions
	    if (milestone.getQuestionList() != null) {
	        for (ProjectQuestionDTO q : milestone.getQuestionList()) {
	            sb.append(formatQuestion(q));
	        }
	    }
	    milestoneTag.setProjectText(sb);
	    tagDTOList.add(milestoneTag);

	    // Process Modules
	    if (milestone.getModuleList() != null) {
	        for (ModuleDTO module : milestone.getModuleList()) {
	            processModuleNode(module, tagDTOList, milestone.getMilestoneId(), projectId);
	        }
	    }
	}

	// Recursive processing for Module
	private void processModuleNode(ModuleDTO module, List<TagDTO> tagDTOList, Long milestoneId, Long projectId) {
	    TagDTO moduleTag = new TagDTO();
	    moduleTag.setEntityType("Module");
	    moduleTag.setEntityId(module.getModuleId());
	    moduleTag.setProjectId(projectId);
	    
	    StringBuilder sb = new StringBuilder();
	    sb.append(module.getModule()).append(". ");
	    // Add module-level questions
	    if (module.getQuestionList() != null) {
	        for (ProjectQuestionDTO q : module.getQuestionList()) {
	            sb.append(formatQuestion(q));
	        }
	    }
	    moduleTag.setProjectText(sb);
	    tagDTOList.add(moduleTag);

	    // Process SubModules
	    if (module.getSubModuleList() != null) {
	        for (SubModuleDTO subModule : module.getSubModuleList()) {
	            processSubModuleNode(subModule, tagDTOList, module.getModuleId(), projectId);
	        }
	    }
	}

	// Recursive processing for SubModule
	private void processSubModuleNode(SubModuleDTO subModule, List<TagDTO> tagDTOList, Long moduleId, Long projectId) {
	    TagDTO subModuleTag = new TagDTO();
	    subModuleTag.setEntityType("SubModule");
	    subModuleTag.setEntityId(subModule.getSubModuleId());
	    subModuleTag.setProjectId(projectId);
	    
	    StringBuilder sb = new StringBuilder();
	    sb.append(subModule.getSubModule()).append(". ");
	    // Add submodule-level questions
	    if (subModule.getQuestionList() != null) {
	        for (ProjectQuestionDTO q : subModule.getQuestionList()) {
	            sb.append(formatQuestion(q));
	        }
	    }
	    subModuleTag.setProjectText(sb);
	    tagDTOList.add(subModuleTag);

	    // Process SubSubModules if present
	    if (subModule.getSubSubModuleList() != null) {
	        for (SubModuleDTO subSubModule : subModule.getSubSubModuleList()) {
	            processSubSubModuleNode(subSubModule, tagDTOList, subModule.getSubModuleId(), projectId);
	        }
	    }
	}

	// Recursive processing for SubSubModule
	private void processSubSubModuleNode(SubModuleDTO subSubModule, List<TagDTO> tagDTOList, Long subModuleId, Long projectId) {
	    TagDTO subSubModuleTag = new TagDTO();
	    subSubModuleTag.setEntityType("Sub-SubModule");
	    subSubModuleTag.setEntityId(subSubModule.getSubModuleId());
	    subSubModuleTag.setProjectId(projectId);
	    
	    StringBuilder sb = new StringBuilder();
	    sb.append(subSubModule.getSubModule()).append(". ");
	    if (subSubModule.getQuestionList() != null) {
	        for (ProjectQuestionDTO q : subSubModule.getQuestionList()) {
	            sb.append(formatQuestion(q));
	        }
	    }
	    subSubModuleTag.setProjectText(sb);
	    tagDTOList.add(subSubModuleTag);
	    
	 // Process SubSubModules if present
	    if (subSubModule.getSubSubModuleList() != null) {
	        for (SubModuleDTO subSubModules : subSubModule.getSubSubModuleList()) {
	            processSubSubModuleNode(subSubModules, tagDTOList, subSubModules.getSubModuleId(), projectId);
	        }
	    }
	}
	
	private String formatQuestion(ProjectQuestionDTO q) {
	    // Check if any response is approved for Knowledge Hub
	    boolean hasApproved = false;
	    if (q.getProjectResponseList() != null) {
	        for (ProjectResponseDTO resp : q.getProjectResponseList()) {
	            if (resp.isApprovedForKnowledgeHub()) {
	                hasApproved = true;
	                break;
	            }
	        }
	    }
	    if (!hasApproved) {
	        return "";
	    }

	    StringBuilder sb = new StringBuilder();
	    sb.append(q.getQuestion()).append(" ");
	    sb.append(q.getDescription()).append(" ");
	    if (q.getOptionType() != null) {
	        sb.append(q.getOptionType()).append(" ");
	    }
	    if (q.getOptions() != null && !q.getOptions().equals("[]")) {
	        sb.append(q.getOptions()).append(" ");
	    }
	    if (q.getProjectResponseList() != null) {
	        for (ProjectResponseDTO resp : q.getProjectResponseList()) {
	            if (resp.getResponse() != null && resp.isApprovedForKnowledgeHub()) {
	                sb.append(resp.getResponse()).append(" ");
	            }
	        }
	    }
	    return sb.toString();
	}

	public ServiceResponse saveReviewPoints(ProjectInsightDTO projectInsightDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setSubFeatureName("saveReviewPoints");
		apiLogInfo.setApiUrl("/api/saveReviewPoints");
		StringBuilder logBuilder = new StringBuilder();
		try {
			
			if (projectInsightDTO.getPointsBy() == null) {
				apiLogInfo.setApiResponse("Employee Id does not exists");
				response.setServiceResponse("Employee Id does not exists.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				return response;
			}
			
			Project project = projectRepository.findByProjectId(Integer.parseInt(projectInsightDTO.getProjectId().toString()));
			if (project == null) {
				throw new RuntimeException("Project Not Found!!");
			}
			
			extractTagDataFromProjectInsight(projectInsightDTO);
			
			if (projectInsightDTO.getQuestionList() != null && !projectInsightDTO.getQuestionList().isEmpty()) {
				saveQuestionResponsesReviewPoints(projectInsightDTO.getQuestionList(),projectInsightDTO.getProjectId(),projectInsightDTO.getPointsBy(),projectInsightDTO.getIsPointsDrafted(),projectInsightDTO.getProjectId(),"Project",projectInsightDTO.getTransferToKnowledgeHub());
			}

			if (projectInsightDTO.getProjectInsightMilestoneList() != null && !projectInsightDTO.getProjectInsightMilestoneList().isEmpty()) {
				saveMilestoneReviewPoints(projectInsightDTO.getProjectInsightMilestoneList(),projectInsightDTO.getProjectId(),projectInsightDTO.getPointsBy(),projectInsightDTO.getIsPointsDrafted(),projectInsightDTO.getTransferToKnowledgeHub());
			}
			
			apiLogInfo.setApiResponse("Review Points Saved Successfully");
			response.setServiceResponse("Review Points Saved Successfully");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
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

	private void saveMilestoneReviewPoints(List<ProjectInsightMilestoneDTO> projectInsightMilestoneList, Long projectId, Long pointsBy, String pointsDrafted, String transferToKnowledgeHub) {
		try {
			for (ProjectInsightMilestoneDTO projectInsightMilestoneDTO : projectInsightMilestoneList) {
				if (projectInsightMilestoneDTO.getQuestionList() != null && !projectInsightMilestoneDTO.getQuestionList().isEmpty()) {
					saveQuestionResponsesReviewPoints(projectInsightMilestoneDTO.getQuestionList(), projectId, pointsBy, pointsDrafted, projectInsightMilestoneDTO.getMilestoneId() ,"Milestone",transferToKnowledgeHub);
				}
				if (projectInsightMilestoneDTO.getModuleList() != null && !projectInsightMilestoneDTO.getModuleList().isEmpty()) { 
					saveModuleReviewPoints(projectInsightMilestoneDTO.getModuleList(), projectId, pointsBy, pointsDrafted,transferToKnowledgeHub);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void saveModuleReviewPoints(List<ModuleDTO> moduleList, Long projectId, Long pointsBy, String pointsDrafted, String transferToKnowledgeHub) {
		try {
			for (ModuleDTO moduleDTO : moduleList) {
				if (moduleDTO.getQuestionList() != null && !moduleDTO.getQuestionList().isEmpty()) {
					saveQuestionResponsesReviewPoints(moduleDTO.getQuestionList(), projectId, pointsBy, pointsDrafted, moduleDTO.getModuleId(),"Module" ,transferToKnowledgeHub);
				}

				if (moduleDTO.getSubModuleList() != null && !moduleDTO.getSubModuleList().isEmpty()) {
					saveSubModuleReviewPoints(moduleDTO.getSubModuleList(), projectId, pointsBy, pointsDrafted, "SubModule",transferToKnowledgeHub);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
 
	private void saveSubModuleReviewPoints(List<SubModuleDTO> subModuleList, Long projectId, Long pointsBy, String pointsDrafted, String subModuleType, String transferToKnowledgeHub) {
		try {
			for (SubModuleDTO subModuleDTO : subModuleList) {
				if (subModuleDTO.getQuestionList() != null && !subModuleDTO.getQuestionList().isEmpty()) {
					saveQuestionResponsesReviewPoints(subModuleDTO.getQuestionList(), projectId, pointsBy, pointsDrafted, subModuleDTO.getSubModuleId(), subModuleType,transferToKnowledgeHub);
				}

				if (subModuleDTO.getSubSubModuleList() != null && !subModuleDTO.getSubSubModuleList().isEmpty()) {
					saveSubModuleReviewPoints(subModuleDTO.getSubSubModuleList(), projectId, pointsBy, pointsDrafted, "Sub-SubModule",transferToKnowledgeHub);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void saveQuestionResponsesReviewPoints(List<ProjectQuestionDTO> questionList, Long projectId, Long pointsBy, String pointsDrafted,Long entityId, String entityType, String transferToKnowledgeHub) {
		try {
			for (ProjectQuestionDTO projectQuestionDTO : questionList) {
				if(projectQuestionDTO.getQuestionId() != null) {
					QuestionMaster questionMaster = questionMasterRepository.findByQuestionMasterId(projectQuestionDTO.getQuestionId());
					if(questionMaster != null) {
						questionMaster.setRecommendedResponseId(projectQuestionDTO.getRecommendedResponseId());
						questionMasterRepository.save(questionMaster);
					}
				}
				if(projectQuestionDTO.getProjectResponseList() != null && !projectQuestionDTO.getProjectResponseList().isEmpty()) {
					for(ProjectResponseDTO projectResponseDTO :  projectQuestionDTO.getProjectResponseList()){
						if(projectResponseDTO.getProjectInsightResponseId() != null && !transferToKnowledgeHub.equals(null) && transferToKnowledgeHub.equals("Y")) {
							ProjectInsightResponse projectInsightResponse = projectInsightResponseRepository.findById(projectResponseDTO.getProjectInsightResponseId()).orElseGet(null);
							if(projectInsightResponse != null) {
								projectInsightResponse.setIsApprovedForKnowledgeHub(projectResponseDTO.isApprovedForKnowledgeHub() ? "Y" : "N");
								projectInsightResponseRepository.save(projectInsightResponse);
							}
						}
						saveResponsePoints(projectResponseDTO.getProjectInsightResponsePointList(),projectResponseDTO.getProjectInsightResponseId() ,pointsBy,pointsDrafted,transferToKnowledgeHub);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void saveResponsePoints(List<ProjectInsightResponsePointsDTO> projectInsightResponsePointList, Long projectInsightResponseId, Long pointsBy, String isPointsDrafted, String transferToKnowledgeHub) {
		try {
			if (projectInsightResponsePointList != null && !projectInsightResponsePointList.isEmpty()) {
				for (ProjectInsightResponsePointsDTO projectInsightResponsePointsDTO : projectInsightResponsePointList) {
					if (projectInsightResponsePointsDTO.getPoints() != null) {
						ProjectInsightResponsePoints projectInsightResponsePoints = projectInsightResponsePointsRepository.findByResponseIdAndPointsBy(projectInsightResponseId, pointsBy);
						if (projectInsightResponsePoints != null) {
							projectInsightResponsePoints.setPoints(projectInsightResponsePointsDTO.getPoints());
							projectInsightResponsePoints.setIsPointsDrafted(isPointsDrafted);
							if (isPointsDrafted.equals("N")) {
								projectInsightResponsePoints.setFinalSubmittedOn(new Timestamp(System.currentTimeMillis()));
							}
							projectInsightResponsePoints.setUpdatedOn(new Timestamp(System.currentTimeMillis()));
						} else {
							projectInsightResponsePoints = new ProjectInsightResponsePoints();
							projectInsightResponsePoints.setPointsBy(pointsBy);
							projectInsightResponsePoints.setResponseId(projectInsightResponseId);
							projectInsightResponsePoints.setPoints(projectInsightResponsePointsDTO.getPoints());
							projectInsightResponsePoints.setIsPointsDrafted(isPointsDrafted);
							if (isPointsDrafted.equals("N")) {
								projectInsightResponsePoints.setFinalSubmittedOn(new Timestamp(System.currentTimeMillis()));
							}
						}
						projectInsightResponsePointsRepository.save(projectInsightResponsePoints);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
}
