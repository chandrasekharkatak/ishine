package com.apmosys.employeeportal.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.DefaultProjectUpdateDTO;
import com.apmosys.employeeportal.dto.OtherProjectSetDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.ProjectFilterDTO;
import com.apmosys.employeeportal.dto.ResourceManagementDTO;
import com.apmosys.employeeportal.dto.SetProjectMappingAndDefaultProjectDTO;
import com.apmosys.employeeportal.dto.TeamDTO;
import com.apmosys.employeeportal.dto.TeamMemberDTO;
import com.apmosys.employeeportal.model.Team;
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
	
	@RequestMapping(value = "/getInternalProject", method = RequestMethod.GET)
	public ServiceResponse getInternalProject() {
		
		ServiceResponse response = resourceManagementService.getInternalProject();
		return response;
	}
	
	@RequestMapping(value = "/getExistingProjectsAndTeamsByEmployee", method = RequestMethod.POST)
	public ServiceResponse getExistingProjectsAndTeamsByEmployee(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		
		ServiceResponse response = resourceManagementService.getExistingProjectsAndTeamsByEmployee(resourceManagementDTO);
		return response;
	}
	
	@RequestMapping(value = "/updateProjectResourceAsInActive", method = RequestMethod.POST)
	public ServiceResponse updateProjectResourceAsInActive(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		
		ServiceResponse response = resourceManagementService.updateProjectResourceAsInActive(resourceManagementDTO);
		return response;
	}
	
	
	
	@RequestMapping(value = "/deleteTeamByTeamId", method = RequestMethod.POST)
	public ServiceResponse deleteTeamByTeamId(@RequestBody TeamDTO teamDto) {
		
		ServiceResponse response = resourceManagementService.deleteTeamByTeamId(teamDto);
		return response;
	}
	
	@RequestMapping(value = "/getProjectInfo", method = RequestMethod.POST)
	public ServiceResponse getProjectInfo(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		
		ServiceResponse response = resourceManagementService.getProjectInfo(resourceManagementDTO);
		return response;
	}
	
	@RequestMapping(value = "/getPoProjectInfo", method = RequestMethod.POST)
	public ServiceResponse getPoProjectInfo(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		
		ServiceResponse response = resourceManagementService.getPoProjectInfo(resourceManagementDTO);
		return response;
	}
	
	@RequestMapping(value = "/getTeamInfo", method = RequestMethod.POST)
	public ServiceResponse getTeamInfo(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		
		ServiceResponse response = resourceManagementService.getTeamInfo(resourceManagementDTO);
		return response;
	}
	@RequestMapping(value = "/getTeamMemberByTeamId/{teamId}", method = RequestMethod.GET)
	public ServiceResponse getTeamMemberByTeamId(@PathVariable("teamId") Long teamId) {
		
		ServiceResponse response = resourceManagementService.getTeamMemberByTeamId(teamId);
		return response;
	}

	@RequestMapping(value = "/syncPoProjectDetailsByProjectId", method = RequestMethod.POST)
	public ServiceResponse syncPoProjectDetailsByProjectId(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		
		ServiceResponse response = resourceManagementService.syncPoProjectDetailsByProjectId(resourceManagementDTO);
		return response;
	}
	
	@RequestMapping(value = "/getPoProjectDetailsForPoProjects", method = RequestMethod.GET)
	public ServiceResponse getPoProjectDetailsForPoProjects() {
		
		ServiceResponse response = resourceManagementService.getPoProjectDetailsForPoProjects();
		return response;
	}
