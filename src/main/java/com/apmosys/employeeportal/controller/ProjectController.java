package com.apmosys.employeeportal.controller;

import java.util.List;
import java.util.Map;
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
import com.apmosys.employeeportal.Encrypted;
import com.apmosys.employeeportal.JobRoleAccess;
import com.apmosys.employeeportal.dto.ClientProjectReportDTO;
import com.apmosys.employeeportal.dto.GetEmployeeProjectReportPayloadDTO;
import com.apmosys.employeeportal.dto.HandleTeamsAsPerLinkedPoPayloadDTO;
import com.apmosys.employeeportal.dto.MilestoneUpdatedLogDto;
import com.apmosys.employeeportal.dto.PoProjectSyncDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.ProjectFilterDTO;
import com.apmosys.employeeportal.dto.RmAndHodEmailDto;
import com.apmosys.employeeportal.request.ProjectRequest;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.service.EmployeeService;
import com.apmosys.employeeportal.service.PoPortalAPIService;
import com.apmosys.employeeportal.service.ProjectService;
import com.apmosys.employeeportal.utility.PoPortalAPIAuthenticationJWTUtility;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping(path = "/api")
public class ProjectController {

	@Autowired
	ProjectService projectService; 
	
	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	EmployeeService employeeService;

	@Autowired
	PoPortalAPIService poPortalApiService;

	@Autowired
	private PoPortalAPIAuthenticationJWTUtility poPortalAPIAuthenticationJWTUtility;

	@Encrypted
	@JobRoleAccess(featureIds = {33,34})
	@RequestMapping(value = "/getAllClients", method = RequestMethod.GET)
	public ServiceResponse getAllClients() {

		ServiceResponse response = projectService.getAllClients();
		return response;
	}

	@Encrypted
	@JobRoleAccess(featureIds = {33,34})
	@RequestMapping(value = "/getAllProjects", method = RequestMethod.GET)
	public ServiceResponse getAllProjects() {
		ServiceResponse response = projectService.getAllProjects();
		return response;
	}
	
	@RequestMapping(value = "/getAllProjectsList", method = RequestMethod.GET)
	public ResponseEntity<List<Project>> getAllProjectsList() {
		List<Project> response = projectRepository.findAll();
		return ResponseEntity.ok(response);
	}

	@Encrypted
	@JobRoleAccess(featureIds = {33,34})
	@RequestMapping(value = "/createProject", method = RequestMethod.POST)
	public ServiceResponse createProject(@RequestBody PoProjectSyncDTO poProjectSyncDto) {
		employeeService.clearEmployeeCache();
		ServiceResponse response = projectService.createProject(poProjectSyncDto);
		return response;
	}

	@Encrypted
	@JobRoleAccess(featureIds = {33})
	@RequestMapping(value = "/getProjectByProjectId", method = RequestMethod.POST)
	public ServiceResponse getProjectByProjectId(@RequestBody PoProjectSyncDTO poProjectSyncDto) {

		ServiceResponse response = projectService.getProjectByProjectId(poProjectSyncDto);
		return response;
	}

	@Encrypted
	@JobRoleAccess(featureIds = {33})
	@RequestMapping(value = "/updateProject", method = RequestMethod.POST)
	public ServiceResponse updateProject(@RequestBody PoProjectSyncDTO poProjectSyncDto) {
		employeeService.clearEmployeeCache();
		ServiceResponse response = projectService.updateProject(poProjectSyncDto);
		return response;
	}

	@Encrypted
	@JobRoleAccess(featureIds = {33})
	@RequestMapping(value = "/deleteProject", method = RequestMethod.POST)
	public ServiceResponse deleteProject(@RequestBody PoProjectSyncDTO poProjectSyncDto) {
		employeeService.clearEmployeeCache();
		ServiceResponse response = projectService.deleteProject(poProjectSyncDto);
		return response;
	}

