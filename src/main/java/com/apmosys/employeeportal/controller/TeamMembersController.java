package com.apmosys.employeeportal.controller;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.EmployeeOtherActiveProject;
import com.apmosys.employeeportal.dto.EmployeeProjectTimesheetDto;
import com.apmosys.employeeportal.dto.MigrateTeam;
import com.apmosys.employeeportal.dto.PoDetailsDto;
import com.apmosys.employeeportal.dto.RmgTeamDto;
import com.apmosys.employeeportal.dto.RmgTeamMemberDto;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.EmployeeTimesheetDTO;
import com.apmosys.employeeportal.service.TeamMembersService;
import com.apmosys.employeeportal.utility.ServiceResponse;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping(path = "/api")
@AllArgsConstructor
public class TeamMembersController {

	private final TeamMembersService teamMembersService;

	// @Encrypted
	@PostMapping("/addOrUpdateTeamMembers")
	public ServiceResponse addOrUpdateTeamMembers(@RequestBody RmgTeamDto rmgTeamDto) {
		return teamMembersService.addOrUpdateTeamMembers(rmgTeamDto);
	}

	// @Encrypted
	@PostMapping("/updateDefaultProjectCompletion")
	public ServiceResponse updateDefaultProjectCompletion(@RequestBody RmgTeamMemberDto rmgTeamMemberDto) {
		return teamMembersService.updateDefaultProjectCompletion(rmgTeamMemberDto);
	}

	// @Encrypted
	@PostMapping("/removeTeamMembersFromProject")
	public ServiceResponse removeTeamMembersFromProject(@RequestBody RmgTeamDto rmgTeamDto) {
		return teamMembersService.removeTeamMembersFromProject(rmgTeamDto);
	}

	// @Encrypted
	@PostMapping("/updateMappingToOtherProjectAsDefault")
	public ServiceResponse updateMappingToOtherProjectAsDefault(
			@RequestBody EmployeeOtherActiveProject employeeOtherActiveProject) {
		return teamMembersService.updateMappingToOtherProjectAsDefault(employeeOtherActiveProject);
	}

	// @Encrypted
	@PostMapping("/migrateTeamMembers")
	public ServiceResponse migrateTeamMembers(@RequestBody MigrateTeam migrateTeam) {
		return teamMembersService.migrateTeamMembers(migrateTeam);
	}

	// @Encrypted
	@PostMapping("/validateEmployeeProjectStartDate")
	public ServiceResponse validateEmployeeProjectStartDate(@RequestBody RmgTeamMemberDto rmgTeamMemberDto) {
		return teamMembersService.validateEmployeeProjectStartDate(rmgTeamMemberDto);
	}

	// @Encrypted
	@PostMapping("/extendTeamMembersEndDate")
	public ServiceResponse extendTeamMembersEndDate(@RequestBody RmgTeamDto rmgTeamDto) {
		return teamMembersService.extendTeamMembersEndDate(rmgTeamDto);
	}

	// @Encrypted
	@PostMapping("/getTeamDetailsByProjectId")
	public ServiceResponse getTeamDetailsByProjectId(@RequestBody PoDetailsDto poDetailsDto) {
		return teamMembersService.getTeamDetailsByProjectId(poDetailsDto);
	}

	// @Encrypted
	@PostMapping("/updateMemberShadowMapping")
	public ServiceResponse updateMemberShadowMapping(@RequestBody RmgTeamMemberDto rmgTeamMemberDto) {
		return teamMembersService.updateMemberShadowMapping(rmgTeamMemberDto);
	}

	// @Encrypted
	@PostMapping("/updateTeamMembersStartDateAndEndDate")
	public ServiceResponse updateTeamMembersStartDateAndEndDate(@RequestBody EmployeeProjectTimesheetDto employeeProjectTimesheetDto) {
		return teamMembersService.updateTeamMembersStartDateAndEndDate(employeeProjectTimesheetDto);
	}

	@Scheduled(cron = "0 0 1 * * *")
	@GetMapping("/triggerUnmappedEmployeeProjectNotificationJob")
	public void triggerUnmappedEmployeeProjectNotificationJob() {
		teamMembersService.sendDepartmentWiseUnmappedEmployeeProjectMail();
	}

}
