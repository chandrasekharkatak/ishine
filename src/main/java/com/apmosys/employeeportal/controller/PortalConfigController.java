package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.ProtalConfigDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.service.PortalConfigService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api")
public class PortalConfigController {
	
	@Autowired
	PortalConfigService portalConfigService;
	
	@RequestMapping(value="/getPortalConfig", method = RequestMethod.GET)
	public ServiceResponse getPortalConfig() {
		ServiceResponse response = portalConfigService.getPortalConfig();
		return response;
	}

	@RequestMapping(value="/updatePortalConfig", method = RequestMethod.POST)
	public ServiceResponse updatePortalConfig(@RequestBody ProtalConfigDTO protalConfigDTO) {
		ServiceResponse response = portalConfigService.updatePortalConfig(protalConfigDTO);
		return response;
	}
	
	@RequestMapping(value="/generatePerviousMonthDSR", method = RequestMethod.GET)
	public ServiceResponse generatePerviousMonthDSR() {
		ServiceResponse response = portalConfigService.generatePerviousMonthDSR();
		return response;
	}
	
	@RequestMapping(value="/generateAllEmployeeDSR", method = RequestMethod.POST)
	public ServiceResponse generateAllEmployeeDSR(@RequestBody TimesheetDTO timesheetDTO) {
		ServiceResponse response = portalConfigService.generateAllEmployeeDSR(timesheetDTO);
		return response;
	}
	
	@RequestMapping(value="/segregatedDeptEodDefaulter", method = RequestMethod.GET)
	public ServiceResponse segregatedDeptEodDefaulter() {
		ServiceResponse response = portalConfigService.segregatedDeptEodDefaulter();
		return response;
	}
	
	@RequestMapping(value="/getAllEmployeeForPortalConfig", method = RequestMethod.GET)
	public ServiceResponse getAllEmployeeForPortalConfig() {
		ServiceResponse response = portalConfigService.getAllEmployeeForPortalConfig();
		return response;
	}
	
}
