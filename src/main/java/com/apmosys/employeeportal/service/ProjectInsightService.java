package com.apmosys.employeeportal.service;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;

import java.net.URI;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.GraphLookupOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
// import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.apmosys.employeeportal.Exception.BadRequestException;
import com.apmosys.employeeportal.Exception.EmployeeNotFoundException;
import com.apmosys.employeeportal.Exception.GlobalException;
import com.apmosys.employeeportal.dto.ApprovalRequest;
import com.apmosys.employeeportal.dto.EmployeeDocumentDTO;
import com.apmosys.employeeportal.dto.FormFieldDTO;
import com.apmosys.employeeportal.dto.GroupSectionData;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.ModuleDTO;
import com.apmosys.employeeportal.dto.OptionDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.ProjectInsighProjectMappingDTO;
import com.apmosys.employeeportal.dto.ProjectInsightDTO;
import com.apmosys.employeeportal.dto.ProjectInsightDetailsExcelDTO;
import com.apmosys.employeeportal.dto.ProjectInsightDomainDataDto;
import com.apmosys.employeeportal.dto.ProjectInsightEntityDTO;
import com.apmosys.employeeportal.dto.ProjectInsightFilterDTO;
import com.apmosys.employeeportal.dto.ProjectInsightMilestoneDTO;
import com.apmosys.employeeportal.dto.ProjectInsightResponsePointsDTO;
import com.apmosys.employeeportal.dto.ProjectInsightUserContributionDTO;
import com.apmosys.employeeportal.dto.ProjectQuestionDTO;
import com.apmosys.employeeportal.dto.ProjectQuestionStatusDto;
import com.apmosys.employeeportal.dto.ProjectResponseDTO;
import com.apmosys.employeeportal.dto.ProjectSectionData;
import com.apmosys.employeeportal.dto.QuesAndResponseDto;
import com.apmosys.employeeportal.dto.QuestionGroupRequest;
import com.apmosys.employeeportal.dto.QuestionMappedStatusDto;
import com.apmosys.employeeportal.dto.RefreshRequestDto;
import com.apmosys.employeeportal.dto.ReviewerInfoDTO;
import com.apmosys.employeeportal.dto.SubModuleDTO;
import com.apmosys.employeeportal.dto.TagDTO;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeTeamMap;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectInsighProjectMapping;
import com.apmosys.employeeportal.model.ProjectInsightAssignees;
import com.apmosys.employeeportal.model.ProjectInsightDomainData;
import com.apmosys.employeeportal.model.ProjectInsightDomainDataFlatSearch;
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
import com.apmosys.employeeportal.mongodb.dto.ClientDTO;
import com.apmosys.employeeportal.mongodb.dto.FormDataDTO;
import com.apmosys.employeeportal.mongodb.dto.OptionValueDTO;
import com.apmosys.employeeportal.mongodb.dto.ProjectInsightDetailsDTO;
import com.apmosys.employeeportal.mongodb.dto.ProjectInsightQuestionDTO;
import com.apmosys.employeeportal.mongodb.modal.FileStorage;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightFormDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightGroupDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightProjectDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightQuestionDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightResponseDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightResponseDetailsHistory;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightStructure;
import com.apmosys.employeeportal.mongodb.repository.FileMongoRepository;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightFormDetailsRepository;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightGroupDetailsRepository;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightProjectDetailsRepository;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightProjectFlatSearchRepository;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightQuestionDetailsRepository;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightResponseDetailsHistoryRepository;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightResponseDetailsRepository;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightStructureRepository;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.ProjectInsighProjectMappingRepository;
import com.apmosys.employeeportal.repository.ProjectInsightAssigneesRepository;
import com.apmosys.employeeportal.repository.ProjectInsightDomainDataFlatSearchRepository;
import com.apmosys.employeeportal.repository.ProjectInsightDomainDataRepository;
import com.apmosys.employeeportal.repository.ProjectInsightDomainRepository;
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
import com.apmosys.employeeportal.utility.ValidationUtility;

@Service
public class ProjectInsightService {

	private static final List<String> ROLE_HIERARCHY = Arrays.asList("EMPLOYEE", "TEAMLEAD", "MANAGER", "HOD",
			"SUPERADMIN", "RMG", "HR");

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
	private ProjectInsightResponseDetailsHistoryRepository projectInsightResponseDetailsHistoryRepository;

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

	@Value("")
	private String dmsPortalUrl;

	@Value("")
	private String dmsPortalUploadUrlKey;

	@Value("")
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

	@Autowired
	ProjectInsightStructureRepository projectInsightStructureRepository;

	@Autowired
	ProjectInsighProjectMappingRepository projectInsighProjectMappingRepository;
	
	@Autowired
	private ProjectInsightDomainRepository projectInsightDomainRepository;
	
	@Autowired
	private ProjectInsightProjectDetailsRepository projectInsightProjectDetailsRepository;

	@Autowired
	private ProjectInsightGroupDetailsRepository projectInsightGroupDetailsRepository ;
	
	@Autowired
	private ProjectInsightFormDetailsRepository projectInsightFormDetailsRepository ;
	
	@Autowired
	private ProjectInsightQuestionDetailsRepository projectInsightQuestionDetailsRepository;
	
	@Autowired 
	private ProjectInsightQuestionLibraryService projectInsightQuestionLibraryService;
	
	@Autowired
	private ProjectInsightResponseDetailsRepository projectInsightResponseDetailsRepository;

	@Autowired
	private ProjectInsightProjectFlatSearchRepository projectInsightProjectFlatSearchRepository;

