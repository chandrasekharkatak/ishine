package com.apmosys.employeeportal.service;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Query;
import org.hibernate.Session;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.ActivityDTO;
import com.apmosys.employeeportal.dto.ActivityTemplateDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.EmployeeTeamMapDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.TeamDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.model.Activity;
import com.apmosys.employeeportal.model.ActivityTemplate;
import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.ClientLocation;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeLeave;
import com.apmosys.employeeportal.model.EmployeeLeavesMap;
import com.apmosys.employeeportal.model.EmployeeTeamMap;
import com.apmosys.employeeportal.model.LeaveBalanceLog;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectDepartmentMap;
import com.apmosys.employeeportal.model.RoleFeatureMap;
import com.apmosys.employeeportal.model.Team;
import com.apmosys.employeeportal.model.Timesheet;
import com.apmosys.employeeportal.repository.ActivitiesRepository;
import com.apmosys.employeeportal.repository.ActivityTemplateRepository;
import com.apmosys.employeeportal.repository.ClientLocationRepository;
import com.apmosys.employeeportal.repository.ClientsRepository;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeLeavesMapRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.LeaveBalanceLogRepository;
import com.apmosys.employeeportal.repository.ProjectDepartmentMapRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.TeamRepository;
import com.apmosys.employeeportal.repository.TimesheetsRepository;
import com.apmosys.employeeportal.utility.LeaveLogMessage;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class TeamsService {

	@Autowired
	ProjectRepository projectRepository;

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

	public ServiceResponse getAllProjectListByProjectManagerId(TimesheetDTO timesheetDTO) {
		ServiceResponse response = new ServiceResponse();

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
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Projects found.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	@Transactional
	public ServiceResponse createTeam(TeamDTO teamDTO) {
		ServiceResponse response = new ServiceResponse();
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
			newTeam.getCommonProperty().setCreatedBy(teamDTO.getCreatedBy());
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
					} else {
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
						}else {
							String message = employeeTeamMapRepository.saveAll(mapList).isEmpty()
									? "Team created but no team members added."
									: "Team created successfully. But unable to map activities";
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse(message);
						}
					}

				}, () -> {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse(
							"Team created but no team members added.Reason: Team members list was null");
				});

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Team creation failed.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getAllTeamsByProjectId(TeamDTO teamDTO) {
		ServiceResponse response = new ServiceResponse();
		try {

			List<Object[]> objectList = teamRepository.projectTeamsByProjectId(teamDTO.getProjectId());

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No teams found. Teams list is empty");
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
		}
		return response;
	}

	public ServiceResponse deleteTeam(TeamDTO teamDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			Optional<Team> team = teamRepository.findById(teamDTO.getTeamId());
			Long teamCount = employeeTeamMapRepository.countByTeamId(teamDTO.getTeamId());
			if(teamCount == 0) {
				
				teamRepository.deleteById(teamDTO.getTeamId());
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Team deleted successfully.");
			}else {
				team.ifPresent((teamFound) -> {
					teamFound.setIsActive("N");
					teamRepository.save(teamFound);
					});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Team Status changed to InActive");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getTeamMembersByTeamId(TeamDTO teamDTO) {
		ServiceResponse response = new ServiceResponse();
		try {

			List<Object[]> objectList = employeeTeamMapRepository.getTeamMembersByTeamId(teamDTO.getTeamId());

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No team members found. Team members list is empty");
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
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No team members found. Team members list is null");
			});

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse updateTeam(TeamDTO teamDTO) {
		ServiceResponse response = new ServiceResponse();
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

					} else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Team updation failed.");
					}

				}, () -> {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Team not found.");
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
					} else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Team updation failed.");
					}

				}, () -> {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Team not found.");
				});
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
	public ServiceResponse getAllMyTeamsByEmpId(EmployeeDTO employeeDTO) {
		ServiceResponse response = new ServiceResponse();
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
		}
		return response;
	}
	
	
	 List<Object[]> getAllMyTeamsByCustomQuery(String customQuery) {
			try {
				Session session = entityManager.unwrap(Session.class);
				
				try {
					
					String q="SELECT distinctrow t.project_id, p.project_name, t.team_id, t.team_name, t.team_lead_id, t.team_lead_name, p.project_manager_id , pm.name as projectManager,\n"
							+ "p.department_name,e1.name as teamCreatedByName,t.created_on, t.is_active, jr.dept_id as teamLeadDept, t.dept_ids \n"
							+ "FROM teams t \n"
							+ "LEFT JOIN employee e1 ON e1.emp_id = t.created_by \n"
							+ "LEFT JOIN projects p ON p.project_id = t.project_id \n"
							+ "LEFT JOIN employee pm ON pm.emp_id = p.project_manager_id  \n"
							+ "LEFT JOIN employee tl ON tl.emp_id = t.team_lead_id \n"
							+ "LEFT JOIN job_role jr ON jr.job_role_id = tl.job_role_id \n"
							+ "WHERE "+ customQuery +" ORDER BY p.project_name, t.team_name";
					
					System.out.println("Query :"+ q);
					Query query = session.createSQLQuery(q);
					System.out.println(query);
					System.out.println("Result List : "+ query.getResultList());
					return query.getResultList();
					
				}catch(Exception e) {
					e.printStackTrace();
				}finally {
					if(session!=null && session.isOpen()) {
						session.close();
					}
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
			return new ArrayList<>();
		}
	 
		public ServiceResponse getMappedActivityPreview(TeamDTO teamDTO) {
			ServiceResponse response = new ServiceResponse();
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
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No activity found.");
				}
				
			}catch(Exception e) {
				e.printStackTrace();
				response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
				response.setServiceResponse("Something Went Wrong.");
				response.setServiceError(e.getMessage());
			}
			return response;
		}
		
		public ServiceResponse getMappedActivityInUpdateTeam(TeamDTO teamDTO) {
			ServiceResponse response = new ServiceResponse();
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
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Activity List is empty");
				}
				
			}catch(Exception e) {
				e.printStackTrace();
				response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
				response.setServiceResponse("Something Went Wrong.");
				response.setServiceError(e.getMessage());
			}
			return response;
		}
	 
	 
