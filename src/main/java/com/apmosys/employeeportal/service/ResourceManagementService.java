package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.ResourceManagementDTO;
import com.apmosys.employeeportal.dto.TeamMemberDTO;
import com.apmosys.employeeportal.model.Activity;
import com.apmosys.employeeportal.model.ActivityTemplate;
import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.ClientLocation;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.DraftTeam;
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
import com.apmosys.employeeportal.repository.DraftTeamRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.ProjectDepartmentMapRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.TeamRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class ResourceManagementService {
	
	@Autowired
	ProjectService projectService;
	
	@Autowired
	DraftTeamRepository draftTeamRepository;
	
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

	public ServiceResponse createDraftProjectInfo(ResourceManagementDTO resourceManagementDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			Project projectObj = projectRepository.findByProjectName(resourceManagementDTO.getName());
			if(projectObj != null) {
				
				
				System.out.println("project found");
				
				
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
						ClientLocation newClientLocation = new ClientLocation();
						newClientLocation.setClientId(clientDbResponse.getClientId());
						newClientLocation.setClientLocation(resourceManagementDTO.getClientLocation());
						ClientLocation clientLocationDbResponse = clientLocationRepository.save(newClientLocation);
						
						if(clientLocationDbResponse != null) {
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
				
				//Add project
				Project newProject = new Project();
				newProject.setProjectManagerId(resourceManagementDTO.getProjectManagerId());
				newProject.setProjectName(resourceManagementDTO.getName());
				newProject.setState(resourceManagementDTO.getClientState());
				newProject.setClientId(clientId);
				newProject.setPoProjectId(resourceManagementDTO.getId());
				newProject.setActive("true");
				newProject.setSyncProject("true");
				newProject.setIsDraftProject("true");
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
						newTeamObj.getCommonProperty().setCreatedBy(teamObj.getEmpId());
						Team teamDbResponse = teamRepository.save(newTeamObj);
						
						if(teamDbResponse != null) {
							List<EmployeeTeamMap> mapList = new ArrayList<EmployeeTeamMap>();
							// Add team member in team
							
							for(TeamMemberDTO teamMember: teamObj.getTeamMemberList()) {
								
								StringBuilder employeeRole = new StringBuilder("");
								for(String empRole: teamMember.getEmployeeRole()) {
									employeeRole.append(empRole).append(",");
								}
								
								EmployeeTeamMap newEmpTeamMap = new EmployeeTeamMap();
								
								//TeamLead
								if(teamMember.getIsTeamLead().equals("true")) {
									newEmpTeamMap.setEmpId(teamMember.getEmpId());
									newEmpTeamMap.setActive(1l);
									newEmpTeamMap.setEmployeeRole("TeamLead");
									newEmpTeamMap.setTeamId(teamDbResponse.getTeamId());
									mapList.add(newEmpTeamMap);
								}
								
								newEmpTeamMap.setEmpId(teamMember.getEmpId());
								newEmpTeamMap.setActive(1l);
								newEmpTeamMap.setEmployeeRole(employeeRole.toString());
								newEmpTeamMap.setTeamId(teamDbResponse.getTeamId());
								mapList.add(newEmpTeamMap);
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
												newActivity.getCommonProperty().setCreatedBy(4l);

												newActivityCreated = activitiesRepository.save(newActivity);
											}
									}
								}
							}
							if(newActivityCreated != null) {
								response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
								response.setServiceResponse("Team created successfully.");
							}else {
								response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
								response.setServiceResponse("Team created successfully, but default activities are not mapped");
							}
						}else {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Unable to create new team.");
						}
					});				
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

}
