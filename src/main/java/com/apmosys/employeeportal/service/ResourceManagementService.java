package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpServerErrorException.InternalServerError;
import org.springframework.web.client.RestTemplate;

import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.PoProjectSyncDTO;
import com.apmosys.employeeportal.dto.PoTeamDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.ResourceManagementDTO;
import com.apmosys.employeeportal.dto.TeamDTO;
import com.apmosys.employeeportal.dto.TeamMemberDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.model.Activity;
import com.apmosys.employeeportal.model.ActivityTemplate;
import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.ClientLocation;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeTeamMap;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectDepartmentMap;
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
import com.apmosys.employeeportal.repository.TeamRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class ResourceManagementService {
	
	@Autowired
	ProjectService projectService;
	
	@Autowired
	ProjectRepository projectRepository;
	
	@Autowired
	ClientsRepository clientsRepository;
	
	@Autowired
	ClientLocationRepository clientLocationRepository;
	
	@Autowired
	DepartmentRepository departmentRepository;
	
	@Autowired
	ProjectDepartmentMapRepository projectDepartmentMapRepository;
	
	@Autowired
	TeamRepository teamRepository;
	
	@Autowired
	ActivitiesRepository activitiesRepository;
	
	@Autowired
	ActivityTemplateRepository activityTemplateRepository;
	
	@Autowired
	EmployeeTeamMapRepository employeeTeamMapRepository;
	
	@Autowired
	EmployeeRepository employeeRepository;
	
	@Autowired
	StringToDateTimeParser stringToDateTimeParser;
	
	@Autowired
	MailService mailService;
	
	@Autowired
	private HttpServletRequest httpRequest;

	@Autowired
	private LogService logService;
	 
	
	@Value("${rmg.mail}")
	private String rmgMail;
	
	@Value("${poPortal.api.syncProject}")
	private String syncProjectApi;

	public ServiceResponse createDraftProjectInfo(ResourceManagementDTO resourceManagementDTO) {
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("createDraftProjectInfo");
        apiLogInfo.setApiUrl("/api/createDraftProjectInfo");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("ProjectType : " + resourceManagementDTO.getProjectType() + " ,ProjectId :" + resourceManagementDTO.getProjectId()
        + " ,ProjectName :" + resourceManagementDTO.getName() + " ,Department :" + resourceManagementDTO.getDeptName() + " ,State:" + 
        resourceManagementDTO.getClientState());

		try {
			Project projObj = null;
			if(resourceManagementDTO.getProjectType().equals("Internal")) {
				projObj = projectRepository.findByProjectId(resourceManagementDTO.getProjectId());
			}else {
				projObj = projectRepository.findByPoProjectId(resourceManagementDTO.getId());				
			}
			
			Project projectObj = projObj;
			Employee employeeObj = employeeRepository.findByEmpId(resourceManagementDTO.getCreatedBy());
			
			List<Long> allTeam = new ArrayList<>();
			if(projectObj != null) {
				
				//Find ProjectManager empId
				Long employeementID = Long.parseLong(resourceManagementDTO.getProjectManager().split("-")[1]);
				Long projManagerId = null;
				Employee employee = employeeRepository.findByEmployeementId(employeementID);
				if(employee != null) {
					projManagerId = employee.getEmpId();
				}
				
				if(resourceManagementDTO.getIsHOD().equals("true")) {
					projectObj.setIsDraftProject("false");
				}else {
                    projectObj.setIsDraftProject("true");
				}
				
				projectObj.setProjectName(resourceManagementDTO.getName());
				projectObj.setProjectManagerId(projManagerId);
				Project projectDbResponse = projectRepository.save(projectObj);
				
				resourceManagementDTO.getTeamList().forEach((teamObj) -> {
					//Check if team present
					Team teamPresent;
					if(teamObj.getTeamId() != null) {
						teamPresent = teamRepository.findByTeamIdAndProjectId(teamObj.getTeamId(), projectObj.getProjectId());
					}else {
						teamPresent = teamRepository.findByTeamNameAndProjectId(teamObj.getTeamName(), projectObj.getProjectId());						
					}
					
					//DeptIds
					StringBuilder deptList = new StringBuilder("");
					for(String department: teamObj.getDepartmentList()) {
							deptList.append(department).append(",");
					}
					
					//teamLead
					Long teamLeadId = null;
					String teamLeadName = null;
					for(TeamMemberDTO teamMember: teamObj.getTeamMemberList()) {
						if((teamMember.getIsTeamLead() != null) && (teamMember.getIsTeamLead().equals("true"))) {
							teamLeadId = teamMember.getEmpId();
							teamLeadName = teamMember.getName();
						}
					}
					
					if(teamPresent != null) {
						allTeam.add(teamPresent.getTeamId());
						
						//Update team
						teamPresent.setIsActive("Y");
						teamPresent.setProjectId(projectObj.getProjectId());
						teamPresent.setTeamLeadId(teamLeadId);
						teamPresent.setTeamName(teamObj.getTeamName());
						teamPresent.setTeamLeadName(teamLeadName);
						teamPresent.setDeptIds(deptList.toString());
						teamPresent.getCommonProperty().setUpdatedBy(resourceManagementDTO.getCreatedBy());
						
						Team teamDbResponse = teamRepository.save(teamPresent);
						
						if(teamDbResponse != null) {
							List<EmployeeTeamMap> alreadyMappedMember = employeeTeamMapRepository.findByTeamId(teamDbResponse.getTeamId());
							List<TeamMemberDTO> newTeamMember = teamObj.getTeamMemberList();
							List<Long> memberToBeRemoved = new ArrayList<Long>();
							
							//Update teamMember mapping
							List<EmployeeTeamMap> updateMemberList = new ArrayList<EmployeeTeamMap>();
							for(EmployeeTeamMap presentMember: alreadyMappedMember) {
								for(TeamMemberDTO newMember: newTeamMember) {
									//Update teamMember mapping
									if(presentMember.getEmpId().equals(newMember.getEmpId())) {
										EmployeeTeamMap updateMember = employeeTeamMapRepository
												.findByEmpIdAndTeamIdAndActive(presentMember.getEmpId(),teamDbResponse.getTeamId(),1l);
										
										if(updateMember != null) {
											StringBuilder employeeRole = new StringBuilder("");
											for(String empRole: newMember.getEmployeeRole()) {
												employeeRole.append(empRole).append(",");
											}
											
											updateMember.setEmpId(newMember.getEmpId());
											updateMember.setActive(1l);
											updateMember.setEmployeeRole(employeeRole.toString());
											updateMember.setTeamId(teamDbResponse.getTeamId());
											
											updateMemberList.add(updateMember);
										}
									}
								}
							}
							List<EmployeeTeamMap> updateMemberDbResponse = employeeTeamMapRepository.saveAll(updateMemberList);
							
							//Add teamMember mapping
							newTeamMember.forEach((newMember) -> {
								List<EmployeeTeamMap> presentMember = employeeTeamMapRepository
										.findFirstByEmpIdAndTeamIdAndActive(newMember.getEmpId(), teamDbResponse.getTeamId(), 1l);
							
								List<EmployeeTeamMap> mapList = new ArrayList<EmployeeTeamMap>();
								EmployeeTeamMap empTeamMap = new EmployeeTeamMap();
								
								if(presentMember.isEmpty()) {
									//TeamLead
									if((newMember.getIsTeamLead() != null) && (newMember.getIsTeamLead().equals("true"))) {
										empTeamMap.setEmpId(newMember.getEmpId());
										empTeamMap.setActive(1l);
										empTeamMap.setEmployeeRole("TeamLead");
										empTeamMap.setTeamId(teamDbResponse.getTeamId());
										mapList.add(empTeamMap);
									}else {
										StringBuilder employeeRole = new StringBuilder("");
										for(String empRole: newMember.getEmployeeRole()) {
											employeeRole.append(empRole).append(",");
										}
										
										empTeamMap.setEmpId(newMember.getEmpId());
										empTeamMap.setActive(1l);
										empTeamMap.setEmployeeRole(employeeRole.toString());
										empTeamMap.setTeamId(teamDbResponse.getTeamId());
										mapList.add(empTeamMap);									
									}
									List<EmployeeTeamMap> teamMapDbResponse = employeeTeamMapRepository.saveAll(mapList);
								}
							});
							
							//InActivate team member
							List<EmployeeTeamMap> alreadyExistMember = new ArrayList<>();
							if(!newTeamMember.isEmpty()) {
								for(TeamMemberDTO obj: newTeamMember) {
									memberToBeRemoved.add(obj.getEmpId());
								}
								alreadyExistMember = employeeTeamMapRepository.
										findByEmpIdNotInAndTeamId(memberToBeRemoved, teamDbResponse.getTeamId());
							}else {
								alreadyExistMember = employeeTeamMapRepository.
										findByTeamId(teamDbResponse.getTeamId());
							}
							
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
							response.setServiceResponse("Team updated successfully.");
                            apiLogInfo.setApiResponse("Team Updated!");
                            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
						}
					}else {
						//Add Team
						Team newTeamObj = new Team();
						newTeamObj.setIsActive("Y");
						newTeamObj.setProjectId(projectObj.getProjectId());
						newTeamObj.setTeamLeadId(teamLeadId);
						newTeamObj.setTeamName(teamObj.getTeamName());
						newTeamObj.setTeamLeadName(teamLeadName);
						newTeamObj.setDeptIds(deptList.toString());
						newTeamObj.getCommonProperty().setCreatedBy(resourceManagementDTO.getCreatedBy());
						Team teamDbResponse = teamRepository.save(newTeamObj);
						
						if(teamDbResponse != null) {
							
							allTeam.add(teamDbResponse.getTeamId());
							
							List<EmployeeTeamMap> mapList = new ArrayList<EmployeeTeamMap>();
							// Add team member in team
							
							for(TeamMemberDTO teamMember: teamObj.getTeamMemberList()) {
								EmployeeTeamMap newEmpTeamMap = new EmployeeTeamMap();
								
								//TeamLead
								if((teamMember.getIsTeamLead() != null) && (teamMember.getIsTeamLead().equals("true"))) {
									newEmpTeamMap.setEmpId(teamMember.getEmpId());
									newEmpTeamMap.setActive(1l);
									newEmpTeamMap.setEmployeeRole("TeamLead");
									newEmpTeamMap.setTeamId(teamDbResponse.getTeamId());
									mapList.add(newEmpTeamMap);
								}else {
									StringBuilder employeeRole = new StringBuilder("");
									for(String empRole: teamMember.getEmployeeRole()) {
										employeeRole.append(empRole).append(",");
									}
									
									newEmpTeamMap.setEmpId(teamMember.getEmpId());
									newEmpTeamMap.setActive(1l);
									newEmpTeamMap.setEmployeeRole(employeeRole.toString());
									newEmpTeamMap.setTeamId(teamDbResponse.getTeamId());
									mapList.add(newEmpTeamMap);									
								}
								
							}
							List<EmployeeTeamMap> teamMemberDbResponse = employeeTeamMapRepository.saveAll(mapList);
							
							// Add default activity
							Activity newActivityCreated = null;
							if(!teamMemberDbResponse.isEmpty()) {
								for(String department: teamObj.getDepartmentList()) {
										List<ActivityTemplate> activityTemplate = activityTemplateRepository.getByDeptId(Long.parseLong(department));
										if(!activityTemplate.isEmpty()) {
											
											for(ActivityTemplate activityObject: activityTemplate) {
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
							if(newActivityCreated != null) {
								
								response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
								response.setServiceResponse("Team created successfully.");
                                apiLogInfo.setApiResponse("Team Created! ");
                                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
								
							}else {
								response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
								response.setServiceResponse("Team created successfully, but default activities are not mapped");
                                apiLogInfo.setApiResponse("Team created successfully, but default activities are not mapped");
                                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
							}
						}else {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Unable to create new team.");
							 apiLogInfo.setApiResponse("Unable to Create new team");
                             apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
							
						}
					}
				});
				
				//InActivate Team
				
				List<Team> alreadyExistTeam = teamRepository.findByTeamIdNotInAndProjectId(allTeam, projectObj.getProjectId());
				if(!alreadyExistTeam.isEmpty()) {
					List<Team> teamToBeRemoved = new ArrayList<>();
					
					alreadyExistTeam.forEach((team) -> {
						team.setIsActive("N");
						team.getCommonProperty().setUpdatedBy(resourceManagementDTO.getCreatedBy());
						teamToBeRemoved.add(team);
					});
					List<Team> teamToBeRemoveResponse = teamRepository.saveAll(teamToBeRemoved);
				}
				
				//Send mail to RMG: if HOD has updated project/Team
				if(resourceManagementDTO.getIsHOD().equals("true") && !resourceManagementDTO.getProjectType().equals("Internal")) {
					try {
						mailService.sendMailWithCC(rmgMail, employeeObj.getEmail(),
								"Regarding Resource managment",
								"Dear RMG Team ,"+"<br>"
								+"<br>"
							    + employeeObj.getName() +" has updated the project : " + resourceManagementDTO.getName());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				// Send Project/Team detail JSON to PoPotal
				
				if(!resourceManagementDTO.getProjectType().equals("Internal")) {
					ServiceResponse poPortalResponse = sendProjectInfoToPoPortal(resourceManagementDTO);
					
					if(poPortalResponse.getServiceStatus().equals("Success")) {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Project updated successfully");
						 apiLogInfo.setApiResponse("Project updated successfully");
                         apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Project & Team created successfully,but unable to sync with PoPortal : "+poPortalResponse.getServiceResponse());
						 apiLogInfo.setApiResponse("Project & Team created successfully,but unable to sync with PoPortal");
                         apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					}	
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Project updated successfully");
					apiLogInfo.setApiResponse("Project updated successfully");
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}
				
			}else {
				Integer clientId = null;
				Optional<Client> clientObj = clientsRepository.findByClientName(resourceManagementDTO.getClientName());
				if(!clientObj.isEmpty()) {
					Client clientPresent = clientObj.get();
					clientId = clientPresent.getClientId();
				}else {
					// Add Client & Client Location
					
					Client newClient = new Client();
					newClient.setClientName(resourceManagementDTO.getClientName());
					Client clientDbResponse = clientsRepository.save(newClient);
					
					if(clientDbResponse != null) {
						clientId = clientDbResponse.getClientId();
						List<ClientLocation> locations = new ArrayList<>();
						
						for(String clientLocation: resourceManagementDTO.getClientLocation()) {
							ClientLocation newClientLocation = new ClientLocation();
							newClientLocation.setClientId(clientDbResponse.getClientId());
							newClientLocation.setClientLocation(clientLocation);
							locations.add(newClientLocation);
						}
						
						//Add WFH location
						boolean contains = Arrays.stream(resourceManagementDTO.getClientLocation()).anyMatch("WFH"::equals);
						if(!contains) {
							ClientLocation newClientLocation = new ClientLocation();
							newClientLocation.setClientId(clientDbResponse.getClientId());
							newClientLocation.setClientLocation("WFH");
							locations.add(newClientLocation);
						}
						
						List<ClientLocation> clientLocationDbResponse = clientLocationRepository.saveAll(locations);
						
						if(!clientLocationDbResponse.isEmpty()) {
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("Client Location added");
							apiLogInfo.setApiResponse("Client Location added");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

						}else {
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("Failed to add client Location");
							apiLogInfo.setApiResponse("Failed to add client Location");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

							return response;
						}
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Failed to add client");
						apiLogInfo.setApiResponse("Failed to add client");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
                        return response;
					}
				}
				
				//Find ProjectManager empId
				Long employeementID = Long.parseLong(resourceManagementDTO.getProjectManager().split("-")[1]);
				Long projManagerId = null;
				Employee employee = employeeRepository.findByEmployeementId(employeementID);
				if(employee != null) {
					projManagerId = employee.getEmpId();
				}
				
				//Add project
				Project newProject = new Project();
				newProject.setProjectManagerId(projManagerId);
				newProject.setProjectName(resourceManagementDTO.getName());
				newProject.setState(resourceManagementDTO.getClientState());
				newProject.setClientId(clientId);
				newProject.setPoProjectId(resourceManagementDTO.getId());
				newProject.setActive("true");
				newProject.setSyncProject("true");
				if(resourceManagementDTO.getIsHOD().equals("true")) {
					newProject.setIsDraftProject("false");
				}else {
					newProject.setIsDraftProject("true");
				}
				newProject.setCreatedBy(resourceManagementDTO.getCreatedBy());
				
				Project projectDbResponse = projectRepository.save(newProject);
				
				if(projectDbResponse != null) {
					// Add project Department Mapping
					
					for(String department: resourceManagementDTO.getDepartment()){
						Department departmentObj = departmentRepository.findByName(department);
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
					}
					// Add Team

					resourceManagementDTO.getTeamList().forEach((teamObj) -> {
						// create new team
						StringBuilder deptList = new StringBuilder("");
						for(String department: teamObj.getDepartmentList()) {
								deptList.append(department).append(",");
						}
						//TeamLead
						Long teamLeadId = null;
						String teamLeadName = null;
						for(TeamMemberDTO teamMember: teamObj.getTeamMemberList()) {
							if((teamMember.getIsTeamLead() != null) && (teamMember.getIsTeamLead().equals("true"))) {
								teamLeadId = teamMember.getEmpId();
								teamLeadName = teamMember.getName();
							}
						}
						
						Team newTeamObj = new Team();
						newTeamObj.setIsActive("Y");
						newTeamObj.setProjectId(projectDbResponse.getProjectId());
						newTeamObj.setTeamLeadId(teamLeadId);
						newTeamObj.setTeamName(teamObj.getTeamName());
						newTeamObj.setTeamLeadName(teamLeadName);
						newTeamObj.setDeptIds(deptList.toString());
						newTeamObj.getCommonProperty().setCreatedBy(resourceManagementDTO.getCreatedBy());
						Team teamDbResponse = teamRepository.save(newTeamObj);
						
						if(teamDbResponse != null) {
							List<EmployeeTeamMap> mapList = new ArrayList<EmployeeTeamMap>();
							// Add team member in team
							
							for(TeamMemberDTO teamMember: teamObj.getTeamMemberList()) {
								
								EmployeeTeamMap newEmpTeamMap = new EmployeeTeamMap();
								
								//TeamLead
								if((teamMember.getIsTeamLead() != null) && (teamMember.getIsTeamLead().equals("true"))) {
									newEmpTeamMap.setEmpId(teamMember.getEmpId());
									newEmpTeamMap.setActive(1l);
									newEmpTeamMap.setEmployeeRole("TeamLead");
									newEmpTeamMap.setTeamId(teamDbResponse.getTeamId());
									mapList.add(newEmpTeamMap);
								}else {
									StringBuilder employeeRole = new StringBuilder("");
									for(String empRole: teamMember.getEmployeeRole()) {
										employeeRole.append(empRole).append(",");
									}
									
									newEmpTeamMap.setEmpId(teamMember.getEmpId());
									newEmpTeamMap.setActive(1l);
									newEmpTeamMap.setEmployeeRole(employeeRole.toString());
									newEmpTeamMap.setTeamId(teamDbResponse.getTeamId());
									mapList.add(newEmpTeamMap);									
								}
								
							}
							List<EmployeeTeamMap> teamMemberDbResponse = employeeTeamMapRepository.saveAll(mapList);
							
							// Add default activity
							Activity newActivityCreated = null;
							if(!teamMemberDbResponse.isEmpty()) {
								for(String department: teamObj.getDepartmentList()) {
										List<ActivityTemplate> activityTemplate = activityTemplateRepository.getByDeptId(Long.parseLong(department));
										if(!activityTemplate.isEmpty()) {
											
											for(ActivityTemplate activityObject: activityTemplate) {
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
							if(newActivityCreated != null) {
								response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
								response.setServiceResponse("Team created successfully.");
								apiLogInfo.setApiResponse("Team created successfully");
								apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

							}else {
								response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
								response.setServiceResponse("Team created successfully, but default activities are not mapped");
								apiLogInfo.setApiResponse("Team created successfully, but default activities are not mapped");
								apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
							}
						}else {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Unable to create new team.");
							apiLogInfo.setApiResponse("Unable to create new team.");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						}
					});	
					
					//Send mail to RMG: if HOD/SuperAdmin has created project/Team
					if(resourceManagementDTO.getIsHOD().equals("true") && !resourceManagementDTO.getProjectType().equals("Internal")) {
						try {
							mailService.sendMailWithCC(rmgMail, employeeObj.getEmail(),
									"Regarding Resource managment",
									"Dear RMG Team ,"+"<br>"
									+"<br>"
								    + employeeObj.getName() +" has created project: " + resourceManagementDTO.getName());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					// Send Project/Team detail JSON to PoPotal
					
					if(!resourceManagementDTO.getProjectType().equals("Internal")) {
						ServiceResponse poPortalResponse = sendProjectInfoToPoPortal(resourceManagementDTO);
						
						if(poPortalResponse.getServiceStatus().equals("Success")) {
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("Project updated successfully");
							apiLogInfo.setApiResponse("Project updated successfully");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

							
						}else {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Project & Team created successfully,but unable to sync with PoPortal : "+poPortalResponse.getServiceResponse());
							apiLogInfo.setApiResponse("Project & Team created successfully ,but unable to sync with PoPortal");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

						}	
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Project updated successfully");
						apiLogInfo.setApiResponse("Project updated successfully");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}
				}
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

	public ServiceResponse getTeamListByProjectName(ResourceManagementDTO resourceManagementDTO) {
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setApiUrl("/api/getTeamListByProjectName");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("ProjectId : " + resourceManagementDTO.getProjectId()+ " ,ProjectName :" + resourceManagementDTO.getName() + " ,Id : " + resourceManagementDTO.getId());

		try {
			
			Project projectObj = null;
			if(resourceManagementDTO.getProjectType().equals("Internal")) {
				projectObj = projectRepository.findByProjectId(resourceManagementDTO.getProjectId());
			}else {
				projectObj = projectRepository.findByPoProjectId(resourceManagementDTO.getId());				
			}
			
			if(projectObj != null) {
				List<Team> teamList = teamRepository.findByProjectIdAndIsActive(projectObj.getProjectId(), "Y");
				
				if(!teamList.isEmpty()) {
					List<TeamDTO> teamListdto = new ArrayList<TeamDTO>();
					teamList.forEach((object) -> {
						TeamDTO teamdto = new TeamDTO();
						
						//Get Team Info
						teamdto.setTeamId(object.getTeamId());
						teamdto.setTeamName(object.getTeamName());
						teamdto.setDepartmentList(object.getDeptIds().split(","));
						
						//Get teamMembers Info
						List<EmployeeTeamMap> empTeamMapping = employeeTeamMapRepository.findByTeamIdAndActive(object.getTeamId(), 1l);
						if(!empTeamMapping.isEmpty()) {
							List<TeamMemberDTO> teamMember = new ArrayList<TeamMemberDTO>();
							empTeamMapping.forEach((teamMemberObj) -> {
								String employeeName = null;
								Employee empObj = employeeRepository.findByEmpId(teamMemberObj.getEmpId());
								if(empObj != null) {
									employeeName = empObj.getName();
								}
								
								TeamMemberDTO teamMemberDTO = new TeamMemberDTO();
								
								if((object.getTeamLeadId() != null) && (object.getTeamLeadId().equals(teamMemberObj.getEmpId()))) {
									//Team Lead
									teamMemberDTO.setEmpId(teamMemberObj.getEmpId());
									teamMemberDTO.setName(employeeName);
									teamMemberDTO.setIsTeamLead("true");
									teamMemberDTO.setStartDate(teamMemberObj.getStartDate().toString());
									teamMemberDTO.setEmployeeRole(teamMemberObj.getEmployeeRole().split(","));
									teamMember.add(teamMemberDTO);
								}else {
									//Team Member
									teamMemberDTO.setEmpId(teamMemberObj.getEmpId());
									teamMemberDTO.setName(employeeName);
									teamMemberDTO.setStartDate(teamMemberObj.getStartDate().toString());
									teamMemberDTO.setEmployeeRole(teamMemberObj.getEmployeeRole().split(","));
									teamMember.add(teamMemberDTO);
								}
							});
							teamdto.setTeamMemberList(teamMember);
						}else {
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
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No team(s) found in the project.");
                    apiLogInfo.setApiResponse("No team(s) found in the project.");			
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

	public ServiceResponse alreadyCreatedTeam() {
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("alreadyCreatedTeam");
        apiLogInfo.setApiUrl("/api/alreadyCreatedTeam");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
       // logBuilder.append("");
		try {
			
			List<Project> allProjectList = projectRepository.findAll();
			if(!allProjectList.isEmpty()) {
				List<ResourceManagementDTO> dtoList = new ArrayList<ResourceManagementDTO>();
				allProjectList.forEach((projObject) -> {
					Long count = teamRepository.countByProjectId(projObject.getProjectId());
					
					if(count > 0) {
						ResourceManagementDTO resourceDTO = new ResourceManagementDTO();
						
						resourceDTO.setName(projObject.getProjectName());
						resourceDTO.setIsDraftProject(projObject.getIsDraftProject());
						resourceDTO.setPoProjectId(projObject.getPoProjectId());
						resourceDTO.setProjectId(projObject.getProjectId());
						dtoList.add(resourceDTO);
					}
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
                apiLogInfo.setApiResponse("dtolist size :" + dtoList.size());
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
	
	public ServiceResponse getPendingForApprovalProject() {
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setApiUrl("/api/getPendingForApprovalProject");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
		try {
			
			List<Object[]> pendingPRoject = projectRepository.findProjectByIsDraftProject();
			List<ResourceManagementDTO> dtoList = new ArrayList<ResourceManagementDTO>();
			
			if(!pendingPRoject.isEmpty()) {
				pendingPRoject.forEach((object) -> {
					ResourceManagementDTO dto = new ResourceManagementDTO();
					
					int projectId = object[0] != null ? Integer.parseInt(object[0].toString()) : null;
					List<String> department = new ArrayList<String>();
					
					List<Object[]> projDeptMap = projectDepartmentMapRepository.getDepartmentByProjectId(projectId);
					if(!projDeptMap.isEmpty()) {
						projDeptMap.forEach((dept) -> {
							String departmentName = dept[1] != null ? dept[1].toString() : null;
							department.add(departmentName);
						});
					}
					
                    Long count = teamRepository.countByProjectId(projectId);
					String isTeamCreated = "false";
					if(count > 0) {
						isTeamCreated = "true";
					}
					
					dto.setId(Long.valueOf(projectId));
					dto.setName(object[1] != null ? object[1].toString() : null);
					dto.setProjectManagerName(object[2] != null ? object[2].toString() : null);
					dto.setProjectManagerId(object[3] != null ? Long.parseLong(object[3].toString()) : null);
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

			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Pending Project found.");
                apiLogInfo.setApiResponse("No Pending Project Found");			
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
	
	public ServiceResponse approvePendingProject(ResourceManagementDTO resourceManagementDTO) {
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("ApprovePendingProject");
        apiLogInfo.setApiUrl("/api/approvePendingProject");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("ProjectId : " + resourceManagementDTO.getId() + ", EmpId : " + resourceManagementDTO.getEmpId());

		try {
			
			Project projectObj = projectRepository.findByPoProjectId(resourceManagementDTO.getId());;
			if(projectObj != null) {
				
				projectObj.setIsDraftProject("false");
				Project projectDbResponse = projectRepository.save(projectObj);
				
				if(projectDbResponse != null) {
					
					Employee employeeObj = employeeRepository.findByEmpId(resourceManagementDTO.getEmpId());
					if(employeeObj != null) {
						
						//Send project Approval successfully mail to rmg
						try {
							mailService.sendMailWithCC(rmgMail, employeeObj.getEmail(),
									"Regarding Project Approval",
									"Dear RMG Team ,"+"<br>"
									+"<br>"
									+ employeeObj.getName() +" has approved the project : " +resourceManagementDTO.getName()
									+"<br>"
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

						
						// Send Project/Team detail JSON to PoPotal
						
						ServiceResponse poPortalResponse = sendProjectInfoToPoPortal(resourceManagementDTO);
						
						if(poPortalResponse.getServiceStatus().equals("Success")) {
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("Project Approved.");
							apiLogInfo.setApiResponse("Project Approved");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

						}else {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Project & Team created successfully,but unable to sync with PoPortal : "+poPortalResponse.getServiceResponse());
							apiLogInfo.setApiResponse("Project & Team created successfully,but unable to sync with PoPortal" +poPortalResponse.getServiceResponse());
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

						}				
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("User's mail address not found.");
                        apiLogInfo.setApiResponse("User's mail address not found");			
                        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

					}
					
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Unable to approve project.");
                    apiLogInfo.setApiResponse("Unable to approve project");			
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

				}
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

	public ServiceResponse rejectPendingProject(ResourceManagementDTO resourceManagementDTO) {
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("Reject Pending Project");
        apiLogInfo.setApiUrl("/api/rejectPendingProject");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("Project Id : "+ resourceManagementDTO.getId() + " ,EmployeeId :" + resourceManagementDTO.getEmpId());
		try {
			
			Project projectObj = projectRepository.findByPoProjectId(resourceManagementDTO.getId());
			if(projectObj != null) {
				
				projectObj.setIsDraftProject("Rejected");
				Project projectDbResponse = projectRepository.save(projectObj);
				
				if(projectDbResponse != null) {
					
					Employee employeeObj = employeeRepository.findByEmpId(resourceManagementDTO.getEmpId());
					if(employeeObj != null) {
						
						//Send project rejection successfully mail to rmg
						try {
							mailService.sendMailWithCC(rmgMail, employeeObj.getEmail(),
									"Regarding Project Rejection",
									"Dear RMG Team ,"+"<br>"
									+"<br>"
									+ employeeObj.getName() +" has rejected the project : " +resourceManagementDTO.getName()
									+"<br>"
									+ "Reject Reason : " + resourceManagementDTO.getRejectReason());
						} catch (Exception e) {
							e.printStackTrace();
						}
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Project Rejected.");
                        apiLogInfo.setApiResponse("Project Rejected");
                        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

					}else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("User's mail address not found.");
                        apiLogInfo.setApiResponse("User's Mail address not Found.");
                        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

					}
					
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Unable to reject project.");
                    apiLogInfo.setApiResponse("Unable to reject Project");
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

				}
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
	
	public ServiceResponse sendProjectApproval(ResourceManagementDTO resourceManagementDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("sendProjectApproval");
		apiLogInfo.setApiUrl("/api/sendProjectApproval");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("ProjectId : " + resourceManagementDTO.getId() + "ProjectName : " + resourceManagementDTO.getName() + " ,ProjectManagerName : " 
		+ resourceManagementDTO.getProjectManagerName() + " ,ClientName : " + resourceManagementDTO.getClientName());
		try {
			
			StringBuilder html = new StringBuilder();
			html.append("<html>\n" +
			        "  <head>\n" +
			        "    <style>\n" +
			        "      table {\n" +
			        "        width: 100%;\n" +
			        "        border-collapse: collapse;\n" +
			        "        font-family: Arial, sans-serif;\n" +
			        "        font-size: 14px;\n" +
			        "      }\n" +
			        "      th, td {\n" +
			        "        border: 1px solid black;\n" +
			        "        padding: 8px;\n" +
			        "        text-align: left;\n" +
			        "      }\n" +
			        "      th {\n" +
			        "        background-color: #dddddd;\n" +
			        "      }\n" +
			        "      h2 {\n" +
			        "        font-size: 18px;\n" +
			        "        font-weight: bold;\n" +
			        "        margin-bottom: 12px;\n" +
			        "      }\n" +
			        "    </style>\n" +
			        "  </head>\n" +
			        "  <body>\n" +
			        "    <h2>Project Details</h2>\n" +
			        "    <table>\n" +
			        "      <tr>\n" +
			        "        <th>Project Name</th>\n" +
			        "        <th>Project Manager</th>\n" +
			        "        <th>Client Name</th>\n" +
			        "        <th>State</th>\n" +
			        "      </tr>\n");

			// add rows to the table
			html.append("      <tr>\n");
			  // add cells to the row
			  html.append("        <td>" + resourceManagementDTO.getName() + "</td>\n");
			  html.append("        <td>" + resourceManagementDTO.getProjectManagerName() + "</td>\n");
			  html.append("        <td>" + resourceManagementDTO.getClientName() + "</td>\n");
			  html.append("        <td>" + resourceManagementDTO.getClientState() + "</td>\n");
			  html.append("      </tr>\n");

			html.append("    </table>\n");

			html.append("<br>\n"
			        + "<h2>Team Info:</h2>\n");
			if(!resourceManagementDTO.getTeamList().isEmpty()) {
			    for(int i = 0; i < resourceManagementDTO.getTeamList().size();i++) {
			        html.append("<h3>Team Name - " + (i + 1) + " : " + resourceManagementDTO.getTeamList().get(i).getTeamName() + "</h3>\n");
			        html.append("<h4>Team Member Info:</h4>\n");
			        if(!resourceManagementDTO.getTeamList().get(i).getTeamMemberList().isEmpty()) {
			            html.append("<ul>\n");
			            for(TeamMemberDTO teamMember : resourceManagementDTO.getTeamList().get(i).getTeamMemberList()) {
			            	if((teamMember.getIsTeamLead() != null) && teamMember.getIsTeamLead().equals("true")) {
			            		html.append("<li>" + teamMember.getName() +" (Team Lead)" + "</li>\n");			            		
			            	}else {
			            		html.append("<li>" + teamMember.getName() + "</li>\n");
			            	}
			            }
			            html.append("</ul>\n");
					}
						html.append("<br><br>");
				}				
			}
			   html.append("<a style='background-color:blue;color:white;padding:10px 20px;text-decoration:none;border-radius:4px;' href='http://localhost:4200/#/user-team/resource-management/"+resourceManagementDTO.getId()+"'>Approve Here</a>");
				
				html.append("  </body>\n" +
			            "</html>");
				
				List<String> mailList = new ArrayList<String>();
				
				for(String dept: resourceManagementDTO.getDepartment()) {
					Department deptObj = departmentRepository.findByName(dept);	
					
					if(deptObj != null) {
						Employee empObj = employeeRepository.findByEmpId(deptObj.getHodId());
						if(empObj != null) {
							mailList.add(empObj.getEmail());
						}
					}
				}
				
				if(!mailList.isEmpty()) {
					boolean mailSent = mailService.sendMailWithCC(String.join(",", mailList), rmgMail,
							"Regarding Project Resource Management Application Request",
							"Dear Team ,"+"<br>"
									+"<br>"+"Please take necessary actions : "
									+"<br>"
									+ html.toString());
					
					if(mailSent) {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Project Approval mail sent to HOD's");
                        apiLogInfo.setApiResponse("Project Approval mail sent to HOD's");
                        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

					}else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Unable to send Approval mail to HOD's");
						apiLogInfo.setApiResponse("Unable to send Approval mail to HOD's");
                        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

					}
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
	
	public String getEmploymentId(Long empId) {
		Employee employeeObj = employeeRepository.findByEmpId(empId);
		String employmentId = null;
		if(employeeObj != null) {
			employmentId = ("A-").concat(employeeObj.getEmployeementId().toString());
		}
		return employmentId;
	}
	
	public ServiceResponse sendProjectInfoToPoPortal(ResourceManagementDTO resourceManagementDTO) {
		ServiceResponse response = new ServiceResponse();
		
		try {
			
			Project projectObj = projectRepository.findByPoProjectId(resourceManagementDTO.getId());
			List<PoProjectSyncDTO> projectInfo = new ArrayList<PoProjectSyncDTO>();
			List<PoTeamDTO> teamList = new ArrayList<PoTeamDTO>();
			
			if(projectObj != null) {
				PoProjectSyncDTO projectDTO = new PoProjectSyncDTO();
				
				projectDTO.setPoProjectId(projectObj.getPoProjectId());
				projectDTO.setProjectName(projectObj.getProjectName());
				
				String projectManagerId = getEmploymentId(projectObj.getProjectManagerId());
				projectDTO.setPoProjectManagerId(projectManagerId != null ? projectManagerId : null);
				
				//Get Team Details
				List<Team> teamDetails = teamRepository.findByProjectIdAndIsActive(projectObj.getProjectId(), "Y");
				
				if(!teamDetails.isEmpty()) {
					teamDetails.forEach((team) -> {
						List<String> teamMember = new ArrayList<String>();

						PoTeamDTO poTeamDTO = new PoTeamDTO();
						
						poTeamDTO.setIshineTeamId(team.getTeamId());
						poTeamDTO.setTeamName(team.getTeamName());
						
						if(team.getCommonProperty().getCreatedBy() != null) {
							String createdBy = getEmploymentId(team.getCommonProperty().getCreatedBy());
							poTeamDTO.setCreatedBy(createdBy);
						}
						
						if(team.getCommonProperty().getUpdatedBy() != null) {
							String updatedBy = getEmploymentId(team.getCommonProperty().getCreatedBy());
							poTeamDTO.setUpdatedBy(updatedBy);
						}
						
						if(team.getCommonProperty().getUpdatedOn() != null) {
							poTeamDTO.setUpdatedOn(team.getCommonProperty().getUpdatedOn().toString());
						}
						
						if(team.getTeamLeadId() != null) {
							String teamLeadId = getEmploymentId(team.getTeamLeadId());
							poTeamDTO.setPoTeamLeadId(teamLeadId);						
						}
						
						String[] deptIds = team.getDeptIds().split(",");
						List<String> departments = new ArrayList<String>();
						for(String deptId : deptIds) {
							Department deptObj = departmentRepository.getById(Long.parseLong(deptId));
							if(deptObj != null) {
								departments.add(deptObj.getName());
							}
						}
						String deptList[] = departments.toArray(new String[departments.size()]);
						poTeamDTO.setDepartmentList(deptList);
						
						//Get TeamMember Details
						List<EmployeeTeamMap> teamMemberDetials = employeeTeamMapRepository.findByTeamIdAndActive(team.getTeamId(), 1l);
						
						if(!teamMemberDetials.isEmpty()) {
							teamMemberDetials.forEach((member) -> {
								String memberEmpId = getEmploymentId(member.getEmpId());
								teamMember.add(memberEmpId);
							});
						}
						String teamMemberList[] = teamMember.toArray(new String[teamMember.size()]);
						poTeamDTO.setTeamMemberList(teamMemberList);
						
						teamList.add(poTeamDTO);
					});
					projectDTO.setTeamList(teamList);
				}
				
				projectInfo.add(projectDTO);		
				//Send projectDTO in PoPortal reverse-sync API
				
				try {
                    JSONArray jsonarray = new JSONArray(projectInfo);
					
					System.out.println(jsonarray  + " : jsonarray \n\n\n");
					
					final String syncUrl = syncProjectApi;
					RestTemplate restTemplate = new RestTemplate();
					String syncResponse = restTemplate.postForObject(syncUrl, projectInfo, String.class);
					
					JSONObject json = new JSONObject(syncResponse);
					
					System.out.println(syncResponse  + " : syncResponse \n\n\n");
					
					if(json.getInt("httpStatusCode") == 200) {
						//Send Mail to PoPortal
					     try {
								mailService.sendMail(rmgMail,
										"Regarding Project Sync With PoPortal",
										"Dear RMG Team ,"+"<br>"
										+"<br>"
										+"Project : " +resourceManagementDTO.getName()+ "has been successfully synced with PoPortal.");
							} catch (Exception e) {
								e.printStackTrace();
							}
					     
					     response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						 response.setServiceResponse(json.get("message"));
					}
					
				}catch(InternalServerError e) {
					JSONObject json = new JSONObject(e.getResponseBodyAsString());
					
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse(json.get("message"));
					
				}catch(HttpClientErrorException e) {
                    JSONObject json = new JSONObject(e.getResponseBodyAsString());
					
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse(json.get("message"));
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

	public ServiceResponse bulkSyncProject(ProjectDTO projectDTO) {

		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("BulkSyncProject");
        apiLogInfo.setApiUrl("/api/bulkSyncProject");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("BulkSyncList : " + projectDTO.getBulkSyncList().size());

		try {
			
			for(ResourceManagementDTO rmg :projectDTO.getBulkSyncList()) {
				
				ServiceResponse syncResponse = sendProjectInfoToPoPortal(rmg);
				
				if(syncResponse.getServiceStatus().equals("Success")) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Project Synced successfully.");
					apiLogInfo.setApiResponse("Project Synced Successfully");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Project & Team created successfully,but unable to sync with PoPortal : "+syncResponse.getServiceResponse());
					apiLogInfo.setApiResponse("Project & Team created successfully,but unable to sync with PoPortal");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}	
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

	public ServiceResponse getInternalProject() {
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        //apiLogInfo.setSubFeatureName("");
        apiLogInfo.setApiUrl("/api/getInternalProject");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("InternalProjectList : "+ projectRepository.getAllInternalProject().size());

		try {
			
			List<Object[]> allInternalProject = projectRepository.getAllInternalProject();
			List<ResourceManagementDTO> projectInfo = new ArrayList<ResourceManagementDTO>();
			
			if(allInternalProject != null) {
				allInternalProject.forEach((object) -> {
					ResourceManagementDTO projectDTO = new ResourceManagementDTO();
					
					projectDTO.setProjectType("Internal");
					projectDTO.setName(object[0] != null ? object[0].toString() : null);
					projectDTO.setProjectManager(object[10] != null ? "A-".concat(object[10].toString()) : null);
					projectDTO.setProjectManagerName(object[2] != null ? object[2].toString() : null);
					projectDTO.setProjectId(object[3] != null ? Integer.parseInt(object[3].toString()) : null);
					projectDTO.setStatus(object[11] != null ? object[11].toString() : null);
					
					//Find ClientName
					Integer clientId = object[7] != null ? Integer.parseInt(object[7].toString()) : null;
					if(clientId != null) {
						Client clientObj = clientsRepository.findByClientId(clientId);
						
						projectDTO.setClientName(clientObj.getClientName());
					}else {
						projectDTO.setClientName(null);
					}
					
					projectDTO.setClientState(object[8] != null ? object[8].toString() : null);
					projectDTO.setCreatedOn(object[5] != null ? object[5].toString() : null);
					projectDTO.setIsDraftProject(object[9] != null ? object[9].toString() : null);
					
					//Find ClientLocation
					if(clientId != null) {
						List<ClientLocation> clientLocation = clientLocationRepository.findByClientId(clientId);
						
						if(clientLocation != null) {
							
							String[] locationList = clientLocation.stream()
								    .map((ClientLocation location) -> location.getClientLocation()).collect(Collectors.toList())
									.toArray(String[]::new);
							
							projectDTO.setClientLocation(locationList);
						}
					}
					
					//Find Project department
					Integer projectId = object[3] != null ? Integer.parseInt(object[3].toString()) : null;
					List<ProjectDepartmentMap> allDeptList = projectDepartmentMapRepository.findByProjectId(projectId);
					List<String> deptList = new ArrayList<String>();
					
					if(allDeptList != null) {
						allDeptList.forEach((dept) -> {
							Department deptObj = departmentRepository.findByDeptId(dept.getDeptId());
							
							if(deptObj != null) {
								deptList.add(deptObj.getName());								
							}
						});
						
						String[] department = deptList.stream().toArray(String[]::new);
						projectDTO.setDepartment(department);
					}
					projectInfo.add(projectDTO);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(projectInfo);
				apiLogInfo.setApiResponse("InternalProjectList fetched");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Internal project found.");
				apiLogInfo.setApiResponse("No Internal Project Found");
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

}
