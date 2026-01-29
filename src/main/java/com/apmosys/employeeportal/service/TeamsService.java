package com.apmosys.employeeportal.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.Session;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.ActivityDTO;
import com.apmosys.employeeportal.dto.ActivityTemplateDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.EmployeeInformationDTO;
import com.apmosys.employeeportal.dto.EmployeeTeamMapDTO;
import com.apmosys.employeeportal.dto.GetActiveProjectDetailsIfMultipleDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.MigrateTeam;
import com.apmosys.employeeportal.dto.PoDetailsDto;
import com.apmosys.employeeportal.dto.PoTeamAndMemberDetailsDto;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.RmgResourceRequirementDto;
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
import com.apmosys.employeeportal.model.LeaveBalanceLog;
import com.apmosys.employeeportal.model.LeavePolicyMaster;
import com.apmosys.employeeportal.model.LeaveTypeMaster;
import com.apmosys.employeeportal.model.PoDepartmentMapping;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectDepartmentMap;
import com.apmosys.employeeportal.model.ProjectManagerMapping;
import com.apmosys.employeeportal.model.ProjectOverheadMapping;
import com.apmosys.employeeportal.model.Team;
import com.apmosys.employeeportal.model.Timesheet;
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
	private PoRequirementMappingRepository poRequirementMappingRepository;

	@Autowired
	private ProjectManagerMappingRepository projectManagerMappingRepository;

	@Autowired
	private ProjectOverheadMappingRepository projectOverheadMappingRepository;

	@Autowired
	private EmployeeClientSideIdMappingRepository employeeClientSideIdMappingRepository;

	@Autowired
	private EmpPrimaryProjectMappingRepository empPrimaryProjectMappingRepository;


	@Value("${timesheet.check.period}")
	private String timesheetCheckPeriod;
	
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
				List<Object[]> timesheetList = timesheetsRepository.getMyTeamsFilledEodCountByManagerId(firstOfMonth, end, employeedto.getEmpId());
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
	                timesheetsRepository.getMyTeamsFilledEodCountByManagerId(
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
	        timesheetsRepository.getMyTeamsFilledEodCountByManagerId(
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
			List<Object[]> list = employeeRepository.getAllTeamMemberView(employeedto.getEmpId());
			List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();
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
					
					Long empId = object[0] != null ? Long.parseLong(object[0].toString()): null;
					List<Object[]> timesheetFilledByMember = timesheetsRepository.getTimesheetFilledByMember(empId,date);
					
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
					
					List<Timesheet> empTimesheet = timesheetsRepository.
							findTimesheetOnLeaveDate(leaveApplication.getEmpId(),leaveApplication.getFromDate().toString(),leaveApplication.getToDate().toString());

					if (empTimesheet != null) {

						empTimesheet.forEach((timesheet)->{
							timesheetsRepository.deleteById(timesheet.getTimesheetId());
						});
					}
					
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

	public ServiceResponse getAllTeamsByPoId(Long poId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getAllTeamsByPoId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("PoId : " + poId);
		try {
			List<RmgTeamDto> rmgTeamDtoList = teamRepository.getActiveTeamDetailsByPoId(poId);
			if (rmgTeamDtoList.isEmpty()) {
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiResponse("No teams found. Teams list is empty");
				response.setServiceResponse("No teams found. Teams list is empty");
				return response;
			}
			System.out.println(rmgTeamDtoList);
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(rmgTeamDtoList);
			apiLogInfo.setApiResponse("Teams List fetched successfully!!");
		} catch (Exception e) {
			e.printStackTrace();
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

	public ServiceResponse getTeamDetailsByTeamId(Long teamId, Integer projectId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getTeamDetailsByTeamId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("TeamId : " + teamId);
		try {
			List<PoTeamAndMemberDetailsDto> objectList = teamRepository.getAllTeamMemberDetailsDtoByPoId(teamId);
			if (objectList.isEmpty()) {
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiResponse("Team Details Not found!!");
				response.setServiceResponse("Team Details Not found!!");
				return response;
			}

			List<Long> empIds = objectList.stream().map(PoTeamAndMemberDetailsDto::getEmpId)
					.collect(Collectors.toList());

			Map<Long, List<Integer>> empIdAndOtherProjectIdsMap = getEmployeeOtherActiveProjectIdMap(empIds, projectId);

			Map<Long, EmployeeInformationDTO> empIdInfoMap = getEmployeeInformationMap(empIds);

			List<RmgResourceRequirementDto> rmgRequirementList = mapRmgResourceRequirementList(objectList,
					empIdInfoMap, empIdAndOtherProjectIdsMap);

			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(rmgRequirementList);
			apiLogInfo.setApiResponse("Resource Requirement List fetched successfully!!");
		} catch (Exception e) {
			e.printStackTrace();
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
				empIdInfoMap.put(empId, dto);
			}
		}
		return empIdInfoMap;
	}

	Map<Long, List<Integer>> getEmployeeOtherActiveProjectIdMap(List<Long> empIds, Integer projectId) {
		List<GetActiveProjectDetailsIfMultipleDTO> objectList = projectRepository.getActiveProjectsByEmpIdIn(empIds,
				projectId);
		if (objectList == null || objectList.isEmpty()) {
			return new HashMap<>();
		}

		return objectList.stream()
				.collect(Collectors.groupingBy(
						dto -> dto.getEmpId(),
						Collectors.mapping(
								GetActiveProjectDetailsIfMultipleDTO::getProjectId,
								Collectors.toList())));
	}

	private List<RmgResourceRequirementDto> mapRmgResourceRequirementList(
			List<PoTeamAndMemberDetailsDto> objectList, Map<Long, EmployeeInformationDTO> empIdInfoMap,
			Map<Long, List<Integer>> empIdAndOtherProjectIdsMap) {

		List<RmgResourceRequirementDto> rmgRequirementList = objectList.stream()
				.collect(Collectors.mapping(
						obj -> new RmgResourceRequirementDto(
								obj.getPoRequirementMappingId(),
								obj.getPoId(), obj.getRole(), obj.getExperience(),
								obj.getDepartment(), obj.isPrmActive()),
						Collectors.collectingAndThen(
								Collectors.toList(),
								list -> list.stream().distinct().collect(Collectors.toList()))));

		mapRmgTeamMemberDto(rmgRequirementList, objectList, empIdInfoMap, empIdAndOtherProjectIdsMap);
		return rmgRequirementList;
	}

	private void mapRmgTeamMemberDto(List<RmgResourceRequirementDto> rmgRequirementList,
			List<PoTeamAndMemberDetailsDto> objectList, Map<Long, EmployeeInformationDTO> empIdInfoMap,
			Map<Long, List<Integer>> empIdAndOtherProjectIdsMap) {

		List<RmgTeamMemberDto> teamMemberList = objectList.stream()
				.collect(Collectors.collectingAndThen(
						Collectors.mapping(
								obj -> getTeamMemberObj(obj, empIdInfoMap, empIdAndOtherProjectIdsMap),
								Collectors.toList()),
						list -> list.stream().distinct().collect(Collectors.toList())));

		for (RmgResourceRequirementDto rmgResourceRequirementDto : rmgRequirementList) {
			rmgResourceRequirementDto.setRmgTeamMemberList(teamMemberList);
		}
	}

	private RmgTeamMemberDto getTeamMemberObj(PoTeamAndMemberDetailsDto obj,
			Map<Long, EmployeeInformationDTO> empIdInfoMap, Map<Long, List<Integer>> empIdAndOtherProjectIdsMap) {
		List<String> employeeRoles = getEmployeeRolesFromString(obj.getEmployeeRole());
		String employeeRole = String.join(",", employeeRoles);
		RmgTeamMemberDto rmgTeamMember = new RmgTeamMemberDto(obj.getMemberName(), employeeRole, employeeRoles,
				obj.getStartDate(), obj.getEndDate(), obj.getIsShadow(),
				obj.getIsMemberActive(), obj.isDefaultProject());
		EmployeeInformationDTO dto = empIdInfoMap.getOrDefault(obj.getEmpId(), null);
		if (dto != null) {
			rmgTeamMember.setEmpId(dto.getEmpId());
			rmgTeamMember.setEmployementId(dto.getEmploymentId());
			rmgTeamMember.setMemberDepartment(dto.getDeptName());
			rmgTeamMember.setJobRoleName(dto.getJobRole());
			rmgTeamMember.setBillableType(dto.getBillableType());
			rmgTeamMember.setPrevExp(dto.getPreviousExperience());
			rmgTeamMember.setCurrentExp(dto.getCurrentExperience());
			rmgTeamMember.setTotalExp(dto.getTotalExperience());
			rmgTeamMember.setOtherActiveProjectIds(empIdAndOtherProjectIdsMap.getOrDefault(dto.getEmpId(), List.of()));
		}
		return rmgTeamMember;
	}

	private List<String> getEmployeeRolesFromString(String employeeRole) {
		return (employeeRole != null && !employeeRole.trim().equals("")) ? Arrays.asList(employeeRole.split(","))
				: List.of("Employee");
	}

	public ServiceResponse getActiveTeamDetailsByPoId(Long poId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getActiveTeamDetailsByPoId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("PoId : " + poId);
		try {
			List<RmgTeamDto> rmgTeamDtoList = teamRepository.getActiveTeamDetailsByPoId(poId);
			if (rmgTeamDtoList.isEmpty()) {
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setApiResponse("No active teams found!!");
				response.setServiceResponse("No active teams found!!");
				return response;
			}
			System.out.println(rmgTeamDtoList);
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(rmgTeamDtoList);
			apiLogInfo.setApiResponse("Teams List fetched successfully!!");
		} catch (Exception e) {
			e.printStackTrace();
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

			// Copy hasClientSideId from source -> target project
			Project sourceProject = projectRepository.findByProjectId(migrateTeam.getSourceProjectId());
			if (sourceProject == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Source Project cannot be null!!");
				return response;
			}
			Project targetProject = projectRepository.findByProjectId(migrateTeam.getTargetProjectId());
			if (targetProject == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Source Project cannot be null!!");
				return response;
			}
			targetProject.setHasClientSideId(sourceProject.getHasClientSideId());
			projectRepository.save(targetProject);

			// Project-level mappings copy
			copyAndSavePODepartmentMapping(migrateTeam); // PoDepartmentMapping
			copyAndSaveProjectManagerMapping(migrateTeam); // ProjectManagerMapping
			copyAndSaveProjectOverheadMapping(migrateTeam); // ProjectOverheadMapping

			// Fetch active teams (Source)
			List<Team> activeTeams = teamRepository.findActiveTeamsByTeamIds(migrateTeam.getMigrationTeamIds());
			if (activeTeams == null || activeTeams.isEmpty()) {
				response = new ServiceResponse();
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("No Active teams found for this Project");
				return response;
			}

			response = copyAndSaveTeamAndEmployeeDetails(migrateTeam, activeTeams);
			if (response != null && response.getServiceStatus().equals(ServiceResponse.STATUS_FAIL)) {
				return response;
			}

			response = new ServiceResponse();
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Team Migration completed successfully for " + activeTeams.size() + " teams.");

			sendTeamMigrationCompletedMail(migrateTeam, sourceProject, targetProject);
		} catch (Exception e) {
			e.printStackTrace();
			response = new ServiceResponse();
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
					.findActiveTeamNameByTeamIds(migrateTeam.getMigrationTeamIds());
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
				.findPoDeptIdsByProjectId(tgtProjectId);

		for (PoDepartmentMapping s : sourceDeptMappings) {
			if (!targetDeptIds.contains(s.getDeptId())) {
				PoDepartmentMapping nm = new PoDepartmentMapping();
				nm.setPoId(s.getPoId());
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

	private ServiceResponse copyAndSaveTeamAndEmployeeDetails(MigrateTeam migrateTeam, List<Team> activeSourceTeams) {
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
			List<Team> newTeams = copyAndSaveTeam(currentUserEmpId, tgtProjectId, activeSourceTeams);
			if (newTeams == null || newTeams.isEmpty()) {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("Failed to migrate teams to the Selected Project!!");
				return serviceResponse;
			}

			// Build oldTeamId -> newTeam entity map
			Map<Long, Team> oldToNewTeamMap = newTeams.stream().filter(t -> t.getOldTeamId() != null)
					.collect(Collectors.toMap(Team::getOldTeamId, Function.identity()));

			// Mark old team mappings inactive
			markSourceTeamsEmployeeMappingInActive(sourceEmployeeTeamMapping, currentUserEmpId);
			copyAndSaveEmployeeTeamMapping(currentUserEmpId, sourceEmployeeTeamMapping, oldToNewTeamMap);

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

	private List<Team> copyAndSaveTeam(Long currentUserEmpId, Long tgtProjectId, List<Team> activeSourceTeams) {
		List<Team> newTeams = activeSourceTeams.stream().map(oldTeam -> {
			Team newTeam = new Team();
			newTeam.setTeamName(oldTeam.getTeamName());
			newTeam.setTeamLeadId(oldTeam.getTeamLeadId());
			newTeam.setProjectId(Integer.parseInt(tgtProjectId.toString()));
			newTeam.setTeamLeadName(oldTeam.getTeamLeadName());
			newTeam.setIsActive("Y");
			newTeam.setPoTeamId(oldTeam.getPoTeamId());
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

	private List<EmployeeTeamMap> copyAndSaveEmployeeTeamMapping(Long currentUserEmpId,
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
				newMap.setPoRequirementMappingId(oldMap.getPoRequirementMappingId());
				newMap.setIsShadow(oldMap.getIsShadow());
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
			e.printStackTrace();
		}
	}

	public ServiceResponse getTeamDetailsByTeamIdsAndProjectId(PoDetailsDto poDetailsDto) {
		ServiceResponse response = new ServiceResponse();
		try {
			List<PoTeamAndMemberDetailsDto> objectList = teamRepository
					.getAllTeamMemberDetailsDtoByPoIdIn(poDetailsDto.getSelectedTeamIds());
			// if (objectList.isEmpty()) {
			// 	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			// 	response.setServiceResponse("Team Details Not found!!");
			// 	return response;
			// }

			List<Long> empIds = objectList.stream().map(PoTeamAndMemberDetailsDto::getEmpId)
					.collect(Collectors.toList());

			Map<Long, List<Integer>> empIdAndOtherProjectIdsMap = getEmployeeOtherActiveProjectIdMap(empIds,
					poDetailsDto.getProjectId());

			Map<Long, EmployeeInformationDTO> empIdInfoMap = getEmployeeInformationMap(empIds);

			List<RmgResourceRequirementDto> rmgRequirementList = mapRmgResourceRequirementList(objectList,
					empIdInfoMap, empIdAndOtherProjectIdsMap);

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(rmgRequirementList);
		} catch (Exception e) {
			e.printStackTrace();
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
			if (poDetailsDto == null || poDetailsDto.getTeamList() == null || poDetailsDto.getTeamList().isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Team Details List cannot be null!!");
				return response;
			}

			List<RmgTeamDto> teamDtoList = poDetailsDto.getTeamList();
			List<Long> selectedTeamIds = teamDtoList.stream().map(RmgTeamDto::getTeamId).collect(Collectors.toList());
			Long currentUserEmpId = teamDtoList.get(0).getUpdatedBy();

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
					team.setUpdatedBy(teamDto.getUpdatedBy());
					team.setUpdatedOn(LocalDateTime.now());

					List<EmployeeTeamMap> employeeTeamMappings = employeeTeamMapRepository
							.findByTeamIdAndActive(teamDto.getTeamId());
					employeeTeamMappings.forEach(empTeamMap -> {
						empTeamMap.setActive(0L);
						empTeamMap.setRescRemovedBy(teamDto.getUpdatedBy());
						empTeamMap.setUpdatedBy(teamDto.getUpdatedBy());
						empTeamMap.setUpdatedOn(LocalDateTime.now());
						empTeamMap.setEndDate(teamDto.getEndDate());
						if (teamDto.getEndDate() == null) {
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
					response.setServiceResponse(
							"Unable to set the following Team and its Resources to inactive : " + teamNames);
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
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse(e.getMessage());
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
			e.printStackTrace();
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse addOrUpdateTeamDetails(PoDetailsDto poDetailsDto) {
		ServiceResponse response = new ServiceResponse();
		try {
			if (poDetailsDto == null || poDetailsDto.getProjectId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project Id cannot be null!!");
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
			response = addOrUpdateTeams(existingProject, poDetailsDto);
			if (response != null && !response.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS)) {
				return response;
			}
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			if (poDetailsDto.isUpdate()) {
				response.setServiceResponse("Team(s) Details updated successfully!!");
			} else {
				response.setServiceResponse("New Team(s) Details Added successfully!!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse addOrUpdateTeams(Project project, PoDetailsDto poDetailsDto) {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Team> updatedTeamList = new ArrayList<>();
			// replace the below with findActiveTeamsByProjectId for active teams
			List<Team> allTeamList = teamRepository.findByProjectId(project.getProjectId());
			Map<Long, Team> allTeamIdMap = allTeamList.stream()
					.collect(Collectors.toMap(Team::getTeamId, Function.identity(), (existing, replace) -> replace));
			Map<String, Team> allTeamNameMap = allTeamList.stream()
					.collect(Collectors.toMap(Team::getTeamName, Function.identity(), (existing, replace) -> replace));

			for (RmgTeamDto teamObj : poDetailsDto.getTeamList()) {
				updatedTeamList.add(
						getTeamObject(project, allTeamIdMap, allTeamNameMap, teamObj, poDetailsDto.getUpdatedBy()));
			}
			if (!updatedTeamList.isEmpty()) {
				teamRepository.saveAll(updatedTeamList);
			}
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Something Went Wrong.");
		}
		return response;
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
		team.setPoId(teamObj.getPoId());
		return team;
	}

}