//	
//	@RequestMapping(value = "/sendEmailNotificationToBDTeam", method = RequestMethod.POST)
//	public ServiceResponse sendEmailNotificationToBDTeam(@RequestBody ResourceManagementDTO resourceManagementDTO) {
//		
//		ServiceResponse response = resourceManagementService.sendEmailNotificationToBDTeam(resourceManagementDTO);
//		return response;
//	}
	
	@PostMapping("/sendEmailNotificationToBDTeam")
	public ServiceResponse sendEmailNotificationToBDTeam(@RequestBody ResourceManagementDTO resourceManagementDTO) {
	    return resourceManagementService.sendEmailNotificationToBDTeam(resourceManagementDTO);
	}
	
	@GetMapping("/getEmployeeByNameAndEmpld")
	public ServiceResponse getEmployeeByNameAndEmpld() {
	    return resourceManagementService.getEmployeeByNameAndEmpld();
	}
	
	 @PostMapping("/combinedPOINTERNALList")
	    public ServiceResponse combinedPOINTERNALList(@RequestBody ProjectFilterDTO projectFilterDTO) {
	        return resourceManagementService.combinedDataListWithCount(projectFilterDTO);
	    }
	 
	@RequestMapping(value = "/deleteTeamsByIdsBulk", method = RequestMethod.POST)
	public ServiceResponse deleteTeamsByIdsBulk(@RequestBody List<TeamDTO> teamDTO) {
		
		ServiceResponse response = resourceManagementService.deleteTeamsByIdsBulk(teamDTO);
		return response;
	}
	
	@RequestMapping(value = "/updateProjectResourcesAsInActiveBulk", method = RequestMethod.POST)
	public ServiceResponse updateProjectResourcesAsInActiveBulk(@RequestBody List<ResourceManagementDTO> resourceManagementDTOList) {
	    ServiceResponse response = resourceManagementService.updateProjectResourcesAsInActiveBulk(resourceManagementDTOList);
	    return response;
	}
	
	@RequestMapping(value = "/updateProjectStartAndEndDate", method = RequestMethod.POST)
	public ServiceResponse updateProjectStartAndEndDate(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		
		ServiceResponse response = resourceManagementService.updateProjectStartAndEndDate(resourceManagementDTO);
		return response;
	}
	
	@RequestMapping(value = "/completionDateOfProject", method = RequestMethod.POST)
	public ServiceResponse completionDateOfProject(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		
		ServiceResponse response = resourceManagementService.completionDateOfProject(resourceManagementDTO);
		return response;
	}
	
	@RequestMapping(value = "/rbacInternalProjects", method = RequestMethod.POST)
	public ServiceResponse getAllInternalProjectsNewRMG(@RequestBody ProjectFilterDTO projectFilterDTO ) {
		ServiceResponse response = resourceManagementService.getAllInternalProjectsNewRMG(projectFilterDTO);
		return response;
	}
	
	@RequestMapping(value = "/rbacShankhProjects", method = RequestMethod.POST)
	public ServiceResponse getAllShankhProjectsNewRMG(@RequestBody ProjectFilterDTO projectFilterDTO ) {
		ServiceResponse response = resourceManagementService.getAllShankhProjectsNewRMG(projectFilterDTO);
		return response;
	}
	
	@RequestMapping(value = "/rbacAllShankhInternalProjects", method = RequestMethod.POST)
	public ServiceResponse getAllShankhInternalProjectsNewRMG(@RequestBody ProjectFilterDTO projectFilterDTO ) {
		ServiceResponse response = resourceManagementService.getAllShankhInternalProjectsNewRMG(projectFilterDTO);
		return response;
	}
	
	
	@RequestMapping(value = "/rbacBothShankhInternal", method = RequestMethod.POST)
	public ServiceResponse getBothShankhInternalProjectsNewRMG(@RequestBody ProjectFilterDTO projectFilterDTO ) {
		ServiceResponse response = resourceManagementService.getBothShankhInternalProjectsNewRMG(projectFilterDTO);
		return response;
	}
	
	@RequestMapping(value = "/projectLessEmployees", method = RequestMethod.POST)
	public ServiceResponse getEmployessWithoutProjects(@RequestBody ProjectFilterDTO projectFilterDTO) {
		ServiceResponse response = resourceManagementService.getEmployessWithoutProjects(projectFilterDTO);
		return response;
	}
	
	
	@RequestMapping(value = "/totalEmployeeCount", method = RequestMethod.GET)
	public ServiceResponse totalEmployeeCount() {
		ServiceResponse response = resourceManagementService.totalEmployeeCount();
		return response;
	}
	
	@GetMapping("/getResourceRequirementByPoProjectId")
	public ServiceResponse getResourceRequirementByPoProjectId(@RequestParam Long id) {
	    return resourceManagementService.getResourceRequirementByPoProjectId(id);
	}
	
	@GetMapping("/getEmployeeInformation")
	public ServiceResponse getEmployeeInformation(@RequestParam Long empId) {
	    return resourceManagementService.getEmployeeInformation(empId);
	}
	
	@GetMapping(value = "/exceptionEmployeeReport")
	public ServiceResponse exceptionEmployeeReport( ) {
		ServiceResponse response = resourceManagementService.getAllExceptionReport();
		return response;
	}

	@PostMapping("/poCrudOperationsInIshine")
	public ServiceResponse importAllNotStartedProjects(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		return resourceManagementService.crudOnAllNotstartedProjs(resourceManagementDTO);
	}
	
	@GetMapping("/poDump")
	public ServiceResponse getAllPOPortalDumpInIshineTemp() {
		return resourceManagementService.dumpPODataInIshine();
	}
	
	@GetMapping("/getPreviousDefaultProjectDetails")
	public ServiceResponse getPreviousDefaultProjectDetails(@RequestParam Long empId) {
	    return resourceManagementService.getPreviousDefaultProjectDetails(empId);
	}
	
	@PostMapping("/setDefaultProjectUpdateBillable")
	public ServiceResponse setDefaultProjectUpdateBillable(@RequestBody DefaultProjectUpdateDTO defaultProjectUpdateDTO) {
		return resourceManagementService.setDefaultProjectUpdateBillable(defaultProjectUpdateDTO);
	}
	
	@GetMapping("/getProjectDetailsForBulkDefaultUpdate")
	public ServiceResponse getProjectDetailsForBulkDefaultUpdate() {
	    return resourceManagementService.getProjectDetailsForBulkDefaultUpdate();
	}
	
	@PostMapping("/getEmployeeInformationBulk")
	public ServiceResponse getEmployeeInformationBulk(@RequestBody List<Long> empIds) {
	    return resourceManagementService.getEmployeeInformationBulk(empIds);
	}
	
	@PostMapping("/setProjectMappingAndDefaultProject")
	public ServiceResponse setProjectMappingAndDefaultProject(@RequestBody SetProjectMappingAndDefaultProjectDTO setProjectMappingAndDefaultProjectDTO ) {
	    return resourceManagementService.setProjectMappingAndDefaultProject(setProjectMappingAndDefaultProjectDTO);
	}
	
	@PostMapping("/getEmployeeInformationForDefaultProject")
	public ServiceResponse getEmployeeInformationForDefaultProject(@RequestBody OtherProjectSetDTO otherProjectSetDTO) {
	    return resourceManagementService.getEmployeeInformationForDefaultProject(otherProjectSetDTO);
	}

}