	@Transactional
	public ServiceResponse createProjectInsightQuestion(ProjectInsightDTO projectInsightDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setApiUrl("/api/createProjectInsightQuestion");
		apiLogInfo.setSubFeatureName("createProjectInsightQuestion");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("CreatedBy : " + projectInsightDTO.getCreatedBy() + " ,ProjectId :"
				+ projectInsightDTO.getProjectId() + " ,Created By EmpId :" + projectInsightDTO.getCreatedBy());

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

			combinedText.append(projectInsightDTO.getProjectName()).append(" ")
					.append(projectInsightDTO.getProjectManagerName()).append(" ");

			Project project = projectRepository
					.findByProjectId(Integer.parseInt(projectInsightDTO.getProjectId().toString()));
			if (project == null) {
				throw new RuntimeException("Project Not Found!!");
			}

			// Project Assign to ProjectManager
			List<Long> assignedToProjectManager = new ArrayList<>();
			assignedToProjectManager.add(project.getProjectManagerId());
			projectInsightDTO.setAssignedToUserId(assignedToProjectManager);
			saveAssignedTo(projectInsightDTO.getAssignedToUserId(), projectInsightDTO.getProjectId(), "Project", false);

			// Add Application Question
			if (!projectInsightDTO.getQuestionList().isEmpty()) {
				ServiceResponse applicationQuestionResponse = addUpdateQuestion(projectInsightDTO.getQuestionList(),
						projectInsightDTO.getProjectId(), "Project", combinedText);
				isSuccess.set("Success".equals(applicationQuestionResponse.getServiceStatus()));
				response.setServiceResponse("Project Insight created successfully.");
				apiLogInfo.setApiResponse("Project Insight Successfully");
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}

			// Add Milestone
			if (!projectInsightDTO.getProjectInsightMilestoneList().isEmpty()) {
				projectInsightDTO.getProjectInsightMilestoneList().forEach((projectInsightMilestoneDTO) -> {
					ProjectInsightMilestone newMilestoneCreated = saveOrUpdateProjectMilestone(
							projectInsightMilestoneDTO, projectInsightDTO.getProjectId(), combinedText,
							projectInsightDTO.getCreatedBy(), projectInsightDTO.getUpdatedBy());

					if (newMilestoneCreated != null && newMilestoneCreated.getMilestoneId() != null) {
						isSuccess.set(true);
						Long milestoneId = newMilestoneCreated.getMilestoneId();

						// Milestone Assign to
						saveAssignedTo(projectInsightMilestoneDTO.getAssignedToUserId(), milestoneId, "Milestone",
								false);

						// Add Milestone Question
						if (!projectInsightMilestoneDTO.getQuestionList().isEmpty()) {
							ServiceResponse milestoneQuestionResponse = addUpdateQuestion(
									projectInsightMilestoneDTO.getQuestionList(), milestoneId, "Milestone",
									combinedText);
							isSuccess.set("Success".equals(milestoneQuestionResponse.getServiceStatus()));
						}

						// Add Module
						if (projectInsightMilestoneDTO.getModuleList() != null
								&& !projectInsightMilestoneDTO.getModuleList().isEmpty()) {

							for (ModuleDTO moduleDTO : projectInsightMilestoneDTO.getModuleList()) {
								ProjectInsightModule projectInsightModule = saveOrUpdateModule(moduleDTO, milestoneId,
										combinedText, projectInsightDTO.getCreatedBy(),
										projectInsightDTO.getUpdatedBy());
								if (projectInsightModule.getModuleId() != null) {
									Long moduleId = projectInsightModule.getModuleId();

									// Module Assign to
									saveAssignedTo(moduleDTO.getAssignedToUserId(), moduleId, "Module", true);

									// Add/Update Module question
									if (moduleDTO.getQuestionList() != null && !moduleDTO.getQuestionList().isEmpty()) {
										addUpdateQuestion(moduleDTO.getQuestionList(), moduleId, "Module",
												combinedText);
									}

									if (!moduleDTO.getSubModuleList().isEmpty()) {
										saveOrUpdateSubModuleList(moduleDTO.getSubModuleList(), moduleId, "SubModule",
												combinedText, projectInsightDTO.getCreatedBy(),
												projectInsightDTO.getUpdatedBy());
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

	private ProjectInsightModule saveProjectModule(ModuleDTO module, Long milestoneId, Long createdBy,
			StringBuilder combinedText) {
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

	private ProjectInsightMilestone saveOrUpdateProjectMilestone(ProjectInsightMilestoneDTO projectInsightMilestoneDTO,
			Long projectId, StringBuilder combinedText, Long createdBy, Long updatedBy) {
		try {
			ProjectInsightMilestone projectInsightMilestone = null;
			if (projectInsightMilestoneDTO.getMilestoneId() == null
					|| (projectInsightMilestoneDTO.getActionType() != null
							&& projectInsightMilestoneDTO.getActionType().equalsIgnoreCase("Add"))) {
				projectInsightMilestone = saveProjectInsightMilestone(projectInsightMilestone,
						projectInsightMilestoneDTO, projectId, createdBy, updatedBy);
			} else if (projectInsightMilestoneDTO.getMilestoneId() != null) {
				ProjectInsightMilestone milestoneDbObject = projectInsightMilestoneRepository
						.getByMilestoneId(projectInsightMilestoneDTO.getMilestoneId());
				if (milestoneDbObject != null) {
					projectInsightMilestone = saveProjectInsightMilestone(milestoneDbObject, projectInsightMilestoneDTO,
							projectId, createdBy, updatedBy);
				} else {
					projectInsightMilestone = saveProjectInsightMilestone(projectInsightMilestone,
							projectInsightMilestoneDTO, projectId, createdBy, updatedBy);
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

	public ProjectInsightMilestone saveProjectInsightMilestone(ProjectInsightMilestone projectInsightMilestone,
			ProjectInsightMilestoneDTO projectInsightMilestoneDTO, Long projectId, Long createdBy, Long updatedBy) {
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

	private void saveSubModuleList(List<SubModuleDTO> subModuleList, Long moduleId, String subModuleType,
			StringBuilder combinedText) {
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
					ProjectInsightSubModule submoduleResponse = projectInsightSubModuleRepository
							.save(newProjectInsightSubModule);
					if (submoduleResponse != null) {
						Long submoduleId = submoduleResponse.getSubmoduleId();

						// Assign to
						saveAssignedTo(subModuleDTO.getAssignedToUserId(), submoduleId, subModuleType, false);

						// Add SubModule question
						if (subModuleDTO.getQuestionList() != null && !subModuleDTO.getQuestionList().isEmpty()) {
							addUpdateQuestion(subModuleDTO.getQuestionList(), submoduleId, subModuleType, combinedText);
						}

						if (subModuleDTO.getSubSubModuleList() != null
								&& !subModuleDTO.getSubSubModuleList().isEmpty()) {
							saveSubModuleList(subModuleDTO.getSubSubModuleList(), submoduleId, "Sub-SubModule",
									combinedText);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public QuestionMaster saveQuestionMaster(QuestionMaster questionMaster, ProjectQuestionDTO projectQuestionDTO,
			Long entityId, String entity) {
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

	private void saveAssignedTo(List<Long> assignedToUserId, Long entityId, String entityType,
			boolean deleteExistingRecords) {
		try {
			if (assignedToUserId != null && !assignedToUserId.isEmpty()) {
				if (deleteExistingRecords) {
					List<ProjectInsightAssignees> milestoneAssigneeDbResponse = projectInsightAssigneesRepository
							.getByEntityIdAndEntityType(entityId, entityType);
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

	public ServiceResponse addUpdateQuestion(List<ProjectQuestionDTO> questionAddList, Long entityId, String entity,
			StringBuilder combinedText) {
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

				if (questionObj.getQuestionId() == null || (questionObj.getActionType() != null
						&& questionObj.getActionType().equalsIgnoreCase("Add"))) {
					questionList.add(saveQuestionMaster(null, questionObj, entityId, entity));
				} else if (questionObj.getQuestionId() != null) {
					QuestionMaster quesDbObject = questionMasterRepository
							.findByQuestionMasterId(questionObj.getQuestionId());
					if (quesDbObject != null) {
						questionList.add(saveQuestionMaster(quesDbObject, questionObj, entityId, entity));
					} else {
						questionList.add(saveQuestionMaster(null, questionObj, entityId, entity));
					}
				}
			});

			List<QuestionMaster> listSaved = questionMasterRepository.saveAll(questionList);
			if (!listSaved.isEmpty()) {
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
				List<Long> projectIds = new ArrayList<Long>();
				objectList = projectInsightMilestoneRepository.getAllProjectInsight();
				if (objectList != null && !objectList.isEmpty()) {
					projectIds = objectList.stream().map(object -> parseLong(object[0])).collect(Collectors.toList());
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

			if (projectInsightDTO.getProjectId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project Id does not exists.");
				apiLogInfo.setApiResponse("Project Id does not exists");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				return response;
			}

			Project project = projectRepository
					.findByProjectId(Integer.parseInt(projectInsightDTO.getProjectId().toString()));
			if (project == null) {
				throw new RuntimeException("Project Not Found!!");
			}

			List<ProjectQuestionDTO> projectQuestionList = getQuestion(projectInsightDTO.getProjectId(), "Project");
			List<ProjectInsightMilestone> mileStoneList = projectInsightMilestoneRepository
					.getByProjectId(projectInsightDTO.getProjectId());
			if ((projectQuestionList == null || projectQuestionList.isEmpty())
					&& (mileStoneList == null || mileStoneList.isEmpty())) {
				response.setServiceResponse("No Questions or Milestone Found for Project.");
				apiLogInfo.setApiResponse("No Questions or Milestone Found for Project.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				return response;
			}

			ProjectInsightDTO responseObject = new ProjectInsightDTO();
			responseObject.setProjectId(projectInsightDTO.getProjectId());
			responseObject.setProjectName(project.getProjectName());
			responseObject.setProjectManagerId(project.getProjectManagerId());
			responseObject.setProjectManagerName(getProjectManagerName(project.getProjectManagerId()));
			responseObject.setCreatedBy(project.getCreatedBy());
			responseObject.setQuestionList(projectQuestionList);

			if (mileStoneList != null && !mileStoneList.isEmpty()) {
				List<ProjectInsightMilestoneDTO> projectInsightQuestion = new ArrayList<>();
				mileStoneList.forEach((mileStone) -> {
					ProjectInsightMilestoneDTO mileStoneProjObject = mapMilestone(mileStone,
							projectInsightDTO.getProjectId());

					List<ProjectInsightModule> moduleList = projectInsightModuleRepository
							.getByMilestoneId(mileStone.getMilestoneId());
					List<ModuleDTO> moduleResponseList = new ArrayList<>();
					if (moduleList != null && !moduleList.isEmpty()) {
						moduleList.forEach((module) -> {
							ModuleDTO modDto = mapProjectModule(module);

							List<ProjectInsightSubModule> subModuleList = projectInsightSubModuleRepository
									.getByModuleIdAndSubModuleType(module.getModuleId(), "SubModule");
							modDto.setSubModuleList(getSubModuleQuestionList(subModuleList, "SubModule"));
							moduleResponseList.add(modDto);
						});
					}
					mileStoneProjObject.setModuleList(moduleResponseList);
					projectInsightQuestion.add(mileStoneProjObject);
				});
				responseObject.setProjectInsightMilestoneList(projectInsightQuestion);
			}

			response.setServiceResponse(responseObject);
			apiLogInfo.setApiResponse("All Questions By MilestoneId Fetched");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
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

			List<Object[]> employeePersonaListForTeam = employeeTeamMapRepository
					.getEmployeePersonaForProject(projectInsightDTO.getEmpId(), projectInsightDTO.getProjectId());
			boolean isEmployee = checkIfIsEmployee(employeePersonaListForTeam, projectInsightDTO.getEmployeeRole());

			List<ProjectQuestionDTO> projectQuestionList = getProjectQuestionDTOList(projectInsightDTO.getProjectId(),
					"Project", projectInsightDTO.getEmpId(), isEmployee, projectInsightDTO.getPerformanceTabName(),
					projectInsightDTO.getProjectId(), null);
			List<ProjectInsightMilestone> projectInsightMilestoneList = projectInsightMilestoneRepository
					.getByProjectId(projectInsightDTO.getProjectId());
			if ((projectQuestionList == null || projectQuestionList.isEmpty())
					&& (projectInsightMilestoneList == null || projectInsightMilestoneList.isEmpty())) {
				response.setServiceResponse("No Questions or Milestone Found for Project.");
				apiLogInfo.setApiResponse("No Questions or Milestone Found for Project.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				return response;
			}

			ProjectInsightDTO projectInsightDTODbObject = createProjectInsightMileStoneObject(
					projectInsightMilestoneList, projectInsightDTO.getEmpId(), isEmployee,
					projectInsightDTO.getPerformanceTabName(), projectInsightDTO.getProjectId());
			projectInsightDTODbObject.setQuestionList(projectQuestionList);
			projectInsightDTODbObject.setProjectId(projectInsightDTO.getProjectId());
			projectInsightDTODbObject.setProjectManagerId(project.getProjectManagerId());
			projectInsightDTODbObject.setProjectManagerName(getProjectManagerName(project.getProjectManagerId()));
			projectInsightDTODbObject.setProjectName(project.getProjectName());
			projectInsightDTODbObject.setIsFinalSubmitted(getIsFinalResponseSubmittedByUserForProject(
					projectInsightDTO.getEmpId(), projectInsightDTO.getProjectId()));
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
			if (projectManagerId == null) {
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

	private ProjectInsightMilestoneDTO mapMilestone(ProjectInsightMilestone mileStone, Long projectId) {
		try {
			ProjectInsightMilestoneDTO mileStoneProjObject = new ProjectInsightMilestoneDTO();
			List<ProjectInsightAssignees> assignToDbList = projectInsightAssigneesRepository
					.getByEntityIdAndEntityType(mileStone.getMilestoneId(), "Milestone");
			if (!assignToDbList.isEmpty()) {
				mileStoneProjObject.setAssignedToUserId(assignToDbList.stream()
						.map(ProjectInsightAssignees::getAssignedTo).collect(Collectors.toList()));
				mileStoneProjObject
						.setAssignedToUserNames(getAssignedToUserName(mileStone.getMilestoneId(), "Milestone"));
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
			List<ProjectInsightAssignees> assignModuleToDbList = projectInsightAssigneesRepository
					.getByEntityIdAndEntityType(module.getModuleId(), "Module");
			if (!assignModuleToDbList.isEmpty()) {
				modDto.setAssignedToUserId(assignModuleToDbList.stream().map(ProjectInsightAssignees::getAssignedTo)
						.collect(Collectors.toList()));
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
			List<ProjectQuestionDTO> moduleQuestionList = getQuestion(module.getModuleId(), "Module");
			modDto.setQuestionList(moduleQuestionList);

			return modDto;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private List<SubModuleDTO> getSubModuleQuestionList(List<ProjectInsightSubModule> subModuleList,
			String subModuleType) {
		List<SubModuleDTO> subModuleResponseList = new ArrayList<>();
		try {
			if (!subModuleList.isEmpty()) {
				subModuleList.forEach((submodule) -> {
					SubModuleDTO submodDto = new SubModuleDTO();
					List<ProjectInsightAssignees> assignSubModuleToDbList = projectInsightAssigneesRepository
							.getByEntityIdAndEntityType(submodule.getSubmoduleId(), subModuleType);
					if (!assignSubModuleToDbList.isEmpty()) {
						submodDto.setAssignedToUserId(assignSubModuleToDbList.stream()
								.map(ProjectInsightAssignees::getAssignedTo).collect(Collectors.toList()));
						submodDto.setAssignedToUserNames(
								getAssignedToUserName(submodule.getSubmoduleId(), subModuleType));
					}
					submodDto.setCreatedBy(submodule.getCreatedBy());
					submodDto.setCreatedOn(submodule.getCreatedOn().toString());
					submodDto.setDescription(submodule.getDescription());
					submodDto.setModuleId(submodule.getModuleId());
					submodDto.setRedmineId(submodule.getRedmineId());
					submodDto.setSubModule(submodule.getSubmodule());
					submodDto.setSubModuleId(submodule.getSubmoduleId());
					submodDto.setUpdatedBy(submodule.getUpdatedBy());
					submodDto.setQuestionList(getQuestion(submodule.getSubmoduleId(), subModuleType));
					List<ProjectInsightSubModule> subSubModuleList = projectInsightSubModuleRepository
							.getByModuleIdAndSubModuleType(submodule.getSubmoduleId(), "Sub-SubModule");
					if (subSubModuleList != null && !subSubModuleList.isEmpty()) {
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

			Project project = projectRepository
					.findByProjectId(Integer.parseInt(projectInsightDTO.getProjectId().toString()));
			if (project == null) {
				throw new RuntimeException("Project Not Found!!");
			}

			combinedText.append(project.getProjectName()).append(" ").append(projectInsightDTO.getProjectManagerName())
					.append(" ");

			// Project Assign to ProjectManager
			List<Long> assignedToProjectManager = new ArrayList<>();
			assignedToProjectManager.add(project.getProjectManagerId());
			projectInsightDTO.setAssignedToUserId(assignedToProjectManager);
			saveAssignedTo(projectInsightDTO.getAssignedToUserId(), projectInsightDTO.getProjectId(), "Project", false);

			if (projectInsightDTO.getDeletedProjectInsightEntityList() != null
					&& !projectInsightDTO.getDeletedProjectInsightEntityList().isEmpty()) {
				deleteProjectInsightEntities(projectInsightDTO.getDeletedProjectInsightEntityList());
			}

			// Add/Update Project question
			if (!projectInsightDTO.getQuestionList().isEmpty()) {
				addUpdateQuestion(projectInsightDTO.getQuestionList(), projectInsightDTO.getProjectId(), "Project",
						combinedText);
			}

			if (projectInsightDTO.getProjectInsightMilestoneList() != null
					&& !projectInsightDTO.getProjectInsightMilestoneList().isEmpty()) {
				for (ProjectInsightMilestoneDTO projectInsightMilestoneDTO : projectInsightDTO
						.getProjectInsightMilestoneList()) {
					ProjectInsightMilestone projectInsightMilestone = saveOrUpdateProjectMilestone(
							projectInsightMilestoneDTO, projectInsightDTO.getProjectId(), combinedText,
							projectInsightDTO.getUpdatedBy(), projectInsightDTO.getCreatedBy());
					if (projectInsightMilestone == null
							|| (projectInsightMilestone != null && projectInsightMilestone.getMilestoneId() == null)) {
						apiLogInfo.setApiResponse("No Project Insight Milestone Found.");
						response.setServiceResponse("No Project Insight Milestone Found.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						return response;
					}
					combinedText.append(projectInsightMilestoneDTO.getMilestone()).append(" ")
							.append(projectInsightMilestoneDTO.getDescription());
					Long milestoneId = projectInsightMilestone.getMilestoneId();

					// Milestone Assign to
					saveAssignedTo(projectInsightMilestoneDTO.getAssignedToUserId(), milestoneId, "Milestone", true);

					// Add/Update Milestone question
					if (projectInsightMilestoneDTO.getQuestionList() != null
							&& !projectInsightMilestoneDTO.getQuestionList().isEmpty()) {
						addUpdateQuestion(projectInsightMilestoneDTO.getQuestionList(), milestoneId, "Milestone",
								combinedText);
					}

					if (projectInsightMilestoneDTO.getModuleList() != null
							&& !projectInsightMilestoneDTO.getModuleList().isEmpty()) {
						for (ModuleDTO moduleDTO : projectInsightMilestoneDTO.getModuleList()) {
							ProjectInsightModule projectInsightModule = saveOrUpdateModule(moduleDTO, milestoneId,
									combinedText, projectInsightDTO.getCreatedBy(), projectInsightDTO.getUpdatedBy());
							if (projectInsightModule.getModuleId() != null) {
								Long moduleId = projectInsightModule.getModuleId();

								// Module Assign to
								saveAssignedTo(moduleDTO.getAssignedToUserId(), moduleId, "Module", true);

								// Add/Update Module question
								if (moduleDTO.getQuestionList() != null && !moduleDTO.getQuestionList().isEmpty()) {
									addUpdateQuestion(moduleDTO.getQuestionList(), moduleId, "Module", combinedText);
								}

								if (!moduleDTO.getSubModuleList().isEmpty()) {
									saveOrUpdateSubModuleList(moduleDTO.getSubModuleList(), moduleId, "SubModule",
											combinedText, projectInsightDTO.getCreatedBy(),
											projectInsightDTO.getUpdatedBy());
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
					deleteProjectInsightSubModuleAndItsChild(projectInsightEntityDTO.getEntityId(),
							projectInsightEntityDTO.getEntityType());
				} else if (projectInsightEntityDTO.getEntityType() != null
						&& projectInsightEntityDTO.getEntityType().equals("Question")) {
					deleteProjectInsightQuestion(projectInsightEntityDTO.getEntityId(), "Question");
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void deleteResponseById(Long questionId) {
		try {
			List<ProjectInsightResponse> projectInsightResponseList = projectInsightResponseRepository
					.findAllByQuestionMasterId(questionId);
			if (projectInsightResponseList != null && !projectInsightResponseList.isEmpty()) {
				List<Long> projectInsightResponseIdList = projectInsightResponseList.stream()
						.map(ProjectInsightResponse::getProjectInsightResponseId).collect(Collectors.toList());
				List<TagMaster> dbTagMasterResponse = tagMasterRepository
						.findByEntityIdInAndEntityTypeAndType(projectInsightResponseIdList, "Response", "user");
				tagMasterRepository.deleteAll(dbTagMasterResponse);
			}
			projectInsightResponseRepository.deleteAllProjectInsightResponseByQuestionMasterId(questionId);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void deleteProjectInsightQuestion(Long questionId, String entityType) {
		try {
			entityType = entityType.equals(null) ? "Question" : entityType;
			deleteResponseById(questionId);
			projectInsightAssigneesRepository.deleteAllProjectInsightAssigneesByEntityIdAndEntityType(questionId,
					entityType);
			questionMasterRepository.deleteAllQuestionsByEntityIdAndEntityType(questionId);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void deleteProjectInsightSubModuleAndItsChild(Long subModuleId, String subModuleType) {
		try {
			List<QuestionMaster> questionList = questionMasterRepository.findByEntityIdAndEntityType(subModuleId,
					subModuleType);
			if (questionList != null && !questionList.isEmpty()) {
				for (QuestionMaster questionMaster : questionList) {
					deleteProjectInsightQuestion(questionMaster.getQuestionMasterId(), subModuleType);
				}
			}

			List<ProjectInsightSubModule> projectInsightSubSubModuleList = projectInsightSubModuleRepository
					.getByModuleIdAndSubModuleType(subModuleId, subModuleType);
			if (projectInsightSubSubModuleList != null && !projectInsightSubSubModuleList.isEmpty()) {
				for (ProjectInsightSubModule projectInsightSubModule : projectInsightSubSubModuleList) {
					deleteProjectInsightSubModuleAndItsChild(projectInsightSubModule.getSubmoduleId(), subModuleType);
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
			List<QuestionMaster> questionList = questionMasterRepository.findByEntityIdAndEntityType(entityId,
					"Module");
			if (questionList != null && !questionList.isEmpty()) {
				for (QuestionMaster questionMaster : questionList) {
					deleteProjectInsightQuestion(questionMaster.getQuestionMasterId(), "Module");
				}
			}

			List<ProjectInsightSubModule> projectInsightSubModuleList = projectInsightSubModuleRepository
					.findByModuleId(entityId);
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
			List<QuestionMaster> questionList = questionMasterRepository.findByEntityIdAndEntityType(entityId,
					"Milestone");
			if (questionList != null && !questionList.isEmpty()) {
				for (QuestionMaster questionMaster : questionList) {
					deleteProjectInsightQuestion(questionMaster.getQuestionMasterId(), "Milestone");
				}
			}
			List<ProjectInsightModule> projectInsightModuleList = projectInsightModuleRepository
					.findByMilestoneId(entityId);
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

	private void saveOrUpdateSubModuleList(List<SubModuleDTO> subModuleList, Long moduleId, Long updatedBy,
			String subModuleType, StringBuilder combinedText) {
		try {
			if (subModuleList != null && !subModuleList.isEmpty()) {
				for (SubModuleDTO subModuleDTO : subModuleList) {

					combinedText.append(subModuleDTO.getDescription()).append(" ")
							.append(subModuleDTO.getSubModule());

					if (subModuleDTO.getSubModuleId() != null) {
						ProjectInsightSubModule submoduleDbResponse = projectInsightSubModuleRepository
								.findById(subModuleDTO.getSubModuleId()).orElse(null);
						if (submoduleDbResponse != null) {
							submoduleDbResponse.setModuleId(moduleId);
							submoduleDbResponse.setDescription(subModuleDTO.getDescription());
							submoduleDbResponse.setRedmineId(subModuleDTO.getRedmineId());
							submoduleDbResponse.setSubmodule(subModuleDTO.getSubModule());
							submoduleDbResponse.setUpdatedBy(updatedBy);
							submoduleDbResponse.setSubModuleType(subModuleType);
							projectInsightSubModuleRepository.save(submoduleDbResponse);
							saveAssignedTo(subModuleDTO.getAssignedToUserId(), subModuleDTO.getSubModuleId(),
									subModuleType, true);

							// Add SubModule question
							if (subModuleDTO.getQuestionList() != null && !subModuleDTO.getQuestionList().isEmpty()) {
								addUpdateQuestion(subModuleDTO.getQuestionList(), subModuleDTO.getSubModuleId(),
										subModuleType, combinedText);
							}

							if (subModuleDTO.getSubSubModuleList() != null
									&& !subModuleDTO.getSubSubModuleList().isEmpty()) {
								saveOrUpdateSubModuleList(subModuleDTO.getSubSubModuleList(),
										submoduleDbResponse.getSubmoduleId(), updatedBy, "Sub-SubModule", combinedText);
							}
						} else {
							ProjectInsightSubModule newProjectInsightSubModule = new ProjectInsightSubModule();
							newProjectInsightSubModule.setCreatedBy(subModuleDTO.getCreatedBy());
							newProjectInsightSubModule.setModuleId(moduleId);
							newProjectInsightSubModule.setDescription(subModuleDTO.getDescription());
							newProjectInsightSubModule.setRedmineId(subModuleDTO.getRedmineId());
							newProjectInsightSubModule.setSubmodule(subModuleDTO.getSubModule());
							newProjectInsightSubModule.setSubModuleType(subModuleType);
							ProjectInsightSubModule projectInsightSubModule = projectInsightSubModuleRepository
									.save(newProjectInsightSubModule);
							saveAssignedTo(subModuleDTO.getAssignedToUserId(), projectInsightSubModule.getSubmoduleId(),
									subModuleType, true);

							// Add SubModule question
							if (subModuleDTO.getQuestionList() != null && !subModuleDTO.getQuestionList().isEmpty()) {
								addUpdateQuestion(subModuleDTO.getQuestionList(),
										projectInsightSubModule.getSubmoduleId(), subModuleType, combinedText);
							}

							if (subModuleDTO.getSubSubModuleList() != null
									&& !subModuleDTO.getSubSubModuleList().isEmpty()) {
								saveOrUpdateSubModuleList(subModuleDTO.getSubSubModuleList(),
										projectInsightSubModule.getSubmoduleId(), updatedBy, "Sub-SubModule",
										combinedText);
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
						ProjectInsightSubModule projectInsightSubModule = projectInsightSubModuleRepository
								.save(newProjectInsightSubModule);

						saveAssignedTo(subModuleDTO.getAssignedToUserId(), projectInsightSubModule.getSubmoduleId(),
								subModuleType, true);

						// Add SubModule question
						if (subModuleDTO.getQuestionList() != null && !subModuleDTO.getQuestionList().isEmpty()) {
							addUpdateQuestion(subModuleDTO.getQuestionList(), projectInsightSubModule.getSubmoduleId(),
									subModuleType, combinedText);
						}
						if (subModuleDTO.getSubSubModuleList() != null
								&& !subModuleDTO.getSubSubModuleList().isEmpty()) {
							saveOrUpdateSubModuleList(subModuleDTO.getSubSubModuleList(),
									projectInsightSubModule.getSubmoduleId(), updatedBy, "Sub-SubModule", combinedText);
						}
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private ProjectInsightModule saveOrUpdateModule(ModuleDTO moduleDTO, Long milestoneId, StringBuilder combinedText,
			Long createdBy, Long updatedBy) {
		try {
			ProjectInsightModule projectInsightModule = null;
			if (moduleDTO.getModuleId() == null
					|| (moduleDTO.getActionType() != null && moduleDTO.getActionType().equalsIgnoreCase("Add"))) {
				projectInsightModule = saveProjectModule(projectInsightModule, moduleDTO, milestoneId, createdBy,
						updatedBy);
			} else if (moduleDTO.getModuleId() != null) {
				ProjectInsightModule moduleDbObject = projectInsightModuleRepository
						.getByModuleId(moduleDTO.getModuleId());
				if (moduleDbObject != null) {
					projectInsightModule = saveProjectModule(moduleDbObject, moduleDTO, milestoneId, createdBy,
							updatedBy);
				} else {
					projectInsightModule = saveProjectModule(projectInsightModule, moduleDTO, milestoneId, createdBy,
							updatedBy);
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

	private ProjectInsightModule saveProjectModule(ProjectInsightModule projectInsightModule, ModuleDTO moduleDTO,
			Long milestoneId, Long createdBy, Long updatedBy) {
		try {
			if (projectInsightModule == null) {
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

	private void saveOrUpdateSubModuleList(List<SubModuleDTO> subModuleList, Long moduleId, String subModuleType,
			StringBuilder combinedText, Long createdBy, Long updatedBy) {
		try {
			if (subModuleList != null && !subModuleList.isEmpty()) {
				for (SubModuleDTO subModuleDTO : subModuleList) {
					combinedText.append(subModuleDTO.getDescription()).append(" ").append(subModuleDTO.getSubModule());
					ProjectInsightSubModule projectInsightSubModule = null;
					if (subModuleDTO.getSubModuleId() == null || (subModuleDTO.getActionType() != null
							&& subModuleDTO.getActionType().equalsIgnoreCase("Add"))) {
						projectInsightSubModule = saveSubModule(projectInsightSubModule, subModuleDTO, moduleId,
								subModuleType, combinedText, createdBy, updatedBy);
					} else if (subModuleDTO.getSubModuleId() != null) {
						ProjectInsightSubModule submoduleDbResponse = projectInsightSubModuleRepository
								.findById(subModuleDTO.getSubModuleId()).orElse(null);
						if (submoduleDbResponse != null) {
							projectInsightSubModule = saveSubModule(submoduleDbResponse, subModuleDTO, moduleId,
									subModuleType, combinedText, createdBy, updatedBy);
						} else {
							projectInsightSubModule = saveSubModule(projectInsightSubModule, subModuleDTO, moduleId,
									subModuleType, combinedText, createdBy, updatedBy);
						}
					}
					if (projectInsightSubModule != null) {
						projectInsightSubModule = projectInsightSubModuleRepository.save(projectInsightSubModule);
						if (projectInsightSubModule.getSubmoduleId() != null) {
							Long subModuleId = projectInsightSubModule.getSubmoduleId();
							saveAssignedTo(subModuleDTO.getAssignedToUserId(), subModuleId, subModuleType, true);

							// Add SubModule question
							if (subModuleDTO.getQuestionList() != null && !subModuleDTO.getQuestionList().isEmpty()) {
								addUpdateQuestion(subModuleDTO.getQuestionList(), subModuleId, subModuleType,
										combinedText);
							}

							if (subModuleDTO.getSubSubModuleList() != null
									&& !subModuleDTO.getSubSubModuleList().isEmpty()) {
								saveOrUpdateSubModuleList(subModuleDTO.getSubSubModuleList(), subModuleId,
										"Sub-SubModule", combinedText, createdBy, updatedBy);
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

	private ProjectInsightSubModule saveSubModule(ProjectInsightSubModule projectInsightSubModule,
			SubModuleDTO subModuleDTO, Long moduleId, String subModuleType, StringBuilder combinedText, Long createdBy,
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

	// ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
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

			Project project = projectRepository
					.findByProjectId(Integer.parseInt(projectInsightDTO.getProjectId().toString()));
			if (project == null) {
				throw new RuntimeException("Project Not Found!!");
			}

			List<Object[]> employeePersonaListForTeam = employeeTeamMapRepository
					.getEmployeePersonaForProject(projectInsightDTO.getEmpId(), projectInsightDTO.getProjectId());
			boolean isEmployee = checkIfIsEmployee(employeePersonaListForTeam, projectInsightDTO.getEmployeeRole());

			List<ProjectInsightMilestone> projectInsightMilestoneList = projectInsightMilestoneRepository
					.getByProjectId(projectInsightDTO.getProjectId());
			if (projectInsightMilestoneList != null && !projectInsightMilestoneList.isEmpty()) {
				ProjectInsightDTO projectInsightDTODbObject = createProjectInsightMileStoneObject(
						projectInsightMilestoneList, projectInsightDTO.getEmpId(), isEmployee,
						projectInsightDTO.getPerformanceTabName(), projectInsightDTO.getProjectId());
				projectInsightDTODbObject.setQuestionList(getProjectQuestionDTOList(projectInsightDTO.getProjectId(),
						"Project", projectInsightDTO.getEmpId(), isEmployee, projectInsightDTO.getPerformanceTabName(),
						projectInsightDTO.getProjectId(), null));
				projectInsightDTODbObject.setTaggedToUserNames(getAllTaggedUserName(projectInsightDTO.getProjectId(),
						"Project", projectInsightDTO.getEmpId()));
				projectInsightDTODbObject.setTaggedToUserId(
						getAllTaggedUserId(projectInsightDTO.getProjectId(), "Project", projectInsightDTO.getEmpId()));
				projectInsightDTODbObject.setProjectId(projectInsightDTO.getProjectId());
				projectInsightDTODbObject.setProjectManagerId(project.getProjectManagerId());
				projectInsightDTODbObject.setProjectManagerName(getProjectManagerName(project.getProjectManagerId()));
				projectInsightDTODbObject.setProjectName(project.getProjectName());
				projectInsightDTODbObject.setIsFinalSubmitted(getIsFinalResponseSubmittedByUserForProject(
						projectInsightDTO.getEmpId(), projectInsightDTO.getProjectId()));
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
			ProjectInsightResponseMetadata projectInsightResponseMetadata = projectInsightResponseMetadataRepository
					.findByResponseByAndProjectId(empId, projectId);
			return projectInsightResponseMetadata != null
					? projectInsightResponseMetadata.getIsFinalSubmitted() != null
							? projectInsightResponseMetadata.getIsFinalSubmitted()
							: "N"
					: "N";
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private ProjectInsightDTO createProjectInsightMileStoneObject(
			List<ProjectInsightMilestone> projectInsightMilestoneList, Long employeeId, boolean isEmployee,
			String performanceTabName, Long projectId) {
		ProjectInsightDTO projectInsightDTO = new ProjectInsightDTO();
		try {
			if (projectInsightMilestoneList != null && !projectInsightMilestoneList.isEmpty()) {
				List<ProjectInsightMilestoneDTO> projectInsightMilestoneDTOList = new ArrayList<>();
				for (ProjectInsightMilestone projectInsightMilestone : projectInsightMilestoneList) {
					ProjectInsightMilestoneDTO projectInsightMilestoneDTO = mapProjectMilestoneToDTO(
							projectInsightMilestone, employeeId, isEmployee, performanceTabName, projectId);
					if ((projectInsightMilestoneDTO.getModuleList() != null
							&& !projectInsightMilestoneDTO.getModuleList().isEmpty())
							|| (projectInsightMilestoneDTO.getQuestionList() != null
									&& !projectInsightMilestoneDTO.getQuestionList().isEmpty())) {
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

	private ProjectInsightMilestoneDTO mapProjectMilestoneToDTO(ProjectInsightMilestone projectInsightMilestone,
			Long employeeId, boolean isEmployee, String performanceTabName, Long projectId) {
		try {
			ProjectInsightMilestoneDTO projectInsightMilestoneDTO = new ProjectInsightMilestoneDTO();
			projectInsightMilestoneDTO.setMilestoneId(projectInsightMilestone.getMilestoneId());
			projectInsightMilestoneDTO.setMilestone(projectInsightMilestone.getMilestone());
			projectInsightMilestoneDTO.setDescription(projectInsightMilestone.getDescription());
			projectInsightMilestoneDTO.setDeptId(projectInsightMilestone.getDeptId());
			projectInsightMilestoneDTO
					.setAssignedToUserId(getAssignedToUserId(projectInsightMilestone.getMilestoneId(), "Milestone"));
			projectInsightMilestoneDTO.setAssignedToUserNames(
					getAssignedToUserName(projectInsightMilestone.getMilestoneId(), "Milestone"));
			projectInsightMilestoneDTO.setTaggedToUserNames(
					getAllTaggedUserName(projectInsightMilestone.getMilestoneId(), "Milestone", employeeId));
			projectInsightMilestoneDTO.setTaggedToUserId(
					getAllTaggedUserId(projectInsightMilestone.getMilestoneId(), "Milestone", employeeId));
			projectInsightMilestoneDTO
					.setQuestionList(getProjectQuestionDTOList(projectInsightMilestone.getMilestoneId(), "Milestone",
							employeeId, isEmployee, performanceTabName, projectId, null));
			projectInsightMilestoneDTO.setModuleList(getProjectInsightModuleDTOList(
					projectInsightMilestone.getMilestoneId(), employeeId, isEmployee, performanceTabName, projectId));
			return projectInsightMilestoneDTO;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private List<ModuleDTO> getProjectInsightModuleDTOList(Long milestoneId, Long employeeId, boolean isEmployee,
			String performanceTabName, Long projectId) {
		List<ModuleDTO> projectInsightModuleDTOList = new ArrayList<>();
		try {
			List<ProjectInsightModule> projectInsightModuleList = projectInsightModuleRepository
					.findByMilestoneId(milestoneId);
			if (!projectInsightModuleList.isEmpty()) {
				for (ProjectInsightModule projectInsightModule : projectInsightModuleList) {
					ModuleDTO moduleDTO = new ModuleDTO();
					moduleDTO.setModuleId(projectInsightModule.getModuleId());
					moduleDTO.setMilestoneId(projectInsightModule.getMilestoneId());
					moduleDTO.setModule(projectInsightModule.getModule());
					moduleDTO.setDescription(projectInsightModule.getDescription());
					moduleDTO.setRedmineId(projectInsightModule.getRedmineId());
					moduleDTO.setAssignedToUserId(getAssignedToUserId(projectInsightModule.getModuleId(), "Module"));
					moduleDTO.setAssignedToUserNames(
							getAssignedToUserName(projectInsightModule.getModuleId(), "Module"));
					moduleDTO.setTaggedToUserNames(
							getAllTaggedUserName(projectInsightModule.getModuleId(), "Module", employeeId));
					moduleDTO.setTaggedToUserId(
							getAllTaggedUserId(projectInsightModule.getModuleId(), "Module", employeeId));
					moduleDTO.setQuestionList(getProjectQuestionDTOList(projectInsightModule.getModuleId(), "Module",
							employeeId, isEmployee, performanceTabName, projectId, null));
					moduleDTO.setSubModuleList(getProjectInsightSubModuleDTOList(projectInsightModule.getModuleId(),
							employeeId, isEmployee, performanceTabName, projectId, "SubModule"));
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

	private List<SubModuleDTO> getProjectInsightSubModuleDTOList(Long moduleId, Long employeeId, boolean isEmployee,
			String performanceTabName, Long projectId, String subModuleType) {
		List<SubModuleDTO> projectInsightSubModuleDTOList = new ArrayList<>();
		try {
			List<ProjectInsightSubModule> projectInsightSubModuleList = projectInsightSubModuleRepository
					.getByModuleIdAndSubModuleType(moduleId, subModuleType);
			if (!projectInsightSubModuleList.isEmpty()) {
				for (ProjectInsightSubModule projectInsightSubModule : projectInsightSubModuleList) {
					SubModuleDTO subModuleDTO = new SubModuleDTO();
					subModuleDTO.setSubModuleId(projectInsightSubModule.getSubmoduleId());
					subModuleDTO.setSubModule(projectInsightSubModule.getSubmodule());
					subModuleDTO.setModuleId(projectInsightSubModule.getModuleId());
					subModuleDTO.setDescription(projectInsightSubModule.getDescription());
					subModuleDTO.setRedmineId(projectInsightSubModule.getRedmineId());
					subModuleDTO.setAssignedToUserId(
							getAssignedToUserId(projectInsightSubModule.getSubmoduleId(), subModuleType));
					subModuleDTO.setAssignedToUserNames(
							getAssignedToUserName(projectInsightSubModule.getSubmoduleId(), subModuleType));
					subModuleDTO.setTaggedToUserNames(
							getAllTaggedUserName(projectInsightSubModule.getSubmoduleId(), subModuleType, employeeId));
					subModuleDTO.setTaggedToUserId(
							getAllTaggedUserId(projectInsightSubModule.getSubmoduleId(), subModuleType, employeeId));
					subModuleDTO.setQuestionList(getProjectQuestionDTOList(projectInsightSubModule.getSubmoduleId(),
							"SubModule", employeeId, isEmployee, performanceTabName, projectId, subModuleType));

					List<ProjectInsightSubModule> projectInsightSubSubModuleList = projectInsightSubModuleRepository
							.getByModuleIdAndSubModuleType(projectInsightSubModule.getSubmoduleId(), "Sub-SubModule");
					if (projectInsightSubSubModuleList != null && !projectInsightSubSubModuleList.isEmpty()) {
						subModuleDTO.setSubSubModuleList(
								getProjectInsightSubModuleDTOList(projectInsightSubModule.getSubmoduleId(), employeeId,
										isEmployee, performanceTabName, projectId, "Sub-SubModule"));
					}

					if ((subModuleDTO.getQuestionList() != null && !subModuleDTO.getQuestionList().isEmpty())
							|| (projectInsightSubSubModuleList != null && !projectInsightSubSubModuleList.isEmpty())) {
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

	public List<ProjectQuestionDTO> getProjectQuestionDTOList(Long entityId, String entityType, Long employeeId,
			boolean isEmployee, String performanceTabName, Long projectId, String subModuleType) {
		List<ProjectQuestionDTO> projectInsightQuestionList = new ArrayList<>();
		try {
			List<QuestionMaster> projectInsightQuestionMasterList = new ArrayList<>();
			if (isEmployee) {
				projectInsightQuestionMasterList = questionMasterRepository
						.findByEntityIdAndEntityTypeAndAssignedTo(entityId, entityType, employeeId);
			} else {
				entityType = subModuleType != null ? subModuleType : entityType;
				projectInsightQuestionMasterList = questionMasterRepository.findByEntityIdAndEntityType(entityId,
						entityType);
			}
			if (!projectInsightQuestionMasterList.isEmpty()) {
				for (QuestionMaster questionMaster : projectInsightQuestionMasterList) {
					ProjectQuestionDTO projectQuestionDTO = mapQuestionMasterToProjectQuestionDTO(questionMaster);
					List<ProjectResponseDTO> projectResponseDTOList = new ArrayList<>();

					ProjectInsightResponseMetadata projectInsightResponseMetadata = projectInsightResponseMetadataRepository
							.findProjectInsightResponseMetadataByResponseByAndProjectId(employeeId, projectId);
					if (projectInsightResponseMetadata != null) {
						List<ProjectInsightResponse> projectInsightResponseList = getProjectInsightResponseListAsPerEmployeeAndQuestion(
								employeeId, performanceTabName, projectId, questionMaster.getQuestionMasterId());
						List<ProjectInsightResponse> projectInsightResponseList2 = getAllTaggedQuestionsResponse(
								entityId, entityType, employeeId, questionMaster.getQuestionMasterId());
						if (projectInsightResponseList2 != null && !projectInsightResponseList2.isEmpty()) {
							for (ProjectInsightResponse projectInsightResponse2 : projectInsightResponseList2) {
								if (questionMaster.getQuestionMasterId()
										.equals(projectInsightResponse2.getQuestionMasterId())) {
									projectQuestionDTO.setTagged(true);
								}
							}
						}
						projectInsightResponseList.addAll(projectInsightResponseList2);

						if (projectInsightResponseList != null && !projectInsightResponseList.isEmpty()) {
							for (ProjectInsightResponse projectInsightResponse : projectInsightResponseList) {
								ProjectResponseDTO projectResponseDTO = mapPojectInsightResponseToProjectInsightResponseDTO(
										projectInsightResponse, questionMaster.getOptions());
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

	private List<ProjectInsightResponse> getAllTaggedQuestionsResponse(Long entityId, String entityType,
			Long employeeId, Long questionMasterId) {
		List<ProjectInsightResponse> projectInsightTaggedResponseList = new ArrayList<>();
		try {
			List<ProjectInsightAssignees> projectInsightAssigneesList = projectInsightAssigneesRepository
					.getProjectInsightAssigneesByEntityIdAndEntityTypeAndHelpTaggedBy(entityId, entityType, employeeId);
			if (projectInsightAssigneesList != null && !projectInsightAssigneesList.isEmpty()) {
				for (ProjectInsightAssignees projectInsightAssignees : projectInsightAssigneesList) {
					ProjectInsightResponse projectInsightResponse = projectInsightResponseRepository
							.findByEmpIdAndQuestionMasterId(projectInsightAssignees.getAssigneeId(), questionMasterId);
					if (projectInsightResponse != null) {
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

	private List<ProjectInsightResponse> getProjectInsightResponseListAsPerEmployeeAndQuestion(Long employeeId,
			String performanceTabName, Long projectId, Long questionMasterId) {
		List<ProjectInsightResponse> projectInsightResponseList = new ArrayList<>();
		try {
			if (!performanceTabName.equals("Teams Dashboard")) {
				return projectInsightResponseRepository.findByQuestionMasterIdAndEmpId(questionMasterId, employeeId);
			}
			List<EmployeeTeamMap> employeeTeamList = employeeTeamMapRepository.findByTeamIdAndIsActive(employeeId, 1l,
					Integer.parseInt(projectId.toString()));

			if (employeeTeamList == null || employeeTeamList.isEmpty()) {
				return projectInsightResponseRepository.findAllByQuestionMasterId(questionMasterId);
			}
			Map<Long, String> employeeTeamMapObj = getEmpIdToHighestRoleMap(employeeTeamList);
			String employeeRole = employeeTeamMapObj.getOrDefault(employeeId, null);

			if (employeeRole != null && (employeeRole.equals("HOD") || employeeRole.equals("SUPERADMIN")
					|| employeeRole.equals("RMG") || employeeRole.equals("HR"))) {
				projectInsightResponseList = projectInsightResponseRepository
						.findAllByQuestionMasterId(questionMasterId);
			} else if (employeeRole != null && !employeeRole.equals("HOD") && !employeeRole.equals("SUPERADMIN")
					&& !employeeRole.equals("RMG") && !employeeRole.equals("HR")) {
				List<Long> seniorEmployeeIdList = getSeniorEmployeeList(employeeTeamMapObj, employeeRole);
				if (seniorEmployeeIdList.isEmpty()) {
					seniorEmployeeIdList.add(-1l);
				}
				projectInsightResponseList = projectInsightResponseRepository
						.findAllByQuestionMasterIdAndEmpIdListNotIn(questionMasterId, seniorEmployeeIdList);
			} else {
				projectInsightResponseList = projectInsightResponseRepository
						.findByQuestionMasterIdAndEmpId(questionMasterId, employeeId);
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

	private ProjectResponseDTO mapPojectInsightResponseToProjectInsightResponseDTO(
			ProjectInsightResponse projectInsightResponse, String options) {
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
			projectResponseDTO.setProjectInsightResponsePointList(
					getResponsePointDTOList(projectInsightResponse.getProjectInsightResponseId()));
			projectResponseDTO.setApprovedForKnowledgeHub(projectInsightResponse.getIsApprovedForKnowledgeHub() != null
					&& projectInsightResponse.getIsApprovedForKnowledgeHub().equals("Y") ? true : false);
			return projectResponseDTO;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private List<String> getAllTagsForResponse(Long projectInsightResponseId) {
		List<String> tagsList = new ArrayList<String>();
		try {
			List<TagMaster> dbTagMasterResponse = tagMasterRepository
					.findByEntityIdAndEntityTypeAndType(projectInsightResponseId, "Response", "user");
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
			List<ProjectInsightResponsePoints> projectInsightResponsePointList = projectInsightResponsePointsRepository
					.findAllProjectInsightResponsePointsByResponseId(projectInsightResponseId);
			if (projectInsightResponsePointList != null && !projectInsightResponsePointList.isEmpty()) {
				for (ProjectInsightResponsePoints projectInsightResponsePoints : projectInsightResponsePointList) {
					ProjectInsightResponsePointsDTO projectInsightResponsePointsDTO = new ProjectInsightResponsePointsDTO();
					projectInsightResponsePointsDTO.setProjectInsightResponsePointId(
							projectInsightResponsePoints.getProjectInsightResponsePointId());
					projectInsightResponsePointsDTO.setResponseId(projectInsightResponseId);
					projectInsightResponsePointsDTO.setPoints(projectInsightResponsePoints.getPoints());
					projectInsightResponsePointsDTO.setPointsBy(projectInsightResponsePoints.getPointsBy());
					projectInsightResponsePointsDTO
							.setPointsByName(getProjectManagerName(projectInsightResponsePoints.getPointsBy()));
					projectInsightResponsePointsDTO
							.setIsPointsDrafted(projectInsightResponsePoints.getIsPointsDrafted());
					projectInsightResponsePointsDTO
							.setFinalSubmittedOn(projectInsightResponsePoints.getFinalSubmittedOn());
					projectInsightResponsePointDTOList.add(projectInsightResponsePointsDTO);
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

			Project project = projectRepository
					.findByProjectId(Integer.parseInt(projectInsightDTO.getProjectId().toString()));
			if (project == null) {
				throw new RuntimeException("Project Not Found!!");
			}

			combinedText.append(project.getProjectName()).append(" ");

			Long reviewerId = getReviewerIdForQuestion(projectInsightDTO.getProjectId(), projectInsightDTO.getEmpId());

			ProjectInsightResponseMetadata projectInsightResponseMetadata = saveProjectInsightResponseMetadata(
					projectInsightDTO.getIsFinalSubmitted(), projectInsightDTO.getResponseBy(),
					projectInsightDTO.getProjectId(), reviewerId);

			if (projectInsightDTO.getProjectInsightMilestoneList() != null
					&& !projectInsightDTO.getProjectInsightMilestoneList().isEmpty()) {
				saveProjectResponse(projectInsightDTO.getQuestionList(), projectInsightDTO.getResponseBy(), files,
						projectInsightResponseMetadata.getProjectInsightResponseMetadataId(),
						projectInsightDTO.getProjectId(), combinedText);
				saveProjectMileStoneResponse(projectInsightDTO.getProjectInsightMilestoneList(),
						projectInsightDTO.getResponseBy(), files,
						projectInsightResponseMetadata.getProjectInsightResponseMetadataId(), combinedText);
				saveTaggedForHelp(projectInsightDTO.getTaggedToUserId(), projectInsightDTO.getProjectId(), "Project",
						projectInsightDTO.getEmpId());

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

	private ProjectInsightResponseMetadata saveProjectInsightResponseMetadata(String isFinalSubmitted, Long responseBy,
			Long projectId, Long reviewerId) {
		ProjectInsightResponseMetadata projectInsightResponseMetadata = null;
		try {
			projectInsightResponseMetadata = projectInsightResponseMetadataRepository
					.findByResponseByAndProjectId(responseBy, projectId);
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

	private void saveProjectResponse(List<ProjectQuestionDTO> questionList, Long empId, List<MultipartFile> files,
			Long projectInsightResponseMetadataId, Long projectId, StringBuilder combinedText) {
		try {
			if (questionList != null && !questionList.isEmpty()) {
				saveProjectInsightResponse(questionList, files, projectInsightResponseMetadataId, empId, combinedText);
				for (ProjectQuestionDTO projectQuestionDTO : questionList) {
					saveTaggedForHelp(projectQuestionDTO.getTaggedForHelp(), projectQuestionDTO.getQuestionId(),
							"Question", empId);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private String saveProjectMileStoneResponse(List<ProjectInsightMilestoneDTO> projectInsightMilestoneDTOList,
			Long responseBy, List<MultipartFile> files, Long projectInsightResponseMetadata,
			StringBuilder combinedText) {
		String response = null;
		try {
			if (projectInsightMilestoneDTOList != null && !projectInsightMilestoneDTOList.isEmpty()) {
				for (ProjectInsightMilestoneDTO projectInsightQuestionDTO : projectInsightMilestoneDTOList) {
					saveProjectInsightResponse(projectInsightQuestionDTO.getQuestionList(), files,
							projectInsightResponseMetadata, responseBy, combinedText);
					saveProjectInsightModuleResponse(projectInsightQuestionDTO.getModuleList(), responseBy, files,
							projectInsightResponseMetadata, combinedText);
					saveTaggedForHelp(projectInsightQuestionDTO.getTaggedToUserId(),
							projectInsightQuestionDTO.getMilestoneId(), "Milestone", responseBy);
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

	private void saveProjectInsightModuleResponse(List<ModuleDTO> moduleList, Long responseBy,
			List<MultipartFile> files, Long projectInsightResponseMetadata, StringBuilder combinedText) {
		try {
			if (moduleList != null && !moduleList.isEmpty()) {
				for (ModuleDTO moduleDTO : moduleList) {
					saveProjectInsightResponse(moduleDTO.getQuestionList(), files, projectInsightResponseMetadata,
							responseBy, combinedText);
					saveProjectInsightSubModuleResponse(moduleDTO.getSubModuleList(), responseBy, files,
							projectInsightResponseMetadata, "SubModule", combinedText);
					saveTaggedForHelp(moduleDTO.getTaggedToUserId(), moduleDTO.getModuleId(), "Module", responseBy);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void saveProjectInsightSubModuleResponse(List<SubModuleDTO> subModuleList, Long responseBy,
			List<MultipartFile> files, Long projectInsightResponseMetadata, String subModuleType,
			StringBuilder combinedText) {
		try {
			if (subModuleList != null && !subModuleList.isEmpty()) {
				for (SubModuleDTO subModuleDTO : subModuleList) {
					saveProjectInsightResponse(subModuleDTO.getQuestionList(), files, projectInsightResponseMetadata,
							responseBy, combinedText);
					saveTaggedForHelp(subModuleDTO.getTaggedToUserId(), subModuleDTO.getSubModuleId(), subModuleType,
							responseBy);
					if (subModuleDTO.getSubSubModuleList() != null && !subModuleDTO.getSubSubModuleList().isEmpty()) {
						saveProjectInsightSubModuleResponse(subModuleDTO.getSubSubModuleList(), responseBy, files,
								projectInsightResponseMetadata, "Sub-SubModule", combinedText);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void saveProjectInsightResponse(List<ProjectQuestionDTO> projectQuestionList, List<MultipartFile> files,
			Long projectInsightResponseMetadataId, Long responseBy, StringBuilder combinedText) {
		try {
			if (projectQuestionList != null && !projectQuestionList.isEmpty()) {
				for (ProjectQuestionDTO projectQuestionDTO : projectQuestionList) {
					if (projectQuestionDTO.getQuestionId() != null
							&& projectQuestionDTO.getProjectResponseList() != null
							&& !projectQuestionDTO.getProjectResponseList().isEmpty()) {
						for (ProjectResponseDTO projectResponseDTO : projectQuestionDTO.getProjectResponseList()) {
							if (projectResponseDTO.getResponse() != null
									&& !projectResponseDTO.getResponse().equals("[]")) {
								saveOrUpdateAllProjectResponseAndUploadDocument(projectQuestionDTO, projectResponseDTO,
										files, projectInsightResponseMetadataId, combinedText, responseBy);
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

	private void saveTaggedForHelp(List<Long> taggedForHelp, Long entityId, String entityType, Long employeeId) {
		if (taggedForHelp != null && !taggedForHelp.isEmpty()) {
			for (Long taggedUserId : taggedForHelp) {
				ProjectInsightAssignees projectInsightAssignees = projectInsightAssigneesRepository
						.getProjectInsightAssigneesByEntityIdAndEntityTypeAndAssignedToHelpTaggedBy(entityId,
								entityType, taggedUserId, employeeId);
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

	private void saveOrUpdateAllProjectResponseAndUploadDocument(ProjectQuestionDTO projectQuestionDTO,
			ProjectResponseDTO projectResponseDTO, List<MultipartFile> files, Long projectInsightResponseMetadataId,
			StringBuilder combinedText, Long responseBy) {
		try {
			ProjectInsightResponse projectInsightResponse = projectInsightResponseRepository
					.findByEmpIdAndQuestionMasterId(responseBy, projectQuestionDTO.getQuestionId());
			if (projectInsightResponse != null) {
				List<TagMaster> dbTagMasterResponse = tagMasterRepository.findByEntityIdAndEntityTypeAndType(
						projectInsightResponse.getProjectInsightResponseId(), "Response", "user");
				tagMasterRepository.deleteAll(dbTagMasterResponse);
				projectInsightResponse = mapProjectResponseDTOToProjectResponseAndUploadFile(projectInsightResponse,
						projectResponseDTO, files, projectInsightResponseMetadataId, projectQuestionDTO.getQuestionId(),
						combinedText);
				if (files != null) {
				}
			} else {
				projectInsightResponse = new ProjectInsightResponse();
				projectInsightResponse = mapProjectResponseDTOToProjectResponseAndUploadFile(projectInsightResponse,
						projectResponseDTO, files, projectInsightResponseMetadataId, projectQuestionDTO.getQuestionId(),
						combinedText);
			}
			ProjectInsightResponse projectInsightResponse2 = projectInsightResponseRepository
					.save(projectInsightResponse);
			if (projectResponseDTO.getTags() != null && !projectResponseDTO.getTags().isEmpty()
					&& projectInsightResponse2 != null
					&& projectInsightResponse2.getProjectInsightResponseId() != null) {
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

	private ProjectInsightResponse mapProjectResponseDTOToProjectResponseAndUploadFile(
			ProjectInsightResponse projectInsightResponse, ProjectResponseDTO projectResponseDTO,
			List<MultipartFile> files, Long projectInsightResponseMetadataId, Long questionMasterId,
			StringBuilder combinedText) {
		try {
			combinedText.append(projectResponseDTO.getResponse()).append(" ")
					.append(projectResponseDTO.getDocumentFileName());
			projectInsightResponse.setProjectInsightResponseMetadataId(projectInsightResponseMetadataId);
			projectInsightResponse.setResponse(projectResponseDTO.getResponse());
			projectInsightResponse.setDocumentPath(projectResponseDTO.getDocumentPath());
			projectInsightResponse.setQuestionMasterId(questionMasterId);
			projectInsightResponse.setDocumentFileName(projectResponseDTO.getDocumentFileName());
			projectInsightResponse
					.setIsApprovedForKnowledgeHub(projectResponseDTO.isApprovedForKnowledgeHub() ? "Y" : "N");
			if (files == null) {
				return projectInsightResponse;
			}
			for (MultipartFile document : files) {
				if (document != null && document.getOriginalFilename() != null
						&& projectResponseDTO.getDocumentFileName() != null
						&& document.getOriginalFilename().equals(projectResponseDTO.getDocumentFileName())) {
					if (projectInsightResponse.getDocumentFileName() != null
							&& !projectInsightResponse.getDocumentFileName().trim().equals("")) {
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
				UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(dmsPortalFetchUrl)
						.queryParam("document_name", documentFileName);
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
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(dmsPortalFetchUrl)
					.queryParam("document_name", employeeDocumentDTO.getDocumentName());
			URI finalUri = builder.build().encode().toUri();
			ResponseEntity<byte[]> response = restTemplate.exchange(
					finalUri,
					HttpMethod.GET,
					entity,
					byte[].class);

			if (response.getBody() != null) {
				String contentType = response.getHeaders().getContentType() != null
						? response.getHeaders().getContentType().toString()
						: null;

				serviceResponse.setServiceResponse(ResponseEntity.ok()
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

	public String uploadProjectResponseDocument(MultipartFile document, Long entityIdOrQuestionMasterId,
			String entityType) {
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

				// Send document for OCR
				if (response.equalsIgnoreCase("Document uploaded successfully")) {
					Long entityId = null;
					String type = null;
					Long questionMasterId = null;
					Long projectId = null;

					if (entityType != null && entityType.equals("UserContribution")) {
						entityId = entityIdOrQuestionMasterId;
						type = entityType;
					} else {
						QuestionMaster questionMasterObject = questionMasterRepository
								.findByQuestionMasterId(entityIdOrQuestionMasterId);
						List<Object[]> projectInfoByQuestionMasterId = questionMasterRepository
								.getProjectInfoByQuestionMasterId(entityIdOrQuestionMasterId);

						if (questionMasterObject != null) {
							entityId = questionMasterObject.getEntityId();
							type = questionMasterObject.getEntityType();
							questionMasterId = entityIdOrQuestionMasterId;
						}
						if (!projectInfoByQuestionMasterId.isEmpty()) {
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
				objectList = projectInsightResponseRepository
						.getAllProjectInsightForReviewByUser(projectInsightDTO.getEmpId());
			} else {
				if (projectInsightDTO.getEmployeeRole().equals("Employee")) {
					objectList = projectInsightResponseRepository
							.getAllProjectInsightByUser(projectInsightDTO.getEmpId());
					List<Long> projectIds = objectList.stream().map(obj -> parseLong(obj[0]))
							.collect(Collectors.toList());
					if (projectIds == null || projectIds.isEmpty()) {
						projectIds.add(-1l);
					}
					List<Object[]> objectList2 = projectInsightMilestoneRepository
							.getAllProjectInsightByUserIdForEmployee(projectInsightDTO.getEmpId(), projectIds);
					if (objectList2 != null && !objectList2.isEmpty()) {
						objectList.addAll(objectList2);
					}
				} else {
					List<Long> projectIds = new ArrayList<Long>();
					objectList = projectInsightMilestoneRepository.getAllProjectInsight();
					if (objectList != null && !objectList.isEmpty()) {
						projectIds = objectList.stream().map(object -> parseLong(object[0]))
								.collect(Collectors.toList());
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
		return obj != null ? !obj.toString().equalsIgnoreCase("null") ? Long.parseLong(obj.toString()) : null : null;
	}

	private String parseString(Object obj) {
		return obj != null ? !obj.toString().equalsIgnoreCase("null") ? obj.toString() : null : null;
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
			return projectInsightAssigneesRepository.getAssignedToByEntityIdAndEntityType(entityId, entityType);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private String getAssignedToUserName(Long entityId, String entityType) {
		String assignedTo = null;
		try {
			List<String> usernameList = projectInsightAssigneesRepository.getUserNameByEntityIdAndEntityType(entityId,
					entityType);
			assignedTo = usernameList != null
					? usernameList.stream().map(String::valueOf).collect(Collectors.joining(", "))
					: "";
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return assignedTo;
	}

	private String getAllTaggedUserName(Long entityId, String entityType, Long taggedBy) {
		String assignedTo = null;
		try {
			List<String> usernameList = projectInsightAssigneesRepository
					.getUserNameByEntityIdAndEntityTypeAndTaggedBy(entityId, entityType, taggedBy);
			assignedTo = usernameList != null
					? usernameList.stream().map(String::valueOf).collect(Collectors.joining(", "))
					: "";
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return assignedTo;
	}

	private List<Long> getAllTaggedUserId(Long entityId, String entityType, Long taggedBy) {
		try {
			return projectInsightAssigneesRepository.getUserIdByEntityIdAndEntityTypeAndTaggedBy(entityId, entityType,
					taggedBy);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private Long getReviewerIdForQuestion(Long projectId, Long employeeId) {
		Long reviewerId = null;
		try {
			List<EmployeeTeamMap> employeeTeamList = employeeTeamMapRepository.findByTeamIdAndIsActive(employeeId, 1l,
					Integer.parseInt(projectId.toString()));
			if (employeeTeamList == null || employeeTeamList.isEmpty()) {
				return getEmployeesRMorManagerId(employeeId);
			}

			Map<Long, String> employeeTeamMapObj = getEmpIdToHighestRoleMap(employeeTeamList);
			String employeeRole = employeeTeamMapObj.getOrDefault(employeeId, null);
			if (employeeRole == null) {
				return getEmployeesRMorManagerId(employeeId);
			} else if (employeeRole.equals("SUPERADMIN") || employeeRole.equals("RMG") || employeeRole.equals("HR")
					|| employeeRole.equals("HOD")) {
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
				return employee.getReportingManagerId() != null ? employee.getReportingManagerId()
						: employee.getManagerId();
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/*
	 * ------------------------------ Search impl
	 * -----------------------------------------
	 */

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
			// List<FileStorage> files = fileMongoRepository.findByTagsIn(tagList);
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
			List<QuestionMaster> projectInsightQuestionMasterList = questionMasterRepository
					.findByEntityIdAndEntityType(entityId, entityType);
			if (!projectInsightQuestionMasterList.isEmpty()) {
				for (QuestionMaster questionMaster : projectInsightQuestionMasterList) {
					ProjectQuestionDTO projectQuestionDTO = mapQuestionMasterToProjectQuestionDTO(questionMaster);
					List<ProjectResponseDTO> projectResponseDTOList = new ArrayList<>();

					List<ProjectInsightResponse> projectInsightResponseList = projectInsightResponseRepository
							.findByQuestionMasterIdAndIsApprovedForKnowledgeHub(questionMaster.getQuestionMasterId(),
									"Y");
					if (projectInsightResponseList != null && !projectInsightResponseList.isEmpty()) {
						for (ProjectInsightResponse projectInsightResponse : projectInsightResponseList) {
							ProjectResponseDTO projectResponseDTO = mapPojectInsightResponseToProjectInsightResponseDTO(
									projectInsightResponse, questionMaster.getOptions());
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

	public ProjectInsightDTO getAllMilestoneInfoByProjectId(Long projectId) {
		ProjectInsightDTO response = new ProjectInsightDTO();
		try {
			// projectinfo + Milestone .....
			Project project = projectRepository.findByProjectId(projectId.intValue());

			if (project != null) {
				// basic project info
				List<Object[]> deptData = departmentRepository.getMappedDepartment(project.getProjectId());
				List<String> departmentNames = deptData.stream().map(obj -> obj[0] != null ? obj[0].toString() : null)
						.filter(Objects::nonNull).collect(Collectors.toList());
				Employee empObject = employeeRepository.findByEmpId(project.getProjectManagerId());
				List<TagMaster> tagMasterList = tagMasterRepository.findByProjectId(project.getProjectId().longValue());

				response.setQuestionList(getProjectQuestionDTOListForSearchResult(projectId, "Project"));
				response.setProjectId(projectId);
				response.setProjectManagerId(project.getProjectManagerId());
				response.setProjectManagerName(getProjectManagerName(project.getProjectManagerId()));
				response.setProjectName(project.getProjectName());

				// addtional info
				response.setDepartmentList(departmentNames.toArray(new String[0]));
				response.setState(project.getState());
				response.setTagList(
						tagMasterList.stream().map(TagMaster::getTag).limit(8).collect(Collectors.toList()));
				response.setCreatedOn(project.getCreatedOn() != null ? project.getCreatedOn().toString() : null);
				response.setUpdatedOn(project.getUpdatedOn() != null ? project.getUpdatedOn().toString() : null);

				List<ProjectInsightMilestone> projectMilestoneListByProjectId = projectInsightMilestoneRepository
						.getByProjectId(projectId);
				if (!projectMilestoneListByProjectId.isEmpty()) {
					List<ProjectInsightMilestoneDTO> milestoneList = new ArrayList<>();
					projectMilestoneListByProjectId.forEach((object) -> {
						ProjectInsightMilestoneDTO projectInsightMilestoneDTO = new ProjectInsightMilestoneDTO();

						projectInsightMilestoneDTO.setMilestoneId(object.getMilestoneId());
						projectInsightMilestoneDTO.setMilestone(object.getMilestone());
						projectInsightMilestoneDTO.setDescription(object.getDescription());
						projectInsightMilestoneDTO.setDeptId(object.getDeptId());

						projectInsightMilestoneDTO.setQuestionList(
								getProjectQuestionDTOListForSearchResult(object.getMilestoneId(), "Milestone"));
						projectInsightMilestoneDTO
								.setModuleList(getAllModuleInfoByMilestoneId(object.getMilestoneId()).getModuleList());

						milestoneList.add(projectInsightMilestoneDTO);
					});

					response.setProjectInsightMilestoneList(milestoneList);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	public ProjectInsightMilestoneDTO getAllModuleInfoByMilestoneId(Long milestoneId) {
		ProjectInsightMilestoneDTO projectInsightMilestoneDTOList = new ProjectInsightMilestoneDTO();
		try {
			// Milestone + List<Module> .....
			ProjectInsightMilestone milestoneObject = projectInsightMilestoneRepository.getByMilestoneId(milestoneId);

			if (milestoneObject != null) {
				projectInsightMilestoneDTOList.setMilestone(milestoneObject.getMilestone());
				projectInsightMilestoneDTOList.setMilestoneId(milestoneObject.getMilestoneId());
				projectInsightMilestoneDTOList.setDescription(milestoneObject.getDescription());
				projectInsightMilestoneDTOList.setDeptId(milestoneObject.getDeptId());
				projectInsightMilestoneDTOList.setCreatedOn(milestoneObject.getCreatedOn().toString());
				projectInsightMilestoneDTOList.setQuestionList(
						getProjectQuestionDTOListForSearchResult(milestoneObject.getProjectId(), "Project"));

				List<ProjectInsightModule> projectInsightModuleList = projectInsightModuleRepository
						.findByMilestoneId(milestoneId);
				if (!projectInsightModuleList.isEmpty()) {
					List<ModuleDTO> projectInsightModuleDTOList = new ArrayList<>();
					for (ProjectInsightModule projectInsightModule : projectInsightModuleList) {
						ModuleDTO moduleDTO = new ModuleDTO();
						moduleDTO.setModuleId(projectInsightModule.getModuleId());
						moduleDTO.setMilestoneId(projectInsightModule.getMilestoneId());
						moduleDTO.setModule(projectInsightModule.getModule());
						moduleDTO.setDescription(projectInsightModule.getDescription());
						moduleDTO.setRedmineId(projectInsightModule.getRedmineId());

						moduleDTO.setQuestionList(getProjectQuestionDTOListForSearchResult(
								projectInsightModule.getMilestoneId(), "Milestone"));
						moduleDTO.setSubModuleList(
								getAllSubModuleInfoByModuleId(projectInsightModule.getModuleId(), "SubModule")
										.getSubModuleList());

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

	public ModuleDTO getAllSubModuleInfoByModuleId(Long moduleId, String subModuleType) {
		ModuleDTO moduleDTO = new ModuleDTO();
		try {
			// Module + List<Submodule> .....
			ProjectInsightModule moduleObject = projectInsightModuleRepository.getByModuleId(moduleId);
			if (moduleObject != null) {
				moduleDTO.setModule(moduleObject.getModule());
				moduleDTO.setModuleId(moduleObject.getModuleId());
				moduleDTO.setMilestoneId(moduleObject.getMilestoneId());
				moduleDTO.setDescription(moduleObject.getDescription());
				moduleDTO.setRedmineId(moduleObject.getRedmineId());
				moduleDTO.setQuestionList(
						getProjectQuestionDTOListForSearchResult(moduleObject.getModuleId(), "Module"));

				List<ProjectInsightSubModule> projectInsightSubModuleList = projectInsightSubModuleRepository
						.getByModuleIdAndSubModuleType(moduleId, subModuleType);
				if (!projectInsightSubModuleList.isEmpty()) {
					List<SubModuleDTO> subModuleList = new ArrayList<>();

					for (ProjectInsightSubModule projectInsightSubModule : projectInsightSubModuleList) {
						SubModuleDTO subModuleDTO = new SubModuleDTO();
						subModuleDTO.setSubModuleId(projectInsightSubModule.getSubmoduleId());
						subModuleDTO.setSubModule(projectInsightSubModule.getSubmodule());
						subModuleDTO.setModuleId(projectInsightSubModule.getModuleId());
						subModuleDTO.setDescription(projectInsightSubModule.getDescription());
						subModuleDTO.setRedmineId(projectInsightSubModule.getRedmineId());
						subModuleDTO.setQuestionList(getProjectQuestionDTOListForSearchResult(
								projectInsightSubModule.getSubmoduleId(), subModuleType));

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

	public SubModuleDTO getAllSubSubModuleInfoBySubModuleId(Long subModuleId, String subModuleType) {
		SubModuleDTO subModuleDTO = new SubModuleDTO();
		try {
			// Module + List<Submodule> .....
			ProjectInsightSubModule subModuleObject = projectInsightSubModuleRepository.getBySubmoduleId(subModuleId);
			if (subModuleObject != null) {
				subModuleDTO.setSubModule(subModuleObject.getSubmodule());
				subModuleDTO.setSubModuleId(subModuleObject.getSubmoduleId());
				subModuleDTO.setModuleId(subModuleObject.getModuleId());
				subModuleDTO.setDescription(subModuleObject.getDescription());
				subModuleDTO.setRedmineId(subModuleObject.getRedmineId());
				subModuleDTO.setQuestionList(
						getProjectQuestionDTOListForSearchResult(subModuleObject.getSubmoduleId(), subModuleType));

				List<ProjectInsightSubModule> projectInsightSubModuleList = projectInsightSubModuleRepository
						.getByModuleIdAndSubModuleType(subModuleId, subModuleType);
				if (!projectInsightSubModuleList.isEmpty()) {
					List<SubModuleDTO> subsubModuleList = new ArrayList<>();

					for (ProjectInsightSubModule projectInsightSubModule : projectInsightSubModuleList) {
						SubModuleDTO subsubModuleDTO = new SubModuleDTO();
						subsubModuleDTO.setSubModuleId(projectInsightSubModule.getSubmoduleId());
						subsubModuleDTO.setSubModule(projectInsightSubModule.getSubmodule());
						subsubModuleDTO.setModuleId(projectInsightSubModule.getModuleId());
						subsubModuleDTO.setDescription(projectInsightSubModule.getDescription());
						subsubModuleDTO.setRedmineId(projectInsightSubModule.getRedmineId());
						subsubModuleDTO.setQuestionList(getProjectQuestionDTOListForSearchResult(
								projectInsightSubModule.getSubmoduleId(), subModuleType));

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

			// if ("Project".equals(entityType)) {
			// ProjectInsightDTO projectInsightDTO =
			// getAllMilestoneInfoByProjectId(projectId);
			// response.setProject(projectInsightDTO);
			//
			// } else if ("Milestone".equals(entityType)) {
			// ProjectInsightMilestoneDTO milestone =
			// getAllModuleInfoByMilestoneId(entityId);
			// response.setMilestone(milestone);
			//
			// } else if ("Module".equals(entityType)) {
			// ModuleDTO module = getAllSubModuleInfoByModuleId(entityId, "SubModule");
			// response.setModule(module);
			//
			// } else if ("SubModule".equals(entityType) ||
			// "Sub-SubModule".equals(entityType)) {
			// SubModuleDTO submodule = getAllSubSubModuleInfoBySubModuleId(entityId,
			// "SubModule");
			//
			// if("SubModule".equals(entityType)) {
			// response.setSubModule(submodule);
			// }else {
			// response.setSubSubModule(submodule);
			// }
			// } else {
			// System.out.println("invalid");
			// }
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok(response);
	}

	public ResponseEntity<List<ProjectInsightFilterDTO>> getFilterList() {
		List<ProjectInsightFilterDTO> response = new ArrayList<>();
		try {
			List<ProjectInsightFilter> projectFilterList = projectInsightFilterRepository.findAll();
			if (!projectFilterList.isEmpty()) {
				projectFilterList.forEach((object) -> {
					List<ProjectInsightFilterOptions> projectFilterOptionList = new ArrayList<>();

					if (object.getFilterName().equals("Department")) {
						// getAllDepartmentList
						List<Department> deptList = departmentRepository.findAll();
						if (!deptList.isEmpty()) {
							for (Department dept : deptList) {
								ProjectInsightFilterOptions dto = new ProjectInsightFilterOptions();

								dto.setOptionId(dept.getDeptId());
								dto.setOptionName(dept.getName());
								dto.setFilterId(object.getFilterId());

								projectFilterOptionList.add(dto);
							}
						}
					} else {
						projectFilterOptionList = projectInsightFilterOptionsRepository
								.findByFilterId(object.getFilterId());
					}

					ProjectInsightFilterDTO dto = new ProjectInsightFilterDTO();

					dto.setFilterId(object.getFilterId());
					dto.setFilterName(object.getFilterName());
					dto.setOptionList(projectFilterOptionList != null ? projectFilterOptionList : null);

					response.add(dto);
				});
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok(response);
	}

	/*
	 * ------------------------------------- ProjectInsight
	 * --------------------------------------------------
	 * 
	 */

	public ResponseEntity<List<ProjectInsightUserContributionDTO>> getContributionByEmpId(
			ProjectInsightUserContributionDTO projectInsightUserContributionDTO) {

		List<ProjectInsightUserContributionDTO> response = new ArrayList<>();

		try {
			if (projectInsightUserContributionDTO == null || projectInsightUserContributionDTO.getEmpId() == null) {
				return ResponseEntity.badRequest().body(Collections.emptyList());
			}

			List<ProjectInsightUserContribution> userContributions = projectInsightUserContributionRepository
					.findByEmpId(projectInsightUserContributionDTO.getEmpId());

			if (userContributions != null && !userContributions.isEmpty()) {
				response = userContributions.stream().map(object -> {

					List<ProjectInsightAssignees> alltaggedUser = projectInsightAssigneesRepository
							.getByEntityIdAndEntityType(object.getUserContributionId(),
									"UserContribution");
					List<TagMaster> dbTagMasterResponse = tagMasterRepository.findByEntityIdAndEntityTypeAndType(
							object.getUserContributionId(), "UserContribution", "user");
					List<UserContributionDocument> dbDocumentResponse = userContributionDocumentRepository
							.findByUserContributionId(object.getUserContributionId());
					List<UserContributionResponseRemarks> responseRemarkDbResp = userContributionResponseRemarksRepository
							.findByUserContributionId(object.getUserContributionId());

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
					dto.setTeamMembers(alltaggedUser.stream().map(assignObj -> assignObj.getAssignedTo())
							.collect(Collectors.toList()));
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
				Optional<ProjectInsightUserContribution> optionalEntity = projectInsightUserContributionRepository
						.findById(dto.getUserContributionId());

				if (optionalEntity.isPresent()) {
					entity = optionalEntity.get();
					entity.setUpdatedOn(LocalDateTime.now());
					entity.setStatus("Pending");

					if (entity.getAssignTo() == null) {
						entity.setAssignTo(
								empObj != null ? empObj.getReportingManagerId() != null ? empObj.getReportingManagerId()
										: empObj.getManagerId() : null);
					}

				} else {
					response.setServiceMessage("User contribution not found with ID: " + dto.getUserContributionId());
					return ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body(response);
				}
			} else {

				entity = new ProjectInsightUserContribution();
				entity.setAssignTo(
						empObj != null ? empObj.getReportingManagerId() != null ? empObj.getReportingManagerId()
								: empObj.getManagerId() : null);
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

			if (dbResponse != null) {
				// Add new team member tag
				List<ProjectInsightAssignees> alltaggedUser = projectInsightAssigneesRepository
						.getByEntityIdAndEntityType(dbResponse.getUserContributionId(),
								"UserContribution");
				if (!alltaggedUser.isEmpty()) {
					projectInsightAssigneesRepository.deleteAll(alltaggedUser);
				}

				if (!dto.getTeamMembers().isEmpty()) {
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

				// Add tags
				List<TagMaster> dbTagMasterResponse = tagMasterRepository
						.findByEntityIdAndEntityTypeAndType(dbResponse.getUserContributionId(), "UserContribution",
								"user");

				if (!dbTagMasterResponse.isEmpty()) {
					tagMasterRepository.deleteAll(dbTagMasterResponse);
				}
				if (!dto.getTags().isEmpty()) {
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

				// Upload Files
				List<UserContributionDocument> existingDocs = userContributionDocumentRepository
						.findByUserContributionId(dbResponse.getUserContributionId());

				final Set<Long> documentIdsToKeep = dto.getUserDocument() != null ? dto.getUserDocument().stream()
						.map(UserContributionDocument::getDocumentId)
						.collect(Collectors.toSet()) : new HashSet<>();

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
							String uploadResponse = uploadProjectResponseDocument(document,
									dbResponse.getUserContributionId(), "UserContribution");
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

			response.setServiceMessage(dto.getUserContributionId() != null ? "User contribution updated successfully."
					: "User contribution created successfully.");
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
			if (projectInsightUserContributionDTO.getEmpId() != null
					&& projectInsightUserContributionDTO.getAssignTo() != null) {
				// Get data for manager i.e ---> Team dashboard --> user's contribution

				userContributionsForReview = projectInsightUserContributionRepository
						.findByEmpIdAndAssignTo(projectInsightUserContributionDTO.getEmpId(),
								projectInsightUserContributionDTO.getAssignTo());

			} else if (projectInsightUserContributionDTO.getAssignTo() != null) {
				// Get for pre reviewer i.e ---> My performance dashboard --> (view assigned
				// contribution)

				userContributionsForReview = projectInsightUserContributionRepository
						.findByAssignTo(projectInsightUserContributionDTO.getAssignTo());

			}

			if (userContributionsForReview != null && !userContributionsForReview.isEmpty()) {
				response = userContributionsForReview.stream().map(object -> {

					List<ProjectInsightAssignees> alltaggedUser = projectInsightAssigneesRepository
							.getByEntityIdAndEntityType(object.getUserContributionId(),
									"UserContribution");
					List<TagMaster> dbTagMasterResponse = tagMasterRepository.findByEntityIdAndEntityTypeAndType(
							object.getUserContributionId(), "UserContribution", "user");
					List<UserContributionDocument> dbDocumentResponse = userContributionDocumentRepository
							.findByUserContributionId(object.getUserContributionId());
					List<UserContributionResponseRemarks> responseRemarkDbResp = userContributionResponseRemarksRepository
							.findByUserContributionId(object.getUserContributionId());

					ProjectInsightUserContributionDTO dto = new ProjectInsightUserContributionDTO();
					dto.setAssignTo(object.getAssignTo());
					dto.setCreatedOn(object.getCreatedOn() != null ? object.getCreatedOn().toString() : null);
					dto.setEmpId(object.getEmpId());
					dto.setContributionBy(
							Optional.ofNullable(employeeList)
									.orElse(Collections.emptyList())
									.stream()
									.filter(obj -> obj[0] != null
											&& Long.valueOf(obj[0].toString()).equals(object.getEmpId()))
									.map(obj -> obj[3] != null ? obj[3].toString() : null)
									.findFirst()
									.orElse(null));
					dto.setProjectId(object.getProjectId());
					dto.setResponse(object.getResponse());
					dto.setStatus(object.getStatus());
					dto.setUpdatedOn(object.getUpdatedOn() != null ? object.getUpdatedOn().toString() : null);
					dto.setUserContributionId(object.getUserContributionId());
					dto.setUserDefinedProjectName(object.getUserDefinedProjectName());
					dto.setParentContribution(object.getParentContribution());
					dto.setTitle(object.getTitle());
					dto.setOnlyText(object.getOnlyTextResponse());
					dto.setTeamMembers(alltaggedUser.stream().map(ProjectInsightAssignees::getAssignedTo)
							.collect(Collectors.toList()));
					dto.setTags(dbTagMasterResponse.stream().map(TagMaster::getTag).collect(Collectors.toList()));
					dto.setUserDocument(dbDocumentResponse);
					dto.setResponseRemarkList(responseRemarkDbResp);
					dto.setReviewType(object.getReviewType());

					return dto;
				}).collect(Collectors.toList());
			}

		} catch (Exception e) {
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

			if (!dbResponse.isEmpty()) {
				ProjectInsightUserContribution userContributionDbResp = dbResponse.get();

				if (projectInsightUserContributionDTO.getProcessType().equals("preReviewer")) {
					userContributionDbResp.setAssignTo(projectInsightUserContributionDTO.getAssignTo());
					userContributionDbResp.setReviewType("preReviewer");
				} else {
					userContributionDbResp.setStatus(projectInsightUserContributionDTO.getStatus());
					userContributionDbResp.setProjectId(projectInsightUserContributionDTO.getProjectId());

					if (projectInsightUserContributionDTO.getProcessType().equals("Reject")) {
						userContributionDbResp.setAssignTo(null);
					} else {
						if (projectInsightUserContributionDTO.getReviewType() == null
								|| projectInsightUserContributionDTO.getReviewType().equals("preReviewer")) {
							Employee empObj = employeeRepository.findByEmpId(userContributionDbResp.getEmpId());
							userContributionDbResp.setAssignTo(empObj != null
									? empObj.getReportingManagerId() != null ? empObj.getReportingManagerId()
											: empObj.getManagerId()
									: null);

							userContributionDbResp.setReviewType("manager");
						}
					}

					// Add remarks
					if (projectInsightUserContributionDTO.getRemark() != null) {
						UserContributionResponseRemarks newObj = new UserContributionResponseRemarks();
						newObj.setRemark(projectInsightUserContributionDTO.getRemark());
						newObj.setRemarkBy(projectInsightUserContributionDTO.getAssignTo());
						newObj.setUserContributionId(userContributionDbResp.getUserContributionId());
						newObj.setRemarkStatus(projectInsightUserContributionDTO.getProcessType());

						userContributionResponseRemarksRepository.save(newObj);
					}
				}

				projectInsightUserContributionRepository.save(userContributionDbResp);
			} else {
				response.setServiceMessage("No contribution found !!");
				return ResponseEntity.badRequest().body(response);
			}

			response.setServiceMessage("Review added successfully !!");
			return ResponseEntity.ok(response);
		} catch (Exception e) {
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

			if (!tagDTOList.isEmpty()) {
				tagDTOList.forEach((tagobj) -> {
					tagUtils.saveTags(tagobj);
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	// Search tag
	// ------------------------------------------------------------------------

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
	private void processSubSubModuleNode(SubModuleDTO subSubModule, List<TagDTO> tagDTOList, Long subModuleId,
			Long projectId) {
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

			Project project = projectRepository
					.findByProjectId(Integer.parseInt(projectInsightDTO.getProjectId().toString()));
			if (project == null) {
				throw new RuntimeException("Project Not Found!!");
			}

			extractTagDataFromProjectInsight(projectInsightDTO);

			if (projectInsightDTO.getQuestionList() != null && !projectInsightDTO.getQuestionList().isEmpty()) {
				saveQuestionResponsesReviewPoints(projectInsightDTO.getQuestionList(), projectInsightDTO.getProjectId(),
						projectInsightDTO.getPointsBy(), projectInsightDTO.getIsPointsDrafted(),
						projectInsightDTO.getProjectId(), "Project", projectInsightDTO.getTransferToKnowledgeHub());
			}

			if (projectInsightDTO.getProjectInsightMilestoneList() != null
					&& !projectInsightDTO.getProjectInsightMilestoneList().isEmpty()) {
				saveMilestoneReviewPoints(projectInsightDTO.getProjectInsightMilestoneList(),
						projectInsightDTO.getProjectId(), projectInsightDTO.getPointsBy(),
						projectInsightDTO.getIsPointsDrafted(), projectInsightDTO.getTransferToKnowledgeHub());
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

	private void saveMilestoneReviewPoints(List<ProjectInsightMilestoneDTO> projectInsightMilestoneList, Long projectId,
			Long pointsBy, String pointsDrafted, String transferToKnowledgeHub) {
		try {
			for (ProjectInsightMilestoneDTO projectInsightMilestoneDTO : projectInsightMilestoneList) {
				if (projectInsightMilestoneDTO.getQuestionList() != null
						&& !projectInsightMilestoneDTO.getQuestionList().isEmpty()) {
					saveQuestionResponsesReviewPoints(projectInsightMilestoneDTO.getQuestionList(), projectId, pointsBy,
							pointsDrafted, projectInsightMilestoneDTO.getMilestoneId(), "Milestone",
							transferToKnowledgeHub);
				}
				if (projectInsightMilestoneDTO.getModuleList() != null
						&& !projectInsightMilestoneDTO.getModuleList().isEmpty()) {
					saveModuleReviewPoints(projectInsightMilestoneDTO.getModuleList(), projectId, pointsBy,
							pointsDrafted, transferToKnowledgeHub);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void saveModuleReviewPoints(List<ModuleDTO> moduleList, Long projectId, Long pointsBy, String pointsDrafted,
			String transferToKnowledgeHub) {
		try {
			for (ModuleDTO moduleDTO : moduleList) {
				if (moduleDTO.getQuestionList() != null && !moduleDTO.getQuestionList().isEmpty()) {
					saveQuestionResponsesReviewPoints(moduleDTO.getQuestionList(), projectId, pointsBy, pointsDrafted,
							moduleDTO.getModuleId(), "Module", transferToKnowledgeHub);
				}

				if (moduleDTO.getSubModuleList() != null && !moduleDTO.getSubModuleList().isEmpty()) {
					saveSubModuleReviewPoints(moduleDTO.getSubModuleList(), projectId, pointsBy, pointsDrafted,
							"SubModule", transferToKnowledgeHub);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void saveSubModuleReviewPoints(List<SubModuleDTO> subModuleList, Long projectId, Long pointsBy,
			String pointsDrafted, String subModuleType, String transferToKnowledgeHub) {
		try {
			for (SubModuleDTO subModuleDTO : subModuleList) {
				if (subModuleDTO.getQuestionList() != null && !subModuleDTO.getQuestionList().isEmpty()) {
					saveQuestionResponsesReviewPoints(subModuleDTO.getQuestionList(), projectId, pointsBy,
							pointsDrafted, subModuleDTO.getSubModuleId(), subModuleType, transferToKnowledgeHub);
				}

				if (subModuleDTO.getSubSubModuleList() != null && !subModuleDTO.getSubSubModuleList().isEmpty()) {
					saveSubModuleReviewPoints(subModuleDTO.getSubSubModuleList(), projectId, pointsBy, pointsDrafted,
							"Sub-SubModule", transferToKnowledgeHub);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void saveQuestionResponsesReviewPoints(List<ProjectQuestionDTO> questionList, Long projectId, Long pointsBy,
			String pointsDrafted, Long entityId, String entityType, String transferToKnowledgeHub) {
		try {
			for (ProjectQuestionDTO projectQuestionDTO : questionList) {
				if (projectQuestionDTO.getQuestionId() != null) {
					QuestionMaster questionMaster = questionMasterRepository
							.findByQuestionMasterId(projectQuestionDTO.getQuestionId());
					if (questionMaster != null) {
						questionMaster.setRecommendedResponseId(projectQuestionDTO.getRecommendedResponseId());
						questionMasterRepository.save(questionMaster);
					}
				}
				if (projectQuestionDTO.getProjectResponseList() != null
						&& !projectQuestionDTO.getProjectResponseList().isEmpty()) {
					for (ProjectResponseDTO projectResponseDTO : projectQuestionDTO.getProjectResponseList()) {
						if (projectResponseDTO.getProjectInsightResponseId() != null
								&& !transferToKnowledgeHub.equals(null) && transferToKnowledgeHub.equals("Y")) {
							ProjectInsightResponse projectInsightResponse = projectInsightResponseRepository
									.findById(projectResponseDTO.getProjectInsightResponseId()).orElseGet(null);
							if (projectInsightResponse != null) {
								projectInsightResponse.setIsApprovedForKnowledgeHub(
										projectResponseDTO.isApprovedForKnowledgeHub() ? "Y" : "N");
								projectInsightResponseRepository.save(projectInsightResponse);
							}
						}
						saveResponsePoints(projectResponseDTO.getProjectInsightResponsePointList(),
								projectResponseDTO.getProjectInsightResponseId(), pointsBy, pointsDrafted,
								transferToKnowledgeHub);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void saveResponsePoints(List<ProjectInsightResponsePointsDTO> projectInsightResponsePointList,
			Long projectInsightResponseId, Long pointsBy, String isPointsDrafted, String transferToKnowledgeHub) {
		try {
			if (projectInsightResponsePointList != null && !projectInsightResponsePointList.isEmpty()) {
				for (ProjectInsightResponsePointsDTO projectInsightResponsePointsDTO : projectInsightResponsePointList) {
					if (projectInsightResponsePointsDTO.getPoints() != null) {
						ProjectInsightResponsePoints projectInsightResponsePoints = projectInsightResponsePointsRepository
								.findByResponseIdAndPointsBy(projectInsightResponseId, pointsBy);
						if (projectInsightResponsePoints != null) {
							projectInsightResponsePoints.setPoints(projectInsightResponsePointsDTO.getPoints());
							projectInsightResponsePoints.setIsPointsDrafted(isPointsDrafted);
							if (isPointsDrafted.equals("N")) {
								projectInsightResponsePoints
										.setFinalSubmittedOn(new Timestamp(System.currentTimeMillis()));
							}
							projectInsightResponsePoints.setUpdatedOn(new Timestamp(System.currentTimeMillis()));
						} else {
							projectInsightResponsePoints = new ProjectInsightResponsePoints();
							projectInsightResponsePoints.setPointsBy(pointsBy);
							projectInsightResponsePoints.setResponseId(projectInsightResponseId);
							projectInsightResponsePoints.setPoints(projectInsightResponsePointsDTO.getPoints());
							projectInsightResponsePoints.setIsPointsDrafted(isPointsDrafted);
							if (isPointsDrafted.equals("N")) {
								projectInsightResponsePoints
										.setFinalSubmittedOn(new Timestamp(System.currentTimeMillis()));
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

	/*
	 * New Implimentation Project Insight : MongoDB ----------- [START]
	 * --------------------------------------
	 */

	public ProjectInsightStructure saveAsDraft(ProjectInsightStructure structure) {
		boolean isNew = (structure.getId() == null);

		if (!isNew) {
			Optional<ProjectInsightStructure> existingOpt = projectInsightStructureRepository.findById(structure.getId());

			if (existingOpt.isPresent()) {
				ProjectInsightStructure existing = existingOpt.get();

				if (!"Y".equalsIgnoreCase(existing.getIsDraft())) {
					throw new RuntimeException("The document is not a draft and cannot be saved as a draft.");
				}

				structure.setCreatedBy(existing.getCreatedBy());
				structure.setCreatedOn(existing.getCreatedOn());
			} else {
				structure.setCreatedBy(structure.getCreatedBy());
				structure.setCreatedOn(getCurrentTimeInString());
			}
		} else {
			structure.setCreatedBy(structure.getCreatedBy());
			structure.setCreatedOn(getCurrentTimeInString());
		}

		structure.setUpdatedOn(getCurrentTimeInString());

		structure.setIsDraft("Y");

		return saveStructure(structure, "Y");
	}

	public ProjectInsightStructure saveAndAssign(ProjectInsightStructure structure) {
		boolean isNew = (structure.getId() == null);

		if (!isNew) {
			ProjectInsightStructure existing = projectInsightStructureRepository
					.findById(structure.getId())
					.orElse(null);

			if (existing != null) {
				existing.setData(structure.getData());
				existing.setStructure(structure.getStructure());
				existing.setUpdatedOn(getCurrentTimeInString());
				existing.setIsDraft("N");
				return projectInsightStructureRepository.save(existing); 
			} else {
				throw new RuntimeException("ProjectInsightStructure not found for update.");
			}
		} else {
			structure.setCreatedOn(getCurrentTimeInString());
			structure.setUpdatedOn(getCurrentTimeInString());
			structure.setIsDraft("N");

			ProjectInsightStructure dbResponse = projectInsightStructureRepository.save(structure);

			if (dbResponse != null) {
				String response = saveMappingInfo(dbResponse);
			} else {
				throw new RuntimeException("Unable to save Project Insight.");
			}

			return dbResponse;

		}
	}

	private ProjectInsightStructure saveStructure(ProjectInsightStructure structure, String isDraftFlag) {
		try {
			structure.setCreatedBy(structure.getCreatedBy());
			structure.setCreatedOn(getCurrentTimeInString());
			structure.setIsDraft(isDraftFlag);
			ProjectInsightStructure dbResponse = null;
			boolean isExists = projectInsightStructureRepository.existsById(structure.getId());

			if (structure.getId() != null && isExists) {
				try {
					dbResponse = projectInsightStructureRepository.save(structure);
				} catch (DataAccessException e) {
					structure.setId(null);
					dbResponse = projectInsightStructureRepository.save(structure);
				}
			} else {
				// New document
				structure.setId(null);
				dbResponse = projectInsightStructureRepository.save(structure);
			}

			if (dbResponse != null) {
				String response = saveMappingInfo(dbResponse);
			} else {
				throw new RuntimeException("Unable to save Project Insight.");
			}
			return dbResponse;
		} catch (DataAccessException dae) {
			throw dae;
		} catch (Exception ex) {
			throw new RuntimeException("Unexpected error occurred while saving Project Insight Structure.", ex);
		}
	}

	public String saveMappingInfo(ProjectInsightStructure structure) {
		Object projectIdObj = structure.getData().getFields()
				.entrySet()
				.stream()
				.filter(e -> e.getKey().toLowerCase().contains("project"))
				.map(Map.Entry::getValue)
				.findFirst()
				.orElse(null);

		if (projectIdObj == null) {
			throw new IllegalArgumentException("Project ID (projectname) is missing in the form data.");
		}

		Integer projectId;
		try {
			projectId = Integer.valueOf(projectIdObj.toString());
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Project ID (projectname) is not a valid number.");
		}

		ProjectInsighProjectMapping mappingResponse = projectInsighProjectMappingRepository.findByProjectId(projectId);

		if (mappingResponse != null) {
			mappingResponse.setProjectInsightId(structure.getId().toString());

			projectInsighProjectMappingRepository.save(mappingResponse);
			return "Mapping updated successfully for projectId: " + projectId;
		} else {
			ProjectInsighProjectMapping newObj = new ProjectInsighProjectMapping();
			newObj.setProjectId(projectId);
			newObj.setProjectInsightId(structure.getId().toString());
			newObj.setIsDraft(structure.getIsDraft());
			newObj.setCreatedBy(Long.parseLong(structure.getCreatedBy()));

			projectInsighProjectMappingRepository.save(newObj);
			return "New mapping created for projectId: " + projectId;
		}
	}

	private Long convertToLong(Object obj) {
		if (obj instanceof Number) {
			return ((Number) obj).longValue();
		}
		try {
			return Long.parseLong(obj.toString());
		} catch (Exception e) {
			return null;
		}
	}

	@Autowired
	private ProjectInsightDomainDataRepository projectInsightDomainDataRepository;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private ProjectInsightDomainDataFlatSearchRepository domainFlatSearchRepo;

	@SuppressWarnings("unchecked")
	public boolean isLong(Object obj) {
		if (obj == null) {
			return false;
		}

		if (obj instanceof String) {
			return isStringLong((String) obj);
		}

		if (obj instanceof List) {
			List<?> list = (List<?>) obj;
			for (Object item : list) {
				if (!(item instanceof String) || !isStringLong((String) item)) {
					return false;
				}
			}
			return true;
		}

		// You can expand to arrays, sets, etc. if needed
		return false;
	}

	public boolean isStringLong(String str) {
		try {
			Long.parseLong(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public List<ProjectInsighProjectMappingDTO> getAllProjectInsight(String domainName, String unique_name, String ids) {
		try {
			if (domainName == null) {
				return Optional.ofNullable(projectInsighProjectMappingRepository.fetchAllProjectMappings())
						.orElseThrow(() -> new RuntimeException("No Project Insight found !!."));
			}

			// handle unique_name (can be null, single, or multiple)
			List<String> uniqueNames = new ArrayList<>();
			if (unique_name != null && !unique_name.trim().isEmpty()) {
				uniqueNames = Arrays.stream(unique_name.split(","))
									.map(String::trim)
									.filter(s -> !s.isEmpty())
									.collect(Collectors.toList());
			}

			List<Long> insightIds = new ArrayList<>();
			if (ids != null && !ids.trim().isEmpty()) {
				insightIds = Arrays.stream(ids.split(","))
						.map(s -> s.trim())                 
						.filter(s -> !s.isEmpty())         
						.filter(s -> isLong(s))        
						.map(s -> Long.valueOf(s))       
						.collect(Collectors.toList());
			}

			List<Long> domainIds = new ArrayList<>();
			List<String> domainNames = Arrays.stream(domainName.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.toList());


			boolean isAllLong = domainNames.stream().allMatch(this::isLong);

			if(isAllLong) {
				for(String dn : domainNames) {
					if (isLong(dn)) {
						domainIds.add(Long.parseLong(dn));
					}
				} 
			}
			else {
				List<ProjectInsightDomainData> domains = projectInsightDomainDataRepository.findByNameIn(domainNames);
				if (domains.isEmpty()) {
					throw new RuntimeException("No Project Insight found !!.");
				}
				domainIds = domains.stream().map(ProjectInsightDomainData::getId).collect(Collectors.toList());
			}
			List<String> domainIdsString = domainIds.stream().map(String::valueOf).collect(Collectors.toList());

			// Base criteria (domain filter)
			Criteria domainCriteria = new Criteria().orOperator(
					Criteria.where("additionalInfo.domainname.id").in(domainIds),
					Criteria.where("additionalInfo.domainname").in(domainIds)
			);

			Criteria finalCriteria;

			if (uniqueNames.isEmpty()) {
				// No unique_name provided → filter only by domain
				finalCriteria = domainCriteria;
			} else {
				if(insightIds.isEmpty()){
					throw new RuntimeException("No Children Domains Insight found !!.");
				}

				List<ProjectInsightDomainData> existingChildrenDomains = projectInsightDomainDataRepository.findAllById(insightIds);

				if(existingChildrenDomains == null || existingChildrenDomains.isEmpty()){
					throw new RuntimeException("No Children Domains Insight found !!.");
				}

				List<Criteria> allNameCriteria = new ArrayList<>();

				for (String u : uniqueNames) {
					String exactField1 = "additionalInfo." + u;
					String exactField2 = "additionalInfo." + u + ".id";

					allNameCriteria.add(Criteria.where(exactField1).in(insightIds));
					allNameCriteria.add(Criteria.where(exactField2).in(insightIds));
				}

				// Final criteria = domain AND (any of the name criteria)
				finalCriteria = new Criteria().orOperator(
					new Criteria().orOperator(allNameCriteria.toArray(new Criteria[0]))
				);

			}

			Query query = new Query(finalCriteria);

			List<ProjectInsightProjectDetails> results = mongoTemplate.find(query, ProjectInsightProjectDetails.class);

			Set<Integer> projectIds = results.stream()
					.map(ProjectInsightProjectDetails::getProjectId)
					.filter(Objects::nonNull)
					.collect(Collectors.toSet());

			if (projectIds.isEmpty()) {
				return new ArrayList<>();
			}

			return projectInsighProjectMappingRepository
					.fetchAllProjectMappingsByInsightIds(new ArrayList<>(projectIds));

		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Something went wrong !!.", ex);
		}
	}

	public List<String> getDomainSearchRecommendation(String query) {
		try {
			return projectInsightDomainDataRepository.findAllDomainByName(query.toLowerCase(), PageRequest.of(0, 5));
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Something went wrong !!.", ex);
		}
	}

	public List<ProjectInsightDomainData> searchProjectInsightDomainData(String query) {
		try{
			List<ProjectInsightDomainDataFlatSearch> results = domainFlatSearchRepo.findByFlatSearch(query.toLowerCase());

			if(results == null || results.isEmpty()) {
				return null;
			}

			List<Long> domainIds = new ArrayList<>();

			for(ProjectInsightDomainDataFlatSearch result : results) {
				domainIds.add(result.getDomainId());
			}

			List<ProjectInsightDomainData> domainDatas = projectInsightDomainDataRepository.findAllById(domainIds);

			// List<Object[]> datas = domainFlatSearchRepo.findAllByFlatSearch(query.toLowerCase());
			// List<ProjectInsightDomainData> domainDatas = new ArrayList<>();

			// for (Object[] row : datas) {
			// 	ProjectInsightDomainData d = new ProjectInsightDomainData();
			// 	d.setId(((Number) row[0]).longValue());
			// 	d.setIsActive(row[4] != null ? ((Boolean) row[4]) : null);
			// 	d.setName((String) row[6]);                          
			// 	d.setType((String) row[7]);
			// 	domainDatas.add(d);
			// }
			
			return domainDatas;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Something went wrong !!.", ex);
		}
	}

	public ProjectInsightStructure getProjectInsightByInsightId(String id) {
		try {
			ObjectId objectId = new ObjectId(id);
			return projectInsightStructureRepository.findById(objectId)
					.orElseThrow(() -> new RuntimeException("Project Insight not found with id: " + id));
		} catch (Exception e) {
			throw new RuntimeException("Something went wrong !!", e);
		}
	}

	@Transactional
	public ResponseEntity<ServiceResponse> deleteProjectInsightById(String id) {
		ServiceResponse response = new ServiceResponse();
		try {
			ObjectId objectId = new ObjectId(id);
			// if (!projectInsightStructureRepository.existsById(objectId)) {
			// 	throw new RuntimeException("Cannot delete. Project Insight Structure not found with ID: " + id);
			// }
			ProjectInsighProjectMapping mappingDbResponse = projectInsighProjectMappingRepository
					.findByProjectInsightDetailsId(id);

			if (mappingDbResponse != null) {
				projectInsighProjectMappingRepository.deleteById(mappingDbResponse.getProjectInsightProjectMappingId());
			}

			if(!projectInsightProjectDetailsRepository.existsById(id)){
				throw new RuntimeException("Cannot delete. Project Insight Structure not found with ID: " + id);
			}

			projectInsightProjectDetailsRepository.deleteById(id);

			projectInsightGroupDetailsRepository.deleteByParentPathIds0(id);

			// projectInsightQuestionDetailsRepository.deleteByParentPathIds0(id);

			Query query = new Query(Criteria.where("parentPathIds.0").is(id));
        	List<ProjectInsightQuestionDetails> projectInsightQuestionDetails = mongoTemplate.findAllAndRemove(query, ProjectInsightQuestionDetails.class);

			List<String> quesIds = projectInsightQuestionDetails.stream().map(ProjectInsightQuestionDetails::getId).collect(Collectors.toList());

			
			projectInsightResponseDetailsRepository.deleteByQuesIdIn(quesIds);

			projectInsightProjectFlatSearchRepository.deleteByParentIds0(id);

			// response details is not commited yet

			response.setServiceMessage("Deleted successfully");
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Something went wrong unable to delete Project !!", e);
		}
	}

	public ProjectInsightStructure updateProjectInsightById(String id, ProjectInsightStructure updatedData) {

		ObjectId objectId = new ObjectId(id);

		Optional<ProjectInsightStructure> existingOpt = projectInsightStructureRepository.findById(objectId);

		if (existingOpt.isEmpty()) {
			throw new RuntimeException("Cannot update. Project Insight Structure not found with ID: " + id);
		}

		ProjectInsightStructure existing = existingOpt.get();
		existing.setStructure(updatedData.getStructure());
		existing.setData(updatedData.getData());
		existing.setUpdatedOn(getCurrentTimeInString());
		existing.setUpdatedBy(updatedData.getUpdatedBy());
		existing.setIsDraft(updatedData.getIsDraft());

		ProjectInsightStructure projectDbResponse = projectInsightStructureRepository.save(existing);

		if (projectDbResponse != null) {
			ProjectInsighProjectMapping mappingResponse = projectInsighProjectMappingRepository
					.findByProjectInsightDetailsId(id);

			if (mappingResponse != null) {
				mappingResponse.setIsDraft(projectDbResponse.getIsDraft());
				mappingResponse.setUpdatedBy(Long.parseLong(projectDbResponse.getUpdatedBy()));

				projectInsighProjectMappingRepository.save(mappingResponse);
			}
		}
		return projectInsightStructureRepository.save(existing);
	}

	public List<ProjectInsighProjectMappingDTO> searchProjectInsightStructures(String keyword) {
		String lowerKeyword = keyword.toLowerCase();
		List<ProjectInsightStructure> allStructures = projectInsightStructureRepository.findAll();
		List<ProjectInsighProjectMappingDTO> allMappings = projectInsighProjectMappingRepository
				.fetchAllProjectMappings();

		// Map projectInsightId -> mapping DTO
		Map<String, ProjectInsighProjectMappingDTO> mappingByInsightId = allMappings.stream()
				.filter(m -> m.getProjectInsightId() != null)
				.collect(Collectors.toMap(ProjectInsighProjectMappingDTO::getProjectInsightId, m -> m, (a, b) -> a));

		Set<String> addedIds = new HashSet<>();
		List<ProjectInsighProjectMappingDTO> result = new ArrayList<>();

		// 1. Search in MongoDB structures (FormDataDTO)
		for (ProjectInsightStructure structure : allStructures) {
			if (structure.getData() != null && containsKeyword(structure.getData(), lowerKeyword)) {
				ProjectInsighProjectMappingDTO mapping = mappingByInsightId.get(structure.getId());
				if (mapping != null && addedIds.add(mapping.getProjectInsightId())) {
					result.add(mapping);
				}
			} else {
				// 2. If not matched in data, check mapped MySQL fields
				ProjectInsighProjectMappingDTO mapping = mappingByInsightId.get(structure.getId());
				if (mapping != null && mappingContainsKeyword(mapping, lowerKeyword)) {
					if (addedIds.add(mapping.getProjectInsightId())) {
						result.add(mapping);
					}
				}
			}
		}
		return result;
	}

	private boolean containsKeyword(FormDataDTO data, String lowerKeyword) {
		if (data == null)
			return false;

		// 1. Search in fields
		if (data.getFields() != null) {
			for (Object value : data.getFields().values()) {
				if (value instanceof String && ((String) value).toLowerCase().contains(lowerKeyword)) {
					return true;
				}
				if (value instanceof List) {
					for (Object item : (List<?>) value) {
						if (item instanceof String && ((String) item).toLowerCase().contains(lowerKeyword)) {
							return true;
						}
					}
				}
			}
		}

		// 2. Search in questions
		if (data.getQuestions() != null) {
			for (ProjectInsightQuestionDTO question : data.getQuestions()) {
				if (question.getText() != null && question.getText().toLowerCase().contains(lowerKeyword)) {
					return true;
				}
				if (question.getDescription() != null
						&& question.getDescription().toLowerCase().contains(lowerKeyword)) {
					return true;
				}
			}
		}

		// 3. Recursively search in children
		if (data.getChild() != null) {
			for (FormDataDTO child : data.getChild()) {
				if (containsKeyword(child, lowerKeyword)) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean mappingContainsKeyword(ProjectInsighProjectMappingDTO mapping, String lowerKeyword) {
		return Stream.of(
				mapping.getProjectName(),
				mapping.getCreatedByName(),
				mapping.getProjectManagerName(),
				mapping.getClient(),
				mapping.getProjectInsightId())
				.filter(Objects::nonNull)
				.map(String::toLowerCase)
				.anyMatch(s -> s.contains(lowerKeyword));
	}

	private boolean isEmpIdAssigned(FormDataDTO data, String empId) {
		if (data == null)
			return false;

		// Debug print
		System.out.println("Checking FormDataDTO: " + data);

		// 1. Check all fields for keys containing "assignedto" or "assignto"
		if (data.getFields() != null) {
			for (Map.Entry<String, Object> entry : data.getFields().entrySet()) {
				String key = entry.getKey().toLowerCase();
				if (key.contains("assignedto") || key.contains("assignto")) {
					Object assignedTo = entry.getValue();
					if (assignedTo instanceof List) {
						List<?> assignedList = (List<?>) assignedTo;
						for (Object assigned : assignedList) {
							System.out.println("Comparing field assigned: " + assigned + " with empId: " + empId);
							if (empId.toString().equals(String.valueOf(assigned))) {
								return true;
							}
						}
					} else if (assignedTo instanceof String) {
						if (empId.toString().equals(assignedTo)) {
							return true;
						}
					} else if (assignedTo != null) {
						if (empId.toString().equals(String.valueOf(assignedTo))) {
							return true;
						}
					}
				}
			}
		}

		// 2. Check all questions' toAssignEmployeeList
		if (data.getQuestions() != null) {
			for (ProjectInsightQuestionDTO question : data.getQuestions()) {
				List<?> toAssignList = question.getToAssignEmployeeList();
				if (toAssignList != null) {
					for (Object assigned : toAssignList) {
						System.out.println("Comparing question assigned: " + assigned + " with empId: " + empId);
						if (empId.toString().equals(String.valueOf(assigned))) {
							return true;
						}
					}
				}
			}
		}

		// 3. Recursively check all children
		if (data.getChild() != null) {
			for (FormDataDTO child : data.getChild()) {
				System.out.println("Recursing into child: " + child);
				if (isEmpIdAssigned(child, empId)) {
					return true;
				}
			}
		}

		return false;
	}

	public ServiceResponse getProjectInsightByAssignedToEmpId(ProjectInsightDTO projectInsightDTO) {
		try {
			ServiceResponse response = new ServiceResponse();
			Long empId = projectInsightDTO.getEmpId();

			List<ProjectInsightStructure> all = projectInsightStructureRepository.findAll();

			List<ProjectInsightStructure> result = new ArrayList<>();
			for (ProjectInsightStructure pis : all) {
				if (pis.getData() != null && isEmpIdAssigned(pis.getData(), empId.toString())) {
					result.add(pis);
				}
			}

			List<String> insightIds = result.stream()
					.map(pis -> pis.getId().toString())
					.collect(Collectors.toList());

			List<ProjectInsighProjectMappingDTO> dbResponse = projectInsighProjectMappingRepository
					.fetchAllProjectMappingsByProjectInsightId(insightIds);

			Map<String, ProjectInsightStructure> insightMap = result.stream()
					.collect(Collectors.toMap(pis -> pis.getId().toString(), pis -> pis));

			for (ProjectInsighProjectMappingDTO dto : dbResponse) {
				ProjectInsightStructure structure = insightMap.get(dto.getProjectInsightId());
				dto.setProjectInsightStructure(structure);
			}
			response.setServiceResponse(dbResponse);
			return response;

		} catch (Exception e) {
			throw new RuntimeException("Something went wrong. Unable to fetch Projects By Employee!", e);
		}
	}

	public ServiceResponse getReviewersForQuestion(ProjectInsightDTO projectInsightDTO) {
		ServiceResponse response = new ServiceResponse();
		Long reviewerId = null;
		Long employeeId = projectInsightDTO.getEmpId();
		try {
			List<EmployeeTeamMap> employeeTeamList = employeeTeamMapRepository.findByTeamIdAndIsActive(
				employeeId, 1L, projectInsightDTO.getProjectId().intValue()
			);

			if (employeeTeamList == null || employeeTeamList.isEmpty()) {
				Long empId = getEmployeesRMorManagerId(employeeId);
				
				response.setServiceStatus("Success");
				response.setServiceMessage("Reviewer found.");
				Map<String, Object> reviewerInfo = new HashMap<>();
				reviewerInfo.put("reviewerid", empId);
				response.setServiceResponse(reviewerInfo);
				return response;
			}

			Map<Long, String> employeeTeamMapObj = getEmpIdToHighestRoleMap(employeeTeamList);
			String employeeRole = employeeTeamMapObj.getOrDefault(employeeId, null);

			if (employeeRole == null) {
				Long empId = getEmployeesRMorManagerId(employeeId);
				
				response.setServiceStatus("Success");
				response.setServiceMessage("Reviewer found.");
				Map<String, Object> reviewerInfo = new HashMap<>();
				reviewerInfo.put("reviewerid", empId);
				response.setServiceResponse(reviewerInfo);
				return response;
			} 
			
			else if (employeeRole.equalsIgnoreCase("SUPERADMIN") || employeeRole.equalsIgnoreCase("RMG") 
					|| employeeRole.equalsIgnoreCase("HR") || employeeRole.equalsIgnoreCase("HOD")) {
				response.setServiceStatus("Fail");
				response.setServiceMessage("No reviewer for top-level roles.");
				response.setServiceResponse(null);
				return response;
			}

			int roleIndex = ROLE_HIERARCHY.indexOf(employeeRole);
			reviewerId = getNextReviewerId(roleIndex, employeeTeamMapObj);

			if (reviewerId == null) {
				Long empId = getEmployeesRMorManagerId(employeeId);
				
				response.setServiceStatus("Success");
				response.setServiceMessage("Reviewer found.");
				Map<String, Object> reviewerInfo = new HashMap<>();
				reviewerInfo.put("reviewerid", empId);
				response.setServiceResponse(reviewerInfo);
				return response;
			}
			
			response.setServiceStatus("Success");
			response.setServiceMessage("Reviewer found.");
			Map<String, Object> reviewerInfo = new HashMap<>();
			reviewerInfo.put("reviewerid", reviewerId);
			response.setServiceResponse(reviewerInfo);
			return response;

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus("Fail");
			response.setServiceMessage("Error occurred: " + e.getMessage());
			response.setServiceResponse(null);
			return response;
		}
	}

	public ServiceResponse onSaveResponseAsDraft(ProjectInsightStructure userDraft, String empId) {
		ServiceResponse response = new ServiceResponse();
		try {
			Optional<ProjectInsightStructure> fullOptional = projectInsightStructureRepository.findById(userDraft.getId());

			if (fullOptional.isEmpty()) {
				throw new RuntimeException("Structure not found.");
			}

			ProjectInsightStructure full = fullOptional.get();

			// 2. Merge user-specific updates into full structure
//		    ProjectInsightStructure merged = mergeDraftChanges(full, userDraft, empId);

			// 3. Save merged object
//		    projectInsightStructureRepository.save(merged);
			
			response.setServiceStatus(response.STATUS_SUCCESS);
			response.setServiceMessage("Draft Response saved successfully");
			return response;
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus("Fail");
			response.setServiceMessage("Error occurred: " + e.getMessage());
			response.setServiceResponse(null);
			return response;
		}
	}
	

	/*	
	 * MongoDb New Structure Implementation [START]
	 */

	public ServiceResponse saveProjectInsightDetails(ProjectInsightDetailsDTO projectInsightDetailsDTO) {
		ServiceResponse serviceResponse = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setApiUrl("/api/saveProjectInsightDetails");
		try {
			ProjectInsightProjectDetails projectInsightProjectDetails = projectInsightDetailsDTO.getProjectInsightProjectDetails();
			if (projectInsightProjectDetails == null) {
				throw new BadRequestException("Project Insight Details cannot be null.");
			} else if (projectInsightProjectDetails.getProjectId() == null) {
				throw new BadRequestException("Project Id cannot be null.");
			}

			Project project = projectRepository.findByProjectId(projectInsightProjectDetails.getProjectId());
			if (project == null) {
				throw new BadRequestException("Project not Found.");
			}

			boolean isNew = (projectInsightProjectDetails.getId() == null);
			if (isNew) {
				projectInsightProjectDetails.setCreatedBy(projectInsightProjectDetails.getCreatedBy());
				projectInsightProjectDetails.setCreatedOn(getCurrentTimeInString());
			} else {
				Optional<ProjectInsightProjectDetails> existingOpt = projectInsightProjectDetailsRepository.findById(projectInsightProjectDetails.getId());
				if (existingOpt.isPresent()) {
					ProjectInsightProjectDetails existing = existingOpt.get();
					projectInsightProjectDetails.setCreatedBy(existing.getCreatedBy());
					projectInsightProjectDetails.setCreatedOn(existing.getCreatedOn());
				} else {
					projectInsightProjectDetails.setCreatedBy(projectInsightProjectDetails.getCreatedBy());
					projectInsightProjectDetails
							.setCreatedOn(getCurrentTimeInString());
				}
				projectInsightProjectDetails.setUpdatedBy(projectInsightProjectDetails.getUpdatedBy());
				projectInsightProjectDetails.setUpdatedOn(getCurrentTimeInString());
			}

			ProjectInsightProjectDetails dbResponse = projectInsightProjectDetailsRepository.save(projectInsightProjectDetails);
			if (dbResponse == null) {
				throw new BadRequestException("Unable to save Project Insight Details.");
			}
			saveProjectInsightDetailsMappingInfo(dbResponse);
			saveProjectInsightFormDetails(projectInsightDetailsDTO.getProjectInsightFormDetails(), dbResponse.getId(),
					"Project");

			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			serviceResponse.setServiceResponse(dbResponse);
		} catch (BadRequestException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceMessage("Something went wrong.");
			serviceResponse.setServiceResponse("Something went wrong.");
		}
		return serviceResponse;
	}

	public ServiceResponse saveProjectInsightGroupDetails(ProjectInsightDetailsDTO projectInsightDetailsDTO) {
		ServiceResponse serviceResponse = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setApiUrl("/api/saveProjectInsightGroupDetails");
		try {
			ProjectInsightGroupDetails projectInsightGroupDetails = projectInsightDetailsDTO.getProjectInsightGroupDetails();
			if (projectInsightGroupDetails == null) {
				throw new BadRequestException("Project Insight Group Details cannot be null.");
			} else if (projectInsightGroupDetails.getParentId() == null
					|| projectInsightGroupDetails.getParentType() == null) {
				throw new BadRequestException("Project Insight Group Details ParentId or ParentType cannot be null.");
			}

			boolean parentExists = checkIfParentExists(projectInsightGroupDetails.getParentId(),
					projectInsightGroupDetails.getParentType(), false);

			if (!parentExists) {
				throw new BadRequestException("Project Insight Group Details Parent Not Found.");
			}

			if (projectInsightGroupDetails.getParentType().equals("Project")) {
				if(!projectInsightGroupDetails.getParentPathIds().contains(projectInsightGroupDetails.getId())) {
					projectInsightGroupDetails.getParentPathIds().add(projectInsightGroupDetails.getId());
				}
				if(!projectInsightGroupDetails.getParentPathIds().contains(projectInsightGroupDetails.getParentId())){
					projectInsightGroupDetails.getParentPathIds().add(projectInsightGroupDetails.getParentId());
				}
			} else {
				Optional<ProjectInsightGroupDetails>  groupOpt = projectInsightGroupDetailsRepository.findById(projectInsightGroupDetails.getParentId());
				ProjectInsightGroupDetails group = groupOpt.get();
				List<String> parentPathIds = new ArrayList<>(group.getParentPathIds());
				if(!parentPathIds.contains(projectInsightGroupDetails.getId())) {
					parentPathIds.add(projectInsightGroupDetails.getId());
				}
				if(!parentPathIds.contains(projectInsightGroupDetails.getParentId())){
					parentPathIds.add(projectInsightGroupDetails.getParentId());
				}
				projectInsightGroupDetails.setParentPathIds(parentPathIds);
			}

			boolean isNew = (projectInsightGroupDetails.getId() == null);

			if (isNew) {
				projectInsightGroupDetails.setCreatedBy(projectInsightGroupDetails.getCreatedBy());
				projectInsightGroupDetails.setCreatedOn(getCurrentTimeInString());
			} else {
				Optional<ProjectInsightGroupDetails> existingOpt = projectInsightGroupDetailsRepository.findById(projectInsightGroupDetails.getId());
				if (existingOpt.isPresent()) {
					ProjectInsightGroupDetails existing = existingOpt.get();
					projectInsightGroupDetails.setCreatedBy(existing.getCreatedBy());
					projectInsightGroupDetails.setCreatedOn(existing.getCreatedOn());
				} else {
					projectInsightGroupDetails.setCreatedBy(projectInsightGroupDetails.getCreatedBy());
					projectInsightGroupDetails
							.setCreatedOn(getCurrentTimeInString());
				}
				projectInsightGroupDetails.setUpdatedBy(projectInsightGroupDetails.getUpdatedBy());
				projectInsightGroupDetails.setUpdatedOn(getCurrentTimeInString());
			}

			ProjectInsightGroupDetails dbResponse = projectInsightGroupDetailsRepository.save(projectInsightGroupDetails);
			if (dbResponse == null) {
				throw new BadRequestException("Unable to save Project Insight Group Details.");
			}
			saveProjectInsightFormDetails(projectInsightDetailsDTO.getProjectInsightFormDetails(), dbResponse.getId(),
					"Group");

			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			serviceResponse.setServiceResponse(dbResponse);
		} catch (BadRequestException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceMessage("Something went wrong.");
			serviceResponse.setServiceResponse("Something went wrong.");
		}
		return serviceResponse;
	}

	public ServiceResponse saveProjectInsightQuestionDetails(ProjectInsightQuestionDetails projectInsightQuestionDetails) {
		ServiceResponse serviceResponse = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setApiUrl("/api/saveProjectInsightQuestionDetails");
		try {
			if (projectInsightQuestionDetails == null) {
				throw new BadRequestException("Project Insight Question Details cannot be null.");
			} else if (projectInsightQuestionDetails.getParentId() == null
					|| projectInsightQuestionDetails.getParentType() == null) {
				throw new BadRequestException(
						"Project Insight Question Details ParentId or ParentType cannot be null.");
			}

			boolean parentExists = checkIfParentExists(projectInsightQuestionDetails.getParentId(),
					projectInsightQuestionDetails.getParentType(), false);

			if (!parentExists) {
				throw new BadRequestException("Project Insight Question Details Parent Not Found.");
			}

			if (projectInsightQuestionDetails.getParentType().equals("Project")) {
				// projectInsightQuestionDetails.setParentPathIds(Arrays.asList(projectInsightQuestionDetails.getParentId()));
				if(!projectInsightQuestionDetails.getParentPathIds().contains(projectInsightQuestionDetails.getParentId())) {
					projectInsightQuestionDetails.getParentPathIds().add(projectInsightQuestionDetails.getParentId());
				}
			} else {
//				Optional<ProjectInsightQuestionDetails> questionOpt = projectInsightQuestionDetailsRepository.findById(projectInsightQuestionDetails.getParentId());
//				ProjectInsightQuestionDetails question = questionOpt.get();
//				List<String> parentPathIds = new ArrayList<>(question.getParentPathIds());
//				parentPathIds.add(question.getId());
				Optional<ProjectInsightGroupDetails> groupOpt = projectInsightGroupDetailsRepository.findById(projectInsightQuestionDetails.getParentId());
				ProjectInsightGroupDetails group = groupOpt.get();
				List<String> parentPathIds = new ArrayList<>(group.getParentPathIds());
				parentPathIds.add(group.getId());
				projectInsightQuestionDetails.setParentPathIds(parentPathIds);
			}

			boolean isNew = (projectInsightQuestionDetails.getId() == null);

			if (isNew) {
				projectInsightQuestionDetails.setCreatedBy(projectInsightQuestionDetails.getCreatedBy());
				projectInsightQuestionDetails.setCreatedOn(getCurrentTimeInString());
			} else {
				Optional<ProjectInsightQuestionDetails> existingOpt = projectInsightQuestionDetailsRepository.findById(projectInsightQuestionDetails.getId());
				if (existingOpt.isPresent()) {
					ProjectInsightQuestionDetails existing = existingOpt.get();
					projectInsightQuestionDetails.setCreatedBy(existing.getCreatedBy());
					projectInsightQuestionDetails.setCreatedOn(existing.getCreatedOn());
				} else {
					projectInsightQuestionDetails.setCreatedBy(projectInsightQuestionDetails.getCreatedBy());
					projectInsightQuestionDetails
							.setCreatedOn(getCurrentTimeInString());
				}
				projectInsightQuestionDetails.setUpdatedBy(projectInsightQuestionDetails.getUpdatedBy());
				projectInsightQuestionDetails.setUpdatedOn(getCurrentTimeInString());
			}

			ProjectInsightQuestionDetails dbResponse = projectInsightQuestionDetailsRepository.save(projectInsightQuestionDetails);
			if (dbResponse == null) {
				throw new BadRequestException("Unable to save Project Insight Question Details.");
			}

			// Add to Question Library 
			if(projectInsightQuestionDetails.isAddToQuestionBank() && !projectInsightQuestionDetails.isQuestionUpdate()){
				dbResponse.setDeptIds(projectInsightQuestionDetails.getDeptIds());
				projectInsightQuestionLibraryService.addQuestionToLibrary(dbResponse);
			}

			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			serviceResponse.setServiceResponse(dbResponse);
		} catch (BadRequestException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceMessage("Something went wrong.");
			serviceResponse.setServiceResponse("Something went wrong.");
		}
		return serviceResponse;
	}

	private ProjectInsightFormDetails saveProjectInsightFormDetails(ProjectInsightFormDetails projectInsightFormDetails, String parentId, String parentType) {
		try {

			boolean parentExists = checkIfParentExists(parentId, parentType, true);

			if (!parentExists) {
				throw new BadRequestException("Project Insight Form Details Parent Not Found.");
			}

			boolean isNew = (projectInsightFormDetails.getId() == null);

			if (isNew) {
				projectInsightFormDetails.setCreatedBy(projectInsightFormDetails.getCreatedBy());
				projectInsightFormDetails.setCreatedOn(getCurrentTimeInString());
			} else {
				Optional<ProjectInsightFormDetails> existingOpt = projectInsightFormDetailsRepository.findById(projectInsightFormDetails.getId());
				if (existingOpt.isPresent()) {
					ProjectInsightFormDetails existing = existingOpt.get();
					projectInsightFormDetails.setCreatedBy(existing.getCreatedBy());
					projectInsightFormDetails.setCreatedOn(existing.getCreatedOn());
				} else {
					projectInsightFormDetails.setCreatedBy(projectInsightFormDetails.getCreatedBy());
					projectInsightFormDetails.setCreatedOn(getCurrentTimeInString());
				}
				projectInsightFormDetails.setUpdatedBy(projectInsightFormDetails.getUpdatedBy());
				projectInsightFormDetails.setUpdatedOn(getCurrentTimeInString());
			}
			projectInsightFormDetails.setParentId(parentId);
			projectInsightFormDetails.setParentType(parentType);

			ProjectInsightFormDetails dbResponse = projectInsightFormDetailsRepository.save(projectInsightFormDetails);
			if (dbResponse == null) {
				throw new BadRequestException("Unable to save Project Insight Form Details.");
			}
			return dbResponse;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/*
	 * create get apis for projects,groups,questions along with their structure.
	 */

	public ProjectInsightDetailsDTO getProjectInsightDetailsByObjectId(String id) {
		ProjectInsightDetailsDTO projectInsightDetailsDTO = new ProjectInsightDetailsDTO();
		try {
			ProjectInsightProjectDetails existing = projectInsightProjectDetailsRepository.findById(id).orElseThrow(() -> new BadRequestException("Project Insight Details not found with id: " + id));
			ProjectInsightFormDetails existingForm = projectInsightFormDetailsRepository.findFormDetailsByParentIdAndParentType(id,"Project").orElseThrow(() -> new BadRequestException("Project Insight Form Details not found with id: " + id));
			projectInsightDetailsDTO.setProjectInsightProjectDetails(existing);
			projectInsightDetailsDTO.setProjectInsightFormDetails(existingForm);
		} catch (BadRequestException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Something went wrong !!", e);
		}
		return projectInsightDetailsDTO;
	}

	public ProjectInsightDetailsDTO getProjectInsightGroupDetailsByObjectId(String id) {
		ProjectInsightDetailsDTO projectInsightDetailsDTO = new ProjectInsightDetailsDTO();
		try {
			ProjectInsightGroupDetails existing = projectInsightGroupDetailsRepository.findById(id).orElseThrow(() -> new BadRequestException("Project Insight Group Details not found with id: " + id));
			ProjectInsightFormDetails existingForm = projectInsightFormDetailsRepository.findByParentIdAndParentType(id,"Group");
			if(existing.getParentPathIds() != null && !existing.getParentPathIds().isEmpty()){
				Optional<ProjectInsightProjectDetails> existingOpt = projectInsightProjectDetailsRepository.findById(existing.getParentPathIds().get(0));
				if(existingOpt.isPresent()){
					existing.setProjectDetailsId(existingOpt.get().getId());
					existing.setProjectId(existingOpt.get().getProjectId());
					existing.setProjectName(existingOpt.get().getProjectName());
				}
			}
			projectInsightDetailsDTO.setProjectInsightGroupDetails(existing);
			projectInsightDetailsDTO.setProjectInsightFormDetails(existingForm);
		} catch (BadRequestException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Something went wrong !!", e);
		}
		return projectInsightDetailsDTO;
	}

	public ServiceResponse getProjectInsightQuestionDetailsByObjectId(String id) {
		ServiceResponse serviceResponse = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setApiUrl("/api/getProjectInsightQuestionDetailsByParentIdAndParentType");
		try {
			Optional<ProjectInsightQuestionDetails> questionDetails = projectInsightQuestionDetailsRepository.findById(id);
			if (questionDetails.isPresent()) {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				serviceResponse.setServiceResponse(questionDetails.get());
			} else {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("Project Insight Question Details Not Found.");
			}
			return serviceResponse;
		} catch (BadRequestException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceResponse("Something went wrong !!");
			return serviceResponse;
		}
	}

	public ServiceResponse getProjectInsightQuestionDetailsByParentIdAndParentType(String parentId, String parentType) {
		ServiceResponse serviceResponse = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setApiUrl("/api/getProjectInsightQuestionDetailsByParentIdAndParentType");
		try {
			List<ProjectInsightQuestionDetails> questionDetailsList = projectInsightQuestionDetailsRepository.findByParentIdAndParentType(parentId, parentType);
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			serviceResponse.setServiceResponse(questionDetailsList);
			return serviceResponse;
		} catch (BadRequestException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Something went wrong !!", e);
		}
	}
	
	public ServiceResponse getProjectInsightGroupDetailsByParentIdAndParentType(String parentId, String parentType) {
		ServiceResponse serviceResponse = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setApiUrl("/api/getProjectInsightGroupDetailsByParentIdAndParentType");
		try {
			List<ProjectInsightGroupDetails> groupDetailsList = projectInsightGroupDetailsRepository.findByParentIdAndParentType(parentId, parentType);
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			serviceResponse.setServiceResponse(groupDetailsList);
			return serviceResponse;
		} catch (BadRequestException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Something went wrong !!", e);
		}
	}
	

	public ServiceResponse deleteProjectInsightQuestionDetails(ProjectInsightQuestionDetails projectInsightQuestionDetails) {
		ServiceResponse serviceResponse = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setApiUrl("/api/deleteProjectInsightQuestionDetails");
		try {
			if(projectInsightQuestionDetails == null || projectInsightQuestionDetails.getId() == null){
				throw new BadRequestException("Project Insight Question Details Id cannot be null.");
			}
			projectInsightQuestionDetailsRepository.deleteById(projectInsightQuestionDetails.getId());
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			return serviceResponse;
		} catch (BadRequestException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Something went wrong, unable to delete Project Insight Question Details !!", e);
		}
	}

	// helper methods

	private void saveProjectInsightDetailsMappingInfo(ProjectInsightProjectDetails projectInsightProjectDetails) {
		ProjectInsighProjectMapping mappingResponse = projectInsighProjectMappingRepository.findByProjectId(projectInsightProjectDetails.getProjectId());
		if (mappingResponse != null) {
			mappingResponse.setProjectInsightDetailsId(projectInsightProjectDetails.getId());
			projectInsighProjectMappingRepository.save(mappingResponse);
		} else {
			ProjectInsighProjectMapping newObj = new ProjectInsighProjectMapping();
			newObj.setProjectId(projectInsightProjectDetails.getProjectId());
			newObj.setProjectInsightDetailsId(projectInsightProjectDetails.getId());
			newObj.setIsDraft(projectInsightProjectDetails.getIsDraft());
			newObj.setCreatedBy(Long.parseLong(projectInsightProjectDetails.getCreatedBy()));
			projectInsighProjectMappingRepository.save(newObj);
		}
	}

	private boolean checkIfParentExists(String parentId, String parentType, boolean checkForQuestion) {
		boolean isParentExists = false;
		if (parentId == null || parentType == null) {
			return isParentExists;
		}
		if (parentType.equals("Project")) {
			isParentExists = projectInsightProjectDetailsRepository.existsById(parentId);
		} else {
			isParentExists = projectInsightGroupDetailsRepository.existsById(parentId);
			if (!isParentExists && checkForQuestion) {
				isParentExists = projectInsightQuestionDetailsRepository.existsByParentIdAndParentType(parentId, parentType);
			}
		}
		return isParentExists;
	}

	public ServiceResponse saveProjectInsightStaticGroupDetails(ProjectInsightGroupDetails projectInsightGroupDetails) {
		ServiceResponse serviceResponse = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setApiUrl("/api/saveProjectInsightStaticGroupDetails");
		try {
			if (projectInsightGroupDetails == null) {
				throw new BadRequestException("Project Insight Group Details cannot be null.");
			} else if (projectInsightGroupDetails.getParentId() == null
					|| projectInsightGroupDetails.getParentType() == null) {
				throw new BadRequestException("Project Insight Group Details ParentId or ParentType cannot be null.");
			}

			boolean parentExists = checkIfParentExists(projectInsightGroupDetails.getParentId(),
					projectInsightGroupDetails.getParentType(), false);

			if (!parentExists) {
				throw new BadRequestException("Project Insight Group Details Parent Not Found.");
			}

			boolean isNew = (projectInsightGroupDetails.getId() == null);
			if (projectInsightGroupDetails.getParentType().equals("Project")) {
				projectInsightGroupDetails.setParentPathIds(Arrays.asList(projectInsightGroupDetails.getParentId()));
			} else {
				Optional<ProjectInsightGroupDetails>  groupOpt = projectInsightGroupDetailsRepository.findById(projectInsightGroupDetails.getParentId());
				ProjectInsightGroupDetails group = groupOpt.get();
				List<String> parentPathIds = new ArrayList<>(group.getParentPathIds());
				parentPathIds.add(group.getId());
				projectInsightGroupDetails.setParentPathIds(parentPathIds);
			}
			if (isNew) {
				if(projectInsightGroupDetailsRepository.existsByGroupTitleAndParentIdAndParentType(projectInsightGroupDetails.getGroupTitle(),projectInsightGroupDetails.getParentId(),projectInsightGroupDetails.getParentType())){
				// if(projectInsightGroupDetailsRepository.existsByGroupTitleAndParentIdAndParentType(projectInsightGroupDetails.getGroupTitle().trim().toLowerCase())){
					throw new BadRequestException("Group title must be unique within the parent group.");
				}
				projectInsightGroupDetails.setCreatedBy(projectInsightGroupDetails.getCreatedBy());
				projectInsightGroupDetails.setCreatedOn(getCurrentTimeInString());
			} else {
				Optional<ProjectInsightGroupDetails> existingOpt = projectInsightGroupDetailsRepository.findById(projectInsightGroupDetails.getId());
				if (existingOpt.isPresent()) {
					ProjectInsightGroupDetails existing = existingOpt.get();
					projectInsightGroupDetails.setCreatedBy(existing.getCreatedBy());
					projectInsightGroupDetails.setCreatedOn(existing.getCreatedOn());
				} else {
					projectInsightGroupDetails.setCreatedBy(projectInsightGroupDetails.getCreatedBy());
					projectInsightGroupDetails
							.setCreatedOn(getCurrentTimeInString());
				}
				projectInsightGroupDetails.setUpdatedBy(projectInsightGroupDetails.getUpdatedBy());
				projectInsightGroupDetails.setUpdatedOn(getCurrentTimeInString());
			}

			ProjectInsightGroupDetails dbResponse = projectInsightGroupDetailsRepository.save(projectInsightGroupDetails);
			if (dbResponse == null) {
				throw new BadRequestException("Unable to save Project Insight Group Details.");
			}

			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			serviceResponse.setServiceResponse(dbResponse);
		} catch (BadRequestException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceMessage("Something went wrong.");
			serviceResponse.setServiceResponse("Something went wrong.");
		}
		return serviceResponse;
	}

	public ServiceResponse getAllProjectInsightGroupsByParentId(String parentId, String parentType) {
		ServiceResponse serviceResponse = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setApiUrl("/api/getAllProjectInsightGroupsByParentId");
		try {
			List<ProjectInsightGroupDetails> groupDetailsList = projectInsightGroupDetailsRepository.getAllProjectInsightGroupsByParentId(parentId, parentType);
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			serviceResponse.setServiceResponse(groupDetailsList);
			return serviceResponse;
		} catch (BadRequestException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Something went wrong !!", e);
		}
	}

	public ServiceResponse getProjectInsightDetailsForExcelDownload(String projectInsightDetailsId){
		ServiceResponse serviceResponse = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setApiUrl("/api/getProjectInsightDetailsForExcelDownload");
		try {
			ProjectInsightProjectDetails existingProjectInsightProjectDetails = projectInsightProjectDetailsRepository.findById(projectInsightDetailsId).orElseThrow(() -> new BadRequestException("Project Insight Details not found with id: " + projectInsightDetailsId));
			List<ProjectInsightDetailsExcelDTO> projectInsightDetailsExcelDTOList = new ArrayList<>();
			projectInsightDetailsExcelDTOList.addAll(mapProjectInsightDetailsToProjectInsightDetailsExcelDTO(existingProjectInsightProjectDetails));
			projectInsightDetailsExcelDTOList.addAll(getProjectInsightGroupDetailsForExcelDownload(projectInsightDetailsId,"Project","Group"));
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			serviceResponse.setServiceResponse(projectInsightDetailsExcelDTOList);
		} catch (BadRequestException e) {
			e.printStackTrace();
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceResponse(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceResponse("Something went wrong !!");
		}
		return serviceResponse;
	}
	
	private List<ProjectInsightDetailsExcelDTO> mapProjectInsightDetailsToProjectInsightDetailsExcelDTO(ProjectInsightProjectDetails projectInsightProjectDetails) {
		try {
			if (projectInsightProjectDetails == null) {
				throw new BadRequestException("Project Insight Details cannot be null.");
			}
			List<ProjectInsightDetailsExcelDTO> projectInsightDetailsExcelDTOList = new ArrayList<>();
			projectInsightDetailsExcelDTOList.add(new ProjectInsightDetailsExcelDTO("Project", "Project Name", "text", (Object) null, (Object) projectInsightProjectDetails.getProjectName(), true));
			
			Optional<ProjectInsightFormDetails> existingFormDetails = projectInsightFormDetailsRepository.findFormDetailsByParentIdAndParentType(projectInsightProjectDetails.getId(), "Project");
			if(existingFormDetails.isPresent()){
				List<FormFieldDTO> formFieldList = existingFormDetails.get().getFields();
				Map<String, Object> additionalInfo = projectInsightProjectDetails.getAdditionalInfo();
				projectInsightDetailsExcelDTOList.addAll(getExcelDetailsListFromAdditionalInfo("Project",formFieldList, additionalInfo));
			}
			
			List<ProjectInsightQuestionDetails> questionDetailsList = projectInsightQuestionDetailsRepository.findByParentIdAndParentType(projectInsightProjectDetails.getId(), "Project");
			projectInsightDetailsExcelDTOList.addAll(getExcelDetailsListFromQuestionsList("Project-|-Question",questionDetailsList));
			
			return projectInsightDetailsExcelDTOList;
		} catch (BadRequestException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public List<ProjectInsightDetailsExcelDTO> getProjectInsightGroupDetailsForExcelDownload(String parentId, String parentType, String sectionGroupName) {
		try {
			List<ProjectInsightDetailsExcelDTO> projectInsightDetailsExcelDTOList = new ArrayList<>();
			List<ProjectInsightGroupDetails> groupDetailsList = projectInsightGroupDetailsRepository.findByParentIdAndParentType(parentId, parentType);
			if (ValidationUtility.isListNotNullOrEmpty(groupDetailsList)) {
				for (int i = 0; i < groupDetailsList.size(); i++) {
					String tempSectionGroupName = sectionGroupName + "-" + (i + 1);
					ProjectInsightGroupDetails groupDetails = groupDetailsList.get(i);
					projectInsightDetailsExcelDTOList.add(new ProjectInsightDetailsExcelDTO(tempSectionGroupName, "GroupTitle", "text", (Object) null, (Object) groupDetails.getGroupTitle(), true));
					projectInsightDetailsExcelDTOList.add(new ProjectInsightDetailsExcelDTO(tempSectionGroupName, "GroupType", "select", (Object) null, (Object) groupDetails.getGroupType(), true));

					Optional<ProjectInsightFormDetails> existingFormDetails = projectInsightFormDetailsRepository.findFormDetailsByParentIdAndParentType(groupDetails.getId(), "Group");
					if (existingFormDetails.isPresent()) {
						List<FormFieldDTO> formFieldList = existingFormDetails.get().getFields();
						Map<String, Object> additionalInfo = groupDetails.getAdditionalInfo();
						projectInsightDetailsExcelDTOList.addAll(getExcelDetailsListFromAdditionalInfo(tempSectionGroupName, formFieldList, additionalInfo));
					}

					List<ProjectInsightQuestionDetails> questionDetailsList = projectInsightQuestionDetailsRepository.findByParentIdAndParentType(groupDetails.getId(), "Group");
					projectInsightDetailsExcelDTOList.addAll(getExcelDetailsListFromQuestionsList(tempSectionGroupName+"-|-Question", questionDetailsList));

					projectInsightDetailsExcelDTOList.addAll(getProjectInsightGroupDetailsForExcelDownload(groupDetails.getId(), "Group",tempSectionGroupName));
				}
			}
			return projectInsightDetailsExcelDTOList;
		} catch (BadRequestException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private List<ProjectInsightDetailsExcelDTO> getExcelDetailsListFromAdditionalInfo(String section, List<FormFieldDTO> formFieldList, Map<String, Object> additionalInfo) {
		try {
			List<ProjectInsightDetailsExcelDTO> projectInsightDetailsExcelDTOList = new ArrayList<>();
			if (additionalInfo != null && !additionalInfo.isEmpty()) {
				for (Map.Entry<String, Object> entry : additionalInfo.entrySet()) {
					FormFieldDTO formFieldDTO = formFieldList.stream()
							.filter(field -> entry.getKey().equals(field.getName())).findFirst().orElse(null);
					if (formFieldDTO != null) {
						if (formFieldDTO.getOptionSource().equalsIgnoreCase("api")
								&& !formFieldDTO.getLabel().toLowerCase().contains("domain")) {
							continue;
						}
						if(formFieldDTO.getName().toLowerCase().contains("domainname")){
							formFieldDTO.setLabel("Industry Domain");
						}
						ProjectInsightDetailsExcelDTO projectInsightDetailsExcelDTO = new ProjectInsightDetailsExcelDTO();
						projectInsightDetailsExcelDTO.setSection(section);
						projectInsightDetailsExcelDTO.setTitle(formFieldDTO.getLabel());
						projectInsightDetailsExcelDTO.setMultiSelect(formFieldDTO.isMultiple());
						projectInsightDetailsExcelDTO.setOptionType(formFieldDTO.getType());
						projectInsightDetailsExcelDTO.setOption(transformOptionBasedOnOptionType(formFieldDTO.getType(), formFieldDTO.getOptions(), false));
						projectInsightDetailsExcelDTO.setRequired(formFieldDTO.isRequired());
						projectInsightDetailsExcelDTO.setFieldWidth(formFieldDTO.getWidth());
						projectInsightDetailsExcelDTO.setValue(transformValueBasedOnOptionType(formFieldDTO.getType(), entry.getValue(), formFieldDTO));
						if (ValidationUtility.isStringNotNullOrEmpty(formFieldDTO.getType())
								&& formFieldDTO.getType().toLowerCase().equals("table")) {
							projectInsightDetailsExcelDTO.setTableConfig(formFieldDTO.getTableConfig());
						}
						projectInsightDetailsExcelDTOList.add(projectInsightDetailsExcelDTO);
					}
				}
			}
			return projectInsightDetailsExcelDTOList;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private List<ProjectInsightDetailsExcelDTO> getExcelDetailsListFromQuestionsList(String section, List<ProjectInsightQuestionDetails> questionDetailsList) {
		try {
			List<ProjectInsightDetailsExcelDTO> projectInsightDetailsExcelDTOList = new ArrayList<>();
			if (!ValidationUtility.isListNotNullOrEmpty(questionDetailsList)) {
				return projectInsightDetailsExcelDTOList;
			}
			for (ProjectInsightQuestionDetails projectInsightQuestionDetails : questionDetailsList) {
				ProjectInsightDetailsExcelDTO projectInsightDetailsExcelDTO = new ProjectInsightDetailsExcelDTO();
				projectInsightDetailsExcelDTO.setSection(section);
				projectInsightDetailsExcelDTO.setTitle(projectInsightQuestionDetails.getQuestion());
				projectInsightDetailsExcelDTO.setOptionType(projectInsightQuestionDetails.getOptionType());
				projectInsightDetailsExcelDTO.setOption(transformOptionBasedOnOptionType(projectInsightQuestionDetails.getOptionType(), projectInsightQuestionDetails.getOptionsList(), true));
				projectInsightDetailsExcelDTOList.add(projectInsightDetailsExcelDTO);
			}

			return projectInsightDetailsExcelDTOList;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private Object transformValueBasedOnOptionType(String optionType, Object value, FormFieldDTO formFieldDTO) {
		try {
			Object tempValue = value;
			if (!ValidationUtility.isStringNotNullOrEmpty(optionType) || value == null || formFieldDTO == null) {
				return tempValue;
			}
			Set<String> excludedTypes = Set.of("select", "checkbox");
			if (!excludedTypes.contains(optionType.toLowerCase())) {
				return tempValue;
			}
			if (value instanceof List<?>) {
				List<?> list = (List<?>) value;
				if(!ValidationUtility.isListNotNullOrEmpty(list)){
					return null;
				}
				if (list.get(0) instanceof LinkedHashMap) {
					String apiLabelKey = formFieldDTO.getApiLabelKey();
					
					List<Map<String, Object>> linkedHashmapList = (List<Map<String, Object>>) list;

					List<Object> collectedValues = new ArrayList<>();
					for (Map<String, Object> map : linkedHashmapList) {
						Object valueNew = map.get(apiLabelKey);
						if (valueNew == null
								&& ValidationUtility.isStringNotNullOrEmpty(formFieldDTO.getDependentLabelKey())) {
							valueNew = map.get(formFieldDTO.getDependentLabelKey());
						}
						if (valueNew != null) {
							collectedValues.add(valueNew);
						}
					}
					tempValue = collectedValues;
				} 
			}
			return tempValue;
		} catch (Exception e) {
			e.printStackTrace();
			return value;
		}
	}

	@SuppressWarnings("unchecked")
	private Object transformOptionBasedOnOptionType(String optionType, Object option, boolean isOptionValue) {
		try {
			if (!ValidationUtility.isStringNotNullOrEmpty(optionType) || option == null) {
				return option;
			}
			Set<String> excludedTypes = Set.of("select", "radio", "checkbox");
			if (!excludedTypes.contains(optionType.toLowerCase())) {
				return option;
			}

			if (option instanceof List<?>) {
				if (!ValidationUtility.isListNotNullOrEmpty((List<?>) option)) {
					return option;
				}
				if (isOptionValue) {
					List<OptionValueDTO> optionsTemp = (List<OptionValueDTO>) option;
					return optionsTemp.stream()
							.map(o -> o.getOptionValue())
							.collect(Collectors.toList());
				} else {
					List<OptionDTO> optionsTemp = (List<OptionDTO>) option;
					return optionsTemp.stream()
							.map(o -> o.getValue())
							.collect(Collectors.toList());
				}
			} else {
				return option;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public ServiceResponse saveProjectInsightDetailsFromExcel(ProjectSectionData projectSectionData) {
		ServiceResponse serviceResponse = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		apiLogInfo.setApiUrl("/api/saveProjectInsightDetailsFromExcel");
		try {
			if (projectSectionData == null) {
				throw new BadRequestException("Project Insight Details Excel Data cannot be null.");
			}
			if (projectSectionData.getProjectInsightProjectDetails() == null) {
				throw new BadRequestException("Project Insight Details cannot be null.");
			}
			String projectName = projectSectionData.getProjectInsightProjectDetails().getProjectName();
			if (projectName == null) {
				throw new BadRequestException("Project Name cannot be null.");
			}
			Project project = projectRepository.findByProjectName(projectName);
			if (project == null) {
				throw new BadRequestException("Project not Found.");
			}
			if (projectSectionData.getProjectInsightProjectDetails().getIndustryDomain() == null || projectSectionData.getProjectInsightProjectDetails().getIndustryDomain().isEmpty()) {
				throw new BadRequestException("Industry Domain Name cannot be null.");
			}

			ProjectInsighProjectMapping mappingResponse = projectInsighProjectMappingRepository.findByProjectId(project.getProjectId());
			if (mappingResponse != null) {
				throw new BadRequestException("Project Insight is already created for the Entered Project Name.");
			}

			ProjectInsightProjectDetails projectInsightProjectDetails = saveExcelProjectInsightDetails(
					projectSectionData.getProjectInsightProjectDetails(), project, projectSectionData.getCreatedBy(),projectSectionData.getProjectInsightProjectDetails().getIndustryDomain());
			saveProjectInsightDetailsMappingInfo(projectInsightProjectDetails);

			ProjectInsightFormDetails projectInsightFormDetails = saveExcelProjectInsightFormDetails(
					projectSectionData.getFields(), projectInsightProjectDetails.getId(), "Project",
					projectSectionData.getCreatedBy());
			List<String> parentPathIds = new ArrayList<>();
			parentPathIds.add(projectInsightProjectDetails.getId());
			saveExcelProjectInsightQuestionDetails(projectSectionData.getQuestions(), projectInsightProjectDetails.getId(), "Project",projectSectionData.getCreatedBy(),parentPathIds);
			
			saveExcelProjectInsightGroupDetails(
					projectSectionData.getGroups(), projectInsightProjectDetails.getId(), "Project",
					projectSectionData.getCreatedBy(),parentPathIds);

			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			serviceResponse.setServiceResponse("Project Insight Details Created Successfully.");
		} catch (BadRequestException e) {
			e.printStackTrace();
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceMessage(e.getMessage());
			serviceResponse.setServiceResponse(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceMessage("Something went wrong.");
			serviceResponse.setServiceResponse("Something went wrong.");
		}
		return serviceResponse;
	}

	private ProjectInsightProjectDetails saveExcelProjectInsightDetails(
			ProjectInsightProjectDetails projectInsightProjectDetails, Project project, String createdBy, List<String> industryDomains) {
		try {
			projectInsightProjectDetails.setProjectId(project.getProjectId());
			ProjectDTO projectDTO = projectRepository.getAllProjectNameAndProjectManagerIdByProjectId(project.getProjectId());
			if (projectDTO != null) {
				// p.projectId,p.projectName,p.projectManagerId,e.name,p.apmosysRM,p.clientRM,c.clientId,S
				// c.clientName
				projectInsightProjectDetails.setProjectName(projectDTO.getProjectName());
				projectInsightProjectDetails.setProjectManagerId(projectDTO.getProjectManagerId());
				projectInsightProjectDetails.setProjectManagerName(projectDTO.getProjectManagerName());
				projectInsightProjectDetails.setApmosysRM(projectDTO.getApmosysRM());
				projectInsightProjectDetails.setClientRM(projectDTO.getClientRM());
				ClientDTO clientDTO = new ClientDTO(projectDTO.getClientId(), projectDTO.getClientName());
				projectInsightProjectDetails.setClient(clientDTO);
			}

			String departmentIds = project.getDeptId() != null ? project.getDeptId() : "";
			List<Long> departmentIdList = Arrays.stream(departmentIds.split(",")).map(String::trim)
					.filter(s -> !s.isEmpty()).map(Long::parseLong).collect(Collectors.toList());
			List<Department> departments = departmentRepository.findByDeptIdIn(departmentIdList);

			List<com.apmosys.employeeportal.mongodb.dto.DepartmentDTO> deptList = new ArrayList<>();
			if (departments != null && !departments.isEmpty()) {
				for (Department department : departments) {
					com.apmosys.employeeportal.mongodb.dto.DepartmentDTO departmentDTO = new com.apmosys.employeeportal.mongodb.dto.DepartmentDTO(
							department.getDeptId(), department.getName());
					deptList.add(departmentDTO);
				}
			}

			if (industryDomains != null && !industryDomains.isEmpty()) {
				List<Long> domainIds = new ArrayList<>();
				for (String domain : industryDomains) {
					ProjectInsightDomainData projectInsightDomainData = projectInsightDomainDataRepository
							.findByDomainName(domain)
							.orElseThrow(() -> new BadRequestException("Provided Domain Not Found : " + domain));
					domainIds.add(projectInsightDomainData.getId());
				}
				Map<String, Object> tempMap = projectInsightProjectDetails.getAdditionalInfo();
				tempMap.put("domainname", domainIds);
			}
			projectInsightProjectDetails.setDepartments(deptList);
			projectInsightProjectDetails.setCreatedBy(createdBy);
			projectInsightProjectDetails.setCreatedOn(getCurrentTimeInString());
			ProjectInsightProjectDetails dbResponse = projectInsightProjectDetailsRepository.save(projectInsightProjectDetails);
			if (dbResponse == null) {
				throw new BadRequestException("Unable to save Project Insight Details.");
			}
			return dbResponse;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private ProjectInsightFormDetails saveExcelProjectInsightFormDetails(List<FormFieldDTO> fields, String parentId,
			String parentType, String createdBy) {
		try {
			ProjectInsightFormDetails projectInsightFormDetails = new ProjectInsightFormDetails();
			projectInsightFormDetails.setParentId(parentId);
			projectInsightFormDetails.setParentType(parentType);
			projectInsightFormDetails.setFields(fields);
			projectInsightFormDetails.setCreatedBy(createdBy);
			projectInsightFormDetails.setCreatedOn(getCurrentTimeInString());
			return saveProjectInsightFormDetails(projectInsightFormDetails, parentId, parentType);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void saveExcelProjectInsightQuestionDetails(List<ProjectInsightQuestionDetails> questions, String parentId,
			String parentType, String createdBy, List<String> parentPathIds) {
		try {
			if (questions != null && !questions.isEmpty()) {
				List<ProjectInsightQuestionDetails> newQuestionsObjList = new ArrayList<>();
				for (ProjectInsightQuestionDetails questionObj : questions) {
					ProjectInsightQuestionDetails projectInsightQuestionDetails = new ProjectInsightQuestionDetails();
					projectInsightQuestionDetails.setParentId(parentId);
					projectInsightQuestionDetails.setParentType(parentType);
					projectInsightQuestionDetails.setQuestion(questionObj.getQuestion());
					projectInsightQuestionDetails.setOptionType(questionObj.getOptionType());
					projectInsightQuestionDetails.setOptionsList(questionObj.getOptionsList());
					projectInsightQuestionDetails.setDescription(questionObj.getDescription());
					projectInsightQuestionDetails.setParentPathIds(parentPathIds);
					projectInsightQuestionDetails.setCreatedBy(createdBy);
					projectInsightQuestionDetails.setCreatedOn(getCurrentTimeInString());
					newQuestionsObjList.add(projectInsightQuestionDetails);
				}
				projectInsightQuestionDetailsRepository.saveAll(newQuestionsObjList);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}	

	private String getCurrentTimeInString() {
		return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
	}

	private void saveExcelProjectInsightGroupDetails(Map<String, GroupSectionData> groups, String parentId,
			String parentType, String createdBy, List<String> parentPathIds) {
		try {
			if (groups != null && !groups.isEmpty()) {

				for (Map.Entry<String, GroupSectionData> entry : groups.entrySet()) {
					String groupName = entry.getKey();
					GroupSectionData groupSectionData = entry.getValue();
					if (groupSectionData != null) {
						ProjectInsightGroupDetails projectInsightGroupDetails = groupSectionData.getProjectInsightGroupDetails();
						if (projectInsightGroupDetailsRepository.existsByGroupTitleAndParentIdAndParentType(
								projectInsightGroupDetails.getGroupTitle(), parentId, parentType)) {
							throw new BadRequestException("Group title must be unique within the parent group.");
						}
						projectInsightGroupDetails.setParentType(parentType);
						projectInsightGroupDetails.setParentId(parentId);
						projectInsightGroupDetails.setParentPathIds(parentPathIds);
						projectInsightGroupDetails.setCreatedBy(createdBy);
						projectInsightGroupDetails.setCreatedOn(getCurrentTimeInString());

						ProjectInsightGroupDetails dbResponse = projectInsightGroupDetailsRepository.save(projectInsightGroupDetails);
						if (dbResponse == null) {
							throw new BadRequestException("Unable to save Project Insight Group Details.");
						}

						ProjectInsightFormDetails projectInsightFormDetails = saveExcelProjectInsightFormDetails(groupSectionData.getFields(), dbResponse.getId(), "Group", createdBy);
						if (projectInsightFormDetails == null) {
							throw new BadRequestException("Unable to save Project Insight Group Form Details.");
						}

						List<String> newParentPathdIds = new ArrayList<>(parentPathIds);
						newParentPathdIds.add(dbResponse.getId());
						saveExcelProjectInsightQuestionDetails(groupSectionData.getQuestions(), dbResponse.getId(), parentType, createdBy, newParentPathdIds);

						if (groupSectionData.getSubGroups() != null && !groupSectionData.getSubGroups().isEmpty()) {
							saveExcelProjectInsightGroupDetails(groupSectionData.getSubGroups(), dbResponse.getId(), parentType, createdBy, newParentPathdIds);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	
	private ProjectQuestionStatusDto arrangeQuestionsStatusWise(List<ProjectInsightQuestionDetails> questions, Long empId, String projId, String projName) {
		Integer pending = 0,approved = 0,draft = 0;
		List<String> questionIds = questions.stream()
	                .map(ProjectInsightQuestionDetails::getId)
	                .collect(Collectors.toList());
	                
	    List<ProjectInsightResponseDetails> responses = projectInsightResponseDetailsRepository.findByQuesIdInAndResponseBy(questionIds, empId);
	        
	    Map<String, ProjectInsightResponseDetails> responseMap = responses.stream()
	                .collect(Collectors.toMap(ProjectInsightResponseDetails::getQuesId, r -> r));
		
		for(ProjectInsightQuestionDetails ques : questions) {
			ProjectInsightResponseDetails res = responseMap.get(ques.getId());
	            if (res != null) {
	                if (res.getReviewerInfo() == null || res.getReviewerInfo().isEmpty()) {
	                    draft++; // Draft
	                } else {
	                    approved++; // Assigned for review
	                }
	            } else {
	                pending++; // Pending
	            }
		}
		ProjectQuestionStatusDto status = new ProjectQuestionStatusDto();
		status.setPendingCount(pending);
		status.setDraftCount(draft);
		status.setApprovedCount(approved);
		status.setTotalCount(draft+pending+approved);
		status.setProjectName(projName);
		status.setProjectId(projId);
		return status;
	}
	
	private ProjectQuestionStatusDto arrangeQuestionsForApprovalTab(List<ProjectInsightQuestionDetails> questions, Long empId, String projId, String projName) {
		Integer pending = 0,approved = 0,rejected = 0;
		List<String> questionIds = questions.stream()
	                .map(ProjectInsightQuestionDetails::getId)
	                .collect(Collectors.toList());
	                
	    List<ProjectInsightResponseDetails> responses = projectInsightResponseDetailsRepository.findByReviewerIdAndQuesIds(empId,questionIds);
	        
	    Map<String, ProjectInsightResponseDetails> responseMap = responses.stream()
	                .collect(Collectors.toMap(ProjectInsightResponseDetails::getQuesId, r -> r));
	    for(ProjectInsightResponseDetails res : responses) {
	    		for(ReviewerInfoDTO reviewer:res.getReviewerInfo()) {
	    			if(reviewer.getReviewerid() == empId) {
	    				if(reviewer.getIsApproved()==null)pending++;
	    				else if(reviewer.getIsApproved())approved++;
	    				else rejected++;
	    			}
	    		}
	    }
	    ProjectQuestionStatusDto status = new ProjectQuestionStatusDto();
	    status.setApprovedCount(approved);
	    status.setPendingCount(pending);
	    status.setRejectedCount(rejected);
	    status.setTotalCount(rejected+approved+pending);
	    status.setProjectId(projId);
	    status.setProjectName(projName);
	    return status;
	}
		
	//Impl-3
	public List<ProjectQuestionStatusDto> getAllQuestionsStatusWise(Long empId) {
	    try {
	        if (empId == null) {
	            throw new BadRequestException("Employee ID cannot be null");
	        }
	        List<ProjectInsightProjectDetails> allProjects = projectInsightProjectDetailsRepository.findAll();
	        if (allProjects == null || allProjects.isEmpty()) {
	            throw new EmployeeNotFoundException("No projects found for employee " + empId);
	        }
	        List<ProjectQuestionStatusDto> response = new ArrayList<>();
	        for (ProjectInsightProjectDetails proj : allProjects) {
	            List<ProjectInsightQuestionDetails> filteredQuestions = projectInsightQuestionDetailsRepository.findByAssignedEmployeeAndParentPathId(empId, proj.getId());
	            ProjectQuestionStatusDto dto = arrangeQuestionsStatusWise(filteredQuestions == null ? Collections.emptyList() : filteredQuestions,empId,proj.getId(),proj.getProjectName());
	            if(dto.getTotalCount()>0)response.add(dto);
	        }
	        return response;
	    } catch (EmployeeNotFoundException | BadRequestException ex) {
	        throw ex;
	    } catch (Exception ex) {
	        throw new RuntimeException("Unexpected error while fetching questions for employee " + empId, ex);
	    }
	}
	
	
	public List<ProjectQuestionStatusDto> getAllQuestionsApprovalWise(Long empId) {
	    try {
	        if (empId == null) {
	            throw new BadRequestException("Employee ID cannot be null");
	        }
	        List<ProjectInsightProjectDetails> allProjects = projectInsightProjectDetailsRepository.findAll();
	        if (allProjects == null || allProjects.isEmpty()) {
	            throw new EmployeeNotFoundException("No projects found for employee " + empId);
	        }
	        List<ProjectQuestionStatusDto> response = new ArrayList<>();
	        for (ProjectInsightProjectDetails proj : allProjects) {
	            List<ProjectInsightQuestionDetails> filteredQuestions = projectInsightQuestionDetailsRepository.findQuestionsForProjectOrGroup(proj.getId());
	            ProjectQuestionStatusDto dto = arrangeQuestionsForApprovalTab(filteredQuestions == null ? Collections.emptyList() : filteredQuestions,empId,proj.getId(),proj.getProjectName());
	            if(dto.getTotalCount()>0)response.add(dto);
	        }
	        return response;
	    } catch (EmployeeNotFoundException | BadRequestException ex) {
	        throw ex;
	    } catch (Exception ex) {
	        throw new RuntimeException("Unexpected error while fetching questions for employee " + empId, ex);
	    }
	}
	
	public ProjectInsightGroupDetails getAllGroupsDataIn(QuestionGroupRequest request) {
	    try {
	        if (request == null || request.getParentId() == null) {
	            throw new BadRequestException("ParentId in request cannot be null");
	        }
	        Optional<ProjectInsightGroupDetails> groupOpt = projectInsightGroupDetailsRepository.findById(request.getParentId());
	        return groupOpt.orElseThrow(() -> new EmployeeNotFoundException("Group not found with ID: " + request.getParentId()));
	    } catch (BadRequestException | EmployeeNotFoundException ex) {
	        throw ex;
	    } catch (Exception ex) {
	        throw new RuntimeException("Unexpected error while fetching group data for parentId: " + request.getParentId(), ex);
	    }
	}

	public List<ProjectQuestionStatusDto> getAllGroupsDataByParentIdAndParentType(String parentId, String parentType, Long empId) {
	    try {
	        if (parentId == null || parentType == null) {
	            throw new BadRequestException("ParentId and ParentType cannot be null");
	        }

	        List<ProjectInsightGroupDetails> groupList =
	                projectInsightGroupDetailsRepository.findByParentIdAndParentType(parentId, parentType);
	        List<ProjectQuestionStatusDto> response = new ArrayList<>();
	        if (groupList == null || groupList.isEmpty()) {
	           // throw new EmployeeNotFoundException("No groups found for parentId: " + parentId + " and parentType: " + parentType);
	        	return response;
	        }

	        for (ProjectInsightGroupDetails group : groupList) {
	            try {
	                ProjectQuestionStatusDto statusDto = getGroupWiseQuestionCounts(group.getId(), group.getGroupTitle(), empId);
	                response.add(statusDto);
	            } catch (Exception e) {
	               // log.error("Error processing group {} for employee {}", group.getId(), empId, e);
	            }
	        }
	        return response;

	    } catch (BadRequestException | EmployeeNotFoundException ex) {
	        throw ex;
	    } catch (Exception ex) {
	        throw new RuntimeException("Unexpected error while fetching groups for parentId: " + parentId, ex);
	    }
	}

	public ProjectQuestionStatusDto getGroupWiseQuestionCounts(String groupId, String groupName, Long empId) {
	    try {
	        if (groupId == null || empId == null) {
	            throw new BadRequestException("GroupId and EmployeeId cannot be null");
	        }
	        List<ProjectInsightQuestionDetails> filteredQuestions = projectInsightQuestionDetailsRepository.findByAssignedEmployeeAndParentPathId(empId, groupId);
	        if (filteredQuestions == null) {
	            filteredQuestions = Collections.emptyList();
	        }
	        return arrangeQuestionsStatusWise(filteredQuestions, empId, groupId, groupName);
	    } catch (BadRequestException ex) {
	        throw ex;
	    } catch (Exception ex) {
	        throw new RuntimeException("Unexpected error while fetching question counts for groupId: " + groupId + " and employeeId: " + empId, ex);
	    }
	}
	
	public List<ProjectQuestionStatusDto> getAllApprovalTabCountData(String parentId, String parentType, Long empId) {
	    try {
	        if (parentId == null || parentType == null) {
	            throw new BadRequestException("ParentId and ParentType cannot be null");
	        }

	        List<ProjectInsightGroupDetails> groupList =
	                projectInsightGroupDetailsRepository.findByParentIdAndParentType(parentId, parentType);
	        List<ProjectQuestionStatusDto> response = new ArrayList<>();
	        if (groupList == null || groupList.isEmpty()) {
	        	return response;
	        }

	        for (ProjectInsightGroupDetails group : groupList) {
	            try {
	            	List<ProjectInsightQuestionDetails> filteredQuestions = projectInsightQuestionDetailsRepository.findQuestionsForProjectOrGroup(group.getId());
		            ProjectQuestionStatusDto dto = arrangeQuestionsForApprovalTab(filteredQuestions == null ? Collections.emptyList() : filteredQuestions,empId,group.getId(),group.getGroupTitle());
		            response.add(dto);
	            } catch (Exception e) {
	            	e.printStackTrace();
	            }
	        }
	        return response;

	    } catch (BadRequestException | EmployeeNotFoundException ex) {
	        throw ex;
	    } catch (Exception ex) {
	        throw new RuntimeException("Unexpected error while fetching groups for parentId: " + parentId, ex);
	    }
	}
	
	public QuesAndResponseDto getQuestionDataById(String quesId, Long empId) {
	    try {
	        if (quesId == null || empId == null) {
	            throw new BadRequestException("QuestionId and EmployeeId cannot be null");
	        }
	        QuesAndResponseDto response = new QuesAndResponseDto();
	        Map<Long, String> empNameMap = new HashMap<>();

	        ProjectInsightQuestionDetails question = projectInsightQuestionDetailsRepository.findById(quesId).orElseThrow(() -> new EmployeeNotFoundException("Question not found with ID: " + quesId));
	        response.setQuestion(question);
	        
	        Optional<ProjectInsightResponseDetails> ans = projectInsightResponseDetailsRepository.findByQuesIdAndResponseBy(quesId, empId);

	        if (ans.isPresent()) {
	            response.setResponse(ans.get());
	            if(ans.get().getReviewerInfo()!=null && !ans.get().getReviewerInfo().isEmpty()) {
	            	for(ReviewerInfoDTO reviewer:ans.get().getReviewerInfo()) {
	            		Employee emp = employeeRepository.findByEmpId(reviewer.getReviewerid());
	            		empNameMap.put(reviewer.getReviewerid(),emp.getName());
	            	}
	            }
	        } else {
	            response.setResponse(new ProjectInsightResponseDetails());
	        }
	        response.setEmpMap(empNameMap);
	        return response;

	    } catch (BadRequestException | EmployeeNotFoundException ex) {
	        throw ex;
	    } catch (Exception ex) {
	        throw new RuntimeException("Unexpected error while fetching question data for quesId: " + quesId, ex);
	    }
	}
	
	public QuestionMappedStatusDto getAllQuestionsforApprovalTab(String parentId, String parentType, Long empId) throws Exception {
	    try {
	        if (parentId == null || parentType == null || empId == null) {
	            throw new BadRequestException("ParentId, ParentType and EmployeeId cannot be null");
	        }
	        List<ProjectInsightQuestionDetails> questions = projectInsightQuestionDetailsRepository.findByParentIdAndParentType(parentId,parentType);

	        if (questions.isEmpty()) {
	            return new QuestionMappedStatusDto(Collections.emptyList(), Collections.emptyMap(),Collections.emptyMap());
	        }
	        
	        List<String> questionIds = questions.stream()
	            .map(ProjectInsightQuestionDetails::getId).toList();

	        List<ProjectInsightResponseDetails> responses =
	            projectInsightResponseDetailsRepository.findByReviewerIdAndQuesIds(empId, questionIds);

	        Map<String, ProjectInsightResponseDetails> responseMap = new HashMap<>(responses.size());
	        for (ProjectInsightResponseDetails resp : responses) {
	            responseMap.put(resp.getQuesId(), resp);
	        }

	        List<ProjectInsightQuestionDetails> allQuestions = new ArrayList<>();
	        Map<String, Integer> quesStatusMap = new HashMap<>();

	        for (ProjectInsightQuestionDetails ques : questions) {
	            ProjectInsightResponseDetails resp = responseMap.get(ques.getId());
	            if (resp == null) continue;

	            for (ReviewerInfoDTO reviewer : resp.getReviewerInfo()) {
	                if (Objects.equals(reviewer.getReviewerid(), empId)) {
	                    allQuestions.add(ques);

	                    if (reviewer.getIsApproved() == null) {
	                        quesStatusMap.put(ques.getId(), 2); // pending
	                    } else if (reviewer.getIsApproved()) {
	                        quesStatusMap.put(ques.getId(), 3); // approved
	                    } else {
	                        quesStatusMap.put(ques.getId(), 1); // rejected
	                    }
	                    break;
	                }
	            }
	        }

	        return new QuestionMappedStatusDto(allQuestions, quesStatusMap, Collections.emptyMap());

	    } catch (BadRequestException | EmployeeNotFoundException ex) {
	        throw ex;
	    } catch (Exception ex) {
	        throw new RuntimeException(
	            "Unexpected error while fetching questions for parentId: " + parentId, ex
	        );
	    }
	}
	
	public List<ProjectInsightResponseDetailsHistory> getResponseHistory(String quesId,Long empId){
		List<ProjectInsightResponseDetailsHistory> result = projectInsightResponseDetailsHistoryRepository.findByQuesIdAndResponseBy(quesId, empId);
		result.sort(Comparator.comparing(ProjectInsightResponseDetailsHistory::getVersion).reversed());
		return result;
	}
	
	public QuestionMappedStatusDto getAllQuestionsOfGroup(String parentId, String parentType, Long empId) {
	    try {
	        if (parentId == null || parentType == null || empId == null) {
	            throw new BadRequestException("ParentId, ParentType and EmployeeId cannot be null");
	        }

	        List<ProjectInsightQuestionDetails> allQuestions =
	                projectInsightQuestionDetailsRepository.findQuestionsForEmployee(parentId, parentType, empId);

	        if (allQuestions == null || allQuestions.isEmpty()) {
	            return new QuestionMappedStatusDto(Collections.emptyList(), Collections.emptyMap(),Collections.emptyMap());
	        }

	        List<String> questionIds = allQuestions.stream()
	                .map(ProjectInsightQuestionDetails::getId)
	                .collect(Collectors.toList());

	        List<ProjectInsightResponseDetails> responses =
	                projectInsightResponseDetailsRepository.findByQuesIdInAndResponseBy(questionIds, empId);
	        
	        List<ProjectInsightResponseDetailsHistory> responseHistory = projectInsightResponseDetailsHistoryRepository.findByQuesIdInAndResponseBy(questionIds, empId);
	        Map<String,Integer> historyMap = new HashMap<>();
	        for(String quesId:questionIds) {
	        	historyMap.put(quesId, 0);
	        }
	        for(ProjectInsightResponseDetailsHistory history:responseHistory) {
	        	if(!historyMap.containsKey(history.getQuesId())) {
	        		historyMap.put(history.getQuesId(), 0);
	        	}
	        	int CurrentVersion = history.getVersion();
	        	int previousVersion = historyMap.get(history.getQuesId());
	        	int finalVersion = Math.max(CurrentVersion, previousVersion);
	        	historyMap.put(history.getQuesId(),finalVersion);
	        }

	        Map<String, ProjectInsightResponseDetails> responseMap = responses.stream()
	                .collect(Collectors.toMap(ProjectInsightResponseDetails::getQuesId, r -> r));

	        Map<String, Integer> quesStatusMap = new HashMap<>();
	        for (ProjectInsightQuestionDetails ques : allQuestions) {
	            ProjectInsightResponseDetails res = responseMap.get(ques.getId());
	            if (res != null) {
	                if (res.getReviewerInfo() == null || res.getReviewerInfo().isEmpty()) {
	                    quesStatusMap.put(ques.getId(), 2); // Draft
	                } else {
	                    quesStatusMap.put(ques.getId(), 3); // Reviewed
	                }
	            } else {
	                quesStatusMap.put(ques.getId(), 1); // Pending
	            }
	        }
	        return new QuestionMappedStatusDto(allQuestions, quesStatusMap,historyMap);

	    } catch (BadRequestException | EmployeeNotFoundException ex) {
	        throw ex;
	    } catch (Exception ex) {
	        throw new RuntimeException("Unexpected error while fetching group questions for parentId: " + parentId, ex);
	    }
	}
	
	public String saveResponseAsDraft(ProjectInsightResponseDetails response) {
	    try {
	        if (response == null || response.getQuesId() == null || response.getResponseBy() == null) {
	            throw new BadRequestException("Response, QuestionId and ResponseBy cannot be null");
	        }
	        Optional<ProjectInsightResponseDetails> existing = projectInsightResponseDetailsRepository.findByQuesIdAndResponseBy(response.getQuesId(), response.getResponseBy());
	        if (existing.isPresent()) {
	            response.setId(existing.get().getId());
	        }
	        projectInsightResponseDetailsRepository.save(response);
	        return existing.isPresent() ? "Response Updated Successfully" : "Response Saved Successfully";
	    } catch (BadRequestException ex) {
	        throw ex;
	    } catch (Exception ex) {
	        throw new RuntimeException("Unexpected error while saving draft response for quesId: " + (response != null ? response.getQuesId() : "null"), ex);
	    }
	}
	
	public ProjectInsightResponseDetails saveApprovalForResponse(ApprovalRequest request)throws Exception{
		ProjectInsightResponseDetails response = request.getResponse();
		int n = response.getReviewerInfo().size();
		response.getReviewerInfo().get(n-1).setReviewedOn(LocalDateTime.now());
		if(request.getEditedByApprover()) {
			response.setLastSavedOn(LocalDateTime.now());
		}
		if(n==1 && response.getReviewerInfo().get(n-1).getLevel()==1 && response.getReviewerInfo().get(n-1).getIsApproved()) {
			ReviewerInfoDTO nextLevel = new ReviewerInfoDTO();
			nextLevel.setLevel(2);
			Long hodId = departmentRepository.findHodIdByEmpId(response.getResponseBy());
			nextLevel.setReviewerid(hodId);
			String revName = employeeRepository.findEmployeeNameById(hodId);
			nextLevel.setReviewerName(revName);
			nextLevel.setReviewAssignedOn(LocalDateTime.now());	
			response.getReviewerInfo().add(nextLevel);
		}
		if(!response.getReviewerInfo().get(n-1).getIsApproved()) {
			if(n==1) {
				QuestionGroupRequest req = new QuestionGroupRequest();
				req.setParentId(response.getId());
				String ans = cleanResponseAndReassign(req);
			}else if(n>1) {
				response.getReviewerInfo().get(n-2).setReassignReason(response.getReviewerInfo().get(n-1));
				response.getReviewerInfo().get(n-2).setIsApproved(null);
				response.getReviewerInfo().get(n-2).setMarks(null);
				response.getReviewerInfo().get(n-2).setQuality(null);
				response.getReviewerInfo().get(n-2).setRemarks(null);
				response.getReviewerInfo().get(n-2).setReviewedOn(null);
				response.getReviewerInfo().remove(n-1);
			}
		}
		ProjectInsightResponseDetails ans = projectInsightResponseDetailsRepository.save(response);
		return ans;
	}
	
	private Set<ProjectInsightQuestionDetails> getAllQuestionsRecursively(String parentId, String parentType, Long empId) {
	    Set<ProjectInsightQuestionDetails> result = new HashSet<>();
	    List<ProjectInsightQuestionDetails> questions = projectInsightQuestionDetailsRepository.findQuestionsForEmployee(parentId, parentType,empId);
	    if (questions != null) {
	        result.addAll(questions);
	    }
	    List<ProjectInsightGroupDetails> groups = projectInsightGroupDetailsRepository.findByParentIdAndParentType(parentId, parentType);
	    if (groups != null) {
	        for (ProjectInsightGroupDetails group : groups) {
	            result.addAll(getAllQuestionsRecursively(group.getId(), "Group", empId));
	        }
	    }
	    return result;
	}
	
	private Long getProjectIdFromProject(Integer projectId) {
		Optional<Project> project = projectRepository.findById(projectId);
		if(project.isPresent()) {
			return project.get().getProjectManagerId();
		}
		return 14L;
	}
	
	private Long getProjectManagerId(String parentId, String parentType) {
	    if ("Project".equals(parentType)) {
	        Optional<ProjectInsightProjectDetails> project = projectInsightProjectDetailsRepository.findById(parentId);
	        if (project.isPresent()) {
	            return project.get().getProjectManagerId() != null ? project.get().getProjectManagerId() : getProjectIdFromProject(project.get().getProjectId());
	        }
	        return 14L;
	    }
	    Optional<ProjectInsightGroupDetails> groupOpt = projectInsightGroupDetailsRepository.findById(parentId);
	    if (groupOpt.isPresent()) {
	        ProjectInsightGroupDetails group = groupOpt.get();
	        return getProjectManagerId(group.getParentId(), group.getParentType());
	    }
	    return 14L;
	}
	
	public String assignReviewersToAnsweredQuestions(QuestionGroupRequest request) {
	    Long managerId = getProjectManagerId(request.getParentId(), request.getParentType());
	    Employee manager = (managerId != null) ? employeeRepository.findByEmpId(managerId) : null;
	    AtomicInteger totalAssignedCount = new AtomicInteger(0);

	    List<ProjectInsightQuestionDetails> allQues = request.getToAllChilds()
	        ? new ArrayList<>(getAllQuestionsRecursively(request.getParentId(), request.getParentType(), request.getEmpId()))
	        : projectInsightQuestionDetailsRepository.findQuestionsForEmployee(request.getParentId(), request.getParentType(), request.getEmpId());

	    for (ProjectInsightQuestionDetails ques : allQues) {
	    	projectInsightResponseDetailsRepository.findByQuesIdAndResponseBy(ques.getId(), request.getEmpId())
	        .ifPresent(ans -> {
	            if ((ans.getReviewerInfo() == null || ans.getReviewerInfo().isEmpty()) && ans.getResponse() != null) {
	                List<ReviewerInfoDTO> reviewerList = new ArrayList<>();
	                ReviewerInfoDTO dto = new ReviewerInfoDTO();
	                if(managerId==null || manager==null) {
	                	Long hodId = departmentRepository.findHodIdByEmpId(Long.parseLong(ques.getCreatedBy()));
	                	String hodName = employeeRepository.findEmployeeNameById(hodId);
	                	dto.setReviewerName(hodName);
	                	dto.setReviewerid(hodId);
		                dto.setLevel(2);
		                totalAssignedCount.incrementAndGet();
	                }else {
	                	dto.setReviewerid(managerId);
	                	String managerName = employeeRepository.findEmployeeNameById(managerId);
	                	dto.setReviewerName(managerName);
		                dto.setLevel(1);
		                totalAssignedCount.incrementAndGet();
	                }
	                dto.setReviewAssignedOn(LocalDateTime.now());
	                reviewerList.add(dto);
	                ans.setReviewerInfo(reviewerList);
	                projectInsightResponseDetailsRepository.save(ans);
	            }
	        });
	    }
	    return totalAssignedCount.get()==0?"No eligible Questions found to Assign.":request.getToAllChilds()
	        ? "Assigned All Questions of All Groups and SubGroups under this Group/Project to Reviewers"
	        : "Assigned All Questions of Single level to Reviewers";
	}
	
	public String cleanResponseAndReassign(QuestionGroupRequest request) throws Exception {
	    String ans = "";
	    try {
	        Optional<ProjectInsightResponseDetails> res = 
	                projectInsightResponseDetailsRepository.findById(request.getParentId());

	        if (res.isPresent()) {
	        	ProjectInsightResponseDetailsHistory history = new ProjectInsightResponseDetailsHistory();
	            BeanUtils.copyProperties(res.get(), history); // Spring utility
	            ProjectInsightResponseDetailsHistory lastVersion = projectInsightResponseDetailsHistoryRepository.findFirstByQuesIdAndResponseByOrderByVersionDesc(history.getQuesId(), history.getResponseBy());
	            history.setVersion(lastVersion!=null?lastVersion.getVersion()+1:1);
	            projectInsightResponseDetailsHistoryRepository.save(history);
	            projectInsightResponseDetailsRepository.deleteById(res.get().getId());
	            ans = "Cleaned and Reassigned Successfully";
	        } else {
	            throw new BadRequestException("No Response Found to delete.");
	        }
	    } catch (BadRequestException e) {
	        throw e;
	    } catch (Exception e) {
	        throw new Exception("Error while cleaning and reassigning response", e);
	    }
	    return ans;
	}
	
	public Map<String,ProjectQuestionStatusDto> getStatusWiseCountByIds(RefreshRequestDto request){
		Map<String,ProjectQuestionStatusDto> response = new HashMap<>();
		List<String> projects = request.getProjects();
		List<String> groups = request.getGroups();
		for(String projId:projects) {
			List<ProjectInsightQuestionDetails> filteredQuestions = projectInsightQuestionDetailsRepository.findByAssignedEmployeeAndParentPathId(request.getEmpId(), projId);
			ProjectQuestionStatusDto dto = arrangeQuestionsStatusWise(filteredQuestions,request.getEmpId(),projId,"N/A");
			response.put(projId, dto);
		}
		for(String groupId:groups) {
			List<ProjectInsightQuestionDetails> filteredQuestions = projectInsightQuestionDetailsRepository.findByAssignedEmployeeAndParentPathId(request.getEmpId(), groupId);
			ProjectQuestionStatusDto dto = arrangeQuestionsStatusWise(filteredQuestions,request.getEmpId(),groupId,"N/A");
			response.put(groupId, dto);
		}
		
		return response;
	}
	
	public Map<String,ProjectQuestionStatusDto> refreshCountsApproval(RefreshRequestDto request){
		try {
	        if (request.getEmpId() == null) {
	            throw new BadRequestException("Employee ID cannot be null");
	        }
		Map<String,ProjectQuestionStatusDto> response = new HashMap<>();
		List<String> projects = request.getProjects();
		List<String> groups = request.getGroups();
		for(String projId:projects) {
			List<ProjectInsightQuestionDetails> filteredQuestions = projectInsightQuestionDetailsRepository.findQuestionsForProjectOrGroup(projId);
			ProjectQuestionStatusDto dto = arrangeQuestionsForApprovalTab(filteredQuestions == null ? Collections.emptyList() : filteredQuestions,request.getEmpId(),projId,"N/A");
			response.put(projId, dto);
		}
		for(String groupId:groups) {
			List<ProjectInsightQuestionDetails> filteredQuestions = projectInsightQuestionDetailsRepository.findByAssignedEmployeeAndParentPathId(request.getEmpId(), groupId);
			ProjectQuestionStatusDto dto = arrangeQuestionsForApprovalTab(filteredQuestions == null ? Collections.emptyList() : filteredQuestions,request.getEmpId(),groupId,"N/A");
			response.put(groupId, dto);
		}
		
		return response;
		} catch (EmployeeNotFoundException | BadRequestException ex) {
	        throw ex;
	    } catch (Exception ex) {
	        throw new RuntimeException("Unexpected error while fetching questions for employee " + request.getEmpId(), ex);
	    }
	}
	
	//Demo service to insert data in Questions table in mongoDb - remove at the end
	private List<String> getIdOfAllParent(String parentId, String parentType) {
	    List<String> result = new ArrayList<>();
	    if ("Project".equals(parentType)) {
	        result.add(parentId);
	        return result;
	    }
	    Optional<ProjectInsightGroupDetails> group = projectInsightGroupDetailsRepository.findById(parentId);
	    if (group.isPresent()) {
	        result.addAll(getIdOfAllParent(group.get().getParentId(), group.get().getParentType()));
	    }
	    result.add(parentId);
	    return result;
	}

	
	public void setDataInQuestions() {
		List<ProjectInsightQuestionDetails> allQues = projectInsightQuestionDetailsRepository.findAll();
		for(ProjectInsightQuestionDetails ques : allQues) {
			if(ques.getParentPathIds() == null|| ques.getParentPathIds().isEmpty()) {
				ques.setToAssignedEmployeeIdList(new ArrayList<>(List.of(14L)));
				List<String> parentPath = getIdOfAllParent(ques.getParentId(),ques.getParentType());
				ques.setParentPathIds(parentPath);
				projectInsightQuestionDetailsRepository.save(ques);
			}
		}
	}
}
