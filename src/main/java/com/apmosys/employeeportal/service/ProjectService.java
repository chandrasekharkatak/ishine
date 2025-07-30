package com.apmosys.employeeportal.service;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat;
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
import java.util.TimeZone;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import javax.persistence.Query;
//import org.hibernate.Query;
//import org.hibernate.Session;

import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException.InternalServerError;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.apmosys.employeeportal.dto.ClientsDTO;
import com.apmosys.employeeportal.dto.EmployeeProjectSummaryDTO;
import com.apmosys.employeeportal.dto.GetEmployeeDashboardCountDTO;
import com.apmosys.employeeportal.dto.GetEmployeeProjectReportDTO;
import com.apmosys.employeeportal.dto.GetEmployeeProjectReportForEmployeeDTO;
import com.apmosys.employeeportal.dto.GetEmployeeProjectReportPayloadDTO;
import com.apmosys.employeeportal.dto.GetProjectToEmployeeReportForEmployeeDTO;
import com.apmosys.employeeportal.dto.GetProjectToEmployeeReportForProjectDTO;
import com.apmosys.employeeportal.dto.GetProjectToEmployeeReportForTeamDTO;
import com.apmosys.employeeportal.dto.HandleTeamsAsPerLinkedPoPayloadDTO;
import com.apmosys.employeeportal.dto.HandleTeamsAsPerLinkedPoProjectDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.PoEmployeeTimesheetSyncDTO;
import com.apmosys.employeeportal.dto.PoProjectSyncDTO;
import com.apmosys.employeeportal.dto.PoProjectTimesheetSyncDTO;
import com.apmosys.employeeportal.dto.PoTeamDTO;
import com.apmosys.employeeportal.dto.PoTeamTimesheetSyncDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.ProjectFetchDTO;
import com.apmosys.employeeportal.dto.ProjectPoPortalDTO;
import com.apmosys.employeeportal.dto.ResourceManagementDTO;
import com.apmosys.employeeportal.dto.ResourceRequirementDTO;
import com.apmosys.employeeportal.dto.SyncableProjectDTO;
import com.apmosys.employeeportal.dto.TeamDTO;
import com.apmosys.employeeportal.model.Activity;
import com.apmosys.employeeportal.model.ActivityTemplate;
import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.ClientLocation;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeTeamMap;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectDepartmentMap;
import com.apmosys.employeeportal.model.ProjectManagerMapping;
import com.apmosys.employeeportal.model.ResourceRequirement;
import com.apmosys.employeeportal.model.Team;
import com.apmosys.employeeportal.repository.ActivitiesRepository;
import com.apmosys.employeeportal.repository.ActivityTemplateRepository;
import com.apmosys.employeeportal.repository.ClientLocationRepository;
import com.apmosys.employeeportal.repository.ClientsRepository;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.ProjectDepartmentMapRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.ResourceRequirementRepository;
import com.apmosys.employeeportal.repository.TeamRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
public class ProjectService {
	
	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	ProjectRepository projectRepository;
	
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
	
