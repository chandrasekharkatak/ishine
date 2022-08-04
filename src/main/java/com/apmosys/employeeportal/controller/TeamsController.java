package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
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
	
	@RequestMapping(value = "/updateTeam", method = RequestMethod.POST)
	public ServiceResponse updateTeam(@RequestBody TeamDTO teamDTO) {

		ServiceResponse response = teamsService.updateTeam(teamDTO);
		return response;
	}
	
//	MyTeam Contoller
	
	@RequestMapping(value="/getAllTeamView" , method = RequestMethod.POST)
	public ServiceResponse getAllTeamView(@RequestBody EmployeeDTO employeedto) {		
		
		ServiceResponse response =	teamsService.getAllTeamView(employeedto);		
		return response;
	}
	
	@RequestMapping(value="/getAllTeamMemberView" , method = RequestMethod.POST)
	public ServiceResponse getAllTeamMemberView(@RequestBody EmployeeDTO employeedto) {		
		
		ServiceResponse response =	teamsService.getAllTeamMemberView(employeedto);		
		return response;
	}
	
	@RequestMapping(value="/getAllTeamLeaveHistoryView" , method = RequestMethod.POST)
	public ServiceResponse getAllTeamLeaveHistoryView(@RequestBody LeaveDTO leaveDTO) {		
		
		ServiceResponse response =	teamsService.getAllTeamLeaveHistoryView(leaveDTO);		
		return response; 
	}
	
	@RequestMapping(value="/getAllTeamCompOffHistoryView" , method = RequestMethod.POST)
	public ServiceResponse getAllTeamCompOffHistoryView(@RequestBody LeaveDTO leaveDTO) {		
		
		ServiceResponse response =	teamsService.getAllTeamCompOffHistoryView(leaveDTO);		
		return response;
	}

}
