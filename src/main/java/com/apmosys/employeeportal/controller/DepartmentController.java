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

import com.apmosys.employeeportal.dto.DepartmentDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
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

	@RequestMapping(value = "/getAllDepartments", method = RequestMethod.GET)
	public ServiceResponse getAllDepartments() {
		ServiceResponse response = departmentService.getAllDepartments();
		return response;
	}
	
	@GetMapping("/getAllDepartmentsFromId/{id}")
	public ServiceResponse getAllDepartmentsFromId(@PathVariable("id") Long id) {
		ServiceResponse response = departmentService.getAllDepartmentsFromId(id);
		return response;
	}

	@RequestMapping(value = "/updateDepartment", method = RequestMethod.POST)
	public ServiceResponse updateDepartment(@RequestBody DepartmentDTO departmentDTO) {
		ServiceResponse response = departmentService.updateDepartment(departmentDTO);
		return response;
	}

	@PostMapping(value = "/deleteDepartment")
	public ServiceResponse deleteDepartment(@RequestBody DepartmentDTO departmentDTO) {
		return departmentService.deleteDepartment(departmentDTO);
	}
	
	@PostMapping(value = "/changeDepartmentJobRoleMapping")
	public ServiceResponse changeDepartmentJobRoleMapping(HttpServletRequest httpRequest, @RequestBody DepartmentDTO departmentDTO) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		return departmentService.changeDepartmentJobRoleMapping(departmentDTO);
	}

	@RequestMapping(value = "/checkDepartmentName", method = RequestMethod.POST)
	public ServiceResponse checkDepartmentName(@RequestBody DepartmentDTO departmentDTO) {
		ServiceResponse response = departmentService.checkDepartmentName(departmentDTO);
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
