package com.apmosys.employeeportal.controller;


import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.FCProjectMilestoneDTO;
import com.apmosys.employeeportal.dto.ClientProjectReportDTO;
import com.apmosys.employeeportal.dto.GetEmployeeProjectReportPayloadDTO;
import com.apmosys.employeeportal.dto.HandleTeamsAsPerLinkedPoPayloadDTO;
import com.apmosys.employeeportal.dto.MilestoneUpdatedLogDto;
import com.apmosys.employeeportal.dto.PoProjectSyncDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.ProjectFilterDTO;
import com.apmosys.employeeportal.dto.RmAndHodEmailDto;
import com.apmosys.employeeportal.request.ProjectRequest;
import com.apmosys.employeeportal.service.EmployeeService;
import com.apmosys.employeeportal.service.PoPortalAPIService;
import com.apmosys.employeeportal.service.ProjectService;
import com.apmosys.employeeportal.utility.PoPortalAPIAuthenticationJWTUtility;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class ProjectController {

	@Autowired
	ProjectService projectService;
	
	@Autowired
	EmployeeService employeeService;
	
	@Autowired
	PoPortalAPIService poPortalApiService;
	
	@Autowired
	private PoPortalAPIAuthenticationJWTUtility poPortalAPIAuthenticationJWTUtility;
	
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
	
	@PostMapping(value = "/syncPoProjectAndTeam")
	public ServiceResponse syncPoProjectAndTeam(HttpServletRequest httpRequest, @RequestBody PoProjectSyncDTO[] poProjectSyncDto) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		return projectService.syncPoProjectAndTeam(poProjectSyncDto);
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
	public ResponseEntity<ServiceResponse> poprojectclone(HttpServletRequest httpRequest) {
		return ResponseEntity.ok(projectService.getProjectCloneFromPoPortal());
	}
	
	@PostMapping(value = "/poProjectTimesheetSync")
	public ServiceResponse poProjectTimesheetSync(HttpServletRequest httpRequest,@RequestBody Set<Long> projectIdList) {
//		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		return projectService.poProjectTimesheetSync(projectIdList);
	}
	
	@PostMapping(value = "/getEmployeeProjectReport")
	public ServiceResponse getEmployeeProjectReport(@RequestBody GetEmployeeProjectReportPayloadDTO dto) {
		return projectService.getEmployeeProjectReport(dto);
	}

	 @RequestMapping(value = "/getAllProjectFCLineItemListByProjectId", method = RequestMethod.POST)
	 public ResponseEntity<ServiceResponse> getAllProjectFCLineItemListByProjectId(@RequestBody ProjectDTO projectDto) {
	 	return ResponseEntity.ok(projectService.getAllProjectFCLineItemListByProjectId(projectDto));
	 }
	
	@RequestMapping(value = "/updateMilestoneById", method = RequestMethod.PUT, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ServiceResponse> updateMilestoneById(@RequestPart("dto") FCProjectMilestoneDTO fcProjectMilestoneDTO,
	                                                           @RequestPart("file") MultipartFile file) {
	    return ResponseEntity.ok(poPortalApiService.updateMilestoneById(fcProjectMilestoneDTO, file));
	}

	
	 @RequestMapping(value = "/getMilestoneById", method = RequestMethod.GET)
	 public ResponseEntity<FCProjectMilestoneDTO> getMilestoneById(@RequestParam Long milestoneId) {
	 	return ResponseEntity.ok(projectService.getMilestoneDocument(milestoneId));
	 }

	@GetMapping(value = "/getResourceRequirementFromPoPortal")
	public ServiceResponse getResourceRequirementFromPoPortal() {
		return projectService.getResourceRequirementFromPoPortal();
	}
	

	@PostMapping(value = "/getProjectWithCliendSideID")
	public ServiceResponse getProjectWithCliendSideID(@RequestBody ProjectDTO projectDto) {
		return projectService.getProjectWithCliendSideID(projectDto);
	}
	
	@PostMapping(value="/getAllMilestoneToBeExpired")
	public ServiceResponse getAllMilestoneToBeExpired(@RequestBody Long rmId){
		return poPortalApiService.getAllMilestoneToBeExpired(rmId);
	}
	
	@PutMapping(value="/updateMilestoneExtendedDate")
	public ServiceResponse updateMilestoneExtendedDate(@RequestBody MilestoneUpdatedLogDto milestoneUpdatedLogDto) {
		return  poPortalApiService.updateMilestoneExtendedDate(milestoneUpdatedLogDto);
	}
	
	
	@GetMapping(value = "/getAllMilestoneExtendReason")
	public ServiceResponse getAllMilestoneExtendReason() {
		return poPortalApiService.getAllMilestoneExtendReason();
	}
	
	
	@GetMapping(value = "/getMilestoneProjectWise")
	public ServiceResponse getMilestoneProjectWise() {
		return poPortalApiService.getMilestoneProjectWise();
	}
	
	

	@PostMapping(value="/getCompletedFixedCostProjects")
	public ServiceResponse getCompletedFixedCostProjects(@RequestBody ProjectRequest projectRequest){
		return projectService.getCompletedFixedCostProjects(projectRequest);
	}
	
	@PostMapping(value = "/getFixedCostCount")
	public ServiceResponse getFixedCostCount(@RequestBody ProjectFilterDTO projectFilter) {
		return projectService.getFcCount(projectFilter);
	}
	


@RequestMapping(value = "/getClientAndProjectReport", method = RequestMethod.POST)
public ServiceResponse getClientAndProjectReport(@RequestBody ClientProjectReportDTO clientProjectReportDTO) {
    ServiceResponse response = projectService.getClientAndProjectReport(clientProjectReportDTO);
    return response;
}

@RequestMapping(value = "/getClientAndProjectReportDataList", method = RequestMethod.POST)
public ServiceResponse getClientAndProjectReportDataList(@RequestBody ClientProjectReportDTO clientProjectReportDTO) {
    ServiceResponse response = projectService.getClientAndProjectReportDataList(clientProjectReportDTO);
    return response;
}
	
}
