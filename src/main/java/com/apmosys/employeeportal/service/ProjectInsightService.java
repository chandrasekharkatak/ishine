package com.apmosys.employeeportal.service;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
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
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
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
import com.apmosys.employeeportal.dto.ProjectInsightFilterDTO;
import com.apmosys.employeeportal.dto.ProjectInsightEntityDTO;
import com.apmosys.employeeportal.dto.ProjectInsightMilestoneDTO;
import com.apmosys.employeeportal.dto.ProjectInsightUserContributionDTO;
import com.apmosys.employeeportal.dto.ProjectQuestionDTO;
import com.apmosys.employeeportal.dto.ProjectResponseDTO;
import com.apmosys.employeeportal.dto.SubModuleDTO;
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
import com.apmosys.employeeportal.model.ProjectInsightSubModule;
import com.apmosys.employeeportal.model.ProjectInsightUserContribution;
import com.apmosys.employeeportal.model.QuestionMaster;
import com.apmosys.employeeportal.model.TagMaster;
import com.apmosys.employeeportal.model.UserContributionDocument;
import com.apmosys.employeeportal.model.UserContributionResponseRemarks;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.ProjectInsightAssigneesRepository;
import com.apmosys.employeeportal.repository.ProjectInsightFilterOptionsRepository;
import com.apmosys.employeeportal.repository.ProjectInsightFilterRepository;
import com.apmosys.employeeportal.repository.ProjectInsightMilestoneRepository;
import com.apmosys.employeeportal.repository.ProjectInsightModuleRepository;
import com.apmosys.employeeportal.repository.ProjectInsightResponseRepository;
import com.apmosys.employeeportal.repository.ProjectInsightSubModuleRepository;
import com.apmosys.employeeportal.repository.ProjectInsightUserContributionRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.QuestionMasterRepository;
import com.apmosys.employeeportal.repository.TagMasterRepository;
import com.apmosys.employeeportal.repository.UserContributionDocumentRepository;
import com.apmosys.employeeportal.repository.UserContributionResponseRemarksRepository;
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
	            	.filter(tag -> tag != null)
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
					ProjectInsightMilestone newMilestoneCreated = saveProjectMilestone(projectInsightMilestoneDTO, projectInsightDTO.getCreatedBy(), projectInsightDTO.getProjectId(), combinedText);

					if (newMilestoneCreated.getMilestoneId() != null) {
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
						if (!projectInsightMilestoneDTO.getModuleList().isEmpty()) {
							projectInsightMilestoneDTO.getModuleList().forEach((module) -> {
								ProjectInsightModule moduleResponse = saveProjectModule(module, milestoneId, projectInsightDTO.getCreatedBy(), combinedText);

								if (moduleResponse != null) {
									isSuccess.set(true);
									Long moduleId = moduleResponse.getModuleId();
									
									// Module Assign to
									saveAssignedTo(module.getAssignedToUserId(), moduleId, "Module",false);

									// Add Module question
									if (!module.getQuestionList().isEmpty()) {
										ServiceResponse moduleQuestionResponse = addUpdateQuestion(module.getQuestionList(), moduleId, "Module", combinedText);
										isSuccess.set("Success".equals(moduleQuestionResponse.getServiceStatus()));
									}

									// Save SubModules
									saveSubModuleList(module.getSubModuleList(), moduleId, "SubModule", combinedText);
								}
							});
						}
					} else {
						apiLogInfo.setApiResponse("Failed to create Project Insight Questions");
						response.setServiceResponse("Failed to create Project Insight Questions.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					}
				});

				if (isSuccess.get()) {
					
					//save for creating tags
					ProjectInsightDTO dtoObject = new ProjectInsightDTO();
					dtoObject.setProjectText(combinedText);
					dtoObject.setProjectId(projectInsightDTO.getProjectId());
					dtoObject.setTagType("create");
					
					String tagSaveResponse = saveProjectWiseTags(dtoObject);
					
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

	private ProjectInsightMilestone saveProjectMilestone(ProjectInsightMilestoneDTO projectInsightMilestoneDTO, Long createdBy, Long projectId, StringBuilder combinedText) {
		try {
			
			combinedText.append(projectInsightMilestoneDTO.getMilestone()).append(" ")
									.append(projectInsightMilestoneDTO.getDescription());
			
			ProjectInsightMilestone projectInsightMilestone = new ProjectInsightMilestone();
			projectInsightMilestone.setCreatedBy(createdBy);
			projectInsightMilestone.setProjectId(projectId);
			projectInsightMilestone.setMilestone(projectInsightMilestoneDTO.getMilestone());
			projectInsightMilestone.setDescription(projectInsightMilestoneDTO.getDescription());
			projectInsightMilestone.setDeptId(projectInsightMilestoneDTO.getDeptId());
			projectInsightMilestone.setRedmineId(projectInsightMilestoneDTO.getRedmineId());
			return projectInsightMilestoneRepository.save(projectInsightMilestone);
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
				
				combinedText.append(questionObj.getQuestion()).append(" ").append(questionObj.getDescription())
				.append(questionObj.getOptions());
				
				// update
				if (questionObj.getQuestionId() != null) {
					QuestionMaster quesDbObject = questionMasterRepository.findByQuestionMasterId(questionObj.getQuestionId());
					if (quesDbObject != null) {
						
						quesDbObject.setQuestion(questionObj.getQuestion());
						quesDbObject.setDescription(questionObj.getDescription());
						quesDbObject.setDocumentUpload(questionObj.getDocumentUpload());
						quesDbObject.setOptions(questionObj.getOptions());
						quesDbObject.setOptionType(questionObj.getOptionType());
						quesDbObject.setRequired(questionObj.getRequired());
						questionList.add(quesDbObject);
					}
				} else {
					// Add
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
				objectList = projectInsightMilestoneRepository.getAllProjectInsight();
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
						ProjectInsightMilestoneDTO mileStoneProjObject =  mapMilestone(mileStone);   
						
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

	private String getProjectManagerName(Long projectManagerId) {
		try {
			 Optional<Employee> employee= employeeRepository.findById(projectManagerId);
			 if(employee.isPresent())
			 return employee.get().getName();
			 else 
				 return "";
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private ProjectInsightMilestoneDTO mapMilestone(ProjectInsightMilestone mileStone) {
		try {
			ProjectInsightMilestoneDTO mileStoneProjObject = new ProjectInsightMilestoneDTO();
			List<ProjectInsightAssignees> assignToDbList = projectInsightAssigneesRepository.getByEntityIdAndEntityType(mileStone.getMilestoneId(), "Milestone");
			if (!assignToDbList.isEmpty()) {
				mileStoneProjObject.setAssignedToUserId(assignToDbList.stream().map(ProjectInsightAssignees::getAssignedTo).collect(Collectors.toList()));
			}

			mileStoneProjObject.setDeptId(mileStone.getDeptId());
			mileStoneProjObject.setDescription(mileStone.getDescription());
			mileStoneProjObject.setMilestone(mileStone.getMilestone());
			mileStoneProjObject.setMilestoneId(mileStone.getMilestoneId());
			mileStoneProjObject.setRedmineId(mileStone.getMilestoneId());
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
					}
					submodDto.setCreatedBy(submodule.getCreatedBy());
					submodDto.setCreatedOn(submodule.getCreatedOn().toString());
					submodDto.setDescription(submodule.getDescription());
					submodDto.setModuleId(submodule.getModuleId());
					submodDto.setRedmineId(submodule.getRedmineId());
					submodDto.setSubModule(submodule.getSubmodule());
					submodDto.setSubmoduleId(submodule.getSubmoduleId());
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
			
			if(projectInsightDTO.getDeletedProjectInsightEntityList() !=null && !projectInsightDTO.getDeletedProjectInsightEntityList().isEmpty()) {
				deleteProjectInsightEntities(projectInsightDTO.getDeletedProjectInsightEntityList());
			}
			
			// Add/Update Project question
			if (!projectInsightDTO.getQuestionList().isEmpty()) {
				addUpdateQuestion(projectInsightDTO.getQuestionList(), projectInsightDTO.getProjectId(), "Project", combinedText);
			}

			if (projectInsightDTO.getProjectInsightMilestoneList() != null && !projectInsightDTO.getProjectInsightMilestoneList().isEmpty()) {
				projectInsightDTO.getProjectInsightMilestoneList().forEach((milestone) -> {

					combinedText.append(milestone.getMilestone()).append(" ")
					.append(milestone.getDescription());
					
					if (milestone.getMilestoneId() != null) {
						ProjectInsightMilestone milestoneDbObject = projectInsightMilestoneRepository.getByMilestoneId(milestone.getMilestoneId());

						if (milestoneDbObject != null) {
							milestoneDbObject.setUpdatedBy(projectInsightDTO.getUpdatedBy());
							milestoneDbObject.setMilestone(milestone.getMilestone());
							milestoneDbObject.setDescription(milestone.getDescription());
							milestoneDbObject.setDeptId(milestone.getDeptId());
							milestoneDbObject.setRedmineId(milestone.getRedmineId());
							ProjectInsightMilestone milestoneUpdateDbResponse = projectInsightMilestoneRepository.save(milestoneDbObject);

							if (milestoneUpdateDbResponse != null) {
								// Add/Update Milestone question
								if (milestone.getQuestionList() != null && !milestone.getQuestionList().isEmpty()) {
									addUpdateQuestion(milestone.getQuestionList(), milestoneUpdateDbResponse.getMilestoneId() , "Milestone", combinedText);
								}

								// Milestone Assign to
								saveAssignedTo(milestone.getAssignedToUserId(), milestone.getMilestoneId(), "Milestone", true);

								// --> Module
								if (!milestone.getModuleList().isEmpty()) {
									milestone.getModuleList().forEach((module) -> {
										ProjectInsightModule moduleDbResponse = saveOrUpdateModule(module, milestoneUpdateDbResponse.getMilestoneId(), projectInsightDTO.getUpdatedBy(), projectInsightDTO.getCreatedBy(), combinedText);
									
										
										if (moduleDbResponse != null) {
											Long moduleId = moduleDbResponse.getModuleId();

											// Module Assign to
											saveAssignedTo(module.getAssignedToUserId(), moduleId, "Module", true);

											// Add/Update Module question
											if (module.getQuestionList() != null
													&& !module.getQuestionList().isEmpty()) {
												addUpdateQuestion(module.getQuestionList(), moduleId, "Module", combinedText);
											}

											// --> SubModule
											if (!module.getSubModuleList().isEmpty()) {
												saveOrUpdateSubModuleList(module.getSubModuleList(), moduleId, projectInsightDTO.getUpdatedBy(), "SubModule", combinedText);
											}
										}
									});
								}
							} else {
								apiLogInfo.setApiResponse("Unable to update project insight milestone.");
								response.setServiceResponse("Unable to update project insight milestone.");
								apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							}
						} else {
							apiLogInfo.setApiResponse("No Project Insight Milestone Found.");
							response.setServiceResponse("No Project Insight Milestone Found.");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						}
					} else {
						// Create New Milestone
						ProjectInsightMilestone newMilestoneCreated = saveProjectMilestone(milestone,
								projectInsightDTO.getUpdatedBy(), projectInsightDTO.getProjectId(), combinedText);
						if (newMilestoneCreated.getMilestoneId() != null) {
							Long milestoneId = newMilestoneCreated.getMilestoneId();

							// Milestone Assign to
							saveAssignedTo(milestone.getAssignedToUserId(), milestoneId, "Milestone", false);

							// Add Milestone Question
							if (!milestone.getQuestionList().isEmpty()) {
								addUpdateQuestion(milestone.getQuestionList(), milestoneId, "Milestone", combinedText);
							}

							// Add Module
							if (!milestone.getModuleList().isEmpty()) {
								milestone.getModuleList().forEach((module) -> {
									ProjectInsightModule moduleResponse = saveProjectModule(module, milestoneId,
											projectInsightDTO.getCreatedBy(), combinedText);

									if (moduleResponse != null) {
										Long moduleId = moduleResponse.getModuleId();

										// Module Assign to
										saveAssignedTo(module.getAssignedToUserId(), moduleId, "Module", false);

										// Add Module question
										if (!module.getQuestionList().isEmpty()) {
											addUpdateQuestion(module.getQuestionList(), moduleId, "Module", combinedText);
										}

										// Save SubModules
										saveSubModuleList(module.getSubModuleList(), moduleId, "SubModule", combinedText);
									}
								});
							}
						} else {
							apiLogInfo.setApiResponse("Failed to create Project Insight Milestone");
							response.setServiceResponse("Failed to create Project Insight Milestone.");
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						}
					}
				});
				
				//save for creating tags
				ProjectInsightDTO dtoObject = new ProjectInsightDTO();
				dtoObject.setProjectText(combinedText);
				dtoObject.setProjectId(projectInsightDTO.getProjectId());
				dtoObject.setTagType("update");
				
				String tagSaveUpdateResponse = saveProjectWiseTags(dtoObject);
				
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

	private void deleteProjectInsightQuestion(Long questionId,String entityType) {
		try {
			entityType = entityType.equals(null) ? "Question" : entityType;
			 projectInsightResponseRepository.deleteAllProjectInsightResponseByQuestionMasterId(questionId);
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
					
					
					if (subModuleDTO.getSubmoduleId() != null) {
						ProjectInsightSubModule submoduleDbResponse = projectInsightSubModuleRepository.getById(subModuleDTO.getSubmoduleId());
						if (submoduleDbResponse != null) {
							submoduleDbResponse.setModuleId(moduleId);
							submoduleDbResponse.setDescription(subModuleDTO.getDescription());
							submoduleDbResponse.setRedmineId(subModuleDTO.getRedmineId());
							submoduleDbResponse.setSubmodule(subModuleDTO.getSubModule());
							submoduleDbResponse.setUpdatedBy(updatedBy);
							submoduleDbResponse.setSubModuleType(subModuleType);
							projectInsightSubModuleRepository.save(submoduleDbResponse);
							saveAssignedTo(subModuleDTO.getAssignedToUserId(), subModuleDTO.getSubmoduleId(),subModuleType, true);

							// Add SubModule question
							if (subModuleDTO.getQuestionList() != null && !subModuleDTO.getQuestionList().isEmpty()) {
								addUpdateQuestion(subModuleDTO.getQuestionList(), subModuleDTO.getSubmoduleId(), subModuleType, combinedText);
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

	private ProjectInsightModule saveOrUpdateModule(ModuleDTO module, Long milestoneId, Long updatedBy, Long createdBy, StringBuilder combinedText) {
		try {
			ProjectInsightModule moduleDbResponse;
			if (module.getModuleId() != null) {
				ProjectInsightModule moduleDbObject = projectInsightModuleRepository.getByModuleId(module.getModuleId());

				combinedText.append(module.getModule()).append(" ")
									.append(module.getDescription());
				
				if (moduleDbObject != null) {
					moduleDbObject.setDescription(module.getDescription());
					moduleDbObject.setModule(module.getModule());
					moduleDbObject.setRedmineId(module.getRedmineId());
					moduleDbObject.setUpdatedBy(updatedBy);
					moduleDbObject.setUpdatedOn(LocalDateTime.now());
					moduleDbResponse = projectInsightModuleRepository.save(moduleDbObject);
				} else {
					moduleDbResponse = saveProjectModule(module, milestoneId, createdBy, combinedText);
				}
			} else {
				moduleDbResponse = saveProjectModule(module, milestoneId, createdBy, combinedText);
			}
			
			return moduleDbResponse;
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
					subModuleDTO.setSubmoduleId(projectInsightSubModule.getSubmoduleId());
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
			if (projectInsightDTO.getProjectInsightMilestoneList() != null && !projectInsightDTO.getProjectInsightMilestoneList().isEmpty()) {
				saveProjectResponse(projectInsightDTO.getQuestionList(), projectInsightDTO.getEmpId(), files, reviewerId,projectInsightDTO.getProjectId(), combinedText);
				saveProjectMileStoneResponse(projectInsightDTO.getProjectInsightMilestoneList(), projectInsightDTO.getEmpId(), files, reviewerId, combinedText);
				saveTaggedForHelp(projectInsightDTO.getTaggedToUserId(), projectInsightDTO.getProjectId(), "Project", projectInsightDTO.getEmpId());
				
				//save for creating tags
				ProjectInsightDTO dtoObject = new ProjectInsightDTO();
				dtoObject.setProjectText(combinedText);
				dtoObject.setProjectId(projectInsightDTO.getProjectId());
				dtoObject.setTagType("response");
				String tagSaveResponse = saveProjectWiseTags(dtoObject);
				
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
	
	
	private void saveProjectResponse(List<ProjectQuestionDTO> questionList, Long empId, List<MultipartFile> files, Long reviewerId, Long projectId, StringBuilder combinedText) {
		try {
			if (questionList != null && !questionList.isEmpty()) {
			saveProjectInsightResponse(questionList, files, reviewerId, empId, combinedText);
				for (ProjectQuestionDTO projectQuestionDTO : questionList) {
					saveTaggedForHelp(projectQuestionDTO.getTaggedForHelp(), projectQuestionDTO.getQuestionId(), "Question", empId);
				}
			} 
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private String saveProjectMileStoneResponse(List<ProjectInsightMilestoneDTO> projectInsightMilestoneDTOList, Long employeeId, List<MultipartFile> files, Long reviewerId, StringBuilder combinedText) {
		String response = null;
		try {
			if (projectInsightMilestoneDTOList != null && !projectInsightMilestoneDTOList.isEmpty()) {
				for (ProjectInsightMilestoneDTO projectInsightQuestionDTO : projectInsightMilestoneDTOList) {
					saveProjectInsightResponse(projectInsightQuestionDTO.getQuestionList(), files, reviewerId,employeeId, combinedText);
					saveProjectInsightModuleResponse(projectInsightQuestionDTO.getModuleList(), employeeId, files, reviewerId, combinedText);
					saveTaggedForHelp(projectInsightQuestionDTO.getTaggedToUserId(),projectInsightQuestionDTO.getMilestoneId(),"Milestone",employeeId);
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

	private void saveProjectInsightModuleResponse(List<ModuleDTO> moduleList, Long employeeId,List<MultipartFile> files,Long reviewerId, StringBuilder combinedText) {
		try {
			if (moduleList != null && !moduleList.isEmpty()) {
				for (ModuleDTO moduleDTO : moduleList) {
					saveProjectInsightResponse(moduleDTO.getQuestionList(),files,reviewerId,employeeId,combinedText);
					saveProjectInsightSubModuleResponse(moduleDTO.getSubModuleList(), employeeId,files,reviewerId,"SubModule",combinedText);
					saveTaggedForHelp(moduleDTO.getTaggedToUserId(),moduleDTO.getModuleId(),"Module",employeeId);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void saveProjectInsightSubModuleResponse(List<SubModuleDTO> subModuleList, Long employeeId,List<MultipartFile> files,Long reviewerId,String subModuleType, StringBuilder combinedText) {
		try {
			if (subModuleList != null && !subModuleList.isEmpty()) {
				for (SubModuleDTO subModuleDTO : subModuleList) {
					saveProjectInsightResponse(subModuleDTO.getQuestionList(),files,reviewerId,employeeId,combinedText);
					saveTaggedForHelp(subModuleDTO.getTaggedToUserId(),subModuleDTO.getSubmoduleId(),subModuleType,employeeId);
					if(subModuleDTO.getSubSubModuleList() != null && !subModuleDTO.getSubSubModuleList().isEmpty()) {
						saveProjectInsightSubModuleResponse(subModuleDTO.getSubSubModuleList(), employeeId, files, reviewerId,"Sub-SubModule",combinedText);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void saveProjectInsightResponse(List<ProjectQuestionDTO> projectQuestionList, List<MultipartFile> files, Long reviewerId, Long employeeId, StringBuilder combinedText) {
		try {
			if (projectQuestionList != null && !projectQuestionList.isEmpty()) {
				for (ProjectQuestionDTO projectQuestionDTO : projectQuestionList) {
					if (projectQuestionDTO.getQuestionId() != null
							&& projectQuestionDTO.getProjectResponseList() != null
							&& !projectQuestionDTO.getProjectResponseList().isEmpty()) {
						for (ProjectResponseDTO projectResponseDTO : projectQuestionDTO.getProjectResponseList()) {
							saveOrUpdateAllProjectResponseAndUploadDocument(projectQuestionDTO, projectResponseDTO, files, reviewerId, combinedText);
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

	private void saveOrUpdateAllProjectResponseAndUploadDocument(ProjectQuestionDTO projectQuestionDTO, ProjectResponseDTO projectResponseDTO, List<MultipartFile> files, Long reviewerId, StringBuilder combinedText) {
		try {
			ProjectInsightResponse projectInsightResponse = projectInsightResponseRepository.findByEmpIdAndQuestionMasterId(projectResponseDTO.getResponseByEmpId(), projectQuestionDTO.getQuestionId());
			if (projectInsightResponse != null) {
				projectInsightResponse = mapProjectResponseDTOToProjectResponseAndUploadFile(projectInsightResponse,projectResponseDTO ,files,reviewerId,projectQuestionDTO.getQuestionId(), combinedText);
				if (files != null) {}
			} else {
				projectInsightResponse = new ProjectInsightResponse();
				projectInsightResponse = mapProjectResponseDTOToProjectResponseAndUploadFile(projectInsightResponse,projectResponseDTO ,files,reviewerId,projectQuestionDTO.getQuestionId(), combinedText);
			}
			projectInsightResponseRepository.save(projectInsightResponse);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private ProjectInsightResponse mapProjectResponseDTOToProjectResponseAndUploadFile(ProjectInsightResponse projectInsightResponse, ProjectResponseDTO projectResponseDTO,List<MultipartFile> files, Long reviewerId, Long questionMasterId, StringBuilder combinedText) {
		try {
			combinedText.append(projectResponseDTO.getResponse()).append(" ")
			            .append(projectResponseDTO.getUploadedFileName());
			
			
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
				Set<Integer> projectIds = matchedTags.stream().map(TagMaster::getProjectId).map(Long::intValue).collect(Collectors.toSet());
				List<PoProjectSyncDTO> allProjectInfoList = getProjectByProjectIds(projectIds);
				
				if(!allProjectInfoList.isEmpty()) {
					response.setProjectList(allProjectInfoList);
				}else {
					return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok(response);
	}
	
	public List<PoProjectSyncDTO> getProjectByProjectIds(Set<Integer> projectIds) {
	    List<PoProjectSyncDTO> response = new ArrayList<>();
	    try {
	        List<Project> projects = projectRepository.findAllById(projectIds);

	        if (!projects.isEmpty()) {
	            for (Project project : projects) {
	                PoProjectSyncDTO dto = new PoProjectSyncDTO();

	                List<Object[]> deptData = departmentRepository.getMappedDepartment(project.getProjectId());
	                List<String> departmentNames = deptData.stream()
	                        .map(obj -> obj[0] != null ? obj[0].toString() : null)
	                        .filter(Objects::nonNull)
	                        .collect(Collectors.toList());
	                Employee empObject = employeeRepository.findByEmpId(project.getProjectManagerId());
	                List<TagMaster> tagMasterList = tagMasterRepository.findByProjectId(project.getProjectId().longValue());

	                dto.setProjectId(project.getProjectId());
	                dto.setProjectName(project.getProjectName());
	                dto.setDepartmentList(departmentNames.toArray(new String[0]));
	                dto.setProjectManagerId(project.getProjectManagerId());
	                dto.setProjectManagerName(empObject != null ? empObject.getName() : null);
	                dto.setState(project.getState());
	                dto.setTagList(tagMasterList.stream().map(TagMaster::getTag).limit(8).collect(Collectors.toList()));
	                dto.setCreatedOn(project.getCreatedOn());
	                dto.setUpdatedOn(project.getUpdatedOn() != null ? project.getUpdatedOn().toString():null);
	                
	                response.add(dto);
	            }
	        }

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
	                dto.setResponseList(responseRemarkDbResp);
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
	                        String uploadResponse = uploadProjectResponseDocument(document);
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
	                dto.setResponseList(responseRemarkDbResp);
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
	
}
