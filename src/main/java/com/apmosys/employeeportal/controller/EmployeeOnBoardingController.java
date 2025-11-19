package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.AssetDTO;
import com.apmosys.employeeportal.service.EmployeeOnBoardingService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class EmployeeOnBoardingController {
	
	@Autowired
	EmployeeOnBoardingService employeeOnBoardingService;

	@RequestMapping(value = "/getEmployeeOnBoardingDetailByEmployeementId", method = RequestMethod.POST)
	public ServiceResponse getEmployeeOnBoardingDetailByEmployeementId(@RequestBody AssetDTO assetDTO) {
		ServiceResponse response = employeeOnBoardingService.getEmployeeOnBoardingDetailByEmployeementId(assetDTO);
		return response;
	}
	
	@RequestMapping(value = "/updateOnBoardingCheckList", method = RequestMethod.POST)
	public ServiceResponse updateOnBoardingCheckList(@RequestBody AssetDTO assetDTO) throws Exception {
		ServiceResponse response = employeeOnBoardingService.updateOnBoardingCheckList(assetDTO);
		return response;
	}
	
	@RequestMapping(value = "/getAssetDataFromSnipitPortal", method = RequestMethod.POST)
	public ServiceResponse getAssetDataFromSnipitPortal(@RequestBody AssetDTO assetDTO) {
		ServiceResponse response = employeeOnBoardingService.getAssetDataFromSnipitPortal(assetDTO);
		return response;
	}

	/*
	  create Asset Mapping for all old employee : later mapping will generate on create Employee
	  */
	
	@RequestMapping(value = "/createEmployeeAssetMapping", method = RequestMethod.GET)
	public ServiceResponse createEmployeeAssetMapping() {
		ServiceResponse response = employeeOnBoardingService.createEmployeeAssetMapping();
		return response;
	}
	
}
