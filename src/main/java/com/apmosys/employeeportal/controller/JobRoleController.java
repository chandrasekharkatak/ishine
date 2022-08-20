package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.DepartmentDTO;
import com.apmosys.employeeportal.dto.JobRoleDTO;
import com.apmosys.employeeportal.service.JobRoleService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class JobRoleController {
	
	@Autowired
	JobRoleService jobRoleService;
	
	
	
	@RequestMapping(value = "/createJobRole" ,method = RequestMethod.POST)
	public ServiceResponse createJobRole(@RequestBody JobRoleDTO jobRoleDTO) {
		
		ServiceResponse response = jobRoleService.createJobRole(jobRoleDTO);
		return response;
	}
	
	@RequestMapping(value="/createJobRoleByList" , method = RequestMethod.POST, consumes="application/json")
	public ServiceResponse createEmployeeByList(@RequestBody JobRoleDTO[] jobRoleDTO) {	
		
		ServiceResponse response = null;
		
		 for (JobRoleDTO jobrole: jobRoleDTO) {
			 response = jobRoleService.createJobRoleByList(jobrole);
		    }	
		return response;
	}
	
	@RequestMapping(value = "/getAllJobRole")
	public ServiceResponse getAllJobRole() {
		
		ServiceResponse response = jobRoleService.getAllJobRole();
		return response;
	}
	
	@RequestMapping(value = "/updateJobRole" ,method = RequestMethod.POST)
	public ServiceResponse updateJobRole(@RequestBody JobRoleDTO jobRoleDTO) {
		
		ServiceResponse response = jobRoleService.updateJobRole(jobRoleDTO);
		return response;
	}
	
	@RequestMapping(value = "/deleteJobRole" ,method = RequestMethod.POST)
	public ServiceResponse deleteJobRole(@RequestBody JobRoleDTO jobRoleDTO) {
		
		ServiceResponse response = jobRoleService.deleteJobRole(jobRoleDTO);
		return response;
	}
	
	@RequestMapping(value = "/changeEmployeeJobRoleMapping" ,method = RequestMethod.POST)
	public ServiceResponse changeEmployeeJobRoleMapping(@RequestBody JobRoleDTO jobRoleDTO) {
		
		ServiceResponse response = jobRoleService.changeEmployeeJobRoleMapping(jobRoleDTO);
		return response;
	}

}
