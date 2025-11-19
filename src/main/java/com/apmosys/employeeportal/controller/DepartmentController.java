package com.apmosys.employeeportal.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.JobRoleAccess;
import com.apmosys.employeeportal.dto.DepartmentDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.service.DepartmentService;
import com.apmosys.employeeportal.utility.PoPortalAPIAuthenticationJWTUtility;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class DepartmentController {

	@Autowired
	DepartmentService departmentService;
	
	@Autowired
	PoPortalAPIAuthenticationJWTUtility poPortalAPIAuthenticationJWTUtility;

	@JobRoleAccess(featureIds = {4})
	@RequestMapping(value = "/createDepartment", method = RequestMethod.POST)
	public ServiceResponse createDepartment(@RequestBody DepartmentDTO departmentDTO) {
		ServiceResponse response = departmentService.createDepartment(departmentDTO);
		return response;
	}
	//test
	@RequestMapping(value="/createDepartmentByList" , method = RequestMethod.POST, consumes="application/json")
	public ServiceResponse createEmployeeByList(@RequestBody DepartmentDTO[] departmentDTO) {	
		
		ServiceResponse response = null;
		
		 for (DepartmentDTO department: departmentDTO) {
			 response = departmentService.createDepartmentByList(department);
		    }	
		return response;
	}

	@JobRoleAccess(featureIds = {3,4,5,14,36,48,26,27,53,25,33,8,34,36,64})
	@RequestMapping(value = "/getAllDepartments", method = RequestMethod.GET)
	public ServiceResponse getAllDepartments() {
		ServiceResponse response = departmentService.getAllDepartments();
		return response;
	}
	
	@RequestMapping(value = "/getAllDepartmentsByProjectId", method = RequestMethod.POST)
	public ServiceResponse getAllDepartmentsByProjectId(@RequestBody ProjectDTO projectDto) {
		ServiceResponse response = departmentService.getAllDepartmentsByProjectId(projectDto.getProjectId());
		return response;
	}
		
	@JobRoleAccess(featureIds = {34,26})
	@GetMapping("/getAllDepartmentsFromId/{id}")
	public ServiceResponse getAllDepartmentsFromId(@PathVariable("id") Long id) {
		ServiceResponse response = departmentService.getAllDepartmentsFromId(id);
		return response;
	}

	@JobRoleAccess(featureIds = {4})
	@RequestMapping(value = "/updateDepartment", method = RequestMethod.POST)
	public ServiceResponse updateDepartment(@RequestBody DepartmentDTO departmentDTO) {
		ServiceResponse response = departmentService.updateDepartment(departmentDTO);
		return response;
	}

	@JobRoleAccess(featureIds = {4})
	@PostMapping(value = "/deleteDepartment")
	public ServiceResponse deleteDepartment(@RequestBody DepartmentDTO departmentDTO) {
		return departmentService.deleteDepartment(departmentDTO);
	}
	
	@JobRoleAccess(featureIds = {4})
	@PostMapping(value = "/changeDepartmentJobRoleMapping")
	public ServiceResponse changeDepartmentJobRoleMapping(HttpServletRequest httpRequest, @RequestBody DepartmentDTO departmentDTO) {
		return departmentService.changeDepartmentJobRoleMapping(departmentDTO);
	}

	@JobRoleAccess(featureIds = {4})
	@RequestMapping(value = "/checkDepartmentName", method = RequestMethod.POST)
	public ServiceResponse checkDepartmentName(@RequestBody DepartmentDTO departmentDTO) {
		ServiceResponse response = departmentService.checkDepartmentName(departmentDTO);
		return response;
	}

	@GetMapping(value = "/getAllDeptsList")
	public ServiceResponse getAllDeptsList() {
		ServiceResponse response = departmentService.getAllDeptsList();
		return response;
	}
	
	/*
	 API for PoPortal
	 */
	
	@GetMapping(value = "/getAllDepartmentInfo")
	public ServiceResponse employeeInfo(HttpServletRequest httpRequest) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		return departmentService.getAllDepartmentInfo();
	}

}
