package com.apmosys.employeeportal.controller;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

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
import com.apmosys.employeeportal.dto.GetEmployeeProjectReportPayloadDTO;
import com.apmosys.employeeportal.dto.HandleTeamsAsPerLinkedPoPayloadDTO;
import com.apmosys.employeeportal.dto.LiftAndShiftTeamsDTO;
import com.apmosys.employeeportal.dto.NonComplianceProjects;
import com.apmosys.employeeportal.dto.OtherProjectSetDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.ProjectFetchDTO;
import com.apmosys.employeeportal.dto.ProjectFilterDTO;
import com.apmosys.employeeportal.dto.ProjectStructureWrapper;
import com.apmosys.employeeportal.dto.ResourceManagementDTO;
import com.apmosys.employeeportal.dto.RestoreProjectPayloadDTO;
import com.apmosys.employeeportal.dto.SetProjectMappingAndDefaultProjectDTO;
import com.apmosys.employeeportal.dto.TeamDTO;
import com.apmosys.employeeportal.dto.TimeSheetRequestDto;
import com.apmosys.employeeportal.dto.UpdateHasClientSideIdDTO;
import com.apmosys.employeeportal.service.ResourceManagementService;
import com.apmosys.employeeportal.utility.PoPortalAPIAuthenticationJWTUtility;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api")
public class ResourceManagementController {
	
	
	@Autowired
	ResourceManagementService resourceManagementService;
	
	@Autowired
	private PoPortalAPIAuthenticationJWTUtility poPortalAPIAuthenticationJWTUtility;

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
	
