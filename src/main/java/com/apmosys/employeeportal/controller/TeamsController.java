package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.JobRoleAccess;
import com.apmosys.employeeportal.dto.ActivityDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.TeamDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.service.EmployeeService;
import com.apmosys.employeeportal.service.TeamsService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class TeamsController {

	@Autowired
	TeamsService teamsService;
	
	@Autowired
	EmployeeService employeeService;
	@JobRoleAccess(featureIds = {7})
	@RequestMapping(value = "/getAllProjectListByProjectManagerId", method = RequestMethod.POST)
	public ServiceResponse getAllProjectListByProjectManagerId(@RequestBody TimesheetDTO timesheetDTO) {

		ServiceResponse response = teamsService.getAllProjectListByProjectManagerId(timesheetDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {7})
	@RequestMapping(value = "/createTeam", method = RequestMethod.POST)
	public ServiceResponse createTeam(@RequestBody TeamDTO teamDTO) {
		
		employeeService.clearEmployeeCache();
		ServiceResponse response = teamsService.createTeam(teamDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {7})
	@RequestMapping(value = "/getAllTeamsByProjectId", method = RequestMethod.POST)
	public ServiceResponse getAllTeamsByProjectId(@RequestBody TeamDTO teamDTO) {

		ServiceResponse response = teamsService.getAllTeamsByProjectId(teamDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {7,34})
	@RequestMapping(value = "/deleteTeam", method = RequestMethod.POST)
	public ServiceResponse deleteTeam(@RequestBody TeamDTO teamDTO) {
		
		employeeService.clearEmployeeCache();
		ServiceResponse response = teamsService.deleteTeam(teamDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {7})
	@RequestMapping(value = "/getTeamMembersByTeamId", method = RequestMethod.POST)
	public ServiceResponse getTeamMembersByTeamId(@RequestBody TeamDTO teamDTO) {

		ServiceResponse response = teamsService.getTeamMembersByTeamId(teamDTO);
		return response;
	}
	@RequestMapping(value = "/getTeamMembersByTeamIdBiomax", method = RequestMethod.POST)
	public ServiceResponse getTeamMembersByTeamIdBiomax(@RequestBody TeamDTO teamDTO) {

		ServiceResponse response = teamsService.getTeamMembersByTeamIdBiomax(teamDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {7})
	@RequestMapping(value = "/updateTeam", method = RequestMethod.POST)
	public ServiceResponse updateTeam(@RequestBody TeamDTO teamDTO) {
		
		employeeService.clearEmployeeCache();
		ServiceResponse response = teamsService.updateTeam(teamDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {7})
	@RequestMapping(value = "/getAllMyTeamsByEmpId", method = RequestMethod.POST)
	public ServiceResponse getAllMyTeamsByEmpId(@RequestBody EmployeeDTO employeeDTO) {

		ServiceResponse response = teamsService.getAllMyTeamsByEmpId(employeeDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {7})
	@RequestMapping(value = "/getMappedActivityPreview", method = RequestMethod.POST)
	public ServiceResponse getMappedActivityPreview(@RequestBody TeamDTO teamDTO) {

		ServiceResponse response = teamsService.getMappedActivityPreview(teamDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {7})
	@RequestMapping(value = "/getMappedActivityInUpdateTeam", method = RequestMethod.POST)
	public ServiceResponse getMappedActivityInUpdateTeam(@RequestBody TeamDTO teamDTO) {

		ServiceResponse response = teamsService.getMappedActivityInUpdateTeam(teamDTO);
		return response;
	}
	
//	MyTeam Contoller
	@JobRoleAccess(featureIds = {14})
	@RequestMapping(value="/getAllTeamView" , method = RequestMethod.POST)
	public ServiceResponse getAllTeamView(@RequestBody EmployeeDTO employeedto) {		
		
		ServiceResponse response =	teamsService.getAllTeamView(employeedto);		
		return response;
	}
	
	@JobRoleAccess(featureIds = {9,13,15,16})
	@RequestMapping(value="/getAllTeamMemberView" , method = RequestMethod.POST)
	public ServiceResponse getAllTeamMemberView(@RequestBody EmployeeDTO employeedto) {		
		
		ServiceResponse response =	teamsService.getAllTeamMemberView(employeedto);		
		return response;
	}
	@JobRoleAccess(featureIds = {14})
	@RequestMapping(value="/getAllTeamLeaveHistoryView" , method = RequestMethod.POST)
	public ServiceResponse getAllTeamLeaveHistoryView(@RequestBody LeaveDTO leaveDTO) {		
		
		ServiceResponse response =	teamsService.getAllTeamLeaveHistoryView(leaveDTO);		
		return response; 
	}
	@JobRoleAccess(featureIds = {14})
	@RequestMapping(value="/getAllTeamCompOffHistoryView" , method = RequestMethod.POST)
	public ServiceResponse getAllTeamCompOffHistoryView(@RequestBody LeaveDTO leaveDTO) {		
		
		ServiceResponse response =	teamsService.getAllTeamCompOffHistoryView(leaveDTO);		
		return response;
	}
	@JobRoleAccess(featureIds = {14})
	@RequestMapping(value="/getAllTeamCompOffHistoryViewByEmpId" , method = RequestMethod.POST)
	public ServiceResponse getAllTeamCompOffHistoryViewByEmpId(@RequestBody LeaveDTO leaveDTO) {		
		
		ServiceResponse response =	teamsService.getAllTeamCompOffHistoryViewByEmpId(leaveDTO);		
		return response;
	}
	@JobRoleAccess(featureIds = {7,34})
	@RequestMapping(value="/checkTeamName" , method = RequestMethod.POST)
	public ServiceResponse checkTeamName(@RequestBody TeamDTO teamdto) {
		
		ServiceResponse response = teamsService.checkTeamName(teamdto);
		return response;
	}
	@JobRoleAccess(featureIds = {14})
	@RequestMapping(value="/getDepartmentLeaveHistory" , method = RequestMethod.POST)
	public ServiceResponse getDepartmentLeaveHistory(@RequestBody LeaveDTO leaveDTO) {		
		
		ServiceResponse response =	teamsService.getDepartmentLeaveHistory(leaveDTO);	
		return response; 
	}
	@JobRoleAccess(featureIds = {14})
	@RequestMapping(value="/getDepartmentPendingLeaveHistory" , method = RequestMethod.POST)
	public ServiceResponse getDepartmentPendingLeaveHistory(@RequestBody LeaveDTO leaveDTO) {		
		
		ServiceResponse response =	teamsService.getDepartmentPendingLeaveHistory(leaveDTO);	
		return response; 
	}
	
	@JobRoleAccess(featureIds = {14})
	@RequestMapping(value="/revokeReporteeLeave" , method = RequestMethod.POST)
	public ServiceResponse revokeReporteeLeave(@RequestBody LeaveDTO leaveDTO) {		
		
		ServiceResponse response =	teamsService.revokeReporteeLeave(leaveDTO);	
		return response; 
	}
	
	@JobRoleAccess(featureIds = {7})
	@RequestMapping(value = "/getAllTeams", method = RequestMethod.GET)
	public ServiceResponse getAllTeams() {

		ServiceResponse response = teamsService.getAllTeams();
		return response;
	}
	
	/*
	 Team, Project, Activity  Data Migration : 17/10/2022 - Harshit
	  */
	
	@RequestMapping(value = "/migrateTeamList", method = RequestMethod.POST, consumes = "application/json")
	public ServiceResponse migrateTeamList(@RequestBody TeamDTO[] teamDTO) {

		ServiceResponse response = null;

		for (TeamDTO team : teamDTO) {
			response = teamsService.migrateTeamList(team);
		}
		return response;
	}
	
	@RequestMapping(value = "/migrateTeamActivityByList", method = RequestMethod.POST, consumes = "application/json")
	public ServiceResponse migrateTeamActivityByList(@RequestBody ActivityDTO[] activityDTO) {

		ServiceResponse response = null;

		for (ActivityDTO activity : activityDTO) {
			response = teamsService.migrateTeamActivityByList(activity);
		}
		return response;
	}
	
	@RequestMapping(value = "/migrateProjectByList", method = RequestMethod.POST, consumes = "application/json")
	public ServiceResponse migrateProjectByList(@RequestBody ProjectDTO[] projectDTO) {

		ServiceResponse response = null;

		for (ProjectDTO project : projectDTO) {
			response = teamsService.migrateProjectByList(project);
		}
		return response;
	}
	
	@RequestMapping(value = "/migrateClientByList", method = RequestMethod.POST, consumes = "application/json")
	public ServiceResponse migrateClientByList(@RequestBody ProjectDTO[] projectDTO) {

		ServiceResponse response = null;

		for (ProjectDTO project : projectDTO) {
			response = teamsService.migrateClientByList(project);
		}
		return response;
	}
	
	@RequestMapping(value = "/migrateClientLocationByList", method = RequestMethod.POST, consumes = "application/json")
	public ServiceResponse migrateClientLocationByList(@RequestBody ProjectDTO[] projectDTO) {

		ServiceResponse response = null;

		for (ProjectDTO project : projectDTO) {
			response = teamsService.migrateClientLocationByList(project);
		}
		return response;
	}
	
	@RequestMapping(value = "/updateProjectByList", method = RequestMethod.POST, consumes = "application/json")
	public ServiceResponse updateProjectByList(@RequestBody ProjectDTO[] projectDTO) {

		ServiceResponse response = null;
		employeeService.clearEmployeeCache();

		for (ProjectDTO project : projectDTO) {
			response = teamsService.updateProjectByList(project);
		}
		return response;
	}
	
	@RequestMapping(value = "/migrateTeamMember", method = RequestMethod.POST, consumes = "application/json")
	public ServiceResponse migrateTeamMember(@RequestBody ProjectDTO[] projectDTO) {

		ServiceResponse response = null;

		for (ProjectDTO project : projectDTO) {
			response = teamsService.migrateTeamMember(project);
		}
		return response;
	}
	
	@RequestMapping(value="/addDepartmentInProjects" , method = RequestMethod.GET)
	public ServiceResponse addDepartmentInProjects() {
		
		ServiceResponse response = teamsService.addDepartmentInProjects();
		return response;
	}
	
	@RequestMapping(value = "/setPoProjectIdAndDepartment", method = RequestMethod.POST, consumes = "application/json")
	public ServiceResponse setPoProjectIdAndDepartment(@RequestBody ProjectDTO[] projectDTO) {

		ServiceResponse response = null;

		for (ProjectDTO project : projectDTO) {
			response = teamsService.setPoProjectIdAndDepartment(project);
		}
		return response;
	}
	
	@RequestMapping(value="/addDeptIdsInActivities" , method = RequestMethod.GET)
	public ServiceResponse addDeptIdsInActivities() {
		
		ServiceResponse response = teamsService.addDeptIdsInActivities();
		return response;
	}
	
	@RequestMapping(value="/addProjectDepartmentMapping" , method = RequestMethod.GET)
	public ServiceResponse addProjectDepartmentMapping() {
		
		ServiceResponse response = teamsService.addProjectDepartmentMapping();
		return response;
	}
	
	@RequestMapping(value="/addProjectManager" , method = RequestMethod.GET)
	public ServiceResponse addProjectManager() {
		
		ServiceResponse response = teamsService.addProjectManager();
		return response;
	}

	// @Encrypted
	@JobRoleAccess(featureIds = { 7 })
	@GetMapping("/getAllTeamsAndRoleWiseMembersByPoId")
	public ServiceResponse getAllTeamsAndRoleWiseMembersByPoId(@RequestParam Long poId) {
		return teamsService.getAllTeamsAndRoleWiseMembersByPoId(poId);
	}
	
}
