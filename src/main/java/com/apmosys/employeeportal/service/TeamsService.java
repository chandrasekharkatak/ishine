package com.apmosys.employeeportal.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.Session;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.Exception.BadRequestException;
import com.apmosys.employeeportal.dto.ActivityDTO;
import com.apmosys.employeeportal.dto.ActivityTemplateDTO;
import com.apmosys.employeeportal.dto.BillableInfo;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.EmployeeDetailsForTeamMemberDTO;
import com.apmosys.employeeportal.dto.EmployeeInformationDTO;
import com.apmosys.employeeportal.dto.EmployeeJobRoleDept;
import com.apmosys.employeeportal.dto.EmployeeOtherActiveProject;
import com.apmosys.employeeportal.dto.EmployeeTeamMapDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.MigrateTeam;
import com.apmosys.employeeportal.dto.PoDetailsDto;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.RmgMemberEndDateDto;
import com.apmosys.employeeportal.dto.RmgTeamDto;
import com.apmosys.employeeportal.dto.RmgTeamMemberDto;
import com.apmosys.employeeportal.dto.TeamDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.model.Activity;
import com.apmosys.employeeportal.model.ActivityTemplate;
import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.ClientLocation;
import com.apmosys.employeeportal.model.CommonProperties;
import com.apmosys.employeeportal.model.CompOffLeave;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.EmpPrimaryProjectMapping;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeClientSideIdMapping;
import com.apmosys.employeeportal.model.EmployeeLeave;
import com.apmosys.employeeportal.model.EmployeeLeavesMap;
import com.apmosys.employeeportal.model.EmployeeTeamMap;
import com.apmosys.employeeportal.model.EmployeeTimesheetsNew;
import com.apmosys.employeeportal.model.LeaveBalanceLog;
import com.apmosys.employeeportal.model.LeavePolicyMaster;
import com.apmosys.employeeportal.model.LeaveTypeMaster;
import com.apmosys.employeeportal.model.PoDepartmentMapping;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectDepartmentMap;
import com.apmosys.employeeportal.model.ProjectManagerMapping;
import com.apmosys.employeeportal.model.ProjectOverheadMapping;
import com.apmosys.employeeportal.model.ProjectPoDetails;
import com.apmosys.employeeportal.model.RoleDetails;
import com.apmosys.employeeportal.model.Team;
import com.apmosys.employeeportal.repository.ActivitiesRepository;
import com.apmosys.employeeportal.repository.ActivityTemplateRepository;
import com.apmosys.employeeportal.repository.ClientLocationRepository;
import com.apmosys.employeeportal.repository.ClientsRepository;
import com.apmosys.employeeportal.repository.CompOffLeaveRepository;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmpPrimaryProjectMappingRepository;
import com.apmosys.employeeportal.repository.EmployeeClientSideIdMappingRepository;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeLeavesMapRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.EmployeeTimesheetsNewRepository;
import com.apmosys.employeeportal.repository.LeaveBalanceLogRepository;
import com.apmosys.employeeportal.repository.LeavePolicyMasterRepository;
import com.apmosys.employeeportal.repository.LeaveTypeMasterRepository;
import com.apmosys.employeeportal.repository.PoDepartmentMappingRepository;
import com.apmosys.employeeportal.repository.PoRequirementMappingRepository;
import com.apmosys.employeeportal.repository.ProjectDepartmentMapRepository;
import com.apmosys.employeeportal.repository.ProjectManagerMappingRepository;
import com.apmosys.employeeportal.repository.ProjectOverheadMappingRepository;
import com.apmosys.employeeportal.repository.ProjectPoDetailsRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.RoleDetailsRepository;
import com.apmosys.employeeportal.repository.TeamRepository;
import com.apmosys.employeeportal.repository.TimesheetsRepository;
import com.apmosys.employeeportal.utility.EmployeeHirarchyCache;
import com.apmosys.employeeportal.utility.LeaveLogMessage;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;
import com.apmosys.employeeportal.utility.TypeConversionUtil;

@Service
public class TeamsService {

	@Autowired
	ProjectRepository projectRepository;
	
	@Autowired
	ProjectPoDetailsRepository projectPoDetailsRepository;
	
	@Autowired
	PoDepartmentMappingRepository poDepartmentMappingRepository;

	@Autowired
	TeamRepository teamRepository;
	
	@Autowired
	EmployeeRepository employeeRepository;
	
	@Autowired
	EmployeeLeaveRepository employeeLeaveRepository;

	@Autowired
	EmployeeTeamMapRepository employeeTeamMapRepository;
	
	@Autowired
	EmployeeLeavesMapRepository employeeLeavesMapRepository;
	
	@Autowired
	LeaveBalanceLogRepository leaveBalanceLogRepository;
	
	@Autowired
	ActivitiesRepository activitiesRepository;
	
	@Autowired
	ClientsRepository clientsRepository;
	
	@Autowired
	ClientLocationRepository clientLocationRepository;

	@Autowired
	ModelMapper modelMapper;

	@Autowired
	StringToDateTimeParser stringToDateTimeParser;
	
	@Autowired
	ActivityTemplateRepository activityTemplateRepository;
	
	@Autowired
	DepartmentRepository departmentRepository;
	
	@Autowired
	ProjectDepartmentMapRepository projectDepartmentMapRepository;
	
	@Autowired
	TimesheetsRepository timesheetsRepository;
	
	@Autowired
	EmployeeTimesheetsNewRepository timesheetsRepositoryNew;

	@PersistenceContext
    private EntityManager entityManager;
	
	@Autowired
	private MailService mailService;
	
	@Value("${hr.mail}")
	private String hrMailAddress;
	
	@Value("${valid.attempt}")
	private Integer failedAttempt;
	
	@Value("${rmg.mail}")
	private String rmgMail;

	@Value("${admin.mail}")
	private String adminMail;

	@Value("${bd.mail}")
	private String bdMail;

	@Value("${finance.mail}")
	private String financeMail;

	@Autowired
	LeaveTypeMasterRepository leaveTypeMasterRepository;
	
	@Autowired
	LeavePolicyMasterRepository leavePolicyMasterRepository;
	
	@Autowired
	CompOffLeaveRepository compOffLeaveRepository;
	
	@Autowired
	private HttpServletRequest httpRequest;

	@Autowired
	private LogService logService;
	 
	@Autowired EmployeeHirarchyCache empCache;
	
	@Autowired
	private ApplicationContext context;

	@Autowired
	private PoRequirementMappingRepository poRequirementMappingRepository;

	@Autowired
	private ProjectManagerMappingRepository projectManagerMappingRepository;

	@Autowired
	private ProjectOverheadMappingRepository projectOverheadMappingRepository;

	@Autowired
	private EmployeeClientSideIdMappingRepository employeeClientSideIdMappingRepository;

	@Autowired
	private EmpPrimaryProjectMappingRepository empPrimaryProjectMappingRepository;

	@Autowired
	private RoleDetailsRepository roleDetailsRepository;
	
	private static final Logger log = LoggerFactory.getLogger(TeamsService.class);

	@Value("${timesheet.check.period}")
	private String timesheetCheckPeriod;
	
	@Value("${app.team.fullPrivilegeRoleIds}")
	private String fullPrivilegeRoleIdsConfig;

	@Value("${maximum.timesheetCanBeFilledByMember}")
	private String maximumTimesheetCanBeFilledByTeamMember;

