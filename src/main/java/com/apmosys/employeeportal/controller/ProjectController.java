package com.apmosys.employeeportal.controller;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.Encrypted;
import com.apmosys.employeeportal.dto.ClientProjectReportDTO;
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
	@Encrypted
	@RequestMapping(value = "/getAllClients", method = RequestMethod.GET)
	public ServiceResponse getAllClients() {
		
		ServiceResponse response = projectService.getAllClients();
		return response;
	}
	@Encrypted
	@RequestMapping(value = "/getAllProjects", method = RequestMethod.GET)
	public ServiceResponse getAllProjects() {
		
		ServiceResponse response = projectService.getAllProjects();
		return response;
	}
	@Encrypted
	@RequestMapping(value = "/createProject", method = RequestMethod.POST)
	public ServiceResponse createProject(@RequestBody PoProjectSyncDTO poProjectSyncDto) {
		employeeService.clearEmployeeCache();
		ServiceResponse response = projectService.createProject(poProjectSyncDto);
		return response;
	}
	@Encrypted
	@RequestMapping(value = "/getProjectByProjectId", method = RequestMethod.POST)
	public ServiceResponse getProjectByProjectId(@RequestBody PoProjectSyncDTO poProjectSyncDto) {
		
		ServiceResponse response = projectService.getProjectByProjectId(poProjectSyncDto);
		return response;
	}
	@Encrypted
	@RequestMapping(value = "/updateProject", method = RequestMethod.POST)
	public ServiceResponse updateProject(@RequestBody PoProjectSyncDTO poProjectSyncDto) {
		employeeService.clearEmployeeCache();
		ServiceResponse response = projectService.updateProject(poProjectSyncDto);
		return response;
	}
	@Encrypted
	@RequestMapping(value = "/deleteProject", method = RequestMethod.POST)
	public ServiceResponse deleteProject(@RequestBody PoProjectSyncDTO poProjectSyncDto) {
		employeeService.clearEmployeeCache();
		ServiceResponse response = projectService.deleteProject(poProjectSyncDto);
		return response;
	}
	@Encrypted
	@RequestMapping(value = "/syncPoProjectAndTeam", method = RequestMethod.POST)
	public ServiceResponse syncPoProjectAndTeam(@RequestBody PoProjectSyncDTO[] poProjectSyncDto) {
		
		ServiceResponse response = projectService.syncPoProjectAndTeam(poProjectSyncDto);
		return response;
	}
	@Encrypted
	@RequestMapping(value = "/getSyncableProject", method = RequestMethod.GET)
	public ServiceResponse getSyncableProject() {
		
		ServiceResponse response = projectService.getSyncableProject();
		return response;
	}
	@Encrypted
	@RequestMapping(value = "/checkProjectName", method = RequestMethod.POST)
	public ServiceResponse checkProjectName(@RequestBody ProjectDTO projectDto) {
		
		ServiceResponse response = projectService.checkProjectName(projectDto);
		return response;
	}
	@Encrypted
	@RequestMapping(value = "/getAllMyProjectByEmpId", method = RequestMethod.POST)
	public ServiceResponse getAllMyProjectByEmpId(@RequestBody ProjectDTO projectDto) {
		
		ServiceResponse response = projectService.getAllMyProjectByEmpId(projectDto);
		return response;
	}
	
	@GetMapping(value = "/poprojectclone")
	public ResponseEntity<ServiceResponse> poprojectclone() {
	    return ResponseEntity.ok(projectService.getProjectCloneFromPoPortal());
	}
	@Encrypted
	@PostMapping(value = "/poProjectTimesheetSync")
	public ServiceResponse poProjectTimesheetSync(@RequestBody Set<Long> projectIdList) {
		return projectService.poProjectTimesheetSync(projectIdList);
	}
	@Encrypted
	@PostMapping(value = "/getEmployeeProjectReport")
	public ServiceResponse getEmployeeProjectReport(@RequestBody GetEmployeeProjectReportPayloadDTO dto) {
		return projectService.getEmployeeProjectReport(dto);
	}
	@Encrypted
	@GetMapping(value = "/getResourceRequirementFromPoPortal")
	public ServiceResponse getResourceRequirementFromPoPortal() {
		return projectService.getResourceRequirementFromPoPortal();
	}
	
	@Encrypted
	@PostMapping(value = "/getProjectWithCliendSideID")
	public ServiceResponse getProjectWithCliendSideID(@RequestBody ProjectDTO projectDto) {
		return projectService.getProjectWithCliendSideID(projectDto);
	}
	@Encrypted
	@PostMapping(value="/getCompletedFixedCostProjects")
	public ServiceResponse getCompletedFixedCostProjects(@RequestBody ProjectRequest projectRequest){
		return projectService.getCompletedFixedCostProjects(projectRequest);
	}
	@Encrypted
	@PostMapping(value = "/getFixedCostCount")
	public ServiceResponse getFixedCostCount(@RequestBody ProjectFilterDTO projectFilter) {
		return projectService.getFcCount(projectFilter);
	}
	

@Encrypted
@RequestMapping(value = "/getClientAndProjectReport", method = RequestMethod.POST)
public ServiceResponse getClientAndProjectReport(@RequestBody ClientProjectReportDTO clientProjectReportDTO) {
    ServiceResponse response = projectService.getClientAndProjectReport(clientProjectReportDTO);
    return response;
}
@Encrypted
@RequestMapping(value = "/getClientAndProjectReportDataList", method = RequestMethod.POST)
public ServiceResponse getClientAndProjectReportDataList(@RequestBody ClientProjectReportDTO clientProjectReportDTO) {
    ServiceResponse response = projectService.getClientAndProjectReportDataList(clientProjectReportDTO);
    return response;
}
	
	
}
