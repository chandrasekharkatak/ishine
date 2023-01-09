package com.apmosys.employeeportal.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.ClientsDTO;
import com.apmosys.employeeportal.dto.PoProjectSyncDTO;
import com.apmosys.employeeportal.dto.PoTeamDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
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
public class ProjectService {

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
	
	public ServiceResponse getAllClients() {
		ServiceResponse response = new ServiceResponse();
		try {
			
			List<Object[]> clientInfo = clientsRepository.getClientInfo();
			
			if(clientInfo != null) {
				List<ClientsDTO> dtoList = new ArrayList<ClientsDTO>();
				
				clientInfo.forEach((object) -> {
					ClientsDTO clientDto = new ClientsDTO();
					
					clientDto.setClientId(object[0] != null ? Integer.valueOf(object[0].toString()) : null);
					clientDto.setClientName(object[1] != null ? object[1].toString() : null);
					clientDto.setClientLocation(object[2] != null ? object[2].toString() : null);
					clientDto.setClientLocationId(object[3] != null ? Integer.valueOf(object[3].toString()) : null);
					
					dtoList.add(clientDto);
				});
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Client Info not found.");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getAllProjects() {
		ServiceResponse response = new ServiceResponse();
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
					
					dtoList.add(projectDto);
				});
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Projects found.");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
	public ServiceResponse createProject(PoProjectSyncDTO poProjectSyncDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			Project projectObj = new Project();
			projectObj.setProjectName(poProjectSyncDTO.getProjectName());
			projectObj.setProjectManagerId(poProjectSyncDTO.getProjectManagerId());
			projectObj.setClientId(poProjectSyncDTO.getClientId());
			projectObj.setState(poProjectSyncDTO.getState());
			projectObj.setActive("true");
			projectObj.setSyncProject(poProjectSyncDTO.getSyncProject());
			projectObj.setCreatedBy(Long.parseLong(poProjectSyncDTO.getCreatedBy()));
			Project projectDbResponse =  projectRepository.save(projectObj);
			
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
				response.setServiceResponse("Project created successfully.");
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to create project.");
			}
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
	public ServiceResponse getProjectByProjectId(PoProjectSyncDTO poProjectSyncDto) {
		ServiceResponse response = new ServiceResponse();
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
				projectdto.setProjectManagerId(projectObj.getProjectManagerId());
				projectdto.setState(projectObj.getState());
				projectdto.setProjectId(projectObj.getProjectId());
				projectdto.setSyncProject(projectObj.getSyncProject());
				dtoList.add(projectdto);
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not found.");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
	public ServiceResponse updateProject(PoProjectSyncDTO poProjectSyncDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			Project project = projectRepository.getById(poProjectSyncDTO.getProjectId());
			
			if(project != null) {
				
				project.setProjectName(poProjectSyncDTO.getProjectName());
				project.setProjectManagerId(poProjectSyncDTO.getProjectManagerId());
				project.setClientId(poProjectSyncDTO.getClientId());
				project.setState(poProjectSyncDTO.getState());
				project.setActive("true");
				project.setSyncProject(poProjectSyncDTO.getSyncProject());
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
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Project updation failed.");
				}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not found.");
			}
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
	public ServiceResponse deleteProject(PoProjectSyncDTO poProjectSyncDto) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			Optional<Project> projectObj = projectRepository.findById(poProjectSyncDto.getProjectId());
			if (projectObj.isPresent()) {
				Project departmentToBeDeleted = projectObj.get();
				departmentToBeDeleted.setActive("false");
				departmentToBeDeleted.setSyncProject("false");
				Project dbResponse =  projectRepository.save(departmentToBeDeleted);
				
				if(dbResponse != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Project deleted.");
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Project deletion Failed.");
				}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not found.");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
	/*
	  - Po Project Sync API : start
	 */
	
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
				
				if(poProjectSyncDTO.getPoProjectManagerId() == null || poProjectSyncDTO.getPoProjectManagerId() == "") {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Please provide PoProject Manager Id.");
					return response;
				}else if(!validationService.validateEmploymentId(Long.parseLong(poProjectSyncDTO.getPoProjectManagerId().split("-")[1]))) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No user exist as project manager with EmpId : " + poProjectSyncDTO.getPoProjectManagerId());
					return response;
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
									response.setServiceResponse("Department does not exists : " + team.getTeamName());
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
				Employee managerObj = getEmployeeByEmployeementId(poProjectSyncDTO.getPoProjectManagerId());
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
						if(teamExists != null) {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Team name already exist : " + team.getTeamName());
							return response;
						}
					}
					Employee updatedByObj = getEmployeeByEmployeementId(poProjectSyncDTO.getUpdatedBy());
					
					//update project info
					project.setProjectName(poProjectSyncDTO.getProjectName());
					project.setProjectManagerId(managerObj.getEmpId());
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
								teamObj.setTeamName(object.getTeamName());
								teamObj.setTeamLeadId(teamLeadObj !=null ? teamLeadObj.getEmpId() : null);
								teamObj.setDescription(object.getDescription());
								teamObj.setTeamLeadName(teamLeadObj !=null ? teamLeadObj.getName() : null);
								teamObj.setDeptIds(deptList.toString());
								teamObj.getCommonProperty().setUpdatedBy(teamUpdatedByObj !=null ? teamUpdatedByObj.getEmpId() : null);
								teamObj.getCommonProperty().setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
								Team teamDbResponse = teamRepository.save(teamObj);
								
								if(teamDbResponse != null) {
									//update employee team mapping : adding new member, in-active removed member
									List<Long> teamMemberList = new ArrayList<Long>();
									List<String> newTeamMemberList = new ArrayList<String>(Arrays.asList(object.getTeamMemberList()));
									//Adding projectManager, HOD, teamLead in teamMemberList
									newTeamMemberList.add(poProjectSyncDTO.getPoProjectManagerId());
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
												findFirstByEmpIdAndTeamIdAndActive(teamMemberObj.getEmpId(), teamDbResponse.getTeamId(), 1l);
										// adding new member
										if(teamMappingObj.isEmpty()) {
											EmployeeTeamMap empTeamMap = new EmployeeTeamMap();
											empTeamMap.setEmpId(teamMemberObj.getEmpId());
											empTeamMap.setTeamId(teamDbResponse.getTeamId());
											empTeamMap.setActive(1l);
											empTeamMap.setEmployeeRole("Employee");
											EmployeeTeamMap teamMapDbResponse = employeeTeamMapRepository.save(empTeamMap);
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
								}else {
									response.setServiceStatus(ServiceResponse.STATUS_FAIL);
									response.setServiceResponse("Team updation failed.");
								}
							}else {
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
								newTeamObj.getCommonProperty().setCreatedBy(teamCreatedByObj != null ? teamCreatedByObj.getEmpId() : null);
								Team teamDbResponse = teamRepository.save(newTeamObj);
								
								List<EmployeeTeamMap> teamMemberDbResponse = null;
								if(teamDbResponse != null) {
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
									defaultMemberMap.setEmpId(managerObj.getEmpId());
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
										if(( (teamLeadObj.getEmpId() == null) || (teamLeadObj.getEmpId() != null && !teamLeadObj.getEmpId().equals(teamMemberObj.getEmpId())) ) && 
												!managerObj.getEmpId().equals(teamMemberObj.getEmpId())
												&& ( (deptsObj.isEmpty()) || (!deptsObj.isEmpty() && 
														!deptsObj.stream().anyMatch(o -> teamMemberObj.getEmpId().equals(o.getHodId()))) )) {
											
											newEmpTeamMap.setActive(1l);
											newEmpTeamMap.setEmpId(teamMemberObj.getEmpId());
											newEmpTeamMap.setTeamId(teamDbResponse.getTeamId());
											newEmpTeamMap.setEmployeeRole("Employee");
											mapList.add(newEmpTeamMap);
										}	
									}
									teamMemberDbResponse = employeeTeamMapRepository.saveAll(mapList);
									
									// Add team member in team
//									for(String teamMember: object.getTeamMemberList()) {
//										Employee teamMemberObj = getEmployeeByEmployeementId(teamMember);
//										EmployeeTeamMap newEmpTeamMap = new EmployeeTeamMap();
//										
//										if(!teamLeadObj.getEmpId().equals(teamMemberObj.getEmpId()) && 
//												!managerObj.getEmpId().equals(teamMemberObj.getEmpId()) && 
//												!hodId.equals(teamMemberObj.getEmpId())) {
//											
//											newEmpTeamMap.setActive(1l);
//											newEmpTeamMap.setEmpId(teamMemberObj.getEmpId());
//											newEmpTeamMap.setTeamId(teamDbResponse.getTeamId());
//											newEmpTeamMap.setEmployeeRole("Employee");
//											mapList.add(newEmpTeamMap);
//										}
//									}
//									teamMemberDbResponse = employeeTeamMapRepository.saveAll(mapList);
								}
								Activity newActivityCreated = null;
								if(!teamMemberDbResponse.isEmpty()) {
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
													newActivity.setDeptId(activityObject.getDeptId());
													newActivity.getCommonProperty().setCreatedBy(4l);

													newActivityCreated = activitiesRepository.save(newActivity);
												}
											}
										}
									}
								}
								if(newActivityCreated != null) {
									response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
									response.setServiceResponse("Project updated & new Team created Successfully.");
								}else {
									response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
									response.setServiceResponse("Project updated & new Team created Successfully, but default activitis are not mapped.");
								}
							}
						});
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Project Updation failed.");
					}
				}else {
					
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
						if(!validationService.validateEmploymentId(Long.parseLong(team.getCreatedBy().split("-")[1]))) {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("No user found with EmpId : " + team.getCreatedBy());
							return response;
						}
					}
					
					Client clientDbresponse = null;
					// Add new client & client location if not exist
					if(client == null) {
						Client clientobj = new Client();
						clientobj.setClientName(poProjectSyncDTO.getClientName());
						clientobj.setPoClientId(poProjectSyncDTO.getPoClientId());
						
						clientDbresponse = clientsRepository.save(clientobj);
						if(clientDbresponse != null) {
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
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Unable to create new client.");
						}
					}
					Employee createdByObj = getEmployeeByEmployeementId(poProjectSyncDTO.getCreatedBy());
					
					// create new project
					Project projectObj = new Project();
					projectObj.setProjectName(poProjectSyncDTO.getProjectName());
					projectObj.setProjectManagerId(managerObj.getEmpId());
					projectObj.setPoProjectId(poProjectSyncDTO.getPoProjectId());
					projectObj.setClientId(clientId);
					projectObj.setState(poProjectSyncDTO.getState());
					projectObj.setActive("true");
					projectObj.setSyncProject("true");
					projectObj.setCreatedBy(createdByObj.getEmpId());
					Project projectDbResponse =  projectRepository.save(projectObj);
					
					// Add department mapping
					for(String department: poProjectSyncDTO.getDepartmentList()) {
						Department departmentObj = departmentRepository.findByName(department);
						ProjectDepartmentMap projectDeptMap = new ProjectDepartmentMap();
						projectDeptMap.setProjectId(projectDbResponse.getProjectId());
						projectDeptMap.setDeptId(departmentObj.getDeptId());
						ProjectDepartmentMap projDeptMapDbResponse = projectDepartmentMapRepository.save(projectDeptMap);
					}
					
					if(projectDbResponse != null) {
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
							newTeamObj.getCommonProperty().setCreatedBy(teamCreatedByObj.getEmpId());
							Team teamDbResponse = teamRepository.save(newTeamObj);
							if(teamDbResponse != null) {
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
								defaultMemberMap.setEmpId(managerObj.getEmpId());
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
									if(( (teamLeadObj.getEmpId() == null) || (teamLeadObj.getEmpId() != null && !teamLeadObj.getEmpId().equals(teamMemberObj.getEmpId())) ) && 
											!managerObj.getEmpId().equals(teamMemberObj.getEmpId())
											&& ( (deptsObj.isEmpty()) || (!deptsObj.isEmpty() && 
													!deptsObj.stream().anyMatch(o -> teamMemberObj.getEmpId().equals(o.getHodId()))) )) {
										
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
													newActivity.setDeptId(activityObject.getDeptId());
													newActivity.getCommonProperty().setCreatedBy(4l);

													newActivityCreated = activitiesRepository.save(newActivity);
												}
											}
										}
									}
								}
								if(newActivityCreated != null) {
									response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
									response.setServiceResponse("Project & Team created successfully.");
								}else {
									response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
									response.setServiceResponse("Project & Team created successfully, but default activities are not mapped");
								}
							}else {
								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
								response.setServiceResponse("Unable to create new team.");
							}
						});
					}else {
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
		try {
			
			Project checkProjectName = projectRepository.findByProjectName(projectDto.getProjectName());
			
			if(checkProjectName != null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project Name already exist.");
				System.out.println(" Project  exist"+checkProjectName);
			}else if(checkProjectName == null){
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
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
