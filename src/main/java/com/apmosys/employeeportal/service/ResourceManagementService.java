package com.apmosys.employeeportal.service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpServerErrorException.InternalServerError;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.apmosys.employeeportal.controller.ProjectStructureRequest;
import com.apmosys.employeeportal.dto.BenchEmployeeDetailsDTO;
import com.apmosys.employeeportal.dto.CombinedPOInternalProjectResponse;
import com.apmosys.employeeportal.dto.DefaultProjectUpdateDTO;
import com.apmosys.employeeportal.dto.EmpMappingDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.EmployeeDetailsForTeamMemberDTO;
import com.apmosys.employeeportal.dto.EmployeeInformationDTO;
import com.apmosys.employeeportal.dto.EmployeeTeamMapDTO;
import com.apmosys.employeeportal.dto.ExceptionReportDTO;
import com.apmosys.employeeportal.dto.ExpiredPoDto;
import com.apmosys.employeeportal.dto.FCLineItemDTO;
import com.apmosys.employeeportal.dto.FCProjectMilestoneDTO;
import com.apmosys.employeeportal.dto.FilterMatrix;
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
import com.apmosys.employeeportal.dto.HandleTeamsAsPerLinkedPoPayloadDTO;
import com.apmosys.employeeportal.dto.HandleTeamsAsPerLinkedPoProjectDTO;
import com.apmosys.employeeportal.dto.IshineLinkProjectDto;
import com.apmosys.employeeportal.dto.IshineToPoEmpDetailsSharingDTO;
import com.apmosys.employeeportal.dto.IshineToPoEmployeeDTO;
import com.apmosys.employeeportal.dto.IshineToPoRequestDTO;
import com.apmosys.employeeportal.dto.LiftAndShiftTeamsDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.NonComplianceProjects;
import com.apmosys.employeeportal.dto.OtherProjectSetDTO;
import com.apmosys.employeeportal.dto.PoDetailsDto;
import com.apmosys.employeeportal.dto.PoDetailsForProjectPoMappingDTO;
import com.apmosys.employeeportal.dto.PoProjectSyncDTO;
import com.apmosys.employeeportal.dto.PoTeamDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.ProjectFetchDTO;
import com.apmosys.employeeportal.dto.ProjectFilterDTO;
import com.apmosys.employeeportal.dto.ProjectInfoDTO;
import com.apmosys.employeeportal.dto.ProjectManagersDTO;
import com.apmosys.employeeportal.dto.ProjectNameAndPrjoectIdDTO;
import com.apmosys.employeeportal.dto.ProjectOverheadsDTO;
import com.apmosys.employeeportal.dto.ProjectPoMappingWithResourceDTO;
import com.apmosys.employeeportal.dto.ProjectRequirementResponse;
import com.apmosys.employeeportal.dto.ProjectRequirementsDTO;
import com.apmosys.employeeportal.dto.RMGFlatEmployeeProjectTeamDTO;
import com.apmosys.employeeportal.dto.RMGProject;
import com.apmosys.employeeportal.dto.RMGProjectMappedEmployees;
import com.apmosys.employeeportal.dto.RMGProjectToEmployeeFlatDTO;
import com.apmosys.employeeportal.dto.RMGTeam;
import com.apmosys.employeeportal.dto.ResourceCountDto;
import com.apmosys.employeeportal.dto.ResourceManagementDTO;
import com.apmosys.employeeportal.dto.ResourceRequirementDTO;
import com.apmosys.employeeportal.dto.RestoreProjectPayloadDTO;
import com.apmosys.employeeportal.dto.RmgProjectDto;
import com.apmosys.employeeportal.dto.RmgResourceRequirementDto;
import com.apmosys.employeeportal.dto.SetProjectMappingAndDefaultProjectDTO;
import com.apmosys.employeeportal.dto.SkippedEmployeeDTO;
import com.apmosys.employeeportal.dto.SpocDTO;
import com.apmosys.employeeportal.dto.SummaryChartDTO;
import com.apmosys.employeeportal.dto.TeamDTO;
import com.apmosys.employeeportal.dto.TeamInfoProjectDTO;
import com.apmosys.employeeportal.dto.TeamInfoTeamDTO;
import com.apmosys.employeeportal.dto.TeamInfoTeamMemberDTO;
import com.apmosys.employeeportal.dto.TeamMemberDTO;
import com.apmosys.employeeportal.dto.TeamSpocDTO;
import com.apmosys.employeeportal.dto.TimeSheetDetailsDto;
import com.apmosys.employeeportal.dto.TimeSheetRequestDto;
import com.apmosys.employeeportal.dto.TimesheetDocumentDetailsDTO;
import com.apmosys.employeeportal.dto.UpdateHasClientSideIdDTO;
import com.apmosys.employeeportal.exception.BadRequestException;
import com.apmosys.employeeportal.exception.DataNotFoundException;
import com.apmosys.employeeportal.model.Activity;
import com.apmosys.employeeportal.model.ActivityTemplate;
import com.apmosys.employeeportal.model.ApiLog;
import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.ClientLocation;
import com.apmosys.employeeportal.model.CommonProperties;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.EmpPrimaryProjectMapping;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeCertificates;
import com.apmosys.employeeportal.model.EmployeeClientSideIdMapping;
import com.apmosys.employeeportal.model.EmployeeTeamMap;
import com.apmosys.employeeportal.model.FCLineItem;
import com.apmosys.employeeportal.model.FCProjectMilestone;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.model.PoDepartmentMapping;
import com.apmosys.employeeportal.model.PoRequirementMapping;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectDepartmentMap;
import com.apmosys.employeeportal.model.ProjectManagerMapping;
import com.apmosys.employeeportal.model.ProjectOverheadMapping;
import com.apmosys.employeeportal.model.ProjectPoDetails;
import com.apmosys.employeeportal.model.ResourceRequirement;
import com.apmosys.employeeportal.model.ResourceRequirementTemp;
import com.apmosys.employeeportal.model.Team;
import com.apmosys.employeeportal.model.TimesheetDocumentDetails;
import com.apmosys.employeeportal.repository.ActivitiesRepository;
import com.apmosys.employeeportal.repository.ActivityTemplateRepository;
import com.apmosys.employeeportal.repository.ClientLocationRepository;
import com.apmosys.employeeportal.repository.ClientsRepository;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmpPrimaryProjectMappingRepository;
import com.apmosys.employeeportal.repository.EmployeeCertificatesRepository;
import com.apmosys.employeeportal.repository.EmployeeClientSideIdMappingRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.FCLineItemRepository;
import com.apmosys.employeeportal.repository.FCProjectMilestoneRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.PoDepartmentMappingRepository;
import com.apmosys.employeeportal.repository.PoRequirementMappingRepository;
import com.apmosys.employeeportal.repository.ProjectDepartmentMapRepository;
import com.apmosys.employeeportal.repository.ProjectManagerMappingRepository;
import com.apmosys.employeeportal.repository.ProjectOverheadMappingRepository;
import com.apmosys.employeeportal.repository.ProjectPoDetailsRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.ProjectTempRepo;
import com.apmosys.employeeportal.repository.ResourceRequirementRepository;
import com.apmosys.employeeportal.repository.ResourceRequirementTempRepo;
import com.apmosys.employeeportal.repository.TeamRepository;
import com.apmosys.employeeportal.repository.TimesheetDocumentDetailsRepository;
import com.apmosys.employeeportal.response.ProjectStructureResponse;
import com.apmosys.employeeportal.response.ResourceRequirementResponse;
import com.apmosys.employeeportal.utility.ApiLogUtility;
import com.apmosys.employeeportal.utility.ExceptionLogContext;
import com.apmosys.employeeportal.utility.ExceptionUtils;
import com.apmosys.employeeportal.utility.PoPortalAPIAuthenticationJWTUtility;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;
import com.apmosys.employeeportal.utility.TypeConversionUtil;

@Service
public class ResourceManagementService {

	@Autowired
	ProjectService projectService;

	@Autowired
	ProjectRepository projectRepository;
	
	@Autowired
	PoDepartmentMappingRepository poDepartmentMappingRepository;
	
	@Autowired
	ProjectPoDetailsRepository projectPoDetailsRepository;

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
	EmployeeCertificatesRepository employeeCertificatesRepository;

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
	ValidationService validationService;

	@Autowired
	private JobRoleRepository jobRoleRepository;

	@Autowired
	private FCLineItemRepository fCLineItemRepository;

	@Autowired
	private FCProjectMilestoneRepository fCProjectMilestoneRepository;

	@Autowired
	private EmployeeClientSideIdMappingRepository employeeClientSideIdMappingRepository;

	@Autowired
	private PoPortalAPIAuthenticationJWTUtility poPortalAPIAuthenticationJWTUtility;

	@Autowired
	private TimesheetDocumentDetailsRepository timesheetDocumentDetailsRepository;

	@Autowired
	PoRequirementMappingRepository poRequirementMappingRepository;

	@Autowired
	private ProjectPoDetailsRepository poDetailsRepository;

	@Autowired
	private ApiLogUtility apiLogUtility;

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

	@Value("${finance.mail}")
	private String financeMail;

	@Value("${poPortal.api.allProjects}")
	private String allPoPortalProjects;

	@Value("${poPortal.api.realtimeProjectData}")
	private String realtimePoProjectData;

	@Autowired
	private final RestTemplate restTemplate = new RestTemplate();

	@Autowired
	private ApplicationContext context;

	private static final Logger log = LoggerFactory.getLogger(ResourceManagementService.class);

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
					if (!teamObj.getTeamMemberList().isEmpty()) {
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

							// Update teamMember mapping
							List<EmployeeTeamMap> updateMemberList = new ArrayList<EmployeeTeamMap>();

							for (EmployeeTeamMap presentMember : alreadyMappedMember) {
								for (TeamMemberDTO newMember : newTeamMember) {
									// Update teamMember mapping
									if (presentMember.getEmpId().equals(newMember.getEmpId())) {
										EmployeeTeamMap updateMember = employeeTeamMapRepository
												.findByEmpIdAndTeamIdAndActive(presentMember.getEmpId(),
														teamDbResponse.getTeamId(), 1L);

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
											updateMember.setIsShadow(
													newMember.getIsShadow() != null ? newMember.getIsShadow() : null);

											// added PoRequirementMappingId changes
											updateMember.setPoRequirementMappingId(
													newMember.getPoRequirementMappingId() != null
															? Long.parseLong(
																	newMember.getPoRequirementMappingId().toString())
															: null);

											if (newMember.getIsDefaultProject() != null) {
												if (newMember.getIsDefaultProject() == 1
														&& newMember.getEmpId() != null)
													defaultProjectEmpId.add(newMember.getEmpId());
											}

											updateMemberList.add(updateMember);
										}
									}
								}
							}
							List<EmployeeTeamMap> updateMemberDbResponse = employeeTeamMapRepository
									.saveAll(updateMemberList);

							if (!defaultProjectEmpId.isEmpty() && defaultProjectEmpId != null) {
								ServiceResponse defaultProjectResponse = this.handleDefaultProjectUpdate(
										defaultProjectEmpId, projectObj.getProjectId(),
										resourceManagementDTO.getCreatedBy(), logBuilder);

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

							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse(
									"Team updated successfully. Please approve it's Project to enable timesheets.");
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
						Project projectdetails = projectRepository.findByProjectId(projectObj.getProjectId());
						projectdetails.setIsDraftProject("true");
						Project dbResponse = projectRepository.save(projectdetails);
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
									newEmpTeamMap.setActive(2l);
									newEmpTeamMap.setEmployeeRole("TeamLead");
									newEmpTeamMap.setTeamId(teamDbResponse.getTeamId());
									newEmpTeamMap.setStartDate(LocalDateTime.now());
									newEmpTeamMap.setIsShadow(
											teamMember.getIsShadow() != null ? teamMember.getIsShadow() : null);

									// added PoRequirementMappingId changes
									newEmpTeamMap
											.setPoRequirementMappingId(teamMember.getPoRequirementMappingId() != null
													? Long.parseLong(teamMember.getPoRequirementMappingId().toString())
													: null);
									newEmpTeamMap.setCreatedBy(
											teamMember.getCreatedBy() != null ? teamMember.getCreatedBy() : null);
									newEmpTeamMap.setCreatedOn(new Timestamp(System.currentTimeMillis()));

									if (teamMember.getIsDefaultProject() != null) {
										if (teamMember.getIsDefaultProject() == 1)
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
									newEmpTeamMap.setIsShadow(
											teamMember.getIsShadow() != null ? teamMember.getIsShadow() : null);

									newEmpTeamMap
											.setPoRequirementMappingId(teamMember.getPoRequirementMappingId() != null
													? Long.parseLong(teamMember.getPoRequirementMappingId().toString())
													: null);

									if (teamMember.getIsDefaultProject() != null) {
										if (teamMember.getIsDefaultProject() == 1)
											defaultProjectEmpIds.add(teamMember.getEmpId());
									}
									newEmpTeamMap.setCreatedBy(
											teamMember.getCreatedBy() != null ? teamMember.getCreatedBy() : null);
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
										+ generateHtmlTable(teamObj.getTeamMemberList())
										+ "<br>Team RMG - ApMoSys Technologies<br>");
							} catch (Exception e) {
								e.printStackTrace();
							}

							List<EmployeeTeamMap> teamMemberDbResponse = employeeTeamMapRepository.saveAll(mapList);

							// Add default project mapping

							if (!defaultProjectEmpIds.isEmpty()) {
								ServiceResponse defaultProjectResponse = this.handleDefaultProjectUpdate(
										defaultProjectEmpIds, projectObj.getProjectId(),
										resourceManagementDTO.getCreatedBy(), logBuilder);

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

		} catch (Exception e) {
			log.error("Error in service", e);
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(ExceptionUtils.getExceptionMessage(e));
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
						empTeamMap.setIsShadow(newMember.getIsShadow() != null ? newMember.getIsShadow() : null);

						// added poRequirementmappingId changes
						empTeamMap.setPoRequirementMappingId(newMember.getPoRequirementMappingId() != null
								? Long.parseLong(newMember.getPoRequirementMappingId().toString())
								: null);

						if (newMember.getIsDefaultProject() != null) {
							if (newMember.getIsDefaultProject() == 1)
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

						// added poRequirementmappingId changes
						empTeamMap.setPoRequirementMappingId(newMember.getPoRequirementMappingId() != null
								? Long.parseLong(newMember.getPoRequirementMappingId().toString())
								: null);

						if (newMember.getIsDefaultProject() != null) {
							if (newMember.getIsDefaultProject() == 1)
								defaultProjectEmpIds.add(newMember.getEmpId());
						}

						empTeamMap.setCreatedBy(newMember.getCreatedBy() != null ? newMember.getCreatedBy() : null);
						empTeamMap.setCreatedOn(new Timestamp(System.currentTimeMillis()));

						mapList.add(empTeamMap);
					}

					List<EmployeeTeamMap> teamMapDbResponse = employeeTeamMapRepository.saveAll(mapList);

					Project projectdetails = projectRepository.findByProjectId(resourceManagementDTO.getProjectId());
					projectdetails.setIsDraftProject("true");
					Project dbResponse = projectRepository.save(projectdetails);

					if (dbResponse != null) {
						logBuilder.append(dbResponse + " /n Project draft staus updated to true");
					}

					// Add default project mapping
					if (!defaultProjectEmpIds.isEmpty()) {
						ServiceResponse defaultProjectResponse = this.handleDefaultProjectUpdate(defaultProjectEmpIds,
								resourceManagementDTO.getProjectId(), resourceManagementDTO.getCreatedBy(), logBuilder);

						if (ServiceResponse.STATUS_SUCCESS.equals(defaultProjectResponse.getServiceStatus())) {
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse(defaultProjectResponse.getServiceResponse());
						} else {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse(defaultProjectResponse.getServiceResponse());
						}
					}

					teamMapDbResponse.forEach((newAddedMember) -> {
						// if (resourceManagementDTO.getIsHOD().equals("true"))
						// newAddedMember.setActive(1L);
						// else
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
			if (project.getIsDraftProject() == null) {
				project.setIsDraftProject(null);
			} else if ("true".equalsIgnoreCase(project.getIsDraftProject())) {
				project.setIsDraftProject("true");
			} else if ("Rejected".equalsIgnoreCase(project.getIsDraftProject())) {
				project.setIsDraftProject("true");
			} else {
				System.out.println("isDraftProject - " + project.getIsDraftProject());
			}
//			project.setIsDraftProject("false");
			project.setProjectName(dto.getName());
			project.setPoNo(dto.getPoNo());
			project.setStartDate(dto.getProjectStartDate());
			project.setEndDate(dto.getProjectEndDate());
			project.setPoProjectType("Internal".equalsIgnoreCase(dto.getProjectType()) ? null : dto.getProjectType());

			project.setApmosysRM(dto.getApmosysRM());
			project.setIsRenewable(dto.getIsRenewable());
			project.setClientRM(dto.getClientRM());
			project.setUpdatedBy(dto.getCreatedBy());
			project.setUpdatedOn(LocalDateTime.now());
			project.setProjectStatus("In Progress");
			project.setDepartmentName(dto.getDepartmentName());
			Project dbResponse = projectRepository.save(project);

			ServiceResponse responseProjectManager = this.setProjectManager(dto, dbResponse);

			if (!responseProjectManager.getServiceStatus().equals("Success")) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(responseProjectManager.getServiceResponse());
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse(responseProjectManager.getServiceResponse());
			}

			ServiceResponse responseProjectOverhead = this.setProjectOverheads(dto, dbResponse);

			if (!responseProjectOverhead.getServiceStatus().equals("Success")) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(responseProjectOverhead.getServiceResponse());
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse(responseProjectOverhead.getServiceResponse());
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

						GetEmployeeByNameAndEmpldDTO teamLeadDetailsList = teamRepository
								.getSpocDetils(object.getTeamLeadId());
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

									// added hanges for PoRequirementMappingId
									teamMemberDTO
											.setPoRequirementMappingId(teamMemberObj.getPoRequirementMappingId() != null
													? Long.parseLong(
															teamMemberObj.getPoRequirementMappingId().toString())
													: null);

									teamMemberDTO.setEmploymentIdEmployeeType(
											prefixxTeamMember + empObj.getEmployeementId());

									teamMemberDTO.setIsShadow(
											teamMemberObj.getIsShadow() != null ? teamMemberObj.getIsShadow() : null);
									teamMemberDTO.setEmployeeTeamMapId(teamMemberObj.getEmployeeTeamMapId());
									Integer flag = this.isDefaultProject(teamMemberObj.getEmpId(), projectId);
									teamMemberDTO.setIsDefaultProject(flag != null ? flag : null);

									Map<String, Object> activeProjectInfo = this
											.getActiveProjectDetailsIfMultiple(teamMemberObj.getEmpId(), projectId);
									if ((Boolean) activeProjectInfo.get("isMultipleActiveProjects")) {
										List<Map<String, Object>> otherProjects = (List<Map<String, Object>>) activeProjectInfo
												.get("projects");
										teamMemberDTO.setOtherActiveProjects(otherProjects);
									} else {
										teamMemberDTO.setOtherActiveProjects(Collections.emptyList());
									}

									teamMember.add(teamMemberDTO);
								} else {
									// Team Member
									teamMemberDTO.setEmpId(teamMemberObj.getEmpId());
									teamMemberDTO.setName(employeeName);
									teamMemberDTO.setStartDate(teamMemberObj.getStartDate() != null
											? teamMemberObj.getStartDate().toString()
											: null);
									teamMemberDTO.setDepartmentName(findDepartment.getName());
									teamMemberDTO.setDepartmentId(findDepartment.getDeptId().toString());
									teamMemberDTO.setEmployeeRole(teamMemberObj.getEmployeeRole().split(","));

									// added hanges for PoRequirementMappingId
									teamMemberDTO
											.setPoRequirementMappingId(teamMemberObj.getPoRequirementMappingId() != null
													? Long.parseLong(
															teamMemberObj.getPoRequirementMappingId().toString())
													: null);
									teamMemberDTO.setEmploymentIdEmployeeType(
											prefixxTeamMember + empObj.getEmployeementId());
									teamMemberDTO.setIsShadow(
											teamMemberObj.getIsShadow() != null ? teamMemberObj.getIsShadow() : null);
									teamMemberDTO.setEmployeeTeamMapId(teamMemberObj.getEmployeeTeamMapId());
									Integer flag = this.isDefaultProject(teamMemberObj.getEmpId(), projectId);
									teamMemberDTO.setIsDefaultProject(flag != null ? flag : null);

									Map<String, Object> activeProjectInfo = this
											.getActiveProjectDetailsIfMultiple(teamMemberObj.getEmpId(), projectId);
									if ((Boolean) activeProjectInfo.get("isMultipleActiveProjects")) {
										List<Map<String, Object>> otherProjects = (List<Map<String, Object>>) activeProjectInfo
												.get("projects");
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
						dto.setProjectOverheadId(obj.getProjectOverheadId() != null
								? Long.parseLong(obj.getProjectOverheadId().toString())
								: null);
						dto.setProjectOverheadName(
								obj.getProjectOverheadName() != null ? obj.getProjectOverheadName().toString() : null);
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

					List<Object[]> poDeptMap = poDepartmentMappingRepository.getDepartmentByPoId(projectId);
					 if (!poDeptMap.isEmpty()) {
		                    Object[] result = poDeptMap.get(0);
		                    String departmentsStr = result[1] != null ? result[1].toString() : null;
		                    if (departmentsStr != null && !departmentsStr.isEmpty()) {
		                        department = Arrays.asList(departmentsStr.split(",\\s*"));
		                    }
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

	@Transactional(rollbackFor = Exception.class)
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
			Project projectObj = projectRepository.findByProjectId(resourceManagementDTO.getProjectId());
			Project projectDbResponse = null;
			String projectType = "";

			Map<String, List<EmployeeTeamMap>> modifiedTeamsMap = new LinkedHashMap<>();
			Map<String, List<EmployeeTeamMap>> allTeamsMap = new LinkedHashMap<>();

			if (projectObj != null) {
				// decide project type
				if (resourceManagementDTO.getPoProjectType() != null) {
					projectType = resourceManagementDTO.getPoProjectType();
				} else if (resourceManagementDTO.getProjectType() != null) {
					projectType = resourceManagementDTO.getProjectType();
				} else if (resourceManagementDTO.getInternalProjectType() != null) {
					projectType = resourceManagementDTO.getInternalProjectType();
				} else {
					projectType = "";
				}
				List<Team> allTeams = teamRepository.findTeamByProjectId(projectObj.getProjectId());
				// update team members
				List<EmployeeTeamMap> teamMembersToActivate = employeeTeamMapRepository
						.findByProjectIdAndActive(projectObj.getProjectId(), 2L);

				if (!teamMembersToActivate.isEmpty()) {
					teamMembersToActivate.forEach(teamMember -> teamMember.setActive(1L));
					employeeTeamMapRepository.saveAll(teamMembersToActivate);

					for (EmployeeTeamMap member : teamMembersToActivate) {
						Long teamId = member.getTeamId();
						if (teamId != null) {
							Team team = allTeams.stream().filter(t -> t.getTeamId().equals(teamId)).findFirst()
									.orElse(null);

							if (team != null) {
								modifiedTeamsMap.computeIfAbsent(team.getTeamName(), k -> new ArrayList<>())
										.add(member);
							}
						}
					}

					List<Project> isDraftProject = employeeTeamMapRepository
							.findByProjectIdAndActiveForDraftProject(projectObj.getProjectId(), 2L);
					if (!isDraftProject.isEmpty()) {
						isDraftProject.forEach(draftProject -> draftProject.setIsDraftProject("true"));
						projectRepository.saveAll(isDraftProject);
					}
				} else {
					List<Project> isDraftProject = employeeTeamMapRepository
							.findByProjectIdAndActiveForDraftProject(projectObj.getProjectId(), 1L);
					if (!isDraftProject.isEmpty()) {
						isDraftProject.forEach(draftProject -> draftProject.setIsDraftProject("false"));
						projectRepository.saveAll(isDraftProject);
					}
				}

				// STEP 1: Save project FIRST
				projectObj.setIsDraftProject("false");
				projectDbResponse = projectRepository.save(projectObj);

				if (projectDbResponse != null) {
					// STEP 2: Then call PoPortal sync (only for certain types)
					if (projectType.equalsIgnoreCase("Fixed Cost") || projectType.equalsIgnoreCase("TNM")
							|| projectType.equalsIgnoreCase("Monitoring")) {
						ServiceResponse poPortalResponse = sendProjectInfoToPoPortal(resourceManagementDTO);

						if (poPortalResponse.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS)) {
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("Project Approved.");
							apiLogInfo.setApiResponse("Project Approved");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
						} else {
							// Do not rollback project save, just log PoPortal failure
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Project saved, but failed to sync with PoPortal: "
									+ poPortalResponse.getServiceResponse());
							apiLogInfo.setApiResponse("Project saved but failed to sync with PoPortal "
									+ poPortalResponse.getServiceResponse());
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						}
						if (!allTeams.isEmpty()) {
							for (Team team : allTeams) {
								List<EmployeeTeamMap> members = employeeTeamMapRepository
										.findByTeamId(team.getTeamId());
								allTeamsMap.put(team.getTeamName(), members);
							}
						}
						String allEmails = null;
						allEmails = rmgMail + "," + financeMail + "," + bdMail + ",";
						ServiceResponse rmBdmailsResponse = poPortalAPIService
								.getAllMailsByProjectId(projectObj.getPoProjectId());
						Object responseObj = rmBdmailsResponse.getServiceResponse();
						List<String> rmBdmails = new ArrayList<>();

						if (responseObj instanceof List<?>) {
							for (Object obj : (List<?>) responseObj) {
								if (obj instanceof String) {
									rmBdmails.add((String) obj);
								}
							}
						}
						allEmails += String.join(",", rmBdmails);
						System.out.println(allEmails);
						Employee employeeObj = employeeRepository.findByEmpId(resourceManagementDTO.getEmpId());
						if (employeeObj != null) {
							try {
								StringBuilder html = new StringBuilder();
								html.append("Dear Recipients, <br><br>").append(employeeObj.getName())
										.append(" has approved the project: <b>")
										.append(resourceManagementDTO.getName()).append("</b><br>")
										.append("The Project Info with Team & Team Member details will be shared with PoPortal.<br><br>");

								// ===== Section 1: Modified Teams =====
								if (modifiedTeamsMap != null && !modifiedTeamsMap.isEmpty()) {
									html.append(
											"<h4 style='color:#2E86C1;font-family:Arial, sans-serif;'>Modified Teams With Employees</h4>");
									html.append(
											"<table border='1' cellspacing='0' cellpadding='8' style='border-collapse:collapse;width:100%;font-family:Arial, sans-serif;font-size:13px;border:1px solid #BFC9CA;'>");
									html.append(
											"<thead style='background-color:#2E86C1;color:#FFFFFF;text-align:left;'>")
											.append("<tr>")
											.append("<th style='padding:8px;color:#FFFFFF;'>Team Name</th>")
											.append("<th style='padding:8px;color:#FFFFFF;'>Employee ID</th>")
											.append("<th style='padding:8px;color:#FFFFFF;'>Employee Name</th>")
											.append("<th style='padding:8px;color:#FFFFFF;'>Role</th>").append("</tr>")
											.append("</thead><tbody>");

									boolean alternate = false;
									for (Map.Entry<String, List<EmployeeTeamMap>> entry : modifiedTeamsMap.entrySet()) {
										String teamName = entry.getKey();
										List<EmployeeTeamMap> members = entry.getValue();

										for (EmployeeTeamMap member : members) {
											String name = employeeRepository.getEmployeeName(member.getEmpId());
											Long employeementId = employeeRepository
													.getEmployeeEmployeementId(member.getEmpId());
											List<String> roleList = resourceRequirementRepository
													.getResourceRoleFromEmpId(member.getEmpId());
											String role = String.join(",", roleList);
											String rowColor = alternate ? "#F8F9F9" : "#FFFFFF";
											alternate = !alternate;

											html.append("<tr style='background-color:" + rowColor + ";'>")
													.append("<td style='padding:8px;'>")
													.append(teamName != null ? teamName : "-").append("</td>")
													.append("<td style='padding:8px;'>")
													.append(employeementId != null ? "A-" + employeementId : "-")
													.append("</td>").append("<td style='padding:8px;'>")
													.append(name != null ? name : "-").append("</td>")
													.append("<td style='padding:8px;'>")
													.append(role != null ? role : "-").append("</td>").append("</tr>");
										}
									}
									html.append("</tbody></table><br><br>");

								}
								if (allTeamsMap != null && !allTeamsMap.isEmpty()) {
									html.append(
											"<h4 style='color:#2E86C1;font-family:Arial, sans-serif;'>All Teams & Members</h4>");

									for (Map.Entry<String, List<EmployeeTeamMap>> entry : allTeamsMap.entrySet()) {
										String teamName = entry.getKey();
										List<EmployeeTeamMap> members = entry.getValue();

										html.append(
												"<h5 style='color:#1F618D;margin-top:10px;font-family:Arial, sans-serif;'>Team: ")
												.append(teamName != null ? teamName : "-").append("</h5>");
										html.append(
												"<table border='1' cellspacing='0' cellpadding='8' style='border-collapse:collapse;width:100%;font-family:Arial, sans-serif;font-size:13px;border:1px solid #BFC9CA;'>");
										html.append(
												"<thead style='background-color:#2874A6;color:#FFFFFF;text-align:left;'>")
												.append("<tr>")
												.append("<th style='padding:8px;color:#FFFFFF;'>Employee ID</th>")
												.append("<th style='padding:8px;color:#FFFFFF;'>Employee Name</th>")
												.append("<th style='padding:8px;color:#FFFFFF;'>Role</th>")
												.append("</tr>").append("</thead><tbody>");

										boolean alternate2 = false;
										for (EmployeeTeamMap member : members) {
											String name = employeeRepository.getEmployeeName(member.getEmpId());
											Long employeementId = employeeRepository
													.getEmployeeEmployeementId(member.getEmpId());
											List<String> roleList = resourceRequirementRepository
													.getResourceRoleFromEmpId(member.getEmpId());
											String role = String.join(",", roleList);
											String rowColor = alternate2 ? "#F8F9F9" : "#FFFFFF";
											alternate2 = !alternate2;

											html.append("<tr style='background-color:" + rowColor + ";'>")
													.append("<td style='padding:8px;'>")
													.append(employeementId != null ? "A-" + employeementId : "-")
													.append("</td>").append("<td style='padding:8px;'>")
													.append(name != null ? name : "-").append("</td>")
													.append("<td style='padding:8px;'>")
													.append(role != null ? role : "-").append("</td>").append("</tr>");
										}
										html.append("</tbody></table><br>");
									}

								}
								System.out.println(allEmails + "allEmails");
								System.out.println(html.toString() + "html");
								html.append("<br><b>Regards,<br>Ishine</b>");
								mailService.sendMailWithCC(allEmails, employeeObj.getEmail(),
										"Regarding Project Approval", html.toString());
								apiLogInfo.setApiResponse("Project Approved and mail sent");
							} catch (Exception e) {
								e.printStackTrace();
								response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
								response.setServiceResponse(
										"Project Approved but unable to send email due to:" + e.getMessage());
							}
						}
					} else {
						// Internal project (no PoPortal sync)
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse(
								"Project Approved but Internal project does not sync with PO portal.");
						apiLogInfo.setApiResponse("Project Approved but Internal project does not sync with PO portal");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

						Employee employeeObj = employeeRepository.findByEmpId(resourceManagementDTO.getEmpId());
						if (employeeObj != null) {
							try {
//		                    	StringBuilder html = new StringBuilder();

								mailService.sendMailWithCC(rmgMail, employeeObj.getEmail(),
										"Regarding Project Approval",
										"Dear RMG Team ," + "<br><br>" + employeeObj.getName()
												+ " has approved the Internal Project : "
												+ resourceManagementDTO.getName() + "<br>"
												+ "The above Project Info with Team & Team Member details will not be shared with PoPortal.<br><br>");
							} catch (Exception e) {
								e.printStackTrace();
								apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
								apiLogInfo.setLogLevel("ERROR");
							}
						} else {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("User's mail address not found.");
							apiLogInfo.setApiResponse("User's mail address not found");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						}
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
			// rollback will happen automatically due to @Transactional
			throw e; // ensure rollback
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
		logBuilder.append("ProjectId: ").append(resourceManagementDTO.getId()).append(", EmployeeId: ")
				.append(resourceManagementDTO.getEmpId());

		try {
			Map<String, List<EmployeeTeamMap>> modifiedTeamsMap = new LinkedHashMap<>();
			Map<String, List<EmployeeTeamMap>> allTeamsMap = new LinkedHashMap<>();

			Project projectObj = projectRepository.findByPoProjectId(resourceManagementDTO.getId());
			if (projectObj == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiResponse("Project not found");
				return response;
			}

			List<Team> allTeams = teamRepository.findTeamByProjectId(projectObj.getProjectId());

			// ==== Modified Team Members (Active = 2) ====
			List<EmployeeTeamMap> rejectedMembers = employeeTeamMapRepository
					.findByProjectIdAndActive(projectObj.getProjectId(), 2L);

			for (EmployeeTeamMap member : rejectedMembers) {
				Team team = allTeams.stream().filter(t -> t.getTeamId().equals(member.getTeamId())).findFirst()
						.orElse(null);

				if (team != null) {
					modifiedTeamsMap.computeIfAbsent(team.getTeamName(), k -> new ArrayList<>()).add(member);
				}
			}

			// ==== Update project as REJECTED ====
			projectObj.setIsDraftProject("Rejected");
			Project savedProject = projectRepository.save(projectObj);

			if (savedProject == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to update project.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiResponse("Unable to update project");
				return response;
			}

			// ==== Collect Emails ====
			List<String> allEmailList = new ArrayList<>();
			allEmailList.add(rmgMail);
			allEmailList.add(financeMail);
			allEmailList.add(bdMail);

			ServiceResponse rmBdMailResponse = poPortalAPIService.getAllMailsByProjectId(projectObj.getPoProjectId());
			Object emailObj = rmBdMailResponse.getServiceResponse();

			if (emailObj instanceof List<?>) {
				for (Object item : (List<?>) emailObj) {
					if (item instanceof String) {
						allEmailList.add((String) item);
					}
				}
			}

			String allEmails = String.join(",", allEmailList);

			// ==== Fetch Employee ====
			Employee employeeObj = employeeRepository.findByEmpId(resourceManagementDTO.getEmpId());
			if (employeeObj == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Requester's email not found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiResponse("Requester's email not found");
				return response;
			}

			// ==== Build Email HTML ====
			try {
				StringBuilder html = new StringBuilder();
				html.append("Dear Recipients,<br><br>").append(employeeObj.getName())
						.append(" has rejected the project: <b>").append(resourceManagementDTO.getName())
						.append("</b><br>").append("Below are the rejected changes.<br><br>");

				// ==== Rejected Changes Table ====
				if (!modifiedTeamsMap.isEmpty()) {
					html.append("<h4 style='color:#2E86C1;'>Rejected Changes</h4>").append(
							"<table border='1' cellspacing='0' cellpadding='8' style='border-collapse:collapse;width:100%;'>")
							.append("<thead style='background-color:#2E86C1;color:#FFF;'><tr>")
							.append("<th>Team</th><th>Emp ID</th><th>Name</th><th>Role</th></tr></thead><tbody>");

					for (var entry : modifiedTeamsMap.entrySet()) {
						for (EmployeeTeamMap member : entry.getValue()) {

							String empName = employeeRepository.getEmployeeName(member.getEmpId());
							Long empEmploymentId = employeeRepository.getEmployeeEmployeementId(member.getEmpId());
							String role = String.join(",",
									resourceRequirementRepository.getResourceRoleFromEmpId(member.getEmpId()));

							html.append("<tr>").append("<td>").append(entry.getKey()).append("</td>").append("<td>")
									.append(empEmploymentId != null ? "A-" + empEmploymentId : "-").append("</td>")
									.append("<td>").append(empName != null ? empName : "-").append("</td>")
									.append("<td>").append(role != null ? role : "-").append("</td>").append("</tr>");
						}
					}

					html.append("</tbody></table><br><br>");
				}

				html.append("<br><b>Regards,<br>Ishine</b>");

				// ==== Send Email ====
				mailService.sendMailWithCC(allEmails, employeeObj.getEmail(), "Regarding Project Rejection",
						html.toString());

				apiLogInfo.setApiResponse("Project rejected & mail sent");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			} catch (Exception mailEx) {
				mailEx.printStackTrace();
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Project Rejected but mail sending failed: " + mailEx.getMessage());
				apiLogInfo.setApiResponse("Project rejected but mail sending failed");
				return response;
			}

			// ==== Team Cleanup Logic (Same Logic, Just Cleaner) ====
			List<Team> teamList = teamRepository.findTeamByProjectId(projectObj.getProjectId());
			for (Team team : teamList) {

				List<EmployeeTeamMap> teamPending = employeeTeamMapRepository.findByTeamIdAndActive(team.getTeamId());
				List<EmployeeTeamMap> activeMembers = employeeTeamMapRepository
						.findTeammembersByTeamIdAndStatus(team.getTeamId());

				if (teamPending.size() <= 1) {
					for (EmployeeTeamMap member : teamPending) {
						if (member.getActive() == 2) {
							activitiesRepository.deleteAll(activitiesRepository.findByTeamId(team.getTeamId()));
							employeeTeamMapRepository.deleteAllByTeamId(team.getTeamId());
							teamRepository.delete(team);
						}
					}
				} else {
					for (EmployeeTeamMap member : teamPending) {
						if (member.getActive() == 1) {
							member.setActive(1L);
						} else {
							if (activeMembers.isEmpty()) {
								activitiesRepository.deleteAll(activitiesRepository.findByTeamId(team.getTeamId()));
								employeeTeamMapRepository.deleteAllByTeamId(team.getTeamId());
								teamRepository.delete(team);
							} else {
								member.setActive(member.getActive() == 2 ? 0L : 1L);
							}
						}
						employeeTeamMapRepository.save(member);
					}
				}
			}

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Project Rejected Successfully.");

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

					List<Object[]> result = projectManagerMappingRepository
							.findProjectManagersPerProject(Long.parseLong(projectObj.getProjectId().toString()));

					List<String> projectManagerIds = new ArrayList<>();

					for (Object[] obj : result) {
						if (obj[2] != null) {
							projectManagerIds.add(obj[2].toString());
						}
					}

					projectDTO.setPoProjectManagers(projectManagerIds);

					List<Team> teamDetails = teamRepository.findByProjectIdAndIsActive(projectObj.getProjectId(), "Y");

					if (!teamDetails.isEmpty()) {
						teamDetails.forEach((team) -> {
							List<EmployeeTeamMap> teamMemberDetials = employeeTeamMapRepository
									.findByTeamIdAndActive(team.getTeamId());
							if (!teamMemberDetials.isEmpty()) {
								projectDTO.setIshineProjectStatus("InProgress");
							} else {
								if ("Completed".equals(resourceManagementDTO.getProjectStatus())) {
									projectDTO.setIshineProjectStatus("Completed");
								} else if ("Rejected".equalsIgnoreCase(resourceManagementDTO.getIsDraftProject())) {
									projectDTO.setIshineProjectStatus("Not Started");
								} else {
									projectDTO.setIshineProjectStatus("Not Started");
								}
							}
						});
					} else {
						if ("Completed".equals(resourceManagementDTO.getProjectStatus())) {
							projectDTO.setIshineProjectStatus("Completed");
						} else if ("Rejected".equalsIgnoreCase(resourceManagementDTO.getIsDraftProject())) {
							projectDTO.setIshineProjectStatus("Not Started");
						} else {
							projectDTO.setIshineProjectStatus("Not Started");
						}
					}
					projectInfo.add(projectDTO);
					try {
						ServiceResponse syncResponse = poPortalAPIService.syncProjectData(projectInfo);

						if (ServiceResponse.STATUS_SUCCESS.equals(syncResponse.getServiceStatus())) {
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

					catch (HttpClientErrorException e) {
						JSONObject json = new JSONObject(e.getResponseBodyAsString());

						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse(json.get("message"));
					} catch (HttpServerErrorException e) {
						JSONObject json = new JSONObject(e.getResponseBodyAsString());

						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse(json.get("message"));
					}
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

		try {

			List<Object[]> allInternalProject = projectRepository.getAllInternalProject();
			logBuilder.append("InternalProjectList : " + allInternalProject.size());
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
					List<PoDepartmentMapping> allDeptList = poDepartmentMappingRepository.findByProjectId(projectId);

					if (allDeptList != null && !allDeptList.isEmpty()) {
					    List<Long> deptIds = allDeptList.stream()
					            .map(PoDepartmentMapping::getDeptId)
					            .distinct()
					            .collect(Collectors.toList());
					    List<Department> departments = departmentRepository.findAllById(deptIds);
					    List<String> deptList = departments.stream()
					            .map(Department::getName)
					            .collect(Collectors.toList());
					    
					    String[] department = deptList.toArray(new String[0]);
					    projectDTO.setDepartment(department);
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
			dto.setActive(obj[9] != null ? obj[9].toString().toString() : null);
//			dto.setActive(obj[9] != null ? Integer.parseInt(obj[9].toString()) : null);
			dto.setProjectId(obj[10] != null ? Integer.parseInt(obj[10].toString()) : null);
			dto.setProjectStartDate(obj[11] != null ? obj[11].toString().toString() : null);
			dto.setProjectEndDate(obj[12] != null ? obj[12].toString().toString() : null);
			dto.setStatus(obj[15] != null ? obj[15].toString() : null);
			dto.setPoProjectType(obj[13] != null ? obj[13].toString() : null);
			dto.setInternalProjectType(obj[14] != null ? obj[14].toString() : null);
			dto.setRescRemovedBy(obj[16] != null ? Long.parseLong(obj[16].toString()) : null);
			dto.setRescRemovedByName(obj[17] != null ? obj[17].toString() : null);
			dto.setEmployeeTeamMapId(obj[18] != null ? Long.parseLong(obj[18].toString()) : null);
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

	@Transactional
	public ServiceResponse updateProjectResourceAsInActive(ResourceManagementDTO resourceManagementDTO) {
		System.err.println(" updateProjectResourceAsInActive   ");
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/matrixCertificationDropdownRbac");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();

		ServiceResponse response = new ServiceResponse();

		EmployeeTeamMap findResource = employeeTeamMapRepository.findByEmpIdAndTeamIdAndActiveStatus(
				resourceManagementDTO.getEmpId(), resourceManagementDTO.getTeamId());
		Team findTeam = teamRepository.findTeamByTeamId(resourceManagementDTO.getTeamId());
		System.out.println("findResource  " + findResource);
		System.out.println("getres removed by :" + resourceManagementDTO.getRescRemovedBy());
		System.out.println("getres updated by :" + resourceManagementDTO.getUpdatedBy());
		System.out.println("getres udpatedon :" + LocalDateTime.now());
		if (findResource != null) {
			findResource.setActive(0l);
			findResource.setRescRemovedBy(resourceManagementDTO.getRescRemovedBy());
			findResource.setUpdatedBy(resourceManagementDTO.getUpdatedBy());
			findResource.setUpdatedOn(LocalDateTime.now());
			if (resourceManagementDTO.getPoRequirementMappingId() != null) {
				findResource.setPoRequirementMappingId(resourceManagementDTO.getPoRequirementMappingId());
			}

			// added flag for thea date stating its po / custom end date
			findResource.setIsCustomDate(resourceManagementDTO.getIsCustomDate());
			if (resourceManagementDTO.getEndDate() != null) {
				String str = resourceManagementDTO.getEndDate();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				LocalDate date = LocalDate.parse(str, formatter);
				LocalDateTime endDateTime = date.atStartOfDay();

				findResource.setEndDate(endDateTime);
			} else {
				findResource.setEndDate(LocalDateTime.now());
			}
			System.out.println("EndDate  " + findResource.getEndDate());
			System.out.println("EmpId  " + resourceManagementDTO.getEmpId());
			logBuilder.append("EndDate  " + findResource.getEndDate() + "/n");
			logBuilder.append("EmpId  " + resourceManagementDTO.getEmpId() + "/n");
			employeeTeamMapRepository.save(findResource);

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Resource removed successfully, from Team Name - " + findTeam.getTeamName());

		}

		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);

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
				project.setStartDate(formattedStartDate);
				project.setEndDate(formattedEndDate);
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
			List<Object[]> projectInfo;
			projectInfo = projectRepository
					.getProjectInfo(Integer.parseInt(resourceManagementDTO.getProjectViewId().toString()));
			if (projectInfo.isEmpty()) {
				projectInfo = projectRepository.getProjectInfoByProjectId(
						Integer.parseInt(resourceManagementDTO.getProjectViewId().toString()));
			}
			Project projObj = projectRepository
					.findByProjectId(Integer.parseInt(resourceManagementDTO.getProjectViewId()));
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

						if (obj.getProjectOverheadName() != null) {
							projectOverheadNames.add(obj.getProjectOverheadName().toString());
						}

						ProjectOverheadsDTO overheadDto = new ProjectOverheadsDTO();
						overheadDto.setProjectOverheadId(obj.getProjectOverheadId() != null
								? Long.parseLong(obj.getProjectOverheadId().toString())
								: null);
						overheadDto.setProjectOverheadName(
								obj.getProjectOverheadName() != null ? obj.getProjectOverheadName().toString() : null);
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

		try {
			List<Object[]> projectInfo = projectRepository
					.getPoProjectInfo(Long.parseLong(resourceManagementDTO.getProjectViewId().toString()));
			logBuilder.append("projectInfo : " + projectInfo.size());
			Project projObj = projectRepository
					.findByPoProjectId(Long.parseLong(resourceManagementDTO.getProjectViewId()));
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
						overheadDto.setProjectOverheadId(obj.getProjectOverheadId() != null
								? Long.parseLong(obj.getProjectOverheadId().toString())
								: null);
						overheadDto.setProjectOverheadName(
								obj.getProjectOverheadName() != null ? obj.getProjectOverheadName().toString() : null);
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
			empDTO.setIsConsultant(object[7] != null ? object[7].toString() : null);
			empDTO.setIsApprenticeship(object[8] != null ? object[8].toString() : null);
			empDTO.setIsApmosysProduct(object[9] != null ? object[9].toString() : null);

			String employmentId = empDTO.getEmployeementId() != null ? empDTO.getEmployeementId().toString() : null;
			String isConsultant = empDTO.getIsConsultant();
			String isApmosysProduct = empDTO.getIsApmosysProduct();

			if (employmentId != null) {
				if ("true".equalsIgnoreCase(isApmosysProduct)) {
					empDTO.setEmploymentIdAcToET("AP-" + employmentId);
//	                    newDto.setEmployeementIdAccToET("AP-" + employmentId);
				} else {
					empDTO.setEmploymentIdAcToET("A-" + employmentId);
//	                    newDto.setEmployeementIdAccToET("A-" + employmentId);
				}
			}

			dtoList.add(empDTO);
		});

		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse(dtoList);

		return response;
	}

	public ServiceResponse getTeamInfo(Integer projectId) {

		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Project 360");
		apiLogInfo.setApiUrl("/api/getTeamInfo");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("TeamInfo : " + projectRepository.getTeamInfo(projectId).size());

		try {
			List<Object[]> teamInfo = projectRepository.getTeamInfo(projectId);

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
					team.setTeamLeadName(object[14] != null ? object[14].toString() : null);
					team.setSpoc(object[13] != null ? object[13].toString() : null);
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
				member.setDepartment(object[16] != null ? object[16].toString() : null);
				member.setEmploymentId(object[17] != null ? object[17].toString() : null);

				member.setIsDefaultProject(this.isDefaultProject(member.getEmpId(), projectId));

				Map<String, Object> activeProjectInfo = this.getActiveProjectDetailsIfMultiple(member.getEmpId(),
						projectId);
				if ((Boolean) activeProjectInfo.get("isMultipleActiveProjects")) {
					List<Map<String, Object>> otherProjects = (List<Map<String, Object>>) activeProjectInfo
							.get("projects");
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

		try {
			List<Object[]> projectInfoList = projectRepository.getPoProjectDetailsForPoProjects();
			logBuilder.append("getPoProjectDetailsForPoProjects " + projectInfoList.size());
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

			Map<Long, GetEmployeeByNameAndEmpldDTO> uniqueMap = employees.stream().collect(Collectors
					.toMap(GetEmployeeByNameAndEmpldDTO::getEmpId, dto -> dto, (existing, replacement) -> existing));

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

//	public ServiceResponse combinedDataCount(ProjectFilterDTO projectFilterDTO) {
//		ServiceResponse response = new ServiceResponse();
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setLogLevel("INFO");
//
//		try {
//			if (projectFilterDTO == null) {
//				return failResponse(response, apiLogInfo, "Invalid input: ProjectFilterDTO is null.");
//			}
//
//			if (Boolean.FALSE.equals(projectFilterDTO.getIsHod()) && Boolean.FALSE.equals(projectFilterDTO.getIsAdmin())
//					&& Boolean.FALSE.equals(projectFilterDTO.getIsOther())) {
//				return failResponse(response, apiLogInfo, "Invalid input: All role flags are false.");
//			}
//			if((Boolean.TRUE.equals(projectFilterDTO.getIsHod())&&Boolean.TRUE.equals(projectFilterDTO.getIsAdmin())) ||
//					Boolean.TRUE.equals(projectFilterDTO.getIsAdmin()) && Boolean.TRUE.equals(projectFilterDTO.getIsOther())||
//					Boolean.TRUE.equals(projectFilterDTO.getIsHod())&&Boolean.TRUE.equals(projectFilterDTO.getIsOther())) {
//				return failResponse(response, apiLogInfo, "Invalid input: More than one flag is true");
//			}
//
//			Map<String, Integer> map = new HashMap<>();
//			List<Long> deptIdList = new ArrayList<>();
//			Set<Integer> projectIdSet = new HashSet<>();
//			List<Integer> projectIdListTemp;
//			Integer days = projectFilterDTO.getDays();
//			Integer pendingForApprovalCount,approvedCount,notStartedCount,rejectedCount,totalCount,completedInIshine ,activeProject
//			,activeProjectsAfterMarch31Count , activeProjectsUpToMarch31Count , internalActiveProjectsCount , activeTNMCount 
//			;
//			
//			CombinedPOInternalProjectResponse responseData = new CombinedPOInternalProjectResponse();
//			
//			Map<String, Integer> expiredProjectCounts = new HashMap<>();
//			
//			
//			if ("All".equalsIgnoreCase(projectFilterDTO.getApprovalStatus()))
//				projectFilterDTO.setApprovalStatus(null);
//			if (Boolean.TRUE.equals(projectFilterDTO.getIsAdmin())) {
//
//				if(!projectFilterDTO.getDepartmentsids().isEmpty() && projectFilterDTO.getDepartmentsids() != null ) {
//					List<Long> selectedDeptList= projectFilterDTO.getDepartmentsids();
//					Set<Integer> matchingProjectIds = teamRepository.findAll().stream()
//						    .filter(team -> {
//						        if (team.getDeptIds() == null) return false;
//						        List<String> teamDeptIds = Arrays.asList(team.getDeptIds().split(","));
//						        return selectedDeptList.stream()
//						                .map(String::valueOf)
//						                .anyMatch(teamDeptIds::contains); 
//						    })
//						    .map(Team::getProjectId)
//						    .filter(Objects::nonNull)
//						    .collect(Collectors.toSet());
//					projectIdSet = matchingProjectIds;
//					pendingForApprovalCount = projectRepository.getAllActiveProjecCountstList("true", projectIdSet, true );
//					approvedCount = projectRepository.getAllActiveProjecCountstList("false", projectIdSet, true);
//					notStartedCount = projectRepository.getAllNotStartedProjectCountInDept(selectedDeptList);
//					rejectedCount = projectRepository.getAllActiveProjecCountstList( "Rejected", projectIdSet, true);
//					completedInIshine = projectRepository.getAllCompletedProjectCountInIshine(selectedDeptList);
//					activeProject = projectRepository.getTotalAllActiveProjectCount(selectedDeptList);
////					activeProjectsAfterMarch31Count = projectRepository.getActiveProjectGreaterThan31MarchCount(selectedDeptList);
////					activeProjectsUpToMarch31Count = projectRepository.getActiveProjectUptoMarch31Count(selectedDeptList);
//					internalActiveProjectsCount = projectRepository.getAllInternalActiveProjectsCount(selectedDeptList);
//					activeTNMCount = projectRepository.getAllActiveTNMProjectsCount(selectedDeptList);
//					
//					
//	                expiredProjectCounts = getExpiredProjectCountsByDateRanges(selectedDeptList);
//				    
//					totalCount = pendingForApprovalCount + approvedCount + rejectedCount;
//					map.put("pendingForApprovalCount",pendingForApprovalCount);
//					map.put("approvedCount",approvedCount);
//					map.put("completedCount",
//							projectRepository.getAllCompleteProjectInShankhCountstList( projectIdSet, true));
//					map.put("completedWithEmployeeCount", projectRepository
//							.completedInSankhButTeamMapped(projectIdSet, true));
//					map.put("notStartedCount",notStartedCount);
//					map.put("rejectedCount",rejectedCount);
//					map.put("completedInIshineCount", completedInIshine);
//					map.put("allTotalActiveProjectCount" , activeProject);
////					map.put("activeProjectGreaterThan31MarchCount", activeProjectsAfterMarch31Count);	
////					map.put("allActiveProjectsUpToMarch31Count", activeProjectsUpToMarch31Count);
//					map.put("allInternalActiveProjectsCounts", internalActiveProjectsCount);
//					map.put("allActiveTNMProjectsCount", activeTNMCount);
//					
//					
//					
//	                map.putAll(expiredProjectCounts);
//					
//					map.put("totalCount", totalCount);
//				}else {
//					List<Long> deptIdsAccToRole = departmentRepository.findAllDepartments() ;
//					
//					pendingForApprovalCount = projectRepository.getAllActiveProjecCountstList( "true", null, false);
//					approvedCount = projectRepository.getAllActiveProjecCountstList( "false", null, false);
//					notStartedCount =  projectRepository.getAllNotStartedProjectCountInDept(deptIdsAccToRole);
//					completedInIshine = projectRepository.getAllCompletedProjectCountInIshine(deptIdsAccToRole);
//					activeProject = projectRepository.getTotalAllActiveProjectCount(deptIdsAccToRole);
////					activeProjectsAfterMarch31Count = projectRepository.getActiveProjectGreaterThan31MarchCount(deptIdsAccToRole);
////					activeProjectsUpToMarch31Count = projectRepository.getActiveProjectUptoMarch31Count(deptIdsAccToRole);
//					internalActiveProjectsCount = projectRepository.getAllInternalActiveProjectsCount(deptIdsAccToRole);
//					activeTNMCount = projectRepository.getAllActiveTNMProjectsCount(deptIdsAccToRole);
//					
//					
//	                expiredProjectCounts = getExpiredProjectCountsByDateRanges(deptIdsAccToRole);
//					
//					
//					rejectedCount = projectRepository
//							.getAllActiveProjecCountstList("Rejected", null, false);
//					
//					totalCount = pendingForApprovalCount + approvedCount + rejectedCount;
//					map.put("pendingForApprovalCount",pendingForApprovalCount);
//				map.put("approvedCount",approvedCount);
//				map.put("completedCount",
//						projectRepository.getAllCompleteProjectInShankhCountstList( null, false));
//				map.put("completedWithEmployeeCount", projectRepository
//						.completedInSankhButTeamMapped( null,false));
//				map.put("notStartedCount",notStartedCount);
//				map.put("rejectedCount",rejectedCount );
//				map.put("completedInIshineCount", completedInIshine);
//				map.put("allTotalActiveProjectCount", activeProject);	
////				map.put("activeProjectGreaterThan31MarchCount", activeProjectsAfterMarch31Count);
////				map.put("allActiveProjectsUpToMarch31Count", activeProjectsUpToMarch31Count);
//				map.put("allInternalActiveProjectsCounts", internalActiveProjectsCount);
//				map.put("allActiveTNMProjectsCount", activeTNMCount);
//				
//				
//                map.putAll(expiredProjectCounts);
//                
//				map.put("totalCount", totalCount);
//				}
//				
//				responseData.setCounts(map);
//			}
//
//			else if (Boolean.TRUE.equals(projectFilterDTO.getIsHod())) {
//				List<Department> deptData = departmentRepository.findByHodId(projectFilterDTO.getCurrentUserEmpId());
//				if (deptData != null) {
//					for (Department data : deptData) {
//						if (data != null && data.getDeptId() != null) {
//							deptIdList.add(data.getDeptId());
//						}
//					}
//				}
//				
////				Set<Integer> matchingProjectIds = teamRepository.findAll().stream()
////					    .filter(team -> {
////					        if (team.getDeptIds() == null) return false;
////					        List<String> teamDeptIds = Arrays.asList(team.getDeptIds().split(","));
////					        return deptIdList.stream()
////					                .map(String::valueOf)
////					                .anyMatch(teamDeptIds::contains);
////					    })
////					    .map(Team::getProjectId)
////					    .filter(Objects::nonNull)
////					    .collect(Collectors.toSet());
//				
//				if (projectManagerMappingRepository.isUserProjectManagerOfAnyActiveInternalAndExternalProject(
//						projectFilterDTO.getCurrentUserEmpId())) {
//					projectIdListTemp = projectManagerMappingRepository
//							.isUserProjectManagerOfAnyActiveInternalProjectList(projectFilterDTO.getCurrentUserEmpId());
//					if (projectIdListTemp != null)
//						projectIdSet.addAll(projectIdListTemp);
//				}
//
//				if (projectOverheadMappingRepository.isUserProjectOverheadOfAnyActiveInternalAndExternalProject(
//						projectFilterDTO.getCurrentUserEmpId())) {
//					projectIdListTemp = projectOverheadMappingRepository
//							.isUserProjectOverheadOfAnyActiveInternalAndExternalProjectList(
//									projectFilterDTO.getCurrentUserEmpId());
//					if (projectIdListTemp != null)
//						projectIdSet.addAll(projectIdListTemp);
//				}
//
//				if (teamRepository.existsBySpocId(projectFilterDTO.getCurrentUserEmpId())) {
//					projectIdListTemp = teamRepository
//							.findActiveShankhInternalProjectIdsBySpocIdList(projectFilterDTO.getCurrentUserEmpId());
//					if (projectIdListTemp != null)
//						projectIdSet.addAll(projectIdListTemp);
////						projectIdSet.addAll(matchingProjectIds);
//				}
//				
//				 // Add projects from departments they manage 
//	            Set<Integer> deptProjectIds = teamRepository.findAll().stream()
//	                .filter(team -> {
//	                    if (team.getDeptIds() == null) return false;
//	                    List<String> teamDeptIds = Arrays.asList(team.getDeptIds().split(","));
//	                    return deptIdList.stream()
//	                            .map(String::valueOf)
//	                            .anyMatch(teamDeptIds::contains);
//	                })
//	                .map(Team::getProjectId)
//	                .filter(Objects::nonNull)
//	                .collect(Collectors.toSet());
//	            
//	            projectIdSet.addAll(deptProjectIds);
//				
//				if(!projectFilterDTO.getDepartmentsids().isEmpty() && projectFilterDTO.getDepartmentsids() != null ) {
//					List<Long> selectedDeptList= projectFilterDTO.getDepartmentsids();
//					Set<Integer> matchingProjIds = teamRepository.findAll().stream()
//						    .filter(team -> {
//						        if (team.getDeptIds() == null) return false;
//						        List<String> teamDeptIds = Arrays.asList(team.getDeptIds().split(","));
//						        return selectedDeptList.stream()
//						                .map(String::valueOf)
//						                .anyMatch(teamDeptIds::contains);
//						    })
//						    .map(Team::getProjectId)
//						    .filter(Objects::nonNull)
//						    .collect(Collectors.toSet());
//					
//					projectIdSet.retainAll(matchingProjIds);
//					
//					pendingForApprovalCount = projectRepository.getAllActiveProjecCountstList("true", projectIdSet, true);
//					approvedCount = projectRepository.getAllActiveProjecCountstList("false", projectIdSet, true);
//					notStartedCount =  projectRepository.getAllNotStartedProjectCountInDept(selectedDeptList);
//					rejectedCount = projectRepository.getAllActiveProjecCountstList( "Rejected", projectIdSet, true);
//					completedInIshine = projectRepository.getAllCompletedProjectCountInIshine(selectedDeptList);
//					activeProject = projectRepository.getTotalAllActiveProjectCount(selectedDeptList);
////					activeProjectsAfterMarch31Count = projectRepository.getActiveProjectGreaterThan31MarchCount(selectedDeptList);
////					activeProjectsUpToMarch31Count = projectRepository.getActiveProjectUptoMarch31Count(selectedDeptList);
//					internalActiveProjectsCount = projectRepository.getAllInternalActiveProjectsCount(selectedDeptList);
//					activeTNMCount = projectRepository.getAllActiveTNMProjectsCount(selectedDeptList);
//					
//					
//	                expiredProjectCounts = getExpiredProjectCountsByDateRanges(selectedDeptList);
//					
//					totalCount = pendingForApprovalCount + approvedCount + rejectedCount;
//					
//					map.put("pendingForApprovalCount",pendingForApprovalCount);
//					map.put("approvedCount",approvedCount);
//					map.put("completedCount",
//							projectRepository.getAllCompleteProjectInShankhCountstList( projectIdSet, true));
//					map.put("completedWithEmployeeCount", projectRepository
//							.completedInSankhButTeamMapped(projectIdSet, true));
//					map.put("notStartedCount",notStartedCount );
//					map.put("rejectedCount",rejectedCount );
//					map.put("completedInIshineCount",completedInIshine);
//					map.put("allTotalActiveProjectCount" , activeProject);
////					map.put("activeProjectGreaterThan31MarchCount", activeProjectsAfterMarch31Count);	
////					map.put("allActiveProjectsUpToMarch31Count", activeProjectsUpToMarch31Count);
//					map.put("allInternalActiveProjectsCounts", internalActiveProjectsCount);
//					map.put("allActiveTNMProjectsCount", activeTNMCount);
//					
//				
//	                map.putAll(expiredProjectCounts);
//					
//					map.put("totalCount", totalCount);
//				}else {
//					
////					List<Long> selectedDeptList= projectFilterDTO.getDepartmentsids();
//					
//					pendingForApprovalCount = projectRepository.getAllActiveProjecCountstList("true", projectIdSet, true);
//					approvedCount = projectRepository.getAllActiveProjecCountstList("false", projectIdSet, true);
//					notStartedCount =  projectRepository.getAllNotStartedProjectCountInDept(deptIdList);
//					completedInIshine = projectRepository.getAllCompletedProjectCountInIshine(deptIdList);
//					activeProject = projectRepository.getTotalAllActiveProjectCount(deptIdList);
////					activeProjectsAfterMarch31Count = projectRepository.getActiveProjectGreaterThan31MarchCount(deptIdList);
////					activeProjectsUpToMarch31Count = projectRepository.getActiveProjectUptoMarch31Count(deptIdList);
//					internalActiveProjectsCount = projectRepository.getAllInternalActiveProjectsCount(deptIdList);
//					rejectedCount = projectRepository.getAllActiveProjecCountstList( "Rejected", projectIdSet, true);
//					activeTNMCount = projectRepository.getAllActiveTNMProjectsCount(deptIdList);
//					
//					
//	                expiredProjectCounts = getExpiredProjectCountsByDateRanges(deptIdList);
//				    
//					totalCount = pendingForApprovalCount + approvedCount + rejectedCount;
//					
//					map.put("pendingForApprovalCount",pendingForApprovalCount);
//					map.put("approvedCount",approvedCount);
//					map.put("completedCount",
//							projectRepository.getAllCompleteProjectInShankhCountstList( projectIdSet, true));
//					map.put("completedWithEmployeeCount", projectRepository
//							.completedInSankhButTeamMapped(projectIdSet, true));
//					map.put("notStartedCount",notStartedCount );
//					map.put("rejectedCount",rejectedCount );
//					map.put("completedInIshineCount", completedInIshine);
//					map.put("allTotalActiveProjectCount" , activeProject);
////					map.put("activeProjectGreaterThan31MarchCount", activeProjectsAfterMarch31Count);	
////					map.put("allActiveProjectsUpToMarch31Count", activeProjectsUpToMarch31Count);
//					map.put("allInternalActiveProjectsCounts", internalActiveProjectsCount);
//					map.put("allActiveTNMProjectsCount", activeTNMCount);
//					
//	                map.putAll(expiredProjectCounts);
//					
//					map.put("totalCount", totalCount);
//				}
//				
//				
//				responseData.setCounts(map);
//			}
//
//			else if (Boolean.TRUE.equals(projectFilterDTO.getIsOther())) {
//
//				if (projectManagerMappingRepository.isUserProjectManagerOfAnyActiveInternalAndExternalProject(
//						projectFilterDTO.getCurrentUserEmpId())) {
//					projectIdListTemp = projectManagerMappingRepository
//							.isUserProjectManagerOfAnyActiveInternalProjectList(projectFilterDTO.getCurrentUserEmpId());
//					if (projectIdListTemp != null)
//						projectIdSet.addAll(projectIdListTemp);
//				}
//
//				if (projectOverheadMappingRepository.isUserProjectOverheadOfAnyActiveInternalAndExternalProject(
//						projectFilterDTO.getCurrentUserEmpId())) {
//					projectIdListTemp = projectOverheadMappingRepository
//							.isUserProjectOverheadOfAnyActiveInternalAndExternalProjectList(
//									projectFilterDTO.getCurrentUserEmpId());
//					if (projectIdListTemp != null)
//						projectIdSet.addAll(projectIdListTemp);
//				}
//
//				if (teamRepository.existsBySpocId(projectFilterDTO.getCurrentUserEmpId())) {
//					projectIdListTemp = teamRepository
//							.findActiveShankhInternalProjectIdsBySpocIdList(projectFilterDTO.getCurrentUserEmpId());
//					if (projectIdListTemp != null)
//						projectIdSet.addAll(projectIdListTemp);
//				}
//
//				
//				if (projectIdSet != null && !projectIdSet.isEmpty()){
//					if(!projectFilterDTO.getDepartmentsids().isEmpty() && projectFilterDTO.getDepartmentsids() != null ) {
//					List<Long> selectedDeptList= projectFilterDTO.getDepartmentsids();
//					Set<Integer> matchingProjectIds = teamRepository.findAll().stream()
//						    .filter(team -> {
//						        if (team.getDeptIds() == null) return false;
//						        List<String> teamDeptIds = Arrays.asList(team.getDeptIds().split(","));
//						        return selectedDeptList.stream()
//						                .map(String::valueOf)
//						                .anyMatch(teamDeptIds::contains);
//						    })
//						    .map(Team::getProjectId)
//						    .filter(Objects::nonNull)
//						    .collect(Collectors.toSet());
//					
//					projectIdSet.retainAll(matchingProjectIds);
//					
//					pendingForApprovalCount = projectRepository.getAllActiveProjecCountstList("true", projectIdSet, true);
//					approvedCount = projectRepository.getAllActiveProjecCountstList("false", projectIdSet, true);
//					notStartedCount =  projectRepository.getAllNotStartedProjectCountInDept(selectedDeptList);
//					completedInIshine = projectRepository.getAllCompletedProjectCountInIshine(selectedDeptList);
//					activeProject = projectRepository.getTotalAllActiveProjectCount(selectedDeptList);
////					activeProjectsAfterMarch31Count = projectRepository.getActiveProjectGreaterThan31MarchCount(selectedDeptList);
////					activeProjectsUpToMarch31Count = projectRepository.getActiveProjectUptoMarch31Count(selectedDeptList);
//					internalActiveProjectsCount = projectRepository.getAllInternalActiveProjectsCount(selectedDeptList);
//					rejectedCount = projectRepository.getAllActiveProjecCountstList( "Rejected", projectIdSet, true);
//					activeTNMCount = projectRepository.getAllActiveTNMProjectsCount(selectedDeptList);
//					
//					
//	                expiredProjectCounts = getExpiredProjectCountsByDateRanges(selectedDeptList);
//					
//					totalCount = pendingForApprovalCount + approvedCount  + rejectedCount;
//					
//					map.put("pendingForApprovalCount",pendingForApprovalCount);
//					map.put("approvedCount",approvedCount);
//					map.put("completedCount",projectRepository.getAllCompleteProjectInShankhCountstList( projectIdSet, true));
//					map.put("completedWithEmployeeCount", projectRepository.completedInSankhButTeamMapped(projectIdSet, true));
//					map.put("notStartedCount", notStartedCount);
//					map.put("rejectedCount", rejectedCount);
//					map.put("completedInIshineCount",completedInIshine);
//					map.put("allTotalActiveProjectCount" , activeProject);
////					map.put("activeProjectGreaterThan31MarchCount", activeProjectsAfterMarch31Count);	
////					map.put("allActiveProjectsUpToMarch31Count", activeProjectsUpToMarch31Count);
//					map.put("allInternalActiveProjectsCounts", internalActiveProjectsCount);
//					
//					
//	                map.putAll(expiredProjectCounts);
//					
//					map.put("allActiveTNMProjectsCount", activeTNMCount);
//					
//					map.put("totalCount", totalCount);
//					}else {
//						Employee employeee = employeeRepository.findByEmpId(projectFilterDTO.getCurrentUserEmpId());
//						List<Long> deptIdOfOther = departmentRepository.findDepartmentIdOfCurrentUser(employeee.getJobRoleId());
////						List<Long> selectedDeptList= projectFilterDTO.getDepartmentsids();
//						
//						pendingForApprovalCount = projectRepository.getAllActiveProjecCountstList("true", projectIdSet, true);
//						approvedCount = projectRepository.getAllActiveProjecCountstList("false", projectIdSet, true);
//						notStartedCount =  projectRepository.getAllNotStartedProjectCountInDept(deptIdOfOther);
//						completedInIshine = projectRepository.getAllCompletedProjectCountInIshine(deptIdOfOther);
//						activeProject = projectRepository.getTotalAllActiveProjectCount(deptIdOfOther);
////						activeProjectsAfterMarch31Count = projectRepository.getActiveProjectGreaterThan31MarchCount(deptIdOfOther);
////						activeProjectsUpToMarch31Count = projectRepository.getActiveProjectUptoMarch31Count(deptIdOfOther);
//						internalActiveProjectsCount = projectRepository.getAllInternalActiveProjectsCount(deptIdOfOther);
//						rejectedCount = projectRepository.getAllActiveProjecCountstList( "Rejected", projectIdSet, true);
//						activeTNMCount = projectRepository.getAllActiveTNMProjectsCount(deptIdOfOther);
//						
//					
//	                    expiredProjectCounts = getExpiredProjectCountsByDateRanges(deptIdOfOther);
//						
//						
//						totalCount = pendingForApprovalCount + approvedCount + notStartedCount + rejectedCount;
//						
//						map.put("pendingForApprovalCount",pendingForApprovalCount);
//						map.put("approvedCount",approvedCount);
//						map.put("completedCount",projectRepository.getAllCompleteProjectInShankhCountstList( projectIdSet, true));
//						map.put("completedWithEmployeeCount", projectRepository.completedInSankhButTeamMapped(projectIdSet, true));
//						map.put("notStartedCount", notStartedCount);
//						map.put("rejectedCount", rejectedCount);
//						map.put("completedInIshineCount",completedInIshine);
//						map.put("allTotalActiveProjectCount" , activeProject);
////						map.put("activeProjectGreaterThan31MarchCount", activeProjectsAfterMarch31Count);
////						map.put("allActiveProjectsUpToMarch31Count", activeProjectsUpToMarch31Count);
//						map.put("allInternalActiveProjectsCounts", internalActiveProjectsCount);
//						map.put("allActiveTNMProjectsCount", activeTNMCount);
//						
//					
//	                    map.putAll(expiredProjectCounts);
//						
//						map.put("totalCount", totalCount);
//					}
//					
//					
//					responseData.setCounts(map);
//				} else {
//					responseData = null;
//				}
//			}
//
//			if (responseData != null) {
//				response.setServiceResponse(responseData);
//				response.setServiceStatus(response.STATUS_SUCCESS);
//			} else {
//				response.setServiceResponse("User is not qualified to see any data");
//				response.setServiceStatus(response.STATUS_FAIL);
//			}
//			return response;
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			return failResponse(response, apiLogInfo, "Internal Server Error: " + e.getMessage());
//		}
//	}

	@Transactional(rollbackFor = Exception.class)
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
			List<Long> departmentsids = Optional.ofNullable(projectFilterDTO.getDepartmentsids())
					.orElse(new ArrayList<>());
			Set<String> deptIdStrings = departmentsids.stream().map(String::valueOf).collect(Collectors.toSet());

			// Fetch projects and team-created projects
			ServiceResponse apiResponse = poPortalAPIService.getAllProjectsFromPoPortal();
			List<ResourceManagementDTO> poPortalProjects;
			if (apiResponse == null) {
				throw new DataNotFoundException("Data not found");
			}
			if (ServiceResponse.STATUS_SUCCESS.equals(apiResponse.getServiceStatus())) {
				poPortalProjects = (List<ResourceManagementDTO>) apiResponse.getServiceResponse();
			} else {
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
			List<ResourceManagementDTO> teamCreatedProjects = castList(
					teamCreatedProjectsResponse.getServiceResponse());

			// Process roles
			Set<Long> spocPoIds = new HashSet<>();
			Set<Integer> spocInternalIds = new HashSet<>();
			Set<Long> hodPoIds = new HashSet<>();
			Set<Integer> hodInternalIds = new HashSet<>();
			Set<String> hodDeptIdSet = new HashSet<>();

			List<Long> hodDeptIds = Optional.ofNullable(departmentRepository.findByHodId(currentUserEmpId))
					.orElse(Collections.emptyList()).stream().map(Department::getDeptId).collect(Collectors.toList());
			hodDeptIdSet.addAll(hodDeptIds.stream().map(String::valueOf).collect(Collectors.toSet()));

			AtomicBoolean isSpoc = new AtomicBoolean(false);
			AtomicBoolean isHod = new AtomicBoolean(false);

			for (ResourceManagementDTO project : teamCreatedProjects) {
				List<TeamSpocDTO> spocs = Optional.ofNullable(project.getTeamSpocs()).orElse(Collections.emptyList());

				for (TeamSpocDTO spoc : spocs) {
					if (spoc == null)
						continue;

					if (currentUserEmpId != null && currentUserEmpId.equals(spoc.getSpocId())) {
						isSpoc.set(true);
						if (project.getPoProjectId() != null)
							spocPoIds.add(project.getPoProjectId());
						if (project.getProjectId() != null)
							spocInternalIds.add(project.getProjectId());
					}

					String[] spocDeptList = spoc.getDepartmentList();
					if (spocDeptList != null && Arrays.stream(spocDeptList).anyMatch(hodDeptIdSet::contains)) {
						isHod.set(true);
						if (project.getPoProjectId() != null)
							hodPoIds.add(project.getPoProjectId());
						if (project.getProjectId() != null)
							hodInternalIds.add(project.getProjectId());
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
					isSuperAdminOrDirector = "SuperAdmin".equalsIgnoreCase(r) || "Director".equalsIgnoreCase(n)
							|| "SuperAdmin".equalsIgnoreCase(n);
				}
			}

			// Tag project attributes
			processPoPortalProjects(poPortalProjects, teamCreatedProjects);
			processInternalProjects(internalProjects, teamCreatedProjects);

			// Merge lists
			List<ResourceManagementDTO> combined = new ArrayList<>();
			combined.addAll(poPortalProjects);
			combined.addAll(internalProjects);

			// Merge and deduplicate based on projectId
			Map<Integer, ResourceManagementDTO> uniqueMap = Stream
					.concat(poPortalProjects.stream(), internalProjects.stream())
					.collect(Collectors.toMap(ResourceManagementDTO::getProjectId, // key extractor
							Function.identity(), // value = object
							(existing, replacement) -> existing, // handle duplicates (keep first)
							LinkedHashMap::new // preserve order
					));

			// Final combined unique list
			List<ResourceManagementDTO> combinedUnique = new ArrayList<>(uniqueMap.values());

			// Apply role-based filtering
			if (!isSuperAdminOrDirector) {
				combined = combined.stream().filter(p -> {
					if (p == null)
						return false;
					if (isSpoc.get()) {
						return "Internal".equalsIgnoreCase(p.getIsDraftProject())
								|| "Not Started".equalsIgnoreCase(p.getIsDraftProject())
								|| (p.getId() != null && spocPoIds.contains(p.getId()))
								|| (p.getProjectId() != null && spocInternalIds.contains(p.getProjectId()));
					}
					if (isHod.get()) {
						return "Internal".equalsIgnoreCase(p.getIsDraftProject())
								|| "Not Started".equalsIgnoreCase(p.getIsDraftProject())
								|| (p.getId() != null && hodPoIds.contains(p.getId()))
								|| (p.getProjectId() != null && hodInternalIds.contains(p.getProjectId()));
					}
					return true;
				}).collect(Collectors.toList());
			}

			// Department-based filtering
			if (!deptIdStrings.isEmpty()) {
				combined = combined.stream()
						.filter(p -> p != null && p.getTeamSpocs() != null && p.getTeamSpocs().stream()
								.anyMatch(spoc -> spoc != null && spoc.getDepartmentList() != null
										&& Arrays.stream(spoc.getDepartmentList()).anyMatch(deptIdStrings::contains)))
						.collect(Collectors.toList());
			}

			// Count project status
			Map<String, Integer> countsMap = countStatus(combined);

			// Approval Status Filter

			if (approvalStatus != null && !"All".equalsIgnoreCase(approvalStatus)) {
				if ("completedInIshine".equalsIgnoreCase(approvalStatus)) {
					combined = combined.stream().filter(p -> "Completed".equalsIgnoreCase(p.getProjectStatus()))
							.collect(Collectors.toList());
				} else {
					combined = combined.stream()
							.filter(p -> approvalStatus.equalsIgnoreCase(p.getIsDraftProject())
									|| ("Not Started".equalsIgnoreCase(approvalStatus)
											&& "Internal".equalsIgnoreCase(p.getIsDraftProject())))
							.collect(Collectors.toList());
				}
			} else if ("All".equalsIgnoreCase(approvalStatus)) {
				combined = combinedUnique;
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
		map.put("completedInIshineCount",
				(int) projects.stream().filter(p -> "Completed".equalsIgnoreCase(p.getProjectStatus())).count());
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
				} else {
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

	@Transactional(rollbackFor = Exception.class)
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
						EmployeeTeamMap emp1 = employeeTeamMapRepository
								.findByEmployeeTeamMapId(resourceManagementDTO.getEmployeeTeamMapId());
						if (emp1 != null && emp1.getEndDate() == null && emp1.getActive() != 0) {
							Employee emp = employeeRepository.findByEmpId(resourceManagementDTO.getEmpId());
							findResource.setActive(0L);
							findResource.setRescRemovedBy(resourceManagementDTO.getCreatedBy());
							if (resourceManagementDTO.getPoRequirementMappingId() != null) {
								findResource
										.setPoRequirementMappingId(resourceManagementDTO.getPoRequirementMappingId());
							}
							// adding this flag for stating date is po / custom
							findResource.setIsCustomDate(resourceManagementDTO.getIsCustomDate());

							if (resourceManagementDTO.getEndDate() != null) {
								String str = resourceManagementDTO.getEndDate();
								DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
								LocalDate date = LocalDate.parse(str, formatter);
								LocalDateTime endDateTime = date.atStartOfDay();
								if (emp1.getStartDate().isAfter(endDateTime)) {
									throw new IllegalArgumentException(
											"End date cannot be less than Start date:" + findResource.getStartDate());
								}
								findResource.setEndDate(endDateTime);
							} else {

								findResource.setEndDate(LocalDateTime.now());
							}

							employeeTeamMapRepository.save(findResource);
							try {
								mailService.sendMail(rmgMail, "Regarding Resource removed from Project ",
										"Dear " + emp.getName() + "<br>" + "You have been removed from project "
												+ findProject.getProjectName() + "under the team - "
												+ findTeam.getTeamName() + "<br>" + "<br><br>" + "Sincerely," + "<br>"
												+ "Team RMG - ApMoSys Technologies");
							} catch (AddressException e) {

								e.printStackTrace();
							} catch (MessagingException e) {

								e.printStackTrace();
							}

							resultMessage.append("Resource with EmpId " + resourceManagementDTO.getEmpId()
									+ " from Team " + findTeam.getTeamName() + " removed successfully.\n");
						} else {
							failureCount++;
							resultMessage.append("No resource found with EmpId " + resourceManagementDTO.getEmpId()
									+ " in Team " + findTeam.getTeamName() + ".\n");
						}

					} catch (Exception e) {
						failureCount++;
						resultMessage.append("Failed to remove resource with EmpId " + resourceManagementDTO.getEmpId()
								+ " from Team " + findTeam.getTeamName() + ". Error: " + e.getMessage() + "\n");
					}
				} else {
					failureCount++;
					resultMessage.append("No resource found with EmpId " + resourceManagementDTO.getEmpId()
							+ " in Team " + findTeam.getTeamName() + ".\n");
				}
			}

			if (failureCount == 0) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Successfully removed resources");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse(resultMessage.toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse(e.getMessage());
//		throw e;
		}

		return response;
	}

	@Transactional(rollbackFor = Exception.class)
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
						emp.setRescRemovedBy(teamDto.getCreatedBy());

						emp.setUpdatedBy(teamDto.getUpdatedBy());
						emp.setUpdatedOn(LocalDateTime.now());
						if (teamDto.getEndDate() != null) {
							String str = teamDto.getEndDate();
							DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
							LocalDate date = LocalDate.parse(str, formatter);
							LocalDateTime endDateTime = date.atStartOfDay();
							if (emp.getStartDate().isAfter(endDateTime)) {
								throw new IllegalArgumentException("End cannot be less than start date..!");
							}
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
				TeamDTO team = teamDtos.get(0);
				Team findTeam = teamRepository.findTeamByTeamId(team.getTeamId());
				Project findProject = projectRepository.findByProjectId(findTeam.getProjectId());
				Employee removedByEmp = employeeRepository.findByEmpId(team.getCreatedBy());
				List<String> managerOverheadEmails = projectRepository
						.findProjectManagerAndProjectoverheadEmails(findProject.getProjectId());

				Set<String> toRecipients = new HashSet<>();
				toRecipients.add(bdMail);
				toRecipients.add(adminMail);
				toRecipients.add(rmgMail);
				toRecipients.add(financeMail);

				Set<String> ccRecipients = managerOverheadEmails.stream().filter(Objects::nonNull).map(String::trim)
						.filter(s -> !s.isEmpty()).collect(Collectors.toCollection(LinkedHashSet::new));
				ccRecipients.removeAll(toRecipients);

				StringBuilder deletedTeamsList = new StringBuilder();
				teamDtos.forEach(teamDto -> deletedTeamsList.append("<br>").append(teamDto.getTeamName()));

				String removedByName = (removedByEmp != null) ? removedByEmp.getName() : "System";

				mailService.sendMailWithCC(String.join(",", toRecipients), String.join(",", ccRecipients),
						"Regarding Team Deletion",
						"Dear All, <br><br>" + "The following teams have been deleted by <b>" + removedByName + "</b>, "
								+ "and the resources have been removed from these teams: " + deletedTeamsList.toString()
								+ "<br><br><b>Under Project:</b> " + findProject.getProjectName()
								+ "<br><br>Sincerely,<br>Team Ishine - ApMoSys Technologies");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {

			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse(e.getMessage());
		}

		return response;
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateProjectStartAndEndDate(ResourceManagementDTO resourceManagementDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			Long empid = resourceManagementDTO.getEmpId();
			Long teamid = resourceManagementDTO.getTeamId();
			Long employeeTeamMapId = resourceManagementDTO.getEmployeeTeamMapId();
			EmployeeTeamMap findResource = null;
//			findResource = employeeTeamMapRepository.findByEmpIdAndTeamId(empid, teamid);
			if (employeeTeamMapId != null) {
				findResource = employeeTeamMapRepository.findByEmployeeTeamMapId(employeeTeamMapId);
			}

			if (findResource == null) {
				findResource = employeeTeamMapRepository.findByEmpIdAndTeamIdAndActive(empid, teamid, 1L);
			}
			Team findTeam = teamRepository.findTeamByTeamId(resourceManagementDTO.getTeamId());

			System.out.println("findResource: " + findResource);

			if (findResource != null) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				System.out.println("resource start date : " + resourceManagementDTO.getStartDate());
				System.out.println("resource end date : " + resourceManagementDTO.getEndDate());
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
					// adding validation before setting the custom end date previously it was
					// setting without validation thats why even efter error it was setting end-date
					if (findResource.getStartDate() != null && findResource.getStartDate().isAfter(endDateTime)) {

						throw new IllegalArgumentException("End date cannot be less than start date..!");
					}
					findResource.setEndDate(endDateTime);

				}
				if (resourceManagementDTO.getStartDate() != null) {

					String str = resourceManagementDTO.getStartDate();
					LocalDateTime startDateTime;

					if (str.contains("T")) {
						startDateTime = LocalDateTime.parse(str);
					} else {
						LocalDate date = LocalDate.parse(str);
						startDateTime = date.atTime(LocalTime.now().getHour(), LocalTime.now().getMinute(),
								LocalTime.now().getSecond());
					}

					findResource.setStartDate(startDateTime);
					// String str = resourceManagementDTO.getStartDate();
					// LocalDate date = LocalDate.parse(str, formatter);
					// LocalDateTime startDateTime = date.atStartOfDay();
					// findResource.setStartDate(Timestamp.valueOf(startDateTime));
				}
//				else {
//					findResource.setEndDate(LocalDateTime.now());
//				}
				if (findResource.getEndDate() != null
						&& findResource.getStartDate().isAfter(findResource.getEndDate())) {
					throw new IllegalArgumentException("End date cannot be less than start date..!");
				}
				findResource.setUpdatedBy(resourceManagementDTO.getUpdatedBy());
				findResource.setUpdatedOn(LocalDateTime.now());

				employeeTeamMapRepository.save(findResource);

				System.out.println("Picked startDate = " + findResource.getStartDate());
				System.out.println("Picked endDate = " + findResource.getEndDate());

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(
						"Project StartDate/EndDate updated successfully, from Team Name - " + findTeam.getTeamName());
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Resource not found for given employeeTeamMapId");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Error occurred while updating StartDate/EndDate: " + e.getMessage());
		}
		return response;
	}

	@Transactional
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

			List<Team> teams = teamRepository.findByProjectIdAndIsActive(projectObj.getProjectId(), "Y");

			if (teams.isEmpty()) {
				logBuilder
						.append("\n Empty teamlist found in database for method findByProjectIdAndIsActive for project "
								+ projectObj.getProjectName());
			} else {
				List<TeamDTO> teamDTOs = teams.stream().map(team -> {
					TeamDTO dto = new TeamDTO();
					dto.setTeamId(team.getTeamId());
					dto.setUpdatedBy(projectObj.getUpdatedBy());
					return dto;
				}).collect(Collectors.toList());

				ServiceResponse response2 = deleteTeamsByIdsBulk(teamDTOs);
				logBuilder
						.append("\n " + response2.getServiceResponse() + " for project " + projectObj.getProjectName());

				if (response2.getServiceStatus() != ServiceResponse.STATUS_SUCCESS) {
					return response2;
				}
			}

			projectObj.setProjectCompletionDate(resourceManagementDTO.getProjectCompletionDate());
			projectObj.setActive("false");
			projectObj.setStatus(resourceManagementDTO.getStatus());
			projectObj.setProjectStatus(resourceManagementDTO.getProjectStatus());
			projectObj.setUpdatedBy(resourceManagementDTO.getUpdatedBy());
			projectObj.setUpdatedOn(LocalDateTime.now());
			Project projectDbResponse = projectRepository.save(projectObj);

			if (projectDbResponse != null) {
				try {
					Employee empupdatedBy = employeeRepository.findByEmpId(resourceManagementDTO.getUpdatedBy());
					List<String> managerOverheadEmails = projectRepository
							.findProjectManagerAndProjectoverheadEmails(projectObj.getProjectId());
					Set<String> toRecipients = new HashSet<>();
					toRecipients.add(bdMail);
					toRecipients.add(adminMail);
					toRecipients.add(rmgMail);
					toRecipients.add(financeMail);

					Set<String> ccRecipients = managerOverheadEmails.stream().filter(Objects::nonNull).map(String::trim)
							.filter(s -> !s.isEmpty()).collect(Collectors.toCollection(LinkedHashSet::new));
					ccRecipients.removeAll(toRecipients);

					String subject = "Project Completion Notification - " + projectObj.getProjectName();
					String body = "<p>The project <b>" + projectObj.getProjectName() + "</b> "
							+ "has been marked as completed in Ishine on " + projectObj.getProjectCompletionDate()
							+ ".</p>" + "<p><b>Updated by: " + empupdatedBy.getName() + "</b></p>";

					String to = String.join(",", toRecipients);
					String cc = String.join(",", managerOverheadEmails);

					mailService.sendMailWithCC(to, cc, subject, body);

				} catch (Exception mailEx) {
					logBuilder.append("\n Failed to send completion mail: " + mailEx.getMessage());
				}

				projectManagerMappingRepository
						.deactivateByProjectId(Long.parseLong(projectObj.getProjectId().toString()));
				projectOverheadMappingRepository
						.deactivateByProjectId(Long.parseLong(projectObj.getProjectId().toString()));
			}

			if (!resourceManagementDTO.getProjectType().equals("Internal")) {
				ServiceResponse poPortalResponse = sendProjectInfoToPoPortal(resourceManagementDTO);

				if (poPortalResponse.getServiceStatus().equals("Success")) {
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

			Set<String> specialDepartments = Set.of("Admin", "Resource Management Group", "Director", "Super Admin",
					"Accounts", "HR");
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
						if (teamDeptIdsStr == null || teamDeptIdsStr.isBlank())
							return false;

						List<Long> teamDeptIds = Arrays.stream(teamDeptIdsStr.split(",")).map(String::trim)
								.filter(s -> !s.isEmpty()).map(Long::parseLong).collect(Collectors.toList());

						return teamDeptIds.stream().anyMatch(deptIds::contains);
					}).map(Team::getProjectId).collect(Collectors.toSet());

					internalProjectIds.addAll(hodProjects);
				}
			}

			internalProjectIds = filterProjectidsApprovalStatusDepartmentFilter(internalProjectIds,
					projectFilterDTO.getApprovalStatus(), projectFilterDTO.getDepartmentsids());

			List<RMGFlatEmployeeProjectTeamDTO> rawData = employeeTeamMapRepository
					.findEmployeeProjectTeamDetailsByInternalProjectIds(internalProjectIds);
			

					rawData = groupEmployeeProjectTeamWise(rawData);
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

					newDto.setDepartment(row.getDepartment());
					newDto.setBillable(row.getBillable());
					newDto.setBillableType(row.getBillableType());
					newDto.setIsConsultant(row.getIsConsultant());
					newDto.setIsApprenticeship(row.getIsApprenticeship());
					newDto.setIsApmosysProduct(row.getIsApmosysProduct());

					String employmentId = newDto.getEmployeementId() != null ? newDto.getEmployeementId().toString()
							: null;
					String isConsultant = newDto.getIsConsultant();
					String isApmosysProduct = newDto.getIsApmosysProduct();

					if (employmentId != null) {
						if ("true".equalsIgnoreCase(isConsultant)) {
							newDto.setEmployeementIdAccToET("CS-" + employmentId);
						} else if ("true".equalsIgnoreCase(isApmosysProduct)) {
							newDto.setEmployeementIdAccToET("AP-" + employmentId);
						} else {
							newDto.setEmployeementIdAccToET("A-" + employmentId);
						}
					}
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
					rmgProject.setPoStartDate(row.getPoStartDate());
					rmgProject.setPoEndDate(row.getPoEndDate());
					rmgProject.setApmosysRM(row.getApmosysRM());
					rmgProject.setClientRM(row.getClientRM());
					rmgProject.setPoProjectType(row.getPoProjectType());
					rmgProject.setPoNo(row.getPoNo());
					rmgProject.setClientName(row.getClientName());
					rmgProject.setRmgTeam(new ArrayList<>());
					rmgProject.setProjectManagers(new ArrayList<>());
					dto.getRmgprojects().add(rmgProject);
					existingProject = rmgProject;
				}

				// Add team details to the project
				RMGTeam rmgTeam = new RMGTeam();
				rmgTeam.setTeamId(row.getTeamId());
				rmgTeam.setTeamName(row.getTeamName());
				rmgTeam.setIsActive(row.getTeamIsActive());
				rmgTeam.setEmployeeRole(row.getEmployeeRole());
				rmgTeam.setStatus(row.getEtmActive() == 1 ? "Approved" : "Pending for Approval");

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

			Set<String> specialDepartments = Set.of("Admin", "Resource Management Group", "Director", "Super Admin",
					"Accounts", "HR");
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
//		    	Set<Integer> combinedProjectIds = new HashSet<>();
//		    	combinedProjectIds.addAll(projectIdsByManager);
//		    	combinedProjectIds.addAll(projectIdsByOverhead);
//		    	combinedProjectIds.addAll(projectIdsBySpocOrTeamLead);
//		    	shankhProjectIds = combinedProjectIds;

				if (departmentRepository.existsByHodId(empIdd)) {
					List<Long> deptIds = departmentRepository.findDeptIdsByHodId(empIdd);
					List<Team> activeTeams = teamRepository.findAllActiveTeamsOfShankhProjects();

					Set<Integer> hodProjects = activeTeams.stream().filter(team -> {
						String teamDeptIdsStr = team.getDeptIds();
						if (teamDeptIdsStr == null || teamDeptIdsStr.isBlank())
							return false;

						List<Long> teamDeptIds = Arrays.stream(teamDeptIdsStr.split(",")).map(String::trim)
								.filter(s -> !s.isEmpty()).map(Long::parseLong).collect(Collectors.toList());

						return teamDeptIds.stream().anyMatch(deptIds::contains);
					}).map(Team::getProjectId).collect(Collectors.toSet());

					shankhProjectIds.addAll(hodProjects);
				}
			}

			shankhProjectIds = filterProjectidsApprovalStatusDepartmentFilter(shankhProjectIds,
					projectFilterDTO.getApprovalStatus(), projectFilterDTO.getDepartmentsids());

			List<RMGFlatEmployeeProjectTeamDTO> rawData = employeeTeamMapRepository
					.findEmployeeProjectTeamDetailsByProjectIds(shankhProjectIds);

			rawData = groupEmployeeProjectTeamWise(rawData);
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
					newDto.setDepartment(row.getDepartment());
					newDto.setBillable(row.getBillable());
					newDto.setBillableType(row.getBillableType());
					newDto.setIsConsultant(row.getIsConsultant());
					newDto.setIsApprenticeship(row.getIsApprenticeship());
					newDto.setIsApmosysProduct(row.getIsApmosysProduct());

					String employmentId = newDto.getEmployeementId() != null ? newDto.getEmployeementId().toString()
							: null;
					String isConsultant = newDto.getIsConsultant();
					String isApmosysProduct = newDto.getIsApmosysProduct();

					if (employmentId != null) {
						if ("true".equalsIgnoreCase(isConsultant)) {
				            newDto.setEmployeementIdAccToET("CS-" + employmentId);
				        } else if ("true".equalsIgnoreCase(isApmosysProduct)) {
							newDto.setEmployeementIdAccToET("AP-" + employmentId);
						} else {
							newDto.setEmployeementIdAccToET("A-" + employmentId);
						}
					}

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
					rmgProject.setPoStartDate(row.getPoStartDate());
					rmgProject.setPoEndDate(row.getPoEndDate());
					rmgProject.setApmosysRM(row.getApmosysRM());
					rmgProject.setClientRM(row.getClientRM());
					rmgProject.setPoProjectType(row.getPoProjectType());
					rmgProject.setPoNo(row.getPoNo());
					rmgProject.setClientName(row.getClientName());
					rmgProject.setRmgTeam(new ArrayList<>());
					rmgProject.setProjectManagers(new ArrayList<>());
					dto.getRmgprojects().add(rmgProject);
					existingProject = rmgProject;
				}

				// Add team details to the project
				RMGTeam rmgTeam = new RMGTeam();
				rmgTeam.setTeamId(row.getTeamId());
				rmgTeam.setTeamName(row.getTeamName());
				rmgTeam.setIsActive(row.getTeamIsActive());
				rmgTeam.setEmployeeRole(row.getEmployeeRole());
				rmgTeam.setStatus(row.getEtmActive() == 1 ? "Approved" : "Pending for Approval");

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

			Set<String> specialDepartments = Set.of("Admin", "Resource Management Group", "Director", "Super Admin",
					"Accounts", "HR");
			Set<Integer> allshankhInternalProjectIds = new HashSet<>();

			if (role.equalsIgnoreCase("SuperAdmin") || name.equalsIgnoreCase("Director")
					|| name.equalsIgnoreCase("Super Admin") || role.equalsIgnoreCase("Accounts")
					|| specialDepartments.contains(departmentName)) {
				allshankhInternalProjectIds = projectRepository.findAllActiveShankhInternalProjectIds();
			} else {

				allshankhInternalProjectIds = projectRepository
						.findAllShankhInternalProjectsByManagerOverheadOrSpocOrTeamLead(empIdd);
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
						if (teamDeptIdsStr == null || teamDeptIdsStr.isBlank())
							return false;

						List<Long> teamDeptIds = Arrays.stream(teamDeptIdsStr.split(",")).map(String::trim)
								.filter(s -> !s.isEmpty()).map(Long::parseLong).collect(Collectors.toList());

						return teamDeptIds.stream().anyMatch(deptIds::contains);
					}).map(Team::getProjectId).collect(Collectors.toSet());

					allshankhInternalProjectIds.addAll(hodProjects);
				}
			}

			List<RMGProjectToEmployeeFlatDTO> rawData = employeeTeamMapRepository.findNonComplianceProjects(
					allshankhInternalProjectIds, nonComplianceProjects.getFromDate(),
					nonComplianceProjects.getToDate());

			System.err.println("lalalacount" + rawData.size());
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

				GetProjectToEmployeeReportForProjectDTO projectDTO = projectMap
						.computeIfAbsent(row.getProjectId().longValue(), id -> {
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
				if (!pmExists && row.getPmEmpId() != null) {
					ProjectManagersDTO pmDTO = new ProjectManagersDTO();
					pmDTO.setProjectManagerId(row.getPmEmpId());
					pmDTO.setProjectManagerName(row.getPmName());
					projectDTO.getProjectManagers().add(pmDTO);
				}

				GetProjectToEmployeeReportForTeamDTO teamDTO = projectDTO.getTeamDetails().stream()
						.filter(t -> t.getTeamId().equals(row.getTeamId())).findFirst().orElseGet(() -> {
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
				empDTO.setEffectiveStartDate(
						row.getEffectiveStartDate() != null ? row.getEffectiveStartDate().toString() : "N/A");

				teamDTO.getMappedEmployeeDetails().add(empDTO);
			}

			List<GetProjectToEmployeeReportForProjectDTO> finalResult = new ArrayList<>(projectMap.values());
			response.setServiceResponse(finalResult);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		} catch (Exception e) {
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

			Set<String> specialDepartments = Set.of("Admin", "Resource Management Group", "Director", "Super Admin",
					"Accounts", "HR");
			Set<Integer> allshankhInternalProjectIds = new HashSet<>();

			if (role.equalsIgnoreCase("SuperAdmin") || name.equalsIgnoreCase("Director")
					|| name.equalsIgnoreCase("Super Admin") || role.equalsIgnoreCase("Accounts")
					|| specialDepartments.contains(departmentName)) {
				allshankhInternalProjectIds = projectRepository.findAllActiveShankhInternalProjectIds();
			} else {
				allshankhInternalProjectIds = projectRepository
						.findAllShankhInternalProjectsByManagerOverheadOrSpocOrTeamLead(empIdd);
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
						if (teamDeptIdsStr == null || teamDeptIdsStr.isBlank())
							return false;

						List<Long> teamDeptIds = Arrays.stream(teamDeptIdsStr.split(",")).map(String::trim)
								.filter(s -> !s.isEmpty()).map(Long::parseLong).collect(Collectors.toList());

						return teamDeptIds.stream().anyMatch(deptIds::contains);
					}).map(Team::getProjectId).collect(Collectors.toSet());

					allshankhInternalProjectIds.addAll(hodProjects);
				}
			}

			allshankhInternalProjectIds = filterProjectidsApprovalStatusDepartmentFilter(allshankhInternalProjectIds,
					projectFilterDTO.getApprovalStatus(), projectFilterDTO.getDepartmentsids());

			List<RMGFlatEmployeeProjectTeamDTO> rawData = employeeTeamMapRepository
					.findEmployeeProjectTeamDetailsByProjectIds(allshankhInternalProjectIds);
			
			rawData = groupEmployeeProjectTeamWise(rawData);
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

					newDto.setDepartment(row.getDepartment());
					newDto.setBillable(row.getBillable());
					newDto.setBillableType(row.getBillableType());

					newDto.setIsConsultant(row.getIsConsultant());
					newDto.setIsApprenticeship(row.getIsApprenticeship());
					newDto.setIsApmosysProduct(row.getIsApmosysProduct());

					String employmentId = newDto.getEmployeementId() != null ? newDto.getEmployeementId().toString()
							: null;
					String isConsultant = newDto.getIsConsultant();
					String isApmosysProduct = newDto.getIsApmosysProduct();

					if (employmentId != null) {
						if ("true".equalsIgnoreCase(isConsultant)) {
							newDto.setEmployeementIdAccToET("CS-" + employmentId);
						} else if ("true".equalsIgnoreCase(isApmosysProduct)) {
							newDto.setEmployeementIdAccToET("AP-" + employmentId);
						} else {
							newDto.setEmployeementIdAccToET("A-" + employmentId);
						}
					}
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
					rmgProject.setPoStartDate(row.getPoStartDate());
					rmgProject.setPoEndDate(row.getPoEndDate());
					rmgProject.setApmosysRM(row.getApmosysRM());
					rmgProject.setClientRM(row.getClientRM());
					rmgProject.setPoProjectType(row.getPoProjectType());
					rmgProject.setPoNo(row.getPoNo());
					rmgProject.setClientName(row.getClientName());
					rmgProject.setRmgTeam(new ArrayList<>());
					rmgProject.setProjectManagers(new ArrayList<>());

					dto.getRmgprojects().add(rmgProject);
					existingProject = rmgProject;
				}

				// Add team details to the project
				RMGTeam rmgTeam = new RMGTeam();
				rmgTeam.setTeamId(row.getTeamId());
				rmgTeam.setTeamName(row.getTeamName());
				rmgTeam.setIsActive(row.getTeamIsActive());
				rmgTeam.setEmployeeRole(row.getEmployeeRole());
				rmgTeam.setStatus(row.getEtmActive() == 1 ? "Approved" : "Pending for Approval");

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

	public ServiceResponse employeesMappedProjectsDepartmentWise(
			GetEmployeeProjectReportPayloadDTO getEmployeeProjectReportPayloadDTO) {
		ServiceResponse response = new ServiceResponse();

		try {
			List<Long> deptIds = getEmployeeProjectReportPayloadDTO.getDeptId();
			Set<Integer> internalProjectIds = new HashSet<>();

			internalProjectIds = projectRepository.findAllActiveShankhInternalProjectIds();

			List<Object[]> rawData = employeeTeamMapRepository.findEmployeeProjectTeamDetailsByProjectIdsAndDepartment(
					internalProjectIds, deptIds, getEmployeeProjectReportPayloadDTO.getHideMaternityLeaveEmps());
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
				String isApmosysProductt = row[23] != null ? row[23].toString() : null;

				RMGProjectMappedEmployees dto = employeeMap.computeIfAbsent(empId, k -> {
					RMGProjectMappedEmployees newDto = new RMGProjectMappedEmployees();
					newDto.setEmpId(empId);
					newDto.setEmployeementId(employeementId);
					newDto.setName(empName);
					newDto.setDepartment(deptName);
					newDto.setBillable(billable);
					newDto.setBillableType(billableType);
					newDto.setIsApmosysProduct(isApmosysProductt);
					String employmentId = newDto.getEmployeementId() != null ? newDto.getEmployeementId().toString()
							: null;
					String isConsultant = newDto.getIsConsultant();
					String isApmosysProduct = newDto.getIsApmosysProduct();

					if (employmentId != null) {
						if ("true".equalsIgnoreCase(isApmosysProduct)) {
							newDto.setEmployeementIdAccToET("AP-" + employmentId);
						} else {
							newDto.setEmployeementIdAccToET("A-" + employmentId);
						}
					}

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
					rmgProject.setProjectStartDate(poStartDate);
					rmgProject.setProjectEndDate(poEndDate);
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

			Set<String> specialDepartments = Set.of("Admin", "Resource Management Group", "Director", "Super Admin",
					"Accounts", "HR");
			Set<Integer> allshankhInternalProjectIds = new HashSet<>();

			if (role.equalsIgnoreCase("SuperAdmin") || name.equalsIgnoreCase("Director")
					|| name.equalsIgnoreCase("Super Admin") || role.equalsIgnoreCase("Accounts")
					|| specialDepartments.contains(departmentName)) {
				allshankhInternalProjectIds = projectRepository.findAllActiveShankhInternalProjectIds();
			} else {

				allshankhInternalProjectIds = projectRepository
						.findAllShankhInternalProjectsByManagerOverheadOrSpocOrTeamLead(empIdd);

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
						if (teamDeptIdsStr == null || teamDeptIdsStr.isBlank())
							return false;

						List<Long> teamDeptIds = Arrays.stream(teamDeptIdsStr.split(",")).map(String::trim)
								.filter(s -> !s.isEmpty()).map(Long::parseLong).collect(Collectors.toList());

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
					newDto.setEmployeementId(row.getEmployeementId());
					newDto.setName(row.getName());

					newDto.setDepartment(row.getDepartment());
					newDto.setBillable(row.getBillable());
					newDto.setBillableType(row.getBillableType());
					newDto.setIsConsultant(row.getIsConsultant());
					newDto.setIsApprenticeship(row.getIsApprenticeship());
					newDto.setIsApmosysProduct(row.getIsApmosysProduct());

					String employmentId = newDto.getEmployeementId() != null ? newDto.getEmployeementId().toString()
							: null;
					String isConsultant = newDto.getIsConsultant();
					String isApmosysProduct = newDto.getIsApmosysProduct();

					if (employmentId != null) {
						if ("true".equalsIgnoreCase(isConsultant)) {
							newDto.setEmployeementIdAccToET("CS-" + employmentId);
						} else if ("true".equalsIgnoreCase(isApmosysProduct)) {
							newDto.setEmployeementIdAccToET("AP-" + employmentId);
						} else {
							newDto.setEmployeementIdAccToET("A-" + employmentId);
						}
					}
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
					rmgProject.setPoStartDate(row.getPoStartDate());
					rmgProject.setPoEndDate(row.getPoEndDate());
					rmgProject.setApmosysRM(row.getApmosysRM());
					rmgProject.setClientRM(row.getClientRM());
					rmgProject.setPoProjectType(row.getPoProjectType());
					rmgProject.setPoNo(row.getPoNo());
					rmgProject.setClientName(row.getClientName());
					rmgProject.setRmgTeam(new ArrayList<>());
					rmgProject.setProjectManagers(new ArrayList<>());

					dto.getRmgprojects().add(rmgProject);
					existingProject = rmgProject;
				}

				// Add team details to the project
				RMGTeam rmgTeam = new RMGTeam();
				rmgTeam.setTeamId(row.getTeamId());
				rmgTeam.setTeamName(row.getTeamName());
				rmgTeam.setIsActive(row.getTeamIsActive());
				rmgTeam.setEmployeeRole(row.getEmployeeRole());
				rmgTeam.setStatus(row.getEtmActive() == 1 ? "Approved" : "Pending for Approval");

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

			Set<String> specialDepartments = Set.of("Admin", "Resource Management Group", "Director", "Super Admin",
					"Accounts", "HR");

			List<EmployeeDTO> employeesWithoutProjects = new ArrayList<>();

			if (role.equalsIgnoreCase("SuperAdmin") || name.equalsIgnoreCase("Director")
					|| name.equalsIgnoreCase("Super Admin") || role.equalsIgnoreCase("Accounts")
					|| specialDepartments.contains(departmentName)) {
				employeesWithoutProjects = employeeRepository.findAllEmployeesWithoutAnyProject();
			} else if (departmentRepository.existsByHodId(projectFilterDTO.getCurrentUserEmpId())) {
				List<Long> deptIds = departmentRepository.findDeptIdsByHodId(projectFilterDTO.getCurrentUserEmpId());
				employeesWithoutProjects = employeeRepository.findAllEmployeesWithoutProjectInDeptIds(deptIds);
			} else {
				Employee employeee = employeeRepository.findByEmpId(projectFilterDTO.getCurrentUserEmpId());
				Long deptId = departmentRepository.findDepartmentIdOfSpoc(employeee.getJobRoleId());
				employeesWithoutProjects = employeeRepository.findAllEmployeesWithoutProjectInDeptId(deptId);
			}

			for (EmployeeDTO newDto : employeesWithoutProjects) {
				String employmentId = newDto.getEmployeementId() != null ? newDto.getEmployeementId().toString() : null;
				String isConsultant = newDto.getIsConsultant();
				String isApmosysProduct = newDto.getIsApmosysProduct();

				if (employmentId != null) {
					if ("true".equalsIgnoreCase(isApmosysProduct)) {
						newDto.setEmploymentIdAcToET("AP-" + employmentId);
//		                    newDto.setEmployeementIdAccToET("AP-" + employmentId);
					} else {
						newDto.setEmploymentIdAcToET("A-" + employmentId);
//		                    newDto.setEmployeementIdAccToET("A-" + employmentId);
					}
				}
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

			Set<String> specialDepartments = Set.of("Admin", "Resource Management Group", "Director", "Super Admin",
					"Accounts", "HR");

			List<EmployeeDTO> employeesWithoutBillable = new ArrayList<>();

			if (role.equalsIgnoreCase("SuperAdmin") || name.equalsIgnoreCase("Director")
					|| name.equalsIgnoreCase("Super Admin") || role.equalsIgnoreCase("Accounts")
					|| specialDepartments.contains(departmentName)) {
				employeesWithoutBillable = employeeRepository.findAllEmployeesWithoutAnyBillable();
			} else if (departmentRepository.existsByHodId(projectFilterDTO.getCurrentUserEmpId())) {
				List<Long> deptIds = departmentRepository.findDeptIdsByHodId(projectFilterDTO.getCurrentUserEmpId());
				employeesWithoutBillable = employeeRepository.findAllEmployeesWithoutAnyBillableInDeptIds(deptIds);
			} else {
				Employee employeee = employeeRepository.findByEmpId(projectFilterDTO.getCurrentUserEmpId());
				Long deptId = departmentRepository.findDepartmentIdOfSpoc(employeee.getJobRoleId());
				employeesWithoutBillable = employeeRepository.findAllEmployeesWithoutBillableInDeptId(deptId);
			}

			for (EmployeeDTO newDto : employeesWithoutBillable) {
				String employmentId = newDto.getEmployeementId() != null ? newDto.getEmployeementId().toString() : null;
				String isConsultant = newDto.getIsConsultant();
				String isApmosysProduct = newDto.getIsApmosysProduct();

				if (employmentId != null) {
					if ("true".equalsIgnoreCase(isApmosysProduct)) {
						newDto.setEmploymentIdAcToET("AP-" + employmentId);
//		                    newDto.setEmployeementIdAccToET("AP-" + employmentId);
					} else {
						newDto.setEmploymentIdAcToET("A-" + employmentId);
//		                    newDto.setEmployeementIdAccToET("A-" + employmentId);
					}
				}
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

	public ServiceResponse getEmployessWithoutProjectsDepartmentWise(
			GetEmployeeProjectReportPayloadDTO getEmployeeProjectReportPayloadDTO) {
		ServiceResponse response = new ServiceResponse();

		try {
			List<Long> departmentIds = getEmployeeProjectReportPayloadDTO.getDeptId();
			List<Object[]> employeesWithoutProjects = new ArrayList<>();

			employeesWithoutProjects = employeeRepository.findAllEmployeesWithoutAnyProjectDepartmentWise(departmentIds,
					getEmployeeProjectReportPayloadDTO.getHideMaternityLeaveEmps());
			List<EmployeeDTO> employeeDTOList = new ArrayList<>();
			for (Object[] row : employeesWithoutProjects) {
				EmployeeDTO employeeDTO = new EmployeeDTO();
				employeeDTO.setEmpId(row[0] != null ? Long.parseLong(row[0].toString()) : null);
				employeeDTO.setEmployeementId(row[1] != null ? Long.parseLong(row[1].toString()) : null);
				employeeDTO.setEmail(row[2] != null ? row[2].toString() : null);
				employeeDTO.setEmploymentstatus(row[3] != null ? row[3].toString() : null);
				employeeDTO.setMobileNo(row[4] != null ? Long.parseLong(row[4].toString()) : null);
				employeeDTO.setManagerId(row[5] != null ? Long.parseLong(row[5].toString()) : null);
				employeeDTO.setManagerName(row[6] != null ? row[6].toString() : null);
				employeeDTO.setJobRoleName(row[7] != null ? row[7].toString() : null);
				employeeDTO.setDepartmentName(row[8] != null ? row[8].toString() : null);
				employeeDTO.setName(row[9] != null ? row[9].toString() : null);
				employeeDTO.setBillableType(row[10] != null ? row[10].toString() : null);
				employeeDTO.setIsApmosysProduct(row[11] != null ? row[11].toString() : null);

				String employmentId = employeeDTO.getEmployeementId() != null
						? employeeDTO.getEmployeementId().toString()
						: null;

				String isApmosysProduct = employeeDTO.getIsApmosysProduct();

				if (employmentId != null) {
					if ("true".equalsIgnoreCase(isApmosysProduct)) {
						employeeDTO.setEmploymentIdAcToET("AP-" + employmentId);
//		                	  employeeDTO.setEmployeementIdAccToET("AP-" + employmentId);
					} else {
						employeeDTO.setEmploymentIdAcToET("A-" + employmentId);
//		                	employeeDTO.setEmployeementIdAccToET("A-" + employmentId);
					}
				}

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
					Long isActive = object.getIsActive() != null ? object.getIsActive().longValue() : null;
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
					} else if ("All".equalsIgnoreCase(approvalStatus)) {
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

	public ServiceResponse totalEmployeeCountInDepartments(
			GetEmployeeProjectReportPayloadDTO getEmployeeProjectReportPayloadDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Long> deptIds = getEmployeeProjectReportPayloadDTO.getDeptId();
			Long employeeActiveCount = employeeRepository.getTotalEmployeeCountInDepartments(deptIds,
					getEmployeeProjectReportPayloadDTO.getHideMaternityLeaveEmps());
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(employeeActiveCount);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;

	}

//	public ServiceResponse getResourceRequirementByPoProjectId(Long id) {
//		ServiceResponse response = new ServiceResponse();
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setSubFeatureName("getResourceRequirementByPoProjectId");
//		apiLogInfo.setApiUrl("/api/getResourceRequirementByPoProjectId");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//
//
//		try {
//			ServiceResponse projectApiResponse = poPortalAPIService.fetchPoPortalProjectById(id);
//			if (projectApiResponse.getServiceStatus().equals(ServiceResponse.STATUS_FAIL)) {
//		         return projectApiResponse;
//			}
//			List<ResourceRequirementResponse> project = (List<ResourceRequirementResponse>) projectApiResponse.getServiceResponse();
//			System.out.println("======================"+project);
//
//			if (project == null) {
//
//				response.setServiceResponse("No Resource Requirement Found!");
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				logBuilder.append("\n No Resource Requirement Found!");
//
//			} else {	
//				
//				ServiceResponse countApiResponse = poPortalAPIService.getCountByProjectId(id);
//				if(countApiResponse.getServiceResponse()!= null){
//	
//				if (ServiceResponse.STATUS_FAIL.equals(countApiResponse.getServiceStatus())) {
//		            throw new RuntimeException("Failed to fetch count from PO Portal." );      
//		        }
//				
//				ResourceRequirementDTO requirementCountDto = new ResourceRequirementDTO();
//				if(countApiResponse.getServiceResponse() != null) {
//				requirementCountDto.setCount(Integer.parseInt(countApiResponse.getServiceResponse().toString()));}
//				if (requirementCountDto == null) {
//		             response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//		             response.setServiceResponse("Received success status from PO Portal, but requirement data was null.");
//		             return response;
//		        }
//				
//				int totalRequirements = requirementCountDto.getCount();
//				int assigned = projectRepository.getAssignedEmployeesCountInProject(id);
//				int difference = totalRequirements - assigned;
//
//				ProjectRequirementsDTO dto = new ProjectRequirementsDTO();
//				dto.setTotalRequirements(totalRequirements);
//				dto.setAssigned(assigned);
//				dto.setDifference(difference);
//
//				response.setServiceResponse(dto);
//				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				logBuilder.append("\n Fetched project requirement details correctly!");
//
//				return response;
//			}
//		}
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//			response.setServiceResponse("\n Something went wrong!");
//		}
//		return response;
//	}

	private List<ResourceManagementDTO> fetchPoPortalProjects() {
		String traceId = UUID.randomUUID().toString();
		ApiLog initialLog = null;
		List<ResourceManagementDTO> poPortalProjects = new ArrayList<>();

		try {

			initialLog = apiLogUtility.startLog(traceId, "fetchPoPortalProjects", "poPortal", null, httpRequest);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("X-Trace-Id", traceId);
			headers.set("Authorization", poPortalAPIAuthenticationJWTUtility.generateAccessToken());

			HttpEntity<?> requestEntity = new HttpEntity<>(headers);

			ResponseEntity<ResourceManagementDTO[]> responseEntity = restTemplate.exchange(allPoPortalProjects,
					HttpMethod.GET, requestEntity, ResourceManagementDTO[].class);

			ResourceManagementDTO[] poPortalProjectArray = responseEntity.getBody();
			poPortalProjects = Arrays
					.asList(poPortalProjectArray != null ? poPortalProjectArray : new ResourceManagementDTO[0]);

			apiLogUtility.endLog(null, traceId, 0, traceId, httpRequest);

		} catch (RestClientException e) {
			apiLogUtility.endLog(null, traceId, 0, traceId, httpRequest);
			throw new RuntimeException("Error fetching projects from PoPortal: " + e.getMessage(), e);
		}

		return poPortalProjects;
	}

//	public ServiceResponse getResourceRequirementByPoProjectId(Long id) {
//		ServiceResponse response = new ServiceResponse();
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setSubFeatureName("getResourceRequirementByPoProjectId");
//		apiLogInfo.setApiUrl("/api/getResourceRequirementByPoProjectId");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append(
//				"\n getResourceRequirementByPoProjectId " + projectRepository.getAssignedEmployeesCountInProject(id));
//		
//		System.out.println(projectRepository.getAssignedEmployeesCountInProject(id));
//
//		try {
//			List<ResourceManagementDTO> poPortalProjects = fetchPoPortalProjects();
//
//			Optional<ResourceManagementDTO> projectDTO = poPortalProjects.stream()
//					.filter(dto -> dto.getId() != null && dto.getId().equals(id)).findFirst();
//			
//			System.out.println(projectDTO.isPresent());
//
//			if (!projectDTO.isPresent()) {
//
//				response.setServiceResponse("Unable to fetched project requirement details correctly!");
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				logBuilder.append("\n Unable to fetched project requirement details correctly!");
//
//				return response;
//
//			} else {
//				ResourceManagementDTO project = projectDTO.get();
//
//				int totalRequirements = project.getResourceRequirements().stream()
//						.mapToInt(ResourceRequirementDTO::getCount).sum();
//				int assigned = projectRepository.getAssignedEmployeesCountInProject(id);
//				int difference = totalRequirements - assigned;
//
//				ProjectRequirementsDTO dto = new ProjectRequirementsDTO();
//				dto.setTotalRequirements(totalRequirements);
//				dto.setAssigned(assigned);
//				dto.setDifference(difference);
//
//				response.setServiceResponse(dto);
//				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				logBuilder.append("\n Fetched project requirement details correctly!");
//
//				return response;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//			response.setServiceResponse("\n Something went wrong!");
//		}
//		return response;
//	}
//

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
			List<ResourceRequirementResponse> project = (List<ResourceRequirementResponse>) projectApiResponse
					.getServiceResponse();
			System.out.println("======================" + project);

			if (project == null) {

				response.setServiceResponse("No Resource Requirement Found!");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				logBuilder.append("\n No Resource Requirement Found!");

				return response;

			} else {

				ServiceResponse countApiResponse = poPortalAPIService.getCountByProjectId(id);
				if (countApiResponse.getServiceResponse() != null) {

					if (ServiceResponse.STATUS_FAIL.equals(countApiResponse.getServiceStatus())) {
						throw new RuntimeException("Failed to fetch count from PO Portal.");
					}

					ResourceRequirementDTO requirementCountDto = new ResourceRequirementDTO();

					if (countApiResponse.getServiceResponse() != null) {
						requirementCountDto
								.setCount(Integer.parseInt(countApiResponse.getServiceResponse().toString()));
					}
					if (requirementCountDto == null) {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse(
								"Received success status from PO Portal, but requirement data was null.");
						return response;
					}

					List<Object[]> result = projectRepository.getAssignedEmployeesCountInProject(id);

					ProjectRequirementsDTO dto = new ProjectRequirementsDTO();
					dto.setTotalRequirements(requirementCountDto.getCount());
					if (!result.isEmpty()) {
						Object[] row = result.get(0);

						Integer assignedApproved = row[1] != null ? ((Number) row[1]).intValue() : 0;
						Integer assignedPending = row[2] != null ? ((Number) row[2]).intValue() : 0;
						Integer assignedTotal = row[3] != null ? ((Number) row[3]).intValue() : 0;

						dto.setAssigned(assignedTotal);
						dto.setAssignedApproved(assignedApproved);
						dto.setAssignedPending(assignedPending);
						dto.setDifference(dto.getTotalRequirements() - assignedTotal);
					} else {
						dto.setAssigned(0);
						dto.setAssignedApproved(0);
						dto.setAssignedPending(0);
						dto.setDifference(dto.getTotalRequirements());
					}

					ProjectRequirementResponse finalDto = new ProjectRequirementResponse();
					finalDto.setResourceRequirementList(project);
					finalDto.setResourceRequirements(dto);

					response.setServiceResponse(finalDto);
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

	@Transactional(rollbackFor = Exception.class)
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

				Set<Long> existingManagerIds = existingMappings.stream().filter(mapping -> mapping.getActive() == 1)
						.map(ProjectManagerMapping::getProjectManagerId).collect(Collectors.toSet());

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

			if (response.getServiceStatus().equalsIgnoreCase(ServiceResponse.STATUS_SUCCESS)) {
				ServiceResponse poPortalResponse = sendProjectInfoToPoPortal(resourceManagementDTO);

				if (poPortalResponse.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS)) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Project manager details sent to Shankh successfully!");
					apiLogInfo.setApiResponse("Project manager details sent to Shankh successfully!");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Failed to sync Project manager details with Shankh! Msg from Po-"
							+ poPortalResponse.getServiceResponse());
					apiLogInfo.setApiResponse("Failed to sync Project manager details with Shankh!");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					throw new RuntimeException("Unable to send project manager details to Po");
				}
			}
			logBuilder.append("\n Project Manager saved successfully!");

			return response;
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("\n Something went wrong!");
			throw new RuntimeException("Error fetching project from PoPortal: " + e.getMessage());
		}
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

		if (teamCreatedProjectsResponse == null) {
			throw new DataNotFoundException("Data not found");
		}

		if (!ServiceResponse.STATUS_SUCCESS.equals(teamCreatedProjectsResponse.getServiceStatus())) {
			return failResponse(serviceResponse, apiLogInfo, "Failed to fetch already created team projects.");
		}
		List<ResourceManagementDTO> teamCreatedProjects = castList(teamCreatedProjectsResponse.getServiceResponse());
		ServiceResponse apiResponse = poPortalAPIService.getAllProjectsFromPoPortal();
		List<ResourceManagementDTO> poPortalProjects;
		if (apiResponse.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS)) {
			poPortalProjects = (List<ResourceManagementDTO>) apiResponse.getServiceResponse();
		} else {
			poPortalProjects = new ArrayList<>();
		}
		System.out.println(poPortalProjects);
		processPoPortalProjects(poPortalProjects, teamCreatedProjects);
		System.out.println(poPortalProjects);
		for (ResourceManagementDTO poData : poPortalProjects) {
			Project data = new Project();
			data.setPoNo(poData.getPoNo());
			data.setApmosysRM(poData.getApmosysRM());
			data.setApmosysRmEmail(poData.getApmosysRmEmail());

			data.setActive("true");

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
					boolean contains = Arrays.stream(poData.getClientLocation()).anyMatch("WFH"::equals);
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
			data.setClientName(poData.getClientName() != null ? poData.getClientName() : null);
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
			data.setEndDate(poData.getEndDate());
			data.setPoNo(poData.getPoNo());
			data.setPoProjectId(poData.getId());
			data.setPoProjectType(poData.getProjectType());
			data.setStartDate(poData.getStartDate());
			data.setProjectManagerId(poData.getProjectManagerId() != null && !poData.getProjectManagerId().isEmpty()
					? poData.getProjectManagerId().get(0)
					: null);
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

			if (apiResponse == null) {
				throw new DataNotFoundException("Data not found");
			}
			if (apiResponse.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS)) {
				poPortalProjects = (List<ResourceManagementDTO>) apiResponse.getServiceResponse();
			} else {
				poPortalProjects = new ArrayList<>();
			}

			List<ResourceManagementDTO> activePOProjects = projectRepository.getAllActivePOProjects();

			int insertCount = 0, updateCount = 0, deactivateCount = 0;

			for (ResourceManagementDTO activeProject : activePOProjects) {
				try {
					Long poProjectId = activeProject.getPoProjectId();
					Integer projectId = activeProject.getProjectId();
					Integer poId = activeProject.getPoId();

					if (poProjectId == null || projectId == null || poId == null)
						continue;

					ResourceManagementDTO portalProject = poPortalProjects.stream()
							.filter(p -> poProjectId.equals(p.getId())).findFirst().orElse(null);

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
									apiLogInfo.setApiResponse("Department not found for name: '" + deptName
											+ "' (Project ID: " + projectId + ")");
								}
							}
						} catch (Exception e) {
							apiLogInfo.setApiResponse(
									"Error while finding department '" + deptName + "' for project ID: " + projectId);
							e.printStackTrace();
						}
					}

					List<PoDepartmentMapping> existingMaps = poDepartmentMappingRepository.findByProjectId(projectId);
					Map<Long, PoDepartmentMapping> deptIdToMap = existingMaps.stream().filter(Objects::nonNull)
							.filter(m -> {
								boolean hasDept = m.getDeptId() != null;
								if (!hasDept) {
									System.out.println("Skipping map with null deptId: " + m);
								}
								return hasDept;
							}).peek(m -> System.out.println("Processing map with deptId: " + m.getDeptId()))
							.collect(Collectors.toMap(PoDepartmentMapping::getDeptId, map -> {
								System.out.println("Putting in map: deptId = " + map.getDeptId() + ", map = " + map);
								return map;
							}));

					Set<Long> newDeptIdSet = new HashSet<>(newDeptIds);

					for (PoDepartmentMapping map : existingMaps) {
						try {
							if (map != null && map.getDeptId() != null 
	                                && !newDeptIdSet.contains(map.getDeptId())
	                                && map.isActive()) {
	                            map.setActive(false);
	                            deactivateCount++;
							}
						} catch (Exception e) {
							apiLogInfo.setApiResponse(
									"Error while deactivating department mapping (Project ID: " + projectId + ")");
							e.printStackTrace();
						}
					}

					for (Long deptId : newDeptIds) {
						try {
							if (deptId != null) {
								if (deptIdToMap.containsKey(deptId)) {
									PoDepartmentMapping existing = deptIdToMap.get(deptId);
									if (!existing.isActive()) {
										existing.setActive(true);
										updateCount++;
									}
								} else {
									PoDepartmentMapping newMap = new PoDepartmentMapping();
									newMap.setPoId(poId.longValue());
									newMap.setDeptId(deptId);
									newMap.setActive(true);
									existingMaps.add(newMap);
									insertCount++;
								}
							} 
						} catch (Exception e) {
							apiLogInfo.setApiResponse("Error inserting/updating mapping (Dept ID: " + deptId
									+ ", Po ID: " + poId + ")");
							e.printStackTrace();
						}
					}

					poDepartmentMappingRepository.saveAll(existingMaps);

				} catch (Exception innerEx) {
					apiLogInfo.setApiResponse("Error processing project with PoProjectId: "
							+ activeProject.getPoId() + ", PoId: " + activeProject.getPoId());
					innerEx.printStackTrace();
				}
			}

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			String finalMessage = "Department mapping synced successfully! Inserted: " + insertCount + ", Updated: "
					+ updateCount + ", Deactivated: " + deactivateCount;
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

	@Transactional(rollbackFor = Exception.class)
	private ServiceResponse setProjectOverheads(ResourceManagementDTO resourceManagementDTO,
			Project projectDbResponse) {
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

			if (resourceManagementDTO.getProjectManagerId() == null
					|| resourceManagementDTO.getProjectManagerId().isEmpty()) {
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
				Set<Long> existingOverheadIds = existingMappings.stream().filter(mapping -> mapping.getActive() == 1)
						.map(ProjectOverheadMapping::getProjectOverheadId).collect(Collectors.toSet());

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
				if (overheadId == null)
					continue;

				ProjectOverheadMapping existingMapping = projectOverheadMappingRepository
						.findByProjectIdAndProjectOverheadId(
								Long.parseLong(projectDbResponse.getProjectId().toString()), overheadId);

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

			List<EmpPrimaryProjectMapping> mappingsToUpdate = new ArrayList<>();

			Project project = projectRepository.findByProjectId(projectId);
			if (project == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Invalid Project");
			}

			LocalDateTime now = LocalDateTime.now();

			List<EmpPrimaryProjectMapping> existingMappingProjectWithSingleEmployee = empPrimaryProjectMappingRepository
					.findByEmpIdInAndIsMapped(empIds);

			for (EmpPrimaryProjectMapping emp : existingMappingProjectWithSingleEmployee) {
				emp.setIsMapped("N");
				emp.setUpdatedBy(updatedBy);
				emp.setUpdatedOn(now);
				mappingsToUpdate.add(emp);
			}

			Map<Long, Employee> employeeMap = employeeRepository.findByEmpIdIn(empIds).stream()
					.collect(Collectors.toMap(Employee::getEmpId, Function.identity()));

			List<Long> shadowEmpIds = employeeTeamMapRepository.findShadowMembersByEmpIdsAndProjectId(empIds,
					projectId);

			String billableType;
			String billable;
			if ("TNM".equalsIgnoreCase(project.getPoProjectType())) {
				billableType = "TNM";
				billable = "Yes";
			} else if ("Fixed cost".equalsIgnoreCase(project.getPoProjectType())
					|| "Fixed Cost".equalsIgnoreCase(project.getPoProjectType())) {
				billableType = "Fixed Cost";
				billable = "No";
			} else if ("Bench".equalsIgnoreCase(project.getInternalProjectType())) {
				billableType = "Bench";
				billable = "No";
			} else if ("InternalRNDProducts".equalsIgnoreCase(project.getInternalProjectType())) {
				billableType = "InternalRNDProducts";
				billable = "No";
			} else if ("Monitoring".equalsIgnoreCase(project.getPoProjectType())) {
				billableType = "Fixed Cost";
				billable = "No";
			} else {
				billableType = null;
				billable = null;
			}

			List<Long> empIdsToUpdateBillable = new ArrayList<>();
			Map<Long, String> empIdToBillable = new HashMap<>();
			Map<Long, String> empIdToBillableType = new HashMap<>();

			for (Long empId : empIds) {
				Long projectIdLong = Long.valueOf(projectId);

				EmpPrimaryProjectMapping newMapping = new EmpPrimaryProjectMapping();
				newMapping.setEmpId(empId);
				newMapping.setPrimaryProjectId(projectIdLong);
				newMapping.setPrimaryProjectName(project.getProjectName());
				newMapping.setIsMapped("Y");
				newMapping.setUpdatedBy(updatedBy);
				newMapping.setUpdatedOn(now);
				mappingsToUpdate.add(newMapping);

				String finalBillableType = shadowEmpIds.contains(empId) ? "Shadow" : billableType;
				String finalBillable = "Shadow".equals(finalBillableType) ? "No" : billable;

				Employee emp = employeeMap.get(empId);
				if (emp == null || !Objects.equals(emp.getBillable(), finalBillable)
						|| !Objects.equals(emp.getBillableType(), finalBillableType)) {
					empIdsToUpdateBillable.add(empId);
					empIdToBillable.put(empId, finalBillable);
					empIdToBillableType.put(empId, finalBillableType);
				}
			}

			if (!mappingsToUpdate.isEmpty()) {
				empPrimaryProjectMappingRepository.saveAll(mappingsToUpdate);
			}

			for (Long empId : empIdsToUpdateBillable) {
				employeeRepository.updateBillableFields(empId, empIdToBillable.get(empId),
						empIdToBillableType.get(empId));
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
				result = projectRepository.getExceptionEmployeeReport();
				
			} else if (departmentRepository.existsByHodId(projectFilterDTO.getCurrentUserEmpId())) {
				List<Long> deptIds = departmentRepository.findDeptIdsByHodId(projectFilterDTO.getCurrentUserEmpId());
				result = projectRepository.getExceptionEmployeeReportInDepartments(deptIds);
			} else if (teamRepository.existsBySpocId(projectFilterDTO.getCurrentUserEmpId())) {
				Employee employeee = employeeRepository.findByEmpId(projectFilterDTO.getCurrentUserEmpId());
				Long deptId = departmentRepository.findDepartmentIdOfSpoc(employeee.getJobRoleId());
				result = projectRepository.getExceptionEmployeeReportInDepartment(deptId);
			} else {
				result = projectRepository.getExceptionEmployeeReport();
			}
			result = groupExceptionEmployeeReport(result);

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
				project.setProjectId(obj.getProjectId() != null ? Integer.parseInt(obj.getProjectId().toString()) : null);
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

	private Map<String, Integer> getExpiredProjectCountsByDateRanges(List<Long> deptIds) {
		Map<String, Integer> expiredCounts = new HashMap<>();

		LocalDate currentDate = LocalDate.now();

		String toDate1Month = currentDate.toString();
		String fromDate1Month = currentDate.minusDays(30).toString();

		String toDate2Month = currentDate.minusDays(31).toString();
		String fromDate2Month = currentDate.minusDays(60).toString();

		String toDate3Month = currentDate.minusDays(61).toString();
		String fromDate3Month = currentDate.minusDays(90).toString();

		String toDate6Month = currentDate.minusDays(91).toString();
		String fromDate6Month = currentDate.minusDays(180).toString();

		String toDate9Month = currentDate.minusDays(181).toString();
		String fromDate9Month = currentDate.minusDays(270).toString();

		String toDate12Month = currentDate.minusDays(271).toString();
		String fromDate12Month = currentDate.minusDays(365).toString();

		String toDateAbove12Month = currentDate.minusDays(366).toString();
		String fromDateAbove12Month = currentDate.minusYears(10).toString();

		Integer expiredWithin1Month = projectRepository.getExpiredProjectCount(deptIds, fromDate1Month, toDate1Month);
		Integer expired1To2Month = projectRepository.getExpiredProjectCount(deptIds, fromDate2Month, toDate2Month);
		Integer expired2To3Month = projectRepository.getExpiredProjectCount(deptIds, fromDate3Month, toDate3Month);
		Integer expired3To6Month = projectRepository.getExpiredProjectCount(deptIds, fromDate6Month, toDate6Month);
		Integer expired6To9Month = projectRepository.getExpiredProjectCount(deptIds, fromDate9Month, toDate9Month);
		Integer expired9To12Month = projectRepository.getExpiredProjectCount(deptIds, fromDate12Month, toDate12Month);
		Integer expiredAbove12Month = projectRepository.getExpiredProjectCount(deptIds, fromDateAbove12Month,
				toDateAbove12Month);

		Integer allMonitoringCount = projectRepository.getAllMonitoringProjectCount(deptIds);
		Integer allInternalCount = projectRepository.getAllInternalProjectsCount(deptIds);

		// unfilled positions
		Integer allUnfilledPositionsCount = projectRepository.getUnfilledPositionsCount(deptIds);

		Integer totalExpiredCount = expiredWithin1Month + expired1To2Month + expired2To3Month + expired3To6Month
				+ expired6To9Month + expired9To12Month + expiredAbove12Month;

		expiredCounts.put("expiredProjectsWithin1Month", expiredWithin1Month);
		expiredCounts.put("expiredProjects1To2Months", expired1To2Month);
		expiredCounts.put("expiredProjects2To3Months", expired2To3Month);
		expiredCounts.put("expiredProjects3To6Months", expired3To6Month);
		expiredCounts.put("expiredProjects6To9Months", expired6To9Month);
		expiredCounts.put("expiredProjects9To12Months", expired9To12Month);
		expiredCounts.put("expiredProjectsAbove12Months", expiredAbove12Month);
		expiredCounts.put("allExpiredTNMProjectsCount", totalExpiredCount);

		expiredCounts.put("allMonitoringProjectCount", allMonitoringCount);
		expiredCounts.put("allInternalProjectCount", allInternalCount);

		expiredCounts.put("allUnfilledPositionsCount", allUnfilledPositionsCount);

		return expiredCounts;
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
			if ((Boolean.TRUE.equals(projectFilterDTO.getIsHod()) && Boolean.TRUE.equals(projectFilterDTO.getIsAdmin()))
					|| Boolean.TRUE.equals(projectFilterDTO.getIsAdmin())
							&& Boolean.TRUE.equals(projectFilterDTO.getIsOther())
					|| Boolean.TRUE.equals(projectFilterDTO.getIsHod())
							&& Boolean.TRUE.equals(projectFilterDTO.getIsOther())) {
				return failResponse(response, apiLogInfo, "Invalid input: More than one flag is true");
			}

			Map<String, Integer> map = new HashMap<>();
			List<Long> deptIdList = new ArrayList<>();
			Set<Integer> projectIdSet = new HashSet<>();
			List<Integer> projectIdListTemp;
			Integer days = projectFilterDTO.getDays();
			Integer pendingForApprovalCount, approvedCount, notStartedCount, rejectedCount, totalCount,
					completedInIshine, activeProject, activeProjectsAfterMarch31Count, activeProjectsUpToMarch31Count,
					internalActiveProjectsCount, activeTNMCount, allCount;

			CombinedPOInternalProjectResponse responseData = new CombinedPOInternalProjectResponse();

			Map<String, Integer> expiredProjectCounts = new HashMap<>();

			if ("All".equalsIgnoreCase(projectFilterDTO.getApprovalStatus()))
				projectFilterDTO.setApprovalStatus("All");
			if (Boolean.TRUE.equals(projectFilterDTO.getIsAdmin())) {

				if (!projectFilterDTO.getDepartmentsids().isEmpty() && projectFilterDTO.getDepartmentsids() != null) {
					List<Long> selectedDeptList = projectFilterDTO.getDepartmentsids();
					Set<Integer> matchingProjectIds = teamRepository.findAll().stream().filter(team -> {
						if (team.getDeptIds() == null)
							return false;
						List<String> teamDeptIds = Arrays.asList(team.getDeptIds().split(","));
						return selectedDeptList.stream().map(String::valueOf).anyMatch(teamDeptIds::contains);
					}).map(Team::getProjectId).filter(Objects::nonNull).collect(Collectors.toSet());
					projectIdSet = matchingProjectIds;
					pendingForApprovalCount = projectRepository.getAllPendingForApprovalProjectCount(selectedDeptList);
					approvedCount = projectRepository.getAllApprovedProjectCount(selectedDeptList);
					notStartedCount = projectRepository.getAllNotStartedProjectCountInDept(selectedDeptList);
					rejectedCount = projectRepository.getAllRejectedProjectCount(selectedDeptList);
					completedInIshine = projectRepository.getAllCompletedProjectCountInIshine(selectedDeptList);
					activeProject = projectRepository.getTotalAllActiveProjectCount(selectedDeptList);
//					activeProjectsAfterMarch31Count = projectRepository.getActiveProjectGreaterThan31MarchCount(selectedDeptList);
//					activeProjectsUpToMarch31Count = projectRepository.getActiveProjectUptoMarch31Count(selectedDeptList);
					internalActiveProjectsCount = projectRepository.getAllInternalActiveProjectsCount(selectedDeptList);
					activeTNMCount = projectRepository.getAllActiveTNMProjectsCount(selectedDeptList);
					allCount = projectRepository.getAllProjectCount(selectedDeptList);

					expiredProjectCounts = getExpiredProjectCountsByDateRanges(selectedDeptList);

					totalCount = projectRepository.getAllTotalProjectCount(selectedDeptList);
					map.put("pendingForApprovalCount", pendingForApprovalCount);
					map.put("approvedCount", approvedCount);
					map.put("completedCount",
							projectRepository.getAllCompleteProjectInShankhCountstList(projectIdSet, true));
					map.put("completedWithEmployeeCount",
							projectRepository.completedInSankhButTeamMapped(projectIdSet, true));
					map.put("notStartedCount", notStartedCount);
					map.put("rejectedCount", rejectedCount);
					map.put("completedInIshineCount", completedInIshine);
					map.put("allTotalActiveProjectCount", activeProject);
//					map.put("activeProjectGreaterThan31MarchCount", activeProjectsAfterMarch31Count);	
//					map.put("allActiveProjectsUpToMarch31Count", activeProjectsUpToMarch31Count);
					map.put("allInternalActiveProjectsCounts", internalActiveProjectsCount);
					map.put("allActiveTNMProjectsCount", activeTNMCount);

					map.putAll(expiredProjectCounts);

					map.put("allCount", allCount);
					map.put("totalCount", totalCount);
				} else {
					List<Long> deptIdsAccToRole = departmentRepository.findAllDepartments();

					pendingForApprovalCount = projectRepository.getAllPendingForApprovalProjectCount(deptIdsAccToRole);
					approvedCount = projectRepository.getAllApprovedProjectCount(deptIdsAccToRole);
					notStartedCount = projectRepository.getAllNotStartedProjectCountInDept(deptIdsAccToRole);
					completedInIshine = projectRepository.getAllCompletedProjectCountInIshine(deptIdsAccToRole);
					activeProject = projectRepository.getTotalAllActiveProjectCount(deptIdsAccToRole);
//					activeProjectsAfterMarch31Count = projectRepository.getActiveProjectGreaterThan31MarchCount(deptIdsAccToRole);
//					activeProjectsUpToMarch31Count = projectRepository.getActiveProjectUptoMarch31Count(deptIdsAccToRole);
					internalActiveProjectsCount = projectRepository.getAllInternalActiveProjectsCount(deptIdsAccToRole);
					activeTNMCount = projectRepository.getAllActiveTNMProjectsCount(deptIdsAccToRole);

					expiredProjectCounts = getExpiredProjectCountsByDateRanges(deptIdsAccToRole);

					rejectedCount = projectRepository.getAllRejectedProjectCount(deptIdsAccToRole);

					totalCount = projectRepository.getAllTotalProjectCount(deptIdsAccToRole);
					allCount = projectRepository.getAllProjectCount(deptIdsAccToRole);
					map.put("pendingForApprovalCount", pendingForApprovalCount);
					map.put("approvedCount", approvedCount);
					map.put("completedCount", projectRepository.getAllCompleteProjectInShankhCountstList(null, false));
					map.put("completedWithEmployeeCount", projectRepository.completedInSankhButTeamMapped(null, false));
					map.put("notStartedCount", notStartedCount);
					map.put("rejectedCount", rejectedCount);
					map.put("completedInIshineCount", completedInIshine);
					map.put("allTotalActiveProjectCount", activeProject);
//				map.put("activeProjectGreaterThan31MarchCount", activeProjectsAfterMarch31Count);
//				map.put("allActiveProjectsUpToMarch31Count", activeProjectsUpToMarch31Count);
					map.put("allInternalActiveProjectsCounts", internalActiveProjectsCount);
					map.put("allActiveTNMProjectsCount", activeTNMCount);

					map.putAll(expiredProjectCounts);

					map.put("totalCount", totalCount);
					map.put("allCount", allCount);
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

//				Set<Integer> matchingProjectIds = teamRepository.findAll().stream()
//					    .filter(team -> {
//					        if (team.getDeptIds() == null) return false;
//					        List<String> teamDeptIds = Arrays.asList(team.getDeptIds().split(","));
//					        return deptIdList.stream()
//					                .map(String::valueOf)
//					                .anyMatch(teamDeptIds::contains);
//					    })
//					    .map(Team::getProjectId)
//					    .filter(Objects::nonNull)
//					    .collect(Collectors.toSet());

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
//						projectIdSet.addAll(matchingProjectIds);
				}

				// Add projects from departments they manage
				Set<Integer> deptProjectIds = teamRepository.findAll().stream().filter(team -> {
					if (team.getDeptIds() == null)
						return false;
					List<String> teamDeptIds = Arrays.asList(team.getDeptIds().split(","));
					return deptIdList.stream().map(String::valueOf).anyMatch(teamDeptIds::contains);
				}).map(Team::getProjectId).filter(Objects::nonNull).collect(Collectors.toSet());

				projectIdSet.addAll(deptProjectIds);

				if (!projectFilterDTO.getDepartmentsids().isEmpty() && projectFilterDTO.getDepartmentsids() != null) {
					List<Long> selectedDeptList = projectFilterDTO.getDepartmentsids();
					Set<Integer> matchingProjIds = teamRepository.findAll().stream().filter(team -> {
						if (team.getDeptIds() == null)
							return false;
						List<String> teamDeptIds = Arrays.asList(team.getDeptIds().split(","));
						return selectedDeptList.stream().map(String::valueOf).anyMatch(teamDeptIds::contains);
					}).map(Team::getProjectId).filter(Objects::nonNull).collect(Collectors.toSet());

					projectIdSet.retainAll(matchingProjIds);

					pendingForApprovalCount = projectRepository.getAllPendingForApprovalProjectCount(selectedDeptList);
					approvedCount = projectRepository.getAllApprovedProjectCount(selectedDeptList);
					notStartedCount = projectRepository.getAllNotStartedProjectCountInDept(selectedDeptList);
					rejectedCount = projectRepository.getAllRejectedProjectCount(selectedDeptList);
					completedInIshine = projectRepository.getAllCompletedProjectCountInIshine(selectedDeptList);
					activeProject = projectRepository.getTotalAllActiveProjectCount(selectedDeptList);
//					activeProjectsAfterMarch31Count = projectRepository.getActiveProjectGreaterThan31MarchCount(selectedDeptList);
//					activeProjectsUpToMarch31Count = projectRepository.getActiveProjectUptoMarch31Count(selectedDeptList);
					internalActiveProjectsCount = projectRepository.getAllInternalActiveProjectsCount(selectedDeptList);
					activeTNMCount = projectRepository.getAllActiveTNMProjectsCount(selectedDeptList);

					expiredProjectCounts = getExpiredProjectCountsByDateRanges(selectedDeptList);

					totalCount = projectRepository.getAllTotalProjectCount(selectedDeptList);
					allCount = projectRepository.getAllProjectCount(selectedDeptList);

					map.put("pendingForApprovalCount", pendingForApprovalCount);
					map.put("approvedCount", approvedCount);
					map.put("completedCount",
							projectRepository.getAllCompleteProjectInShankhCountstList(projectIdSet, true));
					map.put("completedWithEmployeeCount",
							projectRepository.completedInSankhButTeamMapped(projectIdSet, true));
					map.put("notStartedCount", notStartedCount);
					map.put("rejectedCount", rejectedCount);
					map.put("completedInIshineCount", completedInIshine);
					map.put("allTotalActiveProjectCount", activeProject);
//					map.put("activeProjectGreaterThan31MarchCount", activeProjectsAfterMarch31Count);	
//					map.put("allActiveProjectsUpToMarch31Count", activeProjectsUpToMarch31Count);
					map.put("allInternalActiveProjectsCounts", internalActiveProjectsCount);
					map.put("allActiveTNMProjectsCount", activeTNMCount);
					map.put("allCount", allCount);

					map.putAll(expiredProjectCounts);

					map.put("totalCount", totalCount);
				} else {

//					List<Long> selectedDeptList= projectFilterDTO.getDepartmentsids();

					pendingForApprovalCount = projectRepository.getAllPendingForApprovalProjectCount(deptIdList);
					approvedCount = projectRepository.getAllApprovedProjectCount(deptIdList);
					notStartedCount = projectRepository.getAllNotStartedProjectCountInDept(deptIdList);
					completedInIshine = projectRepository.getAllCompletedProjectCountInIshine(deptIdList);
					activeProject = projectRepository.getTotalAllActiveProjectCount(deptIdList);
//					activeProjectsAfterMarch31Count = projectRepository.getActiveProjectGreaterThan31MarchCount(deptIdList);
//					activeProjectsUpToMarch31Count = projectRepository.getActiveProjectUptoMarch31Count(deptIdList);
					internalActiveProjectsCount = projectRepository.getAllInternalActiveProjectsCount(deptIdList);
					rejectedCount = projectRepository.getAllRejectedProjectCount(deptIdList);
					activeTNMCount = projectRepository.getAllActiveTNMProjectsCount(deptIdList);

					expiredProjectCounts = getExpiredProjectCountsByDateRanges(deptIdList);

					totalCount = projectRepository.getAllTotalProjectCount(deptIdList);
					allCount = projectRepository.getAllProjectCount(deptIdList);

					map.put("pendingForApprovalCount", pendingForApprovalCount);
					map.put("approvedCount", approvedCount);
					map.put("completedCount",
							projectRepository.getAllCompleteProjectInShankhCountstList(projectIdSet, true));
					map.put("completedWithEmployeeCount",
							projectRepository.completedInSankhButTeamMapped(projectIdSet, true));
					map.put("notStartedCount", notStartedCount);
					map.put("rejectedCount", rejectedCount);
					map.put("completedInIshineCount", completedInIshine);
					map.put("allTotalActiveProjectCount", activeProject);
//					map.put("activeProjectGreaterThan31MarchCount", activeProjectsAfterMarch31Count);	
//					map.put("allActiveProjectsUpToMarch31Count", activeProjectsUpToMarch31Count);
					map.put("allInternalActiveProjectsCounts", internalActiveProjectsCount);
					map.put("allActiveTNMProjectsCount", activeTNMCount);

					map.putAll(expiredProjectCounts);

					map.put("totalCount", totalCount);
					map.put("allCount", allCount);
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

				if (projectIdSet != null && !projectIdSet.isEmpty()) {
					if (!projectFilterDTO.getDepartmentsids().isEmpty()
							&& projectFilterDTO.getDepartmentsids() != null) {
						List<Long> selectedDeptList = projectFilterDTO.getDepartmentsids();
						Set<Integer> matchingProjectIds = teamRepository.findAll().stream().filter(team -> {
							if (team.getDeptIds() == null)
								return false;
							List<String> teamDeptIds = Arrays.asList(team.getDeptIds().split(","));
							return selectedDeptList.stream().map(String::valueOf).anyMatch(teamDeptIds::contains);
						}).map(Team::getProjectId).filter(Objects::nonNull).collect(Collectors.toSet());

						projectIdSet.retainAll(matchingProjectIds);

						pendingForApprovalCount = projectRepository
								.getAllPendingForApprovalProjectCount(selectedDeptList);
						approvedCount = projectRepository.getAllApprovedProjectCount(selectedDeptList);
						notStartedCount = projectRepository.getAllNotStartedProjectCountInDept(selectedDeptList);
						completedInIshine = projectRepository.getAllCompletedProjectCountInIshine(selectedDeptList);
						activeProject = projectRepository.getTotalAllActiveProjectCount(selectedDeptList);
//					activeProjectsAfterMarch31Count = projectRepository.getActiveProjectGreaterThan31MarchCount(selectedDeptList);
//					activeProjectsUpToMarch31Count = projectRepository.getActiveProjectUptoMarch31Count(selectedDeptList);
						internalActiveProjectsCount = projectRepository
								.getAllInternalActiveProjectsCount(selectedDeptList);
						rejectedCount = projectRepository.getAllRejectedProjectCount(selectedDeptList);
						activeTNMCount = projectRepository.getAllActiveTNMProjectsCount(selectedDeptList);

						expiredProjectCounts = getExpiredProjectCountsByDateRanges(selectedDeptList);

						totalCount = projectRepository.getAllTotalProjectCount(selectedDeptList);
						allCount = projectRepository.getAllProjectCount(selectedDeptList);

						map.put("pendingForApprovalCount", pendingForApprovalCount);
						map.put("approvedCount", approvedCount);
						map.put("completedCount",
								projectRepository.getAllCompleteProjectInShankhCountstList(projectIdSet, true));
						map.put("completedWithEmployeeCount",
								projectRepository.completedInSankhButTeamMapped(projectIdSet, true));
						map.put("notStartedCount", notStartedCount);
						map.put("rejectedCount", rejectedCount);
						map.put("completedInIshineCount", completedInIshine);
						map.put("allTotalActiveProjectCount", activeProject);
//					map.put("activeProjectGreaterThan31MarchCount", activeProjectsAfterMarch31Count);	
//					map.put("allActiveProjectsUpToMarch31Count", activeProjectsUpToMarch31Count);
						map.put("allInternalActiveProjectsCounts", internalActiveProjectsCount);

						map.putAll(expiredProjectCounts);

						map.put("allActiveTNMProjectsCount", activeTNMCount);

						map.put("totalCount", totalCount);
						map.put("allCount", allCount);
					} else {
						Employee employeee = employeeRepository.findByEmpId(projectFilterDTO.getCurrentUserEmpId());
						List<Long> deptIdOfOther = departmentRepository
								.findDepartmentIdOfCurrentUser(employeee.getJobRoleId());
//						List<Long> selectedDeptList= projectFilterDTO.getDepartmentsids();

						pendingForApprovalCount = projectRepository.getAllPendingForApprovalProjectCount(deptIdOfOther);
						approvedCount = projectRepository.getAllApprovedProjectCount(deptIdOfOther);
						notStartedCount = projectRepository.getAllNotStartedProjectCountInDept(deptIdOfOther);
						completedInIshine = projectRepository.getAllCompletedProjectCountInIshine(deptIdOfOther);
						activeProject = projectRepository.getTotalAllActiveProjectCount(deptIdOfOther);
//						activeProjectsAfterMarch31Count = projectRepository.getActiveProjectGreaterThan31MarchCount(deptIdOfOther);
//						activeProjectsUpToMarch31Count = projectRepository.getActiveProjectUptoMarch31Count(deptIdOfOther);
						internalActiveProjectsCount = projectRepository
								.getAllInternalActiveProjectsCount(deptIdOfOther);
						rejectedCount = projectRepository.getAllRejectedProjectCount(deptIdOfOther);
						activeTNMCount = projectRepository.getAllActiveTNMProjectsCount(deptIdOfOther);

						expiredProjectCounts = getExpiredProjectCountsByDateRanges(deptIdOfOther);

						totalCount = projectRepository.getAllTotalProjectCount(deptIdOfOther);
						allCount = projectRepository.getAllProjectCount(deptIdOfOther);

						map.put("pendingForApprovalCount", pendingForApprovalCount);
						map.put("approvedCount", approvedCount);
						map.put("completedCount",
								projectRepository.getAllCompleteProjectInShankhCountstList(projectIdSet, true));
						map.put("completedWithEmployeeCount",
								projectRepository.completedInSankhButTeamMapped(projectIdSet, true));
						map.put("notStartedCount", notStartedCount);
						map.put("rejectedCount", rejectedCount);
						map.put("completedInIshineCount", completedInIshine);
						map.put("allTotalActiveProjectCount", activeProject);
//						map.put("activeProjectGreaterThan31MarchCount", activeProjectsAfterMarch31Count);
//						map.put("allActiveProjectsUpToMarch31Count", activeProjectsUpToMarch31Count);
						map.put("allInternalActiveProjectsCounts", internalActiveProjectsCount);
						map.put("allActiveTNMProjectsCount", activeTNMCount);

						map.putAll(expiredProjectCounts);

						map.put("totalCount", totalCount);
						map.put("allCount", allCount);
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

	private Map<String, List<ProjectFetchDTO>> getExpiredProjectListsByDateRanges(List<Long> deptIds) {
		Map<String, List<ProjectFetchDTO>> expiredProjects = new HashMap<>();

		LocalDate currentDate = LocalDate.now();

		String toDate1Month = currentDate.toString();
		String fromDate1Month = currentDate.minusDays(30).toString();

		String toDate2Month = currentDate.minusDays(31).toString();
		String fromDate2Month = currentDate.minusDays(60).toString();

		String toDate3Month = currentDate.minusDays(61).toString();
		String fromDate3Month = currentDate.minusDays(90).toString();

		String toDate6Month = currentDate.minusDays(91).toString();
		String fromDate6Month = currentDate.minusDays(180).toString();

		String toDate9Month = currentDate.minusDays(181).toString();
		String fromDate9Month = currentDate.minusDays(270).toString();

		String toDate12Month = currentDate.minusDays(271).toString();
		String fromDate12Month = currentDate.minusDays(365).toString();

		String toDateAbove12Month = currentDate.minusDays(366).toString();
		String fromDateAbove12Month = currentDate.minusYears(10).toString();

		List<Object[]> expiredWithin1MonthResults = projectRepository.getExpiredProjectList(deptIds, fromDate1Month,
				toDate1Month);
		List<Object[]> expired1To2MonthResults = projectRepository.getExpiredProjectList(deptIds, fromDate2Month,
				toDate2Month);
		List<Object[]> expired2To3MonthResults = projectRepository.getExpiredProjectList(deptIds, fromDate3Month,
				toDate3Month);
		List<Object[]> expired3To6MonthResults = projectRepository.getExpiredProjectList(deptIds, fromDate6Month,
				toDate6Month);
		List<Object[]> expired6To9MonthResults = projectRepository.getExpiredProjectList(deptIds, fromDate9Month,
				toDate9Month);
		List<Object[]> expired9To12MonthResults = projectRepository.getExpiredProjectList(deptIds, fromDate12Month,
				toDate12Month);
		List<Object[]> expiredAbove12MonthResults = projectRepository.getExpiredProjectList(deptIds,
				fromDateAbove12Month, toDateAbove12Month);

		List<ProjectFetchDTO> expiredWithin1Month = expiredWithin1MonthResults.stream().map(ProjectFetchDTO::new)
				.collect(Collectors.toList());

		List<ProjectFetchDTO> expired1To2Month = expired1To2MonthResults.stream().map(ProjectFetchDTO::new)
				.collect(Collectors.toList());

		List<ProjectFetchDTO> expired2To3Month = expired2To3MonthResults.stream().map(ProjectFetchDTO::new)
				.collect(Collectors.toList());

		List<ProjectFetchDTO> expired3To6Month = expired3To6MonthResults.stream().map(ProjectFetchDTO::new)
				.collect(Collectors.toList());

		List<ProjectFetchDTO> expired6To9Month = expired6To9MonthResults.stream().map(ProjectFetchDTO::new)
				.collect(Collectors.toList());

		List<ProjectFetchDTO> expired9To12Month = expired9To12MonthResults.stream().map(ProjectFetchDTO::new)
				.collect(Collectors.toList());

		List<ProjectFetchDTO> expiredAbove12Month = expiredAbove12MonthResults.stream().map(ProjectFetchDTO::new)
				.collect(Collectors.toList());

		List<ProjectFetchDTO> allExpiredProjects = new ArrayList<>();
		allExpiredProjects.addAll(expiredWithin1Month);
		allExpiredProjects.addAll(expired1To2Month);
		allExpiredProjects.addAll(expired2To3Month);
		allExpiredProjects.addAll(expired3To6Month);
		allExpiredProjects.addAll(expired6To9Month);
		allExpiredProjects.addAll(expired9To12Month);
		allExpiredProjects.addAll(expiredAbove12Month);

		expiredProjects.put("expiredProjectsWithin1Month", expiredWithin1Month);
		expiredProjects.put("expiredProjects1To2Months", expired1To2Month);
		expiredProjects.put("expiredProjects2To3Months", expired2To3Month);
		expiredProjects.put("expiredProjects3To6Months", expired3To6Month);
		expiredProjects.put("expiredProjects6To9Months", expired6To9Month);
		expiredProjects.put("expiredProjects9To12Months", expired9To12Month);
		expiredProjects.put("expiredProjectsAbove12Months", expiredAbove12Month);
		expiredProjects.put("allExpiredTNMProjects", allExpiredProjects);

		return expiredProjects;
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
			if ((Boolean.TRUE.equals(projectFilterDTO.getIsHod()) && Boolean.TRUE.equals(projectFilterDTO.getIsAdmin()))
					|| Boolean.TRUE.equals(projectFilterDTO.getIsAdmin())
							&& Boolean.TRUE.equals(projectFilterDTO.getIsOther())
					|| Boolean.TRUE.equals(projectFilterDTO.getIsHod())
							&& Boolean.TRUE.equals(projectFilterDTO.getIsOther())) {
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
			List<ProjectFetchDTO> expiredTNMList = new ArrayList<>();
			List<ProjectFetchDTO> activeTNMList = new ArrayList<>();

			CombinedPOInternalProjectResponse responseData = new CombinedPOInternalProjectResponse();

			if ("expiredTNM".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
				Map<String, List<ProjectFetchDTO>> expiredProjectsMap = new HashMap<>();

				if (Boolean.TRUE.equals(projectFilterDTO.getIsAdmin())) {
					if (projectFilterDTO.getDepartmentsids() != null
							&& !projectFilterDTO.getDepartmentsids().isEmpty()) {
						List<Long> selectedDeptList = projectFilterDTO.getDepartmentsids();
						expiredProjectsMap = getExpiredProjectListsByDateRanges(selectedDeptList);
					} else {
						List<Long> deptIdsAccToRole = departmentRepository.findAllDepartments();
						expiredProjectsMap = getExpiredProjectListsByDateRanges(deptIdsAccToRole);
					}
				} else if (Boolean.TRUE.equals(projectFilterDTO.getIsHod())) {
					List<Department> deptData = departmentRepository
							.findByHodId(projectFilterDTO.getCurrentUserEmpId());
					if (deptData != null) {
						for (Department data : deptData) {
							if (data != null && data.getDeptId() != null) {
								deptIdList.add(data.getDeptId());
							}
						}
					}

					if (projectFilterDTO.getDepartmentsids() != null
							&& !projectFilterDTO.getDepartmentsids().isEmpty()) {
						List<Long> selectedDeptList = projectFilterDTO.getDepartmentsids();
						expiredProjectsMap = getExpiredProjectListsByDateRanges(selectedDeptList);
					} else {
						expiredProjectsMap = getExpiredProjectListsByDateRanges(deptIdList);
					}
				} else if (Boolean.TRUE.equals(projectFilterDTO.getIsOther())) {
					if (projectFilterDTO.getDepartmentsids() != null
							&& !projectFilterDTO.getDepartmentsids().isEmpty()) {
						List<Long> selectedDeptList = projectFilterDTO.getDepartmentsids();
						expiredProjectsMap = getExpiredProjectListsByDateRanges(selectedDeptList);
					} else {
						Employee employeee = employeeRepository.findByEmpId(projectFilterDTO.getCurrentUserEmpId());
						List<Long> deptIdOfOther = departmentRepository
								.findDepartmentIdOfCurrentUser(employeee.getJobRoleId());
						expiredProjectsMap = getExpiredProjectListsByDateRanges(deptIdOfOther);
					}
				}

				responseData.setExpiredTNMProjectsWithin1Month(expiredProjectsMap.get("expiredProjectsWithin1Month"));
				responseData.setExpiredTNMProjects1To2Months(expiredProjectsMap.get("expiredProjects1To2Months"));
				responseData.setExpiredTNMProjects2To3Months(expiredProjectsMap.get("expiredProjects2To3Months"));
				responseData.setExpiredTNMProjects3To6Months(expiredProjectsMap.get("expiredProjects3To6Months"));
				responseData.setExpiredTNMProjects6To9Months(expiredProjectsMap.get("expiredProjects6To9Months"));
				responseData.setExpiredTNMProjects9To12Months(expiredProjectsMap.get("expiredProjects9To12Months"));
				responseData.setExpiredTNMProjectsAbove12Months(expiredProjectsMap.get("expiredProjectsAbove12Months"));
				responseData.setAllExpiredTNMProjects(expiredProjectsMap.get("allExpiredTNMProjects"));

				if (!expiredProjectsMap.get("allExpiredTNMProjects").isEmpty()) {
					response.setServiceResponse(responseData);
					response.setServiceStatus(response.STATUS_SUCCESS);
				} else {
					response.setServiceResponse("No projects found...!!");
					response.setServiceStatus(response.STATUS_FAIL);
				}
				return response;
			}

			if ("activeTNM".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {

				if (Boolean.TRUE.equals(projectFilterDTO.getIsAdmin())) {

					if (projectFilterDTO.getDepartmentsids() != null
							&& !projectFilterDTO.getDepartmentsids().isEmpty()) {
						List<Long> selectedDeptList = projectFilterDTO.getDepartmentsids();
						List<Object[]> activeTNMResults = projectRepository
								.getAllActiveTNMProjectsList(selectedDeptList);

						activeTNMList = activeTNMResults.stream().map(ProjectFetchDTO::new)
								.collect(Collectors.toList());
					} else {
						List<Long> deptIdsAccToRole = departmentRepository.findAllDepartments();
						List<Object[]> activeTNMResults = projectRepository
								.getAllActiveTNMProjectsList(deptIdsAccToRole);

						activeTNMList = activeTNMResults.stream().map(ProjectFetchDTO::new)
								.collect(Collectors.toList());
					}
				} else if (Boolean.TRUE.equals(projectFilterDTO.getIsHod())) {

					List<Department> deptData = departmentRepository
							.findByHodId(projectFilterDTO.getCurrentUserEmpId());
					if (deptData != null) {
						for (Department data : deptData) {
							if (data != null && data.getDeptId() != null) {
								deptIdList.add(data.getDeptId());
							}
						}
					}

					if (projectFilterDTO.getDepartmentsids() != null
							&& !projectFilterDTO.getDepartmentsids().isEmpty()) {
						List<Long> selectedDeptList = projectFilterDTO.getDepartmentsids();
						List<Object[]> activeTNMResults = projectRepository
								.getAllActiveTNMProjectsList(selectedDeptList);

						activeTNMList = activeTNMResults.stream().map(ProjectFetchDTO::new)
								.collect(Collectors.toList());
					} else {
						List<Object[]> activeTNMResults = projectRepository.getAllActiveTNMProjectsList(deptIdList);
						activeTNMList = activeTNMResults.stream().map(ProjectFetchDTO::new)
								.collect(Collectors.toList());
					}
				} else if (Boolean.TRUE.equals(projectFilterDTO.getIsOther())) {
					if (projectFilterDTO.getDepartmentsids() != null
							&& !projectFilterDTO.getDepartmentsids().isEmpty()) {
						List<Long> selectedDeptList = projectFilterDTO.getDepartmentsids();

						List<Object[]> activeTNMResults = projectRepository
								.getAllActiveTNMProjectsList(selectedDeptList);
						activeTNMList = activeTNMResults.stream().map(ProjectFetchDTO::new)
								.collect(Collectors.toList());
					} else {
						Employee employeee = employeeRepository.findByEmpId(projectFilterDTO.getCurrentUserEmpId());
						List<Long> deptIdOfOther = departmentRepository
								.findDepartmentIdOfCurrentUser(employeee.getJobRoleId());

						List<Object[]> activeTNMResults = projectRepository.getAllActiveTNMProjectsList(deptIdOfOther);
						activeTNMList = activeTNMResults.stream().map(ProjectFetchDTO::new)
								.collect(Collectors.toList());
					}
				}
				populateProjectDetails(activeTNMList);

				responseData.setActiveTNMProjects(activeTNMList);

				if (!activeTNMList.isEmpty()) {
					response.setServiceResponse(responseData);
					response.setServiceStatus(response.STATUS_SUCCESS);

				} else {
					response.setServiceResponse("No projects found...!!");
					response.setServiceStatus(response.STATUS_FAIL);

				}
				return response;
			}

			List<ProjectFetchDTO> internal = new ArrayList<ProjectFetchDTO>();

			if ("internal".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {

				if (Boolean.TRUE.equals(projectFilterDTO.getIsAdmin())) {

					if (projectFilterDTO.getDepartmentsids() != null
							&& !projectFilterDTO.getDepartmentsids().isEmpty()) {
						List<Long> selectedDeptList = projectFilterDTO.getDepartmentsids();
						List<Object[]> internalResults = projectRepository.getAllInternalList(selectedDeptList);

						internal = internalResults.stream().map(ProjectFetchDTO::new).collect(Collectors.toList());
					} else {
						List<Long> deptIdsAccToRole = departmentRepository.findAllDepartments();
						List<Object[]> internalResults = projectRepository.getAllInternalList(deptIdsAccToRole);

						internal = internalResults.stream().map(ProjectFetchDTO::new).collect(Collectors.toList());
					}
				} else if (Boolean.TRUE.equals(projectFilterDTO.getIsHod())) {

					List<Department> deptData = departmentRepository
							.findByHodId(projectFilterDTO.getCurrentUserEmpId());
					if (deptData != null) {
						for (Department data : deptData) {
							if (data != null && data.getDeptId() != null) {
								deptIdList.add(data.getDeptId());
							}
						}
					}

					if (projectFilterDTO.getDepartmentsids() != null
							&& !projectFilterDTO.getDepartmentsids().isEmpty()) {
						List<Long> selectedDeptList = projectFilterDTO.getDepartmentsids();
						List<Object[]> internalResults = projectRepository.getAllInternalList(selectedDeptList);

						internal = internalResults.stream().map(ProjectFetchDTO::new).collect(Collectors.toList());
					} else {
						List<Object[]> internalResults = projectRepository.getAllInternalList(deptIdList);
						internal = internalResults.stream().map(ProjectFetchDTO::new).collect(Collectors.toList());
					}
				} else if (Boolean.TRUE.equals(projectFilterDTO.getIsOther())) {
					if (projectFilterDTO.getDepartmentsids() != null
							&& !projectFilterDTO.getDepartmentsids().isEmpty()) {
						List<Long> selectedDeptList = projectFilterDTO.getDepartmentsids();

						List<Object[]> internalResults = projectRepository.getAllInternalList(selectedDeptList);
						internal = internalResults.stream().map(ProjectFetchDTO::new).collect(Collectors.toList());
					} else {
						Employee employeee = employeeRepository.findByEmpId(projectFilterDTO.getCurrentUserEmpId());
						List<Long> deptIdOfOther = departmentRepository
								.findDepartmentIdOfCurrentUser(employeee.getJobRoleId());

						List<Object[]> internalResults = projectRepository.getAllInternalList(deptIdOfOther);
						internal = internalResults.stream().map(ProjectFetchDTO::new).collect(Collectors.toList());
					}
				}

				populateProjectDetails(internal);

				responseData.setInternalProjects(internal);

				if (!internal.isEmpty()) {
					response.setServiceResponse(responseData);
					response.setServiceStatus(response.STATUS_SUCCESS);

				} else {
					response.setServiceResponse("No projects found...!!");
					response.setServiceStatus(response.STATUS_FAIL);

				}
				return response;
			}

			// unfilled positions
			List<ProjectFetchDTO> unfilledPositions = new ArrayList<ProjectFetchDTO>();
			if ("unfilledPositions".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {

				if (Boolean.TRUE.equals(projectFilterDTO.getIsAdmin())) {

					if (projectFilterDTO.getDepartmentsids() != null
							&& !projectFilterDTO.getDepartmentsids().isEmpty()) {
						List<Long> selectedDeptList = projectFilterDTO.getDepartmentsids();
						List<Object[]> results = projectRepository.getAllUnfilledPositionList(selectedDeptList);

						unfilledPositions = results.stream().map(ProjectFetchDTO::new).collect(Collectors.toList());
					} else {
						List<Long> deptIdsAccToRole = departmentRepository.findAllDepartments();
						List<Object[]> results = projectRepository.getAllUnfilledPositionList(deptIdsAccToRole);

						unfilledPositions = results.stream().map(ProjectFetchDTO::new).collect(Collectors.toList());
					}
				} else if (Boolean.TRUE.equals(projectFilterDTO.getIsHod())) {

					List<Department> deptData = departmentRepository
							.findByHodId(projectFilterDTO.getCurrentUserEmpId());
					if (deptData != null) {
						for (Department data : deptData) {
							if (data != null && data.getDeptId() != null) {
								deptIdList.add(data.getDeptId());
							}
						}
					}

					if (projectFilterDTO.getDepartmentsids() != null
							&& !projectFilterDTO.getDepartmentsids().isEmpty()) {
						List<Long> selectedDeptList = projectFilterDTO.getDepartmentsids();
						List<Object[]> results = projectRepository.getAllUnfilledPositionList(selectedDeptList);

						unfilledPositions = results.stream().map(ProjectFetchDTO::new).collect(Collectors.toList());
					} else {
						List<Object[]> results = projectRepository.getAllUnfilledPositionList(deptIdList);
						unfilledPositions = results.stream().map(ProjectFetchDTO::new).collect(Collectors.toList());
					}
				} else if (Boolean.TRUE.equals(projectFilterDTO.getIsOther())) {
					if (projectFilterDTO.getDepartmentsids() != null
							&& !projectFilterDTO.getDepartmentsids().isEmpty()) {
						List<Long> selectedDeptList = projectFilterDTO.getDepartmentsids();

						List<Object[]> results = projectRepository.getAllUnfilledPositionList(selectedDeptList);
						unfilledPositions = results.stream().map(ProjectFetchDTO::new).collect(Collectors.toList());
					} else {
						Employee employeee = employeeRepository.findByEmpId(projectFilterDTO.getCurrentUserEmpId());
						List<Long> deptIdOfOther = departmentRepository
								.findDepartmentIdOfCurrentUser(employeee.getJobRoleId());

						List<Object[]> Results = projectRepository.getAllUnfilledPositionList(deptIdOfOther);
						unfilledPositions = Results.stream().map(ProjectFetchDTO::new).collect(Collectors.toList());
					}
				}
				populateProjectDetails(unfilledPositions);

				responseData.setUnfilledPositions(unfilledPositions);
				if (!unfilledPositions.isEmpty()) {
					response.setServiceResponse(responseData);
					response.setServiceStatus(response.STATUS_SUCCESS);

				} else {
					response.setServiceResponse("No unfilled positions...!!");
					response.setServiceStatus(response.STATUS_FAIL);

				}
				return response;
			}

			List<ProjectFetchDTO> monitoring = new ArrayList<ProjectFetchDTO>();
			if ("monitoring".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {

				if (Boolean.TRUE.equals(projectFilterDTO.getIsAdmin())) {

					if (projectFilterDTO.getDepartmentsids() != null
							&& !projectFilterDTO.getDepartmentsids().isEmpty()) {
						List<Long> selectedDeptList = projectFilterDTO.getDepartmentsids();
						List<Object[]> results = projectRepository.getAllMonitoringList(selectedDeptList);

						monitoring = results.stream().map(ProjectFetchDTO::new).collect(Collectors.toList());
					} else {
						List<Long> deptIdsAccToRole = departmentRepository.findAllDepartments();
						List<Object[]> results = projectRepository.getAllMonitoringList(deptIdsAccToRole);

						monitoring = results.stream().map(ProjectFetchDTO::new).collect(Collectors.toList());
					}
				} else if (Boolean.TRUE.equals(projectFilterDTO.getIsHod())) {

					List<Department> deptData = departmentRepository
							.findByHodId(projectFilterDTO.getCurrentUserEmpId());
					if (deptData != null) {
						for (Department data : deptData) {
							if (data != null && data.getDeptId() != null) {
								deptIdList.add(data.getDeptId());
							}
						}
					}

					if (projectFilterDTO.getDepartmentsids() != null
							&& !projectFilterDTO.getDepartmentsids().isEmpty()) {
						List<Long> selectedDeptList = projectFilterDTO.getDepartmentsids();
						List<Object[]> results = projectRepository.getAllMonitoringList(selectedDeptList);

						monitoring = results.stream().map(ProjectFetchDTO::new).collect(Collectors.toList());
					} else {
						List<Object[]> results = projectRepository.getAllMonitoringList(deptIdList);
						monitoring = results.stream().map(ProjectFetchDTO::new).collect(Collectors.toList());
					}
				} else if (Boolean.TRUE.equals(projectFilterDTO.getIsOther())) {
					if (projectFilterDTO.getDepartmentsids() != null
							&& !projectFilterDTO.getDepartmentsids().isEmpty()) {
						List<Long> selectedDeptList = projectFilterDTO.getDepartmentsids();

						List<Object[]> results = projectRepository.getAllMonitoringList(selectedDeptList);
						monitoring = results.stream().map(ProjectFetchDTO::new).collect(Collectors.toList());
					} else {
						Employee employeee = employeeRepository.findByEmpId(projectFilterDTO.getCurrentUserEmpId());
						List<Long> deptIdOfOther = departmentRepository
								.findDepartmentIdOfCurrentUser(employeee.getJobRoleId());

						List<Object[]> Results = projectRepository.getAllMonitoringList(deptIdOfOther);
						monitoring = Results.stream().map(ProjectFetchDTO::new).collect(Collectors.toList());
					}
				}

				populateProjectDetails(monitoring);

				responseData.setMonitoringProjects(monitoring);

				if (!monitoring.isEmpty()) {
					response.setServiceResponse(responseData);
					response.setServiceStatus(response.STATUS_SUCCESS);

				} else {
					response.setServiceResponse("No projects found...!!");
					response.setServiceStatus(response.STATUS_FAIL);

				}
				return response;
			}

			List<ProjectFetchDTO> totalProjects = new ArrayList<ProjectFetchDTO>();
			if ("TotalProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {

				if (Boolean.TRUE.equals(projectFilterDTO.getIsAdmin())) {

					if (projectFilterDTO.getDepartmentsids() != null
							&& !projectFilterDTO.getDepartmentsids().isEmpty()) {
						List<Long> selectedDeptList = projectFilterDTO.getDepartmentsids();
						List<Object[]> results = projectRepository.getAllTotalProjectList(selectedDeptList);

						totalProjects = results.stream().map(ProjectFetchDTO::createFromTotalProject)
								.collect(Collectors.toList());
					} else {
						List<Long> deptIdsAccToRole = departmentRepository.findAllDepartments();
						List<Object[]> results = projectRepository.getAllTotalProjectList(deptIdsAccToRole);

						totalProjects = results.stream().map(ProjectFetchDTO::createFromTotalProject)
								.collect(Collectors.toList());
					}
				} else if (Boolean.TRUE.equals(projectFilterDTO.getIsHod())) {

					List<Department> deptData = departmentRepository
							.findByHodId(projectFilterDTO.getCurrentUserEmpId());
					if (deptData != null) {
						for (Department data : deptData) {
							if (data != null && data.getDeptId() != null) {
								deptIdList.add(data.getDeptId());
							}
						}
					}

					if (projectFilterDTO.getDepartmentsids() != null
							&& !projectFilterDTO.getDepartmentsids().isEmpty()) {
						List<Long> selectedDeptList = projectFilterDTO.getDepartmentsids();
						List<Object[]> results = projectRepository.getAllTotalProjectList(selectedDeptList);

						totalProjects = results.stream().map(ProjectFetchDTO::createFromTotalProject)
								.collect(Collectors.toList());
					} else {
						List<Object[]> results = projectRepository.getAllTotalProjectList(deptIdList);
						totalProjects = results.stream().map(ProjectFetchDTO::createFromTotalProject)
								.collect(Collectors.toList());
					}
				} else if (Boolean.TRUE.equals(projectFilterDTO.getIsOther())) {
					if (projectFilterDTO.getDepartmentsids() != null
							&& !projectFilterDTO.getDepartmentsids().isEmpty()) {
						List<Long> selectedDeptList = projectFilterDTO.getDepartmentsids();

						List<Object[]> results = projectRepository.getAllTotalProjectList(selectedDeptList);
						totalProjects = results.stream().map(ProjectFetchDTO::createFromTotalProject)
								.collect(Collectors.toList());
					} else {
						Employee employeee = employeeRepository.findByEmpId(projectFilterDTO.getCurrentUserEmpId());
						List<Long> deptIdOfOther = departmentRepository
								.findDepartmentIdOfCurrentUser(employeee.getJobRoleId());

						List<Object[]> Results = projectRepository.getAllTotalProjectList(deptIdOfOther);
						totalProjects = Results.stream().map(ProjectFetchDTO::createFromTotalProject)
								.collect(Collectors.toList());
					}
				}
//                List<Long> projectIds = totalProjects.stream()
//                        .peek(data -> data.setActive(null))
//
//                        .map(ProjectFetchDTO::getProjectId)
//                        .filter(Objects::nonNull)
//                        .map(Integer::longValue)
//                        .collect(Collectors.toList());
//
//                // Fetch PM and Overhead data
//                List<ProjectManagersDTO> pmData = projectManagerMappingRepository
//                        .getAllProjectManagerListWithNameThroughPids(projectIds);
//                List<ProjectOverheadsDTO> overHeadData = projectOverheadMappingRepository
//                        .findProjectOverheadsPerProjectThroughPidList(projectIds);
//
//                // Loop over final data list and populate PM and OH info
//                totalProjects.forEach(data -> {
//                    Long currentProjectId = data.getProjectId() != null
//                            ? data.getProjectId().longValue()
//                            : null;
//
//                    if (currentProjectId != null) {
//                        // Filter PM data
//                        List<ProjectManagersDTO> selectedPmData = pmData.stream()
//                                .filter(pm -> Objects.equals(pm.getProjectId(), currentProjectId))
//                                .collect(Collectors.toList());
//
//                        data.setProjectManagers(selectedPmData);
//                        if (!selectedPmData.isEmpty()) {
//                            List<Long> pmIdList = selectedPmData.stream()
//                                    .map(ProjectManagersDTO::getProjectManagerId)
//                                    .filter(Objects::nonNull)
//                                    .collect(Collectors.toList());
//                            data.setProjectManagerId(pmIdList);
//                        }
//
//                        // Filter Overhead data
//                        List<ProjectOverheadsDTO> selectedOverHeadData = overHeadData.stream()
//                                .filter(oh -> Objects.equals(oh.getProjectId(), currentProjectId))
//                                .collect(Collectors.toList());
//
//                        data.setProjectOverheads(selectedOverHeadData);
//                        if (!selectedOverHeadData.isEmpty()) {
//                            List<Long> ohIdList = selectedOverHeadData.stream()
//                                    .map(ProjectOverheadsDTO::getProjectOverheadId)
//                                    .filter(Objects::nonNull)
//                                    .collect(Collectors.toList());
//                            data.setProjectOverheadId(ohIdList);
//                        }
//                    }
//                });

				populateProjectDetails(totalProjects);

				responseData.setTotalProjects(totalProjects);

				if (!totalProjects.isEmpty()) {
					response.setServiceResponse(responseData);
					response.setServiceStatus(response.STATUS_SUCCESS);

				} else {
					response.setServiceResponse("No projects found...!!");
					response.setServiceStatus(response.STATUS_FAIL);

				}
				return response;
			}

			if ("All".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
				approvalStatus = "All";
				approvalCheck = false;
			} else {

				if ("Pending for approval".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
					approvalStatus = "true";
					approvalCheck = true;
				} else if ("Approved".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
					approvalStatus = "false";
					approvalCheck = true;
				} else if ("Rejected".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
					approvalStatus = "Rejected";
					approvalCheck = true;
				}

			}

			if ("Completed".equalsIgnoreCase(projectFilterDTO.getCompletionStatus())) {
				status = "Completed";
			} else if ("completedInIshine".equalsIgnoreCase(projectFilterDTO.getCompletionStatus())) {
				projectStatus = "Completed";
			} else if ("CompletedWithTeam".equalsIgnoreCase(projectFilterDTO.getCompletionStatus())) {
				status = "Completed";
			} else if ("CompletedWithShankh".equalsIgnoreCase(projectFilterDTO.getCompletionStatus())) {
				status = "Completed";
			} else {
				projectStatus = null;
				status = null;
			}
			// for admin
			if (Boolean.TRUE.equals(projectFilterDTO.getIsAdmin())) {
				if (!projectFilterDTO.getDepartmentsids().isEmpty() && projectFilterDTO.getDepartmentsids() != null) {
					List<Long> selectedDeptList = projectFilterDTO.getDepartmentsids();
					Set<Integer> matchingProjectIds = teamRepository.findAll().stream().filter(team -> {
						if (team.getDeptIds() == null)
							return false;
						List<String> teamDeptIds = Arrays.asList(team.getDeptIds().split(","));
						return selectedDeptList.stream().map(String::valueOf).anyMatch(teamDeptIds::contains);
					}).map(Team::getProjectId).filter(Objects::nonNull).collect(Collectors.toSet());
					projectIdSet = matchingProjectIds;
					if ("All".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())
							&& projectFilterDTO.getCompletionStatus() == null) {
						finalDataList = projectRepository.getAllProjectList(selectedDeptList);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					} else if ("Not Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
						// List<Long> deptIdsAccToRole = departmentRepository.findAllDepartments() ;
						finalDataList = projectRepository.getAllNotStartedProjects(selectedDeptList);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					} else if ("completedInIshine".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
						finalDataList = projectRepository.getAllCompletedProjectListInIshine(selectedDeptList);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					} else if ("ApprovedProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
						finalDataList = projectRepository.getAllApprovedProjectList(selectedDeptList);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					} else if ("PendingProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
						finalDataList = projectRepository.getAllPendingProjectList(selectedDeptList);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					} else if ("RejectedProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
						finalDataList = projectRepository.getAllRejectedProjectList(selectedDeptList);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					} else if ("CompletedWithShankh".equalsIgnoreCase(projectFilterDTO.getCompletionStatus())) {
						finalDataList = projectRepository.getAllActiveProjectList(projectStatus, status, approvalStatus,
								projectIdSet, false, approvalCheck);
						// Merging po_details
						finalDataList = groupOfPoDetilasByProject(finalDataList);

					} else if ("CompletedWithTeam".equalsIgnoreCase(projectFilterDTO.getCompletionStatus())) {
						finalDataList = projectRepository.completedInSankhButTeamMappedList(projectIdSet, true);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					} else if (!"CompletedWithTeam".equalsIgnoreCase(projectFilterDTO.getCompletionStatus())) {

						List<Object[]> results = projectRepository.getAllTotalProjectList(selectedDeptList);
						finalDataList = results.stream().map(ProjectFetchDTO::createFromTotalProject)
								.collect(Collectors.toList());

					} else {
						finalDataList = projectRepository.completedInSankhButTeamMappedList(projectIdSet, true);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					}
				} else {
					if ("All".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())
							&& projectFilterDTO.getCompletionStatus() == null) {
						List<Long> deptIdsAccToRole = departmentRepository.findAllDepartments();
						finalDataList = projectRepository.getAllProjectList(deptIdsAccToRole);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					} else if ("Not Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
						List<Long> deptIdsAccToRole = departmentRepository.findAllDepartments();
						finalDataList = projectRepository.getAllNotStartedProjects(deptIdsAccToRole);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					} else if ("ApprovedProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
						List<Long> deptIdsAccToRole = departmentRepository.findAllDepartments();
						finalDataList = projectRepository.getAllApprovedProjectList(deptIdsAccToRole);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					} else if ("PendingProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
						List<Long> deptIdsAccToRole = departmentRepository.findAllDepartments();
						finalDataList = projectRepository.getAllPendingProjectList(deptIdsAccToRole);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					} else if ("RejectedProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
						List<Long> deptIdsAccToRole = departmentRepository.findAllDepartments();
						finalDataList = projectRepository.getAllRejectedProjectList(deptIdsAccToRole);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					} else if ("completedInIshine".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
						List<Long> deptIdsAccToRole = departmentRepository.findAllDepartments();
						finalDataList = projectRepository.getAllCompletedProjectListInIshine(deptIdsAccToRole);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					} else if ("CompletedWithShankh".equalsIgnoreCase(projectFilterDTO.getCompletionStatus())) {
						finalDataList = projectRepository.getAllActiveProjectList(projectStatus, status, approvalStatus,
								null, false, approvalCheck);
								finalDataList = groupOfPoDetilasByProject(finalDataList);
					} else if ("CompletedWithTeam".equalsIgnoreCase(projectFilterDTO.getCompletionStatus())) {
						finalDataList = projectRepository.completedInSankhButTeamMappedList(null, false);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					} else if (!"CompletedWithTeam".equalsIgnoreCase(projectFilterDTO.getCompletionStatus())) {
						List<Long> deptIdsAccToRole = departmentRepository.findAllDepartments();
						List<Object[]> results = projectRepository.getAllTotalProjectList(deptIdsAccToRole);
						finalDataList = results.stream().map(ProjectFetchDTO::createFromTotalProject)
								.collect(Collectors.toList());
					} else {
						finalDataList = projectRepository.completedInSankhButTeamMappedList(null, false);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					}
				}

			}
			// for hod
			else if (Boolean.TRUE.equals(projectFilterDTO.getIsHod())) {
				List<Department> deptData = departmentRepository.findByHodId(projectFilterDTO.getCurrentUserEmpId());
				if (deptData != null) {
					for (Department data : deptData) {
						if (data != null && data.getDeptId() != null) {
							deptIdList.add(data.getDeptId());
						}
					}
				}

				Set<Integer> matchingProjectIds = teamRepository.findAll().stream().filter(team -> {
					if (team.getDeptIds() == null)
						return false;
					List<String> teamDeptIds = Arrays.asList(team.getDeptIds().split(","));
					return deptIdList.stream().map(String::valueOf).anyMatch(teamDeptIds::contains);
				}).map(Team::getProjectId).filter(Objects::nonNull).collect(Collectors.toSet());

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

				if (!projectFilterDTO.getDepartmentsids().isEmpty() && projectFilterDTO.getDepartmentsids() != null) {
					List<Long> selectedDeptList = projectFilterDTO.getDepartmentsids();
					Set<Integer> matchingProjIds = teamRepository.findAll().stream().filter(team -> {
						if (team.getDeptIds() == null)
							return false;
						List<String> teamDeptIds = Arrays.asList(team.getDeptIds().split(","));
						return selectedDeptList.stream().map(String::valueOf).anyMatch(teamDeptIds::contains);
					}).map(Team::getProjectId).filter(Objects::nonNull).collect(Collectors.toSet());

					projectIdSet.retainAll(matchingProjIds);

					if ("All".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())
							&& projectFilterDTO.getCompletionStatus() == null) {
						finalDataList = projectRepository.getAllProjectList(selectedDeptList);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					} else if ("Not Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
						// List<Long> deptIdsAccToRole = departmentRepository.findAllDepartments() ;
						finalDataList = projectRepository.getAllNotStartedProjects(selectedDeptList);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					} else if ("completedInIshine".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
						finalDataList = projectRepository.getAllCompletedProjectListInIshine(selectedDeptList);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					} else if ("ApprovedProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
						finalDataList = projectRepository.getAllApprovedProjectList(selectedDeptList);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					} else if ("RejectedProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
						finalDataList = projectRepository.getAllRejectedProjectList(selectedDeptList);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					} else if ("PendingProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
						finalDataList = projectRepository.getAllPendingProjectList(selectedDeptList);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					} else if ("CompletedWithShankh".equalsIgnoreCase(projectFilterDTO.getCompletionStatus())) {
						finalDataList = projectRepository.getAllActiveProjectList(projectStatus, status, approvalStatus,
								projectIdSet, true, approvalCheck);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					} else if ("CompletedWithTeam".equalsIgnoreCase(projectFilterDTO.getCompletionStatus())) {
						finalDataList = projectRepository.completedInSankhButTeamMappedList(projectIdSet, true);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					} else if (!"CompletedWithTeam".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())
							&& !"Not Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())
							&& !"completedInIshine".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())
							&& !"PendingProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())
							&& !"ApprovedProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())
							&& !"RejectedProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
						List<Object[]> results = projectRepository.getAllTotalProjectList(selectedDeptList);
						finalDataList = results.stream().map(ProjectFetchDTO::createFromTotalProject)
								.collect(Collectors.toList());
					}

				} else {
					if ("All".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())
							&& projectFilterDTO.getCompletionStatus() == null) {
						finalDataList = projectRepository.getAllProjectList(deptIdList);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					} else if ("Not Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
						// List<Long> deptIdsAccToRole = departmentRepository.findAllDepartments() ;
						finalDataList = projectRepository.getAllNotStartedProjects(deptIdList);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					} else if ("completedInIshine".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
						finalDataList = projectRepository.getAllCompletedProjectListInIshine(deptIdList);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					} else if ("ApprovedProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
						finalDataList = projectRepository.getAllApprovedProjectList(deptIdList);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					} else if ("PendingProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
						finalDataList = projectRepository.getAllPendingProjectList(deptIdList);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					}

					else if ("RejectedProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
						finalDataList = projectRepository.getAllRejectedProjectList(deptIdList);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					} else if ("CompletedWithShankh".equalsIgnoreCase(projectFilterDTO.getCompletionStatus())) {
						finalDataList = projectRepository.getAllActiveProjectList(projectStatus, status, approvalStatus,
								projectIdSet, true, approvalCheck);
								finalDataList = groupOfPoDetilasByProject(finalDataList);
					} else if ("CompletedWithTeam".equalsIgnoreCase(projectFilterDTO.getCompletionStatus())) {
						finalDataList = projectRepository.completedInSankhButTeamMappedList(projectIdSet, false);
						finalDataList = groupOfPoDetilasByProject(finalDataList);
					} else if (!"CompletedWithTeam".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())
							&& !"Not Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())
							&& !"completedInIshine".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())
							&& !"PendingProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())
							&& !"ApprovedProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())
							&& !"RejectedProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
						List<Object[]> results = projectRepository.getAllTotalProjectList(deptIdList);
						finalDataList = results.stream().map(ProjectFetchDTO::createFromTotalProject)
								.collect(Collectors.toList());
					}

				}

				// if(!"CompletedWithTeam".equalsIgnoreCase(projectFilterDTO.getCompletionStatus())
				// && !"Not Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus()) &&
				// !"completedInIshine".equalsIgnoreCase(projectFilterDTO.getApprovalStatus()))
				// {
				//
				// List<Object[]> results =
				// projectRepository.getAllTotalProjectList(deptIdList);
				// finalDataList = results.stream()
				// .map(ProjectFetchDTO::createFromTotalProject)
				// .collect(Collectors.toList());
				// }else if(!"Not
				// Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())&&
				// !"completedInIshine".equalsIgnoreCase(projectFilterDTO.getApprovalStatus()))
				// {
				// finalDataList =
				// projectRepository.completedInSankhButTeamMappedList(projectIdSet,true);
				// }
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

				if (projectIdSet != null && !projectIdSet.isEmpty()) {

					if (!projectFilterDTO.getDepartmentsids().isEmpty()
							&& projectFilterDTO.getDepartmentsids() != null) {
						List<Long> selectedDeptList = projectFilterDTO.getDepartmentsids();
						Set<Integer> matchingProjectIds = teamRepository.findAll().stream().filter(team -> {
							if (team.getDeptIds() == null)
								return false;
							List<String> teamDeptIds = Arrays.asList(team.getDeptIds().split(","));
							return selectedDeptList.stream().map(String::valueOf).anyMatch(teamDeptIds::contains);
						}).map(Team::getProjectId).filter(Objects::nonNull).collect(Collectors.toSet());

						projectIdSet.retainAll(matchingProjectIds);

						if ("All".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())
								&& projectFilterDTO.getCompletionStatus() == null) {
							finalDataList = projectRepository.getAllProjectList(selectedDeptList);
							finalDataList = groupOfPoDetilasByProject(finalDataList);
						} else if ("Not Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
							// List<Long> deptIdsAccToRole = departmentRepository.findAllDepartments() ;
							finalDataList = projectRepository.getAllNotStartedProjects(selectedDeptList);
							finalDataList = groupOfPoDetilasByProject(finalDataList);
						} else if ("completedInIshine".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
							finalDataList = projectRepository.getAllCompletedProjectListInIshine(selectedDeptList);
							finalDataList = groupOfPoDetilasByProject(finalDataList);
						} else if ("ApprovedProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
							finalDataList = projectRepository.getAllApprovedProjectList(selectedDeptList);
						} else if ("PendingProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
							finalDataList = projectRepository.getAllPendingProjectList(selectedDeptList);
							finalDataList = groupOfPoDetilasByProject(finalDataList);
						} else if ("RejectedProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
							finalDataList = projectRepository.getAllRejectedProjectList(selectedDeptList);
							finalDataList = groupOfPoDetilasByProject(finalDataList);
						} else if ("CompletedWithShankh".equalsIgnoreCase(projectFilterDTO.getCompletionStatus())) {
							finalDataList = projectRepository.getAllActiveProjectList(projectStatus, status,
									approvalStatus, projectIdSet, true, approvalCheck);
							finalDataList = groupOfPoDetilasByProject(finalDataList);
						} else if ("CompletedWithTeam".equalsIgnoreCase(projectFilterDTO.getCompletionStatus())) {
							finalDataList = projectRepository.completedInSankhButTeamMappedList(projectIdSet, false);
							finalDataList = groupOfPoDetilasByProject(finalDataList);
						} else if (!"CompletedWithTeam".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())
								&& !"Not Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())
								&& !"completedInIshine".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())
								&& !"PendingProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())
								&& !"ApprovedProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())
								&& !"RejectedProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
							List<Object[]> results = projectRepository.getAllTotalProjectList(deptIdList);
							finalDataList = results.stream().map(ProjectFetchDTO::createFromTotalProject)
									.collect(Collectors.toList());
						}

					} else {
						Employee employeee = employeeRepository.findByEmpId(projectFilterDTO.getCurrentUserEmpId());
						List<Long> deptIdOfOther = departmentRepository
								.findDepartmentIdOfCurrentUser(employeee.getJobRoleId());
						if ("All".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())
								&& projectFilterDTO.getCompletionStatus() == null) {
							finalDataList = projectRepository.getAllProjectList(deptIdOfOther);
							finalDataList = groupOfPoDetilasByProject(finalDataList);
						} else if ("Not Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
							// List<Long> deptIdsAccToRole = departmentRepository.findAllDepartments() ;
							finalDataList = projectRepository.getAllNotStartedProjects(deptIdOfOther);
							finalDataList = groupOfPoDetilasByProject(finalDataList);
						} else if ("completedInIshine".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
							finalDataList = projectRepository.getAllCompletedProjectListInIshine(deptIdOfOther);
							finalDataList = groupOfPoDetilasByProject(finalDataList);
						} else if ("ApprovedProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
							finalDataList = projectRepository.getAllApprovedProjectList(deptIdOfOther);
						} else if ("PendingProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
							finalDataList = projectRepository.getAllPendingProjectList(deptIdOfOther);
							finalDataList = groupOfPoDetilasByProject(finalDataList);
						} else if ("RejectedProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
							finalDataList = projectRepository.getAllRejectedProjectList(deptIdOfOther);
							finalDataList = groupOfPoDetilasByProject(finalDataList);
						} else if ("CompletedWithShankh".equalsIgnoreCase(projectFilterDTO.getCompletionStatus())) {
							finalDataList = projectRepository.getAllActiveProjectList(projectStatus, status,
									approvalStatus, projectIdSet, true, approvalCheck);
								finalDataList = groupOfPoDetilasByProject(finalDataList);
						} else if ("CompletedWithTeam".equalsIgnoreCase(projectFilterDTO.getCompletionStatus())) {
							finalDataList = projectRepository.completedInSankhButTeamMappedList(projectIdSet, false);
							finalDataList = groupOfPoDetilasByProject(finalDataList);
						} else if (!"CompletedWithTeam".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())
								&& !"Not Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())
								&& !"completedInIshine".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())
								&& !"PendingProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())
								&& !"ApprovedProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())
								&& !"RejectedProjects".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
							List<Object[]> results = projectRepository.getAllTotalProjectList(deptIdList);
							finalDataList = results.stream().map(ProjectFetchDTO::createFromTotalProject)
									.collect(Collectors.toList());
						}

					}

					// if(!"CompletedWithTeam".equalsIgnoreCase(projectFilterDTO.getCompletionStatus())
					// && !"Not Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus()) &&
					// !"completedInIshine".equalsIgnoreCase(projectFilterDTO.getApprovalStatus()))
					// {
					// finalDataList =
					// projectRepository.getAllActiveProjectList(projectStatus,status,approvalStatus,projectIdSet,true,approvalCheck);
					// }else if(!"Not
					// Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus()) &&
					// !"completedInIshine".equalsIgnoreCase(projectFilterDTO.getApprovalStatus()))
					// {
					// finalDataList =
					// projectRepository.completedInSankhButTeamMappedList(projectIdSet,true);
					// }

				}
			} else {
				finalDataList = null;
			}
			if (finalDataList != null && !finalDataList.isEmpty()) {
//                List<Long> projectIds = finalDataList.stream()
//                        // .peek(data -> data.setActive(null))
//
//                        .map(ProjectFetchDTO::getProjectId)
//                        .filter(Objects::nonNull)
//                        .map(id -> ((Number) id).longValue())
//                        .collect(Collectors.toList());
//
//                // Fetch PM and Overhead data
//                List<ProjectManagersDTO> pmData = projectManagerMappingRepository
//                        .getAllProjectManagerListWithNameThroughPids(projectIds);
//                List<ProjectOverheadsDTO> overHeadData = projectOverheadMappingRepository
//                        .findProjectOverheadsPerProjectThroughPidList(projectIds);
//
//                // Loop over final data list and populate PM and OH info
//                finalDataList.forEach(data -> {
//                    Long currentProjectId = data.getProjectId() != null
//                            ? data.getProjectId().longValue()
//                            : null;
//
//                    if (currentProjectId != null) {
//                        // Filter PM data
//                        List<ProjectManagersDTO> selectedPmData = pmData.stream()
//                                .filter(pm -> Objects.equals(pm.getProjectId(), currentProjectId))
//                                .collect(Collectors.toList());
//
//                        data.setProjectManagers(selectedPmData);
//                        if (!selectedPmData.isEmpty()) {
//                            List<Long> pmIdList = selectedPmData.stream()
//                                    .map(ProjectManagersDTO::getProjectManagerId)
//                                    .filter(Objects::nonNull)
//                                    .collect(Collectors.toList());
//                            data.setProjectManagerId(pmIdList);
//                        }
//
//                        // Filter Overhead data
//                        List<ProjectOverheadsDTO> selectedOverHeadData = overHeadData.stream()
//                                .filter(oh -> Objects.equals(oh.getProjectId(), currentProjectId))
//                                .collect(Collectors.toList());
//
//                        data.setProjectOverheads(selectedOverHeadData);
//                        if (!selectedOverHeadData.isEmpty()) {
//                            List<Long> ohIdList = selectedOverHeadData.stream()
//                                    .map(ProjectOverheadsDTO::getProjectOverheadId)
//                                    .filter(Objects::nonNull)
//                                    .collect(Collectors.toList());
//                            data.setProjectOverheadId(ohIdList);
//                        }
//                    }
//                });

				populateProjectDetails(finalDataList);

				responseData.setCombinedNewProjects(finalDataList);
			}

			if ((responseData.getCombinedNewProjects() != null && !responseData.getCombinedNewProjects().isEmpty())
					|| (responseData.getExpiredTNMProjects() != null
							&& !responseData.getExpiredTNMProjects().isEmpty())) {
				response.setServiceResponse(responseData);
				response.setServiceStatus(response.STATUS_SUCCESS);
				response.setServiceResponse1(finalDataList.size());
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

	private void populateProjectDetails(List<ProjectFetchDTO> projectList) {
		if (projectList == null || projectList.isEmpty()) {
			return;
		}

		List<Long> projectIds = projectList.stream().map(ProjectFetchDTO::getProjectId).filter(Objects::nonNull)
				.map(id -> ((Number) id).longValue()).collect(Collectors.toList());

		if (projectIds.isEmpty())
			return;

		List<ProjectManagersDTO> pmData = projectManagerMappingRepository
				.getAllProjectManagerListWithNameThroughPids(projectIds);
		List<ProjectOverheadsDTO> overHeadData = projectOverheadMappingRepository
				.findProjectOverheadsPerProjectThroughPidList(projectIds);

		projectList.forEach(data -> {
			Long currentProjectId = data.getProjectId() != null ? ((Number) data.getProjectId()).longValue() : null;

			if (currentProjectId != null) {
				List<ProjectManagersDTO> selectedPmData = pmData.stream()
						.filter(pm -> Objects.equals(pm.getProjectId(), currentProjectId)).collect(Collectors.toList());

				data.setProjectManagers(selectedPmData);
				if (!selectedPmData.isEmpty()) {
					List<Long> pmIdList = selectedPmData.stream().map(ProjectManagersDTO::getProjectManagerId)
							.filter(Objects::nonNull).collect(Collectors.toList());
					data.setProjectManagerId(pmIdList);
				}

				List<ProjectOverheadsDTO> selectedOverHeadData = overHeadData.stream()
						.filter(oh -> Objects.equals(oh.getProjectId(), currentProjectId)).collect(Collectors.toList());

				data.setProjectOverheads(selectedOverHeadData);
				if (!selectedOverHeadData.isEmpty()) {
					List<Long> ohIdList = selectedOverHeadData.stream().map(ProjectOverheadsDTO::getProjectOverheadId)
							.filter(Objects::nonNull).collect(Collectors.toList());
					data.setProjectOverheadId(ohIdList);
				}
			}
		});
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
			if (projectFetchDTO.getProjectId() == null) {
				List<ResourceRequirementDTO> resourceRequirementsTemp = resourceRequirementTempRepo
						.findByPoProjectId(projectFetchDTO.getPoProjectId());
				response.setServiceStatus(response.STATUS_SUCCESS);
				response.setServiceResponse(resourceRequirementsTemp);
			} else {
				List<ResourceRequirementDTO> resourceData = resourceRequirementRepository
						.findByProjectId(projectFetchDTO.getProjectId());

				response.setServiceStatus(response.STATUS_SUCCESS);
				response.setServiceResponse(resourceData);
			}
			return response;

		} catch (Exception e) {
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
			if (!details.isEmpty()) {
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
			} else {
				response.setServiceResponse("No previous default project mapping!");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				logBuilder.append("\n No previous default project mapping!");
			}
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("\n Something went wrong!");
		}
		return response;
	}

	private ServiceResponse handleDefaultProjectUpdate(List<Long> empIds, Integer projectId, Long updatedBy,
			StringBuilder logBuilder) {

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

		List<GetActiveProjectDetailsIfMultipleDTO> dtoList = employeeTeamMapRepository
				.getActiveProjectIdAndProjectNameByEmpId(empId, currentProjectId);

		Map<Integer, GetActiveProjectDetailsIfMultipleDTO> uniqueProjectsMap = dtoList.stream()
				.collect(Collectors.toMap(GetActiveProjectDetailsIfMultipleDTO::getProjectId, dto -> dto,
						(existing, replacement) -> existing));

		List<Map<String, Object>> activeProjects = uniqueProjectsMap.values().stream().map(dto -> {
			Map<String, Object> map = new HashMap<>();
			map.put("projectId", dto.getProjectId());
			map.put("projectName", dto.getProjectName());
			return map;
		}).collect(Collectors.toList());

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
			EmpPrimaryProjectMapping empPrimaryProjectMapping = empPrimaryProjectMappingRepository
					.findByEmpIdAndIsMapped(empId, "Y");

			if (empPrimaryProjectMapping != null && empPrimaryProjectMapping.getPrimaryProjectId() != null) {
				if (Integer.parseInt(empPrimaryProjectMapping.getPrimaryProjectId().toString()) == projectId) {
//	            	System.err.println("The selected project ( projectId : " + projectId +  " ) is the default project for the employee ( empId :  " + empId );
					logs.append("The selected project ( projectId : " + projectId
							+ " ) is the default project for the employee ( empId :  " + empId);
					return 1;
				} else {
//	            	System.err.println("The selected project ( projectId : " + projectId +  " ) is not the default project for the employee ( empId :  " + empId );
					logs.append("The selected project ( projectId : " + projectId
							+ " ) is not the default project for the employee ( empId :  " + empId);
					return 0;
				}
			} else {
//	        	System.err.println("Mapping not found or null primaryProjectId");
				logs.append("\n Mapping not found or null primaryProjectId");
				return 0;
			}
		} catch (Exception e) {
//	        System.err.println("Error in isDefaultProject: " + e.getMessage());
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

			List<GetProjectDetailsForBulkDefaultUpdateProjectDTO> benchProjectList = projectRepository
					.getProjectDetailsForBulkDefaultUpdateBench();
			List<GetProjectDetailsForBulkDefaultUpdateProjectDTO> otherProjectList = projectRepository
					.getProjectDetailsForBulkDefaultUpdateOther();

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

	private List<GetProjectDetailsForBulkDefaultUpdateProjectDTO> mapResultsToDTOs(
			List<GetProjectDetailsForBulkDefaultUpdateProjectDTO> resourceDTOList) {
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

			if (teamId != null && teamName != null
					&& projectDTO.getTeamList().stream().noneMatch(t -> teamId.equals(t.getTeamId()))) {

				GetProjectDetailsForBulkDefaultUpdateTeamDTO teamDTO = new GetProjectDetailsForBulkDefaultUpdateTeamDTO();
				teamDTO.setTeamId(teamId);
				teamDTO.setTeamName(teamName);
				projectDTO.getTeamList().add(teamDTO);
			}

			if (resource.getResourceOverviewId() != null && projectDTO.getResourceRequirement().stream()
					.noneMatch(r -> r.getResourceOverviewId().equals(resource.getResourceOverviewId()))) {

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

	@Transactional(rollbackFor = Exception.class)
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

				// added poRequirementOverviewid changes
				if (dto.getPoRequirementMappingId() != null) {
					member.setPoRequirementMappingId(dto.getPoRequirementMappingId());
				}
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

					// added poRequirementMappingid changes
					empTeamMap.setPoRequirementMappingId(newMember.getPoRequirementMappingId() != null
							? Long.parseLong(newMember.getPoRequirementMappingId().toString())
							: null);

					empTeamMap.setUpdatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : null);

					if (newMember.getIsDefaultProject() != null) {
						if (newMember.getIsDefaultProject() == 1) {
							defaultProjectEmpIds.add(newMember.getEmpId());
						}
					}

					mapList.add(empTeamMap);

					List<EmployeeTeamMap> teamMapDbResponse = employeeTeamMapRepository.saveAll(mapList);

					// Add default project mapping
					if (!defaultProjectEmpIds.isEmpty()) {
						ServiceResponse defaultProjectResponse = this.handleDefaultProjectUpdate(defaultProjectEmpIds,
								dto.getProjectId(), dto.getCreatedBy(), logBuilder);

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
													newActivity.getCommonProperty().setCreatedBy(dto.getCreatedBy());
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
									"Dear " + findEmp.getName() + "<br>" + "You have been mapped "
											+ " under the project " + projectFind.getProjectName() + "<br><br><br>"
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

				Map<String, Object> activeProjectInfo = this.getActiveProjectDetailsIfMultiple(empId,
						otherProjectSetDTO.getProjectId());
				if ((Boolean) activeProjectInfo.get("isMultipleActiveProjects")) {
					List<Map<String, Object>> otherProjects = (List<Map<String, Object>>) activeProjectInfo
							.get("projects");
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

			Set<String> specialDepartments = Set.of("Admin", "Resource Management Group", "Director", "Super Admin",
					"Accounts", "HR");

			List<Object[]> resultSet = new ArrayList<>();

			if (role.equalsIgnoreCase("SuperAdmin") || name.equalsIgnoreCase("Director")
					|| name.equalsIgnoreCase("Super Admin") || role.equalsIgnoreCase("Accounts")
					|| specialDepartments.contains(departmentName)) {
				resultSet = projectRepository.getBenchEmployeeMoreThan30Days();
			} else if (departmentRepository.existsByHodId(projectFilterDTO.getCurrentUserEmpId())) {
				List<Long> deptIds = departmentRepository.findDeptIdsByHodId(projectFilterDTO.getCurrentUserEmpId());
				resultSet = projectRepository.getBenchEmployeeMoreThan30DaysInDeptIds(deptIds);
			} else {
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
				Long daysOnBench = ((Number) row[24]).longValue();

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

				RMGProject project = employee.getRmgprojects().stream().filter(p -> p.getProjectId().equals(projectId))
						.findFirst().orElse(null);

				if (project == null) {
					project = new RMGProject();
					project.setProjectId(projectId);
					project.setProjectName(projectName);
					project.setPoProjectId(poProjectId);
					project.setProjectStartDate(poStartDate);
					project.setProjectEndDate(poEndDate);
					project.setApmosysRM(apmosysRM);
					project.setClientRM(clientRM);
					project.setPoProjectType(poProjectType);
					project.setPoNo(poNo);
					project.setClientName(clientName);
					project.setRmgTeam(new ArrayList<>());
					project.setProjectManagers(new ArrayList<>());
					employee.getRmgprojects().add(project);
				}

				boolean teamExists = project.getRmgTeam().stream().anyMatch(t -> t.getTeamId().equals(teamId));

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
		logBuilder.append("Fetching timesheet summary for emp_id: ").append(resourceManagementDTO.getEmpId());

		try {
			List<SummaryChartDTO> summaryData = projectRepository
					.getProjectTimesheetSummaryByEmpId(resourceManagementDTO.getEmpId());

			List<SummaryChartDTO> result = new ArrayList<>();

			if (!summaryData.isEmpty()) {
				for (SummaryChartDTO row : summaryData) {
					SummaryChartDTO dto = new SummaryChartDTO();

					dto.setProjectName(row.getProjectName() != null ? row.getProjectName().toString() : null);
					dto.setTotalTimesheetsFilled(row.getTotalTimesheetsFilled() != null
							? Long.parseLong(row.getTotalTimesheetsFilled().toString())
							: 0);

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

	// public ServiceResponse getProjectStatusByPoProjectId(Set<Long> poProjectId) {
	// ServiceResponse response = new ServiceResponse();
	// LogDTO apiLogInfo = new LogDTO();
	// apiLogInfo.setSubFeatureName("getProjectStatusByPoProjectId");
	// apiLogInfo.setApiUrl("/api/getProjectStatusByPoProjectId");
	// apiLogInfo.setLogLevel("INFO");
	// StringBuilder logBuilder = new StringBuilder();
	// logBuilder.append("\n getProjectStatusByPoProjectId ");
	// ApiLog initialLog = null;
	// String exceptionDetailsForLog = null;
	// int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
	// String sourceSystem = httpRequest.getRequestURI().toString();

	// try {

	// initialLog =
	// apiLogUtility.startLog(poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),"getProjectStatusByPoProjectId",
	// "PoPortal", null, httpRequest);
	// if (poProjectId == null || poProjectId.isEmpty()){
	// response.setServiceResponse("PoProject Id cannot be null!");
	// apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	// response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	// apiLogInfo.setApiResponse("PoProject Id cannot be null!");
	// exceptionDetailsForLog = "PoProject Id cannot be null!";
	// finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
	// throw new BadRequestException("PoProject Id cannot be null!");
	// }

	// List<Object[]> poProjectStatusList =
	// projectRepository.getProjectStatusByPoProjectId(poProjectId);
	// if (poProjectStatusList != null && !poProjectStatusList.isEmpty()) {
	// Map<Long, String> result = objListToPoProjectStatusMap(poProjectStatusList);
	// response.setServiceResponse(result);
	// response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	// apiLogInfo.setApiResponse("Project status fetched");
	// apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	// finalHttpStatusCode = HttpStatus.OK.value();

	// } else {
	// response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	// response.setServiceResponse("No Projects found for provided poProjectIds.");
	// apiLogInfo.setApiResponse("No Projects found for provided poProjectIds.");
	// apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	// finalHttpStatusCode = HttpStatus.NOT_FOUND.value();
	// throw new DataNotFoundException("No Projects found for provided
	// poProjectIds.");
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	// response.setServiceResponse("Something went wrong.");
	// response.setServiceError(e.getMessage());
	// apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	// apiLogInfo.setLogLevel("ERROR");
	// exceptionDetailsForLog = e.toString();
	// } finally {
	// if (initialLog != null) {
	// apiLogUtility.endLog(initialLog.getId(),sourceSystem ,finalHttpStatusCode,
	// exceptionDetailsForLog, httpRequest);
	// }
	// }
	// return response;
	// }

	public ServiceResponse getProjectStatusByPoProjectId(Set<Long> poProjectId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getProjectStatusByPoProjectId");
		apiLogInfo.setApiUrl("/api/getProjectStatusByPoProjectId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("\n getProjectStatusByPoProjectId ");

		try {
			if (!poProjectId.isEmpty()) {

				List<Object[]> poProjectStatus = projectRepository.getProjectStatusByPoProjectId(poProjectId);

				Map<Long, String> result = new HashMap<>();

				for (Object[] row : poProjectStatus) {
					Long projectId = row[0] != null ? ((Number) row[0]).longValue() : null;
					String status = row[1] != null ? row[1].toString() : "Not Started";
					if (projectId != null) {
						result.put(projectId, status);
					}
				}

				response.setServiceResponse(result);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setApiResponse("Project status fetched");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No poProjectIds provided.");
				apiLogInfo.setApiResponse("No poProjectIds provided.");
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

			Set<String> specialDepartments = Set.of("Admin", "Resource Management Group", "Director", "Super Admin",
					"Accounts", "HR");

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

				if (departmentRepository.existsByHodId(currentUserEmpId)) {

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

				List<GetDeptIdByRoleDTO> pmDeptIds = departmentRepository
						.findDeptIdsForProjectManager(currentUserEmpId);
				if (pmDeptIds != null && !pmDeptIds.isEmpty()) {
					logBuilder.append("\n Department list fetched for Project Manager.");
					for (GetDeptIdByRoleDTO dto : pmDeptIds) {
						accessibleDeptIds.add(dto.getDeptId());
						fullDeptMap.put(dto.getDeptId(), dto.getName());
					}
					isOther = projectFilterDTO.getIsHod() == null ? true : false;
				}

				List<GetDeptIdByRoleDTO> overheadDeptIds = departmentRepository
						.findDeptIdsForProjectOverhead(currentUserEmpId);
				if (overheadDeptIds != null && !overheadDeptIds.isEmpty()) {
					logBuilder.append("\n Department list fetched for Project Overhead.");
					for (GetDeptIdByRoleDTO dto : overheadDeptIds) {
						accessibleDeptIds.add(dto.getDeptId());
						fullDeptMap.put(dto.getDeptId(), dto.getName());
					}
					isOther = projectFilterDTO.getIsHod() == null ? true : false;
				}
				List<String> teamLeadDeptList = departmentRepository.findDeptIdsForTeamLead(currentUserEmpId);
				String teamLeadDeptCsv = String.join(",", teamLeadDeptList)	;
				if (teamLeadDeptCsv != null && !teamLeadDeptCsv.isEmpty()) {
					logBuilder.append("\n Department list fetched for Team Lead.");
					List<Long> teamLeadIds = Arrays.stream(teamLeadDeptCsv.split(",")).map(String::trim)
							.filter(s -> s.matches("\\d+")).map(Long::parseLong)
							.filter(deptId -> !accessibleDeptIds.contains(deptId)).collect(Collectors.toList());

					if (!teamLeadIds.isEmpty()) {
						List<GetDeptIdByRoleDTO> teamLeadDepts = departmentRepository.findDepartmentsByIds(teamLeadIds);
						for (GetDeptIdByRoleDTO dto : teamLeadDepts) {
							accessibleDeptIds.add(dto.getDeptId());
							fullDeptMap.put(dto.getDeptId(), dto.getName());
						}
						isOther = projectFilterDTO.getIsHod() == null;
					}
				}
				List<String> spocDeptList = departmentRepository.findDeptIdsForSpoc(currentUserEmpId);
				String spocDeptCsv = String.join(",", spocDeptList);
				if (spocDeptCsv != null && !spocDeptCsv.isEmpty()) {
					logBuilder.append("\n Department list fetched for Spoc.");
					List<Long> spocIds = Arrays.stream(spocDeptCsv.split(",")).map(String::trim)
							.filter(s -> s.matches("\\d+")).map(Long::parseLong)
							.filter(deptId -> !accessibleDeptIds.contains(deptId)).collect(Collectors.toList());

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
				response.setServiceResponse(
						"You currently do not have access to any departments or projects based on your role (Admin, HOD, Project Manager, Project Overhead, Team Lead, or SPOC).");
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

	private ResourceManagementDTO fetchPoProjectForSync() {
		ResourceManagementDTO poPortalProject = new ResourceManagementDTO();
		try {
			poPortalProject = restTemplate.getForObject(realtimePoProjectData, ResourceManagementDTO.class);
			System.out.println(poPortalProject);
		} catch (RestClientException e) {
			throw new RuntimeException("Error fetching project from PoPortal: " + e.getMessage());
		}
		return poPortalProject;
	}

	public ServiceResponse crudOnAllNotstartedProjs(ResourceManagementDTO resourceManagementDTO) {
		ServiceResponse serviceResponse = new ServiceResponse();
		serviceResponse.setServiceResponse("Changes made successfully");
		serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		return serviceResponse;
	}

	@Transactional
	public ServiceResponse poCrudOperationsInIshine() {

		ServiceResponse serviceResponse = new ServiceResponse();
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("api/poCrudOperationsInIshine");
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");

		ServiceResponse teamCreatedProjectsResponse = alreadyCreatedTeam();
		if (!ServiceResponse.STATUS_SUCCESS.equals(teamCreatedProjectsResponse.getServiceStatus())) {
			return failResponse(serviceResponse, apiLogInfo, "Failed to fetch already created team projects.");
		}

		List<ResourceManagementDTO> teamCreatedProjects = castList(teamCreatedProjectsResponse.getServiceResponse());
		ResourceManagementDTO poPortalProjects = Optional.ofNullable(fetchPoProjectForSync())
				.orElse(new ResourceManagementDTO());
		System.out.println(poPortalProjects);
		processPoPortalProject(poPortalProjects, teamCreatedProjects);
		System.out.println(poPortalProjects);

		if ("Create".equals(poPortalProjects.getRequestType())) {
			ResourceManagementDTO poData = poPortalProjects;

			Project project = new Project();
			project.setPoProjectId(poData.getId());
			project.setPoNo(poData.getPoNo());
			project.setApmosysRM(poData.getApmosysRM());
			project.setApmosysRmEmail(poData.getApmosysRmEmail());
			project.setActive("Pending".equals(poData.getStatus()) ? "false"
					: "Completed".equals(poData.getStatus()) ? null : "true");
			project.setIsDraftProject(null);
			project.setClientRM(poData.getClientRM());
			project.setEndDate(convertIsoToDate(poData.getEndDate()));
			project.setPoProjectType(poData.getProjectType());
			project.setStartDate(convertIsoToDate(poData.getStartDate()));
			project.setProjectName(poData.getName());
			project.setState(poData.getClientState());
			project.setProjectCompletionDate(poData.getProjectCompletionDate());
			project.setProjectStatus(poData.getProjectStatus());
			project.setStatus(poData.getStatus());

			project.setCreatedBy(6l);

			if (poData.getCreatedOn() != null) {
				ZonedDateTime zdt = ZonedDateTime.parse(poData.getCreatedOn());
				project.setCreatedOn(Timestamp.valueOf(zdt.toLocalDateTime()));
			}

			project.setDepartmentName(poData.getDepartmentName());
			project.setDeptId(poData.getDeptId());

			Integer clientId = null;
			Optional<Client> clientOpt = clientsRepository.findByClientName(poData.getClientName());

			if (clientOpt.isPresent()) {
				clientId = clientOpt.get().getClientId();
			} else {
				Client newClient = new Client();
				newClient.setClientName(poData.getClientName());
				Client savedClient = clientsRepository.save(newClient);

				if (savedClient == null) {
					serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					serviceResponse.setServiceResponse("Failed to add client");
					apiLogInfo.setApiResponse("Failed to add client");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					return serviceResponse;
				}

				clientId = savedClient.getClientId();
				List<ClientLocation> locations = new ArrayList<>();

				if (poData.getClientLocation() != null) {
					for (String loc : poData.getClientLocation()) {
						ClientLocation cl = new ClientLocation();
						cl.setClientId(clientId);
						cl.setClientLocation(loc);
						locations.add(cl);
					}
				}

				if (poData.getClientLocation() == null || !Arrays.asList(poData.getClientLocation()).contains("WFH")) {
					ClientLocation cl = new ClientLocation();
					cl.setClientId(clientId);
					cl.setClientLocation("WFH");
					locations.add(cl);
				}

				List<ClientLocation> savedLocations = clientLocationRepository.saveAll(locations);
				if (savedLocations.isEmpty()) {
					serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					serviceResponse.setServiceResponse("Failed to add client Location");
					apiLogInfo.setApiResponse("Failed to add client Location");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					return serviceResponse;
				}

				serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				serviceResponse.setServiceResponse("Client Location added");
				apiLogInfo.setApiResponse("Client Location added");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}

			project.setClientId(clientId);
			project.setClientLocation(
					poData.getClientLocation() != null ? String.join(", ", poData.getClientLocation()) : null);

			Project savedProject = projectRepository.save(project);
			if (savedProject == null)
				return serviceResponse;

			if (poData.getResourceRequirements() != null && !poData.getResourceRequirements().isEmpty()) {
				for (ResourceRequirementDTO req : poData.getResourceRequirements()) {
					ResourceRequirement res = new ResourceRequirement();
					res.setCount(req.getCount());
					res.setDepartment(req.getDepartment());
					res.setExperience(req.getExperience());
					res.setRole(req.getRole());
					res.setResourceOverviewId(
							req.getResourceOverviewId() != null ? Long.parseLong(req.getResourceOverviewId().toString())
									: null);
					res.setProjectId(savedProject.getProjectId());

					resourceRequirementRepository.save(res);
				}
			}

			if (poData.getDepartment() != null) {
				for (String deptName : poData.getDepartment()) {
					Department dept = departmentRepository.findByName(deptName);
					if (dept != null) {
						ProjectDepartmentMap pdMap = new ProjectDepartmentMap();
						pdMap.setProjectId(savedProject.getProjectId());
						pdMap.setDeptId(dept.getDeptId());
						projectDepartmentMapRepository.save(pdMap);
						logBuilder.append("Mapped department: ").append(deptName).append("\n");
					} else {
						logBuilder.append("Department not found: ").append(deptName).append("\n");
					}
				}
			}

		} else if ("Update".equals(poPortalProjects.getRequestType())) {

			Project existingProject = projectRepository.findByPoProjectId(poPortalProjects.getId());

			if (existingProject != null) {
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

				String newActive = poPortalProjects.getStatus() != null
						&& !poPortalProjects.getStatus().equals("Pending") ? "true"
								: poPortalProjects.getStatus().equals("Completed") ? "false" : null;
				if (!Objects.equals(existingProject.getActive(), newActive)) {
					existingProject.setActive(newActive);
					isModified = true;
				}

				if (!Objects.equals(existingProject.getClientRM(), poPortalProjects.getClientRM())) {
					existingProject.setClientRM(poPortalProjects.getClientRM());
					isModified = true;
				}

				if (!Objects.equals(existingProject.getEndDate(), convertIsoToDate(poPortalProjects.getEndDate()))) {
					existingProject.setEndDate(convertIsoToDate(poPortalProjects.getEndDate()));
					isModified = true;
				}

				if (!Objects.equals(existingProject.getPoProjectType(), poPortalProjects.getProjectType())) {
					existingProject.setPoProjectType(poPortalProjects.getProjectType());
					isModified = true;
				}

				if (!Objects.equals(existingProject.getStartDate(),
						convertIsoToDate(poPortalProjects.getStartDate()))) {
					existingProject.setStartDate(convertIsoToDate(poPortalProjects.getStartDate()));
					isModified = true;
				}

				if (!Objects.equals(existingProject.getProjectName(), poPortalProjects.getName())) {
					existingProject.setProjectName(poPortalProjects.getName());
					isModified = true;
				}

				if (!Objects.equals(existingProject.getState(), poPortalProjects.getClientState())) {
					existingProject.setState(poPortalProjects.getClientState());
					isModified = true;
				}

				if (!Objects.equals(existingProject.getStatus(), poPortalProjects.getStatus())) {
					existingProject.setStatus(poPortalProjects.getStatus());
					isModified = true;
				}

				if (!Objects.equals(existingProject.getDepartmentName(), poPortalProjects.getDepartmentName())) {
					existingProject.setDepartmentName(poPortalProjects.getDepartmentName());
					isModified = true;
				}

				if (!Objects.equals(existingProject.getDeptId(), poPortalProjects.getDeptId())) {
					existingProject.setDeptId(poPortalProjects.getDeptId());
					isModified = true;
				}

				String joinedLocations = poPortalProjects.getClientLocation() != null
						? String.join(", ", poPortalProjects.getClientLocation())
						: null;

				if (!Objects.equals(existingProject.getClientLocation(), joinedLocations)) {
					existingProject.setClientLocation(joinedLocations);
					isModified = true;
				}

				String input = poPortalProjects.getCreatedOn();
				if (input != null) {
					ZonedDateTime zdt = ZonedDateTime.parse(input);
					Timestamp parsedCreatedOn = Timestamp.valueOf(zdt.toLocalDateTime());

					if (!Objects.equals(existingProject.getCreatedOn(), parsedCreatedOn)) {
						existingProject.setCreatedOn(parsedCreatedOn);
						isModified = true;
					}
				}

				Integer clientId = existingProject.getClientId();
				Client currentClient = clientId != null ? clientsRepository.findById(clientId).orElse(null) : null;

				if (currentClient == null || !currentClient.getClientName().equals(poPortalProjects.getClientName())) {
					Optional<Client> existingClientOpt = clientsRepository
							.findByClientName(poPortalProjects.getClientName());
					Client newClient;
					if (existingClientOpt.isPresent()) {
						newClient = existingClientOpt.get();
					} else {
						newClient = new Client();
						newClient.setClientName(poPortalProjects.getClientName());
						newClient = clientsRepository.save(newClient);
					}
					existingProject.setClientId(newClient.getClientId());
					isModified = true;
				}

				List<ClientLocation> existingLocations = clientLocationRepository
						.findByClientId(existingProject.getClientId());
				List<String> dbLocations = existingLocations.stream().map(ClientLocation::getClientLocation)
						.collect(Collectors.toList());
				List<String> newLocations = Arrays.asList(poPortalProjects.getClientLocation());

				if (!new HashSet<>(dbLocations).equals(new HashSet<>(newLocations))) {
					clientLocationRepository.deleteAll(existingLocations);
					List<ClientLocation> newClientLocs = newLocations.stream().map(loc -> {
						ClientLocation cl = new ClientLocation();
						cl.setClientId(existingProject.getClientId());
						cl.setClientLocation(loc);
						return cl;
					}).collect(Collectors.toList());

					clientLocationRepository.saveAll(newClientLocs);
				}

				List<ProjectPoDetails> projectPoDetailsList = projectPoDetailsRepository.findByProjectId(existingProject.getProjectId());

				if (projectPoDetailsList != null && !projectPoDetailsList.isEmpty()) {
					  List<Integer> activePoIds = projectPoDetailsList.stream()
				                .filter(ProjectPoDetails::isActive)
				                .map(po -> po.getPoId().intValue())
				                .collect(Collectors.toList());
				    
				    if (activePoIds.isEmpty()) {
				        activePoIds.add(projectPoDetailsList.get(0).getPoId().intValue());
				    }
				    List<PoDepartmentMapping> existingDeptMap = poDepartmentMappingRepository
				            .findByProjectId(existingProject.getProjectId());
				    
				    Set<Long> existingDeptIds = existingDeptMap.stream()
				            .map(PoDepartmentMapping::getDeptId)
				            .distinct()
				            .collect(Collectors.toSet());
				    
				    Set<Long> newDeptIds = new HashSet<>();

				    for (String deptName : poPortalProjects.getDepartment()) {
				        Department dept = departmentRepository.findByName(deptName);
				        if (dept != null)
				            newDeptIds.add(dept.getDeptId());
				    }

				    if (!existingDeptIds.equals(newDeptIds)) {
				        poDepartmentMappingRepository.deleteAll(existingDeptMap);
				        List<PoDepartmentMapping> newMappings = new ArrayList<>();
				        for (Integer poId : activePoIds) {
				            for (Long deptId : newDeptIds) {
				                PoDepartmentMapping pdm = new PoDepartmentMapping();
				                pdm.setDeptId(deptId);
				                pdm.setPoId(poId.longValue());
				                pdm.setActive(true);
				                newMappings.add(pdm);
				            }
				        }
				        poDepartmentMappingRepository.saveAll(newMappings);
				    }
				}

				List<ResourceRequirementDTO> existingReqList = resourceRequirementRepository
						.findByProjectId(existingProject.getProjectId());
				Map<Long, ResourceRequirementDTO> existingReqMap = new HashMap<>();
				for (ResourceRequirementDTO r : existingReqList) {
					if (r.getResourceOverviewId() != null) {
						existingReqMap.put(r.getResourceOverviewId(), r);
					}
				}

				for (ResourceRequirementDTO req : poPortalProjects.getResourceRequirements()) {
					Long overviewId = req.getResourceOverviewId() != null
							? Long.parseLong(req.getResourceOverviewId().toString())
							: null;
					if (overviewId == null)
						continue;

					ResourceRequirementDTO existing = existingReqMap.get(overviewId);
					if (existing != null) {
						boolean needsUpdate = !Objects.equals(existing.getCount(), req.getCount())
								|| !Objects.equals(existing.getDepartment(), req.getDepartment())
								|| !Objects.equals(existing.getExperience(), req.getExperience())
								|| !Objects.equals(existing.getRole(), req.getRole());

						if (needsUpdate) {
							ResourceRequirement updatedEntity = new ResourceRequirement();
							updatedEntity.setProjectId(existingProject.getProjectId());
							updatedEntity.setResourceOverviewId(overviewId);
							updatedEntity.setCount(req.getCount());
							updatedEntity.setDepartment(req.getDepartment());
							updatedEntity.setExperience(req.getExperience());
							updatedEntity.setRole(req.getRole());
							resourceRequirementRepository.save(updatedEntity);
						}
					} else {
						ResourceRequirement newEntity = new ResourceRequirement();
						newEntity.setProjectId(existingProject.getProjectId());
						newEntity.setResourceOverviewId(overviewId);
						newEntity.setCount(req.getCount());
						newEntity.setDepartment(req.getDepartment());
						newEntity.setExperience(req.getExperience());
						newEntity.setRole(req.getRole());
						resourceRequirementRepository.save(newEntity);
					}
				}

				existingProject.setUpdatedBy(6l);

				String input2 = poPortalProjects.getUpdatedOn();
				if (input2 != null) {
					existingProject.setUpdatedOn(LocalDateTime.now());
				} else {
					existingProject.setUpdatedOn(null);
				}

				if (isModified) {
					projectRepository.save(existingProject);
					System.out.println("Project updated successfully with PoProjectId: " + poPortalProjects.getId());
				} else {
					System.out.println("No changes detected for PoProjectId: " + poPortalProjects.getId());
				}
			} else {
				System.out.println("No project found with PoProjectId: " + poPortalProjects.getId());
			}

			serviceResponse.setServiceResponse(poPortalProjects);
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);

		} else if ("Delete".equals(poPortalProjects.getRequestType())) {

			Project existingProject = projectRepository.findByPoProjectId(poPortalProjects.getId());

			if (existingProject != null) {
				existingProject.setActive("false");
				existingProject.setUpdatedBy(6l);
				projectRepository.save(existingProject);

				List<ProjectManagerMapping> activePMs = projectManagerMappingRepository
						.findByProjectIdAndActive(Long.parseLong(existingProject.getProjectId().toString()), 1);
				for (ProjectManagerMapping pm : activePMs) {
					pm.setActive(0);
					pm.setUpdatedBy(6l);
					pm.setUpdatedOn(LocalDateTime.now());
				}

				projectManagerMappingRepository.saveAll(activePMs);

				List<ProjectOverheadMapping> activeOverheads = projectOverheadMappingRepository
						.findByProjectIdAndActive(Long.parseLong(existingProject.getProjectId().toString()), 1);
				for (ProjectOverheadMapping oh : activeOverheads) {
					oh.setActive(0);
					oh.setUpdatedBy(6l);
					oh.setUpdatedOn(LocalDateTime.now());
				}

				projectOverheadMappingRepository.saveAll(activeOverheads);

				System.out.println("Project updated successfully with PoProjectId: " + poPortalProjects.getId());
			} else {
				System.out.println("No changes detected for PoProjectId: " + poPortalProjects.getId());
			}

			serviceResponse.setServiceResponse(poPortalProjects);
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		} else {
			serviceResponse.setServiceResponse("No Valid request type found! ");
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
		}

		return serviceResponse;
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

			if (departmentRepository.existsByHodId(currentUserEmpId)) {

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

				if (departmentRepository.isUserMappedInAnyRole(currentUserEmpId)) {

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
				response.setServiceResponse(
						"You currently do not have access to any departments or projects based on your role (Admin, HOD, Project Manager, Project Overhead, Team Lead, or SPOC).");
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

	@Transactional
//	public ServiceResponse poCrudOperationsInIshine(ResourceManagementDTO poPortalProjects) {
//	    ServiceResponse serviceResponse = new ServiceResponse();
//	    StringBuilder logBuilder = new StringBuilder();
//	    logBuilder.append("api/poCrudOperationsInIshine");
//	    LogDTO apiLogInfo = new LogDTO();
//	    apiLogInfo.setLogLevel("INFO");
//
//	    if(poPortalProjects != null) {
//	    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//
//	    	poPortalProjects.setStartDate(
//	    	    Optional.ofNullable(poPortalProjects.getStartDate())
//	    	            .filter(s -> !s.isEmpty())
//	    	            .map(Long::parseLong)
//	    	            .map(Date::new)
//	    	            .map(dateFormat::format)
//	    	            .orElse(null)
//	    	);
//
//	    	poPortalProjects.setEndDate(
//	    	    Optional.ofNullable(poPortalProjects.getEndDate())
//	    	            .filter(s -> !s.isEmpty())
//	    	            .map(Long::parseLong)
//	    	            .map(Date::new)
//	    	            .map(dateFormat::format)
//	    	            .orElse(null)
//	    	);
//	    		    	try {
//		        if ("create".equalsIgnoreCase(poPortalProjects.getRequestType())) {
//		        	
//		        
//		        
//		        			Project existingProject = projectRepository.findByPoProjectId(poPortalProjects.getId());
//		        	
//		            if (!(existingProject != null)){
//		            	try {
//			                ResourceManagementDTO poData = poPortalProjects;
//
//			                Project project = new Project();
//			                project.setPoProjectId(poData.getId());
//			                project.setPoNo(poData.getPoNo());
//			                project.setApmosysRM(poData.getApmosysRM());
//			                project.setApmosysRmEmail(poData.getApmosysRmEmail());
//			                project.setActive("Pending".equals(poData.getStatus()) ? "true" :
//			                                  "Completed".equals(poData.getStatus()) ? null : "false");
//			                project.setIsDraftProject(null);
//			                project.setClientRM(poData.getClientRM());
//			                project.setPoEndDate(poData.getEndDate());
//			                project.setPoProjectType(poData.getProjectType());
//			                project.setPoStartDate(poData.getStartDate());
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
//			                e.printStackTrace();
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
//				    	Map<Boolean, Runnable> updateChecks = new HashMap<>();
//
//				    	
//				    	if (!Objects.equals(existingProject.getPoNo(), poPortalProjects.getPoNo())) {
//				    	    updateChecks.put(true, () -> existingProject.setPoNo(poPortalProjects.getPoNo()));
//				    	}
//				    	if (!Objects.equals(existingProject.getApmosysRM(), poPortalProjects.getApmosysRM())) {
//				    	    updateChecks.put(true, () -> existingProject.setApmosysRM(poPortalProjects.getApmosysRM()));
//				    	}
//				    	if (!Objects.equals(existingProject.getApmosysRmEmail(), poPortalProjects.getApmosysRmEmail())) {
//				    	    updateChecks.put(true, () -> existingProject.setApmosysRmEmail(poPortalProjects.getApmosysRmEmail()));
//				    	}
//				    	if (!Objects.equals(existingProject.getClientRM(), poPortalProjects.getClientRM())) {
//				    	    updateChecks.put(true, () -> existingProject.setClientRM(poPortalProjects.getClientRM()));
//				    	}
//				    	if (!Objects.equals(existingProject.getPoProjectType(), poPortalProjects.getProjectType())) {
//				    	    updateChecks.put(true, () -> existingProject.setPoProjectType(poPortalProjects.getProjectType()));
//				    	}
//				    	if (!Objects.equals(existingProject.getProjectName(), poPortalProjects.getName())) {
//				    	    updateChecks.put(true, () -> existingProject.setProjectName(poPortalProjects.getName()));
//				    	}
//				    	if (!Objects.equals(existingProject.getState(), poPortalProjects.getClientState())) {
//				    	    updateChecks.put(true, () -> existingProject.setState(poPortalProjects.getClientState()));
//				    	}
//				    	if (!Objects.equals(existingProject.getStatus(), poPortalProjects.getStatus())) {
//				    	    updateChecks.put(true, () -> existingProject.setStatus(poPortalProjects.getStatus()));
//				    	}
//
//
//				    	for (Map.Entry<Boolean, Runnable> entry : updateChecks.entrySet()) {
//				    	    if (entry.getKey()) {
//				    	        entry.getValue().run();
//				    	        isModified = true;
//				    	    }
//				    	}
//
//				    	String newActive = null;
//				    	if (poPortalProjects.getStatus() != null) {
//				    	    if (poPortalProjects.getStatus().equals("Pending")) {
//				    	        newActive = "true";
//				    	    } else if (poPortalProjects.getStatus().equals("Completed")) {
//				    	        newActive = "false";
//				    	    }
//				    	}
//
//				    	if (!Objects.equals(existingProject.getActive(), newActive)) {
//				    	    existingProject.setActive(newActive);
//				    	    isModified = true;
//				    	}
//
//
////				    	String endDate = convertIsoToDate(poPortalProjects.getEndDate());
//				    	if (!Objects.equals(existingProject.getPoEndDate(), poPortalProjects.getEndDate())) {
//				    	    existingProject.setPoEndDate(poPortalProjects.getEndDate());
//				    	    isModified = true;
//				    	}
//
////				    	String startDate = convertIsoToDate(poPortalProjects.getStartDate());
//				    	if (!Objects.equals(existingProject.getPoStartDate(), poPortalProjects.getStartDate())) {
//				    	    existingProject.setPoStartDate(poPortalProjects.getStartDate());
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
//				    	    long timestamp = Long.parseLong(createdInput);
//				    	    Instant instant = Instant.ofEpochMilli(timestamp);
//				    	    ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
//				    	    Timestamp parsedCreatedOn = Timestamp.valueOf(zdt.toLocalDateTime());
//				    	    
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
//				    	e.printStackTrace();
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
//	
	// public static String convertIsoToDate(String dateString) {
	// StringBuilder logBuilder = new StringBuilder();
	// try {
	// OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateString);
	// return offsetDateTime.toLocalDate().toString();
	// } catch (DateTimeParseException isoEx) {
	// try {
	// long epochMillis = Long.parseLong(dateString);
	// Instant instant = Instant.ofEpochMilli(epochMillis);
	// return instant.atZone(ZoneId.systemDefault()).toLocalDate().toString();
	// } catch (NumberFormatException | DateTimeException epochEx) {
	// logBuilder.append("Failed to parse date: ").append(dateString)
	// .append(" | Exception: ").append(epochEx.getClass().getSimpleName())
	// .append(" - ").append(epochEx.getMessage());
	// System.err.println(logBuilder.toString());
	// return null;
	// }
	// } catch (NullPointerException npe) {
	// logBuilder.append("Date string is null");
	// System.err.println(logBuilder.toString());
	// return null;
	// }
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
		Set<String> currentLocSet = existing.stream().map(ClientLocation::getClientLocation)
				.collect(Collectors.toSet());
		Set<String> newLocSet = new HashSet<>(Arrays.asList(locations));

		if (!currentLocSet.equals(newLocSet)) {
			clientLocationRepository.deleteAll(existing);
			List<ClientLocation> newLocs = newLocSet.stream().map(loc -> {
				ClientLocation cl = new ClientLocation();
				cl.setClientId(clientId);
				cl.setClientLocation(loc);
				return cl;
			}).collect(Collectors.toList());
			clientLocationRepository.saveAll(newLocs);
		}
	}

	private ServiceResponse syncProjectDepartments(Project project, ResourceManagementDTO dto) {

		if (dto.getDepartment() == null || dto.getDepartment().length == 0) {
			throw new DataNotFoundException("Department list cannot be empty for project update.");
		}
		
		List<ProjectPoDetails> projectPoDetailsList = projectPoDetailsRepository.findByProjectId(project.getProjectId());
	    if (projectPoDetailsList == null || projectPoDetailsList.isEmpty()) {
	        throw new DataNotFoundException("No PO found for Project ID: " + project.getProjectId());
	    }
	    List<Integer> activePoIds = projectPoDetailsList.stream()
                .filter(ProjectPoDetails::isActive)
                .map(po -> po.getPoId().intValue())
                .collect(Collectors.toList());
	    if (activePoIds.isEmpty()) {
	        ProjectPoDetails projectPoDetails = projectPoDetailsList.get(0);
	        if (projectPoDetails.getPoId() == null) {
	            throw new DataNotFoundException("PO ID is null for Project ID: " + project.getProjectId());
	        }
	        activePoIds.add(projectPoDetails.getPoId().intValue());
	    }
	    List<PoDepartmentMapping> existing = poDepartmentMappingRepository.findByProjectId(project.getProjectId());
	    Set<Long> existingIds = existing.stream()
	            .map(PoDepartmentMapping::getDeptId)
	            .distinct()
	            .collect(Collectors.toSet());
	    Set<Long> newIds = Arrays.stream(dto.getDepartment()).map(name -> {
	        Department dept = departmentRepository.findByName(name);
	        if (dept == null) {
	            throw new DataNotFoundException("Invalid department from PO: " + name);
	        }
	        return dept.getDeptId();
	    }).collect(Collectors.toSet());
	    
	    if (!existingIds.equals(newIds)) {
	        existing.forEach(pdm -> pdm.setActive(false));
	        poDepartmentMappingRepository.saveAll(existing);
	        List<PoDepartmentMapping> newMaps = new ArrayList<>();
	        for (Integer poId : activePoIds) {
	            for (Long deptId : newIds) {
	                PoDepartmentMapping pdm = new PoDepartmentMapping();
	                pdm.setDeptId(deptId);
	                pdm.setPoId(poId.longValue());
	                pdm.setActive(true);
	                newMaps.add(pdm);
	            }
	        }
	        poDepartmentMappingRepository.saveAll(newMaps);
	    }

	    String departmentIdsAsString = newIds.stream().map(String::valueOf).collect(Collectors.joining(","));
	    project.setDeptId(departmentIdsAsString);
	    projectRepository.save(project);
		ServiceResponse response = new ServiceResponse();
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse("Department mappings updated.");
		return response;
	}

	@Transactional(rollbackFor = Exception.class)
	private ServiceResponse syncResourceRequirements(Project project, ResourceManagementDTO dto) {
		ServiceResponse response = new ServiceResponse();
		if (project == null || dto == null) {
			throw new IllegalArgumentException("Project and ResourceManagementDTO cannot be null");
		}

		if (!"TNM".equalsIgnoreCase(project.getPoProjectType())) {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Sync skipped. Not a TNM project.");
			return response;
		}

		Map<Long, ResourceRequirementDTO> existingMap = resourceRequirementRepository
				.findByProjectId(project.getProjectId()).stream().filter(req -> req.getResourceOverviewId() != null)
				.collect(Collectors.toMap(ResourceRequirementDTO::getResourceOverviewId, Function.identity()));

		for (ResourceRequirementDTO r : dto.getResourceRequirements()) {
			Long id = r.getResourceOverviewId();
			if (id == null)
				continue;
			ResourceRequirementDTO existing = existingMap.get(id);

			boolean changed = existing == null || !Objects.equals(existing.getCount(), r.getCount())
					|| !Objects.equals(existing.getDepartment(), r.getDepartment())
					|| !Objects.equals(existing.getExperience(), r.getExperience())
					|| !Objects.equals(existing.getRole(), r.getRole());

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

		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse("Resource requirements synced.");
		return response;
	}

	private ServiceResponse syncLineItemsAndMilestones(Project project, ResourceManagementDTO dto) {
		ServiceResponse response = new ServiceResponse();
		if (dto.getFcLineItemDtoList() != null) {
			for (FCLineItemDTO lineItemDTO : dto.getFcLineItemDtoList()) {
				FCLineItem lineItem = Optional.ofNullable(lineItemDTO.getId()).flatMap(fCLineItemRepository::findById)
						.orElse(new FCLineItem());

				boolean changed = !Objects.equals(lineItem.getPoId(), lineItemDTO.getPoId())
						|| !Objects.equals(lineItem.getPoProjectId(), lineItemDTO.getProjectId())
						|| !Objects.equals(lineItem.getName(), lineItemDTO.getName())
						|| !Objects.equals(lineItem.getStatus(), lineItemDTO.getStatus());

				if (changed) {
					lineItem.setPoId(lineItemDTO.getPoId());
					lineItem.setProjectId(Long.parseLong(project.getProjectId().toString()));
					lineItem.setPoProjectId(lineItemDTO.getProjectId());
					lineItem.setName(lineItemDTO.getName());
					lineItem.setStatus(lineItemDTO.getStatus());
					fCLineItemRepository.save(lineItem);
				}

				for (FCProjectMilestoneDTO milestoneDTO : lineItemDTO.getMilestones()) {
					FCProjectMilestone milestone = Optional.ofNullable(milestoneDTO.getId())
							.flatMap(fCProjectMilestoneRepository::findById).orElse(new FCProjectMilestone());

					boolean msChanged = !Objects.equals(milestone.getName(), milestoneDTO.getName())
							|| !Objects.equals(milestone.getDescription(), milestoneDTO.getDescription())
							|| !Objects.equals(milestone.getStartDate(), milestoneDTO.getStartDate())
							|| !Objects.equals(milestone.getEndDate(), milestoneDTO.getEndDate())
							|| !Objects.equals(milestone.getStatus(), milestoneDTO.getStatus())
							|| !Objects.equals(milestone.getRemarks(), milestoneDTO.getRemarks());

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
		} else {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Line items and milestones not  synced.");
			return response;

		}

		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse("Line items and milestones synced.");
		return response;
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse createDraftProjectInfo(ResourceManagementDTO dto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO log = initializeLog(dto);
		StringBuilder logBuilder = new StringBuilder();

		try {
			logBuilder.append("ProjectType: ").append(dto.getProjectType()).append(", ProjectId: ")
					.append(dto.getProjectId()).append(", ProjectName: ").append(dto.getName()).append(", Department: ")
					.append(dto.getDeptName()).append(", State: ").append(dto.getClientState());

			Project existingProject = new Project();

			if (dto.getProjectId() != null || dto.getProjectType() != null || dto.getId() != null) {
				existingProject = fetchExistingProject(dto);
			}

			if (existingProject != null) {
				return handleExistingProject(existingProject, dto, log);
			}

			Integer clientId = resolveOrCreateClient(dto, response, log);
			if (clientId == null)
				return response;

			Project newProject = createAndSaveNewProject(dto, clientId);
//	        updateProjectTempStatus(dto);

			mapDepartmentsToProject(dto, newProject);
			handleProjectManagersAndOverheads(dto, newProject, response);
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

	// HELPER METHODS ///////////////////////////////////////////////////////////

	private LogDTO initializeLog(ResourceManagementDTO dto) {
		LogDTO log = new LogDTO();
		log.setSubFeatureName("createDraftProjectInfo");
		log.setApiUrl("/api/createDraftProjectInfo");
		log.setLogLevel("INFO");
		return log;
	}

	private Project fetchExistingProject(ResourceManagementDTO dto) {
		return "Internal".equalsIgnoreCase(dto.getProjectType()) ? projectRepository.findByProjectId(dto.getProjectId())
				: projectRepository.findByPoProjectId(dto.getId());
	}
//	private Project fetchExistingProject(ResourceManagementDTO dto) {
//	    if (dto.getInternalProjectType() != null && !dto.getInternalProjectType().trim().isEmpty()) {
//	        return projectRepository.findByProjectId(dto.getProjectId());
//	    } else if (dto.getId() != null) {
//	        return projectRepository.findByPoProjectId(dto.getId());
//	    } else {
//	        throw new IllegalArgumentException("Project ID is missing for non-internal project");
//	    }
//	}

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
		if (optionalClient.isPresent())
			return optionalClient.get().getClientId();

		Client newClient = new Client();
		newClient.setClientName(dto.getClientName());
		Client savedClient = clientsRepository.save(newClient);
		if (savedClient == null) {
			failResponse(response, log, "Failed to add client");
			return null;
		}

		List<ClientLocation> locations = Arrays.stream(dto.getClientLocation())
				.map(loc -> new ClientLocation(savedClient.getClientId(), loc)).collect(Collectors.toList());

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
		p.setStartDate(dto.getProjectStartDate());
		p.setEndDate(dto.getProjectEndDate());
		p.setCreatedOn(new Timestamp(System.currentTimeMillis()));
		p.setApmosysRM(dto.getApmosysRM());
		p.setIsRenewable(dto.getIsRenewable());
		p.setClientRM(dto.getClientRM());
		p.setApmosysRmEmail(dto.getApmosysRmEmail());
		p.setIsDraftProject("true");
		p.setCreatedBy(dto.getCreatedBy());
		p.setProjectStatus("Not Started");
		String[] departments = dto.getDepartment();

		if (departments != null) {
			String combinedDepartments = String.join(", ", departments);
			p.setDepartmentName(combinedDepartments);
		}
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
		
		 List<ProjectPoDetails> projectPoDetailsList = projectPoDetailsRepository.findByProjectId(project.getProjectId());
		    
		    if (projectPoDetailsList == null || projectPoDetailsList.isEmpty()) {
		        System.out.println("No PO found for project: " + project.getProjectId());
		        return;
		    }
		    List<Integer> activePoIds = projectPoDetailsList.stream()
	                .filter(ProjectPoDetails::isActive)
	                .map(po -> po.getPoId().intValue())
	                .collect(Collectors.toList());
		    
		    if (activePoIds.isEmpty()) {
		        activePoIds.add(projectPoDetailsList.get(0).getPoId().intValue());
		    }
		    
		    for (String deptName : dto.getDepartment()) {
		        Department dept = departmentRepository.findByName(deptName);
		        if (dept != null) {
		            List<PoDepartmentMapping> existingMappings = poDepartmentMappingRepository
		                    .findByProjectIdAndDeptId(project.getProjectId(), dept.getDeptId());
		            
		            if (existingMappings == null || existingMappings.isEmpty()) {
		                for (Integer poId : activePoIds) {
		                    PoDepartmentMapping map = new PoDepartmentMapping();
		                    map.setPoId(poId.longValue());
		                    map.setDeptId(dept.getDeptId());
		                    map.setActive(true); 
		                    poDepartmentMappingRepository.save(map);
		                }
		            }
		        }
		    }
	}

	private void handleProjectManagersAndOverheads(ResourceManagementDTO dto, Project project,
			ServiceResponse response) {
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
					ServiceResponse defaultResp = this.handleDefaultProjectUpdate(defaultEmpIds, project.getProjectId(),
							dto.getCreatedBy(), new StringBuilder());
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

			// set poRequirementMappingId
			if (member.getPoRequirementMappingId() != null) {
				map.setPoRequirementMappingId(member.getPoRequirementMappingId());
			}

			mappings.add(map);
		}

		return employeeTeamMapRepository.saveAll(mappings);
	}

	private List<Long> getDefaultProjectEmpIds(List<EmployeeTeamMap> mappings) {
		return mappings.stream().map(EmployeeTeamMap::getEmpId).filter(Objects::nonNull).distinct()
				.collect(Collectors.toList());
	}

	private void sendTeamCreationEmail(Team team, List<EmployeeTeamMap> mappings) {
		StringBuilder emailBody = new StringBuilder();
		emailBody.append("<html><body>").append("Dear RMG,<br><br>")
				.append("The Team has been created with the team name - <b>").append(team.getTeamName())
				.append("</b><br><br>").append("<table border='1' style='border-collapse: collapse; width: 100%;'>")
				.append("<tr>").append("<th style='padding: 8px;'>Employment ID</th>")
				.append("<th style='padding: 8px;'>Employee Name</th>")
				.append("<th style='padding: 8px;'>Job Role</th>").append("<th style='padding: 8px;'>Department</th>")
				.append("<th style='padding: 8px;'>Start Date</th>").append("</tr>");

		for (EmployeeTeamMap mapping : mappings) {
			List<EmployeeDetailsForTeamMemberDTO> employeeDetails = employeeRepository
					.getEmployeeDetailsForTeam(mapping.getEmpId());
			for (EmployeeDetailsForTeamMemberDTO dto : employeeDetails) {
				String empIdPrefix = "A-";
				if ("true".equalsIgnoreCase(dto.getIsConsultant())) {
					empIdPrefix = "CS-";
				}

				emailBody.append("<tr>").append("<td>").append(empIdPrefix).append(dto.getEmployeementId())
						.append("</td>").append("<td>").append(dto.getEmployeeName()).append("</td>").append("<td>")
						.append(dto.getJobRoleName()).append("</td>").append("<td>").append(dto.getDeptName())
						.append("</td>").append("<td>").append(dto.getStartDate()).append("</td>").append("</tr>");
			}
		}

		emailBody.append("</table><br><br>").append("Sincerely,<br><b>Team RMG - ApMoSys Technologies</b>")
				.append("</body></html>");

		try {
			String ccMails = getAllHodMails(mappings);
			mailService.sendMailWithCC(ccMails, rmgMail, "Regarding Team Creation", emailBody.toString());
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	private Long getTeamLeadId(TeamDTO teamDTO) {
		return teamDTO.getTeamMemberList().stream().filter(m -> "true".equalsIgnoreCase(m.getIsTeamLead()))
				.map(TeamMemberDTO::getEmpId).findFirst().orElse(null);
	}

	private String getTeamLeadName(TeamDTO teamDTO) {
		return teamDTO.getTeamMemberList().stream().filter(m -> "true".equalsIgnoreCase(m.getIsTeamLead()))
				.map(TeamMemberDTO::getName).findFirst().orElse(null);
	}

	private String getAllHodMails(List<EmployeeTeamMap> mappings) {
		return mappings.stream().map(m -> employeeRepository.findHodMail(m.getEmpId())).filter(Objects::nonNull)
				.distinct().collect(Collectors.joining(","));
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse poCrudOperationsInIshine(ResourceManagementDTO poData) {
		ServiceResponse serviceResponse = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/poCrudOperationsInIshine");
		apiLogInfo.setLogLevel("INFO");
		ApiLog initialLog = null;
		String exceptionDetailsForLog = null;
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		initialLog = apiLogUtility.startLog(poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),
				"poCrudOperationsInIshine", "PoPortal", null, httpRequest);

		String sourceSystem = httpRequest.getRequestURI().toString();

		if (poData == null) {
			finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
			throw new BadRequestException("No data received from PoPortal");
		}

		try {
			if (poData.getId() == null) {
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				throw new BadRequestException("No Po Project ID received from PoPortal");
			}
			String requestType = poData.getRequestType();
			if ("create".equalsIgnoreCase(requestType)) {
				serviceResponse = context.getBean(getClass()).handleCreate(poData);
			} else if ("update".equalsIgnoreCase(requestType)) {
				serviceResponse = context.getBean(getClass()).handleUpdate(poData);
			} else if ("delete".equalsIgnoreCase(requestType)) {
				serviceResponse = context.getBean(getClass()).handleDelete(poData);
			} else {
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				throw new BadRequestException("Invalid request type: " + requestType);
			}
			finalHttpStatusCode = HttpStatus.OK.value();

		} catch (Exception e) {
			e.printStackTrace();
			exceptionDetailsForLog = e.toString();
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceResponse(e);
			serviceResponse.setServiceError(e);
			throw e;
//			serviceResponse.setServiceMessage(e.getMessage());

		} finally {
			if (initialLog != null) {
				apiLogUtility.endLog(initialLog.getId(), sourceSystem, finalHttpStatusCode, exceptionDetailsForLog,
						httpRequest);
			}
		}
		return serviceResponse;
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse handleCreate(ResourceManagementDTO poData) {
		ServiceResponse response = new ServiceResponse();
		try {

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

			poData.setStartDate(Optional.ofNullable(poData.getStartDate()).filter(s -> !s.isEmpty())
					.map(Long::parseLong).map(Date::new).map(dateFormat::format).orElse(null));

			poData.setEndDate(Optional.ofNullable(poData.getEndDate()).filter(s -> !s.isEmpty()).map(Long::parseLong)
					.map(Date::new).map(dateFormat::format).orElse(null));

			boolean is_dept = true;
			if (projectRepository.findByPoProjectId(poData.getId()) != null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project is already created in ishine!");
				response.setStatusCode(HttpStatus.CONFLICT.value());
				return response;
			}

			Project project = createProjectEntity(poData);
			Integer clientId = createOrUpdateClient(poData, response);
			if (clientId == null)
				return response;

			project.setClientId(clientId);
			project.setClientLocation(
					String.join(", ", Optional.ofNullable(poData.getClientLocation()).orElse(new String[] {})));

			if ("TNM".equalsIgnoreCase(project.getPoProjectType())) {
				project.setHasClientSideId(true);
			}

			for (String deptName : poData.getDepartment()) {
				Department dept = departmentRepository.findByName(deptName);
				if (dept == null) {
					is_dept = false;
				} else {
					is_dept = true;
				}
			}
			if (is_dept) {
				Set<Long> newIds = Arrays.stream(poData.getDepartment())
						.map(name -> departmentRepository.findByName(name)).filter(Objects::nonNull)
						.map(Department::getDeptId).collect(Collectors.toSet());
				String departmentIdsAsString = newIds.stream().map(String::valueOf).collect(Collectors.joining(","));

				project.setDeptId(departmentIdsAsString);
				Project savedProject = projectRepository.save(project);

//			if ("TNM".equalsIgnoreCase(project.getPoProjectType())) {
//				syncResourceRequirementsTNM(savedProject, poData);
//			}
				syncDepartments(savedProject, poData);

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Project created successfully!");

			}
			if (!is_dept) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project can not be created!  Invalid Department ");
				return response;
			}
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(e.getMessage());
			return response;

		}
	}

	private Project createProjectEntity(ResourceManagementDTO poData) {
		Project project = new Project();
		if (poData.getId() == null)
			throw new DataNotFoundException("Po Project Id Is Not Provided");
		project.setPoProjectId(poData.getId());
		if (poData.getPoNo() == null)
			throw new DataNotFoundException("Po No. Is Not Provided");
		project.setPoNo(poData.getPoNo());
		project.setApmosysRM(poData.getApmosysRM());
		project.setApmosysRmEmail(poData.getApmosysRmEmail());
		if (poData.getStatus() == null)
			throw new DataNotFoundException("Status Is Not Provided");
		project.setActive("Completed".equals(poData.getStatus()) ? null : "true");
		project.setClientRM(poData.getClientRM());
		if (poData.getEndDate() == null)
			throw new DataNotFoundException("End Date Is Not Provided");
		project.setEndDate(poData.getEndDate());
		if (poData.getStartDate() == null)
			throw new DataNotFoundException("Start Date Is Not Provided");
		project.setStartDate(poData.getStartDate());
		if (poData.getProjectType() == null)
			throw new DataNotFoundException("Po Project Type Is Not Provided");
		project.setPoProjectType(poData.getProjectType());
		project.setProjectName(poData.getName());
		project.setState(poData.getClientState());
		project.setStatus(poData.getStatus());

		project.setProjectStatus("Not Started");

		if (poData.getIsRenewable() == null)
			throw new DataNotFoundException("Po Project Renewable Type Is Not Provided");
		project.setIsRenewable(poData.getIsRenewable());
		project.setIsDraftProject(null);
		project.setCreatedBy(6l);
		if (poData.getCreatedOn() != null) {
			try {
				long epochMillis = Long.parseLong(poData.getCreatedOn());
				LocalDateTime createdOn = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault())
						.toLocalDateTime();
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

	@Transactional(rollbackFor = Exception.class)
	private ServiceResponse syncResourceRequirementsTNM(Project project, ResourceManagementDTO poData) {
		ServiceResponse response = new ServiceResponse();
		List<ResourceRequirementDTO> requirementDTOs = poData.getResourceRequirements();
		if (requirementDTOs != null && !requirementDTOs.isEmpty()) {
			for (ResourceRequirementDTO dto : requirementDTOs) {
				if ((dto.getResourceOverviewId() == resourceRequirementRepository
						.existsByResourceOverviewId(Long.parseLong(dto.getResourceOverviewId().toString())))) {
					System.out.println("dto.getResourceOverviewId()" + dto.getResourceOverviewId());
					throw new BadCredentialsException(
							"Resource Overview Id Already Exists:" + dto.getResourceOverviewId());
				}
				if (dto == null) {
					throw new IllegalArgumentException("ResourceRequirementDTO cannot be null");
				}
				if (dto.getResourceOverviewId() == null) {
					throw new IllegalArgumentException("Resource Overview Id cannot be null");
				}
				if (dto.getCount() == null || dto.getCount() <= 0) {
					throw new IllegalArgumentException("Resource count must be greater than 0");
				}
				if (dto.getDepartment() == null || dto.getDepartment().trim().isEmpty()) {
					throw new IllegalArgumentException("Department cannot be null or empty");
				}
				if (dto.getRole() == null || dto.getRole().trim().isEmpty()) {
					throw new IllegalArgumentException("Role cannot be null or empty");
				}
				if (dto.getExperience() == null) {
					throw new IllegalArgumentException("Experience cannot be null");
				}
				if (project.getProjectId() == null) {
					throw new IllegalArgumentException("ProjectID cannot be null");

				}
				ResourceRequirement req = new ResourceRequirement();
				req.setProjectId(project.getProjectId());
				req.setCount(dto.getCount());
				req.setDepartment(dto.getDepartment());
				req.setExperience(dto.getExperience());
				req.setRole(dto.getRole());
				req.setResourceOverviewId(
						dto.getResourceOverviewId() != null ? Long.parseLong(dto.getResourceOverviewId().toString())
								: null);
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

	@Transactional(rollbackFor = Exception.class)
	private ServiceResponse syncDepartments(Project project, ResourceManagementDTO poData) {
		ServiceResponse response = new ServiceResponse();
		try {
			 List<ProjectPoDetails> projectPoDetailsList = projectPoDetailsRepository.findByProjectId(project.getProjectId());
		        
		        if (projectPoDetailsList == null || projectPoDetailsList.isEmpty()) {
		            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		            response.setServiceResponse("No PO found for project: " + project.getProjectId());
		            throw new RuntimeException("No PO found for project: " + project.getProjectId());
		        }
		        List<Integer> activePoIds = projectPoDetailsList.stream()
		                .filter(ProjectPoDetails::isActive)
		                .map(po -> po.getPoId().intValue())
		                .collect(Collectors.toList());
		        
		        if (activePoIds.isEmpty()) {
		            ProjectPoDetails projectPoDetails = projectPoDetailsList.get(0);
		            if (projectPoDetails == null || projectPoDetails.getPoId() == null) {
		                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		                response.setServiceResponse("Invalid PO details for project: " + project.getProjectId());
		                throw new RuntimeException("Invalid PO details for project: " + project.getProjectId());
		            }
		            activePoIds.add(projectPoDetails.getPoId().intValue());
		        }
		        
		        for (String deptName : poData.getDepartment()) {
		            Department dept = departmentRepository.findByName(deptName);
		            if (dept == null) {
		                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		                response.setServiceResponse("Create operation failed at saving departments!");
		                throw new RuntimeException("Create operation failed at saving departments!");
		            }
		            for (Integer poId : activePoIds) {
		                PoDepartmentMapping pdm = new PoDepartmentMapping();
		                pdm.setPoId(poId.longValue());
		                pdm.setDeptId(dept.getDeptId());
		                pdm.setActive(true);
		                PoDepartmentMapping saved = poDepartmentMappingRepository.save(pdm);
		                if (saved == null) {
		                    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		                    response.setServiceResponse("Failed to map department: " + deptName + " to PO: " + poId);
		                    throw new RuntimeException("Failed to map department: " + deptName + " to PO: " + poId);
		                }
		            }
		        }
		        
		        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		        response.setServiceResponse("Departments mapped successfully.");

		} catch (Exception e) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(ExceptionUtils.getExceptionMessage(e));
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
		return response;
	}

	public ServiceResponse handleUpdate(ResourceManagementDTO poData) {
		ServiceResponse response = new ServiceResponse();

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		poData.setStartDate(Optional.ofNullable(poData.getStartDate()).filter(s -> !s.isEmpty()).map(Long::parseLong)
				.map(Date::new).map(dateFormat::format).orElse(null));

		poData.setEndDate(Optional.ofNullable(poData.getEndDate()).filter(s -> !s.isEmpty()).map(Long::parseLong)
				.map(Date::new).map(dateFormat::format).orElse(null));

		boolean isModified = false;
		if (poData.getId() == null)
			throw new DataNotFoundException("Po Project ID cannot be null");
		Project existingProject = projectRepository.findByPoProjectId(poData.getId());

		if (existingProject == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("No project found with PoProjectId: " + poData.getId());
			return response;
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
			response.setServiceResponse("Failed to Sync Departments while updating project.");
			return response;
		}

//		ServiceResponse reqResp = syncResourceRequirements(existingProject, poData);
//		if (ServiceResponse.STATUS_FAIL.equals(reqResp.getServiceStatus())) {
//			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//			response.setServiceResponse("Failed to Sync Resource Requirements while updating project.");
//			return response;
//		}

		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse("Project updated successfully.");
		return response;
	}

	@Transactional(rollbackFor = Exception.class)
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
			if (poPortalProjects.getPoProjectType().equalsIgnoreCase("TNM")) {
				existingProject.setHasClientSideId(true);
			}
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

		if (!Objects.equals(existingProject.getClientName(), poPortalProjects.getClientName())) {
			existingProject.setClientName(poPortalProjects.getClientName());
			isModified = true;
		}
		String newActive = (poPortalProjects.getStatus() != null && poPortalProjects.getStatus().equals("Completed")
				? null
				: "true");
		if (!Objects.equals(existingProject.getActive(), newActive)) {
			existingProject.setActive(newActive);
			isModified = true;
		}

//		String startDate = convertIsoToDate(poPortalProjects.getStartDate());
		if (!Objects.equals(existingProject.getStartDate(), poPortalProjects.getStartDate())) {
			existingProject.setStartDate(poPortalProjects.getStartDate());
			isModified = true;
		}

//		String endDate = convertIsoToDate(poPortalProjects.getEndDate());
		if (!Objects.equals(existingProject.getEndDate(), poPortalProjects.getEndDate())) {
			existingProject.setEndDate(poPortalProjects.getEndDate());
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

	private List<String> parseCsvString(String csvString) {
		if (csvString == null || csvString.trim().isEmpty() || "()".equals(csvString.trim())) {
			return Collections.emptyList();
		}
		// Remove parentheses and split by comma, trimming whitespace from each element
		return Arrays.stream(csvString.replace("(", "").replace(")", "").split(",")).map(String::trim)
				.filter(s -> !s.isEmpty()) // Ensure empty elements are not included
				.collect(Collectors.toList());
	}

	public ServiceResponse handleDelete(ResourceManagementDTO poData) {
		ServiceResponse response = new ServiceResponse();
		Project existingProject = projectRepository.findByPoProjectId(poData.getId());

		if (existingProject == null) {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Project not found in Ishine Portal for PoProjectID: " + poData.getId());
			return response; // Use the list to fetch the relevant employees
		}

		List<Team> team = teamRepository.findTeamandIsActive(poData.getId());
		if (!team.isEmpty()) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Team is active in Ishine Portal for PoProjectID: " + poData.getId());
			return response;
		}
		existingProject.setActive("false");
		existingProject.setUpdatedBy(6L);
		existingProject.setUpdatedOn(LocalDateTime.now());
		projectRepository.save(existingProject);

		// Deactivate ProjectManagerMappings
		List<ProjectManagerMapping> activePMs = projectManagerMappingRepository.findByProjectIdAndActive(poData.getId(),
				1);
		for (ProjectManagerMapping pm : activePMs) {
			pm.setActive(0);
			pm.setUpdatedBy(6L);
			pm.setUpdatedOn(LocalDateTime.now());
		}
		projectManagerMappingRepository.saveAll(activePMs);

		List<ProjectOverheadMapping> activeOverheads = projectOverheadMappingRepository
				.findByProjectIdAndActive(Long.parseLong(existingProject.getProjectId().toString()), 1);
		for (ProjectOverheadMapping oh : activeOverheads) {
			oh.setActive(0);
			oh.setUpdatedBy(6l);
			oh.setUpdatedOn(LocalDateTime.now());
		}

		projectOverheadMappingRepository.saveAll(activeOverheads);

		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		response.setServiceResponse("Project deactivated successfully.");
		return response;
	}

//public ServiceResponse getProjectStructure(ProjectStructureRequest projectStructure,ProjectFilterDTO projectFilter)
//	{
//		ServiceResponse response = new ServiceResponse();
//		try {
//			
//			List<Object[]> fetchStructure = new ArrayList<>();
//			 if (projectFilter != null && projectFilter.getDepartmentsids() != null && !projectFilter.getDepartmentsids().isEmpty() && projectStructure.getDeptName() == null) {
//		          	
//				 String baseQuery = "SELECT DISTINCT client_name, project_name, po_project_type, dept_abbreviation, dept_name, dept_ids \n"
//				            + "FROM (\n"
//				            + "    SELECT DISTINCT \n"
//				            + "        p.project_id, \n"
//				            + "        c.client_name, \n"
//				            + "        p.project_name, \n"
//				            + "        p.po_project_type,  \n"
//				            + "        GROUP_CONCAT(DISTINCT d.dept_id ORDER BY d.dept_id SEPARATOR ',') AS dept_ids, \n"
//				            + "        GROUP_CONCAT(DISTINCT d.dept_abbreviation ORDER BY d.dept_id SEPARATOR ', ') AS dept_abbreviation, \n"
//				            + "        GROUP_CONCAT(DISTINCT d.name ORDER BY d.dept_id SEPARATOR ', ') AS dept_name \n"
//				            + "    FROM projects p \n"
//				            + "    INNER JOIN clients c ON c.client_id = p.client_id \n"
//				            + "    INNER JOIN project_department_map pdm ON pdm.project_id = p.project_id\n"
//				            + "    INNER JOIN teams t ON t.project_id = p.project_id\n"
//				            + "    INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id \n"
//				            + "    INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
//				            + "    INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id\n"
//				            + "    INNER JOIN department d ON d.dept_id = jr.dept_id\n"
//				            + "    WHERE etm.active != 0 \n"
//				            + "      AND t.is_active != 'N' \n"
//				            + "      AND p.active != 'false' \n"
//				            + "      AND e.employmentstatus != 'InActive' \n"
//				            + "    GROUP BY p.project_id, c.client_name, p.project_name, p.po_project_type  \n"
//				            + ") AS dept_list ";
//
//				 StringBuilder whereClause = new StringBuilder("WHERE 1=1");
//				 
//				 String groupByClause = " GROUP BY dept_ids, dept_abbreviation, client_name, project_name, po_project_type";
//		
//				 
//			        if (projectFilter != null && projectFilter.getDepartmentsids() != null && !projectFilter.getDepartmentsids().isEmpty() && "All".equals(projectStructure.getType().toString())) {
//			          
//			        	 whereClause.append(" AND ("); 
//			             
//			            
//			             String orConditions = projectFilter.getDepartmentsids().stream()
//			                 .map(deptId -> "FIND_IN_SET(" + deptId + ", dept_ids) > 0")
//			                 .collect(Collectors.joining(" OR "));
//			             
//			             whereClause.append(orConditions);
//			             whereClause.append(")"); 
//			        	
//			        	 String finalSql = baseQuery + whereClause.toString() + groupByClause;
//
//			           
//			             Query query = entityManager.createNativeQuery(finalSql);
//			             fetchStructure = query.getResultList();
//			             
//			        } 
//			        else if(projectFilter != null && projectFilter.getDepartmentsids() != null && !projectFilter.getDepartmentsids().isEmpty() && !"All".equals(projectStructure.getType().toString()))
//			        		{
//			        	whereClause.append(" AND ("); 
//			             
//			            
//			             String orConditions = projectFilter.getDepartmentsids().stream()
//			                 .map(deptId -> "FIND_IN_SET(" + deptId + ", dept_ids) > 0")
//			                 .collect(Collectors.joining(" OR "));
//			             
//			             whereClause.append(orConditions);
//			             whereClause.append(")"); 
//			             
//			             whereClause.append(" AND po_project_type = '").append(projectStructure.getType().toString().replace("'", "''")).append("' ");
//			        	
//			        	 String finalSql = baseQuery + whereClause.toString() + groupByClause;
//
//			           
//			             Query query = entityManager.createNativeQuery(finalSql);
//			             fetchStructure = query.getResultList();
//			        		}
//		        }
//			 
//			
//		else {
//			if("All".equals(projectStructure.getType().toString()) && projectFilter.getDepartmentsids().isEmpty()) {
//				if(projectStructure.getDeptName()!=null ) {	
//				String dept = projectStructure.getDeptName()[0].toString();
//				fetchStructure = resourceRequirementRepository.getListAllProjectStructure(dept);}
//				else
//				{
//					fetchStructure = resourceRequirementRepository.getAllStructure();
//				}
//			}
//			else {
//				if(projectStructure.getDeptName()==null) {
//					fetchStructure = resourceRequirementRepository.getAllProjectStructure( projectStructure.getType());
//				}
//				else {
//				String dept = projectStructure.getDeptName()[0].toString();
//				fetchStructure = resourceRequirementRepository.getListProjectStructure(dept, projectStructure.getType());
//				}
//			}
//			 }
//			List<ProjectStructureResponse> result = fetchStructure.stream().map(ProjectStructureResponse::new).collect(Collectors.toList());
//			response.setServiceResponse(result);
//			response.setServiceMessage("Structure Fetched Successfully .. ");
//			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//			return response;
//		}catch(Exception e)
//		{
//			e.printStackTrace();
//			response.setServiceMessage("Error fetching Project Structure..");
//			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//			return response;
//		}
//	}

	public ServiceResponse getProjectStructure(ProjectStructureRequest projectStructure,
			ProjectFilterDTO projectFilter) {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> fetchStructure = new ArrayList<>();

			if (projectStructure.getDeptName() != null && projectStructure.getDeptName().length > 0) {
				String dept = projectStructure.getDeptName()[0].toString();

				if ("All".equals(projectStructure.getType().toString())) {
					fetchStructure = resourceRequirementRepository.getListAllProjectStructure(dept);
				} else {
					fetchStructure = resourceRequirementRepository.getListProjectStructure(dept,
							projectStructure.getType());
				}
			} else if (projectFilter != null && projectFilter.getDepartmentsids() != null
					&& !projectFilter.getDepartmentsids().isEmpty()) {

				String baseQuery = "SELECT DISTINCT client_name, project_name, po_project_type, dept_abbreviation, dept_name, dept_ids \n"
						+ "FROM (\n" + "    SELECT DISTINCT \n" + "        p.project_id, \n"
						+ "        c.client_name, \n" + "        p.project_name, \n" + "        p.po_project_type,  \n"
						+ "        GROUP_CONCAT(DISTINCT d.dept_id ORDER BY d.dept_id SEPARATOR ',') AS dept_ids, \n"
						+ "        GROUP_CONCAT(DISTINCT d.dept_abbreviation ORDER BY d.dept_id SEPARATOR ', ') AS dept_abbreviation, \n"
						+ "        GROUP_CONCAT(DISTINCT d.name ORDER BY d.dept_id SEPARATOR ', ') AS dept_name \n"
						+ "    FROM projects p \n" + "    INNER JOIN clients c ON c.client_id = p.client_id \n"
						+ "    INNER JOIN project_department_map pdm ON pdm.project_id = p.project_id\n"
						+ "    INNER JOIN teams t ON t.project_id = p.project_id\n"
						+ "    INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id \n"
						+ "    INNER JOIN employee e ON e.emp_id = etm.emp_id\n"
						+ "    INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id\n"
						+ "    INNER JOIN department d ON d.dept_id = jr.dept_id\n" + "    WHERE etm.active != 0 \n"
						+ "      AND t.is_active != 'N' \n" + "      AND p.active != 'false' \n"
						+ "      AND e.employmentstatus != 'InActive' \n"
						+ "    GROUP BY p.project_id, c.client_name, p.project_name, p.po_project_type  \n"
						+ ") AS dept_list ";

				StringBuilder whereClause = new StringBuilder("WHERE 1=1");
				String groupByClause = " GROUP BY dept_ids, dept_abbreviation, client_name, project_name, po_project_type";

				if ("All".equals(projectStructure.getType().toString())) {
					whereClause.append(" AND (");

					String orConditions = projectFilter.getDepartmentsids().stream()
							.map(deptId -> "FIND_IN_SET(" + deptId + ", dept_ids) > 0")
							.collect(Collectors.joining(" OR "));

					whereClause.append(orConditions);
					whereClause.append(")");

					String finalSql = baseQuery + whereClause.toString() + groupByClause;
					Query query = entityManager.createNativeQuery(finalSql);
					fetchStructure = query.getResultList();

				} else {
					whereClause.append(" AND (");

					String orConditions = projectFilter.getDepartmentsids().stream()
							.map(deptId -> "FIND_IN_SET(" + deptId + ", dept_ids) > 0")
							.collect(Collectors.joining(" OR "));

					whereClause.append(orConditions);
					whereClause.append(")");

					whereClause.append(" AND po_project_type = '")
							.append(projectStructure.getType().toString().replace("'", "''")).append("' ");

					String finalSql = baseQuery + whereClause.toString() + groupByClause;
					Query query = entityManager.createNativeQuery(finalSql);
					fetchStructure = query.getResultList();
				}
			} else {
				if ("All".equals(projectStructure.getType().toString())) {
					fetchStructure = resourceRequirementRepository.getAllStructure();
				} else {
					fetchStructure = resourceRequirementRepository.getAllProjectStructure(projectStructure.getType());
				}
			}

			List<ProjectStructureResponse> result = fetchStructure.stream().map(ProjectStructureResponse::new)
					.collect(Collectors.toList());

			response.setServiceResponse(result);
			response.setServiceMessage("Structure Fetched Successfully .. ");
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			return response;

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceMessage("Error fetching Project Structure..");
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			return response;
		}
	}

	private void processPoPortalProject(ResourceManagementDTO poPortalProjects,
			List<ResourceManagementDTO> teamCreatedProjects) {

		Map<Long, ResourceManagementDTO> teamCreatedMap = teamCreatedProjects.stream()
				.filter(proj -> proj.getPoProjectId() != null)
				.collect(Collectors.toMap(ResourceManagementDTO::getPoProjectId, Function.identity(), (a, b) -> a));

		ResourceManagementDTO proj = poPortalProjects;
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

	public static String convertIsoToDate(String isoDateString) {
		StringBuilder logInfo = new StringBuilder();
		try {
			OffsetDateTime offsetDateTime = OffsetDateTime.parse(isoDateString);
			return offsetDateTime.toLocalDate().toString();
		} catch (DateTimeParseException | NullPointerException e) {

			logInfo.append("Failed to parse date: ").append(isoDateString).append(" | Exception: ")
					.append(e.getClass().getSimpleName()).append(" - ").append(e.getMessage());

			System.err.println(logInfo.toString());
		}
		return null;
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse deleteProjectTemp() {
		ServiceResponse response = new ServiceResponse();
		try {
			projectTempRepo.deleteAll();
			resourceRequirementTempRepo.deleteAll();
			try {
				dumpPODataInIshine();
//			Thread.sleep(5000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				projectRepository.callSyncProjectsSP();
//			Thread.sleep(5000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				fillDepartmentforAllProjectsInIshine();
//			Thread.sleep(5000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			response.setServiceStatus(response.STATUS_SUCCESS);
			System.out.println(response);
		} catch (Exception e) {
			response.setServiceStatus(response.STATUS_FAIL);
			response.setServiceResponse(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}

	public ServiceResponse getAllExpiredTNMProject() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getAllExpiredTNMProject");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder("Fetching all expired TNM projects");

		try {

			List<ExpiredPoDto> expiredProjects = projectRepository.getAllExpiredTNMProject();

			if (expiredProjects != null && !expiredProjects.isEmpty()) {
				response.setServiceResponse(expiredProjects);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo
						.setApiResponse("Expired TNM projects fetched successfully. Count: " + expiredProjects.size());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				logBuilder.append(". Total expired projects found: ").append(expiredProjects.size());
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No expired TNM projects found.");
				apiLogInfo.setApiResponse("No expired TNM projects found");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				logBuilder.append(". No expired projects found");
			}
		} catch (Exception e) {

			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong while fetching expired TNM projects.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			logBuilder.append(". Error occurred: ").append(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse updateHasClientSideId(UpdateHasClientSideIdDTO dto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/updateHasClientSideId");
		apiLogInfo.setLogLevel("INFO");

		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("flag : " + dto.getHasClientSideId() + "projectId: " + dto.getProjectId() + "\n");

		try {
			if (dto.getProjectId() != null) {
				Project project = projectRepository.getByProjectId(dto.getProjectId());

				if (project == null) {

					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Could not update the Client Side Id status!");
					response.setServiceMessage("No Project fetched for ProjectId: " + dto.getProjectId());
					apiLogInfo.setApiResponse("No Project fetched for ProjectId: " + dto.getProjectId());
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					logService.logMyInfo(httpRequest, apiLogInfo);

					return response;

				} else {

					project.setHasClientSideId(dto.getHasClientSideId());
					project.setClientFlag(dto.getClientFlag());
					project.setUpdatedBy(dto.getCurrentUserEmpId());
					project.setUpdatedOn(LocalDateTime.now());

					Project savedProject = projectRepository.save(project);

					if (savedProject == null) {

						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Failed to save client side id status!");
						response.setServiceMessage("Could not save client id status for the project with project id : "
								+ dto.getProjectId());
						apiLogInfo.setApiResponse("Could not save client id status for the project with project id : "
								+ dto.getProjectId());
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						logService.logMyInfo(httpRequest, apiLogInfo);
						return response;

					}
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Client Side Id status updated successfully !");
					response.setServiceMessage("Client Side Id status updated successfully !");
					logBuilder.append("Project id : " + savedProject.getProjectId() + "Client Side Id Stautus: "
							+ savedProject.getHasClientSideId() + "\n");

					apiLogInfo.setApiResponse("Client Side Id status updated successfully!");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Exception in lift and shift service", e);
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong.");
			response.setServiceError(e.getMessage());

			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setApiResponse(e.getMessage());
			apiLogInfo.setLogLevel("ERROR");
		}

		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	private LocalDateTime parseDateTime(Object obj) {
		if (obj == null)
			return null;
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
			return LocalDateTime.parse(obj.toString(), formatter);
		} catch (DateTimeParseException e) {
			return null;
		}
	}

	public ServiceResponse getActiveProjectList() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getActiveProjectList");
		apiLogInfo.setLogLevel("INFO");
		try {
			List<ProjectNameAndPrjoectIdDTO> projectList = projectRepository.getActiveProjectList();

			if (projectList == null || projectList.isEmpty()) {
				apiLogInfo.setLogLevel("FAIL");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiResponse("Empty list fetched from repository!");
				response.setServiceResponse("Unable to fetch project list!");
				logService.logMyInfo(httpRequest, apiLogInfo);
				return response;
			}

			response.setServiceResponse(projectList);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			apiLogInfo.setApiResponse("Project List fetched successfully !");
			apiLogInfo.setLogLevel("SUCCESS");
			logService.logMyInfo(httpRequest, apiLogInfo);
			return response;

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setApiResponse(e.getMessage());
			apiLogInfo.setLogLevel("ERROR");
		}
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse liftAndShiftTeams(LiftAndShiftTeamsDTO dto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/liftAndShiftTeams");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();

		try {
			// --- 0. basic validation ---
			if (dto == null || dto.getTeamIds() == null || dto.getTeamIds().isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No teamIds provided");
				return response;
			}
			if (dto.getTargetProjectId() == null || dto.getSourceProjectId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Source or Target project id missing");
				return response;
			}

			// --- 1. copy hasClientSideId from source -> target project early ---
			Project sourceProject = projectRepository.findByProjectId(dto.getSourceProjectId());
			Project targetProject = projectRepository.findByProjectId(dto.getTargetProjectId());
			if (sourceProject != null && targetProject != null) {
				targetProject.setHasClientSideId(sourceProject.getHasClientSideId());
				projectRepository.save(targetProject);
			}

			// --- 1.a project-level mappings copy (dept, manager, overhead) - avoid
			// duplicates ---
			// ProjectDepartmentMap
			List<ProjectPoDetails> sourcePoList = projectPoDetailsRepository.findByProjectId(dto.getSourceProjectId());
			List<ProjectPoDetails> targetPoList = projectPoDetailsRepository.findByProjectId(dto.getTargetProjectId());

           if (sourcePoList == null || sourcePoList.isEmpty()) {
                throw new RuntimeException("No PO found for source project: " + dto.getSourceProjectId());
            }

           if (targetPoList == null || targetPoList.isEmpty()) {
                throw new RuntimeException("No PO found for target project: " + dto.getTargetProjectId());
            }
            Integer poId = targetPoList.get(0).getPoId().intValue();
			
			
           List<PoDepartmentMapping> sourceDeptMappings = poDepartmentMappingRepository.findByProjectId(dto.getSourceProjectId());
           List<PoDepartmentMapping> targetDeptMappings = poDepartmentMappingRepository.findByProjectId(dto.getTargetProjectId());
           Set<Long> targetDeptIds = targetDeptMappings.stream()
                            .map(PoDepartmentMapping::getDeptId)
                            .collect(Collectors.toSet());

                     List<PoDepartmentMapping> newDeptMappings = new ArrayList<>();

                             for (PoDepartmentMapping s : sourceDeptMappings) {
                                      if (!targetDeptIds.contains(s.getDeptId())) {
                                  PoDepartmentMapping nm = new PoDepartmentMapping();
                                     nm.setPoId(poId.longValue()); 
                                     nm.setDeptId(s.getDeptId());
                                     nm.setActive(true); 
                                  newDeptMappings.add(nm);
                               }
                            }
                             if (!newDeptMappings.isEmpty()) {
                        poDepartmentMappingRepository.saveAll(newDeptMappings);
             }

			// ProjectManagerMapping
			List<ProjectManagerMapping> sourceManagerMappings = projectManagerMappingRepository
					.findByProjectIdAndActive(Long.parseLong(dto.getSourceProjectId().toString()), 1);
			List<ProjectManagerMapping> targetManagerMappings = projectManagerMappingRepository
					.findByProjectIdAndActive(Long.parseLong(dto.getTargetProjectId().toString()), 1);
			Set<Long> targetManagerIds = targetManagerMappings.stream().map(ProjectManagerMapping::getProjectManagerId)
					.collect(Collectors.toSet());
			List<ProjectManagerMapping> newManagerMappings = new ArrayList<>();
			for (ProjectManagerMapping s : sourceManagerMappings) {
				if (!targetManagerIds.contains(s.getProjectManagerId())) {
					ProjectManagerMapping nm = new ProjectManagerMapping();
					nm.setProjectId(Long.parseLong(dto.getTargetProjectId().toString()));
					nm.setProjectManagerId(s.getProjectManagerId());
					nm.setActive(1);
					nm.setCreatedBy(dto.getCurrentUserEmpId());
					nm.setCreatedOn(new Timestamp(System.currentTimeMillis()));
					newManagerMappings.add(nm);
				}
			}
			if (!newManagerMappings.isEmpty())
				projectManagerMappingRepository.saveAll(newManagerMappings);

			// ProjectOverheadMapping
			List<ProjectOverheadMapping> sourceOverheadMappings = projectOverheadMappingRepository
					.findByProjectIdAndActive(Long.parseLong(dto.getSourceProjectId().toString()), 1);
			List<ProjectOverheadMapping> targetOverheadMappings = projectOverheadMappingRepository
					.findByProjectIdAndActive(Long.parseLong(dto.getTargetProjectId().toString()), 1);
			Set<Long> targetOverheadIds = targetOverheadMappings.stream()
					.map(ProjectOverheadMapping::getProjectOverheadId).collect(Collectors.toSet());
			List<ProjectOverheadMapping> newOverheadMappings = new ArrayList<>();
			for (ProjectOverheadMapping s : sourceOverheadMappings) {
				if (!targetOverheadIds.contains(s.getProjectOverheadId())) {
					ProjectOverheadMapping nm = new ProjectOverheadMapping();
					nm.setProjectId(Long.parseLong(dto.getTargetProjectId().toString()));
					nm.setProjectOverheadId(s.getProjectOverheadId());
					nm.setActive(1);
					nm.setCreatedBy(dto.getCurrentUserEmpId());
					nm.setCreatedOn(new Timestamp(System.currentTimeMillis()));
					newOverheadMappings.add(nm);
				}
			}
			if (!newOverheadMappings.isEmpty())
				projectOverheadMappingRepository.saveAll(newOverheadMappings);

			// --- 2. fetch active teams (old) ---
			List<Team> activeTeams = teamRepository.findActiveTeamsByTeamIds(dto.getTeamIds());
			if (activeTeams.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("No Active teams found for this Project");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setApiResponse("Empty list fetched from repository!");
				apiLogInfo.setLogLevel("SUCCESS");
				logService.logMyInfo(httpRequest, apiLogInfo);
				return response;
			}

			// --- 3. PRE-FETCH employee-team mappings & activities for old teams
			// (important: do this before changing teams) ---
			// active employee-team mappings (these are the rows we will mark inactive and
			// then duplicate)
			List<EmployeeTeamMap> oldEmployeeTeamMaps = employeeTeamMapRepository
					.activeAndPendingEmployeesByTeamIds(dto.getTeamIds());
			// activities for old teams
			List<Activity> oldActivities = activitiesRepository.findByTeamIdIn(dto.getTeamIds());

			if (oldEmployeeTeamMaps != null || !oldEmployeeTeamMaps.isEmpty()) {
				// derive empIds from oldEmployeeTeamMaps
				List<Long> empIds = oldEmployeeTeamMaps.stream().map(EmployeeTeamMap::getEmpId).distinct()
						.collect(Collectors.toList());

				// --- 4. Mark old teams inactive (persist) ---
				LocalDateTime now = LocalDateTime.now();
				activeTeams.forEach(t -> {
					t.setIsActive("N");
					t.setUpdatedBy(dto.getCurrentUserEmpId());
					t.setUpdatedOn(now);
				});
				teamRepository.saveAll(activeTeams);

				// --- 5. Create new teams for target project (set oldTeamId transient) ---
				List<Team> newTeams = activeTeams.stream().map(oldTeam -> {
					Team newTeam = new Team();
					newTeam.setTeamName(oldTeam.getTeamName());
					newTeam.setTeamLeadId(oldTeam.getTeamLeadId());
					newTeam.setProjectId(dto.getTargetProjectId());
					newTeam.setTeamLeadName(oldTeam.getTeamLeadName());
					newTeam.setIsActive("Y");
					newTeam.setPoTeamId(oldTeam.getPoTeamId());
					newTeam.setDescription(oldTeam.getDescription());
					newTeam.setDeptIds(oldTeam.getDeptIds());
					newTeam.setSpocId(oldTeam.getSpocId());
					newTeam.setCreatedBy(dto.getCurrentUserEmpId());
					newTeam.setCreatedOn(Timestamp.valueOf(LocalDateTime.now()));
					newTeam.setOldTeamId(oldTeam.getTeamId()); // transient helper
					return newTeam;
				}).collect(Collectors.toList());

				List<Team> createdTeams = teamRepository.saveAll(newTeams);
				if (createdTeams.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Failed to shift teams to new project");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					apiLogInfo.setApiResponse("Failed to create new teams");
					apiLogInfo.setLogLevel("FAIL");
					logService.logMyInfo(httpRequest, apiLogInfo);
					return response;
				}

				// Build oldTeamId -> newTeam entity map
				Map<Long, Team> oldToNewTeamMap = createdTeams.stream().filter(t -> t.getOldTeamId() != null)
						.collect(Collectors.toMap(Team::getOldTeamId, Function.identity()));

				// --- 6. Employee–Team mappings update ---
				// Mark old mappings inactive (we already fetched oldEmployeeTeamMaps before)
				if (oldEmployeeTeamMaps != null || !oldEmployeeTeamMaps.isEmpty()) {
					oldEmployeeTeamMaps.forEach(etm -> {
						etm.setActive(0L);
						etm.setUpdatedOn(LocalDateTime.now());
						etm.setUpdatedBy(dto.getCurrentUserEmpId());
					});
				}
				employeeTeamMapRepository.saveAll(oldEmployeeTeamMaps);

				// Create new mappings using oldToNewTeamMap
				List<EmployeeTeamMap> newEmployeeTeamMaps = new ArrayList<>();
				for (EmployeeTeamMap oldMap : oldEmployeeTeamMaps) {
					Team mappedNewTeam = oldToNewTeamMap.get(oldMap.getTeamId());
					if (mappedNewTeam != null) {
						EmployeeTeamMap newMap = new EmployeeTeamMap();
						newMap.setEmpId(oldMap.getEmpId());
						newMap.setTeamId(mappedNewTeam.getTeamId());
						newMap.setJobRoleId(oldMap.getJobRoleId());
						newMap.setActive(1L);
						newMap.setStartDate(LocalDateTime.now());
						newMap.setEmployeeRole(oldMap.getEmployeeRole());
						newMap.setEndDate(null);
						newMap.setUpdatedOn(null);
						newMap.setUpdatedBy(null);

						// added storing poRequirementMapping Id
						if (oldMap.getPoRequirementMappingId() != null) {
							newMap.setPoRequirementMappingId(oldMap.getPoRequirementMappingId());
						}

						newMap.setIsShadow(oldMap.getIsShadow());
						newMap.setCreatedBy(dto.getCurrentUserEmpId());
						newMap.setCreatedOn(Timestamp.valueOf(LocalDateTime.now()));
						newEmployeeTeamMaps.add(newMap);
					}
				}
				if (!newEmployeeTeamMaps.isEmpty())
					employeeTeamMapRepository.saveAll(newEmployeeTeamMaps);

				if (empIds != null || !empIds.isEmpty()) {
					// --- 7. Employee primary project mapping update (deactivate old, create new)
					// ---
					List<EmpPrimaryProjectMapping> oldPrimaryMappings = empPrimaryProjectMappingRepository
							.findByEmpIdInAndPrimaryProjectIdInAndIsMapped(empIds,
									Collections.singletonList(dto.getSourceProjectId().longValue()), "Y");

					boolean primaryMappingsChanged = false;
					if (!oldPrimaryMappings.isEmpty()) {
						oldPrimaryMappings.forEach(m -> {
							m.setIsMapped("N");
							m.setUpdatedOn(LocalDateTime.now());
							m.setUpdatedBy(dto.getCurrentUserEmpId());
						});
						empPrimaryProjectMappingRepository.saveAll(oldPrimaryMappings);

						List<EmpPrimaryProjectMapping> newPrimaryMappings = new ArrayList<>();
						for (EmpPrimaryProjectMapping oldMap : oldPrimaryMappings) {
							EmpPrimaryProjectMapping newMap = new EmpPrimaryProjectMapping();
							newMap.setEmpId(oldMap.getEmpId());
							newMap.setPrimaryProjectId(dto.getTargetProjectId().longValue());
							newMap.setPrimaryProjectName(oldMap.getPrimaryProjectName());
							newMap.setIsMapped("Y");
							newMap.setUpdatedBy(dto.getCurrentUserEmpId());
							newMap.setUpdatedOn(LocalDateTime.now());
							newPrimaryMappings.add(newMap);
						}
						empPrimaryProjectMappingRepository.saveAll(newPrimaryMappings);
						primaryMappingsChanged = true;
					}

					// --- 8. Only after primary mappings changed, update billable/billableType on
					// employees ---
					if (primaryMappingsChanged) {
						Project refreshedTarget = projectRepository.findByProjectId(dto.getTargetProjectId());
						String poProjectType = (refreshedTarget != null ? refreshedTarget.getPoProjectType() : null);
						String ishineProjectType = (refreshedTarget != null ? refreshedTarget.getInternalProjectType()
								: null);

						String billable = null;
						String billableType = null;

						if (poProjectType != null) {
							switch (poProjectType) {
							case "Fixed Cost":
								billable = "No";
								billableType = "Fixed Cost";
								break;
							case "TNM":
								billable = "Yes";
								billableType = "TNM";
								break;
							case "Monitoring":
								billable = "No";
								billableType = "Fixed Cost";
								break;
							default:
								billable = null;
								billableType = null;
							}
						} else if (ishineProjectType != null) {
							switch (ishineProjectType) {
							case "InternalRNDProducts":
								billable = "No";
								billableType = "InternalRNDProducts";
								break;
							case "Bench":
								billable = "No";
								billableType = "Bench";
								break;
							default:
								billable = null;
								billableType = null;
							}
						}

						if (billable != null || billableType != null) {
							employeeRepository.updateBillableAndTypeForEmpIds(billable, billableType, empIds);
						}
					}
				}

				// --- 9. Employee client-side ID mappings ---
				List<EmployeeClientSideIdMapping> oldClientSideMappings = employeeClientSideIdMappingRepository
						.findByEmpIdInAndProjectIdInAndActive(empIds,
								Collections.singletonList(dto.getSourceProjectId().longValue()), true);

				if (!oldClientSideMappings.isEmpty()) {
					oldClientSideMappings.forEach(m -> {
						m.setActive(false);
						m.setUpdatedOn(LocalDateTime.now());
						m.setUpdatedBy(dto.getCurrentUserEmpId());
					});
					employeeClientSideIdMappingRepository.saveAll(oldClientSideMappings);

					List<EmployeeClientSideIdMapping> newClientSideMappings = new ArrayList<>();
					for (EmployeeClientSideIdMapping oldMap : oldClientSideMappings) {
						EmployeeClientSideIdMapping newMap = new EmployeeClientSideIdMapping();
						newMap.setClientSideId(oldMap.getClientSideId());
						newMap.setEmpId(oldMap.getEmpId());
						newMap.setProjectId(dto.getTargetProjectId().longValue());
						newMap.setActive(true);
						newMap.setCreatedBy(dto.getCurrentUserEmpId());
						newMap.setCreatedOn(LocalDateTime.now());
						newClientSideMappings.add(newMap);
					}
					employeeClientSideIdMappingRepository.saveAll(newClientSideMappings);
				}

				// --- 10. Activities: recreate for new teamIds (we fetched oldActivities
				// earlier) ---
				List<Activity> newActivities = new ArrayList<>();
				for (Activity oldAct : oldActivities) {
					Team newTeam = oldToNewTeamMap.get(oldAct.getTeamId());
					if (newTeam != null) {
						Activity newAct = new Activity();
						newAct.setTeamId(newTeam.getTeamId());
						newAct.setActivity(oldAct.getActivity());
						newAct.setEta(oldAct.getEta());
						newAct.setEmployeeRole(oldAct.getEmployeeRole());
						newAct.setDeptIds(oldAct.getDeptIds());
						CommonProperties cp = new CommonProperties();
						cp.setCreatedBy(dto.getCurrentUserEmpId());
						cp.setCreatedOn(Timestamp.valueOf(LocalDateTime.now()));
						newAct.setCommonProperty(cp);
						newActivities.add(newAct);
					}
				}

				if (!newActivities.isEmpty())
					activitiesRepository.saveAll(newActivities);
			}

			// done
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Lift and Shift completed successfully for " + activeTeams.size() + " teams.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			apiLogInfo.setApiResponse(
					"Lift & Shift done for teamIds=" + dto.getTeamIds() + " → " + dto.getTargetProjectId());
			apiLogInfo.setLogLevel("SUCCESS");

			try {

				Employee updatedByemp = employeeRepository.findByEmpId(dto.getCurrentUserEmpId());
				List<String> projManagerOverheadHODMails = projectRepository
						.findProjectManagerAndProjectoverheadEmails(targetProject.getProjectId());
				Set<String> toRecipients = new HashSet<>();
				toRecipients.add(bdMail);
				toRecipients.add(adminMail);
				toRecipients.add(rmgMail);
				toRecipients.add(financeMail);

				Set<String> ccRecipients = projManagerOverheadHODMails.stream().filter(Objects::nonNull)
						.map(String::trim).filter(s -> !s.isEmpty())
						.collect(Collectors.toCollection(LinkedHashSet::new));
				ccRecipients.removeAll(toRecipients);

				List<Team> teams = teamRepository.findAllById(dto.getTeamIds());
				List<String> teamNames = teams.stream().map(Team::getTeamName).filter(Objects::nonNull)
						.collect(Collectors.toList());

				String teamNamesStr = String.join(", ", teamNames);

				String subject = "Team Migration from " + sourceProject.getProjectName() + " to "
						+ targetProject.getProjectName();

				String currentDate = LocalDate.now().toString();

				String body;
				if (teamNames.size() == 1) {
					body = "The team <b>" + teamNamesStr + "</b> has been migrated from <b>"
							+ sourceProject.getProjectName() + "</b> to <b>" + targetProject.getProjectName()
							+ "</b> by <b>" + (updatedByemp != null ? updatedByemp.getName() : "System") + "</b> on "
							+ currentDate + ".";
				} else {
					body = "The teams <b>" + teamNamesStr + "</b> have been migrated from <b>"
							+ sourceProject.getProjectName() + "</b> to <b>" + targetProject.getProjectName()
							+ "</b> by <b>" + (updatedByemp != null ? updatedByemp.getName() : "System") + "</b> on "
							+ currentDate + ".";
				}

				mailService.sendMailWithCC(String.join(",", toRecipients), String.join(",", ccRecipients), subject,
						body);

			} catch (Exception mailEx) {
				logBuilder.append("\n Failed to send lift-and-shift mail: " + mailEx.getMessage());
			}

		} catch (NullPointerException ex) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Null value encountered.");
			response.setServiceError(ex.getMessage());
			apiLogInfo.setApiResponse("LiftAndShift failed" + ex.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			throw ex;
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setApiResponse("LiftAndShift failed" + e.getMessage());
			apiLogInfo.setLogLevel("ERROR");
			throw e;
		}

		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse fetchHasClientSideId(UpdateHasClientSideIdDTO dto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/updateHasClientSideId");
		apiLogInfo.setLogLevel("INFO");

		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("projectId: " + dto.getProjectId() + "\n");

		try {
			if (dto.getProjectId() != null) {
				Project project = projectRepository.getByProjectId(dto.getProjectId());
				UpdateHasClientSideIdDTO responseDTO = new UpdateHasClientSideIdDTO();

				if (project == null) {

					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Could not update the Client Side Id status!");
					response.setServiceMessage("No Project fetched for ProjectId: " + dto.getProjectId());
					apiLogInfo.setApiResponse("No Project fetched for ProjectId: " + dto.getProjectId());
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					logService.logMyInfo(httpRequest, apiLogInfo);

					return response;

				} else {

					responseDTO.setHasClientSideId(project.getHasClientSideId());
					responseDTO.setClientFlag(project.getClientFlag());
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(responseDTO);
					response.setServiceMessage("Fetched hasClientSideId status successfully !");
					logBuilder.append("Project id : " + project.getProjectId() + "\n");

					apiLogInfo.setApiResponse("Fetched hasClientSideId status successfully !");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong.");
			response.setServiceError(e.getMessage());

			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setApiResponse(e.getMessage());
			apiLogInfo.setLogLevel("ERROR");
		}

		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse sendTimesheetDetailsToShankh(TimeSheetRequestDto payloadDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/sendTimesheetDetailsToShankh");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();

		try {

			List<TimeSheetDetailsDto> timesheetDetails = projectRepository
					.findByProjectIdAndEmployeeIdAndWorkDateBetween(payloadDTO.getTeamId(), payloadDTO.getEmpId(),
							payloadDTO.getSt_Date(), payloadDTO.getEnd_Date());

			if (timesheetDetails != null && !timesheetDetails.isEmpty()) {
//	    		 timesheetDetails.forEach(timesheet ->{
//	 	    		List<TimesheetDocumentDetailsDTO> docData = timesheetDocumentDetailsRepository.findAllDocIdByTimesheetId(timesheet.getTimesheet_id());
//	 	    		if(docData != null && !docData.isEmpty()) {
//	 	    			timesheet.setDocData(docData);
//	 	    		}
//	 	    	 });
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(timesheetDetails);
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setLogLevel("Info");

			} else {

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("No timesheet details found!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setLogLevel("Info");

			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			throw e;
		}

		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse filterPoProjectsHavingTeam(List<Long> pIds) {
		ServiceResponse serviceResponse = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/filterPoProjectsHavingTeam");
		apiLogInfo.setLogLevel("INFO");
		ApiLog initialLog = null;
		String exceptionDetailsForLog = null;
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		initialLog = apiLogUtility.startLog(poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),
				"expiredPoNotificationCount", "PoPortal", null, httpRequest);

		String sourceSystem = httpRequest.getRequestURI().toString();

		try {

			List<Long> filteredProIds = new ArrayList<>();
			List<Long> allData = projectRepository.getAllTnmProjectsWithActiveTeams();
			filteredProIds = allData.stream().filter(pIds::contains).collect(Collectors.toList());

			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			serviceResponse.setServiceResponse(filteredProIds);
			finalHttpStatusCode = HttpStatus.OK.value();

		} catch (Exception e) {
			e.printStackTrace();
			log.error("error in filterPoProjectsHavingTeam" + e);
			exceptionDetailsForLog = e.toString();
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceResponse(e.getMessage());
			throw e;
//			serviceResponse.setServiceMessage(e.getMessage());

		} finally {
			if (initialLog != null) {
				apiLogUtility.endLog(initialLog.getId(), sourceSystem, finalHttpStatusCode, exceptionDetailsForLog,
						httpRequest);
			}
		}
		return serviceResponse;
	}

	public ServiceResponse getResourceCountFromProjectId(List<Long> pIds) {
		ServiceResponse serviceResponse = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getResourceCountFromProjectId");
		apiLogInfo.setLogLevel("INFO");
		ApiLog initialLog = null;
		String exceptionDetailsForLog = null;
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		initialLog = apiLogUtility.startLog(poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),
				"getResourceCountFromProjectId", "PoPortal", null, httpRequest);

		String sourceSystem = httpRequest.getRequestURI().toString();

		try {
			if (pIds == null || pIds.isEmpty()) {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("No project info (poProjectId) received at Ishine");
				finalHttpStatusCode = HttpStatus.NO_CONTENT.value();
				if (initialLog != null) {
					apiLogUtility.endLog(initialLog.getId(), sourceSystem, finalHttpStatusCode, exceptionDetailsForLog,
							httpRequest);
				}
				return serviceResponse;
			}
			List<ResourceCountDto> data = projectRepository.getResourceCounts(pIds);
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			serviceResponse.setServiceResponse(data);
			finalHttpStatusCode = HttpStatus.OK.value();

		} catch (Exception e) {
			e.printStackTrace();
			log.error("error in getResourceCountFromProjectId" + e);
			exceptionDetailsForLog = e.toString();
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceResponse(e.getMessage());
			throw e;
//			serviceResponse.setServiceMessage(e.getMessage());

		} finally {
			if (initialLog != null) {
				apiLogUtility.endLog(initialLog.getId(), sourceSystem, finalHttpStatusCode, exceptionDetailsForLog,
						httpRequest);
			}
		}
		return serviceResponse;
	}

	public ServiceResponse getDocumentDataByDocId(Long docId) {
		ServiceResponse serviceResponse = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getResourceCountFromProjectId");
		apiLogInfo.setLogLevel("INFO");
		ApiLog initialLog = null;
		String exceptionDetailsForLog = null;
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		initialLog = apiLogUtility.startLog(poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),
				"getResourceCountFromProjectId", "PoPortal", null, httpRequest);

		String sourceSystem = httpRequest.getRequestURI().toString();
		try {
			TimesheetDocumentDetails docDetails = new TimesheetDocumentDetails();
			docDetails = timesheetDocumentDetailsRepository.findByDocIdAndActive(docId, true);
			if (docDetails == null) {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("Document not found...!!");
				serviceResponse.setServiceMessage("Document not found...!!" + docId);

				apiLogInfo.setApiResponse("Document not found...!!" + docId);
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				logService.logMyInfo(httpRequest, apiLogInfo);
				return serviceResponse;
			} else {
				TimesheetDocumentDetailsDTO timesheetDocumentDetailsDTO = new TimesheetDocumentDetailsDTO();

				timesheetDocumentDetailsDTO.setDocId(docDetails.getDocId());
				timesheetDocumentDetailsDTO.setDocName(docDetails.getDocName());
				timesheetDocumentDetailsDTO.setDocData(docDetails.getDocData());
				timesheetDocumentDetailsDTO.setMimeType(docDetails.getDocMimeType());

				serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				serviceResponse.setServiceResponse(timesheetDocumentDetailsDTO);
//		            response.setServiceMessage("Client Side Id fetched successfully!");
				apiLogInfo.setApiResponse("Document details fetched successfully");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}
			finalHttpStatusCode = HttpStatus.OK.value();

		} catch (Exception e) {
			e.printStackTrace();
			exceptionDetailsForLog = e.toString();
			log.error("error in getDocumentDataByDocId" + e);
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceResponse(e.getMessage());
			throw e;
//				serviceResponse.setServiceMessage(e.getMessage());

		} finally {
			if (initialLog != null) {
				apiLogUtility.endLog(initialLog.getId(), sourceSystem, finalHttpStatusCode, exceptionDetailsForLog,
						httpRequest);
			}
		}
		return serviceResponse;
	}

	public ServiceResponse getAllApprovedPoWithTimesheet() {
		ServiceResponse serviceResponse = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getAllApprovedPoWithTimesheet");
		apiLogInfo.setLogLevel("INFO");
		ApiLog initialLog = null;
		String exceptionDetailsForLog = null;
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();

		// Start log
		initialLog = apiLogUtility.startLog(poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),
				"getAllApprovedPoWithTimesheet", "PoPortal", null, httpRequest);

		String sourceSystem = httpRequest.getRequestURI().toString();

		try {
			// Fetch data from repository
			List<Object> resultList = employeeTeamMapRepository.getAllApprovedPoWithTimesheet();

			// Validate result
			if (resultList == null || resultList.isEmpty()) {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("No Approved PO Projects found with Timesheet data.");
				serviceResponse.setServiceMessage("No records available for Approved POs.");

				apiLogInfo.setApiResponse("No Approved PO Projects found with Timesheet data.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				logService.logMyInfo(httpRequest, apiLogInfo);

				return serviceResponse;
			} else {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				serviceResponse.setServiceResponse(resultList);
				serviceResponse.setServiceMessage("Approved PO Projects with Timesheet data fetched successfully.");

				apiLogInfo.setApiResponse("Approved PO Projects with Timesheet data fetched successfully.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}

			finalHttpStatusCode = HttpStatus.OK.value();

		} catch (Exception e) {
			e.printStackTrace();
			exceptionDetailsForLog = e.toString();
			log.error("error in getAllApprovedPoWithTimesheet" + e);
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceResponse(ExceptionUtils.getExceptionMessage(e));
			serviceResponse.setServiceMessage("Error while fetching Approved PO Projects with Timesheet data.");

			// rethrow if you want global handler to catch it(if in future roolback logic is
			// needed)//reff-by Dibya
			throw e;

		} finally {
			if (initialLog != null) {
				apiLogUtility.endLog(initialLog.getId(), sourceSystem, finalHttpStatusCode, exceptionDetailsForLog,
						httpRequest);
			}
		}

		return serviceResponse;
	}

	public ServiceResponse getActiveTeamAndTimeSheetWithForRm(List<Long> projectIds) {
		ServiceResponse serviceResponse = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getActiveTeamAndTimeSheetWithForRm");
		apiLogInfo.setLogLevel("INFO");
		ApiLog initialLog = null;
		String exceptionDetailsForLog = null;
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();

		// Start log
		initialLog = apiLogUtility.startLog(poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),
				"getActiveTeamAndTimeSheetWithForRm", "PoPortal", null, httpRequest);

		String sourceSystem = httpRequest.getRequestURI().toString();

		try {

			if (projectIds == null || projectIds.isEmpty()) {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("Project id list is empty or null.");
				serviceResponse.setServiceMessage("No ActiveTeamAndTimeSheetWithForRm.");
				return serviceResponse;
			}

			// Fetch data from repository
			List<Object[]> resultList = employeeTeamMapRepository.getActiveTeamAndTimeSheetWithForRm(projectIds);

			// Validate result
			if (resultList == null || resultList.isEmpty()) {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("No ActiveTeamAndTimeSheetWithForRm.");
				serviceResponse.setServiceMessage("No ActiveTeamAndTimeSheetWithForRm.");

				apiLogInfo.setApiResponse("No ActiveTeamAndTimeSheetWithForRm");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				logService.logMyInfo(httpRequest, apiLogInfo);

				return serviceResponse;
			} else {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				serviceResponse.setServiceResponse(resultList);
				serviceResponse.setServiceMessage("ActiveTeamAndTimeSheetWithForRm fetched successfully.");

				apiLogInfo.setApiResponse("ActiveTeamAndTimeSheetWithForRm data fetched successfully.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}

			finalHttpStatusCode = HttpStatus.OK.value();

		} catch (Exception e) {
			e.printStackTrace();
			exceptionDetailsForLog = e.toString();
			log.error("error in getActiveTeamAndTimeSheetWithForRm" + e);
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceResponse(ExceptionUtils.getExceptionMessage(e));
			serviceResponse.setServiceMessage("Error while fetching Approved PO Projects with Timesheet data.");
			serviceResponse.setServiceError(ExceptionUtils.getExceptionMessage(e));
			// rethrow if you want global handler to catch it(if in future roolback logic is
			// needed)//reff-by Dibya
			throw e;

		} finally {
			if (initialLog != null) {
				apiLogUtility.endLog(initialLog.getId(), sourceSystem, finalHttpStatusCode, exceptionDetailsForLog,
						httpRequest);
			}
		}

		return serviceResponse;
	}

	@Transactional(rollbackFor = Exception.class)
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
			String ishineProjectStatus = "";

			Set<String> primaryTeamNames = (primaryTeams == null || primaryTeams.isEmpty()) ? Collections.emptySet()
					: primaryTeams.stream().map(t -> t[1] != null ? t[1].toString() : null).filter(Objects::nonNull)
							.collect(Collectors.toSet());
			DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

			if (payloadDTO.getDeletedProjects() == null || payloadDTO.getDeletedProjects().isEmpty()) {

				logBuilder.append("Deleted project list received at Ishine is empty.");
				System.err.println("Deleted project list received at Ishine is empty.");

			} else {

				for (HandleTeamsAsPerLinkedPoProjectDTO deletedProject : payloadDTO.getDeletedProjects()) {
					List<Object[]> deletedTeams = projectRepository
							.getTeamIdsForPoProjectId(deletedProject.getProjectId());
					List<Long> deletedTeamIds = new ArrayList<>();

					if (!deletedTeams.isEmpty() || deletedTeams != null) {
						for (Object[] team : deletedTeams) {
							Long teamId = team[0] != null ? Long.parseLong(team[0].toString()) : null;
							String teamName = team[1] != null ? team[1].toString() : null;

							String newTeamName = primaryTeamNames.contains(teamName)
									? teamName + " | " + deletedProject.getProjectName()
									: teamName;

							teamRepository.updateTeamName(teamId, newTeamName);
							deletedTeamIds.add(teamId);
						}
					}

					if (!deletedTeamIds.isEmpty()) {
						LiftAndShiftTeamsDTO liftAndShiftDTO = new LiftAndShiftTeamsDTO();
						liftAndShiftDTO.setTeamIds(deletedTeamIds);
						Project sourceProject = projectRepository.findByPoProjectId(deletedProject.getProjectId());
						liftAndShiftDTO.setSourceProjectId(sourceProject.getProjectId());
						Project targetProject = projectRepository.findByPoProjectId(primaryProjectDTO.getProjectId());
						liftAndShiftDTO.setTargetProjectId(targetProject.getProjectId());
						liftAndShiftDTO.setCurrentUserEmpId(6L);

						ServiceResponse lsResponse = context.getBean(getClass()).liftAndShiftTeams(liftAndShiftDTO);

						if (!ServiceResponse.STATUS_SUCCESS.equals(lsResponse.getServiceStatus())) {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse(
									"Team migration failed for project: " + deletedProject.getProjectName());
							return response;
						}
					}

					Project deletedProjEntity = projectRepository.findByPoProjectId(deletedProject.getProjectId());
					if (deletedProjEntity != null) {
						deletedProjEntity.setActive("false");
						deletedProjEntity.setPoNo(deletedProject.getPoNo());
//		                 deletedProjEntity.setClientId(deletedProject.getClientId());
						deletedProjEntity.setStartDate(
								dateFormatter.format(deletedProject.getStartDate().toLocalDateTime().toLocalDate()));
						deletedProjEntity.setEndDate(
								dateFormatter.format(deletedProject.getEndDate().toLocalDateTime().toLocalDate()));
						deletedProjEntity.setProjectName(deletedProject.getProjectName());
						deletedProjEntity.setUpdatedOn(LocalDateTime.now());
						deletedProjEntity.setUpdatedBy(6L);
						projectRepository.save(deletedProjEntity);

					} else {
						apiLogInfo.setApiResponse("No project found for poProjectId: " + deletedProject.getProjectId());
					}
				}
			}

			Project primaryProjectEntity = projectRepository.findByPoProjectId(primaryProjectDTO.getProjectId());
			if (primaryProjectEntity != null) {
				primaryProjectEntity.setPoNo(primaryProjectDTO.getPoNo());
//	             primaryProjectEntity.setClientId(primaryProjectDTO.getClientId());
				primaryProjectEntity.setStartDate(
						dateFormatter.format(primaryProjectDTO.getStartDate().toLocalDateTime().toLocalDate()));
				primaryProjectEntity.setEndDate(
						dateFormatter.format(primaryProjectDTO.getEndDate().toLocalDateTime().toLocalDate()));
				primaryProjectEntity.setProjectName(primaryProjectDTO.getProjectName());
				primaryProjectEntity.setUpdatedOn(LocalDateTime.now());
//	             primaryProjectEntity.setIsDraftProject("false");
				primaryProjectEntity.setUpdatedBy(6L);

				if (payloadDTO.getDeletedProjects() == null || payloadDTO.getDeletedProjects().isEmpty()) {
					logBuilder.append("Deleted project list received at Ishine is empty.");
					System.err.println("Deleted project list received at Ishine is empty.");
					ishineProjectStatus = getProjectStatusState(primaryProjectEntity);
				} else {

					for (HandleTeamsAsPerLinkedPoProjectDTO deletedProject : payloadDTO.getDeletedProjects()) {
						Project deletedProjEntity = projectRepository.findByPoProjectId(deletedProject.getProjectId());
						if (deletedProjEntity != null) {
							String primaryState = getProjectStatusState(primaryProjectEntity);
							String deletedState = getProjectStatusState(deletedProjEntity);

							String resolvedState = resolveProjectStatusState(primaryState, deletedState);

							if ("Pending".equals(resolvedState)) {
								primaryProjectEntity.setIsDraftProject("true");
								updateEmployeeTeamMapStatus(primaryProjectEntity.getProjectId());
							} else if ("Approved".equals(resolvedState)) {
								primaryProjectEntity.setIsDraftProject("false");
							} else if ("Rejected".equals(resolvedState)) {
								primaryProjectEntity.setIsDraftProject("Rejected");
							} else if ("Not Started".equals(resolvedState)) {
								primaryProjectEntity.setIsDraftProject(null);
							} else if ("Completed".equals(resolvedState)) {
								primaryProjectEntity.setProjectStatus("Completed");
								updateProjectActiveField(primaryProjectEntity.getProjectId());
							}

							ishineProjectStatus = getIshineProjectStatus(resolvedState);
						}
					}
				}

				projectRepository.save(primaryProjectEntity);
			}

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Poject/Team details upated after Linked Po successfully.");
			response.setServiceResponse1(ishineProjectStatus);
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

		} catch (NullPointerException ex) {

			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Null value encountered.");
			response.setServiceError(ex.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			throw ex;

		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error in handleTeamsAsPerLinkedPo", e);
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse(ExceptionUtils.getExceptionMessage(e));
			response.setServiceError(ExceptionUtils.getExceptionMessage(e));
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			throw e;
		}

		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	private String getProjectStatusState(Project projectEntity) {
		if (projectEntity == null)
			return "Not Started";

		String draftStatus = projectEntity.getIsDraftProject();
		String projectStatus = projectEntity.getProjectStatus();

		if ("true".equalsIgnoreCase(draftStatus)) {
			return "Pending";
		} else if ("false".equalsIgnoreCase(draftStatus)) {
			return "Approved";
		} else if ("Rejected".equalsIgnoreCase(draftStatus)) {
			return "Rejected";
		} else if ("Not Started".equalsIgnoreCase(draftStatus) || draftStatus == null) {
			return "Not Started";
		}

		if ("Completed".equalsIgnoreCase(projectStatus)) {
			return "Completed";
		}

		return "Not Started";
	}

	private String resolveProjectStatusState(String primaryState, String deletedState) {
		if (primaryState.equals(deletedState)) {
			return primaryState;
		}

		if ("Not Started".equals(primaryState) && "Approved".equals(deletedState)) {
			return "Pending";
		}
		if ("Approved".equals(primaryState) && "Not Started".equals(deletedState)) {
			return "Approved";
		}
		if ("Pending".equals(primaryState) && "Approved".equals(deletedState)) {
			return "Pending";
		}
		if ("Approved".equals(primaryState) && "Pending".equals(deletedState)) {
			return "Pending";
		}
		if ("Rejected".equals(primaryState)) {
			return deletedState;
		}
		if ("Rejected".equals(deletedState)) {
			return primaryState;
		}

		if ("Completed".equals(primaryState)) {
			return deletedState;
		}
		if ("Completed".equals(deletedState)) {
			return primaryState;
		}

		return primaryState;
	}

	private String getIshineProjectStatus(String resolvedState) {
		if (resolvedState == null) {
			return "Not Started";
		}

		switch (resolvedState) {
		case "Not Started":
		case "Rejected":
		case "Pending":
			return "Not Started";

		case "Approved":
			return "InProgress";

		case "Completed":
			return "Completed";

		default:
			return "Not Started";
		}
	}

	private void updateEmployeeTeamMapStatus(Integer projectId) {
		if (projectId == null) {
			return;
		}
		int updatedRows = employeeTeamMapRepository.updateActiveFrom1To2ByProjectId(projectId);
	}

	private void updateProjectActiveField(Integer projectId) {
		if (projectId == null) {
			return;
		}
		int updatedRows = projectRepository.updateProjectActiveField(projectId);
	}

	public ServiceResponse checkActiveAndPendingEmployeeMappingWithResourceOverViewId(
			List<Long> poRequirementMappingId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/checkActiveAndPendingEmployeeMappingWithResourceOverViewId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();

		try {
			List<Long> mappedEmployementIds = new ArrayList<Long>();

			if (poRequirementMappingId == null || poRequirementMappingId.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse(null);
				response.setServiceResponse1("No data recieved at Ishine's end");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setLogLevel("Info");

				return response;
			}

			poRequirementMappingId = poRequirementMappingId.stream().filter(Objects::nonNull)
					.collect(Collectors.toList());

			mappedEmployementIds = poRequirementMappingRepository
					.checkActiveAndPendingEmployeeMappingWithPoRequirementId(poRequirementMappingId);

			if (mappedEmployementIds != null && !mappedEmployementIds.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(mappedEmployementIds);
				response.setServiceResponse1("Employees mapped to this resource requirement!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setLogLevel("Info");

			} else {

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(null);
				response.setServiceResponse1("No employees mapped to this resource requirements!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setLogLevel("Info");

			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			throw e;
		}

		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	private ServiceResponse removeOtherMappingsForNewTNMMapping(Integer projectId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/removeOtherMappingsForNewTNMMapping");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();

		try {
			Map<String, List<Integer>> resultMap = new HashMap<>();
			Map<String, List<Long>> resultMap2 = new HashMap<>();
			List<Long> updatedTeamIds = new ArrayList<>();
			List<Integer> updatedProjectIds = new ArrayList<>();

			if (projectId == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse(resultMap);
				response.setServiceResponse1("Null values passed!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setLogLevel("Info");

				return response;
			}

			boolean isEligible = projectRepository.existsEligibleProject(projectId);

			if (!isEligible) {
				resultMap2.put("updatedTeamIds", updatedTeamIds);
				resultMap.put("updatedProjectIds", updatedProjectIds);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(resultMap);
				response.setServiceResponse1("No other active project mappings of the employee.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setLogLevel("Info");

				return response;
			}

			List<Team> teams = teamRepository.findByProjectIdAndIsActive(projectId.longValue(), "Y");

			for (Team team : teams) {
				List<EmployeeTeamMap> activeEtms = employeeTeamMapRepository.findByTeamIdAndActiveNot(team.getTeamId(),
						0);

				if (!activeEtms.isEmpty()) {
					// Update etm.active = 0
					activeEtms.forEach(etm -> etm.setActive(0L));
					employeeTeamMapRepository.saveAll(activeEtms);
					updatedTeamIds.add(team.getTeamId());

					// Check if any etm.active != 0 remain
					boolean stillActive = employeeTeamMapRepository.existsByTeamIdAndActiveNot(team.getTeamId(), 0);
					if (!stillActive) {
						team.setIsActive("N");
						teamRepository.save(team);
						updatedProjectIds.add(team.getProjectId());
					}
				}
			}

			resultMap2.put("updatedTeamIds", updatedTeamIds);
			resultMap.put("updatedProjectIds", updatedProjectIds);

			if (resultMap != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(resultMap);
				response.setServiceResponse1("Other Project Mappings of the employee has been removed success fully!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setLogLevel("Info");

			} else {

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(resultMap);
				response.setServiceResponse1("No employees mapped to this resource requirements!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setLogLevel("Info");

			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			throw e;
		}

		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse restorePreviousStateOfProject(RestoreProjectPayloadDTO payloadDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/restorePreviousStateOfProject");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();

		try {
			Integer projectId = payloadDTO.getProjectId();
			Long empId = payloadDTO.getCurrentUserEmpId();
			if (projectId == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to find the project!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setLogLevel("ERROR");
				apiLogInfo.setApiError("Recieved projectId is null");
				throw new BadRequestException("Unable to process the project Info");
			}
			Project project = projectRepository.findByProjectId(projectId);
			if (project == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to find the project!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setLogLevel("ERROR");
				apiLogInfo.setApiError("Project not found by repository!");
				throw new BadRequestException("Unable to process the project Info");
			}
			response = context.getBean(getClass()).updateTeams(projectId, empId);
			Object obj = response.getServiceResponse1();
			List<Long> teamIds = new ArrayList<>();
			if (obj != null) {
				if (obj instanceof List<?>) {
					teamIds = ((List<?>) obj).stream().filter(Objects::nonNull).map(o -> {
						if (o instanceof Long)
							return (Long) o;
						else if (o instanceof Integer)
							return ((Integer) o).longValue();
						else
							throw new IllegalArgumentException("Unexpected type in list: " + o.getClass());
					}).collect(Collectors.toList());
				} else {
					throw new IllegalArgumentException("serviceResponse1 is not a List");
				}
			}
			if (!teamIds.isEmpty()) {
				response = context.getBean(getClass()).updateEmployeeTeamMap(teamIds, project.getPoProjectId(), empId);
				response = context.getBean(getClass()).updateProjectManagers(projectId, empId);
				response = context.getBean(getClass()).updateProjectOverheads(projectId, empId);
			}
			project.setActive("true");
			project.setIsDraftProject("true");
			project.setProjectCompletionDate(null);
			project.setProjectStatus(null);
			projectRepository.save(project);
			if (ServiceResponse.STATUS_SUCCESS.equals(response.getServiceStatus())) {
				project.setActive("true");
				project.setIsDraftProject("true");
				projectRepository.save(project);
			}
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(
					"Project Restored Successfully! Please approve it to allow employees to fill timesheet in it.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			apiLogInfo.setLogLevel("Info");
		} catch (BadRequestException bre) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Bad Request: " + bre.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			throw bre;
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			throw e;
		}

		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

//	public ServiceResponse getCertificatesRbac(FilterMatrix filterMatrix) {
//		ServiceResponse response = new ServiceResponse();
//	     LogDTO apiLogInfo = new LogDTO();
//	     apiLogInfo.setApiUrl("/api/matrixCertificationDropdownRbac");
//		 apiLogInfo.setLogLevel("INFO");
//	     StringBuilder logBuilder = new StringBuilder(); 
//
//	     try {
//	    	    Long empIdd = filterMatrix.getEmpId();
////			    Employee employee = employeeRepository.findByEmpId(empIdd);
////			    JobRole jobRole = jobRoleRepository.findByjobRoleId(employee.getJobRoleId());
////			    Department department = departmentRepository.findByDeptId(jobRole.getDeptId());
//			    JobRoleDTO jobRoleDept = jobRoleRepository.findJobRoleDept(empIdd);
//			    String departmentName = jobRoleDept.getDepartmentName();
//			    String role = jobRoleDept.getEmployeeRole();
//			    String name = jobRoleDept.getName();
//
//			    Set<String> specialDepartments = Set.of("Admin", "Resource Management Group", "Director", "Super Admin", "Accounts", "HR");
//			   List<EmployeeCertificates> certficates = new ArrayList<EmployeeCertificates>() ;
//
//			    
//			    if (role.equalsIgnoreCase("SuperAdmin") || name.equalsIgnoreCase("Director")
//			            || name.equalsIgnoreCase("Super Admin") || role.equalsIgnoreCase("Accounts")
//			            || specialDepartments.contains(departmentName)) {
//			    	certficates = employeeCertificatesRepository.findAllActiveCertficates();
//			    } else if (departmentRepository.existsByHodId(empIdd)) {
//		            List<Long> deptIds = departmentRepository.findDeptIdsByHodId(empIdd);
//		            List<Long> reportees = departmentRepository.findAllReporteesOfHod(empIdd);
//		            certficates = employeeCertificatesRepository.findCertficatesByDeptIdsAndOfReportees(deptIds,reportees);
//		            
//			    } 
////		            else {
////			    	Long deptId = departmentRepository.findDepartmentofCurrentuser(employee.getJobRoleId());			    	
////			    }
//			    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//		         response.setServiceResponse(certficates);
//		         apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//		         apiLogInfo.setLogLevel("Info");
//		            
//	    	 
//	     }catch (Exception e) {
//			e.printStackTrace();
//	         response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//	         response.setServiceResponse("Something Went Wrong.");
//	         response.setServiceError(e.getMessage());
//	         apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//	         apiLogInfo.setLogLevel("ERROR");
//	         throw e;
//	     }
//
//	     apiLogInfo.setApiRequest(logBuilder.toString());
//	     logService.logMyInfo(httpRequest, apiLogInfo);
//	     return response;
//	}

	public ServiceResponse getCertificatesRbac(FilterMatrix filterMatrix) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/matrixCertificationDropdownRbac");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();

		try {

			List<EmployeeCertificates> certficates = employeeCertificatesRepository.findAllActiveCertficates();
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(certficates);
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			apiLogInfo.setLogLevel("Info");

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			throw e;
		}

		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

//	public ServiceResponse getDepartmentsRbac(FilterMatrix filterMatrix) {
//		ServiceResponse response = new ServiceResponse();
//	     LogDTO apiLogInfo = new LogDTO();
//	     apiLogInfo.setApiUrl("/api/matrixDepartmentDropdownRbac");
//	     apiLogInfo.setLogLevel("INFO");
//	     StringBuilder logBuilder = new StringBuilder(); 
//
//	     try {
//	    	  Long empIdd = filterMatrix.getEmpId();
////			    Employee employee = employeeRepository.findByEmpId(empIdd);
////			    JobRole jobRole = jobRoleRepository.findByjobRoleId(employee.getJobRoleId());
////			    Department department = departmentRepository.findByDeptId(jobRole.getDeptId());
//	    	  
//	    	  JobRoleDTO jobRoleDept = jobRoleRepository.findJobRoleDept(empIdd);
//	    	  
//
//			    String departmentName = jobRoleDept.getDepartmentName();
//			    String role = jobRoleDept.getEmployeeRole();
//			    String name = jobRoleDept.getName();
//
//			    Set<String> specialDepartments = Set.of("Admin", "Resource Management Group", "Director", "Super Admin", "Accounts", "HR");
//			   List<GetDeptIdByRoleDTO> departments = new ArrayList<GetDeptIdByRoleDTO>() ;
//
//			    
//			    if (role.equalsIgnoreCase("SuperAdmin") || name.equalsIgnoreCase("Director")
//			            || name.equalsIgnoreCase("Super Admin") || role.equalsIgnoreCase("Accounts")
//			            || specialDepartments.contains(departmentName)) {
//			    	departments = departmentRepository.findAllDepartmentsForSA();
//			    } else if (departmentRepository.existsByHodId(empIdd)) {
//		            List<Long> deptIds = departmentRepository.findDeptIdsByHodId(empIdd);
//		           
//		            departments = departmentRepository.findDepartmentsByIds(deptIds);
//		            
//			    } 
////		            else {
////			    	Long deptId = departmentRepository.findDepartmentofCurrentuser(employee.getJobRoleId());			    	
////			    }
//			    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//		         response.setServiceResponse(departments);
//		         apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//		         apiLogInfo.setLogLevel("Info");
//	    	 
//	     }catch (Exception e) {
//	         e.printStackTrace();
//	         response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//	         response.setServiceResponse("Something Went Wrong.");
//	         response.setServiceError(e.getMessage());
//	         apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//	         apiLogInfo.setLogLevel("ERROR");
//	         throw e;
//	     }
//
//	     apiLogInfo.setApiRequest(logBuilder.toString());
//	     logService.logMyInfo(httpRequest, apiLogInfo);
//	     return response;
//	}

	public ServiceResponse getDepartmentsRbac(FilterMatrix filterMatrix) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/matrixDepartmentDropdownRbac");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();

		try {

			List<GetDeptIdByRoleDTO> departments = departmentRepository.findAllDepartmentsForSA();
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(departments);
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			apiLogInfo.setLogLevel("Info");

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			throw e;
		}

		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateProjectManagers(Integer projectId, Long empId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/restorePreviousStateOfProject/updateProjectManagers");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		try {
			if (projectId == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to find the project!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setLogLevel("ERROR");
				apiLogInfo.setApiError("Recieved projectId is null");
				throw new BadRequestException("Unable to process the project Info");
			}
			List<ProjectManagerMapping> projManagerList = projectManagerMappingRepository
					.findByProjectId(Long.valueOf(projectId));
			logBuilder.append("updateProjectManagers started:- \n");
			if (projManagerList != null && !projManagerList.isEmpty()) {
				projManagerList.forEach(pmm -> {
					pmm.setActive(1);
					pmm.setUpdatedOn(LocalDateTime.now());
					pmm.setUpdatedBy(empId);
					logBuilder.append("ProjectManagerMappingId: " + pmm.getProjectManagerMappingId() + "\n");
				});
				projectManagerMappingRepository.saveAll(projManagerList);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Project Managers Updated Successfully!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setLogLevel("Info");
			}
		} catch (BadRequestException bre) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Bad Request: " + bre.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			throw bre;
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			throw e;
		}
		apiLogInfo.setApiError(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateProjectOverheads(Integer projectId, Long empId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/restorePreviousStateOfProject/updateProjectOverheads");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		try {
			if (projectId == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to find the project!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setLogLevel("ERROR");
				apiLogInfo.setApiError("Recieved projectId is null");
				throw new BadRequestException("Unable to process the project Info");
			}
			List<ProjectOverheadMapping> projOverheadList = projectOverheadMappingRepository
					.findByProjectId(Long.valueOf(projectId));
			logBuilder.append("updateProjectOverheads started:- \n");
			if (projOverheadList != null && !projOverheadList.isEmpty()) {
				projOverheadList.forEach(poh -> {
					poh.setActive(1);
					poh.setUpdatedOn(LocalDateTime.now());
					poh.setUpdatedBy(empId);
					logBuilder.append("ProjectOverheadMappingId: " + poh.getProjectOverheadMappingId() + "\n");
				});
				projectOverheadMappingRepository.saveAll(projOverheadList);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Project Overheads Updated Successfully!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setLogLevel("Info");
			}
		} catch (BadRequestException bre) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Bad Request: " + bre.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			throw bre;
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			throw e;
		}
		apiLogInfo.setApiError(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateTeams(Integer projectId, Long empId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/restorePreviousStateOfProject/updateTeams");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		try {
			if (projectId == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to find the project!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setLogLevel("ERROR");
				apiLogInfo.setApiError("Recieved projectId is null");
				throw new BadRequestException("Unable to process the project Info");
			}
			List<Team> teamList = teamRepository.findLatestTeamsPerProject(projectId);
			List<Long> teamIds = new ArrayList<>();
			logBuilder.append("updateTeams started " + "\n");
			logBuilder.append("teamList: " + teamList + "\n");
			if (teamList != null && !teamList.isEmpty()) {
				teamList.forEach(t -> {
					t.setIsActive("Y");
					t.setUpdatedOn(LocalDateTime.now());
					t.setUpdatedBy(empId);
					teamIds.add(t.getTeamId());
				});
				teamRepository.saveAll(teamList);
			}
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Teams Updated Successfully!");
			response.setServiceResponse1(teamIds);
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			apiLogInfo.setLogLevel("Info");
		} catch (BadRequestException bre) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Bad Request: " + bre.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			throw bre;
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			throw e;
		}
		apiLogInfo.setApiError(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateEmployeeTeamMap(List<Long> teamIds, Long poId, Long empId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/restorePreviousStateOfProject/updateProjectOverheads");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		try {
			Map<Long, Long> skippedEmpMap = new HashMap<>();
			if (teamIds == null || teamIds.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to find teams for this project!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setLogLevel("ERROR");
				apiLogInfo.setApiError("Recieved teamId is null");
				throw new BadRequestException("Unable to process the project Info");
			}

			List<EmployeeTeamMap> empList = employeeTeamMapRepository.findLatestByTeamIds(teamIds);
			logBuilder.append("updateEmployeeTeamMap started " + "\n");
			logBuilder.append("empList: " + empList + "\n");
//			ServiceResponse requirementResponse = getResourceRequirementByPoProjectId(poProjectId);
//			Set<Long> validResourceOverviewIds = new HashSet<>();
//
//			if (requirementResponse.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS)) {
//				ProjectRequirementResponse requirementData = (ProjectRequirementResponse) requirementResponse
//						.getServiceResponse();
//				List<ResourceRequirementResponse> requirements = requirementData.getResourceRequirementList();
//				validResourceOverviewIds = requirements.stream().map(ResourceRequirementResponse::getResourceOverviewId)
//						.collect(Collectors.toSet());
//			}
			Set<Long> validPoRequirementMappingIds = getValidPoRequirementMappingIds(poId);
			logBuilder.append("Valid PO Requirement Mapping IDs: ").append(validPoRequirementMappingIds).append("\n");

			if (validPoRequirementMappingIds.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No valid PO requirements found for PO ID: " + poId);
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setLogLevel("ERROR");
				logBuilder.append("No valid PO requirements found for PO ID: ").append(poId).append("\n");
				throw new IllegalStateException("No valid PO requirements found for the given PO");
			}

			if (empList != null && !empList.isEmpty()) {
				List<EmployeeTeamMap> toUpdate = new ArrayList<>();
				for (EmployeeTeamMap etm : empList) {
					if (etm.getPoRequirementMappingId() != null
							&& validPoRequirementMappingIds.contains(etm.getPoRequirementMappingId())) {
						etm.setActive(2L);
						etm.setUpdatedOn(LocalDateTime.now());
						etm.setUpdatedBy(empId);
						toUpdate.add(etm);
					} else {
						skippedEmpMap.put(etm.getEmpId(), etm.getPoRequirementMappingId());
					}
				}
				if (!toUpdate.isEmpty()) {
					employeeTeamMapRepository.saveAll(toUpdate);
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Employees Updated Successfully!");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					apiLogInfo.setLogLevel("Info");
					logBuilder.append("Employees Updated Successfully!: " + "\n");
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No employees eligible for update, rolling back!");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					apiLogInfo.setLogLevel("ERROR");
					logBuilder.append("No employees eligible for update, rolling back " + "\n");
					throw new IllegalStateException(
							"Could not restore the previous project state as mapped resource overview has been removed/modified from Shankh Portal");
				}
			}
//			 if (!skippedEmpMap.isEmpty()) {
//				    Set<Long> empIds = skippedEmpMap.keySet();
//				    Set<Long> resourceOverviewIds = new HashSet<>(skippedEmpMap.values());
//				    notifyHod(empIds, resourceOverviewIds);
//				}

			if (!skippedEmpMap.isEmpty()) {
				Set<Long> empIds = skippedEmpMap.keySet();
				Set<Long> poRequirementMappingIds = new HashSet<>(skippedEmpMap.values());
				notifyHod(empIds, poRequirementMappingIds);
				logBuilder.append("HOD notification sent for ").append(skippedEmpMap.size())
						.append(" skipped employees\n");
			}
		} catch (BadRequestException bre) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Bad Request: " + bre.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			throw bre;
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			throw e;
		}
		apiLogInfo.setApiError(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public void notifyHod(Set<Long> empIds, Set<Long> poRequirementMappingId) {
		List<SkippedEmployeeDTO> skippedList = projectRepository.findAllSkippedEmployees(empIds,
				poRequirementMappingId);

		Map<String, List<SkippedEmployeeDTO>> hodWiseMap = skippedList.stream()
				.collect(Collectors.groupingBy(SkippedEmployeeDTO::getHodEmail));

		hodWiseMap.forEach((hodEmail, list) -> {
			try {
				System.err.println("Mail initiated: Hod - " + hodEmail);
				System.err.println("Mail body: - " + buildEmailTable(list));
				mailService.sendMailWithCC(hodEmail, rmgMail,
						"Action Required: Employees Skipped Due to Missing Resource Overview in Shankh Portal",
						"Dear HOD,<br><br>"
								+ "The following employees could not be restored as the resource requirement they were earlier mapped to no longer exists in Shankh Portal.<br><br>"
								+ buildEmailTable(list) + "<br><br>Sincerely,<br>Team RMG - ApMoSys Technologies");
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private String buildEmailTable(List<SkippedEmployeeDTO> skippedList) {
		StringBuilder sb = new StringBuilder();
		sb.append("<table border='1' cellpadding='6' cellspacing='0' style='border-collapse: collapse;'>");
		sb.append("<tr>").append("<th>Employment ID</th>").append("<th>Name</th>").append("<th>Designation</th>")
				.append("<th>Role (ResourceRequirement)</th>").append("<th>Department (ResourceRequirement)</th>")
				.append("<th>Experience</th>").append("</tr>");

		for (SkippedEmployeeDTO s : skippedList) {
			sb.append("<tr>").append("<td>").append(s.getEmploymentId()).append("</td>").append("<td>")
					.append(s.getEmpName()).append("</td>").append("<td>").append(s.getDesignationName())
					.append("</td>").append("<td>").append(s.getRole()).append("</td>").append("<td>")
					.append(s.getResourceDept()).append("</td>").append("<td>").append(s.getExperience())
					.append("</td>").append("</tr>");
		}
		sb.append("</table>");
		return sb.toString();
	}

//	
	public ServiceResponse getResourceRequirementByPoProjectId(Long id, String projectType) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getResourceRequirementByPoProjectId");
		apiLogInfo.setApiUrl("/api/getResourceRequirementByPoProjectId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		try {
			if (id == null || projectType == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project Id or Project Type cannot be null!");
				logBuilder.append("\n Project Id or Project Type cannot be null!");
				return response;
			}

			if (projectType != null && !projectType.equals("TNM")) {
				return getFixedCostProjectResourceRequirement(id, response);
			}

			ServiceResponse projectApiResponse = poPortalAPIService.fetchPoPortalProjectById(id);
			if (projectApiResponse.getServiceStatus().equals(ServiceResponse.STATUS_FAIL)) {
				return projectApiResponse;
			}
			List<ResourceRequirementResponse> project = (List<ResourceRequirementResponse>) projectApiResponse
					.getServiceResponse();

			if (project == null) {
				response.setServiceResponse("No Resource Requirement Found!");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				logBuilder.append("\n No Resource Requirement Found!");
				return response;
			} else {
				ServiceResponse countApiResponse = poPortalAPIService.getCountByProjectId(id);
				if (countApiResponse.getServiceResponse() != null) {
					if (ServiceResponse.STATUS_FAIL.equals(countApiResponse.getServiceStatus())) {
						throw new RuntimeException("Failed to fetch count from PO Portal.");
					}

					ProjectRequirementsDTO projectRequirementsDTO = new ProjectRequirementsDTO();
					if (countApiResponse.getServiceResponse() != null) {
						projectRequirementsDTO.setTotalRequirements(
								Integer.parseInt(countApiResponse.getServiceResponse().toString()));
					}
					if (projectRequirementsDTO.getTotalRequirements() == null) {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse(
								"Received success status from PO Portal, but requirement data was null.");
						return response;
					}

					ProjectRequirementResponse finalDto = new ProjectRequirementResponse();
//					List<Object[]> result = projectRepository.getAssignedEmployeesCountInProject(id);
//					setProjectRequirementByProjectId(id,projectRequirementsDTO,result);
					finalDto.setResourceRequirementList(project);
					finalDto.setResourceRequirements(projectRequirementsDTO);
					response.setServiceResponse(finalDto);
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

	public ServiceResponse getFixedCostProjectResourceRequirement(Long id, ServiceResponse serviceResponse) {
		try {
			ProjectRequirementsDTO projectRequirementsDTO = new ProjectRequirementsDTO();
			ProjectRequirementResponse finalDto = new ProjectRequirementResponse();
			List<Object[]> result = projectRepository.getFCAssignedEmployeesCountInProjectByPOProjectId(id);
			if (result == null || result.isEmpty()) {
				result = projectRepository.getFCAssignedEmployeesCountInProjectByProjectId(id);
			}
			setProjectRequirementByProjectId(id, projectRequirementsDTO, result, 0);
			finalDto.setResourceRequirementList(Collections.emptyList());
			finalDto.setResourceRequirements(projectRequirementsDTO);
			serviceResponse.setServiceResponse(finalDto);
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceResponse("Something went wrong while fetching assigned resources count!");
		}
		return serviceResponse;
	}

	public void setProjectRequirementByProjectId(Long id, ProjectRequirementsDTO dto, List<Object[]> result,
			int totalRequirement) {
		if (result != null && !result.isEmpty()) {
			Object[] row = result.get(0);
			Integer assignedApproved = row[1] != null ? ((Number) row[1]).intValue() : 0;
			Integer assignedPending = row[2] != null ? ((Number) row[2]).intValue() : 0;
			Integer assignedTotal = row[3] != null ? ((Number) row[3]).intValue() : 0;
			dto.setAssigned(assignedTotal);
			dto.setAssignedApproved(assignedApproved);
			dto.setAssignedPending(assignedPending);
			dto.setDifference(totalRequirement - assignedTotal);
		} else {
			dto.setAssigned(0);
			dto.setAssignedApproved(0);
			dto.setAssignedPending(0);
			dto.setDifference(dto.getTotalRequirements());
		}
	}

	public ServiceResponse getAssignedDataForAProject(Long id, int totalRequirement) {
		ServiceResponse response = new ServiceResponse();
		try {
			if (id == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project Id cannot be null!");
				return response;
			}

			ProjectRequirementsDTO projectRequirementsDTO = new ProjectRequirementsDTO();
			List<Object[]> result = projectRepository.getAssignedEmployeesCountInProject(id);

			setProjectRequirementByProjectId(id, projectRequirementsDTO, result, totalRequirement);
			response.setServiceResponse(projectRequirementsDTO);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);

		}

		catch (Exception ex) {
			ex.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Something went wrong while fetching assigned resources count!");

		}
		return response;

	}

	public ServiceResponse empCountDepartmentsWise(FilterMatrix filterMatrix) {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Long> deptIds = filterMatrix.getDeptIds();
			Long employeeActiveCount = employeeRepository.empCountDepartmentsWise(deptIds);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(employeeActiveCount);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;

	}

	public ServiceResponse getProjectConfigurationDetailsByProjectId(Integer projectId, boolean isAllProjects) {
		ServiceResponse serviceResponse = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		try {
			if (projectId == null) {
				return failResponse(serviceResponse, apiLogInfo, "Project Id cannot be null");
			}
			RmgProjectDto rmgProjectDto = null;
			List<Object[]> projectConfigurationDetails = projectRepository
					.getProjectConfigurationDetailsByProjectIdNew(projectId);

			if (projectConfigurationDetails != null && !projectConfigurationDetails.isEmpty()) {
				rmgProjectDto = projectConfigurationDetails.stream().findFirst().map(RmgProjectDto::new).orElse(null);

				if (rmgProjectDto != null && rmgProjectDto.getProjectId() != null) {
					Long currentProjectId = Long.parseLong(rmgProjectDto.getProjectId().toString());

					List<Long> deptIds = poDepartmentMappingRepository.findPoDeptIdsByProjectId(rmgProjectDto.getProjectId(), isAllProjects);
					rmgProjectDto.setDepartmentIds(deptIds);

					List<Long> projectManagerIds = projectManagerMappingRepository
							.findProjectManagerIdByProjectIdAndActive(currentProjectId,1);
					rmgProjectDto.setProjectManagerIds(projectManagerIds);

					List<Long> overHeadIds = projectOverheadMappingRepository.findProjectOverheadIdByProjectIdAndActive(currentProjectId,1);
					rmgProjectDto.setProjectOverheadIds(overHeadIds);

					List<PoDetailsDto> poDetailsDtos = getPoDetailsByProjectId(rmgProjectDto.getProjectId(), rmgProjectDto.getInternalProjectType(),rmgProjectDto.getPoProjectType(), isAllProjects);
					rmgProjectDto.setPoDetailsList(poDetailsDtos);
				}
			}
			if (rmgProjectDto != null) {
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				serviceResponse.setServiceResponse(rmgProjectDto);
			} else {
				return failResponse(serviceResponse, apiLogInfo, "Project details not found.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return failResponse(serviceResponse, apiLogInfo, "Something went wrong.");
		}
		return serviceResponse;
	}

	private List<PoDetailsDto> getPoDetailsByProjectId(Integer currentProjectId, String internalProjectType,
			String poProjectType, boolean isAllProjects) {
		List<PoDetailsDto> poDetailsDtos = new ArrayList<>();
		try {
			poDetailsDtos = poDetailsRepository.getAllProjectPoDetailsDtoByProjectId(currentProjectId, isAllProjects);
			if (poProjectType != null) {
				if (!poDetailsDtos.isEmpty()) {
					List<Long> poIds = poDetailsDtos.stream().map(PoDetailsDto::getPoId)
							.collect(Collectors.toList());

					Map<Long, Long> poIdCountMap = getPoIdAndCountMap(poIds, currentProjectId);
					if (poIdCountMap != null && !poIdCountMap.isEmpty()) {
						for (PoDetailsDto poDetail : poDetailsDtos) {
							poDetail.setTotalRequirements(poIdCountMap.getOrDefault(poDetail.getPoId(), 0L));
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return poDetailsDtos;
	}

	private Map<Long, Long> getPoIdAndCountMap(List<Long> poIds, Integer projectId) {
		Map<Long, Long> poIdCountMap = new HashMap<>();
		List<Object[]> results = poRequirementMappingRepository.getPoIdAndTotalActiveRequiredCountByPoIdInAndProjectId(poIds, projectId);
		if (results != null && !results.isEmpty()) {
			for (Object[] obj : results) {
				Long poId = TypeConversionUtil.safeParseLong(obj[0]);
				Long count = TypeConversionUtil.safeParseLong(obj[1]);
				poIdCountMap.put(poId, count);
			}
		}
		return poIdCountMap;
	}

	private Set<Long> getValidPoRequirementMappingIds(Long poId) {
		Set<Long> validPoRequirementMappingIds = new HashSet<>();

		try {
			List<PoRequirementMapping> poRequirements = poRequirementMappingRepository.findByPoId(poId);

			if (poRequirements != null && !poRequirements.isEmpty()) {
				validPoRequirementMappingIds = poRequirements.stream().filter(PoRequirementMapping::isActive)
						.map(PoRequirementMapping::getPoRequirementMappingId).collect(Collectors.toSet());
			}

			System.out.println("Found " + validPoRequirementMappingIds.size()
					+ " valid PO requirement mappings for PO ID: " + poId);

		} catch (Exception e) {
			System.err.println("Error fetching PO requirements for PO ID " + poId + ": " + e.getMessage());
			e.printStackTrace();
		}

		return validPoRequirementMappingIds;
	}

	public ServiceResponse getResourceRequirementByPoId(Long poId) {
		ServiceResponse response = new ServiceResponse();
		try {
			if (poId == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Po Id cannot be null!!");
				return response;
			}

			List<RmgResourceRequirementDto> resourceRequirementList = poRequirementMappingRepository
					.getPoRequirementDataByPoId(poId);

			if (resourceRequirementList == null || resourceRequirementList.isEmpty()) {
				response.setServiceResponse("No Resource Requirement Found!!");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				return response;
			}
			response.setServiceResponse(resourceRequirementList);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Something went wrong!!");
		}
		return response;
	}

	public ServiceResponse getActivePoDetailsByProjectId(Integer projectId) {
		ServiceResponse serviceResponse = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		try {
			if (projectId == null) {
				return failResponse(serviceResponse, apiLogInfo, "Project Id cannot be null");
			}

			List<PoDetailsDto> poDetailsDtos = poDetailsRepository.getActivePoDetailsDtoByProjectId(projectId);
			
			if (poDetailsDtos == null || poDetailsDtos.isEmpty()) {
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiResponse("PO Details Not found!!");
				serviceResponse.setServiceResponse("PO Details Not found!!");
				return serviceResponse;
			}

			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			serviceResponse.setServiceResponse(poDetailsDtos);
			apiLogInfo.setApiResponse("PO Details fetched successfully!!");
		} catch (Exception e) {
			e.printStackTrace();
			return failResponse(serviceResponse, apiLogInfo, "Something went wrong.");
		}
		return serviceResponse;
	}

	public ServiceResponse getResourceRequirementByTeamId(Long teamId) {
		ServiceResponse response = new ServiceResponse();
		try {
			if (teamId == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Team Id cannot be null!!");
				return response;
			}

			List<RmgResourceRequirementDto> resourceRequirementList = poRequirementMappingRepository
					.getPoRequirementDataByTeamId(teamId);

			if (resourceRequirementList == null || resourceRequirementList.isEmpty()) {
				response.setServiceResponse("No Resource Requirement Found!!");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				return response;
			}
			response.setServiceResponse(resourceRequirementList);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Something went wrong!!");
		}
		return response;
	}
	
	public ServiceResponse ishineToPoEmpDetails(IshineToPoRequestDTO ishineToPoRequest) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		ApiLog initialLog = null;
		int finalHttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		String sourceSystem = httpRequest.getRequestURI().toString();

		try {
			
			initialLog = apiLogUtility.startLog(poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),
					"ishineToPoEmpDetails", "PoPortal", null, httpRequest);

			if (ishineToPoRequest == null){
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Request recieved from PO is null!!");
				apiLogInfo.setApiResponse("Request recieved from PO is null!!");
				return response;
			}
			
			initialLog = apiLogUtility.startLog(poPortalAPIAuthenticationJWTUtility.extractTraceId(httpRequest),
					"ishineToPoEmpDetails", "PoPortal", 
					ishineToPoRequest.getUserId()!=null?ishineToPoRequest.getUserId():null, httpRequest);

			
			if (ishineToPoRequest.getPoId() == null){
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("PO Id in request is null!!");
				apiLogInfo.setApiResponse("PO Id in request is null!!");
				return response;
			}else if (ishineToPoRequest.getProjectId()== null){
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project Id in request is null!!");
				apiLogInfo.setApiResponse("Project Id in request is null!!");
				return response;
			}else if (ishineToPoRequest.getStartDateOfBilling()== null){
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Start Date in request is null!!");
				apiLogInfo.setApiResponse("Start Date in request is null!!");
				return response;
			}else if (ishineToPoRequest.getEndDateOfBilling()== null){
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("End Date in request is null!!");
				apiLogInfo.setApiResponse("End Date in request is null!!");
				return response;
			}
			
			if (ishineToPoRequest.getStartDateOfBilling()
			        .after(ishineToPoRequest.getEndDateOfBilling())) {
				finalHttpStatusCode = HttpStatus.BAD_REQUEST.value();
			    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			    response.setServiceResponse("Start Date cannot be after End Date!!");
				apiLogInfo.setApiResponse("Start Date cannot be after End Date!!");
			    return response;
			}
	
			IshineToPoEmpDetailsSharingDTO dto = projectPoDetailsRepository
					.findPoBasicDetails(ishineToPoRequest.getPoId(),ishineToPoRequest.getProjectId());
			
		    LocalDate startDate = convertToLocalDate(ishineToPoRequest.getStartDateOfBilling());
		    LocalDate endDate   = convertToLocalDate(ishineToPoRequest.getEndDateOfBilling());
		    
		    if(dto.getIshineProjectId()==null) {
					response.setServiceResponse("Project Details for the PO not found!!");
					apiLogInfo.setApiResponse("Project Details for the PO not found!!");
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					return response;
			    }
		    
		    Project project = projectRepository.findByProjectId(dto.getIshineProjectId());
		      
		    if(project==null) {
				 response.setServiceResponse("Project Details for the PO not found!!");
				 apiLogInfo.setApiResponse("Project Details for the PO not found!!");
				 response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				 return response;
			 }else if( project.getIsDraftProject()==null) {
				response.setServiceResponse("Resource onboarding has not started!!");
				apiLogInfo.setApiResponse("Resource onboarding has not started!!");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				return response;
		    }
//			 else if(!"true".equalsIgnoreCase(project.getActive())) {
//				response.setServiceResponse("Selected Project is no more active!!");
//				apiLogInfo.setApiResponse("Selected Project is no more active!!");
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				return response;
//		    }
		    		    
		    List<EmpMappingDTO> etm = employeeTeamMapRepository
		    		.getActiveEmpDetails(ishineToPoRequest.getPoId());
		    
		    if(etm == null || etm.isEmpty()){
				response.setServiceResponse("No active employee mapping found!!");
				apiLogInfo.setApiResponse("No active employee mapping found!!");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				return response;
		    }
		    
//		    if (etm.stream().allMatch(e -> Integer.valueOf(1).equals(e.getIsShadow()))) {
//		        response.setServiceResponse("Only shadow employee mappings found!!");
//		        apiLogInfo.setApiResponse("Only shadow employee mappings found!!");
//		        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//		        return response;
//		    }		    
		    		 		    	
			List<IshineToPoEmployeeDTO> employees = new ArrayList<>();
			
			for (EmpMappingDTO e : etm) {

			    IshineToPoEmployeeDTO emp =
	                    projectPoDetailsRepository.findEmployeesWithTimesheetCount(
	                            e.getEmpId(),startDate,endDate,
	                            ishineToPoRequest.getPoId(),ishineToPoRequest.getClientId());
			    
	            if (emp != null && (emp.getPoId()!=null && emp.getPoId().equals(ishineToPoRequest.getPoId())))
	            		{ employees.add(emp); }
	        }
						
			if (employees == null || employees.isEmpty()) {
				response.setServiceMessage("No timesheet filled by employee for the given time range!!");
				apiLogInfo.setApiResponse("No timesheet filled by employee for the given time range!!");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				return response;
			}
			
			//Handling shadow logic
			Map<Long, IshineToPoEmployeeDTO> empMap = new HashMap<>();

			for (IshineToPoEmployeeDTO emp : employees) {
			    empMap.put(emp.getIshineEmpId(), emp);
			}
			
			for (IshineToPoEmployeeDTO emp : new ArrayList<>(empMap.values())) {

			    if (emp.getIsShadow() != null && emp.getIsShadow() == 1
			            && emp.getShadowEmpId() != null) {

			        Long shadowEmpId = emp.getIshineEmpId();   // Shadow resource
			        Long mainEmpId   = emp.getShadowEmpId();   // Main resource

			        IshineToPoEmployeeDTO mainEmp = empMap.get(mainEmpId);

			        // Main resource missing then fetch minimal info
			        if (mainEmp == null) {

			            Employee mainEmpData = employeeRepository.findByEmpId(mainEmpId);
			            if (mainEmpData == null) {
			                continue; 
			            }

			            mainEmp = new IshineToPoEmployeeDTO();
	  
			            mainEmp.setEmpId("true".equalsIgnoreCase(mainEmpData.getIsApmosysProduct())
			                 ? "AP-" + mainEmpData.getEmployeementId(): "A-" + mainEmpData.getEmployeementId());
			            
			            mainEmp.setEmpName(mainEmpData.getName());
			            mainEmp.setRoleName(emp.getRoleName());
			            mainEmp.setExp(emp.getExp());
			            mainEmp.setDepartmentName(emp.getDepartmentName());
			            mainEmp.setRoleId(emp.getRoleId());
			            mainEmp.setClientSideId(emp.getClientSideId());
			            mainEmp.setIsApmosysProduct(emp.getIsApmosysProduct());

			            mainEmp.setNoOfWorkingDays(0L);
			            mainEmp.setBillableDays(0L);
			            mainEmp.setStartDate(emp.getStartDate());
			            mainEmp.setEndDate(emp.getEndDate());
			            mainEmp.setPoId(emp.getPoId());
			            mainEmp.setIshineEmpId(mainEmpData.getEmpId());

			            empMap.put(mainEmpId, mainEmp);
			        }

	    mainEmp.setNoOfWorkingDays(mainEmp.getNoOfWorkingDays() + emp.getNoOfWorkingDays());
		mainEmp.setBillableDays(mainEmp.getNoOfWorkingDays());
		
		if (mainEmp.getStartDate() == null || emp.getStartDate().isBefore(mainEmp.getStartDate())) {
		    mainEmp.setStartDate(emp.getStartDate());
		}

		if (mainEmp.getEndDate() == null || emp.getEndDate().isAfter(mainEmp.getEndDate())) {
		    mainEmp.setEndDate(emp.getEndDate());
		}
		
		mainEmp.setMsg("Shadow's Timesheet Count Added with the Resource!!");	        
		empMap.remove(shadowEmpId);
			    }
			}

			employees = new ArrayList<>(empMap.values());
			dto.setProjectName(ishineToPoRequest.getProjectName());
			dto.setProjectId(ishineToPoRequest.getProjectId());
			dto.setPoId(ishineToPoRequest.getPoId());	
			dto.setStartDateOfBilling(ishineToPoRequest.getStartDateOfBilling());
			dto.setEndDateOfBilling(ishineToPoRequest.getEndDateOfBilling());	
		    dto.setEmployees(employees);	
			
			response.setServiceResponse(dto);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			return response;
			
		} catch (Exception e) {
			ExceptionLogContext.add(e);
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Something went wrong!!");
		}finally {
			if (initialLog != null) {
				apiLogUtility.endLog(initialLog.getId(), sourceSystem, finalHttpStatusCode, ExceptionLogContext.get(),
						httpRequest);
			}
		}
		return response;
	}
	
	private LocalDate convertToLocalDate(Date date) {
	    return date.toInstant()
	               .atZone(ZoneId.systemDefault())
	               .toLocalDate();
	}
	
	private List<ProjectFetchDTO> groupOfPoDetilasByProject(List<ProjectFetchDTO> list) {

		if (list == null || list.isEmpty()) {
			return list;
		}
	
		Map<Long, ProjectFetchDTO> grouped = new LinkedHashMap<>();
	
		for (ProjectFetchDTO row : list) {
	
			Long projectId = row.getProjectId() != null
					? ((Number) row.getProjectId()).longValue()
					: null;
	
			if (projectId == null) {
				continue;
			}
	
			if (!grouped.containsKey(projectId)) {
				grouped.put(projectId, row);
			} else {
				ProjectFetchDTO base = grouped.get(projectId);
	
				// MySQL: GROUP_CONCAT(DISTINCT po_no)
				base.setPoNo(
						groupConcatDistinct(base.getPoNo(), row.getPoNo())
				);
	
				// MySQL: GROUP_CONCAT(DISTINCT apmosys_rm)
				base.setApmosysRM(
						groupConcatDistinct(base.getApmosysRM(), row.getApmosysRM())
				);
	
				// MySQL: GROUP_CONCAT(DISTINCT client_rm)
				base.setClientRM(
						groupConcatDistinct(base.getClientRM(), row.getClientRM())
				);
			}
		}
	
		return new ArrayList<>(grouped.values());
	}

	private List<RMGFlatEmployeeProjectTeamDTO> groupExceptionEmployeeReport(
        List<RMGFlatEmployeeProjectTeamDTO> list) {

    if (list == null || list.isEmpty()) {
        return list;
    }

    Map<String, RMGFlatEmployeeProjectTeamDTO> grouped = new LinkedHashMap<>();

    for (RMGFlatEmployeeProjectTeamDTO row : list) {

        String key = row.getEmployeementId() + "_" + row.getProjectId();

        if (!grouped.containsKey(key)) {
            grouped.put(key, row);
        } else {
            RMGFlatEmployeeProjectTeamDTO base = grouped.get(key);

            // GROUP_CONCAT(DISTINCT po_no)
            base.setPoNo(
                groupConcatDistinct(base.getPoNo(), row.getPoNo())
            );

            // GROUP_CONCAT(DISTINCT apmosys_rm)
            base.setApmosysRM(
                groupConcatDistinct(base.getApmosysRM(), row.getApmosysRM())
            );

            // GROUP_CONCAT(DISTINCT client_rm)
            base.setClientRM(
                groupConcatDistinct(base.getClientRM(), row.getClientRM())
            );
        }
    }

    return new ArrayList<>(grouped.values());
}

	private List<RMGFlatEmployeeProjectTeamDTO> groupEmployeeProjectTeamWise(
			List<RMGFlatEmployeeProjectTeamDTO> list) {
		if (list == null || list.isEmpty()) {
			return list;
		}

		Map<String, RMGFlatEmployeeProjectTeamDTO> grouped = new LinkedHashMap<>();
		for (RMGFlatEmployeeProjectTeamDTO row : list) {
			String key = row.getEmpId() + "_" +
					row.getProjectId() + "_" +
					row.getTeamId();

			if (!grouped.containsKey(key)) {
				grouped.put(key, row);
			} else {
				RMGFlatEmployeeProjectTeamDTO base = grouped.get(key);
				base.setPoNo(groupConcatDistinct(base.getPoNo(), row.getPoNo()));
				base.setApmosysRM(groupConcatDistinct(base.getApmosysRM(), row.getApmosysRM()));
				base.setClientRM(groupConcatDistinct(base.getClientRM(), row.getClientRM()));
			}
		}
		return new ArrayList<>(grouped.values());
	}


	private String groupConcatDistinct(String existing, String incoming) {
		if (incoming == null || incoming.isBlank()) {
			return existing;
		}
		if (existing == null || existing.isBlank()) {
			return incoming;
		}
	
		Set<String> set = new LinkedHashSet<>();
		for (String s : existing.split(",")) {
			set.add(s.trim());
		}
		for (String s : incoming.split(",")) {
			set.add(s.trim());
		}
		return String.join(", ", set);
	}

 	public ServiceResponse getResourceRequirementCountByPoId(Long poId, Integer projectId, String projectType) {
		ServiceResponse serviceResponse = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setLogLevel("INFO");
		try {
			if (projectId == null) {
				return failResponse(serviceResponse, apiLogInfo, "Project Id cannot be null!!");
			}
			if (projectType == null || StringUtils.isEmpty(projectType)) {
				return failResponse(serviceResponse, apiLogInfo, "Project type cannot be null!!");
			}
			PoDetailsDto poDetailsDto = null;
			List<PoDetailsDto> projectConfigurationDetailsList = new ArrayList<>();
			if (projectType.equalsIgnoreCase("TNM")) {
				projectConfigurationDetailsList = projectPoDetailsRepository
						.getResourceRequirementCountByPoIdAndProjectId(poId, projectId);
			} else {
				projectConfigurationDetailsList = projectPoDetailsRepository
						.getResourceRequirementCountByProjectId(projectId);
			}

			if (projectConfigurationDetailsList != null && !projectConfigurationDetailsList.isEmpty()) {
				poDetailsDto = projectConfigurationDetailsList.get(0);
				if (poDetailsDto != null && projectType.equalsIgnoreCase("TNM")) {
					poDetailsDto.setTotalRequirements(poRequirementMappingRepository.getTotalActiveRequiredCountByPoIdAndProjectId(poId, projectId));
				}
			} else {
				return failResponse(serviceResponse, apiLogInfo, "Unable to fetch latest requirment count!!");
			}
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			serviceResponse.setServiceResponse(poDetailsDto);
		} catch (Exception e) {
			e.printStackTrace();
			return failResponse(serviceResponse, apiLogInfo,
					"Unable to fetch latest requirment count.Something went wrong!!");
		}
		return serviceResponse;
	}
 	
 	
         public void liftAndShiftTeamNew(IshineLinkProjectDto payloadDTO) {
		
		ProjectPoMappingWithResourceDTO primaryProjectDTO = payloadDTO.getPrimaryProject();
		List<Object[]> primaryTeams = projectRepository.getTeamIdsForPoProjectId(primaryProjectDTO.getProjectId());
		String ishineProjectStatus = "";

		Set<String> primaryTeamNames = (primaryTeams == null || primaryTeams.isEmpty()) ? Collections.emptySet()
				: primaryTeams.stream().map(t -> t[1] != null ? t[1].toString() : null).filter(Objects::nonNull)
						.collect(Collectors.toSet());
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		
		PoDetailsForProjectPoMappingDTO poDto =
				payloadDTO.getDeletedProjects().get(0).getPoDetailsList().get(0);

	    Long updatedBy = validationService.
	            validateAndGetEmployeeEmpId(
	                    poDto.getUpdatedByEmpId(),
	                    poDto.getUpdatedByEmpName());
		
		for (ProjectPoMappingWithResourceDTO deletedProject : payloadDTO.getDeletedProjects()) {
			List<Object[]> deletedTeams = projectRepository
					.getTeamIdsForPoProjectId(deletedProject.getProjectId());
			List<Long> deletedTeamIds = new ArrayList<>();
			if (!deletedTeams.isEmpty() || deletedTeams != null) {
				for (Object[] team : deletedTeams) {
					Long teamId = team[0] != null ? Long.parseLong(team[0].toString()) : null;
					String teamName = team[1] != null ? team[1].toString() : null;

					String newTeamName = primaryTeamNames.contains(teamName)
							? teamName + " | " + deletedProject.getProjectName()
							: teamName;

					teamRepository.updateTeamName(teamId, newTeamName);
					deletedTeamIds.add(teamId);
				}
			}
			
			if (!deletedTeamIds.isEmpty()) {
				LiftAndShiftTeamsDTO liftAndShiftDTO = new LiftAndShiftTeamsDTO();
				liftAndShiftDTO.setTeamIds(deletedTeamIds);
				Project sourceProject = projectRepository.findByPoProjectId(deletedProject.getProjectId());
				liftAndShiftDTO.setSourceProjectId(sourceProject.getProjectId());
				Project targetProject = projectRepository.findByPoProjectId(primaryProjectDTO.getProjectId());
				liftAndShiftDTO.setTargetProjectId(targetProject.getProjectId());
				liftAndShiftDTO.setCurrentUserEmpId(updatedBy);
				
				
				


			     context.getBean(getClass()).liftAndShiftTeamsOneByone(liftAndShiftDTO);
			}
			
			
			
		
		}
	}
         
         
         
         public void liftAndShiftTeamsOneByone(LiftAndShiftTeamsDTO dto) {
        	 
        	 
        	 Project sourceProject = projectRepository.findByProjectId(dto.getSourceProjectId());
 			Project targetProject = projectRepository.findByProjectId(dto.getTargetProjectId());
 			
 			// maintain hasclientsideid
 			if (sourceProject != null && targetProject != null) {
 				targetProject.setHasClientSideId(sourceProject.getHasClientSideId());
 				projectRepository.save(targetProject);
 			}
 			
 			// po department mapping
 			List<PoDepartmentMapping> sourceMappings =
 		            poDepartmentMappingRepository
 		                    .findByProjectIdAndActiveTrue(sourceProject.getProjectId());	
 			
 			if (sourceMappings == null || sourceMappings.isEmpty()) {
 		        return;
 		    }

 		   
 		    for (PoDepartmentMapping old : sourceMappings) {
 		        old.setActive(false);
 		    }
 		    poDepartmentMappingRepository.saveAll(sourceMappings);
 			 
 			 
 			List<PoDepartmentMapping> newMappings = new ArrayList<>();

 		    for (PoDepartmentMapping old : sourceMappings) {

 		        PoDepartmentMapping nm = new PoDepartmentMapping();
 		        nm.setPoId(old.getPoId());
 		        nm.setDeptId(old.getDeptId());
 		        nm.setProjectId(targetProject.getProjectId());	      
 		        nm.setActive(true);
 		        newMappings.add(nm);
 		    }
 		   poDepartmentMappingRepository.saveAll(newMappings);
 		   
 		   //project manager mappings
 		   
 		  List<ProjectManagerMapping> sourceManagerMappings = projectManagerMappingRepository
					.findByProjectIdAndActive(Long.parseLong(dto.getSourceProjectId().toString()), 1);
			List<ProjectManagerMapping> targetManagerMappings = projectManagerMappingRepository
					.findByProjectIdAndActive(Long.parseLong(dto.getTargetProjectId().toString()), 1);
			Set<Long> targetManagerIds = targetManagerMappings.stream().map(ProjectManagerMapping::getProjectManagerId)
					.collect(Collectors.toSet());
			List<ProjectManagerMapping> newManagerMappings = new ArrayList<>();
			for (ProjectManagerMapping s : sourceManagerMappings) {
				if (!targetManagerIds.contains(s.getProjectManagerId())) {
					ProjectManagerMapping nm = new ProjectManagerMapping();
					nm.setProjectId(Long.parseLong(dto.getTargetProjectId().toString()));
					nm.setProjectManagerId(s.getProjectManagerId());
					nm.setActive(1);
					nm.setCreatedBy(dto.getCurrentUserEmpId());
					nm.setCreatedOn(new Timestamp(System.currentTimeMillis()));
					newManagerMappings.add(nm);
				}
			}
			if (!newManagerMappings.isEmpty())
				projectManagerMappingRepository.saveAll(newManagerMappings);
			
			
			//project overheadmappings
			List<ProjectOverheadMapping> sourceOverheadMappings = projectOverheadMappingRepository
					.findByProjectIdAndActive(Long.parseLong(dto.getSourceProjectId().toString()), 1);
			List<ProjectOverheadMapping> targetOverheadMappings = projectOverheadMappingRepository
					.findByProjectIdAndActive(Long.parseLong(dto.getTargetProjectId().toString()), 1);
			Set<Long> targetOverheadIds = targetOverheadMappings.stream()
					.map(ProjectOverheadMapping::getProjectOverheadId).collect(Collectors.toSet());
			List<ProjectOverheadMapping> newOverheadMappings = new ArrayList<>();
			for (ProjectOverheadMapping s : sourceOverheadMappings) {
				if (!targetOverheadIds.contains(s.getProjectOverheadId())) {
					ProjectOverheadMapping nm = new ProjectOverheadMapping();
					nm.setProjectId(Long.parseLong(dto.getTargetProjectId().toString()));
					nm.setProjectOverheadId(s.getProjectOverheadId());
					nm.setActive(1);
					nm.setCreatedBy(dto.getCurrentUserEmpId());
					nm.setCreatedOn(new Timestamp(System.currentTimeMillis()));
					newOverheadMappings.add(nm);
				}
			}
			if (!newOverheadMappings.isEmpty())
				projectOverheadMappingRepository.saveAll(newOverheadMappings);
			
 		   
			List<Team> activeTeams = teamRepository.findActiveTeamsByTeamIds(dto.getTeamIds());
			if (activeTeams.isEmpty()) {
//				return;    //because prev returned success
				return;
			}
			List<EmployeeTeamMap> oldEmployeeTeamMaps = employeeTeamMapRepository
					.activeAndPendingEmployeesByTeamIds(dto.getTeamIds());
			// activities for old teams
			List<Activity> oldActivities = activitiesRepository.findByTeamIdIn(dto.getTeamIds());
			if (oldEmployeeTeamMaps != null || !oldEmployeeTeamMaps.isEmpty()) {
				// derive empIds from oldEmployeeTeamMaps
				List<Long> empIds = oldEmployeeTeamMaps.stream().map(EmployeeTeamMap::getEmpId).distinct()
						.collect(Collectors.toList());

				// --- 4. Mark old teams inactive (persist) ---
				LocalDateTime now = LocalDateTime.now();
				activeTeams.forEach(t -> {
					t.setIsActive("N");
					t.setUpdatedBy(dto.getCurrentUserEmpId());
					t.setUpdatedOn(now);
				});
				teamRepository.saveAll(activeTeams);

				// --- 5. Create new teams for target project (set oldTeamId transient) ---
				List<Team> newTeams = activeTeams.stream().map(oldTeam -> {
					Team newTeam = new Team();
					newTeam.setTeamName(oldTeam.getTeamName());
					newTeam.setTeamLeadId(oldTeam.getTeamLeadId());
					newTeam.setProjectId(dto.getTargetProjectId());
					newTeam.setTeamLeadName(oldTeam.getTeamLeadName());
					newTeam.setIsActive("Y");
					newTeam.setDescription(oldTeam.getDescription());
					newTeam.setDeptIds(oldTeam.getDeptIds());
					newTeam.setSpocId(oldTeam.getSpocId());
					newTeam.setCreatedBy(dto.getCurrentUserEmpId());
					newTeam.setCreatedOn(Timestamp.valueOf(LocalDateTime.now()));
					newTeam.setOldTeamId(oldTeam.getTeamId()); // transient helper
					newTeam.setPoId(oldTeam.getPoId());	
					
					return newTeam;
				}).collect(Collectors.toList());

				List<Team> createdTeams = teamRepository.saveAll(newTeams);
				if (createdTeams.isEmpty()) {
					
					throw new RuntimeException("Failed to create new teams");
//					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//					response.setServiceResponse("Failed to shift teams to new project");
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//					apiLogInfo.setApiResponse("Failed to create new teams");
//					apiLogInfo.setLogLevel("FAIL");
//					logService.logMyInfo(httpRequest, apiLogInfo);
//					return response;
				}

				// Build oldTeamId -> newTeam entity map
				Map<Long, Team> oldToNewTeamMap = createdTeams.stream().filter(t -> t.getOldTeamId() != null)
						.collect(Collectors.toMap(Team::getOldTeamId, Function.identity()));

				// --- 6. Employee–Team mappings update ---
				// Mark old mappings inactive (we already fetched oldEmployeeTeamMaps before)
				if (oldEmployeeTeamMaps != null || !oldEmployeeTeamMaps.isEmpty()) {
					oldEmployeeTeamMaps.forEach(etm -> {
						etm.setActive(0L);
						etm.setUpdatedOn(LocalDateTime.now());
						etm.setUpdatedBy(dto.getCurrentUserEmpId());
					});
				}
				employeeTeamMapRepository.saveAll(oldEmployeeTeamMaps);

				// Create new mappings using oldToNewTeamMap
				List<EmployeeTeamMap> newEmployeeTeamMaps = new ArrayList<>();
				for (EmployeeTeamMap oldMap : oldEmployeeTeamMaps) {
					Team mappedNewTeam = oldToNewTeamMap.get(oldMap.getTeamId());
					if (mappedNewTeam != null) {
						EmployeeTeamMap newMap = new EmployeeTeamMap();
						newMap.setEmpId(oldMap.getEmpId());
						newMap.setTeamId(mappedNewTeam.getTeamId());
						newMap.setJobRoleId(oldMap.getJobRoleId());;
						newMap.setStartDate(LocalDateTime.now());
						newMap.setEmployeeRole(oldMap.getEmployeeRole());
						newMap.setEndDate(null);
						newMap.setUpdatedOn(null);
						newMap.setUpdatedBy(null);
						newMap.setPoId(oldMap.getPoId());
						
						// added storing poRequirementMapping Id
						if (oldMap.getPoRequirementMappingId() != null) {
							newMap.setPoRequirementMappingId(oldMap.getPoRequirementMappingId());
						}

						newMap.setIsShadow(oldMap.getIsShadow());
						newMap.setCreatedBy(dto.getCurrentUserEmpId());
						newMap.setCreatedOn(Timestamp.valueOf(LocalDateTime.now()));
						newEmployeeTeamMaps.add(newMap);
					}
				}
				if (!newEmployeeTeamMaps.isEmpty())
					employeeTeamMapRepository.saveAll(newEmployeeTeamMaps);

				if (empIds != null || !empIds.isEmpty()) {
					// --- 7. Employee primary project mapping update (deactivate old, create new)
					// ---
					List<EmpPrimaryProjectMapping> oldPrimaryMappings = empPrimaryProjectMappingRepository
							.findByEmpIdInAndPrimaryProjectIdInAndIsMapped(empIds,
									Collections.singletonList(dto.getSourceProjectId().longValue()), "Y");

					boolean primaryMappingsChanged = false;
					if (!oldPrimaryMappings.isEmpty()) {
						oldPrimaryMappings.forEach(m -> {
							m.setIsMapped("N");
							m.setUpdatedOn(LocalDateTime.now());
							m.setUpdatedBy(dto.getCurrentUserEmpId());
						});
						empPrimaryProjectMappingRepository.saveAll(oldPrimaryMappings);

						List<EmpPrimaryProjectMapping> newPrimaryMappings = new ArrayList<>();
						for (EmpPrimaryProjectMapping oldMap : oldPrimaryMappings) {
							EmpPrimaryProjectMapping newMap = new EmpPrimaryProjectMapping();
							newMap.setEmpId(oldMap.getEmpId());
							newMap.setPrimaryProjectId(dto.getTargetProjectId().longValue());
							newMap.setPrimaryProjectName(oldMap.getPrimaryProjectName());
							newMap.setIsMapped("Y");
							newMap.setUpdatedBy(dto.getCurrentUserEmpId());
							newMap.setUpdatedOn(LocalDateTime.now());
							
							
							newPrimaryMappings.add(newMap);
						}
						empPrimaryProjectMappingRepository.saveAll(newPrimaryMappings);
						primaryMappingsChanged = true;
					}

					// --- 8. Only after primary mappings changed, update billable/billableType on
					// employees ---
					if (primaryMappingsChanged) {
						Project refreshedTarget = projectRepository.findByProjectId(dto.getTargetProjectId());
						String poProjectType = (refreshedTarget != null ? refreshedTarget.getPoProjectType() : null);
						String ishineProjectType = (refreshedTarget != null ? refreshedTarget.getInternalProjectType()
								: null);

						String billable = null;
						String billableType = null;

						if (poProjectType != null) {
							switch (poProjectType) {
							case "Fixed Cost":
								billable = "No";
								billableType = "Fixed Cost";
								break;
							case "TNM":
								billable = "Yes";
								billableType = "TNM";
								break;
							case "Monitoring":
								billable = "No";
								billableType = "Fixed Cost";
								break;
							default:
								billable = null;
								billableType = null;
							}
						} else if (ishineProjectType != null) {
							switch (ishineProjectType) {
							case "InternalRNDProducts":
								billable = "No";
								billableType = "InternalRNDProducts";
								break;
							case "Bench":
								billable = "No";
								billableType = "Bench";
								break;
							default:
								billable = null;
								billableType = null;
							}
						}

						if (billable != null || billableType != null) {
							employeeRepository.updateBillableAndTypeForEmpIds(billable, billableType, empIds);
						}
					}
				}

				// --- 9. Employee client-side ID mappings ---
				List<EmployeeClientSideIdMapping> oldClientSideMappings = employeeClientSideIdMappingRepository
						.findByEmpIdInAndProjectIdInAndActive(empIds,
								Collections.singletonList(dto.getSourceProjectId().longValue()), true);

				if (!oldClientSideMappings.isEmpty()) {
					oldClientSideMappings.forEach(m -> {
						m.setActive(false);
						m.setUpdatedOn(LocalDateTime.now());
						m.setUpdatedBy(dto.getCurrentUserEmpId());
					});
					employeeClientSideIdMappingRepository.saveAll(oldClientSideMappings);

					List<EmployeeClientSideIdMapping> newClientSideMappings = new ArrayList<>();
					for (EmployeeClientSideIdMapping oldMap : oldClientSideMappings) {
						EmployeeClientSideIdMapping newMap = new EmployeeClientSideIdMapping();
						newMap.setClientSideId(oldMap.getClientSideId());
						newMap.setEmpId(oldMap.getEmpId());
						newMap.setProjectId(dto.getTargetProjectId().longValue());
						newMap.setActive(true);
						newMap.setCreatedBy(dto.getCurrentUserEmpId());
						newMap.setCreatedOn(LocalDateTime.now());
						newClientSideMappings.add(newMap);
					}
					employeeClientSideIdMappingRepository.saveAll(newClientSideMappings);
				}

				// --- 10. Activities: recreate for new teamIds (we fetched oldActivities
				// earlier) ---
				List<Activity> newActivities = new ArrayList<>();
				for (Activity oldAct : oldActivities) {
					Team newTeam = oldToNewTeamMap.get(oldAct.getTeamId());
					if (newTeam != null) {
						Activity newAct = new Activity();
						newAct.setTeamId(newTeam.getTeamId());
						newAct.setActivity(oldAct.getActivity());
						newAct.setEta(oldAct.getEta());
						newAct.setEmployeeRole(oldAct.getEmployeeRole());
						newAct.setDeptIds(oldAct.getDeptIds());
						CommonProperties cp = new CommonProperties();
						cp.setCreatedBy(dto.getCurrentUserEmpId());
						cp.setCreatedOn(Timestamp.valueOf(LocalDateTime.now()));
						newAct.setCommonProperty(cp);
						newActivities.add(newAct);
					}
				}

				if (!newActivities.isEmpty())
					activitiesRepository.saveAll(newActivities);
			}
 		   
 		   
 		   
 		   
         }
         
         
         public String ishineStatusReturn(List<ProjectPoMappingWithResourceDTO> deletedProjects,Project primaryProject) {
        	 
        	 String ishineProjectStatus="";
        	 
        	 for (ProjectPoMappingWithResourceDTO deletedProject : deletedProjects) {
					Project deletedProjEntity = projectRepository.findByPoProjectId(deletedProject.getProjectId());
					if (deletedProjEntity != null) {
						String primaryState = getProjectStatusState(primaryProject);
						String deletedState = getProjectStatusState(deletedProjEntity);

						String resolvedState = resolveProjectStatusState(primaryState, deletedState);

						if ("Pending".equals(resolvedState)) {
							primaryProject.setIsDraftProject("true");
							updateEmployeeTeamMapStatus(primaryProject.getProjectId());
						} else if ("Approved".equals(resolvedState)) {
							primaryProject.setIsDraftProject("false");
						} else if ("Rejected".equals(resolvedState)) {
							primaryProject.setIsDraftProject("Rejected");
						} else if ("Not Started".equals(resolvedState)) {
							primaryProject.setIsDraftProject(null);
						} else if ("Completed".equals(resolvedState)) {
							primaryProject.setProjectStatus("Completed");
							updateProjectActiveField(primaryProject.getProjectId());
						}

						ishineProjectStatus = getIshineProjectStatus(resolvedState);
					}
				}
        	 return ishineProjectStatus;
         }
         
      

         




  
 	
 	
 	

}
