package com.apmosys.employeeportal.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.Encrypted;
import com.apmosys.employeeportal.JobRoleAccess;
import com.apmosys.employeeportal.dto.DefaultProjectUpdateDTO;
import com.apmosys.employeeportal.dto.DeletedPoSyncDTO;
import com.apmosys.employeeportal.dto.EmployeeTeamDepartmentDTO;
import com.apmosys.employeeportal.dto.FilterMatrix;
import com.apmosys.employeeportal.dto.GetEmployeeProjectReportPayloadDTO;
import com.apmosys.employeeportal.dto.IshineLinkProjectDto;
import com.apmosys.employeeportal.dto.IshineToPoRequestDTO;
import com.apmosys.employeeportal.dto.LiftAndShiftTeamsDTO;
import com.apmosys.employeeportal.dto.NonComplianceProjects;
import com.apmosys.employeeportal.dto.OtherProjectSetDTO;
import com.apmosys.employeeportal.dto.PageDTO;
import com.apmosys.employeeportal.dto.PoClientAddressUpdateDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.ProjectFetchDTO;
import com.apmosys.employeeportal.dto.ProjectFilterDTO;
import com.apmosys.employeeportal.dto.ProjectPoMappingWithResourceDTO;
import com.apmosys.employeeportal.dto.RMGDashboardProjectRequest;
import com.apmosys.employeeportal.dto.RenewedPoSyncDto;
import com.apmosys.employeeportal.dto.ResourceManagementDTO;
import com.apmosys.employeeportal.dto.RestoreProjectPayloadDTO;
import com.apmosys.employeeportal.dto.RmUpdateSyncDto;
import com.apmosys.employeeportal.dto.SetProjectMappingAndDefaultProjectDTO;
import com.apmosys.employeeportal.dto.TeamDTO;
import com.apmosys.employeeportal.dto.TimeSheetRequestDto;
import com.apmosys.employeeportal.dto.UpdateHasClientSideIdDTO;
import com.apmosys.employeeportal.service.CronJobService;
import com.apmosys.employeeportal.service.PoSyncOrchestratorService;
import com.apmosys.employeeportal.service.ResourceManagementService;
import com.apmosys.employeeportal.utility.PoPortalAPIAuthenticationJWTUtility;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api")
public class ResourceManagementController {
	
	
	@Autowired
	ResourceManagementService resourceManagementService;
	
	@Autowired
	PoSyncOrchestratorService poSyncOrchestratorService;
	
