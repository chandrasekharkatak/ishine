package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.ResourceManagementDTO;
import com.apmosys.employeeportal.service.ResourceManagementService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api")
public class ResourceManagementController {
	
	@Autowired
	ResourceManagementService resourceManagementService;

	@RequestMapping(value = "/createDraftProjectInfo", method = RequestMethod.POST)
	public ServiceResponse createDraftProjectInfo(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		
		ServiceResponse response = resourceManagementService.createDraftProjectInfo(resourceManagementDTO);
		return response;
	}
	
	@RequestMapping(value = "/getTeamListByProjectName", method = RequestMethod.POST)
	public ServiceResponse getTeamListByProjectName(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		
		ServiceResponse response = resourceManagementService.getTeamListByProjectName(resourceManagementDTO);
		return response;
	}
	
	@RequestMapping(value = "/alreadyCreatedTeam", method = RequestMethod.GET)
	public ServiceResponse alreadyCreatedTeam() {
		
		ServiceResponse response = resourceManagementService.alreadyCreatedTeam();
		return response;
	}
	
	@RequestMapping(value = "/getPendingForApprovalProject", method = RequestMethod.GET)
	public ServiceResponse getPendingForApprovalProject() {
		
		ServiceResponse response = resourceManagementService.getPendingForApprovalProject();
		return response;
	}
	
	@RequestMapping(value = "/approvePendingProject", method = RequestMethod.POST)
	public ServiceResponse approvePendingProject(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		
		ServiceResponse response = resourceManagementService.approvePendingProject(resourceManagementDTO);
		return response;
	}
	
	@RequestMapping(value = "/rejectPendingProject", method = RequestMethod.POST)
	public ServiceResponse rejectPendingProject(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		
		ServiceResponse response = resourceManagementService.rejectPendingProject(resourceManagementDTO);
		return response;
	}
	
	@RequestMapping(value = "/sendProjectApproval", method = RequestMethod.POST)
	public ServiceResponse sendProjectApproval(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		
		ServiceResponse response = resourceManagementService.sendProjectApproval(resourceManagementDTO);
		return response;
	}
	
	@RequestMapping(value = "/sendProjectInfoToPoPortal", method = RequestMethod.POST)
	public ServiceResponse sendProjectInfoToPoPortal(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		
		ServiceResponse response = resourceManagementService.sendProjectInfoToPoPortal(resourceManagementDTO);
		return response;
	}
	
	@RequestMapping(value = "/bulkSyncProject", method = RequestMethod.POST)
	public ServiceResponse bulkSyncProject(@RequestBody ProjectDTO projectDTO) {
		
		ServiceResponse response = resourceManagementService.bulkSyncProject(projectDTO);
		return response;
	}
	
	@RequestMapping(value = "/approveProject", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
	public String approveProject(@RequestParam(name = "id") String id,@RequestParam(name = "status") String status) {
		return "<html>\n" + "<header><title>Welcome</title></header>\n" +
		          "<body>\n" + "<h1>Your Request for Project "+ id +" is "+ status +"!!</h1>" + "</body>\n" + "</html>";
	}
	
}
