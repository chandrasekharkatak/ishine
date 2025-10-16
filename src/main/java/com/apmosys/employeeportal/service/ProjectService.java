package com.apmosys.employeeportal.service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
 
import javax.persistence.Query;

import javax.persistence.Query;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
//import org.hibernate.Query;
//import org.hibernate.Session;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.apmosys.employeeportal.dto.ClientProjectReportDTO;
import com.apmosys.employeeportal.dto.ClientsDTO;
import com.apmosys.employeeportal.dto.EmployeeProjectSummaryDTO;
import com.apmosys.employeeportal.dto.FixedCostProjectCount;
import com.apmosys.employeeportal.dto.GetEmployeeProjectCountDTO;
import com.apmosys.employeeportal.dto.FCLineItemDTO;
import com.apmosys.employeeportal.dto.FCProjectMilestoneDTO;
import com.apmosys.employeeportal.dto.GetEmployeeProjectReportDTO;
import com.apmosys.employeeportal.dto.GetEmployeeProjectReportForEmployeeDTO;
import com.apmosys.employeeportal.dto.GetEmployeeProjectReportPayloadDTO;
import com.apmosys.employeeportal.dto.GetProjectToEmployeeReportForEmployeeDTO;
import com.apmosys.employeeportal.dto.GetProjectToEmployeeReportForProjectDTO;
import com.apmosys.employeeportal.dto.GetProjectToEmployeeReportForTeamDTO;
import com.apmosys.employeeportal.dto.HandleTeamsAsPerLinkedPoPayloadDTO;
import com.apmosys.employeeportal.dto.HandleTeamsAsPerLinkedPoProjectDTO;
import com.apmosys.employeeportal.dto.LiftAndShiftTeamsDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.PoEmployeeTimesheetSyncDTO;
import com.apmosys.employeeportal.dto.PoProjectSyncDTO;
import com.apmosys.employeeportal.dto.PoProjectTimesheetSyncDTO;
import com.apmosys.employeeportal.dto.PoTeamDTO;
import com.apmosys.employeeportal.dto.PoTeamTimesheetSyncDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.ProjectFetchDTO;
import com.apmosys.employeeportal.dto.ProjectFilterDTO;
import com.apmosys.employeeportal.dto.ProjectManagersDTO;
import com.apmosys.employeeportal.dto.ProjectPoPortalDTO;
import com.apmosys.employeeportal.dto.ResourceRequirementDTO;
import com.apmosys.employeeportal.dto.SyncableProjectDTO;
import com.apmosys.employeeportal.exception.DataNotFoundException;
import com.apmosys.employeeportal.model.Activity;
import com.apmosys.employeeportal.model.ActivityTemplate;
import com.apmosys.employeeportal.model.ApiLog;
import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.ClientLocation;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeTeamMap;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectDepartmentMap;
import com.apmosys.employeeportal.model.ResourceRequirement;
import com.apmosys.employeeportal.model.Team;
import com.apmosys.employeeportal.model.UserSession;
import com.apmosys.employeeportal.repository.ActivitiesRepository;
import com.apmosys.employeeportal.repository.ActivityTemplateRepository;
import com.apmosys.employeeportal.repository.ClientLocationRepository;
import com.apmosys.employeeportal.repository.ClientsRepository;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.FCLineItemRepository;
import com.apmosys.employeeportal.repository.FCProjectMilestoneRepository;
import com.apmosys.employeeportal.repository.ProjectDepartmentMapRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.ResourceRequirementRepository;
import com.apmosys.employeeportal.repository.TeamRepository;
import com.apmosys.employeeportal.request.ProjectRequest;
import com.apmosys.employeeportal.repository.UserSessionRepository;
import com.apmosys.employeeportal.utility.ApiLogUtility;
import com.apmosys.employeeportal.utility.PoPortalAPIAuthenticationJWTUtility;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class ProjectService {
	
	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	ProjectRepository projectRepository;
	
	@Autowired
	UserSessionRepository userSessionRepo;
	
	
	@Autowired
	ClientsRepository clientsRepository;
	
	@Autowired
	EmployeeRepository employeeRepository;
	
	@Autowired
	ClientLocationRepository clientLocationRepository;
	
	@Autowired
	TeamRepository teamRepository;
	 
	@Autowired
	EmployeeTeamMapRepository employeeTeamMapRepository;
	
	@Autowired
	StringToDateTimeParser stringToDateTimeParser;
	
	@Autowired
	ProjectDepartmentMapRepository projectDepartmentMapRepository;
	
	@Autowired
	DepartmentRepository departmentRepository;
	
	@Autowired
	private ValidationService validationService;
	
	@Autowired
	ActivityTemplateRepository activityTemplateRepository;
	
	@Autowired
	ActivitiesRepository activitiesRepository;
	
	@Autowired
	ResourceRequirementRepository resourceRequirementRepository;
	
	@Autowired
	private HttpServletRequest httpRequest;
	
	@Autowired
	private LogService logService;
	
	@Value("${poPortal.api.allProjects}")
	private String allPoPortalProjects;
	
// 	@Value("${file.location.documents.fcmilestone}")
// 	private String fcMileStone;
	
	
// 	@Value("${poPortal.api.updateMilestones}")
// 	private String updateMilestoneUrl;
	
// //	@Value("${file.location.documents.fcmilestone}")
// 	private String fcMileStone;
	
	
// //	@Value("${poPortal.api.updateMilestones}")
// 	private String updateMilestoneUrl;
	
// //	@Value("${file.location.documents.fcmilestone}")
// 	private String fcMileStone;
	
	
// //	@Value("${poPortal.api.updateMilestones}")
// 	private String updateMilestoneUrl;
	
	@Autowired
	private final RestTemplate restTemplate = new RestTemplate();

	@Autowired
	private FCLineItemRepository fcLineItemRepository;	

	@Autowired
	private FCProjectMilestoneRepository fcProjectMilestoneRepository;

	@Autowired
	private PoPortalAPIAuthenticationJWTUtility poPortalAPIAuthenticationJWTUtility;
	
	@Autowired
	private ApiLogUtility apiLogUtility;
	
	@Autowired
	private PoPortalAPIService poPortalAPIService;

	public ServiceResponse getAllClients() {
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        //apiLogInfo.setSubFeatureName("");
        apiLogInfo.setApiUrl("/api/getAllClients");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();

		try {
			
			List<ClientsDTO> clientInfo = clientsRepository.getClientInfo();
			logBuilder.append("ClientInfoList : "  + clientInfo.size());
			
			if(clientInfo != null) {
				List<ClientsDTO> dtoList = new ArrayList<ClientsDTO>();
				
				clientInfo.forEach((object) -> {
					ClientsDTO clientDto = new ClientsDTO();
					
					clientDto.setClientId(object.getClientId() != null ? Integer.valueOf(object.getClientId().toString()) : null);
					clientDto.setClientName(object.getClientName() != null ? object.getClientName().toString() : null);
					clientDto.setClientLocation(object.getClientLocation() != null ? object.getClientLocation().toString() : null);
					clientDto.setClientLocationId(object.getClientLocationId() != null ? Integer.valueOf(object.getClientLocationId().toString()) : null);
					
					dtoList.add(clientDto);
				});
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("ClientInfoList: " + dtoList.size());			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Client Info not found.");
				apiLogInfo.setApiResponse("Client Info not Found");			
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
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse getAllProjects() {
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        //apiLogInfo.setSubFeatureName("");
        apiLogInfo.setApiUrl("/api/getAllProjects");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
		try {
			
			List<Object[]> projects = projectRepository.getAllProject();
        	logBuilder.append("AllProjectList : " + projects.size());
			
			if(projects != null) {
				List<ProjectDTO> dtoList = new ArrayList<ProjectDTO>();
				
				projects.forEach((object) -> {
					ProjectDTO projectDto = new ProjectDTO();
					
					projectDto.setProjectId(object[0] != null ? Integer.valueOf(object[0].toString()) : null);
					projectDto.setEmployeeName(object[1] != null ? object[1].toString() : null);
					projectDto.setProjectName(object[2] != null ? object[2].toString() : null);
					projectDto.setState(object[3] != null ? object[3].toString() : null);
					projectDto.setDepartmentName(object[4] != null ? object[4].toString() : null);
					projectDto.setClientName(object[5] != null ? object[5].toString() : null);
					projectDto.setClientLocation(object[6] != null ? object[6].toString() : null);
					projectDto.setCreatedByName(object[9] != null ? object[9].toString() : null);
					projectDto.setCreatedOn(Timestamp.valueOf(object[10] != null ? object[10].toString() : null));
					projectDto.setUpdatedOn(object[11] != null ? object[11].toString() : null);
					projectDto.setUpdatedByName(object[12] != null ? object[12].toString() : null);
					//as per RMG requirement
					projectDto.setRole(object[13] != null ? object[13].toString() : null);
					projectDto.setCount(object[14] != null ? Integer.valueOf(object[14].toString()) : null);
					projectDto.setExperience(object[15] != null ? object[15].toString() : null);
					projectDto.setEmpId(object[16] != null ? Long.parseLong(object[16].toString()) : null);
					dtoList.add(projectDto);
					System.err.println("vhg"+projectDto);
				});
				
				System.err.println("----------------------------------------------------------------------------------------_________________________________________________________________________________-------------------------------------------------_______________________----------____________________------____----__---_");
				
				
				
				System.out.println("Anurag :: dtoList finded   :: "+dtoList.toString());
				
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("All Project fetched!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Projects found.");
				apiLogInfo.setApiResponse("No Projects Found!");
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
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	@Transactional
	public ServiceResponse createProject(PoProjectSyncDTO poProjectSyncDTO) {
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("Create Project");
        apiLogInfo.setApiUrl("/api/createProject");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("Project Name : " + poProjectSyncDTO.getProjectName() + " , ProjectManagerId :" + poProjectSyncDTO.getProjectManagerId());
		try {
			//Find client (Inhouse : Apmosys)
			String internalClient = "Apmosys";
			Client firstClientOptional = clientsRepository.findByClientNameList(internalClient);
			if (firstClientOptional != null) {
			    Project projectObj = new Project();
				projectObj.setProjectName(poProjectSyncDTO.getProjectName());
				projectObj.setClientId(firstClientOptional.getClientId());
				projectObj.setState(poProjectSyncDTO.getState());
				projectObj.setActive("true");
				projectObj.setSyncProject("false");
				projectObj.setCreatedBy(Long.parseLong(poProjectSyncDTO.getCreatedBy()));
				projectObj.setCreatedOn(new Timestamp(System.currentTimeMillis()));
				projectObj.setProjectStatus("Not Started");
				projectObj.setInternalProjectType(poProjectSyncDTO.getInternalProjectType());	
//				projectObj.setIsDraftProject("Not Started");
				
				List<String> deptIds = new ArrayList<>();
				for (String department : poProjectSyncDTO.getDepartmentList()) {
				    Department departmentObj = departmentRepository.findByDeptId(Long.parseLong(department));
				    deptIds.add(departmentObj.getDeptId().toString()); 
				}

				String commaSeparatedDeptIds = String.join(", ", deptIds);
				projectObj.setDeptId(commaSeparatedDeptIds);
				commaSeparatedDeptIds = "";
				
				Project projectDbResponse =  projectRepository.save(projectObj);
				if(projectDbResponse != null) {
					// Add department mapping
					for(String department: poProjectSyncDTO.getDepartmentList()) {
						Department departmentObj = departmentRepository.findByDeptId(Long.parseLong(department));
						ProjectDepartmentMap projectDeptMap = new ProjectDepartmentMap();
						projectDeptMap.setProjectId(projectDbResponse.getProjectId());
						projectDeptMap.setDeptId(departmentObj.getDeptId());
						ProjectDepartmentMap projDeptMapDbResponse = projectDepartmentMapRepository.save(projectDeptMap);
					}
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Project created successfully.");
                    apiLogInfo.setApiResponse("Project Created Successfully!");
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Unable to create project.");
                    apiLogInfo.setApiResponse("Unable to create project!");
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
			    
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to find ApMoSys as internal client.");
                apiLogInfo.setApiResponse("Unable to find ApMoSys as internal client");
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
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	public ServiceResponse getProjectByProjectId(PoProjectSyncDTO poProjectSyncDto) {
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        //apiLogInfo.setSubFeatureName("");
        apiLogInfo.setApiUrl("/api/getProjectByProjectId");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("ProjectId : " + poProjectSyncDto.getProjectId());
		try {
			
			Optional<Project> project = projectRepository.findById(poProjectSyncDto.getProjectId());
			List<PoProjectSyncDTO> dtoList = new ArrayList<PoProjectSyncDTO>();
			List<String> departmentName = new ArrayList<String>();
			
			if(project.isPresent()) {
				Project projectObj = project.get();
				PoProjectSyncDTO projectdto = new PoProjectSyncDTO();
				List<Object[]> department = departmentRepository.getMappedDepartment(projectObj.getProjectId());
				for(Object[] object: department) {
					departmentName.add(object[0] != null ? object[0].toString() : null);
				}
				String[] departmentArr = new String[ departmentName.size() ];
				departmentName.toArray( departmentArr );
//				List<ClientLocation> clientLocation = clientLocationRepository.findByClientId(projectObj.getClientId());
//				
//				if(!clientLocation.isEmpty()) {
//					projectdto.setAllClientLocationList(clientLocation);
//				}else {
//					projectdto.setAllClientLocationList(null);
//				}
				
				projectdto.setProjectName(projectObj.getProjectName());
				projectdto.setClientId(projectObj.getClientId());
				projectdto.setDepartmentList(departmentArr);
//				projectdto.setProjectManagerId(projectObj.getProjectManagerId());
				projectdto.setState(projectObj.getState());
				projectdto.setProjectId(projectObj.getProjectId());
				projectdto.setSyncProject(projectObj.getSyncProject());
				dtoList.add(projectdto);
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
                apiLogInfo.setApiResponse("ProjectList :" + dtoList.size());
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not found.");
				apiLogInfo.setApiResponse("Project not Found");			
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
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	public ServiceResponse updateProject(PoProjectSyncDTO poProjectSyncDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Update Project");
		apiLogInfo.setApiUrl("/api/updateProject");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("ProjectId : " + poProjectSyncDTO.getProjectId() + " , ProjectName :" + poProjectSyncDTO.getProjectName() 
		 + " , ProjectManagerId : " + poProjectSyncDTO.getProjectManagerId());
		try {
			
			Project project = projectRepository.getById(poProjectSyncDTO.getProjectId());
			
			if(project != null) {
				
				project.setProjectName(poProjectSyncDTO.getProjectName());
				project.setClientId(poProjectSyncDTO.getClientId());
				project.setState(poProjectSyncDTO.getState());
				project.setActive("true");
				project.setSyncProject(poProjectSyncDTO.getSyncProject());
				project.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
				project.setUpdatedBy(Long.parseLong(poProjectSyncDTO.getUpdatedBy()));
				Project projectDbResponse =  projectRepository.save(project);
				
				if(projectDbResponse != null) {
					// Add department mapping
					for(String department: poProjectSyncDTO.getDepartmentList()) {
						Department departmentObj = departmentRepository.findByName(department);
						ProjectDepartmentMap projectDeptMap = new ProjectDepartmentMap();
						projectDeptMap.setProjectId(projectDbResponse.getProjectId());
						projectDeptMap.setDeptId(departmentObj.getDeptId());
						ProjectDepartmentMap projDeptMapDbResponse = projectDepartmentMapRepository.save(projectDeptMap);
					}
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Project updated successfully.");
                    apiLogInfo.setApiResponse("Project updated successfully!");			
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Project updation failed.");
                    apiLogInfo.setApiResponse("Project Updation failed!");			
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not found.");
                apiLogInfo.setApiResponse("Project Not Found");			
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
        apiLogInfo.setApiRequest(logBuilder.toString());
        logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	public ServiceResponse deleteProject(PoProjectSyncDTO poProjectSyncDto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Delete Project");
		apiLogInfo.setApiUrl("/api/deleteProject");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("ProjectId : " + poProjectSyncDto.getProjectId());
		try {
			
			Optional<Project> projectObj = projectRepository.findById(poProjectSyncDto.getProjectId());
			if (projectObj.isPresent()) {
				Project projectToBeDeleted = projectObj.get();
				projectToBeDeleted.setActive("false");
				projectToBeDeleted.setSyncProject("false");
				projectToBeDeleted.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
				Project dbResponse =  projectRepository.save(projectToBeDeleted);
				
				if(dbResponse != null) {
					//In-Activate Team related to project
					List<Team> teamList = teamRepository.findByProjectId(dbResponse.getProjectId());
					
					if(!teamList.isEmpty()) {
						teamList.forEach((teamToBeDeleted) -> {
							teamToBeDeleted.setIsActive("N");
							teamRepository.save(teamToBeDeleted);
						});	
					}
					
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Project deleted.");
					apiLogInfo.setApiResponse("Project deleted");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Project deletion Failed.");
					apiLogInfo.setApiResponse("Project deletion failed");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not found.");
				apiLogInfo.setApiResponse("Project not found");			
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
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	/*
	  - Po Project Sync API : start
	 */
	
	public List<Employee> getEmployeesByEmployeementIds(List<String> employeementIds) {
	    List<Employee> employeeList = new ArrayList<>();

	    if (employeementIds != null) {
	        for (String employeementId : employeementIds) {
	            if (employeementId != null && !employeementId.isEmpty()) {
	                try {
	                    Long employmentId;

	                    if (employeementId.contains("-")) {
	                        String[] parts = employeementId.split("-");
	                        if (parts.length == 2) {
	                            employmentId = Long.parseLong(parts[1]);
	                        } else {
	                            continue; 
	                        }
	                    } else {
	                        employmentId = Long.parseLong(employeementId);
	                    }

	                    Employee employeeObj = employeeRepository.findByEmployeementId(employmentId);
	                    if (employeeObj != null) {
	                        employeeList.add(employeeObj);
	                    }

	                } catch (NumberFormatException e) {
	                    continue;
	                }
	            }
	        }
	    }

	    return employeeList;
	}
	
	public Employee getEmployeeByEmployeementId(String employeementId) {
		Long employmentId = null;
		Employee employeeObj = null;
		if(employeementId != null && employeementId != "") {
			if(employeementId.contains("A-")) {
				employmentId = Long.parseLong(employeementId.split("-")[1]);
				employeeObj = employeeRepository.findByEmployeementId(employmentId);
			}else {
				employmentId = Long.parseLong(employeementId);
				employeeObj = employeeRepository.findByEmployeementId(employmentId);
			}
		}
		return employeeObj;
	}

	public ServiceResponse syncPoProjectAndTeam(PoProjectSyncDTO[] poProjectSyncDto) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("syncPoProjectAndTeam");
		apiLogInfo.setApiUrl("/api/syncPoProjectAndTeam");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder requestBuilder = new StringBuilder();
		StringBuilder responseBuilder = new StringBuilder();
		requestBuilder.append("Sync Start");
		try {
			
			for(PoProjectSyncDTO poProjectSyncDTO : poProjectSyncDto) {
				
				//validation
				if(poProjectSyncDTO.getPoProjectId() == null) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Please provide PoProject Id.");
					return response;
				}
				
				if(poProjectSyncDTO.getProjectName() == null || poProjectSyncDTO.getProjectName() == "") {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Please provide project name.");
					return response;
				}
				
				if(poProjectSyncDTO.getPoProjectManagers() == null || poProjectSyncDTO.getPoProjectManagers().isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Please provide PoProject Manager Id.");
					return response;
				}else if(!poProjectSyncDTO.getPoProjectManagers().isEmpty()) {
					for (String managerId : poProjectSyncDTO.getPoProjectManagers()) {
					    try {
					        // Split by "-" and parse the numeric part
					        String[] parts = managerId.split("-");
					        if (parts.length != 2) {
					            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					            response.setServiceResponse("Invalid manager ID format: " + managerId);
					            return response;
					        }

					        Long empId = Long.parseLong(parts[1]);

					        if (!validationService.validateEmploymentId(empId)) {
					            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					            response.setServiceResponse("No user exists as project manager with EmpId: " + managerId);
					            return response;
					        }

					    } catch (NumberFormatException e) {
					        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					        response.setServiceResponse("Invalid number in manager ID: " + managerId);
					        return response;
					    }
					}
				}
				if(poProjectSyncDTO.getPoClientId() == null) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Please provide PoClient Id.");
					return response;
				}
				
				if(poProjectSyncDTO.getClientName() == null || poProjectSyncDTO.getClientName() == "") {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Please provide Client name.");
					return response;
				}
				
				if(poProjectSyncDTO.getState() == null || poProjectSyncDTO.getState() == "") {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Please provide State.");
					return response;
				}
				
				if(poProjectSyncDTO.getClientLocation() == null) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Please provide Client location(s).");
					return response;
				}else if(poProjectSyncDTO.getClientLocation().length == 0) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Please provide Client location(s).");
					return response;
				}else if(poProjectSyncDTO.getClientLocation().length != 0) {
					for(String location: poProjectSyncDTO.getClientLocation()) {
						if(location == "") {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Client location(s) is empty.");
							return response;
						}
					}
				}
				Long duplicateClientLocation = Arrays.stream(poProjectSyncDTO.getClientLocation()).distinct().count();
				if(duplicateClientLocation < poProjectSyncDTO.getClientLocation().length) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Client Locations are duplicate.");
					return response;
				}
				
				if(poProjectSyncDTO.getStatus() == null || poProjectSyncDTO.getStatus() == "") {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Please provide project status.");
					return response;
				}
				
				if(poProjectSyncDTO.getDepartmentList() == null) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Please provide department.");
					return response;
				}else if(poProjectSyncDTO.getDepartmentList().length == 0) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Please provide department.");
					return response;
				}else if(poProjectSyncDTO.getDepartmentList().length != 0) {
					for(String department: poProjectSyncDTO.getDepartmentList()) {
						Department departmentObj = departmentRepository.findByName(department);
						if(department == "") {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Department is empty.");
							return response;
						}
						if(departmentObj == null) {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Department not found : " + department);
							return response;
						}
					}
				}
				Long duplicateDepartment = Arrays.stream(poProjectSyncDTO.getDepartmentList()).distinct().count();
				if(duplicateDepartment < poProjectSyncDTO.getDepartmentList().length) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Department name are duplicate.");
					return response;
				}
				
				if(poProjectSyncDTO.getTeamList() == null) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Please provide Team info.");
					return response;
				}else if(poProjectSyncDTO.getTeamList().isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Please provide Team info.");
					return response;
				}else if(!poProjectSyncDTO.getTeamList().isEmpty()) {
					for (int i = 0; i < poProjectSyncDTO.getTeamList().size(); i++){
						PoTeamDTO team = poProjectSyncDTO.getTeamList().get(i);
						String teamMemberDepartment = null;
						if(team.getPoTeamId() == null) {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Please provide PoTeam Id.");
							return response;
						}
						if(team.getPoTeamLeadId() != null && !validationService.validateEmploymentId(Long.parseLong(team.getPoTeamLeadId().split("-")[1]))) {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("No team lead exist with EmpId : " + team.getPoTeamLeadId());
							return response;
						}
						if(team.getTeamName() == null || team.getTeamName() == "") {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Please provide team name.");
							return response;
						}
						
						if(team.getDepartmentList().length == 0) {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Please provide departments for " + team.getTeamName());
							return response;
						}else if(team.getDepartmentList().length != 0) {
							//check if department present
							for(String department: team.getDepartmentList()) {
								Department deptObj = departmentRepository.findByName(department);
								if(deptObj == null) {
									response.setServiceStatus(ServiceResponse.STATUS_FAIL);
									response.setServiceResponse("Department does not exists : "+" "+department+" " + team.getTeamName());
									return response;
								}
							}
						}
						if(team.getTeamMemberList() == null) {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Please provide team member(s) list in " + team.getTeamName());
							return response;
						}else if(team.getTeamMemberList().length == 0 ) {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Please provide team member(s) in " + team.getTeamName());
							return response;
						}else if(team.getTeamMemberList().length != 0) {
							for(String teamMember: team.getTeamMemberList()) {
								if(teamMember == null || teamMember == "") {
									response.setServiceStatus(ServiceResponse.STATUS_FAIL);
									response.setServiceResponse("Team member is empty.");
									return response;
								}else if(!validationService.validateEmploymentId(Long.parseLong(teamMember.split("-")[1]))) {
									response.setServiceStatus(ServiceResponse.STATUS_FAIL);
									response.setServiceResponse("No user found with EmpId : " + teamMember + " exist in " + team.getTeamName());
									return response;
								}
								//check if teamMember are from given department department
								List<Object[]> departmentObj = employeeRepository.getDepartmentByEmployeementId(Long.parseLong(teamMember.split("-")[1]));
								if(departmentObj != null) {
									String departmentName = null;
									for(Object[] object: departmentObj) {
										departmentName = object[1] != null ? object[1].toString() : null;
									}
									if(!Arrays.asList(team.getDepartmentList()).contains(departmentName)) {
										response.setServiceStatus(ServiceResponse.STATUS_FAIL);
										response.setServiceResponse("Team member(s) should be from department mentioned in departmentList : " + team.getTeamName());
										return response;
									}
//									if(teamMemberDepartment != null) {
//										if(!departmentName.equals(teamMemberDepartment)) {
//											response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//											response.setServiceResponse("Team member(s) should be from same department " + team.getTeamName());
//											return response;
//										}
//									}else {
//										teamMemberDepartment = departmentName;
//									}
								}
							}
							Long duplicateTeamMember = Arrays.stream(team.getTeamMemberList()).distinct().count();
							if(duplicateTeamMember < team.getTeamMemberList().length) {
								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
								response.setServiceResponse("Team member(s) are duplicate.");
								return response;
							}
						}
					}
				}
				long duplicatePoTeamId = poProjectSyncDTO.getTeamList().stream().map(p -> p.getPoTeamId()).distinct().count();
				if(duplicatePoTeamId < poProjectSyncDTO.getTeamList().size()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("PoTeam Id is duplicate in TeamList.");
					return response;
				}
				
				Project project = projectRepository.findByPoProjectId(poProjectSyncDTO.getPoProjectId());
				List<Employee> managerObjs = getEmployeesByEmployeementIds(poProjectSyncDTO.getPoProjectManagers());
				Integer clientId = null;
				
				Client client = clientsRepository.findByPoClientId(poProjectSyncDTO.getPoClientId());
				ClientLocation clientLocation = null;
				if(client != null) {
					clientId = client.getClientId();
					for(String location: poProjectSyncDTO.getClientLocation()) {
						clientLocation = clientLocationRepository.findByClientIdAndClientLocation(client.getClientId(), location);
						if(clientLocation == null) {
							ClientLocation locationObj = new ClientLocation();
							locationObj.setClientId(client.getClientId());
							locationObj.setClientLocation(location);
							
							ClientLocation locationDbResponse = clientLocationRepository.save(locationObj);
						}
					}
				}
				
				if(project != null) {
					requestBuilder.append("Update Project : " + "Project Name In Ishine : " + project.getProjectName() + "Project Name fro PoPortal : " + poProjectSyncDTO.getProjectName());
					
					//validate
					if(poProjectSyncDTO.getUpdatedBy() == null || poProjectSyncDTO.getUpdatedBy() == "") {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Please provide Updated By : " + poProjectSyncDTO.getProjectName());
						return response;
					}else if(!validationService.validateEmploymentId(Long.parseLong(poProjectSyncDTO.getUpdatedBy().split("-")[1]))) {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("No user found with EmpId : " + poProjectSyncDTO.getUpdatedBy());
						return response;
					}
					if(validationService.validateProjectName(poProjectSyncDTO.getProjectName()) && !project.getProjectName().equals(poProjectSyncDTO.getProjectName())) {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Project name already exist : " + poProjectSyncDTO.getProjectName());
						return response;
					}
					if(clientId == null) {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Cannot create new client while updating a project.");
						return response;
					}
					if(!project.getClientId().equals(clientId)) {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Client cannot be updated.");
						return response;
					}
					for (int i = 0; i < poProjectSyncDTO.getTeamList().size(); i++){
						PoTeamDTO team = poProjectSyncDTO.getTeamList().get(i);
						Team teamExists = teamRepository.findByTeamNameAndProjectId(team.getTeamName(), project.getProjectId());
						if(!team.getPoTeamId().equals(teamExists.getPoTeamId())) {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Team name already exist : " + team.getTeamName());
							return response;
						}
					}
					Employee updatedByObj = getEmployeeByEmployeementId(poProjectSyncDTO.getUpdatedBy());
					
					//update project info
					project.setProjectName(poProjectSyncDTO.getProjectName());
//					project.setProjectManagerId(managerObj.getEmpId());
					project.setState(poProjectSyncDTO.getState());
					project.setUpdatedBy(updatedByObj.getEmpId());
					project.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
					if(poProjectSyncDTO.getStatus().equals("Completed")) {
						project.setActive("false");
						project.setSyncProject("false");
					}else {
						project.setActive("true");
						project.setSyncProject("true");
					}
					Project projectDbResponse =  projectRepository.save(project);
					
					if(projectDbResponse != null) {
						responseBuilder.append("Update Project resposne : success " + "Project Id " + projectDbResponse.getProjectId() + " Project Name" + projectDbResponse.getProjectName());
												
						//update department
						List<Long> departmentId = new ArrayList<Long>();
						for(String department: poProjectSyncDTO.getDepartmentList()){
							Department departmentObj = departmentRepository.findByName(department);
							departmentId.add(departmentObj.getDeptId());
							if(departmentObj != null) {
								ProjectDepartmentMap projectDeptMapObj = projectDepartmentMapRepository.
										findByProjectIdAndDeptId(projectDbResponse.getProjectId(),departmentObj.getDeptId());
								if(projectDeptMapObj == null) {
									//add department
									ProjectDepartmentMap projectDeptMap = new ProjectDepartmentMap();
									projectDeptMap.setProjectId(projectDbResponse.getProjectId());
									projectDeptMap.setDeptId(departmentObj.getDeptId());
									ProjectDepartmentMap projDeptMapDbResponse = projectDepartmentMapRepository.save(projectDeptMap);
									
									if(projDeptMapDbResponse != null) {
										responseBuilder.append("Project Department Mapping resposne : sucess" + "ProjDept Mapping Id : " + projDeptMapDbResponse.getProjectDepartmentMapId());
									}else {
										responseBuilder.append("Project Department Mapping resposne : failed");
									}
								}
							}
						}
						// remove department
//						List<ProjectDepartmentMap> removeProjDeptMap = projectDepartmentMapRepository.
//								findByDeptIdNotInAndProjectId(departmentId, projectDbResponse.getProjectId());
//						
//						if(!removeProjDeptMap.isEmpty()) {
//							removeProjDeptMap.forEach((projDeptObj) -> {
//								
//							});
//						}
						
						//update team info 
						poProjectSyncDTO.getTeamList().forEach((object) -> {
												
							Team teamObj =  teamRepository.findByPoTeamId(object.getPoTeamId());
							
							Employee teamLeadObj = null;
							if(object.getPoTeamLeadId() != null && object.getPoTeamLeadId() != "") {
								teamLeadObj = getEmployeeByEmployeementId(object.getPoTeamLeadId());
							}
//							Long deptId = null;
//							Long hodId = null;
//							List<Object[]> departmentObj = employeeRepository.getDepartmentByEmployeementId(Long.parseLong(object.getTeamMemberList()[0].split("-")[1]));
//							if(departmentObj != null) {
//								for(Object[] deptObj: departmentObj) {
//									deptId = deptObj[3] != null ? Long.parseLong(deptObj[3].toString()) : null;
//									hodId = deptObj[4] != null ? Long.parseLong(deptObj[4].toString()) : null;
//								}
//							}
							StringBuilder deptList = new StringBuilder("");
							for(String department: object.getDepartmentList()) {
								Department deptObj = departmentRepository.findByName(department);
								if(deptObj != null) {
									deptList.append(deptObj.getDeptId()).append(",");
								}
							}
							Employee teamUpdatedByObj = getEmployeeByEmployeementId(object.getUpdatedBy());
							if(teamObj != null) {
								
								requestBuilder.append("update Team Info : " + "Team Name from PoPortal : "+ object.getTeamName() + "Team Name in IShine : " + teamObj.getTeamName() + " Team Id : " + teamObj.getTeamId());
								
								
								teamObj.setTeamName(object.getTeamName());
								teamObj.setTeamLeadId(teamLeadObj !=null ? teamLeadObj.getEmpId() : null);
								teamObj.setDescription(object.getDescription());
								teamObj.setTeamLeadName(teamLeadObj !=null ? teamLeadObj.getName() : null);
								teamObj.setDeptIds(deptList.toString());
								teamObj.setUpdatedBy(teamUpdatedByObj !=null ? teamUpdatedByObj.getEmpId() : null);
								teamObj.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
								Team teamDbResponse = teamRepository.save(teamObj);
								
								if(teamDbResponse != null) {
									
									responseBuilder.append("Update Team Response : success" + " Team Id : " +  teamDbResponse.getTeamId() + "Team Name : " + teamDbResponse.getTeamName());
									
									//update employee team mapping : adding new member, in-active removed member
									List<Long> teamMemberList = new ArrayList<Long>();
									List<String> newTeamMemberList = new ArrayList<String>(Arrays.asList(object.getTeamMemberList()));
									//Adding projectManager, HOD, teamLead in teamMemberList
//									newTeamMemberList.add(poProjectSyncDTO.getPoProjectManagerId());
									newTeamMemberList.add(object.getPoTeamLeadId());
									for(String department: object.getDepartmentList()) {
										Department deptObj = departmentRepository.findByName(department);
										Employee emp = employeeRepository.findByEmpId(deptObj.getHodId());
										newTeamMemberList.add(emp.getEmployeementId().toString());
									}
									for (String memberObj : newTeamMemberList) {
										Employee teamMemberObj = getEmployeeByEmployeementId(memberObj);
										teamMemberList.add(teamMemberObj.getEmpId());
										List<EmployeeTeamMap> teamMappingObj = employeeTeamMapRepository.
												findFirstByEmpIdAndTeamIdAndActive(teamMemberObj.getEmpId(), teamDbResponse.getTeamId());
										// adding new member
										if(teamMappingObj.isEmpty()) {
											
											requestBuilder.append("Add new Team Member : " + " Member id : " + teamMemberObj.getEmpId() + " Member Team Id : " + teamDbResponse.getTeamId());
											
											EmployeeTeamMap empTeamMap = new EmployeeTeamMap();
											empTeamMap.setEmpId(teamMemberObj.getEmpId());
											empTeamMap.setTeamId(teamDbResponse.getTeamId());
											empTeamMap.setActive(1l);
											empTeamMap.setEmployeeRole("Employee");
											EmployeeTeamMap teamMapDbResponse = employeeTeamMapRepository.save(empTeamMap);
											
											if(teamMapDbResponse != null) {
												responseBuilder.append("Adding new member resposne : success" + "EmpTeamMap Id : " + teamMapDbResponse.getEmployeeTeamMapId() + " Team Id : " + teamMapDbResponse.getTeamId());
											}else {
												responseBuilder.append("Adding new member resposne : failed" + "employee Id : " + teamMemberObj.getEmpId() + " Team Id : " + teamDbResponse.getTeamId());
											}
										}
							        }
									// In-Active/removed team member
									List<EmployeeTeamMap> alreadyExistMember = employeeTeamMapRepository.
											findByEmpIdNotInAndTeamId(teamMemberList, teamDbResponse.getTeamId());
									if(alreadyExistMember != null) {
										List<EmployeeTeamMap> inActiveMember = new ArrayList<EmployeeTeamMap>();
										alreadyExistMember.forEach((member) -> {
											
											member.setActive(0l);
											member.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
											inActiveMember.add(member);
										});
										List<EmployeeTeamMap> inActiveDbResponse = employeeTeamMapRepository.saveAll(inActiveMember);
									}
									response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
									response.setServiceResponse("Project & Team updated Successfully.");
									
									responseBuilder.append("Team Updation response : success");								
								}else {
									response.setServiceStatus(ServiceResponse.STATUS_FAIL);
									response.setServiceResponse("Team updation failed.");
									
									responseBuilder.append("Team Updation response : failed");
								}
							}else {
								
								requestBuilder.append("New Team creation while updation Project : " + "PoTeam Id : " + object.getPoTeamId() + "PoTeam Name : " + object.getTeamName());
								
								Employee teamCreatedByObj = getEmployeeByEmployeementId(object.getCreatedBy());
								//create Team
								
								Team newTeamObj = new Team();
								newTeamObj.setIsActive("Y");
								newTeamObj.setPoTeamId(object.getPoTeamId());
								newTeamObj.setProjectId(projectDbResponse.getProjectId());
								newTeamObj.setTeamLeadId(teamLeadObj != null ? teamLeadObj.getEmpId() : null);
								newTeamObj.setTeamName(object.getTeamName());
								newTeamObj.setTeamLeadName(teamLeadObj != null ? teamLeadObj.getName() : null);
								newTeamObj.setDescription(object.getDescription());
								newTeamObj.setDeptIds(deptList.toString());
								newTeamObj.setCreatedBy(teamCreatedByObj != null ? teamCreatedByObj.getEmpId() : null);
								newTeamObj.setCreatedOn(new Timestamp(System.currentTimeMillis())); 
								Team teamDbResponse = teamRepository.save(newTeamObj);
								
								List<EmployeeTeamMap> teamMemberDbResponse = null;
								if(teamDbResponse != null) {
									
									responseBuilder.append("New Team creation in update project response : success" + "Team Id : " + teamDbResponse.getTeamId());
									
									List<EmployeeTeamMap> mapList = new ArrayList<EmployeeTeamMap>();
									// Add teamLead
									EmployeeTeamMap defaultMemberMap = new EmployeeTeamMap();
									defaultMemberMap.setActive(1l);
									defaultMemberMap.setEmpId(teamLeadObj != null ? teamLeadObj.getEmpId() : null);
									defaultMemberMap.setTeamId(teamDbResponse.getTeamId());
									defaultMemberMap.setEmployeeRole("TeamLead");
									mapList.add(defaultMemberMap);
									
									// Add Project Manager
									defaultMemberMap = new EmployeeTeamMap();
									defaultMemberMap.setActive(1l);
//									defaultMemberMap.setEmpId(managerObj.getEmpId());
									defaultMemberMap.setTeamId(teamDbResponse.getTeamId());
									defaultMemberMap.setEmployeeRole("Manager");
									mapList.add(defaultMemberMap);
									
									List<Long> deptIds = new ArrayList<>();
									// Add HOD of selected Department
									for(String department: object.getDepartmentList()) {
										Department deptObj = departmentRepository.findByName(department);
										deptIds.add(deptObj.getDeptId());
										if(deptObj != null) {
											defaultMemberMap = new EmployeeTeamMap();
											defaultMemberMap.setActive(1l);
											defaultMemberMap.setEmpId(deptObj.getHodId());
											defaultMemberMap.setTeamId(teamDbResponse.getTeamId());
											defaultMemberMap.setEmployeeRole("HOD");
											mapList.add(defaultMemberMap);
										}
									}
									
									// Add team member in team
									List<Department> deptsObj = departmentRepository.findByDeptIdIn(deptIds);
									for(String teamMember: object.getTeamMemberList()) {
										Employee teamMemberObj = getEmployeeByEmployeementId(teamMember);
										EmployeeTeamMap newEmpTeamMap = new EmployeeTeamMap();
										
										// check if HOD, Manager, TeamLead already added
										if(( (teamLeadObj.getEmpId() == null) || (teamLeadObj.getEmpId() != null && !teamLeadObj.getEmpId().equals(teamMemberObj.getEmpId())) 
												
//												) && !managerObj.getEmpId().equals(teamMemberObj.getEmpId())
												)
												&& ( (deptsObj.isEmpty()) || (!deptsObj.isEmpty() && 
														!deptsObj.stream().anyMatch(o -> teamMemberObj.getEmpId().equals(o.getHodId()))) )) {
											
											requestBuilder.append("Adding new member in team : " + " EmpId : " + teamMemberObj.getEmpId() + " Team Id : " + teamDbResponse.getTeamId());
											
											newEmpTeamMap.setActive(1l);
											newEmpTeamMap.setEmpId(teamMemberObj.getEmpId());
											newEmpTeamMap.setTeamId(teamDbResponse.getTeamId());
											newEmpTeamMap.setEmployeeRole("Employee");
											mapList.add(newEmpTeamMap);
										}	
									}
									teamMemberDbResponse = employeeTeamMapRepository.saveAll(mapList);
									
									//Adding default Activities
									
									Activity newActivityCreated = null;
									if(!teamMemberDbResponse.isEmpty()) {
										
										responseBuilder.append("Add new Member in Team response : success");
										
										
										//Add default activities mapping
										for(String department: object.getDepartmentList()) {
											Department deptObj = departmentRepository.findByName(department);
											if(deptObj != null) {
												List<ActivityTemplate> activityTemplate = activityTemplateRepository.getByDeptId(deptObj.getDeptId());
												if(!activityTemplate.isEmpty()) {
													
													for(ActivityTemplate activityObject: activityTemplate) {
														Activity newActivity = new Activity();

														newActivity.setActivity(activityObject.getTemplateActivity());
														newActivity.setTeamId(teamDbResponse.getTeamId());
														newActivity.setEmployeeRole(activityObject.getEmployeeRole());
														newActivity.setDeptIds(activityObject.getDeptId().toString());
														newActivity.getCommonProperty().setCreatedBy(4l);

														newActivityCreated = activitiesRepository.save(newActivity);
													}
												}
											}
										}
									}
									if(newActivityCreated != null) {
										
										responseBuilder.append("Adding Default activities in Team : success");
										
										response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
										response.setServiceResponse("Project updated & new Team created Successfully.");
									}else {
										responseBuilder.append("Adding Default activities in Team : failed");
										
										response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
										response.setServiceResponse("Project updated & new Team created Successfully, but default activitis are not mapped.");
									}
								}else {
									responseBuilder.append("New Team creation in update project response : failed");
									
									response.setServiceStatus(ServiceResponse.STATUS_FAIL);
									response.setServiceResponse("Unable to create new team.");
								}
							}
						});
					}else {
						
						responseBuilder.append("Update Project resposne : failed " + "Project Id " + " Project Name" + poProjectSyncDTO.getProjectName());
						
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Project Updation failed.");
					}
				}else {
					
					requestBuilder.append("Create Project : " + "Project Name  : " +poProjectSyncDTO.getProjectName() + "PoProject Id : " + poProjectSyncDTO.getPoProjectId());
					
					//validation
					if(poProjectSyncDTO.getCreatedBy() == null || poProjectSyncDTO.getCreatedBy() == "") {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Please provide Created By : " + poProjectSyncDTO.getProjectName());
						return response;
					}
					if(!validationService.validateEmploymentId(Long.parseLong(poProjectSyncDTO.getCreatedBy().split("-")[1]))) {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("No user found with EmpId : " + poProjectSyncDTO.getCreatedBy());
						return response;
					}
					if(validationService.validateProjectName(poProjectSyncDTO.getProjectName())) {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Project name already exist : " + poProjectSyncDTO.getProjectName());
						return response;
					}
					for (int i = 0; i < poProjectSyncDTO.getTeamList().size(); i++){
						PoTeamDTO team = poProjectSyncDTO.getTeamList().get(i);
						Team teamExists = teamRepository.findByPoTeamId(team.getPoTeamId());
						if(teamExists != null) {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("PoTeam ID already exists : " + poProjectSyncDTO.getProjectName());
							return response;
						}
						if(team.getCreatedBy() != null) {
							if(!validationService.validateEmploymentId(Long.parseLong(team.getCreatedBy().split("-")[1]))) {
								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
								response.setServiceResponse("No user found with EmpId : " + team.getCreatedBy());
								return response;
							}
						}else {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Please provide Team created By : " + team.getTeamName());						return response;
						}
					}
					
					Client clientDbresponse = null;
					// Add new client & client location if not exist
					if(client == null) {
						
						requestBuilder.append("Add new Client : " + " Client Name : " + poProjectSyncDTO.getClientName() + " PoClient Id :" + poProjectSyncDTO.getPoClientId());
						
						Client clientobj = new Client();
						clientobj.setClientName(poProjectSyncDTO.getClientName());
						clientobj.setPoClientId(poProjectSyncDTO.getPoClientId());
						
						clientDbresponse = clientsRepository.save(clientobj);
						if(clientDbresponse != null) {
							
							responseBuilder.append("Create New Client resposne : success " + "Client Id : " + clientDbresponse.getClientId());
							
							clientId = clientDbresponse.getClientId();
							List<ClientLocation> clientLocations = new ArrayList<ClientLocation>();
							//default location : WFH
							ClientLocation clientLocationObj = new ClientLocation();
							clientLocationObj.setClientId(clientDbresponse.getClientId());
							clientLocationObj.setClientLocation("WFH");
							clientLocations.add(clientLocationObj);
							// add client location
							for(String location: poProjectSyncDTO.getClientLocation()) {
								ClientLocation locationObj = new ClientLocation();
								locationObj.setClientId(clientDbresponse.getClientId());
								locationObj.setClientLocation(location);
								clientLocations.add(locationObj);
							}
							List<ClientLocation> locationDbResponse = clientLocationRepository.saveAll(clientLocations);
						}else {
							
							responseBuilder.append("Create New Client resposne : failed ");
							
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Unable to create new client.");
						}
					}
					Employee createdByObj = getEmployeeByEmployeementId(poProjectSyncDTO.getCreatedBy());
					
					// create new project
					Project projectObj = new Project();
					projectObj.setProjectName(poProjectSyncDTO.getProjectName());
//					projectObj.setProjectManagerId(managerObj.getEmpId());
					projectObj.setPoProjectId(poProjectSyncDTO.getPoProjectId());
					projectObj.setClientId(clientId);
					projectObj.setState(poProjectSyncDTO.getState());
					projectObj.setActive("true");
					projectObj.setSyncProject("true");
					projectObj.setCreatedBy(createdByObj.getEmpId());
					Project projectDbResponse =  projectRepository.save(projectObj);
					
					if(projectDbResponse != null) {
						
						responseBuilder.append("Create New Project response : success " + "Project Id : " + projectDbResponse.getProjectId());
						
						// Add department mapping
						for(String department: poProjectSyncDTO.getDepartmentList()) {
							
							requestBuilder.append("New Project Department Mapping : " + "Project Name : " + projectDbResponse.getProjectName() + " Department Name : " +department);
							
							Department departmentObj = departmentRepository.findByName(department);
							ProjectDepartmentMap projectDeptMap = new ProjectDepartmentMap();
							projectDeptMap.setProjectId(projectDbResponse.getProjectId());
							projectDeptMap.setDeptId(departmentObj.getDeptId());
							ProjectDepartmentMap projDeptMapDbResponse = projectDepartmentMapRepository.save(projectDeptMap);
							
							if(projDeptMapDbResponse != null) {
								responseBuilder.append("Project department Mapping response : success " + "projDept Mapp Id" + projDeptMapDbResponse.getProjectDepartmentMapId());
							}else {
								responseBuilder.append("Project department Mapping response : Failed");
							}
						}
						
						poProjectSyncDTO.getTeamList().forEach((teamObj) -> {
							Employee teamLeadObj = null;
							if(teamObj.getPoTeamLeadId() != null && teamObj.getPoTeamLeadId() != "") {
								teamLeadObj = getEmployeeByEmployeementId(teamObj.getPoTeamLeadId());
							}
//							Long deptId = null;
//							Long hodId = null;
//							List<Object[]> departmentObj = employeeRepository.getDepartmentByEmployeementId(Long.parseLong(teamObj.getTeamMemberList()[0].split("-")[1]));
//							if(departmentObj != null) {
//								for(Object[] deptObj: departmentObj) {
//									deptId = deptObj[3] != null ? Long.parseLong(deptObj[3].toString()) : null;
//									hodId = deptObj[4] != null ? Long.parseLong(deptObj[4].toString()) : null;
//								}
//							}
							// create new team
							
							requestBuilder.append("Create New Team : " + "Project Id : " + projectDbResponse.getProjectId() +  " Team Name : " + teamObj.getTeamName() + " PoTeam Id : " + teamObj.getPoTeamId());
							
							StringBuilder deptList = new StringBuilder("");
							for(String department: teamObj.getDepartmentList()) {
								Department deptObj = departmentRepository.findByName(department);
								if(deptObj != null) {
									deptList.append(deptObj.getDeptId()).append(",");
								}
							}
							Employee teamCreatedByObj = getEmployeeByEmployeementId(teamObj.getCreatedBy());
							
							Team newTeamObj = new Team();
							newTeamObj.setIsActive("Y");
							newTeamObj.setPoTeamId(teamObj.getPoTeamId());
							newTeamObj.setProjectId(projectDbResponse.getProjectId());
							newTeamObj.setTeamLeadId(teamLeadObj != null ? teamLeadObj.getEmpId() : null);
							newTeamObj.setTeamName(teamObj.getTeamName());
							newTeamObj.setTeamLeadName(teamLeadObj != null ? teamLeadObj.getName() : null);
							newTeamObj.setDescription(teamObj.getDescription());
							newTeamObj.setDeptIds(deptList.toString());
							newTeamObj.setCreatedBy(teamCreatedByObj.getEmpId());
							newTeamObj.setCreatedOn(new Timestamp(System.currentTimeMillis())); 
							Team teamDbResponse = teamRepository.save(newTeamObj);
							
							if(teamDbResponse != null) {
								
								responseBuilder.append("New Team Created resposne : success " + " team Id : " + teamDbResponse.getTeamId());
								
								List<EmployeeTeamMap> mapList = new ArrayList<EmployeeTeamMap>();
								
								// Add teamLead
								EmployeeTeamMap defaultMemberMap = new EmployeeTeamMap();
								defaultMemberMap.setActive(1l);
								defaultMemberMap.setEmpId(teamLeadObj != null ? teamLeadObj.getEmpId() : null);
								defaultMemberMap.setTeamId(teamDbResponse.getTeamId());
								defaultMemberMap.setEmployeeRole("TeamLead");
								mapList.add(defaultMemberMap);
								
								// Add Project Manager
								defaultMemberMap = new EmployeeTeamMap();
								defaultMemberMap.setActive(1l);
//								defaultMemberMap.setEmpId(managerObj.getEmpId());
								defaultMemberMap.setTeamId(teamDbResponse.getTeamId());
								defaultMemberMap.setEmployeeRole("Manager");
								mapList.add(defaultMemberMap);
								
								List<Long> deptIds = new ArrayList<>();
								// Add HOD of selected Department
								for(String department: teamObj.getDepartmentList()) {
									Department deptObj = departmentRepository.findByName(department);
									deptIds.add(deptObj.getDeptId());
									if(deptObj != null) {
										defaultMemberMap = new EmployeeTeamMap();
										defaultMemberMap.setActive(1l);
										defaultMemberMap.setEmpId(deptObj.getHodId());
										defaultMemberMap.setTeamId(teamDbResponse.getTeamId());
										defaultMemberMap.setEmployeeRole("HOD");
										mapList.add(defaultMemberMap);
									}
								}
								
								// Add team member in team
								List<Department> deptsObj = departmentRepository.findByDeptIdIn(deptIds);
								for(String teamMember: teamObj.getTeamMemberList()) {
									Employee teamMemberObj = getEmployeeByEmployeementId(teamMember);
									EmployeeTeamMap newEmpTeamMap = new EmployeeTeamMap();
									
									// check if HOD, Manager, TeamLead already added
									if(( (teamLeadObj.getEmpId() == null) || (teamLeadObj.getEmpId() != null && !teamLeadObj.getEmpId().equals(teamMemberObj.getEmpId())) 
//											) && !managerObj.getEmpId().equals(teamMemberObj.getEmpId())
											) && ( (deptsObj.isEmpty()) || (!deptsObj.isEmpty() && 
													!deptsObj.stream().anyMatch(o -> teamMemberObj.getEmpId().equals(o.getHodId()))) )) {
										
										requestBuilder.append("Add Team Member : " + "Member EmpId :" + teamMemberObj.getEmpId() + " Team Id : " + teamDbResponse.getTeamId());
										
										newEmpTeamMap.setActive(1l);
										newEmpTeamMap.setEmpId(teamMemberObj.getEmpId());
										newEmpTeamMap.setTeamId(teamDbResponse.getTeamId());
										newEmpTeamMap.setEmployeeRole("Employee");
										mapList.add(newEmpTeamMap);
									}	
								}
								List<EmployeeTeamMap> teamMemberDbResponse = employeeTeamMapRepository.saveAll(mapList);
								
								// Add default activity
								Activity newActivityCreated = null;
								if(!teamMemberDbResponse.isEmpty()) {
									
									responseBuilder.append("New Team Member addtion resposne : success");
									requestBuilder.append("Add Default Activity : ");
									
									for(String department: teamObj.getDepartmentList()) {
										Department deptObj = departmentRepository.findByName(department);
										if(deptObj != null) {
											List<ActivityTemplate> activityTemplate = activityTemplateRepository.getByDeptId(deptObj.getDeptId());
											if(!activityTemplate.isEmpty()) {
												
												for(ActivityTemplate activityObject: activityTemplate) {
													Activity newActivity = new Activity();

													newActivity.setActivity(activityObject.getTemplateActivity());
													newActivity.setTeamId(teamDbResponse.getTeamId());
													newActivity.setEmployeeRole(activityObject.getEmployeeRole());
													newActivity.setDeptIds(activityObject.getDeptId().toString());
													newActivity.getCommonProperty().setCreatedBy(4l);

													newActivityCreated = activitiesRepository.save(newActivity);
												}
											}
										}
									}
								}
								if(newActivityCreated != null) {
									
									responseBuilder.append("Add default activity response : success " + " Response message : Project & Team created successfully.");
									
									response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
									response.setServiceResponse("Project & Team created successfully.");
								}else {
									
									responseBuilder.append("Add default activity response : success " + " Response message : Project & Team created successfully, but default activities are not mapped");
									
									response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
									response.setServiceResponse("Project & Team created successfully, but default activities are not mapped");
								}
							}else {
								
								responseBuilder.append("Create New Team resposne : failed");
								
								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
								response.setServiceResponse("Unable to create new team.");
							}
						});
					}else {
						
						responseBuilder.append("Create New Project response : failed ");
						
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Unable to create new Project.");
					}
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(requestBuilder.toString());
		apiLogInfo.setApiResponse(responseBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse getSyncableProject() {
		ServiceResponse response = new ServiceResponse();
		
		try {
			
			List<Project> syncableProject = projectRepository.findBySyncProject("false");
			List<SyncableProjectDTO> dtoList = new ArrayList<SyncableProjectDTO>();
			
			if(syncableProject != null) {
				
				syncableProject.forEach((object -> {
					SyncableProjectDTO dto = new SyncableProjectDTO();
					
					dto.setProjectName(object.getProjectName());
					dtoList.add(dto);
				}));
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Syncable project found.");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse checkProjectName(ProjectDTO projectDto) {
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("CheckProjectName");
        apiLogInfo.setApiUrl("/api/checkProjectName");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("ProjectName : " + projectDto.getProjectName());
		try {
			
			Project checkProjectName = projectRepository.findByProjectName(projectDto.getProjectName());
			
			if(checkProjectName != null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project Name already exist.");
				System.out.println(" Project  exist"+checkProjectName);
                apiLogInfo.setApiResponse("Project Name already Exists ");			
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}else if(checkProjectName == null){
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                apiLogInfo.setApiResponse("Project Exists");			
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}
			
		}catch(Exception e) {
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

	public ServiceResponse getAllMyProjectByEmpId(ProjectDTO projectDto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("");
		apiLogInfo.setApiUrl("/api/getAllMyProjectByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("EmpId : " + projectDto.getEmpId());
		try {
			
            List<Object[]> projectList = projectRepository.getAllMyProjectByEmpId(projectDto.getEmpId());
			
			if(projectList != null) {
				List<ProjectDTO> dtoList = new ArrayList<ProjectDTO>();
				
				projectList.forEach((object) -> {
					ProjectDTO dto = new ProjectDTO();
					
					dto.setProjectId(object[0] != null ? Integer.valueOf(object[0].toString()) : null);
					dto.setProjectName(object[1] != null ? object[1].toString() : null);
					dto.setState(object[2] != null ? object[2].toString() : null);
					dto.setDepartmentName(object[4] != null ? object[4].toString() : null);
					
					dtoList.add(dto);
				});
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("ProjectList:" + dtoList.size());			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Projects found.");
				apiLogInfo.setApiResponse("No Projects found");			
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

        apiLogInfo.setApiRequest(logBuilder.toString());
        logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	private String formatDate(String dateTime) {
	    if (dateTime == null || dateTime.isEmpty()) {
	        return null;
	    }
	    return dateTime.split("T")[0]; 
	}
	
	public Long getCurrentUserId() {
		String sessionToken = httpRequest.getHeader("Authorization");
		if (sessionToken != null) {
			sessionToken = sessionToken.substring(7);
		}
		UserSession existingUserSession = userSessionRepo.findBySessionKey(sessionToken);
		return existingUserSession != null ? existingUserSession.getEmpId() : null;
	}
	
	@Transactional
	public ServiceResponse getProjectCloneFromPoPortal() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getProjectCloneFromPoPortal");
		apiLogInfo.setApiUrl("/api/getProjectCloneFromPoPortal");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		try {
			ServiceResponse serviceResponseTemp = poPortalAPIService.getAllProjectsFromPoPortal();
			if (serviceResponseTemp != null && !serviceResponseTemp.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS)) {
				response.setServiceStatus(serviceResponseTemp.getServiceStatus());
				response.setServiceResponse(serviceResponseTemp.getServiceResponse());
				apiLogInfo.setApiStatus(serviceResponseTemp.getServiceStatus());
				apiLogInfo.setApiResponse(serviceResponseTemp.getServiceResponse().toString());
			}
			List<ProjectPoPortalDTO> list = (List<ProjectPoPortalDTO>) serviceResponseTemp.getServiceResponse();
			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("No projects found from Shankh Portal API to sync.");
				logBuilder.append("Sync completed successfully with 0 projects.");
			} else {
				for (ProjectPoPortalDTO dto : list) {
					updateProject(dto,logBuilder);
				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Successfully synced project details from PoPortal.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setApiResponse("Successfully updated projects from PoPortal.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Failed to sync project details from PoPortal.");
			apiLogInfo.setApiResponse("Failed to sync project details from PoPortal.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	private void updateProject(ProjectPoPortalDTO dto, StringBuilder logBuilder) {
		try {
			Project project = projectRepository.findByPoProjectId(dto.getId());
			if (project == null) {
				logBuilder.append("Project not found for PoProjectId: ").append(dto.getId()).append("\n");
				return;
			}
			updateProjectBasicDetails(project, dto);
			updateProjectDepartments(project, dto.getDepartment(), logBuilder); // Department Mapping
			updateProjectClient(project, dto, logBuilder); // Client Mapping
			projectRepository.save(project);
			logBuilder.append("Updated Project: ID=").append(dto.getId()).append(", PoNo=").append(dto.getPoNo()).append("\n");
		} catch (Exception ex) {
			ex.printStackTrace();
			logBuilder.append("Error updating project ID: ").append(dto.getId()).append(" - ").append(ex.getMessage()).append("\n");
		}
	}

	private void updateProjectBasicDetails(Project project, ProjectPoPortalDTO dto) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		project.setProjectName(dto.getName());
		project.setPoProjectType(dto.getProjectType());
		project.setPoStartDate(dateFormat.format(dto.getStartDate()));
		project.setPoEndDate(dateFormat.format(dto.getEndDate()));
		project.setStatus(dto.getStatus());
		project.setPoNo(dto.getPoNo());
		project.setApmosysRM(dto.getApmosysRM());
		project.setApmosysRmEmail(dto.getApmosysRmEmail());
		project.setIsRenewable(dto.getIsRenewable());
		project.setClientRM(dto.getClientRM());
	}

	private void updateProjectDepartments(Project project, List<String> departmentList, StringBuilder logBuilder) {
		if (departmentList == null || departmentList.isEmpty())
			return;

		List<String> deptIds = departmentList.stream().map(deptName -> {
			Department deptEntity = departmentRepository.findByName(deptName);
			if (deptEntity != null)
				return deptEntity.getDeptId().toString();
			logBuilder.append("No Dept Id fetched for dept: ").append(deptName).append("\n");
			return null;
		}).filter(Objects::nonNull).collect(Collectors.toList());

		if (!deptIds.isEmpty()) {
			project.setDeptId(String.join(", ", deptIds));
		}
	}

	private void updateProjectClient(Project project, ProjectPoPortalDTO dto, StringBuilder logBuilder) {
		String clientName = dto.getClientName();
		Integer clientId = null;

		Optional<Client> clientOpt = clientsRepository.findByClientName(clientName);
		if (clientOpt.isPresent()) {
			clientId = clientOpt.get().getClientId();
		} else {
			Client newClient = new Client();
			newClient.setClientName(clientName);
			Client savedClient = clientsRepository.save(newClient);
			if (savedClient != null) {
				clientId = savedClient.getClientId();
				List<ClientLocation> locations = dto.getClientLocation().stream().map(location -> {
					ClientLocation cl = new ClientLocation();
					cl.setClientId(savedClient.getClientId());
					cl.setClientLocation(location);
					return cl;
				}).collect(Collectors.toList());

				if (!dto.getClientLocation().contains("WFH")) {
					ClientLocation wfh = new ClientLocation();
					wfh.setClientId(clientId);
					wfh.setClientLocation("WFH");
					locations.add(wfh);
				}
				clientLocationRepository.saveAll(locations);
				logBuilder.append("Client and locations stored for: ").append(clientName).append("\n");
			}
		}
		project.setClientId(clientId);
	}

	
	public ServiceResponse poProjectTimesheetSync(Set<Long> poProjectIdList) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/poProjectTimesheetSync");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		ApiLog initialLog = null;
		String exceptionDetailsForLog = null;
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		String sourceSystem = httpRequest.getRequestURL().toString();
		try {
			initialLog = apiLogUtility.startLog(poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest), "poProjectTimesheetSync", "PoPortal", null, httpRequest);
			
			if (poProjectIdList == null || poProjectIdList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("PoprojectId's are empty");
				response.setServiceError("PoprojectId's are empty");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setLogLevel("ERROR");	
				return response;
			}
			List<Object[]> poProjectTimesheetSyncDTOObjectList = projectRepository.poProjectTimesheetSync(poProjectIdList);
			if (poProjectTimesheetSyncDTOObjectList == null || poProjectTimesheetSyncDTOObjectList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(null);
				apiLogInfo.setApiResponse("Project Info not Found");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				finalHttpStatusCode = HttpStatus.NOT_FOUND.value();
//				throw new DataNotFoundException("Project Details Not Found.");
			} else {
				List<PoProjectTimesheetSyncDTO> poProjectTimesheetSyncDTOList = mapObjectToPoProjectTimesheetSyncDTO(poProjectTimesheetSyncDTOObjectList);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(poProjectTimesheetSyncDTOList);
				apiLogInfo.setApiResponse("ProjectInfoList: " + poProjectTimesheetSyncDTOList.size());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				finalHttpStatusCode = HttpStatus.OK.value();
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(e.getMessage());
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			exceptionDetailsForLog = e.toString();
		} finally {
			if (initialLog != null) {
				apiLogUtility.endLog(initialLog.getId(), sourceSystem ,finalHttpStatusCode, exceptionDetailsForLog, httpRequest);
			}
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	private List<PoProjectTimesheetSyncDTO> mapObjectToPoProjectTimesheetSyncDTO(List<Object[]> poProjectTimesheetSyncDTOObjectList) {
		List<PoProjectTimesheetSyncDTO> poProjectTimesheetSyncDTOList = new ArrayList<>();
		Map<Long, PoProjectTimesheetSyncDTO> projectMap = new HashMap<>();
			for (Object[] object : poProjectTimesheetSyncDTOObjectList) {
				try {
					Long poProjectId = parseLong(object[0]);
					if (poProjectId == null)
						continue;

					PoProjectTimesheetSyncDTO projectDTO = projectMap.computeIfAbsent(poProjectId, id -> {
						PoProjectTimesheetSyncDTO dto = new PoProjectTimesheetSyncDTO();
						dto.setPoProjectId(id);
						dto.setIshineStoredProjectName(toStr(object[1]));
						dto.setIshineStoredPoNo(toStr(object[2]));
						dto.setTeamDetails(new ArrayList<>());
						return dto;
					});

					Long teamId = parseLong(object[3]);
					if (teamId != null) {
						List<PoTeamTimesheetSyncDTO> teamList = projectDTO.getTeamDetails();
						PoTeamTimesheetSyncDTO teamDTO = teamList.stream().filter(t -> teamId.equals(t.getTeamId()))
								.findFirst().orElseGet(() -> {
									PoTeamTimesheetSyncDTO newTeam = new PoTeamTimesheetSyncDTO();
									newTeam.setTeamId(teamId);
									newTeam.setTeamName(toStr(object[4]));
									newTeam.setEmployeesMapped(new ArrayList<>());
									teamList.add(newTeam);
									return newTeam;
								});

						if (object[5] != null) {
							PoEmployeeTimesheetSyncDTO empDTO = new PoEmployeeTimesheetSyncDTO();
							empDTO.setEmployeementId(parseLong(object[6]));
							empDTO.setEmployeeName(toStr(object[7]));
							empDTO.setCurrentStatus(toStr(object[8]));
							empDTO.setStartDate(toTimestamp(object[9]));
							empDTO.setEndDate(toTimestamp(object[10]));
							empDTO.setLastTimesheetFilledPoProjectId(parseLong(object[11]));
							empDTO.setLastTimesheetFilledProjectName(toStr(object[12]));
							empDTO.setIsInternal(object[11] != null);
							teamDTO.getEmployeesMapped().add(empDTO);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (!projectMap.isEmpty()) {
				poProjectTimesheetSyncDTOList = new ArrayList<>(projectMap.values());
			}
		return poProjectTimesheetSyncDTOList;
	}

	private Long parseLong(Object obj) {
		return (obj != null) ? Long.parseLong(obj.toString()) : null;
	}

	private String toStr(Object obj) {
		return (obj != null) ? obj.toString() : null;
	}

	private Timestamp toTimestamp(Object obj) {
		return (obj instanceof Timestamp) ? (Timestamp) obj : null;
	}

	public String buildDynamicQuery(GetEmployeeProjectReportPayloadDTO dto) {
        String category = dto.getCategory();
        String poProjectType = dto.getPoProjectType();
        String flag = dto.getFlag();
        List<String> billableType = dto.getBillableType();
        List<Long> deptIds = dto.getDeptId();
        Boolean hideMaternityLeaveEmps = dto.getHideMaternityLeaveEmps();

        StringBuilder query = new StringBuilder();

        if ("P".equalsIgnoreCase(dto.getReport())) {
            // === Project Query ===
            query.append("SELECT p.project_id, p.po_project_id, p.project_name, p.project_manager_id, ep.name as projManager, ")
                 .append("p.po_no, p.po_project_type, p.po_start_date, p.po_end_date, p.clientrm, p.apmosysrm, ")
                 .append("t.team_id, team_name, etm.emp_id, e.name, etm.start_date, j.name as jobRole, d.name as deptName, e.billable_type, ")
                 .append("e.billable, e.mobile_no, e.email, CASE \n"
                 		+ "    WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-', e.employeement_id)\n"
                 		+ "    WHEN e.is_consultant = 'true' THEN CONCAT('CS-', e.employeement_id)\n"
                 		+ "    ELSE CONCAT('A-', e.employeement_id)\n"
                 		+ "  END AS prefixed_employeementId ")
                 .append("FROM projects p ")
                 .append("INNER JOIN teams t ON t.project_id = p.project_id ")
                 .append("INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id ")
                 .append("INNER JOIN employee e ON etm.emp_id = e.emp_id ")
                 .append("INNER JOIN job_role j ON e.job_role_id = j.job_role_id ")
                 .append("INNER JOIN department d ON d.dept_id = j.dept_id ")
                 .append("INNER JOIN employee ep ON p.project_manager_id = ep.emp_id "
                 		+ "LEFT JOIN (\n"
                 		+ "			select distinct e.emp_id as emp_id, e.name as name, e.email as email\n"
                 		+ "				  ,case when el.emp_id is null then 'No' else 'Yes' end as On_Maternity_Leave\n"
                 		+ "			from employee e \n"
                 		+ "			left join employee_leave el \n"
                 		+ "				on el.emp_id = e.emp_id \n"
                 		+ "				and leave_status_id in (1,2) \n"
                 		+ "				and manager_approval_status = 'Approved' \n"
                 		+ "				and leave_type_master_id = 5 \n"
                 		+ "				and curdate() between date(el.from_date) and date(el.to_date) \n"
                 		+ "		) eld on eld.emp_id = e.emp_id \n")
                 .append("WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false' and e.emp_id not between 1 and 6  ")
                 .append(buildInnerWhereClause(poProjectType, flag))
                 .append(buildOuterWhereClause(billableType, deptIds, hideMaternityLeaveEmps));;
            
        } else if ("EC".equalsIgnoreCase(dto.getReport())) {
            // === Employee Consolidated Query ===
            query.append("SELECT e.emp_id, e.employeement_id, e.name, e.email, e.mobile_no, ")
                 .append("e.manager_id, m.name as ManagerName, e.employmentstatus, e.billable, e.billable_type, ")
                 .append("emp_proj_client.team_id, emp_proj_client.team_name, emp_proj_client.project_id, ")
                 .append("emp_proj_client.project_name, emp_proj_client.po_start_date, emp_proj_client.po_end_date, ")
                 .append("emp_proj_client.po_no, emp_proj_client.client_name, emp_proj_client.client_location, e.work_location, ")
                 .append("e.total_experience, d.dept_id, d.name as departmentName, emp_proj_client.po_project_type, j.name as jobrole, ")
                 .append("emp_proj_client.po_project_id, eppm.primary_project_name, eppm.primary_project_id, emp_proj_client.clientrm, ")
                 .append("emp_proj_client.apmosysrm, emp_proj_client.effective_start_date, emp_proj_client.effective_end_date, CASE \n"
                 		+ "    WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-', e.employeement_id)\n"
                 		+ "    WHEN e.is_consultant = 'true' THEN CONCAT('CS-', e.employeement_id)\n"
                 		+ "    ELSE CONCAT('A-', e.employeement_id)\n"
                 		+ "  END AS prefixed_employeementId FROM employee e ")
                 .append("INNER JOIN job_role j ON j.job_role_id = e.job_role_id ")
                 .append("INNER JOIN department d ON d.dept_id = j.dept_id ")
                 .append("INNER JOIN employee m ON e.manager_id = m.emp_id ")
                 .append("LEFT JOIN emp_primary_project_mapping eppm ON eppm.emp_id = e.emp_id ")
                 .append("LEFT JOIN (\n"
                 		+ "			select distinct e.emp_id as emp_id, e.name as name, e.email as email\n"
                 		+ "				  ,case when el.emp_id is null then 'No' else 'Yes' end as On_Maternity_Leave\n"
                 		+ "			from employee e \n"
                 		+ "			left join employee_leave el \n"
                 		+ "				on el.emp_id = e.emp_id \n"
                 		+ "				and leave_status_id in (1,2) \n"
                 		+ "				and manager_approval_status = 'Approved' \n"
                 		+ "				and leave_type_master_id = 5 \n"
                 		+ "				and curdate() between date(el.from_date) and date(el.to_date) \n"
                 		+ "		) eld on eld.emp_id = e.emp_id \n")
                 .append("LEFT JOIN ( ")
                 .append("    SELECT etm.emp_id, GROUP_CONCAT(DISTINCT p.project_name ORDER BY p.project_id) AS project_name, ")
                 .append("           GROUP_CONCAT(DISTINCT p.project_id ORDER BY p.project_id) AS project_id, ")
                 .append("           GROUP_CONCAT(DISTINCT p.po_start_date ORDER BY p.project_id) AS po_start_date, ")
                 .append("           GROUP_CONCAT(DISTINCT p.po_end_date ORDER BY p.project_id) AS po_end_date, ")
                 .append("           GROUP_CONCAT(DISTINCT p.po_no ORDER BY p.project_id) AS po_no, ")
                 .append("           GROUP_CONCAT(DISTINCT p.po_project_type ORDER BY p.project_id) AS po_project_type, ")
                 .append("           GROUP_CONCAT(DISTINCT c.client_name ORDER BY p.project_id) AS client_name, ")
                 .append("           GROUP_CONCAT(DISTINCT cl.client_location ORDER BY p.project_id) AS client_location, ")
                 .append("           GROUP_CONCAT(DISTINCT t.team_name ORDER BY p.project_id) AS team_name, ")
                 .append("           GROUP_CONCAT(DISTINCT t.team_id ORDER BY p.project_id) AS team_id, ")
                 .append("           GROUP_CONCAT(DISTINCT p.po_project_id ORDER BY p.project_id) AS po_project_id, ")
                 .append("           GROUP_CONCAT(DISTINCT p.clientrm ORDER BY p.project_id) AS clientrm, ")
                 .append("           GROUP_CONCAT(DISTINCT p.apmosysrm ORDER BY p.project_id) AS apmosysrm, ")
                 .append("           GROUP_CONCAT(DISTINCT etm.start_date ORDER BY p.project_id) AS effective_start_date, ")
                 .append("           GROUP_CONCAT(DISTINCT etm.end_date ORDER BY p.project_id) AS effective_end_date ")
                 .append("    FROM employee_team_mapping etm ")
                 .append("    LEFT JOIN teams t ON t.team_id = etm.team_id ")
                 .append("    LEFT JOIN projects p ON p.project_id = t.project_id ")
                 .append("    LEFT JOIN clients c ON c.client_id = p.client_id ")
                 .append("    LEFT JOIN client_locations cl ON cl.client_id = p.client_id ")
                 .append("    WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false' ")
                 .append(buildInnerWhereClause(poProjectType, flag))
                 .append("    GROUP BY etm.emp_id ")
                 .append(") emp_proj_client ON emp_proj_client.emp_id = e.emp_id ")
                 .append("WHERE e.employmentstatus != 'InActive' AND emp_proj_client.project_id IS NOT NULL and e.emp_id not between 1 and 6 ")
                 .append(buildOuterWhereClause(billableType, deptIds, hideMaternityLeaveEmps));  

        } else if ("E".equalsIgnoreCase(dto.getReport())) {
            // === Employee Query ===
            query.append("SELECT e.emp_id, e.employeement_id, e.name, e.email, e.mobile_no, ")
	             .append("e.manager_id, m.name as ManagerName, e.employmentstatus, e.billable, e.billable_type, ")
	             .append("t.team_id, t.team_name, p.project_id, ")
	             .append("p.project_name, p.po_start_date, p.po_end_date, ")
	             .append("p.po_no, c.client_name, cl.client_location, e.work_location, ")
	             .append("e.total_experience, d.dept_id, d.name as departmentName, p.po_project_type, j.name as jobrole, ")
	             .append("p.po_project_id, eppm.primary_project_name, eppm.primary_project_id, p.clientrm, ")
	             .append("p.apmosysrm, etm.start_date as effective_start_date, etm.end_date as effective_end_date, CASE \n"
	             		+ "    WHEN e.is_apmosys_product = 'true' THEN CONCAT('AP-', e.employeement_id)\n"
	             		+ "    WHEN e.is_consultant = 'true' THEN CONCAT('CS-', e.employeement_id)\n"
	             		+ "    ELSE CONCAT('A-', e.employeement_id)\n"
	             		+ "  END AS prefixed_employeementId FROM employee e ")
                 .append("INNER JOIN employee_team_mapping etm ON etm.emp_id = e.emp_id ")
                 .append("LEFT JOIN teams t ON t.team_id = etm.team_id ")
                 .append("LEFT JOIN projects p ON p.project_id = t.project_id ")
                 .append("INNER JOIN job_role j ON j.job_role_id = e.job_role_id ")
                 .append("INNER JOIN department d ON d.dept_id = j.dept_id ")
                 .append("INNER JOIN employee m ON m.emp_id = e.manager_id ")
                 .append("INNER JOIN client_locations cl ON cl.client_id = p.client_id ")
                 .append("INNER JOIN clients c ON c.client_id = p.client_id ")
                 .append("LEFT JOIN emp_primary_project_mapping eppm ON eppm.emp_id = e.emp_id "
                 		+ "LEFT JOIN (\n"
                 		+ "			select distinct e.emp_id as emp_id, e.name as name, e.email as email\n"
                 		+ "				  ,case when el.emp_id is null then 'No' else 'Yes' end as On_Maternity_Leave\n"
                 		+ "			from employee e \n"
                 		+ "			left join employee_leave el \n"
                 		+ "				on el.emp_id = e.emp_id \n"
                 		+ "				and leave_status_id in (1,2) \n"
                 		+ "				and manager_approval_status = 'Approved' \n"
                 		+ "				and leave_type_master_id = 5 \n"
                 		+ "				and curdate() between date(el.from_date) and date(el.to_date) \n"
                 		+ "		) eld on eld.emp_id = e.emp_id \n")
                 .append("WHERE e.employmentstatus != 'InActive' AND etm.active != 0 AND t.is_active != 'N' AND p.active != 'false' AND p.project_id IS NOT NULL and e.emp_id not between 1 and 6 ")
	             .append(buildInnerWhereClause(poProjectType, flag))
	             .append(buildOuterWhereClause(billableType, deptIds, hideMaternityLeaveEmps));
        } else {
        	System.out.println("query not generated!");
        }
        
        System.out.println("query.toString() "+query.toString());
        return query.toString();
    }
	
	public String buildInnerWhereClause(String poProjectType, String flag) {
	    StringBuilder innerWhere = new StringBuilder();
	    
	    if (poProjectType != null && !poProjectType.isEmpty()) {
	        if ("Internal".equalsIgnoreCase(poProjectType)) {
	            innerWhere.append(" AND po_project_type IS NULL ");
	        } else {
	            innerWhere.append(" AND po_project_type = '").append(poProjectType).append("' ");
	        }
	    }

	    if (flag != null && !flag.isEmpty()) {
	        if ("Active".equalsIgnoreCase(flag)) {
	            innerWhere.append(" AND p.po_end_date >= CURRENT_DATE ");
	        } else if ("Inactive".equalsIgnoreCase(flag)) {
	            innerWhere.append(" AND p.po_end_date < CURRENT_DATE ");
	        } else {
	            innerWhere.append(" AND flag = '").append(flag).append("' "); // Default case if flag is not Active/Inactive
	        }
	    }

	    return innerWhere.toString();
	}

	public String buildOuterWhereClause(List<String> billableType, List<Long> deptIds, Boolean hideMaternityLeaveEmps) {
	    StringBuilder outerWhere = new StringBuilder();

	    if (billableType != null && !billableType.isEmpty()) {
	        outerWhere.append(" AND e.billable_type IN ('")
	                  .append(String.join("','", billableType)).append("') ");
	    }

	    if (deptIds != null && !deptIds.isEmpty()) {
	        outerWhere.append(" AND d.dept_id IN (")
	            	  .append(String.join(",", deptIds.stream().map(String::valueOf).collect(Collectors.toList())))
	                  .append(") ");
	    }
	    
	    if (hideMaternityLeaveEmps != null) {
	        outerWhere.append(" AND (( \n"
	        		+ hideMaternityLeaveEmps
	        		+ " = true) or ("
	        		+ hideMaternityLeaveEmps
	        		+ " != true and eld.On_Maternity_Leave = 'No')\n"
	        		+ "	)");
	    }

	    return outerWhere.toString();
	}
	
	public String buildProjectSummaryQuery(GetEmployeeProjectReportPayloadDTO dto, List<Long> deptId, Boolean hideMaternityLeaveEmps) {
	    StringBuilder query = new StringBuilder();

	    query.append("WITH AllPoProjectTypes AS (\n"
	    		+ "    SELECT 'Internal' AS po_project_type\n"
	    		+ "    UNION ALL\n"
	    		+ "    SELECT 'TNM'\n"
	    		+ "    UNION ALL\n"
	    		+ "    SELECT 'Fixed Cost'\n"
	    		+ "    UNION ALL\n"
	    		+ "    SELECT 'Monitoring' \n"
	    		+ "),\n"
	    		+ "AllBillableTypes AS (\n"
	    		+ "    SELECT 'Bench' AS billable_type\n"
	    		+ "    UNION ALL\n"
	    		+ "    SELECT 'Shadow'\n"
	    		+ "    UNION ALL\n"
	    		+ "    SELECT 'TNM'\n"
	    		+ "    UNION ALL\n"
	    		+ "    SELECT 'Fixed Cost'\n"
	    		+ "    UNION ALL\n"
	    		+ "    SELECT 'InternalRNDProducts'\n"
	    		+ "),\n"
	    		+ "AllCombinations AS (\n"
	    		+ "    SELECT \n"
	    		+ "        apt.po_project_type, \n"
	    		+ "        abt.billable_type\n"
	    		+ "    FROM AllPoProjectTypes apt\n"
	    		+ "    CROSS JOIN AllBillableTypes abt\n"
	    		+ "),\n"
	    		+ "MainAgg AS (\n"
	    		+ "    SELECT \n"
	    		+ "        CASE WHEN po_project_type IS NULL THEN 'Internal' ELSE po_project_type END AS po_project_type,\n"
	    		+ "        e.billable_type, COUNT(DISTINCT e.emp_id) AS total_emp, \n"
	    		+ "        COUNT(DISTINCT p.po_project_id) AS total_projects\n"
	    		+ "    FROM projects p\n"
	    		+ "    INNER JOIN teams t ON t.project_id = p.project_id \n"
	    		+ "    INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
	    		+ "    INNER JOIN employee e ON etm.emp_id = e.emp_id\n"
	    		+ "    INNER JOIN job_role j ON e.job_role_id = j.job_role_id\n"
	    		+ "    INNER JOIN department d ON d.dept_id = j.dept_id \n"
	    		+ "    LEFT JOIN employee ep ON p.project_manager_id = ep.emp_id\n"
	    		+ "	   LEFT JOIN (\n"
	    		+ "			select distinct e.emp_id as emp_id, e.name as name, e.email as email\n"
	    		+ "				  ,case when el.emp_id is null then 'No' else 'Yes' end as On_Maternity_Leave\n"
	    		+ "			from employee e \n"
	    		+ "			left join employee_leave el \n"
	    		+ "				on el.emp_id = e.emp_id \n"
	    		+ "				and leave_status_id in (1,2) \n"
	    		+ "				and manager_approval_status = 'Approved' \n"
	    		+ "				and leave_type_master_id = 5 \n"
	    		+ "				and curdate() between date(el.from_date) and date(el.to_date) \n"
	    		+ "		) eld on eld.emp_id = e.emp_id \n"
	    		+ "    WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false' and e.emp_id not between 1 and 6 ");

	    if (dto != null) {
	        query.append(buildInnerWhereClause(dto.getPoProjectType(), dto.getFlag()))
	             .append(buildOuterWhereClause(dto.getBillableType(), deptId, hideMaternityLeaveEmps));
	    } else {
	        query.append(buildInnerWhereClause(null, null))
	             .append(buildOuterWhereClause(null, deptId, hideMaternityLeaveEmps));
	    }

	    query.append("GROUP BY\n"
	    		+ "        CASE WHEN po_project_type IS NULL THEN 'Internal' ELSE po_project_type END, e.billable_type\n"
	    		+ "),\n"
	    		+ "SubAgg AS (\n"
	    		+ "    SELECT \n"
	    		+ "        CASE WHEN po_project_type IS NULL THEN 'Internal' ELSE po_project_type END AS po_project_type,\n"
	    		+ "        COUNT(DISTINCT e.emp_id) AS total_emp_per_project_type, \n"
	    		+ "        COUNT(DISTINCT p.po_project_id) AS total_projects_per_po_project\n"
	    		+ "    FROM projects p\n"
	    		+ "    INNER JOIN teams t ON t.project_id = p.project_id \n"
	    		+ "    INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id\n"
	    		+ "    INNER JOIN employee e ON etm.emp_id = e.emp_id\n"
	    		+ "    INNER JOIN job_role j ON e.job_role_id = j.job_role_id\n"
	    		+ "    INNER JOIN department d ON d.dept_id = j.dept_id \n"
	    		+ "    LEFT JOIN employee ep ON p.project_manager_id = ep.emp_id\n"
	    		+ "	   LEFT JOIN (\n"
	    		+ "			select distinct e.emp_id as emp_id, e.name as name, e.email as email\n"
	    		+ "				  ,case when el.emp_id is null then 'No' else 'Yes' end as On_Maternity_Leave\n"
	    		+ "			from employee e \n"
	    		+ "			left join employee_leave el \n"
	    		+ "				on el.emp_id = e.emp_id \n"
	    		+ "				and leave_status_id in (1,2) \n"
	    		+ "				and manager_approval_status = 'Approved' \n"
	    		+ "				and leave_type_master_id = 5 \n"
	    		+ "				and curdate() between date(el.from_date) and date(el.to_date) \n"
	    		+ "		) eld on eld.emp_id = e.emp_id"
	    		+ "    WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false' and e.emp_id not between 1 and 6 ");

	    if (dto != null) {
	        query.append(buildInnerWhereClause(dto.getPoProjectType(), dto.getFlag()))
	             .append(buildOuterWhereClause(dto.getBillableType(), deptId, hideMaternityLeaveEmps));
	    }else {
	        query.append(buildInnerWhereClause(null, null))
            .append(buildOuterWhereClause(null, deptId,hideMaternityLeaveEmps));
	    }

	    query.append("GROUP BY \n"
	    		+ "        CASE WHEN po_project_type IS NULL THEN 'Internal' ELSE po_project_type END\n"
	    		+ ")\n"
	    		+ "SELECT \n"
	    		+ "    ac.po_project_type, \n"
	    		+ "    ac.billable_type,\n"
	    		+ "    COALESCE(ma.total_emp, 0) AS total_emp,\n"
	    		+ "    COALESCE(sa.total_emp_per_project_type, 0) AS total_emp_per_project_type, \n"
	    		+ "    COALESCE(ma.total_projects, 0) AS total_projects,\n"
	    		+ "    s(sa.total_projects_per_po_project, 0) AS total_projects_per_po_project\n"
	    		+ "FROM AllCombinations ac\n"
	    		+ "LEFT JOIN MainAgg ma \n"
	    		+ "    ON ac.po_project_type = ma.po_project_type \n"
	    		+ "    AND ac.billable_type = ma.billable_type\n"
	    		+ "LEFT JOIN SubAgg sa \n"
	    		+ "    ON ac.po_project_type = sa.po_project_type\n"
	    		+ "ORDER BY ac.po_project_type, ac.billable_type ");

	    return query.toString();
	}

	public Map<String, Map<String, Map<String, Object>>> getProjectSummary(GetEmployeeProjectReportPayloadDTO dto) {
	    List<String> allBillableTypes = Arrays.asList("Bench", "Fixed Cost", "InternalRNDProducts", "Shadow", "TNM");

	    Map<String, Map<String, EmployeeProjectSummaryDTO>> summaryMap = new HashMap<>();

	    if (dto.getPoProjectType() != null) {
	        String filteredQuery = buildProjectSummaryQuery(dto, dto.getDeptId(),dto.getHideMaternityLeaveEmps());
	        List<Object[]> filteredResults = entityManager.createNativeQuery(filteredQuery).getResultList();

	        for (Object[] row : filteredResults) {
	            String poType = row[0] != null ? row[0].toString() : "Internal";
	            String billableType = row[1] != null ? row[1].toString() : null;
	            Long totalEmp = row[2] != null ? Long.parseLong(row[2].toString()) : 0L;
	            Long totalEmpPerPoType = row[3] != null ? Long.parseLong(row[3].toString()) : 0L;
	            Long totalProjects = row[4] != null ? Long.parseLong(row[4].toString()) : 0L;
	            Long totalProjectsPerPoType = row[5] != null ? Long.parseLong(row[5].toString()) : 0L;

	            summaryMap.putIfAbsent(poType, new HashMap<>());
	            Map<String, EmployeeProjectSummaryDTO> innerMap = summaryMap.get(poType);

	            EmployeeProjectSummaryDTO dtoObj = new EmployeeProjectSummaryDTO();
	            dtoObj.setPoProjectType(poType);
	            dtoObj.setBillableType(billableType);
	            dtoObj.setTotalEmp(totalEmp);
	            dtoObj.setTotalEmpPerProjectType(totalEmpPerPoType);
	            dtoObj.setTotal_projects(totalProjects);
	            dtoObj.setTotal_projects_per_po_project(totalProjectsPerPoType);

	            innerMap.put(billableType, dtoObj);
	        }

	        for (String poType : summaryMap.keySet()) {
	            Map<String, EmployeeProjectSummaryDTO> innerMap = summaryMap.get(poType);
	            for (String billableType : allBillableTypes) {
	                if (!innerMap.containsKey(billableType)) {
	                    EmployeeProjectSummaryDTO emptyDto = new EmployeeProjectSummaryDTO();
	                    emptyDto.setPoProjectType(poType);
	                    emptyDto.setBillableType(billableType);
	                    emptyDto.setTotalEmp(0L);
	                    emptyDto.setTotal_projects(0L);
	                    emptyDto.setTotalEmpPerProjectType(0L);
	                    emptyDto.setTotal_projects_per_po_project(0L);
	                    innerMap.put(billableType, emptyDto);
	                }
	            }
	        }
	    }

	    Set<String> poTypesNeedingBaseData = new HashSet<>();
	    for (Map.Entry<String, Map<String, EmployeeProjectSummaryDTO>> entry : summaryMap.entrySet()) {
	        String poType = entry.getKey();
	        Map<String, EmployeeProjectSummaryDTO> billableMap = entry.getValue();

	        EmployeeProjectSummaryDTO anyDto = billableMap.values().stream().findFirst().orElse(null);
	        if (anyDto != null &&
	            (anyDto.getTotalEmpPerProjectType() == null || anyDto.getTotalEmpPerProjectType() == 0L) &&
	            (anyDto.getTotal_projects_per_po_project() == null || anyDto.getTotal_projects_per_po_project() == 0L)) {
	            poTypesNeedingBaseData.add(poType);
	        }
	    }

	    String baseQuery = buildProjectSummaryQuery(null, dto.getDeptId(),dto.getHideMaternityLeaveEmps());
	    List<Object[]> baseResults = entityManager.createNativeQuery(baseQuery).getResultList();

	    for (Object[] row : baseResults) {
	        String poType = row[0] != null ? row[0].toString() : "Internal";
	        String billableType = row[1] != null ? row[1].toString() : null;
	        Long totalEmp = row[2] != null ? Long.parseLong(row[2].toString()) : 0L;
	        Long totalEmpPerPoType = row[3] != null ? Long.parseLong(row[3].toString()) : 0L;
	        Long totalProjects = row[4] != null ? Long.parseLong(row[4].toString()) : 0L;
	        Long totalProjectsPerPoType = row[5] != null ? Long.parseLong(row[5].toString()) : 0L;

	        summaryMap.putIfAbsent(poType, new HashMap<>());
	        Map<String, EmployeeProjectSummaryDTO> innerMap = summaryMap.get(poType);

	        EmployeeProjectSummaryDTO summaryDto = innerMap.getOrDefault(billableType, new EmployeeProjectSummaryDTO());
	        summaryDto.setPoProjectType(poType);
	        summaryDto.setBillableType(billableType);

	        boolean shouldFillAll = poTypesNeedingBaseData.contains(poType);
	        if (shouldFillAll) {
	            summaryDto.setTotalEmp(totalEmp);
	            summaryDto.setTotal_projects(totalProjects);
	        } else {
	            if (summaryDto.getTotalEmp() == null || summaryDto.getTotalEmp() == 0L) {
	                summaryDto.setTotalEmp(totalEmp);
	            }
	            if (summaryDto.getTotal_projects() == null || summaryDto.getTotal_projects() == 0L) {
	                summaryDto.setTotal_projects(totalProjects);
	            }
	        }

	        if ((summaryDto.getTotalEmpPerProjectType() == null || summaryDto.getTotalEmpPerProjectType() == 0L) && totalEmpPerPoType > 0) {
	            summaryDto.setTotalEmpPerProjectType(totalEmpPerPoType);
	        }

	        if ((summaryDto.getTotal_projects_per_po_project() == null || summaryDto.getTotal_projects_per_po_project() == 0L)
	                && totalProjectsPerPoType > 0) {
	            summaryDto.setTotal_projects_per_po_project(totalProjectsPerPoType);
	        }

	        innerMap.put(billableType, summaryDto);
	    }

	    Map<String, Map<String, Map<String, Object>>> finalMap = new LinkedHashMap<>();
	    List<String> fixedPoTypes = Arrays.asList("Active", "Inactive", "Default");

	    for (String poType : summaryMap.keySet()) {
	        Map<String, EmployeeProjectSummaryDTO> billableMap = summaryMap.get(poType);

	        // Initialize empty buckets
	        for (String bucket : fixedPoTypes) {
	            Map<String, Object> empBucketMap = new LinkedHashMap<>();
	            Map<String, Object> projectBucketMap = new LinkedHashMap<>();

	            for (String billableType : allBillableTypes) {
	                empBucketMap.put(billableType, 0L);
	                projectBucketMap.put(billableType, 0L);
	            }

	            empBucketMap.put("totalEmpPerProjectType", 0L);
	            projectBucketMap.put("total_projects_per_po_project", 0L);

	            Map<String, Map<String, Object>> poTypeMap = new LinkedHashMap<>();
	            poTypeMap.put("Employee", empBucketMap);
	            poTypeMap.put("Project", projectBucketMap);

	            finalMap.put(poType + "." + bucket, poTypeMap);
	        }

	        // Determine flag and populate accordingly
	        String flag = dto.getFlag(); 
	        Map<String, Object> empMap = new LinkedHashMap<>();
	        Map<String, Object> projectMap = new LinkedHashMap<>();

	        if ("Active".equals(flag)) {
	            for (String billableType : allBillableTypes) {
	                EmployeeProjectSummaryDTO dtoObj = billableMap.getOrDefault(billableType, new EmployeeProjectSummaryDTO());
	                empMap.put(billableType, dtoObj.getTotalEmp() != null ? dtoObj.getTotalEmp() : 0L);
	                projectMap.put(billableType, dtoObj.getTotal_projects() != null ? dtoObj.getTotal_projects() : 0L);
	            }

	            empMap.put("totalEmpPerProjectType", billableMap.values().stream()
	                    .map(EmployeeProjectSummaryDTO::getTotalEmpPerProjectType)
	                    .filter(Objects::nonNull)
	                    .findFirst().orElse(0L));
	            projectMap.put("total_projects_per_po_project", billableMap.values().stream()
	                    .map(EmployeeProjectSummaryDTO::getTotal_projects_per_po_project)
	                    .filter(Objects::nonNull)
	                    .findFirst().orElse(0L));

	            finalMap.put(poType + ".Active", Map.of(
	                    "Employee", empMap,
	                    "Project", projectMap
	            ));

	            finalMap.put(poType + ".Inactive", Map.of(
	                    "Employee", new HashMap<String, Object>(),
	                    "Project", new HashMap<String, Object>()
	            ));
	            finalMap.put(poType + ".Default", Map.of(
	                    "Employee", new HashMap<String, Object>(),
	                    "Project", new HashMap<String, Object>()
	            ));

	        } else if ("Inactive".equals(flag)) {
	            for (String billableType : allBillableTypes) {
	                EmployeeProjectSummaryDTO dtoObj = billableMap.getOrDefault(billableType, new EmployeeProjectSummaryDTO());
	                empMap.put(billableType, dtoObj.getTotalEmp() != null ? dtoObj.getTotalEmp() : 0L);
	                projectMap.put(billableType, dtoObj.getTotal_projects() != null ? dtoObj.getTotal_projects() : 0L);
	            }

	            empMap.put("totalEmpPerProjectType", billableMap.values().stream()
	                    .map(EmployeeProjectSummaryDTO::getTotalEmpPerProjectType)
	                    .filter(Objects::nonNull)
	                    .findFirst().orElse(0L));
	            projectMap.put("total_projects_per_po_project", billableMap.values().stream()
	                    .map(EmployeeProjectSummaryDTO::getTotal_projects_per_po_project)
	                    .filter(Objects::nonNull)
	                    .findFirst().orElse(0L));

	            finalMap.put(poType + ".Inactive", Map.of(
	                    "Employee", empMap,
	                    "Project", projectMap
	            ));

	            finalMap.put(poType + ".Active", Map.of(
	                    "Employee", new HashMap<String, Object>(),
	                    "Project", new HashMap<String, Object>()
	            ));
	            finalMap.put(poType + ".Default", Map.of(
	                    "Employee", new HashMap<String, Object>(),
	                    "Project", new HashMap<String, Object>()
	            ));

	        } else {
	            for (String billableType : allBillableTypes) {
	                EmployeeProjectSummaryDTO dtoObj = billableMap.getOrDefault(billableType, new EmployeeProjectSummaryDTO());
	                empMap.put(billableType, dtoObj.getTotalEmp() != null ? dtoObj.getTotalEmp() : 0L);
	                projectMap.put(billableType, dtoObj.getTotal_projects() != null ? dtoObj.getTotal_projects() : 0L);
	            }

	            empMap.put("totalEmpPerProjectType", billableMap.values().stream()
	                    .map(EmployeeProjectSummaryDTO::getTotalEmpPerProjectType)
	                    .filter(Objects::nonNull)
	                    .findFirst().orElse(0L));
	            projectMap.put("total_projects_per_po_project", billableMap.values().stream()
	                    .map(EmployeeProjectSummaryDTO::getTotal_projects_per_po_project)
	                    .filter(Objects::nonNull)
	                    .findFirst().orElse(0L));

	            finalMap.put(poType + ".Default", Map.of(
	                    "Employee", empMap,
	                    "Project", projectMap
	            ));

	            finalMap.put(poType + ".Active", Map.of(
	                    "Employee", new HashMap<String, Object>(),
	                    "Project", new HashMap<String, Object>()
	            ));
	            finalMap.put(poType + ".Inactive", Map.of(
	                    "Employee", new HashMap<String, Object>(),
	                    "Project", new HashMap<String, Object>()
	            ));
	        }
	    }

	    return finalMap;
	}
	
	public ServiceResponse getEmployeeProjectReport(GetEmployeeProjectReportPayloadDTO dto) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("Employee Report");
	    apiLogInfo.setApiUrl("/api/getEmployeeProjectReport");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("Category: ").append(dto.getCategory()).append(", ");
	    logBuilder.append("Report Type: ").append(dto.getReport());

	    try {
	        String reportType = dto.getReport();
	        GetEmployeeProjectReportDTO reportDTO;
	        if (reportType == null) {
	            reportType = "E";
	        }

	        switch (reportType) {
	            case "E":
	            case "EC":
	                reportDTO = getEmployeeReport(dto);
	                break;
	            case "P":
	                reportDTO = getProjectReport(dto);
	                break;
	            default:
	                throw new IllegalArgumentException("Invalid report type: " + reportType);
	        }
	        
	        
//	        Map<String, Map<String, Map<String, Object>>> summary = getProjectSummary(dto);
//	        reportDTO.setProjectSummary(summary);
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(reportDTO);
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong.");
	        response.setServiceError(e.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("ERROR");
	    }

	    apiLogInfo.setApiRequest(logBuilder.toString());
	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}
	 
	public GetEmployeeProjectReportDTO getEmployeeReport(GetEmployeeProjectReportPayloadDTO dto) {
	    try {
	    	Session session = entityManager.unwrap(Session.class);
	        String queryStr = buildDynamicQuery(dto);
	        Query query = session.createSQLQuery(queryStr);
	        List<Object[]> resultList = entityManager.createNativeQuery(queryStr).getResultList();


	        if (resultList.isEmpty()) 
	            return new GetEmployeeProjectReportDTO(null, null, null);

	        List<GetEmployeeProjectReportForEmployeeDTO> employeeDTOs = resultList.stream().map(record -> {
	            GetEmployeeProjectReportForEmployeeDTO dtoObj = new GetEmployeeProjectReportForEmployeeDTO();

	            dtoObj.setEmpId(record[0] != null ? Long.parseLong(record[0].toString()) : null);
	            dtoObj.setEmployeementId(record[1] != null ? record[1].toString() : null);
	            dtoObj.setName(record[2] != null ? record[2].toString() : null);
	            dtoObj.setEmail(record[3] != null ? record[3].toString() : null);
	            dtoObj.setMobileNo(record[4] != null ? Long.parseLong(record[4].toString()) : null);
	            dtoObj.setManagerId(record[5] != null ? Long.parseLong(record[5].toString()) : null);
	            dtoObj.setManagerName(record[6] != null ? record[6].toString() : null);
	            dtoObj.setEmploymentstatus(record[7] != null ? record[7].toString() : null);
	            dtoObj.setBillable(record[8] != null ? record[8].toString() : null);
	            dtoObj.setBillableType(record[9] != null ? record[9].toString() : null);
	            dtoObj.setTeamIds(record[10] != null ? record[10].toString() : null);
	            dtoObj.setTeamName(record[11] != null ? record[11].toString() : null);
	            dtoObj.setProjectIds(record[12] != null ? record[12].toString() : null);
	            dtoObj.setProjectName(record[13] != null ? record[13].toString() : null);
	            dtoObj.setPoStartDate(record[14] != null ? record[14].toString() : null);
	            dtoObj.setPoEndDate(record[15] != null ? record[15].toString() : null);
	            dtoObj.setPoNo(record[16] != null ? record[16].toString() : null);
	            dtoObj.setClientName(record[17] != null ? record[17].toString() : null);
	            dtoObj.setClientLocation(record[18] != null ? record[18].toString() : null);
	            dtoObj.setWorkLocation(record[19] != null ? record[19].toString() : null);
	            dtoObj.setTotalExperience(record[20] != null ? Float.parseFloat(record[20].toString()) : null);
	            dtoObj.setDepartmentId(record[21] != null ? Long.parseLong(record[21].toString()) : null);
	            dtoObj.setDepartmentName(record[22] != null ? record[22].toString() : null);
	            dtoObj.setPoProjectType(record[23] != null ? record[23].toString() : null);
	            dtoObj.setJobRole(record[24] != null ? record[24].toString() : null);
	            dtoObj.setPoProjectId(record[25] != null ? record[25].toString() : null);
	            dtoObj.setPrimaryProjectName(record[26] != null ? record[26].toString() : null);
	            dtoObj.setPrimaryProjectId(record[27] != null ? record[27].toString() : null);
	            dtoObj.setClientRM(record[28] != null ? record[28].toString() : null);
	            dtoObj.setApmosysRM(record[29] != null ? record[29].toString() : null);
	            dtoObj.setEffectiveStartDate(record[30] != null ? record[30].toString() : null);
	            dtoObj.setEffectiveEndDate(record[31] != null ? record[31].toString() : null);
	            dtoObj.setEmployeementIdAccToET(record[32] != null ? record[32].toString() : null);

	            return dtoObj;
	        }).collect(Collectors.toList());

	        return new GetEmployeeProjectReportDTO(employeeDTOs, null, null);

	    } catch (Exception e) {
	        e.printStackTrace();
	        return new GetEmployeeProjectReportDTO(null, null, null);
	    }
	}

	 public GetEmployeeProjectReportDTO getProjectReport(GetEmployeeProjectReportPayloadDTO dto) {
		    try {
		        String queryStr = buildDynamicQuery(dto);

		        List<Object[]> resultList = entityManager.createNativeQuery(queryStr).getResultList();

		        if (resultList.isEmpty())
		            return new GetEmployeeProjectReportDTO(null, Collections.emptyList(), null);

		        Map<Long, GetProjectToEmployeeReportForProjectDTO> projectMap = new HashMap<>();
		        Map<String, GetProjectToEmployeeReportForTeamDTO> teamMap = new HashMap<>();

		        for (Object[] record : resultList) {

		            Long projectId = record[0] != null ? Long.parseLong(record[0].toString()) : null;
		            String teamKey = projectId + "-" + (record[11] != null ? Long.parseLong(record[11].toString()) : null);

		            if (!projectMap.containsKey(projectId)) {
		                GetProjectToEmployeeReportForProjectDTO projectDTO = new GetProjectToEmployeeReportForProjectDTO();
		                projectDTO.setProjectId(projectId);
		                projectDTO.setPoProjectId(record[1] != null ? Long.parseLong(record[1].toString()) : null);
		                projectDTO.setProjectName(record[2] != null ? record[2].toString() : null);
		                projectDTO.setProjectManagerId(record[3] != null ? Long.parseLong(record[3].toString()) : null);
		                projectDTO.setProjectManager(record[4] != null ? record[4].toString() : null);
		                projectDTO.setPoNo(record[5] != null ? record[5].toString() : null);
		                projectDTO.setPoProjectType(record[6] != null ? record[6].toString() : null);
		                projectDTO.setPoStartDate(record[7] != null ? record[7].toString() : null);
		                projectDTO.setPoEndDate(record[8] != null ? record[8].toString() : null);
		                projectDTO.setClientRM(record[9] != null ? record[9].toString() : null);
		                projectDTO.setApmosysRM(record[10] != null ? record[10].toString() : null);
		                projectDTO.setTeamDetails(new ArrayList<>());
		                projectMap.put(projectId, projectDTO);
		            }

		            if (!teamMap.containsKey(teamKey)) {
		                GetProjectToEmployeeReportForTeamDTO teamDTO = new GetProjectToEmployeeReportForTeamDTO();
		                teamDTO.setTeamId(record[11] != null ? Long.parseLong(record[11].toString()) : null);
		                teamDTO.setTeamName(record[12] != null ? record[12].toString() : null);
		                teamDTO.setMappedEmployeeDetails(new ArrayList<>());
		                projectMap.get(projectId).getTeamDetails().add(teamDTO);
		                teamMap.put(teamKey, teamDTO);
		            }

		            GetProjectToEmployeeReportForEmployeeDTO empDTO = new GetProjectToEmployeeReportForEmployeeDTO();
		            empDTO.setEmpId(record[13] != null ? Long.parseLong(record[13].toString()) : null);
		            empDTO.setEmployeeName(record[14] != null ? record[14].toString() : null);
		            empDTO.setEffectiveStartDate(record[15] != null ? record[15].toString() : null);
		            empDTO.setJobRole(record[16] != null ? record[16].toString() : null);
		            empDTO.setDeptName(record[17] != null ? record[17].toString() : null);
		            empDTO.setBillableType(record[18] != null ? record[18].toString() : null);
		            empDTO.setBillable(record[19] != null ? record[19].toString() : null);
		            empDTO.setMobileNo(record[20] != null ? Long.parseLong(record[20].toString()) : null);
		            empDTO.setEmail(record[21] != null ? record[21].toString() : null);
		            empDTO.setEmployeementIdAccToET(record[22] != null ? record[22].toString() : null);

		            teamMap.get(teamKey).getMappedEmployeeDetails().add(empDTO);
		        }
		        
		        return new GetEmployeeProjectReportDTO(null, new ArrayList<>(projectMap.values()), null);

		    } catch (Exception e) {
		        e.printStackTrace();
		        return new GetEmployeeProjectReportDTO(null, null, null);
		    }
		}
	 
	

	 @Transactional
	 public ServiceResponse getResourceRequirementFromPoPortal() {
	     ServiceResponse serviceResponse = new ServiceResponse();
	     LogDTO apiLogInfo = new LogDTO();
	     apiLogInfo.setSubFeatureName("getResourceRequirementFromPoPortal");
	     apiLogInfo.setApiUrl("/api/getResourceRequirementFromPoPortal");
	     apiLogInfo.setLogLevel("INFO");

	     StringBuilder logBuilder = new StringBuilder();
	     logBuilder.append("API to update existing resource requirement table\n");
	    
	     List<ProjectPoPortalDTO> list = null ;

	     try {
	    	 ProjectPoPortalDTO[] projects = new ProjectPoPortalDTO[0];
	    	 ServiceResponse response = poPortalAPIService.getAllProjectsFromPoPortal();
	    	 if (response != null) { 

				  List<ProjectPoPortalDTO> projectList = (List<ProjectPoPortalDTO>) response.getServiceResponse();
				   projects = projectList.toArray(new ProjectPoPortalDTO[0]);
	    		    // projects = (ProjectPoPortalDTO[]) response.getServiceResponse();
					list = projectList;
	    		    if (projects != null) {
	    		        list = projectList;
	    		        String msg = "Total Projects Fetched = " + list.size() + "\n";
	    		        logBuilder.append(msg);
	    		    } else {
	    		        String msg = "Project fetch successful, but the project list was null.\n";
	    		        logBuilder.append(msg);
	    		    }
	    		} else {
	    		    String errorMsg = "Failed to fetch projects from PO Portal.";
	    		    if (response != null && response.getServiceMessage() != null) { 
	    		        errorMsg += " Reason: " + response.getServiceMessage();
	    		    }
	    		    logBuilder.append(errorMsg).append("\n");
	    		}
	    	 
	     } catch (RestClientException e) {
	         String msg = "Error fetching projects from PoPortal API: " + e.getMessage();
	         logBuilder.append(msg);
	         serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	         serviceResponse.setServiceResponse(msg);
	         apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	         apiLogInfo.setApiResponse(msg);
	         return serviceResponse;
	     }

	     if (!list.isEmpty()) {
	    	 saveResourceRequirement(list);
	     } else {
	         String msg = "No projects found from PoPortal API.\n";
	         logBuilder.append(msg);
	     }

	     apiLogInfo.setApiResponse(logBuilder.toString());
	     apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	     serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	     serviceResponse.setServiceResponse("Sync completed. Summary:\n" + logBuilder);
	     return serviceResponse;
	 }

	 public ServiceResponse getAllProjectFCLineItemListByProjectId(ProjectDTO projectDto) {
			ServiceResponse serviceResponse = new ServiceResponse();
			LogDTO apiLogInfo = new LogDTO();
			apiLogInfo.setApiUrl("/api/getAllProjectFCLineItemListByProjectId");
			apiLogInfo.setLogLevel("INFO");
			try {
				if (projectDto == null || projectDto.getPoProjectId() == null) {
					serviceResponse.setServiceResponse("Project Id cannot be null!");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
					apiLogInfo.setApiResponse("Project Id cannot be null!");
					return serviceResponse;
				} else {
					Project project = projectRepository.findByPoProjectId(projectDto.getPoProjectId());					
					if (project == null) {
						apiLogInfo.setApiResponse("Project not found!");
						serviceResponse.setServiceResponse("Project not found!");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
						return serviceResponse;
					} else {
						ServiceResponse serviceResponseTemp = poPortalAPIService.callGetFCLineItemDetails(projectDto.getPoProjectId());    
						if(!serviceResponseTemp.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS)) {
							apiLogInfo.setApiResponse(serviceResponseTemp.getServiceResponse().toString());
							serviceResponse.setServiceResponse(serviceResponseTemp.getServiceResponse());
							apiLogInfo.setApiStatus(serviceResponseTemp.getServiceStatus());
							serviceResponse.setServiceStatus(serviceResponseTemp.getServiceStatus());
							return serviceResponse;
						}
						List<FCLineItemDTO> fCLineItemDTO = (List<FCLineItemDTO>) serviceResponseTemp.getServiceResponse();
						
						List<FCProjectMilestoneDTO> fcProjectMilestoneDTOList = mapLineItemToMilestone(fCLineItemDTO);
						if (fcProjectMilestoneDTOList.isEmpty()) {
							serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
							serviceResponse.setServiceResponse("No Milestone(s) found for this project!");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
							apiLogInfo.setApiResponse("No Milestone(s) found for this project!");
							return serviceResponse;
						} else {
							fcProjectMilestoneDTOList.forEach(dto -> {
								System.out
										.println("DTO ID: " + dto.getId() + ", PO Project ID: " + dto.getPoProjectId());
							});
							serviceResponse.setServiceResponse(fcProjectMilestoneDTOList);
							serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
							apiLogInfo.setApiResponse("FC Project Milestone list fetched successfully!");
							return serviceResponse;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("Something went wrong.");
				serviceResponse.setServiceError(e.getMessage());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			return serviceResponse;
		}


	 public FCProjectMilestoneDTO getMilestoneDocument(Long milestoneId) {
			   ApiLog initialLog = null;
			   String traceId = UUID.randomUUID().toString();
			   int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
			   String exceptionDetailsForLog = null;
			   String requestUrl = httpRequest.getRequestURI();
			   
			   try {
			       initialLog = apiLogUtility.startLog(traceId, "getMilestoneDocument", "Ishine", getCurrentUserId(), httpRequest);
			       
			       if (milestoneId == null) {
			           finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
			           exceptionDetailsForLog = "Milestone ID cannot be null.";
			           throw new IllegalArgumentException(exceptionDetailsForLog);
			       }
			       
			       FCProjectMilestoneDTO documentBytes = poPortalAPIService.getMilestoneDocumentFromExternalApi(milestoneId);
			       
			       finalHttpStatusCode = HttpStatus.OK.value();
			       return documentBytes;
			       
			   } catch (Exception e) {
			       if (e instanceof HttpClientErrorException) {
			           finalHttpStatusCode = ((HttpClientErrorException) e).getStatusCode().value();
			       }
			       exceptionDetailsForLog = "Error retrieving document for milestone ID " + milestoneId + ": " + e.toString();
			       e.printStackTrace();
			       
			       throw new RuntimeException("Failed to retrieve milestone document.", e);
			       
			   } finally {
			       if (initialLog != null && initialLog.getId() != null) {
			           apiLogUtility.endLog(initialLog.getId(), requestUrl, finalHttpStatusCode, exceptionDetailsForLog, httpRequest);
			       }
			   }
			}



	 public void saveResourceRequirement(List<ProjectPoPortalDTO> list) {

    	 
    	 StringBuilder logBuilder = new StringBuilder();
         for (ProjectPoPortalDTO dto : list) {
             try {
                 if (!"TNM".equalsIgnoreCase(dto.getProjectType())) {
                     String msg = "Skipping projectId = " + dto.getId() + " as it is not TNM\n";
                     logBuilder.append(msg);
                     System.out.println(msg);
                     continue;
                 }

                 Project project = projectRepository.findByPoProjectId(dto.getId());

                 if (project != null) {
                     Integer projectId = project.getProjectId();
                     List<ResourceRequirementDTO> requirements = dto.getResourceRequirements();

                     if (requirements != null && !requirements.isEmpty()) {
                         int saveCount = 0;

                         for (ResourceRequirementDTO rrDto : requirements) {
                             if (rrDto.getResourceOverviewId() == null) continue;

                             ResourceRequirement existing = resourceRequirementRepository
                                     .findByProjectIdAndResourceOverviewId(projectId, rrDto.getResourceOverviewId());

                             boolean isChanged = false;

                             if (existing == null) {
                                 isChanged = true;
                             } else {
                                 isChanged = !Objects.equals(existing.getRole(), rrDto.getRole()) ||
                                         !Objects.equals(existing.getCount(), rrDto.getCount()) ||
                                         !Objects.equals(existing.getExperience(), rrDto.getExperience()) ||
                                         !Objects.equals(existing.getDepartment(), rrDto.getDepartment());
                             }

                             if (isChanged) {
                                 ResourceRequirement rr = existing != null ? existing : new ResourceRequirement();
                                 rr.setProjectId(projectId);
                                 rr.setResourceOverviewId(rrDto.getResourceOverviewId());
                                 rr.setRole(rrDto.getRole());
                                 rr.setCount(rrDto.getCount());
                                 rr.setExperience(rrDto.getExperience());
                                 rr.setDepartment(rrDto.getDepartment());
                                 resourceRequirementRepository.save(rr);
                                 saveCount++;
                             }
                         }

                         String msg = "Updated/Inserted " + saveCount + " Resource Requirements for Project ID = " + dto.getId() + "\n";
                         logBuilder.append(msg);
                         System.out.println(msg);
                     }

                 } else {
                     String msg = "Project not found for PoProjectId = " + dto.getId() + "\n";
                     logBuilder.append(msg);
                     System.out.println(msg);
                 }
             } catch (Exception ex) {
                 ex.printStackTrace();
                 String msg = "Error updating project ID: " + dto.getId() + " - " + ex.getMessage() + "\n";
                 logBuilder.append(msg);
                 System.out.println(msg);
             }
         }
     }


	 private List<FCProjectMilestoneDTO> mapLineItemToMilestone(List<FCLineItemDTO> fCLineItemDTO) {
		    List<FCProjectMilestoneDTO> fcProjectMilestoneDTOList = new ArrayList<>();
		    if (fCLineItemDTO != null && !fCLineItemDTO.isEmpty()) {
		        for (FCLineItemDTO fcLineItemDTO : fCLineItemDTO) {
		            if (fcLineItemDTO.getMilestones() != null) {
		                for (FCProjectMilestoneDTO fcProjectMilestoneDTOTemp : fcLineItemDTO.getMilestones()) {
		                    FCProjectMilestoneDTO fcProjectMilestoneDTO = new FCProjectMilestoneDTO();

		                    // milestone fields
		                    fcProjectMilestoneDTO.setId(fcProjectMilestoneDTOTemp.getId());
		                    fcProjectMilestoneDTO.setPoId(fcProjectMilestoneDTOTemp.getPoId());
		                    fcProjectMilestoneDTO.setPoProjectId(fcProjectMilestoneDTOTemp.getPoProjectId());
		                    fcProjectMilestoneDTO.setProjectId(fcProjectMilestoneDTOTemp.getProjectId());
		                    fcProjectMilestoneDTO.setName(fcProjectMilestoneDTOTemp.getName());
		                    fcProjectMilestoneDTO.setDescription(fcProjectMilestoneDTOTemp.getDescription());
		                    fcProjectMilestoneDTO.setStartDate(fcProjectMilestoneDTOTemp.getStartDate());
		                    fcProjectMilestoneDTO.setEndDate(fcProjectMilestoneDTOTemp.getEndDate());
		                    fcProjectMilestoneDTO.setExtendedDate(fcProjectMilestoneDTOTemp.getExtendedDate());
		                    fcProjectMilestoneDTO.setStatus(fcProjectMilestoneDTOTemp.getStatus()); // ✅ milestone status
		                    fcProjectMilestoneDTO.setRemarks(fcProjectMilestoneDTOTemp.getRemarks());

		                    // parent line item info (use a different field!)
		                    fcProjectMilestoneDTO.setLineItemId(fcLineItemDTO.getId());
		                    fcProjectMilestoneDTO.setLineItemName(fcLineItemDTO.getName());
		                    fcProjectMilestoneDTO.setLineItemStatus(fcLineItemDTO.getStatus()); // ✅ store line item status separately

		                    fcProjectMilestoneDTOList.add(fcProjectMilestoneDTO);
		                }
		            }
		        }
		    }
		    return fcProjectMilestoneDTOList;
		}

public ServiceResponse getCompletedFixedCostProjects(ProjectRequest projectRequest) {
    ServiceResponse response = new ServiceResponse();
    LogDTO apiLogInfo = new LogDTO();
    StringBuilder logBuilder = new StringBuilder("Fetching completed fixed cost projects... ");
    apiLogInfo.setApiUrl("/api/getCompletedFixedCostProjects");
    apiLogInfo.setLogLevel("INFO");
    
    try {
        String timeFrame = projectRequest.getTabName();	
       
        List<Long> deptIds;
        if (projectRequest.getProjectFilter() != null && 
            projectRequest.getProjectFilter().getDepartmentsids() != null && 
            !projectRequest.getProjectFilter().getDepartmentsids().isEmpty()) {
            deptIds = projectRequest.getProjectFilter().getDepartmentsids();
            System.out.println("filtered department Ids: " + deptIds);
        } else {
            deptIds = projectRepository.deptIds();
            System.out.println("all department Ids: " + deptIds);
        }
        
        System.out.println("department Ids as per query:::::::::: " + deptIds);
        
        if ("all".equalsIgnoreCase(timeFrame)) {
            List<ProjectFetchDTO> totalfixedCost = new ArrayList<>();
            List<Object[]> allFcProjects = new ArrayList<>();
            allFcProjects = projectRepository.findAllFixedCostProjects(deptIds);
            totalfixedCost = allFcProjects.stream().map(ProjectFetchDTO::new).collect(Collectors.toList());
            response.setServiceResponse(totalfixedCost);
            response.setServiceMessage("All fixed cost projects fetched successfully.");
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            logBuilder.append("Fetched ").append(totalfixedCost.size()).append(" projects successfully.");
            return response;
        }

        if ("defaulter".equalsIgnoreCase(timeFrame)) {
            List<Object[]> expiredProjects = new ArrayList<>();
            expiredProjects = projectRepository.findExpiredFixedCostProjects(deptIds);
            List<ProjectFetchDTO> expiredFixedcost = expiredProjects.stream().map(ProjectFetchDTO::new).collect(Collectors.toList());
            response.setServiceResponse(expiredFixedcost);
            response.setServiceMessage("All defaulter fixed cost projects fetched successfully.");
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            logBuilder.append("Fetched ").append(expiredProjects.size()).append(" expired projects successfully.");
            return response;
        }
        
        if ("delays".equalsIgnoreCase(timeFrame)) {
            List<Object[]> delayedProjects = new ArrayList<>();
            delayedProjects = projectRepository.findDelayedFCProject(deptIds);
            List<ProjectFetchDTO> delayedFCProjects = delayedProjects.stream().map(ProjectFetchDTO::new).collect(Collectors.toList());
            response.setServiceResponse(delayedFCProjects);
            response.setServiceMessage("All delayed fixed cost projects fetched successfully.");
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            logBuilder.append("Fetched ").append(delayedProjects.size()).append(" delayed projects successfully.");
            return response;
        }
        
        if ("ontime".equalsIgnoreCase(timeFrame)) {
            List<Object[]> ontimeProjects = new ArrayList<>();
            ontimeProjects = projectRepository.findOntimeFCList(deptIds);
            List<ProjectFetchDTO> ontimeFcProjects = ontimeProjects.stream().map(ProjectFetchDTO::new).collect(Collectors.toList());
            response.setServiceResponse(ontimeFcProjects);
            response.setServiceMessage("All ontime fixed cost projects fetched successfully.");
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            logBuilder.append("Fetched ").append(ontimeProjects.size()).append(" ontime projects successfully.");
            return response;
        }
      
    } catch (Exception e) {
        e.printStackTrace();
        logBuilder.append("Failed. Exception: ").append(e.getMessage());
        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
        response.setServiceMessage("Failed to fetch completed fixed cost projects.");
        response.setServiceError(e.getMessage());
        response.setServiceResponse(Collections.emptyList());
        return response;
    } finally {
        System.out.println(logBuilder.toString());
    }
    return response;
}
		
		public ServiceResponse getFcCount(ProjectFilterDTO projectFilter) {
			
			ServiceResponse response = new ServiceResponse();
	        LogDTO apiLogInfo = new LogDTO();
	        StringBuilder logBuilder = new StringBuilder("Fetching completed fixed cost projects... ");
	        apiLogInfo.setApiUrl("/api/getCompletedFixedCostProjects");
	        apiLogInfo.setLogLevel("INFO");
	        
	        try {
	        	Long totalCountFC,totalExpiredCount,delayedCount,ontimeCount;
	        	if(!projectFilter.getDepartmentsids().isEmpty()) {
	        	totalCountFC = projectRepository.totalFcCount(projectFilter.getDepartmentsids());
	        	totalExpiredCount = projectRepository.expiredFCcount(projectFilter.getDepartmentsids());
	        	delayedCount = projectRepository.getAllDelayedProjectCount(projectFilter.getDepartmentsids());
	        	ontimeCount = projectRepository.getAllOntimeCount(projectFilter.getDepartmentsids());
	        	}
	        	else
	        	{
	        		totalCountFC = projectRepository.totalFcCount(projectRepository.deptIds());
		        	totalExpiredCount = projectRepository.expiredFCcount(projectRepository.deptIds());
		        	delayedCount = projectRepository.getAllDelayedProjectCount(projectRepository.deptIds());
		        	ontimeCount = projectRepository.getAllOntimeCount(projectRepository.deptIds());
	        	}
	        	
	        	FixedCostProjectCount fixedCountDTO = new FixedCostProjectCount();
	        	
	        	fixedCountDTO.setTotalFixedCostcount(totalCountFC);
	        	fixedCountDTO.setExpiredCount(totalExpiredCount);
	        	fixedCountDTO.setDelayedCount(delayedCount);
	        	fixedCountDTO.setOntimeCount(ontimeCount);
	        	response.setServiceResponse(fixedCountDTO);
                response.setServiceMessage("All defaulter fixed cost projects fetched successfully.");
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                logBuilder.append("Fetched ").append(" Count of projects successfully.");
                return response;
	        	
	        }catch(Exception e) {
	        	e.printStackTrace();
	        	logBuilder.append("Failed. Exception: ").append(e.getMessage());
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceMessage("Failed to fetch completed fixed cost projects count.");
	            response.setServiceError(e.getMessage());
	            response.setServiceResponse(Collections.emptyList());
	            return response;
	        }
		}
	
		
	public ServiceResponse getProjectWithCliendSideID(ProjectDTO projectDto) {
		
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("getProjectWithCliendSideID");
        apiLogInfo.setApiUrl("/api/getProjectWithCliendSideID");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        try {        	
        	List<Object[]>  details;
            if(projectDto.getIsClientDashboard()) {
             details = projectRepository.getProjectWithCliendSideID(projectDto.getEmpId());
            }else {
        	 details = projectRepository.getAllEmpProjectWithID(projectDto.getEmpId());
            }
		List<ProjectFetchDTO> dtoList = new ArrayList<ProjectFetchDTO>();
		
       if(details != null) {
		for(Object[] object:details) {
			ProjectFetchDTO projectDetails= new ProjectFetchDTO();
			
			projectDetails.setProjectName(object[0] != null ? object[0].toString() : null);
			projectDetails.setPoNo(object[1] != null ? object[1].toString() : null);
			projectDetails.setProjectId(object[2] != null ? Integer.valueOf(object[2].toString()) : null);
			
			dtoList.add(projectDetails);
			}
        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
        response.setServiceResponse(dtoList);
        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
		}
		}catch(Exception e) {
			String msg = "Error fetching projects: " + e.getMessage();
	         logBuilder.append(msg);
	        
	         response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	         response.setServiceResponse(msg);
	         apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	         apiLogInfo.setApiResponse(msg);
	         return response;
		}
		
		
		return response;
	}
//	public ServiceResponse getProjectWithCliendSideID(ProjectDTO projectDto) {
//		
//		ServiceResponse response = new ServiceResponse();
//        LogDTO apiLogInfo = new LogDTO();
//        apiLogInfo.setSubFeatureName("getProjectWithCliendSideID");
//        apiLogInfo.setApiUrl("/api/getProjectWithCliendSideID");
//        apiLogInfo.setLogLevel("INFO");
//        StringBuilder logBuilder = new StringBuilder();
//        try {
//        	
//		List<Object[]>  details = projectRepository.getProjectWithCliendSideID(projectDto.getEmpId());
//		List<ProjectFetchDTO> dtoList = new ArrayList<ProjectFetchDTO>();
//		
//       if(details != null) {
//		for(Object[] object:details) {
//			ProjectFetchDTO projectDetails= new ProjectFetchDTO();
//			
//			projectDetails.setProjectName(object[0] != null ? object[0].toString() : null);
//			projectDetails.setPoNo(object[1] != null ? object[1].toString() : null);
//			projectDetails.setProjectId(object[2] != null ? Integer.valueOf(object[2].toString()) : null);
//			
//			dtoList.add(projectDetails);
//			}
//        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//        response.setServiceResponse(dtoList);
//        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//		}
//		}catch(Exception e) {
//			String msg = "Error fetching projects: " + e.getMessage();
//	         logBuilder.append(msg);
//	        
//	         response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//	         response.setServiceResponse(msg);
//	         apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//	         apiLogInfo.setApiResponse(msg);
//	         return response;
//		}
//		
//		
//		return response;
//	}
	
	

	
		
		
		
		
		public ServiceResponse getClientAndProjectReport(ClientProjectReportDTO clientProjectReportDTO) {
		    ServiceResponse response = new ServiceResponse();
		    LogDTO apiLogInfo = new LogDTO();
		    apiLogInfo.setSubFeatureName("getClientAndProjectReport");
		    apiLogInfo.setApiUrl("/api/getClientAndProjectReport");
		    apiLogInfo.setLogLevel("INFO");
		    StringBuilder logBuilder = new StringBuilder();

		    try {
		        List<Long> deptIds;
		        if (clientProjectReportDTO != null &&
		            clientProjectReportDTO.getDeptIds() != null &&
		            !clientProjectReportDTO.getDeptIds().isEmpty()) {

		            String[] deptIdArray = clientProjectReportDTO.getDeptIds().split(",");
		            deptIds = Arrays.stream(deptIdArray)
		                .map(String::trim)
		                .map(Long::parseLong)
		                .collect(Collectors.toList());
		            System.out.println("filtered department Ids: " + deptIds);
		        } else {
		            deptIds = projectRepository.deptIds();
		            System.out.println("all department Ids: " + deptIds);
		        }

		        System.out.println("department Ids as per query:::::::::: " + deptIds);

		        List<Object[]> rawData = projectRepository.getClientAndProjectData(deptIds);

		        List<ClientProjectReportDTO> reportData = new ArrayList<>();
		        if (rawData != null && !rawData.isEmpty()) {
		            reportData = rawData.stream().map(row -> {
		                ClientProjectReportDTO dto = new ClientProjectReportDTO();
		                dto.setDeptId(row[0] != null ? Long.parseLong(row[0].toString()) : null);
		                dto.setClientId(row[1] != null ? Long.parseLong(row[1].toString()) : null);
		                dto.setDepartmentName(row[2] != null ? row[2].toString() : null);
		                dto.setClientName(row[3] != null ? row[3].toString() : null);
		                dto.setTotalProjects(row[4] != null ? Integer.valueOf(row[4].toString()) : 0);
		                dto.setTotalActiveProjects(row[5] != null ? Integer.valueOf(row[5].toString()) : 0);
		                dto.setTotalActiveResources(row[6] != null ? Integer.valueOf(row[6].toString()) : 0);
		                dto.setTotalInactiveProjects(row[7] != null ? Integer.valueOf(row[7].toString()) : 0);
		                
		                return dto;
		            }).collect(Collectors.toList());

		            response.setServiceResponse(reportData);
		            response.setServiceMessage("Client and project report with counts fetched successfully.");
		            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		            logBuilder.append("Fetched ").append(reportData.size()).append(" records successfully.");
		        } else {
		            response.setServiceResponse(Collections.emptyList());
		            response.setServiceMessage("No data found for the provided department IDs.");
		            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		            logBuilder.append("No records found.");
		        }

		        return response;

		    } catch (Exception e) {
		        e.printStackTrace();
		        logBuilder.append("Failed. Exception: ").append(e.getMessage());
		        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		        response.setServiceMessage("Failed to fetch client and project report.");
		        response.setServiceError(e.getMessage());
		        response.setServiceResponse(Collections.emptyList());
		        return response;
		    } 
		}
		
	public ServiceResponse getClientAndProjectReportDataList(ClientProjectReportDTO clientProjectReportDTO) {
    ServiceResponse response = new ServiceResponse();
    LogDTO apiLogInfo = new LogDTO();
    apiLogInfo.setSubFeatureName("getClientAndProjectReportDataList");
    apiLogInfo.setApiUrl("/api/getClientAndProjectReportDataList");
    apiLogInfo.setLogLevel("INFO");
    StringBuilder logBuilder = new StringBuilder();

    try {
        List<Long> deptIds = null;
        if (clientProjectReportDTO != null &&
            clientProjectReportDTO.getDeptIds() != null &&
            !clientProjectReportDTO.getDeptIds().isEmpty()) {

            String[] deptIdArray = clientProjectReportDTO.getDeptIds().split(",");
            deptIds = Arrays.stream(deptIdArray)
                    .map(String::trim)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            System.out.println("filtered department Ids: " + deptIds);
        } else {
            System.out.println("No department filter applied - fetching all departments");
        }

        List<Long> clientIds = null;
        if (clientProjectReportDTO != null &&
            clientProjectReportDTO.getClientIds() != null &&
            !clientProjectReportDTO.getClientIds().isEmpty()) {

            String[] clientIdArray = clientProjectReportDTO.getClientIds().split(",");
            clientIds = Arrays.stream(clientIdArray)
                    .map(String::trim)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            System.out.println("filtered client Ids: " + clientIds);
        } else {
            System.out.println("No client filter applied - fetching all clients");
        }

        String projectType = clientProjectReportDTO.getProjectType();  
                           
        
        if (deptIds == null || deptIds.isEmpty()) {
            deptIds = Arrays.asList(-1L); // Use a dummy value that won't match any real dept_id
        }
        if (clientIds == null || clientIds.isEmpty()) {
            clientIds = Arrays.asList(-1L); // Use a dummy value that won't match any real client_id
        }

        System.out.println("Project type: " + projectType);
        System.out.println("Department Ids filter: " + deptIds);
        System.out.println("Client Ids filter: " + clientIds);

        List<Object[]> rawData = projectRepository.getClientAndProjectDataList(
                projectType, deptIds, clientIds);

        List<ClientProjectReportDTO> reportData = new ArrayList<>();
        if (rawData != null && !rawData.isEmpty()) {
            reportData = rawData.stream().map(row -> {
                ClientProjectReportDTO dto = new ClientProjectReportDTO();
                dto.setDeptId(row[0] != null ? Long.valueOf(row[0].toString()) : null);
//                dto.setClientId(row[1] != null ? Integer.valueOf(row[1].toString()) : null);
                dto.setDepartmentName(row[2] != null ? row[2].toString() : null);
                dto.setClientName(row[3] != null ? row[3].toString() : null);
                dto.setProjectId(row[4] != null ? Integer.valueOf(row[4].toString()) : null);
                dto.setProjectName(row[5] != null ? row[5].toString() : null);
                dto.setPoNo(row[6] != null ? row[6].toString() : null);
                dto.setPoProjectType(row[7] != null ? row[7].toString() : null);
                // Note: ProjectManager field not found in DTO - skipping row[8]
                dto.setApmosysRM(row[9] != null ? row[9].toString() : null);
                dto.setClientRM(row[10] != null ? row[10].toString() : null);
                dto.setPoStartDate(row[11] != null ? row[11].toString() : null);
                dto.setPoEndDate(row[12] != null ? row[12].toString() : null);
                dto.setCreatedOn(row[13] != null ? Timestamp.valueOf(row[13].toString()) : null);
                dto.setPoProjectId(row[14] != null ? Long.valueOf(row[14].toString()) : null);
                dto.setProjectType(row[15] != null ? row[15].toString() : null);
                dto.setProjectViewId(row[16] != null ? row[16].toString() : null)  ;
                return dto;
            }).collect(Collectors.toList());

            response.setServiceResponse(reportData);
            response.setServiceMessage("Client and project report data fetched successfully.");
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            logBuilder.append("Fetched ").append(reportData.size()).append(" records successfully.");
        } else {
            response.setServiceResponse(Collections.emptyList());
            response.setServiceMessage("No data found for the provided filters.");
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            logBuilder.append("No records found.");
        }

        return response;

    } catch (Exception e) {
        e.printStackTrace();
        logBuilder.append("Failed. Exception: ").append(e.getMessage());
        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
        response.setServiceMessage("Failed to fetch client and project report.");
        response.setServiceError(e.getMessage());
        response.setServiceResponse(Collections.emptyList());
        return response;
    }
}
	
	public ServiceResponse getEmployeeProjectCount(GetEmployeeProjectReportPayloadDTO dto) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("Employee Report");
	    apiLogInfo.setApiUrl("/api/getEmployeeProjectReport");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("Category: ").append(dto.getCategory()).append(", ");
	    logBuilder.append("Report Type: ").append(dto.getReport());

	    try {
	        String reportType = dto.getReport();
	        GetEmployeeProjectCountDTO countDTO = new GetEmployeeProjectCountDTO();

	        //repocall based on box, category,flag,maternityleave
	        
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(countDTO);
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong.");
	        response.setServiceError(e.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("ERROR");
	    }

	    apiLogInfo.setApiRequest(logBuilder.toString());
	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}

}
