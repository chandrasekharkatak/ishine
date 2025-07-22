package com.apmosys.employeeportal.service;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.management.RuntimeErrorException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.xml.bind.DataBindingException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpServerErrorException.InternalServerError;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.apmosys.employeeportal.dto.BenchEmployeeDetailsDTO;
import com.apmosys.employeeportal.dto.CombinedPOInternalProjectResponse;
import com.apmosys.employeeportal.dto.DefaultProjectUpdateDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.EmployeeDetailsForTeamMemberDTO;
import com.apmosys.employeeportal.dto.EmployeeInformationDTO;
import com.apmosys.employeeportal.dto.EmployeeTeamMapDTO;
import com.apmosys.employeeportal.dto.ExceptionReportDTO;
import com.apmosys.employeeportal.dto.FCLineItemDTO;
import com.apmosys.employeeportal.dto.FCProjectMilestoneDTO;
import com.apmosys.employeeportal.dto.GetActiveProjectDetailsIfMultipleDTO;
import com.apmosys.employeeportal.dto.GetDeptIdByRoleDTO;
import com.apmosys.employeeportal.dto.GetEmployeeByNameAndEmpldDTO;
import com.apmosys.employeeportal.dto.GetEmployeeInformationForDefaultProjectDTO;
import com.apmosys.employeeportal.dto.GetEmployeeProjectReportPayloadDTO;
import com.apmosys.employeeportal.dto.GetPreviousDefaultProjectDetailsDTO;
import com.apmosys.employeeportal.dto.GetProjectDetailsForBulkDefaultUpdateProjectDTO;
import com.apmosys.employeeportal.dto.GetProjectDetailsForBulkDefaultUpdateTeamDTO;
import com.apmosys.employeeportal.dto.GetProjectToEmployeeReportForEmployeeDTO;
import com.apmosys.employeeportal.dto.GetProjectToEmployeeReportForProjectDTO;
import com.apmosys.employeeportal.dto.GetProjectToEmployeeReportForTeamDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.NonComplianceProjects;
import com.apmosys.employeeportal.dto.OtherProjectSetDTO;
import com.apmosys.employeeportal.dto.PoProjectSyncDTO;
import com.apmosys.employeeportal.dto.PoTeamDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.ProjectFetchDTO;
import com.apmosys.employeeportal.dto.ProjectFilterDTO;
import com.apmosys.employeeportal.dto.ProjectInfoDTO;
import com.apmosys.employeeportal.dto.ProjectManagersDTO;
import com.apmosys.employeeportal.dto.ProjectOverheadsDTO;
import com.apmosys.employeeportal.dto.ProjectRequirementsDTO;
import com.apmosys.employeeportal.dto.RMGFlatEmployeeProjectTeamDTO;
import com.apmosys.employeeportal.dto.RMGProject;
import com.apmosys.employeeportal.dto.RMGProjectMappedEmployees;
import com.apmosys.employeeportal.dto.RMGProjectToEmployeeFlatDTO;
import com.apmosys.employeeportal.dto.RMGTeam;
import com.apmosys.employeeportal.dto.ResourceManagementDTO;
import com.apmosys.employeeportal.dto.ResourceRequirementDTO;
import com.apmosys.employeeportal.dto.SetProjectMappingAndDefaultProjectDTO;
import com.apmosys.employeeportal.dto.SpocDTO;
import com.apmosys.employeeportal.dto.SummaryChartDTO;
import com.apmosys.employeeportal.dto.TeamDTO;
import com.apmosys.employeeportal.dto.TeamInfoProjectDTO;
import com.apmosys.employeeportal.dto.TeamInfoTeamDTO;
import com.apmosys.employeeportal.dto.TeamInfoTeamMemberDTO;
import com.apmosys.employeeportal.dto.TeamMemberDTO;
import com.apmosys.employeeportal.dto.TeamSpocDTO;
import com.apmosys.employeeportal.exception.BadRequestException;
import com.apmosys.employeeportal.exception.ConflictException;
import com.apmosys.employeeportal.exception.DataNotFoundException;
import com.apmosys.employeeportal.model.Activity;
import com.apmosys.employeeportal.model.ActivityTemplate;
import com.apmosys.employeeportal.model.ApiLog;
import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.ClientLocation;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.EmpPrimaryProjectMapping;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeTeamMap;
import com.apmosys.employeeportal.model.FCLineItem;
import com.apmosys.employeeportal.model.FCProjectMilestone;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectDepartmentMap;
import com.apmosys.employeeportal.model.ProjectManagerMapping;
import com.apmosys.employeeportal.model.ProjectOverheadMapping;
import com.apmosys.employeeportal.model.ProjectTemp;
import com.apmosys.employeeportal.model.ResourceRequirement;
import com.apmosys.employeeportal.model.ResourceRequirementTemp;
import com.apmosys.employeeportal.model.Team;
import com.apmosys.employeeportal.repository.ActivitiesRepository;
import com.apmosys.employeeportal.repository.ActivityTemplateRepository;
import com.apmosys.employeeportal.repository.ClientLocationRepository;
import com.apmosys.employeeportal.repository.ClientsRepository;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmpPrimaryProjectMappingRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.FCLineItemRepository;
import com.apmosys.employeeportal.repository.FCProjectMilestoneRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.ProjectDepartmentMapRepository;
import com.apmosys.employeeportal.repository.ProjectManagerMappingRepository;
import com.apmosys.employeeportal.repository.ProjectOverheadMappingRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.ProjectTempRepo;
import com.apmosys.employeeportal.repository.ResourceRequirementRepository;
import com.apmosys.employeeportal.repository.ResourceRequirementTempRepo;
import com.apmosys.employeeportal.repository.TeamRepository;
import com.apmosys.employeeportal.response.ResourceRequirementResponse;
import com.apmosys.employeeportal.utility.ApiLogUtility;
import com.apmosys.employeeportal.utility.PoPortalAPIAuthenticationJWTUtility;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

@Service
public class ResourceManagementService {

	@Autowired
	ProjectService projectService;

	@Autowired
	ProjectRepository projectRepository;

	@Autowired
	ClientLocationRepository clientLocationRepository;

	@Autowired
	DepartmentRepository departmentRepository;

	@Autowired
	ProjectDepartmentMapRepository projectDepartmentMapRepository;

	@Autowired
	TeamRepository teamRepository;

	@Autowired
	ClientsRepository clientsRepository;
	
	@Autowired
	EmpPrimaryProjectMappingRepository empPrimaryProjectMappingRepository;

	@Autowired
	ProjectManagerMappingRepository projectManagerMappingRepository;

	@Autowired
	ActivitiesRepository activitiesRepository;

	@Autowired
	ActivityTemplateRepository activityTemplateRepository;

	@Autowired
	EmployeeTeamMapRepository employeeTeamMapRepository;

	@Autowired
	EmployeeRepository employeeRepository;

	@Autowired
	ResourceRequirementRepository resourceRequirementRepository;
	
	@Autowired
	ResourceRequirementTempRepo resourceRequirementTempRepo;
	
	@Autowired
	ProjectTempRepo projectTempRepo;
	
	
	@Autowired
	ProjectOverheadMappingRepository projectOverheadMappingRepository;

	@Autowired
	StringToDateTimeParser stringToDateTimeParser;
	
	@Autowired
	 PoPortalAPIService poPortalAPIService;

	@Autowired
	MailService mailService;

	@Autowired
	private HttpServletRequest httpRequest;

	@Autowired
	private LogService logService;

	@Autowired
	private JobRoleRepository jobRoleRepository;
	
	@Autowired 
	private FCLineItemRepository fCLineItemRepository;
	
	@Autowired 
	private FCProjectMilestoneRepository fCProjectMilestoneRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Value("${rmg.mail}")
	private String rmgMail;

	@Value("${poPortal.api.syncProject}")
	private String syncProjectApi;

	@Value("${rmg.project.approval.link}")
	private String rmgProjectApprovalLink;

	@Value("${admin.mail}")
	private String adminMail;

	@Value("${bd.mail}")
	private String bdMail;

	@Value("${poPortal.api.allProjects}")
	private String allPoPortalProjects;
	
//	@Value("${poPortal.api.realtimeProjectData}")
//	private String realtimePoProjectData;

	@Autowired
	private final RestTemplate restTemplate = new RestTemplate();

	@Autowired
	private PoPortalAPIAuthenticationJWTUtility poPortalAPIAuthenticationJWTUtility;

	@Autowired
	private ApiLogUtility apiLogUtility;

	
//	public ServiceResponse createDraftProjectInfo(ResourceManagementDTO resourceManagementDTO) {
//		ServiceResponse response = new ServiceResponse();
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setSubFeatureName("createDraftProjectInfo");
//		apiLogInfo.setApiUrl("/api/createDraftProjectInfo");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("ProjectType : " + resourceManagementDTO.getProjectType() + " ,ProjectId :"
//				+ resourceManagementDTO.getProjectId() + " ,ProjectName :" + resourceManagementDTO.getName()
//				+ " ,Department :" + resourceManagementDTO.getDeptName() + " ,State:"
//				+ resourceManagementDTO.getClientState());
//
//		try {
//			Project projObj = null;
//			if (resourceManagementDTO.getProjectType().equals("Internal")) {
//				projObj = projectRepository.findByProjectId(resourceManagementDTO.getProjectId());
//
//			} else {
//				projObj = projectRepository.findByPoProjectId(resourceManagementDTO.getId());
//			}
//
//			System.err.println("Project Type" + projObj);
//
//			Project projectObj = projObj;
//			Employee employeeObj = employeeRepository.findByEmpId(resourceManagementDTO.getCreatedBy());
//
//			List<Long> allTeam = new ArrayList<>();
//
//			if (projectObj != null) {
//				ServiceResponse response1 = new ServiceResponse();
//				response1 = this.projectIsPresent(projectObj, resourceManagementDTO, allTeam);
//				if (response1.getServiceStatus() != "Success") {
//					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//					response.setServiceResponse(response1.getServiceResponse());
//					apiLogInfo.setApiResponse("Project not updated successfully");
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//				} else {
//					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//					response.setServiceResponse(response1.getServiceResponse());
//					apiLogInfo.setApiResponse("Project updated successfully");
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//				}
//
//			} else {
//				Integer clientId = null;
//				Optional<Client> clientObj = clientsRepository.findByClientName(resourceManagementDTO.getClientName());
//				if (!clientObj.isEmpty()) {
//					Client clientPresent = clientObj.get();
//					clientId = clientPresent.getClientId();
//				} else {
//					// Add Client & Client Location
//					Client newClient = new Client();
//					newClient.setClientName(resourceManagementDTO.getClientName());
//					Client clientDbResponse = clientsRepository.save(newClient);
//
//					if (clientDbResponse != null) {
//						clientId = clientDbResponse.getClientId();
//						List<ClientLocation> locations = new ArrayList<>();
//
//						for (String clientLocation : resourceManagementDTO.getClientLocation()) {
//							ClientLocation newClientLocation = new ClientLocation();
//							newClientLocation.setClientId(clientDbResponse.getClientId());
//							newClientLocation.setClientLocation(clientLocation);
//							locations.add(newClientLocation);
//						}
//
//						// Add WFH location
//						boolean contains = Arrays.stream(resourceManagementDTO.getClientLocation())
//								.anyMatch("WFH"::equals);
//						if (!contains) {
//							ClientLocation newClientLocation = new ClientLocation();
//							newClientLocation.setClientId(clientDbResponse.getClientId());
//							newClientLocation.setClientLocation("WFH");
//							locations.add(newClientLocation);
//						}
//
//						List<ClientLocation> clientLocationDbResponse = clientLocationRepository.saveAll(locations);
//
//						if (!clientLocationDbResponse.isEmpty()) {
//							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//							response.setServiceResponse("Client Location added");
//							apiLogInfo.setApiResponse("Client Location added");
//							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//						} else {
//							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//							response.setServiceResponse("Failed to add client Location");
//							apiLogInfo.setApiResponse("Failed to add client Location");
//							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//							return response;
//						}
//					} else {
//						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//						response.setServiceResponse("Failed to add client");
//						apiLogInfo.setApiResponse("Failed to add client");
//						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//						return response;
//					}
//				}
//				// Add project
//				Project newProject = new Project();
//				newProject.setProjectName(resourceManagementDTO.getName());
//				newProject.setState(resourceManagementDTO.getState());
//				newProject.setClientId(clientId);
//				newProject.setPoProjectId(resourceManagementDTO.getId());
//				newProject.setActive("true");
//				newProject.setSyncProject("true");
//				newProject.setPoProjectType("Internal".equalsIgnoreCase(resourceManagementDTO.getProjectType()) ? null
//						: resourceManagementDTO.getProjectType());
//
//				newProject.setPoNo(resourceManagementDTO.getPoNo());
//				newProject.setPoStartDate(resourceManagementDTO.getPoStartDate());
//				newProject.setPoEndDate(resourceManagementDTO.getPoEndDate());
//				newProject.setCreatedOn(new Timestamp(System.currentTimeMillis()));
//				newProject.setApmosysRM(resourceManagementDTO.getApmosysRM());
//				newProject.setIsRenewable(resourceManagementDTO.getIsRenewable());
//				newProject.setClientRM(resourceManagementDTO.getClientRM());
//				newProject.setApmosysRmEmail(resourceManagementDTO.getApmosysRmEmail());
//
////				if (resourceManagementDTO.getIsHOD().equals("true")) {
////					newProject.setIsDraftProject("false");
////				} else {
//					newProject.setIsDraftProject("true");
////				}
//
//				newProject.setCreatedBy(resourceManagementDTO.getCreatedBy());
//
//				Project projectDbResponse = projectRepository.save(newProject);
//				ProjectTemp	projObj1= null;
//				if (resourceManagementDTO.getProjectType().equals("Internal")) {
//					projObj = projectRepository.findByProjectId(resourceManagementDTO.getProjectId());
//
//				} else {
//						projObj1=projectTempRepo.findByPoProjectId(resourceManagementDTO.getId());
////					projObj = projectTempRepo.(resourceManagementDTO.getId());
//				}
//				projObj1.setIsDraftProject("Pending") ;
//				ProjectTemp temp=projectTempRepo.save(projObj1);
//				
//				if (projectDbResponse != null) {
//					// Add project Department Mapping
//					for (String department : resourceManagementDTO.getDepartment()) {
//						Department departmentObj = departmentRepository.findByName(department);
//						if (departmentObj != null) {
//							ProjectDepartmentMap projectDeptMapObj = projectDepartmentMapRepository
//									.findByProjectIdAndDeptId(projectDbResponse.getProjectId(),
//											departmentObj.getDeptId());
//							if (projectDeptMapObj == null) {
//								// Add department
//								ProjectDepartmentMap projectDeptMap = new ProjectDepartmentMap();
//								projectDeptMap.setProjectId(projectDbResponse.getProjectId());
//								projectDeptMap.setDeptId(departmentObj.getDeptId());
//								ProjectDepartmentMap projDeptMapDbResponse = projectDepartmentMapRepository
//										.save(projectDeptMap);
//							}
//						}
//					}
//
//					ServiceResponse responseProjectManager = this.setProjectManager(resourceManagementDTO,
//							projectDbResponse);
//
//					if (responseProjectManager.getServiceStatus() != "Success") {
//						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//						response.setServiceResponse(responseProjectManager.getServiceResponse());
//					} else {
//						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//						response.setServiceResponse(responseProjectManager.getServiceResponse());
//					}
//					
//					ServiceResponse responseProjectOverhead = this.setProjectOverheads(resourceManagementDTO,
//							projectDbResponse);
//					
//					if (responseProjectOverhead.getServiceStatus() != "Success") {
//						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//						response.setServiceResponse(responseProjectOverhead.getServiceResponse());
//					} else {
//						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//						response.setServiceResponse(responseProjectOverhead.getServiceResponse());
//					}
//					
//					if (!resourceManagementDTO.getResourceRequirements().isEmpty()) {
//						resourceManagementDTO.getResourceRequirements().forEach(req -> {
//							
//							Long overviewId = req.getResourceOverviewId() != null
//					                ? Long.parseLong(req.getResourceOverviewId().toString())
//					                : null;
//
//					        if (overviewId != null && resourceRequirementRepository.existsById(overviewId)) {
//					            return; 
//					        }
//
//							ResourceRequirement dto = new ResourceRequirement();
//
//							dto.setCount(req.getCount());
//							dto.setDepartment(req.getDepartment());
//							dto.setExperience(req.getExperience());
//							dto.setRole(req.getRole());
//							dto.setResourceOverviewId(
//									req.getResourceOverviewId() != null ? Long.parseLong(req.getResourceOverviewId().toString())
//											: null);
//							dto.setProjectId(projectDbResponse.getProjectId());
//
//							resourceRequirementRepository.save(dto);
//						});
//					}
//
//					if (resourceManagementDTO.getTeamList() != null && !resourceManagementDTO.getTeamList().isEmpty()) {
//						resourceManagementDTO.getTeamList().forEach(teamObj -> {
//							// your logic here
//							System.out.println("Inside loop: " + teamObj.getTeamName());
//
//							// Create a new team
//							StringBuilder deptList = new StringBuilder("");
//							for (String department : teamObj.getDepartmentList()) {
//								deptList.append(department).append(",");
//							}
//							// TeamLead
//							Long teamLeadId = null;
//							String teamLeadName = null;
//							for (TeamMemberDTO teamMember : teamObj.getTeamMemberList()) {
//								if ((teamMember.getIsTeamLead() != null)
//										&& (teamMember.getIsTeamLead().equals("true"))) {
//									teamLeadId = teamMember.getEmpId();
//									teamLeadName = teamMember.getName();
//								}
//							}
//
//							Team newTeamObj = new Team();
//							newTeamObj.setIsActive("Y");
//							newTeamObj.setProjectId(projectDbResponse.getProjectId());
//							newTeamObj.setTeamLeadId(teamLeadId);
//							newTeamObj.setTeamName(teamObj.getTeamName());
//							newTeamObj.setTeamLeadName(teamLeadName);
//							newTeamObj.setDeptIds(deptList.toString());
//							newTeamObj.setDeptIds(deptList.toString());
//							newTeamObj.setCreatedBy(resourceManagementDTO.getCreatedBy());
//							newTeamObj.setCreatedOn(new Timestamp(System.currentTimeMillis()));
//							newTeamObj.setSpocId(teamObj.getSpocId());
//							Team teamDbResponse = teamRepository.save(newTeamObj);
//                           StringBuilder emailBody = new StringBuilder();
//							emailBody.append("<html><body>") // Wrap email content inside HTML tags
//									.append("Dear RMG,<br><br>")
//									.append("The Team has been created with the team name - <b>")
//									.append(teamDbResponse.getTeamName()).append("</b><br><br>")
//									.append("<table border='1' style='border-collapse: collapse; width: 100%;'>")
//									.append("<tr>")
//									.append("<th style='padding: 8px; text-align: left;'>Employment ID</th>")
//									.append("<th style='padding: 8px; text-align: left;'>Employee Name</th>")
//									.append("<th style='padding: 8px; text-align: left;'>Job Role</th>")
//									.append("<th style='padding: 8px; text-align: left;'>Department</th>")
//									.append("<th style='padding: 8px; text-align: left;'>Start Date</th>")
////    			                     .append("<th style='padding: 8px; text-align: left;'>Employee Role</th>")
//									.append("</tr>");
//
//							if (teamDbResponse != null) {
//								List<Long> defaultProjectEmpIds = new ArrayList<Long>();
//								List<EmployeeTeamMap> mapList = new ArrayList<EmployeeTeamMap>();
//								String ccMail = adminMail;
//								// Add team member in the team
//								for (TeamMemberDTO teamMember : teamObj.getTeamMemberList()) {
//									EmployeeTeamMap newEmpTeamMap = new EmployeeTeamMap();
//							
//
//									List<EmployeeDetailsForTeamMemberDTO> dtoList = new ArrayList<EmployeeDetailsForTeamMemberDTO>();
//									List<EmployeeDetailsForTeamMemberDTO> employeeDetails = employeeRepository
//											.getEmployeeDetailsForTeam(teamMember.getEmpId());
//
//									if (employeeDetails != null && !employeeDetails.isEmpty()) {
//										dtoList.addAll(employeeDetails);
//										//										employeeDetails.forEach((object) -> {
////											EmployeeDetailsForTeamMemberDTO dto = new EmployeeDetailsForTeamMemberDTO();
////
////											dto.setEmpId(
////													object[0] != null ? Long.parseLong(object[0].toString()) : null);
////											dto.setEmployeementId(
////													object[1] != null ? Long.parseLong(object[1].toString()) : null);
////											dto.setName(object[2] != null ? object[2].toString() : null);
////											dto.setJobRoleId(
////													object[3] != null ? Long.parseLong(object[3].toString()) : null);
////											dto.setJobRoleName(object[4] != null ? object[4].toString() : null);
////											dto.setDeptId(
////													object[5] != null ? Long.parseLong(object[5].toString()) : null);
////											dto.setDeptName(object[6] != null ? object[6].toString() : null);
////											dto.setIsConsultant(object[7] != null ? object[7].toString() : null);
////
////											dtoList.add(dto);
////										});
//									}
//									// it is returning multiple resuts................///////////////////
//									String hodMail = employeeRepository.findHodMail(teamMember.getEmpId());
//
//									ccMail = ccMail + "," + hodMail;
//
//									// TeamLead
//									if ((teamMember.getIsTeamLead() != null)
//											&& (teamMember.getIsTeamLead().equals("true"))) {
//										newEmpTeamMap.setEmpId(teamMember.getEmpId());
//										newEmpTeamMap.setActive(2L); // Set Active to 2 for TeamLead
//										newEmpTeamMap.setEmployeeRole("TeamLead");
//										newEmpTeamMap.setTeamId(teamDbResponse.getTeamId());
//										newEmpTeamMap.setStartDate(new Timestamp(System.currentTimeMillis())); 
//										newEmpTeamMap.setIsShadow(teamMember.getIsShadow() != null ? teamMember.getIsShadow(): null);
//										newEmpTeamMap.setResourceOverviewId(teamMember.getResourceOverviewId() != null
//												? Long.parseLong(teamMember.getResourceOverviewId().toString())
//												: null);
//										newEmpTeamMap.setCreatedBy(teamMember.getCreatedBy() != null ? teamMember.getCreatedBy() : null);
//										newEmpTeamMap.setCreatedOn(new Timestamp(System.currentTimeMillis()));
//										
//										if(teamMember.getIsDefaultProject() != null) {
//											if(teamMember.getIsDefaultProject() == 1)
//												defaultProjectEmpIds.add(teamMember.getEmpId());
//										}
//										
//										mapList.add(newEmpTeamMap);
//									} else {
//										StringBuilder employeeRole = new StringBuilder("");
//										for (String empRole : teamMember.getEmployeeRole()) {
//											employeeRole.append(empRole).append(",");
//										}
//
//										newEmpTeamMap.setEmpId(teamMember.getEmpId());
//										newEmpTeamMap.setActive(2L); // Set Active to 2 for Team Member
//										newEmpTeamMap.setEmployeeRole(employeeRole.toString());
//										newEmpTeamMap.setStartDate(new Timestamp(System.currentTimeMillis())); 
//										newEmpTeamMap.setTeamId(teamDbResponse.getTeamId());
//										newEmpTeamMap.setIsShadow(teamMember.getIsShadow() != null ? teamMember.getIsShadow() : null);
//										newEmpTeamMap.setResourceOverviewId(teamMember.getResourceOverviewId() != null
//												? Long.parseLong(teamMember.getResourceOverviewId().toString())
//												: null);
//										
//										if(teamMember.getIsDefaultProject() != null) {
//											if(teamMember.getIsDefaultProject() == 1)
//												defaultProjectEmpIds.add(teamMember.getEmpId());
//										}
//										newEmpTeamMap.setCreatedBy(teamMember.getCreatedBy() != null ? teamMember.getCreatedBy() : null);
//										newEmpTeamMap.setCreatedOn(new Timestamp(System.currentTimeMillis()));
//										
//										mapList.add(newEmpTeamMap);
//									}
//
//									for (EmployeeDetailsForTeamMemberDTO dto : dtoList) {
//										if (dto != null) {
//											String employmentId = "A-" + dto.getEmployeementId();
//											if ("true".equalsIgnoreCase(dto.getIsConsultant())) {
//												employmentId = "CS-" + dto.getEmployeementId();
//											}
//
//											String employeeRole = teamMember.getIsTeamLead() != null
//													&& teamMember.getIsTeamLead().equalsIgnoreCase("true") ? "TeamLead"
//															: String.join(",", teamMember.getEmployeeRole());
//
//											emailBody.append("<tr>").append("<td style='padding: 8px;'>")
//													.append(employmentId).append("</td>")
//													.append("<td style='padding: 8px;'>").append(dto.getEmployeeName())
//													.append("</td>").append("<td style='padding: 8px;'>")
//													.append(dto.getJobRoleName()).append("</td>")
//													.append("<td style='padding: 8px;'>").append(dto.getDeptName())
//													.append("</td>").append("<td style='padding: 8px;'>")
//													.append(dto.getStartDate()).append("</td>")
////        			                        .append("<td style='padding: 8px;'>").append(employeeRole).append("</td>")
//													.append("</tr>");
//										}
//
//									}
//
//								}
//								List<EmployeeTeamMap> teamMemberDbResponse = employeeTeamMapRepository.saveAll(mapList);
//								
//								if(!defaultProjectEmpIds.isEmpty())		{
//									ServiceResponse defaultProjectResponse = this.handleDefaultProjectUpdate(defaultProjectEmpIds, projectDbResponse.getProjectId(), resourceManagementDTO.getCreatedBy(), logBuilder);
//
//									if (ServiceResponse.STATUS_SUCCESS.equals(defaultProjectResponse.getServiceStatus())) {
//									    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//									    response.setServiceResponse(defaultProjectResponse.getServiceResponse());
//									} else {
//									    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//									    response.setServiceResponse(defaultProjectResponse.getServiceResponse());
//									}
//								}
//
//								emailBody.append("</table><br><br>").append("Sincerely,<br>")
//										.append("<b>Team RMG - ApMoSys Technologies</b>").append("</body></html>");
//
//								try {
//									mailService.sendMailWithCC(ccMail, rmgMail, "Regarding Team Creation",
//											emailBody.toString());
//								} catch (MessagingException e) {
//									e.printStackTrace();
//								}
//
//								// Add default activity
////    			                this.activityRealtedToTeam(teamMemberDbResponse,teamObj);
//								ServiceResponse response3 = this.activityRealtedToTeam(teamMemberDbResponse, teamObj,
//										resourceManagementDTO, teamDbResponse);
//
//								if (response3.getServiceStatus() != "Success") {
//									response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//									response.setServiceResponse(response3.getServiceResponse());
//									apiLogInfo.setApiResponse(
//											"Team created successfully, but default activities are not mapped");
//									apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//								} else {
//									response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//									response.setServiceResponse(response3.getServiceResponse());
//									apiLogInfo.setApiResponse("Team created successfully");
//									apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//								}
//
//							} else {
//								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//								response.setServiceResponse("Unable to create a new team.");
//								apiLogInfo.setApiResponse("Unable to create a new team.");
//								apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//							}
//
//						});
//					} else {
//						System.out.println("Team list is null or empty");
//						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//						response.setServiceResponse("Team list is null or empty");
//						apiLogInfo.setApiResponse("Team list is null or empty");
//
//					}
//
//					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//					response.setServiceResponse("Project updated successfully");
//					apiLogInfo.setApiResponse("Project updated successfully");
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			apiLogInfo.setLogLevel("ERROR");
//		}
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//	}

	public ServiceResponse projectIsPresent(Project projectObj, ResourceManagementDTO resourceManagementDTO,
			List<Long> allTeam) {
		ServiceResponse response = new ServiceResponse();
		StringBuilder logBuilder = new StringBuilder();
		LogDTO apiLogInfo = new LogDTO();

		try {

			List<Team> findTeamByProject = null;
			List<Team> addTeamList = new ArrayList<Team>();
			List<Team> allExistTeam = teamRepository.findByProjectId(resourceManagementDTO.getProjectId());
			if (resourceManagementDTO.getProjectType().equals("Internal"))
				findTeamByProject = teamRepository.findTeamByProjectId(projectObj.getProjectId());
			else
				findTeamByProject = teamRepository.findByProjectId(projectObj.getProjectId());

			addTeamList.addAll(findTeamByProject);

			List<Long> teamId = allExistTeam.stream().map(Team::getTeamId).collect(Collectors.toList());
			Set<Team> newlyAddedTeam = addTeamList.stream().filter(dto -> !teamId.contains(dto.getTeamId()))
					.collect(Collectors.toSet());

			ServiceResponse response1 = this.updateExistingProject(projectObj, resourceManagementDTO);
			System.err.print("jbsjhg" + response1.getServiceStatus());
			if (!"Success".equals(response1.getServiceStatus())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse(response1.getServiceResponse());
				apiLogInfo.setApiResponse("Project not updated successfully");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(response1.getServiceResponse());
				apiLogInfo.setApiResponse("Project  updated successfully");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}

			if (resourceManagementDTO != null && resourceManagementDTO.getTeamList() != null) {
				resourceManagementDTO.getTeamList().forEach(teamObj -> {
					System.out.println("Team Name: " + teamObj.getTeamName());

					// Check if team present
					Team teamPresent;
					if (teamObj.getTeamId() != null) {
						teamPresent = teamRepository.findByTeamIdAndProjectId(teamObj.getTeamId(),
								projectObj.getProjectId());
					} else {
						teamPresent = teamRepository.findByTeamNameAndProjectId(teamObj.getTeamName(),
								projectObj.getProjectId());
					}

					// DeptIds
					StringBuilder deptList = new StringBuilder("");
					for (String department : teamObj.getDepartmentList()) {
						deptList.append(department).append(",");
					}

					// teamLead
					Long teamLeadId = null;
					String teamLeadName = null;
					if(!teamObj.getTeamMemberList().isEmpty()) {
					for (TeamMemberDTO teamMember : teamObj.getTeamMemberList()) {
						if ((teamMember.getIsTeamLead() != null) && (teamMember.getIsTeamLead().equals("true"))) {
							teamLeadId = teamMember.getEmpId();
							teamLeadName = teamMember.getName();
						}
					}
					}
					if (teamPresent != null) {
						allTeam.add(teamPresent.getTeamId());

						// Update team
						teamPresent.setIsActive("Y");
						teamPresent.setProjectId(projectObj.getProjectId());
						teamPresent.setTeamLeadId(teamLeadId);
						teamPresent.setTeamName(teamObj.getTeamName());
						teamPresent.setTeamLeadName(teamLeadName);
						teamPresent.setDeptIds(deptList.toString());
						teamPresent.setUpdatedBy(resourceManagementDTO.getCreatedBy());
						teamPresent.setUpdatedOn(LocalDateTime.now());
						teamPresent.setSpocId(teamObj.getSpocId());

						Team teamDbResponse = teamRepository.save(teamPresent);

						if (teamDbResponse != null) {
							List<Long> defaultProjectEmpId = new ArrayList<Long>();
							List<EmployeeTeamMap> alreadyMappedMember = employeeTeamMapRepository
									.findByTeamId(teamDbResponse.getTeamId());
							List<TeamMemberDTO> newTeamMember = teamObj.getTeamMemberList();
							List<Long> memberToBeRemoved = new ArrayList<Long>();

							// Update teamMember mapping
							List<EmployeeTeamMap> updateMemberList = new ArrayList<EmployeeTeamMap>();
							
							for (EmployeeTeamMap presentMember : alreadyMappedMember) {
								for (TeamMemberDTO newMember : newTeamMember) {
									// Update teamMember mapping
									if (presentMember.getEmpId().equals(newMember.getEmpId())) {
										EmployeeTeamMap updateMember = employeeTeamMapRepository.findByEmpIdAndTeamIdAndActive(presentMember.getEmpId(),teamDbResponse.getTeamId(), 1L);

										if (updateMember != null) {
											StringBuilder employeeRole = new StringBuilder("");
											for (String empRole : newMember.getEmployeeRole()) {
												employeeRole.append(empRole).append(",");
											}

											updateMember.setEmpId(newMember.getEmpId());
											updateMember.setActive(1L);
											updateMember.setEmployeeRole(employeeRole.toString());
											updateMember.setTeamId(teamDbResponse.getTeamId());
											updateMember.setUpdatedOn(LocalDateTime.now());
											updateMember.setUpdatedBy(resourceManagementDTO.getCreatedBy());
											updateMember.setIsShadow(newMember.getIsShadow() != null? newMember.getIsShadow(): null);
											updateMember.setResourceOverviewId(newMember.getResourceOverviewId() != null
													? Long.parseLong(newMember.getResourceOverviewId().toString())
													: null);
											
											if(newMember.getIsDefaultProject() != null) {
												if(newMember.getIsDefaultProject() == 1 && newMember.getEmpId()!= null)
													defaultProjectEmpId.add(newMember.getEmpId());
											}
											
											updateMemberList.add(updateMember);
										}
									}
								}
							}
							List<EmployeeTeamMap> updateMemberDbResponse = employeeTeamMapRepository
									.saveAll(updateMemberList);
							
								if(!defaultProjectEmpId.isEmpty() && defaultProjectEmpId != null) {
									ServiceResponse defaultProjectResponse = this.handleDefaultProjectUpdate(defaultProjectEmpId, projectObj.getProjectId(), resourceManagementDTO.getCreatedBy(), logBuilder);
								
									if (ServiceResponse.STATUS_SUCCESS.equals(defaultProjectResponse.getServiceStatus())) {
									    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
									    response.setServiceResponse(defaultProjectResponse.getServiceResponse());
									} else {
									    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
									    response.setServiceResponse(defaultProjectResponse.getServiceResponse());
									}
								}

							// Add teamMember mapping
							ServiceResponse response2 = this.processNewTeamMembers(newTeamMember, teamDbResponse,
									resourceManagementDTO, rmgMail, adminMail);

							if (!(("Success").equals(response2.getServiceStatus()))) {
								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
								response.setServiceResponse(response2.getServiceResponse());
								apiLogInfo.setApiResponse("Team not updated successfully");
								apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
							} else {
								response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
								response.setServiceResponse(response2.getServiceResponse());
								apiLogInfo.setApiResponse("Team updated successfully");
								apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
							}

							// Inactivate team member
							List<EmployeeTeamMap> alreadyExistMember = new ArrayList<>();
							if (!newTeamMember.isEmpty()) {
								for (TeamMemberDTO obj : newTeamMember) {
									memberToBeRemoved.add(obj.getEmpId());
								}
								alreadyExistMember = employeeTeamMapRepository
										.findByEmpIdNotInAndTeamId(memberToBeRemoved, teamDbResponse.getTeamId());
							} else {
								alreadyExistMember = employeeTeamMapRepository.findByTeamId(teamDbResponse.getTeamId());
							}

							if (alreadyExistMember != null) {
								List<EmployeeTeamMap> inActiveMember = new ArrayList<EmployeeTeamMap>();

								alreadyExistMember.forEach((member) -> {
									Employee emp = employeeRepository.findByEmpId(member.getEmpId());
									Team findTeam = teamRepository.findByTeamId(teamDbResponse.getTeamId());
									Project findProject = projectRepository.findByProjectId(findTeam.getProjectId());

									member.setActive(0L);
									member.setEndDate(LocalDateTime.now());	
									member.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
									inActiveMember.add(member);

//						                    mail for inactive employee

									Employee findEmp = employeeRepository.findByEmpId(member.getEmpId());
									Employee managerEmail = employeeRepository.findByEmpId(findEmp.getManagerId());
									String hodMail = employeeRepository.findHodMail(member.getEmpId());

									String ccMail = hodMail + "," + managerEmail.getEmail().toString() + "," + rmgMail
											+ "," + adminMail;

									// try {
									// 	mailService.sendMailWithCC(findEmp.getEmail().toString(), ccMail,
									// 			"Regarding Resource removed from Project ",
									// 			"Dear " + emp.getName() + "<br>" + "You have been removed from project "
									// 					+ findProject.getProjectName() + "under the team - "
									// 					+ findTeam.getTeamName() + "<br>" + "<br><br>" + "Sincerely,"
									// 					+ "<br>" + "Team RMG - ApMoSys Technologies");
									// } catch (AddressException e) {
									// 	// TODO Auto-generated catch block
									// 	e.printStackTrace();
									// } catch (MessagingException e) {
									// 	// TODO Auto-generated catch block
									// 	e.printStackTrace();
									// }
								});
								List<EmployeeTeamMap> inActiveDbResponse = employeeTeamMapRepository
										.saveAll(inActiveMember);
							}

							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("Team updated successfully. Please approve it's Project to enable timesheets.");
							apiLogInfo.setApiResponse("Team Updated!");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
						}
					} else {
						// Add Team
						Team newTeamObj = new Team();
						newTeamObj.setIsActive("Y");
						newTeamObj.setProjectId(projectObj.getProjectId());
						newTeamObj.setTeamLeadId(teamLeadId);
						newTeamObj.setTeamName(teamObj.getTeamName());
						newTeamObj.setTeamLeadName(teamLeadName);
						newTeamObj.setDeptIds(deptList.toString());
						newTeamObj.setCreatedBy(resourceManagementDTO.getCreatedBy());
						newTeamObj.setCreatedOn(new Timestamp(System.currentTimeMillis()));
						newTeamObj.setSpocId(teamObj.getSpocId());
						Team teamDbResponse = teamRepository.save(newTeamObj);
						Project projectdetails= projectRepository.findByProjectId(projectObj.getProjectId());
                        projectdetails.setIsDraftProject("true");
                        Project dbResponse=projectRepository.save(projectdetails);
						if (teamDbResponse != null) {
							allTeam.add(teamDbResponse.getTeamId());

							List<EmployeeTeamMap> mapList = new ArrayList<EmployeeTeamMap>();
							// Add team member in team
							List<Long> defaultProjectEmpIds = new ArrayList<Long>();

							for (TeamMemberDTO teamMember : teamObj.getTeamMemberList()) {
								EmployeeTeamMap newEmpTeamMap = new EmployeeTeamMap();

								// TeamLead
								if ((teamMember.getIsTeamLead() != null)
										&& (teamMember.getIsTeamLead().equals("true"))) {
									newEmpTeamMap.setEmpId(teamMember.getEmpId());
									newEmpTeamMap.setActive(1l);
									newEmpTeamMap.setEmployeeRole("TeamLead");
									newEmpTeamMap.setTeamId(teamDbResponse.getTeamId());
									newEmpTeamMap.setStartDate(LocalDateTime.now()); 
									newEmpTeamMap.setIsShadow(teamMember.getIsShadow() != null ? teamMember.getIsShadow() : null);
									newEmpTeamMap.setResourceOverviewId(teamMember.getResourceOverviewId() != null
											? Long.parseLong(teamMember.getResourceOverviewId().toString())
											: null);
									newEmpTeamMap.setCreatedBy(teamMember.getCreatedBy() != null ? teamMember.getCreatedBy() : null);
									newEmpTeamMap.setCreatedOn(new Timestamp(System.currentTimeMillis()));
									
									if(teamMember.getIsDefaultProject() != null) {
										if(teamMember.getIsDefaultProject() == 1)
											defaultProjectEmpIds.add(teamMember.getEmpId());
									}
									
									mapList.add(newEmpTeamMap);
								} else {
									StringBuilder employeeRole = new StringBuilder("");
									for (String empRole : teamMember.getEmployeeRole()) {
										employeeRole.append(empRole).append(",");
									}

									newEmpTeamMap.setEmpId(teamMember.getEmpId());
									newEmpTeamMap.setActive(2l); // Set active value as 2 for newly added team members
									newEmpTeamMap.setEmployeeRole(employeeRole.toString());
									newEmpTeamMap.setTeamId(teamDbResponse.getTeamId());
									newEmpTeamMap.setStartDate(LocalDateTime.now()); 
									newEmpTeamMap.setIsShadow(teamMember.getIsShadow() != null ? teamMember.getIsShadow() : null);
									newEmpTeamMap.setResourceOverviewId(teamMember.getResourceOverviewId() != null
											? Long.parseLong(teamMember.getResourceOverviewId().toString())
											: null);
									
									if(teamMember.getIsDefaultProject() != null) {
										if(teamMember.getIsDefaultProject() == 1)
											defaultProjectEmpIds.add(teamMember.getEmpId());
									}
									newEmpTeamMap.setCreatedBy(teamMember.getCreatedBy() != null ? teamMember.getCreatedBy() : null);
									newEmpTeamMap.setCreatedOn(new Timestamp(System.currentTimeMillis()));
									
									
									mapList.add(newEmpTeamMap);
								}
							}
//						            mail for create Team

							try {
								mailService.sendMail(rmgMail, "Regarding Resource management", "Dear RMG Team ,"
										+ "<br>" + "<br>"
										+ "The team has been created and the following reources are mapped to this team -> : "
										+ teamDbResponse.getTeamName() + "<br>" + "<br><br>" + "Sincerely," + "<br>"
										+ "Team RMG - ApMoSys Technologies" + "<br>"
										+ generateHtmlTable(teamObj.getTeamMemberList()));
							} catch (Exception e) {
								e.printStackTrace();
							}

							List<EmployeeTeamMap> teamMemberDbResponse = employeeTeamMapRepository.saveAll(mapList);
							
							//Add default project mapping
							
							if(!defaultProjectEmpIds.isEmpty()) {
								ServiceResponse defaultProjectResponse = this.handleDefaultProjectUpdate(defaultProjectEmpIds, projectObj.getProjectId(), resourceManagementDTO.getCreatedBy(), logBuilder);
							
								if (ServiceResponse.STATUS_SUCCESS.equals(defaultProjectResponse.getServiceStatus())) {
								    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
								    response.setServiceResponse(defaultProjectResponse.getServiceResponse());
								} else {
								    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
								    response.setServiceResponse(defaultProjectResponse.getServiceResponse());
								}
							}
							// Add default activity
							ServiceResponse response3 = this.activityRealtedToTeam(teamMemberDbResponse, teamObj,
									resourceManagementDTO, teamDbResponse);

							if (response3.getServiceStatus() != "Success") {
								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
								response.setServiceResponse(response3.getServiceResponse());
								apiLogInfo.setApiResponse(
										"Team created successfully, but default activities are not mapped");
								apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
							} else {
								response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
								response.setServiceResponse(response3.getServiceResponse());
								apiLogInfo.setApiResponse("Team created successfully");
								apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
							}
						} else {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Unable to create new team.");
							apiLogInfo.setApiResponse("Unable to Create new team");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						}
					}

				});
			} else {
				System.out.println("Team list or DTO is null");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Team list is null or empty");
				apiLogInfo.setApiResponse("Team list is null or empty");
			}

			// InActivate Team

			List<Team> alreadyExistTeam = teamRepository.findByTeamIdNotInAndProjectId(allTeam,
					projectObj.getProjectId());
			if (!alreadyExistTeam.isEmpty()) {
				List<Team> teamToBeRemoved = new ArrayList<>();

				alreadyExistTeam.forEach((team) -> {
					team.setIsActive("N");
					team.setUpdatedBy(resourceManagementDTO.getCreatedBy());
					team.setUpdatedOn(LocalDateTime.now());
					teamToBeRemoved.add(team);

				});
				List<Team> teamToBeRemoveResponse = teamRepository.saveAll(teamToBeRemoved);
			}

		} catch (Exception e) {

			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");

		}

		return response;
	}

	private ServiceResponse processNewTeamMembers(List<TeamMemberDTO> newTeamMember, Team teamDbResponse,
			ResourceManagementDTO resourceManagementDTO, String rmgMail, String adminMail) {
		ServiceResponse response = new ServiceResponse();
		StringBuilder logBuilder = new StringBuilder();
		LogDTO apiLogInfo = new LogDTO();
		try {
			List<Long> defaultProjectEmpIds = new ArrayList<>();
			newTeamMember.forEach((newMember) -> {
				List<EmployeeTeamMap> presentMember = employeeTeamMapRepository
						.findFirstByEmpIdAndTeamIdAndActive(newMember.getEmpId(), teamDbResponse.getTeamId());

				List<EmployeeTeamMap> mapList = new ArrayList<EmployeeTeamMap>();
				EmployeeTeamMap empTeamMap = new EmployeeTeamMap();

				if (presentMember.isEmpty()) {
					if ((newMember.getIsTeamLead() != null) && (newMember.getIsTeamLead().equals("true"))) {
						empTeamMap.setEmpId(newMember.getEmpId());
						empTeamMap.setEmployeeRole("TeamLead");
						empTeamMap.setTeamId(teamDbResponse.getTeamId());
						empTeamMap.setStartDate(LocalDateTime.now());
						empTeamMap.setIsShadow(newMember.getIsShadow() != null ? newMember.getIsShadow(): null);
						empTeamMap.setResourceOverviewId(newMember.getResourceOverviewId() != null
								? Long.parseLong(newMember.getResourceOverviewId().toString())
								: null);
						
						if(newMember.getIsDefaultProject() != null) {
							if(newMember.getIsDefaultProject() == 1)
								defaultProjectEmpIds.add(newMember.getEmpId());
						}
						
						empTeamMap.setCreatedBy(newMember.getCreatedBy() != null ? newMember.getCreatedBy() : null);
						empTeamMap.setCreatedOn(new Timestamp(System.currentTimeMillis()));
						
						
						mapList.add(empTeamMap);
					} else {
						StringBuilder employeeRole = new StringBuilder("");
						for (String empRole : newMember.getEmployeeRole()) {
							employeeRole.append(empRole).append(",");
						}
						empTeamMap.setEmpId(newMember.getEmpId());
						empTeamMap.setEmployeeRole(employeeRole.toString());
						empTeamMap.setTeamId(teamDbResponse.getTeamId());
						empTeamMap.setStartDate(LocalDateTime.now());
						empTeamMap.setIsShadow(newMember.getIsShadow() != null ? newMember.getIsShadow() : null);
						empTeamMap.setResourceOverviewId(newMember.getResourceOverviewId() != null
								? Long.parseLong(newMember.getResourceOverviewId().toString())
								: null);
						
						if(newMember.getIsDefaultProject() != null) {
							if(newMember.getIsDefaultProject() == 1)
								defaultProjectEmpIds.add(newMember.getEmpId());
						}
						
						empTeamMap.setCreatedBy(newMember.getCreatedBy() != null ? newMember.getCreatedBy() : null);
						empTeamMap.setCreatedOn(new Timestamp(System.currentTimeMillis()));
						
						mapList.add(empTeamMap);
					}

					List<EmployeeTeamMap> teamMapDbResponse = employeeTeamMapRepository.saveAll(mapList);
					
					Project projectdetails= projectRepository.findByProjectId(resourceManagementDTO.getProjectId());
                    projectdetails.setIsDraftProject("true");
                    Project dbResponse=projectRepository.save(projectdetails);
                    
                    if(dbResponse != null) {
						logBuilder.append(dbResponse + " /n Project draft staus updated to true");
					}
					
					//Add default project mapping
					if(!defaultProjectEmpIds.isEmpty()) {
						ServiceResponse defaultProjectResponse = this.handleDefaultProjectUpdate(defaultProjectEmpIds, resourceManagementDTO.getProjectId(), resourceManagementDTO.getCreatedBy(), logBuilder);
						
						if (ServiceResponse.STATUS_SUCCESS.equals(defaultProjectResponse.getServiceStatus())) {
						    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						    response.setServiceResponse(defaultProjectResponse.getServiceResponse());
						} else {
						    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						    response.setServiceResponse(defaultProjectResponse.getServiceResponse());
						}
					}

					teamMapDbResponse.forEach((newAddedMember) -> {
						if (resourceManagementDTO.getIsHOD().equals("true"))
							newAddedMember.setActive(1L);
						else
							newAddedMember.setActive(2L);

						System.out.println(" newTeamMember   " + newAddedMember);

						if (newAddedMember.getEmpId() != null) {
							List<Object[]> employeeDetails = employeeRepository
									.getEmployeeByEmpId(newAddedMember.getEmpId());
							System.out.println("New member added: " + employeeDetails.get(0));

							if (employeeDetails != null && !employeeDetails.isEmpty()) {
								Object[] employeeDetailRow = employeeDetails.get(0);
								String departmentId = employeeDetailRow[46] != null ? employeeDetailRow[46].toString()
										: null;
								System.out.println("Department ID: " + departmentId);

								if (departmentId != null) {
									List<String> employeeRoles = Arrays
											.asList(newAddedMember.getEmployeeRole().split(","));
									for (String role : employeeRoles) {
										role = role.trim();
										System.out.println("Processing role: " + role);

										List<Activity> existingActivities = activitiesRepository
												.findByDeptIdsAndEmployeeRoleAndTeamId(departmentId, role,
														teamDbResponse.getTeamId());

										if (existingActivities.isEmpty()) {
											System.out.println("No activities exist for Dept ID: " + departmentId
													+ ", Role: " + role);

											List<ActivityTemplate> activityTemplateList = activityTemplateRepository
													.getByDeptIdAndEmployeeRoleType(Long.parseLong(departmentId), role);
											if (!activityTemplateList.isEmpty()) {
												for (ActivityTemplate activityTemplate : activityTemplateList) {
													Activity newActivity = new Activity();
													newActivity.setActivity(activityTemplate.getTemplateActivity());
													newActivity.setTeamId(teamDbResponse.getTeamId());
													newActivity.setEmployeeRole(activityTemplate.getEmployeeRole());
													newActivity.setDeptIds(activityTemplate.getDeptId().toString());
													newActivity.getCommonProperty()
															.setCreatedBy(resourceManagementDTO.getCreatedBy());
													activitiesRepository.save(newActivity);

													System.out.println("New activity created: "
															+ activityTemplate.getTemplateActivity());
												}
											} else {
												System.out.println("No activity templates found for Dept ID: "
														+ departmentId + ", Role: " + role);
											}
										} else {
											System.out.println("Activities already exist for Dept ID: " + departmentId
													+ ", Role: " + role);
										}
									}
								} else {
									System.out.println(
											"Department ID is null for Employee ID: " + newAddedMember.getEmpId());
								}
							} else {
								System.out.println(
										"No employee details found for Employee ID: " + newAddedMember.getEmpId());
							}
						}

						Employee findEmp = employeeRepository.findByEmpId(newAddedMember.getEmpId());
						Employee managerEmail = employeeRepository.findByEmpId(findEmp.getManagerId());
						String hodMail = employeeRepository.findHodMail(newAddedMember.getEmpId());

						String ccMail = hodMail + "," + managerEmail.getEmail().toString() + "," + rmgMail + ","
								+ adminMail;

						Project projectFind = null;
						if (resourceManagementDTO.getProjectType().equals("Internal")) {
							projectFind = projectRepository.findByProjectId(resourceManagementDTO.getProjectId());
						} else {
							projectFind = projectRepository.findByPoProjectId(resourceManagementDTO.getId());
						}

						try {
							mailService.sendMailWithCC(findEmp.getEmail().toString(), ccMail,
									"Regarding resource mapping to new project",
									"Dear " + findEmp.getName() + "<br>" + "You have been mapped to client name - "
											+ resourceManagementDTO.getClientName() + " under the project "
											+ projectFind.getProjectName() + "<br><br><br>"
											+ "Sincerely,<br>Team RMG - ApMoSys Technologies");
						} catch (AddressException e) {
							e.printStackTrace();
						} catch (MessagingException e) {
							e.printStackTrace();
						}
					});

					employeeTeamMapRepository.saveAll(teamMapDbResponse);
				}

			});

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		return response;

	}

	private ServiceResponse updateExistingProject(Project project, ResourceManagementDTO dto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();

		try {
			// Find project manager
//		    Long projManagerId = getProjectManagerId(dto.getProjectManager());

			// Update project properties
//		    if(projManagerId != null) {
//			project.setIsDraftProject("false");
			project.setProjectName(dto.getName());
			project.setPoNo(dto.getPoNo());
			project.setPoStartDate(dto.getPoStartDate());
			project.setPoEndDate(dto.getPoEndDate());
			project.setPoProjectType("Internal".equalsIgnoreCase(dto.getProjectType()) ? null : dto.getProjectType());

			project.setApmosysRM(dto.getApmosysRM());
			project.setIsRenewable(dto.getIsRenewable());
			project.setClientRM(dto.getClientRM());
			project.setUpdatedBy(dto.getCreatedBy());
			project.setUpdatedOn(LocalDateTime.now());
			Project dbResponse = projectRepository.save(project);

			ServiceResponse responseProjectManager = this.setProjectManager(dto, dbResponse);

			if (!responseProjectManager.getServiceStatus().equals("Success")) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(responseProjectManager.getServiceResponse());
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse(responseProjectManager.getServiceResponse());
			}
			
			ServiceResponse responseProjectOverhead = this.setProjectOverheads(dto,dbResponse);
			
			if (!responseProjectOverhead.getServiceStatus().equals("Success")) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(responseProjectOverhead.getServiceResponse());
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse(responseProjectOverhead.getServiceResponse());
			}

			if (!dto.getResourceRequirements().isEmpty()) {
				dto.getResourceRequirements().forEach(req -> {
					
					Long overviewId = req.getResourceOverviewId() != null
			                ? Long.parseLong(req.getResourceOverviewId().toString())
			                : null;

			        if (overviewId != null && resourceRequirementRepository.existsById(overviewId)) {
			            return; 
			        }

					ResourceRequirement resourceManagementDTO = new ResourceRequirement();

					resourceManagementDTO.setCount(req.getCount());
					resourceManagementDTO.setDepartment(req.getDepartment());
					resourceManagementDTO.setExperience(req.getExperience());
					resourceManagementDTO.setRole(req.getRole());
					resourceManagementDTO.setResourceOverviewId(
							req.getResourceOverviewId() != null ? Long.parseLong(req.getResourceOverviewId().toString())
									: null);
					resourceManagementDTO.setProjectId(project.getProjectId());

					ResourceRequirement res = resourceRequirementRepository.save(resourceManagementDTO);
				});
			}

			if (dbResponse != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Project updated successfully");
				apiLogInfo.setApiResponse("Project updated successfully");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not updated successfully");
				apiLogInfo.setApiResponse("Project not updated successfully");
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
		return response;
	}

	private Long getProjectManagerId(String projectManagerString) {
		if (projectManagerString == null || !projectManagerString.contains("-")) {
			return null;
		}

		try {
			Long employmentId = Long.parseLong(projectManagerString.split("-")[1]);
			Employee employee = employeeRepository.findByEmployeementId(employmentId);
			return employee != null ? employee.getEmpId() : null;
		} catch (Exception e) {
//	        logger.error("Invalid project manager ID format", e);
			e.printStackTrace();
			return null;
		}
	}

	public ServiceResponse activityRealtedToTeam(List<EmployeeTeamMap> teamMemberDbResponse, TeamDTO teamObj,
			ResourceManagementDTO resourceManagementDTO, Team teamDbResponse) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();

		Activity newActivityCreated = null;
		if (!teamMemberDbResponse.isEmpty()) {
			for (String department : teamObj.getDepartmentList()) {
				List<ActivityTemplate> activityTemplate = activityTemplateRepository
						.getByDeptId(Long.parseLong(department));
				if (!activityTemplate.isEmpty()) {

					for (ActivityTemplate activityObject : activityTemplate) {
						Activity newActivity = new Activity();

						newActivity.setActivity(activityObject.getTemplateActivity());
						newActivity.setTeamId(teamDbResponse.getTeamId());
						newActivity.setEmployeeRole(activityObject.getEmployeeRole());
						newActivity.setDeptIds(activityObject.getDeptId().toString());
						newActivity.getCommonProperty().setCreatedBy(resourceManagementDTO.getCreatedBy());

						newActivityCreated = activitiesRepository.save(newActivity);
					}
				}
			}
		}
		if (newActivityCreated != null) {

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Team created successfully.");
			apiLogInfo.setApiResponse("Team Created! ");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

		} else {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Team created successfully, but default activities are not mapped");
			apiLogInfo.setApiResponse("Team created successfully, but default activities are not mapped");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
		}

		return response;
	}
//	public ServiceResponse createDraftProjectInfo(ResourceManagementDTO resourceManagementDTO) {
//		ServiceResponse response = new ServiceResponse();
//        LogDTO apiLogInfo = new LogDTO();
//        apiLogInfo.setSubFeatureName("createDraftProjectInfo");
//        apiLogInfo.setApiUrl("/api/createDraftProjectInfo");
//        apiLogInfo.setLogLevel("INFO");
//        StringBuilder logBuilder = new StringBuilder();
//        logBuilder.append("ProjectType : " + resourceManagementDTO.getProjectType() + " ,ProjectId :" + resourceManagementDTO.getProjectId()
//        + " ,ProjectName :" + resourceManagementDTO.getName() + " ,Department :" + resourceManagementDTO.getDeptName() + " ,State:" + 
//        resourceManagementDTO.getClientState());
//
//		try {
//			Project projObj = null;
//		    if (resourceManagementDTO.getProjectType().equals("Internal")) {
//		        projObj = projectRepository.findByProjectId(resourceManagementDTO.getProjectId());
//		    } else {
//		        projObj = projectRepository.findByPoProjectId(resourceManagementDTO.getId());
//		    }
//
//		    Project projectObj = projObj;
//		    Employee employeeObj = employeeRepository.findByEmpId(resourceManagementDTO.getCreatedBy());
//
//		    List<Long> allTeam = new ArrayList<>();
//
//		    if (projectObj != null) {
//
//		        List<Team> findTeamByProject = null;
//		        List<Team> addTeamList = new ArrayList<Team>();
//		        List<Team> allExistTeam = teamRepository.findByProjectId(resourceManagementDTO.getProjectId());
//		        if (resourceManagementDTO.getProjectType().equals("Internal"))
//		            findTeamByProject = teamRepository.findTeamByProjectId(projectObj.getProjectId());
//		        else
//		            findTeamByProject = teamRepository.findByProjectId(projectObj.getProjectId());
//
//		        addTeamList.addAll(findTeamByProject);
//
//		        List<Long> teamId = allExistTeam.stream().map(Team::getTeamId).collect(Collectors.toList());
//		        Set<Team> newlyAddedTeam = addTeamList.stream().filter(dto -> !teamId.contains(dto.getTeamId())).collect(Collectors.toSet());
//
//		        // Logic to handle existing teams and newly added teams...
//
//		        Long employeementID = Long.parseLong(resourceManagementDTO.getProjectManager().split("-")[1]);
//		        Long projManagerId = null;
//		        Employee employee = employeeRepository.findByEmployeementId(employeementID);
//		        if (employee != null) {
//		            projManagerId = employee.getEmpId();
//		        }
//		        projectObj.setIsDraftProject("false");
//
//		        projectObj.setProjectName(resourceManagementDTO.getName());
//		        projectObj.setProjectManagerId(projManagerId);
//		        Project projectDbResponse = projectRepository.save(projectObj);
//				
//				resourceManagementDTO.getTeamList().forEach((teamObj) -> {
//					//Check if team present
//					 Team teamPresent;
//					    if (teamObj.getTeamId() != null) {
//					        teamPresent = teamRepository.findByTeamIdAndProjectId(teamObj.getTeamId(), projectObj.getProjectId());
//					    } else {
//					        teamPresent = teamRepository.findByTeamNameAndProjectId(teamObj.getTeamName(), projectObj.getProjectId());
//					    }
//
//					    // DeptIds
//					    StringBuilder deptList = new StringBuilder("");
//					    for (String department : teamObj.getDepartmentList()) {
//					        deptList.append(department).append(",");
//					    }
//
//					    // teamLead
//					    Long teamLeadId = null;
//					    String teamLeadName = null;
//					    for (TeamMemberDTO teamMember : teamObj.getTeamMemberList()) {
//					        if ((teamMember.getIsTeamLead() != null) && (teamMember.getIsTeamLead().equals("true"))) {
//					            teamLeadId = teamMember.getEmpId();
//					            teamLeadName = teamMember.getName();
//					        }
//					    }
//
//					    if (teamPresent != null) {
//					        allTeam.add(teamPresent.getTeamId());
//
//					        // Update team
//					        teamPresent.setIsActive("Y");
//					        teamPresent.setProjectId(projectObj.getProjectId());
//					        teamPresent.setTeamLeadId(teamLeadId);
//					        teamPresent.setTeamName(teamObj.getTeamName());
//					        teamPresent.setTeamLeadName(teamLeadName);
//					        teamPresent.setDeptIds(deptList.toString());
//					        teamPresent.getCommonProperty().setUpdatedBy(resourceManagementDTO.getCreatedBy());
//
//					        Team teamDbResponse = teamRepository.save(teamPresent);
//
//					        if (teamDbResponse != null) {
//					            List<EmployeeTeamMap> alreadyMappedMember = employeeTeamMapRepository.findByTeamId(teamDbResponse.getTeamId());
//					            List<TeamMemberDTO> newTeamMember = teamObj.getTeamMemberList();
//					            List<Long> memberToBeRemoved = new ArrayList<Long>();
//
//					            // Update teamMember mapping
//					            List<EmployeeTeamMap> updateMemberList = new ArrayList<EmployeeTeamMap>();
//					            for (EmployeeTeamMap presentMember : alreadyMappedMember) {
//					                for (TeamMemberDTO newMember : newTeamMember) {
//					                    // Update teamMember mapping
//					                    if (presentMember.getEmpId().equals(newMember.getEmpId())) {
//					                        EmployeeTeamMap updateMember = employeeTeamMapRepository
//					                                .findByEmpIdAndTeamIdAndActive(presentMember.getEmpId(), teamDbResponse.getTeamId(), 1L);
//
//					                        if (updateMember != null) {
//					                            StringBuilder employeeRole = new StringBuilder("");
//					                            for (String empRole : newMember.getEmployeeRole()) {
//					                                employeeRole.append(empRole).append(",");
//					                            }
//
//					                            updateMember.setEmpId(newMember.getEmpId());
//					                            updateMember.setActive(1L);
//					                            updateMember.setEmployeeRole(employeeRole.toString());
//					                            updateMember.setTeamId(teamDbResponse.getTeamId());
//
//					                            updateMemberList.add(updateMember);
//					                        }
//					                    }
//					                }
//					            }
//					            List<EmployeeTeamMap> updateMemberDbResponse = employeeTeamMapRepository.saveAll(updateMemberList);
//
//					            // Add teamMember mapping
//					            newTeamMember.forEach((newMember) -> {
//					                List<EmployeeTeamMap> presentMember = employeeTeamMapRepository
//					                        .findFirstByEmpIdAndTeamIdAndActive(newMember.getEmpId(), teamDbResponse.getTeamId());
//
//					                List<EmployeeTeamMap> mapList = new ArrayList<EmployeeTeamMap>();
//					                EmployeeTeamMap empTeamMap = new EmployeeTeamMap();
//
//					                if (presentMember.isEmpty()) {
//					                    // TeamLead
//					                    if ((newMember.getIsTeamLead() != null) && (newMember.getIsTeamLead().equals("true"))) {
//					                        empTeamMap.setEmpId(newMember.getEmpId());
////					                        empTeamMap.setActive(1L);
//					                        empTeamMap.setEmployeeRole("TeamLead");
//					                        empTeamMap.setTeamId(teamDbResponse.getTeamId());
//					                        mapList.add(empTeamMap);
//					                    } else {
//					                        StringBuilder employeeRole = new StringBuilder("");
//					                        for (String empRole : newMember.getEmployeeRole()) {
//					                            employeeRole.append(empRole).append(",");
//					                        }
//
//					                        empTeamMap.setEmpId(newMember.getEmpId());
////					                        empTeamMap.setActive(1L);
//					                        empTeamMap.setEmployeeRole(employeeRole.toString());
//					                        empTeamMap.setTeamId(teamDbResponse.getTeamId());
//					                        mapList.add(empTeamMap);
//					                    }
//					                    List<EmployeeTeamMap> teamMapDbResponse = employeeTeamMapRepository.saveAll(mapList);
//
//					                    // Set active value as 2 for new added team members
//					                    teamMapDbResponse.forEach((newAddedMember) -> {
//					                    	if(resourceManagementDTO.getIsHOD().equals("true"))
//					                        newAddedMember.setActive(1L);
//					                    	else
//					                    		newAddedMember.setActive(2L);
//					                    	
//					                    	  System.out.println(" newTeamMember   "+newAddedMember);
////					                    	// Add default activity for the new team member
////						                    	// Add default activity for the new team member
//						                    	  if (newAddedMember.getEmpId() != null) {
//						                    	      // Fetch the employee's job role and department ID
//						                    	      List<Object[]> employeeDetails = employeeRepository.getEmployeeByEmpId(newAddedMember.getEmpId());
//						                    	      System.out.println("New member added: " + employeeDetails.get(0));
//
//						                    	      if (employeeDetails != null && !employeeDetails.isEmpty()) {
//						                    	          // Assuming the first row contains the desired details
//						                    	          Object[] employeeDetailRow = employeeDetails.get(0);
//						                    	          String departmentId = employeeDetailRow[46] != null ? employeeDetailRow[46].toString() : null;
//						                    	          System.out.println("Department ID: " + departmentId);
//
//						                    	          if (departmentId != null) {
//						                    	              // Split employeeRole into a list of strings
//						                    	              List<String> employeeRoles = Arrays.asList(newAddedMember.getEmployeeRole().split(","));
//
//						                    	              for (String role : employeeRoles) {
//						                    	                  role = role.trim(); // Trim whitespace around each role
//						                    	                  System.out.println("Processing role: " + role);
//
//						                    	                  // Check if activities exist for the employee's department and role
//						                    	                  List<Activity> existingActivities = activitiesRepository.findByDeptIdsAndEmployeeRoleAndTeamId(
//						                    	                      departmentId,
//						                    	                      role,
//						                    	                      teamDbResponse.getTeamId()
//						                    	                  );
//
//						                    	                  if (existingActivities.isEmpty()) {
//						                    	                      System.out.println("No activities exist for Dept ID: " + departmentId + ", Role: " + role);
//
//						                    	                      // Fetch the activity templates for the given department and role
//						                    	                      List<ActivityTemplate> activityTemplateList = activityTemplateRepository.getByDeptIdAndEmployeeRoleType(
//						                    	                          Long.parseLong(departmentId),
//						                    	                          role
//						                    	                      );
//						                    	                      if (!activityTemplateList.isEmpty()) {
//						                    	                          for (ActivityTemplate activityTemplate : activityTemplateList) {
//						                    	                              // Create and save new activities
//						                    	                              Activity newActivity = new Activity();
//						                    	                              newActivity.setActivity(activityTemplate.getTemplateActivity());
//						                    	                              newActivity.setTeamId(teamDbResponse.getTeamId());
//						                    	                              newActivity.setEmployeeRole(activityTemplate.getEmployeeRole());
//						                    	                              newActivity.setDeptIds(activityTemplate.getDeptId().toString());
//						                    	                              newActivity.getCommonProperty().setCreatedBy(resourceManagementDTO.getCreatedBy());
//						                    	                              activitiesRepository.save(newActivity);
//
//						                    	                              System.out.println("New activity created: " + activityTemplate.getTemplateActivity());
//						                    	                          }
//						                    	                      } else {
//						                    	                          System.out.println("No activity templates found for Dept ID: " + departmentId + ", Role: " + role);
//						                    	                      }
//						                    	                  } else {
//						                    	                      System.out.println("Activities already exist for Dept ID: " + departmentId + ", Role: " + role);
//						                    	                  }
//						                    	              }
//						                    	          } else {
//						                    	              System.out.println("Department ID is null for Employee ID: " + newAddedMember.getEmpId());
//						                    	          }
//						                    	      } else {
//						                    	          System.out.println("No employee details found for Employee ID: " + newAddedMember.getEmpId());
//						                    	      }
//						                    	  }
//
//
////					                    	  find added employee's email
//					                    	  Employee  findEmp = employeeRepository.findByEmpId(newAddedMember.getEmpId());
//					                    	  Employee managerEmail = employeeRepository.findByEmpId(findEmp.getManagerId());
//					                    	  
//					                    	  Project projectFind = null;
//					              		    if (resourceManagementDTO.getProjectType().equals("Internal")) {
//					              		    	projectFind = projectRepository.findByProjectId(resourceManagementDTO.getProjectId());
//					              		    } else {
//					              		    	projectFind = projectRepository.findByPoProjectId(resourceManagementDTO.getId());
//					              		    }
//					                    	  
//					                    	  try {
//												mailService.sendMailWithCC("tmp@gmail.com", rmgMail, "Regarding Resource mapped to new Project", "Dear "
//														+ findEmp.getName()+"<br>"
//														+ "You have been mapped to client name - "+resourceManagementDTO.getClientName()+" under the project "+projectFind.getProjectName()+"<br>"
//																+ "<br><br>"
//																+ "Sincerely,"+"<br>"
//																+ "Team RMG - ApMoSys Technologies"
//														);
//											} catch (AddressException e) {
//												// TODO Auto-generated catch block
//												e.printStackTrace();
//											} catch (MessagingException e) {
//												// TODO Auto-generated catch block
//												e.printStackTrace();
//											}
//					                    	  
//					                    });
//					                    employeeTeamMapRepository.saveAll(teamMapDbResponse);
//					                }
//					            });
//					            
//					            // Inactivate team member
//					            List<EmployeeTeamMap> alreadyExistMember = new ArrayList<>();
//					            if (!newTeamMember.isEmpty()) {
//					                for (TeamMemberDTO obj : newTeamMember) {
//					                    memberToBeRemoved.add(obj.getEmpId());
//					                }
//					                alreadyExistMember = employeeTeamMapRepository.findByEmpIdNotInAndTeamId(memberToBeRemoved, teamDbResponse.getTeamId());
//					            } else {
//					                alreadyExistMember = employeeTeamMapRepository.findByTeamId(teamDbResponse.getTeamId());
//					            }
//
//					            if (alreadyExistMember != null) {
//					                List<EmployeeTeamMap> inActiveMember = new ArrayList<EmployeeTeamMap>();
//
//					                alreadyExistMember.forEach((member) -> {
//					                	Employee emp = employeeRepository.findByEmpId(member.getEmpId());
//					                	Team findTeam = teamRepository.findByTeamId(teamDbResponse.getTeamId());
//					                	Project findProject = projectRepository.findByProjectId(findTeam.getProjectId());
//					                	
//					                    member.setActive(0L);
//					                    member.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
//					                    inActiveMember.add(member);
//					                    
////					                    mail for inactive employee
//					                    
//					                    try {
//											mailService.sendMail(rmgMail,"Regarding Resource removed from Project ", "Dear "
//													+ emp.getName()+"<br>"
//													+ "You have been removed from project "+findProject.getProjectName()+ "under the team - "+findTeam.getTeamName()+"<br>"
//															+ "<br><br>"
//															+ "Sincerely,"+"<br>"
//															+ "Team RMG - ApMoSys Technologies"
//													);
//										} catch (AddressException e) {
//											// TODO Auto-generated catch block
//											e.printStackTrace();
//										} catch (MessagingException e) {
//											// TODO Auto-generated catch block
//											e.printStackTrace();
//										}
//					                });
//					                List<EmployeeTeamMap> inActiveDbResponse = employeeTeamMapRepository.saveAll(inActiveMember);
//					            }
//
//					            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//					            response.setServiceResponse("Team updated successfully.");
//					            apiLogInfo.setApiResponse("Team Updated!");
//					            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//					        }
//					    }else {
//					    	// Add Team
//					        Team newTeamObj = new Team();
//					        newTeamObj.setIsActive("Y");
//					        newTeamObj.setProjectId(projectObj.getProjectId());
//					        newTeamObj.setTeamLeadId(teamLeadId);
//					        newTeamObj.setTeamName(teamObj.getTeamName());
//					        newTeamObj.setTeamLeadName(teamLeadName);
//					        newTeamObj.setDeptIds(deptList.toString());
//					        newTeamObj.getCommonProperty().setCreatedBy(resourceManagementDTO.getCreatedBy());
//					        Team teamDbResponse = teamRepository.save(newTeamObj);
//
//					        if (teamDbResponse != null) {
//					            allTeam.add(teamDbResponse.getTeamId());
//
//					            List<EmployeeTeamMap> mapList = new ArrayList<EmployeeTeamMap>();
//					            // Add team member in team
//
//					            for (TeamMemberDTO teamMember : teamObj.getTeamMemberList()) {
//					                EmployeeTeamMap newEmpTeamMap = new EmployeeTeamMap();
//
//					                // TeamLead
//					                if ((teamMember.getIsTeamLead() != null) && (teamMember.getIsTeamLead().equals("true"))) {
//					                    newEmpTeamMap.setEmpId(teamMember.getEmpId());
//					                    newEmpTeamMap.setActive(1l);
//					                    newEmpTeamMap.setEmployeeRole("TeamLead");
//					                    newEmpTeamMap.setTeamId(teamDbResponse.getTeamId());
//					                    mapList.add(newEmpTeamMap);
//					                } else {
//					                    StringBuilder employeeRole = new StringBuilder("");
//					                    for (String empRole : teamMember.getEmployeeRole()) {
//					                        employeeRole.append(empRole).append(",");
//					                    }
//
//					                    newEmpTeamMap.setEmpId(teamMember.getEmpId());
//					                    newEmpTeamMap.setActive(2l); // Set active value as 2 for newly added team members
//					                    newEmpTeamMap.setEmployeeRole(employeeRole.toString());
//					                    newEmpTeamMap.setTeamId(teamDbResponse.getTeamId());
//					                    mapList.add(newEmpTeamMap);
//					                }
//					            }
////					            mail for create Team
//					            
//					            try {
//						            mailService.sendMail(rmgMail,
//						                    "Regarding Resource management",
//						                    "Dear RMG Team ," + "<br>"
//						                            + "<br>"
//						                            + "The team has been created and the following reources are mapped to this team -> : " + teamDbResponse.getTeamName()+"<br>"
//						                            		+ "<br><br>"
//															+ "Sincerely,"+"<br>"
//															+ "Team RMG - ApMoSys Technologies"
//															+ "<br>"
//						                            +generateHtmlTable(teamObj.getTeamMemberList()));                             
//						        } catch (Exception e) {
//						            e.printStackTrace();
//						        }
//					            
//					            
//					            List<EmployeeTeamMap> teamMemberDbResponse = employeeTeamMapRepository.saveAll(mapList);
//
//					            // Add default activity
//					            Activity newActivityCreated = null;
//					            if (!teamMemberDbResponse.isEmpty()) {
//					                for (String department : teamObj.getDepartmentList()) {
//					                    List<ActivityTemplate> activityTemplate = activityTemplateRepository.getByDeptId(Long.parseLong(department));
//					                    if (!activityTemplate.isEmpty()) {
//
//					                        for (ActivityTemplate activityObject : activityTemplate) {
//					                            Activity newActivity = new Activity();
//
//					                            newActivity.setActivity(activityObject.getTemplateActivity());
//					                            newActivity.setTeamId(teamDbResponse.getTeamId());
//					                            newActivity.setEmployeeRole(activityObject.getEmployeeRole());
//					                            newActivity.setDeptIds(activityObject.getDeptId().toString());
//					                            newActivity.getCommonProperty().setCreatedBy(resourceManagementDTO.getCreatedBy());
//
//					                            newActivityCreated = activitiesRepository.save(newActivity);
//					                        }
//					                    }
//					                }
//					            }
//					            if (newActivityCreated != null) {
//
//					                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//					                response.setServiceResponse("Team created successfully.");
//					                apiLogInfo.setApiResponse("Team Created! ");
//					                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//
//					            } else {
//					                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//					                response.setServiceResponse("Team created successfully, but default activities are not mapped");
//					                apiLogInfo.setApiResponse("Team created successfully, but default activities are not mapped");
//					                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//					            }
//					        } else {
//					            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//					            response.setServiceResponse("Unable to create new team.");
//					            apiLogInfo.setApiResponse("Unable to Create new team");
//					            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//					        }
//					    }
//				});
//				
//				//InActivate Team
//				
//				List<Team> alreadyExistTeam = teamRepository.findByTeamIdNotInAndProjectId(allTeam, projectObj.getProjectId());
//			    if (!alreadyExistTeam.isEmpty()) {
//			        List<Team> teamToBeRemoved = new ArrayList<>();
//
//			        alreadyExistTeam.forEach((team) -> {
//			            team.setIsActive("N");
//			            team.getCommonProperty().setUpdatedBy(resourceManagementDTO.getCreatedBy());
//			            
//			            teamToBeRemoved.add(team);			            
//			            
//			        });
//			        List<Team> teamToBeRemoveResponse = teamRepository.saveAll(teamToBeRemoved);
//			    }
//				
//				//Send mail to RMG: if HOD has updated project/Team
////			    if (resourceManagementDTO.getIsHOD().equals("true") && !resourceManagementDTO.getProjectType().equals("Internal")) {
////			        try {
////			            mailService.sendMailWithCC("demo@gmail.com", "sakti.das@apmosys.com",
////			                    "Regarding Resource management",
////			                    "Dear RMG Team ," + "<br>"
////			                            + "<br>"
////			                            + employeeObj.getName() + " project update hua hua  has updated the project : " + resourceManagementDTO.getName());
////			        } catch (Exception e) {
////			            e.printStackTrace();
////			        }
////			    }
//				
//				// Send Project/Team detail JSON to PoPotal
//				
////			    if (!resourceManagementDTO.getProjectType().equals("Internal")) {
////			    	resourceManagementDTO.setPoProjectId(resourceManagementDTO.getId());
////			        ServiceResponse poPortalResponse = sendProjectInfoToPoPortal(resourceManagementDTO);
////
////			        if (poPortalResponse.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS)) {
////			            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
////			            response.setServiceResponse("Project updated successfully");
////			            apiLogInfo.setApiResponse("Project updated successfully");
////			            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
////			        } else {
////			            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
////			            response.setServiceResponse("Project & Team created successfully, but unable to sync with PoPortal : " + poPortalResponse.getServiceResponse());
////			            apiLogInfo.setApiResponse("Project & Team created successfully, but unable to sync with PoPortal");
////			            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
////			        }
////			    } else {
//			    	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//			        response.setServiceResponse("Project updated successfully");
//			        apiLogInfo.setApiResponse("Project updated successfully");
//			        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
////			    }
//			} else {
//				Integer clientId = null;
//			    Optional<Client> clientObj = clientsRepository.findByClientName(resourceManagementDTO.getClientName());
//			    if (!clientObj.isEmpty()) {
//			        Client clientPresent = clientObj.get();
//			        clientId = clientPresent.getClientId();
//			    } else {
//			        // Add Client & Client Location
//			        Client newClient = new Client();
//			        newClient.setClientName(resourceManagementDTO.getClientName());
//			        Client clientDbResponse = clientsRepository.save(newClient);
//
//			        if (clientDbResponse != null) {
//			            clientId = clientDbResponse.getClientId();
//			            List<ClientLocation> locations = new ArrayList<>();
//
//			            for (String clientLocation : resourceManagementDTO.getClientLocation()) {
//			                ClientLocation newClientLocation = new ClientLocation();
//			                newClientLocation.setClientId(clientDbResponse.getClientId());
//			                newClientLocation.setClientLocation(clientLocation);
//			                locations.add(newClientLocation);
//			            }
//
//			            // Add WFH location
//			            boolean contains = Arrays.stream(resourceManagementDTO.getClientLocation()).anyMatch("WFH"::equals);
//			            if (!contains) {
//			                ClientLocation newClientLocation = new ClientLocation();
//			                newClientLocation.setClientId(clientDbResponse.getClientId());
//			                newClientLocation.setClientLocation("WFH");
//			                locations.add(newClientLocation);
//			            }
//
//			            List<ClientLocation> clientLocationDbResponse = clientLocationRepository.saveAll(locations);
//
//			            if (!clientLocationDbResponse.isEmpty()) {
//			                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//			                response.setServiceResponse("Client Location added");
//			                apiLogInfo.setApiResponse("Client Location added");
//			                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//			            } else {
//			                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//			                response.setServiceResponse("Failed to add client Location");
//			                apiLogInfo.setApiResponse("Failed to add client Location");
//			                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//			                return response;
//			            }
//			        } else {
//			            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//			            response.setServiceResponse("Failed to add client");
//			            apiLogInfo.setApiResponse("Failed to add client");
//			            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//			            return response;
//			        }
//			    }
//
//				
//				//Find ProjectManager empId
//			    Long employeementID = Long.parseLong(resourceManagementDTO.getProjectManager().split("-")[1]);
//			    Long projManagerId = null;
//			    Employee employee = employeeRepository.findByEmployeementId(employeementID);
//			    if (employee != null) {
//			        projManagerId = employee.getEmpId();
//			    }
//				
//				//Add project
//			    Project newProject = new Project();
//			    newProject.setProjectManagerId(projManagerId);
//			    newProject.setProjectName(resourceManagementDTO.getName());
//			    newProject.setState(resourceManagementDTO.getClientState());
//			    newProject.setClientId(clientId);
//			    newProject.setPoProjectId(resourceManagementDTO.getId());
//			    newProject.setActive("true");
//			    newProject.setSyncProject("true");
//				
//				  	if (resourceManagementDTO.getIsHOD().equals("true")) {
//				 	newProject.setIsDraftProject("false"); 
//				 	} else {
//				 	newProject.setIsDraftProject("true"); 
//				 	}
//				 
//			    System.out.println(" ANurag     ::   "+projectObj);
//
//			    newProject.setCreatedBy(resourceManagementDTO.getCreatedBy());
//
//			    Project projectDbResponse = projectRepository.save(newProject);
//				
//			    if (projectDbResponse != null) {
//			        // Add project Department Mapping
//			        for (String department : resourceManagementDTO.getDepartment()) {
//			            Department departmentObj = departmentRepository.findByName(department);
//			            if (departmentObj != null) {
//			                ProjectDepartmentMap projectDeptMapObj = projectDepartmentMapRepository.
//			                        findByProjectIdAndDeptId(projectDbResponse.getProjectId(), departmentObj.getDeptId());
//			                if (projectDeptMapObj == null) {
//			                    // Add department
//			                    ProjectDepartmentMap projectDeptMap = new ProjectDepartmentMap();
//			                    projectDeptMap.setProjectId(projectDbResponse.getProjectId());
//			                    projectDeptMap.setDeptId(departmentObj.getDeptId());
//			                    ProjectDepartmentMap projDeptMapDbResponse = projectDepartmentMapRepository.save(projectDeptMap);
//			                }
//			            }
//			        }
//
//					// Add Team
//
//			        resourceManagementDTO.getTeamList().forEach((teamObj) -> {
//			            // Create a new team
//			            StringBuilder deptList = new StringBuilder("");
//			            for (String department : teamObj.getDepartmentList()) {
//			                deptList.append(department).append(",");
//			            }
//			            // TeamLead
//			            Long teamLeadId = null;
//			            String teamLeadName = null;
//			            for (TeamMemberDTO teamMember : teamObj.getTeamMemberList()) {
//			                if ((teamMember.getIsTeamLead() != null) && (teamMember.getIsTeamLead().equals("true"))) {
//			                    teamLeadId = teamMember.getEmpId();
//			                    teamLeadName = teamMember.getName();
//			                }
//			            }
//
//						
//			            Team newTeamObj = new Team();
//			            newTeamObj.setIsActive("Y");
//			            newTeamObj.setProjectId(projectDbResponse.getProjectId());
//			            newTeamObj.setTeamLeadId(teamLeadId);
//			            newTeamObj.setTeamName(teamObj.getTeamName());
//			            newTeamObj.setTeamLeadName(teamLeadName);
//						newTeamObj.setDeptIds(deptList.toString());
//						newTeamObj.setDeptIds(deptList.toString());
//			            newTeamObj.getCommonProperty().setCreatedBy(resourceManagementDTO.getCreatedBy());
//			            Team teamDbResponse = teamRepository.save(newTeamObj);
//
//			            if (teamDbResponse != null) {
//			                List<EmployeeTeamMap> mapList = new ArrayList<EmployeeTeamMap>();
//
//			                // Add team member in the team
//			                for (TeamMemberDTO teamMember : teamObj.getTeamMemberList()) {
//			                    EmployeeTeamMap newEmpTeamMap = new EmployeeTeamMap();
//
//			                    // TeamLead
//			                    if ((teamMember.getIsTeamLead() != null) && (teamMember.getIsTeamLead().equals("true"))) {
//			                        newEmpTeamMap.setEmpId(teamMember.getEmpId());
//			                        newEmpTeamMap.setActive(2L); // Set Active to 2 for TeamLead
//			                        newEmpTeamMap.setEmployeeRole("TeamLead");
//			                        newEmpTeamMap.setTeamId(teamDbResponse.getTeamId());
//			                        mapList.add(newEmpTeamMap);
//			                    } else {
//			                        StringBuilder employeeRole = new StringBuilder("");
//			                        for (String empRole : teamMember.getEmployeeRole()) {
//			                            employeeRole.append(empRole).append(",");
//			                        }
//
//			                        newEmpTeamMap.setEmpId(teamMember.getEmpId());
//			                        newEmpTeamMap.setActive(2L); // Set Active to 2 for Team Member
//			                        newEmpTeamMap.setEmployeeRole(employeeRole.toString());
//			                        newEmpTeamMap.setTeamId(teamDbResponse.getTeamId());
//			                        mapList.add(newEmpTeamMap);
//			                    }
//			                }
//			                List<EmployeeTeamMap> teamMemberDbResponse = employeeTeamMapRepository.saveAll(mapList);
//			                
////			                after create team
//			                
//			                try {
//								mailService.sendMail(rmgMail,"Regarding Team Create", "Dear "
//										+ "RMG ,"+"<br>"
//										+ "The Team has been created with the team name - "+teamDbResponse.getTeamName()+"<br>"
//												+ "<br><br>"
//												+ "Sincerely,"+"<br>"
//												+ "Team RMG - ApMoSys Technologies"
//										);
//							} catch (AddressException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							} catch (MessagingException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//
//			                // Add default activity
//			                Activity newActivityCreated = null;
//			                if (!teamMemberDbResponse.isEmpty()) {
//			                    for (String department : teamObj.getDepartmentList()) {
//			                        List<ActivityTemplate> activityTemplate = activityTemplateRepository.getByDeptId(Long.parseLong(department));
//			                        if (!activityTemplate.isEmpty()) {
//			                            for (ActivityTemplate activityObject : activityTemplate) {
//			                                Activity newActivity = new Activity();
//
//			                                newActivity.setActivity(activityObject.getTemplateActivity());
//			                                newActivity.setTeamId(teamDbResponse.getTeamId());
//			                                newActivity.setEmployeeRole(activityObject.getEmployeeRole());
//			                                newActivity.setDeptIds(activityObject.getDeptId().toString());
//			                                newActivity.getCommonProperty().setCreatedBy(resourceManagementDTO.getCreatedBy());
//
//			                                newActivityCreated = activitiesRepository.save(newActivity);
//			                            }
//			                        }
//			                    }
//			                }
//			                if (newActivityCreated != null) {
//			                    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//			                    response.setServiceResponse("Team created successfully.");
//			                    apiLogInfo.setApiResponse("Team created successfully");
//			                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//
//			                } else {
//			                    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//			                    response.setServiceResponse("Team created successfully, but default activities are not mapped");
//			                    apiLogInfo.setApiResponse("Team created successfully, but default activities are not mapped");
//			                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//			                }
//			            } else {
//			                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//			                response.setServiceResponse("Unable to create a new team.");
//			                apiLogInfo.setApiResponse("Unable to create a new team.");
//			                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			            }
//			        });
//
//			        // Send mail to RMG: if HOD/SuperAdmin has created project/Team
////			        if (resourceManagementDTO.getIsHOD().equals("true") && !resourceManagementDTO.getProjectType().equals("Internal")) {
////			            try {
////			                mailService.sendMailWithCC("demo@gmail.com","sakti.das@apmosys.com",
////			                        "Regarding Resource management",
////			                        "Dear RMG Team ," + "<br>"
////			                                + "<br>"
////			                                + employeeObj.getName() + " dusra wala call kiya hai team create pr project update ka line no 729 has created a project:  -> " + resourceManagementDTO.getName()+"under this team hai "+resourceManagementDTO.getTeamList().get(0).getTeamName());                                      
////			            } catch (Exception e) {
////			                e.printStackTrace();
////			            }
////			        }
//
//			        // Send Project/Team detail JSON to PoPortal
////			        if (!resourceManagementDTO.getProjectType().equals("Internal")) {
////			            ServiceResponse poPortalResponse = sendProjectInfoToPoPortal(resourceManagementDTO);
////
////			            if (poPortalResponse.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS)) {
////			                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
////			                response.setServiceResponse("Project updated successfully");
////			                apiLogInfo.setApiResponse("Project updated successfully");
////			                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
////			            } else {
////			                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
////			                response.setServiceResponse("Project & Team created successfully, but unable to sync with PoPortal : " + poPortalResponse.getServiceResponse());
////			                apiLogInfo.setApiResponse("Project & Team created successfully, but unable to sync with PoPortal");
////			                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
////			            }
////			        } else {
//			            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//			            response.setServiceResponse("Project updated successfully");
//			            apiLogInfo.setApiResponse("Project updated successfully");
//			            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
////			        }
//			    }
//			}
//			}catch(Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//            apiLogInfo.setLogLevel("ERROR");
//		}
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//	}

	public ServiceResponse getTeamListByProjectName(ResourceManagementDTO resourceManagementDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getTeamListByProjectName");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("ProjectId : " + resourceManagementDTO.getProjectId() + " ,ProjectName :"
				+ resourceManagementDTO.getName() + " ,Id : " + resourceManagementDTO.getId());

		try {

			Project projectObj = null;
			if (resourceManagementDTO.getPoProjectId() == null) {
				projectObj = projectRepository.findByProjectId(resourceManagementDTO.getProjectId());
			} else {
				projectObj = projectRepository.findByPoProjectId(resourceManagementDTO.getId());
			}
			final Integer projectId = projectObj.getProjectId();
			System.err.println(" projectObj     " + projectObj.getProjectId());
			if (projectObj != null) {
				List<Team> teamList = teamRepository.findByProjectIdAndIsActive(projectObj.getProjectId(), "Y");
				System.out.println(" teamList    ::   " + teamList.size());
				if (!teamList.isEmpty()) {
					List<TeamDTO> teamListdto = new ArrayList<TeamDTO>();
					teamList.forEach((object) -> {
						TeamDTO teamdto = new TeamDTO();

						// Get Team Info
						teamdto.setTeamId(object.getTeamId());
						teamdto.setTeamName(object.getTeamName());
						teamdto.setDepartmentList(object.getDeptIds().split(","));
						teamdto.setSpocId(object.getSpocId());
						teamdto.setTeamLeadId(object.getTeamLeadId() != null ? object.getTeamLeadId() : null);

						GetEmployeeByNameAndEmpldDTO spocDetailsList = teamRepository.getSpocDetils(object.getSpocId());
						if (spocDetailsList != null) {
							SpocDTO spocDTO = new SpocDTO();
							spocDTO.setEmpId(spocDetailsList.getEmpId());
							spocDTO.setName(spocDetailsList.getName());
							spocDTO.setEmploymentId(spocDetailsList.getEmploymentId());
							teamdto.setSpoc(spocDTO);
						}
						
						GetEmployeeByNameAndEmpldDTO teamLeadDetailsList = teamRepository.getSpocDetils(object.getTeamLeadId());
						if (teamLeadDetailsList != null) {
							SpocDTO teamLeadDTO = new SpocDTO();
							teamLeadDTO.setEmpId(teamLeadDetailsList.getEmpId());
							teamLeadDTO.setName(teamLeadDetailsList.getName());
							teamLeadDTO.setEmploymentId(teamLeadDetailsList.getEmploymentId());
							teamdto.setTeamLead(teamLeadDTO);
						}

						// Get teamMembers Info
						List<EmployeeTeamMap> empTeamMapping = employeeTeamMapRepository
								.findByTeamIdAndActive(object.getTeamId());
						if (!empTeamMapping.isEmpty()) {
							List<TeamMemberDTO> teamMember = new ArrayList<TeamMemberDTO>();
							empTeamMapping.forEach((teamMemberObj) -> {
								String employeeName = null;
								Employee empObj = employeeRepository.findByEmpId(teamMemberObj.getEmpId());
								if (empObj == null) {
								    System.out.println("Skipping null employee for empId: " + teamMemberObj.getEmpId());
								    return; 
								}
								String consultant = empObj.getIsConsultant() != null ? empObj.getIsConsultant() : null;
								String prefixxTeamMember = "A-";
								if ("true".equalsIgnoreCase(consultant)) {
									prefixxTeamMember = "CS-";
								}

								// find department
								JobRole findJobRole = jobRoleRepository.findByjobRoleId(empObj.getJobRoleId());
								Department findDepartment = departmentRepository.findByDeptId(findJobRole.getDeptId());

								if (empObj != null && !"InActive".equalsIgnoreCase(empObj.getEmploymentstatus())) {
									employeeName = empObj.getName();
								}

								TeamMemberDTO teamMemberDTO = new TeamMemberDTO();

								if ((object.getTeamLeadId() != null)
										&& (object.getTeamLeadId().equals(teamMemberObj.getEmpId()))) {
									// Team Lead
									teamMemberDTO.setEmpId(teamMemberObj.getEmpId());
									teamMemberDTO.setName(employeeName);
									teamMemberDTO.setIsTeamLead("true");
									teamMemberDTO.setStartDate(teamMemberObj.getStartDate().toString());
									teamMemberDTO.setDepartmentName(findDepartment.getName());
									teamMemberDTO.setDepartmentId(findDepartment.getDeptId().toString());
									teamMemberDTO.setEmployeeRole(teamMemberObj.getEmployeeRole().split(","));
									teamMemberDTO.setResourceOverviewId(teamMemberObj.getResourceOverviewId() != null ?
											Long.parseLong(teamMemberObj.getResourceOverviewId().toString()) : null);
									teamMemberDTO.setEmploymentIdEmployeeType(
											prefixxTeamMember + empObj.getEmployeementId());
									teamMemberDTO.setResourceOverviewId(teamMemberObj.getResourceOverviewId() != null ? Long.parseLong(teamMemberObj.getResourceOverviewId().toString()) : null);
									teamMemberDTO.setIsShadow(teamMemberObj.getIsShadow() != null ? teamMemberObj.getIsShadow() : null);
									teamMemberDTO.setEmployeeTeamMapId(teamMemberObj.getEmployeeTeamMapId());
									Integer flag = this.isDefaultProject(teamMemberObj.getEmpId(),projectId);
									teamMemberDTO.setIsDefaultProject(flag != null ? flag: null);
									
									Map<String, Object> activeProjectInfo = this.getActiveProjectDetailsIfMultiple(teamMemberObj.getEmpId(), projectId);
									if ((Boolean) activeProjectInfo.get("isMultipleActiveProjects")) {
									    List<Map<String, Object>> otherProjects = (List<Map<String, Object>>) activeProjectInfo.get("projects");
									    teamMemberDTO.setOtherActiveProjects(otherProjects);
									} else {
									    teamMemberDTO.setOtherActiveProjects(Collections.emptyList());
									}
									
									teamMember.add(teamMemberDTO);
								} else {
									// Team Member
									teamMemberDTO.setEmpId(teamMemberObj.getEmpId());
									teamMemberDTO.setName(employeeName);
									teamMemberDTO.setStartDate(teamMemberObj.getStartDate() != null ? teamMemberObj.getStartDate().toString() : null);
									teamMemberDTO.setDepartmentName(findDepartment.getName());
									teamMemberDTO.setDepartmentId(findDepartment.getDeptId().toString());
									teamMemberDTO.setEmployeeRole(teamMemberObj.getEmployeeRole().split(","));
									teamMemberDTO.setResourceOverviewId(teamMemberObj.getResourceOverviewId() != null ?
											Long.parseLong(teamMemberObj.getResourceOverviewId().toString()) : null);
									teamMemberDTO.setEmploymentIdEmployeeType(
											prefixxTeamMember + empObj.getEmployeementId());
									teamMemberDTO.setIsShadow(teamMemberObj.getIsShadow() != null ? teamMemberObj.getIsShadow() : null);
									teamMemberDTO.setEmployeeTeamMapId(teamMemberObj.getEmployeeTeamMapId());
									Integer flag = this.isDefaultProject(teamMemberObj.getEmpId(),projectId);
									teamMemberDTO.setIsDefaultProject(flag != null ? flag: null);
									
									Map<String, Object> activeProjectInfo = this.getActiveProjectDetailsIfMultiple(teamMemberObj.getEmpId(), projectId);
									if ((Boolean) activeProjectInfo.get("isMultipleActiveProjects")) {
									    List<Map<String, Object>> otherProjects = (List<Map<String, Object>>) activeProjectInfo.get("projects");
									    teamMemberDTO.setOtherActiveProjects(otherProjects);
									} else {
									    teamMemberDTO.setOtherActiveProjects(Collections.emptyList());
									}
									
									teamMember.add(teamMemberDTO);
								}
							});
							teamdto.setTeamMemberList(teamMember);
						} else {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("No teamMember(s) found in the Team.");
							apiLogInfo.setApiResponse("No teamMember(s) Found in the Team");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						}
						teamListdto.add(teamdto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(teamListdto);
					apiLogInfo.setApiResponse("teamListDto :" + teamListdto.size());
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No team(s) found in the project.");
					apiLogInfo.setApiResponse("No team(s) found in the project.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not found.");
				apiLogInfo.setApiResponse("Project not found");
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

	public ServiceResponse alreadyCreatedTeam() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("alreadyCreatedTeam");
		apiLogInfo.setApiUrl("/api/alreadyCreatedTeam");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		// logBuilder.append("");
		try {

//			List<Project> allProjectList = projectRepository.findAll();
			List<Object[]> allProjectList = projectRepository.findAllProjectByIsDraftAndIsActive();

			allProjectList.forEach((obj) -> {
				System.err.println(obj[1] + "" + obj[7]);
			});

			if (!allProjectList.isEmpty()) {
				List<ResourceManagementDTO> dtoList = new ArrayList<ResourceManagementDTO>();
				List<EmployeeTeamMapDTO> dtoTeamList = new ArrayList<EmployeeTeamMapDTO>();

				allProjectList.forEach((object) -> {
					Integer projectId = object[0] != null ? Integer.parseInt(object[0].toString()) : null;

					ResourceManagementDTO projectDto = new ResourceManagementDTO();

					projectDto.setProjectId(object[0] != null ? Integer.parseInt(object[0].toString()) : null);
					projectDto.setProjectName(object[1] != null ? object[1].toString() : null);
//					projectDto.setProjectManager(object[2] != null ? object[2].toString() : null);
					projectDto.setCreatedOn(object[2] != null ? object[2].toString() : null);
					projectDto.setClientName(object[3] != null ? object[3].toString() : null);
					projectDto.setClientState(object[4] != null ? object[4].toString() : null);
					projectDto.setIsDraftProject(object[5] != null ? object[5].toString() : null);
					projectDto.setPoProjectId(object[6] != null ? Long.parseLong(object[6].toString()) : null);
					projectDto.setIsActive(object[7] != null ? Long.parseLong(object[7].toString()) : null);
					projectDto.setId(object[6] != null ? Long.parseLong(object[6].toString()) : null);
					projectDto.setProjectStatus(object[8] != null ? object[8].toString() : null);
					List<TeamSpocDTO> spocList = getTeamSpocsByProjectId(projectId);
					projectDto.setTeamSpocs(spocList);

					List<Object[]> result = projectManagerMappingRepository
							.findProjectManagersPerProject(Long.parseLong(projectId.toString()));

					List<Long> projectManagerIds = new ArrayList<>();
					List<ProjectManagersDTO> projectManagersList = new ArrayList<>();
					List<String> projectManagerNames = new ArrayList<>();

					for (Object[] obj : result) {
						if (obj[0] != null) {
							projectManagerIds.add(Long.parseLong(obj[0].toString()));
						}

						if (obj[1] != null) {
							projectManagerNames.add(obj[1].toString());
						}

						ProjectManagersDTO dto = new ProjectManagersDTO();
						dto.setProjectManagerId(obj[0] != null ? Long.parseLong(obj[0].toString()) : null);
						dto.setProjectManagerName(obj[1] != null ? obj[1].toString() : null);
						projectManagersList.add(dto);
					}

					String commaSeparatedNames = String.join(", ", projectManagerNames);

					projectDto.setProjectManagerId(projectManagerIds);
					projectDto.setProjectManagerName(commaSeparatedNames);
					commaSeparatedNames = "";
					projectDto.setProjectManagers(projectManagersList);
					
					List<ProjectOverheadsDTO> result2 = projectOverheadMappingRepository
							.findProjectOverheadsPerProject(Long.parseLong(projectId.toString()));

					List<Long> projectOverheadIds = new ArrayList<>();
					List<ProjectOverheadsDTO> projectOverheadsList = new ArrayList<>();
					List<String> projectOverheadNames = new ArrayList<>();

					for (ProjectOverheadsDTO obj : result2) {
						if (obj.getProjectOverheadId() != null) {
							projectOverheadIds.add(Long.parseLong(obj.getProjectOverheadId().toString()));
						}

						if (obj.getProjectOverheadName() != null) {
							projectOverheadNames.add(obj.getProjectOverheadName().toString());
						}

						ProjectOverheadsDTO dto = new ProjectOverheadsDTO();
						dto.setProjectOverheadId(obj.getProjectOverheadId() != null ? Long.parseLong(obj.getProjectOverheadId().toString()) : null);
						dto.setProjectOverheadName(obj.getProjectOverheadName() != null ? obj.getProjectOverheadName().toString() : null);
						projectOverheadsList.add(dto);
					}

					String commaSeparatedName = String.join(", ", projectManagerNames);

					projectDto.setProjectOverheadId(projectOverheadIds);
					projectDto.setProjectOverheadName(commaSeparatedName);
					commaSeparatedName = "";
					projectDto.setProjectOverheads(projectOverheadsList);

					dtoList.add(projectDto);

				});

				System.out.println(" dtoList     ::::   " + dtoList);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("dtolist size :" + dtoList.size());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Projects found.");
				apiLogInfo.setApiResponse("No Projects found");
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
//		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public List<TeamSpocDTO> getTeamSpocsByProjectId(Integer projectId) {
		List<Object[]> result = teamRepository.findTeamsAndSpocsByProjectId(projectId);
		List<TeamSpocDTO> spocList = new ArrayList<>();

		for (Object[] obj : result) {
			TeamSpocDTO dto = new TeamSpocDTO();
			dto.setTeamId(obj[0] != null ? Long.parseLong(obj[0].toString()) : null);
			dto.setTeamName(obj[1] != null ? obj[1].toString() : null);
			dto.setSpocId(obj[2] != null ? Long.parseLong(obj[2].toString()) : null);
			dto.setSpocName(obj[3] != null ? obj[3].toString() : null);
			dto.setDepartmentList(obj[4] != null ? obj[4].toString().split(",") : null);
			spocList.add(dto);
		}

		return spocList;
	}

	public List<TeamSpocDTO> getTeamsByProjectId(Integer projectId) {
		List<Team> result = teamRepository.findActiveTeamsByProjectId(projectId);
		List<TeamSpocDTO> spocList = new ArrayList<>();

		for (Team team : result) {
			TeamSpocDTO dto = new TeamSpocDTO();
			dto.setTeamId(team.getTeamId());
			dto.setTeamName(team.getTeamName());
//	        dto.setSpocId(team.getSpocId());
//	        dto.setSpocName(team.getSpocName());

			String deptListStr = team.getDeptIds();
			dto.setDepartmentList(deptListStr != null ? deptListStr.split(",") : null);

			spocList.add(dto);
		}

		return spocList;
	}

	public List<ProjectManagersDTO> getProjectManagersByProjectId(Integer projectId) {
		List<Object[]> result = projectManagerMappingRepository
				.findProjectManagersPerProject(Long.parseLong(projectId.toString()));
		List<ProjectManagersDTO> projectManagerList = new ArrayList<>();

		for (Object[] obj : result) {
			ProjectManagersDTO dto = new ProjectManagersDTO();
			dto.setProjectManagerId(obj[0] != null ? Long.parseLong(obj[0].toString()) : null);
			dto.setProjectManagerName(obj[1] != null ? obj[1].toString() : null);
			projectManagerList.add(dto);
		}

		return projectManagerList;
	}

	public ServiceResponse getPendingForApprovalProject() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getPendingForApprovalProject");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		try {

			List<Object[]> pendingPRoject = projectRepository.findProjectByIsDraftProject();
			List<ResourceManagementDTO> dtoList = new ArrayList<ResourceManagementDTO>();

			if (!pendingPRoject.isEmpty()) {
				pendingPRoject.forEach((object) -> {
					ResourceManagementDTO dto = new ResourceManagementDTO();

					int projectId = object[0] != null ? Integer.parseInt(object[0].toString()) : null;
					List<String> department = new ArrayList<String>();

					List<Object[]> projDeptMap = projectDepartmentMapRepository.getDepartmentByProjectId(projectId);
					if (!projDeptMap.isEmpty()) {
						projDeptMap.forEach((dept) -> {
							String departmentName = dept[1] != null ? dept[1].toString() : null;
							department.add(departmentName);
						});
					}

					Long count = teamRepository.countByProjectId(projectId);
					String isTeamCreated = "false";
					if (count > 0) {
						isTeamCreated = "true";
					}

					dto.setId(Long.valueOf(projectId));
					dto.setName(object[1] != null ? object[1].toString() : null);
//					dto.setProjectManagerName(object[2] != null ? object[2].toString() : null);
//					dto.setProjectManagerId(object[3] != null ? Long.parseLong(object[3].toString()) : null);
					dto.setClientName(object[4] != null ? object[4].toString() : null);
					dto.setClientState(object[5] != null ? object[5].toString() : null);
					dto.setIsDraftProject(object[6] != null ? object[6].toString() : null);
					dto.setPoProjectId(object[7] != null ? Long.parseLong(object[7].toString()) : null);
					dto.setDepartment(department.toArray(new String[department.size()]));
					dto.setIsTeamCreated(isTeamCreated);

					dtoList.add(dto);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("Pending Project Fetched:" + dtoList.size());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Pending Project found.");
				apiLogInfo.setApiResponse("No Pending Project Found");
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

	public ServiceResponse approvePendingProject(ResourceManagementDTO resourceManagementDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("ApprovePendingProject");
		apiLogInfo.setApiUrl("/api/approvePendingProject");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append(
				"ProjectId : " + resourceManagementDTO.getId() + ", EmpId : " + resourceManagementDTO.getEmpId());
		
		try {
			Project projectObj = null;
			if (resourceManagementDTO.getProjectType().equals("Internal") || resourceManagementDTO.getProjectType().equals("Bench") || resourceManagementDTO.getProjectType().equals("InternalRNDProducts") )
				projectObj = projectRepository.findByProjectId(resourceManagementDTO.getProjectId());
			else
				projectObj = projectRepository.findByPoProjectId(resourceManagementDTO.getId());
			if (projectObj != null) {

				projectObj.setIsDraftProject("false");
				Project projectDbResponse = projectRepository.save(projectObj);

				if (projectDbResponse != null) {
					Employee employeeObj = employeeRepository.findByEmpId(resourceManagementDTO.getEmpId());

					if (employeeObj != null) {
						// Send project Approval successfully mail to RMG
						try {
							mailService.sendMailWithCC(rmgMail, employeeObj.getEmail(), "Regarding Project Approval",
									"Dear RMG Team ," + "<br>" + "<br>" + employeeObj.getName()
											+ " has approved the project : " + resourceManagementDTO.getName() + "<br>"
											+ "The above Project Info with Team & Team Member details will be shared with PoPortal.");
						} catch (Exception e) {
							e.printStackTrace();
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
							apiLogInfo.setLogLevel("ERROR");
						}

						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Project Approved successfully.");
						apiLogInfo.setApiResponse("Project Approved successfully.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

						// Send Project/Team detail JSON to PoPortal
						ServiceResponse poPortalResponse = new ServiceResponse();
						if (!resourceManagementDTO.getProjectType().equals("Internal") && !resourceManagementDTO.getProjectType().equals("Bench") && !resourceManagementDTO.getProjectType().equals("InternalRNDProducts")) {    
							poPortalResponse = sendProjectInfoToPoPortal(resourceManagementDTO);

							if (poPortalResponse.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS)) {
								response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
								response.setServiceResponse("Project Approved.");
								apiLogInfo.setApiResponse("Project Approved");
								apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

								// Set active value to 1 in EmployeeTeamMap where employee's active status is 2
								List<EmployeeTeamMap> teamMembersToActivate = employeeTeamMapRepository
										.findByProjectIdAndActive(projectObj.getProjectId(), 2L);

								if (!teamMembersToActivate.isEmpty()) {
									teamMembersToActivate.forEach(teamMember -> teamMember.setActive(1L)

											);
									
									List<Project> isDraftProject = employeeTeamMapRepository
											.findByProjectIdAndActiveForDraftProject(projectObj.getProjectId(), 2L);
									
									if(!isDraftProject.isEmpty()) {
										isDraftProject.forEach(draftProject -> draftProject.setIsDraftProject("true"));
										
									}
									
									projectRepository.saveAll(isDraftProject);

									employeeTeamMapRepository.saveAll(teamMembersToActivate);
								}

							} else {
								
								List<Project> isDraftProject = employeeTeamMapRepository
										.findByProjectIdAndActiveForDraftProject(projectObj.getProjectId(), 1L);
								
								if(!isDraftProject.isEmpty()) {
									isDraftProject.forEach(draftProject -> draftProject.setIsDraftProject("false"));
									
								}
								
								projectRepository.saveAll(isDraftProject);
								
								
								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
								response.setServiceResponse(
										"Project & Team created successfully, but unable to sync with PoPortal:1010 "
												+ poPortalResponse.getServiceResponse());
								apiLogInfo.setApiResponse(
										"Project & Team created successfully, but unable to sync with PoPortal"
												+ poPortalResponse.getServiceResponse());
								apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
							}
						} else {
							
							List<EmployeeTeamMap> teamMembersToActivate = employeeTeamMapRepository
									.findByProjectIdAndActive(projectObj.getProjectId(), 2L);

							if (!teamMembersToActivate.isEmpty()) {
								teamMembersToActivate.forEach(teamMember -> teamMember.setActive(1L));
								employeeTeamMapRepository.saveAll(teamMembersToActivate);
							}
						
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse(
									"Project Approved but Internal project does not sync with PO portal.");
							apiLogInfo.setApiResponse(
									"Project Approved but Internal project does not sync with PO portal");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
						}
					} else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("User's mail address not found.");
						apiLogInfo.setApiResponse("User's mail address not found");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					}
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Unable to approve project.");
					apiLogInfo.setApiResponse("Unable to approve project");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
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

	public ServiceResponse rejectPendingProject(ResourceManagementDTO resourceManagementDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Reject Pending Project");
		apiLogInfo.setApiUrl("/api/rejectPendingProject");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append(
				"Project Id : " + resourceManagementDTO.getId() + " ,EmployeeId :" + resourceManagementDTO.getEmpId());
		try {
			System.err.println("  resourceManagementDTO    \n\n\n\n\n\n" + resourceManagementDTO);
			Project projectObj = projectRepository.findByPoProjectId(resourceManagementDTO.getId());
			if (projectObj != null) {

				projectObj.setIsDraftProject("Rejected");
				Project projectDbResponse = projectRepository.save(projectObj);

				if (projectDbResponse != null) {

					Employee employeeObj = employeeRepository.findByEmpId(resourceManagementDTO.getEmpId());
					if (employeeObj != null) {

						// Send project rejection successfully mail to rmg
						try {
							mailService.sendMailWithCC(rmgMail, employeeObj.getEmail(), "Regarding Project Rejection",
									"Dear RMG Team ," + "<br>" + "<br>" + employeeObj.getName()
											+ " has rejected the project : " + resourceManagementDTO.getName() + "<br>"
											+ "Reject Reason : " + resourceManagementDTO.getRejectReason());
						} catch (Exception e) {
							e.printStackTrace();
						}
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Project Rejected.");
						apiLogInfo.setApiResponse("Project Rejected");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

						// added by anurag for newly added user
						List<Team> findTeamToBeDeleted = teamRepository.findTeamByProjectId(projectObj.getProjectId());

						for (Team findTeam : findTeamToBeDeleted) {
							List<EmployeeTeamMap> teamMembersToActivate = employeeTeamMapRepository
									.findByTeamIdAndActive(findTeam.getTeamId());
							List<EmployeeTeamMap> findActiveTeamMembers = employeeTeamMapRepository
									.findTeammembersByTeamIdAndStatus(findTeam.getTeamId());

							System.err.println(" teamMembersToActivate     size   " + teamMembersToActivate.size());

							if (teamMembersToActivate.size() <= 1) {
								teamMembersToActivate.forEach((object) -> {
									if (object.getActive() == 2) {
										List<Activity> findActivities = activitiesRepository
												.findByTeamId(findTeam.getTeamId());
										if (!findActivities.isEmpty()) {
											activitiesRepository.deleteAll();
										}
										employeeTeamMapRepository.deleteAllByTeamId(findTeam.getTeamId());
										teamRepository.delete(findTeam);
									}
								});
							} else {
								teamMembersToActivate.forEach((teamObj) -> {
									if (teamObj.getActive() == 1) {
										teamObj.setActive(1L);
									} else {
										if (findActiveTeamMembers.isEmpty()) {
											List<Activity> findActivities = activitiesRepository
													.findByTeamId(findTeam.getTeamId());
											if (!findActivities.isEmpty()) {
												activitiesRepository.deleteAll();
											}
											employeeTeamMapRepository.deleteAllByTeamId(findTeam.getTeamId());
											teamRepository.delete(findTeam);
										} else {
											if (teamObj.getActive() == 2) {
												teamObj.setActive(0L);
											} else {
												teamObj.setActive(1L);
											}

										}
									}

									employeeTeamMapRepository.save(teamObj);
								});
							}

						}

					} else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("User's mail address not found.");
						apiLogInfo.setApiResponse("User's Mail address not Found.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

					}

				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Unable to reject project.");
					apiLogInfo.setApiResponse("Unable to reject Project");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

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

	public ServiceResponse sendProjectApproval(ResourceManagementDTO resourceManagementDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("sendProjectApproval");
		apiLogInfo.setApiUrl("/api/sendProjectApproval");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append(
				"ProjectId : " + resourceManagementDTO.getId() + "ProjectName : " + resourceManagementDTO.getName()
						+ " ,ProjectManagerName : " + resourceManagementDTO.getProjectManagerName() + " ,ClientName : "
						+ resourceManagementDTO.getClientName());
		try {

			StringBuilder html = new StringBuilder();
			html.append("<html>\n" + "  <head>\n" + "    <style>\n" + "      table {\n" + "        width: 100%;\n"
					+ "        border-collapse: collapse;\n" + "        font-family: Arial, sans-serif;\n"
					+ "        font-size: 14px;\n" + "      }\n" + "      th, td {\n"
					+ "        border: 1px solid black;\n" + "        padding: 8px;\n" + "        text-align: left;\n"
					+ "      }\n" + "      th {\n" + "        background-color: #dddddd;\n" + "      }\n"
					+ "      h2 {\n" + "        font-size: 18px;\n" + "        font-weight: bold;\n"
					+ "        margin-bottom: 12px;\n" + "      }\n" + "    </style>\n" + "  </head>\n" + "  <body>\n"
					+ "    <h2>Project Details</h2>\n" + "    <table>\n" + "      <tr>\n"
					+ "        <th>Project Name</th>\n" + "        <th>Project Manager</th>\n"
					+ "        <th>Client Name</th>\n" + "        <th>State</th>\n" + "      </tr>\n");

			// add rows to the table
			html.append("      <tr>\n");
			// add cells to the row
			html.append("        <td>" + resourceManagementDTO.getName() + "</td>\n");
			html.append("        <td>" + resourceManagementDTO.getProjectManagerName() + "</td>\n");
			html.append("        <td>" + resourceManagementDTO.getClientName() + "</td>\n");
			html.append("        <td>" + resourceManagementDTO.getClientState() + "</td>\n");
			html.append("      </tr>\n");

			html.append("    </table>\n");

			html.append("<br>\n" + "<h2>Team Info:</h2>\n");
			if (!resourceManagementDTO.getTeamList().isEmpty()) {
				for (int i = 0; i < resourceManagementDTO.getTeamList().size(); i++) {
					html.append("<h3>Team Name - " + (i + 1) + " : "
							+ resourceManagementDTO.getTeamList().get(i).getTeamName() + "</h3>\n");
					html.append("<h4>Team Member Info:</h4>\n");
					if (!resourceManagementDTO.getTeamList().get(i).getTeamMemberList().isEmpty()) {
						html.append("<ul>\n");
						for (TeamMemberDTO teamMember : resourceManagementDTO.getTeamList().get(i)
								.getTeamMemberList()) {
							if ((teamMember.getIsTeamLead() != null) && teamMember.getIsTeamLead().equals("true")) {
								html.append("<li>" + teamMember.getName() + " (Team Lead)" + "</li>\n");
							} else {
								html.append("<li>" + teamMember.getName() + "</li>\n");
							}
						}
						html.append("</ul>\n");
					}
					html.append("<br><br>");
				}
			}
			html.append(
					"<a style='background-color:blue;color:white;padding:10px 20px;text-decoration:none;border-radius:4px;' href='"
							+ rmgProjectApprovalLink + resourceManagementDTO.getId() + "'>Approve Here</a>");

			html.append("  </body>\n" + "</html>");

			List<String> mailList = new ArrayList<String>();

			for (String dept : resourceManagementDTO.getDepartment()) {
				Department deptObj = departmentRepository.findByName(dept);

				if (deptObj != null) {
					Employee empObj = employeeRepository.findByEmpId(deptObj.getHodId());
					if (empObj != null) {
						mailList.add(empObj.getEmail());
					}
				}
			}

			if (!mailList.isEmpty()) {
				boolean mailSent = mailService.sendMailWithCC(String.join(",", mailList), rmgMail,
						"Regarding Project Resource Management Application Request", "Dear Team ," + "<br>" + "<br>"
								+ "Please take necessary actions : " + "<br>" + html.toString());

				if (mailSent) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Project Approval mail sent to HOD's");
					apiLogInfo.setApiResponse("Project Approval mail sent to HOD's");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Unable to send Approval mail to HOD's");
					apiLogInfo.setApiResponse("Unable to send Approval mail to HOD's");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

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

	public String getEmploymentId(Long empId) {
		Employee employeeObj = employeeRepository.findByEmpId(empId);
		String employmentId = null;
		if (employeeObj != null) {
			employmentId = ("A-").concat(employeeObj.getEmployeementId().toString());
		}
		return employmentId;
	}

	public ServiceResponse sendProjectInfoToPoPortal(ResourceManagementDTO resourceManagementDTO) {
		ServiceResponse response = new ServiceResponse();
		System.err.println(" Anurag sync PO portal    ::   " + resourceManagementDTO);
		try {
			Project projectObj = null;
			if (!resourceManagementDTO.getProjectType().equals("Internal")) {
				projectObj = projectRepository.findByPoProjectId(resourceManagementDTO.getId());
				System.err.println(" projectObj   " + projectObj.getPoProjectId());
				List<PoProjectSyncDTO> projectInfo = new ArrayList<PoProjectSyncDTO>();
				List<PoTeamDTO> teamList = new ArrayList<PoTeamDTO>();

				if (projectObj != null) {
					PoProjectSyncDTO projectDTO = new PoProjectSyncDTO();

					projectDTO.setPoProjectId(projectObj.getPoProjectId());
					projectDTO.setProjectName(projectObj.getProjectName());
					
					if (projectObj.getIsDraftProject() == null) {
					    projectDTO.setIshineProjectStatus("Not Started");
					} else {
					    String status = String.valueOf(projectObj.getIsDraftProject());

					    if ("true".equals(status)) {
					        projectDTO.setIshineProjectStatus("Pending For Approval");
					    } else if ("false".equals(status)) {
					        projectDTO.setIshineProjectStatus("Approved");
					    } else if ("Rejected".equals(status)) {
					        projectDTO.setIshineProjectStatus("Rejected");
					    } else if ("Completed".equals(status)) {
					        projectDTO.setIshineProjectStatus("Completed");
					    } else {
					        projectDTO.setIshineProjectStatus("Not Started");
					    }
					}
				
					List<Object[]> result = projectManagerMappingRepository
							.findProjectManagersPerProject(Long.parseLong(projectObj.getProjectId().toString()));

					List<String> projectManagerIds = new ArrayList<>();

					for (Object[] obj : result) {
						if (obj[2] != null) {
							projectManagerIds.add(obj[2].toString());
						}
					}

					projectDTO.setPoProjectManagers(projectManagerIds);
					System.out.println(" projectManagerId   ::   " + projectManagerIds);
					

					// Get Team Details
					List<Team> teamDetails = teamRepository.findByProjectIdAndIsActive(projectObj.getProjectId(), "Y");

//					if (!teamDetails.isEmpty()) {
//						teamDetails.forEach((team) -> {
//							System.err.println(" anurag get PO portal sync details ::   " + team);
//							List<String> teamMember = new ArrayList<String>();
//
//							PoTeamDTO poTeamDTO = new PoTeamDTO();
//
//							poTeamDTO.setIshineTeamId(team.getTeamId());
//							poTeamDTO.setTeamName(team.getTeamName());
//
//							if (team.getCreatedBy() != null) {
//								String createdBy = getEmploymentId(team.getCreatedBy());
//								poTeamDTO.setCreatedBy(createdBy);
//							}
//
//							if (team.getUpdatedBy() != null) {
//								String updatedBy = getEmploymentId(team.getCreatedBy());
//								poTeamDTO.setUpdatedBy(updatedBy);
//							}
//
//							if (team.getUpdatedOn() != null) {
//								poTeamDTO.setUpdatedOn(team.getUpdatedOn().toString());
//							}
//
//							if (team.getTeamLeadId() != null) {
//								String teamLeadId = getEmploymentId(team.getTeamLeadId());
//								poTeamDTO.setPoTeamLeadId(teamLeadId);
//							}
//
//							String[] deptIds = team.getDeptIds().split(",");
//							List<String> departments = new ArrayList<String>();
//							for (String deptId : deptIds) {
//								Department deptObj = departmentRepository.getById(Long.parseLong(deptId));
//								if (deptObj != null) {
//									departments.add(deptObj.getName());
//								}
//							}
//							String deptList[] = departments.toArray(new String[departments.size()]);
//							poTeamDTO.setDepartmentList(deptList);
//
//							// Get TeamMember Details
//							List<EmployeeTeamMap> teamMemberDetials = employeeTeamMapRepository
//									.findByTeamIdAndActive(team.getTeamId());
////						List<EmployeeTeamMap> teamMemberDetials1 = employeeTeamMapRepository.findByTeamIdAndActive(team.getTeamId(), 2l);
//							if (!teamMemberDetials.isEmpty()) {
//								teamMemberDetials.forEach((member) -> {
//									String memberEmpId = getEmploymentId(member.getEmpId());
//									teamMember.add(memberEmpId);
//								});
//							}
////						if(!teamMemberDetials1.isEmpty()) {
////							teamMemberDetials1.forEach((member) -> {
////								String memberEmpId = getEmploymentId(member.getEmpId());
////								teamMember.add(memberEmpId);
////							});
////						}
////							String teamMemberList[] = teamMember.toArray(new String[teamMember.size()]);
////							poTeamDTO.setTeamMemberList(teamMemberList);
//							poTeamDTO.setProjectId(team.getProjectId());
//							teamList.add(poTeamDTO);
//						});
////						projectDTO.setTeamList(teamList);
//
//						System.err.println("Anurag poPortalListFind    :: " + projectDTO.toString());
//
//						// added by anurag for temp
//						for (PoTeamDTO team2 : teamList) {
//							System.err.println(team2);
//							System.out.println("-=------------------------------------------------");
//						}
//					}

					projectInfo.add(projectDTO);
					// Send projectDTO in PoPortal reverse-sync API

					try {
//						JSONArray jsonarray = new JSONArray(projectInfo);
//						ServiceResponse response1 = poPortalAPIService.reverseSync();					System.out.println(jsonarray + " : jsonarray \n\n\n");
//
//						final String syncUrl = syncProjectApi;
//						RestTemplate restTemplate = new RestTemplate();
//
//						String syncResponse = restTemplate.postForObject(syncUrl, projectInfo, String.class);
//
//						JSONObject json = new JSONObject(syncResponse);
//
//						System.out.println(syncResponse + " : syncResponse \n\n\n");
//
//						System.err.println("   jsonjsonjsonjsonjsonjson   json    " + json);
						 ServiceResponse syncResponse = poPortalAPIService.syncProjectData(projectInfo);

						if (ServiceResponse.STATUS_SUCCESS.equals(syncResponse.getServiceStatus())) {
							// Send Mail to PoPortal
							try {
								mailService.sendMail(rmgMail, "Regarding Project Sync With PoPortal",
										"Dear RMG Team ," + "<br>" + "<br>" + "Project : "
												+ resourceManagementDTO.getName()
												+ " has been successfully synced with PoPortal.");
							} catch (Exception e) {
								e.printStackTrace();
							}

							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		                    response.setServiceResponse(syncResponse.getServiceResponse());
						}

					} catch (InternalServerError e) {
						JSONObject json = new JSONObject(e.getResponseBodyAsString());

						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse(json.get("message"));
					}
//					 catch (HttpClientErrorException e) {
//						JSONObject json = new JSONObject(e.getResponseBodyAsString());
//
//						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//						response.setServiceResponse(json.get("message"));
//					} catch (HttpServerErrorException e) {
//						JSONObject json = new JSONObject(e.getResponseBodyAsString());
//
//						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//						response.setServiceResponse(json.get("message"));
//					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse bulkSyncProject(ProjectDTO projectDTO) {

		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("BulkSyncProject");
		apiLogInfo.setApiUrl("/api/bulkSyncProject");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("BulkSyncList : " + projectDTO.getBulkSyncList().size());

		try {

			for (ResourceManagementDTO rmg : projectDTO.getBulkSyncList()) {

				ServiceResponse syncResponse = sendProjectInfoToPoPortal(rmg);

				if (syncResponse.getServiceStatus().equals("Success")) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Project Synced successfully.");
					apiLogInfo.setApiResponse("Project Synced Successfully");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse(
							"Project & Team created successfully,but unable to sync with PoPortal :1496 "
									+ syncResponse.getServiceResponse());
					apiLogInfo.setApiResponse("Project & Team created successfully,but unable to sync with PoPortal");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
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

	public ServiceResponse getInternalProject() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		// apiLogInfo.setSubFeatureName("");
		apiLogInfo.setApiUrl("/api/getInternalProject");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("InternalProjectList : " + projectRepository.getAllInternalProject().size());

		try {

			List<Object[]> allInternalProject = projectRepository.getAllInternalProject();
			List<ResourceManagementDTO> projectInfo = new ArrayList<ResourceManagementDTO>();

			if (allInternalProject != null) {
				allInternalProject.forEach((object) -> {
					ResourceManagementDTO projectDTO = new ResourceManagementDTO();

					projectDTO.setProjectType("Internal");
					projectDTO.setName(object[0] != null ? object[0].toString() : null);
//					projectDTO.setProjectManager(object[10] != null ? "A-".concat(object[10].toString()) : null);
					projectDTO.setProjectManagerName(object[2] != null ? object[2].toString() : null);
					projectDTO.setProjectId(object[3] != null ? Integer.parseInt(object[3].toString()) : null);
					projectDTO.setStatus(object[11] != null ? object[11].toString() : null);
					projectDTO.setIsActive(object[12] != null ? Long.parseLong(object[12].toString()) : null);
					// projectDTO.setIsActive(2l);
					projectDTO.setEmpId(object[13] != null ? Long.parseLong(object[13].toString()) : null);
					projectDTO.setProjectStatus(object[14] != null ? object[14].toString() : null);
					// Find ClientName
					Integer clientId = object[7] != null ? Integer.parseInt(object[7].toString()) : null;
					if (clientId != null) {
						Client clientObj = clientsRepository.findByClientId(clientId);

						projectDTO.setClientName(clientObj.getClientName());
					} else {
						projectDTO.setClientName(null);
					}

					projectDTO.setClientState(object[8] != null ? object[8].toString() : null);
					projectDTO.setCreatedOn(object[5] != null ? object[5].toString() : null);
					projectDTO.setIsDraftProject(object[9] != null ? object[9].toString() : null);

					// Find ClientLocation
					if (clientId != null) {
						List<ClientLocation> clientLocation = clientLocationRepository.findByClientId(clientId);

						if (clientLocation != null) {

							String[] locationList = clientLocation.stream()
									.map((ClientLocation location) -> location.getClientLocation())
									.collect(Collectors.toList()).toArray(String[]::new);

							projectDTO.setClientLocation(locationList);
						}
					}

					// Find Project department
					Integer projectId = object[3] != null ? Integer.parseInt(object[3].toString()) : null;
//					String isTeamCreated = "false";
//					Long count = teamRepository.countByProjectId(projectId);
//					if(count>0) {
//						isTeamCreated = "false";
//					}
					List<ProjectDepartmentMap> allDeptList = projectDepartmentMapRepository.findByProjectId(projectId);
					List<String> deptList = new ArrayList<String>();

					if (allDeptList != null) {
						allDeptList.forEach((dept) -> {
							Department deptObj = departmentRepository.findByDeptId(dept.getDeptId());

							if (deptObj != null) {
								deptList.add(deptObj.getName());
							}
						});

						String[] department = deptList.stream().toArray(String[]::new);
						projectDTO.setDepartment(department);
//						projectDTO.setIsTeamCreated(isTeamCreated);
					}
					projectInfo.add(projectDTO);
				});

				System.err.println(" ANurag projectInfo  " + projectInfo);

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(projectInfo);
				apiLogInfo.setApiResponse("InternalProjectList fetched");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Internal project found.");
				apiLogInfo.setApiResponse("No Internal Project Found");
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

	/**
	 * getExistingProjectsAndTeamsByEmployee adding this method for RMG
	 * 
	 * @param employeeId
	 * @return
	 */
	public ServiceResponse getExistingProjectsAndTeamsByEmployee(ResourceManagementDTO resourceManagementDTO) {

		ServiceResponse response = new ServiceResponse();
		List<Object[]> getAllExistingProjectsAndTeams;

		if (resourceManagementDTO.getIsAllProj() != null && resourceManagementDTO.getIsAllProj() == "true") {
			getAllExistingProjectsAndTeams = employeeTeamMapRepository
					.getAllProjectsTeamsInfo(resourceManagementDTO.getEmpId());
		} else {
			getAllExistingProjectsAndTeams = employeeTeamMapRepository
					.getAllProjectsAndTeamsDetails(resourceManagementDTO.getEmpId());
		}

		List<ResourceManagementDTO> allData = new ArrayList<ResourceManagementDTO>();
		getAllExistingProjectsAndTeams.forEach(obj -> {
			ResourceManagementDTO dto = new ResourceManagementDTO();

			dto.setTeamId(obj[0] != null ? Long.parseLong(obj[0].toString()) : null);
			dto.setProjectName(obj[1] != null ? obj[1].toString() : null);
			dto.setTeamName(obj[2] != null ? obj[2].toString() : null);
			dto.setClientName(obj[3] != null ? obj[3].toString() : null);
			dto.setBillableType(obj[4] != null ? obj[4].toString() : null);
			dto.setStartDate(obj[5] != null ? obj[5].toString() : null);
			dto.setUpdatedOn(obj[6] != null ? obj[6].toString() : null);
			dto.setEmpId(obj[7] != null ? Long.parseLong(obj[7].toString()) : null);
			dto.setEndDate(obj[8] != null ? obj[8].toString().toString() : null);
//			dto.setActive(obj[9] != null ? obj[9].toString().toString() : null);
			dto.setActive(obj[9] != null ? Integer.parseInt(obj[9].toString()) : null);
			dto.setProjectId(obj[10] != null ? Integer.parseInt(obj[10].toString()) : null);
			dto.setPoStartDate(obj[11] != null ? obj[11].toString().toString() : null);
			dto.setPoEndDate(obj[12] != null ? obj[12].toString().toString() : null);
			dto.setStatus(obj[15] != null ? obj[15].toString() : null);
			dto.setPoProjectType(obj[13] != null ? obj[13].toString() : null);
			dto.setInternalProjectType(obj[14] != null ? obj[14].toString() : null);

			allData.add(dto);
		});

		if (allData != null) {
			response.setServiceResponse(allData);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);

		}
		return response;
	}

	/**
	 * update status as inActive from team and project
	 * 
	 */

	public ServiceResponse updateProjectResourceAsInActive(ResourceManagementDTO resourceManagementDTO) {
		System.err.println("Anurag   updateProjectResourceAsInActive   ");

		ServiceResponse response = new ServiceResponse();

		EmployeeTeamMap findResource = employeeTeamMapRepository.findByEmpIdAndTeamIdAndActiveStatus(
				resourceManagementDTO.getEmpId(), resourceManagementDTO.getTeamId());
		Team findTeam = teamRepository.findTeamByTeamId(resourceManagementDTO.getTeamId());
		System.out.println("findResource  " + findResource);
		if (findResource != null) {
			findResource.setActive(0l);

			if (resourceManagementDTO.getEndDate() != null) {
				String str = resourceManagementDTO.getEndDate();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				LocalDate date = LocalDate.parse(str, formatter);
				LocalDateTime endDateTime = date.atStartOfDay();

				findResource.setEndDate(endDateTime);
			} else {
				findResource.setEndDate(LocalDateTime.now());
			}

			employeeTeamMapRepository.save(findResource);

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Resource removed successfully, from Team Name - " + findTeam.getTeamName());

		}

		return response;
	}

	private String generateHtmlTable(List<TeamMemberDTO> dtoList) {
		StringBuilder html = new StringBuilder();
		Employee empName = null;
		Department department = null;
		JobRole jobRole = null;
		String designation = null;

		html.append("<html>\n" + "  <head>\n" + "    <style>\n" + "      table, th, td {\n"
				+ "        border: 1px solid black;\n" + "      }\n" + "      table {\n"
				+ "        border-collapse: collapse;\n" + "      }\n" + "    </style>\n" + "  </head>\n" + "  <body>\n"
				+ "    <table>\n" + "      <tr>\n" + "        <th>Employee Id</th>\n"
				+ "        <th>Employee Name</th>\n" + "        <th>Department Name</th>\n"
				+ "		<th>Designation Name</th>\n" + "      </tr>\n");
		for (TeamMemberDTO obj : dtoList) {
			empName = employeeRepository.findByEmpId(obj.getEmpId());
			jobRole = jobRoleRepository.findByjobRoleId(empName.getJobRoleId());
			department = departmentRepository.findByDeptId(jobRole.getDeptId());
			Optional<Object[]> result = employeeRepository.getDesignationByEmpId(obj.getEmpId());

			if (result.isPresent()) {
				Object[] data = result.get();
				designation = (String) data[0]; // Cast the first element to String
//	    	    System.out.println("Designation Name: " + designationName);
			}
//	    	designation =employeeRepository.getDesignationByEmpId(obj.getEmpId());

			html.append("      <tr>\n");
			html.append("        <td>").append(empName.getEmployeementId()).append("</td>\n");
			html.append("        <td>").append(empName.getName()).append("</td>\n");
			html.append("        <td>").append(department.getName()).append("</td>\n");
			html.append("        <td>").append(designation.toString()).append("</td>\n");
			html.append("      </tr>\n");
		}
		html.append("    </table>\n" + "  </body>\n" + "</html>");

		return html.toString();
	}

	public ServiceResponse deleteTeamByTeamId(TeamDTO teamDto) {
		ServiceResponse response = new ServiceResponse();

		Optional<Team> team = teamRepository.findById(teamDto.getTeamId());
		Team dbTeam = null;
		if (team.isPresent()) {
			Team getTeam = team.get();
			getTeam.setIsActive("N");

			List<EmployeeTeamMap> findAllMappedEmp = employeeTeamMapRepository.findByTeamId(teamDto.getTeamId());
			System.err.println("findAllMappedEmp  " + findAllMappedEmp);

			findAllMappedEmp.forEach(emp -> {

				emp.setActive(0l);
				System.err.println("findAllMappedEmp  " + teamDto.getEndDate());
				if (teamDto.getEndDate() != null) {
					String str = teamDto.getEndDate();
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
					LocalDate date = LocalDate.parse(str, formatter);
					LocalDateTime endDateTime = date.atStartOfDay();

					emp.setEndDate(endDateTime);
				} else {
					emp.setEndDate(LocalDateTime.now());
				}
				employeeTeamMapRepository.save(emp);
			});

			dbTeam = teamRepository.save(getTeam);
		}

//        team inactivate mail generateHtmlTable
		if (dbTeam != null) {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Team Deleted Successfully !!");
		} else {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Team not found !!");
		}

		try {
			mailService.sendMail("sakti.das@apmosys.com", "Regarding Resource management", "Dear RMG Team ," + "<br>"
					+ "<br>" + " team has been deleted and the following reources are removed from this team -> : "
					+ teamDto.getTeamName() + "<br>" + "<br><br>" + "Sincerely," + "<br>"
					+ "Team RMG - ApMoSys Technologies" + "<br>" + generateHtmlTable(teamDto.getTeamMemberList()));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return response;
	}

	public ServiceResponse syncPoProjectDetailsByProjectId(ResourceManagementDTO dto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("syncPoProjectDetailsByProjectId");
		apiLogInfo.setApiUrl("/api/syncPoProjectDetailsByProjectId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("syncPoProjectDetailsByProjectId " + dto.getId());

		try {
			Project projObj = null;
			if (dto.getProjectType().equals("Internal")) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("This is marked as an internal project !");
				return response;
			} else {
				projObj = projectRepository.findByPoProjectId(Long.parseLong(dto.getId().toString()));
			}

			Project project = projObj;
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

			if (project != null) {

//                project.setProjectName(dto.getName());
				project.setPoProjectType(dto.getProjectType());
//                String formattedStartDate = dateFormat.format(dto.getStartDate());
//                project.setPoStartDate(formattedStartDate);
//                String formattedEndDate = dateFormat.format(dto.getEndDate());
//                project.setPoEndDate(formattedEndDate);
				DateTimeFormatter isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

				// Parse the ISO timestamp to ZonedDateTime
				ZonedDateTime startDateTime = ZonedDateTime.parse(dto.getStartDate(), isoFormatter);
				ZonedDateTime endDateTime = ZonedDateTime.parse(dto.getEndDate(), isoFormatter);

				// Format to desired output (yyyy-MM-dd)
				DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				String formattedStartDate = startDateTime.format(outputFormatter);
				String formattedEndDate = endDateTime.format(outputFormatter);

				// Set formatted dates into project object
				project.setPoStartDate(formattedStartDate);
				project.setPoEndDate(formattedEndDate);
				project.setStatus(dto.getStatus());
				// departmentIds
				String[] departmentArray = dto.getDepartment();

				List<String> departmentList = Arrays.stream(departmentArray)
						.flatMap(department -> Arrays.stream(department.split(","))).map(String::trim)
						.collect(Collectors.toList());

				List<String> deptIds = new ArrayList<String>();
				if (!departmentList.isEmpty()) {
					departmentList.forEach(dept -> {
						Department deptList = departmentRepository.findByName(dept);
						if (deptList == null) {
							logBuilder.append("No Dept Id fetched");
						} else {
							deptIds.add(deptList.getDeptId().toString());
						}
					});
					if (!deptIds.isEmpty()) {
						String deptIdStr = String.join(", ", deptIds);
						project.setDeptId(deptIdStr);
					}
				}

				// clientId
				Integer clientId = null;
				Optional<Client> clientObj = clientsRepository.findByClientName(dto.getClientName());
				if (!clientObj.isEmpty()) {
					Client clientPresent = clientObj.get();
					clientId = clientPresent.getClientId();
				} else {
					// Add Client & Client Location
					Client newClient = new Client();
					newClient.setClientName(dto.getClientName());
					Client clientDbResponse = clientsRepository.save(newClient);
					if (clientDbResponse != null) {
						clientId = clientDbResponse.getClientId();
						List<ClientLocation> locations = new ArrayList<>();

						for (String clientLocation : dto.getClientLocation()) {
							ClientLocation newClientLocation = new ClientLocation();
							newClientLocation.setClientId(clientDbResponse.getClientId());
							newClientLocation.setClientLocation(clientLocation);
							locations.add(newClientLocation);
						}
						// Add WFH location
//			            boolean contains = dto.getClientLocation().stream().anyMatch("WFH"::equals);
						String[] clientLocations = dto.getClientLocation();
						boolean contains = Arrays.stream(clientLocations).anyMatch("WFH"::equals);

						if (!contains) {
							ClientLocation newClientLocation = new ClientLocation();
							newClientLocation.setClientId(clientDbResponse.getClientId());
							newClientLocation.setClientLocation("WFH");
							locations.add(newClientLocation);
						}
						List<ClientLocation> clientLocationDbResponse = clientLocationRepository.saveAll(locations);
						if (!clientLocationDbResponse.isEmpty()) {
							logBuilder.append("Client stored successfully in database");
						} else {
							logBuilder.append("Error occured while storing updated project details from PoPortal API.");
						}
					} else {

					}
				}
				project.setClientId(clientId);

				project.setPoNo(dto.getPoNo());
				project.setApmosysRM(dto.getApmosysRM());
				project.setIsRenewable(dto.getIsRenewable());
				project.setClientRM(dto.getClientRM());

				projectRepository.save(project);

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Synced Successfully!");

				logBuilder.append("Updated Project: ID=" + dto.getId() + ", PoNo=" + dto.getPoNo());

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

	public ServiceResponse getProjectInfo(ResourceManagementDTO resourceManagementDTO) {

		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Project 360");
		apiLogInfo.setApiUrl("/api/getProjectInfo");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("projectInfo : " + projectRepository
				.getProjectInfo(Integer.parseInt(resourceManagementDTO.getProjectViewId().toString())));

		try {
			List<Object[]> projectInfo = projectRepository
					.getProjectInfo(Integer.parseInt(resourceManagementDTO.getProjectViewId().toString()));
			Project projObj = projectRepository.findByProjectId(Integer.parseInt(resourceManagementDTO.getProjectViewId()));
			System.out.println(projObj);
			List<ResourceManagementDTO> result = new ArrayList<>();
			if (!projectInfo.isEmpty()) {
				projectInfo.forEach(object -> {
					ResourceManagementDTO dto = new ResourceManagementDTO();

					dto.setProjectId(object[0] != null ? Integer.parseInt(object[0].toString()) : null);
					dto.setProjectName(object[1] != null ? object[1].toString() : null);

					List<Object[]> resultManager = projectManagerMappingRepository
							.findProjectManagersPerProject(Long.parseLong(projObj.getProjectId().toString()));

					List<Long> projectManagerIds = new ArrayList<>();
					List<ProjectManagersDTO> projectManagersList = new ArrayList<>();
					List<String> projectManagerNames = new ArrayList<>();

					for (Object[] obj : resultManager) {
						if (obj[0] != null) {
							projectManagerIds.add(Long.parseLong(obj[0].toString()));
						}

						if (obj[1] != null) {
							projectManagerNames.add(obj[1].toString());
						}

						ProjectManagersDTO dtoManager = new ProjectManagersDTO();
						dtoManager.setProjectManagerId(obj[0] != null ? Long.parseLong(obj[0].toString()) : null);
						dtoManager.setProjectManagerName(obj[1] != null ? obj[1].toString() : null);
						projectManagersList.add(dtoManager);
					}

					String commaSeparatedNames = String.join(", ", projectManagerNames);

					dto.setProjectManagerId(projectManagerIds);
					dto.setProjectManagerName(commaSeparatedNames);
					commaSeparatedNames = "";
					dto.setProjectManagers(projectManagersList);

					
					List<ProjectOverheadsDTO> result2 = projectOverheadMappingRepository
							.findProjectOverheadsPerProject(Long.parseLong(projObj.getProjectId().toString()));

					List<Long> projectOverheadIds = new ArrayList<>();
					List<ProjectOverheadsDTO> projectOverheadsList = new ArrayList<>();
					List<String> projectOverheadNames = new ArrayList<>();

					for (ProjectOverheadsDTO obj : result2) {
						if (obj.getProjectOverheadId() == null) {
							projectOverheadIds.add(Long.parseLong(obj.getProjectOverheadId().toString()));
						}

						if (obj.getProjectOverheadName()!= null) {
							projectOverheadNames.add(obj.getProjectOverheadName().toString());
						}

						ProjectOverheadsDTO overheadDto = new ProjectOverheadsDTO();
						overheadDto.setProjectOverheadId(obj.getProjectOverheadId()!= null ? Long.parseLong(obj.getProjectOverheadId().toString()) : null);
						overheadDto.setProjectOverheadName(obj.getProjectOverheadName() != null ? obj.getProjectOverheadName().toString() : null);
						projectOverheadsList.add(overheadDto);
					}

					String commaSeparatedName = String.join(", ", projectManagerNames);

					dto.setProjectOverheadId(projectOverheadIds);
					dto.setProjectOverheadName(commaSeparatedName);
					commaSeparatedName = "";
					dto.setProjectOverheads(projectOverheadsList);

					dto.setClientName(object[4] != null ? object[4].toString().toString() : null);
					dto.setClientState(object[5] != null ? object[5].toString() : null);
					dto.setDraftStatus(object[8] != null ? object[8].toString() : null);
					dto.setPoProjectType(object[6] != null ? object[6].toString() : null);
					dto.setInternalProjectType(object[7] != null ? object[7].toString() : null);
					result.add(dto);
				});

				if (projectInfo != null) {
					response.setServiceResponse(result);
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					apiLogInfo.setApiResponse("projectInfoList fetched");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No project found.");
				apiLogInfo.setApiResponse("No Project Found");
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

	public ServiceResponse getPoProjectInfo(ResourceManagementDTO resourceManagementDTO) {

		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Project 360");
		apiLogInfo.setApiUrl("/api/getPoProjectInfo");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("projectInfo : " + projectRepository.getAllInternalProject().size());

		try {
			List<Object[]> projectInfo = projectRepository
					.getPoProjectInfo(Long.parseLong(resourceManagementDTO.getProjectViewId().toString()));
			Project projObj = projectRepository.findByPoProjectId(Long.parseLong(resourceManagementDTO.getProjectViewId()));
			List<ResourceManagementDTO> result = new ArrayList<>();
			if (!projectInfo.isEmpty()) {
				projectInfo.forEach(object -> {
					ResourceManagementDTO dto = new ResourceManagementDTO();

					dto.setProjectId(object[0] != null ? Integer.parseInt(object[0].toString()) : null);
					dto.setProjectName(object[1] != null ? object[1].toString() : null);
					List<Object[]> resultManager = projectManagerMappingRepository
							.findProjectManagersPerProject(Long.parseLong(projObj.getProjectId().toString()));

					List<Long> projectManagerIds = new ArrayList<>();
					List<ProjectManagersDTO> projectManagersList = new ArrayList<>();
					List<String> projectManagerNames = new ArrayList<>();

					for (Object[] obj : resultManager) {
						if (obj[0] != null) {
							projectManagerIds.add(Long.parseLong(obj[0].toString()));
						}

						if (obj[1] != null) {
							projectManagerNames.add(obj[1].toString());
						}

						ProjectManagersDTO dtoManager = new ProjectManagersDTO();
						dtoManager.setProjectManagerId(obj[0] != null ? Long.parseLong(obj[0].toString()) : null);
						dtoManager.setProjectManagerName(obj[1] != null ? obj[1].toString() : null);
						projectManagersList.add(dtoManager);
					}

					String commaSeparatedNames = String.join(", ", projectManagerNames);

					dto.setProjectManagerId(projectManagerIds);
					dto.setProjectManagerName(commaSeparatedNames);
					commaSeparatedNames = "";
					dto.setProjectManagers(projectManagersList);

					
					List<ProjectOverheadsDTO> result2 = projectOverheadMappingRepository
							.findProjectOverheadsPerProject(Long.parseLong(projObj.getProjectId().toString()));

					List<Long> projectOverheadIds = new ArrayList<>();
					List<ProjectOverheadsDTO> projectOverheadsList = new ArrayList<>();
					List<String> projectOverheadNames = new ArrayList<>();

					for (ProjectOverheadsDTO obj : result2) {
						if (obj.getProjectOverheadId() == null) {
							projectOverheadIds.add(Long.parseLong(obj.getProjectOverheadId().toString()));
						}

						if (obj.getProjectOverheadName() != null) {
							projectOverheadNames.add(obj.getProjectOverheadName().toString());
						}

						ProjectOverheadsDTO overheadDto = new ProjectOverheadsDTO();
						overheadDto.setProjectOverheadId(obj.getProjectOverheadId() != null ? Long.parseLong(obj.getProjectOverheadId().toString()) : null);
						overheadDto.setProjectOverheadName(obj.getProjectOverheadName() != null ? obj.getProjectOverheadName().toString() : null);
						projectOverheadsList.add(overheadDto);
					}

					String commaSeparatedName = String.join(", ", projectManagerNames);

					dto.setProjectOverheadId(projectOverheadIds);
					dto.setProjectOverheadName(commaSeparatedName);
					commaSeparatedName = "";
					dto.setProjectOverheads(projectOverheadsList);

					dto.setClientName(object[4] != null ? object[4].toString().toString() : null);
					dto.setClientState(object[5] != null ? object[5].toString() : null);
					dto.setDraftStatus(object[8] != null ? object[8].toString() : null);
					dto.setPoProjectType(object[6] != null ? object[6].toString() : null);
					dto.setInternalProjectType(object[7] != null ? object[7].toString() : null);

					result.add(dto);
				});

				if (projectInfo != null) {
					response.setServiceResponse(result);
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					apiLogInfo.setApiResponse("projectInfoList fetched");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No project found.");
				apiLogInfo.setApiResponse("No Project Found");
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

	public ServiceResponse getTeamMemberByTeamId(Long teamId) {
		ServiceResponse response = new ServiceResponse();
		List<Object[]> allEmployeeList = employeeTeamMapRepository.findEmployeeByTeamId(teamId);
		List<EmployeeDTO> dtoList = new ArrayList();
		allEmployeeList.forEach((object) -> {
			EmployeeDTO empDTO = new EmployeeDTO();

			empDTO.setEmpId(object[1] != null ? Long.parseLong(object[1].toString()) : null);
			empDTO.setName(object[0] != null ? object[0].toString() : null);

			empDTO.setEmployeementId(object[2] != null ? Long.parseLong(object[2].toString()) : null);
			empDTO.setTeamLeadName(object[5] != null ? object[5].toString() : null);
			empDTO.setTeamName(object[4] != null ? object[4].toString() : null);
			empDTO.setEmail(object[3] != null ? object[3].toString() : null);
			empDTO.setTeamLeadId(object[6] != null ? Long.parseLong(object[6].toString()) : null);

			dtoList.add(empDTO);
		});

		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(dtoList);

		return response;
	}

	public ServiceResponse getTeamInfo(ResourceManagementDTO resourceManagementDTO) {

		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Project 360");
		apiLogInfo.setApiUrl("/api/getTeamInfo");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("TeamInfo : " + projectRepository.getTeamInfo(resourceManagementDTO.getProjectId()).size());

		try {
			List<Object[]> teamInfo = projectRepository.getTeamInfo(resourceManagementDTO.getProjectId());
		    
		    Map<Long, TeamInfoTeamDTO> teamMap = new LinkedHashMap<>();

		    TeamInfoProjectDTO projectDetails = new TeamInfoProjectDTO();
		    List<TeamInfoTeamDTO> teams = new ArrayList<>();

		    if (!teamInfo.isEmpty()) {
		        Object[] firstRow = teamInfo.get(0);
		        projectDetails.setProjectId(firstRow[8] != null ? Integer.parseInt(firstRow[8].toString()) : null);
		        projectDetails.setProjectName(firstRow[9] != null ? firstRow[9].toString() : null);
		        projectDetails.setClientId(firstRow[10] != null ? Long.parseLong(firstRow[10].toString()) : null);
		        projectDetails.setClientName(firstRow[11] != null ? firstRow[11].toString() : null);
		    }

		    for (Object[] object : teamInfo) {
		        Long teamId = object[0] != null ? Long.parseLong(object[0].toString()) : null;
		        TeamInfoTeamDTO team = teamMap.get(teamId);

		        if (team == null) {
		            team = new TeamInfoTeamDTO();
		            team.setTeamId(teamId);
		            team.setTeamName(object[1] != null ? object[1].toString() : null);
		            team.setTeamLeadName(object[13] != null ? object[13].toString() : null);
		            team.setSpoc(object[12] != null ? object[12].toString() : null);
		            team.setTeamMemberDetails(new ArrayList<>());

		            teamMap.put(teamId, team);
		            teams.add(team);
		        }

		        TeamInfoTeamMemberDTO member = new TeamInfoTeamMemberDTO();
		        member.setEmpId(object[2] != null ? Long.parseLong(object[2].toString()) : null);
		        member.setEmployeeName(object[3] != null ? object[3].toString() : null);
		        member.setEmployeeRole(object[4] != null ? object[4].toString() : null);
		        member.setBillableType(object[5] != null ? object[5].toString() : null);
		        member.setStartDate(object[6] != null ? object[6].toString() : null);
		        member.setActive(object[7] != null ? Integer.parseInt(object[7].toString()) : null);
		        member.setEmployeeTeamMapId(object[15] != null ? Long.parseLong(object[15].toString()) : null);

		        member.setIsDefaultProject(
		            this.isDefaultProject(
		                member.getEmpId(), resourceManagementDTO.getProjectId()
		            )
		        );
		        	
				Map<String, Object> activeProjectInfo = this.getActiveProjectDetailsIfMultiple(member.getEmpId(), resourceManagementDTO.getProjectId());
				if ((Boolean) activeProjectInfo.get("isMultipleActiveProjects")) {
				    List<Map<String, Object>> otherProjects = (List<Map<String, Object>>) activeProjectInfo.get("projects");
				    member.setOtherActiveProjects(otherProjects);
				} else {
					member.setOtherActiveProjects(Collections.emptyList());
				}

		        team.getTeamMemberDetails().add(member);
		    }

		    projectDetails.setTeamDetails(teams);

		    if (!teamInfo.isEmpty()) {
		        response.setServiceResponse(projectDetails);
		        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		        apiLogInfo.setApiResponse("projectInfoList fetched");
		        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
		    } else {
		        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		        response.setServiceResponse("No team found.");
		        apiLogInfo.setApiResponse("No team Found");
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

	public ServiceResponse getPoProjectDetailsForPoProjects() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getPoProjectDetailsForPoProjects");
		apiLogInfo.setApiUrl("/api/getPoProjectDetailsForPoProjects");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append(
				"getPoProjectDetailsForPoProjects " + projectRepository.getPoProjectDetailsForPoProjects().size());

		try {
			List<Object[]> projectInfoList = projectRepository.getPoProjectDetailsForPoProjects();
			List<ProjectInfoDTO> projectObjList = new ArrayList<ProjectInfoDTO>();

			if (projectInfoList != null || !projectInfoList.isEmpty()) {
				projectInfoList.forEach((projectInfo) -> {
					ProjectInfoDTO projectObj = new ProjectInfoDTO();

					projectObj
							.setProjectId(projectInfo[0] != null ? Integer.parseInt(projectInfo[0].toString()) : null);
					projectObj.setProjectName(projectInfo[1] != null ? projectInfo[1].toString() : null);
					projectObj.setPoNo(projectInfo[2] != null ? projectInfo[2].toString() : null);
					projectObj.setStartDate(projectInfo[3] != null ? projectInfo[3].toString() : null);
					projectObj.setEndDate(projectInfo[4] != null ? projectInfo[4].toString() : null);
					projectObj.setProjectType(projectInfo[5] != null ? projectInfo[5].toString() : null);
					projectObj.setId(projectInfo[6] != null ? Long.parseLong(projectInfo[6].toString()) : null);

					projectObjList.add(projectObj);
				});
			}
			if (projectObjList != null || !projectObjList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(projectObjList);
			} else {
				response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
				response.setServiceResponse("Data not present !");
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

	public ServiceResponse sendEmailNotificationToBDTeam(ResourceManagementDTO resourceManagementDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("sendEmailNotificationToBDTeam");
		apiLogInfo.setApiUrl("/api/sendEmailNotificationToBDTeam");
		apiLogInfo.setLogLevel("INFO");

		try {
			DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME; // Parses ISO 8601 format
			DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

			String subject = "PO Expired: Action Required for " + resourceManagementDTO.getProjectName();

			String text = "<p>Dear BD Team,</p>" + "<p>We hope this email finds you well.</p>"
					+ "<p>The Purchase Order (PO) for the project <b>" + resourceManagementDTO.getName()
					+ "</b> has expired. Below are the PO details:</p>" + "<ul>" + "<li><b>PO Number:</b> "
					+ resourceManagementDTO.getPoNo() + "</li>" + "<li><b>Start Date:</b> "
					+ ZonedDateTime.parse(resourceManagementDTO.getStartDate(), inputFormatter).format(outputFormatter)
					+ "</li>" + "<li><b>End Date:</b> "
					+ ZonedDateTime.parse(resourceManagementDTO.getEndDate(), inputFormatter).format(outputFormatter)
					+ "</li>" + "</ul>"
					+ "<p>Currently, resources are still allocated to this project. We kindly request you to either initiate the PO renewal process or confirm if the project has been completed so that we can proceed with the necessary resource reallocation.</p>"
					+ "<p>Please let us know how you would like to proceed at your earliest convenience.</p>"
					+ "<p>Best Regards,<br/><b>RMG Team | ApMoSys Technologies Pvt. Ltd.</b></p>";

			boolean isSent = mailService.sendMailWithCC(bdMail, rmgMail, subject, text);

			if (isSent) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Email sent successfully to BD Team.");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Email sent successfully to BD Team.");
			}
		} catch (MessagingException e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Error while sending email: " + e.getMessage());
		}

		return response;
	}

	public ServiceResponse getEmployeeByNameAndEmpld() {

		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getEmployeeByNameAndEmpld");
		apiLogInfo.setApiUrl("/api/getEmployeeByNameAndEmpld");
		apiLogInfo.setLogLevel("INFO");

		try {

			List<GetEmployeeByNameAndEmpldDTO> employees = employeeRepository.getEmployeeByNameAndEmpld();

			Map<Long, GetEmployeeByNameAndEmpldDTO> uniqueMap = employees.stream()
			    .collect(Collectors.toMap(
			        GetEmployeeByNameAndEmpldDTO::getEmpId, 
			        dto -> dto, 
			        (existing, replacement) -> existing  
			    ));

			List<GetEmployeeByNameAndEmpldDTO> employeeDTOList = new ArrayList<>(uniqueMap.values());
			employeeDTOList.sort(Comparator.comparing(GetEmployeeByNameAndEmpldDTO::getName));

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(employeeDTOList);

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Error : " + e.getMessage());
		}

		return response;
	}

	@Transactional(rollbackOn = Exception.class)
	public ServiceResponse combinedPOINTERNALList(ProjectFilterDTO projectFilterDTO) {
	    ServiceResponse response = new ServiceResponse();
	    StringBuilder logBuilder = new StringBuilder();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setLogLevel("INFO");

	    try {
	        if (projectFilterDTO == null) {
	            return failResponse(response, apiLogInfo, "Invalid input: ProjectFilterDTO is null.");
	        }

	        String approvalStatus = projectFilterDTO.getApprovalStatus();
	        Long currentUserEmpId = projectFilterDTO.getCurrentUserEmpId();
	        List<Long> departmentsids = Optional.ofNullable(projectFilterDTO.getDepartmentsids()).orElse(new ArrayList<>());
	        Set<String> deptIdStrings = departmentsids.stream().map(String::valueOf).collect(Collectors.toSet());

	        // Fetch projects and team-created projects
	        ServiceResponse apiResponse = poPortalAPIService.getAllProjectsFromPoPortal();
			List<ResourceManagementDTO> poPortalProjects;
			if(apiResponse == null) {
				throw new DataNotFoundException("Data not found");
			}
	        if(ServiceResponse.STATUS_SUCCESS.equals(apiResponse.getServiceStatus())) {
	        	 poPortalProjects = (List<ResourceManagementDTO>) apiResponse.getServiceResponse();
	        }
	        else
	        {
	        	poPortalProjects = new ArrayList<>();
	        }
	        
	        
	        ServiceResponse internalProjectResponse = getInternalProject();
	        if (!ServiceResponse.STATUS_SUCCESS.equals(internalProjectResponse.getServiceStatus())) {
	            return failResponse(response, apiLogInfo, "Failed to fetch internal projects.");
	        }
	        List<ResourceManagementDTO> internalProjects = castList(internalProjectResponse.getServiceResponse());

	        ServiceResponse teamCreatedProjectsResponse = alreadyCreatedTeam();
	        if (!ServiceResponse.STATUS_SUCCESS.equals(teamCreatedProjectsResponse.getServiceStatus())) {
	            return failResponse(response, apiLogInfo, "Failed to fetch already created team projects.");
	        }
	        List<ResourceManagementDTO> teamCreatedProjects = castList(teamCreatedProjectsResponse.getServiceResponse());

	        // Process roles
	        Set<Long> spocPoIds = new HashSet<>();
	        Set<Integer> spocInternalIds = new HashSet<>();
	        Set<Long> hodPoIds = new HashSet<>();
	        Set<Integer> hodInternalIds = new HashSet<>();
	        Set<String> hodDeptIdSet = new HashSet<>();

	        List<Long> hodDeptIds = Optional.ofNullable(departmentRepository.findByHodId(currentUserEmpId))
	                .orElse(Collections.emptyList()).stream()
	                .map(Department::getDeptId).collect(Collectors.toList());
	        hodDeptIdSet.addAll(hodDeptIds.stream().map(String::valueOf).collect(Collectors.toSet()));

	        AtomicBoolean isSpoc = new AtomicBoolean(false);
	        AtomicBoolean isHod = new AtomicBoolean(false);

	        for (ResourceManagementDTO project : teamCreatedProjects) {
	            List<TeamSpocDTO> spocs = Optional.ofNullable(project.getTeamSpocs()).orElse(Collections.emptyList());

	            for (TeamSpocDTO spoc : spocs) {
	                if (spoc == null) continue;

	                if (currentUserEmpId != null && currentUserEmpId.equals(spoc.getSpocId())) {
	                    isSpoc.set(true);
	                    if (project.getPoProjectId() != null) spocPoIds.add(project.getPoProjectId());
	                    if (project.getProjectId() != null) spocInternalIds.add(project.getProjectId());
	                }

	                String[] spocDeptList = spoc.getDepartmentList();
	                if (spocDeptList != null && Arrays.stream(spocDeptList).anyMatch(hodDeptIdSet::contains)) {
	                    isHod.set(true);
	                    if (project.getPoProjectId() != null) hodPoIds.add(project.getPoProjectId());
	                    if (project.getProjectId() != null) hodInternalIds.add(project.getProjectId());
	                }
	            }
	        }

	        // Check SuperAdmin/Director Role
	        boolean isSuperAdminOrDirector = false;
	        Employee currentUser = employeeRepository.findByEmpId(currentUserEmpId);
	        if (currentUser != null && currentUser.getJobRoleId() != null) {
	            JobRole role = jobRoleRepository.findByjobRoleId(currentUser.getJobRoleId());
	            if (role != null) {
	                String r = role.getEmployeeRole();
	                String n = role.getName();
	                isSuperAdminOrDirector = "SuperAdmin".equalsIgnoreCase(r) || "Director".equalsIgnoreCase(n) || "SuperAdmin".equalsIgnoreCase(n);
	            }
	        }

	        // Tag project attributes
	        processPoPortalProjects(poPortalProjects, teamCreatedProjects);
	        processInternalProjects(internalProjects, teamCreatedProjects);

	        // Merge lists
	        List<ResourceManagementDTO> combined = new ArrayList<>();
	        combined.addAll(poPortalProjects);
	        combined.addAll(internalProjects);

	        // Apply role-based filtering
	        if (!isSuperAdminOrDirector) {
	            combined = combined.stream().filter(p -> {
	                if (p == null) return false;
	                if (isSpoc.get()) {
	                    return "Internal".equalsIgnoreCase(p.getIsDraftProject()) || "Not Started".equalsIgnoreCase(p.getIsDraftProject())
	                            || (p.getId() != null && spocPoIds.contains(p.getId()))
	                            || (p.getProjectId() != null && spocInternalIds.contains(p.getProjectId()));
	                }
	                if (isHod.get()) {
	                    return "Internal".equalsIgnoreCase(p.getIsDraftProject()) || "Not Started".equalsIgnoreCase(p.getIsDraftProject())
	                            || (p.getId() != null && hodPoIds.contains(p.getId()))
	                            || (p.getProjectId() != null && hodInternalIds.contains(p.getProjectId()));
	                }
	                return true;
	            }).collect(Collectors.toList());
	        }

	        // Department-based filtering
	        if (!deptIdStrings.isEmpty()) {
	            combined = combined.stream()
	                    .filter(p -> p != null && p.getTeamSpocs() != null &&
	                            p.getTeamSpocs().stream().anyMatch(spoc ->
	                                    spoc != null && spoc.getDepartmentList() != null &&
	                                            Arrays.stream(spoc.getDepartmentList()).anyMatch(deptIdStrings::contains)))
	                    .collect(Collectors.toList());
	        }

	        // Count project status
	        Map<String, Integer> countsMap = countStatus(combined);

	        // Approval Status Filter
	        
	        if (approvalStatus != null && !"All".equalsIgnoreCase(approvalStatus)) {
	        	if ("completedInIshine".equalsIgnoreCase(approvalStatus)) {
	        		combined = combined.stream()
	                        .filter(p -> "Completed".equalsIgnoreCase(p.getProjectStatus()))
	                        .collect(Collectors.toList());
	        	}else {
	        		combined = combined.stream()
		                    .filter(p -> approvalStatus.equalsIgnoreCase(p.getIsDraftProject()) ||
		                            ("Not Started".equalsIgnoreCase(approvalStatus) &&
		                                    "Internal".equalsIgnoreCase(p.getIsDraftProject())))
		                    .collect(Collectors.toList());
	        	}
	        }

	        // Sort by Created On descending
	        combined.sort(Comparator.comparing((ResourceManagementDTO p) -> {
	            try {
	                return p.getCreatedOn() != null ? new SimpleDateFormat("yyyy-MM-dd").parse(p.getCreatedOn()) : null;
	            } catch (Exception e) {
	                return null;
	            }
	        }, Comparator.nullsLast(Comparator.reverseOrder())));

	        // Prepare response
	        CombinedPOInternalProjectResponse result = new CombinedPOInternalProjectResponse();
	        result.setCombinedProjects(combined);
	        result.setCounts(countsMap);

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(result);
	        apiLogInfo.setApiResponse("Filtered and combined list size: " + combined.size());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something Went Wrong.");
	        response.setServiceError(e.getMessage());
	        apiLogInfo.setLogLevel("ERROR");
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	    }

	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}


	private <T> List<T> castList(Object obj) {
		return (List<T>) obj;
	}

	private ServiceResponse failResponse(ServiceResponse response, LogDTO log, String msg) {
		response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		response.setServiceResponse(msg);
		log.setApiResponse(msg);
		log.setApiStatus(ServiceResponse.STATUS_FAIL);
		return response;
	}

	private Map<String, Integer> countStatus(List<ResourceManagementDTO> projects) {
		Map<String, Integer> map = new HashMap<>();
		map.put("pendingForApprovalCount", countByStatus(projects, "Pending For Approval"));
		map.put("approvedCount", countByStatus(projects, "Approved"));
		map.put("completedCount", countByStatus(projects, "Completed"));
		map.put("notStartedCount",
				(int) projects.stream().filter(p -> "Not Started".equalsIgnoreCase(p.getIsDraftProject())
						|| "Internal".equalsIgnoreCase(p.getIsDraftProject())).count());
		map.put("rejectedCount", countByStatus(projects, "Rejected"));
		map.put("completedInIshineCount", (int) projects.stream().filter(p -> "Completed".equalsIgnoreCase(p.getProjectStatus())).count());
		return map;
	}

	private int countByStatus(List<ResourceManagementDTO> projects, String status) {
		return (int) projects.stream().filter(p -> status.equalsIgnoreCase(p.getIsDraftProject())).count();
	}

//	private List<ResourceManagementDTO> fetchPoPortalProjects() {
//		List<ResourceManagementDTO> poPortalProjects = new ArrayList<>();
//		try {
//			ResourceManagementDTO[] poPortalProjectArray = restTemplate.getForObject(allPoPortalProjects,
//					ResourceManagementDTO[].class);
//			poPortalProjects = Arrays
//					.asList(poPortalProjectArray != null ? poPortalProjectArray : new ResourceManagementDTO[0]);
//			System.out.println(poPortalProjects);
//		} catch (RestClientException e) {
//			throw new RuntimeException("Error fetching projects from PoPortal: " + e.getMessage());
//		}
//		return poPortalProjects;
//	}

	private void processPoPortalProjects(List<ResourceManagementDTO> poPortalProjects,
			List<ResourceManagementDTO> teamCreatedProjects) {
		// Step 1: Convert teamCreatedProjects to a Map for quick lookup
		Map<Long, ResourceManagementDTO> teamCreatedMap = teamCreatedProjects.stream()
				.filter(proj -> proj.getPoProjectId() != null)
				.collect(Collectors.toMap(ResourceManagementDTO::getPoProjectId, Function.identity(), (a, b) -> a));

		// Step 2: Process each poPortalProject
		for (ResourceManagementDTO proj : poPortalProjects) {
			ResourceManagementDTO selectedProj = null;
			if (proj.getId() != null) {
				selectedProj = teamCreatedMap.get(proj.getId());
			}

			if (selectedProj != null) {
				proj.setIsTeamCreated("true");
				proj.setProjectManagers(selectedProj.getProjectManagers());
				proj.setProjectManagerId(selectedProj.getProjectManagerId());
				proj.setProjectManagerName(selectedProj.getProjectManagerName());
				proj.setTeamSpocs(selectedProj.getTeamSpocs());
				proj.setProjectViewId("po" + proj.getId());
				proj.setProjectStatus(selectedProj.getProjectStatus());
				proj.setProjectOverheads(selectedProj.getProjectOverheads());
				proj.setProjectOverheadId(selectedProj.getProjectOverheadId());
				proj.setProjectOverheadName(selectedProj.getProjectOverheadName());

				Long isActive = selectedProj.getIsActive();
				String isDraft = selectedProj.getIsDraftProject();

				if (isActive != null && isActive == 2) {
					proj.setIsDraftProject("Pending For Approval");
				} else if ("Rejected".equalsIgnoreCase(isDraft)) {
					proj.setIsDraftProject("Rejected");
				} else if ("false".equalsIgnoreCase(isDraft)) {
					proj.setIsDraftProject("Approved");
				} else if ("Completed".equalsIgnoreCase(isDraft)) {
					proj.setIsDraftProject("Completed");
				} else {
					proj.setIsDraftProject("Not started");
				}
			} else {
				proj.setIsTeamCreated("false");
				proj.setIsDraftProject("Not Started");
			}

			String status = proj.getStatus();
			if (status != null) {
				if ("true".equals(status)) {
					proj.setStatus("InProgress");
				} else if ("false".equals(status)) {
					proj.setStatus("Completed");
				}
			}
		}
	}

	private void processInternalProjects(List<ResourceManagementDTO> internalProjects,
			List<ResourceManagementDTO> teamCreatedProjects) {
// Step 1: Build map from teamCreatedProjects keyed by projectId
		Map<Integer, ResourceManagementDTO> teamCreatedMap = teamCreatedProjects.stream()
				.filter(proj -> proj.getProjectId() != null)
				.collect(Collectors.toMap(ResourceManagementDTO::getProjectId, Function.identity(), (a, b) -> a));

// Step 2: Process internal projects efficiently
		for (ResourceManagementDTO proj : internalProjects) {
			Integer projectId = proj.getProjectId();
			ResourceManagementDTO selectedProj = (projectId != null) ? teamCreatedMap.get(projectId) : null;

			if (selectedProj != null) {
				proj.setIsTeamCreated("true");
				proj.setProjectManagers(selectedProj.getProjectManagers());
				proj.setProjectManagerId(selectedProj.getProjectManagerId());
				proj.setProjectManagerName(selectedProj.getProjectManagerName());
				proj.setTeamSpocs(selectedProj.getTeamSpocs());
				proj.setProjectViewId(proj.getProjectId() != null ? proj.getProjectId().toString() : null);
				proj.setProjectStatus(selectedProj.getProjectStatus());
				proj.setProjectOverheads(selectedProj.getProjectOverheads());
				proj.setProjectOverheadId(selectedProj.getProjectOverheadId());
				proj.setProjectOverheadName(selectedProj.getProjectOverheadName());

				String draftStatus = selectedProj.getIsDraftProject();
				if ("true".equalsIgnoreCase(draftStatus)) {
					proj.setIsDraftProject("Pending For Approval");
				} else if ("Rejected".equalsIgnoreCase(draftStatus)) {
					proj.setIsDraftProject("Rejected");
				} else if ("false".equalsIgnoreCase(draftStatus)) {
					proj.setIsDraftProject("Approved");
				} else if ("Completed".equalsIgnoreCase(draftStatus)) {
					proj.setIsDraftProject("Completed");
				}  else {
					proj.setIsDraftProject("Not Started");
				}
			} else {
				proj.setIsTeamCreated("false");
				proj.setIsDraftProject(proj.getProjectType());
			}

			String status = proj.getStatus();
			if (status != null) {
				if ("true".equals(status)) {
					proj.setStatus("InProgress");
				} else if ("false".equals(status)) {
					proj.setStatus("Completed");
				}
			}
		}
	}

	public ServiceResponse updateProjectResourcesAsInActiveBulk(List<ResourceManagementDTO> resourceManagementDTOList) {

		ServiceResponse response = new ServiceResponse();
		StringBuilder resultMessage = new StringBuilder();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/updateProjectResourcesAsInActiveBulk");
		apiLogInfo.setLogLevel("INFO");
		int failureCount = 0;
 try {
		for (ResourceManagementDTO resourceManagementDTO : resourceManagementDTOList) {

			EmployeeTeamMap findResource = employeeTeamMapRepository.findByEmpIdAndTeamIdAndActiveStatus(
					resourceManagementDTO.getEmpId(), resourceManagementDTO.getTeamId());
			Team findTeam = teamRepository.findTeamByTeamId(resourceManagementDTO.getTeamId());
			Project findProject = projectRepository.findByProjectId(findTeam.getProjectId());

			if (findResource != null) {
				try {
					EmployeeTeamMap emp1= employeeTeamMapRepository.findByEmployeeTeamMapId(resourceManagementDTO.getEmployeeTeamMapId());
					if(emp1 != null && emp1.getEndDate() == null  && emp1.getActive() != 0) {
					Employee emp = employeeRepository.findByEmpId(resourceManagementDTO.getEmpId());
					findResource.setActive(0L);

					if (resourceManagementDTO.getEndDate() != null) {
						String str = resourceManagementDTO.getEndDate();
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
						LocalDate date = LocalDate.parse(str, formatter);
						LocalDateTime endDateTime = date.atStartOfDay();

						findResource.setEndDate(endDateTime);
					} else {

						findResource.setEndDate(LocalDateTime.now());
					}

					employeeTeamMapRepository.save(findResource);
					try {
						mailService.sendMail(rmgMail, "Regarding Resource removed from Project ",
								"Dear " + emp.getName() + "<br>" + "You have been removed from project "
										+ findProject.getProjectName() + "under the team - " + findTeam.getTeamName()
										+ "<br>" + "<br><br>" + "Sincerely," + "<br>"
										+ "Team RMG - ApMoSys Technologies");
					} catch (AddressException e) {

						e.printStackTrace();
					} catch (MessagingException e) {

						e.printStackTrace();
					}

					resultMessage.append("Resource with EmpId " + resourceManagementDTO.getEmpId() + " from Team "
							+ findTeam.getTeamName() + " removed successfully.\n");
				}else {
					failureCount++;
					resultMessage.append("No resource found with EmpId " + resourceManagementDTO.getEmpId() + " in Team "
							+ findTeam.getTeamName() + ".\n");
				}

				} catch (Exception e) {
					failureCount++;
					resultMessage.append("Failed to remove resource with EmpId " + resourceManagementDTO.getEmpId()
							+ " from Team " + findTeam.getTeamName() + ". Error: " + e.getMessage() + "\n");
				}
			} else {
				failureCount++;
				resultMessage.append("No resource found with EmpId " + resourceManagementDTO.getEmpId() + " in Team "
						+ findTeam.getTeamName() + ".\n");
			}
		}

		if (failureCount == 0) {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Successfully removed resources");
		} else {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(resultMessage.toString());
		}
		
 }catch(Exception e) {
	 e.printStackTrace();
		response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		response.setServiceResponse("Error while sending email: " + e.getMessage());
 }

		return response;
	}

	public ServiceResponse deleteTeamsByIdsBulk(List<TeamDTO> teamDtos) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("deleteTeamsByIdsBulk");
		apiLogInfo.setApiUrl("/api/deleteTeamsByIdsBulk");
		apiLogInfo.setLogLevel("INFO");

		try {
			for (TeamDTO teamDto : teamDtos) {
				Optional<Team> team = teamRepository.findById(teamDto.getTeamId());
				Team dbTeam = null;

				if (team.isPresent()) {
					Team getTeam = team.get();
					getTeam.setIsActive("N");
					getTeam.setUpdatedBy(teamDto.getUpdatedBy());
					getTeam.setUpdatedOn(LocalDateTime.now());
					List<EmployeeTeamMap> findAllMappedEmp = employeeTeamMapRepository
							.findByTeamIdAndActive(teamDto.getTeamId());
					System.err
							.println("findAllMappedEmp for Team: " + teamDto.getTeamName() + " -> " + findAllMappedEmp);

					findAllMappedEmp.forEach(emp -> {
						emp.setActive(0L);
						emp.setUpdatedBy(teamDto.getUpdatedBy());
						emp.setUpdatedOn(LocalDateTime.now());
						if (teamDto.getEndDate() != null) {
							String str = teamDto.getEndDate();
							DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
							LocalDate date = LocalDate.parse(str, formatter);
							LocalDateTime endDateTime = date.atStartOfDay();

							emp.setEndDate(endDateTime);
						} else {
							emp.setEndDate(LocalDateTime.now());
						}
						employeeTeamMapRepository.save(emp);
					});

					dbTeam = teamRepository.save(getTeam);

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Team and Its Resources are Set Inactive");
				} else {

					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Team and Its Resources are NOt Set Inactive");
				}
			}

			try {
				StringBuilder deletedTeamsList = new StringBuilder();
				teamDtos.forEach(teamDto -> deletedTeamsList.append("<br>").append(teamDto.getTeamName()));

				mailService.sendMail("sakti.das@apmosys.com", "Regarding Resource management", "Dear RMG Team, <br><br>"
						+ "The following teams have been deleted, and the resources have been removed from these teams: "
						+ deletedTeamsList.toString() + "<br><br>Sincerely,<br>Team RMG - ApMoSys Technologies");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {

			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Error while sending email: " + e.getMessage());
		}

		return response;
	}

	public ServiceResponse updateProjectStartAndEndDate(ResourceManagementDTO resourceManagementDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			Long empid = resourceManagementDTO.getEmpId();
			Long teamid = resourceManagementDTO.getTeamId();
			EmployeeTeamMap findResource = employeeTeamMapRepository.findByEmpIdAndTeamId(empid, teamid);
			Team findTeam = teamRepository.findTeamByTeamId(resourceManagementDTO.getTeamId());

			System.out.println("findResource: " + findResource);

			if (findResource != null) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

				if (resourceManagementDTO.getEndDate() != null) {
					// String str = resourceManagementDTO.getEndDate();
					// LocalDate date = LocalDate.parse(str, formatter);
					// LocalDateTime endDateTime = date.atStartOfDay();
					// findResource.setEndDate(endDateTime);
					String str = resourceManagementDTO.getEndDate();
					LocalDateTime endDateTime;

					if (str.contains("T")) {
						endDateTime = LocalDateTime.parse(str);
					} else {
						LocalDate date = LocalDate.parse(str);
						endDateTime = date.atTime(LocalTime.now().getHour(), LocalTime.now().getMinute(),
								LocalTime.now().getSecond());
					}
					findResource.setEndDate(endDateTime);
				} else if (resourceManagementDTO.getStartDate() != null) {

					String str = resourceManagementDTO.getStartDate();
					LocalDateTime startDateTime;

					if (str.contains("T")) {
						startDateTime = LocalDateTime.parse(str);
					} else {
						LocalDate date = LocalDate.parse(str);
						startDateTime = date.atTime(LocalTime.now().getHour(), LocalTime.now().getMinute(),
								LocalTime.now().getSecond());
					}

					findResource.setStartDate(LocalDateTime.now());
					// String str = resourceManagementDTO.getStartDate();
					// LocalDate date = LocalDate.parse(str, formatter);
					// LocalDateTime startDateTime = date.atStartOfDay();
					// findResource.setStartDate(Timestamp.valueOf(startDateTime));
				} else {
					findResource.setEndDate(LocalDateTime.now());
				}

				employeeTeamMapRepository.save(findResource);

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(
						"Project StartDate/EndDate updated successfully, from Team Name - " + findTeam.getTeamName());
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Resource not found for given empId and teamId.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Error occurred while updating StartDate/EndDate: " + e.getMessage());
		}
		return response;
	}

	@Transactional(rollbackOn = Exception.class)
	public ServiceResponse completionDateOfProject(ResourceManagementDTO resourceManagementDTO) {

		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("completionDateOfProject");
		apiLogInfo.setApiUrl("/api/completionDateOfProject");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("ProjectType : " + resourceManagementDTO.getProjectType() + " ,ProjectId :"
				+ resourceManagementDTO.getProjectId() + " ,ProjectName :" + resourceManagementDTO.getName()
				+ " ,Department :" + resourceManagementDTO.getDeptName() + " ,State:"
				+ resourceManagementDTO.getClientState());

		try {
			Project projObj = null;
			if (resourceManagementDTO.getProjectType().equals("Internal")) {
				projObj = projectRepository.findByProjectId(resourceManagementDTO.getProjectId());

			} else {
				System.err.print(resourceManagementDTO.getId());
				projObj = projectRepository.findByPoProjectId(resourceManagementDTO.getId());
			}

			if (projObj == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Project Detais Is Present ");
				apiLogInfo.setApiResponse("No Project Detais Is Present");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				return response;
			}
			Project projectObj = projObj;
			
			List<Team> teams = teamRepository.findByProjectIdAndIsActive(projectObj.getProjectId(),"Y");
			
			if(teams.isEmpty()) {
				logBuilder.append("\n Empty teamlist found in database for method findByProjectIdAndIsActive for project " + projectObj.getProjectName());
			}else {
				List<TeamDTO> teamDTOs = teams.stream()
					    .map(team -> {
					        TeamDTO dto = new TeamDTO();
					        dto.setTeamId(team.getTeamId());
					        dto.setUpdatedBy(projectObj.getUpdatedBy());
					        return dto;
					    })
					    .collect(Collectors.toList());
				
				ServiceResponse response2 = deleteTeamsByIdsBulk(teamDTOs);
				logBuilder.append("\n " + response2.getServiceResponse() + " for project " + projectObj.getProjectName());
				
				if(response2.getServiceStatus() != ServiceResponse.STATUS_SUCCESS) {
					return response2;
				}
			}
			
			projectManagerMappingRepository.deactivateByProjectId(Long.parseLong(projectObj.getProjectId().toString()));

			projectObj.setProjectCompletionDate(resourceManagementDTO.getProjectCompletionDate());
			projectObj.setProjectStatus(resourceManagementDTO.getStatus());
			projectObj.setProjectStatus(resourceManagementDTO.getProjectStatus());
			Project projectDbResponse = projectRepository.save(projectObj);
			
			if (!resourceManagementDTO.getProjectType().equals("Internal")) {
				ServiceResponse	poPortalResponse = sendProjectInfoToPoPortal(resourceManagementDTO);

				if (poPortalResponse.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS)) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Completion status updated to Shankh portal!");
					apiLogInfo.setApiResponse("Reverse synced successfully!");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Unable to intimate completion status to Shankh portal!");
					apiLogInfo.setApiResponse("Reverse synced failed!");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
			}
			if (projectDbResponse != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Project Status Updated As Completed !!");
				apiLogInfo.setApiResponse("Project Status Updated As Completed");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project Status Not Updated");
				apiLogInfo.setApiResponse("Project Status Not Updated to Completed");
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
		return response;
	}
	
	
	
	public ServiceResponse nEWgetAllInternalProjectsNewRMG(ProjectFilterDTO projectFilterDTO) {
		ServiceResponse response = new ServiceResponse();
		if ("Not Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(Collections.emptyList());
			return response;
		}

		try {
			Long empIdd = projectFilterDTO.getCurrentUserEmpId();
		    Employee employee = employeeRepository.findByEmpId(empIdd);
		    JobRole jobRole = jobRoleRepository.findByjobRoleId(employee.getJobRoleId());
		    Department department = departmentRepository.findByDeptId(jobRole.getDeptId());

		    String departmentName = department.getName();
		    String role = jobRole.getEmployeeRole();
		    String name = jobRole.getName();

		    Set<String> specialDepartments = Set.of("Admin", "Resource Management Group", "Director", "Super Admin", "Accounts", "HR");
		    Set<Integer> internalProjectIds = new HashSet<>();

		    
		    if (role.equalsIgnoreCase("SuperAdmin") || name.equalsIgnoreCase("Director")
		            || name.equalsIgnoreCase("Super Admin") || role.equalsIgnoreCase("Accounts")
		            || specialDepartments.contains(departmentName)) {
		        internalProjectIds = projectRepository.findAllActiveInternalProjectIds();
		    } else {
		    	internalProjectIds = projectRepository.findInternalProjectsByManagerOverheadOrSpocOrTeamLead(empIdd);
//		    	Set<Integer> projectIdsByManager = employeeTeamMapRepository.findInternalProjectsByProjectManager(empIdd);
//		    	Set<Integer> projectIdsByOverhead = employeeTeamMapRepository.findInternalProjectsByOverhead(empIdd);
//		    	Set<Integer> projectIdsBySpocOrTeamLead = employeeTeamMapRepository.findInternalProjectsBySpocOrTeamLead(empIdd);
//
//		    	Set<Integer> combinedProjectIds = new HashSet<>();
//		    	combinedProjectIds.addAll(projectIdsByManager);
//		    	combinedProjectIds.addAll(projectIdsByOverhead);
//		    	combinedProjectIds.addAll(projectIdsBySpocOrTeamLead);
//
//		    	internalProjectIds = combinedProjectIds;

		       
		        if (departmentRepository.existsByHodId(empIdd)) {
		            List<Long> deptIds = departmentRepository.findDeptIdsByHodId(empIdd);
		            List<Team> activeTeams = teamRepository.findAllActiveTeamsOfInternalProjects();

		            Set<Integer> hodProjects = activeTeams.stream().filter(team -> {
		                String teamDeptIdsStr = team.getDeptIds();
		                if (teamDeptIdsStr == null || teamDeptIdsStr.isBlank()) return false;

		                List<Long> teamDeptIds = Arrays.stream(teamDeptIdsStr.split(","))
		                    .map(String::trim)
		                    .filter(s -> !s.isEmpty())
		                    .map(Long::parseLong)
		                    .collect(Collectors.toList());

		                return teamDeptIds.stream().anyMatch(deptIds::contains);
		            }).map(Team::getProjectId).collect(Collectors.toSet());

		            internalProjectIds.addAll(hodProjects);
		        }
		    }

			

			internalProjectIds = filterProjectidsApprovalStatusDepartmentFilter(internalProjectIds,
					projectFilterDTO.getApprovalStatus(), projectFilterDTO.getDepartmentsids());

			List<RMGFlatEmployeeProjectTeamDTO> rawData = employeeTeamMapRepository
					.findEmployeeProjectTeamDetailsByProjectIds(internalProjectIds);

			Map<Long, RMGProjectMappedEmployees> employeeMap = new HashMap<>();

			for (RMGFlatEmployeeProjectTeamDTO row : rawData) {
//				Long empId = row[0] != null ? Long.parseLong(row[0].toString()) : null;
//				Long employeementId = row[1] != null ? Long.parseLong(row[1].toString()) : null;
//				String billable = row[2] != null ? row[2].toString() : null;
//				String billableType = row[3] != null ? row[3].toString() : null;
//				String empName = row[4] != null ? row[4].toString() : null;
//				String deptName = row[5] != null ? row[5].toString() : null;
//				Integer projectId = row[6] != null ? Integer.parseInt(row[6].toString()) : null;
//				String projectName = row[7] != null ? row[7].toString() : null;
//				Long poProjectId = row[8] != null ? Long.parseLong(row[8].toString()) : null;
//				String poStartDate = row[9] != null ? row[9].toString() : null;
//				String poEndDate = row[10] != null ? row[10].toString() : null;
//				String apmosysRM = row[11] != null ? row[11].toString() : null;
//				String clientRM = row[12] != null ? row[12].toString() : null;
//				String poProjectType = row[13] != null ? row[13].toString() : null;
//				String poNo = row[14] != null ? row[14].toString() : null;
//				String clientName = row[15] != null ? row[15].toString() : null;
//				Long teamId = row[16] != null ? Long.parseLong(row[16].toString()) : null;
//				String teamName = row[17] != null ? row[17].toString() : null;
//				String teamIsActive = row[18] != null ? row[18].toString() : null;
//				String employeeRole = row[19] != null ? row[19].toString() : null;
//				Integer active = row[20] != null ? Integer.parseInt(row[20].toString()) : null;
//				Long projectManagerId = row[21] != null ? Long.parseLong(row[21].toString()) : null;
//				String projectManagerName = row[22] != null ? row[22].toString() : null;


				RMGProjectMappedEmployees dto = employeeMap.computeIfAbsent(row.getEmpId()  , k -> {
					RMGProjectMappedEmployees newDto = new RMGProjectMappedEmployees();
					newDto.setEmpId(row.getEmpId());
					newDto.setEmployeementId(row.getEmployeementId());
					newDto.setName(row.getName());
					
					newDto.setDepartment(row.getDepartment())	;
					newDto.setBillable(row.getBillable());
					newDto.setBillableType(row.getBillableType());
					newDto.setRmgprojects(new ArrayList<>());
					return newDto;
				});

				// Find existing project in employee's projects list or create new
				RMGProject existingProject = dto.getRmgprojects().stream()
						.filter(p -> p.getProjectId().equals(row.getProjectId() )).findFirst().orElse(null);

				if (existingProject == null) {
					RMGProject rmgProject = new RMGProject();
					rmgProject.setProjectId(row.getProjectId());
					rmgProject.setProjectName(row.getProjectName());
					rmgProject.setPoProjectId(row.getPoProjectId());
					rmgProject.setPoStartDate(row.getPoStartDate() );
					rmgProject.setPoEndDate(row.getPoEndDate() );
					rmgProject.setApmosysRM(row.getApmosysRM());
					rmgProject.setClientRM(row.getClientRM());
					rmgProject.setPoProjectType(row.getPoProjectType() );
					rmgProject.setPoNo(row.getPoNo() );
					rmgProject.setClientName(row.getClientName() );
					rmgProject.setRmgTeam(new ArrayList<>());
					rmgProject.setProjectManagers(new ArrayList<>());
					
					dto.getRmgprojects().add(rmgProject);
					existingProject = rmgProject;
				}

				// Add team details to the project
				RMGTeam rmgTeam = new RMGTeam();
				rmgTeam.setTeamId(row.getTeamId());
				rmgTeam.setTeamName(row.getTeamName() );
				rmgTeam.setIsActive(row.getTeamIsActive() );
				rmgTeam.setEmployeeRole(row.getEmployeeRole());
				rmgTeam.setStatus(row.getEtmActive()  == 1 ? "Approved" : "Pending for Approval");

				existingProject.getRmgTeam().add(rmgTeam);
				if (row.getPmEmpId() != null && row.getPmName() != null) {
	                boolean alreadyExists = existingProject.getProjectManagers().stream()
	                        .anyMatch(pm -> pm.getProjectManagerId().equals(row.getPmEmpId()));

	                if (!alreadyExists) {
	                    ProjectManagersDTO managerDTO = new ProjectManagersDTO();
	                    managerDTO.setProjectManagerId(row.getPmEmpId());
	                    managerDTO.setProjectManagerName(row.getPmName());
	                    existingProject.getProjectManagers().add(managerDTO);
	                }
	            }
	        }

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(new ArrayList<>(employeeMap.values()));

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Error: " + e.getMessage());
		}
		return response;
	}
	
	
	
	
	
	public ServiceResponse nEWgetAllShankhProjectsNewRMG(ProjectFilterDTO projectFilterDTO) {
		ServiceResponse response = new ServiceResponse();
		if ("Not Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(Collections.emptyList());
			return response;
		}

		try {
			Long empIdd = projectFilterDTO.getCurrentUserEmpId();
		    Employee employee = employeeRepository.findByEmpId(empIdd);
		    JobRole jobRole = jobRoleRepository.findByjobRoleId(employee.getJobRoleId());
		    Department department = departmentRepository.findByDeptId(jobRole.getDeptId());

		    String departmentName = department.getName();
		    String role = jobRole.getEmployeeRole();
		    String name = jobRole.getName();

		    Set<String> specialDepartments = Set.of("Admin", "Resource Management Group", "Director", "Super Admin", "Accounts", "HR");
		    Set<Integer> shankhProjectIds = new HashSet<>();

		    
		    if (role.equalsIgnoreCase("SuperAdmin") || name.equalsIgnoreCase("Director")
		            || name.equalsIgnoreCase("Super Admin") || role.equalsIgnoreCase("Accounts")
		            || specialDepartments.contains(departmentName)) {
		    	shankhProjectIds = projectRepository.findAllActiveShankhProjectIds();
		    } else {
		    	shankhProjectIds = projectRepository.findShankhProjectsByManagerOverheadOrSpocOrTeamLead(empIdd);
//		    	Set<Integer> projectIdsByManager = employeeTeamMapRepository.findShankhProjectsByProjectManager(empIdd);
//		    	Set<Integer> projectIdsByOverhead = employeeTeamMapRepository.findShankhProjectsByOverhead(empIdd);
//		    	Set<Integer> projectIdsBySpocOrTeamLead = employeeTeamMapRepository.findShankhProjectsBySpocOrTeamLead(empIdd);
//
//		    	Set<Integer> combinedProjectIds = new HashSet<>();
//		    	combinedProjectIds.addAll(projectIdsByManager);
//		    	combinedProjectIds.addAll(projectIdsByOverhead);
//		    	combinedProjectIds.addAll(projectIdsBySpocOrTeamLead);
//
//		    	shankhProjectIds = combinedProjectIds;

		       
		        if (departmentRepository.existsByHodId(empIdd)) {
		            List<Long> deptIds = departmentRepository.findDeptIdsByHodId(empIdd);
		            List<Team> activeTeams = teamRepository.findAllActiveTeamsOfShankhProjects();

		            Set<Integer> hodProjects = activeTeams.stream().filter(team -> {
		                String teamDeptIdsStr = team.getDeptIds();
		                if (teamDeptIdsStr == null || teamDeptIdsStr.isBlank()) return false;

		                List<Long> teamDeptIds = Arrays.stream(teamDeptIdsStr.split(","))
		                    .map(String::trim)
		                    .filter(s -> !s.isEmpty())
		                    .map(Long::parseLong)
		                    .collect(Collectors.toList());

		                return teamDeptIds.stream().anyMatch(deptIds::contains);
		            }).map(Team::getProjectId).collect(Collectors.toSet());

		            shankhProjectIds.addAll(hodProjects);
		        }
		    }

			

		    shankhProjectIds = filterProjectidsApprovalStatusDepartmentFilter(shankhProjectIds,
					projectFilterDTO.getApprovalStatus(), projectFilterDTO.getDepartmentsids());

			List<RMGFlatEmployeeProjectTeamDTO> rawData = employeeTeamMapRepository
					.findEmployeeProjectTeamDetailsByProjectIds(shankhProjectIds);

			Map<Long, RMGProjectMappedEmployees> employeeMap = new HashMap<>();

			for (RMGFlatEmployeeProjectTeamDTO row : rawData) {
//				Long empId = row[0] != null ? Long.parseLong(row[0].toString()) : null;
//				Long employeementId = row[1] != null ? Long.parseLong(row[1].toString()) : null;
//				String billable = row[2] != null ? row[2].toString() : null;
//				String billableType = row[3] != null ? row[3].toString() : null;
//				String empName = row[4] != null ? row[4].toString() : null;
//				String deptName = row[5] != null ? row[5].toString() : null;
//				Integer projectId = row[6] != null ? Integer.parseInt(row[6].toString()) : null;
//				String projectName = row[7] != null ? row[7].toString() : null;
//				Long poProjectId = row[8] != null ? Long.parseLong(row[8].toString()) : null;
//				String poStartDate = row[9] != null ? row[9].toString() : null;
//				String poEndDate = row[10] != null ? row[10].toString() : null;
//				String apmosysRM = row[11] != null ? row[11].toString() : null;
//				String clientRM = row[12] != null ? row[12].toString() : null;
//				String poProjectType = row[13] != null ? row[13].toString() : null;
//				String poNo = row[14] != null ? row[14].toString() : null;
//				String clientName = row[15] != null ? row[15].toString() : null;
//				Long teamId = row[16] != null ? Long.parseLong(row[16].toString()) : null;
//				String teamName = row[17] != null ? row[17].toString() : null;
//				String teamIsActive = row[18] != null ? row[18].toString() : null;
//				String employeeRole = row[19] != null ? row[19].toString() : null;
//				Integer active = row[20] != null ? Integer.parseInt(row[20].toString()) : null;
//				Long projectManagerId = row[21] != null ? Long.parseLong(row[21].toString()) : null;
//				String projectManagerName = row[22] != null ? row[22].toString() : null;


				RMGProjectMappedEmployees dto = employeeMap.computeIfAbsent(row.getEmpId(), k -> {
					RMGProjectMappedEmployees newDto = new RMGProjectMappedEmployees();
					newDto.setEmpId(row.getEmpId());
					newDto.setEmployeementId(row.getEmployeementId());
					newDto.setName(row.getName());
					
					newDto.setDepartment(row.getDepartment())	;
					newDto.setBillable(row.getBillable());
					newDto.setBillableType(row.getBillableType());
					newDto.setRmgprojects(new ArrayList<>());
					return newDto;
				});

				// Find existing project in employee's projects list or create new
				RMGProject existingProject = dto.getRmgprojects().stream()
						.filter(p -> p.getProjectId().equals(row.getProjectId())).findFirst().orElse(null);

				if (existingProject == null) {
					RMGProject rmgProject = new RMGProject();
					rmgProject.setProjectId(row.getProjectId());
					rmgProject.setProjectName(row.getProjectName());
					rmgProject.setPoProjectId(row.getPoProjectId());
					rmgProject.setPoStartDate(row.getPoStartDate() );
					rmgProject.setPoEndDate(row.getPoEndDate() );
					rmgProject.setApmosysRM(row.getApmosysRM());
					rmgProject.setClientRM(row.getClientRM());
					rmgProject.setPoProjectType(row.getPoProjectType() );
					rmgProject.setPoNo(row.getPoNo() );
					rmgProject.setClientName(row.getClientName() );
					rmgProject.setRmgTeam(new ArrayList<>());
					rmgProject.setProjectManagers(new ArrayList<>());
					
					dto.getRmgprojects().add(rmgProject);
					existingProject = rmgProject;
				}

				// Add team details to the project
				RMGTeam rmgTeam = new RMGTeam();
				rmgTeam.setTeamId(row.getTeamId());
				rmgTeam.setTeamName(row.getTeamName() );
				rmgTeam.setIsActive(row.getTeamIsActive() );
				rmgTeam.setEmployeeRole(row.getEmployeeRole());
				rmgTeam.setStatus(row.getEtmActive()  == 1 ? "Approved" : "Pending for Approval");

				existingProject.getRmgTeam().add(rmgTeam);
				if (row.getPmEmpId() != null && row.getPmName() != null) {
	                boolean alreadyExists = existingProject.getProjectManagers().stream()
	                        .anyMatch(pm -> pm.getProjectManagerId().equals(row.getPmEmpId()));

	                if (!alreadyExists) {
	                    ProjectManagersDTO managerDTO = new ProjectManagersDTO();
	                    managerDTO.setProjectManagerId(row.getPmEmpId());
	                    managerDTO.setProjectManagerName(row.getPmName());
	                    existingProject.getProjectManagers().add(managerDTO);
	                }
	            }
	        }

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(new ArrayList<>(employeeMap.values()));

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Error: " + e.getMessage());
		}
		return response;
	}
	
	public ServiceResponse getAllUnfilledTimesheetsProjects(NonComplianceProjects nonComplianceProjects) {
		ServiceResponse response = new ServiceResponse();

		try {
			Long empIdd = nonComplianceProjects.getEmpId();
			Employee employee = employeeRepository.findByEmpId(empIdd);
		    JobRole jobRole = jobRoleRepository.findByjobRoleId(employee.getJobRoleId());
		    Department department = departmentRepository.findByDeptId(jobRole.getDeptId());

		    String departmentName = department.getName();
		    String role = jobRole.getEmployeeRole();
		    String name = jobRole.getName();

		    Set<String> specialDepartments = Set.of("Admin", "Resource Management Group", "Director", "Super Admin", "Accounts", "HR");
		    Set<Integer> allshankhInternalProjectIds = new HashSet<>();

		    
		    if (role.equalsIgnoreCase("SuperAdmin") || name.equalsIgnoreCase("Director")
		            || name.equalsIgnoreCase("Super Admin") || role.equalsIgnoreCase("Accounts")
		            || specialDepartments.contains(departmentName)) {
		    	allshankhInternalProjectIds = projectRepository.findAllActiveShankhInternalProjectIds();
		    } else {
		       
		    	allshankhInternalProjectIds = projectRepository.findAllShankhInternalProjectsByManagerOverheadOrSpocOrTeamLead(empIdd);
//		    	Set<Integer> projectIdsByManager = employeeTeamMapRepository.findProjectsByProjectManager(empIdd);
//		    	Set<Integer> projectIdsByOverhead = employeeTeamMapRepository.findProjectsByOverhead(empIdd);
//		    	Set<Integer> projectIdsBySpocOrTeamLead = employeeTeamMapRepository.findProjectsBySpocOrTeamLead(empIdd);
//
//		    	Set<Integer> combinedProjectIds = new HashSet<>();
//		    	combinedProjectIds.addAll(projectIdsByManager);
//		    	combinedProjectIds.addAll(projectIdsByOverhead);
//		    	combinedProjectIds.addAll(projectIdsBySpocOrTeamLead);
//
//		    	allshankhInternalProjectIds = combinedProjectIds;

		       
		        if (departmentRepository.existsByHodId(empIdd)) {
		            List<Long> deptIds = departmentRepository.findDeptIdsByHodId(empIdd);
		            List<Team> activeTeams = teamRepository.findAllActiveTeamsOfShankhInternalProjects();

		            Set<Integer> hodProjects = activeTeams.stream().filter(team -> {
		                String teamDeptIdsStr = team.getDeptIds();
		                if (teamDeptIdsStr == null || teamDeptIdsStr.isBlank()) return false;

		                List<Long> teamDeptIds = Arrays.stream(teamDeptIdsStr.split(","))
		                    .map(String::trim)
		                    .filter(s -> !s.isEmpty())
		                    .map(Long::parseLong)
		                    .collect(Collectors.toList());

		                return teamDeptIds.stream().anyMatch(deptIds::contains);
		            }).map(Team::getProjectId).collect(Collectors.toSet());

		            allshankhInternalProjectIds.addAll(hodProjects);
		        }
		    }
		    
		    List<RMGProjectToEmployeeFlatDTO> rawData = employeeTeamMapRepository
					.findNonComplianceProjects(allshankhInternalProjectIds,nonComplianceProjects.getFromDate(),nonComplianceProjects.getToDate());
		    
		    System.err.println("lalalacount"+rawData.size())    ;
		    Map<Long, GetProjectToEmployeeReportForProjectDTO> projectMap = new HashMap<>();

		    for (RMGProjectToEmployeeFlatDTO row : rawData) {

//		        Long projectId = row[0] != null ? Long.parseLong(row[0].toString()) : null;
//		        String projectName = row[1] != null ? row[1].toString() : null;
//		        String apmosysRm = row[2] != null ? row[2].toString() : null;
//		        String clientRm = row[3] != null ? row[3].toString() : null;
//		        String poStartDate = row[4] != null ? row[4].toString() : null;
//		        String poEndDate = row[5] != null ? row[5].toString() : null;
//		        String poNo = row[6] != null ? row[6].toString() : null;
//		        String poProjectType = row[7] != null ? row[7].toString() : null;
//		        String clientName = row[8] != null ? row[8].toString() : null;
//
//		        Long projectManagerId = row[9] != null ? Long.parseLong(row[9].toString()) : null;
//		        String projectManagerName = row[10] != null ? row[10].toString() : null;
//
//		        Long teamId = row[11] != null ? Long.parseLong(row[11].toString()) : null;
//		        String teamName = row[12] != null ? row[12].toString() : null;
//
//		        Long empId = row[13] != null ? Long.parseLong(row[13].toString()) : null;
//		        String employeeName = row[14] != null ? row[14].toString() : null;
//		        String jobRoleName = row[15] != null ? row[15].toString() : null;
//		        String deptName = row[16] != null ? row[16].toString() : null;
//		        String mobileNoStr = row[17] != null ? row[17].toString() : null;
//		        Long mobileNo = mobileNoStr != null ? Long.parseLong(mobileNoStr) : null;
//		        String email = row[18] != null ? row[18].toString() : null;
//		        String billable = row[19] != null ? row[19].toString() : null;
//		        String billableType = row[20] != null ? row[20].toString() : null;
//		        String effectiveStartDate = row[21] != null ? row[21].toString() : null;
//		        Long employmentId =  row[22] != null ? Long.parseLong(row[22].toString()) : null;

		        GetProjectToEmployeeReportForProjectDTO projectDTO = projectMap.computeIfAbsent(row.getProjectId().longValue(),id -> {
		            GetProjectToEmployeeReportForProjectDTO dto = new GetProjectToEmployeeReportForProjectDTO();
		            dto.setProjectId(row.getProjectId().longValue());
		            dto.setProjectName(row.getProjectName());
		            dto.setApmosysRM(row.getApmosysRM());
		            dto.setClientRM(row.getClientRM());
		            dto.setClientName(row.getClientName());           
		            
		            dto.setPoStartDate(row.getPoStartDate());
		            dto.setPoEndDate(row.getPoEndDate());
		            dto.setPoNo(row.getPoNo());
		            dto.setPoProjectType(row.getPoProjectType());
		            dto.setProjectManagers(new ArrayList<>());
		            dto.setTeamDetails(new ArrayList<>());
		            return dto;
		        });

		        boolean pmExists = projectDTO.getProjectManagers().stream()
		            .anyMatch(pm -> pm.getProjectManagerId().equals(row.getPmEmpId()));
		        if (!pmExists && row.getPmEmpId()!= null) {
		            ProjectManagersDTO pmDTO = new ProjectManagersDTO();
		            pmDTO.setProjectManagerId(row.getPmEmpId());
		            pmDTO.setProjectManagerName(row.getPmName());
		            projectDTO.getProjectManagers().add(pmDTO);
		        }

		        GetProjectToEmployeeReportForTeamDTO teamDTO = projectDTO.getTeamDetails().stream()
		            .filter(t -> t.getTeamId().equals(row.getTeamId() ))
		            .findFirst()
		            .orElseGet(() -> {
		                GetProjectToEmployeeReportForTeamDTO dto = new GetProjectToEmployeeReportForTeamDTO();
		                dto.setTeamId(row.getTeamId());
		                dto.setTeamName(row.getTeamName());
		                dto.setMappedEmployeeDetails(new ArrayList<>());
		                projectDTO.getTeamDetails().add(dto);
		                return dto;
		            });

		        GetProjectToEmployeeReportForEmployeeDTO empDTO = new GetProjectToEmployeeReportForEmployeeDTO();
		        empDTO.setEmpId(row.getEmpId());
		        empDTO.setEmployeementId(row.getEmployeementId());       
		        
		        
		        empDTO.setEmployeeName(row.getName());
		        empDTO.setJobRole(row.getJobRoleName());
		        empDTO.setDeptName(row.getDepartment());
		        empDTO.setMobileNo(row.getMobileNo());
		        empDTO.setEmail(row.getEmail());
		        empDTO.setBillable(row.getBillable());
		        empDTO.setBillableType(row.getBillableType());
		        empDTO.setEffectiveStartDate(row.getEffectiveStartDate().toString());
		      

		        teamDTO.getMappedEmployeeDetails().add(empDTO);
		    }

		    List<GetProjectToEmployeeReportForProjectDTO> finalResult = new ArrayList<>(projectMap.values());
		    response.setServiceResponse(finalResult);
		    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		}catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Error: " + e.getMessage());
		}
		return response;
	}
		    
	
	public ServiceResponse nEWgetAllShankhInternalProjectsNewRMG(ProjectFilterDTO projectFilterDTO) {
		ServiceResponse response = new ServiceResponse();
		if ("Not Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(Collections.emptyList());
			return response;
		}

		try {
			Long empIdd = projectFilterDTO.getCurrentUserEmpId();
		    Employee employee = employeeRepository.findByEmpId(empIdd);
		    JobRole jobRole = jobRoleRepository.findByjobRoleId(employee.getJobRoleId());
		    Department department = departmentRepository.findByDeptId(jobRole.getDeptId());

		    String departmentName = department.getName();
		    String role = jobRole.getEmployeeRole();
		    String name = jobRole.getName();

		    Set<String> specialDepartments = Set.of("Admin", "Resource Management Group", "Director", "Super Admin", "Accounts", "HR");
		    Set<Integer> allshankhInternalProjectIds = new HashSet<>();

		    
		    if (role.equalsIgnoreCase("SuperAdmin") || name.equalsIgnoreCase("Director")
		            || name.equalsIgnoreCase("Super Admin") || role.equalsIgnoreCase("Accounts")
		            || specialDepartments.contains(departmentName)) {
		    	allshankhInternalProjectIds = projectRepository.findAllActiveShankhInternalProjectIds();
		    } else {
		    	allshankhInternalProjectIds = projectRepository.findAllShankhInternalProjectsByManagerOverheadOrSpocOrTeamLead(empIdd);
//		    	Set<Integer> projectIdsByManager = employeeTeamMapRepository.findProjectsByProjectManager(empIdd);
//		    	Set<Integer> projectIdsByOverhead = employeeTeamMapRepository.findProjectsByOverhead(empIdd);
//		    	Set<Integer> projectIdsBySpocOrTeamLead = employeeTeamMapRepository.findProjectsBySpocOrTeamLead(empIdd);
//
//		    	Set<Integer> combinedProjectIds = new HashSet<>();
//		    	combinedProjectIds.addAll(projectIdsByManager);
//		    	combinedProjectIds.addAll(projectIdsByOverhead);
//		    	combinedProjectIds.addAll(projectIdsBySpocOrTeamLead);
//
//		    	allshankhInternalProjectIds = combinedProjectIds;

		       
		        if (departmentRepository.existsByHodId(empIdd)) {
		            List<Long> deptIds = departmentRepository.findDeptIdsByHodId(empIdd);
		            List<Team> activeTeams = teamRepository.findAllActiveTeamsOfShankhInternalProjects();

		            Set<Integer> hodProjects = activeTeams.stream().filter(team -> {
		                String teamDeptIdsStr = team.getDeptIds();
		                if (teamDeptIdsStr == null || teamDeptIdsStr.isBlank()) return false;

		                List<Long> teamDeptIds = Arrays.stream(teamDeptIdsStr.split(","))
		                    .map(String::trim)
		                    .filter(s -> !s.isEmpty())
		                    .map(Long::parseLong)
		                    .collect(Collectors.toList());

		                return teamDeptIds.stream().anyMatch(deptIds::contains);
		            }).map(Team::getProjectId).collect(Collectors.toSet());

		            allshankhInternalProjectIds.addAll(hodProjects);
		        }
		    }

			

		    allshankhInternalProjectIds = filterProjectidsApprovalStatusDepartmentFilter(allshankhInternalProjectIds,
					projectFilterDTO.getApprovalStatus(), projectFilterDTO.getDepartmentsids());

			List<RMGFlatEmployeeProjectTeamDTO> rawData = employeeTeamMapRepository
					.findEmployeeProjectTeamDetailsByProjectIds(allshankhInternalProjectIds);

			Map<Long, RMGProjectMappedEmployees> employeeMap = new HashMap<>();

			for (RMGFlatEmployeeProjectTeamDTO row : rawData) {
//				Long empId = row[0] != null ? Long.parseLong(row[0].toString()) : null;
//				Long employeementId = row[1] != null ? Long.parseLong(row[1].toString()) : null;
//				String billable = row[2] != null ? row[2].toString() : null;
//				String billableType = row[3] != null ? row[3].toString() : null;
//				String empName = row[4] != null ? row[4].toString() : null;
//				String deptName = row[5] != null ? row[5].toString() : null;
//				Integer projectId = row[6] != null ? Integer.parseInt(row[6].toString()) : null;
//				String projectName = row[7] != null ? row[7].toString() : null;
//				Long poProjectId = row[8] != null ? Long.parseLong(row[8].toString()) : null;
//				String poStartDate = row[9] != null ? row[9].toString() : null;
//				String poEndDate = row[10] != null ? row[10].toString() : null;
//				String apmosysRM = row[11] != null ? row[11].toString() : null;
//				String clientRM = row[12] != null ? row[12].toString() : null;
//				String poProjectType = row[13] != null ? row[13].toString() : null;
//				String poNo = row[14] != null ? row[14].toString() : null;
//				String clientName = row[15] != null ? row[15].toString() : null;
//				Long teamId = row[16] != null ? Long.parseLong(row[16].toString()) : null;
//				String teamName = row[17] != null ? row[17].toString() : null;
//				String teamIsActive = row[18] != null ? row[18].toString() : null;
//				String employeeRole = row[19] != null ? row[19].toString() : null;
//				Integer active = row[20] != null ? Integer.parseInt(row[20].toString()) : null;
//				Long projectManagerId = row[21] != null ? Long.parseLong(row[21].toString()) : null;
//				String projectManagerName = row[22] != null ? row[22].toString() : null;


				RMGProjectMappedEmployees dto = employeeMap.computeIfAbsent(row.getEmpId(), k -> {
					RMGProjectMappedEmployees newDto = new RMGProjectMappedEmployees();
					newDto.setEmpId(row.getEmpId());
					newDto.setEmployeementId(row.getEmployeementId() );
					newDto.setName(row.getName() );
					
					newDto.setDepartment(row.getDepartment() )	;
					newDto.setBillable(row.getBillable() );
					newDto.setBillableType(row.getBillableType() );
					newDto.setRmgprojects(new ArrayList<>());
					return newDto;
				});

				// Find existing project in employee's projects list or create new
				RMGProject existingProject = dto.getRmgprojects().stream()
						.filter(p -> p.getProjectId().equals(row.getProjectId())).findFirst().orElse(null);

				if (existingProject == null) {
					RMGProject rmgProject = new RMGProject();
					rmgProject.setProjectId(row.getProjectId());
					rmgProject.setProjectName(row.getProjectName() );
					rmgProject.setPoProjectId(row.getPoProjectId() );
					rmgProject.setPoStartDate(row.getPoStartDate() );
					rmgProject.setPoEndDate(row.getPoEndDate() );
					rmgProject.setApmosysRM(row.getApmosysRM());
					rmgProject.setClientRM(row.getClientRM() );
					rmgProject.setPoProjectType(row.getPoProjectType() );
					rmgProject.setPoNo(row.getPoNo() );
					rmgProject.setClientName(row.getClientName() );
					rmgProject.setRmgTeam(new ArrayList<>());
					rmgProject.setProjectManagers(new ArrayList<>());
					
					dto.getRmgprojects().add(rmgProject);
					existingProject = rmgProject;
				}

				// Add team details to the project
				RMGTeam rmgTeam = new RMGTeam();
				rmgTeam.setTeamId(row.getTeamId() );
				rmgTeam.setTeamName(row.getTeamName() );
				rmgTeam.setIsActive(row.getTeamIsActive() );
				rmgTeam.setEmployeeRole(row.getEmployeeRole() );
				rmgTeam.setStatus(row.getEtmActive()  == 1 ? "Approved" : "Pending for Approval");

				existingProject.getRmgTeam().add(rmgTeam);
				if (row.getPmEmpId() != null && row.getPmName() != null) {
	                boolean alreadyExists = existingProject.getProjectManagers().stream()
	                        .anyMatch(pm -> pm.getProjectManagerId().equals(row.getPmEmpId() ));

	                if (!alreadyExists) {
	                    ProjectManagersDTO managerDTO = new ProjectManagersDTO();
	                    managerDTO.setProjectManagerId(row.getPmEmpId() );
	                    managerDTO.setProjectManagerName(row.getPmName());
	                    existingProject.getProjectManagers().add(managerDTO);
	                }
	            }
	        }

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(new ArrayList<>(employeeMap.values()));

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Error: " + e.getMessage());
		}
		return response;
	}

	


	
	
	public ServiceResponse employeesMappedProjectsDepartmentWise(GetEmployeeProjectReportPayloadDTO getEmployeeProjectReportPayloadDTO ) {
		ServiceResponse response = new ServiceResponse();

		try {
			List<Long> deptIds = getEmployeeProjectReportPayloadDTO.getDeptId();
			Set<Integer> internalProjectIds = new HashSet<>();
			
				internalProjectIds = projectRepository.findAllActiveShankhInternalProjectIds();

			List<Object[]> rawData = employeeTeamMapRepository
					.findEmployeeProjectTeamDetailsByProjectIdsAndDepartment(internalProjectIds,deptIds);

			Map<Long, RMGProjectMappedEmployees> employeeMap = new HashMap<>();

			for (Object[] row : rawData) {
				Long empId = row[0] != null ? Long.parseLong(row[0].toString()) : null;
				Long employeementId = row[1] != null ? Long.parseLong(row[1].toString()) : null;
				String billable = row[2] != null ? row[2].toString() : null;
				String billableType = row[3] != null ? row[3].toString() : null;
				String empName = row[4] != null ? row[4].toString() : null;
				String deptName = row[5] != null ? row[5].toString() : null;
				Integer projectId = row[6] != null ? Integer.parseInt(row[6].toString()) : null;
				String projectName = row[7] != null ? row[7].toString() : null;
				Long poProjectId = row[8] != null ? Long.parseLong(row[8].toString()) : null;
				String poStartDate = row[9] != null ? row[9].toString() : null;
				String poEndDate = row[10] != null ? row[10].toString() : null;
				String apmosysRM = row[11] != null ? row[11].toString() : null;
				String clientRM = row[12] != null ? row[12].toString() : null;
				String poProjectType = row[13] != null ? row[13].toString() : null;
				String poNo = row[14] != null ? row[14].toString() : null;
				String clientName = row[15] != null ? row[15].toString() : null;
				Long teamId = row[16] != null ? Long.parseLong(row[16].toString()) : null;
				String teamName = row[17] != null ? row[17].toString() : null;
				String teamIsActive = row[18] != null ? row[18].toString() : null;
				String employeeRole = row[19] != null ? row[19].toString() : null;
				Integer active = row[20] != null ? Integer.parseInt(row[20].toString()) : null;
				Long projectManagerId = row[21] != null ? Long.parseLong(row[21].toString()) : null;
				String projectManagerName = row[22] != null ? row[22].toString() : null;


				RMGProjectMappedEmployees dto = employeeMap.computeIfAbsent(empId, k -> {
					RMGProjectMappedEmployees newDto = new RMGProjectMappedEmployees();
					newDto.setEmpId(empId);
					newDto.setEmployeementId(employeementId);
					newDto.setName(empName);
					newDto.setDepartment(deptName)	;
					newDto.setBillable(billable);
					newDto.setBillableType(billableType);
					newDto.setRmgprojects(new ArrayList<>());
					return newDto;
				});

				// Find existing project in employee's projects list or create new
				RMGProject existingProject = dto.getRmgprojects().stream()
						.filter(p -> p.getProjectId().equals(projectId)).findFirst().orElse(null);

				if (existingProject == null) {
					RMGProject rmgProject = new RMGProject();
					rmgProject.setProjectId(projectId);
					rmgProject.setProjectName(projectName);
					rmgProject.setPoProjectId(poProjectId);
					rmgProject.setPoStartDate(poStartDate);
					rmgProject.setPoEndDate(poEndDate);
					rmgProject.setApmosysRM(apmosysRM);
					rmgProject.setClientRM(clientRM);
					rmgProject.setPoProjectType(poProjectType);
					rmgProject.setPoNo(poNo);
					rmgProject.setClientName(clientName);
					rmgProject.setRmgTeam(new ArrayList<>());
					rmgProject.setProjectManagers(new ArrayList<>());
					
					dto.getRmgprojects().add(rmgProject);
					existingProject = rmgProject;
				}

				// Add team details to the project
				RMGTeam rmgTeam = new RMGTeam();
				rmgTeam.setTeamId(teamId);
				rmgTeam.setTeamName(teamName);
				rmgTeam.setIsActive(teamIsActive);
				rmgTeam.setEmployeeRole(employeeRole);
				rmgTeam.setStatus(active == 1 ? "Approved" : "Pending for Approval");

				existingProject.getRmgTeam().add(rmgTeam);
				if (projectManagerId != null && projectManagerName != null) {
	                boolean alreadyExists = existingProject.getProjectManagers().stream()
	                        .anyMatch(pm -> pm.getProjectManagerId().equals(projectManagerId));

	                if (!alreadyExists) {
	                    ProjectManagersDTO managerDTO = new ProjectManagersDTO();
	                    managerDTO.setProjectManagerId(projectManagerId);
	                    managerDTO.setProjectManagerName(projectManagerName);
	                    existingProject.getProjectManagers().add(managerDTO);
	                }
	            }
	        }

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(new ArrayList<>(employeeMap.values()));

		} catch (Exception e) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Error: " + e.getMessage());
		}
		return response;
	} 
	
	public ServiceResponse nEWgetBOTHShankhInternalProjectsNewRMG(ProjectFilterDTO projectFilterDTO) {
		ServiceResponse response = new ServiceResponse();

		if ("Not Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(Collections.emptyList());
			return response;
		}
		try {
			Long empIdd = projectFilterDTO.getCurrentUserEmpId();
		    Employee employee = employeeRepository.findByEmpId(empIdd);
		    JobRole jobRole = jobRoleRepository.findByjobRoleId(employee.getJobRoleId());
		    Department department = departmentRepository.findByDeptId(jobRole.getDeptId());

		    String departmentName = department.getName();
		    String role = jobRole.getEmployeeRole();
		    String name = jobRole.getName();

		    Set<String> specialDepartments = Set.of("Admin", "Resource Management Group", "Director", "Super Admin", "Accounts", "HR");
		    Set<Integer> allshankhInternalProjectIds = new HashSet<>();

		    
		    if (role.equalsIgnoreCase("SuperAdmin") || name.equalsIgnoreCase("Director")
		            || name.equalsIgnoreCase("Super Admin") || role.equalsIgnoreCase("Accounts")
		            || specialDepartments.contains(departmentName)) {
		    	allshankhInternalProjectIds = projectRepository.findAllActiveShankhInternalProjectIds();
		    } else {
		    	
		    	allshankhInternalProjectIds = projectRepository.findAllShankhInternalProjectsByManagerOverheadOrSpocOrTeamLead(empIdd);
		       
//		    	Set<Integer> projectIdsByManager = employeeTeamMapRepository.findProjectsByProjectManager(empIdd);
//		    	Set<Integer> projectIdsByOverhead = employeeTeamMapRepository.findProjectsByOverhead(empIdd);
//		    	Set<Integer> projectIdsBySpocOrTeamLead = employeeTeamMapRepository.findProjectsBySpocOrTeamLead(empIdd);
//
//		    	Set<Integer> combinedProjectIds = new HashSet<>();
//		    	combinedProjectIds.addAll(projectIdsByManager);
//		    	combinedProjectIds.addAll(projectIdsByOverhead);
//		    	combinedProjectIds.addAll(projectIdsBySpocOrTeamLead);
//
//		    	allshankhInternalProjectIds = combinedProjectIds;

		       
		        if (departmentRepository.existsByHodId(empIdd)) {
		            List<Long> deptIds = departmentRepository.findDeptIdsByHodId(empIdd);
		            List<Team> activeTeams = teamRepository.findAllActiveTeamsOfShankhInternalProjects();

		            Set<Integer> hodProjects = activeTeams.stream().filter(team -> {
		                String teamDeptIdsStr = team.getDeptIds();
		                if (teamDeptIdsStr == null || teamDeptIdsStr.isBlank()) return false;

		                List<Long> teamDeptIds = Arrays.stream(teamDeptIdsStr.split(","))
		                    .map(String::trim)
		                    .filter(s -> !s.isEmpty())
		                    .map(Long::parseLong)
		                    .collect(Collectors.toList());

		                return teamDeptIds.stream().anyMatch(deptIds::contains);
		            }).map(Team::getProjectId).collect(Collectors.toSet());

		            allshankhInternalProjectIds.addAll(hodProjects);
		        }
		    }

		    allshankhInternalProjectIds = filterProjectidsApprovalStatusDepartmentFilter(allshankhInternalProjectIds,
					projectFilterDTO.getApprovalStatus(), projectFilterDTO.getDepartmentsids());

			List<RMGFlatEmployeeProjectTeamDTO> rawData = employeeTeamMapRepository
					.findEmployeeProjectTeamDetailsMatchedBothProjects(allshankhInternalProjectIds);

			Map<Long, RMGProjectMappedEmployees> employeeMap = new HashMap<>();

			for (RMGFlatEmployeeProjectTeamDTO row : rawData) {
//				Long empId = row[0] != null ? Long.parseLong(row[0].toString()) : null;
//				Long employeementId = row[1] != null ? Long.parseLong(row[1].toString()) : null;
//				String billable = row[2] != null ? row[2].toString() : null;
//				String billableType = row[3] != null ? row[3].toString() : null;
//				String empName = row[4] != null ? row[4].toString() : null;
//				String deptName = row[5] != null ? row[5].toString() : null;
//				Integer projectId = row[6] != null ? Integer.parseInt(row[6].toString()) : null;
//				String projectName = row[7] != null ? row[7].toString() : null;
//				Long poProjectId = row[8] != null ? Long.parseLong(row[8].toString()) : null;
//				String poStartDate = row[9] != null ? row[9].toString() : null;
//				String poEndDate = row[10] != null ? row[10].toString() : null;
//				String apmosysRM = row[11] != null ? row[11].toString() : null;
//				String clientRM = row[12] != null ? row[12].toString() : null;
//				String poProjectType = row[13] != null ? row[13].toString() : null;
//				String poNo = row[14] != null ? row[14].toString() : null;
//				String clientName = row[15] != null ? row[15].toString() : null;
//				Long teamId = row[16] != null ? Long.parseLong(row[16].toString()) : null;
//				String teamName = row[17] != null ? row[17].toString() : null;
//				String teamIsActive = row[18] != null ? row[18].toString() : null;
//				String employeeRole = row[19] != null ? row[19].toString() : null;
//				Integer active = row[20] != null ? Integer.parseInt(row[20].toString()) : null;
//				Long projectManagerId = row[21] != null ? Long.parseLong(row[21].toString()) : null;
//				String projectManagerName = row[22] != null ? row[22].toString() : null;


				RMGProjectMappedEmployees dto = employeeMap.computeIfAbsent(row.getEmpId(), k -> {
					RMGProjectMappedEmployees newDto = new RMGProjectMappedEmployees();
					newDto.setEmpId(row.getEmpId());
					newDto.setEmployeementId(row.getEmployeementId() );
					newDto.setName(row.getName() );
					
					newDto.setDepartment(row.getDepartment() )	;
					newDto.setBillable(row.getBillable() );
					newDto.setBillableType(row.getBillableType() );
					newDto.setRmgprojects(new ArrayList<>());
					return newDto;
				});

				// Find existing project in employee's projects list or create new
				RMGProject existingProject = dto.getRmgprojects().stream()
						.filter(p -> p.getProjectId().equals(row.getProjectId())).findFirst().orElse(null);

				if (existingProject == null) {
					RMGProject rmgProject = new RMGProject();
					rmgProject.setProjectId(row.getProjectId());
					rmgProject.setProjectName(row.getProjectName() );
					rmgProject.setPoProjectId(row.getPoProjectId() );
					rmgProject.setPoStartDate(row.getPoStartDate() );
					rmgProject.setPoEndDate(row.getPoEndDate() );
					rmgProject.setApmosysRM(row.getApmosysRM());
					rmgProject.setClientRM(row.getClientRM() );
					rmgProject.setPoProjectType(row.getPoProjectType() );
					rmgProject.setPoNo(row.getPoNo() );
					rmgProject.setClientName(row.getClientName() );
					rmgProject.setRmgTeam(new ArrayList<>());
					rmgProject.setProjectManagers(new ArrayList<>());
					
					dto.getRmgprojects().add(rmgProject);
					existingProject = rmgProject;
				}

				// Add team details to the project
				RMGTeam rmgTeam = new RMGTeam();
				rmgTeam.setTeamId(row.getTeamId() );
				rmgTeam.setTeamName(row.getTeamName() );
				rmgTeam.setIsActive(row.getTeamIsActive() );
				rmgTeam.setEmployeeRole(row.getEmployeeRole() );
				rmgTeam.setStatus(row.getEtmActive()  == 1 ? "Approved" : "Pending for Approval");

				existingProject.getRmgTeam().add(rmgTeam);
				if (row.getPmEmpId() != null && row.getPmName() != null) {
	                boolean alreadyExists = existingProject.getProjectManagers().stream()
	                        .anyMatch(pm -> pm.getProjectManagerId().equals(row.getPmEmpId() ));

	                if (!alreadyExists) {
	                    ProjectManagersDTO managerDTO = new ProjectManagersDTO();
	                    managerDTO.setProjectManagerId(row.getPmEmpId() );
	                    managerDTO.setProjectManagerName(row.getPmName());
	                    existingProject.getProjectManagers().add(managerDTO);
	                }
	            }
	        }

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(new ArrayList<>(employeeMap.values()));

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Error: " + e.getMessage());
		}
		return response;
	} 
	
	public ServiceResponse getEmployessWithoutProjects(ProjectFilterDTO projectFilterDTO) {
		ServiceResponse response = new ServiceResponse();
		
		try {
			Long empIdd = projectFilterDTO.getCurrentUserEmpId();
		    Employee employee = employeeRepository.findByEmpId(empIdd);
		    JobRole jobRole = jobRoleRepository.findByjobRoleId(employee.getJobRoleId());
		    Department department = departmentRepository.findByDeptId(jobRole.getDeptId());

		    String departmentName = department.getName();
		    String role = jobRole.getEmployeeRole();
		    String name = jobRole.getName();

		    Set<String> specialDepartments = Set.of("Admin", "Resource Management Group", "Director", "Super Admin", "Accounts", "HR");
			
			 List<EmployeeDTO> employeesWithoutProjects = new ArrayList<>();
			
			 if (role.equalsIgnoreCase("SuperAdmin") || name.equalsIgnoreCase("Director")
			            || name.equalsIgnoreCase("Super Admin") || role.equalsIgnoreCase("Accounts")
			            || specialDepartments.contains(departmentName)) {
				employeesWithoutProjects = employeeRepository.findAllEmployeesWithoutAnyProject();
			}else if(departmentRepository.existsByHodId(projectFilterDTO.getCurrentUserEmpId())) {
				List<Long> deptIds = departmentRepository.findDeptIdsByHodId(projectFilterDTO.getCurrentUserEmpId());
				employeesWithoutProjects = employeeRepository.findAllEmployeesWithoutProjectInDeptIds(deptIds);
			}else {
				Employee employeee = employeeRepository.findByEmpId(projectFilterDTO.getCurrentUserEmpId());
				Long deptId = departmentRepository.findDepartmentIdOfSpoc(employeee.getJobRoleId());
				employeesWithoutProjects = employeeRepository.findAllEmployeesWithoutProjectInDeptId(deptId);
			}
			
			
//			 List<EmployeeDTO> employeeDTOList = new ArrayList<>();
//			 for (Object[] row : employeesWithoutProjects) {
//		            EmployeeDTO employeeDTO = new EmployeeDTO();
//		            employeeDTO.setEmpId(row[0] != null ? Long.parseLong(row[0].toString()) : null);
//		            employeeDTO.setEmployeementId( row[1] != null ? Long.parseLong(row[1].toString()) : null);
//		            employeeDTO.setEmail(row[2] != null ? row[2].toString() : null);
//		            employeeDTO.setEmploymentstatus(row[3] != null ? row[3].toString() : null);
//		            employeeDTO.setMobileNo(row[4] != null ? Long.parseLong(row[4].toString()) : null);
//		            employeeDTO.setManagerId(row[5] != null ? Long.parseLong(row[5].toString()) : null);
//		            employeeDTO.setManagerName(row[6] != null ? row[6].toString() : null);
//		            employeeDTO.setJobRoleName(row[7] != null ? row[7].toString() : null);
//		            employeeDTO.setDepartmentName(row[8] != null ? row[8].toString() : null); 
//		            employeeDTO.setName(row[9] != null ? row[9].toString() : null);
//		            employeeDTO.setBillableType(row[10] != null ? row[10].toString() : null);            
//		            employeeDTOList.add(employeeDTO);
//		        }

		        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		        response.setServiceResponse(employeesWithoutProjects);
		     

		    } catch (Exception e) {
		    	e.printStackTrace();
		    	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		        response.setServiceResponse("Error: " + e.getMessage());
		    }
		    return response; 
		}
	
	
	public ServiceResponse getEmployessWithoutBillable(ProjectFilterDTO projectFilterDTO) {
		ServiceResponse response = new ServiceResponse();
		
		try {
			Long empIdd = projectFilterDTO.getCurrentUserEmpId();
		    Employee employee = employeeRepository.findByEmpId(empIdd);
		    JobRole jobRole = jobRoleRepository.findByjobRoleId(employee.getJobRoleId());
		    Department department = departmentRepository.findByDeptId(jobRole.getDeptId());

		    String departmentName = department.getName();
		    String role = jobRole.getEmployeeRole();
		    String name = jobRole.getName();

		    Set<String> specialDepartments = Set.of("Admin", "Resource Management Group", "Director", "Super Admin", "Accounts", "HR");
			
			 List<EmployeeDTO> employeesWithoutBillable = new ArrayList<>();
			
			 if (role.equalsIgnoreCase("SuperAdmin") || name.equalsIgnoreCase("Director")
			            || name.equalsIgnoreCase("Super Admin") || role.equalsIgnoreCase("Accounts")
			            || specialDepartments.contains(departmentName)) {
				 employeesWithoutBillable = employeeRepository.findAllEmployeesWithoutAnyBillable();
			}else if(departmentRepository.existsByHodId(projectFilterDTO.getCurrentUserEmpId())) {
				List<Long> deptIds = departmentRepository.findDeptIdsByHodId(projectFilterDTO.getCurrentUserEmpId());
				employeesWithoutBillable = employeeRepository.findAllEmployeesWithoutAnyBillableInDeptIds(deptIds);
			}else {
				Employee employeee = employeeRepository.findByEmpId(projectFilterDTO.getCurrentUserEmpId());
				Long deptId = departmentRepository.findDepartmentIdOfSpoc(employeee.getJobRoleId());
				employeesWithoutBillable = employeeRepository.findAllEmployeesWithoutBillableInDeptId(deptId);
			}
			
			
//			 List<EmployeeDTO> employeeDTOList = new ArrayList<>();
//			 for (Object[] row : employeesWithoutBillable) {
//		            EmployeeDTO employeeDTO = new EmployeeDTO();
//		            employeeDTO.setEmpId(row[0] != null ? Long.parseLong(row[0].toString()) : null);
//		            employeeDTO.setEmployeementId( row[1] != null ? Long.parseLong(row[1].toString()) : null);
//		            employeeDTO.setEmail(row[2] != null ? row[2].toString() : null);
//		            employeeDTO.setEmploymentstatus(row[3] != null ? row[3].toString() : null);
//		            employeeDTO.setMobileNo(row[4] != null ? Long.parseLong(row[4].toString()) : null);
//		            employeeDTO.setManagerId(row[5] != null ? Long.parseLong(row[5].toString()) : null);
//		            employeeDTO.setManagerName(row[6] != null ? row[6].toString() : null);
//		            employeeDTO.setJobRoleName(row[7] != null ? row[7].toString() : null);
//		            employeeDTO.setDepartmentName(row[8] != null ? row[8].toString() : null); 
//		            employeeDTO.setName(row[9] != null ? row[9].toString() : null);
//		            employeeDTO.setBillableType(row[10] != null ? row[10].toString() : null);            
//		            employeeDTOList.add(employeeDTO);
//		        }

		        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		        response.setServiceResponse(employeesWithoutBillable);
		     

		    } catch (Exception e) {
		    	e.printStackTrace();
		    	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		        response.setServiceResponse("Error: " + e.getMessage());
		    }
		    return response; 
		}
	
	public ServiceResponse getEmployessWithoutProjectsDepartmentWise(GetEmployeeProjectReportPayloadDTO getEmployeeProjectReportPayloadDTO) {
		ServiceResponse response = new ServiceResponse();
		
		try {
			List<Long> departmentIds = getEmployeeProjectReportPayloadDTO.getDeptId();	
			 List<Object[]> employeesWithoutProjects = new ArrayList<>();
			 
			 employeesWithoutProjects = employeeRepository.findAllEmployeesWithoutAnyProjectDepartmentWise(departmentIds);	
			 List<EmployeeDTO> employeeDTOList = new ArrayList<>();
			 for (Object[] row : employeesWithoutProjects) {
		            EmployeeDTO employeeDTO = new EmployeeDTO();
		            employeeDTO.setEmpId(row[0] != null ? Long.parseLong(row[0].toString()) : null);
		            employeeDTO.setEmployeementId( row[1] != null ? Long.parseLong(row[1].toString()) : null);
		            employeeDTO.setEmail(row[2] != null ? row[2].toString() : null);
		            employeeDTO.setEmploymentstatus(row[3] != null ? row[3].toString() : null);
		            employeeDTO.setMobileNo(row[4] != null ? Long.parseLong(row[4].toString()) : null);
		            employeeDTO.setManagerId(row[5] != null ? Long.parseLong(row[5].toString()) : null);
		            employeeDTO.setManagerName(row[6] != null ? row[6].toString() : null);
		            employeeDTO.setJobRoleName(row[7] != null ? row[7].toString() : null);
		            employeeDTO.setDepartmentName(row[8] != null ? row[8].toString() : null); 
		            employeeDTO.setName(row[9] != null ? row[9].toString() : null); 
		            employeeDTO.setBillableType(row[10] != null ? row[10].toString() : null);            
		            employeeDTOList.add(employeeDTO);
		        }

		        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		        response.setServiceResponse(employeeDTOList);
		     

		    } catch (Exception e) {
		        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		        response.setServiceResponse("Error: " + e.getMessage());
		    }
		    return response;
		}
	
	
	

	public Set<Integer> filterProjectidsApprovalStatusDepartmentFilter(Set<Integer> projectIds, String approvalStatus,
			List<Long> departmentsids) {

		Set<Integer> filteredProjectIds = new HashSet<>();

		try {
			List<ProjectFetchDTO> allProjectList = projectRepository
					.findAllProjectByIsDraftAndIsActiveOfProjectIds(projectIds);

			if (!allProjectList.isEmpty()) {
				List<ResourceManagementDTO> dtoList = new ArrayList<>();

				for (ProjectFetchDTO object : allProjectList) {
					Integer projectId = object.getProjectId() != null ? object.getProjectId() : null;
					String isDraftProject = object.getIsDraftProject() != null ? object.getIsDraftProject() : null;
					Long isActive = object.getIsActive()!= null ? object.getIsActive().longValue() : null;
					String projectStatus = object.getProjectStatus() != null ? object.getProjectStatus() : null;
					
					boolean matchesApproval = false;

					if ("Pending For Approval".equalsIgnoreCase(approvalStatus) && Long.valueOf(2).equals(isActive)) {
						matchesApproval = true;
					} else if ("Approved".equalsIgnoreCase(approvalStatus)
							&& "false".equalsIgnoreCase(isDraftProject)) {
						matchesApproval = true;
					} else if ("Rejected".equalsIgnoreCase(approvalStatus)
							&& "Rejected".equalsIgnoreCase(isDraftProject)) {
						matchesApproval = true;
					} else if ("Completed".equalsIgnoreCase(approvalStatus)
							&& "Completed".equalsIgnoreCase(isDraftProject)) {
						matchesApproval = true;
					} else if ("CompletedInIshine".equalsIgnoreCase(approvalStatus)
							&& "Completed".equalsIgnoreCase(projectStatus)) {
						matchesApproval = true;
					}else if ("All".equalsIgnoreCase(approvalStatus)) {
						matchesApproval = true;
					}

					if (!matchesApproval)
						continue;

					List<TeamSpocDTO> spocList = getTeamsByProjectId(projectId);

					boolean matchesDepartment = true;
					if (departmentsids != null && !departmentsids.isEmpty()) {
						matchesDepartment = spocList.stream().anyMatch(
								spoc -> spoc.getDepartmentList() != null && Arrays.stream(spoc.getDepartmentList())
										.map(Long::valueOf).anyMatch(departmentsids::contains));
					}

					if (matchesDepartment) {
						filteredProjectIds.add(projectId);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return filteredProjectIds;
	}

	public ServiceResponse totalEmployeeCount() {
		ServiceResponse response = new ServiceResponse();
		try {
			Long employeeActiveCount = employeeRepository.getTotalEmployeeCount();
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(employeeActiveCount);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;

	}
	public ServiceResponse totalEmployeeCountInDepartments(GetEmployeeProjectReportPayloadDTO getEmployeeProjectReportPayloadDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Long> deptIds = getEmployeeProjectReportPayloadDTO.getDeptId();
			Long employeeActiveCount = employeeRepository.getTotalEmployeeCountInDepartments(deptIds);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(employeeActiveCount);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;

	}

	public ServiceResponse getResourceRequirementByPoProjectId(Long id) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getResourceRequirementByPoProjectId");
		apiLogInfo.setApiUrl("/api/getResourceRequirementByPoProjectId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();


		try {
			ServiceResponse projectApiResponse = poPortalAPIService.fetchPoPortalProjectById(id);
			if (projectApiResponse.getServiceStatus().equals(ServiceResponse.STATUS_FAIL)) {
		         return projectApiResponse;
			}
			List<ResourceRequirementResponse> project = (List<ResourceRequirementResponse>) projectApiResponse.getServiceResponse();
			System.out.println("======================"+project);

			if (project == null) {

				response.setServiceResponse("No Resource Requirement Found!");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				logBuilder.append("\n No Resource Requirement Found!");

				return response;

			} else {	
				
				ServiceResponse countApiResponse = poPortalAPIService.getCountByProjectId(id);
				if(countApiResponse.getServiceResponse()!= null){
	
				if (ServiceResponse.STATUS_FAIL.equals(countApiResponse.getServiceStatus())) {
		            throw new RuntimeException("Failed to fetch count from PO Portal." );      
		        }
				
				ResourceRequirementDTO requirementCountDto = new ResourceRequirementDTO();
				if(countApiResponse.getServiceResponse() != null) {
				requirementCountDto.setCount(Integer.parseInt(countApiResponse.getServiceResponse().toString()));}
				if (requirementCountDto == null) {
		             response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		             response.setServiceResponse("Received success status from PO Portal, but requirement data was null.");
		             return response;
		        }
				
				int totalRequirements = requirementCountDto.getCount();
				int assigned = projectRepository.getAssignedEmployeesCountInProject(id);
				int difference = totalRequirements - assigned;

				ProjectRequirementsDTO dto = new ProjectRequirementsDTO();
				dto.setTotalRequirements(totalRequirements);
				dto.setAssigned(assigned);
				dto.setDifference(difference);

				response.setServiceResponse(dto);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				logBuilder.append("\n Fetched project requirement details correctly!");

				return response;
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("\n Something went wrong!");
		}
		return response;
	}

	@Transactional(rollbackOn = Exception.class)
	public ServiceResponse setProjectManager(ResourceManagementDTO resourceManagementDTO, Project projectDbResponse) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("setProjectManager");
		apiLogInfo.setApiUrl("/api/setProjectManager");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("\n setProjectManager ");

		try {
			Project project = null;
			if (!resourceManagementDTO.getProjectManagerId().isEmpty()) {

				if (resourceManagementDTO.getProjectType().equals("Internal")) {
					project = projectRepository.findByProjectId(resourceManagementDTO.getProjectId());

				} else {
					project = projectRepository.findByPoProjectId(resourceManagementDTO.getId());
				}
//				Project project = projectRepository.findByPoProjectId(resourceManagementDTO.getId());

				List<ProjectManagerMapping> existingMappings = projectManagerMappingRepository
						.findByProjectId(Long.parseLong(project.getProjectId().toString()));

				List<Long> newManagerIds = resourceManagementDTO.getProjectManagerId();
				
				Set<Long> existingManagerIds = existingMappings.stream()
						.filter(mapping -> mapping.getActive() == 1)
						.map(ProjectManagerMapping::getProjectManagerId)
						.collect(Collectors.toSet());

				Set<Long> newManagerIdsSet = new HashSet<>(newManagerIds);

				// If no change then return
				if (existingManagerIds.equals(newManagerIdsSet)) {
					logBuilder.append("\n No changes detected in project managers.");
					response.setServiceResponse("No changes detected in project managers.");
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					return response;
				}

				// In case a existing project manager is deselected and sent
				existingMappings.forEach(existingMapping -> {
					if (!newManagerIds.contains(existingMapping.getProjectManagerId())) {
						existingMapping.setActive(0);
						existingMapping.setUpdatedBy(resourceManagementDTO.getUpdatedBy());
						existingMapping.setUpdatedOn(LocalDateTime.now());
						projectManagerMappingRepository.save(existingMapping);
					}
				});

				// In case project manager was present but made inactive then make active again
				// in the same row
				newManagerIds.forEach(managerId -> {
					ProjectManagerMapping existingMapping = projectManagerMappingRepository
							.findByProjectIdAndProjectManagerId(
									Long.parseLong(projectDbResponse.getProjectId().toString()), managerId);

					if (existingMapping == null) {
						ProjectManagerMapping newMapping = new ProjectManagerMapping();
						newMapping.setProjectId(Long.parseLong(projectDbResponse.getProjectId().toString()));
						newMapping.setProjectManagerId(managerId);
						newMapping.setActive(1);
						newMapping.setCreatedBy(resourceManagementDTO.getCreatedBy());
						newMapping.setCreatedOn(new Timestamp(System.currentTimeMillis()));
						projectManagerMappingRepository.save(newMapping);
					} else {
						existingMapping.setActive(1);
						existingMapping.setUpdatedBy(resourceManagementDTO.getCreatedBy());
						existingMapping.setUpdatedOn(LocalDateTime.now());
						projectManagerMappingRepository.save(existingMapping);
					}
				});
			}

			response.setServiceResponse("Project Manager saved successfully!");
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			logBuilder.append("\n Project Manager saved successfully!");

			return response;
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("\n Something went wrong!");
		}
		return response;
	}

	public ServiceResponse getEmployeeInformation(Long empId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getEmployeeInformation");
		apiLogInfo.setApiUrl("/api/getEmployeeInformation");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("\n getEmployeeInformation " + projectRepository.getEmployeeInformation(empId));

		try {
			List<Object[]> results = projectRepository.getEmployeeInformation(empId);
			if (results != null && !results.isEmpty()) {
				Object[] result = results.get(0);

				EmployeeInformationDTO dto = new EmployeeInformationDTO();
				dto.setEmpId(result[0] != null ? Long.parseLong(result[0].toString()) : null);
				dto.setEmploymentId(result[1] != null ? result[1].toString() : null);
				dto.setName(result[2] != null ? result[2].toString() : null);
				dto.setPreviousExperience(result[3] != null ? result[3].toString() : null);
				dto.setCurrentExperience(result[4] != null ? result[4].toString() : null);
				dto.setTotalExperience(result[5] != null ? result[5].toString() : null);
				dto.setBillableType(result[6] != null ? result[6].toString() : null);
				dto.setJobRole(result[7] != null ? result[7].toString() : null);
				dto.setDeptName(result[8] != null ? result[8].toString() : null);
				dto.setStartDate(result[9] != null ? result[9].toString() : null);
				

				response.setServiceResponse(dto);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No data found for empId: " + empId);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("\n Something went wrong!");
		}
		return response;
	}
	
	public ServiceResponse dumpPODataInIshine() {
		ServiceResponse serviceResponse = new ServiceResponse();
		 StringBuilder logBuilder = new StringBuilder();
		    LogDTO apiLogInfo = new LogDTO();
		    apiLogInfo.setLogLevel("INFO");
		ServiceResponse teamCreatedProjectsResponse = alreadyCreatedTeam();
		
		if(teamCreatedProjectsResponse == null) {
			throw new DataNotFoundException("Data not found");
		}
		
		if (!ServiceResponse.STATUS_SUCCESS.equals(teamCreatedProjectsResponse.getServiceStatus())) {
            return failResponse(serviceResponse, apiLogInfo, "Failed to fetch already created team projects.");
        }
		List<ResourceManagementDTO> teamCreatedProjects = castList(teamCreatedProjectsResponse.getServiceResponse());
		ServiceResponse apiResponse = poPortalAPIService.getAllProjectsFromPoPortal();
		List<ResourceManagementDTO> poPortalProjects;
        if(apiResponse.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS) ) {
        	 poPortalProjects = (List<ResourceManagementDTO>) apiResponse.getServiceResponse();
        }
        else
        {
        	poPortalProjects = new ArrayList<>();
        }
		System.out.println(poPortalProjects);
		processPoPortalProjects(poPortalProjects,teamCreatedProjects);
		System.out.println(poPortalProjects);
		for (ResourceManagementDTO poData :  poPortalProjects) {
			Project data = new Project();
			data.setPoNo(poData.getPoNo());
			data.setApmosysRM(poData.getApmosysRM());
			data.setApmosysRmEmail(poData.getApmosysRmEmail());	
			
			data.setActive(poData.getActive() != null ? poData.getActive().toString() : null);
			
		    Integer clientId = null;
			Optional<Client> clientObj = clientsRepository.findByClientName(poData.getClientName());
			if (!clientObj.isEmpty()) {
				Client clientPresent = clientObj.get();
				clientId = clientPresent.getClientId();
			} else {
				// Add Client & Client Location
				Client newClient = new Client();
				newClient.setClientName(poData.getClientName());
				Client clientDbResponse = clientsRepository.save(newClient);

				if (clientDbResponse != null) {
					clientId = clientDbResponse.getClientId();
					List<ClientLocation> locations = new ArrayList<>();

					for (String clientLocation : poData.getClientLocation()) {
						ClientLocation newClientLocation = new ClientLocation();
						newClientLocation.setClientId(clientDbResponse.getClientId());
						newClientLocation.setClientLocation(clientLocation);
						locations.add(newClientLocation);
					}

					// Add WFH location
					boolean contains = Arrays.stream(poData.getClientLocation())
							.anyMatch("WFH"::equals);
					if (!contains) {
						ClientLocation newClientLocation = new ClientLocation();
						newClientLocation.setClientId(clientDbResponse.getClientId());
						newClientLocation.setClientLocation("WFH");
						locations.add(newClientLocation);
					}

					List<ClientLocation> clientLocationDbResponse = clientLocationRepository.saveAll(locations);

					if (!clientLocationDbResponse.isEmpty()) {
						serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						serviceResponse.setServiceResponse("Client Location added");
						apiLogInfo.setApiResponse("Client Location added");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					} else {
						serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						serviceResponse.setServiceResponse("Failed to add client Location");
						apiLogInfo.setApiResponse("Failed to add client Location");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
						return serviceResponse;
					}
				} else {
					serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					serviceResponse.setServiceResponse("Failed to add client");
					apiLogInfo.setApiResponse("Failed to add client");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					return serviceResponse;
				}
			}
			

		    data.setClientId(clientId != null ? clientId : null);
		    data.setClientName(poData.getClientName()!=null ? poData.getClientName() : null );
		    if (poData.getClientLocation() != null) {
		        data.setClientLocation(String.join(", ", poData.getClientLocation()));
		    } else {
		        data.setClientLocation(null);
		    }
//		    data.setClientLocation(poData.getClientLocation() != null ? poData.getClientLocation() : null);
		    data.setProjectName(poData.getName() != null ? poData.getName() : null);    
		    data.setCreatedBy(poData.getCreatedBy());
		    String input = poData.getCreatedOn();
		    if (input != null) {
		        ZonedDateTime zdt = ZonedDateTime.parse(input);
		        LocalDateTime ldt = zdt.toLocalDateTime();
		        data.setCreatedOn(Timestamp.valueOf(ldt));
		    } else {
		        data.setCreatedOn(null);
		    }		    
		    data.setDepartmentName(poData.getDepartmentName());
		    data.setIsDraftProject(poData.getIsDraftProject());
		    data.setPoEndDate(poData.getEndDate());
		    data.setPoNo(poData.getPoNo());
		    data.setPoProjectId(poData.getId());
		    data.setPoProjectType(poData.getProjectType());
		    data.setPoStartDate(poData.getStartDate());
		    data.setProjectManagerId(poData.getProjectManagerId() != null && !poData.getProjectManagerId().isEmpty() ? poData.getProjectManagerId().get(0) : null);
		    data.setProjectName(poData.getName());
		    data.setRole(poData.getEmployeeRole());
		    data.setState(poData.getClientState());
		    data.setUpdatedBy(poData.getUpdatedBy());
		    data.setUpdatedOn(poData.getUpdatedOn() != null ? LocalDateTime.parse(poData.getUpdatedOn()) : null);
		    data.setApmosysRM(poData.getApmosysRM());
		    data.setClientRM(poData.getClientRM());
		    data.setDeptId(poData.getDeptId());
		    data.setIsRenewable(poData.getIsRenewable());
		    data.setStatus(poData.getStatus());
		    data.setApmosysRmEmail(poData.getApmosysRmEmail());
		    data.setProjectCompletionDate(poData.getProjectCompletionDate());
		    data.setProjectStatus(poData.getProjectStatus());
		    data.setProjectId(poData.getProjectId());
//		    data.setPrevPoNo(poData.getPrevPo()); 
//		    data.setNextPoNo(poData.getNextPo());
		    if (!poData.getResourceRequirements().isEmpty() && poData.getResourceRequirements() != null) {
		    	poData.getResourceRequirements().forEach(req -> {
					ResourceRequirementTemp resourceManagementDTO = new ResourceRequirementTemp();

					resourceManagementDTO.setCount(req.getCount());
					resourceManagementDTO.setDepartment(req.getDepartment());
					resourceManagementDTO.setExperience(req.getExperience());
					resourceManagementDTO.setRole(req.getRole());
					resourceManagementDTO.setResourceOverviewId(
							req.getResourceOverviewId() != null ? Long.parseLong(req.getResourceOverviewId().toString())
									: null);
					resourceManagementDTO.setPoProjectId(poData.getId());

					ResourceRequirementTemp res = resourceRequirementTempRepo.save(resourceManagementDTO);
				});
			}
		    
		    System.out.println(data);
//		    projectTempRepo.save(data);
		    }
		serviceResponse.setServiceResponse(poPortalProjects);
		serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		return serviceResponse;
	}
	
//	public ServiceResponse fillDepartmentforAllProjectsInIshine() {
//		ServiceResponse response = new ServiceResponse();
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setSubFeatureName("fillDepartmentforNotStartedProjects");
//		apiLogInfo.setApiUrl("/api/fillDepartmentforNotStartedProjects");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("\n fillDepartmentforNotStartedProjects ");
//		try {
//			
//			List<ResourceManagementDTO> poPortalProjects = Optional.ofNullable(fetchPoPortalProjects()).orElse(new ArrayList<>());
//			List<ResourceManagementDTO> poNotStartedProjects = projectRepository.getAllActivePOProjects();
//			
//			for (ResourceManagementDTO notStarted : poNotStartedProjects) {
//				Long poProjectId = notStarted.getPoProjectId();
//				Integer projectId = notStarted.getProjectId();
//
//				
//				for (ResourceManagementDTO poPortal : poPortalProjects) {
//					if (poProjectId != null && poProjectId.equals(poPortal.getId())) {
//						
//						List<String> departments = Arrays.asList(poPortal.getDepartment()); 
//						
//						if (departments != null && !departments.isEmpty()) {
//							for (String deptName : departments) {
//								Department dept = departmentRepository.findByName(deptName.trim());
//								if (dept != null) {
//									ProjectDepartmentMap map = new ProjectDepartmentMap();
//									map.setProjectId(projectId);
//									map.setDeptId(dept.getDeptId());
//									projectDepartmentMapRepository.save(map);
//								}
//							}
//						}
//						break; 
//					}
//				}
//			}
//
//			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//			response.setServiceResponse("Department mapping inserted successfully!");
//			
//			
//		}catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//			response.setServiceResponse("\n Something went wrong!");
//		}   
//		return response;
//	}
	
	public ServiceResponse fillDepartmentforAllProjectsInIshine() {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();

	    try {
	    	ServiceResponse apiResponse = poPortalAPIService.getAllProjectsFromPoPortal();
			List<ResourceManagementDTO> poPortalProjects;
			
			if(apiResponse == null) {
				throw new DataNotFoundException("Data not found");
			}
	        if(apiResponse.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS)) {
	        	 poPortalProjects = (List<ResourceManagementDTO>) apiResponse.getServiceResponse();
	        }
	        else
	        {
	        	poPortalProjects = new ArrayList<>();
	        }
	                
	        List<ResourceManagementDTO> activePOProjects = projectRepository.getAllActivePOProjects();

	        int insertCount = 0, updateCount = 0, deactivateCount = 0;

	        for (ResourceManagementDTO activeProject : activePOProjects) {
	            try {
	                Long poProjectId = activeProject.getPoProjectId();
	                Integer projectId = activeProject.getProjectId();

	                if (poProjectId == null || projectId == null) continue;

	                ResourceManagementDTO portalProject = poPortalProjects.stream()
	                        .filter(p -> poProjectId.equals(p.getId()))
	                        .findFirst()
	                        .orElse(null);

	                if (portalProject == null || portalProject.getDepartment() == null)
	                    continue;

	                List<String> newDeptNames = Arrays.asList(portalProject.getDepartment());
	                List<Long> newDeptIds = new ArrayList<>();

	                for (String deptName : newDeptNames) {
	                    try {
	                        if (deptName != null && !deptName.trim().isEmpty()) {
	                            Department dept = departmentRepository.findByName(deptName.trim());
	                            if (dept != null) {
	                                newDeptIds.add(dept.getDeptId());
	                            } else {
	                                apiLogInfo.setApiResponse("Department not found for name: '" + deptName + "' (Project ID: " + projectId + ")");
	                            }
	                        }
	                    } catch (Exception e) {
	                        apiLogInfo.setApiResponse("Error while finding department '" + deptName + "' for project ID: " + projectId);
	                        e.printStackTrace();
	                    }
	                }

	                List<ProjectDepartmentMap> existingMaps = projectDepartmentMapRepository.findByProjectId(projectId);
	                Map<Long, ProjectDepartmentMap> deptIdToMap = existingMaps.stream()
	                	    .filter(Objects::nonNull)
	                	    .filter(m -> {
	                	        boolean hasDept = m.getDeptId() != null;
	                	        if (!hasDept) {
	                	            System.out.println("Skipping map with null deptId: " + m);
	                	        }
	                	        return hasDept;
	                	    })
	                	    .peek(m -> System.out.println("Processing map with deptId: " + m.getDeptId()))
	                	    .collect(Collectors.toMap(
	                	        ProjectDepartmentMap::getDeptId,
	                	        map -> {
	                	            System.out.println("Putting in map: deptId = " + map.getDeptId() + ", map = " + map);
	                	            return map;
	                	        }
	                	    ));

	                Set<Long> newDeptIdSet = new HashSet<>(newDeptIds);

	                for (ProjectDepartmentMap map : existingMaps) {
	                    try {
	                        if (map != null && map.getDeptId() != null &&
	                                !newDeptIdSet.contains(map.getDeptId()) && !Objects.equals(map.getActive(), 0L)) {
	                            map.setActive(0L);
	                            deactivateCount++;
	                        }
	                    } catch (Exception e) {
	                        apiLogInfo.setApiResponse("Error while deactivating department mapping (Project ID: " + projectId + ")");
	                        e.printStackTrace();
	                    }
	                }

	                for (Long deptId : newDeptIds) {
	                    try {
	                        if (deptId != null) {
	                            if (deptIdToMap.containsKey(deptId)) {
	                                ProjectDepartmentMap existing = deptIdToMap.get(deptId);
	                                if (existing.getActive() == null || existing.getActive() == 0L) {
	                                    existing.setActive(1L);
	                                    updateCount++;
	                                }
	                            } else {
	                                ProjectDepartmentMap newMap = new ProjectDepartmentMap();
	                                newMap.setProjectId(projectId);
	                                newMap.setDeptId(deptId);
	                                newMap.setActive(1L);
	                                existingMaps.add(newMap);
	                                insertCount++;
	                            }
	                        }
	                    } catch (Exception e) {
	                        apiLogInfo.setApiResponse("Error inserting/updating mapping (Dept ID: " + deptId + ", Project ID: " + projectId + ")");
	                        e.printStackTrace();
	                    }
	                }

	                projectDepartmentMapRepository.saveAll(existingMaps);

	            } catch (Exception innerEx) {
	                apiLogInfo.setApiResponse("Error processing project with PoProjectId: " + activeProject.getPoProjectId()
	                        + ", ProjectId: " + activeProject.getProjectId());
	                innerEx.printStackTrace();
	            }
	        }

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        String finalMessage = "Department mapping synced successfully! Inserted: " + insertCount +
	                ", Updated: " + updateCount + ", Deactivated: " + deactivateCount;
	        response.setServiceResponse(finalMessage);
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        apiLogInfo.setApiResponse(finalMessage);

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse("Something went wrong while syncing departments.");
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setApiResponse("Exception occurred during department sync");
	    }

	    return response;
	}

	@Transactional(rollbackOn = Exception.class)
	private ServiceResponse setProjectOverheads(ResourceManagementDTO resourceManagementDTO, Project projectDbResponse) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("setProjectOverheads");
		apiLogInfo.setApiUrl("/api/setProjectOverheads");
		apiLogInfo.setLogLevel("INFO");

		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("\n setProjectOverheads ");

		try {
			if (resourceManagementDTO == null || projectDbResponse == null) {
				logBuilder.append("\n Invalid input: resourceManagementDTO or projectDbResponse is null");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Invalid input");
				return response;
			}

			if (resourceManagementDTO.getProjectManagerId() == null || resourceManagementDTO.getProjectManagerId().isEmpty()) {
				logBuilder.append("\n Project Manager ID is empty. Skipping overhead assignment.");
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("No project manager ID provided.");
				return response;
			}

			Project project;
			if ("Internal".equals(resourceManagementDTO.getProjectType())) {
				project = projectRepository.findByProjectId(resourceManagementDTO.getProjectId());
			} else {
				project = projectRepository.findByPoProjectId(resourceManagementDTO.getId());
			}

			if (project == null || project.getProjectId() == null) {
				logBuilder.append("\n Project not found.");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not found.");
				return response;
			}

			List<ProjectOverheadMapping> existingMappings = projectOverheadMappingRepository
					.findByProjectId(Long.parseLong(project.getProjectId().toString()));

			List<Long> newOverheadIds = resourceManagementDTO.getProjectOverheadId();
			Set<Long> newOverheadIdsSet = (newOverheadIds != null) ? new HashSet<>(newOverheadIds) : new HashSet<>();

			if (existingMappings != null && !existingMappings.isEmpty()) {
				Set<Long> existingOverheadIds = existingMappings.stream()
						.filter(mapping -> mapping.getActive() == 1)
						.map(ProjectOverheadMapping::getProjectOverheadId)
						.collect(Collectors.toSet());

				if (!existingOverheadIds.isEmpty() && existingOverheadIds.equals(newOverheadIdsSet)) {
					logBuilder.append("\n No changes detected in project overheads.");
					response.setServiceResponse("No changes detected in project overheads.");
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					return response;
				}

				// Deactivate removed overheads
				for (ProjectOverheadMapping existingMapping : existingMappings) {
					if (!newOverheadIdsSet.contains(existingMapping.getProjectOverheadId())) {
						existingMapping.setActive(0);
						existingMapping.setUpdatedBy(resourceManagementDTO.getUpdatedBy());
						existingMapping.setUpdatedOn(LocalDateTime.now());
						projectOverheadMappingRepository.save(existingMapping);
					}
				}
			}

			// Add or reactivate new overheads
			for (Long overheadId : newOverheadIdsSet) {
				if (overheadId == null) continue;

				ProjectOverheadMapping existingMapping = projectOverheadMappingRepository
						.findByProjectIdAndProjectOverheadId(Long.parseLong(projectDbResponse.getProjectId().toString()), overheadId);

				if (existingMapping == null) {
					ProjectOverheadMapping newMapping = new ProjectOverheadMapping();
					newMapping.setProjectId(Long.parseLong(projectDbResponse.getProjectId().toString()));
					newMapping.setProjectOverheadId(overheadId);
					newMapping.setActive(1);
					newMapping.setCreatedBy(resourceManagementDTO.getCreatedBy());
					newMapping.setCreatedOn(new Timestamp(System.currentTimeMillis()));
					projectOverheadMappingRepository.save(newMapping);
				} else {
					existingMapping.setActive(1);
					existingMapping.setUpdatedBy(resourceManagementDTO.getCreatedBy());
					existingMapping.setUpdatedOn(LocalDateTime.now());
					projectOverheadMappingRepository.save(existingMapping);
				}
			}

			response.setServiceResponse("Project Overhead saved successfully!");
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			logBuilder.append("\n Project Overhead saved successfully!");

		} catch (Exception e) {
			e.printStackTrace();
			logBuilder.append("\n Exception: ").append(e.getMessage());
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Something went wrong!");
		}

		return response;
	}
	
	public ServiceResponse setDefaultProjectUpdateBillable(DefaultProjectUpdateDTO defaultProjectUpdateDTO) {
		
		ServiceResponse response = new ServiceResponse();
		try {
			
			Long updatedBy = defaultProjectUpdateDTO.getUpdatedBy();
			Integer projectId = defaultProjectUpdateDTO.getProjectId();
			List<Long> empIds = defaultProjectUpdateDTO.getEmpIds();
			
			 if (empIds == null || empIds.isEmpty() || projectId == null) {
				 response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Invalid Emp Id");
		        }
			 
			  Map<Long, EmpPrimaryProjectMapping> existingMappings = empPrimaryProjectMappingRepository
		                .findByEmpIdIn(empIds)
		                .stream()
		                .collect(Collectors.toMap(EmpPrimaryProjectMapping::getEmpId, Function.identity()));
			  
			  Project project = projectRepository.findByProjectId(projectId);
			  if (project == null) {
				  response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Invalid Project");
		        }
			  
			 
			  Map<Long, Employee> employeeMap = employeeRepository.findByEmpIdIn(empIds).stream()
		                .collect(Collectors.toMap(Employee::getEmpId, Function.identity()));
			  
			  
			  List<Long> shadowEmpIds = employeeTeamMapRepository.findShadowMembersByEmpIdsAndProjectId(empIds, projectId);
			  
			  
			  String billableType;
		        String billable;
			  if ("TNM".equalsIgnoreCase(project.getPoProjectType())) {
		            billableType = "TNM";
		            billable = "Yes";
		        } else if ("Fixed cost".equalsIgnoreCase(project.getPoProjectType()) || "Fixed Cost".equalsIgnoreCase(project.getPoProjectType())) {
		            billableType = "Fixed Cost";
		            billable = "No";
		        } else if ("Bench".equalsIgnoreCase(project.getInternalProjectType())) {
		            billableType = "Bench";
		            billable = "No";
		        } else if ("InternalRNDProducts".equalsIgnoreCase(project.getInternalProjectType())) {
		            billableType = "InternalRNDProducts";
		            billable = "No";
		        } else {
		            billableType = null;
		            billable = null;
		        }
			  
			  
			  LocalDateTime now = LocalDateTime.now();
		        List<EmpPrimaryProjectMapping> mappingsToUpdate = new ArrayList<>();
		        List<Long> empIdsToUpdateBillable = new ArrayList<>();
		        Map<Long, String> empIdToBillable = new HashMap<>();
		        Map<Long, String> empIdToBillableType = new HashMap<>();

		        for (Long empId : empIds) {
		            EmpPrimaryProjectMapping existing = existingMappings.get(empId);
		            boolean isNewMapping = false;
		            Long projectIdLong = Long.valueOf(projectId);

		            if (existing != null && !existing.getPrimaryProjectId().equals(projectId)) {
		                existing.setPrimaryProjectId(projectIdLong);
		                existing.setPrimaryProjectName(project.getProjectName());
		                existing.setIsMapped("Y");
		                existing.setUpdatedBy(updatedBy);
		                existing.setUpdatedOn(now);
		                mappingsToUpdate.add(existing);
		            } else if (existing == null) {
		                EmpPrimaryProjectMapping newMapping = new EmpPrimaryProjectMapping();
		                newMapping.setEmpId(empId);
		                newMapping.setPrimaryProjectId(projectIdLong);
		                newMapping.setPrimaryProjectName(project.getProjectName());
		                newMapping.setIsMapped("Y");
		                newMapping.setUpdatedBy(updatedBy);
		                newMapping.setUpdatedOn(now);
		                mappingsToUpdate.add(newMapping);
		            }

		           
		            String finalBillableType = shadowEmpIds.contains(empId) ? "Shadow" : billableType;
		            String finalBillable = "Shadow".equals(finalBillableType) ? "No" : billable;

		            Employee emp = employeeMap.get(empId);
		            if (emp == null ||
		                !Objects.equals(emp.getBillable(), finalBillable) ||
		                !Objects.equals(emp.getBillableType(), finalBillableType)) {
		                empIdsToUpdateBillable.add(empId);
		                empIdToBillable.put(empId, finalBillable);
		                empIdToBillableType.put(empId, finalBillableType);
		            }
		        }

		      
		        if (!mappingsToUpdate.isEmpty()) {
		            empPrimaryProjectMappingRepository.saveAll(mappingsToUpdate);
		        }

		     
		        for (Long empId : empIdsToUpdateBillable) {
		            employeeRepository.updateBillableFields(empId, empIdToBillable.get(empId), empIdToBillableType.get(empId));
		        }

		        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		        response.setServiceResponse("Updated " + empIds.size() + " employee(s) successfully.");
		    } catch (Exception e) {
		        e.printStackTrace();
		        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		        response.setServiceResponse("Error occurred while updating employees.");
		        response.setServiceError(e.getMessage());
		    }
		    return response;
		}
	
	
	public ServiceResponse getAllExceptionReport(ProjectFilterDTO projectFilterDTO) {
	    ServiceResponse response = new ServiceResponse();
	    try {
	    	
	    	Employee employee = employeeRepository.findByEmpId(projectFilterDTO.getCurrentUserEmpId());
			JobRole jobRole = jobRoleRepository.findByjobRoleId(employee.getJobRoleId());

			String role = jobRole.getEmployeeRole();
			String name = jobRole.getName();
	        List<RMGFlatEmployeeProjectTeamDTO> result = new ArrayList<>();
	        
	        if (role.equalsIgnoreCase("SuperAdmin") || name.equalsIgnoreCase("Director")
					|| name.equalsIgnoreCase("Super Admin")) {
	        	result=	projectRepository.getExceptionEmployeeReport();
			}else if(departmentRepository.existsByHodId(projectFilterDTO.getCurrentUserEmpId())) {
				List<Long> deptIds = departmentRepository.findDeptIdsByHodId(projectFilterDTO.getCurrentUserEmpId());
				result=	projectRepository.getExceptionEmployeeReportInDepartments(deptIds);
			}else if(teamRepository.existsBySpocId(projectFilterDTO.getCurrentUserEmpId())){
				Employee employeee = employeeRepository.findByEmpId(projectFilterDTO.getCurrentUserEmpId());
				Long deptId = departmentRepository.findDepartmentIdOfSpoc(employeee.getJobRoleId());
				result=	projectRepository.getExceptionEmployeeReportInDepartment(deptId);
			}else {
				result=	projectRepository.getExceptionEmployeeReport();
			}
	        Map<String, ExceptionReportDTO> dtoMap = new LinkedHashMap<>();
	        for (RMGFlatEmployeeProjectTeamDTO obj : result) {
	            String employmentId = obj.getEmployeementId() != null ? obj.getEmployeementId().toString() : null;

	            // Create or fetch existing DTO
	            ExceptionReportDTO dto = dtoMap.computeIfAbsent(employmentId, id -> {
	                ExceptionReportDTO newDto = new ExceptionReportDTO();
	                newDto.setEmploymentId(id);
	                newDto.setEmployeeName(obj.getName());
	                newDto.setDepartment(obj.getDepartment());
	                newDto.setBillableType(obj.getBillableType());
	                newDto.setRmgProjects(new ArrayList<>());
	                return newDto;
	            });

	            // Now construct the RMGProject
	            RMGProject project = new RMGProject();
	            project.setProjectId(obj.getProjectId()!= null ? Integer.parseInt(obj.getProjectId().toString()) : null);
	            project.setProjectName(obj.getProjectName());
	            project.setClientName(obj.getClientName());
	            project.setApmosysRM(obj.getApmosysRM());
	            project.setClientRM(obj.getClientRM());
	            project.setPoNo(obj.getPoNo());
	            project.setPoProjectType(obj.getPoProjectType());
	            project.setPoStartDate(obj.getPoStartDate());
	            project.setPoEndDate(obj.getPoEndDate());

	            dto.getRmgProjects().add(project);
	        }

	       
	        List<ExceptionReportDTO> dtoList = new ArrayList<>(dtoMap.values());

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(dtoList);
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceError("Error while fetching report: " + e.getMessage());
	    }
	    return response;
	}
	
	public ServiceResponse combinedDataCount(ProjectFilterDTO projectFilterDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");

		try {
			if (projectFilterDTO == null) {
				return failResponse(response, apiLogInfo, "Invalid input: ProjectFilterDTO is null.");
			}

			if (Boolean.FALSE.equals(projectFilterDTO.getIsHod()) && Boolean.FALSE.equals(projectFilterDTO.getIsAdmin())
					&& Boolean.FALSE.equals(projectFilterDTO.getIsOther())) {
				return failResponse(response, apiLogInfo, "Invalid input: All role flags are false.");
			}
			if((Boolean.TRUE.equals(projectFilterDTO.getIsHod())&&Boolean.TRUE.equals(projectFilterDTO.getIsAdmin())) ||
					Boolean.TRUE.equals(projectFilterDTO.getIsAdmin()) && Boolean.TRUE.equals(projectFilterDTO.getIsOther())||
					Boolean.TRUE.equals(projectFilterDTO.getIsHod())&&Boolean.TRUE.equals(projectFilterDTO.getIsOther())) {
				return failResponse(response, apiLogInfo, "Invalid input: More than one flag is true");
			}

			Map<String, Integer> map = new HashMap<>();
			List<Long> deptIdList = new ArrayList<>();
			Set<Integer> projectIdSet = new HashSet<>();
			List<Integer> projectIdListTemp;
			Integer pendingForApprovalCount,approvedCount,notStartedCount,rejectedCount,totalCount,completedInIshine;
			CombinedPOInternalProjectResponse responseData = new CombinedPOInternalProjectResponse();
			if ("All".equalsIgnoreCase(projectFilterDTO.getApprovalStatus()))
				projectFilterDTO.setApprovalStatus(null);
			if (Boolean.TRUE.equals(projectFilterDTO.getIsAdmin())) {

				if(!projectFilterDTO.getDepartmentsids().isEmpty() && projectFilterDTO.getDepartmentsids() != null ) {
					List<Long> selectedDeptList= projectFilterDTO.getDepartmentsids();
					Set<Integer> matchingProjectIds = teamRepository.findAll().stream()
						    .filter(team -> {
						        if (team.getDeptIds() == null) return false;
						        List<String> teamDeptIds = Arrays.asList(team.getDeptIds().split(","));
						        return selectedDeptList.stream()
						                .map(String::valueOf)
						                .anyMatch(teamDeptIds::contains);
						    })
						    .map(Team::getProjectId)
						    .filter(Objects::nonNull)
						    .collect(Collectors.toSet());
					projectIdSet = matchingProjectIds;
					pendingForApprovalCount = projectRepository.getAllActiveProjecCountstList("true", projectIdSet, true);
					approvedCount = projectRepository.getAllActiveProjecCountstList("false", projectIdSet, true);
					notStartedCount = projectRepository.getAllNotStartedProjectCountInDept(selectedDeptList);
					rejectedCount = projectRepository.getAllActiveProjecCountstList( "Rejected", projectIdSet, true);
					completedInIshine = projectRepository.getAllCompletedProjectCountInIshine(selectedDeptList);
					totalCount = pendingForApprovalCount + approvedCount + rejectedCount;
					map.put("pendingForApprovalCount",pendingForApprovalCount);
					map.put("approvedCount",approvedCount);
					map.put("completedCount",
							projectRepository.getAllCompleteProjectInShankhCountstList( projectIdSet, true));
					map.put("completedWithEmployeeCount", projectRepository
							.completedInSankhButTeamMapped(projectIdSet, true));
					map.put("notStartedCount",notStartedCount);
					map.put("rejectedCount",rejectedCount);
					map.put("completedInIshineCount", completedInIshine);
					map.put("totalCount", totalCount);
				}else {
					List<Long> deptIdsAccToRole = departmentRepository.findAllDepartments() ;
					pendingForApprovalCount = projectRepository.getAllActiveProjecCountstList( "true", null, false);
					approvedCount = projectRepository.getAllActiveProjecCountstList( "false", null, false);
					notStartedCount =  projectRepository.getAllNotStartedProjectCountInDept(deptIdsAccToRole);
					completedInIshine = projectRepository.getAllCompletedProjectCountInIshine(deptIdsAccToRole);
					rejectedCount = projectRepository
							.getAllActiveProjecCountstList("Rejected", null, false);
					totalCount = pendingForApprovalCount + approvedCount + rejectedCount;
					map.put("pendingForApprovalCount",pendingForApprovalCount);
				map.put("approvedCount",approvedCount);
				map.put("completedCount",
						projectRepository.getAllCompleteProjectInShankhCountstList( null, false));
				map.put("completedWithEmployeeCount", projectRepository
						.completedInSankhButTeamMapped( null,false));
				map.put("notStartedCount",notStartedCount);
				map.put("rejectedCount",rejectedCount );
				map.put("completedInIshineCount", completedInIshine);
				map.put("totalCount", totalCount);
				}
				
				responseData.setCounts(map);
			}

			else if (Boolean.TRUE.equals(projectFilterDTO.getIsHod())) {
				List<Department> deptData = departmentRepository.findByHodId(projectFilterDTO.getCurrentUserEmpId());
				if (deptData != null) {
					for (Department data : deptData) {
						if (data != null && data.getDeptId() != null) {
							deptIdList.add(data.getDeptId());
						}
					}
				}
				
				Set<Integer> matchingProjectIds = teamRepository.findAll().stream()
					    .filter(team -> {
					        if (team.getDeptIds() == null) return false;
					        List<String> teamDeptIds = Arrays.asList(team.getDeptIds().split(","));
					        return deptIdList.stream()
					                .map(String::valueOf)
					                .anyMatch(teamDeptIds::contains);
					    })
					    .map(Team::getProjectId)
					    .filter(Objects::nonNull)
					    .collect(Collectors.toSet());
				
				if (projectManagerMappingRepository.isUserProjectManagerOfAnyActiveInternalAndExternalProject(
						projectFilterDTO.getCurrentUserEmpId())) {
					projectIdListTemp = projectManagerMappingRepository
							.isUserProjectManagerOfAnyActiveInternalProjectList(projectFilterDTO.getCurrentUserEmpId());
					if (projectIdListTemp != null)
						projectIdSet.addAll(projectIdListTemp);
				}

				if (projectOverheadMappingRepository.isUserProjectOverheadOfAnyActiveInternalAndExternalProject(
						projectFilterDTO.getCurrentUserEmpId())) {
					projectIdListTemp = projectOverheadMappingRepository
							.isUserProjectOverheadOfAnyActiveInternalAndExternalProjectList(
									projectFilterDTO.getCurrentUserEmpId());
					if (projectIdListTemp != null)
						projectIdSet.addAll(projectIdListTemp);
				}

				if (teamRepository.existsBySpocId(projectFilterDTO.getCurrentUserEmpId())) {
					projectIdListTemp = teamRepository
							.findActiveShankhInternalProjectIdsBySpocIdList(projectFilterDTO.getCurrentUserEmpId());
					if (projectIdListTemp != null)
						projectIdSet.addAll(projectIdListTemp);
						projectIdSet.addAll(matchingProjectIds);
				}
				
				if(!projectFilterDTO.getDepartmentsids().isEmpty() && projectFilterDTO.getDepartmentsids() != null ) {
					List<Long> selectedDeptList= projectFilterDTO.getDepartmentsids();
					Set<Integer> matchingProjIds = teamRepository.findAll().stream()
						    .filter(team -> {
						        if (team.getDeptIds() == null) return false;
						        List<String> teamDeptIds = Arrays.asList(team.getDeptIds().split(","));
						        return selectedDeptList.stream()
						                .map(String::valueOf)
						                .anyMatch(teamDeptIds::contains);
						    })
						    .map(Team::getProjectId)
						    .filter(Objects::nonNull)
						    .collect(Collectors.toSet());
					
					projectIdSet.retainAll(matchingProjIds);
					
					pendingForApprovalCount = projectRepository.getAllActiveProjecCountstList("true", projectIdSet, true);
					approvedCount = projectRepository.getAllActiveProjecCountstList("false", projectIdSet, true);
					notStartedCount =  projectRepository.getAllNotStartedProjectCountInDept(selectedDeptList);
					rejectedCount = projectRepository.getAllActiveProjecCountstList( "Rejected", projectIdSet, true);
					completedInIshine = projectRepository.getAllCompletedProjectCountInIshine(selectedDeptList);
					totalCount = pendingForApprovalCount + approvedCount + rejectedCount;
					
					map.put("pendingForApprovalCount",pendingForApprovalCount);
					map.put("approvedCount",approvedCount);
					map.put("completedCount",
							projectRepository.getAllCompleteProjectInShankhCountstList( projectIdSet, true));
					map.put("completedWithEmployeeCount", projectRepository
							.completedInSankhButTeamMapped(projectIdSet, true));
					map.put("notStartedCount",notStartedCount );
					map.put("rejectedCount",rejectedCount );
					map.put("completedInIshineCount",completedInIshine);
					map.put("totalCount", totalCount);
				}else {
					
					pendingForApprovalCount = projectRepository.getAllActiveProjecCountstList("true", projectIdSet, true);
					approvedCount = projectRepository.getAllActiveProjecCountstList("false", projectIdSet, true);
					notStartedCount =  projectRepository.getAllNotStartedProjectCountInDept(deptIdList);
					completedInIshine = projectRepository.getAllCompletedProjectCountInIshine(deptIdList);
					rejectedCount = projectRepository.getAllActiveProjecCountstList( "Rejected", projectIdSet, true);
					totalCount = pendingForApprovalCount + approvedCount + rejectedCount;
					
					map.put("pendingForApprovalCount",pendingForApprovalCount);
					map.put("approvedCount",approvedCount);
					map.put("completedCount",
							projectRepository.getAllCompleteProjectInShankhCountstList( projectIdSet, true));
					map.put("completedWithEmployeeCount", projectRepository
							.completedInSankhButTeamMapped(projectIdSet, true));
					map.put("notStartedCount",notStartedCount );
					map.put("rejectedCount",rejectedCount );
					map.put("completedInIshineCount", completedInIshine);
					map.put("totalCount", totalCount);
				}
				
				
				responseData.setCounts(map);
			}

			else if (Boolean.TRUE.equals(projectFilterDTO.getIsOther())) {

				if (projectManagerMappingRepository.isUserProjectManagerOfAnyActiveInternalAndExternalProject(
						projectFilterDTO.getCurrentUserEmpId())) {
					projectIdListTemp = projectManagerMappingRepository
							.isUserProjectManagerOfAnyActiveInternalProjectList(projectFilterDTO.getCurrentUserEmpId());
					if (projectIdListTemp != null)
						projectIdSet.addAll(projectIdListTemp);
				}

				if (projectOverheadMappingRepository.isUserProjectOverheadOfAnyActiveInternalAndExternalProject(
						projectFilterDTO.getCurrentUserEmpId())) {
					projectIdListTemp = projectOverheadMappingRepository
							.isUserProjectOverheadOfAnyActiveInternalAndExternalProjectList(
									projectFilterDTO.getCurrentUserEmpId());
					if (projectIdListTemp != null)
						projectIdSet.addAll(projectIdListTemp);
				}

				if (teamRepository.existsBySpocId(projectFilterDTO.getCurrentUserEmpId())) {
					projectIdListTemp = teamRepository
							.findActiveShankhInternalProjectIdsBySpocIdList(projectFilterDTO.getCurrentUserEmpId());
					if (projectIdListTemp != null)
						projectIdSet.addAll(projectIdListTemp);
				}

				
				if (projectIdSet != null && !projectIdSet.isEmpty()){
					if(!projectFilterDTO.getDepartmentsids().isEmpty() && projectFilterDTO.getDepartmentsids() != null ) {
					List<Long> selectedDeptList= projectFilterDTO.getDepartmentsids();
					Set<Integer> matchingProjectIds = teamRepository.findAll().stream()
						    .filter(team -> {
						        if (team.getDeptIds() == null) return false;
						        List<String> teamDeptIds = Arrays.asList(team.getDeptIds().split(","));
						        return selectedDeptList.stream()
						                .map(String::valueOf)
						                .anyMatch(teamDeptIds::contains);
						    })
						    .map(Team::getProjectId)
						    .filter(Objects::nonNull)
						    .collect(Collectors.toSet());
					
					projectIdSet.retainAll(matchingProjectIds);
					
					pendingForApprovalCount = projectRepository.getAllActiveProjecCountstList("true", projectIdSet, true);
					approvedCount = projectRepository.getAllActiveProjecCountstList("false", projectIdSet, true);
					notStartedCount =  projectRepository.getAllNotStartedProjectCountInDept(selectedDeptList);
					completedInIshine = projectRepository.getAllCompletedProjectCountInIshine(selectedDeptList);
					rejectedCount = projectRepository.getAllActiveProjecCountstList( "Rejected", projectIdSet, true);
					totalCount = pendingForApprovalCount + approvedCount  + rejectedCount;
					
					map.put("pendingForApprovalCount",pendingForApprovalCount);
					map.put("approvedCount",approvedCount);
					map.put("completedCount",projectRepository.getAllCompleteProjectInShankhCountstList( projectIdSet, true));
					map.put("completedWithEmployeeCount", projectRepository.completedInSankhButTeamMapped(projectIdSet, true));
					map.put("notStartedCount", notStartedCount);
					map.put("rejectedCount", rejectedCount);
					map.put("completedInIshineCount",completedInIshine);
					map.put("totalCount", totalCount);
					}else {
						Employee employeee = employeeRepository.findByEmpId(projectFilterDTO.getCurrentUserEmpId());
						List<Long> deptIdOfOther = departmentRepository.findDepartmentIdOfCurrentUser(employeee.getJobRoleId());
						
						pendingForApprovalCount = projectRepository.getAllActiveProjecCountstList("true", projectIdSet, true);
						approvedCount = projectRepository.getAllActiveProjecCountstList("false", projectIdSet, true);
						notStartedCount =  projectRepository.getAllNotStartedProjectCountInDept(deptIdOfOther);
						completedInIshine = projectRepository.getAllCompletedProjectCountInIshine(deptIdOfOther);
						rejectedCount = projectRepository.getAllActiveProjecCountstList( "Rejected", projectIdSet, true);
						totalCount = pendingForApprovalCount + approvedCount + notStartedCount + rejectedCount;
						
						map.put("pendingForApprovalCount",pendingForApprovalCount);
						map.put("approvedCount",approvedCount);
						map.put("completedCount",projectRepository.getAllCompleteProjectInShankhCountstList( projectIdSet, true));
						map.put("completedWithEmployeeCount", projectRepository.completedInSankhButTeamMapped(projectIdSet, true));
						map.put("notStartedCount", notStartedCount);
						map.put("rejectedCount", rejectedCount);
						map.put("completedInIshineCount",completedInIshine);
						map.put("totalCount", totalCount);
					}
					
					
					responseData.setCounts(map);
				} else {
					responseData = null;
				}
			}

			if (responseData != null) {
				response.setServiceResponse(responseData);
				response.setServiceStatus(response.STATUS_SUCCESS);
			} else {
				response.setServiceResponse("User is not qualified to see any data");
				response.setServiceStatus(response.STATUS_FAIL);
			}
			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return failResponse(response, apiLogInfo, "Internal Server Error: " + e.getMessage());
		}
	}

		
	public ServiceResponse combinedDataList(ProjectFilterDTO projectFilterDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");

		try {
			if (projectFilterDTO == null) {
				return failResponse(response, apiLogInfo, "Invalid input: ProjectFilterDTO is null.");
			}

			if (Boolean.FALSE.equals(projectFilterDTO.getIsHod()) && Boolean.FALSE.equals(projectFilterDTO.getIsAdmin())
					&& Boolean.FALSE.equals(projectFilterDTO.getIsOther())) {
				return failResponse(response, apiLogInfo, "Invalid input: All role flags are false.");
			}
			if((Boolean.TRUE.equals(projectFilterDTO.getIsHod())&&Boolean.TRUE.equals(projectFilterDTO.getIsAdmin())) ||
					Boolean.TRUE.equals(projectFilterDTO.getIsAdmin()) && Boolean.TRUE.equals(projectFilterDTO.getIsOther())||
					Boolean.TRUE.equals(projectFilterDTO.getIsHod())&&Boolean.TRUE.equals(projectFilterDTO.getIsOther())) {
				return failResponse(response, apiLogInfo, "Invalid input: More than one flag is true");
			}

			List<Long> deptIdList = new ArrayList<>();
			Set<Integer> projectIdSet = new HashSet<>();
			List<Integer> projectIdListTemp;
			String approvalStatus = null;
			Boolean approvalCheck = null;
			String status = null;
			String projectStatus = null;
			List<ProjectFetchDTO> finalDataList = new ArrayList<>();
			CombinedPOInternalProjectResponse responseData = new CombinedPOInternalProjectResponse();
			
			if ("All".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
				approvalCheck = false;
			}
			else {
				
				 if("Pending for approval".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
					approvalStatus = "true";
					approvalCheck = true;
				}else if("Approved".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
					approvalStatus = "false";
					approvalCheck = true;
				}else if("Rejected".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
					approvalStatus = "Rejected";
					approvalCheck = true;
				}
				
			}
			
			if("Completed".equalsIgnoreCase(projectFilterDTO.getCompletionStatus())) {
				status = "Completed";
			}else if("completedInIshine".equalsIgnoreCase(projectFilterDTO.getCompletionStatus())) {
				projectStatus = "Completed";
			}else if("CompletedWithTeam".equalsIgnoreCase(projectFilterDTO.getCompletionStatus())) {
				status = "Completed";
			}else {
				projectStatus = null;
				status = null;
			}
			
			if (Boolean.TRUE.equals(projectFilterDTO.getIsAdmin())) {
				if(!projectFilterDTO.getDepartmentsids().isEmpty() && projectFilterDTO.getDepartmentsids() != null ) {
					List<Long> selectedDeptList= projectFilterDTO.getDepartmentsids();
					Set<Integer> matchingProjectIds = teamRepository.findAll().stream()
						    .filter(team -> {
						        if (team.getDeptIds() == null) return false;
						        List<String> teamDeptIds = Arrays.asList(team.getDeptIds().split(","));
						        return selectedDeptList.stream()
						                .map(String::valueOf)
						                .anyMatch(teamDeptIds::contains);
						    })
						    .map(Team::getProjectId)
						    .filter(Objects::nonNull)
						    .collect(Collectors.toSet());
					projectIdSet = matchingProjectIds;
					if("Not Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
//						List<Long> deptIdsAccToRole = departmentRepository.findAllDepartments() ;
						finalDataList = projectRepository.getAllNotStartedProjects(selectedDeptList);
						
					}else if("completedInIshine".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
						finalDataList=projectRepository.getAllCompletedProjectListInIshine(selectedDeptList);
					}
					else if(!"CompletedWithTeam".equalsIgnoreCase(projectFilterDTO.getCompletionStatus())) {
						finalDataList = projectRepository.getAllActiveProjectList(projectStatus,status,approvalStatus,projectIdSet,true,approvalCheck);
						}else {
							finalDataList = projectRepository.completedInSankhButTeamMappedList(projectIdSet,true);
						}
				}else {
					if("Not Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
						List<Long> deptIdsAccToRole = departmentRepository.findAllDepartments() ;
						finalDataList = projectRepository.getAllNotStartedProjects(deptIdsAccToRole);
						
					}
					else if("completedInIshine".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
						List<Long> deptIdsAccToRole = departmentRepository.findAllDepartments() ;
						finalDataList=projectRepository.getAllCompletedProjectListInIshine(deptIdsAccToRole);
					}
					else if(!"CompletedWithTeam".equalsIgnoreCase(projectFilterDTO.getCompletionStatus())) {
						finalDataList = projectRepository.getAllActiveProjectList(projectStatus,status,approvalStatus,null,false,approvalCheck);
					}else {
						finalDataList = projectRepository.completedInSankhButTeamMappedList(null,false);
					}
				}
				
			}

			else if (Boolean.TRUE.equals(projectFilterDTO.getIsHod())) {
				List<Department> deptData = departmentRepository.findByHodId(projectFilterDTO.getCurrentUserEmpId());
				if (deptData != null) {
					for (Department data : deptData) {
						if (data != null && data.getDeptId() != null) {
							deptIdList.add(data.getDeptId());
						}
					}
				}
				
				Set<Integer> matchingProjectIds = teamRepository.findAll().stream()
					    .filter(team -> {
					        if (team.getDeptIds() == null) return false;
					        List<String> teamDeptIds = Arrays.asList(team.getDeptIds().split(","));
					        return deptIdList.stream()
					                .map(String::valueOf)
					                .anyMatch(teamDeptIds::contains);
					    })
					    .map(Team::getProjectId)
					    .filter(Objects::nonNull)
					    .collect(Collectors.toSet());
				
				if (projectManagerMappingRepository.isUserProjectManagerOfAnyActiveInternalAndExternalProject(
						projectFilterDTO.getCurrentUserEmpId())) {
					projectIdListTemp = projectManagerMappingRepository
							.isUserProjectManagerOfAnyActiveInternalProjectList(projectFilterDTO.getCurrentUserEmpId());
					if (projectIdListTemp != null)
						projectIdSet.addAll(projectIdListTemp);
				}

				if (projectOverheadMappingRepository.isUserProjectOverheadOfAnyActiveInternalAndExternalProject(
						projectFilterDTO.getCurrentUserEmpId())) {
					projectIdListTemp = projectOverheadMappingRepository
							.isUserProjectOverheadOfAnyActiveInternalAndExternalProjectList(
									projectFilterDTO.getCurrentUserEmpId());
					if (projectIdListTemp != null)
						projectIdSet.addAll(projectIdListTemp);
					
				}

				if (teamRepository.existsBySpocId(projectFilterDTO.getCurrentUserEmpId())) {
					projectIdListTemp = teamRepository
							.findActiveShankhInternalProjectIdsBySpocIdList(projectFilterDTO.getCurrentUserEmpId());
					if (projectIdListTemp != null)
						projectIdSet.addAll(projectIdListTemp);
						
				}
				
				projectIdSet.addAll(matchingProjectIds);
				
				if(!projectFilterDTO.getDepartmentsids().isEmpty() && projectFilterDTO.getDepartmentsids() != null ) {
					List<Long> selectedDeptList= projectFilterDTO.getDepartmentsids();
					Set<Integer> matchingProjIds = teamRepository.findAll().stream()
						    .filter(team -> {
						        if (team.getDeptIds() == null) return false;
						        List<String> teamDeptIds = Arrays.asList(team.getDeptIds().split(","));
						        return selectedDeptList.stream()
						                .map(String::valueOf)
						                .anyMatch(teamDeptIds::contains);
						    })
						    .map(Team::getProjectId)
						    .filter(Objects::nonNull)
						    .collect(Collectors.toSet());
					
					projectIdSet.retainAll(matchingProjIds);
					
					if("Not Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
//						List<Long> deptIdsAccToRole = departmentRepository.findAllDepartments() ;
						finalDataList = projectRepository.getAllNotStartedProjects(selectedDeptList);
						
					}else if("completedInIshine".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
						finalDataList=projectRepository.getAllCompletedProjectListInIshine(selectedDeptList);
					}
					}else {
						if("Not Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
//							List<Long> deptIdsAccToRole = departmentRepository.findAllDepartments() ;
							finalDataList = projectRepository.getAllNotStartedProjects(deptIdList);
							
						}else if("completedInIshine".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
							finalDataList=projectRepository.getAllCompletedProjectListInIshine(deptIdList);
						}
					}
				
				 if(!"CompletedWithTeam".equalsIgnoreCase(projectFilterDTO.getCompletionStatus()) && !"Not Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus()) && !"completedInIshine".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
					finalDataList = projectRepository.getAllActiveProjectList(projectStatus,status,approvalStatus,projectIdSet,true,approvalCheck);
					}else if(!"Not Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())&& !"completedInIshine".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
						finalDataList = projectRepository.completedInSankhButTeamMappedList(projectIdSet,true);
					}
			}

			else if (Boolean.TRUE.equals(projectFilterDTO.getIsOther())) {

				if (projectManagerMappingRepository.isUserProjectManagerOfAnyActiveInternalAndExternalProject(
						projectFilterDTO.getCurrentUserEmpId())) {
					projectIdListTemp = projectManagerMappingRepository
							.isUserProjectManagerOfAnyActiveInternalProjectList(projectFilterDTO.getCurrentUserEmpId());
					if (projectIdListTemp != null)
						projectIdSet.addAll(projectIdListTemp);
				}

				if (projectOverheadMappingRepository.isUserProjectOverheadOfAnyActiveInternalAndExternalProject(
						projectFilterDTO.getCurrentUserEmpId())) {
					projectIdListTemp = projectOverheadMappingRepository
							.isUserProjectOverheadOfAnyActiveInternalAndExternalProjectList(
									projectFilterDTO.getCurrentUserEmpId());
					if (projectIdListTemp != null)
						projectIdSet.addAll(projectIdListTemp);
				}

				if (teamRepository.existsBySpocId(projectFilterDTO.getCurrentUserEmpId())) {
					projectIdListTemp = teamRepository
							.findActiveShankhInternalProjectIdsBySpocIdList(projectFilterDTO.getCurrentUserEmpId());
					if (projectIdListTemp != null)
						projectIdSet.addAll(projectIdListTemp);
				}

				
				if (projectIdSet != null && !projectIdSet.isEmpty()){
					
					if(!projectFilterDTO.getDepartmentsids().isEmpty() && projectFilterDTO.getDepartmentsids() != null ) {
					List<Long> selectedDeptList= projectFilterDTO.getDepartmentsids();
					Set<Integer> matchingProjectIds = teamRepository.findAll().stream()
						    .filter(team -> {
						        if (team.getDeptIds() == null) return false;
						        List<String> teamDeptIds = Arrays.asList(team.getDeptIds().split(","));
						        return selectedDeptList.stream()
						                .map(String::valueOf)
						                .anyMatch(teamDeptIds::contains);
						    })
						    .map(Team::getProjectId)
						    .filter(Objects::nonNull)
						    .collect(Collectors.toSet());
					
					projectIdSet.retainAll(matchingProjectIds);
					
					if("Not Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
//						List<Long> deptIdsAccToRole = departmentRepository.findAllDepartments() ;
						finalDataList = projectRepository.getAllNotStartedProjects(selectedDeptList);
						
					}else if("completedInIshine".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
						finalDataList=projectRepository.getAllCompletedProjectListInIshine(selectedDeptList);
					}
				}else {
					Employee employeee = employeeRepository.findByEmpId(projectFilterDTO.getCurrentUserEmpId());
					List<Long> deptIdOfOther = departmentRepository.findDepartmentIdOfCurrentUser(employeee.getJobRoleId());
					if("Not Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
//						List<Long> deptIdsAccToRole = departmentRepository.findAllDepartments() ;
						finalDataList = projectRepository.getAllNotStartedProjects(deptIdOfOther);
						
					}else if("completedInIshine".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
						finalDataList=projectRepository.getAllCompletedProjectListInIshine(deptIdOfOther);
					}
				}
					
					if(!"CompletedWithTeam".equalsIgnoreCase(projectFilterDTO.getCompletionStatus()) && !"Not Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus()) && !"completedInIshine".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
						finalDataList = projectRepository.getAllActiveProjectList(projectStatus,status,approvalStatus,projectIdSet,true,approvalCheck);
						}else if(!"Not Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus()) && !"completedInIshine".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
							finalDataList = projectRepository.completedInSankhButTeamMappedList(projectIdSet,true);
						}

					
				} 
			}
			else {
				finalDataList = null;
			}
			if(!finalDataList.isEmpty() && finalDataList != null) {List<Long> projectIds = finalDataList.stream()
				    .peek(data -> data.setActive(null))
				    
				    .map(ProjectFetchDTO::getProjectId)
				    .filter(Objects::nonNull)
				    .map(Integer::longValue)
				    .collect(Collectors.toList());

				// Fetch PM and Overhead data
				List<ProjectManagersDTO> pmData = projectManagerMappingRepository
				    .getAllProjectManagerListWithNameThroughPids(projectIds);
				List<ProjectOverheadsDTO> overHeadData = projectOverheadMappingRepository
				    .findProjectOverheadsPerProjectThroughPidList(projectIds);

				// Loop over final data list and populate PM and OH info
				finalDataList.forEach(data -> {
				    Long currentProjectId = data.getProjectId() != null 
				        ? data.getProjectId().longValue() 
				        : null;

				    if (currentProjectId != null) {
				        // Filter PM data
				        List<ProjectManagersDTO> selectedPmData = pmData.stream()
				            .filter(pm -> Objects.equals(pm.getProjectId(), currentProjectId))
				            .collect(Collectors.toList());

				        data.setProjectManagers(selectedPmData);
				        if (!selectedPmData.isEmpty()) {
				            List<Long> pmIdList = selectedPmData.stream()
				                .map(ProjectManagersDTO::getProjectManagerId)
				                .filter(Objects::nonNull)
				                .collect(Collectors.toList());
				            data.setProjectManagerId(pmIdList);
				        }

				        // Filter Overhead data
				        List<ProjectOverheadsDTO> selectedOverHeadData = overHeadData.stream()
				            .filter(oh -> Objects.equals(oh.getProjectId(), currentProjectId))
				            .collect(Collectors.toList());

				        data.setProjectOverheads(selectedOverHeadData);
				        if (!selectedOverHeadData.isEmpty()) {
				            List<Long> ohIdList = selectedOverHeadData.stream()
				                .map(ProjectOverheadsDTO::getProjectOverheadId)
				                .filter(Objects::nonNull)
				                .collect(Collectors.toList());
				            data.setProjectOverheadId(ohIdList);
				        }
				    }
				});

				responseData.setCombinedNewProjects(finalDataList);
}
			
			if (responseData.getCombinedNewProjects() != null) {
				response.setServiceResponse(responseData);
				response.setServiceStatus(response.STATUS_SUCCESS);
			} else {
				response.setServiceResponse("No projects found...!!");
				response.setServiceStatus(response.STATUS_FAIL);
			}
			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return failResponse(response, apiLogInfo, "Internal Server Error: " + e.getMessage());
		}
	}
	
		       
	public ServiceResponse getAllResourceRequirementForProject(ProjectFetchDTO projectFetchDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getAllResourceRequirementForProject");
		apiLogInfo.setApiUrl("/api/getAllResourceRequirementForProject");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("\n getAllResourceRequirementForProject ");
		try {
		if(projectFetchDTO.getProjectId() == null) {
			List<ResourceRequirementDTO> resourceRequirementsTemp= resourceRequirementTempRepo.findByPoProjectId(projectFetchDTO.getPoProjectId());
	        response.setServiceStatus(response.STATUS_SUCCESS);
	        response.setServiceResponse(resourceRequirementsTemp);
		}
		else {
			List<ResourceRequirementDTO> resourceData = resourceRequirementRepository.findByProjectId(projectFetchDTO.getProjectId());
			 
		        response.setServiceStatus(response.STATUS_SUCCESS);
		        response.setServiceResponse(resourceData);
		}
		return response;
		
	}
		catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(response.SOMETHING_WENT_WRONG);
	        response.setServiceResponse(e.getMessage());
	        return response;
		}
	        
	}
	
	public ServiceResponse getPreviousDefaultProjectDetails(Long empId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getPreviousDefaultProjectDetails");
		apiLogInfo.setApiUrl("/api/getPreviousDefaultProjectDetails");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("\n getPreviousDefaultProjectDetails ");
		try {
			
			List<Object[]> details = projectRepository.getPreviousDefaultProjectDetails(empId);
			if(!details.isEmpty()) {
				 Object[] row = details.get(0); 
	             GetPreviousDefaultProjectDetailsDTO dto = new GetPreviousDefaultProjectDetailsDTO();

	             dto.setEmpId(row[0] != null ? ((Number) row[0]).longValue() : null);
	             dto.setPrimaryProjectName(row[1] != null ? row[1].toString() : null);
	             dto.setStartDate(row[2] != null ? row[2].toString() : null); 
	             dto.setClientName(row[3] != null ? row[3].toString() : null); 
	             dto.setBillableType(row[4] != null ? row[4].toString() : null); 
	             
	 			 response.setServiceResponse(dto);
				 response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				 logBuilder.append("\n Default project details fetched successfully!");
			}else {
				response.setServiceResponse("No previous default project mapping!");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				logBuilder.append("\n No previous default project mapping!");
			}
			return response;
		}catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("\n Something went wrong!");
		}
		return response;
	}
	
	private ServiceResponse handleDefaultProjectUpdate(List<Long> empIds, Integer projectId, Long updatedBy, StringBuilder logBuilder) {
		
		ServiceResponse response = new ServiceResponse();
		
		 List<Long> uniqueEmpIds = empIds.stream().distinct().collect(Collectors.toList());

	    if (uniqueEmpIds.isEmpty()) {
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("No default project mapping to update.");
	        logBuilder.append("\n No default project mapping to update.");
	        return response;
	    }

	    DefaultProjectUpdateDTO defaultProjectUpdateDTO = new DefaultProjectUpdateDTO();
	    defaultProjectUpdateDTO.setEmpIds(uniqueEmpIds);
	    defaultProjectUpdateDTO.setProjectId(projectId);
	    defaultProjectUpdateDTO.setUpdatedBy(updatedBy);

	    response = this.setDefaultProjectUpdateBillable(defaultProjectUpdateDTO);

	    if (ServiceResponse.STATUS_SUCCESS.equals(response.getServiceStatus())) {
	        logBuilder.append("\nDefault project mapping updated successfully.");
	        System.out.println("Default project mapping updated successfully.");
	    } else {
	        logBuilder.append("\nFailed to update default project mapping: ").append(response.getServiceResponse());
	        System.out.println("Failed to update default project mapping: " + response.getServiceResponse());
	    }

	    return response;
	}
	
	public Map<String, Object> getActiveProjectDetailsIfMultiple(Long empId, Integer currentProjectId) {
		
	    Map<String, Object> result = new HashMap<>();

	    List<GetActiveProjectDetailsIfMultipleDTO> dtoList = employeeTeamMapRepository.getActiveProjectIdAndProjectNameByEmpId(empId,currentProjectId); 
	    
	    Map<Integer, GetActiveProjectDetailsIfMultipleDTO> uniqueProjectsMap = dtoList.stream()
	            .collect(Collectors.toMap(
	                GetActiveProjectDetailsIfMultipleDTO::getProjectId, 
	                dto -> dto,                                         
	                (existing, replacement) -> existing                 
	            ));
	    
	    List<Map<String, Object>> activeProjects = uniqueProjectsMap.values().stream()
	    	    .map(dto -> {
	    	        Map<String, Object> map = new HashMap<>();
	    	        map.put("projectId", dto.getProjectId());
	    	        map.put("projectName", dto.getProjectName());
	    	        return map;
	    	    })
	    	    .collect(Collectors.toList());
	    
	    if (!activeProjects.isEmpty()) {
	        result.put("isMultipleActiveProjects", true);
	        result.put("projects", activeProjects);
	    } else {
	        result.put("isMultipleActiveProjects", false);
	        result.put("projects", Collections.emptyList());
	    }

	    return result;
	}
	
	public Integer isDefaultProject(Long empId, Integer projectId) {
		StringBuilder logs = new StringBuilder();
	    try {
	        EmpPrimaryProjectMapping empPrimaryProjectMapping = empPrimaryProjectMappingRepository.findByEmpId(empId);

	        if (empPrimaryProjectMapping != null && empPrimaryProjectMapping.getPrimaryProjectId() != null) {
	            if (Integer.parseInt(empPrimaryProjectMapping.getPrimaryProjectId().toString()) == projectId) {
	            	System.err.println("The selected project ( projectId : " + projectId +  " ) is the default project for the employee ( empId :  " + empId );
			        logs.append("The selected project ( projectId : " + projectId +  " ) is the default project for the employee ( empId :  " + empId );
	                return 1;
	            } else {
	            	System.err.println("The selected project ( projectId : " + projectId +  " ) is not the default project for the employee ( empId :  " + empId );
			        logs.append("The selected project ( projectId : " + projectId +  " ) is not the default project for the employee ( empId :  " + empId );
	                return 0;
	            }
	        } else {
	        	System.err.println("Mapping not found or null primaryProjectId");
		        logs.append("\n Mapping not found or null primaryProjectId");
	            return 0;
	        }
	    } catch (Exception e) {
	        System.err.println("Error in isDefaultProject: " + e.getMessage());
	        logs.append("\n Error in isDefaultProject: " + e.getMessage());
	        e.printStackTrace();
	        return 0;
	    }
	}
	
	public ServiceResponse getProjectDetailsForBulkDefaultUpdate() {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("getProjectDetailsForBulkDefaultUpdate");
	    apiLogInfo.setApiUrl("/api/getProjectDetailsForBulkDefaultUpdate");
	    apiLogInfo.setLogLevel("INFO");
	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("\n getProjectDetailsForBulkDefaultUpdate ");

	    try {
	       
	        List<GetProjectDetailsForBulkDefaultUpdateProjectDTO> benchProjectList = projectRepository.getProjectDetailsForBulkDefaultUpdateBench();
	        List<GetProjectDetailsForBulkDefaultUpdateProjectDTO> otherProjectList = projectRepository.getProjectDetailsForBulkDefaultUpdateOther();

	        benchProjectList = mapResultsToDTOs(benchProjectList);
	        otherProjectList = mapResultsToDTOs(otherProjectList);
	        
	        Map<String, Object> resultMap = new HashMap<>();
	        resultMap.put("benchProjectList", benchProjectList);
	        resultMap.put("otherProjectList", otherProjectList);

	        response.setServiceResponse(resultMap);
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        logBuilder.append("\n Bench and other project details fetched successfully!");
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse("Something went wrong!");
	        logBuilder.append("\n Exception: ").append(e.getMessage());
	    }

	    apiLogInfo.setApiRequest(logBuilder.toString());
	    logService.logMyInfo(httpRequest, apiLogInfo);

	    return response;
	}
	
	private List<GetProjectDetailsForBulkDefaultUpdateProjectDTO> mapResultsToDTOs(List<GetProjectDetailsForBulkDefaultUpdateProjectDTO> resourceDTOList) {
	    Map<Integer, GetProjectDetailsForBulkDefaultUpdateProjectDTO> projectMap = new LinkedHashMap<>();

	    for (GetProjectDetailsForBulkDefaultUpdateProjectDTO resource : resourceDTOList) {
	        Integer projectId = resource.getProjectId();
	        String projectName = resource.getProjectName();
	        Long teamId = resource.getTeamId();
	        String teamName = resource.getTeamName();

	        GetProjectDetailsForBulkDefaultUpdateProjectDTO projectDTO = projectMap.computeIfAbsent(projectId, id -> {
	            GetProjectDetailsForBulkDefaultUpdateProjectDTO dto = new GetProjectDetailsForBulkDefaultUpdateProjectDTO();
	            dto.setProjectId(id);
	            dto.setProjectName(projectName);
	            return dto;
	        });

	        if (teamId != null && teamName != null &&
	            projectDTO.getTeamList().stream().noneMatch(t -> teamId.equals(t.getTeamId()))) {
	            
	            GetProjectDetailsForBulkDefaultUpdateTeamDTO teamDTO = new GetProjectDetailsForBulkDefaultUpdateTeamDTO();
	            teamDTO.setTeamId(teamId);
	            teamDTO.setTeamName(teamName);
	            projectDTO.getTeamList().add(teamDTO);
	        }

	        if (resource.getResourceOverviewId() != null &&
	            projectDTO.getResourceRequirement().stream().noneMatch(r -> r.getResourceOverviewId().equals(resource.getResourceOverviewId()))) {
	            
	            ResourceRequirementDTO resourceCopy = new ResourceRequirementDTO();
	            resourceCopy.setResourceOverviewId(resource.getResourceOverviewId());
	            resourceCopy.setCount(resource.getCount());
	            resourceCopy.setDepartment(resource.getDepartment());
	            resourceCopy.setExperience(resource.getExperience());
	            resourceCopy.setRole(resource.getRole());
	            resourceCopy.setTeamId(resource.getTeamId());
	            resourceCopy.setTeamName(resource.getTeamName());
	            resourceCopy.setProjectId(resource.getProjectId());
	            resourceCopy.setProjectName(resource.getProjectName());

	            projectDTO.getResourceRequirement().add(resourceCopy);
	        }
	    }

	    return new ArrayList<>(projectMap.values());
	}
	
	public ServiceResponse getEmployeeInformationBulk(List<Long> empIds) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("getEmployeeInformationBulk");
	    apiLogInfo.setApiUrl("/api/getEmployeeInformationBulk");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();

	    try {
	        List<Object[]> resultSet = projectRepository.getEmployeeInformationBulk(empIds);
	        logBuilder.append("\n getEmployeeInformationBulk - Total Records: ").append(resultSet.size());

	        List<EmployeeInformationDTO> resultDTO = new ArrayList<>();

	        for (Object[] result : resultSet) {
	            EmployeeInformationDTO dto = new EmployeeInformationDTO();
	            dto.setEmpId(result[0] != null ? Long.parseLong(result[0].toString()) : null);
	            dto.setEmploymentId(result[1] != null ? result[1].toString() : null);
	            dto.setName(result[2] != null ? result[2].toString() : null);
	            dto.setPreviousExperience(result[3] != null ? result[3].toString() : null);
	            dto.setCurrentExperience(result[4] != null ? result[4].toString() : null);
	            dto.setTotalExperience(result[5] != null ? result[5].toString() : null);
	            dto.setBillableType(result[6] != null ? result[6].toString() : null);
	            dto.setJobRole(result[7] != null ? result[7].toString() : null);
	            dto.setDeptName(result[8] != null ? result[8].toString() : null);

	            resultDTO.add(dto);
	        }

	        if (!resultDTO.isEmpty()) {
	            response.setServiceResponse(resultDTO);
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            logBuilder.append("\n Employee information fetched successfully.");
	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No data found for empIds: " + empIds);
	            logBuilder.append("\n No data found for empIds: ").append(empIds);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse("Something went wrong!");
	        logBuilder.append("\n Exception occurred: ").append(e.getMessage());
	    }

	    return response;
	}
	
	@Transactional(rollbackOn = Exception.class)
	public ServiceResponse setProjectMappingAndDefaultProject(SetProjectMappingAndDefaultProjectDTO dto) {
		ServiceResponse response = new ServiceResponse();
		StringBuilder logBuilder = new StringBuilder();
		LogDTO apiLogInfo = new LogDTO();
		try {
			List<Long> defaultProjectEmpIds = new ArrayList<>();
			
			List<EmployeeTeamMapDTO> newTeamMember = new ArrayList<>();
			
	        for (Long empId : dto.getEmpId()) {
	        	EmployeeTeamMapDTO member = new EmployeeTeamMapDTO();
	            member.setEmpId(empId);
	            member.setTeamId(dto.getTeamId());
	            member.setEmployeeRole(dto.getEmployeeRole());
	            member.setStartDate(new Timestamp(System.currentTimeMillis()));
	            member.setIsDefaultProject(1);	            
	            member.setResourceOverviewId(dto.getResourceOverViewId());
	            newTeamMember.add(member);
	        }
			
			newTeamMember.forEach((newMember) -> {
				List<EmployeeTeamMap> presentMember = employeeTeamMapRepository
						.findFirstByEmpIdAndTeamIdAndActive(newMember.getEmpId(), dto.getTeamId());

				List<EmployeeTeamMap> mapList = new ArrayList<EmployeeTeamMap>();
				EmployeeTeamMap empTeamMap = new EmployeeTeamMap();

				if (presentMember.isEmpty()) {
					
						StringBuilder employeeRole = new StringBuilder("");
						for (String empRole : newMember.getEmployeeRole()) {
							employeeRole.append(empRole).append(",");
						}
						empTeamMap.setEmpId(newMember.getEmpId());
						empTeamMap.setEmployeeRole(employeeRole.toString());
						empTeamMap.setTeamId(dto.getTeamId());
						empTeamMap.setStartDate(LocalDateTime.now());
						empTeamMap.setIsShadow(newMember.getIsShadow() != null ? newMember.getIsShadow() : null);
						empTeamMap.setResourceOverviewId(newMember.getResourceOverviewId() != null
								? Long.parseLong(newMember.getResourceOverviewId().toString())
								: null);
						empTeamMap.setUpdatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : null);
						
						if(newMember.getIsDefaultProject() != null) {
							if( newMember.getIsDefaultProject() == 1) {
								defaultProjectEmpIds.add(newMember.getEmpId());
							}
						}
						
						
						mapList.add(empTeamMap);
					

					List<EmployeeTeamMap> teamMapDbResponse = employeeTeamMapRepository.saveAll(mapList);
					
					//Add default project mapping
					if(!defaultProjectEmpIds.isEmpty()) {
						ServiceResponse defaultProjectResponse = this.handleDefaultProjectUpdate(defaultProjectEmpIds, dto.getProjectId(), dto.getCreatedBy(), logBuilder);
						
						if (ServiceResponse.STATUS_SUCCESS.equals(defaultProjectResponse.getServiceStatus())) {
						    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						    response.setServiceResponse(defaultProjectResponse.getServiceResponse());
						} else {
						    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						    response.setServiceResponse(defaultProjectResponse.getServiceResponse());
						}
					}

					teamMapDbResponse.forEach((newAddedMember) -> {
						newAddedMember.setActive(2L);

						System.out.println(" newTeamMember   " + newAddedMember);

						if (newAddedMember.getEmpId() != null) {
							List<Object[]> employeeDetails = employeeRepository
									.getEmployeeByEmpId(newAddedMember.getEmpId());
							System.out.println("New member added: " + employeeDetails.get(0));

							if (employeeDetails != null && !employeeDetails.isEmpty()) {
								Object[] employeeDetailRow = employeeDetails.get(0);
								String departmentId = employeeDetailRow[46] != null ? employeeDetailRow[46].toString()
										: null;
								System.out.println("Department ID: " + departmentId);

								if (departmentId != null) {
									List<String> employeeRoles = Arrays
											.asList(newAddedMember.getEmployeeRole().split(","));
									for (String role : employeeRoles) {
										role = role.trim();
										System.out.println("Processing role: " + role);

										List<Activity> existingActivities = activitiesRepository
												.findByDeptIdsAndEmployeeRoleAndTeamId(departmentId, role,
														dto.getTeamId());

										if (existingActivities.isEmpty()) {
											System.out.println("No activities exist for Dept ID: " + departmentId
													+ ", Role: " + role);

											List<ActivityTemplate> activityTemplateList = activityTemplateRepository
													.getByDeptIdAndEmployeeRoleType(Long.parseLong(departmentId), role);
											if (!activityTemplateList.isEmpty()) {
												for (ActivityTemplate activityTemplate : activityTemplateList) {
													Activity newActivity = new Activity();
													newActivity.setActivity(activityTemplate.getTemplateActivity());
													newActivity.setTeamId(dto.getTeamId());
													newActivity.setEmployeeRole(activityTemplate.getEmployeeRole());
													newActivity.setDeptIds(activityTemplate.getDeptId().toString());
													newActivity.getCommonProperty()
															.setCreatedBy(dto.getCreatedBy());
													activitiesRepository.save(newActivity);

													System.out.println("New activity created: "
															+ activityTemplate.getTemplateActivity());
												}
											} else {
												System.out.println("No activity templates found for Dept ID: "
														+ departmentId + ", Role: " + role);
											}
										} else {
											System.out.println("Activities already exist for Dept ID: " + departmentId
													+ ", Role: " + role);
										}
									}
								} else {
									System.out.println(
											"Department ID is null for Employee ID: " + newAddedMember.getEmpId());
								}
							} else {
								System.out.println(
										"No employee details found for Employee ID: " + newAddedMember.getEmpId());
							}
						}

						Employee findEmp = employeeRepository.findByEmpId(newAddedMember.getEmpId());
						Employee managerEmail = employeeRepository.findByEmpId(findEmp.getManagerId());
						String hodMail = employeeRepository.findHodMail(newAddedMember.getEmpId());

						String ccMail = hodMail + "," + managerEmail.getEmail().toString() + "," + rmgMail + ","
								+ adminMail;
					
						Project projectFind = projectRepository.findByProjectId(dto.getProjectId());
						
						try {
							mailService.sendMailWithCC(findEmp.getEmail().toString(), ccMail,
									"Regarding resource mapping to new project",
									"Dear " + findEmp.getName() + "<br>" + "You have been mapped " + " under the project "
											+ projectFind.getProjectName() + "<br><br><br>"
											+ "Sincerely,<br>Team RMG - ApMoSys Technologies");
						} catch (AddressException e) {
							e.printStackTrace();
						} catch (MessagingException e) {
							e.printStackTrace();
						}
					});

					employeeTeamMapRepository.saveAll(teamMapDbResponse);
				}

			});

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		return response;

	}
	
	public ServiceResponse getEmployeeInformationForDefaultProject(OtherProjectSetDTO otherProjectSetDTO) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("getEmployeeInformationForDefaultProject");
	    apiLogInfo.setApiUrl("/api/getEmployeeInformationForDefaultProject");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();

	    try {
	        List<Object[]> resultSet = projectRepository.getEmployeeInformationBulk(otherProjectSetDTO.getEmpIds());
	        logBuilder.append("\n getEmployeeInformationBulk - Total Records: ").append(resultSet.size());

	        List<GetEmployeeInformationForDefaultProjectDTO> resultDTO = new ArrayList<>();

	        for (Object[] result : resultSet) {
	        	GetEmployeeInformationForDefaultProjectDTO dto = new GetEmployeeInformationForDefaultProjectDTO();
	        	Long empId = Long.parseLong(result[0].toString());
	            dto.setEmpId(result[0] != null ? Long.parseLong(result[0].toString()) : null);
	            dto.setEmploymentId(result[1] != null ? result[1].toString() : null);
	            dto.setName(result[2] != null ? result[2].toString() : null);
	            
	            Map<String, Object> activeProjectInfo = this.getActiveProjectDetailsIfMultiple(empId, otherProjectSetDTO.getProjectId());
				if ((Boolean) activeProjectInfo.get("isMultipleActiveProjects")) {
				    List<Map<String, Object>> otherProjects = (List<Map<String, Object>>) activeProjectInfo.get("projects");
				    dto.setOtherActiveProjects(otherProjects);
				} else {
					dto.setOtherActiveProjects(Collections.emptyList());
				}

	            resultDTO.add(dto);
	        }

	        if (!resultDTO.isEmpty()) {
	            response.setServiceResponse(resultDTO);
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            logBuilder.append("\n Employee information fetched successfully.");
	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No data found for empIds: " + otherProjectSetDTO.getEmpIds());
	            logBuilder.append("\n No data found for empIds: ").append(otherProjectSetDTO.getEmpIds());
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse("Something went wrong!");
	        logBuilder.append("\n Exception occurred: ").append(e.getMessage());
	    }

	    return response;
	}
	
	public ServiceResponse getBenchEmployeeMoreThan30Days(ProjectFilterDTO projectFilterDTO) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("getBenchEmployeeMoreThan30Days");
	    apiLogInfo.setApiUrl("/api/getBenchEmployeeMoreThan30Days");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();

	    try {
	    	Long empIdd = projectFilterDTO.getCurrentUserEmpId();
		    Employee employee1 = employeeRepository.findByEmpId(empIdd);
		    JobRole jobRole = jobRoleRepository.findByjobRoleId(employee1.getJobRoleId());
		    Department department1 = departmentRepository.findByDeptId(jobRole.getDeptId());

		    String departmentName = department1.getName();
		    String role = jobRole.getEmployeeRole();
		    String name = jobRole.getName();

		    Set<String> specialDepartments = Set.of("Admin", "Resource Management Group", "Director", "Super Admin", "Accounts", "HR");
			
			 List<Object[]> resultSet = new ArrayList<>();
			
			 if (role.equalsIgnoreCase("SuperAdmin") || name.equalsIgnoreCase("Director")
			            || name.equalsIgnoreCase("Super Admin") || role.equalsIgnoreCase("Accounts")
			            || specialDepartments.contains(departmentName)) {
				 resultSet = projectRepository.getBenchEmployeeMoreThan30Days();
			 }else if(departmentRepository.existsByHodId(projectFilterDTO.getCurrentUserEmpId())) {
					List<Long> deptIds = departmentRepository.findDeptIdsByHodId(projectFilterDTO.getCurrentUserEmpId());
					resultSet = projectRepository.getBenchEmployeeMoreThan30DaysInDeptIds(deptIds);
			}else {
				Employee employeee = employeeRepository.findByEmpId(projectFilterDTO.getCurrentUserEmpId());
				Long deptId = departmentRepository.findDepartmentIdOfSpoc(employeee.getJobRoleId());
				resultSet = projectRepository.getBenchEmployeeMoreThan30DaysInDeptId(deptId);
			}
	        logBuilder.append("\n getBenchEmployeeMoreThan30Days - Total Records: ").append(resultSet.size());

	        Map<Long, BenchEmployeeDetailsDTO> employeeMap = new HashMap<>();

	        for (Object[] row : resultSet) {
//	        	 String employmentId = (String) row[0];
	            Long empId = ((Number) row[0]).longValue();
	            String empName = (String) row[1];
	            String employmentId = (String) row[2];
	            String billable = (String) row[3];
	            String billableType = (String) row[4];
	            String department = (String) row[5];

	            Integer projectId = ((Number) row[6]).intValue();
	            String projectName = (String) row[7];
	            Long poProjectId = row[8] != null ? ((Number) row[8]).longValue() : null;
	            String poStartDate = row[9] != null ? String.valueOf(row[9]) : null;
	            String poEndDate = row[10] != null ? String.valueOf(row[10]) : null;
	            String apmosysRM = (String) row[11];
	            String clientRM = (String) row[12];
	            String poProjectType = (String) row[13];
	            String poNo = (String) row[14];
	            String clientName = (String) row[15];

	            Long teamId = ((Number) row[16]).longValue();
	            String teamName = (String) row[17];
	            String teamIsActive = (String) row[18];
	            String employeeRole = (String) row[19];
	            String teamStatus = String.valueOf(row[20]);
	            
	            Long projectManagerId = ((Number) row[21]).longValue();
	            String projectManagerName = (String) row[22];
	            
	            Date onBenchDateObj = (Date) row[23];
	            String onBenchDate = onBenchDateObj.toString(); 
	        	Long daysOnBench =  ((Number)row[24]).longValue();
	            
	            BenchEmployeeDetailsDTO employee = employeeMap.computeIfAbsent(empId, id -> {
	                BenchEmployeeDetailsDTO e = new BenchEmployeeDetailsDTO();
	                e.setEmpId(empId);
	                e.setName(empName);
	                e.setEmployeementId(employmentId);
	                e.setBillable(billable);
	                e.setBillableType(billableType);
	                e.setDepartment(department);
	                e.setDaysOnBench(daysOnBench);
	                e.setOnBenchDate(onBenchDate);
	                e.setRmgprojects(new ArrayList<>());
	                return e;
	            });

	            RMGProject project = employee.getRmgprojects().stream()
	                    .filter(p -> p.getProjectId().equals(projectId))
	                    .findFirst()
	                    .orElse(null);

	            if (project == null) {
	                project = new RMGProject();
	                project.setProjectId(projectId);
	                project.setProjectName(projectName);
	                project.setPoProjectId(poProjectId);
	                project.setPoStartDate(poStartDate);
	                project.setPoEndDate(poEndDate);
	                project.setApmosysRM(apmosysRM);
	                project.setClientRM(clientRM);
	                project.setPoProjectType(poProjectType);
	                project.setPoNo(poNo);
	                project.setClientName(clientName);
	                project.setRmgTeam(new ArrayList<>());
	                project.setProjectManagers(new ArrayList<>());
	                employee.getRmgprojects().add(project);
	            }

	            boolean teamExists = project.getRmgTeam().stream()
	                    .anyMatch(t -> t.getTeamId().equals(teamId));

	            if (!teamExists) {
	                RMGTeam team = new RMGTeam();
	                team.setTeamId(teamId);
	                team.setTeamName(teamName);
	                team.setIsActive(teamIsActive);
	                team.setEmployeeRole(employeeRole);
	                team.setStatus(teamStatus);
	                project.getRmgTeam().add(team);
	            }

	            boolean managerExists = project.getProjectManagers().stream()
	                    .anyMatch(pm -> pm.getProjectManagerName().equals(projectManagerName));

	            if (!managerExists) {
	                ProjectManagersDTO pmDTO = new ProjectManagersDTO();
	                pmDTO.setProjectManagerId(projectManagerId);
	                pmDTO.setProjectManagerName(projectManagerName);
	                project.getProjectManagers().add(pmDTO);
	            }
	        }

	        List<BenchEmployeeDetailsDTO> resultDTO = new ArrayList<>(employeeMap.values());

	        if (!resultDTO.isEmpty()) {
	            response.setServiceResponse(resultDTO);
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            logBuilder.append("\n Resource requirement fetched successfully.");
	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No employee found in bench that has exceeded 30 days!");
	            logBuilder.append("\n No employee found in bench that has exceeded 30 days! ");
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse("Something went wrong!");
	        logBuilder.append("\n Exception occurred: ").append(e.getMessage());
	    }

	    return response;
	}
	
	
	public ServiceResponse getProjectTimesheetSummary(ResourceManagementDTO resourceManagementDTO) {

	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("Project 360");
	    apiLogInfo.setApiUrl("/api/getProjectTimesheetSummary");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("Fetching timesheet summary for emp_id: ")
	              .append(resourceManagementDTO.getEmpId());

	    try {
	        List<SummaryChartDTO> summaryData = projectRepository
	            .getProjectTimesheetSummaryByEmpId(resourceManagementDTO.getEmpId());

	        List<SummaryChartDTO> result = new ArrayList<>();

	        if (!summaryData.isEmpty()) {
	            for (SummaryChartDTO row : summaryData) {
	            	SummaryChartDTO dto = new SummaryChartDTO();

	                dto.setProjectName(row.getProjectName() != null ? row.getProjectName().toString() : null);
	                dto.setTotalTimesheetsFilled(row.getTotalTimesheetsFilled() != null ? Long.parseLong(row.getTotalTimesheetsFilled().toString()) : 0);

	                result.add(dto);
	            }

	            response.setServiceResponse(result);
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            apiLogInfo.setApiResponse("Project timesheet summary fetched");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No timesheet data found for the employee.");
	            apiLogInfo.setApiResponse("No data found");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        }
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
	
	public ServiceResponse getProjectStatusByPoProjectId(Set<Long> poProjectId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getProjectStatusByPoProjectId");
		apiLogInfo.setApiUrl("/api/getProjectStatusByPoProjectId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("\n getProjectStatusByPoProjectId ");
		ApiLog initialLog = null;
		String exceptionDetailsForLog = null;
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		String sourceSystem = httpRequest.getRequestURI().toString();

		try {
			
			initialLog = apiLogUtility.startLog(poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),"getProjectStatusByPoProjectId", "PoPortal", null, httpRequest);
			if (poProjectId == null || poProjectId.isEmpty()){
				response.setServiceResponse("PoProject Id cannot be null!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiResponse("PoProject Id cannot be null!");
				exceptionDetailsForLog = "PoProject Id cannot be null!";
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				throw new BadRequestException("PoProject Id cannot be null!");
			}
			
			List<Object[]> poProjectStatusList = projectRepository.getProjectStatusByPoProjectId(poProjectId);
			if (poProjectStatusList != null && !poProjectStatusList.isEmpty()) {
				Map<Long, String> result = objListToPoProjectStatusMap(poProjectStatusList);
				response.setServiceResponse(result);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setApiResponse("Project status fetched");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				finalHttpStatusCode = HttpStatus.OK.value();

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Projects found for provided poProjectIds.");
				apiLogInfo.setApiResponse("No Projects found for provided poProjectIds.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				finalHttpStatusCode = HttpStatus.NOT_FOUND.value();
				throw new DataNotFoundException("No Projects found for provided poProjectIds.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			exceptionDetailsForLog = e.toString();
		} finally {
			if (initialLog != null) {
				apiLogUtility.endLog(initialLog.getId(),sourceSystem ,finalHttpStatusCode, exceptionDetailsForLog, httpRequest);
			}
		}
		return response;
	}
	
	private Map<Long, String> objListToPoProjectStatusMap(List<Object[]> poProjectStatusList) {
		Map<Long, String> result = new HashMap<>();
		try {
			for (Object[] row : poProjectStatusList) {
				Long projectId = row[0] != null ? ((Number) row[0]).longValue() : null;
				String status = row[1] != null ? row[1].toString() : "Not Started";
				if (projectId != null) {
					result.put(projectId, status);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return result;
	}

	public ServiceResponse getDeptsByRole(Long currentUserEmpId) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("getDeptsByRole");
	    apiLogInfo.setApiUrl("/api/getDeptsByRole");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();

	    try {
	    	ProjectFilterDTO projectFilterDTO = new ProjectFilterDTO();
	    	projectFilterDTO.setCurrentUserEmpId(currentUserEmpId);
	    	Employee employee1 = employeeRepository.findByEmpId(currentUserEmpId);
		    JobRole jobRole = jobRoleRepository.findByjobRoleId(employee1.getJobRoleId());
		    Department department1 = departmentRepository.findByDeptId(jobRole.getDeptId());

		    String departmentName = department1.getName();
		    String role = jobRole.getEmployeeRole();
		    String name = jobRole.getName();

		    Set<String> specialDepartments = Set.of("Admin", "Resource Management Group", "Director", "Super Admin", "Accounts", "HR");
			
		    Set<Long> accessibleDeptIds = new HashSet<>();
		    Map<Long, String> fullDeptMap = new HashMap<>();
			
		    if (role.equalsIgnoreCase("SuperAdmin") || name.equalsIgnoreCase("Director")
			            || name.equalsIgnoreCase("Super Admin") || role.equalsIgnoreCase("Accounts")
			            || specialDepartments.contains(departmentName)) {
		    	
		    	List<GetDeptIdByRoleDTO> departments = departmentRepository.findAllExceptId1();
		        for (GetDeptIdByRoleDTO dept : departments) {
		            accessibleDeptIds.add(dept.getDeptId());
		            fullDeptMap.put(dept.getDeptId(), dept.getName());
		        }
		    	projectFilterDTO.setIsAdmin(true);
		    	
			 } else {
				 
				 if(departmentRepository.existsByHodId(currentUserEmpId)) {

					 List<GetDeptIdByRoleDTO> hodDeptList = departmentRepository.findDeptIdsByHodId2(currentUserEmpId);
					 if (hodDeptList != null && !hodDeptList.isEmpty()) {
					     logBuilder.append("\n Department list fetched for HOD.");
					     for (GetDeptIdByRoleDTO dto : hodDeptList) {
					         accessibleDeptIds.add(dto.getDeptId());
					         fullDeptMap.put(dto.getDeptId(), dto.getName());
					     }
					     projectFilterDTO.setIsHod(true);
					 }
					 
				 }
				
				 boolean isOther = false;

				 List<GetDeptIdByRoleDTO> pmDeptIds = departmentRepository.findDeptIdsForProjectManager(currentUserEmpId);
				 if (pmDeptIds != null && !pmDeptIds.isEmpty()) {
				     logBuilder.append("\n Department list fetched for Project Manager.");
				     for (GetDeptIdByRoleDTO dto : pmDeptIds) {
				            accessibleDeptIds.add(dto.getDeptId());
				            fullDeptMap.put(dto.getDeptId(), dto.getName());
			         }
				     isOther = projectFilterDTO.getIsHod() == null ? true : false;
				 }

				 List<GetDeptIdByRoleDTO> overheadDeptIds = departmentRepository.findDeptIdsForProjectOverhead(currentUserEmpId);
				 if (overheadDeptIds != null && !overheadDeptIds.isEmpty()) {
				     logBuilder.append("\n Department list fetched for Project Overhead.");
				     for (GetDeptIdByRoleDTO dto : overheadDeptIds) {
				            accessibleDeptIds.add(dto.getDeptId());
				            fullDeptMap.put(dto.getDeptId(), dto.getName());
			         }
				     isOther = projectFilterDTO.getIsHod() == null ? true : false;
				 }
				 
				 String teamLeadDeptCsv = departmentRepository.findDeptIdsForTeamLead(currentUserEmpId);
				 if (teamLeadDeptCsv != null && !teamLeadDeptCsv.isEmpty()) {
				 	logBuilder.append("\n Department list fetched for Team Lead.");
				 	List<Long> teamLeadIds = Arrays.stream(teamLeadDeptCsv.split(","))
				 	    .map(String::trim)
				 	    .filter(s -> s.matches("\\d+"))
				 	    .map(Long::parseLong)
				 	    .filter(deptId -> !accessibleDeptIds.contains(deptId))
				 	    .collect(Collectors.toList());

				 	if (!teamLeadIds.isEmpty()) {
				 		List<GetDeptIdByRoleDTO> teamLeadDepts = departmentRepository.findDepartmentsByIds(teamLeadIds);
				 		for (GetDeptIdByRoleDTO dto : teamLeadDepts) {
			                accessibleDeptIds.add(dto.getDeptId());
			                fullDeptMap.put(dto.getDeptId(), dto.getName());
			            }
				 		isOther = projectFilterDTO.getIsHod() == null;
				 	}
				 }

				 String spocDeptCsv = departmentRepository.findDeptIdsForSpoc(currentUserEmpId);
				 if (spocDeptCsv != null && !spocDeptCsv.isEmpty()) {
				 	logBuilder.append("\n Department list fetched for Spoc.");
				 	List<Long> spocIds = Arrays.stream(spocDeptCsv.split(","))
				 	    .map(String::trim)
				 	    .filter(s -> s.matches("\\d+"))
				 	    .map(Long::parseLong)
				 	    .filter(deptId -> !accessibleDeptIds.contains(deptId))
				 	    .collect(Collectors.toList());

				 	if (!spocIds.isEmpty()) {
				 		List<GetDeptIdByRoleDTO> spocDepts = departmentRepository.findDepartmentsByIds(spocIds);
				 		for (GetDeptIdByRoleDTO dto : spocDepts) {
			                accessibleDeptIds.add(dto.getDeptId());
			                fullDeptMap.put(dto.getDeptId(), dto.getName());
			            }
				 		isOther = projectFilterDTO.getIsHod() == null;
				 	}
				 }

				 if (isOther && projectFilterDTO.getIsHod() == null) {
				     projectFilterDTO.setIsOther(true);
				 }
			 }

	        if (accessibleDeptIds.isEmpty() || fullDeptMap.isEmpty()) {

	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse("You currently do not have access to any departments or projects based on your role (Admin, HOD, Project Manager, Project Overhead, Team Lead, or SPOC).");
	            logBuilder.append("\n No department found ! ");
	        	
	        } else {
	        	
	        	List<GetDeptIdByRoleDTO> finalDeptList = fullDeptMap.entrySet().stream()
	        	        .map(entry -> new GetDeptIdByRoleDTO(entry.getKey(), entry.getValue()))
	        	        .collect(Collectors.toList());
	        	
	        	projectFilterDTO.setDepartments(finalDeptList);
	            projectFilterDTO.setDepartmentsids(new ArrayList<>(accessibleDeptIds));
	            response.setServiceResponse(projectFilterDTO);
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            logBuilder.append("\n Department list fetched successfully.");
	            
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse("Something went wrong!");
	        logBuilder.append("\n Exception occurred: ").append(e.getMessage());
	    }

	    return response;
	}
	
	public ServiceResponse getDeptsByUser(Long currentUserEmpId) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("getDeptsByUser");
	    apiLogInfo.setApiUrl("/api/getDeptsByUser");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();

	    try {
	    	ProjectFilterDTO projectFilterDTO = new ProjectFilterDTO();
	    	projectFilterDTO.setCurrentUserEmpId(currentUserEmpId);
			
		    Set<Long> accessibleDeptIds = new HashSet<>();
		    Map<Long, String> fullDeptMap = new HashMap<>();
			
		    if(departmentRepository.existsByHodId(currentUserEmpId)) {

				 List<GetDeptIdByRoleDTO> hodDeptList = departmentRepository.findDeptIdsByHodId2(currentUserEmpId);
				 if (hodDeptList != null && !hodDeptList.isEmpty()) {
				     logBuilder.append("\n Department list fetched for HOD.");
				     for (GetDeptIdByRoleDTO dto : hodDeptList) {
				         accessibleDeptIds.add(dto.getDeptId());
				         fullDeptMap.put(dto.getDeptId(), dto.getName());
				     }
				     projectFilterDTO.setIsHod(true);
				 }
				 
			 } else {
				 
				 if(departmentRepository.isUserMappedInAnyRole(currentUserEmpId)) {
					 
					 List<GetDeptIdByRoleDTO> deptIds = departmentRepository.findDeptsByEmpId(currentUserEmpId);
					 if (deptIds != null && !deptIds.isEmpty()) {
					     logBuilder.append("\n Department list fetched for Other (Not HOD and NOt SuperAdmin).");
					     for (GetDeptIdByRoleDTO dto : deptIds) {
					            accessibleDeptIds.add(dto.getDeptId());
					            fullDeptMap.put(dto.getDeptId(), dto.getName());
				         }
					     projectFilterDTO.setIsOther(true);
					 }
					 
				 }
			 }

	        if (accessibleDeptIds.isEmpty() || fullDeptMap.isEmpty()) {

	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse("You currently do not have access to any departments or projects based on your role (Admin, HOD, Project Manager, Project Overhead, Team Lead, or SPOC).");
	            logBuilder.append("\n No department found ! ");
	        	
	        } else {
	        	
	        	List<GetDeptIdByRoleDTO> finalDeptList = fullDeptMap.entrySet().stream()
	        	        .map(entry -> new GetDeptIdByRoleDTO(entry.getKey(), entry.getValue()))
	        	        .collect(Collectors.toList());
	        	
	        	projectFilterDTO.setDepartments(finalDeptList);
	            projectFilterDTO.setDepartmentsids(new ArrayList<>(accessibleDeptIds));
	            response.setServiceResponse(projectFilterDTO);
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            logBuilder.append("\n Department list fetched successfully.");
	            
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse("Something went wrong!");
	        logBuilder.append("\n Exception occurred: ").append(e.getMessage());
	    }

	    return response;
	}
	
//	@Transactional
//	public ServiceResponse poCrudOperationsInIshine(ResourceManagementDTO poPortalProjects) {
//	    ServiceResponse serviceResponse = new ServiceResponse();
//	    StringBuilder logBuilder = new StringBuilder();
//	    logBuilder.append("api/poCrudOperationsInIshine");
//	    LogDTO apiLogInfo = new LogDTO();
//	    apiLogInfo.setLogLevel("INFO");
//
//	    if(poPortalProjects != null) {
//	    	try {
//		        if ("create".equalsIgnoreCase(poPortalProjects.getRequestType())) {
//		        	Project existingProject = projectRepository.findByPoProjectId(poPortalProjects.getId());
//		        	
//		            if (existingProject != null) {
//		            	try {
//			                ResourceManagementDTO poData = poPortalProjects;
//
//			                Project project = new Project();
//			                project.setPoProjectId(poData.getId());
//			                project.setPoNo(poData.getPoNo());
//			                project.setApmosysRM(poData.getApmosysRM());
//			                project.setApmosysRmEmail(poData.getApmosysRmEmail());
//			                project.setActive("Pending".equals(poData.getStatus()) ? "false" :
//			                                  "Completed".equals(poData.getStatus()) ? null : "true");
//			                project.setIsDraftProject(null);
//			                project.setClientRM(poData.getClientRM());
//			                project.setPoEndDate(convertIsoToDate(poData.getEndDate()));
//			                project.setPoProjectType(poData.getProjectType());
//			                project.setPoStartDate(convertIsoToDate(poData.getStartDate()));
//			                project.setProjectName(poData.getName());
//			                project.setState(poData.getClientState());
//			                project.setStatus(poData.getStatus());
//			                project.setIsRenewable(poData.getIsRenewable());
//			                project.setCreatedBy(6l);
//
//			                if (poData.getCreatedOn() != null) {
//			                	 try {
//			                	        long epochMillis = Long.parseLong(poData.getCreatedOn());
//			                	        Instant instant = Instant.ofEpochMilli(epochMillis);
//			                	        LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
//			                	        project.setCreatedOn(Timestamp.valueOf(localDateTime));
//			                	    } catch (NumberFormatException | DateTimeException e) {
//			                	        System.err.println("Failed to parse createdOn: " + poData.getCreatedOn() +
//			                	                           " | Exception: " + e.getMessage());
//			                	        project.setCreatedOn(null);
//			                	    }
//			                }
//
//			                Integer clientId = null;
//			                Optional<Client> clientOpt = clientsRepository.findByClientName(poData.getClientName());
//
//			                if (clientOpt.isPresent()) {
//			                    clientId = clientOpt.get().getClientId();
//			                } else {
//			                    Client newClient = new Client();
//			                    newClient.setClientName(poData.getClientName());
//			                    Client savedClient = clientsRepository.save(newClient);
//
//			                    if (savedClient == null) {
//			                        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
//			                        serviceResponse.setServiceResponse("Failed to add client");
//			                        apiLogInfo.setApiResponse("Failed to add client");
//			                        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			                        return serviceResponse;
//			                    } else {
//			                    	serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//			                        serviceResponse.setServiceResponse("Client added!");
//			                        apiLogInfo.setApiResponse("Client added!");
//			                        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//			                    }
//
//			                    clientId = savedClient.getClientId();
//			                    List<ClientLocation> locations = new ArrayList<>();
//
//			                    if (poData.getClientLocation() != null) {
//			                        for (String loc : poData.getClientLocation()) {
//			                            ClientLocation cl = new ClientLocation();
//			                            cl.setClientId(clientId);
//			                            cl.setClientLocation(loc);
//			                            locations.add(cl);
//			                        }
//			                    }
//
//			                    if (poData.getClientLocation() == null || !Arrays.asList(poData.getClientLocation()).contains("WFH")) {
//			                        ClientLocation cl = new ClientLocation();
//			                        cl.setClientId(clientId);
//			                        cl.setClientLocation("WFH");
//			                        locations.add(cl);
//			                    }
//
//			                    List<ClientLocation> savedLocations = clientLocationRepository.saveAll(locations);
//			                    if (savedLocations.isEmpty() || savedLocations == null) {
//			                        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
//			                        serviceResponse.setServiceResponse("Failed to add client Location");
//			                        apiLogInfo.setApiResponse("Failed to add client Location");
//			                        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			                        return serviceResponse;
//			                    } else {
//				                    serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				                    serviceResponse.setServiceResponse("Client Location added");
//				                    apiLogInfo.setApiResponse("Client Location added");
//				                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//			                    }
//			                }
//
//			                project.setClientId(clientId);
//			                project.setClientLocation(poData.getClientLocation() != null ? String.join(", ", poData.getClientLocation()) : null);
//
//			                Project savedProject = projectRepository.save(project);
//			                if (savedProject == null) {
//			                	serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				                serviceResponse.setServiceResponse("Create operation failed! " );
//				                return serviceResponse;
//			                } else {
//			                	serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				                serviceResponse.setServiceResponse("Project created successfully! " );
//			                }
//			                
//			                if("TNM".equalsIgnoreCase(savedProject.getPoProjectType())) {
//			                	if (poData.getResourceRequirements() != null && !poData.getResourceRequirements().isEmpty()) {
//				                    for (ResourceRequirementDTO req : poData.getResourceRequirements()) {
//				                        ResourceRequirement res = new ResourceRequirement();
//				                        res.setCount(req.getCount());
//				                        res.setDepartment(req.getDepartment());
//				                        res.setExperience(req.getExperience());
//				                        res.setRole(req.getRole());
//				                        res.setResourceOverviewId(req.getResourceOverviewId() != null ?
//				                            Long.parseLong(req.getResourceOverviewId().toString()) : null);
//				                        res.setProjectId(savedProject.getProjectId());
//
//				                        ResourceRequirement rr = resourceRequirementRepository.save(res);
//				                        if (rr == null) {
//				                        	serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
//							                serviceResponse.setServiceResponse("Create operation failed at saving resource requirement! " );
//							                return serviceResponse;
//				                        }else {
//				                        	serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//							                serviceResponse.setServiceResponse("Project created successfully! " );
//				                        }
//				                    }
//				                }
//			                }
//
//						    if (poData.getDepartment() != null ) {
//						        for (String deptName : poData.getDepartment()) {
//						            Department dept = departmentRepository.findByName(deptName);
//						            if (dept != null) {
//						                ProjectDepartmentMap pdMap = new ProjectDepartmentMap();
//						                pdMap.setProjectId(savedProject.getProjectId());
//						                pdMap.setDeptId(dept.getDeptId());
//						                ProjectDepartmentMap pdm = projectDepartmentMapRepository.save(pdMap);
//						                if(pdm == null) {
//						                	serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
//							                serviceResponse.setServiceResponse("Create operation failed at saving departments! " );
//							                return serviceResponse;
//						                }else {
//						                	serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//							                serviceResponse.setServiceResponse("Project created successfully! " );
//						                }
//						                logBuilder.append("Mapped department: ").append(deptName).append("\n");
//						            } else {
//						            	serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
//						                serviceResponse.setServiceResponse("Create operation failed at saving departments! " );
//						                logBuilder.append("Unmapped department: ").append(deptName).append("\n");
//						                return serviceResponse;
//						            }
//						        }
//						    }
//				
//							if (poData.getFcLineItemDtoList() != null && !poData.getFcLineItemDtoList().isEmpty()) {
//						        for (FCLineItemDTO lineItemDTO : poData.getFcLineItemDtoList()) {
//				
//						            FCLineItem lineItem = new FCLineItem();
//						            lineItem.setId(lineItemDTO.getId());
//						            lineItem.setPoId(lineItemDTO.getPoId());
//						            lineItem.setProjectId(Long.parseLong(savedProject.getProjectId().toString()));
//						            lineItem.setPoProjectId(lineItemDTO.getProjectId());
//						            lineItem.setName(lineItemDTO.getName());
//						            lineItem.setStatus(lineItemDTO.getStatus());
//						            FCLineItem savedLineItem = fCLineItemRepository.save(lineItem);
//						            if(savedLineItem == null) {
//						            	serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
//						                serviceResponse.setServiceResponse("Create operation failed at saving departments! " );
//						                logBuilder.append("Failed to save lineItem for projectId : ").append(savedLineItem.getProjectId()).append("\n");
//						                return serviceResponse;
//						            } else {
//						            	serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//						                serviceResponse.setServiceResponse("Project created successfully! " );
//						            	logBuilder.append("Saved LineItem Successfully for projectId : ").append(savedLineItem.getProjectId()).append("\n");
//				            		}
//						            if (lineItemDTO.getMilestones() != null && !lineItemDTO.getMilestones().isEmpty()) {
//						                for (FCProjectMilestoneDTO milestoneDTO : lineItemDTO.getMilestones()) {
//						                    FCProjectMilestone milestone = new FCProjectMilestone();
//						                    milestone.setId(milestoneDTO.getId());
//						                    milestone.setPoId(milestoneDTO.getPoId());
//						                    milestone.setPoProjectId(milestoneDTO.getProjectId());
//						                    milestone.setProjectId(Long.parseLong(savedProject.getProjectId().toString()));
//						                    milestone.setName(milestoneDTO.getName());
//						                    milestone.setDescription(milestoneDTO.getDescription());
//						                    milestone.setStartDate(milestoneDTO.getStartDate());
//						                    milestone.setEndDate(milestoneDTO.getEndDate());
//						                    milestone.setStatus(milestoneDTO.getStatus());
//						                    milestone.setRemarks(milestoneDTO.getRemarks());
//						                    milestone.setLineItemId(milestoneDTO.getLineItemId());
//						                    milestone.setUpdatedBy(6L); 
//						                    milestone.setUpdatedOn(LocalDateTime.now());
//						                    
//						                    FCProjectMilestone savedMilestone = fCProjectMilestoneRepository.save(milestone);
//								            if(savedMilestone == null) {
//								                logBuilder.append("Failed to save lineItem for projectId : ").append(savedMilestone.getProjectId()).append("\n");
//								                serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
//								                serviceResponse.setServiceResponse("Create operation failed at saving departments! " );
//								                return serviceResponse;
//								            } else {
//								            	serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//								                serviceResponse.setServiceResponse("Project created successfully! " );
//								                logBuilder.append("Saved LineItem Successfully for projectId : ").append(savedMilestone.getProjectId()).append("\n");
//								            }
//						                }
//						            }
//						        }
//						    }
//			            } catch (Exception e) {
//			                serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
//			                serviceResponse.setServiceResponse("Create operation failed: " + e.getMessage());
//			                logBuilder.append("Create operation failed!");
//			                System.err.println("Create operation failed: " + e.getMessage());
//			                return serviceResponse;
//			            }
//		            } else  {
//		            	serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
//		                serviceResponse.setServiceResponse("Project is already created in ishine!");
//		                logBuilder.append("Project is already created in ishine! For PO Project Id : " + poPortalProjects.getId());
//		                System.err.println("Project is already created in ishine! For PO Project Id : " + poPortalProjects.getId());
//		                return serviceResponse;
//		            }
//				} else if ("Update".equalsIgnoreCase(poPortalProjects.getRequestType())) {
//					
//				    try {
//				    	Project existingProject = projectRepository.findByPoProjectId(poPortalProjects.getId());
//
//				    	if (existingProject == null) {
//				    	    System.out.println("No project found with PoProjectId: " + poPortalProjects.getId());
//				    	    serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				    	    serviceResponse.setServiceResponse(poPortalProjects);
//				    	    return serviceResponse;
//				    	}
//
//				    	boolean isModified = false;
//
//				    	Map<Boolean, Runnable> updateChecks = Map.of(
//				    	    !Objects.equals(existingProject.getPoNo(), poPortalProjects.getPoNo()), () -> existingProject.setPoNo(poPortalProjects.getPoNo()),
//				    	    !Objects.equals(existingProject.getApmosysRM(), poPortalProjects.getApmosysRM()), () -> existingProject.setApmosysRM(poPortalProjects.getApmosysRM()),
//				    	    !Objects.equals(existingProject.getApmosysRmEmail(), poPortalProjects.getApmosysRmEmail()), () -> existingProject.setApmosysRmEmail(poPortalProjects.getApmosysRmEmail()),
//				    	    !Objects.equals(existingProject.getClientRM(), poPortalProjects.getClientRM()), () -> existingProject.setClientRM(poPortalProjects.getClientRM()),
//				    	    !Objects.equals(existingProject.getPoProjectType(), poPortalProjects.getProjectType()), () -> existingProject.setPoProjectType(poPortalProjects.getProjectType()),
//				    	    !Objects.equals(existingProject.getProjectName(), poPortalProjects.getName()), () -> existingProject.setProjectName(poPortalProjects.getName()),
//				    	    !Objects.equals(existingProject.getState(), poPortalProjects.getClientState()), () -> existingProject.setState(poPortalProjects.getClientState()),
//				    	    !Objects.equals(existingProject.getStatus(), poPortalProjects.getStatus()), () -> existingProject.setStatus(poPortalProjects.getStatus())
//				    	);
//
//				    	for (Map.Entry<Boolean, Runnable> entry : updateChecks.entrySet()) {
//				    	    if (entry.getKey()) {
//				    	        entry.getValue().run();
//				    	        isModified = true;
//				    	    }
//				    	}
//
//				    	String newActive = (poPortalProjects.getStatus() != null && !poPortalProjects.getStatus().equals("Pending")) ? "true" : (poPortalProjects.getStatus().equals("Completed") ? "false" : null);
//				    	if (!Objects.equals(existingProject.getActive(), newActive)) {
//				    	    existingProject.setActive(newActive);
//				    	    isModified = true;
//				    	}
//
//				    	String endDate = convertIsoToDate(poPortalProjects.getEndDate());
//				    	if (!Objects.equals(existingProject.getPoEndDate(), endDate)) {
//				    	    existingProject.setPoEndDate(endDate);
//				    	    isModified = true;
//				    	}
//
//				    	String startDate = convertIsoToDate(poPortalProjects.getStartDate());
//				    	if (!Objects.equals(existingProject.getPoStartDate(), startDate)) {
//				    	    existingProject.setPoStartDate(startDate);
//				    	    isModified = true;
//				    	}
//
//				    	String joinedLocations = poPortalProjects.getClientLocation() != null ? String.join(", ", poPortalProjects.getClientLocation()) : null;
//				    	if (!Objects.equals(existingProject.getClientLocation(), joinedLocations)) {
//				    	    existingProject.setClientLocation(joinedLocations);
//				    	    isModified = true;
//				    	}
//
//				    	String createdInput = poPortalProjects.getCreatedOn();
//				    	if (createdInput != null) {
//				    	    ZonedDateTime zdt = ZonedDateTime.parse(createdInput);
//				    	    Timestamp parsedCreatedOn = Timestamp.valueOf(zdt.toLocalDateTime());
//				    	    if (!Objects.equals(existingProject.getCreatedOn(), parsedCreatedOn)) {
//				    	        existingProject.setCreatedOn(parsedCreatedOn);
//				    	        isModified = true;
//				    	    }
//				    	}
//
//				    	ServiceResponse clientUpdateResponse = updateClient(existingProject, poPortalProjects);
//				    	if (ServiceResponse.STATUS_FAIL.equals(clientUpdateResponse.getServiceStatus())) return clientUpdateResponse;
//				    	isModified = true;
//
//				    	existingProject.setUpdatedBy(6L);
//				    	existingProject.setUpdatedOn(LocalDateTime.now());
//
//				    	if (isModified) {
//				    	    projectRepository.save(existingProject);
//				    	}
//
//				    	ServiceResponse deptMapResponse = syncProjectDepartments(existingProject, poPortalProjects);
//				    	if (ServiceResponse.STATUS_FAIL.equals(deptMapResponse.getServiceStatus())) return deptMapResponse;
//
//				    	ServiceResponse reqResponse = syncResourceRequirements(existingProject, poPortalProjects);
//				    	if (ServiceResponse.STATUS_FAIL.equals(reqResponse.getServiceStatus())) return reqResponse;
//
//				    	ServiceResponse lineItemResponse = syncLineItemsAndMilestones(existingProject, poPortalProjects);
//				    	if (ServiceResponse.STATUS_FAIL.equals(lineItemResponse.getServiceStatus())) return lineItemResponse;
//
//				    	serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				    	serviceResponse.setServiceResponse("Project updated successfully!");
//				    	return serviceResponse;
//				    	
//				    }catch (Exception e) {
//		                serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
//		                serviceResponse.setServiceResponse("Update operation failed: " + e.getMessage());
//		                System.err.println(e.getMessage());
//		                return serviceResponse;
//		            }
//				    
//				} else if("Delete".equalsIgnoreCase(poPortalProjects.getRequestType())) {
//					
//				    try {
//				    	Project existingProject = projectRepository.findByPoProjectId(poPortalProjects.getId());
//		
//		                if (existingProject != null) {
//		                    existingProject.setActive("false");
//		                    existingProject.setUpdatedBy(6l);
//		                    Project inactiveProj = projectRepository.save(existingProject);
//		
//		                    List<ProjectManagerMapping> activePMs = projectManagerMappingRepository.findByProjectIdAndActive(Long.parseLong(existingProject.getProjectId().toString()), 1);
//		                    for (ProjectManagerMapping pm : activePMs) {
//		                        pm.setActive(0);
//		                        pm.setUpdatedBy(6l);
//		                        pm.setUpdatedOn(LocalDateTime.now());
//		                    }
//		
//		                    List<ProjectManagerMapping> inactivePM = projectManagerMappingRepository.saveAll(activePMs);
//		                    
//		                    if(inactivePM.isEmpty() || inactivePM != null) {
//		                    	logBuilder.append("Project manager mappings made inactive Successfully in Ishine");
//		                    	System.out.println("Project manager mappings made inactive Successfully in Ishine");
//		                    }
//		
//		                    List<ProjectOverheadMapping> activeOverheads = projectOverheadMappingRepository.findByProjectIdAndActive(Long.parseLong(existingProject.getProjectId().toString()), 1);
//		                    for (ProjectOverheadMapping oh : activeOverheads) {
//		                        oh.setActive(0);
//		                        oh.setUpdatedBy(6l);
//		                        oh.setUpdatedOn(LocalDateTime.now());
//		                    }
//		
//		                    List<ProjectOverheadMapping> inactivePO = projectOverheadMappingRepository.saveAll(activeOverheads);
//		                    
//		                    if(inactivePO.isEmpty() || inactivePO != null) {
//		                    	logBuilder.append("Project overhead mappings made inactive Successfully in Ishine");
//		                    	System.out.println("Project overhead mappings made inactive Successfully in Ishine");
//		                    }
//		
//			                if(inactiveProj != null) {
//		
//			                    System.out.println("Project updated successfully with PoProjectId: " + poPortalProjects.getId());
//			                    logBuilder.append("Project updated successfully with PoProjectId: ")
//			                    .append(poPortalProjects.getId());
//			                    serviceResponse.setServiceResponse("Project made inactive Successfully in Ishine");
//				                serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//		                    }
//			                
//		                } else {
//		                	
//		                    System.out.println("Project not found in Ishine Portal for PoProjectID: " + poPortalProjects.getId());
//		                    logBuilder.append("Project not found in Ishine Portal for PoProjectID: ")
//		                    .append(poPortalProjects.getId());
//		                    serviceResponse.setServiceResponse("Project not found in Ishine Portal!");
//			                serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
//			                
//		                }
//		            } catch (Exception e) {
//		                serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
//		                serviceResponse.setServiceResponse("Delete operation failed: " + e.getMessage());
//		                return serviceResponse;
//		            }
//		        } else {
//		            serviceResponse.setServiceResponse("No Valid request type found! ");
//		            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
//		            System.out.println("No Valid request type found! ");
//		            logBuilder.append("No Valid request type found! ");
//	            }
//		    } catch (Exception e) {
//		        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
//		        serviceResponse.setServiceResponse("Unexpected error: " + e.getMessage());
//	            System.out.println("Unexpected error:  "+ e.getMessage());
//	            logBuilder.append("Unexpected error:  "+ e.getMessage());
//            }
//	    } else {
//	    	logBuilder.append("No data recieved from Shankh");
//	    	serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
//	        serviceResponse.setServiceResponse("No data recieved from Shankh");
//    	}
//
//	    return serviceResponse;
//	}
	
	// public static String convertIsoToDate(String dateString) {
	// 	StringBuilder logBuilder = new StringBuilder();
	//     try {
	//         OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateString);
	//         return offsetDateTime.toLocalDate().toString();
	//     } catch (DateTimeParseException isoEx) {
	//         try {
	//             long epochMillis = Long.parseLong(dateString);
	//             Instant instant = Instant.ofEpochMilli(epochMillis);
	//             return instant.atZone(ZoneId.systemDefault()).toLocalDate().toString();
	//         } catch (NumberFormatException | DateTimeException epochEx) {
	//             logBuilder.append("Failed to parse date: ").append(dateString)
	//                       .append(" | Exception: ").append(epochEx.getClass().getSimpleName())
	//                       .append(" - ").append(epochEx.getMessage());
	//             System.err.println(logBuilder.toString());
	//             return null;
	//         }
	//     } catch (NullPointerException npe) {
	//         logBuilder.append("Date string is null");
	//         System.err.println(logBuilder.toString());
	//         return null;
	//     }
    // }
	
	private ServiceResponse updateClient(Project project, ResourceManagementDTO dto) {
	    Integer clientId = project.getClientId();
	    Client currentClient = clientId != null ? clientsRepository.findById(clientId).orElse(null) : null;

	    if (currentClient == null || !Objects.equals(currentClient.getClientName(), dto.getClientName())) {
	        Client newClient = clientsRepository.findByClientName(dto.getClientName()).orElseGet(() -> {
	            Client client = new Client();
	            client.setClientName(dto.getClientName());
	            return clientsRepository.save(client);
	        });

	        if (newClient == null) {
	            ServiceResponse response = new ServiceResponse();
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Failed to update the new client!");
	            return response;
	        }

	        project.setClientId(newClient.getClientId());
	        updateClientLocations(newClient.getClientId(), dto.getClientLocation());
	    }
	    ServiceResponse response = new ServiceResponse();
	    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	    response.setServiceResponse("Client updated successfully!");
	    return response;
	}

	private void updateClientLocations(Integer clientId, String[] locations) {
	    List<ClientLocation> existing = clientLocationRepository.findByClientId(clientId);
	    Set<String> currentLocSet = existing.stream().map(ClientLocation::getClientLocation).collect(Collectors.toSet());
	    Set<String> newLocSet = new HashSet<>(Arrays.asList(locations));

	    if (!currentLocSet.equals(newLocSet)) {
	        clientLocationRepository.deleteAll(existing);
	        List<ClientLocation> newLocs = newLocSet.stream()
	            .map(loc -> {
	                ClientLocation cl = new ClientLocation();
	                cl.setClientId(clientId);
	                cl.setClientLocation(loc);
	                return cl;
	            })
	            .collect(Collectors.toList());
	        clientLocationRepository.saveAll(newLocs);
	    }
	}

	private ServiceResponse syncProjectDepartments(Project project, ResourceManagementDTO dto) {
	    List<ProjectDepartmentMap> existing = projectDepartmentMapRepository.findByProjectId(project.getProjectId());
	    Set<Long> existingIds = existing.stream().map(ProjectDepartmentMap::getDeptId).collect(Collectors.toSet());
	    Set<Long> newIds = Arrays.stream(dto.getDepartment())
	        .map(name -> departmentRepository.findByName(name))
	        .filter(Objects::nonNull)
	        .map(Department::getDeptId)
	        .collect(Collectors.toSet());

	    if (!existingIds.equals(newIds)) {
	        projectDepartmentMapRepository.deleteAll(existing);
	        List<ProjectDepartmentMap> newMaps = newIds.stream()
	            .map(id -> {
	                ProjectDepartmentMap pdm = new ProjectDepartmentMap();
	                pdm.setDeptId(id);
	                pdm.setProjectId(project.getProjectId());
	                return pdm;
	            }).collect(Collectors.toList());
	        projectDepartmentMapRepository.saveAll(newMaps);
	    }
	    ServiceResponse response = new ServiceResponse();
	    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	    response.setServiceResponse("Department mappings updated.");
	    return response;
	}
	
	private ServiceResponse syncResourceRequirements(Project project, ResourceManagementDTO dto) {
	    Map<Long, ResourceRequirementDTO> existingMap = resourceRequirementRepository.findByProjectId(project.getProjectId()).stream()
	        .filter(req -> req.getResourceOverviewId() != null)
	        .collect(Collectors.toMap(ResourceRequirementDTO::getResourceOverviewId, Function.identity()));

	    for (ResourceRequirementDTO r : dto.getResourceRequirements()) {
	        Long id = r.getResourceOverviewId();
	        if (id == null) continue;
	        ResourceRequirementDTO existing = existingMap.get(id);

	        boolean changed = existing == null ||
	            !Objects.equals(existing.getCount(), r.getCount()) ||
	            !Objects.equals(existing.getDepartment(), r.getDepartment()) ||
	            !Objects.equals(existing.getExperience(), r.getExperience()) ||
	            !Objects.equals(existing.getRole(), r.getRole());

	        if (changed) {
	            ResourceRequirement entity = new ResourceRequirement();
	            entity.setProjectId(project.getProjectId());
	            entity.setResourceOverviewId(id);
	            entity.setCount(r.getCount());
	            entity.setDepartment(r.getDepartment());
	            entity.setExperience(r.getExperience());
	            entity.setRole(r.getRole());
	            resourceRequirementRepository.save(entity);
	        }
	    }
	    ServiceResponse response = new ServiceResponse();
	    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	    response.setServiceResponse("Resource requirements synced.");
	    return response;
	}

	private ServiceResponse syncLineItemsAndMilestones(Project project, ResourceManagementDTO dto) {
	    for (FCLineItemDTO lineItemDTO : dto.getFcLineItemDtoList()) {
	        FCLineItem lineItem = Optional.ofNullable(lineItemDTO.getId())
	            .flatMap(fCLineItemRepository::findById).orElse(new FCLineItem());

	        boolean changed = !Objects.equals(lineItem.getPoId(), lineItemDTO.getPoId()) ||
	                          !Objects.equals(lineItem.getPoProjectId(), lineItemDTO.getProjectId()) ||
	                          !Objects.equals(lineItem.getName(), lineItemDTO.getName()) ||
	                          !Objects.equals(lineItem.getStatus(), lineItemDTO.getStatus());

	        if (changed) {
	            lineItem.setPoId(lineItemDTO.getPoId());
	            lineItem.setProjectId(Long.parseLong(project.getProjectId().toString()));
	            lineItem.setPoProjectId(lineItemDTO.getProjectId());
	            lineItem.setName(lineItemDTO.getName());
	            lineItem.setStatus(lineItemDTO.getStatus());
	            fCLineItemRepository.save(lineItem);
	        }

	        for (FCProjectMilestoneDTO milestoneDTO : lineItemDTO.getFcProjectMilestoneDTOList()) {
	            FCProjectMilestone milestone = Optional.ofNullable(milestoneDTO.getId())
	                .flatMap(fCProjectMilestoneRepository::findById).orElse(new FCProjectMilestone());

	            boolean msChanged = !Objects.equals(milestone.getName(), milestoneDTO.getName()) ||
	                                !Objects.equals(milestone.getDescription(), milestoneDTO.getDescription()) ||
	                                !Objects.equals(milestone.getStartDate(), milestoneDTO.getStartDate()) ||
	                                !Objects.equals(milestone.getEndDate(), milestoneDTO.getEndDate()) ||
	                                !Objects.equals(milestone.getStatus(), milestoneDTO.getStatus()) ||
	                                !Objects.equals(milestone.getRemarks(), milestoneDTO.getRemarks());

	            if (msChanged) {
	                milestone.setPoId(milestoneDTO.getPoId());
	                milestone.setPoProjectId(milestoneDTO.getPoProjectId());
	                milestone.setProjectId(Long.parseLong(project.getProjectId().toString()));
	                milestone.setLineItemId(lineItem.getId());
	                milestone.setName(milestoneDTO.getName());
	                milestone.setDescription(milestoneDTO.getDescription());
	                milestone.setStartDate(milestoneDTO.getStartDate());
	                milestone.setEndDate(milestoneDTO.getEndDate());
	                milestone.setStatus(milestoneDTO.getStatus());
	                milestone.setRemarks(milestoneDTO.getRemarks());
	                milestone.setUpdatedBy(6L);
	                milestone.setUpdatedOn(LocalDateTime.now());
	                fCProjectMilestoneRepository.save(milestone);
	            }
	        }
	    }
	    ServiceResponse response = new ServiceResponse();
	    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	    response.setServiceResponse("Line items and milestones synced.");
	    return response;
	}
	
	@Transactional(rollbackOn = Exception.class)
	public ServiceResponse createDraftProjectInfo(ResourceManagementDTO dto) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO log = initializeLog(dto);
	    StringBuilder logBuilder = new StringBuilder();

	    try {
	        logBuilder.append("ProjectType: ").append(dto.getProjectType())
	                  .append(", ProjectId: ").append(dto.getProjectId())
	                  .append(", ProjectName: ").append(dto.getName())
	                  .append(", Department: ").append(dto.getDeptName())
	                  .append(", State: ").append(dto.getClientState());

	        
	        
	        Project existingProject = new Project();
	        if(dto.getProjectId() != null && dto.getProjectType() != null && dto.getId() != null) {
	        existingProject = fetchExistingProject(dto);}

	        if (existingProject != null) {
	            return handleExistingProject(existingProject, dto, log);
	        }

	        Integer clientId = resolveOrCreateClient(dto, response, log);
	        if (clientId == null) return response;

	        Project newProject = createAndSaveNewProject(dto, clientId);
//	        updateProjectTempStatus(dto);

	        mapDepartmentsToProject(dto, newProject);
	        handleProjectManagersAndOverheads(dto, newProject, response);
	        handleResourceRequirements(dto, newProject);
	        handleTeamCreation(dto, newProject, response, log);

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("Project created successfully");
	        log.setApiResponse("Project created successfully");
	        log.setApiStatus(ServiceResponse.STATUS_SUCCESS);

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something Went Wrong.");
	        response.setServiceError(e.getMessage());
	        log.setApiStatus(ServiceResponse.STATUS_FAIL);
	        log.setLogLevel("ERROR");
	    }

	    log.setApiRequest(logBuilder.toString());
	    logService.logMyInfo(httpRequest, log);
	    return response;
	}

	//  HELPER METHODS ///////////////////////////////////////////////////////////

	private LogDTO initializeLog(ResourceManagementDTO dto) {
	    LogDTO log = new LogDTO();
	    log.setSubFeatureName("createDraftProjectInfo");
	    log.setApiUrl("/api/createDraftProjectInfo");
	    log.setLogLevel("INFO");
	    return log;
	}

	private Project fetchExistingProject(ResourceManagementDTO dto) {
	    return "Internal".equalsIgnoreCase(dto.getProjectType())
	            ? projectRepository.findByProjectId(dto.getProjectId())
	            : projectRepository.findByPoProjectId(dto.getId());
	}

	private ServiceResponse handleExistingProject(Project project, ResourceManagementDTO dto, LogDTO log) {
	    ServiceResponse response = this.projectIsPresent(project, dto, new ArrayList<>());
	    if (!"Success".equals(response.getServiceStatus())) {
	        log.setApiResponse("Project not updated successfully");
	        log.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	    } else {
	        log.setApiResponse("Project updated successfully");
	        log.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	    }
	    return response;
	}

	private Integer resolveOrCreateClient(ResourceManagementDTO dto, ServiceResponse response, LogDTO log) {   
	    Optional<Client> optionalClient = clientsRepository.findByClientName(dto.getClientName());
	    if (optionalClient.isPresent()) return optionalClient.get().getClientId();

	    Client newClient = new Client();
	    newClient.setClientName(dto.getClientName());
	    Client savedClient = clientsRepository.save(newClient);
	    if (savedClient == null) {
	        failResponse(response, log, "Failed to add client");
	        return null;
	    }

	    List<ClientLocation> locations = Arrays.stream(dto.getClientLocation())
	            .map(loc -> new ClientLocation(savedClient.getClientId(), loc))
	            .collect(Collectors.toList());

	    if (!Arrays.asList(dto.getClientLocation()).contains("WFH")) {
	        locations.add(new ClientLocation(savedClient.getClientId(), "WFH"));
	    }

	    List<ClientLocation> savedLocations = clientLocationRepository.saveAll(locations);
	    if (savedLocations.isEmpty()) {
	        failResponse(response, log, "Failed to add client Location");
	        return null;
	    }

	    return savedClient.getClientId();
	}

//	private ServiceResponse failResponse(ServiceResponse response, LogDTO log, String message) {
//	    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//	    response.setServiceResponse(message);
//	    log.setApiResponse(message);
//	    log.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//		return response;
//	}

	private Project createAndSaveNewProject(ResourceManagementDTO dto, Integer clientId) {
	    Project p = new Project();
	    p = projectRepository.findByPoProjectId(dto.getId());
	    p.setProjectName(dto.getName());
	    p.setState(dto.getState());
	    p.setClientId(clientId);
	    p.setPoProjectId(dto.getId());
	    p.setActive("true");
	    p.setSyncProject("true");
	    p.setPoProjectType("Internal".equalsIgnoreCase(dto.getProjectType()) ? null : dto.getProjectType());
	    p.setPoNo(dto.getPoNo());
	    p.setPoStartDate(dto.getPoStartDate());
	    p.setPoEndDate(dto.getPoEndDate());
	    p.setCreatedOn(new Timestamp(System.currentTimeMillis()));
	    p.setApmosysRM(dto.getApmosysRM());
	    p.setIsRenewable(dto.getIsRenewable());
	    p.setClientRM(dto.getClientRM());
	    p.setApmosysRmEmail(dto.getApmosysRmEmail());
	    p.setIsDraftProject("true");
	    p.setCreatedBy(dto.getCreatedBy());
	    return projectRepository.save(p);
	}

//	private void updateProjectTempStatus(ResourceManagementDTO dto) {
//	    if (!"Internal".equalsIgnoreCase(dto.getProjectType())) {
//	        ProjectTemp projectTemp = projectTempRepo.findByPoProjectId(dto.getId());
//	        if (projectTemp != null) {
//	            projectTemp.setIsDraftProject("Pending");
//	            projectTempRepo.save(projectTemp);
//	        }
//	    }
//	}

	private void mapDepartmentsToProject(ResourceManagementDTO dto, Project project) {
	    for (String deptName : dto.getDepartment()) {
	        Department dept = departmentRepository.findByName(deptName);
	        if (dept != null && projectDepartmentMapRepository
	                .findByProjectIdAndDeptId(project.getProjectId(), dept.getDeptId()) == null) {
	            ProjectDepartmentMap map = new ProjectDepartmentMap();
	            map.setProjectId(project.getProjectId());
	            map.setDeptId(dept.getDeptId());
	            projectDepartmentMapRepository.save(map);
	        }
	    }
	}

	private void handleProjectManagersAndOverheads(ResourceManagementDTO dto, Project project, ServiceResponse response) {
	    ServiceResponse managerResp = this.setProjectManager(dto, project);
	    if (!"Success".equals(managerResp.getServiceStatus())) {
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse(managerResp.getServiceResponse());
	        throw new DataNotFoundException("Unable to set Project Manager");
	    }

	    ServiceResponse overheadResp = this.setProjectOverheads(dto, project);
	    if (!"Success".equals(overheadResp.getServiceStatus())) {
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse(overheadResp.getServiceResponse());
	        throw new DataNotFoundException("Unable to set Project Over Head");
	    }
	}

	private void handleResourceRequirements(ResourceManagementDTO dto, Project project) {
	    if (dto.getResourceRequirements() != null && !dto.getResourceRequirements().isEmpty()) {
	        for (ResourceRequirementDTO req : dto.getResourceRequirements()) {
	            if (req.getResourceOverviewId() != null &&
	                resourceRequirementRepository.existsById(req.getResourceOverviewId())) {
	                continue;
	            }
	            ResourceRequirement r = new ResourceRequirement();
	            r.setCount(req.getCount());
	            r.setDepartment(req.getDepartment());
	            r.setExperience(req.getExperience());
	            r.setRole(req.getRole());
	            r.setResourceOverviewId(req.getResourceOverviewId() != null ?
	                    Long.parseLong(req.getResourceOverviewId().toString()) : null);
	            r.setProjectId(project.getProjectId());
	            resourceRequirementRepository.save(r);
	        }
	    }
	}

	private void handleTeamCreation(ResourceManagementDTO dto, Project project, ServiceResponse response, LogDTO log) {
	    if (dto.getTeamList() == null || dto.getTeamList().isEmpty()) {
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse("Team list is null or empty");
	        log.setApiResponse("Team list is null or empty");
	        return;
	    }

	    for (TeamDTO teamDTO : dto.getTeamList()) {
	        Team team = createTeam(teamDTO, project, dto.getCreatedBy());
	        List<EmployeeTeamMap> mappings = mapTeamMembers(teamDTO, team, dto.getCreatedBy());
	        sendTeamCreationEmail(team, mappings);

	        if (!mappings.isEmpty()) {
	            List<Long> defaultEmpIds = getDefaultProjectEmpIds(mappings);
	            if (!defaultEmpIds.isEmpty()) {
	                ServiceResponse defaultResp = this.handleDefaultProjectUpdate(defaultEmpIds,
	                        project.getProjectId(), dto.getCreatedBy(), new StringBuilder());
	                response.setServiceStatus(defaultResp.getServiceStatus());
	                response.setServiceResponse(defaultResp.getServiceResponse());
	            }

	            ServiceResponse activityResponse = this.activityRealtedToTeam(mappings, teamDTO, dto, team);
	            if (!"Success".equals(activityResponse.getServiceStatus())) {
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("Team created, but default activities failed");
	                log.setApiResponse("Team created, but default activities failed");
	                throw new RuntimeException("Team created, but default activities failed");
	            } else {
	                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                response.setServiceResponse("Team created successfully");
	                log.setApiResponse("Team created successfully");
	            }
	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Unable to map any team member");
	            log.setApiResponse("Unable to map any team member");
	            throw new RuntimeException("Unable to map any team member");
	        }
	    }
	}

	private Team createTeam(TeamDTO teamDTO, Project project, Long createdBy) {
	    String deptIds = String.join(",", teamDTO.getDepartmentList());

	    Team team = new Team();
	    team.setTeamName(teamDTO.getTeamName());
	    team.setProjectId(project.getProjectId());
	    team.setTeamLeadId(getTeamLeadId(teamDTO));
	    team.setTeamLeadName(getTeamLeadName(teamDTO));
	    team.setDeptIds(deptIds);
	    team.setIsActive("Y");
	    team.setSpocId(teamDTO.getSpocId());
	    team.setCreatedBy(createdBy);
	    team.setCreatedOn(new Timestamp(System.currentTimeMillis()));
	    return teamRepository.save(team);
	}

	private List<EmployeeTeamMap> mapTeamMembers(TeamDTO teamDTO, Team team, Long createdBy) {
	    List<EmployeeTeamMap> mappings = new ArrayList<>();

	    for (TeamMemberDTO member : teamDTO.getTeamMemberList()) {
	        EmployeeTeamMap map = new EmployeeTeamMap();
	        map.setTeamId(team.getTeamId());
	        map.setEmpId(member.getEmpId());
	        map.setStartDate(LocalDateTime.now());
	        map.setCreatedBy(member.getCreatedBy() != null ? member.getCreatedBy() : createdBy);
	        map.setCreatedOn(new Timestamp(System.currentTimeMillis()));
	        map.setIsShadow(member.getIsShadow());

	        if ("true".equalsIgnoreCase(member.getIsTeamLead())) {
	            map.setActive(2L);
	            map.setEmployeeRole("TeamLead");
	        } else {
	            String empRoles = String.join(",", member.getEmployeeRole());
	            map.setEmployeeRole(empRoles);
	            map.setActive(2L);
	        }

	        if (member.getResourceOverviewId() != null) {
	            map.setResourceOverviewId(Long.parseLong(member.getResourceOverviewId().toString()));
	        }

	        mappings.add(map);
	    }

	    return employeeTeamMapRepository.saveAll(mappings);
	}

	private List<Long> getDefaultProjectEmpIds(List<EmployeeTeamMap> mappings) {
	    return mappings.stream()
	            .map(EmployeeTeamMap::getEmpId)
	            .filter(Objects::nonNull)
	            .distinct()
	            .collect(Collectors.toList());
	}

	private void sendTeamCreationEmail(Team team, List<EmployeeTeamMap> mappings) {
	    StringBuilder emailBody = new StringBuilder();
	    emailBody.append("<html><body>")
	            .append("Dear RMG,<br><br>")
	            .append("The Team has been created with the team name - <b>")
	            .append(team.getTeamName()).append("</b><br><br>")
	            .append("<table border='1' style='border-collapse: collapse; width: 100%;'>")
	            .append("<tr>")
	            .append("<th style='padding: 8px;'>Employment ID</th>")
	            .append("<th style='padding: 8px;'>Employee Name</th>")
	            .append("<th style='padding: 8px;'>Job Role</th>")
	            .append("<th style='padding: 8px;'>Department</th>")
	            .append("<th style='padding: 8px;'>Start Date</th>")
	            .append("</tr>");

	    for (EmployeeTeamMap mapping : mappings) {
	        List<EmployeeDetailsForTeamMemberDTO> employeeDetails = employeeRepository
	                .getEmployeeDetailsForTeam(mapping.getEmpId());
	        for (EmployeeDetailsForTeamMemberDTO dto : employeeDetails) {
	            String empIdPrefix = "A-";
	            if ("true".equalsIgnoreCase(dto.getIsConsultant())) {
	                empIdPrefix = "CS-";
	            }

	            emailBody.append("<tr>")
	                    .append("<td>").append(empIdPrefix).append(dto.getEmployeementId()).append("</td>")
	                    .append("<td>").append(dto.getEmployeeName()).append("</td>")
	                    .append("<td>").append(dto.getJobRoleName()).append("</td>")
	                    .append("<td>").append(dto.getDeptName()).append("</td>")
	                    .append("<td>").append(dto.getStartDate()).append("</td>")
	                    .append("</tr>");
	        }
	    }

	    emailBody.append("</table><br><br>")
	            .append("Sincerely,<br><b>Team RMG - ApMoSys Technologies</b>")
	            .append("</body></html>");

	    try {
	        String ccMails = getAllHodMails(mappings);
	        mailService.sendMailWithCC(ccMails, rmgMail, "Regarding Team Creation", emailBody.toString());
	    } catch (MessagingException e) {
	        e.printStackTrace();
	    }
	}

	private Long getTeamLeadId(TeamDTO teamDTO) {
	    return teamDTO.getTeamMemberList().stream()
	            .filter(m -> "true".equalsIgnoreCase(m.getIsTeamLead()))
	            .map(TeamMemberDTO::getEmpId)
	            .findFirst()
	            .orElse(null);
	}

	private String getTeamLeadName(TeamDTO teamDTO) {
	    return teamDTO.getTeamMemberList().stream()
	            .filter(m -> "true".equalsIgnoreCase(m.getIsTeamLead()))
	            .map(TeamMemberDTO::getName)
	            .findFirst()
	            .orElse(null);
	}

	private String getAllHodMails(List<EmployeeTeamMap> mappings) {
	    return mappings.stream()
	            .map(m -> employeeRepository.findHodMail(m.getEmpId()))
	            .filter(Objects::nonNull)
	            .distinct()
	            .collect(Collectors.joining(","));
	}

	
	public String convertIsoToDate(String dateString) {
	    StringBuilder logInfo = new StringBuilder();
	    try {
	        if (dateString.matches("\\d+")) {  // it's a timestamp
	            long millis = Long.parseLong(dateString);
	            Instant instant = Instant.ofEpochMilli(millis);
	            return instant.atZone(ZoneId.systemDefault()).toLocalDate().toString();
	        } else {
	            OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateString);
	            return offsetDateTime.toLocalDate().toString();
	        }
	    } catch (Exception e) {
	        logInfo.append("Failed to parse date: ").append(dateString)
	            .append(" | Exception: ").append(e.getClass().getSimpleName())
	            .append(" - ").append(e.getMessage());
	        System.err.println(logInfo.toString());
	        return null;
	    }
	}

	@Transactional(rollbackOn = Exception.class)
	public ServiceResponse deleteProjectTemp() {
		ServiceResponse response = new ServiceResponse();
		try {
		projectTempRepo.deleteAll();
		try {
			dumpPODataInIshine();
//			Thread.sleep(5000);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		try {
			projectRepository.callSyncProjectsSP();
//			Thread.sleep(5000);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		try {
			fillDepartmentforAllProjectsInIshine();
//			Thread.sleep(5000);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		response.setServiceStatus(response.STATUS_SUCCESS);
		System.out.println(response);
		}
		catch(Exception e) {
			response.setServiceStatus(response.STATUS_FAIL);
			response.setServiceResponse(e.getMessage());
			e.printStackTrace();
			}
		return response;
	}
	
	@Transactional(rollbackOn = Exception.class)
	public ServiceResponse poCrudOperationsInIshine(ResourceManagementDTO poData) {
		ServiceResponse serviceResponse = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/poCrudOperationsInIshine");
		apiLogInfo.setLogLevel("INFO");
		ApiLog initialLog = null;
		String exceptionDetailsForLog = null;
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		initialLog = apiLogUtility.startLog(poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest), "poCrudOperationsInIshine", "PoPortal", null ,httpRequest);

		String sourceSystem = httpRequest.getRequestURI().toString();
		
		if (poData == null) {
			finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
			throw new BadRequestException("No data received from PoPortal");
		}

		try {
			if(poData.getId() == null) {
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
			throw new BadRequestException("No Po Project ID received from PoPortal");
			}
			String requestType = poData.getRequestType();
			if ("create".equalsIgnoreCase(requestType)) {
				serviceResponse =  handleCreate(poData);
			} else if ("update".equalsIgnoreCase(requestType)) {
				serviceResponse =  handleUpdate(poData);
			} else if ("delete".equalsIgnoreCase(requestType)) {
				serviceResponse =  handleDelete(poData);
			} else {
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				throw new BadRequestException("Invalid request type: " + requestType);
			}
			finalHttpStatusCode = HttpStatus.OK.value();

		} catch (Exception e) {
			e.printStackTrace();
			exceptionDetailsForLog = e.toString();
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceResponse(e.getMessage());
			throw e;
//			serviceResponse.setServiceMessage(e.getMessage());
			
		} finally {
			if (initialLog != null) {
				apiLogUtility.endLog(initialLog.getId(), sourceSystem,finalHttpStatusCode, exceptionDetailsForLog, httpRequest);
			}
		}
		return serviceResponse;
	}
	@Transactional(rollbackOn = Exception.class)
	private ServiceResponse handleCreate(ResourceManagementDTO poData) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			
		boolean is_dept = true;
		if (projectRepository.findByPoProjectId(poData.getId()) != null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Project is already created in ishine!");
			response.setStatusCode(HttpStatus.CONFLICT.value());
			throw new ConflictException("Project is already created in Ishine!");
		}

		Project project = createProjectEntity(poData);
		Integer clientId = createOrUpdateClient(poData, response);
		if (clientId == null)
			return response;

		project.setClientId(clientId);
		project.setClientLocation(String.join(", ", Optional.ofNullable(poData.getClientLocation()).orElse(new String[] {})));
		
	
		
		if ("TNM".equalsIgnoreCase(project.getPoProjectType())) {
			syncResourceRequirementsTNM(project, poData);
		}
		
		for (String deptName : poData.getDepartment()) {
			Department dept = departmentRepository.findByName(deptName);
			if (dept == null) {
				is_dept = false;
			}
			else {
			is_dept = true;}
		}
		if(is_dept) {
			Project savedProject = projectRepository.save(project);
			syncDepartments(savedProject, poData);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Project created successfully!");
			
		}
		if(!is_dept) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Project can not be created!  Invalid Department ");
			throw new BadRequestException("Invalid Department Name");
		}
		return response;
		}
		catch(Exception e)
			{
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(e.getMessage());
			return response;
			
			}
	}

	private Project createProjectEntity(ResourceManagementDTO poData) {
		Project project = new Project();
		if(poData.getId() == null)throw new DataNotFoundException("Po Project Id Is Not Provided");
		project.setPoProjectId(poData.getId());
		if(poData.getPoNo() == null)throw new DataNotFoundException("Po No. Is Not Provided");
		project.setPoNo(poData.getPoNo());
		project.setApmosysRM(poData.getApmosysRM());
		project.setApmosysRmEmail(poData.getApmosysRmEmail());
		if(poData.getStatus() == null)throw new DataNotFoundException("Status Is Not Provided");
		project.setActive( "Completed".equals(poData.getStatus()) ? null : "true");
		project.setClientRM(poData.getClientRM());
		if(poData.getEndDate() == null)throw new DataNotFoundException("End Date Is Not Provided");
		project.setPoEndDate(convertIsoToDate(poData.getEndDate()));
		if(poData.getStartDate() == null)throw new DataNotFoundException("Start Date Is Not Provided");
		project.setPoStartDate(convertIsoToDate(poData.getStartDate()));
		if(poData.getProjectType() == null)throw new DataNotFoundException("Po Project Type Is Not Provided");
		project.setPoProjectType(poData.getProjectType());
		project.setProjectName(poData.getName());
		project.setState(poData.getClientState());
		project.setStatus(poData.getStatus());
		if(poData.getIsRenewable() == null)throw new DataNotFoundException("Po Project Renewable Type Is Not Provided");
		project.setIsRenewable(poData.getIsRenewable());
		project.setIsDraftProject(null);
		project.setCreatedBy(6l); 
		if (poData.getCreatedOn() != null) {
			try {
				long epochMillis = Long.parseLong(poData.getCreatedOn());
				LocalDateTime createdOn = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDateTime();
				project.setCreatedOn(Timestamp.valueOf(createdOn));
			} catch (NumberFormatException | DateTimeException e) {
				project.setCreatedOn(null);
			}
		}
		return project;
	}

	private Integer createOrUpdateClient(ResourceManagementDTO poData, ServiceResponse response) {
		Optional<Client> existing = clientsRepository.findByClientName(poData.getClientName());
		if (existing.isPresent()) {
			return existing.get().getClientId();
		}

		Client newClient = new Client();
		newClient.setClientName(poData.getClientName());
		Client savedClient = clientsRepository.save(newClient);
		if (savedClient == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Failed to add client");
			throw new RuntimeException("Failed to add client.");
		}

		List<ClientLocation> locations = new ArrayList<>();
		if (poData.getClientLocation() != null) {
			for (String loc : poData.getClientLocation()) {
				locations.add(new ClientLocation(savedClient.getClientId(), loc));
			}
		}
		locations.add(new ClientLocation(savedClient.getClientId(), "WFH")); // Always ensure WFH

		List<ClientLocation> savedLocations = clientLocationRepository.saveAll(locations);
		if (savedLocations == null || savedLocations.isEmpty()) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Failed to add client Location.");
			throw new RuntimeException("Failed to add client Location.");
		}
		return savedClient.getClientId();
	}

	private ServiceResponse syncResourceRequirementsTNM(Project project, ResourceManagementDTO poData) {
		ServiceResponse response = new ServiceResponse();
		List<ResourceRequirementDTO> requirementDTOs = poData.getResourceRequirements();
		if (requirementDTOs != null && !requirementDTOs.isEmpty()) {
			for (ResourceRequirementDTO dto : requirementDTOs) {
				if(resourceRequirementRepository.existsByResourceOverviewId(Long.parseLong(dto.getResourceOverviewId().toString()))) throw new BadCredentialsException("Resource Overview Id Already Exists:" + dto.getResourceOverviewId());
				ResourceRequirement req = new ResourceRequirement();
				req.setProjectId(project.getProjectId());
				req.setCount(dto.getCount());
				req.setDepartment(dto.getDepartment());
				req.setExperience(dto.getExperience());
				req.setRole(dto.getRole());
				req.setResourceOverviewId(dto.getResourceOverviewId() != null ? Long.parseLong(dto.getResourceOverviewId().toString()): null);
				ResourceRequirement saved = resourceRequirementRepository.save(req);
				if (saved == null) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Create operation failed at saving resource requirement!");
					throw new RuntimeException("Create operation failed at saving resource requirement!");
				}
			}
		}
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse("Resource requirements synced.");
		return response;
	}

	@Transactional(rollbackOn = Exception.class)
	private ServiceResponse syncDepartments(Project project, ResourceManagementDTO poData) {
		ServiceResponse response = new ServiceResponse();
		try {
			for (String deptName : poData.getDepartment()) {
				Department dept = departmentRepository.findByName(deptName);
				if (dept == null) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Create operation failed at saving departments!");
					throw new RuntimeException("Create operation failed at saving departments!");
				}

				ProjectDepartmentMap pdm = new ProjectDepartmentMap();
				pdm.setProjectId(project.getProjectId());
				pdm.setDeptId(dept.getDeptId());
				ProjectDepartmentMap saved = projectDepartmentMapRepository.save(pdm);
				if (saved == null) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Failed to map department: " + deptName);
					throw new RuntimeException("Failed to map department: " + deptName);
				}
			}
		
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse("Departments mapped successfully.");
		}
		catch(Exception e) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Departments mapped successfully.");
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
		return response;
	}
	
	public ServiceResponse handleUpdate(ResourceManagementDTO poData) {
		ServiceResponse response = new ServiceResponse();
		boolean isModified = false;
		if(poData.getId() == null)throw new DataNotFoundException("Po Project ID cannot be null");
		Project existingProject = projectRepository.findByPoProjectId(poData.getId());

		if (existingProject == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("No project found with PoProjectId: " + poData.getId());
			throw new RuntimeException("No project found with PoProjectId: " + poData.getId());
		}

		isModified = checkIfExistingProjectUpdated(existingProject, poData);

		if (isModified) {
			existingProject.setUpdatedBy(6L);
			existingProject.setUpdatedOn(LocalDateTime.now());
			projectRepository.save(existingProject);
		}

		// Sync child entities (departments, resource requirements)
		ServiceResponse deptResp = syncProjectDepartments(existingProject, poData);
		if (ServiceResponse.STATUS_FAIL.equals(deptResp.getServiceStatus())) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			throw new RuntimeException("Failed to Sync Department while updating project.");
		}

		ServiceResponse reqResp = syncResourceRequirements(existingProject, poData);
		if (ServiceResponse.STATUS_FAIL.equals(reqResp.getServiceStatus())) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			throw new RuntimeException("Failed to Sync Resource Requirement while updating project.");
		}

		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse("Project updated successfully.");
		return response;
	}

	private boolean checkIfExistingProjectUpdated(Project existingProject, ResourceManagementDTO poPortalProjects) {
		boolean isModified = false;

		if (!Objects.equals(existingProject.getPoNo(), poPortalProjects.getPoNo())) {
			existingProject.setPoNo(poPortalProjects.getPoNo());
			isModified = true;
		}

		if (!Objects.equals(existingProject.getApmosysRM(), poPortalProjects.getApmosysRM())) {
			existingProject.setApmosysRM(poPortalProjects.getApmosysRM());
			isModified = true;
		}

		if (!Objects.equals(existingProject.getApmosysRmEmail(), poPortalProjects.getApmosysRmEmail())) {
			existingProject.setApmosysRmEmail(poPortalProjects.getApmosysRmEmail());
			isModified = true;
		}

		if (!Objects.equals(existingProject.getClientRM(), poPortalProjects.getClientRM())) {
			existingProject.setClientRM(poPortalProjects.getClientRM());
			isModified = true;
		}

		if (!Objects.equals(existingProject.getPoProjectType(), poPortalProjects.getProjectType())) {
			existingProject.setPoProjectType(poPortalProjects.getPoProjectType());
			isModified = true;
		}

		if (!Objects.equals(existingProject.getProjectName(), poPortalProjects.getName())) {
			existingProject.setProjectName(poPortalProjects.getName());
			isModified = true;
		}

		if (!Objects.equals(existingProject.getState(), poPortalProjects.getClientState())) {
			existingProject.setState(poPortalProjects.getState());
			isModified = true;
		}

		if (!Objects.equals(existingProject.getStatus(), poPortalProjects.getStatus())) {
			existingProject.setStatus(poPortalProjects.getStatus());
			isModified = true;
		}
		
		if (!Objects.equals(existingProject.getClientName(),poPortalProjects.getClientName())) {
			existingProject.setClientName(poPortalProjects.getClientName());
					isModified = true;
		}
		String newActive = (poPortalProjects.getStatus() != null && poPortalProjects.getStatus().equals("Completed") ? null : "true");
		if (!Objects.equals(existingProject.getActive(), newActive)) {
			existingProject.setActive(newActive);
			isModified = true;
		}

		String startDate = convertIsoToDate(poPortalProjects.getStartDate());
		if (!Objects.equals(existingProject.getPoStartDate(), startDate)) {
			existingProject.setPoStartDate(startDate);
			isModified = true;
		}

		String endDate = convertIsoToDate(poPortalProjects.getEndDate());
		if (!Objects.equals(existingProject.getPoEndDate(), endDate)) {
			existingProject.setPoEndDate(endDate);
			isModified = true;
		}

		String joinedLocations = poPortalProjects.getClientLocation() != null
				? String.join(", ", poPortalProjects.getClientLocation())
				: null;
		if (!Objects.equals(existingProject.getClientLocation(), joinedLocations)) {
			existingProject.setClientLocation(joinedLocations);
			isModified = true;
		}

		String createdInput = poPortalProjects.getCreatedOn();

		if (createdInput != null) {
		    try {
		        long millis = Long.parseLong(createdInput);
		        Timestamp parsedCreatedOn = new Timestamp(millis);

		        if (!Objects.equals(existingProject.getCreatedOn(), parsedCreatedOn)) {
		            existingProject.setCreatedOn(parsedCreatedOn);
		            isModified = true;
		        }

		    } catch (NumberFormatException e) {
		        e.printStackTrace();
		    }
		}

		ServiceResponse clientUpdateResponse = updateClient(existingProject, poPortalProjects);
		if (ServiceResponse.STATUS_FAIL.equals(clientUpdateResponse.getServiceStatus())) {
			throw new RuntimeException(clientUpdateResponse.getServiceResponse().toString());
		} else if (ServiceResponse.STATUS_SUCCESS.equals(clientUpdateResponse.getServiceStatus())
				&& clientUpdateResponse.getServiceResponse().equals("Client updated successfully!")) {
			isModified = true;
		}
		return isModified;
	}
	
	
	public ServiceResponse handleDelete(ResourceManagementDTO poData) {
		ServiceResponse response = new ServiceResponse();
		Project existingProject = projectRepository.findByPoProjectId(poData.getId());

		if (existingProject == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Project not found in Ishine Portal for PoProjectID: " + poData.getId());
			return response;
		}
		
		List<Team> team = teamRepository.findTeamandIsActive(poData.getId());
		if(!team.isEmpty())
		{
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Team is active in Ishine Portal for PoProjectID: " + poData.getId());
			throw new BadRequestException("Team is active in Ishine Portal for PoProjectID: " + poData.getId());
		}
		existingProject.setActive("false");
		existingProject.setUpdatedBy(6L);
		existingProject.setUpdatedOn(LocalDateTime.now());
		projectRepository.save(existingProject);

		// Deactivate ProjectManagerMappings
		List<ProjectManagerMapping> activePMs = projectManagerMappingRepository.findByProjectIdAndActive(poData.getId(), 1);
		for (ProjectManagerMapping pm : activePMs) {
			pm.setActive(0);
			pm.setUpdatedBy(6L);
			pm.setUpdatedOn(LocalDateTime.now());
		}
		projectManagerMappingRepository.saveAll(activePMs);

		// Deactivate ProjectOverheadMappings
		List<ProjectOverheadMapping> overheads = projectOverheadMappingRepository.findByProjectIdAndActive(poData.getId(), 1);
		for (ProjectOverheadMapping oh : overheads) {
			oh.setActive(0);
			oh.setUpdatedBy(6L);
			oh.setUpdatedOn(LocalDateTime.now());
		}
		projectOverheadMappingRepository.saveAll(overheads);

		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse("Project deactivated successfully.");
		return response;
	}

	
}
