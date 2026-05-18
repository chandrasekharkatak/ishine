package com.apmosys.employeeportal.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.JobRoleAccess;
import com.apmosys.employeeportal.dto.AssetDTO;
import com.apmosys.employeeportal.service.EmployeeOnBoardingService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class EmployeeOnBoardingController {
	
	@Autowired
	EmployeeOnBoardingService employeeOnBoardingService;

	@JobRoleAccess(featureIds = {8,39})
	@RequestMapping(value = "/getEmployeeOnBoardingDetailByEmployeementId", method = RequestMethod.POST)
	public ServiceResponse getEmployeeOnBoardingDetailByEmployeementId(@RequestBody AssetDTO assetDTO) {
		ServiceResponse response = employeeOnBoardingService.getEmployeeOnBoardingDetailByEmployeementId(assetDTO);
		return response;
	}
	
	@JobRoleAccess(featureIds = {39})
	@RequestMapping(value = "/updateOnBoardingCheckList", method = RequestMethod.POST)
	public ServiceResponse updateOnBoardingCheckList(@RequestBody AssetDTO assetDTO) throws Exception {
		ServiceResponse response = employeeOnBoardingService.updateOnBoardingCheckList(assetDTO);
		return response;
	}
	
	@JobRoleAccess(featureIds = {39})
	@RequestMapping(value = "/getAssetDataFromSnipitPortal", method = RequestMethod.POST)
	public ServiceResponse getAssetDataFromSnipitPortal(@RequestBody AssetDTO assetDTO) {
		ServiceResponse response = employeeOnBoardingService.getAssetDataFromSnipitPortal(assetDTO);
		return response;
	}

	/*
	  create Asset Mapping for all old employee : later mapping will generate on create Employee
	  */
	@JobRoleAccess(featureIds = {39})
	@RequestMapping(value = "/createEmployeeAssetMapping", method = RequestMethod.GET)
	public ServiceResponse createEmployeeAssetMapping() {
		ServiceResponse response = employeeOnBoardingService.createEmployeeAssetMapping();
		return response;
	}
	

	
}