	@Encrypted
	@PostMapping(value = "/syncPoProjectAndTeam")
	public ServiceResponse syncPoProjectAndTeam(HttpServletRequest httpRequest,
			@RequestBody PoProjectSyncDTO[] poProjectSyncDto) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		return projectService.syncPoProjectAndTeam(poProjectSyncDto);
	}

	@Encrypted
	@RequestMapping(value = "/getSyncableProject", method = RequestMethod.GET)
	public ServiceResponse getSyncableProject() {

		ServiceResponse response = projectService.getSyncableProject();
		return response;
	}

	@Encrypted
	@JobRoleAccess(featureIds = {33,34})
	@RequestMapping(value = "/checkProjectName", method = RequestMethod.POST)
	public ServiceResponse checkProjectName(@RequestBody ProjectDTO projectDto) {

		ServiceResponse response = projectService.checkProjectName(projectDto);
		return response;
	}

	@Encrypted
	@JobRoleAccess(featureIds = {7})
	@RequestMapping(value = "/getAllMyProjectByEmpId", method = RequestMethod.POST)
	public ServiceResponse getAllMyProjectByEmpId(@RequestBody ProjectDTO projectDto) {

		ServiceResponse response = projectService.getAllMyProjectByEmpId(projectDto);
		return response;
	}

	@JobRoleAccess(featureIds = {26})
	@GetMapping(value = "/poprojectclone")
	public ResponseEntity<ServiceResponse> poprojectclone(HttpServletRequest httpRequest) {
		return ResponseEntity.ok(projectService.getProjectCloneFromPoPortal());
	}
	
	@PostMapping(value = "/poProjectTimesheetSync")
	public ServiceResponse poProjectTimesheetSync(HttpServletRequest httpRequest,@RequestBody Set<Long> projectIdList) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		return projectService.poProjectTimesheetSync(projectIdList);
	}

	@Encrypted
	@JobRoleAccess(featureIds = {26})
	@PostMapping(value = "/getEmployeeProjectReport")
	public ServiceResponse getEmployeeProjectReport(@RequestBody GetEmployeeProjectReportPayloadDTO dto) {
		return projectService.getEmployeeProjectReport(dto);
	}
	@JobRoleAccess(featureIds = {34})
	@RequestMapping(value = "/getAllProjectFCLineItemListByProjectId", method = RequestMethod.POST)
	public ResponseEntity<ServiceResponse> getAllProjectFCLineItemListByProjectId(@RequestBody ProjectDTO projectDto) {
		return ResponseEntity.ok(projectService.getAllProjectFCLineItemListByProjectId(projectDto));
	}

	@JobRoleAccess(featureIds = {34})
	@RequestMapping(value = "/updateMilestoneById", method = RequestMethod.PUT, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ServiceResponse> updateMilestoneById(
			@RequestPart("dto") FCProjectMilestoneDTO fcProjectMilestoneDTO,
			@RequestPart(value = "file", required = false) MultipartFile file,
			@RequestPart("projectName") String projectNameForMilestoneUpdate) {
		return ResponseEntity.ok(poPortalApiService.updateMilestoneById(fcProjectMilestoneDTO, file , projectNameForMilestoneUpdate));
	}

	@JobRoleAccess(featureIds = {34})
	@RequestMapping(value = "/getMilestoneById", method = RequestMethod.GET)
	public ResponseEntity<FCProjectMilestoneDTO> getMilestoneById(@RequestParam Long milestoneId) {
		return ResponseEntity.ok(projectService.getMilestoneDocument(milestoneId));
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

	@JobRoleAccess(featureIds = {24})
	@PostMapping(value = "/getAllMilestoneToBeExpired")
	public ServiceResponse getAllMilestoneToBeExpired(@RequestBody Long rmId) {
		return poPortalApiService.getAllMilestoneToBeExpired(rmId);
	}

	@JobRoleAccess(featureIds = {24})
	@PostMapping(value = "/updateMilestoneExtendedDate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ServiceResponse updateMilestoneExtendedDate(@RequestPart("milestoneData") String milestoneData,
	        @RequestPart(value = "extensionFile", required = false) MultipartFile extensionFile) throws Exception {

	    ObjectMapper mapper = new ObjectMapper();
	    MilestoneUpdatedLogDto milestoneUpdatedLogDto =
	            mapper.readValue(milestoneData, MilestoneUpdatedLogDto.class);

	    return poPortalApiService.updateMilestoneExtendedDate(milestoneUpdatedLogDto, extensionFile);
	}

	@JobRoleAccess(featureIds = {24})
	@GetMapping(value = "/getAllMilestoneExtendReason")
	public ServiceResponse getAllMilestoneExtendReason() {
		return poPortalApiService.getAllMilestoneExtendReason();
	}
	
	@GetMapping(value = "/getPodetailsPromPOPortal")
	public ServiceResponse getPodetailsPromPOPortal() {
		return poPortalApiService.syncProjectPoFromPoPortal();
	}
	
	@GetMapping(value = "/getProjectSDEDFromPOPortal")
	public ServiceResponse getProjectSDEDFromPOPortal() {
		return poPortalApiService.updateSDEDOfproject();
	} 
	

	@GetMapping(value = "/getMilestoneProjectWise")
	public ServiceResponse getMilestoneProjectWise() {
		return poPortalApiService.getMilestoneProjectWise();
	}

	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@PostMapping(value = "/getCompletedFixedCostProjects")
	public ServiceResponse getCompletedFixedCostProjects(@RequestBody ProjectRequest projectRequest) {
		return projectService.getCompletedFixedCostProjects(projectRequest);
	}

	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@PostMapping(value = "/getFixedCostCount")
	public ServiceResponse getFixedCostCount(@RequestBody ProjectFilterDTO projectFilter) {
		return projectService.getFcCount(projectFilter);
	}

	@Encrypted
	@JobRoleAccess(featureIds = {26})
	@RequestMapping(value = "/getClientAndProjectReport", method = RequestMethod.POST)
	public ServiceResponse getClientAndProjectReport(@RequestBody ClientProjectReportDTO clientProjectReportDTO) {
		ServiceResponse response = projectService.getClientAndProjectReport(clientProjectReportDTO);
		return response;
	}

	@Encrypted
	@JobRoleAccess(featureIds = {26})
	@RequestMapping(value = "/getClientAndProjectReportDataList", method = RequestMethod.POST)
	public ServiceResponse getClientAndProjectReportDataList(
			@RequestBody ClientProjectReportDTO clientProjectReportDTO) {
		ServiceResponse response = projectService.getClientAndProjectReportDataList(clientProjectReportDTO);
		return response;
	}

	@RequestMapping(value = "/getResourceCountByPoprojectId", method = RequestMethod.POST)
	public ServiceResponse getResourceCountByPoprojectId(HttpServletRequest httpRequest,
			@RequestBody List<String> projectNames) {

//	poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);

		ServiceResponse response = poPortalApiService.getResourceCountByPoprojectId(projectNames);
		return response;
	}

	@Encrypted
	@PostMapping(value = "/getEmployeeProjectCount")
	public ServiceResponse getEmployeeProjectCount(@RequestBody GetEmployeeProjectReportPayloadDTO dto) {
		return projectService.getEmployeeProjectCount(dto);
	}
	
	@RequestMapping(value = "/getResourceCountListByPoprojectName", method = RequestMethod.POST)
	public ServiceResponse getResourceCountListByPoprojectName(HttpServletRequest httpRequest,
			@RequestBody List<String> projectNames) {

//	poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);

		ServiceResponse response = poPortalApiService.getResourceCountListByPoprojectName(projectNames);
		return response;
	}
	

	@RequestMapping(value = "/getProjectByName", method = RequestMethod.POST)
	public ServiceResponse getProjectByName(@RequestBody ProjectDTO projectDto) {
		ServiceResponse response = projectService.getProjectByName(projectDto);
		return response;
	}
	
	@PostMapping("/getExtensionDocumentByName")
	public ResponseEntity<FCProjectMilestoneDTO> getExtensionDocumentById(@RequestBody Map<String, String> request) {
	    String uniquefile = request.get("uniquefile");
	    return ResponseEntity.ok(projectService.getExtensionDocumentByName(uniquefile));
	}
	
	@PostMapping("/validateDocName")
	public ServiceResponse validateDocName(@RequestBody Map<String, String> request) {
	    String uniquefile = request.get("uniquefile");
		ServiceResponse response = projectService.validateDocName(uniquefile);
		return response;
	}


	
}