	@PostMapping(value = "/bulkSyncProject")
	public ServiceResponse bulkSyncProject(@RequestBody ProjectDTO projectDTO) {
		return resourceManagementService.bulkSyncProject(projectDTO);
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
	
	@GetMapping("/getTeamInfo")
	public ServiceResponse getTeamInfo(@RequestParam Integer projectId) {
		
		ServiceResponse response = resourceManagementService.getTeamInfo(projectId);
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
	
	@GetMapping("/getAllExpiredTNMProject")
	public ServiceResponse getAllExpiredTNMProject() {
	    return resourceManagementService.getAllExpiredTNMProject();
	}
	
	
	
	 @PostMapping("/combinedPOINTERNALCountList")
	    public ServiceResponse combinedDataCount(@RequestBody ProjectFilterDTO projectFilterDTO) {
	        return resourceManagementService.combinedDataCount(projectFilterDTO);
	    }
	 @PostMapping("/combinedPOINTERNALDataList")
	    public ServiceResponse combinedDataList(@RequestBody ProjectFilterDTO projectFilterDTO) {
	        return resourceManagementService.combinedDataList(projectFilterDTO);
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
		ServiceResponse response = resourceManagementService.nEWgetAllInternalProjectsNewRMG(projectFilterDTO);
		return response;
	}
	
	@RequestMapping(value ="/rbacUnfilledTimesheetsProjects", method = RequestMethod.POST)
	public ServiceResponse getAllUnfilledTimesheetsProjects(@RequestBody NonComplianceProjects nonComplianceProjects  ) {
		ServiceResponse response = resourceManagementService.getAllUnfilledTimesheetsProjects(nonComplianceProjects);
		return response;
	}
	
	@RequestMapping(value = "/rbacShankhProjects", method = RequestMethod.POST)
	public ServiceResponse getAllShankhProjectsNewRMG(@RequestBody ProjectFilterDTO projectFilterDTO ) {
		ServiceResponse response = resourceManagementService.nEWgetAllShankhProjectsNewRMG(projectFilterDTO);
		return response;
	}
	
	@RequestMapping(value = "/rbacAllShankhInternalProjects", method = RequestMethod.POST)
	public ServiceResponse getAllShankhInternalProjectsNewRMG(@RequestBody ProjectFilterDTO projectFilterDTO ) {
		ServiceResponse response = resourceManagementService.nEWgetAllShankhInternalProjectsNewRMG(projectFilterDTO);
		return response;
	}
	
	@RequestMapping(value = "/employeesMappedProjectsDepartmentWise", method = RequestMethod.POST)
	public ServiceResponse employeesMappedProjectsDepartmentWise(@RequestBody GetEmployeeProjectReportPayloadDTO getEmployeeProjectReportPayloadDTO ) {
		ServiceResponse response = resourceManagementService.employeesMappedProjectsDepartmentWise(getEmployeeProjectReportPayloadDTO);
		return response;
	}
	
	
	@RequestMapping(value = "/rbacBothShankhInternal", method = RequestMethod.POST)
	public ServiceResponse getBothShankhInternalProjectsNewRMG(@RequestBody ProjectFilterDTO projectFilterDTO ) {
		ServiceResponse response = resourceManagementService.nEWgetBOTHShankhInternalProjectsNewRMG(projectFilterDTO);
		return response;
	}
	
	@RequestMapping(value = "/projectLessEmployees", method = RequestMethod.POST)
	public ServiceResponse getEmployessWithoutProjects(@RequestBody ProjectFilterDTO projectFilterDTO) {
		ServiceResponse response = resourceManagementService.getEmployessWithoutProjects(projectFilterDTO);
		return response;
	}
	
	@RequestMapping(value = "/getEmployessWithoutBillable", method = RequestMethod.POST)
	public ServiceResponse getEmployessWithoutBillable(@RequestBody ProjectFilterDTO projectFilterDTO) {
		ServiceResponse response = resourceManagementService.getEmployessWithoutBillable(projectFilterDTO);
		return response;
	}
	
	@PostMapping(value = "/exceptionEmployeeReport")
	public ServiceResponse exceptionEmployeeReport( @RequestBody ProjectFilterDTO projectFilterDTO) {
		ServiceResponse response = resourceManagementService.getAllExceptionReport(projectFilterDTO);
		return response;
	}
	
	@RequestMapping(value = "/projectLessEmployeesDepartmentWise", method = RequestMethod.POST)
	public ServiceResponse getEmployessWithoutProjectsDepartmentWise(@RequestBody GetEmployeeProjectReportPayloadDTO getEmployeeProjectReportPayloadDTO) {
		ServiceResponse response = resourceManagementService.getEmployessWithoutProjectsDepartmentWise(getEmployeeProjectReportPayloadDTO);
		return response;
	}
	
	
	
	
	@RequestMapping(value = "/totalEmployeeCount", method = RequestMethod.GET)
	public ServiceResponse totalEmployeeCount() {
		ServiceResponse response = resourceManagementService.totalEmployeeCount();
		return response;
	}
	
	@RequestMapping(value = "/totalEmployeeCountInDepartments", method = RequestMethod.POST)
	public ServiceResponse totalEmployeeCountInDepartments(@RequestBody GetEmployeeProjectReportPayloadDTO getEmployeeProjectReportPayloadDTO) {
		ServiceResponse response = resourceManagementService.totalEmployeeCountInDepartments(getEmployeeProjectReportPayloadDTO);
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
	
	@GetMapping("/poDump")
	public ServiceResponse getAllPOPortalDumpInIshineTemp() {
		return resourceManagementService.dumpPODataInIshine();
	}
	
	@GetMapping("/fillDepartmentforAllProjectsInIshine")
	public ServiceResponse fillDepartmentforAllProjectsInIshine() {
		return resourceManagementService.fillDepartmentforAllProjectsInIshine();
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
	
	@PostMapping("/getAllResourceRequirementForProject")
	public ServiceResponse getAllResourceRequirementForProject(@RequestBody ProjectFetchDTO projectFetchDTO) {
		return resourceManagementService.getAllResourceRequirementForProject(projectFetchDTO);
	}
	
	@PostMapping("/getBenchEmployeeMoreThan30Days")
	public ServiceResponse getBenchEmployeeMoreThan30Days(@RequestBody ProjectFilterDTO projectFilterDTO) {
	    return resourceManagementService.getBenchEmployeeMoreThan30Days(projectFilterDTO);
	}
	
	
	@RequestMapping(value = "/getProjectTimesheetSummary", method = RequestMethod.POST)
	public ServiceResponse getProjectTimesheetSummary(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		ServiceResponse response = resourceManagementService.getProjectTimesheetSummary(resourceManagementDTO);
		return response;
	}
	
	@PostMapping("/getProjectStatusByPoProjectId")
	public ServiceResponse getProjectStatusByPoProjectId(HttpServletRequest httpRequest,@RequestBody Set<Long> projectIds) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
	    return resourceManagementService.getProjectStatusByPoProjectId(projectIds);
	}
	
	@PostMapping("/getDeptsByRole")
	public ServiceResponse getDeptsByRole(@RequestBody Long currentUserEmpId) {
	    return resourceManagementService.getDeptsByRole(currentUserEmpId);
	}
	
	@PostMapping("/getDeptsByUser")
	public ServiceResponse getDeptsByUser(@RequestBody Long currentUserEmpId) {
	    return resourceManagementService.getDeptsByUser(currentUserEmpId);
	}
	
	 @PostMapping("/poCrudOperationsInIshine")
	 public ServiceResponse poCrudOperationsInIshine(HttpServletRequest httpRequest,@RequestBody ResourceManagementDTO poPortalProjects) {
	 	poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
	 	return resourceManagementService.poCrudOperationsInIshine(poPortalProjects);
	 }


//	@PostMapping("/poCrudOperationsInIshine")
//	public ServiceResponse importAllNotStartedProjects(@RequestBody ResourceManagementDTO resourceManagementDTO) {
//		return resourceManagementService.crudOnAllNotstartedProjs(resourceManagementDTO);
//	}

	@GetMapping("/deleteTempProjects")
//	@Scheduled(cron = "${project.temp.logs}")
	public ServiceResponse deleteProjectTemp() {
	    return resourceManagementService.deleteProjectTemp();
	}
//	@GetMapping("/deleteTempProjects")
//	@Scheduled(cron = "${project.temp.logs}")
//	public ServiceResponse deleteProjectTemp() {
//	    return resourceManagementService.deleteProjectTemp();
//	}
	
	@PostMapping("/updateHasClientSideId")
    public ServiceResponse updateHasClientSideId(@RequestBody UpdateHasClientSideIdDTO dto) {
        return resourceManagementService.updateHasClientSideId(dto);
    }

	@GetMapping("/getActiveProjectList")
	public ServiceResponse getActiveProjectList() {
	    return resourceManagementService.getActiveProjectList();
	}
	
	@PostMapping("/liftAndShiftTeams")
    public ServiceResponse liftAndShiftTeams(@RequestBody LiftAndShiftTeamsDTO dto) {
        return resourceManagementService.liftAndShiftTeams(dto);
    }
	
	@PostMapping("/fetchHasClientSideId")
    public ServiceResponse fetchHasClientSideId(@RequestBody UpdateHasClientSideIdDTO dto) {
        return resourceManagementService.fetchHasClientSideId(dto);
    }	
	
	@PostMapping("/getProjectStructure")
	public ServiceResponse getProjectStructure(@RequestBody ProjectStructureWrapper wrapper) {
	    return resourceManagementService.getProjectStructure(wrapper.getProjectStructure(),
	                                                         wrapper.getProjectFilter());
	}

	@PostMapping(value = "/sendTimesheetDetailsToShankh")
	public ServiceResponse sendTimesheetDetailsToShankh(HttpServletRequest httpRequest,@RequestBody TimeSheetRequestDto payloadDTO) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		return resourceManagementService.sendTimesheetDetailsToShankh(payloadDTO);
	}
	
	@PostMapping("/filterPoProjectsHavingTeam")
	 public ServiceResponse filterPoProjectsHavingTeam(HttpServletRequest httpRequest, @RequestBody List<Long> proIds) {
	 	poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
	 	return resourceManagementService.filterPoProjectsHavingTeam(proIds);
	 }

	@PostMapping("/getResourceCountFromProjectId")
	 public ServiceResponse getResourceCountFromProjectId(HttpServletRequest httpRequest, @RequestBody List<Long> proIds) {
	 	poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
	 	return resourceManagementService.getResourceCountFromProjectId(proIds);
	 }
	
	@PostMapping("/getDocumentDataByDocIdForPO")
	 public ServiceResponse getDocumentDataByDocId(HttpServletRequest httpRequest,@RequestBody Long docId) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
	 	return resourceManagementService.getDocumentDataByDocId(docId);
	 }
	
	@PostMapping(value = "/handleTeamsAsPerLinkedPo")
	public ServiceResponse handleTeamsAsPerLinkedPo(@RequestBody HandleTeamsAsPerLinkedPoPayloadDTO payloadDTO) {
		return resourceManagementService.handleTeamsAsPerLinkedPo(payloadDTO);
	}
	
//	@PostMapping(value = "/sendTimesheetDetailsToShankh")
//	public ServiceResponse sendTimesheetDetailsToShankh(@RequestBody TimeSheetRequestDto payloadDTO) {
//		return resourceManagementService.sendTimesheetDetailsToShankh(payloadDTO);
//	}
	
	@GetMapping("/getAllApprovedPoWithTimesheet")
	 public ServiceResponse getAllApprovedPoWithTimesheet(HttpServletRequest httpRequest) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
	 	return resourceManagementService.getAllApprovedPoWithTimesheet();
	 }
	
	
	@PostMapping("/getActiveTeamAndTimeSheetWithForRm")
	 public ServiceResponse getActiveTeamAndTimeSheetWithForRm(HttpServletRequest httpRequest,@RequestBody List<Long> projectId) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
	 	return resourceManagementService.getActiveTeamAndTimeSheetWithForRm(projectId);
	 }
	
	@PostMapping(value = "/checkActiveAndPendingEmployeeMappingWithResourceOverViewId")
	public ServiceResponse checkActiveAndPendingEmployeeMappingWithResourceOverViewId(HttpServletRequest httpRequest,@RequestBody List<Long> resourceOverviewId) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		return resourceManagementService.checkActiveAndPendingEmployeeMappingWithResourceOverViewId(resourceOverviewId);
	}
	
	@PostMapping(value = "/restorePreviousStateOfProject")
	public ServiceResponse restorePreviousStateOfProject(@RequestBody RestoreProjectPayloadDTO payloadDTO) {
		return resourceManagementService.restorePreviousStateOfProject(payloadDTO);
	}
}