	@Autowired
	private PoPortalAPIAuthenticationJWTUtility poPortalAPIAuthenticationJWTUtility;

	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@RequestMapping(value = "/createDraftProjectInfo", method = RequestMethod.POST)
	public ServiceResponse createDraftProjectInfo(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		
		ServiceResponse response = resourceManagementService.createDraftProjectInfo(resourceManagementDTO);
		return response;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {3,34})
	@RequestMapping(value = "/getTeamListByProjectName", method = RequestMethod.POST)
	public ServiceResponse getTeamListByProjectName(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		
		ServiceResponse response = resourceManagementService.getTeamListByProjectName(resourceManagementDTO);
		return response;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@RequestMapping(value = "/alreadyCreatedTeam", method = RequestMethod.GET)
	public ServiceResponse alreadyCreatedTeam() {
		
		ServiceResponse response = resourceManagementService.alreadyCreatedTeam();
		return response;
	}
	@Encrypted
	@RequestMapping(value = "/getPendingForApprovalProject", method = RequestMethod.GET)
	public ServiceResponse getPendingForApprovalProject() {
		
		ServiceResponse response = resourceManagementService.getPendingForApprovalProject();
		return response;
	}
	
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@PostMapping(value = "/approvePendingProject")
	public ServiceResponse approvePendingProject(@RequestBody ResourceManagementDTO resourceManagementDTO) throws Exception {
		ServiceResponse response = resourceManagementService.approvePendingProject(resourceManagementDTO);
		return response;
	}
	
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@RequestMapping(value = "/rejectPendingProject", method = RequestMethod.POST)
	public ServiceResponse rejectPendingProject(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		
		ServiceResponse response = resourceManagementService.rejectPendingProject(resourceManagementDTO);
		return response;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@RequestMapping(value = "/sendProjectApproval", method = RequestMethod.POST)
	public ServiceResponse sendProjectApproval(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		
		ServiceResponse response = resourceManagementService.sendProjectApproval(resourceManagementDTO);
		return response;
	}
	@Encrypted
	@RequestMapping(value = "/sendProjectInfoToPoPortal", method = RequestMethod.POST)
	public ServiceResponse sendProjectInfoToPoPortal(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		
		ServiceResponse response = resourceManagementService.sendProjectInfoToPoPortal(resourceManagementDTO);
		return response;
	}
	
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@PostMapping(value = "/bulkSyncProject")
	public ServiceResponse bulkSyncProject(@RequestBody ProjectDTO projectDTO) {
		return resourceManagementService.bulkSyncProject(projectDTO);
	}
	@Encrypted
	@JobRoleAccess(featureIds = {3,34})
	@RequestMapping(value = "/approveProject", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
	public String approveProject(@RequestParam(name = "id") String id,@RequestParam(name = "status") String status) {
		return "<html>\n" + "<header><title>Welcome</title></header>\n" +
		          "<body>\n" + "<h1>Your Request for Project "+ id +" is "+ status +"!!</h1>" + "</body>\n" + "</html>";
	}
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@RequestMapping(value = "/getInternalProject", method = RequestMethod.GET)
	public ServiceResponse getInternalProject() {
		
		ServiceResponse response = resourceManagementService.getInternalProject();
		return response;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {3,7,34})
	@RequestMapping(value = "/getExistingProjectsAndTeamsByEmployee", method = RequestMethod.POST)
	public ServiceResponse getExistingProjectsAndTeamsByEmployee(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		
		ServiceResponse response = resourceManagementService.getExistingProjectsAndTeamsByEmployee(resourceManagementDTO);
		return response;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {3,7,34})
	@RequestMapping(value = "/updateProjectResourceAsInActive", method = RequestMethod.POST)
	public ServiceResponse updateProjectResourceAsInActive(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		
		ServiceResponse response = resourceManagementService.updateProjectResourceAsInActive(resourceManagementDTO);
		return response;
	}
	
	
	@Encrypted
	@JobRoleAccess(featureIds = {7,34})
	@RequestMapping(value = "/deleteTeamByTeamId", method = RequestMethod.POST)
	public ServiceResponse deleteTeamByTeamId(@RequestBody TeamDTO teamDto) {
		
		ServiceResponse response = resourceManagementService.deleteTeamByTeamId(teamDto);
		return response;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {3,7})
	@RequestMapping(value = "/getProjectInfo", method = RequestMethod.POST)
	public ServiceResponse getProjectInfo(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		
		ServiceResponse response = resourceManagementService.getProjectInfo(resourceManagementDTO);
		return response;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {7})
	@RequestMapping(value = "/getPoProjectInfo", method = RequestMethod.POST)
	public ServiceResponse getPoProjectInfo(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		
		ServiceResponse response = resourceManagementService.getPoProjectInfo(resourceManagementDTO);
		return response;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {3,7})
	@GetMapping("/getTeamInfo")
	public ServiceResponse getTeamInfo(@RequestParam Integer projectId) {
		
		ServiceResponse response = resourceManagementService.getTeamInfo(projectId);
		return response;
	}
	@JobRoleAccess(featureIds = {3})
	@RequestMapping(value = "/getTeamMemberByTeamId/{teamId}", method = RequestMethod.GET)
	public ServiceResponse getTeamMemberByTeamId(@PathVariable("teamId") Long teamId) {
		
		ServiceResponse response = resourceManagementService.getTeamMemberByTeamId(teamId);
		return response;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@RequestMapping(value = "/syncPoProjectDetailsByProjectId", method = RequestMethod.POST)
	public ServiceResponse syncPoProjectDetailsByProjectId(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		
		ServiceResponse response = resourceManagementService.syncPoProjectDetailsByProjectId(resourceManagementDTO);
		return response;
	}
	@Encrypted
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
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@PostMapping("/sendEmailNotificationToBDTeam")
	public ServiceResponse sendEmailNotificationToBDTeam(@RequestBody ResourceManagementDTO resourceManagementDTO) {
	    return resourceManagementService.sendEmailNotificationToBDTeam(resourceManagementDTO);
	}
	@Encrypted
	@JobRoleAccess(featureIds = {34,26})
	@GetMapping("/getEmployeeByNameAndEmpld")
	public ServiceResponse getEmployeeByNameAndEmpld() {
	    return resourceManagementService.getEmployeeByNameAndEmpld();
	}
	@Encrypted
	@GetMapping("/getAllExpiredTNMProject")
	public ServiceResponse getAllExpiredTNMProject() {
	    return resourceManagementService.getAllExpiredTNMProject();
	}
	
	
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	 @PostMapping("/combinedPOINTERNALCountList")
	    public ServiceResponse combinedDataCount(@RequestBody ProjectFilterDTO projectFilterDTO) {
	        return resourceManagementService.combinedDataCount(projectFilterDTO);
	    }
	 
	@Encrypted
	@JobRoleAccess(featureIds = {34,7})
	 @PostMapping("/combinedPOINTERNALDataList")
	    public ServiceResponse combinedDataList(@RequestBody ProjectFilterDTO projectFilterDTO) {
	        return resourceManagementService.combinedDataList(projectFilterDTO);
	    }
	
	 @Encrypted
	 @JobRoleAccess(featureIds = {34})
	@RequestMapping(value = "/deleteTeamsByIdsBulk", method = RequestMethod.POST)
	public ServiceResponse deleteTeamsByIdsBulk(@RequestBody List<TeamDTO> teamDTO) {
		
		ServiceResponse response = resourceManagementService.deleteTeamsByIdsBulk(teamDTO);
		return response;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {34,7})
	@RequestMapping(value = "/updateProjectResourcesAsInActiveBulk", method = RequestMethod.POST)
	public ServiceResponse updateProjectResourcesAsInActiveBulk(@RequestBody List<ResourceManagementDTO> resourceManagementDTOList) {
	    ServiceResponse response = resourceManagementService.updateProjectResourcesAsInActiveBulk(resourceManagementDTOList);
	    return response;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {3})
	@RequestMapping(value = "/updateProjectStartAndEndDate", method = RequestMethod.POST)
	public ServiceResponse updateProjectStartAndEndDate(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		
		ServiceResponse response = resourceManagementService.updateProjectStartAndEndDate(resourceManagementDTO);
		return response;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@RequestMapping(value = "/completionDateOfProject", method = RequestMethod.POST)
	public ServiceResponse completionDateOfProject(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		
		ServiceResponse response = resourceManagementService.completionDateOfProject(resourceManagementDTO);
		return response;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@RequestMapping(value = "/rbacInternalProjects", method = RequestMethod.POST)
	public ServiceResponse getAllInternalProjectsNewRMG(@RequestBody ProjectFilterDTO projectFilterDTO ) {
		ServiceResponse response = resourceManagementService.nEWgetAllInternalProjectsNewRMG(projectFilterDTO);
		return response;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@RequestMapping(value ="/rbacUnfilledTimesheetsProjects", method = RequestMethod.POST)
	public ServiceResponse getAllUnfilledTimesheetsProjects(@RequestBody NonComplianceProjects nonComplianceProjects  ) {
		ServiceResponse response = resourceManagementService.getAllUnfilledTimesheetsProjectsNEW(nonComplianceProjects);
		return response;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@RequestMapping(value = "/rbacShankhProjects", method = RequestMethod.POST)
	public ServiceResponse getAllShankhProjectsNewRMG(@RequestBody ProjectFilterDTO projectFilterDTO ) {
		ServiceResponse response = resourceManagementService.nEWgetAllShankhProjectsNewRMG(projectFilterDTO);
		return response;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@RequestMapping(value = "/rbacAllShankhInternalProjects", method = RequestMethod.POST)
	public ServiceResponse getAllShankhInternalProjectsNewRMG(@RequestBody ProjectFilterDTO projectFilterDTO ) {
		ServiceResponse response = resourceManagementService.nEWgetAllShankhInternalProjectsNewRMG(projectFilterDTO);
		return response;
	}
	@Encrypted
	@RequestMapping(value = "/employeesMappedProjectsDepartmentWise", method = RequestMethod.POST)
	public ServiceResponse employeesMappedProjectsDepartmentWise(@RequestBody GetEmployeeProjectReportPayloadDTO getEmployeeProjectReportPayloadDTO ) {
		ServiceResponse response = resourceManagementService.employeesMappedProjectsDepartmentWise(getEmployeeProjectReportPayloadDTO);
		return response;
	}
	
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@RequestMapping(value = "/rbacBothShankhInternal", method = RequestMethod.POST)
	public ServiceResponse getBothShankhInternalProjectsNewRMG(@RequestBody ProjectFilterDTO projectFilterDTO ) {
		ServiceResponse response = resourceManagementService.nEWgetBOTHShankhInternalProjectsNewRMG(projectFilterDTO);
		return response;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@RequestMapping(value = "/projectLessEmployees", method = RequestMethod.POST)
	public ServiceResponse getEmployessWithoutProjects(@RequestBody ProjectFilterDTO projectFilterDTO) {
		ServiceResponse response = resourceManagementService.getEmployessWithoutProjects(projectFilterDTO);
		return response;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@RequestMapping(value = "/getEmployessWithoutBillable", method = RequestMethod.POST)
	public ServiceResponse getEmployessWithoutBillable(@RequestBody ProjectFilterDTO projectFilterDTO) {
		ServiceResponse response = resourceManagementService.getEmployessWithoutBillable(projectFilterDTO);
		return response;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@PostMapping(value = "/exceptionEmployeeReport")
	public ServiceResponse exceptionEmployeeReport( @RequestBody ProjectFilterDTO projectFilterDTO) {
		ServiceResponse response = resourceManagementService.getAllExceptionReport(projectFilterDTO);
		return response;
	}
	@Encrypted
	@RequestMapping(value = "/projectLessEmployeesDepartmentWise", method = RequestMethod.POST)
	public ServiceResponse getEmployessWithoutProjectsDepartmentWise(@RequestBody GetEmployeeProjectReportPayloadDTO getEmployeeProjectReportPayloadDTO) {
		ServiceResponse response = resourceManagementService.getEmployessWithoutProjectsDepartmentWise(getEmployeeProjectReportPayloadDTO);
		return response;
	}
	
	
	
	@Encrypted
	@JobRoleAccess(featureIds = {34,26})
	@RequestMapping(value = "/totalEmployeeCount", method = RequestMethod.GET)
	public ServiceResponse totalEmployeeCount() {
		ServiceResponse response = resourceManagementService.totalEmployeeCount();
		return response;
	}
	@Encrypted
	@RequestMapping(value = "/totalEmployeeCountInDepartments", method = RequestMethod.POST)
	public ServiceResponse totalEmployeeCountInDepartments(@RequestBody GetEmployeeProjectReportPayloadDTO getEmployeeProjectReportPayloadDTO) {
		ServiceResponse response = resourceManagementService.totalEmployeeCountInDepartments(getEmployeeProjectReportPayloadDTO);
		return response;
	}
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@GetMapping("/getResourceRequirementByPoProjectId")
	public ServiceResponse getResourceRequirementByPoProjectId(@RequestParam Long id,@RequestParam String type) {
	    return resourceManagementService.getResourceRequirementByPoProjectId(id,type);
	}
	@Encrypted
	@JobRoleAccess(featureIds = {3,7,34})
	@GetMapping("/getEmployeeInformation")
	public ServiceResponse getEmployeeInformation(@RequestParam Long empId) {
	    return resourceManagementService.getEmployeeInformation(empId);
	}
	
	@GetMapping("/poDump")
	public ServiceResponse getAllPOPortalDumpInIshineTemp() {
		return resourceManagementService.dumpPODataInIshine();
	}
	@Encrypted
	@GetMapping("/fillDepartmentforAllProjectsInIshine")
	public ServiceResponse fillDepartmentforAllProjectsInIshine() {
		return resourceManagementService.fillDepartmentforAllProjectsInIshine();
	}
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@GetMapping("/getPreviousDefaultProjectDetails")
	public ServiceResponse getPreviousDefaultProjectDetails(@RequestParam Long empId) {
	    return resourceManagementService.getPreviousDefaultProjectDetails(empId);
	}
	@Encrypted
	@JobRoleAccess(featureIds = {3,7,34})
	@PostMapping("/setDefaultProjectUpdateBillable")
	public ServiceResponse setDefaultProjectUpdateBillable(@RequestBody DefaultProjectUpdateDTO defaultProjectUpdateDTO) {
		return resourceManagementService.setDefaultProjectUpdateBillable(defaultProjectUpdateDTO);
	}
	@Encrypted
	@JobRoleAccess(featureIds = {3,7,34})
	@GetMapping("/getProjectDetailsForBulkDefaultUpdate")
	public ServiceResponse getProjectDetailsForBulkDefaultUpdate() {
	    return resourceManagementService.getProjectDetailsForBulkDefaultUpdate();
	}
	@Encrypted
	@JobRoleAccess(featureIds = {3,7,34})
	@PostMapping("/getEmployeeInformationBulk")
	public ServiceResponse getEmployeeInformationBulk(@RequestBody List<Long> empIds) {
	    return resourceManagementService.getEmployeeInformationBulk(empIds);
	}
	@Encrypted
	@JobRoleAccess(featureIds = {3,7,34})
	@PostMapping("/setProjectMappingAndDefaultProject")
	public ServiceResponse setProjectMappingAndDefaultProject(@RequestBody SetProjectMappingAndDefaultProjectDTO setProjectMappingAndDefaultProjectDTO ) {
	    return resourceManagementService.setProjectMappingAndDefaultProject(setProjectMappingAndDefaultProjectDTO);
	}
	@Encrypted
	@JobRoleAccess(featureIds = {3,7,34})
	@PostMapping("/getEmployeeInformationForDefaultProject")
	public ServiceResponse getEmployeeInformationForDefaultProject(@RequestBody OtherProjectSetDTO otherProjectSetDTO) {
	    return resourceManagementService.getEmployeeInformationForDefaultProject(otherProjectSetDTO);
	}
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@PostMapping("/getAllResourceRequirementForProject")
	public ServiceResponse getAllResourceRequirementForProject(@RequestBody ProjectFetchDTO projectFetchDTO) {
		return resourceManagementService.getAllResourceRequirementForProject(projectFetchDTO);
	}
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@PostMapping("/getBenchEmployeeMoreThan30Days")
	public ServiceResponse getBenchEmployeeMoreThan30Days(@RequestBody ProjectFilterDTO projectFilterDTO) {
	    return resourceManagementService.getBenchEmployeeMoreThan30Days(projectFilterDTO);
	}
	
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@RequestMapping(value = "/getProjectTimesheetSummary", method = RequestMethod.POST)
	public ServiceResponse getProjectTimesheetSummary(@RequestBody ResourceManagementDTO resourceManagementDTO) {
		ServiceResponse response = resourceManagementService.getProjectTimesheetSummary(resourceManagementDTO);
		return response;
	}
	@Encrypted
	@PostMapping("/getProjectStatusByPoProjectId")
	public ServiceResponse getProjectStatusByPoProjectId(HttpServletRequest httpRequest,@RequestBody Set<Long> projectIds) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
	    return resourceManagementService.getProjectStatusByPoProjectId(projectIds);
	}
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@PostMapping("/getDeptsByRole")
	public ServiceResponse getDeptsByRole(@RequestBody Long currentUserEmpId) {
	    return resourceManagementService.getDeptsByRole(currentUserEmpId);
	}
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@PostMapping("/getDeptsByUser")
	public ServiceResponse getDeptsByUser(@RequestBody Long currentUserEmpId) {
	    return resourceManagementService.getDeptsByUser(currentUserEmpId);
	}
	
	 @PostMapping("/poCrudOperationsInIshine")
	 public ServiceResponse poCrudOperationsInIshine(HttpServletRequest httpRequest,@RequestBody ResourceManagementDTO poPortalProjects) {
	 	poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
	 	return resourceManagementService.poCrudOperationsInIshine(poPortalProjects);
	 }
	 
	 
	 @PostMapping("/poCrudOperationsInIshineNew")
	 public ServiceResponse poCrudOperationsInIshineNew(HttpServletRequest httpRequest,@RequestBody ProjectPoMappingWithResourceDTO poPortalProjects) {
		 poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		 return poSyncOrchestratorService.poCrudOperationsInIshineNew(poPortalProjects);
	 }
	 
	 
	 @PostMapping("/renewPoInIshineNew")
	 public ServiceResponse renewPoInIshineNew(HttpServletRequest httpRequest, @Valid @RequestBody RenewedPoSyncDto dto) {
		 poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		 return poSyncOrchestratorService.renewPoInIshineNew(dto);
	 }
	 
	 
	 @PostMapping("/deletePoInIshineNew")
	 public ServiceResponse deletePoInIshineNew(HttpServletRequest httpRequest,@Valid @RequestBody  DeletedPoSyncDTO dto) {
		 poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		 return poSyncOrchestratorService.deletePoInIshineNew(dto);
	 }
	 
	 
	 @PostMapping("/linkPoInIshineNew")
	 public ServiceResponse linkPoInIshineNew(HttpServletRequest httpRequest,@RequestBody  IshineLinkProjectDto dto) {
		 poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		 return poSyncOrchestratorService.linkPoInIshineNew(dto);
	 }
	 
	 @PostMapping("/updateRmDetailsInPo")
	 public ServiceResponse updateRmDetailsInPo(HttpServletRequest httpRequest,@RequestBody  RmUpdateSyncDto dto) {
		 poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		 return poSyncOrchestratorService.updateRmOdPos(dto);
	 }
	 
	 
	 @PostMapping("/updateAddressInPos")
	 public ServiceResponse updateAddressInPos(HttpServletRequest httpRequest,
			 @Valid @RequestBody PoClientAddressUpdateDTO dto) {
		 poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		 return poSyncOrchestratorService.updateAddressInPos(dto);
	 }
	 
	 
	 


//	@PostMapping("/poCrudOperationsInIshine")
//	public ServiceResponse importAllNotStartedProjects(@RequestBody ResourceManagementDTO resourceManagementDTO) {
//		return resourceManagementService.crudOnAllNotstartedProjs(resourceManagementDTO);
//	}
	@Encrypted
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
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@PostMapping("/updateHasClientSideId")
    public ServiceResponse updateHasClientSideId(@RequestBody UpdateHasClientSideIdDTO dto) {
        return resourceManagementService.updateHasClientSideId(dto);
    }
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@GetMapping("/getActiveProjectList")
	public ServiceResponse getActiveProjectList() {
	    return resourceManagementService.getActiveProjectList();
	}
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@PostMapping("/liftAndShiftTeams")
    public ServiceResponse liftAndShiftTeams(@RequestBody LiftAndShiftTeamsDTO dto) {
        return resourceManagementService.liftAndShiftTeams(dto);
    }
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@PostMapping("/fetchHasClientSideId")
    public ServiceResponse fetchHasClientSideId(@RequestBody UpdateHasClientSideIdDTO dto) {
        return resourceManagementService.fetchHasClientSideId(dto);
    }	
	
    
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@PostMapping("/getProjectStructure")
	public ServiceResponse getProjectStructure(@RequestBody ProjectStructureRequest projectStructure) {
	    return resourceManagementService.getProjectStructure(projectStructure);
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
	 public ServiceResponse getDocumentDataByDocId(HttpServletRequest httpRequest,@RequestBody Long docId) throws Exception {
		 poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
	 	return resourceManagementService.getDocumentDataByDocId(docId);
	 }
	
	
	@PostMapping("/getResourceCountFromPoId")
	 public ServiceResponse getResourceCountFromPoId(HttpServletRequest httpRequest,@RequestBody List<Long> poIds) throws Exception {
		 poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
	 	return resourceManagementService.getResourceCountFromPoId(poIds);
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
	
	@JobRoleAccess(featureIds = {34})
	@PostMapping(value="/matrixCertificationDropdownRbac")
	public ServiceResponse getCertificatesRbac(@RequestBody FilterMatrix filterMatrix) {
		return resourceManagementService.getCertificatesRbac(filterMatrix);
	}
	
	@JobRoleAccess(featureIds = {34})
	@PostMapping(value="/matrixDepartmentDropdownRbac")
	public ServiceResponse getDepartmentsRbac(@RequestBody FilterMatrix filterMatrix) {
		return resourceManagementService.getDepartmentsRbac(filterMatrix);
	}

	@JobRoleAccess(featureIds = {34})
	@PostMapping(value = "/restorePreviousStateOfProject")
	public ServiceResponse restorePreviousStateOfProject(@RequestBody RestoreProjectPayloadDTO payloadDTO) {
		try {
			return resourceManagementService.restorePreviousStateOfProject(payloadDTO);
		} catch (Exception e) {
			ServiceResponse response = new ServiceResponse();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse(e.getMessage());
            System.err.println(e.getMessage());
            return response;
		}
	}
	
	@Encrypted
	@JobRoleAccess(featureIds = {34})
	@GetMapping(value = "/getProjectAssignedDataByProjectId")
	public ServiceResponse getProjectAssignedData(@RequestParam Long id , @RequestParam int totalRequirements) {
		try {
			ServiceResponse response = resourceManagementService.getAssignedDataForAProject(id,totalRequirements);		
			return response;
			
		}
		catch(Exception e) {
			ServiceResponse response = new ServiceResponse();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse(e.getMessage());
            System.err.println(e.getMessage());
            return response;
		}
	}
	
	@PostMapping(value = "/empCountSEDepartmentsWise")
	public ServiceResponse empCountDepartmentsWise(@RequestBody FilterMatrix filterMatrix) {
		ServiceResponse response = resourceManagementService.empCountDepartmentsWise(filterMatrix);
		return response;
	}
	
	// @Encrypted
	@GetMapping("/getProjectConfigurationDetailsByProjectId")
	public ServiceResponse getProjectConfigurationDetailsByProjectId(@RequestParam Integer projectId, @RequestParam boolean isAllProjects) {
		return resourceManagementService.getProjectConfigurationDetailsByProjectId(projectId, isAllProjects);
	}
	
	// @Encrypted
	@GetMapping("/getResourceRequirementByPoId")
	public ServiceResponse getResourceRequirementByPoId(@RequestParam Long poId) {
		return resourceManagementService.getResourceRequirementByPoId(poId);
	}

	// @Encrypted
	@GetMapping("/getActivePoDetailsByProjectId")
	public ServiceResponse getActivePoDetailsByProjectId(@RequestParam Integer projectId) {
		return resourceManagementService.getActivePoDetailsByProjectId(projectId);
	}

	// @Encrypted
	@GetMapping("/getResourceRequirementByTeamId")
	public ServiceResponse getResourceRequirementByTeamId(@RequestParam Long teamId) {
		return resourceManagementService.getResourceRequirementByTeamId(teamId);
	}

	@PostMapping("/ishineToPoEmpDetails")
	public ServiceResponse ishineToPoEmpDetails(HttpServletRequest httpRequest, @RequestBody IshineToPoRequestDTO ishineToPoRequest) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		return resourceManagementService.ishineToPoEmpDetails(ishineToPoRequest);
	}

	// @Encrypted
	@GetMapping("/getResourceRequirementCountByProjectId")
	public ServiceResponse getResourceRequirementCountByProjectId(
			@RequestParam Integer projectId, @RequestParam String projectType) {
		return resourceManagementService.getResourceRequirementCountByProjectId(projectId, projectType);
	}

	// @Encrypted
	@GetMapping("/getResourceRequirementDetailsByProjectId")
	public ServiceResponse getResourceRequirementDetailsByProjectId(
			@RequestParam Integer projectId, @RequestParam String projectType, @RequestParam boolean currentActivePO) {
		return resourceManagementService.getResourceRequirementDetailsByProjectId(projectId, projectType,currentActivePO);
	}
	
	@GetMapping("/oneTimeUpdatePoClientId")
	public ServiceResponse oneTimeUpdatePoClientId() {
		return resourceManagementService.oneTimeUpdatePoClientId("once");
	}
	
	@Scheduled(cron = "0 0 0 * * ?")
//	@GetMapping("/clientcron")
	public void cronToUpdateClient() {
		 resourceManagementService.oneTimeUpdatePoClientId("");
	}

	
////	@Scheduled(cron = "0 0 0 * * ?")
//	@GetMapping("/oneTimeUpdatePoClientId")
//	public ServiceResponse oneTimeUpdatePoClientId(@RequestParam(value = "mode", required = false) String mode) {
//		return resourceManagementService.oneTimeUpdatePoClientId(mode);
//
//	}


	// @Encrypted
	@PostMapping("/fetchProjectDetailsList")
	public ServiceResponse fetchProjectDetailsList(@RequestBody RMGDashboardProjectRequest rmgDashboardProjectRequest) {
		return resourceManagementService.fetchProjectDetailsList(rmgDashboardProjectRequest);
	}

	// @Encrypted
	@PostMapping("/getEmployeeCountByEmployeeGroup")
	public ServiceResponse getEmployeeCountByEmployeeGroup(@RequestBody RMGDashboardProjectRequest rmgDashboardProjectRequest) {
		return resourceManagementService.getEmployeeCountByEmployeeGroup(rmgDashboardProjectRequest);
	}

	// @Encrypted
	@PostMapping("/getEmployeeDetailsListByEmployeeGroup")
	public ServiceResponse getEmployeeDetailsListByEmployeeGroup(@RequestBody PageDTO pageDTO) {
		return resourceManagementService.getEmployeeDetailsListByEmployeeGroup(pageDTO);
	}

	// @Encrypted
	@PostMapping("/getProjectStatusCount")
	public ServiceResponse getProjectStatusCount(@RequestBody RMGDashboardProjectRequest rmgDashboardProjectRequest) {
		return resourceManagementService.getProjectStatusCount(rmgDashboardProjectRequest);
	}

	// @Encrypted
	@PostMapping("/getUnfilledTimesheetProjectDetailsList")
	public ServiceResponse getUnfilledTimesheetProjectDetailsList(@RequestBody PageDTO pageDTO) {
		return resourceManagementService.getUnfilledTimesheetProjectDetailsList(pageDTO);
	}	

	// @Encrypted
	@PostMapping("/getUnfilledTimesheetProjectDetailsCount")
	public ServiceResponse getUnfilledTimesheetProjectDetailsCount(@RequestBody RMGDashboardProjectRequest rmgDashboardProjectRequest) {
		return resourceManagementService.getUnfilledTimesheetProjectDetailsCount(rmgDashboardProjectRequest);
	}
	
	//API called by PoPortal(Shankh) application to check if iShine is up and running
	@GetMapping("/healthCheck")
	public String healthCheck(HttpServletRequest httpRequest) {
	    poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
	    return "iShine is online...";
	}

	@PostMapping("/getEmployeeTeamDepartment")
	public ServiceResponse getEmployeeTeamDepartment(@RequestBody EmployeeTeamDepartmentDTO dto) {
		Long teamId = dto.getTeamId();
		Long empId = dto.getEmpId();
		LocalDate date = dto.getDate();
		if(teamId == null || empId == null) {
			return null;
		}
		return resourceManagementService.getEmployeeTeamDepartment(teamId , empId , date);
	}

	// @Encrypted
	@PostMapping("/getBillingLossRiskScore")
	public ServiceResponse getBillingLossRiskScore(@RequestBody RMGDashboardProjectRequest rmgDashboardProjectRequest) {
		return resourceManagementService.getBillingLossRiskScore(rmgDashboardProjectRequest);
	}
	
	// @Encrypted
	@PostMapping("/getExpiredTNMFilterWiseProjectStatusCount")
	public ServiceResponse getExpiredTNMFilterWiseProjectStatusCount(@RequestBody RMGDashboardProjectRequest rmgDashboardProjectRequest) {
		return resourceManagementService.getExpiredTNMFilterWiseProjectStatusCount(rmgDashboardProjectRequest);
	}
	
	// @Encrypted
		@PostMapping("/getFCFilterWiseProjectStatusCount")
		public ServiceResponse getFCFilterWiseProjectStatusCount(@RequestBody RMGDashboardProjectRequest rmgDashboardProjectRequest) {
			return resourceManagementService.getFCFilterWiseProjectStatusCount(rmgDashboardProjectRequest);
		}
	
	// @Encrypted
	@PostMapping("/getAllUnfilledTimesheetProjectDetailsCount")
	public ServiceResponse getAllUnfilledTimesheetProjectDetailsCount(@RequestBody RMGDashboardProjectRequest rmgDashboardProjectRequest) {
		return resourceManagementService.getAllUnfilledTimesheetProjectDetailsCount(rmgDashboardProjectRequest);
	}
	
	@GetMapping("/getEmployeeMappedToClientPercent")
	public ServiceResponse getEmployeeMappedToClientPercent() {
		return resourceManagementService.getEmployeeMappedToClientPercent();
	}
	
}