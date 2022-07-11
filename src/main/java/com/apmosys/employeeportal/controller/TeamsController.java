package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.TeamDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.service.TeamsService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class TeamsController {

	@Autowired
	TeamsService teamsService;

	@RequestMapping(value = "/getAllProjectListByProjectManagerId", method = RequestMethod.POST)
	public ServiceResponse getAllProjectListByProjectManagerId(@RequestBody TimesheetDTO timesheetDTO) {

		ServiceResponse response = teamsService.getAllProjectListByProjectManagerId(timesheetDTO);
		return response;
	}

	@RequestMapping(value = "/createTeam", method = RequestMethod.POST)
	public ServiceResponse createTeam(@RequestBody TeamDTO teamDTO) {

		ServiceResponse response = teamsService.createTeam(teamDTO);
		return response;
	}

	@RequestMapping(value = "/getAllTeamsByProjectId", method = RequestMethod.POST)
	public ServiceResponse getAllTeamsByProjectId(@RequestBody TeamDTO teamDTO) {

		ServiceResponse response = teamsService.getAllTeamsByProjectId(teamDTO);
		return response;
	}

	@RequestMapping(value = "/deleteTeam", method = RequestMethod.POST)
	public ServiceResponse deleteTeam(@RequestBody TeamDTO teamDTO) {

		ServiceResponse response = teamsService.deleteTeam(teamDTO);
		return response;
	}
	
	@RequestMapping(value = "/getTeamMembersByTeamId", method = RequestMethod.POST)
	public ServiceResponse getTeamMembersByTeamId(@RequestBody TeamDTO teamDTO) {

		ServiceResponse response = teamsService.getTeamMembersByTeamId(teamDTO);
		return response;
	}

}
