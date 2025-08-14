package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.DepartmentDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.service.DepartmentService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class DepartmentController {

	@Autowired
	DepartmentService departmentService;

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
	
	@RequestMapping(value = "/getAllDepartmentsByProjectId", method = RequestMethod.POST)
	public ServiceResponse getAllDepartmentsByProjectId(@RequestBody ProjectDTO projectDto) {
		ServiceResponse response = departmentService.getAllDepartmentsByProjectId(projectDto.getProjectId());
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

	@RequestMapping(value = "/deleteDepartment", method = RequestMethod.POST)
	public ServiceResponse deleteDepartment(@RequestBody DepartmentDTO departmentDTO) {
		ServiceResponse response = departmentService.deleteDepartment(departmentDTO);
		return response;
	}
	
	@RequestMapping(value = "/changeDepartmentJobRoleMapping", method = RequestMethod.POST)
	public ServiceResponse changeDepartmentJobRoleMapping(@RequestBody DepartmentDTO departmentDTO) {
		ServiceResponse response = departmentService.changeDepartmentJobRoleMapping(departmentDTO);
		return response;
	}
	
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
	
	@RequestMapping(value = "/getAllDepartmentInfo", method = RequestMethod.GET)
	public ServiceResponse employeeInfo() {

		ServiceResponse response = departmentService.getAllDepartmentInfo();
		return response;
	}
	
}
