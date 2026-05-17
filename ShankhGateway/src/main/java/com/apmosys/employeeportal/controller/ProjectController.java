package com.apmosys.employeeportal.controller;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.service.PoPortalAPIService;
import com.apmosys.employeeportal.service.ProjectService;
import com.apmosys.employeeportal.utility.PoPortalAPIAuthenticationJWTUtility;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class ProjectController {

	@Autowired
	ProjectService projectService;

	@Autowired
	PoPortalAPIService poPortalApiService;

	@Autowired
	private PoPortalAPIAuthenticationJWTUtility poPortalAPIAuthenticationJWTUtility;

	@PostMapping(value = "/poProjectTimesheetSync")
	public ServiceResponse poProjectTimesheetSync(HttpServletRequest httpRequest, @RequestBody Set<Long> poIdList) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		return projectService.poProjectTimesheetSync(poIdList);
	}

	@RequestMapping(value = "/getResourceCountListByPoprojectName", method = RequestMethod.POST)
	public ServiceResponse getResourceListByPoNumbers(HttpServletRequest httpRequest,
			@RequestBody List<String> poNumbers) {
		poPortalAPIAuthenticationJWTUtility.extractAndValidateToken(httpRequest);
		return poPortalApiService.getResourceListByPoNumbers(poNumbers);
	}
}