	@Autowired
	private final RestTemplate restTemplate = new RestTemplate();

	
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
        logBuilder.append("AllProjectList : " + projectRepository.getAllProject().size());
		try {
			
			List<Object[]> projects = projectRepository.getAllProject();
			
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
	
	@Transactional
	public ServiceResponse getProjectCloneFromPoPortal() {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("getProjectCloneFromPoPortal");
	    apiLogInfo.setApiUrl("/api/getProjectCloneFromPoPortal");
	    apiLogInfo.setLogLevel("INFO");
	    StringBuilder logBuilder = new StringBuilder();

	    List<ProjectPoPortalDTO> list = new ArrayList<>();

	    try {
	        ProjectPoPortalDTO[] projects = restTemplate.getForObject(allPoPortalProjects, ProjectPoPortalDTO[].class);
	        list = Arrays.asList(projects != null ? projects : new ProjectPoPortalDTO[0]);
	        logBuilder.append("Total Projects Fetched = ").append(list.size()).append("\n");
	    } catch (RestClientException e) {
	        logBuilder.append("Error fetching projects from Shankh Portal API: ").append(e.getMessage()).append("\n");
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse("Error fetching projects: " + e.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setApiRequest(logBuilder.toString());
	        logService.logMyInfo(httpRequest, apiLogInfo);
	        return response;
	    }

	    if (!list.isEmpty()) {
	        for (ProjectPoPortalDTO dto : list) {
	            try {
	                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	                Project project = projectRepository.findByPoProjectId(dto.getId());

	                if (project != null) {
	                    project.setProjectName(dto.getName());
	                    project.setPoProjectType(dto.getProjectType());
	                    project.setPoStartDate(dateFormat.format(dto.getStartDate()));
	                    project.setPoEndDate(dateFormat.format(dto.getEndDate()));
	                    project.setStatus(dto.getStatus());

	                    // Department Mapping
	                    List<String> departmentList = dto.getDepartment();
	                    List<String> deptIds = new ArrayList<>();
	                    if (!departmentList.isEmpty()) {
	                        for (String dept : departmentList) {
	                            Department deptEntity = departmentRepository.findByName(dept);
	                            if (deptEntity != null) {
	                                deptIds.add(deptEntity.getDeptId().toString());
	                            } else {
	                                logBuilder.append("No Dept Id fetched for dept: ").append(dept).append("\n");
	                            }
	                        }
	                        if (!deptIds.isEmpty()) {
	                            project.setDeptId(String.join(", ", deptIds));
	                        }
	                    }

	                    // Client Mapping — Using explicit loop
	                    Integer clientId = null;
	                    Optional<Client> clientOpt = clientsRepository.findByClientName(dto.getClientName());
	                    if (clientOpt.isPresent()) {
	                        clientId = clientOpt.get().getClientId();
	                    } else {
	                        Client newClient = new Client();
	                        newClient.setClientName(dto.getClientName());
	                        Client savedClient = clientsRepository.save(newClient);

	                        if (savedClient != null) {
	                            clientId = savedClient.getClientId();
	                            List<ClientLocation> locations = new ArrayList<>();

	                            for (String location : dto.getClientLocation()) {
	                                ClientLocation cl = new ClientLocation();
	                                cl.setClientId(clientId);
	                                cl.setClientLocation(location);
	                                locations.add(cl);
	                            }

	                            if (!dto.getClientLocation().contains("WFH")) {
	                                ClientLocation wfh = new ClientLocation();
	                                wfh.setClientId(clientId);
	                                wfh.setClientLocation("WFH");
	                                locations.add(wfh);
	                            }

	                            clientLocationRepository.saveAll(locations);
	                            logBuilder.append("Client and locations stored for: ").append(dto.getClientName()).append("\n");
	                        }
	                    }

	                    project.setClientId(clientId);
	                    project.setPoNo(dto.getPoNo());
	                    project.setApmosysRM(dto.getApmosysRM());
	                    project.setApmosysRmEmail(dto.getApmosysRmEmail());               
	                    project.setIsRenewable(dto.getIsRenewable());
	                    project.setClientRM(dto.getClientRM());

	                    projectRepository.save(project);

	                    logBuilder.append("Updated Project: ID=").append(dto.getId())
	                              .append(", PoNo=").append(dto.getPoNo()).append("\n");
	                } else {
	                    logBuilder.append("Project not found for PoProjectId: ").append(dto.getId()).append("\n");
	                }

	            } catch (Exception ex) {
	                ex.printStackTrace();
	                logBuilder.append("Error updating project ID: ").append(dto.getId())
	                          .append(" - ").append(ex.getMessage()).append("\n");
	            }
	        }

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("Successfully synced project details from Shankh Portal.");
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        apiLogInfo.setApiResponse("Successfully updated projects from PoPortal.");
	    } else {
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse("No projects found from Shank Portal API.");
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setApiResponse("No projects returned from PoPortal API.");
	    }

	    apiLogInfo.setApiRequest(logBuilder.toString());
	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}
	
	public ServiceResponse poProjectTimesheetSync(Set<Long> poProjectIdList) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/poProjectTimesheetSync");
	    apiLogInfo.setLogLevel("INFO");
	    StringBuilder logBuilder = new StringBuilder();
	    
	    try {
	        List<Object[]> projectInfo = projectRepository.poProjectTimesheetSync(poProjectIdList);
	        
	        if (projectInfo.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Project Info not found.");
	            apiLogInfo.setApiResponse("Project Info not Found");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        } else {
	            Map<Long, PoProjectTimesheetSyncDTO> projectMap = new HashMap<>();
	            
	            projectInfo.forEach(object -> {
	                try { 
	                	Long poProjectId = object[0] != null ? Long.parseLong(object[0].toString()) : null;

		                PoProjectTimesheetSyncDTO projectDTO = projectMap.computeIfAbsent(poProjectId, id -> {
		                    PoProjectTimesheetSyncDTO poProjectDTO = new PoProjectTimesheetSyncDTO();
		                    poProjectDTO.setPoProjectId(poProjectId);
		                    poProjectDTO.setIshineStoredProjectName(object[1] != null ? object[1].toString() : null);
		                    poProjectDTO.setIshineStoredPoNo(object[2] != null ? object[2].toString() : null);
		                    poProjectDTO.setTeamDetails(new ArrayList<>());
		                    return poProjectDTO;
		                });

		                if (object[3] != null) {
		                    Long teamId = Long.parseLong(object[3].toString()) ;
		                    PoTeamTimesheetSyncDTO teamDTO = projectDTO.getTeamDetails().stream()
		                        .filter(t -> t.getTeamId().equals(teamId))
		                        .findFirst()
		                        .orElseGet(() -> {
		                            PoTeamTimesheetSyncDTO poTeamDTO = new PoTeamTimesheetSyncDTO();
		                            poTeamDTO.setTeamId(teamId);
		                            poTeamDTO.setTeamName(object[4] != null ? object[4].toString() : null);
		                            poTeamDTO.setEmployeesMapped(new ArrayList<>());
		                            projectDTO.getTeamDetails().add(poTeamDTO);
		                            return poTeamDTO;
		                        });
		                    
		                    if (object[5] != null) {
		                        PoEmployeeTimesheetSyncDTO employeeDTO = new PoEmployeeTimesheetSyncDTO();
		                        employeeDTO.setEmployeementId(Long.parseLong(object[6].toString()));
		                        employeeDTO.setEmployeeName(object[7] != null ? object[7].toString() : null);
		                        employeeDTO.setCurrentStatus(object[8] != null ? object[8].toString() : null);
		                        employeeDTO.setStartDate(object[9] != null ? (Timestamp) object[9] : null);
		                        employeeDTO.setEndDate(object[10] != null ? (Timestamp) object[10] : null);
		                        employeeDTO.setLastTimesheetFilledPoProjectId(object[11] != null ? Long.parseLong(object[11].toString()) : null);
		                        employeeDTO.setLastTimesheetFilledProjectName(object[12] != null ? object[12].toString() : null);
		                        employeeDTO.setIsInternal(object[11] != null ? true : false);

		                        teamDTO.getEmployeesMapped().add(employeeDTO);
		                    }
		                }
	                }catch (Exception e) {
	                	 logBuilder.append("Error processing record: ").append(Arrays.toString(object)).append("\n");
	                     logBuilder.append("Exception: ").append(e.getMessage()).append("\n");
	                }
	            });

	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse(new ArrayList<>(projectMap.values()));
	            apiLogInfo.setApiResponse("ProjectInfoList: " + projectMap.size());
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
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
	
	public String buildDynamicQuery(GetEmployeeProjectReportPayloadDTO dto) {
        String category = dto.getCategory();
        String poProjectType = dto.getPoProjectType();
        String flag = dto.getFlag();
        List<String> billableType = dto.getBillableType();
        List<Long> deptIds = dto.getDeptId();

        StringBuilder query = new StringBuilder();

        if ("P".equalsIgnoreCase(dto.getReport())) {
            // === Project Query ===
            query.append("SELECT p.project_id, p.po_project_id, p.project_name, p.project_manager_id, ep.name as projManager, ")
                 .append("p.po_no, p.po_project_type, p.po_start_date, p.po_end_date, p.clientrm, p.apmosysrm, ")
                 .append("t.team_id, team_name, etm.emp_id, e.name, etm.start_date, j.name as jobRole, d.name as deptName, e.billable_type, ")
                 .append("e.billable, e.mobile_no, e.email ")
                 .append("FROM projects p ")
                 .append("INNER JOIN teams t ON t.project_id = p.project_id ")
                 .append("INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id ")
                 .append("INNER JOIN employee e ON etm.emp_id = e.emp_id ")
                 .append("INNER JOIN job_role j ON e.job_role_id = j.job_role_id ")
                 .append("INNER JOIN department d ON d.dept_id = j.dept_id ")
                 .append("INNER JOIN employee ep ON p.project_manager_id = ep.emp_id ")
                 .append("WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false' and e.emp_id not between 1 and 6  ")
                 .append(buildInnerWhereClause(poProjectType, flag))
                 .append(buildOuterWhereClause(billableType, deptIds));;
            
        } else if ("EC".equalsIgnoreCase(dto.getReport())) {
            // === Employee Consolidated Query ===
            query.append("SELECT e.emp_id, e.employeement_id, e.name, e.email, e.mobile_no, ")
                 .append("e.manager_id, m.name as ManagerName, e.employmentstatus, e.billable, e.billable_type, ")
                 .append("emp_proj_client.team_id, emp_proj_client.team_name, emp_proj_client.project_id, ")
                 .append("emp_proj_client.project_name, emp_proj_client.po_start_date, emp_proj_client.po_end_date, ")
                 .append("emp_proj_client.po_no, emp_proj_client.client_name, emp_proj_client.client_location, e.work_location, ")
                 .append("e.total_experience, d.dept_id, d.name as departmentName, emp_proj_client.po_project_type, j.name as jobrole, ")
                 .append("emp_proj_client.po_project_id, eppm.primary_project_name, eppm.primary_project_id, emp_proj_client.clientrm, ")
                 .append("emp_proj_client.apmosysrm, emp_proj_client.effective_start_date, emp_proj_client.effective_end_date FROM employee e ")
                 .append("INNER JOIN job_role j ON j.job_role_id = e.job_role_id ")
                 .append("INNER JOIN department d ON d.dept_id = j.dept_id ")
                 .append("INNER JOIN employee m ON e.manager_id = m.emp_id ")
                 .append("LEFT JOIN emp_primary_project_mapping eppm ON eppm.emp_id = e.emp_id ")
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
                 .append(buildOuterWhereClause(billableType, deptIds));  

        } else if ("E".equalsIgnoreCase(dto.getReport())) {
            // === Employee Query ===
            query.append("SELECT e.emp_id, e.employeement_id, e.name, e.email, e.mobile_no, ")
	             .append("e.manager_id, m.name as ManagerName, e.employmentstatus, e.billable, e.billable_type, ")
	             .append("t.team_id, t.team_name, p.project_id, ")
	             .append("p.project_name, p.po_start_date, p.po_end_date, ")
	             .append("p.po_no, c.client_name, cl.client_location, e.work_location, ")
	             .append("e.total_experience, d.dept_id, d.name as departmentName, p.po_project_type, j.name as jobrole, ")
	             .append("p.po_project_id, eppm.primary_project_name, eppm.primary_project_id, p.clientrm, ")
	             .append("p.apmosysrm, etm.start_date as effective_start_date, etm.end_date as effective_end_date FROM employee e ")
                 .append("INNER JOIN employee_team_mapping etm ON etm.emp_id = e.emp_id ")
                 .append("LEFT JOIN teams t ON t.team_id = etm.team_id ")
                 .append("LEFT JOIN projects p ON p.project_id = t.project_id ")
                 .append("INNER JOIN job_role j ON j.job_role_id = e.job_role_id ")
                 .append("INNER JOIN department d ON d.dept_id = j.dept_id ")
                 .append("INNER JOIN employee m ON m.emp_id = e.manager_id ")
                 .append("INNER JOIN client_locations cl ON cl.client_id = p.client_id ")
                 .append("INNER JOIN clients c ON c.client_id = p.client_id ")
                 .append("LEFT JOIN emp_primary_project_mapping eppm ON eppm.emp_id = e.emp_id ")
                 .append("WHERE e.employmentstatus != 'InActive' AND etm.active != 0 AND t.is_active != 'N' AND p.active != 'false' AND p.project_id IS NOT NULL and e.emp_id not between 1 and 6 ")
	             .append(buildInnerWhereClause(poProjectType, flag))
	             .append(buildOuterWhereClause(billableType, deptIds));
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

	public String buildOuterWhereClause(List<String> billableType, List<Long> deptIds) {
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

	    return outerWhere.toString();
	}
	
	public String buildProjectSummaryQuery(GetEmployeeProjectReportPayloadDTO dto, List<Long> deptId) {
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
	    		+ "    WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false' and e.emp_id not between 1 and 6 ");

	    if (dto != null) {
	        query.append(buildInnerWhereClause(dto.getPoProjectType(), dto.getFlag()))
	             .append(buildOuterWhereClause(dto.getBillableType(), deptId));
	    } else {
	        query.append(buildInnerWhereClause(null, null))
	             .append(buildOuterWhereClause(null, deptId));
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
	    		+ "    WHERE etm.active != 0 AND t.is_active != 'N' AND p.active != 'false' and e.emp_id not between 1 and 6 ");

	    if (dto != null) {
	        query.append(buildInnerWhereClause(dto.getPoProjectType(), dto.getFlag()))
	             .append(buildOuterWhereClause(dto.getBillableType(), deptId));
	    }else {
	        query.append(buildInnerWhereClause(null, null))
            .append(buildOuterWhereClause(null, deptId));
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
	    		+ "    COALESCE(sa.total_projects_per_po_project, 0) AS total_projects_per_po_project\n"
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
	        String filteredQuery = buildProjectSummaryQuery(dto, dto.getDeptId());
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

	    String baseQuery = buildProjectSummaryQuery(null, dto.getDeptId());
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
	        
	        
	        Map<String, Map<String, Map<String, Object>>> summary = getProjectSummary(dto);
	        reportDTO.setProjectSummary(summary);
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

		            teamMap.get(teamKey).getMappedEmployeeDetails().add(empDTO);
		        }
		        
		        return new GetEmployeeProjectReportDTO(null, new ArrayList<>(projectMap.values()), null);

		    } catch (Exception e) {
		        e.printStackTrace();
		        return new GetEmployeeProjectReportDTO(null, null, null);
		    }
		}
	 
	 @Transactional
	 public ServiceResponse handleTeamsAsPerLinkedPo(HandleTeamsAsPerLinkedPoPayloadDTO payloadDTO) {
	     ServiceResponse response = new ServiceResponse();
	     LogDTO apiLogInfo = new LogDTO();
	     apiLogInfo.setApiUrl("/api/handleTeamsAsPerLinkedPo");
	     apiLogInfo.setLogLevel("INFO");
	     StringBuilder logBuilder = new StringBuilder();

	     try {
	         if (payloadDTO.getPrimaryProject() == null) {
	             response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	             response.setServiceResponse("Primary project list received at Ishine is empty!");
	             apiLogInfo.setApiResponse("Empty data(Primary project list) received at Ishine");
	             apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	             return response;
	         }

	         HandleTeamsAsPerLinkedPoProjectDTO primaryProjectDTO = payloadDTO.getPrimaryProject();
	         List<Object[]> primaryTeams = projectRepository.getTeamIdsForPoProjectId(primaryProjectDTO.getProjectId());

	         if (primaryTeams.isEmpty()) {
	             response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	             response.setServiceResponse("The resource onboarding procees to teams has not started for "+primaryProjectDTO.getProjectName().toString()+ ". Therefore not able to proceed with link PO. Kindly contact the RMG team to start the onboarding proccess for the "+primaryProjectDTO.getProjectName().toString()+".");
	             apiLogInfo.setApiResponse("Project has no team created in Ishine");
	             apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	             return response;
	         }

	         Set<String> primaryTeamNames = primaryTeams.stream()
	                 .map(t -> t[1].toString())
	                 .collect(Collectors.toSet());

	         if (payloadDTO.getDeletedProjects().isEmpty()) {
	             response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	             response.setServiceResponse("Deleted project list received at Ishine is empty.");
	             apiLogInfo.setApiResponse("Deleted project list at Ishine is empty.");
	             apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	             return response;
	         }

	         DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	         for (HandleTeamsAsPerLinkedPoProjectDTO deletedProject : payloadDTO.getDeletedProjects()) {
	             List<Object[]> deletedTeams = projectRepository.getTeamIdsForPoProjectId(deletedProject.getProjectId());

	             for (Object[] team : deletedTeams) {
	                 Long teamId = team[0] != null ? Long.parseLong(team[0].toString()) : null;
	                 String teamName = team[1] != null ? team[1].toString() : null;

	                 if (teamId == null || teamName == null) {
	                     apiLogInfo.setApiResponse("No team found for this project " + primaryProjectDTO.getProjectName().toString() + " in Ishine");
	                     continue;
	                 }

	                 String newTeamName = primaryTeamNames.contains(teamName)
	                         ? teamName + " | " + deletedProject.getProjectName()
	                         : teamName;

	                 teamRepository.updateTeamName(teamId, newTeamName);
	                 teamRepository.updateTeamProjectByPoProjectId(teamId, Long.parseLong(primaryProjectDTO.getProjectId().toString()));
	             }

	             Project deletedProjEntity = projectRepository.findByPoProjectId(deletedProject.getProjectId());
	             if (deletedProjEntity != null) {
	                 deletedProjEntity.setActive("false");
	                 deletedProjEntity.setPoNo(deletedProject.getPoNo());
	                 deletedProjEntity.setClientId(deletedProject.getClientId());
	                 deletedProjEntity.setPoStartDate(dateFormatter.format(deletedProject.getStartDate().toLocalDateTime().toLocalDate()));
	                 deletedProjEntity.setPoEndDate(dateFormatter.format(deletedProject.getEndDate().toLocalDateTime().toLocalDate()));
	                 deletedProjEntity.setProjectName(deletedProject.getProjectName());
	                 deletedProjEntity.setUpdatedOn(LocalDateTime.now());

	                 projectRepository.save(deletedProjEntity);
	             } else {
	            	 apiLogInfo.setApiResponse("No project found for poProjectId: " + deletedProject.getProjectId());
	             }
	         }

	         Project primaryProjectEntity = projectRepository.findByPoProjectId(primaryProjectDTO.getProjectId());
	         if (primaryProjectEntity != null) {
	             primaryProjectEntity.setPoNo(primaryProjectDTO.getPoNo());
	             primaryProjectEntity.setClientId(primaryProjectDTO.getClientId());
	             primaryProjectEntity.setPoStartDate(dateFormatter.format(primaryProjectDTO.getStartDate().toLocalDateTime().toLocalDate()));
	             primaryProjectEntity.setPoEndDate(dateFormatter.format(primaryProjectDTO.getEndDate().toLocalDateTime().toLocalDate()));
	             primaryProjectEntity.setProjectName(primaryProjectDTO.getProjectName());
	             primaryProjectEntity.setUpdatedOn(LocalDateTime.now());
	             primaryProjectEntity.setIsDraftProject("true");

	             projectRepository.save(primaryProjectEntity);
	         }

	         response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	         response.setServiceResponse("Teams reassigned successfully.");
	         apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

	     } catch (NullPointerException ex) {
    	    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
    	    response.setServiceResponse("Null value encountered.");
    	    response.setServiceError(ex.getMessage());
    	    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
    	    apiLogInfo.setLogLevel("ERROR");
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
	 
	 @Transactional
	 public ServiceResponse getResourceRequirementFromPoPortal() {
	     ServiceResponse serviceResponse = new ServiceResponse();
	     LogDTO apiLogInfo = new LogDTO();
	     apiLogInfo.setSubFeatureName("getResourceRequirementFromPoPortal");
	     apiLogInfo.setApiUrl("/api/getResourceRequirementFromPoPortal");
	     apiLogInfo.setLogLevel("INFO");

	     StringBuilder logBuilder = new StringBuilder();
	     logBuilder.append("API to update existing resource requirement table\n");
	     System.out.println("API to update existing resource requirement table");

	     List<ProjectPoPortalDTO> list = new ArrayList<>();

	     try {
	         ProjectPoPortalDTO[] projects = restTemplate.getForObject(allPoPortalProjects, ProjectPoPortalDTO[].class);
	         list = Arrays.asList(projects != null ? projects : new ProjectPoPortalDTO[0]);
	         String msg = "Total Projects Fetched = " + list.size() + "\n";
	         logBuilder.append(msg);
	         System.out.println(msg);
	     } catch (RestClientException e) {
	         String msg = "Error fetching projects from PoPortal API: " + e.getMessage();
	         logBuilder.append(msg);
	         System.out.println(msg);
	         serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	         serviceResponse.setServiceResponse(msg);
	         apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	         apiLogInfo.setApiResponse(msg);
	         return serviceResponse;
	     }

	     if (!list.isEmpty()) {
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
	     } else {
	         String msg = "No projects found from PoPortal API.\n";
	         logBuilder.append(msg);
	         System.out.println(msg);
	     }

	     apiLogInfo.setApiResponse(logBuilder.toString());
	     apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	     serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	     serviceResponse.setServiceResponse("Sync completed. Summary:\n" + logBuilder);
	     return serviceResponse;
	 }

	public ServiceResponse getProjectWithCliendSideID() {
		
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("getProjectWithCliendSideID");
        apiLogInfo.setApiUrl("/api/getProjectWithCliendSideID");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        try {
        	
		List<Object[]>  details = projectRepository.getProjectWithCliendSideID();
		List<ProjectFetchDTO> dtoList = new ArrayList<ProjectFetchDTO>();
		
       if(details != null) {
		for(Object[] object:details) {
			ProjectFetchDTO projectDetails= new ProjectFetchDTO();
			projectDetails.setProjectId(object[2] != null ? Integer.valueOf(object[2].toString()) : null);
			projectDetails.setProjectName(object[0] != null ? object[0].toString() : null);
			projectDetails.setPoNo(object[1] != null ? object[1].toString() : null)			;
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


}
