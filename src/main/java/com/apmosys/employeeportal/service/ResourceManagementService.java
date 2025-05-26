package com.apmosys.employeeportal.service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpServerErrorException.InternalServerError;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.apmosys.employeeportal.dto.CombinedPOInternalProjectResponse;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.EmployeeDetailsForTeamMemberDTO;
import com.apmosys.employeeportal.dto.EmployeeTeamMapDTO;
import com.apmosys.employeeportal.dto.GetEmployeeByNameAndEmpldDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.PoProjectSyncDTO;
import com.apmosys.employeeportal.dto.PoTeamDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.ProjectFilterDTO;
import com.apmosys.employeeportal.dto.ProjectInfoDTO;
import com.apmosys.employeeportal.dto.ProjectManagerMappingDTO;
import com.apmosys.employeeportal.dto.ProjectManagersDTO;
import com.apmosys.employeeportal.dto.RMGProject;
import com.apmosys.employeeportal.dto.RMGProjectMappedEmployees;
import com.apmosys.employeeportal.dto.RMGTeam;
import com.apmosys.employeeportal.dto.ProjectRequirementsDTO;
import com.apmosys.employeeportal.dto.ResourceManagementDTO;
import com.apmosys.employeeportal.dto.ResourceRequirementDTO;
import com.apmosys.employeeportal.dto.SpocDTO;
import com.apmosys.employeeportal.dto.TeamDTO;
import com.apmosys.employeeportal.dto.TeamMemberDTO;
import com.apmosys.employeeportal.dto.TeamSpocDTO;
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
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.ProjectDepartmentMapRepository;
import com.apmosys.employeeportal.repository.ProjectManagerMappingRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.ResourceRequirementRepository;
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
	
	@Autowired
	private final RestTemplate restTemplate = new RestTemplate();

	
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

		    System.err.println("Project Type"+projObj);
		    
		    Project projectObj = projObj;
		    Employee employeeObj = employeeRepository.findByEmpId(resourceManagementDTO.getCreatedBy());

		    List<Long> allTeam = new ArrayList<>();

		    if (projectObj != null) {
		    	ServiceResponse response1 = new ServiceResponse();
		    	response1=this.projectIsPresent(projectObj,resourceManagementDTO,allTeam);
		    	  if(response1.getServiceStatus()!= "Success") {
		            	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			            response.setServiceResponse(response1.getServiceResponse());
			            apiLogInfo.setApiResponse("Project not updated successfully");
			            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
		            }else {
		            	  response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			                response.setServiceResponse(response1.getServiceResponse());
			                apiLogInfo.setApiResponse("Project updated successfully");
			                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
		            }
		    	  
	                
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
//			    Long employeementID = Long.parseLong(resourceManagementDTO.getProjectManager().split("-")[1]);
//			    Long projManagerId = null;
//			    Employee employee = employeeRepository.findByEmployeementId(employeementID);
//			    if (employee != null) {
//			        projManagerId = employee.getEmpId();
//			    }
				
				//Add project
			    Project newProject = new Project();
			    newProject.setProjectName(resourceManagementDTO.getName());
			    newProject.setState(resourceManagementDTO.getClientState());
			    newProject.setClientId(clientId);
			    newProject.setPoProjectId(resourceManagementDTO.getId());
			    newProject.setActive("true");
			    newProject.setSyncProject("true");
			    newProject.setPoProjectType(
			    	    "Internal".equalsIgnoreCase(resourceManagementDTO.getProjectType()) ? null : resourceManagementDTO.getProjectType()
			    	);

			    newProject.setPoNo(resourceManagementDTO.getPoNo());
			    newProject.setPoStartDate(resourceManagementDTO.getStartDate());
			    newProject.setPoEndDate(resourceManagementDTO.getEndDate());
                newProject.setCreatedOn(new Timestamp(System.currentTimeMillis()));
                newProject.setApmosysRM(resourceManagementDTO.getApmosysRM());	
			    newProject.setIsRenewable(resourceManagementDTO.getIsRenewable());
			    newProject.setClientRM(resourceManagementDTO.getClientRM());
			    newProject.setApmosysRmEmail(resourceManagementDTO.getApmosysRmEmail());
			    
			    
			    
				  	if (resourceManagementDTO.getIsHOD().equals("true")) {
				 	newProject.setIsDraftProject("false"); 
				 	} else {
				 	newProject.setIsDraftProject("true"); 
				 	}


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
			        
			        ServiceResponse responseProjectManager= this.setProjectManager(resourceManagementDTO,projectDbResponse);
		            
		            if(responseProjectManager.getServiceStatus()!= "Success") {
		            	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			            response.setServiceResponse(responseProjectManager.getServiceResponse());
		            }else {
		            	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			            response.setServiceResponse(responseProjectManager.getServiceResponse());
		            }
				
                    if (resourceManagementDTO.getTeamList() != null && !resourceManagementDTO.getTeamList().isEmpty()) {
                        resourceManagementDTO.getTeamList().forEach(teamObj -> {
                            // your logic here
                            System.out.println("Inside loop: " + teamObj.getTeamName());

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
    			            newTeamObj.setCreatedBy(resourceManagementDTO.getCreatedBy());
    			            newTeamObj.setCreatedOn(new Timestamp(System.currentTimeMillis()));   
    			            newTeamObj.setSpocId(teamObj.getSpocId());
    			            
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
//    			                     .append("<th style='padding: 8px; text-align: left;'>Employee Role</th>")
    			                     .append("</tr>");

    			            if (teamDbResponse != null) {
    			                List<EmployeeTeamMap> mapList = new ArrayList<EmployeeTeamMap>();
    			                String ccMail = adminMail;
    			                // Add team member in the team
    			                for (TeamMemberDTO teamMember : teamObj.getTeamMemberList()) {
    			                    EmployeeTeamMap newEmpTeamMap = new EmployeeTeamMap();
    			                    //it is returning multiple resuts................///////////////////
    			                   
    			                    List<EmployeeDetailsForTeamMemberDTO> dtoList = new ArrayList<EmployeeDetailsForTeamMemberDTO>();
    			                    List<Object[]> employeeDetails = employeeRepository.getEmployeeDetailsForTeam(teamMember.getEmpId());

    			        			if (employeeDetails != null) {
    			        				employeeDetails.forEach((object) -> {
    			        					 EmployeeDetailsForTeamMemberDTO dto = new EmployeeDetailsForTeamMemberDTO();
    				            
    				                
                     
                                  dto.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
                                  dto.setEmployeementId(object[1] != null ? Long.parseLong(object[1].toString()) : null);
                                  dto.setName(object[2] != null ? object[2].toString() : null);
                                  dto.setJobRoleId(object[3] != null ? Long.parseLong(object[3].toString()) : null);
                                  dto.setJobRoleName(object[4] != null ? object[4].toString() : null);
                                  dto.setDeptId(object[5] != null ? Long.parseLong(object[5].toString()) : null);
                                  dto.setDeptName(object[6] != null ? object[6].toString() : null);
                                  dto.setIsConsultant(object[7] != null ? object[7].toString() : null);
                                  
                                  dtoList.add(dto);
                                  });
                                  }
    				                //it is returning multiple resuts................///////////////////
    				                String hodMail = employeeRepository.findHodMail(teamMember.getEmpId());
    		                    	
    	                    	    ccMail = ccMail + "," + hodMail;
    		                    	 
    			                    // TeamLead
    			                    if ((teamMember.getIsTeamLead() != null) && (teamMember.getIsTeamLead().equals("true"))) {
    			                        newEmpTeamMap.setEmpId(teamMember.getEmpId());
    			                        newEmpTeamMap.setActive(2L); // Set Active to 2 for TeamLead
    			                        newEmpTeamMap.setEmployeeRole("TeamLead");
    			                        newEmpTeamMap.setTeamId(teamDbResponse.getTeamId());
					                    newEmpTeamMap.setShadowEmpId(teamMember.getShadowEmpId() != null ? Long.parseLong(teamMember.getShadowEmpId().toString()) : null);
//					                    newEmpTeamMap.setBillable(teamMember.getBillable());
//					                    newEmpTeamMap.setBillableType(teamMember.getBillableType());
//						                newEmpTeamMap.setShadowBillable(teamMember.getShadowBillable());
//						                newEmpTeamMap.setShadowBillableType(teamMember.getShadowBillableType());
						                newEmpTeamMap.setResourceOverviewId(teamMember.getResourceOverviewId() != null ? Long.parseLong(teamMember.getResourceOverviewId().toString()) : null);
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
					                    newEmpTeamMap.setShadowEmpId(teamMember.getShadowEmpId() != null ? Long.parseLong(teamMember.getShadowEmpId().toString()) : null);
//					                    newEmpTeamMap.setBillable(teamMember.getBillable() != null ? teamMember.getBillable() : null);
//					                    newEmpTeamMap.setBillableType(teamMember.getBillableType() != null ? teamMember.getBillableType() : null);
//						                newEmpTeamMap.setShadowBillable(teamMember.getShadowBillable() != null ? teamMember.getShadowBillable() : null);
//						                newEmpTeamMap.setShadowBillableType(teamMember.getShadowBillableType() != null ? teamMember.getShadowBillableType() : null);
						                newEmpTeamMap.setResourceOverviewId(teamMember.getResourceOverviewId() != null ? Long.parseLong(teamMember.getResourceOverviewId().toString()) : null);
    			                        mapList.add(newEmpTeamMap);
    			                    }
    			                    
    			                    for (EmployeeDetailsForTeamMemberDTO dto : dtoList) {
    			                        if (dto != null) {
        			                        String employmentId = "A-" + dto.getEmployeementId();
        			                        if ("true".equalsIgnoreCase(dto.getIsConsultant())) {
        			                            employmentId = "CS-" + dto.getEmployeementId();
        			                        }

        			                        String employeeRole = teamMember.getIsTeamLead() != null && teamMember.getIsTeamLead().equalsIgnoreCase("true")
        			                                            ? "TeamLead"
        			                                            : String.join(",", teamMember.getEmployeeRole());

        			                        emailBody.append("<tr>")
        			                        .append("<td style='padding: 8px;'>").append(employmentId).append("</td>")
        			                        .append("<td style='padding: 8px;'>").append(dto.getEmployeeName()).append("</td>")
        			                        .append("<td style='padding: 8px;'>").append(dto.getJobRoleName()).append("</td>")
        			                        .append("<td style='padding: 8px;'>").append(dto.getDeptName()).append("</td>")
        			                        .append("<td style='padding: 8px;'>").append(dto.getStartDate()).append("</td>")
//        			                        .append("<td style='padding: 8px;'>").append(employeeRole).append("</td>")
        			                        .append("</tr>");
        			                    }
    			                   
    			                    }
    			              
    			                }
    			                List<EmployeeTeamMap> teamMemberDbResponse = employeeTeamMapRepository.saveAll(mapList);

    			                emailBody.append("</table><br><br>")
    			                .append("Sincerely,<br>")
    			                .append("<b>Team RMG - ApMoSys Technologies</b>")
    			                .append("</body></html>");

    			                try {
    			                    mailService.sendMailWithCC(ccMail,rmgMail, "Regarding Team Creation", emailBody.toString());
    			                } catch ( MessagingException e) {
    			                    e.printStackTrace();
    			                }
    			               

    			                // Add default activity
//    			                this.activityRealtedToTeam(teamMemberDbResponse,teamObj);
    			                ServiceResponse response3= this.activityRealtedToTeam(teamMemberDbResponse,teamObj,resourceManagementDTO,teamDbResponse);
					            
					            if(response3.getServiceStatus()!= "Success") {
					            	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						            response.setServiceResponse(response3.getServiceResponse());
						            apiLogInfo.setApiResponse("Team created successfully, but default activities are not mapped");
						            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					            }else {
					            	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						            response.setServiceResponse(response3.getServiceResponse());
						            apiLogInfo.setApiResponse("Team created successfully");
						            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					            }

    			                
    			               } else {
    			                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
    			                response.setServiceResponse("Unable to create a new team.");
    			                apiLogInfo.setApiResponse("Unable to create a new team.");
    			                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
    			            }
    			        
                            
                        });
                    } else {
                        System.out.println("Team list is null or empty");
                        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			            response.setServiceResponse("Team list is null or empty");
			            apiLogInfo.setApiResponse("Team list is null or empty");
			            
                    }

                    
                    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		            response.setServiceResponse("Project updated successfully");
		            apiLogInfo.setApiResponse("Project updated successfully");
		            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
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
	
	
	public  ServiceResponse projectIsPresent(Project projectObj, ResourceManagementDTO resourceManagementDTO,List<Long> allTeam){
		ServiceResponse response = new ServiceResponse();
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
		        Set<Team> newlyAddedTeam = addTeamList.stream().filter(dto -> !teamId.contains(dto.getTeamId())).collect(Collectors.toSet());

		        ServiceResponse response1= this.updateExistingProject(projectObj, resourceManagementDTO);
		        System.err.print("jbsjhg"+response1.getServiceStatus());
		        if(response1.getServiceStatus()!= "Success") {
	            	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		            response.setServiceResponse(response1.getServiceResponse());
		            apiLogInfo.setApiResponse("Project not updated successfully");
		            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	            }else {
	            	    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		                response.setServiceResponse(response1.getServiceResponse());
		                apiLogInfo.setApiResponse("Project  updated successfully");
		                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	            }
		        

		        if (resourceManagementDTO != null && resourceManagementDTO.getTeamList() != null) {
		            resourceManagementDTO.getTeamList().forEach(teamObj -> {
		                System.out.println("Team Name: " + teamObj.getTeamName());

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
						        teamPresent.setUpdatedBy(resourceManagementDTO.getCreatedBy());
						        teamPresent.setUpdatedOn(LocalDateTime.now());
						        teamPresent.setSpocId(teamObj.getSpocId());

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
						                            updateMember.setUpdatedOn(LocalDateTime.now());
						                            updateMember.setUpdatedBy(resourceManagementDTO.getCreatedBy());
						                            updateMember.setShadowEmpId(newMember.getShadowEmpId() != null ? Long.parseLong(newMember.getShadowEmpId().toString()) : null);
//						                            updateMember.setBillable(newMember.getBillable());
//						                            updateMember.setBillableType(newMember.getBillableType());
//						                            updateMember.setShadowBillable(newMember.getShadowBillable());
//						                            updateMember.setShadowBillableType(newMember.getShadowBillableType());
						                            updateMember.setResourceOverviewId(newMember.getResourceOverviewId() != null ? Long.parseLong(newMember.getResourceOverviewId().toString()) : null);
						                            updateMemberList.add(updateMember);
						                        }
						                    }
						                }
						            }
						            List<EmployeeTeamMap> updateMemberDbResponse = employeeTeamMapRepository.saveAll(updateMemberList);

						            // Add teamMember mapping
						            ServiceResponse response2=this.processNewTeamMembers( newTeamMember, teamDbResponse, resourceManagementDTO, rmgMail,adminMail);
						            
						            if(response2.getServiceStatus()!= "Success") {
						            	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							            response.setServiceResponse(response2.getServiceResponse());
							            apiLogInfo.setApiResponse("Team not updated successfully");
							            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
						            }else {
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
						                    
//						                    mail for inactive employee

					                    	Employee  findEmp = employeeRepository.findByEmpId(member.getEmpId());
				                    	    Employee managerEmail = employeeRepository.findByEmpId(findEmp.getManagerId());
				                    	    String hodMail = employeeRepository.findHodMail(member.getEmpId());
				                    	  
				                    	    String ccMail = hodMail + "," + managerEmail.getEmail().toString() + "," + rmgMail + "," + adminMail;
					                    	  
						                    
						                    try {
												mailService.sendMailWithCC(findEmp.getEmail().toString(),ccMail,"Regarding Resource removed from Project ", "Dear "
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
						        newTeamObj.setCreatedBy(resourceManagementDTO.getCreatedBy());
						        newTeamObj.setCreatedOn(new Timestamp(System.currentTimeMillis()));  
						        newTeamObj.setSpocId(teamObj.getSpocId());
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
						                    newEmpTeamMap.setShadowEmpId(teamMember.getShadowEmpId() != null ? Long.parseLong(teamMember.getShadowEmpId().toString()) : null);
//						                    newEmpTeamMap.setBillable(teamMember.getBillable());
//						                    newEmpTeamMap.setBillableType(teamMember.getBillableType());
//							                newEmpTeamMap.setShadowBillable(teamMember.getShadowBillable());
//							                newEmpTeamMap.setShadowBillableType(teamMember.getShadowBillableType());
							                newEmpTeamMap.setResourceOverviewId(teamMember.getResourceOverviewId() != null ? Long.parseLong(teamMember.getResourceOverviewId().toString()) : null );
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
						                    newEmpTeamMap.setShadowEmpId(teamMember.getShadowEmpId() != null ? Long.parseLong(teamMember.getShadowEmpId().toString()) : null);
//						                    newEmpTeamMap.setBillable(teamMember.getBillable());
//						                    newEmpTeamMap.setBillableType(teamMember.getBillableType());
//							                newEmpTeamMap.setShadowBillable(teamMember.getShadowBillable());
//							                newEmpTeamMap.setShadowBillableType(teamMember.getShadowBillableType());
							                newEmpTeamMap.setResourceOverviewId(teamMember.getResourceOverviewId() != null ? Long.parseLong(teamMember.getResourceOverviewId().toString()) : null);
						                    mapList.add(newEmpTeamMap);
						                }
						            }
//						            mail for create Team
						            
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
						           ServiceResponse response3= this.activityRealtedToTeam(teamMemberDbResponse,teamObj,resourceManagementDTO,teamDbResponse);
						            
						            if(response3.getServiceStatus()!= "Success") {
						            	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							            response.setServiceResponse(response3.getServiceResponse());
							            apiLogInfo.setApiResponse("Team created successfully, but default activities are not mapped");
							            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
						            }else {
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
				
				
				//InActivate Team
				
				List<Team> alreadyExistTeam = teamRepository.findByTeamIdNotInAndProjectId(allTeam, projectObj.getProjectId());
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

	    	
	    }catch(Exception e){
	    	
	    	e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            apiLogInfo.setLogLevel("ERROR");
	    	
	    }
			      								    
				    return response;
	}
	
	private ServiceResponse processNewTeamMembers(List<TeamMemberDTO> newTeamMember, Team teamDbResponse, ResourceManagementDTO resourceManagementDTO, String rmgMail, String adminMail) {
		ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
		try {

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
		                empTeamMap.setShadowEmpId(newMember.getShadowEmpId() != null ? Long.parseLong(newMember.getShadowEmpId().toString()) : null);
//		                empTeamMap.setBillable(newMember.getBillable());
//		                empTeamMap.setBillableType(newMember.getBillableType());
//		                empTeamMap.setShadowBillable(newMember.getShadowBillable());
//		                empTeamMap.setShadowBillableType(newMember.getShadowBillableType());
		                empTeamMap.setResourceOverviewId(newMember.getResourceOverviewId() != null ? Long.parseLong(newMember.getResourceOverviewId().toString()) : null);
		                mapList.add(empTeamMap);
		            } else {
		                StringBuilder employeeRole = new StringBuilder("");
		                for (String empRole : newMember.getEmployeeRole()) {
		                    employeeRole.append(empRole).append(",");
		                }
		                empTeamMap.setEmpId(newMember.getEmpId());
		                empTeamMap.setEmployeeRole(employeeRole.toString());
		                empTeamMap.setTeamId(teamDbResponse.getTeamId());
		                empTeamMap.setShadowEmpId(newMember.getShadowEmpId() != null ? Long.parseLong(newMember.getShadowEmpId().toString()) : null);
//		                empTeamMap.setBillable(newMember.getBillable());
//		                empTeamMap.setBillableType(newMember.getBillableType());
//		                empTeamMap.setShadowBillable(newMember.getShadowBillable());
//		                empTeamMap.setShadowBillableType(newMember.getShadowBillableType());
		                empTeamMap.setResourceOverviewId(newMember.getResourceOverviewId() != null ? Long.parseLong(newMember.getResourceOverviewId().toString()): null);
		                mapList.add(empTeamMap);
		            }

		            List<EmployeeTeamMap> teamMapDbResponse = employeeTeamMapRepository.saveAll(mapList);

		            teamMapDbResponse.forEach((newAddedMember) -> {
		                if (resourceManagementDTO.getIsHOD().equals("true"))
		                    newAddedMember.setActive(1L);
		                else
		                    newAddedMember.setActive(2L);

		                System.out.println(" newTeamMember   " + newAddedMember);

		                if (newAddedMember.getEmpId() != null) {
		                    List<Object[]> employeeDetails = employeeRepository.getEmployeeByEmpId(newAddedMember.getEmpId());
		                    System.out.println("New member added: " + employeeDetails.get(0));

		                    if (employeeDetails != null && !employeeDetails.isEmpty()) {
		                        Object[] employeeDetailRow = employeeDetails.get(0);
		                        String departmentId = employeeDetailRow[46] != null ? employeeDetailRow[46].toString() : null;
		                        System.out.println("Department ID: " + departmentId);

		                        if (departmentId != null) {
		                            List<String> employeeRoles = Arrays.asList(newAddedMember.getEmployeeRole().split(","));
		                            for (String role : employeeRoles) {
		                                role = role.trim();
		                                System.out.println("Processing role: " + role);

		                                List<Activity> existingActivities = activitiesRepository.findByDeptIdsAndEmployeeRoleAndTeamId(
		                                        departmentId,
		                                        role,
		                                        teamDbResponse.getTeamId()
		                                );

		                                if (existingActivities.isEmpty()) {
		                                    System.out.println("No activities exist for Dept ID: " + departmentId + ", Role: " + role);

		                                    List<ActivityTemplate> activityTemplateList = activityTemplateRepository.getByDeptIdAndEmployeeRoleType(
		                                            Long.parseLong(departmentId),
		                                            role
		                                    );
		                                    if (!activityTemplateList.isEmpty()) {
		                                        for (ActivityTemplate activityTemplate : activityTemplateList) {
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

		                Employee findEmp = employeeRepository.findByEmpId(newAddedMember.getEmpId());
		                Employee managerEmail = employeeRepository.findByEmpId(findEmp.getManagerId());
		                String hodMail = employeeRepository.findHodMail(newAddedMember.getEmpId());

		                String ccMail = hodMail + "," + managerEmail.getEmail().toString() + "," + rmgMail + "," + adminMail;

		                Project projectFind = null;
		                if (resourceManagementDTO.getProjectType().equals("Internal")) {
		                    projectFind = projectRepository.findByProjectId(resourceManagementDTO.getProjectId());
		                } else {
		                    projectFind = projectRepository.findByPoProjectId(resourceManagementDTO.getId());
		                }

		                try {
		                    mailService.sendMailWithCC(findEmp.getEmail().toString(), ccMail,
		                            "Regarding resource mapping to new project",
		                            "Dear " + findEmp.getName() + "<br>"
		                                    + "You have been mapped to client name - " + resourceManagementDTO.getClientName()
		                                    + " under the project " + projectFind.getProjectName() + "<br><br><br>"
		                                    + "Sincerely,<br>Team RMG - ApMoSys Technologies"
		                    );
		                } catch (AddressException e) {
		                    e.printStackTrace();
		                } catch (MessagingException e) {
		                    e.printStackTrace();
		                }
		            });

		            employeeTeamMapRepository.saveAll(teamMapDbResponse);
		        }

		    });

		}catch(Exception e) {
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
		    project.setIsDraftProject("false");
		    project.setProjectName(dto.getName());
		    project.setPoNo(dto.getPoNo());
		    project.setPoStartDate(dto.getStartDate());
		    project.setPoEndDate(dto.getEndDate());
		    project.setPoProjectType("Internal".equalsIgnoreCase(dto.getProjectType()) ? null : dto.getProjectType());

		    project.setApmosysRM(dto.getApmosysRM());
		    project.setIsRenewable(dto.getIsRenewable());
		    project.setClientRM(dto.getClientRM());
		    project.setUpdatedBy(dto.getCreatedBy());
		    project.setUpdatedOn(LocalDateTime.now());
		     Project dbResponse = projectRepository.save(project);
		     
		     ServiceResponse responseProjectManager= this.setProjectManager(dto,dbResponse);
	            
	            if(responseProjectManager.getServiceStatus()!= "Success") {
	            	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		            response.setServiceResponse(responseProjectManager.getServiceResponse());
	            }else {
	            	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		            response.setServiceResponse(responseProjectManager.getServiceResponse());
	            }
		     
		     if(!dto.getResourceRequirements().isEmpty()) {
		    	 dto.getResourceRequirements().forEach(req -> {
		        		ResourceRequirement resourceManagementDTO= new ResourceRequirement();
		        		
		        		resourceManagementDTO.setCount(req.getCount());
		        		resourceManagementDTO.setDepartment(req.getDepartment());
		        		resourceManagementDTO.setExperience(req.getExperience());
		        		resourceManagementDTO.setRole(req.getRole());
		        		resourceManagementDTO.setResourceOverviewId(req.getResourceOverviewId() != null ? Long.parseLong(req.getResourceOverviewId().toString()): null);
		        		resourceManagementDTO.setProjectId(project.getProjectId());

			        	ResourceRequirement res = resourceRequirementRepository.save(resourceManagementDTO);	
		        		});
		         }
		     
		    if(dbResponse!= null) {
		    	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse("Project updated successfully");
	            apiLogInfo.setApiResponse("Project updated successfully");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
		    }else {
		    	    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		            response.setServiceResponse("Project not updated successfully");
		            apiLogInfo.setApiResponse("Project not updated successfully");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		    }
		    
		}catch(Exception e){
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
	
	public ServiceResponse activityRealtedToTeam(List<EmployeeTeamMap> teamMemberDbResponse,TeamDTO teamObj,ResourceManagementDTO resourceManagementDTO,Team teamDbResponse) {
		ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
		
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
						teamdto.setSpocId(object.getSpocId());
						
						List<Object[]> spocDetailsList = teamRepository.getSpocDetils(object.getSpocId());
				        if (!spocDetailsList.isEmpty()) {
				            Object[] spoc = spocDetailsList.get(0);
				            SpocDTO spocDTO = new SpocDTO();
				            spocDTO.setEmpId(Long.parseLong(spoc[0].toString()));
				            spocDTO.setName( spoc[1].toString());
				            spocDTO.setEmploymentId(spoc[2].toString());
				            teamdto.setSpoc(spocDTO);
				        }
						
						//Get teamMembers Info
						List<EmployeeTeamMap> empTeamMapping = employeeTeamMapRepository.findByTeamIdAndActive(object.getTeamId());
						if(!empTeamMapping.isEmpty()) {
							List<TeamMemberDTO> teamMember = new ArrayList<TeamMemberDTO>();
							empTeamMapping.forEach((teamMemberObj) -> {
								String employeeName = null;
								Employee empObj = employeeRepository.findByEmpId(teamMemberObj.getEmpId());
								JobRole findJobRole = jobRoleRepository.findByjobRoleId(empObj.getJobRoleId());
								Department findDepartment = departmentRepository.findByDeptId(findJobRole.getDeptId());
								String isConsultant = empObj.getIsConsultant();
								String prefix = "A-";
								if ("true".equalsIgnoreCase(isConsultant)) {
									prefix = "CS-";
							    }
								
								if(empObj != null && !"InActive".equalsIgnoreCase(empObj.getEmploymentstatus())) {
									employeeName = empObj.getName();
								}
								
								TeamMemberDTO teamMemberDTO = new TeamMemberDTO();
								
								if((object.getTeamLeadId() != null) && (object.getTeamLeadId().equals(teamMemberObj.getEmpId()))) {
									//Team Lead
									teamMemberDTO.setEmpId(teamMemberObj.getEmpId());
									teamMemberDTO.setName(employeeName);
									teamMemberDTO.setIsTeamLead("true");
									teamMemberDTO.setEmploymentIdEmployeeType(prefix + empObj.getEmployeementId());									
									
									teamMemberDTO.setStartDate(teamMemberObj.getStartDate().toString());
									teamMemberDTO.setDepartmentName(findDepartment.getName());
									teamMemberDTO.setDepartmentId(findDepartment.getDeptId().toString());
									teamMemberDTO.setEmployeeRole(teamMemberObj.getEmployeeRole().split(","));
									teamMemberDTO.setResourceOverviewId(Long.parseLong(teamMemberObj.getResourceOverviewId().toString()));
									
									if(teamMemberObj.getShadowEmpId()!= null) {
									Employee shadowempObj = employeeRepository.findByEmpId(teamMemberObj.getShadowEmpId());
									JobRole findShadowJobRole = jobRoleRepository.findByjobRoleId(shadowempObj.getJobRoleId());
									Department findShadowDepartment = departmentRepository.findByDeptId(findShadowJobRole.getDeptId());
									String isConsultantShadow = shadowempObj.getIsConsultant();
									String prefixx = "A-";
									
									if ("true".equalsIgnoreCase(isConsultantShadow)) {
										prefixx = "CS-";
								    }

									
									teamMemberDTO.setShadowEmpId(Long.parseLong(teamMemberObj.getShadowEmpId().toString()));
									teamMemberDTO.setShadowEmployeeEmploymentId(prefixx + shadowempObj.getEmployeementId());
									teamMemberDTO.setShadowEmployeeName(shadowempObj.getName());
									teamMemberDTO.setShadowEmployeeDepartmentName(findShadowDepartment.getName());
									
									}
									
									
>>>>>>> Stashed changes
//									teamMemberDTO.setBillable(teamMemberObj.getBillable());
//									teamMemberDTO.setBillableType(teamMemberObj.getBillableType());
//									teamMemberDTO.setShadowBillable(teamMemberObj.getShadowBillable());
//									teamMemberDTO.setShadowBillableType(teamMemberObj.getShadowBillableType());
									teamMember.add(teamMemberDTO);
								}else {
									//Team Member
									teamMemberDTO.setEmpId(teamMemberObj.getEmpId());
									teamMemberDTO.setName(employeeName);
									teamMemberDTO.setEmploymentIdEmployeeType(prefix + empObj.getEmployeementId());	
									teamMemberDTO.setStartDate(teamMemberObj.getStartDate().toString());
									teamMemberDTO.setDepartmentName(findDepartment.getName());
									teamMemberDTO.setDepartmentId(findDepartment.getDeptId().toString());
									teamMemberDTO.setEmployeeRole(teamMemberObj.getEmployeeRole().split(","));
									teamMemberDTO.setResourceOverviewId(Long.parseLong(teamMemberObj.getResourceOverviewId().toString()));
									if(teamMemberObj.getShadowEmpId()!= null) {
										Employee shadowempObj = employeeRepository.findByEmpId(teamMemberObj.getShadowEmpId());
										JobRole findShadowJobRole = jobRoleRepository.findByjobRoleId(shadowempObj.getJobRoleId());
										Department findShadowDepartment = departmentRepository.findByDeptId(findShadowJobRole.getDeptId());
										String isConsultantShadow = shadowempObj.getIsConsultant();
										String prefixx = "A-";
										
										if ("true".equalsIgnoreCase(isConsultantShadow)) {
											prefixx = "CS-";
									    }
												
										
										teamMemberDTO.setShadowEmpId(teamMemberObj.getShadowEmpId()!= null ? Long.parseLong(teamMemberObj.getShadowEmpId().toString()):null);
										teamMemberDTO.setShadowEmployeeEmploymentId(prefixx + shadowempObj.getEmployeementId());
										teamMemberDTO.setShadowEmployeeName(shadowempObj.getName());
										teamMemberDTO.setShadowEmployeeDepartmentName(findShadowDepartment.getName());
										
										}
//									teamMemberDTO.setShadowEmpId(teamMemberObj.getShadowEmpId()!= null ? Long.parseLong(teamMemberObj.getShadowEmpId().toString()):null);
>>>>>>> Stashed changes
//									teamMemberDTO.setBillable(teamMemberObj.getBillable());
//									teamMemberDTO.setBillableType(teamMemberObj.getBillableType());
//									teamMemberDTO.setShadowBillable(teamMemberObj.getShadowBillable());
//									teamMemberDTO.setShadowBillableType(teamMemberObj.getShadowBillableType());
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
//					projectDto.setProjectManager(object[2] != null ? object[2].toString() : null);
					projectDto.setCreatedOn(object[2] != null ? object[2].toString() : null);
					projectDto.setClientName(object[3] != null ? object[3].toString() : null);			
					projectDto.setClientState(object[4] != null ? object[4].toString() : null);
					projectDto.setIsDraftProject(object[5] != null ? object[5].toString() : null);
					projectDto.setPoProjectId(object[6] != null ? Long.parseLong(object[6].toString()) : null);	
					projectDto.setIsActive(object[7] != null ? Long.parseLong(object[7].toString()): null);
					projectDto.setId(object[6] != null ? Long.parseLong(object[6].toString()) : null);				
					List<TeamSpocDTO> spocList = getTeamSpocsByProjectId(projectId);
					projectDto.setTeamSpocs(spocList);
					
					List<Object[]> result = projectManagerMappingRepository.findProjectManagersPerProject(Long.parseLong(projectId.toString()));

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
	
	
	public List<TeamSpocDTO> getTeamSpocsByProjectId(Integer projectId) {
	    List<Object[]> result = teamRepository.findTeamsAndSpocsByProjectId(projectId);
	    List<TeamSpocDTO> spocList = new ArrayList<>();

	    for (Object[] obj : result) {
	        TeamSpocDTO dto = new TeamSpocDTO();
	        dto.setTeamId(obj[0] != null ? Long.parseLong(obj[0].toString()) : null);
	        dto.setTeamName(obj[1] != null ? obj[1].toString() : null);
	        dto.setSpocId(obj[2] != null ? Long.parseLong(obj[2].toString()) : null);
	        dto.setSpocName(obj[3] != null ? obj[3].toString() : null);
	        dto.setDepartmentList(obj[4]!= null ? obj[4].toString().split(",") : null);      
	        spocList.add(dto);
	    }

	    return spocList;
	}
	
	public List<ProjectManagersDTO> getProjectManagersByProjectId(Integer projectId) {
	    List<Object[]> result = projectManagerMappingRepository.findProjectManagersPerProject(Long.parseLong(projectId.toString()));
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
						
						if(team.getCreatedBy() != null) {
							String createdBy = getEmploymentId(team.getCreatedBy());
							poTeamDTO.setCreatedBy(createdBy);
						}
						
						if(team.getUpdatedBy() != null) {
							String updatedBy = getEmploymentId(team.getCreatedBy());
							poTeamDTO.setUpdatedBy(updatedBy);
						}
						
						if(team.getUpdatedOn() != null) {
							poTeamDTO.setUpdatedOn(team.getUpdatedOn().toString());
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
				}catch(HttpServerErrorException e) {
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
//					projectDTO.setProjectManager(object[10] != null ? "A-".concat(object[10].toString()) : null);
					projectDTO.setProjectManagerName(object[2] != null ? object[2].toString() : null);
					projectDTO.setProjectId(object[3] != null ? Integer.parseInt(object[3].toString()) : null);
					projectDTO.setStatus(object[11] != null ? object[11].toString() : null);
					projectDTO.setIsActive(object[12] != null ? Long.parseLong(object[12].toString()) :null);
					//projectDTO.setIsActive(2l);
					projectDTO.setEmpId(object[13] != null ? Long.parseLong(object[13].toString()) :null);
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
			dto.setStatus(obj[13] != null ? obj[13].toString() : null);
			
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
			System.err.println("findAllMappedEmp  "+teamDto.getEndDate());
			 if(teamDto.getEndDate() != null) {
 				String str = teamDto.getEndDate();
 				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
 				LocalDate date = LocalDate.parse(str, formatter);
 				LocalDateTime endDateTime = date.atStartOfDay();

 				emp.setEndDate(endDateTime);
 			}else {
 				emp.setEndDate(LocalDateTime.now());
 			}
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
	
	public ServiceResponse syncPoProjectDetailsByProjectId(ResourceManagementDTO dto) {
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("syncPoProjectDetailsByProjectId");
        apiLogInfo.setApiUrl("/api/syncPoProjectDetailsByProjectId");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("syncPoProjectDetailsByProjectId "+dto.getId());

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
                //departmentIds
                String[] departmentArray = dto.getDepartment();

                List<String> departmentList = Arrays.stream(departmentArray)
                    .flatMap(department -> Arrays.stream(department.split(",")))
                    .map(String::trim) 
                    .collect(Collectors.toList());

                List<String> deptIds = new ArrayList<String>();
                if(!departmentList.isEmpty()) {
                	departmentList.forEach(dept->{
                		Department deptList =  departmentRepository.findByName(dept);
                		if(deptList == null) {
            		        logBuilder.append("No Dept Id fetched");
                		}
                		else {
                			deptIds.add(deptList.getDeptId().toString());
                		}
                	});
                	 if(!deptIds.isEmpty()) {
	                    	String deptIdStr = String.join(", ", deptIds);
	                        project.setDeptId(deptIdStr); 
	                    }
                }
               
                //clientId
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
//				dto.setProjectManagerId(object[2] != null ?  Long.parseLong(object[2].toString()) : null);
//				dto.setProjectManagerName(object[3] != null ? object[3].toString() : null);	
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

public ServiceResponse getPoProjectInfo(ResourceManagementDTO resourceManagementDTO) {
	
	ServiceResponse response = new ServiceResponse();
	LogDTO apiLogInfo = new LogDTO();
    apiLogInfo.setSubFeatureName("Project 360");
    apiLogInfo.setApiUrl("/api/getPoProjectInfo");
    apiLogInfo.setLogLevel("INFO");
    StringBuilder logBuilder = new StringBuilder();
    logBuilder.append("projectInfo : "+ projectRepository.getAllInternalProject().size());
    
	try {
		List<Object[]> projectInfo = projectRepository.getPoProjectInfo(resourceManagementDTO.getProjectId());
		List<ResourceManagementDTO> result = new ArrayList<>();
		if(!projectInfo.isEmpty()) {
			projectInfo.forEach(object ->{
				ResourceManagementDTO dto = new ResourceManagementDTO();
				
				dto.setProjectId(object[0] != null ? Integer.parseInt(object[0].toString()) : null);
//				dto.setProjectName(object[1] != null ? object[1].toString() : null);		
//				dto.setProjectManagerId(object[2] != null ?  Long.parseLong(object[2].toString()) : null);
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
	            
	            String text = "<p>Dear BD Team,</p>"
	                    + "<p>We hope this email finds you well.</p>"
	                    + "<p>The Purchase Order (PO) for the project <b>" + resourceManagementDTO.getName() + "</b> has expired. Below are the PO details:</p>"
	                    + "<ul>"
	                    + "<li><b>PO Number:</b> " + resourceManagementDTO.getPoNo() + "</li>"
	                    + "<li><b>Start Date:</b> " + ZonedDateTime.parse(resourceManagementDTO.getStartDate(), inputFormatter)
                        .format(outputFormatter) + "</li>"
	                    + "<li><b>End Date:</b> " + ZonedDateTime.parse(resourceManagementDTO.getEndDate(), inputFormatter)
                        .format(outputFormatter) + "</li>"
	                    + "</ul>"
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
	        	
	        	List<Object[]> employees = employeeRepository.getEmployeeByNameAndEmpld();
	            List<GetEmployeeByNameAndEmpldDTO> employeeDTOList = new ArrayList<>();

	            employees.forEach(object -> {
	                GetEmployeeByNameAndEmpldDTO dto = new GetEmployeeByNameAndEmpldDTO();
	                
	                dto.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
	                dto.setName(object[1] != null ? object[1].toString() : null);
	                dto.setEmploymentId(object[2] != null ? object[2].toString() : null);

	                employeeDTOList.add(dto);
	            });

	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse(employeeDTOList);
	            
	        } catch (Exception e) {
	            e.printStackTrace();
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Error : " + e.getMessage());
	        }
	        
	        return response;
	 }
	 @Transactional
	 public ServiceResponse combinedPOINTERNALList(ProjectFilterDTO projectFilterDTO) {
	     ServiceResponse response = new ServiceResponse();
	     StringBuilder logBuilder = new StringBuilder();
	     LogDTO apiLogInfo = new LogDTO();
	     apiLogInfo.setLogLevel("INFO");

	     try {
	    	 
	    	 String approvalStatus = projectFilterDTO.getApprovalStatus();
	    	 Long currentUserEmpId = projectFilterDTO.getCurrentUserEmpId();
	    	 List<Long> departmentsids = projectFilterDTO.getDepartmentsids()  ;      
	    	 List<ResourceManagementDTO> poPortalProjects = fetchPoPortalProjects();
	         ServiceResponse internalProjectResponse = getInternalProject();

	         if (internalProjectResponse.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS)) {
	             List<ResourceManagementDTO> internalProjects = (List<ResourceManagementDTO>) internalProjectResponse.getServiceResponse();
	             ServiceResponse teamCreatedProjectsResponse = alreadyCreatedTeam();

	             if (teamCreatedProjectsResponse.getServiceStatus().equals(ServiceResponse.STATUS_SUCCESS)) {
	                 List<ResourceManagementDTO> teamCreatedProjects = (List<ResourceManagementDTO>) teamCreatedProjectsResponse.getServiceResponse();
	                 
	                 

	                 processPoPortalProjects(poPortalProjects, teamCreatedProjects);
	                 processInternalProjects(internalProjects, teamCreatedProjects);

	                
	                 List<ResourceManagementDTO> combinedProjects = new ArrayList<>();
	                     combinedProjects.addAll(poPortalProjects);
	                     combinedProjects.addAll(internalProjects);
	                     
	                     Employee currentUser = employeeRepository.findByEmpId(currentUserEmpId);
	                     boolean isSuperAdminOrDirector = false;
	                     boolean isSpoc = false;
	                     boolean isHod = false;

	                     if (currentUser != null && currentUser.getJobRoleId() != null) {
	                         JobRole jobRole = jobRoleRepository.findByjobRoleId(currentUser.getJobRoleId());
	                         if (jobRole != null) {
	                             String role = jobRole.getEmployeeRole();
	                             String name = jobRole.getName();
	                             if ("SuperAdmin".equalsIgnoreCase(role) || "Director".equalsIgnoreCase(name) || "SuperAdmin".equalsIgnoreCase(name)) {
	                                 isSuperAdminOrDirector = true;
	                             }
	                         }
	                     }

	                     // SPOC Check
	                     isSpoc = teamCreatedProjects.stream()
	                             .anyMatch(team -> team.getTeamSpocs() != null &&
	                                     team.getTeamSpocs().stream()
	                                             .anyMatch(spoc -> spoc.getSpocId() != null && spoc.getSpocId().equals(currentUserEmpId)));

	                     // HOD Check
	                     List<Long> hodDeptIds = departmentRepository.findByHodId(currentUserEmpId)
	                             .stream().map(Department::getDeptId).collect(Collectors.toList());

	                     isHod = !hodDeptIds.isEmpty();

	                     // Now apply role-specific filtering
	                     if (isSuperAdminOrDirector) {
	                         // Super Admin, Director, and all remaining users → No filtering needed
	                         // Return all combinedProjects as-is
	                     }
	                     else if (isSpoc && !isSuperAdminOrDirector) {
	                         Set<Long> spocPoProjectIds = teamCreatedProjects.stream()
	                                 .filter(team -> team.getTeamSpocs() != null &&
	                                         team.getTeamSpocs().stream()
	                                                 .anyMatch(spoc -> spoc.getSpocId() != null && spoc.getSpocId().equals(currentUserEmpId)))
	                                 .map(ResourceManagementDTO::getPoProjectId)
	                                 .filter(Objects::nonNull)
	                                 .collect(Collectors.toSet());

	                         Set<Integer> spocInternalProjectIds = teamCreatedProjects.stream()
	                                 .filter(team -> team.getTeamSpocs() != null &&
	                                         team.getTeamSpocs().stream()
	                                                 .anyMatch(spoc -> spoc.getSpocId() != null && spoc.getSpocId().equals(currentUserEmpId)))
	                                 .map(ResourceManagementDTO::getProjectId)
	                                 .filter(Objects::nonNull)
	                                 .collect(Collectors.toSet());

	                         combinedProjects = combinedProjects.stream()
	                                 .filter(project -> "Internal".equalsIgnoreCase(project.getIsDraftProject())
	                                         || "Not Started".equalsIgnoreCase(project.getIsDraftProject())
	                                         || (project.getId() != null && spocPoProjectIds.contains(project.getId()))
	                                         || (project.getProjectId() != null && spocInternalProjectIds.contains(project.getProjectId())))
	                                 .collect(Collectors.toList());
	                     }
	                     else if (isHod && !isSuperAdminOrDirector) {
	                         Set<Long> hodPoProjectIds = teamCreatedProjects.stream()
	                                 .filter(team -> team.getTeamSpocs() != null &&
	                                         team.getTeamSpocs().stream().anyMatch(spoc ->
	                                                 spoc.getDepartmentList() != null &&
	                                                 Arrays.stream(spoc.getDepartmentList())
	                                                         .anyMatch(deptId -> hodDeptIds.contains(Long.valueOf(deptId)))))
	                                 .map(ResourceManagementDTO::getPoProjectId)
	                                 .filter(Objects::nonNull)
	                                 .collect(Collectors.toSet());

	                         Set<Integer> hodInternalProjectIds = teamCreatedProjects.stream()
	                                 .filter(team -> team.getTeamSpocs() != null &&
	                                         team.getTeamSpocs().stream().anyMatch(spoc ->
	                                                 spoc.getDepartmentList() != null &&
	                                                 Arrays.stream(spoc.getDepartmentList())
	                                                         .anyMatch(deptId -> hodDeptIds.contains(Long.valueOf(deptId)))))
	                                 .map(ResourceManagementDTO::getProjectId)
	                                 .filter(Objects::nonNull)
	                                 .collect(Collectors.toSet());

	                         combinedProjects = combinedProjects.stream()
	                                 .filter(project -> "Internal".equalsIgnoreCase(project.getIsDraftProject())
	                                         || "Not Started".equalsIgnoreCase(project.getIsDraftProject())
	                                         || (project.getId() != null && hodPoProjectIds.contains(project.getId()))
	                                         || (project.getProjectId() != null && hodInternalProjectIds.contains(project.getProjectId())))
	                                 .collect(Collectors.toList());
	                     }
	                     
	                   
	                 
		                 if (departmentsids != null && !departmentsids.isEmpty()) {
		                	    List<String> departmentIdStrings = departmentsids.stream()
		                	            .map(String::valueOf)
		                	            .map(String::trim)
		                	            .collect(Collectors.toList());

		                	    combinedProjects = combinedProjects.stream()
		                	            .filter(project -> project.getTeamSpocs() != null &&
		                	                    project.getTeamSpocs().stream().anyMatch(spoc ->
		                	                            spoc.getDepartmentList() != null &&
		                	                            Arrays.stream(spoc.getDepartmentList())
		                	                                    .anyMatch(deptId -> departmentIdStrings.contains(deptId.trim()))
		                	                    )
		                	            )
		                	            .collect(Collectors.toList());
		                	}
		                 
		                 int pendingForApprovalCountBefore = (int) combinedProjects.stream()
		                         .filter(project -> "Pending For Approval".equalsIgnoreCase(project.getIsDraftProject()))
		                         .count();

		                 int approvedCountBefore = (int) combinedProjects.stream()
		                         .filter(project -> "Approved".equalsIgnoreCase(project.getIsDraftProject()))
		                         .count();

		                 int notStartedCountBefore = (int) combinedProjects.stream()
		                         .filter(project -> "Not Started".equalsIgnoreCase(project.getIsDraftProject()) ||
		                                            "Internal".equalsIgnoreCase(project.getIsDraftProject()))
		                         .count();

		                 int rejectedCountBefore = (int) combinedProjects.stream()
		                         .filter(project -> "Rejected".equalsIgnoreCase(project.getIsDraftProject()))
		                         .count();  
		                 
		                 
	                 
	             
	              
	                
	                 if ("Pending For Approval".equalsIgnoreCase(approvalStatus)) {
	                     combinedProjects = combinedProjects.stream()
	                             .filter(project -> "Pending For Approval".equalsIgnoreCase(project.getIsDraftProject()))
	                             .collect(Collectors.toList());
	                 } else if ("Approved".equalsIgnoreCase(approvalStatus)) {
	                     combinedProjects = combinedProjects.stream()
	                             .filter(project -> "Approved".equalsIgnoreCase(project.getIsDraftProject()))
	                             .collect(Collectors.toList());
	                 } else if ("Not Started".equalsIgnoreCase(approvalStatus)) {
	                     combinedProjects = combinedProjects.stream()
	                             .filter(project -> "Not Started".equalsIgnoreCase(project.getIsDraftProject()) || "Internal".equalsIgnoreCase(project.getIsDraftProject()))
	                             .collect(Collectors.toList());
	                 } else if ("Rejected".equalsIgnoreCase(approvalStatus)) {
	                     combinedProjects = combinedProjects.stream()
	                             .filter(project -> "Rejected".equalsIgnoreCase(project.getIsDraftProject()))
	                             .collect(Collectors.toList());
	                 }

	               
	                 if ("All".equalsIgnoreCase(approvalStatus)) {
	                	
	                 }
	                 
	                

	                                 

	                
	                 Map<String, Integer> countsMap = new HashMap<>();
	             
	                	 countsMap.put("pendingForApprovalCount", pendingForApprovalCountBefore);
		                 countsMap.put("approvedCount", approvedCountBefore);
		                 countsMap.put("notStartedCount", notStartedCountBefore);
		                 countsMap.put("rejectedCount", rejectedCountBefore);	 
	               


	                
	                 combinedProjects.sort((a, b) -> {
	                     try {
	                         String createdOnStrA = a.getCreatedOn();
	                         String createdOnStrB = b.getCreatedOn();

	                         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	                         Date createdOnA = (createdOnStrA != null && !createdOnStrA.isEmpty()) ? sdf.parse(createdOnStrA) : null;
	                         Date createdOnB = (createdOnStrB != null && !createdOnStrB.isEmpty()) ? sdf.parse(createdOnStrB) : null;

	                         if (createdOnA == null && createdOnB == null) return 0;
	                         if (createdOnA == null) return 1;
	                         if (createdOnB == null) return -1;

	                         return createdOnB.compareTo(createdOnA); 

	                     } catch (Exception e) {
	                         e.printStackTrace();
	                         return 0;
	                     }
	                 });
	                 
	                 CombinedPOInternalProjectResponse combinedProjectResponse = new CombinedPOInternalProjectResponse();
	                 combinedProjectResponse.setCombinedProjects(combinedProjects);
	                 combinedProjectResponse.setCounts(countsMap);
	                 

	                 response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                 response.setServiceResponse(combinedProjectResponse);
//	                 response.setServiceResponse(teamCreatedProjects);
	                 apiLogInfo.setApiResponse("Filtered and combined project info list size: " + combinedProjects.size());
	                 apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	             } else {
	                 response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                 response.setServiceResponse("Failed to fetch already created team projects.");
	                 apiLogInfo.setApiResponse("Failed to fetch already created team projects.");
	                 apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	             }
	         } else {
	             response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	             response.setServiceResponse("Failed to fetch internal projects.");
	             apiLogInfo.setApiResponse("Failed to fetch internal projects.");
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


	 private List<ResourceManagementDTO> fetchPoPortalProjects() {
		    List<ResourceManagementDTO> poPortalProjects = new ArrayList<>();
		    try {
		        ResourceManagementDTO[] poPortalProjectArray = restTemplate.getForObject(
		                allPoPortalProjects, ResourceManagementDTO[].class);
		        poPortalProjects = Arrays.asList(poPortalProjectArray != null ? poPortalProjectArray : new ResourceManagementDTO[0]);
		    } catch (RestClientException e) {
		        throw new RuntimeException("Error fetching projects from PoPortal: " + e.getMessage());
		    }
		    return poPortalProjects;
		}
	 
	 
	 private void processPoPortalProjects(List<ResourceManagementDTO> poPortalProjects,
             List<ResourceManagementDTO> teamCreatedProjects) {
                for (ResourceManagementDTO proj : poPortalProjects) {
                    ResourceManagementDTO selectedProj = teamCreatedProjects.stream()
                        .filter(projTeam -> proj.getId() != null && proj.getId().equals(projTeam.getPoProjectId()))
                         .findFirst()
                         .orElse(null);
                    
                    if (selectedProj != null) {
                        proj.setIsTeamCreated("true");
                        proj.setProjectManagers(selectedProj.getProjectManagers());
                        proj.setProjectManagerId(selectedProj.getProjectManagerId());
                        proj.setProjectManagerName(selectedProj.getProjectManagerName());
                        proj.setTeamSpocs(selectedProj.getTeamSpocs());                      
                        
                        
                        
                        if (selectedProj.getIsActive() != null && selectedProj.getIsActive() == 2) {
                            proj.setIsDraftProject("Pending For Approval");
                        } else {
                            if ("Rejected".equalsIgnoreCase(selectedProj.getIsDraftProject())) {
                                proj.setIsDraftProject("Rejected");
                            } else if ("false".equalsIgnoreCase(selectedProj.getIsDraftProject())) {
                                proj.setIsDraftProject("Approved");
                            } else {
                                proj.setIsDraftProject("Not started");
                            }
                        }
                    } else {
                        proj.setIsTeamCreated("false");
                        proj.setIsDraftProject("Not Started");
                    }
                    
                    if (proj.getStatus() != null) {
                        if ("true".equals(proj.getStatus())) {
                            proj.setStatus("InProgress");
                        } else if ("false".equals(proj.getStatus())) {
                            proj.setStatus("Completed");
                        }
                    }
                }
            }
	 
	 
	 private void processInternalProjects(List<ResourceManagementDTO> internalProjects,
             List<ResourceManagementDTO> teamCreatedProjects) {
                      for (ResourceManagementDTO proj : internalProjects) {
                            ResourceManagementDTO selectedProj = teamCreatedProjects.stream()
                             .filter(projTeam -> proj.getProjectId() != null && proj.getProjectId().equals(projTeam.getProjectId()))
                             .findFirst()
                             .orElse(null);
                            
                            if (selectedProj != null) {
                                proj.setIsTeamCreated("true");
                                proj.setProjectManagers(selectedProj.getProjectManagers());
                                proj.setProjectManagerId(selectedProj.getProjectManagerId());
                                proj.setProjectManagerName(selectedProj.getProjectManagerName());
                                proj.setTeamSpocs(selectedProj.getTeamSpocs());    
                                if ("true".equalsIgnoreCase(selectedProj.getIsDraftProject())) {
                                    proj.setIsDraftProject("Pending For Approval");
                                } else {
                                    if ("Rejected".equalsIgnoreCase(selectedProj.getIsDraftProject())) {
                                        proj.setIsDraftProject("Rejected");
                                    } else if ("false".equalsIgnoreCase(selectedProj.getIsDraftProject())) {
                                        proj.setIsDraftProject("Approved");
                                    } else {
                                        proj.setIsDraftProject("Not Started");
                                    }
                                }
                            } else {
                                proj.setIsTeamCreated("false");
                                proj.setIsDraftProject(proj.getProjectType());
                            }
                            
                            if (proj.getStatus() != null) {
                                if ("true".equals(proj.getStatus())) {
                                    proj.setStatus("InProgress");
                                } else if ("false".equals(proj.getStatus())) {
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

		    for (ResourceManagementDTO resourceManagementDTO : resourceManagementDTOList) {
		       
		        EmployeeTeamMap findResource = employeeTeamMapRepository
		                .findByEmpIdAndTeamIdAndActiveStatus(resourceManagementDTO.getEmpId(), resourceManagementDTO.getTeamId());
		        Team findTeam = teamRepository.findTeamByTeamId(resourceManagementDTO.getTeamId());
		        Employee emp = employeeRepository.findByEmpId(resourceManagementDTO.getEmpId());        	
         	Project findProject = projectRepository.findByProjectId(findTeam.getProjectId());


		        if (findResource != null) {
		            try {
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
							mailService.sendMail(rmgMail,"Regarding Resource removed from Project ", "Dear "
									+ emp.getName()+"<br>"
									+ "You have been removed from project "+findProject.getProjectName()+ "under the team - "+findTeam.getTeamName()+"<br>"
											+ "<br><br>"
											+ "Sincerely,"+"<br>"
											+ "Team RMG - ApMoSys Technologies"
									);
						} catch (AddressException e) {
							
							e.printStackTrace();
						} catch (MessagingException e) {
							
							e.printStackTrace();
						}
		               
		                resultMessage.append("Resource with EmpId " + resourceManagementDTO.getEmpId() + " from Team " +
		                        findTeam.getTeamName() + " removed successfully.\n");

		            } catch (Exception e) {
		                failureCount++;
		                resultMessage.append("Failed to remove resource with EmpId " + resourceManagementDTO.getEmpId() +
		                        " from Team " + findTeam.getTeamName() + ". Error: " + e.getMessage() + "\n");
		            }
		        } else {
		            failureCount++;
		            resultMessage.append("No resource found with EmpId " + resourceManagementDTO.getEmpId() +
		                    " in Team " + findTeam.getTeamName() + ".\n");
		        }
		    }

		    if (failureCount == 0) {
		        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		        response.setServiceResponse("Successfully removed resources");
		    } else {
		        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		        response.setServiceResponse(resultMessage.toString());
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
			            List<EmployeeTeamMap> findAllMappedEmp = employeeTeamMapRepository.findByTeamId(teamDto.getTeamId());
			            System.err.println("findAllMappedEmp for Team: " + teamDto.getTeamName() + " -> " + findAllMappedEmp);

			            findAllMappedEmp.forEach(emp -> {
			                emp.setActive(0L); 
			                if(teamDto.getEndDate() != null) {
			    				String str = teamDto.getEndDate();
			    				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			    				LocalDate date = LocalDate.parse(str, formatter);
			    				LocalDateTime endDateTime = date.atStartOfDay();

			    				emp.setEndDate(endDateTime);
			    			}else {
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

			        mailService.sendMail("sakti.das@apmosys.com",
			                "Regarding Resource management",
			                "Dear RMG Team, <br><br>" +
			                        "The following teams have been deleted, and the resources have been removed from these teams: " + deletedTeamsList.toString() +
			                        "<br><br>Sincerely,<br>Team RMG - ApMoSys Technologies");
			    } catch (Exception e) {
			        e.printStackTrace();
			    }
		   } catch(Exception e) {
			   
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
				EmployeeTeamMap findResource = employeeTeamMapRepository.findByEmpIdAndTeamId(empid,teamid);
				Team findTeam = teamRepository.findTeamByTeamId(resourceManagementDTO.getTeamId());
		
				System.out.println("findResource: " + findResource);
		
				if (findResource != null) {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		
					if (resourceManagementDTO.getEndDate() != null) {
		//                String str = resourceManagementDTO.getEndDate();
		//                LocalDate date = LocalDate.parse(str, formatter);
		//                LocalDateTime endDateTime = date.atStartOfDay();
		//                findResource.setEndDate(endDateTime); 
						 String str = resourceManagementDTO.getEndDate();
							LocalDateTime endDateTime;
		
							if (str.contains("T")) {
								endDateTime = LocalDateTime.parse(str);
							} else {
								LocalDate date = LocalDate.parse(str);
								endDateTime = date.atTime(LocalTime.now().getHour(), LocalTime.now().getMinute(), LocalTime.now().getSecond());
							}
							findResource.setEndDate(endDateTime);
					} else if (resourceManagementDTO.getStartDate() != null) {
		
						String str = resourceManagementDTO.getStartDate();
						LocalDateTime startDateTime;
		
						if (str.contains("T")) {
							startDateTime = LocalDateTime.parse(str);
						} else {
							 LocalDate date = LocalDate.parse(str);
							 startDateTime = date.atTime(LocalTime.now().getHour(), LocalTime.now().getMinute(), LocalTime.now().getSecond());             
						   }
		
						findResource.setStartDate(Timestamp.valueOf(startDateTime));
		//                String str = resourceManagementDTO.getStartDate(); 
		//                LocalDate date = LocalDate.parse(str, formatter);
		//                LocalDateTime startDateTime = date.atStartOfDay();
						//findResource.setStartDate(Timestamp.valueOf(startDateTime)); 
					} else {
						findResource.setEndDate(LocalDateTime.now()); 
					}
		
					employeeTeamMapRepository.save(findResource);
		
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Project StartDate/EndDate updated successfully, from Team Name - " + findTeam.getTeamName());
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
		
		
		public ServiceResponse completionDateOfProject(ResourceManagementDTO resourceManagementDTO) {
			
			ServiceResponse response = new ServiceResponse();
		       LogDTO apiLogInfo = new LogDTO();
		       apiLogInfo.setSubFeatureName("completionDateOfProject");
		       apiLogInfo.setApiUrl("/api/completionDateOfProject");
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
				    	System.err.print(resourceManagementDTO.getId());
				        projObj = projectRepository.findByPoProjectId(resourceManagementDTO.getId());
				    }
				    
				    if(projObj == null) {
				    	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			            response.setServiceResponse("No Project Detais Is Present ");
			            apiLogInfo.setApiResponse("No Project Detais Is Present");
			            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			            return response;
				    }
				    Project projectObj =projObj;
				   
				    projectObj.setProjectCompletionDate(resourceManagementDTO.getProjectCompletionDate());				    projectObj.setProjectStatus(resourceManagementDTO.getStatus());
				    projectObj.setProjectStatus(resourceManagementDTO.getProjectStatus());
				    Project projectDbResponse = projectRepository.save(projectObj);
				    if(projectDbResponse != null) {
				    	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			            response.setServiceResponse("Project Status Updated As Completed !!");
			            apiLogInfo.setApiResponse("Project Status Updated As Completed");
			            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
						
				    }else {
				    	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			            response.setServiceResponse("Project Status Not Updated");
			            apiLogInfo.setApiResponse("Project Status Not Updated to Completed");
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
			return response;		
		}
		
		
//		public ServiceResponse getAllInternalProjectsNewRMG(ProjectFilterDTO projectFilterDTO) {
//			ServiceResponse response = new ServiceResponse();
//			try {
//			Employee employee = employeeRepository.findByEmpId(projectFilterDTO.getCurrentUserEmpId());
//			JobRole jobRole = jobRoleRepository.findByjobRoleId(employee.getJobRoleId());
//			
//			String role = jobRole.getEmployeeRole();
//			String name = jobRole.getName();
//			   Set<Integer> internalProjectIds = new HashSet<>();
//			   if (role.equalsIgnoreCase("SuperAdmin") || name.equalsIgnoreCase("Director") || name.equalsIgnoreCase("Super Admin")) {
//				   internalProjectIds = projectRepository.findAllActiveInternalProjectIds();
//			   } else if (teamRepository.existsBySpocId(projectFilterDTO.getCurrentUserEmpId())) {
//	                internalProjectIds = teamRepository.findActiveInternalProjectIdsBySpocId(projectFilterDTO.getCurrentUserEmpId());
//			   } else if (departmentRepository.existsByHodId(projectFilterDTO.getCurrentUserEmpId())) {
//	                List<Long> deptIds = departmentRepository.findDeptIdsByHodId(projectFilterDTO.getCurrentUserEmpId());
//	                List<Team> activeTeams = teamRepository.findAllActiveTeamsOfInternalProjects();
//	                
//	                internalProjectIds = activeTeams.stream()
//	                        .filter(team -> {
//	                            String teamDeptIdsStr = team.getDeptIds();
//	                            if (teamDeptIdsStr == null || teamDeptIdsStr.isBlank()) return false;
//
//	                            List<Long> teamDeptIds = Arrays.stream(teamDeptIdsStr.split(","))
//	                                                           .map(String::trim)
//	                                                           .filter(s -> !s.isEmpty())
//	                                                           .map(Long::parseLong)
//	                                                           .collect(Collectors.toList());
//
//	                            return teamDeptIds.stream().anyMatch(deptIds::contains);
//	                        })
//	                        .map(Team::getProjectId)
//	                        .collect(Collectors.toSet());
//
//	            }else {
//	                internalProjectIds = projectRepository.findAllActiveInternalProjectIds();
//	            }
//			   
//			   List<RMGProjectMappedEmployees> result = new ArrayList<>();
//			   if(internalProjectIds != null) {
//			   for (Integer projectId : internalProjectIds) {
//	                List<Team> teams = teamRepository.findActiveTeamsByProjectId(projectId);
//	                if (teams == null || teams.isEmpty()) continue;
//	                for (Team team : teams) {
//	                	 if (team == null) continue;
//	                    List<EmployeeTeamMap> mappings = employeeTeamMapRepository.findByTeamIdWhereEmployeesAreActive(team.getTeamId());
//	                    if (mappings == null || mappings.isEmpty()) continue;
//	                    for (EmployeeTeamMap map : mappings) {
//	                    	 if (map == null) continue;
//	                        Employee emp = employeeRepository.findByEmpId(map.getEmpId());
//	                        if (emp == null) continue;
//	                        JobRole jr = jobRoleRepository.findByjobRoleId(emp.getJobRoleId()); 
//	                        if (jr == null) continue;
//	                        String deptName = departmentRepository.findDepartmentNameFromDeptId(jr.getDeptId());
//	                        if (deptName == null) deptName = "";
//	                        RMGProjectMappedEmployees dto = new RMGProjectMappedEmployees();
//	                        dto.setEmpId(emp.getEmpId());
//	                        dto.setEmployeementId(emp.getEmployeementId());
//	                        dto.setName(emp.getName());
//	                        dto.setEmploymentstatus(emp.getEmploymentstatus());
//	                        dto.setBillable(emp.getBillable());
//	                        dto.setBillableType(emp.getBillableType());
//	                        dto.setDepartment(deptName);
//
//	                        RMGProject rmgProject = new RMGProject();
//	                        Project project = projectRepository.findByProjectId(projectId);                     		
//	                        		
//	                        if (project == null) continue;
//
//	                        Client client = clientsRepository.findByClientId(project.getClientId());                     
////	                        ClientLocation clientLocation = clientLocationRepository.findByClientIdd(project.getClientId());                      
//	                        		
//	                        rmgProject.setProjectId(project.getProjectId());
//	                        rmgProject.setClientName(client != null ? client.getClientName() : "");
////	                        rmgProject.setClientLocation(clientLocation !=null ? clientLocation.getClientLocation() : "");
//	                        rmgProject.setProjectName(project.getProjectName());
//	                        rmgProject.setPoProjectId(project.getPoProjectId());
//	                        rmgProject.setPoStartDate(project.getPoStartDate());
//	                        rmgProject.setPoEndDate(project.getPoEndDate());
//	                        rmgProject.setApmosysRM(project.getApmosysRM());
//	                        rmgProject.setClientRM(project.getClientRM());
//	                        rmgProject.setPoProjectType(project.getPoProjectType());
//	                        rmgProject.setPoNo(project.getPoNo());
//	                        rmgProject.setActive("true".equalsIgnoreCase(project.getActive()) ? "true" : "false");
//
//	                        RMGTeam rmgTeam = new RMGTeam();
//	                        rmgTeam.setTeamId(team.getTeamId());
//	                        rmgTeam.setTeamName(team.getTeamName());
//	                        rmgTeam.setIsActive(team.getIsActive());
//	                        rmgTeam.setEmployeeRole(map.getEmployeeRole());
//	                        rmgTeam.setStatus(map.getActive() == 1 ? "Approved" : "Pending for Approval");
//	                        rmgTeam.setDepartmentList(team.getDeptIds().split(","));
//
//	                        rmgProject.setRmgTeam(List.of(rmgTeam));
//	                        dto.setRmgprojects(List.of(rmgProject));
//
//	                        result.add(dto);
//	                    }
//	                }
//	            }
//			}
//
//	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//	            response.setServiceResponse(result);
//	        } catch (Exception e) {
//	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//	            response.setServiceResponse("Error: " + e.getMessage());
//	        }
//	        return response;
//	    }
		
		public ServiceResponse getAllInternalProjectsNewRMG(ProjectFilterDTO projectFilterDTO) {
		    ServiceResponse response = new ServiceResponse();
		    if ("Not Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
		        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		        response.setServiceResponse(Collections.emptyList());
		        return response;
		    }

		    try {
		        Employee employee = employeeRepository.findByEmpId(projectFilterDTO.getCurrentUserEmpId());
		        JobRole jobRole = jobRoleRepository.findByjobRoleId(employee.getJobRoleId());

		        String role = jobRole.getEmployeeRole();
		        String name = jobRole.getName();

		        Set<Integer> internalProjectIds = new HashSet<>();
		        if (role.equalsIgnoreCase("SuperAdmin") || name.equalsIgnoreCase("Director") || name.equalsIgnoreCase("Super Admin")) {
		            internalProjectIds = projectRepository.findAllActiveInternalProjectIds();
		        } else if (teamRepository.existsBySpocId(projectFilterDTO.getCurrentUserEmpId())) {
		            internalProjectIds = teamRepository.findActiveInternalProjectIdsBySpocId(projectFilterDTO.getCurrentUserEmpId());
		        } else if (departmentRepository.existsByHodId(projectFilterDTO.getCurrentUserEmpId())) {
		            List<Long> deptIds = departmentRepository.findDeptIdsByHodId(projectFilterDTO.getCurrentUserEmpId());
		            List<Team> activeTeams = teamRepository.findAllActiveTeamsOfInternalProjects();

		            internalProjectIds = activeTeams.stream()
		                    .filter(team -> {
		                        String teamDeptIdsStr = team.getDeptIds();
		                        if (teamDeptIdsStr == null || teamDeptIdsStr.isBlank()) return false;

		                        List<Long> teamDeptIds = Arrays.stream(teamDeptIdsStr.split(","))
		                                .map(String::trim)
		                                .filter(s -> !s.isEmpty())
		                                .map(Long::parseLong)
		                                .collect(Collectors.toList());

		                        return teamDeptIds.stream().anyMatch(deptIds::contains);
		                    })
		                    .map(Team::getProjectId)
		                    .collect(Collectors.toSet());
		        } else {
		            internalProjectIds = projectRepository.findAllActiveInternalProjectIds();
		        }
		        
		        internalProjectIds = filterProjectidsApprovalStatusDepartmentFilter(
		                internalProjectIds,
		                projectFilterDTO.getApprovalStatus(),
		                projectFilterDTO.getDepartmentsids()
		            );

		        Map<Long, RMGProjectMappedEmployees> employeeMap = new HashMap<>();

		        for (Integer projectId : internalProjectIds) {
		            List<Team> teams = teamRepository.findActiveTeamsByProjectId(projectId);
		            if (teams == null || teams.isEmpty()) continue;

		            for (Team team : teams) {
		                if (team == null) continue;

		                List<EmployeeTeamMap> mappings = employeeTeamMapRepository.findByTeamIdWhereEmployeesAreActive(team.getTeamId());
		                if (mappings == null || mappings.isEmpty()) continue;

		                for (EmployeeTeamMap map : mappings) {
		                    if (map == null) continue;

		                    Employee emp = employeeRepository.findByEmpId(map.getEmpId());
		                    if (emp == null) continue;

		                    JobRole jr = jobRoleRepository.findByjobRoleId(emp.getJobRoleId());
		                    if (jr == null) continue;

		                    String deptName = departmentRepository.findDepartmentNameFromDeptId(jr.getDeptId());
		                    if (deptName == null) deptName = "";

		                    RMGProjectMappedEmployees dto = employeeMap.get(emp.getEmpId());
		                    if (dto == null) {
		                        dto = new RMGProjectMappedEmployees();
		                        dto.setEmpId(emp.getEmpId());
		                        dto.setEmployeementId(emp.getEmployeementId());
		                        dto.setName(emp.getName());
		                        dto.setEmploymentstatus(emp.getEmploymentstatus());
		                        dto.setBillable(emp.getBillable());
		                        dto.setBillableType(emp.getBillableType());
		                        dto.setDepartment(deptName);
		                        dto.setRmgprojects(new ArrayList<>());
		                        employeeMap.put(emp.getEmpId(), dto);
		                    }

		                    RMGTeam rmgTeam = new RMGTeam();
		                    rmgTeam.setTeamId(team.getTeamId());
		                    rmgTeam.setTeamName(team.getTeamName());
		                    rmgTeam.setIsActive(team.getIsActive());
		                    rmgTeam.setEmployeeRole(map.getEmployeeRole());
		                    rmgTeam.setStatus(map.getActive() == 1 ? "Approved" : "Pending for Approval");
		                    rmgTeam.setDepartmentList(team.getDeptIds().split(","));

		                    RMGProject existingProject = dto.getRmgprojects().stream()
		                            .filter(p -> p.getProjectId().equals(projectId))
		                            .findFirst()
		                            .orElse(null);

		                    if (existingProject == null) {
		                        Project project = projectRepository.findByProjectId(projectId);
		                        if (project == null) continue;

		                        Client client = clientsRepository.findByClientId(project.getClientId());

		                        RMGProject rmgProject = new RMGProject();
		                        rmgProject.setProjectId(project.getProjectId());
		                        rmgProject.setClientName(client != null ? client.getClientName() : "");
		                        rmgProject.setProjectName(project.getProjectName());
		                        rmgProject.setPoProjectId(project.getPoProjectId());
		                        rmgProject.setPoStartDate(project.getPoStartDate());
		                        rmgProject.setPoEndDate(project.getPoEndDate());
		                        rmgProject.setApmosysRM(project.getApmosysRM());
		                        rmgProject.setClientRM(project.getClientRM());
		                        rmgProject.setPoProjectType(project.getPoProjectType());
		                        rmgProject.setPoNo(project.getPoNo());
		                        rmgProject.setActive("true".equalsIgnoreCase(project.getActive()) ? "true" : "false");
		                        rmgProject.setRmgTeam(new ArrayList<>());

		                        rmgProject.getRmgTeam().add(rmgTeam);
		                        dto.getRmgprojects().add(rmgProject);
		                    } else {
		                        existingProject.getRmgTeam().add(rmgTeam);
		                    }
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
		
		public ServiceResponse getAllShankhProjectsNewRMG(ProjectFilterDTO projectFilterDTO) {
		    ServiceResponse response = new ServiceResponse();
		    String approvalStatus = projectFilterDTO.getApprovalStatus();
		    List<Long> departmentsids = projectFilterDTO.getDepartmentsids();
		    if ("Not Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
		        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		        response.setServiceResponse(Collections.emptyList());
		        return response;
		    }
		    try {
		        Employee employee = employeeRepository.findByEmpId(projectFilterDTO.getCurrentUserEmpId());
		        JobRole jobRole = jobRoleRepository.findByjobRoleId(employee.getJobRoleId());

		        String role = jobRole.getEmployeeRole();
		        String name = jobRole.getName();

		        Set<Integer> internalProjectIds = new HashSet<>();
		        if (role.equalsIgnoreCase("SuperAdmin") || name.equalsIgnoreCase("Director") || name.equalsIgnoreCase("Super Admin")) {
		            internalProjectIds = projectRepository.findAllActiveShankhInternalProjectIds();
		        } else if (teamRepository.existsBySpocId(projectFilterDTO.getCurrentUserEmpId())) {
		            internalProjectIds = teamRepository.findActiveShankhInternalProjectIdsBySpocId(projectFilterDTO.getCurrentUserEmpId());
		        } else if (departmentRepository.existsByHodId(projectFilterDTO.getCurrentUserEmpId())) {
		            List<Long> deptIds = departmentRepository.findDeptIdsByHodId(projectFilterDTO.getCurrentUserEmpId());
		            List<Team> activeTeams = teamRepository.findAllActiveTeamsOfShankhInternalProjects();

		            internalProjectIds = activeTeams.stream()
		                    .filter(team -> {
		                        String teamDeptIdsStr = team.getDeptIds();
		                        if (teamDeptIdsStr == null || teamDeptIdsStr.isBlank()) return false;

		                        List<Long> teamDeptIds = Arrays.stream(teamDeptIdsStr.split(","))
		                                .map(String::trim)
		                                .filter(s -> !s.isEmpty())
		                                .map(Long::parseLong)
		                                .collect(Collectors.toList());

		                        return teamDeptIds.stream().anyMatch(deptIds::contains);
		                    })
		                    .map(Team::getProjectId)
		                    .collect(Collectors.toSet());
		        } else {
		            internalProjectIds = projectRepository.findAllActiveShankhInternalProjectIds();
		        }
		        
		        internalProjectIds = filterProjectidsApprovalStatusDepartmentFilter(
		                internalProjectIds,
		                projectFilterDTO.getApprovalStatus(),
		                projectFilterDTO.getDepartmentsids()
		            );

		        Map<Long, RMGProjectMappedEmployees> employeeMap = new HashMap<>();

		        for (Integer projectId : internalProjectIds) {
		            List<Team> teams = teamRepository.findActiveTeamsByProjectId(projectId);
		            if (teams == null || teams.isEmpty()) continue;

		            for (Team team : teams) {
		                if (team == null) continue;

		                List<EmployeeTeamMap> mappings = employeeTeamMapRepository.findByTeamIdWhereEmployeesAreActive(team.getTeamId());
		                if (mappings == null || mappings.isEmpty()) continue;

		                for (EmployeeTeamMap map : mappings) {
		                    if (map == null) continue;

		                    Employee emp = employeeRepository.findByEmpId(map.getEmpId());
		                    if (emp == null) continue;

		                    JobRole jr = jobRoleRepository.findByjobRoleId(emp.getJobRoleId());
		                    if (jr == null) continue;

		                    String deptName = departmentRepository.findDepartmentNameFromDeptId(jr.getDeptId());
		                    if (deptName == null) deptName = "";

		                    RMGProjectMappedEmployees dto = employeeMap.get(emp.getEmpId());
		                    if (dto == null) {
		                        dto = new RMGProjectMappedEmployees();
		                        dto.setEmpId(emp.getEmpId());
		                        dto.setEmployeementId(emp.getEmployeementId());
		                        dto.setName(emp.getName());
		                        dto.setEmploymentstatus(emp.getEmploymentstatus());
		                        dto.setBillable(emp.getBillable());
		                        dto.setBillableType(emp.getBillableType());
		                        dto.setDepartment(deptName);
		                        dto.setRmgprojects(new ArrayList<>());
		                        employeeMap.put(emp.getEmpId(), dto);
		                    }

		                    RMGTeam rmgTeam = new RMGTeam();
		                    rmgTeam.setTeamId(team.getTeamId());
		                    rmgTeam.setTeamName(team.getTeamName());
		                    rmgTeam.setIsActive(team.getIsActive());
		                    rmgTeam.setEmployeeRole(map.getEmployeeRole());
		                    rmgTeam.setStatus(map.getActive() == 1 ? "Approved" : "Pending for Approval");
		                    rmgTeam.setDepartmentList(team.getDeptIds().split(","));

		                    RMGProject existingProject = dto.getRmgprojects().stream()
		                            .filter(p -> p.getProjectId().equals(projectId))
		                            .findFirst()
		                            .orElse(null);

		                    if (existingProject == null) {
		                        Project project = projectRepository.findByProjectId(projectId);
		                        if (project == null) continue;

		                        Client client = clientsRepository.findByClientId(project.getClientId());

		                        RMGProject rmgProject = new RMGProject();
		                        rmgProject.setProjectId(project.getProjectId());
		                        rmgProject.setClientName(client != null ? client.getClientName() : "");
		                        rmgProject.setProjectName(project.getProjectName());
		                        rmgProject.setPoProjectId(project.getPoProjectId());
		                        rmgProject.setPoStartDate(project.getPoStartDate());
		                        rmgProject.setPoEndDate(project.getPoEndDate());
		                        rmgProject.setApmosysRM(project.getApmosysRM());
		                        rmgProject.setClientRM(project.getClientRM());
		                        rmgProject.setPoProjectType(project.getPoProjectType());
		                        rmgProject.setPoNo(project.getPoNo());
		                        rmgProject.setActive("true".equalsIgnoreCase(project.getActive()) ? "true" : "false");
		                        rmgProject.setRmgTeam(new ArrayList<>());

		                        rmgProject.getRmgTeam().add(rmgTeam);
		                        dto.getRmgprojects().add(rmgProject);
		                    } else {
		                        existingProject.getRmgTeam().add(rmgTeam);
		                    }
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
		
		public ServiceResponse getAllShankhInternalProjectsNewRMG(ProjectFilterDTO projectFilterDTO) {
		    ServiceResponse response = new ServiceResponse();
		    
		    if ("Not Started".equalsIgnoreCase(projectFilterDTO.getApprovalStatus())) {
		        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		        response.setServiceResponse(Collections.emptyList());
		        return response;
		    }
		    try {
		        Employee employee = employeeRepository.findByEmpId(projectFilterDTO.getCurrentUserEmpId());
		        JobRole jobRole = jobRoleRepository.findByjobRoleId(employee.getJobRoleId());

		        String role = jobRole.getEmployeeRole();
		        String name = jobRole.getName();

		        Set<Integer> internalProjectIds = new HashSet<>();
		        if (role.equalsIgnoreCase("SuperAdmin") || name.equalsIgnoreCase("Director") || name.equalsIgnoreCase("Super Admin")) {
		            internalProjectIds = projectRepository.findAllActiveShankhInternalProjectIds();
		        } else if (teamRepository.existsBySpocId(projectFilterDTO.getCurrentUserEmpId())) {
		            internalProjectIds = teamRepository.findActiveShankhProjectIdsBySpocId(projectFilterDTO.getCurrentUserEmpId());
		        } else if (departmentRepository.existsByHodId(projectFilterDTO.getCurrentUserEmpId())) {
		            List<Long> deptIds = departmentRepository.findDeptIdsByHodId(projectFilterDTO.getCurrentUserEmpId());
		            List<Team> activeTeams = teamRepository.findAllActiveTeamsOfShankhProjects();

		            internalProjectIds = activeTeams.stream()
		                    .filter(team -> {
		                        String teamDeptIdsStr = team.getDeptIds();
		                        if (teamDeptIdsStr == null || teamDeptIdsStr.isBlank()) return false;

		                        List<Long> teamDeptIds = Arrays.stream(teamDeptIdsStr.split(","))
		                                .map(String::trim)
		                                .filter(s -> !s.isEmpty())
		                                .map(Long::parseLong)
		                                .collect(Collectors.toList());

		                        return teamDeptIds.stream().anyMatch(deptIds::contains);
		                    })
		                    .map(Team::getProjectId)
		                    .collect(Collectors.toSet());
		        } else {
		            internalProjectIds = projectRepository.findAllActiveShankhProjectIds();
		        }
		        
		        internalProjectIds = filterProjectidsApprovalStatusDepartmentFilter(
		                internalProjectIds,
		                projectFilterDTO.getApprovalStatus(),
		                projectFilterDTO.getDepartmentsids()
		            );

		        Map<Long, RMGProjectMappedEmployees> employeeMap = new HashMap<>();

		        for (Integer projectId : internalProjectIds) {
		            List<Team> teams = teamRepository.findActiveTeamsByProjectId(projectId);
		            if (teams == null || teams.isEmpty()) continue;

		            for (Team team : teams) {
		                if (team == null) continue;

		                List<EmployeeTeamMap> mappings = employeeTeamMapRepository.findByTeamIdWhereEmployeesAreActive(team.getTeamId());
		                if (mappings == null || mappings.isEmpty()) continue;

		                for (EmployeeTeamMap map : mappings) {
		                    if (map == null) continue;

		                    Employee emp = employeeRepository.findByEmpId(map.getEmpId());
		                    if (emp == null) continue;

		                    JobRole jr = jobRoleRepository.findByjobRoleId(emp.getJobRoleId());
		                    if (jr == null) continue;

		                    String deptName = departmentRepository.findDepartmentNameFromDeptId(jr.getDeptId());
		                    if (deptName == null) deptName = "";

		                    RMGProjectMappedEmployees dto = employeeMap.get(emp.getEmpId());
		                    if (dto == null) {
		                        dto = new RMGProjectMappedEmployees();
		                        dto.setEmpId(emp.getEmpId());
		                        dto.setEmployeementId(emp.getEmployeementId());
		                        dto.setName(emp.getName());
		                        dto.setEmploymentstatus(emp.getEmploymentstatus());
		                        dto.setBillable(emp.getBillable());
		                        dto.setBillableType(emp.getBillableType());
		                        dto.setDepartment(deptName);
		                        dto.setRmgprojects(new ArrayList<>());
		                        employeeMap.put(emp.getEmpId(), dto);
		                    }

		                    RMGTeam rmgTeam = new RMGTeam();
		                    rmgTeam.setTeamId(team.getTeamId());
		                    rmgTeam.setTeamName(team.getTeamName());
		                    rmgTeam.setIsActive(team.getIsActive());
		                    rmgTeam.setEmployeeRole(map.getEmployeeRole());
		                    rmgTeam.setStatus(map.getActive() == 1 ? "Approved" : "Pending for Approval");
		                    rmgTeam.setDepartmentList(team.getDeptIds().split(","));

		                    RMGProject existingProject = dto.getRmgprojects().stream()
		                            .filter(p -> p.getProjectId().equals(projectId))
		                            .findFirst()
		                            .orElse(null);

		                    if (existingProject == null) {
		                        Project project = projectRepository.findByProjectId(projectId);
		                        if (project == null) continue;

		                        Client client = clientsRepository.findByClientId(project.getClientId());

		                        RMGProject rmgProject = new RMGProject();
		                        rmgProject.setProjectId(project.getProjectId());
		                        rmgProject.setClientName(client != null ? client.getClientName() : "");
		                        rmgProject.setProjectName(project.getProjectName());
		                        rmgProject.setPoProjectId(project.getPoProjectId());
		                        rmgProject.setPoStartDate(project.getPoStartDate());
		                        rmgProject.setPoEndDate(project.getPoEndDate());
		                        rmgProject.setApmosysRM(project.getApmosysRM());
		                        rmgProject.setClientRM(project.getClientRM());
		                        rmgProject.setPoProjectType(project.getPoProjectType());
		                        rmgProject.setPoNo(project.getPoNo());
		                        rmgProject.setActive("true".equalsIgnoreCase(project.getActive()) ? "true" : "false");
		                        rmgProject.setRmgTeam(new ArrayList<>());

		                        rmgProject.getRmgTeam().add(rmgTeam);
		                        dto.getRmgprojects().add(rmgProject);
		                    } else {
		                        existingProject.getRmgTeam().add(rmgTeam);
		                    }
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
		
		public Set<Integer> filterProjectidsApprovalStatusDepartmentFilter(
		        Set<Integer> projectIds,
		        String approvalStatus,
		        List<Long> departmentsids) {

		    Set<Integer> filteredProjectIds = new HashSet<>();

		    try {
		        List<Object[]> allProjectList = projectRepository.findAllProjectByIsDraftAndIsActiveOfProjectIds(projectIds);

		        if (!allProjectList.isEmpty()) {
		            List<ResourceManagementDTO> dtoList = new ArrayList<>();

		            for (Object[] object : allProjectList) {
		                Integer projectId = object[0] != null ? Integer.parseInt(object[0].toString()) : null;
		                String isDraftProject = object[2] != null ? object[2].toString() : null;
		                Long isActive = object[3] != null ? Long.parseLong(object[3].toString()) : null;

		               
		                boolean matchesApproval = false;

		                if ("Pending For Approval".equalsIgnoreCase(approvalStatus) && Long.valueOf(2).equals(isActive)) {
		                    matchesApproval = true;
		                } else if ("Approved".equalsIgnoreCase(approvalStatus) && "false".equalsIgnoreCase(isDraftProject)) {
		                    matchesApproval = true;
		                } else if ("Rejected".equalsIgnoreCase(approvalStatus) && "Rejected".equalsIgnoreCase(isDraftProject)) {
		                    matchesApproval = true;
		                }else if("All".equalsIgnoreCase(approvalStatus)) {
		                	 matchesApproval = true;
		                }

		                if (!matchesApproval) continue;

		                
		                List<TeamSpocDTO> spocList = getTeamSpocsByProjectId(projectId);

		               
		                boolean matchesDepartment = true;
		                if (departmentsids != null && !departmentsids.isEmpty()) {
		                    matchesDepartment = spocList.stream().anyMatch(spoc ->
		                        spoc.getDepartmentList() != null &&
		                        Arrays.stream(spoc.getDepartmentList())
		                              .map(Long::valueOf)
		                              .anyMatch(departmentsids::contains)
		                    );
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
				
			}catch(Exception e) {
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
	        logBuilder.append("\n getResourceRequirementByPoProjectId "+projectRepository.getAssignedEmployeesCountInProject(id));

			try {
				List<ResourceManagementDTO> poPortalProjects = fetchPoPortalProjects();
				
				Optional<ResourceManagementDTO> projectDTO = poPortalProjects.stream()
						.filter(dto -> dto.getId() != null && dto.getId().equals(id))
			            .findFirst();

		        if (!projectDTO.isPresent()) {
		        	
		        	response.setServiceResponse("Unable to fetched project requirement details correctly!");
		            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		            logBuilder.append("\n Unable to fetched project requirement details correctly!");
		            
		            return response;
		         
		        } else {
		        	ResourceManagementDTO project = projectDTO.get();

		            int totalRequirements = project.getResourceRequirements().stream()
		                .mapToInt(ResourceRequirementDTO::getCount)
		                .sum();
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
			}catch(Exception e) {
				e.printStackTrace();
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("\n Something went wrong!");
			}
		 return response;
	 } 
	 
	 @Transactional
	 private ServiceResponse setProjectManager(ResourceManagementDTO resourceManagementDTO,Project projectDbResponse) {
		 ServiceResponse response = new ServiceResponse();
		 LogDTO apiLogInfo = new LogDTO();
         apiLogInfo.setSubFeatureName("setProjectManager");
         apiLogInfo.setApiUrl("/api/setProjectManager");
         apiLogInfo.setLogLevel("INFO");
         StringBuilder logBuilder = new StringBuilder();
         logBuilder.append("\n setProjectManager ");

		 try {
			 
			 if (!resourceManagementDTO.getProjectManagerId().isEmpty()) {
				 
				 Project project = projectRepository.findByPoProjectId(resourceManagementDTO.getId());
				 
				    List<ProjectManagerMapping> existingMappings = projectManagerMappingRepository.findByProjectId(Long.parseLong(project.getProjectId().toString()));

				    List<Long> newManagerIds = resourceManagementDTO.getProjectManagerId();

				    // In case a existing project manager is deselected and sent
				    existingMappings.forEach(existingMapping -> {
				        if (!newManagerIds.contains(existingMapping.getProjectManagerId())) {
				            existingMapping.setActive(0);
				            existingMapping.setUpdatedBy(resourceManagementDTO.getUpdatedBy());
				            existingMapping.setUpdatedOn(LocalDateTime.now());
				            projectManagerMappingRepository.save(existingMapping);
				        }
				    });

				    // In case project manager was present but made inactive then make active again in the same row
				    newManagerIds.forEach(managerId -> {
				        ProjectManagerMapping existingMapping = projectManagerMappingRepository.findByProjectIdAndProjectManagerId(Long.parseLong(projectDbResponse.getProjectId().toString()), managerId);

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
		 } catch(Exception e) { 
			 e.printStackTrace();
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("\n Something went wrong!");
		 }
		 return response;
	 }
	 
}
