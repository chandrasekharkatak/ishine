package com.apmosys.employeeportal.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.JobRoleAccess;
import com.apmosys.employeeportal.dto.DepartmentDTO;
import com.apmosys.employeeportal.dto.JobRoleDTO;
import com.apmosys.employeeportal.dto.SubFeatureMasterDTO;
import com.apmosys.employeeportal.service.JobRoleService;
import com.apmosys.employeeportal.utility.PoPortalAPIAuthenticationJWTUtility;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class JobRoleController {

	@Autowired
	JobRoleService jobRoleService;

	@Autowired
	private PoPortalAPIAuthenticationJWTUtility poPortalAPIAuthenticationJWTUtility;
	
	@JobRoleAccess(featureIds = {5})
	@RequestMapping(value = "/createJobRole", method = RequestMethod.POST)
	public ServiceResponse createJobRole(@RequestBody JobRoleDTO jobRoleDTO) {

		ServiceResponse response = jobRoleService.createJobRole(jobRoleDTO);
		return response;
	}

	
	@RequestMapping(value = "/createJobRoleByList", method = RequestMethod.POST, consumes = "application/json")
	public ServiceResponse createEmployeeByList(@RequestBody JobRoleDTO[] jobRoleDTO) {

		ServiceResponse response = null;

		for (JobRoleDTO jobrole : jobRoleDTO) {
			response = jobRoleService.createJobRoleByList(jobrole);
		}
		return response;
	}

	@JobRoleAccess(featureIds = {3,4,5,6,7,13,14,16,23,24,25,26,28,29,31,36,39,41,43,44,46,52,53,63})
	@RequestMapping(value = "/getAllJobRole")
	public ServiceResponse getAllJobRole() {

		ServiceResponse response = jobRoleService.getAllJobRole();
		return response;
	}

	@JobRoleAccess(featureIds = {5,26})
	@RequestMapping(value = "/updateJobRole", method = RequestMethod.POST)
	public ServiceResponse updateJobRole(@RequestBody JobRoleDTO jobRoleDTO) {

		ServiceResponse response = jobRoleService.updateJobRole(jobRoleDTO);
		return response;
	}

	@JobRoleAccess(featureIds = {5})
	@RequestMapping(value = "/deleteJobRole", method = RequestMethod.POST)
	public ServiceResponse deleteJobRole(@RequestBody JobRoleDTO jobRoleDTO) {

		ServiceResponse response = jobRoleService.deleteJobRole(jobRoleDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {5})
	@PostMapping(value = "/changeEmployeeJobRoleMapping")
	public ServiceResponse changeEmployeeJobRoleMapping(@RequestBody JobRoleDTO jobRoleDTO) {
		return jobRoleService.changeEmployeeJobRoleMapping(jobRoleDTO);
	}

	@RequestMapping(value = "/addNewSubFeatures", method = RequestMethod.POST)
	public ServiceResponse addNewSubFeatures(@RequestBody SubFeatureMasterDTO subFeatureMasterDTO) {

		ServiceResponse response = jobRoleService.addNewSubFeatures(subFeatureMasterDTO);
		return response;
	}
	@JobRoleAccess(featureIds = {5})
	@RequestMapping(value="/checkJobRole" , method = RequestMethod.POST)
	public ServiceResponse checkEmployeeEmail(@RequestBody JobRoleDTO jobRoleDto) {		
		
		ServiceResponse response = jobRoleService.checkJobRole(jobRoleDto);
		return response;		
	}
	@JobRoleAccess(featureIds = {26})
	@RequestMapping(value="/updateJobRoleSubFeatureMapping", method = RequestMethod.POST)
    public ServiceResponse updateJobRoleSubFeatureMapping(@RequestBody JobRoleDTO jobRoleDTO) {
		
    	ServiceResponse response = jobRoleService.updateJobRoleSubFeatureMapping(jobRoleDTO);
    	return response;
    }
	
	/*
	 PoPortal API : jobRole Info
	 */
	
	@GetMapping(value="/getAllJobRoleInfo")
    public ServiceResponse allDepartmentInfo(HttpServletRequest httpRequest) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		return jobRoleService.getAllJobRoleInfo();
    }

}