	public ServiceResponse getAllProjectListByProjectManagerId(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        //apiLogInfo.setSubFeatureName(""); 
        apiLogInfo.setApiUrl("/api/getAllProjectListByProjectManagerId");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("ProjectManagerId : "+ timesheetDTO.getProjectManagerId());
        
		try {

			List<Object[]> projectList = projectRepository.getAllProject();
			
			if(projectList != null) {
				List<ProjectDTO> dtoList = new ArrayList<ProjectDTO>();
				
				projectList.forEach((object) -> {
					ProjectDTO projectDto = new ProjectDTO();
					
					projectDto.setProjectId(object[0] != null ? Integer.valueOf(object[0].toString()) : null);
					projectDto.setEmployeeName(object[1] != null ? object[1].toString() : null);
					projectDto.setProjectName(object[2] != null ? object[2].toString() : null);
					projectDto.setState(object[3] != null ? object[3].toString() : null);
					projectDto.setDepartmentName(object[4] != null ? object[4].toString() : null);
					projectDto.setClientName(object[5] != null ? object[5].toString() : null);
					projectDto.setClientLocation(object[6] != null ? object[6].toString() : null);
					
					dtoList.add(projectDto);
				});
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("Project List Fetched :" + dtoList.size());			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Projects found.");
				apiLogInfo.setApiResponse("No projects found");			
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

	@Transactional
	public ServiceResponse createTeam(TeamDTO teamDTO) {
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("createTeam");
        apiLogInfo.setApiUrl("/api/createTeam");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("TeamId : " + teamDTO.getTeamId() + "  ,TeamName : " + teamDTO.getTeamName()
         + " ,TeamLeadName :" + teamDTO.getTeamLeadName());
		try {

			String teamLeadName = null;
			
			if(teamDTO.getTeamLeadId() != null) {
				 
				Optional<Employee> getTeamLeadData = employeeRepository.findById(teamDTO.getTeamLeadId());
				if(!getTeamLeadData.isEmpty()) {
					Employee empObj = getTeamLeadData.get();
					
					teamLeadName = empObj.getName();
				}
			}else {
				teamLeadName = "NA";
			}
			
			Team newTeam = new Team();
			
			// Multiple department
			StringBuilder department = new StringBuilder("");
			for(String deptId: teamDTO.getDepartmentList()) {
				department.append(deptId).append(",");
			}

			newTeam.setTeamName(teamDTO.getTeamName());
			newTeam.setTeamLeadId(teamDTO.getTeamLeadId());
			newTeam.setProjectId(teamDTO.getProjectId());
			newTeam.setTeamLeadName(teamLeadName);
			newTeam.setCreatedBy(teamDTO.getCreatedBy());
			newTeam.setCreatedOn(new Timestamp(System.currentTimeMillis())); 
			newTeam.setIsActive("Y");
			newTeam.setDeptIds(department.toString());
			
			Team teamCreated = teamRepository.save(newTeam);

			if (teamCreated != null) {
				
				List<EmployeeTeamMapDTO> teamMembersList = teamDTO.getAllTeamMemberList();

				Optional.ofNullable(teamMembersList).ifPresentOrElse((list) -> {

					if (list.isEmpty()) {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse(
								"Team created but no team members added.Reason: Team members list was empty.");
						apiLogInfo.setApiResponse("Team Created but no team members added Reason: team members list was empty.");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

					} 
					else {
						Project projectObj = projectRepository.getById(teamDTO.getProjectId());
						List<EmployeeTeamMap> mapList = new ArrayList<>();
						List<Long> deptIds = new ArrayList<>();
						
						EmployeeTeamMap map = new EmployeeTeamMap();
						// Add teamLead
						if(teamDTO.getTeamLeadId() != null) {
							map.setActive(1l);
							map.setEmpId(teamDTO.getTeamLeadId());
							map.setTeamId(teamCreated.getTeamId());
							map.setEmployeeRole("TeamLead");
							mapList.add(map);
						}
						
						// Add HOD of selected Department
						for(String deptId: teamDTO.getDepartmentList()) {
							Department departmentObj = departmentRepository.getById(Long.parseLong(deptId));
							deptIds.add(Long.parseLong(deptId));
							if(departmentObj != null) {
								
								//Check if HOD is already added
								EmployeeTeamMap isFound = mapList.stream().filter(team -> departmentObj.getHodId().equals(team.getEmpId())).findFirst().orElse(null);
								
								if(isFound != null) {
									isFound.setEmployeeRole(isFound.getEmployeeRole()+","+"HOD");
								}else {
									map = new EmployeeTeamMap();
									map.setActive(1l);
									map.setEmpId(departmentObj.getHodId());
									map.setTeamId(teamCreated.getTeamId());
									map.setEmployeeRole("HOD");
									mapList.add(map);									
								}
								
							}
						}
						
						// Add project Manager
						if(projectObj != null) {
							
							//Check if ProjManager is already added
							EmployeeTeamMap isFound = mapList.stream().filter(team -> projectObj.getProjectManagerId().equals(team.getEmpId())).findFirst().orElse(null);
							
							if(isFound != null) {
								isFound.setEmployeeRole(isFound.getEmployeeRole()+","+"Manager");
							}else {
								map = new EmployeeTeamMap();
								map.setActive(1l);
								map.setEmpId(projectObj.getProjectManagerId());
								map.setTeamId(teamCreated.getTeamId());
								map.setEmployeeRole("Manager");
								mapList.add(map);								
							}
						}

						list.forEach((teamMember) -> {
							StringBuilder str = new StringBuilder("");
							for(String role: teamMember.getEmployeeRole()) {
								str.append(role).append(",");
							}
							EmployeeTeamMap teamMemberMap = new EmployeeTeamMap();
							List<Department> deptObj = departmentRepository.findByDeptIdIn(deptIds);
							
							//check If selected member are not HOD,projManager, TeamLead
							if(( (teamDTO.getTeamLeadId() == null) || (teamDTO.getTeamLeadId() != null && !teamDTO.getTeamLeadId().equals(teamMember.getEmpId())) ) && 
									!projectObj.getProjectManagerId().equals(teamMember.getEmpId())
									&& ( (deptObj.isEmpty()) || (!deptObj.isEmpty() && !deptObj.stream().anyMatch(o -> teamMember.getEmpId().equals(o.getHodId()))) )) {
								

								teamMemberMap.setEmpId(teamMember.getEmpId());
								teamMemberMap.setTeamId(teamCreated.getTeamId());
								teamMemberMap.setJobRoleId(teamMember.getJobRoleId());
								// 1: Active  0: InActive
								teamMemberMap.setActive((long) 1);
								teamMemberMap.setEmployeeRole(str.toString());
								mapList.add(teamMemberMap);
								
							}else {
								// If member already added as HOD,manager or teamLead --> check activity persona
								int index = IntStream.range(0, mapList.size())
									     .filter(i -> mapList.get(i).getEmpId().equals(teamMember.getEmpId()))
									     .findFirst()
									     .orElse(-1);
								
								if(!str.toString().contains(mapList.get(index).getEmployeeRole())) {
									str.append(mapList.get(index).getEmployeeRole()).append(",");
								}
								
								if(index >= 0) {
									mapList.get(index).setEmployeeRole(str.toString());
								}
							}
						});
						
						// Mapp activities to Team according to departments
						Activity newActivityCreated = null;
						for(String deptId: teamDTO.getDepartmentList()) {
							List<ActivityTemplate> activityTemplate = activityTemplateRepository.getByDeptId(Long.parseLong(deptId));
							if(!activityTemplate.isEmpty()) {
								
								for(ActivityTemplate object: activityTemplate) {
									Activity newActivity = new Activity();

									newActivity.setActivity(object.getTemplateActivity());
									newActivity.setTeamId(teamCreated.getTeamId());
									newActivity.setEmployeeRole(object.getEmployeeRole());
									newActivity.setDeptIds(object.getDeptId().toString());
									newActivity.getCommonProperty().setCreatedBy(teamDTO.getCreatedBy());

									newActivityCreated = activitiesRepository.save(newActivity);
								}
							}
						}

						if(newActivityCreated != null) {
							String message = employeeTeamMapRepository.saveAll(mapList).isEmpty()
									? "Team created but no team members added."
									: "Team created successfully.";
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse(message);
							apiLogInfo.setApiResponse(message);
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

						}else {
							String message = employeeTeamMapRepository.saveAll(mapList).isEmpty()
									? "Team created but no team members added."
									: "Team created successfully. But unable to map activities";
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse(message);
							apiLogInfo.setApiResponse(message);
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

						}
					}

				}, () -> {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse(
							"Team created but no team members added.Reason: Team members list was null");
					apiLogInfo.setApiResponse("team created but no team members added.Reason: team members list was null");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

				});

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Team creation failed.");
				apiLogInfo.setApiResponse("Team creation failed");
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

	public ServiceResponse getAllTeamsByProjectId(TeamDTO teamDTO) {
		ServiceResponse response = new ServiceResponse();

     LogDTO apiLogInfo = new LogDTO();
     //apiLogInfo.setSubFeatureName("");
     apiLogInfo.setApiUrl("/api/getAllTeamsByProjectId");
     apiLogInfo.setLogLevel("INFO");
     StringBuilder logBuilder = new StringBuilder();
     logBuilder.append("ProjectId : " + teamDTO.getProjectId());
		try {

			List<Object[]> objectList = teamRepository.projectTeamsByProjectId(teamDTO.getProjectId());

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No teams found. Teams list is empty");
					apiLogInfo.setApiResponse("No teams found. Teams list is empty");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					
				} else {
					
					List<TeamDTO> dtoList = new ArrayList<TeamDTO>();
						
						list.forEach((object) -> {
							TeamDTO dto = new TeamDTO();

							dto.setProjectId(object[0] != null ? Integer.parseInt(object[0].toString()) : null);
							dto.setProjectName(object[1] != null ? object[1].toString() : null);
							dto.setTeamId(object[2] != null ? Long.parseLong(object[2].toString()) : null);
							dto.setTeamName(object[3] != null ? object[3].toString() : null);
							dto.setTeamLeadId(object[4] != null ? Long.parseLong(object[4].toString()) : null);
							dto.setTeamLeadName(object[5] != null ? object[5].toString() : null);
							dto.setProjectManagerId(object[6] != null ? Long.parseLong(object[6].toString()) : null);
							dto.setProjectManagerName(object[7] != null ? object[7].toString() : null);
							dto.setDepartmentName(object[8] != null ? object[8].toString() : null);
							dto.setCreatedByName(object[9] != null ? object[9].toString() : null);
							dto.setCreatedOn(object[10] != null ? object[10].toString() : null);
							dto.setIsActive(object[11] != null ? object[11].toString() : null);
							dto.setTeamLeadDeptId(object[12] != null ? Long.parseLong(object[12].toString()) : null);
							dto.setDepartmentList(object[13] != null ? object[13].toString().split(",") : null);
							dtoList.add(dto);
						});
						
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("All Teams By ProjectId fetched" + dtoList.size());			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No teams found.Teams list is null");
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

	public ServiceResponse deleteTeam(TeamDTO teamDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("DeleteTeam");
		apiLogInfo.setApiUrl("/api/deleteTeam");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("TeamId : " + teamDTO.getTeamId() + " ,teamName:" + teamDTO.getTeamName());
		try {
			
			Optional<Team> team = teamRepository.findById(teamDTO.getTeamId());
			Long teamCount = employeeTeamMapRepository.countByTeamId(teamDTO.getTeamId());
			if(teamCount == 0) {
				
				teamRepository.deleteById(teamDTO.getTeamId());
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Team deleted successfully.");
				apiLogInfo.setApiResponse("team deleted successfully");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}else {
				team.ifPresent((teamFound) -> {
					teamFound.setIsActive("N");
					teamRepository.save(teamFound);
					});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Team Status changed to InActive");
				apiLogInfo.setApiResponse("Team Status Changed to InActive");			
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

	public ServiceResponse getTeamMembersByTeamId(TeamDTO teamDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("");
		apiLogInfo.setApiUrl("/api/getTeamMembersByTeamId"); 
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("TeamId : " +  teamDTO.getTeamId());

		try {

			List<Object[]> objectList = employeeTeamMapRepository.getTeamMembersByTeamId(teamDTO.getTeamId());

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No team members found. Team members list is empty");
                    apiLogInfo.setApiResponse("No team members found.team members list is empty");			
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				} else {
					List<EmployeeTeamMapDTO> dtoList = new ArrayList<EmployeeTeamMapDTO>();

					list.forEach((object) -> {

						EmployeeTeamMapDTO dto = new EmployeeTeamMapDTO();
						String[] employeeRole = (object[7] != null ? object[7].toString() : null).split(",");

						dto.setEmployeeTeamMapId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						dto.setEmpId(object[1] != null ? Long.parseLong(object[1].toString()) : null);
						dto.setTeamId(object[2] != null ? Long.parseLong(object[2].toString()) : null);
						dto.setEmployeeRole(employeeRole);
						dto.setTeamMemberName(object[8] != null ? object[8].toString() : null);
						dto.setTeamMemberDeptId(object[9] != null ? Long.parseLong(object[9].toString()) : null);
						
						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
                    apiLogInfo.setApiResponse("TeamMembers List By TeamId fetched:" + dtoList.size());			
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No team members found. Team members list is null");
                apiLogInfo.setApiResponse("No team members found. team members list is null");			
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
	public ServiceResponse getTeamMembersByTeamIdBiomax(TeamDTO teamDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("");
		apiLogInfo.setApiUrl("/api/getTeamMembersByTeamIdBiomax"); 
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("TeamId : " +  teamDTO.getTeamId());

		try {

			List<Object[]> objectList = employeeTeamMapRepository.getTeamMembersByTeamIdBioMax(teamDTO.getTeamId());

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No team members found. Team members list is empty");
                    apiLogInfo.setApiResponse("No team members found.team members list is empty");			
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				} else {
					List<EmployeeTeamMapDTO> dtoList = new ArrayList<EmployeeTeamMapDTO>();

					list.forEach((object) -> {
						Employee empid=employeeRepository.findByEmpId(Long.parseLong(object[0].toString()));
						EmployeeTeamMapDTO dto = new EmployeeTeamMapDTO();
						//String[] employeeRole = (object[7] != null ? object[7].toString() : null).split(",");
						//dto.setEmployeementId(hrMailAddress)
						//dto.setEmployeeTeamMapId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						dto.setEmployeementId ("A"+empid.getEmployeementId() != null ? "A"+empid.getEmployeementId().toString() : null);
						dto.setTeamId(object[1] != null ? Long.parseLong(object[1].toString()) : null);
						//dto.setEmployeeRole(employeeRole);
						dto.setTeamMemberName(object[2] != null ? object[2].toString() : null);
						//dto.setTeamMemberDeptId(object[9] != null ? Long.parseLong(object[9].toString()) : null);
						
						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
                    apiLogInfo.setApiResponse("TeamMembers List By TeamId fetched:" + dtoList.size());			
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No team members found. Team members list is null");
                apiLogInfo.setApiResponse("No team members found. team members list is null");			
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

	public ServiceResponse updateTeam(TeamDTO teamDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("UpdateTeam");
		apiLogInfo.setApiUrl("/api/updateTeam");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("TeamId :" + teamDTO.getTeamId() + " ,TeamName :" + teamDTO.getTeamName());

		try {
			List<EmployeeTeamMapDTO> allTeamMemberList = teamDTO.getAllTeamMemberList();
			List<EmployeeTeamMapDTO> updatedTeamMemberList = teamDTO.getUpdatedTeamMemberList();
			
			String teamLeadName = null;
			TeamDTO teamObj = new TeamDTO();

			if (teamDTO.getTeamLeadId() != null) {

				Optional<Employee> getTeamLeadData = employeeRepository.findById(teamDTO.getTeamLeadId());
				if (!getTeamLeadData.isEmpty()) {
					Employee empObj = getTeamLeadData.get();

					teamLeadName = empObj.getName();
					teamObj.setTeamLeadName(teamLeadName);	
				}
			} else {
				teamLeadName = "NA";
			}
			
			if(!allTeamMemberList.isEmpty()) {
				
				// Case 5 : Updating Existing Member
				allTeamMemberList.stream().filter((existingMember) -> existingMember.getEmployeeTeamMapId() != null)
				.forEach((employee) -> {
					StringBuilder str = new StringBuilder("");
					for(String role: employee.getEmployeeRole()) {
						str.append(role).append(",");
					}
					
					EmployeeTeamMap map = employeeTeamMapRepository.findByEmployeeTeamMapId(employee.getEmployeeTeamMapId());
					map.setEmployeeRole(str.toString());
					employeeTeamMapRepository.save(map);
				});
			}

			if (!updatedTeamMemberList.isEmpty()) {
				Optional<Team> teamObject = teamRepository.findById(teamDTO.getTeamId());

				teamObject.ifPresentOrElse((teamFound) -> {
					
					StringBuilder department = new StringBuilder("");
					for(String deptId: teamDTO.getDepartmentList()) {
						department.append(deptId).append(",");
					}

					teamFound.setTeamName(teamDTO.getTeamName());
					teamFound.setTeamLeadId(teamDTO.getTeamLeadId());
					teamFound.setTeamLeadName(teamObj.getTeamLeadName());
					teamFound.setDeptIds(department.toString());
					Team teamUpdated = teamRepository.save(teamFound);

					if (teamUpdated.getTeamId() != null) {

						// Case 1 : No existing Team Members + Adding New Member in Update
						updatedTeamMemberList.stream().filter((teamMember) -> teamMember.getEmployeeTeamMapId() == null)
								.forEach((employee) -> {
									
									StringBuilder str = new StringBuilder("");
									for(String role: employee.getEmployeeRole()) {
										str.append(role).append(",");
									}
									
									EmployeeTeamMap map = new EmployeeTeamMap();
									map.setEmpId(employee.getEmpId());
									map.setTeamId(teamUpdated.getTeamId());
									map.setEmployeeRole(str.toString());
									map.setActive((long) 1);
									employeeTeamMapRepository.save(map);
								});

						// Case 2 : No New Member is Added + ONLY Removed Existing Member
						updatedTeamMemberList.stream().filter((teamMember) -> teamMember.getEmployeeTeamMapId() != null)
								.forEach((employee) -> {
//									employee.setActive((long) 0);
									
									EmployeeTeamMap map = employeeTeamMapRepository.findByEmployeeTeamMapId(employee.getEmployeeTeamMapId());
									map.setActive((long) 0);
									map.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
									employeeTeamMapRepository.save(map);
									//employeeTeamMapRepository.deleteById(employee.getEmployeeTeamMapId());
								});

						// Case 3 : Removed Existing Member + Added New Member (Combination of Case 1&2)
						
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Team updated.");
						apiLogInfo.setApiResponse("Team updated");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);


					} else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Team updation failed.");
						apiLogInfo.setApiResponse("Team updation failed");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

					}

				}, () -> {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Team not found.");
					apiLogInfo.setApiResponse("Team not Found");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

				});

			} else {

				// Case 4 : No Existing member is removed + No new member is added
				Optional<Team> teamObject = teamRepository.findById(teamDTO.getTeamId());

				teamObject.ifPresentOrElse((teamFound) -> {
					
					StringBuilder department = new StringBuilder("");
					for(String deptId: teamDTO.getDepartmentList()) {
						department.append(deptId).append(",");
					}

					teamFound.setTeamName(teamDTO.getTeamName());
					teamFound.setTeamLeadId(teamDTO.getTeamLeadId());
					teamFound.setTeamLeadName(teamObj.getTeamLeadName());
					teamFound.setDeptIds(department.toString());
					Team teamUpdated = teamRepository.save(teamFound);
					
					if (teamUpdated.getTeamId() != null) {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Team updated.");
						apiLogInfo.setApiResponse("Team updated");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

					} else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Team updation failed.");
						apiLogInfo.setApiResponse("Team updation failed");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

					}

				}, () -> {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Team not found.");
					apiLogInfo.setApiResponse("Team not found");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

				});
			}
			
			//Add Default Activities which are not mapped
			List<Activity> activityList = activitiesRepository.findByTeamId(teamDTO.getTeamId());
			Set<Long> activityDeptId = new HashSet<>();
			
			if(!activityList.isEmpty()) {
				activityList.forEach((object) -> {
					String[] ids = object.getDeptIds().split(",");
					for(String id : ids) {
						activityDeptId.add(Long.parseLong(id));
					}
				});;
			}
			
			//Add activities if not added
			if(!activityDeptId.isEmpty()){
				for(Long deptId : activityDeptId) {
					String departmentId = String.valueOf(deptId);
					Long teamId = teamDTO.getTeamId();
					List<Activity> activityPresent = activitiesRepository.getActivityByTeamIdAndDepts(departmentId, teamDTO.getTeamId());
					String[] employeeRoles = {"TeamLead","Employee", "HOD", "Manager"};
					
					if(!activityPresent.isEmpty()) {
						for(String role : employeeRoles) {
							boolean contain = containsEmployeeRole(activityPresent, role);
							
							if(!contain) {
								List<ActivityTemplate> activityTemplate = activityTemplateRepository.getByDeptIdAndEmployeeRole(deptId, role);
								if(!activityTemplate.isEmpty()) {
									
									for(ActivityTemplate object: activityTemplate) {
										Activity newActivity = new Activity();

										newActivity.setActivity(object.getTemplateActivity());
										newActivity.setTeamId(teamDTO.getTeamId());
										newActivity.setEmployeeRole(object.getEmployeeRole());
										newActivity.setDeptIds(object.getDeptId().toString());
										newActivity.getCommonProperty().setCreatedBy(teamDTO.getCreatedBy());

										Activity newActivityCreated = activitiesRepository.save(newActivity);
									}
								}
							}
						}
					}
				}				
			}
			
			// Add activity if new Department added in update team
			for(String deptId: teamDTO.getDepartmentList()) {
				if(!activityDeptId.contains(Long.parseLong(deptId))) {
					List<ActivityTemplate> activityTemplate = activityTemplateRepository.getByDeptId(Long.parseLong(deptId));
					if(!activityTemplate.isEmpty()) {
						
						for(ActivityTemplate object: activityTemplate) {
							Activity newActivity = new Activity();

							newActivity.setActivity(object.getTemplateActivity());
							newActivity.setTeamId(teamDTO.getTeamId());
							newActivity.setEmployeeRole(object.getEmployeeRole());
							newActivity.setDeptIds(object.getDeptId().toString());
							newActivity.getCommonProperty().setCreatedBy(teamDTO.getCreatedBy());

							Activity newActivityCreated = activitiesRepository.save(newActivity);
						}
					}
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
	
	public boolean containsEmployeeRole(final List<Activity> list, final String employeeName){
	    return list.stream().anyMatch(o -> o.getEmployeeRole().equals(employeeName));
	}
	
	public ServiceResponse getAllMyTeamsByEmpId(EmployeeDTO employeeDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("");
		apiLogInfo.setApiUrl("/api/getAllMyTeamsByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("EmpId : " + employeeDTO.getEmpId());
		String customQuery = "";
		try {
			
			String employeeRole = employeeDTO.getEmployeeRole();
			if(employeeRole.equals("SuperAdmin") || employeeRole.equals("HR") || employeeRole.equals("HOD")) {
				customQuery = "t.created_by = "+ employeeDTO.getEmpId() +" OR p.department_name = '"+ employeeDTO.getDepartmentName()+"' OR p.project_manager_id = "+ employeeDTO.getEmpId() +" OR t.team_lead_id = "+employeeDTO.getEmpId();
			}else if(employeeRole.equals("Manager")){
				customQuery = "t.created_by = "+ employeeDTO.getEmpId() +" OR p.project_manager_id = "+ employeeDTO.getEmpId() +" OR t.team_lead_id = "+employeeDTO.getEmpId()+"";
			}else if(employeeRole.equals("TeamLead")) {
				customQuery = "t.created_by = "+ employeeDTO.getEmpId() +" OR t.team_lead_id = "+employeeDTO.getEmpId()+"";
			}else {
				customQuery = "t.created_by = "+ employeeDTO.getEmpId();
			}
			
			List<Object[]> objectList = getAllMyTeamsByCustomQuery(customQuery);

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No teams found. Teams list is empty");
					apiLogInfo.setApiResponse("No teams found. teams list is empty");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				} else {
					
					List<TeamDTO> dtoList = new ArrayList<TeamDTO>();
						
						list.forEach((object) -> {
							TeamDTO dto = new TeamDTO();

							dto.setProjectId(object[0] != null ? Integer.parseInt(object[0].toString()) : null);
							dto.setProjectName(object[1] != null ? object[1].toString() : null);
							dto.setTeamId(object[2] != null ? Long.parseLong(object[2].toString()) : null);
							dto.setTeamName(object[3] != null ? object[3].toString() : null);
							dto.setTeamLeadId(object[4] != null ? Long.parseLong(object[4].toString()) : null);
							dto.setTeamLeadName(object[5] != null ? object[5].toString() : null);
							dto.setProjectManagerId(object[6] != null ? Long.parseLong(object[6].toString()) : null);
							dto.setProjectManagerName(object[7] != null ? object[7].toString() : null);
							dto.setDepartmentName(object[8] != null ? object[8].toString() : null);
							dto.setCreatedByName(object[9] != null ? object[9].toString() : null);
							dto.setCreatedOn(object[10] != null ? object[10].toString() : null);
							dto.setIsActive(object[11] != null ? object[11].toString() : null);
							dto.setTeamLeadDeptId(object[12] != null ? Long.parseLong(object[12].toString()) : null);
							dto.setDepartmentList(object[13] != null ? object[13].toString().split(",") : null);
							dto.setEmpId(object[14] != null ? Long.parseLong(object[14].toString()) : null);
							dtoList.add(dto);
						});
						
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("All my teams by EmpId fetched :" + dtoList.size());			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No teams found.Teams list is null");
				apiLogInfo.setApiResponse("No teams found. Teams List is null");			
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
	
	
//	 List<Object[]> getAllMyTeamsByCustomQuery(String customQuery) {
//			try {
//				Session session = entityManager.unwrap(Session.class);
//				
//				try {
//					
//					String q="SELECT distinctrow t.project_id, p.project_name, t.team_id, t.team_name, t.team_lead_id, t.team_lead_name, p.project_manager_id , pm.name as projectManager,\n"
//							+ "p.department_name,e1.name as teamCreatedByName,t.created_on, t.is_active, jr.dept_id as teamLeadDept, t.dept_ids,e1.emp_id \n"
//							+ "FROM teams t \n"
//							+ "LEFT JOIN employee e1 ON e1.emp_id = t.created_by \n"
//							+ "LEFT JOIN projects p ON p.project_id = t.project_id \n"
//							+ "LEFT JOIN employee pm ON pm.emp_id = p.project_manager_id  \n"
//							+ "LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id \n"
//							+ "LEFT JOIN job_role jr ON jr.job_role_id = tl.job_role_id \n"
//							+ "WHERE "+ customQuery +" ORDER BY p.project_name, t.team_name";
//					
//					System.out.println("Query :"+ q);
//					Query query = session.createSQLQuery(q);
//					System.out.println(query);
//					System.out.println("Result List : "+ query.getResultList());
//					return query.getResultList();
//					
//				}catch(Exception e) {
//					e.printStackTrace();
//				}finally {
//					if(session!=null && session.isOpen()) {
//						session.close();
//					}
//				}
//			}catch(Exception e) {
//				e.printStackTrace();
//			}
//			return new ArrayList<>();
//		}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> getAllMyTeamsByCustomQuery(String customQuery) {
	    try {
	        Session session = entityManager.unwrap(Session.class);
	        try {
	            String q = "SELECT DISTINCTROW t.project_id, p.project_name, t.team_id, t.team_name, t.team_lead_id, " +
	                       "t.team_lead_name, p.project_manager_id , pm.name as projectManager, " +
	                       "p.department_name, e1.name as teamCreatedByName, t.created_on, t.is_active, " +
	                       "jr.dept_id as teamLeadDept, t.dept_ids, e1.emp_id " +
	                       "FROM teams t " +
	                       "LEFT JOIN employee e1 ON e1.emp_id = t.created_by " +
	                       "LEFT JOIN projects p ON p.project_id = t.project_id " +
	                       "LEFT JOIN employee pm ON pm.emp_id = p.project_manager_id " +
	                       "LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id " +
	                       "LEFT JOIN job_role jr ON jr.job_role_id = tl.job_role_id " +
	                       "WHERE " + customQuery + " ORDER BY p.project_name, t.team_name";

	            System.out.println("Query: " + q);
	            org.hibernate.query.NativeQuery<Object[]> query = session.createSQLQuery(q);
	            List<Object[]> result = query.getResultList();
	            System.out.println("Result List: " + result);
	            return result;

	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            if (session != null && session.isOpen()) {
	                session.close();
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return new ArrayList<>();
	}

	 
		public ServiceResponse getMappedActivityPreview(TeamDTO teamDTO) {
			ServiceResponse response = new ServiceResponse();
			LogDTO apiLogInfo = new LogDTO();
			//apiLogInfo.setSubFeatureName("");
			apiLogInfo.setApiUrl("/api/getMappedActivityPreview");
			apiLogInfo.setLogLevel("INFO");
			StringBuilder logBuilder = new StringBuilder();
			logBuilder.append("EmpId : " + teamDTO.getEmpId() + " ,DeptId" + teamDTO.getDeptId()
			 + " ,EmployeeRole :" + teamDTO.getEmployeeRole());
			try {
				
				List<Object[]> empObj = employeeRepository.getEmployeeData(teamDTO.getEmpId());
				Long deptId = null;
				
				if(!empObj.isEmpty()){
					for(Object[] object: empObj) {
						deptId = object[4] != null ? Long.parseLong(object[4].toString()) : null;
					}
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("User Info not found.");
					apiLogInfo.setApiResponse("User Info not found");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
				
				List<ActivityTemplate> activityObj = activityTemplateRepository
						.getByDeptIdAndEmployeeRoleIn(deptId, teamDTO.getEmployeeRole());
				
				if(!activityObj.isEmpty()) {
					List<ActivityTemplateDTO> dtoList = new ArrayList<>();
					
					activityObj.forEach((object) -> {
						ActivityTemplateDTO dto = new ActivityTemplateDTO();
						
						dto.setActivity(object.getTemplateActivity());
						dto.setEmployeeRole(object.getEmployeeRole());
						dtoList.add(dto);
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("MappedActivityPreview fetched:" + dtoList.size());			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No activity found.");
					apiLogInfo.setApiResponse("No activity found");			
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
		
		public ServiceResponse getMappedActivityInUpdateTeam(TeamDTO teamDTO) {
			ServiceResponse response = new ServiceResponse();
            LogDTO apiLogInfo = new LogDTO();
            //apiLogInfo.setSubFeatureName("");
            apiLogInfo.setApiUrl("/api/getMappedActivityInUpdateTeam");
            apiLogInfo.setLogLevel("INFO");
            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append("EmpId : " + teamDTO.getEmpId() + " ,TeamId :" + teamDTO.getTeamId() 
             + " ,EmployeeRole :" + teamDTO.getEmployeeTeamRole());

			
			try {
				
				String deptId = null;
				List<Object[]> empObj = employeeRepository.getEmployeeData(teamDTO.getEmpId());
				if(!empObj.isEmpty()) {
					for(Object[] object: empObj) {
						deptId = object[4] != null ? object[4].toString() : null;
					}
				}
				
				List<Activity> activityObj = activitiesRepository
						.findByTeamIdAndEmployeeRoleIn(teamDTO.getTeamId(), teamDTO.getEmployeeRole());
				List<Activity> dtoList = new ArrayList<Activity>();
				
				if(!activityObj.isEmpty()) {
					
					for(Activity object: activityObj) {
						List<String> list = Arrays.asList(object.getDeptIds().split(","));
						
						if (list.contains(deptId)) {
							Activity dto = new Activity();
							
							dto.setActivity(object.getActivity());
							dto.setEmployeeRole(object.getEmployeeRole());
							dto.setTeamId(object.getTeamId());
							dtoList.add(dto);
						}
					}
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("Mapped Activity List Fetched" + dtoList.size());			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Activity List is empty");
					apiLogInfo.setApiResponse("Activity List is empty");			
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
	 	

//	MyTeam Servcie
	
	public ServiceResponse getAllTeamView(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("");
		apiLogInfo.setApiUrl("/api/getAllTeamView");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append(" ,EmpId : " + employeedto.getEmpId());


		try {
			List<Object[]> list = employeeRepository.getAllTeamView(employeedto.getEmpId());
			List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();
			if (list.isEmpty()) {
		            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No teams found");
				apiLogInfo.setApiResponse("No teams found");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			} else {
				
				// Date Range to Check Timesheet
				int currentYear = LocalDate.now().getYear();
				int currentMonth = LocalDate.now().getMonthValue();
				
				LocalDate firstOfMonth = LocalDate.of(currentYear, currentMonth, 1);
				LocalDate end = LocalDate.now().minusDays(1);
				
				Long period = ChronoUnit.DAYS.between(firstOfMonth, end) + 1;
				
				// Get Filled EOD Count for Team Members
				List<Object[]> timesheetList = timesheetsRepository.getMyTeamsFilledEodCountByManagerIdOLD(firstOfMonth, end, employeedto.getEmpId());
				System.err.println(timesheetList.size());
				
				list.forEach((object) -> {
					EmployeeDTO dto = new EmployeeDTO();
					dto.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()): null);
					dto.setName(object[1] != null ? object[1].toString(): null);
					dto.setEmail(object[2] != null ? object[2].toString(): null);
					dto.setJobRoleName(object[3] != null ? object[3].toString(): null);
					dto.setMobileNo(object[4] != null ? Long.parseLong(object[4].toString()): null);
					dto.setManagerName(object[5] != null ? object[5].toString(): null);
					dto.setEmployeementId(object[6] != null ? Long.parseLong(object[6].toString()): null);
					dto.setInvalidAccessAttempt(object[7] != null ? Integer.parseInt(object[7].toString()): null);
					dto.setIsTimesheetLockCheckEnable(object[8] != null ? object[8].toString(): null);
					dto.setEmploymentstatus(object[9] != null ? object[9].toString(): null);				
					LocalDate dateOfRelieving = object[10] != null ? LocalDate.parse(object[10].toString()) : null;
					dto.setPipFlag(object[11] != null ? object[11].toString() : null);
					dto.setPipId(object[12] != null ? Long.parseLong(object[12].toString()) : null);
					dto.setIsConsultant(object[13] != null ? object[13].toString() : null);
					dto.setIsApprenticeship(object[14] != null ? object[14].toString() : null);
					dto.setIsApmosysProduct(object[15] != null ? object[15].toString() : null);
					
					String employmentId = dto.getEmployeementId() != null ? dto.getEmployeementId().toString() : null;
//				    String isConsultant = timesheetDto.getIsConsultant();
				    String isApmosysProduct = dto.getIsApmosysProduct();

				    if (employmentId != null) {
				        if ("true".equalsIgnoreCase(isApmosysProduct)) {
				        	dto.setEmploymentIdAcToET("AP-" + employmentId);
				        }else {
				        	dto.setEmploymentIdAcToET("A-" + employmentId);
				        }
				    }

					
					
					dto.setFailedAttempt(failedAttempt);
					if(dateOfRelieving != null && dateOfRelieving.isEqual(LocalDate.now())) {
						dto.setIsDateOfRelievingToday("true");
					}else {
						dto.setIsDateOfRelievingToday("false");
					}
					
//					 dto.setTimesheetStatus("Defaulter");
					timesheetList.forEach((timesheet) -> {

						Long timesheetEmpId = timesheet[0] != null ? Long.parseLong(timesheet[0].toString()) : null;
						Long employeeEmpId = object[0] != null ? Long.parseLong(object[0].toString()) : null;

						if (timesheetEmpId.equals(employeeEmpId)) {
							Long filledEodCount = timesheet[1] != null ? Long.parseLong(timesheet[1].toString()) : 0L;
							Long pendingEodCount = period - filledEodCount;

							if(pendingEodCount >= 3) {
		                dto.setTimesheetStatus("Defaulter");
							}else if (pendingEodCount > 0 && pendingEodCount < 3) {
								dto.setTimesheetStatus("Pending Timesheets : "+ pendingEodCount);
							}
							else {
		                dto.setTimesheetStatus("Timesheets upto date");
		            }
						}
					});
					

		            dtoList.add(dto);
				});

		        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		        response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("AllTeamView fetched:" + dtoList.size() );			
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
	
	
	public ServiceResponse getAllTeamView1(EmployeeDTO employeedto) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getAllTeamView");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append(" ,EmpId : ").append(employeedto.getEmpId());

	    try {
	        // Fetch direct team members
	        List<Object[]> list = employeeRepository.getAllTeamView(employeedto.getEmpId());
	        List<EmployeeDTO> dtoList = new ArrayList<>();

	        if (list.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No teams found");
	            apiLogInfo.setApiResponse("No teams found");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        } else {
	            // Date Range for Timesheet check
	            int currentYear = LocalDate.now().getYear();
	            int currentMonth = LocalDate.now().getMonthValue();
	            LocalDate firstOfMonth = LocalDate.of(currentYear, currentMonth, 1);
	            LocalDate end = LocalDate.now().minusDays(1);
	            Long period = ChronoUnit.DAYS.between(firstOfMonth, end) + 1;

	            // Get filled EOD counts for team members
	            List<Object[]> timesheetList =
	                timesheetsRepository.getMyTeamsFilledEodCountByManagerIdOLD(
	                    firstOfMonth, end, employeedto.getEmpId()
	                );

	            // Map employees
	            for (Object[] obj : list) {
	                EmployeeDTO dto = mapObjectToDTO(obj, period, timesheetList);

	                // If hierarchy requested → recursively build sub-team
	                if (Boolean.TRUE.equals(employeedto.getIsHierarchy())) {
//	                    dto = buildHierarchy(dto, true, period, timesheetList);
	                    dto = buildHierarchy(dto, true, period, firstOfMonth, end);

	                }

	                dtoList.add(dto);
	            }

	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse(dtoList);
	            apiLogInfo.setApiResponse("AllTeamView fetched: " + dtoList.size());
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

	/**
	 * Recursively builds hierarchy for an employee.
	 */
	private EmployeeDTO buildHierarchy(EmployeeDTO manager, boolean includeHierarchy,
            Long period, LocalDate firstOfMonth, LocalDate end) {
	    if (!includeHierarchy) {
	        return manager;
	    }

	    List<Object[]> subList = employeeRepository.getAllTeamView(manager.getEmpId());
	    if(subList.size()<1 ) {
	    	return manager;
	    }
	    
	    // Fetch timesheet status for this manager's direct reportees
	    List<Object[]> timesheetList =
	        timesheetsRepository.getMyTeamsFilledEodCountByManagerIdOLD(
	            firstOfMonth, end, manager.getEmpId()
	        );
	    
	    
	    for (Object[] obj : subList) {
	        EmployeeDTO child = mapObjectToDTO(obj, period, timesheetList);
//	        EmployeeDTO childWithTeam = buildHierarchy(child, true, period, timesheetList);
	        EmployeeDTO childWithTeam = buildHierarchy(child, true, period, firstOfMonth, end);

	        manager.getReportees().add(childWithTeam);
	    }
	    return manager;
	}

	/**
	 * Maps DB object[] → EmployeeDTO
	 */
	private EmployeeDTO mapObjectToDTO(Object[] object, Long period, List<Object[]> timesheetList) {
	    EmployeeDTO dto = new EmployeeDTO();

	    dto.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
	    dto.setName(object[1] != null ? object[1].toString() : null);
	    dto.setEmail(object[2] != null ? object[2].toString() : null);
	    dto.setJobRoleName(object[3] != null ? object[3].toString() : null);
	    dto.setMobileNo(object[4] != null ? Long.parseLong(object[4].toString()) : null);
	    dto.setManagerName(object[5] != null ? object[5].toString() : null);
	    dto.setEmployeementId(object[6] != null ? Long.parseLong(object[6].toString()) : null);
	    dto.setInvalidAccessAttempt(object[7] != null ? Integer.parseInt(object[7].toString()) : null);
	    dto.setIsTimesheetLockCheckEnable(object[8] != null ? object[8].toString() : null);
	    dto.setEmploymentstatus(object[9] != null ? object[9].toString() : null);
	    LocalDate dateOfRelieving = object[10] != null ? LocalDate.parse(object[10].toString()) : null;
	    dto.setPipFlag(object[11] != null ? object[11].toString() : null);
	    dto.setPipId(object[12] != null ? Long.parseLong(object[12].toString()) : null);
	    dto.setIsConsultant(object[13] != null ? object[13].toString() : null);
	    dto.setIsApprenticeship(object[14] != null ? object[14].toString() : null);
	    dto.setIsApmosysProduct(object[15] != null ? object[15].toString() : null);

	    // Employment Id formatting
	    String employmentId = dto.getEmployeementId() != null ? dto.getEmployeementId().toString() : null;
	    String isApmosysProduct = dto.getIsApmosysProduct();
	    if (employmentId != null) {
	        if ("true".equalsIgnoreCase(isApmosysProduct)) {
	            dto.setEmploymentIdAcToET("AP-" + employmentId);
	        } else {
	            dto.setEmploymentIdAcToET("A-" + employmentId);
	        }
	    }

	    // Date of relieving check
	    if (dateOfRelieving != null && dateOfRelieving.isEqual(LocalDate.now())) {
	        dto.setIsDateOfRelievingToday("true");
	    } else {
	        dto.setIsDateOfRelievingToday("false");
	    }

	    // Timesheet status
	    dto.setTimesheetStatus("Defaulter");
	    for (Object[] timesheet : timesheetList) {
	        Long timesheetEmpId = timesheet[0] != null ? Long.parseLong(timesheet[0].toString()) : null;
	        Long employeeEmpId = dto.getEmpId();
	        if (timesheetEmpId != null && timesheetEmpId.equals(employeeEmpId)) {
	            Long filledEodCount = timesheet[1] != null ? Long.parseLong(timesheet[1].toString()) : 0L;
	            Long pendingEodCount = period - filledEodCount;

	            if (pendingEodCount >= 3) {
	                dto.setTimesheetStatus("Defaulter");
	            } else if (pendingEodCount > 0 && pendingEodCount < 3) {
	                dto.setTimesheetStatus("Pending Timesheets : " + pendingEodCount);
	            } else {
	                dto.setTimesheetStatus("OK");
	            }
	        }
	    }

	    return dto;
	}


	public ServiceResponse getAllTeamMemberView(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("");
		apiLogInfo.setApiUrl("/api/getAllTeamMemberView");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("EmpId : "+employeedto.getEmpId());

		try {

			LocalDate date = LocalDate.now().minusDays(Long.parseLong(timesheetCheckPeriod));
			//List<Object[]> list = employeeRepository.getAllTeamMemberView(employeedto.getEmpId());
			List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();
			
			Long empId = employeedto.getEmpId();
			Map<Long, Object[]> employeeMap = new LinkedHashMap<>(); // Use LinkedHashMap to preserve order and avoid duplicates

			// ========== 3-A: Full Privilege (Department-based access) ==========
			// Check if employee has full privilege roles (job_role_id from app.team.fullPrivilegeRoleIds)
			List<EmployeeJobRoleDept> roleDeptList = employeeRepository.findRoleDeptByEmpId(empId);
			Set<Long> fullAccessDeptIds = new HashSet<>();
			Set<Long> fullPrivilegeRoleIds = Arrays.stream(fullPrivilegeRoleIdsConfig.split(","))
					.map(String::trim)
					.filter(s -> !s.isEmpty())
					.map(Long::parseLong)
					.collect(Collectors.toSet());
			
			for (EmployeeJobRoleDept roleDept : roleDeptList) {
				if (fullPrivilegeRoleIds.contains(roleDept.getJobRoleId())) {
					fullAccessDeptIds.add(roleDept.getDepartmentId());
				}
			}

			// If employee has full privilege, fetch all employees from those departments (same 26-column structure as old query)
			if (!fullAccessDeptIds.isEmpty()) {
				List<Object[]> deptEmployees = employeeRepository.getAllTeamMemberViewByDepartmentIds(new ArrayList<>(fullAccessDeptIds));
				for (Object[] obj : deptEmployees) {
					Long empIdFromResult = obj[0] != null ? Long.parseLong(obj[0].toString()) : null;
					if (empIdFromResult != null && !empIdFromResult.equals(empId)) {
						employeeMap.put(empIdFromResult, obj);
					}
				}
			}

			// ========== 3-B: Project-based access ==========
			// Collect all project IDs where employee has access through various roles
			Set<Long> projectIds = new HashSet<>();
			
			// 1. Employee is on a team (common team member)
			projectIds.addAll(employeeRepository.findProjectIdsWhereEmpIsOnTeam(empId));
			
			// 2. Employee is a project manager
			projectIds.addAll(employeeRepository.findProjectIdsWhereEmpIsProjectManager(empId));
			
			// 3. Employee is an overhead
			projectIds.addAll(employeeRepository.findProjectIdsWhereEmpIsOverhead(empId));
			
			// 4. Employee is a team lead or SPOC
			projectIds.addAll(employeeRepository.findProjectIdsWhereEmpIsTeamLeadOrSpoc(empId));
			
			
			// Fetch employees for all collected project IDs
			if (!projectIds.isEmpty()) {
				List<Object[]> projectEmployees = employeeRepository.getAllTeamMemberViewByProjectIds(new ArrayList<>(projectIds), empId);
				for (Object[] obj : projectEmployees) {
					Long empIdFromResult = obj[0] != null ? Long.parseLong(obj[0].toString()) : null;
					if (empIdFromResult != null) {
						employeeMap.put(empIdFromResult, obj); // Map will automatically handle duplicates
					}
				}
			}

			// Convert map values to list
			List<Object[]> list = new ArrayList<>(employeeMap.values());
			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No teams found");
				apiLogInfo.setApiResponse("No teams found");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			} else {

				list.forEach((object) -> {
					EmployeeDTO dto = new EmployeeDTO();
					dto.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()): null);
					dto.setName(object[1] != null ? object[1].toString(): null);
					dto.setEmail(object[2] != null ? object[2].toString(): null);
					dto.setJobRoleName(object[3] != null ? object[3].toString(): null);
					dto.setMobileNo(object[4] != null ? Long.parseLong(object[4].toString()): null);
					dto.setEmployeementId(object[5] != null ? Long.parseLong(object[5].toString()): null);
					dto.setEmploymentstatus(object[6] != null ? object[6].toString(): null);
					dto.setManagerId(object[7] != null ? Long.parseLong(object[7].toString()): null);
					dto.setManagerName(object[8] != null ? object[8].toString() : null);
					dto.setManagerEmail(object[9] != null ? object[9].toString() : null);
					dto.setHodId(object[10] != null ? Long.parseLong(object[10].toString()) : null);
					dto.setHodName(object[11] != null ? object[11].toString() : null);
					dto.setHodEmail(object[12] != null ? object[12].toString() : null);
					dto.setDepartmentId(object[13] != null ? Long.parseLong(object[13].toString()) : null);
					dto.setIsTimesheetLockCheckEnable(object[14] != null ? object[14].toString() : null);
					dto.setReportingManagerId(object[15] != null ? Long.parseLong(object[15].toString()) : null);
					dto.setApprovalsTo(object[16] != null ? object[16].toString() : null);
					dto.setReportingManagerName(object[17] != null ? object[17].toString() : null);
					dto.setReportingManagerEmail(object[18] != null ? object[18].toString() : null);
					dto.setDateOfJoining(object[19] != null ? object[19].toString() : null);
					dto.setProbationPeriod(object[20] != null ? Short.parseShort(object[20].toString()) : null);
					dto.setIsConsultant(object[21] != null ? object[21].toString() : null);
					dto.setIsApprenticeship(object[22] != null ? object[22].toString() : null);
					dto.setIsApmosysProduct(object[23] != null ? object[23].toString() : null);
					String employmentId = dto.getEmployeementId() != null ? dto.getEmployeementId().toString() : null;
//				    String isConsultant = timesheetDto.getIsConsultant();
				    String isApmosysProduct = dto.getIsApmosysProduct();

				    if (employmentId != null) {
				        if ("true".equalsIgnoreCase(isApmosysProduct)) {
				        	dto.setEmploymentIdAcToET("AP-" + employmentId);
				        }else {
				        	dto.setEmploymentIdAcToET("A-" + employmentId);
				        }
				    }

					dto.setClientSideId(object[24] != null ? object[24].toString() : null);
					dto.setEmploymentId(object[25] != null ? object[25].toString() : null);
					
					Long emp_Id = object[0] != null ? Long.parseLong(object[0].toString()): null;
					List<Object[]> timesheetFilledByMember = timesheetsRepositoryNew.getTimesheetFilledByMember(emp_Id,date);
					
					if(timesheetFilledByMember.size() >= Long.parseLong(maximumTimesheetCanBeFilledByTeamMember)) {
						dto.setIsTimesheetFilledByMember("true");
					}else {
						dto.setIsTimesheetFilledByMember("false");
					}
					
					dtoList.add(dto);
					
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("AllTeamMemberView Fetched:" + dtoList.size());			
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

	public ServiceResponse getAllTeamLeaveHistoryView1(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("");
		apiLogInfo.setApiUrl("/api/getAllTeamLeaveHistoryView");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("EmpId : "+ leaveDTO.getEmpId() + " ,FromDate :" + leaveDTO.getFromDate()
		 + " ,ToDate :" + leaveDTO.getToDate());		
		try {
			
			LocalDate start = LocalDate.parse(leaveDTO.getFromDate());

			LocalDate end = LocalDate.parse(leaveDTO.getToDate());

			List<Object[]> list = employeeLeaveRepository.getAllTeamLeaveHistoryView(leaveDTO.getEmpId(),start,end);
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No teams leave history found");
				apiLogInfo.setApiResponse("No teams leave history found");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			} else {

				list.forEach((object) -> {
					LeaveDTO dto = new LeaveDTO();
					dto.setEmployeementId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setCreatedByName(object[1] != null ? object[1].toString() : null);
					dto.setFromDate(object[2] != null ? object[2].toString() : null);
					dto.setToDate(object[3] != null ? object[3].toString() : null);
					dto.setCreatedOn(object[4] != null ? object[4].toString() : null);
					dto.setNoOfDays(object[5] != null ? Float.parseFloat(object[5].toString()) : null);
					dto.setStatus(object[6] != null ? object[6].toString() : null);
					dto.setReason(object[7] != null ? object[7].toString() : null);
					dto.setLeaveType(object[8] != null ? object[8].toString() : null);
					dto.setLeaveStatusUpdatedByName(object[9] != null ? object[9].toString() : null);
					dto.setLeaveStatusUpdatedBy(object[10] != null ? Long.parseLong(object[10].toString()) : null);
					dto.setLeaveId(object[11] != null ? Long.parseLong(object[11].toString()) : null);
					dto.setRemark(object[12] != null ? object[12].toString() : null);
					
					dto.setApproverName(object[13] != null ? object[13].toString() : null);
					dto.setApproverEmail(object[14] != null ? object[14].toString() : null);

					dto.setManagerApprovalStatus(object[15] != null ? object[15].toString() : null);
					dto.setLevel2ApproverId(object[16] != null ? Long.parseLong(object[16].toString()) : null);
					dto.setLevel2ApproverName(object[17] != null ? object[17].toString() : null);
					dto.setLevel2ApproverEmail(object[18] != null ? object[18].toString() : null);
					dto.setLevel2ApprovalStatus(object[19] != null ? object[19].toString() : null);
					
					dto.setLevel3ApproverId(object[20] != null ? Long.parseLong(object[20].toString()) : null);
					dto.setLevel3ApproverName(object[21] != null ? object[21].toString() : null);
					dto.setLevel3ApprovalStatus(object[22] != null ? object[22].toString() : null);
					dto.setLevel3ApproverEmail(object[23] != null ? object[23].toString() : null);
					
					dto.setCurrentApprovalLevel(object[24] != null ? Integer.parseInt(object[24].toString()) : null);
					dto.setFinalApprovalLevel(object[25] != null ? Integer.parseInt(object[25].toString()) : null);
					dto.setEmpId(object[26] != null ? Long.parseLong(object[26].toString()) : null)	;				
					
					dtoList.add(dto);					
					});

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("AllTeamLeaveHistoryView fetched:" + dtoList.size());			
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

//	from here 
	
	public ServiceResponse getAllTeamLeaveHistoryView(LeaveDTO leaveDTO) {
	    ServiceResponse response = new ServiceResponse();
	    try {
	        LocalDate start = LocalDate.parse(leaveDTO.getFromDate());
	        LocalDate end = LocalDate.parse(leaveDTO.getToDate());

	        List<LeaveDTO> allLeaves = new ArrayList<>();

	        if (Boolean.TRUE.equals(leaveDTO.getIsHierarchyView())) {
	            // allLeaves = getLeavesRecursively(leaveDTO.getEmpId(), start, end);
				List<Long> empIds = empCache.getEmployeesUnderAnyLeadingPerson(leaveDTO.getEmpId());

				List<Object[]> list = employeeLeaveRepository.getAllTeamLeaveHistoryViewHirarchy(empIds, start, end);
	            // System.out.println("===============list size ================" + list.size());
				allLeaves = list.stream()
				.map(obj -> mapObjectToDTO(obj))
				.collect(Collectors.toList());
	        } else {
	            List<Object[]> list = employeeLeaveRepository.getAllTeamLeaveHistoryView(leaveDTO.getEmpId(), start, end);
//	            list.forEach(obj -> allLeaves.add(mapObjectToDTO(obj)));
	             allLeaves = list.stream()
                        .map(obj -> mapObjectToDTO(obj))
                        .collect(Collectors.toList());

	        }

	        if (allLeaves.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No team leave history found");
	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse(allLeaves);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong.");
	        response.setServiceError(e.getMessage());
	    }
	    return response;
	}

	private List<LeaveDTO> getLeavesRecursively(Long managerId, LocalDate start, LocalDate end) {
	    List<LeaveDTO> result = new ArrayList<>();

	    List<Object[]> list = employeeLeaveRepository.getAllTeamLeaveHistoryView(managerId, start, end);
		Set<Long> empIds = new HashSet<>();

	    for (Object[] obj : list) {
	        LeaveDTO dto = mapObjectToDTO(obj);
	        result.add(dto);

	        Long empId = dto.getEmpId();
			if(empId != null) {
				empIds.add(empId);
			}
	        // List<LeaveDTO> subLeaves = getLeavesRecursively(empId, start, end);
	        // result.addAll(subLeaves);
	    }
		for(Long id : empIds) {
			List<LeaveDTO> subLeaves = getLeavesRecursively(id, start, end);
			result.addAll(subLeaves);
		}
	    return result;
	}

	// Convert Object[] from repository to LeaveDTO
	private LeaveDTO mapObjectToDTO(Object[] obj) {
	    LeaveDTO dto = new LeaveDTO();
	    dto.setEmployeementId(obj[0] != null ? Long.parseLong(obj[0].toString()) : null);
	    dto.setCreatedByName(obj[1] != null ? obj[1].toString() : null);
	    dto.setFromDate(obj[2] != null ? obj[2].toString() : null);
	    dto.setToDate(obj[3] != null ? obj[3].toString() : null);
	    dto.setCreatedOn(obj[4] != null ? obj[4].toString() : null);
	    dto.setNoOfDays(obj[5] != null ? Float.parseFloat(obj[5].toString()) : null);
	    dto.setStatus(obj[6] != null ? obj[6].toString() : null);
	    dto.setReason(obj[7] != null ? obj[7].toString() : null);
	    dto.setLeaveType(obj[8] != null ? obj[8].toString() : null);
	    dto.setLeaveStatusUpdatedByName(obj[9] != null ? obj[9].toString() : null);
	    dto.setLeaveStatusUpdatedBy(obj[10] != null ? Long.parseLong(obj[10].toString()) : null);
	    dto.setLeaveId(obj[11] != null ? Long.parseLong(obj[11].toString()) : null);
	    dto.setRemark(obj[12] != null ? obj[12].toString() : null);
	    dto.setApproverName(obj[13] != null ? obj[13].toString() : null);
	    dto.setApproverEmail(obj[14] != null ? obj[14].toString() : null);
	    dto.setManagerApprovalStatus(obj[15] != null ? obj[15].toString() : null);
	    dto.setLevel2ApproverId(obj[16] != null ? Long.parseLong(obj[16].toString()) : null);
	    dto.setLevel2ApproverName(obj[17] != null ? obj[17].toString() : null);
	    dto.setLevel2ApproverEmail(obj[18] != null ? obj[18].toString() : null);
	    dto.setLevel2ApprovalStatus(obj[19] != null ? obj[19].toString() : null);
	    dto.setLevel3ApproverId(obj[20] != null ? Long.parseLong(obj[20].toString()) : null);
	    dto.setLevel3ApproverName(obj[21] != null ? obj[21].toString() : null);
	    dto.setLevel3ApprovalStatus(obj[22] != null ? obj[22].toString() : null);
	    dto.setLevel3ApproverEmail(obj[23] != null ? obj[23].toString() : null);
	    dto.setCurrentApprovalLevel(obj[24] != null ? Integer.parseInt(obj[24].toString()) : null);
	    dto.setFinalApprovalLevel(obj[25] != null ? Integer.parseInt(obj[25].toString()) : null);
	    dto.setEmpId(obj[26] != null ? Long.parseLong(obj[26].toString()) : null);
	    dto.setName(obj[1] != null ? obj[1].toString() : null); // or set employeeName if you prefer
	    return dto;
	}



	public ServiceResponse getAllTeamCompOffHistoryView1(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("");
		apiLogInfo.setApiUrl("/api/getAllTeamCompOffHistoryView");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("EmpId : " + leaveDTO.getEmpId() + " ,FromDate :" + leaveDTO.getFromDate() + 
				" ,ToDate :" + leaveDTO.getToDate());

		try {
			
			LocalDate start = LocalDate.parse(leaveDTO.getFromDate());

			LocalDate end = LocalDate.parse(leaveDTO.getToDate());

			List<Object[]> list = employeeLeaveRepository.getAllTeamCompOffHistoryView(leaveDTO.getEmpId(),start,end);
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No teams leave history found");
				apiLogInfo.setApiResponse("NO teams leave history found");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			} else {

				list.forEach((object) -> {
					LeaveDTO dto = new LeaveDTO();
					dto.setEmployeementId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setCreatedByName(object[1] != null ? object[1].toString() : null);
					dto.setFromDate(object[2] != null ? object[2].toString() : null);
					dto.setToDate(object[3] != null ? object[3].toString() : null);
					dto.setCreatedOn(object[4] != null ? object[4].toString() : null);
					dto.setNoOfDays(object[5] != null ? Float.parseFloat(object[5].toString()) : null);
					dto.setStatus(object[6] != null ? object[6].toString() : null);
					dto.setReason(object[7] != null ? object[7].toString() : null);
					dto.setLeaveType(object[8] != null ? object[8].toString() : null);
					dto.setLeaveStatusUpdatedByName(object[9] != null ? object[9].toString() : null);
					dto.setLeaveStatusUpdatedBy(object[10] != null ? Long.parseLong(object[10].toString()) : null);
					dto.setEmpId(object[11] != null ? Long.parseLong(object[11].toString()) : null);
					
					
					System.out.println("object[10]"+object[10] != null ? Long.parseLong(object[10].toString()) : null);
					dtoList.add(dto);					
					});

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("AllTeamCompoff History View Fetched:" + dtoList.size());			
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
	
	public ServiceResponse getAllTeamCompOffHistoryView(LeaveDTO leaveDTO) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getAllTeamCompOffHistoryView");
	    apiLogInfo.setLogLevel("INFO");
	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("EmpId : " + leaveDTO.getEmpId() + " ,FromDate :" + leaveDTO.getFromDate() +
	            " ,ToDate :" + leaveDTO.getToDate());

	    try {
	        LocalDate start = LocalDate.parse(leaveDTO.getFromDate());
	        LocalDate end = LocalDate.parse(leaveDTO.getToDate());

	        List<LeaveDTO> dtoList;

	        if (Boolean.TRUE.equals(leaveDTO.getIsHierarchyView())) {
//	            dtoList = getCompOffLeavesRecursively(leaveDTO.getEmpId(), start, end);
	        	List<Long> empIds = empCache.getEmployeesUnderAnyLeadingPerson(leaveDTO.getEmpId());
	        	 List<Object[]> list = employeeLeaveRepository.getAllTeamCompOffHistoryViewHirarchy(empIds, start, end);
		            dtoList = list.stream()
		                          .map(this::mapCompOffObjectToDTO)
		                          .collect(Collectors.toList());

	        } else {
	            List<Object[]> list = employeeLeaveRepository.getAllTeamCompOffHistoryView(leaveDTO.getEmpId(), start, end);
	            dtoList = list.stream()
	                          .map(this::mapCompOffObjectToDTO)
	                          .collect(Collectors.toList());
	        }

	        if (dtoList.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No teams comp-off history found");
	            apiLogInfo.setApiResponse("No teams comp-off history found");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse(dtoList);
	            apiLogInfo.setApiResponse("AllTeamCompOff History View Fetched: " + dtoList.size());
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

	// Recursive method to fetch comp-off leaves for all hierarchy
	private List<LeaveDTO> getCompOffLeavesRecursively(Long managerId, LocalDate start, LocalDate end) {
	    List<LeaveDTO> result = new ArrayList<>();

	    List<Object[]> list = employeeLeaveRepository.getAllTeamCompOffHistoryView(managerId, start, end);

	    for (Object[] obj : list) {
	        LeaveDTO dto = mapCompOffObjectToDTO(obj);
	        result.add(dto);

	        Long empId = dto.getEmpId();
	        List<LeaveDTO> subLeaves = getCompOffLeavesRecursively(empId, start, end);
	        result.addAll(subLeaves);
	    }

	    return result;
	}

	// For comp-off history
	private LeaveDTO mapCompOffObjectToDTO(Object[] obj) {
	    LeaveDTO dto = new LeaveDTO();
	    dto.setEmployeementId(obj[0] != null ? Long.parseLong(obj[0].toString()) : null);
	    dto.setCreatedByName(obj[1] != null ? obj[1].toString() : null);
	    dto.setFromDate(obj[2] != null ? obj[2].toString() : null);
	    dto.setToDate(obj[3] != null ? obj[3].toString() : null);
	    dto.setCreatedOn(obj[4] != null ? obj[4].toString() : null);
	    dto.setNoOfDays(obj[5] != null ? Float.parseFloat(obj[5].toString()) : null);
	    dto.setStatus(obj[6] != null ? obj[6].toString() : null);
	    dto.setReason(obj[7] != null ? obj[7].toString() : null);
	    dto.setLeaveType(obj[8] != null ? obj[8].toString() : null);
	    dto.setLeaveStatusUpdatedByName(obj[9] != null ? obj[9].toString() : null);
	    dto.setLeaveStatusUpdatedBy(obj[10] != null ? Long.parseLong(obj[10].toString()) : null);
	    dto.setEmpId(obj[11] != null ? Long.parseLong(obj[11].toString()) : null);
	    return dto;
	}
	
	public ServiceResponse getAllTeamCompOffHistoryViewByEmpId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("");
		apiLogInfo.setApiUrl("/api/getAllTeamCompOffHistoryViewByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("EmpId : " + leaveDTO.getEmpId() + " ,FromDate :" + leaveDTO.getFromDate() + 
				" ,ToDate :" + leaveDTO.getToDate());

		try {
			
			LocalDate start = LocalDate.parse(leaveDTO.getFromDate());

			LocalDate end = LocalDate.parse(leaveDTO.getToDate());

			List<Object[]> list = employeeLeaveRepository.getAllTeamCompOffHistoryViewByEmpId(leaveDTO.getEmpId(),start,end);
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No teams leave history found");
				apiLogInfo.setApiResponse("NO teams leave history found");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			} else {

				list.forEach((object) -> {
					LeaveDTO dto = new LeaveDTO();
					dto.setCreatedByName(object[0] != null ? object[0].toString() : null);
					dto.setFromDate(object[1] != null ? object[1].toString() : null);
					dto.setToDate(object[2] != null ? object[2].toString() : null);
					dto.setCreatedOn(object[3] != null ? object[3].toString() : null);
					dto.setNoOfDays(object[4] != null ? Float.parseFloat(object[4].toString()) : null);
					dto.setStatus(object[5] != null ? object[5].toString() : null);
					dto.setReason(object[6] != null ? object[6].toString() : null);
					dto.setLeaveType(object[7] != null ? object[7].toString() : null);
					dto.setLeaveStatusUpdatedByName(object[8] != null ? object[8].toString() : null);
					dtoList.add(dto);					
					});

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("AllTeamCompoff History View Fetched:" + dtoList.size());			
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
	
	public ServiceResponse checkTeamName(TeamDTO teamdto) {
		
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Check Team Name");
		apiLogInfo.setApiUrl("/api/checkTeamName");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("TeamName : "+ teamdto.getTeamName() + " ,TeamId :" + teamdto.getTeamId()
		 + " ,ProjectId :" + teamdto.getProjectId() + " ,ProjectName : " + teamdto.getProjectName());
		try {
			
			if(teamdto.getTeamId() != null && teamdto.getProjectId() != null) {
				
				Team checkTeamNameByName=teamRepository.findByTeamNameAndTeamIdAndProjectIdAndIsActive(teamdto.getTeamName(),teamdto.getTeamId(), teamdto.getProjectId(), "Y");
//				if(checkTeamNameByName != null) {
//					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//					response.setServiceResponse("Team Name already exist!");
//				}else {
//					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				}
				if((checkTeamNameByName != null) && !checkTeamNameByName.getTeamId().equals(teamdto.getTeamId())){
					if(checkTeamNameByName != null) {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Team Name already exist!");
						apiLogInfo.setApiResponse("Team Name already exist!");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					}
					System.out.println("   checkTeamNameByName = "+checkTeamNameByName);    
				}
				
			}else if(teamdto.getProjectId() == null && teamdto.getTeamName() != null && teamdto.getProjectName() != null) {
				
				Project projectObj = projectRepository.findByProjectName(teamdto.getProjectName());
				
				if(projectObj != null) {
					Team checkTeamNameByName=teamRepository.findByTeamNameAndProjectIdAndIsActive(teamdto.getTeamName(), projectObj.getProjectId(), "Y");
					
					if((checkTeamNameByName != null) && !checkTeamNameByName.getTeamId().equals(teamdto.getTeamId())){
						if(checkTeamNameByName != null) {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Team Name already exist!");
							apiLogInfo.setApiResponse("Team Name already exist!");			
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						}
						System.out.println("   checkTeamNameByName - "+checkTeamNameByName);
					}
				}
			}else {
				
				Team checkTeamNameByName=teamRepository.findByTeamNameAndProjectId(teamdto.getTeamName(), teamdto.getProjectId());
				if(checkTeamNameByName != null) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Team Name already exist!");
					apiLogInfo.setApiResponse("Team Name already exist!");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					apiLogInfo.setApiResponse("TeamName  Does Not Exist!");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}
				System.out.println("   checkTeamNameByName : "+checkTeamNameByName);
			}
			 
		}catch (Exception e) {
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

	public ServiceResponse migrateTeamList(TeamDTO team) {
		ServiceResponse response = new ServiceResponse();
		try {

			String teamLeadName = null;
			Employee getTeamLeadData = null;
			if(team.getTeamLeadId()!=null) {
				getTeamLeadData = employeeRepository.findByEmployeementId(team.getTeamLeadId());
			}
			Employee getEmpData = employeeRepository.findByEmployeementId(team.getEmployeementId());
			Team teamAlreadyPresent = teamRepository.findByTeamName(team.getTeamName());
			Project projectPresent = projectRepository.findByProjectName(team.getProjectName());
			
				// create team & add member
				
				if (team.getTeamLeadId() != null) {
					if(getTeamLeadData != null) {
						teamLeadName = getTeamLeadData.getName();
					}else {
						teamLeadName = "NA";
					}
				}else {
					teamLeadName = "NA";
				}
				
				if(teamAlreadyPresent == null) {
					
					Team newTeam = new Team();

					newTeam.setTeamName(team.getTeamName());
					newTeam.setTeamLeadId(team.getTeamLeadId());
					newTeam.setProjectId(projectPresent.getProjectId());
					newTeam.setTeamLeadName(teamLeadName);
					newTeam.setCreatedBy(team.getCreatedBy());
					newTeam.setCreatedOn(new Timestamp(System.currentTimeMillis())); 

					Team teamCreated = teamRepository.save(newTeam);

					if (teamCreated != null) {

						EmployeeTeamMap map = new EmployeeTeamMap();

						map.setEmpId(getEmpData.getEmpId());
						map.setTeamId(teamCreated.getTeamId());
						// 1: Active 0: InActive
						map.setActive((long) 1);

						employeeTeamMapRepository.save(map);
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Team Created Successfully");

					}else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Team creation failed.");
					}
				} else {
					EmployeeTeamMap map = new EmployeeTeamMap();
					map.setEmpId(getEmpData.getEmpId());
					map.setTeamId(teamAlreadyPresent.getTeamId());
					// 1: Active 0: InActive
					map.setActive((long) 1);

					employeeTeamMapRepository.save(map);
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Team Created Successfully");
				}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse migrateTeamActivityByList(ActivityDTO activityDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			Team teamAlreadyPresent = teamRepository.findByTeamName(activityDTO.getTeamName());
			
			if(teamAlreadyPresent != null) {
				
				Activity newActivity = new Activity();

				newActivity.setActivity(activityDTO.getActivity());
				newActivity.setEta(activityDTO.getEta());
				newActivity.setTeamId(teamAlreadyPresent.getTeamId());
				newActivity.getCommonProperty().setCreatedBy(activityDTO.getCreatedBy());

				Activity newActivityCreated = activitiesRepository.save(newActivity);

				Optional.ofNullable(newActivityCreated).ifPresentOrElse((activity) -> {

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("New activity added to team.");

				}, () -> {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Failed to add new activity to team.");
				});
				
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Team Not Found");
			}
			
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse migrateProjectByList(ProjectDTO project) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			Project projectObj = projectRepository.findByProjectName(project.getProjectName());
			
			if(projectObj != null) {
				
				projectObj.setPoProjectId(project.getPoProjectId());
				
				Project projectDbResponse = projectRepository.save(projectObj);
				
				if(projectDbResponse != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Added PoProject Id.");
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Fail.");
				}
			}else {
				
				Integer clientId = null;
				Optional<Client> clientObj = clientsRepository.findByClientName(project.getClientName());
				if(!clientObj.isEmpty()) {
					Client clientPresent = clientObj.get();
					clientId = clientPresent.getClientId();
				}else {
					// Add Client & Client Location
					
					Client newClient = new Client();
					newClient.setClientName(project.getClientName());
					Client clientDbResponse = clientsRepository.save(newClient);
					
					if(clientDbResponse != null) {
						clientId = clientDbResponse.getClientId();
						List<ClientLocation> locations = new ArrayList<>();
						
							ClientLocation newClientLocation = new ClientLocation();
							newClientLocation.setClientId(clientDbResponse.getClientId());
							newClientLocation.setClientLocation(project.getClientLocation());
							locations.add(newClientLocation);
							
						List<ClientLocation> clientLocationDbResponse = clientLocationRepository.saveAll(locations);
						
						if(!clientLocationDbResponse.isEmpty()) {
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("Client Location added");
						}else {
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("Failed to add client Location");
							return response;
						}
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Failed to add client");
						return response;
					}
				}
				
				//Find ProjectManager empId
				Long projManagerId = null;
				if(project.getProjectManagerId() != null) {
					Long employeementID = project.getProjectManagerId();
					Employee employee = employeeRepository.findByEmployeementId(employeementID);
					if(employee != null) {
						projManagerId = employee.getEmpId();
					}
				}else {
					Department dept = departmentRepository.findByName(project.getDepartmentName());
					projManagerId = dept.getHodId();
				}
				
				//Add project
				Project newProject = new Project();
				newProject.setProjectManagerId(projManagerId);
				newProject.setProjectName(project.getProjectName());
				newProject.setState(project.getState());
				newProject.setClientId(clientId);
				newProject.setPoProjectId(project.getPoProjectId());
				newProject.setActive("true");
				newProject.setSyncProject("true");
				newProject.setCreatedBy(3l);
				
				
				Project projectDbResponse = projectRepository.save(newProject);
				
				
				
				if(projectDbResponse != null) {
					
						Department departmentObj = departmentRepository.findByName(project.getDepartmentName());
						
						if(departmentObj != null) {
							ProjectDepartmentMap projectDeptMapObj = projectDepartmentMapRepository.
									findByProjectIdAndDeptId(projectDbResponse.getProjectId(),departmentObj.getDeptId());
							if(projectDeptMapObj == null) {
								//add department
								ProjectDepartmentMap projectDeptMap = new ProjectDepartmentMap();
								projectDeptMap.setProjectId(projectDbResponse.getProjectId());
								projectDeptMap.setDeptId(departmentObj.getDeptId());
								ProjectDepartmentMap projDeptMapDbResponse = projectDepartmentMapRepository.save(projectDeptMap);
							}
						}
					
					
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("project added successfully.");
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Unable to add Project.");
				}
				
			}
			
//			Employee getEmpData = null;
//			if(project.getEmployeementId() != null) {
//				getEmpData = employeeRepository.findByEmployeementId(project.getEmployeementId());
//			}
//			
//			Project projectPresent = projectRepository.findByProjectName(project.getProjectName());
//			
//			if(projectPresent == null) {
//				Project newProject = new Project();
//
//				newProject.setClientName(project.getClientName());
//				newProject.setClientLocation(project.getClientLocation());
//				newProject.setState(project.getState());
//				newProject.setProjectName(project.getProjectName());
//				newProject.setDescription(project.getDescription());
//				
//				if(getEmpData != null) {
//					newProject.setProjectManagerId(getEmpData.getEmpId());
//				}
//				
//				Project projectCreated = projectRepository.save(newProject);
//				
//				if(projectCreated != null) {
//					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//					response.setServiceResponse("Project created");
//				}else {
//					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//					response.setServiceResponse("project creation failed");
//				}
//			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse migrateClientByList(ProjectDTO project) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			Optional<Client> clientPresent = clientsRepository.findByClientName(project.getClientName());
			
			if(clientPresent.isEmpty()) {
				
				Client clientObj = new Client();
				clientObj.setClientName(project.getClientName());
				Client clientSaved = clientsRepository.save(clientObj);
				
				if(clientSaved != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Client added successfully");
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Failed to add new Client");
				}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Client already present");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse migrateClientLocationByList(ProjectDTO project) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			Optional<Client> clientPresent = clientsRepository.findByClientName(project.getClientName());
			
			if(!clientPresent.isEmpty()) {
				Client clientObj = clientPresent.get();
				
				ClientLocation clientLocationPresent = clientLocationRepository
						.findByClientIdAndClientLocation(clientObj.getClientId(), project.getClientLocation());
				
				if(clientLocationPresent != null) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Client Data Already present");
				}else {
					
					ClientLocation clientLocationObj = new ClientLocation();
					clientLocationObj.setClientId(clientObj.getClientId());
					clientLocationObj.setClientLocation(project.getClientLocation());
					
					ClientLocation clientLocationSaved = clientLocationRepository.save(clientLocationObj);
					if(clientLocationSaved != null) {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Client Location added successfully");
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Failed to add Client Location");
					}
				}
				
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Client not found");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse updateProjectByList(ProjectDTO project) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			Optional<Client> clientPresent = clientsRepository.findByClientName(project.getClientName());
			Project projectPresent = projectRepository.findByProjectName(project.getProjectName());
			
			if(!clientPresent.isEmpty()) {
				Client clientObj = clientPresent.get();
				
				if(projectPresent != null) {
					projectPresent.setClientId(clientObj.getClientId());
					
					Project projSaved = projectRepository.save(projectPresent);
					if(projSaved != null) {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Project Updated Successfully");
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Project Updation Failed");
					}
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Project not found");
				}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Client not found");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse migrateTeamMember(ProjectDTO project) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			EmployeeTeamMap map = new EmployeeTeamMap();

			map.setEmpId(project.getEmpId());
			map.setTeamId(project.getTeamId());
			// 1: Active 0: InActive
			map.setActive((long) 1);

			employeeTeamMapRepository.save(map);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Team Created Successfully");
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	public ServiceResponse addDepartmentInProjects() {
		ServiceResponse response = new ServiceResponse();
		try {
			
			List<Project> allProjects = projectRepository.findAll();
			
			if(allProjects != null) {
				
				for(Project obj : allProjects) {
					String deptCode = obj.getProjectName().split("-")[0];
					
					if(deptCode.equals("AUT")) {
						obj.setDepartmentName("Automation Testing");;
					}else if(deptCode.equals("FT")) {
						obj.setDepartmentName("Functional Testing");
					}else if(deptCode.equals("PT")) {
						obj.setDepartmentName("Performance Testing");
					}else if(deptCode.equals("APM")) {
						obj.setDepartmentName("APM");
					}else if(deptCode.equals("DEV")) {
						obj.setDepartmentName("Development");
					}else if(deptCode.equals("PS")) {
						obj.setDepartmentName("Production Support");
					}else if(deptCode.equals("AC")) {
						obj.setDepartmentName("Accounts");
					}else if(deptCode.equals("MONITORING")) {
						obj.setDepartmentName("Application Performance Monitoring");
					}else if(deptCode.equals("MON")) {
						obj.setDepartmentName("Application Performance Monitoring");
					}else if(deptCode.equals("MONT")) {
						obj.setDepartmentName("Application Performance Monitoring");
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Unknown format");
					}
					
					Project dbResponse = projectRepository.save(obj);
					
					if(dbResponse != null) {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Department added successfully");
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("department addtion failed");
					}
				}
				
			}
			
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse setPoProjectIdAndDepartment(ProjectDTO projectDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			Project projectObj = projectRepository.findByProjectName(projectDTO.getProjectName());
			
			if(projectObj != null) {
				projectObj.setPoProjectId(projectDTO.getPoProjectId());
				projectObj.setDepartmentName(projectDTO.getDepartmentName());
				
				
				Project dbResponse = projectRepository.save(projectObj);
				
				if(dbResponse != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("PoProject Id & department added successfully");
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("PoProject Id & department updation failed");
				}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not found in PoPotal");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return null;
	}

//	public ServiceResponse getDepartmentLeaveHistory(LeaveDTO leaveDTO) {
//		ServiceResponse response = new ServiceResponse();
//		LogDTO apiLogInfo = new LogDTO();
//		//apiLogInfo.setSubFeatureName("");
//		apiLogInfo.setApiUrl("/api/getDepartmentLeaveHistory");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("DeptId : "+ leaveDTO.getDeptId() + "  ,FromDate : " + leaveDTO.getFromDate()
//		 + " ,ToDate : " + leaveDTO.getToDate());
//
//		try {
//			
//			LocalDate start = LocalDate.parse(leaveDTO.getFromDate());
//			LocalDate end = LocalDate.parse(leaveDTO.getToDate());
//			List<Object[]> objectList = employeeLeaveRepository.getDepartmentLeaveHistory(leaveDTO.getDeptId(),start,end);
//			
//			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
//			if (objectList.isEmpty()) {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("No department leave history found");
//				apiLogInfo.setApiResponse("No department leave history found");			
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//
//			} else {
//
//				objectList.forEach((object) -> {
//					LeaveDTO dto = new LeaveDTO();
//					dto.setCreatedByName(object[0] != null ? object[0].toString() : null);
//					dto.setFromDate(object[1] != null ? object[1].toString() : null);
//					dto.setToDate(object[2] != null ? object[2].toString() : null);
//					dto.setCreatedOn(object[3] != null ? object[3].toString() : null);
//					dto.setNoOfDays(object[4] != null ? Float.parseFloat(object[4].toString()) : null);
//					dto.setStatus(object[5] != null ? object[5].toString() : null);
//					dto.setReason(object[6] != null ? object[6].toString() : null);
//					dto.setLeaveType(object[7] != null ? object[7].toString() : null);
//					dto.setLeaveStatusUpdatedByName(object[8] != null ? object[8].toString() : null);
//					dto.setLeaveId(object[9] != null ? Long.parseLong(object[9].toString()) : null);
//					dto.setRemark(object[10] != null ? object[10].toString() : null);
//					dtoList.add(dto);					
//					});
//
//				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				response.setServiceResponse(dtoList);
//				apiLogInfo.setApiResponse("Department leave History fetched");			
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//
//			}
//		}catch(Exception e) {
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
	
	// added by anurag
	
	public ServiceResponse getDepartmentLeaveHistory(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("");
		apiLogInfo.setApiUrl("/api/getDepartmentLeaveHistory");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("DeptId : "+ leaveDTO.getDeptId() + "  ,FromDate : " + leaveDTO.getFromDate()
		 + " ,ToDate : " + leaveDTO.getToDate());

		try {
			List<String> jobRoles = null;
			List<Object[]> objectList = null;
			
			LocalDate start = LocalDate.parse(leaveDTO.getFromDate());
			LocalDate end = LocalDate.parse(leaveDTO.getToDate());
			
			
			if(leaveDTO.getEmployeeRole().equals("HOD")) {
				 jobRoles = new ArrayList<String>(Arrays.asList("SuperAdmin"));
				 objectList = employeeLeaveRepository.getDepartmentLeaveHistoryAndNotIn(leaveDTO.getEmpId(),start,end,jobRoles); 
			}
			if(leaveDTO.getEmployeeRole().equals("Manager")) {
				 jobRoles = new ArrayList<String>(Arrays.asList("SuperAdmin","HOD"));
				 objectList = employeeLeaveRepository.getDepartmentLeaveHistoryAndNotIn(leaveDTO.getEmpId(),start,end,jobRoles);
			}
			if(leaveDTO.getEmployeeRole().equals("SuperAdmin"))  {
				 objectList = employeeLeaveRepository.getDepartmentLeaveHistory(leaveDTO.getDeptId(),start,end);
			}
			
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			if (objectList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No department leave history found");
				apiLogInfo.setApiResponse("No department leave history found");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

			} else {

				objectList.forEach((object) -> {
					LeaveDTO dto = new LeaveDTO();
					dto.setEmployeementId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setCreatedByName(object[1] != null ? object[1].toString() : null);
					dto.setFromDate(object[2] != null ? object[2].toString() : null);
					dto.setToDate(object[3] != null ? object[3].toString() : null);
					dto.setCreatedOn(object[4] != null ? object[4].toString() : null);
					dto.setNoOfDays(object[5] != null ? Float.parseFloat(object[5].toString()) : null);
					dto.setStatus(object[6] != null ? object[6].toString() : null);
					dto.setReason(object[7] != null ? object[7].toString() : null);
					dto.setLeaveType(object[8] != null ? object[8].toString() : null);
					dto.setLeaveStatusUpdatedByName(object[9] != null ? object[9].toString() : null);
					
					dto.setLeaveStatusUpdatedBy(object[10] != null ? Long.parseLong(object[10].toString()) : null);
					dto.setLeaveId(object[11] != null ? Long.parseLong(object[11].toString()) : null);
					dto.setRemark(object[12] != null ? object[12].toString() : null);
										
					dto.setApproverName(object[13] != null ? object[13].toString() : null);
					
					dto.setApproverId(object[14] != null ? Long.parseLong(object[14].toString()) : null);

					dto.setApproverEmail(object[15] != null ? object[15].toString() : null);

					dto.setManagerApprovalStatus(object[16] != null ? object[16].toString() : null);
					dto.setLevel2ApproverId(object[17] != null ? Long.parseLong(object[17].toString()) : null);
					dto.setLevel2ApproverName(object[18] != null ? object[18].toString() : null);
					dto.setLevel2ApproverEmail(object[19] != null ? object[19].toString() : null);
					dto.setLevel2ApprovalStatus(object[20] != null ? object[20].toString() : null);

					dto.setLevel3ApproverId(object[21] != null ? Long.parseLong(object[21].toString()) : null);
					dto.setLevel3ApproverName(object[22] != null ? object[22].toString() : null);
					dto.setLevel3ApprovalStatus(object[23] != null ? object[23].toString() : null);
					dto.setLevel3ApproverEmail(object[24] != null ? object[24].toString() : null);

					dto.setCurrentApprovalLevel(object[25] != null ? Integer.parseInt(object[25].toString()) : null);
					dto.setFinalApprovalLevel(object[26] != null ? Integer.parseInt(object[26].toString()) : null);

					dtoList.add(dto);					
					});

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("Department leave History fetched");			
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
	
	public ServiceResponse getDepartmentPendingLeaveHistory(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("");
		apiLogInfo.setApiUrl("/api/getDepartmentPendingLeaveHistory");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("DeptId : "+ leaveDTO.getDeptId() + "  ,FromDate : " + leaveDTO.getFromDate()
		 + " ,ToDate : " + leaveDTO.getToDate());

		try {
			
//			added by anurag
			
			List<String> jobRoles = null;
			List<Object[]> objectList = null;
			
			if(leaveDTO.getEmployeeRole().equals("HOD")) {
				jobRoles = new ArrayList<String>(Arrays.asList("SuperAdmin"));
				objectList = employeeLeaveRepository.getDepartmentPendingLeaveHistoryStatusNotIn(leaveDTO.getDeptId(),jobRoles);
			}
			if(leaveDTO.getEmployeeRole().equals("Manager")) {
				jobRoles = new ArrayList<String>(Arrays.asList("HOD"));
				objectList = employeeLeaveRepository.getDepartmentPendingLeaveHistoryStatusNotIn(leaveDTO.getDeptId(),jobRoles);
			}
			if(leaveDTO.getEmployeeRole().equals("SuperAdmin")) {
				objectList = employeeLeaveRepository.getDepartmentPendingLeaveHistory(leaveDTO.getDeptId());	
			}
			
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			if (objectList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No department leave history found");
				apiLogInfo.setApiResponse("No department leave history found");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

			} else {

				objectList.forEach((object) -> {
					LeaveDTO dto = new LeaveDTO();
					dto.setEmployeementId(object[0] != null ? Long.parseLong(object[0].toString()) : null);					
					dto.setCreatedByName(object[1] != null ? object[1].toString() : null);
					dto.setFromDate(object[2] != null ? object[2].toString() : null);
					dto.setToDate(object[3] != null ? object[3].toString() : null);
					dto.setCreatedOn(object[4] != null ? object[4].toString() : null);
					dto.setNoOfDays(object[5] != null ? Float.parseFloat(object[5].toString()) : null);
					dto.setStatus(object[6] != null ? object[6].toString() : null);
					dto.setReason(object[7] != null ? object[7].toString() : null);
					dto.setLeaveType(object[8] != null ? object[8].toString() : null);
					dto.setLeaveStatusUpdatedByName(object[9] != null ? object[9].toString() : null);
					dto.setLeaveId(object[10] != null ? Long.parseLong(object[10].toString()) : null);
					dto.setRemark(object[11] != null ? object[11].toString() : null);
					
//					added by anurag
					
					dto.setApproverName(object[12] != null ? object[12].toString() : null);
					dto.setApproverEmail(object[13] != null ? object[13].toString() : null);

					dto.setManagerApprovalStatus(object[14] != null ? object[14].toString() : null);
					dto.setLevel2ApproverId(object[15] != null ? Long.parseLong(object[15].toString()) : null);
					dto.setLevel2ApproverName(object[16] != null ? object[16].toString() : null);
					dto.setLevel2ApproverEmail(object[17] != null ? object[17].toString() : null);
					dto.setLevel2ApprovalStatus(object[18] != null ? object[18].toString() : null);
					
					dto.setLevel3ApproverId(object[19] != null ? Long.parseLong(object[19].toString()) : null);
					dto.setLevel3ApproverName(object[20] != null ? object[20].toString() : null);
					dto.setLevel3ApprovalStatus(object[21] != null ? object[21].toString() : null);
					dto.setLevel3ApproverEmail(object[22] != null ? object[22].toString() : null);
					
					dto.setCurrentApprovalLevel(object[23] != null ? Integer.parseInt(object[23].toString()) : null);
					dto.setFinalApprovalLevel(object[24] != null ? Integer.parseInt(object[24].toString()) : null);
					
					
					
					
					dtoList.add(dto);					
					});

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("Department leave History fetched");			
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

	
	
//	end
	
	

	public ServiceResponse addDeptIdsInActivities() {
		ServiceResponse response = new ServiceResponse();
		
		try {
			
			List<Team> allTeamList = teamRepository.findAll();
			if(!allTeamList.isEmpty()) {
				allTeamList.forEach((teamOBj) -> {
					// Find team memeber's Department
					Set<String> deptIds = new HashSet<String>();
					
					List<EmployeeTeamMap> allTeamMemeber = employeeTeamMapRepository.findByTeamId(teamOBj.getTeamId());
					if(!allTeamMemeber.isEmpty()) {
						allTeamMemeber.forEach((teamMapObj) -> {
							// get Department of each employee
							
							List<Object[]> getEmpDepartemnt = employeeRepository.getEmployeeData(teamMapObj.getEmpId());
							if(!getEmpDepartemnt.isEmpty()) {
								getEmpDepartemnt.forEach((empObj) -> {
									String deptId = empObj[4] != null ? empObj[4].toString() : null;
									
									deptIds.add(deptId);
								});
							}
						});
					}
					//Add deptIds in teams
//					String departemntIds = String.join(",", deptIds);
//					teamOBj.setDeptIds(departemntIds);
//					Team teamDbResposnse = teamRepository.save(teamOBj);
//					
//					if(teamDbResposnse != null) {
//						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//						response.setServiceResponse("deptIds added successfully in team table");
//					}else {
//						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//						response.setServiceResponse("deptIds failed to add");
//					}
					
					// Get team activities to set deptIds
					List<Activity> teamActivity = activitiesRepository.findByTeamId(teamOBj.getTeamId());
					if(!teamActivity.isEmpty()) {
						teamActivity.forEach((activityObj) -> {
							String departemntIds = String.join(",", deptIds);
							activityObj.setDeptIds(departemntIds);
							Activity activityDbResponse = activitiesRepository.save(activityObj);
							
							if(activityDbResponse != null) {
								response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
								response.setServiceResponse("deptIds added successfully in Activity table");
							}else {
								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
								response.setServiceResponse("deptIds failed to add");
							}
						});
					}
				});
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse addProjectDepartmentMapping() {
		ServiceResponse response = new ServiceResponse();
		try {
			
			List<Project> allProject = projectRepository.findAll();
			if(!allProject.isEmpty()) {
				allProject.forEach((projObj) -> {
					Department deptObj = departmentRepository.findByName(projObj.getDepartmentName());
					
					if(deptObj != null) {
						ProjectDepartmentMap projectDeptMap = new ProjectDepartmentMap();
						projectDeptMap.setProjectId(projObj.getProjectId());
						projectDeptMap.setDeptId(deptObj.getDeptId());
						ProjectDepartmentMap projDeptMapDbResponse = projectDepartmentMapRepository.save(projectDeptMap);
						
						if(projDeptMapDbResponse != null) {
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("Project Department mapping completed");
						}else {
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("Failed to mapp Project & department");
						}
					}
				});
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return null;
	}

	public ServiceResponse addProjectManager() {
		ServiceResponse response = new ServiceResponse();
		try {
			
			List<Project> allProject = projectRepository.findAll();
			
			if(!allProject.isEmpty()){
				allProject.forEach(obj -> {
					if(obj.getProjectManagerId() == null && (obj.getDepartmentName() != null)) {
						Department dept = departmentRepository.findByName(obj.getDepartmentName());
						
						obj.setProjectManagerId(dept.getHodId());
						Project dbResponse = projectRepository.save(obj);
						
						if(dbResponse != null) {
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("Project manager added");
						}else {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Failed to add project manager");
						}
					}
				});
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

    @Transactional
	public ServiceResponse revokeReporteeLeave(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("RevokeReporteeLeave");
		apiLogInfo.setApiUrl("/api/revokeReporteeLeave");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("LeaveId : "+ leaveDTO.getLeaveId() + " , EmpId :" + leaveDTO.getEmpId() + 
				 " ,LeaveType :" + leaveDTO.getLeaveType() + " ,leaveRevokeId :"+ leaveDTO.getLeaveRevokeId());
		try {
			
			EmployeeLeave leaveApplication = employeeLeaveRepository.findByLeaveId(leaveDTO.getLeaveId());
			
			if(leaveApplication != null) {
				leaveApplication.setLeaveStatusId((short)5);
				leaveApplication.setLeaveStatusUpdatedBy(leaveDTO.getLeaveStatusUpdatedBy());
				
				EmployeeLeave dbResponse = employeeLeaveRepository.save(leaveApplication);
				
				if(dbResponse != null) {
					
					//Delete Timesheet Application regarding leave
					
					List<EmployeeTimesheetsNew> empTimesheet = timesheetsRepositoryNew.findByEmpIdAndDateBetween(leaveApplication.getEmpId(),leaveApplication.getFromDate(),leaveApplication.getToDate());

					if (empTimesheet != null&& !empTimesheet.isEmpty()) {
						for (EmployeeTimesheetsNew ts : empTimesheet) {
                            Long currentTsId = ts.getTimesheetId();
                            timesheetsRepositoryNew.cleanTimesheetById(currentTsId);
                        }
					}
                    // entityManager.flush();
					
					//Get Expiration Period of CompOff
					Integer expirationPeriod = null;
					Employee employeeObj = employeeRepository.findByEmpId(leaveApplication.getEmpId());
					Optional<LeaveTypeMaster> leaveType = leaveTypeMasterRepository.findById(leaveApplication.getLeaveTypeMasterId());
					LeaveTypeMaster leaveTypeObj = leaveType.get();
					if(leaveTypeObj.getLeaveTypeCode().equals("CO")) {
						LeavePolicyMaster leavePolicy  = leavePolicyMasterRepository.findByLeaveTypeMasterIdAndEmploymentStatus(leaveTypeObj.getLeaveTypeMasterId(),employeeObj.getEmploymentstatus());
					      if(leavePolicy != null){
					    	  if(leavePolicy.getExpirationPeriod().equals("Yes")) {
						        	 expirationPeriod = leavePolicy.getExpirationPeriodValue();
				               }
					      }
					}
					
					//Manage Employee leave balance
					
					if(!leaveTypeObj.getLeaveTypeCode().equals("CO")){
						EmployeeLeavesMap empLeaveMapping = employeeLeavesMapRepository
								.findByEmpIdAndLeaveTypeMasterId(leaveApplication.getEmpId(), leaveApplication.getLeaveTypeMasterId());
						
						if(empLeaveMapping != null) {
							Float prevBalace = empLeaveMapping.getBalance();
							empLeaveMapping.setBalance(prevBalace + leaveApplication.getNoOfDays());
							
							EmployeeLeavesMap mapDbResponse = employeeLeavesMapRepository.save(empLeaveMapping);
							
							if(mapDbResponse != null) {
								LeaveBalanceLog log = new LeaveBalanceLog();
								
								log.setBalance(mapDbResponse.getBalance());
								log.setEmpId(leaveApplication.getEmpId());
								log.setLeaveTypeMasterId(leaveApplication.getLeaveTypeMasterId());
								log.setMessage(LeaveLogMessage.leaveRevokedByManager.replace("0.0", leaveDTO.getNoOfDays().toString()));
								log.setUpdateBalanceBy("+" + leaveDTO.getNoOfDays());

								LeaveBalanceLog balanceDbResponse = leaveBalanceLogRepository.save(log);
								
								if(balanceDbResponse != null) {
									
									Employee empObj = employeeRepository.findByEmpId(leaveApplication.getEmpId());
									Employee managerObj = employeeRepository.findByEmpId(leaveDTO.getLeaveStatusUpdatedBy());
									
									if(empObj != null && managerObj != null) {
										
										//Send Mail to User with cc: HR,HOD,Manager
										mailService.sendMailWithCC(empObj.getEmail(), hrMailAddress +","+ managerObj.getEmail(),
												"Regarding Revoke Leave Application By Manager",
												"Dear "+ empObj.getName() + ","+"<br>"
												+"<br>"+" &nbsp"+" &nbsp"+" "+"Leave Application has been revoked by "+managerObj.getName()+
												"<br>"+"<br>"+"<b>"+"Leave Details"+"<b>"+
												"<br>"+
												"EmpID :"+" "+ empObj.getEmployeementId()+
												"<br>"+
												"Name :"+" "+ empObj.getName()+
												"<br>"+
												" From :"+" "+ leaveApplication.getFromDate()+
												"<br>"+
												" To :"+" "+ leaveApplication.getToDate() +
												"<br>"+
												" No. Of Days : "+ leaveApplication.getNoOfDays() + " day(s)" 
												+"<br>"+
												" Leave Type :"+" "+leaveDTO.getLeaveType()+
												"<br>"+
												"leave Reason :"+" "+leaveDTO.getRevokeReason());
									}
									
									response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
									response.setServiceResponse("Leave revoked successfully.");	
									apiLogInfo.setApiResponse("Leave revoked successfully");
									apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

								}else {
									response.setServiceStatus(ServiceResponse.STATUS_FAIL);
									response.setServiceResponse("Unable to revoke leave.");
									apiLogInfo.setApiResponse("Unable to revoke leave");
									apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

								}
							}
						}else {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Employee Leave Balance details not found.");
							apiLogInfo.setApiResponse("Employee leave balance details not found");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

						}
					}
					
					// CompOff Leave : 4 (LeaveTypeMasterId)
					if(leaveTypeObj.getLeaveTypeCode().equals("CO")) {
						List<CompOffLeave> compOffLeave = compOffLeaveRepository.findByLeaveId(leaveApplication.getLeaveId());
						
						if(!compOffLeave.isEmpty()) {
								
								for(CompOffLeave leave: compOffLeave) {
									
									LocalDate expireDate = leave.getFromDate().plusDays(expirationPeriod != null ? expirationPeriod : 30);
									if(LocalDate.now().isAfter(expireDate) || LocalDate.now().isEqual(expireDate)) {
										
										leave.setCompOffStatus("Expired");
										compOffLeaveRepository.save(leave);
									}else {
										
										//Update Leave Balance

										EmployeeLeavesMap empLeaveMapping = employeeLeavesMapRepository
												.findByEmpIdAndLeaveTypeMasterId(leaveApplication.getEmpId(), leaveApplication.getLeaveTypeMasterId());
										
										if(empLeaveMapping != null) {
											Float prevBalace = empLeaveMapping.getBalance();
											empLeaveMapping.setBalance(prevBalace + leave.getNoOfDays());
											
											EmployeeLeavesMap mapDbResponse = employeeLeavesMapRepository.save(empLeaveMapping);
											
											if(mapDbResponse != null) {
												LeaveBalanceLog log = new LeaveBalanceLog();
												
												log.setBalance(mapDbResponse.getBalance());
												log.setEmpId(leaveApplication.getEmpId());
												log.setLeaveTypeMasterId(leaveApplication.getLeaveTypeMasterId());
												log.setMessage(LeaveLogMessage.leaveRevokedByManager.replace("0.0", leave.getNoOfDays().toString()));
												log.setUpdateBalanceBy("+" + leave.getNoOfDays());

												LeaveBalanceLog balanceDbResponse = leaveBalanceLogRepository.save(log);
											}
										}
									
										
										// change compOff application status
										leave.setCompOffStatus("Pending");
										leave.setLeaveId(null);
										
										compOffLeaveRepository.save(leave);
									}
									
								}
						}
					}
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Unable to revoke Leave Application.");
					apiLogInfo.setApiResponse("Unable to revoke leave Application");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

				}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave Application not found.");
				apiLogInfo.setApiResponse("Leave Application not found");
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

	
	public ServiceResponse getAllTeams() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("");
		apiLogInfo.setApiUrl("/api/getAllTeams");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		try {
			
			List<Object[]> objectList = teamRepository.getAllTeams();
			logBuilder.append("AllTeamList : " + objectList.size());

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No teams found. Teams list is empty");
					apiLogInfo.setApiResponse("No teams found. Teams list is empty");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				} else {
					
					List<TeamDTO> dtoList = new ArrayList<TeamDTO>();
						
						list.forEach((object) -> {
							TeamDTO dto = new TeamDTO();

							dto.setProjectId(object[0] != null ? Integer.parseInt(object[0].toString()) : null);
							dto.setProjectName(object[1] != null ? object[1].toString() : null);
							dto.setTeamId(object[2] != null ? Long.parseLong(object[2].toString()) : null);
							dto.setTeamName(object[3] != null ? object[3].toString() : null);
							dto.setTeamLeadId(object[4] != null ? Long.parseLong(object[4].toString()) : null);
							dto.setTeamLeadName(object[5] != null ? object[5].toString() : null);
							dto.setProjectManagerId(object[6] != null ? Long.parseLong(object[6].toString()) : null);
							dto.setProjectManagerName(object[7] != null ? object[7].toString() : null);
							dto.setDepartmentName(object[8] != null ? object[8].toString() : null);
							dto.setCreatedByName(object[9] != null ? object[9].toString() : null);
							dto.setCreatedOn(object[10] != null ? object[10].toString() : null);
							dto.setIsActive(object[11] != null ? object[11].toString() : null);
							dto.setTeamLeadDeptId(object[12] != null ? Long.parseLong(object[12].toString()) : null);
							dto.setDepartmentList(object[13] != null ? object[13].toString().split(",") : null);
							
							dtoList.add(dto);
						});
						
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("All Team List Fetched");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No teams found.Teams list is null");
				apiLogInfo.setApiResponse("No teams found.Teams List is null");			
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

	@Transactional(readOnly = true)
	public ServiceResponse getAllTeamsByPoId(Long poId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getAllTeamsByPoId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("PoId : " + poId);
		try {
			if(poId == null) {
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiResponse("PO Id cannot be null!!");
				response.setServiceResponse("PO Id cannot be null!!");
				return response;
			}
			
			List<RmgTeamDto> rmgTeamDtoList = teamRepository.getActiveTeamDetailsByPoId(poId);
			if (rmgTeamDtoList.isEmpty()) {
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiResponse("No teams found. Teams list is empty");
				response.setServiceResponse("No teams found. Teams list is empty");
				return response;
			}
			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(rmgTeamDtoList);
			apiLogInfo.setApiResponse("Teams List fetched successfully!!");
		} catch (Exception e) {
			log.error("Error in getAllTeamsByPoId : ", e);
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong!!");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Transactional(readOnly = true)
	public ServiceResponse getTeamDetailsByTeamId(Long teamId, Integer projectId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getTeamDetailsByTeamId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("TeamId : " + teamId);
		try {
			if (teamId == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Team Id cannot be null!!");
				return response;
			}
			if (projectId == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project Id cannot be null!!");
				return response;
			}

			Project existingProject = projectRepository.findByProjectId(projectId);
			if (existingProject == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not found!!");
				return response;
			}

			List<RmgTeamMemberDto> teamMemberDetailsList = teamRepository
					.getAllTeamMemberDetailsDtoByTeamIdAndProjectId(teamId, projectId.longValue());
			if (teamMemberDetailsList == null || teamMemberDetailsList.isEmpty()) {
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setApiResponse("Team Members List is Empty!!");
				response.setServiceResponse(new ArrayList<RmgTeamMemberDto>());
				return response;
			}

			List<Long> empIds = teamMemberDetailsList.stream().map(RmgTeamMemberDto::getEmpId)
					.collect(Collectors.toList());

			Map<Long, List<EmployeeOtherActiveProject>> empIdAndOtherProjectIdsMap = getEmployeeOtherActiveProjectIdMap(
					empIds, projectId);

			Map<Long, EmployeeInformationDTO> empIdInfoMap = getEmployeeInformationMap(empIds);

			for (RmgTeamMemberDto obj : teamMemberDetailsList) {

				EmployeeInformationDTO dto = empIdInfoMap.getOrDefault(obj.getEmpId(), null);
				if (dto != null) {
					obj.setEmpId(dto.getEmpId());
					obj.setEmployementId(dto.getEmploymentId());
					obj.setMemberDepartment(dto.getDeptName());
					obj.setJobRoleName(dto.getJobRole());
					obj.setBillableType(dto.getBillableType());
					obj.setPrevExp(dto.getPreviousExperience());
					obj.setCurrentExp(dto.getCurrentExperience());
					obj.setTotalExp(dto.getTotalExperience());
					obj.setTotalExp(dto.getTotalExperience());
					obj.setEmploymentStatus(dto.getEmploymentStatus());
				}
				obj.setOtherActiveProjects(empIdAndOtherProjectIdsMap.getOrDefault(obj.getEmpId(), List.of()));
				
				if (obj.getOtherActiveProjects() != null && !obj.getOtherActiveProjects().isEmpty()) {
					List<Integer> projectIds = obj.getOtherActiveProjects().stream()
							.map(e -> e.getProjectId())
							.collect(Collectors.toList());
					obj.setOtherActiveProjectIds(projectIds);
				} else {
					obj.setOtherActiveProjectIds(List.of());
				}
				obj.setDisplayRequirement(getDisplayRequirement(obj));
			}
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(teamMemberDetailsList);
			apiLogInfo.setApiResponse("Team Members List fetched successfully!!");
		} catch (Exception e) {
			log.error("Error in getTeamDetailsByTeamId : ", e);
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong!!");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	private String getDisplayRequirement(RmgTeamMemberDto obj) {
		StringBuilder sb = new StringBuilder();
		appendIfNotNull(sb, "Role", obj.getRole());
		appendIfNotNull(sb, "Experience", obj.getExperience());
		appendIfNotNull(sb, "Department", obj.getDepartment());
		return sb.toString();
	}

	private void appendIfNotNull(StringBuilder sb, String label, Object value) {
		if (value != null) {
			if (sb.length() > 0) {
				sb.append(" | ");
			}
			sb.append(label).append(" : ").append(value);
		}
	}

	private Map<Long, EmployeeInformationDTO> getEmployeeInformationMap(List<Long> empIds) {
		Map<Long, EmployeeInformationDTO> empIdInfoMap = new HashMap<>();
		List<Object[]> results = employeeRepository.getEmployeeInformationIn(empIds);
		if (results != null && !results.isEmpty()) {
			for (Object[] obj : results) {
				Long empId = TypeConversionUtil.safeParseLong(obj[0]);
				EmployeeInformationDTO dto = new EmployeeInformationDTO();
				dto.setEmpId(empId);
				dto.setEmploymentId(TypeConversionUtil.getSafeString(obj[1]));
				dto.setName(TypeConversionUtil.getSafeString(obj[2]));
				dto.setPreviousExperience(TypeConversionUtil.getSafeString(obj[3]));
				dto.setCurrentExperience(TypeConversionUtil.getSafeString(obj[4]));
				dto.setTotalExperience(TypeConversionUtil.getSafeString(obj[5]));
				dto.setBillableType(TypeConversionUtil.getSafeString(obj[6]));
				dto.setJobRole(TypeConversionUtil.getSafeString(obj[7]));
				dto.setDeptName(TypeConversionUtil.getSafeString(obj[8]));
				dto.setEmploymentStatus(TypeConversionUtil.getSafeString(obj[9]));
				empIdInfoMap.put(empId, dto);
			}
		}
		return empIdInfoMap;
	}

	private Map<Long, List<EmployeeOtherActiveProject>> getEmployeeOtherActiveProjectIdMap(List<Long> empIds, Integer projectId) {
		List<EmployeeOtherActiveProject> empOtherActiveProjectList = projectRepository.getOtherActiveProjectsByEmpIdIn(empIds, projectId);
		if (empOtherActiveProjectList == null || empOtherActiveProjectList.isEmpty()) {
			 return new HashMap<>();
		}
		return empOtherActiveProjectList.stream()
            .collect(Collectors.groupingBy(EmployeeOtherActiveProject::getEmpId));
	}

	@Transactional(readOnly = true)
	public ServiceResponse getActiveTeamDetailsByPoId(Long poId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getActiveTeamDetailsByPoId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("PoId : " + poId);
		try {
			if(poId == null){
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiResponse("PO Id cannot be null!!");
				response.setServiceResponse("PO Id cannot be null!!");
				return response;
			}
			
			List<RmgTeamDto> rmgTeamDtoList = teamRepository.getActiveTeamDetailsByPoId(poId);
			if (rmgTeamDtoList.isEmpty()) {
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiResponse("No active teams found!!");
				response.setServiceResponse("No active teams found!!");
				return response;
			}

			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(rmgTeamDtoList);
			apiLogInfo.setApiResponse("Teams List fetched successfully!!");
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error in getActiveTeamDetailsByPoId : ", e);
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong!!");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse migrateTeam(MigrateTeam migrateTeam) {
		ServiceResponse response = new ServiceResponse();
		try {
			// Validation
			response = validateMigrateTeamObject(migrateTeam);
			if (response != null && response.getServiceStatus().equals(ServiceResponse.STATUS_FAIL)) {
				return response;
			}
			response = new ServiceResponse();

			Project sourceProject = projectRepository.findByProjectId(migrateTeam.getSourceProjectId());
			if (sourceProject == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Source Project not found!!");
				return response;
			}
			Project targetProject = projectRepository.findByProjectId(migrateTeam.getTargetProjectId());
			if (targetProject == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Target Project not found!!");
				return response;
			}
			List<Team> activeTeams = teamRepository.findActiveTeamsByTeamIds(migrateTeam.getMigrationTeamIds());
			if (activeTeams == null || activeTeams.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("No Active teams found for this Project");
				return response;
			}
			
			// Copy hasClientSideId from source -> target project
			targetProject.setHasClientSideId(sourceProject.getHasClientSideId());
			projectRepository.save(targetProject);

			// Project-level mappings copy
			copyAndSavePODepartmentMapping(migrateTeam); // PoDepartmentMapping
			copyAndSaveProjectManagerMapping(migrateTeam); // ProjectManagerMapping
			copyAndSaveProjectOverheadMapping(migrateTeam); // ProjectOverheadMapping

			response = copyAndSaveTeamAndEmployeeDetails(migrateTeam, activeTeams);
			if (response != null && response.getServiceStatus().equals(ServiceResponse.STATUS_FAIL)) {
				return response;
			}

			response = new ServiceResponse();
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Team Migration completed successfully for " + activeTeams.size() + " teams.");

			sendTeamMigrationCompletedMail(migrateTeam, sourceProject, targetProject);
		} catch (Exception e) {
			response = new ServiceResponse();
			log.error("Error in Team Migration : ", e);
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	private ServiceResponse validateMigrateTeamObject(MigrateTeam migrateTeam) {
		ServiceResponse response = new ServiceResponse();
		if (migrateTeam == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Team Migration Object cannot be null!!");
			return response;
		}
		if (migrateTeam.getCurrentUserEmpId() == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Employee Id cannot be null!!");
			return response;
		}
		if (migrateTeam.getSourceProjectId() == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Source Project Id cannot be null!!");
			return response;
		}
		if (migrateTeam.getMigrationTeamIds() == null || migrateTeam.getMigrationTeamIds().isEmpty()) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Team Migration Ids cannot be null!!");
			return response;
		}
		if (migrateTeam.getTargetProjectId() == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Target Project Id cannot be null!!");
			return response;
		}
		if (migrateTeam.getTargetPoId() == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Target PO Id cannot be null!!");
			return response;
		}
		if (migrateTeam.isMergeTeam() && migrateTeam.getTargetTeamId() == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("When Merging team, Target Team Id cannot be null!!");
			return response;
		}
		List<String> srcActiveTeamNames = teamRepository.findActiveTeamNameByTeamIds(migrateTeam.getMigrationTeamIds());
		if (srcActiveTeamNames == null || srcActiveTeamNames.isEmpty()) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Selected Migration Team(s) Not found!!");
			return response;
		}
		if (srcActiveTeamNames != null && !srcActiveTeamNames.isEmpty()) {
			List<String> tgtActiveTeamNames = teamRepository
					.findActiveTeamNameByProjectId(migrateTeam.getTargetProjectId());
			boolean hasCommonTeams = srcActiveTeamNames.stream().anyMatch(tgtActiveTeamNames::contains);
			if (hasCommonTeams) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse(
						"Team(s) with the same name already exists in the Target Project & PO, Kindly select the 'Merge into Existing Team' option!!");
				return response;
			}
		}
		return null;
	}

	private List<PoDepartmentMapping> copyAndSavePODepartmentMapping(MigrateTeam migrateTeam) {
		Integer srcProjectId = migrateTeam.getSourceProjectId();
		Integer tgtProjectId = migrateTeam.getTargetProjectId();
		List<PoDepartmentMapping> migratedPoDepartmentMapping = new ArrayList<>();

		List<PoDepartmentMapping> sourceDeptMappings = poDepartmentMappingRepository
				.findByProjectIdAndActive(srcProjectId);
		List<Long> targetDeptIds = poDepartmentMappingRepository
				.findPoDeptIdsByProjectId(tgtProjectId, false);

		for (PoDepartmentMapping s : sourceDeptMappings) {
			if (!targetDeptIds.contains(s.getDeptId())) {
				PoDepartmentMapping nm = new PoDepartmentMapping();
				nm.setPoId(migrateTeam.getTargetPoId());
				nm.setDeptId(s.getDeptId());
				nm.setActive(true);
				migratedPoDepartmentMapping.add(nm);
			}
		}
		if (!migratedPoDepartmentMapping.isEmpty()) {
			migratedPoDepartmentMapping = poDepartmentMappingRepository.saveAll(migratedPoDepartmentMapping);
		}
		return migratedPoDepartmentMapping;
	}

	private List<ProjectManagerMapping> copyAndSaveProjectManagerMapping(MigrateTeam migrateTeam) {
		Long srcProjectId = Long.parseLong(migrateTeam.getSourceProjectId().toString());
		Long tgtProjectId = Long.parseLong(migrateTeam.getTargetProjectId().toString());
		Long currentUserEmpId = migrateTeam.getCurrentUserEmpId();
		List<ProjectManagerMapping> migratedProjectManagerMappingList = new ArrayList<>();

		List<ProjectManagerMapping> sourceManagerMappings = projectManagerMappingRepository
				.findByProjectIdAndActive(srcProjectId, 1);
		List<Long> targetManagerIds = projectManagerMappingRepository
				.findProjectManagerIdByProjectIdAndActive(tgtProjectId, 1);

		for (ProjectManagerMapping s : sourceManagerMappings) {
			if (!targetManagerIds.contains(s.getProjectManagerId())) {
				ProjectManagerMapping nm = new ProjectManagerMapping();
				nm.setProjectId(tgtProjectId);
				nm.setProjectManagerId(s.getProjectManagerId());
				nm.setActive(1);
				nm.setCreatedBy(currentUserEmpId);
				nm.setCreatedOn(new Timestamp(System.currentTimeMillis()));
				migratedProjectManagerMappingList.add(nm);
			}
		}
		if (!migratedProjectManagerMappingList.isEmpty()) {
			migratedProjectManagerMappingList = projectManagerMappingRepository
					.saveAll(migratedProjectManagerMappingList);
		}
		return migratedProjectManagerMappingList;
	}

	private List<ProjectOverheadMapping> copyAndSaveProjectOverheadMapping(MigrateTeam migrateTeam) {
		Long srcProjectId = Long.parseLong(migrateTeam.getSourceProjectId().toString());
		Long tgtProjectId = Long.parseLong(migrateTeam.getTargetProjectId().toString());
		Long currentUserEmpId = migrateTeam.getCurrentUserEmpId();
		List<ProjectOverheadMapping> migratedProjectOverheadMappingList = new ArrayList<>();

		List<ProjectOverheadMapping> sourceOverheadMappings = projectOverheadMappingRepository
				.findByProjectIdAndActive(srcProjectId, 1);

		List<Long> targetOverheadIds = projectOverheadMappingRepository
				.findProjectOverheadIdByProjectIdAndActive(tgtProjectId, 1);

		for (ProjectOverheadMapping s : sourceOverheadMappings) {
			if (!targetOverheadIds.contains(s.getProjectOverheadId())) {
				ProjectOverheadMapping nm = new ProjectOverheadMapping();
				nm.setProjectId(tgtProjectId);
				nm.setProjectOverheadId(s.getProjectOverheadId());
				nm.setActive(1);
				nm.setCreatedBy(currentUserEmpId);
				nm.setCreatedOn(new Timestamp(System.currentTimeMillis()));
				migratedProjectOverheadMappingList.add(nm);
			}
		}
		if (!migratedProjectOverheadMappingList.isEmpty()) {
			migratedProjectOverheadMappingList = projectOverheadMappingRepository
					.saveAll(migratedProjectOverheadMappingList);
		}
		return migratedProjectOverheadMappingList;
	}

	private ServiceResponse copyAndSaveTeamAndEmployeeDetails(MigrateTeam migrateTeam, List<Team> activeSourceTeams) throws Exception {
		ServiceResponse serviceResponse = new ServiceResponse();

		Long srcProjectId = Long.parseLong(migrateTeam.getSourceProjectId().toString());
		Long tgtProjectId = Long.parseLong(migrateTeam.getTargetProjectId().toString());
		Long currentUserEmpId = migrateTeam.getCurrentUserEmpId();
		List<Long> migrationTeamIds = migrateTeam.getMigrationTeamIds();

		List<EmployeeTeamMap> sourceEmployeeTeamMapping = employeeTeamMapRepository
				.activeAndPendingEmployeesByTeamIds(migrationTeamIds);
		List<Activity> sourceTeamActivities = activitiesRepository.findByTeamIdIn(migrationTeamIds);

		if (sourceEmployeeTeamMapping != null && !sourceEmployeeTeamMapping.isEmpty()) {
			List<Long> sourceEmpIds = sourceEmployeeTeamMapping.stream().map(EmployeeTeamMap::getEmpId).distinct()
					.collect(Collectors.toList());

			// Mark old teams inactive
			markSourceTeamsInActive(activeSourceTeams, currentUserEmpId);
			List<Team> newTeams = copyAndSaveTeam(currentUserEmpId, tgtProjectId, activeSourceTeams, migrateTeam);
			if (newTeams == null || newTeams.isEmpty()) {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("Failed to migrate teams to the Selected Project!!");
				throw new Exception("Failed to migrate teams to the Selected Project!!");
			}

			// Build oldTeamId -> newTeam entity map
			Map<Long, Team> oldToNewTeamMap = newTeams.stream().filter(t -> t.getOldTeamId() != null)
					.collect(Collectors.toMap(Team::getOldTeamId, Function.identity()));

			// Mark old team mappings inactive
			markSourceTeamsEmployeeMappingInActive(sourceEmployeeTeamMapping, currentUserEmpId);
			copyAndSaveEmployeeTeamMapping(migrateTeam, currentUserEmpId, sourceEmployeeTeamMapping, oldToNewTeamMap);

			if (sourceEmpIds != null && !sourceEmpIds.isEmpty()) {
				boolean primaryMappingsChanged = false;
				List<EmpPrimaryProjectMapping> sourceEmpPrimaryProjectMapping = empPrimaryProjectMappingRepository
						.findByEmpIdInAndPrimaryProjectIdInAndIsMapped(sourceEmpIds, List.of(srcProjectId), "Y");

				if (sourceEmpPrimaryProjectMapping != null && !sourceEmpPrimaryProjectMapping.isEmpty()) {
					markSourceEmpPrimaryProjectMappingInActive(sourceEmpPrimaryProjectMapping, currentUserEmpId);
					primaryMappingsChanged = copyAndSaveEmpPrimaryProjectMapping(currentUserEmpId, tgtProjectId,
							sourceEmpPrimaryProjectMapping);
				}
				// update billable/billableType in employees
				if (primaryMappingsChanged) {
					updateEmployeeBillableType(migrateTeam.getTargetProjectId(), sourceEmpIds);
				}
			}

			// Update Employee client-side ID mappings
			List<EmployeeClientSideIdMapping> oldClientSideIdMappings = employeeClientSideIdMappingRepository
					.findByEmpIdInAndProjectIdInAndActive(sourceEmpIds, List.of(srcProjectId), true);

			if (!oldClientSideIdMappings.isEmpty()) {
				markSourceClientSideIdMappingInActive(oldClientSideIdMappings, currentUserEmpId);
				copyAndSaveClientSideIdMapping(currentUserEmpId, tgtProjectId, oldClientSideIdMappings);
			}

			// Create New Activities
			copyAndSaveActivities(currentUserEmpId, oldToNewTeamMap, sourceTeamActivities);
		}
		return null;
	}

	private List<Team> copyAndSaveTeam(Long currentUserEmpId, Long tgtProjectId, List<Team> activeSourceTeams,
			MigrateTeam migrateTeam) {
		if (migrateTeam.isMergeTeam() && migrateTeam.getTargetTeamId() != null) {
			Team exisingTeam = teamRepository.findByTeamId(migrateTeam.getTargetTeamId());
			if (exisingTeam == null) {
				throw new RuntimeException(
						"Unable to find the elected Target Team to migrate the selected Teams from the Project!!");
			}
			return List.of(exisingTeam);
		}
		List<Team> newTeams = activeSourceTeams.stream().map(oldTeam -> {
			Team newTeam = new Team();
			newTeam.setTeamName(oldTeam.getTeamName());
			newTeam.setTeamLeadId(oldTeam.getTeamLeadId());
			newTeam.setProjectId(Integer.parseInt(tgtProjectId.toString()));
			newTeam.setTeamLeadName(oldTeam.getTeamLeadName());
			newTeam.setIsActive("Y");
			newTeam.setDescription(oldTeam.getDescription());
			newTeam.setDeptIds(oldTeam.getDeptIds());
			newTeam.setSpocId(oldTeam.getSpocId());
			newTeam.setCreatedBy(currentUserEmpId);
			newTeam.setCreatedOn(Timestamp.valueOf(LocalDateTime.now()));
			newTeam.setOldTeamId(oldTeam.getTeamId());
			return newTeam;
		}).collect(Collectors.toList());
		return teamRepository.saveAll(newTeams);
	}

	private void markSourceTeamsInActive(List<Team> activeSourceTeams, Long currentUserEmpId) {
		LocalDateTime now = LocalDateTime.now();
		activeSourceTeams.forEach(t -> {
			t.setIsActive("N");
			t.setUpdatedBy(currentUserEmpId);
			t.setUpdatedOn(now);
		});
		teamRepository.saveAll(activeSourceTeams);
	}

	private void markSourceTeamsEmployeeMappingInActive(List<EmployeeTeamMap> sourceEmployeeTeamMapping,
			Long currentUserEmpId) {
		sourceEmployeeTeamMapping.forEach(etm -> {
			etm.setActive(0L);
			etm.setUpdatedOn(LocalDateTime.now());
			etm.setUpdatedBy(currentUserEmpId);
		});
		employeeTeamMapRepository.saveAll(sourceEmployeeTeamMapping);
	}

	private List<EmployeeTeamMap> copyAndSaveEmployeeTeamMapping(MigrateTeam migrateTeam, Long currentUserEmpId,
			List<EmployeeTeamMap> sourceEmployeeTeamMapping, Map<Long, Team> oldTeamIdNewTeamMap) {
		List<EmployeeTeamMap> newEmployeeTeamMapping = new ArrayList<>();

		for (EmployeeTeamMap oldMap : sourceEmployeeTeamMapping) {
			Team mappedNewTeam = oldTeamIdNewTeamMap.get(oldMap.getTeamId());

			if (mappedNewTeam != null) {
				EmployeeTeamMap newMap = new EmployeeTeamMap();
				newMap.setEmpId(oldMap.getEmpId());
				newMap.setTeamId(mappedNewTeam.getTeamId());
				newMap.setJobRoleId(oldMap.getJobRoleId());
				newMap.setActive(1L);
				newMap.setStartDate(LocalDateTime.now());
				newMap.setEmployeeRole(oldMap.getEmployeeRole());
				newMap.setEndDate(null);
				newMap.setIsShadow(oldMap.getIsShadow());
				newMap.setPoId(migrateTeam.getTargetPoId());
				newMap.setRoleId(migrateTeam.getTargetRoleId());
				newMap.setCreatedBy(currentUserEmpId);
				newMap.setCreatedOn(Timestamp.valueOf(LocalDateTime.now()));
				newMap.setUpdatedBy(null);
				newMap.setUpdatedOn(null);
				newEmployeeTeamMapping.add(newMap);
			}
		}
		if (!newEmployeeTeamMapping.isEmpty()) {
			newEmployeeTeamMapping = employeeTeamMapRepository.saveAll(newEmployeeTeamMapping);
		}
		return newEmployeeTeamMapping;
	}

	private void updateEmployeeBillableType(Integer tgtProjectId, List<Long> sourceEmpIds) {
		Project updatedTargetProject = projectRepository.findByProjectId(tgtProjectId);
		if (updatedTargetProject == null) {
			return;
		}
		String poProjectType = updatedTargetProject != null ? updatedTargetProject.getPoProjectType() : null;
		String ishineProjectType = updatedTargetProject != null ? updatedTargetProject.getInternalProjectType() : null;

		String billable = null;
		String billableType = null;

		if (poProjectType != null) {
			switch (poProjectType) {
				case "TNM":
					billable = "Yes";
					billableType = "TNM";
					break;
				case "Fixed Cost":
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
			employeeRepository.updateBillableAndTypeForEmpIds(billable, billableType, sourceEmpIds);
		}
	}

	private void markSourceEmpPrimaryProjectMappingInActive(
			List<EmpPrimaryProjectMapping> sourceEmpPrimaryProjectMapping, Long currentUserEmpId) {
		sourceEmpPrimaryProjectMapping.forEach(m -> {
			m.setIsMapped("N");
			m.setUpdatedOn(LocalDateTime.now());
			m.setUpdatedBy(currentUserEmpId);
		});
		empPrimaryProjectMappingRepository.saveAll(sourceEmpPrimaryProjectMapping);
	}

	private boolean copyAndSaveEmpPrimaryProjectMapping(Long currentUserEmpId, Long tgtProjectId,
			List<EmpPrimaryProjectMapping> sourceEmpPrimaryProjectMapping) {
		boolean flag = false;
		List<EmpPrimaryProjectMapping> newPrimaryMappings = new ArrayList<>();
		for (EmpPrimaryProjectMapping oldMap : sourceEmpPrimaryProjectMapping) {
			EmpPrimaryProjectMapping newMap = new EmpPrimaryProjectMapping();
			newMap.setEmpId(oldMap.getEmpId());
			newMap.setPrimaryProjectId(tgtProjectId);
			newMap.setPrimaryProjectName(oldMap.getPrimaryProjectName());
			newMap.setIsMapped("Y");
			newMap.setUpdatedBy(currentUserEmpId);
			newMap.setUpdatedOn(LocalDateTime.now());
			newPrimaryMappings.add(newMap);
		}
		if (!newPrimaryMappings.isEmpty()) {
			newPrimaryMappings = empPrimaryProjectMappingRepository.saveAll(newPrimaryMappings);
			flag = true;
		}
		return flag;
	}

	private void markSourceClientSideIdMappingInActive(List<EmployeeClientSideIdMapping> oldClientSideIdMappings,
			Long currentUserEmpId) {
		oldClientSideIdMappings.forEach(m -> {
			m.setActive(false);
			m.setUpdatedOn(LocalDateTime.now());
			m.setUpdatedBy(currentUserEmpId);
		});
		employeeClientSideIdMappingRepository.saveAll(oldClientSideIdMappings);
	}

	private void copyAndSaveClientSideIdMapping(Long currentUserEmpId, Long tgtProjectId,
			List<EmployeeClientSideIdMapping> oldClientSideIdMappings) {
		List<EmployeeClientSideIdMapping> newClientSideIdMappings = new ArrayList<>();

		for (EmployeeClientSideIdMapping oldMap : oldClientSideIdMappings) {
			EmployeeClientSideIdMapping newMap = new EmployeeClientSideIdMapping();
			newMap.setClientSideId(oldMap.getClientSideId());
			newMap.setEmpId(oldMap.getEmpId());
			newMap.setProjectId(tgtProjectId);
			newMap.setActive(true);
			newMap.setCreatedBy(currentUserEmpId);
			newMap.setCreatedOn(LocalDateTime.now());
			newClientSideIdMappings.add(newMap);
		}
		if (!newClientSideIdMappings.isEmpty()) {
			newClientSideIdMappings = employeeClientSideIdMappingRepository.saveAll(newClientSideIdMappings);
		}
	}

	private void copyAndSaveActivities(Long currentUserEmpId, Map<Long, Team> oldToNewTeamMap,
			List<Activity> sourceTeamActivities) {
		List<Activity> newActivities = new ArrayList<>();
		for (Activity sourceActivity : sourceTeamActivities) {
			Team newTeam = oldToNewTeamMap.get(sourceActivity.getTeamId());
			if (newTeam != null) {
				Activity newAct = new Activity();
				newAct.setTeamId(newTeam.getTeamId());
				newAct.setActivity(sourceActivity.getActivity());
				newAct.setEta(sourceActivity.getEta());
				newAct.setEmployeeRole(sourceActivity.getEmployeeRole());
				newAct.setDeptIds(sourceActivity.getDeptIds());
				CommonProperties cp = new CommonProperties();
				cp.setCreatedBy(currentUserEmpId);
				cp.setCreatedOn(Timestamp.valueOf(LocalDateTime.now()));
				newAct.setCommonProperty(cp);
				newActivities.add(newAct);
			}
		}
		if (!newActivities.isEmpty()) {
			activitiesRepository.saveAll(newActivities);
		}
	}

	private void sendTeamMigrationCompletedMail(MigrateTeam migrateTeam, Project sourceProject, Project targetProject)
			throws Exception {
		try {
			List<Long> migrationTeamIds = migrateTeam.getMigrationTeamIds();
			Employee updatedByemp = employeeRepository.findByEmpId(migrateTeam.getCurrentUserEmpId());

			Set<String> toRecipients = new HashSet<>();
			toRecipients.add(bdMail);
			toRecipients.add(adminMail);
			toRecipients.add(rmgMail);
			toRecipients.add(financeMail);

			List<String> projManagerOverheadHODMails = projectRepository
					.findProjectManagerAndProjectoverheadEmails(targetProject.getProjectId());
			Set<String> ccRecipients = projManagerOverheadHODMails.stream().filter(Objects::nonNull)
					.map(String::trim).filter(s -> !s.isEmpty())
					.collect(Collectors.toCollection(LinkedHashSet::new));
			ccRecipients.removeAll(toRecipients);

			List<Team> teams = teamRepository.findAllById(migrationTeamIds);
			List<String> teamNames = teams.stream().map(Team::getTeamName).filter(Objects::nonNull)
					.collect(Collectors.toList());

			String currentDate = LocalDate.now().toString();
			String teamNamesStr = String.join(", ", teamNames);
			String subject = "Team Migration from " + sourceProject.getProjectName() + " to "
					+ targetProject.getProjectName();

			StringBuilder body = new StringBuilder();
			body.append("The ").append(teamNames.size() == 1 ? "team" : "teams")
					.append("<b>").append(teamNamesStr).append(teamNames.size() == 1 ? "has" : "have")
					.append(" been migrated from <b>").append(sourceProject.getProjectName())
					.append("</b> to <b>").append(targetProject.getProjectName()).append("</b> by <b>")
					.append((updatedByemp != null ? updatedByemp.getName() : "System"))
					.append("</b> on ").append(currentDate).append(".");

			mailService.sendMailWithCC(String.join(",", toRecipients), String.join(",", ccRecipients), subject,
					body.toString());
		} catch (Exception e) {
			log.error("Error in sendTeamMigrationCompletedMail : ", e);
		}
	}

	@Transactional(readOnly = true)
	public ServiceResponse getTeamDetailsByTeamIdsAndProjectId(PoDetailsDto poDetailsDto) {
		ServiceResponse response = new ServiceResponse();
		try {
			if (poDetailsDto == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Request cannot be null!!");
				return response;
			}
			if (poDetailsDto.getSelectedTeamIds() == null || poDetailsDto.getSelectedTeamIds().isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Selected Team Ids cannot be null!!");
				return response;
			}
			if (poDetailsDto.getProjectId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project Id cannot be null!!");
				return response;
			}
			
			Project existingProject = projectRepository.findByProjectId(poDetailsDto.getProjectId());
			if (existingProject == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not found!!");
				return response;
			}

			List<RmgTeamMemberDto> teamMemberDetailsList = teamRepository
					.getAllTeamMemberDetailsDtoByProjectIdAndTeamIdIn(poDetailsDto.getSelectedTeamIds(),
							existingProject.getProjectId().longValue(), poDetailsDto.isActiveEtmFlag());
			if (teamMemberDetailsList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(new ArrayList<RmgTeamMemberDto>());
				return response;
			}

			List<Long> empIds = teamMemberDetailsList.stream().map(RmgTeamMemberDto::getEmpId)
					.collect(Collectors.toList());

			Map<Long, List<EmployeeOtherActiveProject>> empIdAndOtherProjectIdsMap = getEmployeeOtherActiveProjectIdMap(
					empIds, poDetailsDto.getProjectId());

			Map<Long, EmployeeInformationDTO> empIdInfoMap = getEmployeeInformationMap(empIds);

			for (RmgTeamMemberDto obj : teamMemberDetailsList) {

				EmployeeInformationDTO dto = empIdInfoMap.getOrDefault(obj.getEmpId(), null);
				if (dto != null) {
					obj.setEmpId(dto.getEmpId());
					obj.setEmployementId(dto.getEmploymentId());
					obj.setMemberDepartment(dto.getDeptName());
					obj.setJobRoleName(dto.getJobRole());
					obj.setBillableType(dto.getBillableType());
					obj.setPrevExp(dto.getPreviousExperience());
					obj.setCurrentExp(dto.getCurrentExperience());
					obj.setTotalExp(dto.getTotalExperience());
					obj.setEmploymentStatus(dto.getEmploymentStatus());
				}

				obj.setOtherActiveProjects(empIdAndOtherProjectIdsMap.getOrDefault(dto.getEmpId(), List.of()));
				if (obj.getOtherActiveProjects() != null && !obj.getOtherActiveProjects().isEmpty()) {
					List<Integer> projectIds = obj.getOtherActiveProjects().stream()
							.map(e -> e.getProjectId())
							.collect(Collectors.toList());
					obj.setOtherActiveProjectIds(projectIds);
				} else {
					obj.setOtherActiveProjectIds(List.of());
				}
				obj.setDisplayRequirement(getDisplayRequirement(obj));
			}
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(teamMemberDetailsList);
		} catch (Exception e) {
			log.error("Error in getTeamDetailsByTeamIdsAndProjectId : ", e);
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong!!");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse deleteSelectedTeams(PoDetailsDto poDetailsDto) {
		ServiceResponse response = new ServiceResponse();
		try {
			if (poDetailsDto == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Request cannot be null!!");
				return response;
			}
			if (poDetailsDto.getTeamList() == null || poDetailsDto.getTeamList().isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Team Details List cannot be null!!");
				return response;
			}
			if (poDetailsDto.getProjectId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project Id cannot be null!!");
				return response;
			}
			
			Project existingProject = projectRepository.findByProjectId(poDetailsDto.getProjectId());
			if (existingProject == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not found!!");
				return response;
			}
			
			List<RmgTeamDto> teamDtoList = poDetailsDto.getTeamList();
			Long currentUserEmpId = teamDtoList.stream().map(RmgTeamDto::getTeamId).filter(Objects::nonNull).findFirst().orElse(null);
			if (currentUserEmpId == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Emp Id cannot be null!!");
				return response;
			}
			
			List<Long> selectedTeamIds = teamDtoList.stream().map(RmgTeamDto::getTeamId).collect(Collectors.toList());
			List<Team> teamList = teamRepository.findActiveTeamsByTeamIds(selectedTeamIds);
			if (teamList == null || teamList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Selected Team Details not found!!");
				return response;
			}

			List<String> ableToInactiveTeamNames = new ArrayList<>();
			List<String> unableToInactiveTeamNames = new ArrayList<>();
			for (RmgTeamDto teamDto : teamDtoList) {
				Team team = teamList.stream().filter(t -> Objects.equals(t.getTeamId(), teamDto.getTeamId()))
						.findFirst().orElse(null);
				if (team != null) {
					team.setIsActive("N");
					team.setUpdatedBy(currentUserEmpId);
					team.setUpdatedOn(LocalDateTime.now());

					List<EmployeeTeamMap> employeeTeamMappings = employeeTeamMapRepository
							.findByTeamIdAndActive(teamDto.getTeamId());
					employeeTeamMappings.forEach(empTeamMap -> {
						empTeamMap.setRescRemovedBy(currentUserEmpId);
						empTeamMap.setUpdatedBy(currentUserEmpId);
						empTeamMap.setUpdatedOn(LocalDateTime.now());
						empTeamMap.setEndDate(teamDto.getEndDate());
						if (teamDto.getEndDate() == null) {
							empTeamMap.setActive(0L);
							empTeamMap.setEndDate(LocalDateTime.now());
						}
					});
					employeeTeamMapRepository.saveAll(employeeTeamMappings);
					teamRepository.save(team);
					ableToInactiveTeamNames.add(team.getTeamName());
				} else {
					unableToInactiveTeamNames.add(teamDto.getTeamName());
				}

				if (unableToInactiveTeamNames != null && !unableToInactiveTeamNames.isEmpty()) {
					String teamNames = unableToInactiveTeamNames.stream().map(String::valueOf)
							.collect(Collectors.joining(", "));
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Unable to set the following Team and its Resources to inactive : " + teamNames);
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Team and Its Resources are set to inactive successfully!!");
				}
			}

			if (ableToInactiveTeamNames != null && !ableToInactiveTeamNames.isEmpty()) {
				sendTeamDeletedMail(poDetailsDto.getProjectId(), currentUserEmpId, ableToInactiveTeamNames);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error in deleteSelectedTeams : ", e);
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse(e.getMessage());
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	private void sendTeamDeletedMail(Integer projectId, Long currentUserEmpId, List<String> deletedTeamNames) {
		try {
			Project project = projectRepository.findByProjectId(projectId);
			Employee removedByEmp = employeeRepository.findByEmpId(currentUserEmpId);
			List<String> managerOverheadEmails = projectRepository
					.findProjectManagerAndProjectoverheadEmails(project.getProjectId());

			Set<String> toRecipients = new HashSet<>();
			toRecipients.add(bdMail);
			toRecipients.add(adminMail);
			toRecipients.add(rmgMail);
			toRecipients.add(financeMail);

			Set<String> ccRecipients = managerOverheadEmails.stream().filter(Objects::nonNull).map(String::trim)
					.filter(s -> !s.isEmpty()).collect(Collectors.toCollection(LinkedHashSet::new));
			ccRecipients.removeAll(toRecipients);

			StringBuilder deletedTeamsList = new StringBuilder();
			deletedTeamNames.forEach(t -> deletedTeamsList.append("<br>").append(t));

			String removedByName = (removedByEmp != null) ? removedByEmp.getName() : "System";

			mailService.sendMailWithCC(String.join(",", toRecipients), String.join(",", ccRecipients),
					"Regarding Team Deletion",
					"Dear All, <br><br>" + "The following teams have been deleted by <b>" + removedByName + "</b>, "
							+ "and the resources have been removed from these teams: " + deletedTeamsList.toString()
							+ "<br><br><b>Under Project:</b> " + project.getProjectName()
							+ "<br><br>Sincerely,<br>Team Ishine - ApMoSys Technologies");
		} catch (Exception e) {
			log.error("Error in sendTeamDeletedMail : ", e);
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse addOrUpdateTeamDetails(PoDetailsDto poDetailsDto) {
		ServiceResponse response = new ServiceResponse();
		try {
			if (poDetailsDto == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Request cannot be null!!");
				return response;
			}
			if (poDetailsDto.getProjectId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project Id cannot be null!!");
				return response;
			}
			if (poDetailsDto.getUpdatedBy() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Emp Id cannot be null!!");
				return response;
			}
			if (poDetailsDto.getTeamList() == null || poDetailsDto.getTeamList().isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Team List cannot be null!!");
				return response;
			}

			Project existingProject = projectRepository.findByProjectId(poDetailsDto.getProjectId());
			if (existingProject == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not found!!");
				return response;
			}
			
			addOrUpdateTeams(existingProject, poDetailsDto);

			response.setServiceResponse(poDetailsDto.isIsupdate() ? "Team(s) Details updated successfully!!"
					: "New Team(s) Details Added successfully!!");
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		} catch (Exception e) {
			log.error("Error in addOrUpdateTeamDetails : ", e);
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public void addOrUpdateTeams(Project project, PoDetailsDto poDetailsDto) {
		List<Team> updatedTeamList = new ArrayList<>();
		List<Team> updatedTeamListDB = new ArrayList<>();

		List<Team> allTeamList = teamRepository.findByProjectId(project.getProjectId());
		
		Map<Long, Team> allTeamIdMap = allTeamList.stream()
				.collect(Collectors.toMap(Team::getTeamId, Function.identity(), (existing, replace) -> replace));
		
		Map<String, Team> allTeamNameMap = allTeamList.stream()
				.collect(Collectors.toMap(Team::getTeamName, Function.identity(), (existing, replace) -> replace));

		for (RmgTeamDto teamObj : poDetailsDto.getTeamList()) {
			updatedTeamList.add(getTeamObject(project, allTeamIdMap, allTeamNameMap, teamObj, poDetailsDto.getUpdatedBy()));
		}

		if (!updatedTeamList.isEmpty()) {
			updatedTeamListDB = teamRepository.saveAll(updatedTeamList);
		}

		for (Team team : updatedTeamListDB) {
			RmgTeamDto teamObj = poDetailsDto.getTeamList().stream()
					.filter(t -> t.getTeamName().equals(team.getTeamName()))
					.findFirst().orElse(null);
			if (teamObj != null) {
				createActivityForTeam(teamObj, poDetailsDto.getUpdatedBy(), team.getTeamId());
			}
		}
	}

	private Team getTeamObject(Project project, Map<Long, Team> allTeamIdMap, Map<String, Team> allTeamNameMap,
			RmgTeamDto teamObj, Long currentUserEmpId) {
		Team team = Optional
				.ofNullable(allTeamIdMap.get(teamObj.getTeamId()))
				.orElseGet(() -> Optional
						.ofNullable(allTeamNameMap.get(teamObj.getTeamName()))
						.orElse(null));

		if (team == null) {
			team = new Team();
			team.setCreatedOn(new Timestamp(System.currentTimeMillis())); 
			team.setCreatedBy(currentUserEmpId);
		} else {
			team.setUpdatedBy(currentUserEmpId);
			team.setUpdatedOn(LocalDateTime.now());
		}
		String departmentIdsAsString = teamObj.getDeptIds().stream().map(String::valueOf)
				.collect(Collectors.joining(","));
		team.setIsActive("Y");
		team.setProjectId(project.getProjectId());
		team.setTeamName(teamObj.getTeamName());
		team.setDeptIds(departmentIdsAsString);
		team.setSpocId(teamObj.getSpocId());
		return team;
	}

	private void createActivityForTeam(RmgTeamDto teamObj, Long currentUserEmpId, Long teamId) {
		List<Activity> activityList = new ArrayList<>();
		for (Long departmentId : teamObj.getDeptIds()) {
			List<ActivityTemplate> activityTemplate = activityTemplateRepository.getByDeptId(departmentId);
			if (!activityTemplate.isEmpty()) {
				for (ActivityTemplate activityObject : activityTemplate) {
					Activity newActivity = new Activity();
					newActivity.setActivity(activityObject.getTemplateActivity());
					newActivity.setTeamId(teamId);
					newActivity.setEmployeeRole(activityObject.getEmployeeRole());
					newActivity.setDeptIds(activityObject.getDeptId().toString());
					newActivity.getCommonProperty().setCreatedBy(currentUserEmpId);
					activityList.add(newActivity);
				}
			}
		}
		if (!activityList.isEmpty()) {
			activitiesRepository.saveAll(activityList);
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse addOrUpdateTeamMembers(RmgTeamDto rmgTeamDto) {
		ServiceResponse response = new ServiceResponse();
		try {
			if (rmgTeamDto == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Request cannot be null!!");
				return response;
			}
			if (rmgTeamDto.getProjectId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project Id cannot be null!!");
				return response;
			}
			if (rmgTeamDto.getUpdatedBy() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Emp Id cannot be null!!");
				return response;
			}
			if (rmgTeamDto.getRmgTeamMemberList() == null
					|| rmgTeamDto.getRmgTeamMemberList().isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Team Member List cannot be null!!");
				return response;
			}

			Project project = projectRepository.findByProjectId(rmgTeamDto.getProjectId());
			if (project == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not found!!");
				return response;
			}

			Team team = teamRepository.findByTeamId(rmgTeamDto.getTeamId());
			if (team == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Team not found!!");
				return response;
			}

			handleAddOrUpdateTeamMembers(rmgTeamDto, project, team);

			response.setServiceResponse(rmgTeamDto.isIsupdate()
					? "Team Member(s) Details updated successfully!!"
					: "New Team Member(s) Details Added successfully. Please approve Project to allow newly added employees to fill timesheet!!");
			response.setServiceResponse1(getDeptIdListFromString(team.getDeptIds()));
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		} catch (Exception e) {
			log.error("Error in addOrUpdateTeamMembers : ", e);
			response.setServiceResponse("Something went wrong!!");
			response.setServiceError(e.getMessage());
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		}
		return response;
	}

	private void handleAddOrUpdateTeamMembers(RmgTeamDto dto, Project project, Team team) {
		ServiceResponse response = new ServiceResponse();
		Long currentUserEmpId = dto.getUpdatedBy();

		List<RmgTeamMemberDto> teamMemberDtoList = new ArrayList<>(dto.getRmgTeamMemberList());
		List<EmployeeTeamMap> existingMappedMember = employeeTeamMapRepository.findByTeamId(team.getTeamId());
		List<Long> newEmpIds = new ArrayList<>();

		Map<Long, EmployeeTeamMap> existingMappedMemberMap = existingMappedMember.stream().collect(Collectors
				.toMap(EmployeeTeamMap::getEmployeeTeamMapId, Function.identity(), (existing, replacement) -> replacement));

		List<EmployeeTeamMap> updatedMemberDbResponse = getTeamMembersObj(teamMemberDtoList, existingMappedMemberMap,
				project, team, currentUserEmpId, dto, newEmpIds);
		
		team = updateTeamDepartmentIds(team,updatedMemberDbResponse);
		
		List<Long> defaultProjectEmpIds = teamMemberDtoList.stream()
				.filter(RmgTeamMemberDto :: isDefaultProject)
				.map(RmgTeamMemberDto :: getEmpId).distinct().collect(Collectors.toList());
			
		List<Long> allEmpIds = teamMemberDtoList.stream()
				.map(RmgTeamMemberDto::getEmpId).distinct().collect(Collectors.toList());

		updateEmployeeDefaultProjectIfUpdated(allEmpIds, defaultProjectEmpIds, project, currentUserEmpId);
		
		if (!newEmpIds.isEmpty()) {
			String clientName = dto.getClientName();
			createActivityForEmployeeRole(team.getTeamId(), currentUserEmpId, newEmpIds, teamMemberDtoList);
			sendProjectMappingEmailToEmployee(project.getProjectName(), clientName, newEmpIds);
		}

		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		if (!dto.isIsupdate() && existingMappedMember.isEmpty()) {
			sendTeamCreationEmail(team, updatedMemberDbResponse);
		}
	}

	private List<EmployeeTeamMap> getTeamMembersObj(List<RmgTeamMemberDto> teamMemberDtoList,
			Map<Long, EmployeeTeamMap> existingMappedMemberMap, Project project,
			Team team, Long currentUserEmpId, RmgTeamDto dto, List<Long> newEmpIds) {

		List<EmployeeTeamMap> updatedMemberList = new ArrayList<>();

		boolean updateProjectFlag = false;
		for (RmgTeamMemberDto teamMember : teamMemberDtoList) {

			if (dto.getProjectType().equalsIgnoreCase("TNM")) {
				if (teamMember.getPoId() == null) {
					throw new BadRequestException(
							"Project Type is TNM & PO Id is null,for EmpId: " + teamMember.getEmpId());
				}
				if (teamMember.getRoleId() == null) {
					throw new BadRequestException(
							"Project Type is TNM & Role Id is null,for EmpId: " + teamMember.getEmpId());
				}
				Optional<RoleDetails> roleDetailsOpt = roleDetailsRepository
						.findById(teamMember.getRoleId());
				if (roleDetailsOpt.isEmpty()) {
					throw new BadRequestException("Requirement Role not found,for EmpId: " + teamMember.getEmpId()
							+ " and Role Id: " + teamMember.getRoleId());
				}
			}

			boolean shadowUpdatedFlag = false;
			EmployeeTeamMap shadowFlagUpdatedMember = new EmployeeTeamMap();

			EmployeeTeamMap presentMember = existingMappedMemberMap.getOrDefault(teamMember.getEtmId(), null);
			if (presentMember == null) {
				updateProjectFlag = true;
				presentMember = new EmployeeTeamMap();
				presentMember.setCreatedOn(new Timestamp(System.currentTimeMillis())); 
				presentMember.setStartDate(teamMember.getStartDate() != null ? teamMember.getStartDate() : LocalDateTime.now());
				presentMember.setEndDate(teamMember.getEndDate());
				presentMember.setCreatedBy(currentUserEmpId);
				presentMember.setActive(2L);
				newEmpIds.add(teamMember.getEmpId());
			} else {
				boolean isTeamMemberUpdated = isTeamMemberValueChanged(presentMember, teamMember);
				if(!isTeamMemberUpdated){
					continue;
				}

				// Create a new Entry of Shadow resource
				if (!Objects.equals(teamMember.getIsShadow(), presentMember.getIsShadow())) {
					shadowUpdatedFlag = true;
					presentMember.setActive(0L);
				}
				presentMember.setActive(Objects.equals(presentMember.getActive(), 1L) ? 1L : 2L);
				presentMember.setUpdatedOn(LocalDateTime.now());
				presentMember.setUpdatedBy(currentUserEmpId);
			}

			String employeeRole = teamMember.getEmployeeRoles().stream().map(String::valueOf)
					.collect(Collectors.joining(","));
			Integer isShadow = teamMember.getIsShadow() != null ? teamMember.getIsShadow() : null;

			presentMember.setEmpId(teamMember.getEmpId());
			presentMember.setEmployeeRole(employeeRole);
			presentMember.setTeamId(team.getTeamId());
			presentMember.setIsShadow(isShadow);
			presentMember.setStartDate(teamMember.getStartDate() != null ? teamMember.getStartDate() : null);
			presentMember.setEndDate(teamMember.getEndDate() != null ? teamMember.getEndDate() : null);
			presentMember.setRoleId(teamMember.getRoleId());
			presentMember.setPoId(teamMember.getPoId());
			presentMember.setEmpTeamDepartmentId(teamMember.getEmpTeamDepartmentId());
			updatedMemberList.add(presentMember);

			if(shadowUpdatedFlag){
				shadowFlagUpdatedMember.setStartDate(LocalDateTime.now());
				shadowFlagUpdatedMember.setStartDate(teamMember.getStartDate() != null ? teamMember.getStartDate() : LocalDateTime.now());
				shadowFlagUpdatedMember.setEndDate(teamMember.getEndDate());
				shadowFlagUpdatedMember.setCreatedBy(currentUserEmpId);
				shadowFlagUpdatedMember.setActive(2L);
				shadowFlagUpdatedMember.setEmpId(teamMember.getEmpId());
				shadowFlagUpdatedMember.setEmployeeRole(employeeRole);
				shadowFlagUpdatedMember.setTeamId(team.getTeamId());
				shadowFlagUpdatedMember.setRoleId(teamMember.getRoleId());
				shadowFlagUpdatedMember.setPoId(teamMember.getPoId());
				shadowFlagUpdatedMember.setIsShadow(isShadow);
				shadowFlagUpdatedMember.setEmpTeamDepartmentId(teamMember.getEmpTeamDepartmentId());
				updatedMemberList.add(shadowFlagUpdatedMember);
				newEmpIds.add(teamMember.getEmpId());
			}
		}
		if (!updatedMemberList.isEmpty()) {
			updatedMemberList = employeeTeamMapRepository.saveAll(updatedMemberList);
		}

		if (updateProjectFlag) {
			project.setIsDraftProject("true");
			projectRepository.save(project);
		}
		return updatedMemberList;
	}

	private boolean isTeamMemberValueChanged(EmployeeTeamMap existingMember, RmgTeamMemberDto dto) {
		List<String> dtoEmpRoles = dto.getEmployeeRoles() != null && !dto.getEmployeeRoles().isEmpty() ? 
									 dto.getEmployeeRoles().stream().map(String::trim)
									.map(String::toUpperCase)
									.filter(role -> !role.isEmpty())
									.collect(Collectors.toList())
									: Collections.emptyList();
		List<String> objEmpRoles = parseRoles(existingMember.getEmployeeRole());

		return isRoleChanged(dtoEmpRoles, objEmpRoles) ||
				dateChanged(existingMember.getStartDate(), dto.getStartDate()) ||
				dateChanged(existingMember.getEndDate(), dto.getEndDate()) ||
				!Objects.equals(existingMember.getIsShadow(), dto.getIsShadow()) ||
				!Objects.equals(existingMember.getActive(), dto.getIsMemberActive())
				;
	}

	private List<String> parseRoles(String roles) {
		if (roles == null || roles.isBlank()) {
			return Collections.emptyList();
		}
		return Arrays.stream(roles.split(","))
				.map(String::trim)
				.map(String::toUpperCase)
				.filter(role -> !role.isEmpty())
				.collect(Collectors.toList());
	}

	private boolean isRoleChanged(List<String> dtoEmpRoles, List<String> objEmpRoles) {
    return !new HashSet<>(dtoEmpRoles).equals(new HashSet<>(objEmpRoles));
	}

	private boolean dateChanged(LocalDateTime a, LocalDateTime b) {
		if (a == null && b == null)
			return false;
		if (a == null || b == null)
			return true;
		return !a.toLocalDate().equals(b.toLocalDate());
	}
	
	private Team updateTeamDepartmentIds(Team team, List<EmployeeTeamMap> updatedMemberDbResponse) {
	    if (updatedMemberDbResponse == null || updatedMemberDbResponse.isEmpty()) {
	        return team;
	    }

	    Set<Long> employeeDeptIds = updatedMemberDbResponse.stream()
	            .filter(emp -> emp.getActive() != null && emp.getActive() != 0L)
	            .map(EmployeeTeamMap::getEmpTeamDepartmentId)
	            .filter(Objects::nonNull)
	            .collect(Collectors.toSet());

	    Set<Long> teamDeptIdSet = (team.getDeptIds() != null && !team.getDeptIds().isBlank())
	            ? Arrays.stream(team.getDeptIds().split(","))
	                    .map(String::trim)
	                    .map(Long::parseLong)
	                    .collect(Collectors.toSet())
	            : new HashSet<>();

	    teamDeptIdSet.addAll(employeeDeptIds);
		team.setDeptIds(teamDeptIdSet.stream().map(String::valueOf).collect(Collectors.joining(",")));

	    return teamRepository.save(team);
	}

	private void createActivityForEmployeeRole(Long teamId, Long currentUserEmpId, List<Long> newEmpIds,
			List<RmgTeamMemberDto> teamMemberDtoList) {
		List<EmployeeDetailsForTeamMemberDTO> empInfoList = employeeRepository
				.getEmployeeDetailsAndEtmDeptIdForTeamByTeamId(newEmpIds, teamId);
		if (empInfoList == null || empInfoList.isEmpty()) {
			return;
		}

		Map<Long, EmployeeDetailsForTeamMemberDTO> empIdInfoMap = empInfoList.stream()
				.collect(Collectors.toMap(emp -> emp.getEmpId(), Function.identity(), (existing, replace) -> replace));

		List<Activity> newActivityList = new ArrayList<>();
		for (RmgTeamMemberDto teamMember : teamMemberDtoList) {
			EmployeeDetailsForTeamMemberDTO empInfoObj = empIdInfoMap.getOrDefault(teamMember.getEmpId(), null);
			
			if (empInfoObj == null || empInfoObj.getDeptId() == null) {
				continue;
			}
			
			Long departmentId = empInfoObj.getDeptId();
			for (String role : teamMember.getEmployeeRoles()) {
				role = role.trim();
				List<Activity> existingActivities = activitiesRepository.findByDeptIdsAndEmployeeRoleAndTeamId(
						departmentId.toString(), role, teamId);

				if (existingActivities.isEmpty()) {
					List<ActivityTemplate> activityTemplateList = activityTemplateRepository
							.getByDeptIdAndEmployeeRoleType(departmentId, role);
					if (!activityTemplateList.isEmpty()) {
						for (ActivityTemplate activityTemplate : activityTemplateList) {
							Activity newActivity = new Activity();
							newActivity.setActivity(activityTemplate.getTemplateActivity());
							newActivity.setTeamId(teamId);
							newActivity.setEmployeeRole(activityTemplate.getEmployeeRole());
							newActivity.setDeptIds(activityTemplate.getDeptId().toString());
							newActivity.getCommonProperty().setCreatedBy(currentUserEmpId);
							newActivityList.add(newActivity);
						}
					}
				}
			}
		}
		if (!newActivityList.isEmpty()) {
			activitiesRepository.saveAll(newActivityList);
		}
	}

	public void updateEmployeeDefaultProject(List<Long> empIds, Project project, Long updatedBy) {
		if (empIds == null || empIds.isEmpty()) {
			return;
		}
		List<Long> uniqueEmpIds = empIds.stream().distinct().collect(Collectors.toList());
		LocalDateTime now = LocalDateTime.now();
		Long projectId = project.getProjectId().longValue();

		List<EmpPrimaryProjectMapping> existingMappings = empPrimaryProjectMappingRepository
				.findByEmpIdInAndIsMapped(uniqueEmpIds);
		existingMappings.forEach(emp -> {
			emp.setIsMapped("N");
			emp.setUpdatedBy(updatedBy);
			emp.setUpdatedOn(now);
		});

		Map<Long, Employee> employeeMap = employeeRepository.findByEmpIdIn(uniqueEmpIds).stream()
				.collect(Collectors.toMap(Employee::getEmpId, Function.identity()));

		List<Long> shadowEmpIds = employeeTeamMapRepository.findShadowMembersByEmpIdsAndProjectId(empIds,
				project.getProjectId());

		BillableInfo billableInfo = resolveBillableInfo(project);

		List<EmpPrimaryProjectMapping> mappingsToSave = new ArrayList<>(existingMappings);
		Map<Long, BillableInfo> billableUpdates = new HashMap<>();

		for (Long empId : uniqueEmpIds) {
			mappingsToSave.add(createNewMapping(empId, project, projectId, updatedBy, now));
			BillableInfo finalInfo = shadowEmpIds.contains(empId) ? new BillableInfo("Shadow", "No") : billableInfo;

			Employee emp = employeeMap.get(empId);
			if (emp == null || !Objects.equals(emp.getBillable(), finalInfo.getBillable())
					|| !Objects.equals(emp.getBillableType(), finalInfo.getBillableType())) {
				billableUpdates.put(empId, finalInfo);
			}
		}
		empPrimaryProjectMappingRepository.saveAll(mappingsToSave);

		for (Map.Entry<Long, BillableInfo> entry : billableUpdates.entrySet()) {
			BillableInfo empIdToBillable = entry.getValue();
			employeeRepository.updateBillableFields(entry.getKey(), empIdToBillable.getBillable(),
					empIdToBillable.getBillableType());
		}
	}

	public void updateEmployeeDefaultProjectIfUpdated(List<Long> allEmpIds, List<Long> defaultEmpIds, Project project,
			Long updatedBy) {
		LocalDateTime now = LocalDateTime.now();
		Long projectId = project.getProjectId().longValue();
		Set<Long> defaultEmpIdSet = new HashSet<>(defaultEmpIds);

		List<EmpPrimaryProjectMapping> existingMappings = empPrimaryProjectMappingRepository
				.findByEmpIdInAndIsMappedAndProjectId(allEmpIds, project.getProjectId().longValue());

		Set<Long> existingMappingEmpIdSet = existingMappings.stream().map(EmpPrimaryProjectMapping::getEmpId)
				.collect(Collectors.toSet());

		if (Objects.equals(existingMappingEmpIdSet, defaultEmpIdSet)) {
			return;
		}

		existingMappings.forEach(emp -> {
			if (!defaultEmpIdSet.contains(emp.getEmpId())) {
				emp.setIsMapped("N");
				emp.setUpdatedBy(updatedBy);
				emp.setUpdatedOn(now);
			}
		});

		Map<Long, Employee> employeeMap = employeeRepository.findByEmpIdIn(allEmpIds).stream()
				.collect(Collectors.toMap(Employee::getEmpId, Function.identity()));

		List<Long> shadowEmpIds = employeeTeamMapRepository.findShadowMembersByEmpIdsAndProjectId(allEmpIds,
				project.getProjectId());

		BillableInfo billableInfo = resolveBillableInfo(project);

		Map<Long, BillableInfo> billableUpdates = new HashMap<>();
		List<EmpPrimaryProjectMapping> mappingsToSave = new ArrayList<>(existingMappings);

		for (Long empId : allEmpIds) {
			if (!existingMappingEmpIdSet.contains(empId) && defaultEmpIdSet.contains(empId)) {
				mappingsToSave.add(createNewMapping(empId, project, projectId, updatedBy, now));
			}

			BillableInfo finalInfo = shadowEmpIds.contains(empId) ? new BillableInfo("Shadow", "No") : billableInfo;
			Employee emp = employeeMap.get(empId);
			if (emp == null || !Objects.equals(emp.getBillable(), finalInfo.getBillable())
					|| !Objects.equals(emp.getBillableType(), finalInfo.getBillableType())) {
				billableUpdates.put(empId, finalInfo);
			}
		}
		empPrimaryProjectMappingRepository.saveAll(mappingsToSave);

		for (Map.Entry<Long, BillableInfo> entry : billableUpdates.entrySet()) {
			BillableInfo empIdToBillable = entry.getValue();
			employeeRepository.updateBillableFields(entry.getKey(), empIdToBillable.getBillable(),
					empIdToBillable.getBillableType());
		}
	}

	private BillableInfo resolveBillableInfo(Project project) {
		String YES = "Yes";
		String NO = "No";
		String Y = "Y";
		String N = "N";
		String SHADOW = "Shadow";
		String TNM = "TNM";
		String FIXED_COST = "Fixed Cost";
		String BENCH = "Bench";
		String INTERNAL_RND = "InternalRNDProducts";
		String MONITORING = "Monitoring";

		if (TNM.equalsIgnoreCase(project.getPoProjectType())) {
			return new BillableInfo(TNM, YES);
		}
		if (FIXED_COST.equalsIgnoreCase(project.getPoProjectType())
				|| MONITORING.equalsIgnoreCase(project.getPoProjectType())) {
			return new BillableInfo(FIXED_COST, NO);
		}
		if (BENCH.equalsIgnoreCase(project.getInternalProjectType())) {
			return new BillableInfo(BENCH, NO);
		}
		if (INTERNAL_RND.equalsIgnoreCase(project.getInternalProjectType())) {
			return new BillableInfo(INTERNAL_RND, NO);
		}
		return new BillableInfo(null, null);
	}

	private EmpPrimaryProjectMapping createNewMapping(Long empId, Project project, Long projectId, Long updatedBy, LocalDateTime now) {
		EmpPrimaryProjectMapping mapping = new EmpPrimaryProjectMapping();
		mapping.setEmpId(empId);
		mapping.setPrimaryProjectId(projectId);
		mapping.setPrimaryProjectName(project.getProjectName());
		mapping.setIsMapped("Y");
		mapping.setUpdatedBy(updatedBy);
		mapping.setUpdatedOn(now);
		return mapping;
	}

	private void sendProjectMappingEmailToEmployee(String projectName, String clientName, List<Long> empIds) {
		List<Object[]> empMaildsForProjectMappingList = employeeRepository.findMailIdsForProjectMappingByEmpIds(empIds);
		for (Object[] object : empMaildsForProjectMappingList) {
			String empName = TypeConversionUtil.getSafeString(object[1]);
			String empEmail = TypeConversionUtil.getSafeString(object[2]);
			String managerEmail = TypeConversionUtil.getSafeString(object[3]);
			String hodEmail = TypeConversionUtil.getSafeString(object[4]);

			String ccMail = hodEmail + "," + managerEmail + "," + rmgMail + ","
					+ adminMail;
			try {
				mailService.sendMailWithCC(empEmail, ccMail,
						"Regarding resource mapping to new project",
						"Dear " + empName + "<br>" + "You have been mapped to client name - "
								+ clientName + " under the project "
								+ projectName + "<br><br><br>"
								+ "Sincerely,<br>Team RMG - ApMoSys Technologies");
			} catch (AddressException e) {
				log.error("Error in sendProjectMappingEmailToEmployee : ", e);
			} catch (MessagingException e) {
				log.error("Error in sendProjectMappingEmailToEmployee : ", e);
			}
		}
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
			log.error("Error in sendTeamCreationEmail : ", e);
		}
	}

	private String getAllHodMails(List<EmployeeTeamMap> mappings) {
		return mappings.stream().map(m -> employeeRepository.findHodMail(m.getEmpId())).filter(Objects::nonNull)
				.distinct().collect(Collectors.joining(","));
	} 
	
	private List<Long> getDeptIdListFromString(String deptIds) {
        return Optional.ofNullable(deptIds)
                .filter(s -> !s.isBlank())
                .map(s -> Arrays.stream(s.split(","))
                        .map(String::trim)
                        .map(Long::valueOf)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }
	
	@Transactional(readOnly = true)
	public ServiceResponse getActiveTeamDetailsByProjectId(Integer projectId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getActiveTeamDetailsByProjectId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("ProjectId : " + projectId);
		try {
			if (projectId == null) {
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiResponse("Project Id cannot be null!!");
				response.setServiceResponse("Project Id cannot be null!!");
				return response;
			}

			List<RmgTeamDto> rmgTeamDtoList = teamRepository.getActiveTeamDetailsByProjectId(projectId);
			if (rmgTeamDtoList.isEmpty()) {
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiResponse("No active teams found!!");
				response.setServiceResponse("No active teams found!!");
				return response;
			}
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(rmgTeamDtoList);
			apiLogInfo.setApiResponse("Teams List fetched successfully!!");
		} catch (Exception e) {
			log.error("Error in getActiveTeamDetailsByProjectId : ", e);
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong!!");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateDefaultProjectCompletion(RmgTeamMemberDto teamMember) {
		ServiceResponse response = new ServiceResponse();
		try {
			response = validateUpdateDefaultProjectCompletionRequest(teamMember, response);
			if (response != null && response.getServiceStatus() != null
					&& response.getServiceStatus().equals(ServiceResponse.STATUS_FAIL)) {
				return response;
			}
			response = new ServiceResponse();
			
			Project project = projectRepository.findByProjectId(teamMember.getProjectId());
			if (project == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not found!!");
				return response;
			}

			Team team = teamRepository.findByTeamId(teamMember.getTeamId());
			if (team == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Team not found!!");
				return response;
			}

			Long teamId = team.getTeamId();
			Long currentUserEmpId = teamMember.getUpdatedBy();

			List<Long> defaultProjectEmpIds = new ArrayList<>();
			List<RmgTeamMemberDto> teamMemberDtoList = new ArrayList<>();
			List<EmployeeTeamMap> newTeamMapList = new ArrayList<>();

			String employeeRoleString = teamMember.getEmployeeRoles().stream().map(String::valueOf)
					.collect(Collectors.joining(","));

			for (Long empId : teamMember.getSelectedEmpIds()) {
				EmployeeTeamMap existingMap = employeeTeamMapRepository
						.findByEmpIdAndTeamIdAndActiveStatus(empId, teamId);
				if (existingMap == null) {
					EmployeeTeamMap empTeamMap = new EmployeeTeamMap();
					empTeamMap.setEmpId(empId);
					empTeamMap.setEmployeeRole(employeeRoleString);
					empTeamMap.setTeamId(teamId);
					empTeamMap.setStartDate(LocalDateTime.now());
					empTeamMap.setActive(2L);
					empTeamMap.setIsShadow(teamMember.getIsShadow() != null ? teamMember.getIsShadow() : null);
					empTeamMap.setUpdatedBy(currentUserEmpId);
					empTeamMap.setRoleId(teamMember.getRoleId());
					empTeamMap.setPoId(teamMember.getPoId());
					empTeamMap.setEmpTeamDepartmentId(teamMember.getEmpTeamDepartmentId());
					if (teamMember.isDefaultProject()) {
						defaultProjectEmpIds.add(empId);
					}
					newTeamMapList.add(empTeamMap);
					teamMemberDtoList.add(new RmgTeamMemberDto(empId, teamMember.getEmployeeRoles()));
				}
			}

			List<EmployeeTeamMap> updatedMemberDbResponse = new ArrayList<>();
			if (!newTeamMapList.isEmpty()) {
				updatedMemberDbResponse = employeeTeamMapRepository.saveAll(newTeamMapList);
			}

			if (!defaultProjectEmpIds.isEmpty()) {
				updateEmployeeDefaultProject(defaultProjectEmpIds, project, currentUserEmpId);
			}

			List<Long> newEmpIds = updatedMemberDbResponse.stream().map(e -> e.getEmpId()).distinct()
					.collect(Collectors.toList());

			if (newEmpIds != null && !newEmpIds.isEmpty()) {
				project.setIsDraftProject("true");
				projectRepository.save(project);
				String clientName = teamMember.getClientName();
				createActivityForEmployeeRole(teamId, currentUserEmpId, newEmpIds, teamMemberDtoList);
				sendProjectMappingEmailToEmployee(project.getProjectName(), clientName, newEmpIds);
			}
			response.setServiceResponse("Employee Project Mapping updated successfully!!");
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		} catch (Exception e) {
			log.error("Error in updateDefaultProjectCompletion : ", e);
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
	private ServiceResponse validateUpdateDefaultProjectCompletionRequest(RmgTeamMemberDto teamMember,
			ServiceResponse response) {
		if (teamMember == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Request cannot be null!!");
			return response;
		}
		if (teamMember.getProjectId() == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Project Id cannot be null!!");
			return response;
		}
		if (teamMember.getUpdatedBy() == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Current Emp Id cannot be null!!");
			return response;
		}
		if (teamMember.getSelectedEmpIds() == null || teamMember.getSelectedEmpIds().isEmpty()) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Selected Employee Id(s) cannot be null!!");
			return response;
		}
		if (teamMember.getEmployeeRoles() == null || teamMember.getEmployeeRoles().isEmpty()) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Selected Employee Role cannot be null!!");
			return response;
		}
		return null;
	}
	
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse removeTeamMembersFromProject(RmgTeamDto rmgTeamDto) {
		ServiceResponse response = new ServiceResponse();
		try {
			if (rmgTeamDto == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Request cannot be null!!");
				return response;
			}
			if (rmgTeamDto.getProjectId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project Id cannot be null!!");
				return response;
			}
			if (rmgTeamDto.getUpdatedBy() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Current Emp Id cannot be null!!");
				return response;
			}
			if (rmgTeamDto.getRmgTeamMemberList() == null
					|| rmgTeamDto.getRmgTeamMemberList().isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Team Member List cannot be null!!");
				return response;
			}
			
			Project project = projectRepository.findByProjectId(rmgTeamDto.getProjectId());
			if (project == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not found!!");
				return response;
			}

			Team team = teamRepository.findByTeamId(rmgTeamDto.getTeamId());
			if (team == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Team not found!!");
				return response;
			}

			List<Long> empIds = rmgTeamDto.getRmgTeamMemberList().stream().map(emp -> emp.getEmpId())
					.collect(Collectors.toList());
			List<EmployeeTeamMap> empMappings = employeeTeamMapRepository.findByEmpIdInAndTeamIdAndActiveStatus(empIds,
					team.getTeamId());
			if (empMappings == null || empMappings.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Team Mapping not found!!");
				return response;
			}
			
			Map<Long, EmployeeTeamMap> empTeamMap = empMappings.stream().collect(
					Collectors.toMap(EmployeeTeamMap::getEmpId, Function.identity(), (existing, replace) -> replace));

			String removeTeamMemberMessage = handleRemoveTeamMembers(rmgTeamDto, project, team, empTeamMap);
			if(removeTeamMemberMessage != null && !removeTeamMemberMessage.isBlank()) {
				response.setServiceResponse("Team Member(s) removed Successfully!! \n" + removeTeamMemberMessage);
			}
			response.setServiceResponse("Team Member(s) removed Successfully!!");
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		} catch (Exception e) {
			log.error("Error in removeTeamMembersFromProject : ", e);
			response.setServiceResponse("Something went wrong!!");
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		}
		return response;
	}

	private String handleRemoveTeamMembers(RmgTeamDto rmgTeamDto, Project project, Team team,
			Map<Long, EmployeeTeamMap> empTeamMap) {
		StringBuilder sb = new StringBuilder();
		Long currentUserEmpId = rmgTeamDto.getUpdatedBy();

		for (RmgTeamMemberDto rmgTeamMember : rmgTeamDto.getRmgTeamMemberList()) {
			try {
				EmployeeTeamMap empTeamMapping = empTeamMap.getOrDefault(rmgTeamMember.getEmpId(), null);
				if (empTeamMapping == null) {
					sb.append("Employee Team Mapping not found for : ").append(rmgTeamMember.getEmpId()).append(" \n");
					continue;
				}
				Employee emp = employeeRepository.findByEmpId(rmgTeamMember.getEmpId());
				if (emp == null) {
					sb.append("Employee not found for : ").append(empTeamMapping.getEmpId()).append(" \n");
					continue;
				}
				empTeamMapping.setRescRemovedBy(currentUserEmpId);
				empTeamMapping.setIsCustomDate(rmgTeamDto.isCustomEndDate());
				empTeamMapping.setEndDate(rmgTeamDto.getEndDate());
				
				if (empTeamMapping.getEndDate() != null) {
					if (empTeamMapping.getStartDate().isAfter(empTeamMapping.getEndDate())) {
						throw new IllegalArgumentException("End date cannot be less than Start date: " + empTeamMapping.getStartDate());
					}
					if (!empTeamMapping.getEndDate().toLocalDate().isAfter(LocalDate.now())) {
						empTeamMapping.setActive(0L);
					}
				} else {
					empTeamMapping.setActive(0L);
					empTeamMapping.setEndDate(LocalDateTime.now());
				}

				employeeTeamMapRepository.save(empTeamMapping); // Sending mail to Individual So saving one object at a time.
				sendResourceRemovalMailToRmg(emp.getName(), project.getProjectName(), team.getTeamName());
			} catch (Exception e) {
				log.error("Error in handleRemoveTeamMembers : ", e);
				sb.append("Employee Team Mapping not updated for : ").append(rmgTeamMember.getEmpId())
						.append(". Something went wrong. \n");
				;
			}
		}
		return sb.toString();
	}

	private void sendResourceRemovalMailToRmg(String empName, String projectName, String teamName) {
		try {
			mailService.sendMail(rmgMail, "Regarding Resource removed from Project ",
					"Dear " + empName + "<br>" + "You have been removed from project "
							+ projectName + "under the team - "
							+ teamName + "<br>" + "<br><br>" + "Sincerely," + "<br>"
							+ "Team RMG - ApMoSys Technologies");
		} catch (AddressException e) {
			log.error("Error in sendResourceRemovalMailToRmg : ", e);
		} catch (MessagingException e) {
			log.error("Error in sendResourceRemovalMailToRmg : ", e);
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateMappingToOtherProjectAsDefault(EmployeeOtherActiveProject empOtherActiveProject) {
		ServiceResponse response = new ServiceResponse();
		try {
			if (empOtherActiveProject == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Request cannot be null!!");
				return response;
			}
			if (empOtherActiveProject.getProjectId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project Id cannot be null!!");
				return response;
			}
			if (empOtherActiveProject.getEmpId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Selected Employee Id cannot be null!!");
				return response;
			}

			Project project = projectRepository.findByProjectId(empOtherActiveProject.getProjectId());
			if (project == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not found!!");
				return response;
			}

			List<Long> empIds = new ArrayList<>();
			empIds.add(empOtherActiveProject.getEmpId());
			updateEmployeeDefaultProject(empIds, project, empOtherActiveProject.getUpdatedBy());
			
			response.setServiceResponse("Default Project updated Successfully!!");
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		} catch (Exception e) {
			log.error("Error in updateMappingToOtherProjectAsDefault : ", e);
			response.setServiceError(e.getMessage());
			response.setServiceResponse("Something went wrong!!");
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		}
		return response;
	}

	public void migrateResourcesAfterRenewal(Integer projectId,Long renewedPoId,Long renewedBy ) {
		ProjectPoDetails previousPo =
                projectPoDetailsRepository
                        .findByNextPOAndProjectIdAndActiveTrue(renewedPoId, projectId).orElse(null);
		
		 if (previousPo == null) {
			 return;
	        }
		 
		 Long previousPoId = previousPo.getPoId();
		 
		 List<Long> oldRoles =
	                poRequirementMappingRepository.findRoleIdsByPoId(previousPoId);
		 
		 List<Long> newRoles =
	                poRequirementMappingRepository.findRoleIdsByPoId(renewedPoId);
		 
		 Set<Long> carryForwardRoles = oldRoles.stream()
	                .filter(newRoles::contains)
	                .collect(Collectors.toSet());
		 
		 
		 if (!carryForwardRoles.isEmpty()) {
			 for (Long roleId : carryForwardRoles) {
				 List<EmployeeTeamMap> employees = employeeTeamMapRepository.findActiveEmployeesForRole(previousPoId, roleId);
			 
			 
				 for (EmployeeTeamMap oldRow : employees) {

				       
				        EmployeeTeamMap newRow = new EmployeeTeamMap();
				        BeanUtils.copyProperties(oldRow, newRow, "employeeTeamMapId");

				        newRow.setPoId(renewedPoId);
				        newRow.setEndDate(null);
//				        newRow.setActive(oldRow.getActive()); 
				        newRow.setCreatedBy(renewedBy);
				        newRow.setUpdatedBy(renewedBy);
				        newRow.setUpdatedOn(LocalDateTime.now());
				        newRow.setStartDate(LocalDateTime.now());
				        newRow.setCreatedOn(new Timestamp(System.currentTimeMillis()));
				        
				        
				        employeeTeamMapRepository.save(newRow);


				       
				        oldRow.setActive(0L);
				        oldRow.setEndDate(LocalDateTime.now());
				        oldRow.setUpdatedBy(renewedBy);
				        oldRow.setUpdatedOn(LocalDateTime.now());

				        employeeTeamMapRepository.save(oldRow);
				    }
		 }
		 }
		 
		 
	}
	
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse migrateTeamMembers(MigrateTeam migrateTeam) {
		ServiceResponse response = new ServiceResponse();
		try {
			// Validation
			response = validateMigrateTeamObject(migrateTeam);
			if (response != null && response.getServiceStatus().equals(ServiceResponse.STATUS_FAIL)) {
				return response;
			}
			response = new ServiceResponse();
			if (migrateTeam.getEmpIds() == null || migrateTeam.getEmpIds().isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Team Member Migration Emp Id(s) cannot be null!!");
				return response;
			}
			
			Project sourceProject = projectRepository.findByProjectId(migrateTeam.getSourceProjectId());
			if (sourceProject == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Source Project not found!!");
				return response;
			}
			Project targetProject = projectRepository.findByProjectId(migrateTeam.getTargetProjectId());
			if (targetProject == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Target Project not found!!");
				return response;
			}
			List<Team> activeTeams = teamRepository.findActiveTeamsByTeamIds(migrateTeam.getMigrationTeamIds());
			if (activeTeams == null || activeTeams.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("No Active teams found for this Project");
				return response;
			}
			
			// Copy hasClientSideId from source -> target project
			targetProject.setHasClientSideId(sourceProject.getHasClientSideId());
			projectRepository.save(targetProject);

			// Project-level mappings copy
			copyAndSavePODepartmentMapping(migrateTeam); // PoDepartmentMapping
			copyAndSaveProjectManagerMapping(migrateTeam); // ProjectManagerMapping
			copyAndSaveProjectOverheadMapping(migrateTeam); // ProjectOverheadMapping

			response = copyAndSaveTeamAndEmployeeDetailsForMembersMigration(migrateTeam, activeTeams);
			if (response != null && response.getServiceStatus().equals(ServiceResponse.STATUS_FAIL)) {
				return response;
			}

			response = new ServiceResponse();
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Team Member Migration completed successfully for " + activeTeams.size() + " teams.");

			sendTeamMemberMigrationCompletedMail(migrateTeam, sourceProject, targetProject);
		} catch (Exception e) {
			log.error("Error in getTeamDetailsByTeamIdsAndProjectId : ", e);
			response = new ServiceResponse();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	private ServiceResponse copyAndSaveTeamAndEmployeeDetailsForMembersMigration(MigrateTeam migrateTeam, List<Team> activeSourceTeams) throws Exception {
		ServiceResponse serviceResponse = new ServiceResponse();

		Long srcProjectId = Long.parseLong(migrateTeam.getSourceProjectId().toString());
		Long tgtProjectId = Long.parseLong(migrateTeam.getTargetProjectId().toString());
		Long currentUserEmpId = migrateTeam.getCurrentUserEmpId();
		List<Long> migrationTeamIds = migrateTeam.getMigrationTeamIds();
		List<Long> migrationEmpIds = migrateTeam.getEmpIds();

		List<EmployeeTeamMap> sourceEmployeeTeamMapping = employeeTeamMapRepository
				.activeAndPendingEmployeesByTeamIdsAndEmpIds(migrationTeamIds, migrationEmpIds);
		if (sourceEmployeeTeamMapping == null || sourceEmployeeTeamMapping.isEmpty()) {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("Selected Employee(s) not found in the Team Mapping!!");
				throw new Exception("Selected Employee(s) not found in the Team Mapping!!");
		}

		List<Activity> sourceTeamActivities = activitiesRepository.findByTeamIdIn(migrationTeamIds);

		if (sourceEmployeeTeamMapping != null && !sourceEmployeeTeamMapping.isEmpty()) {
			List<Long> sourceEmpIds = sourceEmployeeTeamMapping.stream().map(EmployeeTeamMap::getEmpId).distinct()
					.collect(Collectors.toList());

			
			List<Team> newTeams = copyAndSaveTeamForMembersMigration(currentUserEmpId, tgtProjectId, activeSourceTeams, migrateTeam);
			if (newTeams == null || newTeams.isEmpty()) {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("Failed to migrate team members to the Selected Project!!");
				throw new Exception("Failed to migrate members to the Selected Project!!");
			}

			// Build oldTeamId -> newTeam entity map
			Map<Long, Team> oldToNewTeamMap = newTeams.stream().filter(t -> t.getOldTeamId() != null)
					.collect(Collectors.toMap(Team::getOldTeamId, Function.identity()));

			// Mark old team mappings inactive
			markSourceTeamsEmployeeMappingInActive(sourceEmployeeTeamMapping, currentUserEmpId);
			copyAndSaveEmployeeTeamMapping(migrateTeam, currentUserEmpId, sourceEmployeeTeamMapping, oldToNewTeamMap);

			if (sourceEmpIds != null && !sourceEmpIds.isEmpty()) {
				boolean primaryMappingsChanged = false;
				List<EmpPrimaryProjectMapping> sourceEmpPrimaryProjectMapping = empPrimaryProjectMappingRepository
						.findByEmpIdInAndPrimaryProjectIdInAndIsMapped(sourceEmpIds, List.of(srcProjectId), "Y");

				if (sourceEmpPrimaryProjectMapping != null && !sourceEmpPrimaryProjectMapping.isEmpty()) {
					markSourceEmpPrimaryProjectMappingInActive(sourceEmpPrimaryProjectMapping, currentUserEmpId);
					primaryMappingsChanged = copyAndSaveEmpPrimaryProjectMapping(currentUserEmpId, tgtProjectId,
							sourceEmpPrimaryProjectMapping);
				}
				// update billable/billableType in employees
				if (primaryMappingsChanged) {
					updateEmployeeBillableType(migrateTeam.getTargetProjectId(), sourceEmpIds);
				}
			}

			// Mark old teams inactive only if no employees are remaining after migration
			markSourceTeamsInActiveForMembersMigration(migrateTeam, activeSourceTeams, currentUserEmpId);

			// Update Employee client-side ID mappings
			List<EmployeeClientSideIdMapping> oldClientSideIdMappings = employeeClientSideIdMappingRepository
					.findByEmpIdInAndProjectIdInAndActive(sourceEmpIds, List.of(srcProjectId), true);

			if (!oldClientSideIdMappings.isEmpty()) {
				markSourceClientSideIdMappingInActive(oldClientSideIdMappings, currentUserEmpId);
				copyAndSaveClientSideIdMapping(currentUserEmpId, tgtProjectId, oldClientSideIdMappings);
			}

			// Create New Activities
			copyAndSaveActivitiesForMembersMigration(currentUserEmpId, oldToNewTeamMap, sourceTeamActivities);
		}
		return null;
	}

	private List<Team> copyAndSaveTeamForMembersMigration(Long currentUserEmpId, Long tgtProjectId, List<Team> activeSourceTeams,
			MigrateTeam migrateTeam) {
		List<Team> newTeams = new ArrayList<>();

		for (Team oldTeam : activeSourceTeams) {
			Team exisingTeam = teamRepository.findByTeamNameAndProjectIdAndIsActive(oldTeam.getTeamName(),
					Integer.parseInt(tgtProjectId.toString()), "Y");
			if (exisingTeam != null) {
				exisingTeam.setOldTeamId(oldTeam.getTeamId());
				newTeams.add(exisingTeam);
				continue;
			}

			Team newTeam = new Team();
			newTeam.setTeamName(oldTeam.getTeamName());
			newTeam.setTeamLeadId(oldTeam.getTeamLeadId());
			newTeam.setProjectId(Integer.parseInt(tgtProjectId.toString()));
			newTeam.setTeamLeadName(oldTeam.getTeamLeadName());
			newTeam.setIsActive("Y");
			newTeam.setDescription(oldTeam.getDescription());
			newTeam.setDeptIds(oldTeam.getDeptIds());
			newTeam.setSpocId(oldTeam.getSpocId());
			newTeam.setCreatedBy(currentUserEmpId);
			newTeam.setCreatedOn(Timestamp.valueOf(LocalDateTime.now()));
			newTeam.setOldTeamId(oldTeam.getTeamId());
			newTeams.add(newTeam);
		}
		return teamRepository.saveAll(newTeams);
	}

	private void markSourceTeamsInActiveForMembersMigration(MigrateTeam migrateTeam, List<Team> activeSourceTeams,
			Long currentUserEmpId) {
		List<Long> teamIds = activeSourceTeams.stream().map(Team::getTeamId).collect(Collectors.toList());
		List<EmployeeTeamMap> activeEtmMappings = employeeTeamMapRepository.findActiveByTeamIds(teamIds);
		Set<Long> currentTeamIdsWithActiveMembers = activeEtmMappings.stream().map(EmployeeTeamMap::getTeamId)
				.collect(Collectors.toSet());

		LocalDateTime now = LocalDateTime.now();
		activeSourceTeams.forEach(t -> {
			if (currentTeamIdsWithActiveMembers != null && !currentTeamIdsWithActiveMembers.isEmpty()
					&& !currentTeamIdsWithActiveMembers.contains(t.getTeamId())) {
				t.setIsActive("N");
				t.setUpdatedBy(currentUserEmpId);
				t.setUpdatedOn(now);
			}
		});
		teamRepository.saveAll(activeSourceTeams);
	}

	private void copyAndSaveActivitiesForMembersMigration(Long currentUserEmpId, Map<Long, Team> oldToNewTeamMap,
			List<Activity> sourceTeamActivities) {
		List<Activity> newActivities = new ArrayList<>();

		if (oldToNewTeamMap == null || oldToNewTeamMap.isEmpty()) {
			return;
		}

		List<Long> newTeamIds = oldToNewTeamMap.values().stream().map(Team::getTeamId).collect(Collectors.toList());
		Map<Long, Set<String>> teamIdAndEmployeeRoleMap = new HashMap<>();

		List<Activity> allNewTeamActivities = activitiesRepository.findByTeamIdIn(newTeamIds);
		if (allNewTeamActivities != null && !allNewTeamActivities.isEmpty()) {
			teamIdAndEmployeeRoleMap = allNewTeamActivities.stream()
					.collect(Collectors.groupingBy(Activity::getTeamId,
							Collectors.mapping(Activity::getEmployeeRole, Collectors.toSet())));
		}

		for (Activity sourceActivity : sourceTeamActivities) {
			Team newTeam = oldToNewTeamMap.getOrDefault(sourceActivity.getTeamId(), null);
			if (newTeam != null) {
				Set<String> existingActivitiesEmployeeRole = teamIdAndEmployeeRoleMap.getOrDefault(newTeam.getTeamId(),
						Set.of());
				if (existingActivitiesEmployeeRole.contains(sourceActivity.getEmployeeRole())) {
					continue;
				}

				Activity newAct = new Activity();
				newAct.setTeamId(newTeam.getTeamId());
				newAct.setActivity(sourceActivity.getActivity());
				newAct.setEta(sourceActivity.getEta());
				newAct.setEmployeeRole(sourceActivity.getEmployeeRole());
				newAct.setDeptIds(sourceActivity.getDeptIds());
				CommonProperties cp = new CommonProperties();
				cp.setCreatedBy(currentUserEmpId);
				cp.setCreatedOn(Timestamp.valueOf(LocalDateTime.now()));
				newAct.setCommonProperty(cp);
				newActivities.add(newAct);
				existingActivitiesEmployeeRole.add(sourceActivity.getEmployeeRole());
			}
		}
		if (!newActivities.isEmpty()) {
			activitiesRepository.saveAll(newActivities);
		}
	}

	private void sendTeamMemberMigrationCompletedMail(MigrateTeam migrateTeam, Project sourceProject,
			Project targetProject)
			throws Exception {
		try {
			List<Long> migrationEmpIds = migrateTeam.getEmpIds();
			Employee updatedByemp = employeeRepository.findByEmpId(migrateTeam.getCurrentUserEmpId());

			Set<String> toRecipients = new HashSet<>();
			toRecipients.add(bdMail);
			toRecipients.add(adminMail);
			toRecipients.add(rmgMail);
			toRecipients.add(financeMail);

			List<String> projManagerOverheadHODMails = projectRepository
					.findProjectManagerAndProjectoverheadEmails(targetProject.getProjectId());

			Set<String> ccRecipients = projManagerOverheadHODMails.stream().filter(Objects::nonNull)
					.map(String::trim).filter(s -> !s.isEmpty())
					.collect(Collectors.toCollection(LinkedHashSet::new));
			ccRecipients.removeAll(toRecipients);

			List<Employee> employees = employeeRepository.findByEmpIdIn(migrationEmpIds);

			List<String> empNames = employees.stream().map(Employee::getName).filter(Objects::nonNull)
					.collect(Collectors.toList());

			String currentDate = LocalDate.now().toString();
			String empNamesStr = String.join(", ", empNames);

			String subject = "Team Member Migration from " + sourceProject.getProjectName() + " to "
					+ targetProject.getProjectName();

			StringBuilder body = new StringBuilder();
			body.append("The ").append(empNames.size() == 1 ? "team member" : "team members")
					.append("<b>").append(empNamesStr).append(empNames.size() == 1 ? "has" : "have")
					.append(" been migrated from <b>").append(sourceProject.getProjectName())
					.append("</b> to <b>").append(targetProject.getProjectName()).append("</b> by <b>")
					.append((updatedByemp != null ? updatedByemp.getName() : "System"))
					.append("</b> on ").append(currentDate).append(".");

			mailService.sendMailWithCC(String.join(",", toRecipients), String.join(",", ccRecipients), subject,
					body.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Transactional(readOnly = true)
	public ServiceResponse validateEmployeeTimesheetFilledToChangeStartDate(RmgTeamMemberDto rmgTeamMemberDto) {
		ServiceResponse response = new ServiceResponse();
		try {
			if (rmgTeamMemberDto == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Request body cannot be null!!");
				return response;
			}
			if (rmgTeamMemberDto.getEmpId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Id cannot be null!!");
				return response;
			}
			if (rmgTeamMemberDto.getProjectId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project Id cannot be null!!");
				return response;
			}
			if (rmgTeamMemberDto.getStartDate() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Start date cannot be null.");
				return response;
			}

			Project project = projectRepository.findByProjectId(rmgTeamMemberDto.getProjectId());
			if (project == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not found!!");
				return response;
			}

			List<Object[]> objectArr = timesheetsRepository.findByEmpIdAndDate(rmgTeamMemberDto.getEmpId(),
					rmgTeamMemberDto.getStartDate());
			if (objectArr.isEmpty()) {
				response.setServiceResponse("No timesheet records found.");
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				return response;
			}
			StringBuilder sb = new StringBuilder();
			LocalDate newStartDate = rmgTeamMemberDto.getStartDate().toLocalDate();

			// if (!objectArr.isEmpty()) {

				for (Object[] obj : objectArr) {
					String projectName = TypeConversionUtil.getSafeString(obj[1]);
					String dateStr = TypeConversionUtil.getSafeString(obj[2]);
					String oldStartDateStr = TypeConversionUtil.getSafeString(obj[3]);
					Integer timesheetCount = TypeConversionUtil.safeParseInt(obj[4]);

					if (dateStr == null || timesheetCount == null || timesheetCount == 0 || oldStartDateStr == null) {
						continue;
					}

					LocalDate projectStartDate = LocalDate.parse(dateStr);
					LocalDate oldStartDate = LocalDate.parse(oldStartDateStr);

					if (isStartDateConflict(newStartDate, projectStartDate, oldStartDate)) {
						sb.append(String.format("Total timesheets submitted for project '%s' is %d.", projectName,
								timesheetCount)).append("\n");
					}
				}
				if (sb.length() > 0) {
					sb.append("The existing timesheet entries of the users need to be rejected.");
					response.setServiceResponse(sb.toString());
				} else {
					response.setServiceResponse("No conflicting timesheet records found.");
				}
			// } else {
			// 	response.setServiceResponse("No timesheet records found.");
			// }

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		} catch (Exception e) {
			log.error("Error in validateEmployeeTimesheetFilledToChangeStartDate : ", e);
			response.setServiceResponse("Something went wrong, unable to validate timesheet filled count at the moment for the updated start date!!");
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	private boolean isStartDateConflict(LocalDate newDate,
                                   LocalDate projectStartDate,
                                   LocalDate oldStartDate) {

    return newDate.isAfter(projectStartDate)
            || newDate.isBefore(oldStartDate);
}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse extendTeamMembersEndDate(RmgTeamDto rmgTeamDto) {
		ServiceResponse response = new ServiceResponse();
		try {
			if (rmgTeamDto == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Request cannot be null!!");
				return response;
			}
			if (rmgTeamDto.getProjectId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project Id cannot be null!!");
				return response;
			}
			if (rmgTeamDto.getUpdatedBy() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Current User Employee Id cannot be null!!");
				return response;
			}
			if (rmgTeamDto.getRmgMemberEndDateList() == null || rmgTeamDto.getRmgMemberEndDateList().isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("RMG Member(s) End Date List cannot be Null or Empty!!");
				return response;
			}
			
			Project project = projectRepository.findByProjectId(rmgTeamDto.getProjectId());
			if (project == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not found!!");
				return response;
			}

			List<Long> etmIds = rmgTeamDto.getRmgMemberEndDateList().stream().map(RmgMemberEndDateDto::getEtmId)
					.filter(Objects::nonNull).collect(Collectors.toList());
			if (etmIds == null || etmIds.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("RMG Member(s) Employee Team Mapping Id(s) cannot be Null or Empty!!");
				return response;
			}
			List<EmployeeTeamMap> employeeTeamMappingList = employeeTeamMapRepository.findByEmployeeTeamMapIdIn(etmIds);
			if (employeeTeamMappingList == null || employeeTeamMappingList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Team Mapping Not found!!");
				return response;
			}
			
			Map<Long, RmgMemberEndDateDto> etmIdAndEtmMap = rmgTeamDto.getRmgMemberEndDateList().stream().collect(Collectors.toMap(
					RmgMemberEndDateDto::getEtmId, Function.identity(), (existing, replace) -> existing));

			for (EmployeeTeamMap employeeTeamMap : employeeTeamMappingList) {
				RmgMemberEndDateDto rmgMemberEndDateDto = etmIdAndEtmMap.getOrDefault(employeeTeamMap.getEmployeeTeamMapId(), null);
				if (rmgMemberEndDateDto != null && rmgMemberEndDateDto.getEndDate() != null) {
					employeeTeamMap.setEndDate(rmgMemberEndDateDto.getEndDate());
					employeeTeamMap.setUpdatedBy(rmgTeamDto.getUpdatedBy());
				}
			}

			employeeTeamMapRepository.saveAll(employeeTeamMappingList);
			response.setServiceResponse("Team Members End Date updated Successfully!!");
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		} catch (Exception e) {
			log.error("Error in extendTeamMembersEndDate : ", e);
			response.setServiceError(e.getMessage());
			response.setServiceResponse("Something went wrong!!");
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		}
		return response;
	}

	public void markTeamMemberAsInactiveAfterEndDate() {
		try {
			List<EmployeeTeamMap> membersActiveAfterEndDateList = employeeTeamMapRepository.findEtmActiveAfterEndDate();
			if (membersActiveAfterEndDateList == null || membersActiveAfterEndDateList.isEmpty()) {
				return;
			}
			
//			membersActiveAfterEndDateList
			
			membersActiveAfterEndDateList.forEach(etm -> {
				etm.setActive(0L);
				etm.setUpdatedOn(LocalDateTime.now());
			});
			employeeTeamMapRepository.saveAll(membersActiveAfterEndDateList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
