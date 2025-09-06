package com.apmosys.employeeportal.controller;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.GetEmployeeProjectReportPayloadDTO;
import com.apmosys.employeeportal.dto.GetProjectToEmployeeReportForProjectDTO;
import com.apmosys.employeeportal.dto.HandleTeamsAsPerLinkedPoPayloadDTO;
import com.apmosys.employeeportal.dto.PoProjectIdRequestDTO;
import com.apmosys.employeeportal.dto.PoProjectSyncDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.ProjectFilterDTO;
import com.apmosys.employeeportal.dto.ProjectPoPortalDTO;
import com.apmosys.employeeportal.request.ProjectRequest;
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
	public ResponseEntity<ServiceResponse> poprojectclone() {
	    return ResponseEntity.ok(projectService.getProjectCloneFromPoPortal());
	}
	
	@PostMapping(value = "/poProjectTimesheetSync")
	public ServiceResponse poProjectTimesheetSync(@RequestBody Set<Long> projectIdList) {
		return projectService.poProjectTimesheetSync(projectIdList);
	}
	
	@PostMapping(value = "/getEmployeeProjectReport")
	public ServiceResponse getEmployeeProjectReport(@RequestBody GetEmployeeProjectReportPayloadDTO dto) {
		return projectService.getEmployeeProjectReport(dto);
	}
		
	@PostMapping(value = "/handleTeamsAsPerLinkedPo")
	public ServiceResponse handleTeamsAsPerLinkedPo(@RequestBody HandleTeamsAsPerLinkedPoPayloadDTO payloadDTO) {
		return projectService.handleTeamsAsPerLinkedPo(payloadDTO);
	}
	
	@GetMapping(value = "/getResourceRequirementFromPoPortal")
	public ServiceResponse getResourceRequirementFromPoPortal() {
		return projectService.getResourceRequirementFromPoPortal();
	}
	

	@PostMapping(value = "/getProjectWithCliendSideID")
	public ServiceResponse getProjectWithCliendSideID(@RequestBody ProjectDTO projectDto) {
		return projectService.getProjectWithCliendSideID(projectDto);
	}
	
	@PostMapping(value="/getCompletedFixedCostProjects")
	public ServiceResponse getCompletedFixedCostProjects(@RequestBody ProjectRequest projectRequest){
		return projectService.getCompletedFixedCostProjects(projectRequest);
	}
	
	@PostMapping(value = "/getFixedCostCount")
	public ServiceResponse getFixedCostCount(@RequestBody ProjectFilterDTO projectFilter) {
		return projectService.getFcCount(projectFilter);
	}
}
