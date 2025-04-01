package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException.InternalServerError;
import org.springframework.web.client.RestTemplate;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.EmployeeDetailsForTeamMemberDTO;
import com.apmosys.employeeportal.dto.EmployeeTeamMapDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.PoProjectSyncDTO;
import com.apmosys.employeeportal.dto.PoTeamDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.ProjectInfoDTO;
import com.apmosys.employeeportal.dto.ResourceManagementDTO;
import com.apmosys.employeeportal.dto.TeamDTO;
import com.apmosys.employeeportal.dto.TeamMemberDTO;
import com.apmosys.employeeportal.model.Activity;
import com.apmosys.employeeportal.model.ActivityTemplate;
import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.ClientLocation;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeTeamMap;
import com.apmosys.employeeportal.model.JobRole;
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
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.ProjectDepartmentMapRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.TeamRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;
import com.apmosys.employeeportal.model.Designation;

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
	
	@Autowired
	private JobRoleRepository jobRoleRepository;
	
	@PersistenceContext
	private EntityManager entityManager;
	 
	
	@Value("${rmg.mail}")
	private String rmgMail;
	
	@Value("${dept_head.mail}")
	private String deptHodMail;
	
	@Value("${poPortal.api.syncProject}")
	private String syncProjectApi;
	
	@Value("${rmg.project.approval.link}")
	private String rmgProjectApprovalLink;

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
		    if (resourceManagementDTO.getProjectType().equals("Internal")) {
		        projObj = projectRepository.findByProjectId(resourceManagementDTO.getProjectId());
		    } else {
		        projObj = projectRepository.findByPoProjectId(resourceManagementDTO.getId());
		    }

		    Project projectObj = projObj;
		    Employee employeeObj = employeeRepository.findByEmpId(resourceManagementDTO.getCreatedBy());

		    List<Long> allTeam = new ArrayList<>();

		    if (projectObj != null) {

		        List<Team> findTeamByProject = null;
		        List<Team> addTeamList = new ArrayList<Team>();
		        List<Team> allExistTeam = teamRepository.findByProjectId(resourceManagementDTO.getProjectId());
		        if (resourceManagementDTO.getProjectType().equals("Internal"))
		            findTeamByProject = teamRepository.findTeamByProjectId(projectObj.getProjectId());
		        else
		            findTeamByProject = teamRepository.findByProjectId(projectObj.getProjectId());

		        addTeamList.addAll(findTeamByProject);

		        List<Long> teamId = allExistTeam.stream().map(Team::getTeamId).collect(Collectors.toList());
		        Set<Team> newlyAddedTeam = addTeamList.stream().filter(dto -> !teamId.contains(dto.getTeamId())).collect(Collectors.toSet());

		        // Logic to handle existing teams and newly added teams...

		        Long employeementID = Long.parseLong(resourceManagementDTO.getProjectManager().split("-")[1]);
		        Long projManagerId = null;
		        Employee employee = employeeRepository.findByEmployeementId(employeementID);
		        if (employee != null) {
		            projManagerId = employee.getEmpId();
		        }
		        projectObj.setIsDraftProject("false");

		        projectObj.setProjectName(resourceManagementDTO.getName());
		        projectObj.setProjectManagerId(projManagerId);
		        
		        projectObj.setPoNo(resourceManagementDTO.getPoNo());
		        projectObj.setPoStartDate(resourceManagementDTO.getStartDate());
		        projectObj.setPoEndDate(resourceManagementDTO.getEndDate());
		        projectObj.setPoProjectType(resourceManagementDTO.getProjectType());

		        projectObj.setApmosysRM(resourceManagementDTO.getApmosysRM());	
		        projectObj.setIsRenewable(resourceManagementDTO.getIsRenewable());
		        projectObj.setClientRM(resourceManagementDTO.getClientRM());
		        
		        Project projectDbResponse = projectRepository.save(projectObj);
				
				resourceManagementDTO.getTeamList().forEach((teamObj) -> {
					//Check if team present
					 Team teamPresent;
					    if (teamObj.getTeamId() != null) {
					        teamPresent = teamRepository.findByTeamIdAndProjectId(teamObj.getTeamId(), projectObj.getProjectId());
					    } else {
					        teamPresent = teamRepository.findByTeamNameAndProjectId(teamObj.getTeamName(), projectObj.getProjectId());
					    }

					    // DeptIds
					    StringBuilder deptList = new StringBuilder("");
					    for (String department : teamObj.getDepartmentList()) {
					        deptList.append(department).append(",");
					    }

					    // teamLead
					    Long teamLeadId = null;
					    String teamLeadName = null;
					    for (TeamMemberDTO teamMember : teamObj.getTeamMemberList()) {
					        if ((teamMember.getIsTeamLead() != null) && (teamMember.getIsTeamLead().equals("true"))) {
					            teamLeadId = teamMember.getEmpId();
					            teamLeadName = teamMember.getName();
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
					        teamPresent.getCommonProperty().setUpdatedBy(resourceManagementDTO.getCreatedBy());

					        Team teamDbResponse = teamRepository.save(teamPresent);

					        if (teamDbResponse != null) {
					            List<EmployeeTeamMap> alreadyMappedMember = employeeTeamMapRepository.findByTeamId(teamDbResponse.getTeamId());
					            List<TeamMemberDTO> newTeamMember = teamObj.getTeamMemberList();
					            List<Long> memberToBeRemoved = new ArrayList<Long>();

					            // Update teamMember mapping
					            List<EmployeeTeamMap> updateMemberList = new ArrayList<EmployeeTeamMap>();
					            for (EmployeeTeamMap presentMember : alreadyMappedMember) {
					                for (TeamMemberDTO newMember : newTeamMember) {
					                    // Update teamMember mapping
					                    if (presentMember.getEmpId().equals(newMember.getEmpId())) {
					                        EmployeeTeamMap updateMember = employeeTeamMapRepository
					                                .findByEmpIdAndTeamIdAndActive(presentMember.getEmpId(), teamDbResponse.getTeamId(), 1L);

					                        if (updateMember != null) {
					                            StringBuilder employeeRole = new StringBuilder("");
					                            for (String empRole : newMember.getEmployeeRole()) {
					                                employeeRole.append(empRole).append(",");
					                            }

					                            updateMember.setEmpId(newMember.getEmpId());
					                            updateMember.setActive(1L);
					                            updateMember.setEmployeeRole(employeeRole.toString());
					                            updateMember.setTeamId(teamDbResponse.getTeamId());

					                            updateMemberList.add(updateMember);
					                        }
					                    }
					                }
					            }
					            List<EmployeeTeamMap> updateMemberDbResponse = employeeTeamMapRepository.saveAll(updateMemberList);

					            // Add teamMember mapping
					            newTeamMember.forEach((newMember) -> {
					                List<EmployeeTeamMap> presentMember = employeeTeamMapRepository
					                        .findFirstByEmpIdAndTeamIdAndActive(newMember.getEmpId(), teamDbResponse.getTeamId());

					                List<EmployeeTeamMap> mapList = new ArrayList<EmployeeTeamMap>();
					                EmployeeTeamMap empTeamMap = new EmployeeTeamMap();

					                if (presentMember.isEmpty()) {
					                    // TeamLead
					                    if ((newMember.getIsTeamLead() != null) && (newMember.getIsTeamLead().equals("true"))) {
					                        empTeamMap.setEmpId(newMember.getEmpId());
//					                        empTeamMap.setActive(1L);
					                        empTeamMap.setEmployeeRole("TeamLead");
					                        empTeamMap.setTeamId(teamDbResponse.getTeamId());
					                        mapList.add(empTeamMap);
					                    } else {
					                        StringBuilder employeeRole = new StringBuilder("");
					                        for (String empRole : newMember.getEmployeeRole()) {
					                            employeeRole.append(empRole).append(",");
					                        }

					                        empTeamMap.setEmpId(newMember.getEmpId());
//					                        empTeamMap.setActive(1L);
					                        empTeamMap.setEmployeeRole(employeeRole.toString());
					                        empTeamMap.setTeamId(teamDbResponse.getTeamId());
					                        mapList.add(empTeamMap);
					                    }
					                    List<EmployeeTeamMap> teamMapDbResponse = employeeTeamMapRepository.saveAll(mapList);

					                    // Set active value as 2 for new added team members
					                    teamMapDbResponse.forEach((newAddedMember) -> {
					                    	if(resourceManagementDTO.getIsHOD().equals("true"))
					                        newAddedMember.setActive(1L);
					                    	else
					                    		newAddedMember.setActive(2L);
					                    	
					                    	  System.out.println(" newTeamMember   "+newAddedMember);
//					                    	// Add default activity for the new team member
//						                    	// Add default activity for the new team member
						                    	  if (newAddedMember.getEmpId() != null) {
						                    	      // Fetch the employee's job role and department ID
						                    	      List<Object[]> employeeDetails = employeeRepository.getEmployeeByEmpId(newAddedMember.getEmpId());
						                    	      System.out.println("New member added: " + employeeDetails.get(0));

						                    	      if (employeeDetails != null && !employeeDetails.isEmpty()) {
						                    	          // Assuming the first row contains the desired details
						                    	          Object[] employeeDetailRow = employeeDetails.get(0);
						                    	          String departmentId = employeeDetailRow[46] != null ? employeeDetailRow[46].toString() : null;
						                    	          System.out.println("Department ID: " + departmentId);

						                    	          if (departmentId != null) {
						                    	              // Split employeeRole into a list of strings
						                    	              List<String> employeeRoles = Arrays.asList(newAddedMember.getEmployeeRole().split(","));

						                    	              for (String role : employeeRoles) {
						                    	                  role = role.trim(); // Trim whitespace around each role
						                    	                  System.out.println("Processing role: " + role);

						                    	                  // Check if activities exist for the employee's department and role
						                    	                  List<Activity> existingActivities = activitiesRepository.findByDeptIdsAndEmployeeRoleAndTeamId(
						                    	                      departmentId,
						                    	                      role,
						                    	                      teamDbResponse.getTeamId()
						                    	                  );

						                    	                  if (existingActivities.isEmpty()) {
						                    	                      System.out.println("No activities exist for Dept ID: " + departmentId + ", Role: " + role);

						                    	                      // Fetch the activity templates for the given department and role
						                    	                      List<ActivityTemplate> activityTemplateList = activityTemplateRepository.getByDeptIdAndEmployeeRoleType(
						                    	                          Long.parseLong(departmentId),
						                    	                          role
						                    	                      );
						                    	                      if (!activityTemplateList.isEmpty()) {
						                    	                          for (ActivityTemplate activityTemplate : activityTemplateList) {
						                    	                              // Create and save new activities
						                    	                              Activity newActivity = new Activity();
						                    	                              newActivity.setActivity(activityTemplate.getTemplateActivity());
						                    	                              newActivity.setTeamId(teamDbResponse.getTeamId());
						                    	                              newActivity.setEmployeeRole(activityTemplate.getEmployeeRole());
						                    	                              newActivity.setDeptIds(activityTemplate.getDeptId().toString());
						                    	                              newActivity.getCommonProperty().setCreatedBy(resourceManagementDTO.getCreatedBy());
						                    	                              activitiesRepository.save(newActivity);

						                    	                              System.out.println("New activity created: " + activityTemplate.getTemplateActivity());
						                    	                          }
						                    	                      } else {
						                    	                          System.out.println("No activity templates found for Dept ID: " + departmentId + ", Role: " + role);
						                    	                      }
						                    	                  } else {
						                    	                      System.out.println("Activities already exist for Dept ID: " + departmentId + ", Role: " + role);
						                    	                  }
						                    	              }
						                    	          } else {
						                    	              System.out.println("Department ID is null for Employee ID: " + newAddedMember.getEmpId());
						                    	          }
						                    	      } else {
						                    	          System.out.println("No employee details found for Employee ID: " + newAddedMember.getEmpId());
						                    	      }
						                    	  }


//					                    	  find added employee's email
					                    	  Employee  findEmp = employeeRepository.findByEmpId(newAddedMember.getEmpId());
					                    	  Employee managerEmail = employeeRepository.findByEmpId(findEmp.getManagerId());
					                    	  
					                    	  Project projectFind = null;
					              		    if (resourceManagementDTO.getProjectType().equals("Internal")) {
					              		    	projectFind = projectRepository.findByProjectId(resourceManagementDTO.getProjectId());
					              		    } else {
					              		    	projectFind = projectRepository.findByPoProjectId(resourceManagementDTO.getId());
					              		    }
					                    	  
					                    	  try {
												mailService.sendMailWithCC("tmp@gmail.com", rmgMail, "Regarding Resource mapped to new Project", "Dear "
														+ findEmp.getName()+"<br>"
														+ "You have been mapped to client name - "+resourceManagementDTO.getClientName()+" under the project "+projectFind.getProjectName()+"<br>"
																+ "<br><br>"
																+ "Sincerely,"+"<br>"
																+ "Team RMG - ApMoSys Technologies"
														);
											} catch (AddressException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											} catch (MessagingException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
					                    	  
					                    });
					                    employeeTeamMapRepository.saveAll(teamMapDbResponse);
					                }
					            });
					            
					            // Inactivate team member
					            List<EmployeeTeamMap> alreadyExistMember = new ArrayList<>();
					            if (!newTeamMember.isEmpty()) {
					                for (TeamMemberDTO obj : newTeamMember) {
					                    memberToBeRemoved.add(obj.getEmpId());
					                }
					                alreadyExistMember = employeeTeamMapRepository.findByEmpIdNotInAndTeamId(memberToBeRemoved, teamDbResponse.getTeamId());
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
					                    member.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
					                    inActiveMember.add(member);
					                    
//					                    mail for inactive employee
					                    
					                    try {
											mailService.sendMail(rmgMail,"Regarding Resource removed from Project ", "Dear "
													+ emp.getName()+"<br>"
													+ "You have been removed from project "+findProject.getProjectName()+ "under the team - "+findTeam.getTeamName()+"<br>"
															+ "<br><br>"
															+ "Sincerely,"+"<br>"
															+ "Team RMG - ApMoSys Technologies"
													);
										} catch (AddressException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (MessagingException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
					                });
					                List<EmployeeTeamMap> inActiveDbResponse = employeeTeamMapRepository.saveAll(inActiveMember);
					            }

					            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					            response.setServiceResponse("Team updated successfully.");
					            apiLogInfo.setApiResponse("Team Updated!");
					            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					        }
					    }else {
					    	// Add Team
					        Team newTeamObj = new Team();
					        newTeamObj.setIsActive("Y");
					        newTeamObj.setProjectId(projectObj.getProjectId());
					        newTeamObj.setTeamLeadId(teamLeadId);
					        newTeamObj.setTeamName(teamObj.getTeamName());
					        newTeamObj.setTeamLeadName(teamLeadName);
					        newTeamObj.setDeptIds(deptList.toString());
					        newTeamObj.getCommonProperty().setCreatedBy(resourceManagementDTO.getCreatedBy());
					        Team teamDbResponse = teamRepository.save(newTeamObj);

					        if (teamDbResponse != null) {
					            allTeam.add(teamDbResponse.getTeamId());

					            List<EmployeeTeamMap> mapList = new ArrayList<EmployeeTeamMap>();
					            // Add team member in team

					            for (TeamMemberDTO teamMember : teamObj.getTeamMemberList()) {
					                EmployeeTeamMap newEmpTeamMap = new EmployeeTeamMap();
					                
					                // TeamLead
					                if ((teamMember.getIsTeamLead() != null) && (teamMember.getIsTeamLead().equals("true"))) {
					                    newEmpTeamMap.setEmpId(teamMember.getEmpId());
					                    newEmpTeamMap.setActive(1l);
					                    newEmpTeamMap.setEmployeeRole("TeamLead");
					                    newEmpTeamMap.setTeamId(teamDbResponse.getTeamId());
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
					                    mapList.add(newEmpTeamMap);
					                }
					            }
//					            mail for create Team
					            
					            try {
						            mailService.sendMail(rmgMail,
						                    "Regarding Resource management",
						                    "Dear RMG Team ," + "<br>"
						                            + "<br>"
						                            + "The team has been created and the following reources are mapped to this team -> : " + teamDbResponse.getTeamName()+"<br>"
						                            		+ "<br><br>"
															+ "Sincerely,"+"<br>"
															+ "Team RMG - ApMoSys Technologies"
															+ "<br>"
						                            +generateHtmlTable(teamObj.getTeamMemberList()));                             
						        } catch (Exception e) {
						            e.printStackTrace();
						        }
					            
					            
					            List<EmployeeTeamMap> teamMemberDbResponse = employeeTeamMapRepository.saveAll(mapList);

					            // Add default activity
					            Activity newActivityCreated = null;
					            if (!teamMemberDbResponse.isEmpty()) {
					                for (String department : teamObj.getDepartmentList()) {
					                    List<ActivityTemplate> activityTemplate = activityTemplateRepository.getByDeptId(Long.parseLong(department));
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
					        } else {
					            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					            response.setServiceResponse("Unable to create new team.");
					            apiLogInfo.setApiResponse("Unable to Create new team");
					            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					        }
					    }
				});
				
				//InActivate Team
				
				List<Team> alreadyExistTeam = teamRepository.findByTeamIdNotInAndProjectId(allTeam, projectObj.getProjectId());
			    if (!alreadyExistTeam.isEmpty()) {
			        List<Team> teamToBeRemoved = new ArrayList<>();

			        alreadyExistTeam.forEach((team) -> {
			            team.setIsActive("N");
			            team.getCommonProperty().setUpdatedBy(resourceManagementDTO.getCreatedBy());
			            
			            teamToBeRemoved.add(team);			            
			            
			        });
			        List<Team> teamToBeRemoveResponse = teamRepository.saveAll(teamToBeRemoved);
			    }
				
				//Send mail to RMG: if HOD has updated project/Team
//			    if (resourceManagementDTO.getIsHOD().equals("true") && !resourceManagementDTO.getProjectType().equals("Internal")) {
//			        try {
//			            mailService.sendMailWithCC("demo@gmail.com", "sakti.das@apmosys.com",
//			                    "Regarding Resource management",
//			                    "Dear RMG Team ," + "<br>"
//			                            + "<br>"
//			                            + employeeObj.getName() + " project update hua hua  has updated the project : " + resourceManagementDTO.getName());
//			        } catch (Exception e) {
//			            e.printStackTrace();
//			        }
//			    }
				
				// Send Project/Team detail JSON to PoPotal
				
//			    if (!resourceManagementDTO.getProjectType().equals("Internal")) {
//			    	resourceManagementDTO.setPoProjectId(resourceManagementDTO.getId());
//			        ServiceResponse poPortalResponse = sendProjectInfoToPoPortal(resourceManagementDTO);
//
//			        if (poPortalResponse.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS)) {
//			            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//			            response.setServiceResponse("Project updated successfully");
//			            apiLogInfo.setApiResponse("Project updated successfully");
//			            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//			        } else {
//			            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//			            response.setServiceResponse("Project & Team created successfully, but unable to sync with PoPortal : " + poPortalResponse.getServiceResponse());
//			            apiLogInfo.setApiResponse("Project & Team created successfully, but unable to sync with PoPortal");
//			            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			        }
//			    } else {
			    	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			        response.setServiceResponse("Project updated successfully");
			        apiLogInfo.setApiResponse("Project updated successfully");
			        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//			    }
			} else {
				Integer clientId = null;
			    Optional<Client> clientObj = clientsRepository.findByClientName(resourceManagementDTO.getClientName());
			    if (!clientObj.isEmpty()) {
			        Client clientPresent = clientObj.get();
			        clientId = clientPresent.getClientId();
			    } else {
			        // Add Client & Client Location
			        Client newClient = new Client();
			        newClient.setClientName(resourceManagementDTO.getClientName());
			        Client clientDbResponse = clientsRepository.save(newClient);

			        if (clientDbResponse != null) {
			            clientId = clientDbResponse.getClientId();
			            List<ClientLocation> locations = new ArrayList<>();

			            for (String clientLocation : resourceManagementDTO.getClientLocation()) {
			                ClientLocation newClientLocation = new ClientLocation();
			                newClientLocation.setClientId(clientDbResponse.getClientId());
			                newClientLocation.setClientLocation(clientLocation);
			                locations.add(newClientLocation);
			            }

			            // Add WFH location
			            boolean contains = Arrays.stream(resourceManagementDTO.getClientLocation()).anyMatch("WFH"::equals);
			            if (!contains) {
			                ClientLocation newClientLocation = new ClientLocation();
			                newClientLocation.setClientId(clientDbResponse.getClientId());
			                newClientLocation.setClientLocation("WFH");
			                locations.add(newClientLocation);
			            }

			            List<ClientLocation> clientLocationDbResponse = clientLocationRepository.saveAll(locations);

			            if (!clientLocationDbResponse.isEmpty()) {
			                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			                response.setServiceResponse("Client Location added");
			                apiLogInfo.setApiResponse("Client Location added");
			                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			            } else {
			                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			                response.setServiceResponse("Failed to add client Location");
			                apiLogInfo.setApiResponse("Failed to add client Location");
			                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			                return response;
			            }
			        } else {
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
			    if (employee != null) {
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
			    newProject.setPoProjectType(resourceManagementDTO.getProjectType());
			    newProject.setPoNo(resourceManagementDTO.getPoNo());
			    newProject.setPoStartDate(resourceManagementDTO.getStartDate());
			    newProject.setPoEndDate(resourceManagementDTO.getEndDate());

			    newProject.setApmosysRM(resourceManagementDTO.getApmosysRM());	
			    newProject.setIsRenewable(resourceManagementDTO.getIsRenewable());
			    newProject.setClientRM(resourceManagementDTO.getClientRM());
				
				  	if (resourceManagementDTO.getIsHOD().equals("true")) {
				 	newProject.setIsDraftProject("false"); 
				 	} else {
				 	newProject.setIsDraftProject("true"); 
				 	}
				 
			    System.out.println(" ANurag     ::   "+projectObj);

			    newProject.setCreatedBy(resourceManagementDTO.getCreatedBy());

			    Project projectDbResponse = projectRepository.save(newProject);
				
			    if (projectDbResponse != null) {
			        // Add project Department Mapping
			        for (String department : resourceManagementDTO.getDepartment()) {
			            Department departmentObj = departmentRepository.findByName(department);
			            if (departmentObj != null) {
			                ProjectDepartmentMap projectDeptMapObj = projectDepartmentMapRepository.
			                        findByProjectIdAndDeptId(projectDbResponse.getProjectId(), departmentObj.getDeptId());
			                if (projectDeptMapObj == null) {
			                    // Add department
			                    ProjectDepartmentMap projectDeptMap = new ProjectDepartmentMap();
			                    projectDeptMap.setProjectId(projectDbResponse.getProjectId());
			                    projectDeptMap.setDeptId(departmentObj.getDeptId());
			                    ProjectDepartmentMap projDeptMapDbResponse = projectDepartmentMapRepository.save(projectDeptMap);
			                }
			            }
			        }

					// Add Team

			        resourceManagementDTO.getTeamList().forEach((teamObj) -> {
			            // Create a new team
			            StringBuilder deptList = new StringBuilder("");
			            for (String department : teamObj.getDepartmentList()) {
			                deptList.append(department).append(",");
			            }
			            // TeamLead
			            Long teamLeadId = null;
			            String teamLeadName = null;
			            for (TeamMemberDTO teamMember : teamObj.getTeamMemberList()) {
			                if ((teamMember.getIsTeamLead() != null) && (teamMember.getIsTeamLead().equals("true"))) {
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
						newTeamObj.setDeptIds(deptList.toString());
			            newTeamObj.getCommonProperty().setCreatedBy(resourceManagementDTO.getCreatedBy());
			            Team teamDbResponse = teamRepository.save(newTeamObj);
			            
			            StringBuilder emailBody = new StringBuilder();
			            emailBody.append("<html><body>") // Wrap email content inside HTML tags
			                     .append("Dear RMG,<br><br>")
			                     .append("The Team has been created with the team name - <b>").append(teamDbResponse.getTeamName()).append("</b><br><br>")
			                     .append("<table border='1' style='border-collapse: collapse; width: 100%;'>")
			                     .append("<tr>")
			                     .append("<th style='padding: 8px; text-align: left;'>Employment ID</th>")
			                     .append("<th style='padding: 8px; text-align: left;'>Employee Name</th>")
			                     .append("<th style='padding: 8px; text-align: left;'>Job Role</th>")
			                     .append("<th style='padding: 8px; text-align: left;'>Department</th>")
			                     .append("<th style='padding: 8px; text-align: left;'>Start Date</th>")
			                     .append("<th style='padding: 8px; text-align: left;'>Employee Role</th>")
			                     .append("</tr>");

			            if (teamDbResponse != null) {
			                List<EmployeeTeamMap> mapList = new ArrayList<EmployeeTeamMap>();

			                // Add team member in the team
			                for (TeamMemberDTO teamMember : teamObj.getTeamMemberList()) {
			                    EmployeeTeamMap newEmpTeamMap = new EmployeeTeamMap();
				                EmployeeDetailsForTeamMemberDTO employeeDetails = employeeRepository.getEmployeeDetailsForTeam(teamMember.getEmpId());

			                    // TeamLead
			                    if ((teamMember.getIsTeamLead() != null) && (teamMember.getIsTeamLead().equals("true"))) {
			                        newEmpTeamMap.setEmpId(teamMember.getEmpId());
			                        newEmpTeamMap.setActive(2L); // Set Active to 2 for TeamLead
			                        newEmpTeamMap.setEmployeeRole("TeamLead");
			                        newEmpTeamMap.setTeamId(teamDbResponse.getTeamId());
			                        mapList.add(newEmpTeamMap);
			                    } else {
			                        StringBuilder employeeRole = new StringBuilder("");
			                        for (String empRole : teamMember.getEmployeeRole()) {
			                            employeeRole.append(empRole).append(",");
			                        }

			                        newEmpTeamMap.setEmpId(teamMember.getEmpId());
			                        newEmpTeamMap.setActive(2L); // Set Active to 2 for Team Member
			                        newEmpTeamMap.setEmployeeRole(employeeRole.toString());
			                        newEmpTeamMap.setTeamId(teamDbResponse.getTeamId());
			                        mapList.add(newEmpTeamMap);
			                    }
			                    
			                    if (employeeDetails != null) {
			                        String employmentId = "A-" + employeeDetails.getEmployeementId();
			                        if ("true".equalsIgnoreCase(employeeDetails.getIsConsultant())) {
			                            employmentId = "CS-" + employeeDetails.getEmployeementId();
			                        }

			                        String employeeRole = teamMember.getIsTeamLead() != null && teamMember.getIsTeamLead().equalsIgnoreCase("true")
			                                            ? "TeamLead"
			                                            : String.join(",", teamMember.getEmployeeRole());

			                        emailBody.append("<tr>")
			                        .append("<td style='padding: 8px;'>").append(employmentId).append("</td>")
			                        .append("<td style='padding: 8px;'>").append(employeeDetails.getEmployeeName()).append("</td>")
			                        .append("<td style='padding: 8px;'>").append(employeeDetails.getJobRoleName()).append("</td>")
			                        .append("<td style='padding: 8px;'>").append(employeeDetails.getDeptName()).append("</td>")
			                        .append("<td style='padding: 8px;'>").append(employeeDetails.getStartDate()).append("</td>")
			                        .append("<td style='padding: 8px;'>").append(employeeRole).append("</td>")
			                        .append("</tr>");
			                    }
			                }
			                List<EmployeeTeamMap> teamMemberDbResponse = employeeTeamMapRepository.saveAll(mapList);

			                emailBody.append("</table><br><br>")
			                .append("Sincerely,<br>")
			                .append("<b>Team RMG - ApMoSys Technologies</b>")
			                .append("</body></html>");

			                try {
			                    mailService.sendMail(rmgMail, "Regarding Team Creation", emailBody.toString());
			                } catch ( MessagingException e) {
			                    e.printStackTrace();
			                }
			               

			                // Add default activity
			                Activity newActivityCreated = null;
			                if (!teamMemberDbResponse.isEmpty()) {
			                    for (String department : teamObj.getDepartmentList()) {
			                        List<ActivityTemplate> activityTemplate = activityTemplateRepository.getByDeptId(Long.parseLong(department));
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
			                    apiLogInfo.setApiResponse("Team created successfully");
			                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			                } else {
			                    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			                    response.setServiceResponse("Team created successfully, but default activities are not mapped");
			                    apiLogInfo.setApiResponse("Team created successfully, but default activities are not mapped");
			                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			                }
			            } else {
			                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			                response.setServiceResponse("Unable to create a new team.");
			                apiLogInfo.setApiResponse("Unable to create a new team.");
			                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			            }
			        });

			        // Send mail to RMG: if HOD/SuperAdmin has created project/Team
//			        if (resourceManagementDTO.getIsHOD().equals("true") && !resourceManagementDTO.getProjectType().equals("Internal")) {
//			            try {
//			                mailService.sendMailWithCC("demo@gmail.com","sakti.das@apmosys.com",
//			                        "Regarding Resource management",
//			                        "Dear RMG Team ," + "<br>"
//			                                + "<br>"
//			                                + employeeObj.getName() + " dusra wala call kiya hai team create pr project update ka line no 729 has created a project:  -> " + resourceManagementDTO.getName()+"under this team hai "+resourceManagementDTO.getTeamList().get(0).getTeamName());                                      
//			            } catch (Exception e) {
//			                e.printStackTrace();
//			            }
//			        }

			        // Send Project/Team detail JSON to PoPortal
//			        if (!resourceManagementDTO.getProjectType().equals("Internal")) {
//			            ServiceResponse poPortalResponse = sendProjectInfoToPoPortal(resourceManagementDTO);
//
//			            if (poPortalResponse.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS)) {
//			                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//			                response.setServiceResponse("Project updated successfully");
//			                apiLogInfo.setApiResponse("Project updated successfully");
//			                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//			            } else {
//			                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//			                response.setServiceResponse("Project & Team created successfully, but unable to sync with PoPortal : " + poPortalResponse.getServiceResponse());
//			                apiLogInfo.setApiResponse("Project & Team created successfully, but unable to sync with PoPortal");
//			                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			            }
//			        } else {
			            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			            response.setServiceResponse("Project updated successfully");
			            apiLogInfo.setApiResponse("Project updated successfully");
			            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//			        }
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
	

	//	public ServiceResponse createDraftProjectInfo(ResourceManagementDTO resourceManagementDTO) {
//		
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
//					                    		newAddedMember.setActive(1L);
//					                    	else
//					                    		newAddedMember.setActive(2L);
//					                    	
//					                    	  System.out.println(" newTeamMember   "+newAddedMember);
//					                    	  
//					                    	// Add default activity for the new team member
//					                    	// Add default activity for the new team member
//					                    	  if (newAddedMember.getEmpId() != null) {
//					                    	      // Fetch the employee's job role and department ID
//					                    	      List<Object[]> employeeDetails = employeeRepository.getEmployeeByEmpId(newAddedMember.getEmpId());
//					                    	      System.out.println("New member added: " + employeeDetails.get(0));
//
//					                    	      if (employeeDetails != null && !employeeDetails.isEmpty()) {
//					                    	          // Assuming the first row contains the desired details
//					                    	          Object[] employeeDetailRow = employeeDetails.get(0);
//					                    	          String departmentId = employeeDetailRow[46] != null ? employeeDetailRow[46].toString() : null;
//					                    	          System.out.println("Department ID: " + departmentId);
//
//					                    	          if (departmentId != null) {
//					                    	              // Split employeeRole into a list of strings
//					                    	              List<String> employeeRoles = Arrays.asList(newAddedMember.getEmployeeRole().split(","));
//
//					                    	              for (String role : employeeRoles) {
//					                    	                  role = role.trim(); // Trim whitespace around each role
//					                    	                  System.out.println("Processing role: " + role);
//
//					                    	                  // Check if activities exist for the employee's department and role
//					                    	                  List<Activity> existingActivities = activitiesRepository.findByDeptIdsAndEmployeeRoleAndTeamId(
//					                    	                      departmentId,
//					                    	                      role,
//					                    	                      teamDbResponse.getTeamId()
//					                    	                  );
//
//					                    	                  if (existingActivities.isEmpty()) {
//					                    	                      System.out.println("No activities exist for Dept ID: " + departmentId + ", Role: " + role);
//
//					                    	                      // Fetch the activity templates for the given department and role
//					                    	                      List<ActivityTemplate> activityTemplateList = activityTemplateRepository.getByDeptIdAndEmployeeRoleType(
//					                    	                          Long.parseLong(departmentId),
//					                    	                          role
//					                    	                      );
//					                    	                      if (!activityTemplateList.isEmpty()) {
//					                    	                          for (ActivityTemplate activityTemplate : activityTemplateList) {
//					                    	                              // Create and save new activities
//					                    	                              Activity newActivity = new Activity();
//					                    	                              newActivity.setActivity(activityTemplate.getTemplateActivity());
//					                    	                              newActivity.setTeamId(teamDbResponse.getTeamId());
//					                    	                              newActivity.setEmployeeRole(activityTemplate.getEmployeeRole());
//					                    	                              newActivity.setDeptIds(activityTemplate.getDeptId().toString());
//					                    	                              newActivity.getCommonProperty().setCreatedBy(resourceManagementDTO.getCreatedBy());
//					                    	                              activitiesRepository.save(newActivity);
//
//					                    	                              System.out.println("New activity created: " + activityTemplate.getTemplateActivity());
//					                    	                          }
//					                    	                      } else {
//					                    	                          System.out.println("No activity templates found for Dept ID: " + departmentId + ", Role: " + role);
//					                    	                      }
//					                    	                  } else {
//					                    	                      System.out.println("Activities already exist for Dept ID: " + departmentId + ", Role: " + role);
//					                    	                  }
//					                    	              }
//					                    	          } else {
//					                    	              System.out.println("Department ID is null for Employee ID: " + newAddedMember.getEmpId());
//					                    	          }
//					                    	      } else {
//					                    	          System.out.println("No employee details found for Employee ID: " + newAddedMember.getEmpId());
//					                    	      }
//					                    	  }
//
//
//
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
//												mailService.sendMailWithCC("dummy@gmail.com", rmgMail, "Regarding Resource mapped to new Project", "Dear "
//														+ findEmp.getName()+"<br>"
//														+ "You have been mapped to client name - "+resourceManagementDTO.getClientName()+" under the project "+projectFind.getProjectName()+"<br>"
//																+ "<br><br>"
//																+ "Sincerely,"+"<br>"
//																+ "Team RMG - ApMoSys Technologies"
//														);
//											} catch (AddressException e) {
//												e.printStackTrace();
//											}
//					                    	  
//					                    });
//					                    employeeTeamMapRepository.saveAll(teamMapDbResponse);
//					                    
//					                    
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
//					            	System.out.println("Team member for Activity"+teamMemberDbResponse);
//					                for (String department : teamObj.getDepartmentList()) {
//					                    List<ActivityTemplate> activityTemplate = activityTemplateRepository.getByDeptId(Long.parseLong(department));
//					                    System.out.println("Activity template fetched "+activityTemplate);
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
//					                            System.out.println("Activity created successfully with ID: {} for template ID: {}"+ newActivity.getActivityId()+ activityObject.getDeptId());
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
////			            mailService.sendMailWithCC("ar731829@gmail.com", "sakti.das@apmosys.com",
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
////			                mailService.sendMailWithCC("ar731829@gmail.com","sakti.das@apmosys.com",
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
        logBuilder.append("ProjectId : " + resourceManagementDTO.getProjectId()+ " ,ProjectName :" + resourceManagementDTO.getName() + " ,Id : " + resourceManagementDTO.getId());

		try {
			
			Project projectObj = null;
			if(resourceManagementDTO.getProjectType().equals("Internal")) {
				projectObj = projectRepository.findByProjectId(resourceManagementDTO.getProjectId());
			}else {
				projectObj = projectRepository.findByPoProjectId(resourceManagementDTO.getId());				
			}
			System.err.println(" projectObj     "+projectObj.getProjectId());
			if(projectObj != null) {
				List<Team> teamList = teamRepository.findByProjectIdAndIsActive(projectObj.getProjectId(), "Y");
				System.out.println(" teamList    ::   "+teamList.size());
				if(!teamList.isEmpty()) {
					List<TeamDTO> teamListdto = new ArrayList<TeamDTO>();
					teamList.forEach((object) -> {
						TeamDTO teamdto = new TeamDTO();
						
						//Get Team Info
						teamdto.setTeamId(object.getTeamId());
						teamdto.setTeamName(object.getTeamName());
						teamdto.setDepartmentList(object.getDeptIds().split(","));
						
						//Get teamMembers Info
						List<EmployeeTeamMap> empTeamMapping = employeeTeamMapRepository.findByTeamIdAndActive(object.getTeamId());
						if(!empTeamMapping.isEmpty()) {
							List<TeamMemberDTO> teamMember = new ArrayList<TeamMemberDTO>();
							empTeamMapping.forEach((teamMemberObj) -> {
								String employeeName = null;
								Employee empObj = employeeRepository.findByEmpId(teamMemberObj.getEmpId());
								// find department 
								JobRole findJobRole = jobRoleRepository.findByjobRoleId(empObj.getJobRoleId());
								Department findDepartment = departmentRepository.findByDeptId(findJobRole.getDeptId());							
								
								if(empObj != null && !"InActive".equalsIgnoreCase(empObj.getEmploymentstatus())) {
									employeeName = empObj.getName();
								}
								
								TeamMemberDTO teamMemberDTO = new TeamMemberDTO();
								
								if((object.getTeamLeadId() != null) && (object.getTeamLeadId().equals(teamMemberObj.getEmpId()))) {
									//Team Lead
									teamMemberDTO.setEmpId(teamMemberObj.getEmpId());
									teamMemberDTO.setName(employeeName);
									teamMemberDTO.setIsTeamLead("true");
									teamMemberDTO.setStartDate(teamMemberObj.getStartDate().toString());
									teamMemberDTO.setDepartmentName(findDepartment.getName());
									teamMemberDTO.setDepartmentId(findDepartment.getDeptId().toString());
									teamMemberDTO.setEmployeeRole(teamMemberObj.getEmployeeRole().split(","));
									teamMember.add(teamMemberDTO);
								}else {
									//Team Member
									teamMemberDTO.setEmpId(teamMemberObj.getEmpId());
									teamMemberDTO.setName(employeeName);
									teamMemberDTO.setStartDate(teamMemberObj.getStartDate().toString());
									teamMemberDTO.setDepartmentName(findDepartment.getName());
									teamMemberDTO.setDepartmentId(findDepartment.getDeptId().toString());
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
			
//			List<Project> allProjectList = projectRepository.findAll();
			List<Object[]> allProjectList = projectRepository.findAllProjectByIsDraftAndIsActive();
			
			allProjectList.forEach((obj)->{
				System.err.println(obj[1]+""+obj[7]);
			});
			
			
			if(!allProjectList.isEmpty()) {
				List<ResourceManagementDTO> dtoList = new ArrayList<ResourceManagementDTO>();
				List<EmployeeTeamMapDTO> dtoTeamList = new ArrayList<EmployeeTeamMapDTO>();
				
				
				
				allProjectList.forEach((object) -> {
					Integer projectId = object[0] != null ? Integer.parseInt(object[0].toString()) : null;
				
						

					ResourceManagementDTO projectDto = new ResourceManagementDTO();
					
					projectDto.setProjectId(object[0] != null ? Integer.parseInt(object[0].toString()) : null);
					projectDto.setProjectName(object[1] != null ? object[1].toString() : null);
					projectDto.setProjectManager(object[2] != null ? object[2].toString() : null);
					projectDto.setCreatedOn(object[4] != null ? object[4].toString() : null);
					projectDto.setClientName(object[5] != null ? object[5].toString() : null);			
					projectDto.setClientState(object[6] != null ? object[6].toString() : null);
					projectDto.setIsDraftProject(object[7] != null ? object[7].toString() : null);
					projectDto.setPoProjectId(object[8] != null ? Long.parseLong(object[8].toString()) : null);	
					projectDto.setIsActive(object[9] != null ? Long.parseLong(object[9].toString()): null);
					projectDto.setId(object[8] != null ? Long.parseLong(object[8].toString()) : null);				
							
				
					
					dtoList.add(projectDto);
						
			});
				
				System.out.println(" dtoList     ::::   "+dtoList);
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
	    	Project projectObj = null;
	    	if(resourceManagementDTO.getProjectType().equals("Internal"))
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
	                    ServiceResponse poPortalResponse = null;
	                    if (!resourceManagementDTO.getProjectType().equals("Internal")) {
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
	                                teamMembersToActivate.forEach(teamMember -> teamMember.setActive(1L));
	                                employeeTeamMapRepository.saveAll(teamMembersToActivate);
	                            }

	                        } else {
	                            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                            response.setServiceResponse(
	                                    "Project & Team created successfully, but unable to sync with PoPortal:1010 "
	                                            + poPortalResponse.getServiceResponse());
	                            apiLogInfo.setApiResponse(
	                                    "Project & Team created successfully, but unable to sync with PoPortal"
	                                            + poPortalResponse.getServiceResponse());
	                            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                        }
	                    }else {
	                    	// else added by anurag
	                    	 List<EmployeeTeamMap> teamMembersToActivate = employeeTeamMapRepository
	                                    .findByProjectIdAndActive(projectObj.getProjectId(), 2L);

	                            if (!teamMembersToActivate.isEmpty()) {
	                                teamMembersToActivate.forEach(teamMember -> teamMember.setActive(1L));
	                                employeeTeamMapRepository.saveAll(teamMembersToActivate);
	                            }
	                            
	                    	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		                    response.setServiceResponse("Project Approved but Internal project does not sync with PO portal.");
		                    apiLogInfo.setApiResponse("Project Approved but Internal project does not sync with PO portal");
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
        logBuilder.append("Project Id : "+ resourceManagementDTO.getId() + " ,EmployeeId :" + resourceManagementDTO.getEmpId());
		try {
			System.err.println("  resourceManagementDTO    \n\n\n\n\n\n"+resourceManagementDTO);
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
                        
                     // added by anurag for newly added user
                        List<Team> findTeamToBeDeleted = teamRepository.findTeamByProjectId(projectObj.getProjectId());
                        
                        for(Team findTeam : findTeamToBeDeleted) {
                            List<EmployeeTeamMap> teamMembersToActivate = employeeTeamMapRepository.findByTeamIdAndActive(findTeam.getTeamId());
                            List<EmployeeTeamMap> findActiveTeamMembers = employeeTeamMapRepository.findTeammembersByTeamIdAndStatus(findTeam.getTeamId());
                            
                            System.err.println(" teamMembersToActivate     size   "+teamMembersToActivate.size());
                            
                            
                            if(teamMembersToActivate.size()<=1) {
                            	teamMembersToActivate.forEach((object) ->{
                                	if(object.getActive() == 2) {
                                		List<Activity> findActivities = activitiesRepository.findByTeamId(findTeam.getTeamId());
                                		if(!findActivities.isEmpty()) {
                                			activitiesRepository.deleteAll();
                                			}
                                		employeeTeamMapRepository.deleteAllByTeamId(findTeam.getTeamId());
                                		teamRepository.delete(findTeam);                           	
                                		}
                                });
                            }else {
                            	teamMembersToActivate.forEach((teamObj)->{
                            		if(teamObj.getActive() == 1) {
                            			teamObj.setActive(1L);                    			
                            		}else {
                            			if(findActiveTeamMembers.isEmpty()) {
                            				List<Activity> findActivities = activitiesRepository.findByTeamId(findTeam.getTeamId());
                                    		if(!findActivities.isEmpty()) {
                                    			activitiesRepository.deleteAll();
                                    			}
                                    		employeeTeamMapRepository.deleteAllByTeamId(findTeam.getTeamId());
                                    		teamRepository.delete(findTeam);   
                            			}else {
                            				if(teamObj.getActive()==2) {
                            					teamObj.setActive(0L); 		
                            				}else {
                            					teamObj.setActive(1L); 		
                            				}
                            				
                            			}
                            		}
                            		
                            		employeeTeamMapRepository.save(teamObj);
                            	});
                            }
                            
                            
                        }
                        

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
			   html.append("<a style='background-color:blue;color:white;padding:10px 20px;text-decoration:none;border-radius:4px;' href='"+rmgProjectApprovalLink+resourceManagementDTO.getId()+"'>Approve Here</a>");
				
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
		System.err.println(" Anurag sync PO portal    ::   "+resourceManagementDTO);
		try {
			Project projectObj = null;
			if(!resourceManagementDTO.getProjectType().equals("Internal")) {
				projectObj = projectRepository.findByPoProjectId(resourceManagementDTO.getId());
			System.err.println(" projectObj   "+projectObj.getPoProjectId());
			List<PoProjectSyncDTO> projectInfo = new ArrayList<PoProjectSyncDTO>();
			List<PoTeamDTO> teamList = new ArrayList<PoTeamDTO>();
			
			if(projectObj != null) {
				PoProjectSyncDTO projectDTO = new PoProjectSyncDTO();
				
				projectDTO.setPoProjectId(projectObj.getPoProjectId());
				projectDTO.setProjectName(projectObj.getProjectName());
				
				String projectManagerId = getEmploymentId(projectObj.getProjectManagerId());
				System.out.println(" projectManagerId   ::   "+projectManagerId);
				projectDTO.setPoProjectManagerId(projectManagerId != null ? projectManagerId : null);
				
				//Get Team Details
				List<Team> teamDetails = teamRepository.findByProjectIdAndIsActive(projectObj.getProjectId(), "Y");
				
				if(!teamDetails.isEmpty()) {
					teamDetails.forEach((team) -> {
						System.err.println(" anurag get PO portal sync details ::   "+team);
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
						List<EmployeeTeamMap> teamMemberDetials = employeeTeamMapRepository.findByTeamIdAndActive(team.getTeamId());
//						List<EmployeeTeamMap> teamMemberDetials1 = employeeTeamMapRepository.findByTeamIdAndActive(team.getTeamId(), 2l);
						if(!teamMemberDetials.isEmpty()) {
							teamMemberDetials.forEach((member) -> {
								String memberEmpId = getEmploymentId(member.getEmpId());
								teamMember.add(memberEmpId);
							});
						}
//						if(!teamMemberDetials1.isEmpty()) {
//							teamMemberDetials1.forEach((member) -> {
//								String memberEmpId = getEmploymentId(member.getEmpId());
//								teamMember.add(memberEmpId);
//							});
//						}
						String teamMemberList[] = teamMember.toArray(new String[teamMember.size()]);
						poTeamDTO.setTeamMemberList(teamMemberList);
						poTeamDTO.setProjectId(team.getProjectId());			
						teamList.add(poTeamDTO);
					});
					projectDTO.setTeamList(teamList);
					
					System.err.println("Anurag poPortalListFind    :: "+projectDTO.toString());
					
					// added by anurag for temp  
					for (PoTeamDTO team2 : teamList) {
						System.err.println(team2);
						System.out.println("-=------------------------------------------------");
					}
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
					
					System.err.println("   jsonjsonjsonjsonjsonjson   json    "+json);
					
					if(json.getInt("httpStatusCode") == 200) {
						//Send Mail to PoPortal
					     try {
								mailService.sendMail(rmgMail,
										"Regarding Project Sync With PoPortal",
										"Dear RMG Team ,"+"<br>"
										+"<br>"
										+"Project : " +resourceManagementDTO.getName()+ " has been successfully synced with PoPortal.");
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
					response.setServiceResponse("Project & Team created successfully,but unable to sync with PoPortal :1496 "+syncResponse.getServiceResponse());
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
					projectDTO.setProjectManager(object[1] != null ? "A-".concat(object[1].toString()) : null);
					projectDTO.setProjectManagerName(object[2] != null ? object[2].toString() : null);
					projectDTO.setProjectId(object[3] != null ? Integer.parseInt(object[3].toString()) : null);
					projectDTO.setStatus(object[13] != null ? object[13].toString() : null);
					//projectDTO.setIsActive(object[13] != null ? Long.parseLong(object[13].toString()) :null);
					 //projectDTO.setIsActive(2l);
					projectDTO.setStartDate(object[4] != null ? object[4].toString() :null);
					projectDTO.setEndDate(object[5] != null ? object[5].toString() :null);
					//Find ClientName
					Integer clientId = object[9] != null ? Integer.parseInt(object[9].toString()) : null;
					if(clientId != null) {
						Client clientObj = clientsRepository.findByClientId(clientId);
						
						projectDTO.setClientName(clientObj.getClientName());
					}else {
						projectDTO.setClientName(null);
					}
					
					projectDTO.setClientState(object[10] != null ? object[10].toString() : null);
					projectDTO.setCreatedOn(object[7] != null ? object[7].toString() : null);
					projectDTO.setIsDraftProject(object[12] != null ? object[12].toString() : null);
					
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
//					String isTeamCreated = "false";
//					Long count = teamRepository.countByProjectId(projectId);
//					if(count>0) {
//						isTeamCreated = "false";
//					}
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
//						projectDTO.setIsTeamCreated(isTeamCreated);
						}
					projectInfo.add(projectDTO);
				});
				
				
				System.err.println(" ANurag projectInfo  "+projectInfo);
				
				
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
	
	
/**
 * getExistingProjectsAndTeamsByEmployee
 * adding this method for RMG
 * @param employeeId
 * @return
 */
	public ServiceResponse getExistingProjectsAndTeamsByEmployee(ResourceManagementDTO resourceManagementDTO) {
		
		ServiceResponse response = new ServiceResponse();
		List<Object[]> getAllExistingProjectsAndTeams;
		
		if(resourceManagementDTO.getIsAllProj() != null && resourceManagementDTO.getIsAllProj() == "true") {
			getAllExistingProjectsAndTeams = employeeTeamMapRepository.getAllProjectsTeamsInfo(resourceManagementDTO.getEmpId());
		}else {
			getAllExistingProjectsAndTeams = employeeTeamMapRepository.getAllProjectsAndTeamsDetails(resourceManagementDTO.getEmpId());
		}
		
		
		List<ResourceManagementDTO> allData = new ArrayList<ResourceManagementDTO>();
		getAllExistingProjectsAndTeams.forEach(obj ->{
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
			
			allData.add(dto);
		});
		
		if(allData != null) {
			response.setServiceResponse(allData);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			
		}
		return response;
	}
	
	/**
	 * update status as inActive from team and project
	 * 
	 */
	
	public ServiceResponse updateProjectResourceAsInActive(ResourceManagementDTO resourceManagementDTO ) {
		System.err.println("Anurag   updateProjectResourceAsInActive   ");
		
		ServiceResponse response = new ServiceResponse();
		
		EmployeeTeamMap findResource = employeeTeamMapRepository.findByEmpIdAndTeamIdAndActiveStatus(resourceManagementDTO.getEmpId(), resourceManagementDTO.getTeamId());
		Team findTeam = teamRepository.findTeamByTeamId(resourceManagementDTO.getTeamId());
		System.out.println("findResource  "+findResource );
		if(findResource != null) {
			findResource.setActive(0l);	
			
			if(resourceManagementDTO.getEndDate() != null) {
				String str = resourceManagementDTO.getEndDate();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				LocalDate date = LocalDate.parse(str, formatter);
				LocalDateTime endDateTime = date.atStartOfDay();

				findResource.setEndDate(endDateTime);
			}else {
				findResource.setEndDate(LocalDateTime.now());
			}
			
			employeeTeamMapRepository.save(findResource);
		
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Resource removed successfully, from Team Name - "+findTeam.getTeamName());
			
		}
		
	return response;	
	}
	
	private String generateHtmlTable(List<TeamMemberDTO> dtoList) {
	    StringBuilder html = new StringBuilder();
	    Employee empName = null;
	    Department department = null;
	    JobRole jobRole = null;
	    String designation=null;
	    
	    html.append("<html>\n" +
  	            "  <head>\n" +
  	            "    <style>\n" +
  	            "      table, th, td {\n" +
  	            "        border: 1px solid black;\n" +
  	            "      }\n" +
  	            "      table {\n" +
  	            "        border-collapse: collapse;\n" +
  	            "      }\n" +
  	            "    </style>\n" +
  	            "  </head>\n" +
  	            "  <body>\n" +
  	            "    <table>\n" +
  	            "      <tr>\n" +
  	            "        <th>Employee Id</th>\n" +
  	            "        <th>Employee Name</th>\n" +
  	            "        <th>Department Name</th>\n" +
  	            "		<th>Designation Name</th>\n" +
  	            "      </tr>\n");
	    for(TeamMemberDTO obj : dtoList) {
	    	empName = employeeRepository.findByEmpId(obj.getEmpId());
	    	jobRole = jobRoleRepository.findByjobRoleId(empName.getJobRoleId());
	    	department = departmentRepository.findByDeptId(jobRole.getDeptId());
	    	Optional<Object[]> result = employeeRepository.getDesignationByEmpId(obj.getEmpId());

	    	if (result.isPresent()) {
	    	    Object[] data = result.get();
	    	    designation = (String) data[0];  // Cast the first element to String
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
	    html.append("    </table>\n" +
  	            "  </body>\n" +
  	            "</html>");
	  
	    return html.toString();
	}
	
	public ServiceResponse deleteTeamByTeamId(TeamDTO teamDto) {
		ServiceResponse response = new ServiceResponse();
		
		Optional<Team> team = teamRepository.findById(teamDto.getTeamId());
		Team dbTeam = null;
		if(team.isPresent()) {
		Team getTeam = team.get();
		getTeam.setIsActive("N");		
		
		List<EmployeeTeamMap> findAllMappedEmp = employeeTeamMapRepository.findByTeamId(teamDto.getTeamId());
		System.err.println("findAllMappedEmp  "+findAllMappedEmp);
		
		findAllMappedEmp.forEach(emp ->{
		
			emp.setActive(0l);		
			employeeTeamMapRepository.save(emp);
		});	
		
		dbTeam = teamRepository.save(getTeam);
		}
		
//        team inactivate mail generateHtmlTable
        if(dbTeam != null) {
        	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
        	response.setServiceResponse("Team Deleted Successfully !!");
        }else {
        	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
        	response.setServiceResponse("Team not found !!");
        }
        
        try {
            mailService.sendMail("sakti.das@apmosys.com",
                    "Regarding Resource management",
                    "Dear RMG Team ," + "<br>"
                            + "<br>"
                            + " team has been deleted and the following reources are removed from this team -> : " + teamDto.getTeamName()+"<br>"
                            		+ "<br><br>"
									+ "Sincerely,"+"<br>"
									+ "Team RMG - ApMoSys Technologies"
									+ "<br>"
                            +generateHtmlTable(teamDto.getTeamMemberList()));                           
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		return response;
	}
	
	public ServiceResponse syncPoProjectDetailsByProjectId(ResourceManagementDTO resourceManagementDTO) {
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("syncPoProjectDetailsByProjectId");
        apiLogInfo.setApiUrl("/api/syncPoProjectDetailsByProjectId");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("syncPoProjectDetailsByProjectId "+resourceManagementDTO.getId());

		try {
			Project projObj = null;
		    if (resourceManagementDTO.getProjectType().equals("Internal")) {
	        	response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
				response.setServiceResponse("This is marked as an internal project !");
				return response;
		    } else {
		        projObj = projectRepository.findByProjectId(Integer.parseInt(resourceManagementDTO.getProjectId().toString()));
		    }

		    Project projectObj = projObj;

		    if (projectObj != null) {
		        
		        projectObj.setPoNo(resourceManagementDTO.getPoNo());
		        projectObj.setPoStartDate(resourceManagementDTO.getStartDate());
		        projectObj.setPoEndDate(resourceManagementDTO.getEndDate());
		        projectObj.setPoProjectType(resourceManagementDTO.getProjectType());
		        
		        Project projectDbResponse = projectRepository.save(projectObj);
		        
		        if(projectObj != projObj) {
		        	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Synced Successfully !");
		        }else if(projectObj == projObj) {
		        	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Already Up To Date !");
		        }else {
		        	response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
					response.setServiceResponse("Data mismatched between Ishine and Po !");
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

public ServiceResponse getProjectInfo(ResourceManagementDTO resourceManagementDTO) {
	
	ServiceResponse response = new ServiceResponse();
	LogDTO apiLogInfo = new LogDTO();
    apiLogInfo.setSubFeatureName("Project 360");
    apiLogInfo.setApiUrl("/api/getProjectInfo");
    apiLogInfo.setLogLevel("INFO");
    StringBuilder logBuilder = new StringBuilder();
    logBuilder.append("projectInfo : "+ projectRepository.getAllInternalProject().size());
    
	try {
		List<Object[]> projectInfo = projectRepository.getProjectInfo(resourceManagementDTO.getProjectId());
		List<ResourceManagementDTO> result = new ArrayList<>();
		if(!projectInfo.isEmpty()) {
			projectInfo.forEach(object ->{
				ResourceManagementDTO dto = new ResourceManagementDTO();
				
				dto.setProjectId(object[0] != null ? Integer.parseInt(object[0].toString()) : null);
				dto.setProjectName(object[1] != null ? object[1].toString() : null);		
				dto.setProjectManagerId(object[2] != null ?  Long.parseLong(object[2].toString()) : null);
				dto.setProjectManagerName(object[3] != null ? object[3].toString() : null);	
				dto.setClientName(object[4] != null ? object[4].toString().toString() : null);
				dto.setClientState(object[5] != null? object[5].toString() : null);		
				
				result.add(dto);
			});
			
			if(projectInfo != null) {
				response.setServiceResponse(result);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setApiResponse("projectInfoList fetched");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No project found.");
				apiLogInfo.setApiResponse("No Project Found");
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

public ServiceResponse getTeamMemberByTeamId(Long teamId) {
	ServiceResponse response = new ServiceResponse();
	List<Object[]> allEmployeeList = employeeTeamMapRepository.findEmployeeByTeamId(teamId);
	List<EmployeeDTO> dtoList=new ArrayList();
	allEmployeeList.forEach((object) -> {
		EmployeeDTO empDTO = new EmployeeDTO();

		empDTO.setEmpId(object[1] != null ? Long.parseLong(object[1].toString()) :null);
		empDTO.setName(object[0] != null ? object[0].toString() : null);
		
		empDTO.setEmployeementId(object[2] != null ? Long.parseLong(object[2].toString()) :null);
		empDTO.setTeamLeadName(object[5] != null ? object[5].toString() :null);
		empDTO.setTeamName(object[4] != null ? object[4].toString() :null);
		empDTO.setEmail(object[3] != null ? object[3].toString() :null);
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
    logBuilder.append("TeamInfo : "+ projectRepository.getTeamInfo(resourceManagementDTO.getProjectId()).size());
    
	try {
		List<Object[]> teamInfo = projectRepository.getTeamInfo(resourceManagementDTO.getProjectId());
		List<ResourceManagementDTO> result = new ArrayList<>();
		if(!teamInfo.isEmpty()) {
			teamInfo.forEach(object ->{
				ResourceManagementDTO dto = new ResourceManagementDTO();
				
				dto.setTeamId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
				dto.setTeamName(object[1] != null ? object[1].toString() : null);		
				dto.setEmpId(object[2] != null ? Long.parseLong(object[2].toString()) : null);
				dto.setEmployeeName(object[3] != null ? object[3].toString() : null);
				dto.setEmployeeRole(object[4] != null ? object[4].toString().toString() : null);
				dto.setBillableType(object[5] != null ? object[5].toString() : null);
				dto.setStartDate(object[6] != null ? object[6].toString() : null);
				dto.setActive(object[7] != null ? Integer.parseInt(object[7].toString()) : null);	
				dto.setProjectId(object[8] != null ? Integer.parseInt(object[8].toString()) : null);
				dto.setProjectName(object[9] != null ? object[9].toString() : null);
				dto.setClientId(object[10] != null ? Long.parseLong(object[10].toString()) : null);
				dto.setClientName(object[11] != null ? object[11].toString() : null);
				
				result.add(dto);
			});
			
			if(teamInfo != null) {
				response.setServiceResponse(result);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setApiResponse("projectInfoList fetched");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No team found.");
				apiLogInfo.setApiResponse("No team Found");
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
	
	public ServiceResponse getPoProjectDetailsForPoProjects() {
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("getPoProjectDetailsForPoProjects");
        apiLogInfo.setApiUrl("/api/getPoProjectDetailsForPoProjects");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("getPoProjectDetailsForPoProjects "+projectRepository.getPoProjectDetailsForPoProjects().size());

		try {
			List<Object[]> projectInfoList = projectRepository.getPoProjectDetailsForPoProjects();
			List<ProjectInfoDTO> projectObjList = new ArrayList<ProjectInfoDTO>();
			
		    if (projectInfoList != null || !projectInfoList.isEmpty()) {
		    	projectInfoList.forEach((projectInfo) -> {
		    	ProjectInfoDTO projectObj = new ProjectInfoDTO();
		    	
		    	projectObj.setProjectId(projectInfo[0]!=null ? Integer.parseInt(projectInfo[0].toString()) : null);
		    	projectObj.setProjectName(projectInfo[1]!=null ? projectInfo[1].toString() : null);
		        projectObj.setPoNo(projectInfo[2]!=null ? projectInfo[2].toString() : null);
		        projectObj.setStartDate(projectInfo[3]!=null ? projectInfo[3].toString() : null);
		        projectObj.setEndDate(projectInfo[4]!=null ? projectInfo[4].toString() : null);
		        projectObj.setProjectType(projectInfo[5]!=null ? projectInfo[5].toString() : null);
		        projectObj.setId(projectInfo[6]!=null ? Long.parseLong(projectInfo[6].toString()) : null);
		        
		        projectObjList.add(projectObj);
		    	});
	    	}
	        if(projectObjList != null || !projectObjList.isEmpty()) {
	        	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(projectObjList);
	        }else {
	        	response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
				response.setServiceResponse("Data not present !");
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
