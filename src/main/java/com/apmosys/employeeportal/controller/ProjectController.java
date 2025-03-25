package com.apmosys.employeeportal.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.PoProjectSyncDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.ProjectPoPortalDTO;
import com.apmosys.employeeportal.service.EmployeeService;
import com.apmosys.employeeportal.service.ProjectService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class ProjectController {

	@Autowired
	ProjectService projectService;
	
	@Autowired
	EmployeeService employeeService;
	
	@RequestMapping(value = "/getAllClients", method = RequestMethod.GET)
	public ServiceResponse getAllClients() {
		
		ServiceResponse response = projectService.getAllClients();
		return response;
	}
	
	@RequestMapping(value = "/getAllProjects", method = RequestMethod.GET)
	public ServiceResponse getAllProjects() {
		
		ServiceResponse response = projectService.getAllProjects();
		return response;
	}
	
	@RequestMapping(value = "/createProject", method = RequestMethod.POST)
	public ServiceResponse createProject(@RequestBody PoProjectSyncDTO poProjectSyncDto) {
		employeeService.clearEmployeeCache();
		ServiceResponse response = projectService.createProject(poProjectSyncDto);
		return response;
	}
	
	@RequestMapping(value = "/getProjectByProjectId", method = RequestMethod.POST)
	public ServiceResponse getProjectByProjectId(@RequestBody PoProjectSyncDTO poProjectSyncDto) {
		
		ServiceResponse response = projectService.getProjectByProjectId(poProjectSyncDto);
		return response;
	}
	
	@RequestMapping(value = "/updateProject", method = RequestMethod.POST)
	public ServiceResponse updateProject(@RequestBody PoProjectSyncDTO poProjectSyncDto) {
		employeeService.clearEmployeeCache();
		ServiceResponse response = projectService.updateProject(poProjectSyncDto);
		return response;
	}
	
	@RequestMapping(value = "/deleteProject", method = RequestMethod.POST)
	public ServiceResponse deleteProject(@RequestBody PoProjectSyncDTO poProjectSyncDto) {
		employeeService.clearEmployeeCache();
		ServiceResponse response = projectService.deleteProject(poProjectSyncDto);
		return response;
	}
	
	@RequestMapping(value = "/syncPoProjectAndTeam", method = RequestMethod.POST)
	public ServiceResponse syncPoProjectAndTeam(@RequestBody PoProjectSyncDTO[] poProjectSyncDto) {
		
		ServiceResponse response = projectService.syncPoProjectAndTeam(poProjectSyncDto);
		return response;
	}
	
	@RequestMapping(value = "/getSyncableProject", method = RequestMethod.GET)
	public ServiceResponse getSyncableProject() {
		
		ServiceResponse response = projectService.getSyncableProject();
		return response;
	}
	
	@RequestMapping(value = "/checkProjectName", method = RequestMethod.POST)
	public ServiceResponse checkProjectName(@RequestBody ProjectDTO projectDto) {
		
		ServiceResponse response = projectService.checkProjectName(projectDto);
		return response;
	}
	
	@RequestMapping(value = "/getAllMyProjectByEmpId", method = RequestMethod.POST)
	public ServiceResponse getAllMyProjectByEmpId(@RequestBody ProjectDTO projectDto) {
		
		ServiceResponse response = projectService.getAllMyProjectByEmpId(projectDto);
		return response;
	}
	
	@GetMapping(value = "/poprojectclone")
	public ResponseEntity<List<ProjectPoPortalDTO>> poprojectclone() {
		return ResponseEntity.ok(projectService.getProjectCloneFromPoPortal());
	}
	
}
