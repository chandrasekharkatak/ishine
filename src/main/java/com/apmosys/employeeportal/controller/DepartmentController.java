package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.DepartmentDTO;
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

	@RequestMapping(value = "/getAllDepartments", method = RequestMethod.GET)
	public ServiceResponse getAllDepartments() {
		ServiceResponse response = departmentService.getAllDepartments();
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
	
	@RequestMapping(value = "/updateDepartmentHolidayMappings", method = RequestMethod.POST)
	public ServiceResponse updateDepartmentHolidayMappings(@RequestBody DepartmentDTO departmentDTO) {
		ServiceResponse response = departmentService.updateDepartmentHolidayMappings(departmentDTO);
		return response;
	}

}
