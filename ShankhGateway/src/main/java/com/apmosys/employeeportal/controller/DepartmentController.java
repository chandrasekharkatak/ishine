package com.apmosys.employeeportal.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

	@GetMapping(value = "/getAllDepartmentInfo")
	public ServiceResponse employeeInfo(HttpServletRequest httpRequest) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		return departmentService.getAllDepartmentInfo();
	}
}