//	MyTeam Servcie
	
	public ServiceResponse getAllTeamView(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> list = employeeRepository.getAllTeamView(employeedto.getEmpId());
			List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();
			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No teams found");
			} else {

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
					dtoList.add(dto);
				});

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getAllTeamMemberView(EmployeeDTO employeedto) {
		ServiceResponse response = new ServiceResponse();
		try {

			List<Object[]> list = employeeRepository.getAllTeamMemberView(employeedto.getEmpId());
			List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();
			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No teams found");
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
					
					dtoList.add(dto);
					
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getAllTeamLeaveHistoryView(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			LocalDate start = LocalDate.parse(leaveDTO.getFromDate());

			LocalDate end = LocalDate.parse(leaveDTO.getToDate());

			List<Object[]> list = employeeLeaveRepository.getAllTeamLeaveHistoryView(leaveDTO.getEmpId(),start,end);
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No teams leave history found");
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
					dto.setLeaveId(object[9] != null ? Long.parseLong(object[9].toString()) : null);
					dto.setRemark(object[10] != null ? object[10].toString() : null);
					dtoList.add(dto);					
					});

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getAllTeamCompOffHistoryView(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			LocalDate start = LocalDate.parse(leaveDTO.getFromDate());

			LocalDate end = LocalDate.parse(leaveDTO.getToDate());

			List<Object[]> list = employeeLeaveRepository.getAllTeamCompOffHistoryView(leaveDTO.getEmpId(),start,end);
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No teams leave history found");
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
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
	public ServiceResponse checkTeamName(TeamDTO teamdto) {
		
		ServiceResponse response = new ServiceResponse();
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
						}
						System.out.println("   checkTeamNameByName - "+checkTeamNameByName);
					}
				}
			}else {
				
				Team checkTeamNameByName=teamRepository.findByTeamNameAndProjectId(teamdto.getTeamName(), teamdto.getProjectId());
				if(checkTeamNameByName != null) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Team Name already exist!");
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				}
				System.out.println("   checkTeamNameByName : "+checkTeamNameByName);
			}
			 
		}catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
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
					newTeam.getCommonProperty().setCreatedBy(team.getCreatedBy());

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

	public ServiceResponse getDepartmentLeaveHistory(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			LocalDate start = LocalDate.parse(leaveDTO.getFromDate());
			LocalDate end = LocalDate.parse(leaveDTO.getToDate());
			List<Object[]> objectList = employeeLeaveRepository.getDepartmentLeaveHistory(leaveDTO.getDeptId(),start,end);
			
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			if (objectList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No department leave history found");
			} else {

				objectList.forEach((object) -> {
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
					dto.setLeaveId(object[9] != null ? Long.parseLong(object[9].toString()) : null);
					dto.setRemark(object[10] != null ? object[10].toString() : null);
					dtoList.add(dto);					
					});

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
			}
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

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
					
					
					//Manage Employee leave balance
					
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
							}else {
								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
								response.setServiceResponse("Unable to revoke leave.");
							}
						}
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Employee Leave Balance details not found.");
					}
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Unable to revoke Leave Application.");
				}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave Application not found.");
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